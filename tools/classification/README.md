# Slot Classification Pipeline

Bun/TypeScript pipeline that produces item-classification layer files from mod
source trees / jars. Outputs validate against [layer.schema.json](layer.schema.json).

## Primary reference

**All design decisions, facet schema, pipeline stages, layer format, merge rules,
and test strategy live in** [`docs/plans/item-classification.md`](../../docs/plans/item-classification.md).
Read that first. This README is just a landing page for developers who find themselves
in this directory.

## What's here today

- [`layer.schema.json`](layer.schema.json) — JSONSchema (Draft-07) that validates
  every classification layer file. Source of truth for the wire format.
- `src/extract/` — stage 1 extractor. Vanilla (mcmeta summary) and mod source
  trees (NeoForge / Forge layouts, with gradle.properties + `${mod_*}` template
  resolution) both supported.
- `src/deterministic/` — stage 2 rule-based facet derivation.
- `src/llm/` — stage 3 LLM completion via `claude -p`. Includes a per-mod
  `mod_subsystem` proposer pre-pass that reads README + mods.toml + recipe
  types and pins a canonical vocabulary into the system prompt; split-prompt
  mode (`--system-prompt` for the stable preamble, stdin for the per-batch
  payload); fixture-based record/replay for free resume; and transient-error
  retry in `ClaudeCliClient`.
- `src/schema/` — facet registry (v1 vocabulary) and AJV wrapper around
  `layer.schema.json`.
- `datasets/<source>/` — curated, git-tracked layer outputs the rest of the
  mod consumes. `datasets/minecraft/` holds the first vanilla v1 layer
  (1536 items) plus its corrections + schema-proposal audit trail.
- `test/fixtures/<run>/` — recorded LLM fixtures for past runs. Resuming a
  run with the same `--fixture-dir` replays them cache-first; new prompts
  call the LLM and persist new fixtures.

## What will be here (next stages)

See [Pipeline layout](../../docs/plans/item-classification.md#pipeline-layout)
in the plan. `src/neighbors/` (stage 4 nearest-neighbor precompute) and
`src/compile/` (stage 5 compile-and-ship) are the remaining stages. For the
current iteration the stage-3 output is already a valid layer file and is
copied directly into `datasets/`.

## Running the pipeline

```sh
cd tools/classification
bun install
# Vanilla, stages 1+2 (no LLM cost):
bun run src/cli.ts classify --mod minecraft --source ../mcmeta/.worktrees/summary
# Vanilla full LLM pass (concurrency 4, ~3h, hits 5h subscription cap once):
bun run src/cli.ts classify --mod minecraft --source ../mcmeta/.worktrees/summary \
    --stages 1,2,3 --model sonnet --concurrency 4 --batch-size 5 \
    --record-replay --fixture-dir test/fixtures/vanilla-full-v1
# Mod source tree (proposer fires automatically; cached at out/<modid>.subsystems.json):
bun run src/cli.ts classify --mod createaddition --source ../../reference/classification/createaddition \
    --stages 1,2,3 --model sonnet --concurrency 6 --batch-size 5 \
    --record-replay --fixture-dir test/fixtures/stage3-canary-createaddition-v5
# Validate any layer file:
bun run src/cli.ts validate datasets/minecraft/minecraft.facets.complete.json
```

The vanilla extractor reads from [misode/mcmeta](https://github.com/misode/mcmeta),
tracked as a git submodule at `tools/mcmeta`. On first run it auto-creates a git
worktree for the `summary` branch under `tools/mcmeta/.worktrees/summary` and
reads the consolidated `*/data.min.json` files from there. To pick up a newer
Minecraft version, `git -C tools/mcmeta fetch`, update the submodule commit,
and delete the worktree; the next pipeline run recreates it.

Outputs (working dir, gitignored):

- `out/<source>.items.ndjson` — stage 1, one JSON record per item.
- `out/<source>.items.meta.json` — stage 1 metadata (extractor, version, timestamp).
- `out/<source>.facets.partial.json` — stage 2 layer file.
- `out/<source>.facets.complete.json` — stage 3 merged layer (stage 2 + LLM).
- `out/<source>.subsystems.json` — proposer's canonical `mod_subsystem`
  vocabulary, cached so the proposer LLM call only fires once per mod.
- `out/<source>.facets.corrections.json` — stage-3 corrections the LLM flagged
  on stage-2 entries (review queue, never auto-applied).
- `out/<source>.facets.schema-proposals.json` — values/facets the LLM wanted
  but didn't find in the schema. Drives schema evolution.

Curated outputs (committed):

- `datasets/<source>/<source>.facets.complete.json` — what the runtime loads.
- `datasets/<source>/<source>.facets.corrections.json` — audit trail for the
  generation that produced `complete.json`.
- `datasets/<source>/<source>.facets.schema-proposals.json` — same audit trail
  for proposals.

## LLM gateway

Stage 3 shells out to `claude -p` (Claude Code CLI in print mode). Contributors who
want to re-run stage 3 locally need their own Claude subscription. See
[LLM gateway: `claude -p`](../../docs/plans/item-classification.md#llm-gateway-claude--p)
for the rationale.
