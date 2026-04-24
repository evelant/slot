import type { Rule } from "../types.ts";

/**
 * Map loot-table ids to `origin` facet values. Walks the item's
 * `loot_table_sources`, classifies each table id, and emits the union of
 * matched origins. If the table id doesn't match any rule we skip it rather
 * than emit `overworld_surface` by default — stage 3 fills the gap.
 *
 * Table ids are fully-qualified (e.g. `minecraft:chests/desert_pyramid`); we
 * check against the path portion only.
 */

interface Matcher {
  test: (path: string) => boolean;
  origin: string;
}

const MATCHERS: Matcher[] = [
  // structure-specific chests
  { test: (p) => p.startsWith("chests/bastion"), origin: "bastion" },
  { test: (p) => p === "chests/nether_bridge", origin: "nether_fortress" },
  { test: (p) => p.startsWith("chests/end_city"), origin: "end_city" },
  { test: (p) => p.startsWith("chests/stronghold"), origin: "stronghold" },
  { test: (p) => p === "chests/woodland_mansion", origin: "woodland_mansion" },
  { test: (p) => p === "chests/ancient_city" || p === "chests/ancient_city_ice_box", origin: "ancient_city" },
  { test: (p) => p.startsWith("chests/ancient_city"), origin: "ancient_city" },
  { test: (p) => p === "chests/desert_pyramid", origin: "desert_temple" },
  { test: (p) => p === "chests/jungle_temple" || p === "chests/jungle_temple_dispenser", origin: "jungle_temple" },
  { test: (p) => p.startsWith("chests/jungle_temple"), origin: "jungle_temple" },
  { test: (p) => p === "chests/pillager_outpost", origin: "pillager_outpost" },
  { test: (p) => p === "chests/ruined_portal", origin: "ruined_portal" },
  { test: (p) => p.startsWith("chests/underwater_ruin"), origin: "overworld_ocean" },
  { test: (p) => p.startsWith("chests/shipwreck"), origin: "overworld_ocean" },
  { test: (p) => p === "chests/buried_treasure", origin: "overworld_ocean" },
  { test: (p) => p.startsWith("chests/trial_chambers"), origin: "trial_chamber" },
  { test: (p) => p.startsWith("chests/village"), origin: "village" },
  { test: (p) => p === "chests/abandoned_mineshaft", origin: "mineshaft" },
  { test: (p) => p === "chests/simple_dungeon", origin: "overworld_cave" },
  { test: (p) => p === "chests/spawn_bonus_chest", origin: "overworld_surface" },
  // archaeology / brush
  { test: (p) => p.startsWith("archaeology/") || p.startsWith("brush/"), origin: "archaeology_site" },
  // entities
  { test: (p) => p.startsWith("entities/"), origin: "mob_drop" },
  // gameplay tables
  { test: (p) => p.startsWith("gameplay/hero_of_the_village"), origin: "village" },
  { test: (p) => p.startsWith("gameplay/sniffer"), origin: "sniffer_garden" },
  // pots / spawners / dispensers — treat pots as trial chambers.
  { test: (p) => p.startsWith("pots/trial_chambers"), origin: "trial_chamber" },
  { test: (p) => p.startsWith("dispensers/trial_chambers"), origin: "trial_chamber" },
  { test: (p) => p.startsWith("spawners/trial_chamber"), origin: "trial_chamber" },
  // ===== block drops =====
  // deep-nether ancient debris
  { test: (p) => p === "blocks/ancient_debris", origin: "nether" },
  // sculk blocks → deep_dark biome
  { test: (p) => p.startsWith("blocks/sculk"), origin: "deep_dark" },
  // deepslate-tier ores are still overworld caves (just the lower band).
  { test: (p) => /^blocks\/deepslate_[a-z_]+_ore$/.test(p), origin: "overworld_cave" },
  // end-exclusive blocks
  {
    test: (p) =>
      p.startsWith("blocks/end_") ||
      p.startsWith("blocks/purpur") ||
      p === "blocks/chorus_plant" ||
      p === "blocks/chorus_flower",
    origin: "end",
  },
  // nether-exclusive blocks (netherrack, soul_*, nether_*, crimson_*, warped_*, …)
  {
    test: (p) =>
      /^blocks\/(netherrack|soul_|crimson_|warped_|nether_|basalt|blackstone|magma_block|bone_block|gilded_blackstone|glowstone|shroomlight|nylium)/.test(p),
    origin: "nether",
  },
  // overworld ores (iron_ore, gold_ore, copper_ore, coal_ore …) → caves.
  { test: (p) => /^blocks\/[a-z_]+_ore$/.test(p), origin: "overworld_cave" },
  // overworld-surface plant drops: leaves/logs/saplings/propagules from surface trees,
  // crops, berries, bamboo, kelp, cactus, flowers, grass.
  {
    test: (p) =>
      /^blocks\/(oak|birch|spruce|jungle|acacia|dark_oak|pale_oak|mangrove|cherry|azalea|flowering_azalea)_(log|wood|leaves|sapling|propagule)$/.test(p) ||
      /^blocks\/(short|tall)_grass$/.test(p) ||
      /^blocks\/(cactus|sugar_cane|wheat|beetroots|carrots|potatoes|pumpkin|melon|bamboo|kelp|sweet_berry_bush|glow_berries|pitcher_crop|torchflower_crop|sunflower|dandelion|poppy|cornflower|lily_of_the_valley|rose_bush|peony|lilac|bluebell|wildflowers|oxeye_daisy|allium|azure_bluet|blue_orchid|orange_tulip|white_tulip|pink_tulip|red_tulip)/.test(p),
    origin: "overworld_surface",
  },
  // remaining blocks/* drops (cobblestone, stone, gravel, obsidian, ice, snow, dirt, …)
  // are too varied to pin without a y-level/biome map — skip rather than guess.
];

export const originRule: Rule = {
  id: "origin",
  facets: ["origin"],
  run({ record }) {
    const origins = new Set<string>();
    for (const tableId of record.loot_table_sources) {
      const colon = tableId.indexOf(":");
      const path = colon >= 0 ? tableId.slice(colon + 1) : tableId;
      for (const m of MATCHERS) {
        if (m.test(path)) {
          origins.add(m.origin);
          break;
        }
      }
    }
    if (origins.size === 0) return [];

    return [
      {
        facet: "origin",
        kind: "multi",
        values: [...origins].sort(),
        mode: "add",
        source: "rule:origin_from_loot_tables",
        confidence: 1,
      },
    ];
  },
};
