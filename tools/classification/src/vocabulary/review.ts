import { readFileSync } from "node:fs";
import { validateMultiValue } from "../schema/facets.ts";
import {
  validateVocabularyArtifact,
  type PackFacetVocabulary,
  type VocabularyValue,
} from "../schema/vocabulary.ts";
import type {
  PackFacetVocabularyReview,
  VocabularyHumanReview,
  VocabularyReviewDecision,
} from "./types.ts";
import { isRecord } from "./helpers.ts";

export interface ApplyVocabularyReviewOptions {
  vocabulary: PackFacetVocabulary;
  review: PackFacetVocabularyReview;
  generatedBy?: string;
  generatedAt?: string;
  reviewPath?: string;
}

export interface ApplyVocabularyReviewResult {
  vocabulary: PackFacetVocabulary;
  changes: VocabularyReviewChange[];
  errors: string[];
}

export interface VocabularyReviewChange {
  facet: string;
  id: string;
  action: Exclude<VocabularyHumanReview["decision"], "pending">;
  approved_id?: string;
}

export function readPackFacetVocabularyReviewFile(path: string): PackFacetVocabularyReview {
  const raw = readFileSync(path, "utf8");
  let parsed: unknown;
  try {
    parsed = JSON.parse(raw);
  } catch (err) {
    throw new Error(`invalid vocabulary review JSON: ${(err as Error).message}`);
  }
  if (!isRecord(parsed) || parsed.kind !== "slot-pack-facet-vocabulary-review") {
    throw new Error("vocabulary review kind must be slot-pack-facet-vocabulary-review");
  }
  if (typeof parsed.pack_id !== "string" || !isRecord(parsed.decisions)) {
    throw new Error("vocabulary review must include pack_id and decisions");
  }
  return parsed as unknown as PackFacetVocabularyReview;
}

export function applyVocabularyReviewDecisions(
  options: ApplyVocabularyReviewOptions,
): ApplyVocabularyReviewResult {
  const next = cloneVocabulary(options.vocabulary);
  const changes: VocabularyReviewChange[] = [];
  const errors: string[] = [];

  if (next.pack_id !== options.review.pack_id) {
    errors.push(`review pack_id ${options.review.pack_id} does not match vocabulary pack_id ${next.pack_id}`);
    return { vocabulary: next, changes, errors };
  }

  if (options.generatedBy) next.generated_by = options.generatedBy;
  if (options.generatedAt) next.generated_at = options.generatedAt;
  next.source = {
    ...(next.source ?? {}),
    ...(options.reviewPath ? { human_review: options.reviewPath } : { human_review: true }),
  };

  for (const [facet, decisions] of Object.entries(options.review.decisions)) {
    for (const decision of decisions) {
      const human = decision.human_review;
      if (!human || human.decision === "pending") {
        if (decision.state === "review") {
          next.facets[facet] ??= { values: {} };
          next.facets[facet]!.values[decision.id] ??= vocabularyValueFromReviewDecision(
            decision,
            decision.id,
            decision.label,
            "review",
          );
        }
        continue;
      }
      if (human.decision === "reject") {
        delete next.facets[facet]?.values[decision.id];
        removeEmptyFacet(next, facet);
        changes.push({ facet, id: decision.id, action: "reject" });
        continue;
      }

      const approvedId = normalizedApprovedId(decision, human);
      const approvedLabel = normalizedApprovedLabel(decision, human);
      const issue = validateMultiValue(facet, [approvedId]);
      if (issue) {
        errors.push(`${facet}/${decision.id} ${human.decision} target ${approvedId}: ${issue.reason}`);
        continue;
      }
      next.facets[facet] ??= { values: {} };
      if (approvedId !== decision.id) delete next.facets[facet]!.values[decision.id];
      next.facets[facet]!.values[approvedId] = vocabularyValueFromReviewDecision(
        decision,
        approvedId,
        approvedLabel,
        "accepted",
      );
      changes.push({
        facet,
        id: decision.id,
        action: human.decision,
        ...(approvedId !== decision.id ? { approved_id: approvedId } : {}),
      });
    }
  }

  const validation = validateVocabularyArtifact(next);
  if (!validation.ok) errors.push(...validation.errors);

  return { vocabulary: next, changes, errors };
}

function vocabularyValueFromReviewDecision(
  decision: VocabularyReviewDecision,
  approvedId: string,
  approvedLabel: string,
  state: "accepted" | "review",
): VocabularyValue {
  const relatedActivity = sanitizeRelatedActivityIds(decision.related_activity ?? []);
  const humanNotes = decision.human_review?.notes?.trim();
  const description = decision.description ?? (state === "accepted" ? humanNotes : undefined);
  return {
    label: approvedLabel,
    origin: state === "accepted" ? "manual" : "pack_generated",
    state,
    ...(decision.aliases?.length ? { aliases: decision.aliases } : {}),
    ...(description ? { description } : {}),
    ...(relatedActivity.length ? { related_activity: relatedActivity } : {}),
    ...(decision.default_organization_group ? { default_organization_group: decision.default_organization_group } : {}),
    ...(decision.facet === "workflow_role" ? { parent: decision.parent ?? approvedId.split("#")[0] ?? "" } : {}),
  };
}

function sanitizeRelatedActivityIds(values: readonly string[]): string[] {
  return [...new Set(values.map((value) => value.trim()).filter((value) => {
    if (!value) return false;
    return !validateMultiValue("activity", [value]);
  }))].sort();
}

function normalizedApprovedId(
  decision: VocabularyReviewDecision,
  human: VocabularyHumanReview,
): string {
  return (human.approved_id?.trim() || decision.id).trim();
}

function normalizedApprovedLabel(
  decision: VocabularyReviewDecision,
  human: VocabularyHumanReview,
): string {
  return (human.approved_label?.trim() || decision.label).trim();
}

function removeEmptyFacet(vocabulary: PackFacetVocabulary, facet: string): void {
  if (Object.keys(vocabulary.facets[facet]?.values ?? {}).length === 0) {
    delete vocabulary.facets[facet];
  }
}

function cloneVocabulary(vocabulary: PackFacetVocabulary): PackFacetVocabulary {
  return JSON.parse(JSON.stringify(vocabulary)) as PackFacetVocabulary;
}
