# Items to classify
{
  "items": [
    {
      "id": "tfc:deposit/native_copper/limestone",
      "namespace": "tfc",
      "display_name": "Limestone Native Copper Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_copper/limestone_deposit",
        "tfg:splashing/native_copper/limestone_deposit_distilled",
        "tfg:vi/vibrating/deposits/limestone_native_copper"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_copper_limestone_deposit"
      ],
      "model_parents": [
        "item/deposit/native_copper/limestone",
        "block/deposit/native_copper/limestone",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_copper/limestone"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_copper/limestone",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_copper/marble",
      "namespace": "tfc",
      "display_name": "Marble Native Copper Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_copper/marble_deposit",
        "tfg:splashing/native_copper/marble_deposit_distilled",
        "tfg:vi/vibrating/deposits/marble_native_copper"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_copper_marble_deposit"
      ],
      "model_parents": [
        "item/deposit/native_copper/marble",
        "block/deposit/native_copper/marble",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_copper/marble"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_copper/marble",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_copper/phyllite",
      "namespace": "tfc",
      "display_name": "Phyllite Native Copper Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_copper/phyllite_deposit",
        "tfg:splashing/native_copper/phyllite_deposit_distilled",
        "tfg:vi/vibrating/deposits/phyllite_native_copper"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_copper_phyllite_deposit"
      ],
      "model_parents": [
        "item/deposit/native_copper/phyllite",
        "block/deposit/native_copper/phyllite",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_copper/phyllite"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_copper/phyllite",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_copper/quartzite",
      "namespace": "tfc",
      "display_name": "Quartzite Native Copper Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_copper/quartzite_deposit",
        "tfg:splashing/native_copper/quartzite_deposit_distilled",
        "tfg:vi/vibrating/deposits/quartzite_native_copper"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_copper_quartzite_deposit"
      ],
      "model_parents": [
        "item/deposit/native_copper/quartzite",
        "block/deposit/native_copper/quartzite",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_copper/quartzite"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_copper/quartzite",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_copper/rhyolite",
      "namespace": "tfc",
      "display_name": "Rhyolite Native Copper Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_copper/rhyolite_deposit",
        "tfg:splashing/native_copper/rhyolite_deposit_distilled",
        "tfg:vi/vibrating/deposits/rhyolite_native_copper"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_copper_rhyolite_deposit"
      ],
      "model_parents": [
        "item/deposit/native_copper/rhyolite",
        "block/deposit/native_copper/rhyolite",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_copper/rhyolite"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_copper/rhyolite",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_copper/schist",
      "namespace": "tfc",
      "display_name": "Schist Native Copper Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_copper/schist_deposit",
        "tfg:splashing/native_copper/schist_deposit_distilled",
        "tfg:vi/vibrating/deposits/schist_native_copper"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_copper_schist_deposit"
      ],
      "model_parents": [
        "item/deposit/native_copper/schist",
        "block/deposit/native_copper/schist",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_copper/schist"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_copper/schist",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_copper/shale",
      "namespace": "tfc",
      "display_name": "Shale Native Copper Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_copper/shale_deposit",
        "tfg:splashing/native_copper/shale_deposit_distilled",
        "tfg:vi/vibrating/deposits/shale_native_copper"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_copper_shale_deposit"
      ],
      "model_parents": [
        "item/deposit/native_copper/shale",
        "block/deposit/native_copper/shale",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_copper/shale"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_copper/shale",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_copper/slate",
      "namespace": "tfc",
      "display_name": "Slate Native Copper Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_copper/slate_deposit",
        "tfg:splashing/native_copper/slate_deposit_distilled",
        "tfg:vi/vibrating/deposits/slate_native_copper"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_copper_slate_deposit"
      ],
      "model_parents": [
        "item/deposit/native_copper/slate",
        "block/deposit/native_copper/slate",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_copper/slate"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_copper/slate",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/andesite",
      "namespace": "tfc",
      "display_name": "Andesite Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/andesite_deposit",
        "tfg:splashing/native_gold/andesite_deposit_distilled",
        "tfg:vi/vibrating/deposits/andesite_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_andesite_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/andesite",
        "block/deposit/native_gold/andesite",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/andesite"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/andesite",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/basalt",
      "namespace": "tfc",
      "display_name": "Basalt Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/basalt_deposit",
        "tfg:splashing/native_gold/basalt_deposit_distilled",
        "tfg:vi/vibrating/deposits/basalt_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_basalt_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/basalt",
        "block/deposit/native_gold/basalt",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/basalt"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/basalt",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/chalk",
      "namespace": "tfc",
      "display_name": "Chalk Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/chalk_deposit",
        "tfg:splashing/native_gold/chalk_deposit_distilled",
        "tfg:vi/vibrating/deposits/chalk_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_chalk_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/chalk",
        "block/deposit/native_gold/chalk",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/chalk"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/chalk",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/chert",
      "namespace": "tfc",
      "display_name": "Chert Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/chert_deposit",
        "tfg:splashing/native_gold/chert_deposit_distilled",
        "tfg:vi/vibrating/deposits/chert_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_chert_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/chert",
        "block/deposit/native_gold/chert",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/chert"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/chert",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/claystone",
      "namespace": "tfc",
      "display_name": "Claystone Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/claystone_deposit",
        "tfg:splashing/native_gold/claystone_deposit_distilled",
        "tfg:vi/vibrating/deposits/claystone_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_claystone_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/claystone",
        "block/deposit/native_gold/claystone",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/claystone"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/claystone",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/conglomerate",
      "namespace": "tfc",
      "display_name": "Conglomerate Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/conglomerate_deposit",
        "tfg:splashing/native_gold/conglomerate_deposit_distilled",
        "tfg:vi/vibrating/deposits/conglomerate_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_conglomerate_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/conglomerate",
        "block/deposit/native_gold/conglomerate",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/conglomerate"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/conglomerate",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/dacite",
      "namespace": "tfc",
      "display_name": "Dacite Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/dacite_deposit",
        "tfg:splashing/native_gold/dacite_deposit_distilled",
        "tfg:vi/vibrating/deposits/dacite_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_dacite_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/dacite",
        "block/deposit/native_gold/dacite",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/dacite"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/dacite",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/diorite",
      "namespace": "tfc",
      "display_name": "Diorite Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/diorite_deposit",
        "tfg:splashing/native_gold/diorite_deposit_distilled",
        "tfg:vi/vibrating/deposits/diorite_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_diorite_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/diorite",
        "block/deposit/native_gold/diorite",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/diorite"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/diorite",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/dolomite",
      "namespace": "tfc",
      "display_name": "Dolomite Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/dolomite_deposit",
        "tfg:splashing/native_gold/dolomite_deposit_distilled",
        "tfg:vi/vibrating/deposits/dolomite_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_dolomite_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/dolomite",
        "block/deposit/native_gold/dolomite",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/dolomite"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/dolomite",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/gabbro",
      "namespace": "tfc",
      "display_name": "Gabbro Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/gabbro_deposit",
        "tfg:splashing/native_gold/gabbro_deposit_distilled",
        "tfg:vi/vibrating/deposits/gabbro_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_gabbro_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/gabbro",
        "block/deposit/native_gold/gabbro",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/gabbro"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/gabbro",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/gneiss",
      "namespace": "tfc",
      "display_name": "Gneiss Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/gneiss_deposit",
        "tfg:splashing/native_gold/gneiss_deposit_distilled",
        "tfg:vi/vibrating/deposits/gneiss_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_gneiss_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/gneiss",
        "block/deposit/native_gold/gneiss",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/gneiss"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/gneiss",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/granite",
      "namespace": "tfc",
      "display_name": "Granite Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/granite_deposit",
        "tfg:splashing/native_gold/granite_deposit_distilled",
        "tfg:vi/vibrating/deposits/granite_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_granite_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/granite",
        "block/deposit/native_gold/granite",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/granite"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/granite",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/limestone",
      "namespace": "tfc",
      "display_name": "Limestone Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/limestone_deposit",
        "tfg:splashing/native_gold/limestone_deposit_distilled",
        "tfg:vi/vibrating/deposits/limestone_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_limestone_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/limestone",
        "block/deposit/native_gold/limestone",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/limestone"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/limestone",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/marble",
      "namespace": "tfc",
      "display_name": "Marble Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/marble_deposit",
        "tfg:splashing/native_gold/marble_deposit_distilled",
        "tfg:vi/vibrating/deposits/marble_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_marble_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/marble",
        "block/deposit/native_gold/marble",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/marble"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/marble",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/phyllite",
      "namespace": "tfc",
      "display_name": "Phyllite Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/phyllite_deposit",
        "tfg:splashing/native_gold/phyllite_deposit_distilled",
        "tfg:vi/vibrating/deposits/phyllite_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_phyllite_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/phyllite",
        "block/deposit/native_gold/phyllite",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/phyllite"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/phyllite",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/quartzite",
      "namespace": "tfc",
      "display_name": "Quartzite Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/quartzite_deposit",
        "tfg:splashing/native_gold/quartzite_deposit_distilled",
        "tfg:vi/vibrating/deposits/quartzite_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_quartzite_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/quartzite",
        "block/deposit/native_gold/quartzite",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/quartzite"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/quartzite",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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
      "id": "tfc:deposit/native_gold/rhyolite",
      "namespace": "tfc",
      "display_name": "Rhyolite Native Gold Deposit",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gravel",
        "tfc:ore_deposits"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "greate:splashing",
        "tfc:damage_inputs_shapeless_crafting",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 9,
        "greate:mixing": 16,
        "greate:splashing": 2,
        "tfc:damage_inputs_shapeless_crafting": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/black_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/cyan_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/green_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_blue_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/light_gray_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/lime_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/magenta_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/orange_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/pink_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/purple_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/red_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/white_concrete_powder",
        "greate:mixing/integration/gtceu/mixer/yellow_concrete_powder",
        "rnr:crafting/base_course",
        "rnr:crafting/hoggin_mix",
        "tfc:crafting/aggregate",
        "tfg:shapeless/create_coarse_alfisol_dirt",
        "tfg:shapeless/create_coarse_loam_dirt",
        "tfg:shapeless/create_coarse_mollisol_dirt",
        "tfg:shapeless/create_coarse_oxisol_dirt",
        "tfg:shapeless/create_coarse_podzol_dirt",
        "tfg:shapeless/create_coarse_sandy_loam_dirt",
        "tfg:shapeless/create_coarse_silt_dirt",
        "tfg:shapeless/create_coarse_silty_loam_dirt",
        "tfg:splashing/native_gold/rhyolite_deposit",
        "tfg:splashing/native_gold/rhyolite_deposit_distilled",
        "tfg:vi/vibrating/deposits/rhyolite_native_gold"
      ],
      "recipe_output_examples": [
        "tfc:landslide/native_gold_rhyolite_deposit"
      ],
      "model_parents": [
        "item/deposit/native_gold/rhyolite",
        "block/deposit/native_gold/rhyolite",
        "block/ore",
        "block/block"
      ],
      "creative_tabs": [
        "tfc:ores"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/deposit/native_gold/rhyolite"
      ],
      "block_context": {
        "block_id": "tfc:deposit/native_gold/rhyolite",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "forge:gravel",
          "minecraft:enderman_holdable",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "tfc:can_landslide",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:ore_deposits",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfg:anemone_plantable_on",
          "tfg:dry_plant_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Can be processed with a sluice or pan"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
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
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "greate:splashing",
            "tfc:damage_inputs_shapeless_crafting",
            "vintageimprovements:vibrating"
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