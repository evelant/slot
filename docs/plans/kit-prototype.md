# Kit Prototype Plan

Last updated: 2026-04-23

Status: slices 1–8 landed plus a playtest polish pass (right-click
card menu, hotbar↔hotbar swap, kit-page drag rearrange, active-kit
save-to-page, `LoadoutApplyService` cross-source ASSIGN /
belt-reorder / clear-target fixes, and UI readability polish).
Slice 9 (Kit-specific undo) is deferred into the general undo/redo
stack. One remaining follow-up: drag-to-edit on an *active* kit
currently only updates the kit definition; per design it should
also sync to the live belt when the edit hits the active page. See
[../status.md](../status.md) → "Kit prototype landing points" for
the full landed detail and the active-kit-edit follow-up note.

For the Kit concept and interaction model, see [../design/kits.md](../design/kits.md).
For the broader near-term engineering sequence, see [current.md](current.md).
For the current operational baseline, see [../status.md](../status.md).

## Goals And Non-Goals

Goals:

- validate the "switch fast between tasks" UX bet before investing in
  persistence, protection polish, or gather flows
- reshape existing loadout and collection domain state into a single Kit
  concept with multi-page hotbar support
- anchor the hotbar as a camera-pinned Belt landmark so Kits have a stable
  place to live
- keep all real inventory mutations on the existing intent router and
  `LoadoutApplyService` path; the prototype must not invent parallel action
  semantics
- keep each slice independently evaluable so we can stop, pivot, or reshape
  without the plan collapsing

Non-goals:

- visual polish; greybox styling is expected until the interaction model lands
- equipment/curios Kits
- multiple simultaneously active Kits
- cross-world or cross-save Kit export/import
- auto-withdraw from closed external containers during gather
- backwards compatibility with the abandoned sidebar prototype; no migration
  path is required because SLOT is unreleased

## Prerequisites

The Kit prototype should start after the current triage/home loop is usable
enough that the Belt can live alongside it without visual chaos. Specifically:

- atlas readability cleanup (IMPLEMENTATION_PLAN slice 1) is landed so the
  camera-anchored Belt does not compete with oversized item cards
- in-memory home assignment (IMPLEMENTATION_PLAN slices 2 and 3) is landed so
  Kit hover tethers have stable home targets to point at
- existing `LoadoutApplyService`, `LoadoutApplyExecutor`, and the
  workflow-domain runtime remain sound under current tests

If those are still in flight, the Belt slice (slice 1 below) can still ship
first because it does not depend on the home model — but Kit Cards and their
tether/hover behavior should wait until homes are present.

## Risk Register

### 1. Multi-Page Carried Capacity Is Tight

Pages × 9 + |bring| must fit in the player's 36-slot carried inventory, minus
any offhand reservation. A Kit with 3 pages and 10 bring items already uses
37 slots.

Mitigation:

- enforce a clear cap at Kit edit time with visible feedback
- treat excess capacity as a validation error on save, not a silent drop
- in early slices, cap Kits at 2 pages until we see how the math feels in
  practice

### 2. Page Switching Cannot Rely On `LoadoutApplyService` As-Is

`LoadoutApplyService` plans from a single target layout against current
authority. Page-swap semantics require the reverse half: moving current belt
items into the prior page's carried slots.

Mitigation:

- model each page switch as a two-phase apply: capture current belt → page N
  carried snapshot, then apply page N+1 as a standard loadout plan
- carried slot targets for inactive pages should be slot-backed for
  determinism; experiment with whether the Kit stores target slot indices
  directly or resolves them dynamically each switch

### 3. Collection Domain Retirement Can Break Tests

`CollectionDefinition`, `CollectionMembership`, `CollectionProjection`, and
`CollectionWorkflowDomainService` are referenced by existing tests and by the
LDLib workspace view model.

Mitigation:

- retire in one focused slice (slice 2 below) together with introducing the
  Kit domain type
- remove the view model's collection fields, RPC emitters, and tag rendering
  in the same change
- keep `InventoryActionScope.LOADOUT` semantics as-is; nothing about Kits
  requires a new scope

### 4. Undo Expectations Outpace Implementation

The design commits to "no diff preview, undo covers mistakes," but undo is
not implemented yet.

Mitigation:

- gate "destructive-feeling" Kit operations (activate, switch page) behind a
  confirm step if undo has not landed by the time slice 3 ships
- capture enough state at activation time (pre-activation belt + offhand
  snapshot) to make undo implementation straightforward in slice 9

### 5. Belt Hotkeys Collide With Vanilla Or Common Modded Bindings

Page-switch hotkey needs to be discoverable and not conflict with common
bindings (`F` swaps hand, `Q` drops, mouse buttons map to actions, etc.).

Mitigation:

- default to a keybinding the player sets explicitly, not a hardcoded one;
  ship with no default binding if needed
