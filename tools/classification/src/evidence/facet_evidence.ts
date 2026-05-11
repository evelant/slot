import { existsSync, readdirSync, readFileSync, statSync } from "node:fs";
import { basename, join, relative, resolve } from "node:path";
import type { ItemExtractRecord, SemanticTextEvidence } from "../extract/record.ts";
import { cleanSemanticText, dedupeSemanticText, splitSemanticText } from "../extract/semantic_text.ts";
import type { RuntimeExportSummary } from "../llm/runtime_subsystems.ts";
import type { InputManifestV2, InputManifestMod } from "../input/manifest.ts";
import { resolveModsFolder, scanModsFolder } from "../scan/mods_folder.ts";
import { ZipArchive } from "../scan/zip.ts";

export const FACET_EVIDENCE_SCHEMA_VERSION = 1;
export const FACET_EVIDENCE_KIND = "slot-pack-facet-evidence";

const DEFAULT_EXAMPLE_LIMIT = 32;
const RUNTIME_ITEM_LIMIT = 96;
const TEXT_SNIPPET_LIMIT = 2_000;
const SEMANTIC_TEXT_LIMIT = 128;

export type FacetEvidenceKind =
  | "runtime_item"
  | "recipe_type"
  | "recipe_role_summary"
  | "recipe_id_family"
  | "item_tag"
  | "block_tag"
  | "guide_page"
  | "quest_node"
  | "advancement"
  | "kubejs_tooltip"
  | "stack_group"
  | "mod_metadata"
  | "existing_vocab";

export type FacetEvidenceSeverity = "info" | "warning" | "error";

export interface FacetEvidenceArtifact {
  schema_version: typeof FACET_EVIDENCE_SCHEMA_VERSION;
  kind: typeof FACET_EVIDENCE_KIND;
  pack_id: string;
  generated_by: string;
  generated_at: string;
  source: FacetEvidenceSource;
  records: FacetEvidenceRecord[];
  diagnostics: FacetEvidenceDiagnostic[];
}

export interface FacetEvidenceSource {
  runtime_items: string;
  runtime_summary?: string;
  mods_path?: string;
  loader?: string;
  minecraft_version?: string;
  item_count?: number;
}

export interface FacetEvidenceRecord {
  kind: FacetEvidenceKind;
  id: string;
  label?: string;
  namespace?: string;
  source: string;
  confidence: number;
  count?: number;
  item_refs?: string[];
  recipe_refs?: string[];
  examples?: string[];
  tags?: string[];
  direct_tags?: string[];
  components?: string[];
  model_parents?: string[];
  creative_tabs?: string[];
  recipe_roles?: RuntimeItemRecipeRoles;
  role?: "input" | "output";
  recipe_type?: string;
  input_count?: number;
  output_count?: number;
  path_prefix?: string;
  direct_membership_known?: boolean;
  file?: string;
  mod_id?: string;
  version?: string;
  loader?: string;
  description?: string;
  icon?: string;
  chapter?: string;
  page_type?: string;
  title?: string;
  dependencies?: string[];
  rewards?: string[];
  semantic_text?: SemanticTextEvidence[];
  metadata?: Record<string, unknown>;
}

export interface RuntimeItemRecipeRoles {
  in_degree: number;
  out_degree: number;
  ingredient_types: Record<string, number>;
  output_types: Record<string, number>;
  ingredient_examples: string[];
  output_examples: string[];
}

export interface FacetEvidenceDiagnostic {
  adapter: string;
  severity: FacetEvidenceSeverity;
  source: string;
  message: string;
  count?: number;
}

export interface BuildFacetEvidenceOptions {
  packId: string;
  generatedBy: string;
  generatedAt?: string;
  runtimeItemsPath: string;
  runtimeSummaryPath?: string;
  modsPath?: string;
  records: readonly ItemExtractRecord[];
  summary?: RuntimeExportSummary | null;
  externalRecords?: readonly FacetEvidenceRecord[];
  diagnostics?: readonly FacetEvidenceDiagnostic[];
}

export interface CollectExternalEvidenceOptions {
  modsPath: string;
  generatedBy: string;
  bundledModIds?: ReadonlySet<string>;
}

export interface ExternalEvidenceResult {
  records: FacetEvidenceRecord[];
  diagnostics: FacetEvidenceDiagnostic[];
  manifest?: InputManifestV2;
}

interface RecipeTypeAccumulator {
  id: string;
  namespace?: string;
  summaryCount: number;
  inputCount: number;
  outputCount: number;
  itemRefs: Set<string>;
  recipeRefs: Set<string>;
}

interface RecipeFamilyAccumulator {
  id: string;
  namespace?: string;
  pathPrefix: string;
  count: number;
  itemRefs: Set<string>;
  recipeRefs: Set<string>;
}

interface TagAccumulator {
  id: string;
  namespace?: string;
  itemRefs: Set<string>;
  examples: Set<string>;
  directRefs: Set<string>;
  summaryCount?: number;
}

export function buildFacetEvidenceArtifact(options: BuildFacetEvidenceOptions): FacetEvidenceArtifact {
  const records = [...options.records].sort((a, b) => a.id.localeCompare(b.id));
  const evidence: FacetEvidenceRecord[] = [];
  evidence.push(...runtimeItemEvidence(records));
  evidence.push(...recipeTypeEvidence(records, options.summary ?? undefined));
  evidence.push(...recipeRoleSummaryEvidence(records));
  evidence.push(...recipeFamilyEvidence(records));
  evidence.push(...itemTagEvidence(records, options.summary ?? undefined));
  evidence.push(...blockTagEvidence(options.summary ?? undefined));
  evidence.push(...(options.externalRecords ?? []));

  return {
    schema_version: FACET_EVIDENCE_SCHEMA_VERSION,
    kind: FACET_EVIDENCE_KIND,
    pack_id: options.packId,
    generated_by: options.generatedBy,
    generated_at: options.generatedAt ?? new Date().toISOString(),
    source: {
      runtime_items: options.runtimeItemsPath,
      ...(options.runtimeSummaryPath ? { runtime_summary: options.runtimeSummaryPath } : {}),
      ...(options.modsPath ? { mods_path: options.modsPath } : {}),
      ...(options.summary?.loader ? { loader: options.summary.loader } : {}),
      ...(options.summary?.minecraft_version ? { minecraft_version: options.summary.minecraft_version } : {}),
      item_count: options.summary?.item_count ?? records.length,
    },
    records: sortEvidenceRecords(evidence),
    diagnostics: sortDiagnostics(options.diagnostics ?? []),
  };
}

