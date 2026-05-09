import { readFileSync } from "node:fs";
import { basename, dirname, join } from "node:path";
import type { ItemExtractRecord } from "../extract/record.ts";
import type { LlmClient, QueryOptions } from "./client.ts";
import {
  parseProposerResponse,
  type SubsystemEntry,
  type SubsystemVocabulary,
} from "./mod_metadata.ts";
import type { SubsystemVocabularyByNamespace } from "./run.ts";

export interface RuntimeExportSummary {
  schema_version?: number;
  format?: string;
  generated_by?: string;
  generated_at?: string;
  pack_id?: string;
  requested_pack_id?: string;
  loader?: string;
  minecraft_version?: string;
  item_count?: number;
  items_file?: string;
  item_tag_membership?: string;
  direct_item_tags_available?: boolean;
  namespace_counts?: Record<string, number>;
  item_tag_members?: Record<string, string[]>;
  block_tag_members?: Record<string, string[]>;
  recipe_type_counts?: Record<string, number>;
}

export interface RuntimeSubsystemContext {
  modNamespace: string;
  packId: string | null;
  loader: string | null;
  minecraftVersion: string | null;
  itemCount: number;
  componentCounts: Record<string, number>;
  tokenClusters: RuntimeCountExamples[];
  itemTagSummaries: RuntimeTagSummary[];
  blockTagSummaries: RuntimeTagSummary[];
  ingredientRecipeTypes: RuntimeCountExamples[];
  outputRecipeTypes: RuntimeCountExamples[];
  recipeIdNamespaces: RuntimeCountExamples[];
  itemSamples: RuntimeItemSample[];
}

export interface RuntimeCountExamples {
  id: string;
  count: number;
  examples: string[];
}

export interface RuntimeTagSummary extends RuntimeCountExamples {
  tag: string;
}

export interface RuntimeItemSample {
  id: string;
  display_name?: string;
  tags?: string[];
  block_tags?: string[];
  ingredient_recipe_types?: string[];
  output_recipe_types?: string[];
  components?: string[];
}

export interface RuntimeSubsystemVocabularyFile {
  schema_version: 1;
  kind: "slot-runtime-subsystem-vocabulary";
  pack_id: string | null;
  generated_by: string;
  generated_at: string;
  model: string;
  source: {
    runtime_items: string;
    runtime_summary?: string;
    loader?: string;
    minecraft_version?: string;
    item_count?: number;
  };
  namespaces: Record<string, RuntimeSubsystemNamespaceResult>;
}

export interface RuntimeSubsystemNamespaceResult {
  modNamespace: string;
  item_count: number;
  evidence: RuntimeSubsystemEvidence;
  vocabulary: SubsystemEntry[];
  raw_response?: string;
}

export interface RuntimeSubsystemEvidence {
  component_counts: Record<string, number>;
  token_clusters: RuntimeCountExamples[];
  item_tags: RuntimeTagSummary[];
  block_tags: RuntimeTagSummary[];
  ingredient_recipe_types: RuntimeCountExamples[];
  output_recipe_types: RuntimeCountExamples[];
  recipe_id_namespaces: RuntimeCountExamples[];
  item_samples: RuntimeItemSample[];
}

export interface RuntimeSubsystemBuildOptions {
  records: readonly ItemExtractRecord[];
  summary?: RuntimeExportSummary | null;
  namespaces?: readonly string[];
  minItems?: number;
}

export interface ProposeRuntimeSubsystemOptions {
  client: LlmClient;
  model?: string;
  clientOptions?: Partial<QueryOptions>;
}

const DEFAULT_MODEL = "deepseek/deepseek-v4-flash";
const TOKEN_CLUSTER_LIMIT = 48;
const TAG_SUMMARY_LIMIT = 28;
const RECIPE_TYPE_LIMIT = 32;
const RECIPE_NAMESPACE_LIMIT = 24;
const ITEM_SAMPLE_LIMIT = 64;
const EXAMPLES_PER_GROUP = 5;

