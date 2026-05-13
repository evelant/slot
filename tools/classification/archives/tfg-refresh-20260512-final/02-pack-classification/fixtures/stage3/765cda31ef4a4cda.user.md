# Items to classify
{
  "items": [
    {
      "id": "gtceu:green_sand_tin_ore",
      "namespace": "gtceu",
      "display_name": "Green Tin Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/tin",
        "forge:ores_in_ground/green_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_tin_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_tin_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_tin_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_tin_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/tin",
          "forge:ores_in_ground/green_sand",
          "minecraft:mineable/shovel",
          "minecraft:needs_stone_tool",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:can_landslide",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfc:prospectable"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Sn"
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
          "value": "tin",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_tin_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:green_sand_topaz_ore",
      "namespace": "gtceu",
      "display_name": "Green Topaz Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/topaz",
        "forge:ores_in_ground/green_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_topaz_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_topaz_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_topaz_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_topaz_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/topaz",
          "forge:ores_in_ground/green_sand",
          "minecraft:mineable/shovel",
          "minecraft:needs_stone_tool",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:can_landslide",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfc:prospectable"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al₂SiO₅FH"
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
          "value": "topaz",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_topaz_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:green_sand_tricalcium_phosphate_ore",
      "namespace": "gtceu",
      "display_name": "Green Tricalcium Phosphate Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/tricalcium_phosphate",
        "forge:ores_in_ground/green_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_tricalcium_phosphate_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_tricalcium_phosphate_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_tricalcium_phosphate_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/tricalcium_phosphate",
          "forge:ores_in_ground/green_sand",
          "minecraft:mineable/shovel",
          "minecraft:needs_stone_tool",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:can_landslide",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfc:prospectable"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ca₃(PO₄)₂"
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
          "value": "tricalcium_phosphate",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_tricalcium_phosphate_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:green_sand_trona_ore",
      "namespace": "gtceu",
      "display_name": "Green Trona Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/trona",
        "forge:ores_in_ground/green_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_trona_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_trona_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_trona_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_trona_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/trona",
          "forge:ores_in_ground/green_sand",
          "minecraft:mineable/shovel",
          "minecraft:needs_stone_tool",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:can_landslide",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfc:prospectable"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Na₃C₂H(H₂O)₂O₆"
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
          "value": "trona",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_trona_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:green_sand_tungstate_ore",
      "namespace": "gtceu",
      "display_name": "Green Tungstate Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/tungstate",
        "forge:ores_in_ground/green_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_tungstate_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_tungstate_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_tungstate_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_tungstate_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/tungstate",
          "forge:ores_in_ground/green_sand",
          "minecraft:mineable/shovel",
          "minecraft:needs_stone_tool",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:can_landslide",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfc:prospectable"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Li₂(WO₃)O"
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
          "value": "tungstate",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_tungstate_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:green_sand_uraninite_ore",
      "namespace": "gtceu",
      "display_name": "Green Uraninite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/uraninite",
        "forge:ores_in_ground/green_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_uraninite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_uraninite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_uraninite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_uraninite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/uraninite",
          "forge:ores_in_ground/green_sand",
          "minecraft:mineable/shovel",
          "minecraft:needs_stone_tool",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:can_landslide",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfc:prospectable"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "UO₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
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
          "value": "uraninite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_uraninite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:green_sand_vanadium_magnetite_ore",
      "namespace": "gtceu",
      "display_name": "Green Vanadium Magnetite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/vanadium_magnetite",
        "forge:ores_in_ground/green_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_vanadium_magnetite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_vanadium_magnetite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_vanadium_magnetite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_vanadium_magnetite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/vanadium_magnetite",
          "forge:ores_in_ground/green_sand",
          "minecraft:mineable/shovel",
          "minecraft:needs_stone_tool",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:can_landslide",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfc:prospectable"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "(Fe₃O₄)V"
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
          "value": "vanadium_magnetite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_vanadium_magnetite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:green_sand_wulfenite_ore",
      "namespace": "gtceu",
      "display_name": "Green Wulfenite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/wulfenite",
        "forge:ores_in_ground/green_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_wulfenite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_wulfenite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_wulfenite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_wulfenite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/wulfenite",
          "forge:ores_in_ground/green_sand",
          "minecraft:mineable/shovel",
          "minecraft:needs_stone_tool",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:can_landslide",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfc:prospectable"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "PbMoO₄"
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
          "value": "wulfenite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_wulfenite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:green_sand_yellow_garnet_ore",
      "namespace": "gtceu",
      "display_name": "Green Yellow Garnet Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/yellow_garnet",
        "forge:ores_in_ground/green_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_yellow_garnet_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_yellow_garnet_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_yellow_garnet_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_yellow_garnet_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/yellow_garnet",
          "forge:ores_in_ground/green_sand",
          "minecraft:mineable/shovel",
          "minecraft:needs_stone_tool",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:can_landslide",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfc:prospectable"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "(Ca₃Fe₂Si₃O₁₂)₅(Ca₃Al₂Si₃O₁₂)₈(Ca₃Cr₂Si₃O₁₂)₃"
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
          "value": "yellow_garnet",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_yellow_garnet_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:green_sand_yellow_limonite_ore",
      "namespace": "gtceu",
      "display_name": "Green Limonite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/yellow_limonite",
        "forge:ores_in_ground/green_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_yellow_limonite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_yellow_limonite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_yellow_limonite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_yellow_limonite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/yellow_limonite",
          "forge:ores_in_ground/green_sand",
          "minecraft:mineable/shovel",
          "minecraft:needs_stone_tool",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:can_landslide",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfc:prospectable"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "FeHO₂"
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
          "value": "yellow_limonite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_yellow_limonite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:green_sand_zeolite_ore",
      "namespace": "gtceu",
      "display_name": "Green Zeolite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/zeolite",
        "forge:ores_in_ground/green_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_zeolite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_zeolite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_zeolite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_zeolite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/zeolite",
          "forge:ores_in_ground/green_sand",
          "minecraft:mineable/shovel",
          "minecraft:needs_stone_tool",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:can_landslide",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfc:prospectable"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Na₂Al₂Si₃O₁₀(H₂O)₂"
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
          "value": "zeolite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_zeolite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:green_sapphire_block",
      "namespace": "gtceu",
      "display_name": "Block of Green Sapphire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:storage_blocks",
        "forge:storage_blocks/green_sapphire",
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
        "greate:cutting/integration/gtceu/cutter/cut_green_sapphire_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_green_sapphire_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_green_sapphire_block_to_plate_water",
        "greate:milling/integration/gtceu/macerator/macerate_green_sapphire_block",
        "greate:pressing/unpacking_green_sapphire_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/green_sapphire_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sapphire_block",
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
          "forge:storage_blocks/green_sapphire",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al₂O₃"
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
      "id": "gtceu:green_sapphire_bud_indicator",
      "namespace": "gtceu",
      "display_name": "Green Sapphire Surface Bud",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:bud_indicators",
        "forge:bud_indicators/green_sapphire"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "minecraft:shapeless/green_sapphire_bud_indicator"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sapphire_bud_indicator",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "forge:bud_indicators",
          "forge:bud_indicators/green_sapphire",
          "minecraft:mineable/pickaxe",
          "tfc:can_be_ice_piled",
          "tfc:can_be_snow_piled",
          "tfccanes:not_slowed_with_cane",
          "tfg:bud_ore_indicators",
          "tfg:not_slowed_with_snowshoes"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al₂O₃"
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
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        }
      }
    },
    {
      "id": "gtceu:green_sapphire_dust",
      "namespace": "gtceu",
      "display_name": "Green Sapphire Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/green_sapphire",
        "tfg:aluminium_oxide"
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
        "crafting_shaped": 2,
        "crafting_shapeless": 1,
        "greate:milling": 7,
        "greate:pressing": 1,
        "greate:splashing": 4,
        "tfc:barrel_instant": 2,
        "tfc:quern": 1,
        "vintageimprovements:centrifugation": 2
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 22,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_green_sapphire",
        "gtceu:shaped/small_dust_disassembling_green_sapphire",
        "gtceu:shaped/tiny_dust_disassembling_3x3_green_sapphire",
        "gtceu:shaped/tiny_dust_disassembling_green_sapphire"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerate_green_sapphire_refined_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_exquisite_green_sapphire_gem",
        "greate:milling/integration/gtceu/macerator/macerate_flawless_green_sapphire_gem",
        "greate:milling/integration/gtceu/macerator/macerate_green_sapphire_block",
        "greate:milling/integration/gtceu/macerator/macerate_green_sapphire_gem",
        "greate:milling/integration/gtceu/macerator/macerate_green_sapphire_plate",
        "greate:milling/integration/gtceu/macerator/macerate_green_sapphire_refined_ore_to_dust",
        "greate:pressing/refined_green_sapphire_to_dust",
        "gtceu:shaped/small_dust_assembling_green_sapphire",
        "gtceu:shaped/tiny_dust_assembling_green_sapphire",
        "gtceu:shapeless/centrifuged_ore_to_dust_green_sapphire",
        "tfg:ae_transform/green_sapphire_dust_from_impure",
        "tfg:ae_transform/green_sapphire_dust_from_pure",
        "tfg:instant_barrel/green_sapphire_dust_from_impure",
        "tfg:instant_barrel/green_sapphire_dust_from_pure",
        "tfg:quern/green_sapphire_gem_to_dust",
        "tfg:splashing/green_sapphire_dust_from_impure_distilled",
        "tfg:splashing/green_sapphire_dust_from_impure_water",
        "tfg:splashing/green_sapphire_dust_from_pure_distilled",
        "tfg:splashing/green_sapphire_dust_from_pure_water",
        "tfg:vi/centrifuge/green_sapphire_dust_from_impure",
        "tfg:vi/centrifuge/green_sapphire_dust_from_pure"
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
          "text": "Al₂O₃"
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
      "id": "gtceu:green_sapphire_gem",
      "namespace": "gtceu",
      "display_name": "Green Sapphire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:gems",
        "forge:gems",
        "forge:gems/green_sapphire",
        "wan_ancient_beasts:snatcher_steals"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:sandpaper_polishing",
        "greate:compacting",
        "greate:cutting",
        "greate:milling",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:polishing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "create:sandpaper_polishing": 1,
        "greate:compacting": 1,
        "greate:cutting": 3,
        "greate:milling": 1,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:polishing": 1
      },
      "recipe_production_by_type": {
        "blasting": 42,
        "crafting_shapeless": 1,
        "create:sandpaper_polishing": 3,
        "greate:cutting": 3,
        "greate:pressing": 4,
        "smelting": 43
      },
      "recipe_ingredient_count": 10,
      "recipe_output_count": 96,
      "recipe_ingredient_examples": [
        "constructionwand:shaped/iron_wand",
        "greate:compacting/green_sapphire_block",
        "greate:cutting/integration/gtceu/cutter/cut_green_sapphire_gem_to_flawed_gem",
        "greate:cutting/integration/gtceu/cutter/cut_green_sapphire_gem_to_flawed_gem_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_green_sapphire_gem_to_flawed_gem_water",
        "greate:milling/integration/gtceu/macerator/macerate_green_sapphire_gem",
        "gtceu:shapeless/gem_to_gem_flawed_gem_green_sapphire",
        "minecraft:shapeless/green_sapphire_bud_indicator",
        "tfg:polishing/green_sapphire_rod",
        "tfg:vi/lathe/green_sapphire_to_rod"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_green_sapphire_flawless_gem_to_gem",
        "greate:cutting/integration/gtceu/cutter/cut_green_sapphire_flawless_gem_to_gem_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_green_sapphire_flawless_gem_to_gem_water",
        "greate:pressing/poor_raw_green_sapphire_to_gem",
        "greate:pressing/raw_green_sapphire_to_gem",
        "greate:pressing/rich_raw_green_sapphire_to_gem",
        "greate:pressing/unpacking_green_sapphire_block",
        "gtceu:blasting/smelt_andesite_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_basalt_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_black_sand_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_brown_sand_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_chalk_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_chert_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_claystone_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_conglomerate_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_dacite_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_deepslate_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_diorite_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_dolomite_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_dripstone_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_flavolite_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_gabbro_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_glacio_stone_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_gneiss_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_granite_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_green_sand_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_limestone_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_marble_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_mars_stone_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_mercury_stone_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_moon_deepslate_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_moon_stone_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_phyllite_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_pink_sand_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_pyroxenite_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_quartzite_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_raw_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_red_granite_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_red_sand_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_rhyolite_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_sandy_jadestone_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_schist_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_shale_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_slate_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_tuff_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_venus_stone_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_white_sand_green_sapphire_ore_to_ingot",
        "gtceu:blasting/smelt_yellow_sand_green_sapphire_ore_to_ingot",
        "gtceu:shapeless/gem_to_gem_gem_green_sapphire",
        "gtceu:smelting/smelt_andesite_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_basalt_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_black_sand_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_brown_sand_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_chalk_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_chert_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_claystone_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_conglomerate_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_dacite_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_deepslate_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_diorite_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_dolomite_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_dripstone_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_flavolite_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_gabbro_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_glacio_stone_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_gneiss_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_granite_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_limestone_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_marble_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_mars_stone_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_mercury_stone_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_moon_deepslate_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_moon_stone_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_phyllite_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_pink_sand_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_pyroxenite_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_quartzite_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_raw_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_red_granite_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_red_sand_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_rhyolite_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_rich_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_sandy_jadestone_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_schist_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_shale_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_slate_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_tuff_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_venus_stone_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_white_sand_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_yellow_sand_green_sapphire_ore_to_ingot",
        "tfg:polishing/poor_raw_green_sapphire_to_gem",
        "tfg:polishing/raw_green_sapphire_to_gem",
        "tfg:polishing/rich_raw_green_sapphire_to_gem"
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
          "text": "Al₂O₃"
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
          "value": "gem",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:gems"
        },
        "processing_in": {
          "values": [
            "crafting",
            "create:sandpaper_polishing",
            "greate:compacting",
            "greate:cutting",
            "greate:milling",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:polishing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:green_sapphire_indicator",
      "namespace": "gtceu",
      "display_name": "Green Sapphire Surface Rock",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:surface_rocks",
        "forge:surface_rocks/green_sapphire"
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
        "block_id": "gtceu:green_sapphire_indicator",
        "block_tags": [
          "forge:surface_rocks",
          "forge:surface_rocks/green_sapphire",
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
          "text": "Al₂O₃"
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
      "id": "gtceu:green_sapphire_ore",
      "namespace": "gtceu",
      "display_name": "Green Sapphire Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/green_sapphire",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sapphire_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_green_sapphire_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sapphire_ore",
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al₂O₃"
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
          "value": "green_sapphire",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sapphire_ore"
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
      "id": "gtceu:green_sapphire_plate",
      "namespace": "gtceu",
      "display_name": "Green Sapphire Plate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:plates",
        "forge:plates/green_sapphire",
        "forge:sheets/green_sapphire",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sapphire_plate"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_green_sapphire_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_green_sapphire_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_green_sapphire_block_to_plate_water"
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
          "text": "Al₂O₃"
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:green_sapphire_rod",
      "namespace": "gtceu",
      "display_name": "Green Sapphire Rod",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:rods",
        "forge:rods/green_sapphire",
        "tfg:precision_fabricator_holder_rods"
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
        "create:sandpaper_polishing": 1,
        "vintageimprovements:polishing": 1
      },
      "recipe_ingredient_count": 214,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_green_sapphire_rod",
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
        "tfg:polishing/green_sapphire_rod",
        "tfg:vi/lathe/green_sapphire_to_rod"
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
          "text": "Al₂O₃"
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
      "id": "gtceu:green_sapphire_slurry_bucket",
      "namespace": "gtceu",
      "display_name": "Green Sapphire Slurry Bucket",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "ae2:p2p_attunements/fluid_p2p_tunnel",
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
        "gtceu:material_fluid"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
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
      "id": "gtceu:green_studs",
      "namespace": "gtceu",
      "display_name": "Green Studs",
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
        "item/green_studs",
        "block/green_studs",
        "block/cube_all"
      ],
      "creative_tabs": [
        "gtceu:decoration"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "gtceu:blocks/green_studs"
      ],
      "block_context": {
        "block_id": "gtceu:green_studs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "forge:fast_walkable_blocks",
          "forge:needs_wood_tool",
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
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:gregification_hazard_sign_block",
      "namespace": "gtceu",
      "display_name": "Gregification Hazard Sign Block",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 1,
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_gregification_hazard_sign_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "gtceu:shapeless/gregification_hazard_to_steel_solid_casing"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/warning_sign_gregification_hazard"
      ],
      "model_parents": [
        "item/gregification_hazard_sign_block",
        "block/gregification_hazard_sign_block",
        "block/cube_all"
      ],
      "creative_tabs": [
        "gtceu:decoration"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "gtceu:blocks/gregification_hazard_sign_block"
      ],
      "block_context": {
        "block_id": "gtceu:gregification_hazard_sign_block",
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
            "crafting",
            "greate:milling",
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
      "id": "gtceu:grossular_block",
      "namespace": "gtceu",
      "display_name": "Block of Grossular",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:storage_blocks",
        "forge:storage_blocks/grossular",
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
        "greate:cutting/integration/gtceu/cutter/cut_grossular_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_grossular_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_grossular_block_to_plate_water",
        "greate:milling/integration/gtceu/macerator/macerate_grossular_block",
        "greate:pressing/unpacking_grossular_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/grossular_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:grossular_block",
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
          "forge:needs_wood_tool",
          "forge:storage_blocks",
          "forge:storage_blocks/grossular",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ca₃Al₂Si₃O₁₂"
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
      "id": "gtceu:grossular_bud_indicator",
      "namespace": "gtceu",
      "display_name": "Grossular Surface Bud",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:bud_indicators",
        "forge:bud_indicators/grossular"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "minecraft:shapeless/grossular_bud_indicator"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:grossular_bud_indicator",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "forge:bud_indicators",
          "forge:bud_indicators/grossular",
          "minecraft:mineable/pickaxe",
          "tfc:can_be_ice_piled",
          "tfc:can_be_snow_piled",
          "tfccanes:not_slowed_with_cane",
          "tfg:bud_ore_indicators",
          "tfg:not_slowed_with_snowshoes"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ca₃Al₂Si₃O₁₂"
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
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        }
      }
    },
    {
      "id": "gtceu:grossular_dust",
      "namespace": "gtceu",
      "display_name": "Grossular Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/grossular"
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
        "crafting_shaped": 2,
        "crafting_shapeless": 1,
        "greate:milling": 7,
        "greate:pressing": 1,
        "greate:splashing": 4,
        "tfc:barrel_instant": 2,
        "tfc:quern": 1,
        "vintageimprovements:centrifugation": 2
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 22,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_grossular",
        "gtceu:shaped/small_dust_disassembling_grossular",
        "gtceu:shaped/tiny_dust_disassembling_3x3_grossular",
        "gtceu:shaped/tiny_dust_disassembling_grossular"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerate_grossular_refined_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_exquisite_grossular_gem",
        "greate:milling/integration/gtceu/macerator/macerate_flawless_grossular_gem",
        "greate:milling/integration/gtceu/macerator/macerate_grossular_block",
        "greate:milling/integration/gtceu/macerator/macerate_grossular_gem",
        "greate:milling/integration/gtceu/macerator/macerate_grossular_plate",
        "greate:milling/integration/gtceu/macerator/macerate_grossular_refined_ore_to_dust",
        "greate:pressing/refined_grossular_to_dust",
        "gtceu:shaped/small_dust_assembling_grossular",
        "gtceu:shaped/tiny_dust_assembling_grossular",
        "gtceu:shapeless/centrifuged_ore_to_dust_grossular",
        "tfg:ae_transform/grossular_dust_from_impure",
        "tfg:ae_transform/grossular_dust_from_pure",
        "tfg:instant_barrel/grossular_dust_from_impure",
        "tfg:instant_barrel/grossular_dust_from_pure",
        "tfg:quern/grossular_gem_to_dust",
        "tfg:splashing/grossular_dust_from_impure_distilled",
        "tfg:splashing/grossular_dust_from_impure_water",
        "tfg:splashing/grossular_dust_from_pure_distilled",
        "tfg:splashing/grossular_dust_from_pure_water",
        "tfg:vi/centrifuge/grossular_dust_from_impure",
        "tfg:vi/centrifuge/grossular_dust_from_pure"
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
          "text": "Ca₃Al₂Si₃O₁₂"
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