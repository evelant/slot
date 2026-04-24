import type { ItemExtractRecord } from "../extract/record.ts";
import type { LayerFile } from "../deterministic/run.ts";
import type { LlmClient, QueryOptions } from "./client.ts";
import {
  buildBatchPrompt,
  buildItemPayload,
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

const DEFAULT_MODEL = "claude-haiku-4-5";
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
  const warnings: string[] = [];
  const proposals: SchemaProposal[] = [];
  const corrections: StageCorrection[] = [];
  const coverageAdded: Record<string, number> = {};

  // Deep-clone only what we'll mutate; the entries map itself is new but
  // entries that were already present are referenced, not copied.
  const mergedEntries: LayerFile["entries"] = { ...options.stage2Layer.entries };
  let filledItems = 0;

  for (let i = 0; i < batches.length; i++) {
    const batch = batches[i]!;
    const start = Date.now();

    const payloads: LlmItemPayload[] = batch.map((record) => {
      const stage2 = options.stage2Layer.entries[record.id]?.facets ?? {};
      return buildItemPayload(record, stage2);
    });

    const prompt = buildBatchPrompt({ items: payloads, target_facets: targetFacets });
    const responseText = await options.client.query(prompt, {
      model,
      ...options.clientOptions,
    });
    const parsed = parseLlmResponse(responseText);
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
          // stage 2 already spoke — stage 3 must not clobber deterministic facets
          warnings.push(`${itemId} ${facetId}: stage 2 already asserted; LLM value dropped`);
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
  }

  const layer: LayerFile = {
    ...options.stage2Layer,
    entries: mergedEntries,
    generated_by: options.stage2Layer.generated_by,
    generated_at: new Date().toISOString(),
  };

  return { layer, filledItems, coverageAdded, proposals, corrections, warnings };
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