const RUNTIME_PROPOSER_SYSTEM = `You are designing a small canonical vocabulary for the \`mod_subsystem\` facet for one Minecraft mod namespace inside one loaded modpack.

\`mod_subsystem\` labels which gameplay subsystem inside the mod an item itself belongs to. Each value is \`<modnamespace>:<token>\` where token is lowercase snake_case. A later item-classification pass will choose zero or more of these labels per item; your job is to pick a stable vocabulary up front so labels do not drift across items or batches.

Runtime export evidence is pack-specific truth: tags and recipes include KubeJS, datapacks, and compatibility scripts from the loaded game. Recipe ownership can come from another namespace, so do not confuse "this item appears in a recipe owned by subsystem X" with "this item is itself part of subsystem X".

Output strict JSON of this shape (no markdown fences, no commentary):
{
  "vocabulary": [
    {"id": "<modnamespace>:<token>", "rationale": "≤80 chars: which items this label covers"},
    ...
  ]
}

Rules:
- Pick **0 to 8** entries. Use 0 when the namespace has no clear broad functional subsystems.
- Prefer broad functional/mechanical systems a player would organize separately: energy networks, item/fluid transport, processing machines, storage networks, rockets, oxygen systems, automation, farming systems.
- Do NOT propose labels for materials, ores, ingots, dusts, plates, rods, blocks, decorative families, tool families, weapon/armor sets, machine hulls/casings, tiers, or generic crafting ingredients.
- Do NOT propose catch-alls like \`misc\`, \`general\`, \`items\`, \`blocks\`, \`materials\`, \`components\`, \`resources\`, or \`crafting\`.
- Each \`id\` MUST start with the namespace from the user message followed by a colon and a snake_case token.
- Prefer fewer labels over weak labels. Bad labels fragment inventory homes; omission is better than a narrow or low-confidence subsystem.
- Rationales: ≤80 chars, terse. No marketing language.
- Respond with the JSON object only. Start with \`{\` and end with \`}\`.`;

const STOP_TOKENS = new Set([
  "and",
  "block",
  "blocks",
  "brick",
  "bricks",
  "button",
  "chiseled",
  "cobble",
  "cobblestone",
  "colored",
  "cracked",
  "cut",
  "door",
  "fence",
  "gate",
  "glass",
  "item",
  "large",
  "log",
  "plank",
  "planks",
  "plate",
  "pressure",
  "raw",
  "small",
  "slab",
  "smooth",
  "stairs",
  "stone",
  "stripped",
  "trapdoor",
  "wall",
  "wood",
  "wooden",
  "black",
  "blue",
  "brown",
  "cyan",
  "gray",
  "green",
  "grey",
  "light",
  "lime",
  "magenta",
  "orange",
  "pink",
  "purple",
  "red",
  "white",
  "yellow",
  "aluminium",
  "aluminum",
  "bronze",
  "copper",
  "gold",
  "iron",
  "lead",
  "nickel",
  "silver",
  "steel",
  "tin",
  "zinc",
  "dust",
  "gear",
  "ingot",
  "nugget",
  "ore",
  "rod",
  "screw",
  "wire",
]);

const HIGH_SIGNAL_TOKENS = new Set([
  "assembler",
  "battery",
  "batteries",
  "belt",
  "bus",
  "buses",
  "cable",
  "cables",
  "cell",
  "cells",
  "chute",
  "chutes",
  "cogwheel",
  "cogwheels",
  "charger",
  "chargers",
  "compressor",
  "compressors",
  "contraption",
  "contraptions",
  "controller",
  "controllers",
  "conveyor",
  "conveyors",
  "cover",
  "crafter",
  "crane",
  "deployer",
  "duct",
  "engine",
  "export",
  "fan",
  "generator",
  "hatch",
  "import",
  "interface",
  "machine",
  "mechanical",
  "mixer",
  "motor",
  "motors",
  "oxygen",
  "package",
  "packages",
  "pipe",
  "pipes",
  "press",
  "presses",
  "pump",
  "pumps",
  "rail",
  "rocket",
  "schematic",
  "shaft",
  "stress",
  "tank",
  "terminal",
  "terminals",
  "track",
  "train",
  "transformer",
  "turbine",
]);

