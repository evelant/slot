# Items to classify
{
  "items": [
    {
      "id": "gtceu:zpm_quantum_tank",
      "namespace": "gtceu",
      "display_name": "Quantum Tank VII",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:non_movable",
        "tfg:cannot_launch_in_railgun",
        "tfg:insulating_container"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_zpm_quantum_tank",
        "gtceu:shapeless/quantum_tank_nbt_7"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/quantum_tank_zpm",
        "gtceu:shapeless/quantum_tank_nbt_7"
      ],
      "model_parents": [
        "item/zpm_quantum_tank",
        "block/machine/zpm_quantum_tank",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:zpm_quantum_tank",
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
          "text": "§aSafely contains §6hot§a, §bcold§a, and §elighter-than-air§a items and fluids.§r"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Compact place to store all your fluids"
        },
        {
          "source": "runtime-tooltip",
          "text": "§9Fluid Capacity: §f256,000,000 mB"
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
      "id": "gtceu:zpm_robot_arm",
      "namespace": "gtceu",
      "display_name": "ZPM Robot Arm",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "gtceu:robot_arms"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 5,
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 6,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_zpm_robot_arm",
        "gtceu:shaped/zpm_assembler",
        "gtceu:shaped/zpm_circuit_assembler",
        "gtceu:shaped/zpm_food_oven",
        "gtceu:shaped/zpm_packer",
        "gtmutils:shaped/zpm_auto_charger_4x"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/zpm_robot_arm",
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
          "text": "§7Limits §fItems§7 to specific quantities as §fCover§7."
        },
        {
          "source": "runtime-tooltip",
          "text": "§bTransfer Rate: §f16 stacks/s"
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
      "id": "gtceu:zpm_rock_crusher",
      "namespace": "gtceu",
      "display_name": "§cElite Rock Crusher III§r",
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
        "greate:milling/integration/gtceu/macerator/macerate_zpm_rock_crusher"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/zpm_rock_crusher"
      ],
      "model_parents": [
        "item/zpm_rock_crusher",
        "block/machine/zpm_rock_crusher",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:zpm_rock_crusher",
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
          "text": "§7Cryogenic Magma Solidifier R-10200"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aVoltage IN: §f131,072 EU/t (§cZPM§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cEnergy Capacity: §r8,388,608 EU"
        },
        {
          "source": "runtime-tooltip",
          "text": "This Machine will not explode when exposed to the Elements"
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
      "id": "gtceu:zpm_rotor_holder",
      "namespace": "gtceu",
      "display_name": "§cZPM Rotor Holder",
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
        "greate:milling/integration/gtceu/macerator/macerate_zpm_rotor_holder"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/zpm_rotor_holder",
        "block/machine/zpm_rotor_holder",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:zpm_rotor_holder",
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
          "text": "Multiblock Sharing §4Disabled"
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
      "id": "gtceu:zpm_scanner",
      "namespace": "gtceu",
      "display_name": "§cElite Scanner III§r",
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
        "greate:milling/integration/gtceu/macerator/macerate_zpm_scanner"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/zpm_scanner"
      ],
      "model_parents": [
        "item/zpm_scanner",
        "block/machine/zpm_scanner",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:zpm_scanner",
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
          "text": "§7Anomaly Detector"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aVoltage IN: §f131,072 EU/t (§cZPM§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cEnergy Capacity: §r8,388,608 EU"
        },
        {
          "source": "runtime-tooltip",
          "text": "§9Fluid Capacity: §f64,000 mB"
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
      "id": "gtceu:zpm_sensor",
      "namespace": "gtceu",
      "display_name": "ZPM Sensor",
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
        "crafting_shaped": 2,
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 3,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_zpm_sensor",
        "gtceu:shaped/parallel_hatch_mk3",
        "gtceu:shaped/zpm_scanner"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/zpm_sensor",
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
      "id": "gtceu:zpm_sifter",
      "namespace": "gtceu",
      "display_name": "§cElite Sifter III§r",
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
        "greate:milling/integration/gtceu/macerator/macerate_zpm_sifter"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/zpm_sifter"
      ],
      "model_parents": [
        "item/zpm_sifter",
        "block/machine/zpm_sifter",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:zpm_sifter",
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
          "text": "§aVoltage IN: §f131,072 EU/t (§cZPM§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cEnergy Capacity: §r8,388,608 EU"
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
      "id": "gtceu:zpm_solar_panel",
      "namespace": "gtceu",
      "display_name": "Zero Point Module Solar Panel",
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
        "item/zpm_solar_panel",
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
          "text": "§aVoltage OUT: §f131072 EU/t (§cZPM§f)"
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
      "id": "gtceu:zpm_substation_input_hatch_64a",
      "namespace": "gtceu",
      "display_name": "§cZPM 64A Substation Energy Hatch",
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
        "greate:milling/integration/gtceu/macerator/macerate_zpm_substation_input_hatch_64_a"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/zpm_substation_input_hatch_64a",
        "block/machine/zpm_substation_input_hatch_64a",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:zpm_substation_input_hatch_64a",
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
          "text": "§aVoltage IN: §f131,072 EU/t (§cZPM§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eAmperage IN: §f64A"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cEnergy Capacity: §r536,870,912 EU"
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
      "id": "gtceu:zpm_substation_output_hatch_64a",
      "namespace": "gtceu",
      "display_name": "§cZPM 64A Substation Dynamo Hatch",
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
        "greate:milling/integration/gtceu/macerator/macerate_zpm_substation_output_hatch_64_a"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/zpm_substation_output_hatch_64a",
        "block/machine/zpm_substation_output_hatch_64a",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:zpm_substation_output_hatch_64a",
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
          "text": "§aVoltage OUT: §f131,072 EU/t (§cZPM§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eAmperage OUT: §f64A"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cEnergy Capacity: §r536,870,912 EU"
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
      "id": "gtceu:zpm_thermal_centrifuge",
      "namespace": "gtceu",
      "display_name": "§cElite Thermal Centrifuge III§r",
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
        "greate:milling/integration/gtceu/macerator/macerate_zpm_thermal_centrifuge"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/zpm_thermal_centrifuge"
      ],
      "model_parents": [
        "item/zpm_thermal_centrifuge",
        "block/machine/zpm_thermal_centrifuge",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:zpm_thermal_centrifuge",
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
          "text": "§7Blaze Sweatshop T-6352"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aVoltage IN: §f131,072 EU/t (§cZPM§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cEnergy Capacity: §r8,388,608 EU"
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
      "id": "gtceu:zpm_transformer_16a",
      "namespace": "gtceu",
      "display_name": "§cZPM Voltage§r Power Transformer",
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
        "greate:milling/integration/gtceu/macerator/macerate_zpm_transformer_16_a"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/zpm_transformer_16a",
        "block/machine/zpm_transformer_16a",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:zpm_transformer_16a",
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
          "text": "§aTransform Down: §f16A 524,288 EU (§3UV§f) -> 64A 131,072 EU (§cZPM§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTransform Up: §f64A 131,072 EU (§cZPM§f) -> 16A 524,288 EU (§3UV§f)"
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
      "id": "gtceu:zpm_transformer_1a",
      "namespace": "gtceu",
      "display_name": "§cZPM Voltage§r Transformer",
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
        "greate:milling/integration/gtceu/macerator/macerate_zpm_transformer_1_a"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/zpm_transformer_1a",
        "block/machine/zpm_transformer_1a",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:zpm_transformer_1a",
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
          "text": "§aTransform Down: §f1A 524,288 EU (§3UV§f) -> 4A 131,072 EU (§cZPM§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTransform Up: §f4A 131,072 EU (§cZPM§f) -> 1A 524,288 EU (§3UV§f)"
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
      "id": "gtceu:zpm_transformer_2a",
      "namespace": "gtceu",
      "display_name": "§cZPM Voltage§r Hi-Amp (2x) Transformer",
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
        "greate:milling/integration/gtceu/macerator/macerate_zpm_transformer_2_a"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/zpm_transformer_2a",
        "block/machine/zpm_transformer_2a",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:zpm_transformer_2a",
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
          "text": "§aTransform Down: §f2A 524,288 EU (§3UV§f) -> 8A 131,072 EU (§cZPM§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTransform Up: §f8A 131,072 EU (§cZPM§f) -> 2A 524,288 EU (§3UV§f)"
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
      "id": "gtceu:zpm_transformer_4a",
      "namespace": "gtceu",
      "display_name": "§cZPM Voltage§r Hi-Amp (4x) Transformer",
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
        "greate:milling/integration/gtceu/macerator/macerate_zpm_transformer_4_a"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/zpm_transformer_4a",
        "block/machine/zpm_transformer_4a",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:zpm_transformer_4a",
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
          "text": "§aTransform Down: §f4A 524,288 EU (§3UV§f) -> 16A 131,072 EU (§cZPM§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTransform Up: §f16A 131,072 EU (§cZPM§f) -> 4A 524,288 EU (§3UV§f)"
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
      "id": "gtceu:zpm_voltage_coil",
      "namespace": "gtceu",
      "display_name": "Zero Point Module Voltage Coil",
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
        "greate:milling/integration/gtceu/macerator/macerate_zpm_voltage_coil"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/zpm_voltage_coil",
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
          "text": "Super Coil"
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
          "key": "item.gtceu.zpm_voltage_coil.tooltip",
          "text": "Super Coil"
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
      "id": "gtceu:zpm_wiremill",
      "namespace": "gtceu",
      "display_name": "§cElite Wiremill III§r",
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
        "greate:milling/integration/gtceu/macerator/macerate_zpm_wiremill"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/zpm_wiremill"
      ],
      "model_parents": [
        "item/zpm_wiremill",
        "block/machine/zpm_wiremill",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:zpm_wiremill",
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
          "text": "§aVoltage IN: §f131,072 EU/t (§cZPM§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cEnergy Capacity: §r8,388,608 EU"
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
      "id": "gtceu:zpm_world_accelerator",
      "namespace": "gtceu",
      "display_name": "§cElite World Accelerator III§r",
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
        "item/zpm_world_accelerator",
        "block/machine/zpm_world_accelerator",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:zpm_world_accelerator",
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
          "text": "§aVoltage IN: §f131,072 EU/t (§cZPM§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cEnergy Capacity: §r8,388,608 EU"
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
          "text": "Random Tick Mode:§f 15x15"
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
      "id": "gtceuterminal:dismantler",
      "namespace": "gtceuterminal",
      "display_name": "Dismantler",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:shaped/dismantler"
      ],
      "model_parents": [
        "item/dismantler",
        "item/generated"
      ],
      "creative_tabs": [
        "gtceuterminal:gtceuterminal"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Disassemble Multiblocks and Return Components"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Multiblock Removal Tool"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Shift + Right-click on Multiblock: §dOpen Dismantler GUI"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GTCEu Terminals"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.dismantler.tooltip",
          "text": "Disassemble Multiblocks and Return Components"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.dismantler.tooltip.tool",
          "text": "Multiblock Removal Tool"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.dismantler.tooltip.open_dismantler_gui",
          "text": "Shift + Right-click on Multiblock: Open Dismantler GUI"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceuterminal",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "gtceuterminal:energy_analyzer",
      "namespace": "gtceuterminal",
      "display_name": "Energy Analyzer",
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
        "item/energy_analyzer",
        "item/generated"
      ],
      "creative_tabs": [
        "gtceuterminal:gtceuterminal"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§6Energy Monitoring Tool"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7No machines linked"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Right-click: §bOpen on that machine"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Right-click (air): §bOpen list"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Shift + Right-click: §eLink / Unlink machine"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GTCEu Terminals"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.energy_analyzer.tooltip.tool",
          "text": "Energy Monitoring Tool"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.energy_analyzer.tooltip.no_machines_linked",
          "text": "No machines linked"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.energy_analyzer.tooltip.linked_machines",
          "text": "Linked machines: %d/%d"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.energy_analyzer.tooltip.machine_entry",
          "text": "● %s"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.energy_analyzer.tooltip.more_machines",
          "text": "... and %d more"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.energy_analyzer.tooltip.right_click_open",
          "text": "Right-click: Open on that machine"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.energy_analyzer.tooltip.right_click_air_open",
          "text": "Right-click (air): Open list"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.energy_analyzer.tooltip.shift_right_click_manage",
          "text": "Shift + Right-click: Link / Unlink machine"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceuterminal",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "gtceuterminal:multi_structure_manager",
      "namespace": "gtceuterminal",
      "display_name": "Multi-Structure Manager",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:shaped/multi_structure_manager"
      ],
      "model_parents": [
        "item/multi_structure_manager",
        "item/generated"
      ],
      "creative_tabs": [
        "gtceuterminal:gtceuterminal"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Manage and Upgrade Nearby Multiblocks"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Multiblock Management Tool"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7✗ Not Linked"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8 Place in ME Wireless Access Point to link"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Right-click: §bSettings"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Shift + Right-click: §cManage Multiblocks"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GTCEu Terminals"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.multi_structure_manager.tooltip",
          "text": "Manage and Upgrade Nearby Multiblocks"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.multi_structure_manager.tooltip.tool",
          "text": "Multiblock Management Tool"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.multi_structure_manager.tooltip.linked",
          "text": "✓ Linked to ME Network"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.multi_structure_manager.tooltip.not_linked",
          "text": "✗ Not Linked"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.multi_structure_manager.tooltip.place_in_me_wireless_access_point",
          "text": "Place in ME Wireless Access Point to link"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.multi_structure_manager.tooltip.right_click_settings",
          "text": "Right-click: Settings"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.multi_structure_manager.tooltip.shift_right_click_manage",
          "text": "Shift + Right-click: Manage Multiblocks"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceuterminal",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "gtceuterminal:schematic_interface",
      "namespace": "gtceuterminal",
      "display_name": "Schematic Interface",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:shaped/schematic_interface"
      ],
      "model_parents": [
        "item/schematic_interface",
        "item/generated"
      ],
      "creative_tabs": [
        "gtceuterminal:gtceuterminal"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Copy and Paste Multiblock Schematics"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6Blueprint Tool"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7✗ Not Linked"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8 Place in ME Wireless Access Point to link"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Shift + Right-click: §bOpen Schematic GUI"
        },
        {
          "source": "runtime-tooltip",
          "text": "§8Right-click: §ePaste Schematic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GTCEu Terminals"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.schematic_interface.tooltip",
          "text": "Copy and Paste Multiblock Schematics"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.schematic_interface.tooltip.blueprint_tool",
          "text": "Blueprint Tool"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.schematic_interface.tooltip.linked",
          "text": "✓ Linked to ME Network"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.schematic_interface.tooltip.not_linked",
          "text": "✗ Not Linked"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.schematic_interface.tooltip.place_in_me_wireless_access_point",
          "text": "Place in ME Wireless Access Point to link"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.schematic_interface.tooltip.shift_right_click_prefix",
          "text": "Shift + Right-click:"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.schematic_interface.tooltip.open_schematic_gui",
          "text": "Open Schematic GUI"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.schematic_interface.tooltip.right_click_prefix",
          "text": "Right-click:"
        },
        {
          "source": "lang",
          "key": "item.gtceuterminal.schematic_interface.tooltip.paste_schematic",
          "text": "Paste Schematic"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceuterminal",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "gtmutils:ev_auto_charger_4x",
      "namespace": "gtmutils",
      "display_name": "§5Extreme Voltage§r 4x Auto Turbo Charger",
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
        "greate:milling/integration/gtceu/macerator/macerate_ev_auto_charger_4_x"
      ],
      "recipe_output_examples": [
        "gtmutils:shaped/ev_auto_charger_4x"
      ],
      "model_parents": [
        "item/ev_auto_charger_4x",
        "block/machine/ev_auto_charger_4x",
        "block/block"
      ],
      "creative_tabs": [
        "gtmutils:gtmutils"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtmutils:ev_auto_charger_4x",
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
          "text": "§6Item Slots: §f4"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aVoltage IN/OUT: §f2,048 EU/t (§5EV§f)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eAmperage IN up to: §f16A"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech Modern Utilities"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtmutils",
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
      "id": "gtmutils:expanded_me_pattern_buffer",
      "namespace": "gtmutils",
      "display_name": "Expanded ME Pattern Buffer",
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
        "item/expanded_me_pattern_buffer",
        "block/machine/expanded_me_pattern_buffer",
        "block/block"
      ],
      "creative_tabs": [
        "gtmutils:gtmutils"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtmutils:expanded_me_pattern_buffer",
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§fAllows expanded direct §6AE2 pattern storage §ffor GregTech Multiblocks."
        },
        {
          "source": "runtime-tooltip",
          "text": "§fAE2 Patterns can utilize anything stored in the §6shared inventory §fwidget."
        },
        {
          "source": "runtime-tooltip",
          "text": "§fLink §6Expanded Pattern Buffer Proxies §fwith a §bdatastick §fto link machines together!"
        },
        {
          "source": "runtime-tooltip",
          "text": "Multiblock Sharing §aEnabled"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech Modern Utilities"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtmutils",
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
      "id": "gtmutils:expanded_me_pattern_buffer_proxy",
      "namespace": "gtmutils",
      "display_name": "Expanded ME Pattern Buffer Proxy",
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
        "item/expanded_me_pattern_buffer_proxy",
        "block/machine/expanded_me_pattern_buffer_proxy",
        "block/block"
      ],
      "creative_tabs": [
        "gtmutils:gtmutils"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtmutils:expanded_me_pattern_buffer_proxy",
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§fAllows linking many machines to a singular §6Expanded ME Pattern Buffer§f."
        },
        {
          "source": "runtime-tooltip",
          "text": "§fLet the factory grow!"
        },
        {
          "source": "runtime-tooltip",
          "text": "Multiblock Sharing §aEnabled"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech Modern Utilities"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtmutils",
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