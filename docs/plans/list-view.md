# List View — Replace 2D Atlas With Sectioned Vertical List

Status: Phase 1 + Phase 2 shipped 2026-05-03 (TOC drag-to-reorder
included). Phase 3 (container-screen sidebar + hide vanilla band +
mod-observer transparency) outstanding. Replaces the pan/zoom atlas
with a single-LOD sectioned vertical scroll list. Same projection,
same item cards, same gestures (outside section reorder), same
authority, same intent router. UI rearrangement, not a semantic
redesign.

For the design that's being replaced, see
[../design/atlas.md](../design/atlas.md). For relevance scoring (which
survives in reduced form), see [../design/relevance-lod.md](../design/relevance-lod.md).

## Why

Pan/zoom is doing more harm than good:

- Players only zoom when forced — when they need information that
  isn't visible at the default cell size. That's a UX failure of the
  default density, not a value of zoom.
- Players only pan when forced — when they had to zoom in for
  visibility. Pan is a downstream cost of zoom.
- 2D nudge layout produces persistent "wasted space" between islands;
  the canvas never feels tight.
- Relevance-LOD machinery (band-driven cell resizing) was designed
  for an "always show everything" model that's been abandoned. We
  render every card at one size today; the LOD code is unused.
- The atlas can't appear on a container/machine screen — its only
  surface is the player inventory key. EMI/JEI integration and
  crafting/machine workflows have no SLOT story today.

A sectioned vertical list with one LOD addresses all four:

- One well-designed density delivers all card information at every
  surface, with no zoom required.
- Vertical scroll replaces 2D pan with a learned, universal gesture.
- Sections are tight flow grids — no wasted space between items, no
  wasted space between sections.
- The same widget renders at full screen for the inventory key and
  in a left ~1/3 column for container/machine screens, preserving
  the "carried inventory unification" value across contexts.

## What's going

1. **Zoom + LOD bands.** Single LOD, no band switching, no
   cell-size variance, no zoom gesture.
2. **2D atlas canvas.** Replaced by a vertical scroll list of
   sections.
3. **2D positioning** (free coordinates within an island). Replaced
   by ordinal position within a section. Sections flow-wrap to
   available width; ordering is invariant under reflow.
4. **Search highlight-only.** Search becomes a filter — non-matching
   cards disappear from the wall during the active query.

## What's staying (semantics + UI)

- Item cards: current chrome (border, status colors, M/N badge,
  progress bar, pips, kit-need saturation), single LOD.
- Ghost vs carried distinction.
- Wall content rule: carried items + proximate-chest ghosts (NOT
  all-known homes; the abandoned "always show everything" model
  stays abandoned).
- Triage panel (left bar, unchanged).
- Chest finder (left bar, unchanged).
- Nearby chests panel (left bar, unchanged).
- Loot chest interactions (unchanged).
- Search input + gestures (only behavior changes from highlight to
  filter).
- Kits panel (unfolds above hotbar, unchanged).
- Hotbar (docked bottom strip, unchanged).
- Wayfinding HUD overlay (unchanged).
- Wayfinding chips in proximity / chest-locator panels (unchanged).
- Storage areas / claimed chests (no model changes; same panels,
  same proximate ghost projection).
- Authority, projection, intent routing, deposit planner, gather,
  cleanup protection (all unchanged).
- All right-click menus, drag-drop semantics, split-cursor gestures
  (unchanged outside section reorder).
- Single-island-only drag rule (no multi-select, no group drag) —
  applies to sections in the new model too.

## What's adding

1. **TOC left-bar tab.** Section index attached to the wall as a
   tab strip (not a separate left-bar box — visually distinct from
   triage / chest finder / nearby chests). Each entry shows section
   name, color (mirrors current island colors), item count, and
   status dots.
   - Click TOC entry → scroll that section into view.
   - Drag TOC entry → reorder sections in the wall.
   - Status dots: search match off-screen, kit-needed off-screen.
     (EMI pin off-screen reserved for the EMI integration pass.)
2. **Container-context layout.** Same wall, narrowed to a
   left-screen column when an `AbstractContainerScreen` is open.
   Section column count reflows to width; ordering invariant.

## Phases

### Phase 1 — Standalone list view (SHIPPED 2026-05-03)

Replace the atlas canvas with the sectioned vertical scroll list on
the player inventory key.

- Swap `WorkspaceUi`'s atlas surface for a vertical scroll widget
  hosting one section block per current island.
- Each section: header row (name + color + count) + flow grid of
  cards at fixed cell size.
- Drop pan/zoom inputs; bind scroll to mouse wheel + PgUp/PgDn.
- Single LOD: cards always render at default size; remove
  `BandPicker` calls in the workspace render path.
