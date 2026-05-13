import { createHash } from "node:crypto";
import { describe, expect, test } from "bun:test";
import type { LlmClient, QueryOptions } from "../src/llm/client.ts";
import type { FacetEvidenceArtifact } from "../src/evidence/facet_evidence.ts";
import { validateVocabularyArtifact, type PackFacetVocabulary } from "../src/schema/vocabulary.ts";
import {
  buildVocabularyCurationPrompt,
  extractVocabularyCandidates,
  parseVocabularyCurationResponse,
  proposePackFacetVocabulary,
  type PackVocabularyCandidate,
} from "../src/vocabulary/pack_vocabulary.ts";
import { selectPromptCandidates } from "../src/vocabulary/selection.ts";

describe("pack facet vocabulary generation", () => {
  test("extracts universal defaults and evidence-backed workflow candidates", () => {
    const evidence = fixtureEvidence();
    const candidates = extractVocabularyCandidates(evidence, {
      packId: "fixture",
      minEvidence: 2,
      facets: ["activity", "workflow", "workflow_role", "organization_group", "food_category", "use_affordance"],
    });

    expect(candidates.find((candidate) => candidate.facet === "activity" && candidate.id === "slot:cooking")?.suggested_state).toBe("accepted");
    expect(candidates.find((candidate) => candidate.facet === "use_affordance" && candidate.id === "slot:open")?.suggested_state).toBe("accepted");
    expect(candidates.find((candidate) => candidate.facet === "workflow" && candidate.id === "example:casting")?.suggested_state).toBe("accepted");
    expect(candidates.find((candidate) => candidate.facet === "workflow" && candidate.id === "example:casting")?.semantic_evidence.some((entry) =>
      entry.text?.includes("Reusable mold")
    )).toBe(true);
    expect(candidates.find((candidate) => candidate.facet === "workflow" && candidate.id === "example:casting/ingot")).toBeUndefined();
    expect(candidates.find((candidate) => candidate.facet === "workflow_role" && candidate.id === "example:casting#input")?.parent).toBe("example:casting");
    expect(candidates.find((candidate) => candidate.facet === "organization_group" && candidate.id === "example:casting")).toBeUndefined();
    expect(candidates.find((candidate) => candidate.facet === "organization_group" && candidate.id === "pack:fixture/casting_molds")?.suggested_state).toBe("review");
    expect(candidates.find((candidate) => candidate.facet === "food_category" && candidate.id === "slot:fruit")?.evidence[0]?.kind).toBe("item_tag");
  });

  test("extracts mod_subsystem candidates from pack vocabulary evidence", () => {
    const evidence = fixtureEvidence();
    evidence.records.push(
      {
        kind: "guide_page",
        id: "create:ponder/kinetics",
        label: "Kinetics",
        namespace: "create",
        source: "ponder",
        confidence: 0.9,
        semantic_text: [{ source: "ponder", text: "Kinetic blocks move power through shafts and cogs." }],
      },
      {
        kind: "guide_page",
        id: "create:ponder/belts/using_mechanical_belts",
        label: "Using Mechanical Belts",
        namespace: "create",
        source: "ponder",
        confidence: 0.9,
        semantic_text: [{ source: "ponder", text: "Belts move items and entities between machines." }],
      },
      {
        kind: "runtime_item",
        id: "create:fluid_pipe",
        label: "Fluid Pipe",
        namespace: "create",
        source: "runtime-items",
        confidence: 1,
        tags: ["create:pipes"],
        direct_tags: ["create:fluid_pipe"],
        item_refs: ["create:fluid_pipe"],
      },
      {
        kind: "runtime_item",
        id: "create:mechanical_press",
        label: "Mechanical Press",
        namespace: "create",
        source: "runtime-items",
        confidence: 1,
        tags: ["create:wrench_pickup"],
        item_refs: ["create:mechanical_press"],
      },
      {
        kind: "item_tag",
        id: "create:pipes",
        label: "Pipes",
        namespace: "create",
        source: "runtime-summary",
        confidence: 0.75,
        count: 4,
        item_refs: ["create:fluid_pipe"],
      },
      {
        kind: "item_tag",
        id: "c:storage_blocks",
        label: "Storage Blocks",
        namespace: "c",
        source: "runtime-summary",
        confidence: 0.75,
        count: 12,
        item_refs: ["create:andesite_casing"],
      },
    );

    const candidates = extractVocabularyCandidates(evidence, {
      packId: "fixture",
      minEvidence: 2,
      facets: ["mod_subsystem"],
    });
    const ids = candidates.map((candidate) => candidate.id);

    expect(ids).toContain("create:kinetics");
    expect(ids).toContain("create:belt");
    expect(ids).toContain("create:pipe");
    expect(ids).toContain("create:fluid");
    expect(ids).not.toContain("create:pipes");
    expect(ids).not.toContain("create:kinetic");
    expect(ids).not.toContain("create:press");
    expect(ids).not.toContain("create:storage");
    expect(ids).not.toContain("create:using_mechanical_belts");
    expect(ids).not.toContain("pack:fixture/kinetics");
  });

  test("extracts player-facing organization groups from item evidence instead of workflows", () => {
    const evidence = fixtureEvidence();
    evidence.records.push(
      {
        kind: "item_tag",
        id: "c:ores",
        label: "Ores",
        namespace: "c",
        source: "runtime-summary",
        confidence: 0.75,
        count: 20,
        item_refs: ["example:raw_copper"],
      },
      {
        kind: "item_tag",
        id: "tfc:seeds",
        label: "Seeds",
        namespace: "tfc",
        source: "runtime-summary",
        confidence: 0.75,
        count: 12,
        item_refs: ["tfc:seeds/wheat"],
      },
      {
        kind: "mod_metadata",
        id: "create",
        label: "Create",
        namespace: "create",
        source: "mods.toml",
        confidence: 0.75,
        count: 100,
        description: "Aesthetic technology that empowers the player with mechanical automation.",
        semantic_text: [{ source: "mod-description", text: "Aesthetic technology that empowers the player with mechanical automation." }],
      },
      {
        kind: "item_tag",
        id: "tfc:pileable_ingots",
        label: "Pileable Ingots",
        namespace: "tfc",
        source: "runtime-summary",
        confidence: 0.75,
        count: 40,
        item_refs: ["example:copper_ingot"],
      },
      {
        kind: "item_tag",
        id: "tfc:metamorphic_items",
        label: "Metamorphic Items",
        namespace: "tfc",
        source: "runtime-summary",
        confidence: 0.75,
        count: 80,
        item_refs: ["example:gneiss_bricks"],
      },
      {
        kind: "runtime_item",
        id: "create:shaft",
        label: "Shaft",
        namespace: "create",
        source: "runtime-items",
        confidence: 1,
        item_refs: ["create:shaft"],
      },
      {
        kind: "runtime_item",
        id: "gtceu:ulv_electric_motor",
        label: "ULV Electric Motor",
        namespace: "gtceu",
        source: "runtime-items",
        confidence: 1,
        item_refs: ["gtceu:ulv_electric_motor"],
      },
      {
        kind: "block_tag",
        id: "minecraft:mineable/pickaxe",
        label: "Mineable Pickaxe",
        namespace: "minecraft",
        source: "runtime-summary",
        confidence: 0.75,
        count: 30,
        item_refs: ["minecraft:stone"],
      },
      {
        kind: "item_tag",
        id: "example:usable_on_tool_rack",
        label: "Usable On Tool Rack",
        namespace: "example",
        source: "runtime-summary",
        confidence: 0.75,
        count: 8,
        item_refs: ["example:ingot_mold"],
      },
    );

    const candidates = extractVocabularyCandidates(evidence, {
      packId: "fixture",
      minEvidence: 2,
      facets: ["workflow", "organization_group"],
    });
    const ids = candidates.filter((candidate) => candidate.facet === "organization_group").map((candidate) => candidate.id);

    expect(ids).not.toContain("pack:fixture/seeds");
    expect(ids).not.toContain("pack:fixture/crops");
    expect(ids).not.toContain("pack:fixture/inedible_plants");
    expect(ids).not.toContain("example:casting");
    expect(ids).not.toContain("pack:fixture/unprocessed_ores");
    expect(ids).not.toContain("pack:fixture/refined_ores");
    expect(ids).not.toContain("gtceu:ulv_components");
    expect(ids).not.toContain("pack:fixture/create_items");
    expect(ids).not.toContain("pack:fixture/pileable_ingots");
    expect(ids).not.toContain("pack:fixture/metamorphic_items");
    expect(ids).not.toContain("pack:fixture/pickaxe");
    expect(ids).not.toContain("pack:fixture/usable_on_tool_rack");
  });

  test("mod_subsystem curation prompt carries identity constraints", () => {
    const candidate: PackVocabularyCandidate = {
      facet: "mod_subsystem",
      id: "create:kinetics",
      label: "Kinetics",
      origin: "namespace_generated",
      suggested_state: "review",
      confidence: 0.75,
      support: 3,
      evidence: [{ kind: "guide_page", id: "create:ponder/kinetics", confidence: 0.9 }],
      semantic_evidence: [{
        kind: "guide_page",
        id: "create:ponder/kinetics",
        source: "ponder",
        text: "Kinetic blocks move power through shafts and cogs.",
      }],
      seed_items: ["create:shaft"],
      aliases: [],
      reasons: ["guide page may name a mod-owned subsystem"],
    };

    const prompt = buildVocabularyCurationPrompt({
      facet: "mod_subsystem",
      packId: "fixture",
      candidates: [candidate],
      previousAccepted: [],
      minEvidence: 2,
    });

    expect(prompt.system).toContain("namespace-scoped identity system inside a mod");
    expect(prompt.system).toContain("accepting a value does not create a wall home");
    expect(prompt.system).toContain("use workflow/used_at");
    expect(prompt.system).toContain("what mod system is this item itself part of");
    expect(JSON.stringify(JSON.parse(prompt.user))).toContain("create:kinetics");
  });

  test("organization_group curation prompt leads with human storage intent", () => {
    const candidate: PackVocabularyCandidate = {
      facet: "organization_group",
      id: "pack:fixture/casting_molds",
      label: "Casting Molds",
      origin: "pack_generated",
      suggested_state: "review",
      confidence: 0.7,
      support: 4,
      evidence: [{ kind: "guide_page", id: "example:guide/casting/molds", confidence: 0.7 }],
      semantic_evidence: [{
        kind: "guide_page",
        id: "example:guide/casting/molds",
        source: "guide-page",
        text: "Use molds to cast molten metal into ingots.",
      }],
      seed_items: ["example:ingot_mold"],
      aliases: ["molds"],
      reasons: ["guide page label may name a player organization group"],
    };

    const prompt = buildVocabularyCurationPrompt({
      facet: "organization_group",
      packId: "fixture",
      candidates: [candidate],
      previousAccepted: [],
      minEvidence: 2,
    });
    const user = JSON.parse(prompt.user) as { policy?: string };

    expect(prompt.system).toContain(
      "the #1 rule is: would a human player spend one of a small number of main-wall sections on this broad item type, so these items and their obvious siblings stay together?",
    );
    expect(prompt.system).toContain("only about 15-20 human-named organization sections total");
    expect(prompt.system).toContain("group primarily by broad item type/role");
    expect(prompt.system).toContain("the protected built-in wall sections are Food, Tools, Weapons, Armor, Lighting");
    expect(prompt.system).toContain("Raw Materials, Wood, Seeds, Crops, Plants, Clay & Pottery, Mob Drops, Storage");
    expect(prompt.system).toContain("Wood is the built-in home for sticks, logs, planks, boards, lumber");
    expect(prompt.system).toContain("Seeds, Crops, Plants, Clay & Pottery, and Mob Drops are built-in homes");
    expect(prompt.system).toContain("Materials exists as a runtime fallback, but it is intentionally not protected");
    expect(prompt.system).toContain("roughly 3-6 broad, useful storage sections");
    expect(prompt.system).toContain("closely duplicate a protected built-in section");
    expect(prompt.system).toContain("the built-in parent is too overloaded");
    expect(prompt.system).toContain("not a hard required list");
    expect(prompt.system).toContain("one mod's mechanical power line, stackable plates, or anvil smithing");
    expect(prompt.system).toContain("stable main-wall storage section a player would actually maintain");
    expect(user.policy).toContain("scarce, broad sections a human player would maintain primarily by item type/role");
  });

  test("curation prompt keeps semantic prose but omits boilerplate and opaque ref lists", () => {
    const candidates = extractVocabularyCandidates(fixtureEvidence(), {
      packId: "fixture",
      minEvidence: 2,
      facets: ["workflow"],
    });
    const casting = candidates.find((candidate) => candidate.facet === "workflow" && candidate.id === "example:casting");
    expect(casting).toBeDefined();

    const prompt = buildVocabularyCurationPrompt({
      facet: "workflow",
      packId: "fixture",
      candidates: [casting!],
      previousAccepted: [],
      minEvidence: 2,
    });
    expect(prompt.system).toContain("ONLY a player-facing station/process/task");
    expect(prompt.system).toContain("CRITICAL OUTPUT CONTRACT");
    expect(prompt.system).toContain("return exactly one value object for every candidate id");
    expect(prompt.system).toContain("Do not omit rejected candidates");
    expect(prompt.system).toContain("reject implementation/meta recipe mechanics");
    expect(prompt.system).toContain("reject item/product/component families");
    expect(prompt.system).toContain("reject environmental physics/events");
    const user = JSON.parse(prompt.user) as {
      candidates: Array<{
        semantic_evidence?: Array<Record<string, unknown>>;
        sample_item_ids?: string[];
        seed_items?: string[];
        reasons?: string[];
      }>;
      required_output_contract?: {
        required_values_count?: number;
        required_candidate_ids?: string[];
        final_instructions?: string[];
      };
    };
    const candidate = user.candidates[0]!;
    const serialized = JSON.stringify(candidate);

    expect(serialized).toContain("Reusable mold");
    expect(serialized).toContain("Use molds to cast molten metal");
    expect(serialized).not.toContain("trophy wall");
    expect(serialized).not.toContain("⚖ Light");
    expect(serialized).not.toContain("Hold SHIFT");
    expect(serialized).not.toContain("item_refs");
    expect(serialized).not.toContain("recipe_refs");
    expect(serialized).not.toContain("suggested_state");
    expect(candidate.seed_items).toBeUndefined();
    expect(candidate.sample_item_ids).toBeUndefined();
    expect(candidate.reasons).toBeUndefined();
    expect(candidate.semantic_evidence?.some((entry) => entry.item_ref_count === 1)).toBe(true);
    expect(candidate.semantic_evidence?.some((entry) => entry.recipe_ref_count === 1)).toBe(true);
    expect(user.required_output_contract?.required_values_count).toBe(1);
    expect(user.required_output_contract?.required_candidate_ids).toEqual(["example:casting"]);
    expect(user.required_output_contract?.final_instructions?.join(" ")).toContain("values.length");
  });

  test("curation prompt trims per-candidate evidence to stay under the prompt budget", () => {
    const candidates: PackVocabularyCandidate[] = Array.from({ length: 60 }, (_, candidateIndex) => ({
      facet: "workflow",
      id: `example:workflow_${candidateIndex}`,
      label: `Workflow ${candidateIndex}`,
      origin: "namespace_generated",
      suggested_state: "review",
      confidence: 0.7,
      support: 64,
      evidence: [{ kind: "guide_page", id: `example:guide/${candidateIndex}`, confidence: 0.7 }],
      semantic_evidence: Array.from({ length: 64 }, (_, evidenceIndex) => ({
        kind: "guide_page",
        id: `example:guide/${candidateIndex}/${evidenceIndex}`,
        source: `file:/Users/example/PrismLauncher/instances/Fixture/minecraft/kubejs/assets/example/patchouli_books/guide/en_us/entries/${candidateIndex}/${evidenceIndex}.json`,
        key: `guide-page:pages.${evidenceIndex}.text`,
        label: `Workflow ${candidateIndex}`,
        text: `Semantic evidence ${candidateIndex}.${evidenceIndex} ${"rich prose ".repeat(80)}`,
      })),
      seed_items: [],
      aliases: [],
      reasons: [],
    }));

    const prompt = buildVocabularyCurationPrompt({
      facet: "workflow",
      packId: "fixture",
      candidates,
      previousAccepted: [],
      minEvidence: 2,
    });
    const user = JSON.parse(prompt.user) as {
      prompt_budget?: { semantic_evidence_per_candidate?: number };
      candidates: Array<{
        semantic_evidence?: Array<{ source?: string; text?: string }>;
        semantic_evidence_omitted?: number;
      }>;
    };
    const candidate = user.candidates[0]!;

    expect(prompt.system.length + prompt.user.length).toBeLessThanOrEqual(3_200_000);
    expect(user.prompt_budget?.semantic_evidence_per_candidate).toBeLessThan(64);
    expect(candidate.semantic_evidence?.[0]?.text).toContain("Semantic evidence 0.0");
    expect(candidate.semantic_evidence?.[0]?.source).toBe("file:minecraft/kubejs/assets/example/patchouli_books/guide/en_us/entries/0/0.json");
    expect(candidate.semantic_evidence_omitted).toBeGreaterThan(0);
  });

  test("progression candidates reject namespace-only dimension noise", () => {
    const evidence = fixtureEvidence();
    evidence.records.push(
      {
        kind: "guide_page",
        id: "ad_astra:astrodux/en_us/entries/the_moon/moon",
        label: "Moon",
        namespace: "ad_astra",
        source: "jar:ad_astra.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_moon/moon.json",
        confidence: 0.7,
        semantic_text: [{
          source: "guide-page",
          key: "pages.0.text",
          text: "The Moon is the first dimension unlocked by building and launching a tier one rocket.",
        }],
      },
      {
        kind: "advancement",
        id: "beneath:crafting/wood/warped_chest",
        label: "Warped Chest",
        namespace: "beneath",
        source: "jar:beneath.jar!data/beneath/advancements/crafting/wood/warped_chest.json",
        confidence: 0.65,
        semantic_text: [{
          source: "advancement",
          key: "title",
          text: "Warped Chest",
        }],
      },
    );

    const candidates = extractVocabularyCandidates(evidence, {
      packId: "fixture",
      minEvidence: 2,
      facets: ["progression_stage"],
    });

    expect(candidates.find((candidate) => candidate.id === "ad_astra:moon")).toBeDefined();
    expect(candidates.find((candidate) => candidate.id === "beneath:warped/chest")).toBeUndefined();
  });

  test("parses curation responses from raw JSON and wrapped envelopes", () => {
    const raw = JSON.stringify({
      result: JSON.stringify({
        values: [{
          id: "example:casting",
          label: "Casting",
          state: "accepted",
          evidence: [{ kind: "recipe_type", id: "example:casting", confidence: 0.85 }],
        }],
      }),
    });

    const parsed = parseVocabularyCurationResponse(raw);

    expect(parsed).toHaveLength(1);
    expect(parsed[0]?.id).toBe("example:casting");
    expect(parsed[0]?.state).toBe("accepted");
  });

  test("curates accepted values and downgrades unsupported model inventions to review", async () => {
    const evidence = fixtureEvidence();
    const client = new StaticSplitClient({
      values: [
        {
          id: "example:casting",
          label: "Casting",
          state: "accepted",
          description: "Casting and mold-based metalworking.",
          confidence: 0.88,
          evidence: [{ kind: "recipe_type", id: "example:casting", confidence: 0.85 }],
          seed_items: ["example:ingot_mold"],
        },
        {
          id: "example:invented",
          label: "Invented",
          state: "accepted",
          confidence: 0.9,
        },
      ],
    });

    const result = await proposePackFacetVocabulary({
      evidence,
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["workflow"],
      minEvidence: 2,
      client,
      model: "test-model",
    });

    expect(result.vocabulary.facets.workflow?.values["example:casting"]?.state).toBe("accepted");
    expect(result.vocabulary.facets.workflow?.values["example:invented"]).toBeUndefined();
    expect(result.review.decisions.workflow?.find((decision) => decision.id === "example:invented")?.state).toBe("review");
    expect(result.review.diagnostics.find((diagnostic) => diagnostic.id === "example:invented")?.message).toContain("downgraded");
  });

  test("keeps universal defaults accepted even when curation downgrades them", async () => {
    const client = new StaticSplitClient({
      values: [
        {
          id: "slot:fill",
          label: "Fill",
          state: "review",
          description: "Rejected because fill is not a useful use affordance.",
          aliases: ["not useful"],
          confidence: 0.4,
        },
      ],
    });

    const result = await proposePackFacetVocabulary({
      evidence: fixtureEvidence(),
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["use_affordance"],
      minEvidence: 2,
      client,
      model: "test-model",
    });

    expect(result.vocabulary.facets.use_affordance?.values["slot:fill"]?.state).toBe("accepted");
    expect(result.vocabulary.facets.use_affordance?.values["slot:fill"]?.description).toBeUndefined();
    expect(result.vocabulary.facets.use_affordance?.values["slot:fill"]?.aliases).toBeUndefined();
    expect(result.review.decisions.use_affordance?.find((decision) => decision.id === "slot:fill")?.policy_notes).toContain(
      "universal default kept accepted by policy",
    );
  });

  test("rejects workflow roles whose parent workflow is not accepted", async () => {
    const previousVocabulary: PackFacetVocabulary = {
      schema_version: 1,
      kind: "slot-pack-facet-vocabulary",
      pack_id: "fixture",
      facets: {
        workflow_role: {
          values: {
            "example:missing#input": {
              label: "Missing Input",
              origin: "previous",
              state: "accepted",
              confidence: 0.9,
              parent: "example:missing",
            },
          },
        },
      },
    };
    const client = new StaticSplitClient({
      values: [{
        id: "example:missing#input",
        label: "Missing Input",
        state: "accepted",
        confidence: 0.9,
        parent: "example:missing",
      }],
    });

    const result = await proposePackFacetVocabulary({
      evidence: fixtureEvidence(),
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["workflow_role"],
      minEvidence: 2,
      previousVocabulary,
      client,
      model: "test-model",
    });

    const decision = result.review.decisions.workflow_role?.find((value) => value.id === "example:missing#input");
    expect(decision?.state).toBe("rejected");
    expect(decision?.policy_notes).toContain("workflow_role parent is not an accepted workflow value");
    expect(result.vocabulary.facets.workflow_role?.values["example:missing#input"]).toBeUndefined();
  });

  test("validates that accepted workflow_role parents exist in workflow vocabulary", () => {
    const validation = validateVocabularyArtifact({
      schema_version: 1,
      kind: "slot-pack-facet-vocabulary",
      pack_id: "fixture",
      facets: {
        workflow_role: {
          values: {
            "example:missing#input": {
              label: "Missing Input",
              origin: "manual",
              state: "accepted",
              confidence: 0.9,
              parent: "example:missing",
            },
          },
        },
      },
    });

    expect(validation.ok).toBe(false);
    expect(validation.errors.join("\n")).toContain("references missing accepted workflow 'example:missing'");
  });

  test("downgrades guide-title workflow phrases that are too granular for accepted workflow vocabulary", async () => {
    const evidence = fixtureEvidence();
    evidence.records.push({
      kind: "guide_page",
      id: "create:ponder/deployer",
      label: "Using the Deployer",
      namespace: "create",
      source: "ponder",
      confidence: 0.7,
      count: 5,
      semantic_text: [{ source: "ponder", text: "The Deployer can right click blocks and items in front of it." }],
    });
    const client = new StaticSplitClient({
      values: [{
        id: "create:using/the/deployer",
        label: "Using the Deployer",
        state: "accepted",
        confidence: 0.7,
        evidence: [{ kind: "guide_page", id: "create:ponder/deployer", confidence: 0.7 }],
      }],
    });

    const result = await proposePackFacetVocabulary({
      evidence,
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["workflow"],
      minEvidence: 2,
      client,
      model: "test-model",
    });

    const decision = result.review.decisions.workflow?.find((value) => value.id === "create:using/the/deployer");
    expect(decision?.state).toBe("review");
    expect(decision?.policy_notes).toContain("guide/quest/advancement workflow title is too granular; prefer a reusable process/station id");
    expect(result.vocabulary.facets.workflow?.values["create:using/the/deployer"]).toBeUndefined();
  });

  test("downgrades document-only used_at page titles that are too granular", async () => {
    const evidence = fixtureEvidence();
    evidence.records.push({
      kind: "guide_page",
      id: "greate:ponder/millstone",
      label: "Processing Items in the Millstone",
      namespace: "greate",
      source: "ponder",
      confidence: 0.7,
      count: 5,
      semantic_text: [{ source: "ponder", text: "The millstone grinds items into powders and other outputs." }],
    });
    const client = new StaticSplitClient({
      values: [{
        id: "greate:processing/items/in/the/millstone",
        label: "Processing Items in the Millstone",
        state: "accepted",
        confidence: 0.7,
        evidence: [{ kind: "guide_page", id: "greate:ponder/millstone", confidence: 0.7 }],
      }],
    });

    const result = await proposePackFacetVocabulary({
      evidence,
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["used_at"],
      minEvidence: 2,
      client,
      model: "test-model",
    });

    const decision = result.review.decisions.used_at?.find((value) => value.id === "greate:processing/items/in/the/millstone");
    expect(decision?.state).toBe("review");
    expect(decision?.policy_notes).toContain("guide/quest/advancement used_at title is too granular; prefer a reusable station/process id");
    expect(result.vocabulary.facets.used_at?.values["greate:processing/items/in/the/millstone"]).toBeUndefined();
  });

  test("downgrades unsupported model-invented organization groups from material tags", async () => {
    const evidence = fixtureEvidence();
    evidence.records.push({
      kind: "item_tag",
      id: "forge:ingots/copper",
      label: "Copper",
      namespace: "forge",
      source: "runtime-summary",
      confidence: 0.75,
      count: 12,
      item_refs: ["example:copper_ingot"],
    });
    const client = new StaticSplitClient({
      values: [{
        id: "pack:fixture/copper",
        label: "Copper",
        state: "accepted",
        confidence: 0.8,
        evidence: [{ kind: "item_tag", id: "forge:ingots/copper", confidence: 0.75 }],
      }],
    });

    const result = await proposePackFacetVocabulary({
      evidence,
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["organization_group"],
      minEvidence: 2,
      client,
      model: "test-model",
    });

    const decision = result.review.decisions.organization_group?.find((value) => value.id === "pack:fixture/copper");
    expect(decision?.state).toBe("review");
    expect(decision?.policy_notes).toContain(
      "model proposed id without deterministic candidate evidence",
    );
    expect(result.vocabulary.facets.organization_group?.values["pack:fixture/copper"]).toBeUndefined();
  });

  test("downgrades model-invented organization groups from tool and form tags", async () => {
    const evidence = fixtureEvidence();
    evidence.records.push({
      kind: "item_tag",
      id: "minecraft:axes",
      label: "Axes",
      namespace: "minecraft",
      source: "runtime-summary",
      confidence: 0.75,
      count: 8,
      item_refs: ["minecraft:iron_axe"],
    });
    const client = new StaticSplitClient({
      values: [{
        id: "pack:fixture/axes",
        label: "Axes",
        state: "accepted",
        confidence: 0.8,
        evidence: [{ kind: "item_tag", id: "minecraft:axes", confidence: 0.75 }],
      }],
    });

    const result = await proposePackFacetVocabulary({
      evidence,
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["organization_group"],
      minEvidence: 2,
      client,
      model: "test-model",
    });

    const decision = result.review.decisions.organization_group?.find((value) => value.id === "pack:fixture/axes");
    expect(decision?.state).toBe("review");
    expect(result.vocabulary.facets.organization_group?.values["pack:fixture/axes"]).toBeUndefined();
  });

  test("downgrades query-only organization groups that were not deterministic candidates", async () => {
    const evidence = fixtureEvidence();
    evidence.records.push(
      {
        kind: "mod_metadata",
        id: "grapplemod",
        label: "Grappling Hook Mod",
        namespace: "grapplemod",
        source: "mods.toml",
        confidence: 0.6,
        count: 33,
        description: "Grappling hooks and movement tools.",
      },
      {
        kind: "item_tag",
        id: "tfc:pileable_ingots",
        label: "Pileable Ingots",
        namespace: "tfc",
        source: "runtime-summary",
        confidence: 0.75,
        count: 40,
        item_refs: ["example:copper_ingot"],
      },
      {
        kind: "item_tag",
        id: "tfc:metamorphic_items",
        label: "Metamorphic Items",
        namespace: "tfc",
        source: "runtime-summary",
        confidence: 0.75,
        count: 80,
        item_refs: ["example:gneiss_bricks"],
      },
    );
    const client = new StaticSplitClient({
      values: [
        {
          id: "pack:fixture/grapplemod_items",
          label: "Grappling Hook Mod Items",
          state: "accepted",
          confidence: 0.8,
          evidence: [{ kind: "mod_metadata", id: "grapplemod", confidence: 0.6 }],
        },
        {
          id: "pack:fixture/pileable_ingots",
          label: "Pileable Ingots",
          state: "accepted",
          confidence: 0.8,
          evidence: [{ kind: "item_tag", id: "tfc:pileable_ingots", confidence: 0.75 }],
        },
        {
          id: "pack:fixture/metamorphic_items",
          label: "Metamorphic Items",
          state: "accepted",
          confidence: 0.8,
          evidence: [{ kind: "item_tag", id: "tfc:metamorphic_items", confidence: 0.75 }],
        },
      ],
    });

    const result = await proposePackFacetVocabulary({
      evidence,
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["organization_group"],
      minEvidence: 2,
      client,
      model: "test-model",
    });

    for (const id of ["pack:fixture/grapplemod_items", "pack:fixture/pileable_ingots", "pack:fixture/metamorphic_items"]) {
      const decision = result.review.decisions.organization_group?.find((value) => value.id === id);
      expect(decision?.state).toBe("review");
      expect(decision?.policy_notes).toContain(
        "model proposed id without deterministic candidate evidence",
      );
      expect(result.vocabulary.facets.organization_group?.values[id]).toBeUndefined();
    }
  });

  test("does not propose equipment slot tags as organization groups", () => {
    const evidence = fixtureEvidence();
    evidence.records.push({
      kind: "item_tag",
      id: "accessories:back",
      label: "Back",
      namespace: "accessories",
      source: "runtime-summary",
      confidence: 0.75,
      count: 8,
      item_refs: ["example:backpack"],
      semantic_text: [{ source: "runtime-tooltip", text: "Slot: Back" }],
    });

    const candidates = extractVocabularyCandidates(evidence, {
      packId: "fixture",
      minEvidence: 2,
      facets: ["organization_group"],
    });

    expect(candidates.find((candidate) => candidate.facet === "organization_group" && candidate.id === "pack:fixture/back")).toBeUndefined();
  });

  test("downgrades slash-form document aliases when a recipe-backed station id exists", async () => {
    const evidence = fixtureEvidence();
    evidence.records.push(
      {
        kind: "recipe_type",
        id: "example:blast_furnace",
        label: "Blast Furnace",
        namespace: "example",
        source: "runtime-summary",
        confidence: 0.85,
        count: 5,
      },
      {
        kind: "guide_page",
        id: "example:guide/blast_furnace",
        label: "Blast Furnace",
        namespace: "example",
        source: "guide",
        confidence: 0.7,
        count: 5,
        semantic_text: [{ source: "guide-page", text: "The blast furnace processes ore into high-tier metals." }],
      },
    );
    const client = new StaticSplitClient({
      values: [{
        id: "example:blast/furnace",
        label: "Blast Furnace",
        state: "accepted",
        confidence: 0.7,
        evidence: [{ kind: "guide_page", id: "example:guide/blast_furnace", confidence: 0.7 }],
      }],
    });

    const result = await proposePackFacetVocabulary({
      evidence,
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["workflow", "used_at"],
      minEvidence: 2,
      client,
      model: "test-model",
    });

    const workflow = result.review.decisions.workflow?.find((value) => value.id === "example:blast/furnace");
    const usedAt = result.review.decisions.used_at?.find((value) => value.id === "example:blast/furnace");
    expect(workflow?.state).toBe("review");
    expect(usedAt?.state).toBe("review");
    expect(workflow?.policy_notes).toContain("document-title alias duplicates a recipe-backed station/process id");
    expect(usedAt?.policy_notes).toContain("document-title alias duplicates a recipe-backed station/process id");
    expect(result.vocabulary.facets.workflow?.values["example:blast/furnace"]).toBeUndefined();
    expect(result.vocabulary.facets.used_at?.values["example:blast/furnace"]).toBeUndefined();
  });

  test("downgrades advancement-title progression phrases that are not canonical gates", async () => {
    const evidence = fixtureEvidence();
    evidence.records.push({
      kind: "advancement",
      id: "ad_astra:moon",
      label: "One Small Step",
      namespace: "ad_astra",
      source: "jar:ad_astra.jar!data/ad_astra/advancements/moon.json",
      confidence: 0.65,
      semantic_text: [{ source: "advancement", key: "description", text: "Reach the Moon by launching a tier one rocket." }],
    });
    const client = new StaticSplitClient({
      values: [{
        id: "ad_astra:one/small/step",
        label: "One Small Step",
        state: "accepted",
        confidence: 0.65,
        evidence: [{ kind: "advancement", id: "ad_astra:moon", confidence: 0.65 }],
      }],
    });

    const result = await proposePackFacetVocabulary({
      evidence,
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["progression_stage"],
      minEvidence: 2,
      client,
      model: "test-model",
    });

    const decision = result.review.decisions.progression_stage?.find((value) => value.id === "ad_astra:one/small/step");
    expect(decision?.state).toBe("review");
    expect(decision?.policy_notes).toContain("progression value is too phrase-like; prefer a canonical gate/tier/dimension id");
    expect(result.vocabulary.facets.progression_stage?.values["ad_astra:one/small/step"]).toBeUndefined();
  });

  test("downgrades document workflow aliases without process signal", async () => {
    const evidence = fixtureEvidence();
    evidence.records.push({
      kind: "guide_page",
      id: "example:guide/making_steel",
      label: "Making Steel",
      namespace: "example",
      source: "guide",
      confidence: 0.7,
      count: 4,
      semantic_text: [{ source: "guide-page", text: "Steel is a major gate for advanced equipment." }],
    });
    const client = new StaticSplitClient({
      values: [{
        id: "example:making/steel",
        label: "Making Steel",
        state: "accepted",
        confidence: 0.7,
        evidence: [{ kind: "guide_page", id: "example:guide/making_steel", confidence: 0.7 }],
      }],
    });

    const result = await proposePackFacetVocabulary({
      evidence,
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["workflow"],
      minEvidence: 2,
      client,
      model: "test-model",
    });

    const decision = result.review.decisions.workflow?.find((value) => value.id === "example:making/steel");
    expect(decision?.state).toBe("review");
    expect(decision?.policy_notes).toContain(
      "guide/quest/advancement workflow title is too granular; prefer a reusable process/station id",
    );
    expect(result.vocabulary.facets.workflow?.values["example:making/steel"]).toBeUndefined();
  });

  test("downgrades nested document used_at aliases even when they mention a station", async () => {
    const evidence = fixtureEvidence();
    evidence.records.push({
      kind: "guide_page",
      id: "example:guide/blast_furnace_tips",
      label: "Blast Furnace Tips",
      namespace: "example",
      source: "guide",
      confidence: 0.7,
      count: 4,
      semantic_text: [{ source: "guide-page", text: "Tips for operating a blast furnace." }],
    });
    const client = new StaticSplitClient({
      values: [{
        id: "example:blast/furnace/tips",
        label: "Blast Furnace Tips",
        state: "accepted",
        confidence: 0.7,
        evidence: [{ kind: "guide_page", id: "example:guide/blast_furnace_tips", confidence: 0.7 }],
      }],
    });

    const result = await proposePackFacetVocabulary({
      evidence,
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["used_at"],
      minEvidence: 2,
      client,
      model: "test-model",
    });

    const decision = result.review.decisions.used_at?.find((value) => value.id === "example:blast/furnace/tips");
    expect(decision?.state).toBe("review");
    expect(decision?.policy_notes).toContain(
      "guide/quest/advancement used_at title is too granular; prefer a reusable station/process id",
    );
    expect(result.vocabulary.facets.used_at?.values["example:blast/furnace/tips"]).toBeUndefined();
  });

  test("downgrades verbose progression aliases when a canonical gate token exists", async () => {
    const evidence = fixtureEvidence();
    evidence.records.push({
      kind: "quest_node",
      id: "pack:quests/progression/ev",
      label: "ev_extreme_voltage",
      namespace: "ftbquests",
      source: "ftbquests",
      confidence: 0.65,
      count: 5,
      semantic_text: [{ source: "quest", key: "title", text: "EV extreme voltage is a progression tier." }],
    });
    const client = new StaticSplitClient({
      values: [{
        id: "pack:fixture/ev_extreme_voltage",
        label: "EV Extreme Voltage",
        state: "accepted",
        confidence: 0.7,
        evidence: [{ kind: "quest_node", id: "pack:quests/progression/ev", confidence: 0.65 }],
      }],
    });

    const result = await proposePackFacetVocabulary({
      evidence,
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["progression_stage"],
      minEvidence: 2,
      client,
      model: "test-model",
    });

    const decision = result.review.decisions.progression_stage?.find((value) => value.id === "pack:fixture/ev_extreme_voltage");
    expect(decision?.state).toBe("review");
    expect(decision?.policy_notes).toContain("progression value is too phrase-like; prefer a canonical gate/tier/dimension id");
    expect(result.vocabulary.facets.progression_stage?.values["pack:fixture/ev_extreme_voltage"]).toBeUndefined();
  });

  test("rejects legacy pack-scoped mod_subsystem values during vocabulary policy", async () => {
    const previousVocabulary: PackFacetVocabulary = {
      schema_version: 1,
      kind: "slot-pack-facet-vocabulary",
      pack_id: "fixture",
      facets: {
        mod_subsystem: {
          values: {
            "pack:fixture/wall": {
              label: "Wall",
              origin: "previous",
              state: "accepted",
              confidence: 0.9,
            },
          },
        },
      },
    };
    const client = new StaticSplitClient({
      values: [
        {
          id: "pack:fixture/wall",
          label: "Wall",
          state: "accepted",
          confidence: 0.9,
        },
      ],
    });

    const result = await proposePackFacetVocabulary({
      evidence: fixtureEvidence(),
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["mod_subsystem"],
      minEvidence: 2,
      previousVocabulary,
      client,
      model: "test-model",
    });

    const decision = result.review.decisions.mod_subsystem?.find((value) => value.id === "pack:fixture/wall");
    expect(decision?.state).toBe("rejected");
    expect(decision?.policy_notes).toContain(
      "mod_subsystem must be a namespace-scoped identity value, not pack/universal/generic",
    );
    expect(result.vocabulary.facets.mod_subsystem?.values["pack:fixture/wall"]).toBeUndefined();
  });

  test("passes a coverage validator so omitted candidate ids are retried", async () => {
    const client = new CoverageProbeSplitClient();

    await proposePackFacetVocabulary({
      evidence: fixtureEvidence(),
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["workflow"],
      minEvidence: 2,
      client,
      model: "test-model",
    });

    expect(client.rejectedMissingCandidateResponse).toBe(true);
  });

  test("splits large curation prompts without dropping facet candidates", async () => {
    const evidence = fixtureEvidence();
    for (let index = 0; index < 5; index++) {
      evidence.records.push({
        kind: "recipe_type",
        id: `example:process_${index}`,
        label: `Process ${index}`,
        namespace: "example",
        source: "runtime-summary",
        confidence: 0.85,
        count: 4,
      });
    }
    const client = new PromptSizeProbeSplitClient();

    const result = await proposePackFacetVocabulary({
      evidence,
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["workflow"],
      minEvidence: 2,
      client,
      model: "test-model",
      maxCandidatesPerPrompt: 2,
    });

    expect(client.candidateCounts.length).toBeGreaterThan(1);
    expect(client.candidateCounts.every((count) => count <= 2)).toBe(true);
    expect(Object.keys(result.prompts).some((key) => key.startsWith("workflow.part-"))).toBe(true);
    expect(result.review.decisions.workflow?.length).toBe(client.candidateCounts.reduce((sum, count) => sum + count, 0));
  });

  test("prompt selection does not starve strong candidates in crowded buckets", () => {
    const candidates: PackVocabularyCandidate[] = [
      selectionCandidate("example:very_strong_process", { support: 100, semanticCount: 0 }),
      ...Array.from({ length: 10 }, (_, index) =>
        selectionCandidate(`example:related_process_${index}`, { support: 99 - index, semanticCount: 0 })),
      ...Array.from({ length: 50 }, (_, index) =>
        selectionCandidate(`mod_${index}:one_off_process`, { support: 2, semanticCount: 0 })),
    ].sort((a, b) =>
      b.support - a.support ||
      a.id.localeCompare(b.id)
    );

    const selected = selectPromptCandidates(candidates, 12).map((candidate) => candidate.id);

    expect(selected).toContain("example:very_strong_process");
    expect(selected).toContain("example:related_process_0");
  });

  test("prompt selection reserves room for rich semantic candidates", () => {
    const candidates: PackVocabularyCandidate[] = [
      ...Array.from({ length: 40 }, (_, index) =>
        selectionCandidate(`high_support_${index}:process`, { support: 100 - index, semanticCount: 0 })),
      selectionCandidate("example:semantically_named_process", { support: 2, semanticCount: 64 }),
    ].sort((a, b) =>
      b.support - a.support ||
      b.semantic_evidence.length - a.semantic_evidence.length ||
      a.id.localeCompare(b.id)
    );

    const selected = selectPromptCandidates(candidates, 16).map((candidate) => candidate.id);

    expect(selected).toContain("example:semantically_named_process");
  });

  test("allows model-accepted single-source progression gates", async () => {
    const evidence = fixtureEvidence();
    evidence.records.push({
      kind: "guide_page",
      id: "ad_astra:astrodux/en_us/entries/the_moon/moon",
      label: "Moon",
      namespace: "ad_astra",
      source: "jar:ad_astra.jar!assets/ad_astra/patchouli_books/astrodux/en_us/entries/the_moon/moon.json",
      confidence: 0.7,
      semantic_text: [{
        source: "guide-page",
        key: "pages.0.text",
        text: "The Moon is the first dimension unlocked by building and launching a tier one rocket.",
      }],
    });
    const client = new StaticSplitClient({
      values: [{
        id: "ad_astra:moon",
        label: "Moon",
        state: "accepted",
        description: "First space dimension unlocked by tier one rocket.",
        confidence: 0.7,
      }],
    });

    const result = await proposePackFacetVocabulary({
      evidence,
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["progression_stage"],
      minEvidence: 2,
      client,
      model: "test-model",
    });

    expect(result.vocabulary.facets.progression_stage?.values["ad_astra:moon"]).toBeDefined();
    expect(result.review.diagnostics.find((diagnostic) => diagnostic.id === "ad_astra:moon")).toBeUndefined();
  });
});

