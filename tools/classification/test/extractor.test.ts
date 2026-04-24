import { describe, test, expect } from "bun:test";
import {
  extractFromBundle,
  VANILLA_NAMESPACE,
} from "../src/extract/vanilla/extractor.ts";
import type { SummaryBundle } from "../src/extract/vanilla/source.ts";

function bundle(): SummaryBundle {
  return {
    registries: {
      item: ["iron_ingot", "iron_ore", "iron_pickaxe"],
    },
    itemComponents: {
      iron_ingot: { "minecraft:max_stack_size": 64 },
      iron_ore: { "minecraft:max_stack_size": 64 },
      iron_pickaxe: { "minecraft:max_damage": 250 },
    },
    recipes: {
      iron_ingot_from_smelting_iron_ore: {
        type: "minecraft:smelting",
        ingredient: "minecraft:iron_ore",
        result: { id: "minecraft:iron_ingot" },
      },
      iron_pickaxe: {
        type: "minecraft:crafting_shaped",
        key: {
          "#": "minecraft:iron_ingot",
          "|": "minecraft:stick",
        },
        pattern: ["###", " | ", " | "],
        result: { id: "minecraft:iron_pickaxe" },
      },
    },
    lootTables: {
      "blocks/iron_ore": {
        type: "minecraft:block",
        pools: [
          {
            entries: [{ type: "minecraft:item", name: "minecraft:iron_ore" }],
          },
        ],
      },
    },
    itemTags: {
      iron_tool_materials: { values: ["minecraft:iron_ingot"] },
      repairs_iron_armor: { values: ["#minecraft:iron_tool_materials"] },
    },
    blockTags: {},
    itemDefinitions: {
      iron_ingot: {
        model: { type: "minecraft:model", model: "minecraft:item/iron_ingot" },
      },
      iron_ore: {
        model: { type: "minecraft:model", model: "minecraft:block/iron_ore" },
      },
      iron_pickaxe: {
        model: {
          type: "minecraft:model",
          model: "minecraft:item/iron_pickaxe",
        },
      },
    },
    models: {
      "item/iron_ingot": { parent: "minecraft:item/generated" },
      "item/iron_pickaxe": { parent: "minecraft:item/handheld" },
      "item/generated": {},
      "item/handheld": {},
      "block/iron_ore": { parent: "minecraft:block/cube_all" },
      "block/cube_all": { parent: "block/block" },
      "block/block": {},
    },
    lang: {
      en_us: {
        "item.minecraft.iron_ingot": "Iron Ingot",
        "item.minecraft.iron_pickaxe": "Iron Pickaxe",
        "block.minecraft.iron_ore": "Iron Ore",
      },
    },
    blocks: {},
    version: "test",
  };
}

describe("extractFromBundle", () => {
  test("produces one record per item, sorted by id", () => {
    const result = extractFromBundle(bundle(), "slot-classify-test");
    expect(result.records.map((r) => r.id)).toEqual([
      "minecraft:iron_ingot",
      "minecraft:iron_ore",
      "minecraft:iron_pickaxe",
    ]);
  });

  test("populates every top-level field", () => {
    const { records, meta } = extractFromBundle(bundle(), "slot-classify-test");
    const ingot = records.find((r) => r.id === "minecraft:iron_ingot")!;

    expect(ingot.namespace).toBe(VANILLA_NAMESPACE);
    expect(ingot.path).toBe("iron_ingot");
    expect(ingot.display_name).toBe("Iron Ingot");
    expect(ingot.minecraft_tags).toEqual([
      "minecraft:iron_tool_materials",
      "minecraft:repairs_iron_armor",
    ]);
    expect(ingot.recipe_role.ingredient_of).toEqual(["minecraft:iron_pickaxe"]);
    expect(ingot.recipe_role.output_of).toEqual([
      "minecraft:iron_ingot_from_smelting_iron_ore",
    ]);
    expect(ingot.model_parents).toEqual(["item/iron_ingot", "item/generated"]);
    expect(ingot.loot_table_sources).toEqual([]);
    expect(ingot.creative_tabs).toEqual([]);
    expect(ingot.component_data).toEqual({ "minecraft:max_stack_size": 64 });

    expect(meta.extractor).toBe("vanilla");
    expect(meta.source_version).toBe("test");
    expect(meta.generated_by).toBe("slot-classify-test");
  });

  test("falls back to block.<ns>.<id> lang key for block items", () => {
    const result = extractFromBundle(bundle(), "slot-classify-test");
    const ore = result.records.find((r) => r.id === "minecraft:iron_ore")!;
    expect(ore.display_name).toBe("Iron Ore");
  });

  test("items with no signals produce empty-but-typed fields", () => {
    const b = bundle();
    b.registries.item = [...b.registries.item!, "mystery"];
    const result = extractFromBundle(b, "slot-classify-test");
    const mystery = result.records.find((r) => r.id === "minecraft:mystery")!;
    expect(mystery.display_name).toBeNull();
    expect(mystery.minecraft_tags).toEqual([]);
    expect(mystery.recipe_role).toEqual({
      ingredient_of: [],
      output_of: [],
      in_degree: 0,
      out_degree: 0,
    });
    expect(mystery.model_parents).toEqual([]);
    expect(mystery.loot_table_sources).toEqual([]);
    expect(mystery.component_data).toBeNull();
  });
});
