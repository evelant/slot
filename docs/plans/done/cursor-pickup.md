# Cursor Pickup — Vanilla Cursor Semantics on Wall Cards

> **Closed (2026-05-05).** Phases **A** (eager extract on left-click
> + cursor origin tracking), **B** (universal cancel + smart-deposit
> + drop targets), and **C** (retire the virtual cursor) all
> shipped 2026-05-04 in a single pass; network protocol bumped
> 23 → 24. **Phase D** (drag-distribute starting from a wall card)
> is explicitly **not pursued** — the two-press flow (click pickup
> then click-drag distribute) reads fine in playtest, the LDLib2
> → vanilla `QUICK_CRAFT` bridge needed to fold them into one
> press-drag-release isn't worth the complexity, and there's no
> demand signal. Don't pick this back up; if a single-gesture
> distribute ever becomes load-bearing, write a fresh plan.
>
> The single follow-up worth carrying forward — **extend shift+click
> on take to auto-deposit excess past the player's desired count**
> — moves out to `docs/plans/current.md § Queue` as a small standalone
> task. Everything else from this doc landed.

Refines the wall card's left-click behaviour: today it sets a no-op
`selectedAtlasIdentity` highlight and the only real "move to cursor"
gesture is the virtual split-cursor (ctrl+right-click pickup-half).
This plan makes left-click eagerly extract the underlying real
`ItemStack` onto `menu.getCarried()` so vanilla's full click / drag
grammar — drop-all, drop-one, drag-distribute, shift-click quick-move,
crafting-table use — works against wall cards with no new client-side
state. Right-click becomes a universal **cancel** (return cursor to
its source) so eager-from-chest pickups can be reversed cleanly into
the chest rather than silently dumped into player inventory.
Re-home stays drag-only — clicks never re-home. Retires the virtual
cursor (`WorkspaceCursorCarry` / `WorkspaceCursorGestures`) in the
same pass and demotes "selected" to a derived signal (cursor identity
OR last-dropped identity).

Sibling to [list-view.md](list-view.md) and
[list-view-phase-3a.md](list-view-phase-3a.md) (which built the
sidebar embed and the cross-surface drag in 3a.2.A). Reuses
`CarriedSourceAccess` and `Slot.safeInsert` from 3a.2.A.

## Why

The wall card's plain left-click does almost nothing. It paints a
selection chrome, used by no gesture, dropped at the slightest excuse.
Players acting on a wall item have to switch modality:

- **For "I want to use this in a crafting table"**: open chest GUI →
  shift-click into inventory → close → open table → shift-click into
  grid. Or use SLOT's shift+click to QuickMove if sidebar is mounted —
  but only one stack at a time, no distribute.
- **For "I want to fill 4 furnaces with coal"**: 4× shift+click cycles
  with no distribute gesture.
- **For partial moves**: ctrl+right-click pickup-half via the virtual
  cursor, then click on a virtual drop target, all entirely outside
  vanilla's protocol.

Vanilla already has the right vocabulary: cursor pickup, left-drag
distribute, right-drag one-each, shift-click, drop-one. Wall cards
just need to participate. Once an item is on the real cursor, the
host menu's slot click handlers (vanilla, modded, machines) handle
everything else.

The key behavioural unlock: **eager extract from proximate chests**.
Left-click a wall card and the item arrives on cursor regardless of
whether it lives in carry, in a backpack, or two blocks behind you in
a chest you haven't opened. Standing in front of a crafting table with
three storage chests around you, every item across all four containers
behaves like one inventory. That's the tedium-killing case.

## What changes (semantics)

The full input vocabulary while carrying. **Re-home is drag-only.**
Click never re-homes; clicks that don't have a more specific
destination route to the identity's home (or Triage for unhomed
identities) instead. Right-click is a universal **cancel** — return
the cursor stack to its source — except over a vanilla crafting /
machine slot, where vanilla's drop-one wins. The cancel rule is
critical because we eagerly auto-withdraw from nearby chests on
pickup; a confused or aborted gesture must put those items *back into
the chest*, not silently dump them into the player's inventory.

### Cursor origin tracking

