# Items to classify
{
  "items": [
    {
      "id": "framedblocks:framed_compound_slope_slab",
      "namespace": "framedblocks",
      "display_name": "Framed Compound Slope Slab",
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
        "framedblocks:framed_compound_slope_slab",
        "framedblocks:framing_saw/framed_compound_slope_slab"
      ],
      "model_parents": [
        "item/framed_compound_slope_slab",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_compound_slope_slab"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_compound_slope_slab",
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
      "id": "framedblocks:framed_corner_pillar",
      "namespace": "framedblocks",
      "display_name": "Framed Corner Pillar",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 5,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "framedblocks:framed_divided_panel_vertical",
        "framedblocks:framed_ext_corner_slope_panel",
        "framedblocks:framed_pillar",
        "framedblocks:framed_stacked_corner_slope_panel",
        "framedblocks:framed_threeway_corner_pillar",
        "framedblocks:framed_vertical_double_stairs"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_corner_pillar",
        "framedblocks:framed_corner_pillar_from_pillar",
        "framedblocks:framing_saw/framed_corner_pillar"
      ],
      "model_parents": [
        "item/framed_corner_pillar",
        "block/cube"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_corner_pillar"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_corner_pillar",
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
      "id": "framedblocks:framed_corner_slope",
      "namespace": "framedblocks",
      "display_name": "Framed Corner Slope",
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
        "framedblocks:framed_double_corner",
        "framedblocks:framed_inner_sloped_prism",
        "framedblocks:framed_sloped_prism"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_corner_slope",
        "framedblocks:framing_saw/framed_corner_slope"
      ],
      "model_parents": [
        "item/framed_corner_slope",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_corner_slope"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_corner_slope",
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
      "id": "framedblocks:framed_corner_strip",
      "namespace": "framedblocks",
      "display_name": "Framed Corner Strip",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 2,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "framedblocks:framed_corner_strip_from_framed_floor_board",
        "framedblocks:framed_corner_strip_from_framed_wall_board",
        "framedblocks:framing_saw/framed_corner_strip"
      ],
      "model_parents": [
        "item/framed_corner_strip",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_corner_strip"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_corner_strip",
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
      "id": "framedblocks:framed_cube",
      "namespace": "framedblocks",
      "display_name": "Framed Cube",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 42,
        "crafting_shapeless": 7
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 49,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "framedblocks:framed_bars",
        "framedblocks:framed_blueprint",
        "framedblocks:framed_bookshelf",
        "framedblocks:framed_bouncy_cube",
        "framedblocks:framed_button",
        "framedblocks:framed_chest",
        "framedblocks:framed_chiseled_bookshelf",
        "framedblocks:framed_collapsible_block",
        "framedblocks:framed_collapsible_copycat_block",
        "framedblocks:framed_corner_pillar",
        "framedblocks:framed_door",
        "framedblocks:framed_fancy_activator_rail",
        "framedblocks:framed_fancy_detector_rail",
        "framedblocks:framed_fancy_powered_rail",
        "framedblocks:framed_fancy_rail",
        "framedblocks:framed_fence",
        "framedblocks:framed_fence_gate",
        "framedblocks:framed_flower_pot",
        "framedblocks:framed_glowing_cube",
        "framedblocks:framed_gold_pressure_plate",
        "framedblocks:framed_hammer",
        "framedblocks:framed_hanging_sign",
        "framedblocks:framed_iron_pressure_plate",
        "framedblocks:framed_item_frame",
        "framedblocks:framed_key",
        "framedblocks:framed_ladder",
        "framedblocks:framed_lattice_block",
        "framedblocks:framed_lever",
        "framedblocks:framed_mini_cube",
        "framedblocks:framed_one_way_window",
        "framedblocks:framed_pane",
        "framedblocks:framed_panel",
        "framedblocks:framed_pressure_plate",
        "framedblocks:framed_redstone_block",
        "framedblocks:framed_redstone_torch",
        "framedblocks:framed_screwdriver",
        "framedblocks:framed_secret_storage",
        "framedblocks:framed_sign",
        "framedblocks:framed_slab",
        "framedblocks:framed_slope",
        "framedblocks:framed_stairs",
        "framedblocks:framed_stone_pressure_plate",
        "framedblocks:framed_target",
        "framedblocks:framed_thick_lattice",
        "framedblocks:framed_tube",
        "framedblocks:framed_vertical_stairs",
        "framedblocks:framed_wall",
        "framedblocks:framed_wrench",
        "framedblocks:framing_saw"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_cube",
        "framedblocks:framing_saw/framed_cube"
      ],
      "model_parents": [
        "item/framed_cube",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_cube"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_cube",
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
      "id": "framedblocks:framed_detector_rail_slope",
      "namespace": "framedblocks",
      "display_name": "Framed Detector Rail Slope",
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
        "framedblocks:framed_detector_rail_slope",
        "framedblocks:framing_saw/framed_detector_rail_slope"
      ],
      "model_parents": [
        "item/framed_detector_rail_slope",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_detector_rail_slope"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_detector_rail_slope",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:prevent_mob_spawning_inside",
          "minecraft:rails"
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
      "id": "framedblocks:framed_divided_panel_horizontal",
      "namespace": "framedblocks",
      "display_name": "Framed Divided Panel (Horizontal)",
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
        "framedblocks:framed_divided_panel_horizontal_rotate_to_framed_divided_slab"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_divided_panel_horizontal",
        "framedblocks:framed_divided_slab_rotate_to_framed_divided_panel_horizontal",
        "framedblocks:framing_saw/framed_divided_panel_horizontal"
      ],
      "model_parents": [
        "item/framed_divided_panel_horizontal",
        "block/cube"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_divided_panel_horizontal"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_divided_panel_horizontal",
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
      "id": "framedblocks:framed_divided_panel_vertical",
      "namespace": "framedblocks",
      "display_name": "Framed Divided Panel (Vertical)",
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
        "framedblocks:framed_divided_panel_vertical",
        "framedblocks:framing_saw/framed_divided_panel_vertical"
      ],
      "model_parents": [
        "item/framed_divided_panel_vertical",
        "block/cube"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_divided_panel_vertical"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_divided_panel_vertical",
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
      "id": "framedblocks:framed_divided_slab",
      "namespace": "framedblocks",
      "display_name": "Framed Divided Slab",
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
        "framedblocks:framed_divided_slab_rotate_to_framed_divided_panel_horizontal"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_divided_panel_horizontal_rotate_to_framed_divided_slab",
        "framedblocks:framed_divided_slab",
        "framedblocks:framing_saw/framed_divided_slab"
      ],
      "model_parents": [
        "item/framed_divided_slab",
        "block/cube"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_divided_slab"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_divided_slab",
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
      "id": "framedblocks:framed_divided_slope",
      "namespace": "framedblocks",
      "display_name": "Framed Divided Slope",
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
        "framedblocks:framed_divided_slope",
        "framedblocks:framing_saw/framed_divided_slope"
      ],
      "model_parents": [
        "item/framed_divided_slope",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_divided_slope"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_divided_slope",
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
      "id": "framedblocks:framed_divided_stairs",
      "namespace": "framedblocks",
      "display_name": "Framed Divided Stairs",
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
        "framedblocks:framed_divided_stairs",
        "framedblocks:framing_saw/framed_divided_stairs"
      ],
      "model_parents": [
        "item/framed_divided_stairs",
        "block/cube"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_divided_stairs"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_divided_stairs",
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
      "id": "framedblocks:framed_door",
      "namespace": "framedblocks",
      "display_name": "Framed Door",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:doors"
      ],
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
        "framedblocks:framed_gate",
        "framedblocks:framed_iron_door"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_door",
        "framedblocks:framing_saw/framed_door"
      ],
      "model_parents": [
        "item/framed_door",
        "item/generated"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_door"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_door",
        "block_tags": [
          "buildinggadgets2:deny",
          "create:brittle",
          "cucumber:mineable/paxel",
          "minecraft:doors",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "FramedBlocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 4,
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
          "value": "door",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:doors"
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
      "id": "framedblocks:framed_double_corner",
      "namespace": "framedblocks",
      "display_name": "Framed Double Corner",
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
        "framedblocks:framed_double_corner",
        "framedblocks:framing_saw/framed_double_corner"
      ],
      "model_parents": [
        "item/framed_double_corner",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_double_corner"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_double_corner",
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
      "id": "framedblocks:framed_double_half_slope",
      "namespace": "framedblocks",
      "display_name": "Framed Double Half Slope",
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
        "framedblocks:framed_double_half_slope",
        "framedblocks:framing_saw/framed_double_half_slope"
      ],
      "model_parents": [
        "item/framed_double_half_slope",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 2,
      "loot_source_examples": [
        "framedblocks:blocks/framed_double_half_slope",
        "framedblocks:blocks/framed_vertical_double_half_slope"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_double_half_slope",
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
        }
      }
    },
    {
      "id": "framedblocks:framed_double_half_stairs",
      "namespace": "framedblocks",
      "display_name": "Framed Double Half Stairs",
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
        "framedblocks:framed_double_half_stairs",
        "framedblocks:framing_saw/framed_double_half_stairs"
      ],
      "model_parents": [
        "item/framed_double_half_stairs",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_double_half_stairs"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_double_half_stairs",
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
      "id": "framedblocks:framed_double_panel",
      "namespace": "framedblocks",
      "display_name": "Framed Double Panel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [
        "item/framed_double_panel",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "framedblocks:framed_double_panel",
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
      "id": "framedblocks:framed_double_prism",
      "namespace": "framedblocks",
      "display_name": "Framed Double Prism",
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
        "framedblocks:framed_double_prism",
        "framedblocks:framing_saw/framed_double_prism"
      ],
      "model_parents": [
        "item/framed_double_prism",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_double_prism"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_double_prism",
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
      "id": "framedblocks:framed_double_prism_corner",
      "namespace": "framedblocks",
      "display_name": "Framed Double Prism Corner",
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
        "framedblocks:framed_double_prism_corner",
        "framedblocks:framing_saw/framed_double_prism_corner"
      ],
      "model_parents": [
        "item/framed_double_prism_corner",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_double_prism_corner"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_double_prism_corner",
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
      "id": "framedblocks:framed_double_slab",
      "namespace": "framedblocks",
      "display_name": "Framed Double Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [
        "item/framed_double_slab",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "framedblocks:framed_double_slab",
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
        }
      }
    },
    {
      "id": "framedblocks:framed_double_slope",
      "namespace": "framedblocks",
      "display_name": "Framed Double Slope",
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
        "framedblocks:framed_double_slope",
        "framedblocks:framing_saw/framed_double_slope"
      ],
      "model_parents": [
        "item/framed_double_slope",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_double_slope"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_double_slope",
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
      "id": "framedblocks:framed_double_slope_panel",
      "namespace": "framedblocks",
      "display_name": "Framed Double Slope Panel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 2,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "framedblocks:framed_double_slope_panel_rotate_to_framed_double_slope_slab",
        "framedblocks:framed_inv_double_slope_panel"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_double_slope_panel",
        "framedblocks:framed_double_slope_panel_from_inverse_double_slope_panel",
        "framedblocks:framed_double_slope_slab_rotate_to_framed_double_slope_panel",
        "framedblocks:framing_saw/framed_double_slope_panel"
      ],
      "model_parents": [
        "item/framed_double_slope_panel",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_double_slope_panel"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_double_slope_panel",
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
      "id": "framedblocks:framed_double_slope_slab",
      "namespace": "framedblocks",
      "display_name": "Framed Double Slope Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 2,
        "framedblocks:frame": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "framedblocks:framed_double_slope_slab_rotate_to_framed_double_slope_panel",
        "framedblocks:framed_inv_double_slope_slab"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_double_slope_panel_rotate_to_framed_double_slope_slab",
        "framedblocks:framed_double_slope_slab",
        "framedblocks:framed_double_slope_slab_from_inverse",
        "framedblocks:framing_saw/framed_double_slope_slab"
      ],
      "model_parents": [
        "item/framed_double_slope_slab",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_double_slope_slab"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_double_slope_slab",
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
      "id": "framedblocks:framed_double_sloped_prism",
      "namespace": "framedblocks",
      "display_name": "Framed Double Sloped Prism",
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
        "framedblocks:framed_double_sloped_prism",
        "framedblocks:framing_saw/framed_double_sloped_prism"
      ],
      "model_parents": [
        "item/framed_double_sloped_prism",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_double_sloped_prism"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_double_sloped_prism",
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
      "id": "framedblocks:framed_double_stairs",
      "namespace": "framedblocks",
      "display_name": "Framed Double Stairs",
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
        "framedblocks:framed_double_stairs_rotate_to_framed_vertical_double_stairs"
      ],
      "recipe_output_examples": [
        "framedblocks:framed_double_stairs",
        "framedblocks:framed_vertical_double_stairs_rotate_to_framed_double_stairs",
        "framedblocks:framing_saw/framed_double_stairs"
      ],
      "model_parents": [
        "item/framed_double_stairs",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_double_stairs"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_double_stairs",
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
      "id": "framedblocks:framed_double_threeway_corner",
      "namespace": "framedblocks",
      "display_name": "Framed Double Threeway Corner",
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
        "framedblocks:framed_double_threeway_corner",
        "framedblocks:framing_saw/framed_double_threeway_corner"
      ],
      "model_parents": [
        "item/framed_double_threeway_corner",
        "block/framed_cube",
        "block/cube_all"
      ],
      "creative_tabs": [
        "framedblocks:framed_blocks"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "framedblocks:blocks/framed_double_threeway_corner"
      ],
      "block_context": {
        "block_id": "framedblocks:framed_double_threeway_corner",
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