- surface a clickable page-cycle affordance on the Belt so the feature is
  discoverable without a keybind

## Slice Sequence

### Slice 1: Camera-Anchored Belt (Greybox)

Goal:

- replace the current inline hotbar flex row with a Belt landmark pinned to
  the viewport's bottom edge, independent of any Kit concept

Deliverables:

- LDLib2 shell change so the hotbar renders as a camera-pinned strip rather
  than an inline flex row
- 9 belt slots plus adjacent offhand slot
- greybox styling; final visual treatment deferred
- click/drag behavior against belt slots continues to route through the
  existing workspace transfer RPC and intent router path
- equipment/curios toggle button (closed state only; body not built yet)
- atlas pan/zoom is unaffected; the Belt is chrome-pinned to the camera

Exit criteria:

- opening the player inventory shows the Belt in a fixed viewport position
- panning the atlas does not move the Belt relative to the viewport
- existing hotbar transfer behavior is unchanged semantically

Tests:

- existing LDLib workspace transfer tests still pass
- a visual/manual smoke test confirms Belt anchoring

### Slice 2: Kit Domain Type And Collection Retirement

Goal:

- establish `KitDefinition` as the canonical task-grouping concept and
  retire the standalone collection concept

Deliverables:

- new domain type `KitDefinition(id, name, pages, bring, offhand, protection)`
- new `KitWorkflowDomainService` exposing: list, create, update, delete,
  snapshot-from-belt, activate, deactivate, switch-page
- in-memory storage through the existing `WorkflowDomainRuntime` contract
  (no persistence yet)
- `LoadoutApplyService.plan()` reused per page for activate and switch-page
- retirement of `CollectionDefinition`, `CollectionMembership`,
  `CollectionProjection`, `CollectionWorkflowDomainService`, and
  `WorkflowDomainSnapshot` collection fields
- retirement of LDLib workspace view model collection tags and RPC emitters
- tests for: Kit create/update/delete, activate against a deterministic
  authority fixture, activate with missing items leaves slots empty, switch
  page permutes carried slots correctly, protection flags turn on when a Kit
  is active and off when none is active

Exit criteria:

- no user-facing collection concept remains in the domain, view model, or
  LDLib UI
- Kit activation produces the same intent router requests that a loadout
  activation would have (per page)
- headless tests prove activate/switch-page correctness

### Slice 3: Kit Rack UI And Snapshot Create

Goal:

- make Kit creation and activation usable through the LDLib workspace without
  multi-page or bring complexity

Deliverables:

- toggle button near the Belt that opens the Kit Rack panel
- Kit Rack panel renders a row of Kit Cards (single page each, no bring yet)
- "Save current belt as Kit" button creates a Kit from the active belt + offhand
- click Kit Card to activate; click active Kit Card to deactivate
- Kit Card shows belt readiness (e.g., 7/9) and highlights missing slots as
  ghosts
- Kit Rack closes when the player clicks away or toggles again
- RPC command path for Kit mutations (create, delete, activate) mirroring the
  existing narrow-command pattern; server builds the real requests

Exit criteria:

- the player can snapshot the current belt, name it, activate it later, and
  have the belt return to that layout (if items are present)
- Kit activation routes through the same intent router as today's hotbar
  transfer; no client-side authority is involved
- deactivating a Kit leaves the belt as-is and clears protection

Tests:

- LDLib workspace test for Kit create, activate, deactivate RPC handling
- view-model refresh after Kit state change
- architecture test confirming no LDLib imports leaked into common

### Slice 4: Multi-Page Kits And Page Switching

Goal:

- extend Kits to own multiple hotbar pages and add a hotkey to cycle between
  them

Deliverables:

- `KitDefinition` page list becomes ordered and mutable
- Kit Card renders each page as a sub-row; "Add page" affordance appends an
  empty page
- page switch action in the domain service: captures current belt into the
  current-page carried slot bindings, then applies the next page's layout
- carried slot bindings are stored on the page so page N knows where its
  items should live when inactive
- client page-cycle affordance on the Belt (button; hotkey is a user-set
  keybind with no default)
- Belt renders current page indicator (e.g., "Mining · 2/3")
- capacity validation: rejects saving a page that would push
  pages × 9 + |bring| past carried capacity

Exit criteria:

- the player can save a Kit with 2 or 3 pages, activate it, and cycle through
  pages without manually opening inventory
- inactive pages' items live in carried main-inventory slots and remain there
  across page swaps
- missing items leave ghost slots on the active page; switch still succeeds

Tests:

- activate 3-page Kit and cycle through each page back to page 1; assert
  resulting authority matches expected layouts
- capacity validation prevents saving an over-sized Kit
- partial-miss: one item missing means that slot is empty after activation

### Slice 5: Bring List And Kit-Active Protection

