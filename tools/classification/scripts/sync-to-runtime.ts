#!/usr/bin/env bun
/**
 * Promote freshly-classified `out/*.facets.complete.json` files into
 * the runtime resource paths the SLOT mod loads at boot. This is the
 * post-classify "ship it" step:
 *
 *   1. Vanilla — `out/minecraft.facets.complete.json` →
 *      `tools/classification/datasets/minecraft/minecraft.facets.complete.json`
 *      → run apply-vanilla-role-corrections.ts (which writes the
 *         corrections-overlaid file to BOTH the dataset path AND
 *         `common/.../classification/vanilla-base.json`).
 *   2. Modset — for every non-skipped entry in a manifest, copy
 *      `out/<modid>.facets.complete.json` →
 *      `common/.../classification/per-mod/<modid>.json`.
 *
 * Idempotent and safe: a file that doesn't exist in `out/` is reported
 * and skipped — the runtime resource for that mod stays as-is. Useful
 * when a partial reclassify only touched some mods.
 *
 * Run via the convenience aliases:
 *   bun run sync                  # everything (vanilla + every mod in default manifest)
 *   bun run sync:vanilla          # vanilla only
 *   bun run sync:test-modset      # default manifest's mods only
 *
 * Or directly:
 *   bun tools/classification/scripts/sync-to-runtime.ts \
 *     --vanilla \
 *     --modpack tools/classification/modpacks/test-modset.json
 */

import { copyFileSync, existsSync, readFileSync, readdirSync, writeFileSync } from "node:fs";
import { resolve } from "node:path";
import { spawnSync } from "node:child_process";
import { loadModpackManifest } from "../src/modpack.ts";

const REPO_ROOT = resolve(import.meta.dir, "..", "..", "..");
const OUT_DIR = resolve(REPO_ROOT, "tools/classification/out");
const PER_MOD_DIR = resolve(
  REPO_ROOT,
  "common/src/main/resources/data/slot/classification/per-mod",
);
const PER_MOD_INDEX = resolve(PER_MOD_DIR, "index.json");
const VANILLA_DATASET = resolve(
  REPO_ROOT,
  "tools/classification/datasets/minecraft/minecraft.facets.complete.json",
);
const VANILLA_OUT = resolve(OUT_DIR, "minecraft.facets.complete.json");
const VANILLA_CORRECTIONS_SCRIPT = resolve(
  REPO_ROOT,
  "tools/classification/scripts/apply-vanilla-role-corrections.ts",
);
const DEFAULT_MANIFEST = resolve(
  REPO_ROOT,
  "tools/classification/modpacks/test-modset.json",
);

interface SyncSummary {
  vanillaCopied: boolean;
  vanillaCorrectionsApplied: boolean;
  modsCopied: string[];
  modsMissing: string[];
  perModIndexUpdated: boolean;
}

function syncVanilla(summary: SyncSummary): void {
  if (!existsSync(VANILLA_OUT)) {
    console.log(`[sync] vanilla: out file missing (${VANILLA_OUT}); skipping`);
    return;
  }
  copyFileSync(VANILLA_OUT, VANILLA_DATASET);
  summary.vanillaCopied = true;
  console.log(`[sync] vanilla: ${VANILLA_OUT} → ${VANILLA_DATASET}`);

  // Run the corrections script — it overlays the systematic role
  // corrections (smithing templates, doors, beds, sherds, etc.) and
  // writes both the dataset and the runtime resource.
  console.log(`[sync] vanilla: running corrections...`);
  const result = spawnSync("bun", [VANILLA_CORRECTIONS_SCRIPT], {
    stdio: "inherit",
  });
  if (result.status !== 0) {
    throw new Error(
      `corrections script failed with exit code ${result.status}`,
    );
  }
  summary.vanillaCorrectionsApplied = true;
}

function syncModpack(manifestPath: string, summary: SyncSummary): void {
  const resolved = loadModpackManifest(manifestPath);
  console.log(`[sync] modpack: ${resolved.pack.name} (${manifestPath})`);

  for (const entry of resolved.pack.mods) {
    if (entry.skip) continue;
    const src = resolve(OUT_DIR, `${entry.namespace}.facets.complete.json`);
    const dst = resolve(PER_MOD_DIR, `${entry.namespace}.json`);
    if (!existsSync(src)) {
      summary.modsMissing.push(entry.namespace);
      console.log(`[sync] ${entry.namespace.padEnd(28)} MISSING in out/ — skip`);
      continue;
    }
    copyFileSync(src, dst);
    summary.modsCopied.push(entry.namespace);
    console.log(`[sync] ${entry.namespace.padEnd(28)} → ${dst}`);
  }
  updatePerModIndex(summary);
}

