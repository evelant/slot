import type { FacetEvidenceRecord } from "../evidence/facet_evidence.ts";
import type { SemanticEvidenceIndex, VocabularySemanticEvidence } from "./types.ts";
import { CANDIDATE_EXAMPLE_LIMIT, GENERIC_TOKENS, RUNTIME_TOOLTIP_REPEAT_BOILERPLATE_THRESHOLD, SEMANTIC_EVIDENCE_LIMIT, SEMANTIC_TEXT_PROMPT_LIMIT } from "./constants.ts";
import { labelFromId, looksLikeResourceLocation, round, sortedLimited, splitResourceLocation, tokenPath } from "./helpers.ts";

export function buildSemanticEvidenceIndex(records: readonly FacetEvidenceRecord[]): SemanticEvidenceIndex {
  const runtimeTooltipBoilerplate = collectRuntimeTooltipBoilerplate(records);
  const runtimeItemIds = new Set(records
    .filter((record) => record.kind === "runtime_item")
    .map((record) => record.id));
  const index: SemanticEvidenceIndex = {
    byItem: new Map(),
    byRuntimeItem: new Map(),
    byRecipeType: new Map(),
    byNamespace: new Map(),
    runtimeItemIds,
    runtimeTooltipBoilerplate,
  };
  for (const record of records) {
    const semantic = semanticEvidenceFromRecord(record, index);
    if (semantic.length === 0) continue;
    for (const item of record.item_refs ?? []) {
      pushSemantic(index.byItem, item, semantic);
      if (record.kind === "runtime_item") {
        pushSemantic(index.byRuntimeItem, item, semantic);
      }
    }
    if (record.recipe_type && record.kind !== "recipe_role_summary") {
      pushSemantic(index.byRecipeType, record.recipe_type, semantic);
    }
    for (const recipe of record.recipe_refs ?? []) {
      const recipeType = recipeTypeFromRecipeRef(recipe);
      if (recipeType && semanticEvidenceMatchesRecipeType(record, recipeType)) {
        pushSemantic(index.byRecipeType, recipeType, semantic);
      }
    }
    if (record.namespace) {
      pushSemantic(index.byNamespace, record.namespace, semantic);
    }
  }
  return index;
}

export function semanticEvidenceForCandidate(
  record: FacetEvidenceRecord,
  index: SemanticEvidenceIndex | undefined,
): VocabularySemanticEvidence[] {
  const out: VocabularySemanticEvidence[] = [...semanticEvidenceFromRecord(record, index)];
  if (index) {
    const recipeType = record.recipe_type ?? (record.kind === "recipe_type" ? record.id : undefined);
    if (recipeType) {
      out.push(...(index.byRecipeType.get(recipeType) ?? []));
    }
    if (canUseRuntimeItemSemanticJoin(record)) {
      for (const item of record.item_refs ?? []) {
        const runtimeEvidence = index.byRuntimeItem.get(item) ?? [];
        out.push(...runtimeEvidence.filter((evidence) =>
          recipeType ? semanticEvidenceEntryMatchesRecipeType(evidence, recipeType) : true
        ));
      }
    } else if (canUseItemSemanticJoin(record)) {
      for (const item of record.item_refs ?? []) {
        out.push(...(index.byItem.get(item) ?? []));
      }
    }
  }
  return limitedSemanticEvidence(out);
}

function semanticEvidenceFromRecord(
  record: FacetEvidenceRecord,
  index?: Pick<SemanticEvidenceIndex, "runtimeTooltipBoilerplate">,
): VocabularySemanticEvidence[] {
  if (record.kind === "recipe_role_summary") return [];
  const out: VocabularySemanticEvidence[] = [];
  for (const entry of record.semantic_text ?? []) {
    const text = clipSemanticPromptText(entry.text.trim());
    if (!text) continue;
    if (isPromptBoilerplateSemanticText(entry.source, text, index?.runtimeTooltipBoilerplate)) continue;
    out.push({
      kind: record.kind,
      id: record.id,
      source: record.source,
      text,
      key: [entry.source, entry.key].filter(Boolean).join(":") || undefined,
      ...(record.label ? { label: record.label } : {}),
      ...(record.item_refs?.length ? { item_ref_count: record.item_refs.length } : {}),
      ...(record.recipe_refs?.length ? { recipe_ref_count: record.recipe_refs.length } : {}),
      ...(record.role ? { role: record.role } : {}),
      ...(record.recipe_type ? { recipe_type: record.recipe_type } : {}),
      ...(record.count !== undefined ? { count: record.count } : {}),
    });
  }
  if (out.length === 0 && record.description) {
    out.push({
      kind: record.kind,
      id: record.id,
      source: record.source,
      text: clipSemanticPromptText(record.description),
      key: "description",
      ...(record.label ? { label: record.label } : {}),
      ...(record.count !== undefined ? { count: record.count } : {}),
    });
  }
  return limitedSemanticEvidence(out);
}

