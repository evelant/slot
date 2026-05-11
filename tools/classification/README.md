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
- `src/llm/` runs stage 3 completion, runtime subsystem vocabulary proposal,
  fixture record/replay, retry repair, and OpenRouter / `claude-cli` backends.
- `datasets/<source>/` holds committed classification outputs that can be
  synced into runtime resources.
- `test/fixtures/<run>/` holds recorded LLM prompt/response fixtures.

Primary docs:

- [classification tool guide](../../docs/design/classification/tool-guide.md)
- [classification system overview](../../docs/design/classification/README.md)
- [facet vocabulary plan](../../docs/plans/classification-facet-vocabulary.md)
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

bun run classify:runtime-pack -- \
  --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \
  --summary modpacks/exports/tfg2.runtime-summary.json \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --evidence out/tfg2/tfg2.facet-evidence.json \
  --out out/tfg2 \
  --force
```

`classify-runtime-pack` is the current one-command workflow for real packs. It:

- merges live runtime records with optional static jar facts
- passes gated guide/advancement snippets into stage 3 as `document_context`
  when `--evidence <pack>.facet-evidence.json` is supplied
- generates or reuses runtime `mod_subsystem` vocabulary
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

The next classification track is pack-specific semantic vocabulary. Slices 0
through 2 are implemented:

- vocabulary-backed facets and scoped value-id grammar
- layer validation against a pack vocabulary artifact
- `validate-vocabulary`
- `collect-pack-facet-evidence`
- `propose-pack-facet-vocabulary`

Semantic evidence is the point of this pass. Preserve tooltip/lore text,
guidebook page bodies, quest text, lang-resolved advancement text,
KubeJS/datapack overlays, Ponder lang text, recipe-category lang labels,
KubeJS client tooltip mappings, stack-group names, zipped resource-pack lang
overrides, and mod descriptions as structured prompt evidence. Do not reduce
vocabulary generation to a few `seed_items` or terse recipe ids. The target
model is cheap and has a very large context window, so prefer rich auditable
context over over-compressed prompts.

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

Vocabulary ids use stable scoped values:

```text
slot:cooking
create:mechanical_power
pack:tfg2/steelmaking
pack:tfg2/steelmaking#input
```

The command writes `facet-vocabulary.json` and
`facet-vocabulary.review.json`. Do not treat a full regenerated pack layer as
publishable until stage-3 consumes the vocabulary and out-of-vocabulary
suggestions are routed to review.

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
  --subsystems-file out/tfg2.runtime-subsystems.json \
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
- `<source>.subsystems.json`: per-mod `mod_subsystem` vocabulary cache.
- `<pack>.runtime-subsystems.json`: namespace-scoped subsystem vocabulary from
  a runtime export.
- `<pack>.facet-evidence.json`: pack-level evidence for vocabulary generation.
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
- `<pack>.facet-vocabulary.review.json`: review/rejected vocabulary candidates.

Committed outputs live in `datasets/<source>/`, then `bun run sync:*` copies
them into `common/src/main/resources/data/slot/classification/`.

## Stage 3 Backend

Stage 3 uses `LlmClient` implementations in `src/llm/`:

- `openrouter` is the default path for production runs. The default model is
  `deepseek/deepseek-v4-flash`, pinned to the `deepseek` provider unless
  overridden.
- `claude-cli` shells out to `claude -p` and remains useful for comparison,
  canary work, and fallback runs.
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
