# Items to classify
{
  "items": [
    {
      "id": "tfg:calorite_dust",
      "namespace": "tfg",
      "display_name": "Calorite Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/calorite"
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
        "greate:milling": 6
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 8,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_calorite",
        "gtceu:shaped/small_dust_disassembling_calorite",
        "gtceu:shaped/tiny_dust_disassembling_3x3_calorite",
        "gtceu:shaped/tiny_dust_disassembling_calorite",
        "gtceu:smelting/smelt_dust_calorite_to_ingot"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_calorite_block",
        "greate:milling/integration/gtceu/macerator/macerate_calorite_frame",
        "greate:milling/integration/gtceu/macerator/macerate_calorite_ingot",
        "greate:milling/integration/gtceu/macerator/macerate_calorite_plate",
        "greate:milling/integration/gtceu/macerator/macerate_calorite_sliding_door",
        "greate:milling/integration/gtceu/macerator/macerate_double_calorite_plate",
        "gtceu:shaped/small_dust_assembling_calorite",
        "gtceu:shaped/tiny_dust_assembling_calorite"
      ],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:calorite_frame",
      "namespace": "tfg",
      "display_name": "Calorite Frame",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:frames",
        "forge:frames/calorite"
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
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "framedblocks:framed_reinforcement",
        "greate:milling/integration/gtceu/macerator/macerate_calorite_frame"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/frame_calorite"
      ],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:calorite_frame",
        "block_tags": [
          "forge:frames",
          "forge:frames/calorite",
          "forge:mineable/wrench",
          "forge:slow_walkable_blocks",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:calorite_rod",
      "namespace": "tfg",
      "display_name": "Calorite Rod",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:rods",
        "forge:rods/calorite"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "tfc:advanced_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 5,
        "greate:milling": 1,
        "tfc:advanced_shapeless_crafting": 208
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "vintageimprovements:polishing": 1
      },
      "recipe_ingredient_count": 214,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_calorite_rod",
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
        "gtceu:shaped/hammer_black_bronze",
        "gtceu:shaped/hammer_blue_steel",
        "gtceu:shaped/hammer_bronze",
        "gtceu:shaped/hammer_diamond_tipped_mo_50_re",
        "gtceu:shaped/hammer_naquadah_alloy",
        "gtceu:shaped/hammer_ostrum_iodide",
        "gtceu:shaped/hammer_steel",
        "gtceu:shaped/hammer_tungsten_carbide",
        "gtceu:shaped/hammer_wrought_iron",
        "gtceu:shaped/hoe_black_bronze",
        "gtceu:shaped/hoe_blue_steel",
        "gtceu:shaped/hoe_bronze",
        "gtceu:shaped/hoe_diamond_tipped_mo_50_re",
        "gtceu:shaped/hoe_hsse",
        "gtceu:shaped/hoe_neutronium",
        "gtceu:shaped/hoe_red_steel",
        "gtceu:shaped/hoe_stone",
        "gtceu:shaped/hoe_vanadium_steel",
        "gtceu:shaped/knife_bismuth_bronze",
        "gtceu:shaped/knife_black_steel",
        "gtceu:shaped/knife_boron_carbide",
        "gtceu:shaped/knife_diamond_tipped_mo_50_re",
        "gtceu:shaped/knife_flint",
        "gtceu:shaped/knife_naquadah_alloy",
        "gtceu:shaped/knife_ostrum_iodide",
        "gtceu:shaped/knife_stone",
        "gtceu:shaped/knife_ultimet",
        "gtceu:shaped/knife_wrought_iron",
        "gtceu:shaped/mining_hammer_black_bronze",
        "gtceu:shaped/mining_hammer_bronze",
        "gtceu:shaped/mining_hammer_red_steel",
        "gtceu:shaped/mining_hammer_wrought_iron",
        "gtceu:shaped/pickaxe_black_bronze",
        "gtceu:shaped/pickaxe_bronze",
        "gtceu:shaped/pickaxe_neutronium",
        "gtceu:shaped/pickaxe_steel",
        "gtceu:shaped/plunger_rubber",
        "gtceu:shaped/saw_bismuth_bronze",
        "gtceu:shaped/saw_black_steel",
        "gtceu:shaped/saw_bronze",
        "gtceu:shaped/saw_red_steel",
        "gtceu:shaped/saw_wrought_iron",
        "gtceu:shaped/screwdriver_black_steel",
        "gtceu:shaped/screwdriver_bronze",
        "gtceu:shaped/screwdriver_neutronium",
        "gtceu:shaped/screwdriver_steel",
        "gtceu:shaped/scythe_black_bronze",
        "gtceu:shaped/scythe_blue_steel",
        "gtceu:shaped/scythe_bronze",
        "gtceu:shaped/scythe_diamond_tipped_mo_50_re",
        "gtceu:shaped/scythe_naquadah_alloy",
        "gtceu:shaped/scythe_ostrum_iodide",
        "gtceu:shaped/scythe_steel",
        "gtceu:shaped/scythe_ultimet",
        "gtceu:shaped/shovel_bismuth_bronze",
        "gtceu:shaped/shovel_black_steel",
        "gtceu:shaped/shovel_bronze",
        "gtceu:shaped/shovel_flint",
        "gtceu:shaped/shovel_stone",
        "gtceu:shaped/spade_bismuth_bronze",
        "gtceu:shaped/spade_black_steel",
        "gtceu:shaped/spade_bronze",
        "gtceu:shaped/spade_steel",
        "gtceu:shaped/sword_bismuth_bronze",
        "gtceu:shaped/sword_black_steel",
        "gtceu:shaped/sword_boron_carbide",
        "gtceu:shaped/sword_diamond_tipped_mo_50_re",
        "gtceu:shaped/sword_flint",
        "gtceu:shaped/sword_naquadah_alloy",
        "gtceu:shaped/sword_ostrum_iodide",
        "gtceu:shaped/sword_tungsten_carbide",
        "gtceu:shaped/sword_vanadium_steel",
        "tfg:shaped/snowshoes"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/stick_calorite",
        "tfg:vi/lathe/calorite_to_rod"
      ],
      "recipe_examples_truncated": true,
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:carbonate_hornfels",
      "namespace": "tfg",
      "display_name": "Carbonate Hornfels",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:non_movable",
        "tfg:stone_composition/sedimentary_carbonate"
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
        "greate:milling/integration/tfg/macerate_sedimentary_carbonate"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/carbonate_hornfels",
        "block/carbonate_hornfels",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/carbonate_hornfels"
      ],
      "block_context": {
        "block_id": "tfg:carbonate_hornfels",
        "block_tags": [
          "create:non_movable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
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
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "firmalife:oven_insulation",
          "firmalife:pipe_replaceable",
          "forge:stone",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_diamond_tool",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:bloomery_insulation",
          "tfc:can_carve",
          "tfc:can_collapse",
          "tfc:creeping_stone_plantable_on",
          "tfc:forge_insulation",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfg:anemone_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
          "value": "diamond",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_diamond_tool"
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
      "id": "tfg:casings/bioculture_rotor_primary",
      "namespace": "tfg",
      "display_name": "Primary Bioculture Rotor",
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
        "greate:milling/integration/gtceu/macerator/macerate_casings/bioculture_rotor_primary",
        "tfg:shapeless/bioculture_rotor_primary_to_secondary"
      ],
      "recipe_output_examples": [
        "tfg:shaped/bioculture_rotor_primary",
        "tfg:shapeless/bioculture_rotor_secondary_to_primary"
      ],
      "model_parents": [
        "item/casings/bioculture_rotor_primary",
        "block/casings/bioculture_rotor_primary"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/bioculture_rotor_primary"
      ],
      "block_context": {
        "block_id": "tfg:casings/bioculture_rotor_primary",
        "block_tags": [
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:casings/bioculture_rotor_secondary",
      "namespace": "tfg",
      "display_name": "Secondary Bioculture Rotor",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "tfg:shapeless/bioculture_rotor_secondary_to_primary"
      ],
      "recipe_output_examples": [
        "tfg:shapeless/bioculture_rotor_primary_to_secondary"
      ],
      "model_parents": [
        "item/casings/bioculture_rotor_secondary",
        "block/casings/bioculture_rotor_secondary"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/bioculture_rotor_secondary"
      ],
      "block_context": {
        "block_id": "tfg:casings/bioculture_rotor_secondary",
        "block_tags": [
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "tfg:casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:casings/greenhouse/copper_greenhouse_casing_0",
      "namespace": "tfg",
      "display_name": "§nFlawless§r Copper Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:copper_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/copper_greenhouse_port",
        "tfg:stonecutter/firmalife_copper_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_1",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_2",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_3",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_0"
      ],
      "model_parents": [
        "item/casings/greenhouse/copper_greenhouse_casing_0",
        "block/casings/greenhouse/copper_greenhouse_casing_0",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/copper_greenhouse_casing_0"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/copper_greenhouse_casing_0",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:all_copper_greenhouse",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/pickaxe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:copper_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/copper_greenhouse_casing_1",
      "namespace": "tfg",
      "display_name": "§nVertical§r Copper Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:copper_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/copper_greenhouse_port",
        "tfg:stonecutter/firmalife_copper_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_0",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_2",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_3",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_1"
      ],
      "model_parents": [
        "item/casings/greenhouse/copper_greenhouse_casing_1",
        "block/casings/greenhouse/copper_greenhouse_casing_1",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/copper_greenhouse_casing_1"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/copper_greenhouse_casing_1",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:all_copper_greenhouse",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/pickaxe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:copper_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/copper_greenhouse_casing_2",
      "namespace": "tfg",
      "display_name": "§nHorizontal§r Copper Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:copper_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/copper_greenhouse_port",
        "tfg:stonecutter/firmalife_copper_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_0",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_1",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_3",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_2"
      ],
      "model_parents": [
        "item/casings/greenhouse/copper_greenhouse_casing_2",
        "block/casings/greenhouse/copper_greenhouse_casing_2",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/copper_greenhouse_casing_2"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/copper_greenhouse_casing_2",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:all_copper_greenhouse",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/pickaxe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:copper_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/copper_greenhouse_casing_3",
      "namespace": "tfg",
      "display_name": "§nNubio§r Copper Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:copper_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/copper_greenhouse_port",
        "tfg:stonecutter/firmalife_copper_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_0",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_1",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_2",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_3"
      ],
      "model_parents": [
        "item/casings/greenhouse/copper_greenhouse_casing_3",
        "block/casings/greenhouse/copper_greenhouse_casing_3",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/copper_greenhouse_casing_3"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/copper_greenhouse_casing_3",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:all_copper_greenhouse",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/pickaxe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:copper_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/copper_greenhouse_casing_4",
      "namespace": "tfg",
      "display_name": "§nOrnate§r Copper Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:copper_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/copper_greenhouse_port",
        "tfg:stonecutter/firmalife_copper_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_0",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_1",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_2",
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_3"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_copper_greenhouse_casing_4"
      ],
      "model_parents": [
        "item/casings/greenhouse/copper_greenhouse_casing_4",
        "block/casings/greenhouse/copper_greenhouse_casing_4",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/copper_greenhouse_casing_4"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/copper_greenhouse_casing_4",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:all_copper_greenhouse",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/pickaxe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:copper_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/iron_greenhouse_casing_0",
      "namespace": "tfg",
      "display_name": "§nFlawless§r Iron Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:iron_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/iron_greenhouse_port",
        "tfg:stonecutter/firmalife_iron_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_1",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_2",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_3",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_0"
      ],
      "model_parents": [
        "item/casings/greenhouse/iron_greenhouse_casing_0",
        "block/casings/greenhouse/iron_greenhouse_casing_0",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/iron_greenhouse_casing_0"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/iron_greenhouse_casing_0",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:all_iron_greenhouse",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/pickaxe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:iron_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/iron_greenhouse_casing_1",
      "namespace": "tfg",
      "display_name": "§nVertical§r Iron Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:iron_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/iron_greenhouse_port",
        "tfg:stonecutter/firmalife_iron_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_0",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_2",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_3",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_1"
      ],
      "model_parents": [
        "item/casings/greenhouse/iron_greenhouse_casing_1",
        "block/casings/greenhouse/iron_greenhouse_casing_1",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/iron_greenhouse_casing_1"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/iron_greenhouse_casing_1",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:all_iron_greenhouse",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/pickaxe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:iron_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/iron_greenhouse_casing_2",
      "namespace": "tfg",
      "display_name": "§nHorizontal§r Iron Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:iron_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/iron_greenhouse_port",
        "tfg:stonecutter/firmalife_iron_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_0",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_1",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_3",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_2"
      ],
      "model_parents": [
        "item/casings/greenhouse/iron_greenhouse_casing_2",
        "block/casings/greenhouse/iron_greenhouse_casing_2",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/iron_greenhouse_casing_2"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/iron_greenhouse_casing_2",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:all_iron_greenhouse",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/pickaxe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:iron_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/iron_greenhouse_casing_3",
      "namespace": "tfg",
      "display_name": "§nNubio§r Iron Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:iron_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/iron_greenhouse_port",
        "tfg:stonecutter/firmalife_iron_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_0",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_1",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_2",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_3"
      ],
      "model_parents": [
        "item/casings/greenhouse/iron_greenhouse_casing_3",
        "block/casings/greenhouse/iron_greenhouse_casing_3",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/iron_greenhouse_casing_3"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/iron_greenhouse_casing_3",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:all_iron_greenhouse",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/pickaxe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:iron_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/iron_greenhouse_casing_4",
      "namespace": "tfg",
      "display_name": "§nOrnate§r Iron Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:iron_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/iron_greenhouse_port",
        "tfg:stonecutter/firmalife_iron_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_0",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_1",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_2",
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_3"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_iron_greenhouse_casing_4"
      ],
      "model_parents": [
        "item/casings/greenhouse/iron_greenhouse_casing_4",
        "block/casings/greenhouse/iron_greenhouse_casing_4",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/iron_greenhouse_casing_4"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/iron_greenhouse_casing_4",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:all_iron_greenhouse",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/pickaxe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:iron_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/stainless_greenhouse_casing_0",
      "namespace": "tfg",
      "display_name": "§nFlawless§r Stainless Steel Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:stainless_steel_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/stainless_steel_greenhouse_port",
        "tfg:stonecutter/firmalife_stainless_steel_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_1",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_2",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_3",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_0"
      ],
      "model_parents": [
        "item/casings/greenhouse/stainless_greenhouse_casing_0",
        "block/casings/greenhouse/stainless_greenhouse_casing_0",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/stainless_greenhouse_casing_0"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/stainless_greenhouse_casing_0",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "firmalife:stainless_steel_greenhouse",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/pickaxe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:stainless_steel_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/stainless_greenhouse_casing_1",
      "namespace": "tfg",
      "display_name": "§nVertical§r Stainless Steel Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:stainless_steel_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/stainless_steel_greenhouse_port",
        "tfg:stonecutter/firmalife_stainless_steel_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_0",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_2",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_3",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_1"
      ],
      "model_parents": [
        "item/casings/greenhouse/stainless_greenhouse_casing_1",
        "block/casings/greenhouse/stainless_greenhouse_casing_1",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/stainless_greenhouse_casing_1"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/stainless_greenhouse_casing_1",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "firmalife:stainless_steel_greenhouse",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/pickaxe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:stainless_steel_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/stainless_greenhouse_casing_2",
      "namespace": "tfg",
      "display_name": "§nHorizontal§r Stainless Steel Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:stainless_steel_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/stainless_steel_greenhouse_port",
        "tfg:stonecutter/firmalife_stainless_steel_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_0",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_1",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_3",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_2"
      ],
      "model_parents": [
        "item/casings/greenhouse/stainless_greenhouse_casing_2",
        "block/casings/greenhouse/stainless_greenhouse_casing_2",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/stainless_greenhouse_casing_2"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/stainless_greenhouse_casing_2",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "firmalife:stainless_steel_greenhouse",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/pickaxe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:stainless_steel_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/stainless_greenhouse_casing_3",
      "namespace": "tfg",
      "display_name": "§nNubio§r Stainless Steel Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:stainless_steel_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/stainless_steel_greenhouse_port",
        "tfg:stonecutter/firmalife_stainless_steel_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_0",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_1",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_2",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_3"
      ],
      "model_parents": [
        "item/casings/greenhouse/stainless_greenhouse_casing_3",
        "block/casings/greenhouse/stainless_greenhouse_casing_3",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/stainless_greenhouse_casing_3"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/stainless_greenhouse_casing_3",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "firmalife:stainless_steel_greenhouse",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/pickaxe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:stainless_steel_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/stainless_greenhouse_casing_4",
      "namespace": "tfg",
      "display_name": "§nOrnate§r Stainless Steel Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:stainless_steel_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/stainless_steel_greenhouse_port",
        "tfg:stonecutter/firmalife_stainless_steel_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_0",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_1",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_2",
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_3"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_stainless_greenhouse_casing_4"
      ],
      "model_parents": [
        "item/casings/greenhouse/stainless_greenhouse_casing_4",
        "block/casings/greenhouse/stainless_greenhouse_casing_4",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/stainless_greenhouse_casing_4"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/stainless_greenhouse_casing_4",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "firmalife:stainless_steel_greenhouse",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/pickaxe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:stainless_steel_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/treated_wood_greenhouse_casing_0",
      "namespace": "tfg",
      "display_name": "§nFlawless§r Wax-Treated Wood Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:treated_wood_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/treated_wood_greenhouse_port",
        "tfg:stonecutter/firmalife_treated_wood_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_1",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_2",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_3",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_0"
      ],
      "model_parents": [
        "item/casings/greenhouse/treated_wood_greenhouse_casing_0",
        "block/casings/greenhouse/treated_wood_greenhouse_casing_0",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/treated_wood_greenhouse_casing_0"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/treated_wood_greenhouse_casing_0",
        "block_tags": [
          "cucumber:mineable/paxel",
          "firmalife:all_treated_wood_greenhouse",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/axe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:treated_wood_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/treated_wood_greenhouse_casing_1",
      "namespace": "tfg",
      "display_name": "§nVertical§r Wax-Treated Wood Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:treated_wood_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/treated_wood_greenhouse_port",
        "tfg:stonecutter/firmalife_treated_wood_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_0",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_2",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_3",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_1"
      ],
      "model_parents": [
        "item/casings/greenhouse/treated_wood_greenhouse_casing_1",
        "block/casings/greenhouse/treated_wood_greenhouse_casing_1",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/treated_wood_greenhouse_casing_1"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/treated_wood_greenhouse_casing_1",
        "block_tags": [
          "cucumber:mineable/paxel",
          "firmalife:all_treated_wood_greenhouse",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/axe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:treated_wood_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/treated_wood_greenhouse_casing_2",
      "namespace": "tfg",
      "display_name": "§nHorizontal§r Wax-Treated Wood Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:treated_wood_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/treated_wood_greenhouse_port",
        "tfg:stonecutter/firmalife_treated_wood_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_0",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_1",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_3",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_2"
      ],
      "model_parents": [
        "item/casings/greenhouse/treated_wood_greenhouse_casing_2",
        "block/casings/greenhouse/treated_wood_greenhouse_casing_2",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/treated_wood_greenhouse_casing_2"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/treated_wood_greenhouse_casing_2",
        "block_tags": [
          "cucumber:mineable/paxel",
          "firmalife:all_treated_wood_greenhouse",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/axe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:treated_wood_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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
      "id": "tfg:casings/greenhouse/treated_wood_greenhouse_casing_3",
      "namespace": "tfg",
      "display_name": "§nNubio§r Wax-Treated Wood Greenhouse Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:all_greenhouse_casings",
        "tfg:casings",
        "tfg:treated_wood_greenhouse_casings"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 1,
        "stonecutting": 5
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/greenhouse/treated_wood_greenhouse_port",
        "tfg:stonecutter/firmalife_treated_wood_greenhouse_wall",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_0",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_1",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_2",
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_4"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_casings_greenhouse_treated_wood_greenhouse_casing_3"
      ],
      "model_parents": [
        "item/casings/greenhouse/treated_wood_greenhouse_casing_3",
        "block/casings/greenhouse/treated_wood_greenhouse_casing_3",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/casings/greenhouse/treated_wood_greenhouse_casing_3"
      ],
      "block_context": {
        "block_id": "tfg:casings/greenhouse/treated_wood_greenhouse_casing_3",
        "block_tags": [
          "cucumber:mineable/paxel",
          "firmalife:all_treated_wood_greenhouse",
          "firmalife:greenhouse",
          "firmalife:greenhouse_full_walls",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:mineable/axe",
          "tfc:mineable_with_glass_saw",
          "tfg:casings",
          "tfg:treated_wood_greenhouse_casings"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs:shaped",
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