export function defaultRuntimeSummaryPath(runtimeItemsPath: string): string {
  if (runtimeItemsPath.endsWith(".runtime-items.ndjson")) {
    return runtimeItemsPath.slice(0, -".runtime-items.ndjson".length) + ".runtime-summary.json";
  }
  return join(dirname(runtimeItemsPath), basename(runtimeItemsPath).replace(/\.ndjson$/, ".summary.json"));
}

export function readRuntimeExportRecords(path: string): ItemExtractRecord[] {
  return readFileSync(path, "utf8")
    .split(/\r?\n/)
    .filter((line) => line.trim().length > 0)
    .map((line) => JSON.parse(line) as ItemExtractRecord);
}

export function readRuntimeExportSummary(path: string): RuntimeExportSummary {
  return JSON.parse(readFileSync(path, "utf8")) as RuntimeExportSummary;
}

export function buildRuntimeSubsystemContexts(
  options: RuntimeSubsystemBuildOptions,
): RuntimeSubsystemContext[] {
  const recordsByNamespace = groupRecordsByNamespace(options.records);
  const requested = options.namespaces?.length
    ? new Set(options.namespaces)
    : null;
  const minItems = options.minItems ?? 4;
  const namespaces = [...recordsByNamespace.keys()]
    .filter((namespace) => (requested ? requested.has(namespace) : true))
    .filter((namespace) => requested || recordsByNamespace.get(namespace)!.length >= minItems)
    .sort((a, b) => {
      const countDelta = recordsByNamespace.get(b)!.length - recordsByNamespace.get(a)!.length;
      return countDelta !== 0 ? countDelta : a.localeCompare(b);
    });

  return namespaces.map((namespace) =>
    buildContextForNamespace(namespace, recordsByNamespace.get(namespace)!, options.summary ?? null),
  );
}

export async function proposeRuntimeSubsystems(
  context: RuntimeSubsystemContext,
  options: ProposeRuntimeSubsystemOptions,
): Promise<SubsystemVocabulary> {
  const model = options.model ?? DEFAULT_MODEL;
  const user = buildRuntimeProposerUser(context);
  const queryOptions: QueryOptions = { model, ...options.clientOptions };

  let raw: string;
  if (options.client.querySplit) {
    raw = await options.client.querySplit(RUNTIME_PROPOSER_SYSTEM, user, queryOptions);
  } else {
    raw = await options.client.query(`${RUNTIME_PROPOSER_SYSTEM}\n\n${user}`, queryOptions);
  }

  const vocabulary = parseProposerResponse(raw, context.modNamespace);
  return { modNamespace: context.modNamespace, vocabulary, raw };
}

export function buildRuntimeProposerPrompt(
  context: RuntimeSubsystemContext,
): { system: string; user: string } {
  return {
    system: RUNTIME_PROPOSER_SYSTEM,
    user: buildRuntimeProposerUser(context),
  };
}

export function contextEvidence(
  context: RuntimeSubsystemContext,
): RuntimeSubsystemEvidence {
  return {
    component_counts: context.componentCounts,
    token_clusters: context.tokenClusters,
    item_tags: context.itemTagSummaries,
    block_tags: context.blockTagSummaries,
    ingredient_recipe_types: context.ingredientRecipeTypes,
    output_recipe_types: context.outputRecipeTypes,
    recipe_id_namespaces: context.recipeIdNamespaces,
    item_samples: context.itemSamples,
  };
}

