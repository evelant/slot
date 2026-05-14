import type { Rule } from "../types.ts";

/**
 * Assign `material_family` using three signals, in order of strength:
 *   1. Tag membership (high confidence — `minecraft:iron_tool_materials`,
 *      `minecraft:oak_logs`, `minecraft:wool`, etc.)
 *   2. Model-chain keywords where a wood prefix is structural (`block/stairs`
 *      doesn't tell us the wood, so this is weak and skipped).
 *   3. Id prefix fallback (`oak_*` → `wood_oak`, `deepslate_*` → `deepslate`).
 *
 * Rule fires only when a mapping is confident. Ambiguous cases (e.g. a generic
 * `_slab` with no material prefix) emit nothing; Stage 3 will decide.
 *
 * Tag-based assignments take precedence over id patterns — the first emitted
 * output in the rule runner is kept as `replace` (single-value). We emit only
 * one entry here to make that ordering unambiguous.
 */

const TAG_TO_MATERIAL: Record<string, string> = {
  "minecraft:iron_tool_materials": "iron",
  "minecraft:gold_tool_materials": "gold",
  "minecraft:copper_tool_materials": "copper",
  "minecraft:diamond_tool_materials": "diamond",
  "minecraft:netherite_tool_materials": "netherite",
  "minecraft:stone_tool_materials": "stone",
  // INTENTIONALLY no `minecraft:wooden_tool_materials` entry: that tag is
  // shared across every plank type (oak, birch, spruce, …) so emitting
  // wood_oak from it labels acacia_planks etc. as oak. Vanilla v1 canary
  // surfaced this on 11 plank items. Each plank's id has a wood-species
  // prefix that the ID_PREFIX_TO_FAMILY pass below catches correctly.
  "minecraft:iron_ores": "iron",
  "minecraft:gold_ores": "gold",
  "minecraft:copper_ores": "copper",
  "minecraft:diamond_ores": "diamond",
  "minecraft:emerald_ores": "emerald",
  "minecraft:lapis_ores": "lapis",
  "minecraft:redstone_ores": "redstone",
  "minecraft:coal_ores": "coal",
  "minecraft:repairs_leather_armor": "leather",
  "minecraft:repairs_chain_armor": "iron",
  "minecraft:repairs_iron_armor": "iron",
  "minecraft:repairs_gold_armor": "gold",
  "minecraft:repairs_copper_armor": "copper",
  "minecraft:repairs_diamond_armor": "diamond",
  "minecraft:repairs_netherite_armor": "netherite",
  "minecraft:wool": "wool",
  "minecraft:wool_carpets": "wool",
};

/** Exact-match wood log tags → wood_<type>. */
const LOG_TAG_TO_WOOD: Record<string, string> = {
  "minecraft:oak_logs": "wood_oak",
  "minecraft:birch_logs": "wood_birch",
  "minecraft:spruce_logs": "wood_spruce",
  "minecraft:jungle_logs": "wood_jungle",
  "minecraft:acacia_logs": "wood_acacia",
  "minecraft:dark_oak_logs": "wood_dark_oak",
  "minecraft:pale_oak_logs": "wood_pale_oak",
  "minecraft:mangrove_logs": "wood_mangrove",
  "minecraft:cherry_logs": "wood_cherry",
  "minecraft:bamboo_blocks": "wood_bamboo",
  "minecraft:crimson_stems": "wood_crimson",
  "minecraft:warped_stems": "wood_warped",
};

/**
 * Tools / weapons / armor follow a strict `<material>_<kind>` naming pattern
 * in vanilla, and the name is the authoritative material signal — the
 * `*_tool_materials` tags live on the ingots/materials, not on the tools.
 * We detect this first because the prefix-list below would also match the
 * `_stairs` / `_slab` variants but with different intent.
 */
const TOOL_ARMOR_SUFFIXES = [
  "_pickaxe", "_sword", "_axe", "_shovel", "_hoe",
  "_helmet", "_chestplate", "_leggings", "_boots",
] as const;

