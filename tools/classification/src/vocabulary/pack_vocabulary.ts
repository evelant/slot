import { readFileSync } from "node:fs";
import type { LlmClient, QueryOptions } from "../llm/client.ts";
import type {
  FacetEvidenceArtifact,
  FacetEvidenceRecord,
  FacetEvidenceKind,
} from "../evidence/facet_evidence.ts";
import {
  FACETS,
  VOCABULARY_BACKED_FACETS,
  validateMultiValue,
} from "../schema/facets.ts";
import {
  validateVocabularyArtifact,
  type PackFacetVocabulary,
  type VocabularyEvidenceRef,
  type VocabularyOrigin,
  type VocabularyState,
  type VocabularyValue,
} from "../schema/vocabulary.ts";

export type VocabularyFacetId = typeof VOCABULARY_BACKED_FACETS[number];

export interface PackVocabularyCandidate {
  facet: VocabularyFacetId;
  id: string;
  label: string;
  origin: VocabularyOrigin;
  suggested_state: VocabularyState;
  confidence: number;
  support: number;
  evidence: VocabularyEvidenceRef[];
  semantic_evidence: VocabularySemanticEvidence[];
  seed_items: string[];
  aliases: string[];
  description?: string;
  parent?: string;
  default_organization_group?: string;
  related_activity?: string[];
  reasons: string[];
}

export interface VocabularySemanticEvidence {
  kind: FacetEvidenceKind;
  id: string;
  source: string;
  text?: string;
  key?: string;
  label?: string;
  item_ref_count?: number;
  recipe_ref_count?: number;
  role?: string;
  recipe_type?: string;
  count?: number;
}

export interface PackFacetVocabularyReview {
  schema_version: 1;
  kind: "slot-pack-facet-vocabulary-review";
  pack_id: string;
  generated_by: string;
  generated_at: string;
  source: PackFacetVocabulary["source"];
  model?: string;
  filters: {
    facets: string[];
    namespaces: string[];
    min_evidence: number;
    previous_vocabulary?: string;
  };
  candidates: Record<string, PackVocabularyCandidate[]>;
  decisions: Record<string, VocabularyDecision[]>;
  diagnostics: VocabularyDiagnostic[];
  raw_responses: Record<string, string>;
}

export interface VocabularyDecision {
  facet: VocabularyFacetId;
  id: string;
  label: string;
  state: VocabularyState;
  origin: VocabularyOrigin;
  confidence: number;
  evidence: VocabularyEvidenceRef[];
  seed_items: string[];
  aliases?: string[];
  description?: string;
  parent?: string;
  default_organization_group?: string;
  related_activity?: string[];
  policy_notes: string[];
}

export interface VocabularyDiagnostic {
  severity: "info" | "warning" | "error";
  facet?: string;
  id?: string;
  message: string;
}

export interface ExtractVocabularyCandidatesOptions {
  packId: string;
  facets?: readonly string[];
  namespaces?: readonly string[];
  minEvidence: number;
  previousVocabulary?: PackFacetVocabulary;
}

export interface ProposePackFacetVocabularyOptions extends ExtractVocabularyCandidatesOptions {
  evidence: FacetEvidenceArtifact;
  generatedBy: string;
  generatedAt?: string;
  evidencePath: string;
  previousVocabularyPath?: string;
  model?: string;
  client?: LlmClient;
  clientOptions?: Partial<QueryOptions>;
  maxCandidatesPerFacet?: number;
  maxCandidatesPerPrompt?: number;
}

export interface ProposePackFacetVocabularyResult {
  vocabulary: PackFacetVocabulary;
  review: PackFacetVocabularyReview;
  prompts: Record<string, { system: string; user: string }>;
}

interface CandidateAccumulator extends PackVocabularyCandidate {
  evidenceKeys: Set<string>;
  semanticEvidenceKeys: Set<string>;
  seedItemKeys: Set<string>;
  aliasKeys: Set<string>;
  reasonKeys: Set<string>;
}

interface CandidateSeed {
  facet: VocabularyFacetId;
  id: string;
  label: string;
  origin: VocabularyOrigin;
  suggestedState: VocabularyState;
  confidence: number;
  support?: number;
  evidence?: VocabularyEvidenceRef[];
  semanticEvidence?: readonly VocabularySemanticEvidence[];
  seedItems?: readonly string[];
  aliases?: readonly string[];
  description?: string;
  parent?: string;
  defaultOrganizationGroup?: string;
  relatedActivity?: readonly string[];
  reason?: string;
}

interface CuratedValue {
  id: string;
  label?: string;
  state?: VocabularyState;
  description?: string;
  aliases?: string[];
  confidence?: number;
  evidence?: VocabularyEvidenceRef[];
  seed_items?: string[];
  parent?: string;
  default_organization_group?: string;
  related_activity?: string[];
}

interface SemanticEvidenceIndex {
  byItem: Map<string, VocabularySemanticEvidence[]>;
  byRuntimeItem: Map<string, VocabularySemanticEvidence[]>;
  byRecipeType: Map<string, VocabularySemanticEvidence[]>;
  byNamespace: Map<string, VocabularySemanticEvidence[]>;
  runtimeItemIds: Set<string>;
  runtimeTooltipBoilerplate: Set<string>;
}

const DEFAULT_MAX_CANDIDATES_PER_FACET = 256;
const DEFAULT_MAX_CANDIDATES_PER_PROMPT = 48;
const CANDIDATE_EXAMPLE_LIMIT = 64;
const SEMANTIC_EVIDENCE_LIMIT = 64;
const VOCABULARY_PROMPT_CHAR_BUDGET = 3_200_000;
const VOCABULARY_CURATION_TIMEOUT_MS = 1_200_000;
const PROMPT_SEMANTIC_EVIDENCE_LIMITS = [64, 48, 40, 32, 24, 16, 12, 8, 4, 2, 1, 0] as const;
const MOD_SUBSYSTEM_PROMPT_SEMANTIC_EVIDENCE_LIMITS = [12, 8, 4, 2, 1, 0] as const;
const MOD_SUBSYSTEM_PROMPT_EVIDENCE_LIMIT = 16;
const SEMANTIC_TEXT_PROMPT_LIMIT = 1_200;
const RUNTIME_TOOLTIP_REPEAT_BOILERPLATE_THRESHOLD = 250;
const GENERIC_TOKENS = new Set([
  "misc",
  "miscellaneous",
  "general",
  "generic",
  "materials",
  "material",
  "components",
  "component",
  "items",
  "blocks",
  "things",
  "stuff",
  "crafting",
  "crafting_shaped",
  "crafting_shapeless",
  "recipes",
  "recipe",
  "machine",
  "machines",
]);

const MOD_SUBSYSTEM_STOP_TOKENS = new Set([
  ...GENERIC_TOKENS,
  "armor",
  "armour",
  "block",
  "bucket",
  "clothing",
  "component",
  "components",
  "dust",
  "equipment",
  "gear",
  "ingot",
  "item",
  "material",
  "nugget",
  "ore",
  "plate",
  "resource",
  "resources",
  "rod",
  "screw",
  "suit",
  "tool",
  "tools",
  "weapon",
  "weapons",
  "wire",
]);

const MOD_SUBSYSTEM_SIGNAL_TOKENS = new Set([
  "assembler",
  "automation",
  "battery",
  "batteries",
  "belt",
  "bus",
  "buses",
  "cable",
  "cables",
  "cell",
  "cells",
  "charger",
  "chargers",
  "chute",
  "chutes",
  "cogwheel",
  "contraption",
  "conveyor",
  "cover",
  "crafter",
  "deployer",
  "duct",
  "energy",
  "engine",
  "export",
  "fan",
  "fission",
  "fluid",
  "generator",
  "hatch",
  "interface",
  "kinetic",
  "kinetics",
  "logistics",
  "mechanical_power",
  "power",
  "multiblock",
  "network",
  "oxygen",
  "package",
  "pipe",
  "pipes",
  "pump",
  "rail",
  "reactor",
  "rocket",
  "schematic",
  "shaft",
  "storage",
  "tank",
  "terminal",
  "track",
  "train",
  "trains",
  "transport",
  "turbine",
]);

const MOD_SUBSYSTEM_CANONICAL_TOKENS = new Map([
  ["batteries", "battery"],
  ["buses", "bus"],
  ["cables", "cable"],
  ["cells", "cell"],
  ["chargers", "charger"],
  ["chutes", "chute"],
  ["kinetic", "kinetics"],
  ["pipes", "pipe"],
  ["trains", "train"],
]);

const ORGANIZATION_GROUP_STOP_TOKENS = new Set([
  "anvil",
  "barrel",
  "blasting",
  "compacting",
  "crafting",
  "cutting",
  "default",
  "deploying",
  "filling",
  "milling",
  "mixing",
  "oven",
  "pot",
  "pressing",
  "quern",
  "smelting",
  "stonecutting",
]);

const VOLTAGE_COMPONENT_TOKENS = new Set([
  "battery",
  "cable",
  "circuit",
  "component",
  "components",
  "conveyor",
  "cover",
  "electric",
  "emitter",
  "field",
  "generator",
  "hatch",
  "hull",
  "motor",
  "piston",
  "pump",
  "robot",
  "sensor",
]);

const VOLTAGE_TIERS = ["ulv", "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv", "uhv"] as const;

const UNIVERSAL_DEFAULTS: Record<string, Array<{ id: string; label: string; description?: string; aliases?: string[] }>> = {
  activity: ([
    ["slot:mining", "Mining"],
    ["slot:exploration", "Exploration"],
    ["slot:cooking", "Cooking", "Food preparation, cooking, meals, and drink work.", ["food prep"]],
    ["slot:building", "Building"],
    ["slot:decorating", "Decorating"],
    ["slot:combat", "Combat"],
    ["slot:farming", "Farming"],
    ["slot:redstone", "Redstone"],
    ["slot:automation", "Automation"],
    ["slot:logistics", "Logistics"],
    ["slot:storage_management", "Storage Management"],
    ["slot:brewing", "Brewing"],
    ["slot:enchanting", "Enchanting"],
    ["slot:magic", "Magic"],
    ["slot:power_generation", "Power Generation"],
    ["slot:transportation", "Transportation"],
  ] satisfies Array<[string, string, string?, string[]?]>).map(defaultTuple),
  food_category: [
    "fruit",
    "vegetable",
    "grain",
    "flour",
    "dough",
    "bread",
    "meat",
    "fish",
    "dairy",
    "cheese",
    "egg",
    "spice",
    "sweetener",
    "fat_oil",
    "sauce",
    "prepared_meal",
    "preserve",
    "drink",
    "bowl",
  ].map(slotDefault),
  food_use: [
    "eat_now",
    "ingredient",
    "meal_component",
    "drink",
    "preserve",
    "animal_feed",
    "buff_food",
    "cooking_fat",
    "sweetener",
    "spice",
  ].map(slotDefault),
  preparation_state: [
    "raw",
    "cooked",
    "dried",
    "salted",
    "pickled",
    "fermented",
    "curdled",
    "flour",
    "dough",
    "preserved",
    "unsealed",
    "sealed",
  ].map(slotDefault),
  material_process_stage: [
    "ore",
    "crushed_ore",
    "purified_ore",
    "dust",
    "tiny_dust",
    "nugget",
    "ingot",
    "double_ingot",
    "sheet",
    "double_sheet",
    "rod",
    "plate",
    "bloom",
    "billet",
    "molten",
    "alloy",
    "mold",
  ].map(slotDefault),
  stock_profile: [
    "bulk",
    "small_batch",
    "singleton",
    "tooling",
    "reserve",
    "display",
    "overflow",
  ].map(slotDefault),
  container_state: [
    "empty_container",
    "filled_container",
    "fluid_container",
    "gas_container",
    "energy_container",
    "reusable_mold",
    "single_use_mold",
    "pattern_template",
    "has_contents",
    "accepts_contents",
  ].map(slotDefault),
  equipment_effect: [
    "night_vision",
    "water_breathing",
    "oxygen_supply",
    "flight",
    "step_assist",
    "speed_boost",
    "reach_boost",
    "tool_mode",
  ].map(slotDefault),
  protection_context: [
    "heat",
    "cold",
    "radiation",
    "vacuum",
    "pressure",
    "fire",
    "poison",
    "fall",
    "magic",
  ].map(slotDefault),
  loadout_context: [
    "mining_run",
    "cave_run",
    "farming_run",
    "building_project",
    "exploration_trip",
    "combat_trip",
    "base_maintenance",
    "machine_setup",
  ].map(slotDefault),
  use_affordance: [
    "place",
    "eat",
    "drink",
    "equip",
    "fuel",
    "repair",
    "configure",
    "cast",
    "fill",
    "empty",
    "preserve",
    "harvest",
    "scan",
    "launch",
  ].map(slotDefault),
};

const FACET_POLICIES: Partial<Record<VocabularyFacetId, string>> = {
  activity: "Broad player activities. Prefer universal slot:* defaults. Reject recipe-internal verbs.",
  workflow: "Player-facing process/task contexts. Use recipe, guide, quest, or advancement evidence. Reject catch-alls and one-off tutorial/advancement titles.",
  workflow_role: "Scoped role values only, formatted as <workflow>#<role>. Parent must be an accepted workflow candidate.",
  used_at: "Player-facing station, machine, tool, or surface. Preserve raw recipe type evidence separately.",
  progression_stage: "Pack/mod progression gates, tiers, ages, voltages, dimensions. Conservative; reject item/product topics and one-off advancements.",
  organization_group: "Direct wall-home candidate: human storage/section groupings, not stations or recipe workflows.",
  mod_subsystem: "Identity-oriented mod subsystem. Do not assign from recipe participation alone.",
};

