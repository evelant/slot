import { FACETS } from "../schema/facets.ts";
import type { PackVocabularyCandidate, VocabularyFacetId, VocabularyPromptOverview } from "./types.ts";
import {
  FACET_POLICIES,
  BUILT_IN_VOCABULARY_SEEDS,
  MOD_SUBSYSTEM_PROMPT_SEMANTIC_EVIDENCE_LIMITS,
  PROMPT_SEMANTIC_EVIDENCE_LIMITS,
  VOCABULARY_PROMPT_CHAR_BUDGET,
} from "./constants.ts";
import { sortedLimited } from "./helpers.ts";
import { promptSemanticEvidence } from "./semantic_index.ts";

const PROMPT_SAMPLE_ITEM_LIMIT = 12;
const COMBINED_PROMPT_SEMANTIC_EVIDENCE_LIMITS = [8, 4, 2, 1, 0] as const;

export function buildVocabularyCurationPrompt(args: {
  facet: VocabularyFacetId;
  packId: string;
  candidates: readonly PackVocabularyCandidate[];
  previousAccepted: readonly string[];
  minEvidence: number;
  packOverview?: VocabularyPromptOverview;
}): { system: string; user: string } {
  const system = buildVocabularySynthesisSystem();

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

export interface CombinedVocabularyPromptFacetInput {
  facet: VocabularyFacetId;
  candidates: readonly PackVocabularyCandidate[];
  previousAccepted: readonly string[];
}

export function buildCombinedVocabularyCurationPrompt(args: {
  packId: string;
  facets: readonly CombinedVocabularyPromptFacetInput[];
  minEvidence: number;
  packOverview?: VocabularyPromptOverview;
}): { system: string; user: string } {
  const system = buildCombinedVocabularySynthesisSystem();
  const lastLimit = COMBINED_PROMPT_SEMANTIC_EVIDENCE_LIMITS[COMBINED_PROMPT_SEMANTIC_EVIDENCE_LIMITS.length - 1]!;
  for (const semanticEvidenceLimit of COMBINED_PROMPT_SEMANTIC_EVIDENCE_LIMITS) {
    const user = JSON.stringify(buildCombinedVocabularyCurationUser(args, semanticEvidenceLimit), null, 2);
    if (system.length + user.length <= VOCABULARY_PROMPT_CHAR_BUDGET || semanticEvidenceLimit === lastLimit) {
      return { system, user };
    }
  }
  throw new Error("unreachable combined vocabulary prompt budget selection");
}

function buildVocabularySynthesisSystem(): string {
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
      "related_activity": ["automation"],
      "default_organization_group": "value id, only when explicitly justified"
    }
  ]
}

Core contract:
- The user payload contains context_records and may contain pack_item_overview. They are concise context for synthesis, not proposed values and not an allowed-value list.
- Do not output one value per context record. Omit ordinary rejected/noisy context records entirely.
- Synthesize the vocabulary values the pack actually needs. Context ids are source handles, not candidate ids; only output an id after deciding it is the best stable vocabulary id.
- Output ids must match the requested facet's schema pattern. Follow the user payload's facet_value_id_guidance and facet_specific_rules for the requested facet.
- Optimize for human review. Output concise labels, descriptions, rationales, and examples that make yes/no/rename decisions easy.
- Do not output provenance scaffolding, file paths, context record ids, evidence refs, confidence scores, or source counts. Your job is to synthesize useful vocabulary.
- "accepted" means the value is ready for this pack. "review" means the value is usable by default but watch-listed for future debugging, playtesting, or optional human cleanup. "rejected" is only for previous usable values that should be retired, or for a specific harmful value that must be blocked; do not list routine rejected context.
- Preserve previous_usable values when they still fit. If a previous value is now clearly wrong, output it as rejected with a description explaining why.
- During automated refinement rounds, previous review or rejected values may appear in context_records. Treat them as prior suggestions, not facts: preserve, merge, rename, or reject them based on the full evidence and current item sample.
- When built_in_values is present, those values are already accepted built-ins for this facet. You do not need to repeat them in values.
- Do not synthesize values that duplicate, rename, or narrowly split built_in_values. Use the built-in concept instead; only emit an additional value when it adds a genuinely distinct reusable concept that the built-ins do not cover.
- Prefer fewer, clearer values over comprehensive taxonomies. Reject catch-alls such as misc, general, materials, components, items, blocks, recipes, crafting, or things.
- semantic_context and pack_item_overview are the primary signals. They preserve item names/ids, tooltip/lore, recipe roles/stations, guide/quest/advancement prose, stack groups, tag membership summaries, default-section pressure, and mod descriptions in compact form.
- Context text describes the pack; vocabulary values describe reusable concepts. Do not copy tutorial titles, prose sentences, URLs, UI labels, or implementation detail into ids.

