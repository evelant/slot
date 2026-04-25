# Items to classify
{
  "items": [
    {
      "id": "sophisticatedstorage:alchemy_upgrade",
      "namespace": "sophisticatedstorage",
      "display_name": "Alchemy Upgrade",
      "minecraft_tags_direct": [
        "sophisticatedstorage:upgrade"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting",
        "sophisticatedcore:upgrade_next_tier"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "sophisticatedcore:upgrade_next_tier": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "sample_ingredient_of": [
        "sophisticatedstorage:advanced_alchemy_upgrade",
        "sophisticatedstorage:backpack_alchemy_upgrade_from_storage_alchemy_upgrade"
      ],
      "sample_output_of": [
        "sophisticatedstorage:alchemy_upgrade",
        "sophisticatedstorage:storage_alchemy_upgrade_from_backpack_alchemy_upgrade"
      ],
      "model_parents": [],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {},
      "stage2_facets": {
        "mod_namespace": {
          "value": "sophisticatedstorage",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "processing_in": {
          "values": [
            "crafting",
            "sophisticatedcore:upgrade_next_tier"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "sophisticatedstorage:auto_blasting_upgrade",
      "namespace": "sophisticatedstorage",
      "display_name": "Auto-blasting Upgrade",
      "minecraft_tags_direct": [
        "sophisticatedstorage:upgrade"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "sophisticatedcore:upgrade_next_tier": 1,
        "crafting_shaped": 2
      },
      "sample_ingredient_of": [
        "sophisticatedstorage:backpack_auto_blasting_upgrade_from_storage_auto_blasting_upgrade"
      ],
      "sample_output_of": [
        "sophisticatedstorage:auto_blasting_upgrade",
        "sophisticatedstorage:auto_blasting_upgrade_from_auto_smelting_upgrade",
        "sophisticatedstorage:storage_auto_blasting_upgrade_from_backpack_auto_blasting_upgrade"
      ],
      "model_parents": [],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {},
      "stage2_facets": {
        "mod_namespace": {
          "value": "sophisticatedstorage",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "sophisticatedstorage:auto_smelting_upgrade",
      "namespace": "sophisticatedstorage",
      "display_name": "Auto-smelting Upgrade",
      "minecraft_tags_direct": [
        "sophisticatedstorage:upgrade"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 3
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "sophisticatedcore:upgrade_next_tier": 1
      },
      "sample_ingredient_of": [
        "sophisticatedstorage:auto_blasting_upgrade_from_auto_smelting_upgrade",
        "sophisticatedstorage:auto_smoking_upgrade_from_auto_smelting_upgrade",
        "sophisticatedstorage:backpack_auto_smelting_upgrade_from_storage_auto_smelting_upgrade"
      ],
      "sample_output_of": [
        "sophisticatedstorage:auto_smelting_upgrade",
        "sophisticatedstorage:storage_auto_smelting_upgrade_from_backpack_auto_smelting_upgrade"
      ],
      "model_parents": [],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {},
      "stage2_facets": {
        "mod_namespace": {
          "value": "sophisticatedstorage",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "sophisticatedstorage:auto_smoking_upgrade",
      "namespace": "sophisticatedstorage",
      "display_name": "Auto-smoking Upgrade",
      "minecraft_tags_direct": [
        "sophisticatedstorage:upgrade"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "sophisticatedcore:upgrade_next_tier": 1
      },
      "sample_ingredient_of": [
        "sophisticatedstorage:backpack_auto_smoking_upgrade_from_storage_auto_smoking_upgrade"
      ],
      "sample_output_of": [
        "sophisticatedstorage:auto_smoking_upgrade",
        "sophisticatedstorage:auto_smoking_upgrade_from_auto_smelting_upgrade",
        "sophisticatedstorage:storage_auto_smoking_upgrade_from_backpack_auto_smoking_upgrade"
      ],
      "model_parents": [],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {},
      "stage2_facets": {
        "mod_namespace": {
          "value": "sophisticatedstorage",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "sophisticatedstorage:barrel",
      "namespace": "sophisticatedstorage",
      "display_name": "%s%sBarrel",
      "minecraft_tags_direct": [
        "c:barrels",
        "c:barrels/wooden",
        "sophisticatedstorage:all_storage",
        "sophisticatedstorage:base_tier_wooden_storage"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "sophisticatedstorage:storage_tier_upgrade"
      ],
      "recipe_consumption_by_type": {
        "sophisticatedstorage:storage_tier_upgrade": 2
      },
      "recipe_production_by_type": {
        "sophisticatedstorage:generic_wood_storage": 1,
        "crafting_shaped": 11,
        "crafting_shapeless": 1
      },
      "sample_ingredient_of": [
        "sophisticatedstorage:copper_barrel",
        "sophisticatedstorage:iron_barrel"
      ],
      "sample_output_of": [
        "sophisticatedstorage:acacia_barrel",
        "sophisticatedstorage:bamboo_barrel",
        "sophisticatedstorage:birch_barrel",
        "sophisticatedstorage:cherry_barrel",
        "sophisticatedstorage:crimson_barrel",
        "sophisticatedstorage:dark_oak_barrel",
        "sophisticatedstorage:generic_barrel",
        "sophisticatedstorage:jungle_barrel",
        "sophisticatedstorage:mangrove_barrel",
        "sophisticatedstorage:oak_barrel"
      ],
      "model_parents": [],
      "sample_loot_sources": [
        "sophisticatedstorage:blocks/barrel"
      ],
      "lore": [],
      "component_highlights": {},
      "stage2_facets": {
        "mod_namespace": {
          "value": "sophisticatedstorage",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
            "sophisticatedstorage:storage_tier_upgrade"
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
Respond with a single JSON object matching the expected output shape above. No other text.