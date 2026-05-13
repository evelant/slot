# Items to classify
{
  "items": [
    {
      "id": "create:small_scorchia_brick_wall",
      "namespace": "create",
      "display_name": "Small Scorchia Brick Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/scorchia",
        "minecraft:walls",
        "tfg:stone_composition/igneous_mafic_half"
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
        "greate:milling/integration/tfg/macerate_igneous_mafic_half",
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
        "tfg:stonecutter/create_small_scorchia_brick_stairs",
        "tfg:stonecutter/create_small_scorchia_bricks"
      ],
      "recipe_output_examples": [
        "tfc:kjs/43s8v5j2s41ygm7c0pldbq87v",
        "tfg:stonecutter/create_small_scorchia_brick_wall",
        "tfg:stonecutting/create_small_scorchia_bricks_to_create_small_scorchia_brick_wall"
      ],
      "model_parents": [
        "item/small_scorchia_brick_wall",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_scorchia_brick_wall"
      ],
      "block_context": {
        "block_id": "create:small_scorchia_brick_wall",
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
      "id": "create:small_scorchia_bricks",
      "namespace": "create",
      "display_name": "Small Scorchia Bricks",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/scorchia",
        "forge:stone_bricks",
        "tfc:rock/bricks",
        "tfc:rock/chiseled_bricks",
        "tfg:stone_composition/igneous_mafic",
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
        "greate:milling/integration/tfg/macerate_igneous_mafic",
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
        "tfg:stonecutter/create_small_scorchia_brick_stairs",
        "tfg:stonecutter/create_small_scorchia_brick_wall",
        "tfg:stonecutter/create_small_scorchia_bricks_to_create_small_scorchia_brick_stairs",
        "tfg:stonecutting/create_small_scorchia_bricks_to_create_small_scorchia_brick_slab",
        "tfg:stonecutting/create_small_scorchia_bricks_to_create_small_scorchia_brick_wall"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/create_small_scorchia_bricks"
      ],
      "model_parents": [
        "item/small_scorchia_bricks",
        "block/small_scorchia_bricks",
        "block/cube_all"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_scorchia_bricks"
      ],
      "block_context": {
        "block_id": "create:small_scorchia_bricks",
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
      "id": "create:small_scoria_brick_slab",
      "namespace": "create",
      "display_name": "Small Scoria Brick Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/scoria_half",
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
        "tfg:stonecutter/create_cut_scoria_brick_slab_slab_to_slab",
        "tfg:stonecutter/create_cut_scoria_slab_slab_to_slab",
        "tfg:stonecutter/create_polished_cut_scoria_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfg:chisel/create_small_scoria_bricks_to_create_small_scoria_brick_slab",
        "tfg:stonecutter/create_small_scoria_brick_slab_half",
        "tfg:stonecutter/create_small_scoria_brick_slab_slab_to_slab",
        "tfg:stonecutting/create_small_scoria_bricks_to_create_small_scoria_brick_slab"
      ],
      "model_parents": [
        "item/small_scoria_brick_slab",
        "block/small_scoria_brick_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_scoria_brick_slab"
      ],
      "block_context": {
        "block_id": "create:small_scoria_brick_slab",
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
      "id": "create:small_scoria_brick_stairs",
      "namespace": "create",
      "display_name": "Small Scoria Brick Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/scoria",
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
        "tfg:stonecutter/create_cut_scoria",
        "tfg:stonecutter/create_cut_scoria_brick_slab_half",
        "tfg:stonecutter/create_cut_scoria_brick_stairs",
        "tfg:stonecutter/create_cut_scoria_brick_wall",
        "tfg:stonecutter/create_cut_scoria_bricks",
        "tfg:stonecutter/create_cut_scoria_slab_half",
        "tfg:stonecutter/create_cut_scoria_stairs",
        "tfg:stonecutter/create_cut_scoria_wall",
        "tfg:stonecutter/create_layered_scoria",
        "tfg:stonecutter/create_polished_cut_scoria",
        "tfg:stonecutter/create_polished_cut_scoria_slab_half",
        "tfg:stonecutter/create_polished_cut_scoria_stairs",
        "tfg:stonecutter/create_polished_cut_scoria_wall",
        "tfg:stonecutter/create_scoria",
        "tfg:stonecutter/create_scoria_pillar",
        "tfg:stonecutter/create_small_scoria_brick_slab_half",
        "tfg:stonecutter/create_small_scoria_brick_wall",
        "tfg:stonecutter/create_small_scoria_bricks"
      ],
      "recipe_output_examples": [
        "tfg:chisel/create_small_scoria_bricks_to_create_small_scoria_brick_stairs",
        "tfg:stonecutter/create_small_scoria_brick_stairs",
        "tfg:stonecutter/create_small_scoria_bricks_to_create_small_scoria_brick_stairs"
      ],
      "model_parents": [
        "item/small_scoria_brick_stairs",
        "block/small_scoria_brick_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_scoria_brick_stairs"
      ],
      "block_context": {
        "block_id": "create:small_scoria_brick_stairs",
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
      "id": "create:small_scoria_brick_wall",
      "namespace": "create",
      "display_name": "Small Scoria Brick Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/scoria",
        "minecraft:walls",
        "tfg:stone_composition/igneous_mafic_half"
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
        "greate:milling/integration/tfg/macerate_igneous_mafic_half",
        "tfg:stonecutter/create_cut_scoria",
        "tfg:stonecutter/create_cut_scoria_brick_slab_half",
        "tfg:stonecutter/create_cut_scoria_brick_stairs",
        "tfg:stonecutter/create_cut_scoria_brick_wall",
        "tfg:stonecutter/create_cut_scoria_bricks",
        "tfg:stonecutter/create_cut_scoria_slab_half",
        "tfg:stonecutter/create_cut_scoria_stairs",
        "tfg:stonecutter/create_cut_scoria_wall",
        "tfg:stonecutter/create_layered_scoria",
        "tfg:stonecutter/create_polished_cut_scoria",
        "tfg:stonecutter/create_polished_cut_scoria_slab_half",
        "tfg:stonecutter/create_polished_cut_scoria_stairs",
        "tfg:stonecutter/create_polished_cut_scoria_wall",
        "tfg:stonecutter/create_scoria",
        "tfg:stonecutter/create_scoria_pillar",
        "tfg:stonecutter/create_small_scoria_brick_slab_half",
        "tfg:stonecutter/create_small_scoria_brick_stairs",
        "tfg:stonecutter/create_small_scoria_bricks"
      ],
      "recipe_output_examples": [
        "tfc:kjs/12oo68lgip22so9pxg898wx1h",
        "tfg:stonecutter/create_small_scoria_brick_wall",
        "tfg:stonecutting/create_small_scoria_bricks_to_create_small_scoria_brick_wall"
      ],
      "model_parents": [
        "item/small_scoria_brick_wall",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_scoria_brick_wall"
      ],
      "block_context": {
        "block_id": "create:small_scoria_brick_wall",
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
      "id": "create:small_scoria_bricks",
      "namespace": "create",
      "display_name": "Small Scoria Bricks",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/scoria",
        "forge:stone_bricks",
        "tfc:rock/bricks",
        "tfc:rock/chiseled_bricks",
        "tfg:stone_composition/igneous_mafic",
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
        "greate:milling/integration/tfg/macerate_igneous_mafic",
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
        "tfg:stonecutter/create_cut_scoria",
        "tfg:stonecutter/create_cut_scoria_brick_slab_half",
        "tfg:stonecutter/create_cut_scoria_brick_stairs",
        "tfg:stonecutter/create_cut_scoria_brick_wall",
        "tfg:stonecutter/create_cut_scoria_bricks",
        "tfg:stonecutter/create_cut_scoria_slab_half",
        "tfg:stonecutter/create_cut_scoria_stairs",
        "tfg:stonecutter/create_cut_scoria_wall",
        "tfg:stonecutter/create_layered_scoria",
        "tfg:stonecutter/create_polished_cut_scoria",
        "tfg:stonecutter/create_polished_cut_scoria_slab_half",
        "tfg:stonecutter/create_polished_cut_scoria_stairs",
        "tfg:stonecutter/create_polished_cut_scoria_wall",
        "tfg:stonecutter/create_scoria",
        "tfg:stonecutter/create_scoria_pillar",
        "tfg:stonecutter/create_small_scoria_brick_slab_half",
        "tfg:stonecutter/create_small_scoria_brick_stairs",
        "tfg:stonecutter/create_small_scoria_brick_wall",
        "tfg:stonecutter/create_small_scoria_bricks_to_create_small_scoria_brick_stairs",
        "tfg:stonecutting/create_small_scoria_bricks_to_create_small_scoria_brick_slab",
        "tfg:stonecutting/create_small_scoria_bricks_to_create_small_scoria_brick_wall"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/create_small_scoria_bricks"
      ],
      "model_parents": [
        "item/small_scoria_bricks",
        "block/small_scoria_bricks",
        "block/cube_all"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_scoria_bricks"
      ],
      "block_context": {
        "block_id": "create:small_scoria_bricks",
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
      "id": "create:small_tuff_brick_slab",
      "namespace": "create",
      "display_name": "Small Tuff Brick Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/tuff_half",
        "minecraft:slabs",
        "tfg:stone_composition/igneous_felsic_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 4
      },
      "recipe_production_by_type": {
        "stonecutting": 3,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_felsic_half",
        "tfg:stonecutter/create_cut_tuff_brick_slab_slab_to_slab",
        "tfg:stonecutter/create_cut_tuff_slab_slab_to_slab",
        "tfg:stonecutter/create_polished_cut_tuff_slab_slab_to_slab",
        "tfg:stonecutter/tfg_rock_bricks_tuff_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfg:chisel/create_small_tuff_bricks_to_create_small_tuff_brick_slab",
        "tfg:stonecutter/create_small_tuff_brick_slab_half",
        "tfg:stonecutter/create_small_tuff_brick_slab_slab_to_slab",
        "tfg:stonecutting/create_small_tuff_bricks_to_create_small_tuff_brick_slab"
      ],
      "model_parents": [
        "item/small_tuff_brick_slab",
        "block/small_tuff_brick_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_tuff_brick_slab"
      ],
      "block_context": {
        "block_id": "create:small_tuff_brick_slab",
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
      "id": "create:small_tuff_brick_stairs",
      "namespace": "create",
      "display_name": "Small Tuff Brick Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/tuff",
        "minecraft:stairs",
        "tfg:stone_composition/igneous_felsic"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 24
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 25,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_felsic",
        "tfg:stonecutter/create_cut_tuff",
        "tfg:stonecutter/create_cut_tuff_brick_slab_half",
        "tfg:stonecutter/create_cut_tuff_brick_stairs",
        "tfg:stonecutter/create_cut_tuff_brick_wall",
        "tfg:stonecutter/create_cut_tuff_bricks",
        "tfg:stonecutter/create_cut_tuff_slab_half",
        "tfg:stonecutter/create_cut_tuff_stairs",
        "tfg:stonecutter/create_cut_tuff_wall",
        "tfg:stonecutter/create_layered_tuff",
        "tfg:stonecutter/create_polished_cut_tuff",
        "tfg:stonecutter/create_polished_cut_tuff_slab_half",
        "tfg:stonecutter/create_polished_cut_tuff_stairs",
        "tfg:stonecutter/create_polished_cut_tuff_wall",
        "tfg:stonecutter/create_small_tuff_brick_slab_half",
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
        "tfg:chisel/create_small_tuff_bricks_to_create_small_tuff_brick_stairs",
        "tfg:stonecutter/create_small_tuff_brick_stairs",
        "tfg:stonecutter/create_small_tuff_bricks_to_create_small_tuff_brick_stairs"
      ],
      "model_parents": [
        "item/small_tuff_brick_stairs",
        "block/small_tuff_brick_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_tuff_brick_stairs"
      ],
      "block_context": {
        "block_id": "create:small_tuff_brick_stairs",
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
      "id": "create:small_tuff_brick_wall",
      "namespace": "create",
      "display_name": "Small Tuff Brick Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/tuff",
        "minecraft:walls",
        "tfg:stone_composition/igneous_felsic_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 24
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 25,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_felsic_half",
        "tfg:stonecutter/create_cut_tuff",
        "tfg:stonecutter/create_cut_tuff_brick_slab_half",
        "tfg:stonecutter/create_cut_tuff_brick_stairs",
        "tfg:stonecutter/create_cut_tuff_brick_wall",
        "tfg:stonecutter/create_cut_tuff_bricks",
        "tfg:stonecutter/create_cut_tuff_slab_half",
        "tfg:stonecutter/create_cut_tuff_stairs",
        "tfg:stonecutter/create_cut_tuff_wall",
        "tfg:stonecutter/create_layered_tuff",
        "tfg:stonecutter/create_polished_cut_tuff",
        "tfg:stonecutter/create_polished_cut_tuff_slab_half",
        "tfg:stonecutter/create_polished_cut_tuff_stairs",
        "tfg:stonecutter/create_polished_cut_tuff_wall",
        "tfg:stonecutter/create_small_tuff_brick_slab_half",
        "tfg:stonecutter/create_small_tuff_brick_stairs",
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
        "tfc:kjs/cdkqhapbtizk5lu2piylcmtlh",
        "tfg:stonecutter/create_small_tuff_brick_wall",
        "tfg:stonecutting/create_small_tuff_bricks_to_create_small_tuff_brick_wall"
      ],
      "model_parents": [
        "item/small_tuff_brick_wall",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_tuff_brick_wall"
      ],
      "block_context": {
        "block_id": "create:small_tuff_brick_wall",
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
      "id": "create:small_tuff_bricks",
      "namespace": "create",
      "display_name": "Small Tuff Bricks",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/tuff",
        "forge:stone_bricks",
        "tfc:rock/bricks",
        "tfc:rock/chiseled_bricks",
        "tfg:stone_composition/igneous_felsic",
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
        "greate:milling/integration/tfg/macerate_igneous_felsic",
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
        "tfg:stonecutter/create_cut_tuff",
        "tfg:stonecutter/create_cut_tuff_brick_slab_half",
        "tfg:stonecutter/create_cut_tuff_brick_stairs",
        "tfg:stonecutter/create_cut_tuff_brick_wall",
        "tfg:stonecutter/create_cut_tuff_bricks",
        "tfg:stonecutter/create_cut_tuff_slab_half",
        "tfg:stonecutter/create_cut_tuff_stairs",
        "tfg:stonecutter/create_cut_tuff_wall",
        "tfg:stonecutter/create_layered_tuff",
        "tfg:stonecutter/create_polished_cut_tuff",
        "tfg:stonecutter/create_polished_cut_tuff_slab_half",
        "tfg:stonecutter/create_polished_cut_tuff_stairs",
        "tfg:stonecutter/create_polished_cut_tuff_wall",
        "tfg:stonecutter/create_small_tuff_brick_slab_half",
        "tfg:stonecutter/create_small_tuff_brick_stairs",
        "tfg:stonecutter/create_small_tuff_brick_wall",
        "tfg:stonecutter/create_small_tuff_bricks_to_create_small_tuff_brick_stairs",
        "tfg:stonecutter/create_tuff_pillar",
        "tfg:stonecutter/minecraft_tuff",
        "tfg:stonecutter/tfg_rock_bricks_tuff",
        "tfg:stonecutter/tfg_rock_bricks_tuff_slab_half",
        "tfg:stonecutter/tfg_rock_bricks_tuff_stairs",
        "tfg:stonecutter/tfg_rock_bricks_tuff_wall",
        "tfg:stonecutter/tfg_rock_chiseled_tuff",
        "tfg:stonecutter/tfg_rock_chiseled_tuff_bricks",
        "tfg:stonecutting/create_small_tuff_bricks_to_create_small_tuff_brick_slab",
        "tfg:stonecutting/create_small_tuff_bricks_to_create_small_tuff_brick_wall"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/create_small_tuff_bricks"
      ],
      "model_parents": [
        "item/small_tuff_bricks",
        "block/small_tuff_bricks",
        "block/cube_all"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_tuff_bricks"
      ],
      "block_context": {
        "block_id": "create:small_tuff_bricks",
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
      "id": "create:small_veridium_brick_slab",
      "namespace": "create",
      "display_name": "Small Veridium Brick Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/veridium_half",
        "minecraft:slabs",
        "tfg:stone_composition/veridium_half"
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
        "greate:milling/integration/tfg/macerate_veridium_half",
        "tfg:stonecutter/create_cut_veridium_brick_slab_slab_to_slab",
        "tfg:stonecutter/create_cut_veridium_slab_slab_to_slab",
        "tfg:stonecutter/create_polished_cut_veridium_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfg:chisel/create_small_veridium_bricks_to_create_small_veridium_brick_slab",
        "tfg:stonecutter/create_small_veridium_brick_slab_half",
        "tfg:stonecutter/create_small_veridium_brick_slab_slab_to_slab",
        "tfg:stonecutting/create_small_veridium_bricks_to_create_small_veridium_brick_slab"
      ],
      "model_parents": [
        "item/small_veridium_brick_slab",
        "block/small_veridium_brick_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_veridium_brick_slab"
      ],
      "block_context": {
        "block_id": "create:small_veridium_brick_slab",
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
      "id": "create:small_veridium_brick_stairs",
      "namespace": "create",
      "display_name": "Small Veridium Brick Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/veridium",
        "minecraft:stairs",
        "tfg:stone_composition/veridium"
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
        "greate:milling/integration/tfg/macerate_veridium",
        "tfg:stonecutter/create_cut_veridium",
        "tfg:stonecutter/create_cut_veridium_brick_slab_half",
        "tfg:stonecutter/create_cut_veridium_brick_stairs",
        "tfg:stonecutter/create_cut_veridium_brick_wall",
        "tfg:stonecutter/create_cut_veridium_bricks",
        "tfg:stonecutter/create_cut_veridium_slab_half",
        "tfg:stonecutter/create_cut_veridium_stairs",
        "tfg:stonecutter/create_cut_veridium_wall",
        "tfg:stonecutter/create_layered_veridium",
        "tfg:stonecutter/create_polished_cut_veridium",
        "tfg:stonecutter/create_polished_cut_veridium_slab_half",
        "tfg:stonecutter/create_polished_cut_veridium_stairs",
        "tfg:stonecutter/create_polished_cut_veridium_wall",
        "tfg:stonecutter/create_small_veridium_brick_slab_half",
        "tfg:stonecutter/create_small_veridium_brick_wall",
        "tfg:stonecutter/create_small_veridium_bricks",
        "tfg:stonecutter/create_veridium",
        "tfg:stonecutter/create_veridium_pillar"
      ],
      "recipe_output_examples": [
        "tfg:chisel/create_small_veridium_bricks_to_create_small_veridium_brick_stairs",
        "tfg:stonecutter/create_small_veridium_brick_stairs",
        "tfg:stonecutter/create_small_veridium_bricks_to_create_small_veridium_brick_stairs"
      ],
      "model_parents": [
        "item/small_veridium_brick_stairs",
        "block/small_veridium_brick_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_veridium_brick_stairs"
      ],
      "block_context": {
        "block_id": "create:small_veridium_brick_stairs",
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
      "id": "create:small_veridium_brick_wall",
      "namespace": "create",
      "display_name": "Small Veridium Brick Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/veridium",
        "minecraft:walls",
        "tfg:stone_composition/veridium_half"
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
        "greate:milling/integration/tfg/macerate_veridium_half",
        "tfg:stonecutter/create_cut_veridium",
        "tfg:stonecutter/create_cut_veridium_brick_slab_half",
        "tfg:stonecutter/create_cut_veridium_brick_stairs",
        "tfg:stonecutter/create_cut_veridium_brick_wall",
        "tfg:stonecutter/create_cut_veridium_bricks",
        "tfg:stonecutter/create_cut_veridium_slab_half",
        "tfg:stonecutter/create_cut_veridium_stairs",
        "tfg:stonecutter/create_cut_veridium_wall",
        "tfg:stonecutter/create_layered_veridium",
        "tfg:stonecutter/create_polished_cut_veridium",
        "tfg:stonecutter/create_polished_cut_veridium_slab_half",
        "tfg:stonecutter/create_polished_cut_veridium_stairs",
        "tfg:stonecutter/create_polished_cut_veridium_wall",
        "tfg:stonecutter/create_small_veridium_brick_slab_half",
        "tfg:stonecutter/create_small_veridium_brick_stairs",
        "tfg:stonecutter/create_small_veridium_bricks",
        "tfg:stonecutter/create_veridium",
        "tfg:stonecutter/create_veridium_pillar"
      ],
      "recipe_output_examples": [
        "tfc:kjs/690cmbzp2grnfv7pnrlfcddku",
        "tfg:stonecutter/create_small_veridium_brick_wall",
        "tfg:stonecutting/create_small_veridium_bricks_to_create_small_veridium_brick_wall"
      ],
      "model_parents": [
        "item/small_veridium_brick_wall",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_veridium_brick_wall"
      ],
      "block_context": {
        "block_id": "create:small_veridium_brick_wall",
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
      "id": "create:small_veridium_bricks",
      "namespace": "create",
      "display_name": "Small Veridium Bricks",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/veridium",
        "forge:stone_bricks",
        "tfc:rock/bricks",
        "tfc:rock/chiseled_bricks",
        "tfg:stone_composition/veridium",
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
        "greate:milling/integration/tfg/macerate_veridium",
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
        "tfg:stonecutter/create_cut_veridium",
        "tfg:stonecutter/create_cut_veridium_brick_slab_half",
        "tfg:stonecutter/create_cut_veridium_brick_stairs",
        "tfg:stonecutter/create_cut_veridium_brick_wall",
        "tfg:stonecutter/create_cut_veridium_bricks",
        "tfg:stonecutter/create_cut_veridium_slab_half",
        "tfg:stonecutter/create_cut_veridium_stairs",
        "tfg:stonecutter/create_cut_veridium_wall",
        "tfg:stonecutter/create_layered_veridium",
        "tfg:stonecutter/create_polished_cut_veridium",
        "tfg:stonecutter/create_polished_cut_veridium_slab_half",
        "tfg:stonecutter/create_polished_cut_veridium_stairs",
        "tfg:stonecutter/create_polished_cut_veridium_wall",
        "tfg:stonecutter/create_small_veridium_brick_slab_half",
        "tfg:stonecutter/create_small_veridium_brick_stairs",
        "tfg:stonecutter/create_small_veridium_brick_wall",
        "tfg:stonecutter/create_small_veridium_bricks_to_create_small_veridium_brick_stairs",
        "tfg:stonecutter/create_veridium",
        "tfg:stonecutter/create_veridium_pillar",
        "tfg:stonecutting/create_small_veridium_bricks_to_create_small_veridium_brick_slab",
        "tfg:stonecutting/create_small_veridium_bricks_to_create_small_veridium_brick_wall"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/create_small_veridium_bricks"
      ],
      "model_parents": [
        "item/small_veridium_bricks",
        "block/small_veridium_bricks",
        "block/cube_all"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/small_veridium_bricks"
      ],
      "block_context": {
        "block_id": "create:small_veridium_bricks",
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
      "id": "create:smart_chute",
      "namespace": "create",
      "display_name": "Smart Chute",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "create:crafting/kinetics/smart_chute"
      ],
      "model_parents": [
        "item/smart_chute",
        "block/smart_chute/block",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/smart_chute"
      ],
      "block_context": {
        "block_id": "create:smart_chute",
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
      "id": "create:smart_fluid_pipe",
      "namespace": "create",
      "display_name": "Smart Fluid Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "create:crafting/kinetics/smart_fluid_pipe"
      ],
      "model_parents": [
        "item/smart_fluid_pipe",
        "block/smart_fluid_pipe/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/smart_fluid_pipe"
      ],
      "block_context": {
        "block_id": "create:smart_fluid_pipe",
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
      "id": "create:speedometer",
      "namespace": "create",
      "display_name": "Speedometer",
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
        "crafting_shapeless": 2
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "create:crafting/kinetics/stressometer_from_conversion",
        "create_connected:crafting/kinetics/centrifugal_clutch"
      ],
      "recipe_output_examples": [
        "create:crafting/kinetics/speedometer",
        "create:crafting/kinetics/speedometer_from_conversion"
      ],
      "model_parents": [
        "item/speedometer",
        "block/gauge/speedometer/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/speedometer"
      ],
      "block_context": {
        "block_id": "create:speedometer",
        "block_tags": [
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
      "id": "create:spout",
      "namespace": "create",
      "display_name": "Spout",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_spout",
        "tfg:shaped/toms_favourite_block"
      ],
      "recipe_output_examples": [
        "tfg:create/shaped/spout"
      ],
      "model_parents": [
        "item/spout",
        "block/spout/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/spout"
      ],
      "block_context": {
        "block_id": "create:spout",
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
      "document_context": [
        {
          "kind": "advancement",
          "id": "create:spout",
          "label": "Sploosh",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Sploosh"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Watch a fluid-containing item be filled by a Spout"
            }
          ]
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
            "kubejs:shaped"
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
      "id": "create:spruce_window",
      "namespace": "create",
      "display_name": "Cypress Window",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:cutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:cutting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/tfg/create/spruce_window_pane",
        "tfg:create/shapeless/spruce_window_pane"
      ],
      "recipe_output_examples": [
        "tfg:create/shaped/spruce_window"
      ],
      "model_parents": [
        "item/spruce_window",
        "block/spruce_window",
        "block/cube_column"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/spruce_window"
      ],
      "block_context": {
        "block_id": "create:spruce_window",
        "block_tags": [
          "framedblocks:frameable",
          "minecraft:impermeable"
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
        "material_family": {
          "value": "wood_spruce",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix spruce_"
        },
        "form": {
          "value": "pane",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _window"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:cutting"
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
      "id": "create:spruce_window_pane",
      "namespace": "create",
      "display_name": "Cypress Window Pane",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:glass_panes"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped",
        "tfc:advanced_shaped_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 46,
        "greate:milling": 1,
        "kubejs:shaped": 1,
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "greate:cutting": 1
      },
      "recipe_ingredient_count": 49,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_glass_pane",
        "gtceu:shaped/basic_terminal",
        "gtceu:shaped/redstone_lamp",
        "gtceu:shaped/solar_panel_ulv",
        "gtceu:shaped/steam_boiler_solar_steel",
        "mcw_tfc_aio:furniture/acacia_furniture/acacia_glass_table",
        "mcw_tfc_aio:furniture/acacia_furniture/stripped_acacia_glass_table",
        "mcw_tfc_aio:furniture/ash_furniture/ash_glass_table",
        "mcw_tfc_aio:furniture/ash_furniture/stripped_ash_glass_table",
        "mcw_tfc_aio:furniture/aspen_furniture/aspen_glass_table",
        "mcw_tfc_aio:furniture/aspen_furniture/stripped_aspen_glass_table",
        "mcw_tfc_aio:furniture/birch_furniture/birch_glass_table",
        "mcw_tfc_aio:furniture/birch_furniture/stripped_birch_glass_table",
        "mcw_tfc_aio:furniture/blackwood_furniture/blackwood_glass_table",
        "mcw_tfc_aio:furniture/blackwood_furniture/stripped_blackwood_glass_table",
        "mcw_tfc_aio:furniture/chestnut_furniture/chestnut_glass_table",
        "mcw_tfc_aio:furniture/chestnut_furniture/stripped_chestnut_glass_table",
        "mcw_tfc_aio:furniture/douglas_fir_furniture/douglas_fir_glass_table",
        "mcw_tfc_aio:furniture/douglas_fir_furniture/stripped_douglas_fir_glass_table",
        "mcw_tfc_aio:furniture/hickory_furniture/hickory_glass_table",
        "mcw_tfc_aio:furniture/hickory_furniture/stripped_hickory_glass_table",
        "mcw_tfc_aio:furniture/kapok_furniture/kapok_glass_table",
        "mcw_tfc_aio:furniture/kapok_furniture/stripped_kapok_glass_table",
        "mcw_tfc_aio:furniture/mangrove_furniture/mangrove_glass_table",
        "mcw_tfc_aio:furniture/mangrove_furniture/stripped_mangrove_glass_table",
        "mcw_tfc_aio:furniture/maple_furniture/maple_glass_table",
        "mcw_tfc_aio:furniture/maple_furniture/stripped_maple_glass_table",
        "mcw_tfc_aio:furniture/oak_furniture/oak_glass_table",
        "mcw_tfc_aio:furniture/oak_furniture/stripped_oak_glass_table",
        "mcw_tfc_aio:furniture/palm_furniture/palm_glass_table",
        "mcw_tfc_aio:furniture/palm_furniture/stripped_palm_glass_table",
        "mcw_tfc_aio:furniture/pine_furniture/pine_glass_table",
        "mcw_tfc_aio:furniture/pine_furniture/stripped_pine_glass_table",
        "mcw_tfc_aio:furniture/rosewood_furniture/rosewood_glass_table",
        "mcw_tfc_aio:furniture/rosewood_furniture/stripped_rosewood_glass_table",
        "mcw_tfc_aio:furniture/sequoia_furniture/sequoia_glass_table",
        "mcw_tfc_aio:furniture/sequoia_furniture/stripped_sequoia_glass_table",
        "mcw_tfc_aio:furniture/spruce_furniture/spruce_glass_table",
        "mcw_tfc_aio:furniture/spruce_furniture/stripped_spruce_glass_table",
        "mcw_tfc_aio:furniture/sycamore_furniture/stripped_sycamore_glass_table",
        "mcw_tfc_aio:furniture/sycamore_furniture/sycamore_glass_table",
        "mcw_tfc_aio:furniture/white_cedar_furniture/stripped_white_cedar_glass_table",
        "mcw_tfc_aio:furniture/white_cedar_furniture/white_cedar_glass_table",
        "mcw_tfc_aio:furniture/willow_furniture/stripped_willow_glass_table",
        "mcw_tfc_aio:furniture/willow_furniture/willow_glass_table",
        "tfg:create/shaped/copper_diving_helmet",
        "tfg:create/shaped/fluid_tank",
        "tfg:create/shaped/stock_ticker",
        "tfg:shaped/snorkel"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/tfg/create/spruce_window_pane",
        "tfg:create/shapeless/spruce_window_pane"
      ],
      "model_parents": [
        "item/spruce_window_pane",
        "item/generated"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/spruce_window_pane"
      ],
      "block_context": {
        "block_id": "create:spruce_window_pane",
        "block_tags": [
          "forge:glass_panes",
          "tfc:mineable_with_glass_saw"
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
        "material_family": {
          "value": "wood_spruce",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix spruce_"
        },
        "form": {
          "value": "pane",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _window_pane"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "kubejs:shaped",
            "tfc:advanced_shaped_crafting"
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
      "id": "create:steam_engine",
      "namespace": "create",
      "display_name": "Steam Engine",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_steam_engine",
        "tfg:shaped/steam_thermal_centrifuge"
      ],
      "recipe_output_examples": [
        "tfg:create/shaped/steam_engine"
      ],
      "model_parents": [
        "item/steam_engine",
        "block/steam_engine/item"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/steam_engine"
      ],
      "block_context": {
        "block_id": "create:steam_engine",
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
      "document_context": [
        {
          "kind": "advancement",
          "id": "create:steam_engine",
          "label": "The Powerhouse",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "The Powerhouse"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Use a Steam Engine to generate torque"
            }
          ]
        },
        {
          "kind": "advancement",
          "id": "create:steam_engine_maxed",
          "label": "Full Steam",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Full Steam"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Run a boiler at the maximum level of power"
            }
          ]
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
            "kubejs:shaped"
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
      "id": "create:steam_whistle",
      "namespace": "create",
      "display_name": "Steam Whistle",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_steam_whistle"
      ],
      "recipe_output_examples": [
        "tfg:create/shaped/steam_whistle"
      ],
      "model_parents": [
        "item/steam_whistle",
        "block/steam_whistle/item"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/steam_whistle"
      ],
      "block_context": {
        "block_id": "create:steam_whistle",
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
      "document_context": [
        {
          "kind": "advancement",
          "id": "create:pipe_organ",
          "label": "The Pipe Organ",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "The Pipe Organ"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Attach 12 uniquely pitched Steam Whistles to a single Fluid Tank (Hidden Advancement)"
            }
          ]
        },
        {
          "kind": "advancement",
          "id": "create:steam_whistle",
          "label": "Voice of an Angel",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Voice of an Angel"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Activate a Steam Whistle"
            }
          ]
        },
        {
          "kind": "advancement",
          "id": "create:train_whistle",
          "label": "Choo Choo!",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Choo Choo!"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Assemble a Steam Whistle to your Train and activate it while driving"
            }
          ]
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
      "id": "create:sticker",
      "namespace": "create",
      "display_name": "Sticker",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "kubejs:shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_sticker"
      ],
      "recipe_output_examples": [
        "tfg:create/shaped/sticker"
      ],
      "model_parents": [
        "item/sticker",
        "block/sticker/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/sticker"
      ],
      "block_context": {
        "block_id": "create:sticker",
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
        "processing_in": {
          "values": [
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
      "id": "create:sticky_mechanical_piston",
      "namespace": "create",
      "display_name": "Sticky Mechanical Piston",
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
        "tfg:create/shaped/sticky_mechanical_piston_from_glue",
        "tfg:create/shaped/sticky_mechanical_piston_from_sticky_resin"
      ],
      "model_parents": [
        "item/sticky_mechanical_piston",
        "block/mechanical_piston/sticky/item",
        "block/mechanical_piston/normal/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/sticky_mechanical_piston"
      ],
      "block_context": {
        "block_id": "create:sticky_mechanical_piston",
        "block_tags": [
          "create:safe_nbt",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
      "id": "create:stock_link",
      "namespace": "create",
      "display_name": "Stock Link",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "crafting_shapeless": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "create:crafting/logistics/stock_link_clear",
        "greate:milling/integration/gtceu/macerator/macerate_stock_link",
        "tfg:create/shaped/redstone_requester",
        "tfg:create/shaped/stock_ticker"
      ],
      "recipe_output_examples": [
        "create:crafting/logistics/stock_link_clear",
        "tfg:create/shaped/stock_link"
      ],
      "model_parents": [
        "item/stock_link",
        "block/stock_link/block_vertical",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/stock_link"
      ],
      "block_context": {
        "block_id": "create:stock_link",
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