export function collectExternalFacetEvidence(options: CollectExternalEvidenceOptions): ExternalEvidenceResult {
  const diagnostics: FacetEvidenceDiagnostic[] = [];
  let manifest: InputManifestV2;
  let resolved: ReturnType<typeof resolveModsFolder>;
  try {
    resolved = resolveModsFolder(options.modsPath);
    manifest = scanModsFolder({
      requestedPath: options.modsPath,
      generatedBy: options.generatedBy,
      bundledModIds: options.bundledModIds,
    });
  } catch (err) {
    return {
      records: [],
      diagnostics: [{
        adapter: "mods-folder",
        severity: "error",
        source: options.modsPath,
        message: err instanceof Error ? err.message : String(err),
      }],
    };
  }

  const records: FacetEvidenceRecord[] = [];
  records.push(...modMetadataEvidence(manifest.mods));

  const jarPaths = [...new Set(manifest.mods.map((mod) => mod.path))].sort();
  const instanceLang = resolved.minecraftRoot ? readInstanceLang(resolved.minecraftRoot) : new Map<string, string>();
  let advancementCount = 0;
  let guideCount = 0;
  let questCount = 0;
  let overlayCount = 0;
  let langDerivedCount = 0;
  let kubeJsTooltipCount = 0;
  let stackGroupCount = 0;
  for (const jarPath of jarPaths) {
    try {
      const zip = ZipArchive.open(jarPath);
      const jarLang = readJarLang(zip);
      const descriptions = readJarModDescriptions(zip);
      for (const record of records) {
        if (record.kind !== "mod_metadata" || record.file !== basename(jarPath) || !record.mod_id) continue;
        const description = descriptions.get(record.mod_id);
        if (description) {
          record.description = description;
          record.semantic_text = semanticTextEntries("mod-description", description, `mod.${record.mod_id}.description`);
        }
      }
      const jarRecords = collectJarDocumentEvidence(zip, jarPath, jarLang);
      advancementCount += jarRecords.filter((record) => record.kind === "advancement").length;
      guideCount += jarRecords.filter((record) => record.kind === "guide_page").length;
      questCount += jarRecords.filter((record) => record.kind === "quest_node").length;
      records.push(...jarRecords);
      const langRecords = collectLangDerivedEvidence(jarLang, `jar:${basename(jarPath)}!assets/*/lang/en_us.json`);
      langDerivedCount += langRecords.length;
      records.push(...langRecords);
    } catch (err) {
      diagnostics.push({
        adapter: "jar-documents",
        severity: "warning",
        source: jarPath,
        message: err instanceof Error ? err.message : String(err),
      });
    }
  }

  if (resolved.minecraftRoot) {
    const overlayRecords = collectInstanceOverlayEvidence(resolved.minecraftRoot, instanceLang);
    overlayCount += overlayRecords.records.length;
    advancementCount += overlayRecords.records.filter((record) => record.kind === "advancement").length;
    guideCount += overlayRecords.records.filter((record) => record.kind === "guide_page").length;
    questCount += overlayRecords.records.filter((record) => record.kind === "quest_node").length;
    stackGroupCount += overlayRecords.records.filter((record) => record.kind === "stack_group").length;
    records.push(...overlayRecords.records);
    diagnostics.push(...overlayRecords.diagnostics);

    const externalQuests = collectQuestFiles(resolved.minecraftRoot, instanceLang);
    questCount += externalQuests.records.length;
    records.push(...externalQuests.records);
    diagnostics.push(...externalQuests.diagnostics);

    const langRecords = collectLangDerivedEvidence(instanceLang, `file:${resolved.minecraftRoot}/lang-overlays`);
    langDerivedCount += langRecords.length;
    records.push(...langRecords);

    const tooltipRecords = collectKubeJsTooltipEvidence(resolved.minecraftRoot, instanceLang);
    kubeJsTooltipCount += tooltipRecords.records.length;
    records.push(...tooltipRecords.records);
    diagnostics.push(...tooltipRecords.diagnostics);
  }

  diagnostics.push(...adapterCountDiagnostics("advancement", options.modsPath, advancementCount));
  diagnostics.push(...adapterCountDiagnostics("guide_page", options.modsPath, guideCount));
  diagnostics.push(...adapterCountDiagnostics("quest_node", options.modsPath, questCount));
  diagnostics.push(...adapterCountDiagnostics("semantic_overlay", options.modsPath, overlayCount));
  diagnostics.push(...adapterCountDiagnostics("lang_derived_semantics", options.modsPath, langDerivedCount));
  diagnostics.push(...adapterCountDiagnostics("kubejs_tooltip", options.modsPath, kubeJsTooltipCount));
  diagnostics.push(...adapterCountDiagnostics("stack_group", options.modsPath, stackGroupCount));

  return {
    records: sortEvidenceRecords(records),
    diagnostics: sortDiagnostics(diagnostics),
    manifest,
  };
}

function runtimeItemEvidence(records: readonly ItemExtractRecord[]): FacetEvidenceRecord[] {
  return records.map((record) => {
    const componentData = record.component_data ?? {};
    return {
      kind: "runtime_item",
      id: record.id,
      ...(record.display_name ? { label: record.display_name } : {}),
      namespace: record.namespace,
      source: "runtime-items",
      confidence: 1,
      item_refs: [record.id],
      ...(record.minecraft_tags.length > 0 ? { tags: sortedLimited(record.minecraft_tags, RUNTIME_ITEM_LIMIT) } : {}),
      ...(record.minecraft_tags_direct.length > 0 ? { direct_tags: sortedLimited(record.minecraft_tags_direct, RUNTIME_ITEM_LIMIT) } : {}),
      ...(Object.keys(componentData).length > 0 ? { components: sortedLimited(Object.keys(componentData), RUNTIME_ITEM_LIMIT) } : {}),
      ...(record.model_parents.length > 0 ? { model_parents: sortedLimited(record.model_parents, RUNTIME_ITEM_LIMIT) } : {}),
      ...(record.creative_tabs.length > 0 ? { creative_tabs: sortedLimited(record.creative_tabs, RUNTIME_ITEM_LIMIT) } : {}),
      ...(record.semantic_text && record.semantic_text.length > 0
        ? { semantic_text: limitedSemanticText(record.semantic_text) }
        : {}),
      recipe_roles: {
        in_degree: record.recipe_role.in_degree,
        out_degree: record.recipe_role.out_degree,
        ingredient_types: sortedRecord(record.recipe_role.ingredient_of_counts),
        output_types: sortedRecord(record.recipe_role.output_of_counts),
        ingredient_examples: sortedLimited(record.recipe_role.ingredient_of, DEFAULT_EXAMPLE_LIMIT),
        output_examples: sortedLimited(record.recipe_role.output_of, DEFAULT_EXAMPLE_LIMIT),
      },
    };
  });
}

