# SLOT Project Status

Last updated: 2026-04-16

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

Next work is the triage-first atlas loop. The previous string-match
auto-home is being removed outright (no `Blocks` starter, no heuristic
id classifier) before the new suggestion-chip layer is built on top.
Chips come from a small set of class/tag/component-based templates and
from rules learned from manual placements; nothing is homed without a
player tap.

The full slice sequence lives in [plans/current.md](plans/current.md) —
single source of truth. Near-term order: atlas readability cleanup →
remove legacy auto-categorization → template predicate layer (headless)
→ chips on Triage cards → home assignment + learned rules → undo toast
→ island management basics → search spotlight → persisted homes and
learned rules. Kit prototype
([plans/kit-prototype.md](plans/kit-prototype.md)) follows after the
triage/home loop proves out.

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
  persistence-facing workflow domain
- `compat`: shared compat helpers and provider-side integration support

NeoForge module:

- `neoforge/client/host`: live screen/menu observation
- `neoforge/client/screen`: player inventory replacement trigger/mount glue
- `neoforge/screen/ldlib`: LDLib2 workspace menu, holder, UI session,
  view-model projection, UI factory, and transfer request factory
- `neoforge/network`: narrow open-workspace payloads; not the old action
  request/outcome transport
- `neoforge/compat`: loader-side integration registration
- `neoforge/persistence`: platform persistence bridge
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
| Host compat shared helpers | `compat` |
| Screen/menu observation | `neoforge/client/host` |
| Player inventory replacement trigger | `neoforge/client/screen` |
| LDLib2 workspace menu, session, view-model, RPC | `neoforge/screen/ldlib` |
| Atlas `GraphView`, item cards, camera preservation | `neoforge/screen/ldlib` (UI factory) |
| Open-workspace network payloads | `neoforge/network` |
| Persistence bridge | `neoforge/persistence` |

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
