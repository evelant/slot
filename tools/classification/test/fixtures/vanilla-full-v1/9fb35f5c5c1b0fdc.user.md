# Items to classify
{
  "items": [
    {
      "id": "minecraft:bolt_armor_trim_smithing_template",
      "namespace": "minecraft",
      "display_name": "Smithing Template",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting",
        "smithing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "smithing_trim": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:bolt_armor_trim_smithing_template",
        "minecraft:bolt_armor_trim_smithing_template_smithing_trim"
      ],
      "sample_output_of": [
        "minecraft:bolt_armor_trim_smithing_template"
      ],
      "model_parents": [
        "item/bolt_armor_trim_smithing_template",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/trial_chambers/reward_unique"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:rarity": "uncommon"
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
        "rarity": {
          "value": "uncommon",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = uncommon"
        },
        "processing_in": {
          "values": [
            "crafting",
            "smithing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "trial_chamber"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:bone",
      "namespace": "minecraft",
      "display_name": "Bone",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "sample_ingredient_of": [
        "minecraft:bone_meal"
      ],
      "sample_output_of": [],
      "model_parents": [
        "item/bone",
        "item/handheld",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/ancient_city",
        "minecraft:chests/desert_pyramid",
        "minecraft:chests/jungle_temple",
        "minecraft:chests/simple_dungeon",
        "minecraft:chests/woodland_mansion",
        "minecraft:entities/bogged",
        "minecraft:entities/parched",
        "minecraft:entities/skeleton",
        "minecraft:entities/skeleton_horse",
        "minecraft:entities/stray"
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
        "material_family": {
          "value": "bone",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "exact id minecraft:bone"
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
            "ancient_city",
            "desert_temple",
            "fishing",
            "jungle_temple",
            "mob_drop",
            "overworld_cave",
            "woodland_mansion"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:bone_block",
      "namespace": "minecraft",
      "display_name": "Bone Block",
      "minecraft_tags_direct": [
        "minecraft:sulfur_cube_archetype/regular"
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
        "minecraft:bone_meal_from_bone_block"
      ],
      "sample_output_of": [
        "minecraft:bone_block"
      ],
      "model_parents": [
        "block/bone_block",
        "block/cube_column",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/bone_block",
        "minecraft:chests/bastion_other"
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
          "value": "bone",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "exact id minecraft:bone_block"
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
            "bastion",
            "nether"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        },
        "y_level_range": {
          "value": "nether_surface",
          "confidence": 1,
          "source": "rule:y_level_range_from_id",
          "rationale": "id pattern"
        }
      }
    },
    {
      "id": "minecraft:bone_meal",
      "namespace": "minecraft",
      "display_name": "Bone Meal",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 2
      },
      "sample_ingredient_of": [
        "minecraft:bone_block",
        "minecraft:white_dye"
      ],
      "sample_output_of": [
        "minecraft:bone_meal",
        "minecraft:bone_meal_from_bone_block"
      ],
      "model_parents": [
        "item/bone_meal",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/composter",
        "minecraft:chests/trial_chambers/supply",
        "minecraft:entities/cod",
        "minecraft:entities/pufferfish",
        "minecraft:entities/salmon",
        "minecraft:entities/tropical_fish"
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
        "material_family": {
          "value": "bone",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "exact id minecraft:bone_meal"
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
            "trial_chamber"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:book",
      "namespace": "minecraft",
      "display_name": "Book",
      "minecraft_tags_direct": [
        "minecraft:bookshelf_books"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "sample_ingredient_of": [
        "minecraft:bookshelf",
        "minecraft:enchanting_table",
        "minecraft:writable_book"
      ],
      "sample_output_of": [
        "minecraft:book"
      ],
      "model_parents": [
        "item/book",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/bookshelf",
        "minecraft:chests/abandoned_mineshaft",
        "minecraft:chests/ancient_city",
        "minecraft:chests/bastion_other",
        "minecraft:chests/desert_pyramid",
        "minecraft:chests/jungle_temple",
        "minecraft:chests/pillager_outpost",
        "minecraft:chests/shipwreck_map",
        "minecraft:chests/simple_dungeon",
        "minecraft:chests/stronghold_corridor"
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
        "has_enchantments": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_enchantments_from_component"
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
            "ancient_city",
            "bastion",
            "desert_temple",
            "fishing",
            "jungle_temple",
            "mineshaft",
            "overworld_cave",
            "overworld_ocean",
            "pillager_outpost",
            "stronghold",
            "trading",
            "trial_chamber",
            "village",
            "woodland_mansion"
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