function fixtureEvidence(): FacetEvidenceArtifact {
  return {
    schema_version: 1,
    kind: "slot-pack-facet-evidence",
    pack_id: "fixture",
    generated_by: "test",
    generated_at: "2026-05-11T00:00:00.000Z",
    source: {
      runtime_items: "/tmp/fixture.runtime-items.ndjson",
      item_count: 2,
    },
    records: [
      {
        kind: "recipe_type",
        id: "example:casting",
        label: "Casting",
        namespace: "example",
        source: "runtime-summary",
        confidence: 0.85,
        count: 4,
        item_refs: ["example:ingot_mold", "patchouli:crafting"],
        recipe_refs: ["example:casting/cast_ingot"],
      },
      {
        kind: "recipe_id_family",
        id: "example:casting/ingot",
        label: "Ingot",
        namespace: "example",
        source: "runtime-recipes",
        confidence: 0.7,
        count: 12,
        recipe_type: "example:casting",
        item_refs: ["example:ingot_mold"],
      },
      {
        kind: "runtime_item",
        id: "example:ingot_mold",
        label: "Ingot Mold",
        namespace: "example",
        source: "runtime-items",
        confidence: 1,
        item_refs: ["example:ingot_mold"],
        semantic_text: [{
          source: "runtime-tooltip",
          text: "Reusable mold for casting ingots and shaping molten metal.",
        }, {
          source: "runtime-tooltip",
          text: "⚖ Light ⇲ Small",
        }, {
          source: "runtime-tooltip",
          text: "Hold SHIFT for more information",
        }],
      },
      {
        kind: "guide_page",
        id: "example:guide/casting/molds",
        label: "Casting Molds",
        namespace: "example",
        source: "jar:example.jar!data/example/patchouli_books/guide/en_us/entries/casting/molds.json",
        confidence: 0.7,
        item_refs: ["example:ingot_mold"],
        recipe_refs: ["example:casting/cast_ingot"],
        semantic_text: [{
          source: "guide-page",
          key: "pages.0.text",
          text: "Use molds to cast molten metal into ingots.",
        }],
      },
      {
        kind: "guide_page",
        id: "example:guide/decorating/trophy_wall",
        label: "Trophy Wall",
        namespace: "example",
        source: "jar:example.jar!data/example/patchouli_books/guide/en_us/entries/decorating/trophy_wall.json",
        confidence: 0.7,
        item_refs: ["example:ingot_mold"],
        semantic_text: [{
          source: "guide-page",
          key: "pages.0.text",
          text: "Put old molds on a trophy wall after retiring them from the forge.",
        }],
      },
      {
        kind: "item_tag",
        id: "c:fruits",
        label: "Fruits",
        namespace: "c",
        source: "runtime-summary",
        confidence: 0.75,
        count: 3,
        item_refs: ["example:apple"],
      },
    ],
    diagnostics: [],
  };
}

