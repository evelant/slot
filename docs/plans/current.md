# SLOT Current Implementation Plan

Last updated: 2026-04-30

Single-page entry for the active plan + queue. For the operational
handoff (project structure, working rules, verification commands),
see [../status.md](../status.md). For shipped plans, see
[`done/`](done/); for superseded designs, [`retired/`](retired/).

## Active

**Facet-driven suggestions, Phase 1**
([facet-driven-suggestions.md](facet-driven-suggestions.md)). The
suggestion engine reads only `role` + `material_family` from the
classified dataset today; the rest of the per-mod LLM data
(`mod_subsystem`, `activity`, `flavor`, `frequency`, `rarity`,
`origin`, `dye_color`, …) sits on disk but is dropped at load. Phase 1
extends `FacetIndex.ItemFacets` + the load parser so every facet our
suggestion engine could plausibly use is available at runtime
lookup. Concentrated in `FacetIndex.java` + `FacetIndexTest.java`;
~150 LOC. Phases 2–5 layer on top (subsystem-primary matching,
activity tie-break, within-island sort, trophy / frequency placement
priority).

Prereqs cleared 2026-04-30: learned-storage UX-bug pass closed and
the FacetIndex-driven populate path playtested cleanly.

## Recent landings

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

1. **Facet-driven suggestions, Phases 2–5**
   ([facet-driven-suggestions.md](facet-driven-suggestions.md)). After
   Phase 1 ships: subsystem-primary matching with min-items threshold
   (Phase 2) is the headline UX win; activity tie-break (Phase 3),
   within-island sort by material_family / flavor (Phase 4), and
   trophy / frequency placement priority (Phase 5) layer on.
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
6. **Relevance-LOD UI refinement**
   ([relevance-lod-prototype.md](relevance-lod-prototype.md)).
   Playtest-driven polish — pip readability at modded scale, atlas
   convulse on pickup, drag-drop ordinal feel.
7. **Kit prototype slice 4** ([kit-prototype.md](kit-prototype.md)).
8. **Cleanup.** Remove (or downgrade to DEBUG) the
   `[SLOT][nudge]` / `[SLOT][layout]` diagnostic INFO logging once
   one or two fresh-world opens confirm the layout converges across
   resolutions / GUI scales.

## Pointers

For product goals, see [../product/direction.md](../product/direction.md).
For current architecture, see
[../architecture/overview.md](../architecture/overview.md). For
action semantics, [../architecture/action-taxonomy.md](../architecture/action-taxonomy.md).
For the LDLib2 workspace decision,
[../decisions/0002-ldlib2-workspace.md](../decisions/0002-ldlib2-workspace.md).
For the triage / home design, [../design/atlas.md](../design/atlas.md).
For the carried-inventory fullness UI plan,
[inventory-fullness.md](inventory-fullness.md). For relevance-LOD /
storage areas history, [../design/relevance-lod.md](../design/relevance-lod.md).
