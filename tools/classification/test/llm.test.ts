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

  test("defaultTargetFacets includes semantic classifier targets and excludes raw-only facts", () => {
    const targets = defaultTargetFacets();
    expect(targets).toContain("role");
    expect(targets).toContain("material_family");
    expect(targets).toContain("form");
    expect(targets).toContain("dye_color");
    expect(targets).toContain("emits_light");
    expect(targets).toContain("activity");
    expect(targets).toContain("workflow");
    expect(targets).toContain("workflow_role");
    expect(targets).toContain("used_at");
    expect(targets).toContain("primary_uses");
    expect(targets).toContain("biome");
    expect(targets).toContain("y_level_range");
    // Exact/raw facts stay out of the default LLM pass.
    expect(targets).not.toContain("mod_namespace");
    expect(targets).not.toContain("is_stackable");
    expect(targets).not.toContain("processing_in");
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
    expect(p.recipe_ingredient_examples!.length).toBeGreaterThan(10);
    expect(p.recipe_ingredient_examples!.length).toBeLessThanOrEqual(24);
    expect(p.recipe_output_examples!.length).toBeLessThanOrEqual(24);
    expect(p.loot_source_examples!.length).toBeGreaterThan(10);
    expect(p.loot_source_examples!.length).toBeLessThanOrEqual(16);
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
    expect(p.minecraft_tags_direct).toBeUndefined();
    expect(p.minecraft_tags_inherited).toBeUndefined();
    expect(p.minecraft_tags_resolved).toEqual(["forge:ingots", "forge:ingots/iron"]);
  });

  test("split prompt includes stable input evidence interpretation notes", () => {
    const staticPayload = buildItemPayload(ironIngotRecord(), {});
    const staticPrompt = buildSplitPrompt({
      items: [staticPayload],
      target_facets: ["role"],
    });
    expect(staticPrompt.system).toContain("# Input evidence notes");

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
    expect(runtimePrompt.system).toContain("# Input evidence notes");
    expect(runtimePrompt.system).toBe(staticPrompt.system);
    expect(runtimePrompt.system).toContain("KubeJS and datapack");
    expect(runtimePrompt.system).toContain("no useful collected evidence");
    expect(runtimePrompt.system).toContain("Recipe absences are weaker");
    expect(runtimePrompt.system).toContain("Emit `primary_uses` for every item");
    expect(runtimePrompt.system).toContain("Empty loot/source");
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
            "lv_low_voltage": {
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
      pack_id: "fixture",
      items: [buildItemPayload(ironIngotRecord(), {})],
      target_facets: ["workflow", "progression_stage", "role"],
      facet_vocabulary: buildPromptFacetVocabulary(vocabulary, ["workflow", "progression_stage", "role"]),
    });

    expect(prompt.system).toContain("# Pack facet vocabulary");
    expect(prompt.system).toContain("grounding values supplied to this classification batch");
    expect(prompt.system).toContain("Use listed values for the matching facet whenever they fit");
    expect(prompt.system).toContain("state: review");
    expect(prompt.system).toContain("usable by default");
    expect(prompt.system).toContain("Aliases are matching hints only, not output values");
    expect(prompt.system).toContain("Pack id for review context: `fixture`");
    expect(prompt.system).toContain("`tfc:casting`");
    expect(prompt.system).toContain("`tfc:maybe`");
    expect(prompt.system).toContain("`lv_low_voltage`");
    expect(prompt.system).toContain("add a top-level vocabulary_proposals entry");
    expect(prompt.user).toContain("use values listed for that exact facet");
    expect(prompt.user).toContain("emit it inside `facets` with `vocab_review: true`");
    expect(prompt.user).toContain("Do not move values across vocabulary-backed facets");
    expect(prompt.user).toContain("Vocabulary aliases are matching hints, not output values");
    expect(prompt.user).toContain("`used_at` is a physical station, machine, tool, or surface");
    expect(prompt.user).toContain("rather than an invented near-miss value such as `crafting`");
    expect(prompt.user).toContain("emit exactly one value for every item");
    expect(prompt.user).toContain("Use listed built-in homes such as `storage`");
    expect(prompt.user).toContain("unlisted organization group only when the listed homes would be genuinely misleading");
    expect(prompt.user).toContain("missing broad player-maintained storage bucket");
  });

  test("classification prompt leaves judgment room for free-text and vocabulary-backed facets", () => {
    const prompt = buildSplitPrompt({
      items: [buildItemPayload(ironIngotRecord(), {})],
      target_facets: ["primary_uses", "material_secondary", "workflow", "role"],
    });

    expect(prompt.system).toContain("ordinary non-vocabulary free_text / multi_free_text facets are judgment outputs");
    expect(prompt.system).toContain("synthesize concise values matching the pattern");
    expect(prompt.system).toContain("vocabulary-backed facets are grounded by the usable Pack facet vocabulary");
    expect(prompt.system).toContain("This is a semantic classification task, not just a storage-section task");
    expect(prompt.system).toContain("lower-risk semantic/query metadata");
    expect(prompt.system).toContain("Do not go silent merely because the");
    expect(prompt.system).toContain("grounding vocabulary is incomplete");
    expect(prompt.system).toContain("This does not apply to ordinary");
    expect(prompt.system).toContain("Do not use this for ordinary free_text values");
    expect(prompt.system).toContain("Keep facet entries small");
    expect(prompt.system).toContain("Optional per-facet review fields");
    expect(prompt.system).toContain("Default to useful judgment, not silence");
    expect(prompt.system).toContain("a reasonable inferred value is usually better than leaving the facet empty");
    expect(prompt.system).toContain("`organization_group` is the high-impact primary home facet");
    expect(prompt.system).toContain("assign exactly one best organization value for every item");
    expect(prompt.system).toContain("judgment is allowed");
    expect(prompt.system).toContain("normal staple rather than rare progression stock");
    expect(prompt.system).not.toContain("`palette` is not the vanilla dye-color facet");
    expect(prompt.system).not.toContain("`metallic` is a `flavor` value");
    expect(prompt.system).not.toContain("`flavor` is not a catch-all visual palette");
    expect(prompt.system).toContain("`document_context` is input evidence, not an output facet");
    expect(prompt.system).toContain("where this sits in pack progression");
    expect(prompt.system).toContain("favor useful judgment");
    expect(prompt.system).not.toContain("stage2_facets");
    expect(prompt.system).not.toContain("deterministic scalar facts inside an item's `facets` block");
    expect(prompt.system).not.toContain("fill_ins");
    expect(prompt.system).not.toContain("corrections");
    expect(prompt.system).toContain("Guide snippets often describe several related items");
    expect(prompt.system).toContain("what the current item is for");
    expect(prompt.system).toContain("not every use of the bowl");
    expect(prompt.system).not.toContain("Optional per-facet review fields: `rationale`, `evidence`, `signal`, and `confidence`");
    expect(prompt.system).not.toContain("MUST include a `signal` field");
    expect(prompt.system).not.toContain("<accepted_id_from_pack_facet_vocabulary>");
  });

  test("organization and subsystem guidance does not teach stale generated ids or role bans", () => {
    const prompt = buildSplitPrompt({
      items: [buildItemPayload(ironIngotRecord(), {})],
      target_facets: ["organization_group", "mod_subsystem", "role"],
    });

    expect(prompt.system).not.toContain("pack:example/casting_molds");
    expect(prompt.system).not.toContain("Use the item's own namespace unless");
    expect(prompt.system).toContain("Concrete anchors use usable vocabulary labels, not literal ids");
    expect(prompt.system).toContain("use the listed Beekeeping organization value if it is listed");
    expect(prompt.system).toContain("the Storage exception is for actual storage containers");
    expect(prompt.system).toContain("every item must get exactly one");
    expect(prompt.system).toContain("Built-in groups are good player homes");
    expect(prompt.system).toContain("explain the broad");
    expect(prompt.system).toContain("top-level `vocabulary_proposals` entry");
    expect(prompt.system).toContain("Role is a cross-check,");
    expect(prompt.system).toContain("not a hard ban");
    expect(prompt.system).toContain("role=functional_block or storage_block");
    expect(prompt.system).toContain("identity, not namespace or recipe participation");
    expect(prompt.system).not.toContain("casting_molds");
    expect(prompt.system).not.toContain("pack:tfg2/crops");
    expect(prompt.system).not.toContain("such as casting molds, crops, woodworking");
    expect(prompt.system).toContain("examples omitted: vocabulary values are pack-specific");
  });

  test("expected output example uses usable vocabulary values only when supplied", () => {
    const record = buildItemPayload(ironIngotRecord(), {});
    const noVocabulary = buildSplitPrompt({
      items: [record],
      target_facets: ["organization_group"],
    });
    expect(noVocabulary.system).toContain('"facets": {}');
    expect(noVocabulary.system).not.toContain("<accepted_id_from_pack_facet_vocabulary>");

    const defaultVocabulary = buildPromptFacetVocabulary(undefined, ["organization_group"]);
    expect(defaultVocabulary).toBeUndefined();

    const vocabulary: PackFacetVocabulary = {
      schema_version: 1,
      kind: "slot-pack-facet-vocabulary",
      pack_id: "fixture",
      facets: {
        organization_group: {
          values: {
            "food": {
              label: "Food",
              origin: "built_in",
              state: "accepted",
            },
            "metal_stock": {
              label: "Metal Stock",
              origin: "built_in",
              state: "accepted",
            },
            "beekeeping": {
              label: "Beekeeping",
              origin: "pack_generated",
              state: "accepted",
            },
          },
        },
      },
    };
    const withVocabulary = buildSplitPrompt({
      pack_id: "fixture",
      items: [record],
      target_facets: ["organization_group"],
      facet_vocabulary: buildPromptFacetVocabulary(vocabulary, ["organization_group"]),
    });
    expect(withVocabulary.system).toContain("`food`");
    expect(withVocabulary.system).toContain("`beekeeping`");
    expect(withVocabulary.system).toContain("`metal_stock`");
    expect(withVocabulary.system).not.toContain("<accepted_id_from_pack_facet_vocabulary>");
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
    expect(prompt.user).toContain("Then use judgment to fill useful semantic/query facets");
    expect(prompt.user).not.toContain("stage2_facets");
    expect(prompt.user).not.toContain("deterministic scalar facts");
    expect(prompt.user).not.toContain("fill_ins");
    expect(prompt.user).not.toContain("corrections");
    expect(prompt.user).toContain("No usable Pack facet vocabulary is supplied");
    expect(prompt.user).toContain("No usable subsystem vocabulary is supplied for this batch");
    expect(prompt.user).toContain("emit the `mod_subsystem` value with `vocab_review: true`");
    expect(prompt.user).toContain("Organization group is required when targeted");
    expect(prompt.user).not.toContain("Optional low-evidence facets are better omitted than guessed.");
    expect(prompt.user).not.toContain("Omit `mod_subsystem`; no accepted subsystem vocabulary is supplied");
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
    expect(p.component_highlights!["minecraft:max_damage"]).toBe(250);
    expect(p.component_highlights!["minecraft:equippable"]).toEqual({ slot: "head" });
    expect(p.component_highlights!["minecraft:light_emission"]).toBe(14);
    expect(p.semantic_text).toEqual([{
      source: "runtime-tooltip",
      text: "Stores heat and can be worked on an anvil.",
    }]);
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
        label: "Start Automation",
        snippets: ["Build a gear to begin mechanical automation."],
      },
    ]);
    expect(JSON.stringify(payload.document_context)).not.toContain("quest");
    expect(JSON.stringify(payload.document_context)).not.toContain("broad page");
  });

  test("document context focuses snippets when the page label is about another item", () => {
    const text = "Bowls are a versatile tool which can be used to make Salads, to make Soups, to Salt meat, or to apply Powder to Glass in order to change the resulting glass's color.";
    const salt = buildItemPayload(runtimeRecord({
      id: "tfc:powder/salt",
      displayName: "Table Salt",
    }), {}, [{
      kind: "guide_page",
      id: "tfc:field_guide/food/bowls",
      label: "Bowls",
      item_ref_count: 2,
      related_item_refs: ["tfc:ceramic/bowl"],
      snippets: [{ source: "guide-page", text }],
    }]);
    expect(salt.document_context?.[0]?.snippets).toEqual(["to Salt meat"]);

    const bowl = buildItemPayload(runtimeRecord({
      id: "tfc:ceramic/bowl",
      displayName: "Ceramic Bowl",
    }), {}, [{
      kind: "guide_page",
      id: "tfc:field_guide/food/bowls",
      label: "Bowls",
      item_ref_count: 2,
      related_item_refs: ["tfc:powder/salt"],
      snippets: [{ source: "guide-page", text }],
    }]);
    expect(bowl.document_context?.[0]?.snippets).toEqual([text]);
  });
});

