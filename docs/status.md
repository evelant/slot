# SLOT Project Status

Last updated: 2026-04-20

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
- storage prototype Slices 2–3, 4a, 5 Take-All, 6, and 7 landed
  (domain + server-side identity machinery, claim RPC + chest-screen
  button, atlas storage-zone rendering + chest tile drag-to-reposition,
  `populate-chests` helper, live chest-contents grid + proximity
  activation, island↔chest link model with popover + proximity-driven
  link threads, deposit verb, Take All verb, per-item chest presence
  strip, chest naming, and workflow persistence — see
  [plans/storage-prototype.md](plans/storage-prototype.md)).
  Key landing points:
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
  - `SlotChestClaimPayload` (playToServer) + `ChestClaimButtonController`
    on the client: captures the last right-clicked block within a
    1.5s window, injects a "Claim" button into any non-shulker
    `AbstractContainerScreen`, and dispatches the claim through
    `ChestClaimServerService` after a server-side reach check
  - `ClaimedChestTile` record on `SlotWorkspaceViewModel` (storage id,
    atlas x/y, label with auto-fallback `Chest #xxxx`, anchor count) +
    codec roundtrip; projection derives tiles directly from
    `ClaimedChestMap` on every refresh
  - storage-zone backdrop + chest tile rendering in `SlotWorkspaceUiFactory`
    + drag-to-reposition via `moveChestEmitter` → `SlotWorkspaceUiSession`
    → `SlotWorkspaceCommandService.moveChest` →
    `ChestClaimWorkflowDomainService.moveChest`
  - `/slot test populate-chests <count> [radius]` places a ring of
    vanilla chests, fills each with a deterministic identity spread,
    and claims each via the real `ChestClaimServerService` path
  - chest contents grid: `ChestContentsReader` on neoforge reads the
    live `IItemHandler` snapshot for each claim and strips empty slots
    at the source (only filled stacks travel to the client);
    `ChestProximityResolver` produces a `Set<storageId>` in-proximity
    (default 8-block radius, same-dimension); both piped through
    `SlotWorkspaceViewModel.project(..., chestContentsResolver,
    proximateStorageIds)` into `ClaimedChestTile.contents` / `proximate`.
    Chest tile renders a 9-col grid that auto-heights to
    `ceil(filled / 9)` rows; non-proximate tiles + cells dim via the
    established carried-vs-ghost vocabulary
  - link model: `ChestLink(islandId, storageId)` + `ChestLinkMap` in
    `common/workflow/domain/`; `WorkflowEvent.ChestLinkCreated` /
    `ChestLinkRemoved` with projection reducer; cascade removal when
    the owning chest or island is deleted;
    `ChestLinkWorkflowDomainService.linkIslandToChest` /
    `unlinkIslandFromChest` gates on claim + island existence;
    `WorkflowDomainRuntime.chestLinkWorkflow()` accessor
  - link UI: "Link" button on each chest tile header opens a popover
    listing all non-Triage islands with per-row Link/Unlink; proximity-
    driven link threads (rotated panels via LDLib2 `Transform2D`) fan
    out from each proximate linked tile to island centers; linked
    islands get a 2-world-unit accent frame when any linked chest is
    in proximity
  - deposit verb: `DepositPlanner` in common iterates carried stacks
    (main + hotbar + offhand), resolves each stack's home island via
    `VisualHomeMap`, filters to stacks whose island has a link to a
    proximate chest, and returns a `DepositPlan` of `Assignment(laneId,
    slotIndex, itemId, candidateStorageIds)` entries. Unit tests cover
    the empty / no-home / triage / no-link / distant / happy-path /
    multi-candidate / multi-lane cases
  - deposit executor (`neoforge/storage/DepositExecutor`): for each
    assignment, simulates `ItemHandlerHelper.insertItemStacked(handler,
    stack, true)` on the first loaded-anchor `IItemHandler`, only
    commits on whole-stack fit, leaves the stack untouched otherwise.
    Triggered by a proximity-gated "Deposit" button in the SLOT
    workspace header via a zero-arg RPC; status bar surfaces
    `deposited / deposited_partial / rejected / nothing_to_deposit`
  - Slice 5 Take All: `TakeAllExecutor` in `neoforge/storage/`
    extracts each non-empty chest slot via the first loaded anchor's
    `IItemHandler`, pushes through `player.getInventory().add(stack)`,
    re-inserts any remainder back into the same chest slot, and falls
    back to `player.drop(...)` so stacks are never silently lost.
    Proximity-gated "Take" button on each chest tile header (enabled
    only when the tile is proximate and non-empty). Status bar surfaces
    `took_all / took_all_partial / nothing_to_take / rejected`
  - chest naming: `ChestLinkWorkflowDomainService.linkIslandToChest`
    auto-labels the chest with the island's label on first link when
    the chest label is still blank; inline rename at the top of the
    per-chest Link popover dispatches via `relabelChestEmitter` →
    `SlotWorkspaceCommandService.relabelChest` →
    `ChestClaimWorkflowDomainService.relabelChest`
  - Slice 6 per-item presence:
    `SlotWorkspaceViewModel.ChestPresenceEntry(storageId, label, count)`
    record + `List<ChestPresenceEntry> presence` on `AtlasItem`,
    codec-roundtripped. `project(...)` buckets each tile's `contents`
    by `ItemIdentity.itemId()` + `storageId`, sums counts, sorts per
    identity by descending count, and attaches the list to matching
    `AtlasItem`s. `detailAtlasBody` renders a single-line
    `in: <label> · <count> · …` strip (up to 3 entries + "+N" overflow)
    using the existing `anchorTextBand` vocabulary in `ACCENT`; click
    pans the camera to the first-ranked chest tile via the new
    `panToChestTile` helper (mirrors `panToIsland`)
  - Slice 7 persistence: `WorkflowCheckpointData` gained
    `claimedChests` + `chestLinks` arrays (plus `ClaimedChestData` /
    `ChestAnchorData` / `ChestLinkData` records) and round-trips every
    chest claim (storage id, anchors, atlas coords, label) + every
    `ChestLink(islandId, storageId)`. The seven previously null-stubbed
    workflow events (`ClaimedChest{Created, Moved, AnchorsChanged,
    Relabeled, Deleted}`, `ChestLink{Created, Removed}`) now encode +
    decode via the existing event-log format (new `WorkflowEventData`
    fields: `storageId`, `anchors`, `claimedChest`). Schema change is
    additive — older save files load cleanly with empty chest / link
    arrays
  - `ChestPersistenceReconciliation.reconcile(server, runtime)` runs
    right after `persistence.loadInto(...)` in
    `SlotPlayerWorkflowRuntimeService.createRuntime`. Policy is
    "unknown ≠ broken": anchors in unloaded chunks are kept; only
    anchors whose BE is loaded and either missing or carrying a
    mismatched `slot:storage_id` attachment are pruned. When every
    surviving anchor is known-broken, the claim is deleted (cascades to
    links via the projection reducer)
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

