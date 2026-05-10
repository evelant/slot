# SLOT Project Status

Last updated: 2026-05-10. Operational handoff. Read after
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

Phase 1 has the shared action catalog/channel, packet codec,
session/menu envelope, Forge transport/runtime, and session-backed
common projection. Forge routes metadata, transfer, hotbar, kit,
desired-count, chest, deposit/take, cursor, active-kit gather, and
cross-surface actions through common services/executors for vanilla
carried sources and claimed chests. Active-kit gather and kit-page cycle
are common services; Forge `/slot test populate <profile>` and
`/slot test clear` cover carried inventory, workflow state, and claimed
chests.

Phase 2 has the wall/card shells, fallback card details, Recents, hotbar,
active chest strip, and non-drag kit rack through the first common UI
SPI. Forge full-screen and sidebar hosts share `ForgeWorkspaceSurface`
and common view-model/search/wall/Recents/kit/active-chest/hotbar
builders plus tooltip metadata. Forge key parity covers vanilla
inventory, kit page cycle, gather, and the wayfinding HUD toggle. Modern
drag/drop, richer LDLib2 card/kit affordances, and richer chest panels
remain backend hooks, not common UI semantics.

Classification now has a pack-authoring path for large modpacks: installed
`mods/` scanning, jar extraction, OpenRouter-backed stage 3, runtime export,
runtime subsystem vocabulary, and drop-in datapack layer output. Runtime
auto-home uses bundled/datapack facets plus dynamic subsystem cohorts; both
loaders expose `inspect`, `export`, and `rehome` / `recompute` commands.

### Production wall shape (post-list-view)

The 2D pan/zoom atlas is gone. The wall is a single-LOD sectioned
vertical scroll list (`ListWallPanelBuilder`, `AtlasCardBuilder`
rewritten) with a docked TOC tab strip (`TocPanelBuilder`). Plan
archived in [plans/done/list-view.md](plans/done/list-view.md);
sidebar embed sub-plan in
[plans/done/list-view-phase-3a.md](plans/done/list-view-phase-3a.md).

The workspace mounts in two surfaces with the **same widget tree**:
standalone (player-inventory key) opens a full-screen
`ModularUIContainerScreen`; sidebar mounts as a child widget on any
non-SLOT `AbstractContainerScreen` (chest, crafting, machine). Both
share `SlotWorkspaceUiController.WORKSPACE_WIDTH_PX = 414` for the
centered content stack and let belt + kit rack escape to root-level
full-width sibling slots so the hotbar covers the vanilla one.
Cross-surface drag (wall → vanilla menu slot) is wired in direction
A: drag-release, shift+click, and shift+wheel-up all route to the
host menu when sidebar is active. Direction B (vanilla cursor →
wall card), wider host coverage past plain chests, hard-custom
screens (AE2 / RS), mod-observer transparency, and EMI exclusion
area registration were considered but **dropped from the plan** —
spin a fresh plan if any gain playtest signal.

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

Top-level docs (see [../README.md](../README.md) for the full map):

- product: [product/direction.md](product/direction.md), [product/spec.md](product/spec.md)
- architecture: [overview](architecture/overview.md),
  [architecture/action-taxonomy.md](architecture/action-taxonomy.md),
  [architecture/host-ui.md](architecture/host-ui.md)
- design: [design/atlas.md](design/atlas.md) (superseded by
  list-view.md; surviving parts only — homes, ghost vs carried,
  single-element drag rule, recents, kit / desired-count /
  wayfinding integration), [design/kits.md](design/kits.md),
  [design/storage.md](design/storage.md). Retired:
  [design/retired/relevance-lod.md](design/retired/relevance-lod.md).
- plans (active queue): [plans/current.md](plans/current.md). Shipped
  plans live in [plans/done/](plans/done/); superseded designs in
  [plans/retired/](plans/retired/).
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
  cluster map, kits, recents, persistence
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
  `AtlasCardBuilder` for single-LOD pixel cards, RPC dispatcher,
  drag/drop
- `neoforge/network`: workspace-open + RPC payload definitions
- `neoforge/storage`: BE `storage_id` attachment, claim orchestrator,
  break-event cleanup, chest contents reader, proximity resolvers,
  deposit observer, loot-chest right-click intercept. Deposit/take-all
  executors live in `common/` behind `CarriedSourceAccess` and
  `WorldStorageAccess`.
- `neoforge/triage`: signal extractor + classifier glue
- `neoforge/workflow`: per-player runtime lifecycle
- `neoforge/config`: dedicated-test-instance config defaults

Forge 1.20 module:

- `forge-1.20`: legacy Forge 1.20.1 target. Current contents are
  production common-source compilation with Forge 1.20 adapters,
  direct Taffy + `GuiGraphics` workspace renderer, Forge
  `SimpleChannel` action transport with server-side validation, Forge
  workflow persistence, session-backed common projection for carried
  inventory and claimed chest ghosts, safe metadata command dispatch,
  carried/world storage accessors, first guarded built-in transfer plus
  identity-to-hotbar / hotbar-return / hotbar-to-section adapters,
  kit/desired-count and chest metadata dispatch, Forge-side `/slot test
  populate` / `clear` commands for carried-inventory and claimed-chest
  testing, `/slot classification inspect` / `export` / `rehome`
  commands for classifier diagnostics and pack-layer work, and the
  Phase 0.5
  `compileSharedProbeJava` task compiling the shared common source tree
  plus Forge 1.20 platform probes.

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
| Visual homes, claimed chests, chest affinity, clusters, kits, persistence | `workflow/domain` |
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
above the wall. **Kit** — task-shaped unit
unifying earlier "collection" + "loadout". **Belt** — docked
hotbar strip at the bottom of the wall. **Authority** — source
of truth about slot contents (kernel owns it; UI never invents).
**Projection** — derived read model built from authority for a
surface.

Expanded definitions in the linked design / architecture docs.

## Verification commands

```bash
./gradlew :common:compileJava :neoforge:compileJava
./gradlew :forge-1.20:compileSharedProbeJava
./gradlew :common:test :neoforge:test
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

Use local reference source first when available, then current docs/APIs.

- LDLib2 docs: <https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/>
- LDLib2 UI agent guide:
  <https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/ui/agent_guide/>
- LDLib2 data bindings:
  <https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/ui/preliminary/data_bindings/>
- LDLib2 RPC packet:
  <https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/sync/rpc_packet/>
- Use Context7 / DeepWiki / upstream docs for NeoForge / Minecraft /
  LDLib2 APIs instead of guessing.
