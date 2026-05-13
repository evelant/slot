# Items to classify
{
  "items": [
    {
      "id": "gtceu:nether_quartz_indicator",
      "namespace": "gtceu",
      "display_name": "Nether Quartz Surface Rock",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:surface_rocks",
        "forge:surface_rocks/nether_quartz"
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
        "block_id": "gtceu:nether_quartz_indicator",
        "block_tags": [
          "forge:surface_rocks",
          "forge:surface_rocks/nether_quartz",
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
          "text": "SiO₂"
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
      "id": "gtceu:nether_quartz_ore",
      "namespace": "gtceu",
      "display_name": "Nether Quartz Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/nether_quartz",
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
        "greate:milling/integration/gtceu/macerator/macerate_nether_quartz_ore_to_crushed_ore",
        "gtceu:blasting/smelt_nether_quartz_ore_to_ingot",
        "gtceu:smelting/smelt_nether_quartz_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:nether_quartz_ore",
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "SiO₂"
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
          "value": "nether_quartz",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id nether_quartz_ore"
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
          "value": "nether_surface",
          "confidence": 1,
          "source": "rule:y_level_range_from_id",
          "rationale": "id pattern"
        }
      }
    },
    {
      "id": "gtceu:nether_quartz_plate",
      "namespace": "gtceu",
      "display_name": "Nether Quartz Plate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:plates",
        "forge:plates/nether_quartz",
        "forge:sheets/nether_quartz",
        "tfc:pileable_sheets"
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
        "greate:cutting": 3
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_nether_quartz_plate",
        "gtceu:shaped/daylight_detector"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_nether_quartz_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_nether_quartz_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_nether_quartz_block_to_plate_water"
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
          "text": "SiO₂"
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:nether_quartz_rod",
      "namespace": "gtceu",
      "display_name": "Nether Quartz Rod",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:rods",
        "forge:rods/nether_quartz"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "tfc:advanced_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 4,
        "greate:milling": 1,
        "tfc:advanced_shapeless_crafting": 208
      },
      "recipe_production_by_type": {
        "create:sandpaper_polishing": 1,
        "vintageimprovements:polishing": 1
      },
      "recipe_ingredient_count": 213,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_nether_quartz_rod",
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
        "gtceu:shaped/file_black_steel",
        "gtceu:shaped/file_bronze",
        "gtceu:shaped/file_diamond_tipped_mo_50_re",
        "gtceu:shaped/file_hsse",
        "gtceu:shaped/file_neutronium",
        "gtceu:shaped/file_steel",
        "gtceu:shaped/file_ultimet",
        "gtceu:shaped/file_wrought_iron",
        "gtceu:shaped/hammer_black_bronze",
        "gtceu:shaped/hammer_boron_carbide",
        "gtceu:shaped/hammer_copper",
        "gtceu:shaped/hammer_duranium",
        "gtceu:shaped/hammer_naquadah_alloy",
        "gtceu:shaped/hammer_ostrum_iodide",
        "gtceu:shaped/hammer_stone",
        "gtceu:shaped/hammer_ultimet",
        "gtceu:shaped/hammer_wrought_iron",
        "gtceu:shaped/hoe_black_bronze",
        "gtceu:shaped/hoe_boron_carbide",
        "gtceu:shaped/hoe_copper",
        "gtceu:shaped/hoe_duranium",
        "gtceu:shaped/hoe_hsse",
        "gtceu:shaped/hoe_ostrum_iodide",
        "gtceu:shaped/hoe_steel",
        "gtceu:shaped/hoe_tungsten_carbide",
        "gtceu:shaped/hoe_vanadium_steel",
        "gtceu:shaped/knife_bismuth_bronze",
        "gtceu:shaped/knife_blue_steel",
        "gtceu:shaped/knife_bronze",
        "gtceu:shaped/knife_diamond_tipped_mo_50_re",
        "gtceu:shaped/knife_flint",
        "gtceu:shaped/knife_neutronium",
        "gtceu:shaped/knife_red_steel",
        "gtceu:shaped/knife_stone",
        "gtceu:shaped/knife_ultimet",
        "gtceu:shaped/mining_hammer_bismuth_bronze",
        "gtceu:shaped/mining_hammer_black_steel",
        "gtceu:shaped/mining_hammer_bronze",
        "gtceu:shaped/mining_hammer_red_steel",
        "gtceu:shaped/pickaxe_bismuth_bronze",
        "gtceu:shaped/pickaxe_black_steel",
        "gtceu:shaped/pickaxe_bronze",
        "gtceu:shaped/pickaxe_neutronium",
        "gtceu:shaped/pickaxe_steel",
        "gtceu:shaped/plunger_silicone_rubber",
        "gtceu:shaped/saw_bismuth_bronze",
        "gtceu:shaped/saw_black_steel",
        "gtceu:shaped/saw_bronze",
        "gtceu:shaped/saw_steel",
        "gtceu:shaped/screwdriver_bismuth_bronze",
        "gtceu:shaped/screwdriver_black_steel",
        "gtceu:shaped/screwdriver_bronze",
        "gtceu:shaped/screwdriver_red_steel",
        "gtceu:shaped/screwdriver_wrought_iron",
        "gtceu:shaped/scythe_black_bronze",
        "gtceu:shaped/scythe_blue_steel",
        "gtceu:shaped/scythe_bronze",
        "gtceu:shaped/scythe_duranium",
        "gtceu:shaped/scythe_naquadah_alloy",
        "gtceu:shaped/scythe_ostrum_iodide",
        "gtceu:shaped/scythe_steel",
        "gtceu:shaped/scythe_vanadium_steel",
        "gtceu:shaped/shovel_bismuth_bronze",
        "gtceu:shaped/shovel_black_steel",
        "gtceu:shaped/shovel_bronze",
        "gtceu:shaped/shovel_red_steel",
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
        "tfg:polishing/nether_quartz_rod",
        "tfg:vi/lathe/nether_quartz_to_rod"
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
          "text": "SiO₂"
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
      "id": "gtceu:nether_star_block",
      "namespace": "gtceu",
      "display_name": "Block of Nether Star",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:storage_blocks",
        "forge:storage_blocks/nether_star",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:cutting",
        "greate:milling",
        "greate:pressing",
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "greate:cutting": 3,
        "greate:milling": 1,
        "greate:pressing": 1,
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {
        "greate:compacting": 1
      },
      "recipe_ingredient_count": 8,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_nether_star_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_nether_star_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_nether_star_block_to_plate_water",
        "greate:milling/integration/gtceu/macerator/macerate_nether_star_block",
        "greate:pressing/unpacking_nether_star_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/nether_star_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:nether_star_block",
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
          "forge:storage_blocks/nether_star",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_diamond_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
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
          "value": "diamond",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_diamond_tool"
        },
        "processing_in": {
          "values": [
            "greate:cutting",
            "greate:milling",
            "greate:pressing",
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:nether_star_dust",
      "namespace": "gtceu",
      "display_name": "Nether Star Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/nether_star"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "greate:milling": 3,
        "tfc:quern": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 6,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_nether_star",
        "gtceu:shaped/small_dust_disassembling_nether_star",
        "gtceu:shaped/tiny_dust_disassembling_3x3_nether_star",
        "gtceu:shaped/tiny_dust_disassembling_nether_star"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_nether_star",
        "greate:milling/integration/gtceu/macerator/macerate_nether_star_block",
        "greate:milling/integration/gtceu/macerator/macerate_nether_star_plate",
        "gtceu:shaped/small_dust_assembling_nether_star",
        "gtceu:shaped/tiny_dust_assembling_nether_star",
        "tfg:quern/nether_star_gem_to_dust"
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
            "gtceu:crafting_shaped_strict"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:nether_star_lens",
      "namespace": "gtceu",
      "display_name": "Nether Star Lens",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:lenses",
        "forge:lenses/nether_star",
        "forge:lenses/white",
        "tfc:usable_on_tool_rack"
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
        "greate:milling/integration/gtceu/macerator/macerate_nether_star_lens"
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
      "id": "gtceu:nether_star_plate",
      "namespace": "gtceu",
      "display_name": "Nether Star Plate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:plates",
        "forge:plates/nether_star",
        "forge:sheets/nether_star",
        "tfc:pileable_sheets"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "greate:cutting": 3
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_nether_star_plate"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_nether_star_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_nether_star_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_nether_star_block_to_plate_water"
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:netherite_dust",
      "namespace": "gtceu",
      "display_name": "Netherite Dust",
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
        "gtceu:material_item"
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
          "source": "rule:form_from_id",
          "rationale": "suffix _dust"
        }
      }
    },
    {
      "id": "gtceu:netherite_nugget",
      "namespace": "gtceu",
      "display_name": "Netherite Nugget",
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
        "gtceu:material_item"
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
          "source": "rule:form_from_id",
          "rationale": "suffix _nugget"
        }
      }
    },
    {
      "id": "gtceu:netherrack_dust",
      "namespace": "gtceu",
      "display_name": "Keratophyre Dust",
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
        "gtceu:material_item"
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
          "source": "rule:form_from_id",
          "rationale": "suffix _dust"
        }
      }
    },
    {
      "id": "gtceu:network_switch",
      "namespace": "gtceu",
      "display_name": "Network Switch",
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
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_network_switch"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/network_switch",
        "block/machine/network_switch",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:network_switch",
        "block_tags": [
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ethernet Hub"
        },
        {
          "source": "runtime-tooltip",
          "text": "Used to route and distribute §fComputation§7."
        },
        {
          "source": "runtime-tooltip",
          "text": "Can combine any number of Computation §fReceivers§7 into any number of Computation §fTransmitters§7."
        },
        {
          "source": "runtime-tooltip",
          "text": "Uses §f7,680 EU/t§7 per Computation Data Hatch."
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
      "id": "gtceu:neuro_processing_unit",
      "namespace": "gtceu",
      "display_name": "Neuro Processing Unit",
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
        "item/neuro_processing_unit",
        "item/generated"
      ],
      "creative_tabs": [
        "gtceu:item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§7Neuro CPU"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        },
        {
          "source": "lang",
          "key": "item.gtceu.neuro_processing_unit.tooltip",
          "text": "Neuro CPU"
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
        }
      }
    },
    {
      "id": "gtceu:neutron_reflector",
      "namespace": "gtceu",
      "display_name": "Iridium Neutron Reflector",
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
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_neutron_reflector"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/neutron_reflector",
        "item/generated"
      ],
      "creative_tabs": [
        "gtceu:item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§7Indestructible"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        },
        {
          "source": "lang",
          "key": "item.gtceu.neutron_reflector.tooltip",
          "text": "Indestructible"
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
      "id": "gtceu:neutronium_alloy",
      "namespace": "gtceu",
      "display_name": "Neutronium Alloy",
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
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Nt"
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
        }
      }
    },
    {
      "id": "gtceu:neutronium_block",
      "namespace": "gtceu",
      "display_name": "Block of Neutronium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:storage_blocks",
        "forge:storage_blocks/neutronium",
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
      "recipe_production_by_type": {
        "greate:compacting": 1
      },
      "recipe_ingredient_count": 7,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_neutronium_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_neutronium_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_neutronium_block_to_plate_water",
        "greate:milling/integration/gtceu/macerator/macerate_neutronium_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/neutronium_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:neutronium_block",
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
          "forge:needs_duranium_tool",
          "forge:storage_blocks",
          "forge:storage_blocks/neutronium",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Nt"
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
      "id": "gtceu:neutronium_bolt",
      "namespace": "gtceu",
      "display_name": "Neutronium Bolt",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:bolts",
        "forge:bolts/neutronium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "tfc:advanced_shaped_crafting",
        "vintageimprovements:polishing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "crafting_shapeless": 1,
        "greate:milling": 1,
        "tfc:advanced_shaped_crafting": 19,
        "vintageimprovements:polishing": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "greate:cutting": 6
      },
      "recipe_ingredient_count": 24,
      "recipe_output_count": 7,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_neutronium_bolt",
        "gtceu:shaped/screw_neutronium",
        "gtceu:shaped/wrench_neutronium",
        "tfchotornot:crafting/tongs/bismuth",
        "tfchotornot:crafting/tongs/bismuth_bronze",
        "tfchotornot:crafting/tongs/black_bronze",
        "tfchotornot:crafting/tongs/black_steel",
        "tfchotornot:crafting/tongs/blue_steel",
        "tfchotornot:crafting/tongs/brass",
        "tfchotornot:crafting/tongs/bronze",
        "tfchotornot:crafting/tongs/copper",
        "tfchotornot:crafting/tongs/gold",
        "tfchotornot:crafting/tongs/nickel",
        "tfchotornot:crafting/tongs/red_steel",
        "tfchotornot:crafting/tongs/rose_gold",
        "tfchotornot:crafting/tongs/silver",
        "tfchotornot:crafting/tongs/steel",
        "tfchotornot:crafting/tongs/sterling_silver",
        "tfchotornot:crafting/tongs/tin",
        "tfchotornot:crafting/tongs/wrought_iron",
        "tfchotornot:crafting/tongs/zinc",
        "tfg:create/shaped/clipboard",
        "tfg:create/shapeless/minecart_coupling",
        "tfg:vi/lathe/neutronium_bolt_to_screw"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_neutronium_rod_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_neutronium_rod_to_bolt_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_neutronium_rod_to_bolt_water",
        "greate:cutting/integration/gtceu/cutter/cut_neutronium_screw_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_neutronium_screw_to_bolt_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_neutronium_screw_to_bolt_water",
        "gtceu:shaped/bolt_saw_neutronium"
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
          "text": "Nt"
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
            "crafting",
            "greate:milling",
            "tfc:advanced_shaped_crafting",
            "vintageimprovements:polishing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:neutronium_boule",
      "namespace": "gtceu",
      "display_name": "Neutronium-doped Monocrystalline Silicon Boule",
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
        "item/neutronium_boule",
        "item/generated"
      ],
      "creative_tabs": [
        "gtceu:item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§7Raw Circuit"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        },
        {
          "source": "lang",
          "key": "item.gtceu.neutronium_boule.tooltip",
          "text": "Raw Circuit"
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
        }
      }
    },
    {
      "id": "gtceu:neutronium_bucket",
      "namespace": "gtceu",
      "display_name": "Liquid Neutronium Bucket",
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
          "text": "Nt"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 100,000 K"
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
      "id": "gtceu:neutronium_butchery_knife",
      "namespace": "gtceu",
      "display_name": "Neutronium Butchery Knife",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:tools/butchery_knives",
        "tfc:deals_slashing_damage",
        "tfc:usable_on_tool_rack"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:advanced_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "gtceu:shaped/butchery_knife_neutronium"
      ],
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
          "text": "§cButcher:§r Butchers animals for more meat"
        },
        {
          "source": "runtime-tooltip",
          "text": "103.5 §cAttack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "2.7 §9Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Usable as: §fButchery Knife"
        },
        {
          "source": "runtime-tooltip",
          "text": "Unbreakable"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Deals §fSlashing§7 Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 65534,
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
      "id": "gtceu:neutronium_butchery_knife_head",
      "namespace": "gtceu",
      "display_name": "Neutronium Butchery Knife Head",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:butchery_knife_heads",
        "forge:butchery_knife_heads/neutronium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:advanced_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:advanced_shapeless_crafting": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:shaped/butchery_knife_neutronium"
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
          "text": "Nt"
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
            "tfc:advanced_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:neutronium_buzz_saw_blade",
      "namespace": "gtceu",
      "display_name": "Neutronium Buzzsaw Blade",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:buzz_saw_heads",
        "forge:buzz_saw_heads/neutronium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 4
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 4,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "domum_ornamentum:architectscutter",
        "framedblocks:framing_saw",
        "gtceu:shaped/neutronium_zpm_buzzsaw",
        "tfg:shaped/stonecutter"
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
          "text": "Nt"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 4,
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
            "crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:neutronium_chainsaw_head",
      "namespace": "gtceu",
      "display_name": "Neutronium Chainsaw Head",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:chainsaw_heads",
        "forge:chainsaw_heads/neutronium"
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
        "greate:milling/integration/gtceu/macerator/macerate_neutronium_chainsaw_head",
        "gtceu:shaped/neutronium_zpm_chainsaw"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/chainsaw_head_neutronium"
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
          "text": "Nt"
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
      "id": "gtceu:neutronium_crowbar",
      "namespace": "gtceu",
      "display_name": "Neutronium Crowbar",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:tools/crowbars",
        "gtceu:tools/crafting_crowbars",
        "tfc:deals_piercing_damage",
        "tfc:usable_on_tool_rack"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 16
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 16,
      "recipe_ingredient_examples": [
        "gtceu:shaped/maintenance_hatch"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/crowbar_neutronium_black",
        "gtceu:shaped/crowbar_neutronium_blue",
        "gtceu:shaped/crowbar_neutronium_brown",
        "gtceu:shaped/crowbar_neutronium_cyan",
        "gtceu:shaped/crowbar_neutronium_gray",
        "gtceu:shaped/crowbar_neutronium_green",
        "gtceu:shaped/crowbar_neutronium_light_blue",
        "gtceu:shaped/crowbar_neutronium_light_gray",
        "gtceu:shaped/crowbar_neutronium_lime",
        "gtceu:shaped/crowbar_neutronium_magenta",
        "gtceu:shaped/crowbar_neutronium_orange",
        "gtceu:shaped/crowbar_neutronium_pink",
        "gtceu:shaped/crowbar_neutronium_purple",
        "gtceu:shaped/crowbar_neutronium_red",
        "gtceu:shaped/crowbar_neutronium_white",
        "gtceu:shaped/crowbar_neutronium_yellow"
      ],
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
          "text": "§8Dismounts Covers"
        },
        {
          "source": "runtime-tooltip",
          "text": "184 §dMining Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eHarvest Level 6 §f(§cNeutronium§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eRailroad Engineer: §fRotates Rails"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Usable as: §fCrowbar"
        },
        {
          "source": "runtime-tooltip",
          "text": "Unbreakable"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Deals §fPiercing§7 Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 65534,
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
      "id": "gtceu:neutronium_drill_head",
      "namespace": "gtceu",
      "display_name": "Neutronium Drill Head",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:drill_heads",
        "forge:drill_heads/neutronium"
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
        "greate:milling/integration/gtceu/macerator/macerate_neutronium_drill_head",
        "gtceu:shaped/neutronium_zpm_drill"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/drill_head_neutronium"
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
          "text": "Nt"
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
            "crafting",
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