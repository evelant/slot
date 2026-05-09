/**
 * Modpack-level classification orchestration.
 *
 * Reads a manifest at `tools/classification/modpacks/<name>.json` and
 * runs `runMod(...)` for each entry that
 *   (a) isn't marked `skip`, and
 *   (b) doesn't already have a populated `<modid>.facets.complete.json`
 *       in the output directory.
 *
 * Per-mod failures don't abort the whole pack — they're collected into a
 * summary printed at the end alongside completed-mod stats.
 */

import { readFileSync, existsSync } from "node:fs";
import { resolve, isAbsolute, dirname, join } from "node:path";
import { validateLayer } from "./schema/validate.ts";

export interface ModpackEntry {
  /** Mod namespace as it appears in the registry (e.g. `create`,
   *  `sophisticatedstorage`). Used as the modid for runMod and the
   *  output filename prefix. */
  namespace: string;
  /** Human-readable name for log output. */
  displayName: string;
  /** Path to the cloned mod source repo. May be relative to the manifest
   *  file's directory (or to the repo root) or absolute. Required unless
   *  `skip` is set. */
  sourcePath?: string;
  /** When set, the entry is skipped — value is the reason printed in the
   *  log. Used for libraries / utility mods that have no item content
   *  worth classifying. */
  skip?: string;
}

export interface Modpack {
  name: string;
  description?: string;
  mods: readonly ModpackEntry[];
}

/** Parsed-and-resolved view of a modpack manifest. */
export interface ResolvedModpack {
  manifestPath: string;
  pack: Modpack;
  /** Directory the manifest lives in — used to resolve relative
   *  `sourcePath` entries. */
  manifestDir: string;
}

export function loadModpackManifest(manifestPath: string): ResolvedModpack {
  const absPath = resolve(manifestPath);
  if (!existsSync(absPath)) {
    throw new Error(`modpack manifest not found: ${absPath}`);
  }
  const raw = readFileSync(absPath, "utf8");
  let parsed: unknown;
  try {
    parsed = JSON.parse(raw);
  } catch (err) {
    throw new Error(`modpack manifest is not valid JSON (${absPath}): ${(err as Error).message}`);
  }
  const pack = parsed as Modpack;
  if (!pack || typeof pack !== "object") {
    throw new Error(`modpack manifest must be a JSON object: ${absPath}`);
  }
  if (!Array.isArray(pack.mods) || pack.mods.length === 0) {
    throw new Error(`modpack manifest has no \`mods\` array: ${absPath}`);
  }
  for (const entry of pack.mods) {
    if (!entry || typeof entry !== "object") {
      throw new Error(`modpack manifest contains a non-object entry: ${absPath}`);
    }
    if (typeof entry.namespace !== "string" || !entry.namespace) {
      throw new Error(`modpack entry is missing \`namespace\`: ${JSON.stringify(entry)}`);
    }
    if (typeof entry.displayName !== "string" || !entry.displayName) {
      throw new Error(`modpack entry is missing \`displayName\`: ${entry.namespace}`);
    }
    if (!entry.skip && (typeof entry.sourcePath !== "string" || !entry.sourcePath)) {
      throw new Error(
        `modpack entry \`${entry.namespace}\` must have \`sourcePath\` (or \`skip: "reason"\`)`,
      );
    }
  }
  return { manifestPath: absPath, pack, manifestDir: dirname(absPath) };
}

/**
 * Resolve a manifest's `sourcePath` to an absolute filesystem path. The
 * manifest may use a path relative to the manifest itself or to the repo
 * root (we walk up looking for a `tools/classification/` ancestor).
 */
export function resolveModSource(manifestDir: string, sourcePath: string): string {
  if (isAbsolute(sourcePath)) return sourcePath;
  // Try manifest-relative first.
  const local = resolve(manifestDir, sourcePath);
  if (existsSync(local)) return local;
  // Fall back to repo-root-relative — manifests live under
  // `tools/classification/modpacks/`, so the repo root is two levels up
  // from the manifest dir.
  const repoRoot = repoRootFrom(manifestDir);
  if (repoRoot) {
    const fromRoot = resolve(repoRoot, sourcePath);
    if (existsSync(fromRoot)) return fromRoot;
  }
  // Return the manifest-relative form regardless so the caller's error
  // message points at where we expected to find it.
  return local;
}

