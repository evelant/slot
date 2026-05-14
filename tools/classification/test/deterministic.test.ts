import { describe, test, expect } from "bun:test";
import type { ItemExtractRecord } from "../src/extract/record.ts";
import type { SummaryBundle } from "../src/extract/vanilla/source.ts";
import { runDeterministic } from "../src/deterministic/run.ts";
import { materialFamilyRule } from "../src/deterministic/rules/material_family.ts";
import { formRule } from "../src/deterministic/rules/form.ts";
import { equipSlotRule } from "../src/deterministic/rules/equip_slot.ts";
import { processingInRule } from "../src/deterministic/rules/processing_in.ts";
import { originRule } from "../src/deterministic/rules/origin.ts";
import { dyeColorRule } from "../src/deterministic/rules/dye_color.ts";
import { requiredToolRule } from "../src/deterministic/rules/required_tool.ts";
import { booleansRule } from "../src/deterministic/rules/booleans.ts";
import { isFuelRule } from "../src/deterministic/rules/is_fuel.ts";
import { emitsLightRule } from "../src/deterministic/rules/emits_light.ts";
import type { RuleContext } from "../src/deterministic/types.ts";

function record(overrides: Partial<ItemExtractRecord>): ItemExtractRecord {
  return {
    id: "minecraft:x",
    namespace: "minecraft",
    path: "x",
    display_name: null,
    minecraft_tags: [],
    minecraft_tags_direct: [],
    recipe_role: { ingredient_of: [], output_of: [], in_degree: 0, out_degree: 0, ingredient_of_counts: {}, output_of_counts: {} },
    model_parents: [],
    loot_table_sources: [],
    creative_tabs: [],
    component_data: null,
    ...overrides,
  };
}

function ctx(rec: ItemExtractRecord, overrides: Partial<RuleContext> = {}): RuleContext {
  return {
    record: rec,
    bundle: {
      registries: {},
      itemComponents: {},
      recipes: {},
      lootTables: {},
      itemTags: {},
      blockTags: {},
      itemDefinitions: {},
      models: {},
      lang: {},
      blocks: {},
      version: "test",
    },
    blockTagClosure: new Map(),
    recipeTypes: new Map(),
    ...overrides,
  };
}

