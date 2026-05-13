# Items to classify
{
  "items": [
    {
      "id": "sns:bound_leather_strip",
      "namespace": "sns",
      "display_name": "Bound Leather Strip",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "tfc:advanced_shaped_crafting",
        "tfc:damage_inputs_shaped_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "tfc:advanced_shaped_crafting": 5,
        "tfc:damage_inputs_shaped_crafting": 1
      },
      "recipe_production_by_type": {
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 7,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "sns:crafting/black_steel_toe_hiking_boots",
        "sns:crafting/blue_steel_toe_hiking_boots",
        "sns:crafting/frame_pack",
        "sns:crafting/hiking_boots",
        "sns:crafting/red_steel_toe_hiking_boots",
        "sns:crafting/steel_toe_hiking_boots",
        "tfg:sophisticated_backpacks/shaped/backpack"
      ],
      "recipe_output_examples": [
        "sns:crafting/bound_leather_strip"
      ],
      "model_parents": [
        "item/bound_leather_strip",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
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
            "crafting",
            "tfc:advanced_shaped_crafting",
            "tfc:damage_inputs_shaped_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "sns:buckle",
      "namespace": "sns",
      "display_name": "Buckle",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "tfc:advanced_shaped_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "tfc:advanced_shaped_crafting": 5
      },
      "recipe_production_by_type": {
        "tfc:anvil": 2,
        "vintageimprovements:curving": 2
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "sns:crafting/black_steel_toe_hiking_boots",
        "sns:crafting/blue_steel_toe_hiking_boots",
        "sns:crafting/hiking_boots",
        "sns:crafting/red_steel_toe_hiking_boots",
        "sns:crafting/steel_toe_hiking_boots",
        "tfg:toolbelt/shaped/belt_2"
      ],
      "recipe_output_examples": [
        "sns:anvil/buckle",
        "sns:anvil/buckle2",
        "sns:vi/curving/buckle",
        "sns:vi/curving/buckle2"
      ],
      "model_parents": [
        "item/buckle",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
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
            "crafting",
            "tfc:advanced_shaped_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "sns:burlap_sack",
      "namespace": "sns",
      "display_name": "Burlap Sack",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "curios:belt",
        "sns:prevented_in_burlap_sack",
        "sns:prevented_in_frame_pack",
        "sns:prevented_in_item_containers",
        "sns:prevented_in_leather_sack",
        "sns:prevented_in_lunchbox",
        "sns:prevented_in_ore_sack",
        "sns:prevented_in_quiver",
        "sns:prevented_in_seed_pouch",
        "sns:prevented_in_straw_basket",
        "tfc:usable_on_tool_rack",
        "tfg:cannot_launch_in_railgun"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:sewing": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "sns:sewing/burlap_sack"
      ],
      "model_parents": [
        "item/burlap_sack",
        "item/default"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Belt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold (Shift) for container info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "document_context": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/sns/burlap_sack",
          "label": "The Burlap Sack",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "guide-page",
              "key": "name",
              "text": "The Burlap Sack"
            },
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "The Burlap Sack is a type of Item Container with moderately more storage than the Leather Sack."
            },
            {
              "source": "guide-page",
              "key": "pages.1.text",
              "text": "By default these can hold up to Small Items"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "sns:frame_pack",
      "namespace": "sns",
      "display_name": "Frame Pack",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "curios:back",
        "sns:prevented_in_burlap_sack",
        "sns:prevented_in_frame_pack",
        "sns:prevented_in_item_containers",
        "sns:prevented_in_leather_sack",
        "sns:prevented_in_lunchbox",
        "sns:prevented_in_ore_sack",
        "sns:prevented_in_quiver",
        "sns:prevented_in_seed_pouch",
        "sns:prevented_in_straw_basket",
        "tfc:usable_on_tool_rack",
        "tfg:cannot_launch_in_railgun"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:damage_inputs_shaped_crafting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "sns:crafting/frame_pack"
      ],
      "model_parents": [
        "item/frame_pack",
        "item/default"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold (Shift) for container info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "sns:tfc/story/frame_pack",
          "label": "Un-Sophisticated Backpack",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Un-Sophisticated Backpack"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Use steel to create a Frame Pack. How Sophisticated?"
            }
          ]
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/sns/frame_pack",
          "label": "The Frame Pack",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "guide-page",
              "key": "name",
              "text": "The Frame Pack"
            },
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "The Frame Pack is a type of Item Container, essentially a TFC chest on your back. Like the TFC chest it respects TFC's Size system."
            },
            {
              "source": "guide-page",
              "key": "pages.1.text",
              "text": "By default these can hold up to Large Items"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "rarity": {
          "value": "uncommon",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = uncommon"
        }
      }
    },
    {
      "id": "sns:hiking_boots",
      "namespace": "sns",
      "display_name": "Hiking Boots",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:freeze_immune_wearables"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 4
      },
      "recipe_production_by_type": {
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "sns:shaped/hiking_boots_to_black_steel_toe_hiking_boots",
        "sns:shaped/hiking_boots_to_blue_steel_toe_hiking_boots",
        "sns:shaped/hiking_boots_to_red_steel_toe_hiking_boots",
        "sns:shaped/hiking_boots_to_steel_toe_hiking_boots"
      ],
      "recipe_output_examples": [
        "sns:crafting/hiking_boots"
      ],
      "model_parents": [
        "item/hiking_boots",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Prevents tall grass slowdown"
        },
        {
          "source": "runtime-tooltip",
          "text": "When on Feet:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+1 Armor"
        },
        {
          "source": "runtime-tooltip",
          "text": "+5% Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "+0.5 Fall Distance"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Resistances: §fSlashing§r 2%, §fPiercing§r 4%, §fCrushing§r 0%"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "sns:tfc/story/hiking_boots",
          "label": "These boots were made for walking",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "These boots were made for walking"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Make some high quality boots helping you explore on foot"
            }
          ]
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/sns/hiking_boots",
          "label": "Hiking Boots",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "guide-page",
              "key": "name",
              "text": "Hiking Boots"
            },
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "Every explorer needs a good pair of boots. These enable you to quickly push through shrubbery and give a minor speed boost in addition to a minor increase in safe fall height"
            },
            {
              "source": "guide-page",
              "key": "pages.1.text",
              "text": "These boots were made for walking"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 71,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "feet"
        },
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
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
        "form": {
          "value": "armor_piece",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _boots"
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
      "id": "sns:leather_sack",
      "namespace": "sns",
      "display_name": "Leather Sack",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "curios:belt",
        "sns:prevented_in_burlap_sack",
        "sns:prevented_in_frame_pack",
        "sns:prevented_in_item_containers",
        "sns:prevented_in_leather_sack",
        "sns:prevented_in_lunchbox",
        "sns:prevented_in_ore_sack",
        "sns:prevented_in_quiver",
        "sns:prevented_in_seed_pouch",
        "sns:prevented_in_straw_basket",
        "tfc:usable_on_tool_rack",
        "tfg:cannot_launch_in_railgun"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:damage_inputs_shaped_crafting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "sns:crafting/leather_sack"
      ],
      "model_parents": [
        "item/leather_sack",
        "item/default"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Belt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold (Shift) for container info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "sns:tfc/story/leather_sack",
          "label": "Leather Sack",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Leather Sack"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Make a Leather Sack out of the finest of leather"
            }
          ]
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/sns/leather_sack",
          "label": "The Leather Sack",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "guide-page",
              "key": "name",
              "text": "The Leather Sack"
            },
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "The Leather Sack is a type of Item Container and is like a vessel with slightly more storage. By default these can hold up to Normal Items"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "sns:leather_strip",
      "namespace": "sns",
      "display_name": "Leather Strip",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "tfc:damage_inputs_shaped_crafting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 6,
        "tfc:damage_inputs_shaped_crafting": 1,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "greate:cutting": 1,
        "tfc:knapping": 1
      },
      "recipe_ingredient_count": 8,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "sns:crafting/bound_leather_strip",
        "sns:crafting/leather_sack",
        "tfg:create/shaped/goggles",
        "tfg:shaped/flippers_leather",
        "tfg:shaped/snorkel",
        "tfg:shaped/snowshoes",
        "tfg:toolbelt/shaped/belt_2",
        "tfg:toolbelt/shaped/pouch"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/tfg/sns_leather_strip_cut",
        "sns:leather_knapping/leather_strip"
      ],
      "model_parents": [
        "item/leather_strip",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
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
            "crafting",
            "tfc:damage_inputs_shaped_crafting",
            "tfc:damage_inputs_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "sns:lunchbox",
      "namespace": "sns",
      "display_name": "Lunchbox",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [
        "item/lunchbox",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold (Shift) for container info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "sns:tfc/story/lunchbox",
          "label": "Food on the go",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Food on the go"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Forge a Lunchbox"
            }
          ]
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/sns/lunchbox",
          "label": "The Lunchbox",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "guide-page",
              "key": "name",
              "text": "The Lunchbox"
            },
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "The Lunchbox is one of the more interesting Item Containers. As you might expect from the name these hold food items and even allow you to consume food out of them. and to open the lunchbox, and scroll wheel to select the consumed stack and to eat the selected stack"
            },
            {
              "source": "guide-page",
              "key": "pages.1.text",
              "text": "For modpack authors or simply people wishing to add extra foods see the item tag sns:lunchbox_food most addons should be automatically supported"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "sns:metal/horseshoe/black_steel",
      "namespace": "sns",
      "display_name": "Black Steel Horseshoe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "createaddition:rolling": 1,
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "sns:crafting/metal/horseshoes/black_steel"
      ],
      "recipe_output_examples": [
        "sns:anvil/metal/horseshoe/black_steel",
        "tfg:rolling/black_steel_horseshoe"
      ],
      "model_parents": [
        "item/metal/horseshoe/black_steel",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "rare"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "rarity": {
          "value": "rare",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = rare"
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
      "id": "sns:metal/horseshoe/blue_steel",
      "namespace": "sns",
      "display_name": "Blue Steel Horseshoe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "createaddition:rolling": 1,
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "sns:crafting/metal/horseshoes/blue_steel"
      ],
      "recipe_output_examples": [
        "sns:anvil/metal/horseshoe/blue_steel",
        "tfg:rolling/blue_steel_horseshoe"
      ],
      "model_parents": [
        "item/metal/horseshoe/blue_steel",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "epic"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "rarity": {
          "value": "unique",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = epic"
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
      "id": "sns:metal/horseshoe/red_steel",
      "namespace": "sns",
      "display_name": "Red Steel Horseshoe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "createaddition:rolling": 1,
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "sns:crafting/metal/horseshoes/red_steel"
      ],
      "recipe_output_examples": [
        "sns:anvil/metal/horseshoe/red_steel",
        "tfg:rolling/red_steel_horseshoe"
      ],
      "model_parents": [
        "item/metal/horseshoe/red_steel",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "epic"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "rarity": {
          "value": "unique",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = epic"
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
      "id": "sns:metal/horseshoe/steel",
      "namespace": "sns",
      "display_name": "Steel Horseshoe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {
        "createaddition:rolling": 1,
        "tfc:anvil": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "sns:crafting/metal/horseshoes/steel"
      ],
      "recipe_output_examples": [
        "sns:anvil/metal/horseshoe/steel",
        "tfg:rolling/steel_horseshoe"
      ],
      "model_parents": [
        "item/metal/horseshoe/steel",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "document_context": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/sns/horseshoes",
          "label": "Horseshoes",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "Horseshoes are smithed individually and then combined together in the crafting grid. What you didn't know Horses had 4 feet? By the way it's a really good idea to tie up your horse with the higher tier horseshoes, the typical safety pit won't work!"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
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
            "crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "sns:metal/horseshoes/black_steel",
      "namespace": "sns",
      "display_name": "Black Steel Horseshoes",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "sns:crafting/metal/horseshoes/black_steel"
      ],
      "model_parents": [
        "item/metal/horseshoes/black_steel",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When on Horse:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+10% Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "+2 Fall Distance"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 4200,
        "minecraft:enchantable": {},
        "minecraft:rarity": "rare"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
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
        "rarity": {
          "value": "rare",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = rare"
        }
      }
    },
    {
      "id": "sns:metal/horseshoes/blue_steel",
      "namespace": "sns",
      "display_name": "Blue Steel Horseshoes",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "sns:crafting/metal/horseshoes/blue_steel"
      ],
      "model_parents": [
        "item/metal/horseshoes/blue_steel",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When on Horse:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+25% Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "+5 Fall Distance"
        },
        {
          "source": "runtime-tooltip",
          "text": "+1 Step Height"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 6500,
        "minecraft:enchantable": {},
        "minecraft:rarity": "epic"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
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
        "rarity": {
          "value": "unique",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = epic"
        }
      }
    },
    {
      "id": "sns:metal/horseshoes/red_steel",
      "namespace": "sns",
      "display_name": "Red Steel Horseshoes",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "sns:crafting/metal/horseshoes/red_steel"
      ],
      "model_parents": [
        "item/metal/horseshoes/red_steel",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When on Horse:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+20% Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "+5 Fall Distance"
        },
        {
          "source": "runtime-tooltip",
          "text": "+1 Step Height"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 6500,
        "minecraft:enchantable": {},
        "minecraft:rarity": "epic"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
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
        "rarity": {
          "value": "unique",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = epic"
        }
      }
    },
    {
      "id": "sns:metal/horseshoes/steel",
      "namespace": "sns",
      "display_name": "Steel Horseshoes",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "sns:crafting/metal/horseshoes/steel"
      ],
      "model_parents": [
        "item/metal/horseshoes/steel",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "When on Horse:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+5% Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "+2 Fall Distance"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "sns:tfc/story/steel_horseshoes",
          "label": "Horseshoes of speed",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Horseshoes of speed"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Make horseshoes out of steel. It's like they are enchanted"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 3300,
        "minecraft:enchantable": {},
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
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
        "rarity": {
          "value": "uncommon",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = uncommon"
        }
      }
    },
    {
      "id": "sns:mob_net",
      "namespace": "sns",
      "display_name": "Mob Net",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [
        "item/mob_net",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "document_context": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/sns/mob_net",
          "label": "The Mob Net",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "guide-page",
              "key": "name",
              "text": "The Mob Net"
            },
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "Mob nets are extremely useful for capturing small mobs. You'll get the most use out of them for transporting things like Birds or babies of larger mobs like Cows."
            },
            {
              "source": "guide-page",
              "key": "pages.1.text",
              "text": "For modpack authors or simply people wishing to add extra mobs see the entity tag sns:netable_mobs"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
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
      "id": "sns:ore_sack",
      "namespace": "sns",
      "display_name": "Ore Sack",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "curios:belt",
        "sns:prevented_in_burlap_sack",
        "sns:prevented_in_frame_pack",
        "sns:prevented_in_item_containers",
        "sns:prevented_in_leather_sack",
        "sns:prevented_in_lunchbox",
        "sns:prevented_in_ore_sack",
        "sns:prevented_in_quiver",
        "sns:prevented_in_seed_pouch",
        "sns:prevented_in_straw_basket",
        "tfc:usable_on_tool_rack",
        "tfg:cannot_launch_in_railgun"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:damage_inputs_shaped_crafting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "sns:crafting/ore_sack"
      ],
      "model_parents": [
        "item/ore_sack",
        "item/default"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Belt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold (Shift) for container info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "document_context": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/sns/ore_sack",
          "label": "The Ore Sack",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "guide-page",
              "key": "name",
              "text": "The Ore Sack"
            },
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "The Ore Sack is a type of Item Container intended for holding large amounts of a single ore like item making it extremely useful when going mining. By default these hold a staggering 512 items"
            },
            {
              "source": "guide-page",
              "key": "pages.1.text",
              "text": "For modpack authors or simply people wishing to add extra ores see the item tag sns:allowed_in_ore_sack most addons should be automatically supported"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "sns:pack_frame",
      "namespace": "sns",
      "display_name": "Pack Frame",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "tfc:damage_inputs_shaped_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "tfc:damage_inputs_shaped_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "sns:crafting/frame_pack",
        "tfg:sophisticated_backpacks/shaped/backpack"
      ],
      "recipe_output_examples": [
        "sns:shaped/pack_frame",
        "sns:shaped/pack_frame_iron"
      ],
      "model_parents": [
        "item/pack_frame",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 4,
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
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
            "tfc:damage_inputs_shaped_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "sns:quiver",
      "namespace": "sns",
      "display_name": "Quiver",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "curios:back",
        "curios:belt",
        "sns:prevented_in_burlap_sack",
        "sns:prevented_in_frame_pack",
        "sns:prevented_in_item_containers",
        "sns:prevented_in_leather_sack",
        "sns:prevented_in_lunchbox",
        "sns:prevented_in_ore_sack",
        "sns:prevented_in_quiver",
        "sns:prevented_in_seed_pouch",
        "sns:prevented_in_straw_basket",
        "tfc:usable_on_tool_rack"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:knapping": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "sns:leather_knapping/quiver"
      ],
      "model_parents": [
        "item/quiver",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Back, Belt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold (Shift) for container info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Huge"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "document_context": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/sns/quiver",
          "label": "Quiver",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "The Quiver is a type of Item Container every ranged warriors best friend, be it one with love for the Javalin or those who prefer the bow. Quivers will be drawn from first for \"ammo\", when using the bow and will replace thrown Javelins."
            },
            {
              "source": "guide-page",
              "key": "pages.1.text",
              "text": "For modpack authors or simply people wishing to support extra \"ammo\" see the item tag sns:allowed_in_quiver. Do note the Javalin refill will not work with anything but TFC (or addon) Javelins"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "sns:red_steel_toe_hiking_boots",
      "namespace": "sns",
      "display_name": "Red Steel Toe Hiking Boots",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:freeze_immune_wearables",
        "tfg:cold_protection_equipment"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "crafting_shaped": 3,
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "sns:crafting/red_steel_toe_hiking_boots",
        "sns:shaped/black_steel_toe_hiking_boots_to_red_steel_toe_hiking_boots",
        "sns:shaped/hiking_boots_to_red_steel_toe_hiking_boots",
        "sns:shaped/steel_toe_hiking_boots_to_red_steel_toe_hiking_boots"
      ],
      "model_parents": [
        "item/red_steel_toe_hiking_boots",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§7Provides protection from all §bcold§7 held items and fluids (chest) and pipes (boots).§r"
        },
        {
          "source": "runtime-tooltip",
          "text": "Prevents tall grass slowdown"
        },
        {
          "source": "runtime-tooltip",
          "text": "When on Feet:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+2 Armor"
        },
        {
          "source": "runtime-tooltip",
          "text": "+1 Armor Toughness"
        },
        {
          "source": "runtime-tooltip",
          "text": "+20% Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "+0.5 Step Height"
        },
        {
          "source": "runtime-tooltip",
          "text": "+5 Fall Distance"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Resistances: §fSlashing§r 4%, §fPiercing§r 7%, §fCrushing§r 6%"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 972,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "feet"
        },
        "minecraft:rarity": "epic"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
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
        "rarity": {
          "value": "unique",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = epic"
        },
        "form": {
          "value": "armor_piece",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _boots"
        }
      }
    },
    {
      "id": "sns:reinforced_fabric",
      "namespace": "sns",
      "display_name": "Reinforced Fabric",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "tfc:damage_inputs_shaped_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "tfc:damage_inputs_shaped_crafting": 1
      },
      "recipe_production_by_type": {
        "tfc:loom": 1
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "hangglider:shaped/reinforced_hang_glider",
        "sns:crafting/frame_pack",
        "tfg:sophisticated_backpacks/shaped/backpack"
      ],
      "recipe_output_examples": [
        "sns:loom/reinforced_fabric"
      ],
      "model_parents": [
        "item/reinforced_fabric",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
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
            "crafting",
            "tfc:damage_inputs_shaped_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "sns:reinforced_fiber",
      "namespace": "sns",
      "display_name": "Reinforced Fiber",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "tfc:advanced_shaped_crafting",
        "tfc:damage_inputs_shaped_crafting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "tfc:advanced_shaped_crafting": 5,
        "tfc:damage_inputs_shaped_crafting": 1,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 8,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "sns:crafting/black_steel_toe_hiking_boots",
        "sns:crafting/blue_steel_toe_hiking_boots",
        "sns:crafting/bound_leather_strip",
        "sns:crafting/hiking_boots",
        "sns:crafting/ore_sack",
        "sns:crafting/red_steel_toe_hiking_boots",
        "sns:crafting/steel_toe_hiking_boots",
        "tfg:sophisticated_backpacks/shaped/backpack"
      ],
      "recipe_output_examples": [
        "sns:shaped/reinforced_fiber"
      ],
      "model_parents": [
        "item/reinforced_fiber",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
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
            "crafting",
            "tfc:advanced_shaped_crafting",
            "tfc:damage_inputs_shaped_crafting",
            "tfc:damage_inputs_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "sns:seed_pouch",
      "namespace": "sns",
      "display_name": "Seed Pouch",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "curios:belt",
        "sns:prevented_in_burlap_sack",
        "sns:prevented_in_frame_pack",
        "sns:prevented_in_item_containers",
        "sns:prevented_in_leather_sack",
        "sns:prevented_in_lunchbox",
        "sns:prevented_in_ore_sack",
        "sns:prevented_in_quiver",
        "sns:prevented_in_seed_pouch",
        "sns:prevented_in_straw_basket",
        "tfc:usable_on_tool_rack",
        "tfg:cannot_launch_in_railgun"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "tfc:sewing": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "sns:sewing/seed_pouch"
      ],
      "model_parents": [
        "item/seed_pouch",
        "item/default"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Slot: Belt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold (Shift) for container info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "document_context": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/sns/seed_pouch",
          "label": "The Seed Pouch",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "guide-page",
              "key": "name",
              "text": "The Seed Pouch"
            },
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "The Seed Pouch is a type of Item Container intended for holding a large variety of seeds making it very useful when farming. By default these have 27 slots holding up to 64 seeds each"
            },
            {
              "source": "guide-page",
              "key": "pages.1.text",
              "text": "For modpack authors or simply people wishing to add extra seeds see the item tag sns:allowed_in_seed_pouch most addons should be automatically supported"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
          "confidence": 1,
          "source": "rule:mod_namespace"
        }
      }
    },
    {
      "id": "sns:steel_toe_hiking_boots",
      "namespace": "sns",
      "display_name": "Steel Toe Hiking Boots",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:freeze_immune_wearables"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 3
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "sns:shaped/steel_toe_hiking_boots_to_black_steel_toe_hiking_boots",
        "sns:shaped/steel_toe_hiking_boots_to_blue_steel_toe_hiking_boots",
        "sns:shaped/steel_toe_hiking_boots_to_red_steel_toe_hiking_boots"
      ],
      "recipe_output_examples": [
        "sns:crafting/steel_toe_hiking_boots",
        "sns:shaped/hiking_boots_to_steel_toe_hiking_boots"
      ],
      "model_parents": [
        "item/steel_toe_hiking_boots",
        "item/generated"
      ],
      "creative_tabs": [
        "sns:sacks"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Prevents tall grass slowdown"
        },
        {
          "source": "runtime-tooltip",
          "text": "When on Feet:"
        },
        {
          "source": "runtime-tooltip",
          "text": "+2 Armor"
        },
        {
          "source": "runtime-tooltip",
          "text": "+1 Armor Toughness"
        },
        {
          "source": "runtime-tooltip",
          "text": "+10% Speed"
        },
        {
          "source": "runtime-tooltip",
          "text": "+0.5 Step Height"
        },
        {
          "source": "runtime-tooltip",
          "text": "+1 Fall Distance"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Resistances: §fSlashing§r 4%, §fPiercing§r 7%, §fCrushing§r 6%"
        },
        {
          "source": "runtime-tooltip",
          "text": "Sacks 'N Such"
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "sns:tfc/story/steel_toe_hiking_boots",
          "label": "Protection for those toesies",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Protection for those toesies"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Protect your toes with steel"
            }
          ]
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/sns/safety_toe_hiking_boots",
          "label": "Safety Toe Hiking Boots",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "guide-page",
              "key": "name",
              "text": "Safety Toe Hiking Boots"
            },
            {
              "source": "guide-page",
              "key": "pages.0.text",
              "text": "Like Hiking Boots but safer thanks to their steel toes. It's just a shame nobody's invented a hard hat yet"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 572,
        "minecraft:enchantable": {},
        "minecraft:equippable": {
          "slot": "feet"
        },
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "sns",
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
        "rarity": {
          "value": "uncommon",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = uncommon"
        },
        "form": {
          "value": "armor_piece",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _boots"
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