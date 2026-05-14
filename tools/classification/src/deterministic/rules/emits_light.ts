import type { Rule, RuleOutput } from "../types.ts";

/**
 * Derive `emits_light=true` for vanilla items the player thinks of as
 * lighting. Modded glowing blocks fall through to LLM fill-in.
 *
 * Two signals, in order:
 *   1. The `minecraft:light_emission` component (sometimes carried by
 *      modded items that follow vanilla conventions). Boolean-truthy
 *      means light-emitter.
 *   2. A known-id list. The set is small enough to enumerate and stable
 *      across vanilla updates; mods get LLM coverage.
 *
 * Items deliberately excluded despite emitting light:
 *   - Lava bucket: role=utility for tool-wielding; the `bucket of fluid`
 *     mental model dominates over its lighting emission.
 *   - Beacon: role=trophy/curiosity, players don't lump it with torches.
 *   - Soul fire / lava blocks: hazardous, not "lighting."
 */
const VANILLA_LIGHT_IDS = new Set<string>([
  "minecraft:torch",
  "minecraft:soul_torch",
  "minecraft:redstone_torch",
  "minecraft:lantern",
  "minecraft:soul_lantern",
  "minecraft:glowstone",
  "minecraft:shroomlight",
  "minecraft:sea_lantern",
  "minecraft:end_rod",
  "minecraft:jack_o_lantern",
  "minecraft:redstone_lamp",
  "minecraft:candle",
  "minecraft:ochre_froglight",
  "minecraft:verdant_froglight",
  "minecraft:pearlescent_froglight",
  "minecraft:crying_obsidian",
  "minecraft:magma_block",
  "minecraft:sea_pickle",
  "minecraft:amethyst_cluster",
  "minecraft:large_amethyst_bud",
  "minecraft:medium_amethyst_bud",
  "minecraft:glow_lichen",
  "minecraft:glow_berries",
]);

/** Suffixes that almost always indicate a light-emitter when matched. */
const LIGHT_EMITTING_SUFFIXES = [
  "_torch",
  "_lantern",
  "_lamp",
  "_candle",
  "_glowstone",
  "_froglight",
];

export const emitsLightRule: Rule = {
  id: "emits_light",
  facets: ["emits_light"],
  run({ record }) {
    const components = record.component_data ?? {};
    const lightComponent = components["minecraft:light_emission"];
    const componentSaysYes =
      typeof lightComponent === "number"
        ? lightComponent > 0
        : Boolean(lightComponent);

    if (componentSaysYes) {
      return emit("rule:emits_light_from_component");
    }

    if (VANILLA_LIGHT_IDS.has(record.id)) {
      return emit("rule:emits_light_from_id_list");
    }

    if (record.minecraft_tags.some(isLightEmitterTag)) {
      return emit("rule:emits_light_from_tag", "light-emitting tag");
    }

    for (const suffix of LIGHT_EMITTING_SUFFIXES) {
      if (record.path.endsWith(suffix)) {
        return emit("rule:emits_light_from_id_suffix", `suffix ${suffix}`);
      }
    }

    return [];
  },
};

function isLightEmitterTag(tag: string): boolean {
  const path = tag.split(":", 2)[1] ?? tag;
  const leaf = path.split("/").at(-1) ?? path;
  return leaf === "lamps" || leaf === "lamp" || leaf.includes("lamp_") || leaf.includes("_lamp");
}

function emit(source: string, rationale?: string): RuleOutput[] {
  return [
    {
      facet: "emits_light",
      kind: "single",
      value: true,
      source,
      confidence: 1,
      rationale,
    },
  ];
}
