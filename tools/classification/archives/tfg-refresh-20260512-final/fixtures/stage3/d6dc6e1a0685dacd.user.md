# Items to classify
{
  "items": [
    {
      "id": "gtceu:plutonium_dust",
      "namespace": "gtceu",
      "display_name": "Plutonium 239 Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/plutonium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "gtceu:crafting_shaped_strict",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "gtceu:crafting_shaped_strict": 4,
        "smelting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "greate:milling": 3
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 5,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_plutonium",
        "gtceu:shaped/small_dust_disassembling_plutonium",
        "gtceu:shaped/tiny_dust_disassembling_3x3_plutonium",
        "gtceu:shaped/tiny_dust_disassembling_plutonium",
        "gtceu:smelting/smelt_dust_plutonium_to_ingot"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_long_plutonium_rod",
        "greate:milling/integration/gtceu/macerator/macerate_plutonium_block",
        "greate:milling/integration/gtceu/macerator/macerate_plutonium_ingot",
        "gtceu:shaped/small_dust_assembling_plutonium",
        "gtceu:shaped/tiny_dust_assembling_plutonium"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Pu²³⁹"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "form": {
          "value": "dust",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:dusts"
        },
        "processing_in": {
          "values": [
            "gtceu:crafting_shaped_strict",
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:plutonium_ingot",
      "namespace": "gtceu",
      "display_name": "Plutonium 239 Ingot",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ingots",
        "forge:ingots",
        "forge:ingots/plutonium",
        "tfc:pileable_ingots"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:compacting",
        "greate:milling",
        "vintageimprovements:polishing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "greate:compacting": 1,
        "greate:milling": 1,
        "vintageimprovements:polishing": 1
      },
      "recipe_production_by_type": {
        "smelting": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:compacting/plutonium_block",
        "greate:milling/integration/gtceu/macerator/macerate_plutonium_ingot",
        "gtceu:shaped/stick_plutonium",
        "tfg:vi/lathe/plutonium_to_rod"
      ],
      "recipe_output_examples": [
        "gtceu:smelting/smelt_dust_plutonium_to_ingot"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Pu²³⁹"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "form": {
          "value": "ingot",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ingots"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:compacting",
            "greate:milling",
            "vintageimprovements:polishing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:plutonium_nugget",
      "namespace": "gtceu",
      "display_name": "Plutonium 239 Nugget",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:nuggets",
        "forge:nuggets",
        "forge:nuggets/plutonium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_plutonium_nugget"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Pu²³⁹"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Instant Radiation (00:00)"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:food": {
          "nutrition": 0,
          "saturation_modifier": 0,
          "is_meat": false,
          "can_always_eat": false,
          "is_fast_food": false,
          "effects": [
            {
              "effect": "tfg:instant_radiation",
              "duration": -1,
              "amplifier": 0,
              "chance": 0.5
            }
          ]
        },
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "form": {
          "value": "nugget",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:nuggets"
        },
        "processing_in": {
          "values": [
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:plutonium_rod",
      "namespace": "gtceu",
      "display_name": "Plutonium 239 Rod",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:rods",
        "forge:rods/plutonium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:compacting",
        "greate:milling",
        "tfc:advanced_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 4,
        "greate:compacting": 1,
        "greate:milling": 1,
        "tfc:advanced_shapeless_crafting": 208
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "greate:cutting": 3,
        "vintageimprovements:polishing": 1
      },
      "recipe_ingredient_count": 214,
      "recipe_output_count": 6,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_plutonium_rod",
        "gtceu:shaped/axe_black_bronze",
        "gtceu:shaped/axe_blue_steel",
        "gtceu:shaped/axe_flint",
        "gtceu:shaped/axe_steel",
        "gtceu:shaped/axe_wrought_iron",
        "gtceu:shaped/butchery_knife_black_bronze",
        "gtceu:shaped/butchery_knife_boron_carbide",
        "gtceu:shaped/butchery_knife_copper",
        "gtceu:shaped/butchery_knife_duranium",
        "gtceu:shaped/butchery_knife_naquadah_alloy",
        "gtceu:shaped/butchery_knife_red_steel",
        "gtceu:shaped/butchery_knife_tungsten_carbide",
        "gtceu:shaped/butchery_knife_vanadium_steel",
        "gtceu:shaped/file_bismuth_bronze",
        "gtceu:shaped/file_blue_steel",
        "gtceu:shaped/file_bronze",
        "gtceu:shaped/file_diamond_tipped_mo_50_re",
        "gtceu:shaped/file_hsse",
        "gtceu:shaped/file_ostrum_iodide",
        "gtceu:shaped/file_steel",
        "gtceu:shaped/file_ultimet",
        "gtceu:shaped/file_wrought_iron",
        "gtceu:shaped/hammer_black_steel",
        "gtceu:shaped/hammer_boron_carbide",
        "gtceu:shaped/hammer_copper",
        "gtceu:shaped/hammer_duranium",
        "gtceu:shaped/hammer_neutronium",
        "gtceu:shaped/hammer_red_steel",
        "gtceu:shaped/hammer_stone",
        "gtceu:shaped/hammer_ultimet",
        "gtceu:shaped/hoe_bismuth_bronze",
        "gtceu:shaped/hoe_black_steel",
        "gtceu:shaped/hoe_boron_carbide",
        "gtceu:shaped/hoe_copper",
        "gtceu:shaped/hoe_duranium",
        "gtceu:shaped/hoe_naquadah_alloy",
        "gtceu:shaped/hoe_ostrum_iodide",
        "gtceu:shaped/hoe_steel",
        "gtceu:shaped/hoe_tungsten_carbide",
        "gtceu:shaped/hoe_wrought_iron",
        "gtceu:shaped/knife_black_bronze",
        "gtceu:shaped/knife_blue_steel",
        "gtceu:shaped/knife_bronze",
        "gtceu:shaped/knife_duranium",
        "gtceu:shaped/knife_hsse",
        "gtceu:shaped/knife_neutronium",
        "gtceu:shaped/knife_red_steel",
        "gtceu:shaped/knife_tungsten_carbide",
        "gtceu:shaped/knife_vanadium_steel",
        "gtceu:shaped/mining_hammer_bismuth_bronze",
        "gtceu:shaped/mining_hammer_black_steel",
        "gtceu:shaped/mining_hammer_copper",
        "gtceu:shaped/mining_hammer_steel",
        "gtceu:shaped/pickaxe_bismuth_bronze",
        "gtceu:shaped/pickaxe_black_steel",
        "gtceu:shaped/pickaxe_copper",
        "gtceu:shaped/pickaxe_red_steel",
        "gtceu:shaped/pickaxe_wrought_iron",
        "gtceu:shaped/plunger_silicone_rubber",
        "gtceu:shaped/saw_black_bronze",
        "gtceu:shaped/saw_blue_steel",
        "gtceu:shaped/saw_copper",
        "gtceu:shaped/saw_steel",
        "gtceu:shaped/screwdriver_bismuth_bronze",
        "gtceu:shaped/screwdriver_blue_steel",
        "gtceu:shaped/screwdriver_copper",
        "gtceu:shaped/screwdriver_red_steel",
        "gtceu:shaped/screwdriver_wrought_iron",
        "gtceu:shaped/scythe_black_steel",
        "gtceu:shaped/scythe_boron_carbide",
        "gtceu:shaped/scythe_copper",
        "gtceu:shaped/scythe_duranium",
        "gtceu:shaped/scythe_neutronium",
        "gtceu:shaped/scythe_red_steel",
        "gtceu:shaped/scythe_tungsten_carbide",
        "gtceu:shaped/scythe_vanadium_steel",
        "gtceu:shaped/shovel_black_bronze",
        "gtceu:shaped/shovel_blue_steel",
        "gtceu:shaped/shovel_copper",
        "gtceu:shaped/shovel_red_steel",
        "gtceu:shaped/shovel_wrought_iron",
        "gtceu:shaped/spade_black_bronze",
        "gtceu:shaped/spade_blue_steel",
        "gtceu:shaped/spade_copper",
        "gtceu:shaped/spade_wrought_iron",
        "gtceu:shaped/sword_black_bronze",
        "gtceu:shaped/sword_blue_steel",
        "gtceu:shaped/sword_bronze",
        "gtceu:shaped/sword_duranium",
        "gtceu:shaped/sword_hsse",
        "gtceu:shaped/sword_neutronium",
        "gtceu:shaped/sword_red_steel",
        "gtceu:shaped/sword_ultimet",
        "gtceu:shaped/sword_wrought_iron",
        "tfg:shaped/snowshoes"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_plutonium_long_rod_to_rod",
        "greate:cutting/integration/gtceu/cutter/cut_plutonium_long_rod_to_rod_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_plutonium_long_rod_to_rod_water",
        "gtceu:shaped/stick_long_plutonium",
        "gtceu:shaped/stick_plutonium",
        "tfg:vi/lathe/plutonium_to_rod"
      ],
      "recipe_examples_truncated": true,
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Pu²³⁹"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "form": {
          "value": "rod",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:rods"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:compacting",
            "greate:milling",
            "tfc:advanced_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:polished_dark_concrete",
      "namespace": "gtceu",
      "display_name": "Polished Dark Concrete",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:smooth_stone",
        "tfc:rock/smooth",
        "tfg:brick_index",
        "tfg:gtceu_concrete_blocks",
        "tfg:gtceu_concrete_blocks/dark_concrete",
        "tfg:interaction/smooth_brick",
        "tfg:stone_composition/concrete",
        "tfg:stone_types/dark_concrete",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 6,
        "greate:milling": 1,
        "kubejs:shaped": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 9
      },
      "recipe_production_by_type": {
        "create:sandpaper_polishing": 4,
        "smelting": 1,
        "stonecutting": 1,
        "tfc:chisel": 1,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:curving": 1
      },
      "recipe_ingredient_count": 20,
      "recipe_output_count": 9,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_concrete",
        "greate:shaped/steel_millstone",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfc:crafting/quern",
        "tfc:crafting/vanilla/redstone/repeater",
        "tfg:create/shaped/schematicannon",
        "tfg:shaped/comparator_certus",
        "tfg:shaped/comparator_nether_quartz",
        "tfg:shaped/comparator_quartzite",
        "tfg:stonecutter/gtceu_chiseled_dark_concrete",
        "tfg:stonecutter/gtceu_dark_concrete",
        "tfg:stonecutter/gtceu_dark_concrete_bricks",
        "tfg:stonecutter/gtceu_dark_concrete_small_tile",
        "tfg:stonecutter/gtceu_dark_concrete_tile",
        "tfg:stonecutter/gtceu_dark_concrete_windmill_a",
        "tfg:stonecutter/gtceu_dark_concrete_windmill_b",
        "tfg:stonecutter/gtceu_small_dark_concrete_bricks",
        "tfg:stonecutter/gtceu_square_dark_concrete_bricks"
      ],
      "recipe_output_examples": [
        "gtceu:smelting/smelt_polished_dark_concrete",
        "tfg:chisel/dark_concrete_raw_to_polished",
        "tfg:polishing/dark_concrete_brick_to_polished",
        "tfg:polishing/dark_concrete_cracked_brick_to_polished",
        "tfg:polishing/dark_concrete_mossy_brick_to_polished",
        "tfg:polishing/dark_concrete_raw_to_polished",
        "tfg:shapeless/dark_concrete_raw_to_polished",
        "tfg:stonecutter/gtceu_polished_dark_concrete",
        "tfg:vi/curving/extruder/extrude_polished_dark_concrete"
      ],
      "model_parents": [
        "item/polished_dark_concrete",
        "block/polished_dark_concrete",
        "block/cube_all"
      ],
      "creative_tabs": [
        "gtceu:decoration"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "gtceu:blocks/polished_dark_concrete"
      ],
      "block_context": {
        "block_id": "gtceu:polished_dark_concrete",
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
          "firmalife:oven_insulation",
          "forge:needs_wood_tool",
          "forge:very_fast_walkable_blocks",
          "minecraft:mineable/pickaxe",
          "tfc:bloomery_insulation",
          "tfc:forge_insulation",
          "tfg:gtceu_concrete_blocks",
          "tfg:gtceu_concrete_blocks/dark_concrete"
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
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
            "kubejs:shaped",
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
      "id": "gtceu:polished_light_concrete",
      "namespace": "gtceu",
      "display_name": "Polished Light Concrete",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:smooth_stone",
        "tfc:rock/smooth",
        "tfg:brick_index",
        "tfg:gtceu_concrete_blocks",
        "tfg:gtceu_concrete_blocks/light_concrete",
        "tfg:interaction/smooth_brick",
        "tfg:stone_composition/concrete",
        "tfg:stone_types/light_concrete",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 6,
        "greate:milling": 1,
        "kubejs:shaped": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 9
      },
      "recipe_production_by_type": {
        "create:sandpaper_polishing": 4,
        "smelting": 1,
        "stonecutting": 1,
        "tfc:chisel": 1,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:curving": 1
      },
      "recipe_ingredient_count": 20,
      "recipe_output_count": 9,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_concrete",
        "greate:shaped/steel_millstone",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfc:crafting/quern",
        "tfc:crafting/vanilla/redstone/repeater",
        "tfg:create/shaped/schematicannon",
        "tfg:shaped/comparator_certus",
        "tfg:shaped/comparator_nether_quartz",
        "tfg:shaped/comparator_quartzite",
        "tfg:stonecutter/gtceu_chiseled_light_concrete",
        "tfg:stonecutter/gtceu_light_concrete",
        "tfg:stonecutter/gtceu_light_concrete_bricks",
        "tfg:stonecutter/gtceu_light_concrete_small_tile",
        "tfg:stonecutter/gtceu_light_concrete_tile",
        "tfg:stonecutter/gtceu_light_concrete_windmill_a",
        "tfg:stonecutter/gtceu_light_concrete_windmill_b",
        "tfg:stonecutter/gtceu_small_light_concrete_bricks",
        "tfg:stonecutter/gtceu_square_light_concrete_bricks"
      ],
      "recipe_output_examples": [
        "gtceu:smelting/smelt_polished_light_concrete",
        "tfg:chisel/light_concrete_raw_to_polished",
        "tfg:polishing/light_concrete_brick_to_polished",
        "tfg:polishing/light_concrete_cracked_brick_to_polished",
        "tfg:polishing/light_concrete_mossy_brick_to_polished",
        "tfg:polishing/light_concrete_raw_to_polished",
        "tfg:shapeless/light_concrete_raw_to_polished",
        "tfg:stonecutter/gtceu_polished_light_concrete",
        "tfg:vi/curving/extruder/extrude_polished_light_concrete"
      ],
      "model_parents": [
        "item/polished_light_concrete",
        "block/polished_light_concrete",
        "block/cube_all"
      ],
      "creative_tabs": [
        "gtceu:decoration"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "gtceu:blocks/polished_light_concrete"
      ],
      "block_context": {
        "block_id": "gtceu:polished_light_concrete",
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
          "firmalife:oven_insulation",
          "forge:needs_wood_tool",
          "forge:very_fast_walkable_blocks",
          "minecraft:mineable/pickaxe",
          "tfc:bloomery_insulation",
          "tfc:forge_insulation",
          "tfg:gtceu_concrete_blocks",
          "tfg:gtceu_concrete_blocks/light_concrete"
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
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
            "kubejs:shaped",
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
      "id": "gtceu:polished_marble",
      "namespace": "gtceu",
      "display_name": "Polished Marble",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:stone_bricks",
        "tfc:rock/bricks",
        "tfc:rock/chiseled_bricks",
        "tfg:stone_composition/metamorphic",
        "tfg:stone_types/marble",
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
        "stonecutting": 16
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 42,
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
        "tfg:stonecutter/gtceu_chiseled_marble",
        "tfg:stonecutter/gtceu_marble_small_tile",
        "tfg:stonecutter/gtceu_marble_tile",
        "tfg:stonecutter/gtceu_marble_windmill_a",
        "tfg:stonecutter/gtceu_marble_windmill_b",
        "tfg:stonecutter/gtceu_small_marble_bricks",
        "tfg:stonecutter/gtceu_square_marble_bricks",
        "tfg:stonecutter/tfc_rock_bricks_marble",
        "tfg:stonecutter/tfc_rock_bricks_marble_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_marble_stairs",
        "tfg:stonecutter/tfc_rock_bricks_marble_wall",
        "tfg:stonecutter/tfc_rock_chiseled_marble",
        "tfg:stonecutter/tfc_rock_smooth_marble",
        "tfg:stonecutter/tfc_rock_smooth_marble_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_marble_stairs",
        "tfg:stonecutter/tfc_rock_smooth_marble_wall"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/gtceu_polished_marble"
      ],
      "model_parents": [
        "item/polished_marble",
        "block/polished_marble",
        "block/cube_all"
      ],
      "creative_tabs": [
        "gtceu:decoration"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "gtceu:blocks/polished_marble"
      ],
      "block_context": {
        "block_id": "gtceu:polished_marble",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "forge:needs_wood_tool",
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
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
      "id": "gtceu:polished_red_granite",
      "namespace": "gtceu",
      "display_name": "Polished Red Granite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:smooth_stone",
        "tfc:rock/smooth",
        "tfg:brick_index",
        "tfg:interaction/smooth_brick",
        "tfg:stone_composition/igneous_felsic",
        "tfg:stonecutting/red_granite",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 6,
        "greate:milling": 1,
        "kubejs:shaped": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 17
      },
      "recipe_production_by_type": {
        "create:sandpaper_polishing": 4,
        "smelting": 1,
        "stonecutting": 1,
        "tfc:chisel": 2,
        "tfc:damage_inputs_shapeless_crafting": 2,
        "vintageimprovements:curving": 1
      },
      "recipe_ingredient_count": 28,
      "recipe_output_count": 11,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_felsic",
        "greate:shaped/steel_millstone",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfc:crafting/quern",
        "tfc:crafting/vanilla/redstone/repeater",
        "tfg:create/shaped/schematicannon",
        "tfg:shaped/comparator_certus",
        "tfg:shaped/comparator_nether_quartz",
        "tfg:shaped/comparator_quartzite",
        "tfg:stonecutter/gtceu_chiseled_red_granite",
        "tfg:stonecutter/gtceu_polished_red_granite_to_tfg_rock_polished_red_granite_stairs",
        "tfg:stonecutter/gtceu_red_granite_bricks",
        "tfg:stonecutter/gtceu_red_granite_small_tile",
        "tfg:stonecutter/gtceu_red_granite_tile",
        "tfg:stonecutter/gtceu_red_granite_windmill_a",
        "tfg:stonecutter/gtceu_red_granite_windmill_b",
        "tfg:stonecutter/gtceu_small_red_granite_bricks",
        "tfg:stonecutter/gtceu_square_red_granite_bricks",
        "tfg:stonecutter/tfg_rock_bricks_red_granite_slab_half",
        "tfg:stonecutter/tfg_rock_bricks_red_granite_stairs",
        "tfg:stonecutter/tfg_rock_bricks_red_granite_wall",
        "tfg:stonecutter/tfg_rock_polished_red_granite_slab_half",
        "tfg:stonecutter/tfg_rock_polished_red_granite_stairs",
        "tfg:stonecutter/tfg_rock_polished_red_granite_wall",
        "tfg:stonecutting/gtceu_polished_red_granite_to_tfg_rock_polished_red_granite_slab",
        "tfg:stonecutting/gtceu_polished_red_granite_to_tfg_rock_polished_red_granite_wall"
      ],
      "recipe_output_examples": [
        "gtceu:smelting/smelt_polished_red_granite",
        "tfg:chisel/red_granite_hardened_to_polished",
        "tfg:chisel/red_granite_raw_to_polished",
        "tfg:polishing/red_granite_brick_to_polished",
        "tfg:polishing/red_granite_cracked_brick_to_polished",
        "tfg:polishing/red_granite_mossy_brick_to_polished",
        "tfg:polishing/red_granite_raw_to_polished",
        "tfg:shapeless/red_granite_hardened_to_polished",
        "tfg:shapeless/red_granite_raw_to_polished",
        "tfg:stonecutter/gtceu_polished_red_granite",
        "tfg:vi/curving/extruder/extrude_polished_red_granite"
      ],
      "model_parents": [
        "item/polished_red_granite",
        "block/polished_red_granite",
        "block/cube_all"
      ],
      "creative_tabs": [
        "gtceu:decoration"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "gtceu:blocks/polished_red_granite"
      ],
      "block_context": {
        "block_id": "gtceu:polished_red_granite",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "forge:needs_wood_tool",
          "minecraft:mineable/pickaxe",
          "tfc:bloomery_insulation",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
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
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
            "kubejs:shaped",
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
      "id": "gtceu:pollucite_dust",
      "namespace": "gtceu",
      "display_name": "Pollucite Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/pollucite"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "ae2:transform": 2,
        "blasting": 42,
        "crafting_shaped": 2,
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "greate:splashing": 4,
        "smelting": 43,
        "tfc:barrel_instant": 2,
        "vintageimprovements:centrifugation": 2
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 101,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_pollucite",
        "gtceu:shaped/small_dust_disassembling_pollucite",
        "gtceu:shaped/tiny_dust_disassembling_3x3_pollucite",
        "gtceu:shaped/tiny_dust_disassembling_pollucite"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerate_pollucite_refined_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_pollucite_refined_ore_to_dust",
        "greate:pressing/refined_pollucite_to_dust",
        "gtceu:blasting/smelt_andesite_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_basalt_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_black_sand_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_brown_sand_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_chalk_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_chert_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_claystone_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_dacite_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_deepslate_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_diorite_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_dolomite_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_dripstone_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_flavolite_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_gabbro_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_glacio_stone_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_gneiss_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_granite_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_green_sand_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_limestone_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_marble_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_mars_stone_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_mercury_stone_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_moon_deepslate_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_moon_stone_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_phyllite_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_pink_sand_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_pyroxenite_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_quartzite_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_raw_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_red_granite_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_red_sand_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_rhyolite_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_sandy_jadestone_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_schist_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_shale_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_slate_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_tuff_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_venus_stone_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_white_sand_pollucite_ore_to_ingot",
        "gtceu:blasting/smelt_yellow_sand_pollucite_ore_to_ingot",
        "gtceu:shaped/small_dust_assembling_pollucite",
        "gtceu:shaped/tiny_dust_assembling_pollucite",
        "gtceu:shapeless/centrifuged_ore_to_dust_pollucite",
        "gtceu:smelting/smelt_andesite_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_basalt_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_brown_sand_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_chalk_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_chert_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_claystone_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_conglomerate_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_dacite_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_deepslate_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_diorite_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_dolomite_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_dripstone_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_flavolite_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_gabbro_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_glacio_stone_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_gneiss_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_granite_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_limestone_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_marble_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_mars_stone_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_moon_deepslate_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_moon_stone_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_phyllite_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_pink_sand_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_pyroxenite_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_quartzite_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_raw_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_red_granite_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_red_sand_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_rhyolite_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_rich_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_sandy_jadestone_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_schist_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_shale_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_slate_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_tuff_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_venus_stone_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_white_sand_pollucite_ore_to_ingot",
        "tfg:ae_transform/pollucite_dust_from_impure",
        "tfg:ae_transform/pollucite_dust_from_pure",
        "tfg:instant_barrel/pollucite_dust_from_impure",
        "tfg:instant_barrel/pollucite_dust_from_pure",
        "tfg:splashing/pollucite_dust_from_impure_distilled",
        "tfg:splashing/pollucite_dust_from_impure_water",
        "tfg:splashing/pollucite_dust_from_pure_distilled",
        "tfg:splashing/pollucite_dust_from_pure_water",
        "tfg:vi/centrifuge/pollucite_dust_from_impure",
        "tfg:vi/centrifuge/pollucite_dust_from_pure"
      ],
      "recipe_examples_truncated": true,
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Cs₂Al₂Si₄(H₂O)₂O₁₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 36 mB of §fAluminium Silicate§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "form": {
          "value": "dust",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:dusts"
        },
        "processing_in": {
          "values": [
            "gtceu:crafting_shaped_strict"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:pollucite_indicator",
      "namespace": "gtceu",
      "display_name": "Pollucite Surface Rock",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:surface_rocks",
        "forge:surface_rocks/pollucite"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:pollucite_indicator",
        "block_tags": [
          "forge:surface_rocks",
          "forge:surface_rocks/pollucite",
          "tfc:can_be_ice_piled",
          "tfc:can_be_snow_piled",
          "tfccanes:not_slowed_with_cane",
          "tfg:dust_ore_indicators",
          "tfg:not_slowed_with_snowshoes"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Cs₂Al₂Si₄(H₂O)₂O₁₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        }
      }
    },
    {
      "id": "gtceu:pollucite_ore",
      "namespace": "gtceu",
      "display_name": "Pollucite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/pollucite",
        "forge:ores_in_ground/stone"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "blasting",
        "greate:milling",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "blasting": 1,
        "greate:milling": 1,
        "smelting": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 3,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_pollucite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_pollucite_ore_to_ingot",
        "gtceu:smelting/smelt_pollucite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:pollucite_ore",
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Cs₂Al₂Si₄(H₂O)₂O₁₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
          "value": "pollucite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id pollucite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "processing_in": {
          "values": [
            "blasting",
            "greate:milling",
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "y_level_range": {
          "value": "underground",
          "confidence": 1,
          "source": "rule:y_level_range_from_id",
          "rationale": "id pattern"
        }
      }
    },
    {
      "id": "gtceu:polybenzimidazole_block",
      "namespace": "gtceu",
      "display_name": "Block of Polybenzimidazole",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:storage_blocks",
        "forge:storage_blocks/polybenzimidazole",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:cutting",
        "greate:milling",
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "greate:cutting": 3,
        "greate:milling": 1,
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 7,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_polybenzimidazole_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_polybenzimidazole_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_polybenzimidazole_block_to_plate_water",
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:polybenzimidazole_block",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
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
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "forge:storage_blocks",
          "forge:storage_blocks/polybenzimidazole",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "C₂₀H₁₂N₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
            "greate:cutting",
            "greate:milling",
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:polybenzimidazole_bucket",
      "namespace": "gtceu",
      "display_name": "Liquid Polybenzimidazole Bucket",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "ae2:p2p_attunements/fluid_p2p_tunnel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_fluid"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "C₂₀H₁₂N₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 1,450 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "form": {
          "value": "bucket",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _bucket"
        }
      }
    },
    {
      "id": "gtceu:polybenzimidazole_dust",
      "namespace": "gtceu",
      "display_name": "Polybenzimidazole Pulp",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/polybenzimidazole"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "gtceu:crafting_shaped_strict",
        "vintageimprovements:curving"
      ],
      "recipe_consumption_by_type": {
        "gtceu:crafting_shaped_strict": 4,
        "vintageimprovements:curving": 7
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "greate:milling": 10
      },
      "recipe_ingredient_count": 11,
      "recipe_output_count": 12,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_polybenzimidazole",
        "gtceu:shaped/small_dust_disassembling_polybenzimidazole",
        "gtceu:shaped/tiny_dust_disassembling_3x3_polybenzimidazole",
        "gtceu:shaped/tiny_dust_disassembling_polybenzimidazole",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_dust_to_foil",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_huge_pipe_dust",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_large_pipe_dust",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_pipe_dust",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_small_pipe_dust",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_tiny_pipe_dust",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_to_ingot"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_advanced_data_access_hatch",
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_block",
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_huge_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_ingot",
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_large_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_nonuple_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_normal_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_plate",
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_quadruple_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_small_fluid_pipe",
        "gtceu:shaped/small_dust_assembling_polybenzimidazole",
        "gtceu:shaped/tiny_dust_assembling_polybenzimidazole"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "C₂₀H₁₂N₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "form": {
          "value": "dust",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:dusts"
        },
        "processing_in": {
          "values": [
            "gtceu:crafting_shaped_strict",
            "vintageimprovements:curving"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:polybenzimidazole_foil",
      "namespace": "gtceu",
      "display_name": "Thin Polybenzimidazole Sheet",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:foils",
        "forge:foils/polybenzimidazole"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "createaddition:rolling": 1,
        "vintageimprovements:curving": 2
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_foil"
      ],
      "recipe_output_examples": [
        "tfg:rolling/polybenzimidazole_foil",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_dust_to_foil",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_ingot_to_foil"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "C₂₀H₁₂N₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "processing_in": {
          "values": [
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:polybenzimidazole_huge_fluid_pipe",
      "namespace": "gtceu",
      "display_name": "Huge Polybenzimidazole Fluid Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:huge_fluid_pipes",
        "forge:huge_fluid_pipes/polybenzimidazole"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "vintageimprovements:curving": 2
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_huge_fluid_pipe"
      ],
      "recipe_output_examples": [
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_huge_pipe",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_huge_pipe_dust"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:polybenzimidazole_huge_fluid_pipe",
        "block_tags": [
          "forge:huge_fluid_pipes",
          "forge:huge_fluid_pipes/polybenzimidazole",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "C₂₀H₁₂N₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bTransfer Rate: §f67200 mB/t"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature Limit: §f1,000 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
        },
        "processing_in": {
          "values": [
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:polybenzimidazole_ingot",
      "namespace": "gtceu",
      "display_name": "Polybenzimidazole Ingot",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ingots",
        "forge:ingots",
        "forge:ingots/polybenzimidazole",
        "tfc:pileable_ingots"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "vintageimprovements:curving"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "vintageimprovements:curving": 7
      },
      "recipe_production_by_type": {
        "vintageimprovements:curving": 1
      },
      "recipe_ingredient_count": 8,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_ingot",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_huge_pipe",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_ingot_to_foil",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_large_pipe",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_pipe",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_small_pipe",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_tiny_pipe",
        "tfg:vi/curving/extruder/fluid_cell_pbi"
      ],
      "recipe_output_examples": [
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_to_ingot"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "C₂₀H₁₂N₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "form": {
          "value": "ingot",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ingots"
        },
        "processing_in": {
          "values": [
            "greate:milling",
            "vintageimprovements:curving"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:polybenzimidazole_large_fluid_pipe",
      "namespace": "gtceu",
      "display_name": "Large Polybenzimidazole Fluid Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:large_fluid_pipes",
        "forge:large_fluid_pipes/polybenzimidazole"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "vintageimprovements:curving": 2
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_large_fluid_pipe",
        "gtceu:shaped/filter_casing_sterile"
      ],
      "recipe_output_examples": [
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_large_pipe",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_large_pipe_dust"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:polybenzimidazole_large_fluid_pipe",
        "block_tags": [
          "forge:large_fluid_pipes",
          "forge:large_fluid_pipes/polybenzimidazole",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "C₂₀H₁₂N₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bTransfer Rate: §f33600 mB/t"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature Limit: §f1,000 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:polybenzimidazole_mallet",
      "namespace": "gtceu",
      "display_name": "Polybenzimidazole Soft Mallet",
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
      "model_parents": [],
      "creative_tabs": [
        "gtceu:tool"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§8Sneak to Pause Machine After Current Recipe."
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Stops/Starts Machines"
        },
        {
          "source": "runtime-tooltip",
          "text": "1,024 §aCrafting Uses"
        },
        {
          "source": "runtime-tooltip",
          "text": "1,023 §eTotal Durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "1,024 §bDurability"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Usable as: §fSoft Mallet"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Craft with a Repair Kit to repair 25% durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 1023,
        "minecraft:enchantable": {},
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "has_durability": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_durability_from_component"
        },
        "has_enchantments": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_enchantments_from_component"
        }
      }
    },
    {
      "id": "gtceu:polybenzimidazole_nonuple_fluid_pipe",
      "namespace": "gtceu",
      "display_name": "Nonuple Polybenzimidazole Fluid Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:nonuple_fluid_pipes",
        "forge:nonuple_fluid_pipes/polybenzimidazole"
      ],
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
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_nonuple_fluid_pipe"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/nonuple_polybenzimidazole_pipe"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:polybenzimidazole_nonuple_fluid_pipe",
        "block_tags": [
          "forge:mineable/wrench",
          "forge:nonuple_fluid_pipes",
          "forge:nonuple_fluid_pipes/polybenzimidazole",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "C₂₀H₁₂N₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bTransfer Rate: §f5600 mB/t"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature Limit: §f1,000 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eChannels: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
        },
        "processing_in": {
          "values": [
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:polybenzimidazole_normal_fluid_pipe",
      "namespace": "gtceu",
      "display_name": "Normal Polybenzimidazole Fluid Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:normal_fluid_pipes",
        "forge:normal_fluid_pipes/polybenzimidazole"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "vintageimprovements:curving": 2
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_normal_fluid_pipe",
        "gtceu:shaped/large_mixer"
      ],
      "recipe_output_examples": [
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_pipe",
        "tfg:vi/curving/extruder/extrude_polybenzimidazole_pipe_dust"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:polybenzimidazole_normal_fluid_pipe",
        "block_tags": [
          "forge:mineable/wrench",
          "forge:normal_fluid_pipes",
          "forge:normal_fluid_pipes/polybenzimidazole",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "C₂₀H₁₂N₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bTransfer Rate: §f16800 mB/t"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature Limit: §f1,000 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:polybenzimidazole_nugget",
      "namespace": "gtceu",
      "display_name": "Polybenzimidazole Chip",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:nuggets",
        "forge:nuggets",
        "forge:nuggets/polybenzimidazole"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_nugget"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "C₂₀H₁₂N₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "form": {
          "value": "nugget",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:nuggets"
        },
        "processing_in": {
          "values": [
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:polybenzimidazole_plate",
      "namespace": "gtceu",
      "display_name": "Polybenzimidazole Sheet",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:plates",
        "forge:plates/polybenzimidazole",
        "forge:sheets/polybenzimidazole",
        "tfc:pileable_sheets"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "createaddition:rolling",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 3,
        "createaddition:rolling": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "greate:cutting": 3
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_plate",
        "gtceu:shaped/uhv_machine_hull",
        "gtceu:shaped/uv_machine_hull",
        "gtceu:shaped/zpm_machine_hull",
        "tfg:rolling/polybenzimidazole_foil"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_polybenzimidazole_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_polybenzimidazole_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_polybenzimidazole_block_to_plate_water"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "C₂₀H₁₂N₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "form": {
          "value": "plate",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:plates"
        },
        "processing_in": {
          "values": [
            "crafting",
            "createaddition:rolling",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:polybenzimidazole_plunger",
      "namespace": "gtceu",
      "display_name": "Polybenzimidazole Plunger",
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
      "model_parents": [],
      "creative_tabs": [
        "gtceu:tool"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§8Removes Fluids from Machines"
        },
        {
          "source": "runtime-tooltip",
          "text": "1,023 §eTotal Durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "1,024 §bDurability"
        },
        {
          "source": "runtime-tooltip",
          "text": "§9Plumber: §fDrains Fluids"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Usable as: §fPlunger"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Craft with a Repair Kit to repair 25% durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 1023,
        "minecraft:enchantable": {},
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "has_durability": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_durability_from_component"
        },
        "has_enchantments": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_enchantments_from_component"
        }
      }
    },
    {
      "id": "gtceu:polybenzimidazole_quadruple_fluid_pipe",
      "namespace": "gtceu",
      "display_name": "Quadruple Polybenzimidazole Fluid Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:quadruple_fluid_pipes",
        "forge:quadruple_fluid_pipes/polybenzimidazole"
      ],
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
        "greate:milling/integration/gtceu/macerator/macerate_polybenzimidazole_quadruple_fluid_pipe"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/quadruple_polybenzimidazole_pipe"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:polybenzimidazole_quadruple_fluid_pipe",
        "block_tags": [
          "forge:mineable/wrench",
          "forge:quadruple_fluid_pipes",
          "forge:quadruple_fluid_pipes/polybenzimidazole",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "C₂₀H₁₂N₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bTransfer Rate: §f5600 mB/t"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature Limit: §f1,000 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eChannels: §f4"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
        },
        "processing_in": {
          "values": [
            "greate:milling"
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