class StaticSplitClient implements LlmClient {
  constructor(private readonly response: unknown) {}

  async query(_prompt: string, _options: QueryOptions): Promise<string> {
    return JSON.stringify(this.response);
  }

  async querySplit(_system: string, user: string, _options: QueryOptions): Promise<string> {
    return JSON.stringify(materializeCandidateCoverageResponse(this.response, user));
  }
}

class CoverageProbeSplitClient implements LlmClient {
  rejectedMissingCandidateResponse = false;

  async query(_prompt: string, _options: QueryOptions): Promise<string> {
    return JSON.stringify({ values: [] });
  }

  async querySplit(_system: string, user: string, options: QueryOptions): Promise<string> {
    const ids = promptCandidateIds(user);
    const missingOne = JSON.stringify({
      values: ids.slice(0, -1).map((id) => ({ id, state: "review" })),
    });
    const verdict = options.responseValidator?.(missingOne);
    this.rejectedMissingCandidateResponse = verdict?.ok === false;
    return JSON.stringify({
      values: ids.map((id) => ({ id, state: "review" })),
    });
  }
}

class PromptSizeProbeSplitClient implements LlmClient {
  candidateCounts: number[] = [];

  async query(_prompt: string, _options: QueryOptions): Promise<string> {
    return JSON.stringify({ values: [] });
  }

