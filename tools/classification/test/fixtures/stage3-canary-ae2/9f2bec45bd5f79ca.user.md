# Items to classify
{
  "items": [
    {
      "id": "ae2:orange_paint_ball",
      "namespace": "ae2",
      "display_name": "Orange Paint Ball",
      "minecraft_tags_direct": [
        "ae2:paint_balls"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "ae2:tools/paintballs_lumen_orange"
      ],
      "sample_output_of": [
        "ae2:tools/paintballs_orange"
      ],
      "model_parents": [],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {},
      "stage2_facets": {
        "mod_namespace": {
          "value": "ae2",
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
      "id": "ae2:orange_smart_cable",
      "namespace": "ae2",
      "display_name": "Orange ME Smart Cable",
      "minecraft_tags_direct": [
        "ae2:smart_cable"
      ],
      "minecraft_tags_inherited": [
        "ae2:p2p_attunements/me_p2p_tunnel"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "ae2:network/cables/smart_orange"
      ],
      "model_parents": [],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {},
      "stage2_facets": {
        "mod_namespace": {
          "value": "ae2",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "ae2:orange_smart_dense_cable",
      "namespace": "ae2",
      "display_name": "Orange ME Dense Smart Cable",
      "minecraft_tags_direct": [
        "ae2:smart_dense_cable"
      ],
      "minecraft_tags_inherited": [
        "ae2:p2p_attunements/me_p2p_tunnel"
      ],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "ae2:network/blocks/quantum_ring"
      ],
      "sample_output_of": [
        "ae2:network/cables/dense_smart_orange"
      ],
      "model_parents": [],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {},
      "stage2_facets": {
        "mod_namespace": {
          "value": "ae2",
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
      "id": "ae2:paint",
      "namespace": "ae2",
      "display_name": "Paint",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [],
      "sample_loot_sources": [
        "ae2:blocks/paint"
      ],
      "lore": [],
      "component_highlights": {},
      "stage2_facets": {
        "mod_namespace": {
          "value": "ae2",
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
      "id": "ae2:pattern_access_terminal",
      "namespace": "ae2",
      "display_name": "ME Pattern Access Terminal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "ae2:network/parts/terminals_pattern_access"
      ],
      "model_parents": [],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {},
      "stage2_facets": {
        "mod_namespace": {
          "value": "ae2",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    }
  ]
}
Respond with a single JSON object matching the expected output shape above. No other text.