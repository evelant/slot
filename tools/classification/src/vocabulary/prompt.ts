import { FACETS } from "../schema/facets.ts";
import type { PackVocabularyCandidate, VocabularyFacetId } from "./types.ts";
import { CANDIDATE_EXAMPLE_LIMIT, FACET_POLICIES, MOD_SUBSYSTEM_PROMPT_EVIDENCE_LIMIT, MOD_SUBSYSTEM_PROMPT_SEMANTIC_EVIDENCE_LIMITS, PROMPT_SEMANTIC_EVIDENCE_LIMITS, VOCABULARY_PROMPT_CHAR_BUDGET } from "./constants.ts";
import { round } from "./helpers.ts";
import { promptSemanticEvidence } from "./semantic_index.ts";

export function buildVocabularyCurationPrompt(args: {
  facet: VocabularyFacetId;
  packId: string;
  candidates: readonly PackVocabularyCandidate[];
  previousAccepted: readonly string[];
  minEvidence: number;
}): { system: string; user: string } {
  const system = `You curate one SLOT pack facet vocabulary.

Output strict JSON only:
{
  "values": [
    {
      "id": "stable value id",
      "label": "Display label",
      "state": "accepted|review|rejected",
      "description": "short usage guidance",
      "aliases": ["optional alias"],
      "confidence": 0.0,
      "evidence": [{"kind": "recipe_type", "id": "example:casting", "confidence": 0.8}],
      "parent": "workflow id, only for workflow_role",
      "related_activity": ["slot:automation"],
      "default_organization_group": "value id, only when explicitly justified"
    }
  ]
}

Rules:
- Curate from the candidate ids. Do not freely invent accepted ids.
- New ids without candidate evidence must be state "review" at most.
- Treat semantic_evidence as the primary signal. It preserves tooltip, guide, quest, advancement, mod-description, and lang-resolved prose.
- CRITICAL OUTPUT CONTRACT: evaluate the full candidate list and return exactly one value object for every candidate id in this prompt.
- Do not omit rejected candidates.
- Do not summarize, group, abbreviate, use ellipses, or omit rejected candidates. Omission of even one candidate id means the response is invalid and will be retried.
- Before finalizing, compare your output ids against required_output_contract.required_candidate_ids and add a review/rejected object for any missing id.
- The previous_accepted list is context only; output decisions only for ids present in candidates.
- Use "review" for borderline useful values that need human confirmation.
- Keep "accepted" values evidence-backed, stable, and non-generic.
- Reject catch-alls like misc, general, materials, components, items, blocks, or broad crafting.
- For workflow, "accepted" means ONLY a player-facing station/process/task the player would plan inventory around or use as a semantic search/task lens.
- For workflow, a good accepted value answers "what am I doing?" or "what station/process is this for?" Examples: casting, anvil, quern, bloomery, sequenced assembly, drying, alloying, barrel sealed.
- For workflow, accept recipe_type candidates when the id/label/evidence names a real reusable player-facing process, station, or task, even if semantic_evidence is sparse. Recipe types such as pressing, compacting, milling/crushing, cutting, rolling, filling, deploying, mixing, casting, anvil, quern, oven, barrel, bloomery, blast furnace, centrifugation, hammering, and polishing are valid workflow candidates.
- For workflow, accepting a process does not create a wall home. Err toward accepting real reusable processes for semantic lookup; use organization_group for conservative wall-home decisions.
- For workflow, reject guide/Ponder/quest/advancement titles that read like tutorial steps or one-off tasks: "using the deployer", "setting up display links", "addressing a stock ticker order", "processing items with the laser". Prefer the simpler reusable station/process candidate when it exists.
- For workflow, reject implementation/meta recipe mechanics even with high support: shaped, shapeless, no_remainder, damage_inputs, impostor, internal placeholder, synthetic helper recipes, broad vanilla crafting variants.
- For workflow, reject item/product/component families: frame, component, upgrade, repair, block_mod, colored/material/product lines, or "craft this one item" groups unless evidence clearly describes a reusable process the player plans around.
- For workflow, reject environmental physics/events unless they are a player workflow: collapse, landslide, falling block, decay, spread, growth ticks.
- For used_at, "accepted" means a player-facing station, machine, tool, surface, multiblock, or interaction context where items are processed or used.
- For used_at, recipe_type candidates can be accepted when the id/label names the station/process players recognize, such as anvil, quern, loom, oven, drying, mechanical press/pressing, compacting, macerating/crushing, mixer/mixing, barrel, blast furnace, centrifuge, deployer, filler, or rolling machine.
- For used_at, reject config labels, keybind text, UI error messages, JEI/EMI internals, jokes, recipe-transfer failures, implementation-only categories, and generic crafting.
- For used_at, prefer stable station/process ids over item ids unless the station item itself is the recognizable surface.
- For progression_stage, "accepted" means ONLY a pack/mod gate, tier, age, voltage band, dimension unlock, or major technology/material milestone.
- For progression_stage, accept broad gates like primitive alloys, steel, bloomery, blast furnace, mechanical power, moon, mars, venus, beneath, rocket tiers, LV/MV/HV, steam/electric ages.
- For progression_stage, reject ordinary guide topics, indexes, recipe lists, mobs, biomes, flora, equipment, boats, decorative blocks, individual crafted items, and one-off advancements.
- For progression_stage, reject advancement-title prose as accepted ids when the candidate id is the phrase itself, such as "one/small/step", "quite/the/sun/tan", or "back/in/black". Accept only canonical gate/tier/dimension/material ids backed by evidence.
- For progression_stage, a dimension word in a namespace/path is not enough. The label or semantic evidence must describe the dimension/unlock/gate itself.
- For progression_stage, material names are accepted only when they gate broad progression; reject isolated material variants or product lines.
- For organization_group, the #1 rule is: would a human player spend one of a small number of main-wall sections on this broad item type, so these items and their obvious siblings stay together?
- For organization_group, imagine the whole pack can sustain only about 15-20 human-named organization sections total, including built-in sections. Be stingy; accept a custom value only if it would deserve one of those scarce slots.
- For organization_group, group primarily by broad item type/role. Use case, material state, or workflow context can refine a broad type, but must not become the main reason to split related items.
- For organization_group, "accepted" means a stable main-wall storage section a player would actually maintain, not a workstation, recipe type, process, provenance label, or query/view.
- For organization_group, broad examples include equipment, cooked food, uncooked food, cooking supplies, workbenches, decorations, molds, metalworking supplies, fiber/cloth materials, masonry supplies, and reagents. These are illustrative examples, not a hard required list.
- For organization_group, the protected built-in wall sections are Food, Tools, Weapons, Armor, Lighting, Ingots, Gems, Raw Materials, Wood, Seeds, Crops, Plants, Clay & Pottery, Mob Drops, Storage, Stairs, Slabs, Walls, Doors, Fences, Windows, Building Blocks, Decoration, Natural, Workbenches, Mechanisms, Redstone, Upgrades, Transport, Utility, Curiosities, and Miscellaneous.
- For organization_group, Wood is the built-in home for sticks, logs, planks, boards, lumber, and close stock-wood siblings. Do not emit a custom wood organization group unless the proposal is a distinct broad non-stock woodcraft bucket that would not split those obvious siblings.
- For organization_group, Seeds, Crops, Plants, Clay & Pottery, and Mob Drops are built-in homes for seed stock, field produce, non-crop botanical stock, clay/brick/terracotta/pottery stock, and common mob/animal drops. Do not emit custom groups that merely rename or split those stock sections.
- For organization_group, Materials exists as a runtime fallback, but it is intentionally not protected here because it becomes too large in real packs. Prefer splitting would-be Materials items into roughly 3-6 broad, useful storage sections when evidence supports them, such as fiber/cloth materials, chemicals/reagents, molds, masonry supplies, ore/metal-related supplies, or other broad item-type groups.
- For organization_group, reject candidates that closely duplicate a protected built-in section or split items a player expects to scan together in one protected built-in section. Accept a near-built-in custom section only when the built-in parent is too overloaded and the candidate carves out a broad, player-obvious subset, such as cooking supplies out of Food/Utility.
- For organization_group, reject values that merely slice by mod name, tag taxonomy, material property such as stackable/pileable, material form/state, individual rock/geology type, recipe mechanic, workstation-specific process, color/style family, or other filters players may search for but would not keep as a primary storage section.
- For organization_group, reject groups too narrow for a main wall section, such as one mod's mechanical power line, stackable plates, or anvil smithing; keep those as mod_subsystem/workflow/used_at/search evidence instead.
- For organization_group, accept any candidate that passes the #1 rule with evidence, even if it is not listed in the examples.
- For organization_group, do not accept a value solely because the same id is a good workflow or used_at value. Wall-home groups must be item groupings a player would plausibly keep together.
- For mod_subsystem, "accepted" means ONLY a namespace-scoped identity system inside a mod: network, transport, automation, storage, multiblock, power, train, rocket, oxygen, or another broad functional family whose own items belong to that system.
- For mod_subsystem, reject pack-scoped ids, universal ids, process/task labels, one-off station labels, equipment/tool/armor sets, material families, decorative families, tiers, machine hulls/casings, and labels based only on recipe participation.
- For mod_subsystem, accepting a value does not create a wall home. It is semantic/query identity evidence.
- For mod_subsystem, use workflow/used_at for "where/how is this processed?" and organization_group for wall-home concepts. Subsystem values should answer "what mod system is this item itself part of?"
- Prefer preserving previous accepted ids.
- For workflow_role, every id must be <workflow>#<role> and parent must equal the workflow id.
- Only output JSON.`;

  const semanticLimits = args.facet === "mod_subsystem"
    ? MOD_SUBSYSTEM_PROMPT_SEMANTIC_EVIDENCE_LIMITS
    : PROMPT_SEMANTIC_EVIDENCE_LIMITS;
  const lastLimit = semanticLimits[semanticLimits.length - 1]!;
  for (const semanticEvidenceLimit of semanticLimits) {
    const user = JSON.stringify(buildVocabularyCurationUser(args, semanticEvidenceLimit), null, 2);
    if (system.length + user.length <= VOCABULARY_PROMPT_CHAR_BUDGET || semanticEvidenceLimit === lastLimit) {
      return { system, user };
    }
  }

  throw new Error("unreachable vocabulary prompt budget selection");
}

