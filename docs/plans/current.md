# SLOT Current Implementation Plan

Last updated: 2026-04-29

This is the near-term engineering sequence from the current baseline. For the
short operational handoff, read [../status.md](../status.md) first.

**Active plan:** learned-storage swap landed
([learned-storage.md](learned-storage.md)). The chest-link / storage-area /
explicit-claim trio is gone; deposit routing reads `ChestAffinityMap`;
proximate-chest contents project as ghost cards on homed islands;
chest tiles → chest-chip stack docked above Triage; backpacks fill before
main inventory; `AtlasNudgeLayout` first-open deadlock fixed; ghost cards
size-match carried cards. Phases 5–8 of the plan are deferred — see the
"Learned-storage follow-ups" track below.

**Earlier landings (kept for context):** core-workflow UX pass landed (all six slices in
[core-workflow-ux.md](core-workflow-ux.md), plus playtest follow-ups
documented in [../status.md](../status.md)). Relevance-LOD prototype
through Phase 2.2 has landed (with UI polish on top). The thin
`FacetIndex` runtime + atlas-homing wiring (item-classification
milestones 6 & 7) has landed; classification-driven Triage chip
suggestions (Phase 4a) followed:

- Bundled vanilla dataset shipped at
  [`common/src/main/resources/data/slot/classification/vanilla-base.json`](../../common/src/main/resources/data/slot/classification/vanilla-base.json)
  (1536 items / 30 facets).
- Pure-logic loader / role lookup at
  [`common/.../classification/FacetIndex`](../../common/src/main/java/dev/imagio/slot/classification/FacetIndex.java)
  with the singleton in
  [`FacetIndexHolder`](../../common/src/main/java/dev/imagio/slot/classification/FacetIndexHolder.java).
  Feature flag: static `FacetIndex.ENABLED`.
- Role → `SemanticBucket` mapping in
  [`RoleSemanticBucketMap`](../../common/src/main/java/dev/imagio/slot/classification/RoleSemanticBucketMap.java);
  classifier shim at
  [`FacetIndexBucketClassifier`](../../common/src/main/java/dev/imagio/slot/debug/FacetIndexBucketClassifier.java).
- `SlotTestCommands.runPopulate` now classifies the realistic-populate
  pool through `FacetIndexBucketClassifier`, falling back to
  `SemanticBucketResolver::classify` for items the dataset doesn't cover.
- Triage chip suggestions: `IslandSignalDescriptor` carries the FacetIndex
  `role` and `material_family` populated by
  [`IslandSignalExtractor`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/triage/IslandSignalExtractor.java);
  each `IslandSuggestionTemplate` has a `roleTriggers` set and matches
  on role first, falling through to the existing class/tag signals when
  no role is available. Template enum covers the full v1 role
  taxonomy: FOOD, TOOLS, WEAPONS, ARMOR, MATERIALS, STORAGE, BUILDING,
  DECORATION, NATURAL, WORKBENCHES, MECHANISMS, REDSTONE, UPGRADES,
  TRANSPORT, UTILITY, CURIOSITY, MISC (17 templates). Bug fix in this
  pass: the `MATERIALS` template no longer claims `natural_resource`
  (NATURAL captured it but never fired due to enum-order overlap).
  Coverage smoke test
  ([`IslandSuggestionTemplateCoverageTest`](../../common/src/test/java/dev/imagio/slot/inventory/triage/IslandSuggestionTemplateCoverageTest.java))
  asserts every role-bearing entry in the bundled vanilla dataset
  routes to some template.
- Learned-rule adjacency now also keys on `MATERIAL_FAMILY` — homing
  e.g. `oak_planks` + `oak_log` + `oak_stairs` to a custom "Wood"
  island fires a learned chip for the next `oak_*` item even when its
  item-tag set has nothing in common with the previous placements.
  Tag-only adjacency couldn't span that gap; this closes it.
