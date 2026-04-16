# Inventory Action Taxonomy

Last updated: 2026-04-15

## Summary

SLOT inventory actions should model server-side mutation semantics, not raw UI
gestures. A list click, drag, scroll wheel tick, hotbar assignment, loadout
apply, or checkbox bulk command should resolve into a small set of domain verbs
plus explicit quantity, scope, and conflict policy.

This avoids the bug class where a gesture is routed through the wrong primitive.
The LDLib hotbar transfer issue was one example: "put this stack in hotbar slot
3" is assignment with displacement, not generic transfer/insert.

## Vocabulary

- Verb: the mutation contract, such as transfer, assign, cursor pickup, or drop
  to world.
- Quantity: how much the action should operate on, such as one, stack, all
  matching, half of source, or an exact server-derived count.
- Scope: which items or targets are in bounds, such as one target, visible rows,
  selected targets, source-local, or loadout.
- Conflict policy: what to do when the destination already contains something,
  such as insert-only, assign with displacement, exact swap, or reject.
- Planner: a command-level service that turns a user intent into one or more
  concrete mutation requests.

## Core Verbs

| Verb | Contract | Examples |
| --- | --- | --- |
| `TRANSFER` | Move items from source to destination using insert/merge semantics. | Main inventory to chest, provider withdrawal to player inventory, hotbar to main inventory. |
| `ASSIGN` | Make a concrete destination contain the source stack, with displacement governed by conflict policy. | Main row to exact hotbar slot, loadout hotbar apply, equipment replacement. |
| `SWAP` | Exchange two concrete non-cursor targets without merge semantics. | Swap two hotbar slots, swap armor slot with carried inventory slot. |
| `CURSOR_PICKUP` | Move items from a target into the carried cursor stack. | Pick up stack, pick up half, pick up one, add one matching item to held cursor. |
| `CURSOR_PLACE` | Move items from the cursor into a target. | Place stack, place one, place half, crafting placement. |
| `CURSOR_SWAP` | Exchange cursor contents with a target. | Left click occupied incompatible target while holding a stack. |
| `QUICK_MOVE` | Delegate to menu/vanilla shift-click routing where the menu owns destination choice. | Shift-click crafting result or container slot. |
| `DROP_TO_WORLD` | Remove items from a source or cursor and spawn them in the world. | Drop key, drop cursor outside screen. |
| `TRASH` | Move items into a recoverable trash history. | Trash button with undo/recovery token. |
| `VOID` | Irreversibly delete items or route them to void-capable storage. | Void button, void upgrade target. |
| `USE` | Invoke item use semantics through selected or staged hand state. | Right-click food, ender pearl, backpack item, offhand usable. |
| `SORT_SOURCE` | Reorder one source locally without changing total item ownership. | Sort main inventory, sort chest region. |
| `DISTRIBUTE` | Place items across multiple destinations as a planned operation. | Drag cursor across crafting inputs, spread evenly over selected slots. |
| `COLLECT_MATCHING` | Gather matching stacks from a scope into a cursor or destination. | Vanilla double-click collect matching. |
| `SET_FILTER` | Mutate a ghost/filter/config slot without consuming real inventory. | Backpack filter, storage filter, recipe ghost slot. |
| `TOOL_ACTIVATE` / `TOOL_ACTION` / `TOOL_TOGGLE` | Provider-defined tool controls. | Craft button, backpack upgrade button, terminal toggle. |

## Quantity Policies

| Quantity | Meaning |
| --- | --- |
| `DEFAULT` | The verb derives its normal amount. |
| `ONE` | One item. |
| `STACK` | One source stack or the request's exact stack count. |
| `ALL_MATCHING` | All matching items in the selected scope. |
| `EXACT_COUNT` | Use the explicit server-validated `requestedCount`. |
| `HALF_SOURCE` | Half of the source stack, rounded like vanilla right-click pickup. |
| `HALF_CURSOR` | Half of the cursor stack, rounded like vanilla right-click place. |
| `FILL_TARGET` | Enough to fill the destination target. |
| `EVEN_SPLIT` | Split cursor/source items evenly across targets. |
| `SINGLE_PER_TARGET` | Place one item into each eligible target. |

`requestedCount` remains on concrete requests because planners often already know
the exact authoritative count. `quantity` records why that count was chosen and
lets server handlers derive counts safely when the client should not provide one.

## Conflict Policies

| Policy | Meaning |
| --- | --- |
| `DEFAULT` | The verb chooses the safest default. |
| `INSERT_ONLY` | Only merge into compatible stacks or empty valid targets. |
| `ASSIGN_WITH_DISPLACE` | Destination becomes the source stack; the old destination is moved back to source or staged. |
| `SWAP_EXACT` | Exchange two concrete targets exactly. |
| `REPLACE_AND_STAGE` | Move the old destination occupant to an explicit staging target. |
| `REJECT_IF_OCCUPIED` | Fail if the target is occupied. |

Transfer defaults to `INSERT_ONLY`. Assignment defaults to
`ASSIGN_WITH_DISPLACE` only for targets whose descriptor explicitly supports the
assignment capability.

## UI Gestures Are Not Verbs

These are not core mutation actions by themselves:

- Search/filter text: browse state.
- Scroll viewport: UI state.
- Checkbox/multi-select: selection state.
- Bulk action button: planner input that emits concrete requests.
- Drag gesture: input shape that maps to cursor placement, distribution, assign,
  or transfer.
- List sort order: browse state unless storage slots are physically reordered.
- "Move selected": planner input whose output depends on selected command.

## Planner Responsibilities

Planners own multi-step behavior and should produce concrete requests with
server-derived counts and targets:

- row transfer planner: projected rows to `TRANSFER` requests
- hotbar/list UI: list row to exact quick-access slot as `ASSIGN`
- loadout apply planner: quick-access/equipment targets as assignment semantics
- cursor planner: raw clicks into cursor pickup/place/swap with quantity policy
- bulk planner: selected/visible rows into ordered concrete requests
- sort planner: source-local reorder plan or delegated menu click sequence
- trash/void planner: recoverable trash or irreversible void request with safety
  policy

## Implementation State

The initial taxonomy migration is landed:

- transfer-count-specific verbs were replaced with `TRANSFER` plus quantity
  policy
- `QUICK_ACCESS_ASSIGN` was replaced with general `ASSIGN` plus
  `ASSIGN_WITH_DISPLACE`
- cursor verbs are `CURSOR_PICKUP`, `CURSOR_PLACE`, and `CURSOR_SWAP`
- world dropping is `DROP_TO_WORLD`
- `InventoryActionRequest` and `InventoryActionOutcome` carry verb, quantity,
  scope, and conflict policy
- tests guard against reintroducing target-specific or count-specific action
  enum values

Still deferred:

- `SWAP`, `TRASH`, `VOID`, `SORT_SOURCE`, `DISTRIBUTE`,
  `COLLECT_MATCHING`, and `SET_FILTER` are vocabulary first and should
  fail closed until their planners/executors are explicitly implemented
- richer cursor and multi-select gestures still need planner work before UI
  exposure