Final response checklist:
- Return strict JSON only: one object with a top-level values array.
- The values array contains only synthesized vocabulary values worth accepting or reviewing, plus explicit rejected previous values when needed.
- Each accepted/review value should include a short rationale and 1-5 useful examples when available.
- Do not include a value merely because it appears many times. Ask whether it is a reusable player-facing vocabulary value for the requested facet.
- Make every id stable, concise, and valid for the requested facet.
- Only output JSON.`;
}

function buildCombinedVocabularySynthesisSystem(): string {
  return `You synthesize one SLOT pack facet vocabulary from evidence.

SLOT uses this vocabulary later when classifying individual Minecraft items.
Your job is to create compact, useful sets of stable semantic values for every
requested facet. Think like a player and pack author, not like a string matcher.

Output strict JSON only:
{
  "facets": {
    "facet_id": {
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
          "related_activity": ["automation"],
          "default_organization_group": "value id, only when explicitly justified"
        }
      ]
    }
  }
}

Core contract:
- The user payload contains a facets object plus pack_item_overview. They are concise context for synthesis, not proposed values and not an allowed-value list.
- Consider all requested facets together so values are coherent and not duplicated across facets.
- Do not output one value per context record. Omit ordinary rejected/noisy context records entirely.
- Synthesize the vocabulary values the pack actually needs. Context ids are source handles, not candidate ids; only output an id after deciding it is the best stable vocabulary id.
- Output ids must match each facet's schema pattern. Follow that facet's facet_value_id_guidance and facet_specific_rules.
- Optimize for human review. Output concise labels, descriptions, rationales, and examples that make yes/no/rename decisions easy.
- Do not output provenance scaffolding, file paths, context record ids, evidence refs, confidence scores, or source counts. Your job is to synthesize useful vocabulary.
- "accepted" means the value is ready for this pack. "review" means the value is usable by default but watch-listed for future debugging, playtesting, or optional human cleanup. "rejected" is only for previous usable values that should be retired, or for a specific harmful value that must be blocked; do not list routine rejected context.
- Preserve previous_usable values when they still fit. If a previous value is now clearly wrong, output it as rejected with a description explaining why.
- During automated refinement rounds, previous review or rejected values may appear in context_records. Treat them as prior suggestions, not facts: preserve, merge, rename, or reject them based on the full evidence and current item sample.
- When a facet has built_in_values, those values are already accepted built-ins for that facet. You do not need to repeat them in values.
- Do not synthesize values that duplicate, rename, or narrowly split built_in_values. Use the built-in concept instead; only emit an additional value when it adds a genuinely distinct reusable concept that the built-ins do not cover.
- Prefer fewer, clearer values over comprehensive taxonomies. Reject catch-alls such as misc, general, materials, components, items, blocks, recipes, crafting, or things.
- semantic_context and pack_item_overview are the primary signals. They preserve item names/ids, tooltip/lore, recipe roles/stations, guide/quest/advancement prose, stack groups, tag membership summaries, default-section pressure, and mod descriptions in compact form.
- Context text describes the pack; vocabulary values describe reusable concepts. Do not copy tutorial titles, prose sentences, URLs, UI labels, or implementation detail into ids.

Final response checklist:
- Return strict JSON only: one object with a top-level facets object.
- Include only requested facet ids as keys inside facets.
- Each facet values array contains only synthesized vocabulary values worth accepting or reviewing, plus explicit rejected previous values when needed.
- Each accepted/review value should include a short rationale and 1-5 useful examples when available.
- Do not include a value merely because it appears many times. Ask whether it is a reusable player-facing vocabulary value for that facet.
- Make every id stable, concise, and valid for its facet.
- Only output JSON.`;
}

function facetSynthesisRules(facet: VocabularyFacetId, packId: string): string {
  switch (facet) {
    case "material_family":
      return `Facet-specific rules for material_family:
