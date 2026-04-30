# Core Workflow UX Plan

Last updated: 2026-04-20

Status: **all six slices landed**, plus playtest follow-ups (inspector
panel removed, chest cell click+drag-extract, storage zone group drag
header, plus several drag/event-priority bug fixes). Per-slice landing
notes live in [../status.md](../status.md) under "Core-workflow UX
landing points." This plan stays here as the historical spec; the live
state is in `status.md`.

Originally framed as an interrupt to the partial Kit prototype. With
the UX pass complete, the next focus is resuming Kit prototype at
slice 4.

For the atlas concept, see [../design/atlas.md](../design/atlas.md). For the
storage concept this builds on, see [../design/storage.md](../design/storage.md).
For the broader near-term sequence and the Kit work this temporarily
supersedes, see [current.md](../current.md). For the operational baseline, see
[../status.md](../status.md).

## Goals And Non-Goals

Goals:

- close the symmetry between hotbar and atlas so the hotbar feels like part
  of the atlas, not a separate surface
- make proximate linked chests fully interactive from the atlas (drop in,
  shift-take out, shift-deposit by identity) without forcing the player into
  the vanilla container UI
- give the player one obvious way to see, at a glance, where any carried
  item belongs ("home") and where any homed item currently is (hotbar slot,
  linked chest)
- replace the Triage island with a docked panel so it stops fighting pan/zoom
  and stops competing with player-authored islands for atlas geography
- introduce a right-click context menu on homes so the new gestures are
  discoverable instead of hidden behind keyboard knowledge
- keep every gesture aligned with existing rules: visual-home changes stay
  separate from real inventory mutations, real mutations route through the
  intent router, no client-authored authority

Non-goals:

- drag-from-hotbar-to-island re-home (rejected: re-homing must be
  intentional and infrequent — see
  [../decisions](../decisions/) for the broader rule)
- Triage entry pulse / new-item indicator (deferred to the future
  `+N since last open` system tracked in
  [current.md](../current.md))
- drag-to-trash surface (deferred — needs its own design pass; see
  [current.md](../current.md) "Later Feature Tracks")
- bulk multi-select on homes, hotbar, or chest contents
- per-island desired-carry, slot pins, or per-chest layout memory
- non-proximate chest interaction (still fail-closed)
- context-menu items beyond the four landed in slice 6 (no Trash, no Pin,
  no Lock until those features exist behind them)
- visual polish; greybox styling is fine until the interaction model lands

## Prerequisites

What must already be landed before each slice can start:

- **all slices** depend on the current carried-atlas, home assignment,
  and chest-claim/link/deposit/take-all paths landed under
  [atlas-prototype.md](atlas-prototype.md) and
  [storage-prototype.md](../retired/storage-prototype.md). All present as of
  2026-04-19.
- **slice 1 (Triage panel)** depends on nothing else in this plan. It
  reshapes a chunk of `SlotWorkspaceUiFactory` and the view model, so it
  goes first to avoid layout-rebase churn on the later slices.
- **slice 4 (chest drag/take)** reuses `DepositExecutor` and
  `TakeAllExecutor`; both exist.
- **slice 5 (shift-deposit by identity)** depends on slice 4's
  single-stack deposit path being factored out of the bulk Deposit flow.
- **slice 6 (right-click menu)** depends on slices 2, 4, and 5 so the
  menu's first three commands actually exist as RPCs.

The partial Kit prototype (slices 4–9 in
[kit-prototype.md](../kit-prototype.md)) pauses for the duration of this plan.
Nothing in this plan touches Kit domain state; resuming after slice 6 lands
should be a clean continuation.

## Risk Register

### 1. Triage Panel Geometry Fights Existing Chrome

The atlas chrome already pins the Belt to the camera and overlays a Kit
Rack above it (Kit Slice 3). A new docked Triage panel adds a third
camera-fixed surface. If any two overlap or steal pointer events, drag
gestures and proximity affordances break.

Mitigation:

