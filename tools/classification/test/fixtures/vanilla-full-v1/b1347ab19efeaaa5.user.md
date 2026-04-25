# Items to classify
{
  "items": [
    {
      "id": "minecraft:waxed_copper_chest",
      "namespace": "minecraft",
      "display_name": "Waxed Copper Chest",
      "minecraft_tags_direct": [
        "minecraft:copper_chests"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:waxed_copper_chest_from_honeycomb"
      ],
      "model_parents": [
        "item/copper_chest",
        "item/template_chest"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/waxed_copper_chest"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
        "material_family": {
          "value": "copper",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix waxed_"
        },
        "form": {
          "value": "storage_block",
          "confidence": 1,
          "source": "rule:form_from_model",
          "rationale": "model item/template_chest"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "minecraft:waxed_copper_door",
      "namespace": "minecraft",
      "display_name": "Waxed Copper Door",
      "minecraft_tags_direct": [
        "minecraft:doors"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:waxed_copper_door_from_honeycomb"
      ],
      "model_parents": [
        "item/copper_door",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/waxed_copper_door"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
        "material_family": {
          "value": "copper",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix waxed_"
        },
        "form": {
          "value": "door",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:doors"
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
      "id": "minecraft:waxed_copper_golem_statue",
      "namespace": "minecraft",
      "display_name": "Waxed Copper Golem Statue",
      "minecraft_tags_direct": [
        "minecraft:copper_golem_statues"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:waxed_copper_golem_statue_from_honeycomb"
      ],
      "model_parents": [
        "item/template_copper_golem_statue"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/waxed_copper_golem_statue"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
        "has_nbt_variation": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_nbt_variation_from_component"
        },
        "material_family": {
          "value": "copper",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix waxed_"
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
      "id": "minecraft:waxed_copper_grate",
      "namespace": "minecraft",
      "display_name": "Waxed Copper Grate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "stonecutting": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:waxed_copper_grate",
        "minecraft:waxed_copper_grate_from_honeycomb",
        "minecraft:waxed_copper_grate_from_waxed_copper_block_stonecutting"
      ],
      "model_parents": [
        "block/copper_grate",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/waxed_copper_grate"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
        "material_family": {
          "value": "copper",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix waxed_"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "minecraft:waxed_copper_lantern",
      "namespace": "minecraft",
      "display_name": "Waxed Copper Lantern",
      "minecraft_tags_direct": [
        "minecraft:lanterns"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:waxed_copper_lantern_from_honeycomb"
      ],
      "model_parents": [
        "item/copper_lantern",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/waxed_copper_lantern"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
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
        "material_family": {
          "value": "copper",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix waxed_"
        },
        "form": {
          "value": "lantern",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _lantern"
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
Respond with a single JSON object matching the expected output shape above. No other text.