import { describe, test, expect } from "bun:test";
import { buildRecipeRoles } from "../src/extract/recipes.ts";

describe("buildRecipeRoles", () => {
  test("shaped recipe with tag ingredients fans out", () => {
    const tagMembers = new Map<string, string[]>([
      ["minecraft:planks", ["minecraft:oak_planks", "minecraft:birch_planks"]],
    ]);
    const roles = buildRecipeRoles(
      {
        chest: {
          type: "minecraft:crafting_shaped",
          key: { "#": "#minecraft:planks" },
          pattern: ["###", "# #", "###"],
          result: { id: "minecraft:chest" },
        },
      },
      "minecraft",
      tagMembers,
    );
    expect(roles.get("minecraft:oak_planks")?.ingredient_of).toEqual(["minecraft:chest"]);
    expect(roles.get("minecraft:birch_planks")?.ingredient_of).toEqual(["minecraft:chest"]);
    expect(roles.get("minecraft:chest")?.output_of).toEqual(["minecraft:chest"]);
    expect(roles.get("minecraft:chest")?.out_degree).toBe(1);
  });

  test("shapeless recipe with array of ingredients", () => {
    const roles = buildRecipeRoles(
      {
        stick: {
          type: "minecraft:crafting_shapeless",
          ingredients: ["minecraft:oak_planks", "minecraft:oak_planks"],
          result: { id: "minecraft:stick", count: 4 },
        },
      },
      "minecraft",
      new Map(),
    );
    expect(roles.get("minecraft:oak_planks")?.in_degree).toBe(1);
    expect(roles.get("minecraft:stick")?.out_degree).toBe(1);
  });

  test("smelting recipe with scalar ingredient and result.id", () => {
    const roles = buildRecipeRoles(
      {
        iron_from_ore: {
          type: "minecraft:smelting",
          ingredient: "minecraft:iron_ore",
          result: { id: "minecraft:iron_ingot" },
        },
      },
      "minecraft",
      new Map(),
    );
    expect(roles.get("minecraft:iron_ore")?.ingredient_of).toEqual(["minecraft:iron_from_ore"]);
    expect(roles.get("minecraft:iron_ingot")?.output_of).toEqual(["minecraft:iron_from_ore"]);
  });

  test("smithing template pulls base + addition + template", () => {
    const roles = buildRecipeRoles(
      {
        netherite_axe: {
          type: "minecraft:smithing_transform",
          base: "minecraft:diamond_axe",
          addition: "minecraft:netherite_ingot",
          template: "minecraft:netherite_upgrade_smithing_template",
          result: { id: "minecraft:netherite_axe" },
        },
      },
      "minecraft",
      new Map(),
    );
    expect(roles.get("minecraft:diamond_axe")?.in_degree).toBe(1);
    expect(roles.get("minecraft:netherite_ingot")?.in_degree).toBe(1);
    expect(roles.get("minecraft:netherite_upgrade_smithing_template")?.in_degree).toBe(1);
    expect(roles.get("minecraft:netherite_axe")?.out_degree).toBe(1);
  });

  test("bare string result is accepted", () => {
    const roles = buildRecipeRoles(
      {
        r: { type: "x", ingredient: "minecraft:a", result: "minecraft:b" },
      },
      "minecraft",
      new Map(),
    );
    expect(roles.get("minecraft:b")?.out_degree).toBe(1);
  });
});
