# Slot Classification Pipeline

This directory will hold the Bun/TypeScript pipeline that produces item classification
layer files from mod source trees / jars. The wire-format schema lives here now
([layer.schema.json](layer.schema.json)); the implementation starts at milestone 3.

## Primary reference

**All design decisions, facet schema, pipeline stages, layer format, merge rules,
and test strategy live in** [`docs/plans/item-classification.md`](../../../docs/plans/item-classification.md).
Read that first. This README is just a landing page for developers who find themselves
in this directory.

## What's here today

- [`layer.schema.json`](layer.schema.json) — JSONSchema (Draft-07) that validates
  every classification layer file. Source of truth for the wire format.
- `src/extract/` — stage 1 extractor. Vanilla target is implemented; modded
  targets land in later milestones.
- `src/schema/validate.ts` — thin AJV wrapper around `layer.schema.json`.

## What will be here (milestone 4+)

See [Pipeline layout](../../../docs/plans/item-classification.md#pipeline-layout) in
the plan for the planned directory shape. `src/deterministic/`, `src/llm/`,
`src/neighbors/`, and `src/compile/` correspond to stages 2–5 and arrive as
subsequent milestones.

## Running the pipeline

```sh
cd reference/classification/pipeline
bun install
bun classify --mod minecraft --source ../mcmeta   # stage 1 only today
bun validate out/minecraft.json                   # wire-format schema check
bun test                                          # unit tests
```

The vanilla extractor reads from a clone of [misode/mcmeta](https://github.com/misode/mcmeta)
at `../mcmeta`. On first run it auto-creates a git worktree for the `summary`
branch under `../mcmeta/.worktrees/summary` and reads the consolidated
`*/data.min.json` files from there. To pick up a newer Minecraft version,
`git fetch` inside the mcmeta clone and delete the worktree; the next run
recreates it.

Output goes to `out/minecraft.items.ndjson` (one JSON record per item) and
`out/minecraft.items.meta.json` (extractor version, MC version, timestamp).

## LLM gateway

Stage 3 shells out to `claude -p` (Claude Code CLI in print mode). Contributors who
want to re-run stage 3 locally need their own Claude subscription. See
[LLM gateway: `claude -p`](../../../docs/plans/item-classification.md#llm-gateway-claude--p)
for the rationale.