function clipSemanticPromptText(value: string): string {
  const normalized = value.replace(/\s+/g, " ").trim();
  return normalized.length <= SEMANTIC_TEXT_PROMPT_LIMIT
    ? normalized
    : `${normalized.slice(0, SEMANTIC_TEXT_PROMPT_LIMIT - 3)}...`;
}

export function promptSemanticEvidence(evidence: VocabularySemanticEvidence): VocabularySemanticEvidence {
  return {
    kind: evidence.kind,
    id: evidence.id,
    source: promptEvidenceSource(evidence.source),
    ...(evidence.text ? { text: evidence.text } : {}),
    ...(evidence.key ? { key: evidence.key } : {}),
    ...(evidence.label ? { label: evidence.label } : {}),
    ...(evidence.item_ref_count !== undefined ? { item_ref_count: evidence.item_ref_count } : {}),
    ...(evidence.recipe_ref_count !== undefined ? { recipe_ref_count: evidence.recipe_ref_count } : {}),
    ...(evidence.role ? { role: evidence.role } : {}),
    ...(evidence.recipe_type ? { recipe_type: evidence.recipe_type } : {}),
    ...(evidence.count !== undefined ? { count: evidence.count } : {}),
  };
}

function promptEvidenceSource(source: string): string {
  if (source.startsWith("jar:")) {
    const jarSource = source.slice("jar:".length);
    const bang = jarSource.indexOf("!");
    if (bang >= 0) {
      return `jar:${lastPathSegment(jarSource.slice(0, bang))}!${jarSource.slice(bang + 1)}`;
    }
    return `jar:${lastPathSegment(jarSource)}`;
  }
  if (source.startsWith("file:")) {
    const fileSource = source.slice("file:".length).replace(/\\/g, "/");
    const marker = "/minecraft/";
    const markerIndex = fileSource.lastIndexOf(marker);
    if (markerIndex >= 0) {
      return `file:minecraft/${fileSource.slice(markerIndex + marker.length)}`;
    }
    return `file:${tailPath(fileSource, 4)}`;
  }
  return source.length <= 200 ? source : `.../${tailPath(source.replace(/\\/g, "/"), 4)}`;
}

function lastPathSegment(path: string): string {
  return path.replace(/\\/g, "/").split("/").filter(Boolean).at(-1) ?? path;
}

function tailPath(path: string, segments: number): string {
  const parts = path.split("/").filter(Boolean);
  return parts.slice(-segments).join("/");
}

export function runtimeItemRefs(
  values: readonly string[] | undefined,
  index: Pick<SemanticEvidenceIndex, "runtimeItemIds"> | undefined,
): string[] {
  if (!values?.length) return [];
  const out: string[] = [];
  for (const value of values) {
    if (!looksLikePromptItemId(value)) continue;
    if (index && !index.runtimeItemIds.has(value)) continue;
    out.push(value);
  }
  return sortedLimited(out, CANDIDATE_EXAMPLE_LIMIT);
}

function looksLikePromptItemId(value: string): boolean {
  if (!looksLikeResourceLocation(value)) return false;
  if (value.startsWith("patchouli:")) return false;
  if (value.includes("/textures/") || value.endsWith(".png") || value.endsWith(".json")) return false;
  return true;
}

function collectRuntimeTooltipBoilerplate(records: readonly FacetEvidenceRecord[]): Set<string> {
  const out = new Set<string>([
    "supported by:",
    "can be placed vertically",
    "allows mixed vertical-horizontal connections (relative to the placement)",
  ]);
  const counts = new Map<string, number>();

  for (const record of records) {
    if (record.kind === "mod_metadata") {
      if (record.label) out.add(normalizeBoilerplateText(record.label));
      if (record.id) out.add(normalizeBoilerplateText(labelFromId(record.id)));
      continue;
    }
    if (record.kind !== "runtime_item") continue;
    for (const entry of record.semantic_text ?? []) {
      if (!entry.source.startsWith("runtime-tooltip")) continue;
      const normalized = normalizeBoilerplateText(entry.text);
      if (!normalized) continue;
      counts.set(normalized, (counts.get(normalized) ?? 0) + 1);
    }
  }

  for (const [text, count] of counts) {
    if (count < RUNTIME_TOOLTIP_REPEAT_BOILERPLATE_THRESHOLD) continue;
    if (isRepeatedRuntimeTooltipBoilerplate(text)) out.add(text);
  }
  return out;
}

