# Items to classify
{
  "items": [
    {
      "id": "minecraft:brown_carpet",
      "namespace": "minecraft",
      "display_name": "Brown Carpet",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:dampens_vibrations",
        "minecraft:wool_carpets",
        "tfc:colored_carpet"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "greate:cutting": 3,
        "tfc:barrel_sealed": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 5,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/awnings/brown_striped_awning"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_brown_wool_to_carpet",
        "greate:cutting/integration/gtceu/cutter/cut_brown_wool_to_carpet_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_brown_wool_to_carpet_water",
        "minecraft:brown_carpet",
        "tfc:barrel/dye/brown_carpet"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:colored_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:brown_carpet",
        "block_tags": [
          "minecraft:combination_step_sound_blocks",
          "minecraft:dampens_vibrations",
          "minecraft:wool_carpets",
          "species:stackatick_is_comfy_on",
          "tfc:pet_sits_on"
        ],
        "requires_correct_tool": false
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
        "material_family": {
          "value": "wool",
          "confidence": 1,
          "source": "rule:material_family_from_tag",
          "rationale": "tag minecraft:wool_carpets"
        },
        "form": {
          "value": "carpet",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:wool_carpets"
        },
        "dye_color": {
          "value": "brown",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
        },
        "processing_in": {
          "values": [
            "crafting"
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
      "id": "minecraft:brown_concrete",
      "namespace": "minecraft",
      "display_name": "Brown Concrete",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:concretes",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 7,
        "kubejs:shapeless": 3,
        "stonecutting": 7
      },
      "recipe_production_by_type": {
        "tfc:barrel_instant": 1,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 17,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "mcw_tfc_aio:roofs/brown_concrete_roofs/brown_concrete_attic_roof",
        "mcw_tfc_aio:roofs/brown_concrete_roofs/brown_concrete_lower_roof",
        "mcw_tfc_aio:roofs/brown_concrete_roofs/brown_concrete_roof",
        "mcw_tfc_aio:roofs/brown_concrete_roofs/brown_concrete_steep_roof",
        "mcw_tfc_aio:roofs/brown_concrete_roofs/brown_concrete_top_roof",
        "mcw_tfc_aio:roofs/brown_concrete_roofs/brown_concrete_upper_lower_roof",
        "mcw_tfc_aio:roofs/brown_concrete_roofs/brown_concrete_upper_steep_roof",
        "mcw_tfc_aio:roofs/brown_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/brown_concrete_roofs/x_lower_roof2",
        "mcw_tfc_aio:roofs/brown_concrete_roofs/x_roof2",
        "mcw_tfc_aio:roofs/brown_concrete_roofs/x_steep_roof2",
        "mcw_tfc_aio:roofs/brown_concrete_roofs/x_top_roof2",
        "mcw_tfc_aio:roofs/brown_concrete_roofs/x_upper_lower_roof2",
        "mcw_tfc_aio:roofs/brown_concrete_roofs/x_upper_steep_roof2"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/brown_concrete_roofs/brown_concrete_roof_uncraft",
        "tfg:barrel/dye/brown_concrete"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:colored_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:brown_concrete",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:concrete",
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
          "forge:concrete",
          "forge:concretes",
          "minecraft:mineable/pickaxe",
          "tfc:bloomery_insulation",
          "tfc:creeping_stone_plantable_on"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "?"
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
        "dye_color": {
          "value": "brown",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
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
            "kubejs:shapeless",
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "minecraft:brown_concrete_powder",
      "namespace": "minecraft",
      "display_name": "Brown Concrete Powder",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:concrete_powders",
        "tfc:colored_concrete_powder"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "greate:mixing": 1,
        "tfc:barrel_sealed": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "greate:mixing/integration/gtceu/mixer/brown_concrete_powder",
        "tfc:barrel/dye/brown_concrete_powder"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:colored_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:brown_concrete_powder",
        "block_tags": [
          "computercraft:turtle_shovel_harvestable",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "forge:concrete_powders",
          "minecraft:mineable/shovel"
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
        "dye_color": {
          "value": "brown",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
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
      "id": "minecraft:brown_dye",
      "namespace": "minecraft",
      "display_name": "Brown Dye",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:brown_dyes",
        "balm:dyes",
        "exposure:photo_agers",
        "forge:dyes",
        "forge:dyes/brown",
        "forge:dyes/brown_dye",
        "railways:internal/dyes/brown_dyes",
        "tfc:dyes"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 26,
        "crafting_shapeless": 6
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 2,
        "create:milling": 1,
        "greate:milling": 1,
        "tfc:quern": 1
      },
      "recipe_ingredient_count": 32,
      "recipe_output_count": 5,
      "recipe_ingredient_examples": [
        "createdeco:brown_placard",
        "createdeco:brown_placard_from_dyeing",
        "domum_ornamentum:brown_brick_extra",
        "domum_ornamentum:brown_bricks",
        "domum_ornamentum:brown_stone_bricks",
        "gtceu:shaped/crowbar_bismuth_bronze_brown",
        "gtceu:shaped/crowbar_black_bronze_brown",
        "gtceu:shaped/crowbar_black_steel_brown",
        "gtceu:shaped/crowbar_blue_steel_brown",
        "gtceu:shaped/crowbar_boron_carbide_brown",
        "gtceu:shaped/crowbar_bronze_brown",
        "gtceu:shaped/crowbar_copper_brown",
        "gtceu:shaped/crowbar_diamond_tipped_mo_50_re_brown",
        "gtceu:shaped/crowbar_duranium_brown",
        "gtceu:shaped/crowbar_hsse_brown",
        "gtceu:shaped/crowbar_naquadah_alloy_brown",
        "gtceu:shaped/crowbar_neutronium_brown",
        "gtceu:shaped/crowbar_ostrum_iodide_brown",
        "gtceu:shaped/crowbar_red_steel_brown",
        "gtceu:shaped/crowbar_steel_brown",
        "gtceu:shaped/crowbar_tungsten_carbide_brown",
        "gtceu:shaped/crowbar_ultimet_brown",
        "gtceu:shaped/crowbar_vanadium_steel_brown",
        "gtceu:shaped/crowbar_wrought_iron_brown",
        "mcw_tfc_aio:roofs/gutters/gutter_base_brown",
        "mcw_tfc_aio:roofs/gutters/gutter_middle_brown",
        "minecraft:kjs/createdeco_dean_bricks",
        "tfc:crafting/ceramic/brown_unfired_large_vessel",
        "tfc:crafting/ceramic/brown_unfired_vessel",
        "tfc:crafting/vanilla/color/brown_bed",
        "tfg:shapeless/glow_ink_sac",
        "tfg:shapeless/wattle/brown"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/tfg/milling/brown_dye",
        "gtceu:shapeless/brown_dye_from_metal_mixture",
        "tfc:crafting/vanilla/brown_dye_from_garnierite",
        "tfc:quern/brown_dye",
        "tfg:milling/brown_dye"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:ingredients"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Tiny"
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
        "dye_color": {
          "value": "brown",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
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
      "id": "minecraft:brown_glazed_terracotta",
      "namespace": "minecraft",
      "display_name": "Brown Glazed Terracotta",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:colored_glazed_terracotta"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "tfc:barrel_sealed": 1,
        "tfc:heating": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_brown_glazed_terracotta"
      ],
      "recipe_output_examples": [
        "minecraft:brown_glazed_terracotta",
        "tfc:barrel/dye/brown_glazed_terracotta",
        "tfc:heating/glazed_terracotta_brown"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:colored_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:brown_glazed_terracotta",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:glaced_terracotta",
          "domum_ornamentum:trapdoors_materials",
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
        "dye_color": {
          "value": "brown",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
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
      "id": "minecraft:brown_mushroom",
      "namespace": "minecraft",
      "display_name": "Brown Mushroom",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "beneath:usable_in_juicer",
        "createaddition:plants",
        "firmalife:foods/pizza_ingredients",
        "firmalife:usable_in_stovetop_soup",
        "forge:mushrooms",
        "sns:prevented_in_burlap_sack",
        "sns:prevented_in_leather_sack",
        "sns:prevented_in_ore_sack",
        "sns:prevented_in_seed_pouch",
        "sns:prevented_in_straw_basket",
        "species:goober_breed_items",
        "tfc:chicken_food",
        "tfc:compost_greens_high",
        "tfc:dog_food",
        "tfc:duck_food",
        "tfc:foods",
        "tfc:foods/usable_in_salad",
        "tfc:foods/usable_in_sandwich",
        "tfc:foods/usable_in_soup",
        "tfc:foods/vegetables",
        "tfc:goat_food",
        "tfc:pig_food",
        "tfc:quail_food",
        "tfc:rabbit_food",
        "tfg:foods/usable_in_burgers",
        "tfg:foods/usable_in_meal_bag"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing",
        "tfc:advanced_shaped_crafting",
        "tfc:advanced_shapeless_crafting",
        "tfc:damage_inputs_shaped_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "greate:mixing": 3,
        "tfc:advanced_shaped_crafting": 2,
        "tfc:advanced_shapeless_crafting": 5,
        "tfc:damage_inputs_shaped_crafting": 18
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 30,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/burrito",
        "firmalife:crafting/crafting/barley_sandwich_flatbread",
        "firmalife:crafting/crafting/barley_sandwich_slice",
        "firmalife:crafting/crafting/maize_sandwich_flatbread",
        "firmalife:crafting/crafting/maize_sandwich_slice",
        "firmalife:crafting/crafting/oat_sandwich_flatbread",
        "firmalife:crafting/crafting/oat_sandwich_slice",
        "firmalife:crafting/crafting/rice_sandwich_flatbread",
        "firmalife:crafting/crafting/rice_sandwich_slice",
        "firmalife:crafting/crafting/rye_sandwich_flatbread",
        "firmalife:crafting/crafting/rye_sandwich_slice",
        "firmalife:crafting/crafting/wheat_sandwich_flatbread",
        "firmalife:crafting/crafting/wheat_sandwich_slice",
        "firmalife:crafting/taco",
        "greate:mixing/integration/tfg/compost_2",
        "greate:mixing/integration/tfg/compost_5",
        "greate:mixing/integration/tfg/compost_8",
        "tfc:crafting/barley_sandwich",
        "tfc:crafting/maize_sandwich",
        "tfc:crafting/oat_sandwich",
        "tfc:crafting/rice_sandwich",
        "tfc:crafting/rye_sandwich",
        "tfc:crafting/wheat_sandwich",
        "tfc_gourmet:crafting/raw_lavash_wrap",
        "tfg:crafting/cheeseburger",
        "tfg:crafting/hamburger",
        "tfg:crafting/pizza_1_extra",
        "tfg:crafting/pizza_2_extra",
        "tfg:shaped/universal_compost_greens_from_high",
        "tfg:shapeless/raw_vareniki"
      ],
      "recipe_output_examples": [
        "tfg:shapeless/cut_brown_mushroom_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:natural_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:brown_mushroom",
        "block_tags": [
          "beneath:mushrooms",
          "cucumber:mineable/paxel",
          "cucumber:mineable/sickle",
          "minecraft:enderman_holdable",
          "minecraft:mineable/axe",
          "minecraft:sword_efficient",
          "tfg:plants/beneath"
        ],
        "requires_correct_tool": false
      },
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
          "text": "Expires on: 11:59 July 4, 1000 (in 1 month(s) and 3 day(s))"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold (Shift) for Nutrition Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "Minecraft"
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
        "minecraft:light_emission": 1,
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
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing",
            "tfc:advanced_shaped_crafting",
            "tfc:advanced_shapeless_crafting",
            "tfc:damage_inputs_shaped_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "emits_light": {
          "value": true,
          "confidence": 1,
          "source": "rule:emits_light_from_component"
        }
      }
    },
    {
      "id": "minecraft:brown_mushroom_block",
      "namespace": "minecraft",
      "display_name": "Brown Mushroom Block",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "ad_astra:destroyed_in_space",
        "tfc:compost_greens"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:mixing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "greate:mixing": 3
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 5,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/tfg/compost_1",
        "greate:mixing/integration/tfg/compost_4",
        "greate:mixing/integration/tfg/compost_7",
        "tfg:shaped/universal_compost_greens_from_medium",
        "tfg:shapeless/cut_brown_mushroom_block"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:natural_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:brown_mushroom_block",
        "block_tags": [
          "ad_astra:destroyed_in_space",
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
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:mixing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "minecraft:brown_shulker_box",
      "namespace": "minecraft",
      "display_name": "Brown Shulker Box",
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
        "minecraft:colored_blocks",
        "minecraft:functional_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:brown_shulker_box",
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
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        },
        "dye_color": {
          "value": "brown",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
        }
      }
    },
    {
      "id": "minecraft:brown_stained_glass",
      "namespace": "minecraft",
      "display_name": "Brown Stained Glass",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:glass",
        "forge:glass/brown",
        "forge:glass/silica",
        "forge:stained_glass",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:mechanical_crafting",
        "greate:cutting",
        "greate:milling",
        "kubejs:shaped",
        "kubejs:shapeless",
        "vintageimprovements:polishing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 139,
        "crafting_shapeless": 1,
        "create:mechanical_crafting": 1,
        "greate:cutting": 3,
        "greate:milling": 1,
        "kubejs:shaped": 5,
        "kubejs:shapeless": 3,
        "vintageimprovements:polishing": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "tfc:glassworking": 1
      },
      "recipe_ingredient_count": 154,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "createdeco:andesite_window",
        "createdeco:copper_window",
        "createdeco:industrial_iron_window",
        "createdeco:zinc_window",
        "framedblocks:powered_framing_saw",
        "greate:cutting/integration/gtceu/cutter/cut_brown_glass_to_pane_distilled_water",
        "greate:milling/integration/gtceu/macerator/macerate_glass",
        "gtceu:facade_cover",
        "gtceu:facade_cover_recycle",
        "gtceu:shaped/brown_glass_pane",
        "gtceu:shaped/lv_aqueous_accumulator",
        "gtceu:shaped/lv_brewery",
        "gtceu:shaped/lv_canner",
        "gtceu:shaped/lv_chemical_reactor",
        "gtceu:shaped/lv_distillery",
        "gtceu:shaped/lv_electrolyzer",
        "gtceu:shaped/lv_fermenter",
        "gtceu:shaped/lv_fluid_heater",
        "gtceu:shaped/lv_food_processor",
        "gtceu:shaped/lv_mixer",
        "gtceu:shaped/lv_ore_washer",
        "gtceu:shaped/mv_aqueous_accumulator",
        "gtceu:shaped/mv_autoclave",
        "gtceu:shaped/mv_canner",
        "gtceu:shaped/mv_cutter",
        "gtceu:shaped/mv_distillery",
        "gtceu:shaped/mv_extractor",
        "gtceu:shaped/mv_fermenter",
        "gtceu:shaped/mv_fluid_solidifier",
        "gtceu:shaped/mv_gas_pressurizer",
        "gtceu:shaped/mv_mixer",
        "gtceu:shaped/mv_rock_crusher",
        "gtceu:shaped/passthrough_hatch_fluid_mv",
        "gtceu:shaped/steam_boiler_lava_steel",
        "gtceu:shaped/ulv_output_hatch",
        "mcw_tfc_aio:roofs/acacia_roofs/acacia_attic_roof",
        "mcw_tfc_aio:roofs/ash_roofs/ash_attic_roof",
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_attic_roof",
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_attic_roof",
        "mcw_tfc_aio:roofs/birch_roofs/birch_attic_roof",
        "mcw_tfc_aio:roofs/birch_roofs/birch_planks_attic_roof",
        "mcw_tfc_aio:roofs/black_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/blackwood_roofs/blackwood_attic_roof",
        "mcw_tfc_aio:roofs/blackwood_roofs/blackwood_planks_attic_roof",
        "mcw_tfc_aio:roofs/blue_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/brick_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/chestnut_roofs/chestnut_planks_attic_roof",
        "mcw_tfc_aio:roofs/cyan_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/douglas_fir_roofs/douglas_fir_attic_roof",
        "mcw_tfc_aio:roofs/gray_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/gray_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/green_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/green_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/hickory_roofs/hickory_planks_attic_roof",
        "mcw_tfc_aio:roofs/kapok_roofs/kapok_planks_attic_roof",
        "mcw_tfc_aio:roofs/light_blue_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/light_gray_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/light_gray_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/lime_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/magenta_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/magenta_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/mangrove_roofs/mangrove_planks_attic_roof",
        "mcw_tfc_aio:roofs/maple_roofs/maple_attic_roof",
        "mcw_tfc_aio:roofs/oak_roofs/oak_attic_roof",
        "mcw_tfc_aio:roofs/orange_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/orange_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/palm_roofs/palm_planks_attic_roof",
        "mcw_tfc_aio:roofs/pine_roofs/pine_planks_attic_roof",
        "mcw_tfc_aio:roofs/pink_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/purple_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/purple_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/red_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/rosewood_roofs/rosewood_planks_attic_roof",
        "mcw_tfc_aio:roofs/sequoia_roofs/sequoia_attic_roof",
        "mcw_tfc_aio:roofs/spruce_roofs/spruce_attic_roof",
        "mcw_tfc_aio:roofs/spruce_roofs/spruce_planks_attic_roof",
        "mcw_tfc_aio:roofs/sycamore_roofs/sycamore_planks_attic_roof",
        "mcw_tfc_aio:roofs/thatch_roofs/thatch_attic_roof",
        "mcw_tfc_aio:roofs/white_cedar_roofs/white_cedar_attic_roof",
        "mcw_tfc_aio:roofs/white_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/white_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/willow_roofs/willow_attic_roof",
        "mcw_tfc_aio:roofs/yellow_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/yellow_terracotta_roofs/x_attic_roof2",
        "tfg:create/shaped/acacia_window",
        "tfg:create/shaped/birch_window",
        "tfg:create/shaped/cherry_window",
        "tfg:create/shaped/dark_oak_window",
        "tfg:create/shaped/industrial_iron_window",
        "tfg:create/shaped/mangrove_window",
        "tfg:create/shaped/ornate_iron_window",
        "tfg:create/shaped/spruce_window",
        "tfg:greate/shaped/treated_wood_window",
        "tfg:shaped/mv_chemical_bath",
        "tfg:vi/lathe/lens"
      ],
      "recipe_output_examples": [
        "tfc:glassworking/brown_glass_block",
        "tfg:shapeless/smooth_brown_stained_glass_to_brown_stained_glass"
      ],
      "recipe_examples_truncated": true,
      "model_parents": [],
      "creative_tabs": [
        "minecraft:colored_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:brown_stained_glass",
        "block_tags": [
          "ae2:whitelisted/facades",
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
          "forge:glass",
          "forge:glass/brown",
          "forge:glass/silica",
          "forge:stained_glass",
          "framedblocks:frameable",
          "minecraft:impermeable",
          "tfc:mineable_with_glass_saw"
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
          "value": "pane",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _stained_glass"
        },
        "dye_color": {
          "value": "brown",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
        },
        "processing_in": {
          "values": [
            "crafting",
            "create:mechanical_crafting",
            "greate:cutting",
            "greate:milling",
            "kubejs:shaped",
            "kubejs:shapeless",
            "vintageimprovements:polishing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "minecraft:brown_stained_glass_pane",
      "namespace": "minecraft",
      "display_name": "Brown Stained Glass Pane",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "exposure:filters",
        "forge:glass_panes",
        "forge:glass_panes/brown",
        "forge:stained_glass_panes"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped",
        "tfc:advanced_shaped_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 48,
        "greate:milling": 1,
        "kubejs:shaped": 1,
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "greate:cutting": 3
      },
      "recipe_ingredient_count": 51,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_glass_pane",
        "gtceu:shaped/basic_terminal",
        "gtceu:shaped/redstone_lamp",
        "gtceu:shaped/solar_panel_ulv",
        "gtceu:shaped/steam_boiler_solar_steel",
        "mcw_tfc_aio:furniture/acacia_furniture/acacia_glass_table",
        "mcw_tfc_aio:furniture/acacia_furniture/stripped_acacia_glass_table",
        "mcw_tfc_aio:furniture/ash_furniture/ash_glass_table",
        "mcw_tfc_aio:furniture/ash_furniture/stripped_ash_glass_table",
        "mcw_tfc_aio:furniture/aspen_furniture/aspen_glass_table",
        "mcw_tfc_aio:furniture/aspen_furniture/stripped_aspen_glass_table",
        "mcw_tfc_aio:furniture/birch_furniture/birch_glass_table",
        "mcw_tfc_aio:furniture/birch_furniture/stripped_birch_glass_table",
        "mcw_tfc_aio:furniture/blackwood_furniture/blackwood_glass_table",
        "mcw_tfc_aio:furniture/blackwood_furniture/stripped_blackwood_glass_table",
        "mcw_tfc_aio:furniture/chestnut_furniture/chestnut_glass_table",
        "mcw_tfc_aio:furniture/chestnut_furniture/stripped_chestnut_glass_table",
        "mcw_tfc_aio:furniture/douglas_fir_furniture/douglas_fir_glass_table",
        "mcw_tfc_aio:furniture/douglas_fir_furniture/stripped_douglas_fir_glass_table",
        "mcw_tfc_aio:furniture/hickory_furniture/hickory_glass_table",
        "mcw_tfc_aio:furniture/hickory_furniture/stripped_hickory_glass_table",
        "mcw_tfc_aio:furniture/kapok_furniture/kapok_glass_table",
        "mcw_tfc_aio:furniture/kapok_furniture/stripped_kapok_glass_table",
        "mcw_tfc_aio:furniture/mangrove_furniture/mangrove_glass_table",
        "mcw_tfc_aio:furniture/mangrove_furniture/stripped_mangrove_glass_table",
        "mcw_tfc_aio:furniture/maple_furniture/maple_glass_table",
        "mcw_tfc_aio:furniture/maple_furniture/stripped_maple_glass_table",
        "mcw_tfc_aio:furniture/oak_furniture/oak_glass_table",
        "mcw_tfc_aio:furniture/oak_furniture/stripped_oak_glass_table",
        "mcw_tfc_aio:furniture/palm_furniture/palm_glass_table",
        "mcw_tfc_aio:furniture/palm_furniture/stripped_palm_glass_table",
        "mcw_tfc_aio:furniture/pine_furniture/pine_glass_table",
        "mcw_tfc_aio:furniture/pine_furniture/stripped_pine_glass_table",
        "mcw_tfc_aio:furniture/rosewood_furniture/rosewood_glass_table",
        "mcw_tfc_aio:furniture/rosewood_furniture/stripped_rosewood_glass_table",
        "mcw_tfc_aio:furniture/sequoia_furniture/sequoia_glass_table",
        "mcw_tfc_aio:furniture/sequoia_furniture/stripped_sequoia_glass_table",
        "mcw_tfc_aio:furniture/spruce_furniture/spruce_glass_table",
        "mcw_tfc_aio:furniture/spruce_furniture/stripped_spruce_glass_table",
        "mcw_tfc_aio:furniture/sycamore_furniture/stripped_sycamore_glass_table",
        "mcw_tfc_aio:furniture/sycamore_furniture/sycamore_glass_table",
        "mcw_tfc_aio:furniture/white_cedar_furniture/stripped_white_cedar_glass_table",
        "mcw_tfc_aio:furniture/white_cedar_furniture/white_cedar_glass_table",
        "mcw_tfc_aio:furniture/willow_furniture/stripped_willow_glass_table",
        "mcw_tfc_aio:furniture/willow_furniture/willow_glass_table",
        "tfg:create/shaped/copper_diving_helmet",
        "tfg:create/shaped/fluid_tank",
        "tfg:create/shaped/stock_ticker",
        "tfg:shaped/ad_astra_brown_industrial_lamp",
        "tfg:shaped/ad_astra_small_brown_industrial_lamp",
        "tfg:shaped/snorkel"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_brown_glass_to_pane",
        "greate:cutting/integration/gtceu/cutter/cut_brown_glass_to_pane_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_brown_glass_to_pane_water",
        "gtceu:shaped/brown_glass_pane"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:colored_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:brown_stained_glass_pane",
        "block_tags": [
          "forge:glass_panes",
          "forge:glass_panes/brown",
          "forge:stained_glass_panes",
          "tfc:mineable_with_glass_saw"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
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
          "value": "pane",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _glass_pane"
        },
        "dye_color": {
          "value": "brown",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "kubejs:shaped",
            "tfc:advanced_shaped_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "minecraft:brown_terracotta",
      "namespace": "minecraft",
      "display_name": "Brown Terracotta",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:terracotta",
        "tfc:colored_terracotta",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shapeless",
        "smelting",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 7,
        "greate:milling": 1,
        "kubejs:shapeless": 3,
        "smelting": 1,
        "stonecutting": 7
      },
      "recipe_production_by_type": {
        "tfc:barrel_sealed": 1,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 19,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_brown_terracotta",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/brown_terracotta_attic_roof",
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/brown_terracotta_lower_roof",
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/brown_terracotta_roof",
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/brown_terracotta_steep_roof",
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/brown_terracotta_top_roof",
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/brown_terracotta_upper_lower_roof",
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/brown_terracotta_upper_steep_roof",
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/x_lower_roof2",
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/x_roof2",
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/x_steep_roof2",
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/x_top_roof2",
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/x_upper_lower_roof2",
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/x_upper_steep_roof2",
        "minecraft:brown_glazed_terracotta"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/brown_terracotta_roofs/brown_terracotta_roof_uncraft",
        "tfc:barrel/dye/brown_terracotta"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:colored_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:brown_terracotta",
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
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "gtceu:charcoal_pile_igniter_walls",
          "minecraft:azalea_grows_on",
          "minecraft:azalea_root_replaceable",
          "minecraft:dead_bush_may_place_on",
          "minecraft:mineable/pickaxe",
          "minecraft:overworld_carver_replaceables",
          "minecraft:sculk_replaceable",
          "minecraft:sculk_replaceable_world_gen",
          "minecraft:terracotta"
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
        "dye_color": {
          "value": "brown",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
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
            "kubejs:shapeless",
            "smelting",
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "overworld_surface"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_hardcoded_override"
        }
      }
    },
    {
      "id": "minecraft:brown_wool",
      "namespace": "minecraft",
      "display_name": "Brown Wool",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:wools/brown",
        "minecraft:dampens_vibrations",
        "minecraft:wool",
        "tfc:colored_wool"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:cutting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "greate:cutting": 3,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "tfc:barrel_sealed": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_brown_wool_to_carpet",
        "greate:cutting/integration/gtceu/cutter/cut_brown_wool_to_carpet_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_brown_wool_to_carpet_water",
        "greate:milling/integration/gtceu/macerate_wool",
        "minecraft:brown_carpet",
        "morered:brown_network_cable"
      ],
      "recipe_output_examples": [
        "tfc:barrel/dye/brown_wool"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:colored_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:brown_wool",
        "block_tags": [
          "computercraft:turtle_sword_harvestable",
          "create:windmill_sails",
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
          "minecraft:dampens_vibrations",
          "minecraft:occludes_vibration_signals",
          "minecraft:wool",
          "species:stackatick_is_comfy_on",
          "tfc:pet_sits_on"
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
          "value": "wool",
          "confidence": 1,
          "source": "rule:material_family_from_tag",
          "rationale": "tag minecraft:wool"
        },
        "form": {
          "value": "whole_block",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:wool"
        },
        "dye_color": {
          "value": "brown",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
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
        },
        "is_fuel": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_fuel_from_id_or_tag"
        }
      }
    },
    {
      "id": "minecraft:brush",
      "namespace": "minecraft",
      "display_name": "Brush",
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
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 64,
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
        }
      }
    },
    {
      "id": "minecraft:bubble_coral",
      "namespace": "minecraft",
      "display_name": "Bubble Coral",
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
        "minecraft:natural_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:bubble_coral",
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
        }
      }
    },
    {
      "id": "minecraft:bubble_coral_block",
      "namespace": "minecraft",
      "display_name": "Bubble Coral Block",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:coral_blocks",
        "forge:coral_blocks/alive"
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
        "greate:milling/integration/gtceu/macerator/macerate_coral_block"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:natural_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:bubble_coral_block",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:stairs_materials",
          "minecraft:coral_blocks",
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "minecraft:bubble_coral_fan",
      "namespace": "minecraft",
      "display_name": "Bubble Coral Fan",
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
        "minecraft:natural_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:bubble_coral_fan",
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
        }
      }
    },
    {
      "id": "minecraft:bucket",
      "namespace": "minecraft",
      "display_name": "Bucket",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "ae2:p2p_attunements/fluid_p2p_tunnel",
        "create_factory_logistics:network_link_qualifier/create_factory_logistics/fluid",
        "ftbchunks:right_click_blacklist",
        "tfc:buckets",
        "tfc:fluid_item_ingredient_empty_containers",
        "tfc:usable_on_tool_rack"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 2,
        "kubejs:shapeless": 1
      },
      "recipe_production_by_type": {
        "greate:compacting": 1,
        "tfc:welding": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "minecraft:kjs/wan_ancient_beasts_quick_red_sand_bucket",
        "minecraft:kjs/wan_ancient_beasts_quick_sand_bucket",
        "tfg:create/shaped/hose_pulley",
        "tfg:grapplemod/downgrades/gravity/0.5"
      ],
      "recipe_output_examples": [
        "greate:compacting/vanilla_bucket",
        "tfg:anvil/vanilla_bucket"
      ],
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
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Minecraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
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
          "value": "bucket",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "exact id"
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
      "id": "minecraft:budding_amethyst",
      "namespace": "minecraft",
      "display_name": "Budding Amethyst",
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
        "minecraft:natural_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:budding_amethyst",
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
        "is_creative_only": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_creative_only_hardcoded",
          "rationale": "known vanilla creative-only item"
        }
      }
    },
    {
      "id": "minecraft:bundle",
      "namespace": "minecraft",
      "display_name": "Bundle",
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
      "creative_tabs": [],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "0/64"
        },
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
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "minecraft:burn_pottery_sherd",
      "namespace": "minecraft",
      "display_name": "Burn Pottery Sherd",
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
        "minecraft:ingredients"
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
        }
      }
    },
    {
      "id": "minecraft:cactus",
      "namespace": "minecraft",
      "display_name": "Cactus",
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
        "minecraft:natural_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:cactus",
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
        }
      }
    },
    {
      "id": "minecraft:cake",
      "namespace": "minecraft",
      "display_name": "Cake",
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
        "minecraft:food_and_drinks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:cake",
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
      "document_context": [
        {
          "kind": "advancement",
          "id": "species:species/v2/feed_cruncher",
          "label": "Feed the Beast",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Feed the Beast"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Satiate the Cruncher's appetite"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_block_item": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_block_item_from_registry"
        }
      }
    },
    {
      "id": "minecraft:calcite",
      "namespace": "minecraft",
      "display_name": "Calcite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/calcite",
        "forge:calcite",
        "tfg:stone_composition/calcite"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 18,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 1,
        "tfc:damage_inputs_shapeless_crafting": 3
      },
      "recipe_ingredient_count": 20,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "create:shapeless/chisel_cut_calcite",
        "greate:milling/integration/tfg/macerate_calcite",
        "tfg:stonecutter/create_calcite_pillar",
        "tfg:stonecutter/create_cut_calcite",
        "tfg:stonecutter/create_cut_calcite_brick_slab_half",
        "tfg:stonecutter/create_cut_calcite_brick_stairs",
        "tfg:stonecutter/create_cut_calcite_brick_wall",
        "tfg:stonecutter/create_cut_calcite_bricks",
        "tfg:stonecutter/create_cut_calcite_slab_half",
        "tfg:stonecutter/create_cut_calcite_stairs",
        "tfg:stonecutter/create_cut_calcite_wall",
        "tfg:stonecutter/create_layered_calcite",
        "tfg:stonecutter/create_polished_cut_calcite",
        "tfg:stonecutter/create_polished_cut_calcite_slab_half",
        "tfg:stonecutter/create_polished_cut_calcite_stairs",
        "tfg:stonecutter/create_polished_cut_calcite_wall",
        "tfg:stonecutter/create_small_calcite_brick_slab_half",
        "tfg:stonecutter/create_small_calcite_brick_stairs",
        "tfg:stonecutter/create_small_calcite_brick_wall",
        "tfg:stonecutter/create_small_calcite_bricks"
      ],
      "recipe_output_examples": [
        "tfg:shapeless/calcite_from_poor_raw",
        "tfg:shapeless/calcite_from_raw",
        "tfg:shapeless/calcite_from_rich_raw",
        "tfg:stonecutter/minecraft_calcite"
      ],
      "model_parents": [],
      "creative_tabs": [
        "minecraft:natural_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:calcite",
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
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "firmalife:oven_insulation",
          "firmalife:pipe_replaceable",
          "forge:calcite",
          "forge:stone",
          "minecraft:mineable/pickaxe",
          "minecraft:overworld_carver_replaceables",
          "minecraft:sculk_replaceable",
          "minecraft:sculk_replaceable_world_gen",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:bloomery_insulation",
          "tfc:can_carve",
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
          "text": "CaCO₃"
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
        "material_family": {
          "value": "calcite",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "exact id minecraft:calcite"
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
      "id": "minecraft:calibrated_sculk_sensor",
      "namespace": "minecraft",
      "display_name": "Calibrated Sculk Sensor",
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
        "minecraft:redstone_blocks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "minecraft:calibrated_sculk_sensor",
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
        "minecraft:light_emission": 1,
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
        "emits_light": {
          "value": true,
          "confidence": 1,
          "source": "rule:emits_light_from_component"
        }
      }
    },
    {
      "id": "minecraft:camel_spawn_egg",
      "namespace": "minecraft",
      "display_name": "Camel Spawn Egg",
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