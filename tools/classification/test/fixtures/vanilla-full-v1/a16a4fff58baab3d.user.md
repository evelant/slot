# Items to classify
{
  "items": [
    {
      "id": "minecraft:dragon_head",
      "namespace": "minecraft",
      "display_name": "Dragon Head",
      "minecraft_tags_direct": [
        "minecraft:noteblock_top_instruments",
        "minecraft:skulls"
      ],
      "minecraft_tags_inherited": [
        "minecraft:enchantable/equippable",
        "minecraft:enchantable/vanishing"
      ],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_special_firework_star": 1
      },
      "recipe_production_by_type": {},
      "sample_ingredient_of": [
        "minecraft:firework_star"
      ],
      "sample_output_of": [],
      "model_parents": [
        "item/dragon_head",
        "item/template_skull"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/dragon_head"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:rarity": "epic"
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
        "equip_slot": {
          "value": "head",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        },
        "rarity": {
          "value": "rare",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_id_override",
          "rationale": "id-specific override (sonnet-v4 canary catch)"
        },
        "form": {
          "value": "head",
          "confidence": 1,
          "source": "rule:form_from_model",
          "rationale": "model item/template_skull"
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
            "end"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        },
        "y_level_range": {
          "value": "end_islands",
          "confidence": 1,
          "source": "rule:y_level_range_from_id",
          "rationale": "id pattern"
        }
      }
    },
    {
      "id": "minecraft:dried_ghast",
      "namespace": "minecraft",
      "display_name": "Dried Ghast",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:dried_ghast"
      ],
      "model_parents": [
        "block/dried_ghast_hydration_0",
        "block/dried_ghast",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/dried_ghast",
        "minecraft:gameplay/piglin_bartering"
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
        "origin": {
          "values": [
            "trading"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:dried_kelp",
      "namespace": "minecraft",
      "display_name": "Dried Kelp",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "campfire_cooking": 1,
        "smelting": 1,
        "smoking": 1
      },
      "sample_ingredient_of": [
        "minecraft:dried_kelp_block"
      ],
      "sample_output_of": [
        "minecraft:dried_kelp",
        "minecraft:dried_kelp_from_campfire_cooking",
        "minecraft:dried_kelp_from_smelting",
        "minecraft:dried_kelp_from_smoking"
      ],
      "model_parents": [
        "item/dried_kelp",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {
        "minecraft:food": {
          "nutrition": 1,
          "saturation": 0.6
        },
        "minecraft:consumable": {
          "consume_seconds": 0.8
        },
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
        "processing_in": {
          "values": [
            "crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "is_fuel": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_fuel_from_id_or_tag"
        }
      }
    },
    {
      "id": "minecraft:dried_kelp_block",
      "namespace": "minecraft",
      "display_name": "Dried Kelp Block",
      "minecraft_tags_direct": [
        "minecraft:sulfur_cube_archetype/fast_flat"
      ],
      "minecraft_tags_inherited": [
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:dried_kelp"
      ],
      "sample_output_of": [
        "minecraft:dried_kelp_block"
      ],
      "model_parents": [
        "block/dried_kelp_block",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/dried_kelp_block"
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
        "required_tool": {
          "value": "hoe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/hoe"
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
        },
        "is_fuel": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_fuel_from_id_or_tag"
        }
      }
    },
    {
      "id": "minecraft:dripstone_block",
      "namespace": "minecraft",
      "display_name": "Dripstone Block",
      "minecraft_tags_direct": [
        "minecraft:sulfur_cube_archetype/regular"
      ],
      "minecraft_tags_inherited": [
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:dripstone_block"
      ],
      "model_parents": [
        "block/dripstone_block",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/dripstone_block"
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
          "value": "dripstone",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "exact id minecraft:dripstone_block"
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