function buildVocabularyCurationUser(
  args: {
    facet: VocabularyFacetId;
    packId: string;
    candidates: readonly PackVocabularyCandidate[];
    previousAccepted: readonly string[];
    minEvidence: number;
  },
  semanticEvidenceLimit: number,
): Record<string, unknown> {
  const policy = FACET_POLICIES[args.facet] ?? FACETS[args.facet]?.description ?? "";
  return {
    pack_id: args.packId,
    facet: args.facet,
    policy,
    min_evidence: args.minEvidence,
    previous_accepted: args.previousAccepted,
    prompt_budget: {
      max_chars: VOCABULARY_PROMPT_CHAR_BUDGET,
      semantic_evidence_per_candidate: semanticEvidenceLimit,
      evidence_refs_per_candidate: args.facet === "mod_subsystem"
        ? MOD_SUBSYSTEM_PROMPT_EVIDENCE_LIMIT
        : CANDIDATE_EXAMPLE_LIMIT,
    },
    candidates: args.candidates.map((candidate) => promptCandidate(candidate, semanticEvidenceLimit, args.facet)),
    required_output_contract: {
      required_values_count: args.candidates.length,
      required_candidate_ids: args.candidates.map((candidate) => candidate.id),
      final_instructions: [
        "Return strict JSON only: one object with a top-level values array.",
        "The values array must contain exactly one object for every id in required_candidate_ids.",
        "Every output id must exactly match one candidate id from required_candidate_ids.",
        "Never omit rejected, low-quality, generic, or uncertain candidates; mark them rejected or review.",
        "Do not add ids that are not in required_candidate_ids.",
        "Before responding, count values.length and verify it equals required_values_count.",
      ],
    },
  };
}