- pick one fixed edge for Triage (left side recommended — Belt is bottom,
  Kit Rack pops from Belt) and keep it there for the whole prototype
- treat the panel as part of the workspace overlay layer that already hosts
  the Belt, not as an LDLib2 `GraphView` content child; pan/zoom must not
  move it
- panel rows must be drop *targets* and drag *sources* with explicit
  hit-testing, not invisible click-through chrome

### 2. Shift+Click Conflicts With Vanilla Click Semantics

Vanilla Minecraft uses shift+click to quick-move stacks. The atlas
workspace is a custom screen and does not currently route shift+click
anywhere meaningful, but the player's muscle memory may still expect
"shift+click moves things in some direction." We're claiming shift+click
for atlas-specific semantics that depend on *what* you click, not on which
lane you're in.

Mitigation:

- pin the semantics in this doc and in
  [../architecture/host-ui.md](../architecture/host-ui.md): shift+click
  acts on the *stack at the click target*, sending it to its canonical
  partner surface (hotbar ↔ home; home ↔ linked chest; chest contents →
  carried)
- never use shift+click to *move a home* — that always stays a deliberate
  drag of the canonical atlas card or a context-menu command
- diagnostics surface "no home / no linked proximate chest / no free
  hotbar slot" rather than failing silently

### 3. Drag Hit-Test Ambiguity

A dragged atlas card may now plausibly land on: an island, an empty atlas
region (creates an island today), a proximate chest tile (slice 4), or
the docked Triage panel (slice 1). LDLib2's hit-testing is per-element;
overlapping drop targets need a deterministic z-order.

Mitigation:

- explicit target priority, highest first: docked Triage panel (overlay
  layer) → proximate chest tile → island → empty atlas → background
- only proximate chests register as drop targets; non-proximate chests
  render but ignore drops (matches deposit/take button gating)
- visual feedback while dragging: highlight the topmost matching target
  only, never two at once

### 4. Per-Stack Deposit Tiebreakers

If a home's island is linked to multiple proximate chests with available
space, "shift-deposit this stack" needs a deterministic choice or it'll
feel arbitrary.

Mitigation:

- tiebreaker order: chest with the smallest free space that still fits the
  whole stack (pack tightly), then chest with most existing of this
  identity (group like with like), then lowest `storageId` for stable
  ordering
- if no proximate linked chest can take the whole stack, fail closed with
  a status diagnostic — never partial-deposit silently from a per-stack
  shortcut (bulk Deposit can still partial because the player explicitly
  asked for "everything you can")

### 5. Hover Trail Cost At Scale

Drawing a thread from a hotbar slot to its home is cheap when there's one;
a careless implementation that recomputes per-frame for every visible card
becomes a problem at the carried-readability scale targets in
[storage-prototype.md](../retired/storage-prototype.md).

Mitigation:

- exactly one trail at a time, anchored to the currently hovered element
- reuse the chest-link thread rendering vocabulary (rotated `Transform2D`
  panels) so we're not introducing a second trail primitive
- compute the trail endpoints lazily on hover-enter, drop on hover-exit;
  no continuous animation, no persistent per-card state

### 6. Right-Click Conflicts With Future Inspector

The atlas already has selection (left-click) and may eventually open a
sticky inspector on the same gesture. Right-click for context menus must
not foreclose inspector design.

Mitigation:

- right-click is reserved for the context menu in this plan; the inspector
  (when it lands) opens on selection or a dedicated affordance, not on
  right-click
- context menu is a transient popover anchored to the click target, not a
  rail or panel; it disappears on click-outside, command activation, or
  Escape

## Slice 1 — Docked Triage Panel

Goal: replace the Triage atlas island with a fixed-position panel that
lives on the workspace chrome and stops competing with player islands.

Deliverables:

- view model:
  - drop the `ISLAND_TRIAGE` atlas island from
    `SlotWorkspaceAtlasLayout.baseIslands`
  - new `triageItems: List<AtlasItem>` field on
    `SlotWorkspaceViewModel`; projection routes any unhomed identity here
    instead of into `ISLAND_TRIAGE`
  - simplify `applyInitialCamera` Triage-fallback branch: empty carried
    set fits to the player-island cluster (or the last camera), never to
    the gone `ISLAND_TRIAGE`