Every SLOT-initiated cursor pickup stamps an **origin** server-side:
`(SourceKind, sourceId, slotIndex)` where `SourceKind ∈ {CARRY,
BACKPACK, CHEST, HOST_SLOT}`. Stored on `SlotWorkspaceUiSession`
keyed by `player.containerMenu` instance. Cleared on any drop or
cancel, reset on the next pickup.

When `menu.getCarried()` is non-empty but the origin tracker is empty,
vanilla put the cursor there directly (player clicked a vanilla slot
without going through SLOT). In that case cancel routes through the
**smart-deposit path** (below): satisfy desired-count gap → proximate
chest with affinity → home / Triage.

### Universal click semantics

| # | Click context | Behaviour |
|---|---|---|
| 1 | Right-click anywhere *except* a vanilla craft/machine slot, while carrying | **Cancel**: route cursor back to origin (or smart-deposit if origin was a craft/machine slot) |
| 2 | Left-click a wall card with a *different* identity, while carrying | Cancel cursor (per 1), then eager-extract the clicked identity |
| 3 | Right-click a vanilla craft/machine slot while carrying | Vanilla drop-one (cursor decrements by 1, slot count +1) |
| 4 | Left-click a wall card while empty | Eager-extract → cursor (carry → backpack → proximate chest by affinity) |
| 5 | Left-click a proximate chest card while carrying | Deposit cursor stack into that chest (same as today's chest chip drop) |
| 6 | Left-click an untracked chest card while carrying | Claim chest + deposit cursor stack |
| 7 | Left-click a vanilla craft/machine slot while carrying | Vanilla drop-all / merge / swap |
| 8 | Left-click anywhere else while carrying (empty UI region, Triage row, search results, etc.) | Smart-deposit: satisfy desired-count gap → proximate chest with affinity → home → Triage |

The smart-deposit path (rows 1, 8) is mostly equivalent to today's
shift+click: route through the deposit planner with home and Triage
as fallbacks. **Follow-up adjacent to this plan**: extend shift+click
on take to auto-deposit excess to proximate chests with affinity when
the take exceeds the player's desired-count for the identity. Same
"satisfy desired count then store" rule, dual-pole.

### Wall card gesture table

| Gesture | Cursor empty | Cursor non-empty, same identity | Cursor non-empty, different identity |
|---|---|---|---|
| left-click | eager extract → cursor (row 4) | merge: extract more from ranked source up to stack max | cancel + pickup (row 2) |
| right-click | context menu | cancel (row 1) — back to source | cancel (row 1) — back to source |
| ctrl+right-click | extract half → cursor | merge half | cancel (row 1) |
| shift+left-click | unchanged: cross-surface QuickMove (sidebar) or take-from-chest (standalone) | unchanged | unchanged |
| shift+wheel | unchanged | unchanged | unchanged |
| drag wall→wall (different section) | unchanged: re-home via existing drag handler | unchanged | unchanged |
| drag wall→vanilla slot | unchanged: drop-on-host-slot RPC (3a.2.A path) | unchanged | unchanged |

**Ranked source resolution** (server, all extract paths): carry →
backpacks (via `CarriedSourceAccess`) → proximate chests in
`ChestAffinityMap` order. First source with `count > 0` for the
identity wins. Same ranking `takeStackByIdentity` already uses; the
change is the destination — `menu.setCarried(extracted)` instead of
inserting into player inventory — and the origin stamp.

**Cancel paths** (the right-click universal):

- Origin = `CARRY` / `BACKPACK`: try `CarriedSourceAccess.insert` at
  the original `(sourceId, slotIndex)`; if full or rejected, fall
  through to `insertBestFit` over the same source.
- Origin = `CHEST`: try the original chest's `Slot.safeInsert` at
  `slotIndex`; if rejected, walk the chest's other slots; if the
  chest is gone (broken / unloaded), fall through to smart-deposit.
- Origin = `HOST_SLOT` (cursor was picked up from a vanilla
  craft/machine slot via SLOT — rare: vanilla pickup typically goes
  through vanilla, but if we ever wire a SLOT path that pickups from
  a host slot, this branch handles it): smart-deposit.
- Origin missing (cursor was set by vanilla directly): smart-deposit.

**Smart-deposit path** (rows 1, 8 fallback; cancel from a
craft/machine origin):

1. **Satisfy desired-count gap** in carry first. If the player has a
   global or kit-scoped `desiredCount > carried` for the identity,
   insert into player inventory up to the gap.
2. **Deposit to proximate chest with affinity** for the identity (in
   `ChestAffinityMap` rank order; positive score required — falls
   through if no chest has memory for this identity).
3. **Deposit to homed chest** if the identity has an externally-homed
   wall card.
4. **Deposit to home section** if the identity is internally-homed —
   re-uses the existing home-routing logic.
5. **Triage** — last-resort: deposit into Triage's holding container
   (or whatever Triage uses today; verify during implementation).

The five-step cascade is the same logic the existing deposit verb
runs end-to-end; reuse `DepositPlanner` and `DepositExecutor` rather
than reimplement.

### Other surfaces touched by the virtual cursor

These currently consume `host.cursor` and need to be rewritten to
either (a) translate to vanilla `menu.clicked(...)` so vanilla handles
the cursor, or (b) call new "drop real cursor" RPCs.

- **Hotbar slot click ([BeltPanelBuilder.java:256+](../../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/BeltPanelBuilder.java)).**
  Translate to `menu.clicked(slotIndex, button, ClickType.PICKUP)` /
  `ClickType.PICKUP_ALL` / `ClickType.QUICK_CRAFT`. Vanilla's slot
  protocol handles all cursor pickup / drop / merge / split. The belt
  widgets keep rendering on top, but click events fan out to the
  underlying vanilla slot.
- **Chest chip drop ([StoragePanelBuilder.java:292+](../../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/StoragePanelBuilder.java)).**
  Today: virtual cursor drops a partial stack via `cursorDropToChest`
  RPC + `DepositExecutor.depositPartialStack`. Replace with: while
  `menu.getCarried()` is non-empty, left-click on a chest chip fires
  `dropCursorIntoChest(storageId)` RPC (rows 5 / 6 of the universal
  table). Server takes `menu.getCarried()`, `Slot.safeInsert`s into
  the chest's slots, sets cursor to the leftover, clears origin
  stamp on success. Untracked chest chip claims first then deposits.
  Right-click on a chest chip is **cancel** (row 1) — not a "drop
  one" gesture.
- **Loot chest panel rows ([LootChestPanelBuilder.java](../../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/LootChestPanelBuilder.java)).**
  Same as chest chip — left-click deposits to that chest, right-click
  cancels.
- **Triage row click ([TriagePanelBuilder.java](../../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/TriagePanelBuilder.java)).**
  Today sets selection. Demote: empty-cursor row click extracts to
  cursor like a wall card; non-empty cursor row click is row 8 of
  the universal table — smart-deposit (the cursor stack heads to its
  own home / Triage, *not* to the clicked Triage row's identity).
- **Workspace overlays ([WorkspaceOverlays.java:444+](../../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/WorkspaceOverlays.java)).**
  Renders the virtual cursor. Delete; vanilla renders its own cursor.

### Root-level click handlers

Two new root-level click listeners on the workspace `ModularUI` root
to satisfy rows 1 and 8 of the universal table:

- **Right-click anywhere while carrying** (cancel): bubble-phase
  listener on the root that fires after specific handlers; if no
  child stopped propagation and `menu.getCarried()` is non-empty,
  fire `sendCursorCancel()`. Skip if the click landed on a vanilla
  slot — vanilla's `mouseClicked(slot, 1, ClickType.PICKUP)` handles
  drop-one (row 3).
- **Left-click on no specific target while carrying** (smart-deposit
  / row 8): same shape — bubble-phase listener; if unhandled and
  carrying, fire `sendCursorSmartDeposit()`. Vanilla slot clicks
  short-circuit this path (vanilla owns row 7).

Both RPCs share server logic with the cancel path above (same
five-step cascade for smart-deposit; same origin-routed insert for
cancel).

### Selected state

Today: `selectedAtlasIdentity` is set on every left-click and consumed
by chrome rendering in `AtlasCardBuilder` / `TriagePanelBuilder` /
`LootChestPanelBuilder` plus a few keyboard nav paths.

After: derive selection from
`activeIdentity = cursorIdentity ?? lastDroppedIdentity`. Cursor
identity is `menu.getCarried()` — observable via tick subscription
since the carried stack is part of menu sync. Last-dropped identity is
a new `Observable<IdentityRef>` set whenever a drop RPC succeeds
(server tells client "you dropped X onto Y", client stashes X).

The selection visual chrome doesn't change shape — we just feed it
from a different source.

## What's going

- `WorkspaceCursorCarry.java` — virtual cursor state model.
- `WorkspaceCursorGestures.java` — virtual cursor click classifier.
- `cursorDropToHotbar` RPC + `WorkspaceRpcDispatcher` emitter +
  `SlotWorkspaceUiSession.cursorDropToHotbar` server handler.
- `cursorDropToChest` RPC + emitter + server handler.
- `DepositExecutor.depositPartialStack` (no remaining callers after
  the chest chip rewrite).
- `host.cursor` field on `SlotWorkspaceUiController`.
- All `host.cursor.*` and `cursor.isCarrying()` callers (about 20
  sites; grep confirms scope).
- The `selectedAtlasIdentity` direct setters in
  `AtlasCardBuilder.installCardClickHandlers`,
  `TriagePanelBuilder`, `LootChestPanelBuilder`, and the
  `WorkspaceRpcDispatcher` post-action setters. Replaced by
  `lastDroppedIdentity` + cursor observation.
- The `WorkspaceOverlays` virtual-cursor render path.
- The cursor section of [../design/gestures.md § 1](../../design/gestures.md)
  — entire split-cursor gesture vocabulary retires; rewrite as a
  pointer to vanilla protocol + this plan's wall-card extensions.

## What's staying

- `CarriedSourceAccess` and `StorageAccessRegistry` — the
  ranked-source resolution this plan depends on.
- Drag-from-wall-card-to-vanilla-slot (3a.2.A path). Now redundant
  with click-pickup + click-drop, but kept as a single-gesture
  shortcut: most muscle-memory players will press-drag-release rather
  than click-move-click.
- Shift+click and shift+wheel cross-surface gestures
  ([AtlasCardBuilder.java:621](../../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/AtlasCardBuilder.java)).
  These live in our widget's MOUSE_DOWN handler, not vanilla's slot
  pipeline. They are independent of cursor state and survive intact.
- Right-click context menu on wall card (bound on
  `event.button == 1`) — same handler, runs only when cursor is empty.
- `desiredCount` ctrl+scroll on wall cards.
- `selectedHotbarIndex` (separate from `selectedAtlasIdentity`,
  drives keyboard nav between hotbar and wall).

## Phases

### Phase A — Eager extract on left-click + cursor origin tracking (additive)

Both cursors coexist briefly. Wall card left-click newly extracts to
`menu.getCarried()`; the virtual cursor still works for split-pickup
on hotbar slots.

- New RPC `pickupToCursor(itemId, comparisonMode, fingerprint, count)`
  on `WorkspaceRpcDispatcher`. Server (`SlotWorkspaceUiSession`):
  resolves identity, walks carry → backpacks → proximate-chest-by-
  affinity using the same logic as `takeByIdentity` but extracts
  directly to `menu.setCarried(extracted)` AND stamps origin
  (`SourceKind`, `sourceId`, `slotIndex`) on the session. Caps at
  the stack's max size and at remaining capacity in the cursor (if
  non-empty + same identity). Source is `CarriedSourceAccess.extract`
  for player sources, `Slot.remove` (or container manipulation
  through `TakeAllExecutor`) for chests.
- `count` parameter: `Integer.MAX_VALUE` for full, the stack's
  half-size for ctrl+right-click pickup-half. Server clamps.
- Cursor origin tracker: per-`SlotWorkspaceUiSession` field
  `CursorOrigin` (record). Set on every SLOT-initiated pickup;
  cleared on drop / cancel success; reset on next pickup. Cleared
  also when `menu.getCarried()` becomes empty via a vanilla path
  (detect by polling at tick start: if cursor is empty but origin
  is set, clear origin).
- Wire `installCardClickHandlers` in `AtlasCardBuilder`:
  - left-click + cursor empty → `sendPickupToCursor(identity, MAX)`
  - left-click + cursor matches identity → `sendPickupToCursor` with
    remaining capacity (clamped server-side anyway)
  - left-click + cursor different identity → Phase A: fall through
    to old "selected" behaviour with a TODO log; Phase B replaces
    with cancel-then-pickup (row 2).
  - ctrl+right-click + cursor empty → `sendPickupToCursor` with half
- Server returns success/failure status via the existing `status` /
  `diagnostics` channel. Status message after pickup names the
  source ("picked up 64 coal from Mining Crate") so eager-from-chest
  isn't a black box.
- Visual: cursor identity drives the wall card's "active" chrome via
  a new tick subscription that reads `menu.getCarried()`. Selected
  chrome retained for `lastDroppedIdentity` (separate observable,
  populated by Phase B drops).
- Sidebar + standalone both use `player.containerMenu` so the same
  code path serves both surfaces.

**Acceptance:**

- Empty cursor, click wall card with item in carry → cursor holds
  the carried stack, slot in inventory now empty.
- Empty cursor, click wall card with item only in a backpack →
  cursor holds the stack pulled from the backpack.
- Empty cursor, standing next to a chest, click wall card with item
  only in that chest → cursor holds the stack pulled from the chest.
- Status message names the source after each pickup.
- Crafting table open in sidebar mode, click wall card → drag cursor
  across crafting grid (vanilla QUICK_CRAFT) → distributes correctly.
- Shift+click and shift+wheel still work.
- `:common:test :neoforge:test` green.

### Phase B — Universal cancel + smart-deposit + drop targets

Wire the universal click table (rows 1–8). After this phase the
virtual cursor has no remaining consumers.

- **Right-click universal cancel (row 1).** Bubble-phase listener on
  the workspace root. New RPC `cursorCancel()`. Server reads
  `menu.getCarried()` and the stamped origin, then routes:
  - `CARRY` / `BACKPACK` → `CarriedSourceAccess.insert` at original
    `(sourceId, slotIndex)`; fall through to `insertBestFit` on the
    same source if rejected.
  - `CHEST` → original chest `Slot.safeInsert` at `slotIndex`; walk
    other slots if rejected; fall through to smart-deposit if the
    chest is gone.
  - `HOST_SLOT` → smart-deposit.
  - origin missing → smart-deposit.
- **Smart-deposit (rows 1 fallback, 8).** New RPC
  `cursorSmartDeposit()`. Server runs the five-step cascade:
  desired-count gap fill → proximate chest by affinity → homed chest
  → home section → Triage. Reuses `DepositPlanner` /
  `DepositExecutor`. Clears cursor + origin on full success; leaves
  remainder on cursor with origin preserved if any step rejects.
- **Left-click on no specific target (row 8).** Same root-level
  bubble-phase listener as cancel; left-button variant fires
  `cursorSmartDeposit()`. Vanilla slot clicks short-circuit (vanilla
  owns row 7).
- **Hotbar drops (row 7 for hotbar in standalone).** Belt panel
  click handlers translate to `menu.clicked(slotIndex, button,
  ClickType.PICKUP)`. Vanilla handles cursor merge/swap/split. For
  sidebar mode, the belt widgets sit on top of the vanilla 36-slot
  band but the hotbar's vanilla slots are the same slot indices in
  `menu.slots`. For standalone (`InventoryScreen`), same thing.
- **Chest chip drops (rows 5, 6).** New RPC
  `dropCursorIntoChest(storageId)`. Server takes
  `menu.getCarried()`, `Slot.safeInsert`s into the chest's slots,
  sets cursor to leftover. Untracked-chest chip claims first
  (existing claim flow) then deposits. Right-click on a chest chip
  is cancel (handled by the row-1 root listener — chest chip's own
  click handler doesn't intercept right-click).
- **Loot chest panel drops.** Same shape as chest chip but takes a
  specific chest slot index — left-click on a loot row = put cursor
  stack into that slot via `Slot.safeInsert`. Right-click cancels.
- **Wall card drop with different identity (row 2).** Wall card
  left-click handler with non-empty cursor + different identity:
  call `cursorCancel()` then `sendPickupToCursor(clickedIdentity,
  MAX)`. Sequence is two RPCs; if the cancel partially fails (some
  cursor stack stuck), the pickup still happens but cursor merges
  with whatever's left (different identity = pickup denied; same
  identity = merge). Implementation can collapse to a single
  `cursorSwap(clickedIdentity)` RPC if the round-trip cost matters.
- **Vanilla craft/machine slot clicks (rows 3, 7).** No-op for SLOT
  — vanilla handles them. We just need to make sure nothing in the
  workspace mounts on top of host menu slots and intercepts events
  (the sidebar widget already lives left of the host menu, so it
  doesn't).
- **Last-dropped identity tracking.** Every drop RPC's success
  response sets `host.lastDroppedIdentity` so the selection chrome
  has a non-empty source after Phase A's "selected" demotion.
- **Triage row click (cursor non-empty)** = row 8 (smart-deposit
  applied to the cursor's identity, not the row's). Triage is *not*
  a drop target by click; only drag-onto-Triage re-homes there.

**Acceptance:**

- Cursor non-empty + right-click on wall card → stack returns to
  source (test all four origins: carry, backpack, chest, host slot).
- Cursor non-empty + right-click in empty UI region → cancel.
- Cursor non-empty + left-click on different wall card → first
  cursor returns to source, then new identity extracts.
- Cursor picked up from chest, right-click → returns to **that
  chest**, not to player inventory.
- Cursor picked up from chest, original chest broken or unloaded
  before cancel → falls through to smart-deposit (proximate chest
  with affinity, then home, then Triage).
- Cursor non-empty + left-click on chest chip → stack lands in
  chest, leftover stays on cursor.
- Cursor non-empty + left-click on hotbar slot → vanilla
  merge/swap/split.
- Cursor non-empty + left-click on empty UI region → smart-deposit
  cascade observed via diagnostics.
- Crafting table open + cursor holds wheat picked up from a
  proximate chest + right-click on the crafting result slot → vanilla
  drop-one (row 3 wins over row 1 because target is a vanilla slot).
- All previous tests still green; no `host.cursor` references in
  click handlers (only in retirement code).

### Phase C — Retire the virtual cursor

Pure deletion. Compile-time-driven; nothing should still touch
`host.cursor` after Phase B.

- Delete `WorkspaceCursorCarry`, `WorkspaceCursorGestures`,
  `host.cursor` field, `cursor` constructor arg if any.
- Delete `cursorDropToHotbar` / `cursorDropToChest` RPCs +
  emitters + server handlers + payload codecs.
- Delete `DepositExecutor.depositPartialStack` if no callers.
- Delete `WorkspaceOverlays` virtual-cursor render path.
- Update `[gestures.md § 1](../../design/gestures.md)`: replace the
  split-cursor section with a "wall card click semantics" section
  pointing to this plan.

**Acceptance:**

- `grep -r "host.cursor\|WorkspaceCursorCarry\|WorkspaceCursorGestures"`
  returns no matches in `neoforge/src/main`.
- `:common:test :neoforge:test` green.
- Manual playtest: all gestures from Phase A / B work; nothing
  references the old virtual cursor visually or behaviourally.

### Phase D — Drag-distribute starting from a wall card *(investigation)*

Phase A enables click-pickup. Vanilla's QUICK_CRAFT (left-drag-
distribute / right-drag-one-each) requires a non-empty cursor at
mouse-down on a slot. Today the player would have to: click wall
card (pickup), then click-drag across slots (distribute) — two
gestures.

A single press-drag-release from wall card across host slots would
need: MOUSE_DOWN on wall card synchronously extracts to cursor AND
the LDLib2 → vanilla event bridge passes the still-held mouse to
vanilla's slot pipeline so QUICK_CRAFT_BEGIN fires when the cursor
crosses the first slot. The bridge details aren't trivial — vanilla's
QUICK_CRAFT state is owned by `AbstractContainerScreen`, set in
`mouseClicked` on a slot, not from a `mouseDragged`.

Defer until Phase A + B + C ship and the two-press flow is felt under
playtest. If it's annoying, this becomes a focused investigation;
otherwise it stays unbuilt.

## Resolved decisions

- **Re-home is drag-only.** No click path re-homes. Confirmed.
- **Right-click while carrying = cancel everywhere except a vanilla
  craft/machine slot.** Critical because eager-from-chest pickup
  must reverse to the chest, not silently dump to player inventory.
  Right-click on a vanilla slot stays vanilla drop-one (row 3) so
  muscle memory at crafting tables / machines is preserved.
- **Left-click on a wall card while carrying a different identity =
  cancel + pickup.** No re-home, no swap, no deny — just sequential
  cancel-then-extract.
- **Smart-deposit cascade order** (used for cancel from a host-slot
  origin and for left-click on no specific target): desired-count
  gap fill → proximate chest with affinity → homed chest → home
  section → Triage.
- **Status message after pickup names the source chest** so
  eager-from-chest isn't a black box. Wayfinding chip on the cursor
  identity for ~2s post-pickup is nice polish, deferred.

## Open questions

- **Right-click on the crafting result slot.** Vanilla pickup from
  a result slot is special — the recipe consumes inputs only on
  pickup. Cancel-after-pickup-from-result-slot can't cleanly return
  the crafted item to the result slot (recipe already fired). Likely
  treat result-slot origin as `HOST_SLOT` and use smart-deposit on
  cancel — confirm this matches user expectation during playtest.
- **Cancel race with vanilla container changes.** Between pickup and
  cancel, the source chest may have been opened by another player or
  modified by a hopper. The cancel path tries the original slot
  first then walks the chest then falls through to smart-deposit, so
  the *item* always lands somewhere reasonable. No crash risk; just
  occasional "back to a different slot than expected." Probably
  fine; flag if playtest shows confusion.
- **Origin clearing when vanilla empties the cursor.** Vanilla can
  drain the cursor outside SLOT's RPCs (player clicks a vanilla slot
  with cursor non-empty, vanilla drops it). Tick-start poll: if
  `menu.getCarried().isEmpty()` and origin is set, clear origin.
  Detection latency = one tick (50ms); harmless.

