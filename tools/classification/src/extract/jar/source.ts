import { basename } from "node:path";
import type {
  ItemDefinitionJson,
  LootTableJson,
  ModelJson,
  RecipeJson,
  SummaryBundle,
  TagJson,
} from "../vanilla/source.ts";
import type { ModSourceBundle } from "../mod/source.ts";
import { ZipArchive } from "../../scan/zip.ts";

export interface JarModBundle extends ModSourceBundle {
  /** Absolute path to the jar this bundle was built from. */
  jarPath: string;
}

export interface LoadJarModBundleOptions {
  jarPath: string;
  modNamespace: string;
  version?: string;
}

/**
 * Build the same SummaryBundle-shaped resource view as the source-tree
 * extractor, but directly from an installed mod jar. This is intentionally a
 * static resource extractor: it sees data/assets shipped in the jar, not
 * registry mutations performed by Java code or KubeJS at runtime.
 */
export function loadJarModBundle(options: LoadJarModBundleOptions): JarModBundle {
  const zip = ZipArchive.open(options.jarPath);
  const names = zip.entryNames();
  const ns = options.modNamespace;

  const recipes: Record<string, RecipeJson> = {};
  const lootTables: Record<string, LootTableJson> = {};
  const itemTags: Record<string, TagJson> = {};
  const blockTags: Record<string, TagJson> = {};
  const itemDefinitions: Record<string, ItemDefinitionJson> = {};
  const models: Record<string, ModelJson> = {};
  const blocks: Record<string, unknown> = {};
  const lang: Record<string, Record<string, string>> = {};

  for (const name of names) {
    collectRecipe(zip, name, ns, recipes);
    collectLootTable(zip, name, ns, lootTables);
    collectTag(zip, name, "item", itemTags);
    collectTag(zip, name, "block", blockTags);
    collectItemDefinition(zip, name, ns, itemDefinitions);
    collectModel(zip, name, ns, models);
    collectBlockstate(zip, name, ns, blocks);
    collectLang(zip, name, ns, lang);
  }

  synthesizeLegacyItemDefinitions(itemDefinitions, models);

  const itemSet = new Set<string>();
  const enUs = lang.en_us ?? {};
  const langKeyRe = new RegExp(`^(item|block)\\.${escapeRegex(ns)}\\.([a-z0-9_/]+)$`);
  for (const key of Object.keys(enUs)) {
    const match = key.match(langKeyRe);
    if (match) itemSet.add(match[2]!);
  }
  for (const id of Object.keys(itemDefinitions)) itemSet.add(id);
  for (const id of Object.keys(blocks)) itemSet.add(id);
  for (const id of collectOwnedRecipeAndLootOutputs(recipes, lootTables, ns)) {
    itemSet.add(id);
  }
  for (const id of collectOwnedTagValues(itemTags, ns)) {
    itemSet.add(id);
  }

  const bundle: SummaryBundle = {
    registries: {
      item: [...itemSet].sort(),
      block: Object.keys(blocks).sort(),
    },
    itemComponents: {},
    recipes,
    lootTables,
    itemTags,
    blockTags,
    itemDefinitions,
    models,
    lang,
    blocks,
    version: usableVersion(options.version) ?? deriveDescriptorVersion(zip) ?? "unknown",
  };

  return {
    ...bundle,
    modNamespace: ns,
    roots: [`jar:${basename(options.jarPath)}`],
    jarPath: options.jarPath,
  };
}

function collectRecipe(
  zip: ZipArchive,
  name: string,
  namespace: string,
  out: Record<string, RecipeJson>,
): void {
  const match = name.match(/^data\/([^/]+)\/recipes?\/(.+)\.json$/);
  if (!match || match[1] !== namespace) return;
  const json = zip.readJson<RecipeJson>(name);
  if (json) out[match[2]!] = json;
}

function collectLootTable(
  zip: ZipArchive,
  name: string,
  namespace: string,
  out: Record<string, LootTableJson>,
): void {
  const match = name.match(/^data\/([^/]+)\/loot_tables?\/(.+)\.json$/);
  if (!match || match[1] !== namespace) return;
  const json = zip.readJson<LootTableJson>(name);
  if (json) out[match[2]!] = json;
}

function collectTag(
  zip: ZipArchive,
  name: string,
  kind: "item" | "block",
  out: Record<string, TagJson>,
): void {
  const plural = kind === "item" ? "items" : "blocks";
  const pattern = new RegExp(`^data\\/([^/]+)\\/tags\\/(${kind}|${plural})\\/(.+)\\.json$`);
  const match = name.match(pattern);
  if (!match) return;
  const json = zip.readJson<TagJson>(name);
  if (!json) return;
  out[`${match[1]}:${match[3]}`] = json;
}

