import type { ItemExtractRecord } from "../extract/record.ts";
import type { PackFacetVocabulary } from "../schema/vocabulary.ts";
import type { DocumentContextByItem } from "./document_context.ts";
import type {
  LayerFile,
  LayerEntry,
  LayerFacetEntry,
} from "../deterministic/run.ts";
import type { LlmClient, QueryOptions } from "./client.ts";
import {
  runStage3,
  type Stage3Result,
} from "./run.ts";
import type { SchemaProposal, StageCorrection } from "./parse.ts";

/**
 * Select items from a first-pass stage-3 layer that warrant a second look.
 * "Warrant" means: at least one LLM facet with `ambiguous: true`.
 *
 * Only considers facets whose source is `llm:*` — non-LLM base facets are not
 * retry candidates because retry is meant to revisit model judgement.
 */
export function selectRetryCandidates(
  layer: LayerFile,
): string[] {
  const out: string[] = [];
  for (const [itemId, entry] of Object.entries(layer.entries)) {
    if (itemNeedsRetry(entry)) out.push(itemId);
  }
  return out.sort();
}

function itemNeedsRetry(entry: LayerEntry): boolean {
  for (const [, raw] of Object.entries(entry.facets)) {
    const facet = raw as LayerFacetEntry & { source?: string; ambiguous?: true };
    const source = facet.source ?? "";
    if (!source.startsWith("llm:")) continue;
    if (facet.ambiguous === true) return true;
  }
  return false;
}

export interface RetryOptions {
  /** The records that stage 3 originally ran against. */
  records: readonly ItemExtractRecord[];
  /** The first-pass stage-3 layer. */
  firstPassLayer: LayerFile;
  /** LLM client configured for the retry pass. */
  client: LlmClient;
  /** Passed through to runStage3. */
  model?: string;
  /** Same as Stage3Options.documentContextByItem. */
  documentContextByItem?: DocumentContextByItem;
  /** Items per retry batch — typically smaller than first-pass to avoid
   *  output-token truncation with verbose models. Default 8. */
  batchSize?: number;
  /** Optional per-batch progress callback. */
  onBatch?: Parameters<typeof runStage3>[0]["onBatch"];
  /** QueryOptions passthrough (timeout, abort signal, validator overrides). */
  clientOptions?: Partial<QueryOptions>;
  /** Same as Stage3Options.facetVocabulary. */
  facetVocabulary?: PackFacetVocabulary;
  /** Same as Stage3Options.promptExtras; retry should use the same prompt
   *  shape as the first pass so divergence between the two is purely about
   *  model strength, not prompt content. */
  promptExtras?: {
    verboseFacetDisambiguation?: boolean;
    verboseCommonMisconceptions?: boolean;
  };
}

export interface RetryResult extends Stage3Result {
  /** Ids the retry ran against (subset of firstPassLayer.entries). */
  retriedItems: readonly string[];
  /** Facets the retry changed (new value disagreed with first-pass). */
  facetsChanged: Record<string, number>;
  /** Facets the retry confirmed with the same value. */
  facetsConfirmed: Record<string, number>;
}

/**
 * Run stage 3 a second time with a stronger model on items the first pass
 * left ambiguous. The retry merge policy:
 *   1. For each candidate item's LLM-authored facets, the retry can confirm
 *      (same value), change (different value; retry wins), or add net-new
 *      facets the first pass skipped.
 *   2. Non-LLM base facets are never overwritten.
 *   3. Corrections / schema_proposals from the retry are returned alongside.
 *
 * The retry feeds an empty base layer so the LLM re-emits every facet freely —
 * we then compare against the first pass per-facet rather than treating
 * first-pass as locked input.
 */
