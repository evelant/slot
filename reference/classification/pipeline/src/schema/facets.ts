/**
 * Facet registry v1 — a minimal TypeScript mirror of the facet catalog in
 * `docs/plans/item-classification.md` §"Facet kinds" and the per-facet sections
 * that follow. This is intentionally *not* the frozen `schema.v1.json` file (that
 * ships at milestone 1 formalization); it's the live vocabulary our Stage 2
 * rules author against until the JSON freezes.
 *
 * Any value a rule emits must appear in the facet's `values` list (for enum-kind
 * facets). The `validateFacetEntry` helper catches typos at write time rather
 * than waiting for the wire-format validator to reject them downstream.
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
  /** Closed value list for enum / multi_enum. */
  values?: readonly string[];
  /** Regex for free_text / multi_free_text. */
  pattern?: RegExp;
  /** Numeric unit, optional. */
  unit?: string;
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

/**
 * Values the plan currently lists. Mod-specific families like `create:brass_casing`
 * expand this vocabulary at runtime; for the vanilla pass we only ever emit
 * values from this list.
 */
export const MATERIAL_FAMILY_VALUES = [
  // woods (12)
  "wood_oak", "wood_birch", "wood_spruce", "wood_dark_oak", "wood_jungle",
  "wood_acacia", "wood_mangrove", "wood_cherry", "wood_bamboo", "wood_pale_oak",
  "wood_crimson", "wood_warped",
  // stones
  "stone", "granite", "diorite", "andesite", "cobblestone", "deepslate",
  "tuff", "calcite", "basalt", "blackstone", "sandstone", "red_sandstone",
  "end_stone", "purpur", "prismarine", "nether_bricks",
  // metals + metal-likes
  "iron", "gold", "copper", "netherite",
  "diamond", "emerald", "lapis", "amethyst", "quartz", "redstone",
  // organics
  "wool", "leather", "bone", "slime", "honey", "scute",
  // borderlines
  "dripstone",
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
] as const;

export const ROLE_VALUES = [
  "material", "natural_resource", "building_block", "decorative_block",
  "functional_block", "storage_block", "mechanism", "redstone_component",
  "tool", "weapon", "armor", "consumable", "ammunition", "transport",
  "container_portable", "utility", "curiosity", "upgrade", "trophy", "admin",
] as const;

export const FACETS: Record<string, FacetDef> = {
  // Identity / simple
  mod_namespace: { kind: "free_text", pattern: /^[a-z0-9_.-]+$/ },
  role: { kind: "enum", values: ROLE_VALUES },
  material_family: { kind: "free_text", pattern: /^[a-z0-9_]+(:[a-z0-9_]+)?$/ },
  material_secondary: { kind: "multi_free_text", pattern: /^[a-z0-9_]+(:[a-z0-9_]+)?$/ },
  form: { kind: "enum", values: FORM_VALUES },
  tier: { kind: "free_text", pattern: /^[a-z0-9_]+(:[a-z0-9_]+)?$/ },
  required_tool: { kind: "enum", values: REQUIRED_TOOL_VALUES },
  required_tool_tier: { kind: "free_text", pattern: /^[a-z0-9_]+(:[a-z0-9_]+)?$/ },
  equip_slot: { kind: "enum", values: EQUIP_SLOT_VALUES },
  dye_color: { kind: "enum", values: DYE_COLOR_VALUES },
  rarity: { kind: "enum", values: ["abundant", "common", "uncommon", "rare", "unique"] },

  // Multi
  origin: { kind: "multi_enum", values: ORIGIN_VALUES },
  activity: {
    kind: "multi_enum",
    values: [
      "mining", "combat", "farming", "building", "decorating", "redstone",
      "automation", "logistics", "storage_management", "exploration",
      "brewing", "enchanting", "trading", "fishing", "magic",
      "power_generation", "transportation",
    ],
  },
  // processing_in takes namespaced verbs like "smelting" (vanilla short form)
  // or "create:crushing" (mod-specific). Stored as multi_free_text so we don't
  // need to enumerate every mod's categories.
  processing_in: { kind: "multi_free_text", pattern: /^[a-z0-9_]+(:[a-z0-9_]+)?$/ },
  primary_uses: { kind: "multi_free_text", pattern: /^.{1,80}$/ },
  storage_categories: {
    kind: "multi_enum",
    values: [
      "standard", "fluid", "gas", "energy", "ae_storage", "backpack_restricted",
      "curio", "pedestal", "jukebox",
    ],
  },
  produces_effect: { kind: "multi_free_text", pattern: /^[a-z0-9_.-]+:[a-z0-9_/.-]+$/ },

  // Booleans (derived)
  is_block_item: { kind: "boolean" },
  is_stackable: { kind: "boolean" },
  is_fuel: { kind: "boolean" },
  has_durability: { kind: "boolean" },
  has_enchantments: { kind: "boolean" },
  has_nbt_variation: { kind: "boolean" },
  is_creative_only: { kind: "boolean" },
};

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
