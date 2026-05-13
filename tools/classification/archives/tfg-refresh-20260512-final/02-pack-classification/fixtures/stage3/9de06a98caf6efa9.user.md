# Items to classify
{
  "items": [
    {
      "id": "gtceu:red_sand_vanadium_magnetite_ore",
      "namespace": "gtceu",
      "display_name": "Red Vanadium Magnetite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/vanadium_magnetite",
        "forge:ores_in_ground/red_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_red_sand_vanadium_magnetite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_red_sand_vanadium_magnetite_ore_to_ingot",
        "gtceu:smelting/smelt_red_sand_vanadium_magnetite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:red_sand_vanadium_magnetite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/vanadium_magnetite",
          "forge:ores_in_ground/red_sand",
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
          "rationale": "ore id red_sand_vanadium_magnetite_ore"
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
      "id": "gtceu:red_sand_wulfenite_ore",
      "namespace": "gtceu",
      "display_name": "Red Wulfenite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/wulfenite",
        "forge:ores_in_ground/red_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_red_sand_wulfenite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_red_sand_wulfenite_ore_to_ingot",
        "gtceu:smelting/smelt_red_sand_wulfenite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:red_sand_wulfenite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/wulfenite",
          "forge:ores_in_ground/red_sand",
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
          "rationale": "ore id red_sand_wulfenite_ore"
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
      "id": "gtceu:red_sand_yellow_garnet_ore",
      "namespace": "gtceu",
      "display_name": "Red Yellow Garnet Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/yellow_garnet",
        "forge:ores_in_ground/red_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_red_sand_yellow_garnet_ore_to_crushed_ore",
        "gtceu:blasting/smelt_red_sand_yellow_garnet_ore_to_ingot",
        "gtceu:smelting/smelt_red_sand_yellow_garnet_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:red_sand_yellow_garnet_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/yellow_garnet",
          "forge:ores_in_ground/red_sand",
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
          "rationale": "ore id red_sand_yellow_garnet_ore"
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
      "id": "gtceu:red_sand_yellow_limonite_ore",
      "namespace": "gtceu",
      "display_name": "Red Limonite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/yellow_limonite",
        "forge:ores_in_ground/red_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_red_sand_yellow_limonite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_red_sand_yellow_limonite_ore_to_ingot",
        "gtceu:smelting/smelt_red_sand_yellow_limonite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:red_sand_yellow_limonite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/yellow_limonite",
          "forge:ores_in_ground/red_sand",
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
          "rationale": "ore id red_sand_yellow_limonite_ore"
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
      "id": "gtceu:red_sand_zeolite_ore",
      "namespace": "gtceu",
      "display_name": "Red Zeolite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/zeolite",
        "forge:ores_in_ground/red_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_red_sand_zeolite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_red_sand_zeolite_ore_to_ingot",
        "gtceu:smelting/smelt_red_sand_zeolite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:red_sand_zeolite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/zeolite",
          "forge:ores_in_ground/red_sand",
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
          "rationale": "ore id red_sand_zeolite_ore"
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
      "id": "gtceu:red_steel_axe",
      "namespace": "gtceu",
      "display_name": "Red Steel Axe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:tools",
        "minecraft:axes",
        "minecraft:breaks_decorated_pots",
        "minecraft:tools",
        "tfc:axes",
        "tfc:axes_that_log",
        "tfc:deals_slashing_damage",
        "tfc:usable_on_tool_rack"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "tfc:advanced_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "tfg:crafting/strip_hardwood",
        "tfg:sophisticated_backpacks/shaped/tool_swapper_upgrade"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/axe_red_steel"
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
          "text": "7,799 §eTotal Durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "7,800 §bDurability"
        },
        {
          "source": "runtime-tooltip",
          "text": "8 §dMining Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eHarvest Level 3 §f(§bDiamond§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cBrute: §fDisables Shields"
        },
        {
          "source": "runtime-tooltip",
          "text": "§4Lumberjack: §fTree Felling"
        },
        {
          "source": "runtime-tooltip",
          "text": "§5Artisan: §fStrips Logs"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bPolisher: §fRemoves Oxidation"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Cleaner: §fRemoves Wax"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Usable as: §fAxe"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Craft with a Repair Kit to repair 25% durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Deals §fSlashing§7 Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fRed Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 7799,
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
        "form": {
          "value": "tool",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _axe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "tfc:damage_inputs_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:red_steel_axe_head",
      "namespace": "gtceu",
      "display_name": "Red Steel Axe Head",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:axe_heads",
        "forge:axe_heads/red_steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:advanced_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:advanced_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1,
        "vintageimprovements:curving": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "gtceu:shaped/axe_red_steel"
      ],
      "recipe_output_examples": [
        "tfc:anvil/red_steel_axe_head",
        "tfg:vi/curving/red_steel_ingot_to_axe_head"
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
          "text": "(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fRed Steel§7 (at Brilliant White§7)"
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
      "id": "gtceu:red_steel_block",
      "namespace": "gtceu",
      "display_name": "Block of Red Steel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:storage_blocks",
        "forge:storage_blocks/red_steel",
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
        "greate:cutting/integration/gtceu/cutter/cut_red_steel_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_red_steel_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_red_steel_block_to_plate_water",
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/red_steel_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:red_steel_block",
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
          "forge:storage_blocks/red_steel",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_iron_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 1296 mB of §fRed Steel§7 (at Brilliant White§7)"
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
          "value": "iron",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_iron_tool"
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
      "id": "gtceu:red_steel_bolt",
      "namespace": "gtceu",
      "display_name": "Red Steel Bolt",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:bolts",
        "forge:bolts/red_steel"
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
        "greate:cutting": 6,
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 24,
      "recipe_output_count": 8,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_bolt",
        "gtceu:shaped/screw_red_steel",
        "gtceu:shaped/wrench_red_steel",
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
        "tfg:vi/lathe/red_steel_bolt_to_screw"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_red_steel_rod_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_red_steel_rod_to_bolt_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_red_steel_rod_to_bolt_water",
        "greate:cutting/integration/gtceu/cutter/cut_red_steel_screw_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_red_steel_screw_to_bolt_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_red_steel_screw_to_bolt_water",
        "gtceu:shaped/bolt_saw_red_steel",
        "tfc:anvil/red_steel_bolt"
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
          "text": "(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 18 mB of §fRed Steel§7 (at Brilliant White§7)"
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
      "id": "gtceu:red_steel_bucket",
      "namespace": "gtceu",
      "display_name": "Liquid Red Steel Bucket",
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
          "text": "(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 1,000 K"
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
      "id": "gtceu:red_steel_butchery_knife",
      "namespace": "gtceu",
      "display_name": "Red Steel Butchery Knife",
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
        "gtceu:shaped/butchery_knife_red_steel"
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
          "text": "7,799 §eTotal Durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "7,800 §bDurability"
        },
        {
          "source": "runtime-tooltip",
          "text": "11 §cAttack Damage"
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
          "text": "§8Craft with a Repair Kit to repair 25% durability"
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
          "text": "§7Melts into 144 mB of §fRed Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 7799,
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
      "id": "gtceu:red_steel_butchery_knife_head",
      "namespace": "gtceu",
      "display_name": "Red Steel Butchery Knife Head",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:butchery_knife_heads",
        "forge:butchery_knife_heads/red_steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:advanced_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:advanced_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1,
        "vintageimprovements:curving": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "gtceu:shaped/butchery_knife_red_steel"
      ],
      "recipe_output_examples": [
        "tfc:anvil/red_steel_knife_butchery_head",
        "tfg:vi/curving/red_steel_ingot_to_butchery_knife_head"
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
          "text": "(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fRed Steel§7 (at Brilliant White§7)"
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
      "id": "gtceu:red_steel_buzz_saw_blade",
      "namespace": "gtceu",
      "display_name": "Red Steel Buzzsaw Blade",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:buzz_saw_heads",
        "forge:buzz_saw_heads/red_steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 4
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1,
        "vintageimprovements:polishing": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "domum_ornamentum:architectscutter",
        "framedblocks:framing_saw",
        "gtceu:shaped/red_steel_buzzsaw",
        "tfg:shaped/stonecutter"
      ],
      "recipe_output_examples": [
        "tfc:anvil/red_steel_buzzsaw_blade",
        "tfg:vi/lathe/red_steel_buzzsaw"
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
          "text": "(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 288 mB of §fRed Steel§7 (at Brilliant White§7)"
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
      "id": "gtceu:red_steel_buzzsaw",
      "namespace": "gtceu",
      "display_name": "Red Steel Buzzsaw (LV)",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:tools/buzzsaws",
        "forge:tools/saws",
        "gtceu:tools/crafting_saws",
        "minecraft:breaks_decorated_pots",
        "minecraft:tools",
        "tfc:deals_slashing_damage",
        "tfc:saws",
        "tfc:usable_on_tool_rack",
        "tfg:artisan_table_tools"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 250,
        "crafting_shapeless": 330,
        "greate:milling": 1,
        "tfc:damage_inputs_shapeless_crafting": 147
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 728,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "afc:crafting/wood/baobab_support",
        "afc:crafting/wood/teak_support",
        "createdeco:brass_window_pane",
        "domum_ornamentum:cactus_extra_silken_pincushion_cactus",
        "firmaciv:crafting/uncraft_hickory_roofing",
        "firmaciv:crafting/uncraft_rosewood_roofing",
        "firmacivplus:crafting/wood/lumber/cypress",
        "firmacivplus:crafting/wood/lumber/tualang",
        "greate:shaped/andesite_alloy_shaft_bismuth_bronze",
        "gtceu:shaped/bolt_saw_aluminium",
        "gtceu:shaped/bolt_saw_brass",
        "gtceu:shaped/bolt_saw_diamond",
        "gtceu:shaped/bolt_saw_inconel_718",
        "gtceu:shaped/bolt_saw_manganese",
        "gtceu:shaped/bolt_saw_neutronium",
        "gtceu:shaped/bolt_saw_rene_41",
        "gtceu:shaped/bolt_saw_silver",
        "gtceu:shaped/bolt_saw_tritanium",
        "gtceu:shaped/bolt_saw_wrought_iron",
        "gtceu:shaped/gear_wood",
        "gtceu:shaped/magenta_glass_pane",
        "gtceu:shaped/small_gear_wood",
        "gtceu:shaped/stick_long_black_bronze",
        "gtceu:shaped/stick_long_cupronickel",
        "gtceu:shaped/stick_long_gem_flawless_diamond",
        "gtceu:shaped/stick_long_iridium",
        "gtceu:shaped/stick_long_naquadah_alloy",
        "gtceu:shaped/stick_long_platinum",
        "gtceu:shaped/stick_long_rose_gold",
        "gtceu:shaped/stick_long_thorium_230",
        "gtceu:shaped/stick_long_tungsten",
        "gtceu:shaped/stick_long_wrought_iron",
        "gtceu:shaped/tiny_neutronium_pipe",
        "gtceu:shaped/treated_wood_stick_saw",
        "mcw_tfc_aio:roofs/acacia_roofs/acacia_log_roof_uncraft",
        "mcw_tfc_aio:roofs/birch_roofs/birch_plank_roof_uncraft",
        "mcw_tfc_aio:roofs/blue_terracotta_roofs/blue_terracotta_roof_uncraft",
        "mcw_tfc_aio:roofs/douglas_fir_roofs/douglas_fir_log_roof_uncraft",
        "mcw_tfc_aio:roofs/hickory_roofs/hickory_plank_roof_uncraft",
        "mcw_tfc_aio:roofs/light_gray_terracotta_roofs/light_gray_terracotta_roof_uncraft",
        "mcw_tfc_aio:roofs/maple_roofs/maple_plank_roof_uncraft",
        "mcw_tfc_aio:roofs/pine_roofs/pine_plank_roof_uncraft",
        "mcw_tfc_aio:roofs/rosewood_roofs/rosewood_log_roof_uncraft",
        "mcw_tfc_aio:roofs/thatch2_roofs/thatch2_roof_uncraft",
        "mcw_tfc_aio:roofs/willow_roofs/willow_plank_roof_uncraft",
        "minecraft:gray_carpet",
        "minecraft:purple_carpet",
        "tfc:crafting/wood/birch_support",
        "tfc:crafting/wood/maple_support",
        "tfc:crafting/wood/white_cedar_support",
        "tfg:create/shapeless/crimson_window_pane",
        "tfg:create/shapeless/oak_window_pane",
        "tfg:everycompat/shapeless/ash_window_pane",
        "tfg:everycompat/shapeless/cypress_window_pane",
        "tfg:everycompat/shapeless/hickory_window_pane",
        "tfg:everycompat/shapeless/palm_window_pane",
        "tfg:everycompat/shapeless/tualang_window_pane",
        "tfg:shaped/ash_crafting_station",
        "tfg:shaped/cypress_crafting_station",
        "tfg:shaped/horse_crank_bismuth_bronze",
        "tfg:shaped/mahogany_crafting_station",
        "tfg:shaped/sequoia_crafting_station",
        "tfg:shaped/white_cedar_crafting_station",
        "tfg:shapeless/aeronos_lumber_from_log",
        "tfg:shapeless/araucaria_lumber_from_slab",
        "tfg:shapeless/aspen_lumber_from_log",
        "tfg:shapeless/bamboo_lumber_from_plank",
        "tfg:shapeless/beech_lumber_from_log",
        "tfg:shapeless/birch_lumber_from_slab",
        "tfg:shapeless/chestnut_lumber_from_log",
        "tfg:shapeless/crimson_lumber_from_slab",
        "tfg:shapeless/diorite_pressure_plate_to_button",
        "tfg:shapeless/eucalyptus_lumber_from_slab",
        "tfg:shapeless/ginkgo_lumber_from_plank",
        "tfg:shapeless/glacian_lumber_from_stair",
        "tfg:shapeless/hickory_lumber_from_log",
        "tfg:shapeless/ironwood_lumber_from_log",
        "tfg:shapeless/kapok_lumber_from_stair",
        "tfg:shapeless/mahogany_lumber_from_plank",
        "tfg:shapeless/maple_lumber_from_plank",
        "tfg:shapeless/oak_lumber_from_stair",
        "tfg:shapeless/phyllite_pressure_plate_to_button",
        "tfg:shapeless/rosewood_lumber_from_plank",
        "tfg:shapeless/saw_bamboo_pressure_plate_to_button",
        "tfg:shapeless/saw_crimson_pressure_plate_to_button",
        "tfg:shapeless/saw_glacian_pressure_plate_to_button",
        "tfg:shapeless/saw_mahogany_pressure_plate_to_button",
        "tfg:shapeless/saw_sequoia_pressure_plate_to_button",
        "tfg:shapeless/saw_warped_pressure_plate_to_button",
        "tfg:shapeless/shale_pressure_plate_to_button",
        "tfg:shapeless/strophar_lumber_from_plank",
        "tfg:shapeless/sycamore_lumber_from_stair",
        "tfg:shapeless/tualang_lumber_from_log",
        "tfg:shapeless/white_cedar_lumber_from_log",
        "tfg:shapeless/willow_lumber_from_stair",
        "tfg_tacz:trapdoor_rifle"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/red_steel_buzzsaw"
      ],
      "recipe_examples_truncated": true,
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
          "text": "§8Not suitable for harvesting Blocks"
        },
        {
          "source": "runtime-tooltip",
          "text": "-1/-1 EU - Tier §7LV"
        },
        {
          "source": "runtime-tooltip",
          "text": "7,800 §aCrafting Uses"
        },
        {
          "source": "runtime-tooltip",
          "text": "7,799 §eTotal Durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "7,800 §bDurability"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Usable as: §fSaw"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Craft with a Repair Kit to repair 25% durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "Craft with a new Tool Head to replace it"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Very Large"
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
        "minecraft:max_damage": 7799,
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
            "crafting",
            "greate:milling",
            "tfc:damage_inputs_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:red_steel_chainsaw_head",
      "namespace": "gtceu",
      "display_name": "Red Steel Chainsaw Head",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:chainsaw_heads",
        "forge:chainsaw_heads/red_steel"
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
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_chainsaw_head",
        "gtceu:shaped/red_steel_lv_chainsaw"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/chainsaw_head_red_steel"
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
          "text": "(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄"
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
      "id": "gtceu:red_steel_crowbar",
      "namespace": "gtceu",
      "display_name": "Red Steel Crowbar",
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
        "gtceu:shaped/crowbar_red_steel_black",
        "gtceu:shaped/crowbar_red_steel_blue",
        "gtceu:shaped/crowbar_red_steel_brown",
        "gtceu:shaped/crowbar_red_steel_cyan",
        "gtceu:shaped/crowbar_red_steel_gray",
        "gtceu:shaped/crowbar_red_steel_green",
        "gtceu:shaped/crowbar_red_steel_light_blue",
        "gtceu:shaped/crowbar_red_steel_light_gray",
        "gtceu:shaped/crowbar_red_steel_lime",
        "gtceu:shaped/crowbar_red_steel_magenta",
        "gtceu:shaped/crowbar_red_steel_orange",
        "gtceu:shaped/crowbar_red_steel_pink",
        "gtceu:shaped/crowbar_red_steel_purple",
        "gtceu:shaped/crowbar_red_steel_red",
        "gtceu:shaped/crowbar_red_steel_white",
        "gtceu:shaped/crowbar_red_steel_yellow"
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
          "text": "7,800 §aCrafting Uses"
        },
        {
          "source": "runtime-tooltip",
          "text": "7,799 §eTotal Durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "7,800 §bDurability"
        },
        {
          "source": "runtime-tooltip",
          "text": "10 §dMining Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eHarvest Level 3 §f(§bDiamond§f)"
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
          "text": "§8Craft with a Repair Kit to repair 25% durability"
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
          "text": "§7Melts into 216 mB of §fRed Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 7799,
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
      "id": "gtceu:red_steel_drill_head",
      "namespace": "gtceu",
      "display_name": "Red Steel Drill Head",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:drill_heads",
        "forge:drill_heads/red_steel"
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
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_drill_head",
        "gtceu:shaped/red_steel_lv_drill"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/drill_head_red_steel"
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
          "text": "(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄"
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
      "id": "gtceu:red_steel_dust",
      "namespace": "gtceu",
      "display_name": "Red Steel Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/red_steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "greate:milling": 30,
        "greate:mixing": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 33,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_red_steel",
        "gtceu:shaped/small_dust_disassembling_red_steel",
        "gtceu:shaped/tiny_dust_disassembling_3x3_red_steel",
        "gtceu:shaped/tiny_dust_disassembling_red_steel",
        "tfg:shapeless/unfired_repair_kit_red_steel"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_double_red_steel_plate",
        "greate:milling/integration/gtceu/macerator/macerate_ev_battery_hull",
        "greate:milling/integration/gtceu/macerator/macerate_long_red_steel_rod",
        "greate:milling/integration/gtceu/macerator/macerate_metal/anvil/red_steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/block/red_steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/block/red_steel_stairs",
        "greate:milling/integration/gtceu/macerator/macerate_metal/chisel_head/red_steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/double_ingot/red_steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/fish_hook/red_steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/ingot/red_steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/javelin_head/red_steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/mace_head/red_steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/mattock_head/red_steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/propick_head/red_steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/scraping_knife_blade/red_steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/trapdoor/red_steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/unfinished_lamp/red_steel",
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_block",
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_drill_head",
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_gear",
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_huge_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_large_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_nonuple_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_normal_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_plate",
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_quadruple_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_small_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_tong_part/red_steel",
        "greate:milling/integration/tfg/recycling/redblu_steel_landing_gear",
        "greate:milling/integration/tfg/recycling/redblu_steel_plated_airplane_propeller",
        "gtceu:shaped/small_dust_assembling_red_steel",
        "gtceu:shaped/tiny_dust_assembling_red_steel",
        "tfg:red_steel_greate"
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
          "text": "(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fRed Steel§7 (at Brilliant White§7)"
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
            "gtceu:crafting_shaped_strict"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:red_steel_file",
      "namespace": "gtceu",
      "display_name": "Red Steel File",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:tools/files",
        "gtceu:tools/crafting_files",
        "tfc:deals_slashing_damage",
        "tfc:usable_on_tool_rack",
        "tfg:artisan_table_tools"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "kubejs:shaped",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 595,
        "kubejs:shaped": 1,
        "tfc:damage_inputs_shapeless_crafting": 7
      },
      "recipe_production_by_type": {
        "tfc:advanced_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 603,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "create:shapeless/chisel_cut_andesite",
        "create:shapeless/chisel_cut_limestone",
        "gtceu:shaped/crowbar_bismuth_bronze_light_blue",
        "gtceu:shaped/crowbar_bismuth_bronze_purple",
        "gtceu:shaped/crowbar_black_bronze_brown",
        "gtceu:shaped/crowbar_black_bronze_magenta",
        "gtceu:shaped/crowbar_black_bronze_yellow",
        "gtceu:shaped/crowbar_black_steel_green",
        "gtceu:shaped/crowbar_black_steel_purple",
        "gtceu:shaped/crowbar_blue_steel_brown",
        "gtceu:shaped/crowbar_blue_steel_lime",
        "gtceu:shaped/crowbar_blue_steel_yellow",
        "gtceu:shaped/crowbar_boron_carbide_green",
        "gtceu:shaped/crowbar_boron_carbide_pink",
        "gtceu:shaped/crowbar_bronze_brown",
        "gtceu:shaped/crowbar_bronze_lime",
        "gtceu:shaped/crowbar_bronze_white",
        "gtceu:shaped/crowbar_copper_green",
        "gtceu:shaped/crowbar_copper_pink",
        "gtceu:shaped/crowbar_diamond_tipped_mo_50_re_blue",
        "gtceu:shaped/crowbar_diamond_tipped_mo_50_re_lime",
        "gtceu:shaped/crowbar_diamond_tipped_mo_50_re_white",
        "gtceu:shaped/crowbar_duranium_gray",
        "gtceu:shaped/crowbar_duranium_pink",
        "gtceu:shaped/crowbar_hsse_blue",
        "gtceu:shaped/crowbar_hsse_light_gray",
        "gtceu:shaped/crowbar_hsse_white",
        "gtceu:shaped/crowbar_naquadah_alloy_gray",
        "gtceu:shaped/crowbar_naquadah_alloy_orange",
        "gtceu:shaped/crowbar_neutronium_blue",
        "gtceu:shaped/crowbar_neutronium_light_gray",
        "gtceu:shaped/crowbar_neutronium_red",
        "gtceu:shaped/crowbar_ostrum_iodide_gray",
        "gtceu:shaped/crowbar_ostrum_iodide_orange",
        "gtceu:shaped/crowbar_red_steel_black",
        "gtceu:shaped/crowbar_red_steel_light_gray",
        "gtceu:shaped/crowbar_red_steel_red",
        "gtceu:shaped/crowbar_steel_cyan",
        "gtceu:shaped/crowbar_steel_orange",
        "gtceu:shaped/crowbar_tungsten_carbide_black",
        "gtceu:shaped/crowbar_tungsten_carbide_light_blue",
        "gtceu:shaped/crowbar_tungsten_carbide_red",
        "gtceu:shaped/crowbar_ultimet_cyan",
        "gtceu:shaped/crowbar_ultimet_magenta",
        "gtceu:shaped/crowbar_vanadium_steel_black",
        "gtceu:shaped/crowbar_vanadium_steel_light_blue",
        "gtceu:shaped/crowbar_vanadium_steel_purple",
        "gtceu:shaped/crowbar_wrought_iron_cyan",
        "gtceu:shaped/crowbar_wrought_iron_magenta",
        "gtceu:shaped/maintenance_hatch",
        "gtceu:shaped/ring_black_bronze",
        "gtceu:shaped/ring_copper",
        "gtceu:shaped/ring_invar",
        "gtceu:shaped/ring_neutronium",
        "gtceu:shaped/ring_rose_gold",
        "gtceu:shaped/ring_titanium",
        "gtceu:shaped/ring_zinc",
        "gtceu:shaped/rotor_chromium",
        "gtceu:shaped/rotor_magnalium",
        "gtceu:shaped/rotor_steel",
        "gtceu:shaped/rotor_wrought_iron",
        "gtceu:shaped/round_osmiridium",
        "gtceu:shaped/screw_black_bronze",
        "gtceu:shaped/screw_chromium",
        "gtceu:shaped/screw_electrum",
        "gtceu:shaped/screw_inconel_718",
        "gtceu:shaped/screw_magnetic_iron",
        "gtceu:shaped/screw_naquadria",
        "gtceu:shaped/screw_platinum",
        "gtceu:shaped/screw_rhodium_plated_palladium",
        "gtceu:shaped/screw_steel",
        "gtceu:shaped/screw_tritanium",
        "gtceu:shaped/screw_vanadium_steel",
        "gtceu:shaped/stick_americium",
        "gtceu:shaped/stick_black_bronze",
        "gtceu:shaped/stick_bronze",
        "gtceu:shaped/stick_cupronickel",
        "gtceu:shaped/stick_enriched_naquadah",
        "gtceu:shaped/stick_hsla_steel",
        "gtceu:shaped/stick_iridium",
        "gtceu:shaped/stick_magnalium",
        "gtceu:shaped/stick_maraging_steel_300",
        "gtceu:shaped/stick_naquadria",
        "gtceu:shaped/stick_nickel_zinc_ferrite",
        "gtceu:shaped/stick_ostrum_iodide",
        "gtceu:shaped/stick_polyvinyl_chloride",
        "gtceu:shaped/stick_rhodium_plated_palladium",
        "gtceu:shaped/stick_ruridit",
        "gtceu:shaped/stick_sterling_silver",
        "gtceu:shaped/stick_titanium",
        "gtceu:shaped/stick_tungsten_steel",
        "gtceu:shaped/stick_wrought_iron",
        "gtceu:shaped/turbine_blade_naquadah_alloy",
        "gtceu:shaped/turbine_blade_tritanium",
        "tfg:railways/shaped/remote_lens",
        "tfg_tacz:trapdoor_scope"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/file_red_steel"
      ],
      "recipe_examples_truncated": true,
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
          "text": "1,950 §aCrafting Uses"
        },
        {
          "source": "runtime-tooltip",
          "text": "7,799 §eTotal Durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "7,800 §bDurability"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Usable as: §fFile"
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
          "text": "§7Deals §fSlashing§7 Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fRed Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 7799,
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
            "crafting",
            "kubejs:shaped",
            "tfc:damage_inputs_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:red_steel_file_head",
      "namespace": "gtceu",
      "display_name": "Red Steel File Head",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:file_heads",
        "forge:file_heads/red_steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:advanced_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:advanced_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1,
        "vintageimprovements:curving": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "gtceu:shaped/file_red_steel"
      ],
      "recipe_output_examples": [
        "tfc:anvil/red_steel_file_head",
        "tfg:vi/curving/red_steel_ingot_to_file_head"
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
          "text": "(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fRed Steel§7 (at Brilliant White§7)"
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
      "id": "gtceu:red_steel_foil",
      "namespace": "gtceu",
      "display_name": "Red Steel Foil",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:foils",
        "forge:foils/red_steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "createaddition:rolling": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_foil"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/foil_red_steel",
        "tfg:rolling/red_steel_foil"
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
          "text": "(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄"
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
      "id": "gtceu:red_steel_gear",
      "namespace": "gtceu",
      "display_name": "Red Steel Gear",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gears",
        "forge:gears/red_steel"
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
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "greate:compacting": 1,
        "tfc:welding": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_red_steel_gear",
        "tfg:shaped/large_steam_turbine"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/gear_red_steel",
        "tfc:welding/red_steel_gear",
        "tfg:compacting/red_steel_gear"
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
          "text": "(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 576 mB of §fRed Steel§7 (at Brilliant White§7)"
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
      "id": "gtceu:red_steel_hammer",
      "namespace": "gtceu",
      "display_name": "Red Steel Hammer",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:tools/hammers",
        "gtceu:tools/crafting_hammers",
        "minecraft:breaks_decorated_pots",
        "minecraft:tools",
        "tfc:deals_crushing_damage",
        "tfc:hammers",
        "tfc:usable_on_tool_rack",
        "tfg:artisan_table_tools"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "kubejs:shaped",
        "kubejs:shapeless",
        "tfc:advanced_shaped_crafting",
        "tfc:advanced_shapeless_crafting",
        "tfc:damage_inputs_shapeless_crafting",
        "tfc:extra_products_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 934,
        "crafting_shapeless": 568,
        "kubejs:shaped": 13,
        "kubejs:shapeless": 34,
        "tfc:advanced_shaped_crafting": 18,
        "tfc:advanced_shapeless_crafting": 2,
        "tfc:damage_inputs_shapeless_crafting": 53,
        "tfc:extra_products_shapeless_crafting": 10
      },
      "recipe_production_by_type": {
        "tfc:advanced_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 1632,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "beneath:crafting/cracked_blackstone_bricks",
        "greate:shaped/aluminium_mechanical_mixer",
        "greate:shaped/steel_millstone",
        "gtceu:shaped/casing_invar_heatproof",
        "gtceu:shaped/chainsaw_head_duranium",
        "gtceu:shaped/crowbar_bismuth_bronze_lime",
        "gtceu:shaped/crowbar_black_bronze_magenta",
        "gtceu:shaped/crowbar_black_steel_orange",
        "gtceu:shaped/crowbar_blue_steel_pink",
        "gtceu:shaped/crowbar_boron_carbide_red",
        "gtceu:shaped/crowbar_bronze_white",
        "gtceu:shaped/crowbar_copper_yellow",
        "gtceu:shaped/crowbar_duranium_black",
        "gtceu:shaped/crowbar_hsse_blue",
        "gtceu:shaped/crowbar_naquadah_alloy_brown",
        "gtceu:shaped/crowbar_neutronium_gray",
        "gtceu:shaped/crowbar_ostrum_iodide_green",
        "gtceu:shaped/crowbar_red_steel_light_blue",
        "gtceu:shaped/crowbar_steel_light_gray",
        "gtceu:shaped/crowbar_tungsten_carbide_lime",
        "gtceu:shaped/crowbar_ultimet_magenta",
        "gtceu:shaped/crowbar_vanadium_steel_pink",
        "gtceu:shaped/crowbar_wrought_iron_purple",
        "gtceu:shaped/extreme_engine_intake_casing",
        "gtceu:shaped/foil_gallium",
        "gtceu:shaped/foil_niobium_titanium",
        "gtceu:shaped/foil_stainless_steel",
        "gtceu:shaped/foil_zirconium_diboride",
        "gtceu:shaped/huge_neutronium_pipe",
        "gtceu:shaped/large_naquadah_pipe",
        "gtceu:shaped/medium_electrum_pipe",
        "gtceu:shaped/pipe_huge_restrictive_brass",
        "gtceu:shaped/pipe_normal_restrictive_ultimet",
        "gtceu:shaped/ring_electrum",
        "gtceu:shaped/ring_silver",
        "gtceu:shaped/rotor_bronze",
        "gtceu:shaped/rotor_tungsten_steel",
        "gtceu:shaped/small_duranium_pipe",
        "gtceu:shaped/small_gear_steel",
        "gtceu:shaped/stainless_steel_crate",
        "gtceu:shaped/tiny_niobium_titanium_pipe",
        "gtceu:shapeless/centrifuged_ore_to_dust_barite",
        "gtceu:shapeless/centrifuged_ore_to_dust_cinnabar",
        "gtceu:shapeless/centrifuged_ore_to_dust_granitic_mineral_sand",
        "gtceu:shapeless/centrifuged_ore_to_dust_malachite",
        "gtceu:shapeless/centrifuged_ore_to_dust_pyrite",
        "gtceu:shapeless/centrifuged_ore_to_dust_sodalite",
        "gtceu:shapeless/centrifuged_ore_to_dust_vanadium_magnetite",
        "gtceu:shapeless/crushed_ore_to_dust_beryllium",
        "gtceu:shapeless/crushed_ore_to_dust_desh",
        "gtceu:shapeless/crushed_ore_to_dust_hematite",
        "gtceu:shapeless/crushed_ore_to_dust_neodymium",
        "gtceu:shapeless/crushed_ore_to_dust_realgar",
        "gtceu:shapeless/crushed_ore_to_dust_sulfur",
        "gtceu:shapeless/gem_to_gem_chipped_gem_almandine",
        "gtceu:shapeless/gem_to_gem_chipped_gem_monazite",
        "gtceu:shapeless/gem_to_gem_chipped_gem_topaz",
        "gtceu:shapeless/gem_to_gem_flawed_gem_lapis",
        "gtceu:shapeless/gem_to_gem_flawed_gem_sodalite",
        "gtceu:shapeless/gem_to_gem_flawless_gem_emerald",
        "gtceu:shapeless/gem_to_gem_flawless_gem_ruby",
        "gtceu:shapeless/gem_to_gem_gem_cinnabar",
        "gtceu:shapeless/gem_to_gem_gem_red_garnet",
        "gtceu:shapeless/purified_ore_to_dust_armalcolite",
        "gtceu:shapeless/purified_ore_to_dust_chalcopyrite",
        "gtceu:shapeless/purified_ore_to_dust_goethite",
        "gtceu:shapeless/purified_ore_to_dust_magnesite",
        "gtceu:shapeless/purified_ore_to_dust_platinum",
        "gtceu:shapeless/purified_ore_to_dust_scheelite",
        "gtceu:shapeless/purified_ore_to_dust_tungstate",
        "rnr:crafting/base_course",
        "tfc:crafting/rock/basalt_pressure_plate",
        "tfc:crafting/rock/gneiss_cracked",
        "tfc:crafting/rock/shale_pressure_plate",
        "tfcgroomer:black_bronze_grooming_station",
        "tfchotornot:crafting/tongs/red_steel",
        "tfg:create/shaped/chute",
        "tfg:create/shaped/piston_extension_pole",
        "tfg:grapplemod/downgrades/forcefield/lv",
        "tfg:grapplemod/downgrades/motor/zpm",
        "tfg:item_application/bismuth_bronze_plated_stair",
        "tfg:item_application/gold_plated_stair",
        "tfg:item_application/tin_plated_slab",
        "tfg:railways/shaped/smokestack_oilburner_brass",
        "tfg:shaped/aeronos_crafting_station",
        "tfg:shaped/bismuth_bronze_drum",
        "tfg:shaped/eucalyptus_pressure_plate",
        "tfg:shaped/ipe_pressure_plate",
        "tfg:shaped/pine_crafting_station",
        "tfg:shaped/strophar_crafting_station",
        "tfg:shaped/wood_belt_connector",
        "tfg:shapeless/deepslate_bricks_to_cracked",
        "tfg:shapeless/rose_gold_plated_block",
        "tfg:temp/large_fluid_pipe_ostrum",
        "tfg:temp/normal_item_pipe_rose_gold",
        "tfg:vi/shaped/helve_hammer"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/hammer_red_steel"
      ],
      "recipe_examples_truncated": true,
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
          "text": "§8Crushes Blocks when harvesting them"
        },
        {
          "source": "runtime-tooltip",
          "text": "3,900 §aCrafting Uses"
        },
        {
          "source": "runtime-tooltip",
          "text": "7,799 §eTotal Durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "7,800 §bDurability"
        },
        {
          "source": "runtime-tooltip",
          "text": "10 §dMining Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eHarvest Level 3 §f(§bDiamond§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Usable as: §fPickaxe"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Craft with a Repair Kit to repair 25% durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Deals §fCrushing§7 Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fRed Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "document_context": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/anvil_working_guide",
          "label": "Anvil Working Guide",
          "item_ref_count": 2,
          "related_item_refs": [
            "gtceu:wrought_iron_pickaxe_head"
          ],
          "snippets": [
            {
              "source": "guide-page",
              "key": "name",
              "text": "Anvil Working Guide"
            },
            {
              "source": "guide-page",
              "key": "pages.0.title",
              "text": "Working with Anvils"
            },
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "Working in Anvils is a key process inside TerraFirmaGreg, as most of your work towards the Steam and Mechanical ages will require the usage of an Anvil. This guide is broken down into two parts, Understanding the UI and How to Perfectly Forge. The Modpack has a custom Resource Pack called TFC Anvil Helper that you need to enable for this guide to make sense."
            },
            {
              "source": "guide-page",
              "key": "pages.1.text",
              "text": "The UI for the Anvil with TFC Anvil Helper Enabled"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 7799,
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
            "crafting",
            "kubejs:shaped",
            "kubejs:shapeless",
            "tfc:advanced_shaped_crafting",
            "tfc:advanced_shapeless_crafting",
            "tfc:damage_inputs_shapeless_crafting",
            "tfc:extra_products_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:red_steel_hammer_head",
      "namespace": "gtceu",
      "display_name": "Red Steel Hammer Head",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:hammer_heads",
        "forge:hammer_heads/red_steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:advanced_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:advanced_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1,
        "vintageimprovements:curving": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "gtceu:shaped/hammer_red_steel"
      ],
      "recipe_output_examples": [
        "tfc:anvil/red_steel_hammer_head",
        "tfg:vi/curving/red_steel_ingot_to_hammer_head"
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
          "text": "(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fRed Steel§7 (at Brilliant White§7)"
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
      "id": "gtceu:red_steel_hoe",
      "namespace": "gtceu",
      "display_name": "Red Steel Hoe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:tools",
        "minecraft:breaks_decorated_pots",
        "minecraft:hoes",
        "minecraft:tools",
        "tfc:deals_piercing_damage",
        "tfc:hoes",
        "tfc:sharp_tools",
        "tfc:usable_on_tool_rack"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 8
      },
      "recipe_production_by_type": {
        "tfc:advanced_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 9,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "tfg:shapeless/sift_coarse_alfisol_dirt",
        "tfg:shapeless/sift_coarse_loam_dirt",
        "tfg:shapeless/sift_coarse_mollisol_dirt",
        "tfg:shapeless/sift_coarse_oxisol_dirt",
        "tfg:shapeless/sift_coarse_podzol_dirt",
        "tfg:shapeless/sift_coarse_sandy_loam_dirt",
        "tfg:shapeless/sift_coarse_silt_dirt",
        "tfg:shapeless/sift_coarse_silty_loam_dirt",
        "tfg:sophisticated_backpacks/shaped/tool_swapper_upgrade"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/hoe_red_steel"
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
          "text": "7,799 §eTotal Durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "7,800 §bDurability"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eFarmer: §fTills Ground"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Usable as: §fHoe"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Craft with a Repair Kit to repair 25% durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Deals §fPiercing§7 Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fRed Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 7799,
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
        "form": {
          "value": "tool",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _hoe"
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