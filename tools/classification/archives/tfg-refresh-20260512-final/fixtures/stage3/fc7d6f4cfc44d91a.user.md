# Items to classify
{
  "items": [
    {
      "id": "tfg:rock/chiseled_bricks_moon_deepslate_slab",
      "namespace": "tfg",
      "display_name": "Chiseled Norite Brick Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:slabs",
        "tfc:igneous_intrusive_items",
        "tfg:stonecutting/moon_deepslate_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "stonecutting": 2
      },
      "recipe_production_by_type": {
        "stonecutting": 3,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "tfg:stonecutter/tfg_rock_bricks_moon_deepslate_slab_slab_to_slab",
        "tfg:stonecutter/tfg_rock_polished_moon_deepslate_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfg:chisel/tfg_rock_chiseled_bricks_moon_deepslate_to_tfg_rock_chiseled_bricks_moon_deepslate_slab",
        "tfg:stonecutter/tfg_rock_chiseled_bricks_moon_deepslate_slab_half",
        "tfg:stonecutter/tfg_rock_chiseled_bricks_moon_deepslate_slab_slab_to_slab",
        "tfg:stonecutting/tfg_rock_chiseled_bricks_moon_deepslate_to_tfg_rock_chiseled_bricks_moon_deepslate_slab"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/chiseled_bricks_moon_deepslate_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs",
          "tfc:igneous_intrusive_items"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Igneous Intrusive"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
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
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:slabs"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "processing_in": {
          "values": [
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/chiseled_bricks_moon_deepslate_stairs",
      "namespace": "tfg",
      "display_name": "Chiseled Norite Brick Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:stairs",
        "tfc:igneous_intrusive_items",
        "tfg:stonecutting/moon_deepslate"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 12,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "tfg:stonecutter/tfg_rock_bricks_moon_deepslate",
        "tfg:stonecutter/tfg_rock_bricks_moon_deepslate_slab_half",
        "tfg:stonecutter/tfg_rock_bricks_moon_deepslate_stairs",
        "tfg:stonecutter/tfg_rock_bricks_moon_deepslate_wall",
        "tfg:stonecutter/tfg_rock_chiseled_bricks_moon_deepslate",
        "tfg:stonecutter/tfg_rock_chiseled_bricks_moon_deepslate_slab_half",
        "tfg:stonecutter/tfg_rock_chiseled_bricks_moon_deepslate_wall",
        "tfg:stonecutter/tfg_rock_pillar_moon_deepslate",
        "tfg:stonecutter/tfg_rock_polished_moon_deepslate",
        "tfg:stonecutter/tfg_rock_polished_moon_deepslate_slab_half",
        "tfg:stonecutter/tfg_rock_polished_moon_deepslate_stairs",
        "tfg:stonecutter/tfg_rock_polished_moon_deepslate_wall"
      ],
      "recipe_output_examples": [
        "tfg:chisel/tfg_rock_chiseled_bricks_moon_deepslate_to_tfg_rock_chiseled_bricks_moon_deepslate_stairs",
        "tfg:stonecutter/tfg_rock_chiseled_bricks_moon_deepslate_stairs",
        "tfg:stonecutter/tfg_rock_chiseled_bricks_moon_deepslate_to_tfg_rock_chiseled_bricks_moon_deepslate_stairs"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/chiseled_bricks_moon_deepslate_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs",
          "tfc:igneous_intrusive_items"
        ],
        "requires_correct_tool": true
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
          "text": "Igneous Intrusive"
        },
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
        "processing_in": {
          "values": [
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/chiseled_bricks_moon_deepslate_wall",
      "namespace": "tfg",
      "display_name": "Chiseled Norite Brick Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:walls",
        "tfc:igneous_intrusive_items",
        "tfg:stonecutting/moon_deepslate"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 12,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "tfg:stonecutter/tfg_rock_bricks_moon_deepslate",
        "tfg:stonecutter/tfg_rock_bricks_moon_deepslate_slab_half",
        "tfg:stonecutter/tfg_rock_bricks_moon_deepslate_stairs",
        "tfg:stonecutter/tfg_rock_bricks_moon_deepslate_wall",
        "tfg:stonecutter/tfg_rock_chiseled_bricks_moon_deepslate",
        "tfg:stonecutter/tfg_rock_chiseled_bricks_moon_deepslate_slab_half",
        "tfg:stonecutter/tfg_rock_chiseled_bricks_moon_deepslate_stairs",
        "tfg:stonecutter/tfg_rock_pillar_moon_deepslate",
        "tfg:stonecutter/tfg_rock_polished_moon_deepslate",
        "tfg:stonecutter/tfg_rock_polished_moon_deepslate_slab_half",
        "tfg:stonecutter/tfg_rock_polished_moon_deepslate_stairs",
        "tfg:stonecutter/tfg_rock_polished_moon_deepslate_wall"
      ],
      "recipe_output_examples": [
        "tfc:kjs/61sfgivnmoap9h6d1v1yvjy7j",
        "tfg:stonecutter/tfg_rock_chiseled_bricks_moon_deepslate_wall",
        "tfg:stonecutting/tfg_rock_chiseled_bricks_moon_deepslate_to_tfg_rock_chiseled_bricks_moon_deepslate_wall"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/chiseled_bricks_moon_deepslate_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:walls",
          "tfc:igneous_intrusive_items"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Igneous Intrusive"
        },
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
        "processing_in": {
          "values": [
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/chiseled_bricks_moon_wall",
      "namespace": "tfg",
      "display_name": "Chiseled Anorthosite Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:walls",
        "tfc:igneous_intrusive_items",
        "tfg:stonecutting/moon_stone"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 12,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "tfg:stonecutter/ad_astra_chiseled_moon_stone_bricks",
        "tfg:stonecutter/ad_astra_chiseled_moon_stone_slab_half",
        "tfg:stonecutter/ad_astra_chiseled_moon_stone_stairs",
        "tfg:stonecutter/ad_astra_moon_pillar",
        "tfg:stonecutter/ad_astra_moon_stone_brick_slab_half",
        "tfg:stonecutter/ad_astra_moon_stone_brick_stairs",
        "tfg:stonecutter/ad_astra_moon_stone_brick_wall",
        "tfg:stonecutter/ad_astra_moon_stone_bricks",
        "tfg:stonecutter/ad_astra_polished_moon_stone",
        "tfg:stonecutter/ad_astra_polished_moon_stone_slab_half",
        "tfg:stonecutter/ad_astra_polished_moon_stone_stairs",
        "tfg:stonecutter/tfg_rock_polished_moon_wall"
      ],
      "recipe_output_examples": [
        "tfc:kjs/bsbleu96srk08f1kl3msqqsdv",
        "tfg:stonecutter/tfg_rock_chiseled_bricks_moon_wall",
        "tfg:stonecutting/ad_astra_chiseled_moon_stone_bricks_to_tfg_rock_chiseled_bricks_moon_wall"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/chiseled_bricks_moon_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:walls",
          "tfc:igneous_intrusive_items"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Igneous Intrusive"
        },
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
        "processing_in": {
          "values": [
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/chiseled_bricks_permafrost_wall",
      "namespace": "tfg",
      "display_name": "Chiseled Permafrost Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:walls",
        "tfg:stonecutting/permafrost"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "stonecutting": 13
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 13,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "tfg:stonecutter/ad_astra_chiseled_permafrost_brick_slab_half",
        "tfg:stonecutter/ad_astra_chiseled_permafrost_brick_stairs",
        "tfg:stonecutter/ad_astra_chiseled_permafrost_bricks",
        "tfg:stonecutter/ad_astra_permafrost_brick_slab_half",
        "tfg:stonecutter/ad_astra_permafrost_brick_stairs",
        "tfg:stonecutter/ad_astra_permafrost_brick_wall",
        "tfg:stonecutter/ad_astra_permafrost_bricks",
        "tfg:stonecutter/ad_astra_permafrost_pillar",
        "tfg:stonecutter/ad_astra_permafrost_tiles",
        "tfg:stonecutter/ad_astra_polished_permafrost",
        "tfg:stonecutter/ad_astra_polished_permafrost_slab_half",
        "tfg:stonecutter/ad_astra_polished_permafrost_stairs",
        "tfg:stonecutter/tfg_rock_polished_permafrost_wall"
      ],
      "recipe_output_examples": [
        "tfc:kjs/7xbhsuzuiq7ua12dr8d83sb4q",
        "tfg:stonecutter/tfg_rock_chiseled_bricks_permafrost_wall",
        "tfg:stonecutting/ad_astra_chiseled_permafrost_bricks_to_tfg_rock_chiseled_bricks_permafrost_wall"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/chiseled_bricks_permafrost_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:walls"
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
        "processing_in": {
          "values": [
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/chiseled_bricks_venus_wall",
      "namespace": "tfg",
      "display_name": "Chiseled Trachyte Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:walls",
        "tfc:igneous_extrusive_items",
        "tfg:stonecutting/venus_stone"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 12,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "tfg:stonecutter/ad_astra_chiseled_venus_stone_bricks",
        "tfg:stonecutter/ad_astra_chiseled_venus_stone_slab_half",
        "tfg:stonecutter/ad_astra_chiseled_venus_stone_stairs",
        "tfg:stonecutter/ad_astra_polished_venus_stone",
        "tfg:stonecutter/ad_astra_polished_venus_stone_slab_half",
        "tfg:stonecutter/ad_astra_polished_venus_stone_stairs",
        "tfg:stonecutter/ad_astra_venus_pillar",
        "tfg:stonecutter/ad_astra_venus_stone_brick_slab_half",
        "tfg:stonecutter/ad_astra_venus_stone_brick_stairs",
        "tfg:stonecutter/ad_astra_venus_stone_brick_wall",
        "tfg:stonecutter/ad_astra_venus_stone_bricks",
        "tfg:stonecutter/tfg_rock_polished_venus_wall"
      ],
      "recipe_output_examples": [
        "tfc:kjs/14rf2ab1axvw4h8ggukuot09o",
        "tfg:stonecutter/tfg_rock_chiseled_bricks_venus_wall",
        "tfg:stonecutting/ad_astra_chiseled_venus_stone_bricks_to_tfg_rock_chiseled_bricks_venus_wall"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/chiseled_bricks_venus_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "minecraft:walls",
          "tfc:igneous_extrusive_items"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Igneous Extrusive"
        },
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
        "processing_in": {
          "values": [
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/chiseled_flavolite_slab",
      "namespace": "tfg",
      "display_name": "Ignimbrite Tile Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:slabs",
        "tfc:igneous_extrusive_items",
        "tfg:stonecutting/flavolite_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "stonecutting": 2
      },
      "recipe_production_by_type": {
        "stonecutting": 3,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "tfg:stonecutter/tfg_rock_bricks_flavolite_slab_slab_to_slab",
        "tfg:stonecutter/tfg_rock_polished_flavolite_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfg:chisel/betterend_flavolite_tiles_to_tfg_rock_chiseled_flavolite_slab",
        "tfg:stonecutter/tfg_rock_chiseled_flavolite_slab_half",
        "tfg:stonecutter/tfg_rock_chiseled_flavolite_slab_slab_to_slab",
        "tfg:stonecutting/betterend_flavolite_tiles_to_tfg_rock_chiseled_flavolite_slab"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/chiseled_flavolite_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs",
          "tfc:igneous_extrusive_items"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Igneous Extrusive"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
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
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:slabs"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "processing_in": {
          "values": [
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/chiseled_flavolite_stairs",
      "namespace": "tfg",
      "display_name": "Ignimbrite Tile Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:stairs",
        "tfc:igneous_extrusive_items",
        "tfg:stonecutting/flavolite"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 12,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "tfg:stonecutter/betterend_flavolite_bricks",
        "tfg:stonecutter/betterend_flavolite_pillar",
        "tfg:stonecutter/betterend_flavolite_polished",
        "tfg:stonecutter/betterend_flavolite_tiles",
        "tfg:stonecutter/tfg_rock_bricks_flavolite_slab_half",
        "tfg:stonecutter/tfg_rock_bricks_flavolite_stairs",
        "tfg:stonecutter/tfg_rock_bricks_flavolite_wall",
        "tfg:stonecutter/tfg_rock_chiseled_flavolite_slab_half",
        "tfg:stonecutter/tfg_rock_chiseled_flavolite_wall",
        "tfg:stonecutter/tfg_rock_polished_flavolite_slab_half",
        "tfg:stonecutter/tfg_rock_polished_flavolite_stairs",
        "tfg:stonecutter/tfg_rock_polished_flavolite_wall"
      ],
      "recipe_output_examples": [
        "tfg:chisel/betterend_flavolite_tiles_to_tfg_rock_chiseled_flavolite_stairs",
        "tfg:stonecutter/betterend_flavolite_tiles_to_tfg_rock_chiseled_flavolite_stairs",
        "tfg:stonecutter/tfg_rock_chiseled_flavolite_stairs"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/chiseled_flavolite_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs",
          "tfc:igneous_extrusive_items"
        ],
        "requires_correct_tool": true
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
          "text": "Igneous Extrusive"
        },
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
        "processing_in": {
          "values": [
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/chiseled_flavolite_wall",
      "namespace": "tfg",
      "display_name": "Ignimbrite Tile Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:walls",
        "tfc:igneous_extrusive_items",
        "tfg:stonecutting/flavolite"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 12,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "tfg:stonecutter/betterend_flavolite_bricks",
        "tfg:stonecutter/betterend_flavolite_pillar",
        "tfg:stonecutter/betterend_flavolite_polished",
        "tfg:stonecutter/betterend_flavolite_tiles",
        "tfg:stonecutter/tfg_rock_bricks_flavolite_slab_half",
        "tfg:stonecutter/tfg_rock_bricks_flavolite_stairs",
        "tfg:stonecutter/tfg_rock_bricks_flavolite_wall",
        "tfg:stonecutter/tfg_rock_chiseled_flavolite_slab_half",
        "tfg:stonecutter/tfg_rock_chiseled_flavolite_stairs",
        "tfg:stonecutter/tfg_rock_polished_flavolite_slab_half",
        "tfg:stonecutter/tfg_rock_polished_flavolite_stairs",
        "tfg:stonecutter/tfg_rock_polished_flavolite_wall"
      ],
      "recipe_output_examples": [
        "tfc:kjs/8dcvdmvxd72wf1aq2umz8nts8",
        "tfg:stonecutter/tfg_rock_chiseled_flavolite_wall",
        "tfg:stonecutting/betterend_flavolite_tiles_to_tfg_rock_chiseled_flavolite_wall"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/chiseled_flavolite_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:walls",
          "tfc:igneous_extrusive_items"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Igneous Extrusive"
        },
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
        "processing_in": {
          "values": [
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/chiseled_sandy_jadestone_slab",
      "namespace": "tfg",
      "display_name": "Lamproite Tile Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:slabs",
        "tfc:igneous_extrusive_items",
        "tfg:stonecutting/sandy_jadestone_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "stonecutting": 2
      },
      "recipe_production_by_type": {
        "stonecutting": 3,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "tfg:stonecutter/tfg_rock_bricks_sandy_jadestone_slab_slab_to_slab",
        "tfg:stonecutter/tfg_rock_polished_sandy_jadestone_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfg:chisel/betterend_sandy_jadestone_tiles_to_tfg_rock_chiseled_sandy_jadestone_slab",
        "tfg:stonecutter/tfg_rock_chiseled_sandy_jadestone_slab_half",
        "tfg:stonecutter/tfg_rock_chiseled_sandy_jadestone_slab_slab_to_slab",
        "tfg:stonecutting/betterend_sandy_jadestone_tiles_to_tfg_rock_chiseled_sandy_jadestone_slab"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/chiseled_sandy_jadestone_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs",
          "tfc:igneous_extrusive_items"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Igneous Extrusive"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
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
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:slabs"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "processing_in": {
          "values": [
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/chiseled_sandy_jadestone_stairs",
      "namespace": "tfg",
      "display_name": "Lamproite Tile Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:stairs",
        "tfc:igneous_extrusive_items",
        "tfg:stonecutting/sandy_jadestone"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 12,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "tfg:stonecutter/betterend_sandy_jadestone_bricks",
        "tfg:stonecutter/betterend_sandy_jadestone_pillar",
        "tfg:stonecutter/betterend_sandy_jadestone_polished",
        "tfg:stonecutter/betterend_sandy_jadestone_tiles",
        "tfg:stonecutter/tfg_rock_bricks_sandy_jadestone_slab_half",
        "tfg:stonecutter/tfg_rock_bricks_sandy_jadestone_stairs",
        "tfg:stonecutter/tfg_rock_bricks_sandy_jadestone_wall",
        "tfg:stonecutter/tfg_rock_chiseled_sandy_jadestone_slab_half",
        "tfg:stonecutter/tfg_rock_chiseled_sandy_jadestone_wall",
        "tfg:stonecutter/tfg_rock_polished_sandy_jadestone_slab_half",
        "tfg:stonecutter/tfg_rock_polished_sandy_jadestone_stairs",
        "tfg:stonecutter/tfg_rock_polished_sandy_jadestone_wall"
      ],
      "recipe_output_examples": [
        "tfg:chisel/betterend_sandy_jadestone_tiles_to_tfg_rock_chiseled_sandy_jadestone_stairs",
        "tfg:stonecutter/betterend_sandy_jadestone_tiles_to_tfg_rock_chiseled_sandy_jadestone_stairs",
        "tfg:stonecutter/tfg_rock_chiseled_sandy_jadestone_stairs"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/chiseled_sandy_jadestone_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs",
          "tfc:igneous_extrusive_items"
        ],
        "requires_correct_tool": true
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
          "text": "Igneous Extrusive"
        },
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
        "processing_in": {
          "values": [
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/chiseled_sandy_jadestone_wall",
      "namespace": "tfg",
      "display_name": "Lamproite Tile Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:walls",
        "tfc:igneous_extrusive_items",
        "tfg:stonecutting/sandy_jadestone"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "stonecutting": 12
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 12,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "tfg:stonecutter/betterend_sandy_jadestone_bricks",
        "tfg:stonecutter/betterend_sandy_jadestone_pillar",
        "tfg:stonecutter/betterend_sandy_jadestone_polished",
        "tfg:stonecutter/betterend_sandy_jadestone_tiles",
        "tfg:stonecutter/tfg_rock_bricks_sandy_jadestone_slab_half",
        "tfg:stonecutter/tfg_rock_bricks_sandy_jadestone_stairs",
        "tfg:stonecutter/tfg_rock_bricks_sandy_jadestone_wall",
        "tfg:stonecutter/tfg_rock_chiseled_sandy_jadestone_slab_half",
        "tfg:stonecutter/tfg_rock_chiseled_sandy_jadestone_stairs",
        "tfg:stonecutter/tfg_rock_polished_sandy_jadestone_slab_half",
        "tfg:stonecutter/tfg_rock_polished_sandy_jadestone_stairs",
        "tfg:stonecutter/tfg_rock_polished_sandy_jadestone_wall"
      ],
      "recipe_output_examples": [
        "tfc:kjs/5ye5g7ri0xfjj9yeu3fdk6sy2",
        "tfg:stonecutter/tfg_rock_chiseled_sandy_jadestone_wall",
        "tfg:stonecutting/betterend_sandy_jadestone_tiles_to_tfg_rock_chiseled_sandy_jadestone_wall"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/chiseled_sandy_jadestone_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:walls",
          "tfc:igneous_extrusive_items"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Igneous Extrusive"
        },
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
        "processing_in": {
          "values": [
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/chiseled_tuff",
      "namespace": "tfg",
      "display_name": "Chiseled Tuff",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/tuff",
        "tfc:igneous_extrusive_items",
        "tfc:rock/chiseled_bricks",
        "tfg:stone_composition/igneous_felsic",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 24
      },
      "recipe_production_by_type": {
        "stonecutting": 1,
        "tfc:chisel": 1,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 28,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_felsic",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfg:stonecutter/create_cut_tuff",
        "tfg:stonecutter/create_cut_tuff_brick_slab_half",
        "tfg:stonecutter/create_cut_tuff_brick_stairs",
        "tfg:stonecutter/create_cut_tuff_brick_wall",
        "tfg:stonecutter/create_cut_tuff_bricks",
        "tfg:stonecutter/create_cut_tuff_slab_half",
        "tfg:stonecutter/create_cut_tuff_stairs",
        "tfg:stonecutter/create_cut_tuff_wall",
        "tfg:stonecutter/create_layered_tuff",
        "tfg:stonecutter/create_polished_cut_tuff",
        "tfg:stonecutter/create_polished_cut_tuff_slab_half",
        "tfg:stonecutter/create_polished_cut_tuff_stairs",
        "tfg:stonecutter/create_polished_cut_tuff_wall",
        "tfg:stonecutter/create_small_tuff_brick_slab_half",
        "tfg:stonecutter/create_small_tuff_brick_stairs",
        "tfg:stonecutter/create_small_tuff_brick_wall",
        "tfg:stonecutter/create_small_tuff_bricks",
        "tfg:stonecutter/create_tuff_pillar",
        "tfg:stonecutter/minecraft_tuff",
        "tfg:stonecutter/tfg_rock_bricks_tuff",
        "tfg:stonecutter/tfg_rock_bricks_tuff_slab_half",
        "tfg:stonecutter/tfg_rock_bricks_tuff_stairs",
        "tfg:stonecutter/tfg_rock_bricks_tuff_wall",
        "tfg:stonecutter/tfg_rock_chiseled_tuff_bricks"
      ],
      "recipe_output_examples": [
        "tfg:chisel/tuff_bricks_to_chiseled",
        "tfg:shapeless/tuff_bricks_to_chiseled",
        "tfg:stonecutter/tfg_rock_chiseled_tuff"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/chiseled_tuff",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "tfc:bloomery_insulation",
          "tfc:forge_insulation",
          "tfc:igneous_extrusive_items"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Igneous Extrusive"
        },
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
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "processing_in": {
          "values": [
            "greate:milling",
            "kubejs:shapeless",
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/chiseled_tuff_bricks",
      "namespace": "tfg",
      "display_name": "Chiseled Tuff Bricks",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/tuff",
        "forge:stone_bricks",
        "tfc:igneous_extrusive_items",
        "tfc:rock/bricks",
        "tfc:rock/chiseled_bricks",
        "tfg:stone_composition/igneous_felsic",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 21,
        "greate:milling": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 24
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 50,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "firmalife:crafting/sealed_bricks",
        "greate:milling/integration/tfg/macerate_igneous_felsic",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfg:shapeless/bismuth_bronze_plated_block",
        "tfg:shapeless/bismuth_plated_block",
        "tfg:shapeless/black_bronze_plated_block",
        "tfg:shapeless/black_steel_plated_block",
        "tfg:shapeless/blue_steel_plated_block",
        "tfg:shapeless/brass_plated_block",
        "tfg:shapeless/bronze_plated_block",
        "tfg:shapeless/chromium_plated_block",
        "tfg:shapeless/copper_plated_block",
        "tfg:shapeless/gold_plated_block",
        "tfg:shapeless/iron_plated_block",
        "tfg:shapeless/nickel_plated_block",
        "tfg:shapeless/red_steel_plated_block",
        "tfg:shapeless/rose_gold_plated_block",
        "tfg:shapeless/silver_plated_block",
        "tfg:shapeless/stainless_steel_plated_block",
        "tfg:shapeless/steel_plated_block",
        "tfg:shapeless/sterling_silver_plated_block",
        "tfg:shapeless/tin_plated_block",
        "tfg:shapeless/wrought_iron_plated_block",
        "tfg:shapeless/zinc_plated_block",
        "tfg:stonecutter/create_cut_tuff",
        "tfg:stonecutter/create_cut_tuff_brick_slab_half",
        "tfg:stonecutter/create_cut_tuff_brick_stairs",
        "tfg:stonecutter/create_cut_tuff_brick_wall",
        "tfg:stonecutter/create_cut_tuff_bricks",
        "tfg:stonecutter/create_cut_tuff_slab_half",
        "tfg:stonecutter/create_cut_tuff_stairs",
        "tfg:stonecutter/create_cut_tuff_wall",
        "tfg:stonecutter/create_layered_tuff",
        "tfg:stonecutter/create_polished_cut_tuff",
        "tfg:stonecutter/create_polished_cut_tuff_slab_half",
        "tfg:stonecutter/create_polished_cut_tuff_stairs",
        "tfg:stonecutter/create_polished_cut_tuff_wall",
        "tfg:stonecutter/create_small_tuff_brick_slab_half",
        "tfg:stonecutter/create_small_tuff_brick_stairs",
        "tfg:stonecutter/create_small_tuff_brick_wall",
        "tfg:stonecutter/create_small_tuff_bricks",
        "tfg:stonecutter/create_tuff_pillar",
        "tfg:stonecutter/minecraft_tuff",
        "tfg:stonecutter/tfg_rock_bricks_tuff",
        "tfg:stonecutter/tfg_rock_bricks_tuff_slab_half",
        "tfg:stonecutter/tfg_rock_bricks_tuff_stairs",
        "tfg:stonecutter/tfg_rock_bricks_tuff_wall",
        "tfg:stonecutter/tfg_rock_chiseled_tuff"
      ],
      "recipe_output_examples": [
        "tfg:stonecutter/tfg_rock_chiseled_tuff_bricks"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/chiseled_tuff_bricks",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "firmalife:oven_insulation",
          "minecraft:mineable/pickaxe",
          "tfc:bloomery_insulation",
          "tfc:forge_insulation",
          "tfc:igneous_extrusive_items"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Igneous Extrusive"
        },
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
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "kubejs:shapeless",
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/cobble_blackstone",
      "namespace": "tfg",
      "display_name": "Pyroxenite Cobble",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:cobblestone",
        "forge:cobblestone/normal",
        "tfc:igneous_intrusive_items",
        "tfg:interaction/cobble",
        "tfg:stone_composition/igneous_felsic",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:deploying",
        "greate:haunting",
        "greate:milling",
        "greate:mixing",
        "greate:pressing",
        "kubejs:shaped",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 7,
        "crafting_shapeless": 3,
        "create:deploying": 1,
        "greate:haunting": 1,
        "greate:milling": 1,
        "greate:mixing": 1,
        "greate:pressing": 1,
        "kubejs:shaped": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 3
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "create:deploying": 2,
        "greate:pressing": 1,
        "tfc:collapse": 3,
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 22,
      "recipe_output_count": 8,
      "recipe_ingredient_examples": [
        "firmalife:crafting/tile_finish",
        "greate:haunting/integration/create/haunting/blackstone",
        "greate:milling/integration/tfg/macerate_igneous_felsic",
        "greate:mixing/integration/tfg/blackstone_cobble_to_mossy_cobble",
        "greate:pressing/blackstone_cobble_to_gravel",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "gtceu:shaped/dispenser",
        "gtceu:shaped/dropper",
        "tfc:crafting/vanilla/redstone/piston",
        "tfg:create/shaped/redstone_contact",
        "tfg:create/shaped/sticker",
        "tfg:deploying/blackstone_cobble_to_mossy_cobble",
        "tfg:shaped/observer_certus_q",
        "tfg:shaped/observer_nether_q",
        "tfg:shaped/observer_quartzite",
        "tfg:shapeless/lever",
        "tfg:shapeless/unpacking_blackstone_cobble",
        "tfg:stonecutter/tfg_rock_cobble_blackstone_to_tfg_rock_cobble_blackstone_stairs",
        "tfg:stonecutting/tfg_rock_cobble_blackstone_to_tfg_rock_cobble_blackstone_slab",
        "tfg:stonecutting/tfg_rock_cobble_blackstone_to_tfg_rock_cobble_blackstone_wall"
      ],
      "recipe_output_examples": [
        "greate:pressing/blackstone_raw_to_cobble",
        "tfc:kjs/40rencbkenfnu44v4mv7w5ob",
        "tfc:kjs/4zeu26p6383xh1wczq3u0r5qw",
        "tfc:kjs/anevoaicu8wbf2dimql34fo9o",
        "tfc:kjs/ymh54acnnivms09i49974t41",
        "tfg:deploying/blackstone_mossy_cobble_to_cobble_knife",
        "tfg:deploying/blackstone_mossy_cobble_to_cobble_pumice",
        "tfg:shaped/packing_blackstone_cobble"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/cobble_blackstone",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "firmalife:oven_insulation",
          "forge:cobblestone",
          "minecraft:enderman_holdable",
          "minecraft:mineable/pickaxe",
          "tfc:bloomery_insulation",
          "tfc:can_landslide",
          "tfc:creeping_stone_plantable_on",
          "tfc:forge_insulation",
          "tfc:igneous_intrusive_items",
          "tfc:toughness_2",
          "tfg:anemone_plantable_on"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Igneous Intrusive"
        },
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
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "create:deploying",
            "greate:haunting",
            "greate:milling",
            "greate:mixing",
            "greate:pressing",
            "kubejs:shaped",
            "kubejs:shapeless",
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/cobble_blackstone_slab",
      "namespace": "tfg",
      "display_name": "Pyroxenite Cobble Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:slabs",
        "tfc:igneous_intrusive_items",
        "tfg:interaction/cobble_slab",
        "tfg:rock_slabs",
        "tfg:stone_composition/igneous_felsic_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 1,
        "tfc:chisel": 1,
        "tfc:collapse": 2
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_felsic_half",
        "minecraft:kjs/beneath_blackstone_pebble_2"
      ],
      "recipe_output_examples": [
        "tfc:kjs/c0bsb56zbseubmhqpkfryisp2",
        "tfc:kjs/k1g9s2e5u8hft7d7ostzfxpl",
        "tfg:chisel/tfg_rock_cobble_blackstone_to_tfg_rock_cobble_blackstone_slab",
        "tfg:stonecutting/tfg_rock_cobble_blackstone_to_tfg_rock_cobble_blackstone_slab"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/cobble_blackstone_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs",
          "tfc:igneous_intrusive_items"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Igneous Intrusive"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
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
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:slabs"
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/cobble_blackstone_stairs",
      "namespace": "tfg",
      "display_name": "Pyroxenite Cobble Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:stairs",
        "tfc:igneous_intrusive_items",
        "tfg:interaction/cobble_stairs",
        "tfg:rock_stairs",
        "tfg:stone_composition/igneous_felsic"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 1,
        "tfc:chisel": 1,
        "tfc:collapse": 2
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_felsic",
        "minecraft:kjs/beneath_blackstone_pebble"
      ],
      "recipe_output_examples": [
        "tfc:kjs/4exjikkvykxelb5frfubbb9qf",
        "tfc:kjs/cllf618fqfm7pd5r4uq66s2cc",
        "tfg:chisel/tfg_rock_cobble_blackstone_to_tfg_rock_cobble_blackstone_stairs",
        "tfg:stonecutter/tfg_rock_cobble_blackstone_to_tfg_rock_cobble_blackstone_stairs"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/cobble_blackstone_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs",
          "tfc:igneous_intrusive_items"
        ],
        "requires_correct_tool": true
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
          "text": "Igneous Intrusive"
        },
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
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/cobble_blackstone_wall",
      "namespace": "tfg",
      "display_name": "Pyroxenite Cobble Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:walls",
        "tfc:igneous_intrusive_items",
        "tfg:interaction/cobble_wall",
        "tfg:rock_walls",
        "tfg:stone_composition/igneous_felsic_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 1,
        "tfc:chisel": 1,
        "tfc:collapse": 2
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_felsic_half",
        "minecraft:kjs/beneath_blackstone_pebble_3"
      ],
      "recipe_output_examples": [
        "tfc:kjs/102tvwbc5m198hd2wkq350nub",
        "tfc:kjs/bz6qv7gg43blyxzzcf5m90rxt",
        "tfc:kjs/esmqd4mo4ysoruif3g7xugkc",
        "tfg:stonecutting/tfg_rock_cobble_blackstone_to_tfg_rock_cobble_blackstone_wall"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/cobble_blackstone_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:walls",
          "tfc:igneous_intrusive_items"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Igneous Intrusive"
        },
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
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/cobble_crackrack",
      "namespace": "tfg",
      "display_name": "Keratophyre Cobble",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:cobblestone",
        "forge:cobblestone/normal",
        "tfc:igneous_extrusive_items",
        "tfg:interaction/cobble",
        "tfg:stone_composition/igneous_intermediate",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:deploying",
        "greate:haunting",
        "greate:milling",
        "greate:mixing",
        "greate:pressing",
        "kubejs:shaped",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 7,
        "crafting_shapeless": 3,
        "create:deploying": 1,
        "greate:haunting": 1,
        "greate:milling": 1,
        "greate:mixing": 1,
        "greate:pressing": 1,
        "kubejs:shaped": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 3
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "create:deploying": 2,
        "greate:pressing": 1,
        "tfc:collapse": 3,
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 22,
      "recipe_output_count": 8,
      "recipe_ingredient_examples": [
        "firmalife:crafting/tile_finish",
        "greate:haunting/integration/create/haunting/blackstone",
        "greate:milling/integration/tfg/macerate_igneous_intermediate",
        "greate:mixing/integration/tfg/crackrack_cobble_to_mossy_cobble",
        "greate:pressing/crackrack_cobble_to_gravel",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "gtceu:shaped/dispenser",
        "gtceu:shaped/dropper",
        "tfc:crafting/vanilla/redstone/piston",
        "tfg:create/shaped/redstone_contact",
        "tfg:create/shaped/sticker",
        "tfg:deploying/crackrack_cobble_to_mossy_cobble",
        "tfg:shaped/observer_certus_q",
        "tfg:shaped/observer_nether_q",
        "tfg:shaped/observer_quartzite",
        "tfg:shapeless/lever",
        "tfg:shapeless/unpacking_crackrack_cobble",
        "tfg:stonecutter/tfg_rock_cobble_crackrack_to_tfg_rock_cobble_crackrack_stairs",
        "tfg:stonecutting/tfg_rock_cobble_crackrack_to_tfg_rock_cobble_crackrack_slab",
        "tfg:stonecutting/tfg_rock_cobble_crackrack_to_tfg_rock_cobble_crackrack_wall"
      ],
      "recipe_output_examples": [
        "greate:pressing/crackrack_raw_to_cobble",
        "tfc:kjs/5axhlmj6pv2r88l0jh80u538q",
        "tfc:kjs/9y5juem83gixonwwngiddn8tg",
        "tfc:kjs/a27lzqz2pf4o8jco0e0ervcdh",
        "tfc:kjs/cc7x5xdu5cgmzo31x9sajcou3",
        "tfg:deploying/crackrack_mossy_cobble_to_cobble_knife",
        "tfg:deploying/crackrack_mossy_cobble_to_cobble_pumice",
        "tfg:shaped/packing_crackrack_cobble"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/cobble_crackrack",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "firmalife:oven_insulation",
          "forge:cobblestone",
          "minecraft:enderman_holdable",
          "minecraft:mineable/pickaxe",
          "tfc:bloomery_insulation",
          "tfc:can_landslide",
          "tfc:creeping_stone_plantable_on",
          "tfc:forge_insulation",
          "tfc:igneous_extrusive_items",
          "tfc:toughness_2",
          "tfg:anemone_plantable_on"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Igneous Extrusive"
        },
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
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "create:deploying",
            "greate:haunting",
            "greate:milling",
            "greate:mixing",
            "greate:pressing",
            "kubejs:shaped",
            "kubejs:shapeless",
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/cobble_crackrack_slab",
      "namespace": "tfg",
      "display_name": "Keratophyre Cobble Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:slabs",
        "tfc:igneous_extrusive_items",
        "tfg:interaction/cobble_slab",
        "tfg:rock_slabs",
        "tfg:stone_composition/igneous_intermediate_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 1,
        "tfc:chisel": 1,
        "tfc:collapse": 2
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_intermediate_half",
        "minecraft:kjs/tfg_loose_crackrack_2"
      ],
      "recipe_output_examples": [
        "tfc:kjs/7svlihap6cw0fdbfiyb8b0v17",
        "tfc:kjs/oe1umnoajfmdgqx67x6r435x",
        "tfg:chisel/tfg_rock_cobble_crackrack_to_tfg_rock_cobble_crackrack_slab",
        "tfg:stonecutting/tfg_rock_cobble_crackrack_to_tfg_rock_cobble_crackrack_slab"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/cobble_crackrack_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs",
          "tfc:igneous_extrusive_items"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Igneous Extrusive"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
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
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:slabs"
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/cobble_crackrack_stairs",
      "namespace": "tfg",
      "display_name": "Keratophyre Cobble Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:stairs",
        "tfc:igneous_extrusive_items",
        "tfg:interaction/cobble_stairs",
        "tfg:rock_stairs",
        "tfg:stone_composition/igneous_intermediate"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 1,
        "tfc:chisel": 1,
        "tfc:collapse": 2
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_intermediate",
        "minecraft:kjs/tfg_loose_crackrack"
      ],
      "recipe_output_examples": [
        "tfc:kjs/a78ioy3ilif1mhrd5mid150hn",
        "tfc:kjs/eduugj2yb3gg0f29plupdhb6j",
        "tfg:chisel/tfg_rock_cobble_crackrack_to_tfg_rock_cobble_crackrack_stairs",
        "tfg:stonecutter/tfg_rock_cobble_crackrack_to_tfg_rock_cobble_crackrack_stairs"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/cobble_crackrack_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs",
          "tfc:igneous_extrusive_items"
        ],
        "requires_correct_tool": true
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
          "text": "Igneous Extrusive"
        },
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
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/cobble_crackrack_wall",
      "namespace": "tfg",
      "display_name": "Keratophyre Cobble Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:walls",
        "tfc:igneous_extrusive_items",
        "tfg:interaction/cobble_wall",
        "tfg:rock_walls",
        "tfg:stone_composition/igneous_intermediate_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 1,
        "tfc:chisel": 1,
        "tfc:collapse": 2
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_intermediate_half",
        "minecraft:kjs/tfg_loose_crackrack_3"
      ],
      "recipe_output_examples": [
        "tfc:kjs/77vi09b3z11bl7ja1dtdq3gml",
        "tfc:kjs/cgzlv7r7s89ln8zzirbyevxry",
        "tfc:kjs/f02e06s8nv35jwy62z0dfq422",
        "tfg:stonecutting/tfg_rock_cobble_crackrack_to_tfg_rock_cobble_crackrack_wall"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/cobble_crackrack_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:walls",
          "tfc:igneous_extrusive_items"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Igneous Extrusive"
        },
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
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/cobble_dripstone",
      "namespace": "tfg",
      "display_name": "Travertine Cobble",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:cobblestone",
        "forge:cobblestone/normal",
        "tfc:sedimentary_items",
        "tfg:interaction/cobble",
        "tfg:stone_composition/sedimentary_carbonate",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:deploying",
        "greate:haunting",
        "greate:milling",
        "greate:mixing",
        "greate:pressing",
        "kubejs:shaped",
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 7,
        "crafting_shapeless": 3,
        "create:deploying": 1,
        "greate:haunting": 1,
        "greate:milling": 1,
        "greate:mixing": 1,
        "greate:pressing": 1,
        "kubejs:shaped": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 3
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "create:deploying": 2,
        "greate:pressing": 1,
        "tfc:collapse": 4,
        "tfc:landslide": 1
      },
      "recipe_ingredient_count": 22,
      "recipe_output_count": 9,
      "recipe_ingredient_examples": [
        "firmalife:crafting/tile_finish",
        "greate:haunting/integration/create/haunting/blackstone",
        "greate:milling/integration/tfg/macerate_sedimentary_carbonate",
        "greate:mixing/integration/tfg/dripstone_cobble_to_mossy_cobble",
        "greate:pressing/dripstone_cobble_to_gravel",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "gtceu:shaped/dispenser",
        "gtceu:shaped/dropper",
        "tfc:crafting/vanilla/redstone/piston",
        "tfg:create/shaped/redstone_contact",
        "tfg:create/shaped/sticker",
        "tfg:deploying/dripstone_cobble_to_mossy_cobble",
        "tfg:shaped/observer_certus_q",
        "tfg:shaped/observer_nether_q",
        "tfg:shaped/observer_quartzite",
        "tfg:shapeless/lever",
        "tfg:shapeless/unpacking_dripstone_cobble",
        "tfg:stonecutter/tfg_rock_cobble_dripstone_to_tfg_rock_cobble_dripstone_stairs",
        "tfg:stonecutting/tfg_rock_cobble_dripstone_to_tfg_rock_cobble_dripstone_slab",
        "tfg:stonecutting/tfg_rock_cobble_dripstone_to_tfg_rock_cobble_dripstone_wall"
      ],
      "recipe_output_examples": [
        "greate:pressing/dripstone_raw_to_cobble",
        "tfc:kjs/5ibw2bghfnarem5e4zvln0wz",
        "tfc:kjs/67rat9rttkmt6olc4o6j1yue2",
        "tfc:kjs/7neyaxyqonhr5l7tgeu02qb05",
        "tfc:kjs/bbcddsh1im3ysgda38l9sn4gz",
        "tfc:kjs/ex2dxrjjneooyf4dizoluwj68",
        "tfg:deploying/dripstone_mossy_cobble_to_cobble_knife",
        "tfg:deploying/dripstone_mossy_cobble_to_cobble_pumice",
        "tfg:shaped/packing_dripstone_cobble"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/cobble_dripstone",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_trapdoors_materials",
          "domum_ornamentum:fence_gate_materials",
          "domum_ornamentum:fence_materials",
          "domum_ornamentum:paper_wall_center",
          "domum_ornamentum:paper_wall_frame",
          "domum_ornamentum:pillar_materials",
          "domum_ornamentum:post_materials",
          "domum_ornamentum:shingles_cover",
          "domum_ornamentum:shingles_roof",
          "domum_ornamentum:shingles_support",
          "domum_ornamentum:slab_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "firmalife:oven_insulation",
          "forge:cobblestone",
          "minecraft:enderman_holdable",
          "minecraft:mineable/pickaxe",
          "tfc:bloomery_insulation",
          "tfc:can_landslide",
          "tfc:creeping_stone_plantable_on",
          "tfc:forge_insulation",
          "tfc:sedimentary_items",
          "tfc:toughness_2",
          "tfg:anemone_plantable_on"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Sedimentary"
        },
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
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "create:deploying",
            "greate:haunting",
            "greate:milling",
            "greate:mixing",
            "greate:pressing",
            "kubejs:shaped",
            "kubejs:shapeless",
            "stonecutting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/cobble_dripstone_slab",
      "namespace": "tfg",
      "display_name": "Travertine Cobble Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:slabs",
        "tfc:sedimentary_items",
        "tfg:interaction/cobble_slab",
        "tfg:rock_slabs",
        "tfg:stone_composition/sedimentary_carbonate_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 1,
        "tfc:chisel": 1,
        "tfc:collapse": 2
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_carbonate_half",
        "minecraft:kjs/tfg_loose_dripstone_2"
      ],
      "recipe_output_examples": [
        "tfc:kjs/1mxgge9fycrg5p9ezylt5ztg4",
        "tfc:kjs/33ol66firs7mkq2w47fqcrs1u",
        "tfg:chisel/tfg_rock_cobble_dripstone_to_tfg_rock_cobble_dripstone_slab",
        "tfg:stonecutting/tfg_rock_cobble_dripstone_to_tfg_rock_cobble_dripstone_slab"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/cobble_dripstone_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs",
          "tfc:sedimentary_items"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Can be placed vertically"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sedimentary"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaGreg"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
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
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:slabs"
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "tfg:rock/cobble_dripstone_stairs",
      "namespace": "tfg",
      "display_name": "Travertine Cobble Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:stairs",
        "tfc:sedimentary_items",
        "tfg:interaction/cobble_stairs",
        "tfg:rock_stairs",
        "tfg:stone_composition/sedimentary_carbonate"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 1,
        "tfc:chisel": 1,
        "tfc:collapse": 2
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_carbonate",
        "minecraft:kjs/tfg_loose_dripstone"
      ],
      "recipe_output_examples": [
        "tfc:kjs/3mye6rgp3x0nk6pngm82bvdi7",
        "tfc:kjs/9oypuyeeh783cawp65xbhjdts",
        "tfg:chisel/tfg_rock_cobble_dripstone_to_tfg_rock_cobble_dripstone_stairs",
        "tfg:stonecutter/tfg_rock_cobble_dripstone_to_tfg_rock_cobble_dripstone_stairs"
      ],
      "model_parents": [],
      "creative_tabs": [
        "kubejs:tab"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfg:rock/cobble_dripstone_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs",
          "tfc:sedimentary_items"
        ],
        "requires_correct_tool": true
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
          "text": "Sedimentary"
        },
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
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
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