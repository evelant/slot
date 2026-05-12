import type { LlmClient, QueryOptions } from "../llm/client.ts";
import type {
  FacetEvidenceArtifact,
  FacetEvidenceKind,
} from "../evidence/facet_evidence.ts";
import type {
  PackFacetVocabulary,
  VocabularyEvidenceRef,
  VocabularyOrigin,
  VocabularyState,
} from "../schema/vocabulary.ts";
import { VOCABULARY_BACKED_FACETS } from "../schema/facets.ts";

export type VocabularyFacetId = typeof VOCABULARY_BACKED_FACETS[number];

export interface PackVocabularyCandidate {
  facet: VocabularyFacetId;
  id: string;
  label: string;
  origin: VocabularyOrigin;
  suggested_state: VocabularyState;
  confidence: number;
  support: number;
  evidence: VocabularyEvidenceRef[];
  semantic_evidence: VocabularySemanticEvidence[];
  seed_items: string[];
  aliases: string[];
  description?: string;
  parent?: string;
  default_organization_group?: string;
  related_activity?: string[];
  reasons: string[];
}

export interface VocabularySemanticEvidence {
  kind: FacetEvidenceKind;
  id: string;
  source: string;
  text?: string;
  key?: string;
  label?: string;
  item_ref_count?: number;
  recipe_ref_count?: number;
  role?: string;
  recipe_type?: string;
  count?: number;
}

export interface PackFacetVocabularyReview {
  schema_version: 1;
  kind: "slot-pack-facet-vocabulary-review";
  pack_id: string;
  generated_by: string;
  generated_at: string;
  source: PackFacetVocabulary["source"];
  model?: string;
  filters: {
    facets: string[];
    namespaces: string[];
    min_evidence: number;
    previous_vocabulary?: string;
  };
  candidates: Record<string, PackVocabularyCandidate[]>;
  decisions: Record<string, VocabularyDecision[]>;
  diagnostics: VocabularyDiagnostic[];
  raw_responses: Record<string, string>;
}

export interface VocabularyDecision {
  facet: VocabularyFacetId;
  id: string;
  label: string;
  state: VocabularyState;
  origin: VocabularyOrigin;
  confidence: number;
  evidence: VocabularyEvidenceRef[];
  seed_items: string[];
  aliases?: string[];
  description?: string;
  parent?: string;
  default_organization_group?: string;
  related_activity?: string[];
  policy_notes: string[];
}

export interface VocabularyDiagnostic {
  severity: "info" | "warning" | "error";
  facet?: string;
  id?: string;
  message: string;
}

export interface ExtractVocabularyCandidatesOptions {
  packId: string;
  facets?: readonly string[];
  namespaces?: readonly string[];
  minEvidence: number;
  previousVocabulary?: PackFacetVocabulary;
}

export interface ProposePackFacetVocabularyOptions extends ExtractVocabularyCandidatesOptions {
  evidence: FacetEvidenceArtifact;
  generatedBy: string;
  generatedAt?: string;
  evidencePath: string;
  previousVocabularyPath?: string;
  model?: string;
  client?: LlmClient;
  clientOptions?: Partial<QueryOptions>;
  maxCandidatesPerFacet?: number;
  maxCandidatesPerPrompt?: number;
}

export interface ProposePackFacetVocabularyResult {
  vocabulary: PackFacetVocabulary;
  review: PackFacetVocabularyReview;
  prompts: Record<string, { system: string; user: string }>;
}

export interface CandidateAccumulator extends PackVocabularyCandidate {
  evidenceKeys: Set<string>;
  semanticEvidenceKeys: Set<string>;
  seedItemKeys: Set<string>;
  aliasKeys: Set<string>;
  reasonKeys: Set<string>;
}

export interface CandidateSeed {
  facet: VocabularyFacetId;
  id: string;
  label: string;
  origin: VocabularyOrigin;
  suggestedState: VocabularyState;
  confidence: number;
  support?: number;
  evidence?: VocabularyEvidenceRef[];
  semanticEvidence?: readonly VocabularySemanticEvidence[];
  seedItems?: readonly string[];
  aliases?: readonly string[];
  description?: string;
  parent?: string;
  defaultOrganizationGroup?: string;
  relatedActivity?: readonly string[];
  reason?: string;
}

export interface CuratedValue {
  id: string;
  label?: string;
  state?: VocabularyState;
  description?: string;
  aliases?: string[];
  confidence?: number;
  evidence?: VocabularyEvidenceRef[];
  seed_items?: string[];
  parent?: string;
  default_organization_group?: string;
  related_activity?: string[];
}

export interface SemanticEvidenceIndex {
  byItem: Map<string, VocabularySemanticEvidence[]>;
  byRuntimeItem: Map<string, VocabularySemanticEvidence[]>;
  byRecipeType: Map<string, VocabularySemanticEvidence[]>;
  byNamespace: Map<string, VocabularySemanticEvidence[]>;
  runtimeItemIds: Set<string>;
  runtimeTooltipBoilerplate: Set<string>;
}