const ROLE_TOKENS = ["input", "output"] as const;

export function readFacetEvidenceArtifactFile(path: string): FacetEvidenceArtifact {
  const parsed = JSON.parse(readFileSync(path, "utf8")) as unknown;
  if (!isRecord(parsed)) throw new Error(`facet evidence must be an object: ${path}`);
  if (parsed.kind !== "slot-pack-facet-evidence") {
    throw new Error(`facet evidence kind must be slot-pack-facet-evidence: ${path}`);
  }
  if (parsed.schema_version !== 1) {
    throw new Error(`facet evidence schema_version must be 1: ${path}`);
  }
  if (typeof parsed.pack_id !== "string" || parsed.pack_id.length === 0) {
    throw new Error(`facet evidence pack_id is missing: ${path}`);
  }
  if (!Array.isArray(parsed.records)) {
    throw new Error(`facet evidence records must be an array: ${path}`);
  }
  return parsed as unknown as FacetEvidenceArtifact;
}

export function extractVocabularyCandidates(
  evidence: FacetEvidenceArtifact,
  options: ExtractVocabularyCandidatesOptions,
): PackVocabularyCandidate[] {
  const facets = facetSet(options.facets);
  const namespaces = new Set((options.namespaces ?? []).filter(Boolean));
  const acc = new Map<string, CandidateAccumulator>();

  addUniversalDefaults(acc, facets);
  if (options.previousVocabulary) {
    addPreviousVocabulary(acc, options.previousVocabulary, facets);
  }

  const records = evidence.records.filter((record) => namespaceAllowed(record, namespaces));
  const semanticIndex = buildSemanticEvidenceIndex(records);
  for (const record of records) {
    addCandidatesFromEvidence(acc, record, options.packId, facets, options.minEvidence, semanticIndex);
  }
  addWorkflowRoleCandidates(acc, options.minEvidence);

  return [...acc.values()]
    .map(finalizeCandidate)
    .filter((candidate) => facets.has(candidate.facet))
    .sort(compareCandidates);
}

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
- For organization_group, "accepted" means a human player storage section or mental bucket for items, not a workstation, recipe type, or process.
- For organization_group, good accepted examples include unprocessed ores, refined ores, cooking tools, Create items, ULV components, woodworking, decorative, animal husbandry, weaving/cloth, dirt and rocks, seeds, inedible plants, and crops. These examples are illustrative, not a closed list.
- For organization_group, accept any candidate that names a plausible player storage section with evidence, even if it is not listed in the examples.
- For organization_group, reject workstation/process labels such as anvil, quern, pot, barrel, smelting, blasting, milling, pressing, cutting, and mixing unless the candidate itself names a broader storage group like cooking_tools or woodworking.
- For organization_group, do not accept a value solely because the same id is a good workflow or used_at value. Wall-home groups must be item groupings a player would plausibly keep together.
- For mod_subsystem, "accepted" means ONLY a namespace-scoped identity system inside a mod: network, transport, automation, storage, multiblock, power, train, rocket, oxygen, or another broad functional family whose own items belong to that system.
- For mod_subsystem, reject pack-scoped ids, universal ids, process/task labels, one-off station labels, equipment/tool/armor sets, material families, decorative families, tiers, machine hulls/casings, and labels based only on recipe participation.
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

export function parseVocabularyCurationResponse(raw: string): CuratedValue[] {
  const inner = unwrapEnvelope(raw);
  const json = firstJsonObject(inner);
  if (!json) return [];
  let parsed: unknown;
  try {
    parsed = JSON.parse(json);
  } catch {
    return [];
  }
  if (!isRecord(parsed)) return [];
  const rawValues = Array.isArray(parsed.values)
    ? parsed.values
    : isRecord(parsed.facets)
      ? Object.values(parsed.facets).flatMap((facet) =>
        isRecord(facet) && Array.isArray(facet.values) ? facet.values : []
      )
      : [];
  const out: CuratedValue[] = [];
  for (const rawValue of rawValues) {
    if (!isRecord(rawValue) || typeof rawValue.id !== "string") continue;
    out.push({
      id: rawValue.id,
      ...(typeof rawValue.label === "string" ? { label: rawValue.label } : {}),
      ...(isVocabularyState(rawValue.state) ? { state: rawValue.state } : {}),
      ...(typeof rawValue.description === "string" ? { description: rawValue.description } : {}),
      ...(Array.isArray(rawValue.aliases) ? { aliases: stringArray(rawValue.aliases) } : {}),
      ...(isConfidence(rawValue.confidence) ? { confidence: rawValue.confidence } : {}),
      ...(Array.isArray(rawValue.evidence) ? { evidence: evidenceRefs(rawValue.evidence) } : {}),
      ...(Array.isArray(rawValue.seed_items) ? { seed_items: stringArray(rawValue.seed_items) } : {}),
      ...(typeof rawValue.parent === "string" ? { parent: rawValue.parent } : {}),
      ...(typeof rawValue.default_organization_group === "string"
        ? { default_organization_group: rawValue.default_organization_group }
        : {}),
      ...(Array.isArray(rawValue.related_activity) ? { related_activity: stringArray(rawValue.related_activity) } : {}),
    });
  }
  return out;
}

export async function proposePackFacetVocabulary(
  options: ProposePackFacetVocabularyOptions,
): Promise<ProposePackFacetVocabularyResult> {
  const generatedAt = options.generatedAt ?? new Date().toISOString();
  const facets = [...facetSet(options.facets)].sort();
  const candidates = extractVocabularyCandidates(options.evidence, options);
  const maxCandidatesPerFacet = options.maxCandidatesPerFacet ?? DEFAULT_MAX_CANDIDATES_PER_FACET;
  const candidatesByFacet = groupCandidates(candidates, maxCandidatesPerFacet);
  const rawCandidatesByFacet = groupCandidates(candidates, Number.MAX_SAFE_INTEGER);
  const previousAccepted = previousAcceptedByFacet(options.previousVocabulary);
  const acceptedWorkflowIds = new Set(previousAccepted.get("workflow") ?? []);
  const maxCandidatesPerPrompt = options.maxCandidatesPerPrompt ?? DEFAULT_MAX_CANDIDATES_PER_PROMPT;
  const prompts: Record<string, { system: string; user: string }> = {};
  const decisions: Record<string, VocabularyDecision[]> = {};
  const rawResponses: Record<string, string> = {};
  const diagnostics: VocabularyDiagnostic[] = [];

  for (const facet of facets) {
    const facetCandidates = facet === "workflow_role"
      ? selectPromptCandidates(
        workflowRoleCandidatesForAcceptedWorkflows(rawCandidatesByFacet[facet] ?? [], acceptedWorkflowIds),
        maxCandidatesPerFacet,
      )
      : candidatesByFacet[facet] ?? [];
    const candidateChunks = chunkCandidates(facetCandidates, maxCandidatesPerPrompt);
    const curatedValues: CuratedValue[] = [];
    for (let index = 0; index < candidateChunks.length; index++) {
      const chunk = candidateChunks[index]!;
      const promptKey = candidateChunks.length === 1
        ? facet
        : `${facet}.part-${String(index + 1).padStart(2, "0")}-of-${String(candidateChunks.length).padStart(2, "0")}`;
      prompts[promptKey] = buildVocabularyCurationPrompt({
        facet,
        packId: options.packId,
        candidates: chunk,
        previousAccepted: previousAccepted.get(facet) ?? [],
        minEvidence: options.minEvidence,
      });
      const curated: { raw: string; values: CuratedValue[] } = options.client
        ? await queryFacetCuration(options.client, prompts[promptKey]!, options.model, options.clientOptions, chunk.map((candidate) => candidate.id))
        : { raw: "", values: [] };
      curatedValues.push(...curated.values);
      if (curated.raw) rawResponses[promptKey] = curated.raw;
    }
    decisions[facet] = applyFacetPolicy({
      facet,
      candidates: facetCandidates,
      curated: curatedValues,
      minEvidence: options.minEvidence,
      diagnostics,
      acceptedWorkflowIds: facet === "workflow_role" ? acceptedWorkflowIds : undefined,
    });
    if (facet === "workflow") {
      for (const value of decisions[facet] ?? []) {
        if (value.state === "accepted") acceptedWorkflowIds.add(value.id);
      }
    }
  }

  const vocabulary: PackFacetVocabulary = {
    schema_version: 1,
    kind: "slot-pack-facet-vocabulary",
    pack_id: options.packId,
    generated_by: options.generatedBy,
    generated_at: generatedAt,
    source: {
      facet_evidence: options.evidencePath,
      ...(options.evidence.source ?? {}),
      ...(options.previousVocabularyPath ? { previous_vocabulary: options.previousVocabularyPath } : {}),
    },
    facets: decisionsToAcceptedVocabulary(decisions),
  };
  const validation = validateVocabularyArtifact(vocabulary);
  if (!validation.ok) {
    for (const error of validation.errors) {
      diagnostics.push({ severity: "error", message: error });
    }
  }

  const review: PackFacetVocabularyReview = {
    schema_version: 1,
    kind: "slot-pack-facet-vocabulary-review",
    pack_id: options.packId,
    generated_by: options.generatedBy,
    generated_at: generatedAt,
    source: vocabulary.source,
    ...(options.model ? { model: options.model } : {}),
    filters: {
      facets,
      namespaces: [...(options.namespaces ?? [])].sort(),
      min_evidence: options.minEvidence,
      ...(options.previousVocabularyPath ? { previous_vocabulary: options.previousVocabularyPath } : {}),
    },
    candidates: Object.fromEntries(
      Object.entries(candidatesByFacet).map(([facet, values]) => [facet, values.map(stripCandidateInternals)]),
    ),
    decisions,
    diagnostics,
    raw_responses: rawResponses,
  };

  return { vocabulary, review, prompts };
}

function addUniversalDefaults(
  acc: Map<string, CandidateAccumulator>,
  facets: ReadonlySet<VocabularyFacetId>,
): void {
  for (const [facet, values] of Object.entries(UNIVERSAL_DEFAULTS)) {
    if (!isVocabularyFacet(facet) || !facets.has(facet)) continue;
    for (const value of values) {
      addCandidate(acc, {
        facet,
        id: value.id,
        label: value.label,
        description: value.description,
        aliases: value.aliases,
        origin: "universal_default",
        suggestedState: "accepted",
        confidence: 0.95,
        support: 999,
        reason: "universal default",
      });
    }
  }
}

function addPreviousVocabulary(
  acc: Map<string, CandidateAccumulator>,
  previous: PackFacetVocabulary,
  facets: ReadonlySet<VocabularyFacetId>,
): void {
  for (const [facet, facetValues] of Object.entries(previous.facets ?? {})) {
    if (!isVocabularyFacet(facet) || !facets.has(facet)) continue;
    for (const [id, value] of Object.entries(facetValues.values ?? {})) {
      addCandidate(acc, {
        facet,
        id,
        label: value.label,
        description: value.description,
        aliases: value.aliases,
        origin: "previous",
        suggestedState: value.state,
        confidence: value.confidence ?? 0.9,
        support: value.state === "accepted" ? 999 : 1,
        evidence: value.evidence,
        seedItems: value.seed_items,
        parent: value.parent,
        defaultOrganizationGroup: value.default_organization_group,
        relatedActivity: value.related_activity,
        reason: `previous vocabulary state=${value.state}`,
      });
    }
  }
}

