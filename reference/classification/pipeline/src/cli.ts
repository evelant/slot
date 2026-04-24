import { mkdirSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { parseArgs } from "node:util";
import { extractVanilla } from "./extract/vanilla/extractor.ts";
import { validateLayerFile } from "./schema/validate.ts";

const TOOL_VERSION = "slot-classify v0.1.0";

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
          stages: { type: "string" }, // reserved: comma list, ignored at milestone 3
        },
        allowPositionals: false,
        strict: true,
      });
      const mod = args.values.mod;
      const sourcePath = args.values.source;
      if (!mod || !sourcePath) {
        console.error("usage: classify --mod <id> --source <path> [--out <dir>]");
        process.exit(2);
      }
      const outDir = resolve(args.values.out ?? "out");
      mkdirSync(outDir, { recursive: true });

      if (mod === "minecraft") {
        await runVanilla(sourcePath, outDir);
        return;
      }
      console.error(
        `classify: unknown mod '${mod}'. Only 'minecraft' is implemented at milestone 3.`,
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

async function runVanilla(sourcePath: string, outDir: string) {
  const start = Date.now();
  console.log(`[vanilla] extracting from ${sourcePath}`);
  const { meta, records } = extractVanilla({
    mcmetaRepoPath: sourcePath,
    generatedBy: TOOL_VERSION,
  });
  console.log(`[vanilla] MC version ${meta.source_version}, ${records.length} items`);

  const ndjsonPath = join(outDir, "minecraft.items.ndjson");
  const metaPath = join(outDir, "minecraft.items.meta.json");
  mkdirSync(dirname(ndjsonPath), { recursive: true });

  const ndjson = records.map((r) => JSON.stringify(r)).join("\n") + "\n";
  writeFileSync(ndjsonPath, ndjson);
  writeFileSync(metaPath, JSON.stringify(meta, null, 2) + "\n");

  console.log(`[vanilla] wrote ${ndjsonPath}`);
  console.log(`[vanilla] wrote ${metaPath}`);
  console.log(`[vanilla] done in ${((Date.now() - start) / 1000).toFixed(2)}s`);
}

function printHelp() {
  console.log(`slot-classify — item classification pipeline

Commands:
  classify --mod <id> --source <path> [--out <dir>]
      Run the pipeline against a source. Currently only --mod minecraft is
      wired up; the source must be a clone of misode/mcmeta.
  validate <layer.json>
      Validate a layer file against reference/classification/pipeline/layer.schema.json.

Example:
  bun run src/cli.ts classify --mod minecraft --source ../mcmeta
`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