export function loadSubsystemVocabularyFile(path: string): {
  vocabulary?: readonly SubsystemEntry[];
  byNamespace?: SubsystemVocabularyByNamespace;
} {
  const parsed = JSON.parse(readFileSync(path, "utf8")) as {
    modNamespace?: unknown;
    vocabulary?: unknown;
    namespaces?: unknown;
  };

  if (parsed.namespaces && typeof parsed.namespaces === "object") {
    const byNamespace: SubsystemVocabularyByNamespace = {};
    for (const [namespace, value] of Object.entries(parsed.namespaces as Record<string, unknown>)) {
      if (!value || typeof value !== "object") continue;
      const vocabulary = (value as { vocabulary?: unknown }).vocabulary;
      if (!Array.isArray(vocabulary)) continue;
      byNamespace[namespace] = vocabulary.filter(isSubsystemEntry);
    }
    return { byNamespace };
  }

  if (Array.isArray(parsed.vocabulary)) {
    const vocabulary = parsed.vocabulary.filter(isSubsystemEntry);
    if (typeof parsed.modNamespace === "string" && parsed.modNamespace.length > 0) {
      return { byNamespace: { [parsed.modNamespace]: vocabulary } };
    }
    return { vocabulary };
  }

  return {};
}

function buildContextForNamespace(
  namespace: string,
  records: readonly ItemExtractRecord[],
  summary: RuntimeExportSummary | null,
): RuntimeSubsystemContext {
  return {
    modNamespace: namespace,
    packId: summary?.pack_id ?? summary?.requested_pack_id ?? null,
    loader: summary?.loader ?? null,
    minecraftVersion: summary?.minecraft_version ?? null,
    itemCount: records.length,
    componentCounts: componentCounts(records),
    tokenClusters: tokenClusters(records),
    itemTagSummaries: tagSummaries(summary?.item_tag_members, namespace),
    blockTagSummaries: tagSummaries(summary?.block_tag_members, namespace),
    ingredientRecipeTypes: recipeTypeCounts(records, "ingredient"),
    outputRecipeTypes: recipeTypeCounts(records, "output"),
    recipeIdNamespaces: recipeIdNamespaceCounts(records),
    itemSamples: itemSamples(records),
  };
}

function buildRuntimeProposerUser(context: RuntimeSubsystemContext): string {
  const parts: string[] = [];
  parts.push(`Pack id: ${context.packId ?? "(unknown)"}`);
  if (context.loader || context.minecraftVersion) {
    parts.push(`Runtime: ${context.loader ?? "unknown loader"} ${context.minecraftVersion ?? "unknown MC"}`);
  }
  parts.push(`Mod namespace: ${context.modNamespace}`);
  parts.push(`Namespace item count: ${context.itemCount}`);
  parts.push("");
  parts.push("Interpretation notes:");
  parts.push("- This evidence comes from a loaded runtime export, not just jars.");
  parts.push("- KubeJS/datapack recipe and tag edits are already reflected.");
  parts.push("- Recipe ids/types may be owned by another namespace; use them as context, not identity.");
  parts.push("- Propose labels only for broad functional subsystems this namespace's own items belong to.");

  renderRecordMap(parts, "Component signal counts", context.componentCounts);
  renderCountExamples(parts, "Repeated id-token clusters", context.tokenClusters);
  renderTagSummaries(parts, "Resolved item tags involving this namespace", context.itemTagSummaries);
  renderTagSummaries(parts, "Resolved block tags involving this namespace", context.blockTagSummaries);
  renderCountExamples(parts, "Recipe types where namespace items are ingredients", context.ingredientRecipeTypes);
  renderCountExamples(parts, "Recipe types that output namespace items", context.outputRecipeTypes);
  renderCountExamples(parts, "Recipe id namespaces touching namespace items", context.recipeIdNamespaces);
  renderItemSamples(parts, context.itemSamples);

  parts.push("");
  parts.push(
    `Propose 0-8 \`mod_subsystem\` values for ${context.modNamespace}. Respond with the JSON object only.`,
  );
  return parts.join("\n");
}