function recipeTypeEvidence(
  records: readonly ItemExtractRecord[],
  summary: RuntimeExportSummary | undefined,
): FacetEvidenceRecord[] {
  const acc = new Map<string, RecipeTypeAccumulator>();
  for (const [type, count] of Object.entries(summary?.recipe_type_counts ?? {})) {
    ensureRecipeType(acc, type).summaryCount += count;
  }
  for (const record of records) {
    for (const [type, count] of Object.entries(record.recipe_role.ingredient_of_counts)) {
      const entry = ensureRecipeType(acc, type);
      entry.inputCount += count;
      entry.itemRefs.add(record.id);
      for (const recipe of record.recipe_role.ingredient_of) entry.recipeRefs.add(recipe);
    }
    for (const [type, count] of Object.entries(record.recipe_role.output_of_counts)) {
      const entry = ensureRecipeType(acc, type);
      entry.outputCount += count;
      entry.itemRefs.add(record.id);
      for (const recipe of record.recipe_role.output_of) entry.recipeRefs.add(recipe);
    }
  }

  return [...acc.values()]
    .filter((entry) => entry.summaryCount + entry.inputCount + entry.outputCount > 0)
    .map((entry) => ({
      kind: "recipe_type",
      id: entry.id,
      label: labelFromId(entry.id),
      ...(entry.namespace ? { namespace: entry.namespace } : {}),
      source: entry.summaryCount > 0 ? "runtime-summary" : "runtime-items",
      confidence: 0.85,
      count: entry.summaryCount > 0 ? entry.summaryCount : entry.inputCount + entry.outputCount,
      input_count: entry.inputCount,
      output_count: entry.outputCount,
      item_refs: sortedLimited(entry.itemRefs, DEFAULT_EXAMPLE_LIMIT),
      recipe_refs: sortedLimited(entry.recipeRefs, DEFAULT_EXAMPLE_LIMIT),
    }));
}

function recipeRoleSummaryEvidence(records: readonly ItemExtractRecord[]): FacetEvidenceRecord[] {
  const out: FacetEvidenceRecord[] = [];
  for (const record of records) {
    for (const [type, count] of Object.entries(record.recipe_role.ingredient_of_counts).sort()) {
      out.push({
        kind: "recipe_role_summary",
        id: `${record.id}|input|${type}`,
        label: `${record.display_name} input in ${labelFromId(type)}`,
        namespace: record.namespace,
        source: "runtime-items",
        confidence: 0.8,
        count,
        item_refs: [record.id],
        recipe_refs: sortedLimited(record.recipe_role.ingredient_of, DEFAULT_EXAMPLE_LIMIT),
        role: "input",
        recipe_type: type,
        ...(record.semantic_text?.length ? { semantic_text: limitedSemanticText(record.semantic_text) } : {}),
      });
    }
    for (const [type, count] of Object.entries(record.recipe_role.output_of_counts).sort()) {
      out.push({
        kind: "recipe_role_summary",
        id: `${record.id}|output|${type}`,
        label: `${record.display_name} output from ${labelFromId(type)}`,
        namespace: record.namespace,
        source: "runtime-items",
        confidence: 0.8,
        count,
        item_refs: [record.id],
        recipe_refs: sortedLimited(record.recipe_role.output_of, DEFAULT_EXAMPLE_LIMIT),
        role: "output",
        recipe_type: type,
        ...(record.semantic_text?.length ? { semantic_text: limitedSemanticText(record.semantic_text) } : {}),
      });
    }
  }
  return out;
}

function recipeFamilyEvidence(records: readonly ItemExtractRecord[]): FacetEvidenceRecord[] {
  const acc = new Map<string, RecipeFamilyAccumulator>();
  for (const record of records) {
    for (const recipe of [...record.recipe_role.ingredient_of, ...record.recipe_role.output_of]) {
      const family = recipeFamilyId(recipe);
      if (!family) continue;
      let entry = acc.get(family.id);
      if (!entry) {
        entry = {
          id: family.id,
          namespace: family.namespace,
          pathPrefix: family.pathPrefix,
          count: 0,
          itemRefs: new Set(),
          recipeRefs: new Set(),
        };
        acc.set(family.id, entry);
      }
      entry.count += 1;
      entry.itemRefs.add(record.id);
      entry.recipeRefs.add(recipe);
    }
  }
  return [...acc.values()].map((entry) => ({
    kind: "recipe_id_family",
    id: entry.id,
    label: labelFromId(entry.pathPrefix),
    ...(entry.namespace ? { namespace: entry.namespace } : {}),
    source: "runtime-items",
    confidence: 0.7,
    count: entry.count,
    path_prefix: entry.pathPrefix,
    item_refs: sortedLimited(entry.itemRefs, DEFAULT_EXAMPLE_LIMIT),
    recipe_refs: sortedLimited(entry.recipeRefs, DEFAULT_EXAMPLE_LIMIT),
  }));
}

function itemTagEvidence(
  records: readonly ItemExtractRecord[],
  summary: RuntimeExportSummary | undefined,
): FacetEvidenceRecord[] {
  const acc = new Map<string, TagAccumulator>();
  for (const [tag, members] of Object.entries(summary?.item_tag_members ?? {})) {
    const entry = ensureTag(acc, tag);
    entry.summaryCount = members.length;
    for (const member of members) {
      entry.itemRefs.add(member);
      entry.examples.add(member);
    }
  }
  for (const record of records) {
    for (const tag of record.minecraft_tags) {
      const entry = ensureTag(acc, tag);
      entry.itemRefs.add(record.id);
      entry.examples.add(record.id);
    }
    for (const tag of record.minecraft_tags_direct) {
      const entry = ensureTag(acc, tag);
      entry.itemRefs.add(record.id);
      entry.directRefs.add(record.id);
      entry.examples.add(record.id);
    }
  }
  return [...acc.values()].map((entry) => ({
    kind: "item_tag",
    id: entry.id,
    label: labelFromId(entry.id),
    ...(entry.namespace ? { namespace: entry.namespace } : {}),
    source: "runtime-summary",
    confidence: entry.directRefs.size > 0 ? 0.85 : 0.75,
    count: entry.summaryCount ?? entry.itemRefs.size,
    item_refs: sortedLimited(entry.itemRefs, DEFAULT_EXAMPLE_LIMIT),
    examples: sortedLimited(entry.examples, DEFAULT_EXAMPLE_LIMIT),
    direct_membership_known: Boolean(summary?.direct_item_tags_available || entry.directRefs.size > 0),
  }));
}

function blockTagEvidence(summary: RuntimeExportSummary | undefined): FacetEvidenceRecord[] {
  return Object.entries(summary?.block_tag_members ?? {}).map(([tag, members]) => ({
    kind: "block_tag",
    id: tag,
    label: labelFromId(tag),
    namespace: namespaceOf(tag),
    source: "runtime-summary",
    confidence: 0.75,
    count: members.length,
    item_refs: sortedLimited(members, DEFAULT_EXAMPLE_LIMIT),
    examples: sortedLimited(members, DEFAULT_EXAMPLE_LIMIT),
  }));
}

function modMetadataEvidence(mods: readonly InputManifestMod[]): FacetEvidenceRecord[] {
  return mods.map((mod) => ({
    kind: "mod_metadata",
    id: mod.id,
    label: mod.display_name ?? labelFromId(mod.id),
    namespace: mod.namespaces.includes(mod.id) ? mod.id : mod.namespaces[0] ?? mod.id,
    source: "mods-folder-scan",
    confidence: 0.6,
    count: mod.item_candidate_count,
    file: mod.file_name,
    mod_id: mod.id,
    ...(mod.version ? { version: mod.version } : {}),
    loader: mod.loader,
    examples: sortedLimited(mod.namespaces, DEFAULT_EXAMPLE_LIMIT),
    metadata: {
      status: mod.status,
      resource_counts: mod.resource_counts,
      diagnostics: mod.diagnostics,
      platform_ids: mod.platform_ids ?? {},
    },
  }));
}

