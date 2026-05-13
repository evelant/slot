# Items to classify
{
  "items": [
    {
      "id": "gtceu:salt_bud_indicator",
      "namespace": "gtceu",
      "display_name": "Halite Surface Bud",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:bud_indicators",
        "forge:bud_indicators/salt"
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
        "minecraft:shapeless/salt_bud_indicator"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:salt_bud_indicator",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "forge:bud_indicators",
          "forge:bud_indicators/salt",
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
          "text": "NaCl"
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
      "id": "gtceu:salt_dust",
      "namespace": "gtceu",
      "display_name": "Salt",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/salt"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:mixing",
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 1,
        "greate:mixing": 4,
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "ae2:transform": 2,
        "crafting_shaped": 2,
        "crafting_shapeless": 1,
        "greate:milling": 6,
        "greate:pressing": 1,
        "greate:splashing": 4,
        "tfc:barrel_instant": 2,
        "tfc:quern": 1,
        "vintageimprovements:centrifugation": 2
      },
      "recipe_ingredient_count": 10,
      "recipe_output_count": 21,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/salt_to_powder",
        "greate:mixing/integration/gtceu/mixer/raw_growth_medium",
        "greate:mixing/integration/gtceu/mixer/salt_water",
        "greate:mixing/integration/tfg/gtceu/salt_water",
        "greate:mixing/integration/tfg/tfc/salt_water",
        "gtceu:shaped/small_dust_disassembling_3x3_salt",
        "gtceu:shaped/small_dust_disassembling_salt",
        "gtceu:shaped/tiny_dust_disassembling_3x3_salt",
        "gtceu:shaped/tiny_dust_disassembling_salt",
        "tfg:mortar/salt"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerate_salt_refined_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_exquisite_salt_gem",
        "greate:milling/integration/gtceu/macerator/macerate_flawless_salt_gem",
        "greate:milling/integration/gtceu/macerator/macerate_salt_block",
        "greate:milling/integration/gtceu/macerator/macerate_salt_gem",
        "greate:milling/integration/gtceu/macerator/macerate_salt_refined_ore_to_dust",
        "greate:pressing/refined_salt_to_dust",
        "gtceu:shaped/small_dust_assembling_salt",
        "gtceu:shaped/tiny_dust_assembling_salt",
        "gtceu:shapeless/centrifuged_ore_to_dust_salt",
        "tfg:ae_transform/salt_dust_from_impure",
        "tfg:ae_transform/salt_dust_from_pure",
        "tfg:instant_barrel/salt_dust_from_impure",
        "tfg:instant_barrel/salt_dust_from_pure",
        "tfg:quern/salt_gem_to_dust",
        "tfg:splashing/salt_dust_from_impure_distilled",
        "tfg:splashing/salt_dust_from_impure_water",
        "tfg:splashing/salt_dust_from_pure_distilled",
        "tfg:splashing/salt_dust_from_pure_water",
        "tfg:vi/centrifuge/salt_dust_from_impure",
        "tfg:vi/centrifuge/salt_dust_from_pure"
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
          "text": "NaCl"
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
            "crafting",
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
      "id": "gtceu:salt_gem",
      "namespace": "gtceu",
      "display_name": "Salt Crystal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:gems",
        "forge:gems",
        "forge:gems/salt",
        "wan_ancient_beasts:snatcher_steals"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:compacting",
        "greate:cutting",
        "greate:milling",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "greate:compacting": 1,
        "greate:cutting": 3,
        "greate:milling": 1,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "blasting": 42,
        "crafting_shapeless": 1,
        "create:sandpaper_polishing": 3,
        "greate:cutting": 3,
        "greate:pressing": 4,
        "smelting": 44
      },
      "recipe_ingredient_count": 8,
      "recipe_output_count": 97,
      "recipe_ingredient_examples": [
        "constructionwand:shaped/iron_wand",
        "greate:compacting/salt_block",
        "greate:cutting/integration/gtceu/cutter/cut_salt_gem_to_flawed_gem",
        "greate:cutting/integration/gtceu/cutter/cut_salt_gem_to_flawed_gem_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_salt_gem_to_flawed_gem_water",
        "greate:milling/integration/gtceu/macerator/macerate_salt_gem",
        "gtceu:shapeless/gem_to_gem_flawed_gem_salt",
        "minecraft:shapeless/salt_bud_indicator"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_salt_flawless_gem_to_gem",
        "greate:cutting/integration/gtceu/cutter/cut_salt_flawless_gem_to_gem_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_salt_flawless_gem_to_gem_water",
        "greate:pressing/poor_raw_salt_to_gem",
        "greate:pressing/raw_salt_to_gem",
        "greate:pressing/rich_raw_salt_to_gem",
        "greate:pressing/unpacking_salt_block",
        "gtceu:blasting/smelt_andesite_salt_ore_to_ingot",
        "gtceu:blasting/smelt_basalt_salt_ore_to_ingot",
        "gtceu:blasting/smelt_black_sand_salt_ore_to_ingot",
        "gtceu:blasting/smelt_brown_sand_salt_ore_to_ingot",
        "gtceu:blasting/smelt_chalk_salt_ore_to_ingot",
        "gtceu:blasting/smelt_chert_salt_ore_to_ingot",
        "gtceu:blasting/smelt_claystone_salt_ore_to_ingot",
        "gtceu:blasting/smelt_conglomerate_salt_ore_to_ingot",
        "gtceu:blasting/smelt_dacite_salt_ore_to_ingot",
        "gtceu:blasting/smelt_deepslate_salt_ore_to_ingot",
        "gtceu:blasting/smelt_diorite_salt_ore_to_ingot",
        "gtceu:blasting/smelt_dolomite_salt_ore_to_ingot",
        "gtceu:blasting/smelt_dripstone_salt_ore_to_ingot",
        "gtceu:blasting/smelt_flavolite_salt_ore_to_ingot",
        "gtceu:blasting/smelt_gabbro_salt_ore_to_ingot",
        "gtceu:blasting/smelt_glacio_stone_salt_ore_to_ingot",
        "gtceu:blasting/smelt_gneiss_salt_ore_to_ingot",
        "gtceu:blasting/smelt_granite_salt_ore_to_ingot",
        "gtceu:blasting/smelt_green_sand_salt_ore_to_ingot",
        "gtceu:blasting/smelt_limestone_salt_ore_to_ingot",
        "gtceu:blasting/smelt_marble_salt_ore_to_ingot",
        "gtceu:blasting/smelt_mars_stone_salt_ore_to_ingot",
        "gtceu:blasting/smelt_mercury_stone_salt_ore_to_ingot",
        "gtceu:blasting/smelt_moon_deepslate_salt_ore_to_ingot",
        "gtceu:blasting/smelt_moon_stone_salt_ore_to_ingot",
        "gtceu:blasting/smelt_phyllite_salt_ore_to_ingot",
        "gtceu:blasting/smelt_pink_sand_salt_ore_to_ingot",
        "gtceu:blasting/smelt_pyroxenite_salt_ore_to_ingot",
        "gtceu:blasting/smelt_quartzite_salt_ore_to_ingot",
        "gtceu:blasting/smelt_raw_salt_ore_to_ingot",
        "gtceu:blasting/smelt_red_granite_salt_ore_to_ingot",
        "gtceu:blasting/smelt_red_sand_salt_ore_to_ingot",
        "gtceu:blasting/smelt_rhyolite_salt_ore_to_ingot",
        "gtceu:blasting/smelt_salt_ore_to_ingot",
        "gtceu:blasting/smelt_sandy_jadestone_salt_ore_to_ingot",
        "gtceu:blasting/smelt_schist_salt_ore_to_ingot",
        "gtceu:blasting/smelt_shale_salt_ore_to_ingot",
        "gtceu:blasting/smelt_slate_salt_ore_to_ingot",
        "gtceu:blasting/smelt_tuff_salt_ore_to_ingot",
        "gtceu:blasting/smelt_venus_stone_salt_ore_to_ingot",
        "gtceu:blasting/smelt_white_sand_salt_ore_to_ingot",
        "gtceu:shapeless/gem_to_gem_gem_salt",
        "gtceu:smelting/smelt_andesite_salt_ore_to_ingot",
        "gtceu:smelting/smelt_basalt_salt_ore_to_ingot",
        "gtceu:smelting/smelt_black_sand_salt_ore_to_ingot",
        "gtceu:smelting/smelt_brown_sand_salt_ore_to_ingot",
        "gtceu:smelting/smelt_chalk_salt_ore_to_ingot",
        "gtceu:smelting/smelt_chert_salt_ore_to_ingot",
        "gtceu:smelting/smelt_claystone_salt_ore_to_ingot",
        "gtceu:smelting/smelt_conglomerate_salt_ore_to_ingot",
        "gtceu:smelting/smelt_dacite_salt_ore_to_ingot",
        "gtceu:smelting/smelt_deepslate_salt_ore_to_ingot",
        "gtceu:smelting/smelt_diorite_salt_ore_to_ingot",
        "gtceu:smelting/smelt_dolomite_salt_ore_to_ingot",
        "gtceu:smelting/smelt_dripstone_salt_ore_to_ingot",
        "gtceu:smelting/smelt_flavolite_salt_ore_to_ingot",
        "gtceu:smelting/smelt_gabbro_salt_ore_to_ingot",
        "gtceu:smelting/smelt_glacio_stone_salt_ore_to_ingot",
        "gtceu:smelting/smelt_gneiss_salt_ore_to_ingot",
        "gtceu:smelting/smelt_granite_salt_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_salt_ore_to_ingot",
        "gtceu:smelting/smelt_limestone_salt_ore_to_ingot",
        "gtceu:smelting/smelt_marble_salt_ore_to_ingot",
        "gtceu:smelting/smelt_mars_stone_salt_ore_to_ingot",
        "gtceu:smelting/smelt_mercury_stone_salt_ore_to_ingot",
        "gtceu:smelting/smelt_moon_deepslate_salt_ore_to_ingot",
        "gtceu:smelting/smelt_moon_stone_salt_ore_to_ingot",
        "gtceu:smelting/smelt_phyllite_salt_ore_to_ingot",
        "gtceu:smelting/smelt_pink_sand_salt_ore_to_ingot",
        "gtceu:smelting/smelt_poor_salt_ore_to_ingot",
        "gtceu:smelting/smelt_pyroxenite_salt_ore_to_ingot",
        "gtceu:smelting/smelt_quartzite_salt_ore_to_ingot",
        "gtceu:smelting/smelt_raw_salt_ore_to_ingot",
        "gtceu:smelting/smelt_red_granite_salt_ore_to_ingot",
        "gtceu:smelting/smelt_red_sand_salt_ore_to_ingot",
        "gtceu:smelting/smelt_rhyolite_salt_ore_to_ingot",
        "gtceu:smelting/smelt_rich_salt_ore_to_ingot",
        "gtceu:smelting/smelt_salt_ore_to_ingot",
        "gtceu:smelting/smelt_sandy_jadestone_salt_ore_to_ingot",
        "gtceu:smelting/smelt_schist_salt_ore_to_ingot",
        "gtceu:smelting/smelt_shale_salt_ore_to_ingot",
        "gtceu:smelting/smelt_slate_salt_ore_to_ingot",
        "gtceu:smelting/smelt_tuff_salt_ore_to_ingot",
        "gtceu:smelting/smelt_venus_stone_salt_ore_to_ingot",
        "gtceu:smelting/smelt_white_sand_salt_ore_to_ingot",
        "gtceu:smelting/smelt_yellow_sand_salt_ore_to_ingot",
        "tfg:polishing/poor_raw_salt_to_gem",
        "tfg:polishing/raw_salt_to_gem",
        "tfg:polishing/rich_raw_salt_to_gem"
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
          "text": "NaCl"
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
          "value": "gem",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:gems"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:compacting",
            "greate:cutting",
            "greate:milling",
            "tfc:damage_inputs_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:salt_indicator",
      "namespace": "gtceu",
      "display_name": "Halite Surface Rock",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:surface_rocks",
        "forge:surface_rocks/salt"
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
        "block_id": "gtceu:salt_indicator",
        "block_tags": [
          "forge:surface_rocks",
          "forge:surface_rocks/salt",
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
          "text": "NaCl"
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
      "id": "gtceu:salt_ore",
      "namespace": "gtceu",
      "display_name": "Halite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/salt",
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
        "greate:milling/integration/gtceu/macerator/macerate_salt_ore_to_crushed_ore",
        "gtceu:blasting/smelt_salt_ore_to_ingot",
        "gtceu:smelting/smelt_salt_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:salt_ore",
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "NaCl"
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
          "value": "salt",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id salt_ore"
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
      "id": "gtceu:salt_water_bucket",
      "namespace": "gtceu",
      "display_name": "Salt Water Bucket",
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
          "text": "(NaCl)(H₂O)"
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
      "id": "gtceu:saltpeter_dust",
      "namespace": "gtceu",
      "display_name": "Saltpeter Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/saltpeter"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:mixing",
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 3,
        "greate:milling": 1,
        "greate:mixing": 3,
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
        "smelting": 44,
        "tfc:barrel_instant": 2,
        "vintageimprovements:centrifugation": 2
      },
      "recipe_ingredient_count": 11,
      "recipe_output_count": 102,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/saltpeter_to_powder",
        "greate:mixing/integration/gtceu/mixer/gunpowder_from_carbon",
        "greate:mixing/integration/gtceu/mixer/gunpowder_from_charcoal",
        "greate:mixing/integration/gtceu/mixer/gunpowder_from_coal",
        "gtceu:shaped/small_dust_disassembling_3x3_saltpeter",
        "gtceu:shaped/small_dust_disassembling_saltpeter",
        "gtceu:shaped/tiny_dust_disassembling_3x3_saltpeter",
        "gtceu:shaped/tiny_dust_disassembling_saltpeter",
        "tfg:shapeless/gunpowder_carbon",
        "tfg:shapeless/gunpowder_charcoal",
        "tfg:shapeless/gunpowder_coal"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerate_saltpeter_refined_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_saltpeter_refined_ore_to_dust",
        "greate:pressing/refined_saltpeter_to_dust",
        "gtceu:blasting/smelt_andesite_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_basalt_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_black_sand_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_brown_sand_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_chalk_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_claystone_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_conglomerate_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_dacite_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_deepslate_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_diorite_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_dolomite_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_dripstone_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_flavolite_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_gabbro_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_glacio_stone_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_gneiss_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_granite_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_green_sand_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_limestone_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_marble_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_mars_stone_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_moon_deepslate_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_moon_stone_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_phyllite_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_pink_sand_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_pyroxenite_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_quartzite_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_raw_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_red_granite_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_red_sand_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_rhyolite_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_sandy_jadestone_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_schist_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_shale_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_slate_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_tuff_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_white_sand_saltpeter_ore_to_ingot",
        "gtceu:blasting/smelt_yellow_sand_saltpeter_ore_to_ingot",
        "gtceu:shaped/small_dust_assembling_saltpeter",
        "gtceu:shaped/tiny_dust_assembling_saltpeter",
        "gtceu:shapeless/centrifuged_ore_to_dust_saltpeter",
        "gtceu:smelting/smelt_andesite_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_basalt_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_black_sand_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_brown_sand_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_chalk_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_chert_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_claystone_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_conglomerate_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_dacite_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_deepslate_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_diorite_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_dripstone_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_flavolite_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_gabbro_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_glacio_stone_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_gneiss_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_granite_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_limestone_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_marble_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_mars_stone_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_mercury_stone_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_moon_deepslate_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_moon_stone_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_phyllite_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_pink_sand_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_poor_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_quartzite_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_raw_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_red_granite_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_red_sand_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_rhyolite_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_rich_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_sandy_jadestone_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_schist_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_shale_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_slate_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_tuff_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_venus_stone_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_white_sand_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_yellow_sand_saltpeter_ore_to_ingot",
        "tfg:ae_transform/saltpeter_dust_from_impure",
        "tfg:instant_barrel/saltpeter_dust_from_impure",
        "tfg:instant_barrel/saltpeter_dust_from_pure",
        "tfg:splashing/saltpeter_dust_from_impure_distilled",
        "tfg:splashing/saltpeter_dust_from_impure_water",
        "tfg:splashing/saltpeter_dust_from_pure_distilled",
        "tfg:splashing/saltpeter_dust_from_pure_water",
        "tfg:vi/centrifuge/saltpeter_dust_from_impure",
        "tfg:vi/centrifuge/saltpeter_dust_from_pure"
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
          "text": "KNO₃"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§b(N) Nitrogen: §r10.0%"
        },
        {
          "source": "runtime-tooltip",
          "text": "§d(K) Potassium: §r35.0%"
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
      "id": "gtceu:saltpeter_indicator",
      "namespace": "gtceu",
      "display_name": "Saltpeter Surface Rock",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:surface_rocks",
        "forge:surface_rocks/saltpeter"
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
        "block_id": "gtceu:saltpeter_indicator",
        "block_tags": [
          "forge:surface_rocks",
          "forge:surface_rocks/saltpeter",
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
          "text": "KNO₃"
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
      "id": "gtceu:saltpeter_ore",
      "namespace": "gtceu",
      "display_name": "Saltpeter Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/saltpeter",
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
        "greate:milling/integration/gtceu/macerator/macerate_saltpeter_ore_to_crushed_ore",
        "gtceu:blasting/smelt_saltpeter_ore_to_ingot",
        "gtceu:smelting/smelt_saltpeter_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:saltpeter_ore",
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "KNO₃"
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
          "value": "saltpeter",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id saltpeter_ore"
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
      "id": "gtceu:samarium_block",
      "namespace": "gtceu",
      "display_name": "Block of Samarium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:storage_blocks",
        "forge:storage_blocks/samarium",
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
        "greate:compacting": 1,
        "smelting": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_samarium_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/samarium_block",
        "gtceu:smelting/demagnetize_magnetic_samarium_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:samarium_block",
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
          "forge:storage_blocks/samarium",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Sm"
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
      "id": "gtceu:samarium_bucket",
      "namespace": "gtceu",
      "display_name": "Liquid Samarium Bucket",
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
          "text": "Sm"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 1,345 K"
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
      "id": "gtceu:samarium_dust",
      "namespace": "gtceu",
      "display_name": "Samarium Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/samarium"
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
        "crafting_shaped": 2,
        "greate:milling": 7,
        "smelting": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 10,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/samarium_iron_arsenic_oxide",
        "gtceu:shaped/small_dust_disassembling_3x3_samarium",
        "gtceu:shaped/small_dust_disassembling_samarium",
        "gtceu:shaped/tiny_dust_disassembling_3x3_samarium",
        "gtceu:shaped/tiny_dust_disassembling_samarium"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_lab_equipment",
        "greate:milling/integration/gtceu/macerator/macerate_long_magnetic_samarium_rod",
        "greate:milling/integration/gtceu/macerator/macerate_long_samarium_rod",
        "greate:milling/integration/gtceu/macerator/macerate_magnetic_samarium_block",
        "greate:milling/integration/gtceu/macerator/macerate_magnetic_samarium_ingot",
        "greate:milling/integration/gtceu/macerator/macerate_samarium_block",
        "greate:milling/integration/gtceu/macerator/macerate_samarium_ingot",
        "gtceu:shaped/small_dust_assembling_samarium",
        "gtceu:shaped/tiny_dust_assembling_samarium",
        "gtceu:smelting/demagnetize_magnetic_samarium_dust"
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
          "text": "Sm"
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
      "id": "gtceu:samarium_ingot",
      "namespace": "gtceu",
      "display_name": "Samarium Ingot",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ingots",
        "forge:ingots",
        "forge:ingots/samarium",
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
        "greate:compacting/samarium_block",
        "greate:milling/integration/gtceu/macerator/macerate_samarium_ingot",
        "gtceu:shaped/stick_samarium",
        "tfg:vi/lathe/samarium_to_rod"
      ],
      "recipe_output_examples": [
        "gtceu:smelting/demagnetize_magnetic_samarium_ingot"
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
          "text": "Sm"
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
      "id": "gtceu:samarium_iron_arsenic_oxide_block",
      "namespace": "gtceu",
      "display_name": "Block of Samarium Iron Arsenic Oxide",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:storage_blocks",
        "forge:storage_blocks/samarium_iron_arsenic_oxide",
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
        "greate:cutting/integration/gtceu/cutter/cut_samarium_iron_arsenic_oxide_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_samarium_iron_arsenic_oxide_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_samarium_iron_arsenic_oxide_block_to_plate_water",
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/samarium_iron_arsenic_oxide_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:samarium_iron_arsenic_oxide_block",
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
          "forge:storage_blocks/samarium_iron_arsenic_oxide",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "SmFeAsO"
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
      "id": "gtceu:samarium_iron_arsenic_oxide_bucket",
      "namespace": "gtceu",
      "display_name": "Liquid Samarium Iron Arsenic Oxide Bucket",
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
          "text": "SmFeAsO"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 1,347 K"
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
      "id": "gtceu:samarium_iron_arsenic_oxide_double_wire",
      "namespace": "gtceu",
      "display_name": "2x Samarium Iron Arsenic Oxide Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:double_wires",
        "forge:double_wires/samarium_iron_arsenic_oxide"
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
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_double_wire",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_double_doubling",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_double_quadrupling",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_double_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_quadruple_splitting",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_single_doubling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:samarium_iron_arsenic_oxide_double_wire",
        "block_tags": [
          "forge:double_wires",
          "forge:double_wires/samarium_iron_arsenic_oxide",
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
          "text": "SmFeAsO"
        },
        {
          "source": "runtime-tooltip",
          "text": "IV §dSuperconductor"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a8,192 §a(§9IV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e12"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c0§7 EU-Volt"
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
      "id": "gtceu:samarium_iron_arsenic_oxide_dust",
      "namespace": "gtceu",
      "display_name": "Samarium Iron Arsenic Oxide Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/samarium_iron_arsenic_oxide"
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
        "greate:milling": 9,
        "greate:mixing": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 12,
      "recipe_ingredient_examples": [
        "gtceu:shaped/small_dust_disassembling_3x3_samarium_iron_arsenic_oxide",
        "gtceu:shaped/small_dust_disassembling_samarium_iron_arsenic_oxide",
        "gtceu:shaped/tiny_dust_disassembling_3x3_samarium_iron_arsenic_oxide",
        "gtceu:shaped/tiny_dust_disassembling_samarium_iron_arsenic_oxide"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_double_samarium_iron_arsenic_oxide_plate",
        "greate:milling/integration/gtceu/macerator/macerate_iv_field_generator",
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_block",
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_double_wire",
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_hex_wire",
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_ingot",
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_octal_wire",
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_plate",
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_quadruple_wire",
        "greate:mixing/integration/gtceu/mixer/samarium_iron_arsenic_oxide",
        "gtceu:shaped/small_dust_assembling_samarium_iron_arsenic_oxide",
        "gtceu:shaped/tiny_dust_assembling_samarium_iron_arsenic_oxide"
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
          "text": "SmFeAsO"
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
      "id": "gtceu:samarium_iron_arsenic_oxide_foil",
      "namespace": "gtceu",
      "display_name": "Samarium Iron Arsenic Oxide Foil",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:foils",
        "forge:foils/samarium_iron_arsenic_oxide"
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
        "createaddition:rolling": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_foil",
        "gtceu:shapeless/fine_wire_samarium_iron_arsenic_oxide"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/foil_samarium_iron_arsenic_oxide",
        "tfg:rolling/samarium_iron_arsenic_oxide_foil"
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
          "text": "SmFeAsO"
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
      "id": "gtceu:samarium_iron_arsenic_oxide_hex_wire",
      "namespace": "gtceu",
      "display_name": "16x Samarium Iron Arsenic Oxide Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:hex_wires",
        "forge:hex_wires/samarium_iron_arsenic_oxide"
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
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_hex_wire",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_hex_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_octal_doubling",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_quadruple_quadrupling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:samarium_iron_arsenic_oxide_hex_wire",
        "block_tags": [
          "forge:hex_wires",
          "forge:hex_wires/samarium_iron_arsenic_oxide",
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
          "text": "SmFeAsO"
        },
        {
          "source": "runtime-tooltip",
          "text": "IV §dSuperconductor"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a8,192 §a(§9IV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e96"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c0§7 EU-Volt"
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:samarium_iron_arsenic_oxide_ingot",
      "namespace": "gtceu",
      "display_name": "Samarium Iron Arsenic Oxide Ingot",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ingots",
        "forge:ingots",
        "forge:ingots/samarium_iron_arsenic_oxide",
        "tfc:pileable_ingots"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "createaddition:rolling",
        "greate:compacting",
        "greate:milling",
        "vintageimprovements:coiling"
      ],
      "recipe_consumption_by_type": {
        "createaddition:rolling": 1,
        "greate:compacting": 1,
        "greate:milling": 1,
        "vintageimprovements:coiling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 4,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:compacting/samarium_iron_arsenic_oxide_block",
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_ingot",
        "tfg:rolling/samarium_iron_arsenic_oxide_plate",
        "tfg:vi/coiling/samarium_iron_arsenic_oxide_single_wire"
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
          "text": "SmFeAsO"
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
            "createaddition:rolling",
            "greate:compacting",
            "greate:milling",
            "vintageimprovements:coiling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:samarium_iron_arsenic_oxide_nugget",
      "namespace": "gtceu",
      "display_name": "Samarium Iron Arsenic Oxide Nugget",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:nuggets",
        "forge:nuggets",
        "forge:nuggets/samarium_iron_arsenic_oxide"
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
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_nugget"
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
          "text": "SmFeAsO"
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
      "id": "gtceu:samarium_iron_arsenic_oxide_octal_wire",
      "namespace": "gtceu",
      "display_name": "8x Samarium Iron Arsenic Oxide Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:octal_wires",
        "forge:octal_wires/samarium_iron_arsenic_oxide"
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
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_octal_wire",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_octal_doubling",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_octal_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_double_quadrupling",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_hex_splitting",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_quadruple_doubling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:samarium_iron_arsenic_oxide_octal_wire",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:octal_wires",
          "forge:octal_wires/samarium_iron_arsenic_oxide",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "SmFeAsO"
        },
        {
          "source": "runtime-tooltip",
          "text": "IV §dSuperconductor"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a8,192 §a(§9IV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e48"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c0§7 EU-Volt"
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:samarium_iron_arsenic_oxide_plate",
      "namespace": "gtceu",
      "display_name": "Samarium Iron Arsenic Oxide Plate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:plates",
        "forge:plates/samarium_iron_arsenic_oxide",
        "forge:sheets/samarium_iron_arsenic_oxide",
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
        "crafting_shaped": 2,
        "createaddition:rolling": 1,
        "greate:compacting": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "createaddition:rolling": 1,
        "greate:cutting": 3
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_plate",
        "gtceu:shaped/foil_samarium_iron_arsenic_oxide",
        "gtceu:shaped/samarium_iron_arsenic_oxide_wire_single",
        "tfg:compacting/samarium_iron_arsenic_oxide_doublePlate",
        "tfg:rolling/samarium_iron_arsenic_oxide_foil"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_samarium_iron_arsenic_oxide_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_samarium_iron_arsenic_oxide_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_samarium_iron_arsenic_oxide_block_to_plate_water",
        "tfg:rolling/samarium_iron_arsenic_oxide_plate"
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
          "text": "SmFeAsO"
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
      "id": "gtceu:samarium_iron_arsenic_oxide_quadruple_wire",
      "namespace": "gtceu",
      "display_name": "4x Samarium Iron Arsenic Oxide Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:quadruple_wires",
        "forge:quadruple_wires/samarium_iron_arsenic_oxide"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 3,
        "greate:milling": 1,
        "kubejs:shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 3
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_quadruple_wire",
        "gtceu:shaped/field_generator_iv",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_quadruple_doubling",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_quadruple_quadrupling",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_quadruple_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_double_doubling",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_octal_splitting",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_single_quadrupling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:samarium_iron_arsenic_oxide_quadruple_wire",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:quadruple_wires",
          "forge:quadruple_wires/samarium_iron_arsenic_oxide",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "SmFeAsO"
        },
        {
          "source": "runtime-tooltip",
          "text": "IV §dSuperconductor"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a8,192 §a(§9IV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e24"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c0§7 EU-Volt"
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
      "id": "gtceu:samarium_iron_arsenic_oxide_single_wire",
      "namespace": "gtceu",
      "display_name": "1x Samarium Iron Arsenic Oxide Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:single_wires",
        "forge:single_wires/samarium_iron_arsenic_oxide"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "vintageimprovements:coiling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 2,
        "greate:milling": 1,
        "vintageimprovements:coiling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "vintageimprovements:coiling": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_samarium_iron_arsenic_oxide_single_wire",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_single_doubling",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_single_quadrupling",
        "tfg:sophisticated_backpacks/shaped/stack_upgrade_tier_1",
        "tfg:vi/coiling/samarium_iron_arsenic_oxide_fine_wire"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/samarium_iron_arsenic_oxide_wire_single",
        "gtceu:shapeless/samarium_iron_arsenic_oxide_wire_wire_gt_double_splitting",
        "tfg:vi/coiling/samarium_iron_arsenic_oxide_single_wire"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:samarium_iron_arsenic_oxide_single_wire",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:single_wires",
          "forge:single_wires/samarium_iron_arsenic_oxide",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "SmFeAsO"
        },
        {
          "source": "runtime-tooltip",
          "text": "IV §dSuperconductor"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a8,192 §a(§9IV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e6"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c0§7 EU-Volt"
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
            "greate:milling",
            "vintageimprovements:coiling"
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