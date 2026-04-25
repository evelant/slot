import {
  existsSync,
  readFileSync,
  readdirSync,
  statSync,
} from "node:fs";
import { basename, join, relative, resolve } from "node:path";
import type {
  ItemDefinitionJson,
  ModelJson,
  RecipeJson,
  LootTableJson,
  TagJson,
  SummaryBundle,
} from "../vanilla/source.ts";

/**
 * Build a SummaryBundle-shaped view of a single mod's resources by walking
 * its source tree directly — no jar build, no runtime registry inspection.
 *
 * Standard NeoForge / Forge mod layouts have two resource roots we read:
 *   - `src/main/resources/`        — hand-authored files (always present)
 *   - `src/generated/resources/`   — datagen output (most modern mods)
 *
 * For each root we collect:
 *   - `data/<modid>/{recipe,loot_table,advancement}/**.json`
 *   - `data/*\/tags/{item,block}/**.json`            — ALL namespaces, since
 *                                                       the mod can extend
 *                                                       `c:`, `minecraft:`,
 *                                                       `forge:`, sibling
 *                                                       mods' tags, etc.
 *   - `assets/<modid>/{lang,models,blockstates,items}/**.json`
 *
 * The mod's own item registry is **not data-driven** in vanilla Minecraft —
 * items are registered in Java code. We approximate the registry by reading
 * the lang file's `item.<modid>.<id>` and `block.<modid>.<id>` keys, which
 * every registered visible item has.
 *
 * Item-component data is NOT available from source (it's set in Java code
 * via DataComponents). The bundle's `itemComponents` is left empty — stage 2
 * boolean rules that read components will simply not fire for modded items.
 * That's an acceptable v1 limitation; later we can fill from a built jar
 * or from the runtime-crawl layer the plan describes.
 */
export interface ModSourceBundle extends SummaryBundle {
  /** The mod's primary namespace (e.g. `createaddition`). */
  modNamespace: string;
  /** Roots scanned, for diagnostic logging. */
  roots: string[];
}

export interface LoadModBundleOptions {
  /** Path to the mod source repo (`src/main/resources`'s parent's parent). */
  modPath: string;
  /** The mod's primary namespace, e.g. `createaddition`. */
  modNamespace: string;
}

/**
 * Walk a mod source tree and produce a SummaryBundle. The shape is
 * compatible with the vanilla `loadSummaryBundle` output, so downstream
 * extract/stage-2 code reuses without changes.
 */
export function loadModSourceBundle(options: LoadModBundleOptions): ModSourceBundle {
  const modPath = resolve(options.modPath);
  const modNs = options.modNamespace;
  if (!existsSync(modPath)) {
    throw new Error(`mod source path not found: ${modPath}`);
  }
  const candidateRoots = [
    join(modPath, "src", "main", "resources"),
    join(modPath, "src", "generated", "resources"),
    // Some mods (Toms-Storage) use platform-specific layout
    join(modPath, "src", "platform-shared", "resources"),
    join(modPath, "NeoForge", "src", "platform-shared", "resources"),
    join(modPath, "neoforge", "src", "main", "resources"),
    join(modPath, "neoforge", "src", "generated", "resources"),
    join(modPath, "forge", "src", "main", "resources"),
    join(modPath, "forge", "src", "generated", "resources"),
  ];
  const roots = candidateRoots.filter((p) => existsSync(p));
  if (roots.length === 0) {
    throw new Error(`no resource roots found under ${modPath}`);
  }

  const recipes: Record<string, RecipeJson> = {};
  const lootTables: Record<string, LootTableJson> = {};
  const itemTags: Record<string, TagJson> = {};
  const blockTags: Record<string, TagJson> = {};
  const itemDefinitions: Record<string, ItemDefinitionJson> = {};
  const models: Record<string, ModelJson> = {};
  const blocks: Record<string, unknown> = {};
  const lang: Record<string, Record<string, string>> = {};

  // ===== walk roots =====
  for (const root of roots) {
    // Mod-namespace recipes & loot tables (data/<modid>/recipe/**, data/<modid>/loot_table/**)
    collectJsonsByPath(
      join(root, "data", modNs, "recipe"),
      (relPath, json) => {
        const id = stripJsonExt(relPath);
        recipes[id] = json as RecipeJson;
      },
    );
    collectJsonsByPath(
      join(root, "data", modNs, "loot_table"),
      (relPath, json) => {
        const id = stripJsonExt(relPath);
        lootTables[id] = json as LootTableJson;
      },
    );

    // Tag walks across ALL namespaces so we capture cross-mod additions.
    // Output map keys are fully-qualified `<ns>:<short>` to disambiguate.
    walkTagDir(root, "item", (ns, short, json) => {
      itemTags[`${ns}:${short}`] = json;
    });
    walkTagDir(root, "block", (ns, short, json) => {
      blockTags[`${ns}:${short}`] = json;
    });

    // Mod-namespace assets
    collectJsonsByPath(
      join(root, "assets", modNs, "items"),
      (relPath, json) => {
        const id = stripJsonExt(relPath);
        itemDefinitions[id] = json as ItemDefinitionJson;
      },
    );
    collectJsonsByPath(
      join(root, "assets", modNs, "models"),
      (relPath, json) => {
        const id = stripJsonExt(relPath);
        models[id] = json as ModelJson;
      },
    );
    collectJsonsByPath(
      join(root, "assets", modNs, "blockstates"),
      (relPath, json) => {
        const id = stripJsonExt(relPath);
        blocks[id] = json;
      },
    );

    // Lang files. Pick en_us.json from any locale dir.
    const langFile = join(root, "assets", modNs, "lang", "en_us.json");
    if (existsSync(langFile)) {
      const data = readJsonSafe<Record<string, string>>(langFile);
      if (data) {
        // Merge later loads on top — generated typically wins over hand-authored.
        lang.en_us = { ...(lang.en_us ?? {}), ...data };
      }
    }
  }

  // Approximate the item registry from lang keys: `item.<modid>.<id>` and
  // `block.<modid>.<id>` give us the registered short ids.
  //
  // Mods (especially Create-family) use sub-keyed lang entries like
  // `item.<modid>.<id>.tooltip`, `item.<modid>.<id>.tooltip.behaviour1` to
  // hold translatable tooltip text. Real item ids never contain a `.` in the
  // short id (vanilla item ids are restricted to `[a-z0-9_/]`); filter
  // dotted captures out.
  const itemSet = new Set<string>();
  const enUs = lang.en_us ?? {};
  const langKeyRe = new RegExp(`^(item|block)\\.${escapeRegex(modNs)}\\.([a-z0-9_/]+)$`);
  for (const key of Object.keys(enUs)) {
    const m = key.match(langKeyRe);
    if (m) itemSet.add(m[2]!);
  }
  // Some items only show up in item-definition JSON (no localized name).
  for (const id of Object.keys(itemDefinitions)) itemSet.add(id);

  const itemList = [...itemSet].sort();

  return {
    modNamespace: modNs,
    roots,
    registries: { item: itemList, block: Object.keys(blocks).sort() },
    itemComponents: {}, // not available from source; left empty
    recipes,
    lootTables,
    itemTags,
    blockTags,
    itemDefinitions,
    models,
    lang,
    blocks,
    version: deriveVersion(roots),
  };
}

