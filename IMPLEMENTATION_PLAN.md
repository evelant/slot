# SLOT Current Implementation Plan

Last updated: 2026-04-14

This is the near-term engineering plan from the current post-rewrite baseline.
It is not the product-direction document and not the normative behavior spec.

For product goals, see [PRODUCT_DIRECTION.md](PRODUCT_DIRECTION.md).
For current architecture, see [ARCHITECTURE.md](ARCHITECTURE.md).

## Current Baseline

The main headless core is now in place. The current phase should build the
first real hosts on top of it rather than reopening the inventory kernel.

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
- selected-subject browse posture persisted in `InventoryBrowseSessionState`
- central client-owned session and routing layer through:
  - `InventorySessionCoordinator`
  - `InventoryIntentRouter`
  - `InventoryCommandInvocation`
  - `InventoryCommandPreflightService`
- correlation, causation, and session metadata threaded through workflow
  events, action requests, outcomes, and activity recording
- typed crafting/tool intents and one routed crafting pipeline for:
  - selected-row placement
  - cursor placement
  - cursor drag distribution
  - authoritative result extraction
  - tool actions and tool toggles
- crafting refresh centralized in the action execution layer
- aligned crafting-surface support for:
  - vanilla `InventoryMenu` and `CraftingMenu`
  - Tom's Storage crafting terminals
  - Sophisticated Backpack crafting upgrades
- old mutable workflow/acquisition store patterns removed instead of preserved
  as facades
- orphaned pre-router action-session storage removed instead of carried forward

This means the next work should not reopen the authority/projection split, the
session/router kernel, or the crafting operation model. The next work should
prove that kernel in real hosts.

## Current Status Review

At a high level, the codebase is now ready for the first real host/UI slice.

What is solid:

- exact authority is explicit and no longer conflated with visible rows
- merged carried browsing retains exact backing refs for deterministic actions
- provider-entry inventories are modeled honestly instead of as fake slots
- browse documents are a core output instead of screen-owned assembly
- durable workflow semantics and bounded inventory activity are event-backed
- one refresh-owned routed path exists from command or typed intent to
  authoritative outcome and rebuilt browse state
- correlation/session metadata is now real operational data instead of empty
  envelope fields
- crafting surfaces follow one contract across vanilla, Tom's, and
  Sophisticated integrations
- the server boundary stays narrow: server handlers execute concrete
  `InventoryActionRequest`s only

What is only partially landed:

- the client runtime exposes the session coordinator and intent router, but no
  production screen host consumes them yet
- the current NeoForge client bootstrap still resolves hosts with placeholder
  screen metadata, so compat that relies on accurate screen context is not yet
  proven through the live client path
- external invalidation and conservative diff fallback exist, but explicit
  world/mod signal producers for pickups, trades, quest rewards, and similar
  activity are still sparse
- workflow surfaces exist in browse output, but no production UI host renders
  collections, loadouts, recents, and cleanup over the new core yet
- trash, overflow, recovery, and undo semantics remain intentionally deferred
- recipe-assisted workflows such as EMI/JEI transfer are not started

## Known Gaps And Risks

These are the highest-signal issues still visible from the current code and
plan.

### 1. The Docs Lag The Code

The previous implementation plan still described the router/coordinator and the
crafting pipeline as missing even though those pieces are now landed.

Practical consequence:

- the written execution order no longer matches the actual codebase
- planning drift is now a bigger risk than kernel under-design

### 2. There Is No Real Host Over The New Core Yet

The main session/runtime exists, but there is still no shipped carried-only or
dual-pane screen consuming browse documents, command invocation, and typed
intents end to end.

Practical consequence:

- the largest remaining risk is now at the screen/session boundary
- core behavior is well-tested headlessly, but the actual user path is not yet
  proven

### 3. Client Host Context Is Still Under-Specified

The current NeoForge bootstrap resolves hosts from the active menu, but it does
not yet feed real screen class, title, ownership, or related host hints into
`InventoryHostContext`.

Practical consequence:

- compat providers that match on screen context are not yet exercised through
  the actual client runtime
- the first host slice needs proper screen observation instead of more planner
  work

### 4. External Activity Is Still Conservative-First

The coordinator can invalidate and classify carried acquisitions from authority
diffs, but explicit signal bridges are still limited.

Practical consequence:

- direct routed outcomes are correct
- some world/mod-driven changes still rely on invalidation plus conservative
  inference instead of explicit producers

### 5. Trash / Overflow / Recovery Are Still Product Work, Not Engineering Debt

The activity model has the right vocabulary, but the behavior rules are still
open:

- where recoverable trash lives
- when overflow staging happens
- what gets a recovery token
- what restore is allowed to do

Practical consequence:

- this should stay deferred until the first real host proves the current
  browse/workflow model

### 6. Recipe-Assisted Workflows Are Still Absent

Crafting is now routed cleanly, but recipe transfer/fill workflows are not yet
integrated.