function renderRecordMap(parts: string[], title: string, counts: Record<string, number>): void {
  const entries = Object.entries(counts)
    .filter(([, count]) => count > 0)
    .sort((a, b) => b[1] - a[1]);
  if (entries.length === 0) return;
  parts.push("");
  parts.push(`${title}:`);
  for (const [id, count] of entries) {
    parts.push(`  - ${id}: ${count}`);
  }
}

function renderCountExamples(
  parts: string[],
  title: string,
  rows: readonly RuntimeCountExamples[],
): void {
  if (rows.length === 0) return;
  parts.push("");
  parts.push(`${title}:`);
  for (const row of rows) {
    const examples = row.examples.length > 0 ? ` — examples: ${row.examples.join(", ")}` : "";
    parts.push(`  - ${row.id}: ${row.count}${examples}`);
  }
}

function renderTagSummaries(
  parts: string[],
  title: string,
  rows: readonly RuntimeTagSummary[],
): void {
  if (rows.length === 0) return;
  parts.push("");
  parts.push(`${title}:`);
  for (const row of rows) {
    const examples = row.examples.length > 0 ? ` — examples: ${row.examples.join(", ")}` : "";
    parts.push(`  - ${row.tag}: ${row.count}${examples}`);
  }
}

function renderItemSamples(parts: string[], samples: readonly RuntimeItemSample[]): void {
  if (samples.length === 0) return;
  parts.push("");
  parts.push(`High-signal item samples (${samples.length}):`);
  for (const sample of samples) {
    const chunks: string[] = [];
    if (sample.display_name) chunks.push(sample.display_name);
    if (sample.tags?.length) chunks.push(`tags=${sample.tags.join(",")}`);
    if (sample.block_tags?.length) chunks.push(`block_tags=${sample.block_tags.join(",")}`);
    if (sample.ingredient_recipe_types?.length) chunks.push(`ingredient_types=${sample.ingredient_recipe_types.join(",")}`);
    if (sample.output_recipe_types?.length) chunks.push(`output_types=${sample.output_recipe_types.join(",")}`);
    if (sample.components?.length) chunks.push(`components=${sample.components.join(",")}`);
    parts.push(`  - ${sample.id}${chunks.length ? ` — ${chunks.join("; ")}` : ""}`);
  }
}

function groupRecordsByNamespace(
  records: readonly ItemExtractRecord[],
): Map<string, ItemExtractRecord[]> {
  const out = new Map<string, ItemExtractRecord[]>();
  for (const record of records) {
    const existing = out.get(record.namespace);
    if (existing) existing.push(record);
    else out.set(record.namespace, [record]);
  }
  return out;
}

function componentCounts(records: readonly ItemExtractRecord[]): Record<string, number> {
  const counts: Record<string, number> = {
    block_items: 0,
    durable_items: 0,
    equippable_items: 0,
    food_items: 0,
    light_emitting_items: 0,
    non_stackable_items: 0,
  };
  for (const record of records) {
    const meta = record.extractor_meta ?? {};
    const components = record.component_data ?? {};
    if (meta.is_block_item === true) counts.block_items = (counts.block_items ?? 0) + 1;
    if (components["minecraft:max_damage"] !== undefined) counts.durable_items = (counts.durable_items ?? 0) + 1;
    if (components["minecraft:equippable"] !== undefined) counts.equippable_items = (counts.equippable_items ?? 0) + 1;
    if (components["minecraft:food"] !== undefined || components["minecraft:use_remainder"] !== undefined) counts.food_items = (counts.food_items ?? 0) + 1;
    if (isPositiveNumber(components["minecraft:light_emission"])) counts.light_emitting_items = (counts.light_emitting_items ?? 0) + 1;
    if (components["minecraft:max_stack_size"] === 1) counts.non_stackable_items = (counts.non_stackable_items ?? 0) + 1;
  }
  return counts;
}

