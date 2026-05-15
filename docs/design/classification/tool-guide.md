# Item Classification Tool Guide

Last updated: 2026-05-14

Concise operator/developer guide for the SLOT item classification tool:
what it does, how it works, how to use it today, and where the tool is
headed. For the full facet schema, read
[README.md](README.md) and [facet-kinds.md](facet-kinds.md).
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
  `activity=automation`, and `mod_subsystem=kinetics`.
- `tfc:ceramic/ingot_mold` can get `role=utility` plus
  `workflow=casting` and `organization_group=ceramics_molds`,
  which tells SLOT it belongs with casting molds instead of a broad Utility
  pile.

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

The modpack pipeline has four practical steps today.

### 1. Extract Evidence

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

### 2. Refine Vocabulary

The vocabulary pass is an LLM loop, not a deterministic candidate picker. Each
round receives broad semantic evidence, the previous round's working
vocabulary, and a rotating sample of raw item records. The model synthesizes
stable values for vocabulary-backed facets such as:

- `role`
- `form`
- `activity`
- `required_tool`
- `equip_slot`
- `origin`
- `storage_categories`
- `spawn_interaction`
- `combat_bonus`
- `environmental_property`
- `transport_medium`
- `workflow`
- `workflow_role`
- `used_at`
- `mod_subsystem`
- `organization_group`
- `multiblock_role`
- `food_category`, `food_use`, `preparation_state`,
  `material_process_stage`, `stock_profile`, `container_state`,
  `equipment_effect`, `protection_context`, `progression_stage`,
  `loadout_context`, `use_affordance`

Vocabulary-backed facets use stable facet-scoped values (`cooking`,
`mechanical_power`, `steelmaking`, and scoped `steelmaking#input`) rather than
display labels or artificial provenance prefixes. `mod_subsystem` is part of
the same `facet-vocabulary.json` artifact; there is no separate runtime
subsystem vocabulary pre-pass. Values marked `review` are usable by default and
kept as watchlist/debugging signal; they are not automatically rejected.

### 3. Classify Items

Stage 3 is an LLM judgement pass over item data and the usable vocabulary. The
vocabulary grounds naming consistency, but the model still decides each item's
facets. The runtime-pack path does not merge a precomputed semantic layer and
does not ask the model merely to fill gaps.

Valid model output is accepted into the layer. If a judgement looks wrong, flag
it for possible review or feed it into another vocabulary/classification
iteration; do not silently replace it with a hardcoded rule.

### 4. Assemble Runtime Artifacts

The final layer is validated for shape/loadability, cached with prompt
fixtures, and packaged as bundled base/per-mod resources or a modpack datapack.
Validation is not a second classifier and should not override LLM decisions.

`organization_group` is the stronger auto-home signal for large packs.
It answers "where would a skilled player put this item if the whole wall only
had roughly 15-20 broad sections?" and can split broad roles into storage
sections such as `ceramics_molds`, `beekeeping`, `glass_products`, or
`textiles` when the pack has enough sibling items for that group.
Runtime `organization_group` homing is temporarily disabled while
the next vocabulary refresh is validated, so `rehome` currently falls through to
the built-in templates instead of materializing `group:*` sections. Generic
sections like Ores & Raw Stock, Metal Stock, Gems & Crystals, Dusts & Powders,
Wood, Seeds, Crops, Plants, Ceramics & Molds, Organic Materials,
Stairs, Slabs, Food, Tools, and Storage remain the home even when the generated
data has a tempting query-style group. Wood covers stock wood
such as sticks, logs, planks, boards, and lumber; Seeds, Crops, Plants,
Ceramics & Molds, and Organic Materials cover their matching stock families. That default
ownership means "already a good home", not "bad grouping": item containers
belong in Storage, lamps/light sources in Lighting, crops in Crops, pottery/molds in
Ceramics & Molds, and redstone components in Redstone. Pack-broad families such
as Beekeeping or Glass Products can still be valid custom sections when they
have enough sibling items and do not merely rename a default.
Do not use `organization_group` for "Mod Name Items", mod subsystem labels, rock
taxonomy, stackable/pileable material properties, material form/state,
workstation-specific processes, or other slices that belong in search, filters,
task views, or within-section ordering. `mod_subsystem` stays semantic/query
evidence; it does not auto-create main-wall sections.

## Layer Outputs

Important files:

- `out/<source>.items.ndjson` — extracted item records
- `out/<source>.facets.partial.json` — exact/reference diagnostic layer when
  explicitly requested
- `out/<source>.facets.complete.json` — generated classification layer
- `out/<source>.facets.corrections.json` — compatibility correction channel,
  when present
- `out/<source>.facets.schema-proposals.json` — schema/value gaps
- `out/<source>.facets.fill-ins.json` — compatibility fill-in channel, when
  present

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

Run the legacy exact/reference diagnostic pass from installed jars:

```sh
bun run src/cli.ts classify-folder \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --out out \
  --stages 1,2
```

