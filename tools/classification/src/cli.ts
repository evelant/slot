import { copyFileSync, existsSync, mkdirSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { basename, dirname, join, resolve } from "node:path";
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
import { loadJarModBundle } from "./extract/jar/source.ts";
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
import {
  selectSubsystemVocabularyForRecords,
  type SubsystemVocabularyByNamespace,
} from "./llm/run.ts";
import { runStage3Retry, selectRetryCandidates } from "./llm/retry.ts";
import {
  buildBatchPrompt,
  buildItemPayload,
  defaultTargetFacets,
  PROMPT_VERSION,
} from "./llm/prompt.ts";
import { VANILLA_CANARY_ITEMS } from "./llm/canary.ts";
import {
  extractModMetadata,
  proposeSubsystems,
  type SubsystemEntry,
} from "./llm/mod_metadata.ts";
import {
  buildRuntimeProposerPrompt,
  buildRuntimeSubsystemContexts,
  contextEvidence,
  defaultRuntimeSummaryPath,
  loadSubsystemVocabularyFile,
  proposeRuntimeSubsystems,
  readRuntimeExportRecords,
  readRuntimeExportSummary,
  type RuntimeExportSummary,
  type RuntimeSubsystemVocabularyFile,
} from "./llm/runtime_subsystems.ts";
import {
  loadModpackManifest,
  planModpack,
  inspectReusableCompleteOutput,
  formatRunSummary,
  type ModpackProcessingDecision,
  type ModpackRunSummary,
  type ResolvedModpack,
} from "./modpack.ts";
import { formatScanReport, scanModsFolder } from "./scan/mods_folder.ts";
import type { InputManifestMod } from "./input/manifest.ts";

const TOOL_VERSION = "slot-classify v0.1.0";
const DEFAULT_STAGE3_MODEL = "deepseek/deepseek-v4-flash";
const DEFAULT_STAGE3_BATCH_SIZE = 20;
const DEFAULT_STAGE3_CONCURRENCY = 4;
const REPO_ROOT = resolve(import.meta.dir, "..", "..", "..");

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

function parsePositiveInteger(input: string | undefined, optionName: string): number | undefined {
  if (input === undefined) return undefined;
  if (!/^[1-9]\d*$/.test(input)) {
    throw new Error(`${optionName} must be a positive integer, got '${input}'`);
  }
  const value = Number(input);
  if (!Number.isSafeInteger(value)) {
    throw new Error(`${optionName} is too large: '${input}'`);
  }
  return value;
}

function parseConfidenceThreshold(input: string | undefined, optionName: string): number | undefined {
  if (input === undefined) return undefined;
  const value = Number(input);
  if (!Number.isFinite(value) || value < 0 || value > 1) {
    throw new Error(`${optionName} must be a number between 0 and 1, got '${input}'`);
  }
  return value;
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
          "subsystems-file": { type: "string" },
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
        batchSize: parsePositiveInteger(args.values["batch-size"], "--batch-size"),
        concurrency: parsePositiveInteger(args.values.concurrency, "--concurrency"),
        effort: parseEffort(args.values.effort),
        thinkingBudget: args.values["thinking-budget"]
          ? parsePositiveInteger(args.values["thinking-budget"], "--thinking-budget")
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
          ? parseConfidenceThreshold(args.values["retry-threshold"], "--retry-threshold")
          : undefined,
        retryBatchSize: args.values["retry-batch-size"]
          ? parsePositiveInteger(args.values["retry-batch-size"], "--retry-batch-size")
          : undefined,
        retryFixtureDir: args.values["retry-fixture-dir"],
        retryUseReplay: args.values["retry-use-replay"],
        retryRecordReplay: args.values["retry-record-replay"],
        proposeSubsystems: !(args.values["no-propose-subsystems"] ?? false),
        subsystemsModel: args.values["subsystems-model"],
        subsystemsFile: args.values["subsystems-file"],
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
          "subsystems-file": { type: "string" },
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
        batchSize: parsePositiveInteger(args.values["batch-size"], "--batch-size"),
        concurrency: parsePositiveInteger(args.values.concurrency, "--concurrency"),
        effort: parseEffort(args.values.effort),
        thinkingBudget: args.values["thinking-budget"]
          ? parsePositiveInteger(args.values["thinking-budget"], "--thinking-budget")
          : undefined,
        disableAdaptiveThinking: args.values["disable-adaptive-thinking"] ?? false,
        fixtureDir: args.values["fixture-dir"],
        useReplay: args.values["use-replay"] ?? false,
        recordReplay: args.values["record-replay"] ?? false,
        dryRun: args.values["dry-run"] ?? false,
        proposeSubsystems: !(args.values["no-propose-subsystems"] ?? false),
        subsystemsModel: args.values["subsystems-model"],
        subsystemsFile: args.values["subsystems-file"],
        verboseFacetDisambiguation: args.values["no-verbose-disambiguation"]
          ? false
          : (args.values["verbose-disambiguation"] ?? true),
        verboseCommonMisconceptions: args.values["verbose-misconceptions"] ?? false,
      };

      const modpackOpts: ModpackRunOptions = {
        force: args.values.force ?? false,
        forceSubsystems: args.values["force-subsystems"] ?? false,
        modConcurrency: args.values["mod-concurrency"]
          ? parsePositiveInteger(args.values["mod-concurrency"], "--mod-concurrency")
          : 1,
      };

      await runModpack(manifestPath, outDir, stages, stage3CliOpts, modpackOpts);
      return;
    }

    case "scan": {
      const args = parseArgs({
        args: rest,
        options: {
          mods: { type: "string" },
          out: { type: "string" },
          json: { type: "string" },
        },
        allowPositionals: false,
        strict: true,
      });
      const modsPath = args.values.mods;
      if (!modsPath) {
        console.error("usage: scan --mods <mods-folder-or-instance-root> [--out <dir>] [--json <path>]");
        process.exit(2);
        return;
      }
      const outDir = resolve(args.values.out ?? "out");
      mkdirSync(outDir, { recursive: true });
      const jsonPath = resolve(args.values.json ?? join(outDir, "scan-report.json"));
      mkdirSync(dirname(jsonPath), { recursive: true });

      const report = scanModsFolder({
        requestedPath: modsPath,
        generatedBy: TOOL_VERSION,
        bundledModIds: loadBundledPerModIds(),
      });
      writeFileSync(jsonPath, JSON.stringify(report, null, 2) + "\n");
      console.log(formatScanReport(report));
      console.log("");
      console.log(`[scan] JSON report: ${jsonPath}`);
      console.log("[scan] use classify-folder --mods <path> to generate jar-backed stage-1/2 outputs.");
      return;
    }

    case "classify-folder": {
      const args = parseArgs({
        args: rest,
        options: {
          mods: { type: "string" },
          out: { type: "string" },
          stages: { type: "string" },
          mod: { type: "string", multiple: true },
          "limit-mods": { type: "string" },
          "include-covered": { type: "boolean" },
          force: { type: "boolean" },
          "mod-concurrency": { type: "string" },
          model: { type: "string" },
          backend: { type: "string" },
          "ignore-provider": { type: "string", multiple: true },
          "only-provider": { type: "string", multiple: true },
          "batch-size": { type: "string" },
          concurrency: { type: "string" },
          effort: { type: "string" },
          "thinking-budget": { type: "string" },
          "disable-adaptive-thinking": { type: "boolean" },
          sample: { type: "string" },
          "fixture-dir": { type: "string" },
          "use-replay": { type: "boolean" },
          "record-replay": { type: "boolean" },
          "dry-run": { type: "boolean" },
          "retry-model": { type: "string" },
          "retry-effort": { type: "string" },
          "retry-threshold": { type: "string" },
          "retry-batch-size": { type: "string" },
          "retry-fixture-dir": { type: "string" },
          "retry-use-replay": { type: "boolean" },
          "retry-record-replay": { type: "boolean" },
          "no-propose-subsystems": { type: "boolean" },
          "subsystems-model": { type: "string" },
          "subsystems-file": { type: "string" },
          "verbose-disambiguation": { type: "boolean" },
          "no-verbose-disambiguation": { type: "boolean" },
          "verbose-misconceptions": { type: "boolean" },
        },
        allowPositionals: false,
        strict: true,
      });
      const modsPath = args.values.mods;
      if (!modsPath) {
        console.error("usage: classify-folder --mods <mods-folder-or-instance-root> [--out <dir>] [--stages 1,2,3]");
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
        batchSize: parsePositiveInteger(args.values["batch-size"], "--batch-size"),
        concurrency: parsePositiveInteger(args.values.concurrency, "--concurrency"),
        effort: parseEffort(args.values.effort),
        thinkingBudget: args.values["thinking-budget"]
          ? parsePositiveInteger(args.values["thinking-budget"], "--thinking-budget")
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
          ? parseConfidenceThreshold(args.values["retry-threshold"], "--retry-threshold")
          : undefined,
        retryBatchSize: args.values["retry-batch-size"]
          ? parsePositiveInteger(args.values["retry-batch-size"], "--retry-batch-size")
          : undefined,
        retryFixtureDir: args.values["retry-fixture-dir"],
        retryUseReplay: args.values["retry-use-replay"],
        retryRecordReplay: args.values["retry-record-replay"],
        proposeSubsystems: !(args.values["no-propose-subsystems"] ?? false),
        subsystemsModel: args.values["subsystems-model"],
        subsystemsFile: args.values["subsystems-file"],
        verboseFacetDisambiguation: args.values["no-verbose-disambiguation"]
          ? false
          : (args.values["verbose-disambiguation"] ?? true),
        verboseCommonMisconceptions: args.values["verbose-misconceptions"] ?? false,
      };

      await runModsFolderClassification(modsPath, outDir, stages, stage3CliOpts, {
        targetMods: (args.values.mod as string[] | undefined) ?? [],
        limitMods: parsePositiveInteger(args.values["limit-mods"], "--limit-mods"),
        includeCovered: args.values["include-covered"] ?? false,
        force: args.values.force ?? false,
        modConcurrency: parsePositiveInteger(args.values["mod-concurrency"], "--mod-concurrency") ?? 1,
      });
      return;
    }

    case "propose-runtime-subsystems": {
      const args = parseArgs({
        args: rest,
        options: {
          "runtime-export": { type: "string" },
          summary: { type: "string" },
          out: { type: "string" },
          namespace: { type: "string", multiple: true },
          "limit-namespaces": { type: "string" },
          "min-items": { type: "string" },
          model: { type: "string" },
          backend: { type: "string" },
          "ignore-provider": { type: "string", multiple: true },
          "only-provider": { type: "string", multiple: true },
          effort: { type: "string" },
          "thinking-budget": { type: "string" },
          "disable-adaptive-thinking": { type: "boolean" },
          "fixture-dir": { type: "string" },
          "use-replay": { type: "boolean" },
          "record-replay": { type: "boolean" },
          "dry-run": { type: "boolean" },
          force: { type: "boolean" },
        },
        allowPositionals: false,
        strict: true,
      });
      const runtimeExportPath = args.values["runtime-export"];
      if (!runtimeExportPath) {
        console.error("usage: propose-runtime-subsystems --runtime-export <pack.runtime-items.ndjson> [options]");
        process.exit(2);
        return;
      }
      const outDir = resolve(args.values.out ?? "out");
      mkdirSync(outDir, { recursive: true });
      const opts: Stage3CliOptions = {
        model: args.values.model,
        backend: parseBackend(args.values.backend),
        ignoredProviders: (args.values["ignore-provider"] as string[] | undefined) ?? undefined,
        onlyProviders: (args.values["only-provider"] as string[] | undefined) ?? undefined,
        effort: parseEffort(args.values.effort),
        thinkingBudget: args.values["thinking-budget"]
          ? parsePositiveInteger(args.values["thinking-budget"], "--thinking-budget")
          : undefined,
        disableAdaptiveThinking: args.values["disable-adaptive-thinking"] ?? false,
        fixtureDir: args.values["fixture-dir"],
        useReplay: args.values["use-replay"] ?? false,
        recordReplay: args.values["record-replay"] ?? false,
        dryRun: args.values["dry-run"] ?? false,
      };
      await runRuntimeSubsystemProposal({
        runtimeExportPath,
        summaryPath: args.values.summary,
        outDir,
        namespaces: (args.values.namespace as string[] | undefined) ?? [],
        limitNamespaces: parsePositiveInteger(args.values["limit-namespaces"], "--limit-namespaces"),
        minItems: parsePositiveInteger(args.values["min-items"], "--min-items") ?? 4,
        force: args.values.force ?? false,
        opts,
      });
      return;
    }

    case "generate-pack-layer": {
      const args = parseArgs({
        args: rest,
        options: {
          "runtime-export": { type: "string" },
          summary: { type: "string" },
          mods: { type: "string" },
          out: { type: "string" },
          "pack-id": { type: "string" },
          stages: { type: "string" },
          datapack: { type: "boolean" },
          "datapack-out": { type: "string" },
          "pack-format": { type: "string" },
          force: { type: "boolean" },
          model: { type: "string" },
          backend: { type: "string" },
          "ignore-provider": { type: "string", multiple: true },
          "only-provider": { type: "string", multiple: true },
          "batch-size": { type: "string" },
          concurrency: { type: "string" },
          effort: { type: "string" },
          "thinking-budget": { type: "string" },
          "disable-adaptive-thinking": { type: "boolean" },
          sample: { type: "string" },
          "fixture-dir": { type: "string" },
          "use-replay": { type: "boolean" },
          "record-replay": { type: "boolean" },
          "dry-run": { type: "boolean" },
          "retry-model": { type: "string" },
          "retry-effort": { type: "string" },
          "retry-threshold": { type: "string" },
          "retry-batch-size": { type: "string" },
          "retry-fixture-dir": { type: "string" },
          "retry-use-replay": { type: "boolean" },
          "retry-record-replay": { type: "boolean" },
          "subsystems-file": { type: "string" },
          "verbose-disambiguation": { type: "boolean" },
          "no-verbose-disambiguation": { type: "boolean" },
          "verbose-misconceptions": { type: "boolean" },
        },
        allowPositionals: false,
        strict: true,
      });
      const runtimeExportPath = args.values["runtime-export"];
      if (!runtimeExportPath) {
        console.error("usage: generate-pack-layer --runtime-export <pack.runtime-items.ndjson> [options]");
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
        batchSize: parsePositiveInteger(args.values["batch-size"], "--batch-size"),
        concurrency: parsePositiveInteger(args.values.concurrency, "--concurrency"),
        effort: parseEffort(args.values.effort),
        thinkingBudget: args.values["thinking-budget"]
          ? parsePositiveInteger(args.values["thinking-budget"], "--thinking-budget")
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
          ? parseConfidenceThreshold(args.values["retry-threshold"], "--retry-threshold")
          : undefined,
        retryBatchSize: args.values["retry-batch-size"]
          ? parsePositiveInteger(args.values["retry-batch-size"], "--retry-batch-size")
          : undefined,
        retryFixtureDir: args.values["retry-fixture-dir"],
        retryUseReplay: args.values["retry-use-replay"],
        retryRecordReplay: args.values["retry-record-replay"],
        proposeSubsystems: false,
        subsystemsFile: args.values["subsystems-file"],
        verboseFacetDisambiguation: args.values["no-verbose-disambiguation"]
          ? false
          : (args.values["verbose-disambiguation"] ?? true),
        verboseCommonMisconceptions: args.values["verbose-misconceptions"] ?? false,
      };
      await runGeneratePackLayer(runtimeExportPath, outDir, stages, stage3CliOpts, {
        summaryPath: args.values.summary,
        modsPath: args.values.mods,
        packId: args.values["pack-id"],
        writeDatapack: args.values.datapack ?? false,
        datapackOut: args.values["datapack-out"],
        packFormat: parsePositiveInteger(args.values["pack-format"], "--pack-format"),
        force: args.values.force ?? false,
      });
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
  /** Load canonical subsystem vocabulary from an existing cache/output file.
   *  Supports both `<modid>.subsystems.json` and runtime
   *  `<pack>.runtime-subsystems.json` namespace maps. */
  subsystemsFile?: string;
  /** Pre-resolved canonical vocabulary, plumbed through to stage 3. Set
   *  inside `runMod` after the proposer runs; consumed by `executeStage3`. */
  subsystemVocabulary?: readonly { id: string; rationale?: string }[];
  /** Namespace-scoped vocabulary for mixed-namespace runtime-export batches. */
  subsystemVocabularyByNamespace?: SubsystemVocabularyByNamespace;
  /** Verbose-prompt extras. disambiguation defaults ON
   *  (principle-based per-facet reasoning; A/B testing showed it
   *  carries sonnet from ~50% → ~93% on hard categories). misconceptions
   *  defaults OFF (item-level enumeration kept in reserve for
   *  regressions). CLI: --no-verbose-disambiguation flips disambiguation
   *  off; --verbose-misconceptions flips misconceptions on. */
  verboseFacetDisambiguation?: boolean;
  verboseCommonMisconceptions?: boolean;
}