/**
 * Walk parent directories looking for a marker that identifies the repo
 * root (`tools/classification/` exists). Returns null when not found.
 */
function repoRootFrom(start: string): string | null {
  let cur = start;
  for (let depth = 0; depth < 6; depth++) {
    if (existsSync(join(cur, "tools/classification"))) return cur;
    const parent = dirname(cur);
    if (parent === cur) break;
    cur = parent;
  }
  return null;
}

export interface ModpackProcessingDecision {
  entry: ModpackEntry;
  decision: "skipped:library" | "skipped:already-classified" | "process";
  reason?: string;
  /** Resolved source path for `process` decisions; undefined for skips. */
  sourcePath?: string;
  /** Existing complete-output path (whether populated or not). */
  completePath: string;
  /** When already classified, how many entries the existing file has. */
  entryCount?: number;
}

/**
 * Decide whether each manifest entry needs classification, given the
 * current state of `outDir`. Returns one decision per entry in manifest
 * order.
 */
export function planModpack(
  resolved: ResolvedModpack,
  outDir: string,
): ModpackProcessingDecision[] {
  return resolved.pack.mods.map((entry) => {
    const completePath = resolve(outDir, `${entry.namespace}.facets.complete.json`);
    if (entry.skip) {
      return { entry, decision: "skipped:library", reason: entry.skip, completePath };
    }
    if (existsSync(completePath)) {
      const reusable = inspectReusableCompleteOutput(completePath);
      if (!reusable.ok) {
        return {
          entry,
          decision: "process",
          reason: reusable.reason,
          sourcePath: resolveModSource(resolved.manifestDir, entry.sourcePath!),
          completePath,
        };
      }
      if (reusable.entryCount > 0) {
        return {
          entry,
          decision: "skipped:already-classified",
          reason: `${reusable.entryCount} entries`,
          entryCount: reusable.entryCount,
          completePath,
        };
      }
      return {
        entry,
        decision: "process",
        reason: "existing complete output has no entries",
        sourcePath: resolveModSource(resolved.manifestDir, entry.sourcePath!),
        completePath,
      };
    }
    return {
      entry,
      decision: "process",
      sourcePath: resolveModSource(resolved.manifestDir, entry.sourcePath!),
      completePath,
    };
  });
}

export function inspectReusableCompleteOutput(path: string):
  | { ok: true; entryCount: number }
  | { ok: false; reason: string } {
  let data: unknown;
  try {
    data = JSON.parse(readFileSync(path, "utf8"));
  } catch (err) {
    return {
      ok: false,
      reason: `existing complete output is not valid JSON: ${(err as Error).message}`,
    };
  }
  const validation = validateLayer(data);
  if (!validation.ok) {
    return {
      ok: false,
      reason: `existing complete output failed schema validation: ${validation.errors.slice(0, 3).join("; ")}`,
    };
  }
  const entries = (data as { entries?: Record<string, unknown> }).entries ?? {};
  return { ok: true, entryCount: Object.keys(entries).length };
}

export interface ModpackRunSummary {
  pack: string;
  total: number;
  skipped: number;
  alreadyClassified: number;
  processed: number;
  failed: number;
  failures: Array<{ namespace: string; error: string }>;
  /** Wall-clock seconds for the whole pack (incl. skips, which are O(ms)). */
  elapsedSeconds: number;
}

/** Pretty-print a summary for end-of-run. */
export function formatRunSummary(s: ModpackRunSummary): string {
  const lines: string[] = [];
  lines.push("");
  lines.push("=".repeat(72));
  lines.push(`Modpack run complete: ${s.pack}`);
  lines.push("=".repeat(72));
  lines.push(`Total mods:           ${s.total}`);
  lines.push(`  skipped (library):  ${s.skipped}`);
  lines.push(`  already classified: ${s.alreadyClassified}`);
  lines.push(`  processed:          ${s.processed}`);
  lines.push(`  failed:             ${s.failed}`);
  lines.push(`Wall time:            ${s.elapsedSeconds.toFixed(1)}s`);
  if (s.failures.length > 0) {
    lines.push("");
    lines.push("Failures:");
    for (const f of s.failures) {
      lines.push(`  - ${f.namespace}: ${f.error.slice(0, 200)}`);
    }
  }
  lines.push("=".repeat(72));
  return lines.join("\n");
}
