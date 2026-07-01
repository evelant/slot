# SLOT Current Implementation Plan

Last updated: 2026-07-01

Single-page entry for the active plan + queue. For the operational
handoff (project structure, working rules, verification commands),
see [../status.md](../status.md). For shipped plans, see
[`done/`](done/); for superseded designs, [`retired/`](retired/).

## Active

**[`cross-loader-refactor.md`](cross-loader-refactor.md) — add a
Minecraft 1.20.1 Forge target while keeping the modern 1.21.1
NeoForge + LDLib2 build.** ADR
[`0006`](../decisions/0006-cross-loader-legacy-forge.md) records the
platform decision. Phase 2 remains active: Forge now consumes the common
tree with production adapters, shared action packet/session envelopes,
common workspace projection, carried/world storage accessors, guarded
metadata/transfer/hotbar/workflow/chest/cursor/gather/wayfinding actions, and
the direct Taffy/GuiGraphics `G` screen plus mounted sidebar. The latest
UI parity pass aligned both loaders on right-side workflows, vanilla-shaped
Belt, shared item-card state chrome, accepted-input menus, remembered
search/scroll, three-row floating Recents, compact section headers, and configurable
sidebar/task panel margins. NeoForge remains the semantic oracle; the next risk is
migrating richer modern-only affordances without reintroducing backend-specific
semantics.

Sidecar product slice: EMI recipe context now uses the normal sidebar as a
transient visible-ingredient filter plus one persisted server-owned current
craft run, per
[`0007`](../decisions/0007-emi-recipe-sidebar.md). When EMI's recipe screen is
open, SLOT renders the sidebar into that screen while syncing through EMI's
underlying handled menu; the wall shows visible recipe ingredients, exposes an
`Add Recipe` action for each visible recipe in the right-side task panel,
renders tracked recipes in that panel with compact output-icon headers and
per-recipe stage/adjust/done controls, hides the `Fetch` task lane
while recipes are tracked, raises same-list producer recipe counts to the output
units required by other tracked recipes, and projects recipe inputs as transient
wanted-count pressure so gather/storage/wayfinding match the highlighted chrome.
The tracked recipe list survives logout/rejoin and server restart through workflow persistence. The earlier
recipe-goal plan is retired at [`retired/emi-goal-projections.md`](retired/emi-goal-projections.md),
and the old recipe-goal code/UI/RPC/persistence surface has been removed; do not
grow a recursive goal planner unless playtesting proves transient recipe context
plus craft runs are insufficient.

Previously active
[`single-column-workspace.md`](single-column-workspace.md) is paused
behind the cross-loader work. Do not delete it; resume when the loader
boundary no longer dominates engineering risk.

Sidecar product slice: workflows are now the task surface layered over the
normal wall. `All` remains the global desired/wanted baseline; active workflows and
one-level variants add local desired/wanted targets, implicit member targets,
Belt/offhand pages, accepted inputs, gather targets, accepted-tag substitute
ghosts, and activation-scoped put-away clutter. The code still uses `Kit*` names as the
transitional implementation
substrate, but user-facing work should speak in workflows.

Recently shipped, no further plan: [`done/list-view.md`](done/list-view.md)
and [`done/cursor-pickup.md`](done/cursor-pickup.md). Verified for the current
cross-loader slice: `./gradlew :common:test :neoforge:test :forge-1.20:test
:forge-1.20:compileJava :forge-1.20:compileSharedProbeJava`.

## Recent landings

Thin log; full detail lives in `git log` and the linked archived
plans. Older entries are deleted — `git log` and `done/<plan>.md`
hold the rest.

- **2026-07-01** — Workspace-performance implementation landed: slow-refresh
  timing logs on Forge/NeoForge, a common per-refresh identity context, indexed
  wayfinding matching, layered storage-index caching, remote-storage detail
  intents for search/x-ray versus collapsed refreshes, sliced view-model encode
  reuse, and bounded identity-memo eviction diagnostics. TerraFirmaGreg
  manual/profile validation remains pending in
  [`workspace-performance.md`](workspace-performance.md).

## Known issues

