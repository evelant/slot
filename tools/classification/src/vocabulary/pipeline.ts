import { readFileSync } from "node:fs";
import type {
  FacetEvidenceArtifact,
  FacetEvidenceRecord,
} from "../evidence/facet_evidence.ts";
import { validateMultiValue } from "../schema/facets.ts";
import {
  validateVocabularyArtifact,
  type PackFacetVocabulary,
  type VocabularyState,
  type VocabularyValue,
} from "../schema/vocabulary.ts";
import type {
  CandidateAccumulator,
  CandidateSeed,
  CuratedValue,
  ExtractVocabularyCandidatesOptions,
  PackFacetVocabularyReview,
  PackVocabularyCandidate,
  ProposePackFacetVocabularyOptions,
  ProposePackFacetVocabularyResult,
  VocabularyReviewSummary,
  SemanticEvidenceIndex,
  VocabularyDecision,
  VocabularyDiagnostic,
  VocabularyFacetId,
  VocabularyReviewDecision,
  VocabularySemanticEvidence,
} from "./types.ts";
import {
  isRecord,
  labelFromId,
  round,
  sortedLimited,
  splitResourceLocation,
  stateRank,
  strongestState,
  tokenPath,
  tokenSet,
} from "./helpers.ts";
import {
  CANDIDATE_EXAMPLE_LIMIT,
  DEFAULT_MAX_CANDIDATES_PER_FACET,
  GENERIC_TOKENS,
  ROLE_TOKENS,
  SEMANTIC_EVIDENCE_LIMIT,
  UNIVERSAL_DEFAULTS,
  VOCABULARY_PROMPT_CHAR_BUDGET,
} from "./constants.ts";
import {
  buildSemanticEvidenceIndex,
  documentLooksLikeWorkflowOrUseContext,
  looksLikeProgressionStage,
  promptSemanticEvidence,
  runtimeItemRefs,
  semanticEvidenceForCandidate,
} from "./semantic_index.ts";
import { buildVocabularyCurationPrompt } from "./prompt.ts";
import {
  parseVocabularyCurationResponse,
  queryFacetCuration,
} from "./curation.ts";
import {
  addCandidate,
  compareCandidates,
  finalizeCandidate,
} from "./candidate_store.ts";
import {
  chunkCandidates,
  groupCandidates,
  previousAcceptedByFacet,
  selectPromptCandidates,
  workflowRoleCandidatesForAcceptedWorkflows,
} from "./selection.ts";
import {
  facetSet,
  isVocabularyFacet,
} from "./facets.ts";
import { buildVocabularyPromptOverview } from "./pack_context.ts";
import {
  evidenceRef,
  namespaceAllowed,
} from "./records.ts";
import {
  addOrganizationGroupEvidenceCandidates,
} from "./candidates/organization_group.ts";
import {
  addModSubsystemDocumentCandidate,
  addModSubsystemRuntimeItemCandidates,
  addModSubsystemTagCandidates,
  modSubsystemIdLooksRejected,
} from "./candidates/mod_subsystem.ts";
import {
  isGenericValueId,
} from "./value_ids.ts";
export type {
  ExtractVocabularyCandidatesOptions,
  PackFacetVocabularyReview,
  PackVocabularyCandidate,
  ProposePackFacetVocabularyOptions,
  ProposePackFacetVocabularyResult,
  VocabularyDecision,
  VocabularyDiagnostic,
  VocabularyFacetId,
  VocabularyReviewDecision,
  VocabularySemanticEvidence,
} from "./types.ts";
export { buildVocabularyCurationPrompt } from "./prompt.ts";
export { parseVocabularyCurationResponse } from "./curation.ts";
export { buildVocabularyPromptOverview } from "./pack_context.ts";


export function readFacetEvidenceArtifactFile(path: string): FacetEvidenceArtifact {
  const parsed = JSON.parse(readFileSync(path, "utf8")) as unknown;
  if (!isRecord(parsed)) throw new Error(`facet evidence must be an object: ${path}`);
  if (parsed.kind !== "slot-pack-facet-evidence") {
    throw new Error(`facet evidence kind must be slot-pack-facet-evidence: ${path}`);
  }
  if (parsed.schema_version !== 1) {
    throw new Error(`facet evidence schema_version must be 1: ${path}`);
  }
  if (typeof parsed.pack_id !== "string" || parsed.pack_id.length === 0) {
    throw new Error(`facet evidence pack_id is missing: ${path}`);
  }
  if (!Array.isArray(parsed.records)) {
    throw new Error(`facet evidence records must be an array: ${path}`);
  }
  return parsed as unknown as FacetEvidenceArtifact;
}

export function extractVocabularyCandidates(
  evidence: FacetEvidenceArtifact,
  options: ExtractVocabularyCandidatesOptions,
): PackVocabularyCandidate[] {
  const facets = facetSet(options.facets);
  const namespaces = new Set((options.namespaces ?? []).filter(Boolean));
  const acc = new Map<string, CandidateAccumulator>();

  addUniversalDefaults(acc, facets);
  if (options.previousVocabulary) {
    addPreviousVocabulary(acc, options.previousVocabulary, facets);
  }

  const records = evidence.records.filter((record) => namespaceAllowed(record, namespaces));
  const semanticIndex = buildSemanticEvidenceIndex(records);
  for (const record of records) {
    addCandidatesFromEvidence(acc, record, options.packId, facets, options.minEvidence, semanticIndex);
  }
  addWorkflowRoleCandidates(acc, options.minEvidence);

  return [...acc.values()]
    .map(finalizeCandidate)
    .filter((candidate) => facets.has(candidate.facet))
    .sort(compareCandidates);
}


