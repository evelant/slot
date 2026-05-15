import {
  FACETS,
  validateMultiValue,
  validateSingleValue,
} from "../schema/facets.ts";

/**
 * Parsed output from a single LLM batch. Callers merge `items` into the layer
 * file and aggregate proposals / legacy review channels for curator review.
 */
export interface ParsedLlmResponse {
  items: Map<string, ParsedItemFacets>;
  proposals: SchemaProposal[];
  /** Missing usable-vocabulary values the model used or wanted for a
   * vocabulary-backed facet. The classification layer keeps those facet values
   * and marks them with `vocab_review`; this side channel is only an aggregate
   * report/review aid. */
  vocabularyProposals: VocabularyProposal[];
  /** Legacy correction channel. The current prompt does not ask for these. */
  corrections: StageCorrection[];
  /** Legacy fill-in channel. The current prompt does not ask for these. */
  fillIns: StageFillIn[];
  /** Non-fatal issues (unknown facet, value out of enum, etc.) — the
   *  affected entries are dropped but the rest of the response is kept. */
  warnings: string[];
}

export interface StageCorrection {
  item: string;
  facet: string;
  current?: string | number | boolean | null;
  suggested?: string | number | boolean | null;
  rationale: string;
}

/**
 * Legacy channel for exact/reference facets noticed outside the main `facets`
 * object. Kept so old fixtures remain parseable.
 */
export interface StageFillIn {
  item: string;
  facet: string;
  value: string | number | boolean | null;
  rationale: string;
}

export interface ParsedItemFacets {
  facets: Record<string, ParsedFacetEntry>;
}

export type ParsedFacetEntry =
  | {
      kind: "single";
      value: string | number | boolean | null;
      rationale?: string;
      ambiguous?: false;
      evidence?: string;
      vocabReview?: true;
    }
  | {
      kind: "ambiguous";
      values: [string | number, string | number];
      rationale?: string;
      ambiguous: true;
      evidence?: string;
      vocabReview?: true;
    }
  | {
      kind: "multi";
      values: (string | number)[];
      rationale?: string;
      evidence?: string;
      vocabReview?: true;
    };

export interface SchemaProposal {
  kind: "add_value" | "add_facet";
  facet?: string;
  name?: string;
  value?: string;
  suggested_kind?: string;
  rationale: string;
  example_items?: string[];
}

export interface VocabularyProposal {
  item: string;
  facet: string;
  label: string;
  proposed_id?: string;
  rationale: string;
  evidence?: string[];
}

/**
 * Some legacy fixtures wrap the model's response in an envelope:
 *   { "type": "result", "result": "<text>", ... }
 * We extract the `result` string, strip any code fences, then JSON.parse the inner.
 * If the raw input is itself a JSON object matching our expected shape, we
 * accept it directly (replay fixtures are usually raw JSON).
 */
