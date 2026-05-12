# Item Classification Tool Guide

Last updated: 2026-05-11

Concise operator/developer guide for the SLOT item classification tool:
what it does, how it works, how to use it today, and where the tool is
headed. For the full facet schema, read
[README.md](README.md) and [../../plans/item-classification.md](../../plans/item-classification.md).
For the public database and pack UX plan, read
[../../plans/classification-database.md](../../plans/classification-database.md).

## What The Tool Does

The classification tool turns Minecraft item data into SLOT facet
layers: JSON files that describe what each item is, what it is made of,
what it is used for, and how it should be grouped.

Examples:

- `minecraft:iron_ingot` gets facets like `role=material`,
  `material_family=iron`, `form=ingot`, `processing_in=crafting`,
  and `primary_uses=[...]`.
- `create:cogwheel` can get facets like `role=mechanism`,
  `activity=slot:automation`, and `mod_subsystem=create:kinetics`.
- `tfc:ceramic/ingot_mold` can get `role=utility` plus
  `organization_group=tfc:casting`, which tells SLOT it belongs in the
  player's casting/metalworking area instead of a broad Utility pile.

SLOT uses these facets to make item organization feel semantic instead
of substring-driven. The immediate runtime use is better default homes
and section suggestions, plus `/slot classification inspect`, runtime
export, and rehome diagnostics. The next semantic consumers should be
task/search views backed by facets such as `workflow`, `used_at`,
`loadout_context`, and `use_affordance`.

## What The Tool Does Not Do

- It does not run LLM calls at game runtime.
- It does not mutate player inventories or save data.
- It does not make semantic guesses silently inside the game.
- It should not require every SLOT player to regenerate public mod data.
- It should not duplicate JEI/EMI recipe, usage, substitute, or
  acquisition surfaces; those tools already own that job.

The LLM step is an offline authoring step. Once a layer is generated and
reviewed for a mod release, it should be reused by everyone with the
same mod artifact.

The useful line is inventory semantics: where an item belongs, whether
it should be carried or stashed, which workflow or mod subsystem it
belongs to, and whether pack-specific scripts have changed what the
item means to the player.

## How It Works

The pipeline has three practical stages today.

### Stage 1: Extract

Reads item facts from a source and writes one NDJSON record per item:

- item id and display name
- item/block tags
- recipe inputs and outputs
- loot-table sources
- model parents
- item components when available

Current inputs:

- vanilla via the `mcmeta` summary branch
- mod source trees with common Forge/NeoForge resource layouts
- installed `mods/` folders and Prism-style instance roots, using static
  jar resources
- running game instances via `/slot classification export`, which captures
  the live registry/tag/recipe state after datapacks and scripts have loaded
- hand-authored modpack manifests pointing at source trees

Planned inputs:

- `.mrpack` files
- CurseForge manifests with locally available jars

### Stage 2: Deterministic Facets

Pure rules derive facts that should not require judgment:

- namespace
- material family
- form
- dye color
- processing recipe types
- origin hints
- tool requirement
- stackability/durability/fuel/light booleans

These rules should be auditable and deterministic. If a rule is wrong,
fix the rule or add a targeted correction; do not hide it behind a vague
fallback.

### Stage 3: Semantic Completion

An LLM fills judgment-call facets:

- `role`
- `activity`
- `workflow`
- `workflow_role`
- `used_at`
- `primary_uses`
- `palette`
- `flavor`
- `carry_frequency`
- `mod_subsystem`
- `organization_group`
- `food_category`, `food_use`, `preparation_state`,
  `material_process_stage`, `stock_profile`, `container_state`,
  `equipment_effect`, `protection_context`, `progression_stage`,
  `loadout_context`, `use_affordance`

The output is validated, cached, and reviewed. The model can propose
schema changes or flag deterministic-rule mistakes, but those are review
queues, not automatic truth.

