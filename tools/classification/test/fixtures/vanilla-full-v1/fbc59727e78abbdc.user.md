# Items to classify
{
  "items": [
    {
      "id": "minecraft:glowstone_dust",
      "namespace": "minecraft",
      "display_name": "Glowstone Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2
      },
      "recipe_production_by_type": {},
      "sample_ingredient_of": [
        "minecraft:glowstone",
        "minecraft:spectral_arrow"
      ],
      "sample_output_of": [],
      "model_parents": [
        "item/glowstone_dust",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/glowstone",
        "minecraft:entities/witch"
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
        "form": {
          "value": "dust",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _dust"
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
            "mob_drop",
            "nether"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:goat_horn",
      "namespace": "minecraft",
      "display_name": "Goat Horn",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "item/tooting_goat_horn",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/pillager_outpost"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "rarity": {
          "value": "uncommon",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = uncommon"
        },
        "origin": {
          "values": [
            "pillager_outpost"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:goat_spawn_egg",
      "namespace": "minecraft",
      "display_name": "Goat Spawn Egg",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "item/goat_spawn_egg",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [],
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
        }
      }
    },
    {
      "id": "minecraft:gold_block",
      "namespace": "minecraft",
      "display_name": "Block of Gold",
      "minecraft_tags_direct": [
        "minecraft:piglin_loved",
        "minecraft:sulfur_cube_archetype/slow_flat"
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
        "minecraft:gold_ingot_from_gold_block"
      ],
      "sample_output_of": [
        "minecraft:gold_block"
      ],
      "model_parents": [
        "block/gold_block",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/gold_block",
        "minecraft:chests/bastion_bridge",
        "minecraft:chests/bastion_hoglin_stable",
        "minecraft:chests/bastion_other",
        "minecraft:chests/bastion_treasure",
        "minecraft:chests/ruined_portal"
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
        "material_family": {
          "value": "gold",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "exact id minecraft:gold_block"
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
            "crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "bastion",
            "ruined_portal"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:gold_ingot",
      "namespace": "minecraft",
      "display_name": "Gold Ingot",
      "minecraft_tags_direct": [
        "minecraft:beacon_payment_items",
        "minecraft:gold_tool_materials",
        "minecraft:piglin_loved",
        "minecraft:repairs_gold_armor",
        "minecraft:trim_materials"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting",
        "smithing"
      ],
      "recipe_consumption_by_type": {
        "smithing_trim": 18,
        "crafting_shaped": 15,
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {
        "blasting": 4,
        "crafting_shapeless": 1,
        "crafting_shaped": 1,
        "smelting": 4
      },
      "sample_ingredient_of": [
        "minecraft:bolt_armor_trim_smithing_template_smithing_trim",
        "minecraft:clock",
        "minecraft:coast_armor_trim_smithing_template_smithing_trim",
        "minecraft:dune_armor_trim_smithing_template_smithing_trim",
        "minecraft:eye_armor_trim_smithing_template_smithing_trim",
        "minecraft:flow_armor_trim_smithing_template_smithing_trim",
        "minecraft:gold_block",
        "minecraft:gold_nugget",
        "minecraft:golden_apple",
        "minecraft:golden_axe"
      ],
      "sample_output_of": [
        "minecraft:gold_ingot_from_blasting_deepslate_gold_ore",
        "minecraft:gold_ingot_from_blasting_gold_ore",
        "minecraft:gold_ingot_from_blasting_nether_gold_ore",
        "minecraft:gold_ingot_from_blasting_raw_gold",
        "minecraft:gold_ingot_from_gold_block",
        "minecraft:gold_ingot_from_nuggets",
        "minecraft:gold_ingot_from_smelting_deepslate_gold_ore",
        "minecraft:gold_ingot_from_smelting_gold_ore",
        "minecraft:gold_ingot_from_smelting_nether_gold_ore",
        "minecraft:gold_ingot_from_smelting_raw_gold"
      ],
      "model_parents": [
        "item/gold_ingot",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/abandoned_mineshaft",
        "minecraft:chests/bastion_bridge",
        "minecraft:chests/bastion_other",
        "minecraft:chests/bastion_treasure",
        "minecraft:chests/buried_treasure",
        "minecraft:chests/desert_pyramid",
        "minecraft:chests/end_city_treasure",
        "minecraft:chests/jungle_temple",
        "minecraft:chests/nether_bridge",
        "minecraft:chests/ruined_portal"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:provides_trim_material": "minecraft:gold",
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
        "material_family": {
          "value": "gold",
          "confidence": 1,
          "source": "rule:material_family_from_tag",
          "rationale": "tag minecraft:gold_tool_materials"
        },
        "form": {
          "value": "ingot",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ingot"
        },
        "processing_in": {
          "values": [
            "crafting",
            "smithing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "bastion",
            "desert_temple",
            "end_city",
            "jungle_temple",
            "mineshaft",
            "mob_drop",
            "nether_fortress",
            "overworld_cave",
            "overworld_ocean",
            "ruined_portal",
            "stronghold",
            "village",
            "woodland_mansion"
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