Core-workflow UX pass is **landed** — all six slices from
[plans/core-workflow-ux.md](plans/core-workflow-ux.md), plus several
playtest-driven follow-ups (see "Core-workflow UX landing points"
below). Next focus is resuming the Kit prototype (slices 4–9). Pick
up at [plans/kit-prototype.md](plans/kit-prototype.md). Nothing in
the UX pass touched Kit domain state, so resumption is a clean
continuation.

### Core-workflow UX landing points

- **Slice 1 — Docked Triage Panel**: `triageItems` field on
  `SlotWorkspaceViewModel` (codec-roundtripped); projection routes
  unhomed identities here instead of into the gone `ISLAND_TRIAGE`
  atlas island. Left-edge overlay (z=7) renders panel rows with
  drag source + drop target, chip rendering preserved. Drop-on-panel
  routes through the existing `clearHome` flow. Orphan assignments
  (target island deleted) fall back to triage rather than rendering
  unrooted. `applyHomeDrop` and `moveHotbarToAtlas` now allow
  `ISLAND_TRIAGE` past their existence checks (regressions caught
  during playtest).
- **Slice 2 — Hotbar ↔ home shift+click**: new RPCs
  `returnHotbarToHome(hotbarIndex)` and
  `assignHomeToFreeHotbar(itemId, comparisonMode, fingerprint)`
  in `SlotWorkspaceUiSession`. Server-side identity → first-main-slot
  resolution via `firstMainSlotForIdentity`; first-free-hotbar slot
  resolution via view-model scan. Status diagnostics:
  `returned_to_home / returned_unhomed / no_free_main_slot /
  nothing_to_return / assigned_to_hotbar_<n> / no_free_hotbar_slot /
  nothing_to_assign`. **No path writes through `ASSIGN_HOME`** —
  visual home position is read-only. Wired via `Screen.hasShiftDown()`
  on atlas card / belt slot / triage row click handlers.