function collectJarDocumentEvidence(
  zip: ZipArchive,
  jarPath: string,
  lang: ReadonlyMap<string, string>,
): FacetEvidenceRecord[] {
  const records: FacetEvidenceRecord[] = [];
  for (const name of zip.entryNames().sort()) {
    if (!name.endsWith(".json")) continue;
    const json = zip.readJson<unknown>(name);
    if (!json) continue;
    const source = `jar:${basename(jarPath)}!${name}`;
    const advancement = advancementRecord(name, json, source, lang);
    if (advancement) records.push(advancement);
    const guide = guidePageRecord(name, json, source, lang);
    if (guide) records.push(guide);
    const quest = questRecord(name, json, source, lang);
    if (quest) records.push(quest);
  }
  return records;
}

function advancementRecord(
  name: string,
  json: unknown,
  source: string,
  lang: ReadonlyMap<string, string>,
): FacetEvidenceRecord | null {
  const match = name.match(/^data\/([^/]+)\/advancements?\/(.+)\.json$/);
  if (!match || !isRecord(json)) return null;
  const display = isRecord(json.display) ? json.display : {};
  const title = readTextValue(display.title, lang);
  const description = readTextValue(display.description, lang);
  const icon = readIcon(display.icon);
  const itemRefs = collectResourceRefs(json, new Set(["item", "items", "id", "name"]));
  if (icon) itemRefs.add(icon);
  const semanticText = [
    ...semanticTextEntries("advancement-title", title, "title"),
    ...semanticTextEntries("advancement-description", description, "description"),
  ];
  return {
    kind: "advancement",
    id: `${match[1]}:${match[2]}`,
    ...(title ? { label: title, title } : { label: labelFromId(match[2]!) }),
    namespace: match[1],
    source,
    confidence: 0.65,
    item_refs: sortedLimited(itemRefs, DEFAULT_EXAMPLE_LIMIT),
    ...(description ? { description: clip(description) } : {}),
    ...(icon ? { icon } : {}),
    ...(semanticText.length > 0 ? { semantic_text: semanticText } : {}),
  };
}

function guidePageRecord(
  name: string,
  json: unknown,
  source: string,
  lang: ReadonlyMap<string, string>,
): FacetEvidenceRecord | null {
  const match = name.match(/^(data|assets)\/([^/]+)\/(?:patchouli_books|books|guides)\/(.+)\.json$/);
  if (!match || !isRecord(json)) return null;
  const path = match[3]!;
  if (hasNonEnglishLocaleSegment(path)) return null;
  const label = readTextValue(json.name, lang) ?? readTextValue(json.title, lang) ?? readTextValue(json.entry, lang) ?? labelFromId(path);
  const pageType = readTextValue(json.type, lang);
  const pageCount = Array.isArray(json.pages) ? json.pages.length : undefined;
  const recipeRefs = collectResourceRefs(json, new Set(["recipe", "recipes", "recipe_id"]));
  const itemRefs = collectAllResourceLocations(json);
  for (const recipe of recipeRefs) itemRefs.delete(recipe);
  const semanticText = collectSemanticTextFromJson(json, "guide-page", lang);
  return {
    kind: "guide_page",
    id: `${match[2]}:${path.replace(/\.json$/i, "")}`,
    label,
    namespace: match[2],
    source,
    confidence: 0.7,
    ...(pageCount !== undefined ? { count: pageCount } : {}),
    item_refs: sortedLimited(itemRefs, DEFAULT_EXAMPLE_LIMIT),
    recipe_refs: sortedLimited(recipeRefs, DEFAULT_EXAMPLE_LIMIT),
    ...(pageType ? { page_type: pageType } : {}),
    ...(readTextValue(json.category, lang) ? { chapter: readTextValue(json.category, lang) } : {}),
    ...(semanticText.length > 0 ? { semantic_text: semanticText } : {}),
  };
}

function questRecord(
  name: string,
  json: unknown,
  source: string,
  lang: ReadonlyMap<string, string>,
): FacetEvidenceRecord | null {
  if (!/(^|\/)(ftbquests|quests)(\/|$)/.test(name) || /\/recipes?\//.test(name) || !isRecord(json) || !isQuestLikeJson(name, json)) {
    return null;
  }
  const match = name.match(/^(?:data|assets)\/([^/]+)\/(.+)\.json$/);
  const namespace = match?.[1] ?? "pack";
  const path = match?.[2] ?? name.replace(/\.json$/i, "");
  const label = readTextValue(json.title, lang) ?? readTextValue(json.name, lang) ?? readTextValue(json.subtitle, lang) ?? labelFromId(path);
  const description = readTextValue(json.description, lang) ?? readTextValue(json.text, lang);
  const itemRefs = collectAllResourceLocations(json);
  const recipeRefs = collectResourceRefs(json, new Set(["recipe", "recipes", "recipe_id"]));
  for (const recipe of recipeRefs) itemRefs.delete(recipe);
  const semanticText = collectSemanticTextFromJson(json, "quest", lang);
  return {
    kind: "quest_node",
    id: `${namespace}:${path.replace(/\.json$/i, "")}`,
    label,
    namespace,
    source,
    confidence: 0.7,
    item_refs: sortedLimited(itemRefs, DEFAULT_EXAMPLE_LIMIT),
    recipe_refs: sortedLimited(recipeRefs, DEFAULT_EXAMPLE_LIMIT),
    ...(description ? { description: clip(description) } : {}),
    ...(readIcon(json.icon) ? { icon: readIcon(json.icon) } : {}),
    dependencies: sortedLimited(collectResourceRefs(json, new Set(["dependency", "dependencies", "requires"])), DEFAULT_EXAMPLE_LIMIT),
    rewards: sortedLimited(collectResourceRefs(json, new Set(["reward", "rewards"])), DEFAULT_EXAMPLE_LIMIT),
    ...(semanticText.length > 0 ? { semantic_text: semanticText } : {}),
  };
}

function collectQuestFiles(
  minecraftRoot: string,
  lang: ReadonlyMap<string, string>,
): {
  records: FacetEvidenceRecord[];
  diagnostics: FacetEvidenceDiagnostic[];
} {
  const questRoot = join(minecraftRoot, "config", "ftbquests", "quests");
  if (!existsSync(questRoot)) return { records: [], diagnostics: [] };
  const records: FacetEvidenceRecord[] = [];
  const diagnostics: FacetEvidenceDiagnostic[] = [];
  for (const path of walkFiles(questRoot)) {
    const rel = relative(questRoot, path).replaceAll("\\", "/");
    if (path.endsWith(".snbt")) {
      try {
        records.push(snbtQuestRecord(rel, readFileSync(path, "utf8"), lang, `file:${path}`));
      } catch (err) {
        diagnostics.push({
          adapter: "quest_node",
          severity: "warning",
          source: path,
          message: err instanceof Error ? err.message : String(err),
        });
      }
      continue;
    }
    if (!path.endsWith(".json")) continue;
    let parsed: unknown;
    try {
      parsed = JSON.parse(readFileSync(path, "utf8"));
    } catch (err) {
      diagnostics.push({
        adapter: "quest_node",
        severity: "warning",
        source: path,
        message: err instanceof Error ? err.message : String(err),
      });
      continue;
    }
    const record = questRecord(`pack/ftbquests/${rel}`, parsed, `file:${path}`, lang);
    if (record) records.push(record);
  }
  return { records, diagnostics };
}

