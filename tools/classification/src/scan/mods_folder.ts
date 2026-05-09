import { existsSync, readFileSync, readdirSync, statSync } from "node:fs";
import { basename, dirname, join, resolve } from "node:path";
import {
  INPUT_MANIFEST_V2_SCHEMA_VERSION,
  type ClassificationLoader,
  type InputManifestMod,
  type InputManifestSource,
  type InputManifestV2,
  type PlatformIds,
} from "../input/manifest.ts";
import { scanJar, type JarPlatformMetadata } from "./jar.ts";

export interface ScanModsFolderOptions {
  requestedPath: string;
  generatedBy: string;
  bundledModIds?: ReadonlySet<string>;
}

export interface ResolvedModsFolder {
  requestedPath: string;
  modsPath: string;
  instanceRoot?: string;
  minecraftRoot?: string;
}

export function scanModsFolder(options: ScanModsFolderOptions): InputManifestV2 {
  const resolved = resolveModsFolder(options.requestedPath);
  const platformIndex = loadPrismIndexMetadata(resolved.modsPath);
  const source = buildSource(resolved);
  const jarPaths = readdirSync(resolved.modsPath)
    .filter((name) => name.endsWith(".jar"))
    .map((name) => join(resolved.modsPath, name))
    .filter((path) => statSync(path).isFile())
    .sort((a, b) => basename(a).localeCompare(basename(b)));

  const mods: InputManifestMod[] = [];
  for (const jarPath of jarPaths) {
    const platformMetadata = platformIndex.get(basename(jarPath));
    try {
      mods.push(...scanJar({
        path: jarPath,
        bundledModIds: options.bundledModIds,
        platformMetadata,
      }));
    } catch (err) {
      mods.push(malformedJarEntry(jarPath, platformMetadata, err));
    }
  }

  const statusCounts: Record<string, number> = {};
  let totalItemCandidates = 0;
  for (const mod of mods) {
    statusCounts[mod.status] = (statusCounts[mod.status] ?? 0) + 1;
    totalItemCandidates += mod.item_candidate_count;
  }

  return {
    kind: "slot-classification-input-manifest",
    schema_version: INPUT_MANIFEST_V2_SCHEMA_VERSION,
    generated_by: options.generatedBy,
    generated_at: new Date().toISOString(),
    source,
    summary: {
      total_jars: jarPaths.length,
      total_mod_entries: mods.length,
      total_item_candidates: totalItemCandidates,
      status_counts: Object.fromEntries(Object.entries(statusCounts).sort()),
    },
    mods,
  };
}

export function resolveModsFolder(inputPath: string): ResolvedModsFolder {
  const requestedPath = resolve(inputPath);
  const candidates = [
    requestedPath,
    join(requestedPath, "mods"),
    join(requestedPath, "minecraft", "mods"),
  ];
  for (const candidate of candidates) {
    if (!existsSync(candidate) || !statSync(candidate).isDirectory()) continue;
    const hasJars = readdirSync(candidate).some((name) => name.endsWith(".jar"));
    if (!hasJars) continue;
    const minecraftRoot = basename(candidate) === "mods" ? dirname(candidate) : undefined;
    const instanceRoot = minecraftRoot && basename(minecraftRoot) === "minecraft"
      ? dirname(minecraftRoot)
      : undefined;
    return {
      requestedPath,
      modsPath: candidate,
      ...(instanceRoot ? { instanceRoot } : {}),
      ...(minecraftRoot ? { minecraftRoot } : {}),
    };
  }
  throw new Error(`no mods folder with jar files found at ${requestedPath}`);
}

export function formatScanReport(report: InputManifestV2): string {
  const lines: string[] = [];
  lines.push(`Scan: ${report.source.resolved_mods_path ?? report.source.requested_path}`);
  if (report.source.pack_name || report.source.minecraft_version || report.source.loader) {
    const bits = [
      report.source.pack_name,
      report.source.pack_version ? `v${report.source.pack_version}` : undefined,
      report.source.minecraft_version ? `MC ${report.source.minecraft_version}` : undefined,
      report.source.loader && report.source.loader !== "unknown"
        ? `${report.source.loader}${report.source.loader_version ? ` ${report.source.loader_version}` : ""}`
        : undefined,
    ].filter(Boolean);
    if (bits.length > 0) lines.push(`Pack: ${bits.join(" | ")}`);
  }
  const kubejs = report.source.pack_scripts?.kubejs;
  if (kubejs && Object.values(kubejs).some((count) => count > 0)) {
    lines.push(
      `KubeJS: server=${kubejs.server_scripts}, startup=${kubejs.startup_scripts}, client=${kubejs.client_scripts}, data=${kubejs.data_files}, assets=${kubejs.asset_files}`,
    );
  }
  lines.push(`Jars: ${report.summary.total_jars}; mod entries: ${report.summary.total_mod_entries}; item candidates: ${report.summary.total_item_candidates}`);
  lines.push("Status:");
  for (const [status, count] of Object.entries(report.summary.status_counts)) {
    lines.push(`  ${status.padEnd(40)} ${count}`);
  }

  const actionable = report.mods
    .filter((mod) => mod.status === "missing:semantic-generation-available")
    .sort((a, b) => b.item_candidate_count - a.item_candidate_count)
    .slice(0, 20);
  if (actionable.length > 0) {
    lines.push("");
    lines.push("Largest missing semantic layers:");
    for (const mod of actionable) {
      const name = mod.display_name && mod.display_name !== mod.id ? ` (${mod.display_name})` : "";
      const platform = mod.platform_ids?.curseforge_project_id
        ? ` cf:${mod.platform_ids.curseforge_project_id}/${mod.platform_ids.curseforge_file_id ?? "?"}`
        : "";
      lines.push(`  ${mod.id.padEnd(28)} ${String(mod.item_candidate_count).padStart(5)} item candidates${name}${platform}`);
    }
  }

  const blocked = report.mods.filter((mod) => mod.status.startsWith("blocked:"));
  if (blocked.length > 0) {
    lines.push("");
    lines.push("Blocked jars:");
    for (const mod of blocked.slice(0, 20)) {
      lines.push(`  ${mod.file_name}: ${mod.diagnostics.join("; ") || mod.status}`);
    }
  }
  return lines.join("\n");
}

