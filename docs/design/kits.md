# Kits Design

Last updated: 2026-04-16

Status: current direction for task-oriented inventory groupings, superseding
the earlier "collections plus loadouts" split.

This document defines the Kit concept: a single task-shaped package that unifies
what SLOT previously modeled as separate "collections" (groupings of items by
task) and "loadouts" (hotbar arrangements). Kits are the user-facing answer to
"I want a fast way to switch between different tasks and have the right items
in the right places for each one."

For the atlas and visual memory model Kits sit on top of, see [atlas.md](atlas.md).
For the host/UI boundary, see [../architecture/host-ui.md](../architecture/host-ui.md).

## What Changed

Previously SLOT planned two separate workflow concepts:

- `CollectionDefinition` + `CollectionMembership`: user-defined groups of item
  identities, rendered as tags on atlas cards, with a membership toggle RPC.
- `QuickAccessLoadoutDefinition`: a hotbar arrangement applied through
  `LoadoutApplyService`.

Those split one mental intent ("when I mine, I want these items ready and my
belt laid out this way") into two lists the player has to manage separately.

Kits collapse that split into one concept. A Kit owns everything a task needs:

- one or more hotbar pages (the belt layouts for this task)
- a bring list (non-hotbar items the player wants in carried inventory while
  this task is active)
- protection flags that activate when the Kit is active (don't let cleanup
  destroy task-critical gear)

Kits replace the previous user-facing collections concept. If non-task
groupings (favorites, project materials, long-term saving) prove valuable later,
they earn their own separate surface rather than reviving generic collections.

## The Kit Concept

### Data Shape

A Kit, at the design level, is approximately:

- `id`: stable identifier
- `name`: display name
- `pages`: ordered list of hotbar pages (1 or more)
- `bring`: ordered list of non-hotbar item identities the player wants in
  carried inventory while this Kit is active
- `offhand`: optional explicit offhand assignment
- `protection`: flags that modulate protection policy when this Kit is active

A hotbar page, internally, is structurally identical to today's
`QuickAccessLoadoutDefinition` — a set of `(target, identity)` entries keyed to
quick-access slot targets. The existing `LoadoutApplyService.plan()` and
`LoadoutApplyExecutor` path is reused per page.

A Kit with a single page and no bring items is functionally equivalent to a
classic loadout. Multi-page and bring are additive capabilities.

### Multi-Hotbar Pages

Modded survival tasks rarely fit in 9 slots. Players hit a painful loop where
they open inventory, find an item, swap it to the belt, and repeat. Kits fix
this by letting one Kit own multiple belt pages and providing a hotkey to cycle
between them.

Semantics:

- a Kit owns an ordered list of pages; page 1 is the default when the Kit is
  activated
- cycling pages is a deterministic slot-for-slot swap through the existing
  `ASSIGN_WITH_DISPLACE` path: items currently on the belt get written back to
  their page-N carried slots, and page-(N+1)'s items are pulled into the belt
- inactive pages' items live in the carried main inventory (non-belt slots),
  not in storage; the player is always carrying the entire Kit's belt-eligible
  items
- if an item is missing at page-switch time, that belt slot stays empty (the
  Kit does not attempt to retrieve from storage mid-switch)
- page count is bounded by carried capacity: pages × 9 + |bring| must fit in
  the player's 36-slot carried inventory, minus any offhand reservation

Why this shape:

- page switching is one-intent, one-hotkey; it feels like flipping a belt
- no external authority is required mid-switch; everything is carried
- it degrades cleanly: if items are missing, switch still works, slots show
  ghosts until the player gathers the missing items

Page switching is a Kit-active-only action. With no Kit active, the player has
a normal vanilla belt and pressing the page key does nothing.

### Bring List (Non-Hotbar)

The bring list captures items the player wants in carried inventory but not on
the belt: spare torches, extra food stacks, a shulker for hauling ore, a
backup pickaxe.

Bring items are item-identity references, not slot assignments. They influence:

- Kit readiness ("7/9 belt ready, 2/3 bring ready")
- gather flows ("missing: shulker, rockets")
- protection (a Kit-active bring item is protected from cleanup)

Bring does not pin items to specific carried slots. Carried placement stays
flexible unless the player explicitly uses a visual home.

### Protection (Auto-Apply)

When a Kit is active, its belt items, offhand, and bring list items are
protected from trash/void/cleanup flows by default. This is automatic — the
player does not configure per-item protection to get reasonable safety.

The active Kit's protection is additive, not a replacement for existing
`ProtectionPolicy` state. Favorites, locks, and other existing protection
mechanisms continue to apply.

### Single Active Kit

Exactly one Kit can be active at a time, or none (the default/resting state).
Multiple concurrent Kits are out of scope — overlap resolution gets complex
quickly and the core value proposition is clean task switching, not layered
presets.

"No Kit active" behaves like vanilla: whatever is on the belt is simply what
the player has placed there. Protection is whatever the base policy says.
Activating a Kit applies its page-1 belt layout, pulls bring items toward
carried inventory if available, and enables the Kit's protection flags.
Deactivating a Kit removes the protection but does not undo the belt changes
(belt state is real inventory and stays as the player left it).

## Atlas Presence

Kits are atlas-native, not a sidebar panel. Three visual surfaces:

### The Belt

A camera-anchored hotbar strip fixed to the bottom of the viewport. It renders
the current hotbar slots plus an adjacent offhand slot. Styling is diegetic
(prototype-greybox is fine for now) rather than an opaque chrome bar — it
should feel like part of the atlas world, not floating UI.

The Belt always shows:

- 9 belt slots with current contents
- offhand slot adjacent to belt slots
- active Kit name and current page indicator (e.g., "Mining · page 1/3")
- a page-cycle control (hotkey-bound; clickable affordance for discoverability)

Equipment and curios are switched much less often. They live behind a toggle
near the Belt that expands an equipment rack surface on demand. Equipment
Kits are out of scope for now; Kits only manage belt pages, offhand, and
bring.

### The Kit Rack

A toggle near the Belt opens the Kit Rack: a row of Kit Cards representing
defined Kits. The active Kit is visually differentiated. Clicking a Kit Card
activates that Kit.

The Kit Rack is a secondary surface — closed by default, opened when the
player wants to switch tasks. It does not consume permanent atlas real estate.

### Kit Cards

Each Kit Card is a compact visualization of the Kit:

```
┌─ Mining · 8/9 · 3/4 ──────────┐
│ [pick][pick][torch][food][  ] │   ← page 1 belt
│ [wtr ][    ][    ][eye ][food]│
│ ─ page 2 ─                    │
│ [shear][shul][stick][lad ][ ] │
│ [    ][    ][    ][    ][cob]│
│ ─ bring ─                     │
│ [torchx64][cobble][food]      │
└───────────────────────────────┘
```

Each cell:

- solid if the identity is present in carried inventory
- outlined if the identity is last-seen in known external storage
- ghost if missing everywhere known

Card header shows belt readiness and bring readiness at a glance.

Hovering a Kit Card highlights all member homes across the atlas, drawing
tethers from the card back to each home. This answers "where do these items
live?" without leaving the Belt area.

## Interactions

### Create a Kit

- **Snapshot from current belt** is the primary create flow. The player lays
  out their belt however they like (vanilla-style), opens the Kit Rack, hits
  "Save as Kit," names it. The current belt becomes page 1; current offhand is
  captured; bring is empty.
- **Blank Kit** creates an empty Kit Card that the player fills by dragging.

### Edit a Kit

- **Drag a home onto a Kit Card's belt slot** sets that identity for that slot.
  If the Kit is active, this also applies the belt change immediately.
- **Drag a home onto a Kit Card's bring area** adds a bring item.
- **Drag an identity off a slot/bring cell** removes it.
- **Snapshot update** overwrites a Kit's current page with the current belt
  state (one-click re-save).
- **Add a page** extends the Kit with a new empty page the player can fill.
- **Reorder / rename pages** is expected; specifics come later.

### Activate

- Click a Kit Card on the Rack → Kit activates immediately. No diff preview.
- Undo covers mistakes (see Open Questions).
- Activation applies page 1's `LoadoutApplyPlan`, moves bring items toward
  carried inventory where possible, and enables the Kit's protection.

### Switch Pages

- Hotkey (default candidate: `R` or cycle-hotbar binding) advances to the next
  page. Shift-hotkey goes to previous.
- Belt visibly transitions as slot contents swap.
- Page indicator on the Belt updates.

### Deactivate

- Re-click the active Kit Card, or hit a "no Kit" affordance.
- Belt stays as-is; protection flags clear; bring items are no longer tracked.

### Gather Missing

When a Kit has missing items, the Kit Card offers "Gather." This walks the
camera through each missing home (highlighting and optionally routing to
last-seen external sources) so the player can restock. Retrieving from
external storage still requires the usual active-authority path; SLOT does not
auto-withdraw from closed containers.

## Relationship to Existing Domain

- `QuickAccessLoadoutDefinition` likely remains as the per-page primitive.
  `LoadoutApplyService.plan()` already produces the right `ASSIGN` +
  `ASSIGN_WITH_DISPLACE` sequence per page, so Kit activation and page swap
  build cleanly on it.
- A new `KitDefinition` (exact name TBD) owns an ordered list of page
  definitions, a bring list of `ItemIdentity`, an optional offhand assignment,
  and protection flags.
- `CollectionDefinition` + `CollectionMembership` + `CollectionProjection` +
  `CollectionWorkflowDomainService` become unused in the user-facing direction.
  They can be retired outright once Kit domain state lands; there is no
  migration obligation because SLOT is unreleased.
- `VisualHomeMap` / `VisualHomeAssignment` state is unaffected. Kits reference
  item identities; those identities resolve to homes via the existing visual
  memory model.
- Protection integration reuses `ProtectionPolicy`. Kit-active protection is a
  layer that composes with the existing policy rather than replacing it.

## Non-Goals

- Equipment and curios loadouts. Equipment lives behind a toggle but is not
  part of Kits for now.
- Multiple simultaneously active Kits.
- Auto-gather from external storage. Gather guides the player; it does not
  mutate closed containers.
- Task-detection heuristics. Kits are explicitly chosen by the player.
- Cross-world or cross-save Kit sharing. Kits are per-player per-world until a
  design explicitly promotes them further.
- Persistence of Kits across restarts is a later slice (mirrors the visual
  home map persistence sequence). The first prototype is in-memory.

## Open Questions

1. **Naming of the domain type.** `KitDefinition`? `TaskKitDefinition`?
   Collisions with `InventoryActionScope.LOADOUT` and existing loadout-scoped
   diagnostics should be considered.
2. **Page switching hotkey.** Default keybinding that does not collide with
   vanilla survival bindings and is memorable.
3. **Undo scope.** Is undo Kit-switch-granular (one undo reverts the entire
   apply), step-granular (per-slot), or something else? Committing to Kit
   granularity keeps mental model simple but may over-revert user edits made
   after activation.
4. **Bring item ordering and identity granularity.** Should bring track
   per-identity desired counts, or be presence-only?
5. **What happens when a Kit activation partially fails?** E.g., protection
   blocks moving an occupant out of slot 3. Current `LoadoutApplyService`
   records missing targets; the Kit UI needs a simple readout for this that
   does not confuse the player.
6. **Page capacity enforcement.** Hard cap (reject creating page N if carried
   won't fit) or soft warning with visible overflow state?
7. **Offhand inheritance.** If a Kit defines offhand on page 1 but not on
   page 2, does page 2 keep page 1's offhand, or clear it?
8. **Rack location on the atlas.** Always at the Belt region, or player-movable
   once landmarks become draggable?
9. **Visual Home interaction.** If a Kit bring item has no visual home,
   should activating the Kit auto-home it (to a dedicated "Kit Items" island,
   maybe), or leave it in Triage? Leaning toward leaving it to the Triage
   flow so Kits do not silently edit visual memory.