const TOOL_ARMOR_MATERIAL_PREFIX: Record<string, string> = {
  // `wooden_*` tools accept any plank in the recipe, not specifically oak;
  // emit the generic `wood` family rather than `wood_oak`. Sonnet-v4 canary
  // flagged this — the previous wood_oak claim was wrong for half the times
  // a player would craft a wooden_pickaxe out of birch/spruce/etc. planks.
  wooden: "wood",
  stone: "stone",
  iron: "iron",
  golden: "gold",
  diamond: "diamond",
  netherite: "netherite",
  copper: "copper",
  leather: "leather", // armor only but the fallthrough is fine
  chainmail: "iron", // repaired with iron via minecraft:repairs_chain_armor
  turtle: "scute", // turtle_helmet
};

const ORE_HOST_PREFIXES = [
  "black_sand",
  "brown_sand",
  "green_sand",
  "yellow_sand",
  "red_sand",
  "moon_deepslate",
  "moon_stone",
  "mars_stone",
  "mercury_stone",
  "glacio_stone",
  "deepslate",
  "andesite",
  "granite",
  "diorite",
  "basalt",
  "gabbro",
  "rhyolite",
  "dacite",
  "chalk",
  "chert",
  "claystone",
  "conglomerate",
  "dolomite",
  "dripstone",
  "gneiss",
  "limestone",
  "marble",
  "phyllite",
  "moon",
  "mars",
  "mercury",
  "venus",
  "glacio",
] as const;

const ORE_GRADE_PREFIXES = ["small_native_", "small_", "poor_", "normal_", "rich_", "native_"] as const;
const ORE_PROCESS_PREFIXES = [
  "dusty_raw_",
  "poor_raw_",
  "rich_raw_",
  "crushed_",
  "purified_",
  "impure_",
  "pure_",
  "raw_",
] as const;

const COMMON_MATERIAL_TAG_ROOTS = new Set([
  "dense_plates",
  "double_ingots",
  "dusts",
  "gems",
  "glass",
  "hot_ingots",
  "impure_dusts",
  "ingots",
  "metal_item",
  "metal_items",
  "nuggets",
  "ores",
  "plates",
  "poor_raw_materials",
  "pure_dusts",
  "raw_ore_blocks",
  "raw_materials",
  "rich_raw_materials",
  "rings",
  "rods",
  "sheets",
  "small_dusts",
  "small_springs",
  "springs",
  "storage_blocks",
  "tiny_dusts",
  "wires",
]);

const DISPLAY_MATERIAL_SUFFIXES = [
  " Crushing Wheel",
  " Double Ingot",
  " Greenhouse Door",
  " Greenhouse Panel Roof",
  " Greenhouse Panel Wall",
  " Greenhouse Port",
  " Greenhouse Roof",
  " Greenhouse Roof Top",
  " Greenhouse Trapdoor",
  " Greenhouse Wall",
  " Gearbox",
  " Knife Blade",
  " Mechanical Mixer",
  " Mechanical Saw",
  " Millstone",
  " Mortar",
  " Plate",
  " Sheet",
  " Toe Hiking Boots",
  " Train Hull",
];

const DISPLAY_STONE_FORM_PREFIXES = [
  "Polished Cut ",
  "Cut ",
  "Small ",
];

const DISPLAY_STONE_FORM_SUFFIXES = [
  " Brick Stairs",
  " Brick Slab",
  " Brick Wall",
  " Bricks",
  " Brick",
  " Stairs",
  " Slab",
  " Wall",
  " Pillar",
];

/**
 * Per-namespace prefix overrides. Checked BEFORE the generic
 * `ID_PREFIX_TO_FAMILY` table so mod-specific naming conventions win over
 * the vanilla defaults.
 *
 * AE2 canary surfaced this: in the `ae2:` namespace the bare token "quartz"
 * refers to **certus quartz** (display names confirm: "Certus Quartz Block",
 * "Smooth Certus Quartz Stairs"), not vanilla nether quartz.
 */
const NAMESPACE_PREFIX_OVERRIDES: Record<string, Array<[prefix: string, family: string]>> = {
  ae2: [
    ["quartz_", "certus_quartz"],
    ["chiseled_quartz_", "certus_quartz"],
    ["smooth_quartz_", "certus_quartz"],
  ],
};

