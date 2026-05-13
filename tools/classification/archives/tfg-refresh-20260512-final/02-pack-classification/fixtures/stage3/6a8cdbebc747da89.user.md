# Items to classify
{
  "items": [
    {
      "id": "railways:vermilion_plated_locometal",
      "namespace": "railways",
      "display_name": "Plated Vermilion Locometal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/vermilion/base",
        "railways:palettes/dye_groups/plated",
        "tfg:locometal_blocks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 13,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "minecraft:kjs/railways_vermilion_flat_riveted_locometal",
        "minecraft:kjs/railways_vermilion_flat_slashed_locometal",
        "minecraft:kjs/railways_vermilion_hazard_stripes_chevron_on_black",
        "minecraft:kjs/railways_vermilion_hazard_stripes_chevron_on_white",
        "minecraft:kjs/railways_vermilion_hazard_stripes_diagonal_on_black",
        "minecraft:kjs/railways_vermilion_hazard_stripes_diagonal_on_white",
        "minecraft:kjs/railways_vermilion_locometal_pillar",
        "minecraft:kjs/railways_vermilion_locometal_smokebox",
        "minecraft:kjs/railways_vermilion_locometal_vent",
        "minecraft:kjs/railways_vermilion_plated_locometal",
        "minecraft:kjs/railways_vermilion_riveted_locometal",
        "minecraft:kjs/railways_vermilion_slashed_locometal",
        "tfg:shapeless/vermilion_locometal_boiler"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/railways_vermilion_plated_locometal"
      ],
      "model_parents": [
        "item/vermilion_plated_locometal",
        "block/palettes/vermilion/plated_locometal",
        "block/cube_all"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/vermilion_plated_locometal"
      ],
      "block_context": {
        "block_id": "railways:vermilion_plated_locometal",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/plated"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
      "id": "railways:vermilion_riveted_locometal",
      "namespace": "railways",
      "display_name": "Vermilion Riveted Locometal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/vermilion/base",
        "railways:palettes/dye_groups/riveted",
        "tfg:locometal_blocks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 13,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "minecraft:kjs/railways_vermilion_flat_riveted_locometal",
        "minecraft:kjs/railways_vermilion_flat_slashed_locometal",
        "minecraft:kjs/railways_vermilion_hazard_stripes_chevron_on_black",
        "minecraft:kjs/railways_vermilion_hazard_stripes_chevron_on_white",
        "minecraft:kjs/railways_vermilion_hazard_stripes_diagonal_on_black",
        "minecraft:kjs/railways_vermilion_hazard_stripes_diagonal_on_white",
        "minecraft:kjs/railways_vermilion_locometal_pillar",
        "minecraft:kjs/railways_vermilion_locometal_smokebox",
        "minecraft:kjs/railways_vermilion_locometal_vent",
        "minecraft:kjs/railways_vermilion_plated_locometal",
        "minecraft:kjs/railways_vermilion_riveted_locometal",
        "minecraft:kjs/railways_vermilion_slashed_locometal",
        "tfg:shapeless/vermilion_locometal_boiler"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/railways_vermilion_riveted_locometal"
      ],
      "model_parents": [
        "item/vermilion_riveted_locometal",
        "block/palettes/vermilion/riveted_locometal",
        "block/cube_all"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/vermilion_riveted_locometal"
      ],
      "block_context": {
        "block_id": "railways:vermilion_riveted_locometal",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/riveted"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
      "id": "railways:vermilion_round_pane_locometal_window",
      "namespace": "railways",
      "display_name": "Vermilion Round Pane Locometal Window",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/vermilion/windows",
        "railways:palettes/dye_groups/round_pane_window",
        "tfg:locometal_blocks"
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
        "item/vermilion_round_pane_locometal_window",
        "block/palettes/vermilion/round_pane_locometal_window",
        "block/cube_column"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/vermilion_round_pane_locometal_window"
      ],
      "block_context": {
        "block_id": "railways:vermilion_round_pane_locometal_window",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:impermeable",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/round_pane_window"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
          "value": "pane",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _window"
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
      "id": "railways:vermilion_single_pane_locometal_window",
      "namespace": "railways",
      "display_name": "Vermilion Single Pane Locometal Window",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/vermilion/windows",
        "railways:palettes/dye_groups/single_pane_window",
        "tfg:locometal_blocks"
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
        "item/vermilion_single_pane_locometal_window",
        "block/palettes/vermilion/single_pane_locometal_window",
        "block/cube_column"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/vermilion_single_pane_locometal_window"
      ],
      "block_context": {
        "block_id": "railways:vermilion_single_pane_locometal_window",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:impermeable",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/single_pane_window"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
          "value": "pane",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _window"
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
      "id": "railways:vermilion_slashed_locometal",
      "namespace": "railways",
      "display_name": "Vermilion Slashed Locometal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/vermilion/base",
        "railways:palettes/dye_groups/slashed",
        "tfg:locometal_blocks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:item_application",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "create:item_application": 3,
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 16,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "minecraft:kjs/railways_vermilion_flat_riveted_locometal",
        "minecraft:kjs/railways_vermilion_flat_slashed_locometal",
        "minecraft:kjs/railways_vermilion_hazard_stripes_chevron_on_black",
        "minecraft:kjs/railways_vermilion_hazard_stripes_chevron_on_white",
        "minecraft:kjs/railways_vermilion_hazard_stripes_diagonal_on_black",
        "minecraft:kjs/railways_vermilion_hazard_stripes_diagonal_on_white",
        "minecraft:kjs/railways_vermilion_locometal_pillar",
        "minecraft:kjs/railways_vermilion_locometal_smokebox",
        "minecraft:kjs/railways_vermilion_locometal_vent",
        "minecraft:kjs/railways_vermilion_plated_locometal",
        "minecraft:kjs/railways_vermilion_riveted_locometal",
        "minecraft:kjs/railways_vermilion_slashed_locometal",
        "tfg:railways/item_application/vermilion_brass_wrapped_locometal",
        "tfg:railways/item_application/vermilion_copper_wrapped_locometal",
        "tfg:railways/item_application/vermilion_iron_wrapped_locometal",
        "tfg:shapeless/vermilion_locometal_boiler"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/railways_vermilion_slashed_locometal"
      ],
      "model_parents": [
        "item/vermilion_slashed_locometal",
        "block/palettes/vermilion/slashed_locometal",
        "block/cube_all"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/vermilion_slashed_locometal"
      ],
      "block_context": {
        "block_id": "railways:vermilion_slashed_locometal",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/slashed"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
            "create:item_application",
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
      "id": "railways:vermilion_sliding_locometal_door",
      "namespace": "railways",
      "display_name": "Vermilion Sliding Locometal Door",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:contraption_controlled",
        "minecraft:doors",
        "railways:palettes/cycle_groups/vermilion/doors",
        "railways:palettes/dye_groups/sliding_door"
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
        "item/vermilion_sliding_locometal_door",
        "item/generated"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/vermilion_sliding_locometal_door"
      ],
      "block_context": {
        "block_id": "railways:vermilion_sliding_locometal_door",
        "block_tags": [
          "buildinggadgets2:deny",
          "create:brittle",
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:doors",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:wooden_doors",
          "quark:non_double_door",
          "railways:locometal",
          "railways:palettes/dye_groups/sliding_door",
          "tacz:interact_key/whitelist"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 4,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
        }
      }
    },
    {
      "id": "railways:vermilion_two_pane_locometal_window",
      "namespace": "railways",
      "display_name": "Vermilion Two Pane Locometal Window",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/vermilion/windows",
        "railways:palettes/dye_groups/two_pane_window",
        "tfg:locometal_blocks"
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
        "item/vermilion_two_pane_locometal_window",
        "block/palettes/vermilion/two_pane_locometal_window",
        "block/cube_column"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/vermilion_two_pane_locometal_window"
      ],
      "block_context": {
        "block_id": "railways:vermilion_two_pane_locometal_window",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:impermeable",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/two_pane_window"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
          "value": "pane",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _window"
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
      "id": "railways:vermilion_wrapped_locometal_smokebox",
      "namespace": "railways",
      "display_name": "Vermilion Brass Wrapped Locometal Smokebox",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/vermilion/wrapped_brass",
        "railways:palettes/dye_groups/brass_wrapped_smokebox",
        "tfg:locometal_blocks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "create:item_application": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:railways/item_application/vermilion_wrapped_locometal_smokebox"
      ],
      "model_parents": [
        "item/vermilion_wrapped_locometal_smokebox",
        "block/palettes/vermilion/wrapped_locometal_smokebox",
        "block/palettes/smokebox/smokebox",
        "block/block"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/vermilion_wrapped_locometal_smokebox"
      ],
      "block_context": {
        "block_id": "railways:vermilion_wrapped_locometal_smokebox",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/brass_wrapped_smokebox"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
      "id": "railways:white_brass_wrapped_locometal",
      "namespace": "railways",
      "display_name": "White Brass Wrapped Locometal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/white/wrapped_brass",
        "railways:palettes/dye_groups/brass_wrapped_slashed",
        "tfg:locometal_blocks",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {
        "create:item_application": 1
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "tfg:railways/item_application/white_brass_wrapped_locometal"
      ],
      "model_parents": [
        "item/white_brass_wrapped_locometal",
        "block/palettes/white/brass_wrapped_locometal",
        "block/cube_all"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/white_brass_wrapped_locometal"
      ],
      "block_context": {
        "block_id": "railways:white_brass_wrapped_locometal",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/brass_wrapped_slashed"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
            "kubejs:shapeless"
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
      "id": "railways:white_brass_wrapped_locometal_boiler",
      "namespace": "railways",
      "display_name": "White Brass Wrapped Locometal Boiler",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/dye_groups/brass_wrapped_boiler",
        "tfg:locometal_blocks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "create:item_application": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:railways/item_application/white_brass_wrapped_locometal_boiler"
      ],
      "model_parents": [
        "item/white_brass_wrapped_locometal_boiler",
        "block/palettes/white/brass_wrapped_locometal_boiler_flat_x",
        "block/palettes/boiler/boiler",
        "block/large_wheels"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/white_brass_wrapped_locometal_boiler"
      ],
      "block_context": {
        "block_id": "railways:white_brass_wrapped_locometal_boiler",
        "block_tags": [
          "create:copycat_deny",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:locometal_boilers",
          "railways:palettes/dye_groups/brass_wrapped_boiler"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
      "id": "railways:white_conductor_cap",
      "namespace": "railways",
      "display_name": "White Conductor's Cap",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:conductor_caps"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:barrel_sealed": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "railways:barrel/cap_decolor"
      ],
      "model_parents": [
        "item/white_conductor_cap",
        "item/conductor_cap"
      ],
      "creative_tabs": [
        "railways:main"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "When on Head:"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:equippable": {
          "slot": "head"
        },
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "equip_slot": {
          "value": "head",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        }
      }
    },
    {
      "id": "railways:white_copper_wrapped_locometal",
      "namespace": "railways",
      "display_name": "White Copper Wrapped Locometal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/white/wrapped_copper",
        "railways:palettes/dye_groups/copper_wrapped_slashed",
        "tfg:locometal_blocks",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {
        "create:item_application": 1
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "tfg:railways/item_application/white_copper_wrapped_locometal"
      ],
      "model_parents": [
        "item/white_copper_wrapped_locometal",
        "block/palettes/white/copper_wrapped_locometal",
        "block/cube_all"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/white_copper_wrapped_locometal"
      ],
      "block_context": {
        "block_id": "railways:white_copper_wrapped_locometal",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/copper_wrapped_slashed"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
            "kubejs:shapeless"
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
      "id": "railways:white_copper_wrapped_locometal_boiler",
      "namespace": "railways",
      "display_name": "White Copper Wrapped Locometal Boiler",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/dye_groups/copper_wrapped_boiler",
        "tfg:locometal_blocks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "create:item_application": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:railways/item_application/white_copper_wrapped_locometal_boiler"
      ],
      "model_parents": [
        "item/white_copper_wrapped_locometal_boiler",
        "block/palettes/white/copper_wrapped_locometal_boiler_flat_x",
        "block/palettes/boiler/boiler",
        "block/large_wheels"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/white_copper_wrapped_locometal_boiler"
      ],
      "block_context": {
        "block_id": "railways:white_copper_wrapped_locometal_boiler",
        "block_tags": [
          "create:copycat_deny",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:locometal_boilers",
          "railways:palettes/dye_groups/copper_wrapped_boiler"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
      "id": "railways:white_copper_wrapped_locometal_smokebox",
      "namespace": "railways",
      "display_name": "White Copper Wrapped Locometal Smokebox",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/white/wrapped_copper",
        "railways:palettes/dye_groups/copper_wrapped_smokebox",
        "tfg:locometal_blocks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "create:item_application": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:railways/item_application/white_copper_wrapped_locometal_smokebox"
      ],
      "model_parents": [
        "item/white_copper_wrapped_locometal_smokebox",
        "block/palettes/white/copper_wrapped_locometal_smokebox",
        "block/palettes/smokebox/smokebox",
        "block/block"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/white_copper_wrapped_locometal_smokebox"
      ],
      "block_context": {
        "block_id": "railways:white_copper_wrapped_locometal_smokebox",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/copper_wrapped_smokebox"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
      "id": "railways:white_flat_riveted_locometal",
      "namespace": "railways",
      "display_name": "Flat White Riveted Locometal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/white/base",
        "railways:palettes/dye_groups/flat_riveted",
        "tfg:locometal_blocks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 13,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "minecraft:kjs/railways_white_flat_riveted_locometal",
        "minecraft:kjs/railways_white_flat_slashed_locometal",
        "minecraft:kjs/railways_white_hazard_stripes_chevron_on_black",
        "minecraft:kjs/railways_white_hazard_stripes_chevron_on_white",
        "minecraft:kjs/railways_white_hazard_stripes_diagonal_on_black",
        "minecraft:kjs/railways_white_hazard_stripes_diagonal_on_white",
        "minecraft:kjs/railways_white_locometal_pillar",
        "minecraft:kjs/railways_white_locometal_smokebox",
        "minecraft:kjs/railways_white_locometal_vent",
        "minecraft:kjs/railways_white_plated_locometal",
        "minecraft:kjs/railways_white_riveted_locometal",
        "minecraft:kjs/railways_white_slashed_locometal",
        "tfg:shapeless/white_locometal_boiler"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/railways_white_flat_riveted_locometal"
      ],
      "model_parents": [
        "item/white_flat_riveted_locometal",
        "block/palettes/white/flat_riveted_locometal",
        "block/cube_all"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/white_flat_riveted_locometal"
      ],
      "block_context": {
        "block_id": "railways:white_flat_riveted_locometal",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/flat_riveted"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
      "id": "railways:white_flat_slashed_locometal",
      "namespace": "railways",
      "display_name": "Flat White Slashed Locometal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/white/base",
        "railways:palettes/dye_groups/flat_slashed",
        "tfg:locometal_blocks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 13,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "minecraft:kjs/railways_white_flat_riveted_locometal",
        "minecraft:kjs/railways_white_flat_slashed_locometal",
        "minecraft:kjs/railways_white_hazard_stripes_chevron_on_black",
        "minecraft:kjs/railways_white_hazard_stripes_chevron_on_white",
        "minecraft:kjs/railways_white_hazard_stripes_diagonal_on_black",
        "minecraft:kjs/railways_white_hazard_stripes_diagonal_on_white",
        "minecraft:kjs/railways_white_locometal_pillar",
        "minecraft:kjs/railways_white_locometal_smokebox",
        "minecraft:kjs/railways_white_locometal_vent",
        "minecraft:kjs/railways_white_plated_locometal",
        "minecraft:kjs/railways_white_riveted_locometal",
        "minecraft:kjs/railways_white_slashed_locometal",
        "tfg:shapeless/white_locometal_boiler"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/railways_white_flat_slashed_locometal"
      ],
      "model_parents": [
        "item/white_flat_slashed_locometal",
        "block/palettes/white/flat_slashed_locometal",
        "block/cube_all"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/white_flat_slashed_locometal"
      ],
      "block_context": {
        "block_id": "railways:white_flat_slashed_locometal",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/flat_slashed"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
      "id": "railways:white_folding_locometal_door",
      "namespace": "railways",
      "display_name": "White Folding Locometal Door",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:contraption_controlled",
        "minecraft:doors",
        "railways:palettes/cycle_groups/white/doors",
        "railways:palettes/dye_groups/folding_door"
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
        "item/white_folding_locometal_door",
        "item/generated"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/white_folding_locometal_door"
      ],
      "block_context": {
        "block_id": "railways:white_folding_locometal_door",
        "block_tags": [
          "buildinggadgets2:deny",
          "create:brittle",
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:doors",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:wooden_doors",
          "quark:non_double_door",
          "railways:locometal",
          "railways:palettes/dye_groups/folding_door",
          "tacz:interact_key/whitelist"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 4,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
        }
      }
    },
    {
      "id": "railways:white_four_pane_locometal_window",
      "namespace": "railways",
      "display_name": "White Four Pane Locometal Window",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/white/windows",
        "railways:palettes/dye_groups/four_pane_window",
        "tfg:locometal_blocks"
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
        "item/white_four_pane_locometal_window",
        "block/palettes/white/four_pane_locometal_window",
        "block/cube_column"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/white_four_pane_locometal_window"
      ],
      "block_context": {
        "block_id": "railways:white_four_pane_locometal_window",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:impermeable",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/four_pane_window"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
          "value": "pane",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _window"
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
      "id": "railways:white_hazard_stripes_chevron_on_black",
      "namespace": "railways",
      "display_name": "White on Black Chevron",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/white/base",
        "railways:palettes/dye_groups/hazard_stripes_chevron_black",
        "tfg:locometal_blocks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 13,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "minecraft:kjs/railways_white_flat_riveted_locometal",
        "minecraft:kjs/railways_white_flat_slashed_locometal",
        "minecraft:kjs/railways_white_hazard_stripes_chevron_on_black",
        "minecraft:kjs/railways_white_hazard_stripes_chevron_on_white",
        "minecraft:kjs/railways_white_hazard_stripes_diagonal_on_black",
        "minecraft:kjs/railways_white_hazard_stripes_diagonal_on_white",
        "minecraft:kjs/railways_white_locometal_pillar",
        "minecraft:kjs/railways_white_locometal_smokebox",
        "minecraft:kjs/railways_white_locometal_vent",
        "minecraft:kjs/railways_white_plated_locometal",
        "minecraft:kjs/railways_white_riveted_locometal",
        "minecraft:kjs/railways_white_slashed_locometal",
        "tfg:shapeless/white_locometal_boiler"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/railways_white_hazard_stripes_chevron_on_black"
      ],
      "model_parents": [
        "item/white_hazard_stripes_chevron_on_black",
        "block/palettes/white/hazard_stripes_chevron_on_black",
        "block/palettes/hazard_stripes/chevron",
        "block/block"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/white_hazard_stripes_chevron_on_black"
      ],
      "block_context": {
        "block_id": "railways:white_hazard_stripes_chevron_on_black",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/hazard_stripes_chevron_black"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
      "id": "railways:white_hazard_stripes_chevron_on_white",
      "namespace": "railways",
      "display_name": "White on White Chevron",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/white/base",
        "railways:palettes/dye_groups/hazard_stripes_chevron_white",
        "tfg:locometal_blocks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 13,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "minecraft:kjs/railways_white_flat_riveted_locometal",
        "minecraft:kjs/railways_white_flat_slashed_locometal",
        "minecraft:kjs/railways_white_hazard_stripes_chevron_on_black",
        "minecraft:kjs/railways_white_hazard_stripes_chevron_on_white",
        "minecraft:kjs/railways_white_hazard_stripes_diagonal_on_black",
        "minecraft:kjs/railways_white_hazard_stripes_diagonal_on_white",
        "minecraft:kjs/railways_white_locometal_pillar",
        "minecraft:kjs/railways_white_locometal_smokebox",
        "minecraft:kjs/railways_white_locometal_vent",
        "minecraft:kjs/railways_white_plated_locometal",
        "minecraft:kjs/railways_white_riveted_locometal",
        "minecraft:kjs/railways_white_slashed_locometal",
        "tfg:shapeless/white_locometal_boiler"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/railways_white_hazard_stripes_chevron_on_white"
      ],
      "model_parents": [
        "item/white_hazard_stripes_chevron_on_white",
        "block/palettes/white/hazard_stripes_chevron_on_white",
        "block/palettes/hazard_stripes/chevron",
        "block/block"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/white_hazard_stripes_chevron_on_white"
      ],
      "block_context": {
        "block_id": "railways:white_hazard_stripes_chevron_on_white",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/hazard_stripes_chevron_white"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
      "id": "railways:white_hazard_stripes_diagonal_on_black",
      "namespace": "railways",
      "display_name": "White on Black Hazard Stripes",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/white/base",
        "railways:palettes/dye_groups/hazard_stripes_diagonal_black",
        "tfg:locometal_blocks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 13,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "minecraft:kjs/railways_white_flat_riveted_locometal",
        "minecraft:kjs/railways_white_flat_slashed_locometal",
        "minecraft:kjs/railways_white_hazard_stripes_chevron_on_black",
        "minecraft:kjs/railways_white_hazard_stripes_chevron_on_white",
        "minecraft:kjs/railways_white_hazard_stripes_diagonal_on_black",
        "minecraft:kjs/railways_white_hazard_stripes_diagonal_on_white",
        "minecraft:kjs/railways_white_locometal_pillar",
        "minecraft:kjs/railways_white_locometal_smokebox",
        "minecraft:kjs/railways_white_locometal_vent",
        "minecraft:kjs/railways_white_plated_locometal",
        "minecraft:kjs/railways_white_riveted_locometal",
        "minecraft:kjs/railways_white_slashed_locometal",
        "tfg:shapeless/white_locometal_boiler"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/railways_white_hazard_stripes_diagonal_on_black"
      ],
      "model_parents": [
        "item/white_hazard_stripes_diagonal_on_black",
        "block/palettes/white/hazard_stripes_diagonal_on_black",
        "block/palettes/hazard_stripes/diagonal",
        "block/block"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/white_hazard_stripes_diagonal_on_black"
      ],
      "block_context": {
        "block_id": "railways:white_hazard_stripes_diagonal_on_black",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/hazard_stripes_diagonal_black"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
      "id": "railways:white_hazard_stripes_diagonal_on_white",
      "namespace": "railways",
      "display_name": "White on White Hazard Stripes",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/white/base",
        "railways:palettes/dye_groups/hazard_stripes_diagonal_white",
        "tfg:locometal_blocks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 13,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "minecraft:kjs/railways_white_flat_riveted_locometal",
        "minecraft:kjs/railways_white_flat_slashed_locometal",
        "minecraft:kjs/railways_white_hazard_stripes_chevron_on_black",
        "minecraft:kjs/railways_white_hazard_stripes_chevron_on_white",
        "minecraft:kjs/railways_white_hazard_stripes_diagonal_on_black",
        "minecraft:kjs/railways_white_hazard_stripes_diagonal_on_white",
        "minecraft:kjs/railways_white_locometal_pillar",
        "minecraft:kjs/railways_white_locometal_smokebox",
        "minecraft:kjs/railways_white_locometal_vent",
        "minecraft:kjs/railways_white_plated_locometal",
        "minecraft:kjs/railways_white_riveted_locometal",
        "minecraft:kjs/railways_white_slashed_locometal",
        "tfg:shapeless/white_locometal_boiler"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/railways_white_hazard_stripes_diagonal_on_white"
      ],
      "model_parents": [
        "item/white_hazard_stripes_diagonal_on_white",
        "block/palettes/white/hazard_stripes_diagonal_on_white",
        "block/palettes/hazard_stripes/diagonal",
        "block/block"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/white_hazard_stripes_diagonal_on_white"
      ],
      "block_context": {
        "block_id": "railways:white_hazard_stripes_diagonal_on_white",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/hazard_stripes_diagonal_white"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
      "id": "railways:white_hinged_locometal_door",
      "namespace": "railways",
      "display_name": "White Hinged Locometal Door",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:doors",
        "railways:palettes/cycle_groups/white/doors",
        "railways:palettes/dye_groups/hinged_door"
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
        "item/white_hinged_locometal_door",
        "item/generated"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/white_hinged_locometal_door"
      ],
      "block_context": {
        "block_id": "railways:white_hinged_locometal_door",
        "block_tags": [
          "buildinggadgets2:deny",
          "create:brittle",
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:doors",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:wooden_doors",
          "railways:locometal",
          "railways:palettes/dye_groups/hinged_door",
          "tacz:interact_key/whitelist"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 4,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
        }
      }
    },
    {
      "id": "railways:white_incomplete_conductor_cap",
      "namespace": "railways",
      "display_name": "Incomplete White Conductor's Cap",
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
        "item/white_incomplete_conductor_cap",
        "item/incomplete_conductor_cap"
      ],
      "creative_tabs": [],
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
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "railways:white_iron_wrapped_locometal",
      "namespace": "railways",
      "display_name": "White Iron Wrapped Locometal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/white/wrapped_iron",
        "railways:palettes/dye_groups/iron_wrapped_slashed",
        "tfg:locometal_blocks",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {
        "create:item_application": 1
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "tfg:railways/item_application/white_iron_wrapped_locometal"
      ],
      "model_parents": [
        "item/white_iron_wrapped_locometal",
        "block/palettes/white/iron_wrapped_locometal",
        "block/cube_all"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/white_iron_wrapped_locometal"
      ],
      "block_context": {
        "block_id": "railways:white_iron_wrapped_locometal",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/iron_wrapped_slashed"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Steam 'n' Rails"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "railways",
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
            "kubejs:shapeless"
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