- UI factory:
  - Triage panel rendered as a fixed left-edge overlay (sibling to the
    Belt; not an LDLib `GraphView` content child)
  - panel header shows the count and a collapse toggle
  - panel rows render as compact card variants of `AtlasItem`
  - chip suggestions on Triage cards continue to render in the panel; chip
    accept flow unchanged
  - new `installTriagePanelDropTarget` replaces the
    "drop on empty atlas while dragged from non-Triage" path that today
    routes to `ISLAND_TRIAGE`; the old `installIslandDropTarget` for
    `ISLAND_TRIAGE` goes away
  - drop priority order set per Risk 3 (panel above chest tiles above
    islands above empty atlas)
- tests in `SlotWorkspaceLdlibModelTest` move from asserting
  `AtlasItem.islandId == ISLAND_TRIAGE` to asserting `triageItems`

Exit criteria:

- a fresh atlas opens with the docked Triage panel visible and pre-loaded
  with all carried unhomed items
- pan/zoom does not move the panel
- drag from a panel row onto an island still homes the identity to that
  island via the existing `ASSIGN_HOME` RPC
- drag from any island/home onto the panel returns the identity to
  unhomed state via the same path that used to route to `ISLAND_TRIAGE`
- chip-accept behavior on panel rows is byte-identical to chip-accept on
  the old island cards
- no atlas island has `islandId == ISLAND_TRIAGE` anywhere in the running
  view model

## Slice 2 — Hotbar ↔ Home Shift+Click Symmetry

Goal: the hotbar and the atlas exchange single stacks with one keystroke,
without ever moving a visual home.

Deliverables:

- shift+click on a hotbar slot → "send back to home":
  - server-side handler resolves the slot's identity → looks up its home
    via `VisualHomeMap` → transfers via the existing main-inventory
    transfer RPC (`TRANSFER + STACK + SINGLE_TARGET + INSERT_ONLY`)
  - the home's coordinate is read but **never written** — explicitly
    asserted in test
  - identity with no home (still in Triage) → transfer to first free
    main-inventory slot; identity stays in `triageItems`
  - status bar surfaces `returned_to_home / returned_unhomed /
    no_free_main_slot`
- shift+click on a home (atlas card) → "send to next free hotbar slot":
  - server-side resolver picks the lowest-index unoccupied hotbar slot;
    if none, fail closed with `no_free_hotbar_slot` and do nothing
  - reuses the existing hotbar assignment RPC
    (`ASSIGN + STACK + SINGLE_TARGET + ASSIGN_WITH_DISPLACE`) with the
    resolved slot index — server constructs the slot, never the client
  - status bar surfaces `assigned_to_hotbar_<n> / no_free_hotbar_slot /
    nothing_to_assign`
- two new RPCs (`returnHotbarToHome`, `assignHomeToFreeHotbar`) with
  matching `SlotWorkspaceCommandService` handlers; existing transfer and
  assign request factories do the actual planning

Exit criteria:

- shift+click a hotbar slot returns its stack to the home's island and
  the home's coordinate is unchanged in the next view-model refresh
- shift+click a home with a free hotbar slot moves that stack to the
  hotbar; visual home unchanged
- shift+click a home with no free hotbar slot does nothing and surfaces
  the diagnostic
- shift+click an unhomed Triage card from the panel returns its hotbar
  copy (if present) into main inventory and leaves it in `triageItems`
- no path in slice 2 ever writes through `ASSIGN_HOME`

## Slice 3 — Bidirectional Hover Trails

Goal: the player can see, at a glance, the relationship between every
hotbar item and its atlas home.

Deliverables:

- hover-enter on a hotbar slot whose stack has a home → render one trail
  from the slot center to the home card center
