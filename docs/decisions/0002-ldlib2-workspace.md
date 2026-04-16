# LDLib2 Workspace Sync Decision

Last updated: 2026-04-15

Status: implemented for the player-inventory LDLib workspace slice.

This document records the decision to adopt LDLib2 more deeply for SLOT's
NeoForge workspace UI transport while keeping SLOT's inventory domain model as
the source of truth.

References:

- [LDLib2 UI Agent Guide](https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/ui/agent_guide/)
- [LDLib2 Data Bindings and RPCEvent](https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/ui/preliminary/data_bindings/)
- [LDLib2 RPC Packet](https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/sync/rpc_packet/)

## Decision

Adopt LDLib2 for the workspace UI transport layer:

- menu lifecycle through LDLib2 menu UI facilities
- server-to-client workspace view-model sync through LDLib2 data bindings
- client-to-server workspace interactions through LDLib2 UI RPC events
- screen composition, layout, styling, and widget behavior through LDLib2 UI

Do not adopt LDLib2 as SLOT's inventory domain:

- inventory authority remains SLOT-owned
- action targets remain SLOT-owned
- host resolution remains SLOT-owned
- transfer planning and mutation execution remain SLOT-owned
- protection policy, canonicalization, workflow, and activity remain SLOT-owned

The selected architecture is full UI transport replacement, not full domain
replacement. LDLib2 replaces the custom workspace UI packet plumbing. SLOT keeps
the authoritative inventory semantics.

## Rationale

The current custom workspace path requires SLOT to maintain screen-specific
networking, status propagation, refresh cadence, and client-side request
construction. LDLib2 already provides menu-scoped initial sync, changed-value
sync, UI RPC dispatch, and server-backed UI lifecycle.

The safer model is also the simpler model:

- the client renders a server-published view model
- the client sends only narrow interaction commands
- the server resolves live source stacks from the current authoritative host
- the server builds and executes SLOT `InventoryActionRequest` values
- the server publishes updated status and view model state back to the client

This improves security relative to the current workspace prototype. The client
should no longer send authoritative source stack, identity, count, host id, or
menu ref fields for workspace actions. Those values should be derived on the
server immediately before execution.

## Boundary

LDLib2 owns:

- player workspace menu opening and menu-scoped UI lifecycle
- `ModularUI` construction and attachment
- S2C display data binding
- UI RPC transport for user interactions
- widget tree composition, local layout, local selection, local focus, and
  local visual affordances

SLOT owns:

- `InventoryHostDescriptor` and host observation inputs
- `InventoryAuthoritySnapshot` and authority reads
- `InventoryActionTarget` variants and canonicalization
- `InventoryIntentRouter`, command preflight, and typed mutation intents
- `InventoryActionRequest` construction on the server
- `InventoryActionExecutor` and `InventoryMutationRouter`
- `ProtectionPolicy`, workflow runtime, activity recording, diagnostics, and
  pending-action semantics

LDLib2 imports must remain out of common domain code. LDLib2 dependencies belong
only in NeoForge UI/menu integration packages.

## Server-Owned UI Session

Add a workspace UI holder/session boundary owned by the LDLib menu instance.
Conceptually this is `SlotWorkspaceUiSession`.

Responsibilities:

- resolve the active player inventory host on the logical server
- read the authoritative inventory snapshot from the server-side player/menu
- project that snapshot into a compact workspace view model
- hold status text, last rejection reason, pending revision, and diagnostics
- expose the view model as S2C-bound LDLib data
- handle LDLib RPC interaction commands
- build server-side `InventoryActionRequest` values and invoke SLOT execution
- refresh and republish view state after every accepted or rejected command

The session is UI-scoped and disposable. It is not workflow persistence and
does not replace `WorkflowDomainRuntime`.

## View Model

Add a serializable/syncable workspace view model containing display-ready UI
data only. Conceptually this is `SlotWorkspaceViewModel`.

It should include:

- revision number
- host diagnostics
- status text
- pending count or pending summary
- grouped main-inventory rows
- hotbar rail slots
- selected quick-access slot index

Main-inventory row fields:

- display stack for icon rendering
- display name
- item id
- total count
- first backing main-inventory slot index

Hotbar slot fields:

- hotbar index
- selected-slot state
- occupied state
- display stack for icon rendering
- count

Do not bind the full `InventorySessionSnapshot`. LDLib2 can sync complex
values, but the workspace should publish a minimal UI DTO instead of leaking the
domain snapshot into the rendering layer.

For list-like values, use explicit sync types and initial values. LDLib2 treats
collection types as read-only sync values, so workspace lists should use a
stable DTO array or an explicitly typed immutable list with an initial value.

## RPC Commands

Use LDLib2 UI RPC for workspace actions. The initial command shape is:

- source target
- destination target
- origin string

The hotbar transfer slice needs only one conceptual command:

- `TransferStackCommand(source, destination, origin)`

Client responsibilities:

- track ephemeral selection locally
- send source and destination target identity through RPC
- clear selection after dispatch or local cancellation
- render server-published status when it changes

Server responsibilities:

