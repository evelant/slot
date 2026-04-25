import type { ItemExtractRecord } from "../extract/record.ts";
import type { LayerFile } from "../deterministic/run.ts";
import type { LlmClient, QueryOptions } from "./client.ts";
import {
  buildBatchPrompt,
  buildItemPayload,
  buildSplitPrompt,
  defaultTargetFacets,
  type LlmItemPayload,
} from "./prompt.ts";
import {
  parseLlmResponse,
  type ParsedFacetEntry,
  type SchemaProposal,
  type StageCorrection,
} from "./parse.ts";

export interface Stage3Options {
  /** Stage-1 records — the extractor output. */
  records: readonly ItemExtractRecord[];
  /** Stage-2 layer file — facets already assigned; stage 3 fills the gaps. */
  stage2Layer: LayerFile;
  /** LLM client (production or replay). */
  client: LlmClient;
  /** Model id, passed to the client. Default haiku-4-5. */
  model?: string;
  /** Items per LLM call. Default 20 per plan §"Batching". */
  batchSize?: number;
  /** Restrict to these item ids; omit for all records. */
  only?: readonly string[];
  /** Override the facet set the LLM tries to populate. */
  targetFacets?: readonly string[];
  /** Max parallel in-flight batches. Default 1 (serial). */
  concurrency?: number;
  /** QueryOptions passthrough (binary path, timeout). */
  clientOptions?: Partial<QueryOptions>;
  /** Progress callback — one event per completed batch. */
  onBatch?: (info: BatchProgress) => void;
  /**
   * Pre-proposed canonical vocabulary for `mod_subsystem`. Forwarded into the
   * system prompt so the LLM picks consistent labels across items in this run.
   * Omit for vanilla / mods with no meaningful subsystem groupings.
   */
  subsystemVocabulary?: readonly { id: string; rationale?: string }[];
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
  /** Warnings collected across all batches. */
  warnings: string[];
}

const DEFAULT_MODEL = "haiku";
const DEFAULT_BATCH_SIZE = 20;

/**
 * Drive stage 3 end-to-end: slice the record list into batches, call the LLM
 * once per batch, parse each response, and return a fresh layer file with the
 * merged facets. Stage-2 entries are preserved — we never overwrite them.
 */
export async function runStage3(options: Stage3Options): Promise<Stage3Result> {
  const model = options.model ?? DEFAULT_MODEL;
  const batchSize = options.batchSize ?? DEFAULT_BATCH_SIZE;
  const targetFacets = options.targetFacets ?? defaultTargetFacets();
  const recordIndex = new Map(options.records.map((r) => [r.id, r]));

  const selected = options.only
    ? options.only
        .map((id) => recordIndex.get(id))
        .filter((r): r is ItemExtractRecord => !!r)
    : options.records;

  const batches = chunk(selected, batchSize);
  const concurrency = Math.max(1, options.concurrency ?? 1);
  const warnings: string[] = [];
  const proposals: SchemaProposal[] = [];
  const corrections: StageCorrection[] = [];
  const coverageAdded: Record<string, number> = {};

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
      return buildItemPayload(record, stage2);
    });

    // Prefer split prompt when the client supports it — sends the stable
    // system content (preamble + schema + disambiguation) via
    // `claude --system-prompt` and the per-batch item data on stdin. This
    // maximizes prompt-cache hit rate and keeps Claude Code's default
    // system prompt from interfering with classification context.
    const queryOptions = { model, ...options.clientOptions };
    let responseText: string;
    const promptInput = {
      items: payloads,
      target_facets: targetFacets,
      subsystem_vocabulary: options.subsystemVocabulary,
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
    warnings.push(...parsed.warnings);
    proposals.push(...parsed.proposals);
    corrections.push(...parsed.corrections);

    for (const [itemId, itemFacets] of parsed.items) {
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

  return { layer, filledItems, coverageAdded, proposals, corrections, warnings };
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
