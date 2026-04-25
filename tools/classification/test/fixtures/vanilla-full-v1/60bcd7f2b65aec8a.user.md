# Items to classify
{
  "items": [
    {
      "id": "minecraft:stone_sword",
      "namespace": "minecraft",
      "display_name": "Stone Sword",
      "minecraft_tags_direct": [
        "minecraft:swords"
      ],
      "minecraft_tags_inherited": [
        "minecraft:breaks_decorated_pots",
        "minecraft:enchantable/durability",
        "minecraft:enchantable/fire_aspect",
        "minecraft:enchantable/melee_weapon",
        "minecraft:enchantable/sharp_weapon",
        "minecraft:enchantable/sweeping",
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
        "minecraft:stone_sword"
      ],
      "model_parents": [
        "item/stone_sword",
        "item/handheld",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {
        "minecraft:weapon": {},
        "minecraft:tool": {
          "can_destroy_blocks_in_creative": false,
          "damage_per_block": 2,
          "rules": [
            {
              "blocks": "minecraft:cobweb",
              "correct_for_drops": true,
              "speed": 15
            },
            {
              "blocks": "#minecraft:sword_instantly_mines",
              "speed": 3.4028235e+38
            },
            {
              "blocks": "#minecraft:sword_efficient",
              "speed": 1.5
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
          "rationale": "stone_sword"
        },
        "form": {
          "value": "weapon",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _sword"
        },
        "tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:tier_from_tool_prefix",
          "rationale": "stone_sword"
        }
      }
    },
    {
      "id": "minecraft:stonecutter",
      "namespace": "minecraft",
      "display_name": "Stonecutter",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:stonecutter"
      ],
      "model_parents": [
        "block/stonecutter",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/stonecutter"
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
      "id": "minecraft:stray_spawn_egg",
      "namespace": "minecraft",
      "display_name": "Stray Spawn Egg",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "item/stray_spawn_egg",
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
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        }
      }
    },
    {
      "id": "minecraft:strider_spawn_egg",
      "namespace": "minecraft",
      "display_name": "Strider Spawn Egg",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "sample_ingredient_of": [],
      "sample_output_of": [],
      "model_parents": [
        "item/strider_spawn_egg",
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
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        }
      }
    },
    {
      "id": "minecraft:string",
      "namespace": "minecraft",
      "display_name": "String",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 9
      },
      "recipe_production_by_type": {},
      "sample_ingredient_of": [
        "minecraft:bow",
        "minecraft:bundle",
        "minecraft:candle",
        "minecraft:crossbow",
        "minecraft:fishing_rod",
        "minecraft:lead",
        "minecraft:loom",
        "minecraft:scaffolding",
        "minecraft:white_wool_from_string"
      ],
      "sample_output_of": [],
      "model_parents": [
        "item/string",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:archaeology/trail_ruins_common",
        "minecraft:blocks/cobweb",
        "minecraft:blocks/tripwire",
        "minecraft:chests/bastion_bridge",
        "minecraft:chests/bastion_hoglin_stable",
        "minecraft:chests/bastion_other",
        "minecraft:chests/desert_pyramid",
        "minecraft:chests/pillager_outpost",
        "minecraft:chests/simple_dungeon",
        "minecraft:chests/woodland_mansion"
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
            "archaeology_site",
            "bastion",
            "desert_temple",
            "fishing",
            "mob_drop",
            "overworld_cave",
            "pillager_outpost",
            "trading",
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