# Items to classify
{
  "items": [
    {
      "id": "ae2:debug_meteorite_placer",
      "namespace": "ae2",
      "display_name": "Dev.MeteoritePlacer",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
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
      "id": "ae2:debug_phantom_node",
      "namespace": "ae2",
      "display_name": "Dev.PhantomNode",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [],
      "sample_loot_sources": [
        "ae2:blocks/debug_phantom_node"
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
      "id": "ae2:debug_replicator_card",
      "namespace": "ae2",
      "display_name": "Dev.ReplicatorCard",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
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
      "id": "ae2:dense_energy_cell",
      "namespace": "ae2",
      "display_name": "Dense Energy Cell",
      "minecraft_tags_direct": [
        "ae2:p2p_attunements/fe_p2p_tunnel"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 3
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "ae2:materials/cardenergy",
        "ae2:network/wireless_crafting_terminal",
        "ae2:network/wireless_terminal"
      ],
      "sample_output_of": [
        "ae2:network/blocks/energy_dense_energy_cell"
      ],
      "model_parents": [],
      "sample_loot_sources": [
        "ae2:blocks/dense_energy_cell"
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
      "id": "ae2:drive",
      "namespace": "ae2",
      "display_name": "ME Drive",
      "minecraft_tags_direct": [],
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
        "ae2:network/blocks/io_port"
      ],
      "sample_output_of": [
        "ae2:network/blocks/storage_drive"
      ],
      "model_parents": [],
      "sample_loot_sources": [
        "ae2:blocks/drive"
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
    }
  ]
}
Respond with a single JSON object matching the expected output shape above. No other text.