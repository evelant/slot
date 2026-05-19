# SLOT Project Status

Last updated: 2026-05-18. Operational handoff. Read after
[../README.md](../README.md). For active work + queue see
[plans/current.md](plans/current.md); for architecture see
[architecture/overview.md](architecture/overview.md).

## Active

Cross-loader support is the active track. SLOT keeps the modern
Minecraft 1.21.1 NeoForge + LDLib2 build and adds a Minecraft 1.20.1
Forge target through [plans/cross-loader-refactor.md](plans/cross-loader-refactor.md)
and ADR [0006](decisions/0006-cross-loader-legacy-forge.md). The spike
is deleted; production Forge renders direct Taffy on vanilla `Screen`,
and `:forge-1.20:compileSharedProbeJava` compiles the whole common tree
against Forge 1.20.1 / Java 17 with real platform adapters.

Phase 1 has shared action transport, Forge runtime, session-backed projection,
and common-service routing for metadata, transfer, hotbar, workflow tabs,
desired-count, chest, deposit/take, cursor, active-tab gather, and
cross-surface actions.

Phase 2 has the production wall shell on both loaders: fallback card details,
Recents, vanilla-shaped Belt, active chest controls, workflow tabs,
desired/wanted count chrome, remembered search/scroll state, configurable
sidebar margins, put-away guidance, and Forge key parity for inventory,
tab-page cycle, gather, put-away, wayfinding, Esc, and wanted-count controls.
Modern drag/drop, richer LDLib2 card/tab affordances, and richer chest panels
remain backend hooks, not common UI semantics.

Classification has a pack-authoring path for large modpacks: installed
`mods/` scanning, jar/static enrichment, runtime export, rich facet-evidence
collection, LLM vocabulary loops, vocabulary-grounded item classification,
datapack output, and runtime inspect/rehome diagnostics. Pre-LLM code gathers
and formats evidence; the LLM owns vocabulary and item-facet decisions, with
review/watchlist flags kept advisory. `organization_group` can materialize
direct wall-home sections; `mod_subsystem` stays semantic/query evidence. Keep
rich semantic text intact instead of reducing prompts to item ids or
deterministic candidates.

EMI recipe context now uses the normal SLOT sidebar as a transient recipe
ingredient filter on both loaders. When EMI's recipe screen is open, SLOT
renders into that screen while syncing through EMI's underlying handled menu;
the wall shows only visible recipe ingredients, with carried/storage context
and existing missing/craft target chrome. ADR
[0007](decisions/0007-emi-recipe-sidebar.md) records the pivot away from the
near-term recipe-goal surface; the old plan lives in
[plans/retired/emi-goal-projections.md](plans/retired/emi-goal-projections.md).

### Production wall shape (post-list-view)

The 2D pan/zoom atlas is gone. The wall is a single-LOD sectioned
vertical scroll list (`ListWallPanelBuilder`, `AtlasCardBuilder`
rewritten) with a docked TOC tab strip (`TocPanelBuilder`). Plan
archived in [plans/done/list-view.md](plans/done/list-view.md);
sidebar embed sub-plan in
[plans/done/list-view-phase-3a.md](plans/done/list-view-phase-3a.md).

The workspace mounts in two surfaces with the **same widget tree**:
standalone full-screen inventory replacement and a sidebar child on supported
container/non-container screens, including EMI recipe screens when a handled
menu remains available for sync. Both use the same search/action row, workflow
tabs, optional active-chest strip, Recents, wall scroller, optional tab editor,
status row, and vanilla-shaped bottom Belt. Sidebar margins are client-config
so packs with FTB Chunks, quests, EMI, or other buttons can make room.

Cross-surface drag from a wall card to a vanilla menu slot remains wired.
Shift-click in sidebar/container mode now stays on SLOT's semantic path:
carried cards deposit to proximate chests by learned affinity or existing
contents, falling back to the currently open external chest when needed;
external ghost cards take from proximate storage. Direction B (vanilla
cursor → wall card), wider host coverage past plain chests, hard-custom
screens (AE2 / RS), mod-observer transparency, and EMI exclusion area
registration were considered but **dropped from the plan** — spin a fresh
plan if any gain playtest signal.

Server-side: `SlotSidebarUiHandle` per-player, attaches the
sidebar's `ModularUI` to `player.containerMenu` via LDLib2's
`IModularUIHolderMenu` mixin so `PacketModularUISync` routes
correctly. Vanilla `broadcastChanges` then ticks the sidebar.
Cross-surface server methods use `CarriedSourceAccess` (covers
Sophisticated Backpacks etc.) + `Slot.safeInsert` (respects
`mayPlace` so crafting input limits / machine filters apply
natively).

