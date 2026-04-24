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
  "minecraft:wooden_tool_materials": "wood_oak", // overridden by log-tag below when possible
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
  "minecraft:honeycomb": "honey",
  "minecraft:honeycomb_block": "honey",
  "minecraft:turtle_scute": "scute",
  "minecraft:armadillo_scute": "scute",
  "minecraft:stone": "stone",
  "minecraft:cobblestone": "cobblestone",
  "minecraft:deepslate": "deepslate",
  "minecraft:cobbled_deepslate": "deepslate",
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

    for (const [prefix, family] of ID_PREFIX_TO_FAMILY) {
      if (record.path.startsWith(prefix)) {
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
    }

    return [];
  },
};