Goal:

- finish the core Kit concept by adding non-hotbar items and automatic
  protection while active

Deliverables:

- `bring: List<ItemIdentity>` on `KitDefinition` and its domain service ops
- Kit Card renders bring area below pages; drag-to-add and remove supported
- Kit-active protection layers on top of `ProtectionPolicy`: belt items,
  offhand item, and bring items are protected from trash/void/cleanup
- deactivation clears the Kit's protection layer
- bring readiness shown in Kit Card header alongside belt readiness

Exit criteria:

- cleanup or trash actions visibly skip Kit-active items and explain why
- bring readiness reflects current carried identity presence
- changing bring while a Kit is active updates protection immediately

Tests:

- protection composition: a trash request against a Kit-active item fails
  closed with the expected diagnostic
- bring readiness derivation from authority snapshot
- Kit deactivation removes protection

### Slice 6: Drag-To-Kit Editing

Goal:

- support building Kits incrementally without always snapshotting

Deliverables:

- drag a home from the atlas onto a Kit Card belt slot: sets identity for
  that slot (if Kit active, applies the belt change)
- drag a home onto a Kit Card bring area: adds to bring
- drag a slot or bring cell off the card: removes it
- right-click slot or bring cell: remove / edit context menu

Exit criteria:

- Kit editing does not require re-snapshotting the belt
- edits to the active Kit take effect immediately through the intent router
- edits to an inactive Kit only change the Kit definition

Tests:

- domain-service update-slot and update-bring ops
- active-Kit edits dispatch correct `LoadoutApplyService` requests
- inactive-Kit edits do not emit any action requests

### Slice 7: Gather Missing

Goal:

- help the player assemble a Kit's missing items without inventing remote
  authority

Deliverables:

- "Gather" affordance on Kit Cards that enumerates missing identities
- camera pans through missing homes in sequence (tap to advance, or auto-play
  at a comfortable cadence)
- if an identity has last-seen external memory (once that lands), surface a
  plain-language hint for it
- no auto-withdraw; actions remain player-driven

Exit criteria:

- gather surfaces missing items clearly and navigates to their homes
- gather does not silently mutate any inventory state
- gather is a no-op when the Kit has no missing items

Tests:

- derivation of the missing list from Kit + authority snapshot
- gather step advancement state machine

### Slice 8: Persist Kits

Goal:

- promote Kit state from in-memory to workflow-domain persistence

Deliverables:

- `KitDefinition` stored in `WorkflowDomainSnapshot` and persisted through the
  existing platform persistence bridge
- migration/versioning rules matching the visual home map approach
- Kits survive client restart and world reload

Exit criteria:

- saved Kits reload with their pages, bring lists, and protection intact
- versioning is in place so future schema changes are safe

Tests:

- persistence round-trip for a realistic Kit (3 pages, 5 bring items,
  protection flags)
- versioning/migration happy path

### Slice 9: Undo For Kit Activation

Goal:

- deliver the undo guarantee the design assumes in place of diff previews

Deliverables:

- activation and page-switch capture a pre-operation snapshot (belt, offhand,
  affected carried slots)
- an undo affordance in the Kit Rack and a matching hotkey
- undo replays inverse requests through the intent router to restore the
  pre-activation state where possible
- clear diagnostic when undo cannot fully restore (e.g., an item was consumed
  between activate and undo)

Exit criteria:

- activating a Kit and immediately undoing returns the belt and offhand to
  their previous state for all items still present
- undo is a no-op when there is nothing to undo
- undo does not silently delete or fabricate items

Tests:

- activate, undo, verify authority state equals pre-activation baseline
- activate, consume an item, undo; verify partial-restore diagnostic

## Testing Priorities

Highest-value near-term coverage for the Kit prototype:

- Kit domain service create/update/activate/switch-page correctness against
  deterministic authority fixtures
- `LoadoutApplyService` reuse per page produces the expected request sequence
- capacity validation
- protection composition while Kit-active
- LDLib RPC command handling for Kit mutations
- view-model refresh after Kit state change
- architecture test keeping LDLib imports out of common
- undo round-trip for activate and switch-page

## Definition Of Done For This Prototype

This prototype is complete when:

- the Belt is camera-anchored and replaces the inline hotbar row
- Kits are the only user-facing task-grouping concept; collections are gone
- Kit create, activate, deactivate, switch-page, and edit all work through
  the LDLib workspace
- multi-page Kits work with deterministic carried-slot bindings
- Kit-active protection integrates with `ProtectionPolicy`
- gather guides the player through missing items without mutating closed
  storage
- Kits persist across restart
- undo covers activate and switch-page
- all mutations continue to route through the existing intent router; no
  client-side authority is introduced
- the LDLib workspace remains transport/presentation only and the common
  kernel continues to own inventory semantics