## Follow-up adjacent to this plan

- **Extend shift+click on take to auto-deposit excess.** When the
  player shift+clicks (or shift+wheels) to pull from a proximate
  chest and the resulting carried count exceeds their desired-count
  for the identity, auto-deposit the excess to a proximate chest
  with affinity (same as smart-deposit step 2). Mirrors the cancel
  path's "satisfy desired count then store" rule on the take side.
  Not part of this plan's phases but blocks on the same
  `DepositPlanner` reuse.

## Pointers

- Vanilla cursor source-of-truth:
  `net.minecraft.world.inventory.AbstractContainerMenu#getCarried` /
  `setCarried`. Synced via the menu's normal sync packet.
- Existing eager-extract precedents:
  [SlotWorkspaceUiSession.crossSurfaceDropOnHostSlot](../../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceUiSession.java)
  (3a.2.A — extracts from carry/backpack via `CarriedSourceAccess`,
  inserts into a host slot via `Slot.safeInsert`). The same pattern
  but the destination is `menu.setCarried(extracted)` instead of a
  slot.
- Existing identity-resolved chest extract:
  [SlotWorkspaceUiSession.takeByIdentity](../../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceUiSession.java).
  Reuse the proximate-by-affinity walk, change the destination.
- Archived legacy-authority code in
  [archive/legacy-authority/.../CursorPickupOperations.java](../../../archive/legacy-authority/neoforge/src/main/java/dev/imagio/slot/neoforge/network/CursorPickupOperations.java)
  did real-cursor manipulation in a previous prototype — reference
  only, don't import (different domain model).
