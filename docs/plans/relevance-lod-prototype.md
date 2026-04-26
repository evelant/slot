# Relevance-LOD Prototype — Implementation Plan

Last updated: 2026-04-25

Engineering task breakdown for the design at
[../design/relevance-lod.md](../design/relevance-lod.md). Read that doc
first for the model, contributors, and rationale.

For the architectural decision behind this plan (score-as-derivation,
client-owned layout), see
[../decisions/0005-relevance-score-and-layout-locality.md](../decisions/0005-relevance-score-and-layout-locality.md).

## Sequence (2026-04-25 alignment)

1. **Phase 1** — core machinery (invisible refactor). **Landed.**
2. **Phase 2.1** — contributors + client-side layout + wire format
   diet. **Landed** with transitional drag-drop using
   `localX/localY`-as-sort-key.
3. **Phase 2.2** — drag-drop ordinal semantics + auto-square islands
   + freeform-helper cleanup. **Landed.**
4. **`FacetIndex` runtime** — [item-classification.md](item-classification.md)
   milestone 6. **NEXT.**
5. **Phase 4** — classification-driven Triage chip suggestions.
6. **Deferred** — `StorageArea` domain + `area_proximity` /
   `chest_holds_relevant`; `shopping_list`; per-player weights;
   anti-relevance; max-relevance hold-toggle.

The flat `ClaimedChest` model gives us enough storage signal to test
the Phase-1+2 core. Re-evaluate StorageArea after Phase 2 playtest.

## Architecture: where things run

Captured in
[ADR 0005](../decisions/0005-relevance-score-and-layout-locality.md).
Summary:

- **Score is a derivation, not state.** Computed at the use site, never
  synced. Both server and client may compute it, with different
  contexts and possibly different contributor sets.
- **Layout is client-owned.** Cell sizes and positions are computed
  client-side from the view model + camera scale + active search query.
  Server stops shipping `x, y, width, height` on `AtlasItem`.
- **Drag-drop is ordinal.** Client resolves drop coordinate → island
  + insertion ordinal before sending. `VisualHomeAssignment` gains an
  `ordinal` field; `localX/localY` retire (or persist transitionally
  as a sort key).
- **Search query stays client-only.** Reflow on submit/clear matches
  the design's coarse-trigger discipline.

This locality choice is what makes Phase 2 cleanly executable. Don't
mix server-side and client-side layout decisions.

## Module-placement contract

Per [../../AGENTS.md](../../AGENTS.md):

- Pure logic with zero `net.minecraft.*` / `net.neoforged.*` /
  `com.lowdragmc.*` / `com.mojang.*` imports → `common/`.
- Rendering / input / LDLib UI / camera presentation → `neoforge/`.
- Score, contributors, band picker, packer → `common/`.
- Layout pass that ties them together → `common/` (pure logic, called
  from neoforge).
- Px-budget tuning, debug-overlay rendering, hotkey wiring → `neoforge/`.

Already-mistakes the codebase has paid for, so we don't repeat them:
do not let LDLib2 imports leak into `common/`; do not put inventory
semantics in `neoforge/` UI code.

## Phase 1 — Core machinery (LANDED)

Shipped as the architectural seam for everything that follows.

**What landed:**