function tokenClusters(records: readonly ItemExtractRecord[]): RuntimeCountExamples[] {
  const buckets = new Map<string, { count: number; examples: Set<string> }>();
  for (const record of records) {
    const seen = new Set(pathTokens(record.path));
    for (const token of seen) {
      const bucket = buckets.get(token) ?? { count: 0, examples: new Set<string>() };
      bucket.count++;
      if (bucket.examples.size < EXAMPLES_PER_GROUP) bucket.examples.add(record.id);
      buckets.set(token, bucket);
    }
  }
  return [...buckets.entries()]
    .filter(([id]) => HIGH_SIGNAL_TOKENS.has(id))
    .map(([id, bucket]) => ({ id, count: bucket.count, examples: [...bucket.examples] }))
    .sort((a, b) => {
      const signalDelta = signalWeight(b.id) - signalWeight(a.id);
      if (signalDelta !== 0) return signalDelta;
      const countDelta = b.count - a.count;
      return countDelta !== 0 ? countDelta : a.id.localeCompare(b.id);
    })
    .slice(0, TOKEN_CLUSTER_LIMIT);
}

function pathTokens(path: string): string[] {
  return path
    .toLowerCase()
    .split(/[:\/_\-.0-9]+/g)
    .map((token) => token.trim())
    .filter((token) => token.length >= 3)
    .filter((token) => !STOP_TOKENS.has(token));
}

function signalWeight(token: string): number {
  return HIGH_SIGNAL_TOKENS.has(token) ? 1 : 0;
}

function tagSummaries(
  membersByTag: Record<string, string[]> | undefined,
  namespace: string,
): RuntimeTagSummary[] {
  if (!membersByTag) return [];
  const prefix = `${namespace}:`;
  const rows: RuntimeTagSummary[] = [];
  for (const [tag, members] of Object.entries(membersByTag)) {
    const examples = members.filter((member) => member.startsWith(prefix)).slice(0, EXAMPLES_PER_GROUP);
    if (examples.length === 0) continue;
    const count = members.reduce((n, member) => n + (member.startsWith(prefix) ? 1 : 0), 0);
    if (!isPotentiallyUsefulTag(tag, namespace)) continue;
    rows.push({ id: tag, tag, count, examples });
  }
  return rows
    .filter((row) => row.count >= 1)
    .sort((a, b) => {
      const ownTagDelta = Number(b.tag.startsWith(`${namespace}:`)) - Number(a.tag.startsWith(`${namespace}:`));
      if (ownTagDelta !== 0) return ownTagDelta;
      const countDelta = b.count - a.count;
      return countDelta !== 0 ? countDelta : a.tag.localeCompare(b.tag);
    })
    .slice(0, TAG_SUMMARY_LIMIT);
}

function isPotentiallyUsefulTag(tag: string, namespace: string): boolean {
  void namespace;
  return pathTokens(tag).some((token) => HIGH_SIGNAL_TOKENS.has(token));
}

function recipeTypeCounts(
  records: readonly ItemExtractRecord[],
  direction: "ingredient" | "output",
): RuntimeCountExamples[] {
  const buckets = new Map<string, { count: number; examples: Set<string> }>();
  for (const record of records) {
    const counts = direction === "ingredient"
      ? record.recipe_role.ingredient_of_counts
      : record.recipe_role.output_of_counts;
    for (const [type, count] of Object.entries(counts)) {
      const bucket = buckets.get(type) ?? { count: 0, examples: new Set<string>() };
      bucket.count += count;
      if (bucket.examples.size < EXAMPLES_PER_GROUP) bucket.examples.add(record.id);
      buckets.set(type, bucket);
    }
  }
  const rows = [...buckets.entries()]
    .map(([id, bucket]) => ({ id, count: bucket.count, examples: [...bucket.examples] }))
    .sort((a, b) => {
      const namespacedDelta = Number(b.id.includes(":")) - Number(a.id.includes(":"));
      if (namespacedDelta !== 0) return namespacedDelta;
      const countDelta = b.count - a.count;
      return countDelta !== 0 ? countDelta : a.id.localeCompare(b.id);
    });
  const namespaced = rows.filter((row) => row.id.includes(":")).slice(0, RECIPE_TYPE_LIMIT);
  const generic = rows.filter((row) => !row.id.includes(":")).slice(0, 8);
  return [...namespaced, ...generic].slice(0, RECIPE_TYPE_LIMIT);
}

