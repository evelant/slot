# Items to classify
{
  "items": [
    {
      "id": "minecraft:mangrove_sign",
      "namespace": "minecraft",
      "display_name": "Mangrove Sign",
      "minecraft_tags_direct": [
        "minecraft:signs"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:mangrove_sign"
      ],
      "model_parents": [
        "item/mangrove_sign",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/mangrove_sign"
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
          "value": "wood_mangrove",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix mangrove_"
        },
        "form": {
          "value": "sign",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:signs"
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
        },
        "is_fuel": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_fuel_from_id_or_tag"
        }
      }
    },
    {
      "id": "minecraft:mangrove_slab",
      "namespace": "minecraft",
      "display_name": "Mangrove Slab",
      "minecraft_tags_direct": [
        "minecraft:wooden_slabs"
      ],
      "minecraft_tags_inherited": [
        "minecraft:slabs"
      ],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 5
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:barrel",
        "minecraft:chiseled_bookshelf",
        "minecraft:composter",
        "minecraft:daylight_detector",
        "minecraft:lectern"
      ],
      "sample_output_of": [
        "minecraft:mangrove_slab"
      ],
      "model_parents": [
        "block/mangrove_slab",
        "block/slab",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/mangrove_slab"
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
          "value": "wood_mangrove",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix mangrove_"
        },
        "form": {
          "value": "slab",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:slabs"
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
        },
        "is_fuel": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_fuel_from_id_or_tag"
        }
      }
    },
    {
      "id": "minecraft:mangrove_stairs",
      "namespace": "minecraft",
      "display_name": "Mangrove Stairs",
      "minecraft_tags_direct": [
        "minecraft:wooden_stairs"
      ],
      "minecraft_tags_inherited": [
        "minecraft:stairs"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:mangrove_stairs"
      ],
      "model_parents": [
        "block/mangrove_stairs",
        "block/stairs",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/mangrove_stairs"
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
          "value": "wood_mangrove",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix mangrove_"
        },
        "form": {
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:stairs"
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
        },
        "is_fuel": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_fuel_from_id_or_tag"
        }
      }
    },
    {
      "id": "minecraft:mangrove_trapdoor",
      "namespace": "minecraft",
      "display_name": "Mangrove Trapdoor",
      "minecraft_tags_direct": [
        "minecraft:wooden_trapdoors"
      ],
      "minecraft_tags_inherited": [
        "minecraft:trapdoors"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:mangrove_trapdoor"
      ],
      "model_parents": [
        "block/mangrove_trapdoor_bottom",
        "block/template_orientable_trapdoor_bottom",
        "block/thin_block",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/mangrove_trapdoor"
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
          "value": "wood_mangrove",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix mangrove_"
        },
        "form": {
          "value": "trapdoor",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:trapdoors"
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
        },
        "is_fuel": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_fuel_from_id_or_tag"
        }
      }
    },
    {
      "id": "minecraft:mangrove_wood",
      "namespace": "minecraft",
      "display_name": "Mangrove Wood",
      "minecraft_tags_direct": [
        "minecraft:mangrove_logs"
      ],
      "minecraft_tags_inherited": [
        "minecraft:completes_find_tree_tutorial",
        "minecraft:logs",
        "minecraft:logs_that_burn",
        "minecraft:sulfur_cube_archetype/bouncy",
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [
        "crafting",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 3,
        "smelting": 1,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:campfire",
        "minecraft:charcoal",
        "minecraft:mangrove_planks",
        "minecraft:smoker",
        "minecraft:soul_campfire"
      ],
      "sample_output_of": [
        "minecraft:mangrove_wood"
      ],
      "model_parents": [
        "block/mangrove_wood",
        "block/cube_column",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/mangrove_wood"
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
          "value": "wood_mangrove",
          "confidence": 1,
          "source": "rule:material_family_from_tag",
          "rationale": "log tag minecraft:mangrove_logs"
        },
        "form": {
          "value": "wood",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "id suffix _wood"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "overworld_surface"
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
    }
  ]
}
Respond with a single JSON object matching the expected output shape above. No other text.