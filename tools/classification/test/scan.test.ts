import { describe, expect, test } from "bun:test";
import { mkdirSync, mkdtempSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { scanJar } from "../src/scan/jar.ts";
import {
  loadPrismIndexMetadata,
  resolveModsFolder,
  scanModsFolder,
} from "../src/scan/mods_folder.ts";

function withTempDir<T>(fn: (dir: string) => T): T {
  const dir = mkdtempSync(join(tmpdir(), "slot-classification-scan-"));
  try {
    return fn(dir);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
}

describe("scanJar", () => {
  test("reads Forge metadata and only counts owned item candidates", () => {
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
displayName="Example Mod"
description="fixture"
`,
        "assets/example/lang/en_us.json": JSON.stringify({
          "item.example.gear": "Gear",
          "item.example.gear.tooltip": "Tooltip that is not an item",
          "block.example.machine": "Machine",
          "item.minecraft.not_owned": "External Name",
        }),
        "assets/example/models/item/gadget.json": "{}",
        "data/example/recipes/gear.json": JSON.stringify({
          type: "minecraft:crafting_shaped",
          key: { I: { item: "minecraft:iron_ingot" } },
          result: { item: "example:gear" },
        }),
        "data/example/tags/items/parts.json": JSON.stringify({
          values: ["example:rod", "minecraft:stick"],
        }),
      });

      const [entry] = scanJar({
        path: jarPath,
        bundledModIds: new Set(["example"]),
        platformMetadata: {
          minecraftVersions: ["1.20.1"],
          loaders: ["forge"],
          platformIds: { curseforge_project_id: 1, curseforge_file_id: 2 },
        },
      });

      expect(entry?.id).toBe("example");
      expect(entry?.display_name).toBe("Example Mod");
      expect(entry?.version).toBe("1.0.0");
      expect(entry?.loader).toBe("forge");
      expect(entry?.minecraft_versions).toEqual(["1.20.1"]);
      expect(entry?.status).toBe("covered:bundled");
      expect(entry?.item_candidate_count).toBe(4);
      expect(entry?.resource_counts.recipes).toBe(1);
      expect(entry?.resource_counts.item_tags).toBe(1);
      expect(entry?.resource_counts.item_models).toBe(1);
      expect(entry?.platform_ids?.curseforge_project_id).toBe(1);
    });
  });

  test("treats descriptorless zero-item jars as libraries", () => {
    withTempDir((dir) => {
      const jarPath = join(dir, "kotlinforforge.jar");
      writeZip(jarPath, {
        "META-INF/MANIFEST.MF": "Manifest-Version: 1.0\nFMLModType: LIBRARY\n",
        "kotlin/Unit.class": "",
      });

      const [entry] = scanJar({ path: jarPath });

      expect(entry?.status).toBe("skipped:library");
      expect(entry?.diagnostics).toContain("no Forge/NeoForge/Fabric mod descriptor found");
    });
  });
});

describe("mods folder scan", () => {
  test("resolves Prism instance roots and preserves local .index platform metadata", () => {
    withTempDir((dir) => {
      const instance = join(dir, "Instance");
      const mods = join(instance, "minecraft", "mods");
      const index = join(mods, ".index");
      mkdirSync(index, { recursive: true });
      mkdirSync(join(instance, "minecraft", "kubejs", "server_scripts"), { recursive: true });
      writeFileSync(join(instance, "minecraft", "kubejs", "server_scripts", "main.js"), "ServerEvents.recipes(() => {})");
      writeFileSync(
        join(instance, "instance.cfg"),
        "ManagedPackType=flame\nManagedPackName=Fixture Pack\nManagedPackVersionName=1.2.3\nManagedPackID=10\nManagedPackVersionID=20\n",
      );
      writeFileSync(
        join(instance, "mmc-pack.json"),
        JSON.stringify({
          components: [
            { uid: "net.minecraft", version: "1.20.1" },
            { uid: "net.minecraftforge", version: "47.4.13" },
          ],
        }),
      );
      writeFileSync(
        join(index, "example.pw.toml"),
        `
filename = 'example.jar'
name = 'Example Mod'
x-prismlauncher-loaders = [ 'forge' ]
x-prismlauncher-mc-versions = [ '1.20.1' ]
x-prismlauncher-version-number = 'Example 1.0.0'

[update.curseforge]
project-id = 123
file-id = 456
`,
      );
      writeZip(join(mods, "example.jar"), {
        "META-INF/mods.toml": `
modLoader="javafml"
loaderVersion="[47,)"
license="MIT"
[[mods]]
modId="example"
version="\${file.jarVersion}"
displayName="Example Mod"
description="fixture"
`,
        "assets/example/lang/en_us.json": JSON.stringify({
          "item.example.gear": "Gear",
        }),
      });

      const resolved = resolveModsFolder(instance);
      expect(resolved.modsPath).toBe(mods);
      const metadata = loadPrismIndexMetadata(mods);
      expect(metadata.get("example.jar")?.platformIds?.curseforge_file_id).toBe(456);

      const report = scanModsFolder({
        requestedPath: instance,
        generatedBy: "test",
      });

      expect(report.source.pack_name).toBe("Fixture Pack");
      expect(report.source.minecraft_version).toBe("1.20.1");
      expect(report.source.loader).toBe("forge");
      expect(report.source.pack_scripts?.kubejs?.server_scripts).toBe(1);
      expect(report.mods[0]?.version).toBe("Example 1.0.0");
      expect(report.mods[0]?.platform_ids?.curseforge_project_id).toBe(123);
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
