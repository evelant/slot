import type { LlmClient, QueryOptions } from "../llm/client.ts";
import type { CuratedValue, VocabularyFacetId } from "./types.ts";
import { VOCABULARY_CURATION_TIMEOUT_MS } from "./constants.ts";
import { evidenceRefs, firstJsonObject, isConfidence, isRecord, isVocabularyState, stringArray, unwrapEnvelope } from "./helpers.ts";

export function parseVocabularyCurationResponse(raw: string): CuratedValue[] {
  const parsed = parseVocabularyCurationJson(raw);
  if (!isRecord(parsed)) return [];
  const rawValues = Array.isArray(parsed.values)
    ? parsed.values
    : isRecord(parsed.facets)
      ? Object.values(parsed.facets).flatMap((facet) =>
        isRecord(facet) && Array.isArray(facet.values) ? facet.values : []
      )
      : [];
  return rawValues.map(parseCuratedValue).filter((value): value is CuratedValue => value !== null);
}

export function parseVocabularyCurationByFacetResponse(
  raw: string,
  facets: readonly VocabularyFacetId[],
): Map<VocabularyFacetId, CuratedValue[]> {
  const parsed = parseVocabularyCurationJson(raw);
  const out = new Map<VocabularyFacetId, CuratedValue[]>();
  if (!isRecord(parsed)) return out;
  const allowed = new Set(facets);
  if (isRecord(parsed.facets)) {
    for (const [facet, value] of Object.entries(parsed.facets)) {
      if (!allowed.has(facet as VocabularyFacetId) || !isRecord(value)) continue;
      const rawValues = Array.isArray(value.values) ? value.values : [];
      out.set(
        facet as VocabularyFacetId,
        rawValues.map(parseCuratedValue).filter((entry): entry is CuratedValue => entry !== null),
      );
    }
    return out;
  }
  if (facets.length === 1 && Array.isArray(parsed.values)) {
    out.set(
      facets[0]!,
      parsed.values.map(parseCuratedValue).filter((entry): entry is CuratedValue => entry !== null),
    );
  }
  return out;
}

function parseVocabularyCurationJson(raw: string): unknown {
  const inner = unwrapEnvelope(raw);
  const json = firstJsonObject(inner);
  if (!json) return undefined;
  try {
    return JSON.parse(json);
  } catch {
    return undefined;
  }
}

function parseCuratedValue(rawValue: unknown): CuratedValue | null {
  if (!isRecord(rawValue) || typeof rawValue.id !== "string") return null;
  return {
    id: rawValue.id,
    ...(typeof rawValue.label === "string" ? { label: rawValue.label } : {}),
    ...(isVocabularyState(rawValue.state) ? { state: rawValue.state } : {}),
    ...(typeof rawValue.description === "string" ? { description: rawValue.description } : {}),
    ...(typeof rawValue.rationale === "string" ? { rationale: rawValue.rationale } : {}),
    ...(Array.isArray(rawValue.examples) ? { examples: stringArray(rawValue.examples) } : {}),
    ...(Array.isArray(rawValue.aliases) ? { aliases: stringArray(rawValue.aliases) } : {}),
    ...(isConfidence(rawValue.confidence) ? { confidence: rawValue.confidence } : {}),
    ...(Array.isArray(rawValue.evidence) ? { evidence: evidenceRefs(rawValue.evidence) } : {}),
    ...(Array.isArray(rawValue.seed_items) ? { seed_items: stringArray(rawValue.seed_items) } : {}),
    ...(typeof rawValue.parent === "string" ? { parent: rawValue.parent } : {}),
    ...(typeof rawValue.default_organization_group === "string"
      ? { default_organization_group: rawValue.default_organization_group }
      : {}),
    ...(Array.isArray(rawValue.related_activity) ? { related_activity: stringArray(rawValue.related_activity) } : {}),
  };
}