Before stage 3, the tool can validate a pack facet vocabulary artifact.
Vocabulary-backed facets use stable ids (`slot:cooking`,
`create:mechanical_power`, `pack:tfg2/steelmaking`, and scoped
`pack:tfg2/steelmaking#input`) rather than display labels. `mod_subsystem`
is part of the same `facet-vocabulary.json` artifact; there is no separate
runtime subsystem vocabulary pre-pass.

`organization_group` is the stronger auto-home signal for large packs.
It answers "where would a skilled player put this item?" and can split
broad roles into workflow sections such as `tfc:casting`, `tfc:masonry`,
or `tfc:leatherworking` once the loaded layer has enough sibling items
for that group. Generic sections like Ingots or Tools remain the home
only when the generated data does not assert a better workflow group.

## Layer Outputs

Important files:

- `out/<source>.items.ndjson` — extracted item records
- `out/<source>.facets.partial.json` — stage-2 deterministic layer
- `out/<source>.facets.complete.json` — stage-2 + stage-3 layer
- `out/<source>.facets.corrections.json` — possible rule mistakes
- `out/<source>.facets.schema-proposals.json` — schema/value gaps
- `out/<source>.facets.fill-ins.json` — deterministic facts stage 2
  missed but the LLM noticed

Curated runtime resources live under:

- `common/src/main/resources/data/slot/classification/vanilla-base.json`
- `common/src/main/resources/data/slot/classification/per-mod/*.json`
- `common/src/main/resources/data/slot/classification/per-mod/index.json`

## How To Use It Today

Run from `tools/classification/`.

Install dependencies:

```sh
bun install
```

Scan an installed instance or `mods/` folder:

```sh
bun run src/cli.ts scan \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --out out/scan
```

The scan command does not use the network or an LLM. It writes an input
manifest v2 JSON report, preserves local Prism/Packwiz CurseForge or
Modrinth ids when `.index/*.pw.toml` files are present, and prints
coverage/status counts. It also flags KubeJS script/data/asset counts so
pack-specific runtime-export work is visible early.

Classify missing layers from installed jars without source checkouts:

```sh
bun run src/cli.ts classify-folder \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --out out \
  --stages 1,2
```

This command scans the folder, skips bundled/covered mods and libraries,
then runs stage 1 and 2 directly from jar resources. Add `--mod <id>` to
target a specific mod, `--include-covered` to regenerate a bundled mod,
or `--stages 1,2,3` to opt into the LLM semantic pass.

Export live runtime facts from a running Forge 1.20.1 or NeoForge 1.21.1
game:

```text
/slot classification export
/slot classification export <pack_id>
```

The command writes:

```text
config/slot/classification/exports/<pack-id>.runtime-items.ndjson
config/slot/classification/exports/<pack-id>.runtime-summary.json
```

Use this for KubeJS/datapack-heavy packs where static jar extraction is
not the final truth. Runtime export v1 captures item ids, display names,
translation keys, resolved item/block tag membership, stack/durability/
rarity/equipment/food-like signals, light emission for block items, and
recipe participation from the loaded recipe manager. Runtime item tags
come from live membership APIs, so `minecraft_tags` is populated and
`minecraft_tags_direct` is intentionally empty.

Inspect the classifier view of an item in a running instance:

```text
/slot classification inspect
/slot classification inspect <item_id>
```

Without an item id the command inspects the held main-hand item, then the
offhand item. The output includes loaded-layer diagnostics, raw facets,
template/group/subsystem target, and the dynamic auto-home target.

Recompute classifier-driven homes in a running instance:

```text
/slot classification rehome
/slot classification recompute
```

This scans every carried source SLOT can see (main inventory, hotbar,
offhand, backpacks/providers) plus every currently accessible claimed
chest, including claimed chests outside the proximity panel. It does
not move physical items. It rebuilds auto-home assignments for the
unique item identities it scanned, materializes qualified dynamic
organization-group or subsystem sections, and reports skipped claimed
chests when the storage is unloaded or otherwise inaccessible.

