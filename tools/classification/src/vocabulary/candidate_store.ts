import { validateMultiValue } from "../schema/facets.ts";
import type { VocabularyOrigin } from "../schema/vocabulary.ts";
import type { CandidateAccumulator, CandidateSeed, PackVocabularyCandidate } from "./types.ts";
import { CANDIDATE_EXAMPLE_LIMIT, SEMANTIC_EVIDENCE_LIMIT } from "./constants.ts";
import { round, sortedLimited, stateRank, strongestState } from "./helpers.ts";

export function addCandidate(
  acc: Map<string, CandidateAccumulator>,
  seed: CandidateSeed,
): void {
  const issue = validateMultiValue(seed.facet, [seed.id]);
  if (issue) return;
  const key = `${seed.facet}\u0000${seed.id}`;
  let candidate = acc.get(key);
  if (!candidate) {
    candidate = {
      facet: seed.facet,
      id: seed.id,
      label: seed.label,
      origin: seed.origin,
      suggested_state: seed.suggestedState,
      confidence: seed.confidence,
      support: seed.support ?? 1,
      evidence: [],
      semantic_evidence: [],
      seed_items: [],
      aliases: [],
      ...(seed.description ? { description: seed.description } : {}),
      ...(seed.parent ? { parent: seed.parent } : {}),
      ...(seed.defaultOrganizationGroup ? { default_organization_group: seed.defaultOrganizationGroup } : {}),
      ...(seed.relatedActivity ? { related_activity: [...seed.relatedActivity] } : {}),
      reasons: [],
      evidenceKeys: new Set(),
      semanticEvidenceKeys: new Set(),
      seedItemKeys: new Set(),
      aliasKeys: new Set(),
      reasonKeys: new Set(),
    };
    acc.set(key, candidate);
  } else {
    candidate.support += seed.support ?? 1;
    candidate.confidence = Math.max(candidate.confidence, seed.confidence);
    candidate.suggested_state = strongestState(candidate.suggested_state, seed.suggestedState);
    if (candidate.origin !== "previous" && seed.origin === "previous") candidate.origin = "previous";
    if (candidate.origin !== "universal_default" && seed.origin === "universal_default") candidate.origin = "universal_default";
    if (!candidate.description && seed.description) candidate.description = seed.description;
    if (!candidate.parent && seed.parent) candidate.parent = seed.parent;
    if (!candidate.default_organization_group && seed.defaultOrganizationGroup) {
      candidate.default_organization_group = seed.defaultOrganizationGroup;
    }
    if (!candidate.related_activity && seed.relatedActivity) candidate.related_activity = [...seed.relatedActivity];
  }

  for (const evidence of seed.evidence ?? []) {
    const evidenceKey = `${evidence.kind}\u0000${evidence.id}`;
    if (candidate.evidenceKeys.has(evidenceKey)) continue;
    candidate.evidenceKeys.add(evidenceKey);
    candidate.evidence.push(evidence);
  }
  for (const evidence of seed.semanticEvidence ?? []) {
    const text = evidence.text ?? "";
    const evidenceKey = `${evidence.kind}\u0000${evidence.id}\u0000${evidence.source}\u0000${evidence.key ?? ""}\u0000${text}`;
    if (candidate.semanticEvidenceKeys.has(evidenceKey)) continue;
    candidate.semanticEvidenceKeys.add(evidenceKey);
    candidate.semantic_evidence.push(evidence);
  }
  for (const item of seed.seedItems ?? []) {
    if (candidate.seedItemKeys.has(item)) continue;
    candidate.seedItemKeys.add(item);
    candidate.seed_items.push(item);
  }
  for (const alias of seed.aliases ?? []) {
    const normalized = alias.trim();
    if (!normalized || normalized === seed.label || candidate.aliasKeys.has(normalized)) continue;
    candidate.aliasKeys.add(normalized);
    candidate.aliases.push(normalized);
  }
  if (seed.reason && !candidate.reasonKeys.has(seed.reason)) {
    candidate.reasonKeys.add(seed.reason);
    candidate.reasons.push(seed.reason);
  }
}


export function stripCandidateInternals(candidate: PackVocabularyCandidate): PackVocabularyCandidate {
  return {
    facet: candidate.facet,
    id: candidate.id,
    label: candidate.label,
    origin: candidate.origin,
    suggested_state: candidate.suggested_state,
    confidence: round(candidate.confidence),
    support: candidate.support,
    evidence: candidate.evidence,
    semantic_evidence: candidate.semantic_evidence,
    seed_items: candidate.seed_items,
    aliases: candidate.aliases,
    ...(candidate.description ? { description: candidate.description } : {}),
    ...(candidate.parent ? { parent: candidate.parent } : {}),
    ...(candidate.default_organization_group ? { default_organization_group: candidate.default_organization_group } : {}),
    ...(candidate.related_activity ? { related_activity: candidate.related_activity } : {}),
    reasons: candidate.reasons,
  };
}

export function finalizeCandidate(candidate: CandidateAccumulator): PackVocabularyCandidate {
  candidate.evidence = candidate.evidence.slice(0, CANDIDATE_EXAMPLE_LIMIT);
  candidate.semantic_evidence = candidate.semantic_evidence.slice(0, SEMANTIC_EVIDENCE_LIMIT);
  candidate.seed_items = sortedLimited(candidate.seed_items, CANDIDATE_EXAMPLE_LIMIT);
  candidate.aliases = sortedLimited(candidate.aliases, CANDIDATE_EXAMPLE_LIMIT);
  candidate.reasons = sortedLimited(candidate.reasons, CANDIDATE_EXAMPLE_LIMIT);
  return stripCandidateInternals(candidate);
}

export function compareCandidates(a: PackVocabularyCandidate, b: PackVocabularyCandidate): number {
  return a.facet.localeCompare(b.facet) ||
    originRank(a.origin) - originRank(b.origin) ||
    stateRank(a.suggested_state) - stateRank(b.suggested_state) ||
    b.support - a.support ||
    b.semantic_evidence.length - a.semantic_evidence.length ||
    b.confidence - a.confidence ||
    a.id.localeCompare(b.id);
}

function originRank(origin: VocabularyOrigin): number {
  switch (origin) {
    case "previous":
      return 0;
    case "universal_default":
      return 1;
    default:
      return 2;
  }
}
