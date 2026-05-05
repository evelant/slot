# SLOT Project Status

Last updated: 2026-05-05. Operational handoff. Read after
[../README.md](../README.md). For active work + queue see
[plans/current.md](plans/current.md); for architecture see
[architecture/overview.md](architecture/overview.md).

## Active

No single dominant track. The list-view rewrite that was the active
plan through 2026-05-04 closed 2026-05-05 — see **Production wall
shape** below for the architectural snapshot it leaves behind. The
queue in [plans/current.md](plans/current.md) is the source of truth
for what's next; near the top sit the cursor + desired-counts
playtest-bug pass, **the deferred Phase 3b experiment** (hide
vanilla 36-slot band — separate task per user direction), and
workspace projection caching.

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

**Discovered LDLib2 bug** (worked around, **user filing
upstream**): `ModularUI.calculateStyleAndLayout` line 563 uses
`Float.isNaN(layoutWidth)` in the second NaN check instead of
`layoutHeight`. When root has a fixed WIDTH style, both axes get
`MAX_CONTENT` available space; scroller never engages, belt
overflows off-screen. Workaround: keep root at `widthPercent(100)`.

For dated landings and the operational bug list, see
[plans/current.md](plans/current.md).

## Project structure

Top-level docs (see [../README.md](../README.md) for the full doc map):

- product: [product/direction.md](product/direction.md), [product/spec.md](product/spec.md)
- architecture: [architecture/overview.md](architecture/overview.md),
  [architecture/action-taxonomy.md](architecture/action-taxonomy.md),
  [architecture/host-ui.md](architecture/host-ui.md)
- design: [design/atlas.md](design/atlas.md) (superseded by
  list-view.md; surviving parts only — homes, ghost vs carried,
  Triage panel, single-element drag rule, kit / desired-count /
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
- `classification`: `FacetIndex` + per-mod facet loaders
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
  deposit / take-all executors, deposit observer, loot-chest right-click
  intercept
- `neoforge/triage`: signal extractor + classifier glue
- `neoforge/workflow`: per-player runtime lifecycle
- `neoforge/config`: dedicated-test-instance config defaults

Reference code (read-only, for design comparison):

- `reference/LDLib2`, `reference/InventoryEssentials`, `reference/TrashSlot`,
  `reference/Applied-Energistics-2`, `reference/SophisticatedBackpacks`,
  `reference/SophisticatedCore`, `reference/Toms-Storage`, `reference/emi`

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
identity. **Triage** — docked panel for unhomed/ambiguous
identities (NOT a wall section). **Kit** — task-shaped unit
unifying earlier "collection" + "loadout". **Belt** — docked
hotbar strip at the bottom of the wall. **Authority** — source
of truth about slot contents (kernel owns it; UI never invents).
**Projection** — derived read model built from authority for a
surface.

Expanded definitions in the linked design / architecture docs.

## Verification commands

```bash
./gradlew :common:compileJava :neoforge:compileJava
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