function buildSource(resolved: ResolvedModsFolder): InputManifestSource {
  const instanceCfg = resolved.instanceRoot ? readPrismInstanceCfg(join(resolved.instanceRoot, "instance.cfg")) : {};
  const mmcPack = resolved.instanceRoot ? readMmcPack(join(resolved.instanceRoot, "mmc-pack.json")) : {};
  const flameManifest = resolved.instanceRoot ? readFlameManifest(join(resolved.instanceRoot, "flame", "manifest.json")) : {};
  return {
    kind: "mods-folder",
    requested_path: resolved.requestedPath,
    resolved_mods_path: resolved.modsPath,
    ...(mmcPack.minecraftVersion || flameManifest.minecraftVersion
      ? { minecraft_version: mmcPack.minecraftVersion ?? flameManifest.minecraftVersion }
      : {}),
    ...(mmcPack.loader ? { loader: mmcPack.loader } : {}),
    ...(mmcPack.loaderVersion ? { loader_version: mmcPack.loaderVersion } : {}),
    ...(instanceCfg.ManagedPackName ? { pack_name: instanceCfg.ManagedPackName } : {}),
    ...(instanceCfg.ManagedPackVersionName ? { pack_version: instanceCfg.ManagedPackVersionName } : {}),
    ...(instanceCfg.ManagedPackType === "flame" ? { platform: "curseforge" as const } : { platform: "unknown" as const }),
    ...(instanceCfg.ManagedPackID ? { platform_project_id: Number(instanceCfg.ManagedPackID) } : {}),
    ...(instanceCfg.ManagedPackVersionID ? { platform_version_id: Number(instanceCfg.ManagedPackVersionID) } : {}),
    ...(resolved.minecraftRoot ? { pack_scripts: detectPackScripts(resolved.minecraftRoot) } : {}),
  };
}

export function loadPrismIndexMetadata(modsPath: string): Map<string, JarPlatformMetadata> {
  const indexDir = join(modsPath, ".index");
  const out = new Map<string, JarPlatformMetadata>();
  if (!existsSync(indexDir) || !statSync(indexDir).isDirectory()) return out;
  for (const file of readdirSync(indexDir).filter((name) => name.endsWith(".toml")).sort()) {
    const path = join(indexDir, file);
    let parsed: unknown;
    try {
      parsed = Bun.TOML.parse(readFileSync(path, "utf8"));
    } catch {
      continue;
    }
    if (!isRecord(parsed)) continue;
    const filename = readString(parsed.filename);
    if (!filename) continue;
    const platformIds: PlatformIds = {};
    const update = isRecord(parsed.update) ? parsed.update : {};
    const curseforge = isRecord(update.curseforge) ? update.curseforge : {};
    const modrinth = isRecord(update.modrinth) ? update.modrinth : {};
    const curseforgeProjectId = readNumber(curseforge["project-id"]);
    const curseforgeFileId = readNumber(curseforge["file-id"]);
    if (curseforgeProjectId !== undefined) platformIds.curseforge_project_id = curseforgeProjectId;
    if (curseforgeFileId !== undefined) platformIds.curseforge_file_id = curseforgeFileId;
    const modrinthProjectId = readString(modrinth["mod-id"]);
    const modrinthVersionId = readString(modrinth["version"]);
    if (modrinthProjectId) platformIds.modrinth_project_id = modrinthProjectId;
    if (modrinthVersionId) platformIds.modrinth_version_id = modrinthVersionId;
    out.set(filename, {
      displayName: readString(parsed.name),
      minecraftVersions: readStringArray(parsed["x-prismlauncher-mc-versions"]),
      loaders: readStringArray(parsed["x-prismlauncher-loaders"]),
      versionNumber: readString(parsed["x-prismlauncher-version-number"]),
      ...(Object.keys(platformIds).length > 0 ? { platformIds } : {}),
    });
  }
  return out;
}

