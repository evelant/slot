# Items to classify
{
  "items": [
    {
      "id": "minecraft:dark_oak_fence_gate",
      "namespace": "minecraft",
      "display_name": "Dark Oak Fence Gate",
      "minecraft_tags_direct": [
        "minecraft:fence_gates"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:dark_oak_fence_gate"
      ],
      "model_parents": [
        "block/dark_oak_fence_gate",
        "block/template_fence_gate",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/dark_oak_fence_gate"
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
          "source": "rule:material_family_from_id",
          "rationale": "id prefix dark_oak_"
        },
        "form": {
          "value": "fence_gate",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:fence_gates"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
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
      "id": "minecraft:dark_oak_hanging_sign",
      "namespace": "minecraft",
      "display_name": "Dark Oak Hanging Sign",
      "minecraft_tags_direct": [
        "minecraft:hanging_signs"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:dark_oak_hanging_sign"
      ],
      "model_parents": [
        "item/dark_oak_hanging_sign",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/dark_oak_hanging_sign"
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
          "source": "rule:material_family_from_id",
          "rationale": "id prefix dark_oak_"
        },
        "form": {
          "value": "hanging_sign",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:hanging_signs"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
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
      "id": "minecraft:dark_oak_leaves",
      "namespace": "minecraft",
      "display_name": "Dark Oak Leaves",
      "minecraft_tags_direct": [
        "minecraft:leaves"
      ],
      "minecraft_tags_inherited": [
        "minecraft:completes_find_tree_tutorial"
      ],
      "processing_in": [
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "smelting": 1
      },
      "recipe_production_by_type": {},
      "sample_ingredient_of": [
        "minecraft:leaf_litter"
      ],
      "sample_output_of": [],
      "model_parents": [
        "block/dark_oak_leaves",
        "block/leaves",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/dark_oak_leaves"
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
          "source": "rule:material_family_from_id",
          "rationale": "id prefix dark_oak_"
        },
        "required_tool": {
          "value": "hoe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/hoe"
        },
        "processing_in": {
          "values": [
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "overworld_surface"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:dark_oak_log",
      "namespace": "minecraft",
      "display_name": "Dark Oak Log",
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
        "crafting_shaped": 4,
        "smelting": 1,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "sample_ingredient_of": [
        "minecraft:campfire",
        "minecraft:charcoal",
        "minecraft:dark_oak_planks",
        "minecraft:dark_oak_wood",
        "minecraft:smoker",
        "minecraft:soul_campfire"
      ],
      "sample_output_of": [],
      "model_parents": [
        "block/dark_oak_log",
        "block/cube_column",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/dark_oak_log",
        "minecraft:chests/pillager_outpost",
        "minecraft:chests/spawn_bonus_chest"
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
          "value": "log",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:logs"
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
            "overworld_surface",
            "pillager_outpost"
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
      "id": "minecraft:dark_oak_planks",
      "namespace": "minecraft",
      "display_name": "Dark Oak Planks",
      "minecraft_tags_direct": [
        "minecraft:planks"
      ],
      "minecraft_tags_inherited": [
        "minecraft:sulfur_cube_archetype/bouncy",
        "minecraft:sulfur_cube_swallowable",
        "minecraft:wooden_tool_materials"
      ],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 49,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "sample_ingredient_of": [
        "minecraft:barrel",
        "minecraft:beehive",
        "minecraft:black_bed",
        "minecraft:blue_bed",
        "minecraft:bookshelf",
        "minecraft:bowl",
        "minecraft:brown_bed",
        "minecraft:cartography_table",
        "minecraft:chest",
        "minecraft:chiseled_bookshelf"
      ],
      "sample_output_of": [
        "minecraft:dark_oak_planks"
      ],
      "model_parents": [
        "block/dark_oak_planks",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/dark_oak_planks"
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
          "value": "wood_oak",
          "confidence": 1,
          "source": "rule:material_family_from_tag",
          "rationale": "tag minecraft:wooden_tool_materials"
        },
        "form": {
          "value": "whole_block",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:planks"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
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