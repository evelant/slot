# Items to classify
{
  "items": [
    {
      "id": "minecraft:orange_carpet",
      "namespace": "minecraft",
      "display_name": "Orange Carpet",
      "minecraft_tags_direct": [
        "minecraft:wool_carpets"
      ],
      "minecraft_tags_inherited": [
        "minecraft:dampens_vibrations"
      ],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 15
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:dye_black_carpet",
        "minecraft:dye_blue_carpet",
        "minecraft:dye_brown_carpet",
        "minecraft:dye_cyan_carpet",
        "minecraft:dye_gray_carpet",
        "minecraft:dye_green_carpet",
        "minecraft:dye_light_blue_carpet",
        "minecraft:dye_light_gray_carpet",
        "minecraft:dye_lime_carpet",
        "minecraft:dye_magenta_carpet"
      ],
      "sample_output_of": [
        "minecraft:dye_orange_carpet",
        "minecraft:orange_carpet"
      ],
      "model_parents": [
        "block/orange_carpet",
        "block/carpet",
        "block/thin_block",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/orange_carpet"
      ],
      "lore": [],
      "component_highlights": {
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
        "equip_slot": {
          "value": "llama_carpet",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
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
          "value": "orange",
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
        "origin": {
          "values": [
            "crafted_only"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        },
        "is_fuel": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_fuel_from_id_or_tag"
        }
      }
    },
    {
      "id": "minecraft:orange_concrete",
      "namespace": "minecraft",
      "display_name": "Orange Concrete",
      "minecraft_tags_direct": [
        "minecraft:concrete"
      ],
      "minecraft_tags_inherited": [
        "minecraft:sulfur_cube_archetype/regular",
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "block/orange_concrete",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/orange_concrete"
      ],
      "lore": [],
      "component_highlights": {
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
          "value": "orange",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
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
      "id": "minecraft:orange_concrete_powder",
      "namespace": "minecraft",
      "display_name": "Orange Concrete Powder",
      "minecraft_tags_direct": [
        "minecraft:concrete_powders"
      ],
      "minecraft_tags_inherited": [
        "minecraft:sulfur_cube_archetype/regular",
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:orange_concrete_powder"
      ],
      "model_parents": [
        "block/orange_concrete_powder",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/orange_concrete_powder"
      ],
      "lore": [],
      "component_highlights": {
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
          "value": "orange",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
        },
        "required_tool": {
          "value": "shovel",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/shovel"
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
      "id": "minecraft:orange_dye",
      "namespace": "minecraft",
      "display_name": "Orange Dye",
      "minecraft_tags_direct": [
        "minecraft:dyes"
      ],
      "minecraft_tags_inherited": [
        "minecraft:cat_collar_dyes",
        "minecraft:loom_dyes",
        "minecraft:wolf_collar_dyes"
      ],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 6,
        "crafting_special_firework_star": 1,
        "crafting_special_firework_star_fade": 1,
        "crafting_dye": 6,
        "crafting_transmute": 2,
        "crafting_shaped": 3
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 4
      },
      "sample_ingredient_of": [
        "minecraft:dye_orange_bed",
        "minecraft:dye_orange_carpet",
        "minecraft:dye_orange_harness",
        "minecraft:dye_orange_wool",
        "minecraft:firework_star",
        "minecraft:firework_star_fade",
        "minecraft:leather_boots_dyed",
        "minecraft:leather_chestplate_dyed",
        "minecraft:leather_helmet_dyed",
        "minecraft:leather_horse_armor_dyed"
      ],
      "sample_output_of": [
        "minecraft:orange_dye_from_open_eyeblossom",
        "minecraft:orange_dye_from_orange_tulip",
        "minecraft:orange_dye_from_red_yellow",
        "minecraft:orange_dye_from_torchflower"
      ],
      "model_parents": [
        "item/orange_dye",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:archaeology/trail_ruins_common"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:dye": "orange",
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
          "value": "orange",
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
        "origin": {
          "values": [
            "archaeology_site"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:orange_glazed_terracotta",
      "namespace": "minecraft",
      "display_name": "Orange Glazed Terracotta",
      "minecraft_tags_direct": [
        "minecraft:glazed_terracotta"
      ],
      "minecraft_tags_inherited": [
        "minecraft:sulfur_cube_archetype/regular",
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "smelting": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:orange_glazed_terracotta"
      ],
      "model_parents": [
        "block/orange_glazed_terracotta",
        "block/template_glazed_terracotta",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/orange_glazed_terracotta"
      ],
      "lore": [],
      "component_highlights": {
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
          "value": "orange",
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
Respond with a single JSON object matching the expected output shape above. No other text.