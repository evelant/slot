import { FACETS } from "../schema/facets.ts";
import type { PackVocabularyCandidate, VocabularyFacetId, VocabularyPromptOverview } from "./types.ts";
import {
  FACET_POLICIES,
  MOD_SUBSYSTEM_PROMPT_SEMANTIC_EVIDENCE_LIMITS,
  PROMPT_SEMANTIC_EVIDENCE_LIMITS,
  UNIVERSAL_DEFAULTS,
  VOCABULARY_PROMPT_CHAR_BUDGET,
} from "./constants.ts";
import { sortedLimited } from "./helpers.ts";
import { promptSemanticEvidence } from "./semantic_index.ts";

const PROMPT_SAMPLE_ITEM_LIMIT = 12;

export function buildVocabularyCurationPrompt(args: {
  facet: VocabularyFacetId;
  packId: string;
  candidates: readonly PackVocabularyCandidate[];
  previousAccepted: readonly string[];
  minEvidence: number;
  packOverview?: VocabularyPromptOverview;
}): { system: string; user: string } {
  const system = buildVocabularySynthesisSystem(args.facet, args.packId);

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

function buildVocabularySynthesisSystem(facet: VocabularyFacetId, packId: string): string {
  return `You synthesize one SLOT pack facet vocabulary from evidence.

SLOT uses this vocabulary later when classifying individual Minecraft items.
Your job is to create a compact, useful set of stable semantic values for the
requested facet. Think like a player and pack author, not like a string matcher.

Output strict JSON only:
{
  "values": [
    {
      "id": "stable value id",
      "label": "Display label",
      "state": "accepted|review|rejected",
      "description": "short usage guidance for future item classification",
      "rationale": "one sentence explaining why this value helps players",
      "examples": ["short item names or ids that help a human reviewer judge it"],
      "aliases": ["optional alias"],
      "parent": "workflow id, only for workflow_role",
      "related_activity": ["slot:automation"],
      "default_organization_group": "value id, only when explicitly justified"
    }
  ]
}

Core contract:
- The user payload contains context_records and may contain pack_item_overview. They are concise context for synthesis, not proposed values and not an allowed-value list.
- Do not output one value per context record. Omit ordinary rejected/noisy context records entirely.
- Synthesize the vocabulary values the pack actually needs. Context ids are source handles, not candidate ids; only output an id after deciding it is the best stable vocabulary id.
- Use id grammar exactly: slot:<token_path> for universal concepts, <namespace>:<token_path> for mod-owned concepts, and pack:${packId}/<token_path> for pack-wide concepts. Use lowercase words joined by underscores and slash path segments; do not emit display labels as ids.
- Optimize for human review. Output concise labels, descriptions, rationales, and examples that make yes/no/rename decisions easy.
- Do not output provenance scaffolding, file paths, context record ids, evidence refs, confidence scores, or source counts. Your job is to synthesize useful vocabulary.
- "accepted" means the value is ready for this pack. "review" means the value is plausible but needs a human yes/no or rename. "rejected" is only for previous accepted values that should be retired, or for a specific harmful value that must be blocked; do not list routine rejected context.
- Preserve previous_accepted values when they still fit. If a previous value is now clearly wrong, output it as rejected with a description explaining why.
- When universal_default_values is present, those values are already accepted built-ins for this facet. You do not need to repeat them in values.
- Do not synthesize pack/mod/slot values that duplicate, rename, or narrowly split universal_default_values. Use the default concept instead; only emit an additional value when it adds a genuinely distinct reusable concept that the defaults do not cover.
- Prefer fewer, clearer values over comprehensive taxonomies. Reject catch-alls such as misc, general, materials, components, items, blocks, recipes, crafting, or things.
- semantic_context and pack_item_overview are the primary signals. They preserve item names/ids, tooltip/lore, recipe roles/stations, guide/quest/advancement prose, stack groups, tag membership summaries, default-section pressure, and mod descriptions in compact form.
- Context text describes the pack; vocabulary values describe reusable concepts. Do not copy tutorial titles, prose sentences, URLs, UI labels, or implementation detail into ids.

${facetSynthesisRules(facet, packId)}

Final response checklist:
- Return strict JSON only: one object with a top-level values array.
- The values array contains only synthesized vocabulary values worth accepting or reviewing, plus explicit rejected previous values when needed.
- Each accepted/review value should include a short rationale and 1-5 useful examples when available.
- Do not include a value merely because it appears many times. Ask whether it is a reusable player-facing vocabulary value for the requested facet.
- Make every id stable, concise, and valid for the requested facet.
- Only output JSON.`;
}

function facetSynthesisRules(facet: VocabularyFacetId, packId: string): string {
  switch (facet) {
    case "organization_group":
      return `Facet-specific rules for organization_group:
- This facet is a direct main-wall storage/home signal. The #1 rule is: would a human player spend one of a small number of main inventory sections on this broad item type so obvious siblings stay together?
- The pack should usually need only a small number of custom organization groups because many homes are built in. accepted + review custom groups should be human-sized, not hundreds; if more than about 15 seem plausible, keep only the broadest and most useful.
- Do not be so conservative that you output nothing when the evidence clearly supports broad pack-specific storage families. Empty output is appropriate only when every useful broad family is already covered by protected built-ins.
- When broad pack-specific families are supported, emit a concise shortlist for human review, usually 3-10 values. Use state=review for plausible custom wall sections unless the value is a preserved accepted default.
- Pack-specific storage families should use pack:${packId}/<token_path> ids. Use a mod namespace only for a concept that is genuinely owned by one mod rather than the pack's cross-mod item universe.
- Group primarily by broad item type or role. Use case and material state may refine a broad type, but must not be the main reason to split related items.
- Good custom groups are broad families a player would maintain: beekeeping, glass products, cooking supplies, dyes, fertilizers, weaving/cloth, masonry supplies, reagents, or similar pack-wide storage buckets. These are examples, not a hard list.
- Protected built-in wall sections are good homes, not bad categories: Food, Tools, Weapons, Armor, Lighting, Ores & Raw Stock, Metal Stock, Gems & Crystals, Dusts & Powders, Wood, Seeds, Crops, Plants, Ceramics & Molds, Organic Materials, Storage, Stairs, Slabs, Walls, Doors, Fences, Windows, Building Blocks, Decoration, Natural, Workbenches, Mechanisms, Redstone, Upgrades, Transport, Utility, Curiosities, and Miscellaneous.
- Do not synthesize custom groups that merely rename or split a protected built-in section. Item containers belong to Storage, lamps/light sources to Lighting, crops to Crops, pottery and molds to Ceramics & Molds, redstone components to Redstone, stock wood to Wood, and common organic stock to Organic Materials.
- Do not create groups from mod names, guide/tutorial/Ponder titles, individual machines, workstation-specific processes, recipe mechanics, UI modes, geometric/layout words, material form/state such as stackable or pileable, rock/geology taxonomy, color/style families, or one-off items.
- Workflow or station evidence is useful context for how items are used, but workflow names are not organization groups unless they describe a broad storage family a player would actually maintain.`;
    case "workflow":
      return `Facet-specific rules for workflow:
- Synthesize reusable player-facing tasks, processes, or station workflows that a player plans inventory around or uses as a semantic task lens.
- Good values answer "what am I doing?" or "what process/station is this for?" Examples in a generic modpack include casting, alloying, crushing, pressing, drying, brewing, enchanting, farming, or assembling.
- Prefer canonical process/station names over tutorial titles. Convert "Processing Items with the Press" into a concise workflow like a press/pressing value only when the process itself is broadly reusable.
- Reject implementation recipe mechanics, synthetic helper recipes, one-off craft-this-item topics, item families, UI/config labels, and environmental events that are not player workflows.
- Accept recipe-type context when it names a real process players recognize, even if prose evidence is sparse.`;
    case "workflow_role":
      return `Facet-specific rules for workflow_role:
- Synthesize scoped roles inside already-accepted workflows only. Every id must be <workflow>#<role>, and parent must exactly equal the workflow id before #.
- Roles should be reusable positions such as input, output, catalyst, mold, container, fuel, tool, or byproduct when the workflow evidence supports them.
- Do not invent parent workflows here. If the parent workflow is not established by context or previous accepted vocabulary, omit the role.`;
    case "used_at":
      return `Facet-specific rules for used_at:
- Synthesize player-facing stations, machines, tools, surfaces, multiblocks, or interaction contexts where items are processed or used.
- Prefer stable station/tool names over tutorial prose. "Using the Deployer" should become a deployer value only if deployer is a real reusable surface.
- Reject config labels, keybind text, UI errors, recipe-viewer internals, jokes, recipe-transfer failures, implementation-only categories, and generic crafting.`;
    case "progression_stage":
      return `Facet-specific rules for progression_stage:
- Synthesize only real pack/mod gates: named ages, tiers, dimensions, major material unlocks, tool tiers, machine tiers, or technology/magic milestones.
- A stage must change what the player can do, craft, visit, automate, or safely survive. Ordinary guide topics, recipe lists, mobs, biomes, decorative blocks, equipment families, and one-off achievements are not stages.
- Do not copy advancement-title prose as ids. Use a concise stable gate id backed by evidence.
- Avoid assuming any particular pack's progression model. Let the evidence define the gates.`;
    case "mod_subsystem":
      return `Facet-specific rules for mod_subsystem:
- Synthesize namespace-scoped identity systems inside a mod: transport networks, automation lines, storage networks, multiblocks, power systems, trains, magic schools, or similar broad functional families.
- This is semantic/query identity evidence, not a wall-home source. Do not use pack-scoped ids for subsystem values.
- The item itself must belong to the subsystem. Do not assign a subsystem merely because the item is consumed by a subsystem recipe.
- Reject equipment/tool/armor sets, material families, decorative families, tiers, machine casings, one-off stations, and process/task labels.`;
    default: {
      const policy = FACET_POLICIES[facet] ?? FACETS[facet]?.description ?? "";
      return `Facet-specific rules for ${facet}:
- ${policy}
- Synthesize a concise closed vocabulary for this facet from the evidence.
- Prefer reusable player-facing concepts over raw technical ids.
- Use universal slot:* ids only for concepts that are broadly applicable beyond this pack; use pack-scoped or namespace-scoped ids for pack/mod-specific concepts.`;
    }
  }
}

function buildVocabularyCurationUser(
  args: {
    facet: VocabularyFacetId;
    packId: string;
    candidates: readonly PackVocabularyCandidate[];
    previousAccepted: readonly string[];
    minEvidence: number;
    packOverview?: VocabularyPromptOverview;
  },
  semanticEvidenceLimit: number,
): Record<string, unknown> {
  const policy = FACET_POLICIES[args.facet] ?? FACETS[args.facet]?.description ?? "";
  const defaultValues = universalDefaultPromptValues(args.facet);
  const contextRecords = args.candidates
    .filter((candidate) => candidate.origin !== "universal_default")
    .map((candidate) => promptCandidate(candidate, semanticEvidenceLimit, args.facet));
  return {
    pack_id: args.packId,
    facet: args.facet,
    policy,
    ...(defaultValues.length ? { universal_default_values: defaultValues } : {}),
    previous_accepted: args.previousAccepted,
    ...(args.packOverview ? { pack_item_overview: args.packOverview } : {}),
    context_records: contextRecords,
    synthesis_contract: {
      context_record_count: contextRecords.length,
      final_instructions: [
        "Return strict JSON only: one object with a top-level values array.",
        "universal_default_values are already accepted built-ins for this facet. Do not emit pack/mod/slot near-duplicates, synonyms, or narrow splits of those defaults.",
        "context_records and pack_item_overview are context, not proposed values. Do not output one value per context_record or overview entry.",
        "Output only synthesized vocabulary values worth accepting or reviewing.",
        "context_id values are source handles, not candidate ids. Only output an id after deciding it is the best stable vocabulary id.",
        "Omit routine rejected/noisy context_records. Use state=rejected only for previous accepted values that should be retired or specific harmful ids that need a visible block.",
        "Do not output provenance metadata. Include concise rationale and examples for human review.",
      ],
    },
  };
}

function universalDefaultPromptValues(
  facet: VocabularyFacetId,
): Array<{ id: string; label: string; description?: string; aliases?: string[] }> {
  return (UNIVERSAL_DEFAULTS[facet] ?? []).map((value) => ({
    id: value.id,
    label: value.label,
    ...(value.description ? { description: value.description } : {}),
    ...(value.aliases?.length ? { aliases: value.aliases } : {}),
  }));
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
  return {
    context_id: candidate.id,
    label: candidate.label,
    ...(candidate.description ? { note: candidate.description } : {}),
    ...(candidate.aliases.length ? { aliases: candidate.aliases } : {}),
    ...(candidate.seed_items.length ? { sample_item_ids: sortedLimited(candidate.seed_items, PROMPT_SAMPLE_ITEM_LIMIT) } : {}),
    semantic_context: semanticEvidence,
    ...(omitted > 0 ? { semantic_context_omitted: omitted } : {}),
    parent: candidate.parent,
    default_organization_group: candidate.default_organization_group,
    related_activity: candidate.related_activity,
  };
}
