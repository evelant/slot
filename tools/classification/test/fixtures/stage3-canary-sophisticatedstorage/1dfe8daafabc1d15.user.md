# Items to classify
{
  "items": [
    {
      "id": "sophisticatedstorage:basic_to_netherite_tier_upgrade",
      "namespace": "sophisticatedstorage",
      "display_name": "Basic to Netherite Tier Upgrade",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "sophisticatedstorage:basic_to_netherite_tier_upgrade"
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
        }
      }
    },
    {
      "id": "sophisticatedstorage:blasting_upgrade",
      "namespace": "sophisticatedstorage",
      "display_name": "Blasting Upgrade",
      "minecraft_tags_direct": [
        "sophisticatedstorage:upgrade"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting",
        "sophisticatedcore:upgrade_next_tier"
      ],
      "recipe_consumption_by_type": {
        "sophisticatedcore:upgrade_next_tier": 1,
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 3
      },
      "sample_ingredient_of": [
        "sophisticatedstorage:auto_blasting_upgrade",
        "sophisticatedstorage:backpack_blasting_upgrade_from_storage_blasting_upgrade"
      ],
      "sample_output_of": [
        "sophisticatedstorage:blasting_upgrade",
        "sophisticatedstorage:blasting_upgrade_from_smelting_upgrade",
        "sophisticatedstorage:storage_blasting_upgrade_from_backpack_blasting_upgrade"
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
      "id": "sophisticatedstorage:chest",
      "namespace": "sophisticatedstorage",
      "display_name": "%s%sChest",
      "minecraft_tags_direct": [
        "c:chests",
        "c:chests/wooden",
        "sophisticatedstorage:all_storage",
        "sophisticatedstorage:base_tier_wooden_storage"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting",
        "sophisticatedstorage:double_chest_tier_upgrade",
        "sophisticatedstorage:shulker_box_from_chest",
        "sophisticatedstorage:storage_tier_upgrade"
      ],
      "recipe_consumption_by_type": {
        "sophisticatedstorage:double_chest_tier_upgrade": 2,
        "crafting_shaped": 2,
        "sophisticatedstorage:storage_tier_upgrade": 2,
        "sophisticatedstorage:shulker_box_from_chest": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 12,
        "crafting_shaped": 11,
        "sophisticatedstorage:generic_wood_storage": 1
      },
      "sample_ingredient_of": [
        "sophisticatedstorage:copper_chest",
        "sophisticatedstorage:crafting_upgrade",
        "sophisticatedstorage:double_copper_chest",
        "sophisticatedstorage:double_iron_chest",
        "sophisticatedstorage:iron_chest",
        "sophisticatedstorage:shulker_box",
        "sophisticatedstorage:shulker_from_chest"
      ],
      "sample_output_of": [
        "sophisticatedstorage:acacia_chest",
        "sophisticatedstorage:acacia_chest_from_quark_acacia_chest",
        "sophisticatedstorage:bamboo_chest",
        "sophisticatedstorage:bamboo_chest_from_quark_bamboo_chest",
        "sophisticatedstorage:birch_chest",
        "sophisticatedstorage:birch_chest_from_quark_birch_chest",
        "sophisticatedstorage:cherry_chest",
        "sophisticatedstorage:cherry_chest_from_quark_cherry_chest",
        "sophisticatedstorage:crimson_chest",
        "sophisticatedstorage:crimson_chest_from_quark_crimson_chest"
      ],
      "model_parents": [],
      "sample_loot_sources": [
        "sophisticatedstorage:blocks/chest"
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
            "crafting",
            "sophisticatedstorage:double_chest_tier_upgrade",
            "sophisticatedstorage:shulker_box_from_chest",
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
    },
    {
      "id": "sophisticatedstorage:compacting_upgrade",
      "namespace": "sophisticatedstorage",
      "display_name": "Compacting Upgrade",
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
        "sophisticatedstorage:advanced_compacting_upgrade",
        "sophisticatedstorage:backpack_compacting_upgrade_from_storage_compacting_upgrade"
      ],
      "sample_output_of": [
        "sophisticatedstorage:compacting_upgrade",
        "sophisticatedstorage:storage_compacting_upgrade_from_backpack_compacting_upgrade"
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
      "id": "sophisticatedstorage:compression_upgrade",
      "namespace": "sophisticatedstorage",
      "display_name": "Compression Upgrade",
      "minecraft_tags_direct": [
        "sophisticatedstorage:upgrade"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "sophisticatedstorage:compression_upgrade"
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
        }
      }
    }
  ]
}
Respond with a single JSON object matching the expected output shape above. No other text.