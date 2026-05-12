# Kits Design

Last updated: 2026-05-12

Status: current direction for task-oriented inventory groupings, superseding
the earlier "collections plus loadouts" split.

This document defines the Kit concept: a single task-shaped package that unifies
what SLOT previously modeled as separate "collections" (groupings of items by
task) and "loadouts" (hotbar arrangements). Kits are the user-facing answer to
"I want a fast way to switch between different tasks and have the right items
in the right places for each one."

For the wall and visual memory model Kits sit on top of, see
[../status.md § Production wall shape](../status.md#production-wall-shape-post-list-view)
and the historical [atlas.md](atlas.md). For how Kit activation feeds the
retired relevance model, see
[relevance-lod.md § Relevance contributors](relevance-lod.md).
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
- kit-scoped desired counts for non-hotbar items the player wants in carried
  inventory while this task is active
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
- kit-scoped desired counts: non-hotbar item identities and counts the player
  wants in carried inventory while this Kit is active
- `offhand`: optional explicit offhand assignment
- `protection`: flags that modulate protection policy when this Kit is active

A hotbar page, internally, is structurally identical to today's
`QuickAccessLoadoutDefinition` — a set of `(target, identity)` entries keyed to
quick-access slot targets. The existing `LoadoutApplyService.plan()` and
`LoadoutApplyExecutor` path is reused per page.

A Kit with a single page and no kit-scoped desired counts is functionally
equivalent to a classic loadout. Multi-page and counted bring targets are
additive capabilities.

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
- page count is bounded by carried capacity: page slots must fit in the
  player's 36-slot carried inventory, minus any offhand reservation; counted
  bring targets are soft carry goals rather than kit slot reservations

Why this shape:

- page switching is one-intent, one-hotkey; it feels like flipping a belt
- no external authority is required mid-switch; everything is carried
- it degrades cleanly: if items are missing, switch still works, slots show
  ghosts until the player gathers the missing items

Page switching is a Kit-active-only action. With no Kit active, the player has
a normal vanilla belt and pressing the page key does nothing.

### Bring Targets (Kit-Scoped Desired Counts)

Bring targets capture items the player wants in carried inventory but not on
the Belt: spare torches, extra food stacks, a shulker for hauling ore, a backup
pickaxe.

Implementation-wise these are kit-scoped desired counts, not a separate list on
`KitDefinition`. They are item-identity/count targets and do not consume Kit
slots. They influence:

- Kit readiness and card gap chrome
- gather flows for missing desired/wanted items
- protection while the Kit is active

Bring targets do not pin items to specific carried slots. Carried placement
stays flexible unless the player explicitly uses a visual home.

### Protection (Auto-Apply)

When a Kit is active, its belt items, offhand, and kit-scoped desired-count
identities are protected from trash/void/cleanup flows by default. This is
automatic — the player does not configure per-item protection to get reasonable
safety.

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
Activating a Kit applies its page-1 Belt layout and enables the Kit's
protection flags. The gather action pulls missing Kit page items and
kit-scoped desired-count targets from proximate storage when available.
Deactivating a Kit removes the protection but does not undo the belt changes
(belt state is real inventory and stays as the player left it).

## Wall Presence

Kits are native to the wall surface, not a separate screen. Current production
surfaces:

### The Belt

A docked hotbar strip fixed to the bottom of the workspace. It renders the
current offhand slot on the left, a gap, then the nine hotbar slots, matching
vanilla's survival layout. Styling should read as workspace chrome, not a
second Minecraft inventory grid.

The Belt always shows:

- offhand slot, gap, then 9 Belt slots with current contents
- active Kit name and current page indicator (e.g., "Mining · page 1/3")
- a page-cycle control (hotkey-bound; clickable affordance for discoverability)

Equipment and curios are switched much less often. Equipment Kits are out of
scope for now; Kits only manage Belt pages, offhand, and kit-scoped desired
counts.

### The Kit Rack

A toggle in the same row as the `All` goal/header opens the Kit Rack as a
vertical right sidebar beside the main wall scroller. Kit Cards stack
vertically; the active Kit is visually differentiated. Clicking a Kit Card
activates that Kit, and clicking the active Kit deactivates it.

The Kit Rack is closed by default and opened when the player wants to switch
tasks or edit kit targets. It should not consume permanent wall width when
closed.

### Kit Cards

Each Kit Card is a compact visualization of the Kit:

```
┌─ Mining · 8/9 · 3/4 ──────────┐
│ [pick][pick][torch][food][  ] │   ← page 1 belt
│ [wtr ][    ][    ][eye ][food]│
│ ─ page 2 ─                    │
│ [shear][shul][stick][lad ][ ] │
│ [    ][    ][    ][    ][cob]│
│ ─ desired ─                   │
│ [torchx64][cobble][food]      │
└───────────────────────────────┘
```

Each cell:

- solid if the identity is present in carried inventory
- outlined if the identity is last-seen in known external storage
- ghost if missing everywhere known

Card header shows Belt readiness and desired-count readiness at a glance.

Hovering a Kit Card should highlight member homes across the wall so the
player can answer "where do these items live?" without leaving the Kit Rack.

## Interactions

### Create a Kit

- **Snapshot from current Belt** is the primary create flow. The player lays
  out their Belt however they like (vanilla-style), opens the Kit Rack, hits
  "Save as Kit," names it. The current Belt becomes page 1; current offhand is
  captured; kit-scoped desired counts start empty.
- **Blank Kit** creates an empty Kit Card that the player fills by dragging.

### Edit a Kit

- **Drag a home onto a Kit Card's belt slot** sets that identity for that slot.
  If the Kit is active, this also applies the belt change immediately.
- **Drag a home onto a Kit Card's desired area** adds or updates a
  kit-scoped desired count, defaulting to one for newly-added items.
- **Drag an identity off a slot/desired cell** removes it.
- **Snapshot update** overwrites a Kit's current page with the current Belt
  state (one-click re-save).
- **Add a page** extends the Kit with a new empty page the player can fill.
- **Reorder / rename pages** is expected; specifics come later.

### Activate

- Click a Kit Card on the Rack → Kit activates immediately. No diff preview.
- Undo covers mistakes (see Open Questions).
- Activation applies page 1's `LoadoutApplyPlan` and enables the Kit's
  protection. It does not silently auto-withdraw from closed storage.

### Switch Pages

- Hotkey (default candidate: `R` or cycle-hotbar binding) advances to the next
  page. Shift-hotkey goes to previous.
- Belt visibly transitions as slot contents swap.
- Page indicator on the Belt updates.

### Deactivate

- Re-click the active Kit Card, or hit a "no Kit" affordance.
- Belt stays as-is; protection flags clear; kit-scoped desired counts remain on
  the Kit but stop influencing the active carry target.

### Gather Missing

When a Kit has missing items, the Kit Card offers "Gather." This pulls
reachable missing Kit page items and kit-scoped desired-count targets from
proximate claimed chests through the normal server-authoritative storage path.
Player-global desired counts and player wanted counts share the same gather
pipeline. Closed, non-proximate, or unsupported storage is not silently used.

## Relationship to Existing Domain

- `KitDefinition` owns the kit id/name, ordered page definitions, and optional
  offhand assignment. The old bring list is represented by
  `DesiredCountWorkflowDomainService#forKit(String)`.
- `LoadoutApplyService.plan()` is the activation/page-swap primitive. It plans
  Belt and offhand changes from live carried authority and degrades to visible
  missing targets when an item is not available.
- The earlier `CollectionDefinition` + `CollectionMembership` direction is not
  the user-facing model. Kits plus desired/wanted counts cover the task-shaped
  workflow surface.
- `VisualHomeMap` / `VisualHomeAssignment` state is unaffected. Kits reference
  item identities; those identities resolve to homes via the existing visual
  memory model.
- Protection integration reuses `ProtectionPolicy`. Kit-active protection is a
  layer that composes with the existing policy rather than replacing it.

## Non-Goals

- Equipment and curios loadouts. Equipment lives behind a toggle but is not
  part of Kits for now.
- Multiple simultaneously active Kits.
- Passive auto-withdraw from external storage. The explicit Gather action may
  pull from proximate claimed chests; activation and page switching do not.
- Task-detection heuristics. Kits are explicitly chosen by the player.
- Cross-world or cross-save Kit sharing. Kits are per-player per-world until a
  design explicitly promotes them further.

## Open Questions

1. **Page switching hotkey.** Default keybinding that does not collide with
   vanilla survival bindings and is memorable.
2. **Undo scope.** Is undo Kit-switch-granular (one undo reverts the entire
   apply), step-granular (per-slot), or something else? Committing to Kit
   granularity keeps mental model simple but may over-revert user edits made
   after activation.
3. **What happens when a Kit activation partially fails?** E.g., protection
   blocks moving an occupant out of slot 3. Current `LoadoutApplyService`
   records missing targets; the Kit UI needs a simple readout for this that
   does not confuse the player.
4. **Page capacity enforcement.** Hard cap (reject creating page N if carried
   won't fit) or soft warning with visible overflow state?
5. **Offhand inheritance.** If a Kit defines offhand on page 1 but not on
   page 2, does page 2 keep page 1's offhand, or clear it?
6. **Visual Home interaction.** If a kit-scoped desired-count item has no
   visual home, should the UI suggest one, or leave it in Triage? Leaning
   toward leaving it to the Triage flow so Kits do not silently edit visual
   memory.