function collectItemDefinition(
  zip: ZipArchive,
  name: string,
  namespace: string,
  out: Record<string, ItemDefinitionJson>,
): void {
  const match = name.match(/^assets\/([^/]+)\/items\/(.+)\.json$/);
  if (!match || match[1] !== namespace) return;
  const json = zip.readJson<ItemDefinitionJson>(name);
  if (json) out[match[2]!] = json;
}

function collectModel(
  zip: ZipArchive,
  name: string,
  namespace: string,
  out: Record<string, ModelJson>,
): void {
  const match = name.match(/^assets\/([^/]+)\/models\/(.+)\.json$/);
  if (!match || match[1] !== namespace) return;
  const json = zip.readJson<ModelJson>(name);
  if (json) out[match[2]!] = json;
}

function collectBlockstate(
  zip: ZipArchive,
  name: string,
  namespace: string,
  out: Record<string, unknown>,
): void {
  const match = name.match(/^assets\/([^/]+)\/blockstates\/(.+)\.json$/);
  if (!match || match[1] !== namespace) return;
  const json = zip.readJson<unknown>(name);
  if (json) out[match[2]!] = json;
}

function collectLang(
  zip: ZipArchive,
  name: string,
  namespace: string,
  out: Record<string, Record<string, string>>,
): void {
  const match = name.match(/^assets\/([^/]+)\/lang\/en_us\.json$/);
  if (!match || match[1] !== namespace) return;
  const json = zip.readJson<Record<string, string>>(name);
  if (json) out.en_us = { ...(out.en_us ?? {}), ...json };
}

/**
 * Forge 1.20 jars usually have assets/<ns>/models/item/*.json but not the
 * 1.21+ assets/<ns>/items/*.json item-definition files. The downstream model
 * resolver starts from an item definition, so synthesize the obvious wrapper
 * when only the legacy item model exists.
 */
function synthesizeLegacyItemDefinitions(
  itemDefinitions: Record<string, ItemDefinitionJson>,
  models: Record<string, ModelJson>,
): void {
  for (const modelId of Object.keys(models)) {
    if (!modelId.startsWith("item/")) continue;
    const itemId = modelId.slice("item/".length);
    if (itemDefinitions[itemId]) continue;
    itemDefinitions[itemId] = {
      model: {
        type: "minecraft:model",
        model: modelId,
      },
    };
  }
}

function collectOwnedRecipeAndLootOutputs(
  recipes: Record<string, RecipeJson>,
  lootTables: Record<string, LootTableJson>,
  namespace: string,
): string[] {
  const out = new Set<string>();
  const visit = (value: unknown) => {
    if (typeof value === "string") {
      addOwnedShortId(value, namespace, out);
      return;
    }
    if (Array.isArray(value)) {
      for (const child of value) visit(child);
      return;
    }
    if (!isRecord(value)) return;
    for (const key of ["id", "item", "name"]) {
      const raw = value[key];
      if (typeof raw === "string") addOwnedShortId(raw, namespace, out);
    }
    for (const child of Object.values(value)) visit(child);
  };
  for (const recipe of Object.values(recipes)) {
    visit(recipe.result);
    visit(recipe.results);
    visit(recipe.output);
    visit(recipe.outputs);
  }
  for (const loot of Object.values(lootTables)) {
    visit(loot);
  }
  return [...out].sort();
}

function collectOwnedTagValues(
  itemTags: Record<string, TagJson>,
  namespace: string,
): string[] {
  const out = new Set<string>();
  for (const tag of Object.values(itemTags)) {
    for (const raw of tag.values ?? []) {
      const id = typeof raw === "string" ? raw : raw.id;
      if (!id || id.startsWith("#")) continue;
      addOwnedShortId(id, namespace, out);
    }
  }
  return [...out].sort();
}

function addOwnedShortId(id: string, namespace: string, out: Set<string>): void {
  if (!id.includes(":")) {
    if (/^[a-z0-9_/.-]+$/.test(id)) out.add(id);
    return;
  }
  const [ns, path] = id.split(":", 2);
  if (ns === namespace && path && /^[a-z0-9_/.-]+$/.test(path)) {
    out.add(path);
  }
}

function deriveDescriptorVersion(zip: ZipArchive): string | undefined {
  const text = zip.readText("META-INF/neoforge.mods.toml") ?? zip.readText("META-INF/mods.toml");
  if (!text) return undefined;
  try {
    const parsed = Bun.TOML.parse(text) as unknown;
    if (!isRecord(parsed) || !Array.isArray(parsed.mods)) return undefined;
    for (const entry of parsed.mods) {
      if (!isRecord(entry)) continue;
      const version = usableVersion(readString(entry.version));
      if (version) return version;
    }
  } catch {
    return undefined;
  }
  return undefined;
}

function readString(value: unknown): string | undefined {
  return typeof value === "string" && value.length > 0 ? value : undefined;
}

function usableVersion(value: string | undefined): string | undefined {
  if (!value || value.includes("${")) return undefined;
  return value;
}

function escapeRegex(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return !!value && typeof value === "object" && !Array.isArray(value);
}