function collectInstanceOverlayEvidence(
  minecraftRoot: string,
  lang: ReadonlyMap<string, string>,
): {
  records: FacetEvidenceRecord[];
  diagnostics: FacetEvidenceDiagnostic[];
} {
  const roots = [
    join(minecraftRoot, "kubejs", "data"),
    join(minecraftRoot, "kubejs", "assets"),
  ];
  const records: FacetEvidenceRecord[] = [];
  const diagnostics: FacetEvidenceDiagnostic[] = [];
  for (const root of roots) {
    if (!existsSync(root)) continue;
    for (const path of walkFiles(root)) {
      if (!path.endsWith(".json")) continue;
      const rel = relative(root, path).replaceAll("\\", "/");
      let parsed: unknown;
      try {
        parsed = JSON.parse(readFileSync(path, "utf8"));
      } catch (err) {
        diagnostics.push({
          adapter: "semantic_overlay",
          severity: "warning",
          source: path,
          message: err instanceof Error ? err.message : String(err),
        });
        continue;
      }
      const logical = root.endsWith("data")
        ? `data/${rel}`
        : `assets/${rel}`;
      const source = `file:${path}`;
      const advancement = advancementRecord(logical, parsed, source, lang);
      if (advancement) {
        records.push(advancement);
        continue;
      }
      const guide = guidePageRecord(logical, parsed, source, lang);
      if (guide) {
        records.push(guide);
        continue;
      }
      const quest = questRecord(logical, parsed, source, lang);
      if (quest) {
        records.push(quest);
        continue;
      }
      const stackGroup = stackGroupRecord(logical, parsed, source, lang);
      if (stackGroup) {
        records.push(stackGroup);
        continue;
      }
    }
  }
  return { records, diagnostics };
}

function stackGroupRecord(
  name: string,
  json: unknown,
  source: string,
  lang: ReadonlyMap<string, string>,
): FacetEvidenceRecord | null {
  const match = name.match(/^assets\/([^/]+)\/stack_groups\/(.+)\.json$/);
  if (!match || !isRecord(json)) return null;
  const declaredId = readString(json.id);
  const id = declaredId && looksLikeResourceLocation(declaredId)
    ? declaredId
    : `${match[1]}:${match[2]}`;
  const label = readTextValue(json.name, lang) ?? labelFromId(id);
  const contents = Array.isArray(json.contents) ? json.contents : [];
  const exclusions = Array.isArray(json.exclusions) ? json.exclusions : [];
  const itemRefs = new Set<string>();
  const tags = new Set<string>();
  for (const value of [...contents, ...exclusions]) {
    if (typeof value !== "string") continue;
    const trimmed = value.trim();
    if (trimmed.startsWith("#") && looksLikeResourceLocation(trimmed.slice(1))) {
      tags.add(trimmed.slice(1));
    } else if (looksLikeResourceLocation(trimmed)) {
      itemRefs.add(trimmed);
    }
  }
  return {
    kind: "stack_group",
    id,
    label,
    namespace: namespaceOf(id),
    source,
    confidence: 0.75,
    count: contents.length,
    item_refs: sortedLimited(itemRefs, DEFAULT_EXAMPLE_LIMIT),
    tags: sortedLimited(tags, DEFAULT_EXAMPLE_LIMIT),
    semantic_text: semanticTextEntries("stack-group-name", label, readString(json.name)),
    metadata: {
      adapter: "emixx-stack-group",
    },
  };
}

function snbtQuestRecord(
  rel: string,
  text: string,
  lang: ReadonlyMap<string, string>,
  source: string,
): FacetEvidenceRecord {
  const semanticText = collectSemanticTextFromSnbt(text, lang);
  const itemRefs = collectResourceLocationsFromText(text);
  const recipeRefs = collectRecipeRefsFromText(text);
  for (const recipe of recipeRefs) itemRefs.delete(recipe);
  const label = semanticText[0]?.text ?? labelFromId(rel.replace(/\.snbt$/i, ""));
  return {
    kind: "quest_node",
    id: `pack:ftbquests/${rel.replace(/\.snbt$/i, "")}`,
    label: clip(label),
    namespace: "pack",
    source,
    confidence: 0.65,
    item_refs: sortedLimited(itemRefs, DEFAULT_EXAMPLE_LIMIT),
    recipe_refs: sortedLimited(recipeRefs, DEFAULT_EXAMPLE_LIMIT),
    ...(semanticText.length > 0 ? { semantic_text: semanticText } : {}),
    metadata: {
      adapter: "ftbquests-snbt",
    },
  };
}

function readJarModDescriptions(zip: ZipArchive): Map<string, string> {
  const out = new Map<string, string>();
  for (const path of ["META-INF/mods.toml", "META-INF/neoforge.mods.toml"]) {
    const text = zip.readText(path);
    if (!text) continue;
    try {
      const parsed = Bun.TOML.parse(text);
      if (!isRecord(parsed) || !Array.isArray(parsed.mods)) continue;
      for (const mod of parsed.mods) {
        if (!isRecord(mod)) continue;
        const id = readString(mod.modId);
        const description = readString(mod.description);
        if (id && description) out.set(id, clip(description));
      }
    } catch {
      continue;
    }
  }
  const fabric = zip.readJson<unknown>("fabric.mod.json");
  if (isRecord(fabric)) {
    const id = readString(fabric.id);
    const description = readString(fabric.description);
    if (id && description) out.set(id, clip(description));
  }
  return out;
}

function readJarLang(zip: ZipArchive): Map<string, string> {
  const out = new Map<string, string>();
  for (const name of zip.entryNames().sort()) {
    if (!/^assets\/[^/]+\/lang\/en_us\.json$/.test(name)) continue;
    mergeLangJson(out, zip.readJson<unknown>(name));
  }
  return out;
}

function readInstanceLang(minecraftRoot: string): Map<string, string> {
  const out = new Map<string, string>();
  const roots = [
    join(minecraftRoot, "kubejs", "assets"),
    join(minecraftRoot, "resourcepacks"),
  ];
  for (const root of roots) {
    if (!existsSync(root)) continue;
    for (const path of walkFiles(root)) {
      if (path.endsWith(".zip")) {
        try {
          const zip = ZipArchive.open(path);
          for (const name of zip.entryNames().sort()) {
            if (/^assets\/[^/]+\/lang\/en_us\.json$/.test(name)) {
              mergeLangJson(out, zip.readJson<unknown>(name));
            }
          }
        } catch {
          continue;
        }
        continue;
      }
      if (!/\/assets\/[^/]+\/lang\/en_us\.json$/.test(path.replaceAll("\\", "/"))
        && !/\/lang\/en_us\.json$/.test(path.replaceAll("\\", "/"))) {
        continue;
      }
      try {
        mergeLangJson(out, JSON.parse(readFileSync(path, "utf8")));
      } catch {
        continue;
      }
    }
  }
  return out;
}

