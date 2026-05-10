# Slot Classification Pipeline

Bun/TypeScript pipeline that produces item-classification layer files from mod
source trees / jars. Outputs validate against [layer.schema.json](layer.schema.json).

## Primary reference

For a concise "what this tool does / how it works / how to use it"
overview, read
[`docs/design/classification/tool-guide.md`](../../docs/design/classification/tool-guide.md).

**All design decisions, facet schema, pipeline stages, layer format, merge rules,
and test strategy live in** [`docs/plans/item-classification.md`](../../docs/plans/item-classification.md).
Read that first. This README is just a landing page for developers who find themselves
in this directory.

## What's here today

- [`layer.schema.json`](layer.schema.json) — JSONSchema (Draft-07) that validates
  every classification layer file. Source of truth for the wire format.
- `src/extract/` — stage 1 extractor. Vanilla (mcmeta summary), mod source
  trees (NeoForge / Forge layouts, with gradle.properties + `${mod_*}` template
  resolution), and installed mod jars are supported.
- `src/scan/` — installed `mods/` folder scanner. Reads local jar metadata,
  hashes, resource counts, item-candidate estimates, and Prism/Packwiz
  `.index/*.pw.toml` platform ids without network or LLM calls.
- `src/deterministic/` — stage 2 rule-based facet derivation.
- `src/llm/` — stage 3 LLM completion. Includes per-mod and runtime-export
  `mod_subsystem` proposer pre-passes that pin canonical vocabularies into
  the system prompt; split-prompt mode; fixture-based record/replay for free
  resume; OpenRouter support; and transient-error retry.
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
# Installed pack scan (no LLM/network):
bun run src/cli.ts scan --mods /path/to/prism/instance-or-minecraft/mods --out out/scan
# Installed pack jar extraction + deterministic facets (no LLM/network):
bun run src/cli.ts classify-folder --mods /path/to/prism/instance-or-minecraft/mods --out out --stages 1,2
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
# One installed mod from a pack, including stage 3:
bun run src/cli.ts classify-folder --mods /path/to/prism/instance --mod createaddition \
    --out out --stages 1,2,3 --record-replay --fixture-dir test/fixtures/createaddition-jar
# Runtime export subsystem vocabulary dry run:
bun run src/cli.ts propose-runtime-subsystems \
    --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \
    --summary modpacks/exports/tfg2.runtime-summary.json \
    --namespace create --namespace gtceu --dry-run
# Recommended one-command static+runtime pack workflow:
bun run classify:runtime-pack -- \
    --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \
    --summary modpacks/exports/tfg2.runtime-summary.json \
    --mods /path/to/prism/instance \
    --out out/tfg2 \
    --force
# Static+runtime pack layer packaged as a datapack folder:
bun run src/cli.ts generate-pack-layer \
    --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \
    --summary modpacks/exports/tfg2.runtime-summary.json \
    --mods /path/to/prism/instance \
    --subsystems-file out/tfg2.runtime-subsystems.json \
    --stages 1,2,3 --datapack
