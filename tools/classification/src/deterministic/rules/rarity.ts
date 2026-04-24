import type { Rule } from "../types.ts";

/**
 * `rarity` is *acquisition difficulty* per the plan's §7 definition, but
 * vanilla's `minecraft:rarity` component is actually the **display-name color**
 * — it's `common` on everything from cobblestone to ancient_debris. Using it
 * verbatim gave us `rarity=common` on netherite_ingot and ancient_debris,
 * which the sonnet canary correctly flagged as wrong.
 *
 * New policy: only fire when the component is informative — `uncommon`,
 * `rare`, or `epic` (→ `unique`). For `common`, skip and let stage 3 judge.
 * That gets us the obvious wins (totems/unique items/enchanted books) without
 * polluting the "common" bucket with items stage 3 would classify as
 * uncommon or rare.
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
      null; // skip `common` — too noisy, stage 3 fills
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
