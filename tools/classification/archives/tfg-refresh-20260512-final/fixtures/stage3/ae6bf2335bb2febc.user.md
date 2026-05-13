# Items to classify
{
  "items": [
    {
      "id": "framedblocks:framed_sliced_stairs_slab",
      "namespace": "framedblocks",
      "display_name": "Framed Sliced Stairs (Slab)",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "framedblocks:framed_sliced_stairs_slab",
        "framedblocks:framing_saw/framed_sliced_stairs_slab"
      ],
      "model_parents": [
        "item/framed_sliced_stairs_slab",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_sliced_stairs_slab"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_sliced_stairs_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
          "source": "rule:form_from_id",
          "rationale": "suffix _slab"
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
        }
      }
    },
    {
      "id": "framedblocks:framed_slope",
      "namespace": "framedblocks",
      "display_name": "Framed Slope",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 17,
        "crafting_shapeless": 5
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 22,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "framedblocks:framed_activator_rail_slope",
        "framedblocks:framed_corner_slope",
        "framedblocks:framed_detector_rail_slope",
        "framedblocks:framed_double_slope",
        "framedblocks:framed_fancy_activator_rail_slope",
        "framedblocks:framed_fancy_detector_rail_slope",
        "framedblocks:framed_fancy_powered_rail_slope",
        "framedblocks:framed_fancy_rail_slope",
        "framedblocks:framed_half_slope",
        "framedblocks:framed_inner_corner_slope",
        "framedblocks:framed_inner_prism",
        "framedblocks:framed_inner_prism_corner",
        "framedblocks:framed_inner_threeway_corner",
        "framedblocks:framed_powered_rail_slope",
        "framedblocks:framed_prism",
        "framedblocks:framed_prism_corner",
        "framedblocks:framed_pyramid_slab",
        "framedblocks:framed_rail_slope",
        "framedblocks:framed_slope_edge",
        "framedblocks:framed_slope_panel",
        "framedblocks:framed_slope_slab",
        "framedblocks:framed_threeway_corner"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_slope",
        "framedblocks:framing_saw/framed_slope"
      ],
      "model_parents": [
        "item/framed_slope",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_slope"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_slope",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
        }
      }
    },
    {
      "id": "framedblocks:framed_slope_edge",
      "namespace": "framedblocks",
      "display_name": "Framed Slope Edge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 3
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "framedblocks:framed_elevated_double_slope_edge",
        "framedblocks:framed_elevated_slope_edge",
        "framedblocks:framed_stacked_slope_edge"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_slope_edge",
        "framedblocks:framing_saw/framed_slope_edge"
      ],
      "model_parents": [
        "item/framed_slope_edge",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_slope_edge"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_slope_edge",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
        }
      }
    },
    {
      "id": "framedblocks:framed_slope_panel",
      "namespace": "framedblocks",
      "display_name": "Framed Slope Panel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 12,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 13,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "framedblocks:framed_compound_slope_panel",
        "framedblocks:framed_double_slope_panel",
        "framedblocks:framed_extended_double_slope_panel",
        "framedblocks:framed_extended_slope_panel",
        "framedblocks:framed_flat_inner_slope_panel_corner",
        "framedblocks:framed_flat_slope_panel_corner",
        "framedblocks:framed_large_corner_slope_panel",
        "framedblocks:framed_large_inner_corner_slope_panel",
        "framedblocks:framed_pyramid",
        "framedblocks:framed_slope_panel_rotate_to_framed_slope_slab",
        "framedblocks:framed_small_corner_slope_panel",
        "framedblocks:framed_small_inner_corner_slope_panel",
        "framedblocks:framed_stacked_slope_panel"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_slope_panel",
        "framedblocks:framed_slope_slab_rotate_to_framed_slope_panel",
        "framedblocks:framing_saw/framed_slope_panel"
      ],
      "model_parents": [
        "item/framed_slope_panel",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_slope_panel"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_slope_panel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
        }
      }
    },
    {
      "id": "framedblocks:framed_slope_slab",
      "namespace": "framedblocks",
      "display_name": "Framed Slope Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 7,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 8,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "framedblocks:framed_compound_slope_slab",
        "framedblocks:framed_double_slope_slab",
        "framedblocks:framed_elevated_double_slope_slab",
        "framedblocks:framed_elevated_slope_slab",
        "framedblocks:framed_flat_inner_slope_slab_corner",
        "framedblocks:framed_flat_slope_slab_corner",
        "framedblocks:framed_slope_slab_rotate_to_framed_slope_panel",
        "framedblocks:framed_stacked_slope_slab"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_slope_panel_rotate_to_framed_slope_slab",
        "framedblocks:framed_slope_slab",
        "framedblocks:framing_saw/framed_slope_slab"
      ],
      "model_parents": [
        "item/framed_slope_slab",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_slope_slab"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_slope_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold sneak key to place upside down"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
          "source": "rule:form_from_id",
          "rationale": "suffix _slab"
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
        }
      }
    },
    {
      "id": "framedblocks:framed_sloped_prism",
      "namespace": "framedblocks",
      "display_name": "Framed Sloped Prism",
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
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "framedblocks:framed_double_sloped_prism"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_sloped_prism",
        "framedblocks:framing_saw/framed_sloped_prism"
      ],
      "model_parents": [
        "item/framed_sloped_prism",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_sloped_prism"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_sloped_prism",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
        }
      }
    },
    {
      "id": "framedblocks:framed_sloped_stairs",
      "namespace": "framedblocks",
      "display_name": "Framed Sloped Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "framedblocks:framed_sloped_stairs",
        "framedblocks:framing_saw/framed_sloped_stairs"
      ],
      "model_parents": [
        "item/framed_sloped_stairs",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_sloped_stairs"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_sloped_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _stairs"
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
        }
      }
    },
    {
      "id": "framedblocks:framed_small_corner_slope_panel",
      "namespace": "framedblocks",
      "display_name": "Framed Small Corner Slope Panel",
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
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "framedblocks:framed_ext_inner_double_corner_slope_panel",
        "framedblocks:framed_small_double_corner_slope_panel"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_small_corner_slope_panel",
        "framedblocks:framing_saw/framed_small_corner_slope_panel"
      ],
      "model_parents": [
        "item/framed_small_corner_slope_panel",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 2,
      "loot_source_examples": [
        "framedblocks:blocks/framed_small_corner_slope_panel",
        "framedblocks:blocks/framed_small_corner_slope_panel_w"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_small_corner_slope_panel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
        }
      }
    },
    {
      "id": "framedblocks:framed_small_double_corner_slope_panel",
      "namespace": "framedblocks",
      "display_name": "Framed Small Double Corner Slope Panel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "framedblocks:framed_small_double_corner_slope_panel",
        "framedblocks:framing_saw/framed_small_double_corner_slope_panel"
      ],
      "model_parents": [
        "item/framed_small_double_corner_slope_panel",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 2,
      "loot_source_examples": [
        "framedblocks:blocks/framed_small_double_corner_slope_panel",
        "framedblocks:blocks/framed_small_double_corner_slope_panel_w"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_small_double_corner_slope_panel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        }
      }
    },
    {
      "id": "framedblocks:framed_small_inner_corner_slope_panel",
      "namespace": "framedblocks",
      "display_name": "Framed Small Inner Corner Slope Panel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "framedblocks:framed_ext_inner_corner_slope_panel",
        "framedblocks:framed_inv_double_corner_slope_panel",
        "framedblocks:framed_small_double_corner_slope_panel",
        "framedblocks:framed_stacked_inner_corner_slope_panel"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_small_inner_corner_slope_panel",
        "framedblocks:framing_saw/framed_small_inner_corner_slope_panel"
      ],
      "model_parents": [
        "item/framed_small_inner_corner_slope_panel",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 2,
      "loot_source_examples": [
        "framedblocks:blocks/framed_small_inner_corner_slope_panel",
        "framedblocks:blocks/framed_small_inner_corner_slope_panel_w"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_small_inner_corner_slope_panel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
        }
      }
    },
    {
      "id": "framedblocks:framed_soul_torch",
      "namespace": "framedblocks",
      "display_name": "Framed Soul Torch",
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
        "item/framed_soul_torch",
        "item/generated"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 2,
      "loot_source_examples": [
        "framedblocks:blocks/framed_soul_torch",
        "framedblocks:blocks/framed_soul_wall_torch"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_soul_torch",
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:light_emission": 14,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
          "value": "torch",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _torch"
        },
        "emits_light": {
          "value": true,
          "confidence": 1,
          "source": "rule:emits_light_from_component"
        }
      }
    },
    {
      "id": "framedblocks:framed_stacked_corner_slope_panel",
      "namespace": "framedblocks",
      "display_name": "Framed Stacked Corner Slope Panel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "framedblocks:framed_stacked_corner_slope_panel",
        "framedblocks:framing_saw/framed_stacked_corner_slope_panel"
      ],
      "model_parents": [
        "item/framed_stacked_corner_slope_panel",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 2,
      "loot_source_examples": [
        "framedblocks:blocks/framed_stacked_corner_slope_panel",
        "framedblocks:blocks/framed_stacked_corner_slope_panel_w"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_stacked_corner_slope_panel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        }
      }
    },
    {
      "id": "framedblocks:framed_stacked_inner_corner_slope_panel",
      "namespace": "framedblocks",
      "display_name": "Framed Stacked Inner Corner Slope Panel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "framedblocks:framed_stacked_inner_corner_slope_panel",
        "framedblocks:framing_saw/framed_stacked_inner_corner_slope_panel"
      ],
      "model_parents": [
        "item/framed_stacked_inner_corner_slope_panel",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 2,
      "loot_source_examples": [
        "framedblocks:blocks/framed_stacked_inner_corner_slope_panel",
        "framedblocks:blocks/framed_stacked_inner_corner_slope_panel_w"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_stacked_inner_corner_slope_panel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        }
      }
    },
    {
      "id": "framedblocks:framed_stacked_slope_edge",
      "namespace": "framedblocks",
      "display_name": "Framed Stacked Slope Edge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "framedblocks:framed_stacked_slope_edge",
        "framedblocks:framing_saw/framed_stacked_slope_edge"
      ],
      "model_parents": [
        "item/framed_stacked_slope_edge",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_stacked_slope_edge"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_stacked_slope_edge",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
        }
      }
    },
    {
      "id": "framedblocks:framed_stacked_slope_panel",
      "namespace": "framedblocks",
      "display_name": "Framed Stacked Slope Panel",
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
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "framedblocks:framed_stacked_slope_panel_rotate_to_framed_stacked_slope_slab"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_stacked_slope_panel",
        "framedblocks:framed_stacked_slope_slab_rotate_to_framed_stacked_slope_panel",
        "framedblocks:framing_saw/framed_stacked_slope_panel"
      ],
      "model_parents": [
        "item/framed_stacked_slope_panel",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_stacked_slope_panel"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_stacked_slope_panel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
        }
      }
    },
    {
      "id": "framedblocks:framed_stacked_slope_slab",
      "namespace": "framedblocks",
      "display_name": "Framed Stacked Slope Slab",
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
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "framedblocks:framed_stacked_slope_slab_rotate_to_framed_stacked_slope_panel"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_stacked_slope_panel_rotate_to_framed_stacked_slope_slab",
        "framedblocks:framed_stacked_slope_slab",
        "framedblocks:framing_saw/framed_stacked_slope_slab"
      ],
      "model_parents": [
        "item/framed_stacked_slope_slab",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_stacked_slope_slab"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_stacked_slope_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
          "source": "rule:form_from_id",
          "rationale": "suffix _slab"
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
        }
      }
    },
    {
      "id": "framedblocks:framed_stairs",
      "namespace": "framedblocks",
      "display_name": "Framed Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:stairs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 3,
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "framedblocks:framed_double_stairs",
        "framedblocks:framed_elevated_slope_edge",
        "framedblocks:framed_half_stairs",
        "framedblocks:framed_stacked_slope_edge",
        "framedblocks:framed_stairs_rotate_to_framed_vertical_stairs"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_stairs",
        "framedblocks:framed_vertical_stairs_rotate_to_framed_stairs",
        "framedblocks:framing_saw/framed_stairs"
      ],
      "model_parents": [
        "item/framed_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_stairs"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe",
          "minecraft:stairs"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
      "id": "framedblocks:framed_stone_button",
      "namespace": "framedblocks",
      "display_name": "Framed Stone Button",
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
        "crafting_shapeless": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "framedblocks:framed_large_stone_button"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_stone_button",
        "framedblocks:framing_saw/framed_stone_button"
      ],
      "model_parents": [
        "item/framed_stone_button",
        "block/block"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_stone_button"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_stone_button",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
          "value": "button",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _button"
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
        }
      }
    },
    {
      "id": "framedblocks:framed_stone_pressure_plate",
      "namespace": "framedblocks",
      "display_name": "Framed Stone Pressure Plate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "framedblocks:framed_stone_pressure_plate",
        "framedblocks:framing_saw/framed_stone_pressure_plate"
      ],
      "model_parents": [
        "item/framed_stone_pressure_plate",
        "block/framed_pressure_plate_up",
        "block/thin_block"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 2,
      "loot_source_examples": [
        "framedblocks:blocks/framed_stone_pressure_plate",
        "framedblocks:blocks/framed_waterloggable_stone_pressure_plate"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_stone_pressure_plate",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Can be placed on ceilings"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
          "value": "pressure_plate",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _pressure_plate"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        }
      }
    },
    {
      "id": "framedblocks:framed_target",
      "namespace": "framedblocks",
      "display_name": "Framed Target",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "framedblocks:framed_target",
        "framedblocks:framing_saw/framed_target"
      ],
      "model_parents": [
        "item/framed_target",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_target"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_target",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
        }
      }
    },
    {
      "id": "framedblocks:framed_thick_lattice",
      "namespace": "framedblocks",
      "display_name": "Framed Thick Lattice",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "framedblocks:framed_thick_lattice",
        "framedblocks:framing_saw/framed_thick_lattice"
      ],
      "model_parents": [
        "item/framed_thick_lattice",
        "block/cube"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_thick_lattice"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_thick_lattice",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
        }
      }
    },
    {
      "id": "framedblocks:framed_threeway_corner",
      "namespace": "framedblocks",
      "display_name": "Framed Threeway Corner",
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
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "framedblocks:framed_double_threeway_corner"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_threeway_corner",
        "framedblocks:framing_saw/framed_threeway_corner"
      ],
      "model_parents": [
        "item/framed_threeway_corner",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_threeway_corner"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_threeway_corner",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
        }
      }
    },
    {
      "id": "framedblocks:framed_threeway_corner_pillar",
      "namespace": "framedblocks",
      "display_name": "Framed Threeway Corner Pillar",
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
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "framedblocks:framed_double_threeway_corner_pillar"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_threeway_corner_pillar",
        "framedblocks:framing_saw/framed_threeway_corner_pillar"
      ],
      "model_parents": [
        "item/framed_threeway_corner_pillar",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_threeway_corner_pillar"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_threeway_corner_pillar",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
        }
      }
    },
    {
      "id": "framedblocks:framed_torch",
      "namespace": "framedblocks",
      "display_name": "Framed Torch",
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
        "item/framed_torch",
        "item/generated"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 2,
      "loot_source_examples": [
        "framedblocks:blocks/framed_torch",
        "framedblocks:blocks/framed_wall_torch"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_torch",
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:light_emission": 14,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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
          "value": "torch",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _torch"
        },
        "emits_light": {
          "value": true,
          "confidence": 1,
          "source": "rule:emits_light_from_component"
        }
      }
    },
    {
      "id": "framedblocks:framed_trapdoor",
      "namespace": "framedblocks",
      "display_name": "Framed Trapdoor",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trapdoors"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "framedblocks:framed_iron_trapdoor",
        "tfg:immersive_aircraft/shaped/sail"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_trapdoor",
        "framedblocks:framing_saw/framed_trapdoor"
      ],
      "model_parents": [
        "item/framed_trapdoor",
        "block/template_orientable_trapdoor_bottom"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_trapdoor"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_trapdoor",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe",
          "minecraft:trapdoors"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "framedblocks",
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