import type { LlmClient, QueryOptions } from "../llm/client.ts";
import type { CuratedValue } from "./types.ts";
import { VOCABULARY_CURATION_TIMEOUT_MS } from "./constants.ts";
import { evidenceRefs, firstJsonObject, isConfidence, isRecord, isVocabularyState, stringArray, unwrapEnvelope } from "./helpers.ts";

export function parseVocabularyCurationResponse(raw: string): CuratedValue[] {
  const inner = unwrapEnvelope(raw);
  const json = firstJsonObject(inner);
  if (!json) return [];
  let parsed: unknown;
  try {
    parsed = JSON.parse(json);
  } catch {
    return [];
  }
  if (!isRecord(parsed)) return [];
  const rawValues = Array.isArray(parsed.values)
    ? parsed.values
    : isRecord(parsed.facets)
      ? Object.values(parsed.facets).flatMap((facet) =>
        isRecord(facet) && Array.isArray(facet.values) ? facet.values : []
      )
      : [];
  const out: CuratedValue[] = [];
  for (const rawValue of rawValues) {
    if (!isRecord(rawValue) || typeof rawValue.id !== "string") continue;
    out.push({
      id: rawValue.id,
      ...(typeof rawValue.label === "string" ? { label: rawValue.label } : {}),
      ...(isVocabularyState(rawValue.state) ? { state: rawValue.state } : {}),
      ...(typeof rawValue.description === "string" ? { description: rawValue.description } : {}),
      ...(Array.isArray(rawValue.aliases) ? { aliases: stringArray(rawValue.aliases) } : {}),
      ...(isConfidence(rawValue.confidence) ? { confidence: rawValue.confidence } : {}),
      ...(Array.isArray(rawValue.evidence) ? { evidence: evidenceRefs(rawValue.evidence) } : {}),
      ...(Array.isArray(rawValue.seed_items) ? { seed_items: stringArray(rawValue.seed_items) } : {}),
      ...(typeof rawValue.parent === "string" ? { parent: rawValue.parent } : {}),
      ...(typeof rawValue.default_organization_group === "string"
        ? { default_organization_group: rawValue.default_organization_group }
        : {}),
      ...(Array.isArray(rawValue.related_activity) ? { related_activity: stringArray(rawValue.related_activity) } : {}),
    });
  }
  return out;
}


export async function queryFacetCuration(
  client: LlmClient,
  prompt: { system: string; user: string },
  model: string | undefined,
  clientOptions: Partial<QueryOptions> | undefined,
  expectedCandidateIds: readonly string[],
): Promise<{ raw: string; values: CuratedValue[] }> {
  const callerValidator = clientOptions?.responseValidator;
  const coverageValidator = vocabularyCurationCoverageValidator(expectedCandidateIds);
  const queryOptions: QueryOptions = {
    model: model ?? "deepseek/deepseek-v4-flash",
    // Do not impose a default completion cap here. Full-pack vocabulary
    // curation can require large JSON responses, and a too-small local
    // maxTokens setting becomes a truncation source. Callers may still pass
    // maxTokens deliberately through clientOptions for provider experiments.
    timeoutMs: clientOptions?.timeoutMs ?? VOCABULARY_CURATION_TIMEOUT_MS,
    ...clientOptions,
    responseValidator: (content) => {
      const coverage = coverageValidator(content);
      if (!coverage.ok) return coverage;
      return callerValidator ? callerValidator(content) : { ok: true };
    },
  };
  const raw = client.querySplit
    ? await client.querySplit(prompt.system, prompt.user, queryOptions)
    : await client.query(`${prompt.system}\n\n${prompt.user}`, queryOptions);
  const verdict = queryOptions.responseValidator!(raw);
  if (!verdict.ok) {
    throw new Error(`facet vocabulary curation response failed coverage validation: ${verdict.reason ?? "incomplete response"}`);
  }
  return { raw, values: parseVocabularyCurationResponse(raw) };
}

export function vocabularyCurationCoverageValidator(
  expectedCandidateIds: readonly string[],
): (content: string) => { ok: boolean; reason?: string } {
  const expected = new Set(expectedCandidateIds);
  return (content: string) => {
    let parsed: CuratedValue[];
    try {
      parsed = parseVocabularyCurationResponse(content);
    } catch (err) {
      return { ok: false, reason: `parse failed: ${(err as Error).message.slice(0, 120)}` };
    }
    const seen = new Set(parsed.map((value) => value.id));
    const missing = [...expected].filter((id) => !seen.has(id));
    if (missing.length > 0) {
      return {
        ok: false,
        reason: `missing ${missing.length}/${expected.size} candidate decision(s): ${missing.slice(0, 8).join(", ")}`,
      };
    }
    return { ok: true };
  };
}
