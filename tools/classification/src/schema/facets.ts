/**
 * Facet registry v1 — a minimal TypeScript mirror of the facet catalog in
 * `docs/plans/item-classification.md` §"Facet kinds" and the per-facet sections
 * that follow. This is intentionally *not* the frozen `schema.v1.json` file (that
 * ships at milestone 1 formalization); it's the live vocabulary our Stage 2
 * rules and Stage 3 LLM prompts author against until the JSON freezes.
 *
 * The `description` field on each facet is the single line the LLM prompt uses
 * to explain the facet — keep it short and disambiguating. The `examples`
 * section seeds the prompt with value hints without showing the whole enum.
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
  /** True when stage 3 should attempt to fill this facet. */
  llm_authored?: boolean;
  /** True when stage 2 rules may fill this facet (partial coverage OK). */
  deterministic?: boolean;
  /** 1–3 short example values (for prompt priming on free-text or large enums). */
  examples?: readonly string[];
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

export const ROLE_VALUES = [
  "material", "natural_resource", "building_block", "decorative_block",
  "functional_block", "storage_block", "mechanism", "redstone_component",
  "tool", "weapon", "armor", "consumable", "ammunition", "transport",
  "container_portable", "utility", "curiosity", "upgrade", "trophy", "admin",
] as const;

export const ACTIVITY_VALUES = [
  "mining", "combat", "farming", "building", "decorating", "redstone",
  "automation", "logistics", "storage_management", "exploration",
  "brewing", "enchanting", "trading", "fishing", "magic",
  "power_generation", "transportation",
] as const;

export const FREQUENCY_VALUES = [
  "everyday", "frequent", "occasional", "rare", "display_only",
] as const;

export const FLAVOR_VALUES = [
  "plain", "variant", "fancy", "ominous", "ancient", "mystical",
  "mechanical", "natural", "colored",
] as const;

export const PALETTE_VALUES = [
  "teal", "turquoise", "aqua", "indigo", "violet", "maroon", "amber",
  "olive", "sage", "coral", "ivory", "mint",
  "gold", "silver", "copper_bright", "copper_oxidized", "iron_dark",
  "netherite_dark", "iridescent", "glossy", "matte",
  "wood_light", "wood_medium", "wood_dark", "wood_red", "wood_pale",
  "leaf_green", "earthy", "sandy", "muddy",
  "warm", "cool", "pastel", "vivid", "muted", "dark", "light",
  "translucent", "opaque_glass", "crystal", "glowing", "emissive",
] as const;

export const STORAGE_CATEGORY_VALUES = [
  "standard", "fluid", "gas", "energy", "ae_storage",
  "backpack_restricted", "curio", "pedestal", "jukebox",
] as const;

export const SPAWN_INTERACTION_VALUES = [
  "blocks_monster_spawn", "allows_spawning", "damages_entities",
  "mob_transport", "mob_launcher", "suffocates_mobs", "repels_mobs",
  "attracts_mobs",
  // sonnet-v4 canary
  "spawns_linked_mob",
] as const;

export const COMBAT_BONUS_VALUES = [
  "undead", "arthropod", "aquatic", "illager", "piglin",
  "boss:ender_dragon", "boss:warden", "boss:wither", "boss:elder_guardian",
  "inflicts_poison", "inflicts_slowness", "inflicts_weakness", "inflicts_wither",
  "bonus_in_water", "bonus_in_daylight",
  // sonnet-v4 canary
  "fall_bonus_damage",
  // vanilla v1 canary
  "disables_blocking",  // axes — disable shield 5s on hit
  "inflicts_glowing",   // spectral arrow
] as const;

