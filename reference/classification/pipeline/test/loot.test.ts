import { describe, test, expect } from "bun:test";
import { buildLootSources } from "../src/extract/loot.ts";

describe("buildLootSources", () => {
  test("walks pools and entries", () => {
    const map = buildLootSources(
      {
        "chests/simple_dungeon": {
          type: "minecraft:chest",
          pools: [
            {
              entries: [
                { type: "minecraft:item", name: "minecraft:iron_ingot" },
                { type: "minecraft:item", name: "minecraft:bread" },
              ],
            },
          ],
        },
      },
      "minecraft",
    );
    expect(map.get("minecraft:iron_ingot")).toEqual([
      "minecraft:chests/simple_dungeon",
    ]);
    expect(map.get("minecraft:bread")).toEqual([
      "minecraft:chests/simple_dungeon",
    ]);
  });

  test("walks children in alternatives/groups", () => {
    const map = buildLootSources(
      {
        "blocks/oak_log": {
          type: "minecraft:block",
          pools: [
            {
              entries: [
                {
                  type: "minecraft:alternatives",
                  children: [
                    { type: "minecraft:item", name: "minecraft:oak_log" },
                  ],
                },
              ],
            },
          ],
        },
      },
      "minecraft",
    );
    expect(map.get("minecraft:oak_log")).toEqual(["minecraft:blocks/oak_log"]);
  });

  test("walks set_contents function entries", () => {
    const map = buildLootSources(
      {
        "blocks/shulker_box": {
          type: "minecraft:block",
          pools: [
            {
              entries: [
                {
                  type: "minecraft:item",
                  name: "minecraft:shulker_box",
                  functions: [
                    {
                      function: "minecraft:set_contents",
                      entries: [
                        { type: "minecraft:item", name: "minecraft:diamond" },
                      ],
                    },
                  ],
                },
              ],
            },
          ],
        },
      },
      "minecraft",
    );
    expect(map.get("minecraft:diamond")).toEqual([
      "minecraft:blocks/shulker_box",
    ]);
  });
});
