# Items to classify
{
  "items": [
    {
      "id": "create:small_deepslate_brick_wall",
      "namespace": "create",
      "display_name": "Small Migmatite Brick Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/deepslate",
        "minecraft:walls",
        "tfg:stone_composition/metamorphic_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 30
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 31,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_metamorphic_half",
        "tfg:stonecutter/create_cut_deepslate",
        "tfg:stonecutter/create_cut_deepslate_brick_slab_half",
        "tfg:stonecutter/create_cut_deepslate_brick_stairs",
        "tfg:stonecutter/create_cut_deepslate_brick_wall",
        "tfg:stonecutter/create_cut_deepslate_bricks",
        "tfg:stonecutter/create_cut_deepslate_slab_half",
        "tfg:stonecutter/create_cut_deepslate_stairs",
        "tfg:stonecutter/create_cut_deepslate_wall",
        "tfg:stonecutter/create_deepslate_pillar",
        "tfg:stonecutter/create_layered_deepslate",
        "tfg:stonecutter/create_polished_cut_deepslate",
        "tfg:stonecutter/create_polished_cut_deepslate_slab_half",
        "tfg:stonecutter/create_polished_cut_deepslate_stairs",
        "tfg:stonecutter/create_polished_cut_deepslate_wall",
        "tfg:stonecutter/create_small_deepslate_brick_slab_half",
        "tfg:stonecutter/create_small_deepslate_brick_stairs",
        "tfg:stonecutter/create_small_deepslate_bricks",
        "tfg:stonecutter/minecraft_chiseled_deepslate",
        "tfg:stonecutter/minecraft_deepslate_brick_slab_half",
        "tfg:stonecutter/minecraft_deepslate_brick_stairs",
        "tfg:stonecutter/minecraft_deepslate_brick_wall",
        "tfg:stonecutter/minecraft_deepslate_bricks",
        "tfg:stonecutter/minecraft_deepslate_tile_slab_half",
        "tfg:stonecutter/minecraft_deepslate_tile_stairs",
        "tfg:stonecutter/minecraft_deepslate_tile_wall",
        "tfg:stonecutter/minecraft_deepslate_tiles",
        "tfg:stonecutter/minecraft_polished_deepslate",
        "tfg:stonecutter/minecraft_polished_deepslate_slab_half",
        "tfg:stonecutter/minecraft_polished_deepslate_stairs",
        "tfg:stonecutter/minecraft_polished_deepslate_wall"
      ],
      "recipe_output_examples": [
        "tfc:kjs/1zc5tchc97ads1rk5mpkihya3",
        "tfg:stonecutter/create_small_deepslate_brick_wall",
        "tfg:stonecutting/create_small_deepslate_bricks_to_create_small_deepslate_brick_wall"
      ],
      "model_parents": [
        "item/small_deepslate_brick_wall",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_deepslate_brick_wall"
      ],
      "block_context": {
        "block_id": "create:small_deepslate_brick_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:walls"
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
        "form": {
          "value": "wall",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:walls"
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
      "id": "create:small_deepslate_bricks",
      "namespace": "create",
      "display_name": "Small Migmatite Bricks",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/deepslate",
        "forge:stone_bricks",
        "tfc:rock/bricks",
        "tfc:rock/chiseled_bricks",
        "tfg:stone_composition/metamorphic",
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
        "stonecutting": 33
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 59,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/sealed_bricks",
        "greate:milling/integration/tfg/macerate_metamorphic",
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
        "tfg:stonecutter/create_cut_deepslate",
        "tfg:stonecutter/create_cut_deepslate_brick_slab_half",
        "tfg:stonecutter/create_cut_deepslate_brick_stairs",
        "tfg:stonecutter/create_cut_deepslate_brick_wall",
        "tfg:stonecutter/create_cut_deepslate_bricks",
        "tfg:stonecutter/create_cut_deepslate_slab_half",
        "tfg:stonecutter/create_cut_deepslate_stairs",
        "tfg:stonecutter/create_cut_deepslate_wall",
        "tfg:stonecutter/create_deepslate_pillar",
        "tfg:stonecutter/create_layered_deepslate",
        "tfg:stonecutter/create_polished_cut_deepslate",
        "tfg:stonecutter/create_polished_cut_deepslate_slab_half",
        "tfg:stonecutter/create_polished_cut_deepslate_stairs",
        "tfg:stonecutter/create_polished_cut_deepslate_wall",
        "tfg:stonecutter/create_small_deepslate_brick_slab_half",
        "tfg:stonecutter/create_small_deepslate_brick_stairs",
        "tfg:stonecutter/create_small_deepslate_brick_wall",
        "tfg:stonecutter/create_small_deepslate_bricks_to_create_small_deepslate_brick_stairs",
        "tfg:stonecutter/minecraft_chiseled_deepslate",
        "tfg:stonecutter/minecraft_deepslate_brick_slab_half",
        "tfg:stonecutter/minecraft_deepslate_brick_stairs",
        "tfg:stonecutter/minecraft_deepslate_brick_wall",
        "tfg:stonecutter/minecraft_deepslate_bricks",
        "tfg:stonecutter/minecraft_deepslate_tile_slab_half",
        "tfg:stonecutter/minecraft_deepslate_tile_stairs",
        "tfg:stonecutter/minecraft_deepslate_tile_wall",
        "tfg:stonecutter/minecraft_deepslate_tiles",
        "tfg:stonecutter/minecraft_polished_deepslate",
        "tfg:stonecutter/minecraft_polished_deepslate_slab_half",
        "tfg:stonecutter/minecraft_polished_deepslate_stairs",
        "tfg:stonecutter/minecraft_polished_deepslate_wall",
        "tfg:stonecutting/create_small_deepslate_bricks_to_create_small_deepslate_brick_slab",
        "tfg:stonecutting/create_small_deepslate_bricks_to_create_small_deepslate_brick_wall"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/create_small_deepslate_bricks"
      ],
      "model_parents": [
        "item/small_deepslate_bricks",
        "block/small_deepslate_bricks",
        "block/cube_all"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_deepslate_bricks"
      ],
      "block_context": {
        "block_id": "create:small_deepslate_bricks",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
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
      "id": "create:small_diorite_brick_slab",
      "namespace": "create",
      "display_name": "Small Diorite Brick Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:slabs",
        "tfg:stone_composition/igneous_intermediate_half",
        "tfg:stone_types/diorite_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 2
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_intermediate_half",
        "tfg:stonecutter/create_cut_diorite_brick_slab_slab_to_slab",
        "tfg:stonecutter/create_cut_diorite_slab_slab_to_slab",
        "tfg:stonecutter/create_polished_cut_diorite_slab_slab_to_slab",
        "tfg:stonecutter/tfc_rock_bricks_diorite_slab_slab_to_slab",
        "tfg:stonecutter/tfc_rock_smooth_diorite_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/create_small_diorite_brick_slab_half",
        "tfg:stonecutter/create_small_diorite_brick_slab_slab_to_slab"
      ],
      "model_parents": [
        "item/small_diorite_brick_slab",
        "block/small_diorite_brick_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_diorite_brick_slab"
      ],
      "block_context": {
        "block_id": "create:small_diorite_brick_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs"
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
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "create:small_diorite_brick_stairs",
      "namespace": "create",
      "display_name": "Small Diorite Brick Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/diorite",
        "minecraft:stairs",
        "tfg:stone_composition/igneous_intermediate",
        "tfg:stone_types/diorite"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 26
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 27,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_intermediate",
        "tfg:stonecutter/create_cut_diorite",
        "tfg:stonecutter/create_cut_diorite_brick_slab_half",
        "tfg:stonecutter/create_cut_diorite_brick_stairs",
        "tfg:stonecutter/create_cut_diorite_brick_wall",
        "tfg:stonecutter/create_cut_diorite_bricks",
        "tfg:stonecutter/create_cut_diorite_slab_half",
        "tfg:stonecutter/create_cut_diorite_stairs",
        "tfg:stonecutter/create_cut_diorite_wall",
        "tfg:stonecutter/create_diorite_pillar",
        "tfg:stonecutter/create_layered_diorite",
        "tfg:stonecutter/create_polished_cut_diorite",
        "tfg:stonecutter/create_polished_cut_diorite_slab_half",
        "tfg:stonecutter/create_polished_cut_diorite_stairs",
        "tfg:stonecutter/create_polished_cut_diorite_wall",
        "tfg:stonecutter/create_small_diorite_brick_slab_half",
        "tfg:stonecutter/create_small_diorite_brick_wall",
        "tfg:stonecutter/create_small_diorite_bricks",
        "tfg:stonecutter/tfc_rock_bricks_diorite",
        "tfg:stonecutter/tfc_rock_bricks_diorite_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_diorite_stairs",
        "tfg:stonecutter/tfc_rock_bricks_diorite_wall",
        "tfg:stonecutter/tfc_rock_chiseled_diorite",
        "tfg:stonecutter/tfc_rock_smooth_diorite",
        "tfg:stonecutter/tfc_rock_smooth_diorite_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_diorite_stairs",
        "tfg:stonecutter/tfc_rock_smooth_diorite_wall"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/create_small_diorite_brick_stairs"
      ],
      "model_parents": [
        "item/small_diorite_brick_stairs",
        "block/small_diorite_brick_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_diorite_brick_stairs"
      ],
      "block_context": {
        "block_id": "create:small_diorite_brick_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs"
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
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "create:small_diorite_brick_wall",
      "namespace": "create",
      "display_name": "Small Diorite Brick Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/diorite",
        "minecraft:walls",
        "tfg:stone_composition/igneous_intermediate_half",
        "tfg:stone_types/diorite"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 26
      },
      "recipe_production_by_type": {
        "stonecutting": 1,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 27,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_intermediate_half",
        "tfg:stonecutter/create_cut_diorite",
        "tfg:stonecutter/create_cut_diorite_brick_slab_half",
        "tfg:stonecutter/create_cut_diorite_brick_stairs",
        "tfg:stonecutter/create_cut_diorite_brick_wall",
        "tfg:stonecutter/create_cut_diorite_bricks",
        "tfg:stonecutter/create_cut_diorite_slab_half",
        "tfg:stonecutter/create_cut_diorite_stairs",
        "tfg:stonecutter/create_cut_diorite_wall",
        "tfg:stonecutter/create_diorite_pillar",
        "tfg:stonecutter/create_layered_diorite",
        "tfg:stonecutter/create_polished_cut_diorite",
        "tfg:stonecutter/create_polished_cut_diorite_slab_half",
        "tfg:stonecutter/create_polished_cut_diorite_stairs",
        "tfg:stonecutter/create_polished_cut_diorite_wall",
        "tfg:stonecutter/create_small_diorite_brick_slab_half",
        "tfg:stonecutter/create_small_diorite_brick_stairs",
        "tfg:stonecutter/create_small_diorite_bricks",
        "tfg:stonecutter/tfc_rock_bricks_diorite",
        "tfg:stonecutter/tfc_rock_bricks_diorite_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_diorite_stairs",
        "tfg:stonecutter/tfc_rock_bricks_diorite_wall",
        "tfg:stonecutter/tfc_rock_chiseled_diorite",
        "tfg:stonecutter/tfc_rock_smooth_diorite",
        "tfg:stonecutter/tfc_rock_smooth_diorite_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_diorite_stairs",
        "tfg:stonecutter/tfc_rock_smooth_diorite_wall"
      ],
      "recipe_output_examples": [
        "tfc:kjs/4g7xoy6f5772detmvnznv3pzl",
        "tfg:stonecutter/create_small_diorite_brick_wall"
      ],
      "model_parents": [
        "item/small_diorite_brick_wall",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_diorite_brick_wall"
      ],
      "block_context": {
        "block_id": "create:small_diorite_brick_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:walls"
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
        "form": {
          "value": "wall",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:walls"
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
      "id": "create:small_diorite_bricks",
      "namespace": "create",
      "display_name": "Small Diorite Bricks",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/diorite",
        "forge:stone_bricks",
        "tfc:rock/bricks",
        "tfc:rock/chiseled_bricks",
        "tfg:stone_composition/igneous_intermediate",
        "tfg:stone_types/diorite",
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
        "stonecutting": 26
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 52,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/sealed_bricks",
        "greate:milling/integration/tfg/macerate_igneous_intermediate",
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
        "tfg:stonecutter/create_cut_diorite",
        "tfg:stonecutter/create_cut_diorite_brick_slab_half",
        "tfg:stonecutter/create_cut_diorite_brick_stairs",
        "tfg:stonecutter/create_cut_diorite_brick_wall",
        "tfg:stonecutter/create_cut_diorite_bricks",
        "tfg:stonecutter/create_cut_diorite_slab_half",
        "tfg:stonecutter/create_cut_diorite_stairs",
        "tfg:stonecutter/create_cut_diorite_wall",
        "tfg:stonecutter/create_diorite_pillar",
        "tfg:stonecutter/create_layered_diorite",
        "tfg:stonecutter/create_polished_cut_diorite",
        "tfg:stonecutter/create_polished_cut_diorite_slab_half",
        "tfg:stonecutter/create_polished_cut_diorite_stairs",
        "tfg:stonecutter/create_polished_cut_diorite_wall",
        "tfg:stonecutter/create_small_diorite_brick_slab_half",
        "tfg:stonecutter/create_small_diorite_brick_stairs",
        "tfg:stonecutter/create_small_diorite_brick_wall",
        "tfg:stonecutter/tfc_rock_bricks_diorite",
        "tfg:stonecutter/tfc_rock_bricks_diorite_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_diorite_stairs",
        "tfg:stonecutter/tfc_rock_bricks_diorite_wall",
        "tfg:stonecutter/tfc_rock_chiseled_diorite",
        "tfg:stonecutter/tfc_rock_smooth_diorite",
        "tfg:stonecutter/tfc_rock_smooth_diorite_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_diorite_stairs",
        "tfg:stonecutter/tfc_rock_smooth_diorite_wall"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/create_small_diorite_bricks"
      ],
      "model_parents": [
        "item/small_diorite_bricks",
        "block/small_diorite_bricks",
        "block/cube_all"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_diorite_bricks"
      ],
      "block_context": {
        "block_id": "create:small_diorite_bricks",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
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
      "id": "create:small_dripstone_brick_slab",
      "namespace": "create",
      "display_name": "Small Travertine Brick Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/dripstone_half",
        "minecraft:slabs",
        "tfg:stone_composition/sedimentary_carbonate_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 3
      },
      "recipe_production_by_type": {
        "stonecutting": 3,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_carbonate_half",
        "tfg:stonecutter/create_cut_dripstone_brick_slab_slab_to_slab",
        "tfg:stonecutter/create_cut_dripstone_slab_slab_to_slab",
        "tfg:stonecutter/create_polished_cut_dripstone_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfg:chisel/create_small_dripstone_bricks_to_create_small_dripstone_brick_slab",
        "tfg:stonecutter/create_small_dripstone_brick_slab_half",
        "tfg:stonecutter/create_small_dripstone_brick_slab_slab_to_slab",
        "tfg:stonecutting/create_small_dripstone_bricks_to_create_small_dripstone_brick_slab"
      ],
      "model_parents": [
        "item/small_dripstone_brick_slab",
        "block/small_dripstone_brick_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_dripstone_brick_slab"
      ],
      "block_context": {
        "block_id": "create:small_dripstone_brick_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs"
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
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "create:small_dripstone_brick_stairs",
      "namespace": "create",
      "display_name": "Small Travertine Brick Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/dripstone",
        "minecraft:stairs",
        "tfg:stone_composition/sedimentary_carbonate"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 17
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 18,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_carbonate",
        "tfg:stonecutter/create_cut_dripstone",
        "tfg:stonecutter/create_cut_dripstone_brick_slab_half",
        "tfg:stonecutter/create_cut_dripstone_brick_stairs",
        "tfg:stonecutter/create_cut_dripstone_brick_wall",
        "tfg:stonecutter/create_cut_dripstone_bricks",
        "tfg:stonecutter/create_cut_dripstone_slab_half",
        "tfg:stonecutter/create_cut_dripstone_stairs",
        "tfg:stonecutter/create_cut_dripstone_wall",
        "tfg:stonecutter/create_dripstone_pillar",
        "tfg:stonecutter/create_layered_dripstone",
        "tfg:stonecutter/create_polished_cut_dripstone",
        "tfg:stonecutter/create_polished_cut_dripstone_slab_half",
        "tfg:stonecutter/create_polished_cut_dripstone_stairs",
        "tfg:stonecutter/create_polished_cut_dripstone_wall",
        "tfg:stonecutter/create_small_dripstone_brick_slab_half",
        "tfg:stonecutter/create_small_dripstone_brick_wall",
        "tfg:stonecutter/create_small_dripstone_bricks"
      ],
      "recipe_output_examples": [
        "tfg:chisel/create_small_dripstone_bricks_to_create_small_dripstone_brick_stairs",
        "tfg:stonecutter/create_small_dripstone_brick_stairs",
        "tfg:stonecutter/create_small_dripstone_bricks_to_create_small_dripstone_brick_stairs"
      ],
      "model_parents": [
        "item/small_dripstone_brick_stairs",
        "block/small_dripstone_brick_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_dripstone_brick_stairs"
      ],
      "block_context": {
        "block_id": "create:small_dripstone_brick_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs"
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
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "create:small_dripstone_brick_wall",
      "namespace": "create",
      "display_name": "Small Travertine Brick Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/dripstone",
        "minecraft:walls",
        "tfg:stone_composition/sedimentary_carbonate_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 17
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 18,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_carbonate_half",
        "tfg:stonecutter/create_cut_dripstone",
        "tfg:stonecutter/create_cut_dripstone_brick_slab_half",
        "tfg:stonecutter/create_cut_dripstone_brick_stairs",
        "tfg:stonecutter/create_cut_dripstone_brick_wall",
        "tfg:stonecutter/create_cut_dripstone_bricks",
        "tfg:stonecutter/create_cut_dripstone_slab_half",
        "tfg:stonecutter/create_cut_dripstone_stairs",
        "tfg:stonecutter/create_cut_dripstone_wall",
        "tfg:stonecutter/create_dripstone_pillar",
        "tfg:stonecutter/create_layered_dripstone",
        "tfg:stonecutter/create_polished_cut_dripstone",
        "tfg:stonecutter/create_polished_cut_dripstone_slab_half",
        "tfg:stonecutter/create_polished_cut_dripstone_stairs",
        "tfg:stonecutter/create_polished_cut_dripstone_wall",
        "tfg:stonecutter/create_small_dripstone_brick_slab_half",
        "tfg:stonecutter/create_small_dripstone_brick_stairs",
        "tfg:stonecutter/create_small_dripstone_bricks"
      ],
      "recipe_output_examples": [
        "tfc:kjs/bemvo00um9p3oqugyrppzjje1",
        "tfg:stonecutter/create_small_dripstone_brick_wall",
        "tfg:stonecutting/create_small_dripstone_bricks_to_create_small_dripstone_brick_wall"
      ],
      "model_parents": [
        "item/small_dripstone_brick_wall",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_dripstone_brick_wall"
      ],
      "block_context": {
        "block_id": "create:small_dripstone_brick_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:walls"
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
        "form": {
          "value": "wall",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:walls"
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
      "id": "create:small_dripstone_bricks",
      "namespace": "create",
      "display_name": "Small Travertine Bricks",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/dripstone",
        "forge:stone_bricks",
        "tfc:rock/bricks",
        "tfc:rock/chiseled_bricks",
        "tfg:stone_composition/sedimentary_carbonate",
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
        "stonecutting": 20
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 46,
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
        "tfg:stonecutter/create_cut_dripstone",
        "tfg:stonecutter/create_cut_dripstone_brick_slab_half",
        "tfg:stonecutter/create_cut_dripstone_brick_stairs",
        "tfg:stonecutter/create_cut_dripstone_brick_wall",
        "tfg:stonecutter/create_cut_dripstone_bricks",
        "tfg:stonecutter/create_cut_dripstone_slab_half",
        "tfg:stonecutter/create_cut_dripstone_stairs",
        "tfg:stonecutter/create_cut_dripstone_wall",
        "tfg:stonecutter/create_dripstone_pillar",
        "tfg:stonecutter/create_layered_dripstone",
        "tfg:stonecutter/create_polished_cut_dripstone",
        "tfg:stonecutter/create_polished_cut_dripstone_slab_half",
        "tfg:stonecutter/create_polished_cut_dripstone_stairs",
        "tfg:stonecutter/create_polished_cut_dripstone_wall",
        "tfg:stonecutter/create_small_dripstone_brick_slab_half",
        "tfg:stonecutter/create_small_dripstone_brick_stairs",
        "tfg:stonecutter/create_small_dripstone_brick_wall",
        "tfg:stonecutter/create_small_dripstone_bricks_to_create_small_dripstone_brick_stairs",
        "tfg:stonecutting/create_small_dripstone_bricks_to_create_small_dripstone_brick_slab",
        "tfg:stonecutting/create_small_dripstone_bricks_to_create_small_dripstone_brick_wall"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/create_small_dripstone_bricks"
      ],
      "model_parents": [
        "item/small_dripstone_bricks",
        "block/small_dripstone_bricks",
        "block/cube_all"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_dripstone_bricks"
      ],
      "block_context": {
        "block_id": "create:small_dripstone_bricks",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
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
      "id": "create:small_granite_brick_slab",
      "namespace": "create",
      "display_name": "Small Chert Brick Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:slabs",
        "tfg:stone_composition/sedimentary_organic_half",
        "tfg:stone_types/chert_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 2
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_organic_half",
        "tfg:stonecutter/create_cut_granite_brick_slab_slab_to_slab",
        "tfg:stonecutter/create_cut_granite_slab_slab_to_slab",
        "tfg:stonecutter/create_polished_cut_granite_slab_slab_to_slab",
        "tfg:stonecutter/tfc_rock_bricks_chert_slab_slab_to_slab",
        "tfg:stonecutter/tfc_rock_smooth_chert_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/create_small_granite_brick_slab_half",
        "tfg:stonecutter/create_small_granite_brick_slab_slab_to_slab"
      ],
      "model_parents": [
        "item/small_granite_brick_slab",
        "block/small_granite_brick_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_granite_brick_slab"
      ],
      "block_context": {
        "block_id": "create:small_granite_brick_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs"
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
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "create:small_granite_brick_stairs",
      "namespace": "create",
      "display_name": "Small Chert Brick Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/granite",
        "minecraft:stairs",
        "tfg:stone_composition/sedimentary_organic",
        "tfg:stone_types/chert"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 26
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 27,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_organic",
        "tfg:stonecutter/create_cut_granite",
        "tfg:stonecutter/create_cut_granite_brick_slab_half",
        "tfg:stonecutter/create_cut_granite_brick_stairs",
        "tfg:stonecutter/create_cut_granite_brick_wall",
        "tfg:stonecutter/create_cut_granite_bricks",
        "tfg:stonecutter/create_cut_granite_slab_half",
        "tfg:stonecutter/create_cut_granite_stairs",
        "tfg:stonecutter/create_cut_granite_wall",
        "tfg:stonecutter/create_granite_pillar",
        "tfg:stonecutter/create_layered_granite",
        "tfg:stonecutter/create_polished_cut_granite",
        "tfg:stonecutter/create_polished_cut_granite_slab_half",
        "tfg:stonecutter/create_polished_cut_granite_stairs",
        "tfg:stonecutter/create_polished_cut_granite_wall",
        "tfg:stonecutter/create_small_granite_brick_slab_half",
        "tfg:stonecutter/create_small_granite_brick_wall",
        "tfg:stonecutter/create_small_granite_bricks",
        "tfg:stonecutter/tfc_rock_bricks_chert",
        "tfg:stonecutter/tfc_rock_bricks_chert_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_chert_stairs",
        "tfg:stonecutter/tfc_rock_bricks_chert_wall",
        "tfg:stonecutter/tfc_rock_chiseled_chert",
        "tfg:stonecutter/tfc_rock_smooth_chert",
        "tfg:stonecutter/tfc_rock_smooth_chert_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_chert_stairs",
        "tfg:stonecutter/tfc_rock_smooth_chert_wall"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/create_small_granite_brick_stairs"
      ],
      "model_parents": [
        "item/small_granite_brick_stairs",
        "block/small_granite_brick_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_granite_brick_stairs"
      ],
      "block_context": {
        "block_id": "create:small_granite_brick_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs"
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
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "create:small_granite_brick_wall",
      "namespace": "create",
      "display_name": "Small Chert Brick Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/granite",
        "minecraft:walls",
        "tfg:stone_composition/sedimentary_organic_half",
        "tfg:stone_types/chert"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 26
      },
      "recipe_production_by_type": {
        "stonecutting": 1,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 27,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_organic_half",
        "tfg:stonecutter/create_cut_granite",
        "tfg:stonecutter/create_cut_granite_brick_slab_half",
        "tfg:stonecutter/create_cut_granite_brick_stairs",
        "tfg:stonecutter/create_cut_granite_brick_wall",
        "tfg:stonecutter/create_cut_granite_bricks",
        "tfg:stonecutter/create_cut_granite_slab_half",
        "tfg:stonecutter/create_cut_granite_stairs",
        "tfg:stonecutter/create_cut_granite_wall",
        "tfg:stonecutter/create_granite_pillar",
        "tfg:stonecutter/create_layered_granite",
        "tfg:stonecutter/create_polished_cut_granite",
        "tfg:stonecutter/create_polished_cut_granite_slab_half",
        "tfg:stonecutter/create_polished_cut_granite_stairs",
        "tfg:stonecutter/create_polished_cut_granite_wall",
        "tfg:stonecutter/create_small_granite_brick_slab_half",
        "tfg:stonecutter/create_small_granite_brick_stairs",
        "tfg:stonecutter/create_small_granite_bricks",
        "tfg:stonecutter/tfc_rock_bricks_chert",
        "tfg:stonecutter/tfc_rock_bricks_chert_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_chert_stairs",
        "tfg:stonecutter/tfc_rock_bricks_chert_wall",
        "tfg:stonecutter/tfc_rock_chiseled_chert",
        "tfg:stonecutter/tfc_rock_smooth_chert",
        "tfg:stonecutter/tfc_rock_smooth_chert_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_chert_stairs",
        "tfg:stonecutter/tfc_rock_smooth_chert_wall"
      ],
      "recipe_output_examples": [
        "tfc:kjs/7a8gtpdzk5wrq7uqbwu0ul4ch",
        "tfg:stonecutter/create_small_granite_brick_wall"
      ],
      "model_parents": [
        "item/small_granite_brick_wall",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_granite_brick_wall"
      ],
      "block_context": {
        "block_id": "create:small_granite_brick_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:walls"
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
        "form": {
          "value": "wall",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:walls"
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
      "id": "create:small_granite_bricks",
      "namespace": "create",
      "display_name": "Small Chert Bricks",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/granite",
        "forge:stone_bricks",
        "tfc:rock/bricks",
        "tfc:rock/chiseled_bricks",
        "tfg:stone_composition/sedimentary_organic",
        "tfg:stone_types/chert",
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
        "stonecutting": 26
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 52,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/sealed_bricks",
        "greate:milling/integration/tfg/macerate_sedimentary_organic",
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
        "tfg:stonecutter/create_cut_granite",
        "tfg:stonecutter/create_cut_granite_brick_slab_half",
        "tfg:stonecutter/create_cut_granite_brick_stairs",
        "tfg:stonecutter/create_cut_granite_brick_wall",
        "tfg:stonecutter/create_cut_granite_bricks",
        "tfg:stonecutter/create_cut_granite_slab_half",
        "tfg:stonecutter/create_cut_granite_stairs",
        "tfg:stonecutter/create_cut_granite_wall",
        "tfg:stonecutter/create_granite_pillar",
        "tfg:stonecutter/create_layered_granite",
        "tfg:stonecutter/create_polished_cut_granite",
        "tfg:stonecutter/create_polished_cut_granite_slab_half",
        "tfg:stonecutter/create_polished_cut_granite_stairs",
        "tfg:stonecutter/create_polished_cut_granite_wall",
        "tfg:stonecutter/create_small_granite_brick_slab_half",
        "tfg:stonecutter/create_small_granite_brick_stairs",
        "tfg:stonecutter/create_small_granite_brick_wall",
        "tfg:stonecutter/tfc_rock_bricks_chert",
        "tfg:stonecutter/tfc_rock_bricks_chert_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_chert_stairs",
        "tfg:stonecutter/tfc_rock_bricks_chert_wall",
        "tfg:stonecutter/tfc_rock_chiseled_chert",
        "tfg:stonecutter/tfc_rock_smooth_chert",
        "tfg:stonecutter/tfc_rock_smooth_chert_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_chert_stairs",
        "tfg:stonecutter/tfc_rock_smooth_chert_wall"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/create_small_granite_bricks"
      ],
      "model_parents": [
        "item/small_granite_bricks",
        "block/small_granite_bricks",
        "block/cube_all"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_granite_bricks"
      ],
      "block_context": {
        "block_id": "create:small_granite_bricks",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
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
      "id": "create:small_limestone_brick_slab",
      "namespace": "create",
      "display_name": "Small Limestone Brick Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:slabs",
        "tfg:stone_composition/sedimentary_carbonate_half",
        "tfg:stone_types/limestone_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 2
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_carbonate_half",
        "tfg:stonecutter/create_cut_limestone_brick_slab_slab_to_slab",
        "tfg:stonecutter/create_cut_limestone_slab_slab_to_slab",
        "tfg:stonecutter/create_polished_cut_limestone_slab_slab_to_slab",
        "tfg:stonecutter/tfc_rock_bricks_limestone_slab_slab_to_slab",
        "tfg:stonecutter/tfc_rock_smooth_limestone_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/create_small_limestone_brick_slab_half",
        "tfg:stonecutter/create_small_limestone_brick_slab_slab_to_slab"
      ],
      "model_parents": [
        "item/small_limestone_brick_slab",
        "block/small_limestone_brick_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_limestone_brick_slab"
      ],
      "block_context": {
        "block_id": "create:small_limestone_brick_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs"
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
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "create:small_limestone_brick_stairs",
      "namespace": "create",
      "display_name": "Small Limestone Brick Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/limestone",
        "minecraft:stairs",
        "tfg:stone_composition/sedimentary_carbonate",
        "tfg:stone_types/limestone"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 27
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 28,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_carbonate",
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
        "tfg:stonecutter/create_limestone_pillar",
        "tfg:stonecutter/create_polished_cut_limestone",
        "tfg:stonecutter/create_polished_cut_limestone_slab_half",
        "tfg:stonecutter/create_polished_cut_limestone_stairs",
        "tfg:stonecutter/create_polished_cut_limestone_wall",
        "tfg:stonecutter/create_small_limestone_brick_slab_half",
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
        "tfg:stonecutter/create_small_limestone_brick_stairs"
      ],
      "model_parents": [
        "item/small_limestone_brick_stairs",
        "block/small_limestone_brick_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_limestone_brick_stairs"
      ],
      "block_context": {
        "block_id": "create:small_limestone_brick_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs"
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
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "create:small_limestone_brick_wall",
      "namespace": "create",
      "display_name": "Small Limestone Brick Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/limestone",
        "minecraft:walls",
        "tfg:stone_composition/sedimentary_carbonate_half",
        "tfg:stone_types/limestone"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 27
      },
      "recipe_production_by_type": {
        "stonecutting": 1,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 28,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_carbonate_half",
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
        "tfg:stonecutter/create_limestone_pillar",
        "tfg:stonecutter/create_polished_cut_limestone",
        "tfg:stonecutter/create_polished_cut_limestone_slab_half",
        "tfg:stonecutter/create_polished_cut_limestone_stairs",
        "tfg:stonecutter/create_polished_cut_limestone_wall",
        "tfg:stonecutter/create_small_limestone_brick_slab_half",
        "tfg:stonecutter/create_small_limestone_brick_stairs",
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
        "tfc:kjs/4kzfaltg5mvgij7f32p2gdpu4",
        "tfg:stonecutter/create_small_limestone_brick_wall"
      ],
      "model_parents": [
        "item/small_limestone_brick_wall",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_limestone_brick_wall"
      ],
      "block_context": {
        "block_id": "create:small_limestone_brick_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:walls"
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
        "form": {
          "value": "wall",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:walls"
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
      "id": "create:small_limestone_bricks",
      "namespace": "create",
      "display_name": "Small Limestone Bricks",
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
        "tfg:stonecutter/create_limestone",
        "tfg:stonecutter/create_limestone_pillar",
        "tfg:stonecutter/create_polished_cut_limestone",
        "tfg:stonecutter/create_polished_cut_limestone_slab_half",
        "tfg:stonecutter/create_polished_cut_limestone_stairs",
        "tfg:stonecutter/create_polished_cut_limestone_wall",
        "tfg:stonecutter/create_small_limestone_brick_slab_half",
        "tfg:stonecutter/create_small_limestone_brick_stairs",
        "tfg:stonecutter/create_small_limestone_brick_wall",
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
        "tfg:stonecutter/create_small_limestone_bricks"
      ],
      "model_parents": [
        "item/small_limestone_bricks",
        "block/small_limestone_bricks",
        "block/cube_all"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_limestone_bricks"
      ],
      "block_context": {
        "block_id": "create:small_limestone_bricks",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
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
      "id": "create:small_ochrum_brick_slab",
      "namespace": "create",
      "display_name": "Small Ochrum Brick Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/ochrum_half",
        "minecraft:slabs",
        "tfg:stone_composition/ochrum_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 3
      },
      "recipe_production_by_type": {
        "stonecutting": 3,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_ochrum_half",
        "tfg:stonecutter/create_cut_ochrum_brick_slab_slab_to_slab",
        "tfg:stonecutter/create_cut_ochrum_slab_slab_to_slab",
        "tfg:stonecutter/create_polished_cut_ochrum_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfg:chisel/create_small_ochrum_bricks_to_create_small_ochrum_brick_slab",
        "tfg:stonecutter/create_small_ochrum_brick_slab_half",
        "tfg:stonecutter/create_small_ochrum_brick_slab_slab_to_slab",
        "tfg:stonecutting/create_small_ochrum_bricks_to_create_small_ochrum_brick_slab"
      ],
      "model_parents": [
        "item/small_ochrum_brick_slab",
        "block/small_ochrum_brick_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_ochrum_brick_slab"
      ],
      "block_context": {
        "block_id": "create:small_ochrum_brick_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs"
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
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "create:small_ochrum_brick_stairs",
      "namespace": "create",
      "display_name": "Small Ochrum Brick Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/ochrum",
        "minecraft:stairs",
        "tfg:stone_composition/ochrum"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 18
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 19,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_ochrum",
        "tfg:stonecutter/create_cut_ochrum",
        "tfg:stonecutter/create_cut_ochrum_brick_slab_half",
        "tfg:stonecutter/create_cut_ochrum_brick_stairs",
        "tfg:stonecutter/create_cut_ochrum_brick_wall",
        "tfg:stonecutter/create_cut_ochrum_bricks",
        "tfg:stonecutter/create_cut_ochrum_slab_half",
        "tfg:stonecutter/create_cut_ochrum_stairs",
        "tfg:stonecutter/create_cut_ochrum_wall",
        "tfg:stonecutter/create_layered_ochrum",
        "tfg:stonecutter/create_ochrum",
        "tfg:stonecutter/create_ochrum_pillar",
        "tfg:stonecutter/create_polished_cut_ochrum",
        "tfg:stonecutter/create_polished_cut_ochrum_slab_half",
        "tfg:stonecutter/create_polished_cut_ochrum_stairs",
        "tfg:stonecutter/create_polished_cut_ochrum_wall",
        "tfg:stonecutter/create_small_ochrum_brick_slab_half",
        "tfg:stonecutter/create_small_ochrum_brick_wall",
        "tfg:stonecutter/create_small_ochrum_bricks"
      ],
      "recipe_output_examples": [
        "tfg:chisel/create_small_ochrum_bricks_to_create_small_ochrum_brick_stairs",
        "tfg:stonecutter/create_small_ochrum_brick_stairs",
        "tfg:stonecutter/create_small_ochrum_bricks_to_create_small_ochrum_brick_stairs"
      ],
      "model_parents": [
        "item/small_ochrum_brick_stairs",
        "block/small_ochrum_brick_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_ochrum_brick_stairs"
      ],
      "block_context": {
        "block_id": "create:small_ochrum_brick_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs"
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
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "create:small_ochrum_brick_wall",
      "namespace": "create",
      "display_name": "Small Ochrum Brick Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/ochrum",
        "minecraft:walls",
        "tfg:stone_composition/ochrum_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 18
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 19,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_ochrum_half",
        "tfg:stonecutter/create_cut_ochrum",
        "tfg:stonecutter/create_cut_ochrum_brick_slab_half",
        "tfg:stonecutter/create_cut_ochrum_brick_stairs",
        "tfg:stonecutter/create_cut_ochrum_brick_wall",
        "tfg:stonecutter/create_cut_ochrum_bricks",
        "tfg:stonecutter/create_cut_ochrum_slab_half",
        "tfg:stonecutter/create_cut_ochrum_stairs",
        "tfg:stonecutter/create_cut_ochrum_wall",
        "tfg:stonecutter/create_layered_ochrum",
        "tfg:stonecutter/create_ochrum",
        "tfg:stonecutter/create_ochrum_pillar",
        "tfg:stonecutter/create_polished_cut_ochrum",
        "tfg:stonecutter/create_polished_cut_ochrum_slab_half",
        "tfg:stonecutter/create_polished_cut_ochrum_stairs",
        "tfg:stonecutter/create_polished_cut_ochrum_wall",
        "tfg:stonecutter/create_small_ochrum_brick_slab_half",
        "tfg:stonecutter/create_small_ochrum_brick_stairs",
        "tfg:stonecutter/create_small_ochrum_bricks"
      ],
      "recipe_output_examples": [
        "tfc:kjs/27i1r91lr3ommt2miflmjq309",
        "tfg:stonecutter/create_small_ochrum_brick_wall",
        "tfg:stonecutting/create_small_ochrum_bricks_to_create_small_ochrum_brick_wall"
      ],
      "model_parents": [
        "item/small_ochrum_brick_wall",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_ochrum_brick_wall"
      ],
      "block_context": {
        "block_id": "create:small_ochrum_brick_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:walls"
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
        "form": {
          "value": "wall",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:walls"
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
      "id": "create:small_ochrum_bricks",
      "namespace": "create",
      "display_name": "Small Ochrum Bricks",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/ochrum",
        "forge:stone_bricks",
        "tfc:rock/bricks",
        "tfc:rock/chiseled_bricks",
        "tfg:stone_composition/ochrum",
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
        "stonecutting": 21
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 47,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/sealed_bricks",
        "greate:milling/integration/tfg/macerate_ochrum",
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
        "tfg:stonecutter/create_cut_ochrum",
        "tfg:stonecutter/create_cut_ochrum_brick_slab_half",
        "tfg:stonecutter/create_cut_ochrum_brick_stairs",
        "tfg:stonecutter/create_cut_ochrum_brick_wall",
        "tfg:stonecutter/create_cut_ochrum_bricks",
        "tfg:stonecutter/create_cut_ochrum_slab_half",
        "tfg:stonecutter/create_cut_ochrum_stairs",
        "tfg:stonecutter/create_cut_ochrum_wall",
        "tfg:stonecutter/create_layered_ochrum",
        "tfg:stonecutter/create_ochrum",
        "tfg:stonecutter/create_ochrum_pillar",
        "tfg:stonecutter/create_polished_cut_ochrum",
        "tfg:stonecutter/create_polished_cut_ochrum_slab_half",
        "tfg:stonecutter/create_polished_cut_ochrum_stairs",
        "tfg:stonecutter/create_polished_cut_ochrum_wall",
        "tfg:stonecutter/create_small_ochrum_brick_slab_half",
        "tfg:stonecutter/create_small_ochrum_brick_stairs",
        "tfg:stonecutter/create_small_ochrum_brick_wall",
        "tfg:stonecutter/create_small_ochrum_bricks_to_create_small_ochrum_brick_stairs",
        "tfg:stonecutting/create_small_ochrum_bricks_to_create_small_ochrum_brick_slab",
        "tfg:stonecutting/create_small_ochrum_bricks_to_create_small_ochrum_brick_wall"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/create_small_ochrum_bricks"
      ],
      "model_parents": [
        "item/small_ochrum_bricks",
        "block/small_ochrum_bricks",
        "block/cube_all"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_ochrum_bricks"
      ],
      "block_context": {
        "block_id": "create:small_ochrum_bricks",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
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
      "id": "create:small_rose_quartz_tiles",
      "namespace": "create",
      "display_name": "Small Rose Quartz Tiles",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
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
        "item/small_rose_quartz_tiles",
        "block/small_rose_quartz_tiles",
        "block/cube_all"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_rose_quartz_tiles"
      ],
      "block_context": {
        "block_id": "create:small_rose_quartz_tiles",
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
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "create:small_scorchia_brick_slab",
      "namespace": "create",
      "display_name": "Small Scorchia Brick Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/scorchia_half",
        "minecraft:slabs",
        "tfg:stone_composition/igneous_mafic_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 3
      },
      "recipe_production_by_type": {
        "stonecutting": 3,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_mafic_half",
        "tfg:stonecutter/create_cut_scorchia_brick_slab_slab_to_slab",
        "tfg:stonecutter/create_cut_scorchia_slab_slab_to_slab",
        "tfg:stonecutter/create_polished_cut_scorchia_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfg:chisel/create_small_scorchia_bricks_to_create_small_scorchia_brick_slab",
        "tfg:stonecutter/create_small_scorchia_brick_slab_half",
        "tfg:stonecutter/create_small_scorchia_brick_slab_slab_to_slab",
        "tfg:stonecutting/create_small_scorchia_bricks_to_create_small_scorchia_brick_slab"
      ],
      "model_parents": [
        "item/small_scorchia_brick_slab",
        "block/small_scorchia_brick_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_scorchia_brick_slab"
      ],
      "block_context": {
        "block_id": "create:small_scorchia_brick_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs"
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
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "create:small_scorchia_brick_stairs",
      "namespace": "create",
      "display_name": "Small Scorchia Brick Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/scorchia",
        "minecraft:stairs",
        "tfg:stone_composition/igneous_mafic"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 18
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 19,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_mafic",
        "tfg:stonecutter/create_cut_scorchia",
        "tfg:stonecutter/create_cut_scorchia_brick_slab_half",
        "tfg:stonecutter/create_cut_scorchia_brick_stairs",
        "tfg:stonecutter/create_cut_scorchia_brick_wall",
        "tfg:stonecutter/create_cut_scorchia_bricks",
        "tfg:stonecutter/create_cut_scorchia_slab_half",
        "tfg:stonecutter/create_cut_scorchia_stairs",
        "tfg:stonecutter/create_cut_scorchia_wall",
        "tfg:stonecutter/create_layered_scorchia",
        "tfg:stonecutter/create_polished_cut_scorchia",
        "tfg:stonecutter/create_polished_cut_scorchia_slab_half",
        "tfg:stonecutter/create_polished_cut_scorchia_stairs",
        "tfg:stonecutter/create_polished_cut_scorchia_wall",
        "tfg:stonecutter/create_scorchia",
        "tfg:stonecutter/create_scorchia_pillar",
        "tfg:stonecutter/create_small_scorchia_brick_slab_half",
        "tfg:stonecutter/create_small_scorchia_brick_wall",
        "tfg:stonecutter/create_small_scorchia_bricks"
      ],
      "recipe_output_examples": [
        "tfg:chisel/create_small_scorchia_bricks_to_create_small_scorchia_brick_stairs",
        "tfg:stonecutter/create_small_scorchia_brick_stairs",
        "tfg:stonecutter/create_small_scorchia_bricks_to_create_small_scorchia_brick_stairs"
      ],
      "model_parents": [
        "item/small_scorchia_brick_stairs",
        "block/small_scorchia_brick_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_scorchia_brick_stairs"
      ],
      "block_context": {
        "block_id": "create:small_scorchia_brick_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs"
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
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
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
        "processing_in": {
          "values": [
            "greate:milling",
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