- reject missing, stale, unknown, or empty source targets
- resolve current source entry from the server authority snapshot
- derive source stack, count, and `ItemIdentity` from that entry
- derive host id and server menu ref from the server-side host
- construct a SLOT `InventoryActionRequest`
- execute through SLOT's existing executor/router path
- record successful outcomes in workflow/activity runtime
- publish updated status and view model

For the list-first player workspace, clicking a main-inventory row and then a
hotbar slot is not generic insert semantics. It is a quick-access assignment:
the selected backing stack should become the exact hotbar slot occupant, and any
previous hotbar occupant is displaced through the assignment policy. This is
modeled as `InventoryActionKind.ASSIGN` with
`InventoryActionConflictPolicy.ASSIGN_WITH_DISPLACE`, gated by
`InventoryCapability.QUICK_ACCESS_ASSIGN`, rather than overloading generic
`TRANSFER` or cursor `SWAP`. See
[../architecture/action-taxonomy.md](../architecture/action-taxonomy.md).

The client must not provide requested count, source stack, item identity, host
id, or server menu ref for workspace mutations.

## Mutation Flow

The canonical LDLib workspace flow is:

1. Server opens the SLOT workspace through LDLib2 `PlayerUIMenuType`.
2. The menu creates a server-owned workspace UI session.
3. The session publishes an initial `SlotWorkspaceViewModel` through S2C
   binding.
4. The client renders the list-first LDLib UI from the bound view model.
5. The user selects a main row or hotbar slot.
6. The client sends a narrow LDLib RPC command.
7. The server resolves live authority and builds a concrete SLOT action
   request.
8. SLOT executes the mutation through existing domain code.
9. The session updates status, workflow/activity, and the bound view model.
10. The client receives the new view model and re-renders.

The previous workspace-specific `InventoryActionRequestPayload` and
`InventoryActionOutcomePayload` path has been removed for this player-inventory
workspace. Equivalent command status and rejection diagnostics now flow through
the LDLib-bound workspace view model.

## Safety Rules

Use server-to-client bindings for inventory display data.

Use RPC for user interactions that mutate server-owned state.

Do not bind real player inventory contents bidirectionally as mutable
`ItemStack` values for workspace actions.

Do not use LDLib `ItemSlot`, `InventorySlots`, or vanilla quick-move behavior as
the semantic owner for the list-first workspace. Those widgets may be useful in
other contexts, but this workspace must preserve SLOT's target model and action
pipeline.

Do not trust client-sent counts, stacks, item identities, host ids, or menu refs.
The server must derive them from the live player/menu state.

Unsupported or ambiguous host state must fail closed with a status diagnostic.

## Implementation State

Implemented:

- LDLib2 is a required NeoForge dependency.
- The player inventory replacement opens a LDLib2 `PlayerUIMenuType` workspace.
- `SlotWorkspaceUiSession` owns the LDLib menu-scoped server UI state.
- `SlotWorkspaceViewModel` projects server authority into list rows and hotbar
  rail slots.
- LDLib S2C binding publishes the view model.
- LDLib RPC dispatches hotbar transfer commands.
- Server-side request construction derives host, menu ref, count, identity, and
  stack from live authority.
- Hotbar exact-slot replacement now uses the general action taxonomy:
  `ASSIGN + STACK + SINGLE_TARGET + ASSIGN_WITH_DISPLACE`.
- The old workspace-specific custom action/outcome payload path is deleted.

Deferred:

- search and filtering
- broader workflow rails and visible recents/protection controls
- persistent server-side workflow state for the LDLib workspace
- dual-pane/container host workspaces beyond the carried player inventory slice
- richer pending-action summaries beyond immediate command status

## Tests

Server command tests:

- main row to exact hotbar target
- occupied hotbar to player main inventory
- empty source rejection
- missing or stale menu rejection
- full destination or incompatible destination rejection
- correct count, stack, identity, host id, and server menu ref derivation from
  server authority

View-model tests:

- main inventory grouping by item identity
- sorting by display name, item id, then first backing slot
- total count aggregation
- first backing slot marker
- hotbar occupied and empty slot projection
- selected quick-access slot marker
- status and diagnostics projection

Architecture tests:

- LDLib2 imports are isolated to NeoForge UI/menu integration packages
- common inventory domain packages do not depend on LDLib2
- workspace RPC handlers build SLOT domain requests instead of bypassing the
  executor

Verification commands:

```bash
./gradlew :common:test --tests 'dev.imagio.slot.*'
./gradlew :neoforge:test
```

Manual acceptance:

- client startup succeeds
- inventory opens through the LDLib workspace menu
- list/hotbar view renders from server-bound state
- main row to hotbar transfer works
- hotbar to main inventory transfer works
- rejected actions update status without desync
- Escape and inventory key close behavior remains correct

## Consequences

Benefits:

- less custom UI networking and status plumbing
- stronger server authority for workspace mutations
- clearer boundary between UI transport and inventory semantics
- less manual refresh/rebuild code in screens
- a better foundation for later search, filters, and richer UI experiments

Costs:

- the workspace UI path becomes intentionally LDLib2-dependent
- a real server-owned UI session is required before deeper migration
- complex list DTOs need explicit sync typing and stable initial values
- LDLib2 lifecycle behavior becomes part of the workspace integration contract

This tradeoff is acceptable for the current dedicated experimental test
instance.
