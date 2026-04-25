# Items to classify
{
  "items": [
    {
      "id": "minecraft:shield",
      "namespace": "minecraft",
      "display_name": "Shield",
      "minecraft_tags_direct": [
        "minecraft:enchantable/durability"
      ],
      "minecraft_tags_inherited": [
        "minecraft:enchantable/vanishing"
      ],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_special_shielddecoration": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_special_shielddecoration": 1
      },
      "sample_ingredient_of": [
        "minecraft:shield_decoration"
      ],
      "sample_output_of": [
        "minecraft:shield",
        "minecraft:shield_decoration"
      ],
      "model_parents": [
        "item/shield_blocking"
      ],
      "sample_loot_sources": [
        "minecraft:chests/trial_chambers/reward_rare"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:banner_patterns": [],
        "minecraft:repairable": {
          "items": "#minecraft:wooden_tool_materials"
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
        "has_nbt_variation": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_nbt_variation_from_component"
        },
        "equip_slot": {
          "value": "off_hand",
          "confidence": 1,
          "source": "rule:equip_slot_from_component"
        },
        "form": {
          "value": "tool",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "exact id"
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
      "id": "minecraft:shulker_box",
      "namespace": "minecraft",
      "display_name": "Shulker Box",
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
        "crafting_shaped": 1
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
        "minecraft:shulker_box"
      ],
      "model_parents": [
        "item/shulker_box",
        "item/template_shulker_box"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/shulker_box"
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
        }
      }
    },
    {
      "id": "minecraft:ender_chest",
      "namespace": "minecraft",
      "display_name": "Ender Chest",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:ender_chest"
      ],
      "model_parents": [
        "item/ender_chest",
        "item/template_chest"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/ender_chest"
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
          "value": "storage_block",
          "confidence": 1,
          "source": "rule:form_from_model",
          "rationale": "model item/template_chest"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        }
      }
    },
    {
      "id": "minecraft:bundle",
      "namespace": "minecraft",
      "display_name": "Bundle",
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
        "crafting_shaped": 1
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
        "minecraft:bundle"
      ],
      "model_parents": [
        "item/bundle_open_back",
        "item/template_bundle_open_back",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/village/village_cartographer",
        "minecraft:chests/village/village_desert_house",
        "minecraft:chests/village/village_plains_house",
        "minecraft:chests/village/village_savanna_house",
        "minecraft:chests/village/village_snowy_house",
        "minecraft:chests/village/village_taiga_house",
        "minecraft:chests/village/village_tannery",
        "minecraft:chests/village/village_weaponsmith"
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
      "id": "minecraft:barrel",
      "namespace": "minecraft",
      "display_name": "Barrel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:barrel"
      ],
      "model_parents": [
        "block/barrel",
        "block/cube_bottom_top",
        "block/cube",
        "block/block"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/barrel",
        "minecraft:chests/village/village_fisher"
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
        "has_nbt_variation": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_nbt_variation_from_component"
        },
        "required_tool": {
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        },
        "origin": {
          "values": [
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