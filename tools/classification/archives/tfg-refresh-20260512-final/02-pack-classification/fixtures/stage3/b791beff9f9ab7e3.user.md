# Items to classify
{
  "items": [
    {
      "id": "gtceu:aluminium_block",
      "namespace": "gtceu",
      "display_name": "Block of Aluminium",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:storage_blocks",
        "forge:storage_blocks/aluminium",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "create:mechanical_crafting",
        "greate:cutting",
        "greate:milling",
        "kubejs:shapeless"
      ],
      "recipe_consumption_by_type": {
        "create:mechanical_crafting": 1,
        "greate:cutting": 3,
        "greate:milling": 1,
        "kubejs:shapeless": 3
      },
      "recipe_production_by_type": {
        "greate:compacting": 1
      },
      "recipe_ingredient_count": 8,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_aluminium_block_to_plate",
        "greate:cutting/integration/gtceu/cutter/cut_aluminium_block_to_plate_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_aluminium_block_to_plate_water",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_block",
        "greate:stainless_steel_crushing_wheel",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle"
      ],
      "recipe_output_examples": [
        "greate:compacting/aluminium_block"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_block",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
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
          "forge:storage_blocks",
          "forge:storage_blocks/aluminium",
          "minecraft:mineable/pickaxe",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
        },
        "processing_in": {
          "values": [
            "create:mechanical_crafting",
            "greate:cutting",
            "greate:milling",
            "kubejs:shapeless"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:aluminium_bolt",
      "namespace": "gtceu",
      "display_name": "Aluminium Bolt",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:bolts",
        "forge:bolts/aluminium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "tfc:advanced_shaped_crafting",
        "vintageimprovements:polishing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "crafting_shapeless": 1,
        "greate:milling": 1,
        "tfc:advanced_shaped_crafting": 18,
        "vintageimprovements:polishing": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "greate:cutting": 6
      },
      "recipe_ingredient_count": 23,
      "recipe_output_count": 7,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_bolt",
        "gtceu:shaped/screw_aluminium",
        "tfchotornot:crafting/tongs/bismuth",
        "tfchotornot:crafting/tongs/bismuth_bronze",
        "tfchotornot:crafting/tongs/black_bronze",
        "tfchotornot:crafting/tongs/black_steel",
        "tfchotornot:crafting/tongs/blue_steel",
        "tfchotornot:crafting/tongs/brass",
        "tfchotornot:crafting/tongs/bronze",
        "tfchotornot:crafting/tongs/copper",
        "tfchotornot:crafting/tongs/gold",
        "tfchotornot:crafting/tongs/nickel",
        "tfchotornot:crafting/tongs/red_steel",
        "tfchotornot:crafting/tongs/rose_gold",
        "tfchotornot:crafting/tongs/silver",
        "tfchotornot:crafting/tongs/steel",
        "tfchotornot:crafting/tongs/sterling_silver",
        "tfchotornot:crafting/tongs/tin",
        "tfchotornot:crafting/tongs/wrought_iron",
        "tfchotornot:crafting/tongs/zinc",
        "tfg:create/shaped/clipboard",
        "tfg:create/shapeless/minecart_coupling",
        "tfg:vi/lathe/aluminium_bolt_to_screw"
      ],
      "recipe_output_examples": [
        "greate:cutting/integration/gtceu/cutter/cut_aluminium_rod_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_aluminium_rod_to_bolt_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_aluminium_rod_to_bolt_water",
        "greate:cutting/integration/gtceu/cutter/cut_aluminium_screw_to_bolt",
        "greate:cutting/integration/gtceu/cutter/cut_aluminium_screw_to_bolt_distilled_water",
        "greate:cutting/integration/gtceu/cutter/cut_aluminium_screw_to_bolt_water",
        "gtceu:shaped/bolt_saw_aluminium"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
            "tfc:advanced_shaped_crafting",
            "vintageimprovements:polishing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:aluminium_bucket",
      "namespace": "gtceu",
      "display_name": "Liquid Aluminium Bucket",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "ae2:p2p_attunements/fluid_p2p_tunnel"
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
        "gtceu:material_fluid"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aState: Liquid"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature: 933 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 1,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "form": {
          "value": "bucket",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _bucket"
        }
      }
    },
    {
      "id": "gtceu:aluminium_crate",
      "namespace": "gtceu",
      "display_name": "Aluminium Crate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "create:non_movable",
        "tfg:cannot_launch_in_railgun"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "greate:milling": 1,
        "kubejs:shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_crate",
        "gtceu:shaped/super_chest_mv",
        "tfg:crafting/me_chest"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/aluminium_crate"
      ],
      "model_parents": [
        "item/aluminium_crate",
        "block/machine/aluminium_crate",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_crate",
        "block_tags": [
          "create:non_movable",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§6Item Slots: §f90"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
      "id": "gtceu:aluminium_double_cable",
      "namespace": "gtceu",
      "display_name": "2x Aluminium Cable",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:double_cables",
        "forge:double_cables/aluminium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 2,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_double_cable",
        "gtceu:shaped/electric_motor_ev"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_double_cable",
        "block_tags": [
          "forge:double_cables",
          "forge:double_cables/aluminium",
          "forge:mineable/wire_cutter",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a2,048 §a(§5EV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e2"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c1§7 EU-Volt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:aluminium_double_wire",
      "namespace": "gtceu",
      "display_name": "2x Aluminium Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:double_wires",
        "forge:double_wires/aluminium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 3,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 2
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_double_wire",
        "gtceu:shapeless/aluminium_wire_wire_gt_double_doubling",
        "gtceu:shapeless/aluminium_wire_wire_gt_double_quadrupling",
        "gtceu:shapeless/aluminium_wire_wire_gt_double_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/aluminium_wire_wire_gt_quadruple_splitting",
        "gtceu:shapeless/aluminium_wire_wire_gt_single_doubling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_double_wire",
        "block_tags": [
          "forge:double_wires",
          "forge:double_wires/aluminium",
          "forge:mineable/wire_cutter",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a2,048 §a(§5EV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e2"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c2§7 EU-Volt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:aluminium_drum",
      "namespace": "gtceu",
      "display_name": "Aluminium Drum",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:cannot_launch_in_railgun"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "greate:milling": 1,
        "kubejs:shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_drum",
        "gtceu:shaped/coal_liquefaction_tower",
        "gtceu:shapeless/drum_nbt_aluminium",
        "tfg:sophisticated_backpacks/shaped/tank_upgrade"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/aluminium_drum",
        "gtceu:shapeless/drum_nbt_aluminium"
      ],
      "model_parents": [
        "item/aluminium_drum",
        "block/machine/aluminium_drum",
        "block/block"
      ],
      "creative_tabs": [
        "gtceu:machine"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_drum",
        "block_tags": [
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§7Compact place to store all your fluids"
        },
        {
          "source": "runtime-tooltip",
          "text": "§9Fluid Capacity: §f128,000 mB"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
      "id": "gtceu:aluminium_dust",
      "namespace": "gtceu",
      "display_name": "Aluminium Dust",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:dusts",
        "forge:dusts/aluminium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:mixing",
        "gtceu:crafting_shaped_strict"
      ],
      "recipe_consumption_by_type": {
        "greate:mixing": 7,
        "gtceu:crafting_shaped_strict": 4
      },
      "recipe_production_by_type": {
        "ae2:transform": 2,
        "crafting_shaped": 2,
        "crafting_shapeless": 1,
        "greate:milling": 126,
        "greate:pressing": 1,
        "greate:splashing": 4,
        "tfc:barrel_instant": 2,
        "vintageimprovements:centrifugation": 2
      },
      "recipe_ingredient_count": 11,
      "recipe_output_count": 140,
      "recipe_ingredient_examples": [
        "greate:mixing/integration/gtceu/mixer/cobalt_brass",
        "greate:mixing/integration/gtceu/mixer/kanthal",
        "greate:mixing/integration/gtceu/mixer/magnalium",
        "greate:mixing/integration/tfg/rene_41_dust",
        "greate:mixing/integration/tfg/rocket_alloy_t_1",
        "greate:mixing/integration/tfg/rocket_alloy_t_2",
        "greate:mixing/integration/tfg/weak_inconel_718",
        "gtceu:shaped/small_dust_disassembling_3x3_aluminium",
        "gtceu:shaped/small_dust_disassembling_aluminium",
        "gtceu:shaped/tiny_dust_disassembling_3x3_aluminium",
        "gtceu:shaped/tiny_dust_disassembling_aluminium"
      ],
      "recipe_output_examples": [
        "greate:milling/integration/gtceu/macerate_aluminium_refined_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_advanced_activity_detector_cover",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_crate",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_double_wire",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_fluid_cell",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_frame",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_hex_cable",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_hex_wire",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_ingot",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_large_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_nonuple_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_normal_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_octal_wire",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_quadruple_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_quadruple_wire",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_rotor",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_small_fluid_pipe",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_whisk",
        "greate:milling/integration/gtceu/macerator/macerate_aluminum_sheet",
        "greate:milling/integration/gtceu/macerator/macerate_cell_workbench",
        "greate:milling/integration/gtceu/macerator/macerate_central_monitor",
        "greate:milling/integration/gtceu/macerator/macerate_coal_liquefaction_tower",
        "greate:milling/integration/gtceu/macerator/macerate_color_applicator",
        "greate:milling/integration/gtceu/macerator/macerate_computer_normal",
        "greate:milling/integration/gtceu/macerator/macerate_data_access_hatch",
        "greate:milling/integration/gtceu/macerator/macerate_double_aluminium_plate",
        "greate:milling/integration/gtceu/macerator/macerate_ender_redstone_link_cover",
        "greate:milling/integration/gtceu/macerator/macerate_ev_auto_charger_4_x",
        "greate:milling/integration/gtceu/macerator/macerate_ev_battery_buffer_16_x",
        "greate:milling/integration/gtceu/macerator/macerate_ev_battery_buffer_4_x",
        "greate:milling/integration/gtceu/macerator/macerate_ev_charger_4_x",
        "greate:milling/integration/gtceu/macerator/macerate_ev_input_bus",
        "greate:milling/integration/gtceu/macerator/macerate_frostproof_machine_casing",
        "greate:milling/integration/gtceu/macerator/macerate_gas_tank",
        "greate:milling/integration/gtceu/macerator/macerate_hpca_heat_sink_component",
        "greate:milling/integration/gtceu/macerator/macerate_interplanetary_item_receiver",
        "greate:milling/integration/gtceu/macerator/macerate_long_aluminium_rod",
        "greate:milling/integration/gtceu/macerator/macerate_machine_casing_aluminium_plated_steel",
        "greate:milling/integration/gtceu/macerator/macerate_me_output_bus",
        "greate:milling/integration/gtceu/macerator/macerate_mod_storage_bus",
        "greate:milling/integration/gtceu/macerator/macerate_mv_1_a_energy_converter",
        "greate:milling/integration/gtceu/macerator/macerate_mv_air_scrubber",
        "greate:milling/integration/gtceu/macerator/macerate_mv_alloy_smelter",
        "greate:milling/integration/gtceu/macerator/macerate_mv_autoclave",
        "greate:milling/integration/gtceu/macerator/macerate_mv_bender",
        "greate:milling/integration/gtceu/macerator/macerate_mv_brewery",
        "greate:milling/integration/gtceu/macerator/macerate_mv_canner",
        "greate:milling/integration/gtceu/macerator/macerate_mv_chemical_reactor",
        "greate:milling/integration/gtceu/macerator/macerate_mv_combustion",
        "greate:milling/integration/gtceu/macerator/macerate_mv_diode",
        "greate:milling/integration/gtceu/macerator/macerate_mv_distillery",
        "greate:milling/integration/gtceu/macerator/macerate_mv_electric_piston",
        "greate:milling/integration/gtceu/macerator/macerate_mv_electrolyzer",
        "greate:milling/integration/gtceu/macerator/macerate_mv_energy_output_hatch",
        "greate:milling/integration/gtceu/macerator/macerate_mv_extractor",
        "greate:milling/integration/gtceu/macerator/macerate_mv_extruder",
        "greate:milling/integration/gtceu/macerator/macerate_mv_food_oven",
        "greate:milling/integration/gtceu/macerator/macerate_mv_forming_press",
        "greate:milling/integration/gtceu/macerator/macerate_mv_gas_pressurizer",
        "greate:milling/integration/gtceu/macerator/macerate_mv_hermetic_casing",
        "greate:milling/integration/gtceu/macerator/macerate_mv_input_hatch",
        "greate:milling/integration/gtceu/macerator/macerate_mv_laser_engraver",
        "greate:milling/integration/gtceu/macerator/macerate_mv_macerator",
        "greate:milling/integration/gtceu/macerator/macerate_mv_machine_casing",
        "greate:milling/integration/gtceu/macerator/macerate_mv_miner",
        "greate:milling/integration/gtceu/macerator/macerate_mv_mixer",
        "greate:milling/integration/gtceu/macerator/macerate_mv_ore_washer",
        "greate:milling/integration/gtceu/macerator/macerate_mv_output_bus",
        "greate:milling/integration/gtceu/macerator/macerate_mv_output_hatch",
        "greate:milling/integration/gtceu/macerator/macerate_mv_rock_crusher",
        "greate:milling/integration/gtceu/macerator/macerate_mv_scanner",
        "greate:milling/integration/gtceu/macerator/macerate_mv_sifter",
        "greate:milling/integration/gtceu/macerator/macerate_mv_super_chest",
        "greate:milling/integration/gtceu/macerator/macerate_mv_transformer_1_a",
        "greate:milling/integration/gtceu/macerator/macerate_mv_voltage_coil",
        "greate:milling/integration/gtceu/macerator/macerate_oxygen_gear",
        "greate:milling/integration/gtceu/macerator/macerate_oxygen_sensor",
        "greate:milling/integration/gtceu/macerator/macerate_pyrolyse_oven",
        "greate:milling/integration/gtceu/macerator/macerate_redstone_relay",
        "greate:milling/integration/gtceu/macerator/macerate_speaker",
        "greate:milling/integration/gtceu/macerator/macerate_stainless_steel_cogwheel",
        "greate:milling/integration/gtceu/macerator/macerate_tag_storage_bus",
        "greate:milling/integration/gtceu/macerator/macerate_wired_modem",
        "greate:milling/integration/immersive_aircraft/recycling/enhanced_propeller",
        "greate:milling/integration/tfg/minecraft/macerator/recycling/elytra",
        "greate:milling/integration/tfg/recycling/aluminium_hull_reinforcement",
        "greate:pressing/refined_aluminium_to_dust",
        "gtceu:shaped/small_dust_assembling_aluminium",
        "gtceu:shapeless/centrifuged_ore_to_dust_aluminium",
        "tfg:ae_transform/aluminium_dust_from_impure",
        "tfg:instant_barrel/aluminium_dust_from_impure",
        "tfg:instant_barrel/aluminium_dust_from_pure",
        "tfg:splashing/aluminium_dust_from_impure_water",
        "tfg:splashing/aluminium_dust_from_pure_distilled",
        "tfg:vi/centrifuge/aluminium_dust_from_impure",
        "tfg:vi/centrifuge/aluminium_dust_from_pure"
      ],
      "recipe_examples_truncated": true,
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:dusts"
        },
        "processing_in": {
          "values": [
            "greate:mixing",
            "gtceu:crafting_shaped_strict"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:aluminium_fluid_cell",
      "namespace": "gtceu",
      "display_name": "Empty Aluminium Cell",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfg:insulating_container"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 1,
        "kubejs:shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_fluid_cell",
        "gtceu:shapeless/cell_nbt_aluminium",
        "tfg:gas_tank"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/cell_nbt_aluminium"
      ],
      "model_parents": [
        "item/aluminium_fluid_cell",
        "item/default"
      ],
      "creative_tabs": [
        "gtceu:item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "§aSafely contains §6hot§a, §bcold§a, and §elighter-than-air§a items and fluids.§r"
        },
        {
          "source": "runtime-tooltip",
          "text": "§9Fluid Capacity: §f32,000 mB"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
      "id": "gtceu:aluminium_foil",
      "namespace": "gtceu",
      "display_name": "Aluminium Foil",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:foils",
        "forge:foils/aluminium"
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
        "createaddition:rolling": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_foil",
        "gtceu:shapeless/fine_wire_aluminium"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/foil_aluminium",
        "tfg:rolling/aluminium_foil"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
            "greate:milling"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:aluminium_frame",
      "namespace": "gtceu",
      "display_name": "Aluminium Frame",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:frames",
        "forge:frames/aluminium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "create:mechanical_crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "create:mechanical_crafting": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "framedblocks:framed_reinforcement",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_frame",
        "gtceu:shaped/casing_aluminium_frostproof",
        "tfg:immersive_aircraft/mechanical_crafter/bamboo_hopper"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/frame_aluminium"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_frame",
        "block_tags": [
          "forge:frames",
          "forge:frames/aluminium",
          "forge:mineable/wrench",
          "forge:slow_walkable_blocks",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:aluminium_gear",
      "namespace": "gtceu",
      "display_name": "Aluminium Gear",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:gears",
        "forge:gears/aluminium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "greate:milling": 1,
        "kubejs:shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_gear",
        "greate:shaped/stainless_steel_millstone",
        "gtceu:shaped/diesel_generator_mv"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/gear_aluminium"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Heavy ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 4,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
      "id": "gtceu:aluminium_hex_cable",
      "namespace": "gtceu",
      "display_name": "16x Aluminium Cable",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:hex_cables",
        "forge:hex_cables/aluminium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 2,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_hex_cable",
        "gtceu:shaped/ev_16a_energy_converter"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_hex_cable",
        "block_tags": [
          "forge:hex_cables",
          "forge:hex_cables/aluminium",
          "forge:mineable/wire_cutter",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a2,048 §a(§5EV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e16"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c1§7 EU-Volt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:aluminium_hex_wire",
      "namespace": "gtceu",
      "display_name": "16x Aluminium Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:hex_wires",
        "forge:hex_wires/aluminium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 2
      },
      "recipe_ingredient_count": 3,
      "recipe_output_count": 2,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_hex_wire",
        "gtceu:shaped/ev_battery_buffer_16x",
        "gtceu:shapeless/aluminium_wire_wire_gt_hex_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/aluminium_wire_wire_gt_octal_doubling",
        "gtceu:shapeless/aluminium_wire_wire_gt_quadruple_quadrupling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_hex_wire",
        "block_tags": [
          "forge:hex_wires",
          "forge:hex_wires/aluminium",
          "forge:mineable/wire_cutter",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a2,048 §a(§5EV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e16"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c3§7 EU-Volt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:aluminium_huge_fluid_pipe",
      "namespace": "gtceu",
      "display_name": "Huge Aluminium Fluid Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:huge_fluid_pipes",
        "forge:huge_fluid_pipes/aluminium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "kubejs:shaped"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "kubejs:shaped": 1
      },
      "recipe_production_by_type": {
        "crafting_shaped": 1
      },
      "recipe_ingredient_count": 2,
      "recipe_output_count": 1,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_huge_fluid_pipe",
        "tfg:shaped/steam_turbine_mv"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/huge_aluminium_pipe"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_huge_fluid_pipe",
        "block_tags": [
          "forge:huge_fluid_pipes",
          "forge:huge_fluid_pipes/aluminium",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bTransfer Rate: §f4800 mB/t"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature Limit: §f1,166 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
        },
        "processing_in": {
          "values": [
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
      "id": "gtceu:aluminium_indicator",
      "namespace": "gtceu",
      "display_name": "Aluminium Surface Rock",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:surface_rocks",
        "forge:surface_rocks/aluminium"
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
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_indicator",
        "block_tags": [
          "forge:surface_rocks",
          "forge:surface_rocks/aluminium",
          "tfc:can_be_ice_piled",
          "tfc:can_be_snow_piled",
          "tfccanes:not_slowed_with_cane",
          "tfg:dust_ore_indicators",
          "tfg:not_slowed_with_snowshoes"
        ],
        "requires_correct_tool": false
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
      "id": "gtceu:aluminium_ingot",
      "namespace": "gtceu",
      "display_name": "Aluminium Ingot",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "ae2:metal_ingots",
        "balm:ingots",
        "forge:ingots",
        "forge:ingots/aluminium",
        "tfc:pileable_ingots"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "createaddition:rolling",
        "greate:compacting",
        "greate:milling",
        "vintageimprovements:coiling",
        "vintageimprovements:polishing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "createaddition:rolling": 1,
        "greate:compacting": 1,
        "greate:milling": 1,
        "vintageimprovements:coiling": 1,
        "vintageimprovements:polishing": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 6,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:compacting/aluminium_block",
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_ingot",
        "gtceu:shaped/stick_aluminium",
        "tfg:rolling/aluminium_plate",
        "tfg:vi/coiling/aluminium_single_wire",
        "tfg:vi/lathe/aluminium_to_rod"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Large"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "form": {
          "value": "ingot",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ingots"
        },
        "processing_in": {
          "values": [
            "crafting",
            "createaddition:rolling",
            "greate:compacting",
            "greate:milling",
            "vintageimprovements:coiling",
            "vintageimprovements:polishing"
          ],
          "mode": "add",
          "confidence": 1,
          "source": "rule:processing_in_from_recipes"
        }
      }
    },
    {
      "id": "gtceu:aluminium_large_fluid_pipe",
      "namespace": "gtceu",
      "display_name": "Large Aluminium Fluid Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:large_fluid_pipes",
        "forge:large_fluid_pipes/aluminium"
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
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_large_fluid_pipe"
      ],
      "recipe_output_examples": [
        "tfg:temp/large_fluid_pipe_aluminium"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_large_fluid_pipe",
        "block_tags": [
          "forge:large_fluid_pipes",
          "forge:large_fluid_pipes/aluminium",
          "forge:mineable/wrench",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bTransfer Rate: §f2400 mB/t"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature Limit: §f1,166 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:aluminium_nonuple_fluid_pipe",
      "namespace": "gtceu",
      "display_name": "Nonuple Aluminium Fluid Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:nonuple_fluid_pipes",
        "forge:nonuple_fluid_pipes/aluminium"
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
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_nonuple_fluid_pipe"
      ],
      "recipe_output_examples": [
        "gtceu:shaped/nonuple_aluminium_pipe"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_nonuple_fluid_pipe",
        "block_tags": [
          "forge:mineable/wrench",
          "forge:nonuple_fluid_pipes",
          "forge:nonuple_fluid_pipes/aluminium",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bTransfer Rate: §f400 mB/t"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature Limit: §f1,166 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eChannels: §f9"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Medium ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 16,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:aluminium_normal_fluid_pipe",
      "namespace": "gtceu",
      "display_name": "Normal Aluminium Fluid Pipe",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:normal_fluid_pipes",
        "forge:normal_fluid_pipes/aluminium"
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
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_normal_fluid_pipe"
      ],
      "recipe_output_examples": [
        "tfg:temp/normal_fluid_pipe_aluminium"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_normal_fluid_pipe",
        "block_tags": [
          "forge:mineable/wrench",
          "forge:normal_fluid_pipes",
          "forge:normal_fluid_pipes/aluminium",
          "gtceu:mineable/pickaxe_or_wrench",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "§bTransfer Rate: §f1200 mB/t"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cTemperature Limit: §f1,166 K"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Hold SHIFT to show Fluid Containment Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:aluminium_nugget",
      "namespace": "gtceu",
      "display_name": "Aluminium Nugget",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:nuggets",
        "forge:nuggets",
        "forge:nuggets/aluminium"
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
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_nugget"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_item"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
          "confidence": 1,
          "source": "rule:mod_namespace"
        },
        "is_stackable": {
          "value": true,
          "confidence": 1,
          "source": "rule:is_stackable_from_component"
        },
        "form": {
          "value": "nugget",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:nuggets"
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
      "id": "gtceu:aluminium_octal_cable",
      "namespace": "gtceu",
      "display_name": "8x Aluminium Cable",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:octal_cables",
        "forge:octal_cables/aluminium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 2,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_octal_cable",
        "gtceu:shaped/ev_8a_energy_converter"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_octal_cable",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:octal_cables",
          "forge:octal_cables/aluminium",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a2,048 §a(§5EV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e8"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c1§7 EU-Volt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:aluminium_octal_wire",
      "namespace": "gtceu",
      "display_name": "8x Aluminium Wire",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:octal_wires",
        "forge:octal_wires/aluminium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 1,
        "crafting_shapeless": 2,
        "greate:milling": 1
      },
      "recipe_production_by_type": {
        "crafting_shapeless": 3
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_octal_wire",
        "gtceu:shaped/ev_battery_buffer_8x",
        "gtceu:shapeless/aluminium_wire_wire_gt_octal_doubling",
        "gtceu:shapeless/aluminium_wire_wire_gt_octal_splitting"
      ],
      "recipe_output_examples": [
        "gtceu:shapeless/aluminium_wire_wire_gt_double_quadrupling",
        "gtceu:shapeless/aluminium_wire_wire_gt_hex_splitting",
        "gtceu:shapeless/aluminium_wire_wire_gt_quadruple_doubling"
      ],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_octal_wire",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:octal_wires",
          "forge:octal_wires/aluminium",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a2,048 §a(§5EV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e8"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c3§7 EU-Volt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Normal"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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
      "id": "gtceu:aluminium_ore",
      "namespace": "gtceu",
      "display_name": "Aluminium Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "balm:ores",
        "c:hidden_from_recipe_viewers",
        "forge:ores",
        "forge:ores/aluminium",
        "forge:ores_in_ground/stone"
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
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_ore_to_crushed_ore"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_block"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_ore",
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
          "value": "aluminium",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id aluminium_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_common_tag",
          "rationale": "tag forge:ores"
        },
        "processing_in": {
          "values": [
            "greate:milling"
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
      "id": "gtceu:aluminium_quadruple_cable",
      "namespace": "gtceu",
      "display_name": "4x Aluminium Cable",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:quadruple_cables",
        "forge:quadruple_cables/aluminium"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 4,
        "greate:milling": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 5,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerator/macerate_aluminium_quadruple_cable",
        "gtceu:shaped/ev_4a_energy_converter",
        "gtceu:shaped/ev_arc_furnace",
        "gtceu:shaped/ev_diode",
        "tfg:shaped/ev_aircraft_engine"
      ],
      "recipe_output_examples": [],
      "model_parents": [],
      "creative_tabs": [
        "gtceu:material_pipe"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "gtceu:aluminium_quadruple_cable",
        "block_tags": [
          "forge:mineable/wire_cutter",
          "forge:quadruple_cables",
          "forge:quadruple_cables/aluminium",
          "gtceu:mineable/pickaxe_or_wire_cutter",
          "minecraft:needs_stone_tool"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Al"
        },
        {
          "source": "runtime-tooltip",
          "text": "§aMax Voltage:§r §a2,048 §a(§5EV§a)"
        },
        {
          "source": "runtime-tooltip",
          "text": "§eMax Amperage:§r §e4"
        },
        {
          "source": "runtime-tooltip",
          "text": "§cLoss/Meter/Ampere:§r §c1§7 EU-Volt"
        },
        {
          "source": "runtime-tooltip",
          "text": "Hold SHIFT to show Tool Info"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "GregTech"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "gtceu",
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
        "required_tool_tier": {
          "value": "stone",
          "confidence": 1,
          "source": "rule:required_tool_tier_from_block_tag",
          "rationale": "tag minecraft:needs_stone_tool"
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