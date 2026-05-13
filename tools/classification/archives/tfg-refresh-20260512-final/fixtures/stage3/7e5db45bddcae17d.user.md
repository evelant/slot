# Items to classify
{
  "items": [
    {
      "id": "waterflasks:iron_flask",
      "namespace": "waterflasks",
      "display_name": "Iron Flask",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:usable_on_tool_rack",
        "waterflasks:flasks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:no_remainder_shaped_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:no_remainder_shaped_crafting": 2
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "tfc:no_remainder_shaped_crafting": 3
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "tfg:shaped/repair_iron_bladder",
        "tfg:shaped/repair_iron_rubber"
      ],
      "recipe_output_examples": [
        "tfg:shaped/iron_flask",
        "tfg:shaped/repair_broken_iron",
        "tfg:shaped/repair_iron_bladder",
        "tfg:shaped/repair_iron_rubber"
      ],
      "model_parents": [
        "item/iron_flask",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:flasks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§9Fluid Capacity: §f2,000 mB"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fCast Iron§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TFC Water Flasks"
        }
      ],
      "document_context": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/categories/waterflasks",
          "label": "Water Flasks",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "guide-page",
              "key": "name",
              "text": "Water Flasks"
            },
            {
              "source": "guide-page",
              "key": "description",
              "text": "Have some better containers for carrying your drinks around with you."
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 400,
        "minecraft:enchantable": {},
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "waterflasks",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "has_durability": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_durability_from_component"
        },
        "has_enchantments": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_enchantments_from_component"
        },
        "processing_in": {
          "values": [
            "tfc:no_remainder_shaped_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "waterflasks:leather_flask",
      "namespace": "waterflasks",
      "display_name": "Leather Flask",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:usable_on_tool_rack",
        "waterflasks:flasks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "waterflasks:heal_flask"
      ],
      "recipe_consumption_by_type": {
        "waterflasks:heal_flask": 1
      },
      "recipe_production_by_type": {
        "tfc:damage_inputs_shaped_crafting": 2,
        "waterflasks:heal_flask": 2
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "waterflasks:crafting/repair_leather"
      ],
      "recipe_output_examples": [
        "waterflasks:crafting/leather_flask",
        "waterflasks:crafting/leather_flask_rotated",
        "waterflasks:crafting/repair_broken_leather",
        "waterflasks:crafting/repair_leather"
      ],
      "model_parents": [
        "item/leather_flask",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:flasks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§9Fluid Capacity: §f500 mB"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TFC Water Flasks"
        }
      ],
      "document_context": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/waterflasks/recipes",
          "label": "Flask Recipes",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "guide-page",
              "key": "name",
              "text": "Flask Recipes"
            },
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "There are three tiers of water flasks. The leather flask holds 5 drinks and lasts for 100 uses. The iron flask holds 20 drinks, and lasts for 400 uses. The red steel flask holds 20 drinks, and lasts for ever. Both leather and iron flasks may be repaired."
            },
            {
              "source": "guide-page",
              "key": "pages.2.title",
              "text": "Repair Recipes"
            },
            {
              "source": "guide-page",
              "key": "pages.3.title",
              "text": "Renew Recipes"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 100,
        "minecraft:enchantable": {},
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "waterflasks",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "has_durability": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_durability_from_component"
        },
        "has_enchantments": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_enchantments_from_component"
        },
        "processing_in": {
          "values": [
            "waterflasks:heal_flask"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "waterflasks:leather_side",
      "namespace": "waterflasks",
      "display_name": "Leather Flask Side",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "tfc:damage_inputs_shaped_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 3,
        "tfc:damage_inputs_shaped_crafting": 2
      },
      "recipe_production_by_type": {
        "tfc:knapping": 2
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "beneath:crafting/juicer",
        "tfg:shaped/iron_flask",
        "tfg:shaped/red_steel_flask_bladder",
        "waterflasks:crafting/leather_flask",
        "waterflasks:crafting/leather_flask_rotated"
      ],
      "recipe_output_examples": [
        "minecraft:waterflasks/leather_knapping/leather_side_2",
        "waterflasks:leather_knapping/leather_side"
      ],
      "model_parents": [
        "item/leather_side",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:flasks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TFC Water Flasks"
        }
      ],
      "document_context": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/waterflasks/bladders",
          "label": "Flask Materials",
          "item_ref_count": 2,
          "related_item_refs": [
            "waterflasks:bladder"
          ],
          "snippets": [
            {
              "source": "guide-page",
              "key": "name",
              "text": "Flask Materials"
            },
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "Central to effective water storage is reuse of something that stored water(ish) before. Bladders! Only certain animals have bladders that work for this purpose, and some species' are easier to extract than others."
            },
            {
              "source": "guide-page",
              "key": "pages.1.text",
              "text": "Animal____________Chance Sheep___________________10% Alpaca__________________10% Goat____________________10% Deer____________________10% Equines_________________20% Bears___________________20% Bovines_________________50% Moose___________________50% To increase your chances, use a higher damage weapon to butcher the animal."
            },
            {
              "source": "guide-page",
              "key": "pages.2.title",
              "text": "Leather Flask Side"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "waterflasks",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "processing_in": {
          "values": [
            "crafting",
            "tfc:damage_inputs_shaped_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "waterflasks:red_steel_flask",
      "namespace": "waterflasks",
      "display_name": "Red Steel Flask",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:usable_on_tool_rack",
        "waterflasks:flasks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:shaped/red_steel_flask_bladder",
        "tfg:shaped/red_steel_flask_rubber"
      ],
      "model_parents": [
        "item/red_steel_flask",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:flasks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§9Fluid Capacity: §f2,000 mB"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fRed Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TFC Water Flasks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "epic"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "waterflasks",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "rarity": {
          "value": "unique",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = epic"
        }
      }
    },
    {
      "id": "waterflasks:unfinished_iron_flask",
      "namespace": "waterflasks",
      "display_name": "Unfinished Iron Flask",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "tfg:shaped/iron_flask"
      ],
      "recipe_output_examples": [
        "waterflasks:anvil/unfinished_iron_flask"
      ],
      "model_parents": [
        "item/unfinished_iron_flask",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:flasks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fCast Iron§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TFC Water Flasks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "waterflasks",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "processing_in": {
          "values": [
            "crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "waterflasks:unfinished_red_steel_flask",
      "namespace": "waterflasks",
      "display_name": "Unfinished Red Steel Flask",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "tfg:shaped/red_steel_flask_bladder",
        "tfg:shaped/red_steel_flask_rubber"
      ],
      "recipe_output_examples": [
        "waterflasks:anvil/unfinished_red_steel_flask"
      ],
      "model_parents": [
        "item/unfinished_red_steel_flask",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:flasks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fRed Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TFC Water Flasks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "epic"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "waterflasks",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "rarity": {
          "value": "unique",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = epic"
        },
        "processing_in": {
          "values": [
            "crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    }
  ]
}
# Final response checklist
- Respond with one strict JSON object matching the expected output shape above. No markdown, no prose, no comments.
- Include every item id from `items` exactly once. If output gets long, shorten rationales instead of dropping items.
- `schema_proposals`, `vocabulary_proposals`, `corrections`, and `fill_ins` are top-level arrays only. Never put them inside `<item_id>.facets`; every key inside `facets` must be a real facet id.
- Use `ambiguous: true` only for single-value enum/free_text facets. Never put `ambiguous` on multi-value facets such as `origin`, `activity`, `organization_group`, or `mod_subsystem`.
- Pick `role` from the player's storage-home mental model, not from recipe participation. Machine parts, machine components, hulls, casings, pumps, presses, pipes, cables, and placed processing parts are mechanisms or functional blocks, not generic materials, even when they are ingredients.
- Keep high-value inventory semantics first: `role`, `primary_uses`, `carry_frequency`, and `rarity` should be present unless the item data is genuinely unusable.
- Do not re-emit `stage2_facets` in `facets`. Use `corrections` only for clearly wrong stage-2 values; use `fill_ins` only for missing deterministic facets and only with values allowed by the schema.
- Vocabulary-backed facets may use only ids listed for that exact facet in `Pack facet vocabulary`. If that facet has no section, or no listed id fits, omit the facet and add `vocabulary_proposals` when a useful missing value is clear. Copy accepted ids exactly as printed; do not rewrite slashes, underscores, namespace, or pack prefix.
- Do not move ids across vocabulary-backed facets. A good `mod_subsystem` id such as `modid:kinetics` is not an `organization_group` unless that exact id is listed under `organization_group`; use the subsystem facet, omit the organization group, or add a vocabulary proposal for the missing storage bucket.
- For `organization_group`, use an accepted storage-bucket id when one clearly matches the item's manual storage family. Do not omit an obvious bucket such as molds, unprocessed ores, seeds, logs, cloth, or voltage components just because `role`, `form`, or `material_family` is already present.
- Emit `mod_subsystem` only when the item itself belongs to a listed subsystem. Never assign it just because the item is consumed or produced by a subsystem recipe.
- Optional low-evidence facets are better omitted than guessed.