function mergeLangJson(out: Map<string, string>, value: unknown): void {
  if (!isRecord(value)) return;
  for (const [key, entry] of Object.entries(value)) {
    if (typeof entry !== "string") continue;
    const text = cleanSemanticText(entry);
    if (text) out.set(key, text);
  }
}

function collectLangDerivedEvidence(
  lang: ReadonlyMap<string, string>,
  source: string,
): FacetEvidenceRecord[] {
  const records: FacetEvidenceRecord[] = [];
  const ponder = new Map<string, {
    id: string;
    namespace: string;
    label?: string;
    semanticText: SemanticTextEvidence[];
  }>();
  const recipeCategory = new Map<string, FacetEvidenceRecord>();

  for (const [key, text] of lang.entries()) {
    const cleaned = cleanSemanticText(text);
    if (!cleaned || shouldSkipSemanticText(cleaned)) continue;

    const ponderKey = ponderRecordKey(key);
    if (ponderKey) {
      const group = ponder.get(ponderKey.id) ?? {
        id: ponderKey.id,
        namespace: ponderKey.namespace,
        semanticText: [],
      };
      if (ponderKey.isLabel && !group.label) group.label = clip(cleaned);
      for (const entry of splitSemanticText(cleaned)) {
        if (!shouldSkipSemanticText(entry)) {
          group.semanticText.push({ source: "lang-ponder", key, text: clip(entry) });
        }
      }
      ponder.set(ponderKey.id, group);
      continue;
    }

    const recipeType = recipeTypeFromLangKey(key);
    if (recipeType) {
      const existing = recipeCategory.get(recipeType);
      const semantic = semanticTextEntries("recipe-category-lang", cleaned, key);
      if (!existing) {
        recipeCategory.set(recipeType, {
          kind: "recipe_type",
          id: recipeType,
          label: clip(cleaned),
          namespace: namespaceOf(recipeType),
          source,
          confidence: 0.65,
          count: 1,
          recipe_type: recipeType,
          semantic_text: semantic,
        });
      } else if (semantic.length > 0) {
        existing.semantic_text = limitedSemanticText([...(existing.semantic_text ?? []), ...semantic]);
      }
    }
  }

  for (const group of ponder.values()) {
    records.push({
      kind: "guide_page",
      id: group.id,
      label: group.label ?? labelFromId(group.id),
      namespace: group.namespace,
      source,
      confidence: 0.7,
      count: group.semanticText.length,
      page_type: "ponder",
      semantic_text: limitedSemanticText(group.semanticText),
      metadata: {
        adapter: "ponder-lang",
      },
    });
  }
  records.push(...recipeCategory.values());
  return records;
}

function collectKubeJsTooltipEvidence(
  minecraftRoot: string,
  lang: ReadonlyMap<string, string>,
): {
  records: FacetEvidenceRecord[];
  diagnostics: FacetEvidenceDiagnostic[];
} {
  const root = join(minecraftRoot, "kubejs", "client_scripts");
  if (!existsSync(root)) return { records: [], diagnostics: [] };
  const records: FacetEvidenceRecord[] = [];
  const diagnostics: FacetEvidenceDiagnostic[] = [];
  for (const path of walkFiles(root)) {
    if (!/\.(?:js|ts)$/.test(path)) continue;
    const rel = relative(root, path).replaceAll("\\", "/");
    let text: string;
    try {
      text = readFileSync(path, "utf8");
    } catch (err) {
      diagnostics.push({
        adapter: "kubejs_tooltip",
        severity: "warning",
        source: path,
        message: err instanceof Error ? err.message : String(err),
      });
      continue;
    }
    let index = 0;
    for (const block of tooltipScriptBlocks(text)) {
      const targets = tooltipTargets(block);
      if (targets.length === 0) continue;
      const semanticText = tooltipSemanticText(block, lang);
      if (semanticText.length === 0) continue;
      const itemRefs = targets.filter((target) => !target.startsWith("#") && looksLikeResourceLocation(target));
      const tags = targets
        .filter((target) => target.startsWith("#") && looksLikeResourceLocation(target.slice(1)))
        .map((target) => target.slice(1));
      if (itemRefs.length === 0 && tags.length === 0) continue;
      records.push({
        kind: "kubejs_tooltip",
        id: `kubejs:${rel.replace(/\.(?:js|ts)$/i, "")}/${index++}`,
        ...(semanticText[0]?.text ? { label: semanticText[0].text } : {}),
        namespace: "kubejs",
        source: `file:${path}`,
        confidence: 0.8,
        item_refs: sortedLimited(itemRefs, DEFAULT_EXAMPLE_LIMIT),
        tags: sortedLimited(tags, DEFAULT_EXAMPLE_LIMIT),
        semantic_text: semanticText,
        metadata: {
          adapter: "kubejs-client-tooltip",
        },
      });
    }
  }
  return { records, diagnostics };
}

function collectSemanticTextFromJson(
  value: unknown,
  source: string,
  lang: ReadonlyMap<string, string>,
): SemanticTextEvidence[] {
  const out: SemanticTextEvidence[] = [];
  collectSemanticTextFromJsonInto(value, source, lang, [], out);
  return limitedSemanticText(out);
}

function collectSemanticTextFromJsonInto(
  value: unknown,
  source: string,
  lang: ReadonlyMap<string, string>,
  path: readonly string[],
  out: SemanticTextEvidence[],
): void {
  const key = path[path.length - 1];
  if (typeof value === "string") {
    if (!isSemanticJsonKey(key)) return;
    const resolved = resolveSemanticString(value, lang);
    if (!resolved || shouldSkipSemanticText(resolved)) return;
    for (const text of splitSemanticText(resolved)) {
      if (!shouldSkipSemanticText(text)) out.push({ source, key: path.join("."), text: clip(text) });
    }
    return;
  }
  if (Array.isArray(value)) {
    for (let i = 0; i < value.length; i++) {
      collectSemanticTextFromJsonInto(value[i], source, lang, [...path, String(i)], out);
    }
    return;
  }
  if (!isRecord(value)) return;
  for (const [childKey, entry] of Object.entries(value)) {
    collectSemanticTextFromJsonInto(entry, source, lang, [...path, childKey], out);
  }
}

