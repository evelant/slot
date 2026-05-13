# TFG Classification Refresh 2026-05-12

Durable archive for the full TerraFirmaGreg classification refresh completed on
2026-05-13. The live LLM run took about six hours, so this directory keeps the
replay fixtures, runtime export input, generated layers, review reports, and
datapack output together instead of leaving them in ignored scratch `out/`.

## Layout

- `00-runtime-export/` - runtime export files used for the run.
- `01-vocabulary-generation/` - facet evidence, accepted vocabulary, review
  output, and vocabulary replay fixtures.
- `02-pack-classification/` - generated item layer, review queues, run report,
  datapack folder/zip, repair output, and Stage 3 replay fixtures.

## Source Inputs

- Runtime items: `00-runtime-export/tfg.runtime-items.ndjson`
- Runtime summary: `00-runtime-export/tfg.runtime-summary.json`
- Static pack source during generation:
  `$HOME/Library/Application Support/PrismLauncher/instances/TerraFirmaGreg-Modern`

## Key Outputs

- Evidence: `01-vocabulary-generation/tfg.facet-evidence.json`
- Vocabulary: `01-vocabulary-generation/tfg.facet-vocabulary.json`
- Complete layer: `02-pack-classification/tfg.pack.facets.complete.json`
- Datapack: `02-pack-classification/tfg.classification-datapack/`
- Datapack zip: `02-pack-classification/tfg.classification-datapack.zip`
- Run report: `02-pack-classification/tfg.run-report.md`

Final run coverage from `02-pack-classification/tfg.run-report.json`:

- Entries: 30,831
- LLM facets: 222,811
- Missing LLM before repair: 66
- Missing LLM after repair: 0
- Repaired items: 66

## Replay Notes

Run from `tools/classification`. To reuse the cached LLM responses while writing
fresh scratch outputs, point `--fixture-dir` at this archive's stage fixture
subdirectories and write `--out` back to ignored `out/`.

Vocabulary replay:

```bash
bun run src/cli.ts propose-pack-facet-vocabulary \
  --evidence archives/tfg-refresh-20260512-final/01-vocabulary-generation/tfg.facet-evidence.json \
  --out out/tfg-refresh-replay \
  --pack-id tfg \
  --max-candidates-per-facet 512 \
  --record-replay \
  --fixture-dir archives/tfg-refresh-20260512-final/01-vocabulary-generation/fixtures \
  --force
```

Classification replay:

```bash
bun run src/cli.ts classify-runtime-pack \
  --runtime-export archives/tfg-refresh-20260512-final/00-runtime-export/tfg.runtime-items.ndjson \
  --summary archives/tfg-refresh-20260512-final/00-runtime-export/tfg.runtime-summary.json \
  --mods "$HOME/Library/Application Support/PrismLauncher/instances/TerraFirmaGreg-Modern" \
  --evidence archives/tfg-refresh-20260512-final/01-vocabulary-generation/tfg.facet-evidence.json \
  --facet-vocabulary archives/tfg-refresh-20260512-final/01-vocabulary-generation/tfg.facet-vocabulary.json \
  --out out/tfg-refresh-replay \
  --pack-id tfg \
  --stages 1,2,3 \
  --fixture-dir archives/tfg-refresh-20260512-final/02-pack-classification/fixtures/stage3 \
  --repair-batch-size 5 \
  --repair-concurrency 8 \
  --force
```