describe("materialFamilyRule", () => {
  test("log tag pins the wood type", () => {
    const out = materialFamilyRule.run(ctx(record({
      id: "minecraft:oak_log",
      path: "oak_log",
      minecraft_tags: ["minecraft:oak_logs", "minecraft:logs"],
    })));
    expect(out).toHaveLength(1);
    expect(out[0]!).toMatchObject({ facet: "material_family", value: "wood_oak" });
  });

  test("iron tool materials tag", () => {
    const out = materialFamilyRule.run(ctx(record({
      id: "minecraft:iron_ingot",
      path: "iron_ingot",
      minecraft_tags: ["minecraft:iron_tool_materials"],
    })));
    expect(out[0]!).toMatchObject({ value: "iron" });
  });

  test("exact id mapping for raw_iron", () => {
    const out = materialFamilyRule.run(ctx(record({
      id: "minecraft:raw_iron",
      path: "raw_iron",
    })));
    expect(out[0]!).toMatchObject({ value: "iron" });
  });

  test("id prefix fallback for deepslate", () => {
    const out = materialFamilyRule.run(ctx(record({
      id: "minecraft:deepslate_tiles",
      path: "deepslate_tiles",
    })));
    expect(out[0]!).toMatchObject({ value: "deepslate" });
  });

  test("ore ids use the resource material instead of the host stone", () => {
    const hosted = materialFamilyRule.run(ctx(record({
      id: "gtceu:andesite_copper_ore",
      namespace: "gtceu",
      path: "andesite_copper_ore",
    })));
    expect(hosted[0]!).toMatchObject({ value: "copper", source: "rule:material_family_from_ore_id" });

    const deepslate = materialFamilyRule.run(ctx(record({
      id: "minecraft:deepslate_iron_ore",
      path: "deepslate_iron_ore",
    })));
    expect(deepslate[0]!).toMatchObject({ value: "iron" });
  });

  test("processed ore ids strip process stage prefixes", () => {
    const crushed = materialFamilyRule.run(ctx(record({
      id: "gtceu:crushed_iron_ore",
      namespace: "gtceu",
      path: "crushed_iron_ore",
    })));
    expect(crushed[0]!).toMatchObject({ value: "iron" });

    const purified = materialFamilyRule.run(ctx(record({
      id: "gtceu:purified_iron_ore",
      namespace: "gtceu",
      path: "purified_iron_ore",
    })));
    expect(purified[0]!).toMatchObject({ value: "iron" });
  });

  test("TFC ore paths strip grade prefixes", () => {
    const poor = materialFamilyRule.run(ctx(record({
      id: "tfc:ore/poor_hematite",
      namespace: "tfc",
      path: "ore/poor_hematite",
    })));
    expect(poor[0]!).toMatchObject({ value: "hematite" });

    const native = materialFamilyRule.run(ctx(record({
      id: "tfc:ore/small_native_copper",
      namespace: "tfc",
      path: "ore/small_native_copper",
    })));
    expect(native[0]!).toMatchObject({ value: "copper" });

    const hosted = materialFamilyRule.run(ctx(record({
      id: "tfc:ore/normal_limonite/diorite",
      namespace: "tfc",
      path: "ore/normal_limonite/diorite",
    })));
    expect(hosted[0]!).toMatchObject({ value: "limonite" });
  });

  test("bloom ids use the bloom metal family", () => {
    const out = materialFamilyRule.run(ctx(record({
      id: "tfc:raw_iron_bloom",
      namespace: "tfc",
      path: "raw_iron_bloom",
    })));
    expect(out[0]!).toMatchObject({ value: "iron", source: "rule:material_family_from_bloom_id" });
  });

  test("common material tags derive modded material families", () => {
    const metalPart = materialFamilyRule.run(ctx(record({
      id: "tfchotornot:tong_part/rose_gold",
      namespace: "tfchotornot",
      path: "tong_part/rose_gold",
      minecraft_tags: ["tfc:metal_item/rose_gold"],
    })));
    expect(metalPart[0]!).toMatchObject({
      value: "rose_gold",
      source: "rule:material_family_from_common_tag",
    });

    const wire = materialFamilyRule.run(ctx(record({
      id: "createaddition:copper_wire",
      namespace: "createaddition",
      path: "copper_wire",
      minecraft_tags: ["forge:wires/copper"],
    })));
    expect(wire[0]!).toMatchObject({ value: "copper" });

    const spring = materialFamilyRule.run(ctx(record({
      id: "vintageimprovements:small_ironwood_spring",
      namespace: "vintageimprovements",
      path: "small_ironwood_spring",
      minecraft_tags: ["vintageimprovements:small_springs/ironwood"],
    })));
    expect(spring[0]!).toMatchObject({ value: "ironwood" });

    const storageBlock = materialFamilyRule.run(ctx(record({
      id: "tfg:boron_carbide_block",
      namespace: "tfg",
      path: "boron_carbide_block",
      minecraft_tags: ["forge:storage_blocks/boron_carbide"],
    })));
    expect(storageBlock[0]!).toMatchObject({ value: "boron_carbide" });

    const hotIngot = materialFamilyRule.run(ctx(record({
      id: "gtceu:hot_titanium_ingot",
      namespace: "gtceu",
      path: "hot_titanium_ingot",
      minecraft_tags: ["forge:hot_ingots/titanium"],
    })));
    expect(hotIngot[0]!).toMatchObject({ value: "titanium" });

    const ring = materialFamilyRule.run(ctx(record({
      id: "gtceu:hsss_ring",
      namespace: "gtceu",
      path: "hsss_ring",
      minecraft_tags: ["forge:rings/hsss"],
    })));
    expect(ring[0]!).toMatchObject({ value: "hsss" });

    const doubleIngot = materialFamilyRule.run(ctx(record({
      id: "gtceu:brass_double_ingot",
      namespace: "gtceu",
      path: "brass_double_ingot",
      minecraft_tags: ["forge:double_ingots/brass"],
    })));
    expect(doubleIngot[0]!).toMatchObject({ value: "brass" });

    const densePlate = materialFamilyRule.run(ctx(record({
      id: "gtceu:dense_rocket_alloy_t1_plate",
      namespace: "gtceu",
      path: "dense_rocket_alloy_t1_plate",
      minecraft_tags: ["forge:dense_plates/rocket_alloy_t1"],
    })));
    expect(densePlate[0]!).toMatchObject({ value: "rocket_alloy_t1" });

    const richRaw = materialFamilyRule.run(ctx(record({
      id: "gtceu:rich_raw_monazite",
      namespace: "gtceu",
      path: "rich_raw_monazite",
      minecraft_tags: ["forge:rich_raw_materials/monazite"],
    })));
    expect(richRaw[0]!).toMatchObject({ value: "monazite" });

    const smallDust = materialFamilyRule.run(ctx(record({
      id: "gtceu:small_black_steel_dust",
      namespace: "gtceu",
      path: "small_black_steel_dust",
      display_name: "Small Pile of Black Steel Dust",
      minecraft_tags: ["forge:small_dusts/black_steel"],
    })));
    expect(smallDust[0]!).toMatchObject({
      value: "black_steel",
      source: "rule:material_family_from_common_tag",
    });

    const glass = materialFamilyRule.run(ctx(record({
      id: "minecraft:tinted_glass",
      path: "tinted_glass",
      minecraft_tags: ["forge:glass/tinted"],
    })));
    expect(glass[0]!).toMatchObject({ value: "glass" });
  });

  test("structured metal paths and material-bearing display names derive family", () => {
    const woodRoofing = materialFamilyRule.run(ctx(record({
      id: "firmacivplus:wood/cypress_roofing",
      namespace: "firmacivplus",
      path: "wood/cypress_roofing",
      display_name: "Cypress Roofing",
    })));
    expect(woodRoofing[0]!).toMatchObject({
      value: "wood_cypress",
      source: "rule:material_family_from_structured_wood_path",
    });

    const woodBarrel = materialFamilyRule.run(ctx(record({
      id: "afc:wood/barrel/cypress",
      namespace: "afc",
      path: "wood/barrel/cypress",
      display_name: "Cypress Barrel",
    })));
    expect(woodBarrel[0]!).toMatchObject({ value: "wood_cypress" });

    const knifeBlade = materialFamilyRule.run(ctx(record({
      id: "tfc:metal/knife_blade/copper",
      namespace: "tfc",
      path: "metal/knife_blade/copper",
      display_name: "Copper Knife Blade",
    })));
    expect(knifeBlade[0]!).toMatchObject({
      value: "copper",
      source: "rule:material_family_from_structured_metal_path",
    });

    const trainHull = materialFamilyRule.run(ctx(record({
      id: "createdeco:andesite_hull",
      namespace: "createdeco",
      path: "andesite_hull",
      display_name: "Tin Alloy Train Hull",
    })));
    expect(trainHull[0]!).toMatchObject({
      value: "tin_alloy",
      source: "rule:material_family_from_display_name",
    });

    const mixer = materialFamilyRule.run(ctx(record({
      id: "greate:tungsten_steel_mechanical_mixer",
      namespace: "greate",
      path: "tungsten_steel_mechanical_mixer",
      display_name: "Tungsten Steel Mechanical Mixer",
    })));
    expect(mixer[0]!).toMatchObject({ value: "tungsten_steel" });

    const greenhouse = materialFamilyRule.run(ctx(record({
      id: "firmalife:iron_greenhouse_panel_wall",
      namespace: "firmalife",
      path: "iron_greenhouse_panel_wall",
      display_name: "Iron Greenhouse Panel Wall",
    })));
    expect(greenhouse[0]!).toMatchObject({ value: "iron" });

    const crushingWheel = materialFamilyRule.run(ctx(record({
      id: "greate:neutronium_crushing_wheel",
      namespace: "greate",
      path: "neutronium_crushing_wheel",
      display_name: "Neutronium Crushing Wheel",
    })));
    expect(crushingWheel[0]!).toMatchObject({ value: "neutronium" });

    const mortar = materialFamilyRule.run(ctx(record({
      id: "gtceu:bismuth_bronze_mortar",
      namespace: "gtceu",
      path: "bismuth_bronze_mortar",
      display_name: "Bismuth Bronze Mortar",
    })));
    expect(mortar[0]!).toMatchObject({ value: "bismuth_bronze" });

    const travertine = materialFamilyRule.run(ctx(record({
      id: "create:cut_dripstone_brick_slab",
      namespace: "create",
      path: "cut_dripstone_brick_slab",
      display_name: "Cut Travertine Brick Slab",
      minecraft_tags: ["create:stone_types/dripstone_half"],
    })));
    expect(travertine[0]!).toMatchObject({
      value: "travertine",
      source: "rule:material_family_from_display_name",
    });

    const remappedStone = materialFamilyRule.run(ctx(record({
      id: "create:polished_cut_granite",
      namespace: "create",
      path: "polished_cut_granite",
      display_name: "Polished Cut Chert",
      minecraft_tags: ["create:stone_types/granite", "tfg:stone_types/chert"],
    })));
    expect(remappedStone[0]!).toMatchObject({ value: "chert" });

    const stoneTag = materialFamilyRule.run(ctx(record({
      id: "create:scorchia_pillar",
      namespace: "create",
      path: "scorchia_pillar",
      display_name: "Scorchia Pillar",
      minecraft_tags: ["create:stone_types/scorchia"],
    })));
    expect(stoneTag[0]!).toMatchObject({ value: "scorchia" });

    const gearbox = materialFamilyRule.run(ctx(record({
      id: "create_connected:brass_gearbox",
      namespace: "create_connected",
      path: "brass_gearbox",
      display_name: "Brass Gearbox",
    })));
    expect(gearbox[0]!).toMatchObject({ value: "brass" });

    const sheet = materialFamilyRule.run(ctx(record({
      id: "vintageimprovements:refined_glowstone_sheet",
      namespace: "vintageimprovements",
      path: "refined_glowstone_sheet",
      display_name: "Refined Glowstone Sheet",
    })));
    expect(sheet[0]!).toMatchObject({ value: "refined_glowstone" });

    const indicator = materialFamilyRule.run(ctx(record({
      id: "gtceu:sodalite_indicator",
      namespace: "gtceu",
      path: "sodalite_indicator",
      display_name: "Sodalite Indicator",
    })));
    expect(indicator[0]!).toMatchObject({ value: "sodalite" });

    const fluixSword = materialFamilyRule.run(ctx(record({
      id: "ae2:fluix_sword",
      namespace: "ae2",
      path: "fluix_sword",
      display_name: "Fluix Sword",
    })));
    expect(fluixSword[0]!).toMatchObject({ value: "fluix" });
  });

  test("exact copper and glass blocks derive family", () => {
    const oxidizedCopper = materialFamilyRule.run(ctx(record({
      id: "minecraft:oxidized_copper",
      path: "oxidized_copper",
    })));
    expect(oxidizedCopper[0]!).toMatchObject({ value: "copper" });

    const tintedGlass = materialFamilyRule.run(ctx(record({
      id: "minecraft:tinted_glass",
      path: "tinted_glass",
    })));
    expect(tintedGlass[0]!).toMatchObject({ value: "glass" });
  });

  test("no signal → no output", () => {
    const out = materialFamilyRule.run(ctx(record({
      id: "minecraft:mystery",
      path: "mystery",
    })));
    expect(out).toEqual([]);
  });
});