/** Id prefix → family. Checked last; wins only when no tag matched. */
const ID_PREFIX_TO_FAMILY: Array<[prefix: string, family: string]> = [
  ["oak_", "wood_oak"],
  ["birch_", "wood_birch"],
  ["spruce_", "wood_spruce"],
  ["jungle_", "wood_jungle"],
  ["acacia_", "wood_acacia"],
  ["dark_oak_", "wood_dark_oak"],
  ["pale_oak_", "wood_pale_oak"],
  ["mangrove_", "wood_mangrove"],
  ["cherry_", "wood_cherry"],
  ["bamboo_", "wood_bamboo"],
  ["crimson_", "wood_crimson"],
  ["warped_", "wood_warped"],
  ["stripped_oak_", "wood_oak"],
  ["stripped_birch_", "wood_birch"],
  ["stripped_spruce_", "wood_spruce"],
  ["stripped_jungle_", "wood_jungle"],
  ["stripped_acacia_", "wood_acacia"],
  ["stripped_dark_oak_", "wood_dark_oak"],
  ["stripped_pale_oak_", "wood_pale_oak"],
  ["stripped_mangrove_", "wood_mangrove"],
  ["stripped_cherry_", "wood_cherry"],
  ["stripped_crimson_", "wood_crimson"],
  ["stripped_warped_", "wood_warped"],
  ["deepslate_", "deepslate"],
  ["granite_", "granite"],
  ["diorite_", "diorite"],
  ["andesite_", "andesite"],
  ["cobblestone_", "cobblestone"],
  ["blackstone_", "blackstone"],
  ["basalt_", "basalt"],
  ["tuff_", "tuff"],
  ["calcite_", "calcite"],
  ["sandstone_", "sandstone"],
  ["red_sandstone_", "red_sandstone"],
  ["end_stone_", "end_stone"],
  ["purpur_", "purpur"],
  ["prismarine_", "prismarine"],
  ["nether_brick_", "nether_bricks"],
  ["polished_deepslate_", "deepslate"],
  ["polished_granite_", "granite"],
  ["polished_diorite_", "diorite"],
  ["polished_andesite_", "andesite"],
  ["polished_blackstone_", "blackstone"],
  ["polished_basalt_", "basalt"],
  ["polished_tuff_", "tuff"],
  ["smooth_stone_", "stone"],
  ["smooth_sandstone_", "sandstone"],
  ["smooth_red_sandstone_", "red_sandstone"],
  ["smooth_quartz_", "quartz"],
  ["chiseled_deepslate_", "deepslate"],
  ["chiseled_sandstone_", "sandstone"],
  ["chiseled_red_sandstone_", "red_sandstone"],
  ["chiseled_quartz_", "quartz"],
  ["cut_copper_", "copper"],
  ["oxidized_copper_", "copper"],
  ["weathered_copper_", "copper"],
  ["exposed_copper_", "copper"],
  ["waxed_", "copper"], // weak — many waxed_* are copper variants; narrows fine via tag first
  ["quartz_", "quartz"],
  ["honey_", "honey"],
];

