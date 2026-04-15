# SLOT UI Host Architecture

Last updated: 2026-04-14

This document defines the long-term host and UI architecture above SLOT's
current headless inventory kernel. It is intentionally rewrite-oriented: the
goal is to establish the right boundary for the next implementation work, not
to preserve older screen code or to stage temporary bridges.

For product goals, see [PRODUCT_DIRECTION.md](PRODUCT_DIRECTION.md).
For current core semantics, see [ARCHITECTURE.md](ARCHITECTURE.md).
For the near-term execution sequence, see
[IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md).

## Goals And Non-Goals

Goals:

- keep inventory authority, browse semantics, routing, crafting, and activity
  outside screen classes
- add one UI-neutral workspace composition layer in `common` so different host
  layouts can render the same session snapshot without forking semantics
- support carried-only, chest-like dual-pane, and terminal/tool-heavy hosts
  through one host model
- make layout experimentation first-class through composable workspace profiles
  instead of one giant hard-coded screen
- make workspace composition deterministic and headless-testable so UI
  experiments can change presentation without reopening authority or routing
- separate persistent/core-owned state from ephemeral/screen-owned state so the
  UI can be rebuilt at any time
- keep unsupported or partially understood hosts fail-closed instead of
  degrading into speculative behavior

Non-goals:

- preserving any architecture from `archive` for compatibility
- introducing a temporary wrapper around old screen abstractions
- defining final visual styling, pixel geometry, or animation polish
- letting screens construct `InventoryActionRequest` values directly
- splitting the design into short-term versus long-term UI stacks
- accepting host-specific bypasses around `InventorySessionCoordinator` or
  `InventoryIntentRouter`

## Layering And Ownership

The UI stack is four layers with hard ownership boundaries.

### 1. Inventory Kernel In `common`

This layer already exists and remains the semantic source of truth.

It owns:

- `InventoryHostContext` and `InventoryHostDescriptor`
- exact authority snapshots and projection
- browse documents and browse posture
- command availability
- typed mutation intents, crafting intents, and routed planning
- pending action tracking, outcomes, workflow, and activity

It must not own:

- screen widgets
- pixel geometry
- NeoForge event wiring
- hover, focus, scroll, or animation state

### 2. Workspace Composition In `common`

This is the next new layer. It translates session output into a UI-neutral
workspace model.

New common concepts:

- `InventoryWorkspaceProfileId`
  - stable profile ids: `carried`, `dual_pane`, `terminal_hybrid`
- `InventoryWorkspaceProfile`
  - the semantic composition policy for a host family
- `InventoryWorkspaceModel`
  - the immutable presentation contract consumed by screens
- `InventoryWorkspaceZone`
  - a stable zone such as primary browse, secondary browse, quick access,
    equipment, workflow rail, tool dock, or status rail
- `InventoryWorkspaceSurface`
  - a typed surface inside a zone, such as a browse pane surface, quick-access
    surface, equipment surface, tool surface, crafting surface, or workflow
    surface
- `InventoryWorkspaceSubjectRef`
  - the workspace-wide interaction identity used for focus, hover, keyboard
    navigation, and stable non-browse surface targeting
- `InventoryWorkspaceStatus`
  - host/session/pending-action/diagnostic state exposed to the shell

This layer consumes:

- `InventorySessionSnapshot`
- `InventoryBrowseDocument`
- `InventoryHostDescriptor`
- current host family and profile selection rules

This layer produces:

- ordered zones and ordered surfaces
- stable surface ids and subject refs
- command availability and action affordances
- tool dock contents, crafting surface presence, and diagnostics
- profile-specific composition without pixel measurements

Hard invariant:

- workspace composition is a pure deterministic transform from session snapshot,
  host descriptor, and profile rules into `InventoryWorkspaceModel`
- the common composer must not depend on screen size, mouse position, renderer
  state, EMI gutters, or any other NeoForge-only runtime detail

It must not own:

- actual rendering
- Minecraft widgets
- `AbstractContainerScreen` subclasses
- screen-local planners or provider reflection
- overlay-mod layout state

### 3. Host Observation And Binding In NeoForge

NeoForge owns the live screen and menu observation path.

New NeoForge concept:

- `ObservedScreenContext`

`ObservedScreenContext` is the native observation layer above the existing
`InventoryHostContext`. It captures the actual open screen and the facts needed
to resolve a host safely:

- `AbstractContainerScreen<?>` identity
- `AbstractContainerMenu` identity
- screen class name
- actual rendered title
- player inventory reference
- slot ownership posture
- carried-only posture
- host-shape hints needed before compat matching
- whether the host should record Recent activity

`ObservedScreenContext` may carry native screen references and NeoForge
lifecycle details. `InventoryHostContext` remains the narrower common-facing
record passed into host resolution.

