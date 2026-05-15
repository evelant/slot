import type { FacetEvidenceKind, FacetEvidenceRecord } from "../evidence/facet_evidence.ts";
import type {
  VocabularyDefaultSectionPressure,
  VocabularyHumanVisibleTextPool,
  VocabularyItemFamilyCluster,
  VocabularyItemSampleMode,
  VocabularyPromptOverview,
  VocabularyRecipeUseNeighborhood,
  VocabularyRuntimeItemSample,
  VocabularyTagMembershipSummary,
  VocabularyFacetId,
} from "./types.ts";
import { labelFromId, sortedLimited, splitResourceLocation, tokenPath, tokenSet } from "./helpers.ts";
import { promptSemanticEvidence } from "./semantic_index.ts";

const ITEM_SAMPLE_LIMIT = 12;
const SECTION_LIMIT = 32;
const FAMILY_CLUSTER_LIMIT = 64;
const TAG_SUMMARY_LIMIT = 96;
const RECIPE_NEIGHBORHOOD_LIMIT = 80;
const TEXT_POOL_SNIPPET_LIMIT = 28;
const RUNTIME_ITEM_SAMPLE_FIELD_LIMIT = 12;
const RUNTIME_ITEM_SAMPLE_SEMANTIC_LIMIT = 3;

interface RuntimeItemSummary {
  id: string;
  label: string;
  namespace?: string;
  record: FacetEvidenceRecord;
  tokens: Set<string>;
  semantic: string[];
  recipeUses: Map<string, number>;
}

interface SectionAccumulator {
  section: string;
  protectedBuiltin: boolean;
  note?: string;
  items: RuntimeItemSummary[];
  terms: Map<string, number>;
}

interface FamilyAccumulator {
  term: string;
  items: RuntimeItemSummary[];
  related: Map<string, number>;
  recipeUses: Map<string, number>;
  semantic: string[];
}

interface RecipeAccumulator {
  id: string;
  label: string;
  recipeCount?: number;
  inputItems: Map<string, RuntimeItemSummary>;
  outputItems: Map<string, RuntimeItemSummary>;
  semantic: string[];
}

interface TextPoolAccumulator {
  topic: string;
  recordIds: Set<string>;
  snippets: string[];
}

interface DefaultSectionRule {
  section: string;
  protectedBuiltin: boolean;
  note?: string;
  tokens?: readonly string[];
  tagTokens?: readonly string[];
  labelPattern?: RegExp;
}

