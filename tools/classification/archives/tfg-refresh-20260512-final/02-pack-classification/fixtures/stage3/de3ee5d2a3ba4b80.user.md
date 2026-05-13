# Items to classify
{
  "items": [
    {
      "id": "gtceu:poor_raw_yellow_garnet",
      "namespace": "gtceu",
      "display_name": "Poor Raw Yellow Garnet",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/yellow_garnet",
        "sns:allowed_in_ore_sack"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "create:sandpaper_polishing",
        "greate:milling",
        "greate:pressing",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "create:sandpaper_polishing": 1,
        "greate:milling": 1,
        "greate:pressing": 1,
        "smelting": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 4,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_poor_raw_yellow_garnet_ore_to_crushed_ore",
        "greate:pressing/poor_raw_yellow_garnet_to_gem",
        "gtceu:smelting/smelt_poor_yellow_garnet_ore_to_ingot",
        "tfg:polishing/poor_raw_yellow_garnet_to_gem"
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
          "text": "(Ca₃Fe₂Si₃O₁₂)₅(Ca₃Al₂Si₃O₁₂)₈(Ca₃Cr₂Si₃O₁₂)₃"
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
            "create:sandpaper_polishing",
            "greate:milling",
            "greate:pressing",
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:poor_raw_zeolite",
      "namespace": "gtceu",
      "display_name": "Poor Raw Zeolite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/zeolite",
        "sns:allowed_in_ore_sack"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "greate:pressing",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "greate:pressing": 1,
        "smelting": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 3,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_poor_raw_zeolite_ore_to_crushed_ore",
        "greate:pressing/poor_raw_zeolite_to_crushed_ore",
        "gtceu:smelting/smelt_poor_zeolite_ore_to_ingot"
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
          "text": "Na₂Al₂Si₃O₁₀(H₂O)₂"
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
            "greate:milling",
            "greate:pressing",
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:portable_debug_scanner",
      "namespace": "gtceu",
      "display_name": "Portable Debug Scanner",
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
        "item/portable_debug_scanner",
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
          "text": "0/1,000,000 EU - Tier §bMV"
        },
        {
          "source": "runtime-tooltip",
          "text": "Use while sneaking to switch mode"
        },
        {
          "source": "runtime-tooltip",
          "text": "Display mode: Show all info"
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
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "gtceu:portable_scanner",
      "namespace": "gtceu",
      "display_name": "Portable Scanner",
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
        "crafting_shaped": 3
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_portable_scanner"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/portable_scanner_mv_cadmium_battery",
        "gtceu:shaped/portable_scanner_mv_lithium_battery",
        "gtceu:shaped/portable_scanner_mv_sodium_battery"
      ],
      "model_parents": [
        "item/portable_scanner",
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
          "text": "0/100,000 EU - Tier §bMV"
        },
        {
          "source": "runtime-tooltip",
          "text": "Use while sneaking to switch mode"
        },
        {
          "source": "runtime-tooltip",
          "text": "Display mode: Show all info"
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
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "gtceu:potash_dust",
      "namespace": "gtceu",
      "display_name": "Potash Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/potash"
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
        "gtceu:shaped/small_dust_disassembling_3x3_potash",
        "gtceu:shaped/small_dust_disassembling_potash",
        "gtceu:shaped/tiny_dust_disassembling_3x3_potash",
        "gtceu:shaped/tiny_dust_disassembling_potash"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_potash",
        "gtceu:shaped/tiny_dust_assembling_potash"
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
          "text": "K₂O"
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
      "id": "gtceu:potassium_bucket",
      "namespace": "gtceu",
      "display_name": "Liquid Potassium Bucket",
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
          "text": "K"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 337 K"
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
      "id": "gtceu:potassium_carbonate_dust",
      "namespace": "gtceu",
      "display_name": "Potassium Carbonate Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/potassium_carbonate"
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
        "gtceu:shaped/small_dust_disassembling_3x3_potassium_carbonate",
        "gtceu:shaped/small_dust_disassembling_potassium_carbonate",
        "gtceu:shaped/tiny_dust_disassembling_3x3_potassium_carbonate",
        "gtceu:shaped/tiny_dust_disassembling_potassium_carbonate"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_potassium_carbonate",
        "gtceu:shaped/tiny_dust_assembling_potassium_carbonate"
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
          "text": "K₂CO₃"
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
      "id": "gtceu:potassium_cyanide_dust",
      "namespace": "gtceu",
      "display_name": "Potassium Cyanide Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/potassium_cyanide"
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
        "gtceu:shaped/small_dust_disassembling_3x3_potassium_cyanide",
        "gtceu:shaped/small_dust_disassembling_potassium_cyanide",
        "gtceu:shaped/tiny_dust_disassembling_3x3_potassium_cyanide",
        "gtceu:shaped/tiny_dust_disassembling_potassium_cyanide"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_potassium_cyanide",
        "gtceu:shaped/tiny_dust_assembling_potassium_cyanide"
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
          "text": "KCN"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
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
      "id": "gtceu:potassium_dichromate_dust",
      "namespace": "gtceu",
      "display_name": "Potassium Dichromate Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/potassium_dichromate"
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
        "gtceu:shaped/small_dust_disassembling_3x3_potassium_dichromate",
        "gtceu:shaped/small_dust_disassembling_potassium_dichromate",
        "gtceu:shaped/tiny_dust_disassembling_3x3_potassium_dichromate",
        "gtceu:shaped/tiny_dust_disassembling_potassium_dichromate"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_potassium_dichromate",
        "gtceu:shaped/tiny_dust_assembling_potassium_dichromate"
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
          "text": "K₂Cr₂O₇"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
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
      "id": "gtceu:potassium_dust",
      "namespace": "gtceu",
      "display_name": "Potassium Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/potassium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:mixing",
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "greate:mixing": 2,
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/ender_pearl_dust",
        "greate:mixing/integration/tfg/pure_potassium",
        "gtceu:shaped/small_dust_disassembling_3x3_potassium",
        "gtceu:shaped/small_dust_disassembling_potassium",
        "gtceu:shaped/tiny_dust_disassembling_3x3_potassium",
        "gtceu:shaped/tiny_dust_disassembling_potassium"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_potassium",
        "gtceu:shaped/tiny_dust_assembling_potassium"
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
          "text": "K"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§d(K) Potassium: §r15.0%"
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
      "id": "gtceu:potassium_feldspar_dust",
      "namespace": "gtceu",
      "display_name": "Potassium Feldspar Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/potassium_feldspar"
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
        "gtceu:shaped/small_dust_disassembling_3x3_potassium_feldspar",
        "gtceu:shaped/small_dust_disassembling_potassium_feldspar",
        "gtceu:shaped/tiny_dust_disassembling_3x3_potassium_feldspar",
        "gtceu:shaped/tiny_dust_disassembling_potassium_feldspar"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_potassium_feldspar",
        "gtceu:shaped/tiny_dust_assembling_potassium_feldspar"
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
          "text": "KAlSi₃O₈"
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
      "id": "gtceu:potassium_ferrocyanide_dust",
      "namespace": "gtceu",
      "display_name": "Potassium Ferrocyanide Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/potassium_ferrocyanide"
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
        "gtceu:shaped/small_dust_disassembling_3x3_potassium_ferrocyanide",
        "gtceu:shaped/small_dust_disassembling_potassium_ferrocyanide",
        "gtceu:shaped/tiny_dust_disassembling_3x3_potassium_ferrocyanide",
        "gtceu:shaped/tiny_dust_disassembling_potassium_ferrocyanide"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_potassium_ferrocyanide",
        "gtceu:shaped/tiny_dust_assembling_potassium_ferrocyanide"
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
          "text": "K₄[Fe(CN)₆]"
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
      "id": "gtceu:potassium_hydroxide_dust",
      "namespace": "gtceu",
      "display_name": "Potassium Hydroxide Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/potassium_hydroxide"
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
        "gtceu:shaped/small_dust_disassembling_3x3_potassium_hydroxide",
        "gtceu:shaped/small_dust_disassembling_potassium_hydroxide",
        "gtceu:shaped/tiny_dust_disassembling_3x3_potassium_hydroxide",
        "gtceu:shaped/tiny_dust_disassembling_potassium_hydroxide"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_potassium_hydroxide",
        "gtceu:shaped/tiny_dust_assembling_potassium_hydroxide"
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
          "text": "KOH"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
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
      "id": "gtceu:potassium_iodide_dust",
      "namespace": "gtceu",
      "display_name": "Potassium Iodide Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/potassium_iodide"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:mixing",
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "greate:mixing": 1,
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/rad_away",
        "gtceu:shaped/small_dust_disassembling_3x3_potassium_iodide",
        "gtceu:shaped/small_dust_disassembling_potassium_iodide",
        "gtceu:shaped/tiny_dust_disassembling_3x3_potassium_iodide",
        "gtceu:shaped/tiny_dust_disassembling_potassium_iodide"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_potassium_iodide",
        "gtceu:shaped/tiny_dust_assembling_potassium_iodide"
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
          "text": "KI"
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
      "id": "gtceu:potassium_sulfate_dust",
      "namespace": "gtceu",
      "display_name": "Potassium Sulfate Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/potassium_sulfate"
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
        "gtceu:shaped/small_dust_disassembling_3x3_potassium_sulfate",
        "gtceu:shaped/small_dust_disassembling_potassium_sulfate",
        "gtceu:shaped/tiny_dust_disassembling_3x3_potassium_sulfate",
        "gtceu:shaped/tiny_dust_disassembling_potassium_sulfate"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_potassium_sulfate",
        "gtceu:shaped/tiny_dust_assembling_potassium_sulfate"
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
          "text": "K₂SO₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§d(K) Potassium: §r30.0%"
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
      "id": "gtceu:potin_block",
      "namespace": "gtceu",
      "display_name": "Block of Potin",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:storage_blocks",
        "forge:storage_blocks/potin",
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
        "greate:cutting/integration/gtceu/cutter/cut_potin_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_potin_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_potin_block_to_plate_water",
        "greate:milling/integration/gtceu/macerator/macerate_potin_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/potin_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:potin_block",
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
          "forge:storage_blocks/potin",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Cu₆Sn₂Pb"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 1296 mB of §fPotin§7 (at Bright Red٭§7)"
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
      "id": "gtceu:potin_bolt",
      "namespace": "gtceu",
      "display_name": "Potin Bolt",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:bolts",
        "forge:bolts/potin"
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
        "tfc:advanced_shaped_crafting": 18,
        "vintageimprovements:polishing": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "greate:cutting": 6,
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 23,
      "recipe_output_count": 8,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_potin_bolt",
        "gtceu:shaped/screw_potin",
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
        "tfg:vi/lathe/potin_bolt_to_screw"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_potin_rod_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_potin_rod_to_bolt_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_potin_rod_to_bolt_water",
        "greate:cutting/integration/gtceu/cutter/cut_potin_screw_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_potin_screw_to_bolt_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_potin_screw_to_bolt_water",
        "gtceu:shaped/bolt_saw_potin",
        "tfc:anvil/potin_bolt"
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
          "text": "Cu₆Sn₂Pb"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 18 mB of §fPotin§7 (at Bright Red٭§7)"
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
      "id": "gtceu:potin_bucket",
      "namespace": "gtceu",
      "display_name": "Liquid Potin Bucket",
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
          "text": "Cu₆Sn₂Pb"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 1,084 K"
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
      "id": "gtceu:potin_double_ingot",
      "namespace": "gtceu",
      "display_name": "Potin Double Ingot",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:double_ingots",
        "forge:double_ingots/potin",
        "tfc:pileable_double_ingots"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "vintageimprovements:hammering"
      ],
      "recipe_consumption_by_type": {
        "vintageimprovements:hammering": 8
      },
      "recipe_production_by_type": {
        "greate:compacting": 1,
        "tfc:welding": 1
      },
      "recipe_ingredient_count": 8,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "tfg:vi/hammer/potin_plate_on_bismuth_bronze_anvil",
        "tfg:vi/hammer/potin_plate_on_black_bronze_anvil",
        "tfg:vi/hammer/potin_plate_on_black_steel_anvil",
        "tfg:vi/hammer/potin_plate_on_blue_steel_anvil",
        "tfg:vi/hammer/potin_plate_on_bronze_anvil",
        "tfg:vi/hammer/potin_plate_on_red_steel_anvil",
        "tfg:vi/hammer/potin_plate_on_steel_anvil",
        "tfg:vi/hammer/potin_plate_on_wrought_iron_anvil"
      ],
      "recipe_output_examples": [
        "tfc:welding/potin_doubleIngot",
        "tfg:compacting/potin_doubleIngot"
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
          "text": "Cu₆Sn₂Pb"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 288 mB of §fPotin§7 (at Bright Red٭§7)"
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
            "vintageimprovements:hammering"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:potin_dust",
      "namespace": "gtceu",
      "display_name": "Potin Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/potin"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "gtceu:crafting_shaped_strict",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "gtceu:crafting_shaped_strict": 4,
        "smelting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "greate:milling": 14,
        "greate:mixing": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 17,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_potin",
        "gtceu:shaped/small_dust_disassembling_potin",
        "gtceu:shaped/tiny_dust_disassembling_3x3_potin",
        "gtceu:shaped/tiny_dust_disassembling_potin",
        "gtceu:smelting/smelt_dust_potin_to_ingot"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_double_potin_plate",
        "greate:milling/integration/gtceu/macerator/macerate_long_potin_rod",
        "greate:milling/integration/gtceu/macerator/macerate_potin_block",
        "greate:milling/integration/gtceu/macerator/macerate_potin_gear",
        "greate:milling/integration/gtceu/macerator/macerate_potin_huge_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_potin_ingot",
        "greate:milling/integration/gtceu/macerator/macerate_potin_large_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_potin_nonuple_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_potin_normal_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_potin_plate",
        "greate:milling/integration/gtceu/macerator/macerate_potin_quadruple_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_potin_small_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_steam_fuser",
        "greate:milling/integration/gtceu/macerator/macerate_steam_squasher",
        "greate:mixing/integration/gtceu/mixer/potin",
        "gtceu:shaped/small_dust_assembling_potin",
        "gtceu:shaped/tiny_dust_assembling_potin"
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
          "text": "Cu₆Sn₂Pb"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fPotin§7 (at Bright Red٭§7)"
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
            "gtceu:crafting_shaped_strict",
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:potin_gear",
      "namespace": "gtceu",
      "display_name": "Potin Gear",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gears",
        "forge:gears/potin"
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
        "greate:milling/integration/gtceu/macerator/macerate_potin_gear",
        "gtceu:shaped/steam_grinder"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/gear_potin",
        "tfc:welding/potin_gear",
        "tfg:compacting/potin_gear"
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
          "text": "Cu₆Sn₂Pb"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 576 mB of §fPotin§7 (at Bright Red٭§7)"
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
      "id": "gtceu:potin_huge_fluid_pipe",
      "namespace": "gtceu",
      "display_name": "Huge Potin Fluid Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:huge_fluid_pipes",
        "forge:huge_fluid_pipes/potin"
      ],
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
        "greate:milling/integration/gtceu/macerator/macerate_potin_huge_fluid_pipe"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/huge_potin_pipe"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:potin_huge_fluid_pipe",
        "block_tags": [
          "forge:huge_fluid_pipes",
          "forge:huge_fluid_pipes/potin",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Cu₆Sn₂Pb"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bTransfer Rate: §f1920 mB/t"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature Limit: §f1,546 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
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
      "id": "gtceu:potin_ingot",
      "namespace": "gtceu",
      "display_name": "Potin Ingot",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ingots",
        "forge:ingots",
        "forge:ingots/potin",
        "tfc:pileable_ingots"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "createaddition:rolling",
        "greate:compacting",
        "greate:milling",
        "vintageimprovements:polishing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "createaddition:rolling": 1,
        "greate:compacting": 2,
        "greate:milling": 1,
        "vintageimprovements:polishing": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:casting": 2
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:compacting/potin_block",
        "greate:milling/integration/gtceu/macerator/macerate_potin_ingot",
        "gtceu:shaped/stick_potin",
        "tfg:compacting/potin_doubleIngot",
        "tfg:rolling/potin_plate",
        "tfg:vi/lathe/potin_to_rod"
      ],
      "recipe_output_examples": [
        "gtceu:smelting/smelt_dust_potin_to_ingot",
        "tfg:casting/potin_ingot_ceramic",
        "tfg:casting/potin_ingot_fire"
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
          "text": "Cu₆Sn₂Pb"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fPotin§7 (at Bright Red٭§7)"
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
            "vintageimprovements:polishing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:potin_large_fluid_pipe",
      "namespace": "gtceu",
      "display_name": "Large Potin Fluid Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:large_fluid_pipes",
        "forge:large_fluid_pipes/potin"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shaped": 3
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_potin_large_fluid_pipe",
        "gtceu:shaped/steam_fuser",
        "gtceu:shaped/steam_presser",
        "gtceu:shaped/steam_squasher"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/large_potin_pipe"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:potin_large_fluid_pipe",
        "block_tags": [
          "forge:large_fluid_pipes",
          "forge:large_fluid_pipes/potin",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Cu₆Sn₂Pb"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bTransfer Rate: §f960 mB/t"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature Limit: §f1,546 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
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
      "id": "gtceu:potin_nonuple_fluid_pipe",
      "namespace": "gtceu",
      "display_name": "Nonuple Potin Fluid Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:nonuple_fluid_pipes",
        "forge:nonuple_fluid_pipes/potin"
      ],
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
        "greate:milling/integration/gtceu/macerator/macerate_potin_nonuple_fluid_pipe"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/nonuple_potin_pipe"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:potin_nonuple_fluid_pipe",
        "block_tags": [
          "forge:mineable/wrench",
          "forge:nonuple_fluid_pipes",
          "forge:nonuple_fluid_pipes/potin",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Cu₆Sn₂Pb"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bTransfer Rate: §f160 mB/t"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature Limit: §f1,546 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eChannels: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
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