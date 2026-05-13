# Items to classify
{
  "items": [
    {
      "id": "greate:tiny_rose_quartz_dust",
      "namespace": "greate",
      "display_name": "Tiny Pile of Rose Quartz Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:tiny_dusts",
        "forge:tiny_dusts/rose_quartz"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1
      },
      "recipe_production_by_type": {
        "gtceu:crafting_shaped_strict": 2
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "gtceu:shaped/tiny_dust_assembling_rose_quartz"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/tiny_dust_disassembling_3x3_rose_quartz",
        "gtceu:shaped/tiny_dust_disassembling_rose_quartz"
      ],
      "model_parents": [],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "(SiO₂)(Si(FeS₂)₅(CrAl₂O₃)Hg₃)₈"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "form": {
          "value": "dust",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _dust"
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
      "id": "greate:tiny_shadow_steel_dust",
      "namespace": "greate",
      "display_name": "Tiny Pile of Shadow Steel Dust",
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
      "model_parents": [],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "?₃(MgFeSi₂O₄)₃((SiO₂)(Si(FeS₂)₅(CrAl₂O₃)Hg₃)₈)Sp"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "form": {
          "value": "dust",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _dust"
        }
      }
    },
    {
      "id": "greate:titanium_cogwheel",
      "namespace": "greate",
      "display_name": "Titanium Cogwheel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:cogwheels",
        "forge:cogwheels/titanium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "tfc:extra_products_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 18,
        "crafting_shapeless": 2,
        "greate:milling": 1,
        "tfc:extra_products_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 22,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "create:shaped/windmill_bearing",
        "create_connected:crafting/kinetics/freewheel_clutch",
        "greate:milling/integration/gtceu/macerator/macerate_titanium_cogwheel",
        "greate:shaped/titanium_mechanical_press",
        "greate:shaped/titanium_mechanical_pump",
        "tfc:kjs/aexes2ur8r9z59nmylk6qvmhq",
        "tfg:create/shaped/deployer",
        "tfg:create/shaped/elevator_pulley",
        "tfg:create/shaped/gantry_carriage",
        "tfg:create/shaped/gearshift",
        "tfg:create/shaped/mechanical_bearing",
        "tfg:create/shaped/mechanical_harvester",
        "tfg:create/shaped/mechanical_plough",
        "tfg:create/shaped/mechanical_roller",
        "tfg:create/shaped/rope_pulley",
        "tfg:create/shaped/sequenced_gearshift",
        "tfg:create/shaped/weighted_ejector",
        "tfg:create/shaped/wrench",
        "tfg:railways/shaped/track_switch_andesite",
        "tfg:railways/shaped/track_switch_brass",
        "tfg:shapeless/large_titanium_cogwheel_upgrade",
        "tfg:sophisticated_backpacks/shaped/pickup_upgrade"
      ],
      "recipe_output_examples": [
        "tfg:shapeless/titanium_cogwheel"
      ],
      "model_parents": [],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "greate:titanium_cogwheel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ti"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
            "tfc:extra_products_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "greate:titanium_crushing_wheel",
      "namespace": "greate",
      "display_name": "Titanium Crushing Wheel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "greate:crushing_wheels"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "create:mechanical_crafting": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_titanium_crushing_wheel"
      ],
      "recipe_output_examples": [
        "greate:titanium_crushing_wheel"
      ],
      "model_parents": [
        "item/titanium_crushing_wheel",
        "block/titanium_crushing_wheel_textures",
        "block/crushing_wheel/textures"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/titanium_crushing_wheel"
      ],
      "block_context": {
        "block_id": "greate:titanium_crushing_wheel",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
            "greate:milling"
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
      "id": "greate:titanium_encased_fan",
      "namespace": "greate",
      "display_name": "Titanium Encased Fan",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "greate:encased_fans"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_titanium_encased_fan"
      ],
      "recipe_output_examples": [
        "greate:shaped/titanium_encased_fan"
      ],
      "model_parents": [
        "item/titanium_encased_fan",
        "block/encased_fan/item"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/titanium_encased_fan"
      ],
      "block_context": {
        "block_id": "greate:titanium_encased_fan",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        },
        "processing_in": {
          "values": [
            "greate:milling"
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
      "id": "greate:titanium_gearbox",
      "namespace": "greate",
      "display_name": "Titanium Gearbox",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gearboxes",
        "forge:gearboxes/titanium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "tfc:extra_products_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 1,
        "tfc:extra_products_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_titanium_gearbox",
        "minecraft:kjs/greate_titanium_vertical_gearbox",
        "tfc:kjs/8orm9ixklhznocbjc8qkssqkl"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/greate_titanium_gearbox",
        "tfg:shaped/titanium_gearbox"
      ],
      "model_parents": [],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "greate:titanium_gearbox",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ti"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "tfc:extra_products_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "greate:titanium_mechanical_mixer",
      "namespace": "greate",
      "display_name": "Titanium Mechanical Mixer",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "greate:mechanical_mixers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_titanium_mechanical_mixer"
      ],
      "recipe_output_examples": [
        "greate:shaped/titanium_mechanical_mixer"
      ],
      "model_parents": [
        "item/titanium_mechanical_mixer",
        "block/mechanical_mixer/item"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/titanium_mechanical_mixer"
      ],
      "block_context": {
        "block_id": "greate:titanium_mechanical_mixer",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        },
        "processing_in": {
          "values": [
            "greate:milling"
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
      "id": "greate:titanium_mechanical_press",
      "namespace": "greate",
      "display_name": "Titanium Mechanical Press",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "greate:mechanical_presses"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_titanium_mechanical_press"
      ],
      "recipe_output_examples": [
        "greate:shaped/titanium_mechanical_press"
      ],
      "model_parents": [
        "item/titanium_mechanical_press",
        "block/mechanical_press/item"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/titanium_mechanical_press"
      ],
      "block_context": {
        "block_id": "greate:titanium_mechanical_press",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
          "value": "axe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/axe"
        },
        "processing_in": {
          "values": [
            "greate:milling"
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
      "id": "greate:titanium_mechanical_pump",
      "namespace": "greate",
      "display_name": "Titanium Mechanical Pump",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "greate:mechanical_pumps",
        "tfc:forge_invisible_whitelist"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_titanium_mechanical_pump"
      ],
      "recipe_output_examples": [
        "greate:shaped/titanium_mechanical_pump"
      ],
      "model_parents": [
        "item/titanium_mechanical_pump",
        "block/mechanical_pump/item"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/titanium_mechanical_pump"
      ],
      "block_context": {
        "block_id": "greate:titanium_mechanical_pump",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:forge_invisible_whitelist"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
            "greate:milling"
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
      "id": "greate:titanium_mechanical_saw",
      "namespace": "greate",
      "display_name": "Titanium Mechanical Saw",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers",
        "greate:mechanical_saws"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_titanium_mechanical_saw"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/titanium_mechanical_saw",
        "block/mechanical_saw/item"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/titanium_mechanical_saw"
      ],
      "block_context": {
        "block_id": "greate:titanium_mechanical_saw",
        "block_tags": [
          "c:hidden_from_recipe_viewers"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
        "processing_in": {
          "values": [
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "greate:titanium_millstone",
      "namespace": "greate",
      "display_name": "Titanium Millstone",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "greate:millstones"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "kubejs:shaped": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_titanium_millstone"
      ],
      "recipe_output_examples": [
        "greate:shaped/titanium_millstone"
      ],
      "model_parents": [
        "item/titanium_millstone",
        "block/millstone/item"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/titanium_millstone"
      ],
      "block_context": {
        "block_id": "greate:titanium_millstone",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
            "greate:milling"
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
      "id": "greate:titanium_shaft",
      "namespace": "greate",
      "display_name": "Titanium Shaft",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:shafts",
        "forge:shafts/titanium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 10,
        "crafting_shapeless": 6,
        "greate:milling": 1,
        "kubejs:shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "greate:cutting": 1
      },
      "recipe_ingredient_count": 18,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "create_connected:crafting/kinetics/brake",
        "create_connected:crafting/kinetics/centrifugal_clutch",
        "create_connected:crafting/kinetics/freewheel_clutch",
        "create_connected:crafting/kinetics/kinetic_bridge",
        "create_connected:crafting/kinetics/overstress_clutch",
        "greate:milling/integration/gtceu/macerator/macerate_titanium_shaft",
        "greate:shaped/titanium_encased_fan",
        "greate:shaped/titanium_mechanical_mixer",
        "greate:shaped/titanium_mechanical_press",
        "tfg:create/shaped/clutch",
        "tfg:create/shaped/flywheel",
        "tfg:create/shaped/mechanical_piston",
        "tfg:create/shaped/rotation_speed_controller",
        "tfg:createadditions/shaped/alternator",
        "tfg:shaped/titanium_gearbox",
        "tfg:shaped/titanium_vertical_gearbox",
        "tfg:shapeless/large_titanium_cogwheel",
        "tfg:shapeless/titanium_cogwheel"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/greate/titanium_shaft",
        "greate:shaped/titanium_shaft"
      ],
      "model_parents": [],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "greate:titanium_shaft",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
            "kubejs:shaped"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "greate:titanium_vertical_gearbox",
      "namespace": "greate",
      "display_name": "Titanium Vertical Gearbox",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:vertical_gearboxes",
        "forge:vertical_gearboxes/titanium"
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
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_titanium_vertical_gearbox",
        "minecraft:kjs/greate_titanium_gearbox"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/greate_titanium_vertical_gearbox",
        "tfg:shaped/titanium_vertical_gearbox"
      ],
      "model_parents": [],
      "creative_tabs": [],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "greate:titanium_gearbox",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/axe",
          "minecraft:mineable/pickaxe"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Ti"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
      "id": "greate:treated_wood_window",
      "namespace": "greate",
      "display_name": "Treated Wood Window",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:cutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:cutting": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/tfg/treated_wood_window_pane",
        "tfg:greate/shapeless/treated_wood_window_pane"
      ],
      "recipe_output_examples": [
        "tfg:greate/shaped/treated_wood_window"
      ],
      "model_parents": [
        "item/treated_wood_window",
        "block/treated_wood_window",
        "block/cube_column"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/treated_wood_window"
      ],
      "block_context": {
        "block_id": "greate:treated_wood_window",
        "block_tags": [
          "minecraft:impermeable"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
          "value": "pane",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _window"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:cutting"
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
      "id": "greate:treated_wood_window_pane",
      "namespace": "greate",
      "display_name": "Treated Wood Window Pane",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:glass_panes"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped",
        "tfc:advanced_shaped_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 46,
        "greate:milling": 1,
        "kubejs:shaped": 1,
        "tfc:advanced_shaped_crafting": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1,
        "greate:cutting": 1
      },
      "recipe_ingredient_count": 49,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_glass_pane",
        "gtceu:shaped/basic_terminal",
        "gtceu:shaped/redstone_lamp",
        "gtceu:shaped/solar_panel_ulv",
        "gtceu:shaped/steam_boiler_solar_steel",
        "mcw_tfc_aio:furniture/acacia_furniture/acacia_glass_table",
        "mcw_tfc_aio:furniture/acacia_furniture/stripped_acacia_glass_table",
        "mcw_tfc_aio:furniture/ash_furniture/ash_glass_table",
        "mcw_tfc_aio:furniture/ash_furniture/stripped_ash_glass_table",
        "mcw_tfc_aio:furniture/aspen_furniture/aspen_glass_table",
        "mcw_tfc_aio:furniture/aspen_furniture/stripped_aspen_glass_table",
        "mcw_tfc_aio:furniture/birch_furniture/birch_glass_table",
        "mcw_tfc_aio:furniture/birch_furniture/stripped_birch_glass_table",
        "mcw_tfc_aio:furniture/blackwood_furniture/blackwood_glass_table",
        "mcw_tfc_aio:furniture/blackwood_furniture/stripped_blackwood_glass_table",
        "mcw_tfc_aio:furniture/chestnut_furniture/chestnut_glass_table",
        "mcw_tfc_aio:furniture/chestnut_furniture/stripped_chestnut_glass_table",
        "mcw_tfc_aio:furniture/douglas_fir_furniture/douglas_fir_glass_table",
        "mcw_tfc_aio:furniture/douglas_fir_furniture/stripped_douglas_fir_glass_table",
        "mcw_tfc_aio:furniture/hickory_furniture/hickory_glass_table",
        "mcw_tfc_aio:furniture/hickory_furniture/stripped_hickory_glass_table",
        "mcw_tfc_aio:furniture/kapok_furniture/kapok_glass_table",
        "mcw_tfc_aio:furniture/kapok_furniture/stripped_kapok_glass_table",
        "mcw_tfc_aio:furniture/mangrove_furniture/mangrove_glass_table",
        "mcw_tfc_aio:furniture/mangrove_furniture/stripped_mangrove_glass_table",
        "mcw_tfc_aio:furniture/maple_furniture/maple_glass_table",
        "mcw_tfc_aio:furniture/maple_furniture/stripped_maple_glass_table",
        "mcw_tfc_aio:furniture/oak_furniture/oak_glass_table",
        "mcw_tfc_aio:furniture/oak_furniture/stripped_oak_glass_table",
        "mcw_tfc_aio:furniture/palm_furniture/palm_glass_table",
        "mcw_tfc_aio:furniture/palm_furniture/stripped_palm_glass_table",
        "mcw_tfc_aio:furniture/pine_furniture/pine_glass_table",
        "mcw_tfc_aio:furniture/pine_furniture/stripped_pine_glass_table",
        "mcw_tfc_aio:furniture/rosewood_furniture/rosewood_glass_table",
        "mcw_tfc_aio:furniture/rosewood_furniture/stripped_rosewood_glass_table",
        "mcw_tfc_aio:furniture/sequoia_furniture/sequoia_glass_table",
        "mcw_tfc_aio:furniture/sequoia_furniture/stripped_sequoia_glass_table",
        "mcw_tfc_aio:furniture/spruce_furniture/spruce_glass_table",
        "mcw_tfc_aio:furniture/spruce_furniture/stripped_spruce_glass_table",
        "mcw_tfc_aio:furniture/sycamore_furniture/stripped_sycamore_glass_table",
        "mcw_tfc_aio:furniture/sycamore_furniture/sycamore_glass_table",
        "mcw_tfc_aio:furniture/white_cedar_furniture/stripped_white_cedar_glass_table",
        "mcw_tfc_aio:furniture/white_cedar_furniture/white_cedar_glass_table",
        "mcw_tfc_aio:furniture/willow_furniture/stripped_willow_glass_table",
        "mcw_tfc_aio:furniture/willow_furniture/willow_glass_table",
        "tfg:create/shaped/copper_diving_helmet",
        "tfg:create/shaped/fluid_tank",
        "tfg:create/shaped/stock_ticker",
        "tfg:shaped/snorkel"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/tfg/treated_wood_window_pane",
        "tfg:greate/shapeless/treated_wood_window_pane"
      ],
      "model_parents": [
        "item/treated_wood_window_pane",
        "item/generated"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/treated_wood_window_pane"
      ],
      "block_context": {
        "block_id": "greate:treated_wood_window_pane",
        "block_tags": [
          "forge:glass_panes",
          "tfc:mineable_with_glass_saw"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
          "value": "pane",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _window_pane"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "kubejs:shaped",
            "tfc:advanced_shaped_crafting"
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
      "id": "greate:tuff_rose_quartz_ore",
      "namespace": "greate",
      "display_name": "Tuff Rose Quartz Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/rose_quartz",
        "forge:ores_in_ground/tuff"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "blasting",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "blasting": 1,
        "smelting": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 2,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "gtceu:blasting/smelt_tuff_rose_quartz_ore_to_ingot",
        "gtceu:smelting/smelt_tuff_rose_quartz_ore_to_ingot"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "greate:tuff_rose_quartz_ore",
        "block_tags": [
          "c:hidden_from_recipe_viewers",
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "endermanoverhaul:cave_enderman_holdable",
          "forge:ores",
          "forge:ores/rose_quartz",
          "forge:ores_in_ground/tuff",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool",
          "species:cliff_hanger_spawnable_on",
          "species:limpet_spawnable_on",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfc:monster_spawns_on",
          "tfc:powderkeg_breaking_blocks",
          "tfc:prospectable"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "(SiO₂)(Si(FeS₂)₅(CrAl₂O₃)Hg₃)₈"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
          "value": "tuff_rose_quartz",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id tuff_rose_quartz_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "required_tool": {
          "value": "pickaxe",
          "confidence": 1,
          "source": "rule:required_tool_from_block_tag",
          "rationale": "tag minecraft:mineable/pickaxe"
        },
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
        },
        "processing_in": {
          "values": [
            "blasting",
            "smelting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        },
        "y_level_range": {
          "value": "underground",
          "confidence": 1,
          "source": "rule:y_level_range_from_id",
          "rationale": "id pattern"
        }
      }
    },
    {
      "id": "greate:tungsten_steel_cogwheel",
      "namespace": "greate",
      "display_name": "Tungsten Steel Cogwheel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_tungsten_steel_cogwheel"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "greate:tungsten_steel_cogwheel",
        "block_tags": [
          "c:hidden_from_recipe_viewers"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "FeW"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
        "processing_in": {
          "values": [
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "greate:tungsten_steel_crushing_wheel",
      "namespace": "greate",
      "display_name": "Tungsten Steel Crushing Wheel",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers",
        "greate:crushing_wheels"
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
        "item/tungsten_steel_crushing_wheel",
        "block/tungsten_steel_crushing_wheel_textures",
        "block/crushing_wheel/textures"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/tungsten_steel_crushing_wheel"
      ],
      "block_context": {
        "block_id": "greate:tungsten_steel_crushing_wheel",
        "block_tags": [
          "c:hidden_from_recipe_viewers"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
        }
      }
    },
    {
      "id": "greate:tungsten_steel_encased_fan",
      "namespace": "greate",
      "display_name": "Tungsten Steel Encased Fan",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers",
        "greate:encased_fans"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_tungsten_steel_encased_fan"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/tungsten_steel_encased_fan",
        "block/encased_fan/item"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/tungsten_steel_encased_fan"
      ],
      "block_context": {
        "block_id": "greate:tungsten_steel_encased_fan",
        "block_tags": [
          "c:hidden_from_recipe_viewers"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
        "processing_in": {
          "values": [
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "greate:tungsten_steel_gearbox",
      "namespace": "greate",
      "display_name": "Tungsten Steel Gearbox",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "tfc:extra_products_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "tfc:extra_products_shapeless_crafting": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 2,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_tungsten_steel_gearbox",
        "tfc:kjs/1fip5749ifp7b0k7fdn1g2fdi"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "greate:tungsten_steel_gearbox",
        "block_tags": [
          "c:hidden_from_recipe_viewers"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "FeW"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
        "processing_in": {
          "values": [
            "greate:milling",
            "tfc:extra_products_shapeless_crafting"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "greate:tungsten_steel_mechanical_mixer",
      "namespace": "greate",
      "display_name": "Tungsten Steel Mechanical Mixer",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers",
        "greate:mechanical_mixers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_tungsten_steel_mechanical_mixer"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/tungsten_steel_mechanical_mixer",
        "block/mechanical_mixer/item"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/tungsten_steel_mechanical_mixer"
      ],
      "block_context": {
        "block_id": "greate:tungsten_steel_mechanical_mixer",
        "block_tags": [
          "c:hidden_from_recipe_viewers"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
        "processing_in": {
          "values": [
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "greate:tungsten_steel_mechanical_press",
      "namespace": "greate",
      "display_name": "Tungsten Steel Mechanical Press",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers",
        "greate:mechanical_presses"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_tungsten_steel_mechanical_press"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/tungsten_steel_mechanical_press",
        "block/mechanical_press/item"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/tungsten_steel_mechanical_press"
      ],
      "block_context": {
        "block_id": "greate:tungsten_steel_mechanical_press",
        "block_tags": [
          "c:hidden_from_recipe_viewers"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
        "processing_in": {
          "values": [
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "greate:tungsten_steel_mechanical_pump",
      "namespace": "greate",
      "display_name": "Tungsten Steel Mechanical Pump",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers",
        "greate:mechanical_pumps"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_tungsten_steel_mechanical_pump"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/tungsten_steel_mechanical_pump",
        "block/mechanical_pump/item"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/tungsten_steel_mechanical_pump"
      ],
      "block_context": {
        "block_id": "greate:tungsten_steel_mechanical_pump",
        "block_tags": [
          "c:hidden_from_recipe_viewers"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
        "processing_in": {
          "values": [
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "greate:tungsten_steel_mechanical_saw",
      "namespace": "greate",
      "display_name": "Tungsten Steel Mechanical Saw",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers",
        "greate:mechanical_saws"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_tungsten_steel_mechanical_saw"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/tungsten_steel_mechanical_saw",
        "block/mechanical_saw/item"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/tungsten_steel_mechanical_saw"
      ],
      "block_context": {
        "block_id": "greate:tungsten_steel_mechanical_saw",
        "block_tags": [
          "c:hidden_from_recipe_viewers"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
        "processing_in": {
          "values": [
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "greate:tungsten_steel_millstone",
      "namespace": "greate",
      "display_name": "Tungsten Steel Millstone",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers",
        "greate:millstones"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_tungsten_steel_millstone"
      ],
      "recipe_output_examples": [],
      "model_parents": [
        "item/tungsten_steel_millstone",
        "block/millstone/item"
      ],
      "creative_tabs": [
        "greate:greate"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "greate:blocks/tungsten_steel_millstone"
      ],
      "block_context": {
        "block_id": "greate:tungsten_steel_millstone",
        "block_tags": [
          "c:hidden_from_recipe_viewers"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Hold [W] to Ponder"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Greate"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "greate",
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
        "processing_in": {
          "values": [
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