- Debug populate generator
  ([`RealisticAtlasGenerator`](../../common/src/main/java/dev/imagio/slot/debug/RealisticAtlasGenerator.java))
  is now template-keyed: it groups items by `IslandSuggestionTemplate`
  via [`FacetIndexTemplateClassifier`](../../common/src/main/java/dev/imagio/slot/debug/FacetIndexTemplateClassifier.java)
  and emits islands with the same `defaultIslandId` /
  `defaultLabel` / `defaultColor` a chip-accept would create — so
  accepting a chip on a populated atlas lands the item in the existing
  template island instead of duplicating it. Replaces the prior
  `SemanticBucket` + `SubBucketResolver` path; legacy
  `SemanticBucket`, `SemanticBucketResolver`, `ParentKeywordRules`,
  `SubBucket*`, `RoleSemanticBucketMap`, and `FacetIndexBucketClassifier`
  are deleted.

**Recently landed (2026-04-28):** atlas island layout rewritten as
`AtlasNudgeLayout` (push-on-grow / pull-home-on-shrink, no global
force) + manual `Shift+left-click` tighten gesture with bounded
follow-on-shrink. Both gravity-toward-origin and gravity-toward-
centroid attempts were scrapped because centroid drift made the
cluster re-shuffle on every size change. Tighten sets a sticky home
one card-row past the snap point, only along the snap axis, so
chained snaps compose per-axis. See
[atlas-nudge-layout.md](atlas-nudge-layout.md).

Next near-term tracks:

1. **Learned-storage follow-ups** ([learned-storage.md](learned-storage.md)).
   Phases 5–8 are deferred from the structural swap; pick up roughly in
   this order:
   - **Auto-claim on first deposit.** Chests currently only enter the
     workspace via `/slot test populate`. Wire a server-side observer
     on the vanilla chest GUI's slot mutations: when the player puts
     items in, call `runtime.chestClaimWorkflow().autoClaimByAnchor(...)`
     keyed on the chest BlockPos, then record affinity for each
     deposited identity via `recordDeposit(...)`. Pair with a
     **loot-chest Triage-style overlay** that surfaces the contents of
     an opened-but-unclaimed chest while the GUI is up (transient,
     dismissed on close).
   - **Search-as-find non-proximate ghosts.** When the player types a
     query that matches an item only present in a non-proximate chest,
     project a temporary ghost on its homed island with an
     "elsewhere" badge. Spacebar zoom reveals "Mountain Mine Chest #3,
     in nether". Drives "find stuff" without changing the atlas
     baseline.
   - **Kit ghost markers.** Activating a kit surfaces non-carried
     needed items as temporary ghosts on their homed islands; reach
     status flips between "in proximate chest" and "elsewhere" as the
     player walks. Reuses the existing ghost rendering pipeline with
     a new driver.
   - **Cluster derivation + rename.** Chest chips are flat right now
     (sorted proximate-first, capped at 7). Add a pure-function spatial
     clustering helper over chest world coords (~16 block threshold) so
     chips group visually under a stable cluster header, with optional
     player-authored rename. Chip panel scrolls when chips overflow.
   - **Affinity decay + accidental-placement guard.** Affinity score is
     monotonically non-decreasing. Add play-time-based decay (so an
     unused chest's old affinity stops claiming items) and a 30 s
     take-back guard (deposit + immediate take ⇒ no affinity bump).
   - **Cross-surface highlight pulses.** Hovering an atlas card should
     pulse the matching slots in proximate chests; hovering a chest
     chip should pulse the atlas cards it overlaps with. Today only
     the title-bar overlap-paint is wired. Plumbing fields
     (`hoveredAtlasIdentity`, `hoveredChestCellIdentity`,
     `hoveredIslandId`, `hoveredStorageId`) are already in
     `SlotWorkspaceUiController`.
2. **Facet-driven suggestions** — see
   [facet-driven-suggestions.md](facet-driven-suggestions.md). The
   suggestion engine and debug populate today read only `role` +
   `material_family` from the classified dataset; the rest
   (`mod_subsystem`, `activity`, `flavor`, `frequency`, `rarity`,
   `origin`, …) is on disk but unused. Five-phase plan to plumb the
   richer facets through, with subsystem-primary matching as the
   biggest UX win and trophy / frequency placement priority closing
   the loop.
3. **Modded classification layers — LANDED 2026-04-26.** Per-mod LLM
   passes for the test modset (10 mods: create,
   createaddition, createoreexcavation, dndesires, create_new_age,
   sophisticatedbackpacks, sophisticatedcore, sophisticatedstorage,
   toms_storage, plus creategoggles which has 0 items) shipped to
   [`common/.../classification/per-mod/`](../../common/src/main/resources/data/slot/classification/per-mod/)
   with a manifest at
   [`per-mod/index.json`](../../common/src/main/resources/data/slot/classification/per-mod/index.json).
   `FacetIndexBootstrap.loadAll()` now merges vanilla-base + every
   per-mod layer into the runtime singleton (~1100 modded entries on
   top of vanilla's 1500). Validator-backed retry in
   [`OpenRouterClient`](../../tools/classification/src/llm/openrouter-client.ts)
   handles upstream truncations + cache invalidation so future regens
   self-heal.
4. **Runtime-crawl as deterministic fallback** — still open
   ([item-classification.md § Runtime discovery](item-classification.md#runtime-discovery),
   milestone 8). Walks the live registry to derive deterministic
   facets for mods we don't have LLM data for. Lifts
   `material_family` / `form` / `processing_in` / etc. without
   lifting role-driven chips. Defer until facet-driven-suggestions
   plays out — the next gap might already be covered by a richer
   prompt regen rather than crawling.
5. **Playtest the FacetIndex-driven populate path and role-driven
   Triage chips.** Run `/slot test populate organized` on a fresh
   world, sample chip suggestions on Triage rows, and decide whether
   the precomputed classification feels meaningfully better before
   - stage-4 NN priming,
   - confidence-band suggestion ranking (Phase 4b),
   - acceptance-rate logging (Phase 4c).
   See [item-classification.md § Integration sequence](item-classification.md#integration-sequence-next-concrete-work)
   step 6.

Parallel tracks (deferred, available when FacetIndex stalls):

- **Storage areas rework.** Group claimed chests into player-named
  areas (Main Base, Mountain Mine, Oil Derrick, …) that default to
  chip size and expand on proximity / search / pin. Five-phase plan
  at [storage-areas.md](storage-areas.md); previously gated on the
  Phase-2 relevance-LOD playtest, which is now in.
- **Storage prototype tail.** Slice 4b (deposit with Kit holdouts)
  and Slice 5 explicit/implicit withdraw are unblocked now that Kit
  prototype slice 5 has landed. See
  [storage-prototype.md](storage-prototype.md).
- **Relevance-LOD UI refinement.** Playtest-driven polish — pip
  readability at modded scale, atlas convulse on pickup, drag-drop
  ordinal feel. See "Risks and open questions" in
  [relevance-lod-prototype.md](relevance-lod-prototype.md).
- **Kit prototype slice 4** — resume per [kit-prototype.md](kit-prototype.md).

For product goals, see [../product/direction.md](../product/direction.md).
For current architecture, see [../architecture/overview.md](../architecture/overview.md).
For action semantics, see
[../architecture/action-taxonomy.md](../architecture/action-taxonomy.md).
For the LDLib2 workspace decision, see
[../decisions/0002-ldlib2-workspace.md](../decisions/0002-ldlib2-workspace.md).
For the triage/home design, see [../design/atlas.md](../design/atlas.md).
For the carried-inventory fullness UI plan, see
[inventory-fullness.md](inventory-fullness.md).

## Current Baseline

The common inventory kernel, LDLib2 workspace transport, and first pan/zoom
player-inventory atlas proof are in place. The immediate phase is not another
kernel rewrite and not further list-screen work; it is refining the atlas into
a triage-first visual-home prototype over the same server-owned transport
boundary.

Landed baseline:

- immutable authority snapshots and exact source/entry modeling
- slot-backed and provider-entry-backed authority
- pane-scoped projections and browse documents
- workflow/activity runtime for collections, loadouts, favorites, junk,
  protection, recents, activity, and persistence-facing state
- session coordinator, command preflight, intent router, pending action state,
  and routed outcomes
- routed crafting/tool pipeline for selected-row placement, cursor placement,
  cursor drag distribution, result extraction, tool actions, and tool toggles
- host-aware canonicalization for source, quick-access, equipment, tool region,
  and outcome targets
- verb-based action taxonomy:
  `InventoryActionKind + InventoryActionQuantity + InventoryActionScope +
  InventoryActionConflictPolicy`
- common workspace composition model in `inventory/workspace`
- NeoForge observed screen/menu context
- LDLib2 player-inventory replacement menu, server-owned workspace session,
  server-bound workspace view model, and RPC transfer command path
- server-side workspace request construction from live authority
- high-signal diagnostics/logging for workspace transfers and action outcomes
- LDLib2 `GraphView` carried-atlas proof with pan, zoom, item-card selection,
  progressive disclosure, translucent workspace chrome, search/navigation
  overlay, camera preservation, and hotbar transfer behavior

## Current Risk Register

### 1. Triage/Home Loop Is Not Proven Yet

The atlas surface works as a proof of concept, but the core visual-memory loop
still needs implementation and manual proof.

Risk:

- pan/zoom and hotbar transfer can work while the home-assignment workflow still
  feels too slow, too abstract, or too easy to confuse with real inventory
  movement

### 2. Silent Auto-Homing Breaks Trust

The previous list/atlas prototypes auto-homed items by string-matching item
ids (e.g. `*_stone_* → Blocks`). In modded packs this misclassifies constantly
and teaches the player the atlas is unreliable.

Risk:

- if SLOT silently places an item in the wrong visual home, the player learns
  not to trust the atlas

Mitigation:

- no pre-created islands on a fresh map — Triage is the only starting island
- no silent auto-homing; everything starts in `Triage`
- a small set of **suggestion templates** (Food, Tools, Weapons, Armor,
  Materials, Storage) drives conservative per-Triage-card **chips**; a chip
  only acts on player tap, and a first-tap materializes the island
- template signals are class/tag/component based (e.g. `DiggerItem`,
  `DataComponents.FOOD`, `#c:ingots`), not substring matching on item ids
- beyond the template seed, suggestions come from **learned rules** built
  from the player's own manual placements (N≥2 confirmations on a shared
  tag / namespace / creative tab before a rule fires)
- learned rules dominate template chips: if a rule covers the same adjacency
  category a template would match, the template chip is suppressed for that
  adjacency

### 3. Visual Home Movement And Real Item Movement Can Be Confused

A visual home is presentation state. Moving it must not imply moving a physical
stack.

Risk:

- drag/drop and map gestures can blur the difference between organizing the
  atlas and mutating inventory

Mitigation:

- start with click-to-assign from `Triage` (chip tap or island-header click)
- keep real stack movement on explicit hotbar/action targets
- add precise drag-to-reposition only after the safer assignment flow is clear
- every home-assignment emits a structured reversible record so the future
  general undo/redo system can unwind accidental chip taps; no per-action
  timed undo toasts (those are easy to miss)

### 4. Dual-Pane And Compat Hosts Are Not Proven

Host observation and common workspace composition exist, but container,
terminal, backpack, and tool-heavy hosts are still future slices.

Risk:

- player inventory success does not automatically prove external host
  ownership, provider entries, compat tool surfaces, or EMI bounds behavior

### 5. Advanced Actions Are Vocabulary, Not Implemented Behavior

`SWAP`, `TRASH`, `VOID`, `SORT_SOURCE`, `DISTRIBUTE`, `COLLECT_MATCHING`, and
`SET_FILTER` are explicit domain vocabulary but should fail closed until their
planners/executors are implemented.

Risk:

- exposing these actions in UI before planner support would recreate the same
  semantic drift the taxonomy was meant to prevent

## Ground Rules

1. One user action gets one authoritative pipeline.
2. UI code renders server-owned state and emits narrow intents/RPC commands; it
   does not choose inventory semantics.
3. The client must not provide authoritative stack, count, identity, host id, or
   menu ref for real inventory mutations.
4. Unsupported or ambiguous integrations fail closed with diagnostics.
5. Broad actions plan against backing entries, not aggregate counts alone.
6. Crafting stays slot-backed and descriptor-driven.
7. LDLib2 owns UI transport and widget composition, not SLOT's inventory domain.
8. Home-assignment commands stay separate from real inventory mutation commands.

## Next Execution Order

### 1. Clean Up Atlas Readability

Goal:

- make the current atlas proof readable enough to evaluate item-home behavior

Deliverables:

- lower-noise or non-aliased atlas background
- tighter item-card padding and typography
- compact medium-zoom summary text
- selected-item inspector or popover for full item ids and source details
- no return to the old list-first prototype

Exit criteria:

- normal browsing and detail zoom remain readable without common labels
  ellipsizing or overflowing the screen

### 2a. Remove Legacy Auto-Categorization

Goal:

- delete the list-prototype category resolver and the neoforge string-match
  auto-home before building the new suggestion layer on top of dead code

Deliverables:

- delete `ItemCategory`, `InventoryCategoryResolver`,
  `HeuristicInventoryCategoryResolver`, `InventoryCategoryOverrides`
- drop the `category` field from `InventoryBrowseAnnotations` and the
  `InventoryBrowseGroupingMode.CATEGORY` branch from `InventoryBrowseService`
- drop `categoryResolver` from `InventoryBrowseRequest` and its callers
- gut `SlotWorkspaceAtlasLayout.defaultIslandId` / `looksLikeStarterBlock` and
  remove the pre-seeded `Blocks` starter island; fresh atlas = Triage only
- simplify `VisualHomeOrigin` to `TRIAGE` + `PLAYER_PLACED`
  (drop `HIGH_CONFIDENCE_AUTO` — chip-accepts are player-confirmed)
- update tests to match

Exit criteria:

- `:common:testClasses` and `:neoforge:testClasses` both compile clean
- zero automatic categorization exists in the codebase
- opening the atlas on a fresh profile shows no islands; every carried
  item appears in the docked Triage panel (per
  [core-workflow-ux.md slice 1](core-workflow-ux.md))

### 2b. Template Predicate Layer (Headless)

Goal:

- build the conservative seed suggestion engine without any UI wiring

Deliverables:

- common `inventory/triage/` package:
  - `IslandSuggestionTemplate` enum: `FOOD`, `TOOLS`, `WEAPONS`, `ARMOR`,
    `MATERIALS`, `STORAGE`
  - `IslandSignalDescriptor` — per-identity flag set populated platform-side
    (food component present, tool/weapon/armor class hit, matched item tags)
  - `LearnedIslandRuleStore` — in-memory adjacency → island map with
    confirmation counts and recency
  - `IslandSuggestionService` — pure function `(descriptor, learnedRules,
    islands) → List<ChipSuggestion>`; max 1 template chip, up to 2 learned
    chips, hard cap of 2 total; learned rules suppress the template chip for
    the adjacency they cover
- neoforge `IslandSignalExtractor` — reads `ItemStack` via
  `DataComponents.FOOD`, `DiggerItem`/`ArmorItem`/`SwordItem`/etc. subclass
  checks, and `#c:ingots` / `#c:chests` / `#c:shulker_boxes` / `#c:barrels`
  tag lookups; produces `IslandSignalDescriptor`
- unit tests for each template predicate and for ranking / suppression
  rules in `IslandSuggestionService`

Exit criteria:

- given an item, the module boundary produces the right chip list with no UI
  involved
- template predicates are class/tag/component based — no substring matching
  on item ids anywhere

### 2c. Chips On Triage Cards

Goal:

- surface chips on Triage cards without yet giving them any behavior

Deliverables:

- workspace view model carries `List<ChipSuggestion>` per Triage card entry
- atlas rendering shows chips with the target island's color/icon
  (template defaults if the island is not yet materialized)
- chip cards are visually distinct from the main Triage card body

Exit criteria:

- chips appear on the right Triage cards and match the service's output
- clicking a chip does nothing yet (wiring comes in 3a)

### 3a. Home Assignment + Learned Rules

Goal:

- make the player able to place an item identity and have the atlas learn
  from it

Deliverables:

- home-assignment RPC distinct from transfer RPC
- chip tap → `ASSIGN_HOME` command; creates the island (from template
  defaults) on first accept, homes the identity, preserves camera/selection
- manual assignment paths:
  - click an existing island header while a Triage card is selected →
    home there
  - click empty atlas space while a Triage card is selected → draft a new
    player island seeded by that item
- every accepted assignment (chip or manual) writes to
  `LearnedIslandRuleStore` keyed on `(shared_item_tag | shared_namespace |
  shared_creative_tab) → islandId` with a confirmation threshold of N≥2
  before a learned rule fires in the suggestion service
- camera, search query, selection, and homes survive LDLib view refreshes

Exit criteria:

- after homing two similar items manually, a third similar item picked up
  later shows a learned chip pointing at that island
- chip-accept and manual-assign paths produce identical downstream state
- home-assignment commands remain fully separate from transfer commands
- home-assignment is reversible via a structured record consumed by a
  general undo/redo system — not via per-action timed toasts

### 3b. Reversible Home-Assignment Records

Goal:

- make chip-accept and manual-assign safe enough to feel instant without
  building per-action timed undo toasts (those are easy to miss and are the
  wrong UX for this product)

Deliverables:

- every home-assignment (chip accept, manual drop, materialized template
  island) emits a structured reversible record capturing: the identity, the
  previous assignment (or absence), the target island, whether the island
  was newly created by this action, and whether that island is still at
  template-default name/color/icon
- records are stored on the workspace session in a bounded ring so a future
  general undo stack can consume them without another migration
- no UI toast and no per-action timer — surfacing these records is deferred
  to the comprehensive undo/redo system

Exit criteria:

- records round-trip through the session and carry enough information to
  unwind the home assignment and, when appropriate, delete the island that
  was materialized solely for that action
- renamed, recolored, or non-empty islands are marked as "do not delete on
  undo" in the record

### 4. Basic Island Management

Goal:

- make player-created and materialized-template islands usable enough to
  judge the visual-memory loop

Deliverables:

- island rename
- island recolor
- island icon from seed item
- visual island move without real inventory movement
- deletion only for empty player-owned and materialized-template islands

Exit criteria:

- player-authored islands are understandable without adding persistence yet
- deleting a materialized-template island leaves that template permanently
  dormant for the save (no auto-respawn on next matching pickup)

### 5. Restore Search As Spotlight

Goal:

- keep search useful while preserving stable visual homes

Deliverables:

- in-place match highlighting
- non-match dimming without layout replacement
- camera jump or cycle through matches
- optional compact result tray if spotlight is not precise enough

Exit criteria:

- search teaches where items live instead of replacing the map with a
  transient list

### 6. Persist Visual Homes And Learned Rules

Goal:

- promote the proven in-memory home and learning model into durable state

Deliverables:

- workflow-domain `VisualHomeMap` state
- persisted `LearnedIslandRuleStore`
- migration/versioning rules
- player-authored home precedence over suggestions
- materialized-template islands persist as regular islands (no special flag
  beyond "this template is no longer dormant")
- ghost homes for important absent identities

Exit criteria:

- placed homes and learned rules survive client restart/world reload
  without implying physical source authority

### 7. Later Feature Tracks

Keep these deferred until the carried triage/home loop is proven:

- dual-pane and active external source islands
- physical storage memory and find/restock trails
- equipment/offhand/cursor gestures (beyond the Belt offhand adjacency)
- recipe viewer integrations
- trash, void, sort, and recovery
- **newness indicators (`+N` delta since last open)**: per
  [atlas-prototype.md:534](atlas-prototype.md) a `+N` badge shows items
  gained since the last inventory open, persists across close/reopen, and
  is superseded when a newer batch forms (a fresh pickup after an open
  clears the prior batch). Needs: a new workflow-domain
  `NewItemsBatch` (identity → count) plus an `openedSinceBatchStart`
  flag; events `NewItemAcquired` / `NewBatchAcknowledged`; NeoForge pickup
  hook to fire acquire; workspace-open hook to fire acknowledge;
  projection threads `newCount` through `SlotWorkspaceViewModel.AtlasItem`;
  UI renders a corner pip at low zoom that expands to `+N` text at
  `READ`+. Current UI has no corner pip at all — the old origin-based
  marker was removed because it labeled every homed item indistinguishably.
- **grid-snap on in-island drops**: placement inside islands stays
  freeform (players can drop anywhere within the island; `fitIsland` grows
  the island as needed), but drops snap to the nearest grid cell by
  default so players don't have to pixel-align every card to keep islands
  tidy. Grid step is the existing card geometry
  (`CARD_WIDTH + CARD_GAP` = 36 world units on X, same on Y, offset by
  `ISLAND_CONTENT_PADDING_X` / `ISLAND_CONTENT_TOP` from the island
  origin). Apply inside
  `SlotWorkspaceAtlasLayout.placementForDrop` after the floor clamp:
  round `(localX - ISLAND_CONTENT_PADDING_X)` to the nearest `CARD_WIDTH
  + CARD_GAP` multiple, same for Y against `ISLAND_CONTENT_TOP`. Leave a
  modifier-key override (e.g. shift-drop skips snapping) for freeform
  placement when the player wants it.
- **Triage / Inbox as a docked panel, not an atlas island**: at scale
  Triage-as-island becomes a drop-target problem — it moves with the
  camera, fights pan/zoom, and gets hard to target when the atlas holds
  thousands of homes. Future direction: render Triage as a fixed UI
  panel (docked side or bottom of the workspace chrome) instead of a
  world-space `AtlasIsland`. Drag source = panel rows; drop targets =
  atlas islands (island→Triage "return to inbox" still works by drag
  onto the panel). Consequences:
  - `SlotWorkspaceAtlasLayout.ISLAND_TRIAGE` goes away as an atlas
    island; `baseIslands` no longer emits it; view-model `AtlasItem`s
    carrying `islandId == ISLAND_TRIAGE` are rerouted into a separate
    `triageItems` list on `SlotWorkspaceViewModel`
  - `applyInitialCamera` Triage-fallback branch is replaced — with an
    empty carried set the fallback should center on the last camera or
    on the player-island cluster, not on Triage
  - drop targets reshape: `installIslandDropTarget` still fires for
    player islands; a new `installTriagePanelDropTarget` handles drops
    onto the docked panel (equivalent to the current "drop on empty
    atlas background while dragged from non-Triage" path that today
    sends to `ISLAND_TRIAGE`)
  - chips still attach to Triage card entries in the panel; chip-accept
    flow is unchanged
  - tests: existing Triage-island tests in `SlotWorkspaceLdlibModelTest`
    need updating to assert `triageItems` instead of
    `AtlasItem.islandId == ISLAND_TRIAGE`
- Kits prototype: camera-anchored Belt, toggleable Kit Rack, task-shaped Kit
  concept with multi-page belt switching and bring lists. See
  [../design/kits.md](../design/kits.md) for the concept and
  [kit-prototype.md](kit-prototype.md) for the slice sequence. This
  supersedes the earlier "task boards, loadouts, and workflow rails" bullet
  and the abandoned sidebar-style collections prototype.

## Testing Priorities

Highest-value near-term coverage:

- LDLib RPC command handling and view-model refresh
- server-side request construction from authority
- exact hotbar assignment semantics
- player-bound target resolution
- action policy/protection rejection
- `IslandSuggestionService` ranking, suppression, and cap rules
- per-template predicate correctness (positive + negative samples)
- learned-rule threshold behavior (single placement does not fire; N≥2 does)
- chip-accept creates island + homes identity + records rule, in one pipeline
- manual-assign records rule identically to chip-accept
- reversible home-assignment record: captures prior assignment, whether the
  island was newly materialized, and whether that island is still at
  template-default name/color/icon (consumed later by the general undo/redo
  system)
- atlas camera/query/selection/home preservation across refresh
- session refresh and stale menu rejection
- common workspace composition output
- architecture assertions keeping LDLib2 imports out of common domain code

## Definition Of Done For This Phase

This phase is complete when:

- the LDLib atlas is the primary player-inventory workspace manually and in
  tests
- a fresh profile opens with no atlas islands; carried items appear in
  the docked Triage panel
- chip-accept and manual-assign both drive the same home-assignment pipeline
  and both feed the learned-rule store
- after the player places a few items, new similar items get learned chips
  pointing at their home islands
- hotbar assignment/transfer continues to route through the taxonomy model
- search spotlights stable homes without replacing the map
- screen/client code remains transport and presentation only
- visual-home changes remain separate from real inventory mutations
- diagnostics remain good enough to explain failed actions from logs
- external storage memory, task boards, and advanced actions remain deferred
  unless their planner/executor semantics are explicitly landed
