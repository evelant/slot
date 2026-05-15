/**
 * Facet registry v1 — the live schema mirror used by extraction, validation,
 * vocabulary synthesis, and LLM classification prompts.
 *
 * The `description` field on each facet is the single line the LLM prompt uses
 * to explain the facet, so keep it short and disambiguating. `examples` are
 * schema-shape hints only; vocabulary-backed facets receive their usable value
 * set from the pack vocabulary artifact.
 */

export type FacetKind =
  | "enum"
  | "multi_enum"
  | "free_text"
  | "multi_free_text"
  | "boolean"
  | "numeric"
  | "item_ref"
  | "multi_item_ref";

export interface FacetDef {
  kind: FacetKind;
  /** One-line explanation for prompt serialization. */
  description: string;
  /** Closed value list for enum / multi_enum. */
  values?: readonly string[];
  /** Regex for free_text / multi_free_text. */
  pattern?: RegExp;
  /** Numeric unit, optional. */
  unit?: string;
  /** True when the LLM classifier should attempt to fill this facet by default. */
  llm_authored?: boolean;
  /** True when exact machine evidence may also fill this facet. */
  deterministic?: boolean;
  /** 1–3 short example values (for prompt priming on free-text or large enums). */
  examples?: readonly string[];
  /**
   * Semantic facet whose allowed values are supplied by a pack vocabulary
   * artifact. The registry validates the stable id grammar; the vocabulary
   * artifact validates the usable value set for a specific pack.
   */
  vocabulary_backed?: boolean;
}

/** The valid `mode` strings for each kind. Matches layer.schema.json. */
export const MODES_BY_KIND: Record<FacetKind, readonly string[]> = {
  enum: ["replace", "override-if-null"],
  multi_enum: ["replace", "add", "remove"],
  free_text: ["replace", "override-if-null"],
  multi_free_text: ["replace", "add", "remove"],
  boolean: ["replace", "override-if-null"],
  numeric: ["replace", "override-if-null"],
  item_ref: ["replace", "override-if-null"],
  multi_item_ref: ["replace", "add", "remove"],
};

// --- closed-enum value lists ----------------------------------------------

export const CARRY_FREQUENCY_VALUES = [
  "everyday", "frequent", "occasional", "rare", "display_only",
] as const;

export const Y_LEVEL_RANGE_VALUES = [
  "sky", "surface", "underground", "deep", "nether_surface", "end_islands",
] as const;

export const DYE_COLOR_VALUES = [
  "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
  "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red",
  "black",
] as const;

const RARITY_VALUES = ["abundant", "common", "uncommon", "rare", "unique"] as const;

const NAMESPACED_TOKEN = /^[a-z0-9_]+(:[a-z0-9_]+)?$/;
const NAMESPACED_ID = /^[a-z0-9_.-]+:[a-z0-9_/.-]+$/;
const TOKEN = "[a-z][a-z0-9_]*";
const TOKEN_PATH = `${TOKEN}(?:/${TOKEN})*`;
const RESOURCE_NAMESPACE = "(?!(?:slot|pack):)[a-z0-9_.-]+";

export const VOCABULARY_VALUE_ID_PATTERN = new RegExp(
  `^(?:${TOKEN_PATH}|${RESOURCE_NAMESPACE}:${TOKEN_PATH})$`,
);
export const SCOPED_VOCABULARY_VALUE_ID_PATTERN = new RegExp(
  `^(?:${TOKEN_PATH}|${RESOURCE_NAMESPACE}:${TOKEN_PATH})#${TOKEN}$`,
);

export function isVocabularyValueId(value: string): boolean {
  return VOCABULARY_VALUE_ID_PATTERN.test(value);
}

export function isScopedVocabularyValueId(value: string): boolean {
  return SCOPED_VOCABULARY_VALUE_ID_PATTERN.test(value);
}

export const VOCABULARY_BACKED_FACETS = [
  "role",
  "material_family",
  "material_secondary",
  "form",
  "tier",
  "required_tool",
  "required_tool_tier",
  "equip_slot",
  "activity",
  "origin",
  "storage_categories",
  "spawn_interaction",
  "combat_bonus",
  "environmental_property",
  "transport_medium",
  "workflow",
  "workflow_role",
  "used_at",
  "food_category",
  "food_use",
  "preparation_state",
  "material_process_stage",
  "stock_profile",
  "container_state",
  "equipment_effect",
  "protection_context",
  "progression_stage",
  "loadout_context",
  "use_affordance",
  "organization_group",
  "mod_subsystem",
  "produces_effect",
  "multiblock_component_of",
  "multiblock_role",
  "biome",
] as const;

