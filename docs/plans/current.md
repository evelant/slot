# SLOT Current Implementation Plan

Last updated: 2026-05-11

Single-page entry for the active plan + queue. For the operational
handoff (project structure, working rules, verification commands),
see [../status.md](../status.md). For shipped plans, see
[`done/`](done/); for superseded designs, [`retired/`](retired/).

## Active

**[`cross-loader-refactor.md`](cross-loader-refactor.md) — add a
Minecraft 1.20.1 Forge target while keeping the modern 1.21.1
NeoForge + LDLib2 build.** ADR
[`0006`](../decisions/0006-cross-loader-legacy-forge.md) records the
platform decision. Phase 0 renderer viability is validated and the
throwaway spike source has been deleted; current work is Phase
2. The shared probe now compiles the whole common
`dev.imagio.slot` tree against Forge 1.20.1 / Java 17, backed by
`SlotStackAccess` and `SlotResourceAccess` loader seams; Forge `main`
now consumes that common tree with production Forge 1.20 platform
adapters. Phase 1 has a shared workspace action catalog/channel, packet
codec, and session/menu envelope in common; NeoForge LDLib2 RPC
registration and sends validate against it, and Forge 1.20 now has a
production `SimpleChannel` payload that decodes the same packet codec and
validates session/menu envelopes against a server-side Forge session
registry. Forge now owns a workflow runtime, projects carried player
inventory through the common `SlotWorkspaceViewModel` pipeline with
bounded auto-home, syncs that view-model to the direct Taffy/GuiGraphics
`G` screen, and routes safe metadata actions through
`SlotWorkspaceCommandService`. Forge also installs carried/world storage
accessors and binds the first guarded `TRANSFER` path for built-in
main/hotbar targets through the common executor, plus identity-to-hotbar,
hotbar-return, hotbar-to-section, kit, desired-count, chest metadata,
deposit/take, cursor, active-kit gather, and cross-surface Forge
adapters for the first belt/workflow interactions. Active-kit gather and
kit-page cycle now live in common services shared by NeoForge and Forge
transports. Forge `/slot test populate <profile>` and
`/slot test clear` are available for carried-inventory/workflow/chest testing, with
chest ids stored in Forge persistent block-entity data and claimed chest
contents feeding the common projection. Manual chest-close deposit
observation and chest-claim persistence reconciliation now use shared
helpers with loader-specific storage-id readers. Phase 2 has the main
wall section/card shells, shared fallback card details, Recents strip,
hotbar belt, active chest strip, and non-drag kit rack rendered through
the first narrow UI SPI + LDLib2/backend-specific renderers. A tested
compact storage-chip builder exists but is intentionally hidden to match
NeoForge's current product surface. Forge also has HUD/in-world
wayfinding driven by the same common view-model projection, plus hotkey
parity for vanilla inventory, active-kit page cycle, active-kit gather,
the wayfinding HUD toggle, and shared classification diagnostics /
runtime-export / rehome commands on both loaders. The next risks are
migrating remaining richer modern-only affordances without reintroducing
panel-by-panel backend semantics.

Previously active
[`single-column-workspace.md`](single-column-workspace.md) is paused
behind the cross-loader work. Do not delete it; resume when the loader
boundary no longer dominates engineering risk.

**Recently shipped, no further plan:**

- **[`done/list-view.md`](done/list-view.md) — replace 2D atlas with
  sectioned vertical list.** Closed 2026-05-05 with Phase 3b
  deferred as a separate experiment and the remaining sidebar/mod-compat
  expansion dropped pending playtest signal.
- **[`done/cursor-pickup.md`](done/cursor-pickup.md) — vanilla
  cursor semantics on wall cards.** Closed 2026-05-05 with Phase D
  dropped; eager extract, universal cancel, smart-deposit, and virtual
  cursor retirement are shipped.

Verified for the current cross-loader slice:
`./gradlew :common:test :neoforge:test :forge-1.20:test :forge-1.20:compileJava :forge-1.20:compileSharedProbeJava`.

## Recent landings

Thin log; full detail lives in `git log` and the linked archived
plans. Older entries are deleted — `git log` and `done/<plan>.md`
hold the rest.

