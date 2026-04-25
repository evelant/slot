# Items to classify
{
  "items": [
    {
      "id": "minecraft:field_masoned_banner_pattern",
      "namespace": "minecraft",
      "display_name": "Field Masoned Banner Pattern",
      "minecraft_tags_direct": [
        "minecraft:loom_patterns"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:field_masoned_banner_pattern"
      ],
      "model_parents": [
        "item/field_masoned_banner_pattern",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {
        "minecraft:provides_banner_patterns": "#minecraft:pattern_item/field_masoned",
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "minecraft:filled_map",
      "namespace": "minecraft",
      "display_name": "Map",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_transmute": 1,
        "crafting_special_mapextending": 1
      },
      "recipe_production_by_type": {
        "crafting_transmute": 1,
        "crafting_special_mapextending": 1
      },
      "sample_ingredient_of": [
        "minecraft:map_cloning",
        "minecraft:map_extending"
      ],
      "sample_output_of": [
        "minecraft:map_cloning",
        "minecraft:map_extending"
      ],
      "model_parents": [
        "item/filled_map",
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
      "id": "minecraft:fire_charge",
      "namespace": "minecraft",
      "display_name": "Fire Charge",
      "minecraft_tags_direct": [
        "minecraft:creeper_igniters"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_special_firework_star": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "sample_ingredient_of": [
        "minecraft:firework_star"
      ],
      "sample_output_of": [
        "minecraft:fire_charge"
      ],
      "model_parents": [
        "item/fire_charge",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/ruined_portal",
        "minecraft:dispensers/trial_chambers/chamber",
        "minecraft:gameplay/piglin_bartering",
        "minecraft:spawners/trial_chamber/items_to_drop_when_ominous"
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
            "ruined_portal",
            "trading",
            "trial_chamber"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:fire_coral",
      "namespace": "minecraft",
      "display_name": "Fire Coral",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "item/fire_coral",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/fire_coral"
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
      "id": "minecraft:fire_coral_block",
      "namespace": "minecraft",
      "display_name": "Fire Coral Block",
      "minecraft_tags_direct": [
        "minecraft:sulfur_cube_archetype/fast_flat"
      ],
      "minecraft_tags_inherited": [
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "block/fire_coral_block",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/fire_coral_block"
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
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        }
      }
    }
  ]
}
Respond with a single JSON object matching the expected output shape above. No other text.