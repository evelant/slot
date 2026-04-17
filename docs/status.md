# SLOT Project Status

Last updated: 2026-04-17

This is the operational handoff document for planning and implementation. Read
this after [../README.md](../README.md), then follow the linked architecture
or decision notes only when a task needs deeper context.

## Current Baseline

SLOT is an unreleased experimental NeoForge-first Minecraft `1.21.1` inventory
overhaul. Backwards compatibility inside this repo is not a constraint; clean
refactors are preferred over compatibility facades.

Currently landed:

- the common inventory kernel for authority snapshots, projections, browse
  documents, workflow/activity state, command preflight, intent routing, action
  execution, crafting/tool routing, and diagnostics
- the verb-based inventory action taxonomy:
  `InventoryActionKind + InventoryActionQuantity + InventoryActionScope +
  InventoryActionConflictPolicy`
- the common workspace composition model under
  `common/src/main/java/dev/imagio/slot/inventory/workspace`
- NeoForge screen observation and player-inventory workspace opening
- mandatory LDLib2 workspace transport for the player-inventory test instance
- a server-owned LDLib workspace session and compact view model for a
  player-inventory workspace plus hotbar rail
- LDLib RPC for workspace hotbar transfer commands, with server-derived source
  stack, count, identity, host id, and menu ref
- logging for high-signal host resolution, workspace transfer, and inventory
  action rejection/application paths
- removal of the old workspace-specific custom action/outcome packet path
- a LDLib2 `GraphView` carried-atlas proof of concept with pan, zoom,
  item-card selection, progressive disclosure, translucent workspace chrome,
  search/navigation overlay, camera preservation, and hotbar transfer behavior
- storage prototype Slice 0 (test helper command) and Slice 1 (carried
  readability at scale) — see
  [plans/storage-prototype.md](plans/storage-prototype.md) for per-slice
  detail. Key landing points:
  - `/slot test populate-atlas {triage|homed} <count>` and `/slot test clear`
    (op-gated) for reproducible populated atlases
  - `AtlasItem.carried` / `AtlasIsland.carriedCount` on the view model;
    carried lanes = main + hotbar + offhand; ghost items emitted for homed
    identities not in any carried lane
  - `FitCarriedCamera` in `common/atlas/` with largest-cluster fallback;
    wired into the workspace `LAYOUT_CHANGED` initial-camera path
  - ghost rendering via card-chrome alpha dim + `overlayTexture` on the
    icon itself (shader-color tints were discarded — see AGENTS.md Traps)
  - per-island carried-count pill with click-to-pan
  - canvas-bound artefacts removed: islands render at their stored
    dimensions and can live at any coord (positive or negative); atlas
    cards are frozen at `item.width() × item.height()` world units — LOD
    adjusts detail, never widget footprint
- storage prototype Slice 2 partial (domain + server-side identity
  machinery landed; claim RPC, UI button, atlas rendering, and
  `populate-chests` helper still open — see
  [plans/storage-prototype.md](plans/storage-prototype.md)). Key landing
  points so far:
  - `ClaimedChest(storageId, anchors, atlasX, atlasY, label)` +
    `ClaimedChestMap` in `common/workflow/domain/`; events and projection
    reducer wired through `WorkflowProjection.Snapshot`;
    `ChestClaimWorkflowDomainService` exposes claim / move / updateAnchors
    / removeAnchor / relabel / delete with server-authoritative anchor
    collision checks
  - `common/atlas/StorageZoneAutoPlacement`: pure-function neighbor-based
    placement with world→atlas scaling, grid snap, collision bump, and
    dimension isolation
  - NeoForge-side identity: `AttachmentType<UUID>` keyed `slot:storage_id`
    stored on block-entity NBT (survives chunk save/load); vanilla double
    chests merge into one `ClaimedChest` with two anchors via
    `ChestBlock.TYPE` + `getConnectedDirection`; shulker exclusion via
    `BlockTags.SHULKER_BOXES`
  - `ChestClaimServerService` orchestrator binds `ServerPlayer + BlockPos`
    → capability check + anchor resolve + auto-placement + common service
    claim + BE attachment write
  - `BlockEvent.BreakEvent` listener removes anchors from the breaking
    player's runtime (single-player first); cascades to claim delete
    when last anchor is gone
  - in-memory-only for Slice 2: claimed-chest events are not yet encoded
    by `WorkflowDomainFileStore` (placeholder `return null` arms + null
    filter on the encode pipeline). Full codec + load-time reconciliation
    lands in Slice 7
- persistence refactor: `WorkflowDomainFileStore` moved from
  `neoforge/persistence` → `common/workflow/domain/persistence/` (zero
  platform imports; lives with the domain types it serializes). Test
  stays in neoforge because the test runtime needs Gson from the bundled
  NeoForge jar. `SlotPlayerWorkflowRuntimeService` continues to own the
  platform-specific file-path resolution and lifecycle triggers.

Current prototype validation point:

- the atlas proof works as a primary player-inventory surface: inventory opens
  into the map, pan/zoom works, and atlas card to hotbar movement works
