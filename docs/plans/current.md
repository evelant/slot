# SLOT Current Implementation Plan

Last updated: 2026-05-21

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
search/scroll, two-row Recents, compact section headers, and configurable
sidebar margins. NeoForge remains the semantic oracle; the next risk is
migrating richer modern-only affordances without reintroducing backend-specific
semantics.

Sidecar product slice: EMI recipe context now uses the normal sidebar as a
transient visible-ingredient filter plus one persisted server-owned current
craft run, per
[`0007`](../decisions/0007-emi-recipe-sidebar.md). When EMI's recipe screen is
open, SLOT renders the sidebar into that screen while syncing through EMI's
underlying handled menu; the wall shows visible recipe ingredients, exposes an
`Add Recipe` action for each visible recipe, renders tracked recipes as normal
wall-list sections with compact output-icon headers and per-recipe stage/adjust/done
controls, hides the fixed `Fetch` suggestion lane while recipes are tracked,
raises same-list producer recipe counts to the output units required by other
tracked recipes, and
projects recipe inputs as transient wanted-count pressure so
gather/storage/wayfinding match the highlighted chrome. The tracked recipe list
survives logout/rejoin and server restart through workflow persistence. The earlier
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

- **2026-05-21** — Chest affinity roles landed on both loaders: active chests
  cycle through `Storage`, `Buffer`, and `Ignore`; only `Storage` learns
  affinity and accepts quick/bulk deposit, `Buffer` remains visible/pullable,
  `Ignore` is hidden, station deny tags cover TFC forge/crucible/alloying
  cases, item menus can clear one active-chest affinity bond, and
  move-to-new-storage rehomes clear the emptied origin bond. ADR
  [`0008`](../decisions/0008-chest-roles-and-affinity-correction.md) records it.
- **2026-05-21** — EMI craft runs landed on both loaders: recipe screens still
  mount the filtered SLOT sidebar, can add visible or hovered EMI recipes to a
  persisted server-owned recipe list, show per-recipe sections directly in the
  wall list, project recipe inputs as transient wanted-count pressure for shared
  gather/storage/wayfinding behavior, clamp count controls to recipe output
  batches, raise same-list producer recipe counts from downstream input needs,
  stage selected recipe inputs from carried providers into player main inventory
  through the shared transfer executor, decrement remaining output from
  meaningful acquisition activity, survive logout/rejoin and server restart,
  and no longer expose the legacy `SLOT goal` button/drop target/goal-tab UI or
  recipe-goal model, RPC, codec, and persistence fields.
- **2026-05-20** — Active-workflow put-away destination wayfinding landed:
  activation-time carried clutter now feeds a visible Put Away strip, no-home
  card chrome, distinct chest/display wayfinding targets, green HUD/glow
  styling and "Put away" labels, and bulk deposit ignores later pickups until
  the workflow is activated again.
- **2026-05-20** — Workflow editing polish landed: workflow and variant
  context menus can move siblings left/right on both loaders, the shared
  workflow event stream persists sibling reorder events, duplicates stay beside
  the source family with readable copy names, and sibling rename collisions are
  rejected instead of creating ambiguous visible workflow names.
- **2026-05-19** — Workflow playtest polish landed: active workflows now keep
  all carried cards visible, reveal only intentful accepted-tag proximate
  substitutes by default, and leave unrelated storage ghosts behind `x`/header
  reveal; accepted-input menus can add or remove exact/tag rules with filtered
  material-specific tags and wider labels,
  section headers carry `+x` nearby counts and compact empty sections, Useful Now
  suggestion rows are hidden while live contextual observation, expensive
  contextual scoring, and storage-ghost expansion are disabled for now,
  Recents renders two rows, search
  idle-commits and later clears after close
  with right-click clear working on Forge, grave accent moves hovered identities
  to main inventory, Shift+grave moves hovered identities to backpack storage,
  and the shared
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
  on both loaders, and pickup routing trashes marked junk stacks before/after
  pickup when effective carried storage is over half full before backpack
  reroute, with pressure reads cached against shared carried-inventory revision
  signals and known specialist Sacks n' Such containers excluded from general
  pressure.
- **2026-05-18** — Contextual suggestion scoring now treats pickup/storage-take
  events as context seeds rather than exact Useful Now self-suggestions,
  deduplicates lane cards by identity, suppresses exact-use cards that are
  already visible in quick access/equipment, ignores non-tool item-destroyed
  events as usefulness evidence, caps desired-count excess to two Put Away
  cards, and requires fresh/repeated deposit history when no storage route is
  visible.