# Validate any layer file:
bun run src/cli.ts validate datasets/minecraft/minecraft.facets.complete.json
```

For KubeJS/datapack-heavy packs, run `/slot classification export
<pack_id>` inside the loaded Minecraft instance. It writes live
stage-1-compatible records under
`config/slot/classification/exports/`.

For in-game validation after installing a generated datapack, run:

```text
/slot classification inspect <item_id>
/slot classification rehome
```

`inspect` shows the loaded facet/template/subsystem view for one item.
`rehome` recomputes classifier-owned homes for every unique item in
carried storage plus currently accessible claimed chests; it does not
move physical stacks.

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
- `out/<pack>.runtime-subsystems.json` — pack-specific namespace-scoped
  `mod_subsystem` vocabulary generated from a live runtime export.
- `out/<pack>.pack.items.ndjson` — merged runtime records enriched with
  static jar facts when `generate-pack-layer --mods` or
  `classify-runtime-pack --mods` is used.
- `out/<pack>.pack.facets.partial.json` / `.complete.json` — generated
  pack-specific `layer: "modpack"` classification layer.
- `out/<pack>.classification-datapack/` — optional drop-in datapack folder
  containing `data/slot/classification/layers/<pack>.json`.
- `out/<pack>.classification-datapack.zip` — zipped datapack from the
  one-command runtime-pack workflow.
- `out/<pack>.run-report.json` / `.md` — machine and human summaries from
  `classify-runtime-pack`, including final paths, review counts, and LLM
  coverage before/after repair.
- `out/<pack>.pack.no-llm-items.json` / `.after-repair.json` — coverage-gap
  audit files written by `classify-runtime-pack`.
- `out/<source>.facets.corrections.json` — stage-3 corrections the LLM flagged
  on stage-2 entries (review queue, never auto-applied).
- `out/<source>.facets.schema-proposals.json` — values/facets the LLM wanted
  but didn't find in the schema. Drives schema evolution.
- `out/<source>.facets.response-mismatches.json` — batch responses that
  omitted requested ids or returned extra ids; missing ids are repaired or
  left visible for rerun.
- `out/<source>.facets.warnings.json` — parser/merge warnings retained for
  audit instead of only scrolling by in the terminal.

Curated outputs (committed):

- `datasets/<source>/<source>.facets.complete.json` — what the runtime loads.
- `datasets/<source>/<source>.facets.corrections.json` — audit trail for the
  generation that produced `complete.json`.
- `datasets/<source>/<source>.facets.schema-proposals.json` — same audit trail
  for proposals.

## LLM gateway

Stage 3 has two backends behind the same `LlmClient` interface
([`src/llm/client.ts`](src/llm/client.ts),
[`src/llm/openrouter-client.ts`](src/llm/openrouter-client.ts)):

- **`openrouter` (default)** — calls the official `@openrouter/sdk` against
  the OpenRouter API. The default model is `deepseek/deepseek-v4-flash`,
  pinned to the `deepseek` upstream provider for price / caching /
  throughput / known-good behaviour. Set `OPENROUTER_API_KEY` in env (the
  CLI auto-loads the repo-root `.env` without overriding existing env vars).
- **`claude-cli`** — shells out to `claude -p` (Claude Code CLI in print
  mode). Selected automatically when `--model` is a Claude alias
  (`haiku`/`sonnet`/`opus`) or a `claude-*` full id; pass explicitly via
  `--backend claude-cli` to force it. See
  [LLM gateway: `claude -p`](../../docs/plans/item-classification.md#llm-gateway-claude--p)
  for the rationale we kept it as a backend.

Backend is auto-inferred from `--model`: a slug containing `/` routes to
openrouter; otherwise claude-cli. The `--only-provider` flag also auto-
defaults to `deepseek` when the model is in the deepseek family — avoids
the SiliconFlow/DeepInfra flakiness we hit during evaluation.

Reasoning-effort flags (`--effort`, `--thinking-budget`) and Claude-specific
`--model` aliases are claude-cli-only and silently ignored on the openrouter
path.

Both backends round-trip through the same fixture-based record/replay
machinery; replay mode (`--use-replay --fixture-dir <path>`) bypasses the
network entirely.

### Why deepseek-v4-flash as default?

A/B-tested 2026-04-26 against Claude haiku/sonnet on a 60-item playtest
sample covering doors, beds, Block-of-X, rails, spawn eggs, mob drops,
buckets, ingredient-stage blocks, lighting, trophy items, and canonical
sanity items:

| Backend / model | Hits / 62 | Wall time | Notes |
|---|---|---|---|
| Claude haiku (lean prompt) | 20 | ~9 min | low cost, low accuracy |
| Claude sonnet | 41 + 17 dropped | ~6.5 min | accuracy capped by silent batch drops |
| **deepseek-v4-flash (deepseek pin)** | **60** | **~2 min** | best accuracy, ~20× cheaper than sonnet |
| deepseek-v4-pro (deepseek pin) | 59 | ~23 min | no quality lift; ~10× cost of flash |

v4-flash hits the sweet spot: better accuracy than sonnet, no batch
dropping, dramatically cheaper, ~3× faster wall time.

## Prompt-evaluation presets

[`scripts/eval-prompt.sh`](scripts/eval-prompt.sh) runs a 60-item playtest
sample (covering doors, beds, Block-of-X, rails, spawn eggs, mob drops,
buckets, ingredient-stage blocks, lighting, trophy/progression items, and
canonical sanity items) against a chosen model. Reads stage-1/2 outputs
from `out/` so it doesn't require an mcmeta clone.

```sh
bun run eval:sonnet                          # claude-cli + sonnet
OPENROUTER_API_KEY=... bun run eval:deepseek # openrouter + deepseek/deepseek-v4-flash
scripts/eval-prompt.sh --backend openrouter --model openai/gpt-4o-mini
```

Output writes to `/tmp/slot-prompt-eval-<label>/`.
