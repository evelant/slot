# Relevance-LOD Prototype — Implementation Plan

Last updated: 2026-04-25

> **Retired (2026-05-03).** Superseded by
> [list-view.md](list-view.md). LOD bands aren't shipping; the list view
> uses a single LOD. Phase 1 (the invisible refactor — relevance score
> machinery + contributors) survives because the scoring is still useful
> for ordering, search, and TOC status dots; subsequent phases (band
> selection, band-driven cell sizing, multi-band card chrome) are
> abandoned. Do not pull from this plan; queue work against the list-view
> plan instead.

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
4. **`FacetIndex` runtime + atlas-homing wiring** —
   [item-classification.md](item-classification.md) milestones 6 + 7.
   **Landed** (V1 surface). Bundled `vanilla-base.json` resource +
   role lookup + `RoleSemanticBucketMap` + `FacetIndexBucketClassifier`
   with `SemanticBucketResolver::classify` as the no-data fallback.
   Awaiting playtest.
5. **Phase 4a** — classification-driven Triage chip suggestions.
   **Landed.** `IslandSignalDescriptor.role` + `materialFamily`
   populated from `FacetIndex` in `IslandSignalExtractor`; each
   `IslandSuggestionTemplate` carries a `roleTriggers` set and prefers
   role-based matching, falling through to the existing class/tag
   signals when role is absent. Template enum now covers the full v1
   role taxonomy (17 templates: FOOD, TOOLS, WEAPONS, ARMOR,
   MATERIALS, STORAGE, BUILDING, DECORATION, NATURAL, WORKBENCHES,
   MECHANISMS, REDSTONE, UPGRADES, TRANSPORT, UTILITY, CURIOSITY,
   MISC) — every classified vanilla item resolves to some template,
   guarded by `IslandSuggestionTemplateCoverageTest`. Bug fix in the
   same pass: `MATERIALS` no longer overlaps with `NATURAL` on
   `natural_resource` (was first-match-wins and NATURAL never fired).
   `MATERIAL_FAMILY` `LearnedAdjacencyKey.Kind` lets learned rules
   span shape variants of the same material (planks/log/stripped/wood
   within the same species), where item-tag adjacency couldn't. The
   stage-3 prompt was also tightened so future regenerations converge
   on `building_block` for all wood/log/stripped/stone variants. The
   debug populate generator (`RealisticAtlasGenerator`) is now
   template-keyed via `FacetIndexTemplateClassifier`: populated
   islands carry the same `defaultIslandId` / label / color that
   chip-accept would create, so test-data atlases and chip flow share
   one taxonomy. Legacy `SemanticBucket(Resolver)`, `SubBucket*`,
   `ParentKeywordRules`, `RoleSemanticBucketMap`, and
   `FacetIndexBucketClassifier` deleted. Vanilla role-corrections
   sweep (2026-04-26) patched 182 entries the LLM had inconsistently
   classified: doors / trapdoors / fence_gates → `building_block`,
   beds / decorated_pot → `decorative_block`, rails → `transport`,
   spawn_eggs → `curiosity`, compressed material blocks (Block of X)
   → `material`, mob drops + raw ores → `material`. Locked in by
   [`IslandSuggestionTemplateCoverageTest`](../../common/src/test/java/dev/imagio/slot/inventory/triage/IslandSuggestionTemplateCoverageTest.java).
