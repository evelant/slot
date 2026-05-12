import { copyFileSync, existsSync, mkdirSync, readFileSync, readdirSync, rmSync, statSync, writeFileSync } from "node:fs";
import { basename, dirname, join, relative, resolve } from "node:path";
import { parseArgs } from "node:util";
import { deflateRawSync } from "node:zlib";
import {
  ensureVanillaSource,
  loadSummaryBundle,
  type SummaryBundle,
} from "./extract/vanilla/source.ts";
import {
  extractFromBundle,
  VANILLA_NAMESPACE,
} from "./extract/vanilla/extractor.ts";
import type { ItemExtractRecord, SemanticTextEvidence } from "./extract/record.ts";
import { loadModSourceBundle } from "./extract/mod/source.ts";
import { extractFromModBundle } from "./extract/mod/extractor.ts";
import { loadJarModBundle } from "./extract/jar/source.ts";
import { runDeterministic, type LayerFile } from "./deterministic/run.ts";
import { validateLayer, validateLayerFile } from "./schema/validate.ts";
import {
  validateVocabularyArtifactFile,
  type PackFacetVocabulary,
} from "./schema/vocabulary.ts";
import {
  RecordingLlmClient,
  ReplayLlmClient,
  type LlmClient,
} from "./llm/client.ts";
import { OpenRouterClient } from "./llm/openrouter-client.ts";
import { runStage3 } from "./llm/run.ts";
import { runStage3Retry, selectRetryCandidates } from "./llm/retry.ts";
import {
  buildDocumentContextByItem,
  type DocumentContextBuildStats,
  type DocumentContextByItem,
} from "./llm/document_context.ts";
import {
  buildBatchPrompt,
  buildItemPayload,
  buildPromptFacetVocabulary,
  defaultTargetFacets,
  PROMPT_VERSION,
} from "./llm/prompt.ts";
import { VANILLA_CANARY_ITEMS } from "./llm/canary.ts";
import {
  defaultRuntimeSummaryPath,
  readRuntimeExportRecords,
  readRuntimeExportSummary,
  type RuntimeExportSummary,
} from "./extract/runtime_export.ts";
import {
  buildFacetEvidenceArtifact,
  collectExternalFacetEvidence,
  type FacetEvidenceRecord,
  type FacetEvidenceDiagnostic,
} from "./evidence/facet_evidence.ts";
import {
  proposePackFacetVocabulary,
  readFacetEvidenceArtifactFile,
} from "./vocabulary/pack_vocabulary.ts";
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
const DEFAULT_RUNTIME_PACK_BATCH_SIZE = 25;
const DEFAULT_RUNTIME_PACK_CONCURRENCY = 8;
const REPO_ROOT = resolve(import.meta.dir, "..", "..", "..");
const CLI_ABORT_CONTROLLER = new AbortController();

let shutdownSignal: string | undefined;
for (const signal of ["SIGINT", "SIGTERM"] as const) {
  process.once(signal, () => {
    shutdownSignal = signal;
    console.error(`[slot-classify] received ${signal}; aborting in-flight OpenRouter request(s)`);
    CLI_ABORT_CONTROLLER.abort(new Error(`received ${signal}`));
    setTimeout(() => process.exit(signal === "SIGINT" ? 130 : 143), 5_000).unref();
  });
}

function cliAbortSignal(): AbortSignal {
  return CLI_ABORT_CONTROLLER.signal;
}

interface StageSelection {
  stage1: boolean;
  stage2: boolean;
  stage3: boolean;
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

function loadDotEnv(path = join(REPO_ROOT, ".env")): boolean {
  if (!existsSync(path)) return false;
  const text = readFileSync(path, "utf8");
  let loaded = false;
  for (const rawLine of text.split(/\r?\n/)) {
    const line = rawLine.trim();
    if (!line || line.startsWith("#")) continue;
    const eq = line.indexOf("=");
    if (eq <= 0) continue;
    const key = line.slice(0, eq).trim();
    if (!/^[A-Za-z_][A-Za-z0-9_]*$/.test(key)) continue;
    if (process.env[key] !== undefined) continue;
    process.env[key] = unquoteEnvValue(line.slice(eq + 1).trim());
    loaded = true;
  }
  return loaded;
}

function unquoteEnvValue(value: string): string {
  if (
    (value.startsWith("\"") && value.endsWith("\"")) ||
    (value.startsWith("'") && value.endsWith("'"))
  ) {
    return value.slice(1, -1);
  }
  return value;
}

function ensureLiveBackendConfigured(opts: Stage3CliOptions, context: string): void {
  if (opts.dryRun || opts.useReplay) return;
  if (!process.env.OPENROUTER_API_KEY) {
    throw new Error(
      `${context} requires OPENROUTER_API_KEY. Put it in ${join(REPO_ROOT, ".env")} ` +
        `or export it in the shell before running.`,
    );
  }
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
  loadDotEnv();
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
          "ignore-provider": { type: "string", multiple: true },
          "only-provider": { type: "string", multiple: true },
          "batch-size": { type: "string" },
          concurrency: { type: "string" },
          sample: { type: "string" }, // `canary`, `N`, or comma-separated ids
          "fixture-dir": { type: "string" },
          "use-replay": { type: "boolean" },
          "record-replay": { type: "boolean" },
          "dry-run": { type: "boolean" },
          // stage 3 retry knobs — applied after the first pass on any item
          // whose LLM facets have confidence < retry-threshold or ambiguous:true.
          "retry-model": { type: "string" },
          "retry-threshold": { type: "string" },
          "retry-batch-size": { type: "string" },
          "retry-fixture-dir": { type: "string" },
          "retry-use-replay": { type: "boolean" },
          "retry-record-replay": { type: "boolean" },
          "facet-vocabulary": { type: "string" },
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
        ignoredProviders: (args.values["ignore-provider"] as string[] | undefined) ?? undefined,
        onlyProviders: (args.values["only-provider"] as string[] | undefined) ?? undefined,
        batchSize: parsePositiveInteger(args.values["batch-size"], "--batch-size"),
        concurrency: parsePositiveInteger(args.values.concurrency, "--concurrency"),
        sample: args.values.sample,
        fixtureDir: args.values["fixture-dir"],
        useReplay: args.values["use-replay"] ?? false,
        recordReplay: args.values["record-replay"] ?? false,
        dryRun: args.values["dry-run"] ?? false,
        retryModel: args.values["retry-model"],
        retryThreshold: args.values["retry-threshold"]
          ? parseConfidenceThreshold(args.values["retry-threshold"], "--retry-threshold")
          : undefined,
        retryBatchSize: args.values["retry-batch-size"]
          ? parsePositiveInteger(args.values["retry-batch-size"], "--retry-batch-size")
          : undefined,
        retryFixtureDir: args.values["retry-fixture-dir"],
        retryUseReplay: args.values["retry-use-replay"],
        retryRecordReplay: args.values["retry-record-replay"],
        facetVocabularyFile: args.values["facet-vocabulary"],
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
          "ignore-provider": { type: "string", multiple: true },
          "only-provider": { type: "string", multiple: true },
          "batch-size": { type: "string" },
          concurrency: { type: "string" },
          "mod-concurrency": { type: "string" },
          "fixture-dir": { type: "string" },
          "use-replay": { type: "boolean" },
          "record-replay": { type: "boolean" },
          "dry-run": { type: "boolean" },
          "facet-vocabulary": { type: "string" },
          "verbose-disambiguation": { type: "boolean" },
          "no-verbose-disambiguation": { type: "boolean" },
          "verbose-misconceptions": { type: "boolean" },
          force: { type: "boolean" },
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
        ignoredProviders: (args.values["ignore-provider"] as string[] | undefined) ?? undefined,
        onlyProviders: (args.values["only-provider"] as string[] | undefined) ?? undefined,
        batchSize: parsePositiveInteger(args.values["batch-size"], "--batch-size"),
        concurrency: parsePositiveInteger(args.values.concurrency, "--concurrency"),
        fixtureDir: args.values["fixture-dir"],
        useReplay: args.values["use-replay"] ?? false,
        recordReplay: args.values["record-replay"] ?? false,
        dryRun: args.values["dry-run"] ?? false,
        facetVocabularyFile: args.values["facet-vocabulary"],
        verboseFacetDisambiguation: args.values["no-verbose-disambiguation"]
          ? false
          : (args.values["verbose-disambiguation"] ?? true),
        verboseCommonMisconceptions: args.values["verbose-misconceptions"] ?? false,
      };

