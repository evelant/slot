# Gestures: Partial-Stack Cursor and Desired Counts

Last updated: 2026-05-01

Status: split-cursor (#1) and desired counts (#2) are **shipped end to
end**, including kit-scoped scope, the bring-list merge, auto-fetch on
kit activation, and cleanup protection. The two features share an input
vocabulary — ctrl modifies what the gesture *means*, shift refines its
*amount* — so this doc is the central reference for both.

For the surrounding interaction model see [atlas.md](atlas.md) and
[kits.md](kits.md). For chest-side affinity routing see [storage.md](storage.md).

## Current gesture vocabulary (for reference)

Implemented today on atlas cards
([AtlasCardBuilder](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/AtlasCardBuilder.java)):

- **left-click** — select the card
- **shift + left-click** — take stack from proximate chests, or send to
  free hotbar slot if already carried
- **right-click** — open context menu
- **shift + scrollwheel up/down** — take / push one per wheel tick from
  proximate chests, scaled by accumulated wheel delta

What is missing:

- moving an arbitrary count (e.g. "12 of this 30-stack to chest A, the
  rest to chest B"). shift+scroll only works against the auto-target.
- expressing a standing intent ("always keep 64 of this in carry").

## 1. Partial-stack cursor (split-cursor mode)

### Goal

Let the player move arbitrary counts to specific targets without
re-implementing vanilla pickup-to-cursor. The cursor *looks* like vanilla
pickup (ghost item + count following the mouse) but is actually a UI
state — drops resolve to the same `transfer` RPCs SLOT already uses.
Whole-stack moves are handled by drag (already shipped), so the cursor
is only needed for *partial* moves.

### Pickup

- **ctrl + right-click** on a source slot → pick up half of that slot's
  current count onto the cursor.
- **ctrl + right-click again on the same slot** → cumulative: each
  subsequent pickup halves the source's *remaining* count and adds that
  to the cursor (vanilla right-click cumulative behaviour: 30 → 15 → 23
  → 27 → 29 → 30).
- **ctrl + right-click on a different source while carrying** → refused
  with a status message ("cursor already holds another item — drop or
  ESC first"); the cursor is single-origin per session.
- No `ctrl + left-click` for "pickup all" — drag covers the whole-stack
  case, and adding it would create two ways to do the same thing.

Pickup sources currently wired:

- **hotbar slot** — full pickup support (incl. cumulative).
- **atlas card** — full pickup support backed by a server-projected
  "largest carried slot" tuple on each `AtlasItem` (source id + slot
  index + count). Works for items in main, hotbar, offhand, or
  backpack. Refuses with a clear status when the identity isn't carried.

Pickup intentionally NOT wired:

- **chest chip** — chips are chest handles, not slot handles. No
  concrete source slot to halve.
- **storage panel slot** — panel doesn't expose per-chest-slot widgets.

### Drop

While the cursor is non-empty, clicks on a valid drop target are
interpreted as:

- **left-click** → drop **all** of cursor
- **right-click** → drop **1** of cursor
- **shift + right-click** → drop **half** of cursor (rounded up)

Drop targets currently wired:

- **hotbar slot** — drop merges into the slot. Self-drop (origin
  slot == target slot) cancels the cursor without a wire transfer.
- **chest chip** — drop deposits the chosen count into that chest;
  bumps affinity for the deposited identity.

Atlas cards and storage panel slots aren't drop targets in the initial
cut; clicking them while carrying cancels the cursor (same as clicking
empty space).

### Cancel

- **ESC** while carrying → cancel.
- **Click on any non-drop-target** → cancel. Implemented as a
  bubble-phase MOUSE_DOWN handler on the workspace root: drop targets
  call `event.stopPropagation()` when they handle a cursor click, so
  unhandled clicks reach root and trigger cancel.

Cancel is free of consequence: the cursor is virtual, so the source
slot still holds the items that were "picked up." Cancelling just clears
the client-side state.

### Server semantics

Each drop sends one of two RPCs that carry the cursor's origin slot
explicitly:

- `cursorDropToHotbar(originSourceId, originSlotIndex, identity, count, hotbarIndex)`
- `cursorDropToChest(originSourceId, originSlotIndex, identity, count, storageId)`

Server re-clamps `count` against the origin slot's actual current count
(handles the case where another mod or auto-refill upgrade mutated the
slot between pickup and drop). Drops to hotbar use TRANSFER + INSERT_ONLY
+ EXACT_COUNT; drops to chest use a new `DepositExecutor.depositPartialStack`
that extracts `count` from the source and inserts into the chest.

### Conflicts disabled while carrying

- **shift+scroll** on atlas cards is suppressed — that gesture mutates
  the origin slot mid-flight and would desync the cursor.
- **drag-from-atlas-item** and **drag-from-hotbar-slot** are suppressed
  — drag and cursor are alternative move paths and shouldn't overlap.

### Known gaps (follow-ups, not blocking)

- **Atlas card drop.** Drop on a card should "send to home" — deposit
  to the affinity-preferred chest if any, else merge into carry. Needs
  a count-aware variant of the existing send-home RPC. Currently a
  click on an atlas card while carrying just cancels the cursor.
- **Chest-side overflow handling.** Hotbar drops are clamped client-
  side against actual slot capacity + identity, so the cursor stays in
  sync. Chest drops still send the requested count without a
  capacity check (the server clamps but doesn't return the moved
  count); cursor may decrement past actual moved when a chest is full.
- **Origin highlight.** The origin slot doesn't visually highlight
  while the cursor is non-empty. Adding a subtle glow would help the
  player remember where the items came from.

## 2. Desired counts (standing-order pip)

### Goal

Let the player declare "I want to keep N of this item with me," so SLOT
can surface and act on the gap. The eventual replacement for the Kit
"bring" list — but the kit-scoped half of that story is deferred to a
follow-up; only the player-global standing order is shipped.

### Setting a desired count

- **ctrl + scrollwheel up/down** on an atlas card → adjust the desired
  count by ±1 per tick. Parallels the existing `shift + scrollwheel`
  (immediate take/push) so the gesture vocabulary stays symmetric:
  shift = "move N items", ctrl = "want N items."
- **right-click on an atlas card → "Set desired count…"** opens an
  inline TextField for numeric entry. Enter commits, ESC dismisses,
  "Clear" sets to 0. Used for precise values like "exactly 64."
- **Suppressed while the cursor is non-empty.** ctrl+scroll mid-cursor
  would silently mutate intent; the gesture is gated behind
  `!cursor.isCarrying()`.

### Scope: kit-active vs global

Two scopes are stored separately:

- **player-global** (`PlayerDesiredCountSet` event,
  `playerDesiredCounts` projection field): always-keep regardless of
  kit. Set when no kit is active.
- **kit-scoped** (`KitDesiredCountSet`, `kitDesiredCounts` keyed by
  kit id): override that wins while that kit is active.

Resolution rule (applied at view-model build): kit-scoped value wins
when a kit is active and has a non-zero entry; else fall back to
player-global. The same rule is exposed as
`DesiredCountWorkflowDomainService.resolved(KitMap, ItemIdentity)` so
server-side consumers don't reinvent it.

The legacy kit "bring" list was retired and folded into kit-scoped
desired counts: drag-onto-bring sets count=1, drag-out clears it. The
view-model still surfaces a `bring` list on each `KitCard` for UI
back-compat, populated entirely from kit-scoped counts > 0.

### Pip representation

Desired counts surface as a small numeric pip in the **bottom-right**
corner of the atlas card, with the count painted in TEXT colour on a
scope-keyed background:

- **DESIRED_COUNT_PIP_GLOBAL** (desaturated blue) — player-global
  scope is in effect (no active kit override).
- **DESIRED_COUNT_PIP_KIT** (warm amber) — active kit-scoped value
  is overriding the global.

Distinct from:

- top-right ACCENT-green pip = proximate chest stock count
- top-left WARNING-orange star = kit-needed indicator
- bottom-left search-only badge = also-stored count

The scope is decided server-side during view-model build (via the
resolution rule above) and shipped on the AtlasItem as
`desiredCountFromKit`.

### Behavior shipped

- Pip renders only when the resolved desired count > 0.
- ctrl+scroll routes to the active scope server-side: kit-scope when
  a kit is active, else player-global. Single ctrl+scroll on an atlas
  card always edits whatever the player sees on the card.
- Right-click "Set desired count…" same scope routing.
- Drag-onto-kit-bring-panel writes kit-scoped count = 1 (default
  seed); drag-out clears.
- Persistence: event-sourced via `PlayerDesiredCountSet` and
  `KitDesiredCountSet` events, snapshotted in
  `WorkflowCheckpointData.{playerDesiredCounts, kitDesiredCounts}`.
- **Auto-fetch on kit activation**: after the kit applies, the server
  walks proximate chests (highest-affinity first) for each kit-scoped
  desired identity and pulls up to the gap (`desired - currently
  carried`). Best-effort; partial fills leave the gap visible on the
  pip.
- **Cleanup protection**: identities with player-global desired count
  > 0 are protected from TRASH/VOID/DROP_TO_WORLD via
  `DesiredCountProtection`; kit-scoped identities are protected via
  the existing `KitActiveProtection` (extended to read kit desired
  counts in addition to belt + offhand).

### Still deferred

- **"Need N more" status text.** The pip shows the *target*, not the
  gap; no "want 64, have 12" surface yet.
- **Kit-scope vs global toggle in the menu.** Right-click currently
  edits the active scope only. Surfacing both with an explicit
  switcher is a UX refinement.

### Why this shape

ctrl+scrollwheel as a parallel to shift+scrollwheel piggybacks on
existing muscle memory: the player already knows "scroll = take/push
counts," now `ctrl` modifies the *intent* (standing) instead of the
*action* (immediate). Right-click numeric entry exists for the case
where the player knows the exact number (e.g. "exactly 64") and would
rather not scroll there.

Color-coded pips will solve the "what scope am I editing" confusion
without adding text once kit-scope ships — at a glance the player will
see whether this is a kit thing or a forever thing.

### Out of scope

- No per-chest desired counts (this is about player carry, not chest
  stock).
- No time-windowed counts ("only when mining at night"). Kit scoping
  is the only conditioning.
- No automatic crafting to satisfy desired counts (a separate concern;
  see crafting design when it arrives).

### Cleanup debt

Resolved: the legacy collection-scoped `WorkflowEvent.DesiredCountSet`
event, `CollectionWorkflowDomainService.setDesiredCount`,
`CollectionProjection.desiredCountsByCollection`, and the orphaned
`DesiredCount` record were deleted alongside the kit-scope rollout.
The legacy `DesiredCountSet` event tag in
`WorkflowDomainFileStore.decodeWorkflowRecord` resolves to `null` so
old event logs replay without the dead event.

## Open questions

- **Cursor-mode discoverability.** Tooltip on hover ("ctrl+right-click
  to split") may be enough; a one-time inline hint when the player
  first opens a chest could help. To be decided.
- **Pickup-half rounding.** `right-click pickup-half` of an odd stack
  — round up like vanilla, or down? Vanilla rounds up (half of 5 = 3);
  shipped implementation matches.
- **Desired-count pip layout under load.** With proximate-stock,
  kit-needed, also-stored, and desired-count badges all potentially
  visible, the corner placement is tight. May need a stacking rule
  when more than three are simultaneously present.