describe("formRule", () => {
  test("stairs tag", () => {
    const out = formRule.run(ctx(record({
      id: "minecraft:oak_stairs",
      path: "oak_stairs",
      minecraft_tags: ["minecraft:stairs", "minecraft:wooden_stairs"],
    })));
    expect(out[0]!).toMatchObject({ facet: "form", value: "stairs" });
  });

  test("stripped log", () => {
    const out = formRule.run(ctx(record({
      id: "minecraft:stripped_oak_log",
      path: "stripped_oak_log",
    })));
    expect(out[0]!).toMatchObject({ value: "stripped_log" });
  });

  test("log suffix", () => {
    const out = formRule.run(ctx(record({
      id: "minecraft:oak_log",
      path: "oak_log",
    })));
    expect(out[0]!).toMatchObject({ value: "log" });
  });

  test("ingot suffix", () => {
    const out = formRule.run(ctx(record({
      id: "minecraft:iron_ingot",
      path: "iron_ingot",
    })));
    expect(out[0]!).toMatchObject({ value: "ingot" });
  });

  test("plate suffix", () => {
    const out = formRule.run(ctx(record({
      id: "gtceu:carbon_fiber_plate",
      namespace: "gtceu",
      path: "carbon_fiber_plate",
    })));
    expect(out[0]!).toMatchObject({ value: "plate" });
  });

  test("common tag roots normalize loader tag conventions", () => {
    const forgeIngot = formRule.run(ctx(record({
      id: "tfc:metal/ingot/copper",
      path: "metal/ingot/copper",
      minecraft_tags: ["forge:ingots/copper"],
    })));
    expect(forgeIngot[0]!).toMatchObject({ value: "ingot" });

    const cRaw = formRule.run(ctx(record({
      id: "modernmod:raw_tin",
      path: "raw_tin",
      minecraft_tags: ["c:raw_materials/tin"],
    })));
    expect(cRaw[0]!).toMatchObject({ value: "raw" });

    const storageBlock = formRule.run(ctx(record({
      id: "gtceu:nichrome_block",
      path: "nichrome_block",
      minecraft_tags: ["forge:storage_blocks/nichrome"],
    })));
    expect(storageBlock[0]!).toMatchObject({ value: "storage_block" });
  });

  test("window suffix maps to pane (Create's _window blocks)", () => {
    const out = formRule.run(ctx(record({
      id: "create:dark_oak_window",
      path: "dark_oak_window",
    })));
    expect(out[0]!).toMatchObject({ value: "pane" });
  });

  test("glass_pane suffix maps to pane", () => {
    const out = formRule.run(ctx(record({
      id: "minecraft:black_stained_glass_pane",
      path: "black_stained_glass_pane",
    })));
    expect(out[0]!).toMatchObject({ value: "pane" });
  });

  test("full stained glass blocks are not panes", () => {
    const out = formRule.run(ctx(record({
      id: "minecraft:black_stained_glass",
      path: "black_stained_glass",
    })));
    expect(out).toEqual([]);
  });

  test("bare 'glass' id is not a pane", () => {
    const out = formRule.run(ctx(record({
      id: "minecraft:glass",
      path: "glass",
    })));
    expect(out).toEqual([]);
  });

  test("glass suffix alone is not enough to infer pane form", () => {
    const out = formRule.run(ctx(record({
      id: "create:framed_glass",
      path: "framed_glass",
    })));
    expect(out).toEqual([]);
  });

  test("TFG smooth stained glass stays out of pane/window form", () => {
    const out = formRule.run(ctx(record({
      id: "tfg:glass/smooth_lime_stained_glass",
      namespace: "tfg",
      path: "glass/smooth_lime_stained_glass",
    })));
    expect(out).toEqual([]);
  });

  test("ore paths infer form, but non-block _ore items do not", () => {
    const orePath = formRule.run(ctx(record({
      id: "firmalife:ore/poor_chromite/quartzite",
      namespace: "firmalife",
      path: "ore/poor_chromite/quartzite",
      extractor_meta: { is_block_item: true },
    })));
    expect(orePath[0]!).toMatchObject({ value: "ore" });

    const refinedOreItem = formRule.run(ctx(record({
      id: "greate:refined_rose_quartz_ore",
      namespace: "greate",
      path: "refined_rose_quartz_ore",
      extractor_meta: { is_block_item: false },
    })));
    expect(refinedOreItem).toEqual([]);
  });

  test("structured tool paths and block fallback infer form", () => {
    const stoneShovel = formRule.run(ctx(record({
      id: "tfc:stone/shovel/sedimentary",
      namespace: "tfc",
      path: "stone/shovel/sedimentary",
    })));
    expect(stoneShovel[0]!).toMatchObject({ value: "tool" });

    const machineBlock = formRule.run(ctx(record({
      id: "gtceu:hv_packer",
      namespace: "gtceu",
      path: "hv_packer",
      extractor_meta: { is_block_item: true },
    })));
    expect(machineBlock[0]!).toMatchObject({
      value: "whole_block",
      source: "rule:form_from_block_item",
    });
  });

  test("exact id override for projectile", () => {
    const out = formRule.run(ctx(record({
      id: "minecraft:arrow",
      path: "arrow",
    })));
    expect(out[0]!).toMatchObject({ value: "projectile" });
  });
});