function addCandidatesFromEvidence(
  acc: Map<string, CandidateAccumulator>,
  record: FacetEvidenceRecord,
  packId: string,
  facets: ReadonlySet<VocabularyFacetId>,
  minEvidence: number,
  semanticIndex: SemanticEvidenceIndex,
): void {
  switch (record.kind) {
    case "recipe_type": {
      const id = normalizeScopedResourceId(record.id, packId, "namespace");
      if (!id || isGenericValueId(id)) return;
      const count = record.count ?? record.input_count ?? record.output_count ?? 1;
      addEvidenceCandidate(acc, {
        facet: "workflow",
        id,
        record,
        count,
        facets,
        state: count >= minEvidence ? "accepted" : "review",
        reason: "recipe type names a repeated process",
        semanticIndex,
      });
      addEvidenceCandidate(acc, {
        facet: "used_at",
        id,
        record,
        count,
        facets,
        state: count >= minEvidence ? "review" : "review",
        reason: "recipe type may name station/category context",
        semanticIndex,
      });
      break;
    }
    case "recipe_id_family": {
      // Recipe id families are useful supporting evidence, but in large
      // packs they are usually product/material families (`shaped/bolt`,
      // `crafting/oak`, `temp/small`) rather than player-facing workflows.
      // Promoting them to workflow candidates swamps recipe types and guide
      // topics, which are better vocabulary sources.
      break;
    }
    case "guide_page": {
      addDocumentCandidates(acc, record, packId, facets, "namespace", "guide page title", semanticIndex);
      addOrganizationGroupEvidenceCandidates(acc, record, packId, facets, semanticIndex);
      addModSubsystemDocumentCandidate(acc, record, packId, facets, semanticIndex);
      break;
    }
    case "quest_node": {
      addDocumentCandidates(acc, record, packId, facets, "pack", "quest title", semanticIndex);
      addOrganizationGroupEvidenceCandidates(acc, record, packId, facets, semanticIndex);
      break;
    }
    case "advancement": {
      addDocumentCandidates(acc, record, packId, facets, "namespace", "advancement title", semanticIndex, {
        requireWorkflowSignal: true,
      });
      addModSubsystemDocumentCandidate(acc, record, packId, facets, semanticIndex);
      break;
    }
    case "stack_group": {
      addStackGroupCandidate(acc, record, packId, facets, semanticIndex);
      break;
    }
    case "item_tag":
    case "block_tag": {
      addTagDomainCandidates(acc, record, facets, semanticIndex);
      addOrganizationGroupEvidenceCandidates(acc, record, packId, facets, semanticIndex);
      addModSubsystemTagCandidates(acc, record, packId, facets, semanticIndex);
      break;
    }
    case "existing_vocab":
    case "kubejs_tooltip":
    case "recipe_role_summary":
      break;
    case "mod_metadata": {
      addModSubsystemDocumentCandidate(acc, record, packId, facets, semanticIndex);
      break;
    }
    case "runtime_item": {
      addOrganizationGroupEvidenceCandidates(acc, record, packId, facets, semanticIndex);
      addModSubsystemRuntimeItemCandidates(acc, record, packId, facets, semanticIndex);
      break;
    }
  }
}

function addDocumentCandidates(
  acc: Map<string, CandidateAccumulator>,
  record: FacetEvidenceRecord,
  packId: string,
  facets: ReadonlySet<VocabularyFacetId>,
  scope: "namespace" | "pack",
  reason: string,
  semanticIndex: SemanticEvidenceIndex,
  options: { requireWorkflowSignal?: boolean } = {},
): void {
  const label = record.label ?? record.title ?? labelFromId(record.id);
  const token = tokenPath(label);
  if (!token || GENERIC_TOKENS.has(token)) return;
  const namespace = scope === "namespace" ? record.namespace : undefined;
  const id = scope === "pack" || !namespace ? `pack:${packId}/${token}` : `${namespace}:${token}`;
  const count = record.count ?? 1;
  if (!options.requireWorkflowSignal || documentLooksLikeWorkflowOrUseContext(record, label)) {
    addEvidenceCandidate(acc, {
      facet: "workflow",
      id,
      record,
      count,
      facets,
      state: "review",
      reason,
      semanticIndex,
    });
    addEvidenceCandidate(acc, {
      facet: "used_at",
      id,
      record,
      count,
      facets,
      state: "review",
      reason: `${reason} may name a station, tool, surface, or interaction context`,
      semanticIndex,
    });
  }
  if (looksLikeProgressionStage(record, label)) {
    addEvidenceCandidate(acc, {
      facet: "progression_stage",
      id,
      record,
      count,
      facets,
      state: "review",
      reason: `${reason} names a likely progression gate`,
      semanticIndex,
    });
    for (const canonicalId of canonicalProgressionIds(record, packId, scope)) {
      if (canonicalId === id) continue;
      addEvidenceCandidate(acc, {
        facet: "progression_stage",
        id: canonicalId,
        record,
        count,
        facets,
        state: "review",
        reason: `${reason} resource path names a canonical progression gate`,
        semanticIndex,
      });
    }
  }
}

function addTagDomainCandidates(
  acc: Map<string, CandidateAccumulator>,
  record: FacetEvidenceRecord,
  facets: ReadonlySet<VocabularyFacetId>,
  semanticIndex: SemanticEvidenceIndex,
): void {
  const tokens = tokenSet(record.id);
  for (const [facet, defaults] of Object.entries(UNIVERSAL_DEFAULTS)) {
    if (!isVocabularyFacet(facet) || !facets.has(facet)) continue;
    for (const value of defaults) {
      const valueToken = value.id.slice("slot:".length);
      if (!tokens.has(valueToken)) continue;
      addCandidate(acc, {
        facet,
        id: value.id,
        label: value.label,
        origin: "universal_default",
        suggestedState: "accepted",
        confidence: Math.max(0.8, record.confidence),
        support: record.count ?? 1,
        evidence: [evidenceRef(record)],
        semanticEvidence: semanticEvidenceForCandidate(record, semanticIndex),
        seedItems: runtimeItemRefs(record.item_refs ?? record.examples, semanticIndex),
        reason: `${record.kind} token matches universal ${facet} value`,
      });
    }
  }
}

function addStackGroupCandidate(
  acc: Map<string, CandidateAccumulator>,
  record: FacetEvidenceRecord,
  packId: string,
  facets: ReadonlySet<VocabularyFacetId>,
  semanticIndex: SemanticEvidenceIndex,
): void {
  if (!facets.has("organization_group")) return;
  const id = normalizeScopedResourceId(record.id, packId, "namespace");
  if (!id || isGenericValueId(id)) return;
  addCandidate(acc, {
    facet: "organization_group",
    id,
    label: record.label ?? labelFromId(id),
    origin: "pack_generated",
    suggestedState: "review",
    confidence: Math.max(0.55, Math.min(0.8, record.confidence)),
    support: Math.max(1, record.count ?? record.item_refs?.length ?? record.tags?.length ?? 1),
    evidence: [evidenceRef(record)],
    semanticEvidence: semanticEvidenceForCandidate(record, semanticIndex),
    seedItems: runtimeItemRefs(record.item_refs ?? record.examples, semanticIndex),
    reason: "pack stack group names an organization group",
  });
}

function addModSubsystemDocumentCandidate(
  acc: Map<string, CandidateAccumulator>,
  record: FacetEvidenceRecord,
  packId: string,
  facets: ReadonlySet<VocabularyFacetId>,
  semanticIndex: SemanticEvidenceIndex,
): void {
  if (!facets.has("mod_subsystem") || !isModSubsystemNamespace(record.namespace, packId)) return;
  const label = record.label ?? record.title ?? "";
  const tokens = modSubsystemSignalTokens([
    record.id,
    label,
    ...(record.kind === "mod_metadata" ? [record.description ?? ""] : []),
  ]);
  for (const value of tokens) {
    const id = modSubsystemId(record.namespace, value);
    if (!id || isGenericValueId(id) || modSubsystemIdLooksRejected(id)) continue;
    addModSubsystemCandidate(acc, {
      id,
      record,
      facets,
      count: record.count ?? record.item_refs?.length ?? 1,
      state: "review",
      confidence: Math.max(0.45, Math.min(0.75, record.confidence)),
      semanticIndex,
      reason: `${record.kind} may name a mod-owned subsystem`,
    });
  }
}

function addModSubsystemRuntimeItemCandidates(
  acc: Map<string, CandidateAccumulator>,
  record: FacetEvidenceRecord,
  packId: string,
  facets: ReadonlySet<VocabularyFacetId>,
  semanticIndex: SemanticEvidenceIndex,
): void {
  if (!facets.has("mod_subsystem") || !isModSubsystemNamespace(record.namespace, packId)) return;
  const tokens = modSubsystemSignalTokens([
    record.id,
    record.label ?? "",
    ...ownedNamespaceValues(record.namespace, record.tags),
    ...ownedNamespaceValues(record.namespace, record.direct_tags),
    ...ownedNamespaceValues(record.namespace, record.model_parents),
    ...ownedNamespaceValues(record.namespace, record.creative_tabs),
  ]);
  for (const value of tokens) {
    const id = modSubsystemId(record.namespace, value);
    if (!id) continue;
    addModSubsystemCandidate(acc, {
      id,
      record,
      facets,
      count: 1,
      state: "review",
      confidence: 0.55,
      semanticIndex,
      reason: "runtime item id/tag/model token suggests subsystem identity",
    });
  }
}

function addModSubsystemTagCandidates(
  acc: Map<string, CandidateAccumulator>,
  record: FacetEvidenceRecord,
  packId: string,
  facets: ReadonlySet<VocabularyFacetId>,
  semanticIndex: SemanticEvidenceIndex,
): void {
  if (!facets.has("mod_subsystem")) return;
  if (!isModSubsystemNamespace(record.namespace, packId)) return;
  const tokens = modSubsystemSignalTokens([record.id, record.label ?? ""]);
  if (tokens.length === 0) return;
  const count = Math.max(1, record.count ?? record.item_refs?.length ?? record.examples?.length ?? 1);
  for (const value of tokens) {
    const id = modSubsystemId(record.namespace, value);
    if (!id) continue;
    addModSubsystemCandidate(acc, {
      id,
      record,
      facets,
      count,
      state: "review",
      confidence: Math.max(0.5, Math.min(0.8, record.confidence)),
      semanticIndex,
      reason: `${record.kind} is a mod-owned subsystem-like tag`,
    });
  }
}

function addModSubsystemCandidate(
  acc: Map<string, CandidateAccumulator>,
  args: {
    id: string;
    record: FacetEvidenceRecord;
    facets: ReadonlySet<VocabularyFacetId>;
    count: number;
    state: VocabularyState;
    confidence: number;
    semanticIndex: SemanticEvidenceIndex;
    reason: string;
  },
): void {
  if (!args.facets.has("mod_subsystem")) return;
  if (isGenericValueId(args.id) || modSubsystemIdLooksRejected(args.id)) return;
  addCandidate(acc, {
    facet: "mod_subsystem",
    id: args.id,
    label: labelFromId(args.id),
    origin: "namespace_generated",
    suggestedState: args.state,
    confidence: args.confidence,
    support: args.count,
    evidence: [evidenceRef(args.record)],
    semanticEvidence: semanticEvidenceForCandidate(args.record, args.semanticIndex),
    seedItems: runtimeItemRefs(args.record.item_refs ?? args.record.examples, args.semanticIndex),
    aliases: args.record.examples?.filter((value) => !looksLikeResourceLocation(value)),
    reason: args.reason,
  });
}

function addWorkflowRoleCandidates(
  acc: Map<string, CandidateAccumulator>,
  minEvidence: number,
): void {
  const workflows = [...acc.values()].filter((candidate) =>
    candidate.facet === "workflow" && candidate.support >= minEvidence && candidate.suggested_state !== "rejected"
  );
  for (const workflow of workflows) {
    for (const role of ROLE_TOKENS) {
      addCandidate(acc, {
        facet: "workflow_role",
        id: `${workflow.id}#${role}`,
        label: `${workflow.label} ${labelFromId(role)}`,
        origin: workflow.origin === "universal_default" ? "universal_default" : "pack_generated",
        suggestedState: workflow.suggested_state === "accepted" ? "accepted" : "review",
        confidence: Math.min(0.85, workflow.confidence),
        support: workflow.support,
        evidence: workflow.evidence,
        semanticEvidence: workflow.semantic_evidence,
        seedItems: workflow.seed_items,
        parent: workflow.id,
        reason: `derived ${role} role from workflow candidate`,
      });
    }
  }
}

function addOrganizationGroupEvidenceCandidates(
  acc: Map<string, CandidateAccumulator>,
  record: FacetEvidenceRecord,
  packId: string,
  facets: ReadonlySet<VocabularyFacetId>,
  semanticIndex: SemanticEvidenceIndex,
): void {
  if (!facets.has("organization_group")) return;
  const seeds = organizationGroupSeedsForRecord(record, packId);
  if (seeds.length === 0) return;
  const support = Math.max(1, record.count ?? record.item_refs?.length ?? record.examples?.length ?? 1);
  for (const seed of seeds) {
    addCandidate(acc, {
      facet: "organization_group",
      id: seed.id,
      label: seed.label,
      description: seed.description,
      origin: seed.id.startsWith("pack:") ? "pack_generated" : "namespace_generated",
      suggestedState: "review",
      confidence: Math.max(seed.confidence, Math.min(0.78, record.confidence)),
      support,
      evidence: [evidenceRef(record)],
      semanticEvidence: semanticEvidenceForCandidate(record, semanticIndex),
      seedItems: runtimeItemRefs(record.item_refs ?? record.examples, semanticIndex),
      aliases: seed.aliases,
      reason: seed.reason,
    });
  }
}

interface OrganizationGroupSeed {
  id: string;
  label: string;
  confidence: number;
  reason: string;
  description?: string;
  aliases?: readonly string[];
}

