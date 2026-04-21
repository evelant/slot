# Inventory Fullness Plan

Last updated: 2026-04-21

Status: proposed. Near-term UI slice to surface carried-inventory capacity
without exposing per-bag routing controls.

For the atlas design, see [../design/atlas.md](../design/atlas.md).
For the UI lifecycle rules this plan must follow, see
[../architecture/ui-lifecycle-rules.md](../architecture/ui-lifecycle-rules.md).
For the current operational baseline, see [../status.md](../status.md).

## Philosophy

Carried containers (Sophisticated Backpacks and future equivalents) are
**largely transparent** to the player. The pickup router and "send home"
flow already decide where items go; the player's job is to care about
items, not about which container holds them.

Because of that, this plan intentionally does **not** build:

- a per-bag strip, drawer, or inventory of inventories
- per-bag drag-and-drop routing controls
- a "send this item to bag X" override affordance

When the player needs that level of control (quest items that must be in
main inventory; mod-specific totem / charm / trinket behaviors; tools
that only activate from a specific container), they use the existing
**"Vanilla" button** escape hatch — we cannot reasonably cover every
third-party mod interaction and trying to will accumulate complexity
without commensurate value.

## Goals And Non-Goals

Goals:

- give the player a glanceable, always-correct answer to "can I pick up
  more stuff right now?"
- surface individual carried bags through the atlas (their natural home,
  since bags are items) with enough detail to let the player see which
  bag has room without leaving SLOT
- keep the default-state UI minimal: no new persistent chrome when the
  player has plenty of room

Non-goals:

- per-bag routing UI (see philosophy above)
- "force this pickup into bag X" overrides
- editing bag upgrades / filters (fall back to vanilla)
- changes to how `SlotPickupRouter` decides routing today
- inline contents preview of bag items (slot count only — vanilla UI
  handles actual contents inspection)

## Key Files

| File | Role in this plan |
|---|---|
| [common/.../inventory/workspace/SlotWorkspaceViewModel.java](../../common/src/main/java/dev/imagio/slot/inventory/workspace/SlotWorkspaceViewModel.java) | Add aggregate + per-item fields |
| [neoforge/.../screen/ldlib/SlotWorkspaceViewModelCodec.java](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceViewModelCodec.java) | Serialize new fields for S2C sync |
| [common/.../query/InventoryAuthoritySnapshot.java](../../common/src/main/java/dev/imagio/slot/inventory/query/InventoryAuthoritySnapshot.java) | Source of carriedSources / entries |
| [common/.../compat/sophisticated/SophisticatedBackpackInventoryIntegrationProvider.java](../../common/src/main/java/dev/imagio/slot/inventory/integration/SophisticatedBackpackInventoryIntegrationProvider.java) | Detects "this ItemStack is a carried container" |
| [neoforge/.../screen/ldlib/SlotWorkspaceUiFactory.java](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceUiFactory.java) | All UI additions (chip, pip, DETAIL stat) |
| [neoforge/.../screen/ldlib/SlotWorkspaceUiSession.java](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceUiSession.java) | `refreshServerView` / `SlotWorkspaceViewModel.project` entry point |
| [neoforge/.../test/.../SlotWorkspaceLdlibModelTest.java](../../neoforge/src/test/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceLdlibModelTest.java) | Existing test bed for ViewModel projection; extend for new fields |

## Data Model Changes

### `SlotWorkspaceViewModel`

Add a top-level aggregate field:

```java
int carriedFreeSlotCount;  // sum of empty slots across every CARRIED-pane source
```

### `AtlasItem`

Add two per-item fields:

```java
boolean isCarriedContainer;  // true when the item is itself a player-carried inventory
int containerFreeSlotCount;  // only meaningful when isCarriedContainer; else 0
```

Any record constructor change propagates to `SlotWorkspaceViewModelCodec`
(S2C tag read/write). Both fields are additive — default values
(`false`, `0`) are safe when sync data is truncated or comes from older
clients.

## Computation

### Empty-slot counting

An `InventoryAuthoritySnapshot` exposes:

- `List<InventorySourceDescriptor> carriedSources()` — every source with
  `InventoryPaneMembership.CARRIED`
- `int slotCapacity(sourceId)`
- `List<InventoryEntrySnapshot> entries(sourceId)` — each entry has a
  `slotIndex()` and a `present()` boolean indicating whether that slot
  is occupied

A source's empty-slot count is
`slotCapacity(id) - (number of present entries where slotIndex >= 0)`.
The aggregate is the sum over all sources returned by
`carriedSources()`.

`"Free slot"` means **empty slot** — a slot with no ItemStack. A slot
with a partial stack is not counted as free even though it can accept
more of its own item type; this is a deliberate under-count that makes
`"{N} free"` actionable ("can I pick up a new item type right now?").