function isPromptBoilerplateSemanticText(
  source: string,
  text: string,
  runtimeTooltipBoilerplate: ReadonlySet<string> | undefined,
): boolean {
  if (!source.startsWith("runtime-tooltip")) return false;
  const normalized = normalizeBoilerplateText(text);
  if (!normalized) return true;
  if (runtimeTooltipBoilerplate?.has(normalized)) return true;
  if (/^⚖\s*.+\s+⇲\s+.+$/u.test(normalized)) return true;
  if (/^hold\s+(shift|\(shift\)|\[w\]|ctrl|alt)\b.*\b(info|information|details|ponder)\b/i.test(normalized)) return true;
  return false;
}

function isRepeatedRuntimeTooltipBoilerplate(text: string): boolean {
  if (/^⚖\s*.+\s+⇲\s+.+$/u.test(text)) return true;
  if (/^hold\s+/i.test(text)) return true;
  if (text.length <= 48 && /^[\p{L}\p{N} '&:+().,-]+$/u.test(text)) return true;
  return false;
}

function normalizeBoilerplateText(text: string): string {
  return text
    .replace(/§[0-9A-FK-OR]/gi, "")
    .replace(/\s+/g, " ")
    .trim()
    .toLowerCase();
}

export function looksLikeProgressionStage(record: FacetEvidenceRecord, label: string): boolean {
  const semantic = (record.semantic_text ?? [])
    .slice(0, 8)
    .map((entry) => entry.text)
    .join(" ");
  const semanticHaystack = `${label} ${semantic}`.toLowerCase();
  const idPath = splitResourceLocation(record.id)?.path.toLowerCase() ?? record.id.toLowerCase();
  const idTail = idPath.split(/[/.]/).filter(Boolean).at(-1) ?? "";
  const labelToken = tokenPath(label) ?? "";
  const isAdvancement = record.kind === "advancement";

  if (/\b(index|tips?|lists?|recipes?|animals?|mobs?|fruits?|crops?|flora|biomes?|damage\s+types?|ores?\s+and\s+minerals|wild\s+animals|wild\s+fruits)\b/.test(semanticHaystack)) {
    return false;
  }
  if (/\b(boots?|horseshoes?|trophies?|minecarts?|boats?|sloops?|cannons?|blocks?|logs?|lumber|chests?|doors?|fences?|fence\s+gates?|bookshelves?|trapdoors?|buttons?|pressure\s+plates?)\b/.test(semanticHaystack)) {
    return false;
  }
  if (isAdvancement && /\b(crafting|recipes?)\b/.test(idPath) && !/\b(rocket|moon|mars|venus|mercury|steel|bronze|anvil|bloomery|blast_furnace|crucible)\b/.test(`${idPath} ${semanticHaystack}`)) {
    return false;
  }

  if (/\b(age|tier|voltage|primitive|steam|electric|lv|mv|hv|ev|iv|luv|zpm|uv|uhv)\b/.test(semanticHaystack)) {
    return true;
  }
  if (/\b(moon|mars|venus|mercury|space|rocket|launch|orbit|proxima|beneath|nether)\b/.test(semanticHaystack)) {
    return true;
  }
  if (/\b(blast\s+furnace|bloomery|mechanical\s+power|primitive\s+alloys?|primitive\s+anvils?|hellforge|ancient\s+altars?|crucible|making\s+steel)\b/.test(semanticHaystack)) {
    return true;
  }
  if (/\b(steel|black\s+steel|red\s+steel|blue\s+steel|wrought\s+iron|bronze|bismuth\s+bronze|black\s+bronze)\b/.test(semanticHaystack)) {
    return true;
  }
  return /\b(tier_[0-9]+_rocket|rocket|moon|mars|venus|mercury|steel|bronze)\b/.test(idTail) ||
    /\b(tier_[0-9]+_rocket)\b/.test(labelToken);
}

export function documentLooksLikeWorkflowOrUseContext(record: FacetEvidenceRecord, label: string): boolean {
  const semantic = (record.semantic_text ?? [])
    .slice(0, 8)
    .map((entry) => entry.text)
    .join(" ");
  const haystack = tokenPath([
    record.id,
    label,
    record.description,
    semantic,
  ].filter((value): value is string => typeof value === "string" && value.length > 0).join(" ")) ?? "";
  if (!haystack) return false;
  if (/\b(ui|config|options?|keybinds?|errors?|recipe_transfer|tooltip|message|subtitle|sound|screen|screenhandler)\b/.test(haystack)) {
    return false;
  }
  return /\b(anvil|barrel|blast|bloomery|boiling|brewing|casting|centrifuge|compressor|compacting|cooking|crucible|crushing|cutting|deployer|deploying|distillery|drying|extruder|filling|forge|furnace|hammering|lathe|loom|machine|macerator|milling|mixer|mixing|oven|polishing|press|pressing|processor|quern|rolling|sawmill|separator|smeltery|smelting|station|washer|workbench)\b/.test(haystack);
}

function canUseItemSemanticJoin(record: FacetEvidenceRecord): boolean {
  return record.kind !== "recipe_type" &&
    record.kind !== "recipe_id_family" &&
    record.kind !== "recipe_role_summary";
}

function canUseRuntimeItemSemanticJoin(record: FacetEvidenceRecord): boolean {
  return record.kind === "recipe_type" || record.kind === "recipe_id_family";
}

function semanticEvidenceMatchesRecipeType(record: FacetEvidenceRecord, recipeType: string): boolean {
  if (record.kind === "recipe_type" && record.id === recipeType) return true;
  if (record.recipe_type === recipeType) return true;
  const haystack = [
    record.id,
    record.label,
    record.title,
    record.description,
    ...(record.semantic_text ?? []).slice(0, 12).map((entry) => entry.text),
  ]
    .filter((value): value is string => typeof value === "string" && value.length > 0)
    .map(tokenPath)
    .join(" ");
  return haystackMatchesRecipeType(haystack, recipeType);
}

function semanticEvidenceEntryMatchesRecipeType(evidence: VocabularySemanticEvidence, recipeType: string): boolean {
  const haystack = [evidence.id, evidence.label, evidence.text]
    .filter((value): value is string => typeof value === "string" && value.length > 0)
    .map(tokenPath)
    .join(" ");
  return haystackMatchesRecipeType(haystack, recipeType);
}

function haystackMatchesRecipeType(haystack: string, recipeType: string): boolean {
  if (!haystack) return false;
  for (const needle of recipeTypeNeedles(recipeType)) {
    if (new RegExp(`(^|[^a-z0-9])${escapeRegExp(needle)}([^a-z0-9]|$)`, "i").test(haystack)) {
      return true;
    }
  }
  return false;
}

function recipeTypeNeedles(recipeType: string): string[] {
  const recipeTypeTail = splitResourceLocation(recipeType)?.path.split("/").filter(Boolean).at(-1);
  if (!recipeTypeTail) return [];
  const normalized = tokenPath(recipeTypeTail);
  if (!normalized) return [];
  const tokens = normalized.split(/[\/_]/).filter((token) => token && !GENERIC_TOKENS.has(token));
  const out = new Set<string>();
  for (const token of tokens) {
    out.add(token);
    if (token.endsWith("ing") && token.length > 5) {
      out.add(undoubleTrailingConsonant(token.slice(0, -3)));
    }
  }
  if (out.has("smelting")) {
    out.add("smelt");
    out.add("melt");
    out.add("melting");
    out.add("molten");
  }
  if (out.has("casting")) {
    out.add("cast");
    out.add("mold");
    out.add("molds");
    out.add("molten");
  }
  return [...out].filter((value) => value && !GENERIC_TOKENS.has(value));
}

function undoubleTrailingConsonant(value: string): string {
  if (value.length < 2) return value;
  const last = value.at(-1);
  if (last && value.at(-2) === last) return value.slice(0, -1);
  return value;
}

function pushSemantic(
  map: Map<string, VocabularySemanticEvidence[]>,
  key: string,
  values: readonly VocabularySemanticEvidence[],
): void {
  if (!key || values.length === 0) return;
  const existing = map.get(key) ?? [];
  map.set(key, limitedSemanticEvidence([...existing, ...values]));
}

function limitedSemanticEvidence(values: readonly VocabularySemanticEvidence[]): VocabularySemanticEvidence[] {
  const seen = new Set<string>();
  const out: VocabularySemanticEvidence[] = [];
  for (const value of values) {
    const text = value.text ?? "";
    const key = `${value.kind}\u0000${value.id}\u0000${value.source}\u0000${value.key ?? ""}\u0000${text}`;
    if (seen.has(key)) continue;
    seen.add(key);
    out.push(value);
    if (out.length >= SEMANTIC_EVIDENCE_LIMIT) break;
  }
  return out;
}

function recipeTypeFromRecipeRef(recipe: string): string | null {
  const split = splitResourceLocation(recipe);
  if (!split) return null;
  const first = split.path.split("/").filter(Boolean)[0];
  return first ? `${split.namespace}:${first}` : null;
}

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}
