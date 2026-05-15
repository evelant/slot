import { readFileSync } from "node:fs";
import {
  FACETS,
  validateMultiValue,
  validateSingleValue,
  type FacetDef,
} from "./facets.ts";

export type VocabularyState = "accepted" | "review" | "rejected";
export type VocabularyOrigin =
  | "built_in"
  | "pack_generated"
  | "namespace_generated"
  | "manual"
  | "previous"
  | "stage3_proposed";

export interface VocabularyEvidenceRef {
  kind: string;
  id: string;
  confidence?: number;
}

export interface VocabularyValue {
  label: string;
  aliases?: string[];
  description?: string;
  origin: VocabularyOrigin;
  state: VocabularyState;
  confidence?: number;
  evidence?: VocabularyEvidenceRef[];
  seed_items?: string[];
  related_activity?: string[];
  parent?: string;
  default_organization_group?: string;
}

export interface PackFacetVocabulary {
  schema_version: 1;
  kind: "slot-pack-facet-vocabulary";
  pack_id: string;
  generated_by?: string;
  generated_at?: string;
  source?: Record<string, unknown>;
  facets: Record<string, { values: Record<string, VocabularyValue> }>;
}

export interface VocabularyValidateResult {
  ok: boolean;
  errors: string[];
  vocabulary?: PackFacetVocabulary;
}

const PACK_ID_PATTERN = /^[a-z0-9_.-]+$/;
const STATES = new Set<VocabularyState>(["accepted", "review", "rejected"]);
const ORIGINS = new Set<VocabularyOrigin>([
  "built_in",
  "pack_generated",
  "namespace_generated",
  "manual",
  "previous",
  "stage3_proposed",
]);

export function validateVocabularyArtifact(obj: unknown): VocabularyValidateResult {
  const errors: string[] = [];
  if (!isRecord(obj)) {
    return { ok: false, errors: ["<root> vocabulary must be an object"] };
  }

  if (obj.schema_version !== 1) {
    errors.push("/schema_version must be 1");
  }
  if (obj.kind !== "slot-pack-facet-vocabulary") {
    errors.push("/kind must be slot-pack-facet-vocabulary");
  }
  if (typeof obj.pack_id !== "string" || !PACK_ID_PATTERN.test(obj.pack_id)) {
    errors.push("/pack_id must match [a-z0-9_.-]+");
  }
  if (!isRecord(obj.facets)) {
    errors.push("/facets must be an object");
  } else {
    validateVocabularyFacets(obj.facets, errors);
    validateWorkflowRoleParentReferences(obj.facets, errors);
  }

  return errors.length === 0
    ? { ok: true, errors: [], vocabulary: obj as unknown as PackFacetVocabulary }
    : { ok: false, errors };
}

export function validateVocabularyArtifactFile(path: string): VocabularyValidateResult {
  const raw = readFileSync(path, "utf8");
  let parsed: unknown;
  try {
    parsed = JSON.parse(raw);
  } catch (err) {
    return { ok: false, errors: [`invalid JSON: ${(err as Error).message}`] };
  }
  return validateVocabularyArtifact(parsed);
}

export function validateLayerAgainstVocabulary(
  layer: unknown,
  vocabulary: PackFacetVocabulary,
): string[] {
  if (!isRecord(layer) || !isRecord(layer.entries)) {
    return [];
  }
  const usable = usableValuesByFacet(vocabulary);
  const errors: string[] = [];
  for (const [itemId, itemEntry] of Object.entries(layer.entries)) {
    if (!isRecord(itemEntry) || !isRecord(itemEntry.facets)) {
      continue;
    }
    for (const [facetId, rawEntry] of Object.entries(itemEntry.facets)) {
      const def = FACETS[facetId];
      if (!def?.vocabulary_backed) {
        continue;
      }
      const allowed = usable.get(facetId);
      const markedForReview = isRecord(rawEntry) && rawEntry.vocab_review === true;
      if (!allowed || allowed.size === 0) {
        if (!markedForReview) {
          errors.push(`/entries/${itemId}/facets/${facetId} has no usable vocabulary values`);
        }
        continue;
      }
      for (const value of facetValues(rawEntry)) {
        if (!allowed.has(value) && !markedForReview) {
          errors.push(`/entries/${itemId}/facets/${facetId} value '${value}' is not usable by vocabulary`);
        }
      }
    }
  }
  return errors;
}

