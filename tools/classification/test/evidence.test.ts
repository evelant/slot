import { describe, expect, test } from "bun:test";
import { existsSync, mkdirSync, mkdtempSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import type { ItemExtractRecord } from "../src/extract/record.ts";
import {
  buildFacetEvidenceArtifact,
  collectExternalFacetEvidence,
} from "../src/evidence/facet_evidence.ts";
import type { RuntimeExportSummary } from "../src/extract/runtime_export.ts";

describe("facet evidence assembly", () => {
  test("builds runtime recipe, role, family, and tag evidence", () => {
    const records: ItemExtractRecord[] = [
      runtimeRecord({
        id: "example:ingot_mold",
        display_name: "Ingot Mold",
        minecraft_tags: ["example:molds", "example:ingot_molds"],
        minecraft_tags_direct: ["example:ingot_molds"],
        ingredient_of: ["example:casting/cast_ingot"],
        ingredient_of_counts: { "example:casting": 1 },
        semantic_text: [{
          source: "runtime-tooltip",
          text: "Reusable ceramic mold for casting metal ingots.",
        }],
      }),
      runtimeRecord({
        id: "example:copper_ingot",
        display_name: "Copper Ingot",
        output_of: ["example:casting/cast_ingot"],
        output_of_counts: { "example:casting": 1 },
      }),
    ];
    const summary: RuntimeExportSummary = {
      schema_version: 1,
      pack_id: "fixture",
      loader: "forge",
      minecraft_version: "1.20.1",
      item_count: 2,
      direct_item_tags_available: true,
      item_tag_members: {
        "example:ingot_molds": ["example:ingot_mold"],
      },
      block_tag_members: {
        "minecraft:mineable/pickaxe": ["example:casting_table"],
      },
      recipe_type_counts: {
        "example:casting": 2,
      },
    };

    const artifact = buildFacetEvidenceArtifact({
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-10T00:00:00.000Z",
      runtimeItemsPath: "/tmp/fixture.runtime-items.ndjson",
      runtimeSummaryPath: "/tmp/fixture.runtime-summary.json",
      records,
      summary,
    });

    expect(artifact.records.find((record) => record.kind === "runtime_item" && record.id === "example:ingot_mold")?.semantic_text?.[0]?.text).toContain("casting metal");
    expect(artifact.records.find((record) => record.kind === "recipe_type" && record.id === "example:casting")?.count).toBe(2);
    expect(artifact.records.find((record) => record.kind === "recipe_role_summary" && record.id === "example:ingot_mold|input|example:casting")).toBeDefined();
    expect(artifact.records.find((record) => record.kind === "recipe_id_family" && record.id === "example:casting/cast")).toBeDefined();
    expect(artifact.records.find((record) => record.kind === "item_tag" && record.id === "example:ingot_molds")?.direct_membership_known).toBe(true);
    expect(artifact.records.find((record) => record.kind === "block_tag" && record.id === "minecraft:mineable/pickaxe")?.item_refs).toContain("example:casting_table");
  });

  test("collects synthetic guide, quest, advancement, and mod metadata records from a jar", () => {
    withTempDir((dir) => {
      const mods = join(dir, "mods");
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
description="Fixture mod for casting evidence"
`,
        "assets/example/lang/en_us.json": JSON.stringify({
          "item.example.ingot_mold": "Ingot Mold",
          "item.example.ingot_mold.tooltip": "Reusable mold for casting ingots.",
          "advancement.example.casting.start.title": "Start Casting",
          "advancement.example.casting.start.description": "Make your first mold",
        }),
        "assets/example/models/item/ingot_mold.json": JSON.stringify({
          parent: "minecraft:item/generated",
        }),
        "data/example/advancements/casting/start.json": JSON.stringify({
          display: {
            title: { translate: "advancement.example.casting.start.title" },
            description: { translate: "advancement.example.casting.start.description" },
            icon: { item: "example:ingot_mold" },
          },
          criteria: {
            has_mold: {
              trigger: "minecraft:inventory_changed",
              conditions: { items: [{ item: "example:ingot_mold" }] },
            },
          },
        }),
        "data/example/patchouli_books/guide/en_us/entries/casting/molds.json": JSON.stringify({
          name: "Casting Molds",
          category: "example:casting",
          pages: [{
            type: "patchouli:crafting",
            recipe: "example:casting/cast_ingot",
            text: "Use example:ingot_mold in casting.",
          }],
        }),
        "data/example/quests/casting/first_mold.json": JSON.stringify({
          title: "First Mold",
          icon: "example:ingot_mold",
          tasks: [{ item: "example:ingot_mold" }],
          rewards: [{ item: "example:copper_ingot" }],
        }),
      });

      const result = collectExternalFacetEvidence({
        modsPath: mods,
        generatedBy: "test",
      });

      expect(result.records.find((record) => record.kind === "mod_metadata" && record.id === "example")?.description).toContain("Fixture mod");
      expect(result.records.find((record) => record.kind === "mod_metadata" && record.id === "example")?.semantic_text?.[0]?.text).toContain("Fixture mod");
      expect(result.records.find((record) => record.kind === "advancement" && record.id === "example:casting/start")?.item_refs).toContain("example:ingot_mold");
      const guide = result.records.find((record) => record.kind === "guide_page" && record.label === "Casting Molds");
      expect(guide?.recipe_refs).toContain("example:casting/cast_ingot");
      expect(guide?.semantic_text?.some((entry) => entry.text.includes("Use example:ingot_mold in casting"))).toBe(true);
      expect(result.records.find((record) => record.kind === "quest_node" && record.label === "First Mold")?.item_refs).toContain("example:ingot_mold");
      expect(result.diagnostics.find((diagnostic) => diagnostic.adapter === "guide_page")?.count).toBe(1);
    });
  });

  test("collects FTB Quest SNBT and KubeJS lang semantic text from an instance", () => {
    withTempDir((dir) => {
      const minecraft = join(dir, "minecraft");
      const mods = join(minecraft, "mods");
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
description="Fixture mod"
`,
      });
      const langDir = join(minecraft, "kubejs", "assets", "tfg", "lang");
      mkdirSync(langDir, { recursive: true });
      writeFileSync(join(langDir, "en_us.json"), JSON.stringify({
        "chapter.tfg.ore_processing": "Ore Processing",
        "quest.tfg.ore_processing.body": "Crush ore, wash it, then smelt it into useful metal.",
        "create.ponder.crusher.header": "Crushing Ores",
        "create.ponder.crusher.text_1": "Crushers process ore into dust.",
        "tfc.jei.heating": "Heating Recipe",
        "stackgroup.emixx.tfg_molds": "Casting Molds",
        "tfg.tooltip.machine.test_1": "Runs biological samples.",
      }));
      const resourcepacks = join(minecraft, "resourcepacks");
      mkdirSync(resourcepacks, { recursive: true });
      writeZip(join(resourcepacks, "fixture.zip"), {
        "assets/tfg/lang/en_us.json": JSON.stringify({
          "tfg.tooltip.machine.zip_only": "Resolved from zipped resource pack.",
        }),
      });
      const scripts = join(minecraft, "kubejs", "client_scripts");
      mkdirSync(scripts, { recursive: true });
      writeFileSync(join(scripts, "tooltips.js"), `
ItemEvents.tooltip(event => {
  event.addAdvanced(['example:ingot_mold'], (item, advanced, text) => {
    text.add(1, Text.translate('tfg.tooltip.machine.test_1'));
    text.add(2, Text.translate('tfg.tooltip.machine.zip_only'));
  })
})
`);
      const stackGroups = join(minecraft, "kubejs", "assets", "tfg", "stack_groups");
      mkdirSync(stackGroups, { recursive: true });
      writeFileSync(join(stackGroups, "molds.json"), JSON.stringify({
        id: "tfg:molds",
        name: "stackgroup.emixx.tfg_molds",
        type: "emixx:group",
        contents: ["example:ingot_mold", "#example:molds"],
      }));
      const questDir = join(minecraft, "config", "ftbquests", "quests", "chapters");
      mkdirSync(questDir, { recursive: true });
      writeFileSync(join(questDir, "ore_processing.snbt"), `
{
  title: "{chapter.tfg.ore_processing}"
  quests: [{
    title: "Crushing and Washing"
    description: ["{quest.tfg.ore_processing.body}", "Use example:ingot_mold after smelting."]
    tasks: [{ item: "example:ingot_mold" }]
  }]
}
`);

      const result = collectExternalFacetEvidence({
        modsPath: mods,
        generatedBy: "test",
      });
      const quest = result.records.find((record) =>
        record.kind === "quest_node" && record.id === "pack:ftbquests/chapters/ore_processing"
      );
      expect(quest?.semantic_text?.map((entry) => entry.text).join("\n")).toContain("Crush ore, wash it");
      expect(quest?.semantic_text?.map((entry) => entry.text).join("\n")).toContain("Crushing and Washing");
      expect(quest?.item_refs).toContain("example:ingot_mold");
      expect(result.records.find((record) =>
        record.kind === "guide_page" && record.id === "create:ponder/crusher"
      )?.semantic_text?.map((entry) => entry.text).join("\n")).toContain("Crushers process ore");
      expect(result.records.find((record) =>
        record.kind === "recipe_type" && record.id === "tfc:heating"
      )?.semantic_text?.[0]?.text).toBe("Heating Recipe");
      const tooltip = result.records.find((record) => record.kind === "kubejs_tooltip");
      expect(tooltip?.item_refs).toContain("example:ingot_mold");
      expect(tooltip?.semantic_text?.map((entry) => entry.text).join("\n")).toContain("Runs biological samples");
      expect(tooltip?.semantic_text?.map((entry) => entry.text).join("\n")).toContain("Resolved from zipped resource pack");
      const stackGroup = result.records.find((record) => record.kind === "stack_group" && record.id === "tfg:molds");
      expect(stackGroup?.label).toBe("Casting Molds");
      expect(stackGroup?.item_refs).toContain("example:ingot_mold");
      expect(stackGroup?.tags).toContain("example:molds");
    });
  });
});

