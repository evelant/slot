# SLOT Current Implementation Plan

Last updated: 2026-04-16

This is the near-term engineering sequence from the current baseline. For the
short operational handoff, read [../status.md](../status.md) first.

For product goals, see [../product/direction.md](../product/direction.md).
For current architecture, see [../architecture/overview.md](../architecture/overview.md).
For action semantics, see
[../architecture/action-taxonomy.md](../architecture/action-taxonomy.md).
For the LDLib2 workspace decision, see
[../decisions/0002-ldlib2-workspace.md](../decisions/0002-ldlib2-workspace.md).

## Current Baseline

The common inventory kernel, LDLib2 workspace transport, and first pan/zoom
player-inventory atlas proof are in place. The immediate phase is not another
kernel rewrite and not further list-screen work; it is refining the atlas into
a triage-first visual-home prototype over the same server-owned transport
boundary.

Landed baseline:

- immutable authority snapshots and exact source/entry modeling
- slot-backed and provider-entry-backed authority
- pane-scoped projections and browse documents
- workflow/activity runtime for collections, loadouts, favorites, junk,
  protection, recents, activity, and persistence-facing state
- session coordinator, command preflight, intent router, pending action state,
  and routed outcomes
- routed crafting/tool pipeline for selected-row placement, cursor placement,
  cursor drag distribution, result extraction, tool actions, and tool toggles
- host-aware canonicalization for source, quick-access, equipment, tool region,
  and outcome targets
- verb-based action taxonomy:
  `InventoryActionKind + InventoryActionQuantity + InventoryActionScope +
  InventoryActionConflictPolicy`
- common workspace composition model in `inventory/workspace`
- NeoForge observed screen/menu context
- LDLib2 player-inventory replacement menu, server-owned workspace session,
  server-bound workspace view model, and RPC transfer command path
- server-side workspace request construction from live authority
- high-signal diagnostics/logging for workspace transfers and action outcomes
- LDLib2 `GraphView` carried-atlas proof with pan, zoom, item-card selection,
  progressive disclosure, translucent workspace chrome, search/navigation
  overlay, camera preservation, and hotbar transfer behavior

## Current Risk Register

### 1. Triage/Home Loop Is Not Proven Yet

The atlas surface works as a proof of concept, but the core visual-memory loop
still needs implementation and manual proof.

Risk:

- pan/zoom and hotbar transfer can work while the home-assignment workflow still
  feels too slow, too abstract, or too easy to confuse with real inventory
  movement

### 2. Automatic Categorization Can Break Trust

The previous category/list prototype showed that broad automatic classification
is hard to get right in heavily modded packs.

Risk:

- if SLOT silently puts an item in the wrong visual home, the player learns not
  to trust the atlas

Mitigation:

- route ambiguous items to `Triage`
- auto-home only high-confidence obvious building blocks at first
- treat heuristic categories as suggestions until the player confirms placement

### 3. Visual Home Movement And Real Item Movement Can Be Confused

A visual home is presentation state. Moving it must not imply moving a physical
stack.

Risk:

- drag/drop and map gestures can blur the difference between organizing the
  atlas and mutating inventory

Mitigation:

- start with click-to-assign from `Triage` to an island/header
- keep real stack movement on explicit hotbar/action targets
- add precise drag-to-reposition only after the safer assignment flow is clear

### 4. Dual-Pane And Compat Hosts Are Not Proven

Host observation and common workspace composition exist, but container,
terminal, backpack, and tool-heavy hosts are still future slices.

Risk:

- player inventory success does not automatically prove external host
  ownership, provider entries, compat tool surfaces, or EMI bounds behavior

### 5. Advanced Actions Are Vocabulary, Not Implemented Behavior

`SWAP`, `TRASH`, `VOID`, `SORT_SOURCE`, `DISTRIBUTE`, `COLLECT_MATCHING`, and
`SET_FILTER` are explicit domain vocabulary but should fail closed until their
planners/executors are implemented.

Risk:

- exposing these actions in UI before planner support would recreate the same
  semantic drift the taxonomy was meant to prevent

## Ground Rules

1. One user action gets one authoritative pipeline.
2. UI code renders server-owned state and emits narrow intents/RPC commands; it
   does not choose inventory semantics.
3. The client must not provide authoritative stack, count, identity, host id, or
   menu ref for real inventory mutations.
4. Unsupported or ambiguous integrations fail closed with diagnostics.
5. Broad actions plan against backing entries, not aggregate counts alone.
6. Crafting stays slot-backed and descriptor-driven.
7. LDLib2 owns UI transport and widget composition, not SLOT's inventory domain.