- Synthesize the primary material/substance vocabulary the pack needs for item classification.
- Use concise lowercase tokens such as iron, wood_oak, leather, rubber, bronze, or steel. Do not emit display labels, pack path ids, item ids, or provenance prefixes.
- Prefer stable material families that group related item forms: ingots, plates, tools, blocks, slabs, fluids, food ingredients, mob drops, fibers, dyes, fuels, or composite parts made from the same material.
- Do not collapse player-distinguished submaterials into one broad value when item names/tags show reusable sibling families. Wood species, stone types, metals, gems, fibers, and common organic substances can be real material families when players choose, craft, build, or search by that substance.
- A useful material family does not need dozens of items. Include common vanilla-level substances such as fuel, fiber, hide, gem, organic, mineral, and wood-species materials when they recur or are clearly understood crafting/building substances.
- Normalize synonyms when players would expect one family. Split only when players treat the materials differently for crafting, progression, building palette, or storage.
- Material families are not organization groups. Rock/geology, color, finish, host stone, or processing form should not become material families unless the pack treats them as actual player-facing materials.`;
    case "material_secondary":
      return `Facet-specific rules for material_secondary:
- Synthesize reusable secondary material tokens for composite items.
- Use the same token style as material_family, but only for substances players would recognize as part of the item rather than recipe history.
- Do not duplicate the primary material, recipe catalysts, temporary molds, station names, or decorative color/finish labels.`;
    case "tier":
      return `Facet-specific rules for tier:
- Synthesize recurring progression/capability tier values for tools, weapons, armor, machines, circuits, materials, or pack systems.
- Good tier values are player-facing rungs that affect what an item can do, what it can craft, or when it becomes relevant.
- Do not use rarity, broad progression stages, individual item names, guide titles, organization groups, or material families unless the material itself is the actual tier name.`;
    case "required_tool_tier":
      return `Facet-specific rules for required_tool_tier:
- Synthesize harvest/tool requirement tier values from block tags, tool requirement text, guide prose, and item samples.
- Values should be the concise tier names a player recognizes when asking "what tier tool do I need?"
- Do not assume vanilla-only tiers; if the pack defines copper, bronze, steel, or other tool tiers, include them when evidence supports them.`;
    case "activity":
      return `Facet-specific rules for activity:
- Synthesize broad player activities that items participate in or help with. Activities are query/task lenses such as mining, building, farming, cooking, brewing, enchanting, smithing, trading, redstone, combat, exploration, transport, storage_management, automation, or magic.
- Activity is broader than workflow: "mining" or "redstone" can cover many tools, blocks, and components, while workflow captures a specific process like smelting or smithing.
- Include activities supported by item names, tags, recipe roles, creative tabs, guide/quest/advancement prose, and obvious sibling families. Do not require a recipe type with the exact activity name.
- Reject one-off actions, individual station names, implementation recipe mechanics, and organization homes that do not describe what the player is doing.`;
    case "material_process_stage":
      return `Facet-specific rules for material_process_stage:
- Synthesize reusable material-processing forms or stages that help classify resource chains: ore, raw, crushed, dust, shard, gem, nugget, ingot, block, plate, rod, sheet, molten, alloy, scrap, mold, and pack-specific equivalents when evidence supports them.
- This facet is about the material's process form, not its material family and not its storage home. Use material_family for iron/copper/oak/etc.; use organization_group for where a player stores it.
- Include broad recurring stages even when they overlap form labels. For example, an ore item can have form=ore and material_process_stage=ore because one describes shape/form and the other grounds material-chain semantics.
- Do not split by individual material, machine, host rock, pileable/stackable property, or one-off recipe step.`;
    case "organization_group":
      return `Facet-specific rules for organization_group:
- This facet is a direct main-wall storage/home signal. The #1 rule is: would a human player spend one of a small number of main inventory sections on this broad item type so obvious siblings stay together?
- The pack should usually need only a small number of custom organization groups because many homes are built in. accepted + review custom groups should be human-sized, not hundreds; if more than about 15 seem plausible, keep only the broadest and most useful.
- Do not be so conservative that you output nothing when the evidence clearly supports broad pack-specific storage families. Empty output is appropriate only when every useful broad family is already covered by protected built-ins.
- When broad pack-specific families are supported, emit a concise shortlist for human review, usually 3-10 values. Use state=review for plausible custom wall sections unless the value is a preserved accepted default.
- Use concise lower_snake values. Do not encode pack id, mod id, provenance, or source path in the value string; the facet and review metadata already provide that context.
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
- Synthesize scoped roles inside already-usable workflows only. Every id must be <workflow>#<role>, and parent must exactly equal the workflow id before #.
- Roles should be reusable positions such as input, output, catalyst, mold, container, fuel, tool, or byproduct when the workflow evidence supports them.
- Do not invent parent workflows here. If the parent workflow is not established by context or previous usable vocabulary, omit the role.`;
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
- Synthesize identity systems inside a mod: transport networks, automation lines, storage networks, multiblocks, power systems, trains, magic schools, or similar broad functional families.
- This is semantic/query identity evidence, not a wall-home source. Do not encode the mod namespace in the value; use metadata/evidence for provenance.
- The item itself must belong to the subsystem. Do not assign a subsystem merely because the item is consumed by a subsystem recipe.
- Reject equipment/tool/armor sets, material families, decorative families, tiers, machine casings, one-off stations, and process/task labels.`;
    case "produces_effect":
      return `Facet-specific rules for produces_effect:
- Synthesize namespace-qualified status-effect ids that items grant, apply, clear, or deliver.
- Prefer exact component, tooltip, lang, guide, or quest evidence. Potion contents, consume effects, equipment effects, and explicit "grants X" text are strong signals.
- Do not invent registry ids from vague magical/flavor text. If the evidence only says "special effect" or "buff" without a specific effect, omit it.`;
    case "multiblock_component_of":
      return `Facet-specific rules for multiblock_component_of:
- Synthesize namespace-qualified multiblock or built-structure ids for real named multiblocks this item is required to build.
- Use evidence from guide pages, quests, controller/tooltips, recipes, and item samples. Component relationship must be direct, not just recipe adjacency.
- Reject generic machines, individual workstations, recipe types, decorative buildings, and broad workflows.`;
    case "biome":
      return `Facet-specific rules for biome:
- Synthesize namespace-qualified biome ids only for biome-specific natural sources.
- Strong signals include loot/worldgen text, guide/quest statements, item names that directly encode a known biome, and semantic text saying the item is found/grows/spawns in that biome.
- Do not use dimensions, origin buckets, climate adjectives, decorative themes, or weak "forest-like" guesses as biome ids.`;
    default: {
      const policy = FACET_POLICIES[facet] ?? FACETS[facet]?.description ?? "";
      return `Facet-specific rules for ${facet}:
- ${policy}
- Synthesize a concise closed vocabulary for this facet from the evidence.
- Prefer reusable player-facing concepts over raw technical ids.
- Match the facet schema pattern exactly. Prefer simple lower_snake values; use namespace-qualified ids only when the facet represents real registry/resource ids.`;
    }
  }
}

