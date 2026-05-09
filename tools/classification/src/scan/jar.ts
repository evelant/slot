import { createHash } from "node:crypto";
import { readFileSync } from "node:fs";
import { basename } from "node:path";
import type {
  ClassificationLoader,
  InputManifestMod,
  PlatformIds,
  ResourceCounts,
  ScanStatus,
} from "../input/manifest.ts";
import { ZipArchive } from "./zip.ts";

export interface JarPlatformMetadata {
  displayName?: string;
  minecraftVersions?: string[];
  loaders?: string[];
  versionNumber?: string;
  platformIds?: PlatformIds;
}

export interface JarScanOptions {
  path: string;
  bundledModIds?: ReadonlySet<string>;
  platformMetadata?: JarPlatformMetadata;
}

interface DeclaredMod {
  id: string;
  displayName?: string;
  version?: string;
  loader: ClassificationLoader;
}

export function scanJar(options: JarScanOptions): InputManifestMod[] {
  const zip = ZipArchive.open(options.path);
  const fileName = basename(options.path);
  const data = readFileSync(options.path);
  const hashes = {
    sha1: createHash("sha1").update(data).digest("hex"),
    sha512: createHash("sha512").update(data).digest("hex"),
  };
  const names = zip.entryNames();
  const declaredMods = readDeclaredMods(zip);
  const namespaces = discoverNamespaces(names, declaredMods.map((mod) => mod.id));
  const resourceCounts = countResources(names);
  const ownedNamespaces = discoverOwnedNamespaces(names, declaredMods.map((mod) => mod.id));
  const itemCandidates = collectItemCandidates(zip, names, ownedNamespaces);
  const itemSetSignature = createHash("sha256")
    .update([...itemCandidates].sort().join("\n"))
    .digest("hex");
  const minecraftVersions = options.platformMetadata?.minecraftVersions ?? [];
  const platformIds = options.platformMetadata?.platformIds;
  const diagnostics: string[] = [];
  if (declaredMods.length === 0) {
    diagnostics.push("no Forge/NeoForge/Fabric mod descriptor found");
  }
  if (itemCandidates.size === 0) {
    diagnostics.push("no item candidates found from lang/model/recipe/loot/tag signals");
  }

  const mods = declaredMods.length > 0
    ? declaredMods
    : [{
        id: fallbackId(fileName),
        displayName: options.platformMetadata?.displayName,
        version: options.platformMetadata?.versionNumber,
        loader: inferLoaderFromPlatform(options.platformMetadata),
      }];

  return mods.map((mod) => {
    const version = usableVersion(mod.version) ?? options.platformMetadata?.versionNumber;
    const status = classifyStatus({
      modId: mod.id,
      itemCandidateCount: itemCandidates.size,
      bundledModIds: options.bundledModIds,
      hasDeclaredMod: declaredMods.length > 0,
    });
    const skippedReason = status === "skipped:library"
      ? "no item candidates found in jar resources"
      : undefined;
    return {
      id: mod.id,
      ...(mod.displayName ?? options.platformMetadata?.displayName
        ? { display_name: mod.displayName ?? options.platformMetadata?.displayName }
        : {}),
      ...(version ? { version } : {}),
      loader: mod.loader,
      minecraft_versions: minecraftVersions,
      source_kind: "jar",
      path: options.path,
      file_name: fileName,
      hashes,
      ...(platformIds && Object.keys(platformIds).length > 0 ? { platform_ids: platformIds } : {}),
      namespaces,
      resource_counts: resourceCounts,
      item_candidate_count: itemCandidates.size,
      item_set_signature: `sha256:${itemSetSignature}`,
      status,
      ...(skippedReason ? { skipped_reason: skippedReason } : {}),
      diagnostics,
    } satisfies InputManifestMod;
  });
}

function readDeclaredMods(zip: ZipArchive): DeclaredMod[] {
  const neoForge = readForgeToml(zip, "META-INF/neoforge.mods.toml", "neoforge");
  if (neoForge.length > 0) return neoForge;
  const forge = readForgeToml(zip, "META-INF/mods.toml", "forge");
  if (forge.length > 0) return forge;
  const fabric = zip.readJson<Record<string, unknown>>("fabric.mod.json");
  if (fabric) {
    const id = readString(fabric.id);
    if (id) {
      return [{
        id,
        displayName: readString(fabric.name),
        version: readString(fabric.version),
        loader: "fabric",
      }];
    }
  }
  return [];
}

