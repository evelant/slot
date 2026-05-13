# TFG Classification Refresh 2026-05-12

Durable archive for the full TerraFirmaGreg classification refresh completed on
2026-05-13. The live LLM run took about six hours, so this directory keeps the
replay fixtures, runtime export input, generated layers, review reports, and
datapack output together instead of leaving them in ignored scratch `out/`.

## Layout

- `inputs/` - runtime export files used for the run.
- `outputs/` - generated evidence, vocabulary, final classification layer,
  review queues, run report, datapack folder, and datapack zip.
- `fixtures/` - OpenRouter replay cache for vocabulary, main stage 3, and the
  missing-item repair pass.

## Source Inputs

- Runtime items: `inputs/tfg.runtime-items.ndjson`
- Runtime summary: `inputs/tfg.runtime-summary.json`
- Static pack source during generation:
  `$HOME/Library/Application Support/PrismLauncher/instances/TerraFirmaGreg-Modern`

## Key Outputs

- Evidence: `outputs/tfg.facet-evidence.json`
- Vocabulary: `outputs/tfg.facet-vocabulary.json`
- Complete layer: `outputs/tfg.pack.facets.complete.json`
- Datapack: `outputs/tfg.classification-datapack/`
- Datapack zip: `outputs/tfg.classification-datapack.zip`
- Run report: `outputs/tfg.run-report.md`

Final run coverage from `outputs/tfg.run-report.json`:

- Entries: 30,831
- LLM facets: 222,811
- Missing LLM before repair: 66
- Missing LLM after repair: 0
- Repaired items: 66

## Replay Notes

Run from `tools/classification`. To reuse the cached LLM responses while writing
fresh scratch outputs, point `--fixture-dir` at this archive's `fixtures/`
subdirectories and write `--out` back to ignored `out/`.

Vocabulary replay:

```bash
bun run src/cli.ts propose-pack-facet-vocabulary \
  --evidence archives/tfg-refresh-20260512-final/outputs/tfg.facet-evidence.json \
  --out out/tfg-refresh-replay \
  --pack-id tfg \
  --max-candidates-per-facet 512 \
  --record-replay \
  --fixture-dir archives/tfg-refresh-20260512-final/fixtures/vocabulary \
  --force
```

Classification replay:

```bash
bun run src/cli.ts classify-runtime-pack \
  --runtime-export archives/tfg-refresh-20260512-final/inputs/tfg.runtime-items.ndjson \
  --summary archives/tfg-refresh-20260512-final/inputs/tfg.runtime-summary.json \
  --mods "$HOME/Library/Application Support/PrismLauncher/instances/TerraFirmaGreg-Modern" \
  --evidence archives/tfg-refresh-20260512-final/outputs/tfg.facet-evidence.json \
  --facet-vocabulary archives/tfg-refresh-20260512-final/outputs/tfg.facet-vocabulary.json \
  --out out/tfg-refresh-replay \
  --pack-id tfg \
  --stages 1,2,3 \
  --fixture-dir archives/tfg-refresh-20260512-final/fixtures/stage3 \
  --repair-batch-size 5 \
  --repair-concurrency 8 \
  --force
```

