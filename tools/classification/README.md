# Slot Classification Tool

Last updated: 2026-05-14

This Bun/TypeScript tool builds the item-classification data that SLOT loads at
runtime. Classification tells the wall and command services what an item is, how
players use it, which process or storage section it belongs near, and how to
explain that decision during diagnostics.

The current modpack production path is:

1. Build or export item facts.
2. Build/refine pack vocabulary with an LLM loop over semantic text plus
   rotating item samples.
3. Run vocabulary-grounded LLM classification over item data. The LLM fills the
   classification layer directly; deterministic/stage-2 semantic guesses are
   not merged into the stage-3 output and do not constrain the model.
4. Validate the layer for shape/loadability.
5. Assemble the output into bundled base/per-mod resources or a modpack
   datapack layer.

Pre-LLM code gathers and formats evidence. It does not curate semantic facet
values, override model decisions, or discard valid model output because a rule
would have guessed differently. Review/watchlist flags are retained for
debugging and playtesting, but usable model output is accepted by default.

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
- `src/deterministic/` contains legacy/exact diagnostic rules. The runtime-pack
  LLM path does not use these rules as semantic authority.
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
- [schema authoring rules and audit](../../docs/design/classification/schema-authoring-rules.md)
- [LLM classification authoring plan](../../docs/plans/classification-facet-vocabulary.md)

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

bun run src/cli.ts refine-pack-facet-vocabulary \
  --evidence out/tfg2/tfg2.facet-evidence.json \
  --base-vocabulary out/vanilla-1.20.1/vanilla-1-20-1.facet-vocabulary.json \
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
- passes usable pack vocabulary into stage 3 when `--facet-vocabulary` is
  supplied; the vocabulary grounds naming consistency while the model still
  makes the per-item facet decisions
- supports `--dry-run --sample <N>` to write split `*.system.md` /
  `*.user.json` prompt files before spending LLM tokens, which is the best way
  to verify cacheable vs per-batch prompt size
- runs stage 3 classification against an LLM-only base layer; deterministic
  stage-2 output is not merged
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
- `refine-pack-facet-vocabulary`
- `--facet-vocabulary` prompting, retry validation, and final layer validation

Vocabulary generation is not a deterministic candidate picker. Evidence records
and item samples are context for model judgement, not an allowed output list.
Each refinement round receives the previous working vocabulary and a new item
sample so the LLM can improve the vocabulary from the pack's actual item
universe. The generated `review` state means "usable by default, keep an eye on
this"; it does not block classification unless a human explicitly changes the
artifact.

Run a vanilla baseline before pack-specific vocabulary for a new Minecraft
version. Use the live vanilla runtime export as the item universe, then enrich
it with the matching mcmeta summary tag so direct tags, model parents, and loot
sources are available without pulling in future-version items:

```sh
bun run src/cli.ts classify \
  --mod minecraft \
  --source /path/to/mcmeta \
  --mcmeta-ref 1.20.1-summary \
  --out out/vanilla-mcmeta-1.20.1 \
  --stages 1

bun run src/cli.ts collect-pack-facet-evidence \
  --runtime-export datasets/minecraft_runtime/vanilla-1-20-1.runtime-items.ndjson \
  --summary datasets/minecraft_runtime/vanilla-1-20-1.runtime-summary.json \
  --static-items out/vanilla-mcmeta-1.20.1/minecraft.items.ndjson \
  --out out/vanilla-1.20.1 \
  --pack-id vanilla-1-20-1 \
  --force
```

Then run the vanilla vocabulary loop and use its usable output as baseline
input for modpack vocabulary loops:

```sh
bun run src/cli.ts refine-pack-facet-vocabulary \
  --evidence out/vanilla-1.20.1/vanilla-1-20-1.facet-evidence.json \
  --out out/vanilla-1.20.1 \
  --pack-id vanilla-1-20-1 \
  --rounds 5 \
  --item-sample-size 1536 \
  --force
```

Pass that artifact to pack vocabulary refinement with `--base-vocabulary`.
The base vocabulary is reusable grounding, not a pack-specific prior judgment.
Do not use the moving mcmeta `summary` branch for a baseline unless the target
pack is actually on that version.

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

Refine the vocabulary from that evidence. The default workflow runs a few
automated rounds before human review. Each round sees the stable semantic
corpus, the previous round's working vocabulary, and a fresh compact
sample of raw runtime item records. The sample is observational only: item ids,
names, tags, components, recipe-role facts, creative tabs, and tooltip/lore
snippets. It does not include deterministic semantic guesses.

```sh
bun run src/cli.ts refine-pack-facet-vocabulary \
  --evidence out/tfg2/tfg2.facet-evidence.json \
  --base-vocabulary out/vanilla-1.20.1/vanilla-1-20-1.facet-vocabulary.json \
  --out out/tfg2 \
  --rounds 3 \
  --item-sample-size 1536 \
  --record-replay \
  --fixture-dir test/fixtures/tfg2-facet-vocabulary \
  --force
```