export async function proposePackFacetVocabulary(
  options: ProposePackFacetVocabularyOptions,
): Promise<ProposePackFacetVocabularyResult> {
  const generatedAt = options.generatedAt ?? new Date().toISOString();
  const facets = [...facetSet(options.facets)].sort();
  const candidates = extractVocabularyCandidates(options.evidence, options);
  const promptRecords = options.evidence.records.filter((record) =>
    namespaceAllowed(record, new Set((options.namespaces ?? []).filter(Boolean)))
  );
  const maxCandidatesPerFacet = options.maxCandidatesPerFacet ?? DEFAULT_MAX_CANDIDATES_PER_FACET;
  const candidatesByFacet = groupCandidates(candidates, maxCandidatesPerFacet);
  const rawCandidatesByFacet = groupCandidates(candidates, Number.MAX_SAFE_INTEGER);
  const previousAccepted = previousAcceptedByFacet(options.previousVocabulary);
  const acceptedWorkflowIds = new Set(previousAccepted.get("workflow") ?? []);
  const prompts: Record<string, { system: string; user: string }> = {};
  const decisions: Record<string, VocabularyDecision[]> = {};
  const diagnostics: VocabularyDiagnostic[] = [];

  for (const facet of facets) {
    const packOverview = buildVocabularyPromptOverview({ facet, records: promptRecords });
    const facetCandidates = facet === "workflow_role"
      ? selectPromptCandidates(
        workflowRoleCandidatesForAcceptedWorkflows(rawCandidatesByFacet[facet] ?? [], acceptedWorkflowIds),
        maxCandidatesPerFacet,
      )
      : candidatesByFacet[facet] ?? [];
    const promptChunks = buildFacetPromptChunks({
      facet,
      packId: options.packId,
      candidates: facetCandidates,
      previousAccepted: previousAccepted.get(facet) ?? [],
      minEvidence: options.minEvidence,
      packOverview,
      maxCandidatesPerPrompt: options.maxCandidatesPerPrompt,
    });
    const curatedValues: CuratedValue[] = [];
    for (let index = 0; index < promptChunks.length; index++) {
      const promptChunk = promptChunks[index]!;
      const promptKey = promptChunks.length === 1
        ? facet
        : `${facet}.part-${String(index + 1).padStart(2, "0")}-of-${String(promptChunks.length).padStart(2, "0")}`;
      prompts[promptKey] = promptChunk.prompt;
      const curated: { raw: string; values: CuratedValue[] } = options.client
        ? await queryFacetCuration(options.client, prompts[promptKey]!, options.model, options.clientOptions)
        : { raw: "", values: [] };
      curatedValues.push(...curated.values);
    }
    decisions[facet] = applyFacetPolicy({
      facet,
      candidates: facetCandidates,
      allCandidates: rawCandidatesByFacet[facet] ?? facetCandidates,
      curated: curatedValues,
      minEvidence: options.minEvidence,
      diagnostics,
      acceptedWorkflowIds: facet === "workflow_role" ? acceptedWorkflowIds : undefined,
    });
    if (facet === "workflow") {
      for (const value of decisions[facet] ?? []) {
        if (value.state === "accepted") acceptedWorkflowIds.add(value.id);
      }
    }
  }

  const vocabulary: PackFacetVocabulary = {
    schema_version: 1,
    kind: "slot-pack-facet-vocabulary",
    pack_id: options.packId,
    generated_by: options.generatedBy,
    generated_at: generatedAt,
    source: {
      facet_evidence: options.evidencePath,
      ...(options.evidence.source ?? {}),
      ...(options.previousVocabularyPath ? { previous_vocabulary: options.previousVocabularyPath } : {}),
    },
    facets: decisionsToAcceptedVocabulary(decisions),
  };
  const validation = validateVocabularyArtifact(vocabulary);
  if (!validation.ok) {
    for (const error of validation.errors) {
      diagnostics.push({ severity: "error", message: error });
    }
  }

  const review: PackFacetVocabularyReview = {
    schema_version: 1,
    kind: "slot-pack-facet-vocabulary-review",
    pack_id: options.packId,
    generated_by: options.generatedBy,
    generated_at: generatedAt,
    source: vocabulary.source,
    ...(options.model ? { model: options.model } : {}),
    filters: {
      facets,
      namespaces: [...(options.namespaces ?? [])].sort(),
      min_evidence: options.minEvidence,
      ...(options.previousVocabularyPath ? { previous_vocabulary: options.previousVocabularyPath } : {}),
    },
    summary: summarizeReviewDecisions(decisions),
    decisions: toReviewDecisions(decisions),
    diagnostics,
  };

  return { vocabulary, review, prompts };
}

interface VocabularyPromptChunk {
  candidates: PackVocabularyCandidate[];
  prompt: { system: string; user: string };
}

