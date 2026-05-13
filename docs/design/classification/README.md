# Classification System

Last updated: 2026-05-13

Classification is the data layer SLOT uses to understand items by meaning:
role, material, recipe participation, player activity, workflow context, and
storage/home intent. It powers wall section suggestions, classifier-owned
auto-home, diagnostics, and future task/search surfaces.

The old pan/zoom atlas is retired. The current product surface is a sectioned
wall/list. Some Java names still contain `Atlas` because the list-view migration
kept stable model and builder names, but the concept they now serve is wall
cards, sections, homes, and projections.

## Start Here

| Want to... | Go to |
| --- | --- |
| Run the tool | [tools/classification/README.md](../../../tools/classification/README.md) |
| Use the CLI day to day | [tool-guide.md](tool-guide.md) |
| Read facet type contracts | [facet-kinds.md](facet-kinds.md) |
| Follow vocabulary work | [../../plans/classification-facet-vocabulary.md](../../plans/classification-facet-vocabulary.md) |
| Read the original pipeline plan | [../../plans/item-classification.md](../../plans/item-classification.md) |
| Read database/distribution planning | [../../plans/classification-database.md](../../plans/classification-database.md) |

## What Problem It Solves

The wall needs a trustworthy signal for questions like:

- is this item a building block, tool, machine part, ingredient, food, or
  container?
- which material, form, tier, color, or environment does it belong to?
- should a large pack split this item into a player-facing section such as
  `pack:tfg/casting_molds` or `pack:tfg/masonry_supplies`?
- is this a broad role-only item that belongs in the generic Wood, Seeds,
  Crops, Plants, Clay & Pottery, Mob Drops, Materials, Building, Food,
  Tooling, or Utility sections?
- why did `/slot classification inspect` or `/slot classification rehome`
  choose a particular template, group, or subsystem?

The previous fallback was a hand-coded substring/tag ladder. It was good enough
for a prototype, but modded packs need data that can be audited, regenerated,
validated, and overridden per pack.

Classification replaces that ladder with layered JSON facets:

- deterministic facets from item ids, tags, recipes, loot, model parents,
  components, and runtime exports
- LLM-authored judgement facets for role, activity, flavor, palette,
  primary uses, subsystem identity, and organization group
- vocabulary-backed semantic facets for pack workflows, stations, food/process
  domains, stock/container behavior, equipment/protection context, progression,
  loadout contexts, and use affordances
- datapack modpack layers that can be loaded without rebuilding SLOT

No LLM runs in-game. Runtime code only consumes validated layer files.

## Runtime Shape

The runtime index is `dev.imagio.slot.classification.FacetIndex` in `common/`.
It is loaded by both NeoForge 1.21.1 and Forge 1.20.1.

Loaded sources:

1. bundled `vanilla-base.json`
2. bundled per-mod layers listed by `per-mod/index.json`
3. datapack layers at `data/slot/classification/layers/<pack>.json`

Runtime commands:

```text
/slot classification status
/slot classification inspect <item_id>
/slot classification export <pack_id>
/slot classification rehome
```

`status` reports loaded and failed layers. `inspect` explains one item.
`export` writes live stage-1-compatible runtime records after datapacks,
KubeJS, and mod integrations have finished mutating registry/tag/recipe state.
`rehome` recomputes classifier-owned homes for carried items and accessible
claimed chests without moving physical stacks.

Auto-home currently uses:

1. built-in fallback templates from role/material/form/tag signals
2. Triage for unsupported or ambiguous cases

`organization_group` homing is temporarily disabled in the mod while the next
vocabulary refresh is validated. Runtime still loads the facet, reports group
counts in `inspect`, and can use the data for audit/search work, but
`/slot classification rehome` will not materialize `group:*` homes until
`DynamicHomeCohortPolicy.ORGANIZATION_GROUP_HOMING_ENABLED` is restored.

