# SLOT Architecture

Last updated: 2026-04-15

This document is the living source of truth for SLOT's current core inventory
model. It describes how authority, projection, actions, crafting surfaces, and
the workflow/activity runtime fit together.

For product direction, see [../product/direction.md](../product/direction.md).
For current operational status, see [../status.md](../status.md).
For user-visible behavior, see [../product/spec.md](../product/spec.md).
For the current execution sequence, see [../plans/current.md](../plans/current.md).

## Core Architecture Principles

- Exact inventory authority and derived UI projections are different layers.
- Inventory authority is real inventory truth. Workflow and activity logs are
  supporting domain history and read-model state, not replacement authority.
- UI consumes projections and emits intents; it does not invent inventory truth.
- Every real mutation must resolve to an authoritative inventory operation.
- Host-aware canonicalization owns action identity for conflict detection,
  protection checks, and outcomes.
- Durable workflow semantics and inventory activity are event-backed. Browse
  posture remains snapshot state.
- Integrations describe sources and surfaces through SLOT-owned descriptors and
  targets rather than exposing foreign widgets as authority.

## Authority Layer

The authority layer represents the exact inventory state SLOT can read and plan
against for the active host/session.

Primary types:

- `InventoryAuthoritySnapshot`
- `InventorySourceSnapshot`
- `InventoryEntrySnapshot`
- `InventoryEntryKey`

### `InventoryAuthoritySnapshot`

`InventoryAuthoritySnapshot` owns:

- the active `InventoryHostDescriptor`
- `sourcesById`
- cursor state

It is the authoritative read model for:

- query services
- workflow services
- action planning
- UI projection building

### `InventorySourceSnapshot`

Each source snapshot represents one real source, not one UI grouping. Sources
stay distinct even when a later projection merges their visible rows.

Examples:

- player main inventory
- a quick-access lane source
- one physical carried backpack
- one open chest
- one terminal source

### `InventoryEntrySnapshot` and `InventoryEntryKey`

`InventoryEntryKey` distinguishes two backing kinds:

- `SLOT`: exact slot-backed authority with a local logical slot index
- `PROVIDER_ENTRY`: provider-owned list entries with a stable `entryId`

This is the key distinction that lets SLOT represent both exact slot inventories
and terminal-like provider inventories without lying about them.

## Projection Layer

The projection layer derives user-facing working sets from authority. It does
not replace authority.

Primary types:

- `InventoryWorkingSetProjection`
- `ProjectedInventoryRow`
- `ProjectedEntryRef`
- `InventoryWorkingSetProjectionService`

### `InventoryWorkingSetProjection`

`InventoryWorkingSetProjection` is a pane-scoped derived view over an
`InventoryAuthoritySnapshot`.

It answers questions like:

- what rows are visible in the carried pane?
- what rows are visible in the external pane?
- what backing entries and sources produced each row?

### `ProjectedInventoryRow`

A projected row contains:

- one visible identity
- pane membership
- visible total count
- ordered `backingEntries`
- ordered `backingSources`

This is how SLOT can show one merged carried row for `3 + 2 + 5` while still
retaining the exact backing entries needed for deterministic broad actions.

### `ProjectedEntryRef`

Each `ProjectedEntryRef` points back to a real `InventoryEntryKey` plus the
stack/count that made it visible. The UI can merge rows, but it must not lose
those exact backreferences.

## Browse Layer

The browse layer turns authority plus workflow/activity read models into a
single UI-independent browse document.

Primary types:

- `InventoryBrowseRequest`
- `InventoryBrowseDocument`
- `InventoryBrowsePane`
- `InventoryBrowseSection`
- `InventoryBrowseEntry`
- `InventoryBrowseService`

`InventoryBrowseService` currently consumes:

- `InventoryAuthoritySnapshot`
- `WorkflowDomainSnapshot`
- `InventoryBrowsePreferences`
- `InventoryBrowseSessionState`
- item identity resolution
- category policy