export function parseLlmResponse(raw: string): ParsedLlmResponse {
  const warnings: string[] = [];
  const text = unwrapEnvelope(raw, warnings);
  const stripped = stripCodeFences(text);

  let parsed: unknown;
  try {
    parsed = JSON.parse(stripped);
  } catch (err) {
    throw new Error(`LLM response was not valid JSON: ${(err as Error).message}\n---\n${stripped.slice(0, 400)}`);
  }

  const rootItems = (parsed as { items?: unknown }).items;
  const rootProposals = (parsed as { schema_proposals?: unknown }).schema_proposals;
  const rootVocabularyProposals = (parsed as { vocabulary_proposals?: unknown }).vocabulary_proposals;
  const rootCorrections = (parsed as { corrections?: unknown }).corrections;
  const rootFillIns = (parsed as { fill_ins?: unknown }).fill_ins;

  const items = new Map<string, ParsedItemFacets>();
  if (rootItems && typeof rootItems === "object") {
    for (const [itemId, entry] of Object.entries(rootItems)) {
      const parsedFacets = parseItemEntry(itemId, entry, warnings);
      if (parsedFacets) items.set(itemId, parsedFacets);
    }
  }

  const proposals: SchemaProposal[] = [];
  if (Array.isArray(rootProposals)) {
    for (const prop of rootProposals) {
      if (prop && typeof prop === "object") proposals.push(prop as SchemaProposal);
    }
  }

  const vocabularyProposals: VocabularyProposal[] = [];
  if (Array.isArray(rootVocabularyProposals)) {
    for (const raw of rootVocabularyProposals) {
      if (!raw || typeof raw !== "object") continue;
      const proposal = raw as Record<string, unknown>;
      if (
        typeof proposal.item !== "string" ||
        typeof proposal.facet !== "string" ||
        typeof proposal.label !== "string" ||
        typeof proposal.rationale !== "string"
      ) {
        warnings.push(`vocabulary_proposal missing required field(s): ${JSON.stringify(raw).slice(0, 120)}`);
        continue;
      }
      const def = FACETS[proposal.facet];
      if (!def) {
        warnings.push(`vocabulary_proposal for ${proposal.item} ${proposal.facet}: unknown facet`);
        continue;
      }
      if (!def.vocabulary_backed) {
        warnings.push(`vocabulary_proposal for ${proposal.item} ${proposal.facet}: facet is not vocabulary-backed`);
        continue;
      }
      vocabularyProposals.push({
        item: proposal.item,
        facet: proposal.facet,
        label: proposal.label,
        ...(typeof proposal.proposed_id === "string" ? { proposed_id: proposal.proposed_id } : {}),
        rationale: proposal.rationale,
        ...(Array.isArray(proposal.evidence)
          ? { evidence: proposal.evidence.filter((value): value is string => typeof value === "string") }
          : {}),
      });
    }
  }

  const corrections: StageCorrection[] = [];
  if (Array.isArray(rootCorrections)) {
    for (const raw of rootCorrections) {
      if (!raw || typeof raw !== "object") continue;
      const c = raw as Record<string, unknown>;
      if (typeof c.item !== "string" || typeof c.facet !== "string" || typeof c.rationale !== "string") {
        warnings.push(`correction missing required field(s): ${JSON.stringify(raw).slice(0, 120)}`);
        continue;
      }
      corrections.push({
        item: c.item,
        facet: c.facet,
        current: c.current as StageCorrection["current"],
        suggested: c.suggested as StageCorrection["suggested"],
        rationale: c.rationale,
      });
    }
  }

  const fillIns: StageFillIn[] = [];
  if (Array.isArray(rootFillIns)) {
    for (const raw of rootFillIns) {
      if (!raw || typeof raw !== "object") continue;
      const f = raw as Record<string, unknown>;
      if (typeof f.item !== "string" || typeof f.facet !== "string") {
        warnings.push(`fill_in missing required field(s): ${JSON.stringify(raw).slice(0, 120)}`);
        continue;
      }
      const value = f.value;
      if (value === undefined) {
        warnings.push(`fill_in for ${f.item} ${f.facet} missing 'value'`);
        continue;
      }
      // Only non-vocabulary reference-style facets — llm-authored vocabulary
      // facets must be grounded by usable vocabulary, not smuggled through the
      // compatibility fill-in channel.
      const def = FACETS[f.facet];
      if (!def) {
        warnings.push(`fill_in for ${f.item} ${f.facet}: unknown facet`);
        continue;
      }
      if (def.vocabulary_backed) {
        warnings.push(`fill_in for ${f.item} ${f.facet}: facet is vocabulary-backed, emit in facets block with usable vocabulary or add vocabulary_proposals instead`);
        continue;
      }
      if (def.llm_authored && !def.deterministic) {
        warnings.push(`fill_in for ${f.item} ${f.facet}: facet is llm-authored, emit in facets block instead`);
        continue;
      }
      const issue = validateSingleValue(f.facet, value);
      if (issue) {
        warnings.push(`fill_in for ${f.item} ${f.facet}: ${issue.reason}; dropped`);
        continue;
      }
      fillIns.push({
        item: f.item,
        facet: f.facet,
        value: value as StageFillIn["value"],
        rationale: typeof f.rationale === "string" && f.rationale.trim().length > 0
          ? f.rationale
          : "LLM did not provide rationale.",
      });
    }
  }

  return { items, proposals, vocabularyProposals, corrections, fillIns, warnings };
}