Run vanilla stages 1 and 2:

```sh
bun run src/cli.ts classify \
  --mod minecraft \
  --source ../mcmeta \
  --stages 1,2
```

Run a full semantic pass for vanilla or a mod source tree:

```sh
bun run src/cli.ts classify \
  --mod createaddition \
  --source ../../reference/classification/createaddition \
  --stages 1,2,3 \
  --record-replay \
  --fixture-dir test/fixtures/createaddition
```

Classify every entry in a hand-authored modpack manifest:

```sh
bun run src/cli.ts classify-modpack \
  modpacks/test-modset.json \
  --out out \
  --stages 1,2,3
```

Generate a pack-specific layer from both loaded runtime facts and static
jar facts, then package it as a datapack:

```sh
bun run src/cli.ts generate-pack-layer \
  --runtime-export exports/pack.runtime-items.ndjson \
  --summary exports/pack.runtime-summary.json \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --facet-vocabulary out/pack/pack.facet-vocabulary.json \
  --out out \
  --stages 1,2,3 \
  --datapack
```

The runtime export is the authoritative item/tag/recipe universe. The
optional `--mods` pass enriches matching runtime records with static jar
signals that the live export does not yet capture, such as model parents
and loot-table sources. The datapack output is a folder containing
`data/slot/classification/layers/<pack>.json`; drop it into a world or
pack datapacks folder so SLOT can load it without rebuilding the mod jar.

Collect pack-level evidence before proposing vocabulary-backed semantic
facets:

```sh
bun run src/cli.ts collect-pack-facet-evidence \
  --runtime-export exports/pack.runtime-items.ndjson \
  --summary exports/pack.runtime-summary.json \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --out out
```

This writes `out/<pack>.facet-evidence.json`. The artifact keeps compact
runtime item facts, recipe-type summaries, recipe-role summaries, recipe-id
families, item/block tags, optional mod metadata, guide pages, quest nodes,
advancements, and adapter diagnostics. Missing guide or quest data is an
informational diagnostic, not a failed run.

Propose a pack vocabulary from that evidence:

```sh
bun run src/cli.ts propose-pack-facet-vocabulary \
  --evidence out/pack/pack.facet-evidence.json \
  --out out/pack \
  --record-replay \
  --fixture-dir test/fixtures/pack-facet-vocabulary
```

Use `--dry-run` to inspect the per-facet curation prompts, `--facet <id>` to
iterate on one vocabulary-backed facet, `--namespace <id>` to limit evidence,
and `--previous-vocabulary <path>` to preserve earlier accepted ids.
The command writes `facet-vocabulary.json` plus
`facet-vocabulary.review.json`; stage 3 consumes the accepted values through
`--facet-vocabulary`.

For a real pack run, prefer the one-command wrapper:

```sh
bun run classify:runtime-pack -- \
  --runtime-export exports/pack.runtime-items.ndjson \
  --summary exports/pack.runtime-summary.json \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --out out/pack \
  --force
```

`classify-runtime-pack` runs the same static+runtime layer generation, defaults
stage 3 to the OpenRouter `deepseek/deepseek-v4-flash` path, records replay
fixtures, checks for items that received no LLM facets, runs a focused repair
pass, validates the final layer and datapack layer, writes a datapack zip, and
emits `run-report.json` plus `run-report.md`. The CLI auto-loads a repo-root
`.env`, so `OPENROUTER_API_KEY=...` can live there for manual runs.

Check a loaded server or singleplayer instance:

```text
/datapack list enabled
/slot classification status
```

`/datapack list enabled` proves Minecraft enabled the datapack.
`/slot classification status` proves SLOT discovered and parsed
`data/slot/classification/layers/*.json`, reporting total role-bearing
entries plus every loaded or failed datapack classification layer.

Validate a layer:

