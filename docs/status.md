# SLOT Project Status

Last updated: 2026-07-08. Operational handoff. Read after [../README.md](../README.md).
For active work + queue see [plans/current.md](plans/current.md); for architecture see [architecture/overview.md](architecture/overview.md).

## Active

Cross-loader support is the active track. SLOT keeps the modern Minecraft
1.21.1 NeoForge + LDLib2 build and adds a Minecraft 1.20.1 Forge target
through [plans/cross-loader-refactor.md](plans/cross-loader-refactor.md)
and ADR [0006](decisions/0006-cross-loader-legacy-forge.md). The spike is
deleted; production Forge renders direct Taffy on vanilla `Screen`, and
`:forge-1.20:compileSharedProbeJava` compiles the whole common tree against
Forge 1.20.1 / Java 17 with real platform adapters.

Phase 1 has shared action transport, Forge runtime, session-backed projection,
common-service routing for metadata/transfer/workflows/storage actions, and
Forge AE2 persistent ME network storage per ADR
[0009](decisions/0009-ae2-persistent-network-storage.md). Phase 2 has the production wall shell on both loaders: shared card chrome, Recents, Belt,
active chest controls, workflow controls, accepted-input menus, compact nearby
headers, remembered search/scroll, margins, Fetch/Put Away guidance, Forge key
parity, and junk/trash pressure relief. Useful Now scoring is hidden while live
contextual observation remains available.

Workspace projection has timing, storage-index caching, remote-detail intents,
typed invalidations/fallbacks, full-oracle parity tests, projection-slice reuse,
revisioned full/delta transfer, budgeted tracked-storage polling, and source /
carried / storage / player-target facts. Slice 9 is retiring the monolithic hot
path: search is client-local, authority-only commands skip redundant
pre-command projection, cursor-only churn localizes hotbar/frame, bounded
carried-revision diffs localize affected identities, Forge menu-slot dirtiness
does not force full projection, storage-proximity/chest-claim dirtiness emits
storage-local invalidations, sequence-only workflow dirtiness becomes frame-only, and
remote-detail toggles, junk/direct-trash/belt/cursor changes, chest/cluster relabels, affinity-forget,
section create/delete/metadata, workflow/kit metadata, and affected identity/storage facts
including active-workflow storage-only put-away route changes localize. Rendered localized branches cover the simple cases in [plans/done/workspace-incremental-projection.md](plans/done/workspace-incremental-projection.md)
and still fail closed for complex put-away routing, complex craft-run pressure,
and wayfinding outside the simple acquisition/KIT cases. TerraFirmaGreg
validation remains in [plans/workspace-performance.md](plans/workspace-performance.md).

Learned storage gates each claimed chest through `Storage`, `Buffer`, or
`Ignore`; see ADR [0008](decisions/0008-chest-roles-and-affinity-correction.md).
Only `Storage` learns affinity and accepts quick/bulk deposit; `Buffer` stays visible/pullable, and `Ignore` is hidden from SLOT storage projection.

Classification has a pack-authoring path for large modpacks: installed `mods/`
scanning, jar/static enrichment, runtime export, rich facet evidence, LLM
vocabulary loops, classification, datapack output, and inspect/rehome commands.
Pre-LLM code gathers evidence; the LLM owns vocabulary and item-facet decisions.
Keep rich semantic text intact instead of reducing prompts to item ids.

EMI recipe context now uses the normal SLOT sidebar as a transient recipe
ingredient filter plus one persisted current craft run on both loaders. Visible
ingredients render in the wall; the tracked recipe list lives in the right-side
task panel; recipe inputs project as transient wanted-count pressure for
gather/storage/wayfinding; EMI recipe screens avoid floating Recents; selected
deficits stage through the shared transfer executor; and the run survives
logout/rejoin through workflow persistence. ADR
[0007](decisions/0007-emi-recipe-sidebar.md) records the pivot; the old
recipe-goal plan lives in
[plans/retired/emi-goal-projections.md](plans/retired/emi-goal-projections.md),
and the legacy code/UI/RPC/persistence model has been removed.

### Production wall shape (post-list-view)

The 2D pan/zoom atlas is gone. The wall is a single-LOD sectioned
vertical scroll list (`ListWallPanelBuilder`, `AtlasCardBuilder` rewritten)
with a docked TOC tab strip (`TocPanelBuilder`). Plan archived in
[plans/done/list-view.md](plans/done/list-view.md); sidebar embed sub-plan in
[plans/done/list-view-phase-3a.md](plans/done/list-view-phase-3a.md).

