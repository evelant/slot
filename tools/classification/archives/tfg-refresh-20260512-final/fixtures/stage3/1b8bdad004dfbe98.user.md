# Items to classify
{
  "items": [
    {
      "id": "gtceu:tetranitromethane_bucket",
      "namespace": "gtceu",
      "display_name": "Tetranitromethane Bucket",
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
          "text": "CN₄O₈"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 293 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
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
      "id": "gtceu:text_module",
      "namespace": "gtceu",
      "display_name": "Text Module",
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
        "greate:milling/integration/gtceu/macerator/macerate_text_module"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/text_module",
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:thallium_dust",
      "namespace": "gtceu",
      "display_name": "Thallium Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/thallium"
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
        "gtceu:shaped/small_dust_disassembling_3x3_thallium",
        "gtceu:shaped/small_dust_disassembling_thallium",
        "gtceu:shaped/tiny_dust_disassembling_3x3_thallium",
        "gtceu:shaped/tiny_dust_disassembling_thallium"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/small_dust_assembling_thallium",
        "gtceu:shaped/tiny_dust_assembling_thallium"
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
          "text": "Tl"
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
      "id": "gtceu:the_end_marker",
      "namespace": "gtceu",
      "display_name": "The End",
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
        "item/the_end_marker",
        "block/the_end_marker",
        "block/cube"
      ],
      "creative_tabs": [],
      "loot_source_count": 1,
      "loot_source_examples": [
        "gtceu:blocks/the_end_marker"
      ],
      "block_context": {
        "block_id": "gtceu:the_end_marker",
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
        }
      }
    },
    {
      "id": "gtceu:the_nether_marker",
      "namespace": "gtceu",
      "display_name": "The Beneath",
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
        "item/the_nether_marker",
        "block/the_nether_marker",
        "block/cube"
      ],
      "creative_tabs": [],
      "loot_source_count": 1,
      "loot_source_examples": [
        "gtceu:blocks/the_nether_marker"
      ],
      "block_context": {
        "block_id": "gtceu:the_nether_marker",
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
        }
      }
    },
    {
      "id": "gtceu:thermochemically_treated_hardwood_dust",
      "namespace": "gtceu",
      "display_name": "Thermochemically Treated Hardwood Pulp",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/thermochemically_treated_hardwood"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:pressing",
        "gtceu:crafting_shaped_strict",
        "vintageimprovements:hammering"
      ],
      "recipe_consumption_by_type": {
        "greate:pressing": 1,
        "gtceu:crafting_shaped_strict": 4,
        "vintageimprovements:hammering": 9
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "firmalife:vat": 1,
        "greate:mixing": 1
      },
      "recipe_ingredient_count": 14,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:pressing/soaked_unrefined_paper",
        "gtceu:shaped/small_dust_disassembling_3x3_thermochemically_treated_hardwood",
        "gtceu:shaped/small_dust_disassembling_thermochemically_treated_hardwood",
        "gtceu:shaped/tiny_dust_disassembling_3x3_thermochemically_treated_hardwood",
        "gtceu:shaped/tiny_dust_disassembling_thermochemically_treated_hardwood",
        "tfg:vi/hammer/gtceu_thermochemically_treated_hardwood_dust_on_bismuth_bronze_anvil",
        "tfg:vi/hammer/gtceu_thermochemically_treated_hardwood_dust_on_black_bronze_anvil",
        "tfg:vi/hammer/gtceu_thermochemically_treated_hardwood_dust_on_black_steel_anvil",
        "tfg:vi/hammer/gtceu_thermochemically_treated_hardwood_dust_on_blue_steel_anvil",
        "tfg:vi/hammer/gtceu_thermochemically_treated_hardwood_dust_on_bronze_anvil",
        "tfg:vi/hammer/gtceu_thermochemically_treated_hardwood_dust_on_copper_anvil",
        "tfg:vi/hammer/gtceu_thermochemically_treated_hardwood_dust_on_red_steel_anvil",
        "tfg:vi/hammer/gtceu_thermochemically_treated_hardwood_dust_on_steel_anvil",
        "tfg:vi/hammer/gtceu_thermochemically_treated_hardwood_dust_on_wrought_iron_anvil"
      ],
      "recipe_output_examples": [
        "greate:mixing/integration/tfg/mixer/mix_hardwood_dust_with_lye",
        "gtceu:shaped/small_dust_assembling_thermochemically_treated_hardwood",
        "gtceu:shaped/tiny_dust_assembling_thermochemically_treated_hardwood",
        "tfg:vat/thermochemically_treat_hardwood_dust"
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
      "document_context": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "label": "Papermaking",
          "item_ref_count": 5,
          "related_item_refs": [
            "minecraft:book",
            "tfc:groundcover/pumice",
            "tfc:unrefined_paper",
            "tfg:soaked_unrefined_paper"
          ],
          "snippets": [
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "Paper is either made from the processed stalk of the Papyrus crop, from Animal Hides, or from a lengthy process using specific types of Wood. Paper is useful for written materials like Books and Maps."
            },
            {
              "source": "guide-page",
              "key": "pages.1.text",
              "text": "Papyrus must first be cut into strips with a Knife"
            },
            {
              "source": "guide-page",
              "key": "pages.2.text",
              "text": "Then, papyrus strips are soaked in a Barrel of Water."
            },
            {
              "source": "guide-page",
              "key": "pages.3.text",
              "text": "Then, soaked papyrus strips are woven together in a loom to make Unrefined Paper. Finally, it must be placed on a log and Scraped to make Paper."
            }
          ]
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
            "greate:pressing",
            "gtceu:crafting_shaped_strict",
            "vintageimprovements:hammering"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:thorium_230_block",
      "namespace": "gtceu",
      "display_name": "Block of Thorium 230",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:storage_blocks",
        "forge:storage_blocks/thorium_230",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {
        "greate:compacting": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_thorium_230_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/thorium_230_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:thorium_230_block",
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
          "forge:storage_blocks/thorium_230",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Th²³⁰"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
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
      "id": "gtceu:thorium_230_dust",
      "namespace": "gtceu",
      "display_name": "Thorium 230 Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/thorium_230"
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
        "greate:milling": 3
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 5,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_thorium_230",
        "gtceu:shaped/small_dust_disassembling_thorium_230",
        "gtceu:shaped/tiny_dust_disassembling_3x3_thorium_230",
        "gtceu:shaped/tiny_dust_disassembling_thorium_230",
        "gtceu:smelting/smelt_dust_thorium_230_to_ingot"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_long_thorium_230_rod",
        "greate:milling/integration/gtceu/macerator/macerate_thorium_230_block",
        "greate:milling/integration/gtceu/macerator/macerate_thorium_230_ingot",
        "gtceu:shaped/small_dust_assembling_thorium_230",
        "gtceu:shaped/tiny_dust_assembling_thorium_230"
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
          "text": "Th²³⁰"
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
      "id": "gtceu:thorium_230_ingot",
      "namespace": "gtceu",
      "display_name": "Thorium 230 Ingot",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ingots",
        "forge:ingots",
        "forge:ingots/thorium_230",
        "tfc:pileable_ingots"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:compacting",
        "greate:milling",
        "vintageimprovements:polishing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "greate:compacting": 1,
        "greate:milling": 1,
        "vintageimprovements:polishing": 1
      },
      "recipe_production_by_type": {
        "smelting": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:compacting/thorium_230_block",
        "greate:milling/integration/gtceu/macerator/macerate_thorium_230_ingot",
        "gtceu:shaped/stick_thorium_230",
        "tfg:vi/lathe/thorium_230_to_rod"
      ],
      "recipe_output_examples": [
        "gtceu:smelting/smelt_dust_thorium_230_to_ingot"
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
          "text": "Th²³⁰"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
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
      "id": "gtceu:thorium_230_nugget",
      "namespace": "gtceu",
      "display_name": "Thorium 230 Nugget",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:nuggets",
        "forge:nuggets",
        "forge:nuggets/thorium_230"
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
        "greate:milling/integration/gtceu/macerator/macerate_thorium_230_nugget"
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
          "text": "Th²³⁰"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Instant Radiation (00:00)"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:food": {
          "nutrition": 0,
          "saturation_modifier": 0,
          "is_meat": false,
          "can_always_eat": false,
          "is_fast_food": false,
          "effects": [
            {
              "effect": "tfg:instant_radiation",
              "duration": -1,
              "amplifier": 0,
              "chance": 0.5
            }
          ]
        },
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
      "id": "gtceu:thorium_230_rod",
      "namespace": "gtceu",
      "display_name": "Thorium 230 Rod",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:rods",
        "forge:rods/thorium_230"
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
        "greate:milling/integration/gtceu/macerator/macerate_thorium_230_rod",
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
        "greate:cutting/integration/gtceu/cutter/cut_thorium_230_long_rod_to_rod",
        "greate:cutting/integration/gtceu/cutter/cut_thorium_230_long_rod_to_rod_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_thorium_230_long_rod_to_rod_water",
        "gtceu:shaped/stick_long_thorium_230",
        "gtceu:shaped/stick_thorium_230",
        "tfg:vi/lathe/thorium_230_to_rod"
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
          "text": "Th²³⁰"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
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
      "id": "gtceu:thorium_232_block",
      "namespace": "gtceu",
      "display_name": "Block of Thorium 232",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:storage_blocks",
        "forge:storage_blocks/thorium_232",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {
        "greate:compacting": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_thorium_232_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/thorium_232_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:thorium_232_block",
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
          "forge:storage_blocks/thorium_232",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Th²³²"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
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
      "id": "gtceu:thorium_232_dust",
      "namespace": "gtceu",
      "display_name": "Thorium 232 Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/thorium_232"
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
        "greate:milling": 3
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 5,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_thorium_232",
        "gtceu:shaped/small_dust_disassembling_thorium_232",
        "gtceu:shaped/tiny_dust_disassembling_3x3_thorium_232",
        "gtceu:shaped/tiny_dust_disassembling_thorium_232",
        "gtceu:smelting/smelt_dust_thorium_232_to_ingot"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_long_thorium_232_rod",
        "greate:milling/integration/gtceu/macerator/macerate_thorium_232_block",
        "greate:milling/integration/gtceu/macerator/macerate_thorium_232_ingot",
        "gtceu:shaped/small_dust_assembling_thorium_232",
        "gtceu:shaped/tiny_dust_assembling_thorium_232"
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
          "text": "Th²³²"
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
      "id": "gtceu:thorium_232_ingot",
      "namespace": "gtceu",
      "display_name": "Thorium 232 Ingot",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ingots",
        "forge:ingots",
        "forge:ingots/thorium_232",
        "tfc:pileable_ingots"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:compacting",
        "greate:milling",
        "vintageimprovements:polishing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "greate:compacting": 1,
        "greate:milling": 1,
        "vintageimprovements:polishing": 1
      },
      "recipe_production_by_type": {
        "smelting": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:compacting/thorium_232_block",
        "greate:milling/integration/gtceu/macerator/macerate_thorium_232_ingot",
        "gtceu:shaped/stick_thorium_232",
        "tfg:vi/lathe/thorium_232_to_rod"
      ],
      "recipe_output_examples": [
        "gtceu:smelting/smelt_dust_thorium_232_to_ingot"
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
          "text": "Th²³²"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
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
      "id": "gtceu:thorium_232_nugget",
      "namespace": "gtceu",
      "display_name": "Thorium 232 Nugget",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:nuggets",
        "forge:nuggets",
        "forge:nuggets/thorium_232"
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
        "greate:milling/integration/gtceu/macerator/macerate_thorium_232_nugget"
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
          "text": "Th²³²"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Instant Radiation (00:00)"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:food": {
          "nutrition": 0,
          "saturation_modifier": 0,
          "is_meat": false,
          "can_always_eat": false,
          "is_fast_food": false,
          "effects": [
            {
              "effect": "tfg:instant_radiation",
              "duration": -1,
              "amplifier": 0,
              "chance": 0.5
            }
          ]
        },
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
      "id": "gtceu:thorium_232_rod",
      "namespace": "gtceu",
      "display_name": "Thorium 232 Rod",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:rods",
        "forge:rods/thorium_232"
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
        "greate:milling/integration/gtceu/macerator/macerate_thorium_232_rod",
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
        "greate:cutting/integration/gtceu/cutter/cut_thorium_232_long_rod_to_rod",
        "greate:cutting/integration/gtceu/cutter/cut_thorium_232_long_rod_to_rod_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_thorium_232_long_rod_to_rod_water",
        "gtceu:shaped/stick_long_thorium_232",
        "gtceu:shaped/stick_thorium_232",
        "tfg:vi/lathe/thorium_232_to_rod"
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
          "text": "Th²³²"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
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
      "id": "gtceu:thorium_dust",
      "namespace": "gtceu",
      "display_name": "Thorianite Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/thorium"
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
        "crafting_shaped": 2,
        "crafting_shapeless": 1,
        "greate:milling": 3,
        "greate:pressing": 1,
        "greate:splashing": 4,
        "tfc:barrel_instant": 2,
        "vintageimprovements:centrifugation": 2
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 17,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_thorium",
        "gtceu:shaped/small_dust_disassembling_thorium",
        "gtceu:shaped/tiny_dust_disassembling_3x3_thorium",
        "gtceu:shaped/tiny_dust_disassembling_thorium"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerate_thorium_refined_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_thorium_plate",
        "greate:milling/integration/gtceu/macerator/macerate_thorium_refined_ore_to_dust",
        "greate:pressing/refined_thorium_to_dust",
        "gtceu:shaped/small_dust_assembling_thorium",
        "gtceu:shaped/tiny_dust_assembling_thorium",
        "gtceu:shapeless/centrifuged_ore_to_dust_thorium",
        "tfg:ae_transform/thorium_dust_from_impure",
        "tfg:ae_transform/thorium_dust_from_pure",
        "tfg:instant_barrel/thorium_dust_from_impure",
        "tfg:instant_barrel/thorium_dust_from_pure",
        "tfg:splashing/thorium_dust_from_impure_distilled",
        "tfg:splashing/thorium_dust_from_impure_water",
        "tfg:splashing/thorium_dust_from_pure_distilled",
        "tfg:splashing/thorium_dust_from_pure_water",
        "tfg:vi/centrifuge/thorium_dust_from_impure",
        "tfg:vi/centrifuge/thorium_dust_from_pure"
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
          "text": "ThO₂"
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
      "id": "gtceu:thorium_indicator",
      "namespace": "gtceu",
      "display_name": "Thorianite Surface Rock",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:surface_rocks",
        "forge:surface_rocks/thorium"
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
        "block_id": "gtceu:thorium_indicator",
        "block_tags": [
          "forge:surface_rocks",
          "forge:surface_rocks/thorium",
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
          "text": "ThO₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
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
      "id": "gtceu:thorium_ore",
      "namespace": "gtceu",
      "display_name": "Thorianite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/thorium",
        "forge:ores_in_ground/stone"
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
        "greate:milling/integration/gtceu/macerator/macerate_thorium_ore_to_crushed_ore"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:thorium_ore",
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "ThO₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
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
          "value": "thorium",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id thorium_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "processing_in": {
          "values": [
            "greate:milling"
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
      "id": "gtceu:thorium_plate",
      "namespace": "gtceu",
      "display_name": "Thorianite Plate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
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
        "greate:milling/integration/gtceu/macerator/macerate_thorium_plate"
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
          "text": "ThO₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:thorium_rod",
      "namespace": "gtceu",
      "display_name": "Thorianite Rod",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
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
        "greate:milling/integration/gtceu/macerator/macerate_thorium_rod"
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
          "text": "ThO₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "§l§cHAZARDOUS §7Hold Shift to show details"
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:tin_alloy_block",
      "namespace": "gtceu",
      "display_name": "Block of Tin Alloy",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "createdeco:internal/blocks/andesite_blocks",
        "forge:storage_blocks",
        "forge:storage_blocks/tin_alloy",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:cutting",
        "greate:milling",
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "greate:cutting": 3,
        "greate:milling": 1,
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {
        "greate:compacting": 1
      },
      "recipe_ingredient_count": 8,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "createdeco:andesite_hull",
        "greate:cutting/integration/gtceu/cutter/cut_tin_alloy_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_tin_alloy_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_tin_alloy_block_to_plate_water",
        "greate:milling/integration/gtceu/macerator/macerate_tin_alloy_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/tin_alloy_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:tin_alloy_block",
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
          "forge:storage_blocks/tin_alloy",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "SnFe"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 1296 mB of §fTin Alloy§7 (at Yellow٭٭٭§7)"
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
            "crafting",
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
      "id": "gtceu:tin_alloy_bolt",
      "namespace": "gtceu",
      "display_name": "Tin Alloy Bolt",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:bolts",
        "forge:bolts/tin_alloy"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped",
        "tfc:advanced_shaped_crafting",
        "vintageimprovements:polishing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 6,
        "crafting_shapeless": 1,
        "greate:milling": 1,
        "kubejs:shaped": 2,
        "tfc:advanced_shaped_crafting": 18,
        "vintageimprovements:polishing": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "greate:cutting": 6,
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 29,
      "recipe_output_count": 8,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_tin_alloy_bolt",
        "gtceu:shaped/screw_tin_alloy",
        "minecraft:kjs/createdeco_blue_andesite_lamp",
        "minecraft:kjs/createdeco_green_andesite_lamp",
        "minecraft:kjs/createdeco_red_andesite_lamp",
        "minecraft:kjs/createdeco_yellow_andesite_lamp",
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
        "tfg:simplylight/edge_light",
        "tfg:simplylight/rod_lamp",
        "tfg:vi/lathe/tin_alloy_bolt_to_screw"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_tin_alloy_rod_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_tin_alloy_rod_to_bolt_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_tin_alloy_rod_to_bolt_water",
        "greate:cutting/integration/gtceu/cutter/cut_tin_alloy_screw_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_tin_alloy_screw_to_bolt_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_tin_alloy_screw_to_bolt_water",
        "gtceu:shaped/bolt_saw_tin_alloy",
        "tfc:anvil/tin_alloy_bolt"
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
          "text": "SnFe"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 18 mB of §fTin Alloy§7 (at Yellow٭٭٭§7)"
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
            "kubejs:shaped",
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
      "id": "gtceu:tin_alloy_bucket",
      "namespace": "gtceu",
      "display_name": "Liquid Tin Alloy Bucket",
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
          "text": "SnFe"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 1,258 K"
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
      "id": "gtceu:tin_alloy_double_ingot",
      "namespace": "gtceu",
      "display_name": "Tin Alloy Double Ingot",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:double_ingots",
        "forge:double_ingots/tin_alloy",
        "tfc:pileable_double_ingots"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "vintageimprovements:hammering"
      ],
      "recipe_consumption_by_type": {
        "vintageimprovements:hammering": 5
      },
      "recipe_production_by_type": {
        "greate:compacting": 1,
        "tfc:welding": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "tfg:vi/hammer/tin_alloy_plate_on_black_steel_anvil",
        "tfg:vi/hammer/tin_alloy_plate_on_blue_steel_anvil",
        "tfg:vi/hammer/tin_alloy_plate_on_red_steel_anvil",
        "tfg:vi/hammer/tin_alloy_plate_on_steel_anvil",
        "tfg:vi/hammer/tin_alloy_plate_on_wrought_iron_anvil"
      ],
      "recipe_output_examples": [
        "tfc:welding/tin_alloy_doubleIngot",
        "tfg:compacting/tin_alloy_doubleIngot"
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
          "text": "SnFe"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 288 mB of §fTin Alloy§7 (at Yellow٭٭٭§7)"
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