- **Slice 3 — Bidirectional hover trails**: amber chrome-layer
  trail (z=9) connects hovered hotbar slot to its home card (or
  hovered home to its hotbar slot). Resolved per-tick via
  `resolveHoverTrail()` from `hoveredHotbarIndex` and
  `hoveredAtlasIdentity`. Endpoints update each tick from
  `getPositionX/Y` on the slot element + new `screenX/screenY`
  helpers on `SlotAtlasGraphView`. Also: hotbar slot accent and
  triage panel row accent toggled per-tick when matching hover state.
  Trail is always-displayed-zero-width when no hover (LDLib2 skips
  TICK on display=NONE elements).
- **Slice 4 — Chest tile drag-in + shift-take**: factored
  `DepositExecutor.depositSingleStack(player, laneId, slotIndex,
  chest)` and `TakeAllExecutor.takeSingleStack(player, chest,
  chestSlotIndex)` from the bulk paths. New `slotIndices`/
  `contentSlotIndices` parallel-list fields on
  `ChestContentsSnapshot` and `ClaimedChestTile` thread real
  IItemHandler slot indices to the UI cells (codec-roundtripped).
  Three new RPCs (`depositCarriedToChest`,
  `depositHotbarToChest`, `takeFromChest`) all gated through a
  shared `resolveProximateChest` (UUID → claim → proximity);
  return `not_proximate / unknown_chest_tile /
  invalid_chest_storage_id` for boundary failures. Drop target
  on each chest tile (proximate accent / non-proximate dim);
  shift+click on a proximate non-empty cell extracts.
- **Slice 5 — Shift-deposit to linked chest**:
  `assignHomeToFreeHotbar` and `returnHotbarToHome` now try a
  proximate-linked-chest deposit first via
  `resolveProximateLinkedChestForIdentity(...)` before falling
  through to the slice-2 hotbar/main path. Resolver iterates
  `ChestLinkMap.chestsLinkedFrom(islandId)`, filters to proximate
  chests with whole-stack room (simulate-then-commit; never
  silently partial), tiebreak: free slots ASC → matching count
  DESC → storage UUID ASC. Reuses `DepositExecutor.depositSingleStack`.
- **Slice 6 — Right-click context menu on homes (and hotbar)**:
  right-click opens a transient z=22 popover with a click-outside
  catcher (z=21). Atlas home menu: Send to hotbar / Deposit to
  linked chest / Re-home… / Cancel — each disabled with reason
  when not applicable. The "Re-home…" command opens an island
  picker (mirror popover) with all islands plus "Return to Triage";
  selecting dispatches the existing `assignHome` RPC (deliberate
  re-home path). Hotbar slot menu: Send to home / Cancel. Two new
  explicit RPCs (`assignHomeToHotbarOnly`,
  `depositHomeToLinkedChest`) bypass slice-5's auto-preference so
  the menu items are unambiguous; shift+click continues to use the
  preference path.

### Follow-ups landed during playtest

- **Inspector panel removed**: the right rail (~284 px) is gone;
  atlas claims the full body width. `inspectorPanel`,
  `selectionPanel`, `focusedAtlasItem`, `selectedHotbarSlot`
  remain as dead code in `SlotWorkspaceUiFactory` for easy
  reintroduction as a floating inspector if needed.
- **Chest cell click + drag-extract**: non-shift left-click on a
  proximate non-empty cell selects the identity (highlights any
  matching home). Drag from a cell starts a `ChestStackDrag`;
  any `DRAG_END` (cancel or drop) calls `takeFromChest` for that
  cell's `(storageId, chestSlotIndex)`.
- **Storage zone group drag**: new `StorageZoneBounds` record
  computes the chests' bounding box. New `storageZoneHeader` (16 px
  bar above the backdrop) is a draggable handle that starts a
  `StorageZoneDrag(grabOffset, origin)` payload. Drop fires a new
  `moveStorageZone(deltaX, deltaY)` RPC; server iterates all
  claimed chests and applies the delta via the existing
  `SlotWorkspaceCommandService.moveChest` per chest.