Practical consequence:

- the next tool-adjacent work should be EMI/JEI-style integration, not another
  rewrite of core crafting semantics

## Ground Rules

1. One user action gets one authoritative pipeline.
2. UI code renders projections and emits intents; it does not choose inventory
   semantics.
3. Unsupported or ambiguous integrations fail closed.
4. Broad actions plan against backing entries, not aggregate counts alone.
5. Crafting stays slot-backed and descriptor-driven.
6. New hosts must consume the same authority/projection/browse/action
   contracts instead of inventing screen-local semantics.

## Next Execution Order

### 1. Build Real Screen Observation And Host Binding

Goal:

- turn live NeoForge screen/menu state into accurate `InventoryHostContext`
  instead of placeholder bootstrap metadata

Deliverables:

- screen observation that captures:
  - actual screen class
  - actual title
  - slot ownership and host shape hints
  - carried-only vs external-host posture
- session refresh ownership around screen/menu transitions
- host resolution that uses those real values before compat matching

Exit criteria:

- compat providers no longer depend on blank screen metadata through the live
  client path
- host changes rebuild the session cleanly without screen-local branching

### 2. Build A Thin Shared Screen Core

Goal:

- make carried-only and dual-pane hosts thin render/input shells over the same
  browse/session/intent pipeline

Deliverables:

- reusable row/list materialization over `InventoryBrowseDocument`
- one input mapping layer for:
  - `InventoryBrowseIntent`
  - `InventoryCommandInvocation`
  - typed crafting/tool intents
- shared handling for search, grouping, selection, pending-action state, and
  tool pinning
- screen modules that do layout and hit testing without owning semantics

Exit criteria:

- no screen class chooses mutation semantics locally
- selection, pending actions, and refresh behavior derive from shared session
  state
- carried-only and dual-pane hosts can share the same intent pipeline

### 3. Ship The First Carried Host Behind Config

Goal:

- prove the new core on the player inventory path before widening to container
  hosts

Deliverables:

- player-inventory replacement path behind `replacePlayerInventory`
- carried browser rendering over the browse document
- search/filter/grouping flows over shared browse state
- row commands, favorites, collections, loadouts, and recents over the router
- crafting tool panel integration for the vanilla 2x2 surface

Exit criteria:

- a carried-only host works end to end without old prototype wrappers
- the first user-visible SLOT host is using the new core rather than bypassing
  it

### 4. Add Dual-Pane And Container Hosts

Goal:

- apply the same shared core to chest-like storage and supported compat hosts

Deliverables:

- dual-pane layout over the same browse/session pipeline
- carried/external row actions and pane/section commands through the router
- compact crafting/tool panels where the active host exposes them
- explicit guarded fallback when a host is unsupported

Exit criteria:

- carried and external panes share one intent and refresh pipeline
- supported chest-like and terminal-style hosts run through the same host core

### 5. Add Recipe-Assisted Workflows And Better External Signals

Goal:

- extend the existing core for recipe viewers and richer activity signaling
  without reopening action semantics

Deliverables:

- recipe transfer/fill intents routed through existing crafting surfaces
- EMI/JEI-facing compat bridges that emit SLOT-owned intents and diagnostics
- explicit activity signal bridges for pickups, trades, quest rewards, and
  compat deltas where available

Exit criteria:

- recipe-assisted workflows do not invent new authority paths
- recents can use explicit producers where integrations provide them

### 6. Design Trash / Overflow / Recovery On Top Of The Existing Activity Model

Goal:

- finish cleanup/undo semantics after the first real hosts prove the browse and
  workflow model

Deliverables:

- explicit product rules for trash, overflow staging, restore, and void
- routed intents and activity producers aligned with those rules
- recovery surfaces that consume activity, not inferred current state

Exit criteria:

- cleanup and recovery semantics are specified before implementation expands
- trash/void remain explicit-intent features, never passive side effects

## Testing Priorities

Highest-value near-term coverage:

- screen/session boundary behavior
- screen observation and host binding
- intent emission from carried-only and dual-pane hosts
- row-driven bulk transfer behavior across merged carried rows
- crafting placement/drag/result flows through live hosts
- protection and workflow interactions with broad actions
- refresh/session preservation across projection rebuilds and host changes
- compat bridge receiver guards and fallback behavior
- Recent production and suppression matrix
- activity replay, dismissal watermark, and future cleanup/recovery projection
  behavior
- persistence migration from legacy snapshots into checkpoint-plus-log state

## Definition Of Done For This Phase

This phase is complete when:

- the docs reflect the landed router/session/crafting baseline
- a production NeoForge carried host consumes the new core directly
- at least one dual-pane/container host consumes the same shared host layer
- screen classes are materially thinner and no longer choose inventory
  semantics
- compat bridges stay narrow and descriptor-driven
- recipe-assisted workflows and cleanup/recovery remain clearly separated
  follow-on phases unless they are explicitly landed
