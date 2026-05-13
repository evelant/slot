import { describe, test, expect } from "bun:test";
import { mkdtempSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import {
  buildBatchPrompt,
  buildItemPayload,
  buildPromptFacetVocabulary,
  buildSplitPrompt,
  defaultTargetFacets,
} from "../src/llm/prompt.ts";
import { parseLlmResponse } from "../src/llm/parse.ts";
import { runStage3 } from "../src/llm/run.ts";
import { selectRetryCandidates, runStage3Retry } from "../src/llm/retry.ts";
import { ReplayLlmClient, RecordingLlmClient, fixtureHash } from "../src/llm/client.ts";
import { buildDocumentContextByItem } from "../src/llm/document_context.ts";
import type { FacetEvidenceArtifact } from "../src/evidence/facet_evidence.ts";
import type { ItemExtractRecord } from "../src/extract/record.ts";
import type { LayerFile } from "../src/deterministic/run.ts";
import type { PackFacetVocabulary } from "../src/schema/vocabulary.ts";

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
  semanticText?: string[];
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
    semantic_text: (args.semanticText ?? []).map((text) => ({ source: "test", text })),
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
    expect(targets).toContain("workflow");
    expect(targets).toContain("workflow_role");
    expect(targets).toContain("used_at");
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

  test("pack facet vocabulary grounds vocabulary-backed facets", () => {
    const vocabulary: PackFacetVocabulary = {
      schema_version: 1,
      kind: "slot-pack-facet-vocabulary",
      pack_id: "fixture",
      facets: {
        workflow: {
          values: {
            "tfc:casting": {
              label: "Casting",
              description: "Mold-based metal casting.",
              origin: "pack_generated",
              state: "accepted",
            },
            "tfc:maybe": {
              label: "Maybe",
              origin: "pack_generated",
              state: "review",
            },
          },
        },
        progression_stage: {
          values: {
            "pack:fixture/lv_low_voltage": {
              label: "Low Voltage",
              description: "Low Voltage machine age.",
              origin: "pack_generated",
              state: "accepted",
            },
          },
        },
      },
    };
    const prompt = buildSplitPrompt({
      items: [buildItemPayload(ironIngotRecord(), {})],
      target_facets: ["workflow", "progression_stage", "role"],
      facet_vocabulary: buildPromptFacetVocabulary(vocabulary, ["workflow", "progression_stage", "role"]),
    });

    expect(prompt.system).toContain("# Pack facet vocabulary");
    expect(prompt.system).toContain("use only these accepted ids");
    expect(prompt.system).toContain("`tfc:casting`");
    expect(prompt.system).toContain("`pack:fixture/lv_low_voltage`");
    expect(prompt.system).not.toContain("tfc:maybe");
    expect(prompt.system).toContain("Do not invent a syntactically valid id");
    expect(prompt.user).toContain("Do not move ids across vocabulary-backed facets");
  });

  test("split prompt repeats hard output constraints at the end of the user message", () => {
    const prompt = buildSplitPrompt({
      items: [buildItemPayload(ironIngotRecord(), {})],
      target_facets: ["role", "primary_uses", "carry_frequency", "rarity", "mod_subsystem"],
    });

    expect(prompt.user).toContain("# Final response checklist");
    expect(prompt.user).toContain("Include every item id from `items` exactly once");
    expect(prompt.user).toContain("top-level arrays only");
    expect(prompt.user).toContain("Never put them inside `<item_id>.facets`");
    expect(prompt.user).toContain("Use `ambiguous: true` only for single-value enum/free_text facets");
    expect(prompt.user).toContain("Machine parts, machine components, hulls, casings, pumps");
    expect(prompt.user).toContain("Omit `mod_subsystem`; no accepted subsystem vocabulary is supplied");
    expect(prompt.user.trim().endsWith("Optional low-evidence facets are better omitted than guessed.")).toBe(true);
  });

  test("payload carries block context and semantic runtime components", () => {
    const r = ironIngotRecord();
    r.semantic_text = [
      {
        source: "runtime-tooltip",
        text: "Stores heat and can be worked on an anvil.",
      },
      {
        source: "lang",
        key: "item.minecraft.iron_ingot.tooltip",
        text: "Stores heat and can be worked on an anvil.",
      },
    ];
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
    expect(p.semantic_text).toEqual([
      {
        source: "runtime-tooltip",
        text: "Stores heat and can be worked on an anvil.",
      },
      {
        source: "lang",
        key: "item.minecraft.iron_ingot.tooltip",
        text: "Stores heat and can be worked on an anvil.",
      },
    ]);
  });

  test("payload carries gated document context separately from item semantic text", () => {
    const record = runtimeRecord({
      id: "example:gear",
      displayName: "Runtime Gear",
    });
    const siblingRecords = Array.from({ length: 9 }, (_, i) =>
      runtimeRecord({ id: `example:part_${i}`, displayName: `Part ${i}` })
    );
    const evidence: FacetEvidenceArtifact = {
      schema_version: 1,
      kind: "slot-pack-facet-evidence",
      pack_id: "fixture",
      generated_by: "test",
      generated_at: "2026-05-11T00:00:00.000Z",
      source: { runtime_items: "fixture.runtime-items.ndjson", item_count: 10 },
      records: [
        {
          kind: "advancement",
          id: "example:automation/start",
          label: "Start Automation",
          source: "example.jar",
          confidence: 0.65,
          item_refs: ["example:gear", "patchouli:text"],
          semantic_text: [{
            source: "advancement-description",
            text: "Build a gear to begin mechanical automation.",
          }],
        },
        {
          kind: "guide_page",
          id: "example:broad/parts",
          label: "Many Parts",
          source: "example.jar",
          confidence: 0.7,
          item_refs: ["example:gear", ...siblingRecords.map((r) => r.id)],
          semantic_text: [{
            source: "guide-page",
            text: "This broad page names too many parts for direct item context.",
          }],
        },
        {
          kind: "quest_node",
          id: "pack:ftbquests/chapters/automation",
          source: "ftbquests",
          confidence: 0.65,
          item_refs: ["example:gear"],
          semantic_text: [{
            source: "quest-snbt",
            text: "Chapter-level quest prose should not enter stage 3 yet.",
          }],
        },
      ],
      diagnostics: [],
    };

    const { byItem, stats } = buildDocumentContextByItem(evidence, [record, ...siblingRecords]);
    expect(stats.items_with_context).toBe(1);
    expect(stats.skipped_broad_documents).toBe(1);
    expect(stats.skipped_quest_records).toBe(1);

    const payload = buildItemPayload(record, {}, byItem["example:gear"]);
    expect(payload.document_context).toEqual([
      {
        kind: "advancement",
        id: "example:automation/start",
        label: "Start Automation",
        item_ref_count: 1,
        snippets: [{
          source: "advancement-description",
          text: "Build a gear to begin mechanical automation.",
        }],
      },
    ]);
    expect(JSON.stringify(payload.document_context)).not.toContain("quest");
    expect(JSON.stringify(payload.document_context)).not.toContain("broad page");
  });
});