/** Exact id matches where the prefix heuristic would miss or mislead. */
const EXACT_ID_TO_FAMILY: Record<string, string> = {
  "minecraft:iron_ingot": "iron",
  "minecraft:iron_nugget": "iron",
  "minecraft:iron_block": "iron",
  "minecraft:gold_ingot": "gold",
  "minecraft:gold_nugget": "gold",
  "minecraft:gold_block": "gold",
  "minecraft:copper_ingot": "copper",
  "minecraft:copper_block": "copper",
  "minecraft:cut_copper": "copper",
  "minecraft:exposed_copper": "copper",
  "minecraft:exposed_cut_copper": "copper",
  "minecraft:oxidized_copper": "copper",
  "minecraft:oxidized_cut_copper": "copper",
  "minecraft:waxed_copper_block": "copper",
  "minecraft:waxed_cut_copper": "copper",
  "minecraft:waxed_exposed_copper": "copper",
  "minecraft:waxed_exposed_cut_copper": "copper",
  "minecraft:waxed_oxidized_copper": "copper",
  "minecraft:waxed_oxidized_cut_copper": "copper",
  "minecraft:waxed_weathered_copper": "copper",
  "minecraft:waxed_weathered_cut_copper": "copper",
  "minecraft:weathered_copper": "copper",
  "minecraft:weathered_cut_copper": "copper",
  "minecraft:netherite_ingot": "netherite",
  "minecraft:netherite_scrap": "netherite",
  "minecraft:netherite_block": "netherite",
  "minecraft:diamond": "diamond",
  "minecraft:diamond_block": "diamond",
  "minecraft:emerald": "emerald",
  "minecraft:emerald_block": "emerald",
  "minecraft:lapis_lazuli": "lapis",
  "minecraft:lapis_block": "lapis",
  "minecraft:redstone": "redstone",
  "minecraft:redstone_block": "redstone",
  "minecraft:amethyst_shard": "amethyst",
  "minecraft:amethyst_block": "amethyst",
  "minecraft:quartz": "quartz",
  "minecraft:quartz_block": "quartz",
  "minecraft:raw_iron": "iron",
  "minecraft:raw_iron_block": "iron",
  "minecraft:raw_gold": "gold",
  "minecraft:raw_gold_block": "gold",
  "minecraft:raw_copper": "copper",
  "minecraft:raw_copper_block": "copper",
  "minecraft:leather": "leather",
  "minecraft:bone": "bone",
  "minecraft:bone_block": "bone",
  "minecraft:bone_meal": "bone",
  "minecraft:slime_ball": "slime",
  "minecraft:slime_block": "slime",
  "minecraft:honey_bottle": "honey",
  "minecraft:honey_block": "honey",
  // honeycomb is crafted with wax, distinct from honey (the food item).
  // Vanilla v1 canary catch — `honeycomb_block` was previously labeled honey.
  "minecraft:honeycomb": "honeycomb",
  "minecraft:honeycomb_block": "honeycomb",
  "minecraft:turtle_scute": "scute",
  "minecraft:armadillo_scute": "scute",
  "minecraft:glass": "glass",
  "minecraft:tinted_glass": "glass",
  "minecraft:stone": "stone",
  "minecraft:cobblestone": "cobblestone",
  "minecraft:deepslate": "deepslate",
  "minecraft:cobbled_deepslate": "deepslate",
  // Nylium is netherrack-based terrain, not wood — the crimson_/warped_
  // id-prefix rules misfire without an explicit override. Vanilla v1 canary.
  "minecraft:crimson_nylium": "netherrack",
  "minecraft:warped_nylium": "netherrack",
  "minecraft:granite": "granite",
  "minecraft:diorite": "diorite",
  "minecraft:andesite": "andesite",
  "minecraft:tuff": "tuff",
  "minecraft:calcite": "calcite",
  "minecraft:basalt": "basalt",
  "minecraft:blackstone": "blackstone",
  "minecraft:sandstone": "sandstone",
  "minecraft:red_sandstone": "red_sandstone",
  "minecraft:end_stone": "end_stone",
  "minecraft:end_stone_bricks": "end_stone",
  "minecraft:purpur_block": "purpur",
  "minecraft:prismarine": "prismarine",
  "minecraft:prismarine_shard": "prismarine",
  "minecraft:prismarine_crystals": "prismarine",
  "minecraft:nether_bricks": "nether_bricks",
  "minecraft:dripstone_block": "dripstone",
  "minecraft:pointed_dripstone": "dripstone",
};

function materialFamilyFromOrePath(path: string): string | null {
  const pathParts = path.split("/");
  const leaf = pathParts.at(-1) ?? path;

  if (path.startsWith("ore/")) {
    return stripOreGradePrefix(pathParts[1] ?? leaf);
  }

  if (!leaf.endsWith("_ore")) return null;
  let base = leaf.slice(0, -"_ore".length);
  if (!base) return null;

  for (const host of ORE_HOST_PREFIXES) {
    const prefix = `${host}_`;
    if (base.startsWith(prefix) && base.length > prefix.length) {
      base = base.slice(prefix.length);
      break;
    }
  }

  return stripOreProcessPrefix(base);
}

