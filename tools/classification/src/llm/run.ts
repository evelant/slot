import type { ItemExtractRecord } from "../extract/record.ts";
import type { LayerFile } from "../deterministic/run.ts";
import { FACETS } from "../schema/facets.ts";
import type { PackFacetVocabulary } from "../schema/vocabulary.ts";
import type { DocumentContextByItem } from "./document_context.ts";
import type { LlmClient, QueryOptions } from "./client.ts";
import {
  buildBatchPrompt,
  buildItemPayload,
  buildPromptFacetVocabulary,
  buildSplitPrompt,
  defaultTargetFacets,
  type LlmItemPayload,
  type PromptFacetVocabulary,
} from "./prompt.ts";
import {
  parseLlmResponse,
  type ParsedFacetEntry,
  type ParsedItemFacets,
  type SchemaProposal,
  type StageCorrection,
  type StageFillIn,
} from "./parse.ts";

export interface Stage3Options {
  /** Stage-1 records — the extractor output. */
  records: readonly ItemExtractRecord[];
  /** Stage-2 layer file — facets already assigned; stage 3 fills the gaps. */
  stage2Layer: LayerFile;
  /** LLM client (production or replay). */
  client: LlmClient;
  /** OpenRouter model id, passed to the client. */
  model?: string;
  /** Items per LLM call. Default 20 per plan §"Batching". */
  batchSize?: number;
  /** Restrict to these item ids; omit for all records. */
  only?: readonly string[];
  /** Override the facet set the LLM tries to populate. */
  targetFacets?: readonly string[];
  /** Optional guidebook/advancement context, keyed by item id. */
  documentContextByItem?: DocumentContextByItem;
  /** Max parallel in-flight batches. Default 1 (serial). */
  concurrency?: number;
  /** QueryOptions passthrough (timeout, abort signal, validator overrides). */
  clientOptions?: Partial<QueryOptions>;
  /** Progress callback — one event per completed batch. */
  onBatch?: (info: BatchProgress) => void;
  /** Accepted pack vocabulary for vocabulary-backed facets. */
  facetVocabulary?: PackFacetVocabulary;
  /**
   * Verbose-prompt extras. Defaults align with {@link LlmPromptInput.prompt_extras}:
   * disambiguation ON (principle-based, generalizes well), misconceptions
   * OFF (item-level enumeration, kept in reserve for regressions). Pass
   * either as `false` to flip off, or as `true` to force on.
   */
  promptExtras?: {
    verboseFacetDisambiguation?: boolean;
    verboseCommonMisconceptions?: boolean;
  };
}

export interface BatchProgress {
  batchIndex: number;
  batchCount: number;
  items: readonly string[];
  warnings: readonly string[];
  parsed: number;
  elapsedMs: number;
}

export interface Stage3Result {
  /** A fresh layer file (stage-2 entries + new stage-3 entries merged in). */
  layer: LayerFile;
  /** Items the LLM returned facets for. */
  filledItems: number;
  /** Count of facets added per facet id. */
  coverageAdded: Record<string, number>;
  /** Aggregated schema proposals for curator review. */
  proposals: SchemaProposal[];
  /** Aggregated stage-2 corrections the LLM suggested (NOT auto-merged). */
  corrections: StageCorrection[];
  /** Aggregated stage-2 fill-ins the LLM noticed (deterministic facets
   *  the rules failed to derive). NOT auto-merged — surfaced for human
   *  review of the stage-2 rule files. */
  fillIns: StageFillIn[];
  /** Warnings collected across all batches. */
  warnings: string[];
  /** Batch responses that did not exactly cover the requested item ids. */
  responseMismatches: BatchResponseMismatch[];
}

export interface BatchResponseMismatch {
  batchIndex: number;
  requested: readonly string[];
  parsed: readonly string[];
  missing: readonly string[];
  extra: readonly string[];
}