describe("response parsing", () => {
  test("accepts wrapped envelope + fenced JSON", () => {
    const inner = JSON.stringify({
      items: {
        "minecraft:iron_ingot": {
          facets: {
            role: { value: "material", confidence: 0.98, rationale: "ingot" },
            activity: { values: ["slot:building", "slot:combat"], confidence: 0.8 },
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
    expect(item.facets.activity).toMatchObject({ kind: "multi", values: ["slot:building", "slot:combat"] });
  });

  test("drops entries with out-of-enum values, keeps others", () => {
    const response = JSON.stringify({
      items: {
        "minecraft:iron_ingot": {
          facets: {
            role: { value: "nonsense-role", confidence: 0.5 }, // out of enum
            activity: { values: ["slot:building"], confidence: 0.9 }, // ok
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

  test("bare array for a multi facet is accepted as values", () => {
    const response = JSON.stringify({
      items: {
        "minecraft:iron_ingot": {
          facets: {
            primary_uses: ["crafting tools", "anvil repairs"],
          },
        },
      },
    });
    const parsed = parseLlmResponse(response);
    const primaryUses = parsed.items.get("minecraft:iron_ingot")!.facets.primary_uses!;
    expect(primaryUses.kind).toBe("multi");
    if (primaryUses.kind === "multi") {
      expect(primaryUses.values).toEqual(["crafting tools", "anvil repairs"]);
    }
    expect(parsed.warnings).toEqual([]);
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
              values: ["slot:building", "slot:combat"],
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

  test("accepts vocabulary-backed semantic facets with scoped ids", () => {
    const response = JSON.stringify({
      items: {
        "gtceu:steel_ingot": {
          facets: {
            activity: { values: ["slot:automation"], signal: "inferred" },
            workflow: { values: ["pack:tfg2/steelmaking"], signal: "named" },
            workflow_role: { values: ["pack:tfg2/steelmaking#input"], signal: "pattern" },
            used_at: { values: ["gtceu:electric_blast_furnace"], signal: "inferred" },
          },
        },
      },
    });
    const parsed = parseLlmResponse(response);
    const facets = parsed.items.get("gtceu:steel_ingot")!.facets;
    expect(facets.workflow).toMatchObject({
      kind: "multi",
      values: ["pack:tfg2/steelmaking"],
    });
    expect(facets.workflow_role).toMatchObject({
      kind: "multi",
      values: ["pack:tfg2/steelmaking#input"],
    });
    expect(parsed.warnings).toEqual([]);
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

  test("vocabulary_proposals flow through for vocabulary-backed facets", () => {
    const response = JSON.stringify({
      items: {},
      vocabulary_proposals: [
        {
          item: "minecraft:iron_ingot",
          facet: "organization_group",
          label: "Metal Stock",
          proposed_id: "pack:test/metal_stock",
          rationale: "No accepted organization group covers ingots and plates.",
          evidence: ["display name: Iron Ingot"],
        },
      ],
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.vocabularyProposals).toEqual([
      {
        item: "minecraft:iron_ingot",
        facet: "organization_group",
        label: "Metal Stock",
        proposed_id: "pack:test/metal_stock",
        rationale: "No accepted organization group covers ingots and plates.",
        evidence: ["display name: Iron Ingot"],
      },
    ]);
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

  test("fill_ins with values outside deterministic facet enums are dropped", () => {
    const response = JSON.stringify({
      items: {},
      fill_ins: [
        {
          item: "create:mechanical_press",
          facet: "form",
          value: "block",
          rationale: "placed in world",
        },
        {
          item: "gtceu:lv_machine_hull",
          facet: "required_tool",
          value: "wrench",
          rationale: "mineable with wrench",
        },
      ],
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.fillIns.length).toBe(0);
    expect(parsed.warnings.some((w) => w.includes("value 'block' not in enum"))).toBe(true);
    expect(parsed.warnings.some((w) => w.includes("value 'wrench' not in enum"))).toBe(true);
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
            activity: { values: ["slot:building", "slot:combat", "slot:mining"], confidence: 0.9 },
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
    expect(facets.activity).toMatchObject({ values: ["slot:building", "slot:combat", "slot:mining"] });
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

  test("response validator rejects missing requested items before accepting a batch", async () => {
    const records: ItemExtractRecord[] = [
      { ...ironIngotRecord(), id: "minecraft:a", path: "a" },
      { ...ironIngotRecord(), id: "minecraft:b", path: "b" },
    ];
    const stage2: LayerFile = {
      schema_version: 1,
      layer: "vanilla-base",
      source: "minecraft",
      entries: {},
    };
    let calls = 0;
    const client = {
      async query(prompt: string, options: { responseValidator?: (text: string) => { ok: boolean } }) {
        calls++;
        const missing = JSON.stringify({
          items: {
            "minecraft:a": {
              facets: { role: { value: "material", signal: "named", evidence: "test" } },
            },
          },
        });
        if (calls === 1 && options.responseValidator?.(missing).ok === false) {
          return JSON.stringify({
            items: {
              "minecraft:a": {
                facets: { role: { value: "material", signal: "named", evidence: "test" } },
              },
              "minecraft:b": {
                facets: { role: { value: "material", signal: "named", evidence: "test" } },
              },
            },
          });
        }
        return missing;
      },
    };

    const result = await runStage3({
      records,
      stage2Layer: stage2,
      client,
      batchSize: 2,
    });

    expect(calls).toBe(1);
    expect(result.responseMismatches).toEqual([]);
    expect(result.layer.entries["minecraft:a"]?.facets.role).toBeDefined();
    expect(result.layer.entries["minecraft:b"]?.facets.role).toBeDefined();
  });

  test("records a batch failure and continues when the client exhausts retries", async () => {
    const records: ItemExtractRecord[] = [
      { ...ironIngotRecord(), id: "minecraft:a", path: "a" },
      { ...ironIngotRecord(), id: "minecraft:b", path: "b" },
      { ...ironIngotRecord(), id: "minecraft:c", path: "c" },
    ];
    const stage2: LayerFile = {
      schema_version: 1,
      layer: "vanilla-base",
      source: "minecraft",
      entries: {},
    };
    const client = {
      async query(prompt: string) {
        if (prompt.includes("minecraft:a")) {
          throw new Error("response coverage mismatch: missing 1/2 requested item(s)");
        }
        return JSON.stringify({
          items: {
            "minecraft:c": {
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
      batchSize: 2,
      concurrency: 1,
    });

    expect(result.warnings.some((warning) => warning.includes("query failed after retries"))).toBe(true);
    expect(result.responseMismatches).toHaveLength(1);
    expect(result.responseMismatches[0]!.missing).toEqual(["minecraft:a", "minecraft:b"]);
    expect(result.layer.entries["minecraft:a"]?.facets.role).toBeUndefined();
    expect(result.layer.entries["minecraft:b"]?.facets.role).toBeUndefined();
    expect(result.layer.entries["minecraft:c"]?.facets.role).toBeDefined();
  });

  test("response validator accepts out-of-vocabulary values so the parser can drop and report them", async () => {
    const record = ironIngotRecord();
    const stage2: LayerFile = {
      schema_version: 1,
      layer: "modpack",
      source: "fixture",
      entries: {},
    };
    const vocabulary: PackFacetVocabulary = {
      schema_version: 1,
      kind: "slot-pack-facet-vocabulary",
      pack_id: "fixture",
      facets: {
        workflow: {
          values: {
            "tfc:casting": {
              label: "Casting",
              origin: "pack_generated",
              state: "accepted",
            },
          },
        },
      },
    };

    let calls = 0;
    const client = {
      async query(_prompt: string, options: { responseValidator?: (text: string) => { ok: boolean } }) {
        calls++;
        const invented = JSON.stringify({
          items: {
            [record.id]: {
              facets: {
                workflow: { values: ["tfc:invented"], confidence: 0.8, rationale: "made up" },
              },
            },
          },
          vocabulary_proposals: [
            {
              item: record.id,
              facet: "workflow",
              label: "Invented",
              proposed_id: "tfc:invented",
              rationale: "Fixture wants a missing workflow value.",
            },
          ],
        });
        expect(options.responseValidator?.(invented).ok).toBe(true);
        return invented;
      },
    };

    const result = await runStage3({
      records: [record],
      stage2Layer: stage2,
      client,
      targetFacets: ["workflow"],
      facetVocabulary: vocabulary,
    });

    expect(calls).toBe(1);
    expect(result.layer.entries[record.id]?.facets.workflow).toBeUndefined();
    expect(result.vocabularyProposals).toHaveLength(1);
    expect(result.vocabularyProposals[0]!.proposed_id).toBe("tfc:invented");
    expect(result.warnings.some((warning) => warning.includes("tfc:invented"))).toBe(true);
  });

  test("drops invalid vocabulary-backed values from clients that do not honor validators", async () => {
    const record = ironIngotRecord();
    const stage2: LayerFile = {
      schema_version: 1,
      layer: "modpack",
      source: "fixture",
      entries: {},
    };
    const vocabulary: PackFacetVocabulary = {
      schema_version: 1,
      kind: "slot-pack-facet-vocabulary",
      pack_id: "fixture",
      facets: {
        workflow: {
          values: {
            "tfc:casting": {
              label: "Casting",
              origin: "pack_generated",
              state: "accepted",
            },
          },
        },
      },
    };
    const client = {
      async query() {
        return JSON.stringify({
          items: {
            [record.id]: {
              facets: {
                workflow: {
                  values: ["tfc:casting", "tfc:invented"],
                  confidence: 0.8,
                  rationale: "mixed accepted and invented",
                },
              },
            },
          },
        });
      },
    };

    const result = await runStage3({
      records: [record],
      stage2Layer: stage2,
      client,
      targetFacets: ["workflow"],
      facetVocabulary: vocabulary,
    });

    expect(result.layer.entries[record.id]?.facets.workflow).toMatchObject({
      values: ["tfc:casting"],
      source: "llm:stage3",
    });
    expect(result.warnings.some((warning) => warning.includes("tfc:invented"))).toBe(true);
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

    const got = await client.query(prompt, { model: "deepseek/deepseek-v4-flash" });
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
    await client.query(prompt, { model: "deepseek/deepseek-v4-flash" });
    expect(innerCalled).toBe(1);

    // Second call with same prompt should hit cache
    await client.query(prompt, { model: "deepseek/deepseek-v4-flash" });
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

    await client.querySplit!("SYSTEM", "USER A", { model: "openai/gpt-4.1-mini" });
    await client.querySplit!("SYSTEM", "USER A", { model: "openai/gpt-4.1-mini" }); // cache hit
    await client.querySplit!("SYSTEM", "USER B", { model: "openai/gpt-4.1-mini" }); // different user → miss

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
      model: "openai/gpt-4.1-mini",
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
