# Items to classify
{
  "items": [
    {
      "id": "minecraft:cobbled_deepslate_stairs",
      "namespace": "minecraft",
      "display_name": "Cobbled Deepslate Stairs",
      "minecraft_tags_direct": [
        "minecraft:stairs"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 2
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:cobbled_deepslate_stairs",
        "minecraft:cobbled_deepslate_stairs_from_cobbled_deepslate_stonecutting",
        "minecraft:cobbled_deepslate_stairs_from_deepslate_stonecutting"
      ],
      "model_parents": [
        "block/cobbled_deepslate_stairs",
        "block/stairs",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/cobbled_deepslate_stairs"
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
        "form": {
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:stairs"
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
      "id": "minecraft:cobbled_deepslate_wall",
      "namespace": "minecraft",
      "display_name": "Cobbled Deepslate Wall",
      "minecraft_tags_direct": [
        "minecraft:walls"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 2
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:cobbled_deepslate_wall",
        "minecraft:cobbled_deepslate_wall_from_cobbled_deepslate_stonecutting",
        "minecraft:cobbled_deepslate_wall_from_deepslate_stonecutting"
      ],
      "model_parents": [
        "block/cobbled_deepslate_wall_inventory",
        "block/wall_inventory",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/cobbled_deepslate_wall"
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
        "form": {
          "value": "wall",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:walls"
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
      "id": "minecraft:cobblestone",
      "namespace": "minecraft",
      "display_name": "Cobblestone",
      "minecraft_tags_direct": [
        "minecraft:stone_crafting_materials",
        "minecraft:stone_tool_materials",
        "minecraft:sulfur_cube_archetype/regular"
      ],
      "minecraft_tags_inherited": [
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [
        "crafting",
        "smelting",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 3,
        "crafting_shaped": 20,
        "stonecutting": 3,
        "smelting": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "sample_ingredient_of": [
        "minecraft:andesite",
        "minecraft:brewing_stand",
        "minecraft:coast_armor_trim_smithing_template",
        "minecraft:cobblestone_slab",
        "minecraft:cobblestone_slab_from_cobblestone_stonecutting",
        "minecraft:cobblestone_stairs",
        "minecraft:cobblestone_stairs_from_cobblestone_stonecutting",
        "minecraft:cobblestone_wall",
        "minecraft:cobblestone_wall_from_cobblestone_stonecutting",
        "minecraft:diorite"
      ],
      "sample_output_of": [
        "minecraft:cobblestone_from_stone_stonecutting"
      ],
      "model_parents": [
        "block/cobblestone",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/cobblestone",
        "minecraft:blocks/infested_cobblestone",
        "minecraft:blocks/stone"
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
          "value": "stone",
          "confidence": 1,
          "source": "rule:material_family_from_tag",
          "rationale": "tag minecraft:stone_tool_materials"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "smelting",
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "minecraft:cobblestone_slab",
      "namespace": "minecraft",
      "display_name": "Cobblestone Slab",
      "minecraft_tags_direct": [
        "minecraft:slabs"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 2
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:cobblestone_slab",
        "minecraft:cobblestone_slab_from_cobblestone_stonecutting",
        "minecraft:cobblestone_slab_from_stone_stonecutting"
      ],
      "model_parents": [
        "block/cobblestone_slab",
        "block/slab",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/cobblestone_slab"
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
          "value": "cobblestone",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix cobblestone_"
        },
        "form": {
          "value": "slab",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:slabs"
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
      "id": "minecraft:cobblestone_stairs",
      "namespace": "minecraft",
      "display_name": "Cobblestone Stairs",
      "minecraft_tags_direct": [
        "minecraft:stairs"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 2
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:cobblestone_stairs",
        "minecraft:cobblestone_stairs_from_cobblestone_stonecutting",
        "minecraft:cobblestone_stairs_from_stone_stonecutting"
      ],
      "model_parents": [
        "block/cobblestone_stairs",
        "block/stairs",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/cobblestone_stairs"
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
          "value": "cobblestone",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix cobblestone_"
        },
        "form": {
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:stairs"
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