- **2026-05-11** — classification pack-layer pass landed installed
  `mods/` scanning, jar-backed static extraction, OpenRouter-backed
  stage 3, runtime subsystem vocabulary, Forge/NeoForge runtime export,
  datapack generation, dynamic organization/subsystem auto-home cohorts,
  `/slot classification inspect` / `rehome`, and the first
  vocabulary-backed facet schema/validator contract plus
  `facet-evidence.json` assembly and pack facet vocabulary proposal;
  deposit routing was tightened back to explicit chest signals
  (learned affinity or existing matching contents).
- **2026-05-07** — Forge parity pass moved active-kit gather and
  in-world kit-page cycle into shared common services, registered
  `GATHER_ACTIVE_KIT`, wired in-screen gather through catalog actions
  on both loaders, added Forge vanilla-inventory / kit-cycle / gather /
  wayfinding-HUD keys, removed the Forge debug/SPI product labels, and
  fixed the NeoForge HUD toggle ordering bug.
- **2026-05-06** — cross-loader direction accepted via
  [`cross-loader-refactor.md`](cross-loader-refactor.md) / ADR
  [`0006`](../decisions/0006-cross-loader-legacy-forge.md): Forge
  1.20 now has the shared compile gate, action catalog transport,
  workflow persistence, session-backed projection, common wall/card/
  Recents/belt/kit/active-chest builders, sidebar host, tooltips,
  parked storage-chip builder, and HUD/world wayfinding.
- **2026-05-05** — backpack-first shift-click routing, ghost-block
  rendering, carried-count badge fix, and docs cleanup landed;
  list-view + cursor-pickup were closed and archived to `done/`.
- **2026-05-04** — list-view sidebar + wall-to-vanilla-slot paths,
  cursor-pickup A/B/C, and layout-mode unification shipped; see
  [`done/list-view.md`](done/list-view.md),
  [`done/list-view-phase-3a.md`](done/list-view-phase-3a.md), and
  [`done/cursor-pickup.md`](done/cursor-pickup.md).
- **2026-05-03** — list-view Phases 1 + 2 retired the 2D atlas in
  favour of the sectioned vertical wall and TOC tab strip.
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
3. **Classification facet vocabulary generation**
   ([classification-facet-vocabulary.md](classification-facet-vocabulary.md)).
   Slices 0-2 landed: the registry has vocabulary-backed semantic
   facets, scoped value-id grammar, layer facet validation, vocabulary
   artifact validation, parser/prompt coverage, and
   `collect-pack-facet-evidence` for runtime/static/guide/quest/
   advancement evidence plus Ponder/category lang text, KubeJS client
   tooltip mappings, stack groups, and zipped resource-pack lang overrides;
   `propose-pack-facet-vocabulary` emits accepted/review/rejected vocabulary
   artifacts with fixture/replay tests and large semantic prompts. Next
   slice is stage-3 vocabulary integration.
4. **Runtime-crawl deterministic fallback**
   ([item-classification.md § Runtime discovery](item-classification.md#runtime-discovery)).
   Walks the live registry to derive deterministic facets
   (`material_family`, `form`, `processing_in`) for mods we don't
   have LLM data for. Defer until the facet-vocabulary path lands;
   the next gap should close from richer pack semantics.
5. **Item-classification stage-4 NN priming + confidence-band
   ranking + acceptance-rate logging**
   ([item-classification.md § Integration sequence](item-classification.md#integration-sequence-next-concrete-work)
   step 6). Now that the FacetIndex-driven populate path playtests
   clean, this is the next layer of suggestion-quality work that
   sits above facet-driven-suggestions.
6. **Kit-holdout deposit + explicit withdraw verb.** Two pieces of
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
7. **Kit prototype slice 4** ([kit-prototype.md](kit-prototype.md)).
8. **Single-column workspace width pass**
   ([single-column-workspace.md](single-column-workspace.md)). Paused
   while the cross-loader/platform boundary is active. Resume once the
   Forge 1.20.1 shared compile gate and UI SPI direction are stable.
9. **Workspace projection caching.** `SlotWorkspaceViewModel`
   re-projects carried / proximate / elsewhere / kit-needed identities
   every server tick while open. Add cache invalidators for inventory
   deltas, chest content, kit changes, and chest-proximity movement;
   the main win is server CPU, with log-spam reduction as a side
   benefit.

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
