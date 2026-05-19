# SLOT Current Implementation Plan

Last updated: 2026-05-19

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
metadata/transfer/hotbar/workflow-tab/chest/cursor/gather/wayfinding actions, and
the direct Taffy/GuiGraphics `G` screen plus mounted sidebar. The latest
UI parity pass aligned both loaders on right-side workflow tabs, vanilla-shaped
Belt, shared item-card state chrome, accepted-input menus, remembered
search/scroll, two-row Recents, compact section headers, and configurable
sidebar margins. NeoForge remains the semantic oracle; the next risk is
migrating richer modern-only affordances without reintroducing backend-specific
semantics.

Sidecar product slice: EMI recipe context now uses the normal sidebar as a
transient visible-ingredient filter, per
[`0007`](../decisions/0007-emi-recipe-sidebar.md). When EMI's recipe screen is
open, SLOT renders the sidebar into that screen while syncing through EMI's
underlying handled menu; the wall shows only visible recipe ingredients and
reuses normal carried/storage/missing-card chrome. The earlier recipe-goal plan
is retired at [`retired/emi-goal-projections.md`](retired/emi-goal-projections.md);
do not grow the goal system unless playtesting proves transient recipe context
is insufficient.

Previously active
[`single-column-workspace.md`](single-column-workspace.md) is paused
behind the cross-loader work. Do not delete it; resume when the loader
boundary no longer dominates engineering risk.

Sidecar product slice: workflow tabs are now the task surface layered over the
normal wall. `All` remains the global desired/wanted baseline; active tabs and
one-level variants add local desired/wanted targets, implicit member targets,
Belt/offhand pages, accepted inputs, gather targets, accepted-tag substitute
ghosts, and routed put-away clutter. The code still uses `Kit*` names as the
transitional implementation
substrate, but user-facing work should speak in workflow tabs.

**Recently shipped, no further plan:**

- **[`done/list-view.md`](done/list-view.md) — replace 2D atlas with
  sectioned vertical list.** Closed 2026-05-05 with Phase 3b
  deferred as a separate experiment and the remaining sidebar/mod-compat
  expansion dropped pending playtest signal.
- **[`done/cursor-pickup.md`](done/cursor-pickup.md) — vanilla
  cursor semantics on wall cards.** Closed 2026-05-05 with Phase D
  dropped; eager extract, universal cancel, smart-deposit, and virtual
  cursor retirement are shipped.

Verified for the current cross-loader slice:
`./gradlew :common:test :neoforge:test :forge-1.20:test :forge-1.20:compileJava :forge-1.20:compileSharedProbeJava`.

## Recent landings

Thin log; full detail lives in `git log` and the linked archived
plans. Older entries are deleted — `git log` and `done/<plan>.md`
hold the rest.

- **2026-05-19** — Workflow-tab playtest polish landed: active tabs now reveal
  only intentful accepted-tag proximate substitutes by default while leaving
  unrelated storage ghosts behind `x`/header reveal, accepted-input menus can add
  or remove exact/tag rules with filtered material-specific tags and wider labels,
  section headers carry `+x` nearby counts and compact empty sections, Useful Now
  and Put Away suggestion rows are hidden while their projection/scoring remains
  live, Recents renders two rows, search idle-commits and later clears after close
  with right-click clear working on Forge, the default grave-accent key toggles
  hovered identities between backpack/hotbar and main inventory, and the shared
  target/display-storage fixes stabilized damaged tools, baskets/sacks, tool
  racks, bulk deposit, undo, and TFC startup.
- **2026-05-19** — Item-card chrome now has a shared common grammar:
  `24px` cells, measured 1px-padded top storage pips, route-only notches,
  have/target bottom badges colored by target source, right-side wayfinding or
  `need N`, and one precedence-ordered status ring. NeoForge now renders that
  common tree instead of replacing card chrome, and Forge fixed its Taffy
  inset/padding order plus tiny-text placement/SDF settings so badges no longer
  overlap or render through the wrong edge.
- **2026-05-19** — Junk/trash pressure relief landed: item-card context menus
  can mark/unmark junk or trash carried matching stacks, junk marks expire after
  30 minutes and show a small card indicator, direct trash records undo/redo and
  marks the identity as junk, a configurable unbound hovered-trash hotkey exists
  on both loaders, and pickup routing deletes newly picked junk when carried
  storage is over 75% full before backpack reroute.
