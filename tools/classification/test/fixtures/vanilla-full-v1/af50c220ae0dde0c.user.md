# Items to classify
{
  "items": [
    {
      "id": "minecraft:cut_sandstone",
      "namespace": "minecraft",
      "display_name": "Cut Sandstone",
      "minecraft_tags_direct": [
        "minecraft:sulfur_cube_archetype/regular"
      ],
      "minecraft_tags_inherited": [
        "minecraft:sulfur_cube_swallowable"
      ],
      "processing_in": [
        "crafting",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "stonecutting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 1
      },
      "sample_ingredient_of": [
        "minecraft:cut_sandstone_slab",
        "minecraft:cut_sandstone_slab_from_cut_sandstone_stonecutting",
        "minecraft:sandstone_stairs"
      ],
      "sample_output_of": [
        "minecraft:cut_sandstone",
        "minecraft:cut_sandstone_from_sandstone_stonecutting"
      ],
      "model_parents": [
        "block/cut_sandstone",
        "block/cube_column",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/cut_sandstone"
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
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "stonecutting"
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
      "id": "minecraft:cut_sandstone_slab",
      "namespace": "minecraft",
      "display_name": "Cut Sandstone Slab",
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
        "minecraft:cut_sandstone_slab",
        "minecraft:cut_sandstone_slab_from_cut_sandstone_stonecutting",
        "minecraft:cut_sandstone_slab_from_sandstone_stonecutting"
      ],
      "model_parents": [
        "block/cut_sandstone_slab",
        "block/slab",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/cut_sandstone_slab"
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
      "id": "minecraft:cyan_banner",
      "namespace": "minecraft",
      "display_name": "Cyan Banner",
      "minecraft_tags_direct": [
        "minecraft:banners"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_special_bannerduplicate": 1,
        "crafting_special_shielddecoration": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_special_bannerduplicate": 1
      },
      "sample_ingredient_of": [
        "minecraft:cyan_banner_duplicate",
        "minecraft:shield_decoration"
      ],
      "sample_output_of": [
        "minecraft:cyan_banner",
        "minecraft:cyan_banner_duplicate"
      ],
      "model_parents": [
        "item/template_banner"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/cyan_banner"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:banner_patterns": [],
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
        "form": {
          "value": "banner",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:banners"
        },
        "dye_color": {
          "value": "cyan",
          "confidence": 1,
          "source": "rule:dye_color_from_id",
          "rationale": "id prefix + tag"
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
      "id": "minecraft:cyan_bed",
      "namespace": "minecraft",
      "display_name": "Cyan Bed",
      "minecraft_tags_direct": [
        "minecraft:beds"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 15
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "sample_ingredient_of": [
        "minecraft:dye_black_bed",
        "minecraft:dye_blue_bed",
        "minecraft:dye_brown_bed",
        "minecraft:dye_gray_bed",
        "minecraft:dye_green_bed",
        "minecraft:dye_light_blue_bed",
        "minecraft:dye_light_gray_bed",
        "minecraft:dye_lime_bed",
        "minecraft:dye_magenta_bed",
        "minecraft:dye_orange_bed"
      ],
      "sample_output_of": [
        "minecraft:cyan_bed",
        "minecraft:dye_cyan_bed"
      ],
      "model_parents": [
        "block/cyan_bed_head",
        "block/bed_head",
        "block/template_bed"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/cyan_bed"
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
        "form": {
          "value": "bed",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:beds"
        },
        "dye_color": {
          "value": "cyan",
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
        },
        "is_fuel": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_fuel_from_id_or_tag"
        }
      }
    },
    {
      "id": "minecraft:cyan_bundle",
      "namespace": "minecraft",
      "display_name": "Cyan Bundle",
      "minecraft_tags_direct": [
        "minecraft:bundles"
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
        "minecraft:black_bundle",
        "minecraft:blue_bundle",
        "minecraft:brown_bundle",
        "minecraft:cyan_bundle",
        "minecraft:gray_bundle",
        "minecraft:green_bundle",
        "minecraft:light_blue_bundle",
        "minecraft:light_gray_bundle",
        "minecraft:lime_bundle",
        "minecraft:magenta_bundle"
      ],
      "sample_output_of": [
        "minecraft:cyan_bundle"
      ],
      "model_parents": [
        "item/cyan_bundle_open_back",
        "item/template_bundle_open_back",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [],
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
        "has_nbt_variation": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_nbt_variation_from_component"
        },
        "processing_in": {
          "values": [
            "crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    }
  ]
}
Respond with a single JSON object matching the expected output shape above. No other text.