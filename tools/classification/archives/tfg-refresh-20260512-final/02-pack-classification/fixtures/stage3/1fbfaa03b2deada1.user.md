# Items to classify
{
  "items": [
    {
      "id": "create:yellow_table_cloth",
      "namespace": "create",
      "display_name": "Yellow Table Cloth",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:dyed_table_cloths",
        "create:table_cloths"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "tfc:barrel_sealed": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "create:crafting/logistics/yellow_table_cloth_clear"
      ],
      "recipe_output_examples": [
        "create:crafting/logistics/yellow_table_cloth_clear",
        "minecraft:barrel/create/yellow_table_cloth"
      ],
      "model_parents": [
        "item/yellow_table_cloth",
        "block/table_cloth/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/yellow_table_cloth"
      ],
      "block_context": {
        "block_id": "create:yellow_table_cloth",
        "block_tags": [
          "create:table_cloths",
          "ftbchunks:interact_whitelist",
          "minecraft:combination_step_sound_blocks"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:yellow_toolbox",
      "namespace": "create",
      "display_name": "Yellow Toolbox",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:non_movable",
        "create:toolboxes",
        "tfg:cannot_launch_in_railgun"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:barrel_sealed": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "minecraft:barrel/create/yellow_toolbox"
      ],
      "model_parents": [
        "item/yellow_toolbox",
        "block/toolbox/item"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/yellow_toolbox"
      ],
      "block_context": {
        "block_id": "create:yellow_toolbox",
        "block_tags": [
          "create:non_movable",
          "create:toolboxes"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:yellow_valve_handle",
      "namespace": "create",
      "display_name": "Yellow Valve Handle",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:valve_handles",
        "tfg:colored_valve_handles"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:barrel_sealed": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "minecraft:barrel/create/yellow_valve_handle"
      ],
      "model_parents": [
        "item/yellow_valve_handle",
        "block/yellow_valve_handle",
        "block/valve_handle",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/yellow_valve_handle"
      ],
      "block_context": {
        "block_id": "create:yellow_valve_handle",
        "block_tags": [
          "create:brittle",
          "create:valve_handles",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfg:colored_valve_handles"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:zinc_block",
      "namespace": "create",
      "display_name": "Block of Zinc",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "createdeco:internal/blocks/zinc_blocks",
        "forge:storage_blocks",
        "forge:storage_blocks/zinc",
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
        "createdeco:zinc_hull",
        "greate:cutting/integration/gtceu/cutter/cut_zinc_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_zinc_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_zinc_block_to_plate_water",
        "greate:milling/integration/gtceu/macerator/macerate_zinc_block",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/zinc_block"
      ],
      "model_parents": [
        "item/zinc_block",
        "block/zinc_block",
        "block/cube_all"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/zinc_block"
      ],
      "block_context": {
        "block_id": "create:zinc_block",
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
          "forge:storage_blocks/zinc",
          "minecraft:beacon_base_blocks",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_iron_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Zn"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 1296 mB of §fZinc§7 (at Very Hot٭٭٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
          "value": "iron",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_iron_tool"
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
      "id": "create:zinc_ingot",
      "namespace": "create",
      "display_name": "Zinc Ingot",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ingots",
        "create:create_ingots",
        "createdeco:internal/ingots/zinc_ingots",
        "forge:ingots",
        "forge:ingots/zinc",
        "minecraft:beacon_payment_items",
        "tfc:pileable_ingots"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "createaddition:rolling",
        "greate:compacting",
        "greate:milling",
        "stonecutting",
        "vintageimprovements:coiling",
        "vintageimprovements:polishing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "createaddition:rolling": 1,
        "greate:compacting": 2,
        "greate:milling": 1,
        "stonecutting": 43,
        "vintageimprovements:coiling": 1,
        "vintageimprovements:polishing": 1
      },
      "recipe_production_by_type": {
        "blasting": 46,
        "smelting": 51,
        "tfc:casting": 2
      },
      "recipe_ingredient_count": 51,
      "recipe_output_count": 99,
      "recipe_ingredient_examples": [
        "createdeco:zinc_catwalk_from_stonecutting",
        "createdeco:zinc_catwalk_railing_from_stonecutting",
        "createdeco:zinc_catwalk_stairs_from_stonecutting",
        "createdeco:zinc_facade_from_stonecutting",
        "createdeco:zinc_ladder_from_stonecutting",
        "createdeco:zinc_mesh_fence_from_stonecutting",
        "createdeco:zinc_support",
        "createdeco:zinc_support_wedge_from_stonecutting",
        "greate:compacting/zinc_block",
        "greate:milling/integration/gtceu/macerator/macerate_zinc_ingot",
        "gtceu:shaped/stick_zinc",
        "minecraft:kjs/copycats_copycat_beam",
        "minecraft:kjs/copycats_copycat_block",
        "minecraft:kjs/copycats_copycat_board",
        "minecraft:kjs/copycats_copycat_byte",
        "minecraft:kjs/copycats_copycat_byte_panel",
        "minecraft:kjs/copycats_copycat_corner_slice",
        "minecraft:kjs/copycats_copycat_door",
        "minecraft:kjs/copycats_copycat_fence",
        "minecraft:kjs/copycats_copycat_fence_gate",
        "minecraft:kjs/copycats_copycat_flat_pane",
        "minecraft:kjs/copycats_copycat_folding_door",
        "minecraft:kjs/copycats_copycat_ghost_block",
        "minecraft:kjs/copycats_copycat_half_layer",
        "minecraft:kjs/copycats_copycat_half_panel",
        "minecraft:kjs/copycats_copycat_ladder",
        "minecraft:kjs/copycats_copycat_layer",
        "minecraft:kjs/copycats_copycat_pane",
        "minecraft:kjs/copycats_copycat_slab",
        "minecraft:kjs/copycats_copycat_slice",
        "minecraft:kjs/copycats_copycat_sliding_door",
        "minecraft:kjs/copycats_copycat_slope",
        "minecraft:kjs/copycats_copycat_slope_layer",
        "minecraft:kjs/copycats_copycat_stacked_half_layer",
        "minecraft:kjs/copycats_copycat_stairs",
        "minecraft:kjs/copycats_copycat_trapdoor",
        "minecraft:kjs/copycats_copycat_vertical_half_layer",
        "minecraft:kjs/copycats_copycat_vertical_slice",
        "minecraft:kjs/copycats_copycat_vertical_slope",
        "minecraft:kjs/copycats_copycat_vertical_stairs",
        "minecraft:kjs/copycats_copycat_vertical_step",
        "minecraft:kjs/copycats_copycat_wall",
        "minecraft:kjs/create_copycat_panel",
        "minecraft:kjs/create_copycat_step",
        "minecraft:kjs/createdeco_zinc_bars",
        "minecraft:kjs/createdeco_zinc_bars_overlay",
        "minecraft:kjs/railways_copycat_headstock",
        "tfg:compacting/zinc_doubleIngot",
        "tfg:rolling/zinc_plate",
        "tfg:vi/coiling/zinc_fine_wire",
        "tfg:vi/lathe/zinc_to_rod"
      ],
      "recipe_output_examples": [
        "gtceu:blasting/smelt_andesite_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_basalt_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_black_sand_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_brown_sand_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_chalk_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_chert_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_claystone_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_conglomerate_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_crushed_ore_sphalerite_to_ingot",
        "gtceu:blasting/smelt_dacite_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_deepslate_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_diorite_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_dolomite_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_dripstone_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_flavolite_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_gabbro_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_gneiss_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_granite_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_green_sand_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_impure_dust_sphalerite_to_ingot",
        "gtceu:blasting/smelt_limestone_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_marble_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_mars_stone_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_mercury_stone_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_moon_deepslate_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_moon_stone_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_phyllite_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_pink_sand_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_pure_dust_sphalerite_to_ingot",
        "gtceu:blasting/smelt_purified_ore_sphalerite_to_ingot",
        "gtceu:blasting/smelt_pyroxenite_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_quartzite_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_red_granite_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_red_sand_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_refined_ore_sphalerite_to_ingot",
        "gtceu:blasting/smelt_rhyolite_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_sandy_jadestone_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_schist_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_shale_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_slate_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_tuff_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_venus_stone_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_white_sand_sphalerite_ore_to_ingot",
        "gtceu:blasting/smelt_yellow_sand_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_andesite_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_basalt_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_black_sand_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_chalk_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_chert_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_claystone_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_conglomerate_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_crushed_ore_sphalerite_to_ingot",
        "gtceu:smelting/smelt_dacite_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_deepslate_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_diorite_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_dolomite_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_dripstone_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_dust_sphalerite_to_ingot",
        "gtceu:smelting/smelt_dust_zinc_to_ingot",
        "gtceu:smelting/smelt_flavolite_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_gabbro_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_glacio_stone_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_gneiss_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_granite_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_green_sand_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_impure_dust_sphalerite_to_ingot",
        "gtceu:smelting/smelt_limestone_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_marble_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_mars_stone_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_mercury_stone_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_moon_deepslate_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_moon_stone_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_phyllite_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_pink_sand_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_pure_dust_sphalerite_to_ingot",
        "gtceu:smelting/smelt_purified_ore_sphalerite_to_ingot",
        "gtceu:smelting/smelt_pyroxenite_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_quartzite_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_raw_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_red_sand_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_refined_ore_sphalerite_to_ingot",
        "gtceu:smelting/smelt_rhyolite_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_rich_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_sandy_jadestone_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_schist_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_shale_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_slate_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_tuff_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_venus_stone_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_white_sand_sphalerite_ore_to_ingot",
        "gtceu:smelting/smelt_yellow_sand_sphalerite_ore_to_ingot",
        "minecraft:kjs/create_zinc_ingot",
        "tfg:casting/zinc_ingot_ceramic",
        "tfg:casting/zinc_ingot_fire"
      ],
      "recipe_examples_truncated": true,
      "model_parents": [
        "item/zinc_ingot",
        "item/generated"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Zn"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 144 mB of §fZinc§7 (at Very Hot٭٭٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
            "stonecutting",
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
      "id": "create:zinc_nugget",
      "namespace": "create",
      "display_name": "Zinc Nugget",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:nuggets",
        "createdeco:internal/nuggets/zinc_nuggets",
        "forge:nuggets",
        "forge:nuggets/zinc",
        "railways:internal/nuggets/zinc_nuggets"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "create:pressing",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "create:pressing": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:anvil": 1,
        "tfc:casting": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "createdeco:pressing/coins/zinc_coin",
        "greate:milling/integration/gtceu/macerator/macerate_zinc_nugget"
      ],
      "recipe_output_examples": [
        "gtceu:smelting/smelt_poor_sphalerite_ore_to_ingot",
        "tfc:anvil/zinc_nugget",
        "tfg:casting/zinc_nugget_ceramic"
      ],
      "model_parents": [
        "item/zinc_nugget",
        "item/generated"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Zn"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 16 mB of §fZinc§7 (at Very Hot٭٭٭§7)"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
            "create:pressing",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "create:zinc_ore",
      "namespace": "create",
      "display_name": "Zinc Ore",
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
        "item/zinc_ore",
        "block/zinc_ore",
        "block/cube_all"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/zinc_ore"
      ],
      "block_context": {
        "block_id": "create:zinc_ore",
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
          "value": "zinc",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id zinc_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "origin": {
          "values": [
            "overworld_cave"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
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
      "id": "create_connected:andesite_encased_cross_connector",
      "namespace": "create_connected",
      "display_name": "Andesite Encased Cross Connector",
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
        "item/andesite_encased_cross_connector",
        "block/cross_connector/item_andesite",
        "block/cross_connector/block_andesite",
        "block/block"
      ],
      "creative_tabs": [],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "create_connected:andesite_encased_cross_connector",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Connected"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create_connected",
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
          "value": "andesite",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix andesite_"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        }
      }
    },
    {
      "id": "create_connected:brake",
      "namespace": "create_connected",
      "display_name": "Brake",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "create_connected:crafting/kinetics/brake"
      ],
      "model_parents": [
        "item/brake",
        "block/brake/item",
        "block/block"
      ],
      "creative_tabs": [
        "create_connected:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create_connected:blocks/brake"
      ],
      "block_context": {
        "block_id": "create_connected:brake",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Connected"
        },
        {
          "source": "lang",
          "key": "block.create_connected.brake.tooltip.summary",
          "text": "A device that produces _immense stress_ when powered, halting the network by _overstressing_ it."
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "create_connected:overpowered_brake_0",
          "label": "Overpowered",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Overpowered"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Keep a network running at speed with a powered brake attached (Hidden Advancement)"
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
          "value": "create_connected",
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
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
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
      "id": "create_connected:brass_encased_cross_connector",
      "namespace": "create_connected",
      "display_name": "Brass Encased Cross Connector",
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
        "item/brass_encased_cross_connector",
        "block/cross_connector/item_brass",
        "block/cross_connector/block_brass",
        "block/block"
      ],
      "creative_tabs": [],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "create_connected:brass_encased_cross_connector",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Connected"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create_connected",
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
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        }
      }
    },
    {
      "id": "create_connected:brass_gearbox",
      "namespace": "create_connected",
      "display_name": "Brass Gearbox",
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
        "item/brass_gearbox",
        "block/brass_gearbox/item",
        "block/block"
      ],
      "creative_tabs": [],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create_connected:blocks/brass_gearbox"
      ],
      "block_context": {
        "block_id": "create_connected:brass_gearbox",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Connected"
        },
        {
          "source": "lang",
          "key": "block.create_connected.brass_gearbox.tooltip.summary",
          "text": "A gearbox where the rotation direction of all 4 sides are _independently configurable_."
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "create_connected:brass_gearbox",
          "label": "Serious Organization",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Serious Organization"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Place down a Brass Gearbox"
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
          "value": "create_connected",
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
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        }
      }
    },
    {
      "id": "create_connected:centrifugal_clutch",
      "namespace": "create_connected",
      "display_name": "Centrifugal Clutch",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "create_connected:crafting/kinetics/centrifugal_clutch"
      ],
      "model_parents": [
        "item/centrifugal_clutch",
        "block/centrifugal_clutch/item",
        "block/block"
      ],
      "creative_tabs": [
        "create_connected:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create_connected:blocks/centrifugal_clutch"
      ],
      "block_context": {
        "block_id": "create_connected:centrifugal_clutch",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Connected"
        },
        {
          "source": "lang",
          "key": "block.create_connected.centrifugal_clutch.tooltip.summary",
          "text": "A clutch that is only _coupled_ if the input RPM is _faster_ than a configurable threshold."
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create_connected",
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
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
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
      "id": "create_connected:charged_kinetic_battery",
      "namespace": "create_connected",
      "display_name": "Charged Kinetic Battery",
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
        "item/charged_kinetic_battery",
        "block/kinetic_battery/item_charged",
        "block/kinetic_battery/item",
        "block/block"
      ],
      "creative_tabs": [
        "create_connected:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create_connected:blocks/kinetic_battery"
      ],
      "block_context": {
        "block_id": "create_connected:kinetic_battery",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Connected"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create_connected",
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
      "id": "create_connected:control_chip",
      "namespace": "create_connected",
      "display_name": "Control Chip",
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
        "item/control_chip",
        "item/generated"
      ],
      "creative_tabs": [
        "create_connected:main"
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
          "text": "Create: Connected"
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "create_connected:control_chip",
          "label": "Precise Fabrication",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Precise Fabrication"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Assemble a Control Chip"
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
          "value": "create_connected",
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
      "id": "create_connected:copycat_beam",
      "namespace": "create_connected",
      "display_name": "Copycat Beam",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "create_connected:crafting/palettes/copycat_beam_compat"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_beam",
        "block/copycat_base/beam",
        "block/block"
      ],
      "creative_tabs": [
        "create_connected:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create_connected:blocks/copycat_beam"
      ],
      "block_context": {
        "block_id": "create_connected:copycat_beam",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Connected"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_beam.tooltip.behaviour1",
          "text": "Applies _held item_ as its _material_ if possible. _Click again_ to cycle _orientation_ or _powered_ state. Use a _Wrench_ to _reset_ the material."
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_beam.tooltip.condition1",
          "text": "When R-Clicked"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_beam.tooltip.summary",
          "text": "_Converts_ any _full block_ into a decorative beam."
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create_connected",
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
        }
      }
    },
    {
      "id": "create_connected:copycat_block",
      "namespace": "create_connected",
      "display_name": "Copycat Block",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "create_connected:crafting/palettes/copycat_block_compat"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_block",
        "block/copycat_base/block",
        "block/block"
      ],
      "creative_tabs": [
        "create_connected:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create_connected:blocks/copycat_block"
      ],
      "block_context": {
        "block_id": "create_connected:copycat_block",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Connected"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_block.tooltip.behaviour1",
          "text": "Applies _held item_ as its _material_ if possible. _Click again_ to cycle _orientation_ or _powered_ state. Use a _Wrench_ to _reset_ the material."
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_block.tooltip.condition1",
          "text": "When R-Clicked"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_block.tooltip.summary",
          "text": "_Converts_ any _full block_ into a decorative clone."
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create_connected",
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
        }
      }
    },
    {
      "id": "create_connected:copycat_board",
      "namespace": "create_connected",
      "display_name": "Copycat Board",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "create_connected:crafting/palettes/copycat_board_compat"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_board",
        "block/copycat_base/board",
        "block/block"
      ],
      "creative_tabs": [
        "create_connected:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create_connected:blocks/copycat_board"
      ],
      "block_context": {
        "block_id": "create_connected:copycat_board",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Connected"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_board.tooltip.behaviour1",
          "text": "Applies _held item_ as its _material_ if possible. _Click again_ to cycle _orientation_ or _powered_ state. Use a _Wrench_ to _reset_ the material."
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_board.tooltip.condition1",
          "text": "When R-Clicked"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_board.tooltip.summary",
          "text": "_Converts_ any _full block_ into a decorative board. Multiple boards can be placed in the same block space."
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create_connected",
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
        }
      }
    },
    {
      "id": "create_connected:copycat_box",
      "namespace": "create_connected",
      "display_name": "Copycat Box",
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
        "item/copycat_box",
        "block/copycat_base/box",
        "block/block"
      ],
      "creative_tabs": [
        "create_connected:main"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "create_connected:copycat_board",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Connected"
        },
        {
          "source": "lang",
          "key": "item.create_connected.copycat_box.tooltip.behaviour1",
          "text": "Applies _held item_ as its _material_ if possible. _Click again_ to cycle _orientation_ or _powered_ state. Use a _Wrench_ to _reset_ the material."
        },
        {
          "source": "lang",
          "key": "item.create_connected.copycat_box.tooltip.condition1",
          "text": "When R-Clicked"
        },
        {
          "source": "lang",
          "key": "item.create_connected.copycat_box.tooltip.summary",
          "text": "_Copycat boards_ pre-assembled into a box for convenient placement."
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create_connected",
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
      "id": "create_connected:copycat_catwalk",
      "namespace": "create_connected",
      "display_name": "Copycat Catwalk",
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
        "item/copycat_catwalk",
        "block/copycat_base/catwalk",
        "block/block"
      ],
      "creative_tabs": [
        "create_connected:main"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "create_connected:copycat_board",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Connected"
        },
        {
          "source": "lang",
          "key": "item.create_connected.copycat_catwalk.tooltip.behaviour1",
          "text": "Applies _held item_ as its _material_ if possible. _Click again_ to cycle _orientation_ or _powered_ state. Use a _Wrench_ to _reset_ the material."
        },
        {
          "source": "lang",
          "key": "item.create_connected.copycat_catwalk.tooltip.condition1",
          "text": "When R-Clicked"
        },
        {
          "source": "lang",
          "key": "item.create_connected.copycat_catwalk.tooltip.summary",
          "text": "_Copycat boards_ pre-assembled into a catwalk for convenient placement."
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create_connected",
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
      "id": "create_connected:copycat_fence",
      "namespace": "create_connected",
      "display_name": "Copycat Fence",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "create_connected:crafting/palettes/copycat_fence_compat"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_fence",
        "block/copycat_base/fence",
        "block/block"
      ],
      "creative_tabs": [
        "create_connected:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create_connected:blocks/copycat_fence"
      ],
      "block_context": {
        "block_id": "create_connected:copycat_fence",
        "block_tags": [
          "ad_astra:passes_flood_fill",
          "create:fan_transparent",
          "cucumber:mineable/paxel",
          "diagonalfences:non_diagonal_fences",
          "diggerhelmet:mineable_with_speed_booster",
          "forge:fences",
          "minecraft:fences",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "railways:semaphore_poles",
          "tacz:bullet_ignore"
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
          "text": "Create: Connected"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_fence.tooltip.behaviour1",
          "text": "Applies _held item_ as its _material_ if possible. _Click again_ to cycle _orientation_ or _powered_ state. Use a _Wrench_ to _reset_ the material."
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_fence.tooltip.condition1",
          "text": "When R-Clicked"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_fence.tooltip.summary",
          "text": "_Converts_ any _full block_ into a decorative fence."
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create_connected",
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
          "value": "fence",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _fence"
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
        }
      }
    },
    {
      "id": "create_connected:copycat_fence_gate",
      "namespace": "create_connected",
      "display_name": "Copycat Fence Gate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "create_connected:crafting/palettes/copycat_fence_gate_compat"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_fence_gate",
        "block/copycat_base/fence_gate",
        "block/block"
      ],
      "creative_tabs": [
        "create_connected:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create_connected:blocks/copycat_fence_gate"
      ],
      "block_context": {
        "block_id": "create_connected:copycat_fence_gate",
        "block_tags": [
          "create:movable_empty_collider",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "forge:fence_gates",
          "minecraft:fence_gates",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:unstable_bottom_center",
          "tacz:bullet_ignore",
          "tacz:interact_key/whitelist"
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
          "text": "Create: Connected"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_fence_gate.tooltip.behaviour1",
          "text": "Applies _held item_ as its _material_ if possible. _Click again_ to cycle _orientation_ or _powered_ state. Use a _Wrench_ to _reset_ the material."
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_fence_gate.tooltip.condition1",
          "text": "When R-Clicked"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_fence_gate.tooltip.summary",
          "text": "_Converts_ any _full block_ into a functional fence gate."
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create_connected",
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
          "value": "fence_gate",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _fence_gate"
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
        }
      }
    },
    {
      "id": "create_connected:copycat_slab",
      "namespace": "create_connected",
      "display_name": "Copycat Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "create_connected:crafting/palettes/copycat_slab_compat"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_slab",
        "block/copycat_base/slab",
        "block/block"
      ],
      "creative_tabs": [
        "create_connected:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create_connected:blocks/copycat_slab"
      ],
      "block_context": {
        "block_id": "create_connected:copycat_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs"
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
          "text": "Create: Connected"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_slab.tooltip.behaviour1",
          "text": "Applies _held item_ as its _material_ if possible. _Click again_ to cycle _orientation_ or _powered_ state. Use a _Wrench_ to _reset_ the material."
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_slab.tooltip.condition1",
          "text": "When R-Clicked"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_slab.tooltip.summary",
          "text": "_Converts_ any _full block_ into a decorative slab. Can be placed _vertically_."
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create_connected",
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
          "value": "slab",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _slab"
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
        }
      }
    },
    {
      "id": "create_connected:copycat_stairs",
      "namespace": "create_connected",
      "display_name": "Copycat Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "create_connected:crafting/palettes/copycat_stairs_compat"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_stairs",
        "block/copycat_base/stairs",
        "block/block"
      ],
      "creative_tabs": [
        "create_connected:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create_connected:blocks/copycat_stairs"
      ],
      "block_context": {
        "block_id": "create_connected:copycat_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs"
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
          "text": "Create: Connected"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_stairs.tooltip.behaviour1",
          "text": "Applies _held item_ as its _material_ if possible. _Click again_ to cycle _orientation_ or _powered_ state. Use a _Wrench_ to _reset_ the material."
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_stairs.tooltip.condition1",
          "text": "When R-Clicked"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_stairs.tooltip.summary",
          "text": "_Converts_ any _full block_ into decorative stairs. Note: buggy connected textures."
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create_connected",
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
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _stairs"
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
        }
      }
    },
    {
      "id": "create_connected:copycat_vertical_step",
      "namespace": "create_connected",
      "display_name": "Copycat Vertical Step",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "create_connected:crafting/palettes/copycat_vertical_step_compat"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_vertical_step",
        "block/copycat_base/vertical_step",
        "block/block"
      ],
      "creative_tabs": [
        "create_connected:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create_connected:blocks/copycat_vertical_step"
      ],
      "block_context": {
        "block_id": "create_connected:copycat_vertical_step",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Connected"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_vertical_step.tooltip.behaviour1",
          "text": "Applies _held item_ as its _material_ if possible. _Click again_ to cycle _orientation_ or _powered_ state. Use a _Wrench_ to _reset_ the material."
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_vertical_step.tooltip.condition1",
          "text": "When R-Clicked"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_vertical_step.tooltip.summary",
          "text": "_Converts_ any _full block_ into a decorative vertical step."
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create_connected",
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
        }
      }
    },
    {
      "id": "create_connected:copycat_wall",
      "namespace": "create_connected",
      "display_name": "Copycat Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "create_connected:crafting/palettes/copycat_wall_compat"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_wall",
        "block/copycat_base/wall",
        "block/block"
      ],
      "creative_tabs": [
        "create_connected:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create_connected:blocks/copycat_wall"
      ],
      "block_context": {
        "block_id": "create_connected:copycat_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:walls"
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
          "text": "Create: Connected"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_wall.tooltip.behaviour1",
          "text": "Applies _held item_ as its _material_ if possible. _Click again_ to cycle _orientation_ or _powered_ state. Use a _Wrench_ to _reset_ the material."
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_wall.tooltip.condition1",
          "text": "When R-Clicked"
        },
        {
          "source": "lang",
          "key": "block.create_connected.copycat_wall.tooltip.summary",
          "text": "_Converts_ any _full block_ into a decorative wall."
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create_connected",
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
          "value": "wall",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _wall"
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