function buildFacetPromptChunks(args: {
  facet: VocabularyFacetId;
  packId: string;
  candidates: readonly PackVocabularyCandidate[];
  previousAccepted: readonly string[];
  minEvidence: number;
  packOverview?: ReturnType<typeof buildVocabularyPromptOverview>;
  maxCandidatesPerPrompt?: number;
}): VocabularyPromptChunk[] {
  const initialChunks = args.maxCandidatesPerPrompt
    ? chunkCandidates(args.candidates, args.maxCandidatesPerPrompt)
    : [[...args.candidates]];
  return initialChunks.flatMap((chunk) => splitPromptChunkToBudget({ ...args, candidates: chunk }));
}

function splitPromptChunkToBudget(args: {
  facet: VocabularyFacetId;
  packId: string;
  candidates: readonly PackVocabularyCandidate[];
  previousAccepted: readonly string[];
  minEvidence: number;
  packOverview?: ReturnType<typeof buildVocabularyPromptOverview>;
}): VocabularyPromptChunk[] {
  const candidates = [...args.candidates];
  const prompt = buildVocabularyCurationPrompt({
    facet: args.facet,
    packId: args.packId,
    candidates,
    previousAccepted: args.previousAccepted,
    minEvidence: args.minEvidence,
    packOverview: args.packOverview,
  });
  if (promptSize(prompt) <= VOCABULARY_PROMPT_CHAR_BUDGET || candidates.length <= 1) {
    return [{ candidates, prompt }];
  }
  const midpoint = Math.ceil(candidates.length / 2);
  return [
    ...splitPromptChunkToBudget({ ...args, candidates: candidates.slice(0, midpoint) }),
    ...splitPromptChunkToBudget({ ...args, candidates: candidates.slice(midpoint) }),
  ];
}

function promptSize(prompt: { system: string; user: string }): number {
  return prompt.system.length + prompt.user.length;
}

function addUniversalDefaults(
  acc: Map<string, CandidateAccumulator>,
  facets: ReadonlySet<VocabularyFacetId>,
): void {
  for (const [facet, values] of Object.entries(UNIVERSAL_DEFAULTS)) {
    if (!isVocabularyFacet(facet) || !facets.has(facet)) continue;
    for (const value of values) {
      addCandidate(acc, {
        facet,
        id: value.id,
        label: value.label,
        description: value.description,
        aliases: value.aliases,
        origin: "universal_default",
        suggestedState: "accepted",
        confidence: 0.95,
        support: 999,
        reason: "universal default",
      });
    }
  }
}

function addPreviousVocabulary(
  acc: Map<string, CandidateAccumulator>,
  previous: PackFacetVocabulary,
  facets: ReadonlySet<VocabularyFacetId>,
): void {
  for (const [facet, facetValues] of Object.entries(previous.facets ?? {})) {
    if (!isVocabularyFacet(facet) || !facets.has(facet)) continue;
    for (const [id, value] of Object.entries(facetValues.values ?? {})) {
      addCandidate(acc, {
        facet,
        id,
        label: value.label,
        description: value.description,
        aliases: value.aliases,
        origin: "previous",
        suggestedState: value.state,
        confidence: value.confidence ?? 0.9,
        support: value.state === "accepted" ? 999 : 1,
        evidence: value.evidence,
        seedItems: value.seed_items,
        parent: value.parent,
        defaultOrganizationGroup: value.default_organization_group,
        relatedActivity: value.related_activity,
        reason: `previous vocabulary state=${value.state}`,
      });
    }
  }
}

function addCandidatesFromEvidence(
  acc: Map<string, CandidateAccumulator>,
  record: FacetEvidenceRecord,
  packId: string,
  facets: ReadonlySet<VocabularyFacetId>,
  minEvidence: number,
  semanticIndex: SemanticEvidenceIndex,
): void {
  switch (record.kind) {
    case "recipe_type": {
      const id = normalizeScopedResourceId(record.id, packId, "namespace");
      if (!id || isGenericValueId(id)) return;
      const count = record.count ?? record.input_count ?? record.output_count ?? 1;
      addEvidenceCandidate(acc, {
        facet: "workflow",
        id,
        record,
        count,
        facets,
        state: count >= minEvidence ? "accepted" : "review",
        reason: "recipe type names a repeated process",
        semanticIndex,
      });
      addEvidenceCandidate(acc, {
        facet: "used_at",
        id,
        record,
        count,
        facets,
        state: count >= minEvidence ? "review" : "review",
        reason: "recipe type may name station/category context",
        semanticIndex,
      });
      break;
    }
    case "recipe_id_family": {
      // Recipe id families are useful supporting evidence, but in large
      // packs they are usually product/material families (`shaped/bolt`,
      // `crafting/oak`, `temp/small`) rather than player-facing workflows.
      // Promoting them to workflow candidates swamps recipe types and guide
      // topics, which are better vocabulary sources.
      break;
    }
    case "guide_page": {
      addDocumentCandidates(acc, record, packId, facets, "namespace", "guide page title", semanticIndex);
      addOrganizationGroupEvidenceCandidates(acc, record, packId, facets, semanticIndex);
      addModSubsystemDocumentCandidate(acc, record, packId, facets, semanticIndex);
      break;
    }
    case "quest_node": {
      addDocumentCandidates(acc, record, packId, facets, "pack", "quest title", semanticIndex);
      addOrganizationGroupEvidenceCandidates(acc, record, packId, facets, semanticIndex);
      break;
    }
    case "advancement": {
      addDocumentCandidates(acc, record, packId, facets, "namespace", "advancement title", semanticIndex, {
        requireWorkflowSignal: true,
      });
      addModSubsystemDocumentCandidate(acc, record, packId, facets, semanticIndex);
      break;
    }
    case "stack_group": {
      addStackGroupCandidate(acc, record, packId, facets, semanticIndex);
      break;
    }
    case "item_tag":
    case "block_tag": {
      addTagDomainCandidates(acc, record, facets, semanticIndex);
      addOrganizationGroupEvidenceCandidates(acc, record, packId, facets, semanticIndex);
      addModSubsystemTagCandidates(acc, record, packId, facets, semanticIndex);
      break;
    }
    case "existing_vocab":
    case "kubejs_tooltip":
    case "recipe_role_summary":
      break;
    case "mod_metadata": {
      addOrganizationGroupEvidenceCandidates(acc, record, packId, facets, semanticIndex);
      addModSubsystemDocumentCandidate(acc, record, packId, facets, semanticIndex);
      break;
    }
    case "runtime_item": {
      addOrganizationGroupEvidenceCandidates(acc, record, packId, facets, semanticIndex);
      addModSubsystemRuntimeItemCandidates(acc, record, packId, facets, semanticIndex);
      break;
    }
  }
}