function updatePerModIndex(summary: SyncSummary): void {
  const mods = readdirSync(PER_MOD_DIR)
    .filter((name) => name.endsWith(".json") && name !== "index.json")
    .map((name) => name.slice(0, -".json".length))
    .filter((id) => /^[a-z0-9_]+$/.test(id))
    .sort();

  let description = [
    "List of per-mod classification layer files bundled with SLOT.",
    "FacetIndexBootstrap loads each one and merges its entries into the runtime FacetIndex on top of vanilla-base.",
    "Mods whose items aren't actually present at runtime simply won't be queried — no harm in shipping their data eagerly.",
  ].join(" ");
  if (existsSync(PER_MOD_INDEX)) {
    try {
      const existing = JSON.parse(readFileSync(PER_MOD_INDEX, "utf8")) as {
        description?: unknown;
      };
      if (typeof existing.description === "string") {
        description = existing.description;
      }
    } catch {
      // Overwrite malformed manifests with a valid regenerated one.
    }
  }

  writeFileSync(
    PER_MOD_INDEX,
    JSON.stringify(
      {
        schema_version: 1,
        description,
        mods,
      },
      null,
      2,
    ) + "\n",
  );
  summary.perModIndexUpdated = true;
  console.log(`[sync] per-mod index: ${mods.length} mod(s) → ${PER_MOD_INDEX}`);
}

function parseFlags(argv: string[]): {
  vanilla: boolean;
  modpack: string | null;
} {
  let vanilla = false;
  let modpack: string | null = null;
  let allRequested = false;
  for (let i = 2; i < argv.length; i++) {
    const arg = argv[i];
    if (arg === "--vanilla") {
      vanilla = true;
    } else if (arg === "--modpack") {
      modpack = argv[++i] ?? null;
    } else if (arg === "--all") {
      allRequested = true;
    } else if (arg === "-h" || arg === "--help") {
      console.log(
        "usage: sync-to-runtime.ts [--vanilla] [--modpack <path>] [--all]",
      );
      console.log("");
      console.log("Default (no flags) is equivalent to --all:");
      console.log("  --all          sync vanilla AND default manifest");
      console.log(`  --vanilla      sync vanilla only`);
      console.log(`  --modpack P    sync the named manifest (defaults to ${DEFAULT_MANIFEST})`);
      process.exit(0);
    }
  }
  if (!vanilla && modpack === null && !allRequested) {
    // Default = sync everything against the default manifest.
    return { vanilla: true, modpack: DEFAULT_MANIFEST };
  }
  if (allRequested) {
    return { vanilla: true, modpack: modpack ?? DEFAULT_MANIFEST };
  }
  return { vanilla, modpack };
}

function main(): void {
  const flags = parseFlags(process.argv);
  const summary: SyncSummary = {
    vanillaCopied: false,
    vanillaCorrectionsApplied: false,
    modsCopied: [],
    modsMissing: [],
    perModIndexUpdated: false,
  };

  if (flags.vanilla) {
    syncVanilla(summary);
  }
  if (flags.modpack) {
    syncModpack(flags.modpack, summary);
  }

  console.log("");
  console.log("=".repeat(64));
  console.log("Sync summary");
  console.log("=".repeat(64));
  if (flags.vanilla) {
    console.log(
      `  vanilla:        ${summary.vanillaCopied ? "copied" : "skipped"}` +
        (summary.vanillaCorrectionsApplied ? " + corrections applied" : ""),
    );
  }
  if (flags.modpack) {
    console.log(`  mods copied:    ${summary.modsCopied.length}`);
    if (summary.modsCopied.length) {
      console.log(`    ${summary.modsCopied.join(", ")}`);
    }
    if (summary.modsMissing.length) {
      console.log(`  mods missing:   ${summary.modsMissing.length}`);
      console.log(`    ${summary.modsMissing.join(", ")}`);
    }
    console.log(`  per-mod index:  ${summary.perModIndexUpdated ? "updated" : "unchanged"}`);
  }
}

main();
