# SLOT

SLOT means `Significantly Less Organizational Tedium`.

SLOT is an experimental Minecraft inventory overhaul for modded survival play.
The goal is to reduce inventory-management friction so players can focus on the
game instead of on shuffling stacks between isolated grids.

SLOT is not remote storage, infinite inventory, a logistics network, or
autocrafting. The design target is a better way to browse, understand, and act
on the inventories the player is already carrying or has actually opened.

Current targets: modern Minecraft `1.21.1`, Java `21`, NeoForge + LDLib2;
legacy Minecraft `1.20.1`, Java `17`, Forge `47.x` in progress via the
cross-loader plan. Optional integrations where available — especially EMI,
Tom's Storage, and Sophisticated Backpacks.

## Start Here

New session? Read in this order:

1. This file.
2. [docs/status.md](docs/status.md) — current code state, next work, and
   verification commands.
3. The topic docs below that match the task.

## Documentation Map

Operational:

- [docs/status.md](docs/status.md) — operational handoff: current baseline,
  next work, project structure, verification commands, working rules.

Product:

- [docs/product/direction.md](docs/product/direction.md) — why SLOT exists and
  the current feature direction.
- [docs/product/spec.md](docs/product/spec.md) — normative player-visible
  behavior.

Architecture:

- [docs/architecture/overview.md](docs/architecture/overview.md) — living core
  model: authority, projection, actions, crafting, workflow/activity runtime.
- [docs/architecture/action-taxonomy.md](docs/architecture/action-taxonomy.md)
  — verb + quantity + scope + conflict policy model.
- [docs/architecture/host-ui.md](docs/architecture/host-ui.md) — host/UI layer
  above the kernel; workspace composition and profile model.

Design:

- [docs/design/atlas.md](docs/design/atlas.md) — *superseded by
  [docs/plans/done/list-view.md](docs/plans/done/list-view.md)
  (2026-05-05).* The pan/zoom atlas is gone; the wall is now a
  single-LOD sectioned vertical list. Surviving content (homes,
  ghost vs carried, single-element drag rule, recents,
  kit / desired-count / wayfinding integration) still applies — see
  the doc's own header for which sections are live.
- [docs/design/kits.md](docs/design/kits.md) — task-oriented Kits (unifies
  earlier "collections + loadouts").
- [docs/design/classification/README.md](docs/design/classification/README.md)
  — item classification system overview: data layout, facet catalog, layer
  format, and where the dataset comes from. Powers wall-home suggestions,
  classification diagnostics, runtime export, rehome, and future semantic
  task/search views.

Plans:

- [docs/plans/current.md](docs/plans/current.md) — single-page entry:
  active plan + queue + pointers.
- [docs/plans/cross-loader-refactor.md](docs/plans/cross-loader-refactor.md)
  — active cross-loader plan for 1.21.1 NeoForge + 1.20.1 Forge support.
- [docs/plans/learned-storage.md](docs/plans/learned-storage.md) —
  canonical design ref for the chest-affinity / chip-stack /
  proximate-ghost storage system (shipped 2026-04-30; residual polish
  items listed in `current.md`).
- [docs/plans/kit-prototype.md](docs/plans/kit-prototype.md) — Kit
  prototype slices.
- [docs/plans/classification-database.md](docs/plans/classification-database.md)
  — proposed public classification database, installed-pack scanning, and
  contribution workflow plan.
- [docs/plans/classification-facet-vocabulary.md](docs/plans/classification-facet-vocabulary.md)
  — active LLM-first classification authoring plan: evidence, iterative
  vocabulary, item classification, and datapack/resource assembly.
- [docs/plans/ambient-task-views.md](docs/plans/ambient-task-views.md)
  — deferred/research plan for generic task views such as Cooking,
  Exploration, Mining, Cleanup, and Ore Processing.
- [docs/plans/contextual-suggestions.md](docs/plans/contextual-suggestions.md)
  — planning doc for behavior-scored Useful Now and Put Away suggestion lanes.
- [docs/plans/inventory-fullness.md](docs/plans/inventory-fullness.md)
  — proposed UI slice for surfacing carried-inventory capacity
  without per-bag routing controls. Not yet active.
