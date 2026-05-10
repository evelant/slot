import { describe, test, expect } from "bun:test";
import { mkdtempSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import {
  buildBatchPrompt,
  buildItemPayload,
  buildSplitPrompt,
  defaultTargetFacets,
} from "../src/llm/prompt.ts";
import { parseLlmResponse } from "../src/llm/parse.ts";
import {
  runStage3,
  selectSubsystemVocabularyForRecords,
} from "../src/llm/run.ts";
import { selectRetryCandidates, runStage3Retry } from "../src/llm/retry.ts";
import { ReplayLlmClient, RecordingLlmClient, fixtureHash } from "../src/llm/client.ts";
import {
  buildRuntimeProposerPrompt,
  buildRuntimeSubsystemContexts,
  loadSubsystemVocabularyFile,
} from "../src/llm/runtime_subsystems.ts";
import type { ItemExtractRecord } from "../src/extract/record.ts";
import type { LayerFile } from "../src/deterministic/run.ts";

function ironIngotRecord(): ItemExtractRecord {
  return {
    id: "minecraft:iron_ingot",
    namespace: "minecraft",
    path: "iron_ingot",
    display_name: "Iron Ingot",
    minecraft_tags: ["minecraft:iron_tool_materials"],
    minecraft_tags_direct: ["minecraft:iron_tool_materials"],
    recipe_role: {
      ingredient_of: ["minecraft:iron_pickaxe"],
      output_of: ["minecraft:iron_ingot_from_smelting_iron_ore"],
      in_degree: 1,
      out_degree: 1,
      ingredient_of_counts: { crafting_shaped: 1 },
      output_of_counts: { smelting: 1 },
    },
    model_parents: ["item/iron_ingot", "item/generated"],
    loot_table_sources: ["minecraft:chests/simple_dungeon"],
    creative_tabs: [],
    component_data: { "minecraft:max_stack_size": 64 },
  };
}

function ironIngotStage2Layer(): LayerFile {
  return {
    schema_version: 1,
    layer: "vanilla-base",
    source: "minecraft",
    entries: {
      "minecraft:iron_ingot": {
        facets: {
          mod_namespace: { value: "minecraft", source: "rule:mod_namespace" },
          material_family: { value: "iron", source: "rule:material_family_from_tag" },
          form: { value: "ingot", source: "rule:form_from_id" },
        },
      },
    },
  };
}

function runtimeRecord(args: {
  id: string;
  displayName: string;
  tags?: string[];
  blockTags?: string[];
  ingredientCounts?: Record<string, number>;
  outputCounts?: Record<string, number>;
  ingredientOf?: string[];
  outputOf?: string[];
  components?: Record<string, unknown>;
  isBlock?: boolean;
}): ItemExtractRecord {
  const [namespace, path] = args.id.split(":") as [string, string];
  return {
    id: args.id,
    namespace,
    path,
    display_name: args.displayName,
    minecraft_tags: args.tags ?? [],
    minecraft_tags_direct: [],
    recipe_role: {
      ingredient_of: args.ingredientOf ?? [],
      output_of: args.outputOf ?? [],
      in_degree: args.ingredientOf?.length ?? 0,
      out_degree: args.outputOf?.length ?? 0,
      ingredient_of_counts: args.ingredientCounts ?? {},
      output_of_counts: args.outputCounts ?? {},
    },
    model_parents: [],
    loot_table_sources: [],
    creative_tabs: [],
    component_data: args.components ?? { "minecraft:max_stack_size": 64 },
    extractor_meta: {
      extractor: "slot-runtime-export",
      item_tag_membership: "resolved_runtime",
      direct_item_tags_available: false,
      ...(args.isBlock ? { is_block_item: true, block_id: args.id } : {}),
      ...(args.blockTags ? { block_tags: args.blockTags } : {}),
    },
  };
}

describe("prompt building", () => {
  test("includes schema for every target facet", () => {
    const record = ironIngotRecord();
    const payload = buildItemPayload(record, {});
    const prompt = buildBatchPrompt({
      items: [payload],
      target_facets: ["role", "activity", "primary_uses"],
    });
    expect(prompt).toContain("## role");
    expect(prompt).toContain("## activity");
    expect(prompt).toContain("## primary_uses");
    expect(prompt).toContain("minecraft:iron_ingot");
    // instructions call out ambiguous + schema_proposals
    expect(prompt).toContain("schema_proposals");
    expect(prompt).toContain("ambiguous");
  });

  test("defaultTargetFacets matches llm_authored registry entries", () => {
    const targets = defaultTargetFacets();
    expect(targets).toContain("role");
    expect(targets).toContain("activity");
    expect(targets).toContain("primary_uses");
    // facets that are deterministic-only should NOT appear
    expect(targets).not.toContain("mod_namespace");
    expect(targets).not.toContain("is_stackable");
  });

  test("payload keeps recipe-role and loot lists bounded", () => {
    const r = ironIngotRecord();
    r.recipe_role = {
      ingredient_of: Array.from({ length: 200 }, (_, i) => `minecraft:r${i}`),
      output_of: Array.from({ length: 110 }, (_, i) => `minecraft:o${i}`),
      in_degree: 200,
      out_degree: 110,
      ingredient_of_counts: { crafting_shaped: 200 },
      output_of_counts: {},
    };
    r.loot_table_sources = Array.from({ length: 90 }, (_, i) => `minecraft:t${i}`);
    const p = buildItemPayload(r, {});
    expect(p.recipe_ingredient_examples.length).toBeGreaterThan(10);
    expect(p.recipe_ingredient_examples.length).toBeLessThanOrEqual(96);
    expect(p.recipe_output_examples.length).toBeLessThanOrEqual(96);
    expect(p.loot_source_examples.length).toBeGreaterThan(10);
    expect(p.loot_source_examples.length).toBeLessThanOrEqual(64);
    expect(p.recipe_ingredient_count).toBe(200);
    expect(p.recipe_output_count).toBe(110);
    expect(p.loot_source_count).toBe(90);
    expect(p.recipe_examples_truncated).toBe(true);
    expect(p.loot_sources_truncated).toBe(true);
  });

  test("payload preserves runtime resolved tags without calling them inherited", () => {
    const r = ironIngotRecord();
    r.minecraft_tags = ["forge:ingots", "forge:ingots/iron"];
    r.minecraft_tags_direct = [];
    r.extractor_meta = {
      item_tag_membership: "resolved_runtime",
      direct_item_tags_available: false,
    };

    const p = buildItemPayload(r, {});
    expect(p.minecraft_tag_membership).toBe("resolved_runtime");
    expect(p.minecraft_tags_direct).toEqual([]);
    expect(p.minecraft_tags_inherited).toEqual([]);
    expect(p.minecraft_tags_resolved).toEqual(["forge:ingots", "forge:ingots/iron"]);
  });

  test("runtime-resolved prompt includes runtime export interpretation notes", () => {
    const staticPayload = buildItemPayload(ironIngotRecord(), {});
    const staticPrompt = buildSplitPrompt({
      items: [staticPayload],
      target_facets: ["role"],
    });
    expect(staticPrompt.system).not.toContain("# Runtime export input notes");

    const r = ironIngotRecord();
    r.display_name = "§bTungstensteel Space Helmet";
    r.minecraft_tags = ["c:hidden_from_recipe_viewers", "gtceu:ppe_armor"];
    r.minecraft_tags_direct = [];
    r.extractor_meta = {
      item_tag_membership: "resolved_runtime",
      direct_item_tags_available: false,
    };
    const runtimePrompt = buildSplitPrompt({
      items: [buildItemPayload(r, {})],
      target_facets: ["role"],
    });
    expect(runtimePrompt.system).toContain("# Runtime export input notes");
    expect(runtimePrompt.system).toContain("KubeJS and datapack");
    expect(runtimePrompt.system).toContain("not collected here");
    expect(runtimePrompt.system).toContain("Recipe absences are weaker");
    expect(runtimePrompt.system).toContain("Emit `primary_uses` for every item");
    expect(runtimePrompt.system).toContain("do not use empty loot/source fields as evidence");
    expect(runtimePrompt.system).toContain("Rationales like \"no loot source\"");
    expect(runtimePrompt.system).toContain("c:hidden_from_recipe_viewers");
    expect(runtimePrompt.system).toContain("§b");
    expect(runtimePrompt.system).toContain("never emit it as a scalar `value`");
  });

  test("payload carries block context and semantic runtime components", () => {
    const r = ironIngotRecord();
    r.component_data = {
      "minecraft:max_stack_size": 1,
      "minecraft:max_damage": 250,
      "minecraft:enchantable": {},
      "minecraft:equippable": { slot: "head" },
      "minecraft:light_emission": 14,
      "minecraft:rarity": "rare",
    };
    r.extractor_meta = {
      is_block_item: true,
      block_id: "minecraft:lantern",
      block_requires_correct_tool: false,
      block_tags: ["minecraft:mineable/pickaxe", "minecraft:needs_stone_tool"],
    };

    const p = buildItemPayload(r, {});
    expect(p.block_context).toEqual({
      block_id: "minecraft:lantern",
      block_tags: ["minecraft:mineable/pickaxe", "minecraft:needs_stone_tool"],
      requires_correct_tool: false,
    });
    expect(p.component_highlights["minecraft:max_damage"]).toBe(250);
    expect(p.component_highlights["minecraft:equippable"]).toEqual({ slot: "head" });
    expect(p.component_highlights["minecraft:light_emission"]).toBe(14);
  });
});

describe("response parsing", () => {
  test("accepts claude envelope + fenced JSON", () => {
    const inner = JSON.stringify({
      items: {
        "minecraft:iron_ingot": {
          facets: {
            role: { value: "material", confidence: 0.98, rationale: "ingot" },
            activity: { values: ["building", "combat"], confidence: 0.8 },
          },
        },
      },
    });
    const envelope = JSON.stringify({
      type: "result",
      result: "```json\n" + inner + "\n```",
    });
    const parsed = parseLlmResponse(envelope);
    expect(parsed.warnings).toEqual([]);
    const item = parsed.items.get("minecraft:iron_ingot")!;
    expect(item.facets.role).toMatchObject({ kind: "single", value: "material" });
    expect(item.facets.activity).toMatchObject({ kind: "multi", values: ["building", "combat"] });
  });

  test("drops entries with out-of-enum values, keeps others", () => {
    const response = JSON.stringify({
      items: {
        "minecraft:iron_ingot": {
          facets: {
            role: { value: "nonsense-role", confidence: 0.5 }, // out of enum
            activity: { values: ["building"], confidence: 0.9 }, // ok
          },
        },
      },
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.items.get("minecraft:iron_ingot")!.facets.role).toBeUndefined();
    expect(parsed.items.get("minecraft:iron_ingot")!.facets.activity).toMatchObject({
      kind: "multi",
    });
    expect(parsed.warnings.length).toBe(1);
    expect(parsed.warnings[0]!).toContain("not in enum");
  });

  test("single 'value' for a multi facet is wrapped into [value]", () => {
    const response = JSON.stringify({
      items: {
        "minecraft:iron_ingot": {
          facets: {
            flavor: { value: "plain", confidence: 0.8 },
          },
        },
      },
    });
    const parsed = parseLlmResponse(response);
    const flavor = parsed.items.get("minecraft:iron_ingot")!.facets.flavor!;
    expect(flavor.kind).toBe("multi");
    if (flavor.kind === "multi") {
      expect(flavor.values).toEqual(["plain"]);
    }
    // the wrap is informational — it still pushes a warning for observability
    expect(parsed.warnings.some((w) => w.includes("wrapped as [value]"))).toBe(true);
  });

  test("ambiguous two-value shape", () => {
    const response = JSON.stringify({
      items: {
        "minecraft:cut_copper_stairs": {
          facets: {
            role: {
              values: ["building_block", "decorative_block"],
              ambiguous: true,
              confidence: 0.3,
            },
          },
        },
      },
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.items.get("minecraft:cut_copper_stairs")!.facets.role).toMatchObject({
      kind: "ambiguous",
      ambiguous: true,
      values: ["building_block", "decorative_block"],
    });
  });

  test("signal=named caps at 0.95 even if model claims higher", () => {
    const response = JSON.stringify({
      items: {
        "minecraft:iron_ingot": {
          facets: {
            role: { value: "material", signal: "named", evidence: "tag minecraft:iron_tool_materials", confidence: 0.99 },
          },
        },
      },
    });
    const parsed = parseLlmResponse(response);
    const role = parsed.items.get("minecraft:iron_ingot")!.facets.role!;
    expect(role.confidence).toBe(0.95);
    expect(role.rationale).toContain("[named]");
    expect(role.rationale).toContain("tag minecraft:iron_tool_materials");
  });

  test("signal=guess caps overconfident model claim at 0.30", () => {
    const response = JSON.stringify({
      items: {
        "minecraft:mystery": {
          facets: {
            role: { value: "curiosity", signal: "guess", evidence: "", confidence: 0.95 },
          },
        },
      },
    });
    const parsed = parseLlmResponse(response);
    const role = parsed.items.get("minecraft:mystery")!.facets.role!;
    expect(role.confidence).toBe(0.30);
  });

  test("signal=pattern without evidence is accepted (evidence is optional)", () => {
    const response = JSON.stringify({
      items: {
        "minecraft:x": {
          facets: {
            role: { value: "tool", signal: "pattern", confidence: 0.85 },
          },
        },
      },
    });
    const parsed = parseLlmResponse(response);
    const role = parsed.items.get("minecraft:x")!.facets.role!;
    // pattern caps at 0.80; model's 0.85 is silently capped
    expect(role.confidence).toBe(0.80);
    expect(parsed.warnings.length).toBe(0);
  });

  test("model confidence below signal floor is preserved (not raised)", () => {
    const response = JSON.stringify({
      items: {
        "minecraft:x": {
          facets: {
            role: { value: "tool", signal: "named", evidence: "tag foo", confidence: 0.6 },
          },
        },
      },
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.items.get("minecraft:x")!.facets.role!.confidence).toBe(0.6);
  });

  test("multi facet preserves signal+evidence; rationale folds them in", () => {
    const response = JSON.stringify({
      items: {
        "minecraft:iron_ingot": {
          facets: {
            activity: {
              values: ["building", "combat"],
              signal: "inferred",
              evidence: "ingredient_of: anvil, sword",
              rationale: "common combat + structural usage",
            },
          },
        },
      },
    });
    const parsed = parseLlmResponse(response);
    const a = parsed.items.get("minecraft:iron_ingot")!.facets.activity!;
    expect(a.kind).toBe("multi");
    expect(a.confidence).toBe(0.6); // inferred floor
    expect(a.rationale).toContain("[inferred]");
    expect(a.rationale).toContain("ingredient_of");
    expect(a.rationale).toContain("structural usage");
  });

  test("schema_proposals flow through unchanged", () => {
    const response = JSON.stringify({
      items: {},
      schema_proposals: [
        {
          kind: "add_value",
          facet: "activity",
          value: "ritual_magic",
          rationale: "Botania use case",
        },
      ],
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.proposals.length).toBe(1);
    expect(parsed.proposals[0]!.kind).toBe("add_value");
  });

  test("corrections at >= 0.7 confidence are retained", () => {
    const response = JSON.stringify({
      items: {},
      corrections: [
        {
          item: "minecraft:iron_ingot",
          facet: "material_family",
          current: "wood_oak",
          suggested: "iron",
          rationale: "Item is clearly iron; stage 2 misread the tag.",
          confidence: 0.95,
        },
        {
          item: "minecraft:foo",
          facet: "form",
          current: "ingot",
          suggested: "nugget",
          rationale: "uncertain",
          confidence: 0.3,
        },
      ],
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.corrections.length).toBe(1);
    expect(parsed.corrections[0]!.item).toBe("minecraft:iron_ingot");
    expect(parsed.warnings.some((w) => w.includes("below confidence"))).toBe(true);
  });

  test("corrections with missing fields are dropped with a warning", () => {
    const response = JSON.stringify({
      items: {},
      corrections: [
        { item: "minecraft:iron_ingot", facet: "form" /* no rationale */ },
      ],
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.corrections.length).toBe(0);
    expect(parsed.warnings.length).toBe(1);
  });

  test("fill_ins surface stage-2 gaps and route to fillIns array", () => {
    const response = JSON.stringify({
      items: {},
      fill_ins: [
        {
          item: "create:dark_oak_window",
          facet: "form",
          value: "pane",
          rationale: "stage-2 form rule didn't catch _window suffix",
        },
        {
          item: "create:brass_pipe",
          facet: "material_family",
          value: "brass",
          rationale: "id prefix brass_ implies family",
        },
      ],
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.fillIns.length).toBe(2);
    expect(parsed.fillIns[0]!.item).toBe("create:dark_oak_window");
    expect(parsed.fillIns[0]!.facet).toBe("form");
    expect(parsed.fillIns[0]!.value).toBe("pane");
  });

  test("fill_ins for llm-authored facets are dropped with a warning", () => {
    // The LLM should put role / activity / carry_frequency etc. in
    // `facets`, not `fill_ins` — the runner enforces that boundary so
    // judgment-call facets don't sneak through the audit channel.
    const response = JSON.stringify({
      items: {},
      fill_ins: [
        {
          item: "minecraft:iron_ingot",
          facet: "role",
          value: "material",
          rationale: "obvious",
        },
      ],
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.fillIns.length).toBe(0);
    expect(parsed.warnings.some((w) => w.includes("llm-authored"))).toBe(true);
  });
});

describe("runStage3", () => {
  test("round-trips a fixture through prompt -> replay client -> merge", async () => {
    const record = ironIngotRecord();
    const stage2Layer = ironIngotStage2Layer();

    // runStage3 prefers querySplit when the client implements it (which
    // ReplayLlmClient does). The fixture must be hashed under the split-mode
    // key, not the combined-prompt key.
    const { system, user } = buildSplitPrompt({
      items: [buildItemPayload(record, stage2Layer.entries["minecraft:iron_ingot"]!.facets)],
      target_facets: defaultTargetFacets(),
    });
    const hash = fixtureHash(`${system}\n\n---\n\n${user}`);

    const fixtureDir = mkdtempSync(join(tmpdir(), "slot-stage3-"));
    const response = JSON.stringify({
      items: {
        "minecraft:iron_ingot": {
          facets: {
            role: { value: "material", confidence: 0.98, rationale: "canonical ingot" },
            activity: { values: ["building", "combat", "mining"], confidence: 0.9 },
            primary_uses: {
              values: ["crafting tools and armor", "anvil repairs"],
              confidence: 0.95,
            },
            carry_frequency: { value: "frequent", confidence: 0.8 },
            // stage 2 already set material_family — this should be dropped by the merger
            material_family: { value: "gold", confidence: 0.1 },
          },
        },
      },
    });
    writeFileSync(join(fixtureDir, `${hash}.response.txt`), response);

    const client = new ReplayLlmClient(fixtureDir);
    const result = await runStage3({
      records: [record],
      stage2Layer,
      client,
    });

    const facets = result.layer.entries["minecraft:iron_ingot"]!.facets;
    // stage-2 facets preserved
    expect(facets.mod_namespace).toMatchObject({ value: "minecraft" });
    expect(facets.material_family).toMatchObject({ value: "iron" }); // NOT gold
    // stage-3 facets merged
    expect(facets.role).toMatchObject({ value: "material", source: "llm:stage3" });
    expect(facets.activity).toMatchObject({ values: ["building", "combat", "mining"] });
    expect(facets.primary_uses).toMatchObject({
      values: ["anvil repairs", "crafting tools and armor"],
    });

    // merger should warn about the stage-2 clobber attempt (only when values disagree)
    const clobberWarn = result.warnings.find((w) =>
      w.includes("material_family") && w.includes("stage 2 asserted"),
    );
    expect(clobberWarn).toBeTruthy();

    expect(result.filledItems).toBe(1);
    expect(result.coverageAdded.role).toBe(1);
    expect(result.coverageAdded.activity).toBe(1);
  });

  test("concurrency runs batches in parallel and merges in any order", async () => {
    const records: ItemExtractRecord[] = Array.from({ length: 12 }, (_, i) => ({
      ...ironIngotRecord(),
      id: `minecraft:item${i}`,
      path: `item${i}`,
    }));
    const stage2: LayerFile = {
      schema_version: 1,
      layer: "vanilla-base",
      source: "minecraft",
      entries: {},
    };

    let inFlight = 0;
    let maxInFlight = 0;
    const completions: number[] = [];
    let completionOrder = 0;

    const client = {
      async query(prompt: string) {
        inFlight++;
        maxInFlight = Math.max(maxInFlight, inFlight);
        // Tiny async tick so other workers actually start before this resolves.
        await new Promise((r) => setTimeout(r, 5));
        // Identify which item batch this is by sniffing the prompt.
        const m = prompt.match(/minecraft:item(\d+)/);
        const itemIdx = m ? Number(m[1]) : -1;
        completions.push(itemIdx);
        completionOrder++;
        inFlight--;
        return JSON.stringify({
          items: {
            [`minecraft:item${itemIdx}`]: {
              facets: { role: { value: "material", signal: "named", evidence: "test" } },
            },
          },
        });
      },
    };

    const result = await runStage3({
      records,
      stage2Layer: stage2,
      client,
      batchSize: 1,
      concurrency: 4,
    });

    expect(maxInFlight).toBeGreaterThan(1); // actually parallel
    expect(maxInFlight).toBeLessThanOrEqual(4); // bounded by concurrency
    // All 12 items got their role facet
    let count = 0;
    for (const r of records) {
      if (result.layer.entries[r.id]?.facets.role) count++;
    }
    expect(count).toBe(12);
  });

  test("only-list restricts execution", async () => {
    const recA: ItemExtractRecord = {
      ...ironIngotRecord(),
      id: "minecraft:a",
      path: "a",
    };
    const recB: ItemExtractRecord = {
      ...ironIngotRecord(),
      id: "minecraft:b",
      path: "b",
    };
    const stage2: LayerFile = {
      schema_version: 1,
      layer: "vanilla-base",
      source: "minecraft",
      entries: {},
    };
    // Client asserts it only ever sees record A
    const client = {
      async query(prompt: string) {
        if (prompt.includes("minecraft:b")) {
          throw new Error("record b should not be in this batch");
        }
        return JSON.stringify({ items: {} });
      },
    };
    await runStage3({
      records: [recA, recB],
      stage2Layer: stage2,
      client,
      only: ["minecraft:a"],
    });
  });
});

describe("runtime subsystem vocabulary", () => {
  test("builds namespace-scoped evidence from runtime export records", () => {
    const records = [
      runtimeRecord({
        id: "create:mechanical_press",
        displayName: "Mechanical Press",
        tags: ["create:wrench_pickup"],
        blockTags: ["create:stress_impact"],
        ingredientCounts: { "create:pressing": 4 },
        outputCounts: { crafting_shaped: 1 },
        ingredientOf: ["tfg:create/pressing/iron_plate"],
        outputOf: ["create:crafting/mechanical_press"],
        isBlock: true,
      }),
      runtimeRecord({
        id: "create:fluid_pipe",
        displayName: "Fluid Pipe",
        tags: ["create:pipes"],
        blockTags: ["create:fluid_pipe"],
        ingredientCounts: { crafting_shaped: 3 },
        outputCounts: { crafting_shaped: 1 },
        isBlock: true,
      }),
      runtimeRecord({
        id: "gtceu:lv_transformer",
        displayName: "LV Transformer",
        tags: ["gtceu:machines"],
        blockTags: ["gtceu:machines"],
        ingredientCounts: { "gtceu:machine": 2 },
        outputCounts: { crafting_shaped: 1 },
        isBlock: true,
      }),
    ];
    const contexts = buildRuntimeSubsystemContexts({
      records,
      minItems: 1,
      summary: {
        pack_id: "sample_pack",
        loader: "forge",
        minecraft_version: "1.20.1",
        item_tag_members: {
          "create:pipes": ["create:fluid_pipe"],
          "gtceu:machines": ["gtceu:lv_transformer"],
        },
        block_tag_members: {
          "create:stress_impact": ["create:mechanical_press"],
          "gtceu:machines": ["gtceu:lv_transformer"],
        },
      },
    });

    const create = contexts.find((context) => context.modNamespace === "create")!;
    expect(create.packId).toBe("sample_pack");
    expect(create.componentCounts.block_items).toBe(2);
    expect(create.tokenClusters.some((cluster) => cluster.id === "press")).toBe(true);
    expect(create.itemTagSummaries.some((tag) => tag.tag === "create:pipes")).toBe(true);
    expect(create.blockTagSummaries.some((tag) => tag.tag === "create:stress_impact")).toBe(true);
    expect(create.recipeFamilyCandidates.some((row) => row.id === "create:pressing")).toBe(true);
    expect(create.recipeIdNamespaces.some((row) => row.id === "tfg")).toBe(true);

    const prompt = buildRuntimeProposerPrompt(create);
    expect(prompt.system).toContain("Pick **0 to 8** entries");
    expect(prompt.system).toContain("Prominent namespace-owned recipe families");
    expect(prompt.user).toContain("KubeJS/datapack recipe and tag edits");
    expect(prompt.user).toContain("Candidate namespace workflow recipe families");
    expect(prompt.user).toContain("Mod namespace: create");
    expect(prompt.user).toContain("create:mechanical_press");
  });

  test("surfaces namespace recipe families for survival workflow mods", () => {
    const records = [
      runtimeRecord({
        id: "tfc:ceramic/ingot_mold",
        displayName: "Ingot Mold",
        outputCounts: { "tfc:casting": 12, "tfc:advanced_shapeless_crafting": 50 },
        outputOf: ["tfc:casting/copper_ingot"],
      }),
      runtimeRecord({
        id: "tfc:metal/anvil/wrought_iron",
        displayName: "Wrought Iron Anvil",
        outputCounts: { "tfc:anvil": 8, "tfc:welding": 3 },
        outputOf: ["tfc:anvil/double_ingot", "tfc:welding/wrought_iron_anvil"],
        isBlock: true,
      }),
      runtimeRecord({
        id: "tfc:barrel",
        displayName: "Barrel",
        ingredientCounts: { "tfc:barrel_sealed": 5 },
        outputCounts: { crafting_shaped: 1 },
        isBlock: true,
      }),
    ];
    const contexts = buildRuntimeSubsystemContexts({ records, minItems: 1 });
    const tfc = contexts.find((context) => context.modNamespace === "tfc")!;

    expect(tfc.recipeFamilyCandidates.map((row) => row.id)).toContain("tfc:casting");
    expect(tfc.recipeFamilyCandidates.map((row) => row.id)).toContain("tfc:anvil");
    expect(tfc.recipeFamilyCandidates.map((row) => row.id)).toContain("tfc:barrel_sealed");
    expect(tfc.recipeFamilyCandidates.map((row) => row.id)).not.toContain("tfc:advanced_shapeless_crafting");

    const prompt = buildRuntimeProposerPrompt(tfc);
    expect(prompt.system).toContain("casting");
    expect(prompt.system).toContain("smithing");
    expect(prompt.user).toContain("tfc:casting");
    expect(prompt.user).toContain("tfc:barrel_sealed");
  });

  test("loads runtime vocabulary maps and filters them per batch namespace", () => {
    const fixtureDir = mkdtempSync(join(tmpdir(), "slot-runtime-vocab-"));
    const path = join(fixtureDir, "pack.runtime-subsystems.json");
    writeFileSync(path, JSON.stringify({
      kind: "slot-runtime-subsystem-vocabulary",
      namespaces: {
        create: {
          vocabulary: [{ id: "create:processing", rationale: "processing machines" }],
        },
        gtceu: {
          vocabulary: [{ id: "gtceu:energy_net", rationale: "power transfer" }],
        },
        ae2: {
          vocabulary: [{ id: "ae2:me_network", rationale: "ME network" }],
        },
      },
    }));

    const loaded = loadSubsystemVocabularyFile(path);
    const selected = selectSubsystemVocabularyForRecords(
      [
        runtimeRecord({ id: "create:mechanical_press", displayName: "Mechanical Press" }),
        runtimeRecord({ id: "gtceu:lv_transformer", displayName: "LV Transformer" }),
      ],
      undefined,
      loaded.byNamespace,
    );

    expect(selected?.map((entry) => entry.id).sort()).toEqual([
      "create:processing",
      "gtceu:energy_net",
    ]);
  });
});

describe("RecordingLlmClient resume behaviour", () => {
  test("returns cached response on hash hit without calling inner", async () => {
    const fixtureDir = mkdtempSync(join(tmpdir(), "slot-resume-"));
    // Pre-plant a fixture for a specific prompt
    const prompt = "canonical prompt";
    const hash = fixtureHash(prompt);
    const cachedResponse = JSON.stringify({ type: "result", result: "{\"items\":{}}" });
    writeFileSync(join(fixtureDir, `${hash}.response.json`), cachedResponse);

    let innerCalled = false;
    const inner = {
      async query() { innerCalled = true; return "SHOULD NOT BE CALLED"; },
    };
    const events: Array<{ hit: boolean }> = [];
    const client = new RecordingLlmClient(inner, fixtureDir, (e) => events.push(e));

    const got = await client.query(prompt, { model: "haiku" });
    expect(got).toBe(cachedResponse);
    expect(innerCalled).toBe(false);
    expect(events[0]?.hit).toBe(true);
  });

  test("calls inner on miss and persists", async () => {
    const fixtureDir = mkdtempSync(join(tmpdir(), "slot-resume-miss-"));
    let innerCalled = 0;
    const inner = {
      async query() {
        innerCalled++;
        return JSON.stringify({ type: "result", result: "{\"items\":{}}" });
      },
    };
    const client = new RecordingLlmClient(inner, fixtureDir);

    const prompt = "fresh prompt";
    await client.query(prompt, { model: "haiku" });
    expect(innerCalled).toBe(1);

    // Second call with same prompt should hit cache
    await client.query(prompt, { model: "haiku" });
    expect(innerCalled).toBe(1); // unchanged
  });

  test("querySplit caches on system+user hash", async () => {
    const fixtureDir = mkdtempSync(join(tmpdir(), "slot-resume-split-"));
    let innerCalls = 0;
    const inner = {
      async query() { throw new Error("should use querySplit"); },
      async querySplit() {
        innerCalls++;
        return JSON.stringify({ type: "result", result: "{\"items\":{}}" });
      },
    };
    const client = new RecordingLlmClient(inner, fixtureDir);

    await client.querySplit!("SYSTEM", "USER A", { model: "sonnet" });
    await client.querySplit!("SYSTEM", "USER A", { model: "sonnet" }); // cache hit
    await client.querySplit!("SYSTEM", "USER B", { model: "sonnet" }); // different user → miss

    expect(innerCalls).toBe(2);
  });
});

describe("retry candidate selection", () => {
  function layer(entries: Record<string, Record<string, unknown>>): LayerFile {
    const layerEntries: LayerFile["entries"] = {};
    for (const [id, facets] of Object.entries(entries)) {
      layerEntries[id] = { facets: facets as LayerFile["entries"][string]["facets"] };
    }
    return { schema_version: 1, layer: "vanilla-base", source: "minecraft", entries: layerEntries };
  }

  test("flags items with confidence below threshold on any llm facet", () => {
    const l = layer({
      "minecraft:a": {
        role: { value: "material", confidence: 0.95, source: "llm:stage3" },
        primary_uses: { values: ["x"], confidence: 0.3, source: "llm:stage3" },
      },
      "minecraft:b": {
        role: { value: "tool", confidence: 0.9, source: "llm:stage3" },
      },
    });
    expect(selectRetryCandidates(l, 0.5)).toEqual(["minecraft:a"]);
  });

  test("flags items with ambiguous: true", () => {
    const l = layer({
      "minecraft:a": {
        role: {
          values: ["material", "natural_resource"],
          ambiguous: true,
          confidence: 0.9,
          source: "llm:stage3",
        },
      },
      "minecraft:b": {
        role: { value: "tool", confidence: 0.9, source: "llm:stage3" },
      },
    });
    expect(selectRetryCandidates(l, 0.5)).toEqual(["minecraft:a"]);
  });

  test("ignores stage-2 rule-derived facets below threshold", () => {
    const l = layer({
      "minecraft:a": {
        material_family: { value: "iron", confidence: 0.2, source: "rule:foo" },
        role: { value: "material", confidence: 0.95, source: "llm:stage3" },
      },
    });
    // rule facet with low confidence shouldn't flag retry
    expect(selectRetryCandidates(l, 0.5)).toEqual([]);
  });
});

describe("runStage3Retry", () => {
  function baseRecord(): ItemExtractRecord {
    return {
      id: "minecraft:mystery",
      namespace: "minecraft",
      path: "mystery",
      display_name: "Mystery Item",
      minecraft_tags: [],
      minecraft_tags_direct: [],
      recipe_role: { ingredient_of: [], output_of: [], in_degree: 0, out_degree: 0, ingredient_of_counts: {}, output_of_counts: {} },
      model_parents: [],
      loot_table_sources: [],
      creative_tabs: [],
      component_data: null,
    };
  }

  test("retry replaces low-confidence facet with higher-confidence result", async () => {
    const firstPassLayer: LayerFile = {
      schema_version: 1,
      layer: "vanilla-base",
      source: "minecraft",
      entries: {
        "minecraft:mystery": {
          facets: {
            role: { value: "curiosity", confidence: 0.35, source: "llm:stage3" },
          },
        },
      },
    };
    const retryResponse = JSON.stringify({
      items: {
        "minecraft:mystery": {
          facets: {
            role: { value: "utility", confidence: 0.88, rationale: "specific behaviour" },
          },
        },
      },
    });
    const retryClient = {
      async query() {
        return retryResponse;
      },
    };

    const result = await runStage3Retry({
      records: [baseRecord()],
      firstPassLayer,
      client: retryClient,
      threshold: 0.5,
      model: "sonnet",
      effort: "max",
    });

    expect(result.retriedItems).toEqual(["minecraft:mystery"]);
    const role = result.layer.entries["minecraft:mystery"]!.facets.role as { value: string; source: string; confidence: number };
    expect(role.value).toBe("utility");
    expect(role.source).toBe("llm:stage3-retry");
    expect(result.facetsChanged.role).toBe(1);
    expect(result.facetsConfirmed.role ?? 0).toBe(0);
  });

  test("retry keeps first-pass value when retry has lower confidence", async () => {
    const firstPassLayer: LayerFile = {
      schema_version: 1,
      layer: "vanilla-base",
      source: "minecraft",
      entries: {
        "minecraft:mystery": {
          facets: {
            role: { value: "curiosity", confidence: 0.45, source: "llm:stage3" },
          },
        },
      },
    };
    const retryResponse = JSON.stringify({
      items: {
        "minecraft:mystery": {
          facets: {
            role: { value: "utility", confidence: 0.3 },
          },
        },
      },
    });
    const retryClient = { async query() { return retryResponse; } };

    const result = await runStage3Retry({
      records: [baseRecord()],
      firstPassLayer,
      client: retryClient,
      threshold: 0.5,
    });

    const role = result.layer.entries["minecraft:mystery"]!.facets.role as { value: string };
    expect(role.value).toBe("curiosity");
    expect(result.warnings.some((w) => w.includes("retry disagreed but lower confidence"))).toBe(true);
  });

  test("retry confirms same value and counts as confirmed, not changed", async () => {
    const firstPassLayer: LayerFile = {
      schema_version: 1,
      layer: "vanilla-base",
      source: "minecraft",
      entries: {
        "minecraft:mystery": {
          facets: {
            role: { value: "material", confidence: 0.4, source: "llm:stage3" },
          },
        },
      },
    };
    const retryResponse = JSON.stringify({
      items: {
        "minecraft:mystery": {
          facets: {
            role: { value: "material", confidence: 0.9 },
          },
        },
      },
    });
    const retryClient = { async query() { return retryResponse; } };

    const result = await runStage3Retry({
      records: [baseRecord()],
      firstPassLayer,
      client: retryClient,
      threshold: 0.5,
    });

    expect(result.facetsConfirmed.role).toBe(1);
    expect(result.facetsChanged.role ?? 0).toBe(0);
    const role = result.layer.entries["minecraft:mystery"]!.facets.role as { confidence: number };
    expect(role.confidence).toBe(0.9); // confidence bumped
  });

  test("no candidates → noop retry", async () => {
    const firstPassLayer: LayerFile = {
      schema_version: 1,
      layer: "vanilla-base",
      source: "minecraft",
      entries: {
        "minecraft:x": {
          facets: {
            role: { value: "material", confidence: 0.95, source: "llm:stage3" },
          },
        },
      },
    };
    let queried = false;
    const client = { async query() { queried = true; return "{}"; } };
    const result = await runStage3Retry({
      records: [baseRecord()],
      firstPassLayer,
      client,
      threshold: 0.5,
    });
    expect(queried).toBe(false);
    expect(result.retriedItems).toEqual([]);
  });
});