function parseItemEntry(
  itemId: string,
  raw: unknown,
  warnings: string[],
): ParsedItemFacets | null {
  if (!raw || typeof raw !== "object") {
    warnings.push(`${itemId}: entry must be an object`);
    return null;
  }
  const facetsRaw = (raw as { facets?: unknown }).facets;
  if (!facetsRaw || typeof facetsRaw !== "object") {
    warnings.push(`${itemId}: missing 'facets' object`);
    return { facets: {} };
  }
  const out: Record<string, ParsedFacetEntry> = {};
  for (const [facetId, entryRaw] of Object.entries(facetsRaw)) {
    const def = FACETS[facetId];
    if (!def) {
      warnings.push(`${itemId}: unknown facet '${facetId}' — dropped (emit schema_proposals instead)`);
      continue;
    }
    const entry = parseFacetEntry(itemId, facetId, entryRaw, warnings);
    if (entry) out[facetId] = entry;
  }
  return { facets: out };
}

function parseFacetEntry(
  itemId: string,
  facetId: string,
  raw: unknown,
  warnings: string[],
): ParsedFacetEntry | null {
  const def = FACETS[facetId]!;
  const isMulti =
    def.kind === "multi_enum" ||
    def.kind === "multi_free_text" ||
    def.kind === "multi_item_ref";

  if (isMulti && Array.isArray(raw)) {
    const issue = validateMultiValue(facetId, raw);
    if (issue) {
      warnings.push(`${itemId} ${facetId}: ${issue.reason}`);
      return null;
    }
    return {
      kind: "multi",
      values: raw as (string | number)[],
    };
  }

  if (!raw || typeof raw !== "object") {
    warnings.push(`${itemId} ${facetId}: entry must be an object`);
    return null;
  }
  const e = raw as Record<string, unknown>;
  const rationale = typeof e.rationale === "string" ? e.rationale : undefined;

  const evidence = typeof e.evidence === "string" ? e.evidence.trim() : "";
  const richRationale = formatRationale(rationale, evidence);

  // Ambiguous two-value shape.
  if (e.ambiguous === true) {
    if (def.kind !== "enum" && def.kind !== "free_text") {
      warnings.push(`${itemId} ${facetId}: ambiguous only valid on enum/free_text (is ${def.kind})`);
      return null;
    }
    const values = e.values;
    if (
      !Array.isArray(values) ||
      values.length !== 2 ||
      !values.every((v) => typeof v === "string" || typeof v === "number")
    ) {
      warnings.push(`${itemId} ${facetId}: ambiguous requires exactly 2 scalar values`);
      return null;
    }
    for (const v of values) {
      const issue = validateSingleValue(facetId, v);
      if (issue) {
        warnings.push(`${itemId} ${facetId}: ${issue.reason}`);
        return null;
      }
    }
    return {
      kind: "ambiguous",
      values: values as [string | number, string | number],
      ambiguous: true,
      rationale: richRationale,
      evidence: evidence || undefined,
    };
  }

  if (isMulti) {
    // Lenient: accept `value: <scalar>` as a single-element array for
    // multi facets. Haiku in particular drifts back to single-value shape
    // when only one value applies; wrapping it rather than dropping lets
    // the entry through. We still warn quietly in case we want to debug.
    let values = e.values;
    if (!Array.isArray(values) && "value" in e) {
      const single = (e as { value: unknown }).value;
      if (typeof single === "string" || typeof single === "number") {
        values = [single];
        warnings.push(`${itemId} ${facetId}: emitted single 'value' for multi facet; wrapped as [value] (informational)`);
      }
    }
    if (!Array.isArray(values)) {
      warnings.push(`${itemId} ${facetId}: multi-value facet requires 'values' array`);
      return null;
    }
    const issue = validateMultiValue(facetId, values);
    if (issue) {
      warnings.push(`${itemId} ${facetId}: ${issue.reason}`);
      return null;
    }
    return {
      kind: "multi",
      values: values as (string | number)[],
      rationale: richRationale,
      evidence: evidence || undefined,
    };
  }

  // single-value (non-ambiguous)
  if (!("value" in e)) {
    warnings.push(`${itemId} ${facetId}: single-value facet requires 'value'`);
    return null;
  }
  const value = e.value;
  const issue = validateSingleValue(facetId, value);
  if (issue) {
    warnings.push(`${itemId} ${facetId}: ${issue.reason}`);
    return null;
  }
  return {
    kind: "single",
    value: value as string | number | boolean | null,
    rationale: richRationale,
    evidence: evidence || undefined,
  };
}

