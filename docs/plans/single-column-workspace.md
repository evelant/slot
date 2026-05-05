# Single-Column Workspace

Last updated: 2026-05-05.

## Goal

Collapse the SLOT workspace from a two-column layout (left column +
wall) to a single column (sliver + wall) so the sidebar mounts cleanly
alongside arbitrary modded container UIs without colliding. Target end
state: `WORKSPACE_WIDTH_PX ≈ 280` (down from 414), still 9 cards per
wall row.

The compression is achieved by either *removing* surfaces whose value
is now redundant (nearby chests, loot panel, dedicated Triage panel)
or *folding* them into existing surfaces (chest finder into cards,
action cluster into icons, TOC into a vertical sliver).

For the upstream brainstorm + rationale per surface, see the
2026-05-05 conversation that decided on this direction.

## Out of scope

- **Right-side panel for external storage.** The chest-tile vision in
  `MEMORY.md` stays as a future direction; this plan keeps everything
  in the existing left-side column shape.
- **Hide vanilla 36-slot band** (Phase 3b of the closed list-view
  plan). Tracked separately under Deferred experiments.
- **Floating / draggable workspace mode.** Brainstormed and skipped:
  if the column is narrow enough, modded UIs coexist without it.
- **Reducing cards-per-row from 9.** Brainstormed and skipped: 9
  remains useful at 280-px sidebar width and changing it churns more
  layout than it's worth.

## Phases

Roughly ordered by isolation and risk: each phase compiles + ships
green on its own and the next phase builds on it. **Skip ahead if a
phase is already covered by a different track.**

### Phase 1 — Hide redundant panels (no semantic change)

Gate three left-column builders to return `null` so they don't render:

1. `StoragePanelBuilder.overlay()` — nearby chests chip stack.
2. `LootChestPanelBuilder.overlay()` — unclaimed chest panel.
3. `TriagePanelBuilder.overlay()` — Triage panel rendering only;
   server-side Triage routing keeps working until Phase 2.

Leave the builder code in place — per the 2026-05-05 conversation
the chip stack in particular may come back as a horizontal strip
above / below the main list. Keep the projection paths
(`SlotWorkspaceViewModel.chestChips()`, `triageItems()`,
`lootChestPanel()`) since other surfaces still consume them
(deposit-by-affinity, search-results panel, server RPC handlers).

For the loot chest, keep the **claim-on-deposit observer** in
`neoforge/storage/`: depositing into an unclaimed chest still
auto-claims it. The visual loot panel goes away; the in-world
discovery + claim semantics stay.

**Acceptance.** Sidebar mounts on a chest screen and shows: search
hint row → wall sections → status bar. No chest chips, no loot panel,
no Triage panel. Compile green; existing tests green.

### Phase 2 — Auto-home eliminates Triage as a routing destination

Today, an item with no `VisualHomeAssignment` lands in a docked
Triage panel and renders chip suggestions for the player to accept.
Per 2026-05-05 playtest signal the suggestions are nearly always
correct, so we move the chip-accept inline:

1. **Server-side auto-home.** When the workflow domain observes an
   identity entering carry without an assignment, run the same
   `IslandSuggestionService.suggest(...)` pipeline the chip uses,
   pick the highest-confidence suggestion, and write the
   `VisualHomeAssignment` immediately. Same persistence path as
   chip-accept.