function materialFamilyFromBloomPath(path: string): string | null {
  const leaf = path.split("/").at(-1) ?? path;
  const match = /^(?:raw_|refined_)?([a-z0-9_]+)_bloom$/.exec(leaf);
  return match?.[1] ?? null;
}

function materialFamilyFromStructuredMetalPath(path: string): string | null {
  const parts = path.split("/");
  if (parts.length >= 3 && parts[0] === "metal") {
    const material = parts.at(-1) ?? "";
    return /^[a-z0-9_]+$/.test(material) ? material : null;
  }
  return null;
}

function materialFamilyFromStructuredWoodPath(path: string): string | null {
  const parts = path.split("/");
  if (parts[0] !== "wood") return null;

  const material = parts.length >= 3
    ? parts.at(-1)
    : materialPrefixFromWoodLeaf(parts[1] ?? "");
  if (!material || !/^[a-z0-9_]+$/.test(material)) return null;
  return `wood_${material}`;
}

const STRUCTURED_WOOD_LEAF_SUFFIXES = [
  "_roofing",
];

function materialPrefixFromWoodLeaf(leaf: string): string | null {
  for (const suffix of STRUCTURED_WOOD_LEAF_SUFFIXES) {
    if (!leaf.endsWith(suffix) || leaf.length <= suffix.length) continue;
    const material = leaf.slice(0, -suffix.length);
    return material || null;
  }
  return null;
}

function materialFamilyFromCommonMaterialTag(tag: string): string | null {
  const path = tag.split(":", 2)[1] ?? tag;
  const parts = path.split("/");
  const root = parts[0] ?? "";
  if (root === "glass") {
    return "glass";
  }
  if (!COMMON_MATERIAL_TAG_ROOTS.has(root) || parts.length !== 2) {
    return null;
  }

  const rawMaterial = parts[1] ?? "";
  if (!/^[a-z0-9_]+$/.test(rawMaterial)) {
    return null;
  }
  return stripOreProcessPrefix(stripOreGradePrefix(rawMaterial) ?? rawMaterial);
}

function materialFamilyFromDisplayName(name: string | null): string | null {
  if (!name) return null;
  for (const suffix of DISPLAY_MATERIAL_SUFFIXES) {
    if (!name.endsWith(suffix) || name.length <= suffix.length) continue;
    const raw = name.slice(0, -suffix.length).trim();
    const normalized = normalizeMaterialName(raw);
    if (/^[a-z0-9_]+$/.test(normalized)) return normalized;
  }
  const decorativeStone = materialFamilyFromDecorativeStoneDisplayName(name);
  if (decorativeStone) return decorativeStone;
  return null;
}

function materialFamilyFromDecorativeStoneDisplayName(name: string): string | null {
  let raw = name.trim();
  let changed = false;
  for (const suffix of DISPLAY_STONE_FORM_SUFFIXES) {
    if (!raw.endsWith(suffix) || raw.length <= suffix.length) continue;
    raw = raw.slice(0, -suffix.length).trim();
    changed = true;
    break;
  }
  for (const prefix of DISPLAY_STONE_FORM_PREFIXES) {
    if (!raw.startsWith(prefix) || raw.length <= prefix.length) continue;
    raw = raw.slice(prefix.length).trim();
    changed = true;
    break;
  }
  if (!changed) return null;
  const normalized = normalizeMaterialName(raw);
  return /^[a-z0-9_]+$/.test(normalized) ? normalized : null;
}

function normalizeMaterialName(raw: string): string {
  return raw.toLowerCase().replace(/[^a-z0-9]+/g, "_").replace(/^_+|_+$/g, "");
}

function materialFamilyFromStoneTypeTags(tags: readonly string[]): string | null {
  const candidates: Array<{ material: string; priority: number }> = [];
  for (const tag of tags) {
    const [namespace, path] = tag.split(":", 2);
    if (!path?.startsWith("stone_types/")) continue;
    const raw = path.split("/").at(-1) ?? "";
    const material = raw.endsWith("_half") ? raw.slice(0, -"_half".length) : raw;
    if (!/^[a-z0-9_]+$/.test(material)) continue;
    candidates.push({
      material,
      // Pack/datapack stone remaps should win over base-mod placeholder names.
      priority: namespace === "tfg" ? 0 : namespace === "tfc" ? 1 : 2,
    });
  }
  candidates.sort((a, b) => a.priority - b.priority || a.material.localeCompare(b.material));
  return candidates[0]?.material ?? null;
}

