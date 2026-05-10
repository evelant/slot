# Classification System

How SLOT understands what items *are* — their role, materials, recipes,
behaviour, gameplay context — so the atlas can place them, the UI can highlight
them, and the player can search them by meaning rather than by id.

This document is the **landing page** for the system. It covers what the data
looks like, where it lives, and how the runtime consumes it. Read it before
diving into the planning document — that one is comprehensive but long.

| Want to… | Go to |
| --- | --- |
| Get the concise tool overview and usage guide | [tool-guide.md](tool-guide.md) |
| Understand the design and tradeoffs | [../../plans/item-classification.md](../../plans/item-classification.md) |
| Understand the public database / pack UX plan | [../../plans/classification-database.md](../../plans/classification-database.md) |
| Read the formal facet-kind type system | [facet-kinds.md](facet-kinds.md) |
| Understand schema evolution / versioning | [schema-changelog.md](schema-changelog.md) |
| Run the pipeline / regenerate data | [tools/classification/README.md](../../../tools/classification/README.md) |
| Validate a layer file | `bun run tools/classification/src/cli.ts validate <path>` |

## What problem this solves

The atlas wants to put `acacia_planks` next to `acacia_log` and far from
`iron_ingot`. The previous solution was [`SemanticBucketResolver`](../../../common/src/main/java/dev/imagio/slot/debug/SemanticBucketResolver.java) —
a hand-coded ladder of substring matches and tag checks (`*_planks` → wood,
`*_ingot` → metal, etc.). It hits roughly 40% accuracy in modded packs and
fails silently when it gets things wrong, which trains the player not to
trust the atlas. See the [original motivation in the planning doc](../../plans/item-classification.md#purpose).

The classification system replaces that guesswork with a structured per-item
record:

- **Deterministic facets** derived from Minecraft data — tags, recipes, loot
  tables, components — by stage-2 rules that a human can read and audit.
- **LLM-authored facets** filling judgement-call gaps (role, flavor, palette,
  primary uses) by a single pass per generation, baked into a JSON file the
  runtime loads — no LLM at runtime, fully deterministic at play time.
- **Layered overrides** so mods, modpacks, servers, and individual players
  can extend or correct the data without forking it.

## Where the data lives

```
tools/classification/
├── layer.schema.json                          # canonical wire format
├── src/
│   ├── extract/                               # stage 1: extract from sources/jars/runtime exports
│   ├── deterministic/                         # stage 2: rule-based facet derivation
│   ├── llm/                                   # stage 3: LLM completion / vocabulary hints
│   └── schema/facets.ts                       # source of truth for the facet catalog
└── datasets/
    └── minecraft/
        ├── minecraft.facets.complete.json     # the layer the runtime loads
        ├── minecraft.facets.corrections.json  # audit trail (LLM-flagged stage-2 errors)
        └── minecraft.facets.schema-proposals.json  # audit trail (schema gaps)
```

`datasets/<source>/` is the curated public artifact. `out/` is a gitignored
working dir. For modpacks, `classify-runtime-pack --runtime-export ... --mods ...`
emits a pack-specific datapack folder and zip containing
`data/slot/classification/layers/<pack>.json`.

At runtime SLOT loads bundled vanilla/per-mod resources, then datapack-provided
classification layers. A generated pack layer can therefore be dropped into an
instance without rebuilding the SLOT jar. Both Forge 1.20.1 and NeoForge
1.21.1 expose `/slot classification inspect`, `export`, and
`rehome` / `recompute` for diagnostics, running-instance exports, and
bulk auto-home testing.

## The facet catalog at a glance

Schema lives at [tools/classification/src/schema/facets.ts](../../../tools/classification/src/schema/facets.ts)
and is the only authoritative list. As of v1 (in-flight) there are **38
facets**, grouped by purpose:

**Identity / scope (4):**
`mod_namespace`, `is_block_item`, `is_creative_only`, `mod_subsystem`

**What it is (4):**
`role` (mechanism / building_block / tool / weapon / armor / food / utility / …),
`material_family` + `material_secondary` (iron, wood_oak, certus_quartz, …),
`form` (ingot, plank, stairs, log, …), `tier` (wood / stone / iron / diamond / …)

**What you do with it / where a player stores it (4):**
`activity` (mining, combat, redstone, …), `primary_uses` (LLM-authored
short verbs: "smelting fuel", "building structure", …), `processing_in`
(crafting, smelting, blast_furnace, smithing, …), `organization_group`
(player-facing storage/workflow groups such as `tfc:casting`,
`tfc:masonry`, `tfc:leatherworking`)

**Where it comes from (3):**
`origin` (overworld_surface, nether, mob_drop, brewing, crafted_only, …),
`y_level_range` (sky / surface / underground / deep / nether_surface / end_islands),
`required_tool` + `required_tool_tier`

**Combat / equip (3):**
`equip_slot` (head / chest / legs / feet / mainhand / offhand),
`combat_bonus` (undead, illager, fall_bonus_damage, disables_blocking, …),
multi-value damage modifiers

**Aesthetics (3):**
`flavor` (plain / variant / fancy / ominous / ancient / mystical / mechanical / natural / colored),
`palette` (multi-value, per-color),
`dye_color` (16 vanilla dyes when explicitly dyed)

**World physics (1):**
`environmental_property` — fireproof, slippery, gravity_affected,
piglin_loved, sustains_fire, climbable, trample_sensitive,
freeze_immune_when_worn, … (28 values)

**Worth + frequency (2):**
`rarity` (abundant / common / uncommon / rare / unique),
`frequency` (how often a player sees it — independent from rarity)

**Storage / IO (2):**
`storage_categories` (multi: tool / resource / food / consumable / …),
`transport_medium` (item / fluid / gas / energy / signal / player / mob)

**Multiblock / interaction (4):**
`multiblock_role`, `multiblock_component_of`, `spawn_interaction`,
`produces_effect`

**Boolean signals (7):**
`is_stackable`, `is_fuel`, `has_durability`, `has_enchantments`,
`has_nbt_variation`, plus the identity flags listed above

Of these, **21 are LLM-authored** and **15 are rule-derived only**.

## Reading a layer entry

A layer file is a JSON object keyed by item id. Each entry holds a `facets`
sub-object keyed by facet id:

```json
{
  "schema_version": 1,
  "layer": "vanilla-base",
  "source": "minecraft",
  "generated_by": "slot-classify v0.1.0",
  "generated_at": "2026-04-25T...",
  "entries": {
    "minecraft:iron_pickaxe": {
      "facets": {
        "role":            { "value": "tool",        "confidence": 1,    "source": "rule:role_from_tags" },
        "material_family": { "value": "iron",        "confidence": 1,    "source": "rule:material_family_from_tag",
                             "rationale": "tag minecraft:iron_tool_materials" },
        "tier":            { "value": "iron",        "confidence": 0.95, "source": "llm:stage3", "signal": "named",
                             "rationale": "id prefix is canonical tool tier" },
        "activity":        { "values": ["mining", "combat"], "mode": "add",
                             "confidence": 0.8,      "source": "llm:stage3", "signal": "pattern",
                             "rationale": "pickaxe form" },
        "required_tool":   { "value": "pickaxe",     "confidence": 1,    "source": "rule:required_tool_from_block_tag" }
      }
    }
  }
}
```

Notable fields:

- **`value` vs `values`** — single-value facets use `value`; multi-value
  facets use `values` (array).
- **`source`** — `rule:<rule_id>` for stage 2, `llm:stage3` (or
  `llm:stage3-retry`) for stage 3, `rule:*_id_override` /
  `rule:*_hardcoded_override` for the per-item correction maps.
- **`confidence`** — 0–1, with stage 3 confidence anchored to a `signal`
  band (`named` 0.95, `pattern` 0.80, `inferred` 0.60, `guess` 0.30).
  Overconfidence on a weak signal is silently demoted.
- **`mode`** — only on multi-value facets in higher-priority layers,
  defaults to `add` (union with lower layers); also `replace` and `remove`.
  Single-value defaults to `replace`.
- **`rationale`** — ≤40 char human-readable note; useful for debugging the
  audit trail.

The full wire schema is [tools/classification/layer.schema.json](../../../tools/classification/layer.schema.json).

## How layers stack

Six layers, lowest → highest priority:

1. `vanilla-base` — what the offline pipeline produces for vanilla.
2. `per-mod` — same, per modded namespace.
3. `runtime-crawl` — synthesized at world load by walking live registries.
   Catches datapacks, KubeJS, cross-mod tag closure, unknown mods.
   **Deterministic only — no LLM facets at runtime.** *(milestone 8)*
4. `modpack` — pack maintainer's overrides.
5. `server` — operator-side overrides.
6. `player` — per-player island assignments + corrections. Always wins.
   *(milestone 9)*

`FacetIndex` merges them at init under the rules in
[../../plans/item-classification.md § Layering & merging](../../plans/item-classification.md#layering--merging).
Bundled vanilla/per-mod resources and datapack-provided modpack layers are
implemented. Runtime-crawl and persistent server/player facet layers remain
future work; player visual homes are still workflow-domain state rather than
serialized facet entries.

## Pipeline (offline)

1. **Stage 1 — Extract**: walk the mod's source tree (or mcmeta's `summary`
   branch for vanilla), produce one NDJSON record per item with tags,
   recipe role, loot sources, model chain, components.
   Running Forge 1.20.1 and NeoForge 1.21.1 instances can also write
   live stage-1-compatible records with `/slot classification export`;
   this path is for KubeJS/datapack-heavy packs where static jar data is
   not the final loaded state. Runtime tags are resolved live membership;
   direct tag provenance remains a static-resource extractor feature.
2. **Stage 2 — Deterministic rules**: rule-based facet derivation. Each
   rule is a TS function in [tools/classification/src/deterministic/rules/](../../../tools/classification/src/deterministic/rules/).
3. **Stage 3 — LLM completion**: the current default backend is OpenRouter
   with `deepseek/deepseek-v4-flash`; `claude-cli` remains a fallback
   backend. The model fills judgement-call facets (role, flavor, palette,
   primary_uses, …). Per-mod and runtime-export runs first do a
   `mod_subsystem` proposer pre-pass that pins a canonical vocabulary into
   the system prompt so item batches stop inventing synonyms. The LLM also
   authors `organization_group`, the direct "where would a player put this?"
   signal used to split broad roles like Materials and Utility in large packs.

Stages 4 (nearest-neighbor priming) and 5 (compile) are spec'd in the plan
but deferred — the stage-3 output is already a valid layer and gets copied
directly into `datasets/`.

Run the pipeline:

```sh
cd tools/classification
bun run src/cli.ts classify --mod minecraft --source ../mcmeta/.worktrees/summary
# Stage 3 is opt-in:
bun run src/cli.ts classify --mod minecraft --source ../mcmeta/.worktrees/summary \
    --stages 1,2,3 --model sonnet --concurrency 4 --batch-size 5 \
    --record-replay --fixture-dir test/fixtures/vanilla-full-v1
```

LLM calls are recorded as fixtures, so resuming a run with the same
`--fixture-dir` replays them cache-first and only the new prompts hit the
API. See the tool [README](../../../tools/classification/README.md) for full
CLI surface.

## Runtime

`dev.imagio.slot.classification.FacetIndex` in `common/`:

- **Init** — loads bundled vanilla/per-mod resources plus datapack
  `data/slot/classification/layers/*.json`, validates schema/layer names,
  merges, and exposes a load report for `/slot classification inspect`.
- **Per-item lookup** — exposes role/material/form/subsystem/organization
  group/activity and adjacent facet helpers used by signal extraction and
  auto-home.
- **Dynamic homes** — auto-home uses count-qualified `organization_group`
  sections first, then `mod_subsystem` sections, then built-in templates. A
  generic section wins when the dataset omits a stronger player-facing group.
- **Bulk recompute** — `/slot classification rehome` scans every carried
  source plus currently accessible claimed chests, then recomputes
  classifier-owned homes without moving physical items.
- **Queries** — inverted-index `index.where(Expr)` API arrives when a real
  use-site needs it. Skipped for V1.

The integration sequence is in
[../../plans/item-classification.md § Integration sequence](../../plans/item-classification.md#integration-sequence-next-concrete-work).

## What the dataset is good for

Beyond just homing items into islands, the layer enables:

- **Smart search** — "show me all the iron tools," "all redstone components,"
  "everything that drops in the End." Currently `FacetIndex.where(role:tool ∧
  material_family:iron)` once the query API lands.
- **Recipe-aware highlighting** — pull in `processing_in` to show "this fits
  in a smithing table."
- **Theme detection** — when a player puts five oak items in an island,
  notice they all share `material_family: wood_oak` and offer auto-include.
- **Loadout suggestions** — pre-built kits keyed off `activity` or
  `primary_uses`.
- **Clutter review** — items the player owns but never uses (cross-reference
  with player activity tracking).

These all become small features once `FacetIndex` is in place; building any
one of them today against `SemanticBucketResolver` would mean rewriting the
classifier. That's the whole reason for the layer.
