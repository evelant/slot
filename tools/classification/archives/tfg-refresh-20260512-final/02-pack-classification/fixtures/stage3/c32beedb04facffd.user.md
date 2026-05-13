# Items to classify
{
  "items": [
    {
      "id": "gtceu:purified_bauxite_ore",
      "namespace": "gtceu",
      "display_name": "Purified Bauxite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bauxite",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:mixing",
        "greate:pressing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:mixing": 1,
        "greate:pressing": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_bauxite_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_bauxite_crushed_ore_to_dust",
        "greate:mixing/integration/gtceu/mixer/bauxite_slurry_from_washed_bauxite",
        "greate:pressing/pure_crushed_bauxite_to_pure_dust",
        "gtceu:shapeless/purified_ore_to_dust_bauxite"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/bauxite_purified_ore",
        "tfg:instant_barrel/bauxite_purified_ore",
        "tfg:splashing/bauxite_purified_ore_distilled",
        "tfg:splashing/bauxite_purified_ore_water"
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
          "text": "Al₂O₃"
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
        "material_family": {
          "value": "bauxite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_bauxite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "greate:mixing",
            "greate:pressing"
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
      "id": "gtceu:purified_bentonite_ore",
      "namespace": "gtceu",
      "display_name": "Purified Bentonite",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bentonite",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:pressing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_bentonite_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_bentonite_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_bentonite_to_pure_dust",
        "gtceu:shapeless/purified_ore_to_dust_bentonite"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/bentonite_purified_ore",
        "tfg:instant_barrel/bentonite_purified_ore",
        "tfg:splashing/bentonite_purified_ore_distilled",
        "tfg:splashing/bentonite_purified_ore_water"
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
          "text": "NaMg₆Si₁₂H₆(H₂O)₅O₃₆"
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
        "material_family": {
          "value": "bentonite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_bentonite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "greate:pressing"
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
      "id": "gtceu:purified_beryllium_ore",
      "namespace": "gtceu",
      "display_name": "Purified Beryllium Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/beryllium",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "blasting",
        "crafting",
        "greate:milling",
        "greate:pressing",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "blasting": 1,
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "smelting": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_beryllium_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_beryllium_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_beryllium_to_pure_dust",
        "gtceu:blasting/smelt_purified_ore_beryllium_to_ingot",
        "gtceu:shapeless/purified_ore_to_dust_beryllium",
        "gtceu:smelting/smelt_purified_ore_beryllium_to_ingot"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/beryllium_purified_ore",
        "tfg:instant_barrel/beryllium_purified_ore",
        "tfg:splashing/beryllium_purified_ore_distilled",
        "tfg:splashing/beryllium_purified_ore_water"
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
          "text": "Be"
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
        "material_family": {
          "value": "beryllium",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_beryllium_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "blasting",
            "crafting",
            "greate:milling",
            "greate:pressing",
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
      "id": "gtceu:purified_bismuth_ore",
      "namespace": "gtceu",
      "display_name": "Purified Bismuth Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "blasting",
        "crafting",
        "greate:milling",
        "greate:pressing",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "blasting": 1,
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "smelting": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_bismuth_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_bismuth_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_bismuth_to_pure_dust",
        "gtceu:blasting/smelt_purified_ore_bismuth_to_ingot",
        "gtceu:shapeless/purified_ore_to_dust_bismuth",
        "gtceu:smelting/smelt_purified_ore_bismuth_to_ingot"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/bismuth_purified_ore",
        "tfg:instant_barrel/bismuth_purified_ore",
        "tfg:splashing/bismuth_purified_ore_distilled",
        "tfg:splashing/bismuth_purified_ore_water"
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
          "text": "Bi"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 100 mB of §fBismuth§7 (at Very Hot٭§7)"
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
        "material_family": {
          "value": "bismuth",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_bismuth_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "blasting",
            "crafting",
            "greate:milling",
            "greate:pressing",
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
      "id": "gtceu:purified_blue_topaz_ore",
      "namespace": "gtceu",
      "display_name": "Purified Blue Topaz Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/blue_topaz",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron",
        "forge:siftables"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:pressing",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_blue_topaz_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_blue_topaz_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_blue_topaz_to_pure_dust",
        "gtceu:shapeless/purified_ore_to_dust_blue_topaz",
        "tfg:vi/vibrating/blue_topaz"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/blue_topaz_purified_ore",
        "tfg:instant_barrel/blue_topaz_purified_ore",
        "tfg:splashing/blue_topaz_purified_ore_distilled",
        "tfg:splashing/blue_topaz_purified_ore_water"
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
          "text": "Al₂SiO₄F₂"
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
        "material_family": {
          "value": "blue_topaz",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_blue_topaz_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "greate:pressing",
            "vintageimprovements:vibrating"
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
      "id": "gtceu:purified_borax_ore",
      "namespace": "gtceu",
      "display_name": "Purified Borax Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/borax",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:pressing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_borax_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_borax_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_borax_to_pure_dust",
        "gtceu:shapeless/purified_ore_to_dust_borax"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/borax_purified_ore",
        "tfg:instant_barrel/borax_purified_ore",
        "tfg:splashing/borax_purified_ore_distilled",
        "tfg:splashing/borax_purified_ore_water"
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
          "text": "Na₂B₄(H₂O)₁₀O₇"
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
        "material_family": {
          "value": "borax",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_borax_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "greate:pressing"
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
      "id": "gtceu:purified_bornite_ore",
      "namespace": "gtceu",
      "display_name": "Purified Bornite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/bornite",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron",
        "tfg:platinum_ore_group"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "blasting",
        "crafting",
        "greate:milling",
        "greate:pressing",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "blasting": 1,
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "smelting": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_bornite_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_bornite_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_bornite_to_pure_dust",
        "gtceu:blasting/smelt_purified_ore_bornite_to_ingot",
        "gtceu:shapeless/purified_ore_to_dust_bornite",
        "gtceu:smelting/smelt_purified_ore_bornite_to_ingot"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/bornite_purified_ore",
        "tfg:instant_barrel/bornite_purified_ore",
        "tfg:splashing/bornite_purified_ore_distilled",
        "tfg:splashing/bornite_purified_ore_water"
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
          "text": "Cu₅FeS₄"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 90 mB of §fCopper§7 (at Orange٭٭٭٭§7)"
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
        "material_family": {
          "value": "bornite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_bornite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "blasting",
            "crafting",
            "greate:milling",
            "greate:pressing",
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
      "id": "gtceu:purified_calcite_ore",
      "namespace": "gtceu",
      "display_name": "Purified Calcite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/calcite",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:pressing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_calcite_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_calcite_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_calcite_to_pure_dust",
        "gtceu:shapeless/purified_ore_to_dust_calcite"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/calcite_purified_ore",
        "tfg:instant_barrel/calcite_purified_ore",
        "tfg:splashing/calcite_purified_ore_distilled",
        "tfg:splashing/calcite_purified_ore_water"
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
          "text": "CaCO₃"
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
        "material_family": {
          "value": "calcite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_calcite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "greate:pressing"
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
      "id": "gtceu:purified_cassiterite_ore",
      "namespace": "gtceu",
      "display_name": "Purified Cassiterite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cassiterite",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "blasting",
        "crafting",
        "greate:milling",
        "greate:pressing",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "blasting": 1,
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "smelting": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_cassiterite_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_cassiterite_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_cassiterite_to_pure_dust",
        "gtceu:blasting/smelt_purified_ore_cassiterite_to_ingot",
        "gtceu:shapeless/purified_ore_to_dust_cassiterite",
        "gtceu:smelting/smelt_purified_ore_cassiterite_to_ingot"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/cassiterite_purified_ore",
        "tfg:instant_barrel/cassiterite_purified_ore",
        "tfg:splashing/cassiterite_purified_ore_distilled",
        "tfg:splashing/cassiterite_purified_ore_water"
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
          "text": "SnO₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 100 mB of §fTin§7 (at Very Hot§7)"
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
        "material_family": {
          "value": "cassiterite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_cassiterite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "blasting",
            "crafting",
            "greate:milling",
            "greate:pressing",
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
      "id": "gtceu:purified_cassiterite_sand_ore",
      "namespace": "gtceu",
      "display_name": "Purified Cassiterite Sand",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cassiterite_sand",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "blasting",
        "crafting",
        "greate:milling",
        "greate:pressing",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "blasting": 1,
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "smelting": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_cassiterite_sand_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_cassiterite_sand_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_cassiterite_sand_to_pure_dust",
        "gtceu:blasting/smelt_purified_ore_cassiterite_sand_to_ingot",
        "gtceu:shapeless/purified_ore_to_dust_cassiterite_sand",
        "gtceu:smelting/smelt_purified_ore_cassiterite_sand_to_ingot"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/cassiterite_sand_purified_ore",
        "tfg:instant_barrel/cassiterite_sand_purified_ore",
        "tfg:splashing/cassiterite_sand_purified_ore_distilled",
        "tfg:splashing/cassiterite_sand_purified_ore_water"
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
          "text": "SnO₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 80 mB of §fTin§7 (at Very Hot§7)"
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
        "material_family": {
          "value": "cassiterite_sand",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_cassiterite_sand_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "blasting",
            "crafting",
            "greate:milling",
            "greate:pressing",
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
      "id": "gtceu:purified_certus_quartz_ore",
      "namespace": "gtceu",
      "display_name": "Purified Certus Quartz Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/certus_quartz",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron",
        "forge:siftables"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:pressing",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_certus_quartz_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_certus_quartz_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_certus_quartz_to_pure_dust",
        "gtceu:shapeless/purified_ore_to_dust_certus_quartz",
        "tfg:vi/vibrating/certus_quartz"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/certus_quartz_purified_ore",
        "tfg:instant_barrel/certus_quartz_purified_ore",
        "tfg:splashing/certus_quartz_purified_ore_distilled",
        "tfg:splashing/certus_quartz_purified_ore_water"
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
          "text": "SiO₂"
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
        "material_family": {
          "value": "certus_quartz",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_certus_quartz_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "greate:pressing",
            "vintageimprovements:vibrating"
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
      "id": "gtceu:purified_chalcocite_ore",
      "namespace": "gtceu",
      "display_name": "Purified Chalcocite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/chalcocite",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron",
        "tfg:platinum_ore_group"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "blasting",
        "crafting",
        "greate:milling",
        "greate:pressing",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "blasting": 1,
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "smelting": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_chalcocite_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_chalcocite_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_chalcocite_to_pure_dust",
        "gtceu:blasting/smelt_purified_ore_chalcocite_to_ingot",
        "gtceu:shapeless/purified_ore_to_dust_chalcocite",
        "gtceu:smelting/smelt_purified_ore_chalcocite_to_ingot"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/chalcocite_purified_ore",
        "tfg:instant_barrel/chalcocite_purified_ore",
        "tfg:splashing/chalcocite_purified_ore_distilled",
        "tfg:splashing/chalcocite_purified_ore_water"
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
          "text": "Cu₂S"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 94 mB of §fCopper§7 (at Orange٭٭٭٭§7)"
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
        "material_family": {
          "value": "chalcocite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_chalcocite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "blasting",
            "crafting",
            "greate:milling",
            "greate:pressing",
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
      "id": "gtceu:purified_chalcopyrite_ore",
      "namespace": "gtceu",
      "display_name": "Purified Chalcopyrite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/chalcopyrite",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron",
        "tfg:platinum_ore_group"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "blasting",
        "crafting",
        "greate:milling",
        "greate:pressing",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "blasting": 1,
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "smelting": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_chalcopyrite_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_chalcopyrite_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_chalcopyrite_to_pure_dust",
        "gtceu:blasting/smelt_purified_ore_chalcopyrite_to_ingot",
        "gtceu:shapeless/purified_ore_to_dust_chalcopyrite",
        "gtceu:smelting/smelt_purified_ore_chalcopyrite_to_ingot"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/chalcopyrite_purified_ore",
        "tfg:instant_barrel/chalcopyrite_purified_ore",
        "tfg:splashing/chalcopyrite_purified_ore_distilled",
        "tfg:splashing/chalcopyrite_purified_ore_water"
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
          "text": "CuFeS₂"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 84 mB of §fCopper§7 (at Orange٭٭٭٭§7)"
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
        "material_family": {
          "value": "chalcopyrite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_chalcopyrite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "blasting",
            "crafting",
            "greate:milling",
            "greate:pressing",
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
      "id": "gtceu:purified_chromite_ore",
      "namespace": "gtceu",
      "display_name": "Purified Chromite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/chromite",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:pressing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_chromite_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_chromite_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_chromite_to_pure_dust",
        "gtceu:shapeless/purified_ore_to_dust_chromite"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/chromite_purified_ore",
        "tfg:instant_barrel/chromite_purified_ore",
        "tfg:splashing/chromite_purified_ore_distilled",
        "tfg:splashing/chromite_purified_ore_water"
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
          "text": "FeCr₂O₄"
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
        "material_family": {
          "value": "chromite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_chromite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "greate:pressing"
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
      "id": "gtceu:purified_cinnabar_ore",
      "namespace": "gtceu",
      "display_name": "Purified Cinnabar Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cinnabar",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron",
        "forge:siftables"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:pressing",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_cinnabar_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_cinnabar_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_cinnabar_to_pure_dust",
        "gtceu:shapeless/purified_ore_to_dust_cinnabar",
        "tfg:vi/vibrating/cinnabar"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/cinnabar_purified_ore",
        "tfg:instant_barrel/cinnabar_purified_ore",
        "tfg:splashing/cinnabar_purified_ore_distilled",
        "tfg:splashing/cinnabar_purified_ore_water"
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
          "text": "HgS"
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
        "material_family": {
          "value": "cinnabar",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_cinnabar_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "greate:pressing",
            "vintageimprovements:vibrating"
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
      "id": "gtceu:purified_coal_ore",
      "namespace": "gtceu",
      "display_name": "Purified Bituminous Coal Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "c:hidden_from_recipe_viewers"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1
      },
      "recipe_production_by_type": {},
      "recipe_ingredient_count": 1,
      "recipe_output_count": 0,
      "recipe_ingredient_examples": [
        "minecraft:kjs/tfc_ore_bituminous_coal_3"
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
          "text": "C"
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
        "material_family": {
          "value": "coal",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_coal_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting"
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
      "id": "gtceu:purified_cobalt_ore",
      "namespace": "gtceu",
      "display_name": "Purified Cobalt Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "blasting",
        "crafting",
        "greate:milling",
        "greate:pressing",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "blasting": 1,
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "smelting": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_cobalt_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_cobalt_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_cobalt_to_pure_dust",
        "gtceu:blasting/smelt_purified_ore_cobalt_to_ingot",
        "gtceu:shapeless/purified_ore_to_dust_cobalt",
        "gtceu:smelting/smelt_purified_ore_cobalt_to_ingot"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/cobalt_purified_ore",
        "tfg:instant_barrel/cobalt_purified_ore",
        "tfg:splashing/cobalt_purified_ore_distilled",
        "tfg:splashing/cobalt_purified_ore_water"
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
          "text": "Co"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 100 mB of §fCobalt§7 (at White٭٭٭٭§7)"
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
        "material_family": {
          "value": "cobalt",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_cobalt_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "blasting",
            "crafting",
            "greate:milling",
            "greate:pressing",
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
      "id": "gtceu:purified_cobaltite_ore",
      "namespace": "gtceu",
      "display_name": "Purified Cobaltite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/cobaltite",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "blasting",
        "crafting",
        "greate:milling",
        "greate:pressing",
        "smelting"
      ],
      "recipe_consumption_by_type": {
        "blasting": 1,
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "smelting": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 6,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_cobaltite_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_cobaltite_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_cobaltite_to_pure_dust",
        "gtceu:blasting/smelt_purified_ore_cobaltite_to_ingot",
        "gtceu:shapeless/purified_ore_to_dust_cobaltite",
        "gtceu:smelting/smelt_purified_ore_cobaltite_to_ingot"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/cobaltite_purified_ore",
        "tfg:instant_barrel/cobaltite_purified_ore",
        "tfg:splashing/cobaltite_purified_ore_distilled",
        "tfg:splashing/cobaltite_purified_ore_water"
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
          "text": "CoAsS"
        },
        {
          "source": "runtime-tooltip",
          "text": "⚖ Very Light ⇲ Very Small"
        },
        {
          "source": "runtime-tooltip",
          "text": "§7Melts into 84 mB of §fCobalt§7 (at White٭٭٭٭§7)"
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
        "material_family": {
          "value": "cobaltite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_cobaltite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "blasting",
            "crafting",
            "greate:milling",
            "greate:pressing",
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
      "id": "gtceu:purified_cooperite_ore",
      "namespace": "gtceu",
      "display_name": "Purified Cooperite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/cooperite",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron",
        "tfg:platinum_ore_group"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:pressing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_cooperite_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_cooperite_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_cooperite_to_pure_dust",
        "gtceu:shapeless/purified_ore_to_dust_cooperite"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/cooperite_purified_ore",
        "tfg:instant_barrel/cooperite_purified_ore",
        "tfg:splashing/cooperite_purified_ore_distilled",
        "tfg:splashing/cooperite_purified_ore_water"
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
          "text": "Pt₃NiSPd"
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
        "material_family": {
          "value": "cooperite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_cooperite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "greate:pressing"
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
      "id": "gtceu:purified_desh_ore",
      "namespace": "gtceu",
      "display_name": "Purified Desh Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/desh",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:pressing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_desh_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_desh_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_desh_to_pure_dust",
        "gtceu:shapeless/purified_ore_to_dust_desh"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/desh_purified_ore",
        "tfg:instant_barrel/desh_purified_ore",
        "tfg:splashing/desh_purified_ore_distilled",
        "tfg:splashing/desh_purified_ore_water"
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
          "text": "(MgFe(SiO₂)₂)₂(FeTiO₃)N₄"
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
        "material_family": {
          "value": "desh",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_desh_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "greate:pressing"
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
      "id": "gtceu:purified_diamond_ore",
      "namespace": "gtceu",
      "display_name": "Purified Diamond Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/diamond",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron",
        "forge:siftables"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:pressing",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_diamond_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_diamond_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_diamond_to_pure_dust",
        "gtceu:shapeless/purified_ore_to_dust_diamond",
        "tfg:vi/vibrating/diamond"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/diamond_purified_ore",
        "tfg:instant_barrel/diamond_purified_ore",
        "tfg:splashing/diamond_purified_ore_distilled",
        "tfg:splashing/diamond_purified_ore_water"
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
          "text": "C"
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
        "material_family": {
          "value": "diamond",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_diamond_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "greate:pressing",
            "vintageimprovements:vibrating"
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
      "id": "gtceu:purified_diatomite_ore",
      "namespace": "gtceu",
      "display_name": "Purified Diatomite Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/diatomite",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:pressing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_diatomite_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_diatomite_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_diatomite_to_pure_dust",
        "gtceu:shapeless/purified_ore_to_dust_diatomite"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/diatomite_purified_ore",
        "tfg:instant_barrel/diatomite_purified_ore",
        "tfg:splashing/diatomite_purified_ore_distilled",
        "tfg:splashing/diatomite_purified_ore_water"
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
          "text": "(SiO₂)₈(Fe₂O₃)(Al₂O₃)"
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
        "material_family": {
          "value": "diatomite",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_diatomite_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "greate:pressing"
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
      "id": "gtceu:purified_electrotine_ore",
      "namespace": "gtceu",
      "display_name": "Purified Electrotine Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/electrotine",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:pressing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_electrotine_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_electrotine_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_electrotine_to_pure_dust",
        "gtceu:shapeless/purified_ore_to_dust_electrotine"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/electrotine_purified_ore",
        "tfg:instant_barrel/electrotine_purified_ore",
        "tfg:splashing/electrotine_purified_ore_distilled",
        "tfg:splashing/electrotine_purified_ore_water"
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
          "text": "(Si(FeS₂)₅(CrAl₂O₃)Hg₃)(AgAu)"
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
        "material_family": {
          "value": "electrotine",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_electrotine_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "greate:pressing"
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
      "id": "gtceu:purified_emerald_ore",
      "namespace": "gtceu",
      "display_name": "Purified Emerald Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/emerald",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron",
        "forge:siftables"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:pressing",
        "vintageimprovements:vibrating"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1,
        "vintageimprovements:vibrating": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 5,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_emerald_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_emerald_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_emerald_to_pure_dust",
        "gtceu:shapeless/purified_ore_to_dust_emerald",
        "tfg:vi/vibrating/emerald"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/emerald_purified_ore",
        "tfg:instant_barrel/emerald_purified_ore",
        "tfg:splashing/emerald_purified_ore_distilled",
        "tfg:splashing/emerald_purified_ore_water"
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
          "text": "Be₃Al₂Si₆O₁₈"
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
        "material_family": {
          "value": "emerald",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_emerald_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "greate:pressing",
            "vintageimprovements:vibrating"
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
      "id": "gtceu:purified_fullers_earth_ore",
      "namespace": "gtceu",
      "display_name": "Purified Fuller's Earth Ore",
      "minecraft_tags_direct": [],
      "minecraft_tags_inherited": [],
      "minecraft_tags_resolved": [
        "forge:purified_ores",
        "forge:purified_ores/aluminium_silicate",
        "forge:purified_ores/bismuth_bronze",
        "forge:purified_ores/black_bronze",
        "forge:purified_ores/black_steel",
        "forge:purified_ores/blue_steel",
        "forge:purified_ores/brass",
        "forge:purified_ores/bronze",
        "forge:purified_ores/cobalt_brass",
        "forge:purified_ores/fullers_earth",
        "forge:purified_ores/invar",
        "forge:purified_ores/potin",
        "forge:purified_ores/red_alloy",
        "forge:purified_ores/red_steel",
        "forge:purified_ores/rose_gold",
        "forge:purified_ores/steel",
        "forge:purified_ores/sterling_silver",
        "forge:purified_ores/tin_alloy",
        "forge:purified_ores/weak_blue_steel",
        "forge:purified_ores/weak_red_steel",
        "forge:purified_ores/wrought_iron"
      ],
      "minecraft_tag_membership": "resolved_runtime",
      "processing_in": [
        "crafting",
        "greate:milling",
        "greate:pressing"
      ],
      "recipe_consumption_by_type": {
        "crafting_shapeless": 1,
        "greate:milling": 2,
        "greate:pressing": 1
      },
      "recipe_production_by_type": {
        "ae2:transform": 1,
        "greate:splashing": 2,
        "tfc:barrel_instant": 1
      },
      "recipe_ingredient_count": 4,
      "recipe_output_count": 4,
      "recipe_ingredient_examples": [
        "greate:milling/integration/gtceu/macerate_fullers_earth_crushed_ore_to_dust",
        "greate:milling/integration/gtceu/macerator/macerate_fullers_earth_crushed_ore_to_dust",
        "greate:pressing/pure_crushed_fullers_earth_to_pure_dust",
        "gtceu:shapeless/purified_ore_to_dust_fullers_earth"
      ],
      "recipe_output_examples": [
        "tfg:ae_transform/fullers_earth_purified_ore",
        "tfg:instant_barrel/fullers_earth_purified_ore",
        "tfg:splashing/fullers_earth_purified_ore_distilled",
        "tfg:splashing/fullers_earth_purified_ore_water"
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
          "text": "Mg₂Si₄O₁₄H₄(H₂O)"
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
        "material_family": {
          "value": "fullers_earth",
          "confidence": 1,
          "source": "rule:material_family_from_ore_id",
          "rationale": "ore id purified_fullers_earth_ore"
        },
        "form": {
          "value": "ore",
          "confidence": 1,
          "source": "rule:form_from_id",
          "rationale": "suffix _ore"
        },
        "processing_in": {
          "values": [
            "crafting",
            "greate:milling",
            "greate:pressing"
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