function addDocumentCandidates(
  acc: Map<string, CandidateAccumulator>,
  record: FacetEvidenceRecord,
  packId: string,
  facets: ReadonlySet<VocabularyFacetId>,
  scope: "namespace" | "pack",
  reason: string,
  semanticIndex: SemanticEvidenceIndex,
  options: { requireWorkflowSignal?: boolean } = {},
): void {
  const label = record.label ?? record.title ?? labelFromId(record.id);
  const token = tokenPath(label);
  if (!token || GENERIC_TOKENS.has(token)) return;
  const namespace = scope === "namespace" ? record.namespace : undefined;
  const id = scope === "pack" || !namespace ? `pack:${packId}/${token}` : `${namespace}:${token}`;
  const count = record.count ?? 1;
  if (!options.requireWorkflowSignal || documentLooksLikeWorkflowOrUseContext(record, label)) {
    addEvidenceCandidate(acc, {
      facet: "workflow",
      id,
      record,
      count,
      facets,
      state: "review",
      reason,
      semanticIndex,
    });
    addEvidenceCandidate(acc, {
      facet: "used_at",
      id,
      record,
      count,
      facets,
      state: "review",
      reason: `${reason} may name a station, tool, surface, or interaction context`,
      semanticIndex,
    });
  }
  if (looksLikeProgressionStage(record, label)) {
    addEvidenceCandidate(acc, {
      facet: "progression_stage",
      id,
      record,
      count,
      facets,
      state: "review",
      reason: `${reason} names a likely progression gate`,
      semanticIndex,
    });
  }
}

function addTagDomainCandidates(
  acc: Map<string, CandidateAccumulator>,
  record: FacetEvidenceRecord,
  facets: ReadonlySet<VocabularyFacetId>,
  semanticIndex: SemanticEvidenceIndex,
): void {
  const tokens = tokenSet(record.id);
  for (const [facet, defaults] of Object.entries(UNIVERSAL_DEFAULTS)) {
    if (!isVocabularyFacet(facet) || !facets.has(facet)) continue;
    for (const value of defaults) {
      const valueToken = value.id.slice("slot:".length);
      if (!tokens.has(valueToken)) continue;
      addCandidate(acc, {
        facet,
        id: value.id,
        label: value.label,
        origin: "universal_default",
        suggestedState: "accepted",
        confidence: Math.max(0.8, record.confidence),
        support: record.count ?? 1,
        evidence: [evidenceRef(record)],
        semanticEvidence: semanticEvidenceForCandidate(record, semanticIndex),
        seedItems: runtimeItemRefs(record.item_refs ?? record.examples, semanticIndex),
        reason: `${record.kind} token matches universal ${facet} value`,
      });
    }
  }
}

function addStackGroupCandidate(
  acc: Map<string, CandidateAccumulator>,
  record: FacetEvidenceRecord,
  packId: string,
  facets: ReadonlySet<VocabularyFacetId>,
  semanticIndex: SemanticEvidenceIndex,
): void {
  if (!facets.has("organization_group")) return;
  const id = normalizeScopedResourceId(record.id, packId, "namespace");
  if (!id || isGenericValueId(id)) return;
  addCandidate(acc, {
    facet: "organization_group",
    id,
    label: record.label ?? labelFromId(id),
    origin: "pack_generated",
    suggestedState: "review",
    confidence: Math.max(0.55, Math.min(0.8, record.confidence)),
    support: Math.max(1, record.count ?? record.item_refs?.length ?? record.tags?.length ?? 1),
    evidence: [evidenceRef(record)],
    semanticEvidence: semanticEvidenceForCandidate(record, semanticIndex),
    seedItems: runtimeItemRefs(record.item_refs ?? record.examples, semanticIndex),
    reason: "pack stack group names an organization group",
  });
}


