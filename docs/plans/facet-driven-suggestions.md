# Facet-Driven Suggestions — Implementation Plan

Last updated: 2026-04-30 (active track for next session, prereqs cleared)

> **Pre-flight (2026-04-30):** the learned-storage bug pass closed
> 2026-04-30 (every claim-flow / chip / panel / kit bug shipped — see
> [current.md](current.md) recap). The FacetIndex-driven populate path
> playtested cleanly on the same day, so the data on disk is producing
> usable triage chips today and we're ready to enrich them. **Start
> here:** Phase 1 of this doc — extend `FacetIndex.ItemFacets` + the
> load parser. Code is concentrated in `FacetIndex.java` and
> `FacetIndexTest.java`; nothing else needs to change to validate the
> shape. Once Phase 1 lands, run a quick subsystem histogram across
> the bundled per-mod files (described in *Notes for the next session*
> below) before deciding the Phase 2 threshold.

## Why

The core SLOT goal is **reducing inventory friction and tedium**. The
ideal end state is that triage suggestions are good enough that users
*mostly don't need to place things manually* — accept the chip and
move on. Today the suggestion engine reads only two facets from the
classified dataset:

- `role` (single value)
- `material_family` (single value)

…out of the ~30 facets each item carries
([common/.../classification/FacetIndex.java:79-84](../../common/src/main/java/dev/imagio/slot/classification/FacetIndex.java#L79-L84)).
The rest of the data — `mod_subsystem`, `activity`, `flavor`,
`frequency`, `rarity`, `origin`, `dye_color`, `palette`,
`primary_uses`, `processing_in`, `form`, `environmental_property`, … —
sits on disk in
[`per-mod/`](../../common/src/main/resources/data/slot/classification/per-mod/)
and [`vanilla-base.json`](../../common/src/main/resources/data/slot/classification/vanilla-base.json)
but is never read at runtime. This plan plumbs the richer facets all
the way through to suggestions and the debug atlas generator so
suggestion quality matches the data we already classify.

A second motivation: the debug `RealisticAtlasGenerator` and the live
Triage suggestion path share the *template-matching* half of the
algorithm but diverge on the *grouping* half. Subsystem-primary
matching collapses that gap — a debug-populated atlas will look like
what a player would build naturally, which is the test we actually
care about.

For background:

- [item-classification.md](item-classification.md) — how the dataset
  is generated.
- [relevance-lod.md](../design/relevance-lod.md) — the broader
  scoring model that placement priority will eventually feed into.
- [current.md](current.md) — the cross-plan ordering.

## Sequence

1. **Phase 1** — Plumb richer facets into `FacetIndex`.
2. **Phase 2** — Subsystem-primary matching, with a
   minimum-items-per-subsystem threshold so singleton subsystems fold
   back into their parent template.
3. **Phase 3** — `activity` as a tie-breaker for role disambiguation
   and overlapping-template cases.
4. **Phase 4** — Implicit within-island sort by `material_family` →
   `flavor` → id (no UI subgrouping; just ordering).
5. **Phase 5** — `rarity` / `frequency` for placement priority and the
   trophy → CURIOSITY shunt.

Phases 1–2 are the foundation; 3–4 are quick layered wins; 5 closes
the loop with the visual / spatial polish that matches how players
actually organize.

---

## Phase 1 — Plumb richer facets into `FacetIndex`

**Goal.** Make every facet our suggestion engine could plausibly use
available at the runtime lookup. Today only `role` and
`material_family` survive the load step — everything else is dropped
on the floor.

**Files.**

- [`common/.../classification/FacetIndex.java`](../../common/src/main/java/dev/imagio/slot/classification/FacetIndex.java)
  — extend the `ItemFacets` record, the `load()` parser, and the
  public accessors.
- [`common/.../test/.../FacetIndexTest.java`](../../common/src/test/java/dev/imagio/slot/classification/FacetIndexTest.java)
  — add multi-value parse tests.

**Concrete changes.**

1. Extend `ItemFacets` to hold:
   - `role: String?` (existing, single-value)
   - `materialFamily: String?` (existing, single-value)
   - `subsystems: List<String>` (new, multi-value — `mod_subsystem`)
   - `activities: List<String>` (new, multi-value — `activity`)
   - `flavor: String?` (new, single-value with multi-fallback)
   - `frequency: String?` (new, enum: `ubiquitous` / `common` /
     `occasional` / `rare` / `never`)
   - `rarity: String?` (new, enum: `mundane` / `uncommon` / `rare` /
     `trophy`)
   - `origin: String?` (new — `crafted_only` / `mob_drop` / `nether` /
     `end` / `overworld_surface` / `overworld_underground` / etc.)
   - `dyeColor: String?` (new, single-value)
2. Add a generic single-value reader and a multi-value-list reader
   (current `readSingleStringFacet` is single-only). Both must handle
   the canonical facet shape: `{value: "x"}` *or* `{values: [...]}`
   *or* the ambiguous `{values: [...], ambiguous: true}` form. For
   facets that are conceptually single but the LLM sometimes returned
   a list (e.g. `role` for `track_signal`), prefer the first list
   entry.
3. Extend `FacetIndex` with public accessors: `subsystems(itemId)`,
   `activities(itemId)`, `flavor(itemId)`, `frequency(itemId)`,
   `rarity(itemId)`, `origin(itemId)`, `dyeColor(itemId)`. Existing
   `role()` / `materialFamily()` keep their signatures.
4. The merge path (`mergedWith`) already handles whole-item
   replacement; it doesn't need changes because we replace the
   `ItemFacets` record wholesale.
5. Items with no role *and* no material_family but at least one of
   the new facets should still be indexed (today they're skipped — see
   [FacetIndex.java:81-84](../../common/src/main/java/dev/imagio/slot/classification/FacetIndex.java#L81-L84)).
   Adjust the index inclusion predicate accordingly.

**Acceptance.**

- `FacetIndexBootstrap.loadAll()` produces an index where modded items
  resolve all the new accessors (e.g. `create:cogwheel` →
  `subsystems = ["create:mechanical_power"]`).
- New tests cover: single-value parse, multi-value parse, missing
  facet returns empty, both-formats-present (prefer `value`), and the
  facet-only-on-new-fields inclusion case.

**Estimated size.** ~150 LOC across `FacetIndex` + tests.

---

## Phase 2 — Subsystem-primary matching with min-items threshold

**Goal.** When a modded item carries `mod_subsystem`, suggest a
subsystem-named island ("Create — Mechanical Power") instead of the
generic role-based template ("MECHANISMS"). Match how players
actually organize a Create + Sophisticated modpack: by *what the item
is for in the mod*, not by which broad MC verb fits.

**Files.**

- [`common/.../inventory/triage/IslandSignalDescriptor.java`](../../common/src/main/java/dev/imagio/slot/inventory/triage/IslandSignalDescriptor.java)
- [`common/.../inventory/triage/IslandSuggestionTemplate.java`](../../common/src/main/java/dev/imagio/slot/inventory/triage/IslandSuggestionTemplate.java)
- [`neoforge/.../triage/IslandSignalExtractor.java`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/triage/IslandSignalExtractor.java)
- [`common/.../debug/RealisticAtlasGenerator.java`](../../common/src/main/java/dev/imagio/slot/debug/RealisticAtlasGenerator.java)
- [`neoforge/.../screen/ldlib/AtlasPanelBuilder.java`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/AtlasPanelBuilder.java)
  — only for the display label format.

**Concrete changes.**

1. `IslandSignalDescriptor` gets new optional fields: `subsystems`
   (List), `activities` (List), `flavor`, `frequency`, `rarity`,
   `origin`. Builder updated.
2. `IslandSignalExtractor.extract` populates the new fields from
   `FacetIndexHolder`. No new I/O; same singleton.
3. `IslandSuggestionTemplate.firstMatch` (and the `firstMatchOrMisc`
   variant used by generate) gain a **subsystem-first** branch:
   - If `descriptor.subsystems` is non-empty AND the global subsystem
     histogram says this subsystem is "big enough" (see threshold
     below), return a synthetic `SubsystemTemplate` with:
     - `defaultIslandId = "subsystem:" + subsystemId`
     - `displayLabel = formatSubsystemLabel(subsystemId)` —
       e.g. `create:mechanical_power` →
       `"Create — Mechanical Power"`
     - `colorHint` = derived from the parent template (so all
       Create-mechanism subsystems share a palette)
   - Else fall through to the existing role / class / tag matching.
4. **Minimum-items threshold.** `RealisticAtlasGenerator.generate` (and
   the live triage path) maintain a histogram of subsystem occurrences
   across the items currently being processed. A subsystem only
   "qualifies" as its own island when it has ≥ `MIN_SUBSYSTEM_ITEMS`
   items present (default **4**, configurable). Items in too-small
   subsystems fall back to template matching. This avoids producing
   1-item or 2-item subsystem islands that just add navigation
   overhead. For triage chips, the histogram is over the *current
   atlas state* — accept-the-chip places into an existing
   subsystem-island when one exists, into the parent template
   otherwise.
5. `RealisticAtlasGenerator` groups by subsystem first (when
   qualified), then by template. This produces e.g. 5–6 Create
   islands instead of one giant MECHANISMS pile. Layout density
   inside each island is unchanged from today.
6. AtlasPanelBuilder reads the synthetic label and renders it; no
   color or layout changes beyond the label string.

**Display label policy.** `formatSubsystemLabel` should prefer the
`rationale` field from
[`<modid>.subsystems.json`](../../tools/classification/out/) when
available — those are LLM-curated descriptions like *"Components
generating, transmitting, or measuring rotational force"*. Fall back
to the id-derived form (`Create — Mechanical Power`) when the
rationale isn't bundled into the runtime layer.

**Acceptance.**

- Debug-populate on the test modset produces ~5–6
  Create islands (one per subsystem above threshold), not one
  MECHANISMS island with 740 items.
- Vanilla-only debug populate is unchanged — vanilla items have no
  `mod_subsystem`, so they all fall through to existing template
  logic.
- A small mod with 1–2 items per subsystem still ends up under one
  template-named island.
- A new test `IslandSuggestionTemplateSubsystemTest` covers: modded
  item with qualified subsystem → subsystem island; modded item with
  too-small subsystem → parent template; vanilla item → unchanged.

**Estimated size.** ~250 LOC across descriptor / template / extractor
/ generator / one new test class.

**Open question — threshold default.** 4 is a guess. Worth tuning
during playtest; surface it as a constant on `IslandSuggestionTemplate`
or a config field on the populate command for now.

---

## Phase 3 — `activity` as a template tie-breaker

**Goal.** When `role` is multi-valued (e.g. `track_signal:
[mechanism, redstone_component]`) or two templates would match
equally, use `activity` to pick.

**Files.**

- `IslandSuggestionTemplate.java` — extend `match()` to consult
  `descriptor.activities` after the primary signals.
- New per-template `activityTriggers` set, mirroring the existing
  `roleTriggers`.

**Concrete changes.**

1. Each template declares an `activityTriggers: Set<String>` (e.g.
   REDSTONE → `{redstone}`, TRANSPORT → `{transportation}`,
   WORKBENCHES → `{automation}`).
2. When `firstMatch` finds multiple templates whose primary signals
   match (or finds a multi-valued role list with two viable
   templates), the activity intersection breaks the tie. If activities
   point unambiguously at one template, pick it; otherwise fall back to
   declaration order (current behavior).
3. Subsystem matching from Phase 2 takes precedence over this — a
   modded item with a qualified subsystem goes there regardless of
   activity.

**Acceptance.**

- `track_signal` (role: `[mechanism, redstone_component]`, activity:
  `[redstone, transportation]`) lands on TRANSPORT (the more
  specific match), not MECHANISMS or REDSTONE.
- `track_observer` (same multi-role) lands on TRANSPORT for the same
  reason.
- New test `IslandSuggestionTemplateActivityTest` covers a few
  hand-picked tie-break cases.

**Estimated size.** ~80 LOC.

---

## Phase 4 — Implicit within-island sort

**Goal.** Items in an island that share a `material_family` (and then
a `flavor`) appear next to each other in the layout. No subgroup UI,
no nested islands — just a stable secondary sort key.

**Files.**

- `RealisticAtlasGenerator.java` — the cluster layout pass.
- Wherever live triage adds an item to an island (the placement
  effect handler) — need to identify the exact entry point during
  Phase 4 implementation (likely in
  [`SlotWorkspaceUiController.java`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceUiController.java)
  or its placement helpers).

**Concrete changes.**

1. Define a stable item comparator:
   `(materialFamily, flavor, primaryUsesFirst, id)` — null-safe; nulls
   sort last.
2. Apply the comparator when laying out a freshly-populated island
   (generate path) and when inserting an item into an existing island
   (live placement). For live placement, the island's existing items
   are not reordered — only the new item's slot is chosen by the
   comparator.

**Acceptance.**

- A populated BUILDING island has all `wood_oak` items adjacent, all
  `wood_birch` items adjacent, all `stone` adjacent, etc. The user
  notices implicitly without any UI affordance.
- Manual drag-drop still wins — the comparator is only the *initial
  placement* policy, not a re-sort that fights the user.

**Estimated size.** ~60 LOC including a unit test on the comparator.

---

## Phase 5 — Rarity / frequency for placement priority

**Goal.** Match how players actually organize: ubiquitous things go
to easy-to-reach places, trophies go on display, never-used items go
deep storage.

**Files.**

- `RealisticAtlasGenerator.java` — island ordering + intra-island
  position priority.
- `IslandSuggestionTemplate.java` — special-case for `rarity = trophy`.
- Possibly a new `PlacementPriority` helper class so the logic is
  unit-testable in isolation.

**Concrete changes.**

1. **Trophy shunt.** When `descriptor.rarity == "trophy"`,
   `firstMatch` returns CURIOSITY regardless of role / subsystem /
   activity. (`nether_star`, `dragon_egg`, `wither_skeleton_skull`,
   etc. — and modded equivalents.) Subsystem-primary matching is
   bypassed for trophies because trophies belong on display, not
   filed.
2. **Frequency-driven ordering.** When generating an atlas:
   - Sort islands so high-aggregate-frequency islands come first
     (FOOD before CURIOSITY before MISC).
   - Within an island, items with `frequency = ubiquitous` /
     `common` get the top-left slots; `rare` / `never` items get
     pushed to the bottom-right.
   - Aggregate frequency = mode of the items' frequencies, with ties
     broken by item count.
3. **`frequency = never` handling.** Items the LLM marked as never
   used (creative-only, dev items, deprecated) go to a `MISC.deep`
   pseudo-section at the bottom of the MISC island. Don't create a
   separate island — keep the "deep storage" idea implicit in the
   ordering. Open question: should we let the user toggle visibility
   on these at all? Defer to playtest.
4. **Triage chip side.** When an item enters Triage and a learned
   rule fires, the rule wins (existing behavior). When falling back to
   templates, the trophy / frequency logic above applies. Frequency
   doesn't change which island is suggested — only which slot in that
   island the item lands in.

**Acceptance.**

- A debug-populate atlas has FOOD / TOOLS / WEAPONS islands in the
  first row and CURIOSITY / MISC at the bottom-right.
- `nether_star` placed via debug populate goes into CURIOSITY, not
  MATERIALS.
- Within BUILDING, `oak_planks` (frequency: ubiquitous) is in the top
  rows; `obsidian` (frequency: rare) is further down.
- Vanilla-only behavior unchanged for items the dataset doesn't have
  frequency for (older entries lack the facet — graceful fallback to
  current behavior).

**Estimated size.** ~150 LOC + 1 new test class.

---

## Out of scope (for this plan)

- **`palette` / `dye_color` for color-clustering decoration.** The
  data is loaded by Phase 1 but not consumed by suggestions. Likely
  worth a separate small phase once we see how Phase 4 + 5 land
  visually.
- **Generate ↔ Triage parity on learned-rule data.** The two paths
  use the same template logic but only Triage consults
  `LearnedIslandRuleStore`. This is a real gap (the user with chip
  history won't see what debug-populate produced) — but the user has
  decided debug-populate should generate from data alone, since the
  goal is for suggestions to be good *without* needing chip history.
  Revisit only if Phases 1–5 don't close the gap and we still want a
  "what would my actual atlas look like" debug command.
- **`primary_uses` as a recipe-graph signal.** Could feed
  WORKBENCHES vs MATERIALS disambiguation (an item used as a recipe
  input vs an item used as fuel vs an item used as decoration).
  Defer; the data shape suggests it's better-suited to a separate
  recipe-aware phase.
- **`environmental_property`.** Probably useful for
  bucket / fluid / radiation handling but not for general placement.
  Defer.

## Test discipline

Every phase ships its own tests; coverage gates are:

- Phase 1 — facet parse coverage (singles, multis, ambiguous, mixed).
- Phase 2 —
  [`IslandSuggestionTemplateCoverageTest`](../../common/src/test/java/dev/imagio/slot/inventory/triage/IslandSuggestionTemplateCoverageTest.java)
  extended to assert the test modset produces N
  subsystem islands. Plus the new `Subsystem` test.
- Phase 3 — new `Activity` tie-break test.
- Phase 4 — comparator unit test + a smoke test on
  `RealisticAtlasGenerator` output ordering.
- Phase 5 — trophy-shunt + frequency-ordering tests.

Existing
[`IslandSuggestionTemplateCoverageTest`](../../common/src/test/java/dev/imagio/slot/inventory/triage/IslandSuggestionTemplateCoverageTest.java)
must still pass at every phase — the invariant "every classified item
routes to *some* template" is the regression guard.

## Notes for the next session

- Start at Phase 1; the code is concentrated in `FacetIndex` and is
  the cheapest validation that the data on disk actually carries
  what we expect (especially the multi-value `subsystems` /
  `activities` shapes).
- Before diving into Phase 2, run a quick histogram of subsystem
  counts across the bundled per-mod files to validate the threshold
  default. If most subsystems have 8+ items, threshold=4 is fine.
  If many cluster around 2–3, lower to 3.
- The minimum-items-per-subsystem threshold cuts both ways: too low
  and the atlas is noisy; too high and big mods collapse back to
  generic templates. Surface it as a tunable, not a hardcoded
  constant.
- Phase 5's "frequency = never" handling overlaps conceptually with
  the relevance-LOD work
  ([../design/relevance-lod.md](../design/relevance-lod.md)) — the
  scoring system *also* wants to know which items are rarely-touched.
  Avoid building a parallel mechanism; the simplest thing is to let
  Phase 5 set initial spatial layout and let relevance-LOD update
  positions over time.
