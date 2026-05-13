# Items to classify
{
  "items": [
    {
      "id": "gtceu:polyvinyl_chloride_small_item_pipe",
      "namespace": "gtceu",
      "display_name": "Small Polyvinyl Chloride Item Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:small_item_pipes",
        "forge:small_item_pipes/polyvinyl_chloride"
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
        "vintageimprovements:curving": 2
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_polyvinyl_chloride_small_item_pipe",
        "gtceu:shaped/pipe_small_restrictive_polyvinyl_chloride"
      ],
      "recipe_output_examples": [
        "tfg:vi/curving/extruder/extrude_polyvinyl_chloride_small_pipe",
        "tfg:vi/curving/extruder/extrude_polyvinyl_chloride_small_pipe_dust"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:polyvinyl_chloride_small_item_pipe",
        "block_tags": [
          "forge:mineable/wrench",
          "forge:small_item_pipes",
          "forge:small_item_pipes/polyvinyl_chloride",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "C₂H₃Cl"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bTransfer Rate: §f2 stacks/s"
        },
        {
          "source": "runtime-tooltip",
          "text": "§9Priority: §f768"
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:polyvinyl_chloride_small_restrictive_item_pipe",
      "namespace": "gtceu",
      "display_name": "Small Restrictive Polyvinyl Chloride Item Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:small_restrictive_pipes",
        "forge:small_restrictive_pipes/polyvinyl_chloride"
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
        "greate:milling/integration/gtceu/macerator/macerate_polyvinyl_chloride_small_restrictive_item_pipe"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/pipe_small_restrictive_polyvinyl_chloride"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:polyvinyl_chloride_small_restrictive_item_pipe",
        "block_tags": [
          "forge:mineable/wrench",
          "forge:small_restrictive_pipes",
          "forge:small_restrictive_pipes/polyvinyl_chloride",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "C₂H₃Cl"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bTransfer Rate: §f2 stacks/s"
        },
        {
          "source": "runtime-tooltip",
          "text": "§9Priority: §f76800"
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
      "id": "gtceu:poor_raw_almandine",
      "namespace": "gtceu",
      "display_name": "Poor Raw Almandine",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/almandine",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_almandine_ore_to_crushed_ore",
        "greate:pressing/poor_raw_almandine_to_gem",
        "gtceu:smelting/smelt_poor_almandine_ore_to_ingot",
        "tfg:polishing/poor_raw_almandine_to_gem"
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
          "text": "Al₂Fe₃Si₃O₁₂"
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
      "id": "gtceu:poor_raw_aluminium",
      "namespace": "gtceu",
      "display_name": "Poor Raw Aluminium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/aluminium",
        "sns:allowed_in_ore_sack"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "greate:pressing"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "greate:pressing": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 2,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_poor_raw_aluminium_ore_to_crushed_ore",
        "greate:pressing/poor_raw_aluminium_to_crushed_ore"
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
          "text": "Al"
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
            "greate:pressing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:poor_raw_alunite",
      "namespace": "gtceu",
      "display_name": "Poor Raw Alunite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/alunite",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_alunite_ore_to_crushed_ore",
        "greate:pressing/poor_raw_alunite_to_crushed_ore",
        "gtceu:smelting/smelt_poor_alunite_ore_to_ingot"
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
          "text": "KAl₂Si₂H₆O₁₄"
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
      "id": "gtceu:poor_raw_amethyst",
      "namespace": "gtceu",
      "display_name": "Poor Raw Amethyst",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/amethyst",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_amethyst_ore_to_crushed_ore",
        "greate:pressing/poor_raw_amethyst_to_gem",
        "gtceu:smelting/smelt_poor_amethyst_ore_to_ingot",
        "tfg:polishing/poor_raw_amethyst_to_gem"
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
          "text": "(SiO₂)₄Fe"
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
      "id": "gtceu:poor_raw_apatite",
      "namespace": "gtceu",
      "display_name": "Poor Raw Apatite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/apatite",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_apatite_ore_to_crushed_ore",
        "greate:pressing/poor_raw_apatite_to_gem",
        "gtceu:smelting/smelt_poor_apatite_ore_to_ingot",
        "tfg:polishing/poor_raw_apatite_to_gem"
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
          "text": "Ca₅(PO₄)₃Cl"
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
      "id": "gtceu:poor_raw_armalcolite",
      "namespace": "gtceu",
      "display_name": "Poor Raw Armalcolite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/armalcolite",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_armalcolite_ore_to_crushed_ore",
        "greate:pressing/poor_raw_armalcolite_to_gem",
        "gtceu:smelting/smelt_poor_armalcolite_ore_to_ingot",
        "tfg:polishing/poor_raw_armalcolite_to_gem"
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
          "text": "Mg(FeTiO₃)O₂"
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
      "id": "gtceu:poor_raw_asbestos",
      "namespace": "gtceu",
      "display_name": "Poor Raw Asbestos",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/asbestos",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_asbestos_ore_to_crushed_ore",
        "greate:pressing/poor_raw_asbestos_to_crushed_ore",
        "gtceu:smelting/smelt_poor_asbestos_ore_to_ingot"
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
          "text": "Mg₃Si₂H₄O₉"
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
      "id": "gtceu:poor_raw_barite",
      "namespace": "gtceu",
      "display_name": "Poor Raw Barite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/barite",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_barite_ore_to_crushed_ore",
        "greate:pressing/poor_raw_barite_to_crushed_ore",
        "gtceu:smelting/smelt_poor_barite_ore_to_ingot"
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
          "text": "BaSO₄"
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
      "id": "gtceu:poor_raw_basaltic_mineral_sand",
      "namespace": "gtceu",
      "display_name": "Poor Raw Basaltic Mineral Sand",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/basaltic_mineral_sand",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_basaltic_mineral_sand_ore_to_crushed_ore",
        "greate:pressing/poor_raw_basaltic_mineral_sand_to_crushed_ore",
        "gtceu:smelting/smelt_poor_basaltic_mineral_sand_ore_to_ingot"
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
          "text": "(Fe₃O₄)((MgFe(SiO₂)₂)(CaCO₃)₃(SiO₂)₈C₄)"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 18 mB of §fCast Iron§7 (at Brilliant White§7)"
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
      "id": "gtceu:poor_raw_bastnasite",
      "namespace": "gtceu",
      "display_name": "Poor Raw Bastnasite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/bastnasite",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_bastnasite_ore_to_crushed_ore",
        "greate:pressing/poor_raw_bastnasite_to_crushed_ore",
        "gtceu:smelting/smelt_poor_bastnasite_ore_to_ingot"
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
          "text": "CeCFO₃"
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
      "id": "gtceu:poor_raw_bauxite",
      "namespace": "gtceu",
      "display_name": "Poor Raw Bauxite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/bauxite",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_bauxite_ore_to_crushed_ore",
        "greate:pressing/poor_raw_bauxite_to_crushed_ore",
        "gtceu:smelting/smelt_poor_bauxite_ore_to_ingot"
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
          "text": "Al₂O₃"
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
      "id": "gtceu:poor_raw_bentonite",
      "namespace": "gtceu",
      "display_name": "Poor Raw Bentonite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/bentonite",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_bentonite_ore_to_crushed_ore",
        "greate:pressing/poor_raw_bentonite_to_crushed_ore",
        "gtceu:smelting/smelt_poor_bentonite_ore_to_ingot"
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
          "text": "NaMg₆Si₁₂H₆(H₂O)₅O₃₆"
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
      "id": "gtceu:poor_raw_beryllium",
      "namespace": "gtceu",
      "display_name": "Poor Raw Beryllium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/beryllium",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_beryllium_ore_to_crushed_ore",
        "greate:pressing/poor_raw_beryllium_to_crushed_ore",
        "gtceu:smelting/smelt_poor_beryllium_ore_to_ingot"
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
          "text": "Be"
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
      "id": "gtceu:poor_raw_blue_topaz",
      "namespace": "gtceu",
      "display_name": "Poor Raw Blue Topaz",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/blue_topaz",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_blue_topaz_ore_to_crushed_ore",
        "greate:pressing/poor_raw_blue_topaz_to_gem",
        "gtceu:smelting/smelt_poor_blue_topaz_ore_to_ingot",
        "tfg:polishing/poor_raw_blue_topaz_to_gem"
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
          "text": "Al₂SiO₄F₂"
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
      "id": "gtceu:poor_raw_borax",
      "namespace": "gtceu",
      "display_name": "Poor Raw Borax",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/borax",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_borax_ore_to_crushed_ore",
        "greate:pressing/poor_raw_borax_to_crushed_ore",
        "gtceu:smelting/smelt_poor_borax_ore_to_ingot"
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
          "text": "Na₂B₄(H₂O)₁₀O₇"
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
      "id": "gtceu:poor_raw_bornite",
      "namespace": "gtceu",
      "display_name": "Poor Raw Bornite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/bornite",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_bornite_ore_to_crushed_ore",
        "greate:pressing/poor_raw_bornite_to_crushed_ore",
        "gtceu:smelting/smelt_poor_bornite_ore_to_ingot"
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
          "text": "Cu₅FeS₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 21 mB of §fCopper§7 (at Orange٭٭٭٭§7)"
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
      "id": "gtceu:poor_raw_calcite",
      "namespace": "gtceu",
      "display_name": "Poor Raw Calcite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/calcite",
        "sns:allowed_in_ore_sack"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "greate:pressing",
        "smelting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "greate:pressing": 1,
        "smelting": 1,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 4,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_poor_raw_calcite_ore_to_crushed_ore",
        "greate:pressing/poor_raw_calcite_to_crushed_ore",
        "gtceu:smelting/smelt_poor_calcite_ore_to_ingot",
        "tfg:shapeless/calcite_from_poor_raw"
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
          "text": "CaCO₃"
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
            "smelting",
            "tfc:damage_inputs_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:poor_raw_cassiterite_sand",
      "namespace": "gtceu",
      "display_name": "Poor Raw Cassiterite Sand",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/cassiterite_sand",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_cassiterite_sand_ore_to_crushed_ore",
        "greate:pressing/poor_raw_cassiterite_sand_to_crushed_ore",
        "gtceu:smelting/smelt_poor_cassiterite_sand_ore_to_ingot"
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
          "text": "SnO₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 18 mB of §fTin§7 (at Very Hot§7)"
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
      "id": "gtceu:poor_raw_certus_quartz",
      "namespace": "gtceu",
      "display_name": "Poor Raw Certus Quartz",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/certus_quartz",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_certus_quartz_ore_to_crushed_ore",
        "greate:pressing/poor_raw_certus_quartz_to_gem",
        "gtceu:smelting/smelt_poor_certus_quartz_ore_to_ingot",
        "tfg:polishing/poor_raw_certus_quartz_to_gem"
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
          "text": "SiO₂"
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
      "id": "gtceu:poor_raw_chalcocite",
      "namespace": "gtceu",
      "display_name": "Poor Raw Chalcocite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/chalcocite",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_chalcocite_ore_to_crushed_ore",
        "greate:pressing/poor_raw_chalcocite_to_crushed_ore",
        "gtceu:smelting/smelt_poor_chalcocite_ore_to_ingot"
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
          "text": "Cu₂S"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 22 mB of §fCopper§7 (at Orange٭٭٭٭§7)"
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
      "id": "gtceu:poor_raw_chalcopyrite",
      "namespace": "gtceu",
      "display_name": "Poor Raw Chalcopyrite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/chalcopyrite",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_chalcopyrite_ore_to_crushed_ore",
        "greate:pressing/poor_raw_chalcopyrite_to_crushed_ore",
        "gtceu:smelting/smelt_poor_chalcopyrite_ore_to_ingot"
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
          "text": "CuFeS₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 19 mB of §fCopper§7 (at Orange٭٭٭٭§7)"
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
      "id": "gtceu:poor_raw_cinnabar",
      "namespace": "gtceu",
      "display_name": "Poor Raw Cinnabar",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/cinnabar",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_cinnabar_ore_to_crushed_ore",
        "greate:pressing/poor_raw_cinnabar_to_gem",
        "gtceu:smelting/smelt_poor_cinnabar_ore_to_ingot",
        "tfg:polishing/poor_raw_cinnabar_to_gem"
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
          "text": "HgS"
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
      "id": "gtceu:poor_raw_cobalt",
      "namespace": "gtceu",
      "display_name": "Poor Raw Cobalt",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:poor_raw_materials",
        "forge:poor_raw_materials/cobalt",
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
        "greate:milling/integration/gtceu/macerate_poor_raw_cobalt_ore_to_crushed_ore",
        "greate:pressing/poor_raw_cobalt_to_crushed_ore",
        "gtceu:smelting/smelt_poor_cobalt_ore_to_ingot"
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
          "text": "Co"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 24 mB of §fCobalt§7 (at White٭٭٭٭§7)"
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