function collectSemanticTextFromSnbt(
  text: string,
  lang: ReadonlyMap<string, string>,
): SemanticTextEvidence[] {
  const out: SemanticTextEvidence[] = [];
  const quoted = text.matchAll(/"((?:\\.|[^"\\])*)"|'((?:\\.|[^'\\])*)'/g);
  for (const match of quoted) {
    const raw = (match[1] ?? match[2] ?? "")
      .replace(/\\"/g, "\"")
      .replace(/\\'/g, "'")
      .replace(/\\\\/g, "\\");
    const resolved = resolveSemanticString(raw, lang);
    if (!resolved || shouldSkipSemanticText(resolved)) continue;
    for (const entry of splitSemanticText(resolved)) {
      if (!shouldSkipSemanticText(entry)) out.push({ source: "quest-snbt", text: clip(entry) });
    }
  }
  for (const match of text.matchAll(/\{([a-z0-9_.-]+(?:\.[a-z0-9_.-]+)+)\}/gi)) {
    const resolved = lang.get(match[1]!);
    if (!resolved || shouldSkipSemanticText(resolved)) continue;
    for (const entry of splitSemanticText(resolved)) {
      if (!shouldSkipSemanticText(entry)) out.push({ source: "quest-snbt-lang", key: match[1], text: clip(entry) });
    }
  }
  return limitedSemanticText(out);
}

function semanticTextEntries(
  source: string,
  text: string | undefined,
  key?: string,
): SemanticTextEvidence[] {
  if (!text) return [];
  return limitedSemanticText(splitSemanticText(text).map((entry) => ({ source, text: clip(entry), ...(key ? { key } : {}) })));
}

function limitedSemanticText(values: Iterable<SemanticTextEvidence>): SemanticTextEvidence[] {
  return dedupeSemanticText(values).slice(0, SEMANTIC_TEXT_LIMIT);
}

function resolveSemanticString(value: string, lang: ReadonlyMap<string, string>): string | undefined {
  const trimmed = value.trim();
  if (!trimmed) return undefined;
  const braced = trimmed.match(/^\{([^}]+)\}$/);
  const key = braced?.[1] ?? trimmed;
  const resolved = lang.get(key);
  if (resolved) return resolved;
  if (looksLikeUnresolvedLangKey(trimmed)) return undefined;
  return trimmed;
}

