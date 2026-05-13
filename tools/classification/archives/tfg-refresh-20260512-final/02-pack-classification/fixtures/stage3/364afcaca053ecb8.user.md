# Items to classify
{
  "items": [
    {
      "id": "mcw_tfc_aio:bridges/palm_bridges/palm_rail_bridge",
      "namespace": "mcw_tfc_aio",
      "display_name": "Palm Rail Bridge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/palm_bridges/palm_rail_bridge"
      ],
      "model_parents": [
        "item/bridges/palm_bridges/palm_rail_bridge",
        "block/bridge/rail/palm/palm",
        "block/bridge/rail/parent/rail"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/palm_bridges/palm_rail_bridge"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/palm_bridges/palm_rail_bridge",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:rail_bridges",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/palm_bridges/palm_rope_bridge_stair",
      "namespace": "mcw_tfc_aio",
      "display_name": "Rope Palm Bridge Stair",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/palm_bridges/palm_rope_bridge_stair_recycle"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/palm_bridges/palm_rope_bridge_stair"
      ],
      "model_parents": [
        "item/bridges/palm_bridges/palm_rope_bridge_stair",
        "block/stair/rope/palm/palm_double",
        "block/stair/rope/parent/double"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/palm_bridges/palm_rope_bridge_stair"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/palm_bridges/palm_rope_bridge_stair",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:rope_stairs",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/palm_bridges/rope_palm_bridge",
      "namespace": "mcw_tfc_aio",
      "display_name": "Rope Palm Bridge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/palm_bridges/palm_rope_bridge_stair"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/palm_bridges/palm_rope_bridge_stair_recycle",
        "mcw_tfc_aio:bridges/palm_bridges/rope_palm_bridge"
      ],
      "model_parents": [
        "item/bridges/palm_bridges/rope_palm_bridge",
        "block/bridge/rope/palm/palm_inventory",
        "block/bridge/rope/parent/inventory"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/palm_bridges/rope_palm_bridge"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/palm_bridges/rope_palm_bridge",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:rope_bridges",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/pine_bridges/pine_bridge_pier",
      "namespace": "mcw_tfc_aio",
      "display_name": "Pine Bridge Support",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/pine_bridges/pine_bridge_pier"
      ],
      "model_parents": [
        "item/bridges/pine_bridges/pine_bridge_pier",
        "block/support_pier/wooden/pine/pine_pillar_single",
        "block/support_pier/wooden/parent/pillar_single"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/pine_bridges/pine_bridge_pier"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/pine_bridges/pine_bridge_pier",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:wooden_piers",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/pine_bridges/pine_log_bridge_middle",
      "namespace": "mcw_tfc_aio",
      "display_name": "Pine Bridge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/pine_bridges/pine_log_bridge_stair"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/pine_bridges/pine_log_bridge_middle",
        "mcw_tfc_aio:bridges/pine_bridges/pine_log_bridge_stair_recycle"
      ],
      "model_parents": [
        "item/bridges/pine_bridges/pine_log_bridge_middle",
        "block/bridge/bridge_wood/pine/pine_middle",
        "block/bridge/bridge_wood/parent/middle"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/pine_bridges/pine_log_bridge_middle"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/pine_bridges/pine_log_bridge_middle",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:log_bridges",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/pine_bridges/pine_log_bridge_stair",
      "namespace": "mcw_tfc_aio",
      "display_name": "Pine Bridge Stair",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/pine_bridges/pine_log_bridge_stair_recycle"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/pine_bridges/pine_log_bridge_stair"
      ],
      "model_parents": [
        "item/bridges/pine_bridges/pine_log_bridge_stair",
        "block/stair/wood/pine/pine_double",
        "block/stair/wood/parent/double"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/pine_bridges/pine_log_bridge_stair"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/pine_bridges/pine_log_bridge_stair",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:log_stairs",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/pine_bridges/pine_rail_bridge",
      "namespace": "mcw_tfc_aio",
      "display_name": "Pine Rail Bridge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/pine_bridges/pine_rail_bridge"
      ],
      "model_parents": [
        "item/bridges/pine_bridges/pine_rail_bridge",
        "block/bridge/rail/pine/pine",
        "block/bridge/rail/parent/rail"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/pine_bridges/pine_rail_bridge"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/pine_bridges/pine_rail_bridge",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:rail_bridges",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/pine_bridges/pine_rope_bridge_stair",
      "namespace": "mcw_tfc_aio",
      "display_name": "Rope Pine Bridge Stair",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/pine_bridges/pine_rope_bridge_stair_recycle"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/pine_bridges/pine_rope_bridge_stair"
      ],
      "model_parents": [
        "item/bridges/pine_bridges/pine_rope_bridge_stair",
        "block/stair/rope/pine/pine_double",
        "block/stair/rope/parent/double"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/pine_bridges/pine_rope_bridge_stair"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/pine_bridges/pine_rope_bridge_stair",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:rope_stairs",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/pine_bridges/rope_pine_bridge",
      "namespace": "mcw_tfc_aio",
      "display_name": "Rope Pine Bridge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/pine_bridges/pine_rope_bridge_stair"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/pine_bridges/pine_rope_bridge_stair_recycle",
        "mcw_tfc_aio:bridges/pine_bridges/rope_pine_bridge"
      ],
      "model_parents": [
        "item/bridges/pine_bridges/rope_pine_bridge",
        "block/bridge/rope/pine/pine_inventory",
        "block/bridge/rope/parent/inventory"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/pine_bridges/rope_pine_bridge"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/pine_bridges/rope_pine_bridge",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:rope_bridges",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/rosewood_bridges/rope_rosewood_bridge",
      "namespace": "mcw_tfc_aio",
      "display_name": "Rope Rosewood Bridge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_rope_bridge_stair"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/rosewood_bridges/rope_rosewood_bridge",
        "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_rope_bridge_stair_recycle"
      ],
      "model_parents": [
        "item/bridges/rosewood_bridges/rope_rosewood_bridge",
        "block/bridge/rope/rosewood/rosewood_inventory",
        "block/bridge/rope/parent/inventory"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/rosewood_bridges/rope_rosewood_bridge"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/rosewood_bridges/rope_rosewood_bridge",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:rope_bridges",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_bridge_pier",
      "namespace": "mcw_tfc_aio",
      "display_name": "Rosewood Bridge Support",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_bridge_pier"
      ],
      "model_parents": [
        "item/bridges/rosewood_bridges/rosewood_bridge_pier",
        "block/support_pier/wooden/rosewood/rosewood_pillar_single",
        "block/support_pier/wooden/parent/pillar_single"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/rosewood_bridges/rosewood_bridge_pier"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_bridge_pier",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:wooden_piers",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_log_bridge_middle",
      "namespace": "mcw_tfc_aio",
      "display_name": "Rosewood Bridge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_log_bridge_stair"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_log_bridge_middle",
        "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_log_bridge_stair_recycle"
      ],
      "model_parents": [
        "item/bridges/rosewood_bridges/rosewood_log_bridge_middle",
        "block/bridge/bridge_wood/rosewood/rosewood_middle",
        "block/bridge/bridge_wood/parent/middle"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/rosewood_bridges/rosewood_log_bridge_middle"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_log_bridge_middle",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:log_bridges",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_log_bridge_stair",
      "namespace": "mcw_tfc_aio",
      "display_name": "Rosewood Bridge Stair",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_log_bridge_stair_recycle"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_log_bridge_stair"
      ],
      "model_parents": [
        "item/bridges/rosewood_bridges/rosewood_log_bridge_stair",
        "block/stair/wood/rosewood/rosewood_double",
        "block/stair/wood/parent/double"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/rosewood_bridges/rosewood_log_bridge_stair"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_log_bridge_stair",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:log_stairs",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_rail_bridge",
      "namespace": "mcw_tfc_aio",
      "display_name": "Rosewood Rail Bridge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_rail_bridge"
      ],
      "model_parents": [
        "item/bridges/rosewood_bridges/rosewood_rail_bridge",
        "block/bridge/rail/rosewood/rosewood",
        "block/bridge/rail/parent/rail"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/rosewood_bridges/rosewood_rail_bridge"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_rail_bridge",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:rail_bridges",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_rope_bridge_stair",
      "namespace": "mcw_tfc_aio",
      "display_name": "Rope Rosewood Bridge Stair",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_rope_bridge_stair_recycle"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_rope_bridge_stair"
      ],
      "model_parents": [
        "item/bridges/rosewood_bridges/rosewood_rope_bridge_stair",
        "block/stair/rope/rosewood/rosewood_double",
        "block/stair/rope/parent/double"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/rosewood_bridges/rosewood_rope_bridge_stair"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/rosewood_bridges/rosewood_rope_bridge_stair",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:rope_stairs",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/sequoia_bridges/rope_sequoia_bridge",
      "namespace": "mcw_tfc_aio",
      "display_name": "Rope Sequoia Bridge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_rope_bridge_stair"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/sequoia_bridges/rope_sequoia_bridge",
        "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_rope_bridge_stair_recycle"
      ],
      "model_parents": [
        "item/bridges/sequoia_bridges/rope_sequoia_bridge",
        "block/bridge/rope/sequoia/sequoia_inventory",
        "block/bridge/rope/parent/inventory"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/sequoia_bridges/rope_sequoia_bridge"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/sequoia_bridges/rope_sequoia_bridge",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:rope_bridges",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_bridge_pier",
      "namespace": "mcw_tfc_aio",
      "display_name": "Sequoia Bridge Support",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_bridge_pier"
      ],
      "model_parents": [
        "item/bridges/sequoia_bridges/sequoia_bridge_pier",
        "block/support_pier/wooden/sequoia/sequoia_pillar_single",
        "block/support_pier/wooden/parent/pillar_single"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/sequoia_bridges/sequoia_bridge_pier"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_bridge_pier",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:wooden_piers",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_log_bridge_middle",
      "namespace": "mcw_tfc_aio",
      "display_name": "Sequoia Bridge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_log_bridge_stair"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_log_bridge_middle",
        "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_log_bridge_stair_recycle"
      ],
      "model_parents": [
        "item/bridges/sequoia_bridges/sequoia_log_bridge_middle",
        "block/bridge/bridge_wood/sequoia/sequoia_middle",
        "block/bridge/bridge_wood/parent/middle"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/sequoia_bridges/sequoia_log_bridge_middle"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_log_bridge_middle",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:log_bridges",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_log_bridge_stair",
      "namespace": "mcw_tfc_aio",
      "display_name": "Sequoia Bridge Stair",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_log_bridge_stair_recycle"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_log_bridge_stair"
      ],
      "model_parents": [
        "item/bridges/sequoia_bridges/sequoia_log_bridge_stair",
        "block/stair/wood/sequoia/sequoia_double",
        "block/stair/wood/parent/double"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/sequoia_bridges/sequoia_log_bridge_stair"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_log_bridge_stair",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:log_stairs",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_rail_bridge",
      "namespace": "mcw_tfc_aio",
      "display_name": "Sequoia Rail Bridge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_rail_bridge"
      ],
      "model_parents": [
        "item/bridges/sequoia_bridges/sequoia_rail_bridge",
        "block/bridge/rail/sequoia/sequoia",
        "block/bridge/rail/parent/rail"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/sequoia_bridges/sequoia_rail_bridge"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_rail_bridge",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:rail_bridges",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_rope_bridge_stair",
      "namespace": "mcw_tfc_aio",
      "display_name": "Rope Sequoia Bridge Stair",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_rope_bridge_stair_recycle"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_rope_bridge_stair"
      ],
      "model_parents": [
        "item/bridges/sequoia_bridges/sequoia_rope_bridge_stair",
        "block/stair/rope/sequoia/sequoia_double",
        "block/stair/rope/parent/double"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/sequoia_bridges/sequoia_rope_bridge_stair"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/sequoia_bridges/sequoia_rope_bridge_stair",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:rope_stairs",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/spruce_bridges/rope_spruce_bridge",
      "namespace": "mcw_tfc_aio",
      "display_name": "Rope Spruce Bridge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/spruce_bridges/spruce_rope_bridge_stair"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/spruce_bridges/rope_spruce_bridge",
        "mcw_tfc_aio:bridges/spruce_bridges/spruce_rope_bridge_stair_recycle"
      ],
      "model_parents": [
        "item/bridges/spruce_bridges/rope_spruce_bridge",
        "block/bridge/rope/spruce/spruce_inventory",
        "block/bridge/rope/parent/inventory"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/spruce_bridges/rope_spruce_bridge"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/spruce_bridges/rope_spruce_bridge",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:rope_bridges",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/spruce_bridges/spruce_bridge_pier",
      "namespace": "mcw_tfc_aio",
      "display_name": "Spruce Bridge Support",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/spruce_bridges/spruce_bridge_pier"
      ],
      "model_parents": [
        "item/bridges/spruce_bridges/spruce_bridge_pier",
        "block/support_pier/wooden/spruce/spruce_pillar_single",
        "block/support_pier/wooden/parent/pillar_single"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/spruce_bridges/spruce_bridge_pier"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/spruce_bridges/spruce_bridge_pier",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:wooden_piers",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/spruce_bridges/spruce_log_bridge_middle",
      "namespace": "mcw_tfc_aio",
      "display_name": "Spruce Bridge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/spruce_bridges/spruce_log_bridge_stair"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/spruce_bridges/spruce_log_bridge_middle",
        "mcw_tfc_aio:bridges/spruce_bridges/spruce_log_bridge_stair_recycle"
      ],
      "model_parents": [
        "item/bridges/spruce_bridges/spruce_log_bridge_middle",
        "block/bridge/bridge_wood/spruce/spruce_middle",
        "block/bridge/bridge_wood/parent/middle"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/spruce_bridges/spruce_log_bridge_middle"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/spruce_bridges/spruce_log_bridge_middle",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:log_bridges",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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
      "id": "mcw_tfc_aio:bridges/spruce_bridges/spruce_log_bridge_stair",
      "namespace": "mcw_tfc_aio",
      "display_name": "Spruce Bridge Stair",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:bridges"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:bridges/spruce_bridges/spruce_log_bridge_stair_recycle"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:bridges/spruce_bridges/spruce_log_bridge_stair"
      ],
      "model_parents": [
        "item/bridges/spruce_bridges/spruce_log_bridge_stair",
        "block/stair/wood/spruce/spruce_double",
        "block/stair/wood/parent/double"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:bridgesitmegroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/bridges/spruce_bridges/spruce_log_bridge_stair"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:bridges/spruce_bridges/spruce_log_bridge_stair",
        "block_tags": [
          "cucumber:mineable/paxel",
          "mcw_tfc_aio:log_stairs",
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
          "text": "Macaw's All-In-One for TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "mcw_tfc_aio",
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