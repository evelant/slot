# Schema Audit Classification Refresh

Generated for the facet schema audit on 2026-05-15.

## Inputs

- Vanilla runtime export:
  - `tools/classification/datasets/minecraft_runtime/vanilla-1-20-1.runtime-items.ndjson`
  - `tools/classification/datasets/minecraft_runtime/vanilla-1-20-1.runtime-summary.json`
- TFG runtime export copied from:
  - `tools/classification/modpacks/exports/tfg.runtime-items.ndjson`
  - `tools/classification/modpacks/exports/tfg.runtime-summary.json`
- TFG static/semantic evidence source:
  - `/Users/imagio/Library/Application Support/PrismLauncher/instances/TerraFirmaGreg-Modern`

## Pipeline

1. Collected vanilla evidence into `00-vanilla-vocabulary/`.
2. Collected TFG evidence into `01-pack-vocabulary/`.
3. Ran `refine-pack-facet-vocabulary --rounds 5` for vanilla into
   `00-vanilla-vocabulary-refined/`.
4. Ran `refine-pack-facet-vocabulary --rounds 5` for TFG into
   `01-pack-vocabulary-refined/`, using the refined vanilla vocabulary as
   `--base-vocabulary`.
5. Ran `classify-runtime-pack` for TFG into `02-pack-classification/` using:
   - `--facet-vocabulary 01-pack-vocabulary-refined/tfg.facet-vocabulary.json`
   - `--evidence 01-pack-vocabulary/tfg.facet-evidence.json`
   - `--batch-size 200`
   - `--concurrency 4`
   - `--repair-batch-size 200`
   - `--repair-concurrency 2`
6. Ran vanilla stage 3 from the up-to-date vanilla runtime export into
   `03-vanilla-classification/`, using
   `00-vanilla-vocabulary-refined/vanilla-1-20-1.facet-vocabulary.json`.

The earlier single-pass vocabulary outputs and smoke classification probes were
discarded from this archive so the remaining generated artifacts reflect the
five-round vocabulary refinement path.

## Results

- Vanilla refined vocabulary: 247 usable values after 5 rounds.
- TFG refined vocabulary: 488 usable values after 5 rounds.
- TFG classification: 30,831 entries, 266,541 LLM-authored facets.
- LLM coverage gap after main pass: 0 items.
- LLM coverage gap after repair: 0 items.
- Vanilla classification: 1,254 entries, promoted into
  `tools/classification/datasets/minecraft/minecraft.facets.complete.json` and
  `common/src/main/resources/data/slot/classification/vanilla-base.json`.
- The generated TFG layer is available as a datapack artifact but is not bundled
  into the base mod; pack-specific classification should be supplied through
  `data/slot/classification/layers/*.json`.
- Review outputs:
  - `02-pack-classification/tfg.pack.facets.schema-proposals.json`
  - `02-pack-classification/tfg.pack.facets.vocabulary-proposals.json`
  - `02-pack-classification/tfg.pack.facets.response-mismatches.json`
  - `02-pack-classification/tfg.pack.facets.warnings.json`
