{
  "pack_id": "tfg",
  "facet": "workflow_role",
  "policy": "Scoped role values only, formatted as <workflow>#<role>. Parent must be an accepted workflow candidate.",
  "min_evidence": 2,
  "previous_accepted": [],
  "prompt_budget": {
    "max_chars": 3200000,
    "semantic_evidence_per_candidate": 64,
    "evidence_refs_per_candidate": 64
  },
  "candidates": [
    {
      "id": "createaddition:rolling#input",
      "label": "Rolling Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 210,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "createaddition:rolling",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "createaddition:rolling"
    },
    {
      "id": "createaddition:rolling#output",
      "label": "Rolling Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 210,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "createaddition:rolling",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "createaddition:rolling"
    },
    {
      "id": "tfc:casting#input",
      "label": "Casting Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 194,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:casting",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "tfc:casting"
    },
    {
      "id": "tfc:casting#output",
      "label": "Casting Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 194,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:casting",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "tfc:casting"
    },
    {
      "id": "greate:brewing#input",
      "label": "Brewing Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 154,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "greate:brewing",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "greate:brewing"
    },
    {
      "id": "greate:brewing#output",
      "label": "Brewing Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 154,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "greate:brewing",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "greate:brewing"
    },
    {
      "id": "tfc:pot#input",
      "label": "Pot Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 150,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:pot",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "tfc:pot"
    },
    {
      "id": "tfc:pot#output",
      "label": "Pot Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 150,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:pot",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "tfc:pot"
    },
    {
      "id": "tfc:welding#input",
      "label": "Welding Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 139,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:welding",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [
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
        }
      ],
      "aliases": [],
      "parent": "tfc:welding"
    },
    {
      "id": "tfc:welding#output",
      "label": "Welding Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 139,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:welding",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [
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
        }
      ],
      "aliases": [],
      "parent": "tfc:welding"
    },
    {
      "id": "vintageimprovements:coiling#input",
      "label": "Coiling Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 131,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "vintageimprovements:coiling",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "vintageimprovements:coiling"
    },
    {
      "id": "vintageimprovements:coiling#output",
      "label": "Coiling Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 131,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "vintageimprovements:coiling",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "vintageimprovements:coiling"
    },
    {
      "id": "tfc:knapping#input",
      "label": "Knapping Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 119,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:knapping",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "tfc:knapping"
    },
    {
      "id": "tfc:knapping#output",
      "label": "Knapping Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 119,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:knapping",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "tfc:knapping"
    },
    {
      "id": "vintageimprovements:vibrating#input",
      "label": "Vibrating Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 112,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "vintageimprovements:vibrating",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "vintageimprovements:vibrating"
    },
    {
      "id": "vintageimprovements:vibrating#output",
      "label": "Vibrating Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 112,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "vintageimprovements:vibrating",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "vintageimprovements:vibrating"
    },
    {
      "id": "tfc:glassworking#input",
      "label": "Glassworking Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 90,
      "evidence": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "confidence": 0.7
        },
        {
          "kind": "recipe_type",
          "id": "tfc:glassworking",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [
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
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/better_fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/better_fuel.json",
          "text": "Better Fuel",
          "key": "guide-page:name",
          "label": "Better Fuel",
          "item_ref_count": 5,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/better_fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/better_fuel.json",
          "text": "Cryo Freezer",
          "key": "guide-page:pages.0.title",
          "label": "Better Fuel",
          "item_ref_count": 5,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/better_fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/better_fuel.json",
          "text": "Fuel is hard to get; it requires venturing out to the ocean, looking for oil spouts. The Cryo Freezer introduces a new way to obtain a better fuel - Cryo Fuel. Cryo Fuel is much more efficient than refined fuel, allowing you to launch a rocket with just one bucket.",
          "key": "guide-page:pages.0.text",
          "label": "Better Fuel",
          "item_ref_count": 5,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/better_fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/better_fuel.json",
          "text": "It's created by collecting ice shards, which are found as an ore underground on cold planets. The ice shard then needs to be inserted into a Cryo Freezer, and it will convert to 25 mB of Cryo Fuel. Alternatively, you can convert ice, packed ice and blue ice to fuel. Once you've made at least two buckets (one for a launch and the other for a return trip), place the buckets in the rocket fuel slot.",
          "key": "guide-page:pages.1.text",
          "label": "Better Fuel",
          "item_ref_count": 5,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/better_fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/better_fuel.json",
          "text": "Cryo Fuel",
          "key": "guide-page:pages.3.title",
          "label": "Better Fuel",
          "item_ref_count": 5,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/better_fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/better_fuel.json",
          "text": "In addition to being an excellent fuel, cryo fuel has some other features. When placed, it will damage anything inside, similar to lava. It also freezes water, allowing for an infinite ice source.",
          "key": "guide-page:pages.3.text",
          "label": "Better Fuel",
          "item_ref_count": 5,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/detection",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/detection.json",
          "text": "Oxygen Sensor",
          "key": "guide-page:pages.0.title",
          "label": "Detection",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/detection",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/detection.json",
          "text": "The oxygen sensor is a block that will emit a redstone signal when it detects oxygen. You can shift-right-click it to invert the signal. Despite its name, you can also change its detection type by right-clicking with a wrench. It has the option to detect oxygen, safe temperature, and normal gravity.",
          "key": "guide-page:pages.0.text",
          "label": "Detection",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/energizer",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/energizer.json",
          "text": "The energizer is a machine capable of powering items and storing large amounts of energy. To charge an item, power the energizer and then right-click on the energizer with that item. It stores 2 million energy, making it capable of charging machines for a very long time. It also retains its energy when broken.",
          "key": "guide-page:pages.0.text",
          "label": "Energizer",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/flags",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/flags.json",
          "text": "Flags are decorative blocks available in 16 colors. By default, when placed, they will display the face of the flag owner. They can be configured to display a custom image from a URL by right-clicking and entering the URL in the GUI. This will display the internet image onto the flag.",
          "key": "guide-page:pages.0.text",
          "label": "Flags",
          "item_ref_count": 5,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/fluid_pipes",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/fluid_pipes.json",
          "text": "Fluid Pipes",
          "key": "guide-page:name",
          "label": "Fluid Pipes",
          "item_ref_count": 4,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/fluid_pipes",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/fluid_pipes.json",
          "text": "Fluid Pipes",
          "key": "guide-page:pages.0.title",
          "label": "Fluid Pipes",
          "item_ref_count": 4,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/fluid_pipes",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/fluid_pipes.json",
          "text": "Fluid pipes are similar to cables but with fluid transfer instead of energy transfer. To use them, craft some fluid pipes and a wrench. Then, right-click on one of the pipes with the wrench to cycle through the pipe's modes: 'none,' 'normal,' 'insert,' and 'extract.'",
          "key": "guide-page:pages.0.text",
          "label": "Fluid Pipes",
          "item_ref_count": 4,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/fluid_pipes",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/fluid_pipes.json",
          "text": "Pipes with 'insert' set will receive fluid from the network, while pipes with 'extract' set will pump fluid into the network.",
          "key": "guide-page:pages.1.text",
          "label": "Fluid Pipes",
          "item_ref_count": 4,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/fluid_pipes",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/fluid_pipes.json",
          "text": "Like cables, fluid pipes also have a duct block for transfering fluids safely in and out of sealed structures.",
          "key": "guide-page:pages.3.text",
          "label": "Fluid Pipes",
          "item_ref_count": 4,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/industrial_lamps",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/industrial_lamps.json",
          "text": "Industrial Lamps",
          "key": "guide-page:name",
          "label": "Industrial Lamps",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/industrial_lamps",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/industrial_lamps.json",
          "text": "Industrial Lamps",
          "key": "guide-page:pages.0.title",
          "label": "Industrial Lamps",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/industrial_lamps",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/industrial_lamps.json",
          "text": "Industrial lamps are available in all 16 colors, and in small and large sizes. By default, these will all emit normal white light; however, if you have Shimmer, each light will emit its respective color.",
          "key": "guide-page:pages.0.text",
          "label": "Industrial Lamps",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/netherite_space_suit",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/netherite_space_suit.json",
          "text": "Netherite Space Suit",
          "key": "guide-page:name",
          "label": "Netherite Space Suit",
          "item_ref_count": 5,
          "recipe_ref_count": 2,
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/netherite_space_suit",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/netherite_space_suit.json",
          "text": "Netherite Space Suit",
          "key": "guide-page:pages.0.title",
          "label": "Netherite Space Suit",
          "item_ref_count": 5,
          "recipe_ref_count": 2,
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/netherite_space_suit",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/netherite_space_suit.json",
          "text": "The netherite space suit is an upgraded version of the standard space suit, featuring permanent fire resistance, more protection and the ability to survive on Venus and Mercury. Before you travel to these planets, you must craft the netherite space suit armour or else you will burn to death.",
          "key": "guide-page:pages.0.text",
          "label": "Netherite Space Suit",
          "item_ref_count": 5,
          "recipe_ref_count": 2,
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/sliding_doors",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/sliding_doors.json",
          "text": "Sliding Doors",
          "key": "guide-page:name",
          "label": "Sliding Doors",
          "item_ref_count": 6,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/sliding_doors",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/sliding_doors.json",
          "text": "Sliding Doors",
          "key": "guide-page:pages.0.title",
          "label": "Sliding Doors",
          "item_ref_count": 6,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/sliding_doors",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/sliding_doors.json",
          "text": "Sliding doors are large 3x3 doors. They seal oxygen when closed, making them great candidates for airlocks. They can be locked by shift-right-clicking them with a wrench. Locked doors can only be opened with redstone.",
          "key": "guide-page:pages.0.text",
          "label": "Sliding Doors",
          "item_ref_count": 6,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/creating_plates",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/creating_plates.json",
          "text": "Creating Plates",
          "key": "guide-page:name",
          "label": "Creating Plates",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/creating_plates",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/creating_plates.json",
          "text": "Once you've got energy, you'll want to craft a compressor. This machine can compress ingots and blocks into plates, which are needed to craft many things.",
          "key": "guide-page:pages.0.text",
          "label": "Creating Plates",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/fuel.json",
          "text": "Even after you place your rocket, you will not be able to fly just yet. To fly, you will need to fill the rocket with fuel.",
          "key": "guide-page:pages.0.text",
          "label": "Fuel",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/fuel.json",
          "text": "Obtaining Fuel",
          "key": "guide-page:pages.1.title",
          "label": "Fuel",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/fuel.json",
          "text": "Oil is found in large amounts spouting over the ocean surface. Collect as much oil as you can, as oil is non-renewable, and you need a lot of it for each rocket launch.",
          "key": "guide-page:pages.1.text",
          "label": "Fuel",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/fuel.json",
          "text": "Obtaining Fuel",
          "key": "guide-page:pages.2.title",
          "label": "Fuel",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/fuel.json",
          "text": "After collecting enough oil (at least six buckets for a launch and return), you will need to refine the oil into fuel. To do this, you will need to craft a fuel refinery. Power it, then place the oil in the left slot, and it will be refined into fuel.",
          "key": "guide-page:pages.2.text",
          "label": "Fuel",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/fuel.json",
          "text": "Fuel Refinery",
          "key": "guide-page:pages.3.title",
          "label": "Fuel",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/fuel.json",
          "text": "Shift-click on the rocket to open its inventory, then place three buckets of fuel into the input slot. Place the other three buckets into the rocket's inventory for a return trip.",
          "key": "guide-page:pages.3.text",
          "label": "Fuel",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/generating_energy",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/generating_energy.json",
          "text": "Generating Energy",
          "key": "guide-page:name",
          "label": "Generating Energy",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/generating_energy",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/generating_energy.json",
          "text": "Coal Generator",
          "key": "guide-page:pages.0.title",
          "label": "Generating Energy",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/generating_energy",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/generating_energy.json",
          "text": "Energy is core resource of Ad Astra. It's used in all machines and is required for everything. There are multiple ways to generate energy, the most basic way being the coal generator. It uses burnable resources like wood or coal to generate energy.",
          "key": "guide-page:pages.0.text",
          "label": "Generating Energy",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/making_steel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/making_steel.json",
          "text": "Making Steel",
          "key": "guide-page:name",
          "label": "Making Steel",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/making_steel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/making_steel.json",
          "text": "Etrionic Blast Furnace",
          "key": "guide-page:pages.0.title",
          "label": "Making Steel",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/making_steel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/making_steel.json",
          "text": "The Etrionic Blast Furnace is an upgraded version of the blast furnace. It has two modes: blasting and alloying. Blasting works like a blast furnace, but with 4 slots for 4x efficiency. Alloying combines materials to make an alloy.",
          "key": "guide-page:pages.0.text",
          "label": "Making Steel",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/making_steel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/making_steel.json",
          "text": "Steel is made in an etrionic blast furnace by combining iron and coal. This will make 1 steel ingot per iron and coal. Steel is essential for most recipes and is the main material of your first rocket.",
          "key": "guide-page:pages.2.text",
          "label": "Making Steel",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/oxygen",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/oxygen.json",
          "text": "At this point, you can launch your rocket; however, you will likely die very fast, as there is no oxygen on the moon. In order to survive on the moon, you will need an oxygenated space suit and an ample oxygen supply.",
          "key": "guide-page:pages.0.text",
          "label": "Oxygen",
          "item_ref_count": 7,
          "recipe_ref_count": 3,
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/oxygen",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/oxygen.json",
          "text": "To obtain oxygen, it must be extracted from water. An oxygen loader is required to do this. Craft one, then place water in the left tank, and watch it convert into oxygen. You must repeat this process many times until you have enough oxygen.",
          "key": "guide-page:pages.3.text",
          "label": "Oxygen",
          "item_ref_count": 7,
          "recipe_ref_count": 3,
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/oxygen",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/oxygen.json",
          "text": "Oxygen Loader",
          "key": "guide-page:pages.4.title",
          "label": "Oxygen",
          "item_ref_count": 7,
          "recipe_ref_count": 3,
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/oxygen",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/oxygen.json",
          "text": "Oxygen can then be extracted by collecting it in a bucket, spacesuit, or a gas tank. After you have enough oxygen, you are moments away from launching your first rocket!",
          "key": "guide-page:pages.4.text",
          "label": "Oxygen",
          "item_ref_count": 7,
          "recipe_ref_count": 3,
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/portable_resources",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/portable_resources.json",
          "text": "Portable Resources",
          "key": "guide-page:name",
          "label": "Portable Resources",
          "item_ref_count": 4,
          "recipe_ref_count": 2,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/portable_resources",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/portable_resources.json",
          "text": "Gas Tanks",
          "key": "guide-page:pages.0.title",
          "label": "Portable Resources",
          "item_ref_count": 4,
          "recipe_ref_count": 2,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/portable_resources",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/portable_resources.json",
          "text": "Oxygen tanks allow you to store oxygen in a convenient portable form. Despite the name, they can store any fluid. Holding down right-click will distribute the oxygen into items in your inventory, with a bias towards armor.",
          "key": "guide-page:pages.0.text",
          "label": "Portable Resources",
          "item_ref_count": 4,
          "recipe_ref_count": 2,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/portable_resources",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/portable_resources.json",
          "text": "Etrionic Capacitor",
          "key": "guide-page:pages.2.title",
          "label": "Portable Resources",
          "item_ref_count": 4,
          "recipe_ref_count": 2,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/portable_resources",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/portable_resources.json",
          "text": "The Etrionic Capacitor is a battery-like item for storing energy in a portable form. It can be placed in the energy slot of machines to power them, and like the gas tanks, can be distributed into your inventory by holding down right-click. It features two modes: Sequential and Round Robin. Sequential will distribute energy one by one while Round Robin will distribute energy evenly. The mode can be changed by right-clicking while sneaking.",
          "key": "guide-page:pages.2.text",
          "label": "Portable Resources",
          "item_ref_count": 4,
          "recipe_ref_count": 2,
          "count": 4
        }
      ],
      "aliases": [],
      "parent": "tfc:glassworking"
    },
    {
      "id": "tfc:glassworking#output",
      "label": "Glassworking Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 90,
      "evidence": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/glassworking",
          "confidence": 0.7
        },
        {
          "kind": "recipe_type",
          "id": "tfc:glassworking",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [
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
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/better_fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/better_fuel.json",
          "text": "Better Fuel",
          "key": "guide-page:name",
          "label": "Better Fuel",
          "item_ref_count": 5,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/better_fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/better_fuel.json",
          "text": "Cryo Freezer",
          "key": "guide-page:pages.0.title",
          "label": "Better Fuel",
          "item_ref_count": 5,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/better_fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/better_fuel.json",
          "text": "Fuel is hard to get; it requires venturing out to the ocean, looking for oil spouts. The Cryo Freezer introduces a new way to obtain a better fuel - Cryo Fuel. Cryo Fuel is much more efficient than refined fuel, allowing you to launch a rocket with just one bucket.",
          "key": "guide-page:pages.0.text",
          "label": "Better Fuel",
          "item_ref_count": 5,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/better_fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/better_fuel.json",
          "text": "It's created by collecting ice shards, which are found as an ore underground on cold planets. The ice shard then needs to be inserted into a Cryo Freezer, and it will convert to 25 mB of Cryo Fuel. Alternatively, you can convert ice, packed ice and blue ice to fuel. Once you've made at least two buckets (one for a launch and the other for a return trip), place the buckets in the rocket fuel slot.",
          "key": "guide-page:pages.1.text",
          "label": "Better Fuel",
          "item_ref_count": 5,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/better_fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/better_fuel.json",
          "text": "Cryo Fuel",
          "key": "guide-page:pages.3.title",
          "label": "Better Fuel",
          "item_ref_count": 5,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/better_fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/better_fuel.json",
          "text": "In addition to being an excellent fuel, cryo fuel has some other features. When placed, it will damage anything inside, similar to lava. It also freezes water, allowing for an infinite ice source.",
          "key": "guide-page:pages.3.text",
          "label": "Better Fuel",
          "item_ref_count": 5,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/detection",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/detection.json",
          "text": "Oxygen Sensor",
          "key": "guide-page:pages.0.title",
          "label": "Detection",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/detection",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/detection.json",
          "text": "The oxygen sensor is a block that will emit a redstone signal when it detects oxygen. You can shift-right-click it to invert the signal. Despite its name, you can also change its detection type by right-clicking with a wrench. It has the option to detect oxygen, safe temperature, and normal gravity.",
          "key": "guide-page:pages.0.text",
          "label": "Detection",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/energizer",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/energizer.json",
          "text": "The energizer is a machine capable of powering items and storing large amounts of energy. To charge an item, power the energizer and then right-click on the energizer with that item. It stores 2 million energy, making it capable of charging machines for a very long time. It also retains its energy when broken.",
          "key": "guide-page:pages.0.text",
          "label": "Energizer",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/flags",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/flags.json",
          "text": "Flags are decorative blocks available in 16 colors. By default, when placed, they will display the face of the flag owner. They can be configured to display a custom image from a URL by right-clicking and entering the URL in the GUI. This will display the internet image onto the flag.",
          "key": "guide-page:pages.0.text",
          "label": "Flags",
          "item_ref_count": 5,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/fluid_pipes",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/fluid_pipes.json",
          "text": "Fluid Pipes",
          "key": "guide-page:name",
          "label": "Fluid Pipes",
          "item_ref_count": 4,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/fluid_pipes",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/fluid_pipes.json",
          "text": "Fluid Pipes",
          "key": "guide-page:pages.0.title",
          "label": "Fluid Pipes",
          "item_ref_count": 4,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/fluid_pipes",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/fluid_pipes.json",
          "text": "Fluid pipes are similar to cables but with fluid transfer instead of energy transfer. To use them, craft some fluid pipes and a wrench. Then, right-click on one of the pipes with the wrench to cycle through the pipe's modes: 'none,' 'normal,' 'insert,' and 'extract.'",
          "key": "guide-page:pages.0.text",
          "label": "Fluid Pipes",
          "item_ref_count": 4,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/fluid_pipes",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/fluid_pipes.json",
          "text": "Pipes with 'insert' set will receive fluid from the network, while pipes with 'extract' set will pump fluid into the network.",
          "key": "guide-page:pages.1.text",
          "label": "Fluid Pipes",
          "item_ref_count": 4,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/fluid_pipes",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/fluid_pipes.json",
          "text": "Like cables, fluid pipes also have a duct block for transfering fluids safely in and out of sealed structures.",
          "key": "guide-page:pages.3.text",
          "label": "Fluid Pipes",
          "item_ref_count": 4,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/industrial_lamps",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/industrial_lamps.json",
          "text": "Industrial Lamps",
          "key": "guide-page:name",
          "label": "Industrial Lamps",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/industrial_lamps",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/industrial_lamps.json",
          "text": "Industrial Lamps",
          "key": "guide-page:pages.0.title",
          "label": "Industrial Lamps",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/industrial_lamps",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/industrial_lamps.json",
          "text": "Industrial lamps are available in all 16 colors, and in small and large sizes. By default, these will all emit normal white light; however, if you have Shimmer, each light will emit its respective color.",
          "key": "guide-page:pages.0.text",
          "label": "Industrial Lamps",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/netherite_space_suit",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/netherite_space_suit.json",
          "text": "Netherite Space Suit",
          "key": "guide-page:name",
          "label": "Netherite Space Suit",
          "item_ref_count": 5,
          "recipe_ref_count": 2,
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/netherite_space_suit",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/netherite_space_suit.json",
          "text": "Netherite Space Suit",
          "key": "guide-page:pages.0.title",
          "label": "Netherite Space Suit",
          "item_ref_count": 5,
          "recipe_ref_count": 2,
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/netherite_space_suit",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/netherite_space_suit.json",
          "text": "The netherite space suit is an upgraded version of the standard space suit, featuring permanent fire resistance, more protection and the ability to survive on Venus and Mercury. Before you travel to these planets, you must craft the netherite space suit armour or else you will burn to death.",
          "key": "guide-page:pages.0.text",
          "label": "Netherite Space Suit",
          "item_ref_count": 5,
          "recipe_ref_count": 2,
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/sliding_doors",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/sliding_doors.json",
          "text": "Sliding Doors",
          "key": "guide-page:name",
          "label": "Sliding Doors",
          "item_ref_count": 6,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/sliding_doors",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/sliding_doors.json",
          "text": "Sliding Doors",
          "key": "guide-page:pages.0.title",
          "label": "Sliding Doors",
          "item_ref_count": 6,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/surviving_mars/sliding_doors",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/surviving_mars/sliding_doors.json",
          "text": "Sliding doors are large 3x3 doors. They seal oxygen when closed, making them great candidates for airlocks. They can be locked by shift-right-clicking them with a wrench. Locked doors can only be opened with redstone.",
          "key": "guide-page:pages.0.text",
          "label": "Sliding Doors",
          "item_ref_count": 6,
          "recipe_ref_count": 3,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/creating_plates",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/creating_plates.json",
          "text": "Creating Plates",
          "key": "guide-page:name",
          "label": "Creating Plates",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/creating_plates",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/creating_plates.json",
          "text": "Once you've got energy, you'll want to craft a compressor. This machine can compress ingots and blocks into plates, which are needed to craft many things.",
          "key": "guide-page:pages.0.text",
          "label": "Creating Plates",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/fuel.json",
          "text": "Even after you place your rocket, you will not be able to fly just yet. To fly, you will need to fill the rocket with fuel.",
          "key": "guide-page:pages.0.text",
          "label": "Fuel",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/fuel.json",
          "text": "Obtaining Fuel",
          "key": "guide-page:pages.1.title",
          "label": "Fuel",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/fuel.json",
          "text": "Oil is found in large amounts spouting over the ocean surface. Collect as much oil as you can, as oil is non-renewable, and you need a lot of it for each rocket launch.",
          "key": "guide-page:pages.1.text",
          "label": "Fuel",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/fuel.json",
          "text": "Obtaining Fuel",
          "key": "guide-page:pages.2.title",
          "label": "Fuel",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/fuel.json",
          "text": "After collecting enough oil (at least six buckets for a launch and return), you will need to refine the oil into fuel. To do this, you will need to craft a fuel refinery. Power it, then place the oil in the left slot, and it will be refined into fuel.",
          "key": "guide-page:pages.2.text",
          "label": "Fuel",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/fuel.json",
          "text": "Fuel Refinery",
          "key": "guide-page:pages.3.title",
          "label": "Fuel",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/fuel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/fuel.json",
          "text": "Shift-click on the rocket to open its inventory, then place three buckets of fuel into the input slot. Place the other three buckets into the rocket's inventory for a return trip.",
          "key": "guide-page:pages.3.text",
          "label": "Fuel",
          "item_ref_count": 6,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/generating_energy",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/generating_energy.json",
          "text": "Generating Energy",
          "key": "guide-page:name",
          "label": "Generating Energy",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/generating_energy",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/generating_energy.json",
          "text": "Coal Generator",
          "key": "guide-page:pages.0.title",
          "label": "Generating Energy",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/generating_energy",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/generating_energy.json",
          "text": "Energy is core resource of Ad Astra. It's used in all machines and is required for everything. There are multiple ways to generate energy, the most basic way being the coal generator. It uses burnable resources like wood or coal to generate energy.",
          "key": "guide-page:pages.0.text",
          "label": "Generating Energy",
          "item_ref_count": 3,
          "recipe_ref_count": 1,
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/making_steel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/making_steel.json",
          "text": "Making Steel",
          "key": "guide-page:name",
          "label": "Making Steel",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/making_steel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/making_steel.json",
          "text": "Etrionic Blast Furnace",
          "key": "guide-page:pages.0.title",
          "label": "Making Steel",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/making_steel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/making_steel.json",
          "text": "The Etrionic Blast Furnace is an upgraded version of the blast furnace. It has two modes: blasting and alloying. Blasting works like a blast furnace, but with 4 slots for 4x efficiency. Alloying combines materials to make an alloy.",
          "key": "guide-page:pages.0.text",
          "label": "Making Steel",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/making_steel",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/making_steel.json",
          "text": "Steel is made in an etrionic blast furnace by combining iron and coal. This will make 1 steel ingot per iron and coal. Steel is essential for most recipes and is the main material of your first rocket.",
          "key": "guide-page:pages.2.text",
          "label": "Making Steel",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/oxygen",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/oxygen.json",
          "text": "At this point, you can launch your rocket; however, you will likely die very fast, as there is no oxygen on the moon. In order to survive on the moon, you will need an oxygenated space suit and an ample oxygen supply.",
          "key": "guide-page:pages.0.text",
          "label": "Oxygen",
          "item_ref_count": 7,
          "recipe_ref_count": 3,
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/oxygen",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/oxygen.json",
          "text": "To obtain oxygen, it must be extracted from water. An oxygen loader is required to do this. Craft one, then place water in the left tank, and watch it convert into oxygen. You must repeat this process many times until you have enough oxygen.",
          "key": "guide-page:pages.3.text",
          "label": "Oxygen",
          "item_ref_count": 7,
          "recipe_ref_count": 3,
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/oxygen",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/oxygen.json",
          "text": "Oxygen Loader",
          "key": "guide-page:pages.4.title",
          "label": "Oxygen",
          "item_ref_count": 7,
          "recipe_ref_count": 3,
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/oxygen",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/oxygen.json",
          "text": "Oxygen can then be extracted by collecting it in a bucket, spacesuit, or a gas tank. After you have enough oxygen, you are moments away from launching your first rocket!",
          "key": "guide-page:pages.4.text",
          "label": "Oxygen",
          "item_ref_count": 7,
          "recipe_ref_count": 3,
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/portable_resources",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/portable_resources.json",
          "text": "Portable Resources",
          "key": "guide-page:name",
          "label": "Portable Resources",
          "item_ref_count": 4,
          "recipe_ref_count": 2,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/portable_resources",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/portable_resources.json",
          "text": "Gas Tanks",
          "key": "guide-page:pages.0.title",
          "label": "Portable Resources",
          "item_ref_count": 4,
          "recipe_ref_count": 2,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/portable_resources",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/portable_resources.json",
          "text": "Oxygen tanks allow you to store oxygen in a convenient portable form. Despite the name, they can store any fluid. Holding down right-click will distribute the oxygen into items in your inventory, with a bias towards armor.",
          "key": "guide-page:pages.0.text",
          "label": "Portable Resources",
          "item_ref_count": 4,
          "recipe_ref_count": 2,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/portable_resources",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/portable_resources.json",
          "text": "Etrionic Capacitor",
          "key": "guide-page:pages.2.title",
          "label": "Portable Resources",
          "item_ref_count": 4,
          "recipe_ref_count": 2,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/portable_resources",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_first_launch/portable_resources.json",
          "text": "The Etrionic Capacitor is a battery-like item for storing energy in a portable form. It can be placed in the energy slot of machines to power them, and like the gas tanks, can be distributed into your inventory by holding down right-click. It features two modes: Sequential and Round Robin. Sequential will distribute energy one by one while Round Robin will distribute energy evenly. The mode can be changed by right-clicking while sneaking.",
          "key": "guide-page:pages.2.text",
          "label": "Portable Resources",
          "item_ref_count": 4,
          "recipe_ref_count": 2,
          "count": 4
        }
      ],
      "aliases": [],
      "parent": "tfc:glassworking"
    },
    {
      "id": "domum_ornamentum:architects_cutter#input",
      "label": "Architects Cutter Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 82,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "domum_ornamentum:architects_cutter",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:blockpaperwall",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Glass Framed Pane",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:blockpillar",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Round Reconstituted Stone Bricks Pillar",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:blocktiledpaperwall",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Glass Tiled Pane",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:blockypillar",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Voxel Reconstituted Stone Bricks Pillar",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:center_light",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed Glowstone",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:crossed_light",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed Glowstone",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:dark_brick",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Dark Polished Andesite Brick",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:dark_brick_stair",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Dark Polished Andesite Brick Stair",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:double_crossed",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:down_gated",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:dynamic_timberframe",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Dynamic Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:fancy_door",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Fancy Spruce Planks Door",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:fancy_light",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed Glowstone",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:fancy_trapdoors",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Fancy Stripped Oak Wood Trapdoor",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:four_light",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed Glowstone",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:framed",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:framed_light",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed Glowstone",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:horizontal_light",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed Glowstone",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:horizontal_plain",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:light_brick",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Light Polished Andesite Brick",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:light_brick_stair",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Light Polished Andesite Brick Stair",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:one_crossed_lr",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:one_crossed_rl",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:panel",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Stripped Oak Wood Panel",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:plain",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:post",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Oak Planks Post",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:shingle",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Brick Extra Shingles",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:shingle_flat",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Brick Extra Flat Shingles",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:shingle_flat_lower",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Brick Extra Flat Lower Shingles",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:shingle_slab",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Brick Extra Shingles",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:shingle_steep",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Brick Extra Steep Shingles",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:shingle_steep_lower",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Brick Extra Steep Lower Shingles",
          "item_ref_count": 1
        }
      ],
      "aliases": [],
      "parent": "domum_ornamentum:architects_cutter"
    },
    {
      "id": "domum_ornamentum:architects_cutter#output",
      "label": "Architects Cutter Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 82,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "domum_ornamentum:architects_cutter",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:blockpaperwall",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Glass Framed Pane",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:blockpillar",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Round Reconstituted Stone Bricks Pillar",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:blocktiledpaperwall",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Glass Tiled Pane",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:blockypillar",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Voxel Reconstituted Stone Bricks Pillar",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:center_light",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed Glowstone",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:crossed_light",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed Glowstone",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:dark_brick",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Dark Polished Andesite Brick",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:dark_brick_stair",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Dark Polished Andesite Brick Stair",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:double_crossed",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:down_gated",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:dynamic_timberframe",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Dynamic Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:fancy_door",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Fancy Spruce Planks Door",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:fancy_light",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed Glowstone",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:fancy_trapdoors",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Fancy Stripped Oak Wood Trapdoor",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:four_light",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed Glowstone",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:framed",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:framed_light",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed Glowstone",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:horizontal_light",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed Glowstone",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:horizontal_plain",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:light_brick",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Light Polished Andesite Brick",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:light_brick_stair",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Light Polished Andesite Brick Stair",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:one_crossed_lr",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:one_crossed_rl",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:panel",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Stripped Oak Wood Panel",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:plain",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Framed White Terracotta",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:post",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Oak Planks Post",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:shingle",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Brick Extra Shingles",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:shingle_flat",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Brick Extra Flat Shingles",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:shingle_flat_lower",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Brick Extra Flat Lower Shingles",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:shingle_slab",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Brick Extra Shingles",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:shingle_steep",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Brick Extra Steep Shingles",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "domum_ornamentum:shingle_steep_lower",
          "source": "runtime-items",
          "text": "Crafted in the Architect's Cutter",
          "key": "runtime-tooltip",
          "label": "Brick Extra Steep Lower Shingles",
          "item_ref_count": 1
        }
      ],
      "aliases": [],
      "parent": "domum_ornamentum:architects_cutter"
    },
    {
      "id": "firmalife:vat#input",
      "label": "Vat Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 81,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "firmalife:vat",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "firmalife:vat"
    },
    {
      "id": "firmalife:vat#output",
      "label": "Vat Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 81,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "firmalife:vat",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "firmalife:vat"
    },
    {
      "id": "tfc:pot_jam#input",
      "label": "Pot Jam Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 78,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:pot_jam",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/fig",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Fig Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/fig",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Fig Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/fig",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 3 day(s))",
          "key": "runtime-tooltip",
          "label": "Fig Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/pineapple",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Pineapple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/pineapple",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Pineapple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/pineapple",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 3 day(s))",
          "key": "runtime-tooltip",
          "label": "Pineapple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/red_grapes",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Red Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/red_grapes",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Red Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/red_grapes",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 3 day(s))",
          "key": "runtime-tooltip",
          "label": "Red Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/white_grapes",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "White Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/white_grapes",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "White Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/white_grapes",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 3 day(s))",
          "key": "runtime-tooltip",
          "label": "White Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/banana",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Banana Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/banana",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Banana Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/banana",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Banana Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blackberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Blackberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blackberry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Blackberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blackberry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Blackberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blueberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Blueberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blueberry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Blueberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blueberry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Blueberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/bunchberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Bunchberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/bunchberry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Bunchberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/bunchberry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Bunchberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cherry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Cherry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cherry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Cherry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cherry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Cherry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cloudberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Cloudberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cloudberry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Cloudberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cloudberry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Cloudberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cranberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Cranberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cranberry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Cranberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cranberry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Cranberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/elderberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Elderberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/elderberry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Elderberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/elderberry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Elderberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/gooseberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Gooseberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/gooseberry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Gooseberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/gooseberry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Gooseberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/green_apple",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Green Apple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/green_apple",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Green Apple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/green_apple",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Green Apple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/lemon",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Lemon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/lemon",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Lemon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/lemon",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Lemon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/melon_slice",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Melon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/melon_slice",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Melon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/melon_slice",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Melon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/olive",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Olive Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/olive",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Olive Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/olive",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Olive Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/orange",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Orange Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/orange",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Orange Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/orange",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Orange Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/peach",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Peach Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/peach",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Peach Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/peach",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Peach Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/plum",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Plum Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/plum",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Plum Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/plum",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Plum Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/pumpkin_chunks",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Pumpkin Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/pumpkin_chunks",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Pumpkin Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/pumpkin_chunks",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Pumpkin Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/raspberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Raspberry Jam",
          "item_ref_count": 1
        }
      ],
      "aliases": [],
      "parent": "tfc:pot_jam"
    },
    {
      "id": "tfc:pot_jam#output",
      "label": "Pot Jam Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 78,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:pot_jam",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/fig",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Fig Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/fig",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Fig Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/fig",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 3 day(s))",
          "key": "runtime-tooltip",
          "label": "Fig Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/pineapple",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Pineapple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/pineapple",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Pineapple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/pineapple",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 3 day(s))",
          "key": "runtime-tooltip",
          "label": "Pineapple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/red_grapes",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Red Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/red_grapes",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Red Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/red_grapes",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 3 day(s))",
          "key": "runtime-tooltip",
          "label": "Red Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/white_grapes",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "White Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/white_grapes",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "White Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "firmalife:jar/white_grapes",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 3 day(s))",
          "key": "runtime-tooltip",
          "label": "White Grapes Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/banana",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Banana Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/banana",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Banana Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/banana",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Banana Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blackberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Blackberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blackberry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Blackberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blackberry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Blackberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blueberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Blueberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blueberry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Blueberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/blueberry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Blueberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/bunchberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Bunchberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/bunchberry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Bunchberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/bunchberry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Bunchberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cherry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Cherry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cherry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Cherry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cherry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Cherry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cloudberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Cloudberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cloudberry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Cloudberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cloudberry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Cloudberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cranberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Cranberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cranberry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Cranberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/cranberry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Cranberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/elderberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Elderberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/elderberry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Elderberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/elderberry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Elderberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/gooseberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Gooseberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/gooseberry",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Gooseberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/gooseberry",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Gooseberry Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/green_apple",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Green Apple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/green_apple",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Green Apple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/green_apple",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Green Apple Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/lemon",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Lemon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/lemon",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Lemon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/lemon",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Lemon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/melon_slice",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Melon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/melon_slice",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Melon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/melon_slice",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Melon Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/olive",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Olive Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/olive",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Olive Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/olive",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Olive Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/orange",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Orange Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/orange",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Orange Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/orange",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Orange Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/peach",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Peach Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/peach",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Peach Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/peach",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Peach Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/plum",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Plum Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/plum",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Plum Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/plum",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Plum Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/pumpkin_chunks",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Pumpkin Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/pumpkin_chunks",
          "source": "runtime-items",
          "text": "Sealed",
          "key": "runtime-tooltip",
          "label": "Pumpkin Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/pumpkin_chunks",
          "source": "runtime-items",
          "text": "Expires on: 11:59 September 5, 1002 (in 2 year(s), 3 month(s) and 4 day(s))",
          "key": "runtime-tooltip",
          "label": "Pumpkin Jam",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:jar/raspberry",
          "source": "runtime-items",
          "text": "1.0 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Raspberry Jam",
          "item_ref_count": 1
        }
      ],
      "aliases": [],
      "parent": "tfc:pot_jam"
    },
    {
      "id": "firmalife:mixing_bowl#input",
      "label": "Mixing Bowl Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 67,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "firmalife:mixing_bowl",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "firmalife:mixing_bowl"
    },
    {
      "id": "firmalife:mixing_bowl#output",
      "label": "Mixing Bowl Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 67,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "firmalife:mixing_bowl",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "firmalife:mixing_bowl"
    },
    {
      "id": "tfc:sewing#input",
      "label": "Sewing Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 34,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:sewing",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "tfc:sewing"
    },
    {
      "id": "tfc:sewing#output",
      "label": "Sewing Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 34,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:sewing",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "tfc:sewing"
    },
    {
      "id": "create:mixing#input",
      "label": "Mixing Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 32,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "create:mixing",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "create:mixing"
    },
    {
      "id": "create:mixing#output",
      "label": "Mixing Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 32,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "create:mixing",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "create:mixing"
    },
    {
      "id": "firmalife:drying#input",
      "label": "Drying Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 29,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "firmalife:drying",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/chocolate",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/chocolate.json",
          "text": "Chocolate-making takes a few processing steps, for not much of a reward. It's important to remember, when playing Firmalife, that being a chocolatier is for your personal enjoyment and pleasure, rather than for trying to extract maximum value from any given input.",
          "key": "guide-page:pages.0.text",
          "label": "Chocolate",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/chocolate",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/chocolate.json",
          "text": "To start chocolate processing, cocoa beans must first be roasted in an Oven to make Roasted Cocoa Beans. Then, craft the roasted beans with a Knife to split the beans into Cocoa Powder and Cocoa Butter.",
          "key": "guide-page:pages.1.text",
          "label": "Chocolate",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/chocolate",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/chocolate.json",
          "text": "The Mixing Bowl is used to mix cocoa powder, butter, and sweetener (sugar or honey) to make Chocolate Blends. The ratio of cocoa butter to powder determines what comes out: - 1 Powder, 1 Butter, 1 Sweetener: Milk Chocolate - 2 Powder, 1 Sweetener: Dark Chocolate - 2 Butter, 1 Sweetener: White Chocolate",
          "key": "guide-page:pages.2.text",
          "label": "Chocolate",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/chocolate",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/chocolate.json",
          "text": "Finally, chocolate is dried on a Drying Mat to make Chocolate.",
          "key": "guide-page:pages.3.text",
          "label": "Chocolate",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "The Drying Mat is used to dry items. It is made with Fruit Leaves, which are obtained from breaking the leaves of Fruit Trees.",
          "key": "guide-page:pages.0.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "The recipe for the drying mat.",
          "key": "guide-page:pages.1.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "To use the drying mat, place it out on the sun and add an item to it with . After a half day, it will be dried. If it rains, the drying process must start over.",
          "key": "guide-page:pages.2.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "The solar drier functions the same as the drying mat, but 12x as fast.",
          "key": "guide-page:pages.3.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "Drying mats can be automated. Pushing a piston head against a drying mat will pop the item off. Dropping an item onto a drying mat will place it on the mat.",
          "key": "guide-page:pages.4.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "Drying fruit is a common use of the drying mat. Dried fruit is used in some recipes, and lasts longer.",
          "key": "guide-page:pages.5.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "Tofu is made using a drying mat.",
          "key": "guide-page:pages.6.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "Cinnamon is made using a drying mat.",
          "key": "guide-page:pages.7.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "More Fertilizer Options",
          "key": "guide-page:name",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "Given a greater need for fertilization in Firmalife, there are more options for getting fertilizers.",
          "key": "guide-page:pages.0.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "Thatch can be Dried into Dry Grass, which can be used in a Composter as a brown item.",
          "key": "guide-page:pages.1.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "Compost Tumblers are a great way to produce more fertilizer. They must be connected to mechanical power in order to work. It can only be interacted with when not powered, so consider connecting it to a clutch!",
          "key": "guide-page:pages.2.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "The compost tumbler is unique in that it takes more types of compost, and does not require precise ratios in order to work.",
          "key": "guide-page:pages.3.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "The tumbler can take green and brown items like a regular composter. It can also take pottery sherds, charcoal, fish, and bones in small amounts.",
          "key": "guide-page:pages.4.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "Smashing pottery with a hammer yields sherds.",
          "key": "guide-page:pages.5.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "Green and brown items count the same, being on the range 1-4, but the new additions like fish always count for 1. Adding too much weird stuff to the composter causes it to produce rotten compost. Further, you will not know it is rotten until the very end! If the compost is more than fifteen percent bones, fish, or pottery, or more than twenty percent charcoal. it will rot. Or, if there are 10 or more green units than brown units, it will rot.",
          "key": "guide-page:pages.6.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "Favorable amounts of certain additions can extend or shorten the length of time it takes for the compost to complete. Play around with it and see what happens. If 32 units are in the composter, 3 compost will be produced. If at least 24, 2 compost will be made. If 16 or more, 1 will be made. Below that, and there will be no compost.",
          "key": "guide-page:pages.7.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        }
      ],
      "aliases": [],
      "parent": "firmalife:drying"
    },
    {
      "id": "firmalife:drying#output",
      "label": "Drying Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 29,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "firmalife:drying",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/chocolate",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/chocolate.json",
          "text": "Chocolate-making takes a few processing steps, for not much of a reward. It's important to remember, when playing Firmalife, that being a chocolatier is for your personal enjoyment and pleasure, rather than for trying to extract maximum value from any given input.",
          "key": "guide-page:pages.0.text",
          "label": "Chocolate",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/chocolate",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/chocolate.json",
          "text": "To start chocolate processing, cocoa beans must first be roasted in an Oven to make Roasted Cocoa Beans. Then, craft the roasted beans with a Knife to split the beans into Cocoa Powder and Cocoa Butter.",
          "key": "guide-page:pages.1.text",
          "label": "Chocolate",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/chocolate",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/chocolate.json",
          "text": "The Mixing Bowl is used to mix cocoa powder, butter, and sweetener (sugar or honey) to make Chocolate Blends. The ratio of cocoa butter to powder determines what comes out: - 1 Powder, 1 Butter, 1 Sweetener: Milk Chocolate - 2 Powder, 1 Sweetener: Dark Chocolate - 2 Butter, 1 Sweetener: White Chocolate",
          "key": "guide-page:pages.2.text",
          "label": "Chocolate",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/chocolate",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/chocolate.json",
          "text": "Finally, chocolate is dried on a Drying Mat to make Chocolate.",
          "key": "guide-page:pages.3.text",
          "label": "Chocolate",
          "item_ref_count": 4,
          "recipe_ref_count": 1,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "The Drying Mat is used to dry items. It is made with Fruit Leaves, which are obtained from breaking the leaves of Fruit Trees.",
          "key": "guide-page:pages.0.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "The recipe for the drying mat.",
          "key": "guide-page:pages.1.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "To use the drying mat, place it out on the sun and add an item to it with . After a half day, it will be dried. If it rains, the drying process must start over.",
          "key": "guide-page:pages.2.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "The solar drier functions the same as the drying mat, but 12x as fast.",
          "key": "guide-page:pages.3.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "Drying mats can be automated. Pushing a piston head against a drying mat will pop the item off. Dropping an item onto a drying mat will place it on the mat.",
          "key": "guide-page:pages.4.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "Drying fruit is a common use of the drying mat. Dried fruit is used in some recipes, and lasts longer.",
          "key": "guide-page:pages.5.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "Tofu is made using a drying mat.",
          "key": "guide-page:pages.6.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/drying",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/drying.json",
          "text": "Cinnamon is made using a drying mat.",
          "key": "guide-page:pages.7.text",
          "label": "Drying",
          "item_ref_count": 5,
          "recipe_ref_count": 5,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "More Fertilizer Options",
          "key": "guide-page:name",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "Given a greater need for fertilization in Firmalife, there are more options for getting fertilizers.",
          "key": "guide-page:pages.0.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "Thatch can be Dried into Dry Grass, which can be used in a Composter as a brown item.",
          "key": "guide-page:pages.1.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "Compost Tumblers are a great way to produce more fertilizer. They must be connected to mechanical power in order to work. It can only be interacted with when not powered, so consider connecting it to a clutch!",
          "key": "guide-page:pages.2.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "The compost tumbler is unique in that it takes more types of compost, and does not require precise ratios in order to work.",
          "key": "guide-page:pages.3.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "The tumbler can take green and brown items like a regular composter. It can also take pottery sherds, charcoal, fish, and bones in small amounts.",
          "key": "guide-page:pages.4.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "Smashing pottery with a hammer yields sherds.",
          "key": "guide-page:pages.5.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "Green and brown items count the same, being on the range 1-4, but the new additions like fish always count for 1. Adding too much weird stuff to the composter causes it to produce rotten compost. Further, you will not know it is rotten until the very end! If the compost is more than fifteen percent bones, fish, or pottery, or more than twenty percent charcoal. it will rot. Or, if there are 10 or more green units than brown units, it will rot.",
          "key": "guide-page:pages.6.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/firmalife/more_fertilizer",
          "source": "jar:Firmalife-1.20.1-2.1.27.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmalife/more_fertilizer.json",
          "text": "Favorable amounts of certain additions can extend or shorten the length of time it takes for the compost to complete. Play around with it and see what happens. If 32 units are in the composter, 3 compost will be produced. If at least 24, 2 compost will be made. If 16 or more, 1 will be made. Below that, and there will be no compost.",
          "key": "guide-page:pages.7.text",
          "label": "More Fertilizer Options",
          "item_ref_count": 5,
          "recipe_ref_count": 3,
          "count": 8
        }
      ],
      "aliases": [],
      "parent": "firmalife:drying"
    },
    {
      "id": "firmalife:oven#input",
      "label": "Oven Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 28,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "firmalife:oven",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "firmalife:oven"
    },
    {
      "id": "firmalife:oven#output",
      "label": "Oven Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 28,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "firmalife:oven",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "firmalife:oven"
    },
    {
      "id": "vintageimprovements:vacuumizing#input",
      "label": "Vacuumizing Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 27,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "vintageimprovements:vacuumizing",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "vintageimprovements:vacuumizing"
    },
    {
      "id": "vintageimprovements:vacuumizing#output",
      "label": "Vacuumizing Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 27,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "vintageimprovements:vacuumizing",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "vintageimprovements:vacuumizing"
    },
    {
      "id": "afc:tree_tapping#input",
      "label": "Tree Tapping Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 20,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "afc:tree_tapping",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "afc:tree_tapping"
    },
    {
      "id": "afc:tree_tapping#output",
      "label": "Tree Tapping Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 20,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "afc:tree_tapping",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "afc:tree_tapping"
    },
    {
      "id": "create:mechanical_crafting#input",
      "label": "Mechanical Crafting Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 17,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "create:mechanical_crafting",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "create:mechanical_crafting"
    },
    {
      "id": "create:mechanical_crafting#output",
      "label": "Mechanical Crafting Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 17,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "create:mechanical_crafting",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "create:mechanical_crafting"
    },
    {
      "id": "pack:tfg/smithing_trim#input",
      "label": "Smithing Trim Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 16,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "smithing_trim",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "pack:tfg/smithing_trim"
    },
    {
      "id": "pack:tfg/smithing_trim#output",
      "label": "Smithing Trim Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 16,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "smithing_trim",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "pack:tfg/smithing_trim"
    },
    {
      "id": "tfc:alloy#input",
      "label": "Alloy Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 14,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:alloy",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "tfc:alloy"
    },
    {
      "id": "tfc:alloy#output",
      "label": "Alloy Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 14,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:alloy",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [],
      "aliases": [],
      "parent": "tfc:alloy"
    },
    {
      "id": "tfc:loom#input",
      "label": "Loom Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 13,
      "evidence": [
        {
          "kind": "advancement",
          "id": "tfc:crafting/vanilla/loom",
          "confidence": 0.65
        },
        {
          "kind": "recipe_type",
          "id": "tfc:loom",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Paper is either made from the processed stalk of the Papyrus crop, from Animal Hides, or from a lengthy process using specific types of Wood. Paper is useful for written materials like Books and Maps.",
          "key": "guide-page:pages.0.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Papyrus must first be cut into strips with a Knife",
          "key": "guide-page:pages.1.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Then, papyrus strips are soaked in a Barrel of Water.",
          "key": "guide-page:pages.2.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Then, soaked papyrus strips are woven together in a loom to make Unrefined Paper. Finally, it must be placed on a log and Scraped to make Paper.",
          "key": "guide-page:pages.3.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Flip to the next page for information on papermaking via the parchment process.",
          "key": "guide-page:pages.7.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Parchment Paper starts with a scraped hide. Review the leather making chapter to learn how to make it. Parchment requires treatment with a few different items. First, Pumice is needed. Pumice is found on the ground near Volcanoes, or from Sluicing or Panning ore deposits with Andesite, Rhyolite, or Dacite in them.",
          "key": "guide-page:pages.8.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "A Pumice rock placed on the ground.",
          "key": "guide-page:pages.9.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Crafting pumice, a hammer, and scraped hide gives sections of Treated Hide.",
          "key": "guide-page:pages.10.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Treated hide, lime powder, flour, and a fresh egg will complete the treatment process and yield usable paper.",
          "key": "guide-page:pages.11.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Creating Paper from Wood is an ancient process that requires processing of sturdy types of wood into sheets, which are broken down into a pulp, and then pressed, dried and scraped to make paper. Note that softer types of wood are not suitable for making paper",
          "key": "guide-page:pages.12.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Paper from Wood",
          "key": "guide-page:pages.12.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "First you need to find Hardwood: Acacia Ash Aspen Birch Blackwood Chestnut Hickory Maple Oak Rosewood Sycamore",
          "key": "guide-page:pages.13.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Stripped Hardwood",
          "key": "guide-page:pages.14.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "The process begins by stripping the Hardwood's bark off to expose the internal fibers of the log, You can strip the log by placing it down and right clicking it with an axe",
          "key": "guide-page:pages.14.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Hardwood Strip",
          "key": "guide-page:pages.15.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Next, the stripped wood is shaven with an axe to obtain Hardwood Strips. This can be done using an Axe or more sophisticated methods",
          "key": "guide-page:pages.15.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Soaked Hardwood Strip",
          "key": "guide-page:pages.16.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Then, the individual strips are soaked in a Barrel of Water. This debilitates the structural integrity of the wood so it can be broken down further",
          "key": "guide-page:pages.16.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Hardwood Pulp",
          "key": "guide-page:pages.17.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Afterwards, utilize the Quern to break down the soaked strips into Hardwood Pulp",
          "key": "guide-page:pages.17.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Piling up the Pulp",
          "key": "guide-page:pages.18.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "While the quern gives you small piles of pulp, you'll want to combine them together in a Workbench. More sophisticated methods of crushing the strips yield higher amounts of pulp",
          "key": "guide-page:pages.18.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Breaking down the Pulp",
          "key": "guide-page:pages.19.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Before the pulp can be processed further into paper, it needs to be broken down further by Boiling it with Lye in a Vat or a Pot.",
          "key": "guide-page:pages.19.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Pressing the Pulp",
          "key": "guide-page:pages.21.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "After broken down by boiling it with Lye, the Thermochemically Treated Hardwood Pulp can be pressed down into a sheet of Soaked Unrefined Paper",
          "key": "guide-page:pages.21.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "You can utilize an Anvil to beat down the pulp into a sheet",
          "key": "guide-page:pages.22.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Stomping in Barrel",
          "key": "guide-page:pages.23.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Alternatively, you can stomp the Thermochemically Treated Hardwood Pulp in a Stomping Barrel",
          "key": "guide-page:pages.23.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "The final step is Drying the Soaked Unrefined Paper into Unrefined Paper, Which then can be Scraped into Paper as shown before.",
          "key": "guide-page:pages.24.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Cutting Steps...?",
          "key": "guide-page:pages.25.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Advancing in the Tech-Tree will allow you to create paper from wood with less overall steps, check EMI for the different approaches you can take for making paper with wood.",
          "key": "guide-page:pages.25.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Paper is either made from the processed stalk of the Papyrus crop, or from Animal Hides. Paper is useful for written materials like Books and Maps.",
          "key": "guide-page:pages.0.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Papyrus must first be cut into strips with a Knife",
          "key": "guide-page:pages.1.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Then, papyrus strips are soaked in a Barrel of Water.",
          "key": "guide-page:pages.2.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Then, soaked papyrus strips are woven together in a loom to make Unrefined Paper. Finally, it must be placed on a log and Scraped to make Paper.",
          "key": "guide-page:pages.3.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Flip to the next page for information on papermaking via the parchment process.",
          "key": "guide-page:pages.7.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Parchment Paper starts with a scraped hide. Review the leather making chapter to learn how to make it. Parchment requires treatment with a few different items. First, Pumice is needed. Pumice is found on the ground near Volcanoes, or from Sluicing or Panning ore deposits with Andesite, Rhyolite, or Dacite in them.",
          "key": "guide-page:pages.8.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "A Pumice rock placed on the ground.",
          "key": "guide-page:pages.9.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Crafting pumice, a hammer, and scraped hide gives sections of Treated Hide.",
          "key": "guide-page:pages.10.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Treated hide, lime powder, flour, and a fresh egg will complete the treatment process and yield usable paper.",
          "key": "guide-page:pages.11.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "Weaving is the process of combining different kinds of string into Cloth. While the last step of weaving is done in a Loom, some cloths such as Wool, obtained from Wooly Animals, requires a Spindle to obtain Wool Yarn in order to be woven.",
          "key": "guide-page:pages.0.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "The Unfired Spindle Head is knapped from clay. It can then be fired to make a Spindle Head. To complete the spindle, craft it with a Stick.",
          "key": "guide-page:pages.1.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "Crafting Wool with a Spindle yields Wool Yarn.",
          "key": "guide-page:pages.2.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "The loom is crafted from just Lumber and a Stick.",
          "key": "guide-page:pages.3.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "The recipe for Wool Cloth takes 16 Wool Yarn. Adding to the loom is done with . Then, hold down to begin working the loom. When it is done, press to retrieve the item.",
          "key": "guide-page:pages.4.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "The stages of the loom working.",
          "key": "guide-page:pages.5.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "Wool Cloth can be re-woven into Wool Blocks. Wool blocks can be dyed.",
          "key": "guide-page:pages.6.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "Silk Cloth can be made in the loom out of String. It can be used as a wool cloth substitute in some cases.",
          "key": "guide-page:pages.7.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "Burlap Cloth does not have a use, but it can be made from Jute Fiber.",
          "key": "guide-page:pages.8.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        }
      ],
      "aliases": [],
      "parent": "tfc:loom"
    },
    {
      "id": "tfc:loom#output",
      "label": "Loom Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 13,
      "evidence": [
        {
          "kind": "advancement",
          "id": "tfc:crafting/vanilla/loom",
          "confidence": 0.65
        },
        {
          "kind": "recipe_type",
          "id": "tfc:loom",
          "confidence": 0.85
        }
      ],
      "semantic_evidence": [
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Paper is either made from the processed stalk of the Papyrus crop, from Animal Hides, or from a lengthy process using specific types of Wood. Paper is useful for written materials like Books and Maps.",
          "key": "guide-page:pages.0.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Papyrus must first be cut into strips with a Knife",
          "key": "guide-page:pages.1.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Then, papyrus strips are soaked in a Barrel of Water.",
          "key": "guide-page:pages.2.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Then, soaked papyrus strips are woven together in a loom to make Unrefined Paper. Finally, it must be placed on a log and Scraped to make Paper.",
          "key": "guide-page:pages.3.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Flip to the next page for information on papermaking via the parchment process.",
          "key": "guide-page:pages.7.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Parchment Paper starts with a scraped hide. Review the leather making chapter to learn how to make it. Parchment requires treatment with a few different items. First, Pumice is needed. Pumice is found on the ground near Volcanoes, or from Sluicing or Panning ore deposits with Andesite, Rhyolite, or Dacite in them.",
          "key": "guide-page:pages.8.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "A Pumice rock placed on the ground.",
          "key": "guide-page:pages.9.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Crafting pumice, a hammer, and scraped hide gives sections of Treated Hide.",
          "key": "guide-page:pages.10.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Treated hide, lime powder, flour, and a fresh egg will complete the treatment process and yield usable paper.",
          "key": "guide-page:pages.11.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Creating Paper from Wood is an ancient process that requires processing of sturdy types of wood into sheets, which are broken down into a pulp, and then pressed, dried and scraped to make paper. Note that softer types of wood are not suitable for making paper",
          "key": "guide-page:pages.12.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Paper from Wood",
          "key": "guide-page:pages.12.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "First you need to find Hardwood: Acacia Ash Aspen Birch Blackwood Chestnut Hickory Maple Oak Rosewood Sycamore",
          "key": "guide-page:pages.13.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Stripped Hardwood",
          "key": "guide-page:pages.14.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "The process begins by stripping the Hardwood's bark off to expose the internal fibers of the log, You can strip the log by placing it down and right clicking it with an axe",
          "key": "guide-page:pages.14.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Hardwood Strip",
          "key": "guide-page:pages.15.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Next, the stripped wood is shaven with an axe to obtain Hardwood Strips. This can be done using an Axe or more sophisticated methods",
          "key": "guide-page:pages.15.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Soaked Hardwood Strip",
          "key": "guide-page:pages.16.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Then, the individual strips are soaked in a Barrel of Water. This debilitates the structural integrity of the wood so it can be broken down further",
          "key": "guide-page:pages.16.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Hardwood Pulp",
          "key": "guide-page:pages.17.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Afterwards, utilize the Quern to break down the soaked strips into Hardwood Pulp",
          "key": "guide-page:pages.17.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Piling up the Pulp",
          "key": "guide-page:pages.18.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "While the quern gives you small piles of pulp, you'll want to combine them together in a Workbench. More sophisticated methods of crushing the strips yield higher amounts of pulp",
          "key": "guide-page:pages.18.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Breaking down the Pulp",
          "key": "guide-page:pages.19.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Before the pulp can be processed further into paper, it needs to be broken down further by Boiling it with Lye in a Vat or a Pot.",
          "key": "guide-page:pages.19.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Pressing the Pulp",
          "key": "guide-page:pages.21.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "After broken down by boiling it with Lye, the Thermochemically Treated Hardwood Pulp can be pressed down into a sheet of Soaked Unrefined Paper",
          "key": "guide-page:pages.21.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "You can utilize an Anvil to beat down the pulp into a sheet",
          "key": "guide-page:pages.22.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Stomping in Barrel",
          "key": "guide-page:pages.23.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Alternatively, you can stomp the Thermochemically Treated Hardwood Pulp in a Stomping Barrel",
          "key": "guide-page:pages.23.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "The final step is Drying the Soaked Unrefined Paper into Unrefined Paper, Which then can be Scraped into Paper as shown before.",
          "key": "guide-page:pages.24.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Cutting Steps...?",
          "key": "guide-page:pages.25.title",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "file:minecraft/kubejs/assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Advancing in the Tech-Tree will allow you to create paper from wood with less overall steps, check EMI for the different approaches you can take for making paper with wood.",
          "key": "guide-page:pages.25.text",
          "label": "Papermaking",
          "item_ref_count": 20,
          "recipe_ref_count": 14,
          "count": 26
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Paper is either made from the processed stalk of the Papyrus crop, or from Animal Hides. Paper is useful for written materials like Books and Maps.",
          "key": "guide-page:pages.0.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Papyrus must first be cut into strips with a Knife",
          "key": "guide-page:pages.1.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Then, papyrus strips are soaked in a Barrel of Water.",
          "key": "guide-page:pages.2.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Then, soaked papyrus strips are woven together in a loom to make Unrefined Paper. Finally, it must be placed on a log and Scraped to make Paper.",
          "key": "guide-page:pages.3.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Flip to the next page for information on papermaking via the parchment process.",
          "key": "guide-page:pages.7.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Parchment Paper starts with a scraped hide. Review the leather making chapter to learn how to make it. Parchment requires treatment with a few different items. First, Pumice is needed. Pumice is found on the ground near Volcanoes, or from Sluicing or Panning ore deposits with Andesite, Rhyolite, or Dacite in them.",
          "key": "guide-page:pages.8.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "A Pumice rock placed on the ground.",
          "key": "guide-page:pages.9.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Crafting pumice, a hammer, and scraped hide gives sections of Treated Hide.",
          "key": "guide-page:pages.10.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/papermaking",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/papermaking.json",
          "text": "Treated hide, lime powder, flour, and a fresh egg will complete the treatment process and yield usable paper.",
          "key": "guide-page:pages.11.text",
          "label": "Papermaking",
          "item_ref_count": 11,
          "recipe_ref_count": 8,
          "count": 12
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "Weaving is the process of combining different kinds of string into Cloth. While the last step of weaving is done in a Loom, some cloths such as Wool, obtained from Wooly Animals, requires a Spindle to obtain Wool Yarn in order to be woven.",
          "key": "guide-page:pages.0.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "The Unfired Spindle Head is knapped from clay. It can then be fired to make a Spindle Head. To complete the spindle, craft it with a Stick.",
          "key": "guide-page:pages.1.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "Crafting Wool with a Spindle yields Wool Yarn.",
          "key": "guide-page:pages.2.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "The loom is crafted from just Lumber and a Stick.",
          "key": "guide-page:pages.3.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "The recipe for Wool Cloth takes 16 Wool Yarn. Adding to the loom is done with . Then, hold down to begin working the loom. When it is done, press to retrieve the item.",
          "key": "guide-page:pages.4.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "The stages of the loom working.",
          "key": "guide-page:pages.5.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "Wool Cloth can be re-woven into Wool Blocks. Wool blocks can be dyed.",
          "key": "guide-page:pages.6.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "Silk Cloth can be made in the loom out of String. It can be used as a wool cloth substitute in some cases.",
          "key": "guide-page:pages.7.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/weaving",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/weaving.json",
          "text": "Burlap Cloth does not have a use, but it can be made from Jute Fiber.",
          "key": "guide-page:pages.8.text",
          "label": "Weaving",
          "item_ref_count": 12,
          "recipe_ref_count": 8,
          "count": 10
        }
      ],
      "aliases": [],
      "parent": "tfc:loom"
    },
    {
      "id": "tfc:scraping#input",
      "label": "Scraping Recipe Input",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 13,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:scraping",
          "confidence": 0.65
        }
      ],
      "semantic_evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:scraping",
          "source": "file:minecraft/lang-overlays",
          "text": "Scraping Recipe",
          "key": "recipe-category-lang:tfc.jei.scraping",
          "label": "Scraping Recipe",
          "recipe_type": "tfc:scraping",
          "count": 1
        },
        {
          "kind": "recipe_type",
          "id": "tfc:scraping",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/*/lang/en_us.json",
          "text": "Scraping Recipe",
          "key": "recipe-category-lang:tfc.jei.scraping",
          "label": "Scraping Recipe",
          "recipe_type": "tfc:scraping",
          "count": 1
        }
      ],
      "aliases": [],
      "parent": "tfc:scraping"
    },
    {
      "id": "tfc:scraping#output",
      "label": "Scraping Recipe Output",
      "origin": "pack_generated",
      "confidence": 0.85,
      "support": 13,
      "evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:scraping",
          "confidence": 0.65
        }
      ],
      "semantic_evidence": [
        {
          "kind": "recipe_type",
          "id": "tfc:scraping",
          "source": "file:minecraft/lang-overlays",
          "text": "Scraping Recipe",
          "key": "recipe-category-lang:tfc.jei.scraping",
          "label": "Scraping Recipe",
          "recipe_type": "tfc:scraping",
          "count": 1
        },
        {
          "kind": "recipe_type",
          "id": "tfc:scraping",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/*/lang/en_us.json",
          "text": "Scraping Recipe",
          "key": "recipe-category-lang:tfc.jei.scraping",
          "label": "Scraping Recipe",
          "recipe_type": "tfc:scraping",
          "count": 1
        }
      ],
      "aliases": [],
      "parent": "tfc:scraping"
    }
  ],
  "required_output_contract": {
    "required_values_count": 48,
    "required_candidate_ids": [
      "createaddition:rolling#input",
      "createaddition:rolling#output",
      "tfc:casting#input",
      "tfc:casting#output",
      "greate:brewing#input",
      "greate:brewing#output",
      "tfc:pot#input",
      "tfc:pot#output",
      "tfc:welding#input",
      "tfc:welding#output",
      "vintageimprovements:coiling#input",
      "vintageimprovements:coiling#output",
      "tfc:knapping#input",
      "tfc:knapping#output",
      "vintageimprovements:vibrating#input",
      "vintageimprovements:vibrating#output",
      "tfc:glassworking#input",
      "tfc:glassworking#output",
      "domum_ornamentum:architects_cutter#input",
      "domum_ornamentum:architects_cutter#output",
      "firmalife:vat#input",
      "firmalife:vat#output",
      "tfc:pot_jam#input",
      "tfc:pot_jam#output",
      "firmalife:mixing_bowl#input",
      "firmalife:mixing_bowl#output",
      "tfc:sewing#input",
      "tfc:sewing#output",
      "create:mixing#input",
      "create:mixing#output",
      "firmalife:drying#input",
      "firmalife:drying#output",
      "firmalife:oven#input",
      "firmalife:oven#output",
      "vintageimprovements:vacuumizing#input",
      "vintageimprovements:vacuumizing#output",
      "afc:tree_tapping#input",
      "afc:tree_tapping#output",
      "create:mechanical_crafting#input",
      "create:mechanical_crafting#output",
      "pack:tfg/smithing_trim#input",
      "pack:tfg/smithing_trim#output",
      "tfc:alloy#input",
      "tfc:alloy#output",
      "tfc:loom#input",
      "tfc:loom#output",
      "tfc:scraping#input",
      "tfc:scraping#output"
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