// --- facet catalog --------------------------------------------------------

export const FACETS: Record<string, FacetDef> = {
  // ===== Identity / simple =====
  mod_namespace: {
    kind: "free_text",
    pattern: /^[a-z0-9_.-]+$/,
    description: "Item id namespace (`minecraft`, `examplemod`).",
    deterministic: true,
  },
  role: {
    kind: "free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed fundamental kind of thing the item is. Every item has exactly one role.",
    llm_authored: true,
    vocabulary_backed: true,
  },
  material_family: {
    kind: "free_text",
    pattern: NAMESPACED_TOKEN,
    description: "Vocabulary-backed primary material or substance the item is made of, as a concise facet-scoped value.",
    llm_authored: true,
    vocabulary_backed: true,
  },
  material_secondary: {
    kind: "multi_free_text",
    pattern: NAMESPACED_TOKEN,
    description: "Vocabulary-backed secondary materials for composite items (e.g. `wood_oak` for a brass casing with a wood frame).",
    llm_authored: true,
    vocabulary_backed: true,
  },
  form: {
    kind: "free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed player-recognizable physical shape or form factor, not purpose or storage role.",
    llm_authored: true,
    vocabulary_backed: true,
  },
  tier: {
    kind: "free_text",
    pattern: NAMESPACED_TOKEN,
    description: "Vocabulary-backed progression tier for tools, weapons, armor, tiered materials, machines, and similar pack concepts.",
    llm_authored: true,
    vocabulary_backed: true,
  },
  required_tool: {
    kind: "free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed tool class required to harvest a block, or `none` for anything-harvestable.",
    llm_authored: true,
    vocabulary_backed: true,
  },
  required_tool_tier: {
    kind: "free_text",
    pattern: NAMESPACED_TOKEN,
    description: "Vocabulary-backed minimum tool tier to harvest (e.g. `stone`, `iron`, `diamond`, or a pack/mod tier).",
    llm_authored: true,
    vocabulary_backed: true,
  },
  equip_slot: {
    kind: "free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed slot the item equips into when wearable.",
    llm_authored: true,
    vocabulary_backed: true,
  },
  dye_color: {
    kind: "enum",
    values: DYE_COLOR_VALUES,
    description: "One of the 16 vanilla dye colors, only when the item is explicitly dyed (wool, beds, candles, banners, stained_glass, terracotta, concrete, shulker_box, paint balls, colored cables, etc.).",
    deterministic: true,
    llm_authored: true,
  },
  rarity: {
    kind: "enum",
    values: RARITY_VALUES,
    description: "How hard the item is to obtain (not how frequently it's used).",
    deterministic: true,
    llm_authored: true,
  },
  emits_light: {
    kind: "boolean",
    description: "True if the item is a player-recognizable light source when placed or held. Examples: torches, lanterns, lamps, candles, glowing blocks, beacons, and similar modded light sources.",
    deterministic: true,
    llm_authored: true,
  },
  carry_frequency: {
    kind: "enum",
    values: CARRY_FREQUENCY_VALUES,
    description: "How often this item lives in a player's carried inventory (hotbar / main inventory) during normal play. Distinct from `rarity` (which is world-abundance) and from how often the item is used in crafting recipes — what we want here is whether opening a random player's inventory mid-play is likely to find this in their pockets. A crafting_table is touched constantly but placed once and not carried, so it's `occasional` here despite being heavily used. A pickaxe is `everyday` because the player carries it everywhere. cobblestone / sticks / oak_planks / iron_ingot / bread / torch / shovel / sword: `everyday`. stairs / slabs / fence_gates / building variants: `occasional` (placed not carried). chiseled / polished / cracked decorative variants: `rare`. dragon_egg / wither_skeleton_skull: `display_only`.",
    llm_authored: true,
  },

  // ===== Multi-value =====
  activity: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed gameplay activities this item participates in.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["mining", "cooking", "automation"],
  },
  origin: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed world, dimension, structure, biome, or acquisition source where the item comes from.",
    llm_authored: true,
    vocabulary_backed: true,
  },
  storage_categories: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed special container, slot, storage medium, or storage behavior that matters to a player.",
    llm_authored: true,
    vocabulary_backed: true,
  },
  spawn_interaction: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed ways this block/item affects mob spawning, movement, and survival.",
    llm_authored: true,
    vocabulary_backed: true,
  },
  combat_bonus: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed mob, boss, status-effect, or combat-mechanic bonuses this item grants beyond base damage.",
    llm_authored: true,
    vocabulary_backed: true,
  },
  environmental_property: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed world-physics and ambient-mechanic interactions such as fire, pistons, sculk, mobs, movement, and hazards.",
    llm_authored: true,
    vocabulary_backed: true,
  },
  transport_medium: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed medium this item moves, carries, transports, or transmits for logistics/automation purposes.",
    llm_authored: true,
    vocabulary_backed: true,
  },
  processing_in: {
    kind: "multi_free_text",
    pattern: NAMESPACED_TOKEN,
    description: "Processing verbs that consume this item as input (`smelting`, `crafting`, `modid:crushing`).",
    deterministic: true,
  },
  workflow: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed player-facing process or task context this item participates in.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["cooking", "mechanical_power", "steelmaking"],
  },
  workflow_role: {
    kind: "multi_free_text",
    pattern: SCOPED_VOCABULARY_VALUE_ID_PATTERN,
    description: "Scoped role the item plays inside a workflow, formatted as `<workflow>#<role>`.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["casting#input", "steelmaking#catalyst"],
  },
  used_at: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed station, machine, tool, or surface where the item is used.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["minecraft:furnace", "mechanical_press", "forge"],
  },
  food_category: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed food family such as fruit, grain, meat, dairy, prepared meals, or drinks.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["fruit", "grain", "prepared_meal"],
  },
  food_use: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed reason a player cares about this item in food contexts.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["eat_now", "meal_component", "animal_feed"],
  },
  preparation_state: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed preparation state such as raw, cooked, dried, pickled, fermented, or sealed.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["raw", "cooked", "fermented"],
  },
  material_process_stage: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed material/process-chain stage recognized in this pack, such as broad ore, dust, ingot, or pack-specific process forms.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["ore", "dust", "plate"],
  },
  stock_profile: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed inventory stock shape: bulk, small batch, singleton, tooling, reserve, display, overflow.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["bulk", "singleton", "tooling"],
  },
  container_state: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed container behavior or state, distinct from raw item capabilities.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["empty_container", "filled_container", "reusable_mold"],
  },
  equipment_effect: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed player-visible effect granted by carrying, wearing, or using the item.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["night_vision", "oxygen_supply", "flight"],
  },
  protection_context: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed hazard or environment this item protects against or is designed for.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["fire", "radiation", "vacuum"],
  },
  progression_stage: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed pack or mod progression stage, tier, age, voltage, dimension, or gate.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["early_survival", "low_voltage"],
  },
  loadout_context: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed trip, kit, or task context where a player would pack this item.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["mining_run", "building_project", "moon_trip"],
  },
  use_affordance: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed direct interaction verb or affordance, not generic recipe membership.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["place", "eat", "fill"],
  },
  primary_uses: {
    kind: "multi_free_text",
    pattern: /^.{1,80}$/,
    description: "Top 1–3 short phrases summarizing what a player picks this item up for. Human-readable, usually ≤40 chars each.",
    llm_authored: true,
    examples: ["crafting tools and armor", "building with iron blocks", "anvil repairs"],
  },
  organization_group: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed primary broad human storage home. Values are usable vocabulary homes, including explicit built-in seeds and review-watchlisted pack-specific splits; every classified item should receive one best home when this facet is targeted.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["metal_stock", "storage", "beekeeping"],
  },
  mod_subsystem: {
    kind: "multi_free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed identity-oriented subsystem within a mod (`trains`, `autocrafting`). Semantic/query evidence; not a main storage-section source.",
    llm_authored: true,
    vocabulary_backed: true,
    examples: ["trains", "autocrafting", "fission"],
  },
  produces_effect: {
    kind: "multi_free_text",
    pattern: NAMESPACED_ID,
    description: "Vocabulary-backed status-effect ids this item grants when consumed/applied (namespace-qualified).",
    llm_authored: true,
    vocabulary_backed: true,
  },
  multiblock_component_of: {
    kind: "multi_free_text",
    pattern: NAMESPACED_ID,
    description: "Vocabulary-backed named multiblocks this item is a required component of. Value is `<namespace>:<multiblock_id>`.",
    llm_authored: true,
    vocabulary_backed: true,
  },
  multiblock_role: {
    kind: "free_text",
    pattern: VOCABULARY_VALUE_ID_PATTERN,
    description: "Vocabulary-backed role this item plays inside a named multiblock or built structure.",
    llm_authored: true,
    vocabulary_backed: true,
  },
  biome: {
    kind: "multi_free_text",
    pattern: NAMESPACED_ID,
    description: "Vocabulary-backed biomes where this item is naturally found (for biome-specific sources only).",
    llm_authored: true,
    vocabulary_backed: true,
  },
  y_level_range: {
    kind: "enum",
    values: Y_LEVEL_RANGE_VALUES,
    description: "Coarse world-height bucket where the item is realistically encountered.",
    deterministic: true,
    llm_authored: true,
  },

  // ===== Booleans (derived) =====
  is_block_item: { kind: "boolean", description: "Placing this item places a block.", deterministic: true },
  is_stackable: { kind: "boolean", description: "Max stack size > 1.", deterministic: true },
  is_fuel: { kind: "boolean", description: "Burns as fuel in a furnace or similar fuel-consuming machine.", deterministic: true, llm_authored: true },
  has_durability: { kind: "boolean", description: "Damageable item.", deterministic: true, llm_authored: true },
  has_enchantments: {
    kind: "boolean",
    description: "Is or can be enchanted.",
    deterministic: true,
    llm_authored: true,
  },
  has_nbt_variation: {
    kind: "boolean",
    description: "Item state varies between stacks (shulker boxes, written books, bundles, banners).",
    deterministic: true,
    llm_authored: true,
  },
  is_creative_only: {
    kind: "boolean",
    description: "Only obtainable in creative (admin blocks, debug items).",
    llm_authored: true,
  },
};

