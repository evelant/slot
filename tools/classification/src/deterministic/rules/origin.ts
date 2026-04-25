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
  { test: (p) => p.startsWith("gameplay/piglin_bartering"), origin: "trading" },
  { test: (p) => p.startsWith("gameplay/villagers"), origin: "trading" },
  { test: (p) => p.startsWith("gameplay/fishing"), origin: "fishing" },
  // pots / spawners / dispensers — treat pots as trial chambers.
  { test: (p) => p.startsWith("pots/trial_chambers"), origin: "trial_chamber" },
  { test: (p) => p.startsWith("dispensers/trial_chambers"), origin: "trial_chamber" },
  { test: (p) => p.startsWith("spawners/trial_chamber"), origin: "trial_chamber" },
  // ===== block drops =====
  // kelp and underwater plants — ocean, not surface (flagged by sonnet canary)
  { test: (p) => p === "blocks/kelp" || p === "blocks/kelp_plant" || p === "blocks/seagrass" || p === "blocks/tall_seagrass" || p.startsWith("blocks/sea_pickle") || p.startsWith("blocks/cod") || p.startsWith("blocks/salmon") || p.startsWith("blocks/pufferfish") || p.startsWith("blocks/tropical_fish"), origin: "overworld_ocean" },
  // amethyst geodes form in overworld caves (flagged by sonnet canary)
  { test: (p) => p.startsWith("blocks/amethyst") || p === "blocks/budding_amethyst" || p.startsWith("blocks/small_amethyst_bud") || p.startsWith("blocks/medium_amethyst_bud") || p.startsWith("blocks/large_amethyst_bud"), origin: "overworld_cave" },
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
      p === "blocks/chorus_flower" ||
      p === "blocks/dragon_head" ||
      p === "blocks/dragon_egg",
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
  // crops, berries, bamboo, kelp, cactus, flowers, grass, plus snow blocks
  // (snowballs drop from snow/snow_block — flagged by sonnet-v4 canary).
  // The plant patterns use `^...$` (exact match) so we don't accidentally
  // match `blocks/bamboo_mosaic` (a crafted item) under "bamboo" — also
  // a sonnet-v4 canary catch.
  {
    test: (p) =>
      /^blocks\/(oak|birch|spruce|jungle|acacia|dark_oak|pale_oak|mangrove|cherry|azalea|flowering_azalea)_(log|wood|leaves|sapling|propagule)$/.test(p) ||
      /^blocks\/(short|tall)_grass$/.test(p) ||
      /^blocks\/(snow|snow_block|powder_snow)$/.test(p) ||
      /^blocks\/(cactus|sugar_cane|wheat|beetroots|carrots|potatoes|pumpkin|melon|bamboo|kelp|sweet_berry_bush|glow_berries|sunflower|dandelion|poppy|cornflower|lily_of_the_valley|rose_bush|peony|lilac|bluebell|wildflowers|oxeye_daisy|allium|azure_bluet|blue_orchid|orange_tulip|white_tulip|pink_tulip|red_tulip)$/.test(p) ||
      /^blocks\/(pitcher_crop|torchflower_crop|pitcher_plant|torchflower)$/.test(p),
    origin: "overworld_surface",
  },
  // remaining blocks/* drops (cobblestone, stone, gravel, obsidian, ice, dirt, …)
  // are too varied to pin without a y-level/biome map — skip rather than guess.
];

/**
 * Items obtained primarily through hardcoded game interactions that don't show
 * up in any loot table — the rule can't see them otherwise. Keep this list
 * tight: only add items where the loot tables we DO see give an incomplete or
 * misleading picture of where the player gets the item.
 */
const HARDCODED_INTERACTION_ORIGINS: Record<string, readonly string[]> = {
  // Right-clicking a cow with an empty bucket. Loot tables only show
  // milk_bucket in trial chamber chests (which is correct but minor) —
  // nearly every player gets it from milking, in the overworld.
  "minecraft:milk_bucket": ["overworld_surface"],
  // Wandering trader llamas drop their lead on death; in 1.21+ this is
  // hardcoded behaviour rather than a loot-table entry, so we add it here.
  "minecraft:lead": ["mob_drop"],
};

export const originRule: Rule = {
  id: "origin",
  facets: ["origin"],
  run({ record }) {
    const origins = new Set<string>();
    const sources = record.loot_table_sources;
    const colonId = `${record.namespace}:`;
    const selfPaths = new Set([`blocks/${record.path}`, `blocks/${record.id.replace(colonId, "")}`]);

    // Self-drop-only detection: when the only loot table for an item is
    // `blocks/<self_id>` (the block dropping itself when broken), the item
    // is crafted/placed rather than naturally generated. Emit `crafted_only`
    // unless we have a more specific signal.
    //
    // Guard against the AE2-canary failure: a self-drop block with NO recipe
    // (`out_degree === 0`) isn't actually crafted. It's either world-gen
    // (e.g. AE2 quartz_cluster, mysterious_cube — meteor-spawned) or a
    // creative-only debug item (debug_*_gen, creative_energy_cell). We have
    // no positive signal for either, so omit `origin` and let stage 3 decide.
    const onlySelfDrop =
      sources.length > 0 &&
      sources.every((tableId) => {
        const colon = tableId.indexOf(":");
        const path = colon >= 0 ? tableId.slice(colon + 1) : tableId;
        return selfPaths.has(path);
      });
    const hasRecipe = (record.recipe_role?.out_degree ?? 0) > 0;
    if (onlySelfDrop && hasRecipe && !MATCHES_KNOWN_ORIGIN(sources, record.namespace)) {
      origins.add("crafted_only");
    } else if (onlySelfDrop && !hasRecipe && !MATCHES_KNOWN_ORIGIN(sources, record.namespace)) {
      // self-drops, no recipe, no recognized matcher — stage 3 will handle it.
    } else {
      for (const tableId of sources) {
        const colon = tableId.indexOf(":");
        const path = colon >= 0 ? tableId.slice(colon + 1) : tableId;
        for (const m of MATCHERS) {
          if (m.test(path)) {
            origins.add(m.origin);
            break;
          }
        }
      }
    }

    // Hardcoded interaction origins for items the loot-table data misses.
    for (const o of HARDCODED_INTERACTION_ORIGINS[record.id] ?? []) {
      origins.add(o);
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

/** True when at least one of the item's loot tables matches a known
 *  origin matcher. Used to decide if a self-drop-only item still has a
 *  meaningful world origin (e.g. dragon_head drops from itself but is
 *  also tagged via the end-block matcher elsewhere). */
function MATCHES_KNOWN_ORIGIN(sources: readonly string[], namespace: string): boolean {
  const ns = `${namespace}:`;
  for (const tableId of sources) {
    const path = tableId.startsWith(ns) ? tableId.slice(ns.length) : tableId;
    for (const m of MATCHERS) {
      if (m.test(path)) return true;
    }
  }
  return false;
}