- [common/.../atlas/lod/Band.java](../../common/src/main/java/dev/imagio/slot/atlas/lod/Band.java) — promoted from neoforge, `PIP` added.
- [common/.../atlas/lod/RelevanceScore.java](../../common/src/main/java/dev/imagio/slot/atlas/lod/RelevanceScore.java) — record with `value` + per-contributor breakdown, max-combine.
- [common/.../atlas/lod/RelevanceContributor.java](../../common/src/main/java/dev/imagio/slot/atlas/lod/RelevanceContributor.java) — interface.
- [common/.../atlas/lod/RelevanceContext.java](../../common/src/main/java/dev/imagio/slot/atlas/lod/RelevanceContext.java) — Phase-1 fields are `carriedIdentities` only.
- [common/.../atlas/lod/contributors/CarriedContributor.java](../../common/src/main/java/dev/imagio/slot/atlas/lod/contributors/CarriedContributor.java) — sole Phase-1 contributor.
- [common/.../atlas/lod/BandPicker.java](../../common/src/main/java/dev/imagio/slot/atlas/lod/BandPicker.java) + [BandPickerConfig.java](../../common/src/main/java/dev/imagio/slot/atlas/lod/BandPickerConfig.java) — `relevanceLift = 0` Phase 1.
- [common/.../atlas/lod/WeightedGridPacker.java](../../common/src/main/java/dev/imagio/slot/atlas/lod/WeightedGridPacker.java) — uniform-weight output bit-identical to `placementForOrdinal` (regression test guards this).
- [common/.../atlas/lod/AtlasRelevance.java](../../common/src/main/java/dev/imagio/slot/atlas/lod/AtlasRelevance.java) — view-model → context + score map.
- [neoforge/.../RelevanceDebugOverlay.java](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/RelevanceDebugOverlay.java) — toggleable badge, hotkey unbound by default.

**What stayed the same:** atlas behavior. Score machinery is wired but
no production code path consumes it for layout/render.

**What's still here from the old model** (gets removed in Phase 2):
`SlotWorkspaceUiController.ghostScaleFor`, `WorkspaceTheme.GHOST_SHRINK_SCALE`,
the `AtlasItem.x/y/width/height` wire fields, `placementForOrdinal`-driven
position assignment, `SlotWorkspaceAtlasLayout.placementForOrdinal` itself.

## Phase 2 — Wire contributors and move layout client-side

Split into two landings:

- **Phase 2.1** (LANDED) — Contributors + client-side layout + wire
  format diet. Atlas now renders relevance-shaped via `AtlasLayout`;
  position/size no longer cross the wire on `AtlasItem`. The
  transitional drag-drop path keeps `localX/localY` on
  `VisualHomeAssignment` and uses them as a canonical-order sort key.
- **Phase 2.2** (NEXT) — Drag-drop ordinal semantics + auto-square
  islands + freeform-helper cleanup. Replaces the transitional sort
  key with an explicit per-assignment `ordinal`, drops authored
  island width/height in favour of computed bounds.

### Phase 2.1 (landed) — Contributors and wire format

Done:

- `RelevanceContext` extended with `recentIdentities`,
  `activeKitMembers`, `activeKitMissing`, `searchMatchedIdentities`.
  `Builder` API for additive growth.
- Four new contributors:
  [`SearchMatchContributor`](../../common/src/main/java/dev/imagio/slot/atlas/lod/contributors/SearchMatchContributor.java)
  (0.95),
  [`KitMemberContributor`](../../common/src/main/java/dev/imagio/slot/atlas/lod/contributors/KitMemberContributor.java)
  (0.85),
  [`KitMissingContributor`](../../common/src/main/java/dev/imagio/slot/atlas/lod/contributors/KitMissingContributor.java)
  (0.85),
  [`RecentlyTouchedContributor`](../../common/src/main/java/dev/imagio/slot/atlas/lod/contributors/RecentlyTouchedContributor.java)
  (0.6). `AtlasRelevance.DEFAULT_CONTRIBUTORS` ships all five.
- `AtlasItem` wire format dropped `x`, `y`, `width`, `height`. Codec
  shrunk; all 49 consumer sites migrated to read positions/sizes via
  `host.placementFor(item)` against the client-side layout result.
- Server-side `SlotWorkspaceViewModel.build` no longer calls
  `placementForOrdinal` / `resolvePlacement` for atlas items;
  accumulators sort by `(islandId, assignment.localY,
  assignment.localX, name)` and emit in that canonical order.
- Server-side `fittedIslands` / `fitIsland` deleted (server ships
  authored island bounds; client packer fits the actual rendered size).
- `WorkspaceTheme.GHOST_SHRINK_SCALE`,
  `SlotWorkspaceUiController.ghostScaleFor`,
  `AtlasCardBuilder.applyAtlasCardGhostScale` deleted. Their visible
  effect is now produced by the band picker / packer.