describe("equipSlotRule", () => {
  test("head armor", () => {
    const out = equipSlotRule.run(ctx(record({
      component_data: { "minecraft:equippable": { slot: "head" } },
    })));
    expect(out[0]!).toMatchObject({ value: "head" });
  });

  test("llama body", () => {
    const out = equipSlotRule.run(ctx(record({
      component_data: {
        "minecraft:equippable": {
          slot: "body",
          allowed_entities: ["minecraft:llama", "minecraft:trader_llama"],
        },
      },
    })));
    expect(out[0]!).toMatchObject({ value: "llama_carpet" });
  });

  test("missing component → no output", () => {
    expect(equipSlotRule.run(ctx(record({})))).toEqual([]);
  });
});

describe("processingInRule", () => {
  test("maps recipe types to short verbs", () => {
    const rec = record({
      id: "minecraft:iron_ore",
      path: "iron_ore",
      recipe_role: {
        ingredient_of: ["minecraft:iron_ingot_from_smelting_iron_ore", "minecraft:iron_ingot_from_blasting_iron_ore"],
        output_of: [],
        in_degree: 2,
        out_degree: 0,
        ingredient_of_counts: { smelting: 1, blasting: 1 },
        output_of_counts: {},
      },
    });
    const out = processingInRule.run(ctx(rec, {
      recipeTypes: new Map([
        ["minecraft:iron_ingot_from_smelting_iron_ore", "minecraft:smelting"],
        ["minecraft:iron_ingot_from_blasting_iron_ore", "minecraft:blasting"],
      ]),
    }));
    expect(out[0]!).toMatchObject({
      facet: "processing_in",
      values: ["blasting", "smelting"],
      kind: "multi",
    });
  });

  test("unknown recipe type passes through", () => {
    const rec = record({
      recipe_role: { ingredient_of: ["create:milling_xyz"], output_of: [], in_degree: 1, out_degree: 0, ingredient_of_counts: {}, output_of_counts: {} },
    });
    const out = processingInRule.run(ctx(rec, {
      recipeTypes: new Map([["create:milling_xyz", "create:milling"]]),
    }));
    expect(out[0]!).toMatchObject({ values: ["create:milling"] });
  });

  test("uses recipe type counts when recipe ids are not in the static bundle", () => {
    const rec = record({
      recipe_role: {
        ingredient_of: ["kubejs:runtime_only_recipe"],
        output_of: [],
        in_degree: 1,
        out_degree: 0,
        ingredient_of_counts: {
          crafting_shaped: 3,
          "create:deploying": 1,
          "kubejs:shapeless": 2,
        },
        output_of_counts: {},
      },
    });
    const out = processingInRule.run(ctx(rec));
    expect(out[0]!).toMatchObject({
      facet: "processing_in",
      values: ["crafting", "create:deploying", "kubejs:shapeless"],
    });
  });
});

