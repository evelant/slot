# Items to classify
{
  "items": [
    {
      "id": "tfg:wood/crafting_station/warped_slab",
      "namespace": "tfg",
      "display_name": "Warped Crafting Station Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:crafting_stations"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:shapeless/warped_crafting_station_slab"
      ],
      "model_parents": [
        "item/wood/crafting_station/warped_slab",
        "block/wood/crafting_station/warped_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/crafting_station/warped_slab"
      ],
      "block_context": {
        "block_id": "tfg:wood/crafting_station/warped_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe",
          "tfg:crafting_stations"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:wood/crafting_station/white_cedar",
      "namespace": "tfg",
      "display_name": "White Cedar Crafting Station",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:crafting_stations"
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
        "tfg:shapeless/white_cedar_crafting_station_slab"
      ],
      "recipe_output_examples": [
        "tfg:shaped/white_cedar_crafting_station"
      ],
      "model_parents": [
        "item/wood/crafting_station/white_cedar",
        "block/wood/crafting_station/white_cedar",
        "block/crafting_station"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/crafting_station/white_cedar"
      ],
      "block_context": {
        "block_id": "tfg:wood/crafting_station/white_cedar",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe",
          "tfg:crafting_stations"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:wood/crafting_station/white_cedar_slab",
      "namespace": "tfg",
      "display_name": "White Cedar Crafting Station Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:crafting_stations"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:shapeless/white_cedar_crafting_station_slab"
      ],
      "model_parents": [
        "item/wood/crafting_station/white_cedar_slab",
        "block/wood/crafting_station/white_cedar_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/crafting_station/white_cedar_slab"
      ],
      "block_context": {
        "block_id": "tfg:wood/crafting_station/white_cedar_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe",
          "tfg:crafting_stations"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:wood/crafting_station/willow",
      "namespace": "tfg",
      "display_name": "Willow Crafting Station",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:crafting_stations"
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
        "tfg:shapeless/willow_crafting_station_slab"
      ],
      "recipe_output_examples": [
        "tfg:shaped/willow_crafting_station"
      ],
      "model_parents": [
        "item/wood/crafting_station/willow",
        "block/wood/crafting_station/willow",
        "block/crafting_station"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/crafting_station/willow"
      ],
      "block_context": {
        "block_id": "tfg:wood/crafting_station/willow",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe",
          "tfg:crafting_stations"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:wood/crafting_station/willow_slab",
      "namespace": "tfg",
      "display_name": "Willow Crafting Station Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:crafting_stations"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:shapeless/willow_crafting_station_slab"
      ],
      "model_parents": [
        "item/wood/crafting_station/willow_slab",
        "block/wood/crafting_station/willow_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/crafting_station/willow_slab"
      ],
      "block_context": {
        "block_id": "tfg:wood/crafting_station/willow_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "minecraft:mineable/axe",
          "tfg:crafting_stations"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:wood/door/araucaria",
      "namespace": "tfg",
      "display_name": "Araucaria Door",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:doors"
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
        "tfg:shaped/araucaria_door_from_lumber"
      ],
      "model_parents": [
        "item/wood/door/araucaria",
        "item/generated"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/door/araucaria"
      ],
      "block_context": {
        "block_id": "tfg:wood/door/araucaria",
        "block_tags": [
          "buildinggadgets2:deny",
          "create:brittle",
          "cucumber:mineable/paxel",
          "minecraft:doors",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 4,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
          "value": "door",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:doors"
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
      "id": "tfg:wood/door/beech",
      "namespace": "tfg",
      "display_name": "Beech Door",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:doors"
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
        "tfg:shaped/beech_door_from_lumber"
      ],
      "model_parents": [
        "item/wood/door/beech",
        "item/generated"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/door/beech"
      ],
      "block_context": {
        "block_id": "tfg:wood/door/beech",
        "block_tags": [
          "buildinggadgets2:deny",
          "create:brittle",
          "cucumber:mineable/paxel",
          "minecraft:doors",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 4,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
          "value": "door",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:doors"
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
      "id": "tfg:wood/door/mahoe",
      "namespace": "tfg",
      "display_name": "Mahoe Door",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:doors"
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
        "tfg:shaped/mahoe_door_from_lumber"
      ],
      "model_parents": [
        "item/wood/door/mahoe",
        "item/generated"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/door/mahoe"
      ],
      "block_context": {
        "block_id": "tfg:wood/door/mahoe",
        "block_tags": [
          "buildinggadgets2:deny",
          "create:brittle",
          "cucumber:mineable/paxel",
          "minecraft:doors",
          "minecraft:mineable/axe"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 4,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
          "value": "door",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:doors"
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
      "id": "tfg:wood/fence/araucaria",
      "namespace": "tfg",
      "display_name": "Araucaria Fence",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:fences"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "tfg:railways/shaped/semaphore"
      ],
      "recipe_output_examples": [
        "tfg:shaped/araucaria_fence_from_lumber_and_plank"
      ],
      "model_parents": [
        "item/wood/fence/araucaria",
        "block/wood/fence/araucaria_inventory",
        "block/fence_inventory"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/fence/araucaria"
      ],
      "block_context": {
        "block_id": "tfg:wood/fence/araucaria",
        "block_tags": [
          "ad_astra:passes_flood_fill",
          "create:fan_transparent",
          "cucumber:mineable/paxel",
          "minecraft:fences",
          "minecraft:mineable/axe",
          "railways:semaphore_poles",
          "tacz:bullet_ignore"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
          "value": "fence",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:fences"
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
      "id": "tfg:wood/fence/beech",
      "namespace": "tfg",
      "display_name": "Beech Fence",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:fences"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "tfg:railways/shaped/semaphore"
      ],
      "recipe_output_examples": [
        "tfg:shaped/beech_fence_from_lumber_and_plank"
      ],
      "model_parents": [
        "item/wood/fence/beech",
        "block/wood/fence/beech_inventory",
        "block/fence_inventory"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/fence/beech"
      ],
      "block_context": {
        "block_id": "tfg:wood/fence/beech",
        "block_tags": [
          "ad_astra:passes_flood_fill",
          "create:fan_transparent",
          "cucumber:mineable/paxel",
          "minecraft:fences",
          "minecraft:mineable/axe",
          "railways:semaphore_poles",
          "tacz:bullet_ignore"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
          "value": "fence",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:fences"
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
      "id": "tfg:wood/fence/mahoe",
      "namespace": "tfg",
      "display_name": "Mahoe Fence",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:fences"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "tfg:railways/shaped/semaphore"
      ],
      "recipe_output_examples": [
        "tfg:shaped/mahoe_fence_from_lumber_and_plank"
      ],
      "model_parents": [
        "item/wood/fence/mahoe",
        "block/wood/fence/mahoe_inventory",
        "block/fence_inventory"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/fence/mahoe"
      ],
      "block_context": {
        "block_id": "tfg:wood/fence/mahoe",
        "block_tags": [
          "ad_astra:passes_flood_fill",
          "create:fan_transparent",
          "cucumber:mineable/paxel",
          "minecraft:fences",
          "minecraft:mineable/axe",
          "railways:semaphore_poles",
          "tacz:bullet_ignore"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
          "value": "fence",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:fences"
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
      "id": "tfg:wood/fence_gate/araucaria",
      "namespace": "tfg",
      "display_name": "Araucaria Fence Gate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:fence_gates"
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
        "tfg:shaped/araucaria_fence_gate_from_lumber_and_plank"
      ],
      "model_parents": [
        "item/wood/fence_gate/araucaria",
        "block/wood/fence_gate/araucaria",
        "block/template_fence_gate"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/fence_gate/araucaria"
      ],
      "block_context": {
        "block_id": "tfg:wood/fence_gate/araucaria",
        "block_tags": [
          "create:movable_empty_collider",
          "cucumber:mineable/paxel",
          "minecraft:fence_gates",
          "minecraft:mineable/axe",
          "minecraft:unstable_bottom_center",
          "tacz:bullet_ignore",
          "tacz:interact_key/whitelist"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
          "value": "fence_gate",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:fence_gates"
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
      "id": "tfg:wood/fence_gate/beech",
      "namespace": "tfg",
      "display_name": "Beech Fence Gate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:fence_gates"
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
        "tfg:shaped/beech_fence_gate_from_lumber_and_plank"
      ],
      "model_parents": [
        "item/wood/fence_gate/beech",
        "block/wood/fence_gate/beech",
        "block/template_fence_gate"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/fence_gate/beech"
      ],
      "block_context": {
        "block_id": "tfg:wood/fence_gate/beech",
        "block_tags": [
          "create:movable_empty_collider",
          "cucumber:mineable/paxel",
          "minecraft:fence_gates",
          "minecraft:mineable/axe",
          "minecraft:unstable_bottom_center",
          "tacz:bullet_ignore",
          "tacz:interact_key/whitelist"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
          "value": "fence_gate",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:fence_gates"
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
      "id": "tfg:wood/fence_gate/mahoe",
      "namespace": "tfg",
      "display_name": "Mahoe Fence Gate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:fence_gates"
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
        "tfg:shaped/mahoe_fence_gate_from_lumber_and_plank"
      ],
      "model_parents": [
        "item/wood/fence_gate/mahoe",
        "block/wood/fence_gate/mahoe",
        "block/template_fence_gate"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/fence_gate/mahoe"
      ],
      "block_context": {
        "block_id": "tfg:wood/fence_gate/mahoe",
        "block_tags": [
          "create:movable_empty_collider",
          "cucumber:mineable/paxel",
          "minecraft:fence_gates",
          "minecraft:mineable/axe",
          "minecraft:unstable_bottom_center",
          "tacz:bullet_ignore",
          "tacz:interact_key/whitelist"
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
          "value": "fence_gate",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:fence_gates"
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
      "id": "tfg:wood/food_shelf/aeronos",
      "namespace": "tfg",
      "display_name": "Aeronos Food Shelf",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "firmalife:food_shelves"
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
        "tfg:shaped/aeronos_food_shelf"
      ],
      "model_parents": [
        "item/wood/food_shelf/aeronos",
        "block/wood/food_shelf/aeronos",
        "block/food_shelf_base"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/food_shelf/aeronos"
      ],
      "block_context": {
        "block_id": "tfg:wood/food_shelf/aeronos",
        "block_tags": [
          "cucumber:mineable/paxel",
          "firmalife:food_shelves",
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:wood/food_shelf/araucaria",
      "namespace": "tfg",
      "display_name": "Araucaria Food Shelf",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "firmalife:food_shelves"
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
        "tfg:shaped/araucaria_food_shelf"
      ],
      "model_parents": [
        "item/wood/food_shelf/araucaria",
        "block/wood/food_shelf/araucaria",
        "block/food_shelf_base"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/food_shelf/araucaria"
      ],
      "block_context": {
        "block_id": "tfg:wood/food_shelf/araucaria",
        "block_tags": [
          "cucumber:mineable/paxel",
          "firmalife:food_shelves",
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:wood/food_shelf/beech",
      "namespace": "tfg",
      "display_name": "Beech Food Shelf",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "firmalife:food_shelves"
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
        "tfg:shaped/beech_food_shelf"
      ],
      "model_parents": [
        "item/wood/food_shelf/beech",
        "block/wood/food_shelf/beech",
        "block/food_shelf_base"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/food_shelf/beech"
      ],
      "block_context": {
        "block_id": "tfg:wood/food_shelf/beech",
        "block_tags": [
          "cucumber:mineable/paxel",
          "firmalife:food_shelves",
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:wood/food_shelf/ginkgo",
      "namespace": "tfg",
      "display_name": "Ginkgo Food Shelf",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "firmalife:food_shelves"
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
        "tfg:shaped/ginkgo_food_shelf"
      ],
      "model_parents": [
        "item/wood/food_shelf/ginkgo",
        "block/wood/food_shelf/ginkgo",
        "block/food_shelf_base"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/food_shelf/ginkgo"
      ],
      "block_context": {
        "block_id": "tfg:wood/food_shelf/ginkgo",
        "block_tags": [
          "cucumber:mineable/paxel",
          "firmalife:food_shelves",
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:wood/food_shelf/glacian",
      "namespace": "tfg",
      "display_name": "Glacian Food Shelf",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "firmalife:food_shelves"
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
        "tfg:shaped/glacian_food_shelf"
      ],
      "model_parents": [
        "item/wood/food_shelf/glacian",
        "block/wood/food_shelf/glacian",
        "block/food_shelf_base"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/food_shelf/glacian"
      ],
      "block_context": {
        "block_id": "tfg:wood/food_shelf/glacian",
        "block_tags": [
          "cucumber:mineable/paxel",
          "firmalife:food_shelves",
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:wood/food_shelf/mahoe",
      "namespace": "tfg",
      "display_name": "Mahoe Food Shelf",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "firmalife:food_shelves"
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
        "tfg:shaped/mahoe_food_shelf"
      ],
      "model_parents": [
        "item/wood/food_shelf/mahoe",
        "block/wood/food_shelf/mahoe",
        "block/food_shelf_base"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/food_shelf/mahoe"
      ],
      "block_context": {
        "block_id": "tfg:wood/food_shelf/mahoe",
        "block_tags": [
          "cucumber:mineable/paxel",
          "firmalife:food_shelves",
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:wood/food_shelf/strophar",
      "namespace": "tfg",
      "display_name": "Strophar Food Shelf",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "firmalife:food_shelves"
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
        "tfg:shaped/strophar_food_shelf"
      ],
      "model_parents": [
        "item/wood/food_shelf/strophar",
        "block/wood/food_shelf/strophar",
        "block/food_shelf_base"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/food_shelf/strophar"
      ],
      "block_context": {
        "block_id": "tfg:wood/food_shelf/strophar",
        "block_tags": [
          "cucumber:mineable/paxel",
          "firmalife:food_shelves",
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:wood/hanger/aeronos",
      "namespace": "tfg",
      "display_name": "Aeronos Hanger",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "firmalife:hangers"
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
        "tfg:shaped/aeronos_hanger"
      ],
      "model_parents": [
        "item/wood/hanger/aeronos",
        "block/wood/hanger/aeronos",
        "block/hanger_base"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/hanger/aeronos"
      ],
      "block_context": {
        "block_id": "tfg:wood/hanger/aeronos",
        "block_tags": [
          "cucumber:mineable/paxel",
          "firmalife:hangers",
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:wood/hanger/araucaria",
      "namespace": "tfg",
      "display_name": "Araucaria Hanger",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "firmalife:hangers"
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
        "tfg:shaped/araucaria_hanger"
      ],
      "model_parents": [
        "item/wood/hanger/araucaria",
        "block/wood/hanger/araucaria",
        "block/hanger_base"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/hanger/araucaria"
      ],
      "block_context": {
        "block_id": "tfg:wood/hanger/araucaria",
        "block_tags": [
          "cucumber:mineable/paxel",
          "firmalife:hangers",
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:wood/hanger/beech",
      "namespace": "tfg",
      "display_name": "Beech Hanger",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "firmalife:hangers"
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
        "tfg:shaped/beech_hanger"
      ],
      "model_parents": [
        "item/wood/hanger/beech",
        "block/wood/hanger/beech",
        "block/hanger_base"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/hanger/beech"
      ],
      "block_context": {
        "block_id": "tfg:wood/hanger/beech",
        "block_tags": [
          "cucumber:mineable/paxel",
          "firmalife:hangers",
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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
      "id": "tfg:wood/hanger/ginkgo",
      "namespace": "tfg",
      "display_name": "Ginkgo Hanger",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "firmalife:hangers"
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
        "tfg:shaped/ginkgo_hanger"
      ],
      "model_parents": [
        "item/wood/hanger/ginkgo",
        "block/wood/hanger/ginkgo",
        "block/hanger_base"
      ],
      "creative_tabs": [
        "tfg:tfg"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfg:blocks/wood/hanger/ginkgo"
      ],
      "block_context": {
        "block_id": "tfg:wood/hanger/ginkgo",
        "block_tags": [
          "cucumber:mineable/paxel",
          "firmalife:hangers",
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
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfg",
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