export async function queryFacetCuration(
  client: LlmClient,
  prompt: { system: string; user: string },
  model: string | undefined,
  clientOptions: Partial<QueryOptions> | undefined,
): Promise<{ raw: string; values: CuratedValue[] }> {
  const callerValidator = clientOptions?.responseValidator;
  const queryOptions: QueryOptions = {
    model: model ?? "deepseek/deepseek-v4-flash",
    // Do not impose a default completion cap here. Full-pack vocabulary
    // curation can require large JSON responses, and a too-small local
    // maxTokens setting becomes a truncation source. Callers may still pass
    // maxTokens deliberately through clientOptions for provider experiments.
    timeoutMs: clientOptions?.timeoutMs ?? VOCABULARY_CURATION_TIMEOUT_MS,
    ...clientOptions,
    responseValidator: (content) => {
      const shape = vocabularyCurationShapeValidator(content);
      if (!shape.ok) return shape;
      return callerValidator ? callerValidator(content) : { ok: true };
    },
  };
  const raw = client.querySplit
    ? await client.querySplit(prompt.system, prompt.user, queryOptions)
    : await client.query(`${prompt.system}\n\n${prompt.user}`, queryOptions);
  const verdict = queryOptions.responseValidator!(raw);
  if (!verdict.ok) {
    throw new Error(`facet vocabulary curation response failed validation: ${verdict.reason ?? "invalid response"}`);
  }
  return { raw, values: parseVocabularyCurationResponse(raw) };
}

export async function queryCombinedFacetCuration(
  client: LlmClient,
  prompt: { system: string; user: string },
  facets: readonly VocabularyFacetId[],
  model: string | undefined,
  clientOptions: Partial<QueryOptions> | undefined,
): Promise<{ raw: string; valuesByFacet: Map<VocabularyFacetId, CuratedValue[]> }> {
  const callerValidator = clientOptions?.responseValidator;
  const queryOptions: QueryOptions = {
    model: model ?? "deepseek/deepseek-v4-flash",
    timeoutMs: clientOptions?.timeoutMs ?? VOCABULARY_CURATION_TIMEOUT_MS,
    ...clientOptions,
    responseValidator: (content) => {
      const shape = vocabularyCurationByFacetShapeValidator(content, facets);
      if (!shape.ok) return shape;
      return callerValidator ? callerValidator(content) : { ok: true };
    },
  };
  const raw = client.querySplit
    ? await client.querySplit(prompt.system, prompt.user, queryOptions)
    : await client.query(`${prompt.system}\n\n${prompt.user}`, queryOptions);
  const verdict = queryOptions.responseValidator!(raw);
  if (!verdict.ok) {
    throw new Error(`facet vocabulary curation response failed validation: ${verdict.reason ?? "invalid response"}`);
  }
  return { raw, valuesByFacet: parseVocabularyCurationByFacetResponse(raw, facets) };
}

export function vocabularyCurationShapeValidator(content: string): { ok: boolean; reason?: string } {
  const parsed = parseVocabularyCurationJson(content);
  if (!isRecord(parsed)) return { ok: false, reason: "response must be a JSON object" };
  if (Array.isArray(parsed.values)) return { ok: true };
  if (isRecord(parsed.facets) && Object.values(parsed.facets).some((facet) =>
    isRecord(facet) && Array.isArray(facet.values)
  )) {
    return { ok: true };
  }
  return { ok: false, reason: "response must contain a top-level values array" };
}

export function vocabularyCurationByFacetShapeValidator(
  content: string,
  facets: readonly VocabularyFacetId[],
): { ok: boolean; reason?: string } {
  const parsed = parseVocabularyCurationJson(content);
  if (!isRecord(parsed)) return { ok: false, reason: "response must be a JSON object" };
  if (!isRecord(parsed.facets)) return { ok: false, reason: "response must contain a top-level facets object" };
  const allowed = new Set(facets);
  const hasAllowedFacet = Object.entries(parsed.facets).some(([facet, value]) =>
    allowed.has(facet as VocabularyFacetId) && isRecord(value) && Array.isArray(value.values)
  );
  return hasAllowedFacet ? { ok: true } : { ok: false, reason: "response facets object contains no requested facet values arrays" };
}