Translation rules:

- `ObservedScreenContext` is NeoForge-native and must not leak upward into the
  common resolver/session stack
- `InventoryHostContext` must become a structured common DTO for host
  resolution, even if that means rewriting the current thin record
- resolver-facing host-shape data must live in a structured hints object such
  as `InventoryHostObservationHints`, not in an expanding set of ad-hoc
  booleans or string heuristics
- those hints must at minimum carry:
  - host family hint
  - slot ownership posture
  - carried-only posture
  - records-recent posture
  - any other compat-facing shape signals needed before provider matching
- title and screen class remain first-class observed facts rather than being
  hidden inside generic hints

This layer is also responsible for deciding whether SLOT should mount at all.
If observation is incomplete or unsafe, it leaves the vanilla screen in place.

### 4. Screen Shells In NeoForge

Screen shells are the only layer that renders pixels and handles direct user
input.

They own:

- layout metrics and responsive geometry
- hit testing
- mouse capture and drag progress
- keyboard navigation
- focus and hover state
- scroll offsets
- inline text entry widgets
- tooltips and animation state

They consume only:

- `InventoryWorkspaceModel`
- renderer-local ephemeral state
- current screen dimensions and external layout constraints such as EMI gutters

They emit only:

- `InventoryBrowseIntent`
- `InventoryCommandInvocation`
- typed mutation intents including crafting and tool intents

They must not own:

- projection building
- command semantics
- transfer planning
- crafting semantics
- outcome classification
- refresh timing policy

Mount model:

- supported hosts mount as full replacement screen shells bound to the same live
  menu authority
- SLOT does not use overlay-on-vanilla or augmentation-of-vanilla as its
  primary architecture
- a host is either replaced completely through the SLOT shell or left untouched
  on vanilla or compat UI
- external mods such as EMI may affect shell layout through exclusion zones or
  gutters, but that is shell-level rendering coordination, not host semantics

## Canonical Data Flow

The UI pipeline is one canonical loop:

1. NeoForge observes the live `AbstractContainerScreen<?>` and
   `AbstractContainerMenu`.
2. NeoForge builds `ObservedScreenContext`.
3. NeoForge converts that into `InventoryHostContext`.
4. `InventoryHostResolver` resolves an `InventoryHostDescriptor`.
5. `InventorySessionCoordinator` reads authority, refreshes workflow/browse
   state, and exposes `InventorySessionSnapshot`.
6. A common workspace composer selects an `InventoryWorkspaceProfile` and
   produces `InventoryWorkspaceModel`.
7. A NeoForge screen shell renders that model and maintains only ephemeral UI
   state.
8. User interaction is translated into one of:
   - `InventoryBrowseIntent`
   - `InventoryCommandInvocation`
   - typed mutation intents
9. `InventoryIntentRouter` routes the intent through the existing kernel.
10. Routed actions become concrete `InventoryActionRequest` payloads to the
    server when authority mutation is required.
11. Outcomes and invalidations flow back into the coordinator.
12. The coordinator rebuilds `InventorySessionSnapshot`, which rebuilds
    `InventoryWorkspaceModel`, which the shell re-renders.

Important rules:

- screens never talk to providers directly
- screens never construct authoritative outcomes
- screens never derive separate action semantics from layout choice
- the common workspace composer is headless and deterministic for a given
  snapshot and profile
- the same session snapshot can be rendered through different workspace
  profiles without changing router behavior

## Workspace Model And Profile Model

The workspace layer exists to prevent screen classes from becoming semantic
owners again.

### `InventoryWorkspaceProfile`

`InventoryWorkspaceProfile` is a composition policy, not a screen class. It
defines:

- which zones exist for a host family
- which surfaces belong in each zone
- which pane is primary versus secondary
- whether tools are compact, docked, or always visible
- which workflow surfaces are exposed by default
- the default focus target when a host first mounts

Profiles are fixed semantic families:

- `carried`
  - carried browse is primary
  - quick access and equipment are adjacent support zones
  - workflow surfaces are available without creating a fake external pane
  - tool surfaces are optional and compact
- `dual_pane`
  - carried and external browse surfaces are both first-class
  - selection model is shared across panes
  - workflow surfaces remain shared rather than duplicated per pane
- `terminal_hybrid`
  - the external browse surface is primary
  - carried browse is secondary but still part of the same interaction kernel
  - tool dock and crafting surfaces are first-class, not bolt-ons
  - provider toggles and terminal tool actions live in the tool dock

Profiles may rearrange emphasis and ordering, but they may not:

- change command meaning
- hide required live surfaces silently
- bypass protection or canonicalization
- reinterpret subject identity
- create host-specific mutation rules

### `InventoryWorkspaceSubjectRef`