- Drag-drop: card → card within section reorders ordinal; card →
  another section moves home; ghost preview during drag (existing
  ghost system is sufficient for v1).
- Search becomes filter: `SearchMatchContributor` still scores
  matches but the workspace surface hides non-matching cards
  during an active query (sections may shrink to zero rows; empty
  sections collapse their headers to a one-line "Section name (0
  shown)" until search clears).
- Triage / chest finder / nearby chests / kit panel / hotbar
  unchanged (left-bar geography preserved; bottom strip preserved).
- Wayfinding chips on max-zoom atlas cards ("lives in this chest"
  text) deleted — no max zoom anymore, no surface for them.

**Acceptance:**

- Player inventory key opens the list view.
- All current gestures (right-click menus, ctrl+scroll desired
  count, drag-to-home, split cursor, deposit, gather) work.
- Cards drag-reorder within sections; cards drag between sections.
- Search filters the wall in place; clear restores full set.
- Sections preserve ordinal stability across window resize / GUI
  scale change.
- `:common:test :neoforge:test` green.

### Phase 2 — TOC left-bar tab (SHIPPED 2026-05-03)

Shipped:

- TOC tab docked at the top of the left column
  (`TocPanelBuilder.java`). Visually distinct from triage / chest
  finder / nearby chests boxes — same `GLASS` panel skin, but no
  inner panel chrome and a tighter row layout (9 px rows, 6 px
  font, 3 px swatch).
