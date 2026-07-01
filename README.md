# SLOT

SLOT means `Significantly Less Organizational Tedium`.

An experimental minecraft inventory overhaul. It reduces tedious tasks without cheating. 
The goal is to let the player spend more time playing the game and less time shuffling things
around in various inventories, especially in big complex modpacks.

Current targets: 1.20.1 forge. Also contains code for 1.21.1 neoforge but that has not been tested recently since I've been playing TerraFirmaGreg on 1.20.1.

# Disclaimer

I made this for my own personal use/enjoyment. It is rough. It is unfinished. I designed and architected everything, codex wrote the code, so if you don't want to read it or dislike AI you don't have to use this. Likely unsuitable for anything but personal or tiny servers. Use at your own risk.


That being said, I am daily driving this on my own TerraFirmaGreg server and it works great for me.

# Screenshot

![screenshot](https://github.com/evelant/slot/blob/main/image.png?raw=true)


# Features

In no particular order

1. Track inventory in all your chests, search them, see how many you have
2. Show all of your carried inventories (incl sophisticated backpacks) and treat them as one seamless container
3. Automatically categorizes all carried items in a list, customizable cateogories, and helpful indicator borders/corners/counts to see at a glance what you have or don't have and how much
4. Can show items in the list as "ghosts" if you're not carrying them but have them stored somewhere, along with guidance to where they're stored
5. Remembers where you put items even if removed so they later get put back in the same place
6. EMI integration
   1. Add recipes to the sidebar
   2. Slot counts the total amounts needed and helps you find them in chests. 
   3. Counts against ALL stored and carried inventory to figure out how many you need to craft or collect
   4. Press r or u while hovering an item to bring up EMI for it, great for adding more recipes for sub-ingredients to your crafting plan
7. Sophisticated backpacks integration - automatically puts picked up items in backpack before main inventory for convenience
8. Chest finder -- when you need an item a hud arrow points to where the chest is and the chest is outlined in world
9.  Auto-take -- when you need items press one button to fetch any of the items from nearby chests. No long distance teleportation, just removes the need to spend time manually picking through chests.
10. Auto-deposit -- press one button to deposit everything you're carrying into nearby chests but _only if it already lives there_ as a remembered home.
11. Desired vs Wanted counts -- set a count of an item to always keep on hand, or a temporary count to show the chest finder for the item until you go pick it up
12. Shortcuts
    1.  Shift+rightclick to take/put a stack in a nearby storage (if item lives there)
    2.  Shift+scroll to take/put one at a time nearby storage
    3.  Leftclick - pickup to cursor, drop on a different section of the list to move it there
    4.  ` (grave/tilde) - move hovered item from backpack to main inventory
    5.  tab - move hovered item to hotbar, evicting least recently used item back to inventory
    6.  ctrl+scroll - set desired (always carry) count on hovered item
    7.  alt+scroll - set wanted count on hovered item (show as ghost in inventory with guidance to chest until you pick up that amount)
13. Workflows and loadouts (rough/wip) -- define different sets of items so you can easily swap between different tasks that require different tools and equipment
14. Recents list -- shows the last 24 item types you picked up or put down
15. Trash items -- instantly delete all of a particular item type you're carrying (good for junk)
16. Mark as junk -- If inventory is above 75% full, items marked as junk will be voided when picked up
17. Undo/Redo (buggy/rough) -- z to undo last action (incl trash), y to redo. Currently buggy, but at least works to undo accidental trashing.
18. Mod settings page for configuring some UI offsets so that it doesn't get in the way of other mods (particularly EMI)
19. Other stuff I probably forgot, it does a lot!

# Compiling and using

1. Have JDK installed
2. Clone repo
3. `./gradlew build` in slot directory
4. Grab the jar you need, either forge-1.20/build/libs/slot-forge-1.20.1....jar or neoforge/build/libs/....jar for 1.21

# Likely outdated LLM docs and design brainstorms below

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
- [docs/plans/workflow-tabs.md](docs/plans/workflow-tabs.md) —
  plan/reference for the workflow-tab task surface: player-authored tabs
  that reuse desired/wanted counts, gather guidance, put-away guidance,
  and the existing hotbar page code.
- [docs/plans/kit-prototype.md](docs/plans/kit-prototype.md) — Kit
  prototype slices; future user-facing task workflow work should follow
  `workflow-tabs.md`.
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
- [docs/plans/workspace-performance.md](docs/plans/workspace-performance.md)
  — late-game storage-heavy performance implementation plan and pending
  TerraFirmaGreg profile checklist for projection timing, identity reuse,
  wayfinding/storage indexing, remote ghost gating, and sliced view-model sends.
- [docs/plans/workspace-incremental-projection.md](docs/plans/workspace-incremental-projection.md)
  — follow-up plan for replacing whole-model workspace refreshes with typed
  invalidations, projection slices, local identity/storage facts, and delta
  view sends.
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
	  filtering plus craft runs).
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
  — EMI recipe context uses the normal SLOT sidebar plus transient craft runs
  instead of growing SLOT-side recipe goals.
- [docs/decisions/0008-chest-roles-and-affinity-correction.md](docs/decisions/0008-chest-roles-and-affinity-correction.md)
  — chest roles gate learned storage affinity and quick-deposit participation.

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
