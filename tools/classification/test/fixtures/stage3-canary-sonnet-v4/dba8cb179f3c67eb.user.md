# Items to classify
{
  "items": [
    {
      "id": "minecraft:netherite_upgrade_smithing_template",
      "namespace": "minecraft",
      "display_name": "Smithing Template",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting",
        "smithing"
      ],
      "recipe_consumption_by_type": {
        "smithing_transform": 12,
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:netherite_axe_smithing",
        "minecraft:netherite_boots_smithing",
        "minecraft:netherite_chestplate_smithing",
        "minecraft:netherite_helmet_smithing",
        "minecraft:netherite_hoe_smithing",
        "minecraft:netherite_horse_armor_smithing",
        "minecraft:netherite_leggings_smithing",
        "minecraft:netherite_nautilus_armor_smithing",
        "minecraft:netherite_pickaxe_smithing",
        "minecraft:netherite_shovel_smithing"
      ],
      "sample_output_of": [
        "minecraft:netherite_upgrade_smithing_template"
      ],
      "model_parents": [
        "item/netherite_upgrade_smithing_template",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/bastion_bridge",
        "minecraft:chests/bastion_hoglin_stable",
        "minecraft:chests/bastion_other",
        "minecraft:chests/bastion_treasure"
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
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "rarity": {
          "value": "uncommon",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = uncommon"
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
            "bastion"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:sentry_armor_trim_smithing_template",
      "namespace": "minecraft",
      "display_name": "Smithing Template",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting",
        "smithing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "smithing_trim": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:sentry_armor_trim_smithing_template",
        "minecraft:sentry_armor_trim_smithing_template_smithing_trim"
      ],
      "sample_output_of": [
        "minecraft:sentry_armor_trim_smithing_template"
      ],
      "model_parents": [
        "item/sentry_armor_trim_smithing_template",
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
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "rarity": {
          "value": "uncommon",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = uncommon"
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
            "pillager_outpost"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:angler_pottery_sherd",
      "namespace": "minecraft",
      "display_name": "Angler Pottery Sherd",
      "minecraft_tags_direct": [
        "minecraft:decorated_pot_sherds"
      ],
      "minecraft_tags_inherited": [
        "minecraft:decorated_pot_ingredients"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "item/angler_pottery_sherd",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:archaeology/ocean_ruin_warm"
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
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
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
            "archaeology_site"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:command_block",
      "namespace": "minecraft",
      "display_name": "Command Block",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "block/command_block",
        "block/template_command_block",
        "block/cube_directional",
        "block/block"
      ],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {
        "minecraft:rarity": "epic"
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
        "rarity": {
          "value": "unique",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = epic"
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
      "id": "minecraft:barrier",
      "namespace": "minecraft",
      "display_name": "Barrier",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "item/barrier",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {
        "minecraft:rarity": "epic"
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
        "rarity": {
          "value": "unique",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = epic"
        },
        "is_creative_only": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_creative_only_hardcoded",
          "rationale": "known vanilla creative-only item"
        }
      }
    }
  ]
}
Respond with a single JSON object matching the expected output shape above. No other text.