- Entries auto-track non-Triage sections in display order. Filters
  out sections with zero atlas items (so empty sections from
  populated bases don't dominate the strip). Each entry shows
  swatch + label + item count; label truncates at 14 chars.
- Capped at 12 visible rows with an inner `ScrollerView` for
  overflow, so the TOC never monopolises the column.
- Click → scrolls that section into view via
  `wallScroller.verticalScroller.setValue(normalizedY)`.
- Status-dot lighting (off-screen search match / kit-needed) is
  handled by the same `installRowStateTracking` TICK handler that
  also drives:
  - **Deposit hover preview**: when `host.depositPreviewActive`,
    highlight rows whose section has any depositable identity and
    swap the count label to show the depositable count in `ACCENT`.
  - **Gather hover preview** (mirrors deposit): when
    `host.gatherPreviewActive`, same treatment in `ACTIVE_HOTBAR`
    palette. Predicate: `item.kitNeeded() && !item.presence().isEmpty()`,
    exposed as `AtlasCardBuilder.isGatherableItem(item)`.

- TOC drag-to-reorder. Each row is now a drag source carrying an
  `IslandDrag(islandId)` payload (a small colored stripe in the
  island's swatch follows the cursor). Each row is also a drop
  target — the drop's vertical half (above vs below the anchor's
  center) decides whether the source lands before or after the
  anchor in the canonical `playerIslands` list. Indices are
  resolved in `playerIslands` order via the view-model islands
  list (Triage is never present), so empty hidden sections keep
  their position.
  - Server side: new `WorkflowEvent.VisualIslandReordered`
    (`islandId`, `targetIndex`) + projection case that removes
    the island from its current index and reinserts at the
    clamped target. `VisualAtlasWorkflowDomainService.reorderIsland`
    + `playerIslandIndex` mirror the existing `assignHome` and
    `moveIsland` patterns. `SlotWorkspaceCommandService.reorderIsland`
    records an undo entry. New RPC plumbed through
    `WorkspaceRpcDispatcher.sendReorderIsland` →
    `SlotWorkspaceUiSession.reorderIsland`.
  - Layout: `SlotWorkspaceAtlasLayout.baseIslands` no longer
    sorts by `(y, x, label)` — the projection's
    `playerIslands` list is now authoritative for display
    order, so the reorder writes through to the wall directly.
    The y/x coordinates on `VisualAtlasIsland` survive only as
    legacy state ignored by the renderer.
  - `WorkspaceDrags.IslandDrag` shed its `grabOffsetX/Y` fields
    (canvas-era dead weight); now just carries `islandId`.

**Outstanding (deferred):**

- Section color picker on the section header.
- Animated scroll (current implementation snaps).

### Phase 3 — Container screen injection (NOT STARTED)

Render the wall as a left-side sidebar on container/machine
screens, replacing the role of the vanilla 36-slot player
inventory section. The unification of carried inventory is the
load-bearing value here — the player must NOT have to shuffle
items through the vanilla inventory to interact with crafting
or machines.

A skeleton hook exists in
[../../neoforge/src/main/java/dev/imagio/slot/neoforge/client/screen/SlotContainerSidebar.java](../../neoforge/src/main/java/dev/imagio/slot/neoforge/client/screen/SlotContainerSidebar.java)
— it listens for `ScreenEvent.Init.Post` on
`AbstractContainerScreen` and currently just diagnostic-logs.
Real mount logic (embedding LDLib2's `ModularUI` widget tree
inside a vanilla container screen) hasn't been attempted; needs
its own design/investigation pass first — see "Investigation
needed" below.

**Investigation needed before committing to 3a:**

- LDLib2 lives in its own `Screen` subclass via `ModularUI`.
  Mounting individual widgets inside an `AbstractContainerScreen`
  requires either: (a) a `Screen.Opening` wrapper that swaps the
  vanilla screen for a custom subclass that hosts both the
  vanilla menu and our widget tree, (b) a mixin into the screen's
  render path, or (c) `ScreenEvent.Render` overdraw with manual
  hit-testing and event routing. Each has different mod-compat
  costs.
- z-order vs. EMI's right-edge real estate.
- Cross-surface drag routing: the wall card's
  `installAtlasItemDragSource` has no awareness of vanilla
  `Slot` drop targets, and vanilla `Slot.mouseDragged` doesn't
  know about LDLib2 drag payloads.
- Width policy: ratio (~1/3 viewport) vs. fixed pixel target.

#### 3a — Sidebar alongside vanilla

- On any `AbstractContainerScreen` open: render the wall as a
  left-side column (target ~1/3 viewport, with min/max clamps).
- Vanilla container UI sits in its native position; the redundant
  vanilla 36-slot band is visually present but functionally
  ignored (the wall is the canonical interaction surface).
- Drag from wall card → machine slot: route through intent
  router using the existing `largestCarriedSlot{SourceId,Index,
  Count}` projection (proven by split-cursor and atlas-card
  pickup paths).
- Drag from machine slot → wall card: cancels into split-cursor
  or routes to home (same as carrying + dropping today).
- EMI right-edge real estate untouched.
- Sidebar uses the SAME widget as standalone, just narrower
  (column count reflows). Ordinal-by-section addressing means
  spatial memory transfers between contexts.

**Acceptance for 3a:**

- Open a chest, crafting table, furnace, anvil: left sidebar
  shows the wall.
- Drag-from-wall to grid slot works.
- Drag-from-grid to wall works.
- EMI panel renders at its usual right edge without overlap.
- `:common:test :neoforge:test` green.

#### 3b — Hide vanilla player inventory band

- For `AbstractContainerScreen` subclasses, hide the vanilla
  player-inventory slot band. Two viable techniques (pick during
  implementation):
  1. Move slot positions off-screen on screen open; restore on
     close. Slot indices in the menu remain valid (EMI's "+"
     button reads inventory by index, not by screen position).
  2. Mixin/event-overdraw the player inventory band with our
     wall extension.
- Vertical real estate freed by hiding the band is given to the
  sidebar (it can extend further down).
- Slot indices preserved in the menu so:
  - EMI's "+" recipe transfer continues to work (it pulls from
    inventory slots, not screen pixels).
  - Mod-side observers that read player inventory directly still
    see consistent state.