`workflow` is not a wall section by itself. It describes process/task context
for search, task views, and future projections. Section-outcome review applies
to home-producing facets, currently `organization_group`; other
vocabulary-backed facets, including `mod_subsystem`, are judged by semantic
usefulness. `organization_group` remains the intended player-facing home
candidate once re-enabled, but it must stay scarce and broad: mostly item
type/role, with use case or material state only as secondary refinement.
Query-only slices like mod name, rock taxonomy, material form/state, per-tag
variants, and mod subsystem names are search/filter/within-section signals,
not main wall homes.
Stock wood such as sticks, logs, planks, boards, and lumber is a protected
built-in Wood section rather than an LLM-proposed organization group. The
same protection applies to Seeds, Crops, Plants, Clay & Pottery, and Mob Drops:
they are default homes for common stock families, not pack-scoped vocabulary
values unless a future design deliberately changes that.

## Tool Layout

```
tools/classification/
├── layer.schema.json
├── src/
│   ├── extract/          # stage 1 records from mcmeta, sources, jars, runtime exports
│   ├── deterministic/    # stage 2 rule facets
│   ├── evidence/         # pack-level facet-vocabulary evidence
│   ├── llm/              # stage 3 completion, replay, retry, subsystem proposer
│   └── schema/           # facet registry, layer validation, vocabulary validation
└── datasets/
    └── minecraft/        # committed generated outputs
```

Working outputs go under `tools/classification/out/` and are gitignored.
Committed bundled data lives under `tools/classification/datasets/` before it
is synced into `common/src/main/resources/data/slot/classification/`.

## Facet Catalog

The authoritative catalog is
[tools/classification/src/schema/facets.ts](../../../tools/classification/src/schema/facets.ts).
The registry marks each facet as deterministic, LLM-authored, and/or
vocabulary-backed.

Structural facets remain code-closed where runtime code interprets them
directly:

- identity/scope: `mod_namespace`, `is_block_item`, `is_creative_only`
- core kind: `role`, `material_family`, `material_secondary`, `form`, `tier`
- origin/equipment/environment/storage facts such as `origin`,
  `required_tool`, `equip_slot`, `environmental_property`, `storage_categories`,
  `transport_medium`, booleans, and related deterministic signals

Semantic facets that vary by pack are modeled behind generated vocabulary:

- `activity`
- `workflow`
- `workflow_role`
- `used_at`
- `food_category`
- `food_use`
- `preparation_state`
- `material_process_stage`
- `stock_profile`
- `container_state`
- `equipment_effect`
- `protection_context`
- `progression_stage`
- `loadout_context`
- `use_affordance`
- `organization_group`
- `mod_subsystem`

Vocabulary-backed values use stable ids, not display labels:

```text
slot:cooking
slot:mining
create:mechanical_power
pack:tfg2/steelmaking
pack:tfg2/steelmaking#input
```

Labels, aliases, evidence, lifecycle state, and review notes live in the pack
vocabulary artifact, not in the layer value string.

## Layer Entry Shape

Layer files are JSON objects keyed by item id:

```json
{
  "schema_version": 1,
  "layer": "modpack",
  "source": "tfg2",
  "generated_by": "slot-classify v0.1.0",
  "generated_at": "2026-05-11T00:00:00Z",
  "entries": {
    "minecraft:iron_pickaxe": {
      "facets": {
        "role": {
          "value": "tool",
          "confidence": 1,
          "source": "rule:role_from_tags"
        },
        "material_family": {
          "value": "iron",
          "confidence": 1,
          "source": "rule:material_family_from_tag",
          "rationale": "tag minecraft:iron_tool_materials"
        },
        "activity": {
          "values": ["slot:mining", "slot:combat"],
          "mode": "add",
          "confidence": 0.8,
          "source": "llm:stage3",
          "signal": "pattern",
          "rationale": "pickaxe form"
        }
      }
    }
  }
}
```

Important fields:

- `value` is for scalar facets; `values` is for multi-value facets.
- `source` identifies the rule or LLM pass that wrote the value.
- `confidence` is 0-1.
- `signal` caps stage-3 confidence by evidence strength.
- `mode` controls multi-value merge behavior in higher-priority layers.
- `rationale` is a short audit note.

## Current Generation Paths

### Bundled Vanilla And Per-Mod Data

Use this for data shipped inside the SLOT jar:

```sh
cd tools/classification
bun run reclassify:vanilla
bun run reclassify:test-modset
bun run sync:vanilla
bun run sync:test-modset
```