- **Bug fixes**:
  - `installChestTileDragSource` no longer uses capture-phase
    `MOUSE_MOVE` (so cell drag-extract can preempt it via the
    `isDragging` guard).
  - Removed `installViewportPanSurface(panel, atlas)` from
    `chestTilePanel` — atlas pan was racing chest tile drag and
    winning in `MOUSE_DOWN`. Empty-atlas pan still works through
    LDLib2's built-in `GraphView.onMouseDown`.
  - Island label sizing now re-applies on atlas-scale change, not
    just on `IslandRenderBudget` hash change. Clamped budget
    fields could match identically across rebuilds at very
    different scales, leaving fonts stuck huge or tiny.
  - Slice-3 hotbar-slot `MOUSE_ENTER/LEAVE` no longer call
    `rebuild()` (it tore down the drag source mid-drag); accent
    overlay updates via per-slot TICK transition tracking.

### Kit prototype: paused arc

Kit prototype slices 1, 2, and 3 are landed (see Kit landing
points below). Slices 4–9 remain paused; this is the natural
resume point. See [plans/kit-prototype.md](plans/kit-prototype.md)
for the original arc and Definition Of Done.

Kit prototype landing points so far:

- **Slice 1**: the Belt is a camera-pinned overlay on the atlas panel
  rather than a flex-row sibling below the atlas body. Pan/zoom leaves
  it anchored. No semantics change — transfer RPC path is untouched
- **Slice 2 (pragmatic scope)**: `KitDefinition(id, name, pages,
  bring, offhand)` + `KitPage` + `KitMap` + `KitActivation` in
  `common/workflow/domain/`. `KitWorkflowDomainService` exposes list,
  create, rename, update, delete, snapshot-from-authority, activate,
  deactivate, switch-page, plus `pageAsLoadout` and `planActivate`
  bridges to `LoadoutApplyService`. Kit events
  (`KitCreated`/`KitUpdated`/`KitDeleted`/`KitActivated`/
  `KitDeactivated`/`KitPageSwitched`) flow through `WorkflowEvent`,
  `WorkflowProjection.Snapshot`, and `WorkflowDomainRuntime`.
  `KitWorkflowDomainServiceTest` covers CRUD, activation,
  deactivation, page switching, and loadout-bridge. User-facing
  collection surface is removed from the LDLib workspace (tags on
  atlas cards, inspector panel, search tokens, RPC emitters, codec
  entries, command-service handlers). **Deferred**: the internal
  `CollectionWorkflowDomainService` and `Collection*` domain types
  still exist as plumbing for `InventoryBrowseService` (itself dead
  code from the retired sidebar prototype that needs its own
  cleanup pass). Kit events intentionally do not persist yet —
  Slice 8 adds that. Protection integration is Slice 5
- **Slice 2 partial**: the plan's exit criterion "Kit activation
  produces the same intent router requests a loadout activation
  would have" is represented by `planActivate` returning a real
  `LoadoutApplyPlan`. Actually dispatching the plan through the
  executor is Slice 3 work (wired when the UI calls activate)
