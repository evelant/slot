# Items to classify
{
  "items": [
    {
      "id": "minecraft:brush",
      "namespace": "minecraft",
      "display_name": "Brush",
      "minecraft_tags_direct": [
        "minecraft:enchantable/durability"
      ],
      "minecraft_tags_inherited": [
        "minecraft:enchantable/vanishing"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:brush"
      ],
      "model_parents": [
        "item/brush_brushing_0",
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
        "has_durability": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_durability_from_component"
        }
      }
    },
    {
      "id": "minecraft:bubble_coral",
      "namespace": "minecraft",
      "display_name": "Bubble Coral",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "item/bubble_coral",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/bubble_coral"
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
      "id": "minecraft:bubble_coral_block",
      "namespace": "minecraft",
      "display_name": "Bubble Coral Block",
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
        "block/bubble_coral_block",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/bubble_coral_block"
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
    },
    {
      "id": "minecraft:bubble_coral_fan",
      "namespace": "minecraft",
      "display_name": "Bubble Coral Fan",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "item/bubble_coral_fan",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/bubble_coral_fan"
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
      "id": "minecraft:bucket",
      "namespace": "minecraft",
      "display_name": "Bucket",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:bucket"
      ],
      "model_parents": [
        "item/bucket",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/simple_dungeon",
        "minecraft:chests/trial_chambers/intersection_barrel",
        "minecraft:chests/village/village_savanna_house",
        "minecraft:chests/woodland_mansion"
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
        "form": {
          "value": "bucket",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "exact id"
        },
        "origin": {
          "values": [
            "overworld_cave",
            "trial_chamber",
            "village",
            "woodland_mansion"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    }
  ]
}
Respond with a single JSON object matching the expected output shape above. No other text.