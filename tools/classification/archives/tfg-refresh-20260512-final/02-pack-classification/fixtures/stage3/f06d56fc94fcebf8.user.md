# Items to classify
{
  "items": [
    {
      "id": "tfg:flask",
      "namespace": "tfg",
      "display_name": "Flask",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:fluid_item_ingredient_empty_containers",
        "tfc:glass_bottles",
        "tfg:lab_equipment_containers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "kubejs_tfc:advanced_shapeless_crafting",
        "tfc:advanced_shapeless_crafting",
        "tfc:extra_products_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "crafting_shapeless": 34,
        "kubejs_tfc:advanced_shapeless_crafting": 7,
        "tfc:advanced_shapeless_crafting": 15,
        "tfc:extra_products_shapeless_crafting": 3
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 61,
      "recipe_output_count": 0,
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
        "tfc:crafting/lingering_water_bottle",
        "tfc:crafting/lingering_water_bottle_from_lemon",
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
        "tfc:crafting/splash_water_bottle",
        "tfg:shaped/auto_drink_modifier_bamboo",
        "tfg:shaped/auto_drink_modifier_rubber",
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
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§9Fluid Capacity: §f144 mB"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "kubejs_tfc:advanced_shapeless_crafting",
            "tfc:advanced_shapeless_crafting",
            "tfc:extra_products_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:flavolite_lorandite_ore",
      "namespace": "tfg",
      "display_name": "Ignimbrite Lorandite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/lorandite",
        "forge:ores_in_ground/flavolite"
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
        "greate:milling/integration/gtceu/macerator/macerate_flavolite_lorandite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_flavolite_lorandite_ore_to_ingot",
        "gtceu:smelting/smelt_flavolite_lorandite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:flavolite_lorandite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/lorandite",
          "forge:ores_in_ground/flavolite",
          "minecraft:mineable/pickaxe",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfc:prospectable"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "TlAsS₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
          "value": "flavolite_lorandite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id flavolite_lorandite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
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
      "id": "tfg:flavolite_support",
      "namespace": "tfg",
      "display_name": "Ignimbrite Support",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:igneous_extrusive_items",
        "tfc:support_beams",
        "tfg:stone_composition/igneous_felsic_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "tfc:damage_inputs_shaped_crafting": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_felsic_half"
      ],
      "recipe_output_examples": [
        "tfc:kjs/damage/shaped/flavolite_support"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:flavolite_support",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:igneous_extrusive_items",
          "tfc:support_beams"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Igneous Extrusive"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:flawed_fluix_gem",
      "namespace": "tfg",
      "display_name": "Flawed Fluix",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:flawed_gems",
        "forge:flawed_gems/fluix"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:cutting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:cutting": 3,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "greate:cutting": 3
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_fluix_flawed_gem_to_chipped_gem",
        "greate:cutting/integration/gtceu/cutter/cut_fluix_flawed_gem_to_chipped_gem_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_fluix_flawed_gem_to_chipped_gem_water",
        "greate:milling/integration/gtceu/macerator/macerate_flawed_fluix_gem",
        "gtceu:shapeless/gem_to_gem_chipped_gem_fluix"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_fluix_gem_to_flawed_gem",
        "greate:cutting/integration/gtceu/cutter/cut_fluix_gem_to_flawed_gem_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_fluix_gem_to_flawed_gem_water",
        "gtceu:shapeless/gem_to_gem_flawed_gem_fluix"
      ],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "(SiO₂)(SiO₂)"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "greate:cutting",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:flawed_zircon_gem",
      "namespace": "tfg",
      "display_name": "Flawed Zircon",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:flawed_gems",
        "forge:flawed_gems/zircon"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:cutting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:cutting": 3,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "greate:cutting": 3
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_zircon_flawed_gem_to_chipped_gem",
        "greate:cutting/integration/gtceu/cutter/cut_zircon_flawed_gem_to_chipped_gem_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_zircon_flawed_gem_to_chipped_gem_water",
        "greate:milling/integration/gtceu/macerator/macerate_flawed_zircon_gem",
        "gtceu:shapeless/gem_to_gem_chipped_gem_zircon"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_zircon_gem_to_flawed_gem",
        "greate:cutting/integration/gtceu/cutter/cut_zircon_gem_to_flawed_gem_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_zircon_gem_to_flawed_gem_water",
        "gtceu:shapeless/gem_to_gem_flawed_gem_zircon"
      ],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "ZrSiO₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "greate:cutting",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:flawless_fluix_gem",
      "namespace": "tfg",
      "display_name": "Flawless Fluix",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:flawless_gems",
        "forge:flawless_gems/fluix"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:cutting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:cutting": 3,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "greate:cutting": 3
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_fluix_flawless_gem_to_gem",
        "greate:cutting/integration/gtceu/cutter/cut_fluix_flawless_gem_to_gem_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_fluix_flawless_gem_to_gem_water",
        "greate:milling/integration/gtceu/macerator/macerate_flawless_fluix_gem",
        "gtceu:shapeless/gem_to_gem_gem_fluix"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_fluix_exquisite_gem_to_flawless_gem",
        "greate:cutting/integration/gtceu/cutter/cut_fluix_exquisite_gem_to_flawless_gem_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_fluix_exquisite_gem_to_flawless_gem_water",
        "gtceu:shapeless/gem_to_gem_flawless_gem_fluix"
      ],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "(SiO₂)(SiO₂)"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "greate:cutting",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:flawless_zircon_gem",
      "namespace": "tfg",
      "display_name": "Flawless Zircon",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:flawless_gems",
        "forge:flawless_gems/zircon"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:cutting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:cutting": 3,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "greate:cutting": 3
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_zircon_flawless_gem_to_gem",
        "greate:cutting/integration/gtceu/cutter/cut_zircon_flawless_gem_to_gem_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_zircon_flawless_gem_to_gem_water",
        "greate:milling/integration/gtceu/macerator/macerate_flawless_zircon_gem",
        "gtceu:shapeless/gem_to_gem_gem_zircon"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_zircon_exquisite_gem_to_flawless_gem",
        "greate:cutting/integration/gtceu/cutter/cut_zircon_exquisite_gem_to_flawless_gem_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_zircon_exquisite_gem_to_flawless_gem_water",
        "gtceu:shapeless/gem_to_gem_flawless_gem_zircon"
      ],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "ZrSiO₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "greate:cutting",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:flax_bundle",
      "namespace": "tfg",
      "display_name": "Flax Bundle",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:scrapable"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:cutting",
        "vintageimprovements:centrifugation"
      ],
      "recipe_consumption_by_type": {
        "greate:cutting": 1,
        "vintageimprovements:centrifugation": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/tfg/flax_line_from_bundle_in_cutter",
        "tfg:vi_seperate_flax_from_bundle"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/tfg_flax_bundle"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "greate:cutting",
            "vintageimprovements:centrifugation"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:flax_line",
      "namespace": "tfg",
      "display_name": "Flax Line Fibers",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:compost_browns_low"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:coiling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "greate:mixing": 3,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:coiling": 1
      },
      "recipe_production_by_type": {
        "greate:cutting": 2,
        "vintageimprovements:centrifugation": 2
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/tfg/compost_0",
        "greate:mixing/integration/tfg/compost_1",
        "greate:mixing/integration/tfg/compost_2",
        "tfc:kjs/damage/shapeless/linen_thread",
        "tfg:shaped/universal_compost_browns_from_low",
        "tfg:vi_spin_flax_line"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/tfg/flax_line_from_bundle_in_cutter",
        "greate:cutting/integration/tfg/flax_line_in_cutter",
        "tfg:vi_seperate_flax",
        "tfg:vi_seperate_flax_from_bundle"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "greate:mixing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:coiling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:flax_product",
      "namespace": "tfg",
      "display_name": "Flax Stems",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:compost_greens_high",
        "tfc:scrapable"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:cutting",
        "greate:mixing",
        "vintageimprovements:centrifugation"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "greate:cutting": 1,
        "greate:mixing": 3,
        "vintageimprovements:centrifugation": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 7,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/tfg/flax_line_in_cutter",
        "greate:mixing/integration/tfg/compost_2",
        "greate:mixing/integration/tfg/compost_5",
        "greate:mixing/integration/tfg/compost_8",
        "minecraft:kjs/tfg_flax_bundle",
        "tfg:shaped/universal_compost_greens_from_high",
        "tfg:vi_seperate_flax"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "greate:cutting",
            "greate:mixing",
            "vintageimprovements:centrifugation"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:flax_seeds",
      "namespace": "tfg",
      "display_name": "Flax Seeds",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:seeds",
        "sns:allowed_in_seed_pouch",
        "tfc:chicken_food",
        "tfc:duck_food",
        "tfc:quail_food",
        "tfc:seeds",
        "tfc:small_fishing_bait",
        "wan_ancient_beasts:snatcher_steals"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "tfc:advanced_shapeless_crafting",
        "vintageimprovements:vacuumizing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 16,
        "tfc:advanced_shapeless_crafting": 1,
        "vintageimprovements:vacuumizing": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 19,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "firmalife:crafting/seed_ball",
        "tfc:crafting/add_small_bait",
        "tfg:shapeless/alfisol_grass_bonemeal",
        "tfg:shapeless/alfisol_grass_fertilizer",
        "tfg:shapeless/loam_grass_bonemeal",
        "tfg:shapeless/loam_grass_fertilizer",
        "tfg:shapeless/mollisol_grass_bonemeal",
        "tfg:shapeless/mollisol_grass_fertilizer",
        "tfg:shapeless/oxisol_grass_bonemeal",
        "tfg:shapeless/oxisol_grass_fertilizer",
        "tfg:shapeless/podzol_grass_bonemeal",
        "tfg:shapeless/podzol_grass_fertilizer",
        "tfg:shapeless/sandy_loam_grass_bonemeal",
        "tfg:shapeless/sandy_loam_grass_fertilizer",
        "tfg:shapeless/silt_grass_bonemeal",
        "tfg:shapeless/silt_grass_fertilizer",
        "tfg:shapeless/silty_loam_grass_bonemeal",
        "tfg:shapeless/silty_loam_grass_fertilizer",
        "tfg:vi/vacuumizing/seed_oil"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:flax",
        "block_tags": [
          "ad_astra:destroyed_in_space",
          "chalk:chalk_cannot_draw_on",
          "computercraft:turtle_hoe_harvestable",
          "cucumber:mineable/sickle",
          "minecraft:flowers",
          "minecraft:maintains_farmland",
          "minecraft:mineable/hoe",
          "quark:simple_harvest_blacklisted",
          "tfc:crops",
          "tfc:mineable_with_knife",
          "tfc:mineable_with_scythe",
          "tfc:mineable_with_sharp_tool",
          "tfcastikorcarts:mineable_plow_hoe",
          "tfcastikorcarts:mineable_plow_knife",
          "tfcastikorcarts:mineable_plow_scythe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "[Hold-Shift]"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Plantable in a Large Planter"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
          "value": "seed",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _seeds"
        },
        "processing_in": {
          "values": [
            "crafting",
            "tfc:advanced_shapeless_crafting",
            "vintageimprovements:vacuumizing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:flax_tow",
      "namespace": "tfg",
      "display_name": "Flax Tow Fibers",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:compost_browns",
        "tfg:burlap_fiber"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "tfc:damage_inputs_shaped_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 52,
        "crafting_shapeless": 1,
        "greate:mixing": 3,
        "tfc:damage_inputs_shaped_crafting": 1
      },
      "recipe_production_by_type": {
        "greate:cutting": 2
      },
      "recipe_ingredient_count": 57,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "firmaciv:crafting/rope_coil",
        "greate:mixing/integration/tfg/compost_3",
        "greate:mixing/integration/tfg/compost_4",
        "greate:mixing/integration/tfg/compost_5",
        "sns:crafting/leather_sack",
        "sns:shaped/reinforced_fiber",
        "tfc:crafting/bismuth_bronze_horse_armor",
        "tfc:crafting/black_bronze_horse_armor",
        "tfc:crafting/black_steel_horse_armor",
        "tfc:crafting/blue_steel_horse_armor",
        "tfc:crafting/bronze_horse_armor",
        "tfc:crafting/copper_horse_armor",
        "tfc:crafting/jute_net",
        "tfc:crafting/lead",
        "tfc:crafting/red_steel_horse_armor",
        "tfc:crafting/steel_horse_armor",
        "tfc:crafting/wrought_iron_horse_armor",
        "tfg:shaped/acacia_rope_ladder",
        "tfg:shaped/aeronos_rope_ladder",
        "tfg:shaped/araucaria_rope_ladder",
        "tfg:shaped/ash_rope_ladder",
        "tfg:shaped/aspen_rope_ladder",
        "tfg:shaped/baobab_rope_ladder",
        "tfg:shaped/beech_rope_ladder",
        "tfg:shaped/birch_rope_ladder",
        "tfg:shaped/blackwood_rope_ladder",
        "tfg:shaped/chestnut_rope_ladder",
        "tfg:shaped/crimson_rope_ladder",
        "tfg:shaped/cypress_rope_ladder",
        "tfg:shaped/douglas_fir_rope_ladder",
        "tfg:shaped/eucalyptus_rope_ladder",
        "tfg:shaped/fig_rope_ladder",
        "tfg:shaped/ginkgo_rope_ladder",
        "tfg:shaped/glacian_rope_ladder",
        "tfg:shaped/hevea_rope_ladder",
        "tfg:shaped/hickory_rope_ladder",
        "tfg:shaped/ipe_rope_ladder",
        "tfg:shaped/ironwood_rope_ladder",
        "tfg:shaped/kapok_rope_ladder",
        "tfg:shaped/mahoe_rope_ladder",
        "tfg:shaped/mahogany_rope_ladder",
        "tfg:shaped/mangrove_rope_ladder",
        "tfg:shaped/maple_rope_ladder",
        "tfg:shaped/oak_rope_ladder",
        "tfg:shaped/palm_rope_ladder",
        "tfg:shaped/pine_rope_ladder",
        "tfg:shaped/rosewood_rope_ladder",
        "tfg:shaped/sequoia_rope_ladder",
        "tfg:shaped/spruce_rope_ladder",
        "tfg:shaped/strophar_rope_ladder",
        "tfg:shaped/sycamore_rope_ladder",
        "tfg:shaped/teak_rope_ladder",
        "tfg:shaped/tualang_rope_ladder",
        "tfg:shaped/universal_compost_browns_from_medium",
        "tfg:shaped/warped_rope_ladder",
        "tfg:shaped/white_cedar_rope_ladder",
        "tfg:shaped/willow_rope_ladder"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/tfg/flax_tow_from_bundle_in_cutter",
        "greate:cutting/integration/tfg/flax_tow_in_cutter"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "greate:mixing",
            "tfc:damage_inputs_shaped_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:flax_waste",
      "namespace": "tfg",
      "display_name": "Scraped Flax",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:scrapable"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:cutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:cutting": 1
      },
      "recipe_production_by_type": {
        "tfc:scraping": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/tfg/flax_tow_in_cutter",
        "minecraft:kjs/tfg_bundled_scraped_flax"
      ],
      "recipe_output_examples": [
        "tfg:scraping/flax_line"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "greate:cutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:flax_wild",
      "namespace": "tfg",
      "display_name": "Wild Flax",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers",
        "tfc:wild_crops"
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
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:flax_wild",
        "block_tags": [
          "ad_astra:destroyed_in_space",
          "chalk:chalk_cannot_draw_on",
          "computercraft:turtle_hoe_harvestable",
          "cucumber:mineable/sickle",
          "minecraft:flowers",
          "minecraft:mineable/hoe",
          "tfc:can_be_snow_piled",
          "tfc:wild_crops",
          "tfccanes:not_slowed_with_cane",
          "tfg:not_slowed_with_snowshoes"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
          "value": "hoe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/hoe"
        }
      }
    },
    {
      "id": "tfg:fletching",
      "namespace": "tfg",
      "display_name": "Fletching",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2
      },
      "recipe_production_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 3
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "tfg:shaped/arrow",
        "tfg:shaped/arrow_shard"
      ],
      "recipe_output_examples": [
        "tfc:kjs/damage/shapeless/fletching",
        "tfc:kjs/damage/shapeless/glider_feather_fletching",
        "tfc:kjs/damage/shapeless/wraptor_feather_fletching"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:flibe_bucket",
      "namespace": "tfg",
      "display_name": "FLiBe Bucket",
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
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "FLiBe"
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
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:flint_arrow_head",
      "namespace": "tfg",
      "display_name": "Flint Arrow Head",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "greate:cutting": 1,
        "tfc:knapping": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "tfg:shaped/arrow"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/tfg/assembler/flint_arrow_head",
        "tfg:knapping/flint_arrow_head"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:flint_club_head",
      "namespace": "tfg",
      "display_name": "Flint Club Head",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "tfc:knapping": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "tfg:shapeless/flint_club"
      ],
      "recipe_output_examples": [
        "tfg:knapping/flint_club_head"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:flintlock_mechanism",
      "namespace": "tfg",
      "display_name": "Flintlock Mechanism",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "tfg_tacz:flintlock_pistol",
        "tfg_tacz:trapdoor_rifle"
      ],
      "recipe_output_examples": [
        "tfg:shaped/flintlock_mechanism_iron",
        "tfg:shaped/flintlock_mechanism_steel"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:flippers",
      "namespace": "tfg",
      "display_name": "Flippers",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "curios:clothes_socks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:shaped/flippers_leather",
        "tfg:shaped/flippers_rubber"
      ],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Clothes (Feet)"
        },
        {
          "source": "runtime-tooltip",
          "text": "Improves agility in water"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "tfg:flora_pellets",
      "namespace": "tfg",
      "display_name": "Flora Pellets",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:blaze_burner_fuel/regular",
        "tfc:compost_greens_low",
        "tfg:advanced_fish_food"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:deploying",
        "greate:mixing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "create:deploying": 80,
        "greate:mixing": 83
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 164,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/tfg/andesite_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/basalt_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/basalt_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/blackstone_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/brick_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/chalk_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/chert_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/claystone_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/compost_0",
        "greate:mixing/integration/tfg/compost_3",
        "greate:mixing/integration/tfg/conglomerate_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/crackrack_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/dacite_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/dacite_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/dark_concrete_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/deepslate_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/deepslate_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/diorite_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/dolomite_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/dripstone_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/dusk_brick_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/gabbro_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/glacio_stone_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/gneiss_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/granite_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/light_concrete_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/limestone_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/limestone_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/marble_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/mars_stone_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/mercury_stone_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/moon_deepslate_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/moon_stone_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/pearl_brick_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/permafrost_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/phyllite_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/quartzite_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/quartzite_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/red_granite_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/rhyolite_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/schist_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/schist_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/shale_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/slate_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/stone_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/tuff_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/umber_brick_bricks_to_mossy_bricks",
        "greate:mixing/integration/tfg/venus_stone_cobble_to_mossy_cobble",
        "greate:mixing/integration/tfg/verdant_brick_bricks_to_mossy_bricks",
        "tfg:deploying/andesite_cobble_to_mossy_cobble",
        "tfg:deploying/basalt_cobble_to_mossy_cobble",
        "tfg:deploying/blackstone_cobble_to_mossy_cobble",
        "tfg:deploying/blue_brick_bricks_to_mossy_bricks",
        "tfg:deploying/chalk_bricks_to_mossy_bricks",
        "tfg:deploying/chert_bricks_to_mossy_bricks",
        "tfg:deploying/chert_cobble_to_mossy_cobble",
        "tfg:deploying/claystone_cobble_to_mossy_cobble",
        "tfg:deploying/conglomerate_cobble_to_mossy_cobble",
        "tfg:deploying/crackrack_cobble_to_mossy_cobble",
        "tfg:deploying/dacite_bricks_to_mossy_bricks",
        "tfg:deploying/dark_concrete_bricks_to_mossy_bricks",
        "tfg:deploying/dean_brick_bricks_to_mossy_bricks",
        "tfg:deploying/deepslate_bricks_to_mossy_bricks",
        "tfg:deploying/diorite_bricks_to_mossy_bricks",
        "tfg:deploying/dolomite_bricks_to_mossy_bricks",
        "tfg:deploying/dripstone_bricks_to_mossy_bricks",
        "tfg:deploying/dripstone_cobble_to_mossy_cobble",
        "tfg:deploying/gabbro_bricks_to_mossy_bricks",
        "tfg:deploying/glacio_stone_bricks_to_mossy_bricks",
        "tfg:deploying/glacio_stone_cobble_to_mossy_cobble",
        "tfg:deploying/gneiss_cobble_to_mossy_cobble",
        "tfg:deploying/granite_cobble_to_mossy_cobble",
        "tfg:deploying/light_concrete_cobble_to_mossy_cobble",
        "tfg:deploying/limestone_bricks_to_mossy_bricks",
        "tfg:deploying/marble_bricks_to_mossy_bricks",
        "tfg:deploying/mars_stone_bricks_to_mossy_bricks",
        "tfg:deploying/mars_stone_cobble_to_mossy_cobble",
        "tfg:deploying/mercury_stone_cobble_to_mossy_cobble",
        "tfg:deploying/moon_deepslate_cobble_to_mossy_cobble",
        "tfg:deploying/moon_stone_cobble_to_mossy_cobble",
        "tfg:deploying/pearl_brick_bricks_to_mossy_bricks",
        "tfg:deploying/permafrost_cobble_to_mossy_cobble",
        "tfg:deploying/phyllite_cobble_to_mossy_cobble",
        "tfg:deploying/quartzite_bricks_to_mossy_bricks",
        "tfg:deploying/red_granite_bricks_to_mossy_bricks",
        "tfg:deploying/rhyolite_bricks_to_mossy_bricks",
        "tfg:deploying/scarlet_brick_bricks_to_mossy_bricks",
        "tfg:deploying/schist_bricks_to_mossy_bricks",
        "tfg:deploying/shale_bricks_to_mossy_bricks",
        "tfg:deploying/slate_bricks_to_mossy_bricks",
        "tfg:deploying/slate_cobble_to_mossy_cobble",
        "tfg:deploying/stone_cobble_to_mossy_cobble",
        "tfg:deploying/tuff_cobble_to_mossy_cobble",
        "tfg:deploying/venus_stone_bricks_to_mossy_bricks",
        "tfg:deploying/venus_stone_cobble_to_mossy_cobble",
        "tfg:shaped/universal_compost_greens_from_low"
      ],
      "recipe_output_examples": [],
      "recipe_examples_truncated": true,
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
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
          "text": "§7Burns at §fWhite§7 for §f1:54"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "create:deploying",
            "greate:mixing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:fluix_bucket",
      "namespace": "tfg",
      "display_name": "Liquid Fluix Bucket",
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
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "(SiO₂)(SiO₂)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 1,200 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:foil_pack",
      "namespace": "tfg",
      "display_name": "Foil Pack",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:foil_packs"
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
        "kubejs:tab"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:food/brioche_bun",
      "namespace": "tfg",
      "display_name": "Brioche Bun",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "sns:prevented_in_burlap_sack",
        "sns:prevented_in_leather_sack",
        "sns:prevented_in_ore_sack",
        "sns:prevented_in_seed_pouch",
        "sns:prevented_in_straw_basket",
        "tfc:foods",
        "tfc:pig_food"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:advanced_shaped_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:advanced_shaped_crafting": 2
      },
      "recipe_production_by_type": {
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "tfg:crafting/cheeseburger",
        "tfg:crafting/hamburger"
      ],
      "recipe_output_examples": [
        "tfc:kjs/c0imnb8yy4d60tmrjoeitkffd"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "0.5 / 16.0g."
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Expires on: 3:59 July 8, 1000 (in 1 month(s) and 6 day(s))"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold (Shift) for Nutrition Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:food": {
          "nutrition": 4,
          "saturation_modifier": 2,
          "is_meat": false,
          "can_always_eat": false,
          "is_fast_food": false,
          "effects": []
        },
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
            "tfc:advanced_shaped_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:food/brioche_dough",
      "namespace": "tfg",
      "display_name": "Brioche Dough",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "sns:prevented_in_burlap_sack",
        "sns:prevented_in_leather_sack",
        "sns:prevented_in_ore_sack",
        "sns:prevented_in_seed_pouch",
        "sns:prevented_in_straw_basket",
        "tfc:foods",
        "tfc:pig_food"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "firmalife:mixing_bowl": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:mixing_bowl/brioche_dough"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "0.5 / 16.0g."
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Expires on: 1:33 July 3, 1000 (in 1 month(s) and 1 day(s))"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold (Shift) for Nutrition Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:food": {
          "nutrition": 2,
          "saturation_modifier": 1,
          "is_meat": false,
          "can_always_eat": false,
          "is_fast_food": false,
          "effects": []
        },
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
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