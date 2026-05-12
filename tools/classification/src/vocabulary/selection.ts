import type { PackFacetVocabulary } from "../schema/vocabulary.ts";
import type { PackVocabularyCandidate, VocabularyFacetId } from "./types.ts";
import { compareCandidates } from "./candidate_store.ts";
import { splitResourceLocation } from "./helpers.ts";
import { isVocabularyFacet } from "./facets.ts";

export function groupCandidates(
  candidates: readonly PackVocabularyCandidate[],
  maxPerFacet: number,
): Record<string, PackVocabularyCandidate[]> {
  const grouped = new Map<string, PackVocabularyCandidate[]>();
  for (const candidate of candidates) {
    const group = grouped.get(candidate.facet) ?? [];
    group.push(candidate);
    grouped.set(candidate.facet, group);
  }
  const out: Record<string, PackVocabularyCandidate[]> = {};
  for (const [facet, values] of grouped) {
    out[facet] = selectPromptCandidates(values, maxPerFacet);
  }
  return out;
}

export function workflowRoleCandidatesForAcceptedWorkflows(
  candidates: readonly PackVocabularyCandidate[],
  acceptedWorkflowIds: ReadonlySet<string>,
): PackVocabularyCandidate[] {
  return candidates.filter((candidate) => {
    if (candidate.origin === "previous") return true;
    const parent = candidate.parent ?? candidate.id.split("#")[0] ?? "";
    return acceptedWorkflowIds.has(parent);
  });
}

export function selectPromptCandidates(
  candidates: readonly PackVocabularyCandidate[],
  maxCandidates: number,
): PackVocabularyCandidate[] {
  const max = Math.max(1, Math.floor(maxCandidates));
  if (candidates.length <= max) return [...candidates];

  const selected = new Map<string, PackVocabularyCandidate>();
  const add = (candidate: PackVocabularyCandidate) => {
    if (selected.size >= max) return;
    selected.set(candidate.id, candidate);
  };

  for (const candidate of candidates) {
    if (candidate.origin === "previous" || candidate.origin === "universal_default") add(candidate);
  }

  const priorityTarget = Math.min(max, Math.max(selected.size, Math.floor(max * 0.45)));
  for (const candidate of candidates) {
    if (selected.size >= priorityTarget) break;
    add(candidate);
  }

  const semanticTarget = Math.min(max, Math.max(selected.size, Math.floor(max * 0.7)));
  const semanticCandidates = [...candidates]
    .filter((candidate) => candidate.semantic_evidence.length > 0)
    .sort(compareSemanticPromptCandidates);
  for (const candidate of semanticCandidates) {
    if (selected.size >= semanticTarget) break;
    add(candidate);
  }

  const buckets = new Map<string, PackVocabularyCandidate[]>();
  for (const candidate of candidates) {
    if (selected.has(candidate.id)) continue;
    const key = promptCandidateBucket(candidate);
    const bucket = buckets.get(key) ?? [];
    bucket.push(candidate);
    buckets.set(key, bucket);
  }

  while (selected.size < max && buckets.size > 0) {
    let progressed = false;
    for (const [key, bucket] of [...buckets]) {
      const candidate = bucket.shift();
      if (candidate) {
        add(candidate);
        progressed = true;
      }
      if (bucket.length === 0) buckets.delete(key);
      if (selected.size >= max) break;
    }
    if (!progressed) break;
  }

  return [...selected.values()].sort(compareCandidates);
}

function promptCandidateBucket(candidate: PackVocabularyCandidate): string {
  const evidenceKind = candidate.evidence[0]?.kind ?? "none";
  const reason = candidate.reasons[0] ?? "";
  const namespace = splitResourceLocation(candidate.id)?.namespace ?? "pack";
  return `${candidate.facet}\u0000${candidate.origin}\u0000${candidate.suggested_state}\u0000${evidenceKind}\u0000${reason}\u0000${namespace}`;
}

function compareSemanticPromptCandidates(a: PackVocabularyCandidate, b: PackVocabularyCandidate): number {
  return b.semantic_evidence.length - a.semantic_evidence.length ||
    b.support - a.support ||
    b.confidence - a.confidence ||
    compareCandidates(a, b);
}

export function chunkCandidates(
  candidates: readonly PackVocabularyCandidate[],
  maxPerPrompt: number,
): PackVocabularyCandidate[][] {
  if (candidates.length === 0) return [[]];
  const size = Math.max(1, Math.floor(maxPerPrompt));
  const out: PackVocabularyCandidate[][] = [];
  for (let index = 0; index < candidates.length; index += size) {
    out.push(candidates.slice(index, index + size));
  }
  return out;
}

export function previousAcceptedByFacet(
  previous: PackFacetVocabulary | undefined,
): Map<VocabularyFacetId, string[]> {
  const out = new Map<VocabularyFacetId, string[]>();
  if (!previous) return out;
  for (const [facet, values] of Object.entries(previous.facets ?? {})) {
    if (!isVocabularyFacet(facet)) continue;
    out.set(facet, Object.entries(values.values ?? {})
      .filter(([, value]) => value.state === "accepted")
      .map(([id]) => id)
      .sort());
  }
  return out;
}
