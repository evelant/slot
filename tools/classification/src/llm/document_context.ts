import type { FacetEvidenceArtifact } from "../evidence/facet_evidence.ts";
import type { ItemExtractRecord, SemanticTextEvidence } from "../extract/record.ts";

export type DocumentContextKind = "advancement" | "guide_page";

export interface LlmDocumentContext {
  kind: DocumentContextKind;
  id: string;
  label?: string;
  item_ref_count: number;
  related_item_refs?: readonly string[];
  snippets: readonly {
    source: string;
    key?: string;
    text: string;
  }[];
}

export type DocumentContextByItem = Record<string, readonly LlmDocumentContext[]>;

export interface DocumentContextBuildOptions {
  maxAdvancementItemRefs?: number;
  maxGuideItemRefs?: number;
  maxContextsPerItem?: number;
  maxSnippetsPerContext?: number;
  maxSnippetChars?: number;
}

export interface DocumentContextBuildStats {
  evidence_records_scanned: number;
  eligible_document_records: number;
  context_count: number;
  items_with_context: number;
  skipped_no_runtime_item_refs: number;
  skipped_no_semantic_text: number;
  skipped_broad_documents: number;
  skipped_quest_records: number;
}

const DEFAULT_MAX_ADVANCEMENT_ITEM_REFS = 4;
const DEFAULT_MAX_GUIDE_ITEM_REFS = 8;
const DEFAULT_MAX_CONTEXTS_PER_ITEM = 6;
const DEFAULT_MAX_SNIPPETS_PER_CONTEXT = 4;
const DEFAULT_MAX_SNIPPET_CHARS = 1_200;
const RELATED_ITEM_REF_LIMIT = 8;

interface Candidate {
  score: number;
  order: number;
  context: LlmDocumentContext;
}

export function buildDocumentContextByItem(
  evidence: FacetEvidenceArtifact,
  records: readonly ItemExtractRecord[],
  options: DocumentContextBuildOptions = {},
): { byItem: DocumentContextByItem; stats: DocumentContextBuildStats } {
  const maxAdvancementItemRefs = options.maxAdvancementItemRefs ?? DEFAULT_MAX_ADVANCEMENT_ITEM_REFS;
  const maxGuideItemRefs = options.maxGuideItemRefs ?? DEFAULT_MAX_GUIDE_ITEM_REFS;
  const maxContextsPerItem = options.maxContextsPerItem ?? DEFAULT_MAX_CONTEXTS_PER_ITEM;
  const maxSnippetsPerContext = options.maxSnippetsPerContext ?? DEFAULT_MAX_SNIPPETS_PER_CONTEXT;
  const maxSnippetChars = options.maxSnippetChars ?? DEFAULT_MAX_SNIPPET_CHARS;
  const runtimeIds = new Set(records.map((record) => record.id));
  const byItemCandidates = new Map<string, Candidate[]>();

  const stats: DocumentContextBuildStats = {
    evidence_records_scanned: evidence.records.length,
    eligible_document_records: 0,
    context_count: 0,
    items_with_context: 0,
    skipped_no_runtime_item_refs: 0,
    skipped_no_semantic_text: 0,
    skipped_broad_documents: 0,
    skipped_quest_records: 0,
  };

  let order = 0;
  for (const record of evidence.records) {
    if (record.kind === "quest_node") {
      stats.skipped_quest_records++;
      continue;
    }
    if (record.kind !== "advancement" && record.kind !== "guide_page") continue;

    const snippets = documentSnippets(record.semantic_text, maxSnippetsPerContext, maxSnippetChars);
    if (snippets.length === 0) {
      stats.skipped_no_semantic_text++;
      continue;
    }

    const runtimeItemRefs = runtimeRefs(record.item_refs, runtimeIds);
    if (runtimeItemRefs.length === 0) {
      stats.skipped_no_runtime_item_refs++;
      continue;
    }

    const maxRefs = record.kind === "advancement" ? maxAdvancementItemRefs : maxGuideItemRefs;
    if (runtimeItemRefs.length > maxRefs) {
      stats.skipped_broad_documents++;
      continue;
    }

    stats.eligible_document_records++;
    const kind = record.kind;
    const baseScore = kind === "advancement" ? 200 : 100;
    const score = baseScore + (maxRefs - runtimeItemRefs.length) * 10 + snippets.length;

    for (const itemId of runtimeItemRefs) {
      const related = runtimeItemRefs
        .filter((ref) => ref !== itemId)
        .slice(0, RELATED_ITEM_REF_LIMIT);
      const context: LlmDocumentContext = {
        kind,
        id: record.id,
        ...(record.label ? { label: record.label } : {}),
        item_ref_count: runtimeItemRefs.length,
        ...(related.length > 0 ? { related_item_refs: related } : {}),
        snippets,
      };
      const list = byItemCandidates.get(itemId) ?? [];
      list.push({ score, order: order++, context });
      byItemCandidates.set(itemId, list);
      stats.context_count++;
    }
  }

  const byItem: DocumentContextByItem = {};
  for (const [itemId, candidates] of byItemCandidates) {
    const deduped = dedupeCandidates(candidates)
      .sort((a, b) => b.score - a.score || a.order - b.order || a.context.id.localeCompare(b.context.id))
      .slice(0, maxContextsPerItem)
      .map((candidate) => candidate.context);
    if (deduped.length > 0) byItem[itemId] = deduped;
  }
  stats.items_with_context = Object.keys(byItem).length;
  return { byItem, stats };
}

function runtimeRefs(
  itemRefs: readonly string[] | undefined,
  runtimeIds: ReadonlySet<string>,
): string[] {
  const out: string[] = [];
  const seen = new Set<string>();
  for (const ref of itemRefs ?? []) {
    if (!runtimeIds.has(ref) || seen.has(ref)) continue;
    seen.add(ref);
    out.push(ref);
  }
  return out.sort();
}

function documentSnippets(
  semanticText: readonly SemanticTextEvidence[] | undefined,
  maxSnippets: number,
  maxChars: number,
): LlmDocumentContext["snippets"] {
  const seen = new Set<string>();
  const out: Array<{ source: string; key?: string; text: string }> = [];
  for (const entry of semanticText ?? []) {
    const text = clipSnippet(entry.text, maxChars);
    if (!text || seen.has(text)) continue;
    seen.add(text);
    out.push({
      source: entry.source,
      ...(entry.key ? { key: entry.key } : {}),
      text,
    });
    if (out.length >= maxSnippets) break;
  }
  return out;
}

function clipSnippet(text: string, maxChars: number): string {
  const normalized = text.replace(/\s+/g, " ").trim();
  if (!normalized) return "";
  if (normalized.length <= maxChars) return normalized;
  return `${normalized.slice(0, Math.max(0, maxChars - 3)).trimEnd()}...`;
}

function dedupeCandidates(candidates: readonly Candidate[]): Candidate[] {
  const out: Candidate[] = [];
  const seen = new Set<string>();
  for (const candidate of candidates) {
    const key = `${candidate.context.kind}\u0000${candidate.context.id}`;
    if (seen.has(key)) continue;
    seen.add(key);
    out.push(candidate);
  }
  return out;
}