- **2026-05-18** — Contextual suggestion scoring now treats pickup/storage-take
  events as context seeds rather than exact Useful Now self-suggestions,
  deduplicates lane cards by identity, suppresses exact-use cards that are
  already visible in quick access/equipment, ignores non-tool item-destroyed
  events as usefulness evidence, caps desired-count excess to two Put Away
  cards, and requires fresh/repeated deposit history when no storage route is
  visible.
- **2026-05-18** — Workflow tabs landed on the existing Kit substrate: `All`
  targets are inherited by active tabs, parent + variant targets compose as
  floors, tab membership creates an implicit wanted-one target, active-tab
  wanted counts clear on deactivation, gather/protection/wayfinding use the
  shared tab resolver, the wall filters to active-tab relevance while keeping
  routed put-away clutter visible, both loaders render visible workflow tabs
  with one-level variants, and card/tab menus can add/remove tab members or
  create variants.
- **2026-05-18** — Workflow-tab cleanup guidance added Put Away projection for
  active-tab-irrelevant carried items, marks items without a learned nearby home
  instead of dropping them silently, preserves routed clutter on normal wall
  cards, and adds an unbound cross-loader put-away hotkey that delegates to the
  same protected deposit command as the button. The rendered row was hidden by
  the 2026-05-19 playtest polish entry above.
- **2026-05-18** — Workflow tabs can now accept exact items or deterministic
  item tags from the card right-click menu without creating wanted/desired
  targets; accepted inputs stay visible in active tabs, are omitted from Put
  Away, persist through workflow state/view-model codecs, and are protected
  from bulk put-away deposit.
- **2026-05-18** — Workflow-tab target cleanup now canonicalizes desired and
  wanted tool/storage-container identities through the shared target resolver,
  uses that resolver for bulk-deposit reservation, keeps damaged carried tools
  and NBT-bearing baskets/sacks from becoming stuck craft targets, limits
  automatic tool-rack deposits to racks with matching visible contents,
  preserves display target ids for deposit undo, keeps active-chest deposit
  fallback behind the shared proximity gate, and syncs TFC display blocks after
  SLOT mutates them.
- **2026-05-17** — Contextual suggestion signal tuning continued: passive
  acquisitions/internal moves no longer train Useful Now, broad station/use
  signatures no longer replay weak associations, place/use/consume events
  avoid exact self-promotion loops, Put Away requires route or deposit evidence,
  nearby storage ghosts reserve Useful Now slots, and `/slot debug contextual`
  dumps event history plus score breakdowns for playtest debugging.
- **2026-05-16** — EMI recipe screens now show the normal SLOT sidebar filtered
  to the visible recipe ingredients on NeoForge and Forge. The projection is
  transient, not a goal: present ingredients keep their normal section/storage
  context, missing ingredients reuse the existing craft-target state, EMI
  remains the recipe explanation surface, and the old recipe-goal plan moved to
  `retired/` with ADR 0007 recording the pivot.
- **2026-05-15** — Contextual suggestion lanes landed as a first playable
  prototype: common signals, bounded aggregates, and learned event associations
  feed Useful Now and Put Away while carried state stays eligibility/action
  state; both loaders render the lanes above the wall with debug score terms.
- **2026-05-14** — Quiet nearby ghosts landed for playtesting: default
  wall sections show carried cards first and collapse ordinary proximate
  storage ghosts behind a per-section nearby chip, while search,
  desired/wanted/workflow-tab/goal intent and storage x-ray toggles reveal the
  hidden storage cards on demand. Follow-up fixes generalized observed
  storage menus beyond vanilla chests for TFC vessels, kept search
  keystrokes inside sidebar search without using Esc as search-clear,
  matched localized hover names in search, disabled affinity decay behind
  the existing kill switch, and added TFC/TFG display storage tracking for
  tool racks and placed items, with deposit limited to tool racks.
