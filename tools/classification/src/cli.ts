import { existsSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
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
import { runDeterministic, type LayerFile } from "./deterministic/run.ts";
import { validateLayer, validateLayerFile } from "./schema/validate.ts";
import {
  ClaudeCliClient,
  RecordingLlmClient,
  ReplayLlmClient,
  type LlmClient,
} from "./llm/client.ts";
import { runStage3 } from "./llm/run.ts";
import { runStage3Retry, selectRetryCandidates } from "./llm/retry.ts";
import {
  buildBatchPrompt,
  buildItemPayload,
  defaultTargetFacets,
} from "./llm/prompt.ts";
import { VANILLA_CANARY_ITEMS } from "./llm/canary.ts";

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

function parseStages(input: string | undefined): StageSelection {
  if (!input) return { stage1: true, stage2: true, stage3: false };
  const set = new Set(input.split(",").map((s) => s.trim()));
  const known = new Set(["1", "2", "3"]);
  for (const s of set) {
    if (!known.has(s)) throw new Error(`unknown stage: ${s}`);
  }
  return { stage1: set.has("1"), stage2: set.has("2"), stage3: set.has("3") };
}

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

      if (mod === "minecraft") {
        await runVanilla(sourcePath, outDir, stages, {
          model: args.values.model,
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
        });
        return;
      }
      console.error(
        `classify: unknown mod '${mod}'. Only 'minecraft' is implemented today.`,
      );
      process.exit(2);
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
}

async function runVanilla(
  sourcePath: string,
  outDir: string,
  stages: StageSelection,
  stage3Opts: Stage3CliOptions,
) {
  const start = Date.now();
  console.log(`[vanilla] loading summary bundle from ${sourcePath}`);
  const source = ensureVanillaSource(sourcePath);
  const bundle = loadSummaryBundle(source);
  console.log(`[vanilla] MC version ${bundle.version}`);

  const ndjsonPath = join(outDir, "minecraft.items.ndjson");
  const metaPath = join(outDir, "minecraft.items.meta.json");
  const partialPath = join(outDir, "minecraft.facets.partial.json");
  const completePath = join(outDir, "minecraft.facets.complete.json");
  mkdirSync(dirname(ndjsonPath), { recursive: true });

  let records: ItemExtractRecord[];
  if (stages.stage1) {
    const { records: extracted, meta } = extractFromBundle(bundle, TOOL_VERSION);
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
      bundle,
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
  const live = new ClaudeCliClient();
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

  validate <layer.json>
      Validate a layer file against layer.schema.json.

Stage selection:
  --stages 1,2[,3]          Which stages to run. Default: 1,2 (stage 3 opt-in).

Stage 3 (LLM) knobs — only used when 3 is in --stages:
  --model <id>              Claude model id (default haiku). Accepts aliases
                            (haiku/sonnet/opus) or full model names.
  --effort <level>          Reasoning effort: low|medium|high|xhigh|max.
  --batch-size <n>          Items per LLM call (default 20 for haiku, try 10 for sonnet).
  --concurrency <n>         Run up to N batches in parallel (default 1 = serial).
                            Each parallel batch spawns its own claude -p process.
                            Recommended: 4 for sonnet on Max plan; cuts wall time
                            ~4x without affecting cost (each batch identical work).
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

Examples:
  bun run src/cli.ts classify --mod minecraft --source ../mcmeta
  bun run src/cli.ts classify --mod minecraft --source ../mcmeta --stages 3 --sample canary --dry-run
  bun run src/cli.ts classify --mod minecraft --source ../mcmeta --stages 3 --sample canary \\
      --record-replay --fixture-dir test/fixtures/stage3-canary
  bun run src/cli.ts classify --mod minecraft --source ../mcmeta --stages 3 --sample canary \\
      --model haiku --record-replay --fixture-dir test/fixtures/stage3-canary-haiku \\
      --retry-model sonnet --retry-effort max --retry-threshold 0.6 \\
      --retry-fixture-dir test/fixtures/stage3-canary-sonnet-retry
`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
