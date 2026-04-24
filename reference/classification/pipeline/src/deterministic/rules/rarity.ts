import type { Rule } from "../types.ts";

/**
 * `rarity` has a direct component in vanilla (`minecraft:rarity`) with values
 * `common` / `uncommon` / `rare` / `epic`. Our enum uses different labels
 * — `abundant` / `common` / `uncommon` / `rare` / `unique` — so we map:
 *   common   -> common
 *   uncommon -> uncommon
 *   rare     -> rare
 *   epic     -> unique
 *
 * The `abundant` bucket isn't expressible from components alone (it needs
 * usage / frequency judgement), so we emit with `mode: override-if-null` to
 * let a richer signal from stage 3 replace us.
 */
export const rarityRule: Rule = {
  id: "rarity",
  facets: ["rarity"],
  run({ record }) {
    const components = record.component_data ?? {};
    const raw = components["minecraft:rarity"];
    if (typeof raw !== "string") return [];
    const value =
      raw === "epic" ? "unique" :
      raw === "rare" ? "rare" :
      raw === "uncommon" ? "uncommon" :
      raw === "common" ? "common" :
      null;
    if (!value) return [];
    return [
      {
        facet: "rarity",
        kind: "single",
        value,
        mode: "override-if-null",
        source: "rule:rarity_from_component",
        confidence: 1,
        rationale: `component minecraft:rarity = ${raw}`,
      },
    ];
  },
};
