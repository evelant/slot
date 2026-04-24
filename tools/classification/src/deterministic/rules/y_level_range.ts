import type { Rule } from "../types.ts";

/**
 * Coarse world-height bucket. Derived from id patterns on items that have a
 * clear world-level association — ores, deep-only blocks, end-exclusive
 * blocks, and nether blocks.
 *
 * Intentionally narrow: items with no y-level association (planks, ingots)
 * get nothing. Stage 3 may fill in edge cases.
 */

interface Matcher {
  test: (path: string) => boolean;
  bucket: "sky" | "surface" | "underground" | "deep" | "nether_surface" | "end_islands";
}

const MATCHERS: Matcher[] = [
  // deepslate-tier ores + ancient debris are the deep-y-level items.
  { test: (p) => /^deepslate_[a-z_]+_ore$/.test(p), bucket: "deep" },
  { test: (p) => p === "ancient_debris", bucket: "deep" },
  { test: (p) => p.startsWith("sculk"), bucket: "deep" },
  { test: (p) => p === "deepslate" || p === "cobbled_deepslate" || p.startsWith("deepslate_") || p.startsWith("polished_deepslate") || p.startsWith("chiseled_deepslate"), bucket: "deep" },
  // surface ores
  { test: (p) => /^[a-z_]+_ore$/.test(p) && !p.startsWith("deepslate_") && !p.startsWith("nether_"), bucket: "underground" },
  // nether-exclusive
  {
    test: (p) =>
      p === "netherrack" ||
      p.startsWith("soul_") ||
      p.startsWith("crimson_") ||
      p.startsWith("warped_") ||
      p.startsWith("nether_") ||
      p === "basalt" || p.startsWith("polished_basalt") ||
      p === "blackstone" || p.startsWith("polished_blackstone") || p.startsWith("chiseled_polished_blackstone") || p === "gilded_blackstone" ||
      p === "magma_block" ||
      p === "glowstone" ||
      p === "shroomlight" ||
      p === "bone_block",
    bucket: "nether_surface",
  },
  // end-exclusive
  {
    test: (p) =>
      p.startsWith("end_") ||
      p.startsWith("purpur") ||
      p === "chorus_plant" || p === "chorus_flower" || p === "chorus_fruit" ||
      p === "dragon_egg" ||
      p === "dragon_head",
    bucket: "end_islands",
  },
];

export const yLevelRangeRule: Rule = {
  id: "y_level_range",
  facets: ["y_level_range"],
  run({ record }) {
    for (const m of MATCHERS) {
      if (m.test(record.path)) {
        return [
          {
            facet: "y_level_range",
            kind: "single",
            value: m.bucket,
            source: "rule:y_level_range_from_id",
            confidence: 1,
            rationale: `id pattern`,
          },
        ];
      }
    }
    return [];
  },
};
