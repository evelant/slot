# Items to classify
{
  "items": [
    {
      "id": "create:dough",
      "namespace": "create",
      "display_name": "Dough",
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
        "item/dough",
        "item/generated"
      ],
      "creative_tabs": [
        "create:base"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:dripstone_pillar",
      "namespace": "create",
      "display_name": "Travertine Pillar",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:stone_types/dripstone",
        "tfg:stone_composition/sedimentary_carbonate",
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
        "stonecutting": 17
      },
      "recipe_production_by_type": {
        "crafting_shaped": 2,
        "stonecutting": 1
      },
      "recipe_ingredient_count": 21,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_carbonate",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfg:stonecutter/create_cut_dripstone",
        "tfg:stonecutter/create_cut_dripstone_brick_slab_half",
        "tfg:stonecutter/create_cut_dripstone_brick_stairs",
        "tfg:stonecutter/create_cut_dripstone_brick_wall",
        "tfg:stonecutter/create_cut_dripstone_bricks",
        "tfg:stonecutter/create_cut_dripstone_slab_half",
        "tfg:stonecutter/create_cut_dripstone_stairs",
        "tfg:stonecutter/create_cut_dripstone_wall",
        "tfg:stonecutter/create_layered_dripstone",
        "tfg:stonecutter/create_polished_cut_dripstone",
        "tfg:stonecutter/create_polished_cut_dripstone_slab_half",
        "tfg:stonecutter/create_polished_cut_dripstone_stairs",
        "tfg:stonecutter/create_polished_cut_dripstone_wall",
        "tfg:stonecutter/create_small_dripstone_brick_slab_half",
        "tfg:stonecutter/create_small_dripstone_brick_stairs",
        "tfg:stonecutter/create_small_dripstone_brick_wall",
        "tfg:stonecutter/create_small_dripstone_bricks"
      ],
      "recipe_output_examples": [
        "minecraft:kjs/create_dripstone_pillar",
        "tfg:shaped/dripstone_pillar",
        "tfg:stonecutter/create_dripstone_pillar"
      ],
      "model_parents": [
        "item/dripstone_pillar",
        "block/dripstone_pillar",
        "block/cube_column"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/dripstone_pillar"
      ],
      "block_context": {
        "block_id": "create:dripstone_pillar",
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
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:electron_tube",
      "namespace": "create",
      "display_name": "Electron Tube",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 20,
        "crafting_shapeless": 2,
        "greate:milling": 1,
        "kubejs:shaped": 3
      },
      "recipe_production_by_type": {
        "crafting_shaped": 3,
        "create:sequenced_assembly": 1
      },
      "recipe_ingredient_count": 26,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "create:crafting/kinetics/clockwork_bearing",
        "create:crafting/kinetics/nixie_tube",
        "create:crafting/kinetics/smart_chute",
        "create:crafting/kinetics/smart_fluid_pipe",
        "create:crafting/logistics/content_observer",
        "create:crafting/logistics/stockpile_switch",
        "create_connected:crafting/kinetics/inventory_access_port",
        "create_connected:crafting/kinetics/overstress_clutch",
        "create_connected:crafting/kinetics/sequenced_pulse_generator",
        "create_factory_logistics:shaped/jar_packager",
        "greate:milling/integration/gtceu/macerator/macerate_electron_tube",
        "tfg:create/shaped/brass_funnel",
        "tfg:create/shaped/brass_funnel_leather",
        "tfg:create/shaped/brass_tunnel",
        "tfg:create/shaped/brass_tunnel_leather",
        "tfg:create/shaped/contraption_controls",
        "tfg:create/shaped/controller_rail",
        "tfg:create/shaped/deployer",
        "tfg:create/shaped/display_link",
        "tfg:create/shaped/elevator_pulley",
        "tfg:create/shaped/mechanical_crafter",
        "tfg:create/shaped/package_frogport",
        "tfg:create/shaped/packager",
        "tfg:create/shaped/sequenced_gearshift",
        "tfg:sophisticated_backpacks/shaped/pickup_upgrade",
        "tfg:vi/shaped/vacuum_chamber"
      ],
      "recipe_output_examples": [
        "tfg:create/sequenced_assembly/electron_tube",
        "tfg:create/shaped/electron_tube",
        "tfg:create/shaped/electron_tube2",
        "tfg:create/shaped/electron_tube3"
      ],
      "model_parents": [
        "item/electron_tube",
        "item/generated"
      ],
      "creative_tabs": [
        "create:base"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:elevator_contact",
      "namespace": "create",
      "display_name": "Elevator Contact",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 0,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [],
      "model_parents": [
        "item/elevator_contact",
        "block/elevator_contact/block",
        "block/block"
      ],
      "creative_tabs": [],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "create:elevator_contact",
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
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
        }
      }
    },
    {
      "id": "create:elevator_pulley",
      "namespace": "create",
      "display_name": "Elevator Pulley",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
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
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_elevator_pulley",
        "minecraft:kjs/create_connected_music_disc_elevator"
      ],
      "recipe_output_examples": [
        "tfg:create/shaped/elevator_pulley"
      ],
      "model_parents": [
        "item/elevator_pulley",
        "block/elevator_pulley/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/elevator_pulley"
      ],
      "block_context": {
        "block_id": "create:elevator_pulley",
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:empty_blaze_burner",
      "namespace": "create",
      "display_name": "Empty Blaze Burner",
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
        "item/empty_blaze_burner",
        "block/blaze_burner/block",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 2,
      "loot_source_examples": [
        "create:blocks/blaze_burner",
        "create:blocks/lit_blaze_burner"
      ],
      "block_context": {
        "block_id": "create:blaze_burner",
        "block_tags": [
          "create:fan_processing_catalysts/smoking",
          "create:fan_transparent",
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:empty_schematic",
      "namespace": "create",
      "display_name": "Empty Schematic",
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
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "create:crafting/schematics/schematic_and_quill"
      ],
      "recipe_output_examples": [
        "create:crafting/schematics/empty_schematic"
      ],
      "model_parents": [
        "item/empty_schematic",
        "item/generated"
      ],
      "creative_tabs": [
        "create:base"
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
          "text": "Create"
        },
        {
          "source": "lang",
          "key": "item.create.empty_schematic.tooltip.summary",
          "text": "Used as a recipe ingredient and for writing at the _Schematic Table_."
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
          "confidence": 1,
          "source": "rule:mod_namespace"
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
      "id": "create:encased_chain_drive",
      "namespace": "create",
      "display_name": "Encased Chain Drive",
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
        "tfg:create/shapeless/encased_chain_drive"
      ],
      "model_parents": [
        "item/encased_chain_drive",
        "block/encased_chain_drive/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/encased_chain_drive"
      ],
      "block_context": {
        "block_id": "create:encased_chain_drive",
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:encased_fan",
      "namespace": "create",
      "display_name": "Encased Fan",
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
        "item/encased_fan",
        "block/encased_fan/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/encased_fan"
      ],
      "block_context": {
        "block_id": "create:encased_fan",
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
          "text": "Create"
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "create:encased_fan",
          "label": "Wind Maker",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Wind Maker"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Place and power an Encased Fan"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
        }
      }
    },
    {
      "id": "create:experience_block",
      "namespace": "create",
      "display_name": "Block of Experience",
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
        "item/experience_block",
        "block/experience_block",
        "block/block"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/experience_block"
      ],
      "block_context": {
        "block_id": "create:experience_block",
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:light_emission": 15,
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
        "rarity": {
          "value": "uncommon",
          "mode": "override-if-null",
          "confidence": 1,
          "source": "rule:rarity_from_component",
          "rationale": "component minecraft:rarity = uncommon"
        },
        "emits_light": {
          "value": true,
          "confidence": 1,
          "source": "rule:emits_light_from_component"
        }
      }
    },
    {
      "id": "create:experience_nugget",
      "namespace": "create",
      "display_name": "Nugget of Experience",
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
        "item/experience_nugget",
        "item/generated"
      ],
      "creative_tabs": [
        "create:base"
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
          "text": "Create"
        },
        {
          "source": "lang",
          "key": "item.create.experience_nugget.tooltip.behaviour1",
          "text": "_Redeems_ _Experience_ points contained within."
        },
        {
          "source": "lang",
          "key": "item.create.experience_nugget.tooltip.condition1",
          "text": "When Used"
        },
        {
          "source": "lang",
          "key": "item.create.experience_nugget.tooltip.summary",
          "text": "A speck of _inspiration_ from your fantastic inventions."
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
        "form": {
          "value": "nugget",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _nugget"
        }
      }
    },
    {
      "id": "create:exposed_copper_shingle_slab",
      "namespace": "create",
      "display_name": "Exposed Copper Shingle Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:stonecutting/shingle_slabs_from_shingles_exposed_copper"
      ],
      "model_parents": [
        "item/exposed_copper_shingle_slab",
        "block/exposed_copper_shingle_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/exposed_copper_shingle_slab"
      ],
      "block_context": {
        "block_id": "create:exposed_copper_shingle_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool",
          "minecraft:slabs"
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
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
          "value": "copper",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix exposed_copper_"
        },
        "form": {
          "value": "slab",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _slab"
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
      "id": "create:exposed_copper_shingle_stairs",
      "namespace": "create",
      "display_name": "Exposed Copper Shingle Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:stonecutting/shingle_stairs_from_shingles_exposed_copper"
      ],
      "model_parents": [
        "item/exposed_copper_shingle_stairs",
        "block/exposed_copper_shingle_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/exposed_copper_shingle_stairs"
      ],
      "block_context": {
        "block_id": "create:exposed_copper_shingle_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool",
          "minecraft:stairs"
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
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
          "value": "copper",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix exposed_copper_"
        },
        "form": {
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _stairs"
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
      "id": "create:exposed_copper_shingles",
      "namespace": "create",
      "display_name": "Exposed Copper Shingles",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shapeless": 3,
        "stonecutting": 2
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfg:stonecutting/shingle_slabs_from_shingles_exposed_copper",
        "tfg:stonecutting/shingle_stairs_from_shingles_exposed_copper"
      ],
      "recipe_output_examples": [
        "tfg:stonecutting/shingles_exposed_copper"
      ],
      "model_parents": [
        "item/exposed_copper_shingles",
        "block/exposed_copper_shingles",
        "block/cube_column"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/exposed_copper_shingles"
      ],
      "block_context": {
        "block_id": "create:exposed_copper_shingles",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
          "value": "copper",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix exposed_copper_"
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
            "kubejs:shapeless",
            "stonecutting"
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
      "id": "create:exposed_copper_tile_slab",
      "namespace": "create",
      "display_name": "Exposed Copper Tile Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:stonecutting/tile_slabs_from_tiles_exposed_copper"
      ],
      "model_parents": [
        "item/exposed_copper_tile_slab",
        "block/exposed_copper_tile_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/exposed_copper_tile_slab"
      ],
      "block_context": {
        "block_id": "create:exposed_copper_tile_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool",
          "minecraft:slabs"
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
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
          "value": "copper",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix exposed_copper_"
        },
        "form": {
          "value": "slab",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _slab"
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
      "id": "create:exposed_copper_tile_stairs",
      "namespace": "create",
      "display_name": "Exposed Copper Tile Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:stonecutting/tile_stairs_from_tiles_exposed_copper"
      ],
      "model_parents": [
        "item/exposed_copper_tile_stairs",
        "block/exposed_copper_tile_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/exposed_copper_tile_stairs"
      ],
      "block_context": {
        "block_id": "create:exposed_copper_tile_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool",
          "minecraft:stairs"
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
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
          "value": "copper",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix exposed_copper_"
        },
        "form": {
          "value": "stairs",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _stairs"
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
      "id": "create:exposed_copper_tiles",
      "namespace": "create",
      "display_name": "Exposed Copper Tiles",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "kubejs:shapeless",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "kubejs:shapeless": 3,
        "stonecutting": 2
      },
      "recipe_production_by_type": {
        "stonecutting": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "tfg:stonecutting/tile_slabs_from_tiles_exposed_copper",
        "tfg:stonecutting/tile_stairs_from_tiles_exposed_copper"
      ],
      "recipe_output_examples": [
        "tfg:stonecutting/tiles_exposed_copper"
      ],
      "model_parents": [
        "item/exposed_copper_tiles",
        "block/exposed_copper_tiles",
        "block/cube_column"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/exposed_copper_tiles"
      ],
      "block_context": {
        "block_id": "create:exposed_copper_tiles",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
          "value": "copper",
          "confidence": 1,
          "source": "rule:material_family_from_id",
          "rationale": "id prefix exposed_copper_"
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
            "kubejs:shapeless",
            "stonecutting"
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
      "id": "create:extendo_grip",
      "namespace": "create",
      "display_name": "Extendo Grip",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [],
      "recipe_consumption_by_type": {},
      "recipe_production_by_type": {
        "create:mechanical_crafting": 1
      },
      "recipe_ingredient_count": 0,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [],
      "recipe_output_examples": [
        "tfg:create/mechanical_crafting/extendo_grip"
      ],
      "model_parents": [
        "item/extendo_grip",
        "item/extendo_grip/item"
      ],
      "creative_tabs": [
        "create:base"
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
          "text": "Create"
        },
        {
          "source": "lang",
          "key": "item.create.extendo_grip.tooltip.behaviour1",
          "text": "Increases _reach distance_ of items used in the _Main-Hand_."
        },
        {
          "source": "lang",
          "key": "item.create.extendo_grip.tooltip.behaviour2",
          "text": "_No_ _Durability_ will be used. Instead, _Air_ _pressure_ is drained from the Tank"
        },
        {
          "source": "lang",
          "key": "item.create.extendo_grip.tooltip.condition1",
          "text": "When in Off-Hand"
        },
        {
          "source": "lang",
          "key": "item.create.extendo_grip.tooltip.condition2",
          "text": "While wearing Backtank"
        },
        {
          "source": "lang",
          "key": "item.create.extendo_grip.tooltip.summary",
          "text": "Greatly _increases reach distance_ of the wielder. Can be powered with _Air_ _Pressure_ from a _Backtank_"
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "create:extendo_grip",
          "label": "Boioioing!",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Boioioing!"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Get hold of an Extendo Grip"
            }
          ]
        },
        {
          "kind": "advancement",
          "id": "create:extendo_grip_dual",
          "label": "To Full Extent",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "To Full Extent"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Dual-wield Extendo Grips for superhuman reach (Hidden Advancement)"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 200,
        "minecraft:enchantable": {},
        "minecraft:rarity": "uncommon"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:factory_gauge",
      "namespace": "create",
      "display_name": "Factory Gauge",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
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
        "create:crafting/logistics/factory_gauge_clear",
        "greate:milling/integration/gtceu/macerator/macerate_factory_gauge"
      ],
      "recipe_output_examples": [
        "create:crafting/logistics/factory_gauge_clear",
        "tfg:create/shaped/factory_gauge"
      ],
      "model_parents": [
        "item/factory_gauge",
        "block/factory_gauge/item"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/factory_gauge"
      ],
      "block_context": {
        "block_id": "create:factory_gauge",
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
          "text": "Create"
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "create:factory_gauge",
          "label": "High Logistics",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "High Logistics"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Trigger an automatic package request using Factory Gauges"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:filter",
      "namespace": "create",
      "display_name": "List Filter",
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
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "create:crafting/appliances/filter_clear"
      ],
      "recipe_output_examples": [
        "create:crafting/appliances/filter_clear",
        "tfg:create/shaped/filter"
      ],
      "model_parents": [
        "item/filter",
        "item/generated"
      ],
      "creative_tabs": [
        "create:base"
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
          "text": "Create"
        },
        {
          "source": "lang",
          "key": "item.create.filter.tooltip.behaviour1",
          "text": "Opens the _configuration interface_."
        },
        {
          "source": "lang",
          "key": "item.create.filter.tooltip.condition1",
          "text": "When R-Clicked"
        },
        {
          "source": "lang",
          "key": "item.create.filter.tooltip.summary",
          "text": "_Matches items_ against a collection of _items_ or _other filters_. Can be used in _Filter Slots_ of Create's Components"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:fluid_pipe",
      "namespace": "create",
      "display_name": "Fluid Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:mechanical_crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 5,
        "crafting_shapeless": 1,
        "create:mechanical_crafting": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 8,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "create:crafting/kinetics/smart_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_fluid_pipe",
        "greate:shaped/aluminium_mechanical_pump",
        "greate:shaped/stainless_steel_mechanical_pump",
        "greate:shaped/steel_mechanical_pump",
        "greate:shaped/titanium_mechanical_pump",
        "tfg:create/mechanical_crafting/potato_cannon",
        "tfg:create/shapeless/fluid_valve"
      ],
      "recipe_output_examples": [
        "tfg:create/shaped/fluid_pipe"
      ],
      "model_parents": [
        "item/fluid_pipe",
        "block/fluid_pipe/item",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 3,
      "loot_source_examples": [
        "create:blocks/encased_fluid_pipe",
        "create:blocks/fluid_pipe",
        "create:blocks/glass_fluid_pipe"
      ],
      "block_context": {
        "block_id": "create:fluid_pipe",
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
          "text": "Create"
        }
      ],
      "document_context": [
        {
          "kind": "advancement",
          "id": "create:glass_pipe",
          "label": "Flow Discovery",
          "item_ref_count": 1,
          "snippets": [
            {
              "source": "advancement-title",
              "key": "title",
              "text": "Flow Discovery"
            },
            {
              "source": "advancement-description",
              "key": "description",
              "text": "Use your Wrench on a pipe that contains a fluid"
            }
          ]
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
            "create:mechanical_crafting",
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "create:fluid_tank",
      "namespace": "create",
      "display_name": "Fluid Tank",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create_factory_logistics:network_link_qualifier/create_factory_logistics/fluid"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "crafting_shapeless": 33,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 36,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "create_connected:crafting/kinetics/fluid_vessel_from_conversion",
        "greate:milling/integration/gtceu/macerator/macerate_fluid_tank",
        "tfg:create/shaped/spout",
        "tfg:railways/shaped/fuel_tank",
        "tfg:shapeless/black_locometal_boiler",
        "tfg:shapeless/blue_locometal_boiler",
        "tfg:shapeless/brown_locometal_boiler",
        "tfg:shapeless/chartreuse_locometal_boiler",
        "tfg:shapeless/cyan_locometal_boiler",
        "tfg:shapeless/diorite_locometal_boiler",
        "tfg:shapeless/dripstone_locometal_boiler",
        "tfg:shapeless/granite_locometal_boiler",
        "tfg:shapeless/gray_locometal_boiler",
        "tfg:shapeless/green_locometal_boiler",
        "tfg:shapeless/light_blue_locometal_boiler",
        "tfg:shapeless/light_gray_locometal_boiler",
        "tfg:shapeless/lime_locometal_boiler",
        "tfg:shapeless/limestone_locometal_boiler",
        "tfg:shapeless/locometal_boiler",
        "tfg:shapeless/magenta_locometal_boiler",
        "tfg:shapeless/maroon_locometal_boiler",
        "tfg:shapeless/ochrum_locometal_boiler",
        "tfg:shapeless/olive_green_locometal_boiler",
        "tfg:shapeless/orange_locometal_boiler",
        "tfg:shapeless/pine_green_locometal_boiler",
        "tfg:shapeless/pink_locometal_boiler",
        "tfg:shapeless/purple_locometal_boiler",
        "tfg:shapeless/red_locometal_boiler",
        "tfg:shapeless/royal_blue_locometal_boiler",
        "tfg:shapeless/scorchia_locometal_boiler",
        "tfg:shapeless/sea_green_locometal_boiler",
        "tfg:shapeless/tuff_locometal_boiler",
        "tfg:shapeless/turquoise_locometal_boiler",
        "tfg:shapeless/vermilion_locometal_boiler",
        "tfg:shapeless/white_locometal_boiler",
        "tfg:shapeless/yellow_locometal_boiler"
      ],
      "recipe_output_examples": [
        "create_connected:crafting/kinetics/fluid_tank_from_conversion",
        "tfg:create/shaped/fluid_tank"
      ],
      "model_parents": [
        "item/fluid_tank",
        "block/fluid_tank/block_single_window",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/fluid_tank"
      ],
      "block_context": {
        "block_id": "create:fluid_tank",
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
          "text": "§9Fluid Capacity: §f16,000 mB"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:fluid_valve",
      "namespace": "create",
      "display_name": "Fluid Valve",
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
        "tfg:create/shapeless/fluid_valve"
      ],
      "model_parents": [
        "item/fluid_valve",
        "block/fluid_valve/item"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/fluid_valve"
      ],
      "block_context": {
        "block_id": "create:fluid_valve",
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:flywheel",
      "namespace": "create",
      "display_name": "Flywheel",
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
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 1,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "tfg:shapeless/create_flywheel_to_snr_flywheel"
      ],
      "recipe_output_examples": [
        "tfg:create/shaped/flywheel",
        "tfg:shapeless/snr_flywheel_to_create_flywheel"
      ],
      "model_parents": [
        "item/flywheel",
        "block/flywheel/item",
        "block/flywheel/flywheel",
        "block/block"
      ],
      "creative_tabs": [
        "create:base"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/flywheel"
      ],
      "block_context": {
        "block_id": "create:flywheel",
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
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "Create"
        },
        {
          "source": "lang",
          "key": "block.create.flywheel.tooltip.behaviour1",
          "text": "Starts spinning."
        },
        {
          "source": "lang",
          "key": "block.create.flywheel.tooltip.condition1",
          "text": "When Powered by Kinetics"
        },
        {
          "source": "lang",
          "key": "block.create.flywheel.tooltip.summary",
          "text": "_Embellish_ your _Machines_ with this imposing Wheel of Brass."
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
      "id": "create:framed_glass",
      "namespace": "create",
      "display_name": "Framed Glass",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "createdeco:internal/colorless_glass",
        "forge:glass",
        "forge:glass/colorless",
        "railways:internal/glass/colorless",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:mechanical_crafting",
        "greate:cutting",
        "greate:milling",
        "kubejs:shaped",
        "kubejs:shapeless",
        "stonecutting",
        "vintageimprovements:polishing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 176,
        "crafting_shapeless": 1,
        "create:mechanical_crafting": 1,
        "greate:cutting": 1,
        "greate:milling": 1,
        "kubejs:shaped": 5,
        "kubejs:shapeless": 3,
        "stonecutting": 4,
        "vintageimprovements:polishing": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 193,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "createdeco:andesite_window",
        "createdeco:copper_window",
        "createdeco:iron_window",
        "everycomp:c/ad_astra/aeronos_window",
        "everycomp:c/ad_astra/strophar_window",
        "everycomp:c/afc/cypress_window",
        "everycomp:c/afc/fig_window",
        "everycomp:c/afc/ipe_window",
        "everycomp:c/afc/mahogany_window",
        "everycomp:c/afc/tualang_window",
        "everycomp:c/domum_ornamentum/cactus_window",
        "everycomp:c/tfc/acacia_window",
        "everycomp:c/tfc/aspen_window",
        "everycomp:c/tfc/blackwood_window",
        "everycomp:c/tfc/douglas_fir_window",
        "everycomp:c/tfc/kapok_window",
        "everycomp:c/tfc/maple_window",
        "everycomp:c/tfc/palm_window",
        "everycomp:c/tfc/rosewood_window",
        "everycomp:c/tfc/spruce_window",
        "everycomp:c/tfc/white_cedar_window",
        "everycomp:c/wan_ancient_beasts/ginkgo_window",
        "greate:cutting/integration/tfg/create/framed_glass_pane",
        "gtceu:facade_cover",
        "gtceu:shaped/glass_dust_hammer",
        "gtceu:shaped/lv_autoclave",
        "gtceu:shaped/lv_canner",
        "gtceu:shaped/lv_chemical_reactor",
        "gtceu:shaped/lv_distillery",
        "gtceu:shaped/lv_extractor",
        "gtceu:shaped/lv_fluid_heater",
        "gtceu:shaped/lv_food_processor",
        "gtceu:shaped/lv_mixer",
        "gtceu:shaped/lv_rock_crusher",
        "gtceu:shaped/mv_autoclave",
        "gtceu:shaped/mv_canner",
        "gtceu:shaped/mv_cutter",
        "gtceu:shaped/mv_electrolyzer",
        "gtceu:shaped/mv_fermenter",
        "gtceu:shaped/mv_fluid_solidifier",
        "gtceu:shaped/mv_gas_pressurizer",
        "gtceu:shaped/mv_ore_washer",
        "gtceu:shaped/passthrough_hatch_fluid_lv",
        "gtceu:shaped/steam_boiler_lava_steel",
        "gtceu:shaped/ulv_output_hatch",
        "mcw_tfc_aio:roofs/acacia_roofs/acacia_planks_attic_roof",
        "mcw_tfc_aio:roofs/ash_roofs/ash_planks_attic_roof",
        "mcw_tfc_aio:roofs/aspen_roofs/aspen_planks_attic_roof",
        "mcw_tfc_aio:roofs/birch_roofs/birch_attic_roof",
        "mcw_tfc_aio:roofs/black_concrete_roofs/base_attic_roof2",
        "mcw_tfc_aio:roofs/black_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/blackwood_roofs/blackwood_planks_attic_roof",
        "mcw_tfc_aio:roofs/blue_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/brown_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/chestnut_roofs/chestnut_attic_roof",
        "mcw_tfc_aio:roofs/cyan_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/douglas_fir_roofs/douglas_fir_attic_roof",
        "mcw_tfc_aio:roofs/gray_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/gray_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/green_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/hickory_roofs/hickory_planks_attic_roof",
        "mcw_tfc_aio:roofs/kapok_roofs/kapok_planks_attic_roof",
        "mcw_tfc_aio:roofs/light_blue_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/light_gray_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/lime_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/magenta_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/mangrove_roofs/mangrove_attic_roof",
        "mcw_tfc_aio:roofs/maple_roofs/maple_attic_roof",
        "mcw_tfc_aio:roofs/oak_roofs/oak_attic_roof",
        "mcw_tfc_aio:roofs/orange_concrete_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/palm_roofs/palm_attic_roof",
        "mcw_tfc_aio:roofs/pine_roofs/pine_attic_roof",
        "mcw_tfc_aio:roofs/pink_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/purple_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/red_terracotta_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/rosewood_roofs/rosewood_planks_attic_roof",
        "mcw_tfc_aio:roofs/sequoia_roofs/sequoia_planks_attic_roof",
        "mcw_tfc_aio:roofs/spruce_roofs/spruce_planks_attic_roof",
        "mcw_tfc_aio:roofs/sycamore_roofs/sycamore_planks_attic_roof",
        "mcw_tfc_aio:roofs/thatch_roofs/thatch_attic_roof",
        "mcw_tfc_aio:roofs/white_cedar_roofs/white_cedar_planks_attic_roof",
        "mcw_tfc_aio:roofs/white_roofs/x_attic_roof2",
        "mcw_tfc_aio:roofs/willow_roofs/willow_attic_roof",
        "mcw_tfc_aio:roofs/yellow_concrete_roofs/x_attic_roof2",
        "minecraft:kjs/railways_four_pane_locometal_window",
        "minecraft:kjs/railways_single_pane_locometal_window",
        "tfg:create/mechanical_crafting/wand_of_symmetry",
        "tfg:create/shaped/bamboo_window",
        "tfg:create/shaped/cherry_window",
        "tfg:create/shaped/dark_oak_window",
        "tfg:create/shaped/jungle_window",
        "tfg:create/shaped/oak_window",
        "tfg:create/shaped/spruce_window",
        "tfg:create/shapeless/framed_glass_pane",
        "tfg:immersive_aircraft/shaped/telescope",
        "tfg:vi/lathe/lens"
      ],
      "recipe_output_examples": [
        "tfg:create/framed_glass"
      ],
      "recipe_examples_truncated": true,
      "model_parents": [
        "item/framed_glass",
        "block/cube_column"
      ],
      "creative_tabs": [
        "create:palettes"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "create:blocks/framed_glass"
      ],
      "block_context": {
        "block_id": "create:framed_glass",
        "block_tags": [
          "ae2:whitelisted/facades",
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
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "forge:glass",
          "forge:glass/colorless",
          "framedblocks:frameable",
          "minecraft:impermeable",
          "railways:internal/glass/colorless",
          "tfc:mineable_with_glass_saw"
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
          "text": "Create"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "create",
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
          "rationale": "suffix _glass"
        },
        "processing_in": {
          "values": [
            "crafting",
            "create:mechanical_crafting",
            "greate:cutting",
            "greate:milling",
            "kubejs:shaped",
            "kubejs:shapeless",
            "stonecutting",
            "vintageimprovements:polishing"
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