export const ENVIRONMENTAL_PROPERTY_VALUES = [
  "fireproof", "lava_safe", "burnable", "ignitable_by_fire",
  "blast_resistant_low", "blast_resistant_high", "blast_resistant_max",
  "piston_movable", "piston_immovable", "piston_sticky",
  "sculk_silent", "sculk_noisy", "warden_distracting",
  "piglin_pacifying", "piglin_barters_with", "piglin_aggroes_on_open",
  "conducts_lightning", "melts_in_powdered_snow", "frost_walker_triggers",
  "slippery", "slows_walking", "bounces",
  "emits_light", "emits_light_underwater",
  "waterlogs", "floats", "sinks",
  // sonnet-v4 canary additions
  "gravity_affected",       // anvil/sand/gravel — fall when unsupported
  "piglin_loved",           // gold items — piglins admire/pick up
  "oxidizes_over_time",     // unwaxed copper variants
  "item_blast_proof",       // item entity survives explosions (nether_star, netherite)
  "freeze_immune_when_worn", // leather armor — prevents powder snow freeze
  "powder_snow_walkable",   // leather boots — walk on powder snow
  // vanilla v1 canary additions
  "sustains_fire",          // netherrack — fire never extinguishes
  "piglin_repellent",       // soul torch/lantern/campfire — piglins flee
  "trample_sensitive",      // turtle eggs, farmland — break under entity weight
  "climbable",              // ladder, vines, scaffolding — player ascends
] as const;

export const TRANSPORT_MEDIUM_VALUES = [
  "item", "fluid", "gas", "energy", "signal", "player", "mob",
] as const;

export const MULTIBLOCK_ROLE_VALUES = [
  "controller", "wall", "casing", "port", "valve", "power_access", "core",
] as const;

export const Y_LEVEL_RANGE_VALUES = [
  "sky", "surface", "underground", "deep", "nether_surface", "end_islands",
] as const;

export const MATERIAL_FAMILY_VALUES = [
  // woods
  "wood_oak", "wood_birch", "wood_spruce", "wood_dark_oak", "wood_jungle",
  "wood_acacia", "wood_mangrove", "wood_cherry", "wood_bamboo", "wood_pale_oak",
  "wood_crimson", "wood_warped",
  // stones
  "stone", "granite", "diorite", "andesite", "cobblestone", "deepslate",
  "tuff", "calcite", "basalt", "blackstone", "sandstone", "red_sandstone",
  "end_stone", "purpur", "prismarine", "nether_bricks",
  // metals + metal-likes
  "iron", "gold", "copper", "netherite",
  "diamond", "emerald", "lapis", "amethyst", "quartz", "certus_quartz", "redstone",
  // organics
  "wool", "leather", "bone", "slime", "honey", "honeycomb", "scute",
  // borderlines
  "dripstone", "netherrack",
] as const;

export const FORM_VALUES = [
  "raw", "ore", "ingot", "nugget", "plate", "sheet", "rod", "gem", "dust",
  "shard", "crystal",
  "storage_block", "whole_block",
  "stairs", "slab", "wall", "fence", "fence_gate", "door", "trapdoor",
  "pane", "pillar", "pressure_plate", "button", "ladder", "bars",
  "log", "stripped_log", "wood",
  "carpet", "bed", "banner", "sign", "hanging_sign", "head", "pot",
  "candle", "torch", "lantern",
  "tool", "weapon", "armor_piece",
  "food_raw", "food_cooked", "potion", "bottle",
  "bucket", "projectile", "vehicle",
  "seed", "sapling", "bulb",
  "special",
] as const;

export const DYE_COLOR_VALUES = [
  "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
  "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red",
  "black",
] as const;

export const REQUIRED_TOOL_VALUES = [
  "none", "pickaxe", "axe", "shovel", "hoe", "shears", "any_tool",
] as const;

export const EQUIP_SLOT_VALUES = [
  "head", "chest", "legs", "feet", "main_hand", "off_hand",
  "saddle", "llama_carpet",
] as const;

