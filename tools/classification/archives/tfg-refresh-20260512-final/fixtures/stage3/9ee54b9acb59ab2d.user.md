# Items to classify
{
  "items": [
    {
      "id": "tfc:ceramic/fire_ingot_mold",
      "namespace": "tfc",
      "display_name": "Fire Ingot Mold",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:fired_molds",
        "tfc:molds",
        "tfcchannelcasting:accepted_in_mold_table"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "create:filling",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "create:filling": 37,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "create:filling": 37,
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 38,
      "recipe_output_count": 39,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_ceramic/fire_ingot_mold",
        "tfg:filling/aluminium_silicate_ingot_fire",
        "tfg:filling/bismuth_bronze_ingot_fire",
        "tfg:filling/bismuth_ingot_fire",
        "tfg:filling/black_bronze_ingot_fire",
        "tfg:filling/black_steel_ingot_fire",
        "tfg:filling/blue_steel_ingot_fire",
        "tfg:filling/brass_ingot_fire",
        "tfg:filling/bronze_ingot_fire",
        "tfg:filling/cobalt_brass_ingot_fire",
        "tfg:filling/cobalt_ingot_fire",
        "tfg:filling/copper_ingot_fire",
        "tfg:filling/gold_ingot_fire",
        "tfg:filling/invar_ingot_fire",
        "tfg:filling/iron_ingot_fire",
        "tfg:filling/lead_ingot_fire",
        "tfg:filling/nickel_ingot_fire",
        "tfg:filling/potin_ingot_fire",
        "tfg:filling/red_alloy_ingot_fire",
        "tfg:filling/red_steel_ingot_fire",
        "tfg:filling/rose_gold_ingot_fire",
        "tfg:filling/silver_ingot_fire",
        "tfg:filling/steel_ingot_fire",
        "tfg:filling/sterling_silver_ingot_fire",
        "tfg:filling/tin_alloy_ingot_fire",
        "tfg:filling/tin_ingot_fire",
        "tfg:filling/weak_blue_steel_ingot_fire",
        "tfg:filling/weak_red_steel_ingot_fire",
        "tfg:filling/zinc_ingot_fire",
        "tfg:tfc/filling/high_carbon_black_steel_fire_ingot",
        "tfg:tfc/filling/high_carbon_blue_steel_fire_ingot",
        "tfg:tfc/filling/high_carbon_red_steel_fire_ingot",
        "tfg:tfc/filling/high_carbon_steel_fire_ingot",
        "tfg:tfc/filling/pig_iron_fire_ingot",
        "tfg:tfc/filling/unknown_fire_ingot",
        "tfg:tfc/filling/weak_blue_steel_fire_ingot",
        "tfg:tfc/filling/weak_red_steel_fire_ingot",
        "tfg:tfc/filling/weak_steel_fire_ingot"
      ],
      "recipe_output_examples": [
        "tfc:heating/fire_ingot_mold",
        "tfg:filling/aluminium_silicate_ingot_fire",
        "tfg:filling/bismuth_bronze_ingot_fire",
        "tfg:filling/bismuth_ingot_fire",
        "tfg:filling/black_bronze_ingot_fire",
        "tfg:filling/black_steel_ingot_fire",
        "tfg:filling/blue_steel_ingot_fire",
        "tfg:filling/brass_ingot_fire",
        "tfg:filling/bronze_ingot_fire",
        "tfg:filling/cobalt_brass_ingot_fire",
        "tfg:filling/cobalt_ingot_fire",
        "tfg:filling/copper_ingot_fire",
        "tfg:filling/gold_ingot_fire",
        "tfg:filling/invar_ingot_fire",
        "tfg:filling/iron_ingot_fire",
        "tfg:filling/lead_ingot_fire",
        "tfg:filling/nickel_ingot_fire",
        "tfg:filling/potin_ingot_fire",
        "tfg:filling/red_alloy_ingot_fire",
        "tfg:filling/red_steel_ingot_fire",
        "tfg:filling/rose_gold_ingot_fire",
        "tfg:filling/silver_ingot_fire",
        "tfg:filling/steel_ingot_fire",
        "tfg:filling/sterling_silver_ingot_fire",
        "tfg:filling/tin_alloy_ingot_fire",
        "tfg:filling/tin_ingot_fire",
        "tfg:filling/weak_blue_steel_ingot_fire",
        "tfg:filling/weak_red_steel_ingot_fire",
        "tfg:filling/zinc_ingot_fire",
        "tfg:smelting/fire_ingot_mold",
        "tfg:tfc/filling/high_carbon_black_steel_fire_ingot",
        "tfg:tfc/filling/high_carbon_blue_steel_fire_ingot",
        "tfg:tfc/filling/high_carbon_red_steel_fire_ingot",
        "tfg:tfc/filling/high_carbon_steel_fire_ingot",
        "tfg:tfc/filling/pig_iron_fire_ingot",
        "tfg:tfc/filling/unknown_fire_ingot",
        "tfg:tfc/filling/weak_blue_steel_fire_ingot",
        "tfg:tfc/filling/weak_red_steel_fire_ingot",
        "tfg:tfc/filling/weak_steel_fire_ingot"
      ],
      "model_parents": [
        "item/ceramic/fire_ingot_mold",
        "item/default"
      ],
      "creative_tabs": [
        "tfc:misc"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
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
            "create:filling",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfc:ceramic/gray_glazed_vessel",
      "namespace": "tfc",
      "display_name": "Gray Glazed Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "sns:prevented_in_burlap_sack",
        "sns:prevented_in_frame_pack",
        "sns:prevented_in_item_containers",
        "sns:prevented_in_leather_sack",
        "sns:prevented_in_lunchbox",
        "sns:prevented_in_ore_sack",
        "sns:prevented_in_quiver",
        "sns:prevented_in_seed_pouch",
        "sns:prevented_in_straw_basket",
        "tfc:fired_vessels",
        "tfc:vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_fired_vessels"
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
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "firmalife:crafting/pottery_sherd",
        "tfg:shaped/smoke_bomb_vessel"
      ],
      "recipe_output_examples": [
        "tfc:heating/glazed_ceramic_vessel_gray",
        "tfg:smelting/gray_glazed_vessel"
      ],
      "model_parents": [
        "item/ceramic/gray_glazed_vessel",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:misc"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§9Fluid Capacity: §f3,024 mB"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cHeating an overfilled vessel will void any overflow and may ruin your alloying!§r"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "tfc:ceramic/gray_unfired_vessel",
      "namespace": "tfc",
      "display_name": "Gray Unfired Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:unfired_pottery",
        "tfc:unfired_vessels",
        "tfc:vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_unfired_vessels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "smelting": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "tfc:barrel_sealed": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "tfg:smelting/gray_glazed_vessel"
      ],
      "recipe_output_examples": [
        "tfc:barrel/dye/gray_glazed_vessel",
        "tfc:crafting/ceramic/gray_unfired_vessel"
      ],
      "model_parents": [
        "item/ceramic/gray_unfired_vessel",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:misc"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 4,
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
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfc:ceramic/green_glazed_vessel",
      "namespace": "tfc",
      "display_name": "Green Glazed Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "sns:prevented_in_burlap_sack",
        "sns:prevented_in_frame_pack",
        "sns:prevented_in_item_containers",
        "sns:prevented_in_leather_sack",
        "sns:prevented_in_lunchbox",
        "sns:prevented_in_ore_sack",
        "sns:prevented_in_quiver",
        "sns:prevented_in_seed_pouch",
        "sns:prevented_in_straw_basket",
        "tfc:fired_vessels",
        "tfc:vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_fired_vessels"
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
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "firmalife:crafting/pottery_sherd",
        "tfg:shaped/smoke_bomb_vessel"
      ],
      "recipe_output_examples": [
        "tfc:heating/glazed_ceramic_vessel_green",
        "tfg:smelting/green_glazed_vessel"
      ],
      "model_parents": [
        "item/ceramic/green_glazed_vessel",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:misc"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§9Fluid Capacity: §f3,024 mB"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cHeating an overfilled vessel will void any overflow and may ruin your alloying!§r"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "tfc:ceramic/green_unfired_vessel",
      "namespace": "tfc",
      "display_name": "Green Unfired Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:unfired_pottery",
        "tfc:unfired_vessels",
        "tfc:vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_unfired_vessels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "smelting": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "tfc:barrel_sealed": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "tfg:smelting/green_glazed_vessel"
      ],
      "recipe_output_examples": [
        "tfc:barrel/dye/green_glazed_vessel",
        "tfc:crafting/ceramic/green_unfired_vessel"
      ],
      "model_parents": [
        "item/ceramic/green_unfired_vessel",
        "item/generated"
      ],
      "creative_tabs": [
        "tfc:misc"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 4,
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
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfc:ceramic/hammer_head_mold",
      "namespace": "tfc",
      "display_name": "Hammer Head Mold",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:fired_molds",
        "tfc:molds",
        "tfcchannelcasting:accepted_in_mold_table"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "create:filling",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "create:filling": 4,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "create:filling": 4,
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 6,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/ceramic_molds",
        "tfg:filling/bismuth_bronze_hammer_head_ceramic",
        "tfg:filling/black_bronze_hammer_head_ceramic",
        "tfg:filling/bronze_hammer_head_ceramic",
        "tfg:filling/copper_hammer_head_ceramic"
      ],
      "recipe_output_examples": [
        "tfc:heating/hammer_head_mold",
        "tfg:filling/bismuth_bronze_hammer_head_ceramic",
        "tfg:filling/black_bronze_hammer_head_ceramic",
        "tfg:filling/bronze_hammer_head_ceramic",
        "tfg:filling/copper_hammer_head_ceramic",
        "tfg:smelting/hammer_head_mold"
      ],
      "model_parents": [
        "item/ceramic/hammer_head_mold",
        "item/default"
      ],
      "creative_tabs": [
        "tfc:misc"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
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
            "create:filling",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfc:ceramic/hoe_head_mold",
      "namespace": "tfc",
      "display_name": "Hoe Head Mold",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:fired_molds",
        "tfc:molds",
        "tfcchannelcasting:accepted_in_mold_table"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "create:filling",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "create:filling": 4,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "create:filling": 4,
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 6,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/ceramic_molds",
        "tfg:filling/bismuth_bronze_hoe_head_ceramic",
        "tfg:filling/black_bronze_hoe_head_ceramic",
        "tfg:filling/bronze_hoe_head_ceramic",
        "tfg:filling/copper_hoe_head_ceramic"
      ],
      "recipe_output_examples": [
        "tfc:heating/hoe_head_mold",
        "tfg:filling/bismuth_bronze_hoe_head_ceramic",
        "tfg:filling/black_bronze_hoe_head_ceramic",
        "tfg:filling/bronze_hoe_head_ceramic",
        "tfg:filling/copper_hoe_head_ceramic",
        "tfg:smelting/hoe_head_mold"
      ],
      "model_parents": [
        "item/ceramic/hoe_head_mold",
        "item/default"
      ],
      "creative_tabs": [
        "tfc:misc"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
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
            "create:filling",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfc:ceramic/ingot_mold",
      "namespace": "tfc",
      "display_name": "Ingot Mold",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:fired_molds",
        "tfc:molds",
        "tfcchannelcasting:accepted_in_mold_table"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "create:filling",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "create:filling": 37,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "create:filling": 37,
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 38,
      "recipe_output_count": 39,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/ceramic_molds",
        "tfg:filling/aluminium_silicate_ingot_ceramic",
        "tfg:filling/bismuth_bronze_ingot_ceramic",
        "tfg:filling/bismuth_ingot_ceramic",
        "tfg:filling/black_bronze_ingot_ceramic",
        "tfg:filling/black_steel_ingot_ceramic",
        "tfg:filling/blue_steel_ingot_ceramic",
        "tfg:filling/brass_ingot_ceramic",
        "tfg:filling/bronze_ingot_ceramic",
        "tfg:filling/cobalt_brass_ingot_ceramic",
        "tfg:filling/cobalt_ingot_ceramic",
        "tfg:filling/copper_ingot_ceramic",
        "tfg:filling/gold_ingot_ceramic",
        "tfg:filling/invar_ingot_ceramic",
        "tfg:filling/iron_ingot_ceramic",
        "tfg:filling/lead_ingot_ceramic",
        "tfg:filling/nickel_ingot_ceramic",
        "tfg:filling/potin_ingot_ceramic",
        "tfg:filling/red_alloy_ingot_ceramic",
        "tfg:filling/red_steel_ingot_ceramic",
        "tfg:filling/rose_gold_ingot_ceramic",
        "tfg:filling/silver_ingot_ceramic",
        "tfg:filling/steel_ingot_ceramic",
        "tfg:filling/sterling_silver_ingot_ceramic",
        "tfg:filling/tin_alloy_ingot_ceramic",
        "tfg:filling/tin_ingot_ceramic",
        "tfg:filling/weak_blue_steel_ingot_ceramic",
        "tfg:filling/weak_red_steel_ingot_ceramic",
        "tfg:filling/zinc_ingot_ceramic",
        "tfg:tfc/filling/high_carbon_black_steel_ingot",
        "tfg:tfc/filling/high_carbon_blue_steel_ingot",
        "tfg:tfc/filling/high_carbon_red_steel_ingot",
        "tfg:tfc/filling/high_carbon_steel_ingot",
        "tfg:tfc/filling/pig_iron_ingot",
        "tfg:tfc/filling/unknown_ingot",
        "tfg:tfc/filling/weak_blue_steel_ingot",
        "tfg:tfc/filling/weak_red_steel_ingot",
        "tfg:tfc/filling/weak_steel_ingot"
      ],
      "recipe_output_examples": [
        "tfc:heating/ingot_mold",
        "tfg:filling/aluminium_silicate_ingot_ceramic",
        "tfg:filling/bismuth_bronze_ingot_ceramic",
        "tfg:filling/bismuth_ingot_ceramic",
        "tfg:filling/black_bronze_ingot_ceramic",
        "tfg:filling/black_steel_ingot_ceramic",
        "tfg:filling/blue_steel_ingot_ceramic",
        "tfg:filling/brass_ingot_ceramic",
        "tfg:filling/bronze_ingot_ceramic",
        "tfg:filling/cobalt_brass_ingot_ceramic",
        "tfg:filling/cobalt_ingot_ceramic",
        "tfg:filling/copper_ingot_ceramic",
        "tfg:filling/gold_ingot_ceramic",
        "tfg:filling/invar_ingot_ceramic",
        "tfg:filling/iron_ingot_ceramic",
        "tfg:filling/lead_ingot_ceramic",
        "tfg:filling/nickel_ingot_ceramic",
        "tfg:filling/potin_ingot_ceramic",
        "tfg:filling/red_alloy_ingot_ceramic",
        "tfg:filling/red_steel_ingot_ceramic",
        "tfg:filling/rose_gold_ingot_ceramic",
        "tfg:filling/silver_ingot_ceramic",
        "tfg:filling/steel_ingot_ceramic",
        "tfg:filling/sterling_silver_ingot_ceramic",
        "tfg:filling/tin_alloy_ingot_ceramic",
        "tfg:filling/tin_ingot_ceramic",
        "tfg:filling/weak_blue_steel_ingot_ceramic",
        "tfg:filling/weak_red_steel_ingot_ceramic",
        "tfg:filling/zinc_ingot_ceramic",
        "tfg:smelting/ingot_mold",
        "tfg:tfc/filling/high_carbon_black_steel_ingot",
        "tfg:tfc/filling/high_carbon_blue_steel_ingot",
        "tfg:tfc/filling/high_carbon_red_steel_ingot",
        "tfg:tfc/filling/high_carbon_steel_ingot",
        "tfg:tfc/filling/pig_iron_ingot",
        "tfg:tfc/filling/unknown_ingot",
        "tfg:tfc/filling/weak_blue_steel_ingot",
        "tfg:tfc/filling/weak_red_steel_ingot",
        "tfg:tfc/filling/weak_steel_ingot"
      ],
      "model_parents": [
        "item/ceramic/ingot_mold",
        "item/default"
      ],
      "creative_tabs": [
        "tfc:misc"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
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
            "create:filling",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfc:ceramic/javelin_head_mold",
      "namespace": "tfc",
      "display_name": "Javelin Head Mold",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:fired_molds",
        "tfc:molds",
        "tfcchannelcasting:accepted_in_mold_table"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "create:filling",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "create:filling": 4,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "create:filling": 4,
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 6,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/ceramic_molds",
        "tfg:filling/bismuth_bronze_javelin_head_ceramic",
        "tfg:filling/black_bronze_javelin_head_ceramic",
        "tfg:filling/bronze_javelin_head_ceramic",
        "tfg:filling/copper_javelin_head_ceramic"
      ],
      "recipe_output_examples": [
        "tfc:heating/javelin_head_mold",
        "tfg:filling/bismuth_bronze_javelin_head_ceramic",
        "tfg:filling/black_bronze_javelin_head_ceramic",
        "tfg:filling/bronze_javelin_head_ceramic",
        "tfg:filling/copper_javelin_head_ceramic",
        "tfg:smelting/javelin_head_mold"
      ],
      "model_parents": [
        "item/ceramic/javelin_head_mold",
        "item/default"
      ],
      "creative_tabs": [
        "tfc:misc"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
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
            "create:filling",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfc:ceramic/jug",
      "namespace": "tfc",
      "display_name": "Ceramic Jug",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:fluid_item_ingredient_empty_containers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "kubejs_tfc:advanced_shapeless_crafting",
        "tfc:advanced_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 34,
        "kubejs_tfc:advanced_shapeless_crafting": 7,
        "tfc:advanced_shapeless_crafting": 15
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 56,
      "recipe_output_count": 2,
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
        "tfc:crafting/soil/silt_mud_3",
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
        "tfg:shapeless/barley_flatbread_dough_mixing",
        "tfg:shapeless/hardtack_dough",
        "tfg:shapeless/hardtack_dough_mixing",
        "tfg:shapeless/maize_flatbread_dough",
        "tfg:shapeless/maize_flatbread_dough_mixing",
        "tfg:shapeless/oat_flatbread_dough",
        "tfg:shapeless/oat_flatbread_dough_mixing",
        "tfg:shapeless/rice_flatbread_dough",
        "tfg:shapeless/rice_flatbread_dough_mixing",
        "tfg:shapeless/rye_flatbread_dough",
        "tfg:shapeless/rye_flatbread_dough_mixing",
        "tfg:shapeless/wheat_flatbread_dough",
        "tfg:shapeless/wheat_flatbread_dough_mixing"
      ],
      "recipe_output_examples": [
        "tfc:heating/ceramic_jug",
        "tfg:smelting/jug"
      ],
      "model_parents": [
        "item/ceramic/jug",
        "item/default"
      ],
      "creative_tabs": [
        "tfc:misc"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§9Fluid Capacity: §f100 mB"
        },
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
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "processing_in": {
          "values": [
            "crafting",
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
      "id": "tfc:ceramic/knife_blade_mold",
      "namespace": "tfc",
      "display_name": "Knife Blade Mold",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:fired_molds",
        "tfc:molds",
        "tfcchannelcasting:accepted_in_mold_table"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "create:filling",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "create:filling": 4,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "create:filling": 4,
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 6,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/ceramic_molds",
        "tfg:filling/bismuth_bronze_knife_head_ceramic",
        "tfg:filling/black_bronze_knife_head_ceramic",
        "tfg:filling/bronze_knife_head_ceramic",
        "tfg:filling/copper_knife_head_ceramic"
      ],
      "recipe_output_examples": [
        "tfc:heating/knife_blade_mold",
        "tfg:filling/bismuth_bronze_knife_head_ceramic",
        "tfg:filling/black_bronze_knife_head_ceramic",
        "tfg:filling/bronze_knife_head_ceramic",
        "tfg:filling/copper_knife_head_ceramic",
        "tfg:smelting/knife_blade_mold"
      ],
      "model_parents": [
        "item/ceramic/knife_blade_mold",
        "item/default"
      ],
      "creative_tabs": [
        "tfc:misc"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
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
            "create:filling",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfc:ceramic/large_vessel",
      "namespace": "tfc",
      "display_name": "Large Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "alekiships:can_place_in_compartments",
        "curios:back",
        "tfc:fired_large_vessels",
        "tfc:large_vessels",
        "tfg:cannot_launch_in_railgun"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/large_vessels"
      ],
      "recipe_output_examples": [
        "tfc:heating/fired_large_vessel",
        "tfg:smelting/large_vessel"
      ],
      "model_parents": [
        "item/ceramic/large_vessel",
        "block/ceramic/large_vessel_opened",
        "block/large_vessel_opened",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:decorations",
        "tfc:misc"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/ceramic/large_vessel"
      ],
      "block_context": {
        "block_id": "tfc:ceramic/large_vessel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:fired_large_vessels",
          "tfc:minecart_holdable",
          "tfc:pet_sits_on"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "tfc:ceramic/large_vessel/black",
      "namespace": "tfc",
      "display_name": "Black Large Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "alekiships:can_place_in_compartments",
        "curios:back",
        "tfc:fired_large_vessels",
        "tfc:large_vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_fired_large_vessels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/large_vessels"
      ],
      "recipe_output_examples": [
        "tfc:heating/glazed_large_vessel_black",
        "tfg:smelting/black_large_vessel"
      ],
      "model_parents": [
        "item/ceramic/large_vessel/black",
        "block/ceramic/black_large_vessel_opened",
        "block/ceramic/large_vessel_c_opened",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:decorations",
        "tfc:misc"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/ceramic/large_vessel/black"
      ],
      "block_context": {
        "block_id": "tfc:ceramic/large_vessel/black",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:fired_large_vessels",
          "tfc:large_vessels",
          "tfc:minecart_holdable",
          "tfc:pet_sits_on"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "tfc:ceramic/large_vessel/blue",
      "namespace": "tfc",
      "display_name": "Blue Large Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "alekiships:can_place_in_compartments",
        "curios:back",
        "tfc:fired_large_vessels",
        "tfc:large_vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_fired_large_vessels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/large_vessels"
      ],
      "recipe_output_examples": [
        "tfc:heating/glazed_large_vessel_blue",
        "tfg:smelting/blue_large_vessel"
      ],
      "model_parents": [
        "item/ceramic/large_vessel/blue",
        "block/ceramic/blue_large_vessel_opened",
        "block/ceramic/large_vessel_a_opened",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:decorations",
        "tfc:misc"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/ceramic/large_vessel/blue"
      ],
      "block_context": {
        "block_id": "tfc:ceramic/large_vessel/blue",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:fired_large_vessels",
          "tfc:large_vessels",
          "tfc:minecart_holdable",
          "tfc:pet_sits_on"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "tfc:ceramic/large_vessel/brown",
      "namespace": "tfc",
      "display_name": "Brown Large Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "alekiships:can_place_in_compartments",
        "curios:back",
        "tfc:fired_large_vessels",
        "tfc:large_vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_fired_large_vessels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/large_vessels"
      ],
      "recipe_output_examples": [
        "tfc:heating/glazed_large_vessel_brown",
        "tfg:smelting/brown_large_vessel"
      ],
      "model_parents": [
        "item/ceramic/large_vessel/brown",
        "block/ceramic/brown_large_vessel_opened",
        "block/ceramic/large_vessel_a_opened",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:decorations",
        "tfc:misc"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/ceramic/large_vessel/brown"
      ],
      "block_context": {
        "block_id": "tfc:ceramic/large_vessel/brown",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:fired_large_vessels",
          "tfc:large_vessels",
          "tfc:minecart_holdable",
          "tfc:pet_sits_on"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "tfc:ceramic/large_vessel/cyan",
      "namespace": "tfc",
      "display_name": "Cyan Large Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "alekiships:can_place_in_compartments",
        "curios:back",
        "tfc:fired_large_vessels",
        "tfc:large_vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_fired_large_vessels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/large_vessels"
      ],
      "recipe_output_examples": [
        "tfc:heating/glazed_large_vessel_cyan",
        "tfg:smelting/cyan_large_vessel"
      ],
      "model_parents": [
        "item/ceramic/large_vessel/cyan",
        "block/ceramic/cyan_large_vessel_opened",
        "block/ceramic/large_vessel_b_opened",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:decorations",
        "tfc:misc"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/ceramic/large_vessel/cyan"
      ],
      "block_context": {
        "block_id": "tfc:ceramic/large_vessel/cyan",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:fired_large_vessels",
          "tfc:large_vessels",
          "tfc:minecart_holdable",
          "tfc:pet_sits_on"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "tfc:ceramic/large_vessel/gray",
      "namespace": "tfc",
      "display_name": "Gray Large Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "alekiships:can_place_in_compartments",
        "curios:back",
        "tfc:fired_large_vessels",
        "tfc:large_vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_fired_large_vessels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/large_vessels"
      ],
      "recipe_output_examples": [
        "tfc:heating/glazed_large_vessel_gray",
        "tfg:smelting/gray_large_vessel"
      ],
      "model_parents": [
        "item/ceramic/large_vessel/gray",
        "block/ceramic/gray_large_vessel_opened",
        "block/ceramic/large_vessel_a_opened",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:decorations",
        "tfc:misc"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/ceramic/large_vessel/gray"
      ],
      "block_context": {
        "block_id": "tfc:ceramic/large_vessel/gray",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:fired_large_vessels",
          "tfc:large_vessels",
          "tfc:minecart_holdable",
          "tfc:pet_sits_on"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "tfc:ceramic/large_vessel/green",
      "namespace": "tfc",
      "display_name": "Green Large Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "alekiships:can_place_in_compartments",
        "curios:back",
        "tfc:fired_large_vessels",
        "tfc:large_vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_fired_large_vessels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/large_vessels"
      ],
      "recipe_output_examples": [
        "tfc:heating/glazed_large_vessel_green",
        "tfg:smelting/green_large_vessel"
      ],
      "model_parents": [
        "item/ceramic/large_vessel/green",
        "block/ceramic/green_large_vessel_opened",
        "block/ceramic/large_vessel_d_opened",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:decorations",
        "tfc:misc"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/ceramic/large_vessel/green"
      ],
      "block_context": {
        "block_id": "tfc:ceramic/large_vessel/green",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:fired_large_vessels",
          "tfc:large_vessels",
          "tfc:minecart_holdable",
          "tfc:pet_sits_on"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "tfc:ceramic/large_vessel/light_blue",
      "namespace": "tfc",
      "display_name": "Light Blue Large Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "alekiships:can_place_in_compartments",
        "curios:back",
        "tfc:fired_large_vessels",
        "tfc:large_vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_fired_large_vessels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/large_vessels"
      ],
      "recipe_output_examples": [
        "tfc:heating/glazed_large_vessel_light_blue",
        "tfg:smelting/light_blue_large_vessel"
      ],
      "model_parents": [
        "item/ceramic/large_vessel/light_blue",
        "block/ceramic/light_blue_large_vessel_opened",
        "block/ceramic/large_vessel_c_opened",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:decorations",
        "tfc:misc"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/ceramic/large_vessel/light_blue"
      ],
      "block_context": {
        "block_id": "tfc:ceramic/large_vessel/light_blue",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:fired_large_vessels",
          "tfc:large_vessels",
          "tfc:minecart_holdable",
          "tfc:pet_sits_on"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "tfc:ceramic/large_vessel/light_gray",
      "namespace": "tfc",
      "display_name": "Light Gray Large Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "alekiships:can_place_in_compartments",
        "curios:back",
        "tfc:fired_large_vessels",
        "tfc:large_vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_fired_large_vessels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/large_vessels"
      ],
      "recipe_output_examples": [
        "tfc:heating/glazed_large_vessel_light_gray",
        "tfg:smelting/light_gray_large_vessel"
      ],
      "model_parents": [
        "item/ceramic/large_vessel/light_gray",
        "block/ceramic/light_gray_large_vessel_opened",
        "block/ceramic/large_vessel_a_opened",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:decorations",
        "tfc:misc"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/ceramic/large_vessel/light_gray"
      ],
      "block_context": {
        "block_id": "tfc:ceramic/large_vessel/light_gray",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:fired_large_vessels",
          "tfc:large_vessels",
          "tfc:minecart_holdable",
          "tfc:pet_sits_on"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "tfc:ceramic/large_vessel/lime",
      "namespace": "tfc",
      "display_name": "Lime Large Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "alekiships:can_place_in_compartments",
        "curios:back",
        "tfc:fired_large_vessels",
        "tfc:large_vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_fired_large_vessels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/large_vessels"
      ],
      "recipe_output_examples": [
        "tfc:heating/glazed_large_vessel_lime",
        "tfg:smelting/lime_large_vessel"
      ],
      "model_parents": [
        "item/ceramic/large_vessel/lime",
        "block/ceramic/lime_large_vessel_opened",
        "block/ceramic/large_vessel_c_opened",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:decorations",
        "tfc:misc"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/ceramic/large_vessel/lime"
      ],
      "block_context": {
        "block_id": "tfc:ceramic/large_vessel/lime",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:fired_large_vessels",
          "tfc:large_vessels",
          "tfc:minecart_holdable",
          "tfc:pet_sits_on"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "tfc:ceramic/large_vessel/magenta",
      "namespace": "tfc",
      "display_name": "Magenta Large Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "alekiships:can_place_in_compartments",
        "curios:back",
        "tfc:fired_large_vessels",
        "tfc:large_vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_fired_large_vessels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/large_vessels"
      ],
      "recipe_output_examples": [
        "tfc:heating/glazed_large_vessel_magenta",
        "tfg:smelting/magenta_large_vessel"
      ],
      "model_parents": [
        "item/ceramic/large_vessel/magenta",
        "block/ceramic/magenta_large_vessel_opened",
        "block/ceramic/large_vessel_a_opened",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:decorations",
        "tfc:misc"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/ceramic/large_vessel/magenta"
      ],
      "block_context": {
        "block_id": "tfc:ceramic/large_vessel/magenta",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:fired_large_vessels",
          "tfc:large_vessels",
          "tfc:minecart_holdable",
          "tfc:pet_sits_on"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "tfc:ceramic/large_vessel/orange",
      "namespace": "tfc",
      "display_name": "Orange Large Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "alekiships:can_place_in_compartments",
        "curios:back",
        "tfc:fired_large_vessels",
        "tfc:large_vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_fired_large_vessels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/large_vessels"
      ],
      "recipe_output_examples": [
        "tfc:heating/glazed_large_vessel_orange",
        "tfg:smelting/orange_large_vessel"
      ],
      "model_parents": [
        "item/ceramic/large_vessel/orange",
        "block/ceramic/orange_large_vessel_opened",
        "block/ceramic/large_vessel_a_opened",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:decorations",
        "tfc:misc"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/ceramic/large_vessel/orange"
      ],
      "block_context": {
        "block_id": "tfc:ceramic/large_vessel/orange",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:fired_large_vessels",
          "tfc:large_vessels",
          "tfc:minecart_holdable",
          "tfc:pet_sits_on"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "tfc:ceramic/large_vessel/pink",
      "namespace": "tfc",
      "display_name": "Pink Large Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "alekiships:can_place_in_compartments",
        "curios:back",
        "tfc:fired_large_vessels",
        "tfc:large_vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_fired_large_vessels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/large_vessels"
      ],
      "recipe_output_examples": [
        "tfc:heating/glazed_large_vessel_pink",
        "tfg:smelting/pink_large_vessel"
      ],
      "model_parents": [
        "item/ceramic/large_vessel/pink",
        "block/ceramic/pink_large_vessel_opened",
        "block/ceramic/large_vessel_b_opened",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:decorations",
        "tfc:misc"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/ceramic/large_vessel/pink"
      ],
      "block_context": {
        "block_id": "tfc:ceramic/large_vessel/pink",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:fired_large_vessels",
          "tfc:large_vessels",
          "tfc:minecart_holdable",
          "tfc:pet_sits_on"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "tfc:ceramic/large_vessel/purple",
      "namespace": "tfc",
      "display_name": "Purple Large Vessel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "alekiships:can_place_in_compartments",
        "curios:back",
        "tfc:fired_large_vessels",
        "tfc:large_vessels",
        "tfg:cannot_launch_in_railgun",
        "tfg:colorized_fired_large_vessels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/large_vessels"
      ],
      "recipe_output_examples": [
        "tfc:heating/glazed_large_vessel_purple",
        "tfg:smelting/purple_large_vessel"
      ],
      "model_parents": [
        "item/ceramic/large_vessel/purple",
        "block/ceramic/purple_large_vessel_opened",
        "block/ceramic/large_vessel_b_opened",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:decorations",
        "tfc:misc"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/ceramic/large_vessel/purple"
      ],
      "block_context": {
        "block_id": "tfc:ceramic/large_vessel/purple",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:fired_large_vessels",
          "tfc:large_vessels",
          "tfc:minecart_holdable",
          "tfc:pet_sits_on"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
          "confidence": 1,
          "source": "rule:mod_namespace"
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