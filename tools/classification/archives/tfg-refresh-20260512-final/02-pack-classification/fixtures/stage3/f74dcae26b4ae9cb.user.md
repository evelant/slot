# Items to classify
{
  "items": [
    {
      "id": "create:layered_limestone",
      "namespace": "create",
      "display_name": "Layered Limestone",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/limestone",
        "tfg:stone_composition/sedimentary_carbonate",
        "tfg:stone_types/limestone",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 27
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 31,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_carbonate",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfg:stonecutter/create_cut_limestone",
        "tfg:stonecutter/create_cut_limestone_brick_slab_half",
        "tfg:stonecutter/create_cut_limestone_brick_stairs",
        "tfg:stonecutter/create_cut_limestone_brick_wall",
        "tfg:stonecutter/create_cut_limestone_bricks",
        "tfg:stonecutter/create_cut_limestone_slab_half",
        "tfg:stonecutter/create_cut_limestone_stairs",
        "tfg:stonecutter/create_cut_limestone_wall",
        "tfg:stonecutter/create_limestone",
        "tfg:stonecutter/create_limestone_pillar",
        "tfg:stonecutter/create_polished_cut_limestone",
        "tfg:stonecutter/create_polished_cut_limestone_slab_half",
        "tfg:stonecutter/create_polished_cut_limestone_stairs",
        "tfg:stonecutter/create_polished_cut_limestone_wall",
        "tfg:stonecutter/create_small_limestone_brick_slab_half",
        "tfg:stonecutter/create_small_limestone_brick_stairs",
        "tfg:stonecutter/create_small_limestone_brick_wall",
        "tfg:stonecutter/create_small_limestone_bricks",
        "tfg:stonecutter/tfc_rock_bricks_limestone",
        "tfg:stonecutter/tfc_rock_bricks_limestone_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_limestone_stairs",
        "tfg:stonecutter/tfc_rock_bricks_limestone_wall",
        "tfg:stonecutter/tfc_rock_chiseled_limestone",
        "tfg:stonecutter/tfc_rock_smooth_limestone",
        "tfg:stonecutter/tfc_rock_smooth_limestone_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_limestone_stairs",
        "tfg:stonecutter/tfc_rock_smooth_limestone_wall"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/create_layered_limestone",
        "tfg:shaped/limestone_pillar2",
        "tfg:stonecutter/create_layered_limestone"
      ],
      "model_parents": [
        "item/layered_limestone",
        "block/layered_limestone",
        "block/cube_column"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/layered_limestone"
      ],
      "block_context": {
        "block_id": "create:layered_limestone",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
            "greate:milling",
            "kubejs:shapeless",
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
      "id": "create:layered_ochrum",
      "namespace": "create",
      "display_name": "Layered Ochrum",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/ochrum",
        "tfg:stone_composition/ochrum",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 18
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 22,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_ochrum",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfg:stonecutter/create_cut_ochrum",
        "tfg:stonecutter/create_cut_ochrum_brick_slab_half",
        "tfg:stonecutter/create_cut_ochrum_brick_stairs",
        "tfg:stonecutter/create_cut_ochrum_brick_wall",
        "tfg:stonecutter/create_cut_ochrum_bricks",
        "tfg:stonecutter/create_cut_ochrum_slab_half",
        "tfg:stonecutter/create_cut_ochrum_stairs",
        "tfg:stonecutter/create_cut_ochrum_wall",
        "tfg:stonecutter/create_ochrum",
        "tfg:stonecutter/create_ochrum_pillar",
        "tfg:stonecutter/create_polished_cut_ochrum",
        "tfg:stonecutter/create_polished_cut_ochrum_slab_half",
        "tfg:stonecutter/create_polished_cut_ochrum_stairs",
        "tfg:stonecutter/create_polished_cut_ochrum_wall",
        "tfg:stonecutter/create_small_ochrum_brick_slab_half",
        "tfg:stonecutter/create_small_ochrum_brick_stairs",
        "tfg:stonecutter/create_small_ochrum_brick_wall",
        "tfg:stonecutter/create_small_ochrum_bricks"
      ],
      "recipe_output_examples": [
        "tfg:shaped/ochrum_pillar2",
        "tfg:stonecutter/create_layered_ochrum"
      ],
      "model_parents": [
        "item/layered_ochrum",
        "block/layered_ochrum",
        "block/cube_column"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/layered_ochrum"
      ],
      "block_context": {
        "block_id": "create:layered_ochrum",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
            "greate:milling",
            "kubejs:shapeless",
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
      "id": "create:layered_scorchia",
      "namespace": "create",
      "display_name": "Layered Scorchia",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/scorchia",
        "tfg:stone_composition/igneous_mafic",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 18
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 22,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_mafic",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfg:stonecutter/create_cut_scorchia",
        "tfg:stonecutter/create_cut_scorchia_brick_slab_half",
        "tfg:stonecutter/create_cut_scorchia_brick_stairs",
        "tfg:stonecutter/create_cut_scorchia_brick_wall",
        "tfg:stonecutter/create_cut_scorchia_bricks",
        "tfg:stonecutter/create_cut_scorchia_slab_half",
        "tfg:stonecutter/create_cut_scorchia_stairs",
        "tfg:stonecutter/create_cut_scorchia_wall",
        "tfg:stonecutter/create_polished_cut_scorchia",
        "tfg:stonecutter/create_polished_cut_scorchia_slab_half",
        "tfg:stonecutter/create_polished_cut_scorchia_stairs",
        "tfg:stonecutter/create_polished_cut_scorchia_wall",
        "tfg:stonecutter/create_scorchia",
        "tfg:stonecutter/create_scorchia_pillar",
        "tfg:stonecutter/create_small_scorchia_brick_slab_half",
        "tfg:stonecutter/create_small_scorchia_brick_stairs",
        "tfg:stonecutter/create_small_scorchia_brick_wall",
        "tfg:stonecutter/create_small_scorchia_bricks"
      ],
      "recipe_output_examples": [
        "tfg:shaped/scorchia_pillar2",
        "tfg:stonecutter/create_layered_scorchia"
      ],
      "model_parents": [
        "item/layered_scorchia",
        "block/layered_scorchia",
        "block/cube_column"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/layered_scorchia"
      ],
      "block_context": {
        "block_id": "create:layered_scorchia",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
            "greate:milling",
            "kubejs:shapeless",
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
      "id": "create:layered_scoria",
      "namespace": "create",
      "display_name": "Layered Scoria",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/scoria",
        "tfg:stone_composition/igneous_mafic",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 18
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 22,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_mafic",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfg:stonecutter/create_cut_scoria",
        "tfg:stonecutter/create_cut_scoria_brick_slab_half",
        "tfg:stonecutter/create_cut_scoria_brick_stairs",
        "tfg:stonecutter/create_cut_scoria_brick_wall",
        "tfg:stonecutter/create_cut_scoria_bricks",
        "tfg:stonecutter/create_cut_scoria_slab_half",
        "tfg:stonecutter/create_cut_scoria_stairs",
        "tfg:stonecutter/create_cut_scoria_wall",
        "tfg:stonecutter/create_polished_cut_scoria",
        "tfg:stonecutter/create_polished_cut_scoria_slab_half",
        "tfg:stonecutter/create_polished_cut_scoria_stairs",
        "tfg:stonecutter/create_polished_cut_scoria_wall",
        "tfg:stonecutter/create_scoria",
        "tfg:stonecutter/create_scoria_pillar",
        "tfg:stonecutter/create_small_scoria_brick_slab_half",
        "tfg:stonecutter/create_small_scoria_brick_stairs",
        "tfg:stonecutter/create_small_scoria_brick_wall",
        "tfg:stonecutter/create_small_scoria_bricks"
      ],
      "recipe_output_examples": [
        "tfg:shaped/scoria_pillar2",
        "tfg:stonecutter/create_layered_scoria"
      ],
      "model_parents": [
        "item/layered_scoria",
        "block/layered_scoria",
        "block/cube_column"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/layered_scoria"
      ],
      "block_context": {
        "block_id": "create:layered_scoria",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
            "greate:milling",
            "kubejs:shapeless",
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
      "id": "create:layered_tuff",
      "namespace": "create",
      "display_name": "Layered Tuff",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/tuff",
        "tfg:stone_composition/igneous_felsic",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 24
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 28,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_felsic",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfg:stonecutter/create_cut_tuff",
        "tfg:stonecutter/create_cut_tuff_brick_slab_half",
        "tfg:stonecutter/create_cut_tuff_brick_stairs",
        "tfg:stonecutter/create_cut_tuff_brick_wall",
        "tfg:stonecutter/create_cut_tuff_bricks",
        "tfg:stonecutter/create_cut_tuff_slab_half",
        "tfg:stonecutter/create_cut_tuff_stairs",
        "tfg:stonecutter/create_cut_tuff_wall",
        "tfg:stonecutter/create_polished_cut_tuff",
        "tfg:stonecutter/create_polished_cut_tuff_slab_half",
        "tfg:stonecutter/create_polished_cut_tuff_stairs",
        "tfg:stonecutter/create_polished_cut_tuff_wall",
        "tfg:stonecutter/create_small_tuff_brick_slab_half",
        "tfg:stonecutter/create_small_tuff_brick_stairs",
        "tfg:stonecutter/create_small_tuff_brick_wall",
        "tfg:stonecutter/create_small_tuff_bricks",
        "tfg:stonecutter/create_tuff_pillar",
        "tfg:stonecutter/minecraft_tuff",
        "tfg:stonecutter/tfg_rock_bricks_tuff",
        "tfg:stonecutter/tfg_rock_bricks_tuff_slab_half",
        "tfg:stonecutter/tfg_rock_bricks_tuff_stairs",
        "tfg:stonecutter/tfg_rock_bricks_tuff_wall",
        "tfg:stonecutter/tfg_rock_chiseled_tuff",
        "tfg:stonecutter/tfg_rock_chiseled_tuff_bricks"
      ],
      "recipe_output_examples": [
        "tfg:shaped/tuff_pillar2",
        "tfg:stonecutter/create_layered_tuff"
      ],
      "model_parents": [
        "item/layered_tuff",
        "block/layered_tuff",
        "block/cube_column"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/layered_tuff"
      ],
      "block_context": {
        "block_id": "create:layered_tuff",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
            "greate:milling",
            "kubejs:shapeless",
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
      "id": "create:layered_veridium",
      "namespace": "create",
      "display_name": "Layered Veridium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/veridium",
        "tfg:stone_composition/veridium",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 18
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 22,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_veridium",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfg:stonecutter/create_cut_veridium",
        "tfg:stonecutter/create_cut_veridium_brick_slab_half",
        "tfg:stonecutter/create_cut_veridium_brick_stairs",
        "tfg:stonecutter/create_cut_veridium_brick_wall",
        "tfg:stonecutter/create_cut_veridium_bricks",
        "tfg:stonecutter/create_cut_veridium_slab_half",
        "tfg:stonecutter/create_cut_veridium_stairs",
        "tfg:stonecutter/create_cut_veridium_wall",
        "tfg:stonecutter/create_polished_cut_veridium",
        "tfg:stonecutter/create_polished_cut_veridium_slab_half",
        "tfg:stonecutter/create_polished_cut_veridium_stairs",
        "tfg:stonecutter/create_polished_cut_veridium_wall",
        "tfg:stonecutter/create_small_veridium_brick_slab_half",
        "tfg:stonecutter/create_small_veridium_brick_stairs",
        "tfg:stonecutter/create_small_veridium_brick_wall",
        "tfg:stonecutter/create_small_veridium_bricks",
        "tfg:stonecutter/create_veridium",
        "tfg:stonecutter/create_veridium_pillar"
      ],
      "recipe_output_examples": [
        "tfg:shaped/veridium_pillar2",
        "tfg:stonecutter/create_layered_veridium"
      ],
      "model_parents": [
        "item/layered_veridium",
        "block/layered_veridium",
        "block/cube_column"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/layered_veridium"
      ],
      "block_context": {
        "block_id": "create:layered_veridium",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
            "greate:milling",
            "kubejs:shapeless",
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
      "id": "create:light_blue_postbox",
      "namespace": "create",
      "display_name": "Light Blue Postbox",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:postboxes"
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
        "minecraft:barrel/create/light_blue_postbox"
      ],
      "model_parents": [
        "item/light_blue_postbox",
        "block/package_postbox/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/light_blue_postbox"
      ],
      "block_context": {
        "block_id": "create:light_blue_postbox",
        "block_tags": [
          "create:postboxes",
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:light_blue_seat",
      "namespace": "create",
      "display_name": "Light Blue Seat",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:seats",
        "tfg:colored_seats"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:mechanical_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 4,
        "crafting_shapeless": 1,
        "create:mechanical_crafting": 5
      },
      "recipe_production_by_type": {
        "tfc:barrel_sealed": 1
      },
      "recipe_ingredient_count": 10,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "minecraft:kjs/create_connected_music_disc_interlude",
        "tfg:create/shaped/turntable",
        "tfg:immersive_aircraft/mechanical_crafter/bamboo_hopper",
        "tfg:immersive_aircraft/mechanical_crafter/gyrodyne",
        "tfg:immersive_aircraft/mechanical_crafter/warship",
        "tfg:immersive_aircraft/shaped/airship",
        "tfg:immersive_aircraft/shaped/quadrocopter",
        "tfg:man_of_many_planes/mechanical_crafter/economy_plane",
        "tfg:man_of_many_planes/mechanical_crafter/scarlet_biplane",
        "tfg:railways/shaped/handcar"
      ],
      "recipe_output_examples": [
        "minecraft:barrel/create/light_blue_seat"
      ],
      "model_parents": [
        "item/light_blue_seat",
        "block/light_blue_seat",
        "block/seat",
        "block/block"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/light_blue_seat"
      ],
      "block_context": {
        "block_id": "create:light_blue_seat",
        "block_tags": [
          "create:seats",
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe",
          "tfg:colored_seats"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
            "crafting",
            "create:mechanical_crafting"
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
      "id": "create:light_blue_table_cloth",
      "namespace": "create",
      "display_name": "Light Blue Table Cloth",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:dyed_table_cloths",
        "create:table_cloths"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "tfc:barrel_sealed": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "create:crafting/logistics/light_blue_table_cloth_clear"
      ],
      "recipe_output_examples": [
        "create:crafting/logistics/light_blue_table_cloth_clear",
        "minecraft:barrel/create/light_blue_table_cloth"
      ],
      "model_parents": [
        "item/light_blue_table_cloth",
        "block/table_cloth/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/light_blue_table_cloth"
      ],
      "block_context": {
        "block_id": "create:light_blue_table_cloth",
        "block_tags": [
          "create:table_cloths",
          "ftbchunks:interact_whitelist",
          "minecraft:combination_step_sound_blocks"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:light_blue_toolbox",
      "namespace": "create",
      "display_name": "Light Blue Toolbox",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:non_movable",
        "create:toolboxes",
        "tfg:cannot_launch_in_railgun"
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
        "minecraft:barrel/create/light_blue_toolbox"
      ],
      "model_parents": [
        "item/light_blue_toolbox",
        "block/toolbox/item"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/light_blue_toolbox"
      ],
      "block_context": {
        "block_id": "create:light_blue_toolbox",
        "block_tags": [
          "create:non_movable",
          "create:toolboxes"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:light_blue_valve_handle",
      "namespace": "create",
      "display_name": "Light Blue Valve Handle",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:valve_handles",
        "tfg:colored_valve_handles"
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
        "minecraft:barrel/create/light_blue_valve_handle"
      ],
      "model_parents": [
        "item/light_blue_valve_handle",
        "block/light_blue_valve_handle",
        "block/valve_handle",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/light_blue_valve_handle"
      ],
      "block_context": {
        "block_id": "create:light_blue_valve_handle",
        "block_tags": [
          "create:brittle",
          "create:valve_handles",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfg:colored_valve_handles"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:light_gray_postbox",
      "namespace": "create",
      "display_name": "Light Gray Postbox",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:postboxes"
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
        "minecraft:barrel/create/light_gray_postbox"
      ],
      "model_parents": [
        "item/light_gray_postbox",
        "block/package_postbox/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/light_gray_postbox"
      ],
      "block_context": {
        "block_id": "create:light_gray_postbox",
        "block_tags": [
          "create:postboxes",
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:light_gray_seat",
      "namespace": "create",
      "display_name": "Light Gray Seat",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:seats",
        "tfg:colored_seats"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:mechanical_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 4,
        "crafting_shapeless": 1,
        "create:mechanical_crafting": 5
      },
      "recipe_production_by_type": {
        "tfc:barrel_sealed": 1
      },
      "recipe_ingredient_count": 10,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "minecraft:kjs/create_connected_music_disc_interlude",
        "tfg:create/shaped/turntable",
        "tfg:immersive_aircraft/mechanical_crafter/bamboo_hopper",
        "tfg:immersive_aircraft/mechanical_crafter/gyrodyne",
        "tfg:immersive_aircraft/mechanical_crafter/warship",
        "tfg:immersive_aircraft/shaped/airship",
        "tfg:immersive_aircraft/shaped/quadrocopter",
        "tfg:man_of_many_planes/mechanical_crafter/economy_plane",
        "tfg:man_of_many_planes/mechanical_crafter/scarlet_biplane",
        "tfg:railways/shaped/handcar"
      ],
      "recipe_output_examples": [
        "minecraft:barrel/create/light_gray_seat"
      ],
      "model_parents": [
        "item/light_gray_seat",
        "block/light_gray_seat",
        "block/seat",
        "block/block"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/light_gray_seat"
      ],
      "block_context": {
        "block_id": "create:light_gray_seat",
        "block_tags": [
          "create:seats",
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe",
          "tfg:colored_seats"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
            "crafting",
            "create:mechanical_crafting"
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
      "id": "create:light_gray_table_cloth",
      "namespace": "create",
      "display_name": "Light Gray Table Cloth",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:dyed_table_cloths",
        "create:table_cloths"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "tfc:barrel_sealed": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "create:crafting/logistics/light_gray_table_cloth_clear"
      ],
      "recipe_output_examples": [
        "create:crafting/logistics/light_gray_table_cloth_clear",
        "minecraft:barrel/create/light_gray_table_cloth"
      ],
      "model_parents": [
        "item/light_gray_table_cloth",
        "block/table_cloth/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/light_gray_table_cloth"
      ],
      "block_context": {
        "block_id": "create:light_gray_table_cloth",
        "block_tags": [
          "create:table_cloths",
          "ftbchunks:interact_whitelist",
          "minecraft:combination_step_sound_blocks"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:light_gray_toolbox",
      "namespace": "create",
      "display_name": "Light Gray Toolbox",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:non_movable",
        "create:toolboxes",
        "tfg:cannot_launch_in_railgun"
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
        "minecraft:barrel/create/light_gray_toolbox"
      ],
      "model_parents": [
        "item/light_gray_toolbox",
        "block/toolbox/item"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/light_gray_toolbox"
      ],
      "block_context": {
        "block_id": "create:light_gray_toolbox",
        "block_tags": [
          "create:non_movable",
          "create:toolboxes"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:light_gray_valve_handle",
      "namespace": "create",
      "display_name": "Light Gray Valve Handle",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:valve_handles",
        "tfg:colored_valve_handles"
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
        "minecraft:barrel/create/light_gray_valve_handle"
      ],
      "model_parents": [
        "item/light_gray_valve_handle",
        "block/light_gray_valve_handle",
        "block/valve_handle",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/light_gray_valve_handle"
      ],
      "block_context": {
        "block_id": "create:light_gray_valve_handle",
        "block_tags": [
          "create:brittle",
          "create:valve_handles",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfg:colored_valve_handles"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:lime_postbox",
      "namespace": "create",
      "display_name": "Lime Postbox",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:postboxes"
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
        "minecraft:barrel/create/lime_postbox"
      ],
      "model_parents": [
        "item/lime_postbox",
        "block/package_postbox/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/lime_postbox"
      ],
      "block_context": {
        "block_id": "create:lime_postbox",
        "block_tags": [
          "create:postboxes",
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:lime_seat",
      "namespace": "create",
      "display_name": "Lime Seat",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:seats",
        "tfg:colored_seats"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:mechanical_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 4,
        "crafting_shapeless": 1,
        "create:mechanical_crafting": 5
      },
      "recipe_production_by_type": {
        "tfc:barrel_sealed": 1
      },
      "recipe_ingredient_count": 10,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "minecraft:kjs/create_connected_music_disc_interlude",
        "tfg:create/shaped/turntable",
        "tfg:immersive_aircraft/mechanical_crafter/bamboo_hopper",
        "tfg:immersive_aircraft/mechanical_crafter/gyrodyne",
        "tfg:immersive_aircraft/mechanical_crafter/warship",
        "tfg:immersive_aircraft/shaped/airship",
        "tfg:immersive_aircraft/shaped/quadrocopter",
        "tfg:man_of_many_planes/mechanical_crafter/economy_plane",
        "tfg:man_of_many_planes/mechanical_crafter/scarlet_biplane",
        "tfg:railways/shaped/handcar"
      ],
      "recipe_output_examples": [
        "minecraft:barrel/create/lime_seat"
      ],
      "model_parents": [
        "item/lime_seat",
        "block/lime_seat",
        "block/seat",
        "block/block"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/lime_seat"
      ],
      "block_context": {
        "block_id": "create:lime_seat",
        "block_tags": [
          "create:seats",
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe",
          "tfg:colored_seats"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
            "crafting",
            "create:mechanical_crafting"
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
      "id": "create:lime_table_cloth",
      "namespace": "create",
      "display_name": "Lime Table Cloth",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:dyed_table_cloths",
        "create:table_cloths"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "tfc:barrel_sealed": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "create:crafting/logistics/lime_table_cloth_clear"
      ],
      "recipe_output_examples": [
        "create:crafting/logistics/lime_table_cloth_clear",
        "minecraft:barrel/create/lime_table_cloth"
      ],
      "model_parents": [
        "item/lime_table_cloth",
        "block/table_cloth/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/lime_table_cloth"
      ],
      "block_context": {
        "block_id": "create:lime_table_cloth",
        "block_tags": [
          "create:table_cloths",
          "ftbchunks:interact_whitelist",
          "minecraft:combination_step_sound_blocks"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:lime_toolbox",
      "namespace": "create",
      "display_name": "Lime Toolbox",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:non_movable",
        "create:toolboxes",
        "tfg:cannot_launch_in_railgun"
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
        "minecraft:barrel/create/lime_toolbox"
      ],
      "model_parents": [
        "item/lime_toolbox",
        "block/toolbox/item"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/lime_toolbox"
      ],
      "block_context": {
        "block_id": "create:lime_toolbox",
        "block_tags": [
          "create:non_movable",
          "create:toolboxes"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:lime_valve_handle",
      "namespace": "create",
      "display_name": "Lime Valve Handle",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:valve_handles",
        "tfg:colored_valve_handles"
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
        "minecraft:barrel/create/lime_valve_handle"
      ],
      "model_parents": [
        "item/lime_valve_handle",
        "block/lime_valve_handle",
        "block/valve_handle",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/lime_valve_handle"
      ],
      "block_context": {
        "block_id": "create:lime_valve_handle",
        "block_tags": [
          "create:brittle",
          "create:valve_handles",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfg:colored_valve_handles"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:limestone",
      "namespace": "create",
      "display_name": "Limestone",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/limestone",
        "forge:stone_bricks",
        "tfc:rock/bricks",
        "tfc:rock/chiseled_bricks",
        "tfg:stone_composition/sedimentary_carbonate",
        "tfg:stone_types/limestone",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 21,
        "greate:milling": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 27
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 53,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/sealed_bricks",
        "greate:milling/integration/tfg/macerate_sedimentary_carbonate",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfg:shapeless/bismuth_bronze_plated_block",
        "tfg:shapeless/bismuth_plated_block",
        "tfg:shapeless/black_bronze_plated_block",
        "tfg:shapeless/black_steel_plated_block",
        "tfg:shapeless/blue_steel_plated_block",
        "tfg:shapeless/brass_plated_block",
        "tfg:shapeless/bronze_plated_block",
        "tfg:shapeless/chromium_plated_block",
        "tfg:shapeless/copper_plated_block",
        "tfg:shapeless/gold_plated_block",
        "tfg:shapeless/iron_plated_block",
        "tfg:shapeless/nickel_plated_block",
        "tfg:shapeless/red_steel_plated_block",
        "tfg:shapeless/rose_gold_plated_block",
        "tfg:shapeless/silver_plated_block",
        "tfg:shapeless/stainless_steel_plated_block",
        "tfg:shapeless/steel_plated_block",
        "tfg:shapeless/sterling_silver_plated_block",
        "tfg:shapeless/tin_plated_block",
        "tfg:shapeless/wrought_iron_plated_block",
        "tfg:shapeless/zinc_plated_block",
        "tfg:stonecutter/create_cut_limestone",
        "tfg:stonecutter/create_cut_limestone_brick_slab_half",
        "tfg:stonecutter/create_cut_limestone_brick_stairs",
        "tfg:stonecutter/create_cut_limestone_brick_wall",
        "tfg:stonecutter/create_cut_limestone_bricks",
        "tfg:stonecutter/create_cut_limestone_slab_half",
        "tfg:stonecutter/create_cut_limestone_stairs",
        "tfg:stonecutter/create_cut_limestone_wall",
        "tfg:stonecutter/create_layered_limestone",
        "tfg:stonecutter/create_limestone_pillar",
        "tfg:stonecutter/create_polished_cut_limestone",
        "tfg:stonecutter/create_polished_cut_limestone_slab_half",
        "tfg:stonecutter/create_polished_cut_limestone_stairs",
        "tfg:stonecutter/create_polished_cut_limestone_wall",
        "tfg:stonecutter/create_small_limestone_brick_slab_half",
        "tfg:stonecutter/create_small_limestone_brick_stairs",
        "tfg:stonecutter/create_small_limestone_brick_wall",
        "tfg:stonecutter/create_small_limestone_bricks",
        "tfg:stonecutter/tfc_rock_bricks_limestone",
        "tfg:stonecutter/tfc_rock_bricks_limestone_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_limestone_stairs",
        "tfg:stonecutter/tfc_rock_bricks_limestone_wall",
        "tfg:stonecutter/tfc_rock_chiseled_limestone",
        "tfg:stonecutter/tfc_rock_smooth_limestone",
        "tfg:stonecutter/tfc_rock_smooth_limestone_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_limestone_stairs",
        "tfg:stonecutter/tfc_rock_smooth_limestone_wall"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/create_limestone"
      ],
      "model_parents": [
        "item/limestone",
        "block/cube_all"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/limestone"
      ],
      "block_context": {
        "block_id": "create:limestone",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:azalea_root_replaceable",
          "minecraft:dripstone_replaceable_blocks",
          "minecraft:lush_ground_replaceable",
          "minecraft:mineable/pickaxe",
          "minecraft:moss_replaceable",
          "tfc:bloomery_insulation",
          "tfc:forge_insulation"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
            "greate:milling",
            "kubejs:shapeless",
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
      "id": "create:limestone_pillar",
      "namespace": "create",
      "display_name": "Limestone Pillar",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/limestone",
        "tfg:stone_composition/sedimentary_carbonate",
        "tfg:stone_types/limestone",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 27
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 31,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_carbonate",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfg:stonecutter/create_cut_limestone",
        "tfg:stonecutter/create_cut_limestone_brick_slab_half",
        "tfg:stonecutter/create_cut_limestone_brick_stairs",
        "tfg:stonecutter/create_cut_limestone_brick_wall",
        "tfg:stonecutter/create_cut_limestone_bricks",
        "tfg:stonecutter/create_cut_limestone_slab_half",
        "tfg:stonecutter/create_cut_limestone_stairs",
        "tfg:stonecutter/create_cut_limestone_wall",
        "tfg:stonecutter/create_layered_limestone",
        "tfg:stonecutter/create_limestone",
        "tfg:stonecutter/create_polished_cut_limestone",
        "tfg:stonecutter/create_polished_cut_limestone_slab_half",
        "tfg:stonecutter/create_polished_cut_limestone_stairs",
        "tfg:stonecutter/create_polished_cut_limestone_wall",
        "tfg:stonecutter/create_small_limestone_brick_slab_half",
        "tfg:stonecutter/create_small_limestone_brick_stairs",
        "tfg:stonecutter/create_small_limestone_brick_wall",
        "tfg:stonecutter/create_small_limestone_bricks",
        "tfg:stonecutter/tfc_rock_bricks_limestone",
        "tfg:stonecutter/tfc_rock_bricks_limestone_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_limestone_stairs",
        "tfg:stonecutter/tfc_rock_bricks_limestone_wall",
        "tfg:stonecutter/tfc_rock_chiseled_limestone",
        "tfg:stonecutter/tfc_rock_smooth_limestone",
        "tfg:stonecutter/tfc_rock_smooth_limestone_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_limestone_stairs",
        "tfg:stonecutter/tfc_rock_smooth_limestone_wall"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/create_limestone_pillar",
        "tfg:shaped/limestone_pillar",
        "tfg:stonecutter/create_limestone_pillar"
      ],
      "model_parents": [
        "item/limestone_pillar",
        "block/limestone_pillar",
        "block/cube_column"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/limestone_pillar"
      ],
      "block_context": {
        "block_id": "create:limestone_pillar",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
            "greate:milling",
            "kubejs:shapeless",
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
      "id": "create:linear_chassis",
      "namespace": "create",
      "display_name": "Linear Chassis",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "kubejs:shaped": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_linear_chassis",
        "minecraft:kjs/create_secondary_linear_chassis"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/create_linear_chassis",
        "tfg:create/shaped/linear_chassis"
      ],
      "model_parents": [
        "item/linear_chassis",
        "block/linear_chassis",
        "block/cube_bottom_top"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/linear_chassis"
      ],
      "block_context": {
        "block_id": "create:linear_chassis",
        "block_tags": [
          "create:safe_nbt",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
            "crafting",
            "greate:milling"
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
      "id": "create:linked_controller",
      "namespace": "create",
      "display_name": "Linked Controller",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "create:crafting/appliances/linked_controller",
        "tfg:create/shaped/linked_controller"
      ],
      "model_parents": [
        "item/linked_controller",
        "item/linked_controller/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
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
          "text": "Create"
        },
        {
          "source": "lang",
          "key": "item.create.linked_controller.tooltip.behaviour1",
          "text": "_Toggles_ the controller. _Movement_ _controls_ are taken over while its active."
        },
        {
          "source": "lang",
          "key": "item.create.linked_controller.tooltip.behaviour2",
          "text": "Opens the manual _Configuration Interface_."
        },
        {
          "source": "lang",
          "key": "item.create.linked_controller.tooltip.behaviour3",
          "text": "Enables _Bind Mode_, press one of the _six controls_ to bind it to the _Links' Frequency_."
        },
        {
          "source": "lang",
          "key": "item.create.linked_controller.tooltip.behaviour4",
          "text": "Places the Controller into the Lectern for easy activation. (R-Click while Sneaking to retrieve it)"
        },
        {
          "source": "lang",
          "key": "item.create.linked_controller.tooltip.condition1",
          "text": "R-Click"
        },
        {
          "source": "lang",
          "key": "item.create.linked_controller.tooltip.condition2",
          "text": "R-Click while Sneaking"
        },
        {
          "source": "lang",
          "key": "item.create.linked_controller.tooltip.condition3",
          "text": "R-Click on Redstone Link Receiver"
        },
        {
          "source": "lang",
          "key": "item.create.linked_controller.tooltip.condition4",
          "text": "R-Click on Lectern"
        },
        {
          "source": "lang",
          "key": "item.create.linked_controller.tooltip.summary",
          "text": "Grants _handheld_ _control_ over _Redstone Link_ frequencies assigned to its _six_ _buttons_."
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "create:linked_controller",
          "label": "Remote Activation",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Remote Activation"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Activate a Redstone Link using a Linked Controller"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
          "confidence": 1,
          "source": "rule:mod_namespace"
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