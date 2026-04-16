# UI Library Assessment

Last updated: 2026-04-15

This assessment reviews whether an external UI framework should help power
SLOT's inventory UI experiments on Minecraft `1.21.1` NeoForge.

The key boundary has changed after the core rewrite:

- the blocker is no longer missing core inventory abstractions
- the blocker is now choosing whether a UI host can cleanly consume SLOT's
  existing architecture without re-owning inventory semantics

## Executive Conclusion

An external UI library is now a presentation-host decision, not a core
architecture rescue plan.

That means:

- SLOT should keep owning authority, projections, action intents, planner
  semantics, crafting surfaces, and workflow/activity behavior
- any future UI host should consume the contracts documented in
  [../architecture/overview.md](../architecture/overview.md)
- a UI framework is useful only if it makes layout, rendering, and local UI
  state easier without becoming another inventory-authority surface

## What A UI Library Must Fit

Any candidate UI host must be able to consume SLOT-owned:

- `InventoryBrowseDocument`
- `InventoryBrowsePane`
- `InventoryBrowseEntry`
- `InventoryWorkingSetProjection`
- `ProjectedInventoryRow`
- `WorkflowProjection`
- `ActivityProjection`
- projected backing-entry semantics
- typed intents over row actions, cursor actions, tool actions, and workflow
  actions
- crafting surfaces and tool-region descriptors
- refresh/state preservation rules

A UI host must not take ownership of:

- source identity
- slot identity
- transfer routing
- protection policy
- crafting semantics
- Recent attribution or cleanup history
- network mutation flow

## LDLib2

Current verdict: accepted for the workspace UI transport path, but only as a UI
host. See
[../decisions/0002-ldlib2-workspace.md](../decisions/0002-ldlib2-workspace.md).

Why it is useful:

- layout and composition tools are better than the current manual screen code
- reusable widget/event systems could reduce screen size substantially
- a prototype could help us test list-first or tool-heavy UI concepts faster

Why the boundary still matters:

- SLOT often wraps foreign menus it did not create
- SLOT cannot allow the UI library to reinterpret slot authority or quick-move
  policy
- framework-specific RPC/sync layers must not become parallel mutation paths

Recommended use in the workspace UI path:

- consume SLOT projections
- emit SLOT intents immediately
- keep mutation routing inside SLOT-owned planners and operations
- treat any framework slot widgets as presentation helpers only after verifying
  they preserve SLOT's logical-slot rules
- use server-to-client bindings for display state
- use LDLib2 UI RPC for interactions that mutate server-owned inventory state
- build authoritative `InventoryActionRequest` values on the server, not from
  client-provided stack/count/identity data

## ModernUI-MC

Current verdict: still not a good fit for SLOT's main inventory-overhaul layer.

Reason:

- it is a broad UI/runtime framework, not a clean answer to source-aware
  inventory browsing and action routing
- it adds a large dependency surface without solving SLOT's specific container
  and planner constraints

It may remain interesting as a general UI reference, but not as the foundation
for SLOT's main inventory workflow UI.

## Adoption Criteria

SLOT has adopted LDLib2 for the dedicated NeoForge workspace transport path.
Any additional LDLib work or future UI-host experiment should still be judged
against the current architecture boundary, not against aesthetics alone.

Required criteria:

- the UI host consumes immutable SLOT projections
- the UI host consumes workflow and activity read models instead of rebuilding
  recents or cleanup state locally
- all meaningful user actions become SLOT intents
- logical slot identity survives hit testing to action request creation
- merged carried rows still resolve through exact backing entries
- crafting surfaces still use SLOT-owned linked slot targets
- refresh preserves valid interaction state
- unsupported screens still fail closed

## Recommendation

LDLib2 is accepted for the player-inventory workspace and should remain the
default UI transport for the dedicated test instance.

The near-term sequence is now:

1. validate and harden the LDLib player-inventory workspace
2. expand list-first behavior over SLOT-owned view models and RPC commands
3. add dual-pane/container host workspaces through the same boundary
4. keep LDLib widgets as UI helpers, not inventory authority

The main question is no longer "which library gives us missing core behavior?"
The main question is "how do we use LDLib2's UI, binding, and RPC abstractions
without letting them replace SLOT's source-aware inventory domain?"
