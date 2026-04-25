# Items to classify
{
  "items": [
    {
      "id": "minecraft:cut_copper",
      "namespace": "minecraft",
      "display_name": "Cut Copper",
      "minecraft_tags_direct": [
        "minecraft:sulfur_cube_archetype/slow_flat"
      ],
      "minecraft_tags_inherited": [
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [
        "crafting",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "stonecutting": 3,
        "crafting_shaped": 2,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 1
      },
      "sample_ingredient_of": [
        "minecraft:chiseled_copper_from_cut_copper_stonecutting",
        "minecraft:cut_copper_slab",
        "minecraft:cut_copper_slab_from_cut_copper_stonecutting",
        "minecraft:cut_copper_stairs",
        "minecraft:cut_copper_stairs_from_cut_copper_stonecutting",
        "minecraft:waxed_cut_copper_from_honeycomb"
      ],
      "sample_output_of": [
        "minecraft:cut_copper",
        "minecraft:cut_copper_from_copper_block_stonecutting"
      ],
      "model_parents": [
        "block/cut_copper",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/cut_copper"
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "minecraft:cut_copper_slab",
      "namespace": "minecraft",
      "display_name": "Cut Copper Slab",
      "minecraft_tags_direct": [
        "minecraft:slabs"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 2
      },
      "sample_ingredient_of": [
        "minecraft:chiseled_copper",
        "minecraft:waxed_cut_copper_slab_from_honeycomb"
      ],
      "sample_output_of": [
        "minecraft:cut_copper_slab",
        "minecraft:cut_copper_slab_from_copper_block_stonecutting",
        "minecraft:cut_copper_slab_from_cut_copper_stonecutting"
      ],
      "model_parents": [
        "block/cut_copper_slab",
        "block/slab",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/cut_copper_slab"
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
          "value": "copper",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix cut_copper_"
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
        }
      }
    },
    {
      "id": "minecraft:cut_copper_stairs",
      "namespace": "minecraft",
      "display_name": "Cut Copper Stairs",
      "minecraft_tags_direct": [
        "minecraft:stairs"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 2
      },
      "sample_ingredient_of": [
        "minecraft:waxed_cut_copper_stairs_from_honeycomb"
      ],
      "sample_output_of": [
        "minecraft:cut_copper_stairs",
        "minecraft:cut_copper_stairs_from_copper_block_stonecutting",
        "minecraft:cut_copper_stairs_from_cut_copper_stonecutting"
      ],
      "model_parents": [
        "block/cut_copper_stairs",
        "block/stairs",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/cut_copper_stairs"
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
          "value": "copper",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix cut_copper_"
        },
        "form": {
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:stairs"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
        }
      }
    },
    {
      "id": "minecraft:cut_red_sandstone",
      "namespace": "minecraft",
      "display_name": "Cut Red Sandstone",
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
        "stonecutting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 1
      },
      "sample_ingredient_of": [
        "minecraft:cut_red_sandstone_slab",
        "minecraft:cut_red_sandstone_slab_from_cut_red_sandstone_stonecutting",
        "minecraft:red_sandstone_stairs"
      ],
      "sample_output_of": [
        "minecraft:cut_red_sandstone",
        "minecraft:cut_red_sandstone_from_red_sandstone_stonecutting"
      ],
      "model_parents": [
        "block/cut_red_sandstone",
        "block/cube_column",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/cut_red_sandstone"
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
      "id": "minecraft:cut_red_sandstone_slab",
      "namespace": "minecraft",
      "display_name": "Cut Red Sandstone Slab",
      "minecraft_tags_direct": [
        "minecraft:slabs"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 2
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:cut_red_sandstone_slab",
        "minecraft:cut_red_sandstone_slab_from_cut_red_sandstone_stonecutting",
        "minecraft:cut_red_sandstone_slab_from_red_sandstone_stonecutting"
      ],
      "model_parents": [
        "block/cut_red_sandstone_slab",
        "block/slab",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/cut_red_sandstone_slab"
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