Operational bugs not currently tied to a plan. Items from the
2026-05-01 cursor + desired/wanted-counts batch live under [Queue](#queue)
item 2. No standalone operational bugs are currently tracked here.

## Queue

Roughly ordered by playtest signal. Pull from the top when the active
track lands.

1. **Incremental workspace projection**
   ([workspace-incremental-projection.md](workspace-incremental-projection.md)).
   Normal play profiles show the first workspace-performance pass helped, but
   ordinary carried/storage changes still flow through a mostly whole-model
   projection path. The follow-up plan promotes encoded slices into projection
   slices, introduces typed invalidations with affected identities/storage ids,
   keeps full projection as the oracle/fallback, and aims to make item/storage
   changes update only local cards, sections, chips, wayfinding, and task
   pressure.

2. **EMI craft-run playtest validation.** Validate the transient sidebar +
   right-side task panel against real recipes before adding more chrome:
   open
   recipes from vanilla inventory, chest/crafting/machine screens, and both
   loaders; confirm the sidebar mounts on EMI's recipe screen and returns to
   normal on close; verify add/stage/adjust/remove actions; verify
   duplicate inputs, tag/list ingredients, tracked/proximate storage pips, and
   acquisition-count decrementing; check that staging moves only selected-entry
   deficits into player main inventory; and decide from playtest whether the
   deferred hovered `Use this` concretization/hotkey is actually needed.

3. **Cursor + desired/wanted-counts playtest bug pass — remainder.**
   Active-scope desired counts, player wanted counts, unified gap chrome,
   gather for wanted/desired gaps, and the basic right-click desired-count
   editor are live. Remaining work: dedupe nearby chest identities that are
   both proximate and workflow-needed, fix the non-stackable multi-chest identity
   split (`bucket_of_water` repro), add structured identity/chest/workflow-needed
   diagnostics, finish the deferred cursor-drop/origin-highlight/overflow
   polish, add a workflow-vs-global toggle to "Set desired count...", carry forward
   shift-click-take auto-deposit of excess, and eventually throttle stable
   deposit logs.

4. **Learned-storage residual polish**
   ([learned-storage.md](learned-storage.md)). Sticky cluster
   ordinals across split / merge (today, single-chest churn keeps
   chips stable but multi-chest topology changes can renumber
   labels); per-row "→ suggested home" preview on the loot-chest
   panel; role UX validation against real feeder / machine-buffer
   builds.
5. **Classification LLM-authoring validation**
   ([classification-facet-vocabulary.md](classification-facet-vocabulary.md)).
   The current contract is: gather/format evidence, let the LLM decide
   vocabulary, feed that vocabulary back into later vocabulary rounds, let the
   LLM decide item facets, accept valid output, and treat review flags as
   advisory playtest/debugging signals rather than rejection gates. Runtime
   `organization_group` homing is temporarily disabled so server rehome falls
   back to default sections until the fresh LLM-classified pack layer is
   validated. Before the next full run, clean up the facet registry and
   generation paths against
   [`../design/classification/schema-authoring-rules.md`](../design/classification/schema-authoring-rules.md):
   migrate the audited pack-shaped semantic facets to vocabulary-backed values,
   remove `flavor` and `palette`, keep code-derived semantic inputs advisory
   only, remove stale `confidence` / `signal` support instead of preserving it
   as compatibility metadata, and define a consistent output root/naming
   convention so vanilla baselines, pack vocabulary loops, review artifacts,
   classification layers, datapacks, reports, and replay fixtures have one
   obvious latest location instead of scattered stale directories. Then
   regenerate vanilla/pack vocabulary and run `classify-runtime-pack` with the
   usable vocabulary.
6. **Workflow follow-ups** ([workflow-tabs.md](workflow-tabs.md)).
   Core workflows, accepted inputs, compact nearby headers, hidden Useful Now scoring,
   right-side activation-scoped Put Away guidance, search/keybind polish, and the shared display-storage/tool fix pass are
   live. Put-away destination wayfinding and workflow/variant reorder plus
   duplicate/rename polish have landed. EMI craft runs and recipe-goal removal
   have landed; remaining workflow follow-ups are the deferred hovered `Use this`
   concretization/hotkey if playtesting asks for it, and later Kit-name cleanup
   without changing the current Kit-backed implementation substrate.
7. **Kit prototype historical cleanup** ([kit-prototype.md](kit-prototype.md)).
   The landed Kit code remains the implementation substrate, but future
   user-facing workflow work should follow `workflow-tabs.md`.
8. **Single-column workspace width pass**
   ([single-column-workspace.md](single-column-workspace.md)). Paused
   while the cross-loader/platform boundary is active. Resume once the
   Forge 1.20.1 shared compile gate and UI SPI direction are stable.
9. **Workspace performance validation**
   ([workspace-performance.md](workspace-performance.md)). The implementation
   has landed in common plus both adapters: timing instrumentation, shared
   per-refresh identity indexing, indexed wayfinding, layered storage-index
   caching, remote ghost gating, sliced view-model encoding, and bounded memo
   diagnostics. Remaining work is TerraFirmaGreg profiling plus charged/fluid/
   damageable/provider-backed state checks, not a new semantics shortcut.

## Deferred experiments

Concepts the team has decided to try in isolation rather than as
phases of a larger plan. Each gets a fresh plan in `docs/plans/`
when picked up; do not extend the closed parents.

- **Hide the vanilla 36-slot player-inventory band** in container/
  machine screens (formerly Phase 3b of
  [`done/list-view.md`](done/list-view.md)). The visual reclaim is
  worth playtesting on its own, and the mod-compat surface is broad
  enough to want a focused plan: EMI `+` recipe transfer must keep
  reading vanilla inventory by *slot index* even when those slots
  aren't on screen; sorting / hotkey-move mods that bind to vanilla
  slot positions will need either a transparent shuffle through
  vanilla or explicit fail-closed behavior; hard-custom screens
  (AE2 / RS terminals not extending `AbstractContainerScreen`) need
  a graceful fallback that leaves their layout alone. Two
  techniques the parent plan considered are still the obvious
  starting points: (1) move slot positions off-screen on screen
  open, restore on close; (2) overdraw the inventory band region
  with the sidebar's wall extension. Pick during implementation
  after a coexistence study with EMI on a representative modded
  pack.

## Pointers

For product goals, see [../product/direction.md](../product/direction.md).
For current architecture, see
[../architecture/overview.md](../architecture/overview.md). For
action semantics, [../architecture/action-taxonomy.md](../architecture/action-taxonomy.md).
For the LDLib2 workspace decision,
[../decisions/0002-ldlib2-workspace.md](../decisions/0002-ldlib2-workspace.md).
For the triage / home design (canvas parts superseded by
[`done/list-view.md`](done/list-view.md)),
[../design/atlas.md](../design/atlas.md).
For the carried-inventory fullness UI plan,
[inventory-fullness.md](inventory-fullness.md). For relevance-LOD
history (retired), [../design/relevance-lod.md](../design/relevance-lod.md).
