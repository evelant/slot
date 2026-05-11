import type { ExtractRunMeta, ItemExtractRecord } from "../record.ts";
import { buildItemTagClosure, buildItemTagMembership } from "../tags.ts";
import { buildRecipeRoles } from "../recipes.ts";
import { buildLootSources } from "../loot.ts";
import { resolveModelParents } from "../models.ts";
import { itemSemanticTextFromLang } from "../semantic_text.ts";
import { loadModSourceBundle, type ModSourceBundle } from "./source.ts";

export interface ModExtractResult {
  meta: ExtractRunMeta;
  records: ItemExtractRecord[];
}

export function extractMod(options: {
  modPath: string;
  modNamespace: string;
  generatedBy: string;
}): ModExtractResult {
  const bundle = loadModSourceBundle({
    modPath: options.modPath,
    modNamespace: options.modNamespace,
  });
  return extractFromModBundle(bundle, options.generatedBy);
}

export function extractFromModBundle(
  bundle: ModSourceBundle,
  generatedBy: string,
): ModExtractResult {
  const ns = bundle.modNamespace;
  const itemIds = bundle.registries.item ?? [];

  // Compute tag membership across ALL namespaces in the bundle (item appears
  // in `c:`, `minecraft:`, `<modid>:`, …). The closure helpers accept
  // fully-qualified tag-id keys, so we feed the bundle's pre-flattened map.
  const tagMembership = buildItemTagMembership(bundle.itemTags, ns);

  // Recipe role: ingredient fan-out uses the transitive item-tag closure
  // (any namespace).
  const itemTagMembersFlat = invertTagsToItemMembers(
    buildItemTagClosure(bundle.itemTags, ns),
  );
  const recipeRoles = buildRecipeRoles(bundle.recipes, ns, itemTagMembersFlat);
  const lootSources = buildLootSources(bundle.lootTables, ns);
  const enUs: Record<string, string> = bundle.lang.en_us ?? {};

  const records: ItemExtractRecord[] = [];
  for (const shortId of itemIds) {
    const id = `${ns}:${shortId}`;
    const components = bundle.itemComponents[shortId] ?? null;
    const definition = bundle.itemDefinitions[shortId];
    const displayName = enUs[`item.${ns}.${shortId}`]
      ?? enUs[`block.${ns}.${shortId}`]
      ?? null;
    const semanticText = itemSemanticTextFromLang({ lang: enUs, namespace: ns, path: shortId });
    const membership = tagMembership.get(id);

    records.push({
      id,
      namespace: ns,
      path: shortId,
      display_name: displayName,
      minecraft_tags: membership?.all ?? [],
      minecraft_tags_direct: membership?.direct ?? [],
      recipe_role: recipeRoles.get(id) ?? {
        ingredient_of: [],
        output_of: [],
        in_degree: 0,
        out_degree: 0,
        ingredient_of_counts: {},
        output_of_counts: {},
      },
      model_parents: resolveModelParents(definition, bundle.models),
      loot_table_sources: lootSources.get(id) ?? [],
      creative_tabs: [],
      component_data: components,
      ...(semanticText.length > 0 ? { semantic_text: semanticText } : {}),
    });
  }
  records.sort((a, b) => a.id.localeCompare(b.id));

  const meta: ExtractRunMeta = {
    extractor: `mod:${ns}`,
    source_version: bundle.version,
    generated_at: new Date().toISOString(),
    generated_by: generatedBy,
  };
  return { meta, records };
}

function invertTagsToItemMembers(
  closure: Map<string, string[]>,
): Map<string, string[]> {
  const out = new Map<string, Set<string>>();
  for (const [item, tags] of closure) {
    for (const tag of tags) {
      const bucket = out.get(tag) ?? new Set<string>();
      bucket.add(item);
      out.set(tag, bucket);
    }
  }
  const result = new Map<string, string[]>();
  for (const [tag, items] of out) result.set(tag, [...items].sort());
  return result;
}
