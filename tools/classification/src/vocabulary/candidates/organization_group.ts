import { validateMultiValue } from "../../schema/facets.ts";
import type { FacetEvidenceRecord } from "../../evidence/facet_evidence.ts";
import type { CandidateAccumulator, SemanticEvidenceIndex, VocabularyFacetId } from "../types.ts";
import { GENERIC_TOKENS, ORGANIZATION_GROUP_PREFIX_STOP_TOKENS, ORGANIZATION_GROUP_STOP_TOKENS, ORGANIZATION_GROUP_SUFFIX_STOP_TOKENS, VOLTAGE_COMPONENT_TOKENS, VOLTAGE_TIERS } from "../constants.ts";
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

  const modGroup = modMetadataOrganizationGroupSeed(record, packId);
  if (modGroup) addOrganizationGroupSeed(out, modGroup);

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
  if (record.kind === "item_tag" || record.kind === "block_tag" || record.kind === "runtime_item") {
    for (const tier of VOLTAGE_TIERS) {
      if (!tokens.has(tier)) continue;
      if (!hasAnyToken(tokens, VOLTAGE_COMPONENT_TOKENS)) continue;
      const namespace = splitResourceLocation(record.id)?.namespace ?? record.namespace;
      if (!namespace || namespace === "minecraft" || namespace === "c") continue;
      const label = `${tier.toUpperCase()} Components`;
      addScoped(namespace, `${tier}_components`, label, `${tier.toUpperCase()} voltage-tier components and machine parts.`, "voltage-tier component evidence suggests a player storage group", 0.6);
    }
  }

  return [...out.values()]
    .filter((seed) => !organizationGroupIdLooksLikeStation(seed.id))
    .sort((a, b) => a.id.localeCompare(b.id));
}

function modMetadataOrganizationGroupSeed(
  record: FacetEvidenceRecord,
  packId: string,
): OrganizationGroupSeed | null {
  if (record.kind !== "mod_metadata") return null;
  if (!record.namespace || record.namespace === "minecraft") return null;
  const count = record.count ?? 0;
  if (count <= 0) return null;
  const label = record.label ?? labelFromId(record.namespace);
  if (modMetadataLooksTechnical(record, label)) return null;
  const namespaceToken = token(record.namespace);
  if (!namespaceToken) return null;
  return {
    id: packOrganizationGroupId(packId, `${namespaceToken}_items`),
    label: `${label} Items`,
    confidence: Math.max(0.42, Math.min(0.6, record.confidence)),
    reason: "mod metadata may name a player organization group",
    description: `Candidate storage section for ${label} items; accept only if players would naturally keep this mod's items together rather than by narrower workflow, role, or material groups.`,
  };
}

function modMetadataLooksTechnical(record: FacetEvidenceRecord, label: string): boolean {
  const haystack = tokenPath([
    record.id,
    record.namespace,
    label,
    record.description,
    ...(record.semantic_text ?? []).slice(0, 4).map((entry) => entry.text),
  ].filter((value): value is string => typeof value === "string" && value.length > 0).join(" ")) ?? "";
  if (!haystack) return true;
  return /(^|[\/_])(api|library|core|config|configuration|performance|optimization|tweaks?|patch|fix|fixes|bugfix|dependency|connector|compat|integration|tooltip|menu|ui|debug|developer|resource|datagen|server|client)([\/_]|$)/.test(haystack);
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
