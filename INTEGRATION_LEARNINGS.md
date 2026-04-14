# Integration Learnings And Gotchas

Last updated: 2026-04-14

This document records practical lessons learned while building SLOT and
reviewing Minecraft, NeoForge, EMI, Tom's Storage, Sophisticated Backpacks, and
similar inventory mods.

For product behavior, see [SPEC.md](SPEC.md).
For the living core model, see [ARCHITECTURE.md](ARCHITECTURE.md).

## Core Inventory Lessons

### Exact Authority And Projection Must Stay Separate

Merged rows are a projection convenience, not storage authority.

The working rule is:

- authority keeps exact per-source, per-container, per-entry truth
- projection derives visible rows from that truth
- actions resolve from rows back to their backing entries

### Not All Authority Is Slot-Backed

Some sources are exact slots. Some are provider-owned entries.

Do not force list-like providers into fake slot coordinates just because the UI
would like everything to look grid-shaped.

### Multiple Carried Backpacks Must Stay Distinct In Authority

When a player carries multiple supported backpacks, authority must preserve one
source per physical container.

Merging those sources for browsing is fine. Losing per-container identity is not.

### Tool Regions Are Presentation Aliases

Tool/crafting panels can expose regions and controls, but the underlying
inventory authority still belongs to linked source slots.

Protection, conflict detection, and outcomes should resolve through the linked
source identity, not through free-floating tool aliases.

### Broad Actions Need Exact Backing Refs

If a visible row can produce `3 + 2 + 5`, the planner must keep the exact three
backing refs.

Otherwise the system cannot:

- choose a deterministic source for `move stack`
- fan out `move all exact` correctly
- report partial success precisely
- respect source-specific protections

### Workflow Meaning And Inventory Activity Need Different State Models

Do not treat durable workflow semantics and observed inventory activity as the
same thing.

Working split:

- workflow history records durable user meaning such as collections, tags,
  loadouts, protection, and recent dismissal
- activity history records what happened to inventory over time
- browse posture stays snapshot state and should not pollute either log

That split keeps recents, cleanup, and future undo/recovery explainable without
turning search text or pane selection into business history.

### Favorites And Junk Work Better As Tags Than As Collections

Favorites and junk should not masquerade as built-in collections.

They behave differently:

- they are lightweight workflow annotations
- they participate in protection and cleanup policy differently from manual
  collections
- they need to stay visible even when collection membership changes

## Minecraft And Menu Lessons

### Logical Slot Identity Is Still Authority For Slot-Backed Menus

Rendered positions are not slot identity.

Always use authoritative menu slot ids for:

- slot-backed crafting inputs
- result extraction
- quick-craft style drag
- exact slot mutation requests

### Not Every Container Screen Is Safe To Treat As Storage

Generic hooks must not assume every `AbstractContainerScreen` is a normal
storage screen with stable transfer semantics.

Unsupported or ambiguous screens should stay vanilla.

### Refresh Can Race Input

Screen refresh and sync events frequently arrive while the player is clicking,
dragging, or carrying a cursor stack.

Refresh logic must preserve valid:

- selected rows
- cursor state
- tool-panel state
- pending action feedback
- drag state

## NeoForge Lessons

### Capabilities Are Primitives, Not A Full SLOT Model

`IItemHandler` and NeoForge capabilities are the right low-level storage
primitives, but they do not provide:

- source identity
- pane membership
- workflow policy
- Recent attribution
- projection semantics

SLOT still has to add those layers.

### Payloads Should Decode And Dispatch, Not Own Semantics

Payload handlers should stay thin.

Mutation semantics belong in operation or planner classes with one action family
each.

### External Changes Should Enter As Activity Or Invalidation, Not Fake Intents

World pickup, merchant rewards, quest rewards, compat-driven backpack changes,
and other external mutations are not user intents after the fact.

Preferred rule:

- if a bridge can produce an explicit activity event, append it
- otherwise invalidate authority and refresh
- only infer activity conservatively from authority deltas

Do not route already-happened external changes back through the same user-intent
path used for row clicks or transfer commands.

## Sophisticated Backpacks Lessons

### Closed Backpack State Is Real Data But Not Vanilla Slot Authority

Closed backpack contents can be authoritative enough for browsing, planning, and
workflow logic, but they are not the same thing as open menu slots.

Mutation still requires a supported authoritative path.

### Crafting Upgrade Slots Are Linked Surfaces, Not Separate Inventories

Sophisticated crafting upgrades should be represented as:

- exact linked source slots
- tool-region presentation
- a crafting surface descriptor

That keeps crafting semantics, protection, and outcomes aligned.

### Stable Per-Container Identity Matters

Contents UUIDs or equivalent stable container identity are critical. Without
them, multiple carried backpacks can collapse into one ambiguous source and the
planner loses correctness.

## Tom's Storage Lessons

### Terminal Items Are External Unless Explicitly Proven Otherwise

Terminal results belong to external storage and should stay distinct from the
carried pane.

### Terminal Lists Are Provider Entries, Not Fake Slots

Tom's-style terminal browsing is a strong example of provider-entry authority:

- stable entry ids
- list-like result sets
- supported extraction/insertion operations
- no need to invent fake slot coordinates

### Provider Simulation May Be Unavailable

Some provider destinations cannot promise precise simulate/apply parity.

The planner must treat provider uncertainty as first-class:

- use simulation when supported
- mark uncertainty when exact capacity cannot be known
- keep diagnostics explicit rather than pretending certainty

## Broad Transfer Lessons

### Source-Wide Insert Destinations Need Their Own Target Type

Broad row transfers often know the destination source or pane, but not the
exact receiving slot yet.

That requires a source-wide destination target rather than abusing a fake slot
target.

### Generic Carried Destinations Should Exclude Quick Access And Equipment

For ordinary broad-transfer flows, carried destinations should prefer:

- player main
- carried storage

They should not silently dump into hotbar or equipment slots unless the action
explicitly targets those surfaces.

### Stable Ordering Matters

Broad actions must be deterministic:

- visible row order first
- then stable backing-entry order
- then stable destination-source order

Without this, the same row can behave differently across refreshes and partial
capacity cases.

## EMI Lessons

### Recipe Fill Is Not Inventory Authority

EMI is a recipe-viewer and recipe-transfer trigger surface, not the authority
surface for SLOT inventory logic.

Recipe interactions should emit SLOT intents that resolve against current
authority and current crafting surfaces.

### Bounds Integration Still Matters

If SLOT occupies large screen regions, recipe viewers still need clear exclusion
or bounds information so the two UIs can coexist.

## Recent Lessons

### Initial Baselines Are Not Acquisitions

Opening a screen or building the first snapshot must not populate Recent.

### Internal Movement Is Not Acquisition

Sorting, restacking, and moving items between carried sources must not create
Recent entries, even if the visible carried browser changes shape.

### Dismissal Needs Sequence Watermarks

If Recent can be dismissed by identity, the dismissal has to carry the highest
seen activity sequence for that identity.

Otherwise the system cannot distinguish:

- "hide what I have already triaged"
- from "never show this identity again"