const DEFAULT_SECTION_RULES: readonly DefaultSectionRule[] = [
  { section: "Food", protectedBuiltin: true, tokens: ["food", "meal", "bread", "cheese", "meat", "fruit", "vegetable", "soup", "salad"], tagTokens: ["foods", "food"] },
  { section: "Tools", protectedBuiltin: true, tokens: ["tool", "pickaxe", "axe", "shovel", "hoe", "hammer", "saw", "knife", "wrench", "drill", "chisel", "prospector"] },
  { section: "Weapons", protectedBuiltin: true, tokens: ["sword", "bow", "crossbow", "trident", "mace", "arrow", "spear", "weapon"] },
  { section: "Armor", protectedBuiltin: true, tokens: ["armor", "armour", "helmet", "chestplate", "leggings", "boots", "pants", "suit"] },
  { section: "Lighting", protectedBuiltin: true, tokens: ["torch", "lantern", "lamp", "candle", "glowstone", "shroomlight", "light"], tagTokens: ["light"] },
  { section: "Stairs", protectedBuiltin: true, tokens: ["stairs"], labelPattern: /\bstairs\b/i },
  { section: "Slabs", protectedBuiltin: true, tokens: ["slab", "slabs"], labelPattern: /\bslab\b/i },
  { section: "Walls", protectedBuiltin: true, tokens: ["wall", "walls"], labelPattern: /\bwall\b/i },
  { section: "Doors", protectedBuiltin: true, tokens: ["door", "doors", "trapdoor", "trapdoors", "gate"], labelPattern: /\b(door|trapdoor|gate)\b/i },
  { section: "Fences", protectedBuiltin: true, tokens: ["fence", "fences"], labelPattern: /\bfence\b/i },
  { section: "Windows", protectedBuiltin: true, tokens: ["window", "windows", "pane", "panes", "bars"], labelPattern: /\b(pane|window|bars)\b/i },
  { section: "Ores & Raw Stock", protectedBuiltin: true, tokens: ["ore", "ores", "raw_ore", "crushed_ore", "poor_ore", "normal_ore", "rich_ore"], tagTokens: ["ores", "raw_materials"] },
  { section: "Metal Stock", protectedBuiltin: true, tokens: ["ingot", "ingots", "nugget", "nuggets", "plate", "plates", "rod", "rods", "wire", "wires", "bolt", "bolts", "screw", "screws", "foil", "foils"], tagTokens: ["ingots", "nuggets", "plates", "rods", "wires"] },
  { section: "Gems & Crystals", protectedBuiltin: true, tokens: ["gem", "gems", "crystal", "crystals", "diamond", "emerald", "ruby", "sapphire", "quartz", "opal"], tagTokens: ["gems", "crystals"] },
  { section: "Dusts & Powders", protectedBuiltin: true, tokens: ["dust", "dusts", "powder", "powders"], tagTokens: ["dusts", "powders"] },
  { section: "Wood", protectedBuiltin: true, tokens: ["wood", "log", "logs", "plank", "planks", "board", "boards", "stick", "sticks", "lumber", "beam", "beams"], tagTokens: ["logs", "planks", "wooden"] },
  { section: "Seeds", protectedBuiltin: true, tokens: ["seed", "seeds"], tagTokens: ["seeds"] },
  { section: "Crops", protectedBuiltin: true, tokens: ["crop", "crops", "grain", "vegetable", "vegetables", "fruit", "fruits"], tagTokens: ["crops", "fruits", "vegetables", "grains"] },
  { section: "Plants", protectedBuiltin: true, tokens: ["plant", "plants", "sapling", "saplings", "leaf", "leaves", "flower", "flowers", "grass", "mushroom"], tagTokens: ["plants", "saplings", "flowers"] },
  { section: "Ceramics & Molds", protectedBuiltin: true, tokens: ["ceramic", "ceramics", "clay", "mold", "molds", "brick", "bricks", "pottery", "pot"], tagTokens: ["molds", "clay", "ceramic"] },
  { section: "Organic Materials", protectedBuiltin: true, tokens: ["hide", "leather", "bone", "feather", "wool", "string", "fiber", "fibers", "straw", "thatch", "wax"], tagTokens: ["organic", "leather", "wool", "string"] },
  { section: "Storage", protectedBuiltin: true, tokens: ["chest", "barrel", "crate", "sack", "bag", "backpack", "basket", "box", "drawer", "tank"], tagTokens: ["storage", "chests", "barrels", "containers"] },
  { section: "Building Blocks", protectedBuiltin: true, tokens: ["block", "blocks", "brick", "bricks", "tile", "tiles", "stone", "cobble", "concrete"], tagTokens: ["building_blocks"] },
  { section: "Decoration", protectedBuiltin: true, tokens: ["decorative", "decoration", "ornament", "banner", "sign", "painting", "carpet"], tagTokens: ["decorations"] },
  { section: "Natural", protectedBuiltin: true, tokens: ["dirt", "sand", "gravel", "soil", "mud", "rock", "rocks", "stone", "cobblestone"], tagTokens: ["natural"] },
  { section: "Workbenches", protectedBuiltin: true, tokens: ["workbench", "bench", "table", "station", "crafting_table"], tagTokens: ["workbenches"] },
  { section: "Mechanisms", protectedBuiltin: true, tokens: ["machine", "mechanism", "motor", "engine", "gear", "gears", "shaft", "cog", "cogwheel", "belt", "pipe"], tagTokens: ["machines", "mechanisms"] },
  { section: "Redstone", protectedBuiltin: true, tokens: ["redstone", "comparator", "repeater", "lever", "button", "pressure_plate"], tagTokens: ["redstone"] },
  { section: "Upgrades", protectedBuiltin: true, tokens: ["upgrade", "upgrades", "augment", "module", "modules"], tagTokens: ["upgrades"] },
  { section: "Transport", protectedBuiltin: true, tokens: ["rail", "rails", "track", "tracks", "cart", "minecart", "boat", "rocket", "train"], tagTokens: ["transport"] },
  { section: "Utility", protectedBuiltin: true, tokens: ["bucket", "bottle", "flask", "map", "compass", "rope", "ladder"], tagTokens: ["utility"] },
  { section: "Curiosities", protectedBuiltin: true, tokens: ["trophy", "curio", "curios", "relic", "disc", "record", "spawn_egg"], tagTokens: ["curios"] },
  {
    section: "Materials fallback pressure",
    protectedBuiltin: false,
    note: "Approximate items that look material-like but did not land in a narrower protected stock section; useful only for judging whether more broad custom splits are needed.",
    tokens: ["material", "component", "part", "parts", "piece", "pieces", "fragment", "fragments", "shard", "shards", "fiber", "fibers"],
  },
];

