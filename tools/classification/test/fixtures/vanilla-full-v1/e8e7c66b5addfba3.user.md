# Items to classify
{
  "items": [
    {
      "id": "minecraft:horn_coral_fan",
      "namespace": "minecraft",
      "display_name": "Horn Coral Fan",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "item/horn_coral_fan",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/horn_coral_fan"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        }
      }
    },
    {
      "id": "minecraft:horse_spawn_egg",
      "namespace": "minecraft",
      "display_name": "Horse Spawn Egg",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "item/horse_spawn_egg",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        }
      }
    },
    {
      "id": "minecraft:host_armor_trim_smithing_template",
      "namespace": "minecraft",
      "display_name": "Smithing Template",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting",
        "smithing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "smithing_trim": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:host_armor_trim_smithing_template",
        "minecraft:host_armor_trim_smithing_template_smithing_trim"
      ],
      "sample_output_of": [
        "minecraft:host_armor_trim_smithing_template"
      ],
      "model_parents": [
        "item/host_armor_trim_smithing_template",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:archaeology/trail_ruins_rare"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
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
            "crafting",
            "smithing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "archaeology_site"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:howl_pottery_sherd",
      "namespace": "minecraft",
      "display_name": "Howl Pottery Sherd",
      "minecraft_tags_direct": [
        "minecraft:decorated_pot_sherds"
      ],
      "minecraft_tags_inherited": [
        "minecraft:decorated_pot_ingredients"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "item/howl_pottery_sherd",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:archaeology/trail_ruins_rare"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "rarity": {
          "value": "uncommon",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = uncommon"
        },
        "origin": {
          "values": [
            "archaeology_site"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:husk_spawn_egg",
      "namespace": "minecraft",
      "display_name": "Husk Spawn Egg",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "item/husk_spawn_egg",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        }
      }
    }
  ]
}
Respond with a single JSON object matching the expected output shape above. No other text.