- **2026-05-18** — Workflows landed on the existing Kit substrate: `All`
  targets are inherited by active workflows, parent + variant targets compose as
  floors, workflow membership creates an implicit wanted-one target, active-workflow
  wanted counts clear on deactivation, gather/protection/wayfinding use the
  shared workflow resolver, the wall keeps carried cards and filters non-carried
  cards to active-workflow relevance while keeping routed put-away clutter visible,
  both loaders render visible workflows
  with one-level variants, and card/workflow menus can add/remove workflow members or
  create variants.
- **2026-05-18** — Workflow cleanup guidance added Put Away projection for
  active-workflow-irrelevant carried items, marks items without a learned nearby home
  instead of dropping them silently, preserves routed clutter on normal wall
  cards, and adds an unbound cross-loader put-away hotkey that delegates to the
  same protected deposit command as the button. The rendered row later became
  visible once guidance was scoped to activation-time clutter.
- **2026-05-18** — Workflows can now accept exact items or deterministic
  item tags from the card right-click menu without creating wanted/desired
  targets; accepted inputs stay visible in active workflows, are omitted from Put
  Away, persist through workflow state/view-model codecs, and are protected
  from bulk put-away deposit.
- **2026-05-18** — Workflow target cleanup now canonicalizes desired and
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
## Known issues

Operational bugs not currently tied to a plan. Items from the
2026-05-01 cursor + desired/wanted-counts batch live under [Queue](#queue)
item 2. No standalone operational bugs are currently tracked here.

## Queue

Roughly ordered by playtest signal. Pull from the top when the active
track lands.

1. **EMI craft-run playtest validation.** Validate the transient sidebar +
   persisted tracked-recipe list against real recipes before adding more chrome:
   open
   recipes from vanilla inventory, chest/crafting/machine screens, and both
   loaders; confirm the sidebar mounts on EMI's recipe screen and returns to
   normal on close; verify add/stage/adjust/remove actions; verify
   duplicate inputs, tag/list ingredients, tracked/proximate storage pips, and
   acquisition-count decrementing; check that staging moves only selected-entry
   deficits into player main inventory; and decide from playtest whether the
   deferred hovered `Use this` concretization/hotkey is actually needed.

2. **Cursor + desired/wanted-counts playtest bug pass — remainder.**
   Active-scope desired counts, player wanted counts, unified gap chrome,
   gather for wanted/desired gaps, and the basic right-click desired-count
   editor are live. Remaining work: dedupe nearby chest identities that are
   both proximate and workflow-needed, fix the non-stackable multi-chest identity
   split (`bucket_of_water` repro), add structured identity/chest/workflow-needed
   diagnostics, finish the deferred cursor-drop/origin-highlight/overflow
   polish, add a workflow-vs-global toggle to "Set desired count...", carry forward
   shift-click-take auto-deposit of excess, and eventually throttle stable
   deposit logs.

3. **Learned-storage residual polish**
   ([learned-storage.md](learned-storage.md)). Sticky cluster
   ordinals across split / merge (today, single-chest churn keeps
   chips stable but multi-chest topology changes can renumber
   labels); per-row "→ suggested home" preview on the loot-chest
   panel; role UX validation against real feeder / machine-buffer
   builds.
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
5. **Workflow follow-ups** ([workflow-tabs.md](workflow-tabs.md)).
   Core workflows, accepted inputs, compact nearby headers, hidden Useful Now scoring,
   visible activation-scoped Put Away guidance, search/keybind polish, and the shared display-storage/tool fix pass are
   live. Put-away destination wayfinding and workflow/variant reorder plus
   duplicate/rename polish have landed. EMI craft runs and recipe-goal removal
   have landed; remaining workflow follow-ups are the deferred hovered `Use this`
   concretization/hotkey if playtesting asks for it, and later Kit-name cleanup
   without changing the current Kit-backed implementation substrate.
6. **Kit prototype historical cleanup** ([kit-prototype.md](kit-prototype.md)).
   The landed Kit code remains the implementation substrate, but future
   user-facing workflow work should follow `workflow-tabs.md`.
7. **Single-column workspace width pass**
   ([single-column-workspace.md](single-column-workspace.md)). Paused
   while the cross-loader/platform boundary is active. Resume once the
   Forge 1.20.1 shared compile gate and UI SPI direction are stable.
8. **Workspace projection caching.** `SlotWorkspaceViewModel`
   re-projects carried / proximate / elsewhere / workflow-needed identities
   every server tick while open. Add cache invalidators for inventory
   deltas, chest content, workflow changes, and chest-proximity movement;
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