- [docs/plans/done/](docs/plans/done/) — shipped plans preserved as
  design refs. Currently: `atlas-prototype`, `atlas-navigation`,
  `atlas-nudge-layout`, `core-workflow-ux`, `cursor-pickup`,
  `facet-driven-suggestions`, `list-view`, `list-view-phase-3a`,
  `storage-panel`, `wayfinding`. Add new entries here when a plan
  closes; see `AGENTS.md § Documentation Maintenance § Plan-archive
  checklist`.
- [docs/plans/retired/](docs/plans/retired/) — superseded directions
  (storage-areas: the explicit-named-areas direction; storage-prototype:
  the chest-link / chest-tile / storage-zone prototype that was
  wholesale replaced by learned-storage; emi-goal-projections: the retired
  SLOT-side recipe-goal direction replaced by transient EMI recipe sidebar
  filtering).
- [docs/plans/outdated/](docs/plans/outdated/) — old planning narratives that
  no longer describe current work.

Decisions (ADR-style):

- [docs/decisions/0001-core-rewrite.md](docs/decisions/0001-core-rewrite.md)
  — reasoning behind the core architecture rewrite.
- [docs/decisions/0002-ldlib2-workspace.md](docs/decisions/0002-ldlib2-workspace.md)
  — LDLib2 workspace transport adoption.
- [docs/decisions/0003-atlas-primary-surface.md](docs/decisions/0003-atlas-primary-surface.md)
  — atlas replaces the list-first prototype as the primary surface.
- [docs/decisions/0004-kits-supersede-collections-loadouts.md](docs/decisions/0004-kits-supersede-collections-loadouts.md)
  — Kits unify the earlier collections + loadouts split.
- [docs/decisions/0005-relevance-score-and-layout-locality.md](docs/decisions/0005-relevance-score-and-layout-locality.md)
  — relevance score is a derivation; layout is client-owned; AtlasItem
  drops position/size from the wire.
- [docs/decisions/0006-cross-loader-legacy-forge.md](docs/decisions/0006-cross-loader-legacy-forge.md)
  — SLOT adds a 1.20.1 Forge target through a SLOT-owned UI/platform SPI
  instead of backporting LDLib2.
- [docs/decisions/0007-emi-recipe-sidebar.md](docs/decisions/0007-emi-recipe-sidebar.md)
  — EMI recipe context uses the normal SLOT sidebar as a transient ingredient
  filter instead of growing SLOT-side recipe goals.

Research and assessments:

- [docs/research/ui-ux-brainstorm.md](docs/research/ui-ux-brainstorm.md) —
  exploratory UI/UX problem-space analysis.
- [docs/research/ui-library-assessment.md](docs/research/ui-library-assessment.md)
  — UI framework assessment and the LDLib2 boundary.
- [docs/research/core-inventory-library-assessment.md](docs/research/core-inventory-library-assessment.md)
  — assessment of external inventory primitives and reference mods.
- [docs/research/integration-learnings.md](docs/research/integration-learnings.md)
  — practical lessons from Minecraft, NeoForge, EMI, Tom's Storage,
  Sophisticated Backpacks, and similar mods.

Contributor rules: [AGENTS.md](AGENTS.md).

Content in the `archive/` folder is old prototyping kept only as reference; it
is not part of the current project.

## Design Principles

- One user action should have one authoritative pipeline.
- Screen code renders state and forwards intents; it does not own inventory
  semantics.
- Exact authority and derived projections are different layers and must stay
  separate.
- Inventory authority is the source of truth. Workflow and activity history
  are supporting domain state, not replacement authority.
- Unsupported integration must fail closed.
- Reflection belongs behind narrow compat bridges.
- UI refresh must preserve valid interaction state unless the logical session
  changed.
- Classification authoring is LLM-judgement first. Pre-LLM code gathers and
  formats evidence; it must not pre-decide semantic facet values, constrain the
  model to deterministic guesses, or overwrite valid model output. Vocabulary is
  refined in LLM loops with rich semantic evidence and rotating item samples;
  item classification then uses that vocabulary as grounding input while still
  letting the model make the facet decisions. Review flags are advisory
  debugging/playtest signals, not automatic rejection gates.

## Development

Fast compile checks:

```bash
./gradlew :common:testClasses
./gradlew :neoforge:testClasses
```

Focused test pass:

```bash
./gradlew :common:test --tests 'dev.imagio.slot.*'
./gradlew :neoforge:test
```
