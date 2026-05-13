{
  "pack_id": "tfg",
  "facet": "use_affordance",
  "policy": "Vocabulary-backed direct interaction verb or affordance, not generic recipe membership.",
  "min_evidence": 2,
  "previous_accepted": [],
  "prompt_budget": {
    "max_chars": 3200000,
    "semantic_evidence_per_candidate": 64,
    "evidence_refs_per_candidate": 64
  },
  "candidates": [
    {
      "id": "slot:fuel",
      "label": "Fuel",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 1678,
      "evidence": [
        {
          "kind": "item_tag",
          "id": "create:blaze_burner_fuel/regular",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "create:blaze_burner_fuel/special",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "deafission:fuels",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "firmalife:oven_fuel",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "firmalife:smoking_fuel",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:enchanting_fuels",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "tfc:blast_furnace_fuel",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "tfc:firepit_fuel",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "tfc:forge_fuel",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "tfg:bloomery_basic_fuels",
          "confidence": 0.75
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:charcoal_dust",
          "source": "runtime-items",
          "text": "Burns at Yellow White٭٭ for 00:36",
          "key": "runtime-tooltip",
          "label": "Charcoal Dust",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:chipped_coal_gem",
          "source": "runtime-items",
          "text": "Burns at White for 00:28",
          "key": "runtime-tooltip",
          "label": "Chipped Bituminous Coal",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:coal_dust",
          "source": "runtime-items",
          "text": "Burns at White for 00:36",
          "key": "runtime-tooltip",
          "label": "Coal Dust",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:flawed_coal_gem",
          "source": "runtime-items",
          "text": "Burns at White for 00:57",
          "key": "runtime-tooltip",
          "label": "Flawed Bituminous Coal",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "tfc:story/charcoal",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/story/charcoal.json",
          "text": "A Better Fuel",
          "key": "advancement-title:title",
          "label": "A Better Fuel",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "tfc:story/charcoal",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/story/charcoal.json",
          "text": "Get some charcoal from a charcoal pit",
          "key": "advancement-description:description",
          "label": "A Better Fuel",
          "item_ref_count": 1
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/charcoal_pit",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/charcoal_pit.json",
          "text": "Charcoal Pit",
          "key": "guide-page:name",
          "label": "Charcoal Pit",
          "item_ref_count": 8,
          "count": 6
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/charcoal_pit",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/charcoal_pit.json",
          "text": "The Charcoal Pit is a way of obtaining Charcoal. Charcoal pits are made with Log Piles. To place a log pile, and while holding a Log. More logs can be inserted by either pressing directly while holding a log, or by pressing with something else to open the interface.",
          "key": "guide-page:pages.0.text",
          "label": "Charcoal Pit",
          "item_ref_count": 8,
          "count": 6
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/charcoal_pit",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/charcoal_pit.json",
          "text": "The Log Pile",
          "key": "guide-page:pages.1.name",
          "label": "Charcoal Pit",
          "item_ref_count": 8,
          "count": 6
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/charcoal_pit",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/charcoal_pit.json",
          "text": "Log piles need a solid block under them to be placed. They are highly flammable.",
          "key": "guide-page:pages.1.text",
          "label": "Charcoal Pit",
          "item_ref_count": 8,
          "count": 6
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/charcoal_pit",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/charcoal_pit.json",
          "text": "The charcoal pit is formed by surrounding log piles with solid, non-flammable blocks. The amount of charcoal produced is proportional to the amount of logs contained inside the log piles. To start the burning process, light one of the log piles, and then cover it. If it worked, you should see smoke particles rise up from the structure.",
          "key": "guide-page:pages.2.text",
          "label": "Charcoal Pit",
          "item_ref_count": 8,
          "count": 6
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/charcoal_pit",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/charcoal_pit.json",
          "text": "The building of one possible charcoal pit, in layers.",
          "key": "guide-page:pages.3.text",
          "label": "Charcoal Pit",
          "item_ref_count": 8,
          "count": 6
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/charcoal_pit",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/charcoal_pit.json",
          "text": "After the charcoal pit burns out and stops smoking, you will be left with Charcoal piles. The charcoal pile contains up to 8 layers of Charcoal. Dig it with a shovel to obtain the charcoal items. Charcoal piles can be added to or placed with .",
          "key": "guide-page:pages.4.text",
          "label": "Charcoal Pit",
          "item_ref_count": 8,
          "count": 6
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/charcoal_pit",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/charcoal_pit.json",
          "text": "The charcoal pile.",
          "key": "guide-page:pages.5.text",
          "label": "Charcoal Pit",
          "item_ref_count": 8,
          "count": 6
        },
        {
          "kind": "runtime_item",
          "id": "minecraft:charcoal",
          "source": "runtime-items",
          "text": "Burns at Yellow White٭٭ for 1:48",
          "key": "runtime-tooltip",
          "label": "Charcoal",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "tfc:world/coal",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/world/coal.json",
          "text": "Carboniferous",
          "key": "advancement-title:title",
          "label": "Carboniferous",
          "item_ref_count": 2
        },
        {
          "kind": "advancement",
          "id": "tfc:world/coal",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/world/coal.json",
          "text": "Find Bituminous Coal or Lignite",
          "key": "advancement-description:description",
          "label": "Carboniferous",
          "item_ref_count": 2
        },
        {
          "kind": "advancement",
          "id": "tfc:world/minerologist",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/world/minerologist.json",
          "text": "Minerologist",
          "key": "advancement-title:title",
          "label": "Minerologist",
          "item_ref_count": 16
        },
        {
          "kind": "advancement",
          "id": "tfc:world/minerologist",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/world/minerologist.json",
          "text": "Find every non-metal mineral in TFC",
          "key": "advancement-description:description",
          "label": "Minerologist",
          "item_ref_count": 16
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Ores and Minerals",
          "key": "guide-page:name",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Ores and Minerals in TFC are rare - unlike Vanilla, ores are found in massive, sparse, yet rare veins that require some prospecting to locate. Different ores will also appear in different rock types, and at different elevations, meaning finding the right rock type at the right elevation is key to locating the ore you are looking for.",
          "key": "guide-page:pages.0.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "In addition, some ores are Graded. Ore blocks may be Poor, Normal, or Rich, and different veins will have different concentrations of each type of block. Veins that are richer are more lucrative. The next several pages show the different types of ores, what they look like, and where to find them.",
          "key": "guide-page:pages.1.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Native Copper is an ore of Copper metal. It can be found in Igneous Extrusive rocks, at elevations above y=40. It can also be found in deposits in rivers, which can be panned.",
          "key": "guide-page:pages.2.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Native Copper",
          "key": "guide-page:pages.2.title",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Native Copper Ores in Dacite.",
          "key": "guide-page:pages.3.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Native Gold is an ore of Gold metal. It can be found at elevations below y=70, but deeper veins are larger and richer. It can be found in Igneous Extrusive and Igneous Intrusive rocks. It can also be found in deposits in rivers, which can be panned.",
          "key": "guide-page:pages.4.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Native Gold",
          "key": "guide-page:pages.4.title",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Native Gold Ores in Diorite.",
          "key": "guide-page:pages.5.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Native Silver is an ore of Silver metal. Small poor veins can be found in Granite or Diorite in uplift regions, above y=90. Larger and richer veins can be found in Granite, Diorite, Schist, and Gneiss deep underground below y=20. It can also be found in deposits in rivers, which can be panned.",
          "key": "guide-page:pages.6.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Native Silver",
          "key": "guide-page:pages.6.title",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Native Silver Ores in Granite.",
          "key": "guide-page:pages.7.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Tetrahedrite is an ore of Copper metal. It can be found at any elevation, but deeper veins are often richer. It can be found in Metamorphic rocks.",
          "key": "guide-page:pages.8.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Tetrahedrite Ores in Schist.",
          "key": "guide-page:pages.9.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Malachite is an ore of Copper metal. It can be found primarily in Marble or Limestone, Chalk, and Dolomite. It can be found at most elevations, however deeper veins are often larger and richer.",
          "key": "guide-page:pages.10.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Malachite Ores in Marble.",
          "key": "guide-page:pages.11.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Cassiterite is an ore of Tin metal. It can be found in Igneous Intrusive rocks at high elevation, above y=80 in uplift regions or in dikes. It can also be found in deposits in rivers, which can be panned.",
          "key": "guide-page:pages.12.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Cassiterite Ores in Diorite.",
          "key": "guide-page:pages.13.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Bismuthinite is an ore of Bismuth metal. It can be found in Sedimentary rocks near the surface, or larger and richer veins in Igneous Intrusive rocks deep underground.",
          "key": "guide-page:pages.14.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Bismuthinite Ores in Shale.",
          "key": "guide-page:pages.15.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Garnierite is an ore of Nickel metal. It can be found at elevations below y=0. It can be found primarily in Gabbro deep underground. Smaller, rarer veins can also be found in any Igneous Intrusive rock.",
          "key": "guide-page:pages.16.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Garnierite Ores in Gabbro.",
          "key": "guide-page:pages.17.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Hematite is an ore of Iron metal. It can be found in large veins in any Igneous Extrusive rocks near the surface.",
          "key": "guide-page:pages.18.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Hematite Ores in Andesite.",
          "key": "guide-page:pages.19.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Magnetite is an ore of Iron metal. It can be found in large veins in any Sedimentary rocks near the surface.",
          "key": "guide-page:pages.20.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Magnetite Ores in Limestone.",
          "key": "guide-page:pages.21.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Limonite is an ore of Iron metal. It can be found in large veins in any Sedimentary rocks near the surface.",
          "key": "guide-page:pages.22.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Limonite Ores in Chalk.",
          "key": "guide-page:pages.23.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Sphalerite is an ore of Zinc metal. Small, poor veins can be found in Igneous Extrusive rocks near the surface, and large richer veins can be found in Igneous Intrusive rocks deep underground.",
          "key": "guide-page:pages.24.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Sphalerite Ores in Quartzite.",
          "key": "guide-page:pages.25.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Lignite is a type of low-grade Coal ore. It can be found in very large flat deposits near the surface in Sedimentary rocks.",
          "key": "guide-page:pages.26.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Lignite in Dolomite.",
          "key": "guide-page:pages.27.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Bituminous Coal",
          "key": "guide-page:pages.28.title",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Bituminous Coal is a type of mid-grade Coal ore. It can be found in very large flat deposits near the surface in Sedimentary rocks.",
          "key": "guide-page:pages.28.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Bituminous Coal in Chert.",
          "key": "guide-page:pages.29.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Kaolinite is a soft Mineral which is used in the construction of Fire Clay. It can be found spawning at high altitudes in Plateaus, Old Mountains, and Highlands, at a temperature of at least 18°C, with a rainfall of at least 300mm. The Blood Lily flower grows on Kaolin clay.",
          "key": "guide-page:pages.30.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Variants of kaolin clay.",
          "key": "guide-page:pages.31.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Graphite is a Mineral which is used in the construction of Fire Clay. It can be found in Gneiss, Marble, Quartzite, and Schist rocks, in elevations below y=60 and above y=-30.",
          "key": "guide-page:pages.32.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Graphite in Gneiss.",
          "key": "guide-page:pages.33.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Cinnabar is a Mineral which can be ground in the Quern to obtain Redstone Dust. It can be found in veins deep underground, in Quartzite, Gneiss, Phyllite, and Schist.",
          "key": "guide-page:pages.34.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Cinnabar in Quartzite.",
          "key": "guide-page:pages.35.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Cryolite is a Mineral which can be ground in the Quern to obtain Redstone Dust. It can be found in veins deep underground, in Granite, and Diorite.",
          "key": "guide-page:pages.36.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Cryolite in Granite.",
          "key": "guide-page:pages.37.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Saltpeter is a Mineral which can be ground in the Quern, and then used in the crafting of Gunpowder. It can be found in very large flat deposits near the surface in Sedimentary rocks.",
          "key": "guide-page:pages.38.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/the_world/ores_and_minerals",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/the_world/ores_and_minerals.json",
          "text": "Saltpeter in Shale.",
          "key": "guide-page:pages.39.text",
          "label": "Ores and Minerals",
          "item_ref_count": 32,
          "count": 60
        }
      ],
      "aliases": []
    },
    {
      "id": "slot:fill",
      "label": "Fill",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 1255,
      "evidence": [
        {
          "kind": "block_tag",
          "id": "ad_astra:passes_flood_fill",
          "confidence": 0.75
        }
      ],
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "ad_astra:gravity_normalizer",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!data/ad_astra/advancements/gravity_normalizer.json",
          "text": "Make Newton Proud",
          "key": "advancement-title:title",
          "label": "Make Newton Proud",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "ad_astra:gravity_normalizer",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!data/ad_astra/advancements/gravity_normalizer.json",
          "text": "Construct a Gravity Normalizer, allowing you to control gravity in the local area",
          "key": "advancement-description:description",
          "label": "Make Newton Proud",
          "item_ref_count": 1
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Make sure you have at least the following:",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "- A Space Suit and a full drum of breathable gas",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "- A Rocket with another full drum of spare Fuel",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "- A Launch Pad so you can return",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "- Plenty of food and water",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "- A weapon and shield in case the Moon's haunted...",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Make sure you're ready!",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Pre-Launch Preparations",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "one_completed",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Find yourself a little short of breath? You'll need something to breathe up in space, so you'll want to look at producing any of these compressed gases, based off real-world ones used for deep sea diving!",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "You can fill your space suit with them the same way you'd fill up a bucket, or if you bring along a Gas Tank you'll be able to \"drink\" from it to replenish your suit without taking it off.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Nitrox is probably the easiest to produce on Earth, while you can set up Heliox-3 on the Moon.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "A full space suit (2500mB) will last about 45 minutes.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Because pure Oxygen is deadly",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Compressed Breathable Gases",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "By keeping the above items in your inventory, you'll be able to build a Space Station in orbit once you launch! There's not really much to do up there, and they're totally optional, but if you want a void dimension with no gravity for whatever reason, or just really like sky diving, they're an option.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "For when you really want to get away from it all",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Space Stations",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Chorus Fruit serves a pretty crucial role on the Moon - it will be your main source of Nitrogen. To get started, find some Chorus Plants, parkour up to the top, and break the Chorus Flowers. Breaking the plant from the bottom will not get you any of the Chorus Flowers! Next up, grow the Chorus Flowers in an Electric Greenhouse to get a renewable source of Chorus Fruit, and from there, you can process them into Biomass and Nitrogen with a Brewery and Fermenter respectively. You can also distill the Biomass into Carbon for more Rocket Fuel!",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Is this the End?",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "or(item(minecraft:chorus_fruit))",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Chorus Fruit",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "There are very few energy sources available on the Moon, but these Solar Panels have a lovely view of the Sun from up here!",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "On the moon, each Solar Panel provides the equivalent of 32 EU/t during the day with no fuel or upkeep needed, but they only produce an eighth of that down on the Earth's surface. Bring along a whole bunch of them!",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Praise the Sun!",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "or(item(gtceu:lv_1a_energy_converter)item(gtceu:lv_4a_energy_converter)item(gtceu:mv_1a_energy_converter)item(gtceu:mv_4a_energy_converter)item(gtceu:hv_1a_energy_converter)item(gtceu:lv_8a_energy_converter))",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Solar Panels",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "So far you may be surviving, but do you want to truly thrive? If you're wanting to set up a more permanent base, look no further than the Air Distributor! Feed it one of the compressed gases from earlier and it will \"oxygenate\" a large enclosed area, letting you take your helmet off, grow crops, and place a water source without it freezing.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Any solid block will work to build your base out of, but you'll want a proper airlock to ensure the gas doesn't all escape in a vortex.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "{@pagebreak}",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "The oxygenated area will be at a constant 15C for any crops you'd like to grow (though the Electric Greenhouse doesn't mind), and the water source staying liquid will also let you easily electrolyze it for a source of Oxygen. You can also use a Diode block to transfer power through the walls while keeping them sealed, just like in a cleanroom.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "A holiday home",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Setting up a permanent base",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "The Moon is full of Helium-3, another important resource, but it's stuck inside the stone! There's two ways to get your hands on some:",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "1) If you haven't set up any stone-related automation before, here's a quick introduction: A Rock Crusher can produce infinite raw stone, which a Macerator crushes into dust for a Centrifuge to process.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Here on the Moon, Asurine can thus be processed into an infinite source of Helium-3!",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "2) Pump it out of the ground with a Fluid Rig This method is simpler, but doesn't come with any useful byproducts.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Helium-3 is used for several things, of which the most important is making more air to breathe via Heliox-3! You can also use it as \"fertilizer\" for Chorus Fruit in the Electric Greenhouse, and eventually as another fuel for Fusion Power.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Clown gas",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Helium-3",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "You had to leave your fancy jetpack at home, there's no atmosphere for airplanes, and horses can't survive, so how are you supposed to get around?",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "The Rover is a vehicle that can hold up to two passengers, runs on combustible fuels (Diesels, Gasolines, and Rocket Fuel), and even has an inventory and radio that can play real-world radio stations!",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Your getaway vehicle",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Space Rover",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "A Zip Gun can be filled up with any of the below Compressed Gases to shoot out air, pushing you around in low gravity environments. They're essential if you want to build anything in orbit!",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Like a jetpack in the palm of your hand",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Zip Gun",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "The Oxygen Detector is a simple machine that emits a redstone signal whenever it's in an oxygenated area.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "The Vent is a solid block that can also let breathable air through without causing a vortex.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "The Gravity Normalizer can change the gravity of an area within an enclosed space similar to how an Air Distributor works, but is a little too expensive at the moment.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Home comforts",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "More Moon base machines",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Introduce yourself to the Moon by planting a flag with whatever image you like on it!",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "ftbfiltersystem:item_tag(ad_astra:flags)",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Any Flag",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "Flag Planter",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "In TerraFirmaGreg, transporting items across dimensions or over long distances requires you to build special multiblock structures.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "This system is powerful and flexible, offering multiple settings that let you customize logistics to perfectly suit your needs.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "This is the perfect system to transport all of your passively-produced resources to exactly where they're needed on other planets! And it comes with a powerful configuration system to let you control everything from a single place.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "To set it up, you'll need three components:",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/moon",
          "source": "file:minecraft/config/ftbquests/quests/chapters/moon.snbt",
          "text": "• Interplanetary Railgun — this is the sender that launches items across space.",
          "key": "quest-snbt",
          "label": "Eager to launch into the final frontier? We totally get the enthusiasm but you can't just hurl yourself into space and hope to survive! Preparation is key to survival, or you'll just end up back in the Stone Age banging rocks together for a few seconds before suffocating to death.",
          "item_ref_count": 32
        }
      ],
      "aliases": []
    },
    {
      "id": "slot:place",
      "label": "Place",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 1208,
      "evidence": [
        {
          "kind": "block_tag",
          "id": "minecraft:dead_bush_may_place_on",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "alekiships:can_place_in_compartments",
          "confidence": 0.75
        }
      ],
      "semantic_evidence": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Extraterrestrial Crops",
          "key": "guide-page:name",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Extraterrestrial Crops",
          "key": "guide-page:pages.0.title",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "It turns out Earth isn't the only celestial body with life. During your travels, you may come across other kinds of edible flora. These all use the same mechanics you're used to on Earth (with some exceptions), and can be grown either in normal Farmland, in a Firmalife Greenhouse, or in a GregTech Electric Greenhouse depending on your needs.",
          "key": "guide-page:pages.0.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "An Air Distributor will also keep an enclosed area a stable 15 °C. Due to technical reasons, this temperature can't be displayed in the Jade tooltip while on other planets, so you'll have to use your inventory's Climate tab to check the temperature. Another alternative is to use Firmalife's Greenhouse, which doesn't provide as much output, but also ignores all climate conditions.",
          "key": "guide-page:pages.1.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "- Amber Root - Blossom Berry - Bolux Mushroom - Bulbkin - Chalmie Mushroom - Chorus Fruit - Nox Berry",
          "key": "guide-page:pages.2.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Chorus Fruit",
          "key": "guide-page:pages.4.title",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Location: The Moon Biomes: Chorus Forest, Chorus Thicket Chorus Plants grow in tall formations. Breaking the stem will yield nothing, while breaking the Flowers at the top will yield Chorus Fruits or a Flower. Chorus Fruits can be eaten for Fruit, cooked and eaten as Popped Chorus Fruit, or can be distilled into Nitrogen.",
          "key": "guide-page:pages.4.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Chorus Flowers can also be grown manually by planting them on any Gravel or Sand.",
          "key": "guide-page:pages.5.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Amber Root",
          "key": "guide-page:pages.6.title",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Location: Mars Temperature: -150 - 15 °C Hydration: 0 - 40 % Nutrient: Phosphorus Amber root is a single block crop. Amber seeds can be planted on farmland and will produce Amber Shoots which can be eaten for Grain.",
          "key": "guide-page:pages.6.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Wild Amber Root grows in any climate on Amber and Rusticus Mycelium.",
          "key": "guide-page:pages.7.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Location: Mars Temperature: -80 - 30 °C Hydration: 70 - 100 % Nutrient: Potassium Blossom Berry is a single block crop. Blossom Berry Seeds can be planted on farmland and will produce Blossom Berries which can be eaten for Fruit.",
          "key": "guide-page:pages.8.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Blossom Berry",
          "key": "guide-page:pages.8.title",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Wild Blossom Berry grows in any climate on Amber, Rusticus, or Sangnum Mycelium.",
          "key": "guide-page:pages.9.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Location: Mars Temperature: -150 - 15 °C Hydration: 0 - 40 % Nutrient: Phosphorus Bolux Mushrooms are a single block crop. Bolux Mushroom Spores can be planted on farmland and will produce Bolux Mushrooms which can be eaten for Vegetables. It is recommended to cook Bolux Mushrooms before eating.",
          "key": "guide-page:pages.10.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Bolux Mushroom",
          "key": "guide-page:pages.10.title",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Wild Bolux Mushrooms grow in any climate on Amber, Rusticus, or Sangnum Mycelium.",
          "key": "guide-page:pages.11.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Location: Mars Temperature: -100 - 30 °C Hydration: 0 - 60 % Nutrient: Nitrogen Bulbkins are a spreading crop. Bulbkin Seeds can be planted on farmland and will place up to two Bulbkin Blocks on the ground next to it while it is mature. If the bulbkin blocks are harvested, and the plant matures again, it can grow more bulbkins. Bulbkins can be eaten for Fruit or processed into Glowstone Dust.",
          "key": "guide-page:pages.12.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Wild Bulbkins grow in any climate on Amber or Sangnum Mycelium.",
          "key": "guide-page:pages.13.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Chalmie Mushroom",
          "key": "guide-page:pages.14.title",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Location: Mars Temperature: -150 - 15 °C Hydration: 0 - 40 % Nutrient: Phosphorus Chalmie Mushrooms are a single block crop. Chalmie mushroom spores can be planted on farmland and will produce Chalmie Mushrooms which can be eaten for Vegetables. It is recommended to cook Chalmie Mushrooms before eating.",
          "key": "guide-page:pages.14.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Wild Chalmie Mushrooms grow in any climate on Amber, Rusticus, and Sangnum Mycelium.",
          "key": "guide-page:pages.15.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Nox Berry",
          "key": "guide-page:pages.16.title",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Location: Mars Temperature: -80 - 30 °C Hydration: 50 - 100 % Nutrient: Potassium Nox Berries are a single block crop. Nox berry seeds can be planted on farmland and will produce Nox Berries which can be eaten for Fruit. It is recommended to cook Nox Berries before eating.",
          "key": "guide-page:pages.16.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Wild Nox Berries grow in any climate on Rusticus and Sangnum Mycelium.",
          "key": "guide-page:pages.17.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Before landing on Mars, there are a few important things to keep in mind.",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "First, like the Moon, Mars will have some hostile surface mobs as well. If you haven't already figured out how to make your Space Suit stay with you after dying or invested in new EV-tier weaponry, you'll have a rough time.",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "The planet is mostly a desert world, dotted with \"islands\" where you’ll find water, lush fauna, and much safer places to build your first base. Meanwhile, the open desert is extremely dangerous if you don't watch your step, but contains some other crucial resources!",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "{@pagebreak}",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "All fluid veins are spread across Mars, except for Heavy Ammoniacial Water which can only be found outside of the desert. That’s another good reason to set up your base there.",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Energy-wise, Mars will not be kind to you at first.",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Nearly every common energy generation method is disabled or worthless on the planet, which means you’ll have to rely almost entirely on Fission to power your base.",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "At the very beginning, we recommend bringing along some filled batteries such as Lapotron Crystals to get started, and then look towards setting up Thorium Fuel Rods.",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Achieving energy stability on Mars will take time and effort, but once your systems are running smoothly, you’ll feel like unlimited power is right in the palm of your hand.",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "And one last tip: don’t kill everything you see. Some animals can be ranched, and you’ll definitely need them later in your progression.",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Be sure to check out the Space Survival chapter for more Mars-related tips too!",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Stay alert, plan ahead, and Mars will reward your courage.",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "The Red Planet",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Land on Mars",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "I'm on Mars!",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "one_completed",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "The water on Mars is Semiheavy which means it contains one Deuterium atom, and is also full of Ammonia, a great antifreeze, which is how it can stay liquid despite the very low temperatures.",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "It's technically drinkable, but you'll likely want to distill it into normal Water first. You can also move source blocks of Semiheavy Ammoniacal Water around and get more of it via an Aqueous Accumulator, just like regular Water.",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "You can also centrifuge the Ammonium Chloride out from it and electrolyze it as a source of Nitrogen. Easy Nitrox from just one source!",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Fancy a drink?",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Semiheavy Ammoniacal Water",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Dotted around the vast Martian deserts are small \"islands\" of life and vegetation, which are probably where you'll want to set up your base. Surviving here will be much easier than in the desert!",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Is that a dinosaur?!",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "The Martian Jungles",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "The majority of Mars is covered in a vast desert. Not much lives here except the occasional Stackatick, and of course the giant Sandworms. If this is your first time here, you'll want to find somewhere more hospitable! Otherwise, you'll want to keep searching...",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Sand, sand everywhere",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Visit the Martian Deep Desert biome",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "The Martian Desert",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "If you make too much noise in the desert, you might attract the attention of the mighty Sandworm! It's more of an environmental hazard than a boss to kill, so your best way of surviving it is to run away!",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "If you manage to hit its head enough times with explosion damage, it will leave you alone... for a while.",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Thankfully, the Sandworm can only damage entities, not buildings, and is only attracted to the footsteps of players, so any way to keep your own feet off the ground won't anger it. The atmosphere is too thin for airplanes and hang gliders, but there are other tools...",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "SHAI-HULUD",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "The Sandworm",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Ostrum Deposits are immovable blocks that you can only find in the Martian Deep Desert, far away from any of the more lush areas. They spawn in small blobs, and can be broken to make space for your Ostrum Harvester if they're on a slope. Try using a Spyglass to find them from a distance!",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "You'll have to figure out some long-distance logistics to ship your Ostrum back to your base. (Try a train!)",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Ostrum is a crucial resource for both EV progression and making infinite ores from Mars.",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Spice Melange",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "The spice must flow",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/mars",
          "source": "file:minecraft/config/ftbquests/quests/chapters/mars.snbt",
          "text": "Ostrum Deposits",
          "key": "quest-snbt",
          "label": "Before landing on Mars, there are a few important things to keep in mind.",
          "item_ref_count": 32
        }
      ],
      "aliases": []
    },
    {
      "id": "slot:launch",
      "label": "Launch",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 1185,
      "evidence": [
        {
          "kind": "block_tag",
          "id": "ad_astra:launch_pads",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "tfg:cannot_launch_in_railgun",
          "confidence": 0.75
        }
      ],
      "semantic_evidence": [
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/categories/the_first_launch",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/categories/the_first_launch.json",
          "text": "The First Launch",
          "key": "guide-page:name",
          "label": "The First Launch",
          "item_ref_count": 1
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/categories/the_first_launch",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/categories/the_first_launch.json",
          "text": "How to build your first rocket and safely travel to the moon and back.",
          "key": "guide-page:description",
          "label": "The First Launch",
          "item_ref_count": 1
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "hv__high_voltage",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Energium Crystals store a significant amount of EU. They're also arguably cheaper.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Science-fiction batteries!",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Super Batteries",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "The HV Autoclave - despite being optional - is a machine you'll definitively want to grab.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "This unlocks unique Batteries that are far superior to their standard counterparts.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Home-grown batteries",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "HV Autoclave",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "The HV Chemical Bath is used to dye Lenses using Chemical Dye. This will let you engrave more types of Wafers, and cool other kinds of Hot Ingots.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Please don't bathe with your toaster...",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "High Voltage Bathing",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "You now have access to two new Maintenance Hatches.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "The Automatic Maintenance Hatch is pretty simple - it eliminates the need for Maintenance, forever.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "The Configurable Maintenance Hatch is more interesting. You can configure it to cut off 10%% duration on recipes, at the cost of making Maintenance happen three times as fast. That is 16 real hours of activity. Additionally, you can use it on Multiblock Generators to increase the duration of the recipe, which indirectly increases their fuel efficiency by 10%%!",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Reminder: You can put Tape in the maintenance Hatch to automatically fix problems.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "{@pagebreak}",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Lore: The Automatic Maintenance Hatch texture and mechanic comes from TecTech, an addon mod originally made for GregTech: New Horizons.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "You'll see more from TecTech if you stick with us with the late game.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "I have become maintenance, eater of tape",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Advanced Maintenance",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Just like MV, you'll need to make higher-tier components.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "The painful Flawless Gems have been replaced with slightly-more-painful Vitrified Ender Pearls. Also notice that the Motors require 2x Silver Cables.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Hulls and Machines use Gold Cables.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Finally, a use for that stockpile of Gold!",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Tips to make life less painful:",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "- Use Electrotine decomposition and Ore Processing for Electrum.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "- Use the Chemical Bath with Mercury for Gold and Silver from a wide variety of ores.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "We call this \"fun\"",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "High Voltage Components",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "The HV Assembler is the next step in progression, but also unlocks a handful of useful utilities - check around this quest for more.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "You will need this HV Assembler to make your Rocket.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "HV Assembler",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "You'll want to grab these two lenses. The Simple System on Chip will be used for the best ULV Circuit recipe, while the LPIC Wafer is required for your next Energy Hatch.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "I see the world in colour",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "ULV Circuits are used in some AE2 recipes and Create recipes.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Best ULV circuits!",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "The Low Power Integrated Circuit requires an HV Cutter inside your Cleanroom.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "HV Energy Hatches will be nice additions so you can run your Multiblocks (mainly your Electric Blast Furnace and Large Chemical Reactor) directly off a HV line.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Noticing a pattern here?",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Upgrade your EBF II",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "The HV Chemical Reactor unlocks a ton of new recipes. Ah, who doesn't love the ever-growing demand on more advanced Chemistry?",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Note: We've actually unlocked a ton of new content by reaching HV. However, not every processing line or item is crucial right now. It'd make the HV chapter far too dense if we included everything!",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "You could still check out the EV Chapter if you want to push yourself.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "High Voltage Chemistry",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Titanium requires a slightly more involved process than the previous materials to process. To get started, you'll need Rutile, which can be obtained by processing Ilmenite that you can get from Bauxite, Aluminium, Armalcolite and Desh, which can be found in massive quantities on The Moon.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Once you've gotten enough Rutile, your journey to Titanium continues in the next quest. There's no need to rush things.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Tip: Ore processing for Bauxite is ridiculously good! It can easily triples your Rutile yield.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "...means it WASN'T Titanium!",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Almost Titanium",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "To get your grubby hands on some Titanium, Rutile must first be reduced to Titanium Tetrachloride in an HV Chemical Reactor, then reduced again in an EBF with Magnesium to finally arrive at Titanium.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "The Carbon, Chlorine, Oxygen, and Magnesium used in the process can be perfectly recovered from the byproduct Carbon Monoxide and Magnesium Chloride with no loss. If you set up a system to recover them, the only material that you'll need to provide for Titanium is Rutile!",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Titanium Tetrachloride also acts as a catalyst in the production of plastics. A touch of TiCl₄ in the polymerisation process can increase the polymer yield to 133%%. Naturally, this is completely optional.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Is it titanium yet?",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Click here to complete this quest/task",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Almost Titanium²",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "By this point, normal Cables have such insignificant loss that superconductors aren't a requirement. That being said, they're still useful for being able to split and combine cables on the fly, and these ones are half oxygen so they're cheap too!",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Superconductors are still required to craft Field Generators.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "HV Superconductors",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Nichrome Heating Coils increase the temperature of your Blast Furnace to 3,600K.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "These will be required to upgrade your primary EBF so you can make Titanium.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "Lore: In the original GregTech 5, there were only three types of coils. This was the last tier.",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/hv__high_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/hv__high_voltage.snbt",
          "text": "A long way to go before we beat the Sun",
          "key": "quest-snbt",
          "label": "hv__high_voltage",
          "item_ref_count": 32
        }
      ],
      "aliases": []
    },
    {
      "id": "slot:eat",
      "label": "Eat",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 1145,
      "evidence": [
        {
          "kind": "item_tag",
          "id": "species:cruncher_eats",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "tfg:auto_eat_blacklist",
          "confidence": 0.75
        }
      ],
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "firmalife:story/bacon",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!data/firmalife/advancements/story/bacon.json",
          "text": "Sizzle",
          "key": "advancement-title:title",
          "label": "Sizzle",
          "item_ref_count": 2
        },
        {
          "kind": "advancement",
          "id": "firmalife:story/bacon",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!data/firmalife/advancements/story/bacon.json",
          "text": "Cook some bacon",
          "key": "advancement-description:description",
          "label": "Sizzle",
          "item_ref_count": 2
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "queststfc_tips",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "This quest chapter gives you a lot, (and we do mean a lot) of tips on how to survive and thrive.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Each quest branch representing major mods in TerraFirmaGreg, with tips to make the most out of them. None of these quests are necessary for progression, although some may be locked behind main questline tasks.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Dear God that's a lot of branches...",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Tips and Tricks be here!",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Firmalife is a mod all about extending the agricultural and gastronomic experience in TerraFirmaCraft, with touches of miscellaneous features. Includes things such as decorations, more preservation methods, new foods, bees and a plethora of useful gadgets!",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Thrive in TerraFirmaCraft",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "ArborFirmaCraft is a mod that adds multiple new trees to TFG: some are variants of existing TFC trees, such as Ancient Kapok, while others are completely new, such as the Baobab and Hevea. Some of the plank and log textures of these trees may remind you of vanilla tree colors...",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "This section will go over Tree Tapping.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "As a bonus, all ArborFirmaCraft woods are compatible with FirmaLife, and Firma:Civ, we made sure of it!",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "tfc:field_guide tfc:arborfirmacraft/tapping_index",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Harvesting the Life-blood of Trees",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Deep, DEEP underground, past the Bedrock, you'll find The Beneath, a complex network of huge caves filled with dangers and strange flora and fauna. While The Beneath is completely optional, it contains a lot of resources as well as some quality of life improvements.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "tfc:field_guide tfc:beneath/beneath",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Journey to the Center of the Earth",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "The Beneath",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "TerraFirmaCraft is one of the main mods in TerraFirmaGreg. This branch mostly focuses on some of its survival features and other Add-Ons the modpack has, such as Aged Alcohol and Canes.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "The other two thirds of the modpack's name",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "TerraFirmaCraft Addons",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "To begin tapping trees, you'll need to forge a Tree Tap from working Copper Ingots on an Anvil.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "The process of tapping requires finding a suitable tree, a dedicated Tapping Index in the ArborFirmaCraft Field Guide section will tell you how to find these trees. There's also the Tapping Trees entry, which can be used to learn how to actually tap them.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "tfc:field_guide tfc:arborfirmacraft/tree_tapping",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Now with less jank!",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "one_completed",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Utilizing the Vacuum Chamber and some additional heating, you can process Latex from various trees into Raw Rubber Pulp through a specific process. You can then smelt together 3 Raw Rubber Pulp with a bit of Sulfur Dust in an Alloy Smelter to create Rubber Ingots, perfect for insulating cables and very much necessary to advance in the Voltaic Age and beyond...",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Tip: As you progress, there will be several ways to make this process cheaper and easier. Keep checking EMI! You'll be using Rubber for a long time, so it's well worth automating.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Insulate them cables",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Rubber Ingot",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Sticky Resin can be utilized as slime balls in some recipes. It is also key in the production of Resin Circuits and Resistors, which you'll need a fair bit of to advance into the Low Voltage age.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "To make sticky resin, you need to boil either Latex or Conifer Pitch in a Vat mixed with Wood Ash.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "No, it's not Rosin",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Sticky Resin",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Latex Trees can be found in temperate areas of the world that receive large amounts of rainfall. It can be tapped to obtain Latex, used to create both Sticky Resin, and Vulcanized Latex. The latter is used to produce Rubber and Rubber Gloves, which protect you from poisonous materials on contact.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "To complete this quest, you need to look at a block that has the tag #tfg:latex_logs. You can use EMI to learn which blocks have this tag by searching with that hashtag.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "tfc:field_guide tfc:arborfirmacraft/making_rubber",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Seek for the Tropics",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "ftbfiltersystem:item_tag(tfg:latex_logs)",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Any #tfg:latex_logs",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Latex Trees",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Syrup Trees can be found in temperate areas of the world that receive moderate amounts of rainfall. It can be tapped to obtain Syrup. Syrup can be used to create Tree Sugars, an alternative sugar among other uses.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "To complete this quest, you need to look at a block that has the tag #tfg:syrup_logs. You can use EMI to learn which blocks have this tag.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "tfc:field_guide tfc:arborfirmacraft/making_syrups",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Diabetes is Skyrocketing",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "{quests.tasktype.lookat} #tfg:syrup_logs",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "#tfg:syrup_logs",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Syrup Trees",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Tree Sugar is a sugar substitute, mostly used as a food ingredient.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "To make it, you'll need to boil Tree Sap to get Concentrated Sap, then boil it again to get Syrup. Finally, use a workbench to turn a Bucket of Syrup into Tree sugar.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Keep in mind that all these boiling processes require a stick in the Vat or pot.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Sweet!",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "or(item(afc:maple_sugar)item(afc:birch_sugar))",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Maple or Birch Sugar",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Tree Sugar",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "If you have advanced to the MV age, you can make a Refrigerator, consuming power in exchange for the best universal food preservation trait for all food items. This includes raw and cooked food, and combined foods like sandwiches.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "This thing may be overpowered",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Beekeeping has a plethora of uses. Not only do bees fertilize nearby crops (assuming you have the correct trait), you will be able to obtain useful resources such as Wax and Honey. Breed bees to achieve better stats.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "The Field Guide offers an extensive explanation of all beekeeping mechanics.",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "tfc:field_guide tfc:firmalife/beekeeping",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "They're so small now...",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Click here to complete this quest/task",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "Bees don't like it when people disrupt them and harvest their precious honey and wax or move their frames around. There are three ways to do it safely:",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/queststfc_tips",
          "source": "file:minecraft/config/ftbquests/quests/chapters/queststfc_tips.snbt",
          "text": "* Harvesting at night time",
          "key": "quest-snbt",
          "label": "queststfc_tips",
          "item_ref_count": 32
        }
      ],
      "aliases": []
    },
    {
      "id": "slot:empty",
      "label": "Empty",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 1093,
      "evidence": [
        {
          "kind": "block_tag",
          "id": "create:movable_empty_collider",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "firmalife:empty_wine_bottles",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "tfc:empty_jar_with_lid",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "tfc:fluid_item_ingredient_empty_containers",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "tfg:empty_dna_syringes",
          "confidence": 0.75
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "create_connected:copycat_fence_gate",
          "source": "runtime-items",
          "text": "Applies _held item_ as its _material_ if possible. _Click again_ to cycle _orientation_ or _powered_ state. Use a _Wrench_ to _reset_ the material.",
          "key": "lang:block.create_connected.copycat_fence_gate.tooltip.behaviour1",
          "label": "Copycat Fence Gate",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:copycat_fence_gate",
          "source": "runtime-items",
          "text": "When R-Clicked",
          "key": "lang:block.create_connected.copycat_fence_gate.tooltip.condition1",
          "label": "Copycat Fence Gate",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:copycat_fence_gate",
          "source": "runtime-items",
          "text": "_Converts_ any _full block_ into a functional fence gate.",
          "key": "lang:block.create_connected.copycat_fence_gate.tooltip.summary",
          "label": "Copycat Fence Gate",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:vanilla_fence_gate_compat",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Stripped Oak Wood Fence gate",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:vanilla_fence_gate_compat",
          "source": "runtime-items",
          "text": "Material: Weathered Cut Copper",
          "key": "runtime-tooltip",
          "label": "Stripped Oak Wood Fence gate",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:empty_jar_with_stainless_steel_lid",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Empty Jar With Stainless Steel Lid",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "firmalife:story/jars",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!data/firmalife/advancements/story/jars.json",
          "text": "minecraft.jar",
          "key": "advancement-title:title",
          "label": "minecraft.jar",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "firmalife:story/jars",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!data/firmalife/advancements/story/jars.json",
          "text": "Craft an empty jar.",
          "key": "advancement-description:description",
          "label": "minecraft.jar",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "firmalife:story/root",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!data/firmalife/advancements/story/root.json",
          "text": "Firmalife Story",
          "key": "advancement-title:title",
          "label": "Firmalife Story",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "firmalife:story/root",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!data/firmalife/advancements/story/root.json",
          "text": "Things to do with Firmalife.",
          "key": "advancement-description:description",
          "label": "Firmalife Story",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:empty_jar_with_lid",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Empty Jar With Lid",
          "item_ref_count": 1
        },
        {
          "kind": "kubejs_tooltip",
          "id": "kubejs:tooltips/79",
          "source": "file:minecraft/kubejs/client_scripts/tooltips.js",
          "text": "Fluid Capacity: %d mB",
          "key": "kubejs-tooltip:gtceu.universal.tooltip.fluid_storage_capacity",
          "label": "Fluid Capacity: %d mB",
          "item_ref_count": 2
        },
        {
          "kind": "runtime_item",
          "id": "tfc:ceramic/jug",
          "source": "runtime-items",
          "text": "Fluid Capacity: 100 mB",
          "key": "runtime-tooltip",
          "label": "Ceramic Jug",
          "item_ref_count": 1
        },
        {
          "kind": "kubejs_tooltip",
          "id": "kubejs:tooltips/77",
          "source": "file:minecraft/kubejs/client_scripts/tooltips.js",
          "text": "Fluid Capacity: %d mB",
          "key": "kubejs-tooltip:gtceu.universal.tooltip.fluid_storage_capacity",
          "label": "Fluid Capacity: %d mB",
          "item_ref_count": 3
        },
        {
          "kind": "runtime_item",
          "id": "tfc:hematitic_glass_bottle",
          "source": "runtime-items",
          "text": "Fluid Capacity: 400 mB",
          "key": "runtime-tooltip",
          "label": "Glass Bottle",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:hematitic_glass_bottle",
          "source": "runtime-items",
          "text": "Hematitic Glass",
          "key": "runtime-tooltip",
          "label": "Glass Bottle",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:olivine_glass_bottle",
          "source": "runtime-items",
          "text": "Fluid Capacity: 400 mB",
          "key": "runtime-tooltip",
          "label": "Glass Bottle",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:olivine_glass_bottle",
          "source": "runtime-items",
          "text": "Olivine Glass",
          "key": "runtime-tooltip",
          "label": "Glass Bottle",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "tfc:story/glass_bottle",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/story/glass_bottle.json",
          "text": "Glass Bottle",
          "key": "advancement-title:title",
          "label": "Glass Bottle",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "tfc:story/glass_bottle",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/story/glass_bottle.json",
          "text": "Make a glass bottle",
          "key": "advancement-description:description",
          "label": "Glass Bottle",
          "item_ref_count": 1
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "Glassworking is the process of turning sand into glass. To start, you must create a Glass Batch, of which there are four types: 1. Silica, from white sand. 2. Hematitic, from yellow, red, or pink sand. 3. Olivine, from green or brown sand. 4. Volcanic, from black sand.",
          "key": "guide-page:pages.0.text",
          "label": "Glassworking",
          "item_ref_count": 9,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "Silica Glass Batch",
          "key": "guide-page:pages.1.title",
          "label": "Glassworking",
          "item_ref_count": 9,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "Glass batches can then be crafted using one of the aforementioned colors of sand, plus Lime and a type of Potash.",
          "key": "guide-page:pages.1.text",
          "label": "Glassworking",
          "item_ref_count": 9,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "Lime is one of the ingredients required to make glass batches. It is a powder obtained by heating Flux.",
          "key": "guide-page:pages.2.text",
          "label": "Glassworking",
          "item_ref_count": 9,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "A type of Potash or equivalent is also required for glass batches. Soda Ash can be used, which is a powder made from heating Dried Seaweed or Kelp. Saltpeter can be used as well.",
          "key": "guide-page:pages.3.text",
          "label": "Glassworking",
          "item_ref_count": 9,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "Glassworking is done by starting with a glass batch, and then completing a series of steps. These steps may require specific tools: - A Blowpipe, to Blow and Stretch - A Paddle, to Flatten - Jacks, to Pinch - A Gem Saw, to Saw",
          "key": "guide-page:pages.4.text",
          "label": "Glassworking",
          "item_ref_count": 9,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "Tools of the Trade",
          "key": "guide-page:pages.4.title",
          "label": "Glassworking",
          "item_ref_count": 9,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "The most important tool is the Blowpipe. It can be knapped from clay, and then fired into a Ceramic Blowpipe.",
          "key": "guide-page:pages.5.text",
          "label": "Glassworking",
          "item_ref_count": 9,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "Ceramic blowpipes are brittle, and have a chance to to break when used. A more sturdy blowpipe can be worked from a Brass Rod on an anvil.",
          "key": "guide-page:pages.6.text",
          "label": "Glassworking",
          "item_ref_count": 9,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "The Flatten operation can be done with a Paddle, which is crafted from wood.",
          "key": "guide-page:pages.7.text",
          "label": "Glassworking",
          "item_ref_count": 9,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "The Pinch operation can be done with Jacks, made from welding two brass rods together.",
          "key": "guide-page:pages.8.text",
          "label": "Glassworking",
          "item_ref_count": 9,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "The Saw operation can be done with a Gem Saw. The gem saw is also used to break both Glass Blocks and Glass Panes and obtain them.",
          "key": "guide-page:pages.9.text",
          "label": "Glassworking",
          "item_ref_count": 9,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "First, glass on the blowpipe must be heated to Faint Red. Then, hold the blowpipe in your offhand and hold to perform each step. Use to pick up a hot blowpipe into your offhand. Blow Use the Blowpipe while facing straight ahead. Stretch Use the Blowpipe while facing straight down.",
          "key": "guide-page:pages.10.text",
          "label": "Glassworking",
          "item_ref_count": 9,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "How to Glass",
          "key": "guide-page:pages.10.title",
          "label": "Glassworking",
          "item_ref_count": 9,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "Flatten Use the Blowpipe while holding a Paddle in your main hand. Pinch Use the Blowpipe while holding Jacks in your main hand. Saw Use the Blowpipe while holding a Gem Saw in your main hand. Roll Use the Blowpipe with a Wool Cloth in your main hand.",
          "key": "guide-page:pages.11.text",
          "label": "Glassworking",
          "item_ref_count": 9,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "Glassworking is the process of turning sand into glass. To start, you must create a Glass Batch, of which there are four types: 1. Silica, from white sand. 2. Hematitic, from yellow, red, or pink sand. 3. Olivine, from green or brown sand. 4. Volcanic, from black sand.",
          "key": "guide-page:pages.0.text",
          "label": "Glassworking",
          "item_ref_count": 8,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "Glass batches can then be crafted using one of the aforementioned colors of sand, plus Lime and a type of Potash.",
          "key": "guide-page:pages.1.text",
          "label": "Glassworking",
          "item_ref_count": 8,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "Lime is one of the ingredients required to make glass batches. It is a powder obtained by heating Flux.",
          "key": "guide-page:pages.2.text",
          "label": "Glassworking",
          "item_ref_count": 8,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "A type of Potash or equivalent is also required for glass batches. Soda Ash can be used, which is a powder made from heating Dried Seaweed or Kelp. Saltpeter can be used as well.",
          "key": "guide-page:pages.3.text",
          "label": "Glassworking",
          "item_ref_count": 8,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "Glassworking is done by starting with a glass batch, and then completing a series of steps. These steps may require specific tools: - A Blowpipe, to Blow and Stretch - A Paddle, to Flatten - Jacks, to Pinch - A Gem Saw, to Saw",
          "key": "guide-page:pages.4.text",
          "label": "Glassworking",
          "item_ref_count": 8,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "Tools of the Trade",
          "key": "guide-page:pages.4.title",
          "label": "Glassworking",
          "item_ref_count": 8,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "The most important tool is the Blowpipe. It can be knapped from clay, and then fired into a Ceramic Blowpipe.",
          "key": "guide-page:pages.5.text",
          "label": "Glassworking",
          "item_ref_count": 8,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "Ceramic blowpipes are brittle, and have a chance to to break when used. A more sturdy blowpipe can be worked from a Brass Rod on an anvil.",
          "key": "guide-page:pages.6.text",
          "label": "Glassworking",
          "item_ref_count": 8,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "The Flatten operation can be done with a Paddle, which is crafted from wood.",
          "key": "guide-page:pages.7.text",
          "label": "Glassworking",
          "item_ref_count": 8,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "The Pinch operation can be done with Jacks, made from welding two brass rods together.",
          "key": "guide-page:pages.8.text",
          "label": "Glassworking",
          "item_ref_count": 8,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "The Saw operation can be done with a Gem Saw. The gem saw is also used to break both Glass Blocks and Glass Panes and obtain them.",
          "key": "guide-page:pages.9.text",
          "label": "Glassworking",
          "item_ref_count": 8,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "First, glass on the blowpipe must be heated to Faint Red.Then, hold the blowpipe and hold to perform each step. Blow Use the Blowpipe while facing straight ahead. Stretch Use the Blowpipe while facing straight down.",
          "key": "guide-page:pages.10.text",
          "label": "Glassworking",
          "item_ref_count": 8,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "How to Glass",
          "key": "guide-page:pages.10.title",
          "label": "Glassworking",
          "item_ref_count": 8,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/glassworking.json",
          "text": "Flatten Use the Blowpipe while holding a Paddle in your offhand. Pinch Use the Blowpipe while holding Jacks in your offhand. Saw Use the Blowpipe while holding a Gem Saw in your offhand. Roll Use the Blowpipe with a Wool Cloth in your offhand.",
          "key": "guide-page:pages.11.text",
          "label": "Glassworking",
          "item_ref_count": 8,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "kubejs_tooltip",
          "id": "kubejs:tooltips/76",
          "source": "file:minecraft/kubejs/client_scripts/tooltips.js",
          "text": "Fluid Capacity: %d mB",
          "key": "kubejs-tooltip:gtceu.universal.tooltip.fluid_storage_capacity",
          "label": "Fluid Capacity: %d mB",
          "item_ref_count": 2
        },
        {
          "kind": "runtime_item",
          "id": "tfc:silica_glass_bottle",
          "source": "runtime-items",
          "text": "Fluid Capacity: 500 mB",
          "key": "runtime-tooltip",
          "label": "Glass Bottle",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:silica_glass_bottle",
          "source": "runtime-items",
          "text": "Silica Glass",
          "key": "runtime-tooltip",
          "label": "Glass Bottle",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:volcanic_glass_bottle",
          "source": "runtime-items",
          "text": "Fluid Capacity: 400 mB",
          "key": "runtime-tooltip",
          "label": "Glass Bottle",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:volcanic_glass_bottle",
          "source": "runtime-items",
          "text": "Volcanic Glass",
          "key": "runtime-tooltip",
          "label": "Glass Bottle",
          "item_ref_count": 1
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/wooden_buckets",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/wooden_buckets.json",
          "text": "Wooden Buckets",
          "key": "guide-page:name",
          "label": "Wooden Buckets",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/wooden_buckets",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/wooden_buckets.json",
          "text": "Wooden Buckets are an early game fluid container. They can contain 1000 mB of fluid. They can pick up any kind of fluid that is used for recipes, such as those in a Pot or Barrel. However, wooden buckets cannot place source blocks. Dumping its fluid on the ground results in a small amount of fluid that quickly disappears.",
          "key": "guide-page:pages.0.text",
          "label": "Wooden Buckets",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/wooden_buckets",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/wooden_buckets.json",
          "text": "The wooden bucket is made of Lumber.",
          "key": "guide-page:pages.1.text",
          "label": "Wooden Buckets",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "kubejs_tooltip",
          "id": "kubejs:tooltips/75",
          "source": "file:minecraft/kubejs/client_scripts/tooltips.js",
          "text": "Fluid Capacity: %d mB",
          "key": "kubejs-tooltip:gtceu.universal.tooltip.fluid_storage_capacity",
          "label": "Fluid Capacity: %d mB",
          "item_ref_count": 2
        },
        {
          "kind": "runtime_item",
          "id": "tfc:wooden_bucket",
          "source": "runtime-items",
          "text": "Fluid Capacity: 1,000 mB",
          "key": "runtime-tooltip",
          "label": "Wooden Bucket",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfg:beaker",
          "source": "runtime-items",
          "text": "Fluid Capacity: 1296 mB",
          "key": "runtime-tooltip",
          "label": "Beaker",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfg:flask",
          "source": "runtime-items",
          "text": "Fluid Capacity: 144 mB",
          "key": "runtime-tooltip",
          "label": "Flask",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfg:vial",
          "source": "runtime-items",
          "text": "Fluid Capacity: 16 mB",
          "key": "runtime-tooltip",
          "label": "Pipette",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfg:empty_dna_syringe",
          "source": "runtime-items",
          "text": "An empty syringe.",
          "key": "runtime-tooltip",
          "label": "Empty Syringe",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfg:empty_dna_syringe",
          "source": "runtime-items",
          "text": "[Hold-Shift]",
          "key": "runtime-tooltip",
          "label": "Empty Syringe",
          "item_ref_count": 1
        }
      ],
      "aliases": []
    },
    {
      "id": "slot:preserve",
      "label": "Preserve",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 1077,
      "evidence": [
        {
          "kind": "item_tag",
          "id": "tfc:foods/preserves",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "tfc:foods/preserves_2",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "tfc:foods/sealed_preserves",
          "confidence": 0.75
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/fig_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Fig Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/fig_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Fig Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/fig_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Fig Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/pineapple_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Pineapple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/pineapple_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Pineapple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/pineapple_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Pineapple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/red_grapes_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Red Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/red_grapes_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Red Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/red_grapes_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Red Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/white_grapes_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "White Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/white_grapes_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "White Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/white_grapes_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "White Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/banana_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Banana Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/banana_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Banana Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/banana_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Banana Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blackberry_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Blackberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blackberry_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Blackberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blackberry_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Blackberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blueberry_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Blueberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blueberry_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Blueberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blueberry_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Blueberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/bunchberry_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Bunchberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/bunchberry_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Bunchberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/bunchberry_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Bunchberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cherry_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Cherry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cherry_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Cherry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cherry_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Cherry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cloudberry_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Cloudberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cloudberry_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Cloudberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cloudberry_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Cloudberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cranberry_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Cranberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cranberry_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Cranberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cranberry_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Cranberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/elderberry_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Elderberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/elderberry_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Elderberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/elderberry_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Elderberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/gooseberry_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Gooseberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/gooseberry_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Gooseberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/gooseberry_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Gooseberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/green_apple_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Green Apple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/green_apple_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Green Apple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/green_apple_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Green Apple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/lemon_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Lemon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/lemon_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Lemon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/lemon_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Lemon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/melon_slice_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Melon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/melon_slice_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Melon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/melon_slice_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Melon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/olive_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Olive Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/olive_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Olive Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/olive_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Olive Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/orange_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Orange Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/orange_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Orange Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/orange_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Orange Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/peach_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Peach Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/peach_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Peach Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/peach_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Peach Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/plum_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Plum Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/plum_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Plum Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/plum_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Plum Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/pumpkin_chunks_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Pumpkin Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/pumpkin_chunks_unsealed",
          "source": "runtime-items",
          "text": "Unsealed",
          "key": "runtime-tooltip",
          "label": "Pumpkin Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/pumpkin_chunks_unsealed",
          "source": "runtime-items",
          "text": "Expires on: 21:35 June 5, 1000 (in 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Pumpkin Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/raspberry_unsealed",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Raspberry Jam",
          "item_ref_count": 1
        }
      ],
      "aliases": []
    },
    {
      "id": "slot:repair",
      "label": "Repair",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 1072,
      "evidence": [
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/bismuth_bronze",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/black_bronze",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/black_steel",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/blue_steel",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/boron_carbide",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/bronze",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/copper",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/diamond_tipped_mo_50_re",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/duranium",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/hsse",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/naquadah_alloy",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/ostrum_iodide",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/red_steel",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/steel",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/tungsten_carbide",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/ultimet",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/vanadium_steel",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:repair_kit_materials/wrought_iron",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/bismuth_bronze",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/black_bronze",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/black_steel",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/blue_steel",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/boron_carbide",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/bronze",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/copper",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/diamond_tipped_mo_50_re",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/duranium",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/hsse",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/naquadah_alloy",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/ostrum_iodide",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/red_steel",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/steel",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/tungsten_carbide",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/ultimet",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/vanadium_steel",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:unfired_repair_kit_materials/wrought_iron",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "railways:paint_brush_repair_items",
          "confidence": 0.75
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_bismuth_bronze",
          "source": "runtime-items",
          "text": "BiZnCu₃",
          "key": "runtime-tooltip",
          "label": "Bismuth Bronze Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_bismuth_bronze",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Bismuth Bronze Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_black_bronze",
          "source": "runtime-items",
          "text": "AuAgCu₃",
          "key": "runtime-tooltip",
          "label": "Black Bronze Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_black_bronze",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Black Bronze Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_black_steel",
          "source": "runtime-items",
          "text": "Ni(AuAgCu₃)Fe₃",
          "key": "runtime-tooltip",
          "label": "Black Steel Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_black_steel",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Black Steel Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_blue_steel",
          "source": "runtime-items",
          "text": "(CuAg₄)(BiZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄",
          "key": "runtime-tooltip",
          "label": "Blue Steel Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_blue_steel",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Blue Steel Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_bronze",
          "source": "runtime-items",
          "text": "SnCu₃",
          "key": "runtime-tooltip",
          "label": "Bronze Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_bronze",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Bronze Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_copper",
          "source": "runtime-items",
          "text": "Cu",
          "key": "runtime-tooltip",
          "label": "Copper Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_copper",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Copper Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_duranium",
          "source": "runtime-items",
          "text": "Dr",
          "key": "runtime-tooltip",
          "label": "Duranium Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_duranium",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Duranium Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_hsse",
          "source": "runtime-items",
          "text": "((FeW)₅CrMo₂V)₆CoMnSi",
          "key": "runtime-tooltip",
          "label": "HSS-E Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_hsse",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "HSS-E Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_naquadah_alloy",
          "source": "runtime-items",
          "text": "Nq₂(Ir₃Os)Ke",
          "key": "runtime-tooltip",
          "label": "Naquadah Alloy Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_naquadah_alloy",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Naquadah Alloy Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_ostrum_iodide",
          "source": "runtime-items",
          "text": "(((UO₂)₃ThPb)₂(Ca₃(PO₄)₂)(Al₂O₃)AgBe)₂I",
          "key": "runtime-tooltip",
          "label": "Ostrum Iodide Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_ostrum_iodide",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Ostrum Iodide Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_red_steel",
          "source": "runtime-items",
          "text": "(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄",
          "key": "runtime-tooltip",
          "label": "Red Steel Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_red_steel",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Red Steel Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_steel",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Steel Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_tungsten_carbide",
          "source": "runtime-items",
          "text": "WC",
          "key": "runtime-tooltip",
          "label": "Tungsten Carbide Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_tungsten_carbide",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Tungsten Carbide Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_ultimet",
          "source": "runtime-items",
          "text": "Co₅Cr₂NiMo",
          "key": "runtime-tooltip",
          "label": "Ultimet Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_ultimet",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Ultimet Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_vanadium_steel",
          "source": "runtime-items",
          "text": "VCrFe₇",
          "key": "runtime-tooltip",
          "label": "Vanadium Steel Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_vanadium_steel",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Vanadium Steel Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:repair_kit_wrought_iron",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Wrought Iron Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfg:repair_kit_boron_carbide",
          "source": "runtime-items",
          "text": "B₄C",
          "key": "runtime-tooltip",
          "label": "Boron Carbide Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfg:repair_kit_boron_carbide",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Boron Carbide Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfg:repair_kit_diamond_tipped_mo_50_re",
          "source": "runtime-items",
          "text": "(MoRe)C",
          "key": "runtime-tooltip",
          "label": "Diamond Tipped Molybdenum-50 Rhenium Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfg:repair_kit_diamond_tipped_mo_50_re",
          "source": "runtime-items",
          "text": "Repairs 25% of a tool's maximum durability",
          "key": "runtime-tooltip",
          "label": "Diamond Tipped Molybdenum-50 Rhenium Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:unfired_repair_kit_bismuth_bronze",
          "source": "runtime-items",
          "text": "BiZnCu₃",
          "key": "runtime-tooltip",
          "label": "Unfired Bismuth Bronze Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:unfired_repair_kit_black_bronze",
          "source": "runtime-items",
          "text": "AuAgCu₃",
          "key": "runtime-tooltip",
          "label": "Unfired Black Bronze Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:unfired_repair_kit_black_steel",
          "source": "runtime-items",
          "text": "Ni(AuAgCu₃)Fe₃",
          "key": "runtime-tooltip",
          "label": "Unfired Black Steel Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:unfired_repair_kit_blue_steel",
          "source": "runtime-items",
          "text": "(CuAg₄)(BiZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄",
          "key": "runtime-tooltip",
          "label": "Unfired Blue Steel Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:unfired_repair_kit_bronze",
          "source": "runtime-items",
          "text": "SnCu₃",
          "key": "runtime-tooltip",
          "label": "Unfired Bronze Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:unfired_repair_kit_copper",
          "source": "runtime-items",
          "text": "Cu",
          "key": "runtime-tooltip",
          "label": "Unfired Copper Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:unfired_repair_kit_duranium",
          "source": "runtime-items",
          "text": "Dr",
          "key": "runtime-tooltip",
          "label": "Unfired Duranium Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:unfired_repair_kit_hsse",
          "source": "runtime-items",
          "text": "((FeW)₅CrMo₂V)₆CoMnSi",
          "key": "runtime-tooltip",
          "label": "Unfired HSS-E Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:unfired_repair_kit_naquadah_alloy",
          "source": "runtime-items",
          "text": "Nq₂(Ir₃Os)Ke",
          "key": "runtime-tooltip",
          "label": "Unfired Naquadah Alloy Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:unfired_repair_kit_ostrum_iodide",
          "source": "runtime-items",
          "text": "(((UO₂)₃ThPb)₂(Ca₃(PO₄)₂)(Al₂O₃)AgBe)₂I",
          "key": "runtime-tooltip",
          "label": "Unfired Ostrum Iodide Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:unfired_repair_kit_red_steel",
          "source": "runtime-items",
          "text": "(CuAu₄)(ZnCu₃)Fe₂(Ni(AuAgCu₃)Fe₃)₄",
          "key": "runtime-tooltip",
          "label": "Unfired Red Steel Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:unfired_repair_kit_tungsten_carbide",
          "source": "runtime-items",
          "text": "WC",
          "key": "runtime-tooltip",
          "label": "Unfired Tungsten Carbide Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:unfired_repair_kit_ultimet",
          "source": "runtime-items",
          "text": "Co₅Cr₂NiMo",
          "key": "runtime-tooltip",
          "label": "Unfired Ultimet Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:unfired_repair_kit_vanadium_steel",
          "source": "runtime-items",
          "text": "VCrFe₇",
          "key": "runtime-tooltip",
          "label": "Unfired Vanadium Steel Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfg:unfired_repair_kit_boron_carbide",
          "source": "runtime-items",
          "text": "B₄C",
          "key": "runtime-tooltip",
          "label": "Unfired Boron Carbide Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfg:unfired_repair_kit_diamond_tipped_mo_50_re",
          "source": "runtime-items",
          "text": "(MoRe)C",
          "key": "runtime-tooltip",
          "label": "Unfired Diamond Tipped Molybdenum-50 Rhenium Repair Kit",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "species:species/v1/shear_wraptor_completely",
          "source": "jar:species-3.5.jar!data/species/advancements/species/v1/shear_wraptor_completely.json",
          "text": "Clever Girl",
          "key": "advancement-title:title",
          "label": "Clever Girl",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "species:species/v1/shear_wraptor_completely",
          "source": "jar:species-3.5.jar!data/species/advancements/species/v1/shear_wraptor_completely.json",
          "text": "Shear a Wraptor completely and face the consequences",
          "key": "advancement-description:description",
          "label": "Clever Girl",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "species:species/v2/tickle_goober",
          "source": "jar:species-3.5.jar!data/species/advancements/species/v2/tickle_goober.json",
          "text": "Allergy Season",
          "key": "advancement-title:title",
          "label": "Allergy Season",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "species:species/v2/tickle_goober",
          "source": "jar:species-3.5.jar!data/species/advancements/species/v2/tickle_goober.json",
          "text": "Make a Goober sneeze Goober Goo and prehistorify the world around you",
          "key": "advancement-description:description",
          "label": "Allergy Season",
          "item_ref_count": 1
        }
      ],
      "aliases": []
    },
    {
      "id": "slot:harvest",
      "label": "Harvest",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 1058,
      "evidence": [
        {
          "kind": "block_tag",
          "id": "quark:simple_harvest_blacklisted",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "tfg:silk_harvest_ice",
          "confidence": 0.75
        }
      ],
      "semantic_evidence": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Extraterrestrial Crops",
          "key": "guide-page:name",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Extraterrestrial Crops",
          "key": "guide-page:pages.0.title",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "It turns out Earth isn't the only celestial body with life. During your travels, you may come across other kinds of edible flora. These all use the same mechanics you're used to on Earth (with some exceptions), and can be grown either in normal Farmland, in a Firmalife Greenhouse, or in a GregTech Electric Greenhouse depending on your needs.",
          "key": "guide-page:pages.0.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "An Air Distributor will also keep an enclosed area a stable 15 °C. Due to technical reasons, this temperature can't be displayed in the Jade tooltip while on other planets, so you'll have to use your inventory's Climate tab to check the temperature. Another alternative is to use Firmalife's Greenhouse, which doesn't provide as much output, but also ignores all climate conditions.",
          "key": "guide-page:pages.1.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "- Amber Root - Blossom Berry - Bolux Mushroom - Bulbkin - Chalmie Mushroom - Chorus Fruit - Nox Berry",
          "key": "guide-page:pages.2.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Chorus Fruit",
          "key": "guide-page:pages.4.title",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Location: The Moon Biomes: Chorus Forest, Chorus Thicket Chorus Plants grow in tall formations. Breaking the stem will yield nothing, while breaking the Flowers at the top will yield Chorus Fruits or a Flower. Chorus Fruits can be eaten for Fruit, cooked and eaten as Popped Chorus Fruit, or can be distilled into Nitrogen.",
          "key": "guide-page:pages.4.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Chorus Flowers can also be grown manually by planting them on any Gravel or Sand.",
          "key": "guide-page:pages.5.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Amber Root",
          "key": "guide-page:pages.6.title",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Location: Mars Temperature: -150 - 15 °C Hydration: 0 - 40 % Nutrient: Phosphorus Amber root is a single block crop. Amber seeds can be planted on farmland and will produce Amber Shoots which can be eaten for Grain.",
          "key": "guide-page:pages.6.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Wild Amber Root grows in any climate on Amber and Rusticus Mycelium.",
          "key": "guide-page:pages.7.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Location: Mars Temperature: -80 - 30 °C Hydration: 70 - 100 % Nutrient: Potassium Blossom Berry is a single block crop. Blossom Berry Seeds can be planted on farmland and will produce Blossom Berries which can be eaten for Fruit.",
          "key": "guide-page:pages.8.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Blossom Berry",
          "key": "guide-page:pages.8.title",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Wild Blossom Berry grows in any climate on Amber, Rusticus, or Sangnum Mycelium.",
          "key": "guide-page:pages.9.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Location: Mars Temperature: -150 - 15 °C Hydration: 0 - 40 % Nutrient: Phosphorus Bolux Mushrooms are a single block crop. Bolux Mushroom Spores can be planted on farmland and will produce Bolux Mushrooms which can be eaten for Vegetables. It is recommended to cook Bolux Mushrooms before eating.",
          "key": "guide-page:pages.10.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Bolux Mushroom",
          "key": "guide-page:pages.10.title",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Wild Bolux Mushrooms grow in any climate on Amber, Rusticus, or Sangnum Mycelium.",
          "key": "guide-page:pages.11.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Location: Mars Temperature: -100 - 30 °C Hydration: 0 - 60 % Nutrient: Nitrogen Bulbkins are a spreading crop. Bulbkin Seeds can be planted on farmland and will place up to two Bulbkin Blocks on the ground next to it while it is mature. If the bulbkin blocks are harvested, and the plant matures again, it can grow more bulbkins. Bulbkins can be eaten for Fruit or processed into Glowstone Dust.",
          "key": "guide-page:pages.12.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Wild Bulbkins grow in any climate on Amber or Sangnum Mycelium.",
          "key": "guide-page:pages.13.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Chalmie Mushroom",
          "key": "guide-page:pages.14.title",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Location: Mars Temperature: -150 - 15 °C Hydration: 0 - 40 % Nutrient: Phosphorus Chalmie Mushrooms are a single block crop. Chalmie mushroom spores can be planted on farmland and will produce Chalmie Mushrooms which can be eaten for Vegetables. It is recommended to cook Chalmie Mushrooms before eating.",
          "key": "guide-page:pages.14.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Wild Chalmie Mushrooms grow in any climate on Amber, Rusticus, and Sangnum Mycelium.",
          "key": "guide-page:pages.15.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Nox Berry",
          "key": "guide-page:pages.16.title",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Location: Mars Temperature: -80 - 30 °C Hydration: 50 - 100 % Nutrient: Potassium Nox Berries are a single block crop. Nox berry seeds can be planted on farmland and will produce Nox Berries which can be eaten for Fruit. It is recommended to cook Nox Berries before eating.",
          "key": "guide-page:pages.16.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/tfg_tips/space_crops",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/tfg_tips/space_crops.json",
          "text": "Wild Nox Berries grow in any climate on Rusticus and Sangnum Mycelium.",
          "key": "guide-page:pages.17.text",
          "label": "Extraterrestrial Crops",
          "item_ref_count": 8,
          "count": 18
        },
        {
          "kind": "runtime_item",
          "id": "betterend:cave_pumpkin",
          "source": "runtime-items",
          "text": "0.5 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Bulbkin",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "betterend:cave_pumpkin",
          "source": "runtime-items",
          "text": "Expires on: 11:59 November 5, 1000 (in 5 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Bulbkin",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "betterend:cave_pumpkin",
          "source": "runtime-items",
          "text": "Betterend",
          "key": "runtime-tooltip",
          "label": "Bulbkin",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:blue_steel_saw",
          "source": "runtime-items",
          "text": "2,010 Crafting Uses",
          "key": "runtime-tooltip",
          "label": "Blue Steel Saw",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:blue_steel_saw",
          "source": "runtime-items",
          "text": "4,019 Total Durability",
          "key": "runtime-tooltip",
          "label": "Blue Steel Saw",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:blue_steel_saw",
          "source": "runtime-items",
          "text": "4,020 Durability",
          "key": "runtime-tooltip",
          "label": "Blue Steel Saw",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:blue_steel_saw",
          "source": "runtime-items",
          "text": "Sculptor: Silk harvests Packed Ice",
          "key": "runtime-tooltip",
          "label": "Blue Steel Saw",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:blue_steel_saw",
          "source": "runtime-items",
          "text": "Boater: Can Create Canoes",
          "key": "runtime-tooltip",
          "label": "Blue Steel Saw",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:blue_steel_saw",
          "source": "runtime-items",
          "text": "Usable as: Saw",
          "key": "runtime-tooltip",
          "label": "Blue Steel Saw",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:blue_steel_saw",
          "source": "runtime-items",
          "text": "Craft with a Repair Kit to repair 25% durability",
          "key": "runtime-tooltip",
          "label": "Blue Steel Saw",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:blue_steel_saw",
          "source": "runtime-items",
          "text": "Deals Slashing Damage",
          "key": "runtime-tooltip",
          "label": "Blue Steel Saw",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:blue_steel_saw",
          "source": "runtime-items",
          "text": "Melts into 144 mB of Blue Steel (at Brilliant White)",
          "key": "runtime-tooltip",
          "label": "Blue Steel Saw",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_ostrum_iodide_chainsaw",
          "source": "runtime-items",
          "text": "-1/-1 EU - Tier EV",
          "key": "runtime-tooltip",
          "label": "Ostrum Iodide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_ostrum_iodide_chainsaw",
          "source": "runtime-items",
          "text": "15,479 Total Durability",
          "key": "runtime-tooltip",
          "label": "Ostrum Iodide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_ostrum_iodide_chainsaw",
          "source": "runtime-items",
          "text": "15,480 Durability",
          "key": "runtime-tooltip",
          "label": "Ostrum Iodide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_ostrum_iodide_chainsaw",
          "source": "runtime-items",
          "text": "104 Mining Speed",
          "key": "runtime-tooltip",
          "label": "Ostrum Iodide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_ostrum_iodide_chainsaw",
          "source": "runtime-items",
          "text": "Harvest Level 4 (Netherite)",
          "key": "runtime-tooltip",
          "label": "Ostrum Iodide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_ostrum_iodide_chainsaw",
          "source": "runtime-items",
          "text": "Sculptor: Silk harvests Packed Ice",
          "key": "runtime-tooltip",
          "label": "Ostrum Iodide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_ostrum_iodide_chainsaw",
          "source": "runtime-items",
          "text": "Brute: Disables Shields",
          "key": "runtime-tooltip",
          "label": "Ostrum Iodide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_ostrum_iodide_chainsaw",
          "source": "runtime-items",
          "text": "Lumberjack: Tree Felling",
          "key": "runtime-tooltip",
          "label": "Ostrum Iodide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_ostrum_iodide_chainsaw",
          "source": "runtime-items",
          "text": "Usable as: Axe",
          "key": "runtime-tooltip",
          "label": "Ostrum Iodide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_ostrum_iodide_chainsaw",
          "source": "runtime-items",
          "text": "Craft with a Repair Kit to repair 25% durability",
          "key": "runtime-tooltip",
          "label": "Ostrum Iodide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_ostrum_iodide_chainsaw",
          "source": "runtime-items",
          "text": "Craft with a new Tool Head to replace it",
          "key": "runtime-tooltip",
          "label": "Ostrum Iodide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_ostrum_iodide_chainsaw",
          "source": "runtime-items",
          "text": "Deals Slashing Damage",
          "key": "runtime-tooltip",
          "label": "Ostrum Iodide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_tungsten_carbide_chainsaw",
          "source": "runtime-items",
          "text": "-1/-1 EU - Tier EV",
          "key": "runtime-tooltip",
          "label": "Tungsten Carbide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_tungsten_carbide_chainsaw",
          "source": "runtime-items",
          "text": "5,119 Total Durability",
          "key": "runtime-tooltip",
          "label": "Tungsten Carbide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_tungsten_carbide_chainsaw",
          "source": "runtime-items",
          "text": "5,120 Durability",
          "key": "runtime-tooltip",
          "label": "Tungsten Carbide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_tungsten_carbide_chainsaw",
          "source": "runtime-items",
          "text": "174 Mining Speed",
          "key": "runtime-tooltip",
          "label": "Tungsten Carbide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_tungsten_carbide_chainsaw",
          "source": "runtime-items",
          "text": "Harvest Level 4 (Netherite)",
          "key": "runtime-tooltip",
          "label": "Tungsten Carbide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_tungsten_carbide_chainsaw",
          "source": "runtime-items",
          "text": "Sculptor: Silk harvests Packed Ice",
          "key": "runtime-tooltip",
          "label": "Tungsten Carbide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_tungsten_carbide_chainsaw",
          "source": "runtime-items",
          "text": "Brute: Disables Shields",
          "key": "runtime-tooltip",
          "label": "Tungsten Carbide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_tungsten_carbide_chainsaw",
          "source": "runtime-items",
          "text": "Lumberjack: Tree Felling",
          "key": "runtime-tooltip",
          "label": "Tungsten Carbide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_tungsten_carbide_chainsaw",
          "source": "runtime-items",
          "text": "Usable as: Axe",
          "key": "runtime-tooltip",
          "label": "Tungsten Carbide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_tungsten_carbide_chainsaw",
          "source": "runtime-items",
          "text": "Craft with a Repair Kit to repair 25% durability",
          "key": "runtime-tooltip",
          "label": "Tungsten Carbide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_tungsten_carbide_chainsaw",
          "source": "runtime-items",
          "text": "Craft with a new Tool Head to replace it",
          "key": "runtime-tooltip",
          "label": "Tungsten Carbide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_tungsten_carbide_chainsaw",
          "source": "runtime-items",
          "text": "Deals Slashing Damage",
          "key": "runtime-tooltip",
          "label": "Tungsten Carbide Chainsaw (EV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_ultimet_chainsaw",
          "source": "runtime-items",
          "text": "-1/-1 EU - Tier HV",
          "key": "runtime-tooltip",
          "label": "Ultimet Chainsaw (HV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_ultimet_chainsaw",
          "source": "runtime-items",
          "text": "8,389 Total Durability",
          "key": "runtime-tooltip",
          "label": "Ultimet Chainsaw (HV)",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_ultimet_chainsaw",
          "source": "runtime-items",
          "text": "8,390 Durability",
          "key": "runtime-tooltip",
          "label": "Ultimet Chainsaw (HV)",
          "item_ref_count": 1
        }
      ],
      "aliases": []
    },
    {
      "id": "slot:cast",
      "label": "Cast",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 1009,
      "evidence": [
        {
          "kind": "item_tag",
          "id": "forge:double_ingots/cast_iron",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:ingots/cast_iron",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:plates/cast_iron",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "forge:wires/cast_iron",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "tfc:metal_item/cast_iron",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "vintageimprovements:small_springs/cast_iron",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "vintageimprovements:springs/cast_iron",
          "confidence": 0.75
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "tfc:metal/double_ingot/cast_iron",
          "source": "runtime-items",
          "text": "Melts into 288 mB of Cast Iron (at Brilliant White)",
          "key": "runtime-tooltip",
          "label": "Cast Iron Double Ingot",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "minecraft:iron_ingot",
          "source": "runtime-items",
          "text": "Melts into 144 mB of Cast Iron (at Brilliant White)",
          "key": "runtime-tooltip",
          "label": "Cast Iron Ingot",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "vintageimprovements:cast_iron_sheet",
          "source": "runtime-items",
          "text": "Melts into 144 mB of Cast Iron (at Brilliant White)",
          "key": "runtime-tooltip",
          "label": "Cast Iron Plate",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:metal/block/cast_iron",
          "source": "runtime-items",
          "text": "Melts into 144 mB of Cast Iron (at Brilliant White)",
          "key": "runtime-tooltip",
          "label": "Cast Iron Plated Block",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:metal/block/cast_iron_slab",
          "source": "runtime-items",
          "text": "Melts into 72 mB of Cast Iron (at Brilliant White)",
          "key": "runtime-tooltip",
          "label": "Cast Iron Plated Slab",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:metal/block/cast_iron_stairs",
          "source": "runtime-items",
          "text": "Melts into 144 mB of Cast Iron (at Brilliant White)",
          "key": "runtime-tooltip",
          "label": "Cast Iron Plated Stairs",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "vintageimprovements:small_cast_iron_spring",
          "source": "runtime-items",
          "text": "Stiffness: 28",
          "key": "runtime-tooltip",
          "label": "Small Cast Iron Spring",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "vintageimprovements:cast_iron_spring",
          "source": "runtime-items",
          "text": "Stiffness: 280",
          "key": "runtime-tooltip",
          "label": "Cast Iron Spring",
          "item_ref_count": 1
        }
      ],
      "aliases": []
    },
    {
      "id": "slot:drink",
      "label": "Drink",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 1002,
      "evidence": [
        {
          "kind": "item_tag",
          "id": "create:deployable_drink",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "railways:paint_drink_blockers",
          "confidence": 0.75
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "minecraft:milk_bucket",
          "source": "runtime-items",
          "text": "Temperature: 300 K",
          "key": "runtime-tooltip",
          "label": "Cow Milk Bucket",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "species:species/v3/aggro_ghoul",
          "source": "jar:species-3.5.jar!data/species/advancements/species/v3/aggro_ghoul.json",
          "text": "Red Light",
          "key": "advancement-title:title",
          "label": "Red Light",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "species:species/v3/aggro_ghoul",
          "source": "jar:species-3.5.jar!data/species/advancements/species/v3/aggro_ghoul.json",
          "text": "Move at the wrong time and get a Ghoul to attack you",
          "key": "advancement-description:description",
          "label": "Red Light",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "minecraft:potion",
          "source": "runtime-items",
          "text": "No Effects",
          "key": "runtime-tooltip",
          "label": "Water Bottle",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create:potato_cannon",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/potato_cannon.json",
          "text": "Fwoomp!",
          "key": "advancement-title:title",
          "label": "Fwoomp!",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create:potato_cannon",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/potato_cannon.json",
          "text": "Defeat an enemy with your Potato Cannon",
          "key": "advancement-description:description",
          "label": "Fwoomp!",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:potato_cannon",
          "source": "runtime-items",
          "text": "_Shoots_ a suitable item from your _Inventory_.",
          "key": "lang:item.create.potato_cannon.tooltip.behaviour1",
          "label": "Potato Cannon",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:potato_cannon",
          "source": "runtime-items",
          "text": "_No_ _Durability_ will be used. Instead, _Air_ _pressure_ is drained from the Tank",
          "key": "lang:item.create.potato_cannon.tooltip.behaviour2",
          "label": "Potato Cannon",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:potato_cannon",
          "source": "runtime-items",
          "text": "When R-Clicked",
          "key": "lang:item.create.potato_cannon.tooltip.condition1",
          "label": "Potato Cannon",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:potato_cannon",
          "source": "runtime-items",
          "text": "While wearing Backtank",
          "key": "lang:item.create.potato_cannon.tooltip.condition2",
          "label": "Potato Cannon",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:potato_cannon",
          "source": "runtime-items",
          "text": "Launches your home-grown vegetables at Enemies. Can be powered with _Air_ _Pressure_ from a _Backtank_",
          "key": "lang:item.create.potato_cannon.tooltip.summary",
          "label": "Potato Cannon",
          "item_ref_count": 1
        }
      ],
      "aliases": []
    },
    {
      "id": "slot:configure",
      "label": "Configure",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:equip",
      "label": "Equip",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:open",
      "label": "Open",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:scan",
      "label": "Scan",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    }
  ],
  "required_output_contract": {
    "required_values_count": 15,
    "required_candidate_ids": [
      "slot:fuel",
      "slot:fill",
      "slot:place",
      "slot:launch",
      "slot:eat",
      "slot:empty",
      "slot:preserve",
      "slot:repair",
      "slot:harvest",
      "slot:cast",
      "slot:drink",
      "slot:configure",
      "slot:equip",
      "slot:open",
      "slot:scan"
    ],
    "final_instructions": [
      "Return strict JSON only: one object with a top-level values array.",
      "The values array must contain exactly one object for every id in required_candidate_ids.",
      "Every output id must exactly match one candidate id from required_candidate_ids.",
      "Never omit rejected, low-quality, generic, or uncertain candidates; mark them rejected or review.",
      "Do not add ids that are not in required_candidate_ids.",
      "Before responding, count values.length and verify it equals required_values_count."
    ]
  }
}