`InventoryWorkspaceSubjectRef` is the stable interaction identity for the whole
workspace. It exists because `InventoryBrowseSubjectRef` is browse-specific and
does not cover quick access, equipment, tool, or crafting surfaces.

It should include distinct variants for:

- browse subjects by embedding `InventoryBrowseSubjectRef`
- quick-access targets
- equipment slot targets
- tool surfaces
- tool-region slots
- crafting inputs and crafting output
- workflow or status surfaces when those surfaces accept focus but are not
  browse entries

Rules:

- `InventoryBrowseSubjectRef` remains the browse identity used for persisted
  browse selection and browse command routing
- `InventoryWorkspaceSubjectRef` owns shell-level focus, hover, and keyboard
  navigation across both browse and non-browse surfaces
- non-browse workspace refs remain ephemeral unless a future design explicitly
  promotes one into persisted workflow or session state
- workspace refs must derive from stable host, tool, lane, group, and source
  ids that already exist in common descriptors rather than from widget ids or
  pixel positions

### `InventoryWorkspaceModel`

`InventoryWorkspaceModel` is the only presentation contract consumed by screen
shells. It should expose:

- the active session token and host id
- the selected workspace profile id
- ordered zones and ordered surfaces
- stable ids for zones and surfaces
- stable `InventoryWorkspaceSubjectRef` values wherever focus or navigation can
  land
- underlying `InventoryBrowseSubjectRef` values for browse panes, sections, and
  entries so persisted selection and `InventoryCommandInvocation` keep using the
  current browse identity model
- browse pane, section, and entry ordering exactly as produced by core browse
  output
- quick access, equipment, workflow, and tool surfaces
- command availability and action affordances
- pending-action and diagnostic status
- tool toggle state and crafting-surface presence

It must not expose:

- widget classes
- pixel positions
- raw NeoForge events
- local drag planners
- direct provider APIs

The workspace model must preserve the current subject-ref identity model
unchanged. Layout experiments are allowed to move a row between zones or panes
visually, but they are not allowed to fork or rename the underlying subject
identity that the router uses.

## Persistent Vs Ephemeral State

The design is only clean if state ownership stays explicit.

### Persistent And Core-Owned

Persistent or session-owned state stays in the existing kernel and any future
workspace model inputs:

- session token
- `InventoryBrowseSessionState`
- selected browse subject
- active pane
- filter, sort, and grouping posture
- pinned tool id
- command availability
- tool toggle state
- crafting surface state exposed by the host descriptor and session snapshot
- pending actions
- workflow-backed collections, loadouts, favorites, junk, and Recent
- host diagnostics and action diagnostics

### Ephemeral And Screen-Owned

Ephemeral state belongs only to the NeoForge shell:

- scroll offsets keyed by stable zone or surface id
- splitter ratios
- hovered subject or hovered surface
- focused widget or focused subject
- text caret position
- drag gesture progress
- transient selection rectangles
- tooltip timing
- animation state

Rules:

- ephemeral state must be rebuildable from the current workspace model plus
  shell-local state
- ephemeral state must never be written into workflow persistence
- session token changes drop all ephemeral state unless a later explicit policy
  says otherwise
- profile changes drop any ephemera whose zone or surface id no longer exists
- browse refresh keeps focus or hover only if the same stable subject or
  surface still exists after rebuild
- non-browse focus does not write back into `InventoryBrowseSessionState`

## Host-Family Composition Rules

Host families are semantic composition choices, not separate architecture
stacks.

### Carried-Only Hosts

Carried-only hosts must compose:

- one primary carried browse surface
- quick access and equipment support surfaces
- workflow surfaces for collections, loadouts, favorites, junk, and Recent
- optional compact tool surfaces when the host exposes tools

They must not:

- fabricate an external pane
- special-case routing outside the shared kernel

### Chest-Like Dual-Pane Hosts

Chest-like hosts must compose:

- one carried browse surface
- one external browse surface
- shared workflow surfaces
- the same command and typed-intent path as carried-only hosts

They must not:

- duplicate workflow state per pane
- create separate selection semantics for carried and external rows

### Terminal And Tool-Heavy Hosts

Terminal and tool-heavy hosts must compose:

- one primary external browse surface
- one secondary carried browse surface
- a first-class tool dock
- crafting surfaces and provider toggles when the host exposes them
- the same workflow surfaces and selection model as other hosts

They must not:

- hide required tool regions because the profile is inconvenient
- move crafting semantics into the screen shell
- reinterpret provider-backed rows as slot-backed rows

### Profile Selection Rules

Profile selection is deterministic:

- use `carried` when the host is truly carried-only and does not require a
  persistent external surface
- use `dual_pane` when the host exposes a normal external pane without a
  terminal-style tool-first posture
- use `terminal_hybrid` when live tool surfaces, crafting surfaces, or
  terminal-style host behavior are first-class to the host

