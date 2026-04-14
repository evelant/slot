# SLOT Current Implementation Plan

Last updated: 2026-04-14

This is the near-term engineering plan from the current post-rewrite baseline.
It is not the product-direction document and not the normative behavior spec.

For product goals, see [PRODUCT_DIRECTION.md](PRODUCT_DIRECTION.md).
For current architecture, see [ARCHITECTURE.md](ARCHITECTURE.md).

## Current Baseline

The core rewrite established the main inventory kernel that future UI
experiments should build on.

Landed baseline:

- immutable authority snapshots:
  - `InventoryAuthoritySnapshot`
  - `InventorySourceSnapshot`
  - `InventoryEntrySnapshot`
  - `InventoryEntryKey`
- explicit slot-backed vs provider-entry authority
- derived pane-scoped working-set projection through
  `InventoryWorkingSetProjectionService`
- typed browse documents over authority plus workflow/activity projections
  through `InventoryBrowseService`
- merged rows that retain ordered backing entries and backing sources
- source-wide, slot-backed, and provider-entry action targets:
  - `SourceTarget`
  - `SourceSlotTarget`
  - `SourceEntryTarget`
- projected-row transfer planning through:
  - `InventoryActionScope`
  - `InventoryActionDestination`
  - `ProjectedRowTransferPlanner`
- host-aware canonicalization for quick access, equipment, tool regions, and
  outcomes
- `CraftingSurfaceDescriptor` for crafting-capable tool surfaces
- workflow/loadout services consuming authority snapshots instead of legacy
  parallel maps
- hybrid event-backed workflow/activity runtime through:
  - `WorkflowEventStore`
  - `InventoryActivityStore`
  - `WorkflowProjection`
  - `ActivityProjection`
- favorites and junk as first-class workflow tags instead of synthetic built-in
  collections
- recent dismissal keyed to activity sequence rather than transient visible
  state
- checkpoint-plus-event-tail persistence with direct legacy snapshot migration
- old mutable workflow/acquisition store patterns removed instead of preserved
  as facades

This means the next work should not reopen the core authority/projection split.
The next work should build above that kernel and above the new event-backed
workflow/activity runtime.

## Current Status Review

At a high level, the core is now strong enough to support real UI experiments
without reopening the inventory kernel again.

What is solid:

- exact authority is explicit and no longer conflated with visible rows
- merged carried browsing retains exact backing refs for deterministic actions
- provider-entry inventories are modeled honestly instead of as fake slots
- row-driven transfer planning exists and is tested
- browse documents are now a real core output instead of screen-owned assembly
- durable workflow semantics and bounded inventory activity are event-backed
- legacy mutable workflow store patterns are removed

What is only partially landed:

- typed browse/workflow/mutation intents exist, but there is still no central
  router executing them end to end
- browse command availability exists in browse documents, but there is still no
  command-invocation layer that maps those commands back into intents
- the event envelope already has `correlationId`, `causationId`, and
  `sessionId`, but the current runtime does not propagate real values through
  workflow events and activity events yet
- external activity can be appended explicitly, but there is still no session
  coordinator for invalidation, refresh ownership, and conservative diff
  fallback

What is still missing for the decoupled-UI target:

- one refresh-owned path from `browse command or typed intent` to
  `workflow write or action planning` to `authoritative outcome` to
  `activity recording` to `rebuilt browse document`
- a clean external-signal path for pickups, trades, quest rewards, and compat
  deltas that happen outside direct SLOT row actions
- one end-to-end crafting pipeline across row-driven and cursor-driven actions

## Known Gaps And Risks

These are the highest-signal issues still visible from the current code and
plan.

### 1. Intent Scaffolding Is Ahead Of Executable Routing

The repo now has typed intent families in:

- `InventoryBrowseIntent`
- `InventoryWorkflowIntent`
- `InventoryMutationIntent`

but they are still just data contracts. There is no `InventoryIntentRouter`,
no command invocation surface, and no shared router result model yet.

Practical consequence:

- UI hosts would still need to branch on commands/intents locally to do
  anything meaningful
- that is the main remaining blocker for truly decoupled UI

### 2. Browse Selection Is Inconsistent

`InventoryBrowseIntent.SelectEntry` already exists, but
`InventoryBrowseSessionState` does not currently persist a selected entry id,
and item/placeholder browse rows do not expose selected state.

Practical consequence:

- there is no clean shared notion of row selection yet
- a UI host would still need local selection semantics for normal item rows

### 3. External Activity Handling Is Still Underdefined In Code

The event-backed runtime can record explicit activity events, but there is no
coordinator yet for:

- authority invalidation
- explicit external signals
- conservative diff inference when explicit hooks are absent
- refresh ownership across open sessions

Practical consequence:

- recents and cleanup can be correct for direct routed outcomes today
- they are not yet guaranteed to stay correct for all world/mod-driven changes
  while a UI is open

### 4. Crafting Intents Are Not Yet Rich Enough For The Planned Pipeline

`InventoryMutationIntent.CraftingSurface` is still just a thin identifier-level
contract. It does not yet capture enough detail for:

- selected-row placement vs cursor placement
- left/right click differences
- drag-distribution semantics
- result extraction/refill sequencing

Practical consequence:

- crafting surfaces are modeled correctly at the descriptor level
- the action/intention layer above them is still under-specified

### 5. Trash / Overflow / Recovery Are Only Partially Designed

The activity model now has the right vocabulary for:

- `TRASHED`
- `OVERFLOW_STAGED`
- `RESTORED`
- `VOIDED`

but the operational semantics are still open:

- where recoverable trash lives
- when overflow staging happens
- what gets a recovery token
- what the restore path is allowed to do

Practical consequence:

- the event model is ahead of the actual product rules
- this is good groundwork, but it is not a finished feature design

### 6. Correlation Plumbing Exists Structurally But Not Operationally

`DomainEventEnvelope` already has global sequence, stream sequence,
correlation, causation, and session fields. The current runtime still writes
empty correlation/causation/session values.

Practical consequence:

- the model is ready for proper end-to-end tracing
- the router/coordinator pass still needs to make those fields real

## Ground Rules

1. One user action gets one authoritative pipeline.
2. UI code renders projections and emits intents; it does not choose inventory
   semantics.
3. Unsupported or ambiguous integrations fail closed.
4. Broad actions plan against backing entries, not aggregate counts alone.
5. Crafting must stay slot-backed and descriptor-driven.
6. New UI hosts must consume the same authority/projection/browse/action
   contracts.

## Next Execution Order

### 1. Build A Central Intent Router And Session Coordinator

Goal:

- replace screen/helper branching and ad hoc refresh handling with one
  refresh-owned session pipeline

Deliverables:

- `InventoryCommandInvocation` and command-subject routing for item, section,
  and pane commands
- typed intents for row actions, cursor actions, pane transfers, quick-access
  actions, loadout actions, trash/void, and crafting/tool actions
- a router that validates intents against the active session and dispatches to
  planners, workflow writes, or direct operations
- router result/effect types that always return:
  - refreshed authority
  - refreshed browse document
  - typed status/reason data
  - executed outcomes or planner details where relevant
- a session coordinator that owns:
  - authority refresh
  - browse rebuild
  - external activity ingestion
  - explicit invalidation handling
  - conservative diff fallback when explicit hooks are missing
- correlation and result plumbing that ties:
  - typed intents
  - workflow events
  - authoritative outcomes
  - activity events
- browse-state support for real selected-entry persistence instead of only
  selected loadout persistence
- screen code reduced to hit testing, presentation, and intent emission

Exit criteria:

- one user action maps to one intent family and one refresh-owned result path
- browse commands no longer require host-local semantic branching
- row selection exists as shared core state rather than screen-local convention
- external pickups/trades/compat changes no longer require screen-local recent
  or cleanup bookkeeping
- screen-local branching no longer chooses mutation semantics
- outcomes and refresh behavior are driven by shared action results

### 2. Rewrite Crafting As One Pipeline

Goal:

- make selected-row placement, cursor placement, drag distribution, result
  extraction, and refill use one end-to-end crafting pipeline

Deliverables:

- typed crafting intents
- authoritative routing through current crafting surfaces
- unified planner/operation path for selected-row and cursor-driven crafting
  interactions
- tests for left click, right click, drag, result extraction, and blocked cases

Exit criteria:

- tool panels stop owning crafting semantics
- generic carried-drop routing cannot steal crafting inputs
- backpack and terminal crafting surfaces follow the same core contract

### 3. Move Workflow/UI Experiments Onto The New Core

Goal:

- let UI experiments and workflow features consume the new
  projection/action/workflow/activity model directly

Deliverables:

- carried-browser experiments over `InventoryWorkingSetProjection`
- row-driven transfer flows over `ProjectedRowTransferPlanner`
- workflow surfaces for collections, loadouts, recents, cleanup, and future
  recovery flows that consume projections rather than screen-local state

Exit criteria:

- no new feature work depends on old prototype-style view wrappers
- merged-row semantics stay consistent across browsing and workflow flows
- recent and cleanup behavior stays consistent across multiple UI hosts

### 4. Split The Screen Layer Into A Thin Shared Core

Goal:

- make carried-only and dual-pane hosts thin specializations over the same
  projection/intent/action pipeline

Deliverables:

- reusable row/list materialization over projections
- smaller state/render/action modules under the screen layer
- thinner concrete screen classes that mostly configure mode and layout

Exit criteria:

- carried and workspace screens share the same intent and refresh pipeline
- large screen classes no longer own workflow semantics or action selection

### 5. Tighten Compat Bridges Around The New Contracts

Goal:

- keep optional-mod integration aligned with the new authority/projection/action
  model

Deliverables:

- bridge modules that emit SLOT-owned descriptors, targets, and diagnostics
- no reflection leaking into screen, projection, or planner code
- future adapter work for more equipment/accessory ecosystems built on the same
  contracts

Exit criteria:

- bridge failures are explicit and fail closed
- provider-entry and crafting-surface integrations remain first-class instead of
  degenerating into special cases

## Testing Priorities

Highest-value near-term coverage:

- intent routing and outcome sequencing
- external activity ingestion and authority invalidation handling
- crafting placement/drag/result/refill flows
- row-driven bulk transfer behavior across merged carried rows
- protection and workflow interactions with broad actions
- refresh/session preservation across projection rebuilds
- compat bridge receiver guards and fallback behavior
- Recent production and suppression matrix
- activity replay, dismissal watermark, and cleanup/recovery projection behavior
- persistence migration from legacy snapshots into checkpoint-plus-log state

## Definition Of Done For This Phase

This phase is complete when:

- typed intents sit above the current planner/operation layer
- workflow/activity runtime remains the only durable write path for recents,
  cleanup, tags, collections, and loadouts
- crafting uses one pipeline end to end
- UI experiments consume the new core directly
- screen classes are materially thinner
- compat bridges stay narrow and descriptor-driven
- the docs remain aligned with the current architecture instead of with deleted
  prototype abstractions
