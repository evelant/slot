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
import { VANILLA_CANARY_ITEMS } from "./llm/canary.ts";

const TOOL_VERSION = "slot-classify v0.1.0";

interface StageSelection {
  stage1: boolean;
  stage2: boolean;
  stage3: boolean;
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
          sample: { type: "string" }, // `canary`, `N`, or comma-separated ids
          "fixture-dir": { type: "string" },
          "use-replay": { type: "boolean" },
          "record-replay": { type: "boolean" },
          "dry-run": { type: "boolean" },
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
          sample: args.values.sample,
          fixtureDir: args.values["fixture-dir"],
          useReplay: args.values["use-replay"] ?? false,
          recordReplay: args.values["record-replay"] ?? false,
          dryRun: args.values["dry-run"] ?? false,
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
  sample?: string;
  fixtureDir?: string;
  useReplay: boolean;
  recordReplay: boolean;
  dryRun: boolean;
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

  const client = buildClient(opts);
  if (opts.dryRun) {
    console.log(`[stage3] --dry-run: skipping LLM call. Exiting after prompt validation.`);
    return;
  }

  const result = await runStage3({
    records,
    stage2Layer,
    client,
    model: opts.model,
    batchSize: opts.batchSize,
    only,
    onBatch: (info) => {
      console.log(
        `[stage3] batch ${info.batchIndex + 1}/${info.batchCount} ` +
          `parsed=${info.parsed}/${info.items.length} ` +
          `warnings=${info.warnings.length} ` +
          `elapsed=${info.elapsedMs}ms`,
      );
    },
  });

  const validation = validateLayer(result.layer);
  if (!validation.ok) {
    console.error(`[stage3] layer failed schema validation`);
    for (const err of validation.errors.slice(0, 10)) console.error(`  ${err}`);
    process.exit(1);
  }
  writeFileSync(completePath, JSON.stringify(result.layer, null, 2) + "\n");
  console.log(`[stage3] wrote ${completePath}`);
  console.log(`[stage3] filled ${result.filledItems} items; coverage added:`);
  const facets = Object.keys(result.coverageAdded).sort(
    (a, b) => result.coverageAdded[b]! - result.coverageAdded[a]!,
  );
  for (const facet of facets) {
    console.log(`  ${facet.padEnd(22)} ${String(result.coverageAdded[facet]).padStart(5)}`);
  }
  if (result.proposals.length) {
    console.log(`[stage3] ${result.proposals.length} schema proposals (review before schema v2):`);
    for (const p of result.proposals.slice(0, 10)) console.log(`  ${JSON.stringify(p)}`);
  }
  if (result.warnings.length) {
    console.log(`[stage3] ${result.warnings.length} warnings:`);
    for (const w of result.warnings.slice(0, 10)) console.log(`  ${w}`);
  }
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
    return new RecordingLlmClient(live, resolve(opts.fixtureDir));
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
  --model <id>              Claude model id (default claude-haiku-4-5).
  --batch-size <n>          Items per LLM call (default 20).
  --sample canary|N|id,...  Restrict to a subset:
                              canary   – the hand-picked ~20-item set.
                              N        – first N records from the extract.
                              id,...   – explicit comma-separated item ids.
  --fixture-dir <path>      Directory for replay fixtures (prompt/response pairs).
  --record-replay           Call real claude -p AND persist fixtures to --fixture-dir.
  --use-replay              Read responses from --fixture-dir; never call claude -p.
  --dry-run                 Build prompts and stop before any LLM call.

Examples:
  bun run src/cli.ts classify --mod minecraft --source ../mcmeta
  bun run src/cli.ts classify --mod minecraft --source ../mcmeta --stages 3 --sample canary --dry-run
  bun run src/cli.ts classify --mod minecraft --source ../mcmeta --stages 3 --sample canary \\
      --record-replay --fixture-dir test/fixtures/stage3-canary
`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