function malformedJarEntry(
  jarPath: string,
  platformMetadata: JarPlatformMetadata | undefined,
  err: unknown,
): InputManifestMod {
  const id = basename(jarPath)
    .replace(/\.jar$/i, "")
    .toLowerCase()
    .replace(/[^a-z0-9_.-]+/g, "_");
  return {
    id,
    ...(platformMetadata?.displayName ? { display_name: platformMetadata.displayName } : {}),
    ...(platformMetadata?.versionNumber ? { version: platformMetadata.versionNumber } : {}),
    loader: inferLoader(platformMetadata),
    minecraft_versions: platformMetadata?.minecraftVersions ?? [],
    source_kind: "jar",
    path: jarPath,
    file_name: basename(jarPath),
    hashes: { sha1: "", sha512: "" },
    ...(platformMetadata?.platformIds ? { platform_ids: platformMetadata.platformIds } : {}),
    namespaces: [],
    resource_counts: {
      recipes: 0,
      loot_tables: 0,
      item_tags: 0,
      block_tags: 0,
      lang_files: 0,
      item_definitions: 0,
      item_models: 0,
      models: 0,
      blockstates: 0,
    },
    item_candidate_count: 0,
    item_set_signature: "sha256:",
    status: "blocked:malformed-jar",
    diagnostics: [err instanceof Error ? err.message : String(err)],
  };
}

function readPrismInstanceCfg(path: string): Record<string, string> {
  if (!existsSync(path)) return {};
  const out: Record<string, string> = {};
  for (const line of readFileSync(path, "utf8").split(/\r?\n/)) {
    const match = line.match(/^([^=#\[][^=]*)=(.*)$/);
    if (match) out[match[1]!.trim()] = match[2]!.trim();
  }
  return out;
}

function readMmcPack(path: string): {
  minecraftVersion?: string;
  loader?: ClassificationLoader;
  loaderVersion?: string;
} {
  if (!existsSync(path)) return {};
  try {
    const data = JSON.parse(readFileSync(path, "utf8")) as {
      components?: Array<{ uid?: string; version?: string }>;
    };
    const minecraft = data.components?.find((component) => component.uid === "net.minecraft");
    const forge = data.components?.find((component) => component.uid === "net.minecraftforge");
    const neoforge = data.components?.find((component) => component.uid === "net.neoforged");
    const fabric = data.components?.find((component) => component.uid === "net.fabricmc.fabric-loader");
    if (neoforge) return { minecraftVersion: minecraft?.version, loader: "neoforge", loaderVersion: neoforge.version };
    if (forge) return { minecraftVersion: minecraft?.version, loader: "forge", loaderVersion: forge.version };
    if (fabric) return { minecraftVersion: minecraft?.version, loader: "fabric", loaderVersion: fabric.version };
    return { minecraftVersion: minecraft?.version };
  } catch {
    return {};
  }
}

function readFlameManifest(path: string): { minecraftVersion?: string } {
  if (!existsSync(path)) return {};
  try {
    const data = JSON.parse(readFileSync(path, "utf8")) as {
      minecraft?: { version?: string };
    };
    return { minecraftVersion: data.minecraft?.version };
  } catch {
    return {};
  }
}

function detectPackScripts(minecraftRoot: string): InputManifestSource["pack_scripts"] | undefined {
  const kubejsRoot = join(minecraftRoot, "kubejs");
  if (!existsSync(kubejsRoot) || !statSync(kubejsRoot).isDirectory()) return undefined;
  return {
    kubejs: {
      server_scripts: countFiles(join(kubejsRoot, "server_scripts")),
      startup_scripts: countFiles(join(kubejsRoot, "startup_scripts")),
      client_scripts: countFiles(join(kubejsRoot, "client_scripts")),
      data_files: countFiles(join(kubejsRoot, "data")),
      asset_files: countFiles(join(kubejsRoot, "assets")),
    },
  };
}

function countFiles(path: string): number {
  if (!existsSync(path) || !statSync(path).isDirectory()) return 0;
  let total = 0;
  for (const name of readdirSync(path)) {
    const child = join(path, name);
    const stats = statSync(child);
    if (stats.isDirectory()) total += countFiles(child);
    else if (stats.isFile()) total++;
  }
  return total;
}

function inferLoader(metadata: JarPlatformMetadata | undefined): ClassificationLoader {
  const loaders = metadata?.loaders ?? [];
  if (loaders.includes("neoforge")) return "neoforge";
  if (loaders.includes("forge")) return "forge";
  if (loaders.includes("fabric")) return "fabric";
  if (loaders.includes("quilt")) return "quilt";
  return "unknown";
}

function readString(value: unknown): string | undefined {
  return typeof value === "string" && value.length > 0 ? value : undefined;
}

function readNumber(value: unknown): number | undefined {
  return typeof value === "number" && Number.isFinite(value) ? value : undefined;
}

function readStringArray(value: unknown): string[] {
  return Array.isArray(value) ? value.filter((item): item is string => typeof item === "string") : [];
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return !!value && typeof value === "object" && !Array.isArray(value);
}
