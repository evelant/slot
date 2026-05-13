# Items to classify
{
  "items": [
    {
      "id": "tfc:metal/boots/red_steel",
      "namespace": "tfc",
      "display_name": "Red Steel Boots",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:freeze_immune_wearables",
        "minecraft:trimmable_armor",
        "tfc:metal_item/red_steel",
        "tfg:cold_protection_equipment"
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
        "tfc:welding/red_steel_boots",
        "tfg:compacting/red_steel_boots"
      ],
      "model_parents": [
        "item/metal/boots/red_steel",
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
          "text": "§7Provides protection from all §bcold§7 held items and fluids (chest) and pipes (boots).§r"
        },
        {
          "source": "runtime-tooltip",
          "text": "When on Feet:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+3 Armor"
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
          "text": "§7Melts into 288 mB of §fRed Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "tfc:world/adventuring_time",
          "label": "Adventuring Time",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Adventuring Time"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Discover every biome in TFC"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 884,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "feet"
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
          "value": "feet",
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
      "id": "tfc:metal/boots/steel",
      "namespace": "tfc",
      "display_name": "Steel Boots",
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
        "tfc:welding/steel_boots",
        "tfg:compacting/steel_boots"
      ],
      "model_parents": [
        "item/metal/boots/steel",
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
          "text": "When on Feet:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+2 Armor"
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
          "text": "§7Melts into 288 mB of §fSteel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 520,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "feet"
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
          "value": "feet",
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
      "id": "tfc:metal/boots/wrought_iron",
      "namespace": "tfc",
      "display_name": "Wrought Iron Boots",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trimmable_armor",
        "tfc:metal_item/wrought_iron",
        "tfc:mob_feet_armor"
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
        "tfc:welding/wrought_iron_boots",
        "tfg:compacting/wrought_iron_boots"
      ],
      "model_parents": [
        "item/metal/boots/wrought_iron",
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
          "text": "When on Feet:"
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
          "text": "§7Resistances: §fSlashing§r 3%, §fPiercing§r 5%, §fCrushing§r 5%"
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
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 429,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "feet"
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
          "value": "feet",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        }
      }
    },
    {
      "id": "tfc:metal/bucket/blue_steel",
      "namespace": "tfc",
      "display_name": "Blue Steel Bucket",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:buckets",
        "tfc:fluid_item_ingredient_empty_containers",
        "tfc:usable_on_tool_rack"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:compacting",
        "kubejs_tfc:advanced_shapeless_crafting",
        "tfc:advanced_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 35,
        "greate:compacting": 1,
        "kubejs_tfc:advanced_shapeless_crafting": 27,
        "tfc:advanced_shapeless_crafting": 38
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 101,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmaciv:crafting/barometer",
        "firmalife:crafting/masa_1",
        "firmalife:crafting/masa_2",
        "firmalife:crafting/masa_3",
        "firmalife:crafting/masa_4",
        "firmalife:crafting/masa_5",
        "firmalife:crafting/masa_6",
        "firmalife:crafting/masa_7",
        "firmalife:crafting/masa_8",
        "firmalife:crafting/rustic_finish",
        "greate:compacting/vanilla_bucket",
        "tfc:crafting/soil/loam_mud_1",
        "tfc:crafting/soil/loam_mud_2",
        "tfc:crafting/soil/loam_mud_3",
        "tfc:crafting/soil/loam_mud_4",
        "tfc:crafting/soil/loam_mud_5",
        "tfc:crafting/soil/loam_mud_6",
        "tfc:crafting/soil/loam_mud_7",
        "tfc:crafting/soil/loam_mud_8",
        "tfc:crafting/soil/sandy_loam_mud_1",
        "tfc:crafting/soil/sandy_loam_mud_2",
        "tfc:crafting/soil/sandy_loam_mud_3",
        "tfc:crafting/soil/sandy_loam_mud_4",
        "tfc:crafting/soil/sandy_loam_mud_5",
        "tfc:crafting/soil/sandy_loam_mud_6",
        "tfc:crafting/soil/sandy_loam_mud_7",
        "tfc:crafting/soil/sandy_loam_mud_8",
        "tfc:crafting/soil/silt_mud_1",
        "tfc:crafting/soil/silt_mud_2",
        "tfc:crafting/soil/silt_mud_4",
        "tfc:crafting/soil/silt_mud_5",
        "tfc:crafting/soil/silt_mud_6",
        "tfc:crafting/soil/silt_mud_7",
        "tfc:crafting/soil/silt_mud_8",
        "tfc:crafting/soil/silty_loam_mud_1",
        "tfc:crafting/soil/silty_loam_mud_2",
        "tfc:crafting/soil/silty_loam_mud_3",
        "tfc:crafting/soil/silty_loam_mud_4",
        "tfc:crafting/soil/silty_loam_mud_5",
        "tfc:crafting/soil/silty_loam_mud_6",
        "tfc:crafting/soil/silty_loam_mud_7",
        "tfc:crafting/soil/silty_loam_mud_8",
        "tfg:shapeless/barley_flatbread_dough",
        "tfg:shapeless/barley_flatbread_dough_2",
        "tfg:shapeless/barley_flatbread_dough_2_mixing",
        "tfg:shapeless/barley_flatbread_dough_3",
        "tfg:shapeless/barley_flatbread_dough_3_mixing",
        "tfg:shapeless/barley_flatbread_dough_4",
        "tfg:shapeless/barley_flatbread_dough_mixing",
        "tfg:shapeless/hardtack_dough",
        "tfg:shapeless/hardtack_dough_2",
        "tfg:shapeless/hardtack_dough_2_mixing",
        "tfg:shapeless/hardtack_dough_3",
        "tfg:shapeless/hardtack_dough_3_mixing",
        "tfg:shapeless/hardtack_dough_mixing",
        "tfg:shapeless/maize_flatbread_dough",
        "tfg:shapeless/maize_flatbread_dough_2",
        "tfg:shapeless/maize_flatbread_dough_2_mixing",
        "tfg:shapeless/maize_flatbread_dough_3",
        "tfg:shapeless/maize_flatbread_dough_3_mixing",
        "tfg:shapeless/maize_flatbread_dough_4",
        "tfg:shapeless/maize_flatbread_dough_4_mixing",
        "tfg:shapeless/maize_flatbread_dough_mixing",
        "tfg:shapeless/oat_flatbread_dough",
        "tfg:shapeless/oat_flatbread_dough_2",
        "tfg:shapeless/oat_flatbread_dough_2_mixing",
        "tfg:shapeless/oat_flatbread_dough_3",
        "tfg:shapeless/oat_flatbread_dough_4",
        "tfg:shapeless/oat_flatbread_dough_4_mixing",
        "tfg:shapeless/oat_flatbread_dough_mixing",
        "tfg:shapeless/raw_crepes",
        "tfg:shapeless/raw_croissants",
        "tfg:shapeless/raw_oladyi",
        "tfg:shapeless/rice_flatbread_dough",
        "tfg:shapeless/rice_flatbread_dough_2",
        "tfg:shapeless/rice_flatbread_dough_2_mixing",
        "tfg:shapeless/rice_flatbread_dough_3",
        "tfg:shapeless/rice_flatbread_dough_3_mixing",
        "tfg:shapeless/rice_flatbread_dough_4",
        "tfg:shapeless/rice_flatbread_dough_4_mixing",
        "tfg:shapeless/rice_flatbread_dough_mixing",
        "tfg:shapeless/rye_flatbread_dough",
        "tfg:shapeless/rye_flatbread_dough_2",
        "tfg:shapeless/rye_flatbread_dough_2_mixing",
        "tfg:shapeless/rye_flatbread_dough_3",
        "tfg:shapeless/rye_flatbread_dough_3_mixing",
        "tfg:shapeless/rye_flatbread_dough_4_mixing",
        "tfg:shapeless/rye_flatbread_dough_mixing",
        "tfg:shapeless/wheat_flatbread_dough",
        "tfg:shapeless/wheat_flatbread_dough_2",
        "tfg:shapeless/wheat_flatbread_dough_2_mixing",
        "tfg:shapeless/wheat_flatbread_dough_3",
        "tfg:shapeless/wheat_flatbread_dough_3_mixing",
        "tfg:shapeless/wheat_flatbread_dough_4",
        "tfg:shapeless/wheat_flatbread_dough_4_mixing",
        "tfg:shapeless/wheat_flatbread_dough_mixing"
      ],
      "recipe_output_examples": [
        "tfc:anvil/blue_steel_bucket"
      ],
      "recipe_examples_truncated": true,
      "model_parents": [
        "item/metal/bucket/blue_steel",
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
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
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
            "crafting",
            "greate:compacting",
            "kubejs_tfc:advanced_shapeless_crafting",
            "tfc:advanced_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfc:metal/bucket/red_steel",
      "namespace": "tfc",
      "display_name": "Red Steel Bucket",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:buckets",
        "tfc:fluid_item_ingredient_empty_containers",
        "tfc:usable_on_tool_rack"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:compacting",
        "kubejs_tfc:advanced_shapeless_crafting",
        "tfc:advanced_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 35,
        "greate:compacting": 1,
        "kubejs_tfc:advanced_shapeless_crafting": 27,
        "tfc:advanced_shapeless_crafting": 38
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 101,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmaciv:crafting/barometer",
        "firmalife:crafting/masa_1",
        "firmalife:crafting/masa_2",
        "firmalife:crafting/masa_3",
        "firmalife:crafting/masa_4",
        "firmalife:crafting/masa_5",
        "firmalife:crafting/masa_6",
        "firmalife:crafting/masa_7",
        "firmalife:crafting/masa_8",
        "firmalife:crafting/rustic_finish",
        "greate:compacting/vanilla_bucket",
        "tfc:crafting/soil/loam_mud_1",
        "tfc:crafting/soil/loam_mud_2",
        "tfc:crafting/soil/loam_mud_3",
        "tfc:crafting/soil/loam_mud_4",
        "tfc:crafting/soil/loam_mud_5",
        "tfc:crafting/soil/loam_mud_6",
        "tfc:crafting/soil/loam_mud_7",
        "tfc:crafting/soil/loam_mud_8",
        "tfc:crafting/soil/sandy_loam_mud_1",
        "tfc:crafting/soil/sandy_loam_mud_2",
        "tfc:crafting/soil/sandy_loam_mud_3",
        "tfc:crafting/soil/sandy_loam_mud_4",
        "tfc:crafting/soil/sandy_loam_mud_5",
        "tfc:crafting/soil/sandy_loam_mud_6",
        "tfc:crafting/soil/sandy_loam_mud_7",
        "tfc:crafting/soil/sandy_loam_mud_8",
        "tfc:crafting/soil/silt_mud_1",
        "tfc:crafting/soil/silt_mud_2",
        "tfc:crafting/soil/silt_mud_4",
        "tfc:crafting/soil/silt_mud_5",
        "tfc:crafting/soil/silt_mud_6",
        "tfc:crafting/soil/silt_mud_7",
        "tfc:crafting/soil/silt_mud_8",
        "tfc:crafting/soil/silty_loam_mud_1",
        "tfc:crafting/soil/silty_loam_mud_2",
        "tfc:crafting/soil/silty_loam_mud_3",
        "tfc:crafting/soil/silty_loam_mud_4",
        "tfc:crafting/soil/silty_loam_mud_5",
        "tfc:crafting/soil/silty_loam_mud_6",
        "tfc:crafting/soil/silty_loam_mud_7",
        "tfc:crafting/soil/silty_loam_mud_8",
        "tfg:shapeless/barley_flatbread_dough",
        "tfg:shapeless/barley_flatbread_dough_2",
        "tfg:shapeless/barley_flatbread_dough_2_mixing",
        "tfg:shapeless/barley_flatbread_dough_3",
        "tfg:shapeless/barley_flatbread_dough_3_mixing",
        "tfg:shapeless/barley_flatbread_dough_4",
        "tfg:shapeless/barley_flatbread_dough_mixing",
        "tfg:shapeless/hardtack_dough",
        "tfg:shapeless/hardtack_dough_2",
        "tfg:shapeless/hardtack_dough_2_mixing",
        "tfg:shapeless/hardtack_dough_3",
        "tfg:shapeless/hardtack_dough_3_mixing",
        "tfg:shapeless/hardtack_dough_mixing",
        "tfg:shapeless/maize_flatbread_dough",
        "tfg:shapeless/maize_flatbread_dough_2",
        "tfg:shapeless/maize_flatbread_dough_2_mixing",
        "tfg:shapeless/maize_flatbread_dough_3",
        "tfg:shapeless/maize_flatbread_dough_3_mixing",
        "tfg:shapeless/maize_flatbread_dough_4",
        "tfg:shapeless/maize_flatbread_dough_4_mixing",
        "tfg:shapeless/maize_flatbread_dough_mixing",
        "tfg:shapeless/oat_flatbread_dough",
        "tfg:shapeless/oat_flatbread_dough_2",
        "tfg:shapeless/oat_flatbread_dough_2_mixing",
        "tfg:shapeless/oat_flatbread_dough_3",
        "tfg:shapeless/oat_flatbread_dough_4",
        "tfg:shapeless/oat_flatbread_dough_4_mixing",
        "tfg:shapeless/oat_flatbread_dough_mixing",
        "tfg:shapeless/raw_crepes",
        "tfg:shapeless/raw_croissants",
        "tfg:shapeless/raw_oladyi",
        "tfg:shapeless/rice_flatbread_dough",
        "tfg:shapeless/rice_flatbread_dough_2",
        "tfg:shapeless/rice_flatbread_dough_2_mixing",
        "tfg:shapeless/rice_flatbread_dough_3",
        "tfg:shapeless/rice_flatbread_dough_3_mixing",
        "tfg:shapeless/rice_flatbread_dough_4",
        "tfg:shapeless/rice_flatbread_dough_4_mixing",
        "tfg:shapeless/rice_flatbread_dough_mixing",
        "tfg:shapeless/rye_flatbread_dough",
        "tfg:shapeless/rye_flatbread_dough_2",
        "tfg:shapeless/rye_flatbread_dough_2_mixing",
        "tfg:shapeless/rye_flatbread_dough_3",
        "tfg:shapeless/rye_flatbread_dough_3_mixing",
        "tfg:shapeless/rye_flatbread_dough_4_mixing",
        "tfg:shapeless/rye_flatbread_dough_mixing",
        "tfg:shapeless/wheat_flatbread_dough",
        "tfg:shapeless/wheat_flatbread_dough_2",
        "tfg:shapeless/wheat_flatbread_dough_2_mixing",
        "tfg:shapeless/wheat_flatbread_dough_3",
        "tfg:shapeless/wheat_flatbread_dough_3_mixing",
        "tfg:shapeless/wheat_flatbread_dough_4",
        "tfg:shapeless/wheat_flatbread_dough_4_mixing",
        "tfg:shapeless/wheat_flatbread_dough_mixing"
      ],
      "recipe_output_examples": [
        "tfc:anvil/red_steel_bucket"
      ],
      "recipe_examples_truncated": true,
      "model_parents": [
        "item/metal/bucket/red_steel",
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
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
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
            "crafting",
            "greate:compacting",
            "kubejs_tfc:advanced_shapeless_crafting",
            "tfc:advanced_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfc:metal/chain/bismuth_bronze",
      "namespace": "tfc",
      "display_name": "Bismuth Bronze Chain",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:chains",
        "forge:chains/bismuth_bronze",
        "tfc:metal_item/bismuth_bronze",
        "tfg:metal_chains"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 43,
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 45,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "afc:crafting/wood/hanging_sign/bismuth_bronze/baobab",
        "afc:crafting/wood/hanging_sign/bismuth_bronze/cypress",
        "afc:crafting/wood/hanging_sign/bismuth_bronze/eucalyptus",
        "afc:crafting/wood/hanging_sign/bismuth_bronze/fig",
        "afc:crafting/wood/hanging_sign/bismuth_bronze/hevea",
        "afc:crafting/wood/hanging_sign/bismuth_bronze/ipe",
        "afc:crafting/wood/hanging_sign/bismuth_bronze/ironwood",
        "afc:crafting/wood/hanging_sign/bismuth_bronze/mahogany",
        "afc:crafting/wood/hanging_sign/bismuth_bronze/teak",
        "afc:crafting/wood/hanging_sign/bismuth_bronze/tualang",
        "beneath:crafting/wood/hanging_sign/bismuth_bronze/crimson",
        "beneath:crafting/wood/hanging_sign/bismuth_bronze/warped",
        "framedblocks:framed_hanging_sign",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/acacia",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/ash",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/aspen",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/birch",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/blackwood",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/chestnut",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/douglas_fir",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/hickory",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/kapok",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/mangrove",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/maple",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/oak",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/palm",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/pine",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/rosewood",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/sequoia",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/spruce",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/sycamore",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/white_cedar",
        "tfc:crafting/wood/hanging_sign/bismuth_bronze/willow",
        "tfg:create/shaped/package_frogport",
        "tfg:create/shapeless/encased_chain_drive",
        "tfg:create/shapeless/minecart_coupling",
        "tfg:immersive_aircraft/shaped/heavy_crossbow",
        "tfg:shaped/aeronos_hanging_sign",
        "tfg:shaped/araucaria_hanging_sign",
        "tfg:shaped/beech_hanging_sign",
        "tfg:shaped/ginkgo_hanging_sign",
        "tfg:shaped/glacian_hanging_sign",
        "tfg:shaped/mahoe_hanging_sign",
        "tfg:shaped/strophar_hanging_sign",
        "tfg:shaped/wood_belt_connector"
      ],
      "recipe_output_examples": [
        "tfc:anvil/bismuth_bronze_chain"
      ],
      "model_parents": [
        "item/metal/chain/bismuth_bronze",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/metal/chain/bismuth_bronze"
      ],
      "block_context": {
        "block_id": "tfc:metal/chain/bismuth_bronze",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "BiZnCu₃"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 9 mB of §fBismuth Bronze§7 (at Orange٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
      "id": "tfc:metal/chain/black_bronze",
      "namespace": "tfc",
      "display_name": "Black Bronze Chain",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:chains",
        "forge:chains/black_bronze",
        "tfc:metal_item/black_bronze",
        "tfg:metal_chains"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 43,
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 45,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "afc:crafting/wood/hanging_sign/black_bronze/baobab",
        "afc:crafting/wood/hanging_sign/black_bronze/cypress",
        "afc:crafting/wood/hanging_sign/black_bronze/eucalyptus",
        "afc:crafting/wood/hanging_sign/black_bronze/fig",
        "afc:crafting/wood/hanging_sign/black_bronze/hevea",
        "afc:crafting/wood/hanging_sign/black_bronze/ipe",
        "afc:crafting/wood/hanging_sign/black_bronze/ironwood",
        "afc:crafting/wood/hanging_sign/black_bronze/mahogany",
        "afc:crafting/wood/hanging_sign/black_bronze/teak",
        "afc:crafting/wood/hanging_sign/black_bronze/tualang",
        "beneath:crafting/wood/hanging_sign/black_bronze/crimson",
        "beneath:crafting/wood/hanging_sign/black_bronze/warped",
        "framedblocks:framed_hanging_sign",
        "tfc:crafting/wood/hanging_sign/black_bronze/acacia",
        "tfc:crafting/wood/hanging_sign/black_bronze/ash",
        "tfc:crafting/wood/hanging_sign/black_bronze/aspen",
        "tfc:crafting/wood/hanging_sign/black_bronze/birch",
        "tfc:crafting/wood/hanging_sign/black_bronze/blackwood",
        "tfc:crafting/wood/hanging_sign/black_bronze/chestnut",
        "tfc:crafting/wood/hanging_sign/black_bronze/douglas_fir",
        "tfc:crafting/wood/hanging_sign/black_bronze/hickory",
        "tfc:crafting/wood/hanging_sign/black_bronze/kapok",
        "tfc:crafting/wood/hanging_sign/black_bronze/mangrove",
        "tfc:crafting/wood/hanging_sign/black_bronze/maple",
        "tfc:crafting/wood/hanging_sign/black_bronze/oak",
        "tfc:crafting/wood/hanging_sign/black_bronze/palm",
        "tfc:crafting/wood/hanging_sign/black_bronze/pine",
        "tfc:crafting/wood/hanging_sign/black_bronze/rosewood",
        "tfc:crafting/wood/hanging_sign/black_bronze/sequoia",
        "tfc:crafting/wood/hanging_sign/black_bronze/spruce",
        "tfc:crafting/wood/hanging_sign/black_bronze/sycamore",
        "tfc:crafting/wood/hanging_sign/black_bronze/white_cedar",
        "tfc:crafting/wood/hanging_sign/black_bronze/willow",
        "tfg:create/shaped/package_frogport",
        "tfg:create/shapeless/encased_chain_drive",
        "tfg:create/shapeless/minecart_coupling",
        "tfg:immersive_aircraft/shaped/heavy_crossbow",
        "tfg:shaped/aeronos_hanging_sign",
        "tfg:shaped/araucaria_hanging_sign",
        "tfg:shaped/beech_hanging_sign",
        "tfg:shaped/ginkgo_hanging_sign",
        "tfg:shaped/glacian_hanging_sign",
        "tfg:shaped/mahoe_hanging_sign",
        "tfg:shaped/strophar_hanging_sign",
        "tfg:shaped/wood_belt_connector"
      ],
      "recipe_output_examples": [
        "tfc:anvil/black_bronze_chain"
      ],
      "model_parents": [
        "item/metal/chain/black_bronze",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/metal/chain/black_bronze"
      ],
      "block_context": {
        "block_id": "tfc:metal/chain/black_bronze",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "AuAgCu₃"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 9 mB of §fBlack Bronze§7 (at Orange٭٭٭٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
      "id": "tfc:metal/chain/black_steel",
      "namespace": "tfc",
      "display_name": "Black Steel Chain",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:chains",
        "forge:chains/black_steel",
        "tfc:metal_item/black_steel",
        "tfg:metal_chains"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 43,
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 45,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "afc:crafting/wood/hanging_sign/black_steel/baobab",
        "afc:crafting/wood/hanging_sign/black_steel/cypress",
        "afc:crafting/wood/hanging_sign/black_steel/eucalyptus",
        "afc:crafting/wood/hanging_sign/black_steel/fig",
        "afc:crafting/wood/hanging_sign/black_steel/hevea",
        "afc:crafting/wood/hanging_sign/black_steel/ipe",
        "afc:crafting/wood/hanging_sign/black_steel/ironwood",
        "afc:crafting/wood/hanging_sign/black_steel/mahogany",
        "afc:crafting/wood/hanging_sign/black_steel/teak",
        "afc:crafting/wood/hanging_sign/black_steel/tualang",
        "beneath:crafting/wood/hanging_sign/black_steel/crimson",
        "beneath:crafting/wood/hanging_sign/black_steel/warped",
        "framedblocks:framed_hanging_sign",
        "tfc:crafting/wood/hanging_sign/black_steel/acacia",
        "tfc:crafting/wood/hanging_sign/black_steel/ash",
        "tfc:crafting/wood/hanging_sign/black_steel/aspen",
        "tfc:crafting/wood/hanging_sign/black_steel/birch",
        "tfc:crafting/wood/hanging_sign/black_steel/blackwood",
        "tfc:crafting/wood/hanging_sign/black_steel/chestnut",
        "tfc:crafting/wood/hanging_sign/black_steel/douglas_fir",
        "tfc:crafting/wood/hanging_sign/black_steel/hickory",
        "tfc:crafting/wood/hanging_sign/black_steel/kapok",
        "tfc:crafting/wood/hanging_sign/black_steel/mangrove",
        "tfc:crafting/wood/hanging_sign/black_steel/maple",
        "tfc:crafting/wood/hanging_sign/black_steel/oak",
        "tfc:crafting/wood/hanging_sign/black_steel/palm",
        "tfc:crafting/wood/hanging_sign/black_steel/pine",
        "tfc:crafting/wood/hanging_sign/black_steel/rosewood",
        "tfc:crafting/wood/hanging_sign/black_steel/sequoia",
        "tfc:crafting/wood/hanging_sign/black_steel/spruce",
        "tfc:crafting/wood/hanging_sign/black_steel/sycamore",
        "tfc:crafting/wood/hanging_sign/black_steel/white_cedar",
        "tfc:crafting/wood/hanging_sign/black_steel/willow",
        "tfg:create/shaped/package_frogport",
        "tfg:create/shapeless/encased_chain_drive",
        "tfg:create/shapeless/minecart_coupling",
        "tfg:immersive_aircraft/shaped/heavy_crossbow",
        "tfg:shaped/aeronos_hanging_sign",
        "tfg:shaped/araucaria_hanging_sign",
        "tfg:shaped/beech_hanging_sign",
        "tfg:shaped/ginkgo_hanging_sign",
        "tfg:shaped/glacian_hanging_sign",
        "tfg:shaped/mahoe_hanging_sign",
        "tfg:shaped/strophar_hanging_sign",
        "tfg:shaped/wood_belt_connector"
      ],
      "recipe_output_examples": [
        "tfc:anvil/black_steel_chain"
      ],
      "model_parents": [
        "item/metal/chain/black_steel",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/metal/chain/black_steel"
      ],
      "block_context": {
        "block_id": "tfc:metal/chain/black_steel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ni(AuAgCu₃)Fe₃"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 9 mB of §fBlack Steel§7 (at White٭٭٭٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
      "id": "tfc:metal/chain/blue_steel",
      "namespace": "tfc",
      "display_name": "Blue Steel Chain",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:chains",
        "forge:chains/blue_steel",
        "tfc:metal_item/blue_steel",
        "tfg:metal_chains"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 43,
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 45,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "afc:crafting/wood/hanging_sign/blue_steel/baobab",
        "afc:crafting/wood/hanging_sign/blue_steel/cypress",
        "afc:crafting/wood/hanging_sign/blue_steel/eucalyptus",
        "afc:crafting/wood/hanging_sign/blue_steel/fig",
        "afc:crafting/wood/hanging_sign/blue_steel/hevea",
        "afc:crafting/wood/hanging_sign/blue_steel/ipe",
        "afc:crafting/wood/hanging_sign/blue_steel/ironwood",
        "afc:crafting/wood/hanging_sign/blue_steel/mahogany",
        "afc:crafting/wood/hanging_sign/blue_steel/teak",
        "afc:crafting/wood/hanging_sign/blue_steel/tualang",
        "beneath:crafting/wood/hanging_sign/blue_steel/crimson",
        "beneath:crafting/wood/hanging_sign/blue_steel/warped",
        "framedblocks:framed_hanging_sign",
        "tfc:crafting/wood/hanging_sign/blue_steel/acacia",
        "tfc:crafting/wood/hanging_sign/blue_steel/ash",
        "tfc:crafting/wood/hanging_sign/blue_steel/aspen",
        "tfc:crafting/wood/hanging_sign/blue_steel/birch",
        "tfc:crafting/wood/hanging_sign/blue_steel/blackwood",
        "tfc:crafting/wood/hanging_sign/blue_steel/chestnut",
        "tfc:crafting/wood/hanging_sign/blue_steel/douglas_fir",
        "tfc:crafting/wood/hanging_sign/blue_steel/hickory",
        "tfc:crafting/wood/hanging_sign/blue_steel/kapok",
        "tfc:crafting/wood/hanging_sign/blue_steel/mangrove",
        "tfc:crafting/wood/hanging_sign/blue_steel/maple",
        "tfc:crafting/wood/hanging_sign/blue_steel/oak",
        "tfc:crafting/wood/hanging_sign/blue_steel/palm",
        "tfc:crafting/wood/hanging_sign/blue_steel/pine",
        "tfc:crafting/wood/hanging_sign/blue_steel/rosewood",
        "tfc:crafting/wood/hanging_sign/blue_steel/sequoia",
        "tfc:crafting/wood/hanging_sign/blue_steel/spruce",
        "tfc:crafting/wood/hanging_sign/blue_steel/sycamore",
        "tfc:crafting/wood/hanging_sign/blue_steel/white_cedar",
        "tfc:crafting/wood/hanging_sign/blue_steel/willow",
        "tfg:create/shaped/package_frogport",
        "tfg:create/shapeless/encased_chain_drive",
        "tfg:create/shapeless/minecart_coupling",
        "tfg:immersive_aircraft/shaped/heavy_crossbow",
        "tfg:shaped/aeronos_hanging_sign",
        "tfg:shaped/araucaria_hanging_sign",
        "tfg:shaped/beech_hanging_sign",
        "tfg:shaped/ginkgo_hanging_sign",
        "tfg:shaped/glacian_hanging_sign",
        "tfg:shaped/mahoe_hanging_sign",
        "tfg:shaped/strophar_hanging_sign",
        "tfg:shaped/wood_belt_connector"
      ],
      "recipe_output_examples": [
        "tfc:anvil/blue_steel_chain"
      ],
      "model_parents": [
        "item/metal/chain/blue_steel",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/metal/chain/blue_steel"
      ],
      "block_context": {
        "block_id": "tfc:metal/chain/blue_steel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "(CuAg₄)(BiZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 9 mB of §fBlue Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
      "id": "tfc:metal/chain/bronze",
      "namespace": "tfc",
      "display_name": "Bronze Chain",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:chains",
        "forge:chains/bronze",
        "tfc:metal_item/bronze",
        "tfg:metal_chains"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 43,
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 45,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "afc:crafting/wood/hanging_sign/bronze/baobab",
        "afc:crafting/wood/hanging_sign/bronze/cypress",
        "afc:crafting/wood/hanging_sign/bronze/eucalyptus",
        "afc:crafting/wood/hanging_sign/bronze/fig",
        "afc:crafting/wood/hanging_sign/bronze/hevea",
        "afc:crafting/wood/hanging_sign/bronze/ipe",
        "afc:crafting/wood/hanging_sign/bronze/ironwood",
        "afc:crafting/wood/hanging_sign/bronze/mahogany",
        "afc:crafting/wood/hanging_sign/bronze/teak",
        "afc:crafting/wood/hanging_sign/bronze/tualang",
        "beneath:crafting/wood/hanging_sign/bronze/crimson",
        "beneath:crafting/wood/hanging_sign/bronze/warped",
        "framedblocks:framed_hanging_sign",
        "tfc:crafting/wood/hanging_sign/bronze/acacia",
        "tfc:crafting/wood/hanging_sign/bronze/ash",
        "tfc:crafting/wood/hanging_sign/bronze/aspen",
        "tfc:crafting/wood/hanging_sign/bronze/birch",
        "tfc:crafting/wood/hanging_sign/bronze/blackwood",
        "tfc:crafting/wood/hanging_sign/bronze/chestnut",
        "tfc:crafting/wood/hanging_sign/bronze/douglas_fir",
        "tfc:crafting/wood/hanging_sign/bronze/hickory",
        "tfc:crafting/wood/hanging_sign/bronze/kapok",
        "tfc:crafting/wood/hanging_sign/bronze/mangrove",
        "tfc:crafting/wood/hanging_sign/bronze/maple",
        "tfc:crafting/wood/hanging_sign/bronze/oak",
        "tfc:crafting/wood/hanging_sign/bronze/palm",
        "tfc:crafting/wood/hanging_sign/bronze/pine",
        "tfc:crafting/wood/hanging_sign/bronze/rosewood",
        "tfc:crafting/wood/hanging_sign/bronze/sequoia",
        "tfc:crafting/wood/hanging_sign/bronze/spruce",
        "tfc:crafting/wood/hanging_sign/bronze/sycamore",
        "tfc:crafting/wood/hanging_sign/bronze/white_cedar",
        "tfc:crafting/wood/hanging_sign/bronze/willow",
        "tfg:create/shaped/package_frogport",
        "tfg:create/shapeless/encased_chain_drive",
        "tfg:create/shapeless/minecart_coupling",
        "tfg:immersive_aircraft/shaped/heavy_crossbow",
        "tfg:shaped/aeronos_hanging_sign",
        "tfg:shaped/araucaria_hanging_sign",
        "tfg:shaped/beech_hanging_sign",
        "tfg:shaped/ginkgo_hanging_sign",
        "tfg:shaped/glacian_hanging_sign",
        "tfg:shaped/mahoe_hanging_sign",
        "tfg:shaped/strophar_hanging_sign",
        "tfg:shaped/wood_belt_connector"
      ],
      "recipe_output_examples": [
        "tfc:anvil/bronze_chain"
      ],
      "model_parents": [
        "item/metal/chain/bronze",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/metal/chain/bronze"
      ],
      "block_context": {
        "block_id": "tfc:metal/chain/bronze",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "SnCu₃"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 9 mB of §fBronze§7 (at Orange§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
      "id": "tfc:metal/chain/copper",
      "namespace": "tfc",
      "display_name": "Copper Chain",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:chains",
        "forge:chains/copper",
        "tfc:metal_item/copper",
        "tfg:metal_chains"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 43,
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 45,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "afc:crafting/wood/hanging_sign/copper/baobab",
        "afc:crafting/wood/hanging_sign/copper/cypress",
        "afc:crafting/wood/hanging_sign/copper/eucalyptus",
        "afc:crafting/wood/hanging_sign/copper/fig",
        "afc:crafting/wood/hanging_sign/copper/hevea",
        "afc:crafting/wood/hanging_sign/copper/ipe",
        "afc:crafting/wood/hanging_sign/copper/ironwood",
        "afc:crafting/wood/hanging_sign/copper/mahogany",
        "afc:crafting/wood/hanging_sign/copper/teak",
        "afc:crafting/wood/hanging_sign/copper/tualang",
        "beneath:crafting/wood/hanging_sign/copper/crimson",
        "beneath:crafting/wood/hanging_sign/copper/warped",
        "framedblocks:framed_hanging_sign",
        "tfc:crafting/wood/hanging_sign/copper/acacia",
        "tfc:crafting/wood/hanging_sign/copper/ash",
        "tfc:crafting/wood/hanging_sign/copper/aspen",
        "tfc:crafting/wood/hanging_sign/copper/birch",
        "tfc:crafting/wood/hanging_sign/copper/blackwood",
        "tfc:crafting/wood/hanging_sign/copper/chestnut",
        "tfc:crafting/wood/hanging_sign/copper/douglas_fir",
        "tfc:crafting/wood/hanging_sign/copper/hickory",
        "tfc:crafting/wood/hanging_sign/copper/kapok",
        "tfc:crafting/wood/hanging_sign/copper/mangrove",
        "tfc:crafting/wood/hanging_sign/copper/maple",
        "tfc:crafting/wood/hanging_sign/copper/oak",
        "tfc:crafting/wood/hanging_sign/copper/palm",
        "tfc:crafting/wood/hanging_sign/copper/pine",
        "tfc:crafting/wood/hanging_sign/copper/rosewood",
        "tfc:crafting/wood/hanging_sign/copper/sequoia",
        "tfc:crafting/wood/hanging_sign/copper/spruce",
        "tfc:crafting/wood/hanging_sign/copper/sycamore",
        "tfc:crafting/wood/hanging_sign/copper/white_cedar",
        "tfc:crafting/wood/hanging_sign/copper/willow",
        "tfg:create/shaped/package_frogport",
        "tfg:create/shapeless/encased_chain_drive",
        "tfg:create/shapeless/minecart_coupling",
        "tfg:immersive_aircraft/shaped/heavy_crossbow",
        "tfg:shaped/aeronos_hanging_sign",
        "tfg:shaped/araucaria_hanging_sign",
        "tfg:shaped/beech_hanging_sign",
        "tfg:shaped/ginkgo_hanging_sign",
        "tfg:shaped/glacian_hanging_sign",
        "tfg:shaped/mahoe_hanging_sign",
        "tfg:shaped/strophar_hanging_sign",
        "tfg:shaped/wood_belt_connector"
      ],
      "recipe_output_examples": [
        "tfc:anvil/copper_chain"
      ],
      "model_parents": [
        "item/metal/chain/copper",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/metal/chain/copper"
      ],
      "block_context": {
        "block_id": "tfc:metal/chain/copper",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
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
          "text": "§7Melts into 9 mB of §fCopper§7 (at Orange٭٭٭٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
      "id": "tfc:metal/chain/red_steel",
      "namespace": "tfc",
      "display_name": "Red Steel Chain",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:chains",
        "forge:chains/red_steel",
        "tfc:metal_item/red_steel",
        "tfg:metal_chains"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 43,
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 45,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "afc:crafting/wood/hanging_sign/red_steel/baobab",
        "afc:crafting/wood/hanging_sign/red_steel/cypress",
        "afc:crafting/wood/hanging_sign/red_steel/eucalyptus",
        "afc:crafting/wood/hanging_sign/red_steel/fig",
        "afc:crafting/wood/hanging_sign/red_steel/hevea",
        "afc:crafting/wood/hanging_sign/red_steel/ipe",
        "afc:crafting/wood/hanging_sign/red_steel/ironwood",
        "afc:crafting/wood/hanging_sign/red_steel/mahogany",
        "afc:crafting/wood/hanging_sign/red_steel/teak",
        "afc:crafting/wood/hanging_sign/red_steel/tualang",
        "beneath:crafting/wood/hanging_sign/red_steel/crimson",
        "beneath:crafting/wood/hanging_sign/red_steel/warped",
        "framedblocks:framed_hanging_sign",
        "tfc:crafting/wood/hanging_sign/red_steel/acacia",
        "tfc:crafting/wood/hanging_sign/red_steel/ash",
        "tfc:crafting/wood/hanging_sign/red_steel/aspen",
        "tfc:crafting/wood/hanging_sign/red_steel/birch",
        "tfc:crafting/wood/hanging_sign/red_steel/blackwood",
        "tfc:crafting/wood/hanging_sign/red_steel/chestnut",
        "tfc:crafting/wood/hanging_sign/red_steel/douglas_fir",
        "tfc:crafting/wood/hanging_sign/red_steel/hickory",
        "tfc:crafting/wood/hanging_sign/red_steel/kapok",
        "tfc:crafting/wood/hanging_sign/red_steel/mangrove",
        "tfc:crafting/wood/hanging_sign/red_steel/maple",
        "tfc:crafting/wood/hanging_sign/red_steel/oak",
        "tfc:crafting/wood/hanging_sign/red_steel/palm",
        "tfc:crafting/wood/hanging_sign/red_steel/pine",
        "tfc:crafting/wood/hanging_sign/red_steel/rosewood",
        "tfc:crafting/wood/hanging_sign/red_steel/sequoia",
        "tfc:crafting/wood/hanging_sign/red_steel/spruce",
        "tfc:crafting/wood/hanging_sign/red_steel/sycamore",
        "tfc:crafting/wood/hanging_sign/red_steel/white_cedar",
        "tfc:crafting/wood/hanging_sign/red_steel/willow",
        "tfg:create/shaped/package_frogport",
        "tfg:create/shapeless/encased_chain_drive",
        "tfg:create/shapeless/minecart_coupling",
        "tfg:immersive_aircraft/shaped/heavy_crossbow",
        "tfg:shaped/aeronos_hanging_sign",
        "tfg:shaped/araucaria_hanging_sign",
        "tfg:shaped/beech_hanging_sign",
        "tfg:shaped/ginkgo_hanging_sign",
        "tfg:shaped/glacian_hanging_sign",
        "tfg:shaped/mahoe_hanging_sign",
        "tfg:shaped/strophar_hanging_sign",
        "tfg:shaped/wood_belt_connector"
      ],
      "recipe_output_examples": [
        "tfc:anvil/red_steel_chain"
      ],
      "model_parents": [
        "item/metal/chain/red_steel",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/metal/chain/red_steel"
      ],
      "block_context": {
        "block_id": "tfc:metal/chain/red_steel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
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
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 9 mB of §fRed Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
      "id": "tfc:metal/chain/steel",
      "namespace": "tfc",
      "display_name": "Steel Chain",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:chains",
        "forge:chains/steel",
        "tfc:metal_item/steel",
        "tfg:metal_chains"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 44,
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 46,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "afc:crafting/wood/hanging_sign/steel/baobab",
        "afc:crafting/wood/hanging_sign/steel/cypress",
        "afc:crafting/wood/hanging_sign/steel/eucalyptus",
        "afc:crafting/wood/hanging_sign/steel/fig",
        "afc:crafting/wood/hanging_sign/steel/hevea",
        "afc:crafting/wood/hanging_sign/steel/ipe",
        "afc:crafting/wood/hanging_sign/steel/ironwood",
        "afc:crafting/wood/hanging_sign/steel/mahogany",
        "afc:crafting/wood/hanging_sign/steel/teak",
        "afc:crafting/wood/hanging_sign/steel/tualang",
        "beneath:crafting/wood/hanging_sign/steel/crimson",
        "beneath:crafting/wood/hanging_sign/steel/warped",
        "framedblocks:framed_hanging_sign",
        "tfc:crafting/wood/hanging_sign/steel/acacia",
        "tfc:crafting/wood/hanging_sign/steel/ash",
        "tfc:crafting/wood/hanging_sign/steel/aspen",
        "tfc:crafting/wood/hanging_sign/steel/birch",
        "tfc:crafting/wood/hanging_sign/steel/blackwood",
        "tfc:crafting/wood/hanging_sign/steel/chestnut",
        "tfc:crafting/wood/hanging_sign/steel/douglas_fir",
        "tfc:crafting/wood/hanging_sign/steel/hickory",
        "tfc:crafting/wood/hanging_sign/steel/kapok",
        "tfc:crafting/wood/hanging_sign/steel/mangrove",
        "tfc:crafting/wood/hanging_sign/steel/maple",
        "tfc:crafting/wood/hanging_sign/steel/oak",
        "tfc:crafting/wood/hanging_sign/steel/palm",
        "tfc:crafting/wood/hanging_sign/steel/pine",
        "tfc:crafting/wood/hanging_sign/steel/rosewood",
        "tfc:crafting/wood/hanging_sign/steel/sequoia",
        "tfc:crafting/wood/hanging_sign/steel/spruce",
        "tfc:crafting/wood/hanging_sign/steel/sycamore",
        "tfc:crafting/wood/hanging_sign/steel/white_cedar",
        "tfc:crafting/wood/hanging_sign/steel/willow",
        "tfg:create/shaped/package_frogport",
        "tfg:create/shapeless/encased_chain_drive",
        "tfg:create/shapeless/minecart_coupling",
        "tfg:immersive_aircraft/shaped/heavy_crossbow",
        "tfg:shaped/aeronos_hanging_sign",
        "tfg:shaped/araucaria_hanging_sign",
        "tfg:shaped/beech_hanging_sign",
        "tfg:shaped/crankbow",
        "tfg:shaped/ginkgo_hanging_sign",
        "tfg:shaped/glacian_hanging_sign",
        "tfg:shaped/mahoe_hanging_sign",
        "tfg:shaped/strophar_hanging_sign",
        "tfg:shaped/wood_belt_connector"
      ],
      "recipe_output_examples": [
        "tfc:anvil/steel_chain"
      ],
      "model_parents": [
        "item/metal/chain/steel",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/metal/chain/steel"
      ],
      "block_context": {
        "block_id": "tfc:metal/chain/steel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
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
          "text": "§7Melts into 9 mB of §fSteel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
      "id": "tfc:metal/chain/wrought_iron",
      "namespace": "tfc",
      "display_name": "Wrought Iron Chain",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:chains",
        "forge:chains/wrought_iron",
        "tfc:metal_item/wrought_iron",
        "tfg:metal_chains"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 43,
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 45,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "afc:crafting/wood/hanging_sign/wrought_iron/baobab",
        "afc:crafting/wood/hanging_sign/wrought_iron/cypress",
        "afc:crafting/wood/hanging_sign/wrought_iron/eucalyptus",
        "afc:crafting/wood/hanging_sign/wrought_iron/fig",
        "afc:crafting/wood/hanging_sign/wrought_iron/hevea",
        "afc:crafting/wood/hanging_sign/wrought_iron/ipe",
        "afc:crafting/wood/hanging_sign/wrought_iron/ironwood",
        "afc:crafting/wood/hanging_sign/wrought_iron/mahogany",
        "afc:crafting/wood/hanging_sign/wrought_iron/teak",
        "afc:crafting/wood/hanging_sign/wrought_iron/tualang",
        "beneath:crafting/wood/hanging_sign/wrought_iron/crimson",
        "beneath:crafting/wood/hanging_sign/wrought_iron/warped",
        "framedblocks:framed_hanging_sign",
        "tfc:crafting/wood/hanging_sign/wrought_iron/acacia",
        "tfc:crafting/wood/hanging_sign/wrought_iron/ash",
        "tfc:crafting/wood/hanging_sign/wrought_iron/aspen",
        "tfc:crafting/wood/hanging_sign/wrought_iron/birch",
        "tfc:crafting/wood/hanging_sign/wrought_iron/blackwood",
        "tfc:crafting/wood/hanging_sign/wrought_iron/chestnut",
        "tfc:crafting/wood/hanging_sign/wrought_iron/douglas_fir",
        "tfc:crafting/wood/hanging_sign/wrought_iron/hickory",
        "tfc:crafting/wood/hanging_sign/wrought_iron/kapok",
        "tfc:crafting/wood/hanging_sign/wrought_iron/mangrove",
        "tfc:crafting/wood/hanging_sign/wrought_iron/maple",
        "tfc:crafting/wood/hanging_sign/wrought_iron/oak",
        "tfc:crafting/wood/hanging_sign/wrought_iron/palm",
        "tfc:crafting/wood/hanging_sign/wrought_iron/pine",
        "tfc:crafting/wood/hanging_sign/wrought_iron/rosewood",
        "tfc:crafting/wood/hanging_sign/wrought_iron/sequoia",
        "tfc:crafting/wood/hanging_sign/wrought_iron/spruce",
        "tfc:crafting/wood/hanging_sign/wrought_iron/sycamore",
        "tfc:crafting/wood/hanging_sign/wrought_iron/white_cedar",
        "tfc:crafting/wood/hanging_sign/wrought_iron/willow",
        "tfg:create/shaped/package_frogport",
        "tfg:create/shapeless/encased_chain_drive",
        "tfg:create/shapeless/minecart_coupling",
        "tfg:immersive_aircraft/shaped/heavy_crossbow",
        "tfg:shaped/aeronos_hanging_sign",
        "tfg:shaped/araucaria_hanging_sign",
        "tfg:shaped/beech_hanging_sign",
        "tfg:shaped/ginkgo_hanging_sign",
        "tfg:shaped/glacian_hanging_sign",
        "tfg:shaped/mahoe_hanging_sign",
        "tfg:shaped/strophar_hanging_sign",
        "tfg:shaped/wood_belt_connector"
      ],
      "recipe_output_examples": [
        "tfc:anvil/wrought_iron_chain"
      ],
      "model_parents": [
        "item/metal/chain/wrought_iron",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:metals"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/metal/chain/wrought_iron"
      ],
      "block_context": {
        "block_id": "tfc:metal/chain/wrought_iron",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
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
          "text": "§7Melts into 9 mB of §fCast Iron§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
      "id": "tfc:metal/chestplate/bismuth_bronze",
      "namespace": "tfc",
      "display_name": "Bismuth Bronze Chestplate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trimmable_armor",
        "tfc:metal_item/bismuth_bronze",
        "tfc:mob_chest_armor"
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
        "tfc:welding/bismuth_bronze_chestplate",
        "tfg:compacting/bismuth_bronze_chestplate"
      ],
      "model_parents": [
        "item/metal/chestplate/bismuth_bronze",
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
          "text": "When on Body:"
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
          "text": "§7Resistances: §fSlashing§r 2%, §fPiercing§r 2%, §fCrushing§r 4%"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 576 mB of §fBismuth Bronze§7 (at Orange٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 311,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "chest"
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
          "value": "chest",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        }
      }
    },
    {
      "id": "tfc:metal/chestplate/black_bronze",
      "namespace": "tfc",
      "display_name": "Black Bronze Chestplate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trimmable_armor",
        "tfc:metal_item/black_bronze",
        "tfc:mob_chest_armor"
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
        "tfc:welding/black_bronze_chestplate",
        "tfg:compacting/black_bronze_chestplate"
      ],
      "model_parents": [
        "item/metal/chestplate/black_bronze",
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
          "text": "When on Body:"
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
          "text": "§7Resistances: §fSlashing§r 2%, §fPiercing§r 4%, §fCrushing§r 2%"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 576 mB of §fBlack Bronze§7 (at Orange٭٭٭٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 336,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "chest"
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
          "value": "chest",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        }
      }
    },
    {
      "id": "tfc:metal/chestplate/black_steel",
      "namespace": "tfc",
      "display_name": "Black Steel Chestplate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trimmable_armor",
        "tfc:metal_item/black_steel"
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
        "tfc:welding/black_steel_chestplate",
        "tfg:compacting/black_steel_chestplate"
      ],
      "model_parents": [
        "item/metal/chestplate/black_steel",
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
          "text": "When on Body:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+6 Armor"
        },
        {
          "source": "runtime-tooltip",
          "text": "+2 Armor Toughness"
        },
        {
          "source": "runtime-tooltip",
          "text": "+0.5 Knockback Resistance"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Resistances: §fSlashing§r 8%, §fPiercing§r 11%, §fCrushing§r 12%"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 576 mB of §fBlack Steel§7 (at White٭٭٭٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 800,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "chest"
        },
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
        "equip_slot": {
          "value": "chest",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
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
      "id": "tfc:metal/chestplate/blue_steel",
      "namespace": "tfc",
      "display_name": "Blue Steel Chestplate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trimmable_armor",
        "tfc:metal_item/blue_steel",
        "tfg:hot_protection_equipment"
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
        "tfg:create/shaped/netherite_backtank"
      ],
      "recipe_output_examples": [
        "tfc:welding/blue_steel_chestplate",
        "tfg:compacting/blue_steel_chestplate"
      ],
      "model_parents": [
        "item/metal/chestplate/blue_steel",
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
          "text": "§7Provides protection from all §6hot§7 held items and fluids (chest) and pipes (boots).§r"
        },
        {
          "source": "runtime-tooltip",
          "text": "When on Body:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+8 Armor"
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
          "text": "§7Melts into 576 mB of §fBlue Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 1088,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "chest"
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
          "value": "chest",
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
      "id": "tfc:metal/chestplate/bronze",
      "namespace": "tfc",
      "display_name": "Bronze Chestplate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trimmable_armor",
        "tfc:metal_item/bronze",
        "tfc:mob_chest_armor"
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
        "tfc:welding/bronze_chestplate",
        "tfg:compacting/bronze_chestplate"
      ],
      "model_parents": [
        "item/metal/chestplate/bronze",
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
          "text": "When on Body:"
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
          "text": "§7Melts into 576 mB of §fBronze§7 (at Orange§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 323,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "chest"
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
          "value": "chest",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        }
      }
    },
    {
      "id": "tfc:metal/chestplate/copper",
      "namespace": "tfc",
      "display_name": "Copper Chestplate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trimmable_armor",
        "tfc:metal_item/copper",
        "tfc:mob_chest_armor"
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
        "tfg:create/shaped/copper_backtank"
      ],
      "recipe_output_examples": [
        "tfc:welding/copper_chestplate",
        "tfg:compacting/copper_chestplate"
      ],
      "model_parents": [
        "item/metal/chestplate/copper",
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
          "text": "When on Body:"
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
          "text": "§7Resistances: §fSlashing§r 2%, §fPiercing§r 2%, §fCrushing§r 2%"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 576 mB of §fCopper§7 (at Orange٭٭٭٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "document_context": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/armor",
          "label": "Armor",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "Armor provides protection against attacks from predators and monsters. The quality of armor scales with the tier of the Metal, with Leather being the weakest and Colored Steels being the strongest."
            },
            {
              "source": "guide-page",
              "key": "pages.1.text",
              "text": "Leather armor is Knapped from Leather. It does not last long, but provides some decent protection if you have a full suit."
            },
            {
              "source": "guide-page",
              "key": "pages.2.text",
              "text": "Metal Armor requires multiple processing steps in an Anvil. First, an unfinished armor piece must be smithed. These require a Double Sheet of the metal, except for boots which require a single sheet."
            },
            {
              "source": "guide-page",
              "key": "pages.3.text",
              "text": "Next, a Sheet must be Welded to the armor piece to finish it. Chestplates require a Double Sheet to be finished."
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 215,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "chest"
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
          "value": "chest",
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
    },
    {
      "id": "tfc:metal/chestplate/red_steel",
      "namespace": "tfc",
      "display_name": "Red Steel Chestplate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:freeze_immune_wearables",
        "minecraft:trimmable_armor",
        "tfc:metal_item/red_steel",
        "tfg:cold_protection_equipment"
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
        "tfc:welding/red_steel_chestplate",
        "tfg:compacting/red_steel_chestplate"
      ],
      "model_parents": [
        "item/metal/chestplate/red_steel",
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
          "text": "§7Provides protection from all §bcold§7 held items and fluids (chest) and pipes (boots).§r"
        },
        {
          "source": "runtime-tooltip",
          "text": "When on Body:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+8 Armor"
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
          "text": "§7Melts into 576 mB of §fRed Steel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 1010,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "chest"
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
          "value": "chest",
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
      "id": "tfc:metal/chestplate/steel",
      "namespace": "tfc",
      "display_name": "Steel Chestplate",
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
        "tfc:welding/steel_chestplate",
        "tfg:compacting/steel_chestplate"
      ],
      "model_parents": [
        "item/metal/chestplate/steel",
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
          "text": "When on Body:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+6 Armor"
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
          "text": "§7Melts into 576 mB of §fSteel§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 640,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "chest"
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
          "value": "chest",
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
      "id": "tfc:metal/chestplate/wrought_iron",
      "namespace": "tfc",
      "display_name": "Wrought Iron Chestplate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trimmable_armor",
        "tfc:metal_item/wrought_iron",
        "tfc:mob_chest_armor"
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
        "tfc:welding/wrought_iron_chestplate",
        "tfg:compacting/wrought_iron_chestplate"
      ],
      "model_parents": [
        "item/metal/chestplate/wrought_iron",
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
          "text": "When on Body:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+5 Armor"
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
          "text": "§7Melts into 576 mB of §fCast Iron§7 (at Brilliant White§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 528,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "chest"
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
          "value": "chest",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        }
      }
    },
    {
      "id": "tfc:metal/chisel/bismuth_bronze",
      "namespace": "tfc",
      "display_name": "Bismuth Bronze Chisel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:breaks_decorated_pots",
        "minecraft:tools",
        "tfc:chisels",
        "tfc:metal_item/bismuth_bronze",
        "tfc:metal_item/bismuth_bronze_tools",
        "tfc:usable_on_tool_rack"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shaped_crafting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shaped_crafting": 65,
        "tfc:damage_inputs_shapeless_crafting": 301
      },
      "recipe_production_by_type": {
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_ingredient_count": 366,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "beneath:crafting/blackstone_brick",
        "create:shapeless/chisel_cut_diorite",
        "firmalife:crafting/brick_countertop",
        "firmalife:crafting/cleaning/rusted_iron_greenhouse_door",
        "firmalife:crafting/polished_sealed_bricks",
        "gtceu:shaped/mortar_bismuth_bronze",
        "gtceu:shaped/mortar_steel",
        "minecraft:shapeless/apatite_bud_indicator",
        "minecraft:shapeless/cinnabar_bud_indicator",
        "minecraft:shapeless/green_sapphire_bud_indicator",
        "minecraft:shapeless/lazurite_bud_indicator",
        "minecraft:shapeless/olivine_bud_indicator",
        "minecraft:shapeless/realgar_bud_indicator",
        "minecraft:shapeless/ruby_bud_indicator",
        "minecraft:shapeless/spessartine_bud_indicator",
        "rnr:crafting/flagstone/basalt",
        "rnr:crafting/flagstone/chalk",
        "rnr:crafting/flagstone/dacite",
        "rnr:crafting/flagstone/gneiss",
        "rnr:crafting/flagstone/marble",
        "rnr:crafting/flagstone/red_sandstone",
        "rnr:crafting/flagstone/slate",
        "rnr:crafting/shingle/ash",
        "rnr:crafting/shingle/birch",
        "rnr:crafting/shingle/douglas_fir",
        "rnr:crafting/shingle/hickory",
        "rnr:crafting/shingle/mahogany",
        "rnr:crafting/shingle/palm",
        "rnr:crafting/shingle/spruce",
        "rnr:crafting/shingle/tualang",
        "tfc:crafting/alabaster_brick/raw_gypsum",
        "tfc:crafting/rock/andesite_chiseled",
        "tfc:crafting/rock/basalt_chiseled",
        "tfc:crafting/rock/chalk_chiseled",
        "tfc:crafting/rock/chert_chiseled",
        "tfc:crafting/rock/claystone_brick_from_mossy",
        "tfc:crafting/rock/conglomerate_brick_from_mossy",
        "tfc:crafting/rock/dacite_brick_from_mossy",
        "tfc:crafting/rock/diorite_brick_from_mossy",
        "tfc:crafting/rock/dolomite_brick_from_mossy",
        "tfc:crafting/rock/gabbro_brick_from_mossy",
        "tfc:crafting/rock/gneiss_brick_from_mossy",
        "tfc:crafting/rock/granite_brick",
        "tfc:crafting/rock/limestone_brick",
        "tfc:crafting/rock/marble_brick",
        "tfc:crafting/rock/phyllite_brick",
        "tfc:crafting/rock/quartzite_brick",
        "tfc:crafting/rock/rhyolite_brick",
        "tfc:crafting/rock/rhyolite_smooth",
        "tfc:crafting/rock/schist_smooth",
        "tfc:crafting/rock/shale_smooth",
        "tfc:crafting/rock/slate_smooth",
        "tfc:crafting/sandstone/brown_smooth",
        "tfc:crafting/sandstone/pink_smooth",
        "tfc:crafting/sandstone/white_cut",
        "tfc:kjs/damage/kjs/tfg_etching_diamond_tip",
        "tfc:kjs/damage/shaped/basalt_support",
        "tfc:kjs/damage/shaped/chert_mossy_support",
        "tfc:kjs/damage/shaped/conglomerate_mossy_support",
        "tfc:kjs/damage/shaped/dacite_support",
        "tfc:kjs/damage/shaped/diorite_support",
        "tfc:kjs/damage/shaped/dripstone_support",
        "tfc:kjs/damage/shaped/glacio_stone_support",
        "tfc:kjs/damage/shaped/granite_support",
        "tfc:kjs/damage/shaped/marble_mossy_support",
        "tfc:kjs/damage/shaped/moon_deepslate_support",
        "tfc:kjs/damage/shaped/phyllite_support",
        "tfc:kjs/damage/shaped/red_granite_support",
        "tfc:kjs/damage/shaped/schist_mossy_support",
        "tfc:kjs/damage/shaped/shale_mossy_support",
        "tfc:kjs/damage/shaped/tuff_support",
        "tfg:shapeless/bamboo_mosaic_slab",
        "tfg:shapeless/blackstone_loose_to_brick",
        "tfg:shapeless/calcite_from_raw",
        "tfg:shapeless/crackrack_bricks_to_chiseled",
        "tfg:shapeless/crimsite_raw_to_polished",
        "tfg:shapeless/deepslate_hardened_to_polished",
        "tfg:shapeless/dripstone_loose_to_brick",
        "tfg:shapeless/flavolite_loose_to_brick",
        "tfg:shapeless/glacio_stone_loose_to_brick",
        "tfg:shapeless/light_concrete_raw_to_polished",
        "tfg:shapeless/mars_stone_raw_to_polished",
        "tfg:shapeless/mercury_stone_raw_to_polished",
        "tfg:shapeless/moon_deepslate_raw_to_polished",
        "tfg:shapeless/moon_stone_raw_to_polished",
        "tfg:shapeless/palm_mosaic_slab",
        "tfg:shapeless/permafrost_hardened_to_polished",
        "tfg:shapeless/red_granite_hardened_to_polished",
        "tfg:shapeless/sandy_jadestone_hardened_to_polished",
        "tfg:shapeless/scorchia_loose_to_brick",
        "tfg:shapeless/scoria_raw_to_polished",
        "tfg:shapeless/stone_raw_to_polished",
        "tfg:shapeless/titanium_concrete_bricks_to_chiseled",
        "tfg:shapeless/tuff_loose_to_brick",
        "tfg:shapeless/venus_stone_bricks_to_chiseled",
        "tfg:shapeless/veridium_raw_to_polished"
      ],
      "recipe_output_examples": [
        "tfc:crafting/metal/chisel/bismuth_bronze"
      ],
      "recipe_examples_truncated": true,
      "model_parents": [
        "item/metal/chisel/bismuth_bronze",
        "item/handheld_flipped",
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
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+1.08 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-1.5 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fBismuth Bronze§7 (at Orange٭§7)"
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
        },
        "processing_in": {
          "values": [
            "tfc:damage_inputs_shaped_crafting",
            "tfc:damage_inputs_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfc:metal/chisel/black_bronze",
      "namespace": "tfc",
      "display_name": "Black Bronze Chisel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:breaks_decorated_pots",
        "minecraft:tools",
        "tfc:chisels",
        "tfc:metal_item/black_bronze",
        "tfc:metal_item/black_bronze_tools",
        "tfc:usable_on_tool_rack"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shaped_crafting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shaped_crafting": 65,
        "tfc:damage_inputs_shapeless_crafting": 301
      },
      "recipe_production_by_type": {
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_ingredient_count": 366,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "beneath:crafting/blackstone_brick",
        "create:shapeless/chisel_cut_diorite",
        "firmalife:crafting/brick_countertop",
        "firmalife:crafting/cleaning/rusted_iron_greenhouse_door",
        "firmalife:crafting/polished_sealed_bricks",
        "gtceu:shaped/mortar_bismuth_bronze",
        "gtceu:shaped/mortar_steel",
        "minecraft:shapeless/apatite_bud_indicator",
        "minecraft:shapeless/cinnabar_bud_indicator",
        "minecraft:shapeless/green_sapphire_bud_indicator",
        "minecraft:shapeless/lazurite_bud_indicator",
        "minecraft:shapeless/olivine_bud_indicator",
        "minecraft:shapeless/realgar_bud_indicator",
        "minecraft:shapeless/ruby_bud_indicator",
        "minecraft:shapeless/spessartine_bud_indicator",
        "rnr:crafting/flagstone/basalt",
        "rnr:crafting/flagstone/chalk",
        "rnr:crafting/flagstone/dacite",
        "rnr:crafting/flagstone/gneiss",
        "rnr:crafting/flagstone/marble",
        "rnr:crafting/flagstone/red_sandstone",
        "rnr:crafting/flagstone/slate",
        "rnr:crafting/shingle/ash",
        "rnr:crafting/shingle/birch",
        "rnr:crafting/shingle/douglas_fir",
        "rnr:crafting/shingle/hickory",
        "rnr:crafting/shingle/mahogany",
        "rnr:crafting/shingle/palm",
        "rnr:crafting/shingle/spruce",
        "rnr:crafting/shingle/tualang",
        "tfc:crafting/alabaster_brick/raw_gypsum",
        "tfc:crafting/rock/andesite_chiseled",
        "tfc:crafting/rock/basalt_chiseled",
        "tfc:crafting/rock/chalk_chiseled",
        "tfc:crafting/rock/chert_chiseled",
        "tfc:crafting/rock/claystone_brick_from_mossy",
        "tfc:crafting/rock/conglomerate_brick_from_mossy",
        "tfc:crafting/rock/dacite_brick_from_mossy",
        "tfc:crafting/rock/diorite_brick_from_mossy",
        "tfc:crafting/rock/dolomite_brick_from_mossy",
        "tfc:crafting/rock/gabbro_brick_from_mossy",
        "tfc:crafting/rock/gneiss_brick_from_mossy",
        "tfc:crafting/rock/granite_brick",
        "tfc:crafting/rock/limestone_brick",
        "tfc:crafting/rock/marble_brick",
        "tfc:crafting/rock/phyllite_brick",
        "tfc:crafting/rock/quartzite_brick",
        "tfc:crafting/rock/rhyolite_brick",
        "tfc:crafting/rock/rhyolite_smooth",
        "tfc:crafting/rock/schist_smooth",
        "tfc:crafting/rock/shale_smooth",
        "tfc:crafting/rock/slate_smooth",
        "tfc:crafting/sandstone/brown_smooth",
        "tfc:crafting/sandstone/pink_smooth",
        "tfc:crafting/sandstone/white_cut",
        "tfc:kjs/damage/kjs/tfg_etching_diamond_tip",
        "tfc:kjs/damage/shaped/basalt_support",
        "tfc:kjs/damage/shaped/chert_mossy_support",
        "tfc:kjs/damage/shaped/conglomerate_mossy_support",
        "tfc:kjs/damage/shaped/dacite_support",
        "tfc:kjs/damage/shaped/diorite_support",
        "tfc:kjs/damage/shaped/dripstone_support",
        "tfc:kjs/damage/shaped/glacio_stone_support",
        "tfc:kjs/damage/shaped/granite_support",
        "tfc:kjs/damage/shaped/marble_mossy_support",
        "tfc:kjs/damage/shaped/moon_deepslate_support",
        "tfc:kjs/damage/shaped/phyllite_support",
        "tfc:kjs/damage/shaped/red_granite_support",
        "tfc:kjs/damage/shaped/schist_mossy_support",
        "tfc:kjs/damage/shaped/shale_mossy_support",
        "tfc:kjs/damage/shaped/tuff_support",
        "tfg:shapeless/bamboo_mosaic_slab",
        "tfg:shapeless/blackstone_loose_to_brick",
        "tfg:shapeless/calcite_from_raw",
        "tfg:shapeless/crackrack_bricks_to_chiseled",
        "tfg:shapeless/crimsite_raw_to_polished",
        "tfg:shapeless/deepslate_hardened_to_polished",
        "tfg:shapeless/dripstone_loose_to_brick",
        "tfg:shapeless/flavolite_loose_to_brick",
        "tfg:shapeless/glacio_stone_loose_to_brick",
        "tfg:shapeless/light_concrete_raw_to_polished",
        "tfg:shapeless/mars_stone_raw_to_polished",
        "tfg:shapeless/mercury_stone_raw_to_polished",
        "tfg:shapeless/moon_deepslate_raw_to_polished",
        "tfg:shapeless/moon_stone_raw_to_polished",
        "tfg:shapeless/palm_mosaic_slab",
        "tfg:shapeless/permafrost_hardened_to_polished",
        "tfg:shapeless/red_granite_hardened_to_polished",
        "tfg:shapeless/sandy_jadestone_hardened_to_polished",
        "tfg:shapeless/scorchia_loose_to_brick",
        "tfg:shapeless/scoria_raw_to_polished",
        "tfg:shapeless/stone_raw_to_polished",
        "tfg:shapeless/titanium_concrete_bricks_to_chiseled",
        "tfg:shapeless/tuff_loose_to_brick",
        "tfg:shapeless/venus_stone_bricks_to_chiseled",
        "tfg:shapeless/veridium_raw_to_polished"
      ],
      "recipe_output_examples": [
        "tfc:crafting/metal/chisel/black_bronze"
      ],
      "recipe_examples_truncated": true,
      "model_parents": [
        "item/metal/chisel/black_bronze",
        "item/handheld_flipped",
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
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+1.15 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-1.5 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fBlack Bronze§7 (at Orange٭٭٭٭§7)"
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
        },
        "processing_in": {
          "values": [
            "tfc:damage_inputs_shaped_crafting",
            "tfc:damage_inputs_shapeless_crafting"
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