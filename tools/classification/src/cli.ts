import { existsSync, mkdirSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { parseArgs } from "node:util";
import {
  ensureVanillaSource,
  loadSummaryBundle,
  type SummaryBundle,
} from "./extract/vanilla/source.ts";
import {
  extractFromBundle,
  VANILLA_NAMESPACE,
} from "./extract/vanilla/extractor.ts";
import type { ItemExtractRecord } from "./extract/record.ts";
import { loadModSourceBundle } from "./extract/mod/source.ts";
import { extractFromModBundle } from "./extract/mod/extractor.ts";
import { runDeterministic, type LayerFile } from "./deterministic/run.ts";
import { validateLayer, validateLayerFile } from "./schema/validate.ts";
import {
  ClaudeCliClient,
  RecordingLlmClient,
  ReplayLlmClient,
  type LlmClient,
} from "./llm/client.ts";
import { OpenRouterClient } from "./llm/openrouter-client.ts";
import { runStage3 } from "./llm/run.ts";
import { runStage3Retry, selectRetryCandidates } from "./llm/retry.ts";
import {
  buildBatchPrompt,
  buildItemPayload,
  defaultTargetFacets,
} from "./llm/prompt.ts";
import { VANILLA_CANARY_ITEMS } from "./llm/canary.ts";
import {
  extractModMetadata,
  proposeSubsystems,
  type SubsystemEntry,
} from "./llm/mod_metadata.ts";
import {
  loadModpackManifest,
  planModpack,
  formatRunSummary,
  type ModpackProcessingDecision,
  type ModpackRunSummary,
  type ResolvedModpack,
} from "./modpack.ts";

const TOOL_VERSION = "slot-classify v0.1.0";

interface StageSelection {
  stage1: boolean;
  stage2: boolean;
  stage3: boolean;
}

/**
 * Assemble the QueryOptions slice the LLM client needs (effort + thinking
 * budget + adaptive-thinking flag). Returns undefined when nothing is set
 * so we don't pass an empty object through the pipeline.
 */
function buildClientOptions(
  effort: "low" | "medium" | "high" | "xhigh" | "max" | undefined,
  thinkingBudget: number | undefined,
  disableAdaptiveThinking: boolean | undefined,
): { effort?: "low" | "medium" | "high" | "xhigh" | "max"; thinkingBudget?: number; disableAdaptiveThinking?: boolean } | undefined {
  const out: ReturnType<typeof buildClientOptions> = {};
  if (effort) out!.effort = effort;
  if (thinkingBudget !== undefined) out!.thinkingBudget = thinkingBudget;
  if (disableAdaptiveThinking) out!.disableAdaptiveThinking = true;
  return Object.keys(out!).length > 0 ? out : undefined;
}

function parseEffort(
  input: string | undefined,
): "low" | "medium" | "high" | "xhigh" | "max" | undefined {
  if (!input) return undefined;
  const allowed = ["low", "medium", "high", "xhigh", "max"] as const;
  if (!(allowed as readonly string[]).includes(input)) {
    throw new Error(`unknown --effort value: ${input} (use one of ${allowed.join("|")})`);
  }
  return input as "low" | "medium" | "high" | "xhigh" | "max";
}

function parseBackend(input: string | undefined): "claude-cli" | "openrouter" | undefined {
  if (!input) return undefined;
  const allowed = ["claude-cli", "openrouter"] as const;
  if (!(allowed as readonly string[]).includes(input)) {
    throw new Error(`unknown --backend value: ${input} (use one of ${allowed.join("|")})`);
  }
  return input as "claude-cli" | "openrouter";
}

function parseStages(input: string | undefined): StageSelection {
  if (!input) return { stage1: true, stage2: true, stage3: false };
  const set = new Set(input.split(",").map((s) => s.trim()));
  const known = new Set(["1", "2", "3"]);
  for (const s of set) {
    if (!known.has(s)) throw new Error(`unknown stage: ${s}`);
  }
  return { stage1: set.has("1"), stage2: set.has("2"), stage3: set.has("3") };
}

// Bun's default behavior on an unhandled promise rejection is to terminate
// the process. The OpenRouter SDK has internal retry / matcher machinery
// that occasionally fires non-awaited promises (e.g. on truncated upstream
// JSON bodies); when those reject they bypass our `sendWithRetry` guard.
// Logging and continuing is safe — the awaited code path either retries
// successfully or surfaces the error to `runModpack`, which catches per-mod
// failures.
process.on("unhandledRejection", (reason) => {
  const msg = reason instanceof Error ? reason.message : String(reason);
  console.warn(`[unhandled-rejection] ${msg.slice(0, 200)} (continuing)`);
});

async function main() {
  const [cmd, ...rest] = Bun.argv.slice(2);

  switch (cmd) {
    case undefined:
    case "-h":
    case "--help":
      printHelp();
      return;

    case "classify":
    case "extract": {
      const args = parseArgs({
        args: rest,
        options: {
          mod: { type: "string" },
          source: { type: "string" },
          out: { type: "string" },
          stages: { type: "string" },
          // stage 3 knobs
          model: { type: "string" },
          backend: { type: "string" }, // claude-cli (default) | openrouter
          "ignore-provider": { type: "string", multiple: true },
          "only-provider": { type: "string", multiple: true },
          "batch-size": { type: "string" },
          concurrency: { type: "string" },
          effort: { type: "string" },
          "thinking-budget": { type: "string" },
          "disable-adaptive-thinking": { type: "boolean" },
          sample: { type: "string" }, // `canary`, `N`, or comma-separated ids
          "fixture-dir": { type: "string" },
          "use-replay": { type: "boolean" },
          "record-replay": { type: "boolean" },
          "dry-run": { type: "boolean" },
          // stage 3 retry knobs — applied after the first pass on any item
          // whose LLM facets have confidence < retry-threshold or ambiguous:true.
          "retry-model": { type: "string" },
          "retry-effort": { type: "string" },
          "retry-threshold": { type: "string" },
          "retry-batch-size": { type: "string" },
          "retry-fixture-dir": { type: "string" },
          "retry-use-replay": { type: "boolean" },
          "retry-record-replay": { type: "boolean" },
          // mod-only: bootstrap a canonical mod_subsystem vocabulary from the
          // mod's README/metadata before stage 3 runs.
          "no-propose-subsystems": { type: "boolean" },
          "subsystems-model": { type: "string" },
          // verbose prompt extras. disambiguation defaults ON (principle-
          // based per-facet reasoning; carries production accuracy on hard
          // categories from ~50% → ~93%). misconceptions defaults OFF
          // (item-level enumeration kept in reserve for regressions).
          // Pass `--no-verbose-disambiguation` to flip off, or
          // `--verbose-misconceptions` to flip on.
          "verbose-disambiguation": { type: "boolean" },
          "no-verbose-disambiguation": { type: "boolean" },
          "verbose-misconceptions": { type: "boolean" },
        },
        allowPositionals: false,
        strict: true,
      });
      const mod = args.values.mod;
      const sourcePath = args.values.source;
      if (!mod || !sourcePath) {
        console.error("usage: classify --mod <id> --source <path> [--out <dir>] [--stages 1,2,3]");
        process.exit(2);
      }
      const outDir = resolve(args.values.out ?? "out");
      mkdirSync(outDir, { recursive: true });

      const stages = parseStages(args.values.stages);

      const stage3CliOpts: Stage3CliOptions = {
        model: args.values.model,
        backend: parseBackend(args.values.backend),
        ignoredProviders: (args.values["ignore-provider"] as string[] | undefined) ?? undefined,
        onlyProviders: (args.values["only-provider"] as string[] | undefined) ?? undefined,
        batchSize: args.values["batch-size"] ? Number(args.values["batch-size"]) : undefined,
        concurrency: args.values.concurrency ? Number(args.values.concurrency) : undefined,
        effort: parseEffort(args.values.effort),
        thinkingBudget: args.values["thinking-budget"]
          ? Number(args.values["thinking-budget"])
          : undefined,
        disableAdaptiveThinking: args.values["disable-adaptive-thinking"] ?? false,
        sample: args.values.sample,
        fixtureDir: args.values["fixture-dir"],
        useReplay: args.values["use-replay"] ?? false,
        recordReplay: args.values["record-replay"] ?? false,
        dryRun: args.values["dry-run"] ?? false,
        retryModel: args.values["retry-model"],
        retryEffort: parseEffort(args.values["retry-effort"]),
        retryThreshold: args.values["retry-threshold"]
          ? Number(args.values["retry-threshold"])
          : undefined,
        retryBatchSize: args.values["retry-batch-size"]
          ? Number(args.values["retry-batch-size"])
          : undefined,
        retryFixtureDir: args.values["retry-fixture-dir"],
        retryUseReplay: args.values["retry-use-replay"],
        retryRecordReplay: args.values["retry-record-replay"],
        proposeSubsystems: !(args.values["no-propose-subsystems"] ?? false),
        subsystemsModel: args.values["subsystems-model"],
        // disambiguation defaults ON; --no-verbose-disambiguation flips off.
        // --verbose-disambiguation is a no-op (always-on by default) — kept
        // for symmetry / discoverability.
        verboseFacetDisambiguation: args.values["no-verbose-disambiguation"]
          ? false
          : (args.values["verbose-disambiguation"] ?? true),
        verboseCommonMisconceptions: args.values["verbose-misconceptions"] ?? false,
      };

      if (mod === "minecraft") {
        await runVanilla(sourcePath!, outDir, stages, stage3CliOpts);
        return;
      }
      // Any other mod id → mod-source extractor (createaddition, mekanism, …).
      // The source path should be the mod's repo root; the loader walks
      // src/main/resources + src/generated/resources for the given namespace.
      await runMod(mod!, sourcePath!, outDir, stages, stage3CliOpts);
      return;
    }

    case "classify-modpack": {
      const args = parseArgs({
        args: rest,
        options: {
          out: { type: "string" },
          stages: { type: "string" },
          // stage 3 knobs (same surface as `classify`).
          model: { type: "string" },
          backend: { type: "string" },
          "ignore-provider": { type: "string", multiple: true },
          "only-provider": { type: "string", multiple: true },
          "batch-size": { type: "string" },
          concurrency: { type: "string" },
          "mod-concurrency": { type: "string" },
          effort: { type: "string" },
          "thinking-budget": { type: "string" },
          "disable-adaptive-thinking": { type: "boolean" },
          "fixture-dir": { type: "string" },
          "use-replay": { type: "boolean" },
          "record-replay": { type: "boolean" },
          "dry-run": { type: "boolean" },
          "no-propose-subsystems": { type: "boolean" },
          "subsystems-model": { type: "string" },
          "verbose-disambiguation": { type: "boolean" },
          "no-verbose-disambiguation": { type: "boolean" },
          "verbose-misconceptions": { type: "boolean" },
          force: { type: "boolean" },
          "force-subsystems": { type: "boolean" },
        },
        allowPositionals: true,
        strict: true,
      });
      const manifestPath = args.positionals[0];
      if (!manifestPath) {
        console.error("usage: classify-modpack <manifest.json> [options]");
        process.exit(2);
        return;
      }
      const outDir = resolve(args.values.out ?? "out");
      mkdirSync(outDir, { recursive: true });

      const stages = parseStages(args.values.stages);

      const stage3CliOpts: Stage3CliOptions = {
        model: args.values.model,
        backend: parseBackend(args.values.backend),
        ignoredProviders: (args.values["ignore-provider"] as string[] | undefined) ?? undefined,
        onlyProviders: (args.values["only-provider"] as string[] | undefined) ?? undefined,
        batchSize: args.values["batch-size"] ? Number(args.values["batch-size"]) : undefined,
        concurrency: args.values.concurrency ? Number(args.values.concurrency) : undefined,
        effort: parseEffort(args.values.effort),
        thinkingBudget: args.values["thinking-budget"]
          ? Number(args.values["thinking-budget"])
          : undefined,
        disableAdaptiveThinking: args.values["disable-adaptive-thinking"] ?? false,
        fixtureDir: args.values["fixture-dir"],
        useReplay: args.values["use-replay"] ?? false,
        recordReplay: args.values["record-replay"] ?? false,
        dryRun: args.values["dry-run"] ?? false,
        proposeSubsystems: !(args.values["no-propose-subsystems"] ?? false),
        subsystemsModel: args.values["subsystems-model"],
        verboseFacetDisambiguation: args.values["no-verbose-disambiguation"]
          ? false
          : (args.values["verbose-disambiguation"] ?? true),
        verboseCommonMisconceptions: args.values["verbose-misconceptions"] ?? false,
      };

      const modpackOpts: ModpackRunOptions = {
        force: args.values.force ?? false,
        forceSubsystems: args.values["force-subsystems"] ?? false,
        modConcurrency: args.values["mod-concurrency"]
          ? Math.max(1, Number(args.values["mod-concurrency"]))
          : 1,
      };

      await runModpack(manifestPath, outDir, stages, stage3CliOpts, modpackOpts);
      return;
    }

    case "validate": {
      const args = parseArgs({
        args: rest,
        options: {},
        allowPositionals: true,
        strict: true,
      });
      const target = args.positionals[0];
      if (!target) {
        console.error("usage: validate <layer.json>");
        process.exit(2);
        return;
      }
      const result = validateLayerFile(resolve(target));
      if (result.ok) {
        console.log(`ok: ${target}`);
        return;
      }
      console.error(`invalid: ${target}`);
      for (const err of result.errors) console.error(`  ${err}`);
      process.exit(1);
      return;
    }

    default:
      console.error(`unknown command: ${cmd}`);
      printHelp();
      process.exit(2);
  }
}

interface Stage3CliOptions {
  model?: string;
  /** Live backend selector. `claude-cli` (default) shells out to
   *  `claude -p`. `openrouter` calls the OpenRouter SDK with
   *  `OPENROUTER_API_KEY` from env. Replay mode bypasses both. */
  backend?: "claude-cli" | "openrouter";
  /** Provider slugs to exclude from OpenRouter routing (e.g.
   *  `["deepinfra"]`). Forwarded as `provider.ignore` per request.
   *  Useful when an upstream provider is rate-limited or returning
   *  flaky responses for our prompt shape. Ignored on claude-cli. */
  ignoredProviders?: readonly string[];
  /** Provider slugs to **pin** OpenRouter to (e.g. `["deepseek"]`).
   *  Forwarded as `provider.only` + `allow_fallbacks: false`. Takes
   *  precedence over ignoredProviders. Ignored on claude-cli. */
  onlyProviders?: readonly string[];
  batchSize?: number;
  concurrency?: number;
  effort?: "low" | "medium" | "high" | "xhigh" | "max";
  thinkingBudget?: number;
  disableAdaptiveThinking?: boolean;
  sample?: string;
  fixtureDir?: string;
  useReplay: boolean;
  recordReplay: boolean;
  dryRun: boolean;
  /** If set, run a retry pass with this model after the first pass.
   *  Note: the retry deliberately does NOT inherit thinkingBudget or
   *  disableAdaptiveThinking — those are first-pass-only knobs. The retry
   *  uses --retry-effort and lets the retry model drive its own thinking. */
  retryModel?: string;
  retryEffort?: "low" | "medium" | "high" | "xhigh" | "max";
  retryThreshold?: number;
  retryBatchSize?: number;
  retryFixtureDir?: string;
  /** Override the retry pass's record/replay mode. Defaults to recording
   *  when --retry-fixture-dir is set, since we usually don't have retry
   *  fixtures pre-populated. */
  retryUseReplay?: boolean;
  retryRecordReplay?: boolean;
  /** When false, skip the mod_subsystem proposer pre-pass.  Default true. */
  proposeSubsystems?: boolean;
  /** Model id for the proposer call.  Default haiku — it's cheap, the prompt
   *  is small, and we just want a plausible vocabulary. */
  subsystemsModel?: string;
  /** Pre-resolved canonical vocabulary, plumbed through to stage 3. Set
   *  inside `runMod` after the proposer runs; consumed by `executeStage3`. */
  subsystemVocabulary?: readonly { id: string; rationale?: string }[];
  /** Verbose-prompt extras. disambiguation defaults ON
   *  (principle-based per-facet reasoning; A/B testing showed it
   *  carries sonnet from ~50% → ~93% on hard categories). misconceptions
   *  defaults OFF (item-level enumeration kept in reserve for
   *  regressions). CLI: --no-verbose-disambiguation flips disambiguation
   *  off; --verbose-misconceptions flips misconceptions on. */
  verboseFacetDisambiguation?: boolean;
  verboseCommonMisconceptions?: boolean;
}

async function runVanilla(
  sourcePath: string,
  outDir: string,
  stages: StageSelection,
  stage3Opts: Stage3CliOptions,
) {
  const start = Date.now();
  // The mcmeta summary bundle is only needed for stage 1 (extract) and
  // stage 2 (deterministic rules over the live tag closure). When the
  // caller is running stage 3 alone — typical for prompt experimentation
  // against an already-extracted dataset — there's no point cloning a
  // ~1GB worktree just to read a version string. Skip the bundle load
  // and let `records` come from the ndjson on disk.
  const needsBundle = stages.stage1 || stages.stage2;
  let bundle: ReturnType<typeof loadSummaryBundle> | null = null;
  if (needsBundle) {
    console.log(`[vanilla] loading summary bundle from ${sourcePath}`);
    const source = ensureVanillaSource(sourcePath);
    bundle = loadSummaryBundle(source);
    console.log(`[vanilla] MC version ${bundle.version}`);
  } else {
    console.log(`[vanilla] (stages 1+2 skipped — bundle load skipped)`);
  }

  const ndjsonPath = join(outDir, "minecraft.items.ndjson");
  const metaPath = join(outDir, "minecraft.items.meta.json");
  const partialPath = join(outDir, "minecraft.facets.partial.json");
  const completePath = join(outDir, "minecraft.facets.complete.json");
  mkdirSync(dirname(ndjsonPath), { recursive: true });

  let records: ItemExtractRecord[];
  if (stages.stage1) {
    const { records: extracted, meta } = extractFromBundle(bundle!, TOOL_VERSION);
    records = extracted;
    const ndjson = records.map((r) => JSON.stringify(r)).join("\n") + "\n";
    writeFileSync(ndjsonPath, ndjson);
    writeFileSync(metaPath, JSON.stringify(meta, null, 2) + "\n");
    console.log(`[stage1] ${records.length} items → ${ndjsonPath}`);
  } else {
    records = readNdjson(ndjsonPath);
    console.log(`[stage1] (skipped; loaded ${records.length} records from ${ndjsonPath})`);
  }

  let stage2Layer: LayerFile | null = null;
  if (stages.stage2) {
    const { layer, coverage, warnings } = runDeterministic({
      records,
      bundle: bundle!,
      namespace: VANILLA_NAMESPACE,
    });
    layer.generated_by = TOOL_VERSION;
    layer.generated_at = new Date().toISOString();
    const validation = validateLayer(layer);
    if (!validation.ok) {
      console.error(`[stage2] layer failed schema validation`);
      for (const err of validation.errors.slice(0, 10)) console.error(`  ${err}`);
      process.exit(1);
    }
    writeFileSync(partialPath, JSON.stringify(layer, null, 2) + "\n");
    console.log(`[stage2] ${Object.keys(layer.entries).length} items with ≥1 facet → ${partialPath}`);
    console.log(`[stage2] coverage:`);
    const facetOrder = Object.keys(coverage).sort((a, b) => coverage[b]! - coverage[a]!);
    for (const facet of facetOrder) {
      const pct = ((coverage[facet]! / records.length) * 100).toFixed(1);
      console.log(`  ${facet.padEnd(22)} ${String(coverage[facet]).padStart(5)}/${records.length} (${pct}%)`);
    }
    if (warnings.length > 0) {
      console.log(`[stage2] ${warnings.length} warnings:`);
      for (const w of warnings.slice(0, 20)) console.log(`  ${w}`);
      if (warnings.length > 20) console.log(`  … and ${warnings.length - 20} more`);
    }
    stage2Layer = layer;
  } else if (stages.stage3) {
    if (!existsSync(partialPath)) {
      console.error(`[stage3] need stage 2 output at ${partialPath}; run with --stages 2,3`);
      process.exit(1);
    }
    stage2Layer = JSON.parse(readFileSync(partialPath, "utf8")) as LayerFile;
    console.log(`[stage2] (skipped; loaded ${Object.keys(stage2Layer.entries).length} entries)`);
  }

  if (stages.stage3 && stage2Layer) {
    await executeStage3(records, stage2Layer, completePath, stage3Opts);
  }

  console.log(`done in ${((Date.now() - start) / 1000).toFixed(2)}s`);
}

/**
 * Run the pipeline against a mod source tree (createaddition, mekanism, …).
 * Mirrors `runVanilla` but uses the mod-source bundle loader and writes
 * outputs under `<modid>.*` filenames so multiple mods can coexist in
 * the same `out/` directory.
 *
 * Stage-2 still runs against the mod's bundle; some rules will fire and
 * some won't (we have no item_components data from source). That's
 * expected — the goal of this pass is to measure how well stage 3 handles
 * modded items, not to perfect stage 2 for them.
 */
async function runMod(
  modNamespace: string,
  modPath: string,
  outDir: string,
  stages: StageSelection,
  stage3Opts: Stage3CliOptions,
) {
  const start = Date.now();
  // Same shape as runVanilla: skip the source-bundle load when stages
  // 1+2 are both off so stage-3-only experiments work against
  // already-extracted ndjson without requiring the mod's repo.
  const needsBundle = stages.stage1 || stages.stage2;
  let bundle: ReturnType<typeof loadModSourceBundle> | null = null;
  if (needsBundle) {
    console.log(`[${modNamespace}] loading mod source bundle from ${modPath}`);
    bundle = loadModSourceBundle({ modPath, modNamespace });
    console.log(`[${modNamespace}] roots: ${bundle.roots.length}; items: ${bundle.registries.item?.length ?? 0}; tags(item): ${Object.keys(bundle.itemTags).length}; recipes: ${Object.keys(bundle.recipes).length}`);
  } else {
    console.log(`[${modNamespace}] (stages 1+2 skipped — bundle load skipped)`);
  }

  const ndjsonPath = join(outDir, `${modNamespace}.items.ndjson`);
  const metaPath = join(outDir, `${modNamespace}.items.meta.json`);
  const partialPath = join(outDir, `${modNamespace}.facets.partial.json`);
  const completePath = join(outDir, `${modNamespace}.facets.complete.json`);
  mkdirSync(dirname(ndjsonPath), { recursive: true });

  let records: ItemExtractRecord[];
  if (stages.stage1) {
    const { records: extracted, meta } = extractFromModBundle(bundle!, TOOL_VERSION);
    records = extracted;
    const ndjson = records.map((r) => JSON.stringify(r)).join("\n") + "\n";
    writeFileSync(ndjsonPath, ndjson);
    writeFileSync(metaPath, JSON.stringify(meta, null, 2) + "\n");
    console.log(`[stage1] ${records.length} items → ${ndjsonPath}`);
  } else {
    records = readNdjson(ndjsonPath);
    console.log(`[stage1] (skipped; loaded ${records.length} records from ${ndjsonPath})`);
  }

  let stage2Layer: LayerFile | null = null;
  if (stages.stage2) {
    const { layer, coverage, warnings } = runDeterministic({
      records,
      bundle: bundle!,
      namespace: modNamespace,
    });
    layer.layer = "per-mod";
    layer.source = modNamespace;
    layer.generated_by = TOOL_VERSION;
    layer.generated_at = new Date().toISOString();
    const validation = validateLayer(layer);
    if (!validation.ok) {
      console.error(`[stage2] layer failed schema validation`);
      for (const err of validation.errors.slice(0, 10)) console.error(`  ${err}`);
      process.exit(1);
    }
    writeFileSync(partialPath, JSON.stringify(layer, null, 2) + "\n");
    console.log(`[stage2] ${Object.keys(layer.entries).length} items with ≥1 facet → ${partialPath}`);
    console.log(`[stage2] coverage:`);
    const facetOrder = Object.keys(coverage).sort((a, b) => coverage[b]! - coverage[a]!);
    for (const facet of facetOrder) {
      const pct = ((coverage[facet]! / records.length) * 100).toFixed(1);
      console.log(`  ${facet.padEnd(22)} ${String(coverage[facet]).padStart(5)}/${records.length} (${pct}%)`);
    }
    if (warnings.length > 0) {
      console.log(`[stage2] ${warnings.length} warnings:`);
      for (const w of warnings.slice(0, 20)) console.log(`  ${w}`);
      if (warnings.length > 20) console.log(`  … and ${warnings.length - 20} more`);
    }
    stage2Layer = layer;
  } else if (stages.stage3) {
    if (!existsSync(partialPath)) {
      console.error(`[stage3] need stage 2 output at ${partialPath}; run with --stages 2,3`);
      process.exit(1);
    }
    stage2Layer = JSON.parse(readFileSync(partialPath, "utf8")) as LayerFile;
    console.log(`[stage2] (skipped; loaded ${Object.keys(stage2Layer.entries).length} entries)`);
  }

  if (stages.stage3 && stage2Layer) {
    let modOpts = stage3Opts;
    if (stage3Opts.proposeSubsystems !== false) {
      const vocab = await resolveModSubsystems({
        modPath,
        bundle,
        outDir,
        modNamespace,
        opts: stage3Opts,
        // dry-run + no cached file: skip the live LLM call but keep going
        skipLiveCall: stage3Opts.dryRun,
      });
      if (vocab.length > 0) {
        modOpts = { ...stage3Opts, subsystemVocabulary: vocab };
      }
    }
    await executeStage3(records, stage2Layer, completePath, modOpts);
  }

  console.log(`done in ${((Date.now() - start) / 1000).toFixed(2)}s`);
}

/**
 * Drive a manifest of mods through `runMod` in sequence. Per-mod failures
 * are caught and recorded — they don't abort the rest of the pack.
 *
 * Idempotent: an entry whose `<modid>.facets.complete.json` already exists
 * with ≥1 entry is skipped, so re-running the command picks up where the
 * previous run left off (or after a manually removed file).
 */
/**
 * Knobs that govern the modpack run itself (cache invalidation,
 * cross-mod parallelism). Stage-3 LLM knobs come from
 * {@link Stage3CliOptions} — these stay one level up so the
 * cache/parallelism story is independently observable in the run
 * banner.
 */
export interface ModpackRunOptions {
  /** Delete `<modid>.facets.complete.json` for every non-skipped mod
   *  before processing, so the run reclassifies every mod from
   *  scratch instead of resuming. Subsystem vocabulary caches stay
   *  intact (they're stable across LLM runs). */
  force?: boolean;
  /** Also delete `<modid>.subsystems.json` so the subsystem proposer
   *  re-runs and the canonical vocabulary is regenerated. Slower; use
   *  when the proposer prompt itself has changed. */
  forceSubsystems?: boolean;
  /** Process this many mods in parallel. Each mod independently runs
   *  its own batch-level worker pool (see {@link Stage3CliOptions#concurrency}),
   *  so the total in-flight LLM call count is roughly
   *  `modConcurrency × concurrency`. OpenRouter handles dozens of
   *  parallel calls comfortably; defaults to 1 (sequential) for
   *  predictable progress reporting. */
  modConcurrency?: number;
}

async function runModpack(
  manifestPath: string,
  outDir: string,
  stages: StageSelection,
  stage3Opts: Stage3CliOptions,
  modpackOpts: ModpackRunOptions = {},
) {
  const start = Date.now();
  const resolved = loadModpackManifest(manifestPath);
  console.log(`[modpack] ${resolved.pack.name}: ${resolved.pack.mods.length} entr(y/ies)`);
  if (resolved.pack.description) {
    console.log(`[modpack] ${resolved.pack.description}`);
  }
  const modConcurrency = Math.max(1, modpackOpts.modConcurrency ?? 1);
  const batchConcurrency = stage3Opts.concurrency ?? 4;
  console.log(
    `[modpack] settings: mod-concurrency=${modConcurrency} batch-concurrency=${batchConcurrency}` +
      (modpackOpts.force ? ` force=true` : ``) +
      (modpackOpts.forceSubsystems ? ` force-subsystems=true` : ``),
  );

  // Cache-clear pass — runs before planModpack so the planner sees
  // post-clean state and routes every non-skipped mod to "process".
  if (modpackOpts.force || modpackOpts.forceSubsystems) {
    const cleared = clearModpackCaches(resolved, outDir, {
      facets: modpackOpts.force ?? false,
      subsystems: modpackOpts.forceSubsystems ?? false,
    });
    console.log(
      `[modpack] cleared caches: ${cleared.facets} facets-complete, ${cleared.subsystems} subsystems`,
    );
  }

  const decisions = planModpack(resolved, outDir);
  let processed = 0;
  let skipped = 0;
  let alreadyClassified = 0;
  const failures: ModpackRunSummary["failures"] = [];

  // First: report the skips (libraries + already-classified) up front
  // so the user sees what's queued. Then process the rest, optionally
  // in parallel.
  const toProcess: ModpackProcessingDecision[] = [];
  for (const d of decisions) {
    const tag = `${d.entry.namespace}`.padEnd(28);
    if (d.decision === "skipped:library") {
      skipped++;
      console.log(`[modpack] ${tag} skipped — ${d.reason}`);
      continue;
    }
    if (d.decision === "skipped:already-classified") {
      alreadyClassified++;
      console.log(`[modpack] ${tag} already classified — ${d.reason} → ${d.completePath}`);
      continue;
    }
    toProcess.push(d);
  }

  let nextIndex = 0;
  const worker = async (workerId: number): Promise<void> => {
    while (true) {
      const idx = nextIndex++;
      if (idx >= toProcess.length) return;
      const d = toProcess[idx]!;
      const tag = `${d.entry.namespace}`.padEnd(28);
      const workerLabel = modConcurrency > 1 ? `[w${workerId}] ` : ``;
      console.log("");
      console.log("─".repeat(72));
      console.log(`[modpack] ${workerLabel}${tag} processing — source ${d.sourcePath}`);
      console.log("─".repeat(72));
      try {
        await runMod(d.entry.namespace, d.sourcePath!, outDir, stages, stage3Opts);
        processed++;
      } catch (err) {
        const message = err instanceof Error ? err.message : String(err);
        console.error(`[modpack] ${workerLabel}${tag} FAILED — ${message}`);
        failures.push({ namespace: d.entry.namespace, error: message });
      }
    }
  };
  await Promise.all(
    Array.from(
      { length: Math.min(modConcurrency, toProcess.length || 1) },
      (_, i) => worker(i + 1),
    ),
  );

  const summary: ModpackRunSummary = {
    pack: resolved.pack.name,
    total: decisions.length,
    skipped,
    alreadyClassified,
    processed,
    failed: failures.length,
    failures,
    elapsedSeconds: (Date.now() - start) / 1000,
  };
  console.log(formatRunSummary(summary));
  if (failures.length > 0) {
    process.exitCode = 1;
  }
}

/**
 * Delete classification caches for every non-skipped mod in the
 * manifest. Returns the count of files actually removed for each
 * cache kind (caller logs the totals).
 */
function clearModpackCaches(
  resolved: ResolvedModpack,
  outDir: string,
  what: { facets: boolean; subsystems: boolean },
): { facets: number; subsystems: number } {
  let facets = 0;
  let subsystems = 0;
  for (const entry of resolved.pack.mods) {
    if (entry.skip) continue;
    if (what.facets) {
      const completePath = resolve(outDir, `${entry.namespace}.facets.complete.json`);
      if (existsSync(completePath)) {
        rmSync(completePath);
        facets++;
      }
    }
    if (what.subsystems) {
      const subsystemsPath = resolve(outDir, `${entry.namespace}.subsystems.json`);
      if (existsSync(subsystemsPath)) {
        rmSync(subsystemsPath);
        subsystems++;
      }
    }
  }
  return { facets, subsystems };
}

/**
 * Resolve the canonical `mod_subsystem` vocabulary for a mod run. Uses an
 * on-disk cache (`<outDir>/<modid>.subsystems.json`) so the proposer LLM call
 * only fires once per mod — subsequent runs read the saved vocabulary.
 *
 * The cache is content-agnostic: edits to README/mods.toml don't auto-bust it.
 * Delete the file to regenerate.
 */
async function resolveModSubsystems(args: {
  modPath: string;
  /** Source bundle for the live proposer call. Optional — when omitted
   *  (e.g. stage-3-only runs against pre-extracted ndjson), only the
   *  cached `<modid>.subsystems.json` will be consulted; if that's
   *  missing the proposer is skipped. */
  bundle: import("./extract/mod/source.ts").ModSourceBundle | null;
  outDir: string;
  modNamespace: string;
  opts: Stage3CliOptions;
  /** When true, only read the cache; never fire a live LLM call. */
  skipLiveCall?: boolean;
}): Promise<SubsystemEntry[]> {
  const { modPath, bundle, outDir, modNamespace, opts, skipLiveCall } = args;
  const cachePath = join(outDir, `${modNamespace}.subsystems.json`);
  if (existsSync(cachePath)) {
    try {
      const data = JSON.parse(readFileSync(cachePath, "utf8")) as {
        vocabulary?: SubsystemEntry[];
      };
      if (Array.isArray(data.vocabulary) && data.vocabulary.length > 0) {
        console.log(
          `[subsystems] using cached vocabulary (${data.vocabulary.length} entries) from ${cachePath}`,
        );
        return data.vocabulary;
      }
    } catch (err) {
      console.warn(`[subsystems] failed to read cache ${cachePath}: ${(err as Error).message}`);
    }
  }

  if (skipLiveCall) {
    console.log(
      `[subsystems] no cache and live call disabled; stage 3 will run without canonical vocabulary.`,
    );
    return [];
  }
  if (opts.useReplay && !opts.recordReplay) {
    // Replay-only mode: don't fire a live LLM call to populate the cache;
    // the user has explicitly asked for offline behavior.
    console.log(
      `[subsystems] no cache and --use-replay set; skipping proposer (stage 3 will run without canonical vocabulary).`,
    );
    return [];
  }

  if (!bundle) {
    console.log(
      `[subsystems] no cache and no source bundle (stage-3-only run); skipping proposer (stage 3 will run without canonical vocabulary).`,
    );
    return [];
  }
  // Pick a sane default proposer model that matches the live backend so
  // we don't ship a `haiku` alias to OpenRouter (or a vendor-slash slug
  // to claude-cli). Override with --subsystems-model.
  const backend = opts.backend ?? inferBackend(opts.model);
  const proposerModel =
    opts.subsystemsModel ?? (backend === "openrouter" ? (opts.model ?? "deepseek/deepseek-v4-flash") : "haiku");
  console.log(`[subsystems] proposing canonical vocabulary for ${modNamespace} (${proposerModel} via ${backend})`);
  const meta = extractModMetadata({ modPath, bundle });
  if (
    !meta.readme &&
    !meta.description &&
    meta.modRecipeTypes.length === 0 &&
    meta.itemDisplayNames.length === 0
  ) {
    console.log(`[subsystems] no metadata signals; skipping proposer`);
    return [];
  }
  const client = buildClient(opts);
  const proposal = await proposeSubsystems(meta, {
    client,
    model: proposerModel,
  });
  if (proposal.vocabulary.length === 0) {
    console.warn(
      `[subsystems] proposer returned no usable vocabulary entries; raw response saved alongside cache`,
    );
  }
  writeFileSync(
    cachePath,
    JSON.stringify(
      {
        modNamespace: proposal.modNamespace,
        generated_at: new Date().toISOString(),
        generated_by: TOOL_VERSION,
        vocabulary: proposal.vocabulary,
      },
      null,
      2,
    ) + "\n",
  );
  console.log(
    `[subsystems] wrote ${proposal.vocabulary.length} entr(y/ies) → ${cachePath}`,
  );
  for (const entry of proposal.vocabulary) {
    console.log(`  ${entry.id.padEnd(40)}${entry.rationale ? " " + entry.rationale : ""}`);
  }
  return proposal.vocabulary;
}

async function executeStage3(
  records: readonly ItemExtractRecord[],
  stage2Layer: LayerFile,
  completePath: string,
  opts: Stage3CliOptions,
) {
  const only = resolveSample(opts.sample, records);
  if (only && only.length === 0) {
    console.error(`[stage3] sample selection produced 0 items`);
    process.exit(1);
  }
  const n = only?.length ?? records.length;
  console.log(`[stage3] running against ${n} items${only ? " (sampled)" : ""}`);

  if (opts.dryRun) {
    await dryRunStage3(records, stage2Layer, only, opts, dirname(completePath));
    return;
  }

  const client = buildClient(opts);

  const result = await runStage3({
    records,
    stage2Layer,
    client,
    model: opts.model,
    batchSize: opts.batchSize,
    concurrency: opts.concurrency,
    only,
    clientOptions: buildClientOptions(opts.effort, opts.thinkingBudget, opts.disableAdaptiveThinking),
    subsystemVocabulary: opts.subsystemVocabulary,
    promptExtras: {
      verboseFacetDisambiguation: opts.verboseFacetDisambiguation,
      verboseCommonMisconceptions: opts.verboseCommonMisconceptions,
    },
    onBatch: (info) => {
      console.log(
        `[stage3] batch ${info.batchIndex + 1}/${info.batchCount} ` +
          `parsed=${info.parsed}/${info.items.length} ` +
          `warnings=${info.warnings.length} ` +
          `elapsed=${info.elapsedMs}ms`,
      );
    },
  });

  // Optional retry pass: re-ask a stronger model about items whose LLM
  // facets were low-confidence or ambiguous.
  if (opts.retryModel) {
    const candidates = selectRetryCandidates(
      result.layer,
      opts.retryThreshold ?? 0.5,
    );
    console.log(
      `[stage3-retry] ${candidates.length} candidate item(s) below confidence ${opts.retryThreshold ?? 0.5} or flagged ambiguous`,
    );
    if (candidates.length > 0) {
      // Resolve retry record/replay mode independently of the first pass:
      //   - explicit --retry-use-replay / --retry-record-replay win
      //   - else if --retry-fixture-dir was provided, default to recording
      //     (we usually don't have retry fixtures pre-populated)
      //   - else inherit from the first-pass mode
      const retryHasFixtureDir = !!opts.retryFixtureDir;
      const retryUseReplay = opts.retryUseReplay
        ?? (retryHasFixtureDir ? false : opts.useReplay);
      const retryRecordReplay = opts.retryRecordReplay
        ?? (retryHasFixtureDir ? true : opts.recordReplay);
      const retryClient = buildClient({
        ...opts,
        fixtureDir: opts.retryFixtureDir ?? opts.fixtureDir,
        useReplay: retryUseReplay,
        recordReplay: retryRecordReplay,
      });
      const retryResult = await runStage3Retry({
        records,
        firstPassLayer: result.layer,
        client: retryClient,
        model: opts.retryModel,
        effort: opts.retryEffort,
        threshold: opts.retryThreshold,
        batchSize: opts.retryBatchSize,
        subsystemVocabulary: opts.subsystemVocabulary,
        promptExtras: {
          verboseFacetDisambiguation: opts.verboseFacetDisambiguation,
          verboseCommonMisconceptions: opts.verboseCommonMisconceptions,
        },
        onBatch: (info) => {
          console.log(
            `[stage3-retry] batch ${info.batchIndex + 1}/${info.batchCount} ` +
              `parsed=${info.parsed}/${info.items.length} ` +
              `warnings=${info.warnings.length} ` +
              `elapsed=${info.elapsedMs}ms`,
          );
        },
      });
      result.layer = retryResult.layer;
      result.warnings.push(...retryResult.warnings);
      result.proposals.push(...retryResult.proposals);
      result.corrections.push(...retryResult.corrections);
      result.fillIns.push(...retryResult.fillIns);
      console.log(
        `[stage3-retry] changed: ${Object.values(retryResult.facetsChanged).reduce((a, b) => a + b, 0)} facet(s), confirmed: ${Object.values(retryResult.facetsConfirmed).reduce((a, b) => a + b, 0)} facet(s)`,
      );
      if (Object.keys(retryResult.facetsChanged).length) {
        const changed = Object.entries(retryResult.facetsChanged).sort(
          (a, b) => b[1] - a[1],
        );
        console.log(`[stage3-retry] changes by facet:`);
        for (const [f, n] of changed) console.log(`  ${f.padEnd(22)} ${n}`);
      }
    }
  }

  const validation = validateLayer(result.layer);
  if (!validation.ok) {
    console.error(`[stage3] layer failed schema validation`);
    for (const err of validation.errors.slice(0, 10)) console.error(`  ${err}`);
    process.exit(1);
  }
  writeFileSync(completePath, JSON.stringify(result.layer, null, 2) + "\n");

  // Persist proposals/corrections to dedicated files (only when non-empty,
  // so the report can list exactly what's on disk and what's worth opening).
  const writtenFiles: { path: string; description: string }[] = [];
  writtenFiles.push({ path: completePath, description: "merged layer (stage 2 + stage 3)" });

  if (result.proposals.length) {
    const proposalsPath = completePath.replace(/\.complete\.json$/, ".schema-proposals.json");
    writeFileSync(proposalsPath, JSON.stringify(result.proposals, null, 2) + "\n");
    writtenFiles.push({
      path: proposalsPath,
      description: `${result.proposals.length} schema proposal(s) — values/facets the LLM wanted but couldn't find in the schema`,
    });
  }
  if (result.corrections.length) {
    const correctionsPath = completePath.replace(/\.complete\.json$/, ".corrections.json");
    writeFileSync(correctionsPath, JSON.stringify(result.corrections, null, 2) + "\n");
    writtenFiles.push({
      path: correctionsPath,
      description: `${result.corrections.length} stage-2 correction(s) flagged by the LLM — review and patch the rule files if valid`,
    });
  }
  if (result.fillIns.length) {
    const fillInsPath = completePath.replace(/\.complete\.json$/, ".fill-ins.json");
    writeFileSync(fillInsPath, JSON.stringify(result.fillIns, null, 2) + "\n");
    writtenFiles.push({
      path: fillInsPath,
      description: `${result.fillIns.length} stage-2 fill-in(s) — deterministic facets the rule layer missed; expand stage-2 rules to cover these patterns`,
    });
  }

  // ===== End-of-run report =====
  const sep = "=".repeat(72);
  console.log("");
  console.log(sep);
  console.log("Stage 3 run complete");
  console.log(sep);

  console.log("");
  console.log("Coverage added (LLM-authored facets, sorted by frequency):");
  const facets = Object.keys(result.coverageAdded).sort(
    (a, b) => result.coverageAdded[b]! - result.coverageAdded[a]!,
  );
  for (const facet of facets) {
    console.log(`  ${facet.padEnd(24)} ${String(result.coverageAdded[facet]).padStart(5)}`);
  }
  console.log(`  (filled ${result.filledItems} items total)`);

  console.log("");
  console.log(`Output files (${writtenFiles.length}):`);
  for (const f of writtenFiles) {
    console.log(`  ${f.path}`);
    console.log(`    ${f.description}`);
  }

  // ----- Review queue: things a curator should look at before shipping -----
  const reviewItems: { kind: string; summary: string; detail?: string[]; path?: string }[] = [];
  if (result.corrections.length) {
    reviewItems.push({
      kind: "STAGE-2 CORRECTIONS",
      summary: `${result.corrections.length} item(s) where the LLM thinks a deterministic stage-2 facet is wrong`,
      detail: result.corrections.slice(0, 10).map((c) =>
        `${c.item} ${c.facet}: '${c.current}' → '${c.suggested}'  (${(c.confidence ?? 0).toFixed(2)}) — ${c.rationale}`,
      ),
      path: completePath.replace(/\.complete\.json$/, ".corrections.json"),
    });
  }
  if (result.proposals.length) {
    reviewItems.push({
      kind: "SCHEMA PROPOSALS",
      summary: `${result.proposals.length} proposal(s) — values or facets the LLM wanted but couldn't find`,
      detail: result.proposals.slice(0, 10).map((p) => {
        if (p.kind === "add_value") {
          return `add_value  ${p.facet}: '${p.value}' — ${p.rationale}`;
        }
        if (p.kind === "add_facet") {
          return `add_facet  ${p.name} (${p.suggested_kind}) — ${p.rationale}`;
        }
        return JSON.stringify(p);
      }),
      path: completePath.replace(/\.complete\.json$/, ".schema-proposals.json"),
    });
  }
  if (result.fillIns.length) {
    reviewItems.push({
      kind: "STAGE-2 FILL-INS",
      summary: `${result.fillIns.length} item(s) where the LLM filled a deterministic facet that the stage-2 rules missed`,
      detail: result.fillIns.slice(0, 10).map((f) =>
        `${f.item} ${f.facet} = '${f.value}' — ${f.rationale}`,
      ),
      path: completePath.replace(/\.complete\.json$/, ".fill-ins.json"),
    });
  }
  if (result.warnings.length) {
    reviewItems.push({
      kind: "WARNINGS",
      summary: `${result.warnings.length} warning(s) (most are stage-2 disagreements + format-fix wraps; usually fine)`,
      detail: result.warnings.slice(0, 5),
    });
  }

  console.log("");
  if (reviewItems.length === 0) {
    console.log("Review queue: clean — no proposals, corrections, or warnings.");
  } else {
    console.log(`Review queue (${reviewItems.length} section(s)):`);
    for (const r of reviewItems) {
      console.log("");
      console.log(`  ${r.kind} — ${r.summary}`);
      if (r.path) console.log(`    file: ${r.path}`);
      if (r.detail?.length) {
        for (const d of r.detail) console.log(`      • ${d}`);
        if ((r.detail.length === 10 || r.detail.length === 5) && r.path) {
          console.log(`      … (full list in ${r.path})`);
        }
      }
    }
  }
  console.log("");
  console.log(sep);
}

/**
 * Build and persist the prompts without calling the LLM. Writes one prompt
 * file per batch + a summary so the prompt content can be eyeballed before
 * we commit to tokens.
 */
async function dryRunStage3(
  records: readonly ItemExtractRecord[],
  stage2Layer: LayerFile,
  only: readonly string[] | undefined,
  opts: Stage3CliOptions,
  outDir: string,
) {
  const batchSize = opts.batchSize ?? 20;
  const targetFacets = defaultTargetFacets();
  const recordIndex = new Map(records.map((r) => [r.id, r]));

  const selected = only
    ? (only.map((id) => recordIndex.get(id)).filter((r): r is ItemExtractRecord => !!r))
    : records;

  const batches: ItemExtractRecord[][] = [];
  for (let i = 0; i < selected.length; i += batchSize) {
    batches.push(selected.slice(i, i + batchSize));
  }

  const dryRunDir = join(outDir, "stage3-dry-run");
  mkdirSync(dryRunDir, { recursive: true });

  const summary: Array<{ batch: number; items: string[]; file: string; chars: number; approxTokens: number }> = [];
  for (let i = 0; i < batches.length; i++) {
    const batch = batches[i]!;
    const payloads = batch.map((record) => {
      const stage2 = stage2Layer.entries[record.id]?.facets ?? {};
      return buildItemPayload(record, stage2);
    });
    const prompt = buildBatchPrompt({ items: payloads, target_facets: targetFacets });
    const file = join(dryRunDir, `batch-${String(i + 1).padStart(2, "0")}.prompt.txt`);
    writeFileSync(file, prompt);
    summary.push({
      batch: i + 1,
      items: batch.map((r) => r.id),
      file,
      chars: prompt.length,
      approxTokens: Math.round(prompt.length / 4),
    });
  }

  const summaryFile = join(dryRunDir, "summary.json");
  writeFileSync(summaryFile, JSON.stringify(summary, null, 2) + "\n");

  console.log(`[stage3] dry run: wrote ${batches.length} prompt(s) to ${dryRunDir}`);
  for (const s of summary) {
    console.log(
      `  batch ${s.batch}: ${s.items.length} items, ${s.chars} chars (~${s.approxTokens} tokens) → ${s.file.split("/").slice(-2).join("/")}`,
    );
  }
  console.log(`  summary → ${summaryFile}`);
}

function resolveSample(
  sample: string | undefined,
  records: readonly ItemExtractRecord[],
): readonly string[] | undefined {
  if (!sample) return undefined;
  if (sample === "canary") return VANILLA_CANARY_ITEMS;
  if (/^\d+$/.test(sample)) {
    const n = Number(sample);
    return records.slice(0, n).map((r) => r.id);
  }
  return sample.split(",").map((s) => s.trim()).filter((s) => s.length > 0);
}

function buildClient(opts: Stage3CliOptions): LlmClient {
  if (opts.useReplay) {
    if (!opts.fixtureDir) {
      console.error("--use-replay requires --fixture-dir");
      process.exit(2);
    }
    return new ReplayLlmClient(resolve(opts.fixtureDir));
  }
  const live = buildLiveClient(opts);
  if (opts.recordReplay) {
    if (!opts.fixtureDir) {
      console.error("--record-replay requires --fixture-dir");
      process.exit(2);
    }
    let hits = 0;
    let misses = 0;
    const cacheLog = (event: { hit: boolean }) => {
      if (event.hit) {
        hits++;
        // Print a tight "resumed" line only on the first hit of a run so the
        // user knows cache is active; batch-completion lines will still count.
        if (hits === 1) console.log(`[stage3] resume: found existing fixture(s); cached batches will skip the LLM.`);
      } else {
        misses++;
      }
    };
    return new RecordingLlmClient(live, resolve(opts.fixtureDir), cacheLog);
  }
  return live;
}

function buildLiveClient(opts: Stage3CliOptions): LlmClient {
  const backend = opts.backend ?? inferBackend(opts.model);
  switch (backend) {
    case "claude-cli":
      return new ClaudeCliClient();
    case "openrouter":
      return new OpenRouterClient({
        ignoredProviders: opts.ignoredProviders,
        // Auto-pin to the official deepseek upstream when the model
        // is a deepseek/* slug and the caller didn't override. This
        // matches the "lock in v4-flash via deepseek" production
        // recipe without requiring every script to repeat the flag.
        onlyProviders: opts.onlyProviders ?? inferOnlyProviders(opts.model),
      });
    default:
      throw new Error(`unknown backend: ${backend}`);
  }
}

/**
 * Infer the live backend from the model id when --backend wasn't
 * specified. OpenRouter slugs always include a vendor prefix
 * (`vendor/name`); Claude aliases (haiku/sonnet/opus) and full Claude
 * model ids (claude-haiku-4-5, …) don't.
 */
function inferBackend(model: string | undefined): "claude-cli" | "openrouter" {
  if (!model) return "openrouter"; // matches DEFAULT_MODEL = "deepseek/deepseek-v4-flash"
  if (model.includes("/")) return "openrouter";
  return "claude-cli";
}

function inferOnlyProviders(model: string | undefined): readonly string[] | undefined {
  if (!model) {
    // DEFAULT_MODEL is deepseek/* — pin to deepseek by default.
    return ["deepseek"];
  }
  if (model.startsWith("deepseek/")) return ["deepseek"];
  return undefined;
}

function readNdjson(path: string): ItemExtractRecord[] {
  const text = readFileSync(path, "utf8");
  return text
    .split("\n")
    .filter((line) => line.trim().length > 0)
    .map((line) => JSON.parse(line) as ItemExtractRecord);
}

function printHelp() {
  console.log(`slot-classify — item classification pipeline

Commands:
  classify --mod <id> --source <path> [options]
      Run stages against a source. Currently only --mod minecraft is wired up;
      the source must be a clone of misode/mcmeta (use tools/mcmeta submodule).

  classify-modpack <manifest.json> [options]
      Run \`classify\` for every mod listed in a modpack manifest. By
      default skips entries marked \`skip\` and entries whose
      <modid>.facets.complete.json already exists in --out
      (so the command is idempotent / resumable). Pass --force to
      reclassify every non-skipped mod from scratch. Per-mod failures
      are collected into an end-of-run summary instead of aborting
      the pack. Manifests live under tools/classification/modpacks/.

      Modpack-only flags:
        --force                Delete <modid>.facets.complete.json
                               for every non-skipped mod before
                               processing, forcing a full rerun.
                               Subsystem vocabulary stays cached
                               (it's stable across runs).
        --force-subsystems     Also delete <modid>.subsystems.json
                               so the proposer regenerates the
                               canonical mod_subsystem vocabulary.
                               Use when the proposer prompt has
                               changed.
        --mod-concurrency <n>  Process N mods in parallel (default 1).
                               Each mod runs its own batch worker
                               pool, so total in-flight LLM calls
                               ≈ mod-concurrency × concurrency.
                               OpenRouter handles dozens comfortably;
                               recommend 3-4 for fast wall-time.

  validate <layer.json>
      Validate a layer file against layer.schema.json.

Stage selection:
  --stages 1,2[,3]          Which stages to run. Default: 1,2 (stage 3 opt-in).

Stage 3 (LLM) knobs — only used when 3 is in --stages:
  --backend <name>          Live backend: claude-cli or openrouter.
                            **Default: auto-inferred from --model.** A
                            vendor-slash slug (e.g. deepseek/deepseek-v4-flash)
                            routes to openrouter; a plain Claude alias
                            (haiku/sonnet/opus) or claude-* full id
                            routes to claude-cli. Pass explicitly to
                            override. openrouter requires OPENROUTER_API_KEY
                            in env. Replay mode ignores this.
  --only-provider <slug>    (openrouter) Pin routing to a single upstream
                            provider — sends provider.only + allow_fallbacks=false.
                            Repeatable. Falls back to OPENROUTER_ONLY_PROVIDERS
                            env var (CSV) when unset. **Auto-defaults to
                            'deepseek' when --model starts with deepseek/.**
                            Useful for price/caching/throughput/data-policy
                            reasons. Avoids known-flaky providers
                            (deepinfra, siliconflow) for the deepseek family.
  --ignore-provider <slug>  (openrouter) Exclude a provider from routing
                            — sends provider.ignore. Repeatable. Falls
                            back to OPENROUTER_IGNORE_PROVIDERS env var.
                            Overridden by --only-provider when both set.
  --model <id>              Model id. **Default: deepseek/deepseek-v4-flash**
                            (production recipe — locked in 2026-04-26 after
                            A/B vs Claude on the 60-item playtest sample).
                            For claude-cli: aliases (haiku/sonnet/opus) or
                            full Claude model names. For openrouter: full
                            slug (e.g. 'deepseek/deepseek-v4-flash',
                            'deepseek/deepseek-v4-pro', 'openai/gpt-4o-mini').
  --effort <level>          Reasoning effort: low|medium|high|xhigh|max.
                            claude-cli only — ignored on openrouter.
  --batch-size <n>          Items per LLM call (default 20 for haiku, try 10 for sonnet).
  --concurrency <n>         Run up to N batches in parallel (default 4).
                            Each parallel batch is an independent LLM call.
                            OpenRouter handles 4-8 comfortably; bump higher
                            (--concurrency 8) for the fastest wall-time.
                            Set to 1 for serial / debugging.
  --sample canary|N|id,...  Restrict to a subset:
                              canary   – the hand-picked 102-item set.
                              N        – first N records from the extract.
                              id,...   – explicit comma-separated item ids.
  --fixture-dir <path>      Directory for replay fixtures (prompt/response pairs).
  --record-replay           Call real claude -p AND persist fixtures to --fixture-dir.
  --use-replay              Read responses from --fixture-dir; never call claude -p.
  --dry-run                 Build prompts and stop before any LLM call.

Retry pass (opt-in; runs after the first pass on low-confidence items):
  --retry-model <id>        Retry model (e.g. sonnet). Enabling this turns on retry.
  --retry-effort <level>    Effort for the retry pass — 'max' for heaviest thinking.
  --retry-threshold <n>     Retry items with any LLM facet confidence < n or ambiguous:true. Default 0.5.
  --retry-batch-size <n>    Items per retry LLM call. Default 8.
  --retry-fixture-dir <p>   Separate fixture directory for the retry pass.

Mod-only subsystem proposer (default: on for mods):
  --no-propose-subsystems   Skip the README/metadata pre-pass. Stage 3 then
                            invents mod_subsystem labels per item.
  --subsystems-model <id>   Model id for the proposer call. Default haiku.
                            Cached at <out>/<modid>.subsystems.json — delete
                            to regenerate.

Prompt extras (defaults: disambiguation ON, misconceptions OFF):
  --no-verbose-disambiguation
                            Drop the principle-based per-facet reasoning
                            section (role / building vs decorative vs
                            functional / storage / transport / curiosity
                            vs utility / consistency-within-family / tier
                            / activity). Lean prompt only — for A/B
                            testing the cardinal rule alone.
  --verbose-misconceptions  Add an item-level checklist of past LLM
                            failure modes (logs / doors / beds / rails /
                            spawn-eggs / Block-of-X / mob-drops). Useful
                            when a regression surfaces a category-wide
                            failure; off by default to avoid biasing
                            toward enumerated examples.

Examples:
  bun run src/cli.ts classify --mod minecraft --source ../mcmeta
  bun run src/cli.ts classify --mod minecraft --source ../mcmeta --stages 3 --sample canary --dry-run
  bun run src/cli.ts classify --mod minecraft --source ../mcmeta --stages 3 --sample canary \\
      --record-replay --fixture-dir test/fixtures/stage3-canary
  bun run src/cli.ts classify --mod minecraft --source ../mcmeta --stages 3 --sample canary \\
      --model haiku --record-replay --fixture-dir test/fixtures/stage3-canary-haiku \\
      --retry-model sonnet --retry-effort max --retry-threshold 0.6 \\
      --retry-fixture-dir test/fixtures/stage3-canary-sonnet-retry

  # OpenRouter backend — same prompt, different model family:
  OPENROUTER_API_KEY=sk-or-... \\
    bun run src/cli.ts classify --mod minecraft --source ../mcmeta --stages 3 --sample canary \\
      --backend openrouter --model deepseek/deepseek-v4-flash \\
      --record-replay --fixture-dir test/fixtures/stage3-canary-deepseek

  # Classify every mod in a modpack manifest (idempotent — re-run to resume):
  OPENROUTER_API_KEY=sk-or-... \\
    bun run src/cli.ts classify-modpack modpacks/test-modset.json --out out

  # FAST: reclassify the whole pack with high parallelism (after a prompt
  # change). --force clears the per-mod completion markers; --concurrency
  # 8 puts 8 batches in flight per mod; --mod-concurrency 4 runs 4 mods
  # at once. Total in-flight LLM calls ≈ 32, well within OpenRouter's
  # comfort zone for the deepseek family.
  OPENROUTER_API_KEY=sk-or-... \\
    bun run src/cli.ts classify-modpack modpacks/test-modset.json --out out \\
      --stages 1,2,3 --force --concurrency 8 --mod-concurrency 4

  # Convenience aliases (same as the FAST recipe above):
  bun run reclassify:test-modset       # reclassify only what changed
  bun run reclassify:test-modset:full  # also regenerate subsystem vocabularies

Prompt-evaluation presets (60-item playtest sample; reads stage-1/2 from out/):
  bun run eval:sonnet                  # claude-cli + sonnet (the baseline)
  OPENROUTER_API_KEY=... bun run eval:deepseek
                                       # openrouter + deepseek/deepseek-v4-flash
  scripts/eval-prompt.sh --backend openrouter --model openai/gpt-4o-mini
                                       # any backend/model combo
`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