describe("originRule", () => {
  test("classifies chest tables", () => {
    const rec = record({
      loot_table_sources: [
        "minecraft:chests/desert_pyramid",
        "minecraft:chests/abandoned_mineshaft",
        "minecraft:chests/trial_chambers/reward_common",
        "minecraft:entities/zombie",
      ],
    });
    const out = originRule.run(ctx(rec));
    expect((out[0]! as { values: string[] }).values).toEqual([
      "desert_temple",
      "mineshaft",
      "mob_drop",
      "trial_chamber",
    ]);
  });

  test("no loot → no output", () => {
    expect(originRule.run(ctx(record({})))).toEqual([]);
  });
});

describe("dyeColorRule", () => {
  test("white_wool gets white", () => {
    const out = dyeColorRule.run(ctx(record({
      id: "minecraft:white_wool",
      path: "white_wool",
      minecraft_tags: ["minecraft:wool"],
    })));
    expect(out[0]!).toMatchObject({ value: "white" });
  });

  test("light_gray_concrete via suffix fallback", () => {
    const out = dyeColorRule.run(ctx(record({
      id: "minecraft:light_gray_concrete",
      path: "light_gray_concrete",
    })));
    expect(out[0]!).toMatchObject({ value: "light_gray" });
  });

  test("white_tulip is not dyed (no tag, no suffix)", () => {
    const out = dyeColorRule.run(ctx(record({
      id: "minecraft:white_tulip",
      path: "white_tulip",
    })));
    expect(out).toEqual([]);
  });

  test("modded dyed cables, lamps, paint balls, and path-leaf variants", () => {
    const cable = dyeColorRule.run(ctx(record({
      id: "ae2:pink_smart_dense_cable",
      namespace: "ae2",
      path: "pink_smart_dense_cable",
      minecraft_tags: ["ae2:smart_dense_cable"],
    })));
    expect(cable[0]!).toMatchObject({ value: "pink" });

    const lamp = dyeColorRule.run(ctx(record({
      id: "gtceu:light_blue_lamp",
      namespace: "gtceu",
      path: "light_blue_lamp",
      minecraft_tags: ["gtceu:lamps"],
    })));
    expect(lamp[0]!).toMatchObject({ value: "light_blue" });

    const paintBall = dyeColorRule.run(ctx(record({
      id: "ae2:white_paint_ball",
      namespace: "ae2",
      path: "white_paint_ball",
      minecraft_tags: ["ae2:paint_balls"],
    })));
    expect(paintBall[0]!).toMatchObject({ value: "white" });

    const vase = dyeColorRule.run(ctx(record({
      id: "tfg:decorative_vase/unfired/blue",
      namespace: "tfg",
      path: "decorative_vase/unfired/blue",
      minecraft_tags: ["tfg:decorative_vases/unfired"],
    })));
    expect(vase[0]!).toMatchObject({ value: "blue" });

    const railwaysWindow = dyeColorRule.run(ctx(record({
      id: "railways:cyan_four_pane_locometal_window",
      namespace: "railways",
      path: "cyan_four_pane_locometal_window",
      minecraft_tags: ["railways:palettes/dye_groups/four_pane_window"],
    })));
    expect(railwaysWindow[0]!).toMatchObject({ value: "cyan" });

    const dyedArmor = dyeColorRule.run(ctx(record({
      id: "wan_ancient_beasts:red_charger_armor",
      namespace: "wan_ancient_beasts",
      path: "red_charger_armor",
      minecraft_tags: ["wan_ancient_beasts:charger_armors"],
      recipe_role: {
        ingredient_of: [],
        output_of: ["tfg:barrel/dyeing/charger_armor_red"],
        in_degree: 0,
        out_degree: 1,
        ingredient_of_counts: {},
        output_of_counts: { "tfc:barrel_sealed": 1 },
      },
    })));
    expect(dyedArmor[0]!).toMatchObject({ value: "red" });

    const hammock = dyeColorRule.run(ctx(record({
      id: "comforts:hammock_green",
      namespace: "comforts",
      path: "hammock_green",
      minecraft_tags: ["comforts:hammocks"],
    })));
    expect(hammock[0]!).toMatchObject({ value: "green" });

    const chalk = dyeColorRule.run(ctx(record({
      id: "chalk:cyan_chalk",
      namespace: "chalk",
      path: "cyan_chalk",
      minecraft_tags: ["forge:dyes", "forge:dyes/cyan", "chalk:chalks"],
    })));
    expect(chalk[0]!).toMatchObject({ value: "cyan" });

    const roof = dyeColorRule.run(ctx(record({
      id: "mcw_tfc_aio:roofs/magenta_concrete_roofs/magenta_concrete_lower_roof",
      namespace: "mcw_tfc_aio",
      path: "roofs/magenta_concrete_roofs/magenta_concrete_lower_roof",
    })));
    expect(roof[0]!).toMatchObject({ value: "magenta" });

    const awning = dyeColorRule.run(ctx(record({
      id: "mcw_tfc_aio:roofs/awnings/light_gray_striped_awning",
      namespace: "mcw_tfc_aio",
      path: "roofs/awnings/light_gray_striped_awning",
    })));
    expect(awning[0]!).toMatchObject({ value: "light_gray" });

    const pouredGlass = dyeColorRule.run(ctx(record({
      id: "tfc:red_poured_glass",
      namespace: "tfc",
      path: "red_poured_glass",
    })));
    expect(pouredGlass[0]!).toMatchObject({ value: "red" });
  });
});

