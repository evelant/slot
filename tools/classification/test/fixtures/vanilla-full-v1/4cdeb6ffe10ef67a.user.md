# Items to classify
{
  "items": [
    {
      "id": "minecraft:diamond_block",
      "namespace": "minecraft",
      "display_name": "Block of Diamond",
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
        "minecraft:diamond"
      ],
      "sample_output_of": [
        "minecraft:diamond_block"
      ],
      "model_parents": [
        "block/diamond_block",
        "block/cube_all",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/diamond_block",
        "minecraft:chests/trial_chambers/intersection",
        "minecraft:chests/trial_chambers/reward_ominous_rare",
        "minecraft:pots/trial_chambers/corridor"
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
          "value": "diamond",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "exact id minecraft:diamond_block"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "required_tool_tier": {
          "value": "iron",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_iron_tool"
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
            "trial_chamber"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:diamond_boots",
      "namespace": "minecraft",
      "display_name": "Diamond Boots",
      "minecraft_tags_direct": [
        "minecraft:foot_armor"
      ],
      "minecraft_tags_inherited": [
        "minecraft:enchantable/armor",
        "minecraft:enchantable/durability",
        "minecraft:enchantable/equippable",
        "minecraft:enchantable/foot_armor",
        "minecraft:enchantable/vanishing",
        "minecraft:trimmable_armor"
      ],
      "processing_in": [
        "smithing"
      ],
      "recipe_consumption_by_type": {
        "smithing_trim": 18,
        "smithing_transform": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:bolt_armor_trim_smithing_template_smithing_trim",
        "minecraft:coast_armor_trim_smithing_template_smithing_trim",
        "minecraft:dune_armor_trim_smithing_template_smithing_trim",
        "minecraft:eye_armor_trim_smithing_template_smithing_trim",
        "minecraft:flow_armor_trim_smithing_template_smithing_trim",
        "minecraft:host_armor_trim_smithing_template_smithing_trim",
        "minecraft:netherite_boots_smithing",
        "minecraft:raiser_armor_trim_smithing_template_smithing_trim",
        "minecraft:rib_armor_trim_smithing_template_smithing_trim",
        "minecraft:sentry_armor_trim_smithing_template_smithing_trim"
      ],
      "sample_output_of": [
        "minecraft:diamond_boots"
      ],
      "model_parents": [
        "item/diamond_boots_quartz_trim",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/bastion_treasure",
        "minecraft:chests/end_city_treasure"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:repairable": {
          "items": "#minecraft:repairs_diamond_armor"
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
        "equip_slot": {
          "value": "feet",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        },
        "material_family": {
          "value": "diamond",
          "confidence": 1,
          "source": "rule:material_family_from_tool_prefix",
          "rationale": "diamond_boots"
        },
        "form": {
          "value": "armor_piece",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _boots"
        },
        "processing_in": {
          "values": [
            "smithing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "bastion",
            "end_city"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        },
        "tier": {
          "value": "diamond",
          "confidence": 1,
          "source": "rule:tier_from_tool_prefix",
          "rationale": "diamond_boots"
        }
      }
    },
    {
      "id": "minecraft:diamond_chestplate",
      "namespace": "minecraft",
      "display_name": "Diamond Chestplate",
      "minecraft_tags_direct": [
        "minecraft:chest_armor"
      ],
      "minecraft_tags_inherited": [
        "minecraft:enchantable/armor",
        "minecraft:enchantable/chest_armor",
        "minecraft:enchantable/durability",
        "minecraft:enchantable/equippable",
        "minecraft:enchantable/vanishing",
        "minecraft:trimmable_armor"
      ],
      "processing_in": [
        "smithing"
      ],
      "recipe_consumption_by_type": {
        "smithing_trim": 18,
        "smithing_transform": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:bolt_armor_trim_smithing_template_smithing_trim",
        "minecraft:coast_armor_trim_smithing_template_smithing_trim",
        "minecraft:dune_armor_trim_smithing_template_smithing_trim",
        "minecraft:eye_armor_trim_smithing_template_smithing_trim",
        "minecraft:flow_armor_trim_smithing_template_smithing_trim",
        "minecraft:host_armor_trim_smithing_template_smithing_trim",
        "minecraft:netherite_chestplate_smithing",
        "minecraft:raiser_armor_trim_smithing_template_smithing_trim",
        "minecraft:rib_armor_trim_smithing_template_smithing_trim",
        "minecraft:sentry_armor_trim_smithing_template_smithing_trim"
      ],
      "sample_output_of": [
        "minecraft:diamond_chestplate"
      ],
      "model_parents": [
        "item/diamond_chestplate_quartz_trim",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/bastion_treasure",
        "minecraft:chests/end_city_treasure",
        "minecraft:chests/trial_chambers/reward_ominous_rare",
        "minecraft:chests/trial_chambers/reward_rare",
        "minecraft:chests/woodland_mansion"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:repairable": {
          "items": "#minecraft:repairs_diamond_armor"
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
        "equip_slot": {
          "value": "chest",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        },
        "material_family": {
          "value": "diamond",
          "confidence": 1,
          "source": "rule:material_family_from_tool_prefix",
          "rationale": "diamond_chestplate"
        },
        "form": {
          "value": "armor_piece",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _chestplate"
        },
        "processing_in": {
          "values": [
            "smithing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "bastion",
            "end_city",
            "trial_chamber",
            "woodland_mansion"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        },
        "tier": {
          "value": "diamond",
          "confidence": 1,
          "source": "rule:tier_from_tool_prefix",
          "rationale": "diamond_chestplate"
        }
      }
    },
    {
      "id": "minecraft:diamond_helmet",
      "namespace": "minecraft",
      "display_name": "Diamond Helmet",
      "minecraft_tags_direct": [
        "minecraft:head_armor"
      ],
      "minecraft_tags_inherited": [
        "minecraft:enchantable/armor",
        "minecraft:enchantable/durability",
        "minecraft:enchantable/equippable",
        "minecraft:enchantable/head_armor",
        "minecraft:enchantable/vanishing",
        "minecraft:trimmable_armor"
      ],
      "processing_in": [
        "smithing"
      ],
      "recipe_consumption_by_type": {
        "smithing_trim": 18,
        "smithing_transform": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:bolt_armor_trim_smithing_template_smithing_trim",
        "minecraft:coast_armor_trim_smithing_template_smithing_trim",
        "minecraft:dune_armor_trim_smithing_template_smithing_trim",
        "minecraft:eye_armor_trim_smithing_template_smithing_trim",
        "minecraft:flow_armor_trim_smithing_template_smithing_trim",
        "minecraft:host_armor_trim_smithing_template_smithing_trim",
        "minecraft:netherite_helmet_smithing",
        "minecraft:raiser_armor_trim_smithing_template_smithing_trim",
        "minecraft:rib_armor_trim_smithing_template_smithing_trim",
        "minecraft:sentry_armor_trim_smithing_template_smithing_trim"
      ],
      "sample_output_of": [
        "minecraft:diamond_helmet"
      ],
      "model_parents": [
        "item/diamond_helmet_quartz_trim",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/bastion_treasure",
        "minecraft:chests/end_city_treasure"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:repairable": {
          "items": "#minecraft:repairs_diamond_armor"
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
        "equip_slot": {
          "value": "head",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        },
        "material_family": {
          "value": "diamond",
          "confidence": 1,
          "source": "rule:material_family_from_tool_prefix",
          "rationale": "diamond_helmet"
        },
        "form": {
          "value": "armor_piece",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _helmet"
        },
        "processing_in": {
          "values": [
            "smithing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "bastion",
            "end_city"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        },
        "tier": {
          "value": "diamond",
          "confidence": 1,
          "source": "rule:tier_from_tool_prefix",
          "rationale": "diamond_helmet"
        }
      }
    },
    {
      "id": "minecraft:diamond_hoe",
      "namespace": "minecraft",
      "display_name": "Diamond Hoe",
      "minecraft_tags_direct": [
        "minecraft:hoes"
      ],
      "minecraft_tags_inherited": [
        "minecraft:breaks_decorated_pots",
        "minecraft:enchantable/durability",
        "minecraft:enchantable/mining",
        "minecraft:enchantable/mining_loot",
        "minecraft:enchantable/vanishing"
      ],
      "processing_in": [
        "smithing"
      ],
      "recipe_consumption_by_type": {
        "smithing_transform": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [
        "minecraft:netherite_hoe_smithing"
      ],
      "sample_output_of": [
        "minecraft:diamond_hoe"
      ],
      "model_parents": [
        "item/diamond_hoe",
        "item/handheld",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/ancient_city",
        "minecraft:chests/woodland_mansion"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:weapon": {
          "item_damage_per_attack": 2
        },
        "minecraft:tool": {
          "rules": [
            {
              "blocks": "#minecraft:incorrect_for_diamond_tool",
              "correct_for_drops": false
            },
            {
              "blocks": "#minecraft:mineable/hoe",
              "correct_for_drops": true,
              "speed": 8
            }
          ]
        },
        "minecraft:repairable": {
          "items": "#minecraft:diamond_tool_materials"
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
          "value": "diamond",
          "confidence": 1,
          "source": "rule:material_family_from_tool_prefix",
          "rationale": "diamond_hoe"
        },
        "form": {
          "value": "tool",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _hoe"
        },
        "processing_in": {
          "values": [
            "smithing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "origin": {
          "values": [
            "ancient_city",
            "woodland_mansion"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        },
        "tier": {
          "value": "diamond",
          "confidence": 1,
          "source": "rule:tier_from_tool_prefix",
          "rationale": "diamond_hoe"
        }
      }
    }
  ]
}
Respond with a single JSON object matching the expected output shape above. No other text.