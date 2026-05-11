import type { Rule } from "../types.ts";

/**
 * `rarity` is *acquisition difficulty* per the plan's §7 definition, but
 * vanilla's `minecraft:rarity` component is actually the **display-name color**
 * — it's `common` on everything from cobblestone to ancient_debris. Using it
 * verbatim gave us `rarity=common` on netherite_ingot and ancient_debris,
 * which an LLM canary correctly flagged as wrong.
 *
 * Policy:
 *   1. Exact-id overrides (`RARITY_OVERRIDES`) win first — for items where
 *      the component value is wrong on its own merits (dragon_head is
 *      `epic` in the component → would map to `unique`, but multiple drop
 *      per world per the End ship count, so it's `rare`).
 *   2. Otherwise, only fire when the component is informative — `uncommon`,
 *      `rare`, or `epic` (→ `unique`). For `common`, skip and let stage 3
 *      judge. That gets us the obvious wins (totems, unique items,
 *      enchanted books) without polluting the "common" bucket with items
 *      stage 3 would classify as uncommon or rare.
 */
const RARITY_OVERRIDES: Record<string, string> = {
  // `dragon_head` component is epic, but every End ship has one and there
  // are many End ships per world. LLM canary flagged this — emit `rare`
  // (acquisition tier matching `requires_exploration`) instead of unique.
  "minecraft:dragon_head": "rare",
  // Vanilla v1 canary catches.
  // creeper_head requires charged-creeper kill of a creeper — once per world
  // typical; the component reports `uncommon` which understates it.
  "minecraft:creeper_head": "rare",
  // totem_of_undying drops only from evokers (woodland mansions / raids);
  // milestone item, not "uncommon".
  "minecraft:totem_of_undying": "rare",
};

export const rarityRule: Rule = {
  id: "rarity",
  facets: ["rarity"],
  run({ record }) {
    const override = RARITY_OVERRIDES[record.id];
    if (override) {
      return [
        {
          facet: "rarity",
          kind: "single",
          value: override,
          mode: "override-if-null",
          source: "rule:rarity_id_override",
          confidence: 1,
          rationale: `id-specific override (LLM canary catch)`,
        },
      ];
    }

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