function materialFamilyFromMaterialPrefix(path: string): string | null {
  const match = /^([a-z0-9_]+)_(?:gearbox|indicator|sheet)$/.exec(path);
  if (!match) return null;
  const material = match[1] ?? "";
  return material.length > 0 ? material : null;
}

function materialFamilyFromToolPath(path: string, namespace: string): string | null {
  const suffixes = [
    "_buzz_saw_blade",
    "_pickaxe",
    "_sword",
    "_scythe",
    "_axe",
    "_shovel",
    "_hoe",
  ];
  for (const suffix of suffixes) {
    if (!path.endsWith(suffix) || path.length <= suffix.length) continue;
    const raw = path.slice(0, -suffix.length);
    const mapped = TOOL_ARMOR_MATERIAL_PREFIX[raw];
    if (mapped) return mapped;
    if (namespace === "minecraft") return null;
    return /^[a-z0-9_]+$/.test(raw) ? raw : null;
  }
  return null;
}

function stripOreGradePrefix(value: string): string | null {
  for (const prefix of ORE_GRADE_PREFIXES) {
    if (value.startsWith(prefix) && value.length > prefix.length) return value.slice(prefix.length);
  }
  return value || null;
}

function stripOreProcessPrefix(value: string): string | null {
  for (const prefix of ORE_PROCESS_PREFIXES) {
    if (value.startsWith(prefix) && value.length > prefix.length) return value.slice(prefix.length);
  }
  return value || null;
}

