# Items to classify
{
  "items": [
    {
      "id": "minecraft:wooden_spear",
      "namespace": "minecraft",
      "display_name": "Wooden Spear",
      "minecraft_tags_direct": [
        "minecraft:spears"
      ],
      "minecraft_tags_inherited": [
        "minecraft:enchantable/durability",
        "minecraft:enchantable/fire_aspect",
        "minecraft:enchantable/lunge",
        "minecraft:enchantable/melee_weapon",
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
        "minecraft:wooden_spear"
      ],
      "model_parents": [
        "item/wooden_spear",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {
        "minecraft:weapon": {},
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
        "has_enchantments": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_enchantments_from_component"
        }
      }
    },
    {
      "id": "minecraft:wooden_sword",
      "namespace": "minecraft",
      "display_name": "Wooden Sword",
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
        "minecraft:wooden_sword"
      ],
      "model_parents": [
        "item/wooden_sword",
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
        "has_enchantments": {
          "value": true,
          "confidence": 1,
          "source": "rule:has_enchantments_from_component"
        },
        "material_family": {
          "value": "wood",
          "confidence": 1,
          "source": "rule:material_family_from_tool_prefix",
          "rationale": "wooden_sword"
        },
        "form": {
          "value": "weapon",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _sword"
        },
        "tier": {
          "value": "wooden",
          "confidence": 1,
          "source": "rule:tier_from_tool_prefix",
          "rationale": "wooden_sword"
        }
      }
    },
    {
      "id": "minecraft:writable_book",
      "namespace": "minecraft",
      "display_name": "Book and Quill",
      "minecraft_tags_direct": [
        "minecraft:book_cloning_target",
        "minecraft:bookshelf_books",
        "minecraft:lectern_books"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_special_bookcloning": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "sample_ingredient_of": [
        "minecraft:book_cloning"
      ],
      "sample_output_of": [
        "minecraft:writable_book"
      ],
      "model_parents": [
        "item/writable_book",
        "item/generated",
        "builtin/generated"
      ],
      "sample_loot_sources": [],
      "lore": [],
      "component_highlights": {
        "minecraft:writable_book_content": {},
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
    },
    {
      "id": "minecraft:written_book",
      "namespace": "minecraft",
      "display_name": "Written Book",
      "minecraft_tags_direct": [
        "minecraft:bookshelf_books",
        "minecraft:lectern_books"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_special_bookcloning": 1
      },
      "recipe_production_by_type": {
        "crafting_special_bookcloning": 1
      },
      "sample_ingredient_of": [
        "minecraft:book_cloning"
      ],
      "sample_output_of": [
        "minecraft:book_cloning"
      ],
      "model_parents": [
        "item/written_book",
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
      "id": "minecraft:yellow_banner",
      "namespace": "minecraft",
      "display_name": "Yellow Banner",
      "minecraft_tags_direct": [
        "minecraft:banners"
      ],
      "minecraft_tags_inherited": [],
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_special_shielddecoration": 1,
        "crafting_special_bannerduplicate": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_special_bannerduplicate": 1
      },
      "sample_ingredient_of": [
        "minecraft:shield_decoration",
        "minecraft:yellow_banner_duplicate"
      ],
      "sample_output_of": [
        "minecraft:yellow_banner",
        "minecraft:yellow_banner_duplicate"
      ],
      "model_parents": [
        "item/template_banner"
      ],
      "sample_loot_sources": [
        "minecraft:blocks/yellow_banner"
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
          "value": "yellow",
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
    }
  ]
}
Respond with a single JSON object matching the expected output shape above. No other text.