# Items to classify
{
  "items": [
    {
      "id": "rnr:rock/flagstones/claystone",
      "namespace": "rnr",
      "display_name": "Claystone Flagstones",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/claystone_flagstones"
      ],
      "model_parents": [
        "item/rock/flagstones/claystone",
        "block/rock/flagstones/claystone",
        "block/path_block",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/claystone"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/claystone",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_blocks",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:rock/flagstones/claystone_slab",
      "namespace": "rnr",
      "display_name": "Claystone Flagstones Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1,
        "tfc:landslide": 2
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/claystone_flagstones_slab",
        "rnr:landslide/claystone_flagstones_stair",
        "rnr:mattock/slab/flagstones_claystone_slab"
      ],
      "model_parents": [
        "item/rock/flagstones/claystone_slab",
        "block/rock/flagstones/claystone_slab",
        "block/path_slab",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/claystone_slab"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/claystone_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_slabs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "slab",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _slab"
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
      "id": "rnr:rock/flagstones/claystone_stairs",
      "namespace": "rnr",
      "display_name": "Claystone Flagstones Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:mattock/stair/flagstones_claystone_stairs"
      ],
      "model_parents": [
        "item/rock/flagstones/claystone_stairs",
        "block/rock/flagstones/claystone_stairs",
        "block/path_stairs",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/claystone_stairs"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/claystone_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_stairs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _stairs"
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
      "id": "rnr:rock/flagstones/conglomerate",
      "namespace": "rnr",
      "display_name": "Conglomerate Flagstones",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/conglomerate_flagstones"
      ],
      "model_parents": [
        "item/rock/flagstones/conglomerate",
        "block/rock/flagstones/conglomerate",
        "block/path_block",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/conglomerate"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/conglomerate",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_blocks",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:rock/flagstones/conglomerate_slab",
      "namespace": "rnr",
      "display_name": "Conglomerate Flagstones Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1,
        "tfc:landslide": 2
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/conglomerate_flagstones_slab",
        "rnr:landslide/conglomerate_flagstones_stair",
        "rnr:mattock/slab/flagstones_conglomerate_slab"
      ],
      "model_parents": [
        "item/rock/flagstones/conglomerate_slab",
        "block/rock/flagstones/conglomerate_slab",
        "block/path_slab",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/conglomerate_slab"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/conglomerate_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_slabs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "slab",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _slab"
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
      "id": "rnr:rock/flagstones/conglomerate_stairs",
      "namespace": "rnr",
      "display_name": "Conglomerate Flagstones Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:mattock/stair/flagstones_conglomerate_stairs"
      ],
      "model_parents": [
        "item/rock/flagstones/conglomerate_stairs",
        "block/rock/flagstones/conglomerate_stairs",
        "block/path_stairs",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/conglomerate_stairs"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/conglomerate_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_stairs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _stairs"
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
      "id": "rnr:rock/flagstones/dacite",
      "namespace": "rnr",
      "display_name": "Dacite Flagstones",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/dacite_flagstones"
      ],
      "model_parents": [
        "item/rock/flagstones/dacite",
        "block/rock/flagstones/dacite",
        "block/path_block",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/dacite"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/dacite",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_blocks",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:rock/flagstones/dacite_slab",
      "namespace": "rnr",
      "display_name": "Dacite Flagstones Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1,
        "tfc:landslide": 2
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/dacite_flagstones_slab",
        "rnr:landslide/dacite_flagstones_stair",
        "rnr:mattock/slab/flagstones_dacite_slab"
      ],
      "model_parents": [
        "item/rock/flagstones/dacite_slab",
        "block/rock/flagstones/dacite_slab",
        "block/path_slab",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/dacite_slab"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/dacite_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_slabs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "slab",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _slab"
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
      "id": "rnr:rock/flagstones/dacite_stairs",
      "namespace": "rnr",
      "display_name": "Dacite Flagstones Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:mattock/stair/flagstones_dacite_stairs"
      ],
      "model_parents": [
        "item/rock/flagstones/dacite_stairs",
        "block/rock/flagstones/dacite_stairs",
        "block/path_stairs",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/dacite_stairs"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/dacite_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_stairs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _stairs"
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
      "id": "rnr:rock/flagstones/diorite",
      "namespace": "rnr",
      "display_name": "Diorite Flagstones",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/diorite_flagstones"
      ],
      "model_parents": [
        "item/rock/flagstones/diorite",
        "block/rock/flagstones/diorite",
        "block/path_block",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/diorite"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/diorite",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_blocks",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:rock/flagstones/diorite_slab",
      "namespace": "rnr",
      "display_name": "Diorite Flagstones Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1,
        "tfc:landslide": 2
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/diorite_flagstones_slab",
        "rnr:landslide/diorite_flagstones_stair",
        "rnr:mattock/slab/flagstones_diorite_slab"
      ],
      "model_parents": [
        "item/rock/flagstones/diorite_slab",
        "block/rock/flagstones/diorite_slab",
        "block/path_slab",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/diorite_slab"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/diorite_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_slabs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "slab",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _slab"
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
      "id": "rnr:rock/flagstones/diorite_stairs",
      "namespace": "rnr",
      "display_name": "Diorite Flagstones Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:mattock/stair/flagstones_diorite_stairs"
      ],
      "model_parents": [
        "item/rock/flagstones/diorite_stairs",
        "block/rock/flagstones/diorite_stairs",
        "block/path_stairs",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/diorite_stairs"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/diorite_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_stairs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _stairs"
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
      "id": "rnr:rock/flagstones/dolomite",
      "namespace": "rnr",
      "display_name": "Dolomite Flagstones",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/dolomite_flagstones"
      ],
      "model_parents": [
        "item/rock/flagstones/dolomite",
        "block/rock/flagstones/dolomite",
        "block/path_block",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/dolomite"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/dolomite",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_blocks",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:rock/flagstones/dolomite_slab",
      "namespace": "rnr",
      "display_name": "Dolomite Flagstones Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1,
        "tfc:landslide": 2
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/dolomite_flagstones_slab",
        "rnr:landslide/dolomite_flagstones_stair",
        "rnr:mattock/slab/flagstones_dolomite_slab"
      ],
      "model_parents": [
        "item/rock/flagstones/dolomite_slab",
        "block/rock/flagstones/dolomite_slab",
        "block/path_slab",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/dolomite_slab"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/dolomite_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_slabs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "slab",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _slab"
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
      "id": "rnr:rock/flagstones/dolomite_stairs",
      "namespace": "rnr",
      "display_name": "Dolomite Flagstones Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:mattock/stair/flagstones_dolomite_stairs"
      ],
      "model_parents": [
        "item/rock/flagstones/dolomite_stairs",
        "block/rock/flagstones/dolomite_stairs",
        "block/path_stairs",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/dolomite_stairs"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/dolomite_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_stairs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _stairs"
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
      "id": "rnr:rock/flagstones/gabbro",
      "namespace": "rnr",
      "display_name": "Gabbro Flagstones",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/gabbro_flagstones"
      ],
      "model_parents": [
        "item/rock/flagstones/gabbro",
        "block/rock/flagstones/gabbro",
        "block/path_block",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/gabbro"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/gabbro",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_blocks",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:rock/flagstones/gabbro_slab",
      "namespace": "rnr",
      "display_name": "Gabbro Flagstones Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1,
        "tfc:landslide": 2
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/gabbro_flagstones_slab",
        "rnr:landslide/gabbro_flagstones_stair",
        "rnr:mattock/slab/flagstones_gabbro_slab"
      ],
      "model_parents": [
        "item/rock/flagstones/gabbro_slab",
        "block/rock/flagstones/gabbro_slab",
        "block/path_slab",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/gabbro_slab"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/gabbro_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_slabs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "slab",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _slab"
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
      "id": "rnr:rock/flagstones/gabbro_stairs",
      "namespace": "rnr",
      "display_name": "Gabbro Flagstones Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:mattock/stair/flagstones_gabbro_stairs"
      ],
      "model_parents": [
        "item/rock/flagstones/gabbro_stairs",
        "block/rock/flagstones/gabbro_stairs",
        "block/path_stairs",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/gabbro_stairs"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/gabbro_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_stairs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _stairs"
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
      "id": "rnr:rock/flagstones/gneiss",
      "namespace": "rnr",
      "display_name": "Gneiss Flagstones",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/gneiss_flagstones"
      ],
      "model_parents": [
        "item/rock/flagstones/gneiss",
        "block/rock/flagstones/gneiss",
        "block/path_block",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/gneiss"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/gneiss",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_blocks",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:rock/flagstones/gneiss_slab",
      "namespace": "rnr",
      "display_name": "Gneiss Flagstones Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1,
        "tfc:landslide": 2
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/gneiss_flagstones_slab",
        "rnr:landslide/gneiss_flagstones_stair",
        "rnr:mattock/slab/flagstones_gneiss_slab"
      ],
      "model_parents": [
        "item/rock/flagstones/gneiss_slab",
        "block/rock/flagstones/gneiss_slab",
        "block/path_slab",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/gneiss_slab"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/gneiss_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_slabs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "slab",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _slab"
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
      "id": "rnr:rock/flagstones/gneiss_stairs",
      "namespace": "rnr",
      "display_name": "Gneiss Flagstones Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:mattock/stair/flagstones_gneiss_stairs"
      ],
      "model_parents": [
        "item/rock/flagstones/gneiss_stairs",
        "block/rock/flagstones/gneiss_stairs",
        "block/path_stairs",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/gneiss_stairs"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/gneiss_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_stairs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _stairs"
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
      "id": "rnr:rock/flagstones/granite",
      "namespace": "rnr",
      "display_name": "Granite Flagstones",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/granite_flagstones"
      ],
      "model_parents": [
        "item/rock/flagstones/granite",
        "block/rock/flagstones/granite",
        "block/path_block",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/granite"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/granite",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_blocks",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
      "id": "rnr:rock/flagstones/granite_slab",
      "namespace": "rnr",
      "display_name": "Granite Flagstones Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1,
        "tfc:landslide": 2
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/granite_flagstones_slab",
        "rnr:landslide/granite_flagstones_stair",
        "rnr:mattock/slab/flagstones_granite_slab"
      ],
      "model_parents": [
        "item/rock/flagstones/granite_slab",
        "block/rock/flagstones/granite_slab",
        "block/path_slab",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/granite_slab"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/granite_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_slabs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "slab",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _slab"
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
      "id": "rnr:rock/flagstones/granite_stairs",
      "namespace": "rnr",
      "display_name": "Granite Flagstones Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "rnr:mattock": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:mattock/stair/flagstones_granite_stairs"
      ],
      "model_parents": [
        "item/rock/flagstones/granite_stairs",
        "block/rock/flagstones/granite_stairs",
        "block/path_stairs",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/granite_stairs"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/granite_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_stairs",
          "tfc:can_landslide",
          "tfc:supports_landslide"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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
        "form": {
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _stairs"
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
      "id": "rnr:rock/flagstones/limestone",
      "namespace": "rnr",
      "display_name": "Limestone Flagstones",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "rnr:flagstone_roads"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "rnr:landslide/limestone_flagstones"
      ],
      "model_parents": [
        "item/rock/flagstones/limestone",
        "block/rock/flagstones/limestone",
        "block/path_block",
        "block/block"
      ],
      "creative_tabs": [
        "rnr:rnr.creative_tab.roads_and_roofs"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "rnr:blocks/rock/flagstones/limestone"
      ],
      "block_context": {
        "block_id": "rnr:rock/flagstones/limestone",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "rnr:flagstones_blocks",
          "tfc:can_landslide",
          "tfc:supports_landslide"
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
          "text": "Speed Multiplier: 1.2"
        },
        {
          "source": "runtime-tooltip",
          "text": "Roads and Roofs TFC"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "rnr",
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