- Hard-custom screens (rare, e.g., some AE2 / Refined Storage
  terminals that aren't `AbstractContainerScreen`): fall back to
  3a behavior (sidebar alongside, vanilla untouched).

**Acceptance for 3b:**

- Vanilla 36-slot band is invisible on every covered screen.
- EMI "+" still pulls from inventory.
- Sidebar extends to fill the freed vertical space.
- Hard-custom screens (if encountered) fall back gracefully.

#### 3c — Mod-observer transparency

Some mods (sorting, transfer, hotkey-move) bind to vanilla player
inventory slots and gestures. SLOT gestures against the wall must
behave as-if they were against vanilla inventory so those mods
continue to work without per-mod patches.

- For gestures that mods observe (typical: shift-click move,
  hotkey transfer, sorting hooks), the intent router may need
  to "shuffle through vanilla" transparently — pull from the
  carried source, momentarily route through a vanilla
  inventory slot, then to target.
- Gesture latency and visual flicker on the vanilla band MUST
  remain zero (since the band is hidden in 3b, the player
  doesn't see the shuffle).
- Where the mod observation pattern can't be satisfied without
  visible side effects, fail closed with a useful diagnostic
  rather than break compat silently.

**Acceptance for 3c:**

- Common mod compat scenarios verified manually:
  - EMI `+` transfer
  - Shift-click move into machine slot
  - Hotkey-move (number keys) into machine slot
- No flicker / no perceptible shuffle.
- Diagnostic logs identify any unsupported observer pattern.

## Out of scope (separate plans)

- **EMI integration** (recipe pin = relevance contributor;
  enhanced "+" that pulls from carry + proximate chests
  transparently). Carved out as a follow-up plan after Phase 3
  stabilizes. Pinned recipe ingredients become "desired carry"
  via the existing desired-counts model — that's the natural fit.
- **Tab mode** (sections as tabs instead of vertical scroll, for
  very narrow contexts). Deferred; revisit if Phase 3 sidebar
  feels too cramped under playtest.
- **Chest-as-section** (claimed chests surfacing as wall sections
  alongside islands). Not part of this plan; chests stay external
  via the existing nearby chests / chest finder panels.

## Retirement

### Code

- `common/src/main/java/dev/imagio/slot/atlas/lod/AtlasLayout.java`
- `.../atlas/lod/AtlasNudgeLayout.java`
- `.../atlas/lod/AtlasLayoutConfig.java`
- `.../atlas/lod/AtlasLayoutResult.java`
- `.../atlas/lod/WeightedGridPacker.java`
- `.../atlas/lod/Band.java`
- `.../atlas/lod/BandPicker.java`
- `.../atlas/lod/BandPickerConfig.java`
- `.../atlas/lod/AtlasDropResolver.java` (drop semantics in flow
  layout differ; replace with section-aware ordinal resolver)
- `.../atlas/FitCarriedCamera.java` (no camera)
- `.../atlas/CameraHistory.java` (no camera)

Survives but reduced:

- `.../atlas/lod/AtlasRelevance.java`,
  `RelevanceContributor.java`, `RelevanceScore.java`,
  `RelevanceContext.java`, all `lod/contributors/*` — relevance
  scoring still drives ordering hints and TOC status dots, but
  no longer drives LOD band selection.
- `.../atlas/AtlasSearchIndex.java` — search filtering still
  needs the index.

### Docs

- [../design/atlas.md](../design/atlas.md) — superseded by this
  plan and the eventual `../design/list-view.md` write-up that
  follows execution. Add a superseded header in the meantime.
- [../design/relevance-lod.md](../design/relevance-lod.md) —
  retire to `retired/`. Relevance scoring survives in reduced
  form; rewrite as a smaller "relevance scoring for ordering and
  highlighting" doc if/when needed.
- [retired/relevance-lod-prototype.md](retired/relevance-lod-prototype.md)
  — retired (moved 2026-05-03). The prototype it sketched (LOD
  bands) isn't happening.

### Memory

- `project_relevance_lod` — outdated direction; LOD bands aren't
  shipping. Update or retire.
- `project_nudge_layout` — nudge layout dies with the canvas;
  retire after Phase 1.
- `project_atlas_drag` — single-island rule still applies, but
  reword for sections.
- `project_slot_overview` — references "pan/zoom atlas" as the
  primary surface; update after Phase 1.

## Open implementation considerations

- **Container screen wrapping technique.** `ScreenEvent.Opening`
  wrap vs `AbstractContainerScreen` mixin. Pick during 3a; aim
  for the smallest surface that works for chests + crafting
  table + furnace + a TerraFirmaGreg machine.
- **Sidebar width policy.** Fixed pixel target (~280–320 px) vs
  ratio (1/3 viewport) with min/max clamps. Probably ratio.
- **Section reorder gesture.** Drag in TOC tab is the primary
  affordance. Drag-section-header in the wall could also work
  but adds a competing target zone; defer.
- **Empty-section behavior under search filter.** Collapse to
  a one-line "Section (0 shown)" header, or hide entirely?
  Probably collapse so player sees what's filtered out. TOC
  status dots light when something is hidden.
- **Card cell size.** Pin a single number (probably 32–40 px)
  early in Phase 1 so all chrome thickness math doesn't drift.
  One density, fixed; the goal is to design it well enough that
  no zoom or per-player adjustment is needed.
- **Naming.** "Atlas" code identifier survives short-term to
  minimize churn. Player-facing surface and new docs use "list
  view" / "wall." Cosmetic rename of code packages is a
  follow-up, not part of this plan.

## Pointers

For product goals, see [../product/direction.md](../product/direction.md).
For action semantics,
[../architecture/action-taxonomy.md](../architecture/action-taxonomy.md).
For the LDLib2 workspace decision,
[../decisions/0002-ldlib2-workspace.md](../decisions/0002-ldlib2-workspace.md).
For Triage's docked-panel design (unchanged),
[../design/atlas.md § Triage](../design/atlas.md). For the
gestures vocabulary the wall inherits,
[../design/gestures.md](../design/gestures.md).
