import type { Rule } from "../types.ts";

/**
 * For each recipe this item is an ingredient of, emit the recipe's type as
 * a `processing_in` value. Vanilla recipe types map to the short verbs in the
 * plan's §13 vocabulary (`smelting`, `blasting`, etc.); mod-registered recipe
 * types flow through unchanged as `<namespace>:<id>`.
 *
 * We don't consult `output_of` — `processing_in` is defined as consumption,
 * not production.
 */

const VANILLA_TYPE_TO_VERB: Record<string, string> = {
  "minecraft:smelting": "smelting",
  "minecraft:blasting": "blasting",
  "minecraft:smoking": "smoking",
  "minecraft:campfire_cooking": "campfire_cooking",
  "minecraft:stonecutting": "stonecutting",
  "minecraft:smithing_transform": "smithing",
  "minecraft:smithing_trim": "smithing",
  "minecraft:crafting_shaped": "crafting",
  "minecraft:crafting_shapeless": "crafting",
  "minecraft:crafting_transmute": "crafting",
  "minecraft:crafting_dye": "crafting",
  "minecraft:crafting_imbue": "crafting",
  "minecraft:crafting_decorated_pot": "crafting",
  "minecraft:crafting_special_repairitem": "anvil_repairing",
  "minecraft:crafting_special_mapextending": "crafting",
  "minecraft:crafting_special_bookcloning": "crafting",
  "minecraft:crafting_special_bannerduplicate": "crafting",
  "minecraft:crafting_special_firework_rocket": "crafting",
  "minecraft:crafting_special_firework_star": "crafting",
  "minecraft:crafting_special_firework_star_fade": "crafting",
  "minecraft:crafting_special_shielddecoration": "crafting",
};

export const processingInRule: Rule = {
  id: "processing_in",
  facets: ["processing_in"],
  run({ record, recipeTypes }) {
    const verbs = new Set<string>();
    for (const recipeId of record.recipe_role.ingredient_of) {
      const type = recipeTypes.get(recipeId);
      if (!type) continue;
      verbs.add(recipeTypeVerb(type));
    }
    for (const type of Object.keys(record.recipe_role.ingredient_of_counts ?? {})) {
      verbs.add(recipeTypeVerb(type));
    }
    if (verbs.size === 0) return [];

    return [
      {
        facet: "processing_in",
        kind: "multi",
        values: [...verbs].sort(),
        mode: "add",
        source: "rule:processing_in_from_recipes",
        confidence: 1,
      },
    ];
  },
};

function recipeTypeVerb(type: string): string {
  const shortType = type.startsWith("minecraft:")
    ? type.slice("minecraft:".length)
    : type;
  return VANILLA_TYPE_TO_VERB[type]
    ?? VANILLA_TYPE_TO_VERB[shortType]
    ?? VANILLA_TYPE_TO_VERB[`minecraft:${shortType}`]
    ?? type;
}