Run the first vocabulary loop for a pack with the matching vanilla
`--base-vocabulary`, but without `--previous-vocabulary`. That clean pack run
is the validation baseline: it shows whether the evidence, item samples,
prompt, and policy can discover the pack's real added concepts without
carry-forward bias. Use `--previous-vocabulary` only after the generated
pack vocabulary is already nearly satisfactory and you are iterating on
refinements.
The loop also writes `<pack>.facet-vocabulary.working.json`; that file is only
for continuing automated refinement because it carries usable values plus
rejected/watchlist context forward. Do not pass it to classification; use the
plain `<pack>.facet-vocabulary.json` artifact instead.

Vocabulary values are scoped by facet, not by artificial prefixes:

```text
cooking
mechanical_power
steelmaking
steelmaking#input
minecraft:furnace
```

Most semantic values are concise `lower_snake` strings. Use a
namespace-qualified value only when the value is a real registry/resource id,
such as a Minecraft effect, biome, or station id. Provenance lives in the
vocabulary metadata and review evidence, not in the value string.

The command writes `facet-vocabulary.json` and a concise
`facet-vocabulary.review.json` for optional approve/reject/rename decisions.
Pass the usable vocabulary to Stage 3 with `--facet-vocabulary`; the prompt
lists usable values, accepts `accepted` and `review` states, and the final
validator checks the complete layer against the same artifact. `review` means
"usable by default, but watch-listed for debugging/playtesting"; `rejected`
values are excluded only when a human or previous review artifact explicitly
marks them rejected.
Malformed or missing-item responses still retry while the provider prompt cache
is warm.

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
stop. By default it reviews pending/review values; skipped review values remain
usable by default. Pass `--all` to force y/n review of already accepted values
too.

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

Use the resulting usable artifact as the `--facet-vocabulary` input for full
stage-3 classification. The filename may say `approved` when produced by the
review command, but the important contract is that usable LLM vocabulary values
remain usable unless explicitly rejected.

The proposer defaults to a broad context-record budget
(`--max-candidates-per-facet 5000`). Unfiltered vocabulary refinement combines
all vocabulary facets into one all-facet prompt per round so the model can keep
the value sets coherent and the run stays to roughly one LLM call per round.
That combined path caps each facet's context records at 256 by default to leave
room for semantic snippets and the rotating item sample; pass
`--max-candidates-per-facet <n>` to override it for experiments.
Passing `--facet` uses the narrower per-facet prompt path for focused
debugging/regeneration. Use `--max-candidates-per-prompt <n>` only when
deliberately forcing smaller per-facet chunks for provider experiments or
debugging.

For one-shot prompt debugging, `propose-pack-facet-vocabulary` remains
available. It uses the same item-sample options as the loop:
`--item-sample-size <n>`, `--item-sample-mode random|coverage`, and
`--item-sample-seed <seed>`.

## Other Common Commands

Scan an installed pack without extraction or LLM calls:

```sh
bun run src/cli.ts scan \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --out out/scan
```

Extract exact/reference diagnostic facets from installed jars:

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
  --stages 1,3 \
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
- `<source>.facets.partial.json`: exact/reference diagnostic layer.
- `<source>.facets.complete.json`: stage-3 classification layer.
- `<pack>.facet-evidence.json`: pack-level evidence for vocabulary generation.
- `<pack>.facet-vocabulary.json`: pack-level vocabulary for vocabulary-backed
  semantic facets, including `mod_subsystem`.
- `<pack>.facet-vocabulary.review.json`: concise curation decisions,
  diagnostics, and pending human-review fields for the vocabulary artifact.
- `<pack>.facet-vocabulary.working.json`: loop carry-forward artifact with
  rejected/watchlist context for additional refinement rounds; not classifier
  input.
- `<pack>.facet-vocabulary.loop.json`: summary of automated vocabulary
  refinement rounds, sample seeds, and per-round counts.
- `<pack>.facet-vocabulary-rounds/`: per-round vocabulary/review/working
  artifacts for audit and replay.
- `<pack>.pack.items.ndjson`: runtime records enriched with static jar facts.
- `<pack>.pack.facets.complete.json`: generated modpack classification layer.
  `<pack>.pack.facets.partial.json` exists only when explicitly running the
  exact/reference diagnostic path.
- `<pack>.classification-datapack/` and `.zip`: drop-in datapack output.
- `<pack>.run-report.json` / `.md`: machine and human run summaries.
- `<pack>.pack.no-llm-items.json` / `.after-repair.json`: coverage gap audits.
- `<source>.facets.corrections.json`: compatibility LLM correction channel, when present.
- `<source>.facets.schema-proposals.json`: proposed schema/facet additions.
- `<source>.facets.response-mismatches.json`: malformed batch response audit.
- `<source>.facets.warnings.json`: parser and merge warnings.
- `<pack>.facet-vocabulary.json`: usable pack semantic vocabulary.
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
