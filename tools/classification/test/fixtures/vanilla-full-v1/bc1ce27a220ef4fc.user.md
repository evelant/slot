# Items to classify
{
  "items": [
    {
      "id": "minecraft:stripped_dark_oak_wood",
      "namespace": "minecraft",
      "display_name": "Stripped Dark Oak Wood",
      "minecraft_tags_direct": [
        "minecraft:dark_oak_logs"
      ],
      "minecraft_tags_inherited": [
        "minecraft:completes_find_tree_tutorial",
        "minecraft:logs",
        "minecraft:logs_that_burn",
        "minecraft:sulfur_cube_archetype/bouncy",
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [
        "crafting",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 3,
        "smelting": 1,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:campfire",
        "minecraft:charcoal",
        "minecraft:dark_oak_planks",
        "minecraft:smoker",
        "minecraft:soul_campfire"
      ],
      "sample_output_of": [
        "minecraft:stripped_dark_oak_wood"
      ],
      "model_parents": [
        "block/stripped_dark_oak_wood",
        "block/cube_column",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/stripped_dark_oak_wood"
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
        "material_family": {
          "value": "wood_dark_oak",
          "confidence": 1,
          "source": "rule:material_family_from_tag",
          "rationale": "log tag minecraft:dark_oak_logs"
        },
        "form": {
          "value": "wood",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "stripped wood"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "crafted_only"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        },
        "is_fuel": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_fuel_from_id_or_tag"
        }
      }
    },
    {
      "id": "minecraft:stripped_jungle_log",
      "namespace": "minecraft",
      "display_name": "Stripped Jungle Log",
      "minecraft_tags_direct": [
        "minecraft:jungle_logs"
      ],
      "minecraft_tags_inherited": [
        "minecraft:completes_find_tree_tutorial",
        "minecraft:logs",
        "minecraft:logs_that_burn",
        "minecraft:sulfur_cube_archetype/bouncy",
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [
        "crafting",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 6,
        "smelting": 1,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "sample_ingredient_of": [
        "minecraft:campfire",
        "minecraft:charcoal",
        "minecraft:jungle_hanging_sign",
        "minecraft:jungle_planks",
        "minecraft:jungle_shelf",
        "minecraft:smoker",
        "minecraft:soul_campfire",
        "minecraft:stripped_jungle_wood"
      ],
      "sample_output_of": [],
      "model_parents": [
        "block/stripped_jungle_log",
        "block/cube_column",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/stripped_jungle_log"
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
        "material_family": {
          "value": "wood_jungle",
          "confidence": 1,
          "source": "rule:material_family_from_tag",
          "rationale": "log tag minecraft:jungle_logs"
        },
        "form": {
          "value": "stripped_log",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "stripped log"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "is_fuel": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_fuel_from_id_or_tag"
        }
      }
    },
    {
      "id": "minecraft:stripped_jungle_wood",
      "namespace": "minecraft",
      "display_name": "Stripped Jungle Wood",
      "minecraft_tags_direct": [
        "minecraft:jungle_logs"
      ],
      "minecraft_tags_inherited": [
        "minecraft:completes_find_tree_tutorial",
        "minecraft:logs",
        "minecraft:logs_that_burn",
        "minecraft:sulfur_cube_archetype/bouncy",
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [
        "crafting",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 3,
        "smelting": 1,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:campfire",
        "minecraft:charcoal",
        "minecraft:jungle_planks",
        "minecraft:smoker",
        "minecraft:soul_campfire"
      ],
      "sample_output_of": [
        "minecraft:stripped_jungle_wood"
      ],
      "model_parents": [
        "block/stripped_jungle_wood",
        "block/cube_column",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/stripped_jungle_wood"
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
        "material_family": {
          "value": "wood_jungle",
          "confidence": 1,
          "source": "rule:material_family_from_tag",
          "rationale": "log tag minecraft:jungle_logs"
        },
        "form": {
          "value": "wood",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "stripped wood"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "crafted_only"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        },
        "is_fuel": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_fuel_from_id_or_tag"
        }
      }
    },
    {
      "id": "minecraft:stripped_mangrove_log",
      "namespace": "minecraft",
      "display_name": "Stripped Mangrove Log",
      "minecraft_tags_direct": [
        "minecraft:mangrove_logs"
      ],
      "minecraft_tags_inherited": [
        "minecraft:completes_find_tree_tutorial",
        "minecraft:logs",
        "minecraft:logs_that_burn",
        "minecraft:sulfur_cube_archetype/bouncy",
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [
        "crafting",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 6,
        "smelting": 1,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "sample_ingredient_of": [
        "minecraft:campfire",
        "minecraft:charcoal",
        "minecraft:mangrove_hanging_sign",
        "minecraft:mangrove_planks",
        "minecraft:mangrove_shelf",
        "minecraft:smoker",
        "minecraft:soul_campfire",
        "minecraft:stripped_mangrove_wood"
      ],
      "sample_output_of": [],
      "model_parents": [
        "block/stripped_mangrove_log",
        "block/cube_column",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/stripped_mangrove_log"
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
        "material_family": {
          "value": "wood_mangrove",
          "confidence": 1,
          "source": "rule:material_family_from_tag",
          "rationale": "log tag minecraft:mangrove_logs"
        },
        "form": {
          "value": "stripped_log",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "stripped log"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "is_fuel": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_fuel_from_id_or_tag"
        }
      }
    },
    {
      "id": "minecraft:stripped_mangrove_wood",
      "namespace": "minecraft",
      "display_name": "Stripped Mangrove Wood",
      "minecraft_tags_direct": [
        "minecraft:mangrove_logs"
      ],
      "minecraft_tags_inherited": [
        "minecraft:completes_find_tree_tutorial",
        "minecraft:logs",
        "minecraft:logs_that_burn",
        "minecraft:sulfur_cube_archetype/bouncy",
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [
        "crafting",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 3,
        "smelting": 1,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:campfire",
        "minecraft:charcoal",
        "minecraft:mangrove_planks",
        "minecraft:smoker",
        "minecraft:soul_campfire"
      ],
      "sample_output_of": [
        "minecraft:stripped_mangrove_wood"
      ],
      "model_parents": [
        "block/stripped_mangrove_wood",
        "block/cube_column",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/stripped_mangrove_wood"
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
        "material_family": {
          "value": "wood_mangrove",
          "confidence": 1,
          "source": "rule:material_family_from_tag",
          "rationale": "log tag minecraft:mangrove_logs"
        },
        "form": {
          "value": "wood",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "stripped wood"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "crafted_only"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        },
        "is_fuel": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_fuel_from_id_or_tag"
        }
      }
    }
  ]
}
Respond with a single JSON object matching the expected output shape above. No other text.