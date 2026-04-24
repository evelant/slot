import { describe, test, expect } from "bun:test";
import { resolveModelParents } from "../src/extract/models.ts";

describe("resolveModelParents", () => {
  test("follows parent chain", () => {
    const models = {
      "item/iron_ingot": { parent: "minecraft:item/generated" },
      "item/generated": { parent: "builtin/generated" },
      "builtin/generated": {},
    };
    const chain = resolveModelParents(
      { model: { type: "minecraft:model", model: "minecraft:item/iron_ingot" } },
      models,
    );
    expect(chain).toEqual(["item/iron_ingot", "item/generated", "builtin/generated"]);
  });

  test("unwraps range_dispatch cases to find first model", () => {
    const models = {
      "item/diamond": { parent: "minecraft:item/generated" },
      "item/generated": {},
    };
    const chain = resolveModelParents(
      {
        model: {
          type: "minecraft:range_dispatch",
          entries: [
            {
              threshold: 1,
              model: { type: "minecraft:model", model: "minecraft:item/diamond" },
            },
          ],
        },
      },
      models,
    );
    expect(chain).toEqual(["item/diamond", "item/generated"]);
  });

  test("unwraps condition on_true/on_false", () => {
    const models = { "item/x": {} };
    const chain = resolveModelParents(
      {
        model: {
          type: "minecraft:condition",
          on_false: { type: "minecraft:model", model: "minecraft:item/x" },
        },
      },
      models,
    );
    expect(chain).toEqual(["item/x"]);
  });

  test("handles missing definition", () => {
    expect(resolveModelParents(undefined, {})).toEqual([]);
  });

  test("minecraft:special uses base as the representative model", () => {
    const models = {
      "item/chest": { parent: "builtin/generated" },
      "builtin/generated": {},
    };
    const chain = resolveModelParents(
      {
        model: {
          type: "minecraft:special",
          base: "minecraft:item/chest",
          model: { type: "minecraft:chest", texture: "minecraft:normal" },
        },
      },
      models,
    );
    expect(chain).toEqual(["item/chest", "builtin/generated"]);
  });

  test("minecraft:composite picks the first child model", () => {
    const models = {
      "block/black_bed_head": { parent: "block/block" },
      "block/block": {},
    };
    const chain = resolveModelParents(
      {
        model: {
          type: "minecraft:composite",
          models: [
            { type: "minecraft:model", model: "minecraft:block/black_bed_head" },
            { type: "minecraft:model", model: "minecraft:block/black_bed_foot" },
          ],
        },
      },
      models,
    );
    expect(chain).toEqual(["block/black_bed_head", "block/block"]);
  });

  test("breaks cycles", () => {
    const models = {
      "item/a": { parent: "minecraft:item/b" },
      "item/b": { parent: "minecraft:item/a" },
    };
    const chain = resolveModelParents(
      { model: { type: "minecraft:model", model: "minecraft:item/a" } },
      models,
    );
    expect(chain).toEqual(["item/a", "item/b"]);
  });
});
