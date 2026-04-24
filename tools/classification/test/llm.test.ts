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
import { runStage3 } from "../src/llm/run.ts";
import { selectRetryCandidates, runStage3Retry } from "../src/llm/retry.ts";
import { ReplayLlmClient, RecordingLlmClient, fixtureHash } from "../src/llm/client.ts";
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
      ingredient_of: Array.from({ length: 50 }, (_, i) => `minecraft:r${i}`),
      output_of: [],
      in_degree: 50,
      out_degree: 0,
      ingredient_of_counts: { crafting_shaped: 50 },
      output_of_counts: {},
    };
    r.loot_table_sources = Array.from({ length: 30 }, (_, i) => `minecraft:t${i}`);
    const p = buildItemPayload(r, {});
    expect(p.sample_ingredient_of.length).toBeLessThanOrEqual(10);
    expect(p.sample_loot_sources.length).toBeLessThanOrEqual(10);
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
            frequency: { value: "frequent", confidence: 0.8 },
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
