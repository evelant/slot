# Gestures: Cursor Semantics and Desired Counts

Last updated: 2026-05-04

Status: the cursor model is now **vanilla `menu.getCarried()`** end to
end (cursor-pickup plan, shipped 2026-05-04 — see
[../plans/done/cursor-pickup.md](../plans/done/cursor-pickup.md)). The
previously-described virtual split-cursor (`WorkspaceCursorCarry`) was
retired in the same pass. Desired counts (#2) shipped 2026-05-01 and
are unchanged.

For the surrounding interaction model see [atlas.md](atlas.md) and
[kits.md](kits.md). For chest-side affinity routing see [storage.md](storage.md).

## 1. Cursor model (vanilla menu cursor)

The wall card's plain left-click eagerly extracts onto
`menu.getCarried()` — the real vanilla cursor. This unifies vanilla's
full click / drag grammar (drop-all, drop-one, drag-distribute,
shift-click quick-move, crafting-table use) with SLOT's identity
addressing. Right-click is a universal cancel that returns the cursor
stack to its tracked origin (so eager-from-chest pickups reverse
cleanly into the chest rather than dump into player inventory).
**Re-home is drag-only** — clicks never re-home; clicks that don't
have a more specific destination route through the smart-deposit
cascade.

The full universal click table while carrying:

| # | Click context | Behaviour |
|---|---|---|
| 1 | Right-click anywhere except a vanilla craft/machine slot | Cancel → origin (or smart-deposit if origin is gone / was a craft slot) |
| 2 | Left-click a wall card with a *different* identity | Cancel cursor, then eager-extract the clicked identity |
| 3 | Right-click a vanilla craft/machine slot | Vanilla drop-one |
| 4 | Left-click a wall card while empty | Eager-extract → cursor (carry → backpack → proximate chest by affinity) |
| 5 | Left-click a proximate chest card | Deposit cursor stack into that chest |
| 6 | Left-click an untracked chest card | Claim chest + deposit |
| 7 | Left-click a vanilla craft/machine slot | Vanilla drop-all / merge / swap |
| 8 | Left-click anywhere else (empty UI region, search results, etc.) | Smart-deposit cascade |

Smart-deposit cascade: desired-count gap fill → eligible proximate chest
with affinity or existing matching contents → home → Triage. Reuses
[`DepositPlanner`](../../common/src/main/java/dev/imagio/slot/inventory/workspace/DepositPlanner.java)
end-to-end.

Cursor pickup ranks sources server-side: carry → backpacks (via
`CarriedSourceAccess`) → proximate chests (by affinity, descending).
The server stamps a `CursorOrigin(kind, sourceId, slotIndex)` so
right-click cancel can route the stack back to the exact slot it came
from. ESC also fires cancel.

ctrl+right-click on a wall card or hotbar slot still extracts a half
stack, now via the same real-cursor RPC (`pickupToCursor` with
`count = stack.maxStackSize / 2`).

Drag from a wall card to a wall card / vanilla slot remains the
re-home / cross-surface path
([3a.2.A](../plans/done/list-view-phase-3a.md)). It is suppressed while
carrying — the universal click table covers movement while a stack is
on cursor.

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
  `!WorkspaceCursorState.isCarrying()`.

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
- ctrl+scroll routes to the scope behind the visible pip: active
  kit-scope when a kit override is visible, player-global when the card
  is showing the global fallback, and kit-scope for a new count while a
  kit is active. Single ctrl+scroll on an atlas card always edits
  whatever the player sees on the card.
- Right-click "Set desired count…" writes the active scope; its Clear
  action follows the visible source so a global fallback can still be
  removed while a kit is active.
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