2. **Confidence threshold + Misc fallback.** When no suggestion
   clears the threshold (TBD: start at the same confidence the chip
   sort uses; tune from playtest), assign to a synthesised **Misc**
   island. Misc auto-creates on first use, sits at a stable position
   in the wall (probably last), and is freely re-orderable like any
   other section. Items moved out of Misc are no longer in Misc;
   when Misc empties, hide it (don't delete — it can repopulate).
3. **Visual mark on auto-placed cards.** Briefly tag cards that
   were auto-homed within the last few ticks (or until the player
   first interacts with them) with a small badge so the player can
   spot a misroute and drag it elsewhere. Drag-to-rehome is already
   the canonical correction gesture; we're just making the trigger
   visible.
4. **Stop populating `triageItems`.** With server-side auto-home,
   the projection's `triageItems` list goes empty in steady state.
   Leave the field + codec — Triage as an *island id* still exists
   so player-driven "send to Triage" gestures can work later if we
   want — but no card lands there automatically.

**Acceptance.** Pick up a brand-new identity → it appears in a
sensible homed section without the player accepting a chip. A
deliberately-unmatched identity (test by stripping facets) lands in
Misc. Drag-to-rehome still works.

**Risk.** Facet misses on modded items will silently misroute. The
auto-placed badge + Recents section (Phase 3) make these visible
quickly enough that the player can fix without ceremony. If
playtest shows a hot mess, fall back to *only* auto-homing items
above a higher confidence threshold and surface the rest in a
single-line inline Triage chip (not a docked panel).

### Phase 3 — Recents section at top of wall

Add a single horizontal strip of small icons (~16 px each, vanilla
chest size) showing the last N item identities the player picked
up. The strip lives **above the wall scroller and outside the
scroll area** — it stays pinned while the player scrolls the
sectioned list, since "where did the thing I just grabbed end up?"
is a question that follows the player around regardless of scroll
position.

Concretely: render the strip as a sibling of the wall scroller
inside the wall panel, between the top action row and the
`midRow` that holds `leftColumn` + `scroller`. The scroller does
NOT include it as a child.

- **Data source.** `recents.visibleItems()` already exists in the
  workflow domain and feeds the card-recency tint today; surface it
  as a strip.
- **Render.** Optional small label or chevron on the left for
  affordance ("Recents"); row of icons (no ordinal-bound 9-per-row
  constraint — these are decorative pointers, not full cards). N
  starts at maybe 12; tune after playtest.
- **Click behavior.** Click on a recents icon = scroll wall to that
  identity's home AND select the homed card. The recents icon and
  the homed card represent the same identity, so collapsing the
  click action onto the homed card is the cleaner semantic.
- **No section in the TOC sliver** — recents is a meta-strip, not a
  homing destination, so it doesn't need a TOC dot.

**Acceptance.** Pick up a sequence of items → they appear in the
Recents row in MRU order. Click any → wall scrolls + card
highlighted.

### Phase 4 — TOC vertical sliver

Replace `TocPanelBuilder` rendering with a 4–6 px vertical strip
glued to the wall scroller's left edge.

- **Layout.** One colored dot per non-empty non-Triage section,
  vertical position proportional to the section's position in the
  scroll content. Dot color = section color.
- **Attention.** When a section has any off-screen card matching
  the active search OR flagged kit-needed, the dot pulses (same
  attention rules as today's TOC row dots).
- **Click → scroll.** Click the dot, wall scrolls so that section's
  header sits at the top of the viewport. Same math as
  `TocPanelBuilder.scrollWallToSection`.
- **Reorder via section header drag.** TOC drag-row reorder goes
  away with the panel; section headers in the wall already implement
  the same drag source via `installSectionHeaderDropTarget`. **Verify
  + extend if needed** — header drag must move sections regardless of
  whether the player drops above or below an anchor section.
- **Tooltip on hover.** A 2-line tooltip showing section name +
  count covers the "I forgot which section is which color" gap.

**Acceptance.** Sidebar shows a thin colored strip with one dot per
section. Click → scroll. Off-screen sections with hits pulse. Drag
a section header up/down → reorders.

### Phase 5 — Chest finder folded into card chrome

Replace `SearchResultsPanelBuilder` rendering with per-card
horizontal expansion: when a card has wayfinding-relevant pointers
(non-proximate chest holding it OR kit-needed and lives in a
remote chest), the card grows wider to embed the chest pointer
inline.

- **Layout.** Standard card 22×22; expanded card N×22 where N
  includes a sub-strip with arrow glyph + distance (or
  dim-shorthand + coords for cross-dim). The wall already uses
  `flexWrap(WRAP)` so wider cards reflow naturally — they take more
  row space, fewer cards fit per row, and the row breaks early.
- **Behavior.** Click on the icon = same as today (extract /
  cancel-cursor / etc.). Click on the sub-strip = TBD: either
  trigger walk-to via the wayfinding HUD, or open the chest's
  context menu (rename, etc.). Settle by playtest.
- **Reuse `WayfindingChip` style.** The compact one-line chip we
  built for the storage panel is the right shape; trim it further
  to fit beside the icon (~50–80 px wide).
- **Drop the SearchResultsPanelBuilder rendering.** Keep the
  projection (`chestChips`-derived match counts, etc.) since
  per-card data may still want it; the dedicated panel goes away.

**Acceptance.** Search for an item that lives in a non-proximate
chest → its card grows horizontally to show "→ 12m" inline. No
separate "Chest locator" panel.

**Risk.** Variable-width cards in a fixed-grid section break visual
rhythm. Mitigations to consider during implementation: (a) cap at
two widths — standard and expanded; (b) put expanded cards on their
own row; (c) animate expansion only when search is active so
non-search browsing keeps the uniform grid.

### Phase 6 — Action cluster icon-ification

The current top row reserves ~200 px for `Deposit`, `Gather`,
`Vanilla` text buttons + 2 icons. Compact:

- **Deposit → icon.** Replace the 72-px text button with a 16-px
  icon (chest + arrow). Tooltip carries the count.
- **Gather → icon.** Same shape. **Remove the active-kit gate.**
  Gather should fire whenever the player has any positive desired
  count gap reachable from a proximate chest, kit-active or not.
  The current `gatherButton.setVisible(activeKit && proximate)` gate
  in `WorkspaceOverlays.topRightActionsOverlay()` becomes
  `gatherableIdentitiesExist()` — wire from
  `viewModel.depositableIdentities()`'s sister projection (TBD: add
  one).
- **Vanilla → icon, lower priority.** It's a safety fallback — push
  it into a small "settings" cluster or behind a chevron. Keep the
  hotkey path.
- **Free space goes to** the search hint + free-slots chip on the
  same single row. Top row stays one line at 280-px sidebar width
  with comfortable margins.

**Acceptance.** Top row: search hint (left), free-slots chip
(middle), 3 small action icons (right). All three actions still
work. Gather lights up without an active kit when desired-counts
need filling and a proximate chest can satisfy.

### Phase 7 — Width recalibration

With phases 1–6 done, drop the dead width:

- `SIDEBAR_LEFT_COLUMN_MAX_WIDTH_PX` → 0 or a small value reserving
  the TOC sliver (`TOC_SLIVER_WIDTH_PX = 6`).
- `WORKSPACE_WIDTH_PX` recomputed from
  `WALL_CONTENT_WIDTH_PX + TOC_SLIVER_WIDTH_PX + padding` — target
  ≈ 280.
- `LeftColumnBuilder` collapses to just the sliver and the
  search-results-fold call site (or goes away entirely if the
  sliver lives directly in the wall scroller's frame).

**Acceptance.** Sidebar mounts at ≤ 290 px on every host. A
crafting table screen at 1080p / GUI scale 3 still has its full
image visible to the right of the sidebar with EMI room left over.

## Work-in-progress notes

These will likely come up during implementation; capture here so
future agents don't re-discover them.

- **Memory rule "Re-home is intentional"** (`MEMORY.md`) means any
  auto-home pipeline must be conservative about *moving* an
  existing assignment. Auto-home only triggers when *no* assignment
  exists; existing homes are sticky.
- **Memory rule "Wall drag is single-element"** carries over —
  Recents icons drag a single identity, not a multi-pickup.
- The `SearchResultsPanelBuilder` deletion shouldn't disturb the
  search-buffer / modal logic in `SearchController`. Search still
  filters wall sections; the per-card pointer is the new locator.

## Verification

```bash
./gradlew :common:compileJava :neoforge:compileJava
./gradlew :common:test :neoforge:test
```

Manual playtest checklist:

- [ ] Sidebar width ≤ 290 px on every host screen.
- [ ] Open crafting table; sidebar + crafting both fully visible at
  GUI scale 3.
- [ ] Pick up a never-seen-before vanilla item → it auto-homes to a
  sensible section.
- [ ] Pick up something facets miss → lands in Misc.
- [ ] Recents shows last N pickups; click → scroll + select.
- [ ] TOC sliver: click → scroll; off-screen kit-needed pulses.
- [ ] Search → matching cards in non-proximate chests grow with
  inline wayfinding.
- [ ] Action cluster: Deposit / Gather / Vanilla all icon-only,
  still working; Gather works without an active kit when
  desired-count gaps + proximate chest exist.
- [ ] Drag a section header to reorder — works without TOC.
- [ ] Loot chest in world: walk up, deposit one item, chest claims
  itself (claim-on-deposit observer). No floating loot panel.