/** Fold the LLM-emitted rationale + evidence into a single readable string for
 * the layer file. */
function formatRationale(
  rationale: string | undefined,
  evidence: string,
): string | undefined {
  const parts: string[] = [];
  if (evidence) parts.push(evidence);
  if (rationale && rationale !== evidence) parts.push(rationale);
  if (parts.length === 0) return undefined;
  return parts.join(" — ");
}

function unwrapEnvelope(raw: string, warnings: string[]): string {
  const trimmed = raw.trim();
  if (!trimmed.startsWith("{")) return trimmed;
  try {
    const obj = JSON.parse(trimmed);
    if (
      obj &&
      typeof obj === "object" &&
      typeof (obj as { result?: unknown }).result === "string"
    ) {
      const type = (obj as { type?: unknown }).type;
      if (type && type !== "result") {
        warnings.push(`LLM envelope type='${type}' (expected 'result'); attempting to use 'result' field anyway`);
      }
      return (obj as { result: string }).result;
    }
  } catch {
    // not a JSON envelope; caller will re-parse the raw text
  }
  return trimmed;
}

/**
 * Extract the classification JSON from a model response. Accepts, in order:
 *   1. The entire text trimmed (when the model respected "JSON only").
 *   2. The first ```json ... ``` block anywhere in the text.
 *   3. The first top-level brace-balanced `{ ... }` region.
 *
 * Sonnet 4.6 occasionally prepends a narration ("Continuing with remaining
 * items…") before emitting the JSON despite the "JSON only" rule, so falling
 * through to substring extraction is necessary for robustness.
 */
function stripCodeFences(text: string): string {
  const trimmed = text.trim();

  // Whole string is a fenced block
  const whole = trimmed.match(/^```(?:json)?\n([\s\S]*?)\n```$/);
  if (whole) return whole[1]!;

  // Any ```json…``` block inside the text
  const anyFence = trimmed.match(/```(?:json)?\s*\n([\s\S]*?)\n```/);
  if (anyFence) return anyFence[1]!;

  // First top-level brace-balanced object (string-aware)
  const obj = firstJsonObject(trimmed);
  if (obj) return obj;

  return trimmed;
}

function firstJsonObject(text: string): string | null {
  const start = text.indexOf("{");
  if (start < 0) return null;
  let depth = 0;
  let inString = false;
  let escape = false;
  for (let i = start; i < text.length; i++) {
    const ch = text[i]!;
    if (inString) {
      if (escape) {
        escape = false;
      } else if (ch === "\\") {
        escape = true;
      } else if (ch === '"') {
        inString = false;
      }
      continue;
    }
    if (ch === '"') {
      inString = true;
      continue;
    }
    if (ch === "{") depth++;
    else if (ch === "}") {
      depth--;
      if (depth === 0) return text.slice(start, i + 1);
    }
  }
  return null;
}
