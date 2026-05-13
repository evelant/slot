# Items to classify
{
  "items": [
    {
      "id": "gtceu:steam_oven",
      "namespace": "gtceu",
      "display_name": "Steam Oven",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "kubejs:shaped": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "gtceu:shaped/steam_oven"
      ],
      "model_parents": [
        "item/steam_oven",
        "block/machine/steam_oven",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:steam_oven",
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
          "text": "Not to be confused with Multi-Smelter"
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
      "id": "gtceu:steel_alloy",
      "namespace": "gtceu",
      "display_name": "Steel Alloy",
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
          "text": "Fe"
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
      "id": "gtceu:steel_axe",
      "namespace": "gtceu",
      "display_name": "Steel Axe",
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
        "gtceu:shaped/axe_steel"
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
          "text": "2,559 §eTotal Durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "2,560 §bDurability"
        },
        {
          "source": "runtime-tooltip",
          "text": "7 §dMining Speed"
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
          "text": "§7Melts into 144 mB of §fSteel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 2559,
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
      "id": "gtceu:steel_axe_head",
      "namespace": "gtceu",
      "display_name": "Steel Axe Head",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:axe_heads",
        "forge:axe_heads/steel"
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
        "gtceu:shaped/axe_steel"
      ],
      "recipe_output_examples": [
        "tfc:anvil/steel_axe_head",
        "tfg:vi/curving/steel_ingot_to_axe_head"
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
          "text": "Fe"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fSteel§7 (at Brilliant White§7)"
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
      "id": "gtceu:steel_bolt",
      "namespace": "gtceu",
      "display_name": "Steel Bolt",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:bolts",
        "forge:bolts/steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "tfc:advanced_shaped_crafting",
        "vintageimprovements:polishing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 26,
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
      "recipe_ingredient_count": 48,
      "recipe_output_count": 8,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_steel_bolt",
        "gtceu:shaped/screw_steel",
        "gtceu:shaped/wrench_steel",
        "minecraft:kjs/createdeco_blue_industrial_iron_lamp",
        "minecraft:kjs/createdeco_green_industrial_iron_lamp",
        "minecraft:kjs/createdeco_red_industrial_iron_lamp",
        "minecraft:kjs/createdeco_yellow_industrial_iron_lamp",
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
        "tfg:create/shaped/electron_tube",
        "tfg:create/shaped/metal_girder_from_steel",
        "tfg:create/shapeless/minecart_coupling",
        "tfg:shaped/ad_astra_small_black_industrial_lamp",
        "tfg:shaped/ad_astra_small_blue_industrial_lamp",
        "tfg:shaped/ad_astra_small_brown_industrial_lamp",
        "tfg:shaped/ad_astra_small_cyan_industrial_lamp",
        "tfg:shaped/ad_astra_small_gray_industrial_lamp",
        "tfg:shaped/ad_astra_small_green_industrial_lamp",
        "tfg:shaped/ad_astra_small_light_blue_industrial_lamp",
        "tfg:shaped/ad_astra_small_light_gray_industrial_lamp",
        "tfg:shaped/ad_astra_small_lime_industrial_lamp",
        "tfg:shaped/ad_astra_small_magenta_industrial_lamp",
        "tfg:shaped/ad_astra_small_orange_industrial_lamp",
        "tfg:shaped/ad_astra_small_pink_industrial_lamp",
        "tfg:shaped/ad_astra_small_purple_industrial_lamp",
        "tfg:shaped/ad_astra_small_red_industrial_lamp",
        "tfg:shaped/ad_astra_small_white_industrial_lamp",
        "tfg:shaped/ad_astra_small_yellow_industrial_lamp",
        "tfg:shaped/flintlock_mechanism_steel",
        "tfg:vi/lathe/steel_bolt_to_screw",
        "tfg:zip_gun"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_steel_rod_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_steel_rod_to_bolt_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_steel_rod_to_bolt_water",
        "greate:cutting/integration/gtceu/cutter/cut_steel_screw_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_steel_screw_to_bolt_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_steel_screw_to_bolt_water",
        "gtceu:shaped/bolt_saw_steel",
        "tfc:anvil/steel_bolt"
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
          "text": "Fe"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 18 mB of §fSteel§7 (at Brilliant White§7)"
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
      "id": "gtceu:steel_boots",
      "namespace": "gtceu",
      "display_name": "Steel Boots",
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
          "text": "When on Feet:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+2 Armor"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 247,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "feet"
        },
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
        "equip_slot": {
          "value": "feet",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        },
        "form": {
          "value": "armor_piece",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _boots"
        }
      }
    },
    {
      "id": "gtceu:steel_brick_casing",
      "namespace": "gtceu",
      "display_name": "Bricked Wrought Iron Casing",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shaped": 10
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 10,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "gtceu:shaped/steam_alloy_smelter_steel",
        "gtceu:shaped/steam_boiler_coal_steel",
        "gtceu:shaped/steam_boiler_lava_steel",
        "gtceu:shaped/steam_boiler_solar_steel",
        "gtceu:shaped/steam_compressor_steel",
        "gtceu:shaped/steam_furnace_steel",
        "gtceu:shaped/steam_hammer_steel",
        "gtceu:shaped/steam_macerator_steel",
        "gtceu:shaped/steam_miner_steel",
        "gtceu:shaped/steam_rock_breaker_steel"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/steel_bricks_hull"
      ],
      "model_parents": [
        "item/steel_brick_casing",
        "block/steel_brick_casing",
        "block/cube_bottom_top"
      ],
      "creative_tabs": [
        "gtceu:decoration"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "gtceu:blocks/steel_brick_casing"
      ],
      "block_context": {
        "block_id": "gtceu:steel_brick_casing",
        "block_tags": [
          "firmalife:oven_insulation",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
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
        "processing_in": {
          "values": [
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
      "id": "gtceu:steel_bucket",
      "namespace": "gtceu",
      "display_name": "Liquid Steel Bucket",
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
          "text": "Fe"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 2,046 K"
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
      "id": "gtceu:steel_butchery_knife",
      "namespace": "gtceu",
      "display_name": "Steel Butchery Knife",
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
        "gtceu:shaped/butchery_knife_steel"
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
          "text": "2,559 §eTotal Durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "2,560 §bDurability"
        },
        {
          "source": "runtime-tooltip",
          "text": "8.5 §cAttack Damage"
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
          "text": "§7Melts into 144 mB of §fSteel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 2559,
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
      "id": "gtceu:steel_butchery_knife_head",
      "namespace": "gtceu",
      "display_name": "Steel Butchery Knife Head",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:butchery_knife_heads",
        "forge:butchery_knife_heads/steel"
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
        "gtceu:shaped/butchery_knife_steel"
      ],
      "recipe_output_examples": [
        "tfc:anvil/steel_knife_butchery_head",
        "tfg:vi/curving/steel_ingot_to_butchery_knife_head"
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
          "text": "Fe"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fSteel§7 (at Brilliant White§7)"
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
      "id": "gtceu:steel_chestplate",
      "namespace": "gtceu",
      "display_name": "Steel Chestplate",
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
          "text": "When on Body:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+7 Armor"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 304,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "chest"
        },
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
        "equip_slot": {
          "value": "chest",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        },
        "form": {
          "value": "armor_piece",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _chestplate"
        }
      }
    },
    {
      "id": "gtceu:steel_crate",
      "namespace": "gtceu",
      "display_name": "Steel Crate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:non_movable",
        "tfg:cannot_launch_in_railgun"
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
        "greate:milling/integration/gtceu/macerator/macerate_steel_crate",
        "gtceu:shaped/super_chest_lv"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/steel_crate"
      ],
      "model_parents": [
        "item/steel_crate",
        "block/machine/steel_crate",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:steel_crate",
        "block_tags": [
          "create:non_movable",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f72"
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
      "id": "gtceu:steel_crowbar",
      "namespace": "gtceu",
      "display_name": "Steel Crowbar",
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
        "gtceu:shaped/crowbar_steel_black",
        "gtceu:shaped/crowbar_steel_blue",
        "gtceu:shaped/crowbar_steel_brown",
        "gtceu:shaped/crowbar_steel_cyan",
        "gtceu:shaped/crowbar_steel_gray",
        "gtceu:shaped/crowbar_steel_green",
        "gtceu:shaped/crowbar_steel_light_blue",
        "gtceu:shaped/crowbar_steel_light_gray",
        "gtceu:shaped/crowbar_steel_lime",
        "gtceu:shaped/crowbar_steel_magenta",
        "gtceu:shaped/crowbar_steel_orange",
        "gtceu:shaped/crowbar_steel_pink",
        "gtceu:shaped/crowbar_steel_purple",
        "gtceu:shaped/crowbar_steel_red",
        "gtceu:shaped/crowbar_steel_white",
        "gtceu:shaped/crowbar_steel_yellow"
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
          "text": "2,560 §aCrafting Uses"
        },
        {
          "source": "runtime-tooltip",
          "text": "2,559 §eTotal Durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "2,560 §bDurability"
        },
        {
          "source": "runtime-tooltip",
          "text": "9 §dMining Speed"
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
          "text": "§7Melts into 216 mB of §fSteel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 2559,
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
      "id": "gtceu:steel_double_cable",
      "namespace": "gtceu",
      "display_name": "2x Steel Cable",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:double_cables",
        "forge:double_cables/steel"
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
        "greate:milling/integration/gtceu/macerator/macerate_steel_double_cable"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:steel_double_cable",
        "block_tags": [
          "forge:double_cables",
          "forge:double_cables/steel",
          "forge:mineable/wire_cutter",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_iron_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Fe"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a2,048 §a(§5EV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e4"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c2§7 EU-Volt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool_tier": {
          "value": "iron",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_iron_tool"
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
      "id": "gtceu:steel_double_ingot",
      "namespace": "gtceu",
      "display_name": "Steel Double Ingot",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:double_ingots",
        "forge:double_ingots/steel",
        "tfc:pileable_double_ingots"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "vintageimprovements:hammering"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "vintageimprovements:hammering": 4
      },
      "recipe_production_by_type": {
        "greate:compacting": 1,
        "tfc:welding": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "tfc:crafting/metal/anvil/steel",
        "tfg:vi/hammer/steel_plate_on_black_steel_anvil",
        "tfg:vi/hammer/steel_plate_on_blue_steel_anvil",
        "tfg:vi/hammer/steel_plate_on_red_steel_anvil",
        "tfg:vi/hammer/steel_plate_on_steel_anvil"
      ],
      "recipe_output_examples": [
        "tfc:welding/steel_doubleIngot",
        "tfg:compacting/steel_doubleIngot"
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
          "text": "Fe"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 288 mB of §fSteel§7 (at Brilliant White§7)"
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
        "form": {
          "value": "ingot",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ingot"
        },
        "processing_in": {
          "values": [
            "crafting",
            "vintageimprovements:hammering"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:steel_double_wire",
      "namespace": "gtceu",
      "display_name": "2x Steel Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:double_wires",
        "forge:double_wires/steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 3,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 2
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_steel_double_wire",
        "gtceu:shapeless/steel_wire_wire_gt_double_doubling",
        "gtceu:shapeless/steel_wire_wire_gt_double_quadrupling",
        "gtceu:shapeless/steel_wire_wire_gt_double_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/steel_wire_wire_gt_quadruple_splitting",
        "gtceu:shapeless/steel_wire_wire_gt_single_doubling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:steel_double_wire",
        "block_tags": [
          "forge:double_wires",
          "forge:double_wires/steel",
          "forge:mineable/wire_cutter",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_iron_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Fe"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a2,048 §a(§5EV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e4"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c4§7 EU-Volt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool_tier": {
          "value": "iron",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_iron_tool"
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
      "id": "gtceu:steel_drum",
      "namespace": "gtceu",
      "display_name": "Steel Drum",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:cannot_launch_in_railgun"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "tfc:advanced_shaped_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 1,
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_steel_drum",
        "gtceu:shapeless/drum_nbt_steel",
        "tfg:create/shaped/netherite_backtank"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/steel_drum",
        "gtceu:shapeless/drum_nbt_steel"
      ],
      "model_parents": [
        "item/steel_drum",
        "block/machine/steel_drum",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:steel_drum",
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
          "text": "§7Compact place to store all your fluids"
        },
        {
          "source": "runtime-tooltip",
          "text": "§9Fluid Capacity: §f64,000 mB"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
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
            "crafting",
            "greate:milling",
            "tfc:advanced_shaped_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:steel_dust",
      "namespace": "gtceu",
      "display_name": "Steel Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:mixing": 7,
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "greate:milling": 252,
        "smelting": 1
      },
      "recipe_ingredient_count": 12,
      "recipe_output_count": 255,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_steel",
        "greate:mixing/integration/gtceu/mixer/tungstensteel",
        "greate:mixing/integration/gtceu/mixer/vanadiumsteel",
        "gtceu:shaped/small_dust_disassembling_3x3_steel",
        "gtceu:shaped/small_dust_disassembling_steel",
        "gtceu:shaped/tiny_dust_disassembling_3x3_steel",
        "gtceu:shaped/tiny_dust_disassembling_steel",
        "tfg:blue_steel_greate",
        "tfg:red_steel_greate",
        "tfg:shapeless/unfired_repair_kit_steel",
        "tfg:weak_blue_steel_greate",
        "tfg:weak_red_steel_greate"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_acid_hazard_sign_block",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_crushing_wheel",
        "greate:milling/integration/gtceu/macerator/macerate_anvil_casting_mold",
        "greate:milling/integration/gtceu/macerator/macerate_ball_casting_mold",
        "greate:milling/integration/gtceu/macerator/macerate_big_buffer",
        "greate:milling/integration/gtceu/macerator/macerate_blaze_burner",
        "greate:milling/integration/gtceu/macerator/macerate_blue_steel_drill_head",
        "greate:milling/integration/gtceu/macerator/macerate_bottle_casting_mold",
        "greate:milling/integration/gtceu/macerator/macerate_buffer",
        "greate:milling/integration/gtceu/macerator/macerate_casings/bioculture_rotor_primary",
        "greate:milling/integration/gtceu/macerator/macerate_causality_hazard_sign_block",
        "greate:milling/integration/gtceu/macerator/macerate_cell_extruder_mold",
        "greate:milling/integration/gtceu/macerator/macerate_copycat_headstock_buffer",
        "greate:milling/integration/gtceu/macerator/macerate_dense_steel_plate",
        "greate:milling/integration/gtceu/macerator/macerate_diamond_tipped_mo_50_re_drill_head",
        "greate:milling/integration/gtceu/macerator/macerate_drive",
        "greate:milling/integration/gtceu/macerator/macerate_energy_level_emitter",
        "greate:milling/integration/gtceu/macerator/macerate_explosion_hazard_sign_block",
        "greate:milling/integration/gtceu/macerator/macerate_fire_hazard_sign_block",
        "greate:milling/integration/gtceu/macerator/macerate_frost_hazard_sign_block",
        "greate:milling/integration/gtceu/macerator/macerate_gear_casting_mold",
        "greate:milling/integration/gtceu/macerator/macerate_gregification_hazard_sign_block",
        "greate:milling/integration/gtceu/macerator/macerate_high_pressure_hazard_sign_block",
        "greate:milling/integration/gtceu/macerator/macerate_high_voltage_hazard_sign_block",
        "greate:milling/integration/gtceu/macerator/macerate_huge_pipe_casting_mold",
        "greate:milling/integration/gtceu/macerator/macerate_hv_input_bus",
        "greate:milling/integration/gtceu/macerator/macerate_implosion_compressor",
        "greate:milling/integration/gtceu/macerator/macerate_industrial_iron_trapdoor",
        "greate:milling/integration/gtceu/macerator/macerate_ingot_extruder_mold",
        "greate:milling/integration/gtceu/macerator/macerate_lamp_casting_mold",
        "greate:milling/integration/gtceu/macerator/macerate_large_casing_extruder_mold",
        "greate:milling/integration/gtceu/macerator/macerate_laser_hazard_sign_block",
        "greate:milling/integration/gtceu/macerator/macerate_long_distance_fluid_pipeline_endpoint",
        "greate:milling/integration/gtceu/macerator/macerate_long_steel_rod",
        "greate:milling/integration/gtceu/macerator/macerate_lv_autoclave",
        "greate:milling/integration/gtceu/macerator/macerate_lv_canner",
        "greate:milling/integration/gtceu/macerator/macerate_lv_combustion",
        "greate:milling/integration/gtceu/macerator/macerate_lv_distillery",
        "greate:milling/integration/gtceu/macerator/macerate_lv_electrolyzer",
        "greate:milling/integration/gtceu/macerator/macerate_lv_energy_output_hatch",
        "greate:milling/integration/gtceu/macerator/macerate_lv_fluid_passthrough_hatch",
        "greate:milling/integration/gtceu/macerator/macerate_lv_gas_pressurizer",
        "greate:milling/integration/gtceu/macerator/macerate_lv_input_bus",
        "greate:milling/integration/gtceu/macerator/macerate_lv_lathe",
        "greate:milling/integration/gtceu/macerator/macerate_lv_machine_hull",
        "greate:milling/integration/gtceu/macerator/macerate_lv_mixer",
        "greate:milling/integration/gtceu/macerator/macerate_lv_output_hatch",
        "greate:milling/integration/gtceu/macerator/macerate_lv_scanner",
        "greate:milling/integration/gtceu/macerator/macerate_lv_sifter",
        "greate:milling/integration/gtceu/macerator/macerate_lv_transformer_1_a",
        "greate:milling/integration/gtceu/macerator/macerate_machine_memory_card",
        "greate:milling/integration/gtceu/macerator/macerate_magnetic_hazard_sign_block",
        "greate:milling/integration/gtceu/macerator/macerate_magnetic_steel_plate",
        "greate:milling/integration/gtceu/macerator/macerate_mechanical_drill",
        "greate:milling/integration/gtceu/macerator/macerate_metal/block/steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/fish_hook/steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/mace_head/steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/propick_head/steel",
        "greate:milling/integration/gtceu/macerator/macerate_metal/unfinished_lamp/steel",
        "greate:milling/integration/gtceu/macerator/macerate_mob_spawner_hazard_sign_block",
        "greate:milling/integration/gtceu/macerator/macerate_name_casting_mold",
        "greate:milling/integration/gtceu/macerator/macerate_noise_hazard_sign_block",
        "greate:milling/integration/gtceu/macerator/macerate_nugget_casting_mold",
        "greate:milling/integration/gtceu/macerator/macerate_oxygen_distributor",
        "greate:milling/integration/gtceu/macerator/macerate_pill_casting_mold",
        "greate:milling/integration/gtceu/macerator/macerate_propick_head_extruder_mold",
        "greate:milling/integration/gtceu/macerator/macerate_radioactive_hazard_sign_block",
        "greate:milling/integration/gtceu/macerator/macerate_requester_terminal",
        "greate:milling/integration/gtceu/macerator/macerate_rotor_casting_mold",
        "greate:milling/integration/gtceu/macerator/macerate_saw_head_extruder_mold",
        "greate:milling/integration/gtceu/macerator/macerate_sequenced_gearshift",
        "greate:milling/integration/gtceu/macerator/macerate_small_buffer",
        "greate:milling/integration/gtceu/macerator/macerate_small_gear_extruder_mold",
        "greate:milling/integration/gtceu/macerator/macerate_small_pipe_extruder_mold",
        "greate:milling/integration/gtceu/macerator/macerate_spade_head_extruder_mold",
        "greate:milling/integration/gtceu/macerator/macerate_steam_large_turbine",
        "greate:milling/integration/gtceu/macerator/macerate_steel_block",
        "greate:milling/integration/gtceu/macerator/macerate_steel_double_wire",
        "greate:milling/integration/gtceu/macerator/macerate_steel_frame",
        "greate:milling/integration/gtceu/macerator/macerate_steel_hex_cable",
        "greate:milling/integration/gtceu/macerator/macerate_steel_ingot",
        "greate:milling/integration/gtceu/macerator/macerate_steel_minecart_wheels",
        "greate:milling/integration/gtceu/macerator/macerate_steel_normal_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_steel_pipe_casing",
        "greate:milling/integration/gtceu/macerator/macerate_steel_quadruple_wire",
        "greate:milling/integration/gtceu/macerator/macerate_steel_sliding_door",
        "greate:milling/integration/gtceu/macerator/macerate_steel_support",
        "greate:milling/integration/gtceu/macerator/macerate_sword_head_extruder_mold",
        "greate:milling/integration/gtceu/macerator/macerate_tiny_pipe_casting_mold",
        "greate:milling/integration/gtceu/macerator/macerate_tong_part/steel",
        "greate:milling/integration/gtceu/macerator/macerate_vanadium_steel_drill_head",
        "greate:milling/integration/gtceu/macerator/macerate_whisk_extruder_mold",
        "greate:milling/integration/gtceu/macerator/macerate_wooden_headstock_buffer",
        "greate:milling/integration/gtceu/macerator/macerate_yellow_stripes_block_b",
        "greate:milling/integration/tfg/recycling/lv_aircraft_engine",
        "gtceu:smelting/demagnetize_magnetic_steel_dust"
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
          "text": "Fe"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fSteel§7 (at Brilliant White§7)"
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
      "id": "gtceu:steel_file",
      "namespace": "gtceu",
      "display_name": "Steel File",
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
        "gtceu:shaped/file_steel"
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
          "text": "640 §aCrafting Uses"
        },
        {
          "source": "runtime-tooltip",
          "text": "2,559 §eTotal Durability"
        },
        {
          "source": "runtime-tooltip",
          "text": "2,560 §bDurability"
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
          "text": "§7Melts into 144 mB of §fSteel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 2559,
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
      "id": "gtceu:steel_file_head",
      "namespace": "gtceu",
      "display_name": "Steel File Head",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:file_heads",
        "forge:file_heads/steel"
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
        "gtceu:shaped/file_steel"
      ],
      "recipe_output_examples": [
        "tfc:anvil/steel_file_head",
        "tfg:vi/curving/steel_ingot_to_file_head"
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
          "text": "Fe"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fSteel§7 (at Brilliant White§7)"
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
      "id": "gtceu:steel_firebox_casing",
      "namespace": "gtceu",
      "display_name": "Steel Firebox Casing",
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
        "greate:milling/integration/gtceu/macerator/macerate_steel_firebox_casing",
        "tfg:shaped/large_steel_boiler"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/casing_steel_firebox"
      ],
      "model_parents": [
        "item/steel_firebox_casing",
        "block/steel_firebox_casing",
        "block/cube_bottom_top"
      ],
      "creative_tabs": [
        "gtceu:decoration"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "gtceu:blocks/steel_firebox_casing"
      ],
      "block_context": {
        "block_id": "gtceu:steel_firebox_casing",
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
      "id": "gtceu:steel_fluid_cell",
      "namespace": "gtceu",
      "display_name": "Empty Steel Cell",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:insulating_container"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_steel_fluid_cell",
        "gtceu:shaped/fluid_jetpack",
        "gtceu:shapeless/cell_nbt_steel"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/cell_nbt_steel"
      ],
      "model_parents": [
        "item/steel_fluid_cell",
        "item/default"
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
          "text": "§aSafely contains §6hot§a, §bcold§a, and §elighter-than-air§a items and fluids.§r"
        },
        {
          "source": "runtime-tooltip",
          "text": "§9Fluid Capacity: §f8,000 mB"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
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
      "id": "gtceu:steel_foil",
      "namespace": "gtceu",
      "display_name": "Steel Foil",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:foils",
        "forge:foils/steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "greate:milling": 1,
        "kubejs:shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "createaddition:rolling": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_steel_foil",
        "gtceu:shapeless/fine_wire_steel",
        "measurements:shapeless/tape_measure",
        "tfg_tacz:target"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/foil_steel",
        "tfg:rolling/steel_foil"
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
          "text": "Fe"
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
            "kubejs:shaped"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:steel_frame",
      "namespace": "gtceu",
      "display_name": "Steel Frame",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:frames",
        "forge:frames/steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 8,
        "greate:milling": 1,
        "kubejs:shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 10,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "framedblocks:framed_reinforcement",
        "greate:milling/integration/gtceu/macerator/macerate_steel_frame",
        "gtceu:shaped/casing_grate_casing",
        "gtceu:shaped/casing_steel_firebox",
        "gtceu:shaped/casing_steel_gearbox",
        "gtceu:shaped/casing_steel_pipe",
        "gtceu:shaped/casing_steel_solid",
        "gtceu:shaped/filter_casing",
        "tfg:shaped/ad_astra_vent",
        "tfg:shaped/industrial_steam_casing"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/frame_steel"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:steel_frame",
        "block_tags": [
          "forge:frames",
          "forge:frames/steel",
          "forge:mineable/wrench",
          "forge:slow_walkable_blocks",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_iron_tool"
        ],
        "requires_correct_tool": true
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
        "required_tool_tier": {
          "value": "iron",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_iron_tool"
        },
        "processing_in": {
          "values": [
            "crafting",
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
      "id": "gtceu:steel_gear",
      "namespace": "gtceu",
      "display_name": "Steel Gear",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gears",
        "forge:gears/steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 3,
        "greate:milling": 1,
        "kubejs:shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "greate:compacting": 1,
        "tfc:welding": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_steel_gear",
        "greate:shaped/aluminium_millstone",
        "gtceu:shaped/casing_steel_gearbox",
        "gtceu:shaped/diesel_generator_lv",
        "gtceu:shaped/large_steam_turbine"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/gear_steel",
        "tfc:welding/steel_gear",
        "tfg:compacting/steel_gear"
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
          "text": "Fe"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 576 mB of §fSteel§7 (at Brilliant White§7)"
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
            "crafting",
            "greate:milling",
            "kubejs:shaped"
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