This command scans the folder, skips bundled/covered mods and libraries, then
runs stage 1 and 2 directly from jar resources. It is useful for diagnostics and
legacy bundled-data work. It is not the modpack semantic classification path.
For real packs, use runtime export, vocabulary refinement, and
`classify-runtime-pack`.

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
template/group target, raw subsystem evidence, and the dynamic auto-home target.

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
organization-group sections, and reports skipped claimed
chests when the storage is unloaded or otherwise inaccessible.

Run vanilla extraction plus the legacy exact/reference diagnostic pass:

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
  --stages 1,3 \
  --record-replay \
  --fixture-dir test/fixtures/createaddition
```

Classify every entry in a hand-authored modpack manifest:

```sh
bun run src/cli.ts classify-modpack \
  modpacks/test-modset.json \
  --out out \
  --stages 1,3
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
  --stages 1,3 \
  --datapack
```

The runtime export is the authoritative item/tag/recipe universe. The
optional `--mods` pass enriches matching runtime records with static jar
signals that the live export does not yet capture, such as model parents
and loot-table sources. The datapack output is a folder containing
`data/slot/classification/layers/<pack>.json`; drop it into a world or
pack datapacks folder so SLOT can load it without rebuilding the mod jar.

For a vanilla baseline vocabulary, use the same runtime-first shape. Export a
clean vanilla instance for the target Minecraft version and enrich it with
exact-version mcmeta records:

```sh
bun run src/cli.ts classify \
  --mod minecraft \
  --source tools/mcmeta \
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

Use the generated vanilla vocabulary as the starting vocabulary for modpack
vocabulary loops so packs inherit common Minecraft concepts instead of
rediscovering or conflicting with them.

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

Refine a pack vocabulary from that evidence:

```sh
bun run src/cli.ts refine-pack-facet-vocabulary \
  --evidence out/pack/pack.facet-evidence.json \
  --base-vocabulary out/vanilla-1.20.1/vanilla-1-20-1.facet-vocabulary.json \
  --out out/pack \
  --rounds 3 \
  --item-sample-size 1536 \
  --record-replay \
  --fixture-dir test/fixtures/pack-facet-vocabulary
```

Use `--dry-run` to inspect the prompts, `--facet <id>` to iterate on one
vocabulary-backed facet, `--namespace <id>` to limit evidence,
`--base-vocabulary <path>` for reusable vanilla/version baselines, and
`--previous-vocabulary <path>` only when deliberately continuing a nearly
satisfactory pack vocabulary.
The command writes `facet-vocabulary.json` plus
`facet-vocabulary.review.json`; stage 3 consumes usable values through
`--facet-vocabulary`.

Optionally review the generator output before using a new pack vocabulary for a
full classification run:

```sh
bun run src/cli.ts review-pack-facet-vocabulary \
  --vocabulary out/pack/pack.facet-vocabulary.json \
  --review out/pack/pack.facet-vocabulary.review.json \
  --out out/pack/pack.facet-vocabulary.approved.json \
  --review-out out/pack/pack.facet-vocabulary.reviewed.json
```

The reviewer iterates over vocabulary decisions, not input provenance or
context records. It prints the label, description, rationale, examples, aliases,
parent links, and policy notes; press `y` to accept, `n` to decline, Enter to
skip, or `q` to stop. Skipped `review` values remain usable by default. The
reviewed artifact can be passed as `--facet-vocabulary`, but the strategy is not
"reject unless manually approved"; it is "use LLM output unless a human or later
playtest explicitly rejects it."

For vocabulary generation, `context_records` and `pack_item_overview` are
evidence context, not allowed output ids. In particular, the `organization_group`
prompt includes pack-wide summaries for default-section pressure, runtime item
families, tag memberships, recipe-use neighborhoods, and human-visible text so
the model can synthesize the few broad storage sections a player would actually
maintain. Pack-specific storage families should use concise facet-scoped values
such as `beekeeping` or `glass_products`; provenance belongs in vocabulary
metadata, not the output value.

Vocabulary prompts are budget-driven: the loop uses one all-facet prompt when
it fits, or one prompt per facet for focused runs, and splits only when a prompt
would exceed the configured budget. Use `--max-candidates-per-prompt` only to
force smaller chunks deliberately.

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
slot-classify collect-pack-facet-evidence --runtime-export exports/pack.runtime-items.ndjson --mods /path/to/instance
slot-classify refine-pack-facet-vocabulary --evidence out/pack/pack.facet-evidence.json --base-vocabulary out/vanilla/vanilla.facet-vocabulary.json --rounds 3
slot-classify classify-runtime-pack --runtime-export exports/pack.runtime-items.ndjson --mods /path/to/instance --facet-vocabulary out/pack/pack.facet-vocabulary.json
```

For normal players, the ideal outcome is no command at all: SLOT ships
or fetches known public data. Unknown items can fall back to generic runtime
homes, but semantic pack classification should come from the offline LLM
authoring path rather than in-game deterministic curation.

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
- Treat runtime registry/tag/recipe facts as authoritative evidence for the LLM
  in a loaded pack, not as a semantic facet layer that overrides model output.
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