const FAMILY_TOKEN_STOPWORDS = new Set([
  "a",
  "an",
  "and",
  "block",
  "blocks",
  "can",
  "composition",
  "crafted",
  "crafting",
  "empty",
  "base",
  "filled",
  "from",
  "group",
  "hidden",
  "half",
  "in",
  "index",
  "interaction",
  "item",
  "items",
  "large",
  "light",
  "main",
  "material",
  "materials",
  "normal",
  "on",
  "of",
  "palette",
  "part",
  "parts",
  "piece",
  "pile",
  "raw",
  "recipe",
  "recipes",
  "small",
  "tab",
  "tiny",
  "usable",
  "value",
  "values",
  "viewer",
  "whitelisted",
  "the",
  "to",
  "with",
  "ground",
  "white",
  "orange",
  "magenta",
  "light_blue",
  "yellow",
  "lime",
  "pink",
  "gray",
  "grey",
  "light_gray",
  "light_grey",
  "cyan",
  "purple",
  "blue",
  "brown",
  "green",
  "red",
  "black",
]);

export function buildVocabularyPromptOverview(args: {
  facet: VocabularyFacetId;
  records: readonly FacetEvidenceRecord[];
  itemSampleSize?: number;
  itemSampleSeed?: string;
  itemSampleMode?: VocabularyItemSampleMode;
  vocabularyIteration?: number;
}): VocabularyPromptOverview | undefined {
  const runtimeItems = args.records
    .filter((record) => record.kind === "runtime_item")
    .map(runtimeItemSummary);
  const itemSample = buildRuntimeItemSample(args.records, runtimeItems, {
    size: args.itemSampleSize ?? 0,
    seed: args.itemSampleSeed ?? "slot-vocabulary-sample",
    mode: args.itemSampleMode ?? "coverage",
  });
  if (args.facet !== "organization_group") {
    if (itemSample.length === 0) return undefined;
    return {
      purpose: "Rotating sample of the pack item universe for vocabulary refinement. These are raw item observations, not proposed vocabulary values.",
      item_sample_purpose: "Show the model concrete item shapes so it can refine vocabulary around actual pack items without relying on deterministic semantic classifications.",
      ...(args.itemSampleSeed ? { item_sample_seed: args.itemSampleSeed } : {}),
      item_sample_mode: args.itemSampleMode ?? "coverage",
      ...(args.vocabularyIteration !== undefined ? { item_sample_iteration: args.vocabularyIteration } : {}),
      runtime_item_count: runtimeItems.length,
      runtime_item_sample: itemSample,
    };
  }
  const itemById = new Map(runtimeItems.map((item) => [item.id, item]));
  const recipeNeighborhoods = buildRecipeUseNeighborhoods(args.records, runtimeItems, itemById);
  return {
    purpose: "Pack-wide context for judging broad human storage sections. These summaries and item samples are not proposed vocabulary values; use them to understand what kinds of items exist, what built-in sections already cover, and where overloaded broad stock might need a small number of custom splits.",
    ...(itemSample.length
      ? {
          item_sample_purpose: "Rotating raw item observations for vocabulary refinement. Do not treat sampled tags, ids, or recipe facts as deterministic section assignments.",
          ...(args.itemSampleSeed ? { item_sample_seed: args.itemSampleSeed } : {}),
          item_sample_mode: args.itemSampleMode ?? "coverage",
        }
      : {}),
    ...(itemSample.length && args.vocabularyIteration !== undefined ? { item_sample_iteration: args.vocabularyIteration } : {}),
    runtime_item_count: runtimeItems.length,
    ...(itemSample.length ? { runtime_item_sample: itemSample } : {}),
    default_section_pressure: buildDefaultSectionPressure(runtimeItems),
    runtime_item_family_clusters: buildRuntimeItemFamilyClusters(runtimeItems),
    tag_membership_summaries: buildTagMembershipSummaries(args.records, itemById),
    recipe_use_neighborhoods: recipeNeighborhoods,
    human_visible_text_pools: buildHumanVisibleTextPools(args.records),
  };
}