function validateVocabularyFacets(
  facets: Record<string, unknown>,
  errors: string[],
): void {
  for (const [facetId, rawFacet] of Object.entries(facets)) {
    const def = FACETS[facetId];
    if (!def) {
      errors.push(`/facets/${facetId} unknown facet`);
      continue;
    }
    if (!def.vocabulary_backed) {
      errors.push(`/facets/${facetId} facet is not vocabulary-backed`);
      continue;
    }
    if (!isRecord(rawFacet) || !isRecord(rawFacet.values)) {
      errors.push(`/facets/${facetId}/values must be an object`);
      continue;
    }
    validateVocabularyValues(facetId, def, rawFacet.values, errors);
  }
}

function validateWorkflowRoleParentReferences(
  facets: Record<string, unknown>,
  errors: string[],
): void {
  const workflowFacet = facets.workflow;
  const workflowRoleFacet = facets.workflow_role;
  if (!isRecord(workflowRoleFacet) || !isRecord(workflowRoleFacet.values)) return;
  const usableWorkflows = new Set<string>();
  if (isRecord(workflowFacet) && isRecord(workflowFacet.values)) {
    for (const [valueId, rawValue] of Object.entries(workflowFacet.values)) {
      if (isRecord(rawValue) && isUsableVocabularyState(rawValue.state)) {
        usableWorkflows.add(valueId);
      }
    }
  }
  for (const [valueId, rawValue] of Object.entries(workflowRoleFacet.values)) {
    if (!isRecord(rawValue) || !isUsableVocabularyState(rawValue.state)) continue;
    const parent = typeof rawValue.parent === "string" ? rawValue.parent : valueId.split("#")[0] ?? "";
    if (!parent || !usableWorkflows.has(parent)) {
      errors.push(`/facets/workflow_role/values/${valueId}/parent references missing usable workflow '${parent}'`);
    }
  }
}

function validateVocabularyValues(
  facetId: string,
  def: FacetDef,
  values: Record<string, unknown>,
  errors: string[],
): void {
  for (const [valueId, rawValue] of Object.entries(values)) {
    const issue = validateVocabularyValueId(facetId, def, valueId);
    if (issue) {
      errors.push(`/facets/${facetId}/values/${valueId} ${issue.reason}`);
    }
    if (!isRecord(rawValue)) {
      errors.push(`/facets/${facetId}/values/${valueId} value entry must be an object`);
      continue;
    }
    if (typeof rawValue.label !== "string" || rawValue.label.trim() === "") {
      errors.push(`/facets/${facetId}/values/${valueId}/label must be a non-empty string`);
    }
    if (typeof rawValue.state !== "string" || !STATES.has(rawValue.state as VocabularyState)) {
      errors.push(`/facets/${facetId}/values/${valueId}/state must be accepted, review, or rejected`);
    }
    if (typeof rawValue.origin !== "string" || !ORIGINS.has(rawValue.origin as VocabularyOrigin)) {
      errors.push(`/facets/${facetId}/values/${valueId}/origin is invalid`);
    }
    if ("confidence" in rawValue && !isConfidence(rawValue.confidence)) {
      errors.push(`/facets/${facetId}/values/${valueId}/confidence must be 0..1`);
    }
    validateStringArray(rawValue.aliases, `/facets/${facetId}/values/${valueId}/aliases`, errors);
    validateStringArray(rawValue.seed_items, `/facets/${facetId}/values/${valueId}/seed_items`, errors);
    validateEvidence(rawValue.evidence, `/facets/${facetId}/values/${valueId}/evidence`, errors);

    if (facetId === "workflow_role") {
      const roleParts = valueId.split("#");
      const parent = roleParts.length === 2 ? roleParts[0] : "";
      if (typeof rawValue.parent !== "string" || rawValue.parent !== parent) {
        errors.push(`/facets/${facetId}/values/${valueId}/parent must equal '${parent}'`);
      }
    } else if ("parent" in rawValue) {
      errors.push(`/facets/${facetId}/values/${valueId}/parent is only valid for workflow_role`);
    }

    if (rawValue.default_organization_group !== undefined) {
      const group = rawValue.default_organization_group;
      const groupIssue = validateMultiValue("organization_group", [group]);
      if (typeof group !== "string" || groupIssue) {
        errors.push(`/facets/${facetId}/values/${valueId}/default_organization_group must be a vocabulary id`);
      }
    }
    if (rawValue.related_activity !== undefined) {
      if (!Array.isArray(rawValue.related_activity)) {
        errors.push(`/facets/${facetId}/values/${valueId}/related_activity must be an array`);
      } else {
        for (const activity of rawValue.related_activity) {
          const activityIssue = validateMultiValue("activity", [activity]);
          if (typeof activity !== "string" || activityIssue) {
            errors.push(`/facets/${facetId}/values/${valueId}/related_activity contains invalid activity id`);
          }
        }
      }
    }
  }

  if (def.kind !== "free_text" && def.kind !== "multi_free_text") {
    errors.push(`/facets/${facetId} vocabulary-backed facets must be free_text or multi_free_text`);
  }
}