- New: [`AtlasLayoutConfig`](../../common/src/main/java/dev/imagio/slot/atlas/lod/AtlasLayoutConfig.java),
  [`AtlasLayoutResult`](../../common/src/main/java/dev/imagio/slot/atlas/lod/AtlasLayoutResult.java),
  [`AtlasLayout`](../../common/src/main/java/dev/imagio/slot/atlas/lod/AtlasLayout.java)
  — packer-driven world-space placements computed every refresh.
  `relevanceLift = 1.5f` so a max-relevance item gets `2.5×` the
  baseline cell size.
- `SlotWorkspaceUiController` holds `currentLayout`, recomputed in
  `rebuildNow()` from the live view model + the active search query.
  All renderer call sites read positions through `placementFor(item)`.

Visible result: at default zoom, carried items render at ~75 px,
search matches at ~78 px, kit members at ~73 px, recents at ~61 px,
ghosts at 32 px. Search submit / clear, kit activate / deactivate,
recents-window changes all reflow because the layout is recomputed
client-side every refresh.

Tests added: [`RelevanceContextTest`](../../common/src/test/java/dev/imagio/slot/atlas/lod/RelevanceContextTest.java),
[`ContributorsTest`](../../common/src/test/java/dev/imagio/slot/atlas/lod/contributors/ContributorsTest.java),
[`AtlasLayoutTest`](../../common/src/test/java/dev/imagio/slot/atlas/lod/AtlasLayoutTest.java);
[`AtlasRelevanceTest`](../../common/src/test/java/dev/imagio/slot/atlas/lod/AtlasRelevanceTest.java)
extended for the new context fields. Wire-format round-trip and
neoforge model tests updated.

Manual playtest deferred — verify in-game that:

- atlas reads as carried-shaped at default zoom
- activating a kit visibly grows kit-relevant items
- submitting a search pops matches
- pickups don't make the atlas convulse mid-session

Known Phase-2.1 limitation: with `relevanceLift = 1.5f`, a max-relevance
carried card is ~75 px wide. Authored islands narrower than ~85 px
(card + padding) will have content overflow the chrome bounds. Phase
2.2's auto-square layout fixes this by sizing islands to their
content. Until then, don't author tiny islands.

Known Phase-2.1 visual quirk: items follow their authored island
position exactly. After Phase 2.2 the atlas-level packer reactivates
and islands auto-arrange — that's where the design's "constellation
auto-organizes around what's relevant" feel comes from. Phase 2.1
keeps player-authored island arrangement as a transitional fallback.

### Phase 2.2 (landed) — Ordinal drag-drop + auto-square islands

Drag-drop is now ordinal. The freeform-coordinate baggage on
`VisualHomeAssignment` is gone, islands no longer ship authored
width/height, and the client-side packer wraps to an auto-square
target derived from the cells' total pixel area.

Agreed semantics (preserved from the plan; verified by tests):

- **Push (insert) on drop.** Drop X onto B → remove X from its
  current order, insert at B's current ordinal. Everyone from that
  ordinal onward shifts +1.
- **Drop on empty space within an island** → append to end.
- **Drop on yourself** → no-op.
- **Cross-island drop** → remove from source list (compacting
  ordinals after the gap), insert into destination list (shifting
  ordinals at and after the insert slot).
- **No half-split** v1. Plain "insert before drop target."

What landed:

- [`VisualHomeAssignment`](../../common/src/main/java/dev/imagio/slot/workflow/domain/VisualHomeAssignment.java)
  carries `int ordinal` instead of `localX` / `localY`.
- [`VisualAtlasIsland`](../../common/src/main/java/dev/imagio/slot/workflow/domain/VisualAtlasIsland.java)
  drops authored `width` / `height` — chrome size now comes from the
  client-side packer. Empty / single-card islands still read because
  [`AtlasLayoutConfig`](../../common/src/main/java/dev/imagio/slot/atlas/lod/AtlasLayoutConfig.java)
  carries `minIslandWidth` / `minIslandHeight` floors.
