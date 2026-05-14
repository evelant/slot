# SLOT Product And Behavior Spec

Last updated: 2026-05-14

This is the normative behavior specification for SLOT. It describes what the
mod should do from the player's point of view, while using the minimum core
terminology needed to keep the behavior unambiguous.

If this file conflicts with exploratory notes or implementation planning, this
file wins for user-visible behavior.

For product direction, see [direction.md](direction.md).
For the current core model, see [../architecture/overview.md](../architecture/overview.md).

## Goals

SLOT should provide:

- one coherent browsing model for carried items
- one coherent compare and transfer model when external storage is open
- category-first and workflow-first discovery for large modpacks
- Kits, desired counts, temporary wanted counts, recents, cleanup surfaces, and
  wayfinding
- source-aware, server-authoritative inventory actions
- conservative safety semantics
- compatibility with supported modded storage and crafting surfaces

SLOT should not require the player to care about storage internals unless the
distinction matters for safety or action correctness.

## Non-Goals

SLOT must not become:

- remote storage
- infinite inventory
- hidden logistics automation
- recursive autocrafting
- a backpack progression system
- an item-destruction system that acts without explicit player intent

## Required Core Terms

### Authority

Authority is the exact inventory state SLOT can act on for the active host.
Authority may be slot-backed or provider-entry-backed, but it must always map to
real authoritative inventory operations.

### Projection

Projection is a derived browsing model built from authority. Rows are
projection. They are never storage authority.

### Backing Entry

A visible row may be backed by one or many real authority entries. A merged row
must retain those backing entries so broad actions resolve to real targets.

### Desired Count

A desired count is a persistent target count for one item identity. It may be
player-global or scoped to the active Kit. Desired counts create visible gaps,
wayfinding targets, gather targets, and protection hints, but they are not
inventory authority.

### Wanted Count

A wanted count is a temporary player target for one item identity. Wanted counts
share the desired-count gap/gather/wayfinding behavior, but remain separate
state and auto-clear once the carried count satisfies the target.

### Pane Destination And Source Destination

Broad transfer actions target either:

- a pane destination such as `carried` or `external`
- a specific destination source

The UI may present the action compactly, but the action pipeline must resolve it
through real authoritative destinations.

## Global Invariants

### One User Action, One Pipeline

Each user-visible action must converge into one authoritative execution path.

### Server-Authoritative Mutation

When real inventory contents change, the final mutation must be
server-authoritative.

### Projection Never Replaces Authority

Visible aggregation is allowed. Invented authority is not.

### Unsupported Means Unsupported

If SLOT cannot safely understand a screen, source, or compat bridge, it must
fail closed and keep the original behavior.

### Refresh Must Preserve Valid Interaction State

Projection rebuilds must preserve valid selected-row, drag, tool-panel, and
pending-action state unless the underlying logical session changed.

## Screen Modes

### Carried-Only Mode

Used for:

- player inventory
- supported carried-storage screens that are part of the carried working set

Behavior:

- one carried browser
- unified search, category, Recent, Kit, desired-count, and wanted-count views
- carried sources may be merged for browsing, but exact source identity must
  still exist for actions

### Dual-Pane Mode

Used when a real external inventory, terminal, or similar source is open.

Behavior:

- carried and external panes stay visually distinct
- each pane renders only its own sources
- search, sort, categories, and planning metadata are shared
- counts and actions remain pane-aware

## Browsing And Rows

### Visible Rows

A visible row represents one of:

- a real projected item identity with a real visible count
- a planning placeholder with zero count, such as a Kit/desired/wanted/goal
  target
- a workflow/control row such as a Kit or goal row

Default row content should stay compact:

- item icon
- item name
- visible count
- minimal source or pane summary
- lightweight affordances on hover or selection

### Merged Carried Rows

Merged carried rows are allowed and expected.

Requirements:

- a merged row may show one total visible count
- the row must retain ordered backing entries and backing sources
- broad actions must resolve through those exact backing entries

Example:

- `3` in player main
- `2` in backpack one
- `5` in backpack two

The carried pane may show one visible row with total `10`, but the action
pipeline must still retain the three exact backreferences.

### Placeholder Rows

Planning placeholders must:

- be visually distinct
- show absence clearly
- support planning/workflow actions only
- never allow real stack transfer actions

### Storage Ghost Visibility

Carried cards are the default wall content. Ordinary non-carried cards from
proximate claimed storage stay collapsed behind a compact per-section nearby
chip unless the player asks to see them.

Sections with no carried cards, visible intent ghosts, or nearby chip stay
hidden from the wall and section index by default. Storage x-ray reveal toggles
may show those empty sections as browse landmarks.

Ghost cards must reveal when there is active intent:

- the section's nearby chip is expanded for the current client session
- search matches the ghost
- a Kit, desired count, wanted count, or goal tab needs the item
- the player enables storage x-ray reveal

Storage x-ray is a session-local browse toggle, not a persisted organization
state. Pressing the x-ray key toggles all proximate storage ghosts; pressing
Shift with the same key toggles all tracked claimed-storage ghosts. Enabling
one x-ray mode replaces the other, and pressing the active mode again returns
to the default carried-first view. Tiny UI indicators mirror the two toggles
so the mode is discoverable without requiring key memory. X-ray does not grant
remote mutation authority: non-proximate tracked ghosts are for quick
inspection and wayfinding only.

Supported placed item displays are live proximate storage ghosts, not claimed
storage. TFC tool racks and TFC placed-item blocks may surface their contents
when nearby. Tool racks may be chosen by deposit routing; placed-item blocks are
browse/take/rollback only. Ordinary dropped `ItemEntity` stacks are never
tracked.

