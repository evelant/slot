import type { ItemExtractRecord, ExtractRunMeta } from "../record.ts";
import { buildItemTagClosure, buildItemTagMembership } from "../tags.ts";
import { buildRecipeRoles } from "../recipes.ts";
import { buildLootSources } from "../loot.ts";
import { resolveModelParents } from "../models.ts";
import { itemSemanticTextFromLang } from "../semantic_text.ts";
import {
  ensureVanillaSource,
  loadSummaryBundle,
  type SummaryBundle,
  type VanillaSource,
} from "./source.ts";

export const VANILLA_NAMESPACE = "minecraft";

export interface VanillaExtractResult {
  meta: ExtractRunMeta;
  records: ItemExtractRecord[];
}

/**
 * Run stage 1 against the vanilla mcmeta source and produce one extract record
 * per registered item. No LLM, no derivation — pure collection from summary
 * registries.
 */
export function extractVanilla(options: {
  mcmetaRepoPath: string;
  generatedBy: string;
}): VanillaExtractResult {
  const source = ensureVanillaSource(options.mcmetaRepoPath);
  const bundle = loadSummaryBundle(source);
  return extractFromBundle(bundle, options.generatedBy, source);
}

/** Split out so tests can feed a synthetic bundle without git. */
export function extractFromBundle(
  bundle: SummaryBundle,
  generatedBy: string,
  _source?: VanillaSource,
): VanillaExtractResult {
  const itemIds = bundle.registries.item ?? [];
  const itemTagMembers = tagMembersOnly(bundle.itemTags, VANILLA_NAMESPACE);
  const tagMembership = buildItemTagMembership(bundle.itemTags, VANILLA_NAMESPACE);
  const recipeRoles = buildRecipeRoles(
    bundle.recipes,
    VANILLA_NAMESPACE,
    itemTagMembers,
  );
  const lootSources = buildLootSources(bundle.lootTables, VANILLA_NAMESPACE);
  const enUs: Record<string, string> = bundle.lang.en_us ?? {};

  const records: ItemExtractRecord[] = [];
  for (const shortId of itemIds) {
    const id = `${VANILLA_NAMESPACE}:${shortId}`;
    const components = bundle.itemComponents[shortId] ?? null;
    const definition = bundle.itemDefinitions[shortId];
    const displayName = enUs[`item.${VANILLA_NAMESPACE}.${shortId}`]
      ?? enUs[`block.${VANILLA_NAMESPACE}.${shortId}`]
      ?? null;
    const semanticText = itemSemanticTextFromLang({
      lang: enUs,
      namespace: VANILLA_NAMESPACE,
      path: shortId,
    });

    const membership = tagMembership.get(id);
    records.push({
      id,
      namespace: VANILLA_NAMESPACE,
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
      // vanilla creative tabs are not data-driven, so this is always empty;
      // modded extractors fill it where the mod exposes tab membership.
      creative_tabs: [],
      component_data: components,
      ...(semanticText.length > 0 ? { semantic_text: semanticText } : {}),
    });
  }
  records.sort((a, b) => a.id.localeCompare(b.id));

  const meta: ExtractRunMeta = {
    extractor: "vanilla",
    source_version: bundle.version,
    generated_at: new Date().toISOString(),
    generated_by: generatedBy,
  };
  return { meta, records };
}

/**
 * Flatten tag -> direct item members (no transitive closure) for ingredient
 * fan-out in `buildRecipeRoles`. We actually want the *transitive* closure
 * for that, so re-use `buildItemTagClosure`'s output inverted back to
 * tag -> [items].
 */
function tagMembersOnly(
  itemTags: Record<string, import("./source.ts").TagJson>,
  defaultNamespace: string,
): Map<string, string[]> {
  const closure = buildItemTagClosure(itemTags, defaultNamespace);
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