- New `targetAspectFudge` knob on `AtlasLayoutConfig` (default
  `1.2`). Per-island wrap width =
  `round(sqrt(totalCellArea) × aspectFudge) + padding`, clamped at
  the empty-island floor. Reads in [`AtlasLayout.packIsland`](../../common/src/main/java/dev/imagio/slot/atlas/lod/AtlasLayout.java).
- [`SlotWorkspaceCommandService.assignHome`](../../common/src/main/java/dev/imagio/slot/inventory/workspace/SlotWorkspaceCommandService.java)
  takes `(itemId, comparisonMode, fingerprint, islandId, ordinal)`.
  `null` ordinal means "append" — `resolveOrdinal` counts the live
  assignments in the destination island.
- [`VisualAtlasWorkflowDomainService.assignHome`](../../common/src/main/java/dev/imagio/slot/workflow/domain/VisualAtlasWorkflowDomainService.java)
  takes `(identity, islandId, ordinal, …)`. The
  [`WorkflowProjection.applyVisualHomeAssignment`](../../common/src/main/java/dev/imagio/slot/workflow/domain/WorkflowProjection.java)
  helper performs the remove-from-source + insert-with-shift
  bookkeeping when the event projects.
- [`AtlasDropResolver`](../../common/src/main/java/dev/imagio/slot/atlas/lod/AtlasDropResolver.java)
  is the new pure helper. Given the live view model + layout result
  + a world coordinate, it returns `(islandId, ordinal)` or `null`.
  [`DragDropWiring.resolveDropOrdinal`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/DragDropWiring.java)
  consumes it for atlas-item, hotbar, and chest-stack drops onto
  islands.
- Wire format change: the home RPC payload now ships
  `(itemId, comparisonMode, fingerprint, islandId, ordinal)` — one
  field instead of two world coords. `moveHotbarToAtlas` similarly
  drops the world-coord pair in favour of `ordinal`.
- View-model `AtlasIsland` drops `width` / `height`. Renderer paths
  (island chrome, header, badges, link arrows, search index, camera
  fits) read sizes through the new
  [`SlotWorkspaceUiController.islandPlacementFor`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceUiController.java)
  helper, which falls back to the empty-island floor when the
  layout pass hasn't included the island yet.

UI follow-up polish (same session):

- **Atlas-level de-overlap.** The auto-square sizing meant authored
  `(x, y)` no longer guaranteed non-overlap. `AtlasLayout.packAtlas`
  walks islands in `(authored y, x, id)` order and slides each one
  right past collisions (with `atlasIslandGap`); when sliding can't
  make progress (collider behind us), drops below and resumes from
  the authored x. Authored positions still drive the *preference* —
  well-spaced islands stay where the player put them.
- **Island header LOD ceiling.** The header strip kept a fixed screen
  size, so zooming out grew it without bound (~50 wu at scale 0.2),
  crashing into rows above. New
  [`SlotWorkspaceAtlasLayout.ISLAND_HEADER_RESERVE`](../../common/src/main/java/dev/imagio/slot/inventory/workspace/SlotWorkspaceAtlasLayout.java)
  caps the world height (24 wu) and is shared by both
  `IslandChestBuilder.applyHeaderScale` (which clamps the header
  layout + derives the world font from the strip height so text
  doesn't overflow vertically) and `AtlasLayout.packAtlas` (which
  reserves the same band when probing for collisions). At extreme
  zoom-out the header degrades to a thin colored bar — labels aren't
  the point at that scale.
- **Carried/ghost differentiation.** Non-carried (ghost) cards now
  shrink to 65% of the relevance baseline via
  `AtlasLayoutConfig.ghostShrinkFactor`. Combined with the existing
  relevance lift, carried/ghost world-size ratio jumps from ~2.3× to
  ~3.6×. `WorkspaceTheme.GHOST_CARD_ALPHA` dropped 0.18 → 0.10 and
  `GHOST_ICON_OVERLAY_COLOR` alpha strengthened (0xC8 → 0xE0) so
  ghost chrome and icons both clearly recede; the carried set forms
  the visual foreground.