**Phase 3b — hide vanilla player-inventory band — deferred as a
separate experiment.** The visual reclaim is worth playtesting in
isolation and the mod-compat surface (EMI `+`, sorting / hotkey-move
observers, hard-custom screens) wants its own plan. Tracked from
[plans/current.md § Queue](plans/current.md) under "deferred
experiments." If you start this, write a fresh plan in
`docs/plans/`; don't reopen the closed list-view plan.

**Discovered LDLib2 bug** (worked around, **user filing upstream**):
`ModularUI.calculateStyleAndLayout` checks width twice instead of height;
keep root at `widthPercent(100)` so scrollers get bounded space.

## Project structure

Top-level docs: see [../README.md](../README.md) for the full map. The
near-term queue lives in [plans/current.md](plans/current.md); product direction
in [product/direction.md](product/direction.md); current architecture in
[architecture/overview.md](architecture/overview.md); shipped plan references in
[plans/done/](plans/done/); superseded directions in [plans/retired/](plans/retired/).

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
  cluster map, workflow tabs, recents, persistence
- `atlas`: pure helpers — `AtlasSearchIndex`, `AtlasRelevance` +
  contributors, `SectionOrdinal` (per-section ordinal lookups for
  drag-drop). Camera / layout / nudge / band / packer code retired
  with the list-view swap.
- `compat`: shared compat helpers

NeoForge module:

- `neoforge/client/host`: live screen/menu observation
- `neoforge/client/screen`: player inventory replacement trigger/mount glue
- `neoforge/screen/ldlib`: LDLib2 workspace menu, holder, UI session,
  view-model projection, panel builders (`ListWallPanelBuilder`
  is the wall surface; `TocPanelBuilder` the docked TOC),
  `AtlasCardBuilder` for single-LOD pixel cards, right-side workflow tab editor,
  active-chest / Recents / Belt builders, RPC dispatcher, drag/drop
- `neoforge/network`: workspace-open + RPC payload definitions
- `neoforge/storage`: BE `storage_id` attachment, claim orchestrator,
  break-event cleanup, chest contents reader, proximity resolvers,
  deposit observer, loot-chest right-click intercept. Deposit/take-all
  executors live in `common/` behind `CarriedSourceAccess` and
  `WorldStorageAccess`.
- `neoforge/triage`: signal extractor + classifier glue
- `neoforge/workflow`: per-player runtime lifecycle
- `neoforge/config`: dedicated-test-instance config defaults plus client
  sidebar margins surfaced through the NeoForge config screen hook

Forge 1.20 module:

- `forge-1.20`: legacy Forge 1.20.1 target with production common-source
  compilation, direct Taffy + `GuiGraphics` workspace renderer,
  `SimpleChannel` action transport, workflow persistence, session-backed
  projection, carried/world storage accessors, guarded
  transfer/hotbar/workflow-tab/desired/wanted/chest/cursor/gather/wayfinding
  actions, sidebar margin config/depth fixes, `/slot test` and
  classification commands, and the Phase 0.5 `compileSharedProbeJava`
  shared-source compile gate.

Reference code (read-only): `reference/LDLib2`, `InventoryEssentials`,
`TrashSlot`, `Applied-Energistics-2`, `SophisticatedBackpacks`,
`SophisticatedCore`, `Toms-Storage`, `emi`.

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
| Visual homes, claimed chests, chest affinity, clusters, workflow tabs, persistence | `workflow/domain` |
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
identity. **Recents** — pinned strip of recently picked-up identities
above the wall. **Workflow tab** — player-authored task view layered on top of
`All`; tab membership behaves as an implicit active wanted-one target, tabs can
have one level of variants, and optional Belt/offhand pages reuse the older Kit
implementation substrate. **Belt** — docked hotbar strip at the bottom of the wall
with vanilla offhand-left layout. **Desired count** — persistent target
count, player-global (`All`) or active-tab scoped. **Wanted count** — temporary
player target; `All` wanted counts retain global auto-clear behavior while
active-tab wanted counts stay visible until the tab deactivates. **Authority** — source of
truth about slot contents (kernel owns it; UI never invents).
**Projection** — derived read model built from authority for a surface.

Expanded definitions in the linked design / architecture docs.

## Verification commands

```bash
./gradlew :common:compileJava :neoforge:compileJava
./gradlew :forge-1.20:compileSharedProbeJava
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
<https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/ui/preliminary/data_bindings/>,
<https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/sync/rpc_packet/>. Use
Context7 / DeepWiki / upstream docs for NeoForge / Minecraft / LDLib2 APIs
instead of guessing.