6. **Modded classification layers** —
   [item-classification.md milestones 10–11](item-classification.md#milestones).
   Per-mod LLM passes for the active modset (Create, Create New Age,
   Create Dreams n Desires, etc.) so role-driven chips fire on modded
   items the same way they do on vanilla. Vanilla-only V1 leaves modded
   items chip-less.
7. **Phase 4b/c** — confidence bands + acceptance-rate logging.
   Deferred until playtest decides whether the role-only matching at
   Phase 4a is sharp enough.
6. **Storage areas** — RETIRED. The `StorageArea` domain +
   `area_proximity` / `chest_holds_relevant` contributors direction
   was wholesale superseded by the learned-storage swap; today's
   "areas" are derived clusters via `ChestClusterMap` and
   chest-content rendering goes through ghost atlas cards on homed
   islands. Historical context:
   [storage-areas.md — retired](retired/storage-areas.md). Still
   deferred from this doc as future contributors: `shopping_list`,
   per-player weights, anti-relevance, max-relevance hold-toggle.

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

- `area_proximity` / `chest_holds_relevant` — these contributors
  depended on the retired `StorageArea` domain. Today's storage
  signal is `ChestClusterMap` + ghost atlas cards on homed islands,
  which produces a different shape; if a chest-locality contributor
  is wanted in the relevance score, it'd need re-spec'ing against the
  new model. Historical context:
  [storage-areas.md — retired](retired/storage-areas.md).
- `shopping_list` — feature doesn't exist.

## Phase 3 — `FacetIndex` runtime (LANDED, V1 surface)

Owned by [item-classification.md milestones 6 + 7](item-classification.md).
The V1 slice of these milestones — thin loader + role lookup + homing
wiring — has landed. The full multi-layer / merge / inverted-index /
expression-AST shape stays deferred until a second consumer actually
demands it.

What landed:

- New package: `common/.../classification/`.
- [`FacetIndex`](../../common/src/main/java/dev/imagio/slot/classification/FacetIndex.java)
  — pure-logic loader; `FacetIndex.load(Reader)` validates
  `schema_version == 1` and the `layer` enum, then exposes
  `Optional<String> role(String itemId)`. Other facets aren't
  materialized in V1.
- [`FacetIndexBootstrap`](../../common/src/main/java/dev/imagio/slot/classification/FacetIndexBootstrap.java)
  reads `/data/slot/classification/vanilla-base.json` from the classpath,
  using `System.Logger` so the loader stays free of MC dependencies and
  is unit-testable from `:common:test`.
- Singleton in
  [`FacetIndexHolder`](../../common/src/main/java/dev/imagio/slot/classification/FacetIndexHolder.java)
  — lazy first-use init, with `install(...)` + `reset()` for tests.
- Static feature flag `FacetIndex.ENABLED` (default on). Per the
  integration plan, no config UI for V1.
- Bundled dataset shipped at
  [`common/src/main/resources/data/slot/classification/vanilla-base.json`](../../common/src/main/resources/data/slot/classification/vanilla-base.json)
  alongside the schema as `layer.schema.json`.
- [`RoleSemanticBucketMap`](../../common/src/main/java/dev/imagio/slot/classification/RoleSemanticBucketMap.java)
  maps the 19 `ROLE_VALUES` entries onto existing `SemanticBucket`
  enum values; loosely-typed roles (`utility`, `curiosity`, `transport`,
  `trophy`, `admin`) target `MISC` so the atlas materializes them as a
  miscellaneous island until a richer SLOT-side role-island taxonomy
  lands.
- [`FacetIndexBucketClassifier`](../../common/src/main/java/dev/imagio/slot/debug/FacetIndexBucketClassifier.java)
  is the homing adapter: lookup → role → bucket, with
  `SemanticBucketResolver::classify` as the no-data fallback (also
  used when `FacetIndex.ENABLED == false`).
- Wired in at
  [`SlotTestCommands.runPopulate`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/command/SlotTestCommands.java)
  — the only homing call site we own today; replaces the previous
  `SemanticBucketResolver::classify` reference.

What's still **not** done in this slice (kept deferred):

- Multi-layer support beyond `vanilla-base` (per-mod / modpack / server
  / player layers, runtime-crawl). The loader accepts any `layer` enum
  value but `FacetIndexHolder` only loads the bundled vanilla file.
- Layer-merge rules + inverted indices + expression AST.
- Other facet readers (`material_family`, `form`, `processing_in`, …).
  Phase 4 chip suggestions are the natural next consumer.

Tests landed:

- [`FacetIndexTest`](../../common/src/test/java/dev/imagio/slot/classification/FacetIndexTest.java)
  — empty index, single-value parse, ambiguous-entry first-candidate
  rule, missing-role item skip, malformed-id skip, schema-version /
  layer rejection, plus a smoke test against the bundled vanilla
  dataset (≥1000 entries; spot-checks `diamond_pickaxe → tool`,
  `diamond_sword → weapon`, `diamond_helmet → armor`,
  `cooked_beef → consumable`, `iron_ingot → material`,
  `oak_planks → building_block`, `chest → storage_block`).
- [`RoleSemanticBucketMapTest`](../../common/src/test/java/dev/imagio/slot/classification/RoleSemanticBucketMapTest.java)
  — every mapped role lands on the expected `SemanticBucket`; loosely
  typed roles fall to `MISC`; unknown / null / blank roles return empty.

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
  `chest_holds_relevant` contributors — RETIRED. Replaced by the
  learned-storage swap (auto-claim, derived `ChestClusterMap`, ghost
  atlas cards on homed islands). Historical context:
  [storage-areas.md — retired](retired/storage-areas.md). If a
  chest-locality relevance contributor is still wanted, it needs
  re-spec'ing against the chip / cluster / ghost-card model.
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
