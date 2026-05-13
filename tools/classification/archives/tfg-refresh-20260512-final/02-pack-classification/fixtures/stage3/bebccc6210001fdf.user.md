# Items to classify
{
  "items": [
    {
      "id": "gtceu:iv_sensor",
      "namespace": "gtceu",
      "display_name": "IV Sensor",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "gtceu:sensors"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 4,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_iv_sensor",
        "gtceu:shaped/casing_assembly_control",
        "gtceu:shaped/iv_scanner",
        "gtceu:shaped/parallel_hatch_mk1",
        "tfg:shaped/schematic_interface"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/sensor_iv"
      ],
      "model_parents": [
        "item/iv_sensor",
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
      "id": "gtceu:iv_sifter",
      "namespace": "gtceu",
      "display_name": "§9Elite Sifter §r",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
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
        "greate:milling/integration/gtceu/macerator/macerate_iv_sifter",
        "gtceu:shaped/large_sifter"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/iv_sifter"
      ],
      "model_parents": [
        "item/iv_sifter",
        "block/machine/iv_sifter",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:iv_sifter",
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
          "text": "§7Sponsored by TFC"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aVoltage IN: §f8,192 EU/t (§9IV§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cEnergy Capacity: §r524,288 EU"
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
      "id": "gtceu:iv_solar_panel",
      "namespace": "gtceu",
      "display_name": "Insane Voltage Solar Panel",
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
        "item/iv_solar_panel",
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
          "text": "§7May the Sun be with you."
        },
        {
          "source": "runtime-tooltip",
          "text": "Produces §fEnergy§7 from the §eSun§7 as §fCover§7."
        },
        {
          "source": "runtime-tooltip",
          "text": "§aVoltage OUT: §f8192 EU/t (§9IV§f)"
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
      "id": "gtceu:iv_substation_input_hatch_64a",
      "namespace": "gtceu",
      "display_name": "§9IV 64A Substation Energy Hatch",
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
        "greate:milling/integration/gtceu/macerator/macerate_iv_substation_input_hatch_64_a"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/iv_substation_input_hatch_64a",
        "block/machine/iv_substation_input_hatch_64a",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:iv_substation_input_hatch_64a",
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
          "text": "§aVoltage IN: §f8,192 EU/t (§9IV§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eAmperage IN: §f64A"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cEnergy Capacity: §r33,554,432 EU"
        },
        {
          "source": "runtime-tooltip",
          "text": "Energy Input for the Power Substation"
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
      "id": "gtceu:iv_substation_output_hatch_64a",
      "namespace": "gtceu",
      "display_name": "§9IV 64A Substation Dynamo Hatch",
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
        "greate:milling/integration/gtceu/macerator/macerate_iv_substation_output_hatch_64_a"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/iv_substation_output_hatch_64a",
        "block/machine/iv_substation_output_hatch_64a",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:iv_substation_output_hatch_64a",
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
          "text": "§aVoltage OUT: §f8,192 EU/t (§9IV§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eAmperage OUT: §f64A"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cEnergy Capacity: §r33,554,432 EU"
        },
        {
          "source": "runtime-tooltip",
          "text": "Energy Output for the Power Substation"
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
      "id": "gtceu:iv_thermal_centrifuge",
      "namespace": "gtceu",
      "display_name": "§9Elite Thermal Centrifuge §r",
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
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_iv_thermal_centrifuge"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/iv_thermal_centrifuge"
      ],
      "model_parents": [
        "item/iv_thermal_centrifuge",
        "block/machine/iv_thermal_centrifuge",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:iv_thermal_centrifuge",
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
          "text": "§7Blaze Sweatshop T-6350"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aVoltage IN: §f8,192 EU/t (§9IV§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cEnergy Capacity: §r524,288 EU"
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
      "id": "gtceu:iv_transformer_16a",
      "namespace": "gtceu",
      "display_name": "§9Insane Voltage§r Power Transformer",
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
        "greate:milling/integration/gtceu/macerator/macerate_iv_transformer_16_a"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/iv_transformer_16a",
        "block/machine/iv_transformer_16a",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:iv_transformer_16a",
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
          "text": "§7Transforms Energy between voltage tiers"
        },
        {
          "source": "runtime-tooltip",
          "text": "Starts as §fTransform Down§7, use Screwdriver to change"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aTransform Down: §f16A 32,768 EU (§dLuV§f) -> 64A 8,192 EU (§9IV§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTransform Up: §f64A 8,192 EU (§9IV§f) -> 16A 32,768 EU (§dLuV§f)"
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
      "id": "gtceu:iv_transformer_1a",
      "namespace": "gtceu",
      "display_name": "§9Insane Voltage§r Transformer",
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
        "greate:milling/integration/gtceu/macerator/macerate_iv_transformer_1_a"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/iv_transformer_1a",
        "block/machine/iv_transformer_1a",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:iv_transformer_1a",
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
          "text": "§7Transforms Energy between voltage tiers"
        },
        {
          "source": "runtime-tooltip",
          "text": "Starts as §fTransform Down§7, use Screwdriver to change"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aTransform Down: §f1A 32,768 EU (§dLuV§f) -> 4A 8,192 EU (§9IV§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTransform Up: §f4A 8,192 EU (§9IV§f) -> 1A 32,768 EU (§dLuV§f)"
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
      "id": "gtceu:iv_transformer_2a",
      "namespace": "gtceu",
      "display_name": "§9Insane Voltage§r Hi-Amp (2x) Transformer",
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
        "greate:milling/integration/gtceu/macerator/macerate_iv_transformer_2_a"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/iv_transformer_2a",
        "block/machine/iv_transformer_2a",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:iv_transformer_2a",
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
          "text": "§7Transforms Energy between voltage tiers"
        },
        {
          "source": "runtime-tooltip",
          "text": "Starts as §fTransform Down§7, use Screwdriver to change"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aTransform Down: §f2A 32,768 EU (§dLuV§f) -> 8A 8,192 EU (§9IV§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTransform Up: §f8A 8,192 EU (§9IV§f) -> 2A 32,768 EU (§dLuV§f)"
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
      "id": "gtceu:iv_transformer_4a",
      "namespace": "gtceu",
      "display_name": "§9Insane Voltage§r Hi-Amp (4x) Transformer",
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
        "greate:milling/integration/gtceu/macerator/macerate_iv_transformer_4_a"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/iv_transformer_4a",
        "block/machine/iv_transformer_4a",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:iv_transformer_4a",
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
          "text": "§7Transforms Energy between voltage tiers"
        },
        {
          "source": "runtime-tooltip",
          "text": "Starts as §fTransform Down§7, use Screwdriver to change"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aTransform Down: §f4A 32,768 EU (§dLuV§f) -> 16A 8,192 EU (§9IV§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTransform Up: §f16A 8,192 EU (§9IV§f) -> 4A 32,768 EU (§dLuV§f)"
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
      "id": "gtceu:iv_vanadium_battery",
      "namespace": "gtceu",
      "display_name": "Medium Vanadium Battery",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "gtceu:batteries",
        "gtceu:batteries/iv",
        "tfclunchbox:electric_batteries"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 3
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 3,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:shaped/iv_power_unit_iv_vanadium_battery",
        "tfg:shaped/schematic_interface",
        "tfg:sophisticated_backpacks/shaped/stack_upgrade_tier_1"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/iv_vanadium_battery",
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
          "text": "§7Reusable Battery"
        },
        {
          "source": "runtime-tooltip",
          "text": "0/40,960,000 EU§7 - Tier §9IV §7(0/250 seconds remaining§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "Use while sneaking to toggle discharge mode"
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
          "key": "item.gtceu.iv_vanadium_battery.tooltip",
          "text": "Reusable Battery"
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
      "id": "gtceu:iv_voltage_coil",
      "namespace": "gtceu",
      "display_name": "Insane Voltage Coil",
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
        "greate:milling/integration/gtceu/macerator/macerate_iv_voltage_coil"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/iv_voltage_coil",
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
          "text": "Elite Coil"
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
          "key": "item.gtceu.iv_voltage_coil.tooltip",
          "text": "Elite Coil"
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
      "id": "gtceu:iv_wiremill",
      "namespace": "gtceu",
      "display_name": "§9Elite Wiremill §r",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
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
        "greate:milling/integration/gtceu/macerator/macerate_iv_wiremill",
        "gtceu:shaped/large_wiremill"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/iv_wiremill"
      ],
      "model_parents": [
        "item/iv_wiremill",
        "block/machine/iv_wiremill",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:iv_wiremill",
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
          "text": "§7Ingot Elongator"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aVoltage IN: §f8,192 EU/t (§9IV§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cEnergy Capacity: §r524,288 EU"
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
      "id": "gtceu:iv_world_accelerator",
      "namespace": "gtceu",
      "display_name": "§9Elite World Accelerator §r",
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
        "item/iv_world_accelerator",
        "block/machine/iv_world_accelerator",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:iv_world_accelerator",
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Tick accelerates nearby blocks in one of 2 modes: §fTile Entity§7 or §fRandom Tick§7. Use Screwdriver to change mode."
        },
        {
          "source": "runtime-tooltip",
          "text": "§aVoltage IN: §f8,192 EU/t (§9IV§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cEnergy Capacity: §r524,288 EU"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bWorking Area:"
        },
        {
          "source": "runtime-tooltip",
          "text": "Block Entity Mode:§f Adjacent Blocks"
        },
        {
          "source": "runtime-tooltip",
          "text": "Random Tick Mode:§f 11x11"
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
      "id": "gtceu:kanthal_block",
      "namespace": "gtceu",
      "display_name": "Block of Kanthal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:storage_blocks",
        "forge:storage_blocks/kanthal",
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
        "greate:cutting/integration/gtceu/cutter/cut_kanthal_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_kanthal_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_kanthal_block_to_plate_water",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/kanthal_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:kanthal_block",
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
          "forge:storage_blocks/kanthal",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "FeAlCr"
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
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:kanthal_bucket",
      "namespace": "gtceu",
      "display_name": "Liquid Kanthal Bucket",
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
          "text": "FeAlCr"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 1,708 K"
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
      "id": "gtceu:kanthal_coil_block",
      "namespace": "gtceu",
      "display_name": "Kanthal Coil Block",
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
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_coil_block"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/kanthal_coil_block",
        "block/kanthal_coil_block",
        "block/cube_all"
      ],
      "creative_tabs": [
        "gtceu:decoration"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "gtceu:blocks/kanthal_coil_block"
      ],
      "block_context": {
        "block_id": "gtceu:kanthal_coil_block",
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
          "text": "§7Hold SHIFT to show Coil Bonus Info"
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
      "id": "gtceu:kanthal_double_cable",
      "namespace": "gtceu",
      "display_name": "2x Kanthal Cable",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:double_cables",
        "forge:double_cables/kanthal"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "create:filling": 3
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_double_cable"
      ],
      "recipe_output_examples": [
        "greate:filling/kanthal_cable_2_rubber",
        "greate:filling/kanthal_cable_2_slicone",
        "greate:filling/kanthal_cable_2_styrene_butadiene"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:kanthal_double_cable",
        "block_tags": [
          "forge:double_cables",
          "forge:double_cables/kanthal",
          "forge:mineable/wire_cutter",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "FeAlCr"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a512 §a(§6HV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e8"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c3§7 EU-Volt"
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
        }
      }
    },
    {
      "id": "gtceu:kanthal_double_wire",
      "namespace": "gtceu",
      "display_name": "2x Kanthal Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:double_wires",
        "forge:double_wires/kanthal"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:filling",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "crafting_shapeless": 3,
        "create:filling": 3,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 2
      },
      "recipe_ingredient_count": 9,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:filling/kanthal_cable_2_rubber",
        "greate:filling/kanthal_cable_2_slicone",
        "greate:filling/kanthal_cable_2_styrene_butadiene",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_double_wire",
        "gtceu:shaped/electric_motor_ev",
        "gtceu:shaped/hv_electric_furnace",
        "gtceu:shapeless/kanthal_wire_wire_gt_double_doubling",
        "gtceu:shapeless/kanthal_wire_wire_gt_double_quadrupling",
        "gtceu:shapeless/kanthal_wire_wire_gt_double_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/kanthal_wire_wire_gt_quadruple_splitting",
        "gtceu:shapeless/kanthal_wire_wire_gt_single_doubling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:kanthal_double_wire",
        "block_tags": [
          "forge:double_wires",
          "forge:double_wires/kanthal",
          "forge:mineable/wire_cutter",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "FeAlCr"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a512 §a(§6HV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e8"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c6§7 EU-Volt"
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
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
        },
        "processing_in": {
          "values": [
            "crafting",
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
      "id": "gtceu:kanthal_dust",
      "namespace": "gtceu",
      "display_name": "Kanthal Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/kanthal"
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
        "greate:milling": 18,
        "greate:mixing": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 21,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_kanthal",
        "gtceu:shaped/small_dust_disassembling_kanthal",
        "gtceu:shaped/tiny_dust_disassembling_3x3_kanthal",
        "gtceu:shaped/tiny_dust_disassembling_kanthal"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_double_kanthal_plate",
        "greate:milling/integration/gtceu/macerator/macerate_ev_electric_motor",
        "greate:milling/integration/gtceu/macerator/macerate_ev_robot_arm",
        "greate:milling/integration/gtceu/macerator/macerate_ev_wiremill",
        "greate:milling/integration/gtceu/macerator/macerate_hv_alloy_smelter",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_block",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_coil_block",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_double_wire",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_hex_cable",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_hex_wire",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_ingot",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_octal_cable",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_octal_wire",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_plate",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_quadruple_cable",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_quadruple_wire",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_spring",
        "greate:milling/integration/gtceu/macerator/macerate_long_kanthal_rod",
        "greate:mixing/integration/gtceu/mixer/kanthal",
        "gtceu:shaped/small_dust_assembling_kanthal",
        "gtceu:shaped/tiny_dust_assembling_kanthal"
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
          "text": "FeAlCr"
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
      "id": "gtceu:kanthal_hex_cable",
      "namespace": "gtceu",
      "display_name": "16x Kanthal Cable",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:hex_cables",
        "forge:hex_cables/kanthal"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "create:filling": 3
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_hex_cable"
      ],
      "recipe_output_examples": [
        "greate:filling/kanthal_cable_16_rubber",
        "greate:filling/kanthal_cable_16_slicone",
        "greate:filling/kanthal_cable_16_styrene_butadiene"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:kanthal_hex_cable",
        "block_tags": [
          "forge:hex_cables",
          "forge:hex_cables/kanthal",
          "forge:mineable/wire_cutter",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "FeAlCr"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a512 §a(§6HV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e64"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c3§7 EU-Volt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
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
        }
      }
    },
    {
      "id": "gtceu:kanthal_hex_wire",
      "namespace": "gtceu",
      "display_name": "16x Kanthal Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:hex_wires",
        "forge:hex_wires/kanthal"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:filling",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "create:filling": 3,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 2
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:filling/kanthal_cable_16_rubber",
        "greate:filling/kanthal_cable_16_slicone",
        "greate:filling/kanthal_cable_16_styrene_butadiene",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_hex_wire",
        "gtceu:shapeless/kanthal_wire_wire_gt_hex_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/kanthal_wire_wire_gt_octal_doubling",
        "gtceu:shapeless/kanthal_wire_wire_gt_quadruple_quadrupling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:kanthal_hex_wire",
        "block_tags": [
          "forge:hex_wires",
          "forge:hex_wires/kanthal",
          "forge:mineable/wire_cutter",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "FeAlCr"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a512 §a(§6HV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e64"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c9§7 EU-Volt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
        },
        "processing_in": {
          "values": [
            "crafting",
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
      "id": "gtceu:kanthal_ingot",
      "namespace": "gtceu",
      "display_name": "Kanthal Ingot",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ingots",
        "forge:ingots",
        "forge:ingots/kanthal",
        "tfc:pileable_ingots"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "createaddition:rolling",
        "greate:compacting",
        "greate:milling",
        "vintageimprovements:coiling",
        "vintageimprovements:polishing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "createaddition:rolling": 1,
        "greate:compacting": 1,
        "greate:milling": 1,
        "vintageimprovements:coiling": 1,
        "vintageimprovements:polishing": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 6,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:compacting/kanthal_block",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_ingot",
        "gtceu:shaped/stick_kanthal",
        "tfg:rolling/kanthal_plate",
        "tfg:vi/coiling/kanthal_single_wire",
        "tfg:vi/lathe/kanthal_to_rod"
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
          "text": "FeAlCr"
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
            "createaddition:rolling",
            "greate:compacting",
            "greate:milling",
            "vintageimprovements:coiling",
            "vintageimprovements:polishing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:kanthal_nugget",
      "namespace": "gtceu",
      "display_name": "Kanthal Nugget",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:nuggets",
        "forge:nuggets",
        "forge:nuggets/kanthal"
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
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_nugget"
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
          "text": "FeAlCr"
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
      "id": "gtceu:kanthal_octal_cable",
      "namespace": "gtceu",
      "display_name": "8x Kanthal Cable",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:octal_cables",
        "forge:octal_cables/kanthal"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "create:filling": 3
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_octal_cable"
      ],
      "recipe_output_examples": [
        "greate:filling/kanthal_cable_8_rubber",
        "greate:filling/kanthal_cable_8_slicone",
        "greate:filling/kanthal_cable_8_styrene_butadiene"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:kanthal_octal_cable",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:octal_cables",
          "forge:octal_cables/kanthal",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "FeAlCr"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a512 §a(§6HV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e32"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c3§7 EU-Volt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
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