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
  // gameplay tables — hero-of-the-village is trading, fishing is fishing-origin
  { test: (p) => p.startsWith("gameplay/hero_of_the_village"), origin: "village" },
  // pots / spawners / dispensers — surface / chamber / etc. Treat pots as trial chambers.
  { test: (p) => p.startsWith("pots/trial_chambers"), origin: "trial_chamber" },
  { test: (p) => p.startsWith("dispensers/trial_chambers"), origin: "trial_chamber" },
  { test: (p) => p.startsWith("spawners/trial_chamber"), origin: "trial_chamber" },
  // the remaining `blocks/...` drops are generic surface/underground — skip.
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
