# Items to classify
{
  "items": [
    {
      "id": "minecraft:squid_spawn_egg",
      "namespace": "minecraft",
      "display_name": "Squid Spawn Egg",
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
      "model_parents": [],
      "creative_tabs": [
        "minecraft:spawn_eggs"
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
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "is_creative_only": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_creative_only_hardcoded",
          "rationale": "spawn egg id pattern"
        }
      }
    },
    {
      "id": "minecraft:stick",
      "namespace": "minecraft",
      "display_name": "Stick",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:wooden_rods",
        "forge:rods",
        "forge:rods/wood",
        "forge:rods/wooden",
        "tfc:can_be_lit_on_torch",
        "tfc:firepit_sticks",
        "tumbleweed:drop_common"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:mechanical_crafting",
        "greate:compacting",
        "greate:cutting",
        "greate:milling",
        "greate:mixing",
        "kubejs:shaped",
        "tfc:advanced_shaped_crafting",
        "tfc:advanced_shapeless_crafting",
        "tfc:damage_inputs_shaped_crafting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 421,
        "crafting_shapeless": 5,
        "create:mechanical_crafting": 1,
        "greate:compacting": 1,
        "greate:cutting": 3,
        "greate:milling": 1,
        "greate:mixing": 8,
        "kubejs:shaped": 2,
        "tfc:advanced_shaped_crafting": 63,
        "tfc:advanced_shapeless_crafting": 209,
        "tfc:damage_inputs_shaped_crafting": 1,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 3,
        "crafting_shapeless": 6,
        "firmalife:vat": 5,
        "greate:cutting": 4
      },
      "recipe_ingredient_count": 716,
      "recipe_output_count": 18,
      "recipe_ingredient_examples": [
        "afc:crafting/wood/baobab_bookshelf",
        "afc:crafting/wood/cypress_sign",
        "afc:crafting/wood/fig_bookshelf",
        "afc:crafting/wood/hevea_sign",
        "afc:crafting/wood/ironwood_bookshelf",
        "afc:crafting/wood/mahogany_sign",
        "afc:crafting/wood/tualang_bookshelf",
        "beneath:crafting/wood/crimson_loom",
        "beneath:crafting/wood/warped_sign",
        "framedblocks:framed_cube",
        "framedblocks:framed_screwdriver",
        "greate:mixing/integration/gtceu/loam_to_rooted",
        "gtceu:shaped/axe_bismuth_bronze",
        "gtceu:shaped/axe_steel",
        "gtceu:shaped/butchery_knife_blue_steel",
        "gtceu:shaped/butchery_knife_neutronium",
        "gtceu:shaped/butchery_knife_wrought_iron",
        "gtceu:shaped/file_diamond_tipped_mo_50_re",
        "gtceu:shaped/file_steel",
        "gtceu:shaped/hammer_black_steel",
        "gtceu:shaped/hammer_naquadah_alloy",
        "gtceu:shaped/hammer_ultimet",
        "gtceu:shaped/hoe_bronze",
        "gtceu:shaped/hoe_neutronium",
        "gtceu:shaped/hoe_wrought_iron",
        "gtceu:shaped/knife_copper",
        "gtceu:shaped/knife_red_steel",
        "gtceu:shaped/ladder",
        "gtceu:shaped/mining_hammer_steel",
        "gtceu:shaped/pickaxe_copper",
        "gtceu:shaped/saw_bismuth_bronze",
        "gtceu:shaped/saw_steel",
        "gtceu:shaped/screwdriver_neutronium",
        "gtceu:shaped/scythe_blue_steel",
        "gtceu:shaped/scythe_neutronium",
        "gtceu:shaped/scythe_wrought_iron",
        "gtceu:shaped/shovel_red_steel",
        "gtceu:shaped/soft_mallet_wood",
        "gtceu:shaped/spade_steel",
        "gtceu:shaped/sword_boron_carbide",
        "gtceu:shaped/sword_naquadah_alloy",
        "gtceu:shaped/sword_wrought_iron",
        "gtceu:shaped/tripwire_hook",
        "mcw_tfc_aio:fences/ash_fences/ash_highley_gate",
        "mcw_tfc_aio:fences/aspen_fences/aspen_pyramid_gate",
        "mcw_tfc_aio:fences/blackwood_fences/blackwood_pyramid_gate",
        "mcw_tfc_aio:fences/douglas_fir_fences/douglas_fir_picket_fence",
        "mcw_tfc_aio:fences/kapok_fences/kapok_picket_fence",
        "mcw_tfc_aio:fences/maple_fences/maple_horse_fence",
        "mcw_tfc_aio:fences/palm_fences/palm_highley_gate",
        "mcw_tfc_aio:fences/pine_fences/pine_pyramid_gate",
        "mcw_tfc_aio:fences/sequoia_fences/sequoia_pyramid_gate",
        "mcw_tfc_aio:fences/sycamore_fences/sycamore_picket_fence",
        "mcw_tfc_aio:fences/willow_fences/willow_picket_fence",
        "mcw_tfc_aio:furniture/aspen_furniture/stripped_aspen_modern_desk",
        "mcw_tfc_aio:furniture/chestnut_furniture/stripped_chestnut_modern_desk",
        "mcw_tfc_aio:furniture/mangrove_furniture/mangrove_modern_desk",
        "mcw_tfc_aio:furniture/pine_furniture/pine_modern_desk",
        "mcw_tfc_aio:furniture/sycamore_furniture/stripped_sycamore_modern_desk",
        "mcw_tfc_aio:roofs/thatch2_roofs/thatch2_lower_roof",
        "mcw_tfc_aio:roofs/thatch_roofs/thatch_steep_roof",
        "rnr:crafting/metal/mattock/blue_steel",
        "tfc:crafting/handstone",
        "tfc:crafting/metal/chisel/red_steel",
        "tfc:crafting/metal/fishing_rod/copper",
        "tfc:crafting/metal/javelin/blue_steel",
        "tfc:crafting/metal/mace/black_steel",
        "tfc:crafting/metal/propick/bismuth_bronze",
        "tfc:crafting/metal/propick/wrought_iron",
        "tfc:crafting/stone/javelin_sedimentary",
        "tfc:crafting/wood/acacia_bookshelf",
        "tfc:crafting/wood/ash_shelf",
        "tfc:crafting/wood/birch_bookshelf",
        "tfc:crafting/wood/blackwood_shelf",
        "tfc:crafting/wood/douglas_fir_bookshelf",
        "tfc:crafting/wood/hickory_shelf",
        "tfc:crafting/wood/mangrove_bookshelf",
        "tfc:crafting/wood/maple_sign",
        "tfc:crafting/wood/palm_bookshelf",
        "tfc:crafting/wood/pine_sign",
        "tfc:crafting/wood/sequoia_bookshelf",
        "tfc:crafting/wood/spruce_sign",
        "tfc:crafting/wood/white_cedar_bookshelf",
        "tfc:crafting/wood/willow_sign",
        "tfcscraping:crafting/metal/scraping_knife/black_steel",
        "tfg:crafting/certus_quartz_cutting_knife",
        "tfg:create/shaped/wrench",
        "tfg:shaped/aeronos_sluice",
        "tfg:shaped/arrow_shard",
        "tfg:shaped/birch_wired_fence",
        "tfg:shaped/ginkgo_loom",
        "tfg:shaped/hickory_wired_fence",
        "tfg:shaped/mangrove_wired_fence",
        "tfg:shaped/sequoia_wired_fence",
        "tfg:shaped/strophar_sign",
        "tfg:shapeless/unstained_wattle"
      ],
      "recipe_output_examples": [
        "afc:vat/birch_concentrate",
        "afc:vat/birch_syrup",
        "afc:vat/maple_concentrate",
        "afc:vat/maple_syrup",
        "afc:vat/maple_syrup_half_batch",
        "greate:cutting/integration/gtceu/cutter/cut_wood_long_rod_to_rod",
        "greate:cutting/integration/gtceu/cutter/cut_wood_long_rod_to_rod_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_wood_long_rod_to_rod_water",
        "greate:cutting/integration/tfg/saplings_to_sticks",
        "gtceu:shaped/stick_long_wood",
        "gtceu:shaped/stick_saw",
        "minecraft:stick_from_bamboo_item",
        "primitive_creatures:erg",
        "tfc:crafting/stick_from_bunch",
        "tfc:crafting/stick_from_bundle",
        "tfc:crafting/wood/stick_from_twigs",
        "tfg:shapeless/driftwood_to_stick",
        "tfg:strip_saplings"
      ],
      "recipe_examples_truncated": true,
      "model_parents": [],
      "creative_tabs": [
        "minecraft:ingredients",
        "tfc:earth"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
            "create:mechanical_crafting",
            "greate:compacting",
            "greate:cutting",
            "greate:milling",
            "greate:mixing",
            "kubejs:shaped",
            "tfc:advanced_shaped_crafting",
            "tfc:advanced_shapeless_crafting",
            "tfc:damage_inputs_shaped_crafting",
            "tfc:damage_inputs_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "is_fuel": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_fuel_from_id_or_tag"
        }
      }
    },
    {
      "id": "minecraft:sticky_piston",
      "namespace": "minecraft",
      "display_name": "Sticky Piston",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:pistons"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_sticky_piston",
        "tfc:crafting/unsticky_piston"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/sticky_piston"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:redstone_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:sticky_piston",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "forge:mineable/wrench",
          "framedblocks:blacklisted",
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
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
      "id": "minecraft:stone",
      "namespace": "minecraft",
      "display_name": "Reconstituted Stone",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:stones",
        "bookshelf:stones",
        "forge:ore_bearing_ground/stone",
        "forge:stone",
        "forge:storage_blocks",
        "forge:storage_blocks/stone",
        "tfg:stone_composition/stone",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:sandpaper_polishing",
        "greate:cutting",
        "greate:milling",
        "greate:pressing",
        "kubejs:shapeless",
        "stonecutting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 8,
        "crafting_shapeless": 1,
        "create:sandpaper_polishing": 1,
        "greate:cutting": 3,
        "greate:milling": 2,
        "greate:pressing": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 3,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 23,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "create:crafting/logistics/powered_latch",
        "create:crafting/logistics/powered_toggle_latch",
        "create:crafting/logistics/pulse_extender",
        "create:crafting/logistics/pulse_repeater",
        "create_connected:crafting/kinetics/sequenced_pulse_generator",
        "framedblocks:framed_stone_button",
        "greate:cutting/integration/gtceu/cutter/cut_stone_into_slab",
        "greate:cutting/integration/gtceu/cutter/cut_stone_into_slab_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_stone_into_slab_water",
        "greate:milling/integration/gtceu/macerator/macerate_stone",
        "greate:milling/integration/tfg/macerate_stone",
        "greate:pressing/stone_raw_to_cobble",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfc:crafting/handstone",
        "tfc:crafting/quern",
        "tfg:polishing/stone_raw_to_polished",
        "tfg:shaped/pulse_timer",
        "tfg:shapeless/stone_raw_to_polished",
        "tfg:stonecutter/minecraft_stone_to_minecraft_stone_stairs",
        "tfg:stonecutting/minecraft_stone_to_minecraft_stone_slab",
        "tfg:stonecutting/minecraft_stone_to_tfg_rock_stone_wall"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:building_blocks",
        "minecraft:natural_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:stone",
        "block_tags": [
          "beneath:event_replaceable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
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
          "endermanoverhaul:cave_enderman_holdable",
          "firmalife:oven_insulation",
          "firmalife:pipe_replaceable",
          "forge:ore_bearing_ground/stone",
          "forge:stone",
          "forge:storage_blocks",
          "forge:storage_blocks/stone",
          "minecraft:azalea_root_replaceable",
          "minecraft:base_stone_overworld",
          "minecraft:dripstone_replaceable_blocks",
          "minecraft:goats_spawnable_on",
          "minecraft:lush_ground_replaceable",
          "minecraft:mineable/pickaxe",
          "minecraft:moss_replaceable",
          "minecraft:nether_carver_replaceables",
          "minecraft:overworld_carver_replaceables",
          "minecraft:sculk_replaceable",
          "minecraft:sculk_replaceable_world_gen",
          "minecraft:snaps_goat_horn",
          "minecraft:stone_ore_replaceables",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:bloomery_insulation",
          "tfc:can_carve",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfc:creeping_plantable_on",
          "tfc:creeping_stone_plantable_on",
          "tfc:forge_insulation",
          "tfc:kaolin_clay_replaceable",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfg:anemone_plantable_on",
          "tfg:epiphyte_plantable_on"
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
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
          "value": "stone",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "exact id minecraft:stone"
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
            "create:sandpaper_polishing",
            "greate:cutting",
            "greate:milling",
            "greate:pressing",
            "kubejs:shapeless",
            "stonecutting",
            "tfc:damage_inputs_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "overworld_cave",
            "overworld_surface"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:stone_axe",
      "namespace": "minecraft",
      "display_name": "Stone Axe",
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
      "model_parents": [],
      "creative_tabs": [
        "minecraft:combat",
        "minecraft:tools_and_utilities"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+8 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-3.2 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 131,
        "minecraft:enchantable": {},
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "has_durability": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_durability_from_component"
        },
        "has_enchantments": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_enchantments_from_component"
        },
        "material_family": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:material_family_from_tool_prefix",
          "rationale": "stone_axe"
        },
        "form": {
          "value": "tool",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _axe"
        },
        "tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:tier_from_tool_prefix",
          "rationale": "stone_axe"
        }
      }
    },
    {
      "id": "minecraft:stone_brick_slab",
      "namespace": "minecraft",
      "display_name": "Stone Brick Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:slabs",
        "tfg:brick_index",
        "tfg:brick_slabs",
        "tfg:interaction/brick_slab",
        "tfg:stone_composition/stone_half",
        "tfg:stone_types/reconstituted_stone_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 21,
        "greate:milling": 1,
        "stonecutting": 1
      },
      "recipe_production_by_type": {
        "greate:cutting": 3,
        "stonecutting": 3,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 23,
      "recipe_output_count": 7,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_stone_half",
        "tfg:item_application/bismuth_bronze_plated_slab",
        "tfg:item_application/bismuth_plated_slab",
        "tfg:item_application/black_bronze_plated_slab",
        "tfg:item_application/black_steel_plated_slab",
        "tfg:item_application/blue_steel_plated_slab",
        "tfg:item_application/brass_plated_slab",
        "tfg:item_application/bronze_plated_slab",
        "tfg:item_application/chromium_plated_slab",
        "tfg:item_application/copper_plated_slab",
        "tfg:item_application/gold_plated_slab",
        "tfg:item_application/iron_plated_slab",
        "tfg:item_application/nickel_plated_slab",
        "tfg:item_application/red_steel_plated_slab",
        "tfg:item_application/rose_gold_plated_slab",
        "tfg:item_application/silver_plated_slab",
        "tfg:item_application/stainless_steel_plated_slab",
        "tfg:item_application/steel_plated_slab",
        "tfg:item_application/sterling_silver_plated_slab",
        "tfg:item_application/tin_plated_slab",
        "tfg:item_application/wrought_iron_plated_slab",
        "tfg:item_application/zinc_plated_slab",
        "tfg:stonecutter/minecraft_smooth_stone_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_stone_brick_into_slab",
        "greate:cutting/integration/gtceu/cutter/cut_stone_brick_into_slab_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_stone_brick_into_slab_water",
        "tfg:chisel/minecraft_stone_bricks_to_minecraft_stone_brick_slab",
        "tfg:stonecutter/minecraft_stone_brick_slab_half",
        "tfg:stonecutter/minecraft_stone_brick_slab_slab_to_slab",
        "tfg:stonecutting/minecraft_stone_bricks_to_minecraft_stone_brick_slab"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:building_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:stone_brick_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:slabs"
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
            "greate:milling",
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "minecraft:stone_brick_stairs",
      "namespace": "minecraft",
      "display_name": "Stone Brick Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:stairs",
        "tfg:brick_index",
        "tfg:brick_stairs",
        "tfg:interaction/brick_stairs",
        "tfg:stone_composition/stone",
        "tfg:stone_types/reconstituted_stone"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 21,
        "greate:milling": 1,
        "stonecutting": 8
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_stone",
        "tfg:item_application/bismuth_bronze_plated_stair",
        "tfg:item_application/bismuth_plated_stair",
        "tfg:item_application/black_bronze_plated_stair",
        "tfg:item_application/black_steel_plated_stair",
        "tfg:item_application/blue_steel_plated_stair",
        "tfg:item_application/brass_plated_stair",
        "tfg:item_application/bronze_plated_stair",
        "tfg:item_application/chromium_plated_stair",
        "tfg:item_application/copper_plated_stair",
        "tfg:item_application/gold_plated_stair",
        "tfg:item_application/iron_plated_stair",
        "tfg:item_application/nickel_plated_stair",
        "tfg:item_application/red_steel_plated_stair",
        "tfg:item_application/rose_gold_plated_stair",
        "tfg:item_application/silver_plated_stair",
        "tfg:item_application/stainless_steel_plated_stair",
        "tfg:item_application/steel_plated_stair",
        "tfg:item_application/sterling_silver_plated_stair",
        "tfg:item_application/tin_plated_stair",
        "tfg:item_application/wrought_iron_plated_stair",
        "tfg:item_application/zinc_plated_stair",
        "tfg:stonecutter/minecraft_chiseled_stone_bricks",
        "tfg:stonecutter/minecraft_smooth_stone",
        "tfg:stonecutter/minecraft_smooth_stone_slab_half",
        "tfg:stonecutter/minecraft_stone_brick_slab_half",
        "tfg:stonecutter/minecraft_stone_brick_wall",
        "tfg:stonecutter/minecraft_stone_bricks",
        "tfg:stonecutter/tfg_rock_smooth_stone_stairs",
        "tfg:stonecutter/tfg_rock_smooth_stone_wall"
      ],
      "recipe_output_examples": [
        "tfg:chisel/minecraft_stone_bricks_to_minecraft_stone_brick_stairs",
        "tfg:stonecutter/minecraft_stone_brick_stairs",
        "tfg:stonecutter/minecraft_stone_bricks_to_minecraft_stone_brick_stairs"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:building_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:stone_brick_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:stairs"
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
            "greate:milling",
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "minecraft:stone_brick_wall",
      "namespace": "minecraft",
      "display_name": "Stone Brick Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:walls",
        "tfg:brick_index",
        "tfg:brick_walls",
        "tfg:interaction/brick_wall",
        "tfg:stone_composition/stone_half",
        "tfg:stone_types/reconstituted_stone"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 2,
        "stonecutting": 8
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 10,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_stone_brick_wall",
        "greate:milling/integration/tfg/macerate_stone_half",
        "tfg:stonecutter/minecraft_chiseled_stone_bricks",
        "tfg:stonecutter/minecraft_smooth_stone",
        "tfg:stonecutter/minecraft_smooth_stone_slab_half",
        "tfg:stonecutter/minecraft_stone_brick_slab_half",
        "tfg:stonecutter/minecraft_stone_brick_stairs",
        "tfg:stonecutter/minecraft_stone_bricks",
        "tfg:stonecutter/tfg_rock_smooth_stone_stairs",
        "tfg:stonecutter/tfg_rock_smooth_stone_wall"
      ],
      "recipe_output_examples": [
        "tfc:kjs/ahkuit490vbqhue0pnn7ozmx",
        "tfg:stonecutter/minecraft_stone_brick_wall",
        "tfg:stonecutting/minecraft_stone_bricks_to_minecraft_stone_brick_wall"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:building_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:stone_brick_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
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
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:walls"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "processing_in": {
          "values": [
            "greate:milling",
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "minecraft:stone_bricks",
      "namespace": "minecraft",
      "display_name": "Reconstituted Stone Bricks",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:stone_bricks",
        "minecraft:stone_bricks",
        "tfc:rock/bricks",
        "tfg:brick_index",
        "tfg:interaction/brick",
        "tfg:stone_composition/stone",
        "tfg:stone_types/reconstituted_stone",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:deploying",
        "create:sandpaper_polishing",
        "greate:cutting",
        "greate:milling",
        "greate:mixing",
        "greate:pressing",
        "kubejs:shapeless",
        "stonecutting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "crafting_shapeless": 21,
        "create:deploying": 1,
        "create:sandpaper_polishing": 1,
        "greate:cutting": 3,
        "greate:milling": 1,
        "greate:mixing": 3,
        "greate:pressing": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 11,
        "tfc:damage_inputs_shapeless_crafting": 2
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "create:deploying": 3,
        "create:sequenced_assembly": 1,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 49,
      "recipe_output_count": 6,
      "recipe_ingredient_examples": [
        "firmalife:crafting/sealed_bricks",
        "greate:cutting/integration/gtceu/cutter/cut_stone_brick_into_slab",
        "greate:cutting/integration/gtceu/cutter/cut_stone_brick_into_slab_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_stone_brick_into_slab_water",
        "greate:milling/integration/tfg/macerate_stone",
        "greate:mixing/integration/gtceu/mixer/mossy_stone_bricks_from_moss_block",
        "greate:mixing/integration/gtceu/mixer/mossy_stone_bricks_from_vine",
        "greate:mixing/integration/tfg/stone_bricks_to_mossy_bricks",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "gtceu:shaped/stone_brick_hammer",
        "tfg:deploying/stone_bricks_to_mossy_bricks",
        "tfg:polishing/stone_brick_to_polished",
        "tfg:pressing/stone_bricks_to_cracked",
        "tfg:shapeless/bismuth_bronze_plated_block",
        "tfg:shapeless/bismuth_plated_block",
        "tfg:shapeless/black_bronze_plated_block",
        "tfg:shapeless/black_steel_plated_block",
        "tfg:shapeless/blue_steel_plated_block",
        "tfg:shapeless/brass_plated_block",
        "tfg:shapeless/bronze_plated_block",
        "tfg:shapeless/chromium_plated_block",
        "tfg:shapeless/copper_plated_block",
        "tfg:shapeless/gold_plated_block",
        "tfg:shapeless/iron_plated_block",
        "tfg:shapeless/nickel_plated_block",
        "tfg:shapeless/red_steel_plated_block",
        "tfg:shapeless/rose_gold_plated_block",
        "tfg:shapeless/silver_plated_block",
        "tfg:shapeless/stainless_steel_plated_block",
        "tfg:shapeless/steel_plated_block",
        "tfg:shapeless/sterling_silver_plated_block",
        "tfg:shapeless/stone_bricks_to_chiseled",
        "tfg:shapeless/stone_bricks_to_cracked",
        "tfg:shapeless/tin_plated_block",
        "tfg:shapeless/wrought_iron_plated_block",
        "tfg:shapeless/zinc_plated_block",
        "tfg:stonecutter/minecraft_chiseled_stone_bricks",
        "tfg:stonecutter/minecraft_smooth_stone",
        "tfg:stonecutter/minecraft_smooth_stone_slab_half",
        "tfg:stonecutter/minecraft_stone_brick_slab_half",
        "tfg:stonecutter/minecraft_stone_brick_stairs",
        "tfg:stonecutter/minecraft_stone_brick_wall",
        "tfg:stonecutter/minecraft_stone_bricks_to_minecraft_stone_brick_stairs",
        "tfg:stonecutter/tfg_rock_smooth_stone_stairs",
        "tfg:stonecutter/tfg_rock_smooth_stone_wall",
        "tfg:stonecutting/minecraft_stone_bricks_to_minecraft_stone_brick_slab",
        "tfg:stonecutting/minecraft_stone_bricks_to_minecraft_stone_brick_wall"
      ],
      "recipe_output_examples": [
        "tfg:deploying/stone_brick_to_bricks",
        "tfg:deploying/stone_cracked_bricks_to_bricks",
        "tfg:deploying/stone_mossy_bricks_to_bricks_knife",
        "tfg:deploying/stone_mossy_bricks_to_bricks_pumice",
        "tfg:shaped/stone_brick_to_bricks",
        "tfg:stonecutter/minecraft_stone_bricks"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:building_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:stone_bricks",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
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
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:stone_bricks",
          "tfc:bloomery_insulation",
          "tfc:forge_insulation"
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
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
            "crafting",
            "create:deploying",
            "create:sandpaper_polishing",
            "greate:cutting",
            "greate:milling",
            "greate:mixing",
            "greate:pressing",
            "kubejs:shapeless",
            "stonecutting",
            "tfc:damage_inputs_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "minecraft:stone_button",
      "namespace": "minecraft",
      "display_name": "Stone Button",
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
      "model_parents": [],
      "creative_tabs": [
        "minecraft:building_blocks",
        "minecraft:redstone_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:stone_button",
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
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
          "value": "button",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _button"
        }
      }
    },
    {
      "id": "minecraft:stone_hoe",
      "namespace": "minecraft",
      "display_name": "Stone Hoe",
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
      "model_parents": [],
      "creative_tabs": [
        "minecraft:tools_and_utilities"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "-2 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 131,
        "minecraft:enchantable": {},
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "has_durability": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_durability_from_component"
        },
        "has_enchantments": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_enchantments_from_component"
        },
        "material_family": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:material_family_from_tool_prefix",
          "rationale": "stone_hoe"
        },
        "form": {
          "value": "tool",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _hoe"
        },
        "tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:tier_from_tool_prefix",
          "rationale": "stone_hoe"
        }
      }
    },
    {
      "id": "minecraft:stone_pickaxe",
      "namespace": "minecraft",
      "display_name": "Stone Pickaxe",
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
      "model_parents": [],
      "creative_tabs": [
        "minecraft:tools_and_utilities"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+2 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-2.8 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 131,
        "minecraft:enchantable": {},
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "has_durability": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_durability_from_component"
        },
        "has_enchantments": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_enchantments_from_component"
        },
        "material_family": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:material_family_from_tool_prefix",
          "rationale": "stone_pickaxe"
        },
        "form": {
          "value": "tool",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _pickaxe"
        },
        "tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:tier_from_tool_prefix",
          "rationale": "stone_pickaxe"
        }
      }
    },
    {
      "id": "minecraft:stone_pressure_plate",
      "namespace": "minecraft",
      "display_name": "Stone Pressure Plate",
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
      "model_parents": [],
      "creative_tabs": [
        "minecraft:building_blocks",
        "minecraft:redstone_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:stone_pressure_plate",
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Can be placed on ceilings"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
          "value": "pressure_plate",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _pressure_plate"
        }
      }
    },
    {
      "id": "minecraft:stone_shovel",
      "namespace": "minecraft",
      "display_name": "Stone Shovel",
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
      "model_parents": [],
      "creative_tabs": [
        "minecraft:tools_and_utilities"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+2.5 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-3 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 131,
        "minecraft:enchantable": {},
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "has_durability": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_durability_from_component"
        },
        "has_enchantments": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_enchantments_from_component"
        },
        "material_family": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:material_family_from_tool_prefix",
          "rationale": "stone_shovel"
        },
        "form": {
          "value": "tool",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _shovel"
        },
        "tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:tier_from_tool_prefix",
          "rationale": "stone_shovel"
        }
      }
    },
    {
      "id": "minecraft:stone_slab",
      "namespace": "minecraft",
      "display_name": "Reconstituted Stone Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:sleepers",
        "minecraft:slabs",
        "tfg:stone_composition/stone_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:cutting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:cutting": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "greate:cutting": 3,
        "stonecutting": 1,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 5,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/tfg/vanilla_stone_slab_to_plate",
        "greate:milling/integration/tfg/macerate_stone_half"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_stone_into_slab",
        "greate:cutting/integration/gtceu/cutter/cut_stone_into_slab_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_stone_into_slab_water",
        "tfg:chisel/minecraft_stone_to_minecraft_stone_slab",
        "tfg:stonecutting/minecraft_stone_to_minecraft_stone_slab"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:building_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:stone_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_slabs"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:slabs"
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "minecraft:stone_stairs",
      "namespace": "minecraft",
      "display_name": "Reconstituted Stone Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:stairs",
        "tfg:stone_composition/stone"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 1,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_stone"
      ],
      "recipe_output_examples": [
        "tfg:chisel/minecraft_stone_to_minecraft_stone_stairs",
        "tfg:stonecutter/minecraft_stone_to_minecraft_stone_stairs"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:building_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:stone_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_stairs"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:stairs"
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
      "id": "minecraft:stone_sword",
      "namespace": "minecraft",
      "display_name": "Stone Sword",
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
      "model_parents": [],
      "creative_tabs": [
        "minecraft:combat"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When in Main Hand:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+4 Attack Damage"
        },
        {
          "source": "runtime-tooltip",
          "text": "-2.4 Attack Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 131,
        "minecraft:enchantable": {},
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "has_durability": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_durability_from_component"
        },
        "has_enchantments": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_enchantments_from_component"
        },
        "material_family": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:material_family_from_tool_prefix",
          "rationale": "stone_sword"
        },
        "form": {
          "value": "weapon",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _sword"
        },
        "tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:tier_from_tool_prefix",
          "rationale": "stone_sword"
        }
      }
    },
    {
      "id": "minecraft:stonecutter",
      "namespace": "minecraft",
      "display_name": "Stonecutter",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "alekiships:can_place_in_compartments"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "tfg:sophisticated_backpacks/shaped/stonecutter_upgrade"
      ],
      "recipe_output_examples": [
        "tfg:shaped/stonecutter"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:functional_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:stonecutter",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
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
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
            "crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "minecraft:stray_spawn_egg",
      "namespace": "minecraft",
      "display_name": "Stray Spawn Egg",
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
      "model_parents": [],
      "creative_tabs": [
        "minecraft:spawn_eggs"
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
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "is_creative_only": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_creative_only_hardcoded",
          "rationale": "spawn egg id pattern"
        }
      }
    },
    {
      "id": "minecraft:strider_spawn_egg",
      "namespace": "minecraft",
      "display_name": "Strider Spawn Egg",
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
      "model_parents": [],
      "creative_tabs": [
        "minecraft:spawn_eggs"
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
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "is_creative_only": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_creative_only_hardcoded",
          "rationale": "spawn egg id pattern"
        }
      }
    },
    {
      "id": "minecraft:string",
      "namespace": "minecraft",
      "display_name": "Silk Thread",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "bookshelf:strings",
        "c:string",
        "forge:string",
        "railways:internal/string",
        "tumbleweed:drop_common"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "computercraft:impostor_shapeless",
        "crafting",
        "create:item_application",
        "kubejs:shapeless",
        "tfc:advanced_shaped_crafting",
        "tfc:damage_inputs_shaped_crafting",
        "tfc:damage_inputs_shapeless_crafting",
        "tfc:no_remainder_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "computercraft:impostor_shapeless": 2,
        "crafting_shaped": 140,
        "crafting_shapeless": 2,
        "create:item_application": 1,
        "kubejs:shapeless": 2,
        "tfc:advanced_shaped_crafting": 9,
        "tfc:damage_inputs_shaped_crafting": 7,
        "tfc:damage_inputs_shapeless_crafting": 10,
        "tfc:no_remainder_shapeless_crafting": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 174,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "afc:crafting/wood/baobab_hanger",
        "afc:crafting/wood/eucalyptus_hanger",
        "afc:crafting/wood/hevea_hanger",
        "afc:crafting/wood/ipe_hanger",
        "afc:crafting/wood/mahogany_hanger",
        "afc:crafting/wood/tualang_hanger",
        "computercraft:printed_pages",
        "create:item_application/bound_cardboard_inworld",
        "createdeco:brass_mesh_fence",
        "createdeco:copper_mesh_fence",
        "createdeco:iron_mesh_fence",
        "firmaciv:crafting/kayak",
        "firmaciv:crafting/small_triangular_sail",
        "firmalife:crafting/wood/acacia_hanger",
        "firmalife:crafting/wood/ash_hanger",
        "firmalife:crafting/wood/birch_hanger",
        "firmalife:crafting/wood/chestnut_hanger",
        "firmalife:crafting/wood/hickory_hanger",
        "firmalife:crafting/wood/mangrove_hanger",
        "firmalife:crafting/wood/oak_hanger",
        "firmalife:crafting/wood/palm_hanger",
        "firmalife:crafting/wood/rosewood_hanger",
        "firmalife:crafting/wood/spruce_hanger",
        "firmalife:crafting/wood/white_cedar_hanger",
        "gtceu:shaped/face_mask",
        "gtceu:shaped/tripwire_hook",
        "mcw_tfc_aio:bridges/acacia_bridges/rope_acacia_bridge",
        "mcw_tfc_aio:bridges/aspen_bridges/rope_aspen_bridge",
        "mcw_tfc_aio:bridges/bamboo_bridges/bamboo_bridge_pier",
        "mcw_tfc_aio:bridges/bamboo_bridges/dry_bamboo_bridge_pier",
        "mcw_tfc_aio:bridges/blackwood_bridges/rope_blackwood_bridge",
        "mcw_tfc_aio:bridges/chestnut_bridges/rope_chestnut_bridge",
        "mcw_tfc_aio:bridges/hickory_bridges/rope_hickory_bridge",
        "mcw_tfc_aio:bridges/mangrove_bridges/rope_mangrove_bridge",
        "mcw_tfc_aio:bridges/oak_bridges/rope_oak_bridge",
        "mcw_tfc_aio:bridges/pine_bridges/rope_pine_bridge",
        "mcw_tfc_aio:bridges/sequoia_bridges/rope_sequoia_bridge",
        "mcw_tfc_aio:bridges/spruce_bridges/rope_spruce_bridge",
        "mcw_tfc_aio:bridges/white_cedar_bridges/rope_white_cedar_bridge",
        "minecraft:kjs/comforts_hammock_white",
        "sns:shaped/reinforced_fiber",
        "tfc:crafting/metal/fishing_rod/black_bronze",
        "tfc:crafting/metal/fishing_rod/black_steel",
        "tfc:crafting/metal/fishing_rod/bronze",
        "tfc:crafting/metal/fishing_rod/red_steel",
        "tfc:crafting/metal/fishing_rod/wrought_iron",
        "tfc:crafting/powderkeg_from_barrel",
        "tfc:crafting/vanilla/crossbow",
        "tfc:crafting/vanilla/loom",
        "tfc:kjs/damage/tfc/small_to_large_prepared_hide",
        "tfc:kjs/damage/tfc/small_to_large_scraped_hide",
        "tfc:kjs/damage/tfc/small_to_large_soaked_hide",
        "tfc:kjs/damage/tfc/small_to_medium_raw_hide",
        "tfc:kjs/damage/tfc/small_to_medium_sheepskin_hide",
        "tfc:kjs/damage/tfc/small_to_medium_soaked_hide",
        "tfc_textile:clothing/black_bear/black_bear_hat",
        "tfc_textile:clothing/black_bear/black_bear_shirt",
        "tfc_textile:clothing/caribou/caribou_hat",
        "tfc_textile:clothing/caribou/caribou_shirt",
        "tfc_textile:clothing/cougar/cougar_boots",
        "tfc_textile:clothing/cougar/cougar_pants",
        "tfc_textile:clothing/crocodile/crocodile_boots",
        "tfc_textile:clothing/crocodile/crocodile_pants",
        "tfc_textile:clothing/direwolf/direwolf_boots",
        "tfc_textile:clothing/direwolf/direwolf_pants",
        "tfc_textile:clothing/direwolf/direwolf_shirt",
        "tfc_textile:clothing/grizzly_bear/grizzly_bear_hat",
        "tfc_textile:clothing/grizzly_bear/grizzly_bear_shirt",
        "tfc_textile:clothing/lion/lion_hat",
        "tfc_textile:clothing/lion/lion_shirt",
        "tfc_textile:clothing/panther/panther_boots",
        "tfc_textile:clothing/panther/panther_pants",
        "tfc_textile:clothing/polar_bear/polar_bear_boots",
        "tfc_textile:clothing/polar_bear/polar_bear_pants",
        "tfc_textile:clothing/sabertooth/sabertooth_boots",
        "tfc_textile:clothing/sabertooth/sabertooth_pants",
        "tfc_textile:clothing/sabertooth/sabertooth_shirt",
        "tfc_textile:clothing/tiger/tiger_hat",
        "tfc_textile:clothing/tiger/tiger_shirt",
        "tfcambiental:crafting/insulated_leather_hat",
        "tfcambiental:crafting/insulated_leather_tunic",
        "tfchotornot:crafting/potholders/silk",
        "tfchotornot:crafting/potholders/wool",
        "tfg:shaped/airship_balloon",
        "tfg:shaped/auto_drink_modifier_bamboo",
        "tfg:shaped/ginkgo_hanger",
        "tfg:shaped/iron_flask",
        "tfg:shaped/leather_belt_connector",
        "tfg:shaped/red_steel_flask_bladder",
        "tfg:shaped/smoke_bomb_paper",
        "tfg:shaped/snowshoes",
        "tfg:shapeless/diggerhelmet/silk_lining",
        "tfg:sophisticated_backpacks/shaped/upgrade_base",
        "tfg:sophisticated_backpacks/shaped/upgrade_base_rubber",
        "tfg:toolbelt/shaped/pouch",
        "waterflasks:crafting/leather_flask_rotated"
      ],
      "recipe_output_examples": [],
      "recipe_examples_truncated": true,
      "model_parents": [],
      "creative_tabs": [
        "minecraft:ingredients",
        "minecraft:redstone_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:tripwire",
        "block_tags": [
          "create:movable_empty_collider",
          "create:wrench_pickup",
          "minecraft:wall_post_override"
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
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
            "computercraft:impostor_shapeless",
            "crafting",
            "create:item_application",
            "kubejs:shapeless",
            "tfc:advanced_shaped_crafting",
            "tfc:damage_inputs_shaped_crafting",
            "tfc:damage_inputs_shapeless_crafting",
            "tfc:no_remainder_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "minecraft:stripped_acacia_log",
      "namespace": "minecraft",
      "display_name": "Stripped Acacia Log",
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
      "model_parents": [],
      "creative_tabs": [
        "minecraft:building_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:stripped_acacia_log",
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
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
          "value": "wood_acacia",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix stripped_acacia_"
        },
        "form": {
          "value": "stripped_log",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "stripped log"
        }
      }
    },
    {
      "id": "minecraft:stripped_acacia_wood",
      "namespace": "minecraft",
      "display_name": "Stripped Acacia Wood",
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
      "model_parents": [],
      "creative_tabs": [
        "minecraft:building_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:stripped_acacia_wood",
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
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
          "value": "wood_acacia",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix stripped_acacia_"
        },
        "form": {
          "value": "wood",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "stripped wood"
        }
      }
    },
    {
      "id": "minecraft:stripped_bamboo_block",
      "namespace": "minecraft",
      "display_name": "Block of Stripped Bamboo",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:vanilla_stripped_logs",
        "forge:stripped_logs",
        "minecraft:bamboo_blocks"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:item_application",
        "greate:cutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "create:item_application": 6,
        "greate:cutting": 1
      },
      "recipe_production_by_type": {
        "vintageimprovements:polishing": 1
      },
      "recipe_ingredient_count": 8,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/tfg/bamboo_lumber_from_log",
        "tfg:create/item_application/andesite_casing",
        "tfg:create/item_application/brass_casing",
        "tfg:create/item_application/copper_casing",
        "tfg:create/item_application/railway_casing",
        "tfg:create/item_application/refined_radiance_casing",
        "tfg:create/item_application/shadow_steel_casing",
        "tfg:shapeless/bamboo_lumber_from_log"
      ],
      "recipe_output_examples": [
        "tfg:vi/lathe/bamboo_stripped_log_from_log"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:building_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:stripped_bamboo_block",
        "block_tags": [
          "cucumber:mineable/paxel",
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
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "minecraft:bamboo_blocks",
          "minecraft:mineable/axe"
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
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
          "value": "wood_bamboo",
          "confidence": 1,
          "source": "rule:material_family_from_tag",
          "rationale": "log tag minecraft:bamboo_blocks"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "create:item_application",
            "greate:cutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "minecraft:stripped_birch_log",
      "namespace": "minecraft",
      "display_name": "Stripped Birch Log",
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
      "model_parents": [],
      "creative_tabs": [
        "minecraft:building_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:stripped_birch_log",
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
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
          "value": "wood_birch",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix stripped_birch_"
        },
        "form": {
          "value": "stripped_log",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "stripped log"
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