Cleanup (dead freeform code, removed):

- `SlotWorkspaceAtlasLayout.placementForOrdinal`,
  `placementForDrop`, `clampPlacement`, `resolvePlacement`,
  `LocalPlacement`, `Placement` — all gone.
- `SlotWorkspaceCommandService.resolvePlacement` — gone; replaced
  by `resolveOrdinal`.
- Freeform-era tests
  (`placementStartsBelowIslandHeaderReserve`,
  `dropPlacementFloorsToContentMinimumButAllowsGrowthPastEdge`,
  `storedLocalHomeCoordinatesProjectBackIntoAtlasSpace`) — gone.

Migration:

- Persistence schema bumped from 5 → 6.
[`WorkflowDomainFileStore.decodeVisualHomesWithMigration`](../../common/src/main/java/dev/imagio/slot/workflow/domain/persistence/WorkflowDomainFileStore.java)
  derives ordinals from legacy `(x, y)` per island when no
  assignment carries an explicit ordinal. The next save flushes the
  migrated form. Cached pre-2.2 `VisualHomeAssigned` /
  `VisualHomeCleared` events are stripped on load — the migrated
  checkpoint is authoritative; we accept losing any unsaved homing
  actions since the last checkpoint.

Tests added (or revised):

- [`VisualAtlasWorkflowDomainServiceTest`](../../common/src/test/java/dev/imagio/slot/workflow/domain/VisualAtlasWorkflowDomainServiceTest.java)
  — append-then-insert shift, same-island move down, cross-island
  move with source compaction, clear compacts trailing ordinals,
  out-of-range ordinal clamps to size.
- [`AtlasDropResolverTest`](../../common/src/test/java/dev/imagio/slot/atlas/lod/AtlasDropResolverTest.java)
  — drop on item, drop on island chrome, cross-island, drop in
  empty space, triage skipped.
- [`AtlasLayoutTest`](../../common/src/test/java/dev/imagio/slot/atlas/lod/AtlasLayoutTest.java)
  — empty-island floor, auto-square aspect for many cells.
- [`WorkflowDomainFileStoreTest.preTwoTwoFileMigratesLegacyCoordsIntoOrdinals`](../../neoforge/src/test/java/dev/imagio/slot/workflow/domain/persistence/WorkflowDomainFileStoreTest.java)
  — hand-crafted v5 file with three legacy assignments; load
  produces ordinals 0, 1, 2 in `(y, x, identity)` order.

### Out of scope this phase (still)

- `area_proximity` — depends on `StorageArea`. Deferred.
- `chest_holds_relevant` — same.
- `shopping_list` — feature doesn't exist.

## Phase 3 — `FacetIndex` runtime

Owned by [item-classification.md milestone 6](item-classification.md).
Summary here so the relevance-LOD plan flows; full spec in that doc.

- New package: `common/.../classification/`.
- `FacetIndex` — load layered JSON, merge per the layer order
  (`vanilla-base` < `per-mod` < `runtime-crawl` < `modpack` < `server`
  < `player`), expose:
  - `Optional<FacetRecord> lookup(ItemIdentity)`
  - `Stream<ItemIdentity> where(FacetPredicate)`
  - `Set<String> facetValues(String facetName)`
- Bundle the vanilla dataset as a resource:
  `common/src/main/resources/data/slot/classification/vanilla-base.json`
  (copy of `tools/classification/datasets/minecraft/minecraft.facets.complete.json`).
- Layer-merge rules: per-facet `mode` (`replace` / `merge` / `add` /
  `subtract`) drives entry combination.
- Init-time merge: build the merged in-memory index once at boot, no
  lazy merging.
- Feature flag (`slot.classification.facetIndex.enabled`) so we can
  ship without flipping homing behavior until Phase 4 lands.

### Tests added

- Load + merge the vanilla dataset; spot-check known items.
- Layer merge: synthetic player layer overrides a vanilla entry.
- `where(role=tool && material=iron)` returns expected identities.

## Phase 4 — Classification-driven Triage suggestions