- the first atlas styling pass has known issues: aliased/flickering background
  texture, overly large item-card padding/text, and text overflow at detail zoom
- the current prototype direction is triage-first visual memory with
  **no silent auto-homing**: a fresh atlas contains only the Triage island,
  and a small set of conservative per-card suggestion chips (driven by item
  class / tag / component signals, never id substring matching) lets the
  player materialize Food / Tools / Weapons / Armor / Materials / Storage
  islands on demand; everything beyond those six seeds is player-authored
  or driven by rules learned from the player's own manual placements

## Current Focus

Storage prototype is underway. **Slice 2 (Claim And Storage Zone Tile) is
partially landed** — the common-side domain (`ClaimedChest`,
`ClaimedChestMap`, events, projection, `ChestClaimWorkflowDomainService`,
`StorageZoneAutoPlacement`) and the NeoForge-side server identity
machinery (`storage_id` BE attachment, double-chest-aware anchor
resolver, shulker exclusion, `ChestClaimServerService`, break-event
anchor cleanup) are in. Still open in Slice 2:

- claim RPC + Claim button in the chest UI
- storage-zone atlas region + chest tile rendering + drag-to-reposition
- `/slot test populate-chests` helper command

See [plans/storage-prototype.md](plans/storage-prototype.md) for the
remaining exit criteria and the full slice sequence.

The underlying triage/home loop (from [plans/current.md](plans/current.md))
is landed enough to support storage work: template + learned chip
suggestions, chip-accept + manual-assign, island management, persisted
homes, and LDLib workspace transport all in place. Slice 3b (reversible
assignment records) is partial; search spotlight (slice 5) has not
started; neither blocks the storage prototype. Kit prototype
([plans/kit-prototype.md](plans/kit-prototype.md)) follows after
storage proves out; `+N since last open` newness indicators are
tracked under "Later Feature Tracks" in `plans/current.md`.

## Small known bugs to fix

- Show item titles slightly sooner when zooming in (adjust lod switch point)
- Show more than just title at lower LOD for differentiating/identifying things like enchanted books
- Island rename loses focus on each keystroke, islands flicker
- Min size of islands is too large and islands have a bit too much padding inside them


## Project Structure

Top-level docs (see [../README.md](../README.md) for the full doc map):

- product: [product/direction.md](product/direction.md), [product/spec.md](product/spec.md)
- architecture: [architecture/overview.md](architecture/overview.md),
  [architecture/action-taxonomy.md](architecture/action-taxonomy.md),
  [architecture/host-ui.md](architecture/host-ui.md)
- design: [design/atlas.md](design/atlas.md), [design/kits.md](design/kits.md)
- plans: [plans/current.md](plans/current.md),
  [plans/atlas-prototype.md](plans/atlas-prototype.md),
  [plans/kit-prototype.md](plans/kit-prototype.md)
- decisions: [decisions/0001-core-rewrite.md](decisions/0001-core-rewrite.md),
  [decisions/0002-ldlib2-workspace.md](decisions/0002-ldlib2-workspace.md)
- research: [research/ui-ux-brainstorm.md](research/ui-ux-brainstorm.md),
  [research/ui-library-assessment.md](research/ui-library-assessment.md),
  [research/core-inventory-library-assessment.md](research/core-inventory-library-assessment.md),
  [research/integration-learnings.md](research/integration-learnings.md)

Common module:

- `inventory/core`: descriptors, capabilities, host topology, policy, builtin
  ids, crafting surface descriptors
- `inventory/query`: authority snapshots and read services
- `inventory/browse`: UI-independent browse documents, panes, rows, command
  availability
- `inventory/action`: targets, action requests/outcomes, taxonomy dimensions,
  planners, canonicalization
- `inventory/session`: coordinator, intent router, command preflight, crafting
  planner, pending action state
- `inventory/integration`: host resolution, providers, mutation router,
  builtin executor, compat provider contracts
- `inventory/workspace`: UI-neutral workspace composition model
- `workflow/domain`: collections, loadouts, protection, recents, activity,
  visual homes, claimed chests, persistence-facing workflow domain
- `workflow/domain/persistence`: JSON file-store codec for the workflow
  domain snapshot (platform-neutral; no Minecraft/NeoForge imports)
- `atlas`: pure-function atlas helpers (`FitCarriedCamera`,
  `StorageZoneAutoPlacement`)
- `compat`: shared compat helpers and provider-side integration support

NeoForge module:

- `neoforge/client/host`: live screen/menu observation
- `neoforge/client/screen`: player inventory replacement trigger/mount glue
- `neoforge/screen/ldlib`: LDLib2 workspace menu, holder, UI session,
  view-model projection, UI factory, and transfer request factory
- `neoforge/network`: narrow open-workspace payloads; not the old action
  request/outcome transport
- `neoforge/compat`: loader-side integration registration
- `neoforge/storage`: BE `storage_id` attachment, claimability/anchor
  resolver, claim orchestrator, break-event anchor cleanup
- `neoforge/workflow`: platform lifecycle wiring for the common
  workflow runtime (per-player file-path resolution, login/logout /
  server-stop save triggers)
