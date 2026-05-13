import { validateMultiValue } from "../../schema/facets.ts";
import type { FacetEvidenceRecord } from "../../evidence/facet_evidence.ts";
import type { CandidateAccumulator, SemanticEvidenceIndex, VocabularyFacetId } from "../types.ts";
import { GENERIC_TOKENS, ORGANIZATION_GROUP_PREFIX_STOP_TOKENS, ORGANIZATION_GROUP_STOP_TOKENS, ORGANIZATION_GROUP_SUFFIX_STOP_TOKENS } from "../constants.ts";
import { addCandidate } from "../candidate_store.ts";
import { labelFromId, splitResourceLocation, token, tokenPath, tokenSet } from "../helpers.ts";
import { runtimeItemRefs, semanticEvidenceForCandidate } from "../semantic_index.ts";
import { evidenceRef } from "../records.ts";
import { isGenericValueId, resourcePathTail } from "../value_ids.ts";

export function addOrganizationGroupEvidenceCandidates(
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

const EQUIPMENT_SLOT_TAG_NAMESPACES = new Set(["accessories", "curios", "trinkets"]);
const EQUIPMENT_SLOT_TOKENS = new Set([
  "back",
  "belt",
  "body",
  "cape",
  "charm",
  "chest",
  "curio",
  "curios",
  "face",
  "feet",
  "foot",
  "hand",
  "hands",
  "head",
  "legs",
  "neck",
  "ring",
  "rings",
  "trinket",
  "trinkets",
  "wrist",
]);

function organizationGroupSeedsForRecord(
  record: FacetEvidenceRecord,
  packId: string,
): OrganizationGroupSeed[] {
  const fields = organizationGroupSignalFields(record);
  if (fields.length === 0) return [];
  const tokens = tokenSet(fields.join(" "));
  const identityTokens = tokenSet(organizationGroupIdentityFields(record).join(" "));
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
  const broad = broadOrganizationGroupSeed(record, packId);
  if (broad) addOrganizationGroupSeed(out, broad);

  // Seeds, Crops, and Plants are protected built-in homes. Do not generate
  // pack-scoped duplicates and ask curation to reject our own noise.
  if (hasAnyToken(tokens, ["dirt", "rock", "rocks", "stone", "stones", "cobble", "cobblestone", "gravel", "sand", "silt", "clay", "mud", "soil"])) {
    addPack("dirt_and_rocks", "Dirt and Rocks", "Terrain rubble such as dirt, stone, gravel, sand, clay, and loose rocks.", "terrain material evidence suggests a player storage group", 0.58, ["rubble"]);
  }
  if (hasAnyToken(tokens, ["decorative", "decoration", "decorations", "decor", "ornament", "ornaments", "furniture", "framed", "frame", "lamp", "lantern"])) {
    addPack("decorative", "Decorative", "Blocks and items primarily kept for decoration or building detail.", "decorative evidence suggests a player storage group", 0.58, ["decorations"]);
  }
  if (hasAnyToken(tokens, ["woodworking", "carpentry", "sawmill", "sawdust", "beam", "beams", "saw"])) {
    addPack("woodworking", "Woodworking", "Carpentry supplies and wood-working outputs beyond stock wood.", "woodworking evidence suggests a player storage group", 0.58);
  }
  if (hasAnyToken(tokens, ["animal", "animals", "husbandry", "feed", "livestock", "cow", "cows", "sheep", "pig", "pigs", "chicken", "chickens"])) {
    addPack("animal_husbandry", "Animal Husbandry", "Livestock supplies, feed, and animal-care items.", "animal/livestock evidence suggests a player storage group", 0.58);
  }
  if (hasAnyToken(tokens, ["weaving", "cloth", "fabric", "textile", "textiles", "thread", "threads", "string", "strings", "yarn", "loom", "sewing", "needle"])) {
    addPack("weaving_cloth", "Weaving and Cloth", "Cloth, fabric, thread, string, weaving, and sewing supplies.", "cloth/weaving evidence suggests a player storage group", 0.6, ["cloth", "textiles"]);
  }
  if (record.kind === "runtime_item" && hasAnyToken(identityTokens, ["pot", "pan", "skillet", "bowl", "bowls", "oven", "grill", "kitchen", "cooking", "cookware"]) && !hasAnyToken(identityTokens, ["food", "foods", "crop", "crops", "seed", "seeds"])) {
    addPack("cooking_tools", "Cooking Tools", "Reusable utensils, cookware, bowls, pots, knives, and food-prep tools.", "runtime item names a cooking tool or utensil", 0.57, ["cookware"]);
  }
  return [...out.values()]
    .filter((seed) => !organizationGroupIdLooksLikeStation(seed.id))
    .sort((a, b) => a.id.localeCompare(b.id));
}

function broadOrganizationGroupSeed(
  record: FacetEvidenceRecord,
  packId: string,
): OrganizationGroupSeed | null {
  // Raw item/block tags are excellent query evidence, but they are too often
  // technical, taxonomic, or form-specific to become main wall sections directly.
  // Keep tag-derived home groups to the explicit broad rules above.
  if (record.kind === "item_tag" || record.kind === "block_tag") {
    return null;
  }
  if (
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
  const namespace = split?.namespace ?? record.namespace;
  const path = tokenPath(split?.path ?? record.id)?.replace(/\//g, "_") ?? "";
  const labelToken = tokenPath(label)?.replace(/\//g, "_") ?? "";
  const haystack = `${path} ${labelToken}`;
  if (organizationGroupRecordLooksLikeEquipmentSlotTag(record, namespace, path, labelToken)) return true;
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

function organizationGroupRecordLooksLikeEquipmentSlotTag(
  record: FacetEvidenceRecord,
  namespace: string | undefined,
  path: string,
  labelToken: string,
): boolean {
  if (record.kind !== "item_tag") return false;
  const terminalPath = path.split("_").filter(Boolean).at(-1) ?? path;
  const terminalLabel = labelToken.split("_").filter(Boolean).at(-1) ?? labelToken;
  const namesEquipmentSlot = EQUIPMENT_SLOT_TOKENS.has(path) ||
    EQUIPMENT_SLOT_TOKENS.has(labelToken) ||
    EQUIPMENT_SLOT_TOKENS.has(terminalPath) ||
    EQUIPMENT_SLOT_TOKENS.has(terminalLabel);
  if (!namesEquipmentSlot) return false;
  if (namespace && EQUIPMENT_SLOT_TAG_NAMESPACES.has(namespace)) return true;
  return (record.semantic_text ?? []).some((entry) => /^slot:/i.test(entry.text.trim()));
}


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

export function organizationGroupIdLooksLikeStation(id: string): boolean {
  const tail = resourcePathTail(id);
  return !!tail && ORGANIZATION_GROUP_STOP_TOKENS.has(tail);
}