```sh
bun run src/cli.ts validate out/createaddition.facets.complete.json
bun run src/cli.ts validate out/tfg2.pack.facets.complete.json \
    --vocabulary out/tfg2.facet-vocabulary.json
bun run src/cli.ts validate-vocabulary out/tfg2.facet-vocabulary.json
```

Sync generated layers into runtime resources:

```sh
bun run sync:vanilla
bun run sync:test-modset
```

## Current Limitations

- Jar-backed extraction is static. It sees resources shipped in jars, but
  not live registry mutations, KubeJS recipe/tag edits, generated runtime
  items, or datapack changes.
- Hand-authored modpack manifests still point at cloned source
  repositories; installed pack folders use `classify-folder` instead.
- Existing resume behavior is strongest in `classify-runtime-pack`, which
  records fixtures and refuses to overwrite outputs unless `--force` is
  passed. Cache identity is still path/output based; future public-database
  work should key by jar hashes, platform file ids, runtime export hashes,
  and item-set signatures.
- Runtime consumes bundled vanilla/per-mod resources and datapack-provided
  pack layers under `data/slot/classification/layers/*.json`. Public DB
  fetch, mod-shipped layers, and richer override policy are planned work.
- Runtime export v1 does not yet include loaded-mod metadata, jar hashes,
  datapack/KubeJS provenance hashes, direct tag provenance, creative tabs,
  loot sources, or fuel tables. `generate-pack-layer --mods` compensates for
  some missing static signals by enriching runtime records from local jars.

## Target UX

The desired workflow is:

```sh
slot-classify scan --mods /path/to/instance/mods
slot-classify fetch-public --mods /path/to/instance/mods
slot-classify classify-folder --mods /path/to/instance/mods --stages 1,2
slot-classify generate-missing --mods /path/to/instance/mods --stages 1,2,3
slot-classify collect-pack-facet-evidence --runtime-export exports/pack.runtime-items.ndjson --mods /path/to/instance
slot-classify propose-pack-facet-vocabulary --evidence out/pack/pack.facet-evidence.json
slot-classify classify-runtime-pack --runtime-export exports/pack.runtime-items.ndjson --mods /path/to/instance
```

For normal players, the ideal outcome is no command at all: SLOT ships
or fetches known public data, then runtime crawl fills deterministic gaps.

For pack authors, the tool should report coverage first, then only
generate missing or pack-specific semantic data on explicit request.

For KubeJS-heavy packs, the best source is a running-instance export:

```text
/slot classification export
```

The implementation captures the actual loaded item registry, item and
block tag membership, recipe participation, and compact component signals.
Runtime tag membership is resolved rather than direct source provenance.
The generated datapack layer or zip is the deployable artifact; remaining
targets are datapack/KubeJS provenance hashes and stronger cache identity.

## Operating Rules

- Prefer exact artifact identity: jar hash, platform file id, and item
  set signature before mod-id/version fallback.
- Treat public semantic layers as reusable reviewed data, not something
  every user regenerates.
- Treat runtime registry/tag/recipe facts as authoritative for
  deterministic facets in a loaded pack.
- Keep KubeJS source snippets tightly scoped per item when feeding LLM
  prompts; never dump whole scripts by default.
- Surface uncertainty in reports and review queues.
- Never call LLMs at game runtime.
- Never scrape platform downloads; use official APIs or local jars.

## Where To Work Next

The near-term implementation plan is in
[../../plans/classification-database.md](../../plans/classification-database.md).
The first useful milestone is now the public-data and repeatability track:

1. key generated data by exact artifact/runtime-export identity
2. define the public database fetch/contribution shape
3. add downloaded-pack manifest ingestion where local jars are present
4. add datapack/KubeJS provenance hashes to runtime exports
5. tighten review tooling around report outputs and schema proposals

That milestone proves the pack UX before adding public database fetch,
mod-shipped layers, or broader LLM generation workflows.