- hover-enter on a home that is currently in a hotbar slot → render the
  trail in the same vocabulary, plus a subtle border accent on the
  hotbar slot itself
- trails reuse the chest-link thread renderer (rotated `Transform2D`
  panels) so the visual vocabulary stays consistent with proximate
  links; color is distinct from chest-link green
- exactly one trail rendered at a time; clears on hover-exit
- view model exposes the lookup needed for trail endpoints: each
  `AtlasItem` already carries identity; each `HotbarSlot` already carries
  identity. No new fields needed if the UI factory does the join. If
  perf measurement says otherwise, add a precomputed
  `Map<HotbarSlotIndex, HomeId?>` on the view model rather than recomputing
  per hover.
- no trail rendered for hotbar slots whose identity is unhomed (Triage);
  hover instead highlights the corresponding panel row

Exit criteria:

- hovering a hotbar slot draws a trail to its home card (or pulses its
  Triage panel row if unhomed)
- hovering a home that's in the hotbar draws the same trail and
  accent-borders the hotbar slot
- hovering a home that is *not* in the hotbar shows nothing extra
- no perf regression on a populated atlas; `GraphView` frame cost flat
  versus baseline at the carried-readability scale target

## Slice 4 — Chest Tile Drag-In And Shift-Take

Goal: proximate chest tiles act as full drop targets and shift-take
sources without opening the vanilla container UI.

Deliverables:

- drag from any atlas card or hotbar card onto a proximate chest tile →
  deposit that single stack:
  - factor out `depositSingleStack(player, sourceLane, sourceSlotIndex,
    storageId)` from the existing bulk `DepositExecutor`; reuse its
    simulate-then-commit logic so partial fits don't silently split
  - new `depositToChest` RPC carries source coords (lane + slot index) +
    target storageId; server validates proximity and ownership before
    executing
  - non-proximate chest tile rejects the drop with a visible cue (gray
    border flash) and a `not_proximate` status diagnostic
  - per Risk 3, chest tile drop target sits above island and below the
    Triage panel in z-order
- shift+click on a stack inside a proximate chest tile's contents grid →
  take that single stack:
  - factor `takeSingleStack(player, storageId, chestSlotIndex)` from
    `TakeAllExecutor`; same `player.getInventory().add(stack)` →
    re-insert remainder → drop fallback policy
  - new `takeFromChest` RPC; server validates proximity
  - status bar surfaces `took_stack / took_partial / nothing_to_take /
    not_proximate`
- visual feedback while dragging an atlas/hotbar card: proximate chest
  tiles get a highlight border; non-proximate stay dim

Exit criteria:

- dragging an atlas card onto a proximate chest tile deposits the stack
  and updates both the chest tile contents grid and the carried atlas in
  the next refresh
- dragging onto a non-proximate chest tile does nothing and surfaces the
  diagnostic
- shift+click a stack inside a proximate chest tile pulls it into carried
  inventory (with drop fallback if main inventory is full)
- no path in slice 4 mutates a visual home

## Slice 5 — Shift-Deposit To Linked Chest

Goal: the player can deposit a single stack into its identity's linked
proximate chest with one keystroke, without aiming at the chest tile.

Deliverables:

- shift+click on a home or hotbar slot whose identity's island has a
  proximate linked chest with room → deposit just that stack
  - tiebreaker order per Risk 4: tightest fit, then most-of-this-identity,
    then lowest storageId
  - falls through to slice 2's "send to home / send to hotbar" semantics
    when there is no proximate linked chest with room — i.e., shift+click
    on a home prefers chest-deposit over hotbar-assign when both apply
  - reuses slice 4's `depositSingleStack` path; the difference is target
    resolution, not execution
- the home's `presence` strip already shows linked-chest counts; no new
  view-model surface needed for resolution
- one new RPC (`shiftDepositToLinkedChest`) or, equivalently, one
  resolver path inside the existing slice-2 RPCs that prefers chest
  deposit when applicable. Pick one in implementation; do not ship both.
- status bar surfaces `deposited_to_<chest label> /
  no_linked_proximate_chest_with_room / nothing_to_deposit`