// Production default: deepseek-v4-flash via OpenRouter pinned to the
// deepseek provider. Locked in 2026-04-26 after the 60-item playtest sample
// showed stable coverage, no batch dropping, and low per-pack cost.
const DEFAULT_MODEL = "deepseek/deepseek-v4-flash";
const DEFAULT_BATCH_SIZE = 20;
/**
 * Per-mod batch concurrency default. OpenRouter handles 4 in-flight
 * calls comfortably and most mods finish in under 5 minutes at this
 * level. Bump on the cli with `--concurrency N` for fast modes; the
 * upper limit is governed by upstream provider rate limits, not the
 * client.
 */
const DEFAULT_BATCH_CONCURRENCY = 4;

/**
 * Drive stage 3 end-to-end: slice the record list into batches, call the LLM
 * once per batch, parse each response, and return a fresh layer file with the
 * merged facets. Stage-2 entries are preserved — we never overwrite them.
 */
export async function runStage3(options: Stage3Options): Promise<Stage3Result> {
  const model = options.model ?? DEFAULT_MODEL;
  const batchSize = options.batchSize ?? DEFAULT_BATCH_SIZE;
  const targetFacets = options.targetFacets ?? defaultTargetFacets();
  const facetVocabulary = buildPromptFacetVocabulary(options.facetVocabulary, targetFacets);
  const recordIndex = new Map(options.records.map((r) => [r.id, r]));

  const selected = options.only
    ? options.only
        .map((id) => recordIndex.get(id))
        .filter((r): r is ItemExtractRecord => !!r)
    : options.records;

  const batches = chunk(selected, batchSize);
  const concurrency = Math.max(1, options.concurrency ?? DEFAULT_BATCH_CONCURRENCY);
  const warnings: string[] = [];
  const proposals: SchemaProposal[] = [];
  const corrections: StageCorrection[] = [];
  const fillIns: StageFillIn[] = [];
  const coverageAdded: Record<string, number> = {};
  const responseMismatches: BatchResponseMismatch[] = [];

  // Deep-clone only what we'll mutate; the entries map itself is new but
  // entries that were already present are referenced, not copied.
  const mergedEntries: LayerFile["entries"] = { ...options.stage2Layer.entries };
  let filledItems = 0;

  /**
   * Process a single batch end-to-end: build the prompt, call the LLM,
   * parse the response, merge into the shared layer state. Mutations to
   * `mergedEntries`, `warnings`, etc. happen synchronously after each
   * `await`, so JS's microtask scheduling guarantees no concurrent writes
   * even with multiple in-flight workers — only one worker is executing
   * JS code at any instant.
   */
  const processBatch = async (batch: ItemExtractRecord[], i: number): Promise<void> => {
    const start = Date.now();

    const payloads: LlmItemPayload[] = batch.map((record) => {
      const stage2 = options.stage2Layer.entries[record.id]?.facets ?? {};
      return buildItemPayload(record, stage2, options.documentContextByItem?.[record.id]);
    });

    // Prefer split prompt when the client supports it: stable system content
    // (preamble + schema + disambiguation) stays separate from per-batch item
    // data. This improves provider-side cache reuse and keeps the
    // classification contract out of volatile batch payloads.
    //
    // Validator: an upstream truncation can produce a non-empty but
    // unparseable response (we hit this on 3 batches of the create
    // modpack run, losing 60 items). When the client supports it
    // (currently only OpenRouterClient), we hand it a `parseLlmResponse`
    // shim so it can re-ask while the prompt cache is still warm. Vocabulary
    // misses are handled after parsing so a mostly useful response is not
    // retried wholesale just because one closed-set value needs to be dropped.
    const queryOptions: Partial<QueryOptions> & { model: string } = {
      model,
      ...options.clientOptions,
      responseValidator: (text) => {
        try {
          const parsed = parseLlmResponse(text);
          const mismatch = batchResponseMismatch(batch, parsed.items);
          if (mismatch && mismatch.missing.length > 0) {
            return {
              ok: false,
              reason: responseMismatchReason(mismatch),
            };
          }
          return { ok: true };
        } catch (err) {
          return { ok: false, reason: (err as Error).message.slice(0, 120) };
        }
      },
    };
    let responseText: string;
    const promptInput = {
      items: payloads,
      target_facets: targetFacets,
      facet_vocabulary: facetVocabulary,
      prompt_extras: options.promptExtras
        ? {
            verbose_facet_disambiguation: options.promptExtras.verboseFacetDisambiguation,
            verbose_common_misconceptions: options.promptExtras.verboseCommonMisconceptions,
          }
        : undefined,
    };
    if (options.client.querySplit) {
      const { system, user } = buildSplitPrompt(promptInput);
      responseText = await options.client.querySplit(system, user, queryOptions);
    } else {
      const prompt = buildBatchPrompt(promptInput);
      responseText = await options.client.query(prompt, queryOptions);
    }

    let parsed;
    try {
      parsed = parseLlmResponse(responseText);
    } catch (err) {
      warnings.push(`batch ${i + 1}: parse failed: ${(err as Error).message.slice(0, 200)}`);
      options.onBatch?.({
        batchIndex: i,
        batchCount: batches.length,
        items: batch.map((r) => r.id),
        warnings: [`parse failure`],
        parsed: 0,
        elapsedMs: Date.now() - start,
      });
      return;
    }
    const vocabularyIssues = dropInvalidVocabularyValues(parsed.items, facetVocabulary);
    if (vocabularyIssues.length > 0) {
      parsed.warnings.push(`batch ${i + 1}: ${vocabularyMismatchReason(vocabularyIssues)}; dropped invalid value(s)`);
    }
    warnings.push(...parsed.warnings);
    proposals.push(...parsed.proposals);
    corrections.push(...parsed.corrections);
    fillIns.push(...parsed.fillIns);

    const mismatch = batchResponseMismatch(batch, parsed.items);
    if (mismatch) {
      responseMismatches.push({ batchIndex: i, ...mismatch });
      warnings.push(`batch ${i + 1}: ${responseMismatchReason(mismatch)}`);
    }
    const batchIds = new Set(batch.map((record) => record.id));

    for (const [itemId, itemFacets] of parsed.items) {
      if (!batchIds.has(itemId)) {
        warnings.push(`${itemId}: not in batch ${i + 1}, ignoring`);
        continue;
      }
      if (!recordIndex.has(itemId)) {
        warnings.push(`${itemId}: not in the extract set, ignoring`);
        continue;
      }
      const existing = mergedEntries[itemId]?.facets ?? {};
      const next: LayerFile["entries"][string]["facets"] = { ...existing };

      let itemAdded = false;
      for (const [facetId, entry] of Object.entries(itemFacets.facets)) {
        if (existing[facetId]) {
          if (valuesDisagree(existing[facetId], entry)) {
            warnings.push(`${itemId} ${facetId}: stage 2 asserted ${describeEntry(existing[facetId])}; LLM value ${describeEntry(toLayerEntry(entry))} dropped — add to corrections if clearly wrong`);
          }
          continue;
        }
        next[facetId] = toLayerEntry(entry);
        coverageAdded[facetId] = (coverageAdded[facetId] ?? 0) + 1;
        itemAdded = true;
      }
      mergedEntries[itemId] = { facets: next };
      if (itemAdded) filledItems++;
    }

    // Merge fill-ins: the LLM put these in `fill_ins` instead of
    // `facets` because they're deterministic facets stage-2 missed.
    // The data still needs to reach the layer — fill_ins are tracked
    // separately for audit (stage-2 rule gaps), but the runtime
    // doesn't care which channel produced them.
    for (const fill of parsed.fillIns) {
      if (!recordIndex.has(fill.item)) {
        warnings.push(`${fill.item}: fill_in for item not in the extract set, ignoring`);
        continue;
      }
      const existing = mergedEntries[fill.item]?.facets ?? {};
      if (existing[fill.facet]) {
        warnings.push(
          `${fill.item} ${fill.facet}: fill_in proposed but stage-2 already has a value (${describeEntry(existing[fill.facet])}); ignoring`,
        );
        continue;
      }
      const next: LayerFile["entries"][string]["facets"] = { ...existing };
      next[fill.facet] = {
        value: fill.value,
        confidence: 0.85,
        source: "llm:stage3-fill-in",
        rationale: fill.rationale,
      } as unknown as LayerFile["entries"][string]["facets"][string];
      mergedEntries[fill.item] = { facets: next };
      coverageAdded[fill.facet] = (coverageAdded[fill.facet] ?? 0) + 1;
    }

    options.onBatch?.({
      batchIndex: i,
      batchCount: batches.length,
      items: batch.map((r) => r.id),
      warnings: parsed.warnings,
      parsed: parsed.items.size,
      elapsedMs: Date.now() - start,
    });
  };

  // Worker pool: each worker pulls the next batch index until exhausted.
  // Concurrency=1 reproduces the original serial loop exactly.
  let nextBatchIndex = 0;
  const worker = async (): Promise<void> => {
    while (true) {
      const idx = nextBatchIndex++;
      if (idx >= batches.length) return;
      await processBatch(batches[idx]!, idx);
    }
  };
  await Promise.all(
    Array.from({ length: Math.min(concurrency, batches.length) }, () => worker()),
  );

  const layer: LayerFile = {
    ...options.stage2Layer,
    entries: mergedEntries,
    generated_by: options.stage2Layer.generated_by,
    generated_at: new Date().toISOString(),
  };

  return { layer, filledItems, coverageAdded, proposals, corrections, fillIns, warnings, responseMismatches };
}

