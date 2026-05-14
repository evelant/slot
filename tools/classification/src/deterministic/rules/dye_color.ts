import type { Rule } from "../types.ts";
import { DYE_COLOR_VALUES } from "../../schema/facets.ts";

/**
 * `dye_color` fires only when the item's id carries one of the 16 canonical
 * color prefixes AND the item is in a color-carrying tag (wool, wool_carpets,
 * beds, banners, stained_glass, concrete, terracotta, candles, shulker_box).
 * Prefix alone is not enough — `white_stained_glass` is dyed but `white_tulip`
 * is not.
 */

const COLOR_SET = new Set<string>(DYE_COLOR_VALUES);

const DYED_TAGS = new Set([
  "minecraft:wool",
  "minecraft:wool_carpets",
  "minecraft:beds",
  "minecraft:banners",
  "minecraft:candles",
  "minecraft:shulker_boxes",
  "ae2:paint_balls",
  "balm:dyes",
  "c:dyes",
  "chalk:chalks",
  "comforts:hammocks",
  "comforts:sleeping_bags",
  "create:postboxes",
  "create:seats",
  "forge:dyes",
  "gtceu:lamps",
]);

const DYED_TAG_PATH_FRAGMENTS = [
  "decorative_vases",
  "palettes/cycle_groups",
  "palettes/dye_groups",
];

/** Items that are explicitly dyed but don't have a clean tag membership. */
const DYED_SUFFIXES = new Set([
  "_stained_glass",
  "_stained_glass_pane",
  "_glazed_terracotta",
  "_terracotta",
  "_concrete",
  "_concrete_powder",
  "_wool",
  "_carpet",
  "_bed",
  "_banner",
  "_candle",
  "_shulker_box",
  "_dye",
  "_lamp",
  "_paint_ball",
  "_cable",
  "_chalk",
  "_hammock",
  "_sleeping_bag",
  "_postbox",
  "_seat",
  "_shipping_container",
  "_awning",
  "_roof",
  "_poured_glass",
]);

export const dyeColorRule: Rule = {
  id: "dye_color",
  facets: ["dye_color"],
  run({ record }) {
    const color = extractColor(record.path);
    if (!color) return [];

    // Tag membership is the strongest signal; suffix is the fallback.
    const anyTag = record.minecraft_tags.some((t) => DYED_TAGS.has(t));
    const anyTagFragment = record.minecraft_tags.some((tag) =>
      DYED_TAG_PATH_FRAGMENTS.some((fragment) => tag.includes(fragment)),
    );
    const anySuffix = [...DYED_SUFFIXES].some((s) => record.path.endsWith(s));
    const anyDyeingRecipe = record.recipe_role.output_of.some(isDyeingRecipeId);
    const structuredTfcColor =
      record.namespace === "tfc" &&
      (record.path.startsWith("alabaster/") || record.path.startsWith("ceramic/unfired_large_vessel/"));
    if (!anyTag && !anyTagFragment && !anySuffix && !anyDyeingRecipe && !structuredTfcColor) return [];

    return [
      {
        facet: "dye_color",
        kind: "single",
        value: color,
        source: "rule:dye_color_from_id",
        confidence: 1,
        rationale: `id prefix + tag`,
      },
    ];
  },
};

function extractColor(path: string): string | undefined {
  const prefix = extractColorPrefix(path);
  if (prefix) return prefix;
  const leaf = path.split("/").at(-1) ?? path;
  const leafPrefix = extractColorPrefix(leaf);
  if (leafPrefix) return leafPrefix;
  const suffix = extractColorSuffix(leaf);
  if (suffix) return suffix;
  return COLOR_SET.has(leaf) ? leaf : undefined;
}

function extractColorPrefix(path: string): string | undefined {
  // light_gray and light_blue have underscores; check the longest prefixes first.
  for (const color of [
    "light_gray",
    "light_blue",
    "white",
    "orange",
    "magenta",
    "yellow",
    "lime",
    "pink",
    "gray",
    "cyan",
    "purple",
    "blue",
    "brown",
    "green",
    "red",
    "black",
  ]) {
    if (path === color || path.startsWith(`${color}_`)) {
      if (COLOR_SET.has(color)) return color;
    }
  }
  return undefined;
}

function extractColorSuffix(path: string): string | undefined {
  for (const color of [
    "light_gray",
    "light_blue",
    "white",
    "orange",
    "magenta",
    "yellow",
    "lime",
    "pink",
    "gray",
    "cyan",
    "purple",
    "blue",
    "brown",
    "green",
    "red",
    "black",
  ]) {
    if (path === color || path.endsWith(`_${color}`)) {
      if (COLOR_SET.has(color)) return color;
    }
  }
  return undefined;
}

function isDyeingRecipeId(id: string): boolean {
  return id.split(":").at(-1)?.split("/").includes("dyeing") ?? false;
}