function addWorkflowRoleCandidates(
  acc: Map<string, CandidateAccumulator>,
  minEvidence: number,
): void {
  const workflows = [...acc.values()].filter((candidate) =>
    candidate.facet === "workflow" && candidate.support >= minEvidence && candidate.suggested_state !== "rejected"
  );
  for (const workflow of workflows) {
    for (const role of ROLE_TOKENS) {
      addCandidate(acc, {
        facet: "workflow_role",
        id: `${workflow.id}#${role}`,
        label: `${workflow.label} ${labelFromId(role)}`,
        origin: workflow.origin === "universal_default" ? "universal_default" : "pack_generated",
        suggestedState: workflow.suggested_state === "accepted" ? "accepted" : "review",
        confidence: Math.min(0.85, workflow.confidence),
        support: workflow.support,
        evidence: workflow.evidence,
        semanticEvidence: workflow.semantic_evidence,
        seedItems: workflow.seed_items,
        parent: workflow.id,
        reason: `derived ${role} role from workflow candidate`,
      });
    }
  }
}


function addEvidenceCandidate(
  acc: Map<string, CandidateAccumulator>,
  args: {
    facet: VocabularyFacetId;
    id: string;
    record: FacetEvidenceRecord;
    count: number;
    facets: ReadonlySet<VocabularyFacetId>;
    state: VocabularyState;
    reason: string;
    semanticIndex?: SemanticEvidenceIndex;
  },
): void {
  if (!args.facets.has(args.facet)) return;
  addCandidate(acc, {
    facet: args.facet,
    id: args.id,
    label: args.record.label ?? labelFromId(args.id),
    origin: args.id.startsWith("pack:") ? "pack_generated" : "namespace_generated",
    suggestedState: args.state,
    confidence: Math.max(0.4, Math.min(0.9, args.record.confidence)),
    support: args.count,
    evidence: [evidenceRef(args.record)],
    semanticEvidence: semanticEvidenceForCandidate(args.record, args.semanticIndex),
    seedItems: runtimeItemRefs(args.record.item_refs ?? args.record.examples, args.semanticIndex),
    reason: args.reason,
  });
}


function applyFacetPolicy(args: {
  facet: VocabularyFacetId;
  candidates: readonly PackVocabularyCandidate[];
  allCandidates?: readonly PackVocabularyCandidate[];
  curated: readonly CuratedValue[];
  minEvidence: number;
  diagnostics: VocabularyDiagnostic[];
  acceptedWorkflowIds?: ReadonlySet<string>;
}): VocabularyDecision[] {
  const candidateById = new Map(args.candidates.map((candidate) => [candidate.id, candidate]));
  const allCandidateById = new Map((args.allCandidates ?? args.candidates).map((candidate) => [candidate.id, candidate]));
  const curatedById = new Map<string, CuratedValue>();
  for (const value of args.curated) {
    if (!curatedById.has(value.id)) curatedById.set(value.id, value);
  }

  const decisions: VocabularyDecision[] = [];
  const ids = new Set([
    ...args.candidates
      .filter((candidate) => candidate.origin === "previous" || candidate.origin === "universal_default")
      .map((candidate) => candidate.id),
    ...args.curated.map((value) => value.id),
  ]);
  for (const id of [...ids].sort()) {
    const candidate = candidateById.get(id);
    const curated = curatedById.get(id);
    const base = candidate ?? curatedToCandidate(args.facet, curated!);
    const notes: string[] = [];
    let state = curated?.state ?? base.suggested_state;
    let origin = base.origin;
    let confidence = curated?.confidence ?? base.confidence;
    let label = curated?.label ?? base.label;
    let description = curated?.description ?? base.description;
    let rationale = curated?.rationale;
    let examples = sortedLimited(curated?.examples ?? [], CANDIDATE_EXAMPLE_LIMIT);
    let aliases = sortedLimited([...(curated?.aliases ?? []), ...base.aliases], CANDIDATE_EXAMPLE_LIMIT);
    let evidence = curated?.evidence?.length ? curated.evidence : base.evidence;
    let seedItems = curated?.seed_items?.length ? curated.seed_items : base.seed_items;
    let defaultOrganizationGroup = curated?.default_organization_group ?? base.default_organization_group;
    let relatedActivity = sortedLimited(
      [...(curated?.related_activity ?? []), ...(base.related_activity ?? [])],
      CANDIDATE_EXAMPLE_LIMIT,
    );

    const issue = validateMultiValue(args.facet, [id]);
    if (issue) {
      state = "rejected";
      notes.push(issue.reason);
    }
    if (!issue && candidate?.origin === "universal_default" && state !== "accepted") {
      state = "accepted";
      origin = "universal_default";
      confidence = Math.max(confidence, candidate.confidence);
      notes.push("universal default kept accepted by policy");
    }
    if (!issue && candidate?.origin === "universal_default" && state === "accepted") {
      label = candidate.label;
      description = candidate.description;
      aliases = candidate.aliases;
      evidence = candidate.evidence;
      seedItems = candidate.seed_items;
      defaultOrganizationGroup = candidate.default_organization_group;
      relatedActivity = candidate.related_activity ?? [];
      origin = "universal_default";
      confidence = Math.max(confidence, candidate.confidence);
    }
    if (!candidate) {
      origin = "stage3_proposed";
      if (state === "accepted") {
        notes.push("model-synthesized value accepted from context");
      }
    }
    if (!curated && origin !== "previous" && origin !== "universal_default") {
      state = "rejected";
      notes.push("context record omitted by synthesis response");
    }
    if (
      candidate &&
      candidate.origin !== "previous" &&
      candidate.origin !== "universal_default" &&
      candidate.support < args.minEvidence &&
      state === "accepted" &&
      !allowsLowSupportAcceptedValue(args.facet, candidate, curated)
    ) {
      state = "review";
      notes.push(`support ${candidate.support} below min_evidence ${args.minEvidence}`);
    }
    if (isGenericValueId(id) && state === "accepted") {
      state = "rejected";
      notes.push("generic catch-all value rejected by policy");
    }
    if (args.facet === "mod_subsystem" && modSubsystemIdLooksRejected(id)) {
      state = "rejected";
      notes.push("mod_subsystem must be a namespace-scoped identity value, not pack/universal/generic");
    }
    if (state === "accepted" && args.facet === "organization_group" && customOrganizationGroupNeedsReview(candidate)) {
      state = "review";
      notes.push("custom organization_group requires human review before auto-home");
    }
    if (args.facet === "workflow_role") {
      const parent = id.split("#")[0] ?? "";
      if (!parent || (curated?.parent && curated.parent !== parent)) {
        state = "rejected";
        notes.push("workflow_role parent must equal id prefix before #");
      }
      if (state === "accepted" && parent && args.acceptedWorkflowIds && !args.acceptedWorkflowIds.has(parent)) {
        state = "rejected";
        notes.push("workflow_role parent is not an accepted workflow value");
      }
    }
    if (
      state === "accepted" &&
      (args.facet === "workflow" || args.facet === "used_at") &&
      documentAliasHasRecipeBackedCanonicalDuplicate(id, candidate, allCandidateById)
    ) {
      state = "review";
      notes.push("document-title alias duplicates a recipe-backed station/process id");
    }
    if (state === "accepted" && args.facet === "workflow" && workflowCandidateLooksTooGranular(id, candidate)) {
      state = "review";
      notes.push("guide/quest/advancement workflow title is too granular; prefer a reusable process/station id");
    }
    if (state === "accepted" && args.facet === "used_at" && usedAtCandidateLooksTooGranular(id, candidate)) {
      state = "review";
      notes.push("guide/quest/advancement used_at title is too granular; prefer a reusable station/process id");
    }
    if (state === "accepted" && args.facet === "progression_stage" && progressionCandidateLooksTooGranular(id, candidate)) {
      state = "review";
      notes.push("progression value is too phrase-like; prefer a concise gate/tier/dimension id");
    }
    if (state === "accepted" && candidate && evidence.length === 0 && origin !== "universal_default" && origin !== "previous") {
      state = "review";
      notes.push("accepted non-default values require evidence");
    }
    if (state !== "accepted" && curated?.state === "accepted") {
      args.diagnostics.push({
        severity: "warning",
        facet: args.facet,
        id,
        message: `policy downgraded accepted value to ${state}: ${notes.join("; ")}`,
      });
    }

    decisions.push({
      facet: args.facet,
      id,
      label,
      state,
      origin,
      confidence: round(confidence),
      evidence: evidence.slice(0, CANDIDATE_EXAMPLE_LIMIT),
      seed_items: sortedLimited(seedItems, CANDIDATE_EXAMPLE_LIMIT),
      ...(aliases.length ? { aliases } : {}),
      ...(description ? { description } : {}),
      ...(rationale ? { rationale } : {}),
      ...(examples.length ? { examples } : {}),
      ...(args.facet === "workflow_role" ? { parent: curated?.parent ?? base.parent ?? id.split("#")[0]! } : {}),
      ...(defaultOrganizationGroup ? { default_organization_group: defaultOrganizationGroup } : {}),
      ...(relatedActivity.length ? { related_activity: relatedActivity } : {}),
      policy_notes: notes,
    });
  }
  return decisions.sort((a, b) => stateRank(a.state) - stateRank(b.state) || a.id.localeCompare(b.id));
}