and produces one browse document that UI hosts can render without rebuilding
core semantics themselves.

Important consequences:

- merged rows, placeholders, loadout rows, command availability, and browse
  annotations are core-owned output
- browse documents derive from workflow and activity projections rather than
  from screen-local recents/favorites/cleanup bookkeeping
- screen hosts should treat browse documents as presentation input, not as a
  place to invent their own grouping or command meaning

## Action Model

The action layer turns user intent into authoritative requests and outcomes.
The current action taxonomy is documented in
[action-taxonomy.md](action-taxonomy.md).

Important types:

- `InventoryActionKind`
- `InventoryActionQuantity`
- `InventoryActionConflictPolicy`
- `InventoryActionTarget`
- `InventoryActionScope`
- `InventoryActionDestination`
- `InventoryActionRequest`
- `InventoryActionOutcome`
- `ProjectedRowTransferIntent`
- `ProjectedRowTransferPlan`
- `ProjectedRowTransferPlanner`

### Verbs, Quantity, Scope, And Conflict

SLOT action requests are verb-based. Count or target variants are expressed as
dimensions on the request, not separate action kinds.

Examples:

- move a stack into compatible storage:
  `TRANSFER + STACK + SINGLE_TARGET + INSERT_ONLY`
- move one item:
  `TRANSFER + ONE + SINGLE_TARGET + INSERT_ONLY`
- assign a stack into an exact hotbar target:
  `ASSIGN + STACK + SINGLE_TARGET + ASSIGN_WITH_DISPLACE`
- place one cursor item:
  `CURSOR_PLACE + ONE + SINGLE_TARGET + INSERT_ONLY`

This keeps UI gestures, command names, and server mutation semantics separate.
Commands such as `TRANSFER_ONE` remain UI command ids; they plan into canonical
domain requests.

### Targets

`InventoryActionTarget` currently distinguishes:

- `SourceTarget(sourceId)` for source-wide targets
- `SourceSlotTarget(sourceId, slotIndex)` for exact slot-backed targets
- `SourceEntryTarget(sourceId, entryId)` for provider-entry targets
- quick access, equipment, tool region, and tool control targets

Host-aware canonicalization resolves aliases such as quick access, equipment,
and linked tool regions through their real backing source identity.

### Scope

`InventoryActionScope` expresses how broad an action should be:

- `SINGLE_TARGET`
- `BEST_SINGLE_SOURCE`
- `SELECTED_TARGETS`
- `VISIBLE_MATCHES`
- `VISIBLE_ROWS`
- `SOURCE_LOCAL`
- `COLLECTION`
- `LOADOUT`
- `ALL_MATCHING_IN_HOST`

### Destinations

`InventoryActionDestination` keeps broad transfer planning explicit:

- `PaneDestination(CARRIED|EXTERNAL)`
- `SourceDestination(sourceId)`

The planner decides how to map those high-level destinations to real
authoritative operations.

## Projected Row Transfer Planning

`ProjectedRowTransferPlanner` is the current core planner for projected-row
transfer actions.

It accepts:

- exact authority
- caller-supplied visible rows in UI order
- an optional anchor row
- an explicit action kind, quantity, and scope
- an explicit destination
- protection policy and mode

It returns ordered concrete `InventoryActionRequest`s over exact backing
entries. It does not guess from aggregate counts alone.

Current semantics:

- `BEST_SINGLE_SOURCE` chooses one backing entry in deterministic order
- `VISIBLE_MATCHES` fans out over all visible matching rows
- `VISIBLE_ROWS` fans out in visible row order, then backing-entry order
- slot-backed extractions target `SourceSlotTarget`
- provider-entry extractions target `SourceEntryTarget`
- broad inserts target `SourceTarget`

The planner uses an in-memory `PlannedAuthorityLedger` to simulate builtin
capacity and to track provider uncertainty where exact simulation is not
available.

## Workspace Composition Layer

