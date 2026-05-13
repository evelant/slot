# Items to classify
{
  "items": [
    {
      "id": "gtceu:treated_wood_trapdoor",
      "namespace": "gtceu",
      "display_name": "Treated Wood Trapdoor",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:trapdoors",
        "minecraft:wooden_trapdoors"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "tfg:create/shapeless/framed_glass_trapdoor",
        "tfg:immersive_aircraft/shaped/sail",
        "tfg:shapeless/create_train_trapdoor"
      ],
      "recipe_output_examples": [
        "tfg:shaped/treated_wood_trapdoor_from_lumber"
      ],
      "model_parents": [
        "item/treated_wood_trapdoor",
        "block/template_orientable_trapdoor_bottom"
      ],
      "creative_tabs": [
        "gtceu:decoration"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "gtceu:blocks/treated_wood_trapdoor"
      ],
      "block_context": {
        "block_id": "gtceu:treated_wood_trapdoor",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe",
          "minecraft:trapdoors",
          "minecraft:wooden_trapdoors",
          "tacz:interact_key/whitelist"
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
          "value": "trapdoor",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:trapdoors"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
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
      "id": "gtceu:tricalcium_phosphate_dust",
      "namespace": "gtceu",
      "display_name": "Tricalcium Phosphate Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/tricalcium_phosphate"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:mixing": 30,
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
      "recipe_ingredient_count": 35,
      "recipe_output_count": 101,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/pill_antipoison",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/pill_haste",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/pill_night_vision",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/pill_regeneration",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/pill_speed",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/pill_water_breathing",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/tablet_antipoison",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/tablet_haste",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/tablet_night_vision",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/tablet_regeneration",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/tablet_speed",
        "greate:mixing/integration/tfg/gtceu/mixer/distilled_water/tablet_water_breathing",
        "greate:mixing/integration/tfg/gtceu/mixer/salvo_absorption",
        "greate:mixing/integration/tfg/gtceu/mixer/salvo_fire_resistance",
        "greate:mixing/integration/tfg/gtceu/mixer/salvo_instant_health",
        "greate:mixing/integration/tfg/gtceu/mixer/salvo_invisibility",
        "greate:mixing/integration/tfg/gtceu/mixer/salvo_luck",
        "greate:mixing/integration/tfg/gtceu/mixer/salvo_resistance",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/pill_antipoison",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/pill_haste",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/pill_night_vision",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/pill_regeneration",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/pill_speed",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/pill_water_breathing",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/tablet_antipoison",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/tablet_haste",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/tablet_night_vision",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/tablet_regeneration",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/tablet_speed",
        "greate:mixing/integration/tfg/gtceu/mixer/spring_water/tablet_water_breathing",
        "gtceu:shaped/small_dust_disassembling_3x3_tricalcium_phosphate",
        "gtceu:shaped/small_dust_disassembling_tricalcium_phosphate",
        "gtceu:shaped/tiny_dust_disassembling_3x3_tricalcium_phosphate",
        "gtceu:shaped/tiny_dust_disassembling_tricalcium_phosphate",
        "tfg:shapeless/tricalcium_phosphate_matches"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerate_tricalcium_phosphate_refined_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_tricalcium_phosphate_refined_ore_to_dust",
        "greate:pressing/refined_tricalcium_phosphate_to_dust",
        "gtceu:blasting/smelt_andesite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_basalt_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_black_sand_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_brown_sand_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_chalk_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_chert_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_claystone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_dacite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_deepslate_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_diorite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_dolomite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_dripstone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_flavolite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_gabbro_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_glacio_stone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_gneiss_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_granite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_green_sand_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_limestone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_marble_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_mars_stone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_mercury_stone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_moon_deepslate_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_moon_stone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_phyllite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_pink_sand_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_quartzite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_raw_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_red_granite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_red_sand_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_rhyolite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_sandy_jadestone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_schist_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_shale_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_slate_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_tuff_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_venus_stone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_white_sand_tricalcium_phosphate_ore_to_ingot",
        "gtceu:blasting/smelt_yellow_sand_tricalcium_phosphate_ore_to_ingot",
        "gtceu:shaped/small_dust_assembling_tricalcium_phosphate",
        "gtceu:shaped/tiny_dust_assembling_tricalcium_phosphate",
        "gtceu:shapeless/centrifuged_ore_to_dust_tricalcium_phosphate",
        "gtceu:smelting/smelt_andesite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_basalt_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_brown_sand_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_chalk_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_chert_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_claystone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_conglomerate_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_dacite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_deepslate_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_diorite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_dolomite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_dripstone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_flavolite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_gabbro_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_glacio_stone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_gneiss_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_granite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_limestone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_marble_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_mars_stone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_moon_deepslate_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_moon_stone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_phyllite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_pink_sand_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_pyroxenite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_quartzite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_raw_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_red_granite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_red_sand_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_rhyolite_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_rich_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_sandy_jadestone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_schist_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_shale_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_slate_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_tuff_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_venus_stone_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_white_sand_tricalcium_phosphate_ore_to_ingot",
        "tfg:ae_transform/tricalcium_phosphate_dust_from_impure",
        "tfg:ae_transform/tricalcium_phosphate_dust_from_pure",
        "tfg:instant_barrel/tricalcium_phosphate_dust_from_impure",
        "tfg:instant_barrel/tricalcium_phosphate_dust_from_pure",
        "tfg:splashing/tricalcium_phosphate_dust_from_impure_distilled",
        "tfg:splashing/tricalcium_phosphate_dust_from_impure_water",
        "tfg:splashing/tricalcium_phosphate_dust_from_pure_distilled",
        "tfg:splashing/tricalcium_phosphate_dust_from_pure_water",
        "tfg:vi/centrifuge/tricalcium_phosphate_dust_from_impure",
        "tfg:vi/centrifuge/tricalcium_phosphate_dust_from_pure"
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
          "text": "Ca₃(PO₄)₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§6(P) Phosphorus: §r15.0%"
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
            "crafting",
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
      "id": "gtceu:tricalcium_phosphate_indicator",
      "namespace": "gtceu",
      "display_name": "Tricalcium Phosphate Surface Rock",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:surface_rocks",
        "forge:surface_rocks/tricalcium_phosphate"
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
        "block_id": "gtceu:tricalcium_phosphate_indicator",
        "block_tags": [
          "forge:surface_rocks",
          "forge:surface_rocks/tricalcium_phosphate",
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
          "text": "Ca₃(PO₄)₂"
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
      "id": "gtceu:tricalcium_phosphate_ore",
      "namespace": "gtceu",
      "display_name": "Tricalcium Phosphate Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/tricalcium_phosphate",
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
        "greate:milling/integration/gtceu/macerator/macerate_tricalcium_phosphate_ore_to_crushed_ore",
        "gtceu:blasting/smelt_tricalcium_phosphate_ore_to_ingot",
        "gtceu:smelting/smelt_tricalcium_phosphate_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:tricalcium_phosphate_ore",
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ca₃(PO₄)₂"
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
          "value": "tricalcium_phosphate",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id tricalcium_phosphate_ore"
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
      "id": "gtceu:trinium_block",
      "namespace": "gtceu",
      "display_name": "Block of Trinium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:storage_blocks",
        "forge:storage_blocks/trinium",
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
        "greate:cutting/integration/gtceu/cutter/cut_trinium_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_trinium_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_trinium_block_to_plate_water",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/trinium_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:trinium_block",
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
          "forge:needs_neutronium_tool",
          "forge:storage_blocks",
          "forge:storage_blocks/trinium",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ke"
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
      "id": "gtceu:trinium_bolt",
      "namespace": "gtceu",
      "display_name": "Trinium Bolt",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:bolts",
        "forge:bolts/trinium"
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
        "greate:cutting": 6
      },
      "recipe_ingredient_count": 23,
      "recipe_output_count": 7,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_trinium_bolt",
        "gtceu:shaped/screw_trinium",
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
        "tfg:vi/lathe/trinium_bolt_to_screw"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_trinium_rod_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_trinium_rod_to_bolt_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_trinium_rod_to_bolt_water",
        "greate:cutting/integration/gtceu/cutter/cut_trinium_screw_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_trinium_screw_to_bolt_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_trinium_screw_to_bolt_water",
        "gtceu:shaped/bolt_saw_trinium"
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
          "text": "Ke"
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
      "id": "gtceu:trinium_bucket",
      "namespace": "gtceu",
      "display_name": "Liquid Trinium Bucket",
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
          "text": "Ke"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 7,200 K"
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
      "id": "gtceu:trinium_coil_block",
      "namespace": "gtceu",
      "display_name": "Trinium Coil Block",
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
        "greate:milling/integration/gtceu/macerator/macerate_trinium_coil_block"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/trinium_coil_block",
        "block/trinium_coil_block",
        "block/cube_all"
      ],
      "creative_tabs": [
        "gtceu:decoration"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "gtceu:blocks/trinium_coil_block"
      ],
      "block_context": {
        "block_id": "gtceu:trinium_coil_block",
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
      "id": "gtceu:trinium_double_cable",
      "namespace": "gtceu",
      "display_name": "2x Trinium Cable",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:double_cables",
        "forge:double_cables/trinium"
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
        "greate:milling/integration/gtceu/macerator/macerate_trinium_double_cable"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:trinium_double_cable",
        "block_tags": [
          "forge:double_cables",
          "forge:double_cables/trinium",
          "forge:mineable/wire_cutter",
          "forge:needs_neutronium_tool",
          "gtceu:mineable/pickaxe_or_wire_cutter"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ke"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a131,072 §a(§cZPM§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e12"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c4§7 EU-Volt"
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
      "id": "gtceu:trinium_double_wire",
      "namespace": "gtceu",
      "display_name": "2x Trinium Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:double_wires",
        "forge:double_wires/trinium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 3,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 2
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_trinium_double_wire",
        "gtceu:shapeless/trinium_wire_wire_gt_double_doubling",
        "gtceu:shapeless/trinium_wire_wire_gt_double_quadrupling",
        "gtceu:shapeless/trinium_wire_wire_gt_double_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/trinium_wire_wire_gt_quadruple_splitting",
        "gtceu:shapeless/trinium_wire_wire_gt_single_doubling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:trinium_double_wire",
        "block_tags": [
          "forge:double_wires",
          "forge:double_wires/trinium",
          "forge:mineable/wire_cutter",
          "forge:needs_neutronium_tool",
          "gtceu:mineable/pickaxe_or_wire_cutter"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ke"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a131,072 §a(§cZPM§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e12"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c8§7 EU-Volt"
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
      "id": "gtceu:trinium_dust",
      "namespace": "gtceu",
      "display_name": "Trinium Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/trinium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:mixing",
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "greate:mixing": 3,
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "greate:milling": 25
      },
      "recipe_ingredient_count": 7,
      "recipe_output_count": 27,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/enriched_naquadah_trinium_europium_duranide",
        "greate:mixing/integration/gtceu/mixer/naquadah_alloy",
        "greate:mixing/integration/gtceu/mixer/ruthenium_trinium_americium_neutronate",
        "gtceu:shaped/small_dust_disassembling_3x3_trinium",
        "gtceu:shaped/small_dust_disassembling_trinium",
        "gtceu:shaped/tiny_dust_disassembling_3x3_trinium",
        "gtceu:shaped/tiny_dust_disassembling_trinium"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_double_trinium_plate",
        "greate:milling/integration/gtceu/macerator/macerate_hpca_bridge_component",
        "greate:milling/integration/gtceu/macerator/macerate_long_trinium_rod",
        "greate:milling/integration/gtceu/macerator/macerate_mega_blast_furnace",
        "greate:milling/integration/gtceu/macerator/macerate_mega_vacuum_freezer",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_block",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_coil_block",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_double_wire",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_gear",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_hex_cable",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_hex_wire",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_ingot",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_octal_cable",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_octal_wire",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_plate",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_quadruple_cable",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_quadruple_wire",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_spring",
        "greate:milling/integration/gtceu/macerator/macerate_uhv_quantum_chest",
        "greate:milling/integration/gtceu/macerator/macerate_uhv_quantum_tank",
        "greate:milling/integration/gtceu/macerator/macerate_zpm_emitter",
        "greate:milling/integration/gtceu/macerator/macerate_zpm_field_generator",
        "greate:milling/integration/gtceu/macerator/macerate_zpm_rotor_holder",
        "greate:milling/integration/gtceu/macerator/macerate_zpm_scanner",
        "greate:milling/integration/gtceu/macerator/macerate_zpm_sensor",
        "gtceu:shaped/small_dust_assembling_trinium",
        "gtceu:shaped/tiny_dust_assembling_trinium"
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
          "text": "Ke"
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
      "id": "gtceu:trinium_foil",
      "namespace": "gtceu",
      "display_name": "Trinium Foil",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:foils",
        "forge:foils/trinium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "createaddition:rolling": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_trinium_foil"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/foil_trinium",
        "tfg:rolling/trinium_foil"
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
          "text": "Ke"
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
      "id": "gtceu:trinium_gear",
      "namespace": "gtceu",
      "display_name": "Trinium Gear",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gears",
        "forge:gears/trinium"
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
        "greate:milling/integration/gtceu/macerator/macerate_trinium_gear"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/gear_trinium"
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
          "text": "Ke"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Large"
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:trinium_hex_cable",
      "namespace": "gtceu",
      "display_name": "16x Trinium Cable",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:hex_cables",
        "forge:hex_cables/trinium"
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
        "greate:milling/integration/gtceu/macerator/macerate_trinium_hex_cable"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:trinium_hex_cable",
        "block_tags": [
          "forge:hex_cables",
          "forge:hex_cables/trinium",
          "forge:mineable/wire_cutter",
          "forge:needs_neutronium_tool",
          "gtceu:mineable/pickaxe_or_wire_cutter"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ke"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a131,072 §a(§cZPM§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e96"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c4§7 EU-Volt"
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
      "id": "gtceu:trinium_hex_wire",
      "namespace": "gtceu",
      "display_name": "16x Trinium Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:hex_wires",
        "forge:hex_wires/trinium"
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
        "crafting_shapeless": 2
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_trinium_hex_wire",
        "gtceu:shapeless/trinium_wire_wire_gt_hex_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/trinium_wire_wire_gt_octal_doubling",
        "gtceu:shapeless/trinium_wire_wire_gt_quadruple_quadrupling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:trinium_hex_wire",
        "block_tags": [
          "forge:hex_wires",
          "forge:hex_wires/trinium",
          "forge:mineable/wire_cutter",
          "forge:needs_neutronium_tool",
          "gtceu:mineable/pickaxe_or_wire_cutter"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ke"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a131,072 §a(§cZPM§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e96"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c12§7 EU-Volt"
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
      "id": "gtceu:trinium_ingot",
      "namespace": "gtceu",
      "display_name": "Trinium Ingot",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ingots",
        "forge:ingots",
        "forge:ingots/trinium",
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
        "greate:compacting/trinium_block",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_ingot",
        "gtceu:shaped/stick_trinium",
        "tfg:rolling/trinium_plate",
        "tfg:vi/coiling/trinium_single_wire",
        "tfg:vi/lathe/trinium_to_rod"
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
          "text": "Ke"
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
      "id": "gtceu:trinium_nugget",
      "namespace": "gtceu",
      "display_name": "Trinium Nugget",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:nuggets",
        "forge:nuggets",
        "forge:nuggets/trinium"
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
        "greate:milling/integration/gtceu/macerator/macerate_trinium_nugget"
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
          "text": "Ke"
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
      "id": "gtceu:trinium_octal_cable",
      "namespace": "gtceu",
      "display_name": "8x Trinium Cable",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:octal_cables",
        "forge:octal_cables/trinium"
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
        "greate:milling/integration/gtceu/macerator/macerate_trinium_octal_cable"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:trinium_octal_cable",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:needs_neutronium_tool",
          "forge:octal_cables",
          "forge:octal_cables/trinium",
          "gtceu:mineable/pickaxe_or_wire_cutter"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ke"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a131,072 §a(§cZPM§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e48"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c4§7 EU-Volt"
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
      "id": "gtceu:trinium_octal_wire",
      "namespace": "gtceu",
      "display_name": "8x Trinium Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:octal_wires",
        "forge:octal_wires/trinium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 2,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 3
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_trinium_octal_wire",
        "gtceu:shapeless/trinium_wire_wire_gt_octal_doubling",
        "gtceu:shapeless/trinium_wire_wire_gt_octal_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/trinium_wire_wire_gt_double_quadrupling",
        "gtceu:shapeless/trinium_wire_wire_gt_hex_splitting",
        "gtceu:shapeless/trinium_wire_wire_gt_quadruple_doubling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:trinium_octal_wire",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:needs_neutronium_tool",
          "forge:octal_wires",
          "forge:octal_wires/trinium",
          "gtceu:mineable/pickaxe_or_wire_cutter"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ke"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a131,072 §a(§cZPM§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e48"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c12§7 EU-Volt"
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
      "id": "gtceu:trinium_plate",
      "namespace": "gtceu",
      "display_name": "Trinium Plate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:plates",
        "forge:plates/trinium",
        "forge:sheets/trinium",
        "tfc:pileable_sheets"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "createaddition:rolling",
        "greate:compacting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 3,
        "createaddition:rolling": 1,
        "greate:compacting": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "createaddition:rolling": 1,
        "greate:cutting": 3
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_trinium_plate",
        "gtceu:shaped/foil_trinium",
        "gtceu:shaped/gear_trinium",
        "gtceu:shaped/trinium_wire_single",
        "tfg:compacting/trinium_doublePlate",
        "tfg:rolling/trinium_foil"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_trinium_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_trinium_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_trinium_block_to_plate_water",
        "tfg:rolling/trinium_plate"
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
          "text": "Ke"
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
            "createaddition:rolling",
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
      "id": "gtceu:trinium_quadruple_cable",
      "namespace": "gtceu",
      "display_name": "4x Trinium Cable",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:quadruple_cables",
        "forge:quadruple_cables/trinium"
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
        "greate:milling/integration/gtceu/macerator/macerate_trinium_quadruple_cable"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:trinium_quadruple_cable",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:needs_neutronium_tool",
          "forge:quadruple_cables",
          "forge:quadruple_cables/trinium",
          "gtceu:mineable/pickaxe_or_wire_cutter"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ke"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a131,072 §a(§cZPM§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e24"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c4§7 EU-Volt"
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
      "id": "gtceu:trinium_quadruple_wire",
      "namespace": "gtceu",
      "display_name": "4x Trinium Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:quadruple_wires",
        "forge:quadruple_wires/trinium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 3,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 3
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_trinium_quadruple_wire",
        "gtceu:shapeless/trinium_wire_wire_gt_quadruple_doubling",
        "gtceu:shapeless/trinium_wire_wire_gt_quadruple_quadrupling",
        "gtceu:shapeless/trinium_wire_wire_gt_quadruple_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/trinium_wire_wire_gt_double_doubling",
        "gtceu:shapeless/trinium_wire_wire_gt_octal_splitting",
        "gtceu:shapeless/trinium_wire_wire_gt_single_quadrupling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:trinium_quadruple_wire",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:needs_neutronium_tool",
          "forge:quadruple_wires",
          "forge:quadruple_wires/trinium",
          "gtceu:mineable/pickaxe_or_wire_cutter"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ke"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a131,072 §a(§cZPM§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e24"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c12§7 EU-Volt"
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
      "id": "gtceu:trinium_rod",
      "namespace": "gtceu",
      "display_name": "Trinium Rod",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:rods",
        "forge:rods/trinium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:compacting",
        "greate:cutting",
        "greate:milling",
        "tfc:advanced_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 6,
        "greate:compacting": 1,
        "greate:cutting": 3,
        "greate:milling": 1,
        "tfc:advanced_shapeless_crafting": 208
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "greate:cutting": 3,
        "vintageimprovements:polishing": 1
      },
      "recipe_ingredient_count": 219,
      "recipe_output_count": 6,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_trinium_rod_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_trinium_rod_to_bolt_water",
        "gtceu:shaped/axe_black_bronze",
        "gtceu:shaped/axe_blue_steel",
        "gtceu:shaped/axe_copper",
        "gtceu:shaped/axe_red_steel",
        "gtceu:shaped/axe_wrought_iron",
        "gtceu:shaped/butchery_knife_bismuth_bronze",
        "gtceu:shaped/butchery_knife_black_steel",
        "gtceu:shaped/butchery_knife_bronze",
        "gtceu:shaped/butchery_knife_diamond_tipped_mo_50_re",
        "gtceu:shaped/butchery_knife_hsse",
        "gtceu:shaped/butchery_knife_ostrum_iodide",
        "gtceu:shaped/butchery_knife_steel",
        "gtceu:shaped/butchery_knife_ultimet",
        "gtceu:shaped/butchery_knife_wrought_iron",
        "gtceu:shaped/file_black_steel",
        "gtceu:shaped/file_boron_carbide",
        "gtceu:shaped/file_copper",
        "gtceu:shaped/file_hsse",
        "gtceu:shaped/file_neutronium",
        "gtceu:shaped/file_red_steel",
        "gtceu:shaped/file_tungsten_carbide",
        "gtceu:shaped/file_wrought_iron",
        "gtceu:shaped/hammer_bismuth_bronze",
        "gtceu:shaped/hammer_black_steel",
        "gtceu:shaped/hammer_bronze",
        "gtceu:shaped/hammer_diamond_tipped_mo_50_re",
        "gtceu:shaped/hammer_hsse",
        "gtceu:shaped/hammer_ostrum_iodide",
        "gtceu:shaped/hammer_steel",
        "gtceu:shaped/hammer_tungsten_carbide",
        "gtceu:shaped/hammer_vanadium_steel",
        "gtceu:shaped/hoe_black_bronze",
        "gtceu:shaped/hoe_blue_steel",
        "gtceu:shaped/hoe_bronze",
        "gtceu:shaped/hoe_duranium",
        "gtceu:shaped/hoe_hsse",
        "gtceu:shaped/hoe_neutronium",
        "gtceu:shaped/hoe_red_steel",
        "gtceu:shaped/hoe_tungsten_carbide",
        "gtceu:shaped/hoe_vanadium_steel",
        "gtceu:shaped/knife_bismuth_bronze",
        "gtceu:shaped/knife_blue_steel",
        "gtceu:shaped/knife_bronze",
        "gtceu:shaped/knife_diamond_tipped_mo_50_re",
        "gtceu:shaped/knife_hsse",
        "gtceu:shaped/knife_neutronium",
        "gtceu:shaped/knife_red_steel",
        "gtceu:shaped/knife_stone",
        "gtceu:shaped/knife_vanadium_steel",
        "gtceu:shaped/mining_hammer_bismuth_bronze",
        "gtceu:shaped/mining_hammer_black_steel",
        "gtceu:shaped/mining_hammer_copper",
        "gtceu:shaped/mining_hammer_steel",
        "gtceu:shaped/pickaxe_bismuth_bronze",
        "gtceu:shaped/pickaxe_blue_steel",
        "gtceu:shaped/pickaxe_copper",
        "gtceu:shaped/pickaxe_red_steel",
        "gtceu:shaped/pickaxe_wrought_iron",
        "gtceu:shaped/plunger_styrene_butadiene_rubber",
        "gtceu:shaped/saw_black_bronze",
        "gtceu:shaped/saw_blue_steel",
        "gtceu:shaped/saw_red_steel",
        "gtceu:shaped/saw_wrought_iron",
        "gtceu:shaped/screwdriver_black_bronze",
        "gtceu:shaped/screwdriver_blue_steel",
        "gtceu:shaped/screwdriver_neutronium",
        "gtceu:shaped/screwdriver_steel",
        "gtceu:shaped/scythe_bismuth_bronze",
        "gtceu:shaped/scythe_blue_steel",
        "gtceu:shaped/scythe_bronze",
        "gtceu:shaped/scythe_diamond_tipped_mo_50_re",
        "gtceu:shaped/scythe_naquadah_alloy",
        "gtceu:shaped/scythe_ostrum_iodide",
        "gtceu:shaped/scythe_steel",
        "gtceu:shaped/scythe_ultimet",
        "gtceu:shaped/shovel_bismuth_bronze",
        "gtceu:shaped/shovel_black_steel",
        "gtceu:shaped/shovel_bronze",
        "gtceu:shaped/shovel_red_steel",
        "gtceu:shaped/shovel_stone",
        "gtceu:shaped/spade_bismuth_bronze",
        "gtceu:shaped/spade_black_steel",
        "gtceu:shaped/spade_copper",
        "gtceu:shaped/spade_steel",
        "gtceu:shaped/sword_bismuth_bronze",
        "gtceu:shaped/sword_blue_steel",
        "gtceu:shaped/sword_bronze",
        "gtceu:shaped/sword_diamond_tipped_mo_50_re",
        "gtceu:shaped/sword_hsse",
        "gtceu:shaped/sword_neutronium",
        "gtceu:shaped/sword_red_steel",
        "gtceu:shaped/sword_tungsten_carbide",
        "gtceu:shaped/sword_wrought_iron",
        "tfg:shaped/snowshoes"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_trinium_long_rod_to_rod",
        "greate:cutting/integration/gtceu/cutter/cut_trinium_long_rod_to_rod_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_trinium_long_rod_to_rod_water",
        "gtceu:shaped/stick_long_trinium",
        "gtceu:shaped/stick_trinium",
        "tfg:vi/lathe/trinium_to_rod"
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
          "text": "Ke"
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
            "greate:cutting",
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
      "id": "gtceu:trinium_screw",
      "namespace": "gtceu",
      "display_name": "Trinium Screw",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:screws",
        "forge:screws/trinium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:cutting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 7,
        "greate:cutting": 3,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "vintageimprovements:polishing": 1
      },
      "recipe_ingredient_count": 11,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_trinium_screw_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_trinium_screw_to_bolt_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_trinium_screw_to_bolt_water",
        "greate:milling/integration/gtceu/macerator/macerate_trinium_screw",
        "hangglider:shaped/glider_framework",
        "tfg:create/shaped/brown_toolbox",
        "tfg:create/shaped/display_link",
        "tfg:create/shaped/turntable",
        "tfg:create/shaped/white_seat",
        "tfg:shaped/trowel",
        "tfg_tacz:trapdoor_scope"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/screw_trinium",
        "tfg:vi/lathe/trinium_bolt_to_screw"
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
          "text": "Ke"
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
      "id": "gtceu:trinium_single_cable",
      "namespace": "gtceu",
      "display_name": "1x Trinium Cable",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:single_cables",
        "forge:single_cables/trinium"
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
        "greate:milling/integration/gtceu/macerator/macerate_trinium_single_cable"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:trinium_single_cable",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:needs_neutronium_tool",
          "forge:single_cables",
          "forge:single_cables/trinium",
          "gtceu:mineable/pickaxe_or_wire_cutter"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ke"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a131,072 §a(§cZPM§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e6"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c4§7 EU-Volt"
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