function batchResponseMismatch(
  batch: readonly ItemExtractRecord[],
  parsedItems: ReadonlyMap<string, unknown>,
): Omit<BatchResponseMismatch, "batchIndex"> | null {
  const requested = batch.map((record) => record.id);
  const requestedSet = new Set(requested);
  const parsed = [...parsedItems.keys()];
  const parsedSet = new Set(parsed);
  const missing = requested.filter((id) => !parsedSet.has(id));
  const extra = parsed.filter((id) => !requestedSet.has(id));
  return missing.length > 0 || extra.length > 0
    ? { requested, parsed, missing, extra }
    : null;
}

function responseMismatchReason(mismatch: Omit<BatchResponseMismatch, "batchIndex">): string {
  const parts: string[] = [];
  if (mismatch.missing.length > 0) {
    parts.push(`missing ${mismatch.missing.length}/${mismatch.requested.length} requested item(s)`);
  }
  if (mismatch.extra.length > 0) {
    parts.push(`included ${mismatch.extra.length} item(s) outside the batch`);
  }
  return `response coverage mismatch: ${parts.join("; ")}`;
}


/** Compact string summary of a layer entry for warning text. */
function describeEntry(entry: unknown): string {
  if (!entry || typeof entry !== "object") return String(entry);
  const e = entry as Record<string, unknown>;
  if ("value" in e) return JSON.stringify(e.value);
  if (Array.isArray(e.values)) return JSON.stringify(e.values);
  return JSON.stringify(e);
}

