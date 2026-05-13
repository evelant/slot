import { createHash } from "node:crypto";
import { describe, expect, test } from "bun:test";
import type { LlmClient, QueryOptions } from "../src/llm/client.ts";
import type { FacetEvidenceArtifact } from "../src/evidence/facet_evidence.ts";
import { validateVocabularyArtifact, type PackFacetVocabulary } from "../src/schema/vocabulary.ts";
import {
	  applyVocabularyReviewDecisions,
	  buildVocabularyCurationPrompt,
	  buildVocabularyPromptOverview,
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
      {
        kind: "mod_metadata",
        id: "railways",
        label: "Steam 'n' Rails",
        namespace: "railways",
        source: "mods-folder-scan",
        confidence: 0.6,
        count: 20,
        description: "Adds railway tracks, conductors, and train logistics.",
        examples: ["minecraft", "forge", "railways"],
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
    expect(ids).toContain("railways:rail");
    expect(ids).not.toContain("create:pipes");
    expect(ids).not.toContain("create:kinetic");
    expect(ids).not.toContain("create:press");
    expect(ids).not.toContain("create:storage");
    expect(ids).not.toContain("create:using_mechanical_belts");
    expect(ids).not.toContain("pack:fixture/kinetics");
    expect(candidates.find((candidate) => candidate.id === "railways:rail")?.aliases).toEqual([]);
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
      {
        kind: "guide_page",
        id: "firmalife:guide/beekeeping",
        label: "Beekeeping",
        title: "Beekeeping",
        namespace: "firmalife",
        source: "guide",
        confidence: 0.82,
        count: 10,
        item_refs: ["firmalife:beehive", "firmalife:honey_jar"],
        semantic_text: [{ source: "guide", text: "Keep bees in hives and collect honey, wax, and honeycomb." }],
      },
      {
        kind: "guide_page",
        id: "tfc:guide/glass_products",
        label: "Glass Products",
        title: "Glass Products",
        namespace: "tfc",
        source: "guide",
        confidence: 0.82,
        count: 12,
        item_refs: ["tfc:glass_batch", "tfc:glass_bottle"],
        semantic_text: [{ source: "guide", text: "Shape glass into panes, bottles, vials, and other glassware." }],
      },
      {
        kind: "guide_page",
        id: "minecraft:guide/item_containers",
        label: "Item Containers",
        title: "Item Containers",
        namespace: "minecraft",
        source: "guide",
        confidence: 0.82,
        count: 8,
        item_refs: ["minecraft:chest", "minecraft:barrel"],
        semantic_text: [{ source: "guide", text: "Containers store items." }],
      },
      {
        kind: "guide_page",
        id: "minecraft:guide/lamps",
        label: "Lamps",
        title: "Lamps",
        namespace: "minecraft",
        source: "guide",
        confidence: 0.82,
        count: 8,
        item_refs: ["minecraft:lantern", "minecraft:redstone_lamp"],
        semantic_text: [{ source: "guide", text: "Lamps and lanterns light an area." }],
      },
      {
        kind: "guide_page",
        id: "minecraft:guide/pottery",
        label: "Pottery",
        title: "Pottery",
        namespace: "minecraft",
        source: "guide",
        confidence: 0.82,
        count: 8,
        item_refs: ["minecraft:brick", "minecraft:flower_pot"],
        semantic_text: [{ source: "guide", text: "Clay, bricks, and pots." }],
      },
      {
        kind: "guide_page",
        id: "minecraft:guide/redstone",
        label: "Redstone",
        title: "Redstone",
        namespace: "minecraft",
        source: "guide",
        confidence: 0.82,
        count: 8,
        item_refs: ["minecraft:redstone", "minecraft:repeater"],
        semantic_text: [{ source: "guide", text: "Redstone dust and circuit components." }],
      },
    );

    const candidates = extractVocabularyCandidates(evidence, {
      packId: "fixture",
      minEvidence: 2,
      facets: ["workflow", "organization_group"],
    });
    const ids = candidates.filter((candidate) => candidate.facet === "organization_group").map((candidate) => candidate.id);

    expect(ids).toContain("pack:fixture/beekeeping");
    expect(ids).toContain("pack:fixture/glass_products");
    expect(ids).not.toContain("pack:fixture/seeds");
    expect(ids).not.toContain("pack:fixture/crops");
    expect(ids).not.toContain("pack:fixture/inedible_plants");
    expect(ids).not.toContain("pack:fixture/item_containers");
    expect(ids).not.toContain("pack:fixture/lamps");
    expect(ids).not.toContain("pack:fixture/pottery");
    expect(ids).not.toContain("pack:fixture/redstone");
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

    expect(prompt.system).toContain("namespace-scoped identity systems inside a mod");
    expect(prompt.system).toContain("not a wall-home source");
    expect(prompt.system).toContain("The item itself must belong to the subsystem");
    expect(prompt.system).toContain("Do not assign a subsystem merely because the item is consumed by a subsystem recipe");
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
    const user = JSON.parse(prompt.user) as {
      policy?: string;
      context_records?: Array<{ context_id?: string }>;
      pack_item_overview?: unknown;
      synthesis_contract?: { final_instructions?: string[] };
      candidates?: unknown;
    };

    expect(prompt.system).toContain("context_records and may contain pack_item_overview");
    expect(prompt.system).toContain("Do not output one value per context record");
    expect(prompt.system).toContain("Synthesize the vocabulary values the pack actually needs");
    expect(prompt.system).not.toContain("Curate from the candidate ids");
    expect(prompt.system).toContain(
      "The #1 rule is: would a human player spend one of a small number of main inventory sections on this broad item type",
    );
    expect(prompt.system).toContain("accepted + review custom groups should be human-sized");
    expect(prompt.system).toContain("Do not be so conservative that you output nothing");
    expect(prompt.system).toContain("usually 3-10 values");
    expect(prompt.system).toContain("Pack-specific storage families should use pack:fixture/");
    expect(prompt.system).toContain("Group primarily by broad item type or role");
    expect(prompt.system).toContain("Protected built-in wall sections are good homes");
    expect(prompt.system).toContain("Ores & Raw Stock, Metal Stock, Gems & Crystals, Dusts & Powders, Wood, Seeds, Crops, Plants, Ceramics & Molds, Organic Materials, Storage");
    expect(prompt.system).toContain("Item containers belong to Storage");
    expect(prompt.system).toContain("lamps/light sources to Lighting");
    expect(prompt.system).toContain("beekeeping, glass products, cooking supplies");
    expect(prompt.system).toContain("not a hard list");
    expect(prompt.system).toContain("material form/state such as stackable or pileable");
    expect(prompt.system).toContain("rock/geology taxonomy");
    expect(prompt.system).toContain("broad storage family a player would actually maintain");
    expect(user.policy).toContain("scarce, broad sections a human player would maintain primarily by item type/role");
    expect(user.context_records?.[0]?.context_id).toBe("pack:fixture/casting_molds");
    expect(user.pack_item_overview).toBeUndefined();
    expect("candidates" in user).toBe(false);
    expect(user.synthesis_contract?.final_instructions?.join(" ")).toContain("not proposed values");
  });

  test("organization_group prompt can include pack-wide item overview without candidate ids", () => {
    const evidence = fixtureEvidence();
    evidence.records.push(
      runtimeItemRecord("example:glass_bottle", "Glass Bottle", {
        tags: ["c:glass", "c:bottles"],
        ingredientTypes: { "example:glassworking": 3 },
        semantic: "A reusable bottle made from worked glass.",
      }),
      runtimeItemRecord("example:glass_pane", "Glass Pane", {
        tags: ["c:glass", "minecraft:impermeable"],
        ingredientTypes: { "example:glassworking": 2 },
        semantic: "Thin glass sheet used for windows.",
      }),
      runtimeItemRecord("example:clear_glass", "Clear Glass", {
        tags: ["c:glass"],
        outputTypes: { "example:glassworking": 4 },
        semantic: "Transparent glass block.",
      }),
      runtimeItemRecord("example:glass_vial", "Glass Vial", {
        tags: ["c:glass", "c:vials"],
        ingredientTypes: { "example:glassworking": 5 },
        semantic: "Small glass vessel for liquids.",
      }),
      runtimeItemRecord("example:oak_log", "Oak Log", {
        tags: ["minecraft:logs", "minecraft:wooden"],
        semantic: "Stock wood used for planks and boards.",
      }),
      {
        kind: "item_tag",
        id: "c:glass",
        label: "Glass",
        namespace: "c",
        source: "runtime-summary",
        confidence: 0.75,
        count: 4,
        item_refs: ["example:glass_bottle", "example:glass_pane", "example:clear_glass", "example:glass_vial"],
      },
      {
        kind: "recipe_type",
        id: "example:glassworking",
        label: "Glassworking",
        namespace: "example",
        source: "runtime-summary",
        confidence: 0.85,
        count: 8,
        item_refs: ["example:glass_bottle", "example:glass_pane", "example:clear_glass", "example:glass_vial"],
        semantic_text: [{ source: "recipe-category-lang", text: "Glassworking shapes glass into bottles, panes, and vials." }],
      },
      {
        kind: "kubejs_tooltip",
        id: "kubejs:glass_tips/0",
        label: "Glassware",
        namespace: "kubejs",
        source: "file:/tmp/glass_tips.js",
        confidence: 0.8,
        item_refs: ["example:glass_bottle", "example:glass_vial"],
        semantic_text: [{ source: "kubejs-tooltip", text: "Glassware is fragile but reusable in fluid recipes." }],
      },
    );

    const overview = buildVocabularyPromptOverview({
      facet: "organization_group",
      records: evidence.records,
    });
    expect(overview?.default_section_pressure?.some((entry) => entry.section === "Wood")).toBe(true);
    expect(overview?.runtime_item_family_clusters?.some((entry) => entry.term === "glass")).toBe(true);
    expect(overview?.tag_membership_summaries?.some((entry) => entry.tag_id === "c:glass")).toBe(true);
    expect(overview?.recipe_use_neighborhoods?.some((entry) => entry.recipe_type === "example:glassworking")).toBe(true);
    expect(overview?.human_visible_text_pools?.some((entry) => entry.topic === "kubejs tooltip text")).toBe(true);

    const prompt = buildVocabularyCurationPrompt({
      facet: "organization_group",
      packId: "fixture",
      candidates: [],
      previousAccepted: [],
      minEvidence: 2,
      packOverview: overview,
    });
    const user = JSON.parse(prompt.user) as {
      pack_item_overview?: {
        runtime_item_family_clusters?: Array<{ term?: string }>;
      };
      synthesis_contract?: { final_instructions?: string[] };
    };

    expect(user.pack_item_overview?.runtime_item_family_clusters?.some((entry) => entry.term === "glass")).toBe(true);
    expect(prompt.system).toContain("Context ids are source handles, not candidate ids");
    expect(user.synthesis_contract?.final_instructions?.join(" ")).toContain("context_id values are source handles");
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
    expect(prompt.system).toContain("Synthesize reusable player-facing tasks, processes, or station workflows");
    expect(prompt.system).toContain("Prefer canonical process/station names over tutorial titles");
    expect(prompt.system).not.toContain("CRITICAL OUTPUT CONTRACT");
    expect(prompt.system).not.toContain("return exactly one value object for every candidate id");
    expect(prompt.system).toContain("Reject implementation recipe mechanics");
    expect(prompt.system).toContain("item families");
    expect(prompt.system).toContain("environmental events");
    const user = JSON.parse(prompt.user) as {
      context_records: Array<{
        context_id?: string;
        semantic_context?: string[];
        sample_item_ids?: string[];
        seed_items?: string[];
        reasons?: string[];
      }>;
      synthesis_contract?: {
        context_record_count?: number;
        final_instructions?: string[];
      };
      candidates?: unknown;
      required_output_contract?: unknown;
    };
    const contextRecord = user.context_records[0]!;
    const serialized = JSON.stringify(contextRecord);

    expect(serialized).toContain("Reusable mold");
    expect(serialized).toContain("Use molds to cast molten metal");
    expect(serialized).not.toContain("trophy wall");
    expect(serialized).not.toContain("⚖ Light");
    expect(serialized).not.toContain("Hold SHIFT");
    expect(serialized).not.toContain("item_refs");
    expect(serialized).not.toContain("recipe_refs");
    expect(serialized).not.toContain("suggested_state");
    expect(contextRecord.context_id).toBe("example:casting");
    expect(contextRecord.seed_items).toBeUndefined();
    expect(contextRecord.sample_item_ids).toContain("example:ingot_mold");
    expect(contextRecord.reasons).toBeUndefined();
    expect(contextRecord.semantic_context?.some((entry) => entry.includes("Item: Ingot Mold"))).toBe(true);
    expect(serialized).not.toContain("item_ref_count");
    expect(serialized).not.toContain("recipe_ref_count");
    expect(user.synthesis_contract?.context_record_count).toBe(1);
    expect(user.synthesis_contract?.final_instructions?.join(" ")).toContain("Output only synthesized vocabulary values");
    expect(user.synthesis_contract?.final_instructions?.join(" ")).toContain("Do not output provenance metadata");
    expect("candidates" in user).toBe(false);
    expect(user.required_output_contract).toBeUndefined();
  });

  test("curation prompt trims per-context-record evidence to stay under the prompt budget", () => {
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
      context_records: Array<{
        semantic_context?: string[];
        semantic_context_omitted?: number;
      }>;
    };
    const contextRecord = user.context_records[0]!;

    expect(prompt.system.length + prompt.user.length).toBeLessThanOrEqual(3_200_000);
    expect(contextRecord.semantic_context?.length).toBeLessThan(64);
    expect(contextRecord.semantic_context?.[0]).toContain("Semantic evidence 0.0");
    expect(JSON.stringify(contextRecord)).not.toContain("file:");
    expect(JSON.stringify(contextRecord)).not.toContain("source");
    expect(contextRecord.semantic_context_omitted).toBeGreaterThan(0);
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
      {
        kind: "advancement",
        id: "tfc:metal/horse_armor/black_steel",
        label: "Black Steel Horse Armor",
        namespace: "tfc",
        source: "jar:tfc.jar!data/tfc/advancements/metal/horse_armor/black_steel.json",
        confidence: 0.65,
        semantic_text: [{
          source: "advancement",
          key: "title",
          text: "Black Steel Horse Armor",
        }],
      },
      {
        kind: "guide_page",
        id: "tfc:field_guide/en_us/entries/firmaciv/canoe",
        label: "Dugout Canoes",
        namespace: "tfc",
        source: "jar:firmaciv.jar!assets/tfc/patchouli_books/field_guide/en_us/entries/firmaciv/canoe.json",
        confidence: 0.7,
        semantic_text: [{
          source: "guide-page",
          key: "pages.0.text",
          text: "The Dugout Canoe will likely be the first step on your aquatic journey. If you're in the copper age, you can probably build a canoe.",
        }],
      },
      {
        kind: "quest_node",
        id: "pack:ftbquests/chapters/applied_energistics_2",
        label: "applied_energistics_2",
        namespace: "fixture",
        source: "config/ftbquests/quests/chapters/applied_energistics_2.snbt",
        confidence: 0.7,
        semantic_text: [{
          source: "quest-snbt",
          text: "Gather Certus Quartz from the Moon so you can progress through AE2.",
        }],
      },
      {
        kind: "quest_node",
        id: "pack:ftbquests/chapters/space",
        label: "Eager to launch into the final frontier? Preparation is key to survival or you will end up back in the stone age.",
        namespace: "fixture",
        source: "config/ftbquests/quests/chapters/space.snbt",
        confidence: 0.7,
        semantic_text: [{
          source: "quest-snbt",
          text: "Gather oxygen and fuel before launching a rocket.",
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
    expect(candidates.find((candidate) => candidate.id === "tfc:black_steel")).toBeUndefined();
    expect(candidates.find((candidate) => candidate.id === "tfc:steel")).toBeUndefined();
    expect(candidates.find((candidate) => candidate.id === "tfc:dugout/canoes")).toBeUndefined();
    expect(candidates.find((candidate) => candidate.id === "pack:fixture/applied_energistics_2")).toBeUndefined();
    expect(candidates.find((candidate) => candidate.id?.startsWith("pack:fixture/eager/to/launch"))).toBeUndefined();
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

  test("curates accepted values and allows synthesized vocabulary without candidate coverage", async () => {
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
          rationale: "The model synthesized this as a compact workflow value from context.",
          examples: ["example:item"],
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
    expect(result.vocabulary.facets.workflow?.values["example:invented"]?.state).toBe("accepted");
    const invented = result.review.decisions.workflow?.find((decision) => decision.id === "example:invented");
    expect(invented?.state).toBe("accepted");
    expect(invented?.rationale).toContain("compact workflow");
    expect(invented?.examples).toContain("example:item");
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

  test("downgrades unsupported model-synthesized organization groups from material tags", async () => {
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
      "custom organization_group requires human review before auto-home",
    );
    expect(result.vocabulary.facets.organization_group?.values["pack:fixture/copper"]).toBeUndefined();
  });

  test("downgrades model-synthesized organization groups without enough org evidence", async () => {
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
    expect(decision?.policy_notes).toContain(
      "custom organization_group requires human review before auto-home",
    );
    expect(result.vocabulary.facets.organization_group?.values["pack:fixture/axes"]).toBeUndefined();
  });

  test("records synthesized value rationale and examples without requiring evidence refs", async () => {
    const client = new StaticSplitClient({
      values: [{
        id: "example:casting_supplies",
        label: "Casting Supplies",
        state: "accepted",
        confidence: 0.82,
        description: "Reusable supplies for casting molten materials into item forms.",
        rationale: "Players commonly gather molds and molten-metal tools together before casting.",
        examples: ["Ingot Mold", "example:ingot_mold"],
      }],
    });

    const result = await proposePackFacetVocabulary({
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

    const decision = result.review.decisions.workflow?.find((value) => value.id === "example:casting_supplies");
    expect(decision?.state).toBe("accepted");
    expect(decision?.policy_notes).toContain("model-synthesized value accepted from context");
    expect(decision?.rationale).toContain("molds");
    expect(decision?.examples).toContain("Ingot Mold");
    expect(result.vocabulary.facets.workflow?.values["example:casting_supplies"]).toBeDefined();
  });

  test("applies manual approve and rename decisions from the concise review artifact", async () => {
    const client = new StaticSplitClient({
      values: [{
        id: "pack:fixture/casting_molds",
        label: "Casting Molds",
        state: "accepted",
        description: "Reusable molds and casting supplies.",
        rationale: "Players gather molds together when preparing casting work.",
        examples: ["Ingot Mold"],
      }],
    });

    const result = await proposePackFacetVocabulary({
      evidence: fixtureEvidence(),
      evidencePath: "/tmp/fixture.facet-evidence.json",
      packId: "fixture",
      generatedBy: "test",
      generatedAt: "2026-05-11T00:00:00.000Z",
      facets: ["organization_group"],
      minEvidence: 2,
      client,
      model: "test-model",
    });
    const decision = result.review.decisions.organization_group?.find((value) => value.id === "pack:fixture/casting_molds");
    expect(decision?.state).toBe("review");
    expect(decision?.human_review?.decision).toBe("pending");
    expect(result.vocabulary.facets.organization_group?.values["pack:fixture/casting_molds"]).toBeUndefined();

    decision!.human_review = {
      decision: "rename",
      approved_id: "pack:fixture/casting_supplies",
      approved_label: "Casting Supplies",
      notes: "Molds alone is a bit too narrow.",
    };

    const applied = applyVocabularyReviewDecisions({
      vocabulary: result.vocabulary,
      review: result.review,
      generatedBy: "test",
      generatedAt: "2026-05-11T01:00:00.000Z",
      reviewPath: "/tmp/fixture.facet-vocabulary.review.json",
    });

    expect(applied.errors).toEqual([]);
    expect(applied.changes).toContainEqual({
      facet: "organization_group",
      id: "pack:fixture/casting_molds",
      action: "rename",
      approved_id: "pack:fixture/casting_supplies",
    });
    expect(applied.vocabulary.facets.organization_group?.values["pack:fixture/casting_supplies"]).toMatchObject({
      label: "Casting Supplies",
      origin: "manual",
      state: "accepted",
      description: "Reusable molds and casting supplies.",
    });
    expect(applied.vocabulary.facets.organization_group?.values["pack:fixture/casting_molds"]).toBeUndefined();
  });

  test("downgrades query-only organization groups without enough org evidence", async () => {
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
        "custom organization_group requires human review before auto-home",
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

  test("downgrades advancement-title progression phrases that are not concise gates", async () => {
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
    expect(decision?.policy_notes).toContain("progression value is too phrase-like; prefer a concise gate/tier/dimension id");
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

  test("allows model judgment for non-phrase progression labels without hardcoded canonical aliases", async () => {
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
    expect(decision?.state).toBe("accepted");
    expect(decision?.policy_notes ?? []).not.toContain("progression value is too phrase-like; prefer a concise gate/tier/dimension id");
    expect(result.vocabulary.facets.progression_stage?.values["pack:fixture/ev_extreme_voltage"]).toBeDefined();
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

  test("accepts compact synthesized curation responses without context-record coverage", async () => {
    const client = new CompactResponseProbeSplitClient();

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

    expect(client.acceptedCompactResponse).toBe(true);
  });

  test("splits large curation prompts without dropping context records", async () => {
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

    expect(client.contextRecordCounts.length).toBeGreaterThan(1);
    expect(client.contextRecordCounts.every((count) => count <= 2)).toBe(true);
    expect(Object.keys(result.prompts).some((key) => key.startsWith("workflow.part-"))).toBe(true);
    expect(result.review.decisions.workflow?.length).toBe(client.contextRecordCounts.reduce((sum, count) => sum + count, 0));
  });

  test("keeps vocabulary facets in one prompt by default when they fit the prompt budget", async () => {
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
    });

    expect(client.contextRecordCounts).toHaveLength(1);
    expect(Object.keys(result.prompts)).toEqual(["workflow"]);
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

function runtimeItemRecord(
  id: string,
  label: string,
  options: {
    tags?: string[];
    ingredientTypes?: Record<string, number>;
    outputTypes?: Record<string, number>;
    semantic?: string;
  } = {},
): FacetEvidenceArtifact["records"][number] {
  const ingredientTypes = options.ingredientTypes ?? {};
  const outputTypes = options.outputTypes ?? {};
  return {
    kind: "runtime_item",
    id,
    label,
    namespace: id.split(":")[0] ?? "example",
    source: "runtime-items",
    confidence: 1,
    item_refs: [id],
    tags: options.tags ?? [],
    direct_tags: options.tags ?? [],
    recipe_roles: {
      in_degree: Object.values(ingredientTypes).reduce((sum, value) => sum + value, 0),
      out_degree: Object.values(outputTypes).reduce((sum, value) => sum + value, 0),
      ingredient_types: ingredientTypes,
      output_types: outputTypes,
      ingredient_examples: [],
      output_examples: [],
    },
    ...(options.semantic ? { semantic_text: [{ source: "runtime-tooltip", text: options.semantic }] } : {}),
  };
}

class StaticSplitClient implements LlmClient {
  constructor(private readonly response: unknown) {}

  async query(_prompt: string, _options: QueryOptions): Promise<string> {
    return JSON.stringify(this.response);
  }

  async querySplit(_system: string, _user: string, _options: QueryOptions): Promise<string> {
    return JSON.stringify(this.response);
  }
}

class CompactResponseProbeSplitClient implements LlmClient {
  acceptedCompactResponse = false;

  async query(_prompt: string, _options: QueryOptions): Promise<string> {
    return JSON.stringify({ values: [] });
  }

  async querySplit(_system: string, _user: string, options: QueryOptions): Promise<string> {
    const compact = JSON.stringify({
      values: [{ id: "example:synthesized_process", label: "Synthesized Process", state: "review" }],
    });
    const verdict = options.responseValidator?.(compact);
    this.acceptedCompactResponse = verdict?.ok === true;
    return compact;
  }
}

class PromptSizeProbeSplitClient implements LlmClient {
  contextRecordCounts: number[] = [];

  async query(_prompt: string, _options: QueryOptions): Promise<string> {
    return JSON.stringify({ values: [] });
  }

  async querySplit(_system: string, user: string, _options: QueryOptions): Promise<string> {
    const ids = promptContextRecordIds(user);
    this.contextRecordCounts.push(ids.length);
    return JSON.stringify({
      values: ids.map((id) => ({ id, state: "review" })),
    });
  }
}

function promptContextRecordIds(user: string): string[] {
  const parsed = JSON.parse(user) as {
    context_records?: Array<{ context_id?: unknown }>;
    candidates?: Array<{ id?: unknown }>;
  };
  const contextRecords = Array.isArray(parsed.context_records) ? parsed.context_records : undefined;
  if (contextRecords) {
    return contextRecords
      .map((record) => record.context_id)
      .filter((id): id is string => typeof id === "string");
  }
  return (parsed.candidates ?? [])
    .map((record) => record.id)
    .filter((id): id is string => typeof id === "string");
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
