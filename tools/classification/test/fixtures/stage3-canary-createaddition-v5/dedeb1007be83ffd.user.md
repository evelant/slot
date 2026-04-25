# Items to classify
{
  "items": [
    {
      "id": "createaddition:electrum_amulet",
      "namespace": "createaddition",
      "display_name": "Pale Gold Amulet",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "createaddition:crafting/electrum_amulet"
      ],
      "model_parents": [],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {},
      "stage2_facets": {
        "mod_namespace": {
          "value": "createaddition",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "createaddition:electrum_block",
      "namespace": "createaddition",
      "display_name": "Electrum Block",
      "minecraft_tags_direct": [
        "c:blocks/electrum",
        "c:storage_blocks",
        "c:storage_blocks/electrum"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "createaddition:charging": 1
      },
      "sample_ingredient_of": [
        "createaddition:crafting/electrum_ingot_from_electrum_block"
      ],
      "sample_output_of": [
        "createaddition:charging/electrify_gold_block",
        "createaddition:crafting/electrum_block"
      ],
      "model_parents": [],
      "sample_loot_sources": [
        "createaddition:blocks/electrum_block"
      ],
      "lore": [],
      "component_highlights": {},
      "stage2_facets": {
        "mod_namespace": {
          "value": "createaddition",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "createaddition:electrum_ingot",
      "namespace": "createaddition",
      "display_name": "Electrum Ingot",
      "minecraft_tags_direct": [
        "c:ingots/electrum"
      ],
      "minecraft_tags_inherited": [
        "c:ingots",
        "minecraft:beacon_payment_items"
      ],
      "processing_in": [
        "crafting",
        "create:pressing",
        "createaddition:rolling"
      ],
      "recipe_consumption_by_type": {
        "createaddition:rolling": 1,
        "create:pressing": 1,
        "crafting_shaped": 2,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "create:mixing": 1,
        "crafting_shapeless": 1,
        "crafting_shaped": 1,
        "createaddition:charging": 1
      },
      "sample_ingredient_of": [
        "createaddition:crafting/electrum_amulet",
        "createaddition:crafting/electrum_block",
        "createaddition:crafting/electrum_nugget",
        "createaddition:pressing/electrum_ingot",
        "createaddition:rolling/electrum_ingot"
      ],
      "sample_output_of": [
        "createaddition:charging/electrify_gold_ingot",
        "createaddition:crafting/electrum_ingot_from_electrum_block",
        "createaddition:crafting/electrum_ingot_from_nugget",
        "createaddition:mixing/electrum"
      ],
      "model_parents": [],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {},
      "stage2_facets": {
        "mod_namespace": {
          "value": "createaddition",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
            "create:pressing",
            "createaddition:rolling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "createaddition:electrum_nugget",
      "namespace": "createaddition",
      "display_name": "Electrum Nugget",
      "minecraft_tags_direct": [
        "c:nuggets/electrum"
      ],
      "minecraft_tags_inherited": [
        "c:nuggets"
      ],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "create:crushing": 2,
        "createaddition:charging": 1
      },
      "sample_ingredient_of": [
        "createaddition:crafting/electrum_ingot_from_nugget"
      ],
      "sample_output_of": [
        "createaddition:charging/electrify_gold_nugget",
        "createaddition:crafting/electrum_nugget",
        "createaddition:crushing/ochrum_recycling",
        "createaddition:crushing/tuff_recycling"
      ],
      "model_parents": [],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {},
      "stage2_facets": {
        "mod_namespace": {
          "value": "createaddition",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "form": {
          "value": "nugget",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _nugget"
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
      "id": "createaddition:electrum_rod",
      "namespace": "createaddition",
      "display_name": "Electrum Rod",
      "minecraft_tags_direct": [
        "c:rods/all_metal",
        "c:rods/electrum"
      ],
      "minecraft_tags_inherited": [
        "c:rods",
        "createaddition:large_connector_usable_rods"
      ],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "createaddition:rolling": 1,
        "createaddition:charging": 1
      },
      "sample_ingredient_of": [
        "createaddition:crafting/large_connector"
      ],
      "sample_output_of": [
        "createaddition:charging/electrify_gold_rod",
        "createaddition:rolling/electrum_ingot"
      ],
      "model_parents": [],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {},
      "stage2_facets": {
        "mod_namespace": {
          "value": "createaddition",
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
    }
  ]
}
Respond with a single JSON object matching the expected output shape above. No other text.