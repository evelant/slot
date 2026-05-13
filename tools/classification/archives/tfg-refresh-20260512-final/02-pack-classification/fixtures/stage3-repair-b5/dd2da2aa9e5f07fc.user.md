# Items to classify
{
  "items": [
    {
      "id": "primitive_creatures:rw",
      "namespace": "primitive_creatures",
      "display_name": "𝕺𝖒𝖎𝖓𝖔𝖚𝖘 𝖚𝖕𝖌𝖗𝖆𝖉𝖊",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [
        "item/rw",
        "item/generated"
      ],
      "creative_tabs": [
        "primitive_creatures:t"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "primitive_creatures:entities/nahida"
      ],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Primitive Creatures"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "rare"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "primitive_creatures",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "rarity": {
          "value": "rare",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = rare"
        },
        "origin": {
          "values": [
            "mob_drop"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "primitive_creatures:tfc_spawn_egg",
      "namespace": "primitive_creatures",
      "display_name": "Kaolin Klayze Spawn Egg",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [
        "item/tfc_spawn_egg",
        "item/template_spawn_egg"
      ],
      "creative_tabs": [
        "primitive_creatures:t"
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
          "text": "Primitive Creatures"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "primitive_creatures",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "form": {
          "value": "special",
          "confidence": 1,
          "source": "rule:form_from_model",
          "rationale": "model item/template_spawn_egg"
        },
        "is_creative_only": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_creative_only_hardcoded",
          "rationale": "spawn egg id pattern"
        }
      }
    },
    {
      "id": "primitive_creatures:totem_0",
      "namespace": "primitive_creatures",
      "display_name": "Small Clay Idol",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "primitive_creatures:totemoo"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/totem_0",
        "item/generated"
      ],
      "creative_tabs": [
        "primitive_creatures:t"
      ],
      "loot_source_count": 10,
      "loot_source_examples": [
        "primitive_creatures:entities/iloger_1",
        "primitive_creatures:entities/iloger_10",
        "primitive_creatures:entities/iloger_2",
        "primitive_creatures:entities/iloger_3",
        "primitive_creatures:entities/iloger_4",
        "primitive_creatures:entities/iloger_5",
        "primitive_creatures:entities/iloger_6",
        "primitive_creatures:entities/piloger_9",
        "primitive_creatures:entities/viloger_10",
        "primitive_creatures:entities/wiloger"
      ],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Primitive Creatures"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "primitive_creatures",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "rarity": {
          "value": "uncommon",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = uncommon"
        },
        "processing_in": {
          "values": [
            "crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "mob_drop"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "primitive_creatures:totem_2",
      "namespace": "primitive_creatures",
      "display_name": "Wooden Lion-Man Idol",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "primitive_creatures:erg"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/totem_2",
        "item/generated"
      ],
      "creative_tabs": [
        "primitive_creatures:t"
      ],
      "loot_source_count": 9,
      "loot_source_examples": [
        "primitive_creatures:entities/iloger_1",
        "primitive_creatures:entities/iloger_2",
        "primitive_creatures:entities/iloger_3",
        "primitive_creatures:entities/iloger_4",
        "primitive_creatures:entities/iloger_5",
        "primitive_creatures:entities/iloger_6",
        "primitive_creatures:entities/piloger_9",
        "primitive_creatures:entities/viloger_10",
        "primitive_creatures:entities/wiloger"
      ],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Primitive Creatures"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "primitive_creatures",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "rarity": {
          "value": "uncommon",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = uncommon"
        },
        "processing_in": {
          "values": [
            "crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "mob_drop"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "primitive_creatures:totem_3",
      "namespace": "primitive_creatures",
      "display_name": "Hardened Mud Idol",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "tfg:shapeless/totem_3_decomp"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/totem_3",
        "item/generated"
      ],
      "creative_tabs": [
        "primitive_creatures:t"
      ],
      "loot_source_count": 9,
      "loot_source_examples": [
        "primitive_creatures:entities/iloger_1",
        "primitive_creatures:entities/iloger_2",
        "primitive_creatures:entities/iloger_3",
        "primitive_creatures:entities/iloger_4",
        "primitive_creatures:entities/iloger_5",
        "primitive_creatures:entities/iloger_6",
        "primitive_creatures:entities/piloger_9",
        "primitive_creatures:entities/viloger_10",
        "primitive_creatures:entities/wiloger"
      ],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Primitive Creatures"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "primitive_creatures",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "rarity": {
          "value": "uncommon",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = uncommon"
        },
        "processing_in": {
          "values": [
            "crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "mob_drop"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
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