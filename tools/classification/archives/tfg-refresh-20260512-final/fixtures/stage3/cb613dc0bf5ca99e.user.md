# Items to classify
{
  "items": [
    {
      "id": "tfc:metal/mace_head/wrought_iron",
      "namespace": "tfc",
      "display_name": "Wrought Iron Mace Head",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:mace_heads",
        "forge:mace_heads/wrought_iron",
        "tfc:metal_item/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "tfc:advanced_shaped_crafting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1,
        "vintageimprovements:curving": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_metal/mace_head/wrought_iron",
        "tfc:crafting/metal/mace/wrought_iron"
      ],
      "recipe_output_examples": [
        "tfc:anvil/wrought_iron_mace_head",
        "tfg:vi/curving/wrought_iron_ingot_to_mace_head"
      ],
      "model_parents": [
        "item/metal/mace_head/wrought_iron",
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
          "text": "Fe"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 288 mB of §fCast Iron§7 (at Brilliant White§7)"
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
        },
        "processing_in": {
          "values": [
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
      "id": "tfc:metal/pickaxe/bismuth_bronze",
      "namespace": "tfc",
      "display_name": "Bismuth Bronze Pickaxe",
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
        "item/metal/pickaxe/bismuth_bronze",
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
          "text": "+3 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-2.8 Attack Speed"
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
      "id": "tfc:metal/pickaxe/black_bronze",
      "namespace": "tfc",
      "display_name": "Black Bronze Pickaxe",
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
        "item/metal/pickaxe/black_bronze",
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
          "text": "-2.8 Attack Speed"
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
      "id": "tfc:metal/pickaxe/black_steel",
      "namespace": "tfc",
      "display_name": "Black Steel Pickaxe",
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
        "item/metal/pickaxe/black_steel",
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
          "text": "+6 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-2.8 Attack Speed"
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
      "id": "tfc:metal/pickaxe/blue_steel",
      "namespace": "tfc",
      "display_name": "Blue Steel Pickaxe",
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
        "item/metal/pickaxe/blue_steel",
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
          "text": "-2.8 Attack Speed"
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
      "id": "tfc:metal/pickaxe/bronze",
      "namespace": "tfc",
      "display_name": "Bronze Pickaxe",
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
        "item/metal/pickaxe/bronze",
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
          "text": "+3 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-2.8 Attack Speed"
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
      "id": "tfc:metal/pickaxe/copper",
      "namespace": "tfc",
      "display_name": "Copper Pickaxe",
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
        "item/metal/pickaxe/copper",
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
          "text": "-2.8 Attack Speed"
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
          "id": "tfc:story/pickaxe",
          "label": "Time to Mine (Finally!)",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Time to Mine (Finally!)"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Make your first metal pickaxe"
            }
          ]
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
      "id": "tfc:metal/pickaxe/red_steel",
      "namespace": "tfc",
      "display_name": "Red Steel Pickaxe",
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
        "item/metal/pickaxe/red_steel",
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
          "text": "-2.8 Attack Speed"
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
      "id": "tfc:metal/pickaxe/steel",
      "namespace": "tfc",
      "display_name": "Steel Pickaxe",
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
        "item/metal/pickaxe/steel",
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
          "text": "-2.8 Attack Speed"
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
      "id": "tfc:metal/pickaxe/wrought_iron",
      "namespace": "tfc",
      "display_name": "Wrought Iron Pickaxe",
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
        "item/metal/pickaxe/wrought_iron",
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
          "text": "+3.75 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-2.8 Attack Speed"
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
      "id": "tfc:metal/pickaxe_head/bismuth_bronze",
      "namespace": "tfc",
      "display_name": "Bismuth Bronze Pickaxe Head",
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
        "item/metal/pickaxe_head/bismuth_bronze",
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
      "id": "tfc:metal/pickaxe_head/black_bronze",
      "namespace": "tfc",
      "display_name": "Black Bronze Pickaxe Head",
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
        "item/metal/pickaxe_head/black_bronze",
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
      "id": "tfc:metal/pickaxe_head/black_steel",
      "namespace": "tfc",
      "display_name": "Black Steel Pickaxe Head",
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
        "item/metal/pickaxe_head/black_steel",
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
      "id": "tfc:metal/pickaxe_head/blue_steel",
      "namespace": "tfc",
      "display_name": "Blue Steel Pickaxe Head",
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
        "item/metal/pickaxe_head/blue_steel",
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
      "id": "tfc:metal/pickaxe_head/bronze",
      "namespace": "tfc",
      "display_name": "Bronze Pickaxe Head",
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
        "item/metal/pickaxe_head/bronze",
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
      "id": "tfc:metal/pickaxe_head/copper",
      "namespace": "tfc",
      "display_name": "Copper Pickaxe Head",
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
        "item/metal/pickaxe_head/copper",
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
      "id": "tfc:metal/pickaxe_head/red_steel",
      "namespace": "tfc",
      "display_name": "Red Steel Pickaxe Head",
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
        "item/metal/pickaxe_head/red_steel",
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
      "id": "tfc:metal/pickaxe_head/steel",
      "namespace": "tfc",
      "display_name": "Steel Pickaxe Head",
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
        "item/metal/pickaxe_head/steel",
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
      "id": "tfc:metal/pickaxe_head/wrought_iron",
      "namespace": "tfc",
      "display_name": "Wrought Iron Pickaxe Head",
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
        "item/metal/pickaxe_head/wrought_iron",
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
      "id": "tfc:metal/propick/bismuth_bronze",
      "namespace": "tfc",
      "display_name": "Bismuth Bronze Prospector's Pick",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:breaks_decorated_pots",
        "minecraft:tools",
        "tfc:metal_item/bismuth_bronze",
        "tfc:metal_item/bismuth_bronze_tools",
        "tfc:propicks",
        "tfc:usable_on_tool_rack",
        "tfg:tools/ore_prospectors/bronze"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfc:crafting/metal/propick/bismuth_bronze"
      ],
      "model_parents": [
        "item/metal/propick/bismuth_bronze",
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
          "text": "+2 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-2.8 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fBismuth Bronze§7 (at Orange٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "Scan Range: 20.0, Cross Section: 16 x 16."
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
      "id": "tfc:metal/propick/black_bronze",
      "namespace": "tfc",
      "display_name": "Black Bronze Prospector's Pick",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:breaks_decorated_pots",
        "minecraft:tools",
        "tfc:metal_item/black_bronze",
        "tfc:metal_item/black_bronze_tools",
        "tfc:propicks",
        "tfc:usable_on_tool_rack",
        "tfg:tools/ore_prospectors/bronze"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfc:crafting/metal/propick/black_bronze"
      ],
      "model_parents": [
        "item/metal/propick/black_bronze",
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
          "text": "+2.12 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-2.8 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fBlack Bronze§7 (at Orange٭٭٭٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "Scan Range: 20.0, Cross Section: 16 x 16."
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
      "id": "tfc:metal/propick/black_steel",
      "namespace": "tfc",
      "display_name": "Black Steel Prospector's Pick",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:breaks_decorated_pots",
        "minecraft:tools",
        "tfc:metal_item/black_steel",
        "tfc:metal_item/black_steel_tools",
        "tfc:propicks",
        "tfc:usable_on_tool_rack",
        "tfg:tools/ore_prospectors/black_steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfc:crafting/metal/propick/black_steel"
      ],
      "model_parents": [
        "item/metal/propick/black_steel",
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
          "text": "+3.5 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-2.8 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fBlack Steel§7 (at White٭٭٭٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "Scan Range: 50.0, Cross Section: 30 x 30."
        },
        {
          "source": "runtime-tooltip",
          "text": "This prospector will display ore counts."
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
      "id": "tfc:metal/propick/blue_steel",
      "namespace": "tfc",
      "display_name": "Blue Steel Prospector's Pick",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:breaks_decorated_pots",
        "minecraft:tools",
        "tfc:metal_item/blue_steel",
        "tfc:metal_item/blue_steel_tools",
        "tfc:propicks",
        "tfc:usable_on_tool_rack",
        "tfg:tools/ore_prospectors/blue_steel"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfc:crafting/metal/propick/blue_steel"
      ],
      "model_parents": [
        "item/metal/propick/blue_steel",
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
          "text": "+4.5 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-2.8 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fBlue Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "Scan Range: 75.0, Cross Section: 30 x 30."
        },
        {
          "source": "runtime-tooltip",
          "text": "This prospector will display ore counts."
        },
        {
          "source": "runtime-tooltip",
          "text": "This prospector will display a per-vein x-ray view of ore blocks."
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
      "id": "tfc:metal/propick/bronze",
      "namespace": "tfc",
      "display_name": "Bronze Prospector's Pick",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:breaks_decorated_pots",
        "minecraft:tools",
        "tfc:metal_item/bronze",
        "tfc:metal_item/bronze_tools",
        "tfc:propicks",
        "tfc:usable_on_tool_rack",
        "tfg:tools/ore_prospectors/bronze"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfc:crafting/metal/propick/bronze"
      ],
      "model_parents": [
        "item/metal/propick/bronze",
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
          "text": "+2 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-2.8 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fBronze§7 (at Orange§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "Scan Range: 20.0, Cross Section: 16 x 16."
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
      "id": "tfc:metal/propick/copper",
      "namespace": "tfc",
      "display_name": "Copper Prospector's Pick",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:breaks_decorated_pots",
        "minecraft:tools",
        "tfc:metal_item/copper",
        "tfc:metal_item/copper_tools",
        "tfc:propicks",
        "tfc:usable_on_tool_rack",
        "tfg:tools/ore_prospectors/copper"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfc:crafting/metal/propick/copper"
      ],
      "model_parents": [
        "item/metal/propick/copper",
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
          "text": "+1.62 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-2.8 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fCopper§7 (at Orange٭٭٭٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "Scan Range: 15.0, Cross Section: 10 x 10."
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "tfc:story/propick",
          "label": "Prospector",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Prospector"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Make a prospector's pickaxe"
            }
          ]
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/prospecting",
          "label": "Prospecting",
          "item_ref_count": 3,
          "related_item_refs": [
            "gtceu:prospector.lv",
            "tfc:metal/propick/wrought_iron"
          ],
          "snippets": [
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "You remembered where you picked up those small metal nuggets, right? Finding additional ores may require extensive exploration and mining. You should become very familiar with ores and minerals. If you need a specific resource, you must find the rock type it spawns in either under your feet or across the world."
            },
            {
              "source": "guide-page",
              "key": "pages.1.text",
              "text": "When picking up small nuggets becomes unsatisfying, it is time to start prospecting to find ore veins: - Small nuggets, surface buds, and surface rocks occur when an ore vein is located below. If you find the center of a group of nuggets, it's likely that the vein is beneath you. - Exposed ore can occur in cliffs and water bodies, which may be seen from farther away."
            },
            {
              "source": "guide-page",
              "key": "pages.2.title",
              "text": "Prospector's Pick"
            },
            {
              "source": "guide-page",
              "key": "pages.2.text",
              "text": "If you're looking for ore veins, and you can't find the vein by guessing, it's time to pull out the prospector's pick. If you are familiar with base prospector's picks in TFC you may be suprised to learn that they function very differently in TerraFirmaGreg"
            }
          ]
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