## Search, Sorting, Categories, Kits, And Counts

Search, sort, category, Kit, desired-count, and wanted-count surfaces must mean
the same thing in carried-only and dual-pane modes.

In dual-pane mode:

- pane membership stays distinct
- counts reflect the local pane
- filtering is shared, not cross-pane aggregation

Kits remain orthogonal to categories. Categories explain what an item is; Kits
describe why the player wants the item ready now. Desired counts and wanted
counts must support:

- placeholders for missing tracked items
- visible carried-vs-target gaps
- gather and wayfinding against proximate known storage
- no real stack transfer unless a real authority entry exists

Wanted counts must clear when satisfied. Desired counts persist until the
player changes or clears them.

## Kits And Quick Access

A Kit is a named task package with one or more Belt pages, an optional offhand
assignment, and kit-scoped desired counts for non-Belt carry targets. It
replaces the earlier user-facing split between collections and loadouts.

Apply is soft:

- matching accessible carried items are moved into requested target slots
- missing items remain missing
- unspecified target slots are left alone
- substitutes are not invented

The workspace Belt must mirror vanilla layout: offhand on the left, a gap, then
the nine hotbar slots. The Kit Rack opens as a vertical right-side list beside
the wall, from the same header row as `All`, and is closed by default.

Kit-protected items and active kit-scoped desired-count identities should be
protected from broad cleanup unless the player explicitly asks to disturb the
active task.

## Recent

An item identity enters `Recent` when it is newly acquired into the carried
working set from a real acquisition event, such as:

- world pickup
- crafting result pickup
- external withdrawal
- positive carried-backpack delta representing real acquisition

The following must not create Recent entries:

- sorting
- restacking
- UI rebuild
- opening or closing a screen
- initial baseline snapshot
- moving items between carried sub-sources

Recent visibility and dismissal rules:

- Recent is driven by recorded acquisition activity, not by rebuilding current
  contents alone
- dismissing Recent for one identity hides activity seen up to the latest
  dismissed event for that identity
- a later acquisition of that identity must surface again

## Baseline Interactions

Baseline gestures:

- left click: stack-oriented action or compatible cursor merge
- right click: one-item or split-style action
- shift left click: move stack between source groups
- ctrl left click: move one item
- shift ctrl left click: move all exact visible type
- middle click on safe headers: sort/restack where supported

Selected-row actions and carried-cursor actions are distinct. The UI may make
them feel similar, but they must not silently collapse into one behavior if that
would change action semantics.

## Transfer And Bulk Action Semantics

### Move One

Move exactly one matching item from the best valid backing entry to the
requested destination.

### Move Stack

Move one full stack from one chosen backing entry. On a merged row, this uses
one backing entry, not the full aggregate.

### Move All Exact Visible Type

Move all visible backing entries whose projected identity matches the selected
row's movable identity.

Requirements:

- selection is based on currently visible rows
- matching uses the normalized movable identity
- planning order is deterministic
- within a row, backing-entry order is deterministic

### Move All Visible

Move all currently visible rows in visible row order, then in backing-entry
order within each row.

### Destination Rules

Broad transfer actions target a pane destination or a destination source.

Default generic carried destination behavior:

- may target player main and supported carried storage
- must not implicitly target quick access or equipment unless the action is
  explicitly about them

### Protection

Broad actions must respect protection policy for:

- favorites
- active Kit page items
- active Kit-scoped desired-count identities
- player-global desired/wanted targets when the action would defeat the target
- portable containers
- any other explicitly protected target or identity

Protection is evaluated against real concrete targets, not just visible rows.

### Determinism And Partial Success

Broad actions may partially succeed if some backing entries are blocked or a
destination fills.

Requirements:

- partial success must be attributable to concrete backing entries
- broad actions must not silently skip successful candidates because one source
  failed
- repeated actions over unchanged state must use stable ordering

## Sort, Restack, Trash, And Void

Sort and restack operations:

- stay within their requested source scope
- do not create Recent entries
- do not count as acquisitions

Trash and void actions require explicit player intent.

Trash should be recoverable where promised.
Void must never trigger from passive browsing.
If SLOT promises restore/undo for trash or overflow flows, that state must be
grounded in recorded cleanup activity rather than inferred only from current
inventory contents.

## Crafting And Tool Panels

Crafting integration must remain source-aware and slot-backed.

Requirements:

- crafting inputs and result must resolve to exact authoritative slot targets
- generic carried-drop routing must not steal tool-region input
- supported crafting surfaces may present compact tool panels, but the panel is
  not the authority surface

### Placement From Selected Row

Placement from a selected row acts on the selected identity and allowed visible
backing entries.

### Placement From Carried Cursor

When carrying a real stack and targeting a crafting input:

- left click performs vanilla-like stack placement
- right click places one
- drag distribution must respect eligible inputs and authoritative slot targets

### Result Extraction

Result extraction must:

- use the authoritative result slot
- update ownership and Recent producers correctly
- support refill behavior only where the active crafting surface supports it

## Compatibility Requirements

Compat bridges must:

- stay narrow
- convert foreign data into SLOT-owned descriptors and targets
- fail closed on ambiguity
- keep reflection out of screen and projection code

Multiple carried containers must remain distinct in authority even when they are
merged in browsing.

Terminal-like provider inventories must not pretend to be slot-backed if they
are not.

## Testing Requirements

Critical coverage belongs where bugs happen:

- screen/session boundaries
- intent routing
- transfer planning and execution
- crafting interactions
- refresh and sync timing
- compat bridge behavior
- Recent production and suppression