function attachGenerationMetadata(
  layer: LayerFile,
  metadata: {
    sourceKind?: string;
    sourcePath?: string;
    sourceVersion?: string;
    namespace?: string;
    stages: readonly string[];
    stage3Opts?: Stage3CliOptions;
    inputMetadata?: Record<string, unknown>;
  },
): void {
  const existing = layer.metadata ?? {};
  const prompt = metadata.stage3Opts ? buildPromptMetadata(metadata.stage3Opts) : existing.prompt;
  layer.metadata = {
    ...existing,
    tool: {
      name: "slot-classify",
      version: TOOL_VERSION,
    },
    schema: {
      layer_schema_version: layer.schema_version,
    },
    pipeline: {
      stages: metadata.stages,
      generated_at: layer.generated_at,
    },
    input: {
      ...(isRecord(existing.input) ? existing.input : {}),
      ...(metadata.sourceKind ? { source_kind: metadata.sourceKind } : {}),
      ...(metadata.sourcePath ? { source_path: metadata.sourcePath } : {}),
      ...(metadata.sourceVersion ? { source_version: metadata.sourceVersion } : {}),
      ...(metadata.namespace ? { namespace: metadata.namespace } : {}),
      ...(metadata.inputMetadata ?? {}),
    },
    ...(prompt ? { prompt } : {}),
  };
}