export async function runStage3Retry(
  options: RetryOptions,
): Promise<RetryResult> {
  const candidates = selectRetryCandidates(options.firstPassLayer);

  const facetsChanged: Record<string, number> = {};
  const facetsConfirmed: Record<string, number> = {};
  const warnings: string[] = [];

  if (candidates.length === 0) {
    return {
      layer: options.firstPassLayer,
      filledItems: 0,
      coverageAdded: {},
      proposals: [],
      vocabularyProposals: [],
      corrections: [],
      fillIns: [],
      warnings: [],
      responseMismatches: [],
      retriedItems: [],
      facetsChanged,
      facetsConfirmed,
    };
  }

  const emptyLayer: LayerFile = {
    schema_version: options.firstPassLayer.schema_version,
    layer: options.firstPassLayer.layer,
    source: options.firstPassLayer.source,
    entries: {},
  };

  const retry = await runStage3({
    records: options.records,
    baseLayer: emptyLayer,
    client: options.client,
    model: options.model,
    batchSize: options.batchSize ?? 8,
    only: candidates,
    documentContextByItem: options.documentContextByItem,
    clientOptions: options.clientOptions,
    onBatch: options.onBatch,
    facetVocabulary: options.facetVocabulary,
    promptExtras: options.promptExtras,
  });
  warnings.push(...retry.warnings);

  const merged: Record<string, LayerEntry> = { ...options.firstPassLayer.entries };
  const coverageAdded: Record<string, number> = {};

  for (const itemId of candidates) {
    const firstPass = options.firstPassLayer.entries[itemId];
    const retryEntry = retry.layer.entries[itemId];
    if (!firstPass || !retryEntry) continue;

    const nextFacets: Record<string, LayerFacetEntry> = { ...firstPass.facets };
    for (const [facetId, retryFacet] of Object.entries(retryEntry.facets)) {
      const retrySource = (retryFacet as { source?: string }).source ?? "";
      if (!retrySource.startsWith("llm:")) continue;

      const current = firstPass.facets[facetId];
      const currentSource = (current as { source?: string })?.source ?? "";
      const currentFromLlm = currentSource.startsWith("llm:");

      if (current && !currentFromLlm) {
        // stage-2 facet — don't touch
        continue;
      }

      if (!current) {
        // net-new facet from retry
        nextFacets[facetId] = retagSource(retryFacet, "llm:stage3-retry");
        coverageAdded[facetId] = (coverageAdded[facetId] ?? 0) + 1;
        continue;
      }

      const sameValue = facetValuesEqual(current, retryFacet);
      if (sameValue) {
        nextFacets[facetId] = retagSource(retryFacet, "llm:stage3-retry");
        facetsConfirmed[facetId] = (facetsConfirmed[facetId] ?? 0) + 1;
        continue;
      }

      nextFacets[facetId] = retagSource(retryFacet, "llm:stage3-retry");
      facetsChanged[facetId] = (facetsChanged[facetId] ?? 0) + 1;
    }
    merged[itemId] = { facets: nextFacets };
  }

  const layer: LayerFile = {
    ...options.firstPassLayer,
    entries: merged,
    generated_at: new Date().toISOString(),
  };

  return {
    layer,
    filledItems: candidates.length,
    coverageAdded,
    proposals: retry.proposals,
    vocabularyProposals: retry.vocabularyProposals,
    corrections: retry.corrections,
    fillIns: retry.fillIns,
    warnings,
    responseMismatches: retry.responseMismatches,
    retriedItems: candidates,
    facetsChanged,
    facetsConfirmed,
  };
}

function retagSource(entry: LayerFacetEntry, source: string): LayerFacetEntry {
  return { ...entry, source } as LayerFacetEntry;
}

function facetValuesEqual(a: LayerFacetEntry, b: LayerFacetEntry): boolean {
  const av = (a as { value?: unknown; values?: unknown[] });
  const bv = (b as { value?: unknown; values?: unknown[] });
  if ("value" in av && "value" in bv) {
    return JSON.stringify(av.value) === JSON.stringify(bv.value);
  }
  if (Array.isArray(av.values) && Array.isArray(bv.values)) {
    const aSorted = [...av.values].sort((x, y) => String(x).localeCompare(String(y)));
    const bSorted = [...bv.values].sort((x, y) => String(x).localeCompare(String(y)));
    return JSON.stringify(aSorted) === JSON.stringify(bSorted);
  }
  return false;
}