**Exit criteria:** Triage chips fire from `FacetIndex` lookups; chip
acceptance rate is logged per confidence band; rates are persisted so
the auto-home stretch goal can be evaluated from real numbers.

### 4a. Replace `IslandSuggestionService` signal source

- `IslandSuggestionService.suggest(...)` currently takes
  `IslandSignalDescriptor` (class/tag/component checks). Add a new
  overload that takes a `FacetRecord` + a `FacetIndex` reference.
- The descriptor-based path stays as the no-data fallback when
  `FacetIndex` returns empty.
- Templates (FOOD/TOOLS/...) stay as the seed island set; matching
  rule reads from facets instead of class checks.

### 4b. Confidence bands

- `FacetRecord` carries per-facet `confidence`. Aggregate per
  candidate island = average of facets agreeing on that target.
- Mapping:
  - **High** (≥ 0.85, ≥ 2 agreeing facets, exactly one matching island):
    single chip.
  - **High, no matching island**: single chip materializes a new
    island seeded from `role` + `material_family`.
  - **Medium** (≥ 0.6, two plausible matches): up to two chips.
  - **Low** (< 0.6 or single low-confidence facet only): no chip.

### 4c. Acceptance-rate tracking

- `common/.../inventory/triage/SuggestionAcceptanceLog.java`.
- Records (chip-shown, chip-accepted, chip-dismissed) per confidence
  band. Persist alongside learned rules.
- Surface as a dev-overlay panel.

### 4d. Server-side scoring becomes useful

`FacetIndex` lets the server make routing decisions for new picks
(future auto-home stretch goal). The score machinery already lives in
common; the server can build its own context (no search query, but
classification facets + carried + active kit) and call
`RelevanceScore.compute(...)` for whatever decision it's making. ADR
0005 anticipates this.

## Risks and open questions

- **Layout reflow cost at modded scale.** The packer runs every
  refresh over all islands with hundreds of items each. Profile during
  Phase 2 with the realistic-atlas fixture; cap the work or memoize
  per-island if hot.
- **Atlas convulse on pickup.** v2 reflows everything on every
  refresh, including mid-session pickups. If the resulting motion
  reads as "the atlas rearranges every time I pick up a stick,"
  introduce a previous-layout snapshot + incremental flag. Watch for
  this in playtest.
- **Pip band readability.** "Single-pixel pip" reads great in the
  design doc; verify 6–8 px icon-only renders aren't noise. Test at
  modded scale before declaring Phase 2 done.
- **Drag-drop ordinal UX.** Players lose pixel-precise placement
  within an island. Watch for "I can't find the spot I dragged things
  to" — if it surfaces, consider a "freeform" island kind that
  retains authored coordinates.
- **`VisualHomeAssignment` migration.** Existing saves have
  `(localX, localY)` but no `ordinal`. Migration derives ordinal at
  load time from the existing sort. Test against a populated dev save.
- **`AtlasRenderBudget` per-cell sizing.** Today the budget is
  computed once per render from the camera scale × the item's world
  width. With variable cell sizes, the per-cell budget naturally
  varies — already what the code does, just with `item.width()`
  replaced by the layout-result width.

## Deferred (write down so we don't forget)

- `StorageArea` domain type + claim-flow change + `area_proximity` /
  `chest_holds_relevant` contributors. Re-evaluate after Phase 2
  playtest decides whether the model needs them to feel right.
- `shopping_list` contributor — depends on a craft-planner that
  doesn't exist.
- "Max relevance everywhere" hold-modifier — implement only if
  pip-band visibility is genuinely insufficient in playtest.
- Per-player relevance weights — not until evidence demands it.
- Anti-relevance / suppression contributors — easier to add later
  than to remove a misbehaving one.
- `Band` rename of `DETAIL` → `CLOSE_INSPECT` for doc-vocabulary
  consistency. Cosmetic; defer to a cleanup pass.
- Auto-homing on pickup. Stretch goal in
  [../design/relevance-lod.md § Stretch goal: rethink Triage's existence](../design/relevance-lod.md);
  builds on Phase 4's classification confidence numbers.