function recipeIdNamespaceCounts(records: readonly ItemExtractRecord[]): RuntimeCountExamples[] {
  const buckets = new Map<string, { count: number; examples: Set<string> }>();
  for (const record of records) {
    for (const recipeId of [...record.recipe_role.ingredient_of, ...record.recipe_role.output_of]) {
      const namespace = recipeId.split(":", 1)[0] || "(unknown)";
      const bucket = buckets.get(namespace) ?? { count: 0, examples: new Set<string>() };
      bucket.count++;
      if (bucket.examples.size < EXAMPLES_PER_GROUP) bucket.examples.add(recipeId);
      buckets.set(namespace, bucket);
    }
  }
  return [...buckets.entries()]
    .map(([id, bucket]) => ({ id, count: bucket.count, examples: [...bucket.examples] }))
    .sort((a, b) => {
      const countDelta = b.count - a.count;
      return countDelta !== 0 ? countDelta : a.id.localeCompare(b.id);
    })
    .slice(0, RECIPE_NAMESPACE_LIMIT);
}

function itemSamples(records: readonly ItemExtractRecord[]): RuntimeItemSample[] {
  return [...records]
    .map((record) => ({ record, score: itemSampleScore(record) }))
    .sort((a, b) => {
      const scoreDelta = b.score - a.score;
      return scoreDelta !== 0 ? scoreDelta : a.record.id.localeCompare(b.record.id);
    })
    .slice(0, ITEM_SAMPLE_LIMIT)
    .map(({ record }) => ({
      id: record.id,
      ...(record.display_name ? { display_name: record.display_name } : {}),
      ...sampleArray("tags", record.minecraft_tags, 6),
      ...sampleArray("block_tags", blockTags(record), 6),
      ...sampleArray("ingredient_recipe_types", Object.keys(record.recipe_role.ingredient_of_counts), 6),
      ...sampleArray("output_recipe_types", Object.keys(record.recipe_role.output_of_counts), 6),
      ...sampleArray("components", componentKeys(record), 6),
    }));
}

function itemSampleScore(record: ItemExtractRecord): number {
  let score = 0;
  const tokens = pathTokens(record.path);
  for (const token of tokens) {
    if (HIGH_SIGNAL_TOKENS.has(token)) score += 8;
  }
  if ((record.extractor_meta ?? {}).is_block_item === true) score += 2;
  if (Object.keys(record.recipe_role.output_of_counts).some((type) => type.includes(":"))) score += 3;
  if (Object.keys(record.recipe_role.ingredient_of_counts).some((type) => type.includes(":"))) score += 2;
  if (record.component_data?.["minecraft:max_damage"] !== undefined) score += 1;
  if (record.component_data?.["minecraft:equippable"] !== undefined) score += 1;
  if (record.recipe_role.in_degree + record.recipe_role.out_degree > 20) score += 2;
  return score;
}

function blockTags(record: ItemExtractRecord): string[] {
  const value = record.extractor_meta?.block_tags;
  return Array.isArray(value) ? value.filter((tag): tag is string => typeof tag === "string") : [];
}

function componentKeys(record: ItemExtractRecord): string[] {
  return Object.keys(record.component_data ?? {}).sort();
}

function sampleArray<K extends string>(
  key: K,
  values: readonly string[],
  limit: number,
): Record<K, string[]> | Record<string, never> {
  const filtered = values.filter((value) => value.length > 0).slice(0, limit);
  return filtered.length > 0 ? { [key]: filtered } as Record<K, string[]> : {};
}

function isPositiveNumber(value: unknown): boolean {
  return typeof value === "number" && value > 0;
}

function isSubsystemEntry(value: unknown): value is SubsystemEntry {
  if (!value || typeof value !== "object") return false;
  const id = (value as { id?: unknown }).id;
  return typeof id === "string" && id.includes(":");
}