function organizationGroupSeedsForRecord(
  record: FacetEvidenceRecord,
  packId: string,
): OrganizationGroupSeed[] {
  const fields = organizationGroupSignalFields(record);
  if (fields.length === 0) return [];
  const tokens = tokenSet(fields.join(" "));
  const identityTokens = tokenSet(organizationGroupIdentityFields(record).join(" "));
  const haystack = fields.join(" ").toLowerCase();
  const out = new Map<string, OrganizationGroupSeed>();
  const addPack = (
    value: string,
    label: string,
    description: string,
    reason: string,
    confidence = 0.55,
    aliases?: readonly string[],
  ) => {
    addOrganizationGroupSeed(out, {
      id: packOrganizationGroupId(packId, value),
      label,
      description,
      confidence,
      aliases,
      reason,
    });
  };
  const addScoped = (
    namespace: string,
    value: string,
    label: string,
    description: string,
    reason: string,
    confidence = 0.55,
    aliases?: readonly string[],
  ) => {
    addOrganizationGroupSeed(out, {
      id: `${namespace}:${token(value)}`,
      label,
      description,
      confidence,
      aliases,
      reason,
    });
  };

  const broad = broadOrganizationGroupSeed(record, packId);
  if (broad) addOrganizationGroupSeed(out, broad);

  if (
    hasAnyToken(tokens, ["ore", "ores", "raw_ore"]) &&
    !hasAnyToken(tokens, ["crushed", "purified", "dust", "dusts", "ingot", "ingots", "nugget", "nuggets", "plate", "plates", "rod", "rods", "wire", "wires"])
  ) {
    addPack("unprocessed_ores", "Unprocessed Ores", "Ore blocks, raw ores, and mine output awaiting processing.", "ore/raw ore evidence suggests a player storage group", 0.62, ["raw ores"]);
  }
  if (hasAnyToken(tokens, ["crushed", "crushed_ore", "purified", "purified_ore", "dust", "dusts", "ingot", "ingots", "nugget", "nuggets", "plate", "plates", "rod", "rods", "bolt", "bolts", "wire", "wires"])) {
    addPack("refined_ores", "Refined Ores", "Processed mineral outputs such as crushed ores, dusts, ingots, plates, rods, and wires.", "processed ore/material evidence suggests a player storage group", 0.6, ["processed ores", "refined metals"]);
  }
  if (hasAnyToken(tokens, ["seed", "seeds"])) {
    addPack("seeds", "Seeds", "Seeds and seed-like planting starts.", "seed evidence suggests a player storage group", 0.62);
  }
  if (hasAnyToken(tokens, ["crop", "crops", "grain", "grains", "vegetable", "vegetables"]) || /\b(cabbage|maize|oat|potato|rye|tomato|wheat)\b/.test(haystack)) {
    addPack("crops", "Crops", "Harvested crops and field produce.", "crop evidence suggests a player storage group", 0.58);
  }
  if (hasAnyToken(tokens, ["plant", "plants", "flower", "flowers", "flora", "sapling", "saplings", "leaf", "leaves"]) && !hasAnyToken(tokens, ["seed", "seeds", "crop", "crops", "food", "foods"])) {
    addPack("inedible_plants", "Inedible Plants", "Plants, flowers, leaves, and other non-food botanical items.", "plant/flora evidence suggests a player storage group", 0.55, ["plants"]);
  }
  if (hasAnyToken(tokens, ["dirt", "rock", "rocks", "stone", "stones", "cobble", "cobblestone", "gravel", "sand", "silt", "clay", "mud", "soil"])) {
    addPack("dirt_and_rocks", "Dirt and Rocks", "Terrain rubble such as dirt, stone, gravel, sand, clay, and loose rocks.", "terrain material evidence suggests a player storage group", 0.58, ["rubble"]);
  }
  if (hasAnyToken(tokens, ["decorative", "decoration", "decorations", "decor", "ornament", "ornaments", "furniture", "framed", "frame", "lamp", "lantern"])) {
    addPack("decorative", "Decorative", "Blocks and items primarily kept for decoration or building detail.", "decorative evidence suggests a player storage group", 0.58, ["decorations"]);
  }
  if (hasAnyToken(tokens, ["wood", "woods", "wooden", "log", "logs", "plank", "planks", "lumber", "board", "boards", "beam", "beams", "sawdust", "carpentry", "saw"])) {
    addPack("woodworking", "Woodworking", "Wood, lumber, planks, carpentry supplies, and wood-working outputs.", "woodworking evidence suggests a player storage group", 0.58);
  }
  if (hasAnyToken(tokens, ["animal", "animals", "husbandry", "hide", "hides", "leather", "wool", "milk", "egg", "eggs", "feed", "livestock", "cow", "cows", "sheep", "pig", "pigs", "chicken", "chickens"])) {
    addPack("animal_husbandry", "Animal Husbandry", "Animal products, livestock supplies, feed, hides, wool, milk, and eggs.", "animal/livestock evidence suggests a player storage group", 0.58);
  }
  if (hasAnyToken(tokens, ["weaving", "cloth", "fabric", "textile", "textiles", "thread", "threads", "string", "strings", "yarn", "loom", "sewing", "needle"])) {
    addPack("weaving_cloth", "Weaving and Cloth", "Cloth, fabric, thread, string, weaving, and sewing supplies.", "cloth/weaving evidence suggests a player storage group", 0.6, ["cloth", "textiles"]);
  }
  if (record.kind === "runtime_item" && hasAnyToken(identityTokens, ["pot", "pan", "skillet", "bowl", "bowls", "oven", "grill", "kitchen", "cooking", "cookware"]) && !hasAnyToken(identityTokens, ["food", "foods", "crop", "crops", "seed", "seeds"])) {
    addPack("cooking_tools", "Cooking Tools", "Reusable utensils, cookware, bowls, pots, knives, and food-prep tools.", "runtime item names a cooking tool or utensil", 0.57, ["cookware"]);
  }
  if (record.namespace === "create") {
    addPack("create_items", "Create Items", "Create mod blocks, components, and logistics/kinetics items a player may keep together.", "Create namespace evidence suggests a player storage group", 0.54);
  }

  if (record.kind === "item_tag" || record.kind === "block_tag" || record.kind === "runtime_item") {
    for (const tier of VOLTAGE_TIERS) {
      if (!tokens.has(tier)) continue;
      if (!hasAnyToken(tokens, VOLTAGE_COMPONENT_TOKENS)) continue;
      const label = `${tier.toUpperCase()} Components`;
      addScoped("gtceu", `${tier}_components`, label, `${tier.toUpperCase()} GregTech electrical components and machine parts.`, "voltage-tier component evidence suggests a player storage group", 0.6);
    }
  }

  return [...out.values()]
    .filter((seed) => !organizationGroupIdLooksLikeStation(seed.id))
    .sort((a, b) => a.id.localeCompare(b.id));
}

function broadOrganizationGroupSeed(
  record: FacetEvidenceRecord,
  packId: string,
): OrganizationGroupSeed | null {
  if (
    record.kind !== "item_tag" &&
    record.kind !== "guide_page" &&
    record.kind !== "quest_node" &&
    record.kind !== "stack_group"
  ) {
    return null;
  }
  const label = record.label ?? record.title ?? labelFromId(record.id);
  const candidateToken = organizationGroupBroadToken(record, label);
  if (!candidateToken) return null;
  const id = packOrganizationGroupId(packId, candidateToken);
  if (isGenericValueId(id) || organizationGroupIdLooksLikeStation(id)) return null;
  return {
    id,
    label,
    confidence: Math.max(0.42, Math.min(0.68, record.confidence)),
    reason: `${record.kind} label/tag may name a player organization group`,
    description: "Candidate player storage section inferred from pack evidence; accept only if it is a useful way to group items.",
  };
}

function organizationGroupBroadToken(record: FacetEvidenceRecord, label: string): string | null {
  if (organizationGroupRecordLooksTechnical(record, label)) return null;
  const labelToken = tokenPath(label);
  if (labelToken && !organizationGroupTokenLooksTechnical(labelToken)) return labelToken;
  const split = splitResourceLocation(record.id);
  const raw = split?.path ?? record.id;
  const parts = raw.split(/[\/_.-]+/).map(token).filter(Boolean);
  while (parts.length > 0 && ORGANIZATION_GROUP_PREFIX_STOP_TOKENS.has(parts[0]!)) parts.shift();
  while (parts.length > 0 && ORGANIZATION_GROUP_SUFFIX_STOP_TOKENS.has(parts[parts.length - 1]!)) parts.pop();
  const pathToken = parts.join("/");
  if (!pathToken || organizationGroupTokenLooksTechnical(pathToken)) return null;
  return pathToken;
}

