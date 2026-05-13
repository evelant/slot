# Items to classify
{
  "items": [
    {
      "id": "gtceu:calcite_indicator",
      "namespace": "gtceu",
      "display_name": "Calcite Surface Rock",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:surface_rocks",
        "forge:surface_rocks/calcite"
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
        "block_id": "gtceu:calcite_indicator",
        "block_tags": [
          "forge:surface_rocks",
          "forge:surface_rocks/calcite",
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
          "text": "CaCO₃"
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
          "value": "calcite",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix calcite_"
        }
      }
    },
    {
      "id": "gtceu:calcite_ore",
      "namespace": "gtceu",
      "display_name": "Calcite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/calcite",
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
        "greate:milling/integration/gtceu/macerator/macerate_calcite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_calcite_ore_to_ingot",
        "gtceu:smelting/smelt_calcite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:calcite_ore",
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "CaCO₃"
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
          "value": "calcite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id calcite_ore"
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
      "id": "gtceu:calcium_carbonate_dust",
      "namespace": "gtceu",
      "display_name": "Calcium Carbonate Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/calcium_carbonate"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_calcium_carbonate",
        "gtceu:shaped/small_dust_disassembling_calcium_carbonate",
        "gtceu:shaped/tiny_dust_disassembling_3x3_calcium_carbonate",
        "gtceu:shaped/tiny_dust_disassembling_calcium_carbonate"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_calcium_carbonate",
        "gtceu:shaped/tiny_dust_assembling_calcium_carbonate"
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
          "text": "CaCO₃"
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
            "gtceu:crafting_shaped_strict"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:calcium_chloride_dust",
      "namespace": "gtceu",
      "display_name": "Calcium Chloride Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/calcium_chloride"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:mixing",
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "greate:mixing": 2,
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/bauxite_slurry_from_crushed_bauxite",
        "greate:mixing/integration/gtceu/mixer/bauxite_slurry_from_washed_bauxite",
        "gtceu:shaped/small_dust_disassembling_3x3_calcium_chloride",
        "gtceu:shaped/small_dust_disassembling_calcium_chloride",
        "gtceu:shaped/tiny_dust_disassembling_3x3_calcium_chloride",
        "gtceu:shaped/tiny_dust_disassembling_calcium_chloride"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_calcium_chloride",
        "gtceu:shaped/tiny_dust_assembling_calcium_chloride"
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
          "text": "CaCl₂"
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
            "greate:mixing",
            "gtceu:crafting_shaped_strict"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:calcium_dust",
      "namespace": "gtceu",
      "display_name": "Calcium Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/calcium",
        "tfg:resistance_ingredients"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:mixing",
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "greate:mixing": 4,
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 8,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/mercury_barium_calcium_cuprate",
        "greate:mixing/integration/gtceu/mixer/raw_growth_medium",
        "greate:mixing/integration/tfg/gtceu/mixer/combat_powder",
        "greate:mixing/integration/tfg/gtceu/mixer/salvo_resistance",
        "gtceu:shaped/small_dust_disassembling_3x3_calcium",
        "gtceu:shaped/small_dust_disassembling_calcium",
        "gtceu:shaped/tiny_dust_disassembling_3x3_calcium",
        "gtceu:shaped/tiny_dust_disassembling_calcium"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_calcium",
        "gtceu:shaped/tiny_dust_assembling_calcium"
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
          "text": "Ca"
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
            "greate:mixing",
            "gtceu:crafting_shaped_strict"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:calcium_ferrocyanide_dust",
      "namespace": "gtceu",
      "display_name": "Calcium Ferrocyanide Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/calcium_ferrocyanide"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_calcium_ferrocyanide",
        "gtceu:shaped/small_dust_disassembling_calcium_ferrocyanide",
        "gtceu:shaped/tiny_dust_disassembling_3x3_calcium_ferrocyanide",
        "gtceu:shaped/tiny_dust_disassembling_calcium_ferrocyanide"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_calcium_ferrocyanide",
        "gtceu:shaped/tiny_dust_assembling_calcium_ferrocyanide"
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
          "text": "Ca₂[Fe(CN)₆]"
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
            "gtceu:crafting_shaped_strict"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:calcium_hydroxide_dust",
      "namespace": "gtceu",
      "display_name": "Calcium Hydroxide Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/calcium_hydroxide"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_calcium_hydroxide",
        "gtceu:shaped/small_dust_disassembling_calcium_hydroxide",
        "gtceu:shaped/tiny_dust_disassembling_3x3_calcium_hydroxide",
        "gtceu:shaped/tiny_dust_disassembling_calcium_hydroxide"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_calcium_hydroxide",
        "gtceu:shaped/tiny_dust_assembling_calcium_hydroxide"
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
          "text": "Ca(OH)₂"
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
            "gtceu:crafting_shaped_strict"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:calcium_phosphide_dust",
      "namespace": "gtceu",
      "display_name": "Calcium Phosphide Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/calcium_phosphide"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_calcium_phosphide",
        "gtceu:shaped/small_dust_disassembling_calcium_phosphide",
        "gtceu:shaped/tiny_dust_disassembling_3x3_calcium_phosphide",
        "gtceu:shaped/tiny_dust_disassembling_calcium_phosphide"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_calcium_phosphide",
        "gtceu:shaped/tiny_dust_assembling_calcium_phosphide"
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
          "text": "CaP"
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
            "gtceu:crafting_shaped_strict"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:californium_252_block",
      "namespace": "gtceu",
      "display_name": "Block of Californium 252",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:storage_blocks",
        "forge:storage_blocks/californium_252",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {
        "greate:compacting": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_californium_252_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/californium_252_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:californium_252_block",
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
          "forge:storage_blocks/californium_252",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ca²⁵²"
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
        },
        "processing_in": {
          "values": [
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
      "id": "gtceu:californium_252_dust",
      "namespace": "gtceu",
      "display_name": "Californium 252 Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/californium_252"
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
        "greate:milling": 2
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_californium_252",
        "gtceu:shaped/small_dust_disassembling_californium_252",
        "gtceu:shaped/tiny_dust_disassembling_3x3_californium_252",
        "gtceu:shaped/tiny_dust_disassembling_californium_252",
        "gtceu:smelting/smelt_dust_californium_252_to_ingot"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_californium_252_block",
        "greate:milling/integration/gtceu/macerator/macerate_californium_252_ingot",
        "gtceu:shaped/small_dust_assembling_californium_252",
        "gtceu:shaped/tiny_dust_assembling_californium_252"
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
          "text": "Ca²⁵²"
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
      "id": "gtceu:californium_252_ingot",
      "namespace": "gtceu",
      "display_name": "Californium 252 Ingot",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ingots",
        "forge:ingots",
        "forge:ingots/californium_252",
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
        "greate:compacting/californium_252_block",
        "greate:milling/integration/gtceu/macerator/macerate_californium_252_ingot",
        "gtceu:shaped/stick_californium_252",
        "tfg:vi/lathe/californium_252_to_rod"
      ],
      "recipe_output_examples": [
        "gtceu:smelting/smelt_dust_californium_252_to_ingot"
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
          "text": "Ca²⁵²"
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
      "id": "gtceu:californium_252_nugget",
      "namespace": "gtceu",
      "display_name": "Californium 252 Nugget",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:nuggets",
        "forge:nuggets",
        "forge:nuggets/californium_252"
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
        "greate:milling/integration/gtceu/macerator/macerate_californium_252_nugget"
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
          "text": "Ca²⁵²"
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
      "id": "gtceu:californium_252_rod",
      "namespace": "gtceu",
      "display_name": "Californium 252 Rod",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:rods",
        "forge:rods/californium_252"
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
        "crafting_shaped": 1,
        "vintageimprovements:polishing": 1
      },
      "recipe_ingredient_count": 213,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_californium_252_rod",
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
        "gtceu:shaped/stick_californium_252",
        "tfg:vi/lathe/californium_252_to_rod"
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
          "text": "Ca²⁵²"
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
      "id": "gtceu:capacitor",
      "namespace": "gtceu",
      "display_name": "Capacitor",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "gtceu:capacitors"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shaped": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 2,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_capacitor",
        "tfg:shaped/large_steel_boiler"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/capacitor",
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
          "text": "§7Basic Electronic Component"
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
          "key": "item.gtceu.capacitor.tooltip",
          "text": "Basic Electronic Component"
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
            "greate:milling",
            "kubejs:shaped"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:caprolactam_dust",
      "namespace": "gtceu",
      "display_name": "Caprolactam Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/caprolactam"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_caprolactam",
        "gtceu:shaped/small_dust_disassembling_caprolactam",
        "gtceu:shaped/tiny_dust_disassembling_3x3_caprolactam",
        "gtceu:shaped/tiny_dust_disassembling_caprolactam"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_caprolactam",
        "gtceu:shaped/tiny_dust_assembling_caprolactam"
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
          "text": "(CH₂)₅C(O)NH"
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
            "gtceu:crafting_shaped_strict"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:carbon_bucket",
      "namespace": "gtceu",
      "display_name": "Liquid Carbon Bucket",
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
          "text": "C"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 4,600 K"
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
      "id": "gtceu:carbon_dioxide_bucket",
      "namespace": "gtceu",
      "display_name": "Carbon Dioxide Bucket",
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
          "text": "CO₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Gaseous"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 293 K"
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
      "id": "gtceu:carbon_dust",
      "namespace": "gtceu",
      "display_name": "Carbon Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/carbon"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "crafting_shapeless": 1,
        "greate:mixing": 5,
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 12,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/graphene",
        "greate:mixing/integration/gtceu/mixer/gunpowder_from_carbon",
        "greate:mixing/integration/gtceu/mixer/tantalum_carbide",
        "greate:mixing/integration/gtceu/mixer/titanium_carbide",
        "greate:mixing/integration/gtceu/mixer/tungstencarbide",
        "gtceu:shaped/resistor_wire_carbon",
        "gtceu:shaped/resistor_wire_fine_carbon",
        "gtceu:shaped/small_dust_disassembling_3x3_carbon",
        "gtceu:shaped/small_dust_disassembling_carbon",
        "gtceu:shaped/tiny_dust_disassembling_3x3_carbon",
        "gtceu:shaped/tiny_dust_disassembling_carbon",
        "tfg:shapeless/gunpowder_carbon"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_carbon",
        "gtceu:shaped/tiny_dust_assembling_carbon"
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
          "text": "C"
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
            "crafting",
            "greate:mixing",
            "gtceu:crafting_shaped_strict"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:carbon_fiber_mesh",
      "namespace": "gtceu",
      "display_name": "Carbon Fiber Mesh",
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
        "item/carbon_fiber_mesh",
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
      "id": "gtceu:carbon_fiber_plate",
      "namespace": "gtceu",
      "display_name": "Carbon Fiber Plate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 6
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 6,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:shaped/nano_boots",
        "gtceu:shaped/nano_chestplate",
        "gtceu:shaped/nano_helmet",
        "gtceu:shaped/nano_leggings",
        "gtceu:shaped/nano_saber",
        "gtceu:shaped/solar_panel_basic"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/carbon_fiber_plate",
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
            "crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:carbon_fibers",
      "namespace": "gtceu",
      "display_name": "Raw Carbon Fibers",
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
        "item/carbon_fibers",
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
      "id": "gtceu:carbon_monoxide_bucket",
      "namespace": "gtceu",
      "display_name": "Carbon Monoxide Bucket",
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
          "text": "CO"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Gaseous"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 293 K"
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
      "id": "gtceu:cassiterite_dust",
      "namespace": "gtceu",
      "display_name": "Cassiterite Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/cassiterite"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "gtceu:crafting_shaped_strict",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "gtceu:crafting_shaped_strict": 4,
        "smelting": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 2,
        "crafting_shaped": 2,
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "greate:splashing": 44,
        "tfc:barrel_instant": 2,
        "vintageimprovements:centrifugation": 2,
        "vintageimprovements:vibrating": 20
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 76,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/cassiterite_to_powder",
        "gtceu:shaped/small_dust_disassembling_3x3_cassiterite",
        "gtceu:shaped/small_dust_disassembling_cassiterite",
        "gtceu:shaped/tiny_dust_disassembling_3x3_cassiterite",
        "gtceu:shaped/tiny_dust_disassembling_cassiterite",
        "gtceu:smelting/smelt_dust_cassiterite_to_ingot"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerate_cassiterite_refined_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_cassiterite_refined_ore_to_dust",
        "greate:pressing/refined_cassiterite_to_dust",
        "gtceu:shaped/small_dust_assembling_cassiterite",
        "gtceu:shaped/tiny_dust_assembling_cassiterite",
        "gtceu:shapeless/centrifuged_ore_to_dust_cassiterite",
        "tfg:ae_transform/cassiterite_dust_from_impure",
        "tfg:ae_transform/cassiterite_dust_from_pure",
        "tfg:instant_barrel/cassiterite_dust_from_impure",
        "tfg:instant_barrel/cassiterite_dust_from_pure",
        "tfg:splashing/cassiterite/andesite_deposit",
        "tfg:splashing/cassiterite/andesite_deposit_distilled",
        "tfg:splashing/cassiterite/basalt_deposit",
        "tfg:splashing/cassiterite/basalt_deposit_distilled",
        "tfg:splashing/cassiterite/chalk_deposit",
        "tfg:splashing/cassiterite/chalk_deposit_distilled",
        "tfg:splashing/cassiterite/chert_deposit",
        "tfg:splashing/cassiterite/chert_deposit_distilled",
        "tfg:splashing/cassiterite/claystone_deposit",
        "tfg:splashing/cassiterite/claystone_deposit_distilled",
        "tfg:splashing/cassiterite/conglomerate_deposit",
        "tfg:splashing/cassiterite/conglomerate_deposit_distilled",
        "tfg:splashing/cassiterite/dacite_deposit",
        "tfg:splashing/cassiterite/dacite_deposit_distilled",
        "tfg:splashing/cassiterite/diorite_deposit",
        "tfg:splashing/cassiterite/diorite_deposit_distilled",
        "tfg:splashing/cassiterite/dolomite_deposit",
        "tfg:splashing/cassiterite/dolomite_deposit_distilled",
        "tfg:splashing/cassiterite/gabbro_deposit",
        "tfg:splashing/cassiterite/gabbro_deposit_distilled",
        "tfg:splashing/cassiterite/gneiss_deposit",
        "tfg:splashing/cassiterite/gneiss_deposit_distilled",
        "tfg:splashing/cassiterite/granite_deposit",
        "tfg:splashing/cassiterite/granite_deposit_distilled",
        "tfg:splashing/cassiterite/limestone_deposit",
        "tfg:splashing/cassiterite/limestone_deposit_distilled",
        "tfg:splashing/cassiterite/marble_deposit",
        "tfg:splashing/cassiterite/marble_deposit_distilled",
        "tfg:splashing/cassiterite/phyllite_deposit",
        "tfg:splashing/cassiterite/phyllite_deposit_distilled",
        "tfg:splashing/cassiterite/quartzite_deposit",
        "tfg:splashing/cassiterite/quartzite_deposit_distilled",
        "tfg:splashing/cassiterite/rhyolite_deposit",
        "tfg:splashing/cassiterite/rhyolite_deposit_distilled",
        "tfg:splashing/cassiterite/schist_deposit",
        "tfg:splashing/cassiterite/schist_deposit_distilled",
        "tfg:splashing/cassiterite/shale_deposit",
        "tfg:splashing/cassiterite/shale_deposit_distilled",
        "tfg:splashing/cassiterite/slate_deposit",
        "tfg:splashing/cassiterite/slate_deposit_distilled",
        "tfg:splashing/cassiterite_dust_from_impure_distilled",
        "tfg:splashing/cassiterite_dust_from_impure_water",
        "tfg:splashing/cassiterite_dust_from_pure_distilled",
        "tfg:splashing/cassiterite_dust_from_pure_water",
        "tfg:vi/centrifuge/cassiterite_dust_from_impure",
        "tfg:vi/centrifuge/cassiterite_dust_from_pure",
        "tfg:vi/vibrating/deposits/andesite_cassiterite",
        "tfg:vi/vibrating/deposits/basalt_cassiterite",
        "tfg:vi/vibrating/deposits/chalk_cassiterite",
        "tfg:vi/vibrating/deposits/chert_cassiterite",
        "tfg:vi/vibrating/deposits/claystone_cassiterite",
        "tfg:vi/vibrating/deposits/conglomerate_cassiterite",
        "tfg:vi/vibrating/deposits/dacite_cassiterite",
        "tfg:vi/vibrating/deposits/diorite_cassiterite",
        "tfg:vi/vibrating/deposits/dolomite_cassiterite",
        "tfg:vi/vibrating/deposits/gabbro_cassiterite",
        "tfg:vi/vibrating/deposits/gneiss_cassiterite",
        "tfg:vi/vibrating/deposits/granite_cassiterite",
        "tfg:vi/vibrating/deposits/limestone_cassiterite",
        "tfg:vi/vibrating/deposits/marble_cassiterite",
        "tfg:vi/vibrating/deposits/phyllite_cassiterite",
        "tfg:vi/vibrating/deposits/quartzite_cassiterite",
        "tfg:vi/vibrating/deposits/rhyolite_cassiterite",
        "tfg:vi/vibrating/deposits/schist_cassiterite",
        "tfg:vi/vibrating/deposits/shale_cassiterite",
        "tfg:vi/vibrating/deposits/slate_cassiterite"
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
          "text": "SnO₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fTin§7 (at Very Hot§7)"
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
            "greate:milling",
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
      "id": "gtceu:cassiterite_indicator",
      "namespace": "gtceu",
      "display_name": "Cassiterite Surface Rock",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:surface_rocks",
        "forge:surface_rocks/cassiterite"
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
        "block_id": "gtceu:cassiterite_indicator",
        "block_tags": [
          "forge:surface_rocks",
          "forge:surface_rocks/cassiterite",
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
          "text": "SnO₂"
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
      "id": "gtceu:cassiterite_ore",
      "namespace": "gtceu",
      "display_name": "Cassiterite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/cassiterite",
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
        "greate:milling/integration/gtceu/macerator/macerate_cassiterite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_cassiterite_ore_to_ingot",
        "gtceu:smelting/smelt_cassiterite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:cassiterite_ore",
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "SnO₂"
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
          "value": "cassiterite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id cassiterite_ore"
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