Exit criteria:

- shift+click a home whose island links to a proximate chest with room
  deposits the stack to that chest, not to the hotbar
- shift+click the same home with no proximate linked chest still falls
  through to slice 2's hotbar-assign behavior
- shift+click a hotbar slot whose identity's island links to a proximate
  chest with room deposits the stack to the chest, not to main inventory
  via slice 2's "return to home" path
- tiebreaker rule is unit-tested at the planner level

## Slice 6 — Right-Click Context Menu On Homes

Goal: every gesture from slices 2, 4, and 5 has a discoverable surface
beyond keyboard knowledge.

Deliverables:

- right-click on an atlas home → transient popover anchored to the card,
  rendered on the same overlay layer as Triage / Belt / Kit Rack so
  pan/zoom doesn't move it
- popover commands, in this order:
  - **Send to hotbar** — invokes the slice-2 home → free hotbar path;
    disabled (with reason) when no free slot
  - **Deposit to linked chest** — invokes the slice-5 path; disabled
    (with reason) when no proximate linked chest with room
  - **Re-home…** — opens an island picker overlay; selecting an island
    invokes the existing `ASSIGN_HOME` RPC. This is the *deliberate*
    re-home path; it is the only quick way to change a home that is not
    a drag of the canonical atlas card.
  - **Cancel** / Escape closes the popover with no effect
- popover dismisses on: click-outside, command activation, Escape
- right-click on a hotbar slot → mirror popover with: **Send to home**
  (slice 2), **Deposit to linked chest** (slice 5 if applicable), **Cancel**
- right-click on chest contents (slice 4 area) and on Triage panel rows
  is reserved for future slices; leave a no-op stub or do not register
  the listener at all

Exit criteria:

- right-clicking any home opens the popover with the three commands
- each command produces the same downstream outcome as its keyboard
  shortcut
- "Re-home…" successfully changes a home and emits a reversible record
  via the existing `ASSIGN_HOME` path so the future undo system can
  unwind it
- popover does not survive a pan/zoom interaction; it dismisses cleanly

## Testing Priorities

Highest-value coverage to add or extend, in slice order:

- `SlotWorkspaceLdlibModelTest`: `triageItems` projection replaces
  `ISLAND_TRIAGE` assertions; drop-target priority order
- `SlotWorkspaceCommandService`: new `returnHotbarToHome` and
  `assignHomeToFreeHotbar` handlers (happy path, no-home, no-free-slot,
  identity-not-carried)
- regression test: shift+click paths never write through `ASSIGN_HOME`
- `DepositPlanner` (or new `SingleStackDepositPlanner`): tiebreaker rule
  with multiple proximate linked chests
- `DepositExecutor` factor-out: `depositSingleStack` matches existing
  bulk semantics on partial-fit (no silent split)
- `TakeAllExecutor` factor-out: `takeSingleStack` matches existing
  add-then-reinsert-then-drop fallback
- proximity gating: every new RPC fails closed when the chest is not
  proximate
- existing architecture assertion (`common` does not import LDLib2)
  continues to pass

## Definition Of Done For This Phase

This phase is complete when:

- Triage is rendered as a docked panel; no atlas island has
  `islandId == ISLAND_TRIAGE`
- shift+click on a hotbar slot returns to home; shift+click on a home
  assigns to a free hotbar slot or deposits to a proximate linked chest
- hover trails draw bidirectionally between hotbar slots and homes
- proximate chest tiles accept drag-deposit and shift-take of single
  stacks
- right-click context menu on homes (and hotbar slots) exposes the new
  gestures plus a deliberate "Re-home…" path
- no shift+click or drag gesture ever moves a visual home; only
  canonical-card drag and the explicit "Re-home…" command do
- visual-home changes remain reversible records compatible with the
  future general undo system
- all real mutations continue to route through the intent router with
  server-constructed targets
- the partial Kit prototype can resume cleanly: nothing in this plan
  touches Kit domain types, persistence, or activation paths