interface RuntimeRecordOverrides extends Partial<ItemExtractRecord> {
  ingredient_of?: string[];
  output_of?: string[];
  ingredient_of_counts?: Record<string, number>;
  output_of_counts?: Record<string, number>;
}

function runtimeRecord(overrides: RuntimeRecordOverrides): ItemExtractRecord {
  const id = overrides.id ?? "example:item";
  const [namespacePart, pathPart] = id.split(":");
  const namespace = namespacePart ?? "example";
  const path = pathPart ?? "item";
  return {
    id,
    namespace,
    path,
    display_name: overrides.display_name ?? "Item",
    minecraft_tags: overrides.minecraft_tags ?? [],
    minecraft_tags_direct: overrides.minecraft_tags_direct ?? [],
    recipe_role: {
      ingredient_of: overrides.recipe_role?.ingredient_of ?? overrides.ingredient_of ?? [],
      output_of: overrides.recipe_role?.output_of ?? overrides.output_of ?? [],
      in_degree: overrides.recipe_role?.in_degree ?? overrides.ingredient_of?.length ?? 0,
      out_degree: overrides.recipe_role?.out_degree ?? overrides.output_of?.length ?? 0,
      ingredient_of_counts: overrides.recipe_role?.ingredient_of_counts ?? overrides.ingredient_of_counts ?? {},
      output_of_counts: overrides.recipe_role?.output_of_counts ?? overrides.output_of_counts ?? {},
    },
    model_parents: overrides.model_parents ?? [],
    loot_table_sources: overrides.loot_table_sources ?? [],
    creative_tabs: overrides.creative_tabs ?? [],
    component_data: overrides.component_data ?? {},
    ...(overrides.semantic_text ? { semantic_text: overrides.semantic_text } : {}),
    extractor_meta: overrides.extractor_meta ?? {
      extractor: "slot-runtime-export",
    },
  };
}

function withTempDir<T>(fn: (dir: string) => T): T {
  const dir = mkdtempSync(join(tmpdir(), "slot-classification-evidence-"));
  try {
    return fn(dir);
  } finally {
    if (existsSync(dir)) rmSync(dir, { recursive: true, force: true });
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
  const localData = Buffer.concat(locals);
  const eocd = Buffer.alloc(22);
  eocd.writeUInt32LE(0x06054b50, 0);
  eocd.writeUInt16LE(0, 4);
  eocd.writeUInt16LE(0, 6);
  eocd.writeUInt16LE(Object.keys(files).length, 8);
  eocd.writeUInt16LE(Object.keys(files).length, 10);
  eocd.writeUInt32LE(centralDirectory.length, 12);
  eocd.writeUInt32LE(localData.length, 16);
  eocd.writeUInt16LE(0, 20);

  writeFileSync(path, Buffer.concat([localData, centralDirectory, eocd]));
}
