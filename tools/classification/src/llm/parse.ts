import {
  FACETS,
  validateMultiValue,
  validateSingleValue,
} from "../schema/facets.ts";

/**
 * Parsed output from a single LLM batch. Callers merge `items` into the layer
 * file and aggregate `proposals` and `corrections` for curator review.
 */
export interface ParsedLlmResponse {
  items: Map<string, ParsedItemFacets>;
  proposals: SchemaProposal[];
  /** Flags from the LLM that a stage-2 facet looks wrong. These are never
   *  merged into the layer automatically — they surface for human review. */
  corrections: StageCorrection[];
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
  confidence?: number;
}

export interface ParsedItemFacets {
  facets: Record<string, ParsedFacetEntry>;
}

/**
 * Strength tag the LLM emits for each facet value. Drives the final confidence
 * via SIGNAL_TO_CONFIDENCE so we don't have to trust the model's self-reported
 * calibration.
 */
export type Signal = "named" | "pattern" | "inferred" | "guess";

/** Map signal strength → confidence floor. The LLM may emit a `confidence`
 *  to nudge DOWN within the band, but values above the signal's mapped
 *  confidence are silently capped — overconfidence on a guess is demoted. */
export const SIGNAL_TO_CONFIDENCE: Record<Signal, number> = {
  named: 0.95,
  pattern: 0.80,
  inferred: 0.60,
  guess: 0.30,
};

export type ParsedFacetEntry =
  | {
      kind: "single";
      value: string | number | boolean | null;
      confidence?: number;
      rationale?: string;
      ambiguous?: false;
      signal?: Signal;
      evidence?: string;
    }
  | {
      kind: "ambiguous";
      values: [string | number, string | number];
      confidence?: number;
      rationale?: string;
      ambiguous: true;
      signal?: Signal;
      evidence?: string;
    }
  | {
      kind: "multi";
      values: (string | number)[];
      confidence?: number;
      rationale?: string;
      signal?: Signal;
      evidence?: string;
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

/**
 * claude -p --output-format json wraps the model's response in an envelope:
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
  const rootCorrections = (parsed as { corrections?: unknown }).corrections;

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

  const corrections: StageCorrection[] = [];
  if (Array.isArray(rootCorrections)) {
    for (const raw of rootCorrections) {
      if (!raw || typeof raw !== "object") continue;
      const c = raw as Record<string, unknown>;
      if (typeof c.item !== "string" || typeof c.facet !== "string" || typeof c.rationale !== "string") {
        warnings.push(`correction missing required field(s): ${JSON.stringify(raw).slice(0, 120)}`);
        continue;
      }
      const confidence = typeof c.confidence === "number" ? c.confidence : undefined;
      if (confidence !== undefined && confidence < 0.7) {
        warnings.push(`correction for ${c.item} ${c.facet} below confidence threshold (${confidence}); dropped`);
        continue;
      }
      corrections.push({
        item: c.item,
        facet: c.facet,
        current: c.current as StageCorrection["current"],
        suggested: c.suggested as StageCorrection["suggested"],
        rationale: c.rationale,
        confidence,
      });
    }
  }

  return { items, proposals, corrections, warnings };
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
  if (!raw || typeof raw !== "object") {
    warnings.push(`${itemId} ${facetId}: entry must be an object`);
    return null;
  }
  const e = raw as Record<string, unknown>;
  const def = FACETS[facetId]!;
  const isMulti =
    def.kind === "multi_enum" ||
    def.kind === "multi_free_text" ||
    def.kind === "multi_item_ref";
  const rationale = typeof e.rationale === "string" ? e.rationale : undefined;

  // Signal + evidence drive the final confidence. The model's self-reported
  // confidence is treated as a CAP — if the model says 0.9 but signal=guess,
  // the actual confidence is min(0.9, signal_floor). Overconfidence on a
  // weak signal is silently demoted; underconfidence is preserved.
  const signal = parseSignal(e.signal);
  const evidence = typeof e.evidence === "string" ? e.evidence.trim() : "";

  if (signal && (signal === "named" || signal === "pattern" || signal === "inferred") && evidence.length === 0) {
    warnings.push(`${itemId} ${facetId}: signal=${signal} requires non-empty evidence; demoted to 'guess'`);
  }
  const effectiveSignal: Signal | undefined =
    signal && (signal === "named" || signal === "pattern" || signal === "inferred") && evidence.length === 0
      ? "guess"
      : signal;

  const modelConf = typeof e.confidence === "number" ? e.confidence : undefined;
  const confidence = computeConfidence(effectiveSignal, modelConf);
  const richRationale = formatRationale(rationale, evidence, effectiveSignal);

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
      confidence,
      rationale: richRationale,
      signal: effectiveSignal,
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
      confidence,
      rationale: richRationale,
      signal: effectiveSignal,
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
    confidence,
    rationale: richRationale,
    signal: effectiveSignal,
    evidence: evidence || undefined,
  };
}

/** Apply the signal floor as a confidence cap. Returns undefined if neither
 *  signal nor model confidence was provided. */
function computeConfidence(
  signal: Signal | undefined,
  modelConf: number | undefined,
): number | undefined {
  if (signal !== undefined) {
    const ceiling = SIGNAL_TO_CONFIDENCE[signal];
    if (modelConf !== undefined) return Math.min(modelConf, ceiling);
    return ceiling;
  }
  return modelConf;
}

/** Fold the LLM-emitted rationale + evidence + signal into a single readable
 *  string for the layer file. Wire format stays unchanged. */
function formatRationale(
  rationale: string | undefined,
  evidence: string,
  signal: Signal | undefined,
): string | undefined {
  const parts: string[] = [];
  if (signal) parts.push(`[${signal}]`);
  if (evidence) parts.push(evidence);
  if (rationale && rationale !== evidence) parts.push(rationale);
  if (parts.length === 0) return undefined;
  return parts.join(" — ");
}

function parseSignal(raw: unknown): Signal | undefined {
  if (typeof raw !== "string") return undefined;
  if (raw === "named" || raw === "pattern" || raw === "inferred" || raw === "guess") {
    return raw;
  }
  return undefined;
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
        warnings.push(`claude envelope type='${type}' (expected 'result'); attempting to use 'result' field anyway`);
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
