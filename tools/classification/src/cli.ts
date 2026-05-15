import { copyFileSync, existsSync, mkdirSync, readFileSync, readdirSync, rmSync, statSync, writeFileSync } from "node:fs";
import { basename, dirname, join, relative, resolve } from "node:path";
import { createInterface } from "node:readline/promises";
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
  FACETS,
  validateMultiValue,
} from "./schema/facets.ts";
import {
  isUsableVocabularyState,
  validateVocabularyArtifact,
  validateVocabularyArtifactFile,
  type PackFacetVocabulary,
  type VocabularyEvidenceRef,
  type VocabularyValue,
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
  buildItemPayload,
  buildPromptFacetVocabulary,
  buildSplitPrompt,
  defaultTargetFacets,
  PROMPT_VERSION,
} from "./llm/prompt.ts";
import type { VocabularyProposal } from "./llm/parse.ts";
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
  applyVocabularyReviewDecisions,
  proposePackFacetVocabulary,
  readFacetEvidenceArtifactFile,
  readPackFacetVocabularyReviewFile,
  type PackFacetVocabularyReview,
  type VocabularyReviewDecision,
  type VocabularyItemSampleMode,
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
const DEFAULT_STAGE3_BATCH_SIZE = 1000;
const DEFAULT_STAGE3_CONCURRENCY = 1;
// Runtime-pack classification is bounded by both the 1M input window and the
// 131k output cap. OpenRouter logs from the TFG canary topped out around 17k
// completion tokens, while a 1000-item prompt is still comfortably below the
// input cap; keep concurrency low so huge cached prompts do not stampede.
const DEFAULT_RUNTIME_PACK_BATCH_SIZE = 1000;
const DEFAULT_RUNTIME_PACK_CONCURRENCY = 1;
const DEFAULT_RUNTIME_PACK_REPAIR_BATCH_SIZE = 500;
const DEFAULT_RUNTIME_PACK_REPAIR_CONCURRENCY = 1;
const DEFAULT_VOCABULARY_ITEM_SAMPLE_SIZE = 1536;
const DEFAULT_VOCABULARY_REFINEMENT_ROUNDS = 3;
const ORGANIZATION_GROUP_HOME_REVIEW_THRESHOLD = 20;
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

function parseNonNegativeInteger(input: string | undefined, optionName: string): number | undefined {
  if (input === undefined) return undefined;
  if (!/^(0|[1-9]\d*)$/.test(input)) {
    throw new Error(`${optionName} must be a non-negative integer, got '${input}'`);
  }
  const value = Number(input);
  if (!Number.isSafeInteger(value)) {
    throw new Error(`${optionName} is too large: '${input}'`);
  }
  return value;
}

function parseVocabularyItemSampleMode(
  input: string | undefined,
  optionName: string,
): VocabularyItemSampleMode | undefined {
  if (input === undefined) return undefined;
  if (input === "random" || input === "coverage") return input;
  throw new Error(`${optionName} must be 'random' or 'coverage', got '${input}'`);
}

