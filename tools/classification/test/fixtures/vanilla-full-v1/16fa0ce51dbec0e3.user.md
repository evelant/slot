# Items to classify
{
  "items": [
    {
      "id": "minecraft:dark_oak_stairs",
      "namespace": "minecraft",
      "display_name": "Dark Oak Stairs",
      "minecraft_tags_direct": [
        "minecraft:wooden_stairs"
      ],
      "minecraft_tags_inherited": [
        "minecraft:stairs"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:dark_oak_stairs"
      ],
      "model_parents": [
        "block/dark_oak_stairs",
        "block/stairs",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/dark_oak_stairs"
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
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:stairs"
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
      "id": "minecraft:dark_oak_trapdoor",
      "namespace": "minecraft",
      "display_name": "Dark Oak Trapdoor",
      "minecraft_tags_direct": [
        "minecraft:wooden_trapdoors"
      ],
      "minecraft_tags_inherited": [
        "minecraft:trapdoors"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:dark_oak_trapdoor"
      ],
      "model_parents": [
        "block/dark_oak_trapdoor_bottom",
        "block/template_trapdoor_bottom",
        "block/thin_block",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/dark_oak_trapdoor"
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
          "value": "trapdoor",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:trapdoors"
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
      "id": "minecraft:dark_oak_wood",
      "namespace": "minecraft",
      "display_name": "Dark Oak Wood",
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
        "minecraft:dark_oak_wood"
      ],
      "model_parents": [
        "block/dark_oak_wood",
        "block/cube_column",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/dark_oak_wood"
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
          "rationale": "id suffix _wood"
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
            "overworld_surface"
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
      "id": "minecraft:dark_prismarine",
      "namespace": "minecraft",
      "display_name": "Dark Prismarine",
      "minecraft_tags_direct": [
        "minecraft:sulfur_cube_archetype/regular"
      ],
      "minecraft_tags_inherited": [
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [
        "crafting",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "stonecutting": 2
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:dark_prismarine_slab",
        "minecraft:dark_prismarine_slab_from_dark_prismarine_stonecutting",
        "minecraft:dark_prismarine_stairs",
        "minecraft:dark_prismarine_stairs_from_dark_prismarine_stonecutting"
      ],
      "sample_output_of": [
        "minecraft:dark_prismarine"
      ],
      "model_parents": [
        "block/dark_prismarine",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/dark_prismarine"
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
        },
        "processing_in": {
          "values": [
            "crafting",
            "stonecutting"
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
        }
      }
    },
    {
      "id": "minecraft:dark_prismarine_slab",
      "namespace": "minecraft",
      "display_name": "Dark Prismarine Slab",
      "minecraft_tags_direct": [
        "minecraft:slabs"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:dark_prismarine_slab",
        "minecraft:dark_prismarine_slab_from_dark_prismarine_stonecutting"
      ],
      "model_parents": [
        "block/dark_prismarine_slab",
        "block/slab",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/dark_prismarine_slab"
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
        "form": {
          "value": "slab",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:slabs"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "origin": {
          "values": [
            "crafted_only"
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