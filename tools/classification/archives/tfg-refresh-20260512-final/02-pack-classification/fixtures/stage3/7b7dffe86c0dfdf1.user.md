# Items to classify
{
  "items": [
    {
      "id": "tfg:grass/amber_clay_mycelium",
      "namespace": "tfg",
      "display_name": "Amber Clay Mycelium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:clay",
        "tfc:grass",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 3,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass/amber_clay_mycelium",
        "block_tags": [
          "ad_astra:mars_stone_replaceables",
          "beneath:event_replaceable",
          "beneath:nether_bush_plantable_on",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:animals_spawnable_on",
          "minecraft:axolotls_spawnable_on",
          "minecraft:bamboo_plantable_on",
          "minecraft:big_dripleaf_placeable",
          "minecraft:frogs_spawnable_on",
          "minecraft:goats_spawnable_on",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "minecraft:small_dripleaf_placeable",
          "minecraft:valid_spawn",
          "tfc:bush_plantable_on",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfc:clay_grass",
          "tfc:creeping_plantable_on",
          "tfc:grass",
          "tfc:grass_plantable_on",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:spreading_fruit_grows_on",
          "tfc:tree_grows_on",
          "tfc:wild_crop_grows_on",
          "tfg:do_not_destroy_in_space",
          "tfg:dry_plant_plantable_on",
          "tfg:mars_soil"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:grass/amber_kaolin_mycelium",
      "namespace": "tfg",
      "display_name": "Amber Kaolin Mycelium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:dirt",
        "tfc:grass",
        "tfc:kaolin_clay",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 5,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfc:crafting/composter",
        "tfc:crafting/daub"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass/amber_kaolin_mycelium",
        "block_tags": [
          "ad_astra:mars_stone_replaceables",
          "beneath:event_replaceable",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "firmalife:pipe_replaceable",
          "framedblocks:camo_sustain_plant",
          "gtceu:charcoal_pile_igniter_walls",
          "minecraft:animals_spawnable_on",
          "minecraft:azalea_grows_on",
          "minecraft:azalea_root_replaceable",
          "minecraft:bamboo_plantable_on",
          "minecraft:big_dripleaf_placeable",
          "minecraft:dead_bush_may_place_on",
          "minecraft:dirt",
          "minecraft:frogs_spawnable_on",
          "minecraft:goats_spawnable_on",
          "minecraft:lush_ground_replaceable",
          "minecraft:mineable/shovel",
          "minecraft:moss_replaceable",
          "minecraft:mushroom_grow_block",
          "minecraft:nether_carver_replaceables",
          "minecraft:overworld_carver_replaceables",
          "minecraft:sculk_replaceable",
          "minecraft:sculk_replaceable_world_gen",
          "minecraft:valid_spawn",
          "tfc:bush_plantable_on",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfc:creeping_plantable_on",
          "tfc:grass",
          "tfc:grass_plantable_on",
          "tfc:halophyte_plantable_on",
          "tfc:kaolin_clay",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfc:tree_grows_on",
          "tfc:wild_crop_grows_on",
          "tfg:do_not_destroy_in_space",
          "tfg:dry_plant_plantable_on",
          "tfg:mars_soil"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:grass/amber_mycelium",
      "namespace": "tfg",
      "display_name": "Amber Mycelium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:grass",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 3,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass/amber_mycelium",
        "block_tags": [
          "ad_astra:mars_stone_replaceables",
          "beneath:event_replaceable",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:animals_spawnable_on",
          "minecraft:bamboo_plantable_on",
          "minecraft:frogs_spawnable_on",
          "minecraft:goats_spawnable_on",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "minecraft:valid_spawn",
          "tfc:bush_plantable_on",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfc:creeping_plantable_on",
          "tfc:grass",
          "tfc:grass_plantable_on",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:spreading_fruit_grows_on",
          "tfc:tree_grows_on",
          "tfc:wild_crop_grows_on",
          "tfg:do_not_destroy_in_space",
          "tfg:dry_plant_plantable_on",
          "tfg:mars_soil"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:grass/mars_clay_dirt",
      "namespace": "tfg",
      "display_name": "Martian Clay Dirt",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:dirt",
        "tfc:clay",
        "tfc:dirt",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 4,
        "greate:mixing": 2,
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {
        "tfc:landslide": 4
      },
      "recipe_ingredient_count": 10,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/tfg/fertilizer",
        "greate:mixing/integration/tfg/mars_dirt",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "minecraft:kjs/wan_ancient_beasts_quick_red_sand_bucket",
        "minecraft:kjs/wan_ancient_beasts_quick_sand_bucket",
        "tfc:crafting/composter",
        "tfc:crafting/daub",
        "tfg:shapeless/moss_block"
      ],
      "recipe_output_examples": [
        "tfc:kjs/5x0ds99gpag6zjcu7hf3ghdga",
        "tfc:kjs/aaceyuuu4i502rb91s838l1qd",
        "tfc:kjs/ddd5ctagaxuxyynpsg6ml0bal",
        "tfc:kjs/dvp9nk4q7850vgvlspuouk0uy"
      ],
      "model_parents": [
        "item/grass/mars_clay_dirt",
        "block/grass/mars_clay_dirt"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass/mars_clay_dirt",
        "block_tags": [
          "ad_astra:mars_stone_replaceables",
          "beneath:event_replaceable",
          "beneath:nether_bush_plantable_on",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "firmalife:pipe_replaceable",
          "framedblocks:camo_sustain_plant",
          "gtceu:charcoal_pile_igniter_walls",
          "minecraft:animals_spawnable_on",
          "minecraft:axolotls_spawnable_on",
          "minecraft:azalea_grows_on",
          "minecraft:azalea_root_replaceable",
          "minecraft:bamboo_plantable_on",
          "minecraft:big_dripleaf_placeable",
          "minecraft:dead_bush_may_place_on",
          "minecraft:dirt",
          "minecraft:enderman_holdable",
          "minecraft:frogs_spawnable_on",
          "minecraft:goats_spawnable_on",
          "minecraft:lush_ground_replaceable",
          "minecraft:mineable/shovel",
          "minecraft:moss_replaceable",
          "minecraft:mushroom_grow_block",
          "minecraft:nether_carver_replaceables",
          "minecraft:overworld_carver_replaceables",
          "minecraft:sculk_replaceable",
          "minecraft:sculk_replaceable_world_gen",
          "minecraft:small_dripleaf_placeable",
          "minecraft:valid_spawn",
          "tfc:bush_plantable_on",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfc:clay",
          "tfc:creeping_plantable_on",
          "tfc:dirt",
          "tfc:grass_plantable_on",
          "tfc:halophyte_plantable_on",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfc:tree_grows_on",
          "tfc:wild_crop_grows_on",
          "tfg:dry_plant_plantable_on",
          "tfg:mars_soil"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:grass/mars_dirt",
      "namespace": "tfg",
      "display_name": "Martian Dirt",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:dirt",
        "tfc:dirt",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 4,
        "greate:mixing": 2,
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {
        "greate:mixing": 1,
        "tfc:landslide": 6
      },
      "recipe_ingredient_count": 10,
      "recipe_output_count": 7,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/tfg/fertilizer",
        "greate:mixing/integration/tfg/mars_dirt",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "minecraft:kjs/wan_ancient_beasts_quick_red_sand_bucket",
        "minecraft:kjs/wan_ancient_beasts_quick_sand_bucket",
        "tfc:crafting/composter",
        "tfc:crafting/daub",
        "tfg:shapeless/moss_block"
      ],
      "recipe_output_examples": [
        "greate:mixing/integration/tfg/mars_dirt",
        "tfc:kjs/1s3ocrvq8z3jhbit0zi3dk3xk",
        "tfc:kjs/2qroc88bovy8grh633a6jnbin",
        "tfc:kjs/3g7d5nna0i8fbqtf1r7yb1zaz",
        "tfc:kjs/ahhtfil08te5m27dxyi8aahhq",
        "tfc:kjs/c71xm5uoohzrdc20xnivfv3l6",
        "tfc:kjs/u4zilwwc26yjvctuarmzpyt8"
      ],
      "model_parents": [
        "item/grass/mars_dirt",
        "block/grass/mars_dirt"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 6,
      "loot_source_examples": [
        "tfg:blocks/grass/amber_mycelium",
        "tfg:blocks/grass/mars_dirt",
        "tfg:blocks/grass/mars_farmland",
        "tfg:blocks/grass/mars_path",
        "tfg:blocks/grass/rusticus_mycelium",
        "tfg:blocks/grass/sangnum_mycelium"
      ],
      "block_context": {
        "block_id": "tfg:grass/mars_dirt",
        "block_tags": [
          "ad_astra:mars_stone_replaceables",
          "beneath:event_replaceable",
          "beneath:nether_bush_plantable_on",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "firmalife:pipe_replaceable",
          "framedblocks:camo_sustain_plant",
          "gtceu:charcoal_pile_igniter_walls",
          "minecraft:animals_spawnable_on",
          "minecraft:azalea_grows_on",
          "minecraft:azalea_root_replaceable",
          "minecraft:bamboo_plantable_on",
          "minecraft:big_dripleaf_placeable",
          "minecraft:dead_bush_may_place_on",
          "minecraft:dirt",
          "minecraft:enderman_holdable",
          "minecraft:frogs_spawnable_on",
          "minecraft:goats_spawnable_on",
          "minecraft:lush_ground_replaceable",
          "minecraft:mineable/shovel",
          "minecraft:moss_replaceable",
          "minecraft:mushroom_grow_block",
          "minecraft:nether_carver_replaceables",
          "minecraft:overworld_carver_replaceables",
          "minecraft:sculk_replaceable",
          "minecraft:sculk_replaceable_world_gen",
          "minecraft:valid_spawn",
          "tfc:bush_plantable_on",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfc:creeping_plantable_on",
          "tfc:dirt",
          "tfc:grass_plantable_on",
          "tfc:halophyte_plantable_on",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfc:tree_grows_on",
          "tfc:wild_crop_grows_on",
          "tfg:dry_plant_plantable_on",
          "tfg:mars_soil"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:grass/mars_farmland",
      "namespace": "tfg",
      "display_name": "Martian Farmland",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:farmland"
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
        "item/grass/mars_farmland",
        "block/grass/mars_farmland"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass/mars_farmland",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:mineable/shovel",
          "tfc:bush_plantable_on",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfc:creeping_plantable_on",
          "tfc:farmland",
          "tfc:grass_plantable_on",
          "tfc:kaolin_clay_replaceable",
          "tfc:spreading_fruit_grows_on",
          "tfc:supports_landslide",
          "tfc:wild_crop_grows_on",
          "tfg:do_not_destroy_in_space",
          "tfg:dry_plant_plantable_on"
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
      "document_context": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 3,
          "related_item_refs": [
            "ad_astra:moon_sand",
            "betterend:cave_pumpkin"
          ],
          "snippets": [
            {
              "source": "guide-page",
              "key": "name",
              "text": "Extraterrestrial Crops"
            },
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "It turns out Earth isn't the only celestial body with life. During your travels, you may come across other kinds of edible flora. These all use the same mechanics you're used to on Earth (with some exceptions), and can be grown either in normal Farmland, in a Firmalife Greenhouse, or in a GregTech Electric Greenhouse depending on your needs."
            },
            {
              "source": "guide-page",
              "key": "pages.1.text",
              "text": "An Air Distributor will also keep an enclosed area a stable 15 °C. Due to technical reasons, this temperature can't be displayed in the Jade tooltip while on other planets, so you'll have to use your inventory's Climate tab to check the temperature. Another alternative is to use Firmalife's Greenhouse, which doesn't provide as much output, but also ignores all climate conditions."
            },
            {
              "source": "guide-page",
              "key": "pages.2.text",
              "text": "- Amber Root - Blossom Berry - Bolux Mushroom - Bulbkin - Chalmie Mushroom - Chorus Fruit - Nox Berry"
            }
          ]
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        }
      }
    },
    {
      "id": "tfg:grass/mars_path",
      "namespace": "tfg",
      "display_name": "Martian Path",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:paths"
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
        "item/grass/mars_path",
        "block/grass/mars_path"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass/mars_path",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:mineable/shovel",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfg:do_not_destroy_in_space"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        }
      }
    },
    {
      "id": "tfg:grass/mollisol",
      "namespace": "tfg",
      "display_name": "Volcanic Grass",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:grass",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 2
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "tfg:shapeless/mollisol_grass_bonemeal",
        "tfg:shapeless/mollisol_grass_fertilizer"
      ],
      "model_parents": [
        "item/grass/mollisol",
        "item/grass_inv"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass/mollisol",
        "block_tags": [
          "beneath:event_replaceable",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:bamboo_plantable_on",
          "minecraft:frogs_spawnable_on",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "minecraft:valid_spawn",
          "tfc:bush_plantable_on",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfc:creeping_plantable_on",
          "tfc:grass",
          "tfc:grass_plantable_on",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:spreading_fruit_grows_on",
          "tfc:tree_grows_on",
          "tfc:wild_crop_grows_on",
          "tfg:dry_plant_plantable_on"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:grass/oxisol",
      "namespace": "tfg",
      "display_name": "Tropical Grass",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:grass",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 2
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "tfg:shapeless/oxisol_grass_bonemeal",
        "tfg:shapeless/oxisol_grass_fertilizer"
      ],
      "model_parents": [
        "item/grass/oxisol",
        "item/grass_inv"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass/oxisol",
        "block_tags": [
          "beneath:event_replaceable",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:bamboo_plantable_on",
          "minecraft:frogs_spawnable_on",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "minecraft:valid_spawn",
          "tfc:bush_plantable_on",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfc:creeping_plantable_on",
          "tfc:grass",
          "tfc:grass_plantable_on",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:spreading_fruit_grows_on",
          "tfc:tree_grows_on",
          "tfc:wild_crop_grows_on",
          "tfg:dry_plant_plantable_on"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:grass/podzol",
      "namespace": "tfg",
      "display_name": "Layered Grass",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:grass",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 2
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "tfg:shapeless/podzol_grass_bonemeal",
        "tfg:shapeless/podzol_grass_fertilizer"
      ],
      "model_parents": [
        "item/grass/podzol",
        "item/grass_inv"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass/podzol",
        "block_tags": [
          "beneath:event_replaceable",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:bamboo_plantable_on",
          "minecraft:frogs_spawnable_on",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "minecraft:valid_spawn",
          "tfc:bush_plantable_on",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfc:creeping_plantable_on",
          "tfc:grass",
          "tfc:grass_plantable_on",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:spreading_fruit_grows_on",
          "tfc:tree_grows_on",
          "tfc:wild_crop_grows_on",
          "tfg:dry_plant_plantable_on"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:grass/rusticus_clay_mycelium",
      "namespace": "tfg",
      "display_name": "Rusticus Clay Mycelium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:clay",
        "tfc:grass",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 3,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass/rusticus_clay_mycelium",
        "block_tags": [
          "ad_astra:mars_stone_replaceables",
          "beneath:event_replaceable",
          "beneath:nether_bush_plantable_on",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:animals_spawnable_on",
          "minecraft:axolotls_spawnable_on",
          "minecraft:bamboo_plantable_on",
          "minecraft:big_dripleaf_placeable",
          "minecraft:frogs_spawnable_on",
          "minecraft:goats_spawnable_on",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "minecraft:small_dripleaf_placeable",
          "minecraft:valid_spawn",
          "tfc:bush_plantable_on",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfc:clay_grass",
          "tfc:creeping_plantable_on",
          "tfc:grass",
          "tfc:grass_plantable_on",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:spreading_fruit_grows_on",
          "tfc:tree_grows_on",
          "tfc:wild_crop_grows_on",
          "tfg:do_not_destroy_in_space",
          "tfg:dry_plant_plantable_on",
          "tfg:mars_soil"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:grass/rusticus_kaolin_mycelium",
      "namespace": "tfg",
      "display_name": "Rusticus Kaolin Mycelium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:dirt",
        "tfc:grass",
        "tfc:kaolin_clay",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 5,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfc:crafting/composter",
        "tfc:crafting/daub"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass/rusticus_kaolin_mycelium",
        "block_tags": [
          "ad_astra:mars_stone_replaceables",
          "beneath:event_replaceable",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "firmalife:pipe_replaceable",
          "framedblocks:camo_sustain_plant",
          "gtceu:charcoal_pile_igniter_walls",
          "minecraft:animals_spawnable_on",
          "minecraft:azalea_grows_on",
          "minecraft:azalea_root_replaceable",
          "minecraft:bamboo_plantable_on",
          "minecraft:big_dripleaf_placeable",
          "minecraft:dead_bush_may_place_on",
          "minecraft:dirt",
          "minecraft:frogs_spawnable_on",
          "minecraft:goats_spawnable_on",
          "minecraft:lush_ground_replaceable",
          "minecraft:mineable/shovel",
          "minecraft:moss_replaceable",
          "minecraft:mushroom_grow_block",
          "minecraft:nether_carver_replaceables",
          "minecraft:overworld_carver_replaceables",
          "minecraft:sculk_replaceable",
          "minecraft:sculk_replaceable_world_gen",
          "minecraft:valid_spawn",
          "tfc:bush_plantable_on",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfc:creeping_plantable_on",
          "tfc:grass",
          "tfc:grass_plantable_on",
          "tfc:halophyte_plantable_on",
          "tfc:kaolin_clay",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfc:tree_grows_on",
          "tfc:wild_crop_grows_on",
          "tfg:do_not_destroy_in_space",
          "tfg:dry_plant_plantable_on",
          "tfg:mars_soil"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:grass/rusticus_mycelium",
      "namespace": "tfg",
      "display_name": "Rusticus Mycelium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:grass",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 3,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass/rusticus_mycelium",
        "block_tags": [
          "ad_astra:mars_stone_replaceables",
          "beneath:event_replaceable",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:animals_spawnable_on",
          "minecraft:bamboo_plantable_on",
          "minecraft:frogs_spawnable_on",
          "minecraft:goats_spawnable_on",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "minecraft:valid_spawn",
          "tfc:bush_plantable_on",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfc:creeping_plantable_on",
          "tfc:grass",
          "tfc:grass_plantable_on",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:spreading_fruit_grows_on",
          "tfc:tree_grows_on",
          "tfc:wild_crop_grows_on",
          "tfg:do_not_destroy_in_space",
          "tfg:dry_plant_plantable_on",
          "tfg:mars_soil"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:grass/sangnum_clay_mycelium",
      "namespace": "tfg",
      "display_name": "Sangnum Clay Mycelium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:clay",
        "tfc:grass",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 3,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass/sangnum_clay_mycelium",
        "block_tags": [
          "ad_astra:mars_stone_replaceables",
          "beneath:event_replaceable",
          "beneath:nether_bush_plantable_on",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:animals_spawnable_on",
          "minecraft:axolotls_spawnable_on",
          "minecraft:bamboo_plantable_on",
          "minecraft:big_dripleaf_placeable",
          "minecraft:frogs_spawnable_on",
          "minecraft:goats_spawnable_on",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "minecraft:small_dripleaf_placeable",
          "minecraft:valid_spawn",
          "tfc:bush_plantable_on",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfc:clay_grass",
          "tfc:creeping_plantable_on",
          "tfc:grass",
          "tfc:grass_plantable_on",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:spreading_fruit_grows_on",
          "tfc:tree_grows_on",
          "tfc:wild_crop_grows_on",
          "tfg:do_not_destroy_in_space",
          "tfg:dry_plant_plantable_on",
          "tfg:mars_soil"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:grass/sangnum_kaolin_mycelium",
      "namespace": "tfg",
      "display_name": "Sangnum Kaolin Mycelium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:dirt",
        "tfc:grass",
        "tfc:kaolin_clay",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 5,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfc:crafting/composter",
        "tfc:crafting/daub"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass/sangnum_kaolin_mycelium",
        "block_tags": [
          "ad_astra:mars_stone_replaceables",
          "beneath:event_replaceable",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "firmalife:pipe_replaceable",
          "framedblocks:camo_sustain_plant",
          "gtceu:charcoal_pile_igniter_walls",
          "minecraft:animals_spawnable_on",
          "minecraft:azalea_grows_on",
          "minecraft:azalea_root_replaceable",
          "minecraft:bamboo_plantable_on",
          "minecraft:big_dripleaf_placeable",
          "minecraft:dead_bush_may_place_on",
          "minecraft:dirt",
          "minecraft:frogs_spawnable_on",
          "minecraft:goats_spawnable_on",
          "minecraft:lush_ground_replaceable",
          "minecraft:mineable/shovel",
          "minecraft:moss_replaceable",
          "minecraft:mushroom_grow_block",
          "minecraft:nether_carver_replaceables",
          "minecraft:overworld_carver_replaceables",
          "minecraft:sculk_replaceable",
          "minecraft:sculk_replaceable_world_gen",
          "minecraft:valid_spawn",
          "tfc:bush_plantable_on",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfc:creeping_plantable_on",
          "tfc:grass",
          "tfc:grass_plantable_on",
          "tfc:halophyte_plantable_on",
          "tfc:kaolin_clay",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:sea_bush_plantable_on",
          "tfc:spreading_fruit_grows_on",
          "tfc:tree_grows_on",
          "tfc:wild_crop_grows_on",
          "tfg:do_not_destroy_in_space",
          "tfg:dry_plant_plantable_on",
          "tfg:mars_soil"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "crafting",
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:grass/sangnum_mycelium",
      "namespace": "tfg",
      "display_name": "Sangnum Mycelium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:grass",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 3,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass/sangnum_mycelium",
        "block_tags": [
          "ad_astra:mars_stone_replaceables",
          "beneath:event_replaceable",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:animals_spawnable_on",
          "minecraft:bamboo_plantable_on",
          "minecraft:frogs_spawnable_on",
          "minecraft:goats_spawnable_on",
          "minecraft:mineable/shovel",
          "minecraft:mushroom_grow_block",
          "minecraft:valid_spawn",
          "tfc:bush_plantable_on",
          "tfc:can_carve",
          "tfc:can_landslide",
          "tfc:creeping_plantable_on",
          "tfc:grass",
          "tfc:grass_plantable_on",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powder_snow_replaceable",
          "tfc:powderkeg_breaking_blocks",
          "tfc:spreading_fruit_grows_on",
          "tfc:tree_grows_on",
          "tfc:wild_crop_grows_on",
          "tfg:do_not_destroy_in_space",
          "tfg:dry_plant_plantable_on",
          "tfg:mars_soil"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "processing_in": {
          "values": [
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:grass_path/alfisol",
      "namespace": "tfg",
      "display_name": "Alkaline Dirt Path",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:paths"
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
        "item/grass_path/alfisol",
        "block/grass_path/alfisol",
        "block/grass_path"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass_path/alfisol",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:mineable/shovel",
          "tfc:can_carve",
          "tfc:can_landslide"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        }
      }
    },
    {
      "id": "tfg:grass_path/mollisol",
      "namespace": "tfg",
      "display_name": "Volcanic Dirt Path",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:paths"
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
        "item/grass_path/mollisol",
        "block/grass_path/mollisol",
        "block/grass_path"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass_path/mollisol",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:mineable/shovel",
          "tfc:can_carve",
          "tfc:can_landslide"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        }
      }
    },
    {
      "id": "tfg:grass_path/oxisol",
      "namespace": "tfg",
      "display_name": "Tropical Dirt Path",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:paths"
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
        "item/grass_path/oxisol",
        "block/grass_path/oxisol",
        "block/grass_path"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass_path/oxisol",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:mineable/shovel",
          "tfc:can_carve",
          "tfc:can_landslide"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        }
      }
    },
    {
      "id": "tfg:grass_path/podzol",
      "namespace": "tfg",
      "display_name": "Layered Dirt Path",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:paths"
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
        "item/grass_path/podzol",
        "block/grass_path/podzol",
        "block/grass_path_column",
        "block/block"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:grass_path/podzol",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:mineable/shovel",
          "tfc:can_carve",
          "tfc:can_landslide"
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
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        }
      }
    },
    {
      "id": "tfg:green_sand_lorandite_ore",
      "namespace": "tfg",
      "display_name": "Green Lorandite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/lorandite",
        "forge:ores_in_ground/green_sand"
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
        "greate:milling/integration/gtceu/macerator/macerate_green_sand_lorandite_ore_to_crushed_ore",
        "gtceu:blasting/smelt_green_sand_lorandite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_lorandite_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:green_sand_lorandite_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/lorandite",
          "forge:ores_in_ground/green_sand",
          "minecraft:mineable/shovel",
          "minecraft:needs_stone_tool",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:can_landslide",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfc:prospectable"
        ],
        "requires_correct_tool": false
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
          "value": "lorandite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id green_sand_lorandite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "tfg:grow_light",
      "namespace": "tfg",
      "display_name": "Grow Light",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shaped": 2
      },
      "recipe_production_by_type": {
        "kubejs:shaped": 1
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_grow_light",
        "tfg:shaped/egh_planter",
        "tfg:shaped/hydroponics_facility"
      ],
      "recipe_output_examples": [
        "tfg:shaped/grow_light"
      ],
      "model_parents": [
        "item/grow_light",
        "block/machines/egh_planter/grow_light"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/grow_light"
      ],
      "block_context": {
        "block_id": "tfg:grow_light",
        "block_tags": [
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§7No. This will not increase your crop growth speed..."
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
        "processing_in": {
          "values": [
            "greate:milling",
            "kubejs:shaped"
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
      "id": "tfg:growth_chamber",
      "namespace": "tfg",
      "display_name": "Growth Chamber",
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
        "item/growth_chamber",
        "block/machine/growth_chamber",
        "block/block"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:growth_chamber",
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
          "text": "§7Growing new life§r"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Contains everything needed for Biological Engineering.§r"
        },
        {
          "source": "runtime-tooltip",
          "text": "Accepts up to §6Two§r Energy Hatches."
        },
        {
          "source": "runtime-tooltip",
          "text": "Can §dParallelize§r with Parallel Control Hatches."
        },
        {
          "source": "runtime-tooltip",
          "text": "This machine can run more recipes at once by increasing the number of layers."
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
        }
      }
    },
    {
      "id": "tfg:growth_monitor",
      "namespace": "tfg",
      "display_name": "Growth Monitor",
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
        "kubejs:shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_growth_monitor"
      ],
      "recipe_output_examples": [
        "tfg:shaped/growth_monitor"
      ],
      "model_parents": [],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/growth_monitor"
      ],
      "block_context": {
        "block_id": "tfg:growth_monitor",
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
        "minecraft:light_emission": 12,
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
        "processing_in": {
          "values": [
            "greate:milling"
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
        },
        "emits_light": {
          "value": true,
          "confidence": 1,
          "source": "rule:emits_light_from_component"
        }
      }
    },
    {
      "id": "tfg:halite",
      "namespace": "tfg",
      "display_name": "Halite",
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
        "item/halite",
        "block/halite",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/halite"
      ],
      "block_context": {
        "block_id": "tfg:halite",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:pipe_replaceable",
          "minecraft:mineable/pickaxe",
          "tfc:can_carve"
        ],
        "requires_correct_tool": true
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
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
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