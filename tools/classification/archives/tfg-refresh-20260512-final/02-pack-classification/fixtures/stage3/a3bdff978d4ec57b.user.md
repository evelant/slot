# Items to classify
{
  "items": [
    {
      "id": "gtceu:green_sand_copper_ore",
      "namespace": "gtceu",
      "display_name": "Green Copper Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/copper",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_copper_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_copper_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_copper_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_copper_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/copper",
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
          "text": "Cu"
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
          "value": "copper",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_copper_ore"
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
      "id": "gtceu:green_sand_desh_ore",
      "namespace": "gtceu",
      "display_name": "Green Desh Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "ad_astra:desh_ores",
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/desh",
        "forge:ores_in_ground/green_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_desh_ore_to_crushed_ore"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_desh_ore",
        "block_tags": [
          "ad_astra:desh_ores",
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/desh",
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
          "text": "(MgFe(SiO₂)₂)₂(FeTiO₃)N₄"
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
          "value": "desh",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_desh_ore"
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
            "greate:milling"
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
      "id": "gtceu:green_sand_diamond_ore",
      "namespace": "gtceu",
      "display_name": "Green Diamond Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/diamond",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_diamond_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_diamond_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_diamond_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_diamond_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/diamond",
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
          "text": "C"
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
          "value": "diamond",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_diamond_ore"
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
      "id": "gtceu:green_sand_diatomite_ore",
      "namespace": "gtceu",
      "display_name": "Green Diatomite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/diatomite",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_diatomite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_diatomite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_diatomite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_diatomite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/diatomite",
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
          "text": "(SiO₂)₈(Fe₂O₃)(Al₂O₃)"
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
          "value": "diatomite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_diatomite_ore"
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
      "id": "gtceu:green_sand_electrotine_ore",
      "namespace": "gtceu",
      "display_name": "Green Electrotine Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/electrotine",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_electrotine_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_electrotine_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_electrotine_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_electrotine_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/electrotine",
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
          "text": "(Si(FeS₂)₅(CrAl₂O₃)Hg₃)(AgAu)"
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
          "value": "electrotine",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_electrotine_ore"
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
      "id": "gtceu:green_sand_emerald_ore",
      "namespace": "gtceu",
      "display_name": "Green Emerald Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/emerald",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_emerald_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_emerald_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_emerald_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_emerald_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/emerald",
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
          "text": "Be₃Al₂Si₆O₁₈"
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
          "value": "emerald",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_emerald_ore"
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
      "id": "gtceu:green_sand_fullers_earth_ore",
      "namespace": "gtceu",
      "display_name": "Green Fuller's Earth Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/fullers_earth",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_fullers_earth_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_fullers_earth_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_fullers_earth_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_fullers_earth_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/fullers_earth",
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
          "text": "Mg₂Si₄O₁₄H₄(H₂O)"
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
          "value": "fullers_earth",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_fullers_earth_ore"
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
      "id": "gtceu:green_sand_galena_ore",
      "namespace": "gtceu",
      "display_name": "Green Galena Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/galena",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_galena_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_galena_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_galena_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_galena_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/galena",
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
          "text": "PbS"
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
          "value": "galena",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_galena_ore"
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
      "id": "gtceu:green_sand_garnet_sand_ore",
      "namespace": "gtceu",
      "display_name": "Green Garnet Sand Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/garnet_sand",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_garnet_sand_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_garnet_sand_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_garnet_sand_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_garnet_sand_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/garnet_sand",
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
          "text": "(Al₂Fe₃Si₃O₁₂)(Ca₃Fe₂Si₃O₁₂)(Ca₃Al₂Si₃O₁₂)(Al₂Mg₃Si₃O₁₂)(Al₂Mn₃Si₃O₁₂)(Ca₃Cr₂Si₃O₁₂)"
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
          "value": "garnet_sand",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_garnet_sand_ore"
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
      "id": "gtceu:green_sand_garnierite_ore",
      "namespace": "gtceu",
      "display_name": "Green Garnierite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/garnierite",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_garnierite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_garnierite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_garnierite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_garnierite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/garnierite",
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
          "text": "NiO"
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
          "value": "garnierite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_garnierite_ore"
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
      "id": "gtceu:green_sand_glauconite_sand_ore",
      "namespace": "gtceu",
      "display_name": "Green Glauconite Sand Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/glauconite_sand",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_glauconite_sand_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_glauconite_sand_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_glauconite_sand_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_glauconite_sand_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/glauconite_sand",
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
          "text": "KMg₂Al₂Si₃O₁₂H₂(H₂O)"
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
          "value": "glauconite_sand",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_glauconite_sand_ore"
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
      "id": "gtceu:green_sand_goethite_ore",
      "namespace": "gtceu",
      "display_name": "Green Goethite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/goethite",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_goethite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_goethite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_goethite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_goethite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/goethite",
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
          "value": "goethite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_goethite_ore"
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
      "id": "gtceu:green_sand_gold_ore",
      "namespace": "gtceu",
      "display_name": "Green Gold Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/gold",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_gold_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_gold_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_gold_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_gold_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/gold",
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
          "text": "Au"
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
          "value": "gold",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_gold_ore"
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
      "id": "gtceu:green_sand_granitic_mineral_sand_ore",
      "namespace": "gtceu",
      "display_name": "Green Granitic Mineral Sand Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/granitic_mineral_sand",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_granitic_mineral_sand_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_granitic_mineral_sand_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_granitic_mineral_sand_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_granitic_mineral_sand_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/granitic_mineral_sand",
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
          "text": "(Fe₃O₄)((SiO₂)₄(KMg₃Al₃F₂Si₃O₁₀))"
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
          "value": "granitic_mineral_sand",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_granitic_mineral_sand_ore"
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
      "id": "gtceu:green_sand_graphite_ore",
      "namespace": "gtceu",
      "display_name": "Green Graphite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/graphite",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_graphite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_graphite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_graphite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_graphite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/graphite",
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
          "text": "C"
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
          "value": "graphite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_graphite_ore"
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
      "id": "gtceu:green_sand_green_sapphire_ore",
      "namespace": "gtceu",
      "display_name": "Green Green Sapphire Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/green_sapphire",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_green_sapphire_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_green_sapphire_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_green_sapphire_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_green_sapphire_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/green_sapphire",
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
          "rationale": "ore id green_sand_green_sapphire_ore"
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
      "id": "gtceu:green_sand_grossular_ore",
      "namespace": "gtceu",
      "display_name": "Green Grossular Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/grossular",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_grossular_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_grossular_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_grossular_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_grossular_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/grossular",
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
        "material_family": {
          "value": "grossular",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_grossular_ore"
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
      "id": "gtceu:green_sand_gypsum_ore",
      "namespace": "gtceu",
      "display_name": "Green Gypsum Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/gypsum",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_gypsum_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_gypsum_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_gypsum_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_gypsum_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/gypsum",
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
          "text": "CaS(H₂O)₂O₄"
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
          "value": "gypsum",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_gypsum_ore"
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
      "id": "gtceu:green_sand_hematite_ore",
      "namespace": "gtceu",
      "display_name": "Green Hematite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/hematite",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_hematite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_hematite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_hematite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_hematite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/hematite",
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
          "text": "Fe₂O₃"
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
          "value": "hematite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_hematite_ore"
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
      "id": "gtceu:green_sand_ilmenite_ore",
      "namespace": "gtceu",
      "display_name": "Green Ilmenite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/ilmenite",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_ilmenite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_ilmenite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_ilmenite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_ilmenite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/ilmenite",
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
          "text": "FeTiO₃"
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
          "value": "ilmenite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_ilmenite_ore"
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
      "id": "gtceu:green_sand_iron_ore",
      "namespace": "gtceu",
      "display_name": "Green Iron Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/iron",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_iron_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_iron_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_iron_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_iron_ore",
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Fe"
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
          "value": "iron",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_iron_ore"
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
      "id": "gtceu:green_sand_kyanite_ore",
      "namespace": "gtceu",
      "display_name": "Green Kyanite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/kyanite",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_kyanite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_kyanite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_kyanite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/kyanite",
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
          "text": "Al₂SiO₅"
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
          "value": "kyanite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_kyanite_ore"
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
      "id": "gtceu:green_sand_lapis_ore",
      "namespace": "gtceu",
      "display_name": "Green Lapis Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/lapis",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_lapis_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_lapis_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_lapis_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_lapis_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/lapis",
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
          "text": "(Al₆Si₆Ca₈Na₈)₁₂(Al₃Si₃Na₄Cl)₂(FeS₂)(CaCO₃)"
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
          "value": "lapis",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_lapis_ore"
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
      "id": "gtceu:green_sand_lazurite_ore",
      "namespace": "gtceu",
      "display_name": "Green Lazurite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/lazurite",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_lazurite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_lazurite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_lazurite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_lazurite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/lazurite",
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
          "text": "Al₆Si₆Ca₈Na₈"
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
          "value": "lazurite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_lazurite_ore"
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
      "id": "gtceu:green_sand_lead_ore",
      "namespace": "gtceu",
      "display_name": "Green Lead Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/lead",
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_lead_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_lead_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_lead_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:green_sand_lead_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/lead",
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
          "text": "Pb"
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
          "value": "lead",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_lead_ore"
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