- **Slice 3**: Kit Rack landed as a glass overlay pinned to the
  camera above the Belt, toggled by a "Kits" button on the Belt.
  Rack body shows one card per kit with: name, per-slot
  `readyCount / slotCount` readiness, a 9-cell mini slot strip that
  renders filled slots with their item icon (ghost-dimmed when the
  identity is not carried), and an inline delete. The header has a
  "Save Current Belt" button that calls `snapshotFromAuthority`
  server-side. Activating a card runs the full
  `KitWorkflowDomainService.planActivate` →
  `LoadoutApplyExecutor.execute` path through `InventoryActionExecutor`
  (same intent router as every other transfer); re-clicking the
  active card deactivates. No new action semantics, no client-side
  authority. View-model plumbing: `KitCard` +
  `KitSlotState` records on `SlotWorkspaceViewModel`, populated by
  `SlotWorkspaceViewModel.project(...)` from the authority snapshot
  and `KitMap`; codec-roundtripped. Command surface
  (`SlotWorkspaceCommandService.saveBeltAsKit / activateKit /
  deactivateKit / deleteKit`) + session RPC handlers +
  `SlotWorkspaceKitCommandServiceTest` covering save, activate,
  deactivate, delete, unknown-id paths. `activateKit` appends the
  plan's per-target reasons to the outcome diagnostics so future
  plan rejections surface as `reasons=target_blocked_by_policy:...`
  etc. in the status bar rather than a bare count.
  Pinned-constant regression:
  `KitWorkflowDomainService.pageAsLoadout` now builds
  `QuickAccessLaneTarget` with `BuiltinInventoryIds.QUICK_ACCESS_LANE_0`
  (the lane id) — earlier it used `PLAYER_QUICK_ACCESS_LANE_0` (the
  source id) which made `InventoryActionPolicy.allows` fail every
  target; `KitWorkflowDomainServiceTest.pageAsLoadoutUsesQuickAccessLaneId`
  locks this down. **Deferred to later slices**:
  multi-page editing (Slice 4), bring list + protection (Slice 5),
  drag-to-edit (Slice 6), gather (Slice 7), persistence (Slice 8),
  undo (Slice 9). **Known gap**: activation only scans
  `authority.carriedSources()`, so items sitting in a linked chest
  are not auto-withdrawn — Gather (Slice 7) and Storage Slice 5
  withdraw will cover this

**Slice 4b** (Kit-holdout deposit) and the withdraw half of storage
**Slice 5** remain blocked on Kit prototype Slice 5 (Kit-active
protection).

The underlying triage/home loop (from [plans/current.md](plans/current.md))
is landed enough to support the remaining Kit slices: template + learned
chip suggestions, chip-accept + manual-assign, island management,
persisted homes, and LDLib workspace transport all in place. Slice 3b
(reversible assignment records) is partial; search spotlight (slice 5)
has not started; neither blocks the Kit prototype.
`+N since last open` newness indicators are tracked under
"Later Feature Tracks" in `plans/current.md`.

## Small known bugs to fix

(none currently tracked — previous batch cleared 2026-04-17; needs playtest
verification of the LOD thresholds, default-camera fit, and batch chip
accept before declaring done)

## Project Structure

Top-level docs (see [../README.md](../README.md) for the full doc map):

- product: [product/direction.md](product/direction.md), [product/spec.md](product/spec.md)
- architecture: [architecture/overview.md](architecture/overview.md),
  [architecture/action-taxonomy.md](architecture/action-taxonomy.md),
  [architecture/host-ui.md](architecture/host-ui.md)
- design: [design/atlas.md](design/atlas.md), [design/kits.md](design/kits.md),
  [design/storage.md](design/storage.md)
- plans: [plans/current.md](plans/current.md),
  [plans/atlas-prototype.md](plans/atlas-prototype.md),
  [plans/kit-prototype.md](plans/kit-prototype.md),
  [plans/storage-prototype.md](plans/storage-prototype.md)
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
- `inventory/workspace`: UI-neutral workspace composition model,
  deposit planner
- `workflow/domain`: collections, loadouts, protection, recents, activity,
  visual homes, claimed chests, chest links, persistence-facing workflow
  domain
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
  resolver, claim orchestrator, break-event anchor cleanup, chest
  contents reader, proximity resolver, deposit / take-all executors,
  load-time anchor reconciliation
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
| Deposit planner (pure) | `inventory/workspace` |
| Collections, loadouts, recents, protection | `workflow/domain` |
| Visual homes, claimed chests, chest links, domain events | `workflow/domain` |
| Workflow snapshot JSON codec | `workflow/domain/persistence` |
| Atlas camera / storage-zone placement (pure) | `atlas` |
| Host compat shared helpers | `compat` |
| Screen/menu observation | `neoforge/client/host` |
| Player inventory replacement trigger + chest-claim button | `neoforge/client/screen` |
| LDLib2 workspace menu, session, view-model, RPC | `neoforge/screen/ldlib` |
| Atlas `GraphView`, item cards, camera preservation | `neoforge/screen/ldlib` (UI factory) |
| Open-workspace + chest-claim network payloads | `neoforge/network` |
| BE `storage_id` attachment + claim orchestrator | `neoforge/storage` |
| Chest contents reader, proximity resolver, deposit / take-all executors, load-time reconciliation | `neoforge/storage` |
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