  async querySplit(_system: string, user: string, _options: QueryOptions): Promise<string> {
    const ids = promptCandidateIds(user);
    this.candidateCounts.push(ids.length);
    return JSON.stringify({
      values: ids.map((id) => ({ id, state: "review" })),
    });
  }
}

function materializeCandidateCoverageResponse(response: unknown, user: string): unknown {
  if (!isRecord(response) || !Array.isArray(response.values)) return response;
  const ids = promptCandidateIds(user);
  const supplied = response.values.filter(isRecord);
  const byId = new Map(supplied
    .filter((value): value is Record<string, unknown> & { id: string } => typeof value.id === "string")
    .map((value) => [value.id, value]));
  const values: Record<string, unknown>[] = ids.map((id) => byId.get(id) ?? { id, state: "review" });
  for (const value of supplied) {
    if (typeof value.id === "string" && !ids.includes(value.id)) values.push(value);
  }
  return { ...response, values };
}

function promptCandidateIds(user: string): string[] {
  const parsed = JSON.parse(user) as { candidates?: Array<{ id?: unknown }> };
  return (parsed.candidates ?? [])
    .map((candidate) => candidate.id)
    .filter((id): id is string => typeof id === "string");
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return !!value && typeof value === "object" && !Array.isArray(value);
}

export function splitFixtureHash(system: string, user: string): string {
  return createHash("sha256").update(`${system}\n\n---\n\n${user}`).digest("hex").slice(0, 16);
}

function selectionCandidate(
  id: string,
  options: { support: number; semanticCount: number },
): PackVocabularyCandidate {
  return {
    facet: "workflow",
    id,
    label: id,
    origin: "namespace_generated",
    suggested_state: "review",
    confidence: 0.7,
    support: options.support,
    evidence: [{ kind: "recipe_type", id, confidence: 0.7 }],
    semantic_evidence: Array.from({ length: options.semanticCount }, (_, index) => ({
      kind: "guide_page",
      id: `${id}/semantic/${index}`,
      source: "guide",
      text: `Semantic evidence ${index} for ${id}.`,
    })),
    seed_items: [],
    aliases: [],
    reasons: ["recipe type names a repeated process"],
  };
}
