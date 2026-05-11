import { describe, expect, test } from "bun:test";
import { createHash } from "node:crypto";
import { existsSync, mkdirSync, mkdtempSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join, resolve } from "node:path";

const TOOL_ROOT = resolve(import.meta.dir, "..");

function runCli(args: string[]) {
  return Bun.spawnSync({
    cmd: [process.execPath, "run", "src/cli.ts", ...args],
    cwd: TOOL_ROOT,
    stdout: "pipe",
    stderr: "pipe",
  });
}

describe("cli option validation", () => {
  test("rejects zero batch size before running extractors", () => {
    const result = runCli([
      "classify",
      "--mod",
      "minecraft",
      "--source",
      "does-not-matter",
      "--batch-size",
      "0",
    ]);

    expect(result.exitCode).not.toBe(0);
    expect(result.stderr.toString()).toContain("--batch-size must be a positive integer");
  });

  test("rejects retry thresholds outside the confidence range", () => {
    const result = runCli([
      "classify",
      "--mod",
      "minecraft",
      "--source",
      "does-not-matter",
      "--retry-threshold",
      "2",
    ]);

    expect(result.exitCode).not.toBe(0);
    expect(result.stderr.toString()).toContain("--retry-threshold must be a number between 0 and 1");
  });

  test("rejects zero mod concurrency instead of silently clamping it", () => {
    const result = runCli(["classify-modpack", "missing.json", "--mod-concurrency", "0"]);

    expect(result.exitCode).not.toBe(0);
    expect(result.stderr.toString()).toContain("--mod-concurrency must be a positive integer");
  });

  test("validate-vocabulary accepts scoped pack vocabulary artifacts", () => {
    withTempDir((dir) => {
      const vocabularyPath = join(dir, "fixture.facet-vocabulary.json");
      writeFileSync(vocabularyPath, JSON.stringify({
        schema_version: 1,
        kind: "slot-pack-facet-vocabulary",
        pack_id: "fixture",
        facets: {
          activity: {
            values: {
              "slot:cooking": {
                label: "Cooking",
                origin: "universal_default",
                state: "accepted",
              },
            },
          },
          workflow: {
            values: {
              "pack:fixture/steelmaking": {
                label: "Steelmaking",
                origin: "pack_generated",
                state: "accepted",
                evidence: [{ kind: "recipe_type", id: "gtceu:alloy_smelter" }],
              },
            },
          },
        },
      }));

      const result = runCli(["validate-vocabulary", vocabularyPath]);

      expect(result.exitCode).toBe(0);
      expect(result.stdout.toString()).toContain("ok:");
    });
  });

  test("classify-folder runs stage 1 and 2 directly from a mods folder", () => {
    withTempDir((dir) => {
      const mods = join(dir, "mods");
      const out = join(dir, "out");
      mkdirSync(mods, { recursive: true });
      writeZip(join(mods, "example.jar"), {
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
        }),
        "assets/example/models/item/gear.json": JSON.stringify({
          parent: "minecraft:item/generated",
        }),
      });

      const result = runCli([
        "classify-folder",
        "--mods",
        mods,
        "--out",
        out,
        "--stages",
        "1,2",
        "--mod",
        "example",
      ]);

      expect(result.exitCode).toBe(0);
      expect(existsSync(join(out, "scan-report.json"))).toBe(true);
      expect(readFileSync(join(out, "example.items.ndjson"), "utf8")).toContain("example:gear");
      const layer = JSON.parse(readFileSync(join(out, "example.facets.partial.json"), "utf8")) as {
        metadata?: { input?: Record<string, unknown> };
        entries?: Record<string, unknown>;
      };
      expect(layer.metadata?.input?.source_kind).toBe("jar");
      expect(layer.metadata?.input?.file_name).toBe("example.jar");
      expect(layer.entries?.["example:gear"]).toBeDefined();
    });
  });

  test("generate-pack-layer merges runtime export with static jar data and writes datapack", () => {
    withTempDir((dir) => {
      const mods = join(dir, "mods");
      const out = join(dir, "out");
      const runtimeItems = join(dir, "fixture.runtime-items.ndjson");
      const runtimeSummary = join(dir, "fixture.runtime-summary.json");
      mkdirSync(mods, { recursive: true });
      writeZip(join(mods, "example.jar"), {
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
        }),
        "assets/example/models/item/gear.json": JSON.stringify({
          parent: "minecraft:item/generated",
        }),
      });
      writeFileSync(runtimeItems, JSON.stringify({
        id: "example:gear",
        namespace: "example",
        path: "gear",
        display_name: "Runtime Gear",
        minecraft_tags: [],
        minecraft_tags_direct: [],
        recipe_role: {
          ingredient_of: [],
          output_of: [],
          in_degree: 0,
          out_degree: 0,
          ingredient_of_counts: {},
          output_of_counts: {},
        },
        model_parents: [],
        loot_table_sources: [],
        creative_tabs: [],
        component_data: { "minecraft:max_stack_size": 64 },
        extractor_meta: {
          extractor: "slot-runtime-export",
          item_tag_membership: "resolved_runtime",
          direct_item_tags_available: false,
        },
      }) + "\n");
      writeFileSync(runtimeSummary, JSON.stringify({
        schema_version: 1,
        format: "slot-runtime-classification-export",
        pack_id: "fixture",
        loader: "forge",
        minecraft_version: "1.20.1",
        item_count: 1,
        item_tag_members: {},
        block_tag_members: {},
      }));

      const result = runCli([
        "generate-pack-layer",
        "--runtime-export",
        runtimeItems,
        "--summary",
        runtimeSummary,
        "--mods",
        mods,
        "--out",
        out,
        "--stages",
        "1,2",
        "--datapack",
      ]);

      expect(result.exitCode).toBe(0);
      const layerPath = join(out, "fixture.pack.facets.partial.json");
      const datapackLayer = join(out, "fixture.classification-datapack", "data", "slot", "classification", "layers", "fixture.json");
      expect(existsSync(layerPath)).toBe(true);
      expect(existsSync(datapackLayer)).toBe(true);
      expect(existsSync(join(out, "fixture.classification-datapack", "pack.mcmeta"))).toBe(true);
      const layer = JSON.parse(readFileSync(layerPath, "utf8")) as {
        layer?: string;
        source?: string;
        entries?: Record<string, { facets?: Record<string, unknown> }>;
      };
      expect(layer.layer).toBe("modpack");
      expect(layer.source).toBe("fixture");
      expect(layer.entries?.["example:gear"]?.facets?.mod_namespace).toBeDefined();
      expect(readFileSync(join(out, "fixture.pack.items.ndjson"), "utf8")).toContain("static_model_parents");
    });
  });

  test("collect-pack-facet-evidence writes runtime and optional pack evidence", () => {
    withTempDir((dir) => {
      const mods = join(dir, "mods");
      const out = join(dir, "out");
      const runtimeItems = join(dir, "fixture.runtime-items.ndjson");
      const runtimeSummary = join(dir, "fixture.runtime-summary.json");
      mkdirSync(mods, { recursive: true });
      writeZip(join(mods, "example.jar"), {
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
          "item.example.ingot_mold": "Ingot Mold",
        }),
        "assets/example/models/item/ingot_mold.json": JSON.stringify({
          parent: "minecraft:item/generated",
        }),
        "data/example/advancements/casting/start.json": JSON.stringify({
          display: {
            title: "Start Casting",
            icon: { item: "example:ingot_mold" },
          },
        }),
      });
      writeFileSync(runtimeItems, JSON.stringify({
        id: "example:ingot_mold",
        namespace: "example",
        path: "ingot_mold",
        display_name: "Ingot Mold",
        minecraft_tags: ["example:ingot_molds"],
        minecraft_tags_direct: ["example:ingot_molds"],
        recipe_role: {
          ingredient_of: ["example:casting/cast_ingot"],
          output_of: [],
          in_degree: 1,
          out_degree: 0,
          ingredient_of_counts: { "example:casting": 1 },
          output_of_counts: {},
        },
        model_parents: [],
        loot_table_sources: [],
        creative_tabs: [],
        component_data: { "minecraft:max_stack_size": 64 },
        extractor_meta: {
          extractor: "slot-runtime-export",
        },
      }) + "\n");
      writeFileSync(runtimeSummary, JSON.stringify({
        schema_version: 1,
        format: "slot-runtime-classification-export",
        pack_id: "fixture",
        loader: "forge",
        minecraft_version: "1.20.1",
        item_count: 1,
        direct_item_tags_available: true,
        item_tag_members: {
          "example:ingot_molds": ["example:ingot_mold"],
        },
        block_tag_members: {},
        recipe_type_counts: {
          "example:casting": 1,
        },
      }));

      const result = runCli([
        "collect-pack-facet-evidence",
        "--runtime-export",
        runtimeItems,
        "--summary",
        runtimeSummary,
        "--mods",
        mods,
        "--out",
        out,
      ]);

      expect(result.exitCode).toBe(0);
      const evidencePath = join(out, "fixture.facet-evidence.json");
      expect(existsSync(evidencePath)).toBe(true);
      const evidence = JSON.parse(readFileSync(evidencePath, "utf8")) as {
        kind?: string;
        records?: Array<{ kind?: string; id?: string }>;
      };
      expect(evidence.kind).toBe("slot-pack-facet-evidence");
      expect(evidence.records?.find((record) => record.kind === "recipe_type" && record.id === "example:casting")).toBeDefined();
      expect(evidence.records?.find((record) => record.kind === "advancement" && record.id === "example:casting/start")).toBeDefined();
    });
  });

  test("propose-pack-facet-vocabulary supports dry-run prompts and replay output", () => {
    withTempDir((dir) => {
      const out = join(dir, "out");
      const fixtures = join(dir, "fixtures");
      const evidencePath = join(dir, "fixture.facet-evidence.json");
      mkdirSync(fixtures, { recursive: true });
      writeFileSync(evidencePath, JSON.stringify({
        schema_version: 1,
        kind: "slot-pack-facet-evidence",
        pack_id: "fixture",
        generated_by: "test",
        generated_at: "2026-05-11T00:00:00.000Z",
        source: { runtime_items: "fixture.runtime-items.ndjson", item_count: 1 },
        records: [{
          kind: "recipe_type",
          id: "example:casting",
          label: "Casting",
          namespace: "example",
          source: "runtime-summary",
          confidence: 0.85,
          count: 4,
          item_refs: ["example:ingot_mold"],
          recipe_refs: ["example:casting/cast_ingot"],
        }],
        diagnostics: [],
      }));

      const dryRun = runCli([
        "propose-pack-facet-vocabulary",
        "--evidence",
        evidencePath,
        "--facet",
        "workflow",
        "--out",
        out,
        "--dry-run",
        "--force",
      ]);
      expect(dryRun.exitCode).toBe(0);
      const dryRunDir = join(out, "fixture.facet-vocabulary-dry-run");
      const system = readFileSync(join(dryRunDir, "workflow.system.md"), "utf8");
      const user = readFileSync(join(dryRunDir, "workflow.user.json"), "utf8");
      const hash = splitFixtureHash(system, user);
      writeFileSync(join(fixtures, `${hash}.response.json`), JSON.stringify({
        values: [{
          id: "example:casting",
          label: "Casting",
          state: "accepted",
          confidence: 0.9,
          evidence: [{ kind: "recipe_type", id: "example:casting", confidence: 0.85 }],
          seed_items: ["example:ingot_mold"],
        }],
      }));

      const replay = runCli([
        "propose-pack-facet-vocabulary",
        "--evidence",
        evidencePath,
        "--facet",
        "workflow",
        "--out",
        out,
        "--use-replay",
        "--fixture-dir",
        fixtures,
        "--force",
      ]);

      expect(replay.exitCode).toBe(0);
      const vocabulary = JSON.parse(readFileSync(join(out, "fixture.facet-vocabulary.json"), "utf8")) as {
        facets?: { workflow?: { values?: Record<string, unknown> } };
      };
      expect(vocabulary.facets?.workflow?.values?.["example:casting"]).toBeDefined();
      expect(existsSync(join(out, "fixture.facet-vocabulary.review.json"))).toBe(true);
    });
  });

  test("propose-pack-facet-vocabulary rejects unknown facets", () => {
    withTempDir((dir) => {
      const evidencePath = join(dir, "fixture.facet-evidence.json");
      writeFileSync(evidencePath, JSON.stringify({
        schema_version: 1,
        kind: "slot-pack-facet-evidence",
        pack_id: "fixture",
        generated_by: "test",
        generated_at: "2026-05-11T00:00:00.000Z",
        source: { runtime_items: "fixture.runtime-items.ndjson", item_count: 0 },
        records: [],
        diagnostics: [],
      }));

      const result = runCli([
        "propose-pack-facet-vocabulary",
        "--evidence",
        evidencePath,
        "--facet",
        "not_a_facet",
        "--dry-run",
      ]);

      expect(result.exitCode).not.toBe(0);
      expect(result.stderr.toString()).toContain("unknown vocabulary facet");
    });
  });

  test("classify-runtime-pack writes report, datapack, and zip in one command", () => {
    withTempDir((dir) => {
      const mods = join(dir, "mods");
      const out = join(dir, "out");
      const runtimeItems = join(dir, "fixture.runtime-items.ndjson");
      const runtimeSummary = join(dir, "fixture.runtime-summary.json");
      mkdirSync(mods, { recursive: true });
      writeZip(join(mods, "example.jar"), {
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
        }),
        "assets/example/models/item/gear.json": JSON.stringify({
          parent: "minecraft:item/generated",
        }),
      });
      writeFileSync(runtimeItems, JSON.stringify({
        id: "example:gear",
        namespace: "example",
        path: "gear",
        display_name: "Runtime Gear",
        minecraft_tags: [],
        minecraft_tags_direct: [],
        recipe_role: {
          ingredient_of: [],
          output_of: [],
          in_degree: 0,
          out_degree: 0,
          ingredient_of_counts: {},
          output_of_counts: {},
        },
        model_parents: [],
        loot_table_sources: [],
        creative_tabs: [],
        component_data: { "minecraft:max_stack_size": 64 },
        extractor_meta: {
          extractor: "slot-runtime-export",
          item_tag_membership: "resolved_runtime",
          direct_item_tags_available: false,
        },
      }) + "\n");
      writeFileSync(runtimeSummary, JSON.stringify({
        schema_version: 1,
        format: "slot-runtime-classification-export",
        pack_id: "fixture",
        loader: "forge",
        minecraft_version: "1.20.1",
        item_count: 1,
        item_tag_members: {},
        block_tag_members: {},
      }));

      const result = runCli([
        "classify-runtime-pack",
        "--runtime-export",
        runtimeItems,
        "--summary",
        runtimeSummary,
        "--mods",
        mods,
        "--out",
        out,
        "--stages",
        "1,2",
      ]);

      expect(result.exitCode).toBe(0);
      expect(existsSync(join(out, "fixture.run-report.json"))).toBe(true);
      expect(existsSync(join(out, "fixture.run-report.md"))).toBe(true);
      expect(existsSync(join(out, "fixture.classification-datapack.zip"))).toBe(true);
      const report = JSON.parse(readFileSync(join(out, "fixture.run-report.json"), "utf8")) as {
        output?: { datapack_zip?: string };
        coverage?: { entries?: number };
      };
      expect(report.output?.datapack_zip).toBe(join(out, "fixture.classification-datapack.zip"));
      expect(report.coverage?.entries).toBe(1);
    });
  });
});

function withTempDir<T>(fn: (dir: string) => T): T {
  const dir = mkdtempSync(join(tmpdir(), "slot-classification-cli-"));
  try {
    return fn(dir);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
}

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

function splitFixtureHash(system: string, user: string): string {
  return createHash("sha256").update(`${system}\n\n---\n\n${user}`).digest("hex").slice(0, 16);
}
