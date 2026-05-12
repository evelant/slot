import type { FacetEvidenceRecord } from "../evidence/facet_evidence.ts";
import type { VocabularyEvidenceRef } from "../schema/vocabulary.ts";
import { round } from "./helpers.ts";

export function namespaceAllowed(record: FacetEvidenceRecord, namespaces: ReadonlySet<string>): boolean {
  if (namespaces.size === 0) return true;
  if (record.namespace && namespaces.has(record.namespace)) return true;
  const idNamespace = record.id.split(":")[0];
  if (idNamespace && namespaces.has(idNamespace)) return true;
  return (record.item_refs ?? []).some((item) => namespaces.has(item.split(":")[0] ?? ""));
}

export function evidenceRef(record: FacetEvidenceRecord): VocabularyEvidenceRef {
  return {
    kind: record.kind,
    id: record.id,
    confidence: round(record.confidence),
  };
}