// --- validation -----------------------------------------------------------

export interface ValidationIssue {
  facet: string;
  reason: string;
}

/**
 * Validate a single-value facet entry. Returns an issue or null. Callers emit
 * warnings rather than throwing so a broken rule doesn't drop the whole run.
 */
export function validateSingleValue(
  facet: string,
  value: unknown,
): ValidationIssue | null {
  const def = FACETS[facet];
  if (!def) return { facet, reason: "unknown facet" };
  switch (def.kind) {
    case "enum":
      if (typeof value !== "string") return { facet, reason: "expected string" };
      if (def.values && !def.values.includes(value)) {
        return { facet, reason: `value '${value}' not in enum` };
      }
      return null;
    case "free_text":
      if (typeof value !== "string") return { facet, reason: "expected string" };
      if (def.pattern && !def.pattern.test(value)) {
        return { facet, reason: `value '${value}' fails pattern ${def.pattern}` };
      }
      return null;
    case "boolean":
      if (typeof value !== "boolean") return { facet, reason: "expected boolean" };
      return null;
    case "numeric":
      if (typeof value !== "number") return { facet, reason: "expected number" };
      return null;
    default:
      return { facet, reason: `single-value kind mismatch (is ${def.kind})` };
  }
}

export function validateMultiValue(
  facet: string,
  values: unknown[],
): ValidationIssue | null {
  const def = FACETS[facet];
  if (!def) return { facet, reason: "unknown facet" };
  const pattern = def.pattern;
  const allowed = def.values;
  for (const v of values) {
    if (typeof v !== "string" && typeof v !== "number") {
      return { facet, reason: `multi value must be scalar, got ${typeof v}` };
    }
    if (def.kind === "multi_enum" && allowed && !allowed.includes(v as string)) {
      return { facet, reason: `value '${v}' not in multi_enum` };
    }
    if (def.kind === "multi_free_text" && pattern && !pattern.test(v as string)) {
      return { facet, reason: `value '${v}' fails pattern ${pattern}` };
    }
  }
  return null;
}