describe("response parsing", () => {
  test("accepts wrapped envelope + fenced JSON", () => {
    const inner = JSON.stringify({
      items: {
        "minecraft:iron_ingot": {
          facets: {
            role: { value: "material", rationale: "ingot" },
            activity: { values: ["building", "combat"] },
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

  test("drops entries with out-of-enum values for true closed facets, keeps others", () => {
    const response = JSON.stringify({
      items: {
        "minecraft:iron_ingot": {
          facets: {
            rarity: { value: "legendary" }, // out of enum
            activity: { values: ["building"] }, // ok
          },
        },
      },
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.items.get("minecraft:iron_ingot")!.facets.rarity).toBeUndefined();
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
            primary_uses: { value: "crafting stock" },
          },
        },
      },
    });
    const parsed = parseLlmResponse(response);
    const primaryUses = parsed.items.get("minecraft:iron_ingot")!.facets.primary_uses!;
    expect(primaryUses.kind).toBe("multi");
    if (primaryUses.kind === "multi") {
      expect(primaryUses.values).toEqual(["crafting stock"]);
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

  test("evidence is folded into rationale and stale confidence/signal fields are ignored", () => {
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
    expect(role.rationale).toContain("tag minecraft:iron_tool_materials");
  });

  test("stale signal without evidence does not manufacture rationale", () => {
    const response = JSON.stringify({
      items: {
        "minecraft:mystery": {
          facets: {
            role: { value: "curiosity", signal: "guess", evidence: "" },
          },
        },
      },
    });
    const parsed = parseLlmResponse(response);
    const role = parsed.items.get("minecraft:mystery")!.facets.role!;
    expect(role.rationale).toBeUndefined();
  });

  test("stale signal=pattern without evidence is accepted but ignored", () => {
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
    expect(role.rationale).toBeUndefined();
    expect(parsed.warnings.length).toBe(0);
  });

  test("stale signal does not alter parsed value", () => {
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
    expect(parsed.items.get("minecraft:x")!.facets.role).toMatchObject({
      kind: "single",
      value: "tool",
    });
  });

  test("multi facet preserves evidence; rationale folds evidence and rationale", () => {
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
    expect(a.rationale).toContain("ingredient_of");
    expect(a.rationale).toContain("structural usage");
  });

  test("accepts vocabulary-backed semantic facets with scoped ids", () => {
    const response = JSON.stringify({
      items: {
        "gtceu:steel_ingot": {
          facets: {
            activity: { values: ["automation"], signal: "inferred" },
            workflow: { values: ["steelmaking"], signal: "named" },
            workflow_role: { values: ["steelmaking#input"], signal: "pattern" },
            used_at: { values: ["gtceu:electric_blast_furnace"], signal: "inferred" },
          },
        },
      },
    });
    const parsed = parseLlmResponse(response);
    const facets = parsed.items.get("gtceu:steel_ingot")!.facets;
    expect(facets.workflow).toMatchObject({
      kind: "multi",
      values: ["steelmaking"],
    });
    expect(facets.workflow_role).toMatchObject({
      kind: "multi",
      values: ["steelmaking#input"],
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
          proposed_id: "metal_stock",
          rationale: "No usable organization group covers ingots and plates.",
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
        proposed_id: "metal_stock",
        rationale: "No usable organization group covers ingots and plates.",
        evidence: ["display name: Iron Ingot"],
      },
    ]);
  });

  test("corrections flow through without confidence metadata", () => {
    const response = JSON.stringify({
      items: {},
      corrections: [
        {
          item: "minecraft:iron_ingot",
          facet: "material_family",
          current: "wood_oak",
          suggested: "iron",
          rationale: "Item is clearly iron; stage 2 misread the tag.",
        },
        {
          item: "minecraft:foo",
          facet: "form",
          current: "ingot",
          suggested: "nugget",
          rationale: "uncertain",
        },
      ],
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.corrections.length).toBe(2);
    expect(parsed.corrections[0]!.item).toBe("minecraft:iron_ingot");
    expect(parsed.corrections[1]).toMatchObject({
      item: "minecraft:foo",
      facet: "form",
    });
    expect(parsed.warnings.some((w) => w.includes("below confidence"))).toBe(false);
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

  test("fill_ins compatibility channel routes reference-style facts to fillIns array", () => {
    const response = JSON.stringify({
      items: {},
      fill_ins: [
        {
          item: "create:dark_oak_window",
          facet: "mod_namespace",
          value: "create",
          rationale: "runtime id namespace",
        },
        {
          item: "create:brass_pipe",
          facet: "is_fuel",
          value: true,
          rationale: "runtime fuel evidence",
        },
        {
          item: "gtceu:brass_double_ingot",
          facet: "emits_light",
          value: true,
        },
      ],
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.fillIns.length).toBe(3);
    expect(parsed.fillIns[0]!.item).toBe("create:dark_oak_window");
    expect(parsed.fillIns[0]!.facet).toBe("mod_namespace");
    expect(parsed.fillIns[0]!.value).toBe("create");
    expect(parsed.fillIns[2]!.rationale).toBe("LLM did not provide rationale.");
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
          facet: "carry_frequency",
          value: "frequent",
          rationale: "obvious",
        },
      ],
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.fillIns.length).toBe(0);
    expect(parsed.warnings.some((w) => w.includes("llm-authored"))).toBe(true);
  });

  test("fill_ins for vocabulary-backed facets are dropped with a warning", () => {
    const response = JSON.stringify({
      items: {},
      fill_ins: [
        {
          item: "minecraft:iron_ingot",
          facet: "material_family",
          value: "iron",
          rationale: "material values must be vocabulary-grounded",
        },
      ],
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.fillIns.length).toBe(0);
    expect(parsed.warnings.some((w) => w.includes("vocabulary-backed"))).toBe(true);
  });

  test("fill_ins with values outside deterministic facet enums are dropped", () => {
    const response = JSON.stringify({
      items: {},
      fill_ins: [
        {
          item: "create:mechanical_press",
          facet: "rarity",
          value: "legendary",
          rationale: "placed in world",
        },
        {
          item: "gtceu:lv_machine_hull",
          facet: "dye_color",
          value: "teal",
          rationale: "colored texture",
        },
      ],
    });
    const parsed = parseLlmResponse(response);
    expect(parsed.fillIns.length).toBe(0);
    expect(parsed.warnings.some((w) => w.includes("value 'legendary' not in enum"))).toBe(true);
    expect(parsed.warnings.some((w) => w.includes("value 'teal' not in enum"))).toBe(true);
  });
});

describe("runStage3", () => {
  test("round-trips a fixture through prompt -> replay client -> merge", async () => {
    const record = ironIngotRecord();
    const stage2Layer = ironIngotStage2Layer();

    // runStage3 prefers querySplit when the client implements it (which
    // ReplayLlmClient does). The fixture must be hashed under the split-mode
    // key, not the combined-prompt key.
    const targetFacets = defaultTargetFacets();
    const facetVocabulary: PackFacetVocabulary = {
      schema_version: 1,
      kind: "slot-pack-facet-vocabulary",
      pack_id: "fixture",
      facets: {
        activity: {
          values: {
            building: { label: "Building", origin: "pack_generated", state: "accepted" },
            combat: { label: "Combat", origin: "pack_generated", state: "accepted" },
            mining: { label: "Mining", origin: "pack_generated", state: "accepted" },
          },
        },
      },
    };
    const { system, user } = buildSplitPrompt({
      pack_id: "fixture",
      items: [buildItemPayload(record, stage2Layer.entries["minecraft:iron_ingot"]!.facets)],
      target_facets: targetFacets,
      facet_vocabulary: buildPromptFacetVocabulary(facetVocabulary, targetFacets),
    });
    const hash = fixtureHash(`${system}\n\n---\n\n${user}`);

    const fixtureDir = mkdtempSync(join(tmpdir(), "slot-stage3-"));
    const response = JSON.stringify({
      items: {
        "minecraft:iron_ingot": {
          facets: {
            role: { value: "material", rationale: "canonical ingot" },
            activity: { values: ["building", "combat", "mining"] },
            primary_uses: {
              values: ["crafting tools and armor", "anvil repairs"],
            },
            carry_frequency: { value: "frequent" },
            // material_family is vocabulary-backed; without a usable material
            // vocabulary value this should be kept but marked for review.
            material_family: { value: "gold" },
          },
        },
      },
    });
    writeFileSync(join(fixtureDir, `${hash}.response.txt`), response);

    const client = new ReplayLlmClient(fixtureDir);
    const result = await runStage3({
      records: [record],
      baseLayer: stage2Layer,
      client,
      facetVocabulary,
    });

    const facets = result.layer.entries["minecraft:iron_ingot"]!.facets;
    // stage-2 facets preserved
    expect(facets.mod_namespace).toMatchObject({ value: "minecraft" });
    expect(facets.material_family).toMatchObject({
      value: "gold",
      source: "llm:stage3",
      vocab_review: true,
    });
    // stage-3 facets merged
    expect(facets.role).toMatchObject({ value: "material", source: "llm:stage3" });
    expect(facets.activity).toMatchObject({ values: ["building", "combat", "mining"] });
    expect(facets.primary_uses).toMatchObject({
      values: ["anvil repairs", "crafting tools and armor"],
    });

    const vocabularyWarn = result.warnings.find((w) =>
      w.includes("material_family") && w.includes("not listed in prompt vocabulary"),
    );
    expect(vocabularyWarn).toBeTruthy();
    expect(result.vocabularyProposals).toContainEqual(expect.objectContaining({
      item: "minecraft:iron_ingot",
      facet: "material_family",
      proposed_id: "gold",
    }));

    expect(result.filledItems).toBe(1);
    expect(result.coverageAdded.role).toBe(1);
    expect(result.coverageAdded.activity).toBe(1);
  });

  test("LLM facets replace same-id base facets instead of being suppressed", async () => {
    const record = ironIngotRecord();
    const baseLayer = ironIngotStage2Layer();

    const targetFacets = ["form"];
    const { system, user } = buildSplitPrompt({
      items: [buildItemPayload(record, {})],
      target_facets: targetFacets,
    });
    const hash = fixtureHash(`${system}\n\n---\n\n${user}`);
    const fixtureDir = mkdtempSync(join(tmpdir(), "slot-stage3-overwrite-"));
    writeFileSync(join(fixtureDir, `${hash}.response.txt`), JSON.stringify({
      items: {
        "minecraft:iron_ingot": {
          facets: {
            form: { value: "nugget", rationale: "fixture disagreement" },
          },
        },
      },
    }));

    const result = await runStage3({
      records: [record],
      baseLayer,
      client: new ReplayLlmClient(fixtureDir),
      targetFacets,
    });

    expect(result.layer.entries["minecraft:iron_ingot"]!.facets.form).toMatchObject({
      value: "nugget",
      source: "llm:stage3",
    });
    expect(result.warnings.some((warning) =>
      warning.includes("form") && warning.includes("replaced base value")
    )).toBe(true);
  });

  test("reference-style scalar facets emitted in facets merge as ordinary LLM facets", async () => {
    const record = ironIngotRecord();
    const stage2Layer = ironIngotStage2Layer();

    const targetFacets = defaultTargetFacets();
    const { system, user } = buildSplitPrompt({
      items: [buildItemPayload(record, stage2Layer.entries["minecraft:iron_ingot"]!.facets)],
      target_facets: targetFacets,
      facet_vocabulary: buildPromptFacetVocabulary(undefined, targetFacets),
    });
    const hash = fixtureHash(`${system}\n\n---\n\n${user}`);
    const fixtureDir = mkdtempSync(join(tmpdir(), "slot-stage3-fillin-"));
    const response = JSON.stringify({
      items: {
        "minecraft:iron_ingot": {
          facets: {
            emits_light: { value: true, rationale: "component says light" },
          },
        },
      },
    });
    writeFileSync(join(fixtureDir, `${hash}.response.txt`), response);

    const result = await runStage3({
      records: [record],
      baseLayer: stage2Layer,
      client: new ReplayLlmClient(fixtureDir),
    });

    const facets = result.layer.entries["minecraft:iron_ingot"]!.facets;
    expect(facets.emits_light).toMatchObject({
      value: true,
      source: "llm:stage3",
    });
    expect(result.fillIns).toEqual([]);
    expect(result.warnings.some((w) =>
      w.includes("emits_light") && w.includes("treated as fill_in")
    )).toBe(false);
  });

  test("listed vocabulary proposals are repaired into facet values", async () => {
    const record = runtimeRecord({
      id: "gtmutils:uhv_auto_charger_4x",
      displayName: "4x UHV Auto Charger",
    });
    const stage2Layer: LayerFile = {
      schema_version: 1,
      layer: "modpack",
      source: "fixture",
      entries: {
        "gtmutils:uhv_auto_charger_4x": {
          facets: {},
        },
      },
    };
    const vocabulary: PackFacetVocabulary = {
      schema_version: 1,
      kind: "slot-pack-facet-vocabulary",
      pack_id: "fixture",
      facets: {
        progression_stage: {
          values: {
            "gtceu:uhv": {
              label: "UHV",
              origin: "manual",
              state: "accepted",
            },
          },
        },
      },
    };

    const targetFacets = defaultTargetFacets();
    const { system, user } = buildSplitPrompt({
      pack_id: "fixture",
      items: [buildItemPayload(record, {})],
      target_facets: targetFacets,
      facet_vocabulary: buildPromptFacetVocabulary(vocabulary, targetFacets),
    });
    const hash = fixtureHash(`${system}\n\n---\n\n${user}`);
    const fixtureDir = mkdtempSync(join(tmpdir(), "slot-stage3-vocab-proposal-repair-"));
    writeFileSync(join(fixtureDir, `${hash}.response.txt`), JSON.stringify({
      items: {
        "gtmutils:uhv_auto_charger_4x": {
          facets: {},
        },
      },
      vocabulary_proposals: [
        {
          item: "gtmutils:uhv_auto_charger_4x",
          facet: "progression_stage",
          label: "UHV",
          proposed_id: "gtceu:uhv",
          rationale: "The item is explicitly UHV tier.",
        },
      ],
    }));

    const result = await runStage3({
      records: [record],
      baseLayer: stage2Layer,
      client: new ReplayLlmClient(fixtureDir),
      facetVocabulary: vocabulary,
    });

    expect(result.layer.entries["gtmutils:uhv_auto_charger_4x"]!.facets.progression_stage)
      .toMatchObject({ values: ["gtceu:uhv"], source: "llm:stage3" });
    expect(result.vocabularyProposals).toEqual([]);
    expect(result.warnings.some((w) => w.includes("listed vocabulary proposal"))).toBe(true);
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
      baseLayer: stage2,
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
      baseLayer: stage2,
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
      baseLayer: stage2,
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

  test("response validator accepts out-of-vocabulary values so runner keeps and flags them", async () => {
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
      baseLayer: stage2,
      client,
      targetFacets: ["workflow"],
      facetVocabulary: vocabulary,
    });

    expect(calls).toBe(1);
    expect(result.layer.entries[record.id]?.facets.workflow).toMatchObject({
      values: ["tfc:invented"],
      source: "llm:stage3",
      vocab_review: true,
    });
    expect(result.vocabularyProposals).toHaveLength(1);
    expect(result.vocabularyProposals[0]!.proposed_id).toBe("tfc:invented");
    expect(result.warnings.some((warning) => warning.includes("tfc:invented"))).toBe(true);
  });

  test("marks mixed listed and unlisted vocabulary-backed values for review", async () => {
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
      baseLayer: stage2,
      client,
      targetFacets: ["workflow"],
      facetVocabulary: vocabulary,
    });

    expect(result.layer.entries[record.id]?.facets.workflow).toMatchObject({
      values: ["tfc:casting", "tfc:invented"],
      source: "llm:stage3",
      vocab_review: true,
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
      baseLayer: stage2,
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

  test("flags items with ambiguous: true", () => {
    const l = layer({
      "minecraft:a": {
        role: {
          values: ["material", "natural_resource"],
          ambiguous: true,
          source: "llm:stage3",
        },
      },
      "minecraft:b": {
        role: { value: "tool", source: "llm:stage3" },
      },
    });
    expect(selectRetryCandidates(l)).toEqual(["minecraft:a"]);
  });

  test("ignores non-LLM ambiguous base facets", () => {
    const l = layer({
      "minecraft:a": {
        form: { values: ["ingot", "nugget"], ambiguous: true, source: "rule:foo" },
        role: { value: "material", source: "llm:stage3" },
      },
    });
    expect(selectRetryCandidates(l)).toEqual([]);
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

  test("retry replaces ambiguous first-pass facet with retry result", async () => {
    const firstPassLayer: LayerFile = {
      schema_version: 1,
      layer: "vanilla-base",
      source: "minecraft",
      entries: {
        "minecraft:mystery": {
          facets: {
            role: { values: ["curiosity", "utility"], ambiguous: true, source: "llm:stage3" },
          },
        },
      },
    };
    const retryResponse = JSON.stringify({
      items: {
        "minecraft:mystery": {
          facets: {
            role: { value: "utility", rationale: "specific behaviour" },
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
      model: "openai/gpt-4.1-mini",
    });

    expect(result.retriedItems).toEqual(["minecraft:mystery"]);
    const role = result.layer.entries["minecraft:mystery"]!.facets.role as { value: string; source: string };
    expect(role.value).toBe("utility");
    expect(role.source).toBe("llm:stage3-retry");
    expect(result.facetsChanged.role).toBe(1);
    expect(result.facetsConfirmed.role ?? 0).toBe(0);
  });

  test("retry accepts different retry value for an ambiguous candidate", async () => {
    const firstPassLayer: LayerFile = {
      schema_version: 1,
      layer: "vanilla-base",
      source: "minecraft",
      entries: {
        "minecraft:mystery": {
          facets: {
            role: { values: ["curiosity", "utility"], ambiguous: true, source: "llm:stage3" },
          },
        },
      },
    };
    const retryResponse = JSON.stringify({
      items: {
        "minecraft:mystery": {
          facets: {
            role: { value: "utility" },
          },
        },
      },
    });
    const retryClient = { async query() { return retryResponse; } };

    const result = await runStage3Retry({
      records: [baseRecord()],
      firstPassLayer,
      client: retryClient,
    });

    const role = result.layer.entries["minecraft:mystery"]!.facets.role as { value: string };
    expect(role.value).toBe("utility");
    expect(result.facetsChanged.role).toBe(1);
  });

  test("retry confirms same value and counts as confirmed, not changed", async () => {
    const firstPassLayer: LayerFile = {
      schema_version: 1,
      layer: "vanilla-base",
      source: "minecraft",
      entries: {
        "minecraft:mystery": {
          facets: {
            role: { values: ["material", "natural_resource"], ambiguous: true, source: "llm:stage3" },
          },
        },
      },
    };
    const retryResponse = JSON.stringify({
      items: {
        "minecraft:mystery": {
          facets: {
            role: { values: ["material", "natural_resource"], ambiguous: true },
          },
        },
      },
    });
    const retryClient = { async query() { return retryResponse; } };

    const result = await runStage3Retry({
      records: [baseRecord()],
      firstPassLayer,
      client: retryClient,
    });

    expect(result.facetsConfirmed.role).toBe(1);
    expect(result.facetsChanged.role ?? 0).toBe(0);
    const role = result.layer.entries["minecraft:mystery"]!.facets.role as { source: string };
    expect(role.source).toBe("llm:stage3-retry");
  });

  test("no candidates → noop retry", async () => {
    const firstPassLayer: LayerFile = {
      schema_version: 1,
      layer: "vanilla-base",
      source: "minecraft",
      entries: {
        "minecraft:x": {
          facets: {
            role: { value: "material", source: "llm:stage3" },
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
    });
    expect(queried).toBe(false);
    expect(result.retriedItems).toEqual([]);
  });
});