function buildRuntimeItemSample(
  records: readonly FacetEvidenceRecord[],
  items: readonly RuntimeItemSummary[],
  options: {
    size: number;
    seed: string;
    mode: VocabularyItemSampleMode;
  },
): VocabularyRuntimeItemSample[] {
  const size = Math.max(0, Math.min(options.size, items.length));
  if (size === 0) return [];
  if (items.length <= size) {
    return items
      .slice()
      .sort((a, b) => a.id.localeCompare(b.id))
      .map(promptRuntimeItemSample);
  }

  const rng = seededRandom(options.seed);
  const selected = new Map<string, RuntimeItemSummary>();
  const addItems = (candidates: readonly RuntimeItemSummary[], limit: number) => {
    for (const item of shuffled(candidates, rng)) {
      if (selected.size >= size || limit <= 0) break;
      if (selected.has(item.id)) continue;
      selected.set(item.id, item);
      limit--;
    }
  };
  const addRoundRobin = (groups: ReadonlyMap<string, readonly RuntimeItemSummary[]>, limit: number) => {
    const queues = shuffled([...groups.values()].filter((group) => group.length > 0), rng)
      .map((group) => shuffled(group, rng));
    let remaining = limit;
    while (remaining > 0 && selected.size < size && queues.length > 0) {
      let touched = false;
      for (const queue of queues) {
        while (queue.length > 0) {
          const item = queue.shift()!;
          if (selected.has(item.id)) continue;
          selected.set(item.id, item);
          remaining--;
          touched = true;
          break;
        }
        if (remaining <= 0 || selected.size >= size) break;
      }
      if (!touched) break;
    }
  };

  if (options.mode === "random") {
    addItems(items, size);
  } else {
    const wholePackQuota = Math.ceil(size * 0.40);
    const namespaceQuota = Math.ceil(size * 0.20);
    const linkedQuota = Math.ceil(size * 0.15);
    const recipeQuota = Math.ceil(size * 0.10);
    const creativeTabQuota = Math.ceil(size * 0.075);
    const tagQuota = Math.ceil(size * 0.075);

    addItems(items, wholePackQuota);
    addRoundRobin(groupBy(items, (item) => item.namespace ?? "unknown"), namespaceQuota);
    addItems(guideLinkedItems(records, items), linkedQuota);
    addItems(highRecipeDegreeItems(items, Math.max(recipeQuota * 4, Math.ceil(items.length * 0.20))), recipeQuota);
    addRoundRobin(groupByCreativeTab(items), creativeTabQuota);
    addRoundRobin(groupByTag(items), tagQuota);
    addItems(items, size - selected.size);
  }

  return [...selected.values()]
    .sort((a, b) => a.id.localeCompare(b.id))
    .map(promptRuntimeItemSample);
}

function promptRuntimeItemSample(item: RuntimeItemSummary): VocabularyRuntimeItemSample {
  const recipeRoles = item.record.recipe_roles;
  return {
    id: item.id,
    label: item.label,
    ...(item.namespace ? { namespace: item.namespace } : {}),
    ...(item.record.tags?.length ? { tags: sortedLimited(item.record.tags, RUNTIME_ITEM_SAMPLE_FIELD_LIMIT) } : {}),
    ...(item.record.direct_tags?.length ? { direct_tags: sortedLimited(item.record.direct_tags, RUNTIME_ITEM_SAMPLE_FIELD_LIMIT) } : {}),
    ...(item.record.creative_tabs?.length ? { creative_tabs: sortedLimited(item.record.creative_tabs, 6) } : {}),
    ...(item.record.components?.length ? { components: sortedLimited(item.record.components, 8) } : {}),
    ...(item.record.model_parents?.length ? { model_parents: sortedLimited(item.record.model_parents, 6) } : {}),
    ...(recipeRoles ? {
      recipe_roles: {
        in_degree: recipeRoles.in_degree,
        out_degree: recipeRoles.out_degree,
        ...nonEmptyRecord("ingredient_types", topObjectEntries(recipeRoles.ingredient_types, 6)),
        ...nonEmptyRecord("output_types", topObjectEntries(recipeRoles.output_types, 6)),
        ...(recipeRoles.ingredient_examples.length
          ? { ingredient_examples: sortedLimited(recipeRoles.ingredient_examples, 8) }
          : {}),
        ...(recipeRoles.output_examples.length
          ? { output_examples: sortedLimited(recipeRoles.output_examples, 8) }
          : {}),
      },
    } : {}),
    ...(item.semantic.length ? { semantic_context: item.semantic.slice(0, RUNTIME_ITEM_SAMPLE_SEMANTIC_LIMIT) } : {}),
  };
}

function guideLinkedItems(
  records: readonly FacetEvidenceRecord[],
  items: readonly RuntimeItemSummary[],
): RuntimeItemSummary[] {
  const linked = new Set<string>();
  for (const record of records) {
    if (record.kind === "runtime_item") continue;
    if (!record.semantic_text?.length && record.kind !== "recipe_role_summary") continue;
    for (const id of record.item_refs ?? []) linked.add(id);
    for (const id of record.examples ?? []) linked.add(id);
  }
  return items.filter((item) => linked.has(item.id));
}

function highRecipeDegreeItems(
  items: readonly RuntimeItemSummary[],
  limit: number,
): RuntimeItemSummary[] {
  return items
    .filter((item) => recipeDegree(item) > 0)
    .sort((a, b) => recipeDegree(b) - recipeDegree(a) || a.id.localeCompare(b.id))
    .slice(0, limit);
}

function recipeDegree(item: RuntimeItemSummary): number {
  return (item.record.recipe_roles?.in_degree ?? 0) + (item.record.recipe_roles?.out_degree ?? 0);
}

function groupBy(
  items: readonly RuntimeItemSummary[],
  keyFor: (item: RuntimeItemSummary) => string,
): Map<string, RuntimeItemSummary[]> {
  const groups = new Map<string, RuntimeItemSummary[]>();
  for (const item of items) {
    const key = keyFor(item);
    const group = groups.get(key) ?? [];
    group.push(item);
    groups.set(key, group);
  }
  return groups;
}