export const ORIGIN_VALUES = [
  "overworld_surface", "overworld_cave", "overworld_ocean", "deep_dark",
  "nether", "nether_fortress", "bastion",
  "end", "end_city", "end_ship",
  "stronghold", "woodland_mansion", "ancient_city", "ruined_portal",
  "pillager_outpost", "village", "trial_chamber", "desert_temple",
  "jungle_temple", "ocean_monument", "mineshaft",
  "trading", "mob_drop", "archaeology_site", "sniffer_garden",
  "crafted_only",
  // sonnet-v4 canary
  "fishing",
  // vanilla v1 canary
  "creative_only",  // spawn eggs, debug items, command blocks — no survival origin
  "brewing",        // splash/lingering potions, tipped arrows produced via brewing
] as const;

const RARITY_VALUES = ["abundant", "common", "uncommon", "rare", "unique"] as const;

const NAMESPACED_TOKEN = /^[a-z0-9_]+(:[a-z0-9_]+)?$/;
const NAMESPACED_ID = /^[a-z0-9_.-]+:[a-z0-9_/.-]+$/;

// --- facet catalog --------------------------------------------------------

export const FACETS: Record<string, FacetDef> = {
  // ===== Identity / simple =====
  mod_namespace: {
    kind: "free_text",
    pattern: /^[a-z0-9_.-]+$/,
    description: "Item id namespace (`minecraft`, `create`, `mekanism`).",
    deterministic: true,
  },
  role: {
    kind: "enum",
    values: ROLE_VALUES,
    description: "The fundamental kind of thing the item is. Every item has exactly one role.",
    llm_authored: true,
  },
  material_family: {
    kind: "free_text",
    pattern: NAMESPACED_TOKEN,
    description: "Primary material the item is made of (e.g. `iron`, `wood_oak`, `wool`).",
    deterministic: true,
    llm_authored: true,
  },
  material_secondary: {
    kind: "multi_free_text",
    pattern: NAMESPACED_TOKEN,
    description: "Secondary materials for composite items (e.g. `wood_oak` for a brass casing with a wood frame).",
    llm_authored: true,
  },
  form: {
    kind: "enum",
    values: FORM_VALUES,
    description: "Shape or form factor (`ingot`, `stairs`, `tool`, `potion`).",
    deterministic: true,
  },
  tier: {
    kind: "free_text",
    pattern: NAMESPACED_TOKEN,
    description: "Progression tier for tools / weapons / armor / tiered materials. Vanilla: `wooden`/`stone`/`iron`/`diamond`/`netherite`; mods use their own vocabulary.",
    llm_authored: true,
  },
  required_tool: {
    kind: "enum",
    values: REQUIRED_TOOL_VALUES,
    description: "Tool required to harvest a block, or `none` for anything-harvestable.",
    deterministic: true,
  },
  required_tool_tier: {
    kind: "free_text",
    pattern: NAMESPACED_TOKEN,
    description: "Minimum tool tier to harvest (e.g. `stone`, `iron`, `diamond`).",
    deterministic: true,
  },
  equip_slot: {
    kind: "enum",
    values: EQUIP_SLOT_VALUES,
    description: "Slot the item equips into when wearable.",
    deterministic: true,
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
  frequency: {
    kind: "enum",
    values: FREQUENCY_VALUES,
    description: "How often a player realistically *uses* this item once obtained.",
    llm_authored: true,
  },

  // ===== Multi-value =====
  activity: {
    kind: "multi_enum",
    values: ACTIVITY_VALUES,
    description: "Gameplay activities this item participates in.",
    llm_authored: true,
  },
  flavor: {
    kind: "multi_enum",
    values: FLAVOR_VALUES,
    description: "Aesthetic / qualitative attributes. `plain` / `variant` / `fancy` / `ominous` / `mechanical` / `natural` / `colored`.",
    llm_authored: true,
  },
  palette: {
    kind: "multi_enum",
    values: PALETTE_VALUES,
    description: "Broader visual descriptors for items that *read as* a color/finish beyond the 16 dyes.",
    llm_authored: true,
    examples: ["teal", "copper_oxidized", "wood_dark", "warm", "glowing"],
  },
  origin: {
    kind: "multi_enum",
    values: ORIGIN_VALUES,
    description: "Where the item is sourced from in the world.",
    deterministic: true,
    llm_authored: true,
  },
  storage_categories: {
    kind: "multi_enum",
    values: STORAGE_CATEGORY_VALUES,
    description: "Container slot kinds that can legitimately hold this item.",
    llm_authored: true,
  },
  spawn_interaction: {
    kind: "multi_enum",
    values: SPAWN_INTERACTION_VALUES,
    description: "How this block/item affects mob spawning, movement, and survival.",
    llm_authored: true,
  },
  combat_bonus: {
    kind: "multi_enum",
    values: COMBAT_BONUS_VALUES,
    description: "Mob / boss / status-effect bonuses this weapon grants beyond its base damage.",
    llm_authored: true,
  },
  environmental_property: {
    kind: "multi_enum",
    values: ENVIRONMENTAL_PROPERTY_VALUES,
    description: "Interaction with world physics and ambient mechanics (fire, piston, sculk, piglins, movement).",
    llm_authored: true,
  },
  transport_medium: {
    kind: "multi_enum",
    values: TRANSPORT_MEDIUM_VALUES,
    description: "What the item moves for logistics/automation purposes (items, fluids, gas, energy, signal, player, mob).",
    llm_authored: true,
  },
  processing_in: {
    kind: "multi_free_text",
    pattern: NAMESPACED_TOKEN,
    description: "Processing verbs that consume this item as input (`smelting`, `crafting`, `create:crushing`).",
    deterministic: true,
  },
  primary_uses: {
    kind: "multi_free_text",
    pattern: /^.{1,80}$/,
    description: "Top 3–5 short phrases summarizing what a player picks this item up for. Human-readable, ≤40 chars each.",
    llm_authored: true,
    examples: ["crafting tools and armor", "building with iron blocks", "anvil repairs"],
  },
  mod_subsystem: {
    kind: "multi_free_text",
    pattern: NAMESPACED_TOKEN,
    description: "Named sub-system within a mod (`create:trains`, `ae2:autocrafting`, `mekanism:fission`). Free-text, <namespace>:<subsystem>.",
    llm_authored: true,
  },
  produces_effect: {
    kind: "multi_free_text",
    pattern: NAMESPACED_ID,
    description: "Status-effect ids this item grants when consumed/applied (namespace-qualified).",
    deterministic: true,
  },
  multiblock_component_of: {
    kind: "multi_free_text",
    pattern: NAMESPACED_ID,
    description: "Named multiblocks this item is a required component of. Value is `<namespace>:<multiblock_id>`.",
    llm_authored: true,
  },
  multiblock_role: {
    kind: "enum",
    values: MULTIBLOCK_ROLE_VALUES,
    description: "Role within a multiblock: `controller` / `wall` / `casing` / `port` / `valve` / `power_access` / `core`.",
    llm_authored: true,
  },
  biome: {
    kind: "multi_free_text",
    pattern: NAMESPACED_ID,
    description: "Biomes where this item is naturally found (for biome-specific sources only).",
    deterministic: true,
  },
  y_level_range: {
    kind: "enum",
    values: Y_LEVEL_RANGE_VALUES,
    description: "Coarse world-height bucket where the item is realistically encountered.",
    deterministic: true,
  },

  // ===== Booleans (derived) =====
  is_block_item: { kind: "boolean", description: "Placing this item places a block.", deterministic: true },
  is_stackable: { kind: "boolean", description: "Max stack size > 1.", deterministic: true },
  is_fuel: { kind: "boolean", description: "Burns in a furnace.", deterministic: true },
  has_durability: { kind: "boolean", description: "Damageable item.", deterministic: true },
  has_enchantments: {
    kind: "boolean",
    description: "Is or can be enchanted.",
    deterministic: true,
  },
  has_nbt_variation: {
    kind: "boolean",
    description: "Item state varies between stacks (shulker boxes, written books, bundles, banners).",
    deterministic: true,
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