function parseStages(input: string | undefined): StageSelection {
  if (!input) return { stage1: true, stage2: false, stage3: true };
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
          "mcmeta-ref": { type: "string" },
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
          // stage 3 retry knobs — applied after the first pass on items whose
          // LLM facets are explicitly ambiguous.
          "retry-model": { type: "string" },
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
        console.error("usage: classify --mod <id> --source <path> [--out <dir>] [--stages 1,3]");
        process.exit(2);
      }
      const outDir = resolve(args.values.out ?? "out");
      mkdirSync(outDir, { recursive: true });

      const stages = parseStages(args.values.stages ?? (cmd === "extract" ? "1,2" : "1,3"));

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
        await runVanilla(sourcePath!, outDir, stages, stage3CliOpts, args.values["mcmeta-ref"]);
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

      const stages = parseStages(args.values.stages ?? "1,3");

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
      console.log("[scan] use classify-folder --mods <path> --stages 1,3 to generate jar-backed LLM classification outputs.");
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
        console.error("usage: classify-folder --mods <mods-folder-or-instance-root> [--out <dir>] [--stages 1,3]");
        process.exit(2);
        return;
      }
      const outDir = resolve(args.values.out ?? "out");
      mkdirSync(outDir, { recursive: true });

      const stages = parseStages(args.values.stages ?? "1,3");
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
          "static-items": { type: "string", multiple: true },
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
          sample: { type: "string" },
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
      const stages = parseStages(args.values.stages ?? "1,3");
      const useReplay = args.values["use-replay"] ?? false;
      const stage3FixtureDir = args.values["fixture-dir"] ?? join(outDir, "fixtures", "stage3");
      const stage3CliOpts: Stage3CliOptions = {
        model: args.values.model ?? DEFAULT_STAGE3_MODEL,
        ignoredProviders: (args.values["ignore-provider"] as string[] | undefined) ?? undefined,
        onlyProviders: (args.values["only-provider"] as string[] | undefined) ?? undefined,
        batchSize: parsePositiveInteger(args.values["batch-size"], "--batch-size") ?? DEFAULT_RUNTIME_PACK_BATCH_SIZE,
        concurrency: parsePositiveInteger(args.values.concurrency, "--concurrency") ?? DEFAULT_RUNTIME_PACK_CONCURRENCY,
        sample: args.values.sample,
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
        staticItemsPaths: (args.values["static-items"] as string[] | undefined) ?? [],
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
        repairBatchSize: parsePositiveInteger(args.values["repair-batch-size"], "--repair-batch-size") ?? DEFAULT_RUNTIME_PACK_REPAIR_BATCH_SIZE,
        repairConcurrency: parsePositiveInteger(args.values["repair-concurrency"], "--repair-concurrency") ?? DEFAULT_RUNTIME_PACK_REPAIR_CONCURRENCY,
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
          "static-items": { type: "string", multiple: true },
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
      const stages = parseStages(args.values.stages ?? "1,3");
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
        staticItemsPaths: (args.values["static-items"] as string[] | undefined) ?? [],
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
          "static-items": { type: "string", multiple: true },
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
        staticItemsPaths: (args.values["static-items"] as string[] | undefined) ?? [],
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
          "base-vocabulary": { type: "string", multiple: true },
          "previous-vocabulary": { type: "string" },
          facet: { type: "string", multiple: true },
          namespace: { type: "string", multiple: true },
          "min-evidence": { type: "string" },
          "max-candidates-per-facet": { type: "string" },
          "max-candidates-per-prompt": { type: "string" },
          "item-sample-size": { type: "string" },
          "item-sample-mode": { type: "string" },
          "item-sample-seed": { type: "string" },
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
        baseVocabularyPaths: (args.values["base-vocabulary"] as string[] | undefined) ?? [],
        previousVocabularyPath: args.values["previous-vocabulary"],
        facets: (args.values.facet as string[] | undefined) ?? [],
        namespaces: (args.values.namespace as string[] | undefined) ?? [],
        minEvidence: parsePositiveInteger(args.values["min-evidence"], "--min-evidence") ?? 2,
        maxCandidatesPerFacet: parsePositiveInteger(args.values["max-candidates-per-facet"], "--max-candidates-per-facet"),
        maxCandidatesPerPrompt: parsePositiveInteger(args.values["max-candidates-per-prompt"], "--max-candidates-per-prompt"),
        itemSampleSize: parseNonNegativeInteger(args.values["item-sample-size"], "--item-sample-size") ?? DEFAULT_VOCABULARY_ITEM_SAMPLE_SIZE,
        itemSampleMode: parseVocabularyItemSampleMode(args.values["item-sample-mode"], "--item-sample-mode") ?? "coverage",
        itemSampleSeed: args.values["item-sample-seed"],
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

    case "refine-pack-facet-vocabulary": {
      const args = parseArgs({
        args: rest,
        options: {
          evidence: { type: "string" },
          out: { type: "string" },
          "pack-id": { type: "string" },
          "base-vocabulary": { type: "string", multiple: true },
          "previous-vocabulary": { type: "string" },
          facet: { type: "string", multiple: true },
          namespace: { type: "string", multiple: true },
          rounds: { type: "string" },
          "min-evidence": { type: "string" },
          "max-candidates-per-facet": { type: "string" },
          "max-candidates-per-prompt": { type: "string" },
          "item-sample-size": { type: "string" },
          "item-sample-mode": { type: "string" },
          "item-sample-seed": { type: "string" },
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
        console.error("usage: refine-pack-facet-vocabulary --evidence <pack.facet-evidence.json> [options]");
        process.exit(2);
        return;
      }
      const outDir = resolve(args.values.out ?? "out");
      mkdirSync(outDir, { recursive: true });
      await runRefinePackFacetVocabulary({
        evidencePath,
        outDir,
        packId: args.values["pack-id"],
        baseVocabularyPaths: (args.values["base-vocabulary"] as string[] | undefined) ?? [],
        previousVocabularyPath: args.values["previous-vocabulary"],
        facets: (args.values.facet as string[] | undefined) ?? [],
        namespaces: (args.values.namespace as string[] | undefined) ?? [],
        rounds: parsePositiveInteger(args.values.rounds, "--rounds") ?? DEFAULT_VOCABULARY_REFINEMENT_ROUNDS,
        minEvidence: parsePositiveInteger(args.values["min-evidence"], "--min-evidence") ?? 2,
        maxCandidatesPerFacet: parsePositiveInteger(args.values["max-candidates-per-facet"], "--max-candidates-per-facet"),
        maxCandidatesPerPrompt: parsePositiveInteger(args.values["max-candidates-per-prompt"], "--max-candidates-per-prompt"),
        itemSampleSize: parseNonNegativeInteger(args.values["item-sample-size"], "--item-sample-size") ?? DEFAULT_VOCABULARY_ITEM_SAMPLE_SIZE,
        itemSampleMode: parseVocabularyItemSampleMode(args.values["item-sample-mode"], "--item-sample-mode") ?? "coverage",
        itemSampleSeed: args.values["item-sample-seed"],
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

    case "apply-pack-facet-vocabulary-review": {
      const args = parseArgs({
        args: rest,
        options: {
          vocabulary: { type: "string" },
          review: { type: "string" },
          out: { type: "string" },
          force: { type: "boolean" },
        },
        allowPositionals: false,
        strict: true,
      });
      const vocabularyPath = args.values.vocabulary;
      const reviewPath = args.values.review;
      const outPath = args.values.out;
      if (!vocabularyPath || !reviewPath || !outPath) {
        console.error("usage: apply-pack-facet-vocabulary-review --vocabulary <pack.facet-vocabulary.json> --review <pack.facet-vocabulary.review.json> --out <approved.facet-vocabulary.json>");
        process.exit(2);
        return;
      }
      runApplyPackFacetVocabularyReview({
        vocabularyPath,
        reviewPath,
        outPath,
        force: args.values.force ?? false,
      });
      return;
    }

    case "review-pack-facet-vocabulary": {
      const args = parseArgs({
        args: rest,
        options: {
          vocabulary: { type: "string" },
          review: { type: "string" },
          out: { type: "string" },
          "review-out": { type: "string" },
          facet: { type: "string", multiple: true },
          all: { type: "boolean" },
          force: { type: "boolean" },
        },
        allowPositionals: false,
        strict: true,
      });
      const vocabularyPath = args.values.vocabulary;
      const reviewPath = args.values.review;
      const outPath = args.values.out;
      if (!vocabularyPath || !reviewPath || !outPath) {
        console.error("usage: review-pack-facet-vocabulary --vocabulary <pack.facet-vocabulary.json> --review <pack.facet-vocabulary.review.json> --out <approved.facet-vocabulary.json> [--review-out <reviewed.json>]");
        process.exit(2);
        return;
      }
      await runInteractivePackFacetVocabularyReview({
        vocabularyPath,
        reviewPath,
        outPath,
        reviewOutPath: args.values["review-out"],
        facets: (args.values.facet as string[] | undefined) ?? [],
        includeAccepted: args.values.all ?? false,
        force: args.values.force ?? false,
      });
      return;
    }

    case "review-stage3-vocabulary-proposals": {
      const args = parseArgs({
        args: rest,
        options: {
          vocabulary: { type: "string" },
          proposals: { type: "string" },
          out: { type: "string" },
          "review-out": { type: "string" },
          facet: { type: "string", multiple: true },
          force: { type: "boolean" },
        },
        allowPositionals: false,
        strict: true,
      });
      const vocabularyPath = args.values.vocabulary;
      const proposalsPath = args.values.proposals;
      const outPath = args.values.out;
      if (!vocabularyPath || !proposalsPath || !outPath) {
        console.error("usage: review-stage3-vocabulary-proposals --vocabulary <approved.facet-vocabulary.json> --proposals <pack.facets.vocabulary-proposals.json> --out <updated.facet-vocabulary.json> [--review-out <reviewed.json>]");
        process.exit(2);
        return;
      }
      await runInteractiveStage3VocabularyProposalReview({
        vocabularyPath,
        proposalsPath,
        outPath,
        reviewOutPath: args.values["review-out"],
        facets: (args.values.facet as string[] | undefined) ?? [],
        force: args.values.force ?? false,
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
  retryBatchSize?: number;
  retryFixtureDir?: string;
  /** Override the retry pass's record/replay mode. Defaults to recording
   *  when --retry-fixture-dir is set, since we usually don't have retry
   *  fixtures pre-populated. */
  retryUseReplay?: boolean;
  retryRecordReplay?: boolean;
  /** Usable pack facet vocabulary for vocabulary-backed semantic facets. */
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
  mcmetaRef?: string,
) {
  const start = Date.now();
  // The mcmeta summary bundle is only needed for stage 1 (extract) and
  // stage 2 (exact/reference facts over the live tag closure). When the
  // caller is running stage 3 alone — typical for prompt experimentation
  // against an already-extracted dataset — there's no point cloning a
  // ~1GB worktree just to read a version string. Skip the bundle load
  // and let `records` come from the ndjson on disk.
  const needsBundle = stages.stage1 || stages.stage2;
  let bundle: ReturnType<typeof loadSummaryBundle> | null = null;
  if (needsBundle) {
    console.log(`[vanilla] loading summary bundle from ${sourcePath}${mcmetaRef ? ` ref=${mcmetaRef}` : ""}`);
    const source = ensureVanillaSource(sourcePath, { ref: mcmetaRef });
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
  }

  if (stages.stage3) {
    const stage3BaseLayer = createLlmStage3BaseLayer({
      layerKind: "vanilla-base",
      source: VANILLA_NAMESPACE,
      sourceKind: "mcmeta-summary",
      sourcePath,
      sourceVersion: bundle?.version,
      namespace: VANILLA_NAMESPACE,
    });
    if (stage2Layer) {
      console.log(`[stage3] using LLM-only base layer; stage 2 facets are diagnostic output only`);
    }
    await executeStage3(records, stage3BaseLayer, completePath, stage3Opts, ["stage1", "stage3"]);
  }

  console.log(`done in ${((Date.now() - start) / 1000).toFixed(2)}s`);
}

/**
 * Run the pipeline against a mod source tree (createaddition, mekanism, …).
 * Mirrors `runVanilla` but uses the mod-source bundle loader and writes
 * outputs under `<modid>.*` filenames so multiple mods can coexist in
 * the same `out/` directory.
 *
 * When requested explicitly, stage 2 still writes a diagnostic/reference layer.
 * Stage 3 does not use that layer as authority; it starts from an empty base so
 * the LLM owns semantic facet decisions.
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
  }

  if (stages.stage3) {
    const stage3BaseLayer = createLlmStage3BaseLayer({
      layerKind: "per-mod",
      source: modNamespace,
      sourceKind: "source-tree",
      sourcePath: modPath,
      sourceVersion: bundle?.version,
      namespace: modNamespace,
    });
    if (stage2Layer) {
      console.log(`[stage3] using LLM-only base layer; stage 2 facets are diagnostic output only`);
    }
    await executeStage3(records, stage3BaseLayer, completePath, stage3Opts, ["stage1", "stage3"]);
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
  const batchConcurrency = stage3Opts.concurrency ?? DEFAULT_STAGE3_CONCURRENCY;
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
  staticItemsPaths: readonly string[];
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
      staticItemsPaths: options.staticItemsPaths,
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
    const expectedItemIds = run.records.map((record) => record.id);
    noLlmBeforeRepair = collectItemsWithoutLlmFacets(readLayerFile(run.completePath), expectedItemIds);
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
      if (!run.stage3BaseLayer) {
        throw new Error("stage 3 repair requested without a stage 3 base layer");
      }
      await executeStage3(run.records, run.stage3BaseLayer, repairPath, repairOpts, ["stage1", "stage3"]);
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

    noLlmAfterRepair = collectItemsWithoutLlmFacets(readLayerFile(run.completePath), expectedItemIds);
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
  staticItemsPaths: readonly string[];
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
  staticItemsPaths?: string[];
  staticMatchingRecords: number;
  staticEnrichedRecords: number;
  recordsPath: string;
  partialPath: string;
  completePath: string;
  layerForDatapack: string | null;
  datapackDir?: string;
  records: ItemExtractRecord[];
  summary: RuntimeExportSummary | null;
  stage3BaseLayer: LayerFile | null;
}

interface CollectPackFacetEvidenceOptions {
  summaryPath?: string;
  modsPath?: string;
  staticItemsPaths: readonly string[];
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
  baseVocabularyPaths: readonly string[];
  previousVocabularyPath?: string;
  facets: readonly string[];
  namespaces: readonly string[];
  minEvidence: number;
  maxCandidatesPerFacet?: number;
  maxCandidatesPerPrompt?: number;
  itemSampleSize: number;
  itemSampleMode: VocabularyItemSampleMode;
  itemSampleSeed?: string;
  force: boolean;
  opts: Stage3CliOptions;
}

interface RefinePackFacetVocabularyCliOptions extends ProposePackFacetVocabularyCliOptions {
  rounds: number;
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

  const staticItemsPaths = options.staticItemsPaths.map((path) => resolve(path));
  if (staticItemsPaths.length > 0) {
    const staticRecords = loadStaticItemEnrichmentRecords(staticItemsPaths, records);
    const merged = mergeRuntimeWithStaticRecords(records, staticRecords, "static-items");
    records = merged.records;
    console.log(
      `[facet-evidence] static item enrichment: ${staticRecords.size} matching item record(s), ` +
        `${merged.enriched} runtime record(s) enriched`,
    );
  }

  if (options.modsPath) {
    const staticRecords = loadStaticEnrichmentRecords(options.modsPath, records);
    const merged = mergeRuntimeWithStaticRecords(records, staticRecords, "jar");
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
    staticItemsPaths,
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

  const vocabularyInput = loadVocabularyInputArtifacts({
    packId,
    baseVocabularyPaths: options.baseVocabularyPaths,
    previousVocabularyPath: options.previousVocabularyPath,
    context: "facet-vocabulary",
  });
  const previousVocabulary = vocabularyInput.vocabulary;
  const previousVocabularyPath = vocabularyInput.sourceLabel;

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
    maxCandidatesPerPrompt: options.maxCandidatesPerPrompt,
    itemSampleSize: options.itemSampleSize,
    itemSampleMode: options.itemSampleMode,
    itemSampleSeed: options.itemSampleSeed ?? `${packId}:vocabulary:single-pass`,
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
    const summary: Array<{
      facet: string;
      system: string;
      user: string;
      context_records: number;
      context_records_without_semantic_context: number;
      semantic_context_entries: number;
      runtime_item_sample: number;
      chars: number;
      approxTokens: number;
    }> = [];
    for (const [facet, prompt] of Object.entries(result.prompts).sort(([a], [b]) => a.localeCompare(b))) {
      const systemPath = join(dryRunDir, `${facet}.system.md`);
      const userPath = join(dryRunDir, `${facet}.user.json`);
      writeFileSync(systemPath, prompt.system);
      writeFileSync(userPath, prompt.user);
      const parsedUser = JSON.parse(prompt.user) as {
        context_records?: unknown[];
        candidates?: unknown[];
        pack_item_overview?: unknown;
      };
      const promptContextRecords = Array.isArray(parsedUser.context_records)
        ? parsedUser.context_records
        : Array.isArray(parsedUser.candidates)
          ? parsedUser.candidates
          : [];
      const runtimeItemSample = isRecord(parsedUser.pack_item_overview) && Array.isArray(parsedUser.pack_item_overview.runtime_item_sample)
        ? parsedUser.pack_item_overview.runtime_item_sample.length
        : 0;
      const chars = prompt.system.length + prompt.user.length;
      summary.push({
        facet,
        system: systemPath,
        user: userPath,
        context_records: promptContextRecords.length,
        context_records_without_semantic_context: countContextRecordsWithoutSemanticContext(promptContextRecords),
        semantic_context_entries: countSemanticContextEntries(promptContextRecords),
        runtime_item_sample: runtimeItemSample,
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

  const usableCounts: Array<[string, number]> =
    Object.entries(result.vocabulary.facets)
      .map(([facet, value]): [string, number] => [facet, Object.keys(value.values).length])
      .sort(([a], [b]) => a.localeCompare(b));
  for (const [facet, count] of usableCounts) {
    console.log(`  ${facet.padEnd(24)} ${String(count).padStart(4)} usable`);
  }
  printVocabularyHomeImpactAudit(result.vocabulary);
  const reviewCount = Object.values(result.review.decisions).flat()
    .filter((decision) => decision.state !== "accepted").length;
  console.log(`[facet-vocabulary] review/rejected decision(s): ${reviewCount}`);
  console.log(`done in ${((Date.now() - start) / 1000).toFixed(2)}s`);
}

function loadVocabularyInputArtifacts(args: {
  packId: string;
  baseVocabularyPaths: readonly string[];
  previousVocabularyPath?: string;
  context: string;
}): { vocabulary?: PackFacetVocabulary; sourceLabel?: string } {
  const inputs: Array<{ role: "base" | "previous"; path: string; vocabulary: PackFacetVocabulary }> = [];
  for (const rawPath of args.baseVocabularyPaths) {
    const path = resolve(rawPath);
    const result = validateVocabularyArtifactFile(path);
    if (!result.ok || !result.vocabulary) {
      console.error(`[${args.context}] invalid base vocabulary: ${path}`);
      for (const error of result.errors) console.error(`  ${error}`);
      process.exit(1);
      throw new Error(`invalid base vocabulary: ${path}`);
    }
    inputs.push({ role: "base", path, vocabulary: result.vocabulary });
  }
  if (args.previousVocabularyPath) {
    const path = resolve(args.previousVocabularyPath);
    const result = validateVocabularyArtifactFile(path);
    if (!result.ok || !result.vocabulary) {
      console.error(`[${args.context}] invalid previous vocabulary: ${path}`);
      for (const error of result.errors) console.error(`  ${error}`);
      process.exit(1);
      throw new Error(`invalid previous vocabulary: ${path}`);
    }
    inputs.push({ role: "previous", path, vocabulary: result.vocabulary });
  }
  if (inputs.length === 0) return {};
  const vocabulary = mergeVocabularyInputs(args.packId, inputs);
  const sourceLabel = inputs.map((input) => `${input.role}:${input.path}`).join(",");
  return { vocabulary, sourceLabel };
}

function mergeVocabularyInputs(
  packId: string,
  inputs: ReadonlyArray<{ role: "base" | "previous"; path: string; vocabulary: PackFacetVocabulary }>,
): PackFacetVocabulary {
  const merged: PackFacetVocabulary = {
    schema_version: 1,
    kind: "slot-pack-facet-vocabulary",
    pack_id: packId,
    generated_by: TOOL_VERSION,
    generated_at: new Date().toISOString(),
    source: {
      vocabulary_inputs: inputs.map((input) => ({
        role: input.role,
        path: input.path,
        pack_id: input.vocabulary.pack_id,
      })),
    },
    facets: {},
  };
  for (const input of inputs) {
    for (const [facet, facetValues] of Object.entries(input.vocabulary.facets ?? {})) {
      merged.facets[facet] ??= { values: {} };
      for (const [id, value] of Object.entries(facetValues.values ?? {})) {
        merged.facets[facet]!.values[id] = cloneJson(value);
      }
    }
  }
  const validation = validateVocabularyArtifact(merged);
  if (!validation.ok) {
    throw new Error(`merged vocabulary input failed validation: ${validation.errors.slice(0, 5).join("; ")}`);
  }
  return merged;
}

async function runRefinePackFacetVocabulary(
  options: RefinePackFacetVocabularyCliOptions,
): Promise<void> {
  const start = Date.now();
  const evidencePath = resolve(options.evidencePath);
  const evidence = readFacetEvidenceArtifactFile(evidencePath);
  const packId = safeFileComponent(options.packId ?? evidence.pack_id);
  const vocabularyPath = join(options.outDir, `${packId}.facet-vocabulary.json`);
  const reviewPath = join(options.outDir, `${packId}.facet-vocabulary.review.json`);
  const workingPath = join(options.outDir, `${packId}.facet-vocabulary.working.json`);
  const loopPath = join(options.outDir, `${packId}.facet-vocabulary.loop.json`);
  const roundsDir = join(options.outDir, `${packId}.facet-vocabulary-rounds`);
  const dryRunDir = join(options.outDir, `${packId}.facet-vocabulary-loop-dry-run`);

  const vocabularyInput = loadVocabularyInputArtifacts({
    packId,
    baseVocabularyPaths: options.baseVocabularyPaths,
    previousVocabularyPath: options.previousVocabularyPath,
    context: "facet-vocabulary-loop",
  });
  let previousVocabulary = vocabularyInput.vocabulary;
  let previousVocabularyPath = vocabularyInput.sourceLabel;

  const outputPaths = options.opts.dryRun
    ? [dryRunDir]
    : [vocabularyPath, reviewPath, workingPath, loopPath, roundsDir];
  if (!options.force && outputPaths.some((path) => existsSync(path))) {
    console.error(`[facet-vocabulary-loop] output already exists for pack ${packId}`);
    console.error(`[facet-vocabulary-loop] pass --force to regenerate loop outputs in ${options.outDir}`);
    process.exit(1);
    return;
  }
  if (options.force) {
    for (const path of outputPaths) {
      if (existsSync(path)) rmSync(path, { recursive: true, force: true });
    }
  }

  const model = options.opts.model ?? DEFAULT_STAGE3_MODEL;
  const client = options.opts.dryRun ? undefined : buildVocabularyClient(options.opts);
  const roundSummaries: Array<Record<string, unknown>> = [];
  let workingVocabulary = previousVocabulary;
  let finalVocabulary: PackFacetVocabulary | undefined;
  let finalReview: PackFacetVocabularyReview | undefined;

  console.log(
    `[facet-vocabulary-loop] evidence=${evidence.records.length} record(s), pack=${packId}, ` +
      `rounds=${options.rounds}, item-sample-size=${options.itemSampleSize}, ` +
      `item-sample-mode=${options.itemSampleMode}, facets=${options.facets.length > 0 ? options.facets.join(",") : "all"}`,
  );

  if (options.opts.dryRun) {
    mkdirSync(dryRunDir, { recursive: true });
  } else {
    mkdirSync(roundsDir, { recursive: true });
  }

  for (let round = 1; round <= options.rounds; round++) {
    const roundName = `round-${String(round).padStart(2, "0")}`;
    const roundSeed = `${options.itemSampleSeed ?? `${packId}:vocabulary-loop`}:${roundName}`;
    console.log(`[facet-vocabulary-loop] ${roundName}: sample-seed=${roundSeed}`);
    const result = await proposePackFacetVocabulary({
      evidence,
      evidencePath,
      packId,
      generatedBy: TOOL_VERSION,
      previousVocabulary: workingVocabulary,
      previousVocabularyPath,
      facets: options.facets,
      namespaces: options.namespaces,
      minEvidence: options.minEvidence,
      maxCandidatesPerFacet: options.maxCandidatesPerFacet,
      maxCandidatesPerPrompt: options.maxCandidatesPerPrompt,
      itemSampleSize: options.itemSampleSize,
      itemSampleMode: options.itemSampleMode,
      itemSampleSeed: roundSeed,
      vocabularyIteration: round,
      model,
      client,
      clientOptions: { signal: cliAbortSignal() },
    });
    finalVocabulary = result.vocabulary;
    finalReview = result.review;
    const working = buildWorkingVocabularyFromReview(result.vocabulary, result.review, {
      generatedAt: result.vocabulary.generated_at,
      round,
      roundSeed,
    });
    workingVocabulary = working;

    const summary = summarizeVocabularyLoopRound(round, roundSeed, result.vocabulary, result.review, working);
    roundSummaries.push(summary);
    console.log(
      `[facet-vocabulary-loop] ${roundName}: usable=${summary.usable_total} ` +
        `review=${summary.review_total} rejected=${summary.rejected_total}`,
    );

    if (options.opts.dryRun) {
      const promptDir = join(dryRunDir, roundName);
      const promptSummary = writeVocabularyPromptFiles(promptDir, result.prompts);
      roundSummaries[roundSummaries.length - 1] = {
        ...summary,
        prompts: promptSummary,
      };
    } else {
      const roundVocabularyPath = join(roundsDir, `${packId}.facet-vocabulary.${roundName}.json`);
      const roundReviewPath = join(roundsDir, `${packId}.facet-vocabulary.${roundName}.review.json`);
      const roundWorkingPath = join(roundsDir, `${packId}.facet-vocabulary.${roundName}.working.json`);
      writeFileSync(roundVocabularyPath, JSON.stringify(result.vocabulary, null, 2) + "\n");
      writeFileSync(roundReviewPath, JSON.stringify(result.review, null, 2) + "\n");
      writeFileSync(roundWorkingPath, JSON.stringify(working, null, 2) + "\n");
      previousVocabularyPath = roundWorkingPath;
      roundSummaries[roundSummaries.length - 1] = {
        ...summary,
        vocabulary: roundVocabularyPath,
        review: roundReviewPath,
        working_vocabulary: roundWorkingPath,
      };
    }
  }

  const loopSummary = {
    schema_version: 1,
    kind: "slot-pack-facet-vocabulary-loop",
    pack_id: packId,
    generated_by: TOOL_VERSION,
    generated_at: new Date().toISOString(),
    evidence: evidencePath,
    base_vocabulary: options.baseVocabularyPaths.length > 0
      ? options.baseVocabularyPaths.map((path) => resolve(path))
      : undefined,
    previous_vocabulary: options.previousVocabularyPath ? resolve(options.previousVocabularyPath) : undefined,
    model,
    rounds: options.rounds,
    item_sample_size: options.itemSampleSize,
    item_sample_mode: options.itemSampleMode,
    item_sample_seed: options.itemSampleSeed ?? null,
    facets: options.facets.length > 0 ? options.facets : "all",
    namespaces: options.namespaces.length > 0 ? options.namespaces : "all",
    round_summaries: roundSummaries,
  };

  if (options.opts.dryRun) {
    const summaryPath = join(dryRunDir, "summary.json");
    writeFileSync(summaryPath, JSON.stringify(loopSummary, null, 2) + "\n");
    console.log(`[facet-vocabulary-loop] dry run: wrote ${options.rounds} round prompt set(s) to ${dryRunDir}`);
    console.log(`[facet-vocabulary-loop] summary -> ${summaryPath}`);
    return;
  }

  if (!finalVocabulary || !finalReview || !workingVocabulary) {
    throw new Error("facet vocabulary loop produced no rounds");
  }
  writeFileSync(vocabularyPath, JSON.stringify(finalVocabulary, null, 2) + "\n");
  writeFileSync(reviewPath, JSON.stringify(finalReview, null, 2) + "\n");
  writeFileSync(workingPath, JSON.stringify(workingVocabulary, null, 2) + "\n");
  writeFileSync(loopPath, JSON.stringify(loopSummary, null, 2) + "\n");
  console.log(`[facet-vocabulary-loop] wrote final usable vocabulary ${vocabularyPath}`);
  console.log(`[facet-vocabulary-loop] wrote final review decisions ${reviewPath}`);
  console.log(`[facet-vocabulary-loop] wrote loop working vocabulary ${workingPath}`);
  console.log(`[facet-vocabulary-loop] wrote loop summary ${loopPath}`);
  printVocabularyHomeImpactAudit(finalVocabulary);
  console.log(`done in ${((Date.now() - start) / 1000).toFixed(2)}s`);
}

function buildWorkingVocabularyFromReview(
  vocabulary: PackFacetVocabulary,
  review: PackFacetVocabularyReview,
  metadata: {
    generatedAt?: string;
    round: number;
    roundSeed: string;
  },
): PackFacetVocabulary {
  const working = cloneJson(vocabulary);
  working.generated_by = TOOL_VERSION;
  if (metadata.generatedAt) working.generated_at = metadata.generatedAt;
  working.source = {
    ...(working.source ?? {}),
    vocabulary_loop_working_copy: {
      round: metadata.round,
      item_sample_seed: metadata.roundSeed,
      note: "Carries usable and rejected generator decisions into the next automated vocabulary refinement round. Review values are usable by default but remain watch-listed; rejected values are retained only as negative context.",
    },
  };

  for (const [facet, decisions] of Object.entries(review.decisions)) {
    for (const decision of decisions) {
      if (isUsableVocabularyState(decision.state)) continue;
      working.facets[facet] ??= { values: {} };
      working.facets[facet]!.values[decision.id] = vocabularyValueFromLoopDecision(decision);
    }
  }

  const validation = validateVocabularyArtifact(working);
  if (!validation.ok) {
    throw new Error(`working vocabulary failed validation: ${validation.errors.slice(0, 5).join("; ")}`);
  }
  return working;
}

function vocabularyValueFromLoopDecision(decision: VocabularyReviewDecision): VocabularyValue {
  const relatedActivity = sanitizeRelatedActivityIds(decision.related_activity ?? []);
  return {
    label: decision.label,
    origin: "stage3_proposed",
    state: decision.state,
    ...(decision.aliases?.length ? { aliases: decision.aliases } : {}),
    ...(decision.description ? { description: decision.description } : {}),
    ...(decision.examples?.length ? { seed_items: decision.examples } : {}),
    ...(relatedActivity.length ? { related_activity: relatedActivity } : {}),
    ...(decision.default_organization_group ? { default_organization_group: decision.default_organization_group } : {}),
    ...(decision.facet === "workflow_role" ? { parent: workflowRoleParentFromId(decision.id) } : {}),
  };
}

function workflowRoleParentFromId(id: string): string {
  return id.includes("#") ? id.slice(0, id.indexOf("#")) : "";
}

function sanitizeRelatedActivityIds(values: readonly string[]): string[] {
  return [...new Set(values.map((value) => value.trim()).filter((value) => {
    if (!value) return false;
    return !validateMultiValue("activity", [value]);
  }))].sort();
}

function summarizeVocabularyLoopRound(
  round: number,
  itemSampleSeed: string,
  vocabulary: PackFacetVocabulary,
  review: PackFacetVocabularyReview,
  workingVocabulary: PackFacetVocabulary,
): Record<string, unknown> {
  const usableEntries: Array<[string, number]> = Object.entries(vocabulary.facets)
    .map(([facet, value]): [string, number] => [facet, Object.keys(value.values).length])
    .sort(([a], [b]) => a.localeCompare(b));
  const usableByFacet: Record<string, number> = Object.fromEntries(
    usableEntries,
  );
  const reviewByFacet = Object.fromEntries(
    Object.entries(review.summary)
      .sort(([a], [b]) => a.localeCompare(b)),
  );
  const workingEntries: Array<[string, number]> = Object.entries(workingVocabulary.facets)
    .map(([facet, value]): [string, number] => [facet, Object.keys(value.values).length])
    .sort(([a], [b]) => a.localeCompare(b));
  const workingByFacet: Record<string, number> = Object.fromEntries(
    workingEntries,
  );
  const decisions = Object.values(review.decisions).flat();
  return {
    round,
    item_sample_seed: itemSampleSeed,
    usable_total: Object.values(usableByFacet).reduce((sum, count) => sum + count, 0),
    accepted_total: Object.values(usableByFacet).reduce((sum, count) => sum + count, 0),
    review_total: decisions.filter((decision) => decision.state === "review").length,
    rejected_total: decisions.filter((decision) => decision.state === "rejected").length,
    usable_by_facet: usableByFacet,
    accepted_by_facet: usableByFacet,
    review_by_facet: reviewByFacet,
    working_values_by_facet: workingByFacet,
  };
}

function writeVocabularyPromptFiles(
  outDir: string,
  prompts: Record<string, { system: string; user: string }>,
): Array<Record<string, unknown>> {
  mkdirSync(outDir, { recursive: true });
  const summary: Array<Record<string, unknown>> = [];
  for (const [facet, prompt] of Object.entries(prompts).sort(([a], [b]) => a.localeCompare(b))) {
    const fileStem = safeFileComponent(facet);
    const systemPath = join(outDir, `${fileStem}.system.md`);
    const userPath = join(outDir, `${fileStem}.user.json`);
    writeFileSync(systemPath, prompt.system);
    writeFileSync(userPath, prompt.user);
    const parsedUser = JSON.parse(prompt.user) as {
      context_records?: unknown[];
      candidates?: unknown[];
      facets?: Record<string, { context_records?: unknown[] }>;
      pack_item_overview?: unknown;
    };
    const promptContextRecords = Array.isArray(parsedUser.context_records)
      ? parsedUser.context_records
      : Array.isArray(parsedUser.candidates)
        ? parsedUser.candidates
        : isRecord(parsedUser.facets)
          ? Object.values(parsedUser.facets).flatMap((facet) => Array.isArray(facet?.context_records) ? facet.context_records : [])
          : [];
    const facetCount = isRecord(parsedUser.facets) ? Object.keys(parsedUser.facets).length : undefined;
    const runtimeItemSample = isRecord(parsedUser.pack_item_overview) && Array.isArray(parsedUser.pack_item_overview.runtime_item_sample)
      ? parsedUser.pack_item_overview.runtime_item_sample.length
      : 0;
    const chars = prompt.system.length + prompt.user.length;
    summary.push({
      facet,
      system: systemPath,
      user: userPath,
      context_records: promptContextRecords.length,
      context_records_without_semantic_context: countContextRecordsWithoutSemanticContext(promptContextRecords),
      semantic_context_entries: countSemanticContextEntries(promptContextRecords),
      ...(facetCount !== undefined ? { facet_count: facetCount } : {}),
      runtime_item_sample: runtimeItemSample,
      chars,
      approxTokens: Math.round(chars / 4),
    });
  }
  writeFileSync(join(outDir, "summary.json"), JSON.stringify(summary, null, 2) + "\n");
  return summary;
}

function runApplyPackFacetVocabularyReview(options: {
  vocabularyPath: string;
  reviewPath: string;
  outPath: string;
  force: boolean;
}): void {
  const vocabularyPath = resolve(options.vocabularyPath);
  const reviewPath = resolve(options.reviewPath);
  const outPath = resolve(options.outPath);
  if (existsSync(outPath) && !options.force) {
    console.error(`[facet-vocabulary-review] output already exists: ${outPath}`);
    console.error("[facet-vocabulary-review] pass --force to overwrite it");
    process.exit(1);
    return;
  }

  const loaded = validateVocabularyArtifactFile(vocabularyPath);
  if (!loaded.ok || !loaded.vocabulary) {
    console.error(`[facet-vocabulary-review] invalid vocabulary: ${vocabularyPath}`);
    for (const error of loaded.errors) console.error(`  ${error}`);
    process.exit(1);
    return;
  }
  let review: ReturnType<typeof readPackFacetVocabularyReviewFile>;
  try {
    review = readPackFacetVocabularyReviewFile(reviewPath);
  } catch (err) {
    console.error(`[facet-vocabulary-review] invalid review: ${(err as Error).message}`);
    process.exit(1);
    return;
  }

  const result = applyVocabularyReviewDecisions({
    vocabulary: loaded.vocabulary,
    review,
    generatedBy: TOOL_VERSION,
    generatedAt: new Date().toISOString(),
    reviewPath,
  });
  if (result.errors.length > 0) {
    console.error("[facet-vocabulary-review] review application failed");
    for (const error of result.errors.slice(0, 20)) console.error(`  ${error}`);
    process.exit(1);
    return;
  }

  mkdirSync(dirname(outPath), { recursive: true });
  writeFileSync(outPath, JSON.stringify(result.vocabulary, null, 2) + "\n");
  console.log(`[facet-vocabulary-review] wrote ${outPath}`);
  console.log(`[facet-vocabulary-review] applied ${result.changes.length} human decision(s)`);
  const counts = result.changes.reduce<Record<string, number>>((acc, change) => {
    acc[change.action] = (acc[change.action] ?? 0) + 1;
    return acc;
  }, {});
  for (const [action, count] of Object.entries(counts).sort(([a], [b]) => a.localeCompare(b))) {
    console.log(`  ${action.padEnd(8)} ${String(count).padStart(4)}`);
  }
}

async function runInteractivePackFacetVocabularyReview(options: {
  vocabularyPath: string;
  reviewPath: string;
  outPath: string;
  reviewOutPath?: string;
  facets: string[];
  includeAccepted: boolean;
  force: boolean;
}): Promise<void> {
  const vocabularyPath = resolve(options.vocabularyPath);
  const reviewPath = resolve(options.reviewPath);
  const outPath = resolve(options.outPath);
  const reviewOutPath = options.reviewOutPath ? resolve(options.reviewOutPath) : undefined;
  for (const path of [outPath, reviewOutPath].filter((path): path is string => !!path)) {
    if (existsSync(path) && !options.force) {
      console.error(`[facet-vocabulary-review] output already exists: ${path}`);
      console.error("[facet-vocabulary-review] pass --force to overwrite it");
      process.exit(1);
      return;
    }
  }

  const loaded = validateVocabularyArtifactFile(vocabularyPath);
  if (!loaded.ok || !loaded.vocabulary) {
    console.error(`[facet-vocabulary-review] invalid vocabulary: ${vocabularyPath}`);
    for (const error of loaded.errors) console.error(`  ${error}`);
    process.exit(1);
    return;
  }
  let review: PackFacetVocabularyReview;
  try {
    review = readPackFacetVocabularyReviewFile(reviewPath);
  } catch (err) {
    console.error(`[facet-vocabulary-review] invalid review: ${(err as Error).message}`);
    process.exit(1);
    return;
  }

  const selectedFacets = new Set(options.facets.filter(Boolean));
  const decisions = reviewableVocabularyDecisions(review, {
    selectedFacets,
    includeAccepted: options.includeAccepted,
  });

  console.log(`[facet-vocabulary-review] pack=${review.pack_id}`);
  console.log(
    `[facet-vocabulary-review] reviewing ${decisions.length} value(s) ` +
      `${selectedFacets.size > 0 ? `for ${[...selectedFacets].join(",")}` : "across all facets"}`,
  );
  if (!options.includeAccepted) {
    console.log("[facet-vocabulary-review] default mode reviews pending/review values only; review values are already usable by default. Pass --all to force y/n on accepted values too.");
  }

  const scriptedAnswers = process.stdin.isTTY ? undefined : readFileSync(0, "utf8").split(/\r?\n/);
  const rl = scriptedAnswers ? undefined : createInterface({ input: process.stdin });
  const counts = { approve: 0, reject: 0, skip: 0 };
  let quit = false;
  try {
    for (let index = 0; index < decisions.length; index++) {
      const decision = decisions[index]!;
      printVocabularyReviewDecision(decision, index + 1, decisions.length);
      process.stdout.write("Accept this value? [y/n, Enter skip, q quit] ");
      const answer = (scriptedAnswers
        ? scriptedAnswers.shift() ?? ""
        : await rl!.question("")
      ).trim().toLowerCase();
      if (scriptedAnswers) process.stdout.write("\n");
      if (answer === "q" || answer === "quit") {
        quit = true;
        break;
      }
      if (answer === "y" || answer === "yes") {
        decision.human_review = {
          decision: "approve",
          approved_id: decision.human_review?.approved_id || decision.id,
          approved_label: decision.human_review?.approved_label || decision.label,
          notes: decision.human_review?.notes ?? "",
        };
        counts.approve += 1;
        continue;
      }
      if (answer === "n" || answer === "no") {
        decision.human_review = {
          decision: "reject",
          notes: decision.human_review?.notes ?? "",
        };
        counts.reject += 1;
        continue;
      }
      counts.skip += 1;
    }

    const result = applyVocabularyReviewDecisions({
      vocabulary: loaded.vocabulary,
      review,
      generatedBy: TOOL_VERSION,
      generatedAt: new Date().toISOString(),
      reviewPath: reviewOutPath ?? reviewPath,
    });
    if (result.errors.length > 0) {
      console.error("[facet-vocabulary-review] review application failed");
      for (const error of result.errors.slice(0, 20)) console.error(`  ${error}`);
      process.exit(1);
      return;
    }

    mkdirSync(dirname(outPath), { recursive: true });
    writeFileSync(outPath, JSON.stringify(result.vocabulary, null, 2) + "\n");
    if (reviewOutPath) {
      mkdirSync(dirname(reviewOutPath), { recursive: true });
      writeFileSync(reviewOutPath, JSON.stringify(review, null, 2) + "\n");
    }

    console.log(`[facet-vocabulary-review] wrote usable vocabulary ${outPath}`);
    if (reviewOutPath) console.log(`[facet-vocabulary-review] wrote reviewed decisions ${reviewOutPath}`);
    console.log(
      `[facet-vocabulary-review] decisions: ` +
        `approved=${counts.approve}, rejected=${counts.reject}, skipped=${counts.skip}` +
        `${quit ? " (quit early)" : ""}`,
    );
    console.log(`[facet-vocabulary-review] applied ${result.changes.length} human decision(s)`);
  } finally {
    rl?.close();
  }
}

interface Stage3VocabularyProposalDecision {
  item: string;
  facet: string;
  label: string;
  proposed_id?: string;
  rationale: string;
  evidence?: string[];
  decision: "approve" | "reject" | "skip" | "already_usable" | "invalid";
  approved_id?: string;
  approved_label?: string;
  notes?: string;
}

async function runInteractiveStage3VocabularyProposalReview(options: {
  vocabularyPath: string;
  proposalsPath: string;
  outPath: string;
  reviewOutPath?: string;
  facets: string[];
  force: boolean;
}): Promise<void> {
  const vocabularyPath = resolve(options.vocabularyPath);
  const proposalsPath = resolve(options.proposalsPath);
  const outPath = resolve(options.outPath);
  const reviewOutPath = options.reviewOutPath ? resolve(options.reviewOutPath) : undefined;
  for (const path of [outPath, reviewOutPath].filter((path): path is string => !!path)) {
    if (existsSync(path) && !options.force) {
      console.error(`[stage3-vocabulary-review] output already exists: ${path}`);
      console.error("[stage3-vocabulary-review] pass --force to overwrite it");
      process.exit(1);
      return;
    }
  }

  const loaded = validateVocabularyArtifactFile(vocabularyPath);
  if (!loaded.ok || !loaded.vocabulary) {
    console.error(`[stage3-vocabulary-review] invalid vocabulary: ${vocabularyPath}`);
    for (const error of loaded.errors) console.error(`  ${error}`);
    process.exit(1);
    return;
  }

  let proposals: VocabularyProposal[];
  try {
    proposals = readStage3VocabularyProposalFile(proposalsPath);
  } catch (err) {
    console.error(`[stage3-vocabulary-review] invalid proposals: ${(err as Error).message}`);
    process.exit(1);
    return;
  }

  const vocabulary = cloneJson(loaded.vocabulary);
  const selectedFacets = new Set(options.facets.filter(Boolean));
  const reviewable = proposals
    .filter((proposal) => selectedFacets.size === 0 || selectedFacets.has(proposal.facet))
    .sort((a, b) => a.facet.localeCompare(b.facet) || (a.proposed_id ?? "").localeCompare(b.proposed_id ?? "") || a.item.localeCompare(b.item));

  console.log(`[stage3-vocabulary-review] pack=${vocabulary.pack_id}`);
  console.log(
    `[stage3-vocabulary-review] reviewing ${reviewable.length} proposal(s) ` +
      `${selectedFacets.size > 0 ? `for ${[...selectedFacets].join(",")}` : "across all facets"}`,
  );

  const scriptedAnswers = process.stdin.isTTY ? undefined : readFileSync(0, "utf8").split(/\r?\n/);
  const rl = scriptedAnswers ? undefined : createInterface({ input: process.stdin });
  const decisions: Stage3VocabularyProposalDecision[] = [];
  const counts = {
    approve: 0,
    reject: 0,
    skip: 0,
    alreadyUsable: 0,
    invalid: 0,
  };
  let quit = false;

  try {
    for (let index = 0; index < reviewable.length; index++) {
      const proposal = reviewable[index]!;
      const preflight = preflightVocabularyProposal(vocabulary, proposal);
      if (preflight.decision === "invalid" || preflight.decision === "already_usable") {
        decisions.push({
          item: proposal.item,
          facet: proposal.facet,
          label: proposal.label,
          proposed_id: proposal.proposed_id,
          rationale: proposal.rationale,
          evidence: proposal.evidence,
          ...preflight,
        });
        if (preflight.decision === "invalid") counts.invalid += 1;
        else counts.alreadyUsable += 1;
        printStage3VocabularyProposal(proposal, index + 1, reviewable.length, preflight.notes);
        continue;
      }

      printStage3VocabularyProposal(proposal, index + 1, reviewable.length);
      process.stdout.write("Accept this vocabulary value? [y/n, Enter skip, q quit] ");
      const answer = (scriptedAnswers
        ? scriptedAnswers.shift() ?? ""
        : await rl!.question("")
      ).trim().toLowerCase();
      if (scriptedAnswers) process.stdout.write("\n");
      if (answer === "q" || answer === "quit") {
        quit = true;
        break;
      }
      if (answer === "y" || answer === "yes") {
        const approved = approveStage3VocabularyProposal(vocabulary, proposal);
        decisions.push({
          item: proposal.item,
          facet: proposal.facet,
          label: proposal.label,
          proposed_id: proposal.proposed_id,
          rationale: proposal.rationale,
          evidence: proposal.evidence,
          decision: "approve",
          approved_id: approved.id,
          approved_label: approved.label,
        });
        counts.approve += 1;
        continue;
      }
      if (answer === "n" || answer === "no") {
        decisions.push({
          item: proposal.item,
          facet: proposal.facet,
          label: proposal.label,
          proposed_id: proposal.proposed_id,
          rationale: proposal.rationale,
          evidence: proposal.evidence,
          decision: "reject",
        });
        counts.reject += 1;
        continue;
      }
      decisions.push({
        item: proposal.item,
        facet: proposal.facet,
        label: proposal.label,
        proposed_id: proposal.proposed_id,
        rationale: proposal.rationale,
        evidence: proposal.evidence,
        decision: "skip",
      });
      counts.skip += 1;
    }

    vocabulary.generated_by = TOOL_VERSION;
    vocabulary.generated_at = new Date().toISOString();
    vocabulary.source = {
      ...(vocabulary.source ?? {}),
      stage3_vocabulary_review: {
        vocabulary: vocabularyPath,
        proposals: proposalsPath,
        review: reviewOutPath,
      },
    };

    const validation = validateVocabularyArtifact(vocabulary);
    if (!validation.ok) {
      console.error("[stage3-vocabulary-review] updated vocabulary failed validation");
      for (const error of validation.errors.slice(0, 20)) console.error(`  ${error}`);
      process.exit(1);
      return;
    }

    mkdirSync(dirname(outPath), { recursive: true });
    writeFileSync(outPath, JSON.stringify(vocabulary, null, 2) + "\n");
    if (reviewOutPath) {
      mkdirSync(dirname(reviewOutPath), { recursive: true });
      writeFileSync(reviewOutPath, JSON.stringify({
        schema_version: 1,
        kind: "slot-stage3-vocabulary-proposal-review",
        pack_id: vocabulary.pack_id,
        generated_by: TOOL_VERSION,
        generated_at: vocabulary.generated_at,
        source: {
          vocabulary: vocabularyPath,
          proposals: proposalsPath,
        },
        decisions,
      }, null, 2) + "\n");
    }

    console.log(`[stage3-vocabulary-review] wrote updated vocabulary ${outPath}`);
    if (reviewOutPath) console.log(`[stage3-vocabulary-review] wrote proposal decisions ${reviewOutPath}`);
    console.log(
      `[stage3-vocabulary-review] decisions: ` +
        `approved=${counts.approve}, rejected=${counts.reject}, skipped=${counts.skip}, ` +
        `already_usable=${counts.alreadyUsable}, invalid=${counts.invalid}` +
        `${quit ? " (quit early)" : ""}`,
    );
  } finally {
    rl?.close();
  }
}

function readStage3VocabularyProposalFile(path: string): VocabularyProposal[] {
  const parsed = JSON.parse(readFileSync(path, "utf8")) as unknown;
  if (!Array.isArray(parsed)) {
    throw new Error("proposal file must be a JSON array");
  }
  const proposals: VocabularyProposal[] = [];
  for (const raw of parsed) {
    if (!raw || typeof raw !== "object") {
      throw new Error("each proposal must be an object");
    }
    const proposal = raw as Record<string, unknown>;
    if (
      typeof proposal.item !== "string" ||
      typeof proposal.facet !== "string" ||
      typeof proposal.label !== "string" ||
      typeof proposal.rationale !== "string"
    ) {
      throw new Error(`proposal missing item/facet/label/rationale: ${JSON.stringify(raw).slice(0, 160)}`);
    }
    proposals.push({
      item: proposal.item,
      facet: proposal.facet,
      label: proposal.label,
      ...(typeof proposal.proposed_id === "string" ? { proposed_id: proposal.proposed_id } : {}),
      rationale: proposal.rationale,
      ...(Array.isArray(proposal.evidence)
        ? { evidence: proposal.evidence.filter((value): value is string => typeof value === "string") }
        : {}),
    });
  }
  return proposals;
}

function preflightVocabularyProposal(
  vocabulary: PackFacetVocabulary,
  proposal: VocabularyProposal,
): Pick<Stage3VocabularyProposalDecision, "decision" | "notes"> {
  const def = FACETS[proposal.facet];
  if (!def) {
    return { decision: "invalid", notes: `unknown facet '${proposal.facet}'` };
  }
  if (!def.vocabulary_backed) {
    return { decision: "invalid", notes: `facet '${proposal.facet}' is not vocabulary-backed` };
  }
  if (!proposal.proposed_id) {
    return { decision: "invalid", notes: "proposal is missing proposed_id" };
  }
  const issue = validateMultiValue(proposal.facet, [proposal.proposed_id]);
  if (issue) {
    return { decision: "invalid", notes: issue.reason };
  }
  const existing = vocabulary.facets[proposal.facet]?.values?.[proposal.proposed_id];
  if (isUsableVocabularyState(existing?.state)) {
    return { decision: "already_usable", notes: `already usable as '${existing.label}' (${existing.state})` };
  }
  return { decision: "skip" };
}

function approveStage3VocabularyProposal(
  vocabulary: PackFacetVocabulary,
  proposal: VocabularyProposal,
): { id: string; label: string } {
  const id = proposal.proposed_id!;
  const facet = vocabulary.facets[proposal.facet] ?? { values: {} };
  vocabulary.facets[proposal.facet] = facet;
  const existing = facet.values[id];
  const existingEvidence = existing?.evidence ?? [];
  const evidence: VocabularyEvidenceRef[] = uniqueVocabularyEvidence([
    ...existingEvidence,
    { kind: "stage3_vocabulary_proposal", id: proposal.item, confidence: 0.8 },
  ]);
  const seedItems = sortedUnique([...(existing?.seed_items ?? []), proposal.item]);
  const entry: VocabularyValue = {
    ...(existing ?? {}),
    label: proposal.label || existing?.label || id,
    origin: "manual",
    state: "accepted",
    description: existing?.description ?? proposal.rationale,
    evidence,
    seed_items: seedItems,
  };
  if (proposal.facet === "workflow_role") {
    entry.parent = id.split("#", 1)[0] ?? "";
  }
  facet.values[id] = entry;
  return { id, label: entry.label };
}

function printStage3VocabularyProposal(
  proposal: VocabularyProposal,
  index: number,
  total: number,
  note?: string,
): void {
  console.log("");
  console.log(`[${index}/${total}] ${proposal.facet}: ${proposal.label}`);
  console.log(`item: ${proposal.item}`);
  if (proposal.proposed_id) console.log(`proposed_id: ${proposal.proposed_id}`);
  console.log(`rationale: ${proposal.rationale}`);
  if (proposal.evidence?.length) console.log(`evidence: ${proposal.evidence.slice(0, 8).join(" | ")}`);
  if (note) console.log(`note: ${note}`);
}

function uniqueVocabularyEvidence(evidence: VocabularyEvidenceRef[]): VocabularyEvidenceRef[] {
  const seen = new Set<string>();
  const out: VocabularyEvidenceRef[] = [];
  for (const ref of evidence) {
    const key = `${ref.kind}\u0000${ref.id}`;
    if (seen.has(key)) continue;
    seen.add(key);
    out.push(ref);
  }
  return out;
}

function sortedUnique(values: string[]): string[] {
  return [...new Set(values)].sort((a, b) => a.localeCompare(b));
}

function cloneJson<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T;
}

function reviewableVocabularyDecisions(
  review: PackFacetVocabularyReview,
  options: {
    selectedFacets: ReadonlySet<string>;
    includeAccepted: boolean;
  },
): VocabularyReviewDecision[] {
  const out: VocabularyReviewDecision[] = [];
  for (const [facet, values] of Object.entries(review.decisions).sort(([a], [b]) => a.localeCompare(b))) {
    if (options.selectedFacets.size > 0 && !options.selectedFacets.has(facet)) continue;
    for (const decision of values) {
      if (decision.state === "rejected") continue;
      const humanDecision = decision.human_review?.decision;
      if (humanDecision && humanDecision !== "pending") continue;
      if (options.includeAccepted || decision.state === "review" || humanDecision === "pending") {
        out.push(decision);
      }
    }
  }
  return out;
}

function printVocabularyReviewDecision(
  decision: VocabularyReviewDecision,
  index: number,
  total: number,
): void {
  console.log("");
  console.log(`[${index}/${total}] ${decision.facet}: ${decision.label}`);
  console.log(`id: ${decision.id}`);
  console.log(`state: ${decision.state}`);
  if (decision.description) console.log(`description: ${decision.description}`);
  if (decision.rationale) console.log(`rationale: ${decision.rationale}`);
  if (decision.examples?.length) console.log(`examples: ${decision.examples.slice(0, 8).join(", ")}`);
  if (decision.aliases?.length) console.log(`aliases: ${decision.aliases.slice(0, 8).join(", ")}`);
  if (decision.parent) console.log(`parent: ${decision.parent}`);
  if (decision.related_activity?.length) console.log(`related_activity: ${decision.related_activity.join(", ")}`);
  if (decision.default_organization_group) console.log(`default_organization_group: ${decision.default_organization_group}`);
  if (decision.policy_notes?.length) console.log(`policy_notes: ${decision.policy_notes.join(" | ")}`);
}

function printVocabularyHomeImpactAudit(vocabulary: PackFacetVocabulary): void {
  const organizationGroups = usableVocabularyEntries(vocabulary, "organization_group");
  if (organizationGroups.length === 0) return;

  console.log("[facet-vocabulary] home-impact audit (section-producing facets only)");
  if (organizationGroups.length > 0) {
    const suffix = organizationGroups.length > ORGANIZATION_GROUP_HOME_REVIEW_THRESHOLD
      ? " REVIEW BEFORE FULL CLASSIFICATION"
      : "";
    console.log(`  organization_group       ${String(organizationGroups.length).padStart(4)} usable${suffix}`);
    if (organizationGroups.length > ORGANIZATION_GROUP_HOME_REVIEW_THRESHOLD) {
      console.log(
        "    usable organization_group values can become main wall sections; " +
          "semantic/query-only facets are not part of this count",
      );
      console.log(`    sample: ${formatVocabularyEntrySample(organizationGroups)}`);
    }
  }
}

function usableVocabularyEntries(
  vocabulary: PackFacetVocabulary,
  facet: string,
): Array<[string, { label?: string; state?: string }]> {
  return Object.entries(vocabulary.facets[facet]?.values ?? {})
    .filter(([, value]) => isUsableVocabularyState(value.state))
    .sort(([a], [b]) => a.localeCompare(b));
}

function formatVocabularyEntrySample(
  entries: readonly [string, { label?: string }][],
  limit = 12,
): string {
  return entries.slice(0, limit)
    .map(([id, value]) => value.label && value.label !== id ? `${id} (${value.label})` : id)
    .join(", ");
}

function countContextRecordsWithoutSemanticContext(contextRecords: readonly unknown[]): number {
  return contextRecords.filter((contextRecord) => {
    if (!contextRecord || typeof contextRecord !== "object") return true;
    const semantic = (contextRecord as { semantic_context?: unknown }).semantic_context;
    return !Array.isArray(semantic) || semantic.length === 0;
  }).length;
}

function countSemanticContextEntries(contextRecords: readonly unknown[]): number {
  let count = 0;
  for (const contextRecord of contextRecords) {
    if (!contextRecord || typeof contextRecord !== "object") continue;
    const semantic = (contextRecord as { semantic_context?: unknown }).semantic_context;
    if (Array.isArray(semantic)) count += semantic.length;
  }
  return count;
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
  const guardedOutputs = [
    ...(stages.stage2 ? [partialPath] : []),
    ...(stages.stage3 ? [completePath] : []),
  ];
  if (!options.force && guardedOutputs.some((path) => existsSync(path))) {
    console.error(`[pack-layer] output already exists for pack ${packId}`);
    console.error(`[pack-layer] pass --force to regenerate ${guardedOutputs.join(" / ")}`);
    process.exit(1);
    throw new Error(`output already exists for pack ${packId}`);
  }

  let records = readRuntimeExportRecords(runtimeItemsPath);
  const staticItemsPaths = options.staticItemsPaths.map((path) => resolve(path));
  let staticMatchingRecords = 0;
  let staticEnrichedRecords = 0;
  console.log(`[pack-layer] runtime records: ${records.length} item(s), pack=${packId}`);
  if (staticItemsPaths.length > 0) {
    const staticRecords = loadStaticItemEnrichmentRecords(staticItemsPaths, records);
    const merged = mergeRuntimeWithStaticRecords(records, staticRecords, "static-items");
    records = merged.records;
    staticEnrichedRecords += merged.enriched;
    console.log(
      `[pack-layer] static item enrichment: ${staticRecords.size} matching item record(s), ` +
        `${merged.enriched} runtime record(s) enriched`,
    );
  }
  if (options.modsPath) {
    const staticRecords = loadStaticEnrichmentRecords(options.modsPath, records);
    const merged = mergeRuntimeWithStaticRecords(records, staticRecords, "jar");
    records = merged.records;
    staticMatchingRecords = staticRecords.size;
    staticEnrichedRecords += merged.enriched;
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
        static_items: staticItemsPaths,
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
  }

  const stage3BaseLayer = stages.stage3
    ? createRuntimeStage3BaseLayer({
        packId,
        runtimeItemsPath,
        summaryPath,
        summary,
        modsPath: options.modsPath,
        staticItemsPaths,
        records,
      })
    : null;

  let layerForDatapack = stage2Layer && !stages.stage3 ? partialPath : null;
  if (stages.stage3 && stage3BaseLayer) {
    console.log(`[stage3] using LLM-only base layer; deterministic stage 2 facets are not merged`);
    ensureLiveBackendConfigured(stage3Opts, "generate-pack-layer stage 3");
    await executeStage3(records, stage3BaseLayer, completePath, stage3Opts, ["stage1", "stage3"]);
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
    staticItemsPaths: staticItemsPaths.length ? staticItemsPaths : undefined,
    staticMatchingRecords,
    staticEnrichedRecords,
    recordsPath,
    partialPath,
    completePath,
    layerForDatapack,
    datapackDir,
    records,
    summary,
    stage3BaseLayer,
  };
}

function createRuntimeStage3BaseLayer(args: {
  packId: string;
  runtimeItemsPath: string;
  summaryPath: string;
  summary: RuntimeExportSummary | null;
  modsPath?: string;
  staticItemsPaths?: readonly string[];
  records: readonly ItemExtractRecord[];
}): LayerFile {
  const layer: LayerFile = {
    schema_version: 1,
    layer: "modpack",
    source: args.packId,
    generated_by: TOOL_VERSION,
    generated_at: new Date().toISOString(),
    entries: {},
  };
  attachGenerationMetadata(layer, {
    sourceKind: "runtime-export",
    sourcePath: args.runtimeItemsPath,
    sourceVersion: args.summary?.minecraft_version,
    namespace: args.packId,
    stages: ["stage1"],
    inputMetadata: {
      runtime_summary: existsSync(args.summaryPath) ? args.summaryPath : null,
      static_items: args.staticItemsPaths ?? [],
      static_mods_path: args.modsPath ? resolve(args.modsPath) : null,
      loader: args.summary?.loader ?? null,
      minecraft_version: args.summary?.minecraft_version ?? null,
      runtime_item_count: args.summary?.item_count ?? args.records.length,
    },
  });
  return layer;
}

function createLlmStage3BaseLayer(args: {
  layerKind: LayerFile["layer"];
  source: string;
  sourceKind?: string;
  sourcePath?: string;
  sourceVersion?: string;
  namespace?: string;
  inputMetadata?: Record<string, unknown>;
}): LayerFile {
  const layer: LayerFile = {
    schema_version: 1,
    layer: args.layerKind,
    source: args.source,
    generated_by: TOOL_VERSION,
    generated_at: new Date().toISOString(),
    entries: {},
  };
  attachGenerationMetadata(layer, {
    sourceKind: args.sourceKind,
    sourcePath: args.sourcePath,
    sourceVersion: args.sourceVersion,
    namespace: args.namespace,
    stages: ["stage1"],
    inputMetadata: args.inputMetadata,
  });
  return layer;
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

function loadStaticItemEnrichmentRecords(
  staticItemsPaths: readonly string[],
  runtimeRecords: readonly ItemExtractRecord[],
): Map<string, ItemExtractRecord> {
  const runtimeIds = new Set(runtimeRecords.map((record) => record.id));
  const out = new Map<string, ItemExtractRecord>();
  for (const path of staticItemsPaths) {
    const resolved = resolve(path);
    const records = readRuntimeExportRecords(resolved);
    let matched = 0;
    for (const record of records) {
      if (!runtimeIds.has(record.id) || out.has(record.id)) continue;
      out.set(record.id, record);
      matched++;
    }
    console.log(`[pack-layer] static item file ${resolved}: records=${records.length}, matches=${matched}`);
  }
  return out;
}

function mergeRuntimeWithStaticRecords(
  records: readonly ItemExtractRecord[],
  staticRecords: ReadonlyMap<string, ItemExtractRecord>,
  sourceLabel: string,
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
      minecraft_tags: runtime.minecraft_tags.length > 0 ? runtime.minecraft_tags : stat.minecraft_tags,
      minecraft_tags_direct: runtime.minecraft_tags_direct.length > 0 ? runtime.minecraft_tags_direct : stat.minecraft_tags_direct,
      model_parents: runtime.model_parents.length > 0 ? runtime.model_parents : stat.model_parents,
      loot_table_sources: runtime.loot_table_sources.length > 0 ? runtime.loot_table_sources : stat.loot_table_sources,
      creative_tabs: runtime.creative_tabs.length > 0 ? runtime.creative_tabs : stat.creative_tabs,
      component_data: hasComponentData(runtime.component_data) ? runtime.component_data : stat.component_data,
      ...(semanticText ? { semantic_text: semanticText } : {}),
      extractor_meta: {
        ...(runtime.extractor_meta ?? {}),
        static_enrichment: sourceLabel,
        static_model_parents: stat.model_parents.length,
        static_loot_sources: stat.loot_table_sources.length,
        static_minecraft_tags_direct: stat.minecraft_tags_direct,
      },
    };
  });
  return { records: merged, enriched };
}

function hasComponentData(value: ItemExtractRecord["component_data"]): boolean {
  return !!value && Object.keys(value).length > 0;
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

function collectItemsWithoutLlmFacets(layer: LayerFile, expectedItemIds?: readonly string[]): string[] {
  const out: string[] = [];
  const itemIds = expectedItemIds ?? Object.keys(layer.entries);
  for (const itemId of itemIds) {
    const entry = layer.entries[itemId];
    const hasLlm = Object.values(entry?.facets ?? {}).some((facet) => {
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
      partial_layer: existsSync(args.run.partialPath) ? args.run.partialPath : null,
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
      vocabulary_proposals: countJsonArrayFile(args.run.completePath.replace(/\.complete\.json$/, ".vocabulary-proposals.json")),
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
    `- Vocabulary proposals: \`${review.vocabulary_proposals}\``,
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
  }

  if (stages.stage3) {
    const stage3BaseLayer = createLlmStage3BaseLayer({
      layerKind: "per-mod",
      source: mod.id,
      sourceKind: "jar",
      sourcePath: mod.path,
      sourceVersion: bundle?.version ?? mod.version,
      namespace: mod.id,
      inputMetadata: jarInputMetadata(mod),
    });
    if (stage2Layer) {
      console.log(`[stage3] using LLM-only base layer; stage 2 facets are diagnostic output only`);
    }
    await executeStage3(records, stage3BaseLayer, completePath, stage3Opts, ["stage1", "stage3"]);
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
  const usable = Object.values(result.vocabulary.facets ?? {})
    .reduce((count, facet) =>
      count + Object.values(facet.values ?? {}).filter((value) => isUsableVocabularyState(value.state)).length,
      0,
    );
  console.log(`[facet-vocabulary] loaded ${usable} usable value(s) for stage 3: ${resolvedPath}`);
  return {
    ...opts,
    facetVocabularyFile: resolvedPath,
    facetVocabulary: result.vocabulary,
  };
}

async function executeStage3(
  records: readonly ItemExtractRecord[],
  baseLayer: LayerFile,
  completePath: string,
  opts: Stage3CliOptions,
  pipelineStages: readonly string[] = ["stage1", "stage2", "stage3"],
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
    await dryRunStage3(records, baseLayer, only, opts, dirname(completePath));
    return;
  }

  ensureLiveBackendConfigured(opts, "stage 3");
  const client = buildClient(opts);

  const result = await runStage3({
    records,
    baseLayer,
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
  // facets were explicitly ambiguous.
  if (opts.retryModel) {
    const candidates = selectRetryCandidates(result.layer);
    console.log(
      `[stage3-retry] ${candidates.length} candidate item(s) flagged ambiguous`,
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
      result.vocabularyProposals.push(...retryResult.vocabularyProposals);
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
    stages: pipelineStages,
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
    ".vocabulary-proposals.json",
    ".corrections.json",
    ".fill-ins.json",
    ".response-mismatches.json",
    ".warnings.json",
  ]) {
    rmSync(completePath.replace(/\.complete\.json$/, suffix), { force: true });
  }
  const writtenFiles: { path: string; description: string }[] = [];
  writtenFiles.push({ path: completePath, description: "merged layer (LLM classification facets)" });

  if (result.proposals.length) {
    const proposalsPath = completePath.replace(/\.complete\.json$/, ".schema-proposals.json");
    writeFileSync(proposalsPath, JSON.stringify(result.proposals, null, 2) + "\n");
    writtenFiles.push({
      path: proposalsPath,
      description: `${result.proposals.length} schema proposal(s) — values/facets the LLM wanted but couldn't find in the schema`,
    });
  }
  if (result.vocabularyProposals.length) {
    const vocabularyProposalsPath = completePath.replace(/\.complete\.json$/, ".vocabulary-proposals.json");
    writeFileSync(vocabularyProposalsPath, JSON.stringify(result.vocabularyProposals, null, 2) + "\n");
    writtenFiles.push({
      path: vocabularyProposalsPath,
      description: `${result.vocabularyProposals.length} vocabulary proposal(s) — usable pack vocabulary was missing a useful value`,
    });
  }
  if (result.corrections.length) {
    const correctionsPath = completePath.replace(/\.complete\.json$/, ".corrections.json");
    writeFileSync(correctionsPath, JSON.stringify(result.corrections, null, 2) + "\n");
    writtenFiles.push({
      path: correctionsPath,
      description: `${result.corrections.length} compatibility correction(s) flagged by the LLM`,
    });
  }
  if (result.fillIns.length) {
    const fillInsPath = completePath.replace(/\.complete\.json$/, ".fill-ins.json");
    writeFileSync(fillInsPath, JSON.stringify(result.fillIns, null, 2) + "\n");
    writtenFiles.push({
      path: fillInsPath,
      description: `${result.fillIns.length} compatibility fill-in(s) emitted by the LLM`,
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
      kind: "COMPATIBILITY CORRECTIONS",
      summary: `${result.corrections.length} compatibility correction(s) emitted by the LLM`,
      detail: result.corrections.slice(0, 10).map((c) =>
        `${c.item} ${c.facet}: '${c.current}' -> '${c.suggested}' — ${c.rationale}`,
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
  if (result.vocabularyProposals.length) {
    reviewItems.push({
      kind: "VOCABULARY PROPOSALS",
      summary: `${result.vocabularyProposals.length} proposal(s) — usable pack vocabulary was missing a useful value`,
      detail: result.vocabularyProposals.slice(0, 10).map((p) =>
        `${p.item} ${p.facet}: '${p.label}'${p.proposed_id ? ` (${p.proposed_id})` : ""} — ${p.rationale}`,
      ),
      path: completePath.replace(/\.complete\.json$/, ".vocabulary-proposals.json"),
    });
  }
  if (result.fillIns.length) {
    reviewItems.push({
      kind: "COMPATIBILITY FILL-INS",
      summary: `${result.fillIns.length} compatibility fill-in(s) emitted by the LLM`,
      detail: result.fillIns.slice(0, 10).map((f) =>
        `${f.item} ${f.facet} = '${f.value}' — ${f.rationale}`,
      ),
      path: completePath.replace(/\.complete\.json$/, ".fill-ins.json"),
    });
  }
  if (result.warnings.length) {
    reviewItems.push({
      kind: "WARNINGS",
      summary: `${result.warnings.length} warning(s) from parser/merge validation`,
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
  baseLayer: LayerFile,
  only: readonly string[] | undefined,
  opts: Stage3CliOptions,
  outDir: string,
) {
  const batchSize = opts.batchSize ?? DEFAULT_STAGE3_BATCH_SIZE;
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

  const summary: Array<{
    batch: number;
    items: string[];
    system: string;
    user: string;
    systemChars: number;
    userChars: number;
    chars: number;
    approxTokens: number;
  }> = [];
  for (let i = 0; i < batches.length; i++) {
    const batch = batches[i]!;
    const payloads = batch.map((record) =>
      buildItemPayload(record, {}, opts.documentContextByItem?.[record.id])
    );
    const promptInput = {
      pack_id: opts.facetVocabulary?.pack_id,
      items: payloads,
      target_facets: targetFacets,
      facet_vocabulary: opts.facetVocabulary
        ? buildPromptFacetVocabulary(opts.facetVocabulary, targetFacets)
        : undefined,
      prompt_extras: {
        verbose_facet_disambiguation: opts.verboseFacetDisambiguation,
        verbose_common_misconceptions: opts.verboseCommonMisconceptions,
      },
    };
    const { system, user } = buildSplitPrompt(promptInput);
    const stem = `batch-${String(i + 1).padStart(2, "0")}`;
    const systemFile = join(dryRunDir, `${stem}.system.md`);
    const userFile = join(dryRunDir, `${stem}.user.json`);
    writeFileSync(systemFile, system);
    writeFileSync(userFile, user);
    summary.push({
      batch: i + 1,
      items: batch.map((r) => r.id),
      system: systemFile,
      user: userFile,
      systemChars: system.length,
      userChars: user.length,
      chars: system.length + user.length,
      approxTokens: Math.round((system.length + user.length) / 4),
    });
  }

  const summaryFile = join(dryRunDir, "summary.json");
  writeFileSync(summaryFile, JSON.stringify(summary, null, 2) + "\n");

  console.log(`[stage3] dry run: wrote ${batches.length} prompt(s) to ${dryRunDir}`);
  for (const s of summary) {
    console.log(
      `  batch ${s.batch}: ${s.items.length} items, ` +
        `system=${s.systemChars} chars user=${s.userChars} chars ` +
        `total=${s.chars} chars (~${s.approxTokens} tokens)`,
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
      resource roots. Defaults to stages 1,3 so the LLM owns semantic
      classification. For exact vanilla extraction, pass --mcmeta-ref with a
      versioned summary tag such as 1.20.1-summary. For installed jars, use
      classify-folder instead.

  extract --mod <id> --source <path> [options]
      Compatibility alias for source extraction/reference diagnostics. Defaults
      to stages 1,2 and does not call the LLM unless --stages includes 3.

  classify-folder --mods <mods-folder-or-instance-root> [options]
      Scan an installed mods folder or Prism-style instance root, then run
      local jar extraction and optional LLM semantic classification for missing
      layers. By default it skips bundled/covered mods, libraries, blocked
      jars, and entries whose <modid>.facets.complete.json already exists.
      Pass --mod <id> to target one or more mods, --include-covered to
      regenerate bundled/covered mods, --force to reprocess existing outputs,
      and --stages 1,3 to run LLM-owned semantic classification.

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
                               Large-prompt classification defaults to 1×1
                               for cache stability; raise deliberately.

  generate-pack-layer --runtime-export <pack.runtime-items.ndjson> [options]
      Generate a pack-specific classification layer from a live runtime export.
      Pass --mods <instance-or-mods-folder> to enrich runtime records with
      static jar facts such as model parents and loot sources. With --datapack,
      writes a drop-in datapack folder containing
      data/slot/classification/layers/<pack>.json.

      Pack-layer flags:
        --summary <path>        Explicit runtime-summary.json path.
        --static-items <path>   Merge matching item records from a static
                                extractor NDJSON before prompts. Repeatable;
                                useful for exact-version vanilla mcmeta data.
        --mods <path>           Prism instance root or mods/ folder for static
                                jar enrichment.
        --evidence <path>       Optional <pack>.facet-evidence.json. Stage 3
                                converts low-breadth guide/advancement records
                                into per-item document_context.
        --facet-vocabulary <path>
                                Usable pack facet vocabulary to ground
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
        --static-items <path>   Merge matching item records from a static
                                extractor NDJSON before building evidence.
                                Repeatable.
        --mods <path>           Prism instance root or mods/ folder for static
                                jar, guide, quest, advancement, and mod metadata
                                evidence.
        --pack-id <id>          Override output pack id.
        --out <dir>             Output directory (default out).
        --force                 Overwrite an existing facet-evidence file.

  propose-pack-facet-vocabulary --evidence <pack.facet-evidence.json> [options]
      Propose a pack-specific vocabulary for vocabulary-backed semantic facets.
      Reads <pack>.facet-evidence.json, builds ranked context records,
      optionally asks the configured LLM to synthesize values from them, and writes
      <out>/<pack>.facet-vocabulary.json plus
      <out>/<pack>.facet-vocabulary.review.json. Use --dry-run to write prompt
      pairs without spending tokens. Completed runs print a home-impact audit
      for section-producing facets only; broad semantic facets such as
      workflow/used_at/mod_subsystem are not judged as wall sections.

      Facet-vocabulary flags:
        --facet <id>            Regenerate one facet vocabulary. Repeatable.
        --namespace <id>        Limit evidence to one namespace. Repeatable.
        --base-vocabulary <path>
                                Reusable baseline vocabulary to include before
                                pack-specific values. Repeatable; use this for
                                the vanilla baseline for the target MC version.
        --previous-vocabulary <path>
                                Refinement-only carry-forward for an already
                                nearly satisfactory vocabulary. Previous values
                                are sticky context records; omit this for clean
                                baseline validation.
        --min-evidence <n>      Minimum context-record support for accepting a
                                value that the model did not synthesize
                                directly (default 2; base/previous/built-in
                                values bypass).
        --max-candidates-per-facet <n>
                                Bound prompt context records per facet (default 5000).
        --max-candidates-per-prompt <n>
                                Optional hard cap per prompt chunk. By default
                                each facet uses one prompt when it fits the
                                prompt budget, and splits only when needed.
        --item-sample-size <n> Include N rotating raw runtime item records in
                                the prompt (default ${DEFAULT_VOCABULARY_ITEM_SAMPLE_SIZE}; 0 disables).
        --item-sample-mode <m> random or coverage (default coverage). Coverage
                                uses only mechanical strata such as namespace,
                                guide linkage, recipe degree, creative tabs,
                                and raw tags.
        --item-sample-seed <s> Stable base seed for reproducible samples.
        --dry-run               Write prompt pairs only.
        --force                 Overwrite vocabulary/review outputs.

  refine-pack-facet-vocabulary --evidence <pack.facet-evidence.json> [options]
      Run several automated vocabulary refinement rounds before human review.
      Each round receives the stable semantic corpus, the previous round's
      working vocabulary, and a fresh rotating item sample. The
      final <pack>.facet-vocabulary.review.json is the human-review artifact;
      <pack>.facet-vocabulary.working.json is for continuing automated
      refinement only, not for classifier input.
      By default an unfiltered round uses one combined all-facet prompt.
      Passing --facet switches to focused per-facet prompt generation. Combined
      rounds cap each facet at 256 context records by default unless
      --max-candidates-per-facet is supplied.

      Refinement flags:
        --rounds <n>            Automated refinement rounds (default 3).
        --facet <id>            Regenerate one facet vocabulary. Repeatable.
        --namespace <id>        Limit evidence to one namespace. Repeatable.
        --base-vocabulary <path>
                                Reusable baseline vocabulary to include before
                                pack-specific values. Repeatable; use this for
                                the vanilla baseline for the target MC version.
        --previous-vocabulary <path>
                                Optional starting vocabulary.
        --item-sample-size <n>  Runtime item sample per round
                                (default ${DEFAULT_VOCABULARY_ITEM_SAMPLE_SIZE}; 0 disables).
        --item-sample-mode <m>  random or coverage (default coverage).
        --item-sample-seed <s>  Stable base seed for reproducible rounds.
        --dry-run               Write prompt pair(s) for each round only.
        --force                 Overwrite loop outputs.

  apply-pack-facet-vocabulary-review --vocabulary <pack.facet-vocabulary.json> --review <pack.facet-vocabulary.review.json> --out <approved.facet-vocabulary.json>
      Apply manual human_review decisions from a concise review JSON. Set
      human_review.decision to approve, reject, or rename; for rename, edit
      approved_id and/or approved_label. Pending decisions are ignored.
      Approved/renamed values are written as manual accepted vocabulary; skipped
      review-state values from the generator remain usable by default.

  review-pack-facet-vocabulary --vocabulary <pack.facet-vocabulary.json> --review <pack.facet-vocabulary.review.json> --out <approved.facet-vocabulary.json>
      Interactively review vocabulary generator output. Shows each pending
      review value with description, rationale, examples, and policy notes;
      press y to accept, n to decline, Enter to skip, or q to stop. Writes the
      usable vocabulary artifact for stage 3 --facet-vocabulary. Use
      --review-out <path> to also save the recorded human decisions, --facet to
      limit facets, and --all to force y/n review of accepted/usable values too.

  review-stage3-vocabulary-proposals --vocabulary <approved.facet-vocabulary.json> --proposals <pack.facets.vocabulary-proposals.json> --out <updated.facet-vocabulary.json>
      Interactively review useful vocabulary values suggested during a stage-3
      classification run. Shows each proposal with its item, facet, proposed id,
      and rationale; press y to accept it into the vocabulary, n to decline,
      Enter to skip, or q to stop. Approved values are written as manual
      usable vocabulary for the next stage-3 run. Use --review-out <path> to
      save the y/n decisions and --facet to limit review to selected facets.

  classify-runtime-pack --runtime-export <pack.runtime-items.ndjson> [options]
      Recommended one-command workflow for a real modpack runtime export.
      Combines runtime records, optional static jar enrichment, stage 3 semantic
      completion, missing-LLM repair, validation, datapack packaging, datapack
      zip creation, and a machine/human run report. Defaults are tuned for the
      cheap OpenRouter deepseek/deepseek-v4-flash path and record replay
      fixtures.

      Runtime-pack flags:
        --summary <path>        Explicit runtime-summary.json path.
        --static-items <path>   Merge matching item records from a static
                                extractor NDJSON before classification.
                                Repeatable.
        --mods <path>           Prism instance root or mods/ folder for static
                                jar enrichment.
        --evidence <path>       Optional <pack>.facet-evidence.json. Stage 3
                                converts low-breadth guide/advancement records
                                into per-item document_context.
        --facet-vocabulary <path>
                                Usable pack facet vocabulary to ground
                                vocabulary-backed Stage 3 values.
        --pack-id <id>          Override output/layer/datapack id.
        --out <dir>             Output directory (default out/<pack-id>).
        --stages <list>         Default 1,3. Stage 3 runtime-pack output is
                                LLM-only and does not merge stage 2 facets.
        --batch-size <n>        Items per Stage 3 LLM call for runtime-pack
                                runs (default ${DEFAULT_RUNTIME_PACK_BATCH_SIZE}).
        --concurrency <n>       Parallel Stage 3 calls for runtime-pack runs
                                (default ${DEFAULT_RUNTIME_PACK_CONCURRENCY}).
        --sample canary|N|ids   Restrict stage 3 to a subset; useful for dry
                                prompt review and canary classification.
        --no-datapack           Skip datapack folder output.
        --datapack-out <path>   Explicit datapack output folder.
        --pack-format <n>       Datapack pack_format.
        --no-zip                Skip datapack zip output.
        --force                 Overwrite existing generated outputs.
        --no-repair             Skip the missing-LLM repair pass.
        --repair-batch-size <n> Items per repair batch (default ${DEFAULT_RUNTIME_PACK_REPAIR_BATCH_SIZE}).
        --repair-concurrency <n>
                                Parallel repair batches (default ${DEFAULT_RUNTIME_PACK_REPAIR_CONCURRENCY}).

  validate <layer.json> [--vocabulary <facet-vocabulary.json>]
      Validate a layer file against layer.schema.json plus the live facet
      registry. With --vocabulary, vocabulary-backed facet values must be
      usable by that pack vocabulary artifact.

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
  --stages 1,3              Which stages to run. Classification commands
                            default to 1,3; extract defaults to 1,2.
                            Stage 2 is an explicit diagnostic/reference pass
                            and is not merged into LLM classification output.

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
  --batch-size <n>          Items per LLM call (default ${DEFAULT_STAGE3_BATCH_SIZE};
                            runtime-pack default ${DEFAULT_RUNTIME_PACK_BATCH_SIZE}).
  --concurrency <n>         Run up to N batches in parallel (default ${DEFAULT_STAGE3_CONCURRENCY};
                            runtime-pack default ${DEFAULT_RUNTIME_PACK_CONCURRENCY}).
                            Each parallel batch is an independent LLM call.
                            Set to 1 for serial/debugging or higher only when
                            intentionally trading request rate for wall-clock.
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
  --facet-vocabulary <path> Usable pack facet vocabulary. Vocabulary-backed
                            facets in Stage 3 are prompted to use only usable
                            ids from this file and final validation enforces it.

Retry pass (opt-in; runs after the first pass on ambiguous items):
  --retry-model <id>        OpenRouter retry model. Enabling this turns on retry.
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
      --retry-model openai/gpt-4.1-mini \\
      --retry-fixture-dir test/fixtures/stage3-canary-retry

  # Classify every mod in a modpack manifest (idempotent — re-run to resume):
  OPENROUTER_API_KEY=sk-or-... \\
    bun run src/cli.ts classify-modpack modpacks/test-modset.json --out out

  # Collect jar extraction/reference diagnostics from an installed Prism instance:
  bun run src/cli.ts classify-folder --mods /path/to/prism/instance --out out --stages 1,2

  # Classify missing mods from an installed Prism instance or mods folder:
  OPENROUTER_API_KEY=sk-or-... \\
    bun run src/cli.ts classify-folder --mods /path/to/prism/instance --mod createaddition \\
      --out out --stages 1,3 --record-replay --fixture-dir test/fixtures/createaddition-jar

  # Generate a static+runtime pack layer and package it as a datapack:
  bun run src/cli.ts generate-pack-layer \\
      --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \\
      --summary modpacks/exports/tfg2.runtime-summary.json \\
      --mods /path/to/TerraFirmaGreg-Modern \\
      --facet-vocabulary out/tfg2/tfg2.facet-vocabulary.json \\
      --stages 1,3 --datapack

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

  # Reclassify the whole manifest with large cached prompts. --force clears the
  # per-mod completion markers; keep concurrency low unless deliberately
  # trading cache stability and request rate for wall-clock.
  OPENROUTER_API_KEY=sk-or-... \\
    bun run src/cli.ts classify-modpack modpacks/test-modset.json --out out \\
      --stages 1,3 --force --batch-size 1000 --concurrency 1 --mod-concurrency 1

  # Convenience aliases (same as the large-prompt recipe above):
  bun run reclassify:test-modset       # reclassify only what changed
  bun run reclassify:test-modset:full  # force full stage outputs

Prompt-evaluation presets (60-item playtest sample; reads extracted records from out/):
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
