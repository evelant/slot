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
  });

  test("bloom ids use the bloom metal family", () => {
    const out = materialFamilyRule.run(ctx(record({
      id: "tfc:raw_iron_bloom",
      namespace: "tfc",
      path: "raw_iron_bloom",
    })));
    expect(out[0]!).toMatchObject({ value: "iron", source: "rule:material_family_from_bloom_id" });
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

  test("stained_glass suffix maps to pane", () => {
    const out = formRule.run(ctx(record({
      id: "minecraft:black_stained_glass",
      path: "black_stained_glass",
    })));
    expect(out[0]!).toMatchObject({ value: "pane" });
  });

  test("bare 'glass' id maps to pane", () => {
    const out = formRule.run(ctx(record({
      id: "minecraft:glass",
      path: "glass",
    })));
    expect(out[0]!).toMatchObject({ value: "pane" });
  });

  test("framed_glass suffix maps to pane", () => {
    const out = formRule.run(ctx(record({
      id: "create:framed_glass",
      path: "framed_glass",
    })));
    expect(out[0]!).toMatchObject({ value: "pane" });
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