function readForgeToml(
  zip: ZipArchive,
  path: string,
  loader: ClassificationLoader,
): DeclaredMod[] {
  const text = zip.readText(path);
  if (!text) return [];
  let parsed: unknown;
  try {
    parsed = Bun.TOML.parse(text);
  } catch {
    return [];
  }
  if (!isRecord(parsed) || !Array.isArray(parsed.mods)) return [];
  return parsed.mods.flatMap((entry): DeclaredMod[] => {
    if (!isRecord(entry)) return [];
    const id = readString(entry.modId);
    if (!id) return [];
    return [{
      id,
      displayName: readString(entry.displayName),
      version: readString(entry.version),
      loader,
    }];
  });
}

function discoverNamespaces(names: readonly string[], declaredModIds: readonly string[]): string[] {
  const namespaces = new Set<string>(declaredModIds.filter(Boolean));
  for (const name of names) {
    const match = name.match(/^(assets|data)\/([^/]+)\//);
    if (match && isNamespace(match[2]!)) {
      namespaces.add(match[2]!);
    }
  }
  return [...namespaces].sort();
}

function discoverOwnedNamespaces(names: readonly string[], declaredModIds: readonly string[]): Set<string> {
  const namespaces = new Set<string>(declaredModIds.filter(Boolean));
  for (const name of names) {
    const match = name.match(/^assets\/([^/]+)\/(lang\/en_us\.json|items\/|models\/item\/|blockstates\/)/);
    if (match && isNamespace(match[1]!)) {
      namespaces.add(match[1]!);
    }
  }
  return namespaces;
}

function countResources(names: readonly string[]): ResourceCounts {
  return {
    recipes: count(names, /^data\/[^/]+\/recipes?\//),
    loot_tables: count(names, /^data\/[^/]+\/loot_tables?\//),
    item_tags: count(names, /^data\/[^/]+\/tags\/items?\//),
    block_tags: count(names, /^data\/[^/]+\/tags\/blocks?\//),
    lang_files: count(names, /^assets\/[^/]+\/lang\/en_us\.json$/),
    item_definitions: count(names, /^assets\/[^/]+\/items\/.*\.json$/),
    item_models: count(names, /^assets\/[^/]+\/models\/item\/.*\.json$/),
    models: count(names, /^assets\/[^/]+\/models\/.*\.json$/),
    blockstates: count(names, /^assets\/[^/]+\/blockstates\/.*\.json$/),
  };
}

function collectItemCandidates(
  zip: ZipArchive,
  names: readonly string[],
  ownedNamespaces: ReadonlySet<string>,
): Set<string> {
  const out = new Set<string>();
  for (const name of names) {
    if (/^assets\/[^/]+\/lang\/en_us\.json$/.test(name)) {
      collectFromLang(zip.readJson<Record<string, unknown>>(name), out, ownedNamespaces);
    } else if (/^assets\/[^/]+\/items\/.*\.json$/.test(name)) {
      const match = name.match(/^assets\/([^/]+)\/items\/(.+)\.json$/);
      if (match) addOwnedCandidate(`${match[1]}:${match[2]}`, out, ownedNamespaces);
    } else if (/^assets\/[^/]+\/models\/item\/.*\.json$/.test(name)) {
      const match = name.match(/^assets\/([^/]+)\/models\/item\/(.+)\.json$/);
      if (match) addOwnedCandidate(`${match[1]}:${match[2]}`, out, ownedNamespaces);
    } else if (/^data\/[^/]+\/recipes?\/.*\.json$/.test(name)) {
      collectNamespacedValues(zip.readJson<unknown>(name), out, ["id", "item", "name"], ownedNamespaces);
    } else if (/^data\/[^/]+\/loot_tables?\/.*\.json$/.test(name)) {
      collectNamespacedValues(zip.readJson<unknown>(name), out, ["name"], ownedNamespaces);
    } else if (/^data\/[^/]+\/tags\/(items?|blocks?)\/.*\.json$/.test(name)) {
      collectTagValues(zip.readJson<unknown>(name), out, ownedNamespaces);
    }
  }
  return out;
}

function collectFromLang(
  lang: Record<string, unknown> | null,
  out: Set<string>,
  ownedNamespaces: ReadonlySet<string>,
): void {
  if (!lang) return;
  const keyPattern = /^(item|block)\.([a-z0-9_.-]+)\.([a-z0-9_/.-]+)$/;
  for (const key of Object.keys(lang)) {
    const match = key.match(keyPattern);
    if (!match) continue;
    addOwnedCandidate(`${match[2]}:${match[3]}`, out, ownedNamespaces);
  }
}

function collectTagValues(
  value: unknown,
  out: Set<string>,
  ownedNamespaces: ReadonlySet<string>,
): void {
  if (!isRecord(value) || !Array.isArray(value.values)) return;
  for (const entry of value.values) {
    const id = typeof entry === "string"
      ? entry
      : isRecord(entry)
        ? readString(entry.id)
        : undefined;
    if (id && !id.startsWith("#")) {
      addOwnedCandidate(id, out, ownedNamespaces);
    }
  }
}

function collectNamespacedValues(
  value: unknown,
  out: Set<string>,
  keys: readonly string[],
  ownedNamespaces: ReadonlySet<string>,
): void {
  if (typeof value === "string") {
    addOwnedCandidate(value, out, ownedNamespaces);
    return;
  }
  if (Array.isArray(value)) {
    for (const item of value) collectNamespacedValues(item, out, keys, ownedNamespaces);
    return;
  }
  if (!isRecord(value)) return;
  for (const key of keys) {
    const candidate = readString(value[key]);
    if (candidate) addOwnedCandidate(candidate, out, ownedNamespaces);
  }
  for (const child of Object.values(value)) {
    collectNamespacedValues(child, out, keys, ownedNamespaces);
  }
}

function addOwnedCandidate(
  id: string,
  out: Set<string>,
  ownedNamespaces: ReadonlySet<string>,
): void {
  if (!isItemId(id)) return;
  const namespace = id.slice(0, id.indexOf(":"));
  if (ownedNamespaces.has(namespace)) {
    out.add(id);
  }
}

function classifyStatus(args: {
  modId: string;
  itemCandidateCount: number;
  bundledModIds?: ReadonlySet<string>;
  hasDeclaredMod: boolean;
}): ScanStatus {
  if (!args.hasDeclaredMod && args.itemCandidateCount > 0) return "blocked:ambiguous-mod-id";
  if (args.bundledModIds?.has(args.modId)) return "covered:bundled";
  if (args.itemCandidateCount === 0) return "skipped:library";
  return "missing:semantic-generation-available";
}

function inferLoaderFromPlatform(metadata: JarPlatformMetadata | undefined): ClassificationLoader {
  const loaders = metadata?.loaders ?? [];
  if (loaders.includes("neoforge")) return "neoforge";
  if (loaders.includes("forge")) return "forge";
  if (loaders.includes("fabric")) return "fabric";
  if (loaders.includes("quilt")) return "quilt";
  return "unknown";
}

function count(values: readonly string[], pattern: RegExp): number {
  return values.reduce((total, value) => total + (pattern.test(value) ? 1 : 0), 0);
}

function readString(value: unknown): string | undefined {
  return typeof value === "string" && value.length > 0 ? value : undefined;
}

function usableVersion(value: string | undefined): string | undefined {
  if (!value || value.includes("${")) return undefined;
  return value;
}

function fallbackId(fileName: string): string {
  return fileName
    .replace(/\.jar$/i, "")
    .toLowerCase()
    .replace(/[^a-z0-9_.-]+/g, "_")
    .replace(/^[_.-]+|[_.-]+$/g, "")
    || "unknown";
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return !!value && typeof value === "object" && !Array.isArray(value);
}

function isNamespace(value: string): boolean {
  return /^[a-z0-9_.-]+$/.test(value);
}

function isItemId(value: string): boolean {
  return /^[a-z0-9_.-]+:[a-z0-9_/.-]+$/.test(value);
}
