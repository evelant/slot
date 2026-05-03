# SLOT Current Implementation Plan

Last updated: 2026-05-03

Single-page entry for the active plan + queue. For the operational
handoff (project structure, working rules, verification commands),
see [../status.md](../status.md). For shipped plans, see
[`done/`](done/); for superseded designs, [`retired/`](retired/).

## Active

**[list-view.md](list-view.md) — replace 2D atlas with sectioned
vertical list.** Drafted 2026-05-03; Phases 1 + 2 landed 2026-05-03.

- **Phase 1 (standalone list view).** SHIPPED. The atlas surface is
  now a vertical `ScrollerView` of section blocks (one per non-Triage
  island), each section a flex-row-wrap grid of cards at a single
  fixed cell size. New `ListWallPanelBuilder` drives population;
  `AtlasCardBuilder` was rewritten as a single-LOD pixel-space
  renderer (~1670 lines → ~500). Pan/zoom + camera + LOD bands are
  gone (`AtlasCamera*`, `CameraNavigator`, `FitCarriedCamera`,
  `CameraHistory`, `BandPicker*`, `AtlasLayout`, `AtlasNudgeLayout`,
  `AtlasLayoutResult`, `WeightedGridPacker`, `Band`,
  `AtlasRenderBudget`, `SlotAtlasGraphView`, `IslandChestBuilder`,
  `AtlasPanelBuilder`, `AtlasDropResolver` all deleted). New
  `SectionOrdinal` helper provides ordinal lookups for drag-drop.
  `DragDropWiring.installSectionDropTarget` resolves drop coords to
  insert ordinals by walking the flex children in flow order. Search
  is now a filter — non-matching cards disappear and the section
  header shows `visible / total`. `WorkspaceFormat.cardChromeColor`
  dropped its `Band` parameter. Memory entries already current.
  `:common:test :neoforge:test` green; full `./gradlew build` green.
- **Phase 2 (TOC tab strip).** SHIPPED partially. New
  `TocPanelBuilder` adds a docked TOC panel to the left column
  showing each section's color swatch, label, and item count.
  Click-to-scroll wired via `wallScroller.verticalScroller.setValue`.
  Status-dot TICK plumbing in place (off-screen detection works on
  the section's element bounds). **Outstanding:** drag-to-reorder
  the TOC entries (needs a server-side reorder-island RPC).
- **Phase 3 (container-screen sidebar).** NOT STARTED. The plan's
  Phase 3a/3b/3c (sidebar alongside vanilla, hide vanilla band,
  mod-observer transparency) are multi-week work each — Phase 3c in
  particular needs iterative playtesting against modded screens
  (EMI `+`, shift-click move, hotkey transfer, sorting hooks). Pick
  up next session.

The wayfinding / atlas-card status redesign / deposit affordances /
top-level Gather plan shipped 2026-05-02. Three follow-on bugs from
the 2026-05-01 pass remain in the queue (duplicate chest, hotbar
ghost differentiation, non-stackable identity mismatch).

The wayfinding / atlas-card status redesign / deposit affordances /
top-level Gather plan shipped 2026-05-02. Three follow-on bugs from
the 2026-05-01 pass remain in the queue (duplicate chest, hotbar
ghost differentiation, non-stackable identity mismatch).

## Recent landings

**2026-05-02 — wayfinding, card status redesign, deposit preview, top-level gather.**

Plan archived in [`done/wayfinding.md`](done/wayfinding.md).

- **Wayfinding (Phases 1–4).** Per the original plan: `WayfindingTarget`
  projection on the view model, in-world chest glow on
  `RenderLevelStageEvent.AFTER_TRANSLUCENT_BLOCKS`, a shared
  `WayfindingChip` UIElement mounted in the proximity panel + chest
  locator, and a HUD edge stack rendered on `RenderGuiEvent.Post`.
  Movable-aware matching for the chest-content intersection so a
  damaged tool satisfies a pristine kit slot. New
  `key.slot.toggle_wayfinding_hud` keymap. Polish on top of the plan:
  cardinal arrows now use stroke glyphs (↑↓←→) so they don't render as
  fat triangles at the vanilla font's hinting; HUD chip widened to
  132 px with text at 0.75× pose-scale; missing-item icons drawn at
  0.625× scale so they fit inside `CHIP_HEIGHT`; no distance fade — the
  HUD chips stay informative at any range.
- **Atlas-card status redesign.** New `AtlasCardStatus` model
  (NEUTRAL / FULFILLED / STORED / MIXED / CRAFT) drives a single status
  colour shared by border + count text + progress bar. Removed the
  kit-needed star and the standalone desired-count pip; both fold into
  a unified bottom-right `M/N` badge with status-coloured digits. New
  thin border (1 logical pixel) frames cards with carried < desired,
  with kit relevance bumping saturation rather than introducing a
  separate visual axis. New progress bar across the bottom: green
  carried fill + status-coloured tail, plus a translucent stored-
  coverage band for MIXED so the bar's shape tells you "how close + how
  easy to fix" at any zoom. 1 px gap between the bottom border and
  the bar so they don't merge into one band. Cleanup pass on chrome
  sizing: every decoration now uses fixed-screen-pixel thickness (via
  `worldUnitsForPixels`) and corner pips share one
  `cornerInset(... border + 1px)` rule, killing the long-standing zoom
  inconsistency where MAX-of-(screen-px, card-fraction) for thicknesses
  fought MIN-of-(card-fraction, screen-px) for insets.
- **Deposit affordances.**
  - Server projects `Set<IdentityRef> depositableIdentities` (carried
    identities with positive direct affinity to a proximate chest);
    codec round-tripped onto every view-model push.
  - Button label is `Deposit (N)` when N > 0, plain `Deposit`
    otherwise. Visibility now requires *both* a proximate chest AND
    `N > 0` — the button hides instead of teasing a no-op click. Tooltip
    explains the affinity rule.
  - Hovering the button paints an ACCENT outline on every depositable
    atlas card via `host.depositPreviewActive` + per-card TICK
    listeners. Cards that aren't depositable skip the listener so
    there's no per-card cost during normal play.
  - Structured logging at every step of the flow: client click, RPC
    send, server receive, plan size, executor outcome, final status.
    `nothing_to_deposit` diagnostic is now a sentence and the
    `[SLOT] deposit ...` log lines fire even on no-op so the absence of
    output isn't the only signal.
- **Top-level Gather button + universal hotkey.** Replaces "open kit
  rack → click gather inside the active card" with a one-click action
  in the top-right overlay (visible when a chest is proximate and a kit
  is active). New `key.slot.gather_active_kit` keymap (UNIVERSAL,
  unbound default) covers in-world too. Both paths route through one
  `SlotGatherActiveKitPayload` → `KitGatherService.gatherActiveKit`
  which unions kit page slots (gap = 1 each) + kit-scoped desired
  counts (gap = desired - carried), ranks proximate chests by affinity,
  and pulls until each gap closes.
- **Kits panel scrolls horizontally.** `ScrollerView` wrap with
  `viewContainer` set to `FlexDirection.ROW` so a base with many kits
  no longer overflows the right edge.

`:common:test :neoforge:test` green.

**2026-05-01 — split-cursor mode and full-scope desired counts.**

Two related additions to the input vocabulary, both shipped end to end.
Design ref: [`../design/gestures.md`](../design/gestures.md).

- **Split cursor.** Virtual cursor for partial-stack moves. ctrl+right
  picks up half (cumulative on repeats); left/right/shift+right drop
  all/one/half. Pickup wired on hotbar slots and atlas cards (atlas
  card uses a server-projected `largestCarriedSlot{SourceId,Index,Count}`
  on each `AtlasItem` so it works for items in main / hotbar / offhand
  / backpack — codec round-tripped). Drop targets: hotbar slot
  (client-clamped against capacity + identity) and chest chip. ESC and
  click-on-non-target cancel via a bubble-phase root handler. Drag is
  suppressed while carrying. New RPCs: `cursorDropToHotbar`,
  `cursorDropToChest`. New helper: `DepositExecutor.depositPartialStack`.
- **Desired counts, both scopes.** New domain:
  `DesiredCountWorkflowDomainService` with `setPlayer/adjustPlayer/
  setForKit/adjustForKit/resolved/activeScope`; events
  `WorkflowEvent.{PlayerDesiredCountSet, KitDesiredCountSet}`;
  projection fields `playerDesiredCounts` and `kitDesiredCounts`;
  checkpoint persistence; kit-deletion cascade clears orphan kit-scoped
  entries. View-model surfaces resolved value + `desiredCountFromKit`
  flag on each `AtlasItem` for scope-coloured pip rendering
  (`DESIRED_COUNT_PIP_KIT` amber vs `DESIRED_COUNT_PIP_GLOBAL` blue).
  ctrl+scroll on atlas cards adjusts ±1 in the active scope; right-
  click "Set desired count…" opens a numeric entry. Drag-onto-kit-
  bring-panel writes kit-scoped count=1 (the kit "bring" list is now
  derived from kit-scoped counts > 0; legacy
  `KitDefinition.bring` field deleted along with its events / RPCs /
  command handlers / persistence). Auto-fetch on kit activation pulls
  toward kit-scoped counts from proximate chests (highest-affinity
  first). Cleanup protection extends both `KitActiveProtection`
  (kit-scoped) and a new `DesiredCountProtection` wrapper
  (player-global). Legacy collection-scoped `DesiredCountSet` /
  `setDesiredCount` / `desiredCountsByCollection` /
  `DesiredCount.java` deleted in the same change; old event tags
  decode to `null` so existing event logs replay clean.
- **Playtest bug pass landed alongside.** Right-click intercept fires
  on tracked + untracked chests (renamed
  `LootChestRightClickInterceptor` → `ChestRightClickInterceptor`).
  Kit apply fills stackables to max from carry instead of using the
  literal kit-page count (Pass 3 fill in `LoadoutApplyService`). Kit
  progress + atlas star now use `ItemIdentityMatcher.matchesMovable`
  so a damaged bow satisfies a kit slot captured with a pristine one.
  Hotbar stack-fill prefers the existing partial slot over a free
  slot (`SlotWorkspaceUiSession.assignHomeToFreeHotbar`). OFFHAND added
  as a kit-displacement fallback target so multi-backpack belts have a
  better chance of clearing.

`:common:test :neoforge:test` green.

**2026-04-30 — facet-driven suggestions Phases 1–6.** All six phases
of the facet-driven suggestion plan are in. The new pass on top of
the previous five was Phase 6 — the data-use win the user asked for.
`dye_color`, the newly-loaded `palette`, the previously-unused
`flavor`, and the previously-unused `origin` now drive a layered
within-island cluster key. Wools / carpets / concretes form a
canonical Minecraft dye-wheel inside their stem (white → light_gray →
gray → black → brown → red → orange → yellow → lime → green → cyan →
light_blue → blue → purple → magenta → pink) instead of alphabetical
chaos; non-dyed items with a `palette` tone (`wood_red`,
`copper_oxidized`, `warm`) cluster by tone; plain-id items partition
by flavor (plain → natural → variant → colored → fancy → mechanical →
mystical → ominous → ancient → unflavored), then by origin tier
(early overworld+crafted → mid overworld structures → late
nether+end → unknown → creative-only). Tests green
(`:common:test :neoforge:test`). Plan archived in
[`done/facet-driven-suggestions.md`](done/facet-driven-suggestions.md).
All facets exposed by `FacetIndex` are now consumed downstream.
Phase 6.1 extracted the comparator into
`WithinIslandOrdering` so chip-accept also uses the same cluster
ordering — new homes slot in next to their cluster peers instead of
blindly appending. Manual drag-drop still wins; insertion only fires
on the auto-append path. Phase 6.2 expanded the learned-rule store
with `SUBSYSTEM` and `DYE_COLOR` adjacency kinds so manual overrides
of the subsystem-primary template default (and color-themed
islands) are sticky after two confirmations. Phase 6.3 wired the
same adjacency-key set into `DepositPlanner` as a facet-affinity
fallback — a brand-new netherite_ingot now deposits into the
"Mining" chest (which holds iron and gold ingots) via shared
material_family / c:ingots tag adjacency, even though direct
affinity is zero. Direct affinity always outranks facet affinity.
Phase 6.4 made the debug populate generator's chest content
facet-themed: each generated chest seeds on a random linked-island
item and biases 70% of subsequent fills toward items sharing
specific (TAG / MATERIAL_FAMILY / SUBSYSTEM / DYE_COLOR) keys with
the seed, so MATERIALS chests read as "iron stuff" / "gold stuff"
rather than uniform scoops. Phase 6.5 threads a per-island
claimed-seed-keys set through `planChests` so multiple chests in
the same island prefer seeds with disjoint keys — three MATERIALS
chests now span "iron / gold / copper" rather than randomly all
landing on the same family. Phase 6.6 wired rarity into
`rollStackCount`: trophies (`role=trophy` / `rarity=unique`) and
display-only items always roll as count=1 instead of getting
template-default stacks.

**2026-04-30 — learned-storage UX bug pass + follow-on batch.**
Every original 14 bug from playtesting shipped, plus 9 follow-on bugs
from real-instance testing. Highlights:

- Right-click chest intercept now matches every claimable container
  (barrels, trapped chests, modded chest-likes), not just
  `ChestBlockEntity`.
- V hotkey is context-sensitive: opens the chest's vanilla GUI when
  a loot panel is showing, falls back to player inventory otherwise.
- Drag carried items onto the loot panel auto-claims the chest and
  deposits in one gesture (`claimAndDepositCarriedToLootChest`).
- Forget chest pushes a reversible record onto the existing undo
  stack; right-click chip pops a Rename / Forget context menu.
- Chest locator (renamed from "Search matches") docks top-left under
  the search input modal; surfaces both search hits **and**
  active-kit-needed identities.
- Carried-also-stored signal: bottom-left `+N stored` badge on
  carried + ghost cards under search.
- Kit activation stages out occupants of slots whose needed item
  isn't in carry, paints faded ghost preview of the kit-declared
  item on empty hotbar slots, and chest takes auto-snap picked items
  into kit slots via `reapplyActiveKitFromCarry`.
- Gather button actually pulls reachable identities from proximate
  chests in a single click (no more silent pan-to-home tour mode).
- Initial-open island overlap fixed: `AtlasNudgeLayout` Phase 3
  chain-resolve now uses monotonic-rightward push so it can't
  oscillate between obstacles on opposite sides.
- Sophisticated Backpacks chest-slot / Curios-slot inventory now
  reachable via a slot-category-aware
  `SophisticatedBackpackSupport.findCarrierByStableId`.
- Four left-column panels (search results, chip stack, loot, Triage)
  compose into a single `LeftColumnBuilder` flex column instead of
  the prior `reservedHeight()` chain.

Compile clean, `:common:test :neoforge:test` green.

## Queue

Roughly ordered by playtest signal. Pull from the top when the active
track lands.

1. **Cursor + desired-counts playtest bug pass — remainder.** Three
   items still open from the original 2026-05-01 batch (the wayfinding
   nav and kit-carry want-vs-have items shipped 2026-05-02 and are
   gone from this list). Likely best taken as one batch since #1 + #3
   share root causes and #4 unblocks the rest.

   1. **Duplicate chest in proximate panel + chest-locator panel.**
      A nearby chest holding a kit-needed item appears in both
      sections. Decide which surface owns "proximate + kit-needed"
      (chest locator already shows kit-needed identities under
      search; proximate panel shouldn't double up) or render a single
      visual hint that the chest covers both intents.
   2. **Ghost vs carried not differentiated enough on the hotbar.**
      The faded kit-needed ghost preview reads too similar to a real
      hotbar item; players miss "this slot is empty / waiting." Atlas
      cards got a status-redesign treatment 2026-05-02; the hotbar
      didn't. Try stronger transparency, a dashed outline, or an
      inset corner glyph on the hotbar specifically.
   3. **Multi-chest / non-stackable identity bug.** Specific repro:
      kit needs `bucket_of_water`, a proximate chest contains one,
      and the atlas ends up with **two** `bucket_of_water` cards —
      one with the desired-count star but no chest-stock pip, one
      with the chest-stock pip but no desired star. Chest locator
      lists two chests for the identity (one proximate, one not).
      Kit progress still says "need 1." Strongly suggests the
      proximate-chest ghost projection produces a parallel identity
      key for non-stackables that doesn't `equals()` the kit-page
      identity. Likely culprits: `ElsewhereGhostProjection`,
      ghost-accumulator merge logic in
      `SlotWorkspaceViewModel.build`, or the chest-locator query.
      Wayfinding's `WayfindingTarget` projection sidesteps this with
      `ItemIdentityMatcher.matchesMovable` end-to-end; the older atlas
      paths still don't.
   4. **More debug logging.** The deposit pipeline got end-to-end
      structured logging on 2026-05-02. The kit-need / chest-presence /
      identity-resolution paths are still sparse, making bugs like
      #3 hard to triage from screenshots alone. Add structured
      INFO/DEBUG at: identity creation per chest enumeration,
      chest-locator query (which identities matched and via which
      equality path), kit-needed projection (input identities +
      carried set + final needed set), proximate vs elsewhere
      classification. Prefer `SlotDiagnostics` / `SlotDebugLog` over
      raw `LOGGER.info` so the pattern stays consistent.

   Cursor / desired-counts polish that was deferred from the
   2026-05-01 ship and could be folded into this pass if convenient
   (each documented in [`../design/gestures.md`](../design/gestures.md)):

   - Atlas card *drop* (cursor → "send to home").
   - Chest-drop overflow tracking (return-count from RPC).
   - Origin-slot highlight while cursor is non-empty.
   - "Need N more" status text on the desired-count pip — partially
     subsumed by 2026-05-02's unified `M/N` badge with status-coloured
     digits, which already communicates the gap; explicit "need N more"
     text would still be a more direct read.
   - Right-click "Set desired count…" kit-vs-global toggle.

   Wayfinding follow-ups (each minor; defer until playtest signals
   demand):

   - The deposit-preview server projection only checks direct affinity,
     not the facet-affinity fallback the planner uses, so items that
     deposit via facet similarity won't light up in the preview. Tighten
     by walking the same `LearnedAdjacencyKey` set the planner uses.
   - The `[SLOT] deposit ...` log lines fire on every click. Throttle
     once the deposit UX is stable — currently they're useful for
     bug triage but will eventually be log-spam.

2. **Learned-storage residual polish**
   ([learned-storage.md](learned-storage.md)). Sticky cluster
   ordinals across split / merge (today, single-chest churn keeps
   chips stable but multi-chest topology changes can renumber
   labels); per-row "→ suggested home" preview on the loot-chest
   panel; atlas-deposit take-back guard (only revisit if playtest
   shows stuck affinity).
3. **Runtime-crawl deterministic fallback**
   ([item-classification.md § Runtime discovery](item-classification.md#runtime-discovery)).
   Walks the live registry to derive deterministic facets
   (`material_family`, `form`, `processing_in`) for mods we don't
   have LLM data for. Defer until facet-driven-suggestions plays out
   — the next gap might already close from a richer prompt regen.
4. **Item-classification stage-4 NN priming + confidence-band
   ranking + acceptance-rate logging**
   ([item-classification.md § Integration sequence](item-classification.md#integration-sequence-next-concrete-work)
   step 6). Now that the FacetIndex-driven populate path playtests
   clean, this is the next layer of suggestion-quality work that
   sits above facet-driven-suggestions.
5. **Kit-holdout deposit + explicit withdraw verb.** Two pieces of
   open work that the retired storage-prototype plan tracked under
   Slices 4b / 5; they need re-planning against the current chip /
   affinity model.
   - *Kit-holdout deposit:* when a Kit is active, the deposit verb
     should skip identities the Kit declares as bring-list members
     (so a deposit pass doesn't strip Kit-critical items into the
     nearest chip). The Kit's protection flags exist; the gate just
     needs wiring into `DepositPlanner` / `DepositExecutor`.
   - *Explicit withdraw:* the gather button (kit rack) already pulls
     reachable Kit-needed identities from proximate chests in one
     click. A general-purpose withdraw verb (independent of an active
     Kit) hasn't been planned. Defer until playtest signals demand.
6. **Relevance-LOD UI refinement** — *retired by
   [list-view.md](list-view.md).* Single LOD going forward; the
   band-driven density work isn't shipping. The
   [retired/relevance-lod-prototype.md](retired/relevance-lod-prototype.md)
   plan and [../design/relevance-lod.md](../design/relevance-lod.md)
   doc are marked superseded; relevance scoring survives in reduced
   form (ordering, search filter, TOC dots).
7. **Kit prototype slice 4** ([kit-prototype.md](kit-prototype.md)).
8. **Cleanup.** Remove (or downgrade to DEBUG) the
   `[SLOT][nudge]` / `[SLOT][layout]` diagnostic INFO logging — moot
   once Phase 1 of the list view lands and the nudge layout retires.

## Pointers

For product goals, see [../product/direction.md](../product/direction.md).
For current architecture, see
[../architecture/overview.md](../architecture/overview.md). For
action semantics, [../architecture/action-taxonomy.md](../architecture/action-taxonomy.md).
For the LDLib2 workspace decision,
[../decisions/0002-ldlib2-workspace.md](../decisions/0002-ldlib2-workspace.md).
For the triage / home design (canvas parts superseded by
[list-view.md](list-view.md)), [../design/atlas.md](../design/atlas.md).
For the carried-inventory fullness UI plan,
[inventory-fullness.md](inventory-fullness.md). For relevance-LOD
history (retired), [../design/relevance-lod.md](../design/relevance-lod.md).
