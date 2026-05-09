import { describe, expect, test } from "bun:test";
import { mkdirSync, mkdtempSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { extractFromModBundle } from "../src/extract/mod/extractor.ts";
import { loadJarModBundle } from "../src/extract/jar/source.ts";

function withTempDir<T>(fn: (dir: string) => T): T {
  const dir = mkdtempSync(join(tmpdir(), "slot-classification-jar-"));
  try {
    return fn(dir);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
}

describe("jar-backed mod extraction", () => {
  test("builds stage-1 records from a Forge 1.20 style jar", () => {
    withTempDir((dir) => {
      const jarPath = join(dir, "example-1.0.0.jar");
      writeZip(jarPath, {
        "META-INF/mods.toml": `
modLoader="javafml"
loaderVersion="[47,)"
license="MIT"
[[mods]]
modId="example"
version="1.0.0"
displayName="Example"
description="fixture"
`,
        "assets/example/lang/en_us.json": JSON.stringify({
          "item.example.gear": "Gear",
          "block.example.machine": "Machine",
        }),
        "assets/example/models/item/gear.json": JSON.stringify({
          parent: "minecraft:item/generated",
        }),
        "assets/example/blockstates/machine.json": "{}",
        "data/example/recipes/pressing/plate.json": JSON.stringify({
          type: "example:pressing",
          ingredient: { item: "example:gear" },
          result: { item: "example:pressed_plate" },
        }),
        "data/c/tags/items/ingots/copper.json": JSON.stringify({
          values: ["example:gear", "minecraft:copper_ingot"],
        }),
      });

      const bundle = loadJarModBundle({
        jarPath,
        modNamespace: "example",
      });
      const { records } = extractFromModBundle(bundle, "test");
      const byId = new Map(records.map((record) => [record.id, record]));

      expect(bundle.version).toBe("1.0.0");
      expect([...byId.keys()]).toContain("example:gear");
      expect([...byId.keys()]).toContain("example:machine");
      expect([...byId.keys()]).toContain("example:pressed_plate");

      const gear = byId.get("example:gear");
      expect(gear?.display_name).toBe("Gear");
      expect(gear?.minecraft_tags_direct).toContain("c:ingots/copper");
      expect(gear?.recipe_role.ingredient_of).toContain("example:pressing/plate");
      expect(gear?.model_parents).toEqual(["item/gear", "item/generated"]);

      const plate = byId.get("example:pressed_plate");
      expect(plate?.recipe_role.output_of).toContain("example:pressing/plate");
    });
  });
});

function writeZip(path: string, files: Record<string, string | Buffer>): void {
  const locals: Buffer[] = [];
  const centrals: Buffer[] = [];
  let offset = 0;

  for (const [name, content] of Object.entries(files)) {
    const nameBytes = Buffer.from(name);
    const data = Buffer.isBuffer(content) ? content : Buffer.from(content);

    const local = Buffer.alloc(30);
    local.writeUInt32LE(0x04034b50, 0);
    local.writeUInt16LE(20, 4);
    local.writeUInt16LE(0, 6);
    local.writeUInt16LE(0, 8);
    local.writeUInt32LE(0, 10);
    local.writeUInt32LE(0, 14);
    local.writeUInt32LE(data.length, 18);
    local.writeUInt32LE(data.length, 22);
    local.writeUInt16LE(nameBytes.length, 26);
    local.writeUInt16LE(0, 28);
    locals.push(local, nameBytes, data);

    const central = Buffer.alloc(46);
    central.writeUInt32LE(0x02014b50, 0);
    central.writeUInt16LE(20, 4);
    central.writeUInt16LE(20, 6);
    central.writeUInt16LE(0, 8);
    central.writeUInt16LE(0, 10);
    central.writeUInt32LE(0, 12);
    central.writeUInt32LE(0, 16);
    central.writeUInt32LE(data.length, 20);
    central.writeUInt32LE(data.length, 24);
    central.writeUInt16LE(nameBytes.length, 28);
    central.writeUInt16LE(0, 30);
    central.writeUInt16LE(0, 32);
    central.writeUInt16LE(0, 34);
    central.writeUInt16LE(0, 36);
    central.writeUInt32LE(0, 38);
    central.writeUInt32LE(offset, 42);
    centrals.push(central, nameBytes);

    offset += local.length + nameBytes.length + data.length;
  }

  const centralDirectory = Buffer.concat(centrals);
  const centralOffset = offset;
  const localData = Buffer.concat(locals);
  const eocd = Buffer.alloc(22);
  eocd.writeUInt32LE(0x06054b50, 0);
  eocd.writeUInt16LE(0, 4);
  eocd.writeUInt16LE(0, 6);
  eocd.writeUInt16LE(Object.keys(files).length, 8);
  eocd.writeUInt16LE(Object.keys(files).length, 10);
  eocd.writeUInt32LE(centralDirectory.length, 12);
  eocd.writeUInt32LE(centralOffset, 16);
  eocd.writeUInt16LE(0, 20);

  writeFileSync(path, Buffer.concat([localData, centralDirectory, eocd]));
}