      const modpackOpts: ModpackRunOptions = {
        force: args.values.force ?? false,
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
          "ignore-provider": { type: "string", multiple: true },
          "only-provider": { type: "string", multiple: true },
          "batch-size": { type: "string" },
          concurrency: { type: "string" },
          sample: { type: "string" },
          "fixture-dir": { type: "string" },
          "use-replay": { type: "boolean" },
          "record-replay": { type: "boolean" },
          "dry-run": { type: "boolean" },
          "retry-model": { type: "string" },
          "retry-threshold": { type: "string" },
          "retry-batch-size": { type: "string" },
          "retry-fixture-dir": { type: "string" },
          "retry-use-replay": { type: "boolean" },
          "retry-record-replay": { type: "boolean" },
          "facet-vocabulary": { type: "string" },
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
        ignoredProviders: (args.values["ignore-provider"] as string[] | undefined) ?? undefined,
        onlyProviders: (args.values["only-provider"] as string[] | undefined) ?? undefined,
        batchSize: parsePositiveInteger(args.values["batch-size"], "--batch-size"),
        concurrency: parsePositiveInteger(args.values.concurrency, "--concurrency"),
        sample: args.values.sample,
        fixtureDir: args.values["fixture-dir"],
        useReplay: args.values["use-replay"] ?? false,
        recordReplay: args.values["record-replay"] ?? false,
        dryRun: args.values["dry-run"] ?? false,
        retryModel: args.values["retry-model"],
        retryThreshold: args.values["retry-threshold"]
          ? parseConfidenceThreshold(args.values["retry-threshold"], "--retry-threshold")
          : undefined,
        retryBatchSize: args.values["retry-batch-size"]
          ? parsePositiveInteger(args.values["retry-batch-size"], "--retry-batch-size")
          : undefined,
        retryFixtureDir: args.values["retry-fixture-dir"],
        retryUseReplay: args.values["retry-use-replay"],
        retryRecordReplay: args.values["retry-record-replay"],
        facetVocabularyFile: args.values["facet-vocabulary"],
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

    case "classify-runtime-pack": {
      const args = parseArgs({
        args: rest,
        options: {
          "runtime-export": { type: "string" },
          summary: { type: "string" },
          mods: { type: "string" },
          evidence: { type: "string" },
          out: { type: "string" },
          "pack-id": { type: "string" },
          stages: { type: "string" },
          datapack: { type: "boolean" },
          "no-datapack": { type: "boolean" },
          "datapack-out": { type: "string" },
          "pack-format": { type: "string" },
          zip: { type: "boolean" },
          "no-zip": { type: "boolean" },
          force: { type: "boolean" },
          "no-repair": { type: "boolean" },
          "repair-batch-size": { type: "string" },
          "repair-concurrency": { type: "string" },
          model: { type: "string" },
          "ignore-provider": { type: "string", multiple: true },
          "only-provider": { type: "string", multiple: true },
          "batch-size": { type: "string" },
          concurrency: { type: "string" },
          "fixture-dir": { type: "string" },
          "use-replay": { type: "boolean" },
          "record-replay": { type: "boolean" },
          "dry-run": { type: "boolean" },
          "facet-vocabulary": { type: "string" },
          "verbose-disambiguation": { type: "boolean" },
          "no-verbose-disambiguation": { type: "boolean" },
          "verbose-misconceptions": { type: "boolean" },
        },
        allowPositionals: false,
        strict: true,
      });
      const runtimeExportPath = args.values["runtime-export"];
      if (!runtimeExportPath) {
        console.error("usage: classify-runtime-pack --runtime-export <pack.runtime-items.ndjson> [options]");
        process.exit(2);
        return;
      }
      const packId = safeFileComponent(args.values["pack-id"] ?? inferRuntimePackId(runtimeExportPath, args.values.summary));
      const outDir = resolve(args.values.out ?? join("out", packId));
      mkdirSync(outDir, { recursive: true });
      const stages = parseStages(args.values.stages ?? "1,2,3");
      const useReplay = args.values["use-replay"] ?? false;
      const stage3FixtureDir = args.values["fixture-dir"] ?? join(outDir, "fixtures", "stage3");
      const stage3CliOpts: Stage3CliOptions = {
        model: args.values.model ?? DEFAULT_STAGE3_MODEL,
        ignoredProviders: (args.values["ignore-provider"] as string[] | undefined) ?? undefined,
        onlyProviders: (args.values["only-provider"] as string[] | undefined) ?? undefined,
        batchSize: parsePositiveInteger(args.values["batch-size"], "--batch-size") ?? DEFAULT_RUNTIME_PACK_BATCH_SIZE,
        concurrency: parsePositiveInteger(args.values.concurrency, "--concurrency") ?? DEFAULT_RUNTIME_PACK_CONCURRENCY,
        fixtureDir: stage3FixtureDir,
        useReplay,
        recordReplay: useReplay ? false : (args.values["record-replay"] ?? true),
        dryRun: args.values["dry-run"] ?? false,
        facetVocabularyFile: args.values["facet-vocabulary"],
        documentContextFile: args.values.evidence,
        verboseFacetDisambiguation: args.values["no-verbose-disambiguation"]
          ? false
          : (args.values["verbose-disambiguation"] ?? true),
        verboseCommonMisconceptions: args.values["verbose-misconceptions"] ?? false,
      };
      await runClassifyRuntimePack({
        runtimeExportPath,
        summaryPath: args.values.summary,
        modsPath: args.values.mods,
        outDir,
        packId,
        stages,
        stage3Opts: stage3CliOpts,
        writeDatapack: !(args.values["no-datapack"] ?? false) && (args.values.datapack ?? true),
        datapackOut: args.values["datapack-out"],
        packFormat: parsePositiveInteger(args.values["pack-format"], "--pack-format"),
        zipDatapack: !(args.values["no-zip"] ?? false) && (args.values.zip ?? true),
        force: args.values.force ?? false,
        repairMissing: !(args.values["no-repair"] ?? false),
        repairBatchSize: parsePositiveInteger(args.values["repair-batch-size"], "--repair-batch-size") ?? 25,
        repairConcurrency: parsePositiveInteger(args.values["repair-concurrency"], "--repair-concurrency") ?? 8,
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
          evidence: { type: "string" },
          out: { type: "string" },
          "pack-id": { type: "string" },
          stages: { type: "string" },
          datapack: { type: "boolean" },
          "datapack-out": { type: "string" },
          "pack-format": { type: "string" },
          force: { type: "boolean" },
          model: { type: "string" },
          "ignore-provider": { type: "string", multiple: true },
          "only-provider": { type: "string", multiple: true },
          "batch-size": { type: "string" },
          concurrency: { type: "string" },
          sample: { type: "string" },
          "fixture-dir": { type: "string" },
          "use-replay": { type: "boolean" },
          "record-replay": { type: "boolean" },
          "dry-run": { type: "boolean" },
          "retry-model": { type: "string" },
          "retry-threshold": { type: "string" },
          "retry-batch-size": { type: "string" },
          "retry-fixture-dir": { type: "string" },
          "retry-use-replay": { type: "boolean" },
          "retry-record-replay": { type: "boolean" },
          "facet-vocabulary": { type: "string" },
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
        ignoredProviders: (args.values["ignore-provider"] as string[] | undefined) ?? undefined,
        onlyProviders: (args.values["only-provider"] as string[] | undefined) ?? undefined,
        batchSize: parsePositiveInteger(args.values["batch-size"], "--batch-size"),
        concurrency: parsePositiveInteger(args.values.concurrency, "--concurrency"),
        sample: args.values.sample,
        fixtureDir: args.values["fixture-dir"],
        useReplay: args.values["use-replay"] ?? false,
        recordReplay: args.values["record-replay"] ?? false,
        dryRun: args.values["dry-run"] ?? false,
        retryModel: args.values["retry-model"],
        retryThreshold: args.values["retry-threshold"]
          ? parseConfidenceThreshold(args.values["retry-threshold"], "--retry-threshold")
          : undefined,
        retryBatchSize: args.values["retry-batch-size"]
          ? parsePositiveInteger(args.values["retry-batch-size"], "--retry-batch-size")
          : undefined,
        retryFixtureDir: args.values["retry-fixture-dir"],
        retryUseReplay: args.values["retry-use-replay"],
        retryRecordReplay: args.values["retry-record-replay"],
        facetVocabularyFile: args.values["facet-vocabulary"],
        documentContextFile: args.values.evidence,
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

    case "collect-pack-facet-evidence": {
      const args = parseArgs({
        args: rest,
        options: {
          "runtime-export": { type: "string" },
          summary: { type: "string" },
          mods: { type: "string" },
          out: { type: "string" },
          "pack-id": { type: "string" },
          force: { type: "boolean" },
        },
        allowPositionals: false,
        strict: true,
      });
      const runtimeExportPath = args.values["runtime-export"];
      if (!runtimeExportPath) {
        console.error("usage: collect-pack-facet-evidence --runtime-export <pack.runtime-items.ndjson> [options]");
        process.exit(2);
        return;
      }
      const outDir = resolve(args.values.out ?? "out");
      mkdirSync(outDir, { recursive: true });
      runCollectPackFacetEvidence(runtimeExportPath, outDir, {
        summaryPath: args.values.summary,
        modsPath: args.values.mods,
        packId: args.values["pack-id"],
        force: args.values.force ?? false,
      });
      return;
    }

    case "propose-pack-facet-vocabulary": {
      const args = parseArgs({
        args: rest,
        options: {
          evidence: { type: "string" },
          out: { type: "string" },
          "pack-id": { type: "string" },
          "previous-vocabulary": { type: "string" },
          facet: { type: "string", multiple: true },
          namespace: { type: "string", multiple: true },
          "min-evidence": { type: "string" },
          "max-candidates-per-facet": { type: "string" },
          force: { type: "boolean" },
          model: { type: "string" },
          "ignore-provider": { type: "string", multiple: true },
          "only-provider": { type: "string", multiple: true },
          "fixture-dir": { type: "string" },
          "use-replay": { type: "boolean" },
          "record-replay": { type: "boolean" },
          "dry-run": { type: "boolean" },
        },
        allowPositionals: false,
        strict: true,
      });
      const evidencePath = args.values.evidence;
      if (!evidencePath) {
        console.error("usage: propose-pack-facet-vocabulary --evidence <pack.facet-evidence.json> [options]");
        process.exit(2);
        return;
      }
      const outDir = resolve(args.values.out ?? "out");
      mkdirSync(outDir, { recursive: true });
      await runProposePackFacetVocabulary({
        evidencePath,
        outDir,
        packId: args.values["pack-id"],
        previousVocabularyPath: args.values["previous-vocabulary"],
        facets: (args.values.facet as string[] | undefined) ?? [],
        namespaces: (args.values.namespace as string[] | undefined) ?? [],
        minEvidence: parsePositiveInteger(args.values["min-evidence"], "--min-evidence") ?? 2,
        maxCandidatesPerFacet: parsePositiveInteger(args.values["max-candidates-per-facet"], "--max-candidates-per-facet"),
        force: args.values.force ?? false,
        opts: {
          model: args.values.model,
          ignoredProviders: (args.values["ignore-provider"] as string[] | undefined) ?? undefined,
          onlyProviders: (args.values["only-provider"] as string[] | undefined) ?? undefined,
          fixtureDir: args.values["fixture-dir"],
          useReplay: args.values["use-replay"] ?? false,
          recordReplay: args.values["record-replay"] ?? false,
          dryRun: args.values["dry-run"] ?? false,
        },
      });
      return;
    }

    case "validate": {
      const args = parseArgs({
        args: rest,
        options: {
          vocabulary: { type: "string" },
        },
        allowPositionals: true,
        strict: true,
      });
      const target = args.positionals[0];
      if (!target) {
        console.error("usage: validate <layer.json> [--vocabulary <facet-vocabulary.json>]");
        process.exit(2);
        return;
      }
      let vocabulary;
      if (args.values.vocabulary) {
        const vocabularyResult = validateVocabularyArtifactFile(resolve(args.values.vocabulary));
        if (!vocabularyResult.ok || !vocabularyResult.vocabulary) {
          console.error(`invalid vocabulary: ${args.values.vocabulary}`);
          for (const err of vocabularyResult.errors) console.error(`  ${err}`);
          process.exit(1);
          return;
        }
        vocabulary = vocabularyResult.vocabulary;
      }
      const result = validateLayerFile(resolve(target), { vocabulary });
      if (result.ok) {
        console.log(`ok: ${target}`);
        return;
      }
      console.error(`invalid: ${target}`);
      for (const err of result.errors) console.error(`  ${err}`);
      process.exit(1);
      return;
    }

    case "validate-vocabulary": {
      const args = parseArgs({
        args: rest,
        options: {},
        allowPositionals: true,
        strict: true,
      });
      const target = args.positionals[0];
      if (!target) {
        console.error("usage: validate-vocabulary <facet-vocabulary.json>");
        process.exit(2);
        return;
      }
      const result = validateVocabularyArtifactFile(resolve(target));
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
  /** Provider slugs to exclude from OpenRouter routing (e.g.
   *  `["deepinfra"]`). Forwarded as `provider.ignore` per request.
   *  Useful when an upstream provider is rate-limited or returning
   *  flaky responses for our prompt shape. */
  ignoredProviders?: readonly string[];
  /** Provider slugs to **pin** OpenRouter to (e.g. `["deepseek"]`).
   *  Forwarded as `provider.only` + `allow_fallbacks: false`. Takes
   *  precedence over ignoredProviders. */
  onlyProviders?: readonly string[];
  batchSize?: number;
  concurrency?: number;
  sample?: string;
  fixtureDir?: string;
  useReplay: boolean;
  recordReplay: boolean;
  dryRun: boolean;
  /** If set, run a retry pass with this OpenRouter model after the first pass. */
  retryModel?: string;
  retryThreshold?: number;
  retryBatchSize?: number;
  retryFixtureDir?: string;
  /** Override the retry pass's record/replay mode. Defaults to recording
   *  when --retry-fixture-dir is set, since we usually don't have retry
   *  fixtures pre-populated. */
  retryUseReplay?: boolean;
  retryRecordReplay?: boolean;
  /** Accepted pack facet vocabulary for vocabulary-backed semantic facets. */
  facetVocabularyFile?: string;
  facetVocabulary?: PackFacetVocabulary;
  /** Optional facet-evidence artifact to convert into per-item document_context. */
  documentContextFile?: string;
  /** Pre-resolved document context, keyed by runtime item id. */
  documentContextByItem?: DocumentContextByItem;
  /** Stats from loading documentContextFile; surfaced in metadata/logs. */
  documentContextStats?: DocumentContextBuildStats;
  /** Verbose-prompt extras. disambiguation defaults ON
   *  (principle-based per-facet reasoning; A/B testing showed it
   *  carries hard-category accuracy substantially). misconceptions
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
  return {
    version: PROMPT_VERSION,
    backend: opts.useReplay ? "replay" : "openrouter",
    model: opts.model ?? DEFAULT_STAGE3_MODEL,
    target_facets: defaultTargetFacets(),
    batch_size: opts.batchSize ?? DEFAULT_STAGE3_BATCH_SIZE,
    concurrency: opts.concurrency ?? DEFAULT_STAGE3_CONCURRENCY,
    record_replay: opts.recordReplay,
    use_replay: opts.useReplay,
    fixture_dir: opts.fixtureDir ?? null,
    verbose_facet_disambiguation: opts.verboseFacetDisambiguation ?? true,
    verbose_common_misconceptions: opts.verboseCommonMisconceptions ?? false,
    document_context_file: opts.documentContextFile ?? null,
    document_context_items: opts.documentContextStats?.items_with_context ?? 0,
    document_context_links: opts.documentContextStats?.context_count ?? 0,
    facet_vocabulary_file: opts.facetVocabularyFile ?? null,
    retry: opts.retryModel
      ? {
          model: opts.retryModel,
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
    await executeStage3(records, stage2Layer, completePath, stage3Opts);
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
   *  scratch instead of resuming. */
  force?: boolean;
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
      (modpackOpts.force ? ` force=true` : ``),
  );

  // Cache-clear pass — runs before planModpack so the planner sees
  // post-clean state and routes every non-skipped mod to "process".
  if (modpackOpts.force) {
    const cleared = clearModpackCaches(resolved, outDir);
    console.log(`[modpack] cleared caches: ${cleared} facets-complete`);
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
): number {
  let facets = 0;
  for (const entry of resolved.pack.mods) {
    if (entry.skip) continue;
    const completePath = resolve(outDir, `${entry.namespace}.facets.complete.json`);
    if (existsSync(completePath)) {
      rmSync(completePath);
      facets++;
    }
  }
  return facets;
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

interface ClassifyRuntimePackOptions {
  runtimeExportPath: string;
  summaryPath?: string;
  modsPath?: string;
  outDir: string;
  packId: string;
  stages: StageSelection;
  stage3Opts: Stage3CliOptions;
  writeDatapack: boolean;
  datapackOut?: string;
  packFormat?: number;
  zipDatapack: boolean;
  force: boolean;
  repairMissing: boolean;
  repairBatchSize: number;
  repairConcurrency: number;
}

async function runClassifyRuntimePack(options: ClassifyRuntimePackOptions): Promise<void> {
  const start = Date.now();
  console.log(`[runtime-pack] pack=${options.packId}`);
  console.log(`[runtime-pack] out=${options.outDir}`);
  const stage3Enabled = options.stages.stage3;

  let stage3Opts = { ...options.stage3Opts };
  if (stage3Enabled) {
    ensureLiveBackendConfigured(stage3Opts, "classify-runtime-pack");
  }

  const run = await runGeneratePackLayer(
    options.runtimeExportPath,
    options.outDir,
    options.stages,
    stage3Opts,
    {
      summaryPath: options.summaryPath,
      modsPath: options.modsPath,
      packId: options.packId,
      writeDatapack: options.writeDatapack,
      datapackOut: options.datapackOut,
      packFormat: options.packFormat,
      force: options.force,
    },
  );

  let noLlmBeforeRepair: string[] = [];
  let noLlmAfterRepair: string[] = [];
  let repairedItems = 0;
  let repairedFacets = 0;

  if (stage3Enabled && !stage3Opts.dryRun && existsSync(run.completePath)) {
    noLlmBeforeRepair = collectItemsWithoutLlmFacets(readLayerFile(run.completePath));
    const noLlmPath = join(options.outDir, `${options.packId}.pack.no-llm-items.json`);
    writeFileSync(noLlmPath, JSON.stringify(noLlmBeforeRepair, null, 2) + "\n");
    console.log(`[runtime-pack] LLM coverage gap after main pass: ${noLlmBeforeRepair.length} item(s)`);

    if (noLlmBeforeRepair.length > 0 && options.repairMissing) {
      const repairDir = join(options.outDir, "repair");
      mkdirSync(repairDir, { recursive: true });
      const repairPath = join(repairDir, `${options.packId}.pack.facets.complete.json`);
      const repairOpts: Stage3CliOptions = {
        ...stage3Opts,
        sample: noLlmBeforeRepair.join(","),
        batchSize: options.repairBatchSize,
        concurrency: options.repairConcurrency,
        fixtureDir: join(options.outDir, "fixtures", `stage3-repair-b${options.repairBatchSize}`),
        recordReplay: stage3Opts.useReplay ? false : true,
      };
      console.log(
        `[runtime-pack] repairing ${noLlmBeforeRepair.length} item(s) with ` +
          `batch-size=${options.repairBatchSize} concurrency=${options.repairConcurrency}`,
      );
      const stage2Layer = readLayerFile(run.partialPath);
      await executeStage3(run.records, stage2Layer, repairPath, repairOpts);
      const merged = mergeLlmFacetsFromRepair({
        fullPath: run.completePath,
        repairPath,
        itemIds: noLlmBeforeRepair,
      });
      repairedItems = merged.itemsTouched;
      repairedFacets = merged.facetsAdded;
      console.log(`[runtime-pack] merged repair facets: items=${repairedItems}, facets=${repairedFacets}`);
      if (run.datapackDir) {
        refreshDatapackLayer(run.datapackDir, options.packId, run.completePath);
      }
    }

    noLlmAfterRepair = collectItemsWithoutLlmFacets(readLayerFile(run.completePath));
    writeFileSync(
      join(options.outDir, `${options.packId}.pack.no-llm-items.after-repair.json`),
      JSON.stringify(noLlmAfterRepair, null, 2) + "\n",
    );
    console.log(`[runtime-pack] LLM coverage gap after repair: ${noLlmAfterRepair.length} item(s)`);
  }

  let datapackZipPath: string | undefined;
  if (options.zipDatapack && run.datapackDir && existsSync(run.datapackDir) && !stage3Opts.dryRun) {
    datapackZipPath = `${run.datapackDir}.zip`;
    writeZipFromDirectory(run.datapackDir, datapackZipPath, { force: true });
    console.log(`[runtime-pack] zipped datapack → ${datapackZipPath}`);
  }

  const finalLayerPath = stage3Enabled && existsSync(run.completePath) ? run.completePath : run.layerForDatapack;
  if (finalLayerPath && existsSync(finalLayerPath) && !stage3Opts.dryRun) {
    const validation = validateLayerFile(finalLayerPath);
    if (!validation.ok) {
      throw new Error(`final layer failed validation: ${validation.errors.slice(0, 5).join("; ")}`);
    }
    if (run.datapackDir) {
      const datapackLayer = datapackLayerPath(run.datapackDir, options.packId);
      const datapackValidation = validateLayerFile(datapackLayer);
      if (!datapackValidation.ok) {
        throw new Error(`datapack layer failed validation: ${datapackValidation.errors.slice(0, 5).join("; ")}`);
      }
    }
  }

  writeRuntimePackReports({
    outDir: options.outDir,
    packId: options.packId,
    elapsedSeconds: (Date.now() - start) / 1000,
    run,
    stage3Enabled,
    noLlmBeforeRepair,
    noLlmAfterRepair,
    repairedItems,
    repairedFacets,
    datapackZipPath,
  });
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

interface GeneratePackLayerRunResult {
  packId: string;
  runtimeItemsPath: string;
  summaryPath: string;
  staticModsPath?: string;
  staticMatchingRecords: number;
  staticEnrichedRecords: number;
  recordsPath: string;
  partialPath: string;
  completePath: string;
  layerForDatapack: string | null;
  datapackDir?: string;
  records: ItemExtractRecord[];
  summary: RuntimeExportSummary | null;
}

interface CollectPackFacetEvidenceOptions {
  summaryPath?: string;
  modsPath?: string;
  packId?: string;
  force: boolean;
}

interface CollectPackFacetEvidenceResult {
  packId: string;
  evidencePath: string;
  runtimeItemsPath: string;
  summaryPath: string;
  records: number;
  evidenceRecords: number;
  diagnostics: number;
}

interface ProposePackFacetVocabularyCliOptions {
  evidencePath: string;
  outDir: string;
  packId?: string;
  previousVocabularyPath?: string;
  facets: readonly string[];
  namespaces: readonly string[];
  minEvidence: number;
  maxCandidatesPerFacet?: number;
  force: boolean;
  opts: Stage3CliOptions;
}

function runCollectPackFacetEvidence(
  runtimeExportPath: string,
  outDir: string,
  options: CollectPackFacetEvidenceOptions,
): CollectPackFacetEvidenceResult {
  const start = Date.now();
  const runtimeItemsPath = resolve(runtimeExportPath);
  const summaryPath = resolve(options.summaryPath ?? defaultRuntimeSummaryPath(runtimeItemsPath));
  let summary: RuntimeExportSummary | null = null;
  if (existsSync(summaryPath)) {
    summary = readRuntimeExportSummary(summaryPath);
  } else if (options.summaryPath) {
    throw new Error(`runtime summary not found: ${summaryPath}`);
  } else {
    console.warn(`[facet-evidence] summary not found at ${summaryPath}; continuing with item records only`);
  }

  const packId = safeFileComponent(
    options.packId ?? summary?.pack_id ?? summary?.requested_pack_id ?? packIdFromRuntimeItemsPath(runtimeItemsPath),
  );
  const evidencePath = join(outDir, `${packId}.facet-evidence.json`);
  if (existsSync(evidencePath)) {
    if (!options.force) {
      console.error(`[facet-evidence] output already exists for pack ${packId}: ${evidencePath}`);
      console.error(`[facet-evidence] pass --force to regenerate it`);
      process.exit(1);
      throw new Error(`output already exists for pack ${packId}`);
    }
    rmSync(evidencePath);
  }

  let records = readRuntimeExportRecords(runtimeItemsPath);
  const externalRecords: FacetEvidenceRecord[] = [];
  const diagnostics: FacetEvidenceDiagnostic[] = [];
  console.log(`[facet-evidence] runtime records: ${records.length} item(s), pack=${packId}`);

  if (options.modsPath) {
    const staticRecords = loadStaticEnrichmentRecords(options.modsPath, records);
    const merged = mergeRuntimeWithStaticRecords(records, staticRecords);
    records = merged.records;
    console.log(
      `[facet-evidence] static jar enrichment: ${staticRecords.size} matching static record(s), ` +
        `${merged.enriched} runtime record(s) enriched`,
    );

    const external = collectExternalFacetEvidence({
      modsPath: options.modsPath,
      generatedBy: TOOL_VERSION,
      bundledModIds: loadBundledPerModIds(),
    });
    externalRecords.push(...external.records);
    diagnostics.push(...external.diagnostics);
    console.log(
      `[facet-evidence] optional pack adapters: ${external.records.length} evidence record(s), ` +
        `${external.diagnostics.length} diagnostic(s)`,
    );
  } else {
    diagnostics.push({
      adapter: "mods-folder",
      severity: "info",
      source: runtimeItemsPath,
      message: "--mods not provided; static jar, guide, quest, advancement, and mod metadata evidence skipped",
      count: 0,
    });
  }

  const artifact = buildFacetEvidenceArtifact({
    packId,
    generatedBy: TOOL_VERSION,
    runtimeItemsPath,
    runtimeSummaryPath: existsSync(summaryPath) ? summaryPath : undefined,
    modsPath: options.modsPath ? resolve(options.modsPath) : undefined,
    records,
    summary,
    externalRecords,
    diagnostics,
  });
  writeFileSync(evidencePath, JSON.stringify(artifact, null, 2) + "\n");
  console.log(`[facet-evidence] wrote ${artifact.records.length} evidence record(s) -> ${evidencePath}`);
  const counts = countBy(artifact.records.map((record) => record.kind));
  for (const [kind, count] of Object.entries(counts).sort(([a], [b]) => a.localeCompare(b))) {
    console.log(`  ${kind.padEnd(22)} ${String(count).padStart(5)}`);
  }
  if (artifact.diagnostics.length > 0) {
    console.log(`[facet-evidence] ${artifact.diagnostics.length} diagnostic(s)`);
  }
  console.log(`done in ${((Date.now() - start) / 1000).toFixed(2)}s`);
  return {
    packId,
    evidencePath,
    runtimeItemsPath,
    summaryPath,
    records: records.length,
    evidenceRecords: artifact.records.length,
    diagnostics: artifact.diagnostics.length,
  };
}

async function runProposePackFacetVocabulary(
  options: ProposePackFacetVocabularyCliOptions,
): Promise<void> {
  const start = Date.now();
  const evidencePath = resolve(options.evidencePath);
  const evidence = readFacetEvidenceArtifactFile(evidencePath);
  const packId = safeFileComponent(options.packId ?? evidence.pack_id);
  const vocabularyPath = join(options.outDir, `${packId}.facet-vocabulary.json`);
  const reviewPath = join(options.outDir, `${packId}.facet-vocabulary.review.json`);

  let previousVocabulary: PackFacetVocabulary | undefined;
  let previousVocabularyPath: string | undefined;
  if (options.previousVocabularyPath) {
    previousVocabularyPath = resolve(options.previousVocabularyPath);
    const previous = validateVocabularyArtifactFile(previousVocabularyPath);
    if (!previous.ok || !previous.vocabulary) {
      console.error(`[facet-vocabulary] invalid previous vocabulary: ${previousVocabularyPath}`);
      for (const error of previous.errors) console.error(`  ${error}`);
      process.exit(1);
      return;
    }
    previousVocabulary = previous.vocabulary;
  }

  if (!options.opts.dryRun && !options.force && (existsSync(vocabularyPath) || existsSync(reviewPath))) {
    console.error(`[facet-vocabulary] output already exists for pack ${packId}`);
    console.error(`[facet-vocabulary] pass --force to regenerate ${vocabularyPath} / ${reviewPath}`);
    process.exit(1);
    return;
  }
  if (!options.opts.dryRun && options.force) {
    if (existsSync(vocabularyPath)) rmSync(vocabularyPath);
    if (existsSync(reviewPath)) rmSync(reviewPath);
  }

  const model = options.opts.model ?? DEFAULT_STAGE3_MODEL;
  console.log(
    `[facet-vocabulary] evidence=${evidence.records.length} record(s), pack=${packId}, ` +
      `facets=${options.facets.length > 0 ? options.facets.join(",") : "all"}, ` +
      `namespaces=${options.namespaces.length > 0 ? options.namespaces.join(",") : "all"}`,
  );

  const client = options.opts.dryRun ? undefined : buildVocabularyClient(options.opts);
  const result = await proposePackFacetVocabulary({
    evidence,
    evidencePath,
    packId,
    generatedBy: TOOL_VERSION,
    previousVocabulary,
    previousVocabularyPath,
    facets: options.facets,
    namespaces: options.namespaces,
    minEvidence: options.minEvidence,
    maxCandidatesPerFacet: options.maxCandidatesPerFacet,
    model,
    client,
    clientOptions: { signal: cliAbortSignal() },
  });

  if (options.opts.dryRun) {
    const dryRunDir = join(options.outDir, `${packId}.facet-vocabulary-dry-run`);
    if (existsSync(dryRunDir)) {
      if (!options.force) {
        console.error(`[facet-vocabulary] dry-run output already exists for pack ${packId}: ${dryRunDir}`);
        console.error(`[facet-vocabulary] pass --force to regenerate it`);
        process.exit(1);
        return;
      }
      rmSync(dryRunDir, { recursive: true, force: true });
    }
    mkdirSync(dryRunDir, { recursive: true });
    const summary: Array<{ facet: string; system: string; user: string; candidates: number; chars: number; approxTokens: number }> = [];
    for (const [facet, prompt] of Object.entries(result.prompts).sort(([a], [b]) => a.localeCompare(b))) {
      const systemPath = join(dryRunDir, `${facet}.system.md`);
      const userPath = join(dryRunDir, `${facet}.user.json`);
      writeFileSync(systemPath, prompt.system);
      writeFileSync(userPath, prompt.user);
      const parsedUser = JSON.parse(prompt.user) as { candidates?: unknown[] };
      const chars = prompt.system.length + prompt.user.length;
      summary.push({
        facet,
        system: systemPath,
        user: userPath,
        candidates: Array.isArray(parsedUser.candidates) ? parsedUser.candidates.length : 0,
        chars,
        approxTokens: Math.round(chars / 4),
      });
    }
    const summaryPath = join(dryRunDir, "summary.json");
    writeFileSync(summaryPath, JSON.stringify(summary, null, 2) + "\n");
    console.log(`[facet-vocabulary] dry run: wrote ${summary.length} prompt pair(s) to ${dryRunDir}`);
    console.log(`[facet-vocabulary] summary -> ${summaryPath}`);
    return;
  }

  const blockingErrors = result.review.diagnostics.filter((diagnostic) => diagnostic.severity === "error");
  if (blockingErrors.length > 0) {
    console.error(`[facet-vocabulary] generated vocabulary failed validation`);
    for (const diagnostic of blockingErrors.slice(0, 20)) console.error(`  ${diagnostic.message}`);
    process.exit(1);
    return;
  }

  writeFileSync(vocabularyPath, JSON.stringify(result.vocabulary, null, 2) + "\n");
  writeFileSync(reviewPath, JSON.stringify(result.review, null, 2) + "\n");
  console.log(`[facet-vocabulary] wrote ${vocabularyPath}`);
  console.log(`[facet-vocabulary] wrote ${reviewPath}`);

  const acceptedCounts: Array<[string, number]> =
    Object.entries(result.vocabulary.facets)
      .map(([facet, value]): [string, number] => [facet, Object.keys(value.values).length])
      .sort(([a], [b]) => a.localeCompare(b));
  for (const [facet, count] of acceptedCounts) {
    console.log(`  ${facet.padEnd(24)} ${String(count).padStart(4)} accepted`);
  }
  const reviewCount = Object.values(result.review.decisions).flat()
    .filter((decision) => decision.state !== "accepted").length;
  console.log(`[facet-vocabulary] review/rejected decision(s): ${reviewCount}`);
  console.log(`done in ${((Date.now() - start) / 1000).toFixed(2)}s`);
}

function buildVocabularyClient(opts: Stage3CliOptions): LlmClient {
  ensureLiveBackendConfigured(opts, "propose-pack-facet-vocabulary");
  return buildClient(opts);
}

async function runGeneratePackLayer(
  runtimeExportPath: string,
  outDir: string,
  stages: StageSelection,
  stage3Opts: Stage3CliOptions,
  options: GeneratePackLayerOptions,
): Promise<GeneratePackLayerRunResult> {
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
    throw new Error(`output already exists for pack ${packId}`);
  }

  let records = readRuntimeExportRecords(runtimeItemsPath);
  let staticMatchingRecords = 0;
  let staticEnrichedRecords = 0;
  console.log(`[pack-layer] runtime records: ${records.length} item(s), pack=${packId}`);
  if (options.modsPath) {
    const staticRecords = loadStaticEnrichmentRecords(options.modsPath, records);
    const merged = mergeRuntimeWithStaticRecords(records, staticRecords);
    records = merged.records;
    staticMatchingRecords = staticRecords.size;
    staticEnrichedRecords = merged.enriched;
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
      throw new Error(`stage 2 pack layer failed schema validation`);
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
      throw new Error(`missing stage 2 output at ${partialPath}`);
    }
    stage2Layer = JSON.parse(readFileSync(partialPath, "utf8")) as LayerFile;
    console.log(`[stage2] (skipped; loaded ${Object.keys(stage2Layer.entries).length} entries)`);
  }

  let layerForDatapack = stage2Layer ? partialPath : null;
  if (stages.stage3 && stage2Layer) {
    ensureLiveBackendConfigured(stage3Opts, "generate-pack-layer stage 3");
    await executeStage3(records, stage2Layer, completePath, stage3Opts);
    if (!stage3Opts.dryRun && existsSync(completePath)) {
      layerForDatapack = completePath;
    }
  }

  let datapackDir: string | undefined;
  if (options.writeDatapack) {
    if (!layerForDatapack || !existsSync(layerForDatapack)) {
      console.warn(`[datapack] no generated layer file available; skipping datapack packaging`);
    } else if (stage3Opts.dryRun) {
      console.warn(`[datapack] stage 3 dry-run requested; skipping datapack packaging`);
    } else {
      datapackDir = writeClassificationDatapack({
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
  return {
    packId,
    runtimeItemsPath,
    summaryPath,
    staticModsPath: options.modsPath ? resolve(options.modsPath) : undefined,
    staticMatchingRecords,
    staticEnrichedRecords,
    recordsPath,
    partialPath,
    completePath,
    layerForDatapack,
    datapackDir,
    records,
    summary,
  };
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
    const semanticText = mergeSemanticText(runtime.semantic_text, stat.semantic_text);
    return {
      ...runtime,
      display_name: nonBlank(runtime.display_name) ? runtime.display_name : stat.display_name,
      model_parents: runtime.model_parents.length > 0 ? runtime.model_parents : stat.model_parents,
      loot_table_sources: runtime.loot_table_sources.length > 0 ? runtime.loot_table_sources : stat.loot_table_sources,
      creative_tabs: runtime.creative_tabs.length > 0 ? runtime.creative_tabs : stat.creative_tabs,
      ...(semanticText ? { semantic_text: semanticText } : {}),
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

function mergeSemanticText(
  runtime: readonly SemanticTextEvidence[] | undefined,
  stat: readonly SemanticTextEvidence[] | undefined,
): SemanticTextEvidence[] | undefined {
  const merged: SemanticTextEvidence[] = [];
  const seen = new Set<string>();
  for (const entry of [...(runtime ?? []), ...(stat ?? [])]) {
    if (!entry?.text) continue;
    const text = entry.text.trim();
    if (!text) continue;
    const key = `${entry.source}\u0000${entry.key ?? ""}\u0000${text}`;
    if (seen.has(key)) continue;
    seen.add(key);
    merged.push({
      source: entry.source,
      text,
      ...(entry.key ? { key: entry.key } : {}),
    });
  }
  return merged.length > 0 ? merged : undefined;
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

function inferRuntimePackId(runtimeExportPath: string, summaryPath: string | undefined): string {
  const runtimeItemsPath = resolve(runtimeExportPath);
  const resolvedSummary = resolve(summaryPath ?? defaultRuntimeSummaryPath(runtimeItemsPath));
  if (existsSync(resolvedSummary)) {
    try {
      const summary = readRuntimeExportSummary(resolvedSummary);
      return summary.pack_id ?? summary.requested_pack_id ?? packIdFromRuntimeItemsPath(runtimeItemsPath);
    } catch {
      return packIdFromRuntimeItemsPath(runtimeItemsPath);
    }
  }
  return packIdFromRuntimeItemsPath(runtimeItemsPath);
}

function readLayerFile(path: string): LayerFile {
  return JSON.parse(readFileSync(path, "utf8")) as LayerFile;
}

function collectItemsWithoutLlmFacets(layer: LayerFile): string[] {
  const out: string[] = [];
  for (const [itemId, entry] of Object.entries(layer.entries)) {
    const hasLlm = Object.values(entry.facets ?? {}).some((facet) => {
      const source = (facet as { source?: unknown }).source;
      return typeof source === "string" && source.startsWith("llm:");
    });
    if (!hasLlm) out.push(itemId);
  }
  return out.sort();
}

function countLlmFacets(layer: LayerFile): number {
  let count = 0;
  for (const entry of Object.values(layer.entries)) {
    for (const facet of Object.values(entry.facets ?? {})) {
      const source = (facet as { source?: unknown }).source;
      if (typeof source === "string" && source.startsWith("llm:")) count++;
    }
  }
  return count;
}

function mergeLlmFacetsFromRepair(args: {
  fullPath: string;
  repairPath: string;
  itemIds: readonly string[];
}): { itemsTouched: number; facetsAdded: number; missingRepair: number } {
  const full = readLayerFile(args.fullPath);
  const repair = readLayerFile(args.repairPath);
  let itemsTouched = 0;
  let facetsAdded = 0;
  let missingRepair = 0;

  for (const itemId of args.itemIds) {
    const sourceEntry = repair.entries[itemId];
    if (!sourceEntry) {
      missingRepair++;
      continue;
    }
    const targetEntry = full.entries[itemId] ?? { facets: {} };
    const nextFacets = { ...(targetEntry.facets ?? {}) };
    let touched = false;
    for (const [facetId, entry] of Object.entries(sourceEntry.facets ?? {})) {
      const source = (entry as { source?: unknown }).source;
      if (typeof source !== "string" || !source.startsWith("llm:")) continue;
      const existing = nextFacets[facetId];
      const existingSource = (existing as { source?: unknown } | undefined)?.source;
      if (existing && (typeof existingSource !== "string" || !existingSource.startsWith("llm:"))) {
        continue;
      }
      nextFacets[facetId] = entry;
      facetsAdded++;
      touched = true;
    }
    full.entries[itemId] = { facets: nextFacets };
    if (touched) itemsTouched++;
  }

  full.generated_at = new Date().toISOString();
  writeFileSync(args.fullPath, JSON.stringify(full, null, 2) + "\n");
  return { itemsTouched, facetsAdded, missingRepair };
}

function datapackLayerPath(datapackDir: string, packId: string): string {
  return join(datapackDir, "data", "slot", "classification", "layers", `${safeFileComponent(packId)}.json`);
}

function refreshDatapackLayer(datapackDir: string, packId: string, sourceLayerPath: string): void {
  const target = datapackLayerPath(datapackDir, packId);
  if (!existsSync(target)) {
    mkdirSync(dirname(target), { recursive: true });
  }
  copyFileSync(sourceLayerPath, target);
}

function writeRuntimePackReports(args: {
  outDir: string;
  packId: string;
  elapsedSeconds: number;
  run: GeneratePackLayerRunResult;
  stage3Enabled: boolean;
  noLlmBeforeRepair: readonly string[];
  noLlmAfterRepair: readonly string[];
  repairedItems: number;
  repairedFacets: number;
  datapackZipPath?: string;
}): void {
  const finalLayerPath = args.stage3Enabled && existsSync(args.run.completePath)
    ? args.run.completePath
    : args.run.layerForDatapack;
  const finalLayer = finalLayerPath && existsSync(finalLayerPath)
    ? readLayerFile(finalLayerPath)
    : null;
  const report = {
    schema_version: 1,
    kind: "slot-runtime-pack-classification-report",
    pack_id: args.packId,
    generated_at: new Date().toISOString(),
    generated_by: TOOL_VERSION,
    elapsed_seconds: args.elapsedSeconds,
    input: {
      runtime_items: args.run.runtimeItemsPath,
      runtime_summary: existsSync(args.run.summaryPath) ? args.run.summaryPath : null,
      static_mods_path: args.run.staticModsPath ?? null,
      static_matching_item_records: args.run.staticMatchingRecords,
      static_enriched_runtime_records: args.run.staticEnrichedRecords,
      loader: args.run.summary?.loader ?? null,
      minecraft_version: args.run.summary?.minecraft_version ?? null,
      item_count: args.run.records.length,
    },
    output: {
      records: args.run.recordsPath,
      partial_layer: args.run.partialPath,
      complete_layer: finalLayerPath,
      datapack: args.run.datapackDir ?? null,
      datapack_zip: args.datapackZipPath ?? null,
    },
    coverage: finalLayer
      ? {
          entries: Object.keys(finalLayer.entries).length,
          llm_facets: countLlmFacets(finalLayer),
          items_without_llm_before_repair: args.noLlmBeforeRepair.length,
          items_without_llm_after_repair: args.noLlmAfterRepair.length,
          repaired_items: args.repairedItems,
          repaired_facets: args.repairedFacets,
        }
      : null,
    review: {
      corrections: countJsonArrayFile(args.run.completePath.replace(/\.complete\.json$/, ".corrections.json")),
      fill_ins: countJsonArrayFile(args.run.completePath.replace(/\.complete\.json$/, ".fill-ins.json")),
      schema_proposals: countJsonArrayFile(args.run.completePath.replace(/\.complete\.json$/, ".schema-proposals.json")),
      warnings: countJsonArrayFile(args.run.completePath.replace(/\.complete\.json$/, ".warnings.json")),
      response_mismatches: countJsonArrayFile(args.run.completePath.replace(/\.complete\.json$/, ".response-mismatches.json")),
    },
  };
  const jsonPath = join(args.outDir, `${args.packId}.run-report.json`);
  const mdPath = join(args.outDir, `${args.packId}.run-report.md`);
  writeFileSync(jsonPath, JSON.stringify(report, null, 2) + "\n");
  writeFileSync(mdPath, formatRuntimePackMarkdownReport(report));
  console.log(`[runtime-pack] report → ${jsonPath}`);
  console.log(`[runtime-pack] summary → ${mdPath}`);
}

function countJsonArrayFile(path: string): number {
  if (!existsSync(path)) return 0;
  try {
    const parsed = JSON.parse(readFileSync(path, "utf8"));
    return Array.isArray(parsed) ? parsed.length : 0;
  } catch {
    return 0;
  }
}

function formatRuntimePackMarkdownReport(report: Record<string, unknown>): string {
  const output = report.output as Record<string, unknown>;
  const coverage = report.coverage as Record<string, unknown> | null;
  const review = report.review as Record<string, unknown>;
  const lines = [
    `# SLOT Runtime Pack Classification Report`,
    ``,
    `Pack: \`${report.pack_id}\``,
    `Generated: \`${report.generated_at}\``,
    `Elapsed: \`${Number(report.elapsed_seconds).toFixed(1)}s\``,
    ``,
    `## Outputs`,
    ``,
    `- Complete layer: \`${output.complete_layer ?? "n/a"}\``,
    `- Datapack: \`${output.datapack ?? "n/a"}\``,
    `- Datapack zip: \`${output.datapack_zip ?? "n/a"}\``,
    ``,
    `## Coverage`,
    ``,
    coverage
      ? `- Entries: \`${coverage.entries}\`\n- LLM facets: \`${coverage.llm_facets}\`\n- Missing LLM before repair: \`${coverage.items_without_llm_before_repair}\`\n- Missing LLM after repair: \`${coverage.items_without_llm_after_repair}\`\n- Repaired items: \`${coverage.repaired_items}\``
      : `- Stage 3 did not produce a complete layer.`,
    ``,
    `## Review Queue`,
    ``,
    `- Corrections: \`${review.corrections}\``,
    `- Fill-ins: \`${review.fill_ins}\``,
    `- Schema proposals: \`${review.schema_proposals}\``,
    `- Warnings: \`${review.warnings}\``,
    `- Response mismatches: \`${review.response_mismatches}\``,
    ``,
  ];
  return lines.join("\n");
}

function writeZipFromDirectory(sourceDir: string, zipPath: string, options: { force: boolean }): void {
  if (existsSync(zipPath)) {
    if (!options.force) throw new Error(`zip output already exists: ${zipPath}`);
    rmSync(zipPath, { force: true });
  }
  const entries = listFilesRecursive(sourceDir)
    .map((file) => ({
      absolute: file,
      name: relative(sourceDir, file).split(/[\\/]+/).join("/"),
    }))
    .sort((a, b) => a.name.localeCompare(b.name));

  const locals: Buffer[] = [];
  const centrals: Buffer[] = [];
  let offset = 0;
  for (const entry of entries) {
    const nameBytes = Buffer.from(entry.name, "utf8");
    const data = readFileSync(entry.absolute);
    const compressed = deflateRawSync(data, { level: 9 });
    const crc = crc32(data);
    const local = Buffer.alloc(30);
    local.writeUInt32LE(0x04034b50, 0);
    local.writeUInt16LE(20, 4);
    local.writeUInt16LE(0, 6);
    local.writeUInt16LE(8, 8);
    local.writeUInt32LE(0, 10);
    local.writeUInt32LE(crc, 14);
    local.writeUInt32LE(compressed.length, 18);
    local.writeUInt32LE(data.length, 22);
    local.writeUInt16LE(nameBytes.length, 26);
    local.writeUInt16LE(0, 28);
    locals.push(local, nameBytes, compressed);

    const central = Buffer.alloc(46);
    central.writeUInt32LE(0x02014b50, 0);
    central.writeUInt16LE(20, 4);
    central.writeUInt16LE(20, 6);
    central.writeUInt16LE(0, 8);
    central.writeUInt16LE(8, 10);
    central.writeUInt32LE(0, 12);
    central.writeUInt32LE(crc, 16);
    central.writeUInt32LE(compressed.length, 20);
    central.writeUInt32LE(data.length, 24);
    central.writeUInt16LE(nameBytes.length, 28);
    central.writeUInt16LE(0, 30);
    central.writeUInt16LE(0, 32);
    central.writeUInt16LE(0, 34);
    central.writeUInt16LE(0, 36);
    central.writeUInt32LE(0, 38);
    central.writeUInt32LE(offset, 42);
    centrals.push(central, nameBytes);
    offset += local.length + nameBytes.length + compressed.length;
  }

  const localData = Buffer.concat(locals);
  const centralDirectory = Buffer.concat(centrals);
  const eocd = Buffer.alloc(22);
  eocd.writeUInt32LE(0x06054b50, 0);
  eocd.writeUInt16LE(0, 4);
  eocd.writeUInt16LE(0, 6);
  eocd.writeUInt16LE(entries.length, 8);
  eocd.writeUInt16LE(entries.length, 10);
  eocd.writeUInt32LE(centralDirectory.length, 12);
  eocd.writeUInt32LE(localData.length, 16);
  eocd.writeUInt16LE(0, 20);
  mkdirSync(dirname(zipPath), { recursive: true });
  writeFileSync(zipPath, Buffer.concat([localData, centralDirectory, eocd]));
}

function listFilesRecursive(dir: string): string[] {
  const out: string[] = [];
  for (const name of readdirSync(dir)) {
    const path = join(dir, name);
    const stat = statSync(path);
    if (stat.isDirectory()) {
      out.push(...listFilesRecursive(path));
    } else if (stat.isFile()) {
      out.push(path);
    }
  }
  return out;
}

let CRC_TABLE: Uint32Array | null = null;
function crc32(buffer: Buffer): number {
  const table = CRC_TABLE ??= buildCrcTable();
  let crc = 0xffffffff;
  for (const byte of buffer) {
    crc = (crc >>> 8) ^ table[(crc ^ byte) & 0xff]!;
  }
  return (crc ^ 0xffffffff) >>> 0;
}

function buildCrcTable(): Uint32Array {
  const table = new Uint32Array(256);
  for (let i = 0; i < 256; i++) {
    let c = i;
    for (let j = 0; j < 8; j++) {
      c = (c & 1) ? (0xedb88320 ^ (c >>> 1)) : (c >>> 1);
    }
    table[i] = c >>> 0;
  }
  return table;
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

function countBy(values: Iterable<string>): Record<string, number> {
  const out: Record<string, number> = {};
  for (const value of values) out[value] = (out[value] ?? 0) + 1;
  return out;
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
    await executeStage3(records, stage2Layer, completePath, stage3Opts);
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

function resolveStage3DocumentContext(
  opts: Stage3CliOptions,
  records: readonly ItemExtractRecord[],
): Stage3CliOptions {
  if (!opts.documentContextFile || opts.documentContextByItem) return opts;
  const resolvedPath = resolve(opts.documentContextFile);
  const evidence = readFacetEvidenceArtifactFile(resolvedPath);
  const { byItem, stats } = buildDocumentContextByItem(evidence, records);
  console.log(
    `[document-context] loaded ${resolvedPath}: ` +
      `${stats.items_with_context} item(s), ${stats.context_count} context link(s), ` +
      `${stats.eligible_document_records} eligible document record(s)`,
  );
  if (stats.skipped_quest_records > 0) {
    console.log(
      `[document-context] skipped ${stats.skipped_quest_records} quest record(s); ` +
        `quest SNBT is not local enough for item classification yet`,
    );
  }
  if (stats.skipped_broad_documents > 0) {
    console.log(`[document-context] skipped ${stats.skipped_broad_documents} broad document record(s)`);
  }
  return {
    ...opts,
    documentContextFile: resolvedPath,
    documentContextByItem: byItem,
    documentContextStats: stats,
  };
}

function resolveStage3FacetVocabulary(opts: Stage3CliOptions): Stage3CliOptions {
  if (!opts.facetVocabularyFile || opts.facetVocabulary) return opts;
  const resolvedPath = resolve(opts.facetVocabularyFile);
  const result = validateVocabularyArtifactFile(resolvedPath);
  if (!result.ok || !result.vocabulary) {
    console.error(`[facet-vocabulary] invalid vocabulary for stage 3: ${resolvedPath}`);
    for (const error of result.errors) console.error(`  ${error}`);
    process.exit(1);
    throw new Error(`invalid facet vocabulary: ${resolvedPath}`);
  }
  const accepted = Object.values(result.vocabulary.facets ?? {})
    .reduce((count, facet) =>
      count + Object.values(facet.values ?? {}).filter((value) => value.state === "accepted").length,
      0,
    );
  console.log(`[facet-vocabulary] loaded ${accepted} accepted value(s) for stage 3: ${resolvedPath}`);
  return {
    ...opts,
    facetVocabularyFile: resolvedPath,
    facetVocabulary: result.vocabulary,
  };
}

async function executeStage3(
  records: readonly ItemExtractRecord[],
  stage2Layer: LayerFile,
  completePath: string,
  opts: Stage3CliOptions,
) {
  opts = resolveStage3FacetVocabulary(opts);
  opts = resolveStage3DocumentContext(opts, records);
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

  ensureLiveBackendConfigured(opts, "stage 3");
  const client = buildClient(opts);

  const result = await runStage3({
    records,
    stage2Layer,
    client,
    model: opts.model,
    batchSize: opts.batchSize,
    concurrency: opts.concurrency,
    only,
    documentContextByItem: opts.documentContextByItem,
    facetVocabulary: opts.facetVocabulary,
    clientOptions: { signal: cliAbortSignal() },
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
        threshold: opts.retryThreshold,
        batchSize: opts.retryBatchSize,
        documentContextByItem: opts.documentContextByItem,
        facetVocabulary: opts.facetVocabulary,
        clientOptions: { signal: cliAbortSignal() },
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
      result.responseMismatches.push(...retryResult.responseMismatches);
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
  const validation = validateLayer(result.layer, { vocabulary: opts.facetVocabulary });
  if (!validation.ok) {
    console.error(`[stage3] layer failed schema validation`);
    for (const err of validation.errors.slice(0, 10)) console.error(`  ${err}`);
    process.exit(1);
  }
  writeFileSync(completePath, JSON.stringify(result.layer, null, 2) + "\n");

  // Persist proposals/corrections to dedicated files (only when non-empty,
  // so the report can list exactly what's on disk and what's worth opening).
  for (const suffix of [
    ".schema-proposals.json",
    ".corrections.json",
    ".fill-ins.json",
    ".response-mismatches.json",
    ".warnings.json",
  ]) {
    rmSync(completePath.replace(/\.complete\.json$/, suffix), { force: true });
  }
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
  if (result.responseMismatches.length) {
    const mismatchesPath = completePath.replace(/\.complete\.json$/, ".response-mismatches.json");
    writeFileSync(mismatchesPath, JSON.stringify(result.responseMismatches, null, 2) + "\n");
    writtenFiles.push({
      path: mismatchesPath,
      description: `${result.responseMismatches.length} batch response coverage mismatch(es) — rerun or repair missing item ids`,
    });
  }
  if (result.warnings.length) {
    const warningsPath = completePath.replace(/\.complete\.json$/, ".warnings.json");
    writeFileSync(warningsPath, JSON.stringify(result.warnings, null, 2) + "\n");
    writtenFiles.push({
      path: warningsPath,
      description: `${result.warnings.length} parser/merge warning(s) for audit`,
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
      path: completePath.replace(/\.complete\.json$/, ".warnings.json"),
    });
  }
  if (result.responseMismatches.length) {
    reviewItems.push({
      kind: "RESPONSE COVERAGE MISMATCHES",
      summary: `${result.responseMismatches.length} batch response(s) omitted or added item ids`,
      detail: result.responseMismatches.slice(0, 5).map((m) =>
        `batch ${m.batchIndex + 1}: missing=${m.missing.length}, extra=${m.extra.length}`,
      ),
      path: completePath.replace(/\.complete\.json$/, ".response-mismatches.json"),
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
      return buildItemPayload(record, stage2, opts.documentContextByItem?.[record.id]);
    });
    const prompt = buildBatchPrompt({
      items: payloads,
      target_facets: targetFacets,
      facet_vocabulary: opts.facetVocabulary
        ? buildPromptFacetVocabulary(opts.facetVocabulary, targetFacets)
        : undefined,
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
  return new OpenRouterClient({
    ignoredProviders: opts.ignoredProviders,
    // Auto-pin to the official deepseek upstream when the model
    // is a deepseek/* slug and the caller didn't override. This
    // matches the "lock in v4-flash via deepseek" production
    // recipe without requiring every script to repeat the flag.
    onlyProviders: opts.onlyProviders ?? inferOnlyProviders(opts.model),
  });
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
        --mod-concurrency <n>  Process N mods in parallel (default 1).
                               Each mod runs its own batch worker
                               pool, so total in-flight LLM calls
                               ≈ mod-concurrency × concurrency.
                               OpenRouter handles dozens comfortably;
                               recommend 3-4 for fast wall-time.

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
        --evidence <path>       Optional <pack>.facet-evidence.json. Stage 3
                                converts low-breadth guide/advancement records
                                into per-item document_context.
        --facet-vocabulary <path>
                                Accepted pack facet vocabulary to ground
                                vocabulary-backed Stage 3 values.
        --pack-id <id>          Override output/layer/datapack id.
        --datapack              Package the layer as a datapack folder.
        --datapack-out <path>   Explicit datapack output folder.
        --pack-format <n>       Datapack pack_format (default inferred from
                                runtime MC version; 1.20.x -> 15).
        --force                 Overwrite existing pack-layer/datapack outputs.

  collect-pack-facet-evidence --runtime-export <pack.runtime-items.ndjson> [options]
      Collect the pack-level evidence used by the facet vocabulary pass.
      Writes <out>/<pack>.facet-evidence.json with runtime item facts,
      recipe-type summaries, recipe-role summaries, recipe-id families,
      item/block tags, optional mod metadata, guide pages, quest nodes,
      advancements, Ponder/category lang text, KubeJS tooltip mappings, and
      stack groups. Missing optional sources are reported as diagnostics and
      do not fail the command.

      Facet-evidence flags:
        --summary <path>        Explicit runtime-summary.json path.
        --mods <path>           Prism instance root or mods/ folder for static
                                jar, guide, quest, advancement, and mod metadata
                                evidence.
        --pack-id <id>          Override output pack id.
        --out <dir>             Output directory (default out).
        --force                 Overwrite an existing facet-evidence file.

  propose-pack-facet-vocabulary --evidence <pack.facet-evidence.json> [options]
      Propose a pack-specific vocabulary for vocabulary-backed semantic facets.
      Reads <pack>.facet-evidence.json, builds deterministic candidates,
      optionally asks the configured LLM to curate them, and writes
      <out>/<pack>.facet-vocabulary.json plus
      <out>/<pack>.facet-vocabulary.review.json. Use --dry-run to write prompt
      pairs without spending tokens.

      Facet-vocabulary flags:
        --facet <id>            Regenerate one facet vocabulary. Repeatable.
        --namespace <id>        Limit evidence to one namespace. Repeatable.
        --previous-vocabulary <path>
                                Refinement-only carry-forward for an already
                                nearly satisfactory vocabulary. Previous values
                                are sticky candidates; omit this for clean
                                baseline validation.
        --min-evidence <n>      Minimum deterministic support for acceptance
                                (default 2; previous/universal values bypass).
        --max-candidates-per-facet <n>
                                Bound prompt size per facet (default 256).
        --dry-run               Write prompt pairs only.
        --force                 Overwrite vocabulary/review outputs.

  classify-runtime-pack --runtime-export <pack.runtime-items.ndjson> [options]
      Recommended one-command workflow for a real modpack runtime export.
      Combines runtime records, optional static jar enrichment, stage 3 semantic
      completion, missing-LLM repair, validation, datapack packaging, datapack
      zip creation, and a machine/human run report. Defaults are tuned for the
      cheap OpenRouter deepseek/deepseek-v4-flash path and record replay
      fixtures.

      Runtime-pack flags:
        --summary <path>        Explicit runtime-summary.json path.
        --mods <path>           Prism instance root or mods/ folder for static
                                jar enrichment.
        --evidence <path>       Optional <pack>.facet-evidence.json. Stage 3
                                converts low-breadth guide/advancement records
                                into per-item document_context.
        --facet-vocabulary <path>
                                Accepted pack facet vocabulary to ground
                                vocabulary-backed Stage 3 values.
        --pack-id <id>          Override output/layer/datapack id.
        --out <dir>             Output directory (default out/<pack-id>).
        --stages <list>         Default 1,2,3.
        --no-datapack           Skip datapack folder output.
        --datapack-out <path>   Explicit datapack output folder.
        --pack-format <n>       Datapack pack_format.
        --no-zip                Skip datapack zip output.
        --force                 Overwrite existing generated outputs.
        --no-repair             Skip the missing-LLM repair pass.
        --repair-batch-size <n> Items per repair batch (default 25).
        --repair-concurrency <n>
                                Parallel repair batches (default 8).

  validate <layer.json> [--vocabulary <facet-vocabulary.json>]
      Validate a layer file against layer.schema.json plus the live facet
      registry. With --vocabulary, vocabulary-backed facet values must be
      accepted by that pack vocabulary artifact.

  validate-vocabulary <facet-vocabulary.json>
      Validate a pack facet vocabulary artifact: schema marker, pack id,
      vocabulary-backed facet ids, value-id grammar, lifecycle state/origin,
      and workflow-role parent links.

Scan options:
  --mods <path>             Required. May point directly at mods/, at a
                            minecraft/ folder containing mods/, or at a
                            Prism instance root containing minecraft/mods/.
  --out <dir>               Output directory for scan-report.json (default out).
  --json <path>             Explicit JSON report path.

Stage selection:
  --stages 1,2[,3]          Which stages to run. Default: 1,2 (stage 3 opt-in).

Stage 3 (LLM) knobs — only used when 3 is in --stages:
  --only-provider <slug>    Pin routing to a single OpenRouter upstream
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
                            OpenRouter model slug (e.g.
                            'deepseek/deepseek-v4-flash',
                            'deepseek/deepseek-v4-pro', 'openai/gpt-4o-mini').
  --batch-size <n>          Items per LLM call (default 20).
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
  --facet-vocabulary <path> Accepted pack facet vocabulary. Vocabulary-backed
                            facets in Stage 3 are prompted to use only accepted
                            ids from this file and final validation enforces it.

Retry pass (opt-in; runs after the first pass on low-confidence items):
  --retry-model <id>        OpenRouter retry model. Enabling this turns on retry.
  --retry-threshold <n>     Retry items with any LLM facet confidence < n or ambiguous:true. Default 0.5.
  --retry-batch-size <n>    Items per retry LLM call. Default 8.
  --retry-fixture-dir <p>   Separate fixture directory for the retry pass.

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
      --model deepseek/deepseek-v4-flash --record-replay \\
      --fixture-dir test/fixtures/stage3-canary-deepseek \\
      --retry-model openai/gpt-4.1-mini --retry-threshold 0.6 \\
      --retry-fixture-dir test/fixtures/stage3-canary-retry

  # Classify every mod in a modpack manifest (idempotent — re-run to resume):
  OPENROUTER_API_KEY=sk-or-... \\
    bun run src/cli.ts classify-modpack modpacks/test-modset.json --out out

  # Classify missing mods from an installed Prism instance or mods folder:
  bun run src/cli.ts classify-folder --mods /path/to/prism/instance --out out --stages 1,2
  OPENROUTER_API_KEY=sk-or-... \\
    bun run src/cli.ts classify-folder --mods /path/to/prism/instance --mod createaddition \\
      --out out --stages 1,2,3 --record-replay --fixture-dir test/fixtures/createaddition-jar

  # Generate a static+runtime pack layer and package it as a datapack:
  bun run src/cli.ts generate-pack-layer \\
      --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \\
      --summary modpacks/exports/tfg2.runtime-summary.json \\
      --mods /path/to/TerraFirmaGreg-Modern \\
      --facet-vocabulary out/tfg2/tfg2.facet-vocabulary.json \\
      --stages 1,2,3 --datapack

  # Collect pack-level facet evidence before proposing a vocabulary:
  bun run src/cli.ts collect-pack-facet-evidence \\
      --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \\
      --summary modpacks/exports/tfg2.runtime-summary.json \\
      --mods /path/to/TerraFirmaGreg-Modern \\
      --out out/tfg2

  # Propose pack facet vocabulary from collected evidence:
  bun run src/cli.ts propose-pack-facet-vocabulary \\
      --evidence out/tfg2/tfg2.facet-evidence.json \\
      --out out/tfg2 \\
      --record-replay --fixture-dir test/fixtures/tfg2-facet-vocabulary

  # Recommended one-command pack workflow: uses repo-root .env for
  # OPENROUTER_API_KEY, records fixtures, repairs missing stage-3 coverage,
  # writes a datapack folder + zip, and emits run-report.{json,md}.
  bun run src/cli.ts classify-runtime-pack \\
      --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \\
      --summary modpacks/exports/tfg2.runtime-summary.json \\
      --mods /path/to/TerraFirmaGreg-Modern \\
      --out out/tfg2 \\
      --force

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
  bun run reclassify:test-modset:full  # force full stage outputs

Prompt-evaluation presets (60-item playtest sample; reads stage-1/2 from out/):
  OPENROUTER_API_KEY=... bun run eval:deepseek
                                       # OpenRouter + deepseek/deepseek-v4-flash
  scripts/eval-prompt.sh --model openai/gpt-4o-mini
                                       # alternate OpenRouter model
`);
}

main().catch((err) => {
  if (shutdownSignal) {
    console.error(`[slot-classify] stopped after ${shutdownSignal}`);
    process.exit(shutdownSignal === "SIGINT" ? 130 : 143);
  }
  console.error(err);
  process.exit(1);
});