function buildPromptMetadata(opts: Stage3CliOptions): Record<string, unknown> {
  const backend = opts.useReplay ? "replay" : (opts.backend ?? inferBackend(opts.model));
  return {
    version: PROMPT_VERSION,
    backend,
    model: opts.model ?? DEFAULT_STAGE3_MODEL,
    target_facets: defaultTargetFacets(),
    batch_size: opts.batchSize ?? DEFAULT_STAGE3_BATCH_SIZE,
    concurrency: opts.concurrency ?? DEFAULT_STAGE3_CONCURRENCY,
    effort: opts.effort ?? null,
    thinking_budget: opts.thinkingBudget ?? null,
    record_replay: opts.recordReplay,
    use_replay: opts.useReplay,
    fixture_dir: opts.fixtureDir ?? null,
    verbose_facet_disambiguation: opts.verboseFacetDisambiguation ?? true,
    verbose_common_misconceptions: opts.verboseCommonMisconceptions ?? false,
    subsystem_vocabulary: opts.subsystemVocabulary?.map((entry) => entry.id) ?? [],
    subsystem_vocabulary_by_namespace: opts.subsystemVocabularyByNamespace
      ? Object.fromEntries(
          Object.entries(opts.subsystemVocabularyByNamespace)
            .map(([namespace, entries]) => [namespace, entries.map((entry) => entry.id)])
            .filter(([, entries]) => (entries as string[]).length > 0),
        )
      : {},
    subsystem_vocabulary_file: opts.subsystemsFile ?? null,
    retry: opts.retryModel
      ? {
          model: opts.retryModel,
          effort: opts.retryEffort ?? null,
          threshold: opts.retryThreshold ?? 0.5,
          batch_size: opts.retryBatchSize ?? 8,
          fixture_dir: opts.retryFixtureDir ?? null,
        }
      : null,
  };
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return !!value && typeof value === "object" && !Array.isArray(value);
}

