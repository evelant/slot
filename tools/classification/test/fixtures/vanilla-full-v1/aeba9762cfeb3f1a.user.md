# Items to classify
{
  "items": [
    {
      "id": "minecraft:brown_shulker_box",
      "namespace": "minecraft",
      "display_name": "Brown Shulker Box",
      "minecraft_tags_direct": [
        "minecraft:shulker_boxes"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_transmute": 16
      },
      "recipe_production_by_type": {
        "crafting_transmute": 1
      },
      "sample_ingredient_of": [
        "minecraft:black_shulker_box",
        "minecraft:blue_shulker_box",
        "minecraft:brown_shulker_box",
        "minecraft:cyan_shulker_box",
        "minecraft:gray_shulker_box",
        "minecraft:green_shulker_box",
        "minecraft:light_blue_shulker_box",
        "minecraft:light_gray_shulker_box",
        "minecraft:lime_shulker_box",
        "minecraft:magenta_shulker_box"
      ],
      "sample_output_of": [
        "minecraft:brown_shulker_box"
      ],
      "model_parents": [
        "item/brown_shulker_box",
        "item/template_shulker_box"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/brown_shulker_box"
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
        "form": {
          "value": "storage_block",
          "confidence": 1,
          "source": "rule:form_from_model",
          "rationale": "model item/template_shulker_box"
        },
        "dye_color": {
          "value": "brown",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
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
      "id": "minecraft:brown_stained_glass",
      "namespace": "minecraft",
      "display_name": "Brown Stained Glass",
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
        "minecraft:brown_stained_glass_pane"
      ],
      "sample_output_of": [
        "minecraft:brown_stained_glass"
      ],
      "model_parents": [
        "block/brown_stained_glass",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/brown_stained_glass"
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
        "dye_color": {
          "value": "brown",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
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
      "id": "minecraft:brown_stained_glass_pane",
      "namespace": "minecraft",
      "display_name": "Brown Stained Glass Pane",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:brown_stained_glass_pane",
        "minecraft:brown_stained_glass_pane_from_glass_pane"
      ],
      "model_parents": [
        "item/brown_stained_glass_pane",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/brown_stained_glass_pane"
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
          "value": "pane",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _pane"
        },
        "dye_color": {
          "value": "brown",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
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
      "id": "minecraft:brown_terracotta",
      "namespace": "minecraft",
      "display_name": "Brown Terracotta",
      "minecraft_tags_direct": [
        "minecraft:terracotta"
      ],
      "minecraft_tags_inherited": [
        "minecraft:sulfur_cube_archetype/regular",
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "smelting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:brown_glazed_terracotta"
      ],
      "sample_output_of": [
        "minecraft:brown_terracotta"
      ],
      "model_parents": [
        "block/brown_terracotta",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/brown_terracotta"
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
        "dye_color": {
          "value": "brown",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "processing_in": {
          "values": [
            "smelting"
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
      "id": "minecraft:brown_wool",
      "namespace": "minecraft",
      "display_name": "Brown Wool",
      "minecraft_tags_direct": [
        "minecraft:wool"
      ],
      "minecraft_tags_inherited": [
        "minecraft:dampens_vibrations",
        "minecraft:sulfur_cube_archetype/light",
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 5,
        "crafting_shapeless": 15
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "sample_ingredient_of": [
        "minecraft:brown_banner",
        "minecraft:brown_bed",
        "minecraft:brown_carpet",
        "minecraft:brown_harness",
        "minecraft:dye_black_wool",
        "minecraft:dye_blue_wool",
        "minecraft:dye_cyan_wool",
        "minecraft:dye_gray_wool",
        "minecraft:dye_green_wool",
        "minecraft:dye_light_blue_wool"
      ],
      "sample_output_of": [
        "minecraft:dye_brown_wool"
      ],
      "model_parents": [
        "block/brown_wool",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/brown_wool",
        "minecraft:chests/village/village_shepherd",
        "minecraft:entities/sheep/brown",
        "minecraft:gameplay/hero_of_the_village/shepherd_gift",
        "minecraft:shearing/sheep/brown"
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
          "value": "wool",
          "confidence": 1,
          "source": "rule:material_family_from_tag",
          "rationale": "tag minecraft:wool"
        },
        "form": {
          "value": "whole_block",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:wool"
        },
        "dye_color": {
          "value": "brown",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
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
            "mob_drop",
            "village"
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