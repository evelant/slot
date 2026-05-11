import { createHash } from "node:crypto";
import { describe, expect, test } from "bun:test";
import type { LlmClient, QueryOptions } from "../src/llm/client.ts";
import type { FacetEvidenceArtifact } from "../src/evidence/facet_evidence.ts";
import {
  buildVocabularyCurationPrompt,
  extractVocabularyCandidates,
  parseVocabularyCurationResponse,
  proposePackFacetVocabulary,
  type PackVocabularyCandidate,
} from "../src/vocabulary/pack_vocabulary.ts";

describe("pack facet vocabulary generation", () => {
  test("extracts universal defaults and evidence-backed workflow candidates", () => {
    const evidence = fixtureEvidence();
    const candidates = extractVocabularyCandidates(evidence, {
      packId: "fixture",
      minEvidence: 2,
      facets: ["activity", "workflow", "workflow_role", "organization_group", "food_category"],
    });

    expect(candidates.find((candidate) => candidate.facet === "activity" && candidate.id === "slot:cooking")?.suggested_state).toBe("accepted");
    expect(candidates.find((candidate) => candidate.facet === "workflow" && candidate.id === "example:casting")?.suggested_state).toBe("accepted");
    expect(candidates.find((candidate) => candidate.facet === "workflow" && candidate.id === "example:casting")?.semantic_evidence.some((entry) =>
      entry.text?.includes("Reusable mold")
    )).toBe(true);
    expect(candidates.find((candidate) => candidate.facet === "workflow" && candidate.id === "example:casting/ingot")).toBeUndefined();
    expect(candidates.find((candidate) => candidate.facet === "workflow_role" && candidate.id === "example:casting#input")?.parent).toBe("example:casting");
    expect(candidates.find((candidate) => candidate.facet === "organization_group" && candidate.id === "example:casting")?.suggested_state).toBe("review");
    expect(candidates.find((candidate) => candidate.facet === "organization_group" && candidate.id === "example:casting")?.description).toBeUndefined();
    expect(candidates.find((candidate) => candidate.facet === "food_category" && candidate.id === "slot:fruit")?.evidence[0]?.kind).toBe("item_tag");
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