describe("requiredToolRule", () => {
  test("pickaxe + iron tier from block tags", () => {
    const out = requiredToolRule.run(ctx(record({
      id: "minecraft:diamond_ore",
      path: "diamond_ore",
    }), {
      blockTagClosure: new Map([
        ["minecraft:diamond_ore", ["minecraft:mineable/pickaxe", "minecraft:needs_iron_tool"]],
      ]),
    }));
    const byFacet = new Map(out.map((o) => [o.facet, o]));
    expect(byFacet.get("required_tool")).toMatchObject({ value: "pickaxe" });
    expect(byFacet.get("required_tool_tier")).toMatchObject({ value: "iron" });
  });

  test("no block tags → no output", () => {
    expect(requiredToolRule.run(ctx(record({})))).toEqual([]);
  });

  test("ore path block falls back to pickaxe when block tags are missing", () => {
    const out = requiredToolRule.run(ctx(record({
      id: "tfc:ore/normal_limonite/diorite",
      namespace: "tfc",
      path: "ore/normal_limonite/diorite",
      extractor_meta: { is_block_item: true },
    })));
    expect(out[0]!).toMatchObject({
      facet: "required_tool",
      value: "pickaxe",
      source: "rule:required_tool_from_ore_path",
    });
  });

  test("runtime exporter block tags drive required tool when closure is absent", () => {
    const out = requiredToolRule.run(ctx(record({
      id: "create_connected:charged_kinetic_battery",
      namespace: "create_connected",
      path: "charged_kinetic_battery",
      extractor_meta: {
        is_block_item: true,
        block_tags: ["minecraft:mineable/axe", "minecraft:mineable/pickaxe"],
      },
    })));
    expect(out[0]!).toMatchObject({
      facet: "required_tool",
      value: "pickaxe",
      source: "rule:required_tool_from_block_tag",
    });
  });
});

