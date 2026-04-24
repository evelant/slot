import type { Rule } from "../types.ts";

/**
 * Vanilla items only obtainable via `/give` or the creative inventory. Hardcoded
 * because there's no data-driven signal that cleanly separates these from
 * legitimately-rare obtainable items like `dragon_egg` or `nether_star` (both
 * rarity=epic too). The list rarely changes and tracks the plan's §1 `admin`
 * role almost exactly.
 */
const VANILLA_CREATIVE_ONLY = new Set<string>([
  "minecraft:command_block",
  "minecraft:chain_command_block",
  "minecraft:repeating_command_block",
  "minecraft:structure_block",
  "minecraft:structure_void",
  "minecraft:jigsaw",
  "minecraft:barrier",
  "minecraft:light",
  "minecraft:debug_stick",
  "minecraft:knowledge_book",
  "minecraft:petrified_oak_slab",
  "minecraft:spawner",
  "minecraft:trial_spawner",
  "minecraft:vault",
  "minecraft:creative_vault",
  "minecraft:end_portal_frame",
  "minecraft:farmland",
  "minecraft:frogspawn",
  "minecraft:budding_amethyst",
  "minecraft:reinforced_deepslate",
  "minecraft:bedrock",
]);

export const isCreativeOnlyRule: Rule = {
  id: "is_creative_only",
  facets: ["is_creative_only"],
  run({ record }) {
    if (!VANILLA_CREATIVE_ONLY.has(record.id)) return [];
    return [
      {
        facet: "is_creative_only",
        kind: "single",
        value: true,
        source: "rule:is_creative_only_hardcoded",
        confidence: 1,
        rationale: "known vanilla creative-only item",
      },
    ];
  },
};