function loadBundledPerModIds(): Set<string> {
  const indexPath = resolve(
    REPO_ROOT,
    "common/src/main/resources/data/slot/classification/per-mod/index.json",
  );
  try {
    const parsed = JSON.parse(readFileSync(indexPath, "utf8")) as { mods?: unknown };
    return new Set(
      Array.isArray(parsed.mods)
        ? parsed.mods.filter((mod): mod is string => typeof mod === "string")
        : [],
    );
  } catch {
    return new Set();
  }
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
    attachGenerationMetadata(layer, {
      sourceKind: "mcmeta-summary",
      sourcePath,
      sourceVersion: bundle!.version,
      namespace: VANILLA_NAMESPACE,
      stages: ["stage1", "stage2"],
    });
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
    attachGenerationMetadata(layer, {
      sourceKind: "source-tree",
      sourcePath: modPath,
      sourceVersion: bundle!.version,
      namespace: modNamespace,
      stages: ["stage1", "stage2"],
    });
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

interface ModsFolderClassificationOptions {
  targetMods: readonly string[];
  limitMods?: number;
  includeCovered: boolean;
  force: boolean;
  modConcurrency: number;
}

async function runModsFolderClassification(
  modsPath: string,
  outDir: string,
  stages: StageSelection,
  stage3Opts: Stage3CliOptions,
  options: ModsFolderClassificationOptions,
) {
  const start = Date.now();
  const report = scanModsFolder({
    requestedPath: modsPath,
    generatedBy: TOOL_VERSION,
    bundledModIds: loadBundledPerModIds(),
  });
  const scanPath = join(outDir, "scan-report.json");
  writeFileSync(scanPath, JSON.stringify(report, null, 2) + "\n");

  console.log(formatScanReport(report));
  console.log("");
  console.log(`[classify-folder] scan report: ${scanPath}`);

  const targetSet = new Set(options.targetMods);
  const allCandidates = report.mods
    .filter((mod) => shouldProcessScannedMod(mod, targetSet, options.includeCovered))
    .sort((a, b) => {
      const bySize = b.item_candidate_count - a.item_candidate_count;
      return bySize !== 0 ? bySize : a.id.localeCompare(b.id);
    });

  let skipped = 0;
  let alreadyClassified = 0;
  let processed = 0;
  const failures: Array<{ namespace: string; error: string }> = [];
  const toProcess: InputManifestMod[] = [];

  for (const mod of allCandidates) {
    const completePath = resolve(outDir, `${mod.id}.facets.complete.json`);
    const tag = mod.id.padEnd(28);
    if (options.force && existsSync(completePath)) {
      rmSync(completePath);
      console.log(`[classify-folder] ${tag} force removed existing complete output`);
    }
    if (!options.force && existsSync(completePath)) {
      const reusable = inspectReusableCompleteOutput(completePath);
      if (reusable.ok && reusable.entryCount > 0) {
        alreadyClassified++;
        console.log(`[classify-folder] ${tag} already classified — ${reusable.entryCount} entries → ${completePath}`);
        continue;
      }
      if (!reusable.ok) {
        console.log(`[classify-folder] ${tag} reprocessing — ${reusable.reason}`);
      }
    }
    toProcess.push(mod);
    if (options.limitMods && toProcess.length >= options.limitMods) break;
  }

  skipped = report.mods.length - toProcess.length - alreadyClassified;
  const modConcurrency = Math.max(1, options.modConcurrency);
  const batchConcurrency = stage3Opts.concurrency ?? DEFAULT_STAGE3_CONCURRENCY;
  console.log(
    `[classify-folder] settings: mods=${toProcess.length}/${report.mods.length} ` +
      `mod-concurrency=${modConcurrency} batch-concurrency=${batchConcurrency}` +
      (options.force ? ` force=true` : ``) +
      (options.includeCovered ? ` include-covered=true` : ``),
  );

  let nextIndex = 0;
  const worker = async (workerId: number): Promise<void> => {
    while (true) {
      const idx = nextIndex++;
      if (idx >= toProcess.length) return;
      const mod = toProcess[idx]!;
      const workerLabel = modConcurrency > 1 ? `[w${workerId}] ` : ``;
      console.log("");
      console.log("─".repeat(72));
      console.log(`[classify-folder] ${workerLabel}${mod.id.padEnd(28)} processing — jar ${mod.file_name}`);
      console.log("─".repeat(72));
      try {
        await runJarMod(mod, outDir, stages, stage3Opts);
        processed++;
      } catch (err) {
        const message = err instanceof Error ? err.message : String(err);
        console.error(`[classify-folder] ${workerLabel}${mod.id.padEnd(28)} FAILED — ${message}`);
        failures.push({ namespace: mod.id, error: message });
      }
    }
  };

  await Promise.all(
    Array.from(
      { length: Math.min(modConcurrency, toProcess.length || 1) },
      (_, i) => worker(i + 1),
    ),
  );

  console.log("");
  console.log("=".repeat(72));
  console.log(`Mods-folder run complete: ${report.source.pack_name ?? report.source.resolved_mods_path ?? report.source.requested_path}`);
  console.log("=".repeat(72));
  console.log(`Total scanned mod entries: ${report.mods.length}`);
  console.log(`  skipped:              ${skipped}`);
  console.log(`  already classified:   ${alreadyClassified}`);
  console.log(`  processed:            ${processed}`);
  console.log(`  failed:               ${failures.length}`);
  console.log(`Wall time:              ${((Date.now() - start) / 1000).toFixed(1)}s`);
  if (failures.length > 0) {
    console.log("");
    console.log("Failures:");
    for (const failure of failures) {
      console.log(`  - ${failure.namespace}: ${failure.error.slice(0, 200)}`);
    }
    process.exitCode = 1;
  }
  console.log("=".repeat(72));
}

function shouldProcessScannedMod(
  mod: InputManifestMod,
  targetSet: ReadonlySet<string>,
  includeCovered: boolean,
): boolean {
  const explicitlyTargeted = targetSet.size > 0 && targetSet.has(mod.id);
  if (targetSet.size > 0 && !explicitlyTargeted) return false;
  if (mod.status.startsWith("blocked:")) return false;
  if (mod.status === "skipped:library") return false;
  if (mod.status === "missing:semantic-generation-available") return true;
  if (mod.status.startsWith("covered:")) return includeCovered || explicitlyTargeted;
  return explicitlyTargeted && mod.item_candidate_count > 0;
}

interface RuntimeSubsystemProposalOptions {
  runtimeExportPath: string;
  summaryPath?: string;
  outDir: string;
  namespaces: readonly string[];
  limitNamespaces?: number;
  minItems: number;
  force: boolean;
  opts: Stage3CliOptions;
}

async function runRuntimeSubsystemProposal(
  options: RuntimeSubsystemProposalOptions,
): Promise<void> {
  const start = Date.now();
  const runtimeItemsPath = resolve(options.runtimeExportPath);
  const summaryPath = resolve(options.summaryPath ?? defaultRuntimeSummaryPath(runtimeItemsPath));
  const records = readRuntimeExportRecords(runtimeItemsPath);
  let summary: RuntimeExportSummary | null = null;
  if (existsSync(summaryPath)) {
    summary = readRuntimeExportSummary(summaryPath);
  } else if (options.summaryPath) {
    throw new Error(`runtime summary not found: ${summaryPath}`);
  } else {
    console.warn(`[runtime-subsystems] summary not found at ${summaryPath}; continuing with item records only`);
  }

  const packId = summary?.pack_id ?? summary?.requested_pack_id ?? packIdFromRuntimeItemsPath(runtimeItemsPath);
  const outputPath = join(options.outDir, `${safeFileComponent(packId)}.runtime-subsystems.json`);
  if (existsSync(outputPath) && !options.force && !options.opts.dryRun) {
    console.error(`[runtime-subsystems] output already exists: ${outputPath}`);
    console.error(`[runtime-subsystems] pass --force to regenerate it`);
    process.exit(1);
    return;
  }

  let contexts = buildRuntimeSubsystemContexts({
    records,
    summary,
    namespaces: options.namespaces,
    minItems: options.minItems,
  });
  if (options.limitNamespaces) contexts = contexts.slice(0, options.limitNamespaces);

  console.log(
    `[runtime-subsystems] ${records.length} runtime item record(s), ` +
      `${contexts.length} namespace context(s), pack=${packId}`,
  );
  if (contexts.length === 0) {
    console.log(`[runtime-subsystems] no namespaces matched --namespace/--min-items filters`);
    return;
  }

  const model = options.opts.model ?? DEFAULT_STAGE3_MODEL;
  const backend = options.opts.backend ?? inferBackend(model);

  if (options.opts.dryRun) {
    const dryRunDir = join(options.outDir, "runtime-subsystems-dry-run");
    mkdirSync(dryRunDir, { recursive: true });
    const promptSummary: Array<{ namespace: string; system: string; user: string; chars: number; approxTokens: number }> = [];
    for (const context of contexts) {
      const prompt = buildRuntimeProposerPrompt(context);
      const systemPath = join(dryRunDir, `${context.modNamespace}.system.md`);
      const userPath = join(dryRunDir, `${context.modNamespace}.user.md`);
      writeFileSync(systemPath, prompt.system);
      writeFileSync(userPath, prompt.user);
      const chars = prompt.system.length + prompt.user.length;
      promptSummary.push({
        namespace: context.modNamespace,
        system: systemPath,
        user: userPath,
        chars,
        approxTokens: Math.round(chars / 4),
      });
    }
    const summaryFile = join(dryRunDir, "summary.json");
    writeFileSync(summaryFile, JSON.stringify(promptSummary, null, 2) + "\n");
    console.log(`[runtime-subsystems] dry run: wrote ${contexts.length} prompt pair(s) to ${dryRunDir}`);
    console.log(`[runtime-subsystems] summary → ${summaryFile}`);
    return;
  }

  console.log(`[runtime-subsystems] proposing vocabulary with ${model} via ${backend}`);
  const client = buildClient(options.opts);
  const output: RuntimeSubsystemVocabularyFile = {
    schema_version: 1,
    kind: "slot-runtime-subsystem-vocabulary",
    pack_id: packId,
    generated_by: TOOL_VERSION,
    generated_at: new Date().toISOString(),
    model,
    source: {
      runtime_items: runtimeItemsPath,
      ...(existsSync(summaryPath) ? { runtime_summary: summaryPath } : {}),
      ...(summary?.loader ? { loader: summary.loader } : {}),
      ...(summary?.minecraft_version ? { minecraft_version: summary.minecraft_version } : {}),
      ...(summary?.item_count ? { item_count: summary.item_count } : { item_count: records.length }),
    },
    namespaces: {},
  };

  for (let i = 0; i < contexts.length; i++) {
    const context = contexts[i]!;
    console.log(
      `[runtime-subsystems] ${i + 1}/${contexts.length} ${context.modNamespace} ` +
        `(${context.itemCount} items)`,
    );
    const proposal = await proposeRuntimeSubsystems(context, {
      client,
      model,
      clientOptions: buildClientOptions(
        options.opts.effort,
        options.opts.thinkingBudget,
        options.opts.disableAdaptiveThinking,
      ),
    });
    output.namespaces[context.modNamespace] = {
      modNamespace: context.modNamespace,
      item_count: context.itemCount,
      evidence: contextEvidence(context),
      vocabulary: proposal.vocabulary,
      raw_response: proposal.raw,
    };
    if (proposal.vocabulary.length === 0) {
      console.log(`  (no vocabulary entries)`);
    } else {
      for (const entry of proposal.vocabulary) {
        console.log(`  ${entry.id.padEnd(40)}${entry.rationale ? " " + entry.rationale : ""}`);
      }
    }
  }

  writeFileSync(outputPath, JSON.stringify(output, null, 2) + "\n");
  console.log(`[runtime-subsystems] wrote ${Object.keys(output.namespaces).length} namespace(s) → ${outputPath}`);
  console.log(`done in ${((Date.now() - start) / 1000).toFixed(2)}s`);
}

interface GeneratePackLayerOptions {
  summaryPath?: string;
  modsPath?: string;
  packId?: string;
  writeDatapack: boolean;
  datapackOut?: string;
  packFormat?: number;
  force: boolean;
}

async function runGeneratePackLayer(
  runtimeExportPath: string,
  outDir: string,
  stages: StageSelection,
  stage3Opts: Stage3CliOptions,
  options: GeneratePackLayerOptions,
): Promise<void> {
  const start = Date.now();
  const runtimeItemsPath = resolve(runtimeExportPath);
  const summaryPath = resolve(options.summaryPath ?? defaultRuntimeSummaryPath(runtimeItemsPath));
  let summary: RuntimeExportSummary | null = null;
  if (existsSync(summaryPath)) {
    summary = readRuntimeExportSummary(summaryPath);
  } else if (options.summaryPath) {
    throw new Error(`runtime summary not found: ${summaryPath}`);
  } else {
    console.warn(`[pack-layer] summary not found at ${summaryPath}; continuing with item records only`);
  }

  const packId = safeFileComponent(
    options.packId ?? summary?.pack_id ?? summary?.requested_pack_id ?? packIdFromRuntimeItemsPath(runtimeItemsPath),
  );
  const recordsPath = join(outDir, `${packId}.pack.items.ndjson`);
  const partialPath = join(outDir, `${packId}.pack.facets.partial.json`);
  const completePath = join(outDir, `${packId}.pack.facets.complete.json`);
  for (const path of [recordsPath, partialPath, completePath]) {
    if (existsSync(path) && options.force) rmSync(path);
  }
  if (!options.force && (existsSync(partialPath) || existsSync(completePath)) && (stages.stage2 || stages.stage3)) {
    console.error(`[pack-layer] output already exists for pack ${packId}`);
    console.error(`[pack-layer] pass --force to regenerate ${partialPath} / ${completePath}`);
    process.exit(1);
    return;
  }

  let records = readRuntimeExportRecords(runtimeItemsPath);
  console.log(`[pack-layer] runtime records: ${records.length} item(s), pack=${packId}`);
  if (options.modsPath) {
    const staticRecords = loadStaticEnrichmentRecords(options.modsPath, records);
    const merged = mergeRuntimeWithStaticRecords(records, staticRecords);
    records = merged.records;
    console.log(
      `[pack-layer] static jar enrichment: ${staticRecords.size} matching static record(s), ` +
        `${merged.enriched} runtime record(s) enriched`,
    );
  }

  if (stages.stage1) {
    writeFileSync(recordsPath, records.map((record) => JSON.stringify(record)).join("\n") + "\n");
    console.log(`[stage1] ${records.length} merged runtime/static records → ${recordsPath}`);
  }

  let stage2Layer: LayerFile | null = null;
  if (stages.stage2) {
    const bundle = runtimeSyntheticBundle(records, summary);
    const { layer, coverage, warnings } = runDeterministic({
      records,
      bundle,
      namespace: packId,
    });
    layer.layer = "modpack";
    layer.source = packId;
    layer.generated_by = TOOL_VERSION;
    layer.generated_at = new Date().toISOString();
    attachGenerationMetadata(layer, {
      sourceKind: "runtime-export",
      sourcePath: runtimeItemsPath,
      sourceVersion: summary?.minecraft_version,
      namespace: packId,
      stages: ["stage1", "stage2"],
      inputMetadata: {
        runtime_summary: existsSync(summaryPath) ? summaryPath : null,
        static_mods_path: options.modsPath ? resolve(options.modsPath) : null,
        loader: summary?.loader ?? null,
        minecraft_version: summary?.minecraft_version ?? null,
        runtime_item_count: summary?.item_count ?? records.length,
      },
    });
    const validation = validateLayer(layer);
    if (!validation.ok) {
      console.error(`[stage2] pack layer failed schema validation`);
      for (const err of validation.errors.slice(0, 10)) console.error(`  ${err}`);
      process.exit(1);
      return;
    }
    writeFileSync(partialPath, JSON.stringify(layer, null, 2) + "\n");
    console.log(`[stage2] ${Object.keys(layer.entries).length} items with ≥1 facet → ${partialPath}`);
    console.log(`[stage2] coverage:`);
    const facetOrder = Object.keys(coverage).sort((a, b) => coverage[b]! - coverage[a]!);
    for (const facet of facetOrder) {
      const pct = records.length === 0 ? "0.0" : ((coverage[facet]! / records.length) * 100).toFixed(1);
      console.log(`  ${facet.padEnd(22)} ${String(coverage[facet]).padStart(5)}/${records.length} (${pct}%)`);
    }
    if (warnings.length > 0) {
      console.log(`[stage2] ${warnings.length} warnings:`);
      for (const warning of warnings.slice(0, 20)) console.log(`  ${warning}`);
      if (warnings.length > 20) console.log(`  … and ${warnings.length - 20} more`);
    }
    stage2Layer = layer;
  } else if (stages.stage3) {
    if (!existsSync(partialPath)) {
      console.error(`[stage3] need stage 2 output at ${partialPath}; run with --stages 1,2,3 first`);
      process.exit(1);
      return;
    }
    stage2Layer = JSON.parse(readFileSync(partialPath, "utf8")) as LayerFile;
    console.log(`[stage2] (skipped; loaded ${Object.keys(stage2Layer.entries).length} entries)`);
  }

  let layerForDatapack = stage2Layer ? partialPath : null;
  if (stages.stage3 && stage2Layer) {
    await executeStage3(records, stage2Layer, completePath, stage3Opts);
    if (!stage3Opts.dryRun && existsSync(completePath)) {
      layerForDatapack = completePath;
    }
  }

  if (options.writeDatapack) {
    if (!layerForDatapack || !existsSync(layerForDatapack)) {
      console.warn(`[datapack] no generated layer file available; skipping datapack packaging`);
    } else if (stage3Opts.dryRun) {
      console.warn(`[datapack] stage 3 dry-run requested; skipping datapack packaging`);
    } else {
      const datapackDir = writeClassificationDatapack({
        sourceLayerPath: layerForDatapack,
        outDir,
        explicitOut: options.datapackOut,
        packId,
        packFormat: options.packFormat ?? inferDatapackPackFormat(summary?.minecraft_version),
        force: options.force,
      });
      console.log(`[datapack] wrote ${datapackDir}`);
    }
  }

  console.log(`done in ${((Date.now() - start) / 1000).toFixed(2)}s`);
}

function loadStaticEnrichmentRecords(
  modsPath: string,
  runtimeRecords: readonly ItemExtractRecord[],
): Map<string, ItemExtractRecord> {
  const runtimeIds = new Set(runtimeRecords.map((record) => record.id));
  const report = scanModsFolder({
    requestedPath: modsPath,
    generatedBy: TOOL_VERSION,
    bundledModIds: loadBundledPerModIds(),
  });
  const out = new Map<string, ItemExtractRecord>();
  const candidates = report.mods
    .filter((mod) => !mod.status.startsWith("blocked:") && mod.item_candidate_count > 0)
    .sort((a, b) => a.id.localeCompare(b.id));

  console.log(
    `[pack-layer] scanning static jar data: ${candidates.length} candidate mod jar(s) from ` +
      `${report.source.resolved_mods_path ?? report.source.requested_path}`,
  );
  let failures = 0;
  for (const mod of candidates) {
    try {
      const bundle = loadJarModBundle({
        jarPath: mod.path,
        modNamespace: mod.id,
        version: mod.version,
      });
      const { records } = extractFromModBundle(bundle, TOOL_VERSION);
      let matched = 0;
      for (const record of records) {
        if (!runtimeIds.has(record.id) || out.has(record.id)) continue;
        out.set(record.id, record);
        matched++;
      }
      if (matched > 0) {
        console.log(`[pack-layer] ${mod.id.padEnd(28)} static matches=${matched}`);
      }
    } catch (err) {
      failures++;
      const message = err instanceof Error ? err.message : String(err);
      console.warn(`[pack-layer] ${mod.id.padEnd(28)} static enrichment failed: ${message.slice(0, 160)}`);
    }
  }
  if (failures > 0) {
    console.warn(`[pack-layer] static enrichment skipped ${failures} jar(s) after extraction errors`);
  }
  return out;
}

function mergeRuntimeWithStaticRecords(
  records: readonly ItemExtractRecord[],
  staticRecords: ReadonlyMap<string, ItemExtractRecord>,
): { records: ItemExtractRecord[]; enriched: number } {
  let enriched = 0;
  const merged = records.map((runtime) => {
    const stat = staticRecords.get(runtime.id);
    if (!stat) return runtime;
    enriched++;
    return {
      ...runtime,
      display_name: nonBlank(runtime.display_name) ? runtime.display_name : stat.display_name,
      model_parents: runtime.model_parents.length > 0 ? runtime.model_parents : stat.model_parents,
      loot_table_sources: runtime.loot_table_sources.length > 0 ? runtime.loot_table_sources : stat.loot_table_sources,
      creative_tabs: runtime.creative_tabs.length > 0 ? runtime.creative_tabs : stat.creative_tabs,
      extractor_meta: {
        ...(runtime.extractor_meta ?? {}),
        static_enrichment: "jar",
        static_model_parents: stat.model_parents.length,
        static_loot_sources: stat.loot_table_sources.length,
        static_minecraft_tags_direct: stat.minecraft_tags_direct,
      },
    };
  });
  return { records: merged, enriched };
}

function runtimeSyntheticBundle(
  records: readonly ItemExtractRecord[],
  summary: RuntimeExportSummary | null,
): SummaryBundle {
  const blocks: Record<string, unknown> = {};
  const itemComponents: Record<string, Record<string, unknown>> = {};
  for (const record of records) {
    itemComponents[record.path] = record.component_data ?? {};
    const meta = record.extractor_meta ?? {};
    if (meta["is_block_item"] === true) {
      blocks[record.path] = {};
    }
    if (typeof meta["block_id"] === "string" && meta["block_id"].length > 0) {
      blocks[pathPart(meta["block_id"])] = {};
    }
  }
  return {
    registries: {
      item: records.map((record) => record.id).sort(),
      block: Object.keys(blocks).sort(),
    },
    itemComponents,
    recipes: {},
    lootTables: {},
    itemTags: tagMemberMapToTagJson(summary?.item_tag_members),
    blockTags: tagMemberMapToTagJson(summary?.block_tag_members),
    itemDefinitions: {},
    models: {},
    lang: {},
    blocks,
    version: summary?.minecraft_version ?? "runtime-export",
  };
}

function tagMemberMapToTagJson(
  members: Record<string, string[]> | undefined,
): SummaryBundle["itemTags"] {
  const out: SummaryBundle["itemTags"] = {};
  for (const [tag, values] of Object.entries(members ?? {})) {
    out[tag] = { values };
  }
  return out;
}

function writeClassificationDatapack(options: {
  sourceLayerPath: string;
  outDir: string;
  explicitOut?: string;
  packId: string;
  packFormat: number;
  force: boolean;
}): string {
  const datapackDir = resolve(options.explicitOut ?? join(options.outDir, `${options.packId}.classification-datapack`));
  if (existsSync(datapackDir)) {
    if (!options.force) {
      throw new Error(`datapack output already exists: ${datapackDir}; pass --force to overwrite`);
    }
    rmSync(datapackDir, { recursive: true, force: true });
  }
  const layerDir = join(datapackDir, "data", "slot", "classification", "layers");
  mkdirSync(layerDir, { recursive: true });
  const layerName = `${safeFileComponent(options.packId)}.json`;
  copyFileSync(options.sourceLayerPath, join(layerDir, layerName));
  writeFileSync(
    join(datapackDir, "pack.mcmeta"),
    JSON.stringify(
      {
        pack: {
          pack_format: options.packFormat,
          description: `SLOT classification layer for ${options.packId}`,
        },
      },
      null,
      2,
    ) + "\n",
  );
  return datapackDir;
}

function inferDatapackPackFormat(minecraftVersion: string | undefined): number {
  if (minecraftVersion?.startsWith("1.20")) return 15;
  if (minecraftVersion?.startsWith("1.21")) return 48;
  return 15;
}

function nonBlank(value: string | null | undefined): value is string {
  return typeof value === "string" && value.trim().length > 0;
}

function pathPart(itemId: string): string {
  const colon = itemId.indexOf(":");
  return colon >= 0 ? itemId.slice(colon + 1) : itemId;
}

function packIdFromRuntimeItemsPath(path: string): string {
  const name = basename(path);
  if (name.endsWith(".runtime-items.ndjson")) {
    return name.slice(0, -".runtime-items.ndjson".length);
  }
  if (name.endsWith(".ndjson")) {
    return name.slice(0, -".ndjson".length);
  }
  return name.replace(/[^a-zA-Z0-9_.-]+/g, "_");
}

function safeFileComponent(value: string): string {
  const safe = value.replace(/[^a-zA-Z0-9_.-]+/g, "_");
  return safe.length > 0 ? safe : "runtime-export";
}

async function runJarMod(
  mod: InputManifestMod,
  outDir: string,
  stages: StageSelection,
  stage3Opts: Stage3CliOptions,
) {
  const start = Date.now();
  const needsBundle = stages.stage1 || stages.stage2;
  let bundle: ReturnType<typeof loadJarModBundle> | null = null;
  if (needsBundle) {
    console.log(`[${mod.id}] loading jar resources from ${mod.path}`);
    bundle = loadJarModBundle({
      jarPath: mod.path,
      modNamespace: mod.id,
      version: mod.version,
    });
    console.log(
      `[${mod.id}] jar resources: items=${bundle.registries.item?.length ?? 0}; ` +
        `tags(item)=${Object.keys(bundle.itemTags).length}; recipes=${Object.keys(bundle.recipes).length}`,
    );
  } else {
    console.log(`[${mod.id}] (stages 1+2 skipped — jar load skipped)`);
  }

  const ndjsonPath = join(outDir, `${mod.id}.items.ndjson`);
  const metaPath = join(outDir, `${mod.id}.items.meta.json`);
  const partialPath = join(outDir, `${mod.id}.facets.partial.json`);
  const completePath = join(outDir, `${mod.id}.facets.complete.json`);
  mkdirSync(dirname(ndjsonPath), { recursive: true });

  let records: ItemExtractRecord[];
  if (stages.stage1) {
    const { records: extracted, meta } = extractFromModBundle(bundle!, TOOL_VERSION);
    records = extracted;
    const ndjson = records.map((r) => JSON.stringify(r)).join("\n") + "\n";
    writeFileSync(ndjsonPath, ndjson);
    writeFileSync(
      metaPath,
      JSON.stringify(
        {
          ...meta,
          extractor: `jar:${mod.id}`,
          jar: jarInputMetadata(mod),
        },
        null,
        2,
      ) + "\n",
    );
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
      namespace: mod.id,
    });
    layer.layer = "per-mod";
    layer.source = mod.id;
    layer.generated_by = TOOL_VERSION;
    layer.generated_at = new Date().toISOString();
    attachGenerationMetadata(layer, {
      sourceKind: "jar",
      sourcePath: mod.path,
      sourceVersion: bundle!.version,
      namespace: mod.id,
      stages: ["stage1", "stage2"],
      inputMetadata: jarInputMetadata(mod),
    });
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
      const pct = records.length === 0 ? "0.0" : ((coverage[facet]! / records.length) * 100).toFixed(1);
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
      const cached = readCachedSubsystems(outDir, mod.id);
      if (cached.length > 0) {
        console.log(`[subsystems] using cached vocabulary (${cached.length} entries) for jar-backed run`);
        modOpts = { ...stage3Opts, subsystemVocabulary: cached };
      } else {
        console.log(`[subsystems] jar-backed run has no source README; stage 3 will run without canonical vocabulary.`);
      }
    }
    await executeStage3(records, stage2Layer, completePath, modOpts);
  }

  console.log(`done in ${((Date.now() - start) / 1000).toFixed(2)}s`);
}

