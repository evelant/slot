# Items to classify
{
  "items": [
    {
      "id": "tfc:rock/smooth/phyllite",
      "namespace": "tfc",
      "display_name": "Smooth Phyllite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:smooth_stone",
        "tfc:metamorphic_items",
        "tfc:rock/smooth",
        "tfg:brick_index",
        "tfg:interaction/smooth_brick",
        "tfg:stone_composition/metamorphic",
        "tfg:stone_types/phyllite",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped",
        "kubejs:shapeless",
        "stonecutting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 6,
        "greate:milling": 1,
        "kubejs:shaped": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 11,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "create:sandpaper_polishing": 4,
        "stonecutting": 1,
        "tfc:chisel": 2,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 23,
      "recipe_output_count": 8,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_metamorphic",
        "greate:shaped/steel_millstone",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "rnr:crafting/flagstone/phyllite",
        "tfc:crafting/quern",
        "tfc:crafting/vanilla/redstone/repeater",
        "tfc:stonecutting/rock/phyllite_smooth_slab",
        "tfc:stonecutting/rock/phyllite_smooth_stairs",
        "tfc:stonecutting/rock/phyllite_smooth_wall",
        "tfg:create/shaped/schematicannon",
        "tfg:shaped/comparator_certus",
        "tfg:shaped/comparator_nether_quartz",
        "tfg:shaped/comparator_quartzite",
        "tfg:stonecutter/tfc_rock_bricks_phyllite",
        "tfg:stonecutter/tfc_rock_bricks_phyllite_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_phyllite_stairs",
        "tfg:stonecutter/tfc_rock_bricks_phyllite_wall",
        "tfg:stonecutter/tfc_rock_chiseled_phyllite",
        "tfg:stonecutter/tfc_rock_smooth_phyllite_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_phyllite_stairs",
        "tfg:stonecutter/tfc_rock_smooth_phyllite_wall"
      ],
      "recipe_output_examples": [
        "tfc:chisel/smooth/phyllite_hardened_smooth",
        "tfc:chisel/smooth/phyllite_smooth",
        "tfc:crafting/rock/phyllite_smooth",
        "tfg:polishing/phyllite_brick_to_polished",
        "tfg:polishing/phyllite_cracked_brick_to_polished",
        "tfg:polishing/phyllite_mossy_brick_to_polished",
        "tfg:polishing/phyllite_raw_to_polished",
        "tfg:stonecutter/tfc_rock_smooth_phyllite"
      ],
      "model_parents": [
        "item/rock/smooth/phyllite",
        "block/rock/smooth/phyllite",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/phyllite"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/phyllite",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
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
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "firmalife:oven_insulation",
          "forge:smooth_stone",
          "minecraft:mineable/pickaxe",
          "tfc:bloomery_insulation",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfc:creeping_stone_plantable_on",
          "tfc:forge_insulation",
          "tfc:rock/smooth"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
            "kubejs:shaped",
            "kubejs:shapeless",
            "stonecutting",
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "tfc:rock/smooth/phyllite_slab",
      "namespace": "tfc",
      "display_name": "Smooth Phyllite Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:smooth_stone_slab",
        "forge:smooth_stone_slabs",
        "minecraft:slabs",
        "tfc:metamorphic_items",
        "tfg:brick_index",
        "tfg:rock_slabs",
        "tfg:stone_composition/metamorphic_half",
        "tfg:stone_types/phyllite_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "greate:milling": 1,
        "stonecutting": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 3,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "exposure:camera_stand",
        "greate:milling/integration/tfg/macerate_metamorphic_half",
        "tfc:crafting/vanilla/armor_stand",
        "tfg:stonecutter/tfc_rock_bricks_phyllite_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfc:chisel/slab/smooth_phyllite_slab",
        "tfc:stonecutting/rock/phyllite_smooth_slab",
        "tfg:stonecutter/tfc_rock_smooth_phyllite_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_phyllite_slab_slab_to_slab"
      ],
      "model_parents": [
        "item/rock/smooth/phyllite_slab",
        "block/rock/smooth/phyllite_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/phyllite_slab"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/phyllite_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "forge:smooth_stone_slab",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_slabs"
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
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
            "crafting",
            "greate:milling",
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
      "id": "tfc:rock/smooth/phyllite_stairs",
      "namespace": "tfc",
      "display_name": "Smooth Phyllite Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:stairs",
        "tfc:metamorphic_items",
        "tfg:brick_index",
        "tfg:rock_stairs",
        "tfg:stone_composition/metamorphic",
        "tfg:stone_types/phyllite"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 8
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 9,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_metamorphic",
        "tfg:stonecutter/tfc_rock_bricks_phyllite",
        "tfg:stonecutter/tfc_rock_bricks_phyllite_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_phyllite_stairs",
        "tfg:stonecutter/tfc_rock_bricks_phyllite_wall",
        "tfg:stonecutter/tfc_rock_chiseled_phyllite",
        "tfg:stonecutter/tfc_rock_smooth_phyllite",
        "tfg:stonecutter/tfc_rock_smooth_phyllite_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_phyllite_wall"
      ],
      "recipe_output_examples": [
        "tfc:chisel/stair/smooth_phyllite_stairs",
        "tfc:stonecutting/rock/phyllite_smooth_stairs",
        "tfg:stonecutter/tfc_rock_smooth_phyllite_stairs"
      ],
      "model_parents": [
        "item/rock/smooth/phyllite_stairs",
        "block/rock/smooth/phyllite_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/phyllite_stairs"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/phyllite_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_stairs"
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
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "tfc:rock/smooth/phyllite_wall",
      "namespace": "tfc",
      "display_name": "Smooth Phyllite Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:walls",
        "tfc:metamorphic_items",
        "tfg:brick_index",
        "tfg:rock_walls",
        "tfg:stone_composition/metamorphic_half",
        "tfg:stone_types/phyllite"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 8
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 9,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_metamorphic_half",
        "tfg:stonecutter/tfc_rock_bricks_phyllite",
        "tfg:stonecutter/tfc_rock_bricks_phyllite_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_phyllite_stairs",
        "tfg:stonecutter/tfc_rock_bricks_phyllite_wall",
        "tfg:stonecutter/tfc_rock_chiseled_phyllite",
        "tfg:stonecutter/tfc_rock_smooth_phyllite",
        "tfg:stonecutter/tfc_rock_smooth_phyllite_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_phyllite_stairs"
      ],
      "recipe_output_examples": [
        "tfc:kjs/davp5qowb3pa7kcd1h4rsbsvy",
        "tfc:stonecutting/rock/phyllite_smooth_wall",
        "tfg:stonecutter/tfc_rock_smooth_phyllite_wall"
      ],
      "model_parents": [
        "item/rock/smooth/phyllite_wall",
        "block/rock/smooth/phyllite_wall_inventory",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/phyllite_wall"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/phyllite_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:walls",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_walls"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
          "value": "wall",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:walls"
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
      "id": "tfc:rock/smooth/quartzite",
      "namespace": "tfc",
      "display_name": "Smooth Quartzite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:smooth_stone",
        "tfc:metamorphic_items",
        "tfc:rock/smooth",
        "tfg:brick_index",
        "tfg:interaction/smooth_brick",
        "tfg:stone_composition/metamorphic",
        "tfg:stone_types/quartzite",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped",
        "kubejs:shapeless",
        "stonecutting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 6,
        "greate:milling": 1,
        "kubejs:shaped": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 11,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "create:sandpaper_polishing": 4,
        "stonecutting": 1,
        "tfc:chisel": 2,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 23,
      "recipe_output_count": 8,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_metamorphic",
        "greate:shaped/steel_millstone",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "rnr:crafting/flagstone/quartzite",
        "tfc:crafting/quern",
        "tfc:crafting/vanilla/redstone/repeater",
        "tfc:stonecutting/rock/quartzite_smooth_slab",
        "tfc:stonecutting/rock/quartzite_smooth_stairs",
        "tfc:stonecutting/rock/quartzite_smooth_wall",
        "tfg:create/shaped/schematicannon",
        "tfg:shaped/comparator_certus",
        "tfg:shaped/comparator_nether_quartz",
        "tfg:shaped/comparator_quartzite",
        "tfg:stonecutter/tfc_rock_bricks_quartzite",
        "tfg:stonecutter/tfc_rock_bricks_quartzite_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_quartzite_stairs",
        "tfg:stonecutter/tfc_rock_bricks_quartzite_wall",
        "tfg:stonecutter/tfc_rock_chiseled_quartzite",
        "tfg:stonecutter/tfc_rock_smooth_quartzite_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_quartzite_stairs",
        "tfg:stonecutter/tfc_rock_smooth_quartzite_wall"
      ],
      "recipe_output_examples": [
        "tfc:chisel/smooth/quartzite_hardened_smooth",
        "tfc:chisel/smooth/quartzite_smooth",
        "tfc:crafting/rock/quartzite_smooth",
        "tfg:polishing/quartzite_brick_to_polished",
        "tfg:polishing/quartzite_cracked_brick_to_polished",
        "tfg:polishing/quartzite_mossy_brick_to_polished",
        "tfg:polishing/quartzite_raw_to_polished",
        "tfg:stonecutter/tfc_rock_smooth_quartzite"
      ],
      "model_parents": [
        "item/rock/smooth/quartzite",
        "block/rock/smooth/quartzite",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/quartzite"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/quartzite",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
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
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "firmalife:oven_insulation",
          "forge:smooth_stone",
          "minecraft:mineable/pickaxe",
          "tfc:bloomery_insulation",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfc:creeping_stone_plantable_on",
          "tfc:forge_insulation",
          "tfc:rock/smooth"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
            "kubejs:shaped",
            "kubejs:shapeless",
            "stonecutting",
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "tfc:rock/smooth/quartzite_slab",
      "namespace": "tfc",
      "display_name": "Smooth Quartzite Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:smooth_stone_slab",
        "forge:smooth_stone_slabs",
        "minecraft:slabs",
        "tfc:metamorphic_items",
        "tfg:brick_index",
        "tfg:rock_slabs",
        "tfg:stone_composition/metamorphic_half",
        "tfg:stone_types/quartzite_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "greate:milling": 1,
        "stonecutting": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 3,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "exposure:camera_stand",
        "greate:milling/integration/tfg/macerate_metamorphic_half",
        "tfc:crafting/vanilla/armor_stand",
        "tfg:stonecutter/tfc_rock_bricks_quartzite_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfc:chisel/slab/smooth_quartzite_slab",
        "tfc:stonecutting/rock/quartzite_smooth_slab",
        "tfg:stonecutter/tfc_rock_smooth_quartzite_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_quartzite_slab_slab_to_slab"
      ],
      "model_parents": [
        "item/rock/smooth/quartzite_slab",
        "block/rock/smooth/quartzite_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/quartzite_slab"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/quartzite_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "forge:smooth_stone_slab",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_slabs"
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
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
            "crafting",
            "greate:milling",
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
      "id": "tfc:rock/smooth/quartzite_stairs",
      "namespace": "tfc",
      "display_name": "Smooth Quartzite Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:stairs",
        "tfc:metamorphic_items",
        "tfg:brick_index",
        "tfg:rock_stairs",
        "tfg:stone_composition/metamorphic",
        "tfg:stone_types/quartzite"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 8
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 9,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_metamorphic",
        "tfg:stonecutter/tfc_rock_bricks_quartzite",
        "tfg:stonecutter/tfc_rock_bricks_quartzite_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_quartzite_stairs",
        "tfg:stonecutter/tfc_rock_bricks_quartzite_wall",
        "tfg:stonecutter/tfc_rock_chiseled_quartzite",
        "tfg:stonecutter/tfc_rock_smooth_quartzite",
        "tfg:stonecutter/tfc_rock_smooth_quartzite_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_quartzite_wall"
      ],
      "recipe_output_examples": [
        "tfc:chisel/stair/smooth_quartzite_stairs",
        "tfc:stonecutting/rock/quartzite_smooth_stairs",
        "tfg:stonecutter/tfc_rock_smooth_quartzite_stairs"
      ],
      "model_parents": [
        "item/rock/smooth/quartzite_stairs",
        "block/rock/smooth/quartzite_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/quartzite_stairs"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/quartzite_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_stairs"
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
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "tfc:rock/smooth/quartzite_wall",
      "namespace": "tfc",
      "display_name": "Smooth Quartzite Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:walls",
        "tfc:metamorphic_items",
        "tfg:brick_index",
        "tfg:rock_walls",
        "tfg:stone_composition/metamorphic_half",
        "tfg:stone_types/quartzite"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 8
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 9,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_metamorphic_half",
        "tfg:stonecutter/tfc_rock_bricks_quartzite",
        "tfg:stonecutter/tfc_rock_bricks_quartzite_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_quartzite_stairs",
        "tfg:stonecutter/tfc_rock_bricks_quartzite_wall",
        "tfg:stonecutter/tfc_rock_chiseled_quartzite",
        "tfg:stonecutter/tfc_rock_smooth_quartzite",
        "tfg:stonecutter/tfc_rock_smooth_quartzite_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_quartzite_stairs"
      ],
      "recipe_output_examples": [
        "tfc:kjs/bob6214t7v5dt8a5fz8gvrw2o",
        "tfc:stonecutting/rock/quartzite_smooth_wall",
        "tfg:stonecutter/tfc_rock_smooth_quartzite_wall"
      ],
      "model_parents": [
        "item/rock/smooth/quartzite_wall",
        "block/rock/smooth/quartzite_wall_inventory",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/quartzite_wall"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/quartzite_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:walls",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_walls"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
          "value": "wall",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:walls"
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
      "id": "tfc:rock/smooth/rhyolite",
      "namespace": "tfc",
      "display_name": "Smooth Rhyolite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:smooth_stone",
        "tfc:igneous_extrusive_items",
        "tfc:rock/smooth",
        "tfg:brick_index",
        "tfg:interaction/smooth_brick",
        "tfg:stone_composition/igneous_felsic",
        "tfg:stone_types/rhyolite",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped",
        "kubejs:shapeless",
        "stonecutting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 6,
        "greate:milling": 1,
        "kubejs:shaped": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 11,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "create:sandpaper_polishing": 4,
        "stonecutting": 1,
        "tfc:chisel": 2,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 23,
      "recipe_output_count": 8,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_felsic",
        "greate:shaped/steel_millstone",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "rnr:crafting/flagstone/rhyolite",
        "tfc:crafting/quern",
        "tfc:crafting/vanilla/redstone/repeater",
        "tfc:stonecutting/rock/rhyolite_smooth_slab",
        "tfc:stonecutting/rock/rhyolite_smooth_stairs",
        "tfc:stonecutting/rock/rhyolite_smooth_wall",
        "tfg:create/shaped/schematicannon",
        "tfg:shaped/comparator_certus",
        "tfg:shaped/comparator_nether_quartz",
        "tfg:shaped/comparator_quartzite",
        "tfg:stonecutter/tfc_rock_bricks_rhyolite",
        "tfg:stonecutter/tfc_rock_bricks_rhyolite_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_rhyolite_stairs",
        "tfg:stonecutter/tfc_rock_bricks_rhyolite_wall",
        "tfg:stonecutter/tfc_rock_chiseled_rhyolite",
        "tfg:stonecutter/tfc_rock_smooth_rhyolite_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_rhyolite_stairs",
        "tfg:stonecutter/tfc_rock_smooth_rhyolite_wall"
      ],
      "recipe_output_examples": [
        "tfc:chisel/smooth/rhyolite_hardened_smooth",
        "tfc:chisel/smooth/rhyolite_smooth",
        "tfc:crafting/rock/rhyolite_smooth",
        "tfg:polishing/rhyolite_brick_to_polished",
        "tfg:polishing/rhyolite_cracked_brick_to_polished",
        "tfg:polishing/rhyolite_mossy_brick_to_polished",
        "tfg:polishing/rhyolite_raw_to_polished",
        "tfg:stonecutter/tfc_rock_smooth_rhyolite"
      ],
      "model_parents": [
        "item/rock/smooth/rhyolite",
        "block/rock/smooth/rhyolite",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/rhyolite"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/rhyolite",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
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
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "firmalife:oven_insulation",
          "forge:smooth_stone",
          "minecraft:mineable/pickaxe",
          "tfc:bloomery_insulation",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfc:creeping_stone_plantable_on",
          "tfc:forge_insulation",
          "tfc:rock/smooth"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Igneous Extrusive"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
            "kubejs:shaped",
            "kubejs:shapeless",
            "stonecutting",
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "tfc:rock/smooth/rhyolite_slab",
      "namespace": "tfc",
      "display_name": "Smooth Rhyolite Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:smooth_stone_slab",
        "forge:smooth_stone_slabs",
        "minecraft:slabs",
        "tfc:igneous_extrusive_items",
        "tfg:brick_index",
        "tfg:rock_slabs",
        "tfg:stone_composition/igneous_felsic_half",
        "tfg:stone_types/rhyolite_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "greate:milling": 1,
        "stonecutting": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 3,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "exposure:camera_stand",
        "greate:milling/integration/tfg/macerate_igneous_felsic_half",
        "tfc:crafting/vanilla/armor_stand",
        "tfg:stonecutter/tfc_rock_bricks_rhyolite_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfc:chisel/slab/smooth_rhyolite_slab",
        "tfc:stonecutting/rock/rhyolite_smooth_slab",
        "tfg:stonecutter/tfc_rock_smooth_rhyolite_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_rhyolite_slab_slab_to_slab"
      ],
      "model_parents": [
        "item/rock/smooth/rhyolite_slab",
        "block/rock/smooth/rhyolite_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/rhyolite_slab"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/rhyolite_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "forge:smooth_stone_slab",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_slabs"
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
          "text": "Igneous Extrusive"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
            "crafting",
            "greate:milling",
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
      "id": "tfc:rock/smooth/rhyolite_stairs",
      "namespace": "tfc",
      "display_name": "Smooth Rhyolite Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:stairs",
        "tfc:igneous_extrusive_items",
        "tfg:brick_index",
        "tfg:rock_stairs",
        "tfg:stone_composition/igneous_felsic",
        "tfg:stone_types/rhyolite"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 8
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 9,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_felsic",
        "tfg:stonecutter/tfc_rock_bricks_rhyolite",
        "tfg:stonecutter/tfc_rock_bricks_rhyolite_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_rhyolite_stairs",
        "tfg:stonecutter/tfc_rock_bricks_rhyolite_wall",
        "tfg:stonecutter/tfc_rock_chiseled_rhyolite",
        "tfg:stonecutter/tfc_rock_smooth_rhyolite",
        "tfg:stonecutter/tfc_rock_smooth_rhyolite_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_rhyolite_wall"
      ],
      "recipe_output_examples": [
        "tfc:chisel/stair/smooth_rhyolite_stairs",
        "tfc:stonecutting/rock/rhyolite_smooth_stairs",
        "tfg:stonecutter/tfc_rock_smooth_rhyolite_stairs"
      ],
      "model_parents": [
        "item/rock/smooth/rhyolite_stairs",
        "block/rock/smooth/rhyolite_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/rhyolite_stairs"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/rhyolite_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_stairs"
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
          "text": "Igneous Extrusive"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "tfc:rock/smooth/rhyolite_wall",
      "namespace": "tfc",
      "display_name": "Smooth Rhyolite Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:walls",
        "tfc:igneous_extrusive_items",
        "tfg:brick_index",
        "tfg:rock_walls",
        "tfg:stone_composition/igneous_felsic_half",
        "tfg:stone_types/rhyolite"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 8
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 9,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_igneous_felsic_half",
        "tfg:stonecutter/tfc_rock_bricks_rhyolite",
        "tfg:stonecutter/tfc_rock_bricks_rhyolite_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_rhyolite_stairs",
        "tfg:stonecutter/tfc_rock_bricks_rhyolite_wall",
        "tfg:stonecutter/tfc_rock_chiseled_rhyolite",
        "tfg:stonecutter/tfc_rock_smooth_rhyolite",
        "tfg:stonecutter/tfc_rock_smooth_rhyolite_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_rhyolite_stairs"
      ],
      "recipe_output_examples": [
        "tfc:kjs/3ye21t45gs070edp4zwv997ot",
        "tfc:stonecutting/rock/rhyolite_smooth_wall",
        "tfg:stonecutter/tfc_rock_smooth_rhyolite_wall"
      ],
      "model_parents": [
        "item/rock/smooth/rhyolite_wall",
        "block/rock/smooth/rhyolite_wall_inventory",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/rhyolite_wall"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/rhyolite_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:walls",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_walls"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Igneous Extrusive"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
          "value": "wall",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:walls"
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
      "id": "tfc:rock/smooth/schist",
      "namespace": "tfc",
      "display_name": "Smooth Schist",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:smooth_stone",
        "tfc:metamorphic_items",
        "tfc:rock/smooth",
        "tfg:brick_index",
        "tfg:interaction/smooth_brick",
        "tfg:stone_composition/metamorphic",
        "tfg:stone_types/schist",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped",
        "kubejs:shapeless",
        "stonecutting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 6,
        "greate:milling": 1,
        "kubejs:shaped": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 11,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "create:sandpaper_polishing": 4,
        "stonecutting": 1,
        "tfc:chisel": 2,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 23,
      "recipe_output_count": 8,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_metamorphic",
        "greate:shaped/steel_millstone",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "rnr:crafting/flagstone/schist",
        "tfc:crafting/quern",
        "tfc:crafting/vanilla/redstone/repeater",
        "tfc:stonecutting/rock/schist_smooth_slab",
        "tfc:stonecutting/rock/schist_smooth_stairs",
        "tfc:stonecutting/rock/schist_smooth_wall",
        "tfg:create/shaped/schematicannon",
        "tfg:shaped/comparator_certus",
        "tfg:shaped/comparator_nether_quartz",
        "tfg:shaped/comparator_quartzite",
        "tfg:stonecutter/tfc_rock_bricks_schist",
        "tfg:stonecutter/tfc_rock_bricks_schist_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_schist_stairs",
        "tfg:stonecutter/tfc_rock_bricks_schist_wall",
        "tfg:stonecutter/tfc_rock_chiseled_schist",
        "tfg:stonecutter/tfc_rock_smooth_schist_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_schist_stairs",
        "tfg:stonecutter/tfc_rock_smooth_schist_wall"
      ],
      "recipe_output_examples": [
        "tfc:chisel/smooth/schist_hardened_smooth",
        "tfc:chisel/smooth/schist_smooth",
        "tfc:crafting/rock/schist_smooth",
        "tfg:polishing/schist_brick_to_polished",
        "tfg:polishing/schist_cracked_brick_to_polished",
        "tfg:polishing/schist_mossy_brick_to_polished",
        "tfg:polishing/schist_raw_to_polished",
        "tfg:stonecutter/tfc_rock_smooth_schist"
      ],
      "model_parents": [
        "item/rock/smooth/schist",
        "block/rock/smooth/schist",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/schist"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/schist",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
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
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "firmalife:oven_insulation",
          "forge:smooth_stone",
          "minecraft:mineable/pickaxe",
          "tfc:bloomery_insulation",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfc:creeping_stone_plantable_on",
          "tfc:forge_insulation",
          "tfc:rock/smooth"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
            "kubejs:shaped",
            "kubejs:shapeless",
            "stonecutting",
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "tfc:rock/smooth/schist_slab",
      "namespace": "tfc",
      "display_name": "Smooth Schist Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:smooth_stone_slab",
        "forge:smooth_stone_slabs",
        "minecraft:slabs",
        "tfc:metamorphic_items",
        "tfg:brick_index",
        "tfg:rock_slabs",
        "tfg:stone_composition/metamorphic_half",
        "tfg:stone_types/schist_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "greate:milling": 1,
        "stonecutting": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 3,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "exposure:camera_stand",
        "greate:milling/integration/tfg/macerate_metamorphic_half",
        "tfc:crafting/vanilla/armor_stand",
        "tfg:stonecutter/tfc_rock_bricks_schist_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfc:chisel/slab/smooth_schist_slab",
        "tfc:stonecutting/rock/schist_smooth_slab",
        "tfg:stonecutter/tfc_rock_smooth_schist_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_schist_slab_slab_to_slab"
      ],
      "model_parents": [
        "item/rock/smooth/schist_slab",
        "block/rock/smooth/schist_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/schist_slab"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/schist_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "forge:smooth_stone_slab",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_slabs"
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
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
            "crafting",
            "greate:milling",
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
      "id": "tfc:rock/smooth/schist_stairs",
      "namespace": "tfc",
      "display_name": "Smooth Schist Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:stairs",
        "tfc:metamorphic_items",
        "tfg:brick_index",
        "tfg:rock_stairs",
        "tfg:stone_composition/metamorphic",
        "tfg:stone_types/schist"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 8
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 9,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_metamorphic",
        "tfg:stonecutter/tfc_rock_bricks_schist",
        "tfg:stonecutter/tfc_rock_bricks_schist_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_schist_stairs",
        "tfg:stonecutter/tfc_rock_bricks_schist_wall",
        "tfg:stonecutter/tfc_rock_chiseled_schist",
        "tfg:stonecutter/tfc_rock_smooth_schist",
        "tfg:stonecutter/tfc_rock_smooth_schist_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_schist_wall"
      ],
      "recipe_output_examples": [
        "tfc:chisel/stair/smooth_schist_stairs",
        "tfc:stonecutting/rock/schist_smooth_stairs",
        "tfg:stonecutter/tfc_rock_smooth_schist_stairs"
      ],
      "model_parents": [
        "item/rock/smooth/schist_stairs",
        "block/rock/smooth/schist_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/schist_stairs"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/schist_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_stairs"
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
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "tfc:rock/smooth/schist_wall",
      "namespace": "tfc",
      "display_name": "Smooth Schist Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:walls",
        "tfc:metamorphic_items",
        "tfg:brick_index",
        "tfg:rock_walls",
        "tfg:stone_composition/metamorphic_half",
        "tfg:stone_types/schist"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 8
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 9,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_metamorphic_half",
        "tfg:stonecutter/tfc_rock_bricks_schist",
        "tfg:stonecutter/tfc_rock_bricks_schist_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_schist_stairs",
        "tfg:stonecutter/tfc_rock_bricks_schist_wall",
        "tfg:stonecutter/tfc_rock_chiseled_schist",
        "tfg:stonecutter/tfc_rock_smooth_schist",
        "tfg:stonecutter/tfc_rock_smooth_schist_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_schist_stairs"
      ],
      "recipe_output_examples": [
        "tfc:kjs/61krykpg8svkhnqei7hqapttz",
        "tfc:stonecutting/rock/schist_smooth_wall",
        "tfg:stonecutter/tfc_rock_smooth_schist_wall"
      ],
      "model_parents": [
        "item/rock/smooth/schist_wall",
        "block/rock/smooth/schist_wall_inventory",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/schist_wall"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/schist_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:walls",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_walls"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
          "value": "wall",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:walls"
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
      "id": "tfc:rock/smooth/shale",
      "namespace": "tfc",
      "display_name": "Smooth Shale",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:smooth_stone",
        "tfc:rock/smooth",
        "tfc:sedimentary_items",
        "tfg:brick_index",
        "tfg:interaction/smooth_brick",
        "tfg:stone_composition/sedimentary_clastic",
        "tfg:stone_types/shale",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped",
        "kubejs:shapeless",
        "stonecutting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 6,
        "greate:milling": 1,
        "kubejs:shaped": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 11,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "create:sandpaper_polishing": 4,
        "stonecutting": 1,
        "tfc:chisel": 2,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 23,
      "recipe_output_count": 8,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_clastic",
        "greate:shaped/steel_millstone",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "rnr:crafting/flagstone/shale",
        "tfc:crafting/quern",
        "tfc:crafting/vanilla/redstone/repeater",
        "tfc:stonecutting/rock/shale_smooth_slab",
        "tfc:stonecutting/rock/shale_smooth_stairs",
        "tfc:stonecutting/rock/shale_smooth_wall",
        "tfg:create/shaped/schematicannon",
        "tfg:shaped/comparator_certus",
        "tfg:shaped/comparator_nether_quartz",
        "tfg:shaped/comparator_quartzite",
        "tfg:stonecutter/tfc_rock_bricks_shale",
        "tfg:stonecutter/tfc_rock_bricks_shale_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_shale_stairs",
        "tfg:stonecutter/tfc_rock_bricks_shale_wall",
        "tfg:stonecutter/tfc_rock_chiseled_shale",
        "tfg:stonecutter/tfc_rock_smooth_shale_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_shale_stairs",
        "tfg:stonecutter/tfc_rock_smooth_shale_wall"
      ],
      "recipe_output_examples": [
        "tfc:chisel/smooth/shale_hardened_smooth",
        "tfc:chisel/smooth/shale_smooth",
        "tfc:crafting/rock/shale_smooth",
        "tfg:polishing/shale_brick_to_polished",
        "tfg:polishing/shale_cracked_brick_to_polished",
        "tfg:polishing/shale_mossy_brick_to_polished",
        "tfg:polishing/shale_raw_to_polished",
        "tfg:stonecutter/tfc_rock_smooth_shale"
      ],
      "model_parents": [
        "item/rock/smooth/shale",
        "block/rock/smooth/shale",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/shale"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/shale",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
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
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "firmalife:oven_insulation",
          "forge:smooth_stone",
          "minecraft:mineable/pickaxe",
          "tfc:bloomery_insulation",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfc:creeping_stone_plantable_on",
          "tfc:forge_insulation",
          "tfc:rock/smooth"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Sedimentary"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
            "kubejs:shaped",
            "kubejs:shapeless",
            "stonecutting",
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "tfc:rock/smooth/shale_slab",
      "namespace": "tfc",
      "display_name": "Smooth Shale Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:smooth_stone_slab",
        "forge:smooth_stone_slabs",
        "minecraft:slabs",
        "tfc:sedimentary_items",
        "tfg:brick_index",
        "tfg:rock_slabs",
        "tfg:stone_composition/sedimentary_clastic_half",
        "tfg:stone_types/shale_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "greate:milling": 1,
        "stonecutting": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 3,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "exposure:camera_stand",
        "greate:milling/integration/tfg/macerate_sedimentary_clastic_half",
        "tfc:crafting/vanilla/armor_stand",
        "tfg:stonecutter/tfc_rock_bricks_shale_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfc:chisel/slab/smooth_shale_slab",
        "tfc:stonecutting/rock/shale_smooth_slab",
        "tfg:stonecutter/tfc_rock_smooth_shale_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_shale_slab_slab_to_slab"
      ],
      "model_parents": [
        "item/rock/smooth/shale_slab",
        "block/rock/smooth/shale_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/shale_slab"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/shale_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "forge:smooth_stone_slab",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_slabs"
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
          "text": "Sedimentary"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
            "crafting",
            "greate:milling",
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
      "id": "tfc:rock/smooth/shale_stairs",
      "namespace": "tfc",
      "display_name": "Smooth Shale Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:stairs",
        "tfc:sedimentary_items",
        "tfg:brick_index",
        "tfg:rock_stairs",
        "tfg:stone_composition/sedimentary_clastic",
        "tfg:stone_types/shale"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 8
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 9,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_clastic",
        "tfg:stonecutter/tfc_rock_bricks_shale",
        "tfg:stonecutter/tfc_rock_bricks_shale_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_shale_stairs",
        "tfg:stonecutter/tfc_rock_bricks_shale_wall",
        "tfg:stonecutter/tfc_rock_chiseled_shale",
        "tfg:stonecutter/tfc_rock_smooth_shale",
        "tfg:stonecutter/tfc_rock_smooth_shale_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_shale_wall"
      ],
      "recipe_output_examples": [
        "tfc:chisel/stair/smooth_shale_stairs",
        "tfc:stonecutting/rock/shale_smooth_stairs",
        "tfg:stonecutter/tfc_rock_smooth_shale_stairs"
      ],
      "model_parents": [
        "item/rock/smooth/shale_stairs",
        "block/rock/smooth/shale_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/shale_stairs"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/shale_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_stairs"
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
          "text": "Sedimentary"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "tfc:rock/smooth/shale_wall",
      "namespace": "tfc",
      "display_name": "Smooth Shale Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:walls",
        "tfc:sedimentary_items",
        "tfg:brick_index",
        "tfg:rock_walls",
        "tfg:stone_composition/sedimentary_clastic_half",
        "tfg:stone_types/shale"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 8
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 9,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_sedimentary_clastic_half",
        "tfg:stonecutter/tfc_rock_bricks_shale",
        "tfg:stonecutter/tfc_rock_bricks_shale_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_shale_stairs",
        "tfg:stonecutter/tfc_rock_bricks_shale_wall",
        "tfg:stonecutter/tfc_rock_chiseled_shale",
        "tfg:stonecutter/tfc_rock_smooth_shale",
        "tfg:stonecutter/tfc_rock_smooth_shale_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_shale_stairs"
      ],
      "recipe_output_examples": [
        "tfc:kjs/dbzsl7vyz6o14n7ry6dyjtand",
        "tfc:stonecutting/rock/shale_smooth_wall",
        "tfg:stonecutter/tfc_rock_smooth_shale_wall"
      ],
      "model_parents": [
        "item/rock/smooth/shale_wall",
        "block/rock/smooth/shale_wall_inventory",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/shale_wall"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/shale_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:walls",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_walls"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Sedimentary"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
          "value": "wall",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:walls"
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
      "id": "tfc:rock/smooth/slate",
      "namespace": "tfc",
      "display_name": "Smooth Slate",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:smooth_stone",
        "tfc:metamorphic_items",
        "tfc:rock/smooth",
        "tfg:brick_index",
        "tfg:interaction/smooth_brick",
        "tfg:stone_composition/metamorphic",
        "tfg:stone_types/slate",
        "tfg:whitelisted/facades"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "kubejs:shaped",
        "kubejs:shapeless",
        "stonecutting",
        "tfc:damage_inputs_shapeless_crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 6,
        "greate:milling": 1,
        "kubejs:shaped": 1,
        "kubejs:shapeless": 3,
        "stonecutting": 11,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_production_by_type": {
        "create:sandpaper_polishing": 4,
        "stonecutting": 1,
        "tfc:chisel": 2,
        "tfc:damage_inputs_shapeless_crafting": 1
      },
      "recipe_ingredient_count": 23,
      "recipe_output_count": 8,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_metamorphic",
        "greate:shaped/steel_millstone",
        "gtceu:facade_cover",
        "gtceu:facade_cover32",
        "gtceu:facade_cover_recycle",
        "rnr:crafting/flagstone/slate",
        "tfc:crafting/quern",
        "tfc:crafting/vanilla/redstone/repeater",
        "tfc:stonecutting/rock/slate_smooth_slab",
        "tfc:stonecutting/rock/slate_smooth_stairs",
        "tfc:stonecutting/rock/slate_smooth_wall",
        "tfg:create/shaped/schematicannon",
        "tfg:shaped/comparator_certus",
        "tfg:shaped/comparator_nether_quartz",
        "tfg:shaped/comparator_quartzite",
        "tfg:stonecutter/tfc_rock_bricks_slate",
        "tfg:stonecutter/tfc_rock_bricks_slate_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_slate_stairs",
        "tfg:stonecutter/tfc_rock_bricks_slate_wall",
        "tfg:stonecutter/tfc_rock_chiseled_slate",
        "tfg:stonecutter/tfc_rock_smooth_slate_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_slate_stairs",
        "tfg:stonecutter/tfc_rock_smooth_slate_wall"
      ],
      "recipe_output_examples": [
        "tfc:chisel/smooth/slate_hardened_smooth",
        "tfc:chisel/smooth/slate_smooth",
        "tfc:crafting/rock/slate_smooth",
        "tfg:polishing/slate_brick_to_polished",
        "tfg:polishing/slate_cracked_brick_to_polished",
        "tfg:polishing/slate_mossy_brick_to_polished",
        "tfg:polishing/slate_raw_to_polished",
        "tfg:stonecutter/tfc_rock_smooth_slate"
      ],
      "model_parents": [
        "item/rock/smooth/slate",
        "block/rock/smooth/slate",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/slate"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/slate",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "domum_ornamentum:all_brick_materials",
          "domum_ornamentum:bricks",
          "domum_ornamentum:default",
          "domum_ornamentum:doors_materials",
          "domum_ornamentum:fancy_doors_materials",
          "domum_ornamentum:fancy_gate_materials",
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
          "domum_ornamentum:stairs_material",
          "domum_ornamentum:stairs_materials",
          "domum_ornamentum:timber_frames_center",
          "domum_ornamentum:timber_frames_frame",
          "domum_ornamentum:trapdoors_materials",
          "domum_ornamentum:wall_materials",
          "firmalife:oven_insulation",
          "forge:smooth_stone",
          "minecraft:mineable/pickaxe",
          "tfc:bloomery_insulation",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfc:creeping_stone_plantable_on",
          "tfc:forge_insulation",
          "tfc:rock/smooth"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
            "kubejs:shaped",
            "kubejs:shapeless",
            "stonecutting",
            "tfc:damage_inputs_shapeless_crafting"
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
      "id": "tfc:rock/smooth/slate_slab",
      "namespace": "tfc",
      "display_name": "Smooth Slate Slab",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:smooth_stone_slab",
        "forge:smooth_stone_slabs",
        "minecraft:slabs",
        "tfc:metamorphic_items",
        "tfg:brick_index",
        "tfg:rock_slabs",
        "tfg:stone_composition/metamorphic_half",
        "tfg:stone_types/slate_half"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shaped": 2,
        "greate:milling": 1,
        "stonecutting": 1
      },
      "recipe_production_by_type": {
        "stonecutting": 3,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "exposure:camera_stand",
        "greate:milling/integration/tfg/macerate_metamorphic_half",
        "tfc:crafting/vanilla/armor_stand",
        "tfg:stonecutter/tfc_rock_bricks_slate_slab_slab_to_slab"
      ],
      "recipe_output_examples": [
        "tfc:chisel/slab/smooth_slate_slab",
        "tfc:stonecutting/rock/slate_smooth_slab",
        "tfg:stonecutter/tfc_rock_smooth_slate_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_slate_slab_slab_to_slab"
      ],
      "model_parents": [
        "item/rock/smooth/slate_slab",
        "block/rock/smooth/slate_slab",
        "block/slab"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/slate_slab"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/slate_slab",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "forge:smooth_stone_slab",
          "minecraft:mineable/pickaxe",
          "minecraft:slabs",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_slabs"
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
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 64,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
            "crafting",
            "greate:milling",
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
      "id": "tfc:rock/smooth/slate_stairs",
      "namespace": "tfc",
      "display_name": "Smooth Slate Stairs",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:stairs",
        "tfc:metamorphic_items",
        "tfg:brick_index",
        "tfg:rock_stairs",
        "tfg:stone_composition/metamorphic",
        "tfg:stone_types/slate"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 8
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 9,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_metamorphic",
        "tfg:stonecutter/tfc_rock_bricks_slate",
        "tfg:stonecutter/tfc_rock_bricks_slate_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_slate_stairs",
        "tfg:stonecutter/tfc_rock_bricks_slate_wall",
        "tfg:stonecutter/tfc_rock_chiseled_slate",
        "tfg:stonecutter/tfc_rock_smooth_slate",
        "tfg:stonecutter/tfc_rock_smooth_slate_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_slate_wall"
      ],
      "recipe_output_examples": [
        "tfc:chisel/stair/smooth_slate_stairs",
        "tfc:stonecutting/rock/slate_smooth_stairs",
        "tfg:stonecutter/tfc_rock_smooth_slate_stairs"
      ],
      "model_parents": [
        "item/rock/smooth/slate_stairs",
        "block/rock/smooth/slate_stairs",
        "block/stairs"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/slate_stairs"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/slate_stairs",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:stairs",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_stairs"
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
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
        "processing_in": {
          "values": [
            "greate:milling",
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
      "id": "tfc:rock/smooth/slate_wall",
      "namespace": "tfc",
      "display_name": "Smooth Slate Wall",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "minecraft:walls",
        "tfc:metamorphic_items",
        "tfg:brick_index",
        "tfg:rock_walls",
        "tfg:stone_composition/metamorphic_half",
        "tfg:stone_types/slate"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "greate:milling",
        "stonecutting"
      ],
      "recipe_consumption_by_type": {
        "greate:milling": 1,
        "stonecutting": 8
      },
      "recipe_production_by_type": {
        "stonecutting": 2,
        "tfc:chisel": 1
      },
      "recipe_ingredient_count": 9,
      "recipe_output_count": 3,
      "recipe_ingredient_examples": [
        "greate:milling/integration/tfg/macerate_metamorphic_half",
        "tfg:stonecutter/tfc_rock_bricks_slate",
        "tfg:stonecutter/tfc_rock_bricks_slate_slab_half",
        "tfg:stonecutter/tfc_rock_bricks_slate_stairs",
        "tfg:stonecutter/tfc_rock_bricks_slate_wall",
        "tfg:stonecutter/tfc_rock_chiseled_slate",
        "tfg:stonecutter/tfc_rock_smooth_slate",
        "tfg:stonecutter/tfc_rock_smooth_slate_slab_half",
        "tfg:stonecutter/tfc_rock_smooth_slate_stairs"
      ],
      "recipe_output_examples": [
        "tfc:kjs/ekumjgdnj6mxjshwzofpc9vjd",
        "tfc:stonecutting/rock/slate_smooth_wall",
        "tfg:stonecutter/tfc_rock_smooth_slate_wall"
      ],
      "model_parents": [
        "item/rock/smooth/slate_wall",
        "block/rock/smooth/slate_wall_inventory",
        "block/wall_inventory"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 1,
      "loot_source_examples": [
        "tfc:blocks/rock/smooth/slate_wall"
      ],
      "block_context": {
        "block_id": "tfc:rock/smooth/slate_wall",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "minecraft:walls",
          "tfc:can_collapse",
          "tfc:can_start_collapse",
          "tfc:can_trigger_collapse",
          "tfg:rock_walls"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Metamorphic"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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
          "value": "wall",
          "confidence": 1,
          "source": "rule:form_from_tag",
          "rationale": "tag minecraft:walls"
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
      "id": "tfc:rock/spike/andesite",
      "namespace": "tfc",
      "display_name": "Andesite Spike",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "tfc:igneous_extrusive_items",
        "tfg:rock_spikes"
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
        "item/rock/spike/andesite",
        "block/rock/spike/andesite_base",
        "block/rock/spike_base",
        "block/cube_all"
      ],
      "creative_tabs": [
        "tfc:rock"
      ],
      "loot_source_count": 0,
      "loot_source_examples": [],
      "block_context": {
        "block_id": "tfc:rock/spike/andesite",
        "block_tags": [
          "cucumber:mineable/paxel",
          "diggerhelmet:mineable_with_speed_booster",
          "minecraft:mineable/pickaxe",
          "tfc:can_collapse"
        ],
        "requires_correct_tool": true
      },
      "lore": [],
      "semantic_text": [
        {
          "source": "runtime-tooltip",
          "text": "Igneous Extrusive"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Light ⇲ Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "TerraFirmaCraft"
        }
      ],
      "component_highlights": {
        "minecraft:max_stack_size": 32,
        "minecraft:rarity": "common"
      },
      "stage2_facets": {
        "mod_namespace": {
          "value": "tfc",
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