## Next Execution Order

### 1. Clean Up Atlas Readability

Goal:

- make the current atlas proof readable enough to evaluate item-home behavior

Deliverables:

- lower-noise or non-aliased atlas background
- tighter item-card padding and typography
- compact medium-zoom summary text
- selected-item inspector or popover for full item ids and source details
- no return to the old list-first prototype

Exit criteria:

- normal browsing and detail zoom remain readable without common labels
  ellipsizing or overflowing the screen

### 2. Add Triage-First Projection

Goal:

- replace broad automatic classification with a trusted first-contact flow

Deliverables:

- `Triage` island as the default for unhomed identities
- `Blocks` starter island for obvious high-confidence placeable building blocks
- projection state that marks `TRIAGE`, `HIGH_CONFIDENCE_AUTO`, and
  `PLAYER_PLACED`
- tests proving ambiguous, modded, and multi-use items stay in `Triage`

Exit criteria:

- new items land somewhere obvious and trusted instead of being silently sorted
  into questionable categories

### 3. Add In-Memory Home Assignment

Goal:

- prove that the player can place an item identity once and rely on that place
  during the current session

Deliverables:

- selected triage card state
- click island header to assign selected item to that island
- click empty atlas space to create a new player island seeded by selected item
- in-memory `VisualHomeAssignment` state
- preservation of camera, search query, selection, and homes through LDLib view
  refreshes

Exit criteria:

- future copies of a placed identity appear at that home for the rest of the
  session
- visual-home commands remain separate from real inventory mutation commands

### 4. Add Basic Island Management

Goal:

- make player-created islands usable enough to judge the visual-memory loop

Deliverables:

- island rename
- island recolor
- island icon from seed item
- visual island move without real inventory movement
- deletion only for empty player islands

Exit criteria:

- player-authored islands are understandable without adding persistence yet

### 5. Restore Search As Spotlight

Goal:

- keep search useful while preserving stable visual homes

Deliverables:

- in-place match highlighting
- non-match dimming without layout replacement
- camera jump or cycle through matches
- optional compact result tray if spotlight is not precise enough

Exit criteria:

- search teaches where items live instead of replacing the map with a transient
  list

### 6. Persist Visual Homes

Goal:

- promote the proven in-memory home model into durable user organization state

Deliverables:

- workflow-domain `VisualHomeMap` state
- migration/versioning rules
- player-authored home precedence over suggestions
- ghost homes for important absent identities

Exit criteria:

- placed homes survive client restart/world reload without implying physical
  source authority

### 7. Later Feature Tracks

Keep these deferred until the carried triage/home loop is proven:

- dual-pane and active external source islands
- physical storage memory and find/restock trails
- equipment/offhand/cursor gestures (beyond the Belt offhand adjacency)
- recipe viewer integrations
- trash, void, sort, and recovery
- Kits prototype: camera-anchored Belt, toggleable Kit Rack, task-shaped Kit
  concept with multi-page belt switching and bring lists. See
  [../design/kits.md](../design/kits.md) for the concept and
  [kit-prototype.md](kit-prototype.md) for the slice sequence. This
  supersedes the earlier "task boards, loadouts, and workflow rails" bullet
  and the abandoned sidebar-style collections prototype.

## Testing Priorities

Highest-value near-term coverage:

- LDLib RPC command handling and view-model refresh
- server-side request construction from authority
- exact hotbar assignment semantics
- player-bound target resolution
- action policy/protection rejection
- atlas projection to `Triage`, `Blocks`, and player-authored homes
- in-memory `VisualHomeAssignment` precedence over suggestions
- home assignment commands that do not mutate inventory
- atlas camera/query/selection/home preservation across refresh
- session refresh and stale menu rejection
- common workspace composition output
- architecture assertions keeping LDLib2 imports out of common domain code

## Definition Of Done For This Phase

This phase is complete when:

- the LDLib atlas is the primary player-inventory workspace manually and in
  tests
- hotbar assignment/transfer continues to route through the taxonomy model
- `Triage`, high-confidence `Blocks` auto-home, and player-authored in-memory
  homes work through refreshes
- search spotlights stable homes without replacing the map
- screen/client code remains transport and presentation only
- visual-home changes remain separate from real inventory mutations
- diagnostics remain good enough to explain failed actions from logs
- external storage memory, task boards, and advanced actions remain deferred
  unless their planner/executor semantics are explicitly landed