- `neoforge/config`: dedicated-test-instance config defaults

Reference code:

- `reference/LDLib2`: local LDLib2 source clone
- `reference/InventoryEssentials`: useful vanilla click-sequence references
- `reference/TrashSlot`: useful trash/delete behavior reference
- `reference/Applied-Energistics-2`: large terminal/sync architecture reference
- `reference/SophisticatedBackpacks` and `reference/SophisticatedCore`:
  carried-container and crafting-upgrade integration references
- `reference/Toms-Storage`: terminal/provider-entry reference
- `reference/emi`: recipe-viewer integration reference

## Concept → Code Map

Use this to find the right package quickly. Paths are under
`common/src/main/java/dev/imagio/slot/` unless noted.

| Concept | Package / area |
| --- | --- |
| Authority snapshots, read services | `inventory/query` |
| Source/entry identity, slot targets | `inventory/core` |
| Action taxonomy (`Kind+Quantity+Scope+Policy`) | `inventory/action` |
| Action planners, canonicalization | `inventory/action` |
| Browse documents, panes, rows | `inventory/browse` |
| Session coordinator, intent router, preflight | `inventory/session` |
| Host resolution, mutation router, executors | `inventory/integration` |
| Workspace composition (UI-neutral) | `inventory/workspace` |
| Collections, loadouts, recents, protection | `workflow/domain` |
| Visual homes, claimed chests, domain events | `workflow/domain` |
| Workflow snapshot JSON codec | `workflow/domain/persistence` |
| Atlas camera / storage-zone placement (pure) | `atlas` |
| Host compat shared helpers | `compat` |
| Screen/menu observation | `neoforge/client/host` |
| Player inventory replacement trigger | `neoforge/client/screen` |
| LDLib2 workspace menu, session, view-model, RPC | `neoforge/screen/ldlib` |
| Atlas `GraphView`, item cards, camera preservation | `neoforge/screen/ldlib` (UI factory) |
| Open-workspace network payloads | `neoforge/network` |
| BE `storage_id` attachment + claim orchestrator | `neoforge/storage` |
| Per-player workflow runtime lifecycle | `neoforge/workflow` |

LDLib2 imports must stay out of `common/`. Inventory semantics stay out
of `neoforge/` UI code.

## Key Terms

Cross-doc vocabulary. Expanded definitions live in the linked docs.

**Atlas** — pan/zoom visual inventory canvas; the primary player-inventory
surface. See [design/atlas.md](design/atlas.md).
**Home** — stable visual coordinate owned by one item identity.
**Island** — player-facing organizational cluster inside a region.
**Region** — broad map zone containing one or more islands.
**Triage** — special island for unhomed/ambiguous identities.
**Lens** — overlay that changes meaning without rewriting atlas geometry
(search, Recent, cleanup, task).
**Landmark** — fixed atlas object (Belt, Triage intake) used as a
navigation anchor.
**Mirror / Ghost / Anchor / Action Surface** — projection roles; see
"Canonical Home Versus Derived Projection" in atlas.md.
**Kit** — task-shaped unit that unifies earlier "collection" + "loadout".
See [design/kits.md](design/kits.md).
**Belt** — camera-anchored atlas landmark representing the active hotbar.
**Kit Rack** — toggleable Kit Card shelf on the atlas.
**Authority** — the source of truth about what is in which slot. Kernel
owns authority; UI never invents it.
**Projection** — derived read model built from authority for a surface.
**Intent / Action Request** — server-authoritative command built from a
UI-triggered intent; see [architecture/action-taxonomy.md](architecture/action-taxonomy.md).

## External Resources

Use local reference source first when available, then current docs/APIs.

- LDLib2 docs: <https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/>
- LDLib2 UI agent guide:
  <https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/ui/agent_guide/>
- LDLib2 data bindings:
  <https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/ui/preliminary/data_bindings/>
- LDLib2 RPC packet:
  <https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/sync/rpc_packet/>
- Use Context7 / DeepWiki / upstream docs for NeoForge, Minecraft, LDLib2, and
  related mod APIs instead of guessing.

## Verification Commands

Fast compile checks:

```bash
./gradlew :common:testClasses
./gradlew :neoforge:testClasses
```

Focused test pass:

```bash
./gradlew :common:test --tests 'dev.imagio.slot.*'
./gradlew :neoforge:test
```

General hygiene:

```bash
git diff --check
rg "InventoryActionKind\\.(TRANSFER_STACK|TRANSFER_ONE|TRANSFER_ALL|QUICK_ACCESS_ASSIGN|PLACE|DROP|UNEQUIP|EQUIP)\\b" common/src neoforge/src
```

## Working Rules

- No quick fixes: investigate root causes and impacts before changing code.
- UI and LDLib code may own rendering, local focus, and transport; SLOT common
  owns inventory semantics.
- Screens and client RPC commands must not provide authoritative stack, count,
  identity, host id, or menu ref for real mutations.
- Unsupported host state must fail closed and log a useful diagnostic.
- Keep LDLib2 imports out of common domain code.
