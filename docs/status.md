# SLOT Project Status

Last updated: 2026-04-30. Operational handoff. Read after
[../README.md](../README.md). For active work + queue see
[plans/current.md](plans/current.md); for architecture see
[architecture/overview.md](architecture/overview.md).

## Active

**Track:** facet-driven suggestions Phase 1
([plans/facet-driven-suggestions.md](plans/facet-driven-suggestions.md)).
Plumb the ~28 unused facets the LLM has classified
(`mod_subsystem`, `activity`, `flavor`, `frequency`, `rarity`,
`origin`, `dye_color`, …) through `FacetIndex` so the suggestion
engine + `RealisticAtlasGenerator` can read them. Phase 1 is the
parser + accessor extension; Phases 2–5 build on it (subsystem-primary
matching, activity tie-break, within-island sort, trophy / frequency
priority).

**Recent landings (2026-04-30):**

- Learned-storage UX-bug pass closed: 14 original bugs + 9 follow-on
  bugs from real-instance testing all shipped. Recap lives in
  [plans/current.md](plans/current.md); the canonical design ref is
  [plans/learned-storage.md](plans/learned-storage.md).
- FacetIndex-driven populate path playtested cleanly — that's what
  unblocked the facet-driven suggestions work.

## Small known bugs

- **Kit drag-edit doesn't auto-apply to the active belt.** Dragging a
  home onto an *active* kit's slot updates the kit definition but the
  belt isn't re-applied. Per [design/kits.md § Edit a Kit](design/kits.md)
  the edit should propagate immediately when the target page is the
  active page. Scoped follow-up for the next person touching kit
  drag-to-edit.
- **Diagnostic logging in `AtlasNudgeLayout` / `AtlasLayout` is on.**
  Added during the initial-open overlap chase. Remove (or downgrade to
  DEBUG) once a couple of fresh-world opens confirm the layout
  converges cleanly across resolutions / GUI scales.

## Project structure

Top-level docs (see [../README.md](../README.md) for the full doc map):

- product: [product/direction.md](product/direction.md), [product/spec.md](product/spec.md)
- architecture: [architecture/overview.md](architecture/overview.md),
  [architecture/action-taxonomy.md](architecture/action-taxonomy.md),
  [architecture/host-ui.md](architecture/host-ui.md)
- design: [design/atlas.md](design/atlas.md), [design/kits.md](design/kits.md),
  [design/storage.md](design/storage.md), [design/relevance-lod.md](design/relevance-lod.md)
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
- `atlas`: pure layout helpers (`FitCarriedCamera`, `AtlasNudgeLayout`,
  `AtlasLayout`)
- `compat`: shared compat helpers

NeoForge module:

- `neoforge/client/host`: live screen/menu observation
- `neoforge/client/screen`: player inventory replacement trigger/mount glue
- `neoforge/screen/ldlib`: LDLib2 workspace menu, holder, UI session,
  view-model projection, panel builders, RPC dispatcher, drag/drop
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
| Atlas layout (pure) | `atlas/lod` |
| Item facets / classification | `classification` |
| LDLib2 workspace UI | `neoforge/screen/ldlib` |
| BE storage-id, claim orchestrator, deposit observer | `neoforge/storage` |
| Per-player workflow runtime | `neoforge/workflow` |

LDLib2 imports stay out of `common/`. Inventory semantics stay out of
`neoforge/` UI code.

## Key terms

**Atlas** — pan/zoom visual inventory canvas; the primary
player-inventory surface. **Home** — stable visual coordinate owned by
one item identity. **Island** — player-facing organizational cluster.
**Triage** — docked panel for unhomed/ambiguous identities (NOT an
atlas island). **Kit** — task-shaped unit unifying earlier "collection"
+ "loadout". **Belt** — camera-anchored atlas landmark for the active
hotbar. **Authority** — source of truth about slot contents (kernel
owns it; UI never invents). **Projection** — derived read model built
from authority for a surface.

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