function deriveVersion(roots: string[]): string {
  // Try to read mods.toml or build.gradle for version. Best-effort.
  for (const root of roots) {
    const modsToml = join(root, "META-INF", "neoforge.mods.toml");
    if (existsSync(modsToml)) {
      const txt = readFileSync(modsToml, "utf8");
      const m = txt.match(/version\s*=\s*"([^"]+)"/);
      if (m) return m[1]!;
    }
    const oldToml = join(root, "META-INF", "mods.toml");
    if (existsSync(oldToml)) {
      const txt = readFileSync(oldToml, "utf8");
      const m = txt.match(/version\s*=\s*"([^"]+)"/);
      if (m) return m[1]!;
    }
  }
  return "unknown";
}

/**
 * Walk all `data/<ns>/tags/{item|block}/**.json` files under root, calling
 * `cb(ns, shortId, parsedJson)` for each. shortId is the path under the
 * tag-kind dir without `.json` extension.
 */
function walkTagDir(
  root: string,
  kind: "item" | "block",
  cb: (ns: string, shortId: string, json: TagJson) => void,
): void {
  const dataDir = join(root, "data");
  if (!existsSync(dataDir) || !statSync(dataDir).isDirectory()) return;
  for (const ns of readdirSync(dataDir)) {
    const tagDir = join(dataDir, ns, "tags", kind);
    if (!existsSync(tagDir) || !statSync(tagDir).isDirectory()) continue;
    walkRecursive(tagDir, (file) => {
      if (!file.endsWith(".json")) return;
      const json = readJsonSafe<TagJson>(file);
      if (!json) return;
      const short = stripJsonExt(relative(tagDir, file));
      cb(ns, short, json);
    });
  }
}

/** Walk a directory tree and call `cb(absolutePath)` for every regular file. */
function walkRecursive(root: string, cb: (file: string) => void): void {
  if (!existsSync(root)) return;
  for (const name of readdirSync(root)) {
    const full = join(root, name);
    const s = statSync(full);
    if (s.isDirectory()) walkRecursive(full, cb);
    else if (s.isFile()) cb(full);
  }
}

function collectJsonsByPath(
  root: string,
  cb: (relPath: string, json: unknown) => void,
): void {
  if (!existsSync(root)) return;
  walkRecursive(root, (file) => {
    if (!file.endsWith(".json")) return;
    const json = readJsonSafe<unknown>(file);
    if (json === null) return;
    cb(relative(root, file).replace(/\\/g, "/"), json);
  });
}

function readJsonSafe<T>(path: string): T | null {
  try {
    return JSON.parse(readFileSync(path, "utf8")) as T;
  } catch {
    return null;
  }
}

function stripJsonExt(relPath: string): string {
  return relPath.replace(/\.json$/, "");
}

function escapeRegex(s: string): string {
  return s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

void basename; // (silence unused-import warnings for any tree-shaking pass)