The workspace layer translates session output into a UI-neutral model that
screen hosts can render without owning inventory semantics.

Primary types:

- `InventoryWorkspaceComposer`
- `InventoryWorkspaceModel`
- `InventoryWorkspaceProfile`
- `InventoryWorkspaceZone`
- `InventoryWorkspaceSurface`
- `InventoryWorkspaceSubjectRef`
- `InventoryWorkspaceStatus`

This layer consumes session snapshots, browse documents, host descriptors, and
profile rules. It produces ordered zones and surfaces with stable subject
identity. It must not depend on widget classes, pixel geometry, LDLib2, or
NeoForge events.

The current LDLib player workspace uses a compact server-owned view model for
the first list/hotbar slice, but that view model must still derive from SLOT
authority and preserve the same domain boundary: UI transport is not inventory
authority.

## Workflow And Activity Runtime

The workflow/runtime layer is now a hybrid event-backed domain model that sits
beside inventory authority.

Primary types:

- `DomainEventEnvelope`
- `WorkflowEventStore`
- `InventoryActivityStore`
- `WorkflowProjection`
- `ActivityProjection`
- `WorkflowDomainSnapshot`
- `CollectionWorkflowDomainService`
- `WorkflowDomainRuntime`

### Workflow Event Log

The workflow log records durable user meaning:

- user collections and membership
- desired counts
- loadout definitions
- favorite tags
- junk tags
- protection rules
- recent dismissal watermarks

This log is not for transient browse posture such as search text, active pane,
or pinned tools.

### Activity Event Log

The activity log records bounded inventory activity and external observations.

Current shape:

- bounded persisted history, default `512` retained events
- authoritative or observed acquisitions, transfers, crafting, smelting,
  trash/overflow, restore, and void events
- room for explicit external producers such as world pickup, merchant trade,
  quest reward, or compat-provided deltas

The activity log exists to explain what happened across sessions and UI hosts.
It does not replace current inventory authority.

### Workflow And Activity Projections

The logs feed two read models:

- `WorkflowProjection`
- `ActivityProjection`

`WorkflowProjection` currently owns:

- collections
- memberships
- desired counts
- loadouts
- favorite tags
- junk tags
- protection state
- recent dismissal watermarks

`ActivityProjection` currently owns:

- `RecentView`
- cleanup candidates
- undo/recovery candidates

Important current rules:

- favorites and junk are first-class tags, not synthetic built-in collections
- recents are derived from activity, not from rebuilding baseline state
- dismissing a recent records the highest seen activity sequence for that
  identity so a later acquisition can surface again

### Snapshot State That Stays Out Of The Logs

Browse posture remains snapshot state:

- `InventoryBrowsePreferences`
- `InventoryBrowseSessionState`

That includes state such as:

- search/filter text
- sort/grouping mode
- pane mode and active pane
- selected collection/loadout
- pinned tool
- expanded sections

This state should survive refresh and persistence, but it is not treated as
durable business history.

### Persistence Shape

`WorkflowDomainSnapshot` persists:

- workflow projection checkpoint
- workflow event tail
- activity projection checkpoint
- activity event tail
- browse preferences
- browse session state

Legacy snapshot-only persistence migrates by seeding projection checkpoints
directly. It does not synthesize fake historical events.

### Current Write Surfaces

`CollectionWorkflowDomainService` is the main durable workflow write surface for
collections, tags, desired counts, and loadouts.

`WorkflowDomainRuntime` owns:

- workflow writes for protection and recent dismissal
- activity recording from authoritative outcomes or explicit external activity
- persistence triggers

Inventory mutation still happens through the action pipeline, not through the
workflow/activity logs.

## Crafting And Tool Surfaces

Crafting stays slot-backed even when presented through compact tool panels.

Important types:

- `InventoryToolDescriptor`
- `CraftingSurfaceDescriptor`
- tool-region targets linked to backing sources

`CraftingSurfaceDescriptor` gives the core exact:

