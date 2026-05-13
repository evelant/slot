# Items to classify
{
  "items": [
    {
      "id": "gtceu:kanthal_octal_wire",
      "namespace": "gtceu",
      "display_name": "8x Kanthal Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:octal_wires",
        "forge:octal_wires/kanthal"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:filling",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 2,
        "create:filling": 3,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 3
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:filling/kanthal_cable_8_rubber",
        "greate:filling/kanthal_cable_8_slicone",
        "greate:filling/kanthal_cable_8_styrene_butadiene",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_octal_wire",
        "gtceu:shapeless/kanthal_wire_wire_gt_octal_doubling",
        "gtceu:shapeless/kanthal_wire_wire_gt_octal_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/kanthal_wire_wire_gt_double_quadrupling",
        "gtceu:shapeless/kanthal_wire_wire_gt_hex_splitting",
        "gtceu:shapeless/kanthal_wire_wire_gt_quadruple_doubling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:kanthal_octal_wire",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:octal_wires",
          "forge:octal_wires/kanthal",
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
          "text": "§cLoss/Meter/Ampere:§r §c9§7 EU-Volt"
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
      "id": "gtceu:kanthal_plate",
      "namespace": "gtceu",
      "display_name": "Kanthal Plate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:plates",
        "forge:plates/kanthal",
        "forge:sheets/kanthal",
        "tfc:pileable_sheets"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:compacting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "greate:compacting": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "createaddition:rolling": 1,
        "greate:cutting": 3
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_plate",
        "gtceu:shaped/kanthal_wire_single",
        "tfg:compacting/kanthal_doublePlate"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_kanthal_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_kanthal_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_kanthal_block_to_plate_water",
        "tfg:rolling/kanthal_plate"
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
            "crafting",
            "greate:compacting",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:kanthal_quadruple_cable",
      "namespace": "gtceu",
      "display_name": "4x Kanthal Cable",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:quadruple_cables",
        "forge:quadruple_cables/kanthal"
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
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_quadruple_cable"
      ],
      "recipe_output_examples": [
        "greate:filling/kanthal_cable_4_rubber",
        "greate:filling/kanthal_cable_4_slicone",
        "greate:filling/kanthal_cable_4_styrene_butadiene"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:kanthal_quadruple_cable",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:quadruple_cables",
          "forge:quadruple_cables/kanthal",
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
          "text": "§eMax Amperage:§r §e16"
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
      "id": "gtceu:kanthal_quadruple_wire",
      "namespace": "gtceu",
      "display_name": "4x Kanthal Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:quadruple_wires",
        "forge:quadruple_wires/kanthal"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:filling",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 5,
        "crafting_shapeless": 3,
        "create:filling": 3,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 3
      },
      "recipe_ingredient_count": 12,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:filling/kanthal_cable_4_rubber",
        "greate:filling/kanthal_cable_4_slicone",
        "greate:filling/kanthal_cable_4_styrene_butadiene",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_quadruple_wire",
        "gtceu:shaped/hv_alloy_smelter",
        "gtceu:shaped/hv_extruder",
        "gtceu:shaped/hv_fluid_heater",
        "gtceu:shaped/hv_food_oven",
        "gtceu:shaped/hv_thermal_centrifuge",
        "gtceu:shapeless/kanthal_wire_wire_gt_quadruple_doubling",
        "gtceu:shapeless/kanthal_wire_wire_gt_quadruple_quadrupling",
        "gtceu:shapeless/kanthal_wire_wire_gt_quadruple_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/kanthal_wire_wire_gt_double_doubling",
        "gtceu:shapeless/kanthal_wire_wire_gt_octal_splitting",
        "gtceu:shapeless/kanthal_wire_wire_gt_single_quadrupling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:kanthal_quadruple_wire",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:quadruple_wires",
          "forge:quadruple_wires/kanthal",
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
          "text": "§eMax Amperage:§r §e16"
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
      "id": "gtceu:kanthal_rod",
      "namespace": "gtceu",
      "display_name": "Kanthal Rod",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:rods",
        "forge:rods/kanthal"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:compacting",
        "greate:milling",
        "tfc:advanced_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 4,
        "greate:compacting": 1,
        "greate:milling": 1,
        "tfc:advanced_shapeless_crafting": 208
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "greate:cutting": 3,
        "vintageimprovements:polishing": 1
      },
      "recipe_ingredient_count": 214,
      "recipe_output_count": 6,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_rod",
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
        "greate:cutting/integration/gtceu/cutter/cut_kanthal_long_rod_to_rod",
        "greate:cutting/integration/gtceu/cutter/cut_kanthal_long_rod_to_rod_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_kanthal_long_rod_to_rod_water",
        "gtceu:shaped/stick_kanthal",
        "gtceu:shaped/stick_long_kanthal",
        "tfg:vi/lathe/kanthal_to_rod"
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
          "text": "FeAlCr"
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
            "greate:compacting",
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
      "id": "gtceu:kanthal_single_cable",
      "namespace": "gtceu",
      "display_name": "1x Kanthal Cable",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:single_cables",
        "forge:single_cables/kanthal"
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
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_single_cable"
      ],
      "recipe_output_examples": [
        "greate:filling/kanthal_cable_1_rubber",
        "greate:filling/kanthal_cable_1_slicone",
        "greate:filling/kanthal_cable_1_styrene_butadiene"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:kanthal_single_cable",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:single_cables",
          "forge:single_cables/kanthal",
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
          "text": "§eMax Amperage:§r §e4"
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
          "text": "⚖ Very Light ⇲ Tiny"
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
      "id": "gtceu:kanthal_single_wire",
      "namespace": "gtceu",
      "display_name": "1x Kanthal Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:single_wires",
        "forge:single_wires/kanthal"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:filling",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 2,
        "create:filling": 3,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "vintageimprovements:coiling": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:filling/kanthal_cable_1_rubber",
        "greate:filling/kanthal_cable_1_slicone",
        "greate:filling/kanthal_cable_1_styrene_butadiene",
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_single_wire",
        "gtceu:shapeless/kanthal_wire_wire_gt_single_doubling",
        "gtceu:shapeless/kanthal_wire_wire_gt_single_quadrupling"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/kanthal_wire_single",
        "gtceu:shapeless/kanthal_wire_wire_gt_double_splitting",
        "tfg:vi/coiling/kanthal_single_wire"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:kanthal_single_wire",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:single_wires",
          "forge:single_wires/kanthal",
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
          "text": "§eMax Amperage:§r §e4"
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
          "text": "⚖ Very Light ⇲ Tiny"
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
      "id": "gtceu:kanthal_spring",
      "namespace": "gtceu",
      "display_name": "Kanthal Spring",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:springs",
        "forge:springs/kanthal"
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
        "vintageimprovements:coiling": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_kanthal_spring",
        "gtceu:shaped/hv_brewery",
        "gtceu:shaped/hv_distillery",
        "tfg:shaped/flintlock_mechanism_iron",
        "tfg:shaped/flintlock_mechanism_steel"
      ],
      "recipe_output_examples": [
        "tfg:vi/coiling/kanthal_spring"
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
      "id": "gtceu:krypton_bucket",
      "namespace": "gtceu",
      "display_name": "Krypton Bucket",
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
          "text": "Kr"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Gaseous"
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
      "id": "gtceu:kyanite_dust",
      "namespace": "gtceu",
      "display_name": "Kyanite Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/kyanite"
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
        "blasting": 42,
        "crafting_shaped": 2,
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "greate:splashing": 4,
        "smelting": 43,
        "tfc:barrel_instant": 2,
        "vintageimprovements:centrifugation": 2
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 101,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_kyanite",
        "gtceu:shaped/small_dust_disassembling_kyanite",
        "gtceu:shaped/tiny_dust_disassembling_3x3_kyanite",
        "gtceu:shaped/tiny_dust_disassembling_kyanite"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerate_kyanite_refined_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_kyanite_refined_ore_to_dust",
        "greate:pressing/refined_kyanite_to_dust",
        "gtceu:blasting/smelt_andesite_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_basalt_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_black_sand_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_brown_sand_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_chalk_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_chert_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_claystone_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_dacite_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_deepslate_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_diorite_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_dolomite_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_dripstone_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_flavolite_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_gabbro_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_glacio_stone_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_gneiss_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_granite_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_green_sand_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_limestone_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_marble_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_mars_stone_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_mercury_stone_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_moon_deepslate_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_moon_stone_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_phyllite_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_pyroxenite_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_quartzite_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_raw_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_red_granite_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_red_sand_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_rhyolite_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_sandy_jadestone_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_schist_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_shale_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_slate_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_tuff_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_venus_stone_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_white_sand_kyanite_ore_to_ingot",
        "gtceu:blasting/smelt_yellow_sand_kyanite_ore_to_ingot",
        "gtceu:shaped/small_dust_assembling_kyanite",
        "gtceu:shaped/tiny_dust_assembling_kyanite",
        "gtceu:shapeless/centrifuged_ore_to_dust_kyanite",
        "gtceu:smelting/smelt_andesite_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_basalt_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_brown_sand_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_chalk_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_chert_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_claystone_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_conglomerate_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_dacite_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_deepslate_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_diorite_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_dolomite_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_dripstone_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_flavolite_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_gabbro_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_glacio_stone_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_gneiss_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_granite_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_limestone_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_marble_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_mercury_stone_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_moon_deepslate_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_moon_stone_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_phyllite_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_pink_sand_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_pyroxenite_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_quartzite_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_raw_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_red_granite_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_red_sand_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_rhyolite_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_rich_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_sandy_jadestone_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_schist_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_shale_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_slate_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_tuff_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_venus_stone_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_white_sand_kyanite_ore_to_ingot",
        "tfg:ae_transform/kyanite_dust_from_impure",
        "tfg:ae_transform/kyanite_dust_from_pure",
        "tfg:instant_barrel/kyanite_dust_from_impure",
        "tfg:instant_barrel/kyanite_dust_from_pure",
        "tfg:splashing/kyanite_dust_from_impure_distilled",
        "tfg:splashing/kyanite_dust_from_impure_water",
        "tfg:splashing/kyanite_dust_from_pure_distilled",
        "tfg:splashing/kyanite_dust_from_pure_water",
        "tfg:vi/centrifuge/kyanite_dust_from_impure",
        "tfg:vi/centrifuge/kyanite_dust_from_pure"
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
          "text": "Al₂SiO₅"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 136 mB of §fAluminium Silicate§7 (at Brilliant White§7)"
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
      "id": "gtceu:kyanite_indicator",
      "namespace": "gtceu",
      "display_name": "Kyanite Surface Rock",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:surface_rocks",
        "forge:surface_rocks/kyanite"
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
        "block_id": "gtceu:kyanite_indicator",
        "block_tags": [
          "forge:surface_rocks",
          "forge:surface_rocks/kyanite",
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
        }
      }
    },
    {
      "id": "gtceu:kyanite_ore",
      "namespace": "gtceu",
      "display_name": "Kyanite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/kyanite",
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
        "greate:milling/integration/gtceu/macerator/macerate_kyanite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_kyanite_ore_to_ingot",
        "gtceu:smelting/smelt_kyanite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:kyanite_ore",
        "requires_correct_tool": true
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
          "rationale": "ore id kyanite_ore"
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
      "id": "gtceu:lactose_dust",
      "namespace": "gtceu",
      "display_name": "Lactose Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/lactose"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:mixing",
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "greate:mixing": 18,
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "greate:mixing": 12
      },
      "recipe_ingredient_count": 22,
      "recipe_output_count": 14,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/tablet_antipoison",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/tablet_haste",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/tablet_night_vision",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/tablet_poison",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/tablet_regeneration",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/tablet_slowness",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/tablet_speed",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/tablet_water_breathing",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/tablet_weakness",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/tablet_antipoison",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/tablet_haste",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/tablet_night_vision",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/tablet_poison",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/tablet_regeneration",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/tablet_slowness",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/tablet_speed",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/tablet_water_breathing",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/tablet_weakness",
        "gtceu:shaped/small_dust_disassembling_3x3_lactose",
        "gtceu:shaped/small_dust_disassembling_lactose",
        "gtceu:shaped/tiny_dust_disassembling_3x3_lactose",
        "gtceu:shaped/tiny_dust_disassembling_lactose"
      ],
      "recipe_output_examples": [
        "greate:mixing/integration/gtceu/lactose_milk_alpaca",
        "greate:mixing/integration/gtceu/lactose_milk_cow",
        "greate:mixing/integration/gtceu/lactose_milk_goat",
        "greate:mixing/integration/gtceu/lactose_milk_ox",
        "greate:mixing/integration/gtceu/lactose_milk_sheep",
        "greate:mixing/integration/gtceu/lactose_milk_vinegar_alpaca",
        "greate:mixing/integration/gtceu/lactose_milk_vinegar_cow",
        "greate:mixing/integration/gtceu/lactose_milk_vinegar_goat",
        "greate:mixing/integration/gtceu/lactose_milk_vinegar_ox",
        "greate:mixing/integration/gtceu/lactose_milk_vinegar_sheep",
        "greate:mixing/integration/gtceu/lactose_milk_vinegar_yak",
        "greate:mixing/integration/gtceu/lactose_milk_yak",
        "gtceu:shaped/small_dust_assembling_lactose",
        "gtceu:shaped/tiny_dust_assembling_lactose"
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
          "text": "C₁₂H₂₂O₁₁"
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
      "id": "gtceu:laminated_glass",
      "namespace": "gtceu",
      "display_name": "Laminated Glass",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "kubejs:shaped",
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 35,
        "kubejs:shaped": 2,
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 40,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "gtceu:shaped/blacklight",
        "gtceu:shaped/iv_aqueous_accumulator",
        "gtceu:shaped/iv_autoclave",
        "gtceu:shaped/iv_brewery",
        "gtceu:shaped/iv_canner",
        "gtceu:shaped/iv_chemical_bath",
        "gtceu:shaped/iv_cutter",
        "gtceu:shaped/iv_distillery",
        "gtceu:shaped/iv_electrolyzer",
        "gtceu:shaped/iv_extractor",
        "gtceu:shaped/iv_fermenter",
        "gtceu:shaped/iv_fluid_heater",
        "gtceu:shaped/iv_fluid_solidifier",
        "gtceu:shaped/iv_food_processor",
        "gtceu:shaped/iv_gas_pressurizer",
        "gtceu:shaped/iv_mixer",
        "gtceu:shaped/iv_ore_washer",
        "gtceu:shaped/iv_rock_crusher",
        "gtceu:shaped/luv_aqueous_accumulator",
        "gtceu:shaped/luv_autoclave",
        "gtceu:shaped/luv_brewery",
        "gtceu:shaped/luv_canner",
        "gtceu:shaped/luv_chemical_bath",
        "gtceu:shaped/luv_cutter",
        "gtceu:shaped/luv_distillery",
        "gtceu:shaped/luv_electrolyzer",
        "gtceu:shaped/luv_extractor",
        "gtceu:shaped/luv_fermenter",
        "gtceu:shaped/luv_fluid_heater",
        "gtceu:shaped/luv_fluid_solidifier",
        "gtceu:shaped/luv_food_processor",
        "gtceu:shaped/luv_gas_pressurizer",
        "gtceu:shaped/luv_mixer",
        "gtceu:shaped/luv_ore_washer",
        "gtceu:shaped/luv_rock_crusher",
        "gtceu:shaped/passthrough_hatch_fluid_iv",
        "gtceu:shaped/passthrough_hatch_fluid_luv"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/laminated_glass",
        "block/laminated_glass",
        "block/cube_all"
      ],
      "creative_tabs": [
        "gtceu:decoration"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "gtceu:blocks/laminated_glass"
      ],
      "block_context": {
        "block_id": "gtceu:laminated_glass",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
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
        "form": {
          "value": "pane",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _glass"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "kubejs:shaped",
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:lanthanum_bucket",
      "namespace": "gtceu",
      "display_name": "Liquid Lanthanum Bucket",
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
          "text": "La"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 1,193 K"
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
      "id": "gtceu:lanthanum_dust",
      "namespace": "gtceu",
      "display_name": "Lanthanum Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/lanthanum"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_lanthanum",
        "gtceu:shaped/small_dust_disassembling_lanthanum",
        "gtceu:shaped/tiny_dust_disassembling_3x3_lanthanum",
        "gtceu:shaped/tiny_dust_disassembling_lanthanum"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_lanthanum",
        "gtceu:shaped/tiny_dust_assembling_lanthanum"
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
          "text": "La"
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
      "id": "gtceu:lapis_bud_indicator",
      "namespace": "gtceu",
      "display_name": "Lapis Surface Bud",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:bud_indicators",
        "forge:bud_indicators/lapis"
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
        "minecraft:shapeless/lapis_bud_indicator"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:lapis_bud_indicator",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "forge:bud_indicators",
          "forge:bud_indicators/lapis",
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
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        }
      }
    },
    {
      "id": "gtceu:lapis_dust",
      "namespace": "gtceu",
      "display_name": "Lapis Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/lapis"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "greate:mixing",
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "greate:mixing": 1,
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
      "recipe_ingredient_count": 6,
      "recipe_output_count": 22,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/lapis_to_powder",
        "greate:mixing/integration/gtceu/mixer/lapotron_dust",
        "gtceu:shaped/small_dust_disassembling_3x3_lapis",
        "gtceu:shaped/small_dust_disassembling_lapis",
        "gtceu:shaped/tiny_dust_disassembling_3x3_lapis",
        "gtceu:shaped/tiny_dust_disassembling_lapis"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerate_lapis_refined_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_exquisite_lapis_gem",
        "greate:milling/integration/gtceu/macerator/macerate_flawless_lapis_gem",
        "greate:milling/integration/gtceu/macerator/macerate_lapis_block",
        "greate:milling/integration/gtceu/macerator/macerate_lapis_lazuli",
        "greate:milling/integration/gtceu/macerator/macerate_lapis_plate",
        "greate:milling/integration/gtceu/macerator/macerate_lapis_refined_ore_to_dust",
        "greate:pressing/refined_lapis_to_dust",
        "gtceu:shaped/small_dust_assembling_lapis",
        "gtceu:shaped/tiny_dust_assembling_lapis",
        "gtceu:shapeless/centrifuged_ore_to_dust_lapis",
        "tfg:ae_transform/lapis_dust_from_impure",
        "tfg:ae_transform/lapis_dust_from_pure",
        "tfg:instant_barrel/lapis_dust_from_impure",
        "tfg:instant_barrel/lapis_dust_from_pure",
        "tfg:quern/lapis_gem_to_dust",
        "tfg:splashing/lapis_dust_from_impure_distilled",
        "tfg:splashing/lapis_dust_from_impure_water",
        "tfg:splashing/lapis_dust_from_pure_distilled",
        "tfg:splashing/lapis_dust_from_pure_water",
        "tfg:vi/centrifuge/lapis_dust_from_impure",
        "tfg:vi/centrifuge/lapis_dust_from_pure"
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
          "text": "(Al₆Si₆Ca₈Na₈)₁₂(Al₃Si₃Na₄Cl)₂(FeS₂)(CaCO₃)"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 13 mB of §fAluminium Silicate§7 (at Brilliant White§7)"
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
            "greate:milling",
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
      "id": "gtceu:lapis_indicator",
      "namespace": "gtceu",
      "display_name": "Lapis Surface Rock",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:surface_rocks",
        "forge:surface_rocks/lapis"
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
        "block_id": "gtceu:lapis_indicator",
        "block_tags": [
          "forge:surface_rocks",
          "forge:surface_rocks/lapis",
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
        }
      }
    },
    {
      "id": "gtceu:lapis_ore",
      "namespace": "gtceu",
      "display_name": "Lapis Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/lapis",
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
        "greate:milling/integration/gtceu/macerator/macerate_lapis_ore_to_crushed_ore",
        "gtceu:blasting/smelt_lapis_ore_to_ingot",
        "gtceu:smelting/smelt_lapis_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:lapis_ore",
        "requires_correct_tool": true
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
          "rationale": "ore id lapis_ore"
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
      "id": "gtceu:lapis_plate",
      "namespace": "gtceu",
      "display_name": "Lapis Plate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:plates",
        "forge:plates/lapis",
        "forge:sheets/lapis",
        "tfc:pileable_sheets"
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
        "greate:cutting": 3
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_lapis_plate",
        "gtceu:shaped/fluid_filter_lapis"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_lapis_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_lapis_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_lapis_block_to_plate_water"
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
          "text": "(Al₆Si₆Ca₈Na₈)₁₂(Al₃Si₃Na₄Cl)₂(FeS₂)(CaCO₃)"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fAluminium Silicate§7 (at Brilliant White§7)"
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
      "id": "gtceu:lapis_rod",
      "namespace": "gtceu",
      "display_name": "Lapis Rod",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:rods",
        "forge:rods/lapis",
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
        "greate:milling/integration/gtceu/macerator/macerate_lapis_rod",
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
        "tfg:polishing/lapis_rod",
        "tfg:vi/lathe/lapis_to_rod"
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
          "text": "(Al₆Si₆Ca₈Na₈)₁₂(Al₃Si₃Na₄Cl)₂(FeS₂)(CaCO₃)"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 72 mB of §fAluminium Silicate§7 (at Brilliant White§7)"
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
      "id": "gtceu:lapotron_crystal",
      "namespace": "gtceu",
      "display_name": "Lapotron Crystal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "gtceu:batteries",
        "gtceu:batteries/ev",
        "tfclunchbox:electric_batteries"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "kubejs:shaped": 2
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 4,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:shaped/ev_power_unit_lapotron_crystal",
        "gtceu:shaped/power_substation",
        "tfg:crafting/pocket_computer_advanced",
        "tfg:sophisticated_backpacks/shaped/everlasting_upgrade"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/lapotron_crystal",
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
          "text": "0/25,000,000 EU§7 - Tier §5EV §7(0/610 seconds remaining§7)"
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
          "key": "item.gtceu.lapotron_crystal.tooltip",
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
        "form": {
          "value": "crystal",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _crystal"
        },
        "processing_in": {
          "values": [
            "crafting",
            "kubejs:shaped"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:lapotron_dust",
      "namespace": "gtceu",
      "display_name": "Lapotron Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/lapotron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "greate:mixing": 1,
        "tfc:quern": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "greate:mixing/integration/gtceu/mixer/lapotron_dust",
        "tfg:quern/lapotron_gem_to_dust"
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
        }
      }
    },
    {
      "id": "gtceu:lapotron_gem",
      "namespace": "gtceu",
      "display_name": "Lapotron",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:gems",
        "forge:gems",
        "forge:gems/lapotron",
        "wan_ancient_beasts:snatcher_steals"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "constructionwand:shaped/iron_wand"
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