function allowsLowSupportAcceptedValue(
  facet: VocabularyFacetId,
  candidate: PackVocabularyCandidate,
  curated: CuratedValue | undefined,
): boolean {
  if (facet !== "progression_stage") return false;
  if (curated?.state !== "accepted") return false;
  if (candidate.evidence.length === 0) return false;
  return (curated.confidence ?? candidate.confidence) >= 0.6;
}

function customOrganizationGroupNeedsReview(
  candidate: PackVocabularyCandidate | undefined,
): boolean {
  return !candidate || (candidate.origin !== "previous" && candidate.origin !== "universal_default");
}

function summarizeReviewDecisions(
  decisions: Record<string, VocabularyDecision[]>,
): Record<string, VocabularyReviewSummary> {
  const out: Record<string, VocabularyReviewSummary> = {};
  for (const [facet, values] of Object.entries(decisions)) {
    const summary: VocabularyReviewSummary = {
      accepted: 0,
      review: 0,
      rejected: 0,
      total: values.length,
    };
    for (const value of values) {
      summary[value.state] += 1;
    }
    out[facet] = summary;
  }
  return out;
}

function toReviewDecisions(
  decisions: Record<string, VocabularyDecision[]>,
): Record<string, VocabularyReviewDecision[]> {
  const out: Record<string, VocabularyReviewDecision[]> = {};
  for (const [facet, values] of Object.entries(decisions)) {
    out[facet] = values.map(toReviewDecision);
  }
  return out;
}