function organizationGroupRecordLooksTechnical(record: FacetEvidenceRecord, label: string): boolean {
  const split = splitResourceLocation(record.id);
  const path = tokenPath(split?.path ?? record.id)?.replace(/\//g, "_") ?? "";
  const labelToken = tokenPath(label)?.replace(/\//g, "_") ?? "";
  const haystack = `${path} ${labelToken}`;
  if (/(^|_)(mineable|needs|incorrect|destroyed|replaceable|replacements|whitelisted|blacklisted|prevented|allowed|completes)(_|$)/.test(path)) {
    return true;
  }
  if (/(^|_)(can|cannot|must|valid|invalid|requires?|trigger|start|stops?|deals|damages?|allows?|disallows?)(_|$)/.test(path)) {
    return true;
  }
  if (/(^|_)(spawnable|spawns?|holdable|harvestable|slowed|speed_booster|pickup|tutorial|whitelist|blacklist)(_|$)/.test(haystack)) {
    return true;
  }
  if (/(^|_)(collapse|landslide|powderkeg|enderman|monster|mob|entity|pathfind|wrench_pickup|prospectable|plantable_on|scraping_surface)(_|$)/.test(haystack)) {
    return true;
  }
  if (/(^|_)(usable_on|usable_in|breaks?|ignore|deny|transparent|index|p2p)(_|$)/.test(haystack)) return true;
  if (record.kind === "block_tag" && /(^|_)(pickaxe|axe|shovel|hoe|sickle|paxel|hammer|tool)(_or_|_|$)/.test(path)) {
    return true;
  }
  return false;
}

const ORGANIZATION_GROUP_PREFIX_STOP_TOKENS = new Set([
  "block",
  "blocks",
  "item",
  "items",
  "tag",
  "tags",
  "whitelisted",
  "blacklisted",
  "mineable",
  "needs",
  "incorrect",
  "replaceable",
  "replacements",
  "runtime",
  "generated",
  "compat",
  "completes",
]);

const ORGANIZATION_GROUP_SUFFIX_STOP_TOKENS = new Set([
  "replaceable",
  "replaceables",
  "whitelisted",
  "blacklisted",
  "pickup",
  "tutorial",
]);

function organizationGroupTokenLooksTechnical(value: string): boolean {
  const normalized = value.replace(/\//g, "_");
  if (GENERIC_TOKENS.has(normalized) || ORGANIZATION_GROUP_STOP_TOKENS.has(normalized)) return true;
  if (/^(destroyed|mineable|needs|incorrect|whitelisted|blacklisted|replaceable|wrench|runtime|generated|prevented|allowed|completes)(_|\/|$)/.test(normalized)) return true;
  if (/(^|_)(recipes?|recipe|advancement|root|story|crafting|shaped|shapeless|misc|internal|placeholder)(_|$)/.test(normalized)) return true;
  if (/(^|_)(can|cannot|must|valid|invalid|requires?|trigger|start|stops?|deals|damages?|allows?|disallows?)(_|$)/.test(normalized)) return true;
  if (/(^|_)(spawnable|spawns?|holdable|harvestable|slowed|speed_booster|pickup|tutorial|whitelist|blacklist)(_|$)/.test(normalized)) return true;
  if (/(^|_)(collapse|landslide|powderkeg|enderman|monster|mob|entity|pathfind|wrench_pickup|prospectable|plantable_on|scraping_surface)(_|$)/.test(normalized)) return true;
  if (/(^|_)(usable_on|usable_in|breaks?|ignore|deny|transparent|index|p2p)(_|$)/.test(normalized)) return true;
  return false;
}

function addOrganizationGroupSeed(out: Map<string, OrganizationGroupSeed>, seed: OrganizationGroupSeed): void {
  const issue = validateMultiValue("organization_group", [seed.id]);
  if (issue) return;
  const existing = out.get(seed.id);
  if (!existing || seed.confidence > existing.confidence) out.set(seed.id, seed);
}

function organizationGroupSignalFields(record: FacetEvidenceRecord): string[] {
  return [
    record.id,
    record.label,
    record.title,
    record.description,
    record.namespace,
    ...(record.tags ?? []),
    ...(record.direct_tags ?? []),
    ...(record.creative_tabs ?? []),
    ...(record.model_parents ?? []),
    ...(record.semantic_text ?? []).slice(0, 16).map((entry) => entry.text),
  ].filter((value): value is string => typeof value === "string" && value.trim().length > 0);
}

function organizationGroupIdentityFields(record: FacetEvidenceRecord): string[] {
  return [
    record.id,
    record.label,
    record.title,
    record.namespace,
    ...(record.direct_tags ?? []),
    ...(record.creative_tabs ?? []),
    ...(record.model_parents ?? []),
  ].filter((value): value is string => typeof value === "string" && value.trim().length > 0);
}

function packOrganizationGroupId(packId: string, value: string): string {
  return `pack:${packId}/${token(value)}`;
}

function hasAnyToken(tokens: ReadonlySet<string>, values: Iterable<string>): boolean {
  for (const value of values) {
    if (tokens.has(value)) return true;
  }
  return false;
}

function organizationGroupIdLooksLikeStation(id: string): boolean {
  const tail = resourcePathTail(id);
  return !!tail && ORGANIZATION_GROUP_STOP_TOKENS.has(tail);
}

function addEvidenceCandidate(
  acc: Map<string, CandidateAccumulator>,
  args: {
    facet: VocabularyFacetId;
    id: string;
    record: FacetEvidenceRecord;
    count: number;
    facets: ReadonlySet<VocabularyFacetId>;
    state: VocabularyState;
    reason: string;
    semanticIndex?: SemanticEvidenceIndex;
  },
): void {
  if (!args.facets.has(args.facet)) return;
  addCandidate(acc, {
    facet: args.facet,
    id: args.id,
    label: args.record.label ?? labelFromId(args.id),
    origin: args.id.startsWith("pack:") ? "pack_generated" : "namespace_generated",
    suggestedState: args.state,
    confidence: Math.max(0.4, Math.min(0.9, args.record.confidence)),
    support: args.count,
    evidence: [evidenceRef(args.record)],
    semanticEvidence: semanticEvidenceForCandidate(args.record, args.semanticIndex),
    seedItems: runtimeItemRefs(args.record.item_refs ?? args.record.examples, args.semanticIndex),
    aliases: args.record.examples?.filter((value) => !looksLikeResourceLocation(value)),
    reason: args.reason,
  });
}

function buildSemanticEvidenceIndex(records: readonly FacetEvidenceRecord[]): SemanticEvidenceIndex {
  const runtimeTooltipBoilerplate = collectRuntimeTooltipBoilerplate(records);
  const runtimeItemIds = new Set(records
    .filter((record) => record.kind === "runtime_item")
    .map((record) => record.id));
  const index: SemanticEvidenceIndex = {
    byItem: new Map(),
    byRuntimeItem: new Map(),
    byRecipeType: new Map(),
    byNamespace: new Map(),
    runtimeItemIds,
    runtimeTooltipBoilerplate,
  };
  for (const record of records) {
    const semantic = semanticEvidenceFromRecord(record, index);
    if (semantic.length === 0) continue;
    for (const item of record.item_refs ?? []) {
      pushSemantic(index.byItem, item, semantic);
      if (record.kind === "runtime_item") {
        pushSemantic(index.byRuntimeItem, item, semantic);
      }
    }
    if (record.recipe_type && record.kind !== "recipe_role_summary") {
      pushSemantic(index.byRecipeType, record.recipe_type, semantic);
    }
    for (const recipe of record.recipe_refs ?? []) {
      const recipeType = recipeTypeFromRecipeRef(recipe);
      if (recipeType && semanticEvidenceMatchesRecipeType(record, recipeType)) {
        pushSemantic(index.byRecipeType, recipeType, semantic);
      }
    }
    if (record.namespace) {
      pushSemantic(index.byNamespace, record.namespace, semantic);
    }
  }
  return index;
}

function semanticEvidenceForCandidate(
  record: FacetEvidenceRecord,
  index: SemanticEvidenceIndex | undefined,
): VocabularySemanticEvidence[] {
  const out: VocabularySemanticEvidence[] = [...semanticEvidenceFromRecord(record, index)];
  if (index) {
    const recipeType = record.recipe_type ?? (record.kind === "recipe_type" ? record.id : undefined);
    if (recipeType) {
      out.push(...(index.byRecipeType.get(recipeType) ?? []));
    }
    if (canUseRuntimeItemSemanticJoin(record)) {
      for (const item of record.item_refs ?? []) {
        const runtimeEvidence = index.byRuntimeItem.get(item) ?? [];
        out.push(...runtimeEvidence.filter((evidence) =>
          recipeType ? semanticEvidenceEntryMatchesRecipeType(evidence, recipeType) : true
        ));
      }
    } else if (canUseItemSemanticJoin(record)) {
      for (const item of record.item_refs ?? []) {
        out.push(...(index.byItem.get(item) ?? []));
      }
    }
  }
  return limitedSemanticEvidence(out);
}

function semanticEvidenceFromRecord(
  record: FacetEvidenceRecord,
  index?: Pick<SemanticEvidenceIndex, "runtimeTooltipBoilerplate">,
): VocabularySemanticEvidence[] {
  if (record.kind === "recipe_role_summary") return [];
  const out: VocabularySemanticEvidence[] = [];
  for (const entry of record.semantic_text ?? []) {
    const text = clipSemanticPromptText(entry.text.trim());
    if (!text) continue;
    if (isPromptBoilerplateSemanticText(entry.source, text, index?.runtimeTooltipBoilerplate)) continue;
    out.push({
      kind: record.kind,
      id: record.id,
      source: record.source,
      text,
      key: [entry.source, entry.key].filter(Boolean).join(":") || undefined,
      ...(record.label ? { label: record.label } : {}),
      ...(record.item_refs?.length ? { item_ref_count: record.item_refs.length } : {}),
      ...(record.recipe_refs?.length ? { recipe_ref_count: record.recipe_refs.length } : {}),
      ...(record.role ? { role: record.role } : {}),
      ...(record.recipe_type ? { recipe_type: record.recipe_type } : {}),
      ...(record.count !== undefined ? { count: record.count } : {}),
    });
  }
  if (out.length === 0 && record.description) {
    out.push({
      kind: record.kind,
      id: record.id,
      source: record.source,
      text: clipSemanticPromptText(record.description),
      key: "description",
      ...(record.label ? { label: record.label } : {}),
      ...(record.count !== undefined ? { count: record.count } : {}),
    });
  }
  return limitedSemanticEvidence(out);
}

function clipSemanticPromptText(value: string): string {
  const normalized = value.replace(/\s+/g, " ").trim();
  return normalized.length <= SEMANTIC_TEXT_PROMPT_LIMIT
    ? normalized
    : `${normalized.slice(0, SEMANTIC_TEXT_PROMPT_LIMIT - 3)}...`;
}

function promptSemanticEvidence(evidence: VocabularySemanticEvidence): VocabularySemanticEvidence {
  return {
    kind: evidence.kind,
    id: evidence.id,
    source: promptEvidenceSource(evidence.source),
    ...(evidence.text ? { text: evidence.text } : {}),
    ...(evidence.key ? { key: evidence.key } : {}),
    ...(evidence.label ? { label: evidence.label } : {}),
    ...(evidence.item_ref_count !== undefined ? { item_ref_count: evidence.item_ref_count } : {}),
    ...(evidence.recipe_ref_count !== undefined ? { recipe_ref_count: evidence.recipe_ref_count } : {}),
    ...(evidence.role ? { role: evidence.role } : {}),
    ...(evidence.recipe_type ? { recipe_type: evidence.recipe_type } : {}),
    ...(evidence.count !== undefined ? { count: evidence.count } : {}),
  };
}

function promptEvidenceSource(source: string): string {
  if (source.startsWith("jar:")) {
    const jarSource = source.slice("jar:".length);
    const bang = jarSource.indexOf("!");
    if (bang >= 0) {
      return `jar:${lastPathSegment(jarSource.slice(0, bang))}!${jarSource.slice(bang + 1)}`;
    }
    return `jar:${lastPathSegment(jarSource)}`;
  }
  if (source.startsWith("file:")) {
    const fileSource = source.slice("file:".length).replace(/\\/g, "/");
    const marker = "/minecraft/";
    const markerIndex = fileSource.lastIndexOf(marker);
    if (markerIndex >= 0) {
      return `file:minecraft/${fileSource.slice(markerIndex + marker.length)}`;
    }
    return `file:${tailPath(fileSource, 4)}`;
  }
  return source.length <= 200 ? source : `.../${tailPath(source.replace(/\\/g, "/"), 4)}`;
}

function lastPathSegment(path: string): string {
  return path.replace(/\\/g, "/").split("/").filter(Boolean).at(-1) ?? path;
}

function tailPath(path: string, segments: number): string {
  const parts = path.split("/").filter(Boolean);
  return parts.slice(-segments).join("/");
}

function runtimeItemRefs(
  values: readonly string[] | undefined,
  index: Pick<SemanticEvidenceIndex, "runtimeItemIds"> | undefined,
): string[] {
  if (!values?.length) return [];
  const out: string[] = [];
  for (const value of values) {
    if (!looksLikePromptItemId(value)) continue;
    if (index && !index.runtimeItemIds.has(value)) continue;
    out.push(value);
  }
  return sortedLimited(out, CANDIDATE_EXAMPLE_LIMIT);
}

function looksLikePromptItemId(value: string): boolean {
  if (!looksLikeResourceLocation(value)) return false;
  if (value.startsWith("patchouli:")) return false;
  if (value.includes("/textures/") || value.endsWith(".png") || value.endsWith(".json")) return false;
  return true;
}

function collectRuntimeTooltipBoilerplate(records: readonly FacetEvidenceRecord[]): Set<string> {
  const out = new Set<string>([
    "supported by:",
    "can be placed vertically",
    "allows mixed vertical-horizontal connections (relative to the placement)",
  ]);
  const counts = new Map<string, number>();

  for (const record of records) {
    if (record.kind === "mod_metadata") {
      if (record.label) out.add(normalizeBoilerplateText(record.label));
      if (record.id) out.add(normalizeBoilerplateText(labelFromId(record.id)));
      continue;
    }
    if (record.kind !== "runtime_item") continue;
    for (const entry of record.semantic_text ?? []) {
      if (!entry.source.startsWith("runtime-tooltip")) continue;
      const normalized = normalizeBoilerplateText(entry.text);
      if (!normalized) continue;
      counts.set(normalized, (counts.get(normalized) ?? 0) + 1);
    }
  }

  for (const [text, count] of counts) {
    if (count < RUNTIME_TOOLTIP_REPEAT_BOILERPLATE_THRESHOLD) continue;
    if (isRepeatedRuntimeTooltipBoilerplate(text)) out.add(text);
  }
  return out;
}

function isPromptBoilerplateSemanticText(
  source: string,
  text: string,
  runtimeTooltipBoilerplate: ReadonlySet<string> | undefined,
): boolean {
  if (!source.startsWith("runtime-tooltip")) return false;
  const normalized = normalizeBoilerplateText(text);
  if (!normalized) return true;
  if (runtimeTooltipBoilerplate?.has(normalized)) return true;
  if (/^⚖\s*.+\s+⇲\s+.+$/u.test(normalized)) return true;
  if (/^hold\s+(shift|\(shift\)|\[w\]|ctrl|alt)\b.*\b(info|information|details|ponder)\b/i.test(normalized)) return true;
  return false;
}

function isRepeatedRuntimeTooltipBoilerplate(text: string): boolean {
  if (/^⚖\s*.+\s+⇲\s+.+$/u.test(text)) return true;
  if (/^hold\s+/i.test(text)) return true;
  if (text.length <= 48 && /^[\p{L}\p{N} '&:+().,-]+$/u.test(text)) return true;
  return false;
}

function normalizeBoilerplateText(text: string): string {
  return text
    .replace(/§[0-9A-FK-OR]/gi, "")
    .replace(/\s+/g, " ")
    .trim()
    .toLowerCase();
}

function looksLikeProgressionStage(record: FacetEvidenceRecord, label: string): boolean {
  const semantic = (record.semantic_text ?? [])
    .slice(0, 8)
    .map((entry) => entry.text)
    .join(" ");
  const semanticHaystack = `${label} ${semantic}`.toLowerCase();
  const idPath = splitResourceLocation(record.id)?.path.toLowerCase() ?? record.id.toLowerCase();
  const idTail = idPath.split(/[/.]/).filter(Boolean).at(-1) ?? "";
  const labelToken = tokenPath(label) ?? "";
  const isAdvancement = record.kind === "advancement";

  if (/\b(index|tips?|lists?|recipes?|animals?|mobs?|fruits?|crops?|flora|biomes?|damage\s+types?|ores?\s+and\s+minerals|wild\s+animals|wild\s+fruits)\b/.test(semanticHaystack)) {
    return false;
  }
  if (/\b(boots?|horseshoes?|trophies?|minecarts?|boats?|sloops?|cannons?|blocks?|logs?|lumber|chests?|doors?|fences?|fence\s+gates?|bookshelves?|trapdoors?|buttons?|pressure\s+plates?)\b/.test(semanticHaystack)) {
    return false;
  }
  if (isAdvancement && /\b(crafting|recipes?)\b/.test(idPath) && !/\b(rocket|moon|mars|venus|mercury|steel|bronze|anvil|bloomery|blast_furnace|crucible)\b/.test(`${idPath} ${semanticHaystack}`)) {
    return false;
  }

  if (/\b(age|tier|voltage|primitive|steam|electric|lv|mv|hv|ev|iv|luv|zpm|uv|uhv)\b/.test(semanticHaystack)) {
    return true;
  }
  if (/\b(moon|mars|venus|mercury|space|rocket|launch|orbit|proxima|beneath|nether)\b/.test(semanticHaystack)) {
    return true;
  }
  if (/\b(blast\s+furnace|bloomery|mechanical\s+power|primitive\s+alloys?|primitive\s+anvils?|hellforge|ancient\s+altars?|crucible|making\s+steel)\b/.test(semanticHaystack)) {
    return true;
  }
  if (/\b(steel|black\s+steel|red\s+steel|blue\s+steel|wrought\s+iron|bronze|bismuth\s+bronze|black\s+bronze)\b/.test(semanticHaystack)) {
    return true;
  }
  return /\b(tier_[0-9]+_rocket|rocket|moon|mars|venus|mercury|steel|bronze)\b/.test(idTail) ||
    /\b(tier_[0-9]+_rocket)\b/.test(labelToken);
}

function documentLooksLikeWorkflowOrUseContext(record: FacetEvidenceRecord, label: string): boolean {
  const semantic = (record.semantic_text ?? [])
    .slice(0, 8)
    .map((entry) => entry.text)
    .join(" ");
  const haystack = tokenPath([
    record.id,
    label,
    record.description,
    semantic,
  ].filter((value): value is string => typeof value === "string" && value.length > 0).join(" ")) ?? "";
  if (!haystack) return false;
  if (/\b(ui|config|options?|keybinds?|errors?|recipe_transfer|tooltip|message|subtitle|sound|screen|screenhandler)\b/.test(haystack)) {
    return false;
  }
  return /\b(anvil|barrel|blast|bloomery|boiling|brewing|casting|centrifuge|compressor|compacting|cooking|crucible|crushing|cutting|deployer|deploying|distillery|drying|extruder|filling|forge|furnace|hammering|lathe|loom|machine|macerator|milling|mixer|mixing|oven|polishing|press|pressing|processor|quern|rolling|sawmill|separator|smeltery|smelting|station|washer|workbench)\b/.test(haystack);
}

function canUseItemSemanticJoin(record: FacetEvidenceRecord): boolean {
  return record.kind !== "recipe_type" &&
    record.kind !== "recipe_id_family" &&
    record.kind !== "recipe_role_summary";
}

function canUseRuntimeItemSemanticJoin(record: FacetEvidenceRecord): boolean {
  return record.kind === "recipe_type" || record.kind === "recipe_id_family";
}

function semanticEvidenceMatchesRecipeType(record: FacetEvidenceRecord, recipeType: string): boolean {
  if (record.kind === "recipe_type" && record.id === recipeType) return true;
  if (record.recipe_type === recipeType) return true;
  const haystack = [
    record.id,
    record.label,
    record.title,
    record.description,
    ...(record.semantic_text ?? []).slice(0, 12).map((entry) => entry.text),
  ]
    .filter((value): value is string => typeof value === "string" && value.length > 0)
    .map(tokenPath)
    .join(" ");
  return haystackMatchesRecipeType(haystack, recipeType);
}

function semanticEvidenceEntryMatchesRecipeType(evidence: VocabularySemanticEvidence, recipeType: string): boolean {
  const haystack = [evidence.id, evidence.label, evidence.text]
    .filter((value): value is string => typeof value === "string" && value.length > 0)
    .map(tokenPath)
    .join(" ");
  return haystackMatchesRecipeType(haystack, recipeType);
}

function haystackMatchesRecipeType(haystack: string, recipeType: string): boolean {
  if (!haystack) return false;
  for (const needle of recipeTypeNeedles(recipeType)) {
    if (new RegExp(`(^|[^a-z0-9])${escapeRegExp(needle)}([^a-z0-9]|$)`, "i").test(haystack)) {
      return true;
    }
  }
  return false;
}

function recipeTypeNeedles(recipeType: string): string[] {
  const recipeTypeTail = splitResourceLocation(recipeType)?.path.split("/").filter(Boolean).at(-1);
  if (!recipeTypeTail) return [];
  const normalized = tokenPath(recipeTypeTail);
  if (!normalized) return [];
  const tokens = normalized.split(/[\/_]/).filter((token) => token && !GENERIC_TOKENS.has(token));
  const out = new Set<string>();
  for (const token of tokens) {
    out.add(token);
    if (token.endsWith("ing") && token.length > 5) {
      out.add(undoubleTrailingConsonant(token.slice(0, -3)));
    }
  }
  if (out.has("smelting")) {
    out.add("smelt");
    out.add("melt");
    out.add("melting");
    out.add("molten");
  }
  if (out.has("casting")) {
    out.add("cast");
    out.add("mold");
    out.add("molds");
    out.add("molten");
  }
  return [...out].filter((value) => value && !GENERIC_TOKENS.has(value));
}

function undoubleTrailingConsonant(value: string): string {
  if (value.length < 2) return value;
  const last = value.at(-1);
  if (last && value.at(-2) === last) return value.slice(0, -1);
  return value;
}

function pushSemantic(
  map: Map<string, VocabularySemanticEvidence[]>,
  key: string,
  values: readonly VocabularySemanticEvidence[],
): void {
  if (!key || values.length === 0) return;
  const existing = map.get(key) ?? [];
  map.set(key, limitedSemanticEvidence([...existing, ...values]));
}

function limitedSemanticEvidence(values: readonly VocabularySemanticEvidence[]): VocabularySemanticEvidence[] {
  const seen = new Set<string>();
  const out: VocabularySemanticEvidence[] = [];
  for (const value of values) {
    const text = value.text ?? "";
    const key = `${value.kind}\u0000${value.id}\u0000${value.source}\u0000${value.key ?? ""}\u0000${text}`;
    if (seen.has(key)) continue;
    seen.add(key);
    out.push(value);
    if (out.length >= SEMANTIC_EVIDENCE_LIMIT) break;
  }
  return out;
}

function recipeTypeFromRecipeRef(recipe: string): string | null {
  const split = splitResourceLocation(recipe);
  if (!split) return null;
  const first = split.path.split("/").filter(Boolean)[0];
  return first ? `${split.namespace}:${first}` : null;
}

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function addCandidate(
  acc: Map<string, CandidateAccumulator>,
  seed: CandidateSeed,
): void {
  const issue = validateMultiValue(seed.facet, [seed.id]);
  if (issue) return;
  const key = `${seed.facet}\u0000${seed.id}`;
  let candidate = acc.get(key);
  if (!candidate) {
    candidate = {
      facet: seed.facet,
      id: seed.id,
      label: seed.label,
      origin: seed.origin,
      suggested_state: seed.suggestedState,
      confidence: seed.confidence,
      support: seed.support ?? 1,
      evidence: [],
      semantic_evidence: [],
      seed_items: [],
      aliases: [],
      ...(seed.description ? { description: seed.description } : {}),
      ...(seed.parent ? { parent: seed.parent } : {}),
      ...(seed.defaultOrganizationGroup ? { default_organization_group: seed.defaultOrganizationGroup } : {}),
      ...(seed.relatedActivity ? { related_activity: [...seed.relatedActivity] } : {}),
      reasons: [],
      evidenceKeys: new Set(),
      semanticEvidenceKeys: new Set(),
      seedItemKeys: new Set(),
      aliasKeys: new Set(),
      reasonKeys: new Set(),
    };
    acc.set(key, candidate);
  } else {
    candidate.support += seed.support ?? 1;
    candidate.confidence = Math.max(candidate.confidence, seed.confidence);
    candidate.suggested_state = strongestState(candidate.suggested_state, seed.suggestedState);
    if (candidate.origin !== "previous" && seed.origin === "previous") candidate.origin = "previous";
    if (candidate.origin !== "universal_default" && seed.origin === "universal_default") candidate.origin = "universal_default";
    if (!candidate.description && seed.description) candidate.description = seed.description;
    if (!candidate.parent && seed.parent) candidate.parent = seed.parent;
    if (!candidate.default_organization_group && seed.defaultOrganizationGroup) {
      candidate.default_organization_group = seed.defaultOrganizationGroup;
    }
    if (!candidate.related_activity && seed.relatedActivity) candidate.related_activity = [...seed.relatedActivity];
  }

  for (const evidence of seed.evidence ?? []) {
    const evidenceKey = `${evidence.kind}\u0000${evidence.id}`;
    if (candidate.evidenceKeys.has(evidenceKey)) continue;
    candidate.evidenceKeys.add(evidenceKey);
    candidate.evidence.push(evidence);
  }
  for (const evidence of seed.semanticEvidence ?? []) {
    const text = evidence.text ?? "";
    const evidenceKey = `${evidence.kind}\u0000${evidence.id}\u0000${evidence.source}\u0000${evidence.key ?? ""}\u0000${text}`;
    if (candidate.semanticEvidenceKeys.has(evidenceKey)) continue;
    candidate.semanticEvidenceKeys.add(evidenceKey);
    candidate.semantic_evidence.push(evidence);
  }
  for (const item of seed.seedItems ?? []) {
    if (candidate.seedItemKeys.has(item)) continue;
    candidate.seedItemKeys.add(item);
    candidate.seed_items.push(item);
  }
  for (const alias of seed.aliases ?? []) {
    const normalized = alias.trim();
    if (!normalized || normalized === seed.label || candidate.aliasKeys.has(normalized)) continue;
    candidate.aliasKeys.add(normalized);
    candidate.aliases.push(normalized);
  }
  if (seed.reason && !candidate.reasonKeys.has(seed.reason)) {
    candidate.reasonKeys.add(seed.reason);
    candidate.reasons.push(seed.reason);
  }
}

async function queryFacetCuration(
  client: LlmClient,
  prompt: { system: string; user: string },
  model: string | undefined,
  clientOptions: Partial<QueryOptions> | undefined,
  expectedCandidateIds: readonly string[],
): Promise<{ raw: string; values: CuratedValue[] }> {
  const callerValidator = clientOptions?.responseValidator;
  const coverageValidator = vocabularyCurationCoverageValidator(expectedCandidateIds);
  const queryOptions: QueryOptions = {
    model: model ?? "deepseek/deepseek-v4-flash",
    // Do not impose a default completion cap here. Full-pack vocabulary
    // curation can require large JSON responses, and a too-small local
    // maxTokens setting becomes a truncation source. Callers may still pass
    // maxTokens deliberately through clientOptions for provider experiments.
    timeoutMs: clientOptions?.timeoutMs ?? VOCABULARY_CURATION_TIMEOUT_MS,
    ...clientOptions,
    responseValidator: (content) => {
      const coverage = coverageValidator(content);
      if (!coverage.ok) return coverage;
      return callerValidator ? callerValidator(content) : { ok: true };
    },
  };
  const raw = client.querySplit
    ? await client.querySplit(prompt.system, prompt.user, queryOptions)
    : await client.query(`${prompt.system}\n\n${prompt.user}`, queryOptions);
  const verdict = queryOptions.responseValidator!(raw);
  if (!verdict.ok) {
    throw new Error(`facet vocabulary curation response failed coverage validation: ${verdict.reason ?? "incomplete response"}`);
  }
  return { raw, values: parseVocabularyCurationResponse(raw) };
}

function vocabularyCurationCoverageValidator(
  expectedCandidateIds: readonly string[],
): (content: string) => { ok: boolean; reason?: string } {
  const expected = new Set(expectedCandidateIds);
  return (content: string) => {
    let parsed: CuratedValue[];
    try {
      parsed = parseVocabularyCurationResponse(content);
    } catch (err) {
      return { ok: false, reason: `parse failed: ${(err as Error).message.slice(0, 120)}` };
    }
    const seen = new Set(parsed.map((value) => value.id));
    const missing = [...expected].filter((id) => !seen.has(id));
    if (missing.length > 0) {
      return {
        ok: false,
        reason: `missing ${missing.length}/${expected.size} candidate decision(s): ${missing.slice(0, 8).join(", ")}`,
      };
    }
    return { ok: true };
  };
}

function applyFacetPolicy(args: {
  facet: VocabularyFacetId;
  candidates: readonly PackVocabularyCandidate[];
  curated: readonly CuratedValue[];
  minEvidence: number;
  diagnostics: VocabularyDiagnostic[];
  acceptedWorkflowIds?: ReadonlySet<string>;
}): VocabularyDecision[] {
  const candidateById = new Map(args.candidates.map((candidate) => [candidate.id, candidate]));
  const curatedById = new Map<string, CuratedValue>();
  for (const value of args.curated) {
    if (!curatedById.has(value.id)) curatedById.set(value.id, value);
  }

  const decisions: VocabularyDecision[] = [];
  const ids = new Set([
    ...args.candidates.map((candidate) => candidate.id),
    ...args.curated.map((value) => value.id),
  ]);
  for (const id of [...ids].sort()) {
    const candidate = candidateById.get(id);
    const curated = curatedById.get(id);
    const base = candidate ?? curatedToCandidate(args.facet, curated!);
    const notes: string[] = [];
    let state = curated?.state ?? base.suggested_state;
    let origin = base.origin;
    let confidence = curated?.confidence ?? base.confidence;
    let label = curated?.label ?? base.label;
    let description = curated?.description ?? base.description;
    let aliases = sortedLimited([...(curated?.aliases ?? []), ...base.aliases], CANDIDATE_EXAMPLE_LIMIT);
    let evidence = curated?.evidence?.length ? curated.evidence : base.evidence;
    let seedItems = curated?.seed_items?.length ? curated.seed_items : base.seed_items;
    let defaultOrganizationGroup = curated?.default_organization_group ?? base.default_organization_group;
    let relatedActivity = sortedLimited(
      [...(curated?.related_activity ?? []), ...(base.related_activity ?? [])],
      CANDIDATE_EXAMPLE_LIMIT,
    );

    const issue = validateMultiValue(args.facet, [id]);
    if (issue) {
      state = "rejected";
      notes.push(issue.reason);
    }
    if (!issue && candidate?.origin === "universal_default" && state !== "accepted") {
      state = "accepted";
      origin = "universal_default";
      confidence = Math.max(confidence, candidate.confidence);
      notes.push("universal default kept accepted by policy");
    }
    if (!issue && candidate?.origin === "universal_default" && state === "accepted") {
      label = candidate.label;
      description = candidate.description;
      aliases = candidate.aliases;
      evidence = candidate.evidence;
      seedItems = candidate.seed_items;
      defaultOrganizationGroup = candidate.default_organization_group;
      relatedActivity = candidate.related_activity ?? [];
      origin = "universal_default";
      confidence = Math.max(confidence, candidate.confidence);
    }
    if (!candidate && state === "accepted") {
      state = "review";
      origin = "stage3_proposed";
      notes.push("model proposed id without deterministic candidate evidence");
    }
    if (!curated && origin !== "previous" && origin !== "universal_default" && state === "accepted") {
      state = "review";
      notes.push("candidate was not accepted by curation response");
    }
    if (
      candidate &&
      candidate.origin !== "previous" &&
      candidate.origin !== "universal_default" &&
      candidate.support < args.minEvidence &&
      state === "accepted" &&
      !allowsLowSupportAcceptedValue(args.facet, candidate, curated)
    ) {
      state = "review";
      notes.push(`support ${candidate.support} below min_evidence ${args.minEvidence}`);
    }
    if (isGenericValueId(id) && state === "accepted") {
      state = "rejected";
      notes.push("generic catch-all value rejected by policy");
    }
    if (args.facet === "mod_subsystem" && modSubsystemIdLooksRejected(id)) {
      state = "rejected";
      notes.push("mod_subsystem must be a namespace-scoped identity value, not pack/universal/generic");
    }
    if (args.facet === "workflow_role") {
      const parent = id.split("#")[0] ?? "";
      if (!parent || (curated?.parent && curated.parent !== parent)) {
        state = "rejected";
        notes.push("workflow_role parent must equal id prefix before #");
      }
      if (state === "accepted" && parent && args.acceptedWorkflowIds && !args.acceptedWorkflowIds.has(parent)) {
        state = "rejected";
        notes.push("workflow_role parent is not an accepted workflow value");
      }
    }
    if (state === "accepted" && args.facet === "workflow" && workflowCandidateLooksTooGranular(id, candidate)) {
      state = "review";
      notes.push("guide/quest/advancement workflow title is too granular; prefer a reusable process/station id");
    }
    if (state === "accepted" && args.facet === "progression_stage" && progressionCandidateLooksTooGranular(id, candidate)) {
      state = "review";
      notes.push("advancement-title progression value is too phrase-like; prefer a canonical gate/tier/dimension id");
    }
    if (state === "accepted" && args.facet === "organization_group" && organizationGroupIdLooksLikeStation(id)) {
      state = "review";
      notes.push("organization_group must be a human storage group, not a workstation/process label");
    }
    if (state === "accepted" && evidence.length === 0 && origin !== "universal_default" && origin !== "previous") {
      state = "review";
      notes.push("accepted non-default values require evidence");
    }
    if (state !== "accepted" && curated?.state === "accepted") {
      args.diagnostics.push({
        severity: "warning",
        facet: args.facet,
        id,
        message: `policy downgraded accepted value to ${state}: ${notes.join("; ")}`,
      });
    }

    decisions.push({
      facet: args.facet,
      id,
      label,
      state,
      origin,
      confidence: round(confidence),
      evidence: evidence.slice(0, CANDIDATE_EXAMPLE_LIMIT),
      seed_items: sortedLimited(seedItems, CANDIDATE_EXAMPLE_LIMIT),
      ...(aliases.length ? { aliases } : {}),
      ...(description ? { description } : {}),
      ...(args.facet === "workflow_role" ? { parent: curated?.parent ?? base.parent ?? id.split("#")[0]! } : {}),
      ...(defaultOrganizationGroup ? { default_organization_group: defaultOrganizationGroup } : {}),
      ...(relatedActivity.length ? { related_activity: relatedActivity } : {}),
      policy_notes: notes,
    });
  }
  return decisions.sort((a, b) => stateRank(a.state) - stateRank(b.state) || a.id.localeCompare(b.id));
}

function allowsLowSupportAcceptedValue(
  facet: VocabularyFacetId,
  candidate: PackVocabularyCandidate,
  curated: CuratedValue | undefined,
): boolean {
  if (facet !== "progression_stage") return false;
  if (curated?.state !== "accepted") return false;
  if (candidate.evidence.length === 0) return false;
  return (curated.confidence ?? candidate.confidence) >= 0.6;
}

function canonicalProgressionIds(
  record: FacetEvidenceRecord,
  packId: string,
  scope: "namespace" | "pack",
): string[] {
  const namespace = scope === "namespace" ? record.namespace : undefined;
  const owner = scope === "pack" || !namespace ? `pack:${packId}/` : `${namespace}:`;
  const rawValues = [
    record.id,
    record.label,
    record.title,
    record.description,
    ...(record.semantic_text ?? []).slice(0, 8).map((entry) => entry.text),
  ].filter((value): value is string => typeof value === "string" && value.length > 0);
  const tokens = new Set<string>();
  for (const raw of rawValues) {
    const normalized = tokenPath(raw)?.replace(/\//g, "_") ?? "";
    for (const value of canonicalProgressionTokens(normalized)) {
      tokens.add(value);
    }
  }
  return [...tokens]
    .sort((a, b) => progressionTokenRank(a) - progressionTokenRank(b) || a.localeCompare(b))
    .slice(0, 4)
    .map((value) => `${owner}${value}`);
}

function canonicalProgressionTokens(value: string): string[] {
  const out = new Set<string>();
  const parts = value.split(/_+/).filter(Boolean);
  const joined = parts.join("_");
  for (const tier of VOLTAGE_TIERS) {
    if (parts.includes(tier)) out.add(tier);
  }
  for (const candidate of [
    "black_steel",
    "red_steel",
    "blue_steel",
    "wrought_iron",
    "bismuth_bronze",
    "black_bronze",
    "blast_furnace",
    "mechanical_power",
    "primitive_alloys",
    "steam_age",
    "electric_age",
  ]) {
    if (joined.includes(candidate)) out.add(candidate);
  }
  for (const candidate of ["moon", "mars", "venus", "mercury", "nether", "beneath", "space", "orbit", "steel", "bronze", "bloomery", "crucible"]) {
    if (parts.includes(candidate)) out.add(candidate);
  }
  const rocket = joined.match(/tier_([0-9]+)_rocket/);
  if (rocket) out.add(`tier_${rocket[1]}_rocket`);
  return [...out].filter(isCanonicalProgressionPath);
}

function progressionTokenRank(value: string): number {
  if (VOLTAGE_TIERS.includes(value as typeof VOLTAGE_TIERS[number])) return 0;
  if (/^tier_[0-9]+_rocket$/.test(value)) return 1;
  if (["moon", "mars", "venus", "mercury", "nether", "beneath", "space", "orbit"].includes(value)) return 2;
  if (value.includes("steel") || value.includes("bronze") || value === "wrought_iron") return 3;
  return 4;
}

function workflowCandidateLooksTooGranular(
  id: string,
  candidate: PackVocabularyCandidate | undefined,
): boolean {
  if (!candidate || candidate.evidence.length === 0) return false;
  if (candidate.evidence.some((evidence) => evidence.kind === "recipe_type")) return false;
  if (!candidate.evidence.every((evidence) =>
    evidence.kind === "guide_page" || evidence.kind === "quest_node" || evidence.kind === "advancement"
  )) {
    return false;
  }
  const path = valueIdPath(id);
  const parts = path.split("/").filter(Boolean);
  if (parts.length >= 3) return true;
  return /\b(using|setting|addressing|processing|generating|controlling|placing|moving|routing|displaying|automating|advanced)\b/.test(
    parts.join(" "),
  );
}

function progressionCandidateLooksTooGranular(
  id: string,
  candidate: PackVocabularyCandidate | undefined,
): boolean {
  if (!candidate || candidate.evidence.length === 0) return false;
  if (!candidate.evidence.every((evidence) => evidence.kind === "advancement")) return false;
  const path = valueIdPath(id);
  const parts = path.split("/").filter(Boolean);
  if (parts.length <= 1) return false;
  return !isCanonicalProgressionPath(parts.join("_"));
}

function isCanonicalProgressionPath(value: string): boolean {
  if (VOLTAGE_TIERS.includes(value as typeof VOLTAGE_TIERS[number])) return true;
  if (/^tier_[0-9]+_rocket$/.test(value)) return true;
  return new Set([
    "moon",
    "mars",
    "venus",
    "mercury",
    "nether",
    "beneath",
    "space",
    "orbit",
    "steel",
    "black_steel",
    "red_steel",
    "blue_steel",
    "wrought_iron",
    "bronze",
    "bismuth_bronze",
    "black_bronze",
    "bloomery",
    "blast_furnace",
    "crucible",
    "mechanical_power",
    "primitive_alloys",
    "steam_age",
    "electric_age",
  ]).has(value);
}

function valueIdPath(id: string): string {
  const base = id.includes("#") ? id.slice(0, id.indexOf("#")) : id;
  const split = splitResourceLocation(base);
  return split?.path ?? base;
}

function decisionsToAcceptedVocabulary(
  decisions: Record<string, VocabularyDecision[]>,
): PackFacetVocabulary["facets"] {
  const facets: PackFacetVocabulary["facets"] = {};
  for (const [facet, values] of Object.entries(decisions)) {
    const accepted = values.filter((value) => value.state === "accepted");
    if (accepted.length === 0) continue;
    facets[facet] = { values: {} };
    for (const value of accepted) {
      const entry: VocabularyValue = {
        label: value.label,
        origin: value.origin,
        state: "accepted",
        confidence: value.confidence,
        ...(value.aliases?.length ? { aliases: value.aliases } : {}),
        ...(value.description ? { description: value.description } : {}),
        ...(value.evidence.length ? { evidence: value.evidence } : {}),
        ...(value.seed_items.length ? { seed_items: value.seed_items } : {}),
        ...(value.parent ? { parent: value.parent } : {}),
        ...(value.default_organization_group ? { default_organization_group: value.default_organization_group } : {}),
        ...(value.related_activity?.length ? { related_activity: value.related_activity } : {}),
      };
      facets[facet]!.values[value.id] = entry;
    }
  }
  return facets;
}

function curatedToCandidate(facet: VocabularyFacetId, curated: CuratedValue): PackVocabularyCandidate {
  return {
    facet,
    id: curated.id,
    label: curated.label ?? labelFromId(curated.id),
    origin: "stage3_proposed",
    suggested_state: curated.state ?? "review",
    confidence: curated.confidence ?? 0.5,
    support: 0,
    evidence: curated.evidence ?? [],
    semantic_evidence: [],
    seed_items: curated.seed_items ?? [],
    aliases: curated.aliases ?? [],
    ...(curated.description ? { description: curated.description } : {}),
    ...(curated.parent ? { parent: curated.parent } : {}),
    ...(curated.default_organization_group ? { default_organization_group: curated.default_organization_group } : {}),
    ...(curated.related_activity ? { related_activity: curated.related_activity } : {}),
    reasons: ["model-proposed value not present in deterministic candidates"],
  };
}

function groupCandidates(
  candidates: readonly PackVocabularyCandidate[],
  maxPerFacet: number,
): Record<string, PackVocabularyCandidate[]> {
  const grouped = new Map<string, PackVocabularyCandidate[]>();
  for (const candidate of candidates) {
    const group = grouped.get(candidate.facet) ?? [];
    group.push(candidate);
    grouped.set(candidate.facet, group);
  }
  const out: Record<string, PackVocabularyCandidate[]> = {};
  for (const [facet, values] of grouped) {
    out[facet] = selectPromptCandidates(values, maxPerFacet);
  }
  return out;
}

function workflowRoleCandidatesForAcceptedWorkflows(
  candidates: readonly PackVocabularyCandidate[],
  acceptedWorkflowIds: ReadonlySet<string>,
): PackVocabularyCandidate[] {
  return candidates.filter((candidate) => {
    if (candidate.origin === "previous") return true;
    const parent = candidate.parent ?? candidate.id.split("#")[0] ?? "";
    return acceptedWorkflowIds.has(parent);
  });
}

function selectPromptCandidates(
  candidates: readonly PackVocabularyCandidate[],
  maxCandidates: number,
): PackVocabularyCandidate[] {
  const max = Math.max(1, Math.floor(maxCandidates));
  if (candidates.length <= max) return [...candidates];

  const selected = new Map<string, PackVocabularyCandidate>();
  const add = (candidate: PackVocabularyCandidate) => {
    if (selected.size >= max) return;
    selected.set(candidate.id, candidate);
  };

  for (const candidate of candidates) {
    if (candidate.origin === "previous" || candidate.origin === "universal_default") add(candidate);
  }

  const buckets = new Map<string, PackVocabularyCandidate[]>();
  for (const candidate of candidates) {
    if (selected.has(candidate.id)) continue;
    const key = promptCandidateBucket(candidate);
    const bucket = buckets.get(key) ?? [];
    bucket.push(candidate);
    buckets.set(key, bucket);
  }

  while (selected.size < max && buckets.size > 0) {
    let progressed = false;
    for (const [key, bucket] of [...buckets]) {
      const candidate = bucket.shift();
      if (candidate) {
        add(candidate);
        progressed = true;
      }
      if (bucket.length === 0) buckets.delete(key);
      if (selected.size >= max) break;
    }
    if (!progressed) break;
  }

  return [...selected.values()].sort(compareCandidates);
}

function promptCandidateBucket(candidate: PackVocabularyCandidate): string {
  const evidenceKind = candidate.evidence[0]?.kind ?? "none";
  const reason = candidate.reasons[0] ?? "";
  const namespace = splitResourceLocation(candidate.id)?.namespace ?? "pack";
  return `${candidate.facet}\u0000${candidate.origin}\u0000${candidate.suggested_state}\u0000${evidenceKind}\u0000${reason}\u0000${namespace}`;
}

function chunkCandidates(
  candidates: readonly PackVocabularyCandidate[],
  maxPerPrompt: number,
): PackVocabularyCandidate[][] {
  if (candidates.length === 0) return [[]];
  const size = Math.max(1, Math.floor(maxPerPrompt));
  const out: PackVocabularyCandidate[][] = [];
  for (let index = 0; index < candidates.length; index += size) {
    out.push(candidates.slice(index, index + size));
  }
  return out;
}

function previousAcceptedByFacet(
  previous: PackFacetVocabulary | undefined,
): Map<VocabularyFacetId, string[]> {
  const out = new Map<VocabularyFacetId, string[]>();
  if (!previous) return out;
  for (const [facet, values] of Object.entries(previous.facets ?? {})) {
    if (!isVocabularyFacet(facet)) continue;
    out.set(facet, Object.entries(values.values ?? {})
      .filter(([, value]) => value.state === "accepted")
      .map(([id]) => id)
      .sort());
  }
  return out;
}

function stripCandidateInternals(candidate: PackVocabularyCandidate): PackVocabularyCandidate {
  return {
    facet: candidate.facet,
    id: candidate.id,
    label: candidate.label,
    origin: candidate.origin,
    suggested_state: candidate.suggested_state,
    confidence: round(candidate.confidence),
    support: candidate.support,
    evidence: candidate.evidence,
    semantic_evidence: candidate.semantic_evidence,
    seed_items: candidate.seed_items,
    aliases: candidate.aliases,
    ...(candidate.description ? { description: candidate.description } : {}),
    ...(candidate.parent ? { parent: candidate.parent } : {}),
    ...(candidate.default_organization_group ? { default_organization_group: candidate.default_organization_group } : {}),
    ...(candidate.related_activity ? { related_activity: candidate.related_activity } : {}),
    reasons: candidate.reasons,
  };
}

function finalizeCandidate(candidate: CandidateAccumulator): PackVocabularyCandidate {
  candidate.evidence = candidate.evidence.slice(0, CANDIDATE_EXAMPLE_LIMIT);
  candidate.semantic_evidence = candidate.semantic_evidence.slice(0, SEMANTIC_EVIDENCE_LIMIT);
  candidate.seed_items = sortedLimited(candidate.seed_items, CANDIDATE_EXAMPLE_LIMIT);
  candidate.aliases = sortedLimited(candidate.aliases, CANDIDATE_EXAMPLE_LIMIT);
  candidate.reasons = sortedLimited(candidate.reasons, CANDIDATE_EXAMPLE_LIMIT);
  return stripCandidateInternals(candidate);
}

function compareCandidates(a: PackVocabularyCandidate, b: PackVocabularyCandidate): number {
  return a.facet.localeCompare(b.facet) ||
    originRank(a.origin) - originRank(b.origin) ||
    stateRank(a.suggested_state) - stateRank(b.suggested_state) ||
    b.support - a.support ||
    b.semantic_evidence.length - a.semantic_evidence.length ||
    b.confidence - a.confidence ||
    a.id.localeCompare(b.id);
}

function originRank(origin: VocabularyOrigin): number {
  switch (origin) {
    case "previous":
      return 0;
    case "universal_default":
      return 1;
    default:
      return 2;
  }
}

function facetSet(facets: readonly string[] | undefined): Set<VocabularyFacetId> {
  const out = new Set<VocabularyFacetId>();
  const requested = facets && facets.length > 0 ? facets : VOCABULARY_BACKED_FACETS;
  const unknown: string[] = [];
  for (const facet of requested) {
    if (isVocabularyFacet(facet)) out.add(facet);
    else unknown.push(facet);
  }
  if (unknown.length > 0) {
    throw new Error(
      `unknown vocabulary facet(s): ${unknown.join(", ")}; expected one of ${VOCABULARY_BACKED_FACETS.join(", ")}`,
    );
  }
  return out;
}

function isVocabularyFacet(facet: string): facet is VocabularyFacetId {
  return (VOCABULARY_BACKED_FACETS as readonly string[]).includes(facet);
}

function namespaceAllowed(record: FacetEvidenceRecord, namespaces: ReadonlySet<string>): boolean {
  if (namespaces.size === 0) return true;
  if (record.namespace && namespaces.has(record.namespace)) return true;
  const idNamespace = record.id.split(":")[0];
  if (idNamespace && namespaces.has(idNamespace)) return true;
  return (record.item_refs ?? []).some((item) => namespaces.has(item.split(":")[0] ?? ""));
}

function evidenceRef(record: FacetEvidenceRecord): VocabularyEvidenceRef {
  return {
    kind: record.kind,
    id: record.id,
    confidence: round(record.confidence),
  };
}

function normalizeScopedResourceId(
  id: string,
  packId: string,
  fallback: "namespace" | "pack",
): string | null {
  const split = splitResourceLocation(id);
  if (!split) {
    const token = tokenPath(id);
    return token ? `pack:${packId}/${token}` : null;
  }
  const path = tokenPath(split.path);
  if (!path) return null;
  if (fallback === "pack") return `pack:${packId}/${path}`;
  return `${split.namespace}:${path}`;
}

function modSubsystemId(namespace: string, value: string): string | null {
  const normalizedNamespace = namespace.match(/^[a-z0-9_.-]+$/) ? namespace : null;
  if (!normalizedNamespace) return null;
  const normalizedToken = token(value);
  if (!normalizedToken || MOD_SUBSYSTEM_STOP_TOKENS.has(normalizedToken)) return null;
  return `${normalizedNamespace}:${normalizedToken}`;
}

function modSubsystemSignalTokens(values: readonly string[]): string[] {
  const out = new Set<string>();
  for (const value of values) {
    for (const raw of value.toLowerCase().split(/[^a-z0-9]+/)) {
      if (!raw || MOD_SUBSYSTEM_STOP_TOKENS.has(raw)) continue;
      const singular = raw.endsWith("s") && raw.length > 4 ? raw.slice(0, -1) : raw;
      addModSubsystemSignalToken(out, raw);
      addModSubsystemSignalToken(out, singular);
    }
    const tail = resourcePathTail(value);
    if (tail && !MOD_SUBSYSTEM_STOP_TOKENS.has(tail) && MOD_SUBSYSTEM_SIGNAL_TOKENS.has(tail)) {
      addModSubsystemSignalToken(out, tail);
    }
  }
  return [...out].sort();
}

function ownedNamespaceValues(namespace: string, values: readonly string[] | undefined): string[] {
  if (!values?.length) return [];
  return values.filter((value) => splitResourceLocation(value)?.namespace === namespace);
}

function addModSubsystemSignalToken(out: Set<string>, value: string): void {
  if (!MOD_SUBSYSTEM_SIGNAL_TOKENS.has(value)) return;
  out.add(MOD_SUBSYSTEM_CANONICAL_TOKENS.get(value) ?? value);
}

function resourcePathTail(value: string): string | null {
  const split = splitResourceLocation(value);
  const raw = split?.path ?? value;
  const parts = raw.split(/[\/_.\s-]+/).filter(Boolean);
  if (parts.length === 0) return null;
  return token(parts[parts.length - 1]!);
}

function isModSubsystemNamespace(namespace: string | undefined, packId?: string): namespace is string {
  return !!namespace && namespace !== packId && !["c", "forge", "minecraft"].includes(namespace);
}

function modSubsystemIdLooksRejected(id: string): boolean {
  const split = splitResourceLocation(id);
  if (!split) return true;
  if (split.namespace === "slot" || split.namespace === "pack" || !isModSubsystemNamespace(split.namespace)) return true;
  const tail = resourcePathTail(id);
  return !tail || MOD_SUBSYSTEM_STOP_TOKENS.has(tail);
}

function tokenPath(value: string): string | null {
  const parts = value
    .split(/[\/:#.\s-]+/)
    .map(token)
    .filter(Boolean);
  if (parts.length === 0) return null;
  return parts.join("/");
}

function token(value: string): string {
  const normalized = value
    .toLowerCase()
    .replace(/&/g, " and ")
    .replace(/[^a-z0-9]+/g, "_")
    .replace(/^_+|_+$/g, "")
    .replace(/_+/g, "_");
  if (!normalized) return "";
  if (/^[a-z]/.test(normalized)) return normalized;
  return `value_${normalized}`;
}

function tokenSet(value: string): Set<string> {
  const out = new Set<string>();
  for (const part of value.toLowerCase().split(/[^a-z0-9]+/)) {
    if (!part) continue;
    out.add(part);
    if (part.endsWith("s") && part.length > 3) out.add(part.slice(0, -1));
  }
  return out;
}

function splitResourceLocation(value: string): { namespace: string; path: string } | null {
  const match = value.match(/^([a-z0-9_.-]+):([a-z0-9_./-]+)$/);
  if (!match) return null;
  return { namespace: match[1]!, path: match[2]! };
}

function isGenericValueId(id: string): boolean {
  const raw = id.includes("#") ? id.slice(0, id.indexOf("#")) : id;
  const tail = raw.includes("/") ? raw.slice(raw.lastIndexOf("/") + 1) : raw.slice(raw.lastIndexOf(":") + 1);
  return GENERIC_TOKENS.has(tail) || tail.startsWith("crafting_");
}

function labelFromId(id: string): string {
  const raw = id.includes("#") ? id.slice(id.indexOf("#") + 1) : id;
  const tail = raw.includes("/") ? raw.slice(raw.lastIndexOf("/") + 1) : raw.slice(raw.lastIndexOf(":") + 1);
  return tail
    .replace(/[_./-]+/g, " ")
    .trim()
    .replace(/\b\w/g, (char) => char.toUpperCase());
}

function defaultTuple(input: [string, string, string?, string[]?]): {
  id: string;
  label: string;
  description?: string;
  aliases?: string[];
} {
  return {
    id: input[0]!,
    label: input[1]!,
    ...(input[2] ? { description: input[2] } : {}),
    ...(input[3] ? { aliases: input[3] } : {}),
  };
}

function slotDefault(value: string): { id: string; label: string } {
  return { id: `slot:${value}`, label: labelFromId(value) };
}

function strongestState(a: VocabularyState, b: VocabularyState): VocabularyState {
  return stateRank(b) < stateRank(a) ? b : a;
}

function stateRank(state: VocabularyState): number {
  switch (state) {
    case "accepted":
      return 0;
    case "review":
      return 1;
    case "rejected":
      return 2;
  }
}

function isVocabularyState(value: unknown): value is VocabularyState {
  return value === "accepted" || value === "review" || value === "rejected";
}

function isConfidence(value: unknown): value is number {
  return typeof value === "number" && Number.isFinite(value) && value >= 0 && value <= 1;
}

function evidenceRefs(values: unknown[]): VocabularyEvidenceRef[] {
  const out: VocabularyEvidenceRef[] = [];
  for (const value of values) {
    if (!isRecord(value) || typeof value.kind !== "string" || typeof value.id !== "string") continue;
    out.push({
      kind: value.kind,
      id: value.id,
      ...(isConfidence(value.confidence) ? { confidence: value.confidence } : {}),
    });
  }
  return out;
}

function stringArray(values: unknown[]): string[] {
  return values.filter((value): value is string => typeof value === "string" && value.trim().length > 0);
}

function sortedLimited(values: Iterable<string>, limit: number): string[] {
  return [...new Set([...values].filter((value) => value.length > 0))]
    .sort()
    .slice(0, limit);
}

function round(value: number): number {
  return Math.round(value * 1000) / 1000;
}

function looksLikeResourceLocation(value: string): boolean {
  return /^[a-z0-9_.-]+:[a-z0-9_./-]+$/.test(value);
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function unwrapEnvelope(raw: string): string {
  const trimmed = raw.trim();
  if (!trimmed.startsWith("{")) return trimmed;
  try {
    const obj = JSON.parse(trimmed);
    if (isRecord(obj) && typeof obj.result === "string") return obj.result;
  } catch {
    return trimmed;
  }
  return trimmed;
}

function firstJsonObject(text: string): string | null {
  const fenced = text.match(/```(?:json)?\s*([\s\S]*?)```/);
  const body = fenced?.[1] ?? text;
  const start = body.indexOf("{");
  if (start < 0) return null;
  let depth = 0;
  let inString = false;
  let escape = false;
  for (let i = start; i < body.length; i++) {
    const ch = body[i]!;
    if (escape) {
      escape = false;
      continue;
    }
    if (inString) {
      if (ch === "\\") escape = true;
      else if (ch === "\"") inString = false;
      continue;
    }
    if (ch === "\"") {
      inString = true;
      continue;
    }
    if (ch === "{") depth++;
    if (ch === "}") {
      depth--;
      if (depth === 0) return body.slice(start, i + 1);
    }
  }
  return null;
}