/**
 * True when the LLM's entry has at least one concrete value not already in
 * the stage-2 entry. For single-value facets, "disagree" means a different
 * value. For multi-value facets, "disagree" means the LLM is trying to add
 * a value we don't have (i.e. LLM's values aren't a subset of existing).
 * A same-value or subset re-emission is harmless and not a warning.
 */
function valuesDisagree(
  existing: unknown,
  llm: ParsedFacetEntry,
): boolean {
  if (!existing || typeof existing !== "object") return true;
  const e = existing as Record<string, unknown>;
  if (llm.kind === "single") {
    return JSON.stringify(e.value) !== JSON.stringify(llm.value);
  }
  if (llm.kind === "ambiguous") {
    // ambiguous is always meaningful — let it through as a disagreement.
    return true;
  }
  // multi
  const existingVals = Array.isArray(e.values) ? (e.values as unknown[]) : [];
  const existingSet = new Set(existingVals.map((v) => JSON.stringify(v)));
  return llm.values.some((v) => !existingSet.has(JSON.stringify(v)));
}

function dropInvalidVocabularyValues(
  items: Map<string, ParsedItemFacets>,
  vocabulary: PromptFacetVocabulary | undefined,
): string[] {
  return enforcePromptVocabulary(items, vocabulary, true);
}