function jarInputMetadata(mod: InputManifestMod): Record<string, unknown> {
  return {
    file_name: mod.file_name,
    loader: mod.loader,
    minecraft_versions: mod.minecraft_versions,
    hashes: mod.hashes,
    item_set_signature: mod.item_set_signature,
    item_candidate_count: mod.item_candidate_count,
    resource_counts: mod.resource_counts,
    namespaces: mod.namespaces,
    ...(mod.platform_ids ? { platform_ids: mod.platform_ids } : {}),
  };
}

function readCachedSubsystems(outDir: string, modNamespace: string): SubsystemEntry[] {
  const cachePath = join(outDir, `${modNamespace}.subsystems.json`);
  if (!existsSync(cachePath)) return [];
  try {
    const data = JSON.parse(readFileSync(cachePath, "utf8")) as {
      vocabulary?: SubsystemEntry[];
    };
    return Array.isArray(data.vocabulary) ? data.vocabulary : [];
  } catch {
    return [];
  }
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

function resolveStage3SubsystemVocabulary(opts: Stage3CliOptions): Stage3CliOptions {
  if (!opts.subsystemsFile) return opts;
  const resolvedPath = resolve(opts.subsystemsFile);
  const loaded = loadSubsystemVocabularyFile(resolvedPath);
  const globalVocabulary = mergeSubsystemEntries(
    opts.subsystemVocabulary,
    loaded.vocabulary,
  );
  const byNamespace = mergeSubsystemMaps(
    opts.subsystemVocabularyByNamespace,
    loaded.byNamespace,
  );
  const globalCount = globalVocabulary.length;
  const namespaceCount = byNamespace ? Object.keys(byNamespace).length : 0;
  console.log(
    `[subsystems] loaded vocabulary file ${resolvedPath} ` +
      `(${globalCount} global entr(y/ies), ${namespaceCount} namespace map entr(y/ies))`,
  );
  return {
    ...opts,
    subsystemVocabulary: globalCount > 0 ? globalVocabulary : undefined,
    subsystemVocabularyByNamespace: byNamespace,
  };
}

function mergeSubsystemEntries(
  a: readonly SubsystemEntry[] | undefined,
  b: readonly SubsystemEntry[] | undefined,
): SubsystemEntry[] {
  const out: SubsystemEntry[] = [];
  const seen = new Set<string>();
  for (const entry of [...(a ?? []), ...(b ?? [])]) {
    if (seen.has(entry.id)) continue;
    seen.add(entry.id);
    out.push(entry);
  }
  return out;
}

function mergeSubsystemMaps(
  a: SubsystemVocabularyByNamespace | undefined,
  b: SubsystemVocabularyByNamespace | undefined,
): SubsystemVocabularyByNamespace | undefined {
  const namespaces = new Set([
    ...Object.keys(a ?? {}),
    ...Object.keys(b ?? {}),
  ]);
  if (namespaces.size === 0) return undefined;
  const out: SubsystemVocabularyByNamespace = {};
  for (const namespace of namespaces) {
    const merged = mergeSubsystemEntries(a?.[namespace], b?.[namespace]);
    if (merged.length > 0) out[namespace] = merged;
  }
  return Object.keys(out).length > 0 ? out : undefined;
}

async function executeStage3(
  records: readonly ItemExtractRecord[],
  stage2Layer: LayerFile,
  completePath: string,
  opts: Stage3CliOptions,
) {
  opts = resolveStage3SubsystemVocabulary(opts);
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
    subsystemVocabularyByNamespace: opts.subsystemVocabularyByNamespace,
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
        subsystemVocabularyByNamespace: opts.subsystemVocabularyByNamespace,
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

  attachGenerationMetadata(result.layer, {
    stages: ["stage1", "stage2", "stage3"],
    stage3Opts: opts,
  });
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
    const prompt = buildBatchPrompt({
      items: payloads,
      target_facets: targetFacets,
      subsystem_vocabulary: selectSubsystemVocabularyForRecords(
        batch,
        opts.subsystemVocabulary,
        opts.subsystemVocabularyByNamespace,
      ),
      prompt_extras: {
        verbose_facet_disambiguation: opts.verboseFacetDisambiguation,
        verbose_common_misconceptions: opts.verboseCommonMisconceptions,
      },
    });
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
  scan --mods <mods-folder-or-instance-root> [options]
      Inspect an installed mods folder or Prism-style instance root without
      network or LLM calls. Writes an input-manifest v2 JSON report and prints
      coverage/status counts. If Prism .index/*.pw.toml metadata exists, local
      CurseForge/Modrinth ids are preserved from those files.

  classify --mod <id> --source <path> [options]
      Run stages against a source. Use --mod minecraft with a misode/mcmeta
      checkout (the tools/mcmeta submodule path is fine), or use any other
      mod id with a source repository containing standard Forge/NeoForge
      resource roots. For installed jars, use classify-folder instead.

  classify-folder --mods <mods-folder-or-instance-root> [options]
      Scan an installed mods folder or Prism-style instance root, then run
      stage 1/2 extraction directly from the local jars for missing semantic
      layers. By default it skips bundled/covered mods, libraries, blocked
      jars, and entries whose <modid>.facets.complete.json already exists.
      Pass --mod <id> to target one or more mods, --include-covered to
      regenerate bundled/covered mods, --force to reprocess existing outputs,
      and --stages 1,2,3 to opt into the offline LLM semantic pass.

      Folder-only flags:
        --mod <id>             Target one mod id. Repeatable.
        --limit-mods <n>       Process only the N largest eligible mods.
        --include-covered      Include bundled/public-covered mods.
        --force                Remove existing complete outputs first.
        --mod-concurrency <n>  Process N mods in parallel.

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

  propose-runtime-subsystems --runtime-export <pack.runtime-items.ndjson> [options]
      Build a pack-specific canonical mod_subsystem vocabulary from a live
      runtime export. Reads the matching <pack>.runtime-summary.json by
      default, proposes 0-8 labels per namespace, and writes
      <out>/<pack>.runtime-subsystems.json. Use --namespace <id> to target
      specific mods and --dry-run to write prompts without an LLM call.

      Runtime-subsystem flags:
        --summary <path>        Explicit runtime-summary.json path.
        --namespace <id>        Target one namespace. Repeatable.
        --limit-namespaces <n>  Process only the N largest matched namespaces.
        --min-items <n>         Skip auto-selected namespaces below N items
                                (default 4; explicit --namespace bypasses it).
        --force                 Overwrite an existing runtime-subsystems file.

  generate-pack-layer --runtime-export <pack.runtime-items.ndjson> [options]
      Generate a pack-specific classification layer from a live runtime export.
      Pass --mods <instance-or-mods-folder> to enrich runtime records with
      static jar facts such as model parents and loot sources. With --datapack,
      writes a drop-in datapack folder containing
      data/slot/classification/layers/<pack>.json.

      Pack-layer flags:
        --summary <path>        Explicit runtime-summary.json path.
        --mods <path>           Prism instance root or mods/ folder for static
                                jar enrichment.
        --pack-id <id>          Override output/layer/datapack id.
        --datapack              Package the layer as a datapack folder.
        --datapack-out <path>   Explicit datapack output folder.
        --pack-format <n>       Datapack pack_format (default inferred from
                                runtime MC version; 1.20.x -> 15).
        --force                 Overwrite existing pack-layer/datapack outputs.

  validate <layer.json>
      Validate a layer file against layer.schema.json.

Scan options:
  --mods <path>             Required. May point directly at mods/, at a
                            minecraft/ folder containing mods/, or at a
                            Prism instance root containing minecraft/mods/.
  --out <dir>               Output directory for scan-report.json (default out).
  --json <path>             Explicit JSON report path.

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
  --record-replay           Call the selected live backend AND persist fixtures
                            to --fixture-dir.
  --use-replay              Read responses from --fixture-dir; never call a live
                            backend.
  --dry-run                 Build prompts and stop before any LLM call.

Retry pass (opt-in; runs after the first pass on low-confidence items):
  --retry-model <id>        Retry model (e.g. sonnet). Enabling this turns on retry.
  --retry-effort <level>    Effort for the retry pass — 'max' for heaviest thinking.
  --retry-threshold <n>     Retry items with any LLM facet confidence < n or ambiguous:true. Default 0.5.
  --retry-batch-size <n>    Items per retry LLM call. Default 8.
  --retry-fixture-dir <p>   Separate fixture directory for the retry pass.

Subsystem vocabulary:
  --no-propose-subsystems   Skip the README/metadata pre-pass. Stage 3 then
                            invents mod_subsystem labels per item.
  --subsystems-model <id>   Model id for the proposer call. Default haiku.
                            With the OpenRouter default backend, the proposer
                            uses deepseek/deepseek-v4-flash unless overridden.
                            Cached at <out>/<modid>.subsystems.json — delete
                            to regenerate.
  --subsystems-file <path>  Load an existing subsystem vocabulary file into
                            stage 3. Supports single-mod <modid>.subsystems.json
                            and runtime <pack>.runtime-subsystems.json
                            namespace maps.

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
  bun run src/cli.ts classify --mod createaddition --source ../../reference/classification/createaddition
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

  # Classify missing mods from an installed Prism instance or mods folder:
  bun run src/cli.ts classify-folder --mods /path/to/prism/instance --out out --stages 1,2
  OPENROUTER_API_KEY=sk-or-... \\
    bun run src/cli.ts classify-folder --mods /path/to/prism/instance --mod createaddition \\
      --out out --stages 1,2,3 --record-replay --fixture-dir test/fixtures/createaddition-jar

  # Propose subsystem vocabulary from a live runtime export:
  bun run src/cli.ts propose-runtime-subsystems \\
      --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \\
      --summary modpacks/exports/tfg2.runtime-summary.json \\
      --namespace create --namespace gtceu --dry-run

  # Generate a static+runtime pack layer and package it as a datapack:
  bun run src/cli.ts generate-pack-layer \\
      --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \\
      --summary modpacks/exports/tfg2.runtime-summary.json \\
      --mods /path/to/TerraFirmaGreg-Modern \\
      --subsystems-file out/tfg2.runtime-subsystems.json \\
      --stages 1,2,3 --datapack

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