The workspace mounts in two surfaces with the **same widget tree**:
standalone full-screen inventory replacement and a sidebar child on supported
container/non-container screens, including EMI recipe screens when a handled
menu remains available for sync. Both use the same search/action row, workflow
controls, optional active-chest strip, Recents, wall scroller, optional workflow editor,
status row, and vanilla-shaped bottom Belt. Sidebar margins are client-config
so packs with FTB Chunks, quests, EMI, or other buttons can make room.

Cross-surface drag from a wall card to a vanilla menu slot remains wired.
Shift-click in sidebar/container mode now stays on SLOT's semantic path:
carried cards deposit to proximate chests by learned affinity or existing
contents, falling back to the currently open external chest when needed;
external ghost cards take from proximate storage. Dropped sidebar expansion
items need fresh plans if playtesting revives them.

Server-side: `SlotSidebarUiHandle` per-player, attaches the
sidebar's `ModularUI` to `player.containerMenu` via LDLib2's
`IModularUIHolderMenu` mixin so `PacketModularUISync` routes
correctly. Vanilla `broadcastChanges` then ticks the sidebar.
Cross-surface server methods use `CarriedSourceAccess` (covers Sophisticated
Backpacks etc.), provider-backed host storage when a dedicated terminal
integration claims the screen, and `Slot.safeInsert` only as the generic fallback.

**Phase 3b — hide vanilla player-inventory band — deferred experiment.**
Tracked from [plans/current.md § Queue](plans/current.md). If you start this,
write a fresh plan in `docs/plans/`; don't reopen the closed list-view plan.

**Discovered LDLib2 bug** (worked around, **user filing upstream**):
`ModularUI.calculateStyleAndLayout` checks width twice; keep root at `widthPercent(100)`.

## Project structure

Top-level docs: see [../README.md](../README.md) for the full map,
[plans/current.md](plans/current.md) for the queue, [product/direction.md](product/direction.md)
for product direction, [architecture/overview.md](architecture/overview.md) for architecture, and [plans/done/](plans/done/) / [plans/retired/](plans/retired/) for shipped or superseded plans.

Common module:

- `inventory/core`: descriptors, capabilities, host topology, policy, builtin
  ids, crafting surface descriptors
- `inventory/query`: authority snapshots and read services
- `inventory/browse`: UI-independent browse documents
- `inventory/action`: targets, action requests/outcomes, taxonomy dimensions,
  planners, canonicalization
- `inventory/session`: coordinator, intent router, command preflight
- `inventory/integration`: host resolution, providers, mutation router,
  builtin executor, compat provider contracts
- `inventory/workspace`: UI-neutral workspace composition + view-model,
  deposit planner
- `inventory/triage`: chip-suggestion service + island templates
- `classification`: `FacetIndex`, layer bootstrap/load reports, runtime
  export formatting, dynamic home-cohort policy
- `workflow/domain`: visual homes, claimed chests, chest affinity, chest
  cluster map, workflows, recents, craft runs, persistence
- `atlas`: pure helpers — `AtlasSearchIndex`, `AtlasRelevance` +
  contributors, `SectionOrdinal` (per-section ordinal lookups for
  drag-drop). Camera / layout / nudge / band / packer code retired
  with the list-view swap.
- `ui/workspace`: loader-neutral wall/card builders and chrome semantics;
  platform UIs render this tree rather than owning card state.
- `compat`: shared compat helpers

NeoForge module:

- `neoforge/client/host`: live screen/menu observation
- `neoforge/client/screen`: player inventory replacement trigger/mount glue
- `neoforge/screen/ldlib`: LDLib2 workspace menu, holder, UI session,
  view-model projection, panel builders (`ListWallPanelBuilder`
  is the wall surface; `TocPanelBuilder` the docked TOC),
  `AtlasCardBuilder` for single-LOD pixel cards, right-side workflow editor,
  active-chest / Recents / Belt builders, RPC dispatcher, drag/drop
- `neoforge/network`: workspace-open + RPC payload definitions
- `neoforge/storage`: BE `storage_id` attachment, claim orchestrator,
  break-event cleanup, chest contents reader, proximity resolvers,
  deposit observer, loot-chest right-click intercept. Deposit/take-all
  executors live in `common/` behind `CarriedSourceAccess` and
  `WorldStorageAccess`.
- `neoforge/triage`: signal extractor + classifier glue
- `neoforge/workflow`: per-player runtime lifecycle
- `neoforge/config`: dedicated-test-instance config defaults plus client UI margins

Forge 1.20 module:

- `forge-1.20`: legacy Forge 1.20.1 target with production common-source
  compilation, direct Taffy + `GuiGraphics` workspace renderer,
  `SimpleChannel` action transport, workflow persistence, session-backed
  projection, carried/world storage accessors, guarded
  transfer/hotbar/workflow/desired/wanted/chest/cursor/gather/wayfinding
  actions, Forge-only AE2 media-set network storage with per-cell media
  observations, chest
  `storage_id` break cleanup, measured shared-card badges,
  sidebar/task-panel margin config/depth fixes,
  `/slot test` and
  classification commands, and the Phase 0.5 `compileSharedProbeJava`
  shared-source compile gate.

Reference code (read-only): `reference/LDLib2`, `InventoryEssentials`,
`TrashSlot`, `Applied-Energistics-2`, `SophisticatedBackpacks`, `SophisticatedCore`, `Toms-Storage`, `emi`.

## Concept → Code Map

| Concept | Package |
| --- | --- |
| Authority snapshots | `inventory/query` |
| Source/entry identity, slot targets | `inventory/core` |
| Action taxonomy (`Kind+Quantity+Scope+Policy`) | `inventory/action` |
| Browse documents | `inventory/browse` |
| Session coordinator, intent router | `inventory/session` |
| Host resolution, mutation router | `inventory/integration` |
| Workspace composition + view model | `inventory/workspace` |
| Deposit planner (pure) | `inventory/workspace` |
| Visual homes, claimed chests, chest affinity, clusters, workflows, persistence | `workflow/domain` |
| Section ordinal lookups, search index, relevance scoring | `atlas/lod` |
| Item facets / classification | `classification` |
| LDLib2 workspace UI | `neoforge/screen/ldlib` |
| BE storage-id, claim orchestrator, deposit observer | `neoforge/storage` |
| World-storage deposit/take executors | `inventory/workspace` |
| Per-player workflow runtime | `neoforge/workflow` |

LDLib2 imports stay out of `common/`. Inventory semantics stay out of
`neoforge/` UI code.

## Key terms

**Wall** — the main inventory surface (formerly the "atlas").
Now a sectioned vertical scroll list of single-LOD cards. The
"atlas" name survives in code identifiers
(`AtlasItem`, `AtlasIsland`, `AtlasCardBuilder`, `atlasItems`)
to minimize churn — see list-view.md § Naming. **Section** —
player-facing organizational block (the new presentation of an
"island"). **Home** — stable section + ordinal owned by one item
identity. **Recents** — three-row floating center-top strip of the most
recently acquired identities into carried inventory, with
client-configurable screen offsets. It is not a discovery filter: world
pickups, chest/storage takes, machine outputs, crafting results, trades, and
rewards count when they enter carried storage; moves wholly inside carried
storage (main, hotbar, offhand, armor, Curios/backpacks, or other carried
providers) do not. **Workflow** — player-authored task view layered on top of
`All`; workflow membership behaves as an implicit active wanted-one target, accepted
inputs make exact/tag matches relevant without target pressure, workflows can have
one level of variants, and optional Belt/offhand pages reuse the older Kit
implementation substrate. **Belt** — docked hotbar strip at the bottom of the wall
with vanilla offhand-left layout. **Desired count** — persistent target
count, player-global (`All`) or active-workflow scoped. **Wanted count** — temporary
player target; `All` wanted counts retain global auto-clear behavior while
active-workflow wanted counts stay visible until the workflow deactivates. **Authority** — source of
truth about slot contents (kernel owns it; UI never invents).
**Projection** — derived read model built from authority for a surface.

## Verification commands

```bash
./gradlew :common:compileJava :neoforge:compileJava :forge-1.20:compileSharedProbeJava
./gradlew :common:test :neoforge:test
```

Classification tool checks:

```bash
cd tools/classification
bunx tsc --noEmit
bun test
```

## Working rules

- Investigate root causes before changing code; no quick fixes.
- UI / LDLib code owns rendering, local focus, and transport. Inventory
  semantics live in `common/`.
- Client RPC must not provide authoritative stack, count, identity,
  host id, or menu ref for real mutations.
- Unsupported host state fails closed with a useful diagnostic.
- LDLib2 imports stay out of `common/`.

## External resources

Use local reference source first when available, then current docs/APIs. LDLib2:
<https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/>,
<https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/ui/agent_guide/>,
data bindings, and RPC packet docs. Use Context7 / DeepWiki / upstream docs
for NeoForge / Minecraft / LDLib2 APIs instead of guessing.