function validateVocabularyValueId(
  facetId: string,
  def: FacetDef,
  valueId: string,
): { facet: string; reason: string } | null {
  if (def.kind === "free_text") {
    return validateSingleValue(facetId, valueId);
  }
  return validateMultiValue(facetId, [valueId]);
}

export function isUsableVocabularyState(state: unknown): state is "accepted" | "review" {
  return state === "accepted" || state === "review";
}

function usableValuesByFacet(
  vocabulary: PackFacetVocabulary,
): Map<string, Set<string>> {
  const out = new Map<string, Set<string>>();
  for (const [facetId, facet] of Object.entries(vocabulary.facets ?? {})) {
    const usable = out.get(facetId) ?? new Set<string>();
    for (const [valueId, value] of Object.entries(facet.values ?? {})) {
      if (isUsableVocabularyState(value.state)) {
        usable.add(valueId);
      }
    }
    out.set(facetId, usable);
  }
  return out;
}

function facetValues(rawEntry: unknown): string[] {
  if (!isRecord(rawEntry)) {
    return [];
  }
  if (Array.isArray(rawEntry.values)) {
    return rawEntry.values.filter((value): value is string => typeof value === "string");
  }
  return typeof rawEntry.value === "string" ? [rawEntry.value] : [];
}

function validateEvidence(raw: unknown, path: string, errors: string[]): void {
  if (raw === undefined) {
    return;
  }
  if (!Array.isArray(raw)) {
    errors.push(`${path} must be an array`);
    return;
  }
  for (let i = 0; i < raw.length; i++) {
    const evidence = raw[i];
    if (!isRecord(evidence)) {
      errors.push(`${path}/${i} must be an object`);
      continue;
    }
    if (typeof evidence.kind !== "string" || evidence.kind.trim() === "") {
      errors.push(`${path}/${i}/kind must be a non-empty string`);
    }
    if (typeof evidence.id !== "string" || evidence.id.trim() === "") {
      errors.push(`${path}/${i}/id must be a non-empty string`);
    }
    if ("confidence" in evidence && !isConfidence(evidence.confidence)) {
      errors.push(`${path}/${i}/confidence must be 0..1`);
    }
  }
}

function validateStringArray(raw: unknown, path: string, errors: string[]): void {
  if (raw === undefined) {
    return;
  }
  if (!Array.isArray(raw) || raw.some((value) => typeof value !== "string")) {
    errors.push(`${path} must be an array of strings`);
  }
}

function isConfidence(value: unknown): value is number {
  return typeof value === "number" && Number.isFinite(value) && value >= 0 && value <= 1;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return !!value && typeof value === "object" && !Array.isArray(value);
}