function facetValueIdGuidance(facet: VocabularyFacetId, packId: string): string {
  switch (facet) {
    case "material_family":
    case "material_secondary":
    case "tier":
    case "required_tool_tier":
      return "For this facet, use concise lower_snake tokens such as iron, wood_oak, steel, bronze, or rubber; do not use display labels, pack ids, or source/provenance prefixes.";
    case "produces_effect":
    case "multiblock_component_of":
    case "biome":
      return "For this facet, use namespace-qualified registry-style ids such as minecraft:regeneration or minecraft:plains; do not use artificial provenance prefixes.";
    default:
      return "Use simple lowercase words joined by underscores, with optional slash path segments only when they materially improve clarity. Do not emit display labels or provenance prefixes as ids.";
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
  const builtInValues = builtInPromptValues(args.facet);
  const contextRecords = args.candidates
    .filter((candidate) => candidate.origin !== "built_in")
    .map((candidate) => promptCandidate(candidate, semanticEvidenceLimit, args.facet));
  return {
    pack_id: args.packId,
    ...(args.packOverview ? { pack_item_overview: args.packOverview } : {}),
    facet: args.facet,
    policy,
    facet_value_id_guidance: facetValueIdGuidance(args.facet, args.packId),
    facet_specific_rules: facetSynthesisRules(args.facet, args.packId),
    ...(builtInValues.length ? { built_in_values: builtInValues } : {}),
    previous_usable: args.previousAccepted,
    context_records: contextRecords,
    synthesis_contract: {
      context_record_count: contextRecords.length,
      final_instructions: [
        "Return strict JSON only: one object with a top-level values array.",
        "built_in_values are already accepted built-ins for this facet and will be included in the output vocabulary. Do not emit near-duplicates, synonyms, or narrow splits of those built-ins.",
        "context_records and pack_item_overview are context, not proposed values. Do not output one value per context_record or overview entry.",
        "previous_vocabulary_state marks carried baseline or prior-round vocabulary, not raw evidence. Keep it when it remains useful for this pack; do not duplicate it with synonyms or narrow splits.",
        "Output only synthesized vocabulary values worth accepting or reviewing.",
        "context_id values are source handles, not candidate ids. Only output an id after deciding it is the best stable vocabulary id.",
        "Omit routine rejected/noisy context_records. Use state=rejected only for previous usable values that should be retired or specific harmful ids that need a visible block.",
        "Do not output provenance metadata. Include concise rationale and examples for human review.",
      ],
    },
  };
}

function buildCombinedVocabularyCurationUser(
  args: {
    packId: string;
    facets: readonly CombinedVocabularyPromptFacetInput[];
    minEvidence: number;
    packOverview?: VocabularyPromptOverview;
  },
  semanticEvidenceLimit: number,
): Record<string, unknown> {
  const facets = Object.fromEntries(args.facets.map((entry) => {
    const policy = FACET_POLICIES[entry.facet] ?? FACETS[entry.facet]?.description ?? "";
    const builtInValues = builtInPromptValues(entry.facet);
    const contextRecords = entry.candidates
      .filter((candidate) => candidate.origin !== "built_in")
      .map((candidate) => promptCandidate(candidate, semanticEvidenceLimitForFacet(entry.facet, semanticEvidenceLimit), entry.facet));
    return [entry.facet, {
      policy,
      facet_value_id_guidance: facetValueIdGuidance(entry.facet, args.packId),
      facet_specific_rules: facetSynthesisRules(entry.facet, args.packId),
      ...(builtInValues.length ? { built_in_values: builtInValues } : {}),
      previous_usable: entry.previousAccepted,
      context_records: contextRecords,
      context_record_count: contextRecords.length,
    }];
  }));
  return {
    pack_id: args.packId,
    ...(args.packOverview ? { pack_item_overview: args.packOverview } : {}),
    facets,
    synthesis_contract: {
      requested_facets: args.facets.map((entry) => entry.facet),
      final_instructions: [
        "Return strict JSON only: one object with a top-level facets object.",
        "Each key under facets must be one of the requested facet ids.",
        "built_in_values are already accepted built-ins for that facet and will be included in the output vocabulary. Do not emit near-duplicates, synonyms, or narrow splits of those built-ins.",
        "context_records and pack_item_overview are context, not proposed values. Do not output one value per context_record or overview entry.",
        "previous_vocabulary_state marks carried baseline or prior-round vocabulary, not raw evidence. Keep it when it remains useful for this pack; do not duplicate it with synonyms or narrow splits.",
        "Output only synthesized vocabulary values worth accepting or reviewing.",
        "context_id values are source handles, not candidate ids. Only output an id after deciding it is the best stable vocabulary id.",
        "Omit routine rejected/noisy context_records. Use state=rejected only for previous usable values that should be retired or specific harmful ids that need a visible block.",
        "Do not output provenance metadata. Include concise rationale and examples for human review.",
      ],
    },
  };
}

function semanticEvidenceLimitForFacet(
  facet: VocabularyFacetId,
  limit: number,
): number {
  if (facet === "mod_subsystem") {
    return Math.min(limit, MOD_SUBSYSTEM_PROMPT_SEMANTIC_EVIDENCE_LIMITS[0]!);
  }
  return limit;
}

function builtInPromptValues(
  facet: VocabularyFacetId,
): Array<{ id: string; label: string; description?: string; aliases?: string[] }> {
  return (BUILT_IN_VOCABULARY_SEEDS[facet] ?? []).map((value) => ({
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
    ...(candidate.origin === "previous" ? { previous_vocabulary_state: candidate.suggested_state } : {}),
    semantic_context: semanticEvidence,
    ...(omitted > 0 ? { semantic_context_omitted: omitted } : {}),
    parent: candidate.parent,
    default_organization_group: candidate.default_organization_group,
    related_activity: candidate.related_activity,
  };
}
