# SLOT Current Implementation Plan

Last updated: 2026-05-05

Single-page entry for the active plan + queue. For the operational
handoff (project structure, working rules, verification commands),
see [../status.md](../status.md). For shipped plans, see
[`done/`](done/); for superseded designs, [`retired/`](retired/).

## Active

No single dominant track. The list-view rewrite that ran from
2026-05-03 closed 2026-05-05 — plan archived in
[`done/list-view.md`](done/list-view.md), sidebar embed sub-plan in
[`done/list-view-phase-3a.md`](done/list-view-phase-3a.md). Pull
from the queue below.

**Recently shipped, no further plan:**

- **[`done/list-view.md`](done/list-view.md) — replace 2D atlas with
  sectioned vertical list.** Phases 1 + 2 (standalone wall + TOC tab
  strip with drag-to-reorder) landed 2026-05-03; Phases 3a.1 +
  3a.2 direction A (sidebar embed on every
  `AbstractContainerScreen`, wall → vanilla slot via
  drag/shift+click/shift+wheel) landed 2026-05-04, plus the
  layout-mode unification pass that collapsed
  `WorkspaceLayoutMode { STANDALONE, SIDEBAR }` so both surfaces
  share one widget tree at `WORKSPACE_WIDTH_PX = 414`. Closed
  2026-05-05 with **3b deferred as a separate experiment** (see
  Queue) and **3a.2 direction B / 3a.3 / 3a.4 / 3c / EMI exclusion
  area** dropped from active tracking — fresh plan if any gain
  playtest signal.
- **[`done/cursor-pickup.md`](done/cursor-pickup.md) — vanilla
  cursor semantics on wall cards. SHIPPED 2026-05-04 (all three
  phases). Closed 2026-05-05 with Phase D dropped (no playtest
  demand for press-drag-release distribute from a wall card).**
  Wall-card left-click eagerly extracts to `menu.getCarried()`
  (resolves carry → backpack → proximate chest by affinity);
  right-click is a universal cancel (returns cursor to its tracked
  origin) so eager-from-chest pickups reverse cleanly into the
  chest. Re-home is drag-only. Smart-deposit cascade
  (desired-count gap → proximate chest with affinity → home →
  Triage) handles cancel-from-host-slot and left-click-on-no-target.
  Virtual `WorkspaceCursorCarry` / `WorkspaceCursorGestures`
  retired in the same pass; "selected" is now a derived signal
  (cursor identity OR legacy `selectedAtlasIdentity`). Network
  protocol bumped 23 → 24.

`./gradlew build` green; `:common:test :neoforge:test` green.

## Recent landings

Thin log; full detail lives in `git log` and the linked archived
plans. Older entries are deleted — `git log` and `done/<plan>.md`
hold the rest.

- **2026-05-05** — backpack-first shift-click routing generalised
  via new `MenuShiftClickMixin` on `AbstractContainerMenu.clicked`
  (snapshot-diffs vanilla lanes, routes positive deltas through
  the extracted `BackpackReroute` helper); ghost-block rendering
  via `GhostItemTexture` redirecting 3D-block GUI rendering to the
  alpha-blended sheet so the 20 % alpha tint actually blends;
  carried-count badge shows "1"; status + plans cleanup pass —
  list-view + cursor-pickup closed and archived to `done/`.
- **2026-05-04** — list-view Phases 3a.1 (sidebar embed on every
  `AbstractContainerScreen`) + 3a.2 direction A (wall → vanilla
  slot via drag / shift+click / shift+wheel); cursor-pickup phases
  A/B/C (eager extract on left-click, universal cancel +
  smart-deposit, virtual cursor retired); layout-mode unification
  collapsed `WorkspaceLayoutMode { STANDALONE, SIDEBAR }` to one
  widget tree at `WORKSPACE_WIDTH_PX = 414`. Network protocol
  23 → 24. ([`done/list-view.md`](done/list-view.md),
  [`done/list-view-phase-3a.md`](done/list-view-phase-3a.md),
  [`done/cursor-pickup.md`](done/cursor-pickup.md)).
- **2026-05-03** — list-view Phases 1 + 2: single-LOD sectioned
  vertical wall with a `ScrollerView` + flow-grid sections, plus
  `TocPanelBuilder` tab strip with drag-to-reorder. 2D pan/zoom
  atlas, camera, LOD bands, and nudge layout retired
  ([`done/list-view.md`](done/list-view.md)).
- **2026-05-02** — wayfinding (4 phases: server projection +
  in-world chest glow + atlas chips + HUD edge stack), atlas-card
  status redesign (NEUTRAL / FULFILLED / STORED / MIXED / CRAFT
  unified colour + bottom-right `M/N` badge), deposit affordances
  + hover preview, top-level Gather button + universal hotkey
  ([`done/wayfinding.md`](done/wayfinding.md)).
- **2026-05-01** — split-cursor (ctrl+right pickup-half on
  hotbar / atlas / chest with virtual drop targets), full-scope
  desired counts (player-global + kit-scoped, ctrl+scroll ±1 +
  numeric entry, kit-active and desired-count cleanup protection);
  legacy collection-scoped `DesiredCount*` deleted. Playtest bug
  pass folded in alongside.