describe("booleansRule", () => {
  test("stackable + durability + enchantable + nbt", () => {
    const out = booleansRule.run(ctx(record({
      path: "shulker_box",
      component_data: {
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 100,
        "minecraft:enchantable": { value: 1 },
        "minecraft:container": [],
      },
    }), {
      bundle: {
        registries: {}, itemComponents: {}, recipes: {}, lootTables: {},
        itemTags: {}, blockTags: {}, itemDefinitions: {}, models: {},
        lang: {}, blocks: { shulker_box: {} }, version: "t",
      },
    }));
    const facets = new Set(out.map((o) => o.facet));
    expect(facets).toContain("has_durability");
    expect(facets).toContain("has_enchantments");
    expect(facets).toContain("has_nbt_variation");
    expect(facets).toContain("is_block_item");
    // stack size is 1, so is_stackable should NOT fire
    expect(facets).not.toContain("is_stackable");
  });

});

describe("isFuelRule", () => {
  test("semantic fuel tags set is_fuel", () => {
    const out = isFuelRule.run(ctx(record({
      id: "afc:wood/twig/cypress",
      namespace: "afc",
      path: "wood/twig/cypress",
      minecraft_tags: ["tfc:firepit_fuel"],
    })));
    expect(out).toContainEqual(expect.objectContaining({
      facet: "is_fuel",
      value: true,
      source: "rule:is_fuel_from_id_or_tag",
    }));
  });
});