function toReviewDecision(value: VocabularyDecision): VocabularyReviewDecision {
  return {
    facet: value.facet,
    id: value.id,
    label: value.label,
    state: value.state,
    ...(value.description ? { description: value.description } : {}),
    ...(value.rationale ? { rationale: value.rationale } : {}),
    ...(value.examples?.length ? { examples: value.examples } : {}),
    ...(value.aliases?.length ? { aliases: value.aliases } : {}),
    ...(value.parent ? { parent: value.parent } : {}),
    ...(value.default_organization_group ? { default_organization_group: value.default_organization_group } : {}),
    ...(value.related_activity?.length ? { related_activity: value.related_activity } : {}),
    ...(value.policy_notes.length ? { policy_notes: value.policy_notes } : {}),
    ...(value.state === "review" ? {
      human_review: {
        decision: "pending",
        approved_id: value.id,
        approved_label: value.label,
        notes: "",
      },
    } : {}),
  };
}

const WORKFLOW_PROCESS_TOKENS = [
  "alloy",
  "alloying",
  "automation",
  "beekeeping",
  "brewing",
  "casting",
  "charging",
  "compacting",
  "cooking",
  "crushing",
  "cutting",
  "drying",
  "farming",
  "fishing",
  "forging",
  "glassworking",
  "hammering",
  "heating",
  "irrigation",
  "jarring",
  "knapping",
  "leatherworking",
  "milling",
  "mixing",
  "panning",
  "papermaking",
  "polishing",
  "pressing",
  "prospecting",
  "rolling",
  "scraping",
  "sewing",
  "smelting",
  "smoking",
  "stomping",
  "tapping",
  "weaving",
  "welding",
  "winemaking",
  "woodworking",
];

const USED_AT_STATION_TOKENS = [
  "altar",
  "altars",
  "anvil",
  "barrel",
  "barrels",
  "bellows",
  "blast_furnace",
  "bloomery",
  "cellar",
  "cellars",
  "composter",
  "crucible",
  "dryer",
  "drying",
  "energizer",
  "firepit",
  "forge",
  "furnace",
  "grill",
  "greenhouse",
  "kiln",
  "kilns",
  "knapping",
  "loom",
  "machine",
  "mixer",
  "oven",
  "ovens",
  "pot",
  "press",
  "pump",
  "quern",
  "scribing_table",
  "sewing_table",
  "sluice",
  "sluices",
  "station",
  "table",
  "vat",
];

function workflowCandidateLooksTooGranular(
  id: string,
  candidate: PackVocabularyCandidate | undefined,
): boolean {
  if (!candidate || candidate.evidence.length === 0) return false;
  if (candidate.evidence.some((evidence) => evidence.kind === "recipe_type")) return false;
  if (!candidate.evidence.every((evidence) =>
    evidence.kind === "guide_page" || evidence.kind === "quest_node" || evidence.kind === "advancement"
  )) {
    return false;
  }
  const path = valueIdPath(id);
  const parts = path.split("/").filter(Boolean);
  if (parts.length >= 3) return true;
  if (parts.length >= 2 && !documentWorkflowPathHasProcessSignal(id, candidate)) return true;
  return /\b(using|setting|addressing|processing|generating|controlling|placing|moving|routing|displaying|automating|assembling|advanced)\b/.test(
    parts.join(" "),
  );
}

function documentAliasHasRecipeBackedCanonicalDuplicate(
  id: string,
  candidate: PackVocabularyCandidate | undefined,
  candidateById: ReadonlyMap<string, PackVocabularyCandidate>,
): boolean {
  if (!candidate || candidate.evidence.length === 0) return false;
  if (candidate.evidence.some((evidence) => evidence.kind === "recipe_type")) return false;
  if (!candidate.evidence.every((evidence) =>
    evidence.kind === "guide_page" || evidence.kind === "quest_node" || evidence.kind === "advancement"
  )) {
    return false;
  }
  const duplicateId = slashToUnderscoreValueId(id);
  if (!duplicateId || duplicateId === id) return false;
  const duplicate = candidateById.get(duplicateId);
  return !!duplicate?.evidence.some((evidence) => evidence.kind === "recipe_type");
}

