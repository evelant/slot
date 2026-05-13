# Items to classify
{
  "items": [
    {
      "id": "copycats:copycat_folding_door",
      "namespace": "copycats",
      "display_name": "Copycat Folding Door",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:contraption_controlled",
        "minecraft:doors"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "minecraft:kjs/copycats_copycat_folding_door"
      ],
      "model_parents": [
        "item/copycat_folding_door",
        "block/copycat_base/folding_door",
        "block/thin_block"
      ],
      "creative_tabs": [
        "copycats:functional"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_folding_door"
      ],
      "block_context": {
        "block_id": "copycats:copycat_folding_door",
        "block_tags": [
          "buildinggadgets2:deny",
          "create:brittle",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:doors",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:wooden_doors",
          "quark:non_double_door",
          "tacz:interact_key/whitelist"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 4,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
      "id": "copycats:copycat_ghost_block",
      "namespace": "copycats",
      "display_name": "Copycat Ghost Block",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "minecraft:kjs/copycats_copycat_ghost_block"
      ],
      "model_parents": [
        "item/copycat_ghost_block",
        "block/copycat_base/ghost_block",
        "block/block"
      ],
      "creative_tabs": [
        "copycats:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_ghost_block"
      ],
      "block_context": {
        "block_id": "copycats:copycat_ghost_block",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
      "id": "copycats:copycat_half_layer",
      "namespace": "copycats",
      "display_name": "Copycat Half Layer",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "tfg:copycat_vertical_half_layer"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/copycats_copycat_half_layer",
        "tfg:copycat_half_layer"
      ],
      "model_parents": [
        "item/copycat_half_layer",
        "block/copycat_base/half_layer",
        "block/block"
      ],
      "creative_tabs": [
        "copycats:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_half_layer"
      ],
      "block_context": {
        "block_id": "copycats:copycat_half_layer",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
      "id": "copycats:copycat_half_panel",
      "namespace": "copycats",
      "display_name": "Copycat Half Panel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "minecraft:kjs/copycats_copycat_half_panel"
      ],
      "model_parents": [
        "item/copycat_half_panel",
        "block/copycat_base/half_panel",
        "block/block"
      ],
      "creative_tabs": [
        "copycats:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_half_panel"
      ],
      "block_context": {
        "block_id": "copycats:copycat_half_panel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
      "id": "copycats:copycat_heavy_weighted_pressure_plate",
      "namespace": "copycats",
      "display_name": "Copycat Heavy Weighted Pressure Plate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_heavy_weighted_pressure_plate",
        "block/copycat_base/pressure_plate",
        "block/thin_block"
      ],
      "creative_tabs": [
        "copycats:functional"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_heavy_weighted_pressure_plate"
      ],
      "block_context": {
        "block_id": "copycats:copycat_heavy_weighted_pressure_plate",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:pressure_plates",
          "minecraft:wall_post_override"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
          "value": "pressure_plate",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _pressure_plate"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        }
      }
    },
    {
      "id": "copycats:copycat_iron_door",
      "namespace": "copycats",
      "display_name": "Copycat Iron Door",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_iron_door",
        "block/copycat_base/door",
        "block/thin_block"
      ],
      "creative_tabs": [
        "copycats:functional"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_iron_door"
      ],
      "block_context": {
        "block_id": "copycats:copycat_iron_door",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
          "source": "rule:form_from_id",
          "rationale": "suffix _door"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        }
      }
    },
    {
      "id": "copycats:copycat_iron_trapdoor",
      "namespace": "copycats",
      "display_name": "Copycat Iron Trapdoor",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_iron_trapdoor",
        "block/copycat_base/trapdoor",
        "block/thin_block"
      ],
      "creative_tabs": [
        "copycats:functional"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_iron_trapdoor"
      ],
      "block_context": {
        "block_id": "copycats:copycat_iron_trapdoor",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:trapdoors"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
          "value": "trapdoor",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _trapdoor"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        }
      }
    },
    {
      "id": "copycats:copycat_ladder",
      "namespace": "copycats",
      "display_name": "Copycat Ladder",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "minecraft:kjs/copycats_copycat_ladder"
      ],
      "model_parents": [
        "item/copycat_ladder",
        "block/copycat_base/ladder"
      ],
      "creative_tabs": [
        "copycats:functional"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_ladder"
      ],
      "block_context": {
        "block_id": "copycats:copycat_ladder",
        "block_tags": [
          "create:copycat_deny",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:climbable",
          "minecraft:fall_damage_resetting",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
          "value": "ladder",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ladder"
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
      "id": "copycats:copycat_large_cogwheel",
      "namespace": "copycats",
      "display_name": "Copycat Large Cogwheel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_large_cogwheel",
        "block/copycat_base/large_cogwheel",
        "block/large_wheels"
      ],
      "creative_tabs": [
        "copycats:functional"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_large_cogwheel"
      ],
      "block_context": {
        "block_id": "copycats:copycat_large_cogwheel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
        }
      }
    },
    {
      "id": "copycats:copycat_layer",
      "namespace": "copycats",
      "display_name": "Copycat Layer",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "minecraft:kjs/copycats_copycat_layer"
      ],
      "model_parents": [
        "item/copycat_layer",
        "block/copycat_base/layer",
        "block/thin_block"
      ],
      "creative_tabs": [
        "copycats:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_layer"
      ],
      "block_context": {
        "block_id": "copycats:copycat_layer",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
      "id": "copycats:copycat_light_weighted_pressure_plate",
      "namespace": "copycats",
      "display_name": "Copycat Light Weighted Pressure Plate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_light_weighted_pressure_plate",
        "block/copycat_base/pressure_plate",
        "block/thin_block"
      ],
      "creative_tabs": [
        "copycats:functional"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_light_weighted_pressure_plate"
      ],
      "block_context": {
        "block_id": "copycats:copycat_light_weighted_pressure_plate",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:pressure_plates",
          "minecraft:wall_post_override"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
          "value": "pressure_plate",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _pressure_plate"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        }
      }
    },
    {
      "id": "copycats:copycat_pane",
      "namespace": "copycats",
      "display_name": "Copycat Pane",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "tfg:copycat_flat_pane"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/copycats_copycat_pane",
        "tfg:copycat_pane"
      ],
      "model_parents": [
        "item/copycat_pane",
        "block/copycat_base/pane",
        "block/block"
      ],
      "creative_tabs": [
        "copycats:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_pane"
      ],
      "block_context": {
        "block_id": "copycats:copycat_pane",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
          "value": "pane",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _pane"
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
      "id": "copycats:copycat_shaft",
      "namespace": "copycats",
      "display_name": "Copycat Shaft",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_shaft",
        "block/copycat_base/shaft",
        "block/block"
      ],
      "creative_tabs": [
        "copycats:functional"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_shaft"
      ],
      "block_context": {
        "block_id": "copycats:copycat_shaft",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
        }
      }
    },
    {
      "id": "copycats:copycat_slab",
      "namespace": "copycats",
      "display_name": "Copycat Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "copycats:copycat_slab"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "create_connected:crafting/palettes/copycat_slab_compat",
        "minecraft:kjs/copycats_copycat_slab"
      ],
      "model_parents": [
        "item/copycat_slab",
        "block/copycat_base/slab",
        "block/block"
      ],
      "creative_tabs": [
        "copycats:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_slab"
      ],
      "block_context": {
        "block_id": "copycats:copycat_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
      "id": "copycats:copycat_slice",
      "namespace": "copycats",
      "display_name": "Copycat Slice",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "tfg:copycat_vertical_slice"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/copycats_copycat_slice",
        "tfg:copycat_slice"
      ],
      "model_parents": [
        "item/copycat_slice",
        "block/copycat_base/slice",
        "block/block"
      ],
      "creative_tabs": [
        "copycats:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_slice"
      ],
      "block_context": {
        "block_id": "copycats:copycat_slice",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
      "id": "copycats:copycat_sliding_door",
      "namespace": "copycats",
      "display_name": "Copycat Sliding Door",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:contraption_controlled",
        "minecraft:doors"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "minecraft:kjs/copycats_copycat_sliding_door"
      ],
      "model_parents": [
        "item/copycat_sliding_door",
        "block/copycat_base/sliding_door",
        "block/thin_block"
      ],
      "creative_tabs": [
        "copycats:functional"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_sliding_door"
      ],
      "block_context": {
        "block_id": "copycats:copycat_sliding_door",
        "block_tags": [
          "buildinggadgets2:deny",
          "create:brittle",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:doors",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:wooden_doors",
          "quark:non_double_door",
          "tacz:interact_key/whitelist"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Very Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 4,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
      "id": "copycats:copycat_slope",
      "namespace": "copycats",
      "display_name": "Copycat Slope",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "tfg:copycat_vertical_slope"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/copycats_copycat_slope",
        "tfg:copycat_slope"
      ],
      "model_parents": [
        "item/copycat_slope",
        "block/copycat_base/slope",
        "block/block"
      ],
      "creative_tabs": [
        "copycats:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_slope"
      ],
      "block_context": {
        "block_id": "copycats:copycat_slope",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
      "id": "copycats:copycat_slope_layer",
      "namespace": "copycats",
      "display_name": "Copycat Slope Layer",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "minecraft:kjs/copycats_copycat_slope_layer"
      ],
      "model_parents": [
        "item/copycat_slope_layer",
        "block/copycat_base/slope_layer",
        "block/block"
      ],
      "creative_tabs": [
        "copycats:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_slope_layer"
      ],
      "block_context": {
        "block_id": "copycats:copycat_slope_layer",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
      "id": "copycats:copycat_stacked_half_layer",
      "namespace": "copycats",
      "display_name": "Copycat Stacked Half Layer",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "minecraft:kjs/copycats_copycat_stacked_half_layer"
      ],
      "model_parents": [
        "item/copycat_stacked_half_layer",
        "block/copycat_base/stacked_half_layer",
        "block/block"
      ],
      "creative_tabs": [
        "copycats:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_stacked_half_layer"
      ],
      "block_context": {
        "block_id": "copycats:copycat_stacked_half_layer",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
      "id": "copycats:copycat_stairs",
      "namespace": "copycats",
      "display_name": "Copycat Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "copycats:copycat_stairs"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 2,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "tfg:copycat_vertical_stairs"
      ],
      "recipe_output_examples": [
        "create_connected:crafting/palettes/copycat_stairs_compat",
        "minecraft:kjs/copycats_copycat_stairs",
        "tfg:copycat_stairs"
      ],
      "model_parents": [
        "item/copycat_stairs",
        "block/copycat_base/stairs",
        "block/block"
      ],
      "creative_tabs": [
        "copycats:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_stairs"
      ],
      "block_context": {
        "block_id": "copycats:copycat_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
      "id": "copycats:copycat_stone_button",
      "namespace": "copycats",
      "display_name": "Copycat Stone Button",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [
        "item/copycat_stone_button",
        "block/copycat_base/button",
        "block/block"
      ],
      "creative_tabs": [
        "copycats:functional"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_stone_button"
      ],
      "block_context": {
        "block_id": "copycats:copycat_stone_button",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:buttons",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:stone_buttons",
          "tacz:interact_key/whitelist"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
          "value": "button",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _button"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        }
      }
    },
    {
      "id": "copycats:copycat_stone_pressure_plate",
      "namespace": "copycats",
      "display_name": "Copycat Stone Pressure Plate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
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
        "tfg:copycats/shaped/copycat_stone_pressure_plate"
      ],
      "model_parents": [
        "item/copycat_stone_pressure_plate",
        "block/copycat_base/pressure_plate",
        "block/thin_block"
      ],
      "creative_tabs": [
        "copycats:functional"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_stone_pressure_plate"
      ],
      "block_context": {
        "block_id": "copycats:copycat_stone_pressure_plate",
        "block_tags": [
          "create:wrench_pickup",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:pressure_plates",
          "minecraft:stone_pressure_plates",
          "minecraft:wall_post_override"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
          "value": "pressure_plate",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _pressure_plate"
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
      "id": "copycats:copycat_trapdoor",
      "namespace": "copycats",
      "display_name": "Copycat Trapdoor",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "minecraft:kjs/copycats_copycat_trapdoor"
      ],
      "model_parents": [
        "item/copycat_trapdoor",
        "block/copycat_base/trapdoor",
        "block/thin_block"
      ],
      "creative_tabs": [
        "copycats:functional"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_trapdoor"
      ],
      "block_context": {
        "block_id": "copycats:copycat_trapdoor",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe",
          "minecraft:trapdoors",
          "minecraft:wooden_trapdoors",
          "tacz:interact_key/whitelist"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
          "value": "trapdoor",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _trapdoor"
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
      "id": "copycats:copycat_vertical_half_layer",
      "namespace": "copycats",
      "display_name": "Copycat Vertical Half Layer",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "tfg:copycat_half_layer"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/copycats_copycat_vertical_half_layer",
        "tfg:copycat_vertical_half_layer"
      ],
      "model_parents": [
        "item/copycat_vertical_half_layer",
        "block/copycat_base/vertical_half_layer",
        "block/block"
      ],
      "creative_tabs": [
        "copycats:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_vertical_half_layer"
      ],
      "block_context": {
        "block_id": "copycats:copycat_vertical_half_layer",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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
      "id": "copycats:copycat_vertical_slice",
      "namespace": "copycats",
      "display_name": "Copycat Vertical Slice",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "tfg:copycat_slice"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/copycats_copycat_vertical_slice",
        "tfg:copycat_vertical_slice"
      ],
      "model_parents": [
        "item/copycat_vertical_slice",
        "block/copycat_base/vertical_slice",
        "block/block"
      ],
      "creative_tabs": [
        "copycats:main"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "copycats:blocks/copycat_vertical_slice"
      ],
      "block_context": {
        "block_id": "copycats:copycat_vertical_slice",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
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
          "text": "Create: Copycats+"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "copycats",
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