# SLOT Current Implementation Plan

Last updated: 2026-05-17

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
metadata/transfer/hotbar/kit/chest/cursor/gather/wayfinding actions, and
the direct Taffy/GuiGraphics `G` screen plus mounted sidebar. The latest
UI parity pass aligned both loaders on right-side Kits, vanilla-shaped
Belt, wanted/desired counts, remembered search/scroll, and configurable
sidebar margins. NeoForge remains the semantic oracle; the next risk is
migrating richer modern-only affordances without reintroducing
backend-specific semantics.

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

- **2026-05-18** — Contextual suggestion scoring now treats pickup/storage-take
  events as context seeds rather than exact Useful Now self-suggestions,
  deduplicates lane cards by identity, suppresses exact-use cards that are
  already visible in quick access/equipment, ignores non-tool item-destroyed
  events as usefulness evidence, caps desired-count excess to two Put Away
  cards, and requires fresh/repeated deposit history when no storage route is
  visible.
- **2026-05-17** — Contextual suggestion signal tuning continued: passive
  authority-diff acquisitions and internal moves no longer train Useful Now,
  block placement no longer double-counts as right-click tool use, placed or
  consumed identities get a short spent penalty instead of exact
  self-promotion, placed/consumed signatures no longer replay old association
  hints, broad station-open/item-use/acquisition/production signatures no
  longer train or replay learned associations, generic carried-only
  SLOT/backpack/inventory hosts, portable menus, and the vanilla player
  inventory menu no longer become station context, low-information
  storage/openable-block right-clicks while a tool is in hand no longer count
  as tool use, targetless/air right-clicks keep exact held-tool relevance
  without adding advisory context, passive offhand right-clicks no longer train
  or score as item use, non-tool meaningful target use can still promote exact
  material interactions while placeable/block-like uses cannot pin themselves,
  weak advisory-only overlap and weak old associations can no longer surface
  carried clutter or nearby storage ghosts, broad advisory terms such as
  generic crafting/material/block/role text no longer pad Useful Now scores,
  Put Away no longer admits ordinary-pressure rare-prior-only carried blocks
  without route or deposit evidence, recent signals decay by world tick,
  qualifying nearby storage ghosts get reserved Useful Now slots, and `/slot
  debug contextual` dumps event history, source counts, association hints, and
  the last closed workspace/sidebar lane score breakdowns for playtest
  debugging.
- **2026-05-16** — EMI recipe screens now show the normal SLOT sidebar filtered
  to the visible recipe ingredients on NeoForge and Forge. The projection is
  transient, not a goal: present ingredients keep their normal section/storage
  context, missing ingredients reuse the existing craft-target state, EMI
  remains the recipe explanation surface, and the old recipe-goal plan moved to
  `retired/` with ADR 0007 recording the pivot.
- **2026-05-15** — Contextual suggestion lanes landed as a first playable
  prototype, then pivoted away from carried-state relevance: common contextual
  signals, bounded item/context aggregates, and a learned event-association
  index persist through workflow schema 9; strong item/station/use/deposit
  events feed Useful Now and Put Away, while carried state is only card
  eligibility/action state. Useful Now can include carried items plus
  suggestion-only nearby storage ghosts, Put Away remains carried-only, both
  loaders render the lanes above the wall with normal card gestures, and debug
  tooltips show history/exact/advisory score terms for tuning in
  [`contextual-suggestions.md`](contextual-suggestions.md).
- **2026-05-14** — Quiet nearby ghosts landed for playtesting: default
  wall sections show carried cards first and collapse ordinary proximate
  storage ghosts behind a per-section nearby chip, while search,
  desired/wanted/Kit/goal intent and storage x-ray toggles reveal the
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
- **2026-05-11** — EMI recipe goal projections landed through Slice 3:
  the common goal/projection model, goal-tab wall projection, browse-only
  goal mode, initial recipe-goal tabs, and explicit EMI recipe-screen /
  drag/drop goal creation/delegation now exist on NeoForge and Forge.
- **2026-05-11** — classification pack-layer work landed installed
  `mods/` scanning, jar extraction, OpenRouter live runs, runtime export,
  datapack generation, dynamic organization-group auto-home cohorts,
  inspect/rehome commands, vocabulary-backed evidence/proposals,
  vocabulary-grounded Stage 3 prompting, and explicit chest-signal
  deposit routing (learned affinity or existing matching contents).

## Known issues

