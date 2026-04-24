import type { Rule, RuleOutput } from "../types.ts";

/**
 * Derive every boolean facet directly from item component data. Rules here
 * only fire `true`; absence means "not asserted," which merges cleanly with
 * higher layers (no false-positive `false`s to override).
 *
 * Component keys we use:
 *   - minecraft:max_stack_size  -> is_stackable (>1)
 *   - minecraft:max_damage      -> has_durability
 *   - minecraft:enchantable     -> has_enchantments
 *   - minecraft:container
 *   - minecraft:bundle_contents
 *   - minecraft:writable_book_content / written_book_content
 *   - minecraft:banner_patterns
 *   - minecraft:bees            -> has_nbt_variation (carries state between stacks)
 */
export const booleansRule: Rule = {
  id: "booleans",
  facets: [
    "is_stackable",
    "has_durability",
    "has_enchantments",
    "is_block_item",
    "has_nbt_variation",
  ],
  run({ record, bundle }) {
    const components = record.component_data ?? {};
    const out: RuleOutput[] = [];

    const emit = (facet: string, source: string) => {
      out.push({
        facet,
        kind: "single",
        value: true,
        source: `rule:${source}`,
        confidence: 1,
      });
    };

    const maxStack = components["minecraft:max_stack_size"];
    if (typeof maxStack === "number" && maxStack > 1) {
      emit("is_stackable", "is_stackable_from_component");
    }

    if ("minecraft:max_damage" in components) {
      emit("has_durability", "has_durability_from_component");
    }

    if ("minecraft:enchantable" in components) {
      emit("has_enchantments", "has_enchantments_from_component");
    }

    if (record.path in bundle.blocks) {
      emit("is_block_item", "is_block_item_from_registry");
    }

    // Components that imply per-stack state / NBT variation. `container`,
    // `bundle_contents`, bee jar, written/writable book, banner patterns,
    // jukebox contents, and block-state all vary between stacks.
    const nbtVariationKeys = [
      "minecraft:container",
      "minecraft:bundle_contents",
      "minecraft:bees",
      "minecraft:written_book_content",
      "minecraft:writable_book_content",
      "minecraft:banner_patterns",
      "minecraft:block_state",
      "minecraft:block_entity_data",
      "minecraft:charged_projectiles",
      "minecraft:lodestone_tracker",
      "minecraft:potion_contents",
    ];
    for (const key of nbtVariationKeys) {
      if (key in components) {
        emit("has_nbt_variation", "has_nbt_variation_from_component");
        break;
      }
    }

    return out;
  },
};