describe("emitsLightRule", () => {
  test("lamp tags set emits_light", () => {
    const out = emitsLightRule.run(ctx(record({
      id: "simplylight:illuminant_black_block",
      namespace: "simplylight",
      path: "illuminant_black_block",
      minecraft_tags: ["simplylight:any_lamp_off"],
    })));
    expect(out[0]!).toMatchObject({
      facet: "emits_light",
      value: true,
      source: "rule:emits_light_from_tag",
    });
  });

  test("candle suffix sets emits_light", () => {
    const out = emitsLightRule.run(ctx(record({
      id: "species:wicked_candle",
      namespace: "species",
      path: "wicked_candle",
    })));
    expect(out[0]!).toMatchObject({
      facet: "emits_light",
      value: true,
      source: "rule:emits_light_from_id_suffix",
    });
  });
});

describe("runDeterministic integration", () => {
  test("merges facets across rules and emits layer file", () => {
    const rec: ItemExtractRecord = record({
      id: "minecraft:iron_ingot",
      path: "iron_ingot",
      minecraft_tags: ["minecraft:iron_tool_materials"],
      component_data: { "minecraft:max_stack_size": 64, "minecraft:rarity": "common" },
    });
    const bundle: SummaryBundle = {
      registries: { item: ["iron_ingot"] },
      itemComponents: { iron_ingot: {} },
      recipes: {},
      lootTables: {},
      itemTags: {},
      blockTags: {},
      itemDefinitions: {},
      models: {},
      lang: {},
      blocks: {},
      version: "t",
    };
    const { layer, coverage, warnings } = runDeterministic({
      records: [rec],
      bundle,
      namespace: "minecraft",
    });
    expect(layer.schema_version).toBe(1);
    expect(layer.layer).toBe("vanilla-base");
    expect(layer.entries["minecraft:iron_ingot"]).toBeDefined();
    const facets = layer.entries["minecraft:iron_ingot"]!.facets;
    expect(facets.mod_namespace).toMatchObject({ value: "minecraft" });
    expect(facets.material_family).toMatchObject({ value: "iron" });
    expect(facets.is_stackable).toMatchObject({ value: true });
    expect(coverage.mod_namespace).toBe(1);
    expect(warnings).toEqual([]);
  });

  test("duplicate single-value assertion emits a warning", () => {
    const rec = record({
      id: "minecraft:iron_ingot",
      path: "iron_ingot",
      minecraft_tags: ["minecraft:iron_tool_materials"],
    });
    const { warnings } = runDeterministic({
      records: [rec],
      bundle: {
        registries: {}, itemComponents: {}, recipes: {}, lootTables: {},
        itemTags: {}, blockTags: {}, itemDefinitions: {}, models: {},
        lang: {}, blocks: {}, version: "t",
      },
      namespace: "minecraft",
      rules: [materialFamilyRule, {
        id: "material_family_dup",
        facets: ["material_family"],
        run: () => [{
          facet: "material_family",
          kind: "single",
          value: "gold",
          source: "rule:dup",
          confidence: 1,
        }],
      }],
    });
    expect(warnings.length).toBe(1);
    expect(warnings[0]!).toContain("duplicate single-value");
  });
});