function shouldSkipSemanticText(value: string): boolean {
  const cleaned = cleanSemanticText(value);
  if (!cleaned || cleaned.length < 2) return true;
  if (looksLikeResourceLocation(cleaned)) return true;
  if (/^[a-z0-9_.-]+(?:\.[a-z0-9_.-]+){2,}$/i.test(cleaned)) return true;
  if (/^[#/a-z0-9_.:-]+$/i.test(cleaned) && !/[ _-]/.test(cleaned)) return true;
  return false;
}

function looksLikeUnresolvedLangKey(value: string): boolean {
  return /^[a-z0-9_.-]+(?:\.[a-z0-9_.-]+){2,}$/i.test(value)
    || /^\{[a-z0-9_.-]+(?:\.[a-z0-9_.-]+)+\}$/i.test(value);
}

function isSemanticJsonKey(key: string | undefined): boolean {
  if (!key) return false;
  return /^(name|title|subtitle|description|desc|summary|text|body|content|tooltip|lore|chapter|category|message|label|quote|note)$/i
    .test(key);
}

function isQuestLikeJson(name: string, json: Record<string, unknown>): boolean {
  if (/\/(?:chapter|chapters|quest|quests|reward_tables|loot_crates)\//.test(name)) return true;
  return ["title", "subtitle", "description", "text", "tasks", "rewards", "dependencies", "x", "y"]
    .some((key) => Object.prototype.hasOwnProperty.call(json, key));
}

function hasNonEnglishLocaleSegment(path: string): boolean {
  const segments = path.split("/");
  const locale = segments.find((segment) => /^[a-z]{2}_[a-z]{2}$/i.test(segment));
  return Boolean(locale && locale.toLowerCase() !== "en_us");
}

function collectResourceLocationsFromText(text: string): Set<string> {
  const out = new Set<string>();
  for (const match of text.matchAll(/\b([a-z0-9_.-]+:[a-z0-9_./-]+)\b/gi)) {
    const value = match[1]!;
    if (looksLikeResourceLocation(value)) out.add(value);
  }
  return out;
}

function collectRecipeRefsFromText(text: string): Set<string> {
  const out = new Set<string>();
  for (const match of text.matchAll(/\b(?:recipe|recipes|recipe_id)\s*[:=]\s*"?([a-z0-9_.-]+:[a-z0-9_./-]+)"?/gi)) {
    const value = match[1]!;
    if (looksLikeResourceLocation(value)) out.add(value);
  }
  return out;
}

function ponderRecordKey(key: string): { id: string; namespace: string; isLabel: boolean } | null {
  const segments = key.split(".");
  const ponderIndex = segments.indexOf("ponder");
  if (ponderIndex <= 0 || ponderIndex >= segments.length - 1) return null;
  const namespace = segments[0]!;
  const terminal = segments[segments.length - 1]!;
  const terminalIsDescriptor = /^(header|title|name|description|text_\d+|text\d+)$/i.test(terminal);
  const rawPath = segments.slice(ponderIndex + 1, terminalIsDescriptor ? -1 : undefined);
  if (rawPath.length === 0) return null;
  const path = rawPath.join("/");
  return {
    id: `${namespace}:ponder/${path}`,
    namespace,
    isLabel: /^(header|title|name)$/i.test(terminal),
  };
}

function recipeTypeFromLangKey(key: string): string | null {
  let match = key.match(/^emi\.category\.([a-z0-9_.-]+)\.([a-z0-9_./.-]+)$/i);
  if (match) return `${match[1]}:${match[2]}`;
  match = key.match(/^([a-z0-9_.-]+)\.jei\.([a-z0-9_./.-]+)$/i);
  if (match) return `${match[1]}:${match[2]}`;
  match = key.match(/^jei\.([a-z0-9_.-]+)\.category\.([a-z0-9_./.-]+)$/i);
  if (match) return `${match[1]}:${match[2]}`;
  match = key.match(/^category\.([a-z0-9_.-]+)\.([a-z0-9_./.-]+)$/i);
  if (match) return `${match[1]}:${match[2]}`;
  return null;
}

function tooltipScriptBlocks(text: string): string[] {
  return text
    .split(/(?=\bevent\.add(?:Advanced)?\s*\()/g)
    .filter((part) => /^\s*event\.add(?:Advanced)?\s*\(/.test(part));
}

function tooltipTargets(block: string): string[] {
  const match = block.match(/^\s*event\.add(?:Advanced)?\s*\(\s*(\[[\s\S]*?\]|["'`][^"'`]+["'`])\s*,/);
  if (!match) return [];
  const raw = match[1]!;
  const targets: string[] = [];
  for (const target of raw.matchAll(/["'`]([^"'`]+)["'`]/g)) {
    const value = target[1]?.trim();
    if (!value || value.includes("${")) continue;
    if (value.startsWith("#") ? looksLikeResourceLocation(value.slice(1)) : looksLikeResourceLocation(value)) {
      targets.push(value);
    }
  }
  return targets;
}

function tooltipSemanticText(
  block: string,
  lang: ReadonlyMap<string, string>,
): SemanticTextEvidence[] {
  const out: SemanticTextEvidence[] = [];
  for (const match of block.matchAll(/Text\.translate\(\s*["'`]([^"'`]+)["'`]/g)) {
    const key = match[1];
    if (!key) continue;
    const resolved = lang.get(key);
    if (!resolved || shouldSkipSemanticText(resolved)) continue;
    for (const text of splitSemanticText(resolved)) {
      if (!shouldSkipSemanticText(text)) {
        out.push({ source: "kubejs-tooltip", key, text: clip(text) });
      }
    }
  }
  return limitedSemanticText(out);
}

function adapterCountDiagnostics(
  adapter: string,
  source: string,
  count: number,
): FacetEvidenceDiagnostic[] {
  return [{
    adapter,
    severity: "info",
    source,
    message: count > 0
      ? `${adapter} records collected`
      : `${adapter} data not found; continuing without this optional evidence source`,
    count,
  }];
}

function ensureRecipeType(acc: Map<string, RecipeTypeAccumulator>, id: string): RecipeTypeAccumulator {
  let entry = acc.get(id);
  if (!entry) {
    entry = {
      id,
      namespace: namespaceOf(id),
      summaryCount: 0,
      inputCount: 0,
      outputCount: 0,
      itemRefs: new Set(),
      recipeRefs: new Set(),
    };
    acc.set(id, entry);
  }
  return entry;
}

function ensureTag(acc: Map<string, TagAccumulator>, id: string): TagAccumulator {
  let entry = acc.get(id);
  if (!entry) {
    entry = {
      id,
      namespace: namespaceOf(id),
      itemRefs: new Set(),
      examples: new Set(),
      directRefs: new Set(),
    };
    acc.set(id, entry);
  }
  return entry;
}

function recipeFamilyId(recipeId: string): { id: string; namespace?: string; pathPrefix: string } | null {
  const split = splitResourceLocation(recipeId);
  if (!split) return null;
  const segments = split.path.split(/[/_-]+/).filter(Boolean);
  if (segments.length === 0) return null;
  const prefix = segments.slice(0, Math.min(2, segments.length)).join("/");
  return {
    id: `${split.namespace}:${prefix}`,
    namespace: split.namespace,
    pathPrefix: prefix,
  };
}

function splitResourceLocation(value: string): { namespace: string; path: string } | null {
  const match = value.match(/^([a-z0-9_.-]+):([a-z0-9_./-]+)$/);
  if (!match) return null;
  return { namespace: match[1]!, path: match[2]! };
}

function namespaceOf(value: string): string | undefined {
  return splitResourceLocation(value)?.namespace;
}

function labelFromId(id: string): string {
  const raw = id.includes(":") ? id.slice(id.indexOf(":") + 1) : id;
  const leaf = raw.includes("/") ? raw.slice(raw.lastIndexOf("/") + 1) : raw;
  return leaf
    .replace(/[#:_./-]+/g, " ")
    .trim()
    .replace(/\b\w/g, (char) => char.toUpperCase());
}

function collectResourceRefs(value: unknown, keys: ReadonlySet<string>): Set<string> {
  const out = new Set<string>();
  collectResourceRefsInto(value, keys, undefined, out);
  return out;
}

function collectResourceRefsInto(
  value: unknown,
  keys: ReadonlySet<string>,
  parentKey: string | undefined,
  out: Set<string>,
): void {
  if (typeof value === "string") {
    if (parentKey && keys.has(parentKey) && looksLikeResourceLocation(value)) out.add(value);
    return;
  }
  if (Array.isArray(value)) {
    for (const entry of value) collectResourceRefsInto(entry, keys, parentKey, out);
    return;
  }
  if (!isRecord(value)) return;
  for (const [key, entry] of Object.entries(value)) {
    collectResourceRefsInto(entry, keys, key, out);
  }
}

function collectAllResourceLocations(value: unknown): Set<string> {
  const out = new Set<string>();
  collectAllResourceLocationsInto(value, out);
  return out;
}

function collectAllResourceLocationsInto(value: unknown, out: Set<string>): void {
  if (typeof value === "string") {
    if (looksLikeResourceLocation(value)) out.add(value);
    return;
  }
  if (Array.isArray(value)) {
    for (const entry of value) collectAllResourceLocationsInto(entry, out);
    return;
  }
  if (!isRecord(value)) return;
  for (const entry of Object.values(value)) collectAllResourceLocationsInto(entry, out);
}

function readTextValue(value: unknown, lang: ReadonlyMap<string, string> = new Map()): string | undefined {
  if (typeof value === "string" && value.trim().length > 0) {
    const resolved = resolveSemanticString(value, lang);
    return resolved ? clip(resolved) : undefined;
  }
  if (!isRecord(value)) return undefined;
  const text = readString(value.text) ?? readString(value.translate);
  if (!text) return undefined;
  const resolved = resolveSemanticString(text, lang);
  return resolved ? clip(resolved) : undefined;
}

function readIcon(value: unknown): string | undefined {
  if (typeof value === "string" && looksLikeResourceLocation(value)) return value;
  if (!isRecord(value)) return undefined;
  const item = readString(value.item) ?? readString(value.id);
  return item && looksLikeResourceLocation(item) ? item : undefined;
}

function looksLikeResourceLocation(value: string): boolean {
  return /^[a-z0-9_.-]+:[a-z0-9_./-]+$/.test(value);
}

function readString(value: unknown): string | undefined {
  return typeof value === "string" && value.trim().length > 0 ? value.trim() : undefined;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function sortedRecord(input: Record<string, number>): Record<string, number> {
  return Object.fromEntries(Object.entries(input).sort(([a], [b]) => a.localeCompare(b)));
}

function sortedLimited(values: Iterable<string>, limit: number): string[] {
  return [...new Set([...values].filter((value) => value.length > 0))]
    .sort()
    .slice(0, limit);
}

function sortEvidenceRecords(records: readonly FacetEvidenceRecord[]): FacetEvidenceRecord[] {
  return [...records].sort((a, b) =>
    a.kind.localeCompare(b.kind) ||
    a.id.localeCompare(b.id) ||
    a.source.localeCompare(b.source)
  );
}

function sortDiagnostics(diagnostics: readonly FacetEvidenceDiagnostic[]): FacetEvidenceDiagnostic[] {
  return [...diagnostics].sort((a, b) =>
    a.adapter.localeCompare(b.adapter) ||
    a.source.localeCompare(b.source) ||
    a.message.localeCompare(b.message)
  );
}

function clip(value: string): string {
  const normalized = value.replace(/\s+/g, " ").trim();
  return normalized.length <= TEXT_SNIPPET_LIMIT
    ? normalized
    : `${normalized.slice(0, TEXT_SNIPPET_LIMIT - 3)}...`;
}

function walkFiles(root: string): string[] {
  if (!existsSync(root)) return [];
  const out: string[] = [];
  for (const name of readdirSync(root).sort()) {
    const path = join(root, name);
    const stat = statSync(path);
    if (stat.isDirectory()) {
      out.push(...walkFiles(path));
    } else if (stat.isFile()) {
      out.push(resolve(path));
    }
  }
  return out;
}
