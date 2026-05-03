# SLOT Project Status

Last updated: 2026-05-03. Operational handoff. Read after
[../README.md](../README.md). For active work + queue see
[plans/current.md](plans/current.md); for architecture see
[architecture/overview.md](architecture/overview.md).

## Active

**[plans/list-view.md](plans/list-view.md) — Phases 1 + 2 shipped
2026-05-03**. The 2D pan/zoom atlas is gone; the wall is now a
single-LOD sectioned vertical scroll list (`ListWallPanelBuilder`,
`AtlasCardBuilder` rewritten) with a docked TOC tab strip on the
left (`TocPanelBuilder`). Camera, layout, LOD-band, and nudge code
all deleted (`AtlasCamera*`, `AtlasLayout`, `AtlasNudgeLayout`,
`BandPicker`, `Band`, `AtlasRenderBudget`, `SlotAtlasGraphView`,
`AtlasPanelBuilder`, `IslandChestBuilder`, `AtlasDropResolver`,
`FitCarriedCamera`, `CameraHistory`, `CameraNavigator`,
`WeightedGridPacker`). `SectionOrdinal` replaces `AtlasDropResolver`'s
projection-only static helpers. Drag-drop is now section-aware via
`DragDropWiring.installSectionDropTarget` resolving drop coords to
ordinals from flex children. Search is a filter (hides non-matching
cards). TOC has click-to-scroll, off-screen status dots, and
deposit/gather hover-preview overlays.

**Outstanding on list-view:**

- Phase 3a/b/c (container-screen sidebar, hide vanilla band,
  mod-observer transparency). A skeleton hook exists at
  `neoforge/.../client/screen/SlotContainerSidebar.java` —
  diagnostic-logs only. Real mount logic + cross-surface drag
  routing not started; needs design pass first.

(Wall renders empty sections as zero-height now;
`TocPanelBuilder` rows are drag sources for section reorder,
backed by the new `WorkflowEvent.VisualIslandReordered`.
`SlotWorkspaceAtlasLayout.baseIslands` switched from a
`(y, x, label)` sort to `playerIslands` list order, so the
projection's list ordering is now the wall's display order.)

Pickup priorities for the next session are at the top of
[plans/current.md § Next session](plans/current.md#next-session--list-view-follow-up).

Wayfinding (full plan), atlas-card status redesign, deposit-preview
highlighting, and a top-level Gather button shipped 2026-05-02.

**Recent landings (2026-05-02):**

- **Wayfinding (Phases 1–4) shipped end to end.** Plan archived in
  [plans/done/wayfinding.md](plans/done/wayfinding.md).
  - Phase 1: server projects `WayfindingTarget` per claimed chest
    holding a kit-needed or unmet desired-count identity, with KIT vs
    PLAYER scope, codec round-tripped, tests green.
  - Phase 2: `WayfindingChestGlowRenderer` paints a wireframe AABB on
    each in-current-dimension target on
    `RenderLevelStageEvent.AFTER_TRANSLUCENT_BLOCKS`. Color tracks scope
    (amber kit / blue player), alpha pulses + falls off with distance +
    line-of-sight (capped 64-block radius / 32-block trace).
  - Phase 3: shared `WayfindingChip` UIElement component drives the
    proximity panel + chest locator inside the atlas — name + cluster +
    missing-icon strip + 8-way compass arrow + distance, with
    cross-dimension swap to dim-shorthand + coords.
  - Phase 4: `WayfindingHudRenderer` renders the chip stack along the
    HUD right edge while no GUI is open. Top-5 sorted by distance plus a
    cross-dimension list. New `key.slot.toggle_wayfinding_hud` keymap.
    Polish: cardinal arrows now stroke glyphs (↑↓←→) instead of fat
    triangles; HUD chip widened to 132px and text rendered at 0.75×
    pose-scale to fit comfortably; missing-item icons render at 0.625×
    so they fit inside the chip; no distance fade — chips stay visible
    at any range.
- **Atlas-card status redesign** (expanded scope from the wayfinding
  pass — surfaced naturally once the pip-recolor brainstorm collided
  with the existing kit-star + desired-count pip duplication). New
  `AtlasCardStatus` model — NEUTRAL / FULFILLED / STORED / MIXED /
  CRAFT — drives a single status colour shared by border, count text,
  and progress bar so the player learns one vocabulary.
  - Removed kit-needed star + standalone desired-count pip; both fold
    into a unified bottom-right `M/N` badge with status-coloured digits.
  - Card border lights when carried < desired; thickness pinned to 1
    logical pixel (was MAX of 2px-screen / 4%-card, which scaled too
    aggressively at zoom-in and overlapped the badge).
  - Progress bar across the bottom of the card: green carried fill +
    status-coloured tail, with a translucent stored-coverage band
    overlaying the gap when MIXED — bar shape + palette together answer
    "how close + how easy to fix" at any zoom.
  - 1px gap between bottom border and bar so they don't merge into one
    fat band when the colour matches.
  - All chrome thicknesses + insets now use fixed screen pixels via
    `worldUnitsForPixels()`; pip footprints stay card-relative. Fixed a
    long-standing inconsistency where decoration sizes diverged across
    zoom because `MAX(screen-floor, card-fraction)` for thicknesses
    fought `MIN(card-fraction, screen-cap)` for insets.
  - Kit relevance modulates colour saturation rather than introducing a
    separate axis (kit-relevant CRAFT = vivid red; player-only CRAFT =
    muted red).
- **Deposit affordances.** The deposit button now shows count + previews
  what would happen.
  - Server projects `Set<IdentityRef> depositableIdentities` onto the
    view model — carried identities with positive direct affinity to a
    proximate chest. Codec round-tripped.
  - Button label is `Deposit (N)` when N > 0, plain `Deposit` otherwise;
    visibility now requires both a proximate chest *and* `N > 0`, so the
    button hides instead of teasing a no-op click.
  - Hovering the deposit button paints an ACCENT-colour outline on every
    matching atlas card via `host.depositPreviewActive` + a per-card
    TICK listener — player sees which stacks would route before
    clicking. Cards that aren't depositable skip the listener.
  - Added structured logging at every step of the deposit flow: client
    click, RPC send, server receive, plan size, executor outcome, final
    status. `nothing_to_deposit` diagnostic is now a sentence ("no
    carried stack matches a chest's affinity — drop one in manually
    first") and tooltip on the button explains the affinity-driven rule.
- **Top-level Gather button + universal hotkey.** Replaces "open kits
  panel → click gather inside the active card" with a one-click action
  in the top-right overlay (visible when a chest is proximate and a kit
  is active). New `key.slot.gather_active_kit` keymap (UNIVERSAL,
  unbound default) covers in-world too; both paths route through one
  packet → `KitGatherService.gatherActiveKit(player)` which unions kit
  page slots + kit-scoped desired counts, ranks proximate chests by
  affinity, and pulls until each gap closes.
- **Kits panel scrolls horizontally.** `ScrollerView` wrap with
  `viewContainer` set to `FlexDirection.ROW` so a base with many kits no
  longer overflows the right edge.

**Recent landings (2026-05-01):**

- **Split-cursor (#5 from playtest list).** Virtual cursor for
  partial-stack moves: ctrl+right-click picks up half (cumulative on
  repeats), left/right/shift+right drops all/one/half on hotbar slots
  and chest chips. Atlas card pickup uses a server-projected
  `largestCarriedSlotIndex` so it works for items in main / hotbar /
  offhand / backpack. ESC and click-on-non-target cancel. Hotbar drops
  client-clamped against capacity + identity so the cursor stays in
  sync. Drag suppressed while carrying. Design ref:
  [design/gestures.md § 1](design/gestures.md).
- **Desired counts (#6 from playtest list), full scope.** Player-global
  + kit-scoped standing-order intents. Resolution rule: kit-scoped wins
  while a kit is active and has a non-zero entry, else fall back to
  player-global. ctrl+scroll on atlas card adjusts active scope ±1.
  Right-click "Set desired count…" opens a numeric entry. Pip in the
  bottom-right of the atlas card; colour reflects scope (kit-scoped
  amber vs player-global blue). Persistence via
  `WorkflowEvent.{PlayerDesiredCountSet, KitDesiredCountSet}` and
  `WorkflowCheckpointData.{playerDesiredCounts, kitDesiredCounts}`.
  Auto-fetch: kit activation pulls toward kit-scoped counts from
  proximate chests (highest-affinity first). Cleanup protection:
  identities with desired count > 0 are protected from
  TRASH/VOID/DROP_TO_WORLD via `DesiredCountProtection` (player-global)
  and the extended `KitActiveProtection` (kit-scoped). Legacy kit
  "bring" list merged into kit-scoped desired counts (drag-onto-bring
  writes count=1; the view-model still surfaces a `bring` list on each
  KitCard, populated from kit-scoped counts > 0). Legacy
  collection-scoped `WorkflowEvent.DesiredCountSet`,
  `CollectionWorkflowDomainService.setDesiredCount`,
  `CollectionProjection.desiredCountsByCollection`, and the orphaned
  `DesiredCount.java` record were deleted in the same change. Design
  ref: [design/gestures.md § 2](design/gestures.md).
- **Playtest bug pass.** Right-click intercept now fires on tracked +
  untracked chests (was unclaimed-only). Kit apply fills stackables to
  max from carry instead of literal kit count. Kit progress / atlas
  star use movable-aware identity matching so a damaged bow satisfies a
  pristine kit slot. Hotbar stack-fill prefers the existing partial
  stack over a new free slot. OFFHAND added as a kit-displacement
  fallback target. Renamed `LootChestRightClickInterceptor` to
  `ChestRightClickInterceptor`.

**Recent landings (2026-04-30):**

- Facet-driven suggestions Phases 1–6.6 shipped. Phase 6 gives the
  within-island comparator a layered cluster key — dyed items sort
  as a canonical Minecraft dye-wheel inside their stem,
  palette-toned items cluster by tone (split by flavor → origin
  within tone), plain-id items partition by flavor (plain → natural →
  variant → colored → fancy → mechanical → mystical → ominous →
  ancient → unflavored) then by origin tier before id-alpha. Phase
  6.1 extracted the comparator into `WithinIslandOrdering` and wired
  the live chip-accept placement path to use it — chip-accepted
  homes slot in next to their cluster peers instead of being
  appended at the end. Phase 6.3 wired the shared
  `LearnedAdjacencyKey.keysFor` into `DepositPlanner` as a
  facet-affinity fallback — chests with no direct identity bond but
  with bonds to facet-similar identities now become deposit
  candidates (ranked below direct-affinity chests). Phase 6.4 made
  the debug populate generator's chest contents facet-themed: each
  generated chest seeds on a random linked-island item and biases
  fills toward seed-similar items via the priority-rank-0 keys
  (TAG / MATERIAL_FAMILY / SUBSYSTEM / DYE_COLOR), so a populated
  MATERIALS section reads as "iron chest / gold chest / copper
  chest" rather than uniform scoops. Phase 6.5 layered cross-chest
  seed diversity on top: a per-island claimed-seed-keys set
  threaded through `planChests` makes subsequent chests in the same
  island prefer seeds with disjoint keys, so the three families
  span across the chests instead of re-rolling onto the same one.
  Phase 6.6 wired rarity into `rollStackCount`: trophies
  (`role=trophy` / `rarity=unique`) and display-only items always
  roll as count=1, so a `nether_star` no longer appears as a stack
  of 5 in a populated chest. Phase 6.2 added
  `SUBSYSTEM` and `DYE_COLOR`
  to the learned-rule adjacency kinds so manual placement overrides
  of the subsystem-primary default (and color-themed islands) become
  sticky after two confirmations. Every facet `FacetIndex` exposes
  is now consumed downstream by routing, ordering, learning,
  deposit fallback, or generator content clustering.
  Plan archived in
  [plans/done/facet-driven-suggestions.md](plans/done/facet-driven-suggestions.md).
- Learned-storage UX-bug pass closed: 14 original bugs + 9 follow-on
  bugs from real-instance testing all shipped. Recap lives in
  [plans/current.md](plans/current.md); the canonical design ref is
  [plans/learned-storage.md](plans/learned-storage.md).
- FacetIndex-driven populate path playtested cleanly — that's what
  unblocked the facet-driven suggestions work.

## Small known bugs

Carry-over from the 2026-05-01 cursor + desired-counts pass; some
were addressed by the 2026-05-02 wayfinding + status-redesign work,
remainders sit at the top of
[plans/current.md § Queue](plans/current.md#queue). Headlines:

- **Duplicate chest in proximate + chest-locator panels.** A nearby
  chest containing a kit-needed item shows up in both sections. Still
  open — wayfinding chip unification didn't change which surface owns
  the entry.
- **Ghost vs carried not differentiated enough on the hotbar.** When
  a kit slot is empty the ghost preview reads too similar to a real
  item. Atlas-card status redesign covered the *atlas* read; the
  hotbar treatment is still pending.
- **Multi-chest / non-stackable identity mismatch.** Active kit needs
  `bucket_of_water`; a proximate chest contains one but a *second*
  `bucket_of_water` shows up on the atlas without the desired-count
  star (and with a stored-pip the original card lacks). Wayfinding's
  movable-aware match handles its own intersection but the underlying
  AtlasItem accumulator path is unchanged. Likely the
  proximate-chest ghost projection still creates a parallel identity
  for non-stackables.
- **Debug logging coverage is uneven.** Deposit pipeline now has end-
  to-end structured logging. The kit-need / chest-presence /
  identity-resolution paths are still sparse, making bugs like the
  bucket-of-water repro hard to triage from screenshots alone.

Resolved by the 2026-05-02 work (no longer a known bug):

- ~~Navigation to chests with kit-needed items~~ — wayfinding glow +
  HUD chip + atlas chip cover the read.
- ~~Kit "carry" section has no want-vs-have indicator~~ — unified
  `M/N` badge on every desired-count atlas card; kit-rack carry rows
  read off the same projection.

Older standing items:

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

Cursor / desired-counts polish that was deferred (also in the queue):

- Atlas card *drop* (cursor → "send to home") not wired; clicking an
  atlas card while carrying just cancels the cursor.
- Chest-drop overflow tracking — hotbar drops are clamped client-side,
  chest drops aren't (server clamps but doesn't return moved count).
- Origin slot doesn't visually highlight while the cursor is non-empty.
- "Need N more" status text on the desired-count pip — pip shows the
  target only, not the gap.
- Right-click "Set desired count…" only edits the active scope; an
  explicit kit-vs-global toggle in the menu would help when both
  scopes are in play.

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
