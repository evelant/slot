import type { Rule } from "../types.ts";

/**
 * Vanilla fuel detection. Minecraft's fuel list is hardcoded in
 * `FurnaceBlockEntity.getFuel()` rather than data-driven, so we mirror it
 * here by id/tag pattern.
 *
 * The major buckets:
 *   1. Anything in `minecraft:planks` (every wood plank is fuel).
 *   2. Anything in `minecraft:logs` (every log/stem/hyphae).
 *   3. Wood-shape derivatives — if an item matches `<wood>_<shape>` for a
 *      wood prefix and shape in {stairs, slab, fence, fence_gate, door,
 *      trapdoor, button, pressure_plate, sign, hanging_sign}, it's fuel.
 *   4. Specific non-wood fuels (coal, charcoal, blaze rod, dried kelp,
 *      bamboo, lava bucket, scaffolding, bookshelf, …).
 *
 * We rely only on the NDJSON record fields (tags + id), no recipe analysis.
 */

const PLANKS_TAG = "minecraft:planks";
const LOGS_TAG = "minecraft:logs";

const WOOD_PREFIXES = [
  "oak_", "birch_", "spruce_", "jungle_", "acacia_", "dark_oak_",
  "pale_oak_", "mangrove_", "cherry_", "bamboo_", "crimson_", "warped_",
  "stripped_oak_", "stripped_birch_", "stripped_spruce_", "stripped_jungle_",
  "stripped_acacia_", "stripped_dark_oak_", "stripped_pale_oak_",
  "stripped_mangrove_", "stripped_cherry_", "stripped_crimson_", "stripped_warped_",
];

const WOOD_SHAPE_SUFFIXES = [
  "_stairs", "_slab", "_fence", "_fence_gate", "_door", "_trapdoor",
  "_button", "_pressure_plate", "_sign", "_hanging_sign", "_chest_boat",
  "_boat",
];

const EXPLICIT_NON_WOOD_FUELS = new Set<string>([
  "minecraft:coal",
  "minecraft:charcoal",
  "minecraft:coal_block",
  "minecraft:blaze_rod",
  "minecraft:dried_kelp",
  "minecraft:dried_kelp_block",
  "minecraft:lava_bucket",
  "minecraft:scaffolding",
  "minecraft:bamboo",
  "minecraft:stick",
  "minecraft:bowl",
  "minecraft:bookshelf",
  "minecraft:chiseled_bookshelf",
  "minecraft:crafting_table",
  "minecraft:crafter",
  "minecraft:cartography_table",
  "minecraft:fletching_table",
  "minecraft:loom",
  "minecraft:smithing_table",
  "minecraft:note_block",
  "minecraft:jukebox",
  "minecraft:ladder",
  "minecraft:barrel",
  "minecraft:chest",
  "minecraft:trapped_chest",
  "minecraft:daylight_detector",
  "minecraft:banner",
  "minecraft:bow",
  "minecraft:fishing_rod",
]);

const EXPLICIT_FUEL_SUFFIXES = [
  "_banner", "_sapling", "_wool", "_carpet", "_bed",
];

export const isFuelRule: Rule = {
  id: "is_fuel",
  facets: ["is_fuel"],
  run({ record }) {
    const tags = new Set(record.minecraft_tags);
    const path = record.path;

    const isFuel =
      tags.has(PLANKS_TAG) ||
      tags.has(LOGS_TAG) ||
      EXPLICIT_NON_WOOD_FUELS.has(record.id) ||
      EXPLICIT_FUEL_SUFFIXES.some((s) => path.endsWith(s)) ||
      // wood_prefix + wood_shape_suffix (covers oak_stairs, mangrove_fence, etc.)
      WOOD_PREFIXES.some((p) =>
        path.startsWith(p) && WOOD_SHAPE_SUFFIXES.some((s) => path.endsWith(s)),
      );

    if (!isFuel) return [];
    return [
      {
        facet: "is_fuel",
        kind: "single",
        value: true,
        source: "rule:is_fuel_from_id_or_tag",
        confidence: 1,
      },
    ];
  },
};