- **2026-05-12** — Forge + NeoForge sidebar polish landed: Kits moved
  from the bottom strip into a right-side vertical rack opened from the
  `All` row, the Belt now mirrors vanilla with offhand-left layout,
  active-chest controls hide when no chest is open, search query and wall
  scroll persist across close/open, search clear moved off Esc so closing
  stays consistent,
  carried card chrome no longer uses unexplained dimming, Cloth Config
  margin screens integrate with both mod menus, Forge shift-click keeps
  SLOT deposit/take semantics inside container screens, and Forge sidebar
  rendering isolates depth so host inventory item icons cannot float over
  the Kit panel.
- **2026-05-12** — EMI goal projection playtest stabilization moved goal plans
  and producer recipe defaults into server workflow state, added EMI recipe
  capture for producer choices, reused visible carried/storage authority to
  resolve choice ingredients, omitted empty crafting slots, handled reusable
  tools, restored non-item producer recursion, and added named synthetic fluid
  display fallbacks.
## Known issues

Operational bugs not currently tied to a plan. Items from the
2026-05-01 cursor + desired/wanted-counts batch live under [Queue](#queue)
item 2; this section is the leftover pile.

- **Workflow tab drag-edit doesn't auto-apply to the active belt.** Dragging
  a home onto an *active* tab's slot updates the tab definition
  but the belt isn't re-applied. Per
  [`../design/kits.md § Edit a Kit`](../design/kits.md), the edit
  should propagate immediately when the target page is the active
  page. Scoped follow-up for the next person touching tab
  drag-to-edit.

## Queue

Roughly ordered by playtest signal. Pull from the top when the active
track lands.

1. **EMI recipe sidebar playtest validation.** Validate the new transient
   sidebar filter against real recipes before adding more chrome: open recipes
   from vanilla inventory, chest/crafting/machine screens, and both loaders;
   confirm the sidebar mounts on EMI's recipe screen and returns to normal on
   close; verify duplicate inputs aggregate into the existing missing/craft
   state; verify tag/list ingredients show the visible alternative when one is
   present and a useful missing card otherwise; confirm tracked/proximate
   storage pips survive the projection; and check EMI exclusion bounds/search
   focus do not overlap or trap keys.

2. **Cursor + desired/wanted-counts playtest bug pass — remainder.**
   Active-scope desired counts, player wanted counts, unified gap chrome,
   gather for wanted/desired gaps, and the basic right-click desired-count
   editor are live. Remaining work: dedupe nearby chest identities that are
   both proximate and tab-needed, fix the non-stackable multi-chest identity
   split (`bucket_of_water` repro), add structured identity/chest/tab-needed
   diagnostics, finish the deferred cursor-drop/origin-highlight/overflow
   polish, add a tab-vs-global toggle to "Set desired count...", carry forward
   shift-click-take auto-deposit of excess, and eventually throttle stable
   deposit logs.

3. **Learned-storage residual polish**
   ([learned-storage.md](learned-storage.md)). Sticky cluster
   ordinals across split / merge (today, single-chest churn keeps
   chips stable but multi-chest topology changes can renumber
   labels); per-row "→ suggested home" preview on the loot-chest
   panel; atlas-deposit take-back guard (only revisit if playtest
   shows stuck affinity).
4. **Classification LLM-authoring validation**
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
5. **Workflow tab follow-ups** ([workflow-tabs.md](workflow-tabs.md)).
   Core tabs, accepted inputs, compact nearby headers, hidden noisy suggestion
   rows, search/keybind polish, and the shared display-storage/tool fix pass are
   live. Remaining slices are recipe import/staging into current tabs,
   destination highlighting/wayfinding polish for put-away, reorder UI for
   tabs/variants, tab duplicate/rename polish where the existing context-menu
   editor is too rough.
6. **Kit prototype historical cleanup** ([kit-prototype.md](kit-prototype.md)).
   The landed Kit code remains the implementation substrate, but future
   user-facing task workflow work should follow `workflow-tabs.md`.
7. **Single-column workspace width pass**
   ([single-column-workspace.md](single-column-workspace.md)). Paused
   while the cross-loader/platform boundary is active. Resume once the
   Forge 1.20.1 shared compile gate and UI SPI direction are stable.
8. **Workspace projection caching.** `SlotWorkspaceViewModel`
   re-projects carried / proximate / elsewhere / tab-needed identities
   every server tick while open. Add cache invalidators for inventory
   deltas, chest content, tab changes, and chest-proximity movement;
   the main win is server CPU, with log-spam reduction as a side
   benefit.

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
