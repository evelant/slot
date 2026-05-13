# Items to classify
{
  "items": [
    {
      "id": "tfc:metal/greaves/blue_steel",
      "namespace": "tfc",
      "display_name": "Blue Steel Greaves",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trimmable_armor",
        "tfc:metal_item/blue_steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:advanced_shaped_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_production_by_type": {
        "greate:compacting": 1,
        "tfc:welding": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "tfg:minecraft/shaped/netherite_leggings"
      ],
      "recipe_output_examples": [
        "tfc:welding/blue_steel_greaves",
        "tfg:compacting/blue_steel_greaves"
      ],
      "model_parents": [
        "item/metal/greaves/blue_steel",
        "item/default"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When on Legs:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+6 Armor"
        },
        {
          "source": "runtime-tooltip",
          "text": "+3 Armor Toughness"
        },
        {
          "source": "runtime-tooltip",
          "text": "+1 Knockback Resistance"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Resistances: §fSlashing§r 12%, §fPiercing§r 12%, §fCrushing§r 14%"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 432 mB of §fBlue Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 960,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "legs"
        },
        "minecraft:rarity": "epic"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
          "value": "legs",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        },
        "rarity": {
          "value": "unique",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = epic"
        },
        "processing_in": {
          "values": [
            "tfc:advanced_shaped_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfc:metal/greaves/bronze",
      "namespace": "tfc",
      "display_name": "Bronze Greaves",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trimmable_armor",
        "tfc:metal_item/bronze",
        "tfc:mob_leg_armor"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "greate:compacting": 1,
        "tfc:welding": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfc:welding/bronze_greaves",
        "tfg:compacting/bronze_greaves"
      ],
      "model_parents": [
        "item/metal/greaves/bronze",
        "item/default"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When on Legs:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+4 Armor"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Resistances: §fSlashing§r 2%, §fPiercing§r 3%, §fCrushing§r 3%"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 432 mB of §fBronze§7 (at Orange§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 315,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "legs"
        },
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
          "value": "legs",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        }
      }
    },
    {
      "id": "tfc:metal/greaves/copper",
      "namespace": "tfc",
      "display_name": "Copper Greaves",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trimmable_armor",
        "tfc:metal_item/copper",
        "tfc:mob_leg_armor"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "greate:compacting": 1,
        "tfc:welding": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfc:welding/copper_greaves",
        "tfg:compacting/copper_greaves"
      ],
      "model_parents": [
        "item/metal/greaves/copper",
        "item/default"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When on Legs:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+3 Armor"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Resistances: §fSlashing§r 2%, §fPiercing§r 2%, §fCrushing§r 2%"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 432 mB of §fCopper§7 (at Orange٭٭٭٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 200,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "legs"
        },
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
          "value": "legs",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        }
      }
    },
    {
      "id": "tfc:metal/greaves/red_steel",
      "namespace": "tfc",
      "display_name": "Red Steel Greaves",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:freeze_immune_wearables",
        "minecraft:trimmable_armor",
        "tfc:metal_item/red_steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "greate:compacting": 1,
        "tfc:welding": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfc:welding/red_steel_greaves",
        "tfg:compacting/red_steel_greaves"
      ],
      "model_parents": [
        "item/metal/greaves/red_steel",
        "item/default"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When on Legs:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+6 Armor"
        },
        {
          "source": "runtime-tooltip",
          "text": "+3 Armor Toughness"
        },
        {
          "source": "runtime-tooltip",
          "text": "+1 Knockback Resistance"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Resistances: §fSlashing§r 12%, §fPiercing§r 14%, §fCrushing§r 12%"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 432 mB of §fRed Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 1020,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "legs"
        },
        "minecraft:rarity": "epic"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
          "value": "legs",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        },
        "rarity": {
          "value": "unique",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = epic"
        }
      }
    },
    {
      "id": "tfc:metal/greaves/steel",
      "namespace": "tfc",
      "display_name": "Steel Greaves",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trimmable_armor",
        "tfc:metal_item/steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "greate:compacting": 1,
        "tfc:welding": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfc:welding/steel_greaves",
        "tfg:compacting/steel_greaves"
      ],
      "model_parents": [
        "item/metal/greaves/steel",
        "item/default"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When on Legs:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+5 Armor"
        },
        {
          "source": "runtime-tooltip",
          "text": "+1 Armor Toughness"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Resistances: §fSlashing§r 4%, §fPiercing§r 7%, §fCrushing§r 6%"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 432 mB of §fSteel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 600,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "legs"
        },
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
          "value": "legs",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        },
        "rarity": {
          "value": "uncommon",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = uncommon"
        }
      }
    },
    {
      "id": "tfc:metal/greaves/wrought_iron",
      "namespace": "tfc",
      "display_name": "Wrought Iron Greaves",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trimmable_armor",
        "tfc:metal_item/wrought_iron",
        "tfc:mob_leg_armor"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "greate:compacting": 1,
        "tfc:welding": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfc:welding/wrought_iron_greaves",
        "tfg:compacting/wrought_iron_greaves"
      ],
      "model_parents": [
        "item/metal/greaves/wrought_iron",
        "item/default"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When on Legs:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+4 Armor"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Resistances: §fSlashing§r 3%, §fPiercing§r 5%, §fCrushing§r 5%"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 432 mB of §fCast Iron§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 495,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "legs"
        },
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
          "value": "legs",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        }
      }
    },
    {
      "id": "tfc:metal/hammer/bismuth_bronze",
      "namespace": "tfc",
      "display_name": "Bismuth Bronze Hammer",
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
      "model_parents": [
        "item/metal/hammer/bismuth_bronze",
        "item/handheld"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+4 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-3 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 1200,
        "minecraft:enchantable": {},
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
      "id": "tfc:metal/hammer/black_bronze",
      "namespace": "tfc",
      "display_name": "Black Bronze Hammer",
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
      "model_parents": [
        "item/metal/hammer/black_bronze",
        "item/handheld"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+4.25 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-3 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 1460,
        "minecraft:enchantable": {},
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
      "id": "tfc:metal/hammer/black_steel",
      "namespace": "tfc",
      "display_name": "Black Steel Hammer",
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
      "model_parents": [
        "item/metal/hammer/black_steel",
        "item/handheld"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+7 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-3 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 4200,
        "minecraft:enchantable": {},
        "minecraft:rarity": "rare"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
        "rarity": {
          "value": "rare",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = rare"
        }
      }
    },
    {
      "id": "tfc:metal/hammer/blue_steel",
      "namespace": "tfc",
      "display_name": "Blue Steel Hammer",
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
      "model_parents": [
        "item/metal/hammer/blue_steel",
        "item/handheld"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+9 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-3 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 6500,
        "minecraft:enchantable": {},
        "minecraft:rarity": "epic"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
        "rarity": {
          "value": "unique",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = epic"
        }
      }
    },
    {
      "id": "tfc:metal/hammer/bronze",
      "namespace": "tfc",
      "display_name": "Bronze Hammer",
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
      "model_parents": [
        "item/metal/hammer/bronze",
        "item/handheld"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+4 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-3 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 1300,
        "minecraft:enchantable": {},
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
      "id": "tfc:metal/hammer/copper",
      "namespace": "tfc",
      "display_name": "Copper Hammer",
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
      "model_parents": [
        "item/metal/hammer/copper",
        "item/handheld"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+3.25 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-3 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 600,
        "minecraft:enchantable": {},
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
      "id": "tfc:metal/hammer/red_steel",
      "namespace": "tfc",
      "display_name": "Red Steel Hammer",
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
      "model_parents": [
        "item/metal/hammer/red_steel",
        "item/handheld"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+9 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-3 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "tfc:story/perfectly_forged",
          "label": "Perfectly Forged",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Perfectly Forged"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Gain the Perfectly Forged bonus on an item"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 6500,
        "minecraft:enchantable": {},
        "minecraft:rarity": "epic"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
        "rarity": {
          "value": "unique",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = epic"
        }
      }
    },
    {
      "id": "tfc:metal/hammer/steel",
      "namespace": "tfc",
      "display_name": "Steel Hammer",
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
      "model_parents": [
        "item/metal/hammer/steel",
        "item/handheld"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+5.75 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-3 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 3300,
        "minecraft:enchantable": {},
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
        "rarity": {
          "value": "uncommon",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = uncommon"
        }
      }
    },
    {
      "id": "tfc:metal/hammer/wrought_iron",
      "namespace": "tfc",
      "display_name": "Wrought Iron Hammer",
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
      "model_parents": [
        "item/metal/hammer/wrought_iron",
        "item/handheld"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+4.75 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-3 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "tfc:story/root",
          "label": "TerraFirmaCraft Story",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "TerraFirmaCraft Story"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "TFC's main progression line"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 2200,
        "minecraft:enchantable": {},
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
      "id": "tfc:metal/hammer_head/bismuth_bronze",
      "namespace": "tfc",
      "display_name": "Bismuth Bronze Hammer Head",
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
      "model_parents": [
        "item/metal/hammer_head/bismuth_bronze",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
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
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
      "id": "tfc:metal/hammer_head/black_bronze",
      "namespace": "tfc",
      "display_name": "Black Bronze Hammer Head",
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
      "model_parents": [
        "item/metal/hammer_head/black_bronze",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
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
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
      "id": "tfc:metal/hammer_head/black_steel",
      "namespace": "tfc",
      "display_name": "Black Steel Hammer Head",
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
      "model_parents": [
        "item/metal/hammer_head/black_steel",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
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
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "rare"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "rarity": {
          "value": "rare",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = rare"
        }
      }
    },
    {
      "id": "tfc:metal/hammer_head/blue_steel",
      "namespace": "tfc",
      "display_name": "Blue Steel Hammer Head",
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
      "model_parents": [
        "item/metal/hammer_head/blue_steel",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
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
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "epic"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "rarity": {
          "value": "unique",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = epic"
        }
      }
    },
    {
      "id": "tfc:metal/hammer_head/bronze",
      "namespace": "tfc",
      "display_name": "Bronze Hammer Head",
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
      "model_parents": [
        "item/metal/hammer_head/bronze",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
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
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
      "id": "tfc:metal/hammer_head/copper",
      "namespace": "tfc",
      "display_name": "Copper Hammer Head",
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
      "model_parents": [
        "item/metal/hammer_head/copper",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
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
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
      "id": "tfc:metal/hammer_head/red_steel",
      "namespace": "tfc",
      "display_name": "Red Steel Hammer Head",
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
      "model_parents": [
        "item/metal/hammer_head/red_steel",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
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
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "epic"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "rarity": {
          "value": "unique",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = epic"
        }
      }
    },
    {
      "id": "tfc:metal/hammer_head/steel",
      "namespace": "tfc",
      "display_name": "Steel Hammer Head",
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
      "model_parents": [
        "item/metal/hammer_head/steel",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
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
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "rarity": {
          "value": "uncommon",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = uncommon"
        }
      }
    },
    {
      "id": "tfc:metal/hammer_head/wrought_iron",
      "namespace": "tfc",
      "display_name": "Wrought Iron Hammer Head",
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
      "model_parents": [
        "item/metal/hammer_head/wrought_iron",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
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
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
      "id": "tfc:metal/helmet/bismuth_bronze",
      "namespace": "tfc",
      "display_name": "Bismuth Bronze Helmet",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trimmable_armor",
        "tfc:metal_item/bismuth_bronze",
        "tfc:mob_head_armor"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "greate:compacting": 1,
        "tfc:welding": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "diggerhelmet:bismuth_bronze_digger_helmet"
      ],
      "recipe_output_examples": [
        "tfc:welding/bismuth_bronze_helmet",
        "tfg:compacting/bismuth_bronze_helmet"
      ],
      "model_parents": [
        "item/metal/helmet/bismuth_bronze",
        "item/default"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When on Head:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+1 Armor"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Resistances: §fSlashing§r 2%, §fPiercing§r 2%, §fCrushing§r 4%"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 432 mB of §fBismuth Bronze§7 (at Orange٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 240,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "head"
        },
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
          "value": "head",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
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