### Detecting carried-container items

The existing
`SophisticatedBackpackInventoryIntegrationProvider.playerExtensions(...)`
already enumerates the player's carried backpacks via
`SophisticatedBackpackSupport.readPlayerBackpacks` and knows their
carrier ItemStacks. We reuse that:

- when `SlotWorkspaceViewModel.project` builds AtlasItems, expose a
  `Predicate<ItemStack>` from the integration provider (or a helper
  that takes an `InventoryHostDescriptor` and returns the set of
  carried-container identities for this player)
- an AtlasItem's `isCarriedContainer` is true iff its identity matches
  one of those carrier stacks

If the Sophisticated Backpacks reflection path is unavailable
(`SophisticatedBackpackSupport.isAvailable()` returns false), no
item gets the flag, the aggregate chip just sums
main/hotbar/offhand slots, and the container pip never renders.
Mod-absent behaviour is identical to before this plan.

### Per-bag free-slot count

For items where `isCarriedContainer` is true, `containerFreeSlotCount`
is the empty-slot count of the *specific backpack source* keyed off
that carrier's identity. The integration provider already exposes
per-bag inventory snapshots via
`SophisticatedBackpackSupport.BackpackInventorySnapshot` — use
`slotCount()` and iterate the entries.

**Known limitation**: two carried backpacks with the same identity
(same type, same NBT / components) are indistinguishable at the
AtlasItem level. If that surfaces in playtest, slice 4's per-bag
value falls back to the sum of those duplicates or a deliberate
`-1` sentinel; decide when it actually comes up.

## UI Pieces

### 1. Aggregate free-slots chip