function groupByCreativeTab(items: readonly RuntimeItemSummary[]): Map<string, RuntimeItemSummary[]> {
  const groups = new Map<string, RuntimeItemSummary[]>();
  for (const item of items) {
    for (const tab of item.record.creative_tabs ?? []) {
      const group = groups.get(tab) ?? [];
      group.push(item);
      groups.set(tab, group);
    }
  }
  return groups;
}

function groupByTag(items: readonly RuntimeItemSummary[]): Map<string, RuntimeItemSummary[]> {
  const groups = new Map<string, RuntimeItemSummary[]>();
  for (const item of items) {
    const tags = [...(item.record.direct_tags ?? []), ...(item.record.tags ?? [])].slice(0, 12);
    for (const tag of tags) {
      if (tagLooksTechnical(tag)) continue;
      const group = groups.get(tag) ?? [];
      group.push(item);
      groups.set(tag, group);
    }
  }
  return groups;
}

function shuffled<T>(values: readonly T[], rng: () => number): T[] {
  const out = values.slice();
  for (let i = out.length - 1; i > 0; i--) {
    const j = Math.floor(rng() * (i + 1));
    [out[i], out[j]] = [out[j]!, out[i]!];
  }
  return out;
}

function seededRandom(seed: string): () => number {
  let state = 0x811c9dc5;
  for (let i = 0; i < seed.length; i++) {
    state ^= seed.charCodeAt(i);
    state = Math.imul(state, 0x01000193) >>> 0;
  }
  return () => {
    state += 0x6d2b79f5;
    let t = state;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

function topObjectEntries(values: Record<string, number>, limit: number): Record<string, number> {
  return Object.fromEntries(
    Object.entries(values)
      .sort(([, a], [, b]) => b - a)
      .slice(0, limit),
  );
}

function nonEmptyRecord<K extends string>(
  key: K,
  value: Record<string, number>,
): Partial<Record<K, Record<string, number>>> {
  return Object.keys(value).length ? { [key]: value } as Partial<Record<K, Record<string, number>>> : {};
}

function runtimeItemSummary(record: FacetEvidenceRecord): RuntimeItemSummary {
  const label = record.label ?? labelFromId(record.id);
  const semantic = (record.semantic_text ?? [])
    .slice(0, 4)
    .map((entry) => promptSemanticEvidence({
      kind: record.kind,
      id: record.id,
      source: record.source,
      text: entry.text,
      key: entry.key,
    }))
    .filter((text) => text && !textLooksLowSignal(text));
  const recipeUses = new Map<string, number>();
  for (const [type, count] of Object.entries(record.recipe_roles?.ingredient_types ?? {})) {
    recipeUses.set(type, (recipeUses.get(type) ?? 0) + count);
  }
  for (const [type, count] of Object.entries(record.recipe_roles?.output_types ?? {})) {
    recipeUses.set(type, (recipeUses.get(type) ?? 0) + count);
  }
  return {
    id: record.id,
    label,
    namespace: record.namespace,
    record,
    tokens: itemTokens(record),
    semantic,
    recipeUses,
  };
}

function buildDefaultSectionPressure(items: readonly RuntimeItemSummary[]): VocabularyDefaultSectionPressure[] {
  const acc = new Map<string, SectionAccumulator>();
  for (const rule of DEFAULT_SECTION_RULES) {
    acc.set(rule.section, {
      section: rule.section,
      protectedBuiltin: rule.protectedBuiltin,
      note: rule.note,
      items: [],
      terms: new Map(),
    });
  }
  for (const item of items) {
    const rule = DEFAULT_SECTION_RULES.find((candidate) => sectionRuleMatches(candidate, item));
    if (!rule) continue;
    const entry = acc.get(rule.section)!;
    entry.items.push(item);
    for (const term of item.tokens) {
      if (FAMILY_TOKEN_STOPWORDS.has(term)) continue;
      entry.terms.set(term, (entry.terms.get(term) ?? 0) + 1);
    }
  }
  return [...acc.values()]
    .filter((entry) => entry.items.length > 0)
    .sort((a, b) => b.items.length - a.items.length || a.section.localeCompare(b.section))
    .slice(0, SECTION_LIMIT)
    .map((entry) => ({
      section: entry.section,
      protected_builtin: entry.protectedBuiltin,
      ...(entry.note ? { note: entry.note } : {}),
      item_count: entry.items.length,
      sample_items: sampleItems(entry.items),
      common_terms: topKeys(entry.terms, 8),
    }));
}

function buildRuntimeItemFamilyClusters(items: readonly RuntimeItemSummary[]): VocabularyItemFamilyCluster[] {
  const acc = new Map<string, FamilyAccumulator>();
  for (const item of items) {
    const clusterTerms = [...item.tokens]
      .filter((term) => term.length >= 3)
      .filter((term) => !FAMILY_TOKEN_STOPWORDS.has(term))
      .filter((term) => !termLooksTechnical(term))
      .slice(0, 32);
    for (const term of clusterTerms) {
      let entry = acc.get(term);
      if (!entry) {
        entry = {
          term,
          items: [],
          related: new Map(),
          recipeUses: new Map(),
          semantic: [],
        };
        acc.set(term, entry);
      }
      entry.items.push(item);
      for (const related of clusterTerms) {
        if (related === term) continue;
        entry.related.set(related, (entry.related.get(related) ?? 0) + 1);
      }
      for (const [type, count] of item.recipeUses) {
        entry.recipeUses.set(type, (entry.recipeUses.get(type) ?? 0) + count);
      }
      for (const text of item.semantic) {
        if (entry.semantic.length >= 8) break;
        if (!entry.semantic.includes(text)) entry.semantic.push(text);
      }
    }
  }

  return [...acc.values()]
    .filter((entry) => familyClusterTermAllowed(entry.term))
    .filter((entry) => entry.items.length >= 4 && entry.items.length <= Math.max(48, items.length * 0.035))
    .sort((a, b) => familyClusterScore(b) - familyClusterScore(a) || a.term.localeCompare(b.term))
    .slice(0, FAMILY_CLUSTER_LIMIT)
    .map((entry) => ({
      term: entry.term,
      item_count: entry.items.length,
      sample_items: sampleItems(entry.items),
      related_terms: topKeys(entry.related, 8),
      top_recipe_uses: topKeys(entry.recipeUses, 5),
      ...(entry.semantic.length ? { semantic_context: entry.semantic.slice(0, 4) } : {}),
    }));
}

function buildTagMembershipSummaries(
  records: readonly FacetEvidenceRecord[],
  itemById: ReadonlyMap<string, RuntimeItemSummary>,
): VocabularyTagMembershipSummary[] {
  return records
    .filter((record) => record.kind === "item_tag" || record.kind === "block_tag")
    .filter((record) => !tagLooksTechnical(record.id))
    .filter((record) => (record.count ?? record.item_refs?.length ?? record.examples?.length ?? 0) >= 3)
    .sort((a, b) => tagScore(b) - tagScore(a) || a.id.localeCompare(b.id))
    .slice(0, TAG_SUMMARY_LIMIT)
    .map((record) => {
      const members = itemRefs(record)
        .map((id) => itemById.get(id))
        .filter((item): item is RuntimeItemSummary => !!item);
      return {
        tag_id: record.id,
        label: record.label ?? labelFromId(record.id),
        kind: record.kind as FacetEvidenceKind,
        member_count: record.count ?? members.length,
        sample_items: sampleItems(members.length ? members : itemRefs(record).map((id) => ({ id, label: labelFromId(id) }))),
        top_namespaces: topNamespaces(itemRefs(record)),
      };
    });
}

function buildRecipeUseNeighborhoods(
  records: readonly FacetEvidenceRecord[],
  items: readonly RuntimeItemSummary[],
  itemById: ReadonlyMap<string, RuntimeItemSummary>,
): VocabularyRecipeUseNeighborhood[] {
  const recipes = new Map<string, RecipeAccumulator>();
  for (const record of records) {
    if (record.kind !== "recipe_type") continue;
    const entry = ensureRecipeAccumulator(recipes, record.id, record.label ?? labelFromId(record.id));
    entry.recipeCount = record.count ?? entry.recipeCount;
    entry.semantic.push(...(record.semantic_text ?? []).slice(0, 4).map((semantic) => semantic.text));
  }
  for (const item of items) {
    for (const [type] of Object.entries(item.record.recipe_roles?.ingredient_types ?? {})) {
      ensureRecipeAccumulator(recipes, type, labelFromId(type)).inputItems.set(item.id, item);
    }
    for (const [type] of Object.entries(item.record.recipe_roles?.output_types ?? {})) {
      ensureRecipeAccumulator(recipes, type, labelFromId(type)).outputItems.set(item.id, item);
    }
  }
  for (const record of records) {
    if (record.kind !== "recipe_role_summary" || !record.recipe_type) continue;
    const entry = ensureRecipeAccumulator(recipes, record.recipe_type, labelFromId(record.recipe_type));
    for (const id of record.item_refs ?? []) {
      const item = itemById.get(id);
      if (!item) continue;
      if (record.role === "input") entry.inputItems.set(id, item);
      if (record.role === "output") entry.outputItems.set(id, item);
    }
  }

  return [...recipes.values()]
    .filter((entry) => !recipeTypeLooksTooGeneric(entry.id))
    .filter((entry) => entry.inputItems.size + entry.outputItems.size >= 3)
    .sort((a, b) => recipeNeighborhoodScore(b) - recipeNeighborhoodScore(a) || a.id.localeCompare(b.id))
    .slice(0, RECIPE_NEIGHBORHOOD_LIMIT)
    .map((entry) => ({
      recipe_type: entry.id,
      label: entry.label,
      ...(entry.recipeCount !== undefined ? { recipe_count: entry.recipeCount } : {}),
      input_item_count: entry.inputItems.size,
      output_item_count: entry.outputItems.size,
      sample_inputs: sampleItems([...entry.inputItems.values()]),
      sample_outputs: sampleItems([...entry.outputItems.values()]),
      ...(entry.semantic.length ? { semantic_context: sortedLimited(entry.semantic, 4) } : {}),
    }));
}

function buildHumanVisibleTextPools(records: readonly FacetEvidenceRecord[]): VocabularyHumanVisibleTextPool[] {
  const pools = new Map<string, TextPoolAccumulator>();
  for (const record of records) {
    const topic = textPoolTopic(record);
    if (!topic) continue;
    for (const entry of record.semantic_text ?? []) {
      const text = clipText(entry.text, 320);
      if (!text || textLooksLowSignal(text)) continue;
      const pool = pools.get(topic) ?? { topic, recordIds: new Set(), snippets: [] };
      pool.recordIds.add(record.id);
      if (pool.snippets.length < TEXT_POOL_SNIPPET_LIMIT) {
        const label = record.label ?? record.title ?? labelFromId(record.id);
        const snippet = `${label}: ${text}`;
        if (!pool.snippets.includes(snippet)) pool.snippets.push(snippet);
      }
      pools.set(topic, pool);
    }
  }
  return [...pools.values()]
    .sort((a, b) => b.recordIds.size - a.recordIds.size || a.topic.localeCompare(b.topic))
    .map((pool) => ({
      topic: pool.topic,
      record_count: pool.recordIds.size,
      snippets: pool.snippets,
    }))
    .filter((pool) => pool.snippets.length > 0);
}

function sectionRuleMatches(rule: DefaultSectionRule, item: RuntimeItemSummary): boolean {
  const idPath = splitResourceLocation(item.id)?.path ?? item.id;
  const labelHaystack = `${item.label} ${idPath}`.toLowerCase();
  if (rule.labelPattern?.test(labelHaystack)) return true;
  if (rule.tokens?.some((term) => item.tokens.has(term))) return true;
  if (rule.tagTokens?.some((term) => itemTagTokens(item.record).has(term))) return true;
  return false;
}

function itemTokens(record: FacetEvidenceRecord): Set<string> {
  const values = [
    record.label,
    record.title,
    record.description,
  ].filter((value): value is string => typeof value === "string" && value.length > 0);
  const out = new Set<string>();
  for (const value of [record.id, ...(record.tags ?? []), ...(record.direct_tags ?? []), ...(record.creative_tabs ?? [])]) {
    const path = splitResourceLocation(value)?.path ?? value;
    for (const term of tokenSet(path)) out.add(canonicalToken(term));
    const pathToken = tokenPath(path);
    if (pathToken) {
      for (const term of pathToken.split(/[\/_]+/)) {
        if (term) out.add(canonicalToken(term));
      }
    }
  }
  for (const value of values) {
    for (const term of tokenSet(value)) out.add(canonicalToken(term));
    const path = tokenPath(value);
    if (path) {
      for (const term of path.split(/[\/_]+/)) {
        if (term) out.add(canonicalToken(term));
      }
    }
  }
  if (record.namespace) out.delete(canonicalToken(record.namespace));
  return out;
}

function itemTagTokens(record: FacetEvidenceRecord): Set<string> {
  const out = new Set<string>();
  for (const value of [...(record.tags ?? []), ...(record.direct_tags ?? [])]) {
    const path = splitResourceLocation(value)?.path ?? value;
    for (const part of path.split(/[\/_.-]+/)) {
      if (part) out.add(canonicalToken(part));
    }
  }
  return out;
}

function canonicalToken(value: string): string {
  const normalized = value.toLowerCase().replace(/[^a-z0-9]+/g, "_").replace(/^_+|_+$/g, "");
  if (normalized.endsWith("ies") && normalized.length > 4) return `${normalized.slice(0, -3)}y`;
  if (normalized.endsWith("ses") && normalized.length > 4) return normalized.slice(0, -2);
  if (
    normalized.endsWith("s") &&
    normalized.length > 4 &&
    !normalized.endsWith("ss") &&
    !normalized.endsWith("ous") &&
    !normalized.endsWith("js")
  ) return normalized.slice(0, -1);
  return normalized;
}

function sampleItems(items: readonly (RuntimeItemSummary | { id: string; label: string })[]): string[] {
  return sortedLimited(
    items.map((item) => `${item.label} (${item.id})`),
    ITEM_SAMPLE_LIMIT,
  );
}

function itemRefs(record: FacetEvidenceRecord): string[] {
  return sortedLimited([...(record.item_refs ?? []), ...(record.examples ?? [])], ITEM_SAMPLE_LIMIT * 2);
}

function topNamespaces(ids: readonly string[]): string[] {
  const counts = new Map<string, number>();
  for (const id of ids) {
    const namespace = splitResourceLocation(id)?.namespace;
    if (!namespace) continue;
    counts.set(namespace, (counts.get(namespace) ?? 0) + 1);
  }
  return topKeys(counts, 6);
}

function topKeys(values: ReadonlyMap<string, number>, limit: number): string[] {
  return [...values.entries()]
    .sort(([, a], [, b]) => b - a)
    .slice(0, limit)
    .map(([key, value]) => `${key} (${value})`);
}

function familyClusterScore(entry: FamilyAccumulator): number {
  const count = entry.items.length;
  const semanticBonus = Math.min(8, entry.semantic.length) * 2;
  const recipeBonus = Math.min(12, entry.recipeUses.size);
  return Math.log2(count + 1) * 10 + semanticBonus + recipeBonus;
}

function familyClusterTermAllowed(term: string): boolean {
  if (term.length < 3) return false;
  if (/^[0-9]+x?$/.test(term)) return false;
  return true;
}

function tagScore(record: FacetEvidenceRecord): number {
  const count = record.count ?? record.item_refs?.length ?? record.examples?.length ?? 0;
  const directBonus = record.direct_membership_known ? 12 : 0;
  const kindBonus = record.kind === "item_tag" ? 6 : 0;
  return Math.log2(count + 1) * 10 + directBonus + kindBonus;
}

function recipeNeighborhoodScore(entry: RecipeAccumulator): number {
  const itemCount = entry.inputItems.size + entry.outputItems.size;
  const recipeCount = entry.recipeCount ?? 0;
  return Math.log2(itemCount + 1) * 12 + Math.log2(recipeCount + 1) * 4 + Math.min(8, entry.semantic.length);
}

function ensureRecipeAccumulator(
  recipes: Map<string, RecipeAccumulator>,
  id: string,
  label: string,
): RecipeAccumulator {
  const existing = recipes.get(id);
  if (existing) return existing;
  const entry: RecipeAccumulator = {
    id,
    label,
    inputItems: new Map(),
    outputItems: new Map(),
    semantic: [],
  };
  recipes.set(id, entry);
  return entry;
}

function textPoolTopic(record: FacetEvidenceRecord): string | null {
  if (!record.semantic_text?.length) return null;
  if (record.kind === "quest_node") return "quest text";
  if (record.kind === "kubejs_tooltip") return "kubejs tooltip text";
  if (record.kind === "mod_metadata") return "mod descriptions";
  if (record.kind === "advancement") return "advancement text";
  if (record.kind === "recipe_type") return "recipe category text";
  if (record.kind === "guide_page" && record.page_type === "ponder") return "ponder/lang guide text";
  if (record.kind === "guide_page") return "guide page text";
  return null;
}

function termLooksTechnical(value: string): boolean {
  return /^(can|cannot|needs?|requires?|valid|invalid|allowed|denied|mineable|replaceable|whitelisted|blacklisted|whitelist|blacklist|tag|texture|model|blockstate|recipe|crafting)$/.test(value);
}

function tagLooksTechnical(id: string): boolean {
  const path = splitResourceLocation(id)?.path ?? id;
  const normalized = tokenPath(path)?.replace(/\//g, "_") ?? "";
  return /(^|_)(mineable|needs|incorrect|destroyed|replaceable|prevented|allowed|whitelist|blacklist|can|cannot|valid|invalid|requires?|trigger|spawnable|wrench_pickup|usable_on|usable_in|transparent|p2p)(_|$)/.test(normalized);
}

function recipeTypeLooksTooGeneric(id: string): boolean {
  const path = splitResourceLocation(id)?.path ?? id;
  const normalized = tokenPath(path)?.replace(/\//g, "_") ?? "";
  return normalized === "crafting" ||
    normalized === "crafting_shaped" ||
    normalized === "crafting_shapeless" ||
    normalized === "shaped" ||
    normalized === "shapeless" ||
    normalized === "special";
}

function textLooksLowSignal(value: string): boolean {
  const normalized = value.trim();
  if (normalized.length < 12) return true;
  if (/^can be placed\b/i.test(normalized)) return true;
  if (/^allows mixed\b/i.test(normalized)) return true;
  if (/^supported by:?$/i.test(normalized)) return true;
  if (/^⚖/u.test(normalized)) return true;
  if (/^hold\s+(shift|ctrl|alt|\[w\])/i.test(normalized)) return true;
  if (/^https?:\/\//i.test(normalized)) return true;
  return false;
}

function clipText(value: string, limit: number): string {
  const normalized = value.replace(/\s+/g, " ").trim();
  if (!normalized) return "";
  return normalized.length <= limit ? normalized : `${normalized.slice(0, limit - 3)}...`;
}
