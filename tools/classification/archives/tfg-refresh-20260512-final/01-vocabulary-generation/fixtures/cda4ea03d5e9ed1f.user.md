{
  "pack_id": "tfg",
  "facet": "mod_subsystem",
  "policy": "Identity-oriented mod subsystem. Do not assign from recipe participation alone.",
  "min_evidence": 2,
  "previous_accepted": [],
  "prompt_budget": {
    "max_chars": 3200000,
    "semantic_evidence_per_candidate": 12,
    "evidence_refs_per_candidate": 16
  },
  "candidates": [
    {
      "id": "ad_astra:rocket",
      "label": "Rocket",
      "origin": "namespace_generated",
      "confidence": 0.75,
      "support": 31,
      "evidence": [
        {
          "kind": "advancement",
          "id": "ad_astra:recipes/misc/rocket_fin",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:recipes/misc/rocket_nose_cone",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:recipes/nasa_workbench/nasa_workbench/tier_1_rocket_from_nasa_workbench",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:recipes/nasa_workbench/nasa_workbench/tier_2_rocket_from_nasa_workbench",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:recipes/nasa_workbench/nasa_workbench/tier_3_rocket_from_nasa_workbench",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:recipes/nasa_workbench/nasa_workbench/tier_4_rocket_from_nasa_workbench",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:rocket_man",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:rocket_science",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:tier_1_rocket",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:tier_2_rocket",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:tier_3_rocket",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:tier_4_rocket",
          "confidence": 0.65
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/your_first_rocket",
          "confidence": 0.7
        },
        {
          "kind": "item_tag",
          "id": "ad_astra:rocket_engine",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "ad_astra:rocket_tank",
          "confidence": 0.75
        },
        {
          "kind": "runtime_item",
          "id": "ad_astra:desh_engine",
          "confidence": 1
        }
      ],
      "evidence_omitted": 9,
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "ad_astra:rocket_science",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!data/ad_astra/advancements/rocket_science.json",
          "text": "Rocket Science",
          "key": "advancement-title:title",
          "label": "Rocket Science",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "ad_astra:rocket_science",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!data/ad_astra/advancements/rocket_science.json",
          "text": "Construct a NASA Workbench, allowing you to craft rockets",
          "key": "advancement-description:description",
          "label": "Rocket Science",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "ad_astra:rocket_man",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!data/ad_astra/advancements/rocket_man.json",
          "text": "Rocket Man",
          "key": "advancement-title:title",
          "label": "Rocket Man",
          "item_ref_count": 4
        },
        {
          "kind": "advancement",
          "id": "ad_astra:rocket_man",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!data/ad_astra/advancements/rocket_man.json",
          "text": "Craft a jet suit, allowing you to propel yourself forward with extreme force",
          "key": "advancement-description:description",
          "label": "Rocket Man",
          "item_ref_count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_hot_planets/jet_suit",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_hot_planets/jet_suit.json",
          "text": "Jet Suit",
          "key": "guide-page:name",
          "label": "Jet Suit",
          "item_ref_count": 6,
          "recipe_ref_count": 2,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_hot_planets/jet_suit",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_hot_planets/jet_suit.json",
          "text": "Jet Suit",
          "key": "guide-page:pages.0.title",
          "label": "Jet Suit",
          "item_ref_count": 6,
          "recipe_ref_count": 2,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_hot_planets/jet_suit",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_hot_planets/jet_suit.json",
          "text": "The Jet Suit is an upgraded version of the Netherite Space Suit, with more protection, additional oxygen storage, and flight. It's expensive to craft, but it offers high-speed flight, making travel easy.",
          "key": "guide-page:pages.0.text",
          "label": "Jet Suit",
          "item_ref_count": 6,
          "recipe_ref_count": 2,
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_hot_planets/jet_suit",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_hot_planets/jet_suit.json",
          "text": "It's got two modes of flight: ascending () and boosted ( + ). Ascending makes the wearer fly upwards, while boosted is similar to an elytra that's constantly being propelled by fireworks. The Jet Suit is powered by energy and must be charged to fly. It can be charged with an energizer by right-clicking it with the jet suit. Enable/disable the suit by pressing ().",
          "key": "guide-page:pages.1.text",
          "label": "Jet Suit",
          "item_ref_count": 6,
          "recipe_ref_count": 2,
          "count": 4
        },
        {
          "kind": "kubejs_tooltip",
          "id": "kubejs:tooltips/51",
          "source": "file:minecraft/kubejs/client_scripts/tooltips.js",
          "text": "Warmth: 0",
          "key": "kubejs-tooltip:tfg.tooltip.armor.space_suit_warmth",
          "label": "Warmth: 0",
          "item_ref_count": 12
        },
        {
          "kind": "kubejs_tooltip",
          "id": "kubejs:tooltips/51",
          "source": "file:minecraft/kubejs/client_scripts/tooltips.js",
          "text": "Insulation: +10",
          "key": "kubejs-tooltip:tfg.tooltip.armor.space_suit_insulation",
          "label": "Warmth: 0",
          "item_ref_count": 12
        },
        {
          "kind": "kubejs_tooltip",
          "id": "kubejs:tooltips/51",
          "source": "file:minecraft/kubejs/client_scripts/tooltips.js",
          "text": "Wear the full set to be Fully Insulated from ambient temperatures.",
          "key": "kubejs-tooltip:tfg.tooltip.armor.space_suit_set",
          "label": "Warmth: 0",
          "item_ref_count": 12
        },
        {
          "kind": "runtime_item",
          "id": "ad_astra:jet_suit",
          "source": "runtime-items",
          "text": "Warmth: 0",
          "key": "runtime-tooltip",
          "label": "Jet Suit",
          "item_ref_count": 1
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "create:engine",
      "label": "Engine",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 31,
      "evidence": [
        {
          "kind": "advancement",
          "id": "create:recipes/misc/crafting/kinetics/steam_engine",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create:steam_engine",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create:steam_engine_maxed",
          "confidence": 0.65
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/steam_engine",
          "confidence": 0.7
        },
        {
          "kind": "runtime_item",
          "id": "create:steam_engine",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "create:steam_engine",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/steam_engine.json",
          "text": "The Powerhouse",
          "key": "advancement-title:title",
          "label": "The Powerhouse",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create:steam_engine",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/steam_engine.json",
          "text": "Use a Steam Engine to generate torque",
          "key": "advancement-description:description",
          "label": "The Powerhouse",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create:steam_engine_maxed",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/steam_engine_maxed.json",
          "text": "Full Steam",
          "key": "advancement-title:title",
          "label": "Full Steam",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create:steam_engine_maxed",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/steam_engine_maxed.json",
          "text": "Run a boiler at the maximum level of power",
          "key": "advancement-description:description",
          "label": "Full Steam",
          "item_ref_count": 1
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "questssteam_age",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "The Millstone is an automatic version of the Quern. You can throw whatever you'd like into the top, and then right-click to take your crushed items back out. It's pretty slow if you connect it directly to your Animal Crank, but you can use gear ratios to increase its speed.",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "You will only receive the output in the first slot. The other slots are part of a GregTech mechanic that won't be relevant until much later (HV).",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "{@pagebreak}",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "Lore: The millstone can't be used to process grains into flour because Create doesn't understand TFC's food expiry system, which previously led to all sorts of bugs involving rotten items becoming fresh and vice versa. Until you're able to get the Food Processor in LV, you can still crush your grains via a Mortar in a crafting grid.",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "No more querning",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "Automatic Ore Processing",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "The Animal Crank is your first accessible source of mechanical power. To use it, place the crank on the center of a 7x7 cleared area and leash an animal to it. Different animals will provide different amounts of power, while the blocks underneath will increase the speed of the output power. The area of multiple cranks can overlap.",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "create:cogwheel",
      "label": "Cogwheel",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 29,
      "evidence": [
        {
          "kind": "advancement",
          "id": "create:recipes/misc/crafting/kinetics/cogwheel",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create:recipes/misc/crafting/kinetics/large_cogwheel",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create:recipes/misc/crafting/kinetics/large_cogwheel_from_little",
          "confidence": 0.65
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cogwheel",
          "confidence": 0.7
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cogwheel_casing",
          "confidence": 0.7
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/large_cogwheel",
          "confidence": 0.7
        },
        {
          "kind": "runtime_item",
          "id": "create:andesite_encased_cogwheel",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:andesite_encased_large_cogwheel",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:brass_encased_cogwheel",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:brass_encased_large_cogwheel",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:cogwheel",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:large_cogwheel",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "create:andesite_alloy",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/andesite_alloy.json",
          "text": "Sturdier Rocks",
          "key": "advancement-title:title",
          "label": "Sturdier Rocks",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create:andesite_alloy",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/andesite_alloy.json",
          "text": "Obtain some Andesite Alloy, Create's most important resource",
          "key": "advancement-description:description",
          "label": "Sturdier Rocks",
          "item_ref_count": 1
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cogwheel",
          "source": "file:minecraft/lang-overlays",
          "text": "Relaying rotational force using Cogwheels",
          "key": "lang-ponder:create.ponder.cogwheel.header",
          "label": "Relaying rotational force using Cogwheels",
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cogwheel",
          "source": "file:minecraft/lang-overlays",
          "text": "Cogwheels will relay rotation to other adjacent cogwheels",
          "key": "lang-ponder:create.ponder.cogwheel.text_1",
          "label": "Relaying rotational force using Cogwheels",
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cogwheel",
          "source": "file:minecraft/lang-overlays",
          "text": "Neighbouring shafts connected like this will rotate in opposite directions",
          "key": "lang-ponder:create.ponder.cogwheel.text_2",
          "label": "Relaying rotational force using Cogwheels",
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cogwheel",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Relaying rotational force using Cogwheels",
          "key": "lang-ponder:create.ponder.cogwheel.header",
          "label": "Relaying rotational force using Cogwheels",
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cogwheel",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Cogwheels will relay rotation to other adjacent cogwheels",
          "key": "lang-ponder:create.ponder.cogwheel.text_1",
          "label": "Relaying rotational force using Cogwheels",
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cogwheel",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Neighbouring shafts connected like this will rotate in opposite directions",
          "key": "lang-ponder:create.ponder.cogwheel.text_2",
          "label": "Relaying rotational force using Cogwheels",
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cogwheel_casing",
          "source": "file:minecraft/lang-overlays",
          "text": "Encasing Cogwheels",
          "key": "lang-ponder:create.ponder.cogwheel_casing.header",
          "label": "Encasing Cogwheels",
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cogwheel_casing",
          "source": "file:minecraft/lang-overlays",
          "text": "Brass or Metal Casing can be used to decorate Cogwheels",
          "key": "lang-ponder:create.ponder.cogwheel_casing.text_1",
          "label": "Encasing Cogwheels",
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cogwheel_casing",
          "source": "file:minecraft/lang-overlays",
          "text": "Components added after encasing will not connect to the shaft outputs",
          "key": "lang-ponder:create.ponder.cogwheel_casing.text_2",
          "label": "Encasing Cogwheels",
          "count": 4
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cogwheel_casing",
          "source": "file:minecraft/lang-overlays",
          "text": "The Wrench can be used to toggle connections",
          "key": "lang-ponder:create.ponder.cogwheel_casing.text_3",
          "label": "Encasing Cogwheels",
          "count": 4
        }
      ],
      "semantic_evidence_omitted": 12,
      "aliases": []
    },
    {
      "id": "tfclunchbox:battery",
      "label": "Battery",
      "origin": "namespace_generated",
      "confidence": 0.75,
      "support": 28,
      "evidence": [
        {
          "kind": "item_tag",
          "id": "tfclunchbox:electric_batteries",
          "confidence": 0.75
        }
      ],
      "semantic_evidence": [
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "gregtech_energy",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "We know that GregTech isn’t the easiest mod to get into — especially when it comes to the energy system. That’s why we’ll take our time in this chapter to explain as much as we can.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "Some things here might not make full sense until later in your progression, so don’t stress yourself. Just try to understand what you can for now, and feel free to come back to this chapter whenever you have questions.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "We’ve split this chapter into four categories, each one covering a topic related to the GregTech Energy System. We’ll provide as many examples as possible to help you understand how it all works.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "It's not as bad as you think",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "Welcome aboard",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "Let's do some explaining",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "Moving Energy in GregTech means understanding a few core mechanics.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "First, Energy Tiers. From LV to UHV, everything in GregTech — wires, machines, recipes — is tied to a tier. You’ll need the correct cable material to move energy. For example:",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "• Tin wire = LV",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "• Copper wire = MV",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "Second, you’ve got the Amperage mechanic. Think of 1 Amp (or 1A) as a packet of energy. Machines request energy \"packets\", which then get sent down the wire.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "create:pump",
      "label": "Pump",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 28,
      "evidence": [
        {
          "kind": "advancement",
          "id": "create:mechanical_pump_0",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create:recipes/misc/crafting/kinetics/mechanical_pump",
          "confidence": 0.65
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/mechanical_pump_flow",
          "confidence": 0.7
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/mechanical_pump_speed",
          "confidence": 0.7
        },
        {
          "kind": "runtime_item",
          "id": "create:mechanical_pump",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "create:mechanical_pump_0",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/mechanical_pump_0.json",
          "text": "Under Pressure",
          "key": "advancement-title:title",
          "label": "Under Pressure",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create:mechanical_pump_0",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/mechanical_pump_0.json",
          "text": "Place and power a Mechanical Pump",
          "key": "advancement-description:description",
          "label": "Under Pressure",
          "item_ref_count": 1
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/mechanical_pump_flow",
          "source": "file:minecraft/lang-overlays",
          "text": "Fluid Transportation using Mechanical Pumps",
          "key": "lang-ponder:create.ponder.mechanical_pump_flow.header",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/mechanical_pump_flow",
          "source": "file:minecraft/lang-overlays",
          "text": "Mechanical Pumps govern the flow of their attached pipe networks",
          "key": "lang-ponder:create.ponder.mechanical_pump_flow.text_1",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/mechanical_pump_flow",
          "source": "file:minecraft/lang-overlays",
          "text": "Their arrow indicates the direction of flow",
          "key": "lang-ponder:create.ponder.mechanical_pump_flow.text_2",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/mechanical_pump_flow",
          "source": "file:minecraft/lang-overlays",
          "text": "The network behind is now pulling fluids...",
          "key": "lang-ponder:create.ponder.mechanical_pump_flow.text_3",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/mechanical_pump_flow",
          "source": "file:minecraft/lang-overlays",
          "text": "...while the network in front is transferring it outward",
          "key": "lang-ponder:create.ponder.mechanical_pump_flow.text_4",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/mechanical_pump_flow",
          "source": "file:minecraft/lang-overlays",
          "text": "The pumps direction is unaffected by the input rotation",
          "key": "lang-ponder:create.ponder.mechanical_pump_flow.text_5",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/mechanical_pump_flow",
          "source": "file:minecraft/lang-overlays",
          "text": "Instead, a Wrench can be used to reverse the direction",
          "key": "lang-ponder:create.ponder.mechanical_pump_flow.text_6",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/mechanical_pump_flow",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Fluid Transportation using Mechanical Pumps",
          "key": "lang-ponder:create.ponder.mechanical_pump_flow.header",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/mechanical_pump_flow",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Mechanical Pumps govern the flow of their attached pipe networks",
          "key": "lang-ponder:create.ponder.mechanical_pump_flow.text_1",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/mechanical_pump_flow",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Their arrow indicates the direction of flow",
          "key": "lang-ponder:create.ponder.mechanical_pump_flow.text_2",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        }
      ],
      "semantic_evidence_omitted": 16,
      "aliases": []
    },
    {
      "id": "immersive_aircraft:transport",
      "label": "Transport",
      "origin": "namespace_generated",
      "confidence": 0.6,
      "support": 27,
      "evidence": [
        {
          "kind": "mod_metadata",
          "id": "immersive_aircraft",
          "confidence": 0.6
        }
      ],
      "semantic_evidence": [
        {
          "kind": "mod_metadata",
          "id": "immersive_aircraft",
          "source": "mods-folder-scan",
          "text": "A bunch of rustic aircraft to travel, transport, and explore!",
          "key": "mod-description:mod.immersive_aircraft.description",
          "label": "Immersive Aircraft",
          "count": 27
        }
      ],
      "aliases": [
        "immersive_aircraft"
      ]
    },
    {
      "id": "ae2:storage",
      "label": "Storage",
      "origin": "namespace_generated",
      "confidence": 0.55,
      "support": 26,
      "evidence": [
        {
          "kind": "runtime_item",
          "id": "ae2:16k_crafting_storage",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:1k_crafting_storage",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:256k_crafting_storage",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:4k_crafting_storage",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:64k_crafting_storage",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:cell_component_16k",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:cell_component_1k",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:cell_component_256k",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:cell_component_4k",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:cell_component_64k",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:fluid_storage_cell_16k",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:fluid_storage_cell_1k",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:fluid_storage_cell_256k",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:fluid_storage_cell_4k",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:fluid_storage_cell_64k",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:item_storage_cell_16k",
          "confidence": 1
        }
      ],
      "evidence_omitted": 10,
      "semantic_evidence": [
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/applied_energistics_2",
          "source": "file:minecraft/config/ftbquests/quests/chapters/applied_energistics_2.snbt",
          "text": "applied_energistics_2",
          "key": "quest-snbt",
          "label": "applied_energistics_2",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/applied_energistics_2",
          "source": "file:minecraft/config/ftbquests/quests/chapters/applied_energistics_2.snbt",
          "text": "With your first step on the Moon comes access to your first Certus Quartz vein.",
          "key": "quest-snbt",
          "label": "applied_energistics_2",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/applied_energistics_2",
          "source": "file:minecraft/config/ftbquests/quests/chapters/applied_energistics_2.snbt",
          "text": "Don't worry, they're quite common and should be easy to locate, though having a decent Ore Prospector wouldn't hurt.",
          "key": "quest-snbt",
          "label": "applied_energistics_2",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/applied_energistics_2",
          "source": "file:minecraft/config/ftbquests/quests/chapters/applied_energistics_2.snbt",
          "text": "Whether you decide to set up a miner or manually dig out a full vein, make sure to gather a hefty amount, as you'll need it to progress through AE2.",
          "key": "quest-snbt",
          "label": "applied_energistics_2",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/applied_energistics_2",
          "source": "file:minecraft/config/ftbquests/quests/chapters/applied_energistics_2.snbt",
          "text": "One small step for you, one giant leap for logistics",
          "key": "quest-snbt",
          "label": "applied_energistics_2",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/applied_energistics_2",
          "source": "file:minecraft/config/ftbquests/quests/chapters/applied_energistics_2.snbt",
          "text": "or(item(gtceu:poor_raw_certus_quartz)item(gtceu:raw_certus_quartz)item(gtceu:rich_raw_certus_quartz))",
          "key": "quest-snbt",
          "label": "applied_energistics_2",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/applied_energistics_2",
          "source": "file:minecraft/config/ftbquests/quests/chapters/applied_energistics_2.snbt",
          "text": "Certus Quartz Ore",
          "key": "quest-snbt",
          "label": "applied_energistics_2",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/applied_energistics_2",
          "source": "file:minecraft/config/ftbquests/quests/chapters/applied_energistics_2.snbt",
          "text": "Certus Quartz",
          "key": "quest-snbt",
          "label": "applied_energistics_2",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/applied_energistics_2",
          "source": "file:minecraft/config/ftbquests/quests/chapters/applied_energistics_2.snbt",
          "text": "One of your main resources for Applied Energistics 2 will be Charged Certus Quartz.",
          "key": "quest-snbt",
          "label": "applied_energistics_2",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/applied_energistics_2",
          "source": "file:minecraft/config/ftbquests/quests/chapters/applied_energistics_2.snbt",
          "text": "To obtain it, there’s really only one method: the HV Polarizer.",
          "key": "quest-snbt",
          "label": "applied_energistics_2",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/applied_energistics_2",
          "source": "file:minecraft/config/ftbquests/quests/chapters/applied_energistics_2.snbt",
          "text": "You’ll quickly notice how long this recipe takes, so it’s strongly recommended to dedicate a Polarizer exclusively to this task.",
          "key": "quest-snbt",
          "label": "applied_energistics_2",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/applied_energistics_2",
          "source": "file:minecraft/config/ftbquests/quests/chapters/applied_energistics_2.snbt",
          "text": "It may seem expensive or daunting at first, but don't worry—it’s absolutely worth it.",
          "key": "quest-snbt",
          "label": "applied_energistics_2",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "gtceu:generator",
      "label": "Generator",
      "origin": "namespace_generated",
      "confidence": 0.75,
      "support": 25,
      "evidence": [
        {
          "kind": "item_tag",
          "id": "gtceu:field_generators",
          "confidence": 0.75
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_field_generator",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_combustion",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_field_generator",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_gas_turbine",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_steam_turbine",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_field_generator",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:luv_field_generator",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_combustion",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_field_generator",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_gas_turbine",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_steam_turbine",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_combustion",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_field_generator",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_gas_turbine",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_steam_turbine",
          "confidence": 1
        }
      ],
      "evidence_omitted": 2,
      "semantic_evidence": [
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "gregtech_energy",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "We know that GregTech isn’t the easiest mod to get into — especially when it comes to the energy system. That’s why we’ll take our time in this chapter to explain as much as we can.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "Some things here might not make full sense until later in your progression, so don’t stress yourself. Just try to understand what you can for now, and feel free to come back to this chapter whenever you have questions.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "We’ve split this chapter into four categories, each one covering a topic related to the GregTech Energy System. We’ll provide as many examples as possible to help you understand how it all works.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "It's not as bad as you think",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "Welcome aboard",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "Let's do some explaining",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "Moving Energy in GregTech means understanding a few core mechanics.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "First, Energy Tiers. From LV to UHV, everything in GregTech — wires, machines, recipes — is tied to a tier. You’ll need the correct cable material to move energy. For example:",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "• Tin wire = LV",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "• Copper wire = MV",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "Second, you’ve got the Amperage mechanic. Think of 1 Amp (or 1A) as a packet of energy. Machines request energy \"packets\", which then get sent down the wire.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "gtceu:bus",
      "label": "Bus",
      "origin": "namespace_generated",
      "confidence": 0.55,
      "support": 25,
      "evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_input_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_output_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_input_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_output_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_input_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_output_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:luv_input_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:luv_output_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_input_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_output_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:me_input_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:me_output_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:me_stocking_input_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_input_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_output_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:steam_input_bus",
          "confidence": 1
        }
      ],
      "evidence_omitted": 9,
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_input_bus",
          "source": "runtime-items",
          "text": "Item Input for Multiblocks",
          "key": "runtime-tooltip",
          "label": "§5EV Input Bus",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_input_bus",
          "source": "runtime-items",
          "text": "Item Slots: 25",
          "key": "runtime-tooltip",
          "label": "§5EV Input Bus",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_output_bus",
          "source": "runtime-items",
          "text": "Item Output for Multiblocks",
          "key": "runtime-tooltip",
          "label": "§5EV Output Bus",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_output_bus",
          "source": "runtime-items",
          "text": "Item Slots: 25",
          "key": "runtime-tooltip",
          "label": "§5EV Output Bus",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_input_bus",
          "source": "runtime-items",
          "text": "Item Input for Multiblocks",
          "key": "runtime-tooltip",
          "label": "§6HV Input Bus",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_input_bus",
          "source": "runtime-items",
          "text": "Item Slots: 16",
          "key": "runtime-tooltip",
          "label": "§6HV Input Bus",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_output_bus",
          "source": "runtime-items",
          "text": "Item Output for Multiblocks",
          "key": "runtime-tooltip",
          "label": "§6HV Output Bus",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_output_bus",
          "source": "runtime-items",
          "text": "Item Slots: 16",
          "key": "runtime-tooltip",
          "label": "§6HV Output Bus",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_input_bus",
          "source": "runtime-items",
          "text": "Item Input for Multiblocks",
          "key": "runtime-tooltip",
          "label": "§9IV Input Bus",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_input_bus",
          "source": "runtime-items",
          "text": "Item Slots: 36",
          "key": "runtime-tooltip",
          "label": "§9IV Input Bus",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_output_bus",
          "source": "runtime-items",
          "text": "Item Output for Multiblocks",
          "key": "runtime-tooltip",
          "label": "§9IV Output Bus",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_output_bus",
          "source": "runtime-items",
          "text": "Item Slots: 36",
          "key": "runtime-tooltip",
          "label": "§9IV Output Bus",
          "item_ref_count": 1
        }
      ],
      "semantic_evidence_omitted": 48,
      "aliases": []
    },
    {
      "id": "tfc:power",
      "label": "Power",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 24,
      "evidence": [
        {
          "kind": "advancement",
          "id": "tfc:story/water_wheel",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "tfc:story/windmill",
          "confidence": 0.65
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/mechanical_power",
          "confidence": 0.7
        }
      ],
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "tfc:story/water_wheel",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/story/water_wheel.json",
          "text": "River Power",
          "key": "advancement-title:title",
          "label": "River Power",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "tfc:story/water_wheel",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/story/water_wheel.json",
          "text": "Make a water wheel",
          "key": "advancement-description:description",
          "label": "River Power",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "tfc:story/windmill",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/story/windmill.json",
          "text": "Wind Power",
          "key": "advancement-title:title",
          "label": "Wind Power",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "tfc:story/windmill",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/story/windmill.json",
          "text": "Make a windmill and add five blades to it",
          "key": "advancement-description:description",
          "label": "Wind Power",
          "item_ref_count": 1
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/mechanical_power",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/mechanical_power.json",
          "text": "Mechanical Power",
          "key": "guide-page:name",
          "label": "Mechanical Power",
          "item_ref_count": 12,
          "recipe_ref_count": 7,
          "count": 22
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/mechanical_power",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/mechanical_power.json",
          "text": "Mechanical power is the art of making things rotate or move, by harnessing the elemental power of either wind or water. Practically, many devices can be hooked up to mechanical power networks either to automate their movement, or provide power for other functionality",
          "key": "guide-page:pages.0.text",
          "label": "Mechanical Power",
          "item_ref_count": 12,
          "recipe_ref_count": 7,
          "count": 22
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/mechanical_power",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/mechanical_power.json",
          "text": "In order to get started with harnessing mechanical power, you will first need a Source of power. Windmills are a way of harnessing the wind. They can be built nearly everywhere with enough space. Water Wheels are a slightly stronger power source, as they harness the current found in Rivers",
          "key": "guide-page:pages.1.text",
          "label": "Mechanical Power",
          "item_ref_count": 12,
          "recipe_ref_count": 7,
          "count": 22
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/mechanical_power",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/mechanical_power.json",
          "text": "Windmills are a way of harnessing the power of the wind to rotate an Axle. They are large, and require a completely unobstructed area of 13 x 13 x 1 to be placed. In order to create one, you will first need an Axle, and then you will need one or more Windmill Blades",
          "key": "guide-page:pages.2.text",
          "label": "Mechanical Power",
          "item_ref_count": 12,
          "recipe_ref_count": 7,
          "count": 22
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/mechanical_power",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/mechanical_power.json",
          "text": "Windmill Blades can be crafted from cloth. A single windmill can have up to five blades, and the more blades, the faster it will rotate.",
          "key": "guide-page:pages.3.text",
          "label": "Mechanical Power",
          "item_ref_count": 12,
          "recipe_ref_count": 7,
          "count": 22
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/mechanical_power",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/mechanical_power.json",
          "text": "In order to create a windmill, first place an Axle in any horizontal orientation. Then, up to five Windmill Blades on the axle to create the windmill. It will slowly start spinning. The windmill may break if the axle is already connected to another source, or if there is not enough clear space.",
          "key": "guide-page:pages.4.text",
          "label": "Mechanical Power",
          "item_ref_count": 12,
          "recipe_ref_count": 7,
          "count": 22
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/mechanical_power",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/mechanical_power.json",
          "text": "Water Wheels are a way of harnessing the power of flowing water in Rivers in order to generate power. When placed optimally, they can rotate at some of the fastest possible speeds available. A water wheel requires a 5 x 5 x 1 area of space to be placed. Note that the water wheel may break if there are any obstructions in the area around it,",
          "key": "guide-page:pages.6.text",
          "label": "Mechanical Power",
          "item_ref_count": 12,
          "recipe_ref_count": 7,
          "count": 22
        },
        {
          "kind": "guide_page",
          "id": "tfc:field_guide/en_us/entries/mechanics/mechanical_power",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/mechanics/mechanical_power.json",
          "text": "Water Wheels",
          "key": "guide-page:pages.6.title",
          "label": "Mechanical Power",
          "item_ref_count": 12,
          "recipe_ref_count": 7,
          "count": 22
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "vintageimprovements:fluid",
      "label": "Fluid",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 24,
      "evidence": [
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/centrifuge",
          "confidence": 0.7
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/vacuum_chamber_secondary",
          "confidence": 0.7
        }
      ],
      "semantic_evidence": [
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/centrifuge",
          "source": "file:minecraft/lang-overlays",
          "text": "Processing Items & Fluids in the Centrifuge",
          "key": "lang-ponder:vintageimprovements.ponder.centrifuge.header",
          "label": "Processing Items & Fluids in the Centrifuge",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/centrifuge",
          "source": "file:minecraft/lang-overlays",
          "text": "The Centrifuge can process a variety of items and fluids",
          "key": "lang-ponder:vintageimprovements.ponder.centrifuge.text_1",
          "label": "Processing Items & Fluids in the Centrifuge",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/centrifuge",
          "source": "file:minecraft/lang-overlays",
          "text": "Before use you must install 4 Basins on the Centrifuge",
          "key": "lang-ponder:vintageimprovements.ponder.centrifuge.text_2",
          "label": "Processing Items & Fluids in the Centrifuge",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/centrifuge",
          "source": "file:minecraft/lang-overlays",
          "text": "It can be powered from the top or bottom using shafts",
          "key": "lang-ponder:vintageimprovements.ponder.centrifuge.text_3",
          "label": "Processing Items & Fluids in the Centrifuge",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/centrifuge",
          "source": "file:minecraft/lang-overlays",
          "text": "Items and Fluids can only be inserted when the Centrifuge is stopped",
          "key": "lang-ponder:vintageimprovements.ponder.centrifuge.text_4",
          "label": "Processing Items & Fluids in the Centrifuge",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/centrifuge",
          "source": "file:minecraft/lang-overlays",
          "text": "The result can be extracted via right-click or automatic extraction...",
          "key": "lang-ponder:vintageimprovements.ponder.centrifuge.text_5",
          "label": "Processing Items & Fluids in the Centrifuge",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/centrifuge",
          "source": "file:minecraft/lang-overlays",
          "text": "...but only when the Centrifuge is stopped",
          "key": "lang-ponder:vintageimprovements.ponder.centrifuge.text_6",
          "label": "Processing Items & Fluids in the Centrifuge",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/centrifuge",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!assets/*/lang/en_us.json",
          "text": "Processing Items & Fluids in the Centrifuge",
          "key": "lang-ponder:vintageimprovements.ponder.centrifuge.header",
          "label": "Processing Items & Fluids in the Centrifuge",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/centrifuge",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!assets/*/lang/en_us.json",
          "text": "The Centrifuge can process a variety of items and fluids",
          "key": "lang-ponder:vintageimprovements.ponder.centrifuge.text_1",
          "label": "Processing Items & Fluids in the Centrifuge",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/centrifuge",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!assets/*/lang/en_us.json",
          "text": "Before use you must install 4 Basins on the Centrifuge",
          "key": "lang-ponder:vintageimprovements.ponder.centrifuge.text_2",
          "label": "Processing Items & Fluids in the Centrifuge",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/centrifuge",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!assets/*/lang/en_us.json",
          "text": "It can be powered from the top or bottom using shafts",
          "key": "lang-ponder:vintageimprovements.ponder.centrifuge.text_3",
          "label": "Processing Items & Fluids in the Centrifuge",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/centrifuge",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!assets/*/lang/en_us.json",
          "text": "Items and Fluids can only be inserted when the Centrifuge is stopped",
          "key": "lang-ponder:vintageimprovements.ponder.centrifuge.text_4",
          "label": "Processing Items & Fluids in the Centrifuge",
          "count": 7
        }
      ],
      "semantic_evidence_omitted": 12,
      "aliases": []
    },
    {
      "id": "create_connected:fan",
      "label": "Fan",
      "origin": "namespace_generated",
      "confidence": 0.65,
      "support": 24,
      "evidence": [
        {
          "kind": "advancement",
          "id": "create_connected:recipes/crafting/kinetics/empty_fan_catalyst",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create_connected:recipes/crafting/kinetics/empty_fan_catalyst_from_blasting",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create_connected:recipes/crafting/kinetics/empty_fan_catalyst_from_ending_dragon_head",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create_connected:recipes/crafting/kinetics/empty_fan_catalyst_from_ending_dragons_breath",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create_connected:recipes/crafting/kinetics/empty_fan_catalyst_from_enriched",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create_connected:recipes/crafting/kinetics/empty_fan_catalyst_from_freezing",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create_connected:recipes/crafting/kinetics/empty_fan_catalyst_from_haunting",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create_connected:recipes/crafting/kinetics/empty_fan_catalyst_from_sanding",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create_connected:recipes/crafting/kinetics/empty_fan_catalyst_from_seething",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create_connected:recipes/crafting/kinetics/empty_fan_catalyst_from_smoking",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create_connected:recipes/crafting/kinetics/empty_fan_catalyst_from_splashing",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create_connected:recipes/crafting/kinetics/empty_fan_catalyst_from_withering",
          "confidence": 0.65
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:empty_fan_catalyst",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:fan_blasting_catalyst",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:fan_ending_catalyst_dragon_head",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:fan_ending_catalyst_dragons_breath",
          "confidence": 1
        }
      ],
      "evidence_omitted": 8,
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "create:brass_block",
          "source": "runtime-items",
          "text": "ZnCu₃",
          "key": "runtime-tooltip",
          "label": "Block of Brass",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:brass_block",
          "source": "runtime-items",
          "text": "Melts into 1296 mB of Brass (at Orange)",
          "key": "runtime-tooltip",
          "label": "Block of Brass",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:empty_fan_catalyst",
          "source": "runtime-items",
          "text": "_Insert_ material to catalyze _fan processing_.",
          "key": "lang:block.create_connected.empty_fan_catalyst.tooltip.summary",
          "label": "Empty Fan Catalyst",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:fan_blasting_catalyst",
          "source": "runtime-items",
          "text": "Dedicated _bulk blasting_ device. Content of the catalyst does not interact with the environment.",
          "key": "lang:block.create_connected.fan_blasting_catalyst.tooltip.summary",
          "label": "Fan Blasting Catalyst",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:fan_ending_catalyst_dragon_head",
          "source": "runtime-items",
          "text": "Dedicated _bulk ending_ device for compatible mods. Content of the catalyst does not interact with the environment.",
          "key": "lang:block.create_connected.fan_ending_catalyst_dragon_head.tooltip.summary",
          "label": "Fan Ending Catalyst with Dragon Head",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:fan_ending_catalyst_dragons_breath",
          "source": "runtime-items",
          "text": "Dedicated _bulk ending_ device for compatible mods. Content of the catalyst does not interact with the environment.",
          "key": "lang:block.create_connected.fan_ending_catalyst_dragons_breath.tooltip.summary",
          "label": "Fan Ending Catalyst with Dragon's Breath",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:fan_enriched_catalyst",
          "source": "runtime-items",
          "text": "Dedicated _bulk enriched_ device for compatible mods. Content of the catalyst does not interact with the environment.",
          "key": "lang:block.create_connected.fan_enriched_catalyst.tooltip.summary",
          "label": "Fan Enriched Catalyst",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:fan_freezing_catalyst",
          "source": "runtime-items",
          "text": "Dedicated _bulk freezing_ device for compatible mods. Content of the catalyst does not interact with the environment.",
          "key": "lang:block.create_connected.fan_freezing_catalyst.tooltip.summary",
          "label": "Fan Freezing Catalyst",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:fan_haunting_catalyst",
          "source": "runtime-items",
          "text": "Dedicated _bulk haunting_ device. Content of the catalyst does not interact with the environment.",
          "key": "lang:block.create_connected.fan_haunting_catalyst.tooltip.summary",
          "label": "Fan Haunting Catalyst",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:fan_sanding_catalyst",
          "source": "runtime-items",
          "text": "Dedicated _bulk sanding_ device for compatible mods. Content of the catalyst does not interact with the environment.",
          "key": "lang:block.create_connected.fan_sanding_catalyst.tooltip.summary",
          "label": "Fan Sanding Catalyst",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:fan_seething_catalyst",
          "source": "runtime-items",
          "text": "Dedicated _bulk superheating_ device for compatible mods. Content of the catalyst does not interact with the environment.",
          "key": "lang:block.create_connected.fan_seething_catalyst.tooltip.summary",
          "label": "Fan Seething Catalyst",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:fan_smoking_catalyst",
          "source": "runtime-items",
          "text": "Dedicated _bulk smoking_ device. Content of the catalyst does not interact with the environment.",
          "key": "lang:block.create_connected.fan_smoking_catalyst.tooltip.summary",
          "label": "Fan Smoking Catalyst",
          "item_ref_count": 1
        }
      ],
      "semantic_evidence_omitted": 2,
      "aliases": []
    },
    {
      "id": "framedblocks:rail",
      "label": "Rail",
      "origin": "namespace_generated",
      "confidence": 0.65,
      "support": 24,
      "evidence": [
        {
          "kind": "advancement",
          "id": "framedblocks:recipes/building_blocks/framed_activator_rail_slope",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "framedblocks:recipes/building_blocks/framed_detector_rail_slope",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "framedblocks:recipes/building_blocks/framed_fancy_activator_rail",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "framedblocks:recipes/building_blocks/framed_fancy_activator_rail_slope",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "framedblocks:recipes/building_blocks/framed_fancy_detector_rail",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "framedblocks:recipes/building_blocks/framed_fancy_detector_rail_slope",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "framedblocks:recipes/building_blocks/framed_fancy_powered_rail",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "framedblocks:recipes/building_blocks/framed_fancy_powered_rail_slope",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "framedblocks:recipes/building_blocks/framed_fancy_rail",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "framedblocks:recipes/building_blocks/framed_fancy_rail_slope",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "framedblocks:recipes/building_blocks/framed_powered_rail_slope",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "framedblocks:recipes/building_blocks/framed_rail_slope",
          "confidence": 0.65
        },
        {
          "kind": "runtime_item",
          "id": "framedblocks:framed_activator_rail_slope",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "framedblocks:framed_detector_rail_slope",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "framedblocks:framed_fancy_activator_rail",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "framedblocks:framed_fancy_activator_rail_slope",
          "confidence": 1
        }
      ],
      "evidence_omitted": 8,
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "gtceu:pump",
      "label": "Pump",
      "origin": "namespace_generated",
      "confidence": 0.75,
      "support": 23,
      "evidence": [
        {
          "kind": "item_tag",
          "id": "gtceu:electric_pumps",
          "confidence": 0.75
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_electric_pump",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_pump",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_electric_pump",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_pump",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_electric_pump",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:luv_electric_pump",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_electric_pump",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_pump",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_electric_pump",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_pump",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:primitive_pump",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:pump_deck",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:pump_hatch",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:uv_electric_pump",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:zpm_electric_pump",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_electric_pump",
          "source": "runtime-items",
          "text": "Transfers Fluids at specific rates as Cover.",
          "key": "runtime-tooltip",
          "label": "EV Electric Pump",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_electric_pump",
          "source": "runtime-items",
          "text": "Transfer Rate: 4,096 mB/t",
          "key": "runtime-tooltip",
          "label": "EV Electric Pump",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_electric_pump",
          "source": "runtime-items",
          "text": "Transfers Fluids at specific rates as Cover.",
          "key": "runtime-tooltip",
          "label": "HV Electric Pump",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_electric_pump",
          "source": "runtime-items",
          "text": "Transfer Rate: 1,024 mB/t",
          "key": "runtime-tooltip",
          "label": "HV Electric Pump",
          "item_ref_count": 1
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "iv__insane_voltage",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "This Superconductor will be required for progression during LuV.",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "IV Superconductors",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "If you already made and centrifuged Rare Earth, that's great! If you haven't, you can obtain it as a Byproduct from various ores, or from centrifuging Monazite.",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "To extract the Rare Earths, you should use a Centrifuge as high a tier as possible, due to the increased chance of bonus outputs.",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "{@pagebreak}",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "The elements you can obtain are:",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "- Lanthanum is used in Fusion - you will eventually want a lot of it!",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "gtceu:turbine",
      "label": "Turbine",
      "origin": "namespace_generated",
      "confidence": 0.55,
      "support": 23,
      "evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:gas_large_turbine",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hsss_turbine_blade",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_gas_turbine",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_steam_turbine",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_gas_turbine",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_steam_turbine",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_gas_turbine",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_steam_turbine",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:naquadah_alloy_turbine_blade",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:neutronium_turbine_blade",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:osmiridium_turbine_blade",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:plasma_large_turbine",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:rocket_alloy_t1_turbine_blade",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:rocket_alloy_t2_turbine_blade",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:stainless_steel_turbine_casing",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:steam_large_turbine",
          "confidence": 1
        }
      ],
      "evidence_omitted": 7,
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:gas_large_turbine",
          "source": "runtime-items",
          "text": "Base Production: 4096 EU/t",
          "key": "runtime-tooltip",
          "label": "Large Gas Turbine",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:gas_large_turbine",
          "source": "runtime-items",
          "text": "Each Rotor Holder above EV adds 10% efficiency and multiplies EU/t by 2.",
          "key": "runtime-tooltip",
          "label": "Large Gas Turbine",
          "item_ref_count": 1
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "gregtech_energy",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "We know that GregTech isn’t the easiest mod to get into — especially when it comes to the energy system. That’s why we’ll take our time in this chapter to explain as much as we can.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "Some things here might not make full sense until later in your progression, so don’t stress yourself. Just try to understand what you can for now, and feel free to come back to this chapter whenever you have questions.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "We’ve split this chapter into four categories, each one covering a topic related to the GregTech Energy System. We’ll provide as many examples as possible to help you understand how it all works.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "It's not as bad as you think",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "Welcome aboard",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "Let's do some explaining",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "Moving Energy in GregTech means understanding a few core mechanics.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "First, Energy Tiers. From LV to UHV, everything in GregTech — wires, machines, recipes — is tied to a tier. You’ll need the correct cable material to move energy. For example:",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "• Tin wire = LV",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "gtceu:cover",
      "label": "Cover",
      "origin": "namespace_generated",
      "confidence": 0.55,
      "support": 23,
      "evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:activity_detector_cover",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_activity_detector_cover",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_energy_detector_cover",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_fluid_detector_cover",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_fluid_voiding_cover",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_item_detector_cover",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_item_voiding_cover",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:computer_monitor_cover",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ender_fluid_link_cover",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ender_item_link_cover",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ender_redstone_link_cover",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:energy_detector_cover",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:facade_cover",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:fluid_detector_cover",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:fluid_voiding_cover",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:infinite_water_cover",
          "confidence": 1
        }
      ],
      "evidence_omitted": 7,
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:activity_detector_cover",
          "source": "runtime-items",
          "text": "Gives out Activity Status as Redstone as Cover.",
          "key": "runtime-tooltip",
          "label": "Activity Detector",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:activity_detector_cover",
          "source": "runtime-items",
          "text": "Gives out Activity Status as Redstone as Cover.",
          "key": "lang:item.gtceu.activity_detector_cover.tooltip",
          "label": "Activity Detector",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_activity_detector_cover",
          "source": "runtime-items",
          "text": "Gives out Machine Progress as Redstone as Cover.",
          "key": "runtime-tooltip",
          "label": "Advanced Activity Detector",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_activity_detector_cover",
          "source": "runtime-items",
          "text": "Gives out Machine Progress as Redstone as Cover.",
          "key": "lang:item.gtceu.advanced_activity_detector_cover.tooltip",
          "label": "Advanced Activity Detector",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_energy_detector_cover",
          "source": "runtime-items",
          "text": "Gives RS-Latch controlled Energy Status as Redstone as Cover.",
          "key": "runtime-tooltip",
          "label": "Advanced Energy Detector",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_energy_detector_cover",
          "source": "runtime-items",
          "text": "Gives RS-Latch controlled Energy Status as Redstone as Cover.",
          "key": "lang:item.gtceu.advanced_energy_detector_cover.tooltip",
          "label": "Advanced Energy Detector",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_fluid_detector_cover",
          "source": "runtime-items",
          "text": "Gives RS-Latch controlled Fluid Storage Status as Redstone as Cover.",
          "key": "runtime-tooltip",
          "label": "Advanced Fluid Detector",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_fluid_detector_cover",
          "source": "runtime-items",
          "text": "Gives RS-Latch controlled Fluid Storage Status as Redstone as Cover.",
          "key": "lang:item.gtceu.advanced_fluid_detector_cover.tooltip",
          "label": "Advanced Fluid Detector",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_fluid_voiding_cover",
          "source": "runtime-items",
          "text": "Voids Fluids with amount control as Cover.",
          "key": "runtime-tooltip",
          "label": "Advanced Fluid Voiding Cover",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_fluid_voiding_cover",
          "source": "runtime-items",
          "text": "Activate with Soft Mallet after placement.",
          "key": "runtime-tooltip",
          "label": "Advanced Fluid Voiding Cover",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_fluid_voiding_cover",
          "source": "runtime-items",
          "text": "Voids Fluids with amount control as Cover.",
          "key": "lang:item.gtceu.advanced_fluid_voiding_cover.tooltip.0",
          "label": "Advanced Fluid Voiding Cover",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_fluid_voiding_cover",
          "source": "runtime-items",
          "text": "Activate with Soft Mallet after placement.",
          "key": "lang:item.gtceu.advanced_fluid_voiding_cover.tooltip.1",
          "label": "Advanced Fluid Voiding Cover",
          "item_ref_count": 1
        }
      ],
      "semantic_evidence_omitted": 33,
      "aliases": []
    },
    {
      "id": "create_connected:battery",
      "label": "Battery",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 22,
      "evidence": [
        {
          "kind": "advancement",
          "id": "create_connected:kinetic_battery",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create_connected:recipes/crafting/kinetics/kinetic_battery",
          "confidence": 0.65
        },
        {
          "kind": "guide_page",
          "id": "create_connected:ponder/kinetic_battery",
          "confidence": 0.7
        },
        {
          "kind": "guide_page",
          "id": "create_connected:ponder/kinetic_battery_automation",
          "confidence": 0.7
        },
        {
          "kind": "guide_page",
          "id": "create_connected:ponder/kinetic_battery_chaining",
          "confidence": 0.7
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:charged_kinetic_battery",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:kinetic_battery",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "create_connected:kinetic_battery",
          "source": "jar:create_connected-1.1.13-mc1.20.1-all.jar!data/create_connected/advancements/kinetic_battery.json",
          "text": "Fully Charged",
          "key": "advancement-title:title",
          "label": "Fully Charged",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create_connected:kinetic_battery",
          "source": "jar:create_connected-1.1.13-mc1.20.1-all.jar!data/create_connected/advancements/kinetic_battery.json",
          "text": "Charge a Kinetic Battery to full",
          "key": "advancement-description:description",
          "label": "Fully Charged",
          "item_ref_count": 1
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "questssteam_age",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "The Millstone is an automatic version of the Quern. You can throw whatever you'd like into the top, and then right-click to take your crushed items back out. It's pretty slow if you connect it directly to your Animal Crank, but you can use gear ratios to increase its speed.",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "You will only receive the output in the first slot. The other slots are part of a GregTech mechanic that won't be relevant until much later (HV).",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "{@pagebreak}",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "Lore: The millstone can't be used to process grains into flour because Create doesn't understand TFC's food expiry system, which previously led to all sorts of bugs involving rotten items becoming fresh and vice versa. Until you're able to get the Food Processor in LV, you can still crush your grains via a Mortar in a crafting grid.",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "No more querning",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "Automatic Ore Processing",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "The Animal Crank is your first accessible source of mechanical power. To use it, place the crank on the center of a 7x7 cleared area and leash an animal to it. Different animals will provide different amounts of power, while the blocks underneath will increase the speed of the output power. The area of multiple cranks can overlap.",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "You may need to hold a second lead to attach an animal.",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "Small Animals (16 SU):",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "ae2wtlib:terminal",
      "label": "Terminal",
      "origin": "namespace_generated",
      "confidence": 0.65,
      "support": 21,
      "evidence": [
        {
          "kind": "advancement",
          "id": "ae2wtlib:recipes/wireless_pattern_access_terminal",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ae2wtlib:recipes/wireless_pattern_encoding_terminal",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ae2wtlib:recipes/wireless_universal_terminal/ae",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ae2wtlib:recipes/wireless_universal_terminal/ca",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ae2wtlib:recipes/wireless_universal_terminal/ce",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ae2wtlib:recipes/wireless_universal_terminal/upgrade_crafting",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ae2wtlib:recipes/wireless_universal_terminal/upgrade_pattern_access",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ae2wtlib:recipes/wireless_universal_terminal/upgrade_pattern_encoding",
          "confidence": 0.65
        },
        {
          "kind": "mod_metadata",
          "id": "ae2wtlib",
          "confidence": 0.6
        },
        {
          "kind": "runtime_item",
          "id": "ae2wtlib:wireless_pattern_access_terminal",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2wtlib:wireless_pattern_encoding_terminal",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2wtlib:wireless_universal_terminal",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "ae2:wireless_terminal",
          "source": "runtime-items",
          "text": "Stored Energy: 0/1.6M AE (0%)",
          "key": "runtime-tooltip",
          "label": "Wireless Terminal",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2:wireless_terminal",
          "source": "runtime-items",
          "text": "Unlinked",
          "key": "runtime-tooltip",
          "label": "Wireless Terminal",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2wtlib:wireless_pattern_access_terminal",
          "source": "runtime-items",
          "text": "Slot: Curio",
          "key": "runtime-tooltip",
          "label": "Wireless Pattern Access Terminal",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2wtlib:wireless_pattern_access_terminal",
          "source": "runtime-items",
          "text": "Stored Energy: 0/1.6M AE (0%)",
          "key": "runtime-tooltip",
          "label": "Wireless Pattern Access Terminal",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2wtlib:wireless_pattern_access_terminal",
          "source": "runtime-items",
          "text": "Unlinked",
          "key": "runtime-tooltip",
          "label": "Wireless Pattern Access Terminal",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2wtlib:wireless_pattern_encoding_terminal",
          "source": "runtime-items",
          "text": "Slot: Curio",
          "key": "runtime-tooltip",
          "label": "Wireless Pattern Encoding Terminal",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2wtlib:wireless_pattern_encoding_terminal",
          "source": "runtime-items",
          "text": "Stored Energy: 0/1.6M AE (0%)",
          "key": "runtime-tooltip",
          "label": "Wireless Pattern Encoding Terminal",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "ae2wtlib:wireless_pattern_encoding_terminal",
          "source": "runtime-items",
          "text": "Unlinked",
          "key": "runtime-tooltip",
          "label": "Wireless Pattern Encoding Terminal",
          "item_ref_count": 1
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "ev__extreme_voltage",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "You’ve built new chemistry lines, manufactured advanced alloys, and even launched a Rocket to reach the Moon. We hope you enjoyed the ride, because now things get even more complex, with powerful new machines and demanding processes ahead.",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "In the EV Chapter, your main objective will be to craft your very first IV and LuV Circuits.",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "{@pagebreak}",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": [
        "ae2",
        "ae2wtlib",
        "curios"
      ]
    },
    {
      "id": "tfc:fluid",
      "label": "Fluid",
      "origin": "namespace_generated",
      "confidence": 0.75,
      "support": 20,
      "evidence": [
        {
          "kind": "item_tag",
          "id": "tfc:fluid_item_ingredient_empty_containers",
          "confidence": 0.75
        },
        {
          "kind": "runtime_item",
          "id": "tfc:ceramic/jug",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:hematitic_glass_bottle",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:metal/bucket/blue_steel",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:metal/bucket/red_steel",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:olivine_glass_bottle",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:silica_glass_bottle",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:volcanic_glass_bottle",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:wooden_bucket",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
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
        }
      ],
      "semantic_evidence_omitted": 39,
      "aliases": []
    },
    {
      "id": "create:shaft",
      "label": "Shaft",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 20,
      "evidence": [
        {
          "kind": "advancement",
          "id": "create:recipes/misc/crafting/kinetics/gantry_shaft",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create:recipes/misc/crafting/kinetics/shaft",
          "confidence": 0.65
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/gantry_shaft",
          "confidence": 0.7
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/shaft",
          "confidence": 0.7
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/shaft_casing",
          "confidence": 0.7
        },
        {
          "kind": "runtime_item",
          "id": "create:andesite_encased_shaft",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:brass_encased_shaft",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:gantry_shaft",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:shaft",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "create:andesite_alloy",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/andesite_alloy.json",
          "text": "Sturdier Rocks",
          "key": "advancement-title:title",
          "label": "Sturdier Rocks",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create:andesite_alloy",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/andesite_alloy.json",
          "text": "Obtain some Andesite Alloy, Create's most important resource",
          "key": "advancement-description:description",
          "label": "Sturdier Rocks",
          "item_ref_count": 1
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/gantry_shaft",
          "source": "file:minecraft/lang-overlays",
          "text": "Using Gantry Shafts",
          "key": "lang-ponder:create.ponder.gantry_shaft.header",
          "label": "Using Gantry Shafts",
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/gantry_shaft",
          "source": "file:minecraft/lang-overlays",
          "text": "Gantry Shafts form the basis of a gantry setup. Attached Carriages will move along them.",
          "key": "lang-ponder:create.ponder.gantry_shaft.text_1",
          "label": "Using Gantry Shafts",
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/gantry_shaft",
          "source": "file:minecraft/lang-overlays",
          "text": "Gantry setups can move attached Blocks.",
          "key": "lang-ponder:create.ponder.gantry_shaft.text_2",
          "label": "Using Gantry Shafts",
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/gantry_shaft",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Using Gantry Shafts",
          "key": "lang-ponder:create.ponder.gantry_shaft.header",
          "label": "Using Gantry Shafts",
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/gantry_shaft",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Gantry Shafts form the basis of a gantry setup. Attached Carriages will move along them.",
          "key": "lang-ponder:create.ponder.gantry_shaft.text_1",
          "label": "Using Gantry Shafts",
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/gantry_shaft",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Gantry setups can move attached Blocks.",
          "key": "lang-ponder:create.ponder.gantry_shaft.text_2",
          "label": "Using Gantry Shafts",
          "count": 3
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/shaft",
          "source": "file:minecraft/lang-overlays",
          "text": "Relaying rotational force using Shafts",
          "key": "lang-ponder:create.ponder.shaft.header",
          "label": "Relaying rotational force using Shafts",
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/shaft",
          "source": "file:minecraft/lang-overlays",
          "text": "Shafts will relay rotation in a straight line.",
          "key": "lang-ponder:create.ponder.shaft.text_1",
          "label": "Relaying rotational force using Shafts",
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/shaft",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Relaying rotational force using Shafts",
          "key": "lang-ponder:create.ponder.shaft.header",
          "label": "Relaying rotational force using Shafts",
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/shaft",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Shafts will relay rotation in a straight line.",
          "key": "lang-ponder:create.ponder.shaft.text_1",
          "label": "Relaying rotational force using Shafts",
          "count": 2
        }
      ],
      "semantic_evidence_omitted": 4,
      "aliases": []
    },
    {
      "id": "create_factory_logistics:package",
      "label": "Package",
      "origin": "namespace_generated",
      "confidence": 0.6,
      "support": 20,
      "evidence": [
        {
          "kind": "mod_metadata",
          "id": "create_factory_logistics",
          "confidence": 0.6
        },
        {
          "kind": "runtime_item",
          "id": "create_factory_logistics:composite_package",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create_factory_logistics:copper_jar_package_8x8",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "mod_metadata",
          "id": "create_factory_logistics",
          "source": "mods-folder-scan",
          "text": "Ever wondered why there are logistics packages, but no logistics jars? Let's fix this!",
          "key": "mod-description:mod.create_factory_logistics.description",
          "label": "Create Factory Logistics",
          "count": 18
        }
      ],
      "aliases": [
        "create",
        "create_factory_logistics",
        "minecraft"
      ]
    },
    {
      "id": "geckolib:engine",
      "label": "Engine",
      "origin": "namespace_generated",
      "confidence": 0.6,
      "support": 20,
      "evidence": [
        {
          "kind": "mod_metadata",
          "id": "geckolib",
          "confidence": 0.6
        }
      ],
      "semantic_evidence": [
        {
          "kind": "mod_metadata",
          "id": "geckolib",
          "source": "mods-folder-scan",
          "text": "GeckoLib is an animation engine for Minecraft Mods, with support for complex 3D keyframe-based animations, 30+ easings, concurrent animation support, sound and particle keyframes, event keyframes, and more.",
          "key": "mod-description:mod.geckolib.description",
          "label": "GeckoLib 4",
          "count": 20
        }
      ],
      "aliases": [
        "geckolib"
      ]
    },
    {
      "id": "gtceu:assembler",
      "label": "Assembler",
      "origin": "namespace_generated",
      "confidence": 0.55,
      "support": 19,
      "evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_assembler",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_circuit_assembler",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_assembler",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_circuit_assembler",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_assembler",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_circuit_assembler",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:large_assembler",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:large_circuit_assembler",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:large_scale_assembler_casing",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:luv_assembler",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:luv_circuit_assembler",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_assembler",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_circuit_assembler",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_assembler",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_circuit_assembler",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:uv_assembler",
          "confidence": 1
        }
      ],
      "evidence_omitted": 3,
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_assembler",
          "source": "runtime-items",
          "text": "Avengers, Assemble!",
          "key": "runtime-tooltip",
          "label": "§5Advanced Assembler III§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_assembler",
          "source": "runtime-items",
          "text": "Voltage IN: 2,048 EU/t (EV)",
          "key": "runtime-tooltip",
          "label": "§5Advanced Assembler III§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_assembler",
          "source": "runtime-items",
          "text": "Energy Capacity: 131,072 EU",
          "key": "runtime-tooltip",
          "label": "§5Advanced Assembler III§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_assembler",
          "source": "runtime-items",
          "text": "Fluid Capacity: 16,000 mB",
          "key": "runtime-tooltip",
          "label": "§5Advanced Assembler III§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_circuit_assembler",
          "source": "runtime-items",
          "text": "Pick-n-Place all over the place",
          "key": "runtime-tooltip",
          "label": "§5Advanced Circuit Assembler III§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_circuit_assembler",
          "source": "runtime-items",
          "text": "Voltage IN: 2,048 EU/t (EV)",
          "key": "runtime-tooltip",
          "label": "§5Advanced Circuit Assembler III§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_circuit_assembler",
          "source": "runtime-items",
          "text": "Energy Capacity: 131,072 EU",
          "key": "runtime-tooltip",
          "label": "§5Advanced Circuit Assembler III§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_circuit_assembler",
          "source": "runtime-items",
          "text": "Fluid Capacity: 16,000 mB",
          "key": "runtime-tooltip",
          "label": "§5Advanced Circuit Assembler III§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_assembler",
          "source": "runtime-items",
          "text": "Avengers, Assemble!",
          "key": "runtime-tooltip",
          "label": "§6Advanced Assembler II§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_assembler",
          "source": "runtime-items",
          "text": "Voltage IN: 512 EU/t (HV)",
          "key": "runtime-tooltip",
          "label": "§6Advanced Assembler II§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_assembler",
          "source": "runtime-items",
          "text": "Energy Capacity: 32,768 EU",
          "key": "runtime-tooltip",
          "label": "§6Advanced Assembler II§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_assembler",
          "source": "runtime-items",
          "text": "Fluid Capacity: 16,000 mB",
          "key": "runtime-tooltip",
          "label": "§6Advanced Assembler II§r",
          "item_ref_count": 1
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "ad_astra:oxygen",
      "label": "Oxygen",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 18,
      "evidence": [
        {
          "kind": "advancement",
          "id": "ad_astra:oxygen_distributor",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:recipes/misc/oxygen_distributor",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:recipes/misc/oxygen_gear",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:recipes/misc/oxygen_loader",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:recipes/misc/oxygen_sensor",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:recipes/oxygen_loading/oxygen_loading/oxygen_from_oxygen_loading_oxygen",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "ad_astra:recipes/oxygen_loading/oxygen_loading/oxygen_from_oxygen_loading_water",
          "confidence": 0.65
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_first_launch/oxygen",
          "confidence": 0.7
        },
        {
          "kind": "guide_page",
          "id": "ad_astra:astrodux/en_us/entries/the_moon/oxygen_distributor",
          "confidence": 0.7
        },
        {
          "kind": "runtime_item",
          "id": "ad_astra:oxygen_bucket",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ad_astra:oxygen_distributor",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ad_astra:oxygen_gear",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ad_astra:oxygen_loader",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "ad_astra:oxygen_sensor",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "ad_astra:oxygen_distributor",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!data/ad_astra/advancements/oxygen_distributor.json",
          "text": "Take a Deep Breath",
          "key": "advancement-title:title",
          "label": "Take a Deep Breath",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "ad_astra:oxygen_distributor",
          "source": "jar:ad_astra-forge-1.20.1-1.15.20.jar!data/ad_astra/advancements/oxygen_distributor.json",
          "text": "Construct an Oxygen Distributor, allowing you to create livable environments",
          "key": "advancement-description:description",
          "label": "Take a Deep Breath",
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
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "greate:shaft",
      "label": "Shaft",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 18,
      "evidence": [
        {
          "kind": "guide_page",
          "id": "greate:ponder/shaft",
          "confidence": 0.7
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/shaft_casing",
          "confidence": 0.7
        },
        {
          "kind": "runtime_item",
          "id": "greate:aluminium_shaft",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "greate:andesite_alloy_shaft",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "greate:darmstadtium_shaft",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "greate:naquadah_alloy_shaft",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "greate:neutronium_shaft",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "greate:rhodium_plated_palladium_shaft",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "greate:stainless_steel_shaft",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "greate:steel_shaft",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "greate:titanium_shaft",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "greate:tungsten_steel_shaft",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "guide_page",
          "id": "greate:ponder/shaft",
          "source": "file:minecraft/lang-overlays",
          "text": "Relaying rotational force using Shafts",
          "key": "lang-ponder:greate.ponder.shaft.header",
          "label": "Relaying rotational force using Shafts",
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/shaft",
          "source": "file:minecraft/lang-overlays",
          "text": "Shafts will relay rotation in a straight line.",
          "key": "lang-ponder:greate.ponder.shaft.text_1",
          "label": "Relaying rotational force using Shafts",
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/shaft",
          "source": "jar:greate-0.0.75.jar!assets/*/lang/en_us.json",
          "text": "Relaying rotational force using Shafts",
          "key": "lang-ponder:greate.ponder.shaft.header",
          "label": "Relaying rotational force using Shafts",
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/shaft",
          "source": "jar:greate-0.0.75.jar!assets/*/lang/en_us.json",
          "text": "Shafts will relay rotation in a straight line.",
          "key": "lang-ponder:greate.ponder.shaft.text_1",
          "label": "Relaying rotational force using Shafts",
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/shaft_casing",
          "source": "file:minecraft/lang-overlays",
          "text": "Encasing Shafts",
          "key": "lang-ponder:greate.ponder.shaft_casing.header",
          "label": "Encasing Shafts",
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/shaft_casing",
          "source": "file:minecraft/lang-overlays",
          "text": "Brass or Andesite Casing can be used to decorate Shafts",
          "key": "lang-ponder:greate.ponder.shaft_casing.text_1",
          "label": "Encasing Shafts",
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/shaft_casing",
          "source": "jar:greate-0.0.75.jar!assets/*/lang/en_us.json",
          "text": "Encasing Shafts",
          "key": "lang-ponder:greate.ponder.shaft_casing.header",
          "label": "Encasing Shafts",
          "count": 2
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/shaft_casing",
          "source": "jar:greate-0.0.75.jar!assets/*/lang/en_us.json",
          "text": "Brass or Andesite Casing can be used to decorate Shafts",
          "key": "lang-ponder:greate.ponder.shaft_casing.text_1",
          "label": "Encasing Shafts",
          "count": 2
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "lv__low_voltage",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "The Fluid Regulator is sort of the equivalent to a Robot Arm for Fluids.",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "It can transfer fluids, but has two useful modes:",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "- Supply Exact will transfer the amount of specified Fluid per tick if available. No more, no less.",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "gtceu:power",
      "label": "Power",
      "origin": "namespace_generated",
      "confidence": 0.55,
      "support": 18,
      "evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:advanced_power_thruster",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_power_unit",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_transformer_16a",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:high_power_casing",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_power_unit",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_transformer_16a",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_power_unit",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_transformer_16a",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:luv_transformer_16a",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_power_unit",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_transformer_16a",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_power_unit",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_transformer_16a",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:power_substation",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:power_thruster",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ulv_transformer_16a",
          "confidence": 1
        }
      ],
      "evidence_omitted": 2,
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_power_unit",
          "source": "runtime-items",
          "text": "0/6,400,000 EU - Tier EV",
          "key": "runtime-tooltip",
          "label": "EV Power Unit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_transformer_16a",
          "source": "runtime-items",
          "text": "Transforms Energy between voltage tiers",
          "key": "runtime-tooltip",
          "label": "§5Extreme Voltage§r Power Transformer",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_transformer_16a",
          "source": "runtime-items",
          "text": "Starts as Transform Down, use Screwdriver to change",
          "key": "runtime-tooltip",
          "label": "§5Extreme Voltage§r Power Transformer",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_transformer_16a",
          "source": "runtime-items",
          "text": "Transform Down: 16A 8,192 EU (IV) -> 64A 2,048 EU (EV)",
          "key": "runtime-tooltip",
          "label": "§5Extreme Voltage§r Power Transformer",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_transformer_16a",
          "source": "runtime-items",
          "text": "Transform Up: 64A 2,048 EU (EV) -> 16A 8,192 EU (IV)",
          "key": "runtime-tooltip",
          "label": "§5Extreme Voltage§r Power Transformer",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_power_unit",
          "source": "runtime-items",
          "text": "0/1,600,000 EU - Tier HV",
          "key": "runtime-tooltip",
          "label": "HV Power Unit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_transformer_16a",
          "source": "runtime-items",
          "text": "Transforms Energy between voltage tiers",
          "key": "runtime-tooltip",
          "label": "§6High Voltage§r Power Transformer",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_transformer_16a",
          "source": "runtime-items",
          "text": "Starts as Transform Down, use Screwdriver to change",
          "key": "runtime-tooltip",
          "label": "§6High Voltage§r Power Transformer",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_transformer_16a",
          "source": "runtime-items",
          "text": "Transform Down: 16A 2,048 EU (EV) -> 64A 512 EU (HV)",
          "key": "runtime-tooltip",
          "label": "§6High Voltage§r Power Transformer",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_transformer_16a",
          "source": "runtime-items",
          "text": "Transform Up: 64A 512 EU (HV) -> 16A 2,048 EU (EV)",
          "key": "runtime-tooltip",
          "label": "§6High Voltage§r Power Transformer",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_power_unit",
          "source": "runtime-items",
          "text": "0/25,600,000 EU - Tier IV",
          "key": "runtime-tooltip",
          "label": "IV Power Unit",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_transformer_16a",
          "source": "runtime-items",
          "text": "Transforms Energy between voltage tiers",
          "key": "runtime-tooltip",
          "label": "§9Insane Voltage§r Power Transformer",
          "item_ref_count": 1
        }
      ],
      "semantic_evidence_omitted": 35,
      "aliases": []
    },
    {
      "id": "create:network",
      "label": "Network",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 18,
      "evidence": [
        {
          "kind": "guide_page",
          "id": "create:ponder/stock_link",
          "confidence": 0.7
        }
      ],
      "semantic_evidence": [
        {
          "kind": "guide_page",
          "id": "create:ponder/stock_link",
          "source": "file:minecraft/lang-overlays",
          "text": "Logistics Networks and the Stock Link",
          "key": "lang-ponder:create.ponder.stock_link.header",
          "label": "Logistics Networks and the Stock Link",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/stock_link",
          "source": "file:minecraft/lang-overlays",
          "text": "When placed, Stock Links create a new stock network",
          "key": "lang-ponder:create.ponder.stock_link.text_1",
          "label": "Logistics Networks and the Stock Link",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/stock_link",
          "source": "file:minecraft/lang-overlays",
          "text": "Right-click an existing link before placing it to bind them",
          "key": "lang-ponder:create.ponder.stock_link.text_2",
          "label": "Logistics Networks and the Stock Link",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/stock_link",
          "source": "file:minecraft/lang-overlays",
          "text": "Stock-linked packagers make their inventory available to the network",
          "key": "lang-ponder:create.ponder.stock_link.text_3",
          "label": "Logistics Networks and the Stock Link",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/stock_link",
          "source": "file:minecraft/lang-overlays",
          "text": "Other components on the network can now find and request their items",
          "key": "lang-ponder:create.ponder.stock_link.text_4",
          "label": "Logistics Networks and the Stock Link",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/stock_link",
          "source": "file:minecraft/lang-overlays",
          "text": "On request, items from the inventories will be placed into packages",
          "key": "lang-ponder:create.ponder.stock_link.text_5",
          "label": "Logistics Networks and the Stock Link",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/stock_link",
          "source": "file:minecraft/lang-overlays",
          "text": "Stock Link signals have unlimited range, but packages require transportation",
          "key": "lang-ponder:create.ponder.stock_link.text_6",
          "label": "Logistics Networks and the Stock Link",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/stock_link",
          "source": "file:minecraft/lang-overlays",
          "text": "Full redstone power will stop a link from broadcasting",
          "key": "lang-ponder:create.ponder.stock_link.text_7",
          "label": "Logistics Networks and the Stock Link",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/stock_link",
          "source": "file:minecraft/lang-overlays",
          "text": "Analog power lowers the priority of a link, causing others to act first",
          "key": "lang-ponder:create.ponder.stock_link.text_8",
          "label": "Logistics Networks and the Stock Link",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/stock_link",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Logistics Networks and the Stock Link",
          "key": "lang-ponder:create.ponder.stock_link.header",
          "label": "Logistics Networks and the Stock Link",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/stock_link",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "When placed, Stock Links create a new stock network",
          "key": "lang-ponder:create.ponder.stock_link.text_1",
          "label": "Logistics Networks and the Stock Link",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/stock_link",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Right-click an existing link before placing it to bind them",
          "key": "lang-ponder:create.ponder.stock_link.text_2",
          "label": "Logistics Networks and the Stock Link",
          "count": 9
        }
      ],
      "semantic_evidence_omitted": 6,
      "aliases": []
    },
    {
      "id": "expatternprovider:bus",
      "label": "Bus",
      "origin": "namespace_generated",
      "confidence": 0.65,
      "support": 17,
      "evidence": [
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/mod_export_bus",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/mod_storage_bus",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/pre_bus",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/precise_storage_bus",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/tag_export_bus",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/tag_storage_bus",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/threshold_export_bus",
          "confidence": 0.65
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:ex_export_bus_part",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:ex_import_bus_part",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:io_bus_upgrade",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:mod_export_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:mod_storage_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:precise_export_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:precise_storage_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:tag_export_bus",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:tag_storage_bus",
          "confidence": 1
        }
      ],
      "evidence_omitted": 1,
      "semantic_evidence": [
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ore_processing",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ore_processing.snbt",
          "text": "ore_processing",
          "key": "quest-snbt",
          "label": "ore_processing",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ore_processing",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ore_processing.snbt",
          "text": "An important part of GregTech is its Ore Processing Mechanics, but it can get pretty complicated, so this chapter is here to help!",
          "key": "quest-snbt",
          "label": "ore_processing",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ore_processing",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ore_processing.snbt",
          "text": "To start with, you should be aware of the Ore Processing Diagram tab in EMI - Press the Show Uses key (default U) on any item related to ore processing and look for the tab with the vanilla Iron Ore icon. This diagram may look overwhelming at first, but we'll take it step by step.",
          "key": "quest-snbt",
          "label": "ore_processing",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ore_processing",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ore_processing.snbt",
          "text": "A major part of GregTech philosophy is that there's not just one way to solve a problem, but we've marked everything important with a star, so if you're completely lost, try following those!",
          "key": "quest-snbt",
          "label": "ore_processing",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ore_processing",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ore_processing.snbt",
          "text": "To Process, Ore Not To Process",
          "key": "quest-snbt",
          "label": "ore_processing",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ore_processing",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ore_processing.snbt",
          "text": "I agree to not just dump everything in a furnace",
          "key": "quest-snbt",
          "label": "ore_processing",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ore_processing",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ore_processing.snbt",
          "text": "Welcome to Ore Processing",
          "key": "quest-snbt",
          "label": "ore_processing",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ore_processing",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ore_processing.snbt",
          "text": "While looking at EMI, you've probably noticed that your Macerator or Millstone lists more outputs than what you're actually getting.",
          "key": "quest-snbt",
          "label": "ore_processing",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ore_processing",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ore_processing.snbt",
          "text": "GregTech doesn't communicate this very well, but these extra slots are only available in HV. Once you make it to that tier, the HV macerator becomes incredibly powerful, giving a huge amount of bonus byproducts from each ore.",
          "key": "quest-snbt",
          "label": "ore_processing",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ore_processing",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ore_processing.snbt",
          "text": "I can get even more from my ores!",
          "key": "quest-snbt",
          "label": "ore_processing",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ore_processing",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ore_processing.snbt",
          "text": "or(item(gtceu:hv_macerator)item(greate:stainless_steel_crushing_wheel))",
          "key": "quest-snbt",
          "label": "ore_processing",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ore_processing",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ore_processing.snbt",
          "text": "Either an HV Macerator or HS Crushing Wheels",
          "key": "quest-snbt",
          "label": "ore_processing",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "deafission:fission",
      "label": "Fission",
      "origin": "namespace_generated",
      "confidence": 0.6,
      "support": 17,
      "evidence": [
        {
          "kind": "mod_metadata",
          "id": "deafission",
          "confidence": 0.6
        },
        {
          "kind": "runtime_item",
          "id": "deafission:fission_reactor_mk1",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "deafission:fission_reactor_mk2",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "deafission:fission_reactor_smr1",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "mod_metadata",
          "id": "deafission",
          "source": "mods-folder-scan",
          "text": "A fission reactor for GTCEu-m",
          "key": "mod-description:mod.deafission.description",
          "label": "Dea's Fission",
          "count": 14
        },
        {
          "kind": "runtime_item",
          "id": "deafission:fission_reactor_mk1",
          "source": "runtime-items",
          "text": "The power of the atom",
          "key": "runtime-tooltip",
          "label": "Fission Reactor MK I",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "deafission:fission_reactor_mk1",
          "source": "runtime-items",
          "text": "Customizable Fission Reactor, add better components, process rods and materials into it and watch out for its temperature.",
          "key": "runtime-tooltip",
          "label": "Fission Reactor MK I",
          "item_ref_count": 1
        },
        {
          "kind": "kubejs_tooltip",
          "id": "kubejs:tooltips/30",
          "source": "file:minecraft/kubejs/client_scripts/tooltips.js",
          "text": "The power of the atom",
          "key": "kubejs-tooltip:tfg.tooltip.machine.fission_reactor_mk1_1",
          "label": "The power of the atom",
          "item_ref_count": 1
        },
        {
          "kind": "kubejs_tooltip",
          "id": "kubejs:tooltips/30",
          "source": "file:minecraft/kubejs/client_scripts/tooltips.js",
          "text": "Customizable Fission Reactor, add better components, process rods and materials into it and watch out for its temperature.",
          "key": "kubejs-tooltip:tfg.tooltip.machine.fission_reactor_mk1_2",
          "label": "The power of the atom",
          "item_ref_count": 1
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "ev__extreme_voltage",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "You’ve built new chemistry lines, manufactured advanced alloys, and even launched a Rocket to reach the Moon. We hope you enjoyed the ride, because now things get even more complex, with powerful new machines and demanding processes ahead.",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "In the EV Chapter, your main objective will be to craft your very first IV and LuV Circuits.",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "{@pagebreak}",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "Along the way, several key challenges await you:",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "• Build your first Alloy Blast Smelter, a faster version of the EBF, specialized for alloys. With it, you’ll be able to construct your first Large Machine: the Large Centrifuge, essential for the Nuclear Fission Line. It also lets you upgrade ore processing with a faster Thermal Centrifuge.",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "• Master our original Tungsten Line - looping all the required materials will test both your knowledge and your patience.",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": [
        "deafission"
      ]
    },
    {
      "id": "deafission:reactor",
      "label": "Reactor",
      "origin": "namespace_generated",
      "confidence": 0.6,
      "support": 17,
      "evidence": [
        {
          "kind": "mod_metadata",
          "id": "deafission",
          "confidence": 0.6
        },
        {
          "kind": "runtime_item",
          "id": "deafission:fission_reactor_mk1",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "deafission:fission_reactor_mk2",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "deafission:fission_reactor_smr1",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "mod_metadata",
          "id": "deafission",
          "source": "mods-folder-scan",
          "text": "A fission reactor for GTCEu-m",
          "key": "mod-description:mod.deafission.description",
          "label": "Dea's Fission",
          "count": 14
        },
        {
          "kind": "runtime_item",
          "id": "deafission:fission_reactor_mk1",
          "source": "runtime-items",
          "text": "The power of the atom",
          "key": "runtime-tooltip",
          "label": "Fission Reactor MK I",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "deafission:fission_reactor_mk1",
          "source": "runtime-items",
          "text": "Customizable Fission Reactor, add better components, process rods and materials into it and watch out for its temperature.",
          "key": "runtime-tooltip",
          "label": "Fission Reactor MK I",
          "item_ref_count": 1
        },
        {
          "kind": "kubejs_tooltip",
          "id": "kubejs:tooltips/30",
          "source": "file:minecraft/kubejs/client_scripts/tooltips.js",
          "text": "The power of the atom",
          "key": "kubejs-tooltip:tfg.tooltip.machine.fission_reactor_mk1_1",
          "label": "The power of the atom",
          "item_ref_count": 1
        },
        {
          "kind": "kubejs_tooltip",
          "id": "kubejs:tooltips/30",
          "source": "file:minecraft/kubejs/client_scripts/tooltips.js",
          "text": "Customizable Fission Reactor, add better components, process rods and materials into it and watch out for its temperature.",
          "key": "kubejs-tooltip:tfg.tooltip.machine.fission_reactor_mk1_2",
          "label": "The power of the atom",
          "item_ref_count": 1
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "ev__extreme_voltage",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "You’ve built new chemistry lines, manufactured advanced alloys, and even launched a Rocket to reach the Moon. We hope you enjoyed the ride, because now things get even more complex, with powerful new machines and demanding processes ahead.",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "In the EV Chapter, your main objective will be to craft your very first IV and LuV Circuits.",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "{@pagebreak}",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "Along the way, several key challenges await you:",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "• Build your first Alloy Blast Smelter, a faster version of the EBF, specialized for alloys. With it, you’ll be able to construct your first Large Machine: the Large Centrifuge, essential for the Nuclear Fission Line. It also lets you upgrade ore processing with a faster Thermal Centrifuge.",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/ev__extreme_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/ev__extreme_voltage.snbt",
          "text": "• Master our original Tungsten Line - looping all the required materials will test both your knowledge and your patience.",
          "key": "quest-snbt",
          "label": "ev__extreme_voltage",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": [
        "deafission"
      ]
    },
    {
      "id": "gtceu:tank",
      "label": "Tank",
      "origin": "namespace_generated",
      "confidence": 0.55,
      "support": 17,
      "evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:bronze_multiblock_tank",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:bronze_tank_valve",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:creative_tank",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_super_tank",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_super_tank",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_quantum_tank",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:luv_quantum_tank",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_super_tank",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_super_tank",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:steel_multiblock_tank",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:steel_tank_valve",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:uhv_quantum_tank",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ulv_super_tank",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:uv_quantum_tank",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:wooden_multiblock_tank",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:wooden_tank_valve",
          "confidence": 1
        }
      ],
      "evidence_omitted": 1,
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:bronze_multiblock_tank",
          "source": "runtime-items",
          "text": "Fill and drain through the controller or tank valves.",
          "key": "runtime-tooltip",
          "label": "Bronze Multiblock Tank",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:bronze_multiblock_tank",
          "source": "runtime-items",
          "text": "Fluid Capacity: 4000000 mB",
          "key": "runtime-tooltip",
          "label": "Bronze Multiblock Tank",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:bronze_multiblock_tank",
          "source": "runtime-items",
          "text": "Temperature Limit: 1,696 K",
          "key": "runtime-tooltip",
          "label": "Bronze Multiblock Tank",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:bronze_tank_valve",
          "source": "runtime-items",
          "text": "Use to fill and drain multiblock tanks. Auto outputs when facing down.",
          "key": "runtime-tooltip",
          "label": "Bronze Tank Valve",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:bronze_tank_valve",
          "source": "runtime-items",
          "text": "Multiblock Sharing Disabled",
          "key": "runtime-tooltip",
          "label": "Bronze Tank Valve",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:creative_tank",
          "source": "runtime-items",
          "text": "You just need Creative Mode to use this",
          "key": "runtime-tooltip",
          "label": "Creative Tank",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_super_tank",
          "source": "runtime-items",
          "text": "Safely contains hot, cold, and lighter-than-air items and fluids.",
          "key": "runtime-tooltip",
          "label": "Super Tank IV",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_super_tank",
          "source": "runtime-items",
          "text": "Compact place to store all your fluids",
          "key": "runtime-tooltip",
          "label": "Super Tank IV",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_super_tank",
          "source": "runtime-items",
          "text": "Fluid Capacity: 32,000,000 mB",
          "key": "runtime-tooltip",
          "label": "Super Tank IV",
          "item_ref_count": 1
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "gregtech_energy",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "We know that GregTech isn’t the easiest mod to get into — especially when it comes to the energy system. That’s why we’ll take our time in this chapter to explain as much as we can.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/gregtech_energy",
          "source": "file:minecraft/config/ftbquests/quests/chapters/gregtech_energy.snbt",
          "text": "Some things here might not make full sense until later in your progression, so don’t stress yourself. Just try to understand what you can for now, and feel free to come back to this chapter whenever you have questions.",
          "key": "quest-snbt",
          "label": "gregtech_energy",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "gtceu:conveyor",
      "label": "Conveyor",
      "origin": "namespace_generated",
      "confidence": 0.75,
      "support": 16,
      "evidence": [
        {
          "kind": "item_tag",
          "id": "gtceu:conveyor_modules",
          "confidence": 0.75
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_conveyor_module",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_conveyor_module",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_conveyor_module",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:luv_conveyor_module",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_conveyor_module",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_conveyor_module",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:uv_conveyor_module",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:zpm_conveyor_module",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_conveyor_module",
          "source": "runtime-items",
          "text": "Transfers Items at specific rates as Cover.",
          "key": "runtime-tooltip",
          "label": "EV Conveyor Module",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_conveyor_module",
          "source": "runtime-items",
          "text": "Transfer Rate: 3 stacks/s",
          "key": "runtime-tooltip",
          "label": "EV Conveyor Module",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_conveyor_module",
          "source": "runtime-items",
          "text": "Transfers Items at specific rates as Cover.",
          "key": "runtime-tooltip",
          "label": "HV Conveyor Module",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_conveyor_module",
          "source": "runtime-items",
          "text": "Transfer Rate: 64 items/s",
          "key": "runtime-tooltip",
          "label": "HV Conveyor Module",
          "item_ref_count": 1
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "iv__insane_voltage",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "This Superconductor will be required for progression during LuV.",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "IV Superconductors",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "If you already made and centrifuged Rare Earth, that's great! If you haven't, you can obtain it as a Byproduct from various ores, or from centrifuging Monazite.",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "To extract the Rare Earths, you should use a Centrifuge as high a tier as possible, due to the increased chance of bonus outputs.",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "{@pagebreak}",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "The elements you can obtain are:",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/iv__insane_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/iv__insane_voltage.snbt",
          "text": "- Lanthanum is used in Fusion - you will eventually want a lot of it!",
          "key": "quest-snbt",
          "label": "iv__insane_voltage",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "createdeco:storage",
      "label": "Storage",
      "origin": "namespace_generated",
      "confidence": 0.75,
      "support": 16,
      "evidence": [
        {
          "kind": "block_tag",
          "id": "createdeco:chest_mounted_storage",
          "confidence": 0.75
        }
      ],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "create:conveyor",
      "label": "Conveyor",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 14,
      "evidence": [
        {
          "kind": "advancement",
          "id": "create:recipes/misc/crafting/kinetics/chain_conveyor",
          "confidence": 0.65
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/chain_conveyor",
          "confidence": 0.7
        },
        {
          "kind": "runtime_item",
          "id": "create:chain_conveyor",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "create:andesite_casing",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/andesite_casing.json",
          "text": "The Andesite Age",
          "key": "advancement-title:title",
          "label": "The Andesite Age",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create:andesite_casing",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/andesite_casing.json",
          "text": "Apply Andesite Alloy to stripped wood, creating a basic casing for your machines",
          "key": "advancement-description:description",
          "label": "The Andesite Age",
          "item_ref_count": 1
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "questssteam_age",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "The Millstone is an automatic version of the Quern. You can throw whatever you'd like into the top, and then right-click to take your crushed items back out. It's pretty slow if you connect it directly to your Animal Crank, but you can use gear ratios to increase its speed.",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "You will only receive the output in the first slot. The other slots are part of a GregTech mechanic that won't be relevant until much later (HV).",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "{@pagebreak}",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "Lore: The millstone can't be used to process grains into flour because Create doesn't understand TFC's food expiry system, which previously led to all sorts of bugs involving rotten items becoming fresh and vice versa. Until you're able to get the Food Processor in LV, you can still crush your grains via a Mortar in a crafting grid.",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "No more querning",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "Automatic Ore Processing",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "The Animal Crank is your first accessible source of mechanical power. To use it, place the crank on the center of a 7x7 cleared area and leash an animal to it. Different animals will provide different amounts of power, while the blocks underneath will increase the speed of the output power. The area of multiple cranks can overlap.",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "You may need to hold a second lead to attach an animal.",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "Small Animals (16 SU):",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "greate:fluid",
      "label": "Fluid",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 14,
      "evidence": [
        {
          "kind": "guide_page",
          "id": "greate:ponder/mechanical_pump_flow",
          "confidence": 0.7
        }
      ],
      "semantic_evidence": [
        {
          "kind": "guide_page",
          "id": "greate:ponder/mechanical_pump_flow",
          "source": "file:minecraft/lang-overlays",
          "text": "Fluid Transportation using Mechanical Pumps",
          "key": "lang-ponder:greate.ponder.mechanical_pump_flow.header",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/mechanical_pump_flow",
          "source": "file:minecraft/lang-overlays",
          "text": "Mechanical Pumps govern the flow of their attached pipe networks",
          "key": "lang-ponder:greate.ponder.mechanical_pump_flow.text_1",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/mechanical_pump_flow",
          "source": "file:minecraft/lang-overlays",
          "text": "Their arrow indicates the direction of flow",
          "key": "lang-ponder:greate.ponder.mechanical_pump_flow.text_2",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/mechanical_pump_flow",
          "source": "file:minecraft/lang-overlays",
          "text": "The network behind is now pulling fluids...",
          "key": "lang-ponder:greate.ponder.mechanical_pump_flow.text_3",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/mechanical_pump_flow",
          "source": "file:minecraft/lang-overlays",
          "text": "...while the network in front is transferring it outward",
          "key": "lang-ponder:greate.ponder.mechanical_pump_flow.text_4",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/mechanical_pump_flow",
          "source": "file:minecraft/lang-overlays",
          "text": "The pumps direction is unaffected by the input rotation",
          "key": "lang-ponder:greate.ponder.mechanical_pump_flow.text_5",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/mechanical_pump_flow",
          "source": "file:minecraft/lang-overlays",
          "text": "Instead, a Wrench can be used to reverse the direction",
          "key": "lang-ponder:greate.ponder.mechanical_pump_flow.text_6",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/mechanical_pump_flow",
          "source": "jar:greate-0.0.75.jar!assets/*/lang/en_us.json",
          "text": "Fluid Transportation using Mechanical Pumps",
          "key": "lang-ponder:greate.ponder.mechanical_pump_flow.header",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/mechanical_pump_flow",
          "source": "jar:greate-0.0.75.jar!assets/*/lang/en_us.json",
          "text": "Mechanical Pumps govern the flow of their attached pipe networks",
          "key": "lang-ponder:greate.ponder.mechanical_pump_flow.text_1",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/mechanical_pump_flow",
          "source": "jar:greate-0.0.75.jar!assets/*/lang/en_us.json",
          "text": "Their arrow indicates the direction of flow",
          "key": "lang-ponder:greate.ponder.mechanical_pump_flow.text_2",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/mechanical_pump_flow",
          "source": "jar:greate-0.0.75.jar!assets/*/lang/en_us.json",
          "text": "The network behind is now pulling fluids...",
          "key": "lang-ponder:greate.ponder.mechanical_pump_flow.text_3",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        },
        {
          "kind": "guide_page",
          "id": "greate:ponder/mechanical_pump_flow",
          "source": "jar:greate-0.0.75.jar!assets/*/lang/en_us.json",
          "text": "...while the network in front is transferring it outward",
          "key": "lang-ponder:greate.ponder.mechanical_pump_flow.text_4",
          "label": "Fluid Transportation using Mechanical Pumps",
          "count": 7
        }
      ],
      "semantic_evidence_omitted": 2,
      "aliases": []
    },
    {
      "id": "vintageimprovements:belt",
      "label": "Belt",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 14,
      "evidence": [
        {
          "kind": "advancement",
          "id": "vintageimprovements:belt_grinder",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "vintageimprovements:belt_grinder_skin_change",
          "confidence": 0.65
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/belt_grinder_processing",
          "confidence": 0.7
        },
        {
          "kind": "runtime_item",
          "id": "vintageimprovements:belt_grinder",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "vintageimprovements:grinder_belt",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "vintageimprovements:belt_grinder",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!data/vintageimprovements/advancements/belt_grinder.json",
          "text": "To Shine",
          "key": "advancement-title:title",
          "label": "To Shine",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "vintageimprovements:belt_grinder",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!data/vintageimprovements/advancements/belt_grinder.json",
          "text": "Use an Belt Grinder to process materials",
          "key": "advancement-description:description",
          "label": "To Shine",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "vintageimprovements:belt_grinder_skin_change",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!data/vintageimprovements/advancements/belt_grinder_skin_change.json",
          "text": "Fashion Show",
          "key": "advancement-title:title",
          "label": "Fashion Show",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "vintageimprovements:belt_grinder_skin_change",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!data/vintageimprovements/advancements/belt_grinder_skin_change.json",
          "text": "Change Belt Grinder skin via right-click with an Sand Paper",
          "key": "advancement-description:description",
          "label": "Fashion Show",
          "item_ref_count": 1
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/belt_grinder_processing",
          "source": "file:minecraft/lang-overlays",
          "text": "Processing Items on the Belt Grinder",
          "key": "lang-ponder:vintageimprovements.ponder.belt_grinder_processing.header",
          "label": "Processing Items on the Belt Grinder",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/belt_grinder_processing",
          "source": "file:minecraft/lang-overlays",
          "text": "Belt Grinders can process a variety of items",
          "key": "lang-ponder:vintageimprovements.ponder.belt_grinder_processing.text_1",
          "label": "Processing Items on the Belt Grinder",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/belt_grinder_processing",
          "source": "file:minecraft/lang-overlays",
          "text": "The processed item always moves against the rotational input to the grinder",
          "key": "lang-ponder:vintageimprovements.ponder.belt_grinder_processing.text_2",
          "label": "Processing Items on the Belt Grinder",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/belt_grinder_processing",
          "source": "file:minecraft/lang-overlays",
          "text": "Some recipes requires a specific RPM",
          "key": "lang-ponder:vintageimprovements.ponder.belt_grinder_processing.text_3",
          "label": "Processing Items on the Belt Grinder",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/belt_grinder_processing",
          "source": "file:minecraft/lang-overlays",
          "text": "There are three speeds: Low (16 RPM or less), Medium (between 16 and 64 RPM) and High (over 64 RPM)",
          "key": "lang-ponder:vintageimprovements.ponder.belt_grinder_processing.text_4",
          "label": "Processing Items on the Belt Grinder",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/belt_grinder_processing",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!assets/*/lang/en_us.json",
          "text": "Processing Items on the Belt Grinder",
          "key": "lang-ponder:vintageimprovements.ponder.belt_grinder_processing.header",
          "label": "Processing Items on the Belt Grinder",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/belt_grinder_processing",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!assets/*/lang/en_us.json",
          "text": "Belt Grinders can process a variety of items",
          "key": "lang-ponder:vintageimprovements.ponder.belt_grinder_processing.text_1",
          "label": "Processing Items on the Belt Grinder",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/belt_grinder_processing",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!assets/*/lang/en_us.json",
          "text": "The processed item always moves against the rotational input to the grinder",
          "key": "lang-ponder:vintageimprovements.ponder.belt_grinder_processing.text_2",
          "label": "Processing Items on the Belt Grinder",
          "count": 5
        }
      ],
      "semantic_evidence_omitted": 2,
      "aliases": []
    },
    {
      "id": "expatternprovider:assembler",
      "label": "Assembler",
      "origin": "namespace_generated",
      "confidence": 0.65,
      "support": 14,
      "evidence": [
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/assembler_matrix_crafter",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/assembler_matrix_frame",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/assembler_matrix_glass",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/assembler_matrix_pattern",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/assembler_matrix_speed",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/assembler_matrix_wall",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/ex_molecular_assembler",
          "confidence": 0.65
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:assembler_matrix_crafter",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:assembler_matrix_frame",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:assembler_matrix_glass",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:assembler_matrix_pattern",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:assembler_matrix_speed",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:assembler_matrix_wall",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:ex_molecular_assembler",
          "confidence": 1
        }
      ],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "create:cover",
      "label": "Cover",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 13,
      "evidence": [
        {
          "kind": "advancement",
          "id": "create:recipes/misc/crafting/kinetics/crafter_slot_cover",
          "confidence": 0.65
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/mechanical_crafter_covers",
          "confidence": 0.7
        },
        {
          "kind": "runtime_item",
          "id": "create:andesite_table_cloth",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:brass_table_cloth",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:copper_table_cloth",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "create:crafter_slot_cover",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "create:crafter_lazy_000",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/crafter_lazy_000.json",
          "text": "Desperate Measures",
          "key": "advancement-title:title",
          "label": "Desperate Measures",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create:crafter_lazy_000",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/crafter_lazy_000.json",
          "text": "Drastically slow down a Mechanical Crafter to procrastinate on proper infrastructure (Hidden Advancement)",
          "key": "advancement-description:description",
          "label": "Desperate Measures",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create:mechanical_crafter",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/mechanical_crafter.json",
          "text": "Automated Assembly",
          "key": "advancement-title:title",
          "label": "Automated Assembly",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create:mechanical_crafter",
          "source": "jar:create-1.20.1-6.0.8.jar!data/create/advancements/mechanical_crafter.json",
          "text": "Place and power some Mechanical Crafters",
          "key": "advancement-description:description",
          "label": "Automated Assembly",
          "item_ref_count": 1
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "questssteam_age",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "The Millstone is an automatic version of the Quern. You can throw whatever you'd like into the top, and then right-click to take your crushed items back out. It's pretty slow if you connect it directly to your Animal Crank, but you can use gear ratios to increase its speed.",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "You will only receive the output in the first slot. The other slots are part of a GregTech mechanic that won't be relevant until much later (HV).",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "{@pagebreak}",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "Lore: The millstone can't be used to process grains into flour because Create doesn't understand TFC's food expiry system, which previously led to all sorts of bugs involving rotten items becoming fresh and vice versa. Until you're able to get the Food Processor in LV, you can still crush your grains via a Mortar in a crafting grid.",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "No more querning",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "Automatic Ore Processing",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/questssteam_age",
          "source": "file:minecraft/config/ftbquests/quests/chapters/questssteam_age.snbt",
          "text": "The Animal Crank is your first accessible source of mechanical power. To use it, place the crank on the center of a 7x7 cleared area and leash an animal to it. Different animals will provide different amounts of power, while the blocks underneath will increase the speed of the output power. The area of multiple cranks can overlap.",
          "key": "quest-snbt",
          "label": "questssteam_age",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "create_connected:generator",
      "label": "Generator",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 13,
      "evidence": [
        {
          "kind": "advancement",
          "id": "create_connected:pulse_generator_infinite_loop",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create_connected:recipes/crafting/kinetics/sequenced_pulse_generator",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "create_connected:sequenced_pulse_generator",
          "confidence": 0.65
        },
        {
          "kind": "guide_page",
          "id": "create_connected:ponder/sequenced_pulse_generator",
          "confidence": 0.7
        },
        {
          "kind": "runtime_item",
          "id": "create_connected:sequenced_pulse_generator",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "create_connected:pulse_generator_infinite_loop",
          "source": "jar:create_connected-1.1.13-mc1.20.1-all.jar!data/create_connected/advancements/pulse_generator_infinite_loop.json",
          "text": "Infinite Loop",
          "key": "advancement-title:title",
          "label": "Infinite Loop",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create_connected:pulse_generator_infinite_loop",
          "source": "jar:create_connected-1.1.13-mc1.20.1-all.jar!data/create_connected/advancements/pulse_generator_infinite_loop.json",
          "text": "Overload a Sequenced Pulse Generator with a buggy program (Hidden Advancement)",
          "key": "advancement-description:description",
          "label": "Infinite Loop",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create_connected:control_chip",
          "source": "jar:create_connected-1.1.13-mc1.20.1-all.jar!data/create_connected/advancements/control_chip.json",
          "text": "Precise Fabrication",
          "key": "advancement-title:title",
          "label": "Precise Fabrication",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create_connected:control_chip",
          "source": "jar:create_connected-1.1.13-mc1.20.1-all.jar!data/create_connected/advancements/control_chip.json",
          "text": "Assemble a Control Chip",
          "key": "advancement-description:description",
          "label": "Precise Fabrication",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create_connected:sequenced_pulse_generator",
          "source": "jar:create_connected-1.1.13-mc1.20.1-all.jar!data/create_connected/advancements/sequenced_pulse_generator.json",
          "text": "Computational Supremacy",
          "key": "advancement-title:title",
          "label": "Computational Supremacy",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "create_connected:sequenced_pulse_generator",
          "source": "jar:create_connected-1.1.13-mc1.20.1-all.jar!data/create_connected/advancements/sequenced_pulse_generator.json",
          "text": "Place down a Sequenced Pulse Generator",
          "key": "advancement-description:description",
          "label": "Computational Supremacy",
          "item_ref_count": 1
        },
        {
          "kind": "guide_page",
          "id": "create_connected:ponder/sequenced_pulse_generator",
          "source": "jar:create_connected-1.1.13-mc1.20.1-all.jar!assets/*/lang/en_us.json",
          "text": "Controlling signals using Sequenced Pulse Generators",
          "key": "lang-ponder:create_connected.ponder.sequenced_pulse_generator.header",
          "label": "Controlling signals using Sequenced Pulse Generators",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create_connected:ponder/sequenced_pulse_generator",
          "source": "jar:create_connected-1.1.13-mc1.20.1-all.jar!assets/*/lang/en_us.json",
          "text": "Seq. Pulse Gen. outputs signals by following a timed list of instructions",
          "key": "lang-ponder:create_connected.ponder.sequenced_pulse_generator.text_1",
          "label": "Controlling signals using Sequenced Pulse Generators",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create_connected:ponder/sequenced_pulse_generator",
          "source": "jar:create_connected-1.1.13-mc1.20.1-all.jar!assets/*/lang/en_us.json",
          "text": "Right-click it to open the Configuration UI",
          "key": "lang-ponder:create_connected.ponder.sequenced_pulse_generator.text_2",
          "label": "Controlling signals using Sequenced Pulse Generators",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create_connected:ponder/sequenced_pulse_generator",
          "source": "jar:create_connected-1.1.13-mc1.20.1-all.jar!assets/*/lang/en_us.json",
          "text": "Upon receiving a signal from its back...",
          "key": "lang-ponder:create_connected.ponder.sequenced_pulse_generator.text_3",
          "label": "Controlling signals using Sequenced Pulse Generators",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create_connected:ponder/sequenced_pulse_generator",
          "source": "jar:create_connected-1.1.13-mc1.20.1-all.jar!assets/*/lang/en_us.json",
          "text": "...it will start running its configured sequence",
          "key": "lang-ponder:create_connected.ponder.sequenced_pulse_generator.text_4",
          "label": "Controlling signals using Sequenced Pulse Generators",
          "count": 9
        },
        {
          "kind": "guide_page",
          "id": "create_connected:ponder/sequenced_pulse_generator",
          "source": "jar:create_connected-1.1.13-mc1.20.1-all.jar!assets/*/lang/en_us.json",
          "text": "Once finished, it waits for the next signal and starts over",
          "key": "lang-ponder:create_connected.ponder.sequenced_pulse_generator.text_5",
          "label": "Controlling signals using Sequenced Pulse Generators",
          "count": 9
        }
      ],
      "semantic_evidence_omitted": 3,
      "aliases": []
    },
    {
      "id": "expatternprovider:interface",
      "label": "Interface",
      "origin": "namespace_generated",
      "confidence": 0.75,
      "support": 12,
      "evidence": [
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/oversize_interface",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/oversize_interface_alt",
          "confidence": 0.65
        },
        {
          "kind": "advancement",
          "id": "expatternprovider:recipes/misc/oversize_interface_part",
          "confidence": 0.65
        },
        {
          "kind": "item_tag",
          "id": "expatternprovider:extended_interface",
          "confidence": 0.75
        },
        {
          "kind": "item_tag",
          "id": "expatternprovider:oversize_interface",
          "confidence": 0.75
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:ex_interface",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:ex_interface_part",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:interface_upgrade",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:oversize_interface",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "expatternprovider:oversize_interface_part",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/luv__ludicrous_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/luv__ludicrous_voltage.snbt",
          "text": "luv__ludicrous_voltage",
          "key": "quest-snbt",
          "label": "luv__ludicrous_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/luv__ludicrous_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/luv__ludicrous_voltage.snbt",
          "text": "The UHPIC unlocks ZPM Energy Hatches and Dynamos, as well as the Active Transformer.",
          "key": "quest-snbt",
          "label": "luv__ludicrous_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/luv__ludicrous_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/luv__ludicrous_voltage.snbt",
          "text": "You may know the drill, but do you know the screwdriver?",
          "key": "quest-snbt",
          "label": "luv__ludicrous_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/luv__ludicrous_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/luv__ludicrous_voltage.snbt",
          "text": "Ultra High Power Integrated Circuit",
          "key": "quest-snbt",
          "label": "luv__ludicrous_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/luv__ludicrous_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/luv__ludicrous_voltage.snbt",
          "text": "Dielectric PCB Coolant is used to maintain some highly advanced late and end-game machinery.",
          "key": "quest-snbt",
          "label": "luv__ludicrous_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/luv__ludicrous_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/luv__ludicrous_voltage.snbt",
          "text": "You only need a little bit in LuV, but you'll want to automate it when you get to ZPM.",
          "key": "quest-snbt",
          "label": "luv__ludicrous_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/luv__ludicrous_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/luv__ludicrous_voltage.snbt",
          "text": "Absolutely beneficial for the environment",
          "key": "quest-snbt",
          "label": "luv__ludicrous_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/luv__ludicrous_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/luv__ludicrous_voltage.snbt",
          "text": "Dielectric PCB Coolant",
          "key": "quest-snbt",
          "label": "luv__ludicrous_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/luv__ludicrous_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/luv__ludicrous_voltage.snbt",
          "text": "The Active Transformer is a multiblock used to transform energy on a much larger scale. For example, you can convert HV to LV power directly by using the apropriate energy hatches.",
          "key": "quest-snbt",
          "label": "luv__ludicrous_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/luv__ludicrous_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/luv__ludicrous_voltage.snbt",
          "text": "It also has a second function, detailed in the above quest.",
          "key": "quest-snbt",
          "label": "luv__ludicrous_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/luv__ludicrous_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/luv__ludicrous_voltage.snbt",
          "text": "{@pagebreak}",
          "key": "quest-snbt",
          "label": "luv__ludicrous_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/luv__ludicrous_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/luv__ludicrous_voltage.snbt",
          "text": "Lore: This is yet another feature from TecTech, which also required these cables to be painted. They do look fantastic if you paint them, but it's optional in CEu.",
          "key": "quest-snbt",
          "label": "luv__ludicrous_voltage",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "gtceu:reactor",
      "label": "Reactor",
      "origin": "namespace_generated",
      "confidence": 0.55,
      "support": 12,
      "evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_chemical_reactor",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_chemical_reactor",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_chemical_reactor",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:large_chemical_reactor",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:luv_chemical_reactor",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:luv_fusion_reactor",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_chemical_reactor",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_chemical_reactor",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:uv_chemical_reactor",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:uv_fusion_reactor",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:zpm_chemical_reactor",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:zpm_fusion_reactor",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_chemical_reactor",
          "source": "runtime-items",
          "text": "Letting Chemicals react with each other",
          "key": "runtime-tooltip",
          "label": "§5Advanced Chemical Reactor III§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_chemical_reactor",
          "source": "runtime-items",
          "text": "Voltage IN: 2,048 EU/t (EV)",
          "key": "runtime-tooltip",
          "label": "§5Advanced Chemical Reactor III§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_chemical_reactor",
          "source": "runtime-items",
          "text": "Energy Capacity: 131,072 EU",
          "key": "runtime-tooltip",
          "label": "§5Advanced Chemical Reactor III§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_chemical_reactor",
          "source": "runtime-items",
          "text": "Fluid Capacity: 16,000 mB",
          "key": "runtime-tooltip",
          "label": "§5Advanced Chemical Reactor III§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_chemical_reactor",
          "source": "runtime-items",
          "text": "Letting Chemicals react with each other",
          "key": "runtime-tooltip",
          "label": "§6Advanced Chemical Reactor II§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_chemical_reactor",
          "source": "runtime-items",
          "text": "Voltage IN: 512 EU/t (HV)",
          "key": "runtime-tooltip",
          "label": "§6Advanced Chemical Reactor II§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_chemical_reactor",
          "source": "runtime-items",
          "text": "Energy Capacity: 32,768 EU",
          "key": "runtime-tooltip",
          "label": "§6Advanced Chemical Reactor II§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_chemical_reactor",
          "source": "runtime-items",
          "text": "Fluid Capacity: 16,000 mB",
          "key": "runtime-tooltip",
          "label": "§6Advanced Chemical Reactor II§r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_chemical_reactor",
          "source": "runtime-items",
          "text": "Chemical Performer",
          "key": "runtime-tooltip",
          "label": "§9Elite Chemical Reactor §r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_chemical_reactor",
          "source": "runtime-items",
          "text": "Voltage IN: 8,192 EU/t (IV)",
          "key": "runtime-tooltip",
          "label": "§9Elite Chemical Reactor §r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_chemical_reactor",
          "source": "runtime-items",
          "text": "Energy Capacity: 524,288 EU",
          "key": "runtime-tooltip",
          "label": "§9Elite Chemical Reactor §r",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_chemical_reactor",
          "source": "runtime-items",
          "text": "Fluid Capacity: 16,000 mB",
          "key": "runtime-tooltip",
          "label": "§9Elite Chemical Reactor §r",
          "item_ref_count": 1
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "create:rail",
      "label": "Rail",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 12,
      "evidence": [
        {
          "kind": "advancement",
          "id": "create:recipes/misc/crafting/kinetics/controller_rail",
          "confidence": 0.65
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cart_assembler_rails",
          "confidence": 0.7
        },
        {
          "kind": "runtime_item",
          "id": "create:controller_rail",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "guide_page",
          "id": "create:ponder/cart_assembler_rails",
          "source": "file:minecraft/lang-overlays",
          "text": "Other types of Minecarts and Rails",
          "key": "lang-ponder:create.ponder.cart_assembler_rails.header",
          "label": "Other types of Minecarts and Rails",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cart_assembler_rails",
          "source": "file:minecraft/lang-overlays",
          "text": "Cart Assemblers on Regular Tracks will not affect the passing carts' motion",
          "key": "lang-ponder:create.ponder.cart_assembler_rails.text_1",
          "label": "Other types of Minecarts and Rails",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cart_assembler_rails",
          "source": "file:minecraft/lang-overlays",
          "text": "When on Powered or Controller Rail, the carts will be held in place until it's Powered",
          "key": "lang-ponder:create.ponder.cart_assembler_rails.text_2",
          "label": "Other types of Minecarts and Rails",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cart_assembler_rails",
          "source": "file:minecraft/lang-overlays",
          "text": "Other types of Minecarts can be used as the anchor",
          "key": "lang-ponder:create.ponder.cart_assembler_rails.text_3",
          "label": "Other types of Minecarts and Rails",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cart_assembler_rails",
          "source": "file:minecraft/lang-overlays",
          "text": "Furnace Carts will keep themselves powered, pulling fuel from any attached inventories",
          "key": "lang-ponder:create.ponder.cart_assembler_rails.text_4",
          "label": "Other types of Minecarts and Rails",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cart_assembler_rails",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Other types of Minecarts and Rails",
          "key": "lang-ponder:create.ponder.cart_assembler_rails.header",
          "label": "Other types of Minecarts and Rails",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cart_assembler_rails",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Cart Assemblers on Regular Tracks will not affect the passing carts' motion",
          "key": "lang-ponder:create.ponder.cart_assembler_rails.text_1",
          "label": "Other types of Minecarts and Rails",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cart_assembler_rails",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "When on Powered or Controller Rail, the carts will be held in place until it's Powered",
          "key": "lang-ponder:create.ponder.cart_assembler_rails.text_2",
          "label": "Other types of Minecarts and Rails",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cart_assembler_rails",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Other types of Minecarts can be used as the anchor",
          "key": "lang-ponder:create.ponder.cart_assembler_rails.text_3",
          "label": "Other types of Minecarts and Rails",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "create:ponder/cart_assembler_rails",
          "source": "jar:create-1.20.1-6.0.8.jar!assets/*/lang/en_us.json",
          "text": "Furnace Carts will keep themselves powered, pulling fuel from any attached inventories",
          "key": "lang-ponder:create.ponder.cart_assembler_rails.text_4",
          "label": "Other types of Minecarts and Rails",
          "count": 5
        },
        {
          "kind": "runtime_item",
          "id": "create:controller_rail",
          "source": "runtime-items",
          "text": "A _uni-directional_ powered rail with _variable speed_, controlled by the _signal strength_ supplied to it.",
          "key": "lang:block.create.controller_rail.tooltip.summary",
          "label": "Controller Rail",
          "item_ref_count": 1
        }
      ],
      "aliases": []
    },
    {
      "id": "gtceu:cell",
      "label": "Cell",
      "origin": "namespace_generated",
      "confidence": 0.55,
      "support": 10,
      "evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:aluminium_fluid_cell",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:cell_extruder_mold",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:electrolytic_cell",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:fluid_cell",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:stainless_steel_fluid_cell",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:steel_fluid_cell",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:stem_cells",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:titanium_fluid_cell",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:tungsten_steel_fluid_cell",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:universal_fluid_cell",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:aluminium_fluid_cell",
          "source": "runtime-items",
          "text": "Safely contains hot, cold, and lighter-than-air items and fluids.",
          "key": "runtime-tooltip",
          "label": "Empty Aluminium Cell",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:aluminium_fluid_cell",
          "source": "runtime-items",
          "text": "Fluid Capacity: 32,000 mB",
          "key": "runtime-tooltip",
          "label": "Empty Aluminium Cell",
          "item_ref_count": 1
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "lv__low_voltage",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "The Fluid Regulator is sort of the equivalent to a Robot Arm for Fluids.",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "It can transfer fluids, but has two useful modes:",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "- Supply Exact will transfer the amount of specified Fluid per tick if available. No more, no less.",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "- Keep Exact will make sure the exact amount of Fluid in the attached machine is being kept.",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "You won't need it much, because natively GT machines will fill only one slot with the Pump Cover",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "A cover that you may not use much",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "LV Fluid Regulator",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "When placed on a machine, Robot Arms are a more configurable version of a Conveyor Module. They allow you to transfer items in specific batches, at specific rates, or keep a certain amount of items stocked.",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "Practical uses for the Robot Arm will be explained in future Quests as a tutorial.",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "gtceu:charger",
      "label": "Charger",
      "origin": "namespace_generated",
      "confidence": 0.55,
      "support": 10,
      "evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:hv_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:iv_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:luv_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:lv_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:mv_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:uhv_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ulv_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:uv_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:zpm_charger_4x",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_charger_4x",
          "source": "runtime-items",
          "text": "Item Slots: 4",
          "key": "runtime-tooltip",
          "label": "§5Extreme Voltage§r 4x Turbo Charger",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_charger_4x",
          "source": "runtime-items",
          "text": "Voltage IN/OUT: 2,048 EU/t (EV)",
          "key": "runtime-tooltip",
          "label": "§5Extreme Voltage§r 4x Turbo Charger",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtceu:ev_charger_4x",
          "source": "runtime-items",
          "text": "Amperage IN up to: 16A",
          "key": "runtime-tooltip",
          "label": "§5Extreme Voltage§r 4x Turbo Charger",
          "item_ref_count": 1
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "lv__low_voltage",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "The Fluid Regulator is sort of the equivalent to a Robot Arm for Fluids.",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "It can transfer fluids, but has two useful modes:",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "- Supply Exact will transfer the amount of specified Fluid per tick if available. No more, no less.",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "- Keep Exact will make sure the exact amount of Fluid in the attached machine is being kept.",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "You won't need it much, because natively GT machines will fill only one slot with the Pump Cover",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "A cover that you may not use much",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "LV Fluid Regulator",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        },
        {
          "kind": "quest_node",
          "id": "pack:ftbquests/chapters/lv__low_voltage",
          "source": "file:minecraft/config/ftbquests/quests/chapters/lv__low_voltage.snbt",
          "text": "When placed on a machine, Robot Arms are a more configurable version of a Conveyor Module. They allow you to transfer items in specific batches, at specific rates, or keep a certain amount of items stocked.",
          "key": "quest-snbt",
          "label": "lv__low_voltage",
          "item_ref_count": 32
        }
      ],
      "semantic_evidence_omitted": 52,
      "aliases": []
    },
    {
      "id": "gtmutils:charger",
      "label": "Charger",
      "origin": "namespace_generated",
      "confidence": 0.55,
      "support": 10,
      "evidence": [
        {
          "kind": "runtime_item",
          "id": "gtmutils:ev_auto_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:hv_auto_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:iv_auto_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:luv_auto_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:lv_auto_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:mv_auto_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:uhv_auto_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:ulv_auto_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:uv_auto_charger_4x",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:zpm_auto_charger_4x",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "runtime_item",
          "id": "gtmutils:ev_auto_charger_4x",
          "source": "runtime-items",
          "text": "Item Slots: 4",
          "key": "runtime-tooltip",
          "label": "§5Extreme Voltage§r 4x Auto Turbo Charger",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:ev_auto_charger_4x",
          "source": "runtime-items",
          "text": "Voltage IN/OUT: 2,048 EU/t (EV)",
          "key": "runtime-tooltip",
          "label": "§5Extreme Voltage§r 4x Auto Turbo Charger",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:ev_auto_charger_4x",
          "source": "runtime-items",
          "text": "Amperage IN up to: 16A",
          "key": "runtime-tooltip",
          "label": "§5Extreme Voltage§r 4x Auto Turbo Charger",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:hv_auto_charger_4x",
          "source": "runtime-items",
          "text": "Item Slots: 4",
          "key": "runtime-tooltip",
          "label": "§6High Voltage§r 4x Auto Turbo Charger",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:hv_auto_charger_4x",
          "source": "runtime-items",
          "text": "Voltage IN/OUT: 512 EU/t (HV)",
          "key": "runtime-tooltip",
          "label": "§6High Voltage§r 4x Auto Turbo Charger",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:hv_auto_charger_4x",
          "source": "runtime-items",
          "text": "Amperage IN up to: 16A",
          "key": "runtime-tooltip",
          "label": "§6High Voltage§r 4x Auto Turbo Charger",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:iv_auto_charger_4x",
          "source": "runtime-items",
          "text": "Item Slots: 4",
          "key": "runtime-tooltip",
          "label": "§9Insane Voltage§r 4x Auto Turbo Charger",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:iv_auto_charger_4x",
          "source": "runtime-items",
          "text": "Voltage IN/OUT: 8,192 EU/t (IV)",
          "key": "runtime-tooltip",
          "label": "§9Insane Voltage§r 4x Auto Turbo Charger",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:iv_auto_charger_4x",
          "source": "runtime-items",
          "text": "Amperage IN up to: 16A",
          "key": "runtime-tooltip",
          "label": "§9Insane Voltage§r 4x Auto Turbo Charger",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:luv_auto_charger_4x",
          "source": "runtime-items",
          "text": "Item Slots: 4",
          "key": "runtime-tooltip",
          "label": "§dLudicrous Voltage§r 4x Auto Turbo Charger",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:luv_auto_charger_4x",
          "source": "runtime-items",
          "text": "Voltage IN/OUT: 32,768 EU/t (LuV)",
          "key": "runtime-tooltip",
          "label": "§dLudicrous Voltage§r 4x Auto Turbo Charger",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "gtmutils:luv_auto_charger_4x",
          "source": "runtime-items",
          "text": "Amperage IN up to: 16A",
          "key": "runtime-tooltip",
          "label": "§dLudicrous Voltage§r 4x Auto Turbo Charger",
          "item_ref_count": 1
        }
      ],
      "semantic_evidence_omitted": 18,
      "aliases": []
    },
    {
      "id": "vintageimprovements:automation",
      "label": "Automation",
      "origin": "namespace_generated",
      "confidence": 0.7,
      "support": 10,
      "evidence": [
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/lathe_automation",
          "confidence": 0.7
        }
      ],
      "semantic_evidence": [
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/lathe_automation",
          "source": "file:minecraft/lang-overlays",
          "text": "Automation of the Lathe",
          "key": "lang-ponder:vintageimprovements.ponder.lathe_automation.header",
          "label": "Automation of the Lathe",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/lathe_automation",
          "source": "file:minecraft/lang-overlays",
          "text": "Lathe can be automated with the Recipe Card",
          "key": "lang-ponder:vintageimprovements.ponder.lathe_automation.text_1",
          "label": "Automation of the Lathe",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/lathe_automation",
          "source": "file:minecraft/lang-overlays",
          "text": "Use a Recipe Card via Right-click to define a recipe",
          "key": "lang-ponder:vintageimprovements.ponder.lathe_automation.text_2",
          "label": "Automation of the Lathe",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/lathe_automation",
          "source": "file:minecraft/lang-overlays",
          "text": "Then you must put it inside a back Lathe block",
          "key": "lang-ponder:vintageimprovements.ponder.lathe_automation.text_3",
          "label": "Automation of the Lathe",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/lathe_automation",
          "source": "file:minecraft/lang-overlays",
          "text": "Lathe will automatically apply chosen recipe",
          "key": "lang-ponder:vintageimprovements.ponder.lathe_automation.text_4",
          "label": "Automation of the Lathe",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/lathe_automation",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!assets/*/lang/en_us.json",
          "text": "Automation of the Lathe",
          "key": "lang-ponder:vintageimprovements.ponder.lathe_automation.header",
          "label": "Automation of the Lathe",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/lathe_automation",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!assets/*/lang/en_us.json",
          "text": "Lathe can be automated with the Recipe Card",
          "key": "lang-ponder:vintageimprovements.ponder.lathe_automation.text_1",
          "label": "Automation of the Lathe",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/lathe_automation",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!assets/*/lang/en_us.json",
          "text": "Use a Recipe Card via Right-click to define a recipe",
          "key": "lang-ponder:vintageimprovements.ponder.lathe_automation.text_2",
          "label": "Automation of the Lathe",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/lathe_automation",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!assets/*/lang/en_us.json",
          "text": "Then you must put it inside a back Lathe block",
          "key": "lang-ponder:vintageimprovements.ponder.lathe_automation.text_3",
          "label": "Automation of the Lathe",
          "count": 5
        },
        {
          "kind": "guide_page",
          "id": "vintageimprovements:ponder/lathe_automation",
          "source": "jar:vintageimprovements-1.20.1-0.3.7.2.jar!assets/*/lang/en_us.json",
          "text": "Lathe will automatically apply chosen recipe",
          "key": "lang-ponder:vintageimprovements.ponder.lathe_automation.text_4",
          "label": "Automation of the Lathe",
          "count": 5
        }
      ],
      "aliases": []
    },
    {
      "id": "species:hatch",
      "label": "Hatch",
      "origin": "namespace_generated",
      "confidence": 0.75,
      "support": 10,
      "evidence": [
        {
          "kind": "advancement",
          "id": "species:species/v1/hatch_wraptor",
          "confidence": 0.65
        },
        {
          "kind": "block_tag",
          "id": "species:petrified_egg_hatch",
          "confidence": 0.75
        }
      ],
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "species:species/v1/hatch_wraptor",
          "source": "jar:species-3.5.jar!data/species/advancements/species/v1/hatch_wraptor.json",
          "text": "Tough Love",
          "key": "advancement-title:title",
          "label": "Tough Love",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "species:species/v1/hatch_wraptor",
          "source": "jar:species-3.5.jar!data/species/advancements/species/v1/hatch_wraptor.json",
          "text": "Hatch a Wraptor Egg using an Anvil",
          "key": "advancement-description:description",
          "label": "Tough Love",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "species:cracked_wraptor_egg",
          "source": "runtime-items",
          "text": "0.3 / 16.0g.",
          "key": "runtime-tooltip",
          "label": "Cracked Wraptor Egg",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "species:cracked_wraptor_egg",
          "source": "runtime-items",
          "text": "Expires on: 19:59 June 8, 1000 (in 7 day(s))",
          "key": "runtime-tooltip",
          "label": "Cracked Wraptor Egg",
          "item_ref_count": 1
        },
        {
          "kind": "runtime_item",
          "id": "species:cracked_wraptor_egg",
          "source": "runtime-items",
          "text": "Wither Resistance (01:30)",
          "key": "runtime-tooltip",
          "label": "Cracked Wraptor Egg",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "tfc:world/volcano",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/world/volcano.json",
          "text": "Pacific Rim",
          "key": "advancement-title:title",
          "label": "Pacific Rim",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "tfc:world/volcano",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/world/volcano.json",
          "text": "Find an area with high volcanic activity",
          "key": "advancement-description:description",
          "label": "Pacific Rim",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "tfc:world/trench",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/world/trench.json",
          "text": "In the Trenches",
          "key": "advancement-title:title",
          "label": "In the Trenches",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "tfc:world/trench",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/world/trench.json",
          "text": "Find a deep ocean trench",
          "key": "advancement-description:description",
          "label": "In the Trenches",
          "item_ref_count": 1
        }
      ],
      "aliases": []
    },
    {
      "id": "tfc:fan",
      "label": "Fan",
      "origin": "namespace_generated",
      "confidence": 0.55,
      "support": 10,
      "evidence": [
        {
          "kind": "runtime_item",
          "id": "tfc:coral/brain_coral_fan",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:coral/brain_dead_coral_fan",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:coral/bubble_coral_fan",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:coral/bubble_dead_coral_fan",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:coral/fire_coral_fan",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:coral/fire_dead_coral_fan",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:coral/horn_coral_fan",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:coral/horn_dead_coral_fan",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:coral/tube_coral_fan",
          "confidence": 1
        },
        {
          "kind": "runtime_item",
          "id": "tfc:coral/tube_dead_coral_fan",
          "confidence": 1
        }
      ],
      "semantic_evidence": [
        {
          "kind": "advancement",
          "id": "tfc:world/coral_reef",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/world/coral_reef.json",
          "text": "What a rel-Reef!",
          "key": "advancement-title:title",
          "label": "What a rel-Reef!",
          "item_ref_count": 1
        },
        {
          "kind": "advancement",
          "id": "tfc:world/coral_reef",
          "source": "jar:TerraFirmaCraft-Forge-1.20.1-3.2.21.jar!data/tfc/advancements/world/coral_reef.json",
          "text": "Find a coral reef",
          "key": "advancement-description:description",
          "label": "What a rel-Reef!",
          "item_ref_count": 1
        }
      ],
      "aliases": []
    }
  ],
  "required_output_contract": {
    "required_values_count": 48,
    "required_candidate_ids": [
      "ad_astra:rocket",
      "create:engine",
      "create:cogwheel",
      "tfclunchbox:battery",
      "create:pump",
      "immersive_aircraft:transport",
      "ae2:storage",
      "gtceu:generator",
      "gtceu:bus",
      "tfc:power",
      "vintageimprovements:fluid",
      "create_connected:fan",
      "framedblocks:rail",
      "gtceu:pump",
      "gtceu:turbine",
      "gtceu:cover",
      "create_connected:battery",
      "ae2wtlib:terminal",
      "tfc:fluid",
      "create:shaft",
      "create_factory_logistics:package",
      "geckolib:engine",
      "gtceu:assembler",
      "ad_astra:oxygen",
      "greate:shaft",
      "gtceu:power",
      "create:network",
      "expatternprovider:bus",
      "deafission:fission",
      "deafission:reactor",
      "gtceu:tank",
      "gtceu:conveyor",
      "createdeco:storage",
      "create:conveyor",
      "greate:fluid",
      "vintageimprovements:belt",
      "expatternprovider:assembler",
      "create:cover",
      "create_connected:generator",
      "expatternprovider:interface",
      "gtceu:reactor",
      "create:rail",
      "gtceu:cell",
      "gtceu:charger",
      "gtmutils:charger",
      "vintageimprovements:automation",
      "species:hatch",
      "tfc:fan"
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