`sync:*` copies generated data into common resources. Both loader jars must
package those resources; if Forge routes most items to generic sections, check
the processed resources and jar contents before assuming Forge tag differences.

### Installed Pack Datapack Layer

For real modpacks, prefer the live export plus static jar enrichment:

```text
/slot classification export tfg2
```

```sh
cd tools/classification
bun run classify:runtime-pack -- \
  --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \
  --summary modpacks/exports/tfg2.runtime-summary.json \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --evidence out/tfg2/tfg2.facet-evidence.json \
  --out out/tfg2 \
  --force
```

The runtime export is the authoritative item/tag/recipe universe. Static jars
fill gaps the runtime export does not currently carry, such as model parents
and loot-table sources. When `--evidence` is supplied, stage 3 also receives a
separate `document_context` field built from conservative guidebook and
advancement evidence linked to actual runtime item ids. Quest evidence remains
vocabulary-only until the adapter splits chapter-level SNBT into local quest
records. The command writes a datapack folder and zip that SLOT loads as a
modpack layer.

### Pack Vocabulary

Facet vocabulary is now part of the stage-3 pack workflow. The tool can collect
pack-level evidence, propose accepted/review/rejected vocabulary, feed accepted
values into stage 3 with `--facet-vocabulary`, attach conservative
`document_context` from the same evidence artifact, reject/downgrade
out-of-vocabulary model output, and validate the final layer against the
accepted vocabulary.

Collect evidence:

```sh
bun run src/cli.ts collect-pack-facet-evidence \
  --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \
  --summary modpacks/exports/tfg2.runtime-summary.json \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --out out/tfg2 \
  --force
```

This writes `out/<pack>.facet-evidence.json` with runtime item facts,
recipe-type summaries, recipe-role summaries, recipe-id families, item/block
tags, mod metadata, guide pages, quest nodes, advancements, and adapter
diagnostics.

Then propose the accepted/review/rejected vocabulary. Run the first full
proposal for a pack without `--previous-vocabulary`; that option is for
refining a nearly satisfactory vocabulary and intentionally biases the
candidate set toward previous accepted values.

```sh
bun run src/cli.ts propose-pack-facet-vocabulary \
  --evidence out/tfg2/tfg2.facet-evidence.json \
  --out out/tfg2 \
  --record-replay \
  --fixture-dir test/fixtures/tfg2-facet-vocabulary \
  --force
```

Use the accepted vocabulary and evidence for the full pack run:

```sh
bun run classify:runtime-pack -- \
  --runtime-export modpacks/exports/tfg2.runtime-items.ndjson \
  --summary modpacks/exports/tfg2.runtime-summary.json \
  --mods /path/to/prism/instance-or-minecraft/mods \
  --evidence out/tfg2/tfg2.facet-evidence.json \
  --facet-vocabulary out/tfg2/tfg2.facet-vocabulary.json \
  --out out/tfg2 \
  --force
```

The next validation step is output quality, not stage-3 plumbing: inspect the
run report, prompt fixtures, warning/review artifacts, and runtime
`/slot classification inspect` results before treating a deep-pack layer as
good enough to ship.

## Runtime Consumers

Current consumers include:

- signal extraction for `IslandSignalDescriptor`
- dynamic organization-group cohorts
- wall-home assignment and `/slot classification rehome`
- `/slot classification inspect` diagnostics
- search/index helpers where already wired

Future consumers should query semantic facets directly instead of inferring task
meaning from wall sections. For example, task views should use `workflow`,
`used_at`, `loadout_context`, `progression_stage`, and `use_affordance`; they
should not treat `organization_group` as a task taxonomy.

## Verification

Tool-level checks:

```sh
cd tools/classification
bunx tsc --noEmit
bun test
```

Runtime checks after installing a datapack layer:

```text
/datapack list enabled
/slot classification status
/slot classification inspect <known_workflow_item>
/slot classification inspect <generic_item_that_should_stay_generic>
/slot classification rehome
```

Packaging checks:

- `common/src/main/resources/data/slot/classification/vanilla-base.json`
- `common/src/main/resources/data/slot/classification/per-mod/index.json`
- processed NeoForge resources
- processed Forge 1.20 resources
- final loader jars
