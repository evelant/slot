import type { FacetEvidenceRecord } from "../../evidence/facet_evidence.ts";
import type { VocabularyState } from "../../schema/vocabulary.ts";
import type { CandidateAccumulator, SemanticEvidenceIndex, VocabularyFacetId } from "../types.ts";
import { MOD_SUBSYSTEM_CANONICAL_TOKENS, MOD_SUBSYSTEM_SIGNAL_TOKENS, MOD_SUBSYSTEM_STOP_TOKENS } from "../constants.ts";
import { addCandidate } from "../candidate_store.ts";
import { labelFromId, splitResourceLocation, token } from "../helpers.ts";
import { runtimeItemRefs, semanticEvidenceForCandidate } from "../semantic_index.ts";
import { evidenceRef } from "../records.ts";
import { isGenericValueId, resourcePathTail } from "../value_ids.ts";

export function addModSubsystemDocumentCandidate(
  acc: Map<string, CandidateAccumulator>,
  record: FacetEvidenceRecord,
  packId: string,
  facets: ReadonlySet<VocabularyFacetId>,
  semanticIndex: SemanticEvidenceIndex,
): void {
  if (!facets.has("mod_subsystem") || !isModSubsystemNamespace(record.namespace, packId)) return;
  const label = record.label ?? record.title ?? "";
  const tokens = modSubsystemSignalTokens([
    record.id,
    label,
    ...(record.kind === "mod_metadata" ? [record.description ?? ""] : []),
  ]);
  for (const value of tokens) {
    const id = modSubsystemId(record.namespace, value);
    if (!id || isGenericValueId(id) || modSubsystemIdLooksRejected(id)) continue;
    addModSubsystemCandidate(acc, {
      id,
      record,
      facets,
      count: record.count ?? record.item_refs?.length ?? 1,
      state: "review",
      confidence: Math.max(0.45, Math.min(0.75, record.confidence)),
      semanticIndex,
      reason: `${record.kind} may name a mod-owned subsystem`,
    });
  }
}

export function addModSubsystemRuntimeItemCandidates(
  acc: Map<string, CandidateAccumulator>,
  record: FacetEvidenceRecord,
  packId: string,
  facets: ReadonlySet<VocabularyFacetId>,
  semanticIndex: SemanticEvidenceIndex,
): void {
  if (!facets.has("mod_subsystem") || !isModSubsystemNamespace(record.namespace, packId)) return;
  const tokens = modSubsystemSignalTokens([
    record.id,
    record.label ?? "",
    ...ownedNamespaceValues(record.namespace, record.tags),
    ...ownedNamespaceValues(record.namespace, record.direct_tags),
    ...ownedNamespaceValues(record.namespace, record.model_parents),
    ...ownedNamespaceValues(record.namespace, record.creative_tabs),
  ]);
  for (const value of tokens) {
    const id = modSubsystemId(record.namespace, value);
    if (!id) continue;
    addModSubsystemCandidate(acc, {
      id,
      record,
      facets,
      count: 1,
      state: "review",
      confidence: 0.55,
      semanticIndex,
      reason: "runtime item id/tag/model token suggests subsystem identity",
    });
  }
}

export function addModSubsystemTagCandidates(
  acc: Map<string, CandidateAccumulator>,
  record: FacetEvidenceRecord,
  packId: string,
  facets: ReadonlySet<VocabularyFacetId>,
  semanticIndex: SemanticEvidenceIndex,
): void {
  if (!facets.has("mod_subsystem")) return;
  if (!isModSubsystemNamespace(record.namespace, packId)) return;
  const tokens = modSubsystemSignalTokens([record.id, record.label ?? ""]);
  if (tokens.length === 0) return;
  const count = Math.max(1, record.count ?? record.item_refs?.length ?? record.examples?.length ?? 1);
  for (const value of tokens) {
    const id = modSubsystemId(record.namespace, value);
    if (!id) continue;
    addModSubsystemCandidate(acc, {
      id,
      record,
      facets,
      count,
      state: "review",
      confidence: Math.max(0.5, Math.min(0.8, record.confidence)),
      semanticIndex,
      reason: `${record.kind} is a mod-owned subsystem-like tag`,
    });
  }
}

function addModSubsystemCandidate(
  acc: Map<string, CandidateAccumulator>,
  args: {
    id: string;
    record: FacetEvidenceRecord;
    facets: ReadonlySet<VocabularyFacetId>;
    count: number;
    state: VocabularyState;
    confidence: number;
    semanticIndex: SemanticEvidenceIndex;
    reason: string;
  },
): void {
  if (!args.facets.has("mod_subsystem")) return;
  if (isGenericValueId(args.id) || modSubsystemIdLooksRejected(args.id)) return;
  addCandidate(acc, {
    facet: "mod_subsystem",
    id: args.id,
    label: labelFromId(args.id),
    origin: "namespace_generated",
    suggestedState: args.state,
    confidence: args.confidence,
    support: args.count,
    evidence: [evidenceRef(args.record)],
    semanticEvidence: semanticEvidenceForCandidate(args.record, args.semanticIndex),
    seedItems: runtimeItemRefs(args.record.item_refs ?? args.record.examples, args.semanticIndex),
    reason: args.reason,
  });
}

function modSubsystemId(namespace: string, value: string): string | null {
  const normalizedNamespace = namespace.match(/^[a-z0-9_.-]+$/) ? namespace : null;
  if (!normalizedNamespace) return null;
  const normalizedToken = token(value);
  if (!normalizedToken || MOD_SUBSYSTEM_STOP_TOKENS.has(normalizedToken)) return null;
  return `${normalizedNamespace}:${normalizedToken}`;
}

function modSubsystemSignalTokens(values: readonly string[]): string[] {
  const out = new Set<string>();
  for (const value of values) {
    for (const raw of value.toLowerCase().split(/[^a-z0-9]+/)) {
      if (!raw || MOD_SUBSYSTEM_STOP_TOKENS.has(raw)) continue;
      const singular = raw.endsWith("s") && raw.length > 4 ? raw.slice(0, -1) : raw;
      addModSubsystemSignalToken(out, raw);
      addModSubsystemSignalToken(out, singular);
    }
    const tail = resourcePathTail(value);
    if (tail && !MOD_SUBSYSTEM_STOP_TOKENS.has(tail) && MOD_SUBSYSTEM_SIGNAL_TOKENS.has(tail)) {
      addModSubsystemSignalToken(out, tail);
    }
  }
  return [...out].sort();
}

function ownedNamespaceValues(namespace: string, values: readonly string[] | undefined): string[] {
  if (!values?.length) return [];
  return values.filter((value) => splitResourceLocation(value)?.namespace === namespace);
}

function addModSubsystemSignalToken(out: Set<string>, value: string): void {
  if (!MOD_SUBSYSTEM_SIGNAL_TOKENS.has(value)) return;
  out.add(MOD_SUBSYSTEM_CANONICAL_TOKENS.get(value) ?? value);
}

function isModSubsystemNamespace(namespace: string | undefined, packId?: string): namespace is string {
  return !!namespace && namespace !== packId && !["c", "forge", "minecraft"].includes(namespace);
}

export function modSubsystemIdLooksRejected(id: string): boolean {
  const split = splitResourceLocation(id);
  if (!split) return true;
  if (split.namespace === "slot" || split.namespace === "pack" || !isModSubsystemNamespace(split.namespace)) return true;
  const tail = resourcePathTail(id);
  return !tail || MOD_SUBSYSTEM_STOP_TOKENS.has(tail);
}