Position: **top-center** of the workspace content (not inside the
atlas's scaled content; overlays layer, fixed screen pixels).

Reference patterns already in `SlotWorkspaceUiFactory`:

- `searchHintOverlay()` (top-left, absolute pixel positioning with
  `.top(10).left(10)`) is the closest sibling — same layer, same
  interaction-free overlay style.
- Centering against a container whose width isn't known at build
  time is done in the island header path by recomputing `.left(...)`
  in a TICK listener when the viewport size changes. Use the same
  pattern: compute `atlasPanelElement.getContentWidth() / 2 -
  estimatedChipWidth / 2` and re-apply `.left(...)` on layout
  changes via a `LAYOUT_CHANGED` or TICK handler. A plain
  `.top(10)` is fine.

Minimum styling to mirror:

```java
UIElement chip = panel(GLASS).layout(layout -> layout
        .positionType(TaffyPosition.ABSOLUTE)
        .top(10)
        .paddingHorizontal(8)
        .paddingVertical(4)
        .alignItems(AlignItems.CENTER)
        .flexDirection(FlexDirection.ROW));
chip.style(style -> style.zIndex(11));  // matches searchHintOverlay
chip.setAllowHitTest(false);
```

Leave `.left(...)` off at build time, then set it from a
LAYOUT_CHANGED or TICK handler that reads the atlas panel's current
width. A static width for the chip (e.g. 80 pixels) simplifies the
centering math and avoids measuring the label.

Content: `"{N} free"` using the i18n key
`slot.screen.inventory.free_slots` in `common/src/main/resources/assets/slot/lang/en_us.json`
with a `%s` placeholder.

Color bands (use existing constants where possible, define in
`SlotWorkspaceUiFactory` alongside `ACCENT`, `TEXT`, `MUTED`):

| Free slots | Background | Text |
|---|---|---|
| ≥ 10 | `GLASS` (current subtle overlay) | `TEXT` |
| 1–9 | new `WARN` (amber, e.g. `0xCCB48A3A`) | `TEXT` |
| 0 | new `DANGER` (red, e.g. `0xCCB44A3A`) | `TEXT` |

Refresh pattern: mirror the
**deposit-button per-TICK pattern** at
[SlotWorkspaceUiFactory.java:983-997](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceUiFactory.java)
— an `int[] lastValue = {...}` cache plus a TICK listener that reads
`viewModel.carriedFreeSlotCount()`, updates text + background color
only when the value changes, and never calls `rebuild()`.

Initial state primed at build time so the first frame already shows
the correct value (see lifecycle rule #4 in
[../architecture/ui-lifecycle-rules.md](../architecture/ui-lifecycle-rules.md)).

### 2. Container pip

Shown on atlas cards where `AtlasItem.isCarriedContainer` is true.
Rendered inside `addCommonAtlasSignals` (same place the proximity pip
is today) with `zIndex(260)` to match the proximity-pip convention
(required to overdraw item icons — see lifecycle rule on
`DrawerHelper.drawItemStack`'s +232 Z translate).

Visual: solid dot, distinct color from the existing pips. Existing pips
use `LINK_THREAD_COLOR` for proximity and `ACCENT` for selection; pick
a fourth distinct color — `CARRIED_CONTAINER_PIP = 0xCC5A7DB4` (cool
blue) is a starting suggestion. Revisit iconography (actual bag glyph)
if more overlays start competing for recognition.

Position on the card: top-left corner so it doesn't collide with the
existing top-right proximity pip. Size: match proximity pip
(`item.width() * 0.22f` clamped to `worldUnitsForPixels(10f)`).

### 3. DETAIL-LOD slot stat for bag cards

At DETAIL LOD only, bag cards (`isCarriedContainer` true) gain a
`"{N} free"` text band below the primary label, using the same i18n
key as the aggregate chip. No inline inventory grid.

Implementation: extend `detailAtlasBody` (in `SlotWorkspaceUiFactory`)
with a branch that, when the item is a carried container, appends an
`anchorTextBand` rendering `containerFreeSlotCount` with the same
sizing rules as the secondary label (`budget.secondaryFontPx()` etc.).

BROWSE / READ / INSPECT cards are unchanged — the stat only appears
at DETAIL, matching the "chest tile vision" LOD gating precedent for
richer per-item overlays.

## Prerequisites (landed)

- Sophisticated Backpacks carried-source integration (landed — this is
  what made bag items appear on the atlas at all via
  [SophisticatedBackpackInventoryIntegrationProvider](../../common/src/main/java/dev/imagio/slot/inventory/integration/SophisticatedBackpackInventoryIntegrationProvider.java)).
- `authority.carriedSources()` iteration replacing hardcoded player-
  lane lists everywhere (landed in the "atlas didn't show backpack
  items" fix — see `SlotWorkspaceViewModel.groupedAtlasEntries` and
  `carriedIdentities`).
- Rebuild-before-render flush at
  `root.TICK` (landed in the "flash on hotbar assign" fix; every new
  UI element in this plan relies on it).

## Slices

Slices are independently shippable. Each one's exit criteria can be
validated without waiting for later slices.

### Slice 1 — Aggregate free-slot count in the ViewModel

Scope:

- Add `carriedFreeSlotCount` to
  `SlotWorkspaceViewModel` (top-level field, all constructors /
  factory methods updated).
- Update `SlotWorkspaceViewModelCodec` to (de)serialize the new
  field in both S2C directions.
- Compute it inside `SlotWorkspaceViewModel.project(...)` using the
  algorithm in the *Empty-slot counting* section above.
- Extend `SlotWorkspaceLdlibModelTest` with cases:
  - empty main + no backpacks → expect 36 (27 main + 9 hotbar)
  - full main + no backpacks → expect 0
  - empty main + 2 empty backpacks (27 slots each) → expect 90
  - full main + 1 backpack with 10 items → expect 17
  - Sophisticated Backpacks unavailable (simulate via
    `SophisticatedBackpackSupport.isAvailable() == false`) → count
    equals vanilla empty slots only

Exit criteria:

- `carriedFreeSlotCount` matches a naive iteration over all carried
  slots across every test case.
- Removing or adding a backpack updates the count on the next sync.
- Test file passes; no behavioural changes visible in-game yet (UI
  slice 2 consumes this field).

### Slice 2 — Top-center aggregate chip

Scope:

- Add `CARRIED_CHIP_WARN` and `CARRIED_CHIP_DANGER` color constants in
  `SlotWorkspaceUiFactory` alongside `ACCENT`, `GLASS`, etc.
- Add `slot.screen.inventory.free_slots` i18n key to
  `common/src/main/resources/assets/slot/lang/en_us.json` with a `%s`
  placeholder.
- Build a `carriedFreeSlotsChip()` overlay creator returning a
  persistent UIElement.
- Prime initial state at build time, then use a TICK listener
  (mirroring the deposit-button pattern) to update text + background
  when `viewModel.carriedFreeSlotCount()` crosses a threshold.
- Add the chip to the atlas panel's persistent overlay set in
  `repopulateAtlasPanel` (or `rebuildNow`'s first-build path —
  whichever matches the "create once" pattern that landed with the
  persistent-content-panel fix).

Exit criteria:

- Chip always visible while the workspace is open.
- Text and background color correctly reflect the current free-slot
  count at rest.
- During rapid pickup bursts (shift-scroll transfer; 10+ pickups in a
  second), no flicker, no new rebuild pressure —
  verified by watching `SlotDebugLog` for rebuild-flush frequency.
- No visible collision with the top-left search hint.

### Slice 3 — Container pip on atlas cards

Scope:

- Add `isCarriedContainer` to `AtlasItem`, to the codec, and to the
  projection in `SlotWorkspaceViewModel.project(...)`.
- Derive it at projection time by asking
  `SophisticatedBackpackInventoryIntegrationProvider` (via a new
  `isCarriedContainerIdentity(ItemIdentity)` helper) whether the
  item's identity is a carried backpack for the active player.
- Add `CARRIED_CONTAINER_PIP` color constant in `SlotWorkspaceUiFactory`.
- In `addCommonAtlasSignals`, render the pip top-left when
  `item.isCarriedContainer()` is true, with `zIndex(260)` and size
  matching the existing proximity pip.
- Extend `SlotWorkspaceLdlibModelTest` to assert that items with
  carried-container identities get the flag set, and non-container
  items do not.

Exit criteria:

- Every carried bag visible on the atlas shows the pip at BROWSE and
  up; non-container items do not.
- Pip renders above the item icon (verify by selecting / zooming —
  lifecycle rule on MC item render Z still applies).
- Absent Sophisticated Backpacks, no pip anywhere — no errors in log.

### Slice 4 — DETAIL-LOD slot stat for bag cards

Scope:

- Add `containerFreeSlotCount` to `AtlasItem` + codec + projection.
- In projection, for each AtlasItem where `isCarriedContainer` is
  true, look up the specific `CarriedSource` for that bag's identity
  and compute its empty-slot count using the same algorithm as slice
  1 (but per-source, not aggregate).
- Extend `detailAtlasBody` (in `SlotWorkspaceUiFactory`) with a
  carried-container branch that appends an `anchorTextBand` showing
  `"{N} free"` below the primary label at DETAIL only.
- Respect lifecycle rules: use `atlas.getScale()` for the band's
  per-tick size updates; prime at build time.

Exit criteria:

- Zooming in on a bag card to DETAIL shows its free-slot count.
- Value refreshes next sync after pickups / transfers change the
  bag's contents.
- BROWSE / READ / INSPECT cards are unchanged.
- Two identical-identity bags still render correctly (value may be
  the combined count — document as the known limitation in the
  Risk Register).

## Risk Register

### 1. Free-slot aggregation performance

Summing across all carried sources every sync is O(total slots).
8 backpacks × 27 slots = ~215 reads per sync. Fine. But if each
read went through reflection into Sophisticated Backpacks, the
reflection cost could add up.

Mitigation: use the existing `authority.carriedSources()` snapshot
produced by `InventoryAuthorityReadService.serverAuthority(...)` —
it already contains each source's entries. Counting empty slots
from that in-memory snapshot is O(n) without extra reflection.

### 2. "Free slot" ambiguity

A partial stack of 32 feathers in a 64-cap slot has room for 32
more feathers but zero room for a different item type. Defining
"free" as "empty slot" is the simplest actionable interpretation.

Mitigation: document "empty slots only" in the ViewModel field's
javadoc and in the chip's future tooltip. Adopt consistently in
slice 1 and 4.

### 3. Identical-identity bags

Two carried backpacks with the same identity (same type, same NBT)
appear as a single AtlasItem. The per-bag `containerFreeSlotCount`
becomes ambiguous — combined vs. picking one arbitrarily.

Mitigation: **combined count** (sum of empty slots across all
matching carriers) is consistent with how AtlasItem's `totalCount`
aggregates. Document as a known limitation. Revisit if a future
"bag nicknames" or "bag filters" feature needs per-carrier identity.

### 4. Sophisticated Backpacks unavailable

If the mod isn't installed, `SophisticatedBackpackSupport.isAvailable()`
is false. All container detection paths return empty / false. The
aggregate chip degrades to showing main + hotbar + offhand empty
slots (still useful). No pip, no DETAIL stat. No runtime errors.

Mitigation: slice 1 test includes this case.

### 5. Threshold thrash

If `carriedFreeSlotCount` hovers at the 9↔10 boundary (pick up one
item, drop one item repeatedly), the chip's color would flap
between yellow and green every tick.

Mitigation: for now, no hysteresis — the count is discrete and
players triggering this boundary every second is a micro-edge case.
If playtesting shows flap is visible, add a 1-slot dead band
(yellow → green requires crossing 11; green → yellow requires
crossing 9).

## Resolved Questions

- **Chip placement** — top-center. Keeps the right edge clean for
  future JEI/EMI integration and doesn't crowd the top-left search
  hint.
- **Bag preview scope** — free-slot count only, no contents grid.
  Players who need to see actual bag contents use the vanilla escape
  hatch.
- **Container pip** — distinct color, no icon glyph for now.
  `CARRIED_CONTAINER_PIP = 0xCC5A7DB4` as a starting proposal;
  adjust during slice 3 based on how it reads against the current
  palette.

## Out-of-scope future work

Things this plan deliberately defers; record here so they don't
creep into the slices:

- tooltips on the chip / pip (would need a hover/tooltip system we
  don't have yet)
- "bag is nearly full" per-bag warning indicator distinct from the
  container pip
- hysteresis on color-band thresholds
- chip tooltip showing per-bag breakdown
- glyph for the container pip
- animation / pulse on the red (0 free) state
