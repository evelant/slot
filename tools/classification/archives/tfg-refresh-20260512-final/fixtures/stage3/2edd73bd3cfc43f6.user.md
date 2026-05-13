# Items to classify
{
  "items": [
    {
      "id": "mcw_tfc_aio:roofs/ash_roofs/ash_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Ash Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:ash_log_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/ash_roofs/ash_log_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/ash_roofs/ash_roof"
      ],
      "model_parents": [
        "item/roofs/ash_roofs/ash_roof",
        "block/roof/ash_roof",
        "block/parent/roof"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/ash_roofs/ash_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/ash_roofs/ash_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/ash_roofs/ash_steep_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Ash Steep Base Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:ash_log_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/ash_roofs/ash_log_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/ash_roofs/ash_steep_roof"
      ],
      "model_parents": [
        "item/roofs/ash_roofs/ash_steep_roof",
        "block/steep/ash_steep",
        "block/parent/xx_steep"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/ash_roofs/ash_steep_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/ash_roofs/ash_steep_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/ash_roofs/ash_top_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Ash Top Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:ash_log_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/ash_roofs/ash_log_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/ash_roofs/ash_top_roof"
      ],
      "model_parents": [
        "item/roofs/ash_roofs/ash_top_roof",
        "block/top/ash_top_roof",
        "block/parent/top_roof"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/ash_roofs/ash_top_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/ash_roofs/ash_top_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/ash_roofs/ash_upper_lower_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Ash Lower Top Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:ash_log_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/ash_roofs/ash_log_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/ash_roofs/ash_upper_lower_roof"
      ],
      "model_parents": [
        "item/roofs/ash_roofs/ash_upper_lower_roof",
        "block/lower/ash_upper_lower",
        "block/parent/xx_upper_lower"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/ash_roofs/ash_upper_lower_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/ash_roofs/ash_upper_lower_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/ash_roofs/ash_upper_steep_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Ash Steep Top Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:ash_log_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/ash_roofs/ash_log_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/ash_roofs/ash_upper_steep_roof"
      ],
      "model_parents": [
        "item/roofs/ash_roofs/ash_upper_steep_roof",
        "block/steep/ash_upper_steep",
        "block/parent/xx_upper_steep"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/ash_roofs/ash_upper_steep_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/ash_roofs/ash_upper_steep_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_attic_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Aspen Attic Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:aspen_log_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_log_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_attic_roof"
      ],
      "model_parents": [
        "item/roofs/aspen_roofs/aspen_attic_roof",
        "block/attic/aspen_attic_roof_closed",
        "block/parent/attic_roof_closed"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/aspen_roofs/aspen_attic_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_attic_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_lower_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Aspen Lower Base Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:aspen_log_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_log_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_lower_roof"
      ],
      "model_parents": [
        "item/roofs/aspen_roofs/aspen_lower_roof",
        "block/lower/aspen_lower",
        "block/parent/xx_lower"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/aspen_roofs/aspen_lower_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_lower_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_attic_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Aspen Planks Attic Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:aspen_planks_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_plank_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_attic_roof"
      ],
      "model_parents": [
        "item/roofs/aspen_roofs/aspen_planks_attic_roof",
        "block/attic/aspen_planks_attic_roof_closed",
        "block/parent/attic_roof_closed"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/aspen_roofs/aspen_planks_attic_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_attic_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_lower_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Aspen Planks Lower Base Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:aspen_planks_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_plank_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_lower_roof"
      ],
      "model_parents": [
        "item/roofs/aspen_roofs/aspen_planks_lower_roof",
        "block/lower/aspen_planks_lower",
        "block/parent/xx_lower"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/aspen_roofs/aspen_planks_lower_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_lower_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Aspen Planks Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:aspen_planks_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_plank_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_roof"
      ],
      "model_parents": [
        "item/roofs/aspen_roofs/aspen_planks_roof",
        "block/roof/aspen_planks_roof",
        "block/parent/roof"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/aspen_roofs/aspen_planks_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_steep_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Aspen Planks Steep Base Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:aspen_planks_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_plank_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_steep_roof"
      ],
      "model_parents": [
        "item/roofs/aspen_roofs/aspen_planks_steep_roof",
        "block/steep/aspen_planks_steep",
        "block/parent/xx_steep"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/aspen_roofs/aspen_planks_steep_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_steep_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_top_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Aspen Planks Top Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:aspen_planks_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_plank_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_top_roof"
      ],
      "model_parents": [
        "item/roofs/aspen_roofs/aspen_planks_top_roof",
        "block/top/aspen_planks_top_roof",
        "block/parent/top_roof"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/aspen_roofs/aspen_planks_top_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_top_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_upper_lower_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Aspen Planks Lower Top Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:aspen_planks_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_plank_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_upper_lower_roof"
      ],
      "model_parents": [
        "item/roofs/aspen_roofs/aspen_planks_upper_lower_roof",
        "block/lower/aspen_planks_upper_lower",
        "block/parent/xx_upper_lower"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/aspen_roofs/aspen_planks_upper_lower_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_upper_lower_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_upper_steep_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Aspen Planks Steep Top Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:aspen_planks_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_plank_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_upper_steep_roof"
      ],
      "model_parents": [
        "item/roofs/aspen_roofs/aspen_planks_upper_steep_roof",
        "block/steep/aspen_planks_upper_steep",
        "block/parent/xx_upper_steep"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/aspen_roofs/aspen_planks_upper_steep_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_upper_steep_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Aspen Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:aspen_log_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_log_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_roof"
      ],
      "model_parents": [
        "item/roofs/aspen_roofs/aspen_roof",
        "block/roof/aspen_roof",
        "block/parent/roof"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/aspen_roofs/aspen_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_steep_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Aspen Steep Base Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:aspen_log_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_log_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_steep_roof"
      ],
      "model_parents": [
        "item/roofs/aspen_roofs/aspen_steep_roof",
        "block/steep/aspen_steep",
        "block/parent/xx_steep"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/aspen_roofs/aspen_steep_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_steep_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_top_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Aspen Top Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:aspen_log_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_log_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_top_roof"
      ],
      "model_parents": [
        "item/roofs/aspen_roofs/aspen_top_roof",
        "block/top/aspen_top_roof",
        "block/parent/top_roof"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/aspen_roofs/aspen_top_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_top_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_upper_lower_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Aspen Lower Top Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:aspen_log_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_log_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_upper_lower_roof"
      ],
      "model_parents": [
        "item/roofs/aspen_roofs/aspen_upper_lower_roof",
        "block/lower/aspen_upper_lower",
        "block/parent/xx_upper_lower"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/aspen_roofs/aspen_upper_lower_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_upper_lower_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_upper_steep_roof",
      "namespace": "mcw_tfc_aio",
      "display_name": "Aspen Steep Top Roof",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:aspen_log_roofs",
        "mcw_tfc_aio:roofs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_log_roof_uncraft"
      ],
      "recipe_output_examples": [
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_upper_steep_roof"
      ],
      "model_parents": [
        "item/roofs/aspen_roofs/aspen_upper_steep_roof",
        "block/steep/aspen_upper_steep",
        "block/parent/xx_upper_steep"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:roofsitemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/aspen_roofs/aspen_upper_steep_roof"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/aspen_roofs/aspen_upper_steep_roof",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "mcw_tfc_aio:roofs/awnings/black_striped_awning",
      "namespace": "mcw_tfc_aio",
      "display_name": "Black Striped Awning",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:awnings"
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
        "mcw_tfc_aio:roofs/awnings/black_striped_awning"
      ],
      "model_parents": [
        "item/roofs/awnings/black_striped_awning",
        "block/awning/black_straight",
        "block/parent/awning_straight"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:gutteritemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/awnings/black_striped_awning"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/awnings/black_striped_awning",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
      "id": "mcw_tfc_aio:roofs/awnings/blue_striped_awning",
      "namespace": "mcw_tfc_aio",
      "display_name": "Blue Striped Awning",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:awnings"
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
        "mcw_tfc_aio:roofs/awnings/blue_striped_awning"
      ],
      "model_parents": [
        "item/roofs/awnings/blue_striped_awning",
        "block/awning/blue_straight",
        "block/parent/awning_straight"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:gutteritemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/awnings/blue_striped_awning"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/awnings/blue_striped_awning",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
      "id": "mcw_tfc_aio:roofs/awnings/brown_striped_awning",
      "namespace": "mcw_tfc_aio",
      "display_name": "Brown Striped Awning",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:awnings"
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
        "mcw_tfc_aio:roofs/awnings/brown_striped_awning"
      ],
      "model_parents": [
        "item/roofs/awnings/brown_striped_awning",
        "block/awning/brown_straight",
        "block/parent/awning_straight"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:gutteritemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/awnings/brown_striped_awning"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/awnings/brown_striped_awning",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
      "id": "mcw_tfc_aio:roofs/awnings/cyan_striped_awning",
      "namespace": "mcw_tfc_aio",
      "display_name": "Cyan Striped Awning",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:awnings"
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
        "mcw_tfc_aio:roofs/awnings/cyan_striped_awning"
      ],
      "model_parents": [
        "item/roofs/awnings/cyan_striped_awning",
        "block/awning/cyan_straight",
        "block/parent/awning_straight"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:gutteritemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/awnings/cyan_striped_awning"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/awnings/cyan_striped_awning",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
      "id": "mcw_tfc_aio:roofs/awnings/gray_striped_awning",
      "namespace": "mcw_tfc_aio",
      "display_name": "Gray Striped Awning",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:awnings"
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
        "mcw_tfc_aio:roofs/awnings/gray_striped_awning"
      ],
      "model_parents": [
        "item/roofs/awnings/gray_striped_awning",
        "block/awning/gray_straight",
        "block/parent/awning_straight"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:gutteritemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/awnings/gray_striped_awning"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/awnings/gray_striped_awning",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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
      "id": "mcw_tfc_aio:roofs/awnings/green_striped_awning",
      "namespace": "mcw_tfc_aio",
      "display_name": "Green Striped Awning",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "mcw_tfc_aio:awnings"
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
        "mcw_tfc_aio:roofs/awnings/green_striped_awning"
      ],
      "model_parents": [
        "item/roofs/awnings/green_striped_awning",
        "block/awning/green_straight",
        "block/parent/awning_straight"
      ],
      "creative_tabs": [
        "mcw_tfc_aio:gutteritemgroup"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "mcw_tfc_aio:blocks/roofs/awnings/green_striped_awning"
      ],
      "block_context": {
        "block_id": "mcw_tfc_aio:roofs/awnings/green_striped_awning",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Allows mixed vertical-horizontal connections (relative to the placement)"
        },
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