function enforcePromptVocabulary(
  items: Map<string, ParsedItemFacets>,
  vocabulary: PromptFacetVocabulary | undefined,
  mutate: boolean,
): string[] {
  if (!vocabulary) return [];
  const allowedByFacet = new Map(
    Object.entries(vocabulary).map(([facetId, values]) => [
      facetId,
      new Set(values.map((value) => value.id)),
    ]),
  );
  const issues: string[] = [];

  for (const [itemId, item] of items) {
    for (const [facetId, entry] of Object.entries(item.facets)) {
      if (!FACETS[facetId]?.vocabulary_backed) continue;
      const allowed = allowedByFacet.get(facetId);
      const validString = (value: string | number | boolean | null | undefined): value is string =>
        typeof value === "string" && !!allowed?.has(value);
      const values = parsedFacetValues(entry);
      const invalid = values.filter((value) => !validString(value));
      if (invalid.length === 0) continue;

      for (const value of invalid) {
        issues.push(`${itemId} ${facetId}=${JSON.stringify(value)}`);
      }
      if (!mutate) continue;

      if (entry.kind === "multi") {
        const kept = entry.values.filter(validString);
        if (kept.length > 0) {
          entry.values = kept;
        } else {
          delete item.facets[facetId];
        }
        continue;
      }
      if (entry.kind === "ambiguous") {
        const kept = entry.values.filter(validString);
        if (kept.length === 2) {
          entry.values = [kept[0]!, kept[1]!];
        } else {
          delete item.facets[facetId];
        }
        continue;
      }
      delete item.facets[facetId];
    }
  }

  return issues;
}

function parsedFacetValues(entry: ParsedFacetEntry): (string | number | boolean | null | undefined)[] {
  if (entry.kind === "single") return [entry.value];
  return [...entry.values];
}

function vocabularyMismatchReason(issues: readonly string[]): string {
  const shown = issues.slice(0, 5).join("; ");
  const suffix = issues.length > 5 ? `; +${issues.length - 5} more` : "";
  return `vocabulary-backed facet value not accepted by prompt vocabulary: ${shown}${suffix}`;
}

function chunk<T>(arr: readonly T[], size: number): T[][] {
  const out: T[][] = [];
  for (let i = 0; i < arr.length; i += size) out.push(arr.slice(i, i + size));
  return out;
}

function toLayerEntry(
  entry: ParsedFacetEntry,
): LayerFile["entries"][string]["facets"][string] {
  const confidence = entry.confidence;
  const rationale = entry.rationale;
  if (entry.kind === "single") {
    return {
      value: entry.value,
      ...(confidence !== undefined ? { confidence } : {}),
      source: "llm:stage3",
      ...(rationale ? { rationale } : {}),
    };
  }
  if (entry.kind === "ambiguous") {
    return {
      values: [...entry.values],
      ambiguous: true,
      ...(confidence !== undefined ? { confidence } : {}),
      source: "llm:stage3",
      ...(rationale ? { rationale } : {}),
    };
  }
  // multi
  return {
    values: [...entry.values].sort((a, b) => String(a).localeCompare(String(b))),
    mode: "add",
    ...(confidence !== undefined ? { confidence } : {}),
    source: "llm:stage3",
    ...(rationale ? { rationale } : {}),
  };
}