function promptCandidate(
  candidate: PackVocabularyCandidate,
  semanticEvidenceLimit: number,
  facet: VocabularyFacetId,
): Record<string, unknown> {
  const semanticEvidence = candidate.semantic_evidence
    .slice(0, semanticEvidenceLimit)
    .map(promptSemanticEvidence);
  const omitted = Math.max(0, candidate.semantic_evidence.length - semanticEvidence.length);
  const evidenceLimit = facet === "mod_subsystem" ? MOD_SUBSYSTEM_PROMPT_EVIDENCE_LIMIT : CANDIDATE_EXAMPLE_LIMIT;
  const evidence = candidate.evidence.slice(0, evidenceLimit);
  const evidenceOmitted = Math.max(0, candidate.evidence.length - evidence.length);
  return {
    id: candidate.id,
    label: candidate.label,
    origin: candidate.origin,
    confidence: round(candidate.confidence),
    support: candidate.support,
    evidence,
    ...(evidenceOmitted > 0 ? { evidence_omitted: evidenceOmitted } : {}),
    semantic_evidence: semanticEvidence,
    ...(omitted > 0 ? { semantic_evidence_omitted: omitted } : {}),
    aliases: candidate.aliases,
    description: candidate.description,
    parent: candidate.parent,
    default_organization_group: candidate.default_organization_group,
    related_activity: candidate.related_activity,
  };
}
