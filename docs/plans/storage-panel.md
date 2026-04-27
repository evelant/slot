# Storage Panel — Design Plan

Last updated: 2026-04-27

## Why

Storage areas currently live on the atlas canvas as foldable chips/clusters
positioned at authored `(atlasX, atlasY)` coords. Two-and-a-half problems
fall out of that:

- When a chip expands into its full chest cluster, it either overlaps
  islands, or the player has to leave a permanent gap on the atlas big
  enough for the *expanded* footprint of every base — wasted real estate
  when bases are collapsed.
- Pushing islands aside on expand wrecks the player's island grouping.
- The 2D atlas position of a chest never matches the 3D position of the
  chest in world. So spatial intuition is fiction at the chest level —
  players remember "iron lives in the smelting corner of Main Base", not
  "iron is at panel-coord (220, 80)". Atlas islands earn their spatial
  treatment because they're invented categories with no real position to
  compete with; chests don't have that excuse.

The mental models players actually use for base storage:

- **"This stuff is over here"** — labelled clusters within a base (a
  smelting nook, a tool wall). Wants sub-grouping, not 2D placement.
- **"By the workbench that uses it"** — wants *links* from chests to
  consumers. Position doesn't help; relationships do.
- **"One big system, I don't care"** (AE2/RS-style) — wants
  search-first item access. 2D layout is a tax.

None of these want a mini-atlas. They want a **list of bases, with
chests inside, searchable**.

## Workspace layout — one workflow per edge

Storage docks at the **top** of the screen, completing a four-edges
arrangement where each edge owns one phase of the inventory workflow:

```
┌─ Top: STORAGE (where my stuff lives) ─────────────────────────────┐
│ [Main Base · 12 · ●] [Mountain Mine · 6] [Oil Derrick · 3] [search]│
│ ┌──────────┬──────────┬──────────┐                                │
│ │  card    │  card    │  card    │  <- active area's chests       │
│ └──────────┴──────────┴──────────┘                                │
├─Triage─┬──────────────────────────────────────────┬──── JEI/EMI──┤
│ [item] │                                          │              │
│ [item] │   ATLAS                                  │   (future)   │
│ [item] │   (islands + carried + relevance LOD)    │              │
│ [item] │                                          │              │
│ [item] │                                          │              │
├────────┴──────────────────────────────────────────┴──────────────┤
│  Bottom: BELT + KITS  (what I'm wielding)                        │
└──────────────────────────────────────────────────────────────────┘
```

| Edge   | Surface       | Workflow phase              |
|--------|---------------|-----------------------------|
| Left   | Triage        | What just arrived           |
| Top    | **Storage**   | Where my stuff lives        |
| Bottom | Belt + Kits   | What I'm wielding           |
| Right  | (JEI/EMI)     | What exists in the world    |
| Centre | Atlas         | What I think about my carry |

## Shape

A horizontal strip docked across the top of the screen. The strip flows
between Triage's right edge and the existing top-right actions cluster
(small button overlay at top-right; either stays in place above the
strip or moves into it). The top-left search-hint overlay is replaced
by a search input embedded in the strip itself.

**Two display states**:

1. **Collapsed (~24 wu tall)** — just the row of tabs + search input.
   Atlas keeps the full vertical area below.
2. **Expanded (varies, ~70 wu typical, ~134 wu worst case)** — a row of
   the active area's chest cards drops down beneath the tab strip.
   Atlas shrinks vertically.

Vertical footprint math: chest card max height with double-chest
contents = 14 wu header + 6 rows × 16 wu = 110 wu. Plus 24 wu tab
strip = 134 wu worst case. Typical single chests with ~12 items: 14 +
2 × 16 = 46 wu, total ~70 wu strip footprint.

**No camera, no packer.** Plain flex-row UIElement for the tab strip
with a flex-row chest-card flow underneath.

## Tab strip

Each `StorageAreaSnapshot` from `viewModel.storageAreas()` becomes a
tab chip:

- Label · chest count · proximity dot (lit when `area.proximate()`)
- Active state painted with the area's `color()`; inactive tabs fade
  toward MUTED
- Click → activate that area (override proximity); click an active tab
  → fully collapse the strip (no active area)

A **search input** sits at the right end of the tab list. Typing in it
auto-activates a virtual "Results" tab that takes the active slot.

**Tab overflow** when a player has many bases: defer the call until we
hit it. First version assumes ≤8 areas fit horizontally on a typical
screen; if more, the overflow tabs scroll horizontally inside the strip
(or fall into a `+N more` popover — pick after we see the visual).

## Active-area selection

One area is active at a time. Resolution order:

1. **Search active** → virtual "Results" tab wins.
2. **Manual override** → if the player clicked a tab, that tab stays
   active until they click another or click the active one to
   collapse.
3. **Proximity** → the area containing any proximate chest auto-
   activates. If multiple are proximate (overlapping bases), the
   highest-`displayOrder` wins arbitrarily; refine later if it bites.
4. **None** → strip collapses to tabs-only, atlas at full vertical
   size.

A `host.activeStorageAreaId: String?` field captures the manual
override; null means "follow proximity / search". Per-session, not
persisted to the projection.

## Chest cards (active area)

Reuse existing `IslandChestBuilder.chestTilePanel` chrome unchanged for
the card body — same header (label + Link/Take buttons), same item
cells (`itemSlotCard` primitive), same drag/drop wiring. The only
difference vs. today: cards lay out in a **horizontal flow** sized to
the strip's content area, no atlas position.

Non-proximate chests inside the active area stay rendered at full grid
(dimmed) — same as today's atlas behaviour.

## Search

Embedded input at the right end of the tab strip:

- Filters chest contents by item id + display name across all areas.
- While the query is non-empty, a virtual "Results · N" tab activates
  and shows matching chests grouped by their parent area (small label
  badge on each card identifying which area it belongs to).
- Clear button resets the query and falls back to proximity / manual
  active selection.

## Behaviors

### Drag interactions (carry over unchanged)

The wiring in `chestTilePanel` and `installChestTileDropTarget` doesn't
care whether the card is on a canvas or in a flex container. All these
flows keep working:

- Carried stack → drop on chest card → deposit-to-chest.
- Chest stack → drag onto hotbar slot or atlas island → re-home / move.
- Chest card → drag onto a tab in the strip → `moveChestToArea`.

### Cross-surface highlighting (replaces link threads)

The atlas currently draws colored threads from islands to their linked
chest tiles. Off-canvas chests can't have threads. Replacement:

- **Per-island presence badges** on island title bars. Render
  `AtlasItem.presence` as a small text strip ("Tools · 24 Main Base · 8
  Mountain Mine"). Data is already populated with area metadata since
  Phase 5 of `storage-areas.md`.
- **Hover hand-off.** New `host.hoveredStorageId` mirroring the existing
  `host.hoveredIslandId`:
  - Hovering an island outlines its linked chest cards in the active
    area (and pulses the tab if the linked chests are in a different
    area).
  - Hovering a chest card highlights linked islands on the atlas (using
    the existing highlight frame).
  - Per-frame visibility flip without rebuild — same pattern as
    `attachIslandHoverListeners`'s dim-thread machinery.

### Link affordance

The chest card's existing Link button keeps working — opens the
`chestLinkPopover` infrastructure, which lists islands. No change.

## Atlas changes

Remove from canvas (now strip-resident or replaced):

- Area chips (`IslandChestBuilder.storageAreaChip`)
- Per-area backdrop + header (`storageAreaHeader`)
- Chest tile rendering loop in `AtlasPanelBuilder.buildAtlas`
- Link threads + dim threads
- Storage-zone bounds + drag source (`storageZoneBounds`,
  `installStorageZoneDragSource`)

The atlas becomes islands-only. Carried items still live on islands as
they do today.

## Client state

Per-session, on `SlotWorkspaceUiController`:

- `activeStorageAreaId: String?` — manual-override active tab; null
  means "follow proximity"
- `storageSearchQuery: String` — current query (non-empty wins over
  manual + proximity)
- `hoveredStorageId: String?` — cross-surface highlight cursor

`expandedAreaIds` (from the previous canvas-side plan) goes away — the
single-active model replaces it.

Nothing new server-side. Workflow domain unchanged.

## Vestigial data

After this change, the following fields are unused by rendering. Keep
them to scope the diff; clean up in a follow-up after the panel has
soaked:

- `StorageArea.atlasX`, `atlasY`
- `ClaimedChest.atlasX`, `atlasY`
- `ClaimedChestTile.atlasX`, `atlasY`, `width`, `height`
- `StorageZoneAutoPlacement` (claim no longer needs an atlas seed
  position)

`displayOrder` on `StorageArea` stays — it's still used to order tabs
in the strip.

## Implementation slices

1. **`StoragePanelBuilder`** (new file) — tab strip + active-area
   chest-card flow + search input. Reuses `chestTilePanel` for cards.
   Reads `host.activeStorageAreaId` + `storageSearchQuery` +
   `viewModel.storageAreas()`.
2. **Workspace layout** — wire the strip into the workspace root above
   the atlas; reflow the existing top-left search-hint overlay (drop
   it; the strip's search input replaces it) and adjust the top-right
   actions overlay's `.top()` if the strip needs the space.
3. **Atlas cleanup** — gut storage iteration / chip rendering /
   link-thread loops from `AtlasPanelBuilder.buildAtlas`. Remove the
   now-unused storage-area helpers in `IslandChestBuilder`. Drop
   `expandedAreaIds` from the host.
4. **Per-island presence badges** — render `AtlasItem.presence` as a
   strip on island title bars. Data is already there.
5. **Cross-surface hover** — `hoveredStorageId` field + listeners on
   chest cards and island panels.
6. **Search** — filter logic in the panel builder + state on the host
   + virtual "Results" tab handling.
7. **Cleanup** (deferred) — drop vestigial `atlasX/Y` fields and
   `StorageZoneAutoPlacement` once the strip has soaked.

(1) + (2) + (3) deliver the core feature; (4) + (5) + (6) are polish;
(7) is post-soak housekeeping.

## Open questions

- **Strip vertical footprint when collapsed.** 24 wu starting estimate;
  may need tuning so tabs aren't cramped with the search input next to
  them.
- **Empty-state copy.** With zero areas: hide the strip entirely, or
  show a "No storage claimed yet — right-click a chest in world" hint
  inside the strip? Probably hide.
- **Tab overflow.** Horizontal scroll vs. `+N more` popover. Defer
  until we have a save with many bases to test against.
- **Multi-active "pin" gesture.** If single-active turns out to feel
  too restrictive in playtest, add a pin toggle to each tab (keeps it
  expanded alongside the active one). Not part of MVP; revisit after
  soak.
- **Chest card max width inside the strip.** Existing card is 152 wu
  wide. A row of 4 cards = 608 wu — fine on most screens, tight on
  small ones. Consider a `+N more` collapse if the active area has
  more cards than fit one row.

## What's intentionally out of scope

- A separate "search-first item-level view" (mental model #3, AE2-style)
  — interesting future direction but a different surface.
- Workstation-aware chest links (mental model #2 — chests linked to a
  furnace, smithing table, etc.) — relies on machinery we don't have.
- Reordering chests within an area by drag — defer to playtest demand.
- Sub-areas (mental model #1 — "smelting nook" inside "Main Base") —
  revisit if real bases need it.