Operational bugs not currently tied to a plan. Items from the
2026-05-01 cursor + desired/wanted-counts batch live under [Queue](#queue)
item 2; this section is the leftover pile.

- **Kit drag-edit doesn't auto-apply to the active belt.** Dragging
  a home onto an *active* kit's slot updates the kit definition
  but the belt isn't re-applied. Per
  [`../design/kits.md § Edit a Kit`](../design/kits.md), the edit
  should propagate immediately when the target page is the active
  page. Scoped follow-up for the next person touching kit
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
   editor are live. These are the remaining items from the original
   2026-05-01 batch; likely best taken as one batch because the chest
   projection issues share diagnosis paths and the logging item unblocks
   faster validation.

   1. **Duplicate chest in proximate panel + chest-locator panel.**
      A nearby chest holding a kit-needed item appears in both
      sections. Decide which surface owns "proximate + kit-needed"
      (chest locator already shows kit-needed identities under
      search; proximate panel shouldn't double up) or render a single
      visual hint that the chest covers both intents.
   2. **Multi-chest / non-stackable identity bug.** Specific repro:
      kit needs `bucket_of_water`, a proximate chest contains one,
      and the atlas ends up with **two** `bucket_of_water` cards —
      one with the desired-count star but no chest-stock pip, one
      with the chest-stock pip but no desired star. Chest locator
      lists two chests for the identity (one proximate, one not).
      Kit progress still says "need 1." Strongly suggests the
      proximate-chest ghost projection produces a parallel identity
      key for non-stackables that doesn't `equals()` the kit-page
      identity. Likely culprits: `ElsewhereGhostProjection`,
      ghost-accumulator merge logic in
      `SlotWorkspaceViewModel.build`, or the chest-locator query.
      Wayfinding's `WayfindingTarget` projection sidesteps this with
      `ItemIdentityMatcher.matchesMovable` end-to-end; the older atlas
      paths still don't.
   3. **More debug logging.** The deposit pipeline got end-to-end
      structured logging on 2026-05-02. The kit-need / chest-presence /
      identity-resolution paths are still sparse, making bugs like the
      multi-chest identity split hard to triage from screenshots alone.
      Add structured INFO/DEBUG at: identity creation per chest enumeration,
      chest-locator query (which identities matched and via which
      equality path), kit-needed projection (input identities +
      carried set + final needed set), proximate vs elsewhere
      classification. Prefer `SlotDiagnostics` / `SlotDebugLog` over
      raw `LOGGER.info` so the pattern stays consistent.

   Cursor / desired/wanted-counts polish that was deferred from the
   2026-05-01 ship and could be folded into this pass if convenient
   (each documented in [`../design/gestures.md`](../design/gestures.md)):

   - Atlas card *drop* (cursor → "send to home").
   - Chest-drop overflow tracking (return-count from RPC).
   - Origin-slot highlight while cursor is non-empty.
   - "Need N more" status text on the desired-count pip — partially
     subsumed by 2026-05-02's unified `M/N` badge with status-coloured
     digits, which already communicates the gap; explicit "need N more"
     text would still be a more direct read.
   - Right-click "Set desired count…" kit-vs-global toggle.
   - **Extend shift+click on take to auto-deposit excess** (carry-
     forward from [`done/cursor-pickup.md`](done/cursor-pickup.md) §
     Follow-up adjacent to this plan). When the player shift+clicks
     (or shift+wheels) to pull from a proximate chest and the
     resulting carried count exceeds their desired-count for the
     identity, auto-deposit the excess to a proximate chest with
     affinity (same as the smart-deposit cascade's step 2 from the
     cursor-pickup plan). Mirrors the cancel path's "satisfy desired
     count then store" rule on the take side. Reuses
     `DepositPlanner` end-to-end; no new domain.

   Wayfinding follow-ups (each minor; defer until playtest signals
   demand):

   - The `[SLOT] deposit ...` log lines fire on every click. Throttle
     once the deposit UX is stable — currently they're useful for
     bug triage but will eventually be log-spam.

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
5. **Workflow tabs** ([workflow-tabs.md](workflow-tabs.md)).
   Replace future Kit Rack work with player-authored tabs: `All`
   desired/wanted counts are the inherited baseline; tabs add local
   targets, implicit wanted-one membership, one-level variants, optional
   Belt pages, gather guidance, put-away guidance, and an adjacent
   overflow/junk pressure relief slice.
6. **Kit prototype historical cleanup** ([kit-prototype.md](kit-prototype.md)).
   The landed Kit code remains the implementation substrate, but future
   user-facing task workflow work should follow `workflow-tabs.md`.
7. **Single-column workspace width pass**
   ([single-column-workspace.md](single-column-workspace.md)). Paused
   while the cross-loader/platform boundary is active. Resume once the
   Forge 1.20.1 shared compile gate and UI SPI direction are stable.
8. **Workspace projection caching.** `SlotWorkspaceViewModel`
   re-projects carried / proximate / elsewhere / kit-needed identities
   every server tick while open. Add cache invalidators for inventory
   deltas, chest content, kit changes, and chest-proximity movement;
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
