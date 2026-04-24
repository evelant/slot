import type { Rule } from "../types.ts";
import { EQUIP_SLOT_VALUES } from "../../schema/facets.ts";

/**
 * Emit `equip_slot` from the `minecraft:equippable` component's `slot` field.
 * Vanilla uses `head` / `chest` / `legs` / `feet` / `body` (e.g. for llama
 * carpets and horse armor — we map `body` to `llama_carpet` when the allowed
 * entity list looks llama-ish, else skip it since the plan's `equip_slot`
 * enum doesn't include a generic `body`).
 */

type EquipSlotValue = typeof EQUIP_SLOT_VALUES[number];

const MAP: Record<string, EquipSlotValue> = {
  head: "head",
  chest: "chest",
  legs: "legs",
  feet: "feet",
  mainhand: "main_hand",
  offhand: "off_hand",
  saddle: "saddle",
};

export const equipSlotRule: Rule = {
  id: "equip_slot",
  facets: ["equip_slot"],
  run({ record }) {
    const components = record.component_data ?? {};
    const equippable = components["minecraft:equippable"];
    if (!equippable || typeof equippable !== "object") return [];
    const slot = (equippable as { slot?: unknown }).slot;
    if (typeof slot !== "string") return [];

    let mapped: EquipSlotValue | undefined = MAP[slot];
    if (!mapped && slot === "body") {
      const allowed = (equippable as { allowed_entities?: unknown }).allowed_entities;
      if (
        Array.isArray(allowed) &&
        allowed.every((v) => typeof v === "string" && v.includes("llama"))
      ) {
        mapped = "llama_carpet";
      }
    }
    if (!mapped) return [];

    return [
      {
        facet: "equip_slot",
        kind: "single",
        value: mapped,
        source: "rule:equip_slot_from_component",
        confidence: 1,
      },
    ];
  },
};
