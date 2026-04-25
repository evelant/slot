# Items to classify
{
  "items": [
    {
      "id": "minecraft:cooked_mutton",
      "namespace": "minecraft",
      "display_name": "Cooked Mutton",
      "minecraft_tags_direct": [
        "minecraft:meat"
      ],
      "minecraft_tags_inherited": [
        "minecraft:wolf_food"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "smelting": 1,
        "campfire_cooking": 1,
        "smoking": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:cooked_mutton",
        "minecraft:cooked_mutton_from_campfire_cooking",
        "minecraft:cooked_mutton_from_smoking"
      ],
      "model_parents": [
        "item/cooked_mutton",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:gameplay/hero_of_the_village/butcher_gift"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:food": {
          "nutrition": 6,
          "saturation": 9.6
        },
        "minecraft:consumable": {},
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
      "id": "minecraft:cooked_porkchop",
      "namespace": "minecraft",
      "display_name": "Cooked Porkchop",
      "minecraft_tags_direct": [
        "minecraft:meat",
        "minecraft:piglin_food"
      ],
      "minecraft_tags_inherited": [
        "minecraft:wolf_food"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "smelting": 1,
        "campfire_cooking": 1,
        "smoking": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:cooked_porkchop",
        "minecraft:cooked_porkchop_from_campfire_cooking",
        "minecraft:cooked_porkchop_from_smoking"
      ],
      "model_parents": [
        "item/cooked_porkchop",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/bastion_hoglin_stable",
        "minecraft:chests/bastion_other",
        "minecraft:gameplay/hero_of_the_village/butcher_gift"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:food": {
          "nutrition": 8,
          "saturation": 12.8
        },
        "minecraft:consumable": {},
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
        "origin": {
          "values": [
            "bastion",
            "village"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:cooked_rabbit",
      "namespace": "minecraft",
      "display_name": "Cooked Rabbit",
      "minecraft_tags_direct": [
        "minecraft:meat"
      ],
      "minecraft_tags_inherited": [
        "minecraft:wolf_food"
      ],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 2
      },
      "recipe_production_by_type": {
        "smelting": 1,
        "campfire_cooking": 1,
        "smoking": 1
      },
      "sample_ingredient_of": [
        "minecraft:rabbit_stew_from_brown_mushroom",
        "minecraft:rabbit_stew_from_red_mushroom"
      ],
      "sample_output_of": [
        "minecraft:cooked_rabbit",
        "minecraft:cooked_rabbit_from_campfire_cooking",
        "minecraft:cooked_rabbit_from_smoking"
      ],
      "model_parents": [
        "item/cooked_rabbit",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:gameplay/hero_of_the_village/butcher_gift"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:food": {
          "nutrition": 5,
          "saturation": 6
        },
        "minecraft:consumable": {},
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
            "village"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:cooked_salmon",
      "namespace": "minecraft",
      "display_name": "Cooked Salmon",
      "minecraft_tags_direct": [
        "minecraft:fishes",
        "minecraft:wolf_food"
      ],
      "minecraft_tags_inherited": [
        "minecraft:nautilus_food"
      ],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "smelting": 1,
        "campfire_cooking": 1,
        "smoking": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:cooked_salmon",
        "minecraft:cooked_salmon_from_campfire_cooking",
        "minecraft:cooked_salmon_from_smoking"
      ],
      "model_parents": [
        "item/cooked_salmon",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:chests/buried_treasure"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:food": {
          "nutrition": 6,
          "saturation": 9.6
        },
        "minecraft:consumable": {},
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
        "origin": {
          "values": [
            "overworld_ocean"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:origin_from_loot_tables"
        }
      }
    },
    {
      "id": "minecraft:cookie",
      "namespace": "minecraft",
      "display_name": "Cookie",
      "minecraft_tags_direct": [
        "minecraft:parrot_poisonous_food"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "sample_ingredient_of": [],
      "sample_output_of": [
        "minecraft:cookie"
      ],
      "model_parents": [
        "item/cookie",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [
        "minecraft:gameplay/hero_of_the_village/farmer_gift"
      ],
      "lore": [],
      "component_highlights": {
        "minecraft:food": {
          "nutrition": 2,
          "saturation": 0.4
        },
        "minecraft:consumable": {},
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
        "origin": {
          "values": [
            "village"
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