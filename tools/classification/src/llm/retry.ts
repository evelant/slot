import type { ItemExtractRecord } from "../extract/record.ts";
import type {
  LayerFile,
  LayerEntry,
  LayerFacetEntry,
} from "../deterministic/run.ts";
import type { LlmClient } from "./client.ts";
import { runStage3, type Stage3Result } from "./run.ts";
import type { SchemaProposal, StageCorrection } from "./parse.ts";

/**
 * Select items from a first-pass stage-3 layer that warrant a second look.
 * "Warrant" means: at least one facet with `ambiguous: true` OR with
 * confidence below `threshold`.
 *
 * Only considers facets whose source is `llm:*` — deterministic stage-2
 * facets (source: `rule:*`) aren't flagged for LLM retry because they're
 * already rule-derived.
 */
export function selectRetryCandidates(
  layer: LayerFile,
  threshold: number,
): string[] {
  const out: string[] = [];
  for (const [itemId, entry] of Object.entries(layer.entries)) {
    if (itemNeedsRetry(entry, threshold)) out.push(itemId);
  }
  return out.sort();
}

function itemNeedsRetry(entry: LayerEntry, threshold: number): boolean {
  for (const [, raw] of Object.entries(entry.facets)) {
    const facet = raw as LayerFacetEntry & { source?: string; confidence?: number; ambiguous?: true };
    const source = facet.source ?? "";
    if (!source.startsWith("llm:")) continue;
    if (facet.ambiguous === true) return true;
    if (typeof facet.confidence === "number" && facet.confidence < threshold) return true;
  }
  return false;
}

export interface RetryOptions {
  /** The records that stage 3 originally ran against. */
  records: readonly ItemExtractRecord[];
  /** The first-pass layer (haiku stage-3 output). */
  firstPassLayer: LayerFile;
  /** LLM client configured for the retry pass (usually Sonnet + high effort). */
  client: LlmClient;
  /** Confidence threshold below which an item is flagged for retry. */
  threshold?: number;
  /** Passed through to runStage3. */
  model?: string;
  /**
   * Effort level for the retry pass. We deliberately do NOT plumb through
   * `thinkingBudget` or `disableAdaptiveThinking` here — Sonnet's adaptive
   * thinking should drive itself, and forcing a fixed budget tends to make
   * it worse. The retry is the one place where the model gets to "think
   * hard," so trust it.
   */
  effort?: "low" | "medium" | "high" | "xhigh" | "max";
  /** Items per retry batch — typically smaller than first-pass to avoid
   *  output-token truncation with verbose models. Default 8. */
  batchSize?: number;
  /** Optional per-batch progress callback. */
  onBatch?: Parameters<typeof runStage3>[0]["onBatch"];
}

export interface RetryResult extends Stage3Result {
  /** Ids the retry ran against (subset of firstPassLayer.entries). */
  retriedItems: readonly string[];
  /** Facets the retry changed (new value disagreed with first-pass). */
  facetsChanged: Record<string, number>;
  /** Facets the retry confirmed (same value, higher confidence). */
  facetsConfirmed: Record<string, number>;
}

/**
 * Run stage 3 a second time with a stronger model on items the first pass
 * was unsure about. The retry merge policy:
 *   1. For each candidate item's LLM-authored facets, the retry can confirm
 *      (same value, possibly bumped confidence), change (different value,
 *      retry wins only if its confidence ≥ first pass), or add net-new
 *      facets the first pass skipped.
 *   2. Stage-2 facets (source: rule:*) are never overwritten.
 *   3. Corrections / schema_proposals from the retry are returned alongside.
 *
 * The retry feeds an empty stage-2 layer so the LLM re-emits every facet
 * freely — we then compare against the first pass per-facet rather than
 * treating first-pass as locked input.
 */
export async function runStage3Retry(
  options: RetryOptions,
): Promise<RetryResult> {
  const threshold = options.threshold ?? 0.5;
  const candidates = selectRetryCandidates(options.firstPassLayer, threshold);

  const facetsChanged: Record<string, number> = {};
  const facetsConfirmed: Record<string, number> = {};
  const warnings: string[] = [];

  if (candidates.length === 0) {
    return {
      layer: options.firstPassLayer,
      filledItems: 0,
      coverageAdded: {},
      proposals: [],
      corrections: [],
      warnings: [],
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
    stage2Layer: emptyLayer,
    client: options.client,
    model: options.model,
    batchSize: options.batchSize ?? 8,
    only: candidates,
    clientOptions: options.effort ? { effort: options.effort } : undefined,
    onBatch: options.onBatch,
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

      const retryConf = (retryFacet as { confidence?: number }).confidence ?? 0;
      const currentConf = (current as { confidence?: number })?.confidence ?? 0;

      if (!current) {
        // net-new facet from retry
        nextFacets[facetId] = retagSource(retryFacet, "llm:stage3-retry");
        coverageAdded[facetId] = (coverageAdded[facetId] ?? 0) + 1;
        continue;
      }

      const sameValue = facetValuesEqual(current, retryFacet);
      if (sameValue) {
        // retry confirms — bump confidence if higher
        if (retryConf > currentConf) {
          nextFacets[facetId] = retagSource(retryFacet, "llm:stage3-retry");
        }
        facetsConfirmed[facetId] = (facetsConfirmed[facetId] ?? 0) + 1;
        continue;
      }

      // different value — accept only if retry has higher confidence
      if (retryConf >= currentConf) {
        nextFacets[facetId] = retagSource(retryFacet, "llm:stage3-retry");
        facetsChanged[facetId] = (facetsChanged[facetId] ?? 0) + 1;
      } else {
        warnings.push(
          `${itemId} ${facetId}: retry disagreed but lower confidence (${retryConf} vs ${currentConf}); keeping first-pass`,
        );
      }
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
    corrections: retry.corrections,
    warnings,
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
