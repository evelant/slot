# Items to classify
{
  "items": [
    {
      "id": "minecraft:sticky_piston",
      "namespace": "minecraft",
      "display_name": "Sticky Piston",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:sticky_piston"
      ],
      "model_parents": [
        "block/sticky_piston_inventory",
        "block/cube_bottom_top",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/sticky_piston"
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
      "id": "minecraft:stone",
      "namespace": "minecraft",
      "display_name": "Stone",
      "minecraft_tags_direct": [
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
        "stonecutting": 11,
        "crafting_shaped": 7,
        "smelting": 1,
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "smelting": 1
      },
      "sample_ingredient_of": [
        "minecraft:chiseled_stone_bricks_from_stone_stonecutting",
        "minecraft:cobblestone_from_stone_stonecutting",
        "minecraft:cobblestone_slab_from_stone_stonecutting",
        "minecraft:cobblestone_stairs_from_stone_stonecutting",
        "minecraft:cobblestone_wall_from_stone_stonecutting",
        "minecraft:comparator",
        "minecraft:repeater",
        "minecraft:smooth_stone",
        "minecraft:stone_brick_slab_from_stone_stonecutting",
        "minecraft:stone_brick_stairs_from_stone_stonecutting"
      ],
      "sample_output_of": [
        "minecraft:stone"
      ],
      "model_parents": [
        "block/stone",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/infested_stone",
        "minecraft:blocks/stone",
        "minecraft:chests/village/village_mason"
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
          "source": "rule:material_family_from_id",
          "rationale": "exact id minecraft:stone"
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
        },
        "origin": {
          "values": [
            "village"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:stone_axe",
      "namespace": "minecraft",
      "display_name": "Stone Axe",
      "minecraft_tags_direct": [
        "minecraft:axes"
      ],
      "minecraft_tags_inherited": [
        "minecraft:breaks_decorated_pots",
        "minecraft:enchantable/durability",
        "minecraft:enchantable/mining",
        "minecraft:enchantable/mining_loot",
        "minecraft:enchantable/sharp_weapon",
        "minecraft:enchantable/vanishing",
        "minecraft:enchantable/weapon"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:stone_axe"
      ],
      "model_parents": [
        "item/stone_axe",
        "item/handheld",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/igloo_chest",
        "minecraft:chests/spawn_bonus_chest",
        "minecraft:chests/trial_chambers/corridor",
        "minecraft:chests/underwater_ruin_small",
        "minecraft:gameplay/hero_of_the_village/toolsmith_gift",
        "minecraft:gameplay/hero_of_the_village/weaponsmith_gift"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:weapon": {
          "disable_blocking_for_seconds": 5,
          "item_damage_per_attack": 2
        },
        "minecraft:tool": {
          "rules": [
            {
              "blocks": "#minecraft:incorrect_for_stone_tool",
              "correct_for_drops": false
            },
            {
              "blocks": "#minecraft:mineable/axe",
              "correct_for_drops": true,
              "speed": 4
            }
          ]
        },
        "minecraft:repairable": {
          "items": "#minecraft:stone_tool_materials"
        },
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "minecraft",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "has_durability": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_durability_from_component"
        },
        "has_enchantments": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_enchantments_from_component"
        },
        "material_family": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:material_family_from_tool_prefix",
          "rationale": "stone_axe"
        },
        "form": {
          "value": "tool",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _axe"
        },
        "origin": {
          "values": [
            "overworld_ocean",
            "overworld_surface",
            "trial_chamber",
            "village"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        },
        "tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:tier_from_tool_prefix",
          "rationale": "stone_axe"
        }
      }
    },
    {
      "id": "minecraft:stone_brick_slab",
      "namespace": "minecraft",
      "display_name": "Stone Brick Slab",
      "minecraft_tags_direct": [
        "minecraft:slabs"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "stonecutting": 2
      },
      "sample_ingredient_of": [
        "minecraft:chiseled_stone_bricks"
      ],
      "sample_output_of": [
        "minecraft:stone_brick_slab",
        "minecraft:stone_brick_slab_from_stone_bricks_stonecutting",
        "minecraft:stone_brick_slab_from_stone_stonecutting"
      ],
      "model_parents": [
        "block/stone_brick_slab",
        "block/slab",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/stone_brick_slab"
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
      "id": "minecraft:stone_brick_stairs",
      "namespace": "minecraft",
      "display_name": "Stone Brick Stairs",
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
        "minecraft:stone_brick_stairs",
        "minecraft:stone_brick_stairs_from_stone_bricks_stonecutting",
        "minecraft:stone_brick_stairs_from_stone_stonecutting"
      ],
      "model_parents": [
        "block/stone_brick_stairs",
        "block/stairs",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/stone_brick_stairs"
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
    }
  ]
}
Respond with a single JSON object matching the expected output shape above. No other text.