If a host requires a surface that the chosen profile cannot represent, that is
not a reason to silently degrade. The host is not ready to mount through SLOT
until the profile can represent it completely.

## Input-To-Intent Mapping Rules

All user interaction must terminate in existing core intent types.

Mapping rules:

- browse posture changes become `InventoryBrowseIntent`
- browse row, pane, and section commands become `InventoryCommandInvocation`
- tool actions become typed tool mutation intents
- tool toggles become typed tool-toggle mutation intents
- crafting placements, drags, and result extraction become typed crafting
  intents
- quick access and equipment interactions become typed mutation intents against
  their existing common targets rather than browse commands

Shell responsibilities:

- determine which zone, surface, subject, or tool the user interacted with
- convert mouse and keyboard gestures into the same semantic intent
- supply ordered drag targets for crafting drags when needed
- only emit `InventoryCommandInvocation` for browse-backed workspace refs
- preserve no semantic differences between click-driven and hotkey-driven
  interaction

Shells must not:

- construct `InventoryActionRequest` directly
- choose source versus slot versus provider-entry targeting themselves
- invent alternate shortcut-only semantics
- bypass the router for “easy” actions

This means a left-click row action, a keyboard shortcut, and a context-menu
choice can feel different in the UI but still terminate in the same routed
intent path.

## Refresh, Rebind, And Invalidation Behavior

The host/UI stack must assume that the workspace can rebuild at any time.

### Reobserve

NeoForge reobserves when:

- the active screen changes
- the active menu identity changes
- the active screen class changes
- the host closes
- the coordinator reports a refreshed session snapshot

### Rebind

`ObservedScreenContext` rebinding rules:

- if the observed host identity changes, build a new `InventoryHostContext`
  and let the coordinator establish a new session
- if the observed host is the same logical host, refresh the existing session
  instead of forking screen-local state
- if observation becomes incomplete or contradictory, unmount SLOT and fall
  back to vanilla

### Refresh

Workspace rebuild rules:

- rebuild the workspace model whenever the session snapshot changes
- preserve only the ephemeral state that still points to existing zone ids,
  surface ids, or subject refs
- keep selected subject only when the core browse/session state still resolves
  to a visible subject after rebuild
- do not let the shell “remember” rows that the refreshed browse document no
  longer exposes

### Invalidation

Invalidations and outcomes stay kernel-owned:

- the shell may request refresh, but it does not classify inventory changes
- the coordinator remains the only place that ingests outcomes, pending-action
  completion, and conservative invalidation results
- the workspace model is derived from the refreshed snapshot rather than
  patched in place by the shell
- shell-local layout constraints may trigger rerender, but they do not trigger
  alternate workspace composition semantics in common

## Unsupported-Host Fallback Rules

SLOT must fail closed.

Fallback is required when:

- screen observation is incomplete
- host resolution is unsupported
- compat matching is ambiguous
- a required host surface cannot be represented by the selected workspace
  profile
- config disables replacement for that host family
- the screen shell cannot mount safely over the active host

Fallback behavior:

- leave the vanilla screen in place
- do not mount a partial SLOT shell
- do not fabricate missing tool or crafting surfaces
- do not hide routing gaps behind disabled buttons if the underlying host model
  is incomplete
- log the reason when debug logging is enabled

The renderer may omit optional decoration. It may not omit required host
surfaces or semantics and still claim the host is supported.

## Acceptance Scenarios

The first host/UI implementation is only acceptable if these statements are
true:

- one `InventorySessionSnapshot` can render through different workspace
  profiles without changing authority, selection semantics, or mutation routing
- the common workspace composer produces the same
  `InventoryWorkspaceModel` for the same session snapshot and profile in a
  headless test, regardless of screen dimensions or overlay-mod state
- host transitions rebuild from a new `ObservedScreenContext` and keep only the
  persistent state that is still compatible with the new host
- browse refresh preserves focus and selection only when the same stable
  subject or surface still exists after rebuild
- browse selection stays browse-scoped; non-browse focus surfaces do not leak
  into persisted workflow or session state
- carried-only, chest-like, and terminal/tool-heavy hosts all terminate in the
  same intent router and session coordinator
- crafting and tool-heavy hosts use the same interaction kernel as simpler
  hosts and do not move crafting logic into screens
- unsupported or partially understood hosts fall back cleanly to vanilla with
  a concrete reason
- supported hosts mount through full replacement SLOT shells rather than
  overlaying behavior onto vanilla container screens
- mouse-first and keyboard-first interactions terminate in the same intent
  types and the same router path
- layout experimentation cannot bypass protection, canonicalization, or action
  routing because those rules never live in screens
- the next NeoForge implementation can delete the archived prototype screen
  architecture rather than wrapping it
