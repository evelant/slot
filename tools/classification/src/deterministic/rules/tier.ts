import type { Rule } from "../types.ts";

/**
 * Derive `tier` for vanilla tools/weapons/armor from the material prefix.
 * Vanilla's tier vocabulary from the plan: `wooden`, `stone`, `leather`,
 * `chainmail`, `copper`, `iron`, `golden`, `diamond`, `netherite`.
 *
 * Only fires on the canonical `<material>_<suffix>` pattern for tool/armor
 * ids — other items have no tier assignment here (stage 3 handles edge cases
 * like smithing templates).
 */

const TOOL_ARMOR_SUFFIXES = [
  "_pickaxe", "_sword", "_axe", "_shovel", "_hoe",
  "_helmet", "_chestplate", "_leggings", "_boots",
] as const;

const PREFIX_TO_TIER: Record<string, string> = {
  wooden: "wooden",
  stone: "stone",
  copper: "copper",
  iron: "iron",
  golden: "golden",
  diamond: "diamond",
  netherite: "netherite",
  leather: "leather",
  chainmail: "chainmail",
  turtle: "turtle",
};

export const tierRule: Rule = {
  id: "tier",
  facets: ["tier"],
  run({ record }) {
    for (const suffix of TOOL_ARMOR_SUFFIXES) {
      if (!record.path.endsWith(suffix)) continue;
      const prefix = record.path.slice(0, record.path.length - suffix.length);
      const tier = PREFIX_TO_TIER[prefix];
      if (tier) {
        return [
          {
            facet: "tier",
            kind: "single",
            value: tier,
            source: "rule:tier_from_tool_prefix",
            confidence: 1,
            rationale: `${prefix}${suffix}`,
          },
        ];
      }
    }
    return [];
  },
};