function slashToUnderscoreValueId(id: string): string | null {
  const base = id.includes("#") ? id.slice(0, id.indexOf("#")) : id;
  const split = splitResourceLocation(base);
  if (!split) return null;
  const path = valueIdPath(base);
  if (!path.includes("/")) return null;
  const canonicalPath = path.replace(/\//g, "_");
  if (split.namespace !== "pack") return `${split.namespace}:${canonicalPath}`;

  const originalSlash = split.path.indexOf("/");
  if (originalSlash < 0) return null;
  return `pack:${split.path.slice(0, originalSlash)}/${canonicalPath}`;
}

function usedAtCandidateLooksTooGranular(
  id: string,
  candidate: PackVocabularyCandidate | undefined,
): boolean {
  if (!candidate || candidate.evidence.length === 0) return false;
  if (candidate.evidence.some((evidence) => evidence.kind === "recipe_type")) return false;
  if (!candidate.evidence.every((evidence) =>
    evidence.kind === "guide_page" || evidence.kind === "quest_node" || evidence.kind === "advancement"
  )) {
    return false;
  }

  const path = valueIdPath(id);
  const parts = path.split("/").filter(Boolean);
  const flatParts = path.split(/[\/_]+/).filter(Boolean);
  if (parts.length >= 3) return true;
  if (parts.length >= 2 && !documentUsedAtPathHasStationSignal(id, candidate)) return true;
  if (flatParts.includes("block") && flatParts.includes("mod")) return true;
  if (flatParts.includes("trim")) return true;
  const phraseTokens = new Set([
    "advanced",
    "automating",
    "building",
    "creating",
    "displaying",
    "fluids",
    "items",
    "making",
    "modes",
    "moving",
    "multi",
    "processing",
    "routing",
    "setting",
    "the",
    "through",
    "tips",
    "using",
  ]);
  return flatParts.some((part) => phraseTokens.has(part));
}

function documentWorkflowPathHasProcessSignal(
  id: string,
  candidate: PackVocabularyCandidate | undefined,
): boolean {
  const haystack = candidateVocabularyHaystack(id, candidate);
  return WORKFLOW_PROCESS_TOKENS.some((token) => haystack.includes(token));
}

function documentUsedAtPathHasStationSignal(
  id: string,
  candidate: PackVocabularyCandidate | undefined,
): boolean {
  const haystack = candidateVocabularyHaystack(id, candidate);
  return USED_AT_STATION_TOKENS.some((token) => haystack.includes(token));
}

function candidateVocabularyHaystack(
  id: string,
  candidate: PackVocabularyCandidate | undefined,
): string {
  return [
    valueIdPath(id),
    candidate?.label,
    candidate?.description,
    ...(candidate?.aliases ?? []),
    ...(candidate?.semantic_evidence ?? []).slice(0, 8).map((entry) => entry.text),
  ]
    .filter((value): value is string => typeof value === "string" && value.length > 0)
    .map((value) => tokenPath(value)?.replace(/\//g, "_") ?? "")
    .join(" ");
}

function progressionCandidateLooksTooGranular(
  id: string,
  candidate: PackVocabularyCandidate | undefined,
): boolean {
  if (!candidate || candidate.evidence.length === 0) return false;
  const path = valueIdPath(id);
  const parts = path.split("/").filter(Boolean);
  if (parts.length >= 3) return true;
  if (parts.includes("and")) return true;
  if (!candidate.evidence.every((evidence) =>
    evidence.kind === "guide_page" || evidence.kind === "quest_node" || evidence.kind === "advancement"
  )) {
    return false;
  }
  const flatParts = path.split(/[\/_]+/).filter(Boolean);
  const phraseTokens = new Set([
    "a",
    "advanced",
    "after",
    "before",
    "crafting",
    "first",
    "getting",
    "how",
    "intro",
    "making",
    "processing",
    "quest",
    "step",
    "the",
    "to",
    "using",
    "welcome",
  ]);
  return flatParts.some((part) => phraseTokens.has(part));
}

function valueIdPath(id: string): string {
  const base = id.includes("#") ? id.slice(0, id.indexOf("#")) : id;
  const split = splitResourceLocation(base);
  if (split?.namespace === "pack") {
    const slash = split.path.indexOf("/");
    return slash >= 0 ? split.path.slice(slash + 1) : split.path;
  }
  return split?.path ?? base;
}

function decisionsToAcceptedVocabulary(
  decisions: Record<string, VocabularyDecision[]>,
): PackFacetVocabulary["facets"] {
  const facets: PackFacetVocabulary["facets"] = {};
  for (const [facet, values] of Object.entries(decisions)) {
    const accepted = values.filter((value) => value.state === "accepted");
    if (accepted.length === 0) continue;
    facets[facet] = { values: {} };
    for (const value of accepted) {
      const entry: VocabularyValue = {
        label: value.label,
        origin: value.origin,
        state: "accepted",
        confidence: value.confidence,
        ...(value.aliases?.length ? { aliases: value.aliases } : {}),
        ...(value.description ? { description: value.description } : {}),
        ...(value.evidence.length ? { evidence: value.evidence } : {}),
        ...(value.seed_items.length ? { seed_items: value.seed_items } : {}),
        ...(value.parent ? { parent: value.parent } : {}),
        ...(value.default_organization_group ? { default_organization_group: value.default_organization_group } : {}),
        ...(value.related_activity?.length ? { related_activity: value.related_activity } : {}),
      };
      facets[facet]!.values[value.id] = entry;
    }
  }
  return facets;
}

function curatedToCandidate(facet: VocabularyFacetId, curated: CuratedValue): PackVocabularyCandidate {
  return {
    facet,
    id: curated.id,
    label: curated.label ?? labelFromId(curated.id),
    origin: "stage3_proposed",
    suggested_state: curated.state ?? "review",
    confidence: curated.confidence ?? 0.5,
    support: 0,
    evidence: curated.evidence ?? [],
    semantic_evidence: [],
    seed_items: curated.seed_items ?? [],
    aliases: curated.aliases ?? [],
    ...(curated.description ? { description: curated.description } : {}),
    ...(curated.parent ? { parent: curated.parent } : {}),
    ...(curated.default_organization_group ? { default_organization_group: curated.default_organization_group } : {}),
    ...(curated.related_activity ? { related_activity: curated.related_activity } : {}),
    reasons: ["model-synthesized value not present as a context record id"],
  };
}
function normalizeScopedResourceId(
  id: string,
  packId: string,
  fallback: "namespace" | "pack",
): string | null {
  const split = splitResourceLocation(id);
  if (!split) {
    const token = tokenPath(id);
    return token ? `pack:${packId}/${token}` : null;
  }
  const path = tokenPath(split.path);
  if (!path) return null;
  if (fallback === "pack") return `pack:${packId}/${path}`;
  return `${split.namespace}:${path}`;
}