- **2026-04-30** — facet-driven suggestions Phases 1–6.6:
  `FacetIndex` consumed end-to-end by routing, ordering, learned
  rules, deposit fallback, and generator content clustering
  ([`done/facet-driven-suggestions.md`](done/facet-driven-suggestions.md));
  learned-storage UX bug pass closed (14 original + 9 follow-on).

## Known issues

Operational bugs not currently tied to a plan. Items from the
2026-05-01 cursor + desired-counts batch live under [Queue](#queue)
item 1; this section is the leftover pile.

- **Kit drag-edit doesn't auto-apply to the active belt.** Dragging
  a home onto an *active* kit's slot updates the kit definition
  but the belt isn't re-applied. Per
  [`../design/kits.md § Edit a Kit`](../design/kits.md), the edit
  should propagate immediately when the target page is the active
  page. Scoped follow-up for the next person touching kit
  drag-to-edit.

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
   2. **Ghost vs carried not differentiated enough on the hotbar
      (2D items only).** 3D-block path fixed 2026-05-05 (`GhostItemTexture`
      routes blocks through the alpha-blended sheet so the 20 % tint
      blends — applies on every surface that uses `WorkspaceUi.itemIcon`,
      hotbar included). 2D sprites already used the alpha-blended
      path; if playtest still reads them as too similar to a real
      hotbar item, the next pass needs a *visual* intervention
      (dashed outline, inset corner glyph, stronger transparency on
      the hotbar specifically) rather than a rendering-pipeline fix.
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
   - **Extend shift+click on take to auto-deposit excess** (carry-
     forward from [`done/cursor-pickup.md`](done/cursor-pickup.md) §
     Follow-up adjacent to this plan). When the player shift+clicks
     (or shift+wheels) to pull from a proximate chest and the
     resulting carried count exceeds their desired-count for the
     identity, auto-deposit the excess to a proximate chest with
     affinity (same as the smart-deposit cascade's step 2 from the
     cursor-pickup plan). Mirrors the cancel path's "satisfy desired
     count then store" rule on the take side. Reuses
     `DepositPlanner` end-to-end; no new domain.

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
   [`done/list-view.md`](done/list-view.md).* Single LOD going
   forward; the band-driven density work isn't shipping. The
   [retired/relevance-lod-prototype.md](retired/relevance-lod-prototype.md)
   plan and [../design/relevance-lod.md](../design/relevance-lod.md)
   doc are marked superseded; relevance scoring survives in reduced
   form (ordering, search filter, TOC dots).
7. **Kit prototype slice 4** ([kit-prototype.md](kit-prototype.md)).
8. **Workspace projection caching.** Surfaced 2026-05-04 while
   wiring the cross-surface drag log spam: `SlotWorkspaceViewModel`
   re-projects the full carried/proximate/elsewhere/kit-needed
   identity census on every server tick per player with the
   workspace open, regardless of whether anything changed. The sync
   layer compares serialized bytes and only sends on real change so
   network/client are cheap, but the server-side scan is wasted CPU
   in steady state. Natural cache invalidators: player inventory
   delta, chest content delta (deposit / withdraw / break), kit
   activate / deactivate / edit, player movement crossing a
   chest-proximity threshold. The `SlotDiagnostics.identityResolution`
   dedupe (content-hash of the inputs, only logs on change) is the
   diagnostic tip of this iceberg — a real cache makes the log
   stop spamming "for free" but is the smaller benefit; the bigger
   one is server CPU on populated worlds.

## Deferred experiments

Concepts the team has decided to try in isolation rather than as
phases of a larger plan. Each gets a fresh plan in `docs/plans/`
when picked up; do not extend the closed parents.

- **Hide the vanilla 36-slot player-inventory band** in container/
  machine screens (formerly Phase 3b of
  [`done/list-view.md`](done/list-view.md)). The visual reclaim is
  worth playtesting on its own, and the mod-compat surface is broad
  enough to want a focused plan: EMI `+` recipe transfer must keep
  reading vanilla inventory by *slot index* even when those slots
  aren't on screen; sorting / hotkey-move mods that bind to vanilla
  slot positions will need either a transparent shuffle through
  vanilla or explicit fail-closed behavior; hard-custom screens
  (AE2 / RS terminals not extending `AbstractContainerScreen`) need
  a graceful fallback that leaves their layout alone. Two
  techniques the parent plan considered are still the obvious
  starting points: (1) move slot positions off-screen on screen
  open, restore on close; (2) overdraw the inventory band region
  with the sidebar's wall extension. Pick during implementation
  after a coexistence study with EMI on a representative modded
  pack.

## Pointers

For product goals, see [../product/direction.md](../product/direction.md).
For current architecture, see
[../architecture/overview.md](../architecture/overview.md). For
action semantics, [../architecture/action-taxonomy.md](../architecture/action-taxonomy.md).
For the LDLib2 workspace decision,
[../decisions/0002-ldlib2-workspace.md](../decisions/0002-ldlib2-workspace.md).
For the triage / home design (canvas parts superseded by
[`done/list-view.md`](done/list-view.md)),
[../design/atlas.md](../design/atlas.md).
For the carried-inventory fullness UI plan,
[inventory-fullness.md](inventory-fullness.md). For relevance-LOD
history (retired), [../design/relevance-lod.md](../design/relevance-lod.md).