export const materialFamilyRule: Rule = {
  id: "material_family",
  facets: ["material_family"],
  run({ record }) {
    const tags = record.minecraft_tags;

    // Log tags win over generic wooden_tool_materials because they pin the wood.
    for (const tag of tags) {
      const mat = LOG_TAG_TO_WOOD[tag];
      if (mat) {
        return [
          {
            facet: "material_family",
            kind: "single",
            value: mat,
            source: `rule:material_family_from_tag`,
            confidence: 1,
            rationale: `log tag ${tag}`,
          },
        ];
      }
    }

    for (const tag of tags) {
      const mat = TAG_TO_MATERIAL[tag];
      if (mat) {
        return [
          {
            facet: "material_family",
            kind: "single",
            value: mat,
            source: `rule:material_family_from_tag`,
            confidence: 1,
            rationale: `tag ${tag}`,
          },
        ];
      }
    }

    for (const tag of tags) {
      const mat = materialFamilyFromCommonMaterialTag(tag);
      if (mat) {
        return [
          {
            facet: "material_family",
            kind: "single",
            value: mat,
            source: "rule:material_family_from_common_tag",
            confidence: 1,
            rationale: `material tag ${tag}`,
          },
        ];
      }
    }

    const structuredWoodPath = materialFamilyFromStructuredWoodPath(record.path);
    if (structuredWoodPath) {
      return [
        {
          facet: "material_family",
          kind: "single",
          value: structuredWoodPath,
          source: "rule:material_family_from_structured_wood_path",
          confidence: 1,
          rationale: `wood path ${record.path}`,
        },
      ];
    }

    const structuredMetalPath = materialFamilyFromStructuredMetalPath(record.path);
    if (structuredMetalPath) {
      return [
        {
          facet: "material_family",
          kind: "single",
          value: structuredMetalPath,
          source: "rule:material_family_from_structured_metal_path",
          confidence: 1,
          rationale: `metal path ${record.path}`,
        },
      ];
    }

    const displayMaterial = materialFamilyFromDisplayName(record.display_name);
    if (displayMaterial) {
      return [
        {
          facet: "material_family",
          kind: "single",
          value: displayMaterial,
          source: "rule:material_family_from_display_name",
          confidence: 0.95,
          rationale: `display name ${record.display_name}`,
        },
      ];
    }

    const stoneTypeMaterial = materialFamilyFromStoneTypeTags(tags);
    if (stoneTypeMaterial) {
      return [
        {
          facet: "material_family",
          kind: "single",
          value: stoneTypeMaterial,
          source: "rule:material_family_from_stone_type_tag",
          confidence: 0.95,
          rationale: "stone type tag",
        },
      ];
    }

    const prefixMaterial = materialFamilyFromMaterialPrefix(record.path);
    if (prefixMaterial) {
      return [
        {
          facet: "material_family",
          kind: "single",
          value: prefixMaterial,
          source: "rule:material_family_from_id",
          confidence: 0.95,
          rationale: "material prefix",
        },
      ];
    }

    const exact = EXACT_ID_TO_FAMILY[record.id];
    if (exact) {
      return [
        {
          facet: "material_family",
          kind: "single",
          value: exact,
          source: "rule:material_family_from_id",
          confidence: 1,
          rationale: `exact id ${record.id}`,
        },
      ];
    }

    // Tool / weapon / armor material prefix: `diamond_pickaxe` → diamond,
    // `chainmail_helmet` → iron, `golden_sword` → gold. The material name
    // before the tool suffix is the authoritative signal for these items.
    for (const suffix of TOOL_ARMOR_SUFFIXES) {
      if (!record.path.endsWith(suffix)) continue;
      const prefix = record.path.slice(0, record.path.length - suffix.length);
      const mat = TOOL_ARMOR_MATERIAL_PREFIX[prefix];
      if (mat) {
        return [
          {
            facet: "material_family",
            kind: "single",
            value: mat,
            source: "rule:material_family_from_tool_prefix",
            confidence: 1,
            rationale: `${prefix}${suffix}`,
          },
        ];
      }
    }

    const toolMaterial = materialFamilyFromToolPath(record.path, record.namespace);
    if (toolMaterial) {
      return [
        {
          facet: "material_family",
          kind: "single",
          value: toolMaterial,
          source: "rule:material_family_from_tool_prefix",
          confidence: 0.95,
          rationale: `tool material prefix ${record.path}`,
        },
      ];
    }

    const oreFamily = materialFamilyFromOrePath(record.path);
    if (oreFamily) {
      return [
        {
          facet: "material_family",
          kind: "single",
          value: oreFamily,
          source: "rule:material_family_from_ore_id",
          confidence: 1,
          rationale: `ore id ${record.path}`,
        },
      ];
    }

    const bloomFamily = materialFamilyFromBloomPath(record.path);
    if (bloomFamily) {
      return [
        {
          facet: "material_family",
          kind: "single",
          value: bloomFamily,
          source: "rule:material_family_from_bloom_id",
          confidence: 1,
          rationale: `bloom id ${record.path}`,
        },
      ];
    }

    const nsOverrides = NAMESPACE_PREFIX_OVERRIDES[record.namespace];
    if (nsOverrides) {
      for (const [prefix, family] of nsOverrides) {
        if (record.path.startsWith(prefix)) {
          return [
            {
              facet: "material_family",
              kind: "single",
              value: family,
              source: "rule:material_family_from_id",
              confidence: 1,
              rationale: `${record.namespace} ns override on prefix ${prefix}`,
            },
          ];
        }
      }
    }

    for (const [prefix, family] of ID_PREFIX_TO_FAMILY) {
      if (!record.path.startsWith(prefix)) continue;
      // Skip when the wood-prefix would over-match a non-wood block:
      //   - `_fungus`  → small nether plant, not wood (crimson_fungus, warped_fungus)
      //   - `_nylium`  → netherrack-based terrain, not wood (crimson_nylium, warped_nylium)
      // Vanilla v1 canary flagged these on the crimson_/warped_ prefixes.
      if (
        (prefix === "crimson_" || prefix === "warped_") &&
        (record.path.endsWith("_fungus") || record.path.endsWith("_nylium"))
      ) {
        continue;
      }
      return [
        {
          facet: "material_family",
          kind: "single",
          value: family,
          source: "rule:material_family_from_id",
          confidence: 1,
          rationale: `id prefix ${prefix}`,
        },
      ];
    }

    return [];
  },
};
