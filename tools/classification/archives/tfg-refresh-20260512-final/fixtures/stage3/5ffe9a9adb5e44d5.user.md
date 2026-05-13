# Items to classify
{
  "items": [
    {
      "id": "railways:yellow_locometal_rung_ladder",
      "namespace": "railways",
      "display_name": "Yellow Locometal Rung Ladder",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/yellow/ladders",
        "railways:palettes/dye_groups/rung_ladder",
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
        "item/yellow_locometal_rung_ladder",
        "item/generated"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/yellow_locometal_rung_ladder"
      ],
      "block_context": {
        "block_id": "railways:yellow_locometal_rung_ladder",
        "block_tags": [
          "create:copycat_deny",
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
          "minecraft:climbable",
          "minecraft:fall_damage_resetting",
          "minecraft:mineable/pickaxe",
          "railways:locometal",
          "railways:palettes/dye_groups/rung_ladder"
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
          "value": "ladder",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ladder"
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
      "id": "railways:yellow_locometal_smokebox",
      "namespace": "railways",
      "display_name": "Yellow Locometal Smokebox",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/yellow/base",
        "railways:palettes/dye_groups/smokebox",
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
        "minecraft:kjs/railways_yellow_flat_riveted_locometal",
        "minecraft:kjs/railways_yellow_flat_slashed_locometal",
        "minecraft:kjs/railways_yellow_hazard_stripes_chevron_on_black",
        "minecraft:kjs/railways_yellow_hazard_stripes_chevron_on_white",
        "minecraft:kjs/railways_yellow_hazard_stripes_diagonal_on_black",
        "minecraft:kjs/railways_yellow_hazard_stripes_diagonal_on_white",
        "minecraft:kjs/railways_yellow_locometal_pillar",
        "minecraft:kjs/railways_yellow_locometal_smokebox",
        "minecraft:kjs/railways_yellow_locometal_vent",
        "minecraft:kjs/railways_yellow_plated_locometal",
        "minecraft:kjs/railways_yellow_riveted_locometal",
        "minecraft:kjs/railways_yellow_slashed_locometal",
        "tfg:railways/item_application/yellow_copper_wrapped_locometal_smokebox",
        "tfg:railways/item_application/yellow_iron_wrapped_locometal_smokebox",
        "tfg:railways/item_application/yellow_wrapped_locometal_smokebox",
        "tfg:shapeless/yellow_locometal_boiler"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/railways_yellow_locometal_smokebox"
      ],
      "model_parents": [
        "item/yellow_locometal_smokebox",
        "block/palettes/yellow/locometal_smokebox",
        "block/palettes/smokebox/smokebox",
        "block/block"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/yellow_locometal_smokebox"
      ],
      "block_context": {
        "block_id": "railways:yellow_locometal_smokebox",
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
          "railways:palettes/dye_groups/smokebox"
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
      "id": "railways:yellow_locometal_trapdoor",
      "namespace": "railways",
      "display_name": "Yellow Locometal Trapdoor",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trapdoors",
        "railways:palettes/dye_groups/trapdoor"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "tfg:immersive_aircraft/shaped/sail"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/yellow_locometal_trapdoor",
        "block/palettes/yellow/locometal_trapdoor",
        "block/palettes/yellow/trapdoors/trapdoor_bottom",
        "block/palettes/trapdoors/template_orientable_trapdoor_bottom",
        "block/thin_block"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/yellow_locometal_trapdoor"
      ],
      "block_context": {
        "block_id": "railways:yellow_locometal_trapdoor",
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
          "minecraft:trapdoors",
          "railways:locometal",
          "railways:palettes/dye_groups/trapdoor"
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
          "value": "trapdoor",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:trapdoors"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
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
      "id": "railways:yellow_locometal_vent",
      "namespace": "railways",
      "display_name": "Yellow Locometal Vent",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/yellow/base",
        "railways:palettes/dye_groups/vent",
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
        "minecraft:kjs/railways_yellow_flat_riveted_locometal",
        "minecraft:kjs/railways_yellow_flat_slashed_locometal",
        "minecraft:kjs/railways_yellow_hazard_stripes_chevron_on_black",
        "minecraft:kjs/railways_yellow_hazard_stripes_chevron_on_white",
        "minecraft:kjs/railways_yellow_hazard_stripes_diagonal_on_black",
        "minecraft:kjs/railways_yellow_hazard_stripes_diagonal_on_white",
        "minecraft:kjs/railways_yellow_locometal_pillar",
        "minecraft:kjs/railways_yellow_locometal_smokebox",
        "minecraft:kjs/railways_yellow_locometal_vent",
        "minecraft:kjs/railways_yellow_plated_locometal",
        "minecraft:kjs/railways_yellow_riveted_locometal",
        "minecraft:kjs/railways_yellow_slashed_locometal",
        "tfg:shapeless/yellow_locometal_boiler"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/railways_yellow_locometal_vent"
      ],
      "model_parents": [
        "item/yellow_locometal_vent",
        "block/palettes/yellow/locometal_vent",
        "block/cube_all"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/yellow_locometal_vent"
      ],
      "block_context": {
        "block_id": "railways:yellow_locometal_vent",
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
          "railways:palettes/dye_groups/vent"
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
      "id": "railways:yellow_paint_pitcher",
      "namespace": "railways",
      "display_name": "Yellow Paint Pitcher",
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
        "item/yellow_paint_pitcher",
        "item/generated"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Paint level: 32 / 32"
        },
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
      "id": "railways:yellow_plated_locometal",
      "namespace": "railways",
      "display_name": "Plated Yellow Locometal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/yellow/base",
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
        "minecraft:kjs/railways_yellow_flat_riveted_locometal",
        "minecraft:kjs/railways_yellow_flat_slashed_locometal",
        "minecraft:kjs/railways_yellow_hazard_stripes_chevron_on_black",
        "minecraft:kjs/railways_yellow_hazard_stripes_chevron_on_white",
        "minecraft:kjs/railways_yellow_hazard_stripes_diagonal_on_black",
        "minecraft:kjs/railways_yellow_hazard_stripes_diagonal_on_white",
        "minecraft:kjs/railways_yellow_locometal_pillar",
        "minecraft:kjs/railways_yellow_locometal_smokebox",
        "minecraft:kjs/railways_yellow_locometal_vent",
        "minecraft:kjs/railways_yellow_plated_locometal",
        "minecraft:kjs/railways_yellow_riveted_locometal",
        "minecraft:kjs/railways_yellow_slashed_locometal",
        "tfg:shapeless/yellow_locometal_boiler"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/railways_yellow_plated_locometal"
      ],
      "model_parents": [
        "item/yellow_plated_locometal",
        "block/palettes/yellow/plated_locometal",
        "block/cube_all"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/yellow_plated_locometal"
      ],
      "block_context": {
        "block_id": "railways:yellow_plated_locometal",
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
      "id": "railways:yellow_riveted_locometal",
      "namespace": "railways",
      "display_name": "Yellow Riveted Locometal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/yellow/base",
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
        "minecraft:kjs/railways_yellow_flat_riveted_locometal",
        "minecraft:kjs/railways_yellow_flat_slashed_locometal",
        "minecraft:kjs/railways_yellow_hazard_stripes_chevron_on_black",
        "minecraft:kjs/railways_yellow_hazard_stripes_chevron_on_white",
        "minecraft:kjs/railways_yellow_hazard_stripes_diagonal_on_black",
        "minecraft:kjs/railways_yellow_hazard_stripes_diagonal_on_white",
        "minecraft:kjs/railways_yellow_locometal_pillar",
        "minecraft:kjs/railways_yellow_locometal_smokebox",
        "minecraft:kjs/railways_yellow_locometal_vent",
        "minecraft:kjs/railways_yellow_plated_locometal",
        "minecraft:kjs/railways_yellow_riveted_locometal",
        "minecraft:kjs/railways_yellow_slashed_locometal",
        "tfg:shapeless/yellow_locometal_boiler"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/railways_yellow_riveted_locometal"
      ],
      "model_parents": [
        "item/yellow_riveted_locometal",
        "block/palettes/yellow/riveted_locometal",
        "block/cube_all"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/yellow_riveted_locometal"
      ],
      "block_context": {
        "block_id": "railways:yellow_riveted_locometal",
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
      "id": "railways:yellow_round_pane_locometal_window",
      "namespace": "railways",
      "display_name": "Yellow Round Pane Locometal Window",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/yellow/windows",
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
        "item/yellow_round_pane_locometal_window",
        "block/palettes/yellow/round_pane_locometal_window",
        "block/cube_column"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/yellow_round_pane_locometal_window"
      ],
      "block_context": {
        "block_id": "railways:yellow_round_pane_locometal_window",
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
      "id": "railways:yellow_single_pane_locometal_window",
      "namespace": "railways",
      "display_name": "Yellow Single Pane Locometal Window",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/yellow/windows",
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
        "item/yellow_single_pane_locometal_window",
        "block/palettes/yellow/single_pane_locometal_window",
        "block/cube_column"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/yellow_single_pane_locometal_window"
      ],
      "block_context": {
        "block_id": "railways:yellow_single_pane_locometal_window",
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
      "id": "railways:yellow_slashed_locometal",
      "namespace": "railways",
      "display_name": "Yellow Slashed Locometal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/yellow/base",
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
        "minecraft:kjs/railways_yellow_flat_riveted_locometal",
        "minecraft:kjs/railways_yellow_flat_slashed_locometal",
        "minecraft:kjs/railways_yellow_hazard_stripes_chevron_on_black",
        "minecraft:kjs/railways_yellow_hazard_stripes_chevron_on_white",
        "minecraft:kjs/railways_yellow_hazard_stripes_diagonal_on_black",
        "minecraft:kjs/railways_yellow_hazard_stripes_diagonal_on_white",
        "minecraft:kjs/railways_yellow_locometal_pillar",
        "minecraft:kjs/railways_yellow_locometal_smokebox",
        "minecraft:kjs/railways_yellow_locometal_vent",
        "minecraft:kjs/railways_yellow_plated_locometal",
        "minecraft:kjs/railways_yellow_riveted_locometal",
        "minecraft:kjs/railways_yellow_slashed_locometal",
        "tfg:railways/item_application/yellow_brass_wrapped_locometal",
        "tfg:railways/item_application/yellow_copper_wrapped_locometal",
        "tfg:railways/item_application/yellow_iron_wrapped_locometal",
        "tfg:shapeless/yellow_locometal_boiler"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/railways_yellow_slashed_locometal"
      ],
      "model_parents": [
        "item/yellow_slashed_locometal",
        "block/palettes/yellow/slashed_locometal",
        "block/cube_all"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/yellow_slashed_locometal"
      ],
      "block_context": {
        "block_id": "railways:yellow_slashed_locometal",
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
      "id": "railways:yellow_sliding_locometal_door",
      "namespace": "railways",
      "display_name": "Yellow Sliding Locometal Door",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:contraption_controlled",
        "minecraft:doors",
        "railways:palettes/cycle_groups/yellow/doors",
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
        "item/yellow_sliding_locometal_door",
        "item/generated"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/yellow_sliding_locometal_door"
      ],
      "block_context": {
        "block_id": "railways:yellow_sliding_locometal_door",
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
      "id": "railways:yellow_two_pane_locometal_window",
      "namespace": "railways",
      "display_name": "Yellow Two Pane Locometal Window",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/yellow/windows",
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
        "item/yellow_two_pane_locometal_window",
        "block/palettes/yellow/two_pane_locometal_window",
        "block/cube_column"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/yellow_two_pane_locometal_window"
      ],
      "block_context": {
        "block_id": "railways:yellow_two_pane_locometal_window",
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
      "id": "railways:yellow_wrapped_locometal_smokebox",
      "namespace": "railways",
      "display_name": "Yellow Brass Wrapped Locometal Smokebox",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "railways:palettes/cycle_groups/yellow/wrapped_brass",
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
        "tfg:railways/item_application/yellow_wrapped_locometal_smokebox"
      ],
      "model_parents": [
        "item/yellow_wrapped_locometal_smokebox",
        "block/palettes/yellow/wrapped_locometal_smokebox",
        "block/palettes/smokebox/smokebox",
        "block/block"
      ],
      "creative_tabs": [
        "railways:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "railways:blocks/yellow_wrapped_locometal_smokebox"
      ],
      "block_context": {
        "block_id": "railways:yellow_wrapped_locometal_smokebox",
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
      "id": "rnr:base_course",
      "namespace": "rnr",
      "display_name": "Base Course",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 9,
        "tfc:landslide": 7
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 16,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/base_course",
        "rnr:landslide/trodden_wet_concrete_road",
        "rnr:landslide/wet_concrete_road",
        "rnr:landslide/wet_concrete_road_control_joint",
        "rnr:landslide/wet_concrete_road_flagstones",
        "rnr:landslide/wet_concrete_road_panel",
        "rnr:landslide/wet_concrete_road_sett",
        "tfg:rnr/mattock/recycle_cracked_concrete_road",
        "tfg:rnr/mattock/recycle_cracked_concrete_road_slab",
        "tfg:rnr/mattock/recycle_cracked_concrete_road_stairs",
        "tfg:rnr/mattock/recycle_cracked_trodden_concrete_road",
        "tfg:rnr/mattock/recycle_cracked_trodden_concrete_road_slab",
        "tfg:rnr/mattock/recycle_cracked_trodden_concrete_road_stairs",
        "tfg:rnr/mattock/recycle_trodden_concrete_road",
        "tfg:rnr/mattock/recycle_trodden_concrete_road_slab",
        "tfg:rnr/mattock/recycle_trodden_concrete_road_stairs"
      ],
      "model_parents": [
        "item/base_course",
        "block/base_course",
        "block/base_course_shape",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 8,
      "loot_source_examples": [
        "rnr:blocks/base_course",
        "rnr:blocks/pouring_concrete_road",
        "rnr:blocks/trodden_wet_concrete_road",
        "rnr:blocks/wet_concrete_road",
        "rnr:blocks/wet_concrete_road_control_joint",
        "rnr:blocks/wet_concrete_road_flagstones",
        "rnr:blocks/wet_concrete_road_panel",
        "rnr:blocks/wet_concrete_road_sett"
      ],
      "block_context": {
        "block_id": "rnr:base_course",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/shovel",
          "rnr:concrete_spreadable",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        }
      }
    },
    {
      "id": "rnr:black_sandstone_flagstones",
      "namespace": "rnr",
      "display_name": "Black Sandstone Flagstones",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/black_sandstone_flagstones"
      ],
      "model_parents": [
        "item/black_sandstone_flagstones",
        "block/black_sandstone_flagstones",
        "block/path_block",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 2,
      "loot_source_examples": [
        "rnr:blocks/black_sandstone_flagstones",
        "rnr:blocks/rock/flagstones/black_sandstone_flagstones"
      ],
      "block_context": {
        "block_id": "rnr:black_sandstone_flagstones",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_blocks",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:black_sandstone_flagstones_slab",
      "namespace": "rnr",
      "display_name": "Black Sandstone Flagstones Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:mattock/slab/black_sandstone_flagstones_slab"
      ],
      "model_parents": [
        "item/black_sandstone_flagstones_slab",
        "block/black_sandstone_flagstones_slab",
        "block/path_slab",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/black_sandstone_flagstones_slab"
      ],
      "block_context": {
        "block_id": "rnr:black_sandstone_flagstones_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_slabs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:black_sandstone_flagstones_stairs",
      "namespace": "rnr",
      "display_name": "Black Sandstone Flagstones Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:mattock/stair/black_sandstone_flagstones_stairs"
      ],
      "model_parents": [
        "item/black_sandstone_flagstones_stairs",
        "block/black_sandstone_flagstones_stairs",
        "block/path_stairs",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/black_sandstone_flagstones_stairs"
      ],
      "block_context": {
        "block_id": "rnr:black_sandstone_flagstones_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_stairs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:brick_road",
      "namespace": "rnr",
      "display_name": "Ceramic Sett Road",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:sett_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/brick_road"
      ],
      "model_parents": [
        "item/brick_road",
        "block/brick_road",
        "block/path_block",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/brick_road"
      ],
      "block_context": {
        "block_id": "rnr:brick_road",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "rnr:sett_road_blocks",
          "tfc:bloomery_insulation",
          "tfc:can_landslide",
          "tfc:forge_insulation",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:brick_road_slab",
      "namespace": "rnr",
      "display_name": "Ceramic Sett Road Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:sett_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1,
        "tfc:landslide": 2
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/brick_road_slab",
        "rnr:landslide/brick_road_stairs",
        "rnr:mattock/slab/brick_road_slab"
      ],
      "model_parents": [
        "item/brick_road_slab",
        "block/brick_road_slab",
        "block/path_slab",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/brick_road_slab"
      ],
      "block_context": {
        "block_id": "rnr:brick_road_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "rnr:sett_road_slabs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:brick_road_stairs",
      "namespace": "rnr",
      "display_name": "Ceramic Sett Road Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:sett_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:mattock/stair/brick_road_stairs"
      ],
      "model_parents": [
        "item/brick_road_stairs",
        "block/brick_road_stairs",
        "block/path_stairs",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/brick_road_stairs"
      ],
      "block_context": {
        "block_id": "rnr:brick_road_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "rnr:sett_road_stairs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:brown_sandstone_flagstones",
      "namespace": "rnr",
      "display_name": "Brown Sandstone Flagstones",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/brown_sandstone_flagstones"
      ],
      "model_parents": [
        "item/brown_sandstone_flagstones",
        "block/brown_sandstone_flagstones",
        "block/path_block",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 2,
      "loot_source_examples": [
        "rnr:blocks/brown_sandstone_flagstones",
        "rnr:blocks/rock/flagstones/brown_sandstone_flagstones"
      ],
      "block_context": {
        "block_id": "rnr:brown_sandstone_flagstones",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_blocks",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:brown_sandstone_flagstones_slab",
      "namespace": "rnr",
      "display_name": "Brown Sandstone Flagstones Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:mattock/slab/brown_sandstone_flagstones_slab"
      ],
      "model_parents": [
        "item/brown_sandstone_flagstones_slab",
        "block/brown_sandstone_flagstones_slab",
        "block/path_slab",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/brown_sandstone_flagstones_slab"
      ],
      "block_context": {
        "block_id": "rnr:brown_sandstone_flagstones_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_slabs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:brown_sandstone_flagstones_stairs",
      "namespace": "rnr",
      "display_name": "Brown Sandstone Flagstones Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:mattock/stair/brown_sandstone_flagstones_stairs"
      ],
      "model_parents": [
        "item/brown_sandstone_flagstones_stairs",
        "block/brown_sandstone_flagstones_stairs",
        "block/path_stairs",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/brown_sandstone_flagstones_stairs"
      ],
      "block_context": {
        "block_id": "rnr:brown_sandstone_flagstones_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_stairs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:bucket/concrete",
      "namespace": "rnr",
      "display_name": "Wet Concrete Mix Bucket",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:concrete_buckets"
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
        "item/bucket/concrete",
        "item/generated"
      ],
      "creative_tabs": [],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Mining Fatigue (01:00)"
        },
        {
          "source": "runtime-tooltip",
          "text": "Tanked (10:00)"
        },
        {
          "source": "runtime-tooltip",
          "text": "Slowness III (10:00)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 300 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "document_context": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/roadsandroofs/concrete_roads",
          "label": "Concrete Roads",
          "item_ref_count": 3,
          "related_item_refs": [
            "gtceu:concrete_bucket",
            "rnr:concrete_road_panel"
          ],
          "snippets": [
            {
              "source": "guide-page",
              "key": "name",
              "text": "Concrete Roads"
            },
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "Concrete roads are the fastest variety of road that can be constructed, offering a 30% movement speed bonus when properly constructed. However, road builders must be diligent when constructing concrete roads to prevent them from cracking or being trodden on while wet, as this will remove the speed bonus."
            },
            {
              "source": "guide-page",
              "key": "pages.1.title",
              "text": "Concrete Road"
            },
            {
              "source": "guide-page",
              "key": "pages.2.title",
              "text": "Concrete Crafting"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:ceramic_roof",
      "namespace": "rnr",
      "display_name": "Ceramic Tile Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:roof_blocks",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 3,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/ceramic_roof",
        "block/ceramic_roof",
        "block/cube_all"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/ceramic_roof"
      ],
      "block_context": {
        "block_id": "rnr:ceramic_roof",
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
          "minecraft:mineable/pickaxe"
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
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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