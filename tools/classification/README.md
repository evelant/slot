# Slot Classification Tool

Last updated: 2026-05-11

This Bun/TypeScript tool builds the item-classification data that SLOT loads at
runtime. Classification tells the wall and command services what an item is, how
players use it, which process or storage section it belongs near, and how to
explain that decision during diagnostics.

The current production path is:

1. Build or export item facts.
2. Derive deterministic facets from tags, recipes, components, ids, and jar
   resources.
3. Optionally run stage 3 LLM completion for judgement-call facets.
4. Validate the layer.
5. Ship bundled resources or package a modpack datapack layer.

No LLM runs in Minecraft. Runtime code only reads validated JSON layers through
`FacetIndex`.

## Current Runtime Contract

SLOT loads classification from:

- bundled resources under
  `common/src/main/resources/data/slot/classification/vanilla-base.json`
- bundled per-mod resources listed by
  `common/src/main/resources/data/slot/classification/per-mod/index.json`
- datapack layers at `data/slot/classification/layers/<pack>.json`

Both NeoForge 1.21.1 and Forge 1.20.1 expose:

```text
/slot classification status
/slot classification inspect <item_id>
/slot classification export <pack_id>
/slot classification rehome
```

`inspect` shows the loaded role/template/group/subsystem view for an item.
`export` writes live runtime item records and a summary after datapacks and
scripts have modified registries, tags, and recipes. `rehome` recomputes
classifier-owned homes for carried items and accessible claimed chests; it does
not move physical stacks.

The player surface is now the sectioned wall/list, not the old pan/zoom atlas.
Some Java identifiers still say `AtlasItem`, `AtlasCardBuilder`, or
`atlasItems` because the list-view migration intentionally avoided churn in
model names. Treat those as legacy names for wall cards/sections, not as the
product direction.

## Directory Map

- [`layer.schema.json`](layer.schema.json) validates layer files.
- `src/schema/` owns the facet registry, AJV validation wrapper, and pack
  facet-vocabulary artifact validation.
- `src/extract/` builds stage-1 `ItemExtractRecord` data from mcmeta summaries,
  mod source trees, installed jars, and runtime exports.
- `src/scan/` scans installed `mods/` folders without network or LLM calls.
- `src/deterministic/` derives stage-2 facets.
- `src/evidence/` assembles pack-level evidence for facet vocabulary work:
  runtime item facts, recipe summaries, tag summaries, mod metadata, guide
  pages, quest nodes, advancements, and diagnostics.
- `src/vocabulary/` proposes and curates pack facet vocabulary; candidate
  modules keep source-specific extraction and semantic-evidence handling
  separate from pipeline orchestration.
- `src/llm/` runs stage 3 completion, fixture record/replay, retry repair, and
  the OpenRouter live client.
- `datasets/<source>/` holds committed classification outputs that can be
  synced into runtime resources.
- `test/fixtures/<run>/` holds recorded LLM prompt/response fixtures.

Primary docs:

- [classification tool guide](../../docs/design/classification/tool-guide.md)
- [classification system overview](../../docs/design/classification/README.md)
- [facet vocabulary plan](../../docs/plans/classification-facet-vocabulary.md)
- [classification pipeline refactor plan](../../docs/plans/classification-pipeline-refactor.md)
- [item classification plan](../../docs/plans/item-classification.md)

## Recommended Pack Workflow

For KubeJS/datapack-heavy packs, start inside a loaded Minecraft instance:

```text
/slot classification export tfg2
```

Copy or point the generated export paths at this tool, then run:

```sh
cd tools/classification
bun install

bun run src/cli.ts collect-pack-facet-evidence \
  --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \
  --summary modpacks/exports/tfg2.runtime-summary.json \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --out out/tfg2 \
  --force

bun run src/cli.ts propose-pack-facet-vocabulary \
  --evidence out/tfg2/tfg2.facet-evidence.json \
  --out out/tfg2 \
  --force

bun run classify:runtime-pack -- \
  --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \
  --summary modpacks/exports/tfg2.runtime-summary.json \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --evidence out/tfg2/tfg2.facet-evidence.json \
  --facet-vocabulary out/tfg2/tfg2.facet-vocabulary.json \
  --out out/tfg2 \
  --force
```

`classify-runtime-pack` is the current one-command workflow for real packs. It:

- merges live runtime records with optional static jar facts
- passes gated guide/advancement snippets into stage 3 as `document_context`
  when `--evidence <pack>.facet-evidence.json` is supplied
- passes accepted pack vocabulary into stage 3 when `--facet-vocabulary` is
  supplied; vocabulary-backed facets must use accepted ids from that file
- runs deterministic facets and optional stage 3 completion
- repairs items that received no LLM-authored facets
- validates the final layer
- writes a datapack folder and zip
- emits `run-report.json` and `run-report.md`

Install the datapack into the test world or pack, reload, then verify:

```text
/datapack list enabled
/slot classification status
/slot classification inspect <known_pack_item>
/slot classification rehome
```

## Facet Vocabulary Work

Pack-specific semantic vocabulary is now part of the Stage 3 path:

- vocabulary-backed facets and scoped value-id grammar
- layer validation against a pack vocabulary artifact
- `validate-vocabulary`
- `collect-pack-facet-evidence`
- `propose-pack-facet-vocabulary`
- `--facet-vocabulary` prompting, retry validation, and final layer validation

Semantic evidence is the point of this pass. Preserve tooltip/lore text,
guidebook page bodies, quest text, lang-resolved advancement text,
KubeJS/datapack overlays, Ponder lang text, recipe-category lang labels,
KubeJS client tooltip mappings, stack-group names, zipped resource-pack lang
overrides, and mod descriptions as structured prompt evidence. Do not reduce
vocabulary generation to a few `seed_items` or terse recipe ids. The target
model is cheap and has a large context window, but spend that space on semantic
detail rather than provenance scaffolding: keep prompts free of jar/file paths,
source-ref lists, candidate scores, and other metadata that does not help the
model synthesize human-usable vocabulary.

Collect evidence before proposing vocabulary:

```sh
bun run src/cli.ts collect-pack-facet-evidence \
  --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \
  --summary modpacks/exports/tfg2.runtime-summary.json \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --out out/tfg2 \
  --force
```

This writes `out/<pack>.facet-evidence.json`. Missing guide, quest, or
advancement data is an informational diagnostic, not a failed run.
The same artifact can be passed to stage 3 with `--evidence`; the prompt builder
uses only conservative per-item `document_context` from low-breadth guide pages
and advancements. Current quest SNBT evidence stays vocabulary-only until the
quest adapter splits chapter files into local quest records.

Propose the vocabulary from that evidence:

```sh
bun run src/cli.ts propose-pack-facet-vocabulary \
  --evidence out/tfg2/tfg2.facet-evidence.json \
  --out out/tfg2 \
  --record-replay \
  --fixture-dir test/fixtures/tfg2-facet-vocabulary \
  --force
```

Run the first full vocabulary proposal for a pack without
`--previous-vocabulary`. That clean run is the validation baseline: it shows
whether the evidence, candidates, prompt, and policy can discover the pack's
real concepts without carry-forward bias. Use `--previous-vocabulary` only
after the generated vocabulary is already nearly satisfactory and you are
iterating on refinements. Previous values are injected back into the candidate
set; accepted previous values are treated as high-support sticky candidates so
they are preserved unless the model or policy explicitly rejects them.

Vocabulary ids use stable scoped values:

```text
slot:cooking
create:mechanical_power
pack:tfg2/steelmaking
pack:tfg2/steelmaking#input
```

The command writes `facet-vocabulary.json` and a concise
`facet-vocabulary.review.json` for manual approve/reject/rename decisions. Pass
the accepted vocabulary to Stage 3 with
`--facet-vocabulary`; the prompt lists accepted ids, Stage 3 drops and reports
invented vocabulary-backed values after parsing, and the final validator checks
the complete layer against the same artifact. Malformed or missing-item
responses still retry while the provider prompt cache is warm.

To approve reviewed vocabulary interactively, run:

```sh
bun run src/cli.ts review-pack-facet-vocabulary \
  --vocabulary out/tfg2/tfg2.facet-vocabulary.json \
  --review out/tfg2/tfg2.facet-vocabulary.review.json \
  --out out/tfg2/tfg2.facet-vocabulary.approved.json \
  --review-out out/tfg2/tfg2.facet-vocabulary.reviewed.json \
  --force
```

The reviewer shows only the vocabulary generator's decision output: label,
description, rationale, examples, aliases, parent links, and policy notes. It
does not walk evidence refs, source provenance, context records, or deterministic
candidate dumps. Press `y` to accept, `n` to decline, Enter to skip, or `q` to
stop. By default it reviews pending/review values; pass `--all` to force y/n
review of already accepted values too.

For non-interactive review, edit each pending `human_review` entry in
`facet-vocabulary.review.json` to `approve`, `reject`, or `rename`; for rename,
edit `approved_id` and/or `approved_label`. Then apply the review:

```sh
bun run src/cli.ts apply-pack-facet-vocabulary-review \
  --vocabulary out/tfg2/tfg2.facet-vocabulary.json \
  --review out/tfg2/tfg2.facet-vocabulary.review.json \
  --out out/tfg2/tfg2.facet-vocabulary.approved.json \
  --force
```

Use the approved artifact as the `--facet-vocabulary` input for full stage-3
classification.

The proposer defaults to a broad context-record budget
(`--max-candidates-per-facet 5000`) and tries to keep each facet in one prompt.
It only splits a facet when the prompt would exceed the configured prompt
budget. Use `--max-candidates-per-prompt <n>` only when deliberately forcing
smaller chunks for provider experiments or debugging.

## Other Common Commands

Scan an installed pack without extraction or LLM calls:

```sh
bun run src/cli.ts scan \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --out out/scan
```

Extract deterministic facets from installed jars:

```sh
bun run src/cli.ts classify-folder \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --out out \
  --stages 1,2
```

Generate a pack layer without the full wrapper:

```sh
bun run src/cli.ts generate-pack-layer \
  --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \
  --summary modpacks/exports/tfg2.runtime-summary.json \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --evidence out/tfg2/tfg2.facet-evidence.json \
  --facet-vocabulary out/tfg2/tfg2.facet-vocabulary.json \
  --out out/tfg2 \
  --stages 1,2,3 \
  --datapack \
  --force
```

Validate layer and vocabulary artifacts:

```sh
bun run src/cli.ts validate out/tfg2.pack.facets.complete.json
bun run src/cli.ts validate out/tfg2.pack.facets.complete.json \
  --vocabulary out/tfg2.facet-vocabulary.json
bun run src/cli.ts validate-vocabulary out/tfg2.facet-vocabulary.json
```

Regenerate committed bundled data:

```sh
bun run reclassify:vanilla
bun run reclassify:test-modset
bun run sync:vanilla
bun run sync:test-modset
```

## Outputs

Working outputs are gitignored under `out/`:

- `<source>.items.ndjson` and `<source>.items.meta.json`: stage-1 records.
- `<source>.facets.partial.json`: deterministic stage-2 layer.
- `<source>.facets.complete.json`: stage-2 plus stage-3 completion.
- `<pack>.facet-evidence.json`: pack-level evidence for vocabulary generation.
- `<pack>.facet-vocabulary.json`: pack-level vocabulary for vocabulary-backed
  semantic facets, including `mod_subsystem`.
- `<pack>.facet-vocabulary.review.json`: concise curation decisions,
  diagnostics, and pending human-review fields for the vocabulary artifact.
- `<pack>.pack.items.ndjson`: runtime records enriched with static jar facts.
- `<pack>.pack.facets.partial.json` / `.complete.json`: generated modpack
  classification layer.
- `<pack>.classification-datapack/` and `.zip`: drop-in datapack output.
- `<pack>.run-report.json` / `.md`: machine and human run summaries.
- `<pack>.pack.no-llm-items.json` / `.after-repair.json`: coverage gap audits.
- `<source>.facets.corrections.json`: LLM-flagged deterministic corrections.
- `<source>.facets.schema-proposals.json`: proposed schema/facet additions.
- `<source>.facets.response-mismatches.json`: malformed batch response audit.
- `<source>.facets.warnings.json`: parser and merge warnings.
- `<pack>.facet-vocabulary.json`: accepted pack semantic vocabulary.
- `<pack>.facet-vocabulary.review.json`: review/rejected vocabulary decisions.

Committed outputs live in `datasets/<source>/`, then `bun run sync:*` copies
them into `common/src/main/resources/data/slot/classification/`.

## Stage 3 Backend

Stage 3 uses `LlmClient` implementations in `src/llm/`:

- `OpenRouterClient` is the live path for production runs. The default model
  is `deepseek/deepseek-v4-flash`, pinned to the `deepseek` provider unless
  overridden.
- `--record-replay --fixture-dir <path>` records prompts and responses.
- `--use-replay --fixture-dir <path>` replays recorded fixtures without network
  calls.

The CLI auto-loads a repo-root `.env` without overriding existing environment
variables, so `OPENROUTER_API_KEY` can live there for manual runs.

## Verification

```sh
bunx tsc --noEmit
bun test
git diff --check
```

For runtime packaging bugs, verify both processed resources and jar contents.
The Forge 1.20 target must package the common classification resources just as
NeoForge does; if Forge routes most items to generic sections, inspect
`data/slot/classification/vanilla-base.json` and `per-mod/index.json` before
chasing tag-alias theories.
