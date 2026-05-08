# SLOT Project Status

Last updated: 2026-05-07. Operational handoff. Read after
[../README.md](../README.md). For active work + queue see
[plans/current.md](plans/current.md); for architecture see
[architecture/overview.md](architecture/overview.md).

## Active

Cross-loader support is the active track. SLOT keeps the modern
Minecraft 1.21.1 NeoForge + LDLib2 build and adds a Minecraft 1.20.1
Forge target through the plan in
[plans/cross-loader-refactor.md](plans/cross-loader-refactor.md) and
ADR [0006](decisions/0006-cross-loader-legacy-forge.md). The validated
Phase 0 proved direct Taffy rendering on vanilla `Screen`; the
throwaway spike source has been deleted now that the production Forge
UI tree exists. The Phase 0.5 shared-platform compile gate now compiles the whole common
`dev.imagio.slot` tree against Forge 1.20.1 / Java 17, and Forge `main`
now consumes that common tree with production Forge 1.20
`SlotStackAccess` / `SlotResourceAccess` adapters. Phase 1 has the
shared workspace action catalog/channel, packet codec, and session/menu
envelope in common, with the modern LDLib2 RPC dispatcher validating
registrations and sends against that catalog. Forge 1.20 now registers a
matching `SimpleChannel` action payload that decodes the common packet
codec, validates session/menu envelopes against a server-side Forge
workspace session registry, and now owns a Forge workflow runtime plus a
session-backed common workspace projection for carried player inventory.
Safe workspace metadata actions (`SET_SEARCH_QUERY`, home/chip/island
management, undo/redo) route through `SlotWorkspaceCommandService`;
Forge installs carried/world storage accessors and the catalog
`TRANSFER` action is bound for built-in main/hotbar targets through
`InventoryActionExecutor`, with identity-to-hotbar, hotbar-return,
hotbar-to-section, kit, desired-count, chest metadata, deposit/take,
cursor, and cross-surface Forge adapters live for vanilla carried
sources and claimed chests. Forge also registers `/slot test populate
<profile>` and `/slot test clear` for carried-inventory/workflow/chest testing;
claimed chest ids use Forge persistent block-entity data and claimed
chest contents feed the common projection. Manual chest-close deposit
observation and chest-claim persistence reconciliation now share common
helpers, with loader-specific storage-id readers. Phase 2 has started
with the main wall section/card shells, shared fallback card details,
Recents strip, hotbar belt, and non-drag kit rack rendered through the
first narrow common UI SPI and LDLib2/backend-specific renderers. The Forge `G` debug screen
is now fed by session-backed view-model sync, including local search,
scroll preservation across rebuilds, hotbar/offhand projection, claimed
chest ghost projection, and the same item-id ghost stack resolver hook
used by NeoForge. Forge also mounts a first-cut vanilla-container
sidebar with the common active chest strip, non-drag kit rack, wall,
Recents, search, and hotbar belt; host-menu changes refresh through a
Forge transport sync message rather than a shared inventory action. The
Forge full-screen and sidebar hosts share `ForgeWorkspaceSurface`, so
widget composition and action dispatch do not fork by host.
Modern drag/drop, tooltips, richer LDLib2 card body
rendering, richer LDLib2 kit drag/context-menu affordances, and richer
chest panels remain backend hooks, not common UI semantics.

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
  deposit observer, loot-chest right-click intercept. Deposit/take-all
  executors live in `common/` behind `CarriedSourceAccess` and
  `WorldStorageAccess`.
- `neoforge/triage`: signal extractor + classifier glue
- `neoforge/workflow`: per-player runtime lifecycle
- `neoforge/config`: dedicated-test-instance config defaults

Forge 1.20 module:

- `forge-1.20`: legacy Forge 1.20.1 target. Current contents are
  Gradle/module scaffolding, production common-source compilation with
  Forge 1.20 platform adapters,
  the direct Taffy + `GuiGraphics` SPI debug renderer, a Forge
  `SimpleChannel` workspace-action path with server-side session
  validation, Forge workflow persistence, session-backed common
  view-model projection for carried player inventory and claimed chest
  ghosts, safe metadata command dispatch, Forge carried/world storage
  accessors, first guarded built-in transfer dispatch plus
  identity-to-hotbar / hotbar-return / hotbar-to-section adapters,
  kit/desired-count and chest metadata dispatch, Forge-side `/slot test
  populate` / `clear` commands for carried-inventory and claimed-chest
  testing, and the Phase 0.5
  `compileSharedProbeJava` task compiling the shared common source tree
  plus Forge 1.20 platform probes.

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