- input slot targets
- output slot target
- grid dimensions
- crafting-surface capability flags such as immediate craft, clear, balance,
  and rotate support

Tool regions are presentation and routing aliases over linked source slots.
They are not independent inventory authority.

## Transfer Routing Layers

`InventoryActionExecutor` is a two-layer orchestrator over the primitives in
`BuiltinInventoryActionExecutor` (direct `MENU` / `PLAYER` slot manipulation) and
`InventoryMutationRouter` (dispatch to `PlayerInventoryExtension.mutate` for
`PROVIDER` / `TOOL` sources). Every action verb is funnelled through the
executor — integrations and session code must never call the builtin layer
directly.

### Layer responsibilities

- **`BuiltinInventoryActionExecutor`** owns the fast path for vanilla-style
  inventories. It resolves `SourceSlotTarget` / `QuickAccessTarget` /
  `EquipmentTarget` against `host.topology()` and reads/writes the
  `AbstractContainerMenu` slot or the `Inventory` player-slot array directly.
  `PROVIDER` and `TOOL` bindings intentionally fall through — the executor
  returns `non_builtin_target_route` / `unsupported_builtin_extract_route`
  / `unresolved_target`. These diagnostics are **boundary-skip markers**, not
  real failures; they signal "not my layer, try the other one."
- **`InventoryMutationRouter`** receives a fully-resolved
  `InventoryMutationRequest` (sourceId + optional slot + identity + stack) and
  dispatches by ownership: `host.ownsHostSource(...)` → `hostSession.mutate`,
  otherwise `host.extensionOwningSource(...)` → `extension.mutate`, otherwise
  fall back to `BuiltinInventoryActionExecutor.mutateSource` (for
  `PLAYER_MUTATION` / `MENU_MUTATION` action routes). If the source declares
  `PROVIDER_MUTATION` action route and no owner is found, the router reports
  `provider_route_missing_owner`. Extensions are the canonical way to plug a
  provider-backed carried container (e.g., Sophisticated Backpacks) into SLOT.
- **`InventoryActionExecutor.executeTransfer`** is the hinge. It tries
  `BuiltinInventoryActionExecutor.transfer` first (fast path when both ends
  are builtin-handleable); on failure it decouples extract + insert and tries
  both layers on each side independently. All four combinations must work:

    | Source binding | Destination binding | Path |
    | --- | --- | --- |
    | `MENU`/`PLAYER` | `MENU`/`PLAYER` | builtin ext + builtin ins |
    | `PROVIDER`/`TOOL` | `MENU`/`PLAYER` | provider ext + builtin ins |
    | `MENU`/`PLAYER` | `PROVIDER`/`TOOL` | builtin ext + provider ins |
    | `PROVIDER`/`TOOL` | `PROVIDER`/`TOOL` | provider ext + provider ins |

  Every combination has explicit coverage in
  [`InventoryActionExecutorTest`](../../common/src/test/java/dev/imagio/slot/inventory/integration/InventoryActionExecutorTest.java).

### Diagnostic surfacing

When a transfer fails, the outcome's diagnostic message should point at the
real reason, not a layer-boundary marker. `preferProviderDiagnostic` in the
executor recognizes both classes of boundary marker — builtin's
`non_builtin_target_route` / `unresolved_target` / `unsupported_builtin_*`
and provider's `source_is_not_provider_backed` /
`target_is_not_provider_backed` / `provider_source_missing_from_host` — and
prefers whichever layer emitted a *meaningful* diagnostic. When both layers
only emitted boundary markers, they're joined with `" | provider:"` so the
operator can see the full shape of the failure. Provider-layer diagnostics
carry a `:sourceId=bindingRoute` suffix so host-topology drift is obvious
from the outcome alone.

### ASSIGN has no provider fallback

`ASSIGN`'s in-place swap semantics require both ends to be `PLAYER`-bound
(see `BuiltinInventoryActionExecutor.assign` — `assign_requires_player_bound_targets`).
`executeAssign` does not fall back to the provider layer because the
semantics don't translate. Any code that needs to move an identity from a
provider-backed source (e.g., a backpack) into a quick-access slot must emit
`TRANSFER` + `INSERT_ONLY`, not `ASSIGN`. `LoadoutApplyService` follows this
rule automatically: when the candidate source isn't player-bound, the apply
request is emitted as `TRANSFER` + `INSERT_ONLY` + `STACK` and the prior
stage step guarantees the target slot is empty by apply time.

### Restore-via-same-layer invariant

When a transfer partially succeeds (insert returns a remainder) or fails
entirely after a successful extract, the un-inserted portion must be
restored through **the same layer that performed the original extract**.
Using the wrong layer silently loses items — e.g., writing a provider-
extracted stack back via `BuiltinInventoryActionExecutor.insert` on a
`PROVIDER`-bound source just returns `non_builtin_target_route` and the
stack vanishes. `ExtractionResult.viaProvider()` tracks the layer so
`restoreExtracted` routes through `InventoryMutationRouter.mutate` when
appropriate.

### Routing preference (`stableOrder`)

Sources are ranked by `stableOrder` (lower = tried first) for both
extraction-candidate search and insertion-destination allocation. The
invariant is **overflow-to-backpack**: backpack sources (stableOrder
15–50, varies by carrier slot) come before `PLAYER_MAIN` (100) come before
`PLAYER_QUICK_ACCESS_LANE_0` (110) come before armor (120) and offhand
(130). The ordering applies consistently everywhere `stableOrder` is
consulted — `PlacementPolicy.orderedInsertionSources`,
`ProjectedTransferDestinationAllocator.paneSources`, `LoadoutApplyService`'s
`findCandidateSource` and `findStagingTarget` — so the user's main
inventory only fills once overflow storage is exhausted.

## Integration Boundaries

Integrations should convert foreign systems into SLOT-owned descriptors and
targets as early as possible.

Current rules:

- carried integrations preserve one source per physical carried container
- Tom's-style terminals expose provider-entry authority instead of synthetic
  fake slots
- tool/crafting integrations expose linked slot-backed sources plus tool
  descriptors
- bridge failures are explicit diagnostics, not silent fallback into guessed
  behavior

### Storage abstraction layer

Beneath the integration provider layer sits a smaller, more prescriptive
layer that unifies *how we actually read and mutate storage*:

- `CarriedSourceAccess` + `CarriedProvider` + `CarriedProviderRegistry` for
  player-adjacent storage (vanilla, backpacks, curios, future mods)
- `WorldStorageAccess` + `WorldStorageAccess.Delegate` for block-bound and
  virtual/aggregated storage (chests, drawers, AE2 networks)
- `DefaultCarriedProviderIntegration` auto-synthesises a minimal
  `PlayerInventoryExtension` from every registered `CarriedProvider`, so
  simple carried mods never need to hand-write integration code

This is the layer to edit when adding a new storage mod — not the
executors, not the UI sessions. See
[storage-integration.md](storage-integration.md) for SPI shapes, concrete
recipes for Carried / World / virtual storage, the `Player` vs
`ServerPlayer` convention, the opt-out flag for rich (SB-class)
providers, and the invariants that prevent the "executor-is-blind-to-
storage-X" bug class from returning.

## Current Open Architectural Work

The main authority/projection/action/session/workspace primitives are now
landed. The largest remaining architecture work is at the host and integration
boundary:

- manual proof and hardening of the LDLib player-inventory workspace
- richer list-first UI behavior over server-owned view models and RPC
- dual-pane hosts over the existing projection, workflow, and action contracts
- broader explicit external-activity signal bridges where integrations can
  provide them, with conservative invalidation remaining the fallback
- recipe-assisted workflows and cleanup/recovery semantics built on the current
  routed action and activity model without reintroducing screen-owned semantics
