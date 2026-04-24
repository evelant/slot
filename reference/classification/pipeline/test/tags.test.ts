import { describe, test, expect } from "bun:test";
import { buildItemTagClosure } from "../src/extract/tags.ts";

describe("buildItemTagClosure", () => {
  test("direct members only", () => {
    const closure = buildItemTagClosure(
      { planks: { values: ["minecraft:oak_planks", "minecraft:birch_planks"] } },
      "minecraft",
    );
    expect(closure.get("minecraft:oak_planks")).toEqual(["minecraft:planks"]);
    expect(closure.get("minecraft:birch_planks")).toEqual(["minecraft:planks"]);
  });

  test("transitive tag references", () => {
    const closure = buildItemTagClosure(
      {
        planks: { values: ["minecraft:oak_planks"] },
        wooden_tool_materials: { values: ["#minecraft:planks"] },
        tools_all: { values: ["#minecraft:wooden_tool_materials"] },
      },
      "minecraft",
    );
    expect(closure.get("minecraft:oak_planks")).toEqual([
      "minecraft:planks",
      "minecraft:tools_all",
      "minecraft:wooden_tool_materials",
    ]);
  });

  test("cycles are broken, items still resolve", () => {
    const closure = buildItemTagClosure(
      {
        a: { values: ["#minecraft:b", "minecraft:x"] },
        b: { values: ["#minecraft:a", "minecraft:y"] },
      },
      "minecraft",
    );
    // both items belong to both tags via the cycle
    expect(closure.get("minecraft:x")?.sort()).toEqual(["minecraft:a", "minecraft:b"]);
    expect(closure.get("minecraft:y")?.sort()).toEqual(["minecraft:a", "minecraft:b"]);
  });

  test("object-form tag values with id/required", () => {
    const closure = buildItemTagClosure(
      {
        tools: {
          values: [
            { id: "minecraft:stick", required: true },
            { id: "minecraft:iron_ingot" },
          ],
        },
      },
      "minecraft",
    );
    expect(closure.get("minecraft:stick")).toEqual(["minecraft:tools"]);
    expect(closure.get("minecraft:iron_ingot")).toEqual(["minecraft:tools"]);
  });
});
