# Items to classify
{
  "items": [
    {
      "id": "minecraft:clay_ball",
      "namespace": "minecraft",
      "display_name": "Clay Ball",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "smelting": 1,
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {},
      "sample_ingredient_of": [
        "minecraft:brick",
        "minecraft:clay"
      ],
      "sample_output_of": [],
      "model_parents": [
        "item/clay_ball",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/clay",
        "minecraft:chests/village/village_desert_house",
        "minecraft:chests/village/village_mason"
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
        "processing_in": {
          "values": [
            "crafting",
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "village"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:clock",
      "namespace": "minecraft",
      "display_name": "Clock",
      "minecraft_tags_direct": [
        "minecraft:piglin_loved"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:clock"
      ],
      "model_parents": [
        "item/clock_00",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/ruined_portal",
        "minecraft:chests/shipwreck_map"
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
        "origin": {
          "values": [
            "overworld_ocean",
            "ruined_portal"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:closed_eyeblossom",
      "namespace": "minecraft",
      "display_name": "Closed Eyeblossom",
      "minecraft_tags_direct": [
        "minecraft:small_flowers"
      ],
      "minecraft_tags_inherited": [
        "minecraft:flowers"
      ],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {},
      "sample_ingredient_of": [
        "minecraft:gray_dye_from_closed_eyeblossom",
        "minecraft:suspicious_stew_from_closed_eyeblossom"
      ],
      "sample_output_of": [],
      "model_parents": [
        "item/closed_eyeblossom",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/closed_eyeblossom",
        "minecraft:blocks/potted_closed_eyeblossom"
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
      "id": "minecraft:coal",
      "namespace": "minecraft",
      "display_name": "Coal",
      "minecraft_tags_direct": [
        "minecraft:coals",
        "minecraft:furnace_minecart_fuel"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 5,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "blasting": 2,
        "smelting": 2
      },
      "sample_ingredient_of": [
        "minecraft:campfire",
        "minecraft:coal_block",
        "minecraft:copper_torch",
        "minecraft:fire_charge",
        "minecraft:soul_torch",
        "minecraft:torch"
      ],
      "sample_output_of": [
        "minecraft:coal",
        "minecraft:coal_from_blasting_coal_ore",
        "minecraft:coal_from_blasting_deepslate_coal_ore",
        "minecraft:coal_from_smelting_coal_ore",
        "minecraft:coal_from_smelting_deepslate_coal_ore"
      ],
      "model_parents": [
        "item/coal",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:archaeology/ocean_ruin_cold",
        "minecraft:archaeology/ocean_ruin_warm",
        "minecraft:archaeology/trail_ruins_common",
        "minecraft:blocks/coal_ore",
        "minecraft:blocks/deepslate_coal_ore",
        "minecraft:chests/abandoned_mineshaft",
        "minecraft:chests/ancient_city",
        "minecraft:chests/igloo_chest",
        "minecraft:chests/shipwreck_supply",
        "minecraft:chests/simple_dungeon"
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
            "ancient_city",
            "archaeology_site",
            "mineshaft",
            "mob_drop",
            "overworld_cave",
            "overworld_ocean",
            "stronghold",
            "village",
            "woodland_mansion"
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
      "id": "minecraft:coal_block",
      "namespace": "minecraft",
      "display_name": "Block of Coal",
      "minecraft_tags_direct": [
        "minecraft:sulfur_cube_archetype/regular"
      ],
      "minecraft_tags_inherited": [
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:coal"
      ],
      "sample_output_of": [
        "minecraft:coal_block"
      ],
      "model_parents": [
        "block/coal_block",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/coal_block"
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
    }
  ]
}
Respond with a single JSON object matching the expected output shape above. No other text.