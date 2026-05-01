# Facet-Driven Suggestions — Implementation Plan (shipped)

Last updated: 2026-04-30 (Phases 1–6 shipped; archived from active queue)

> **Status (2026-04-30):** all phases shipped, tests green
> (`:common:test :neoforge:test`). Phase 1 (FacetIndex extension),
> Phase 2 (subsystem-primary matching with min-items threshold —
> ratcheted to 10 in `RealisticAtlasGenerator` after playtest showed 4
> was too aggressive), Phase 3 (activity tie-break in
> `firstMatchWithActivityTieBreak`), Phase 4 (within-island sort —
> re-shaped during implementation: carry-rank dominates, then a
> layered cluster key with dye-stem before palette before plain id),
> Phase 5 (rarity / carry_frequency placement priority + trophy →
> CURIOSITY shunt), Phase 6 (color clustering — `dye_color` drives a
> canonical Minecraft dye-wheel within each stem, `palette` drives a
> tone-cluster fallback for non-dyed items).
>
> Phase 6 was originally listed as out-of-scope — the data shape made
> it the natural follow-on once Phases 1–5 played cleanly. See `Phase
> 6` section below.
>
> Pre-flight note preserved for historical context:
>
> **Original pre-flight (2026-04-30):** the learned-storage bug pass closed
> 2026-04-30 (every claim-flow / chip / panel / kit bug shipped — see
> [current.md](../current.md) recap). The FacetIndex-driven populate path
> playtested cleanly on the same day, so the data on disk is producing
> usable triage chips today and we're ready to enrich them. **Start
> here:** Phase 1 of this doc — extend `FacetIndex.ItemFacets` + the
> load parser.

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

## Phase 6 — Color + flavor + origin clustering (shipped)

**Goal.** Use the previously-loaded-but-unused `dye_color`, `flavor`,
and `origin` facets plus the newly-loaded `palette` facet to give the
within-island sort a color-, flavor-, and progression-aware
sub-cluster. A 16-color stack of wools / carpets / concretes should
read as a Minecraft dye-table color wheel rather than the alphabetical
chaos pure id-sorting produces (black, blue, brown, cyan, gray, ...).
Non-dyed items with a palette tone (`wood_red`, `copper_oxidized`,
`warm`) cluster by tone. Plain-id items partition first by flavor
(plain → natural → variant → colored → fancy → mechanical → mystical →
ominous → ancient → unflavored) so a BUILDING island leads with plain
stone bricks before chiseled / mossy / cracked variants, then by
origin tier (early overworld+crafted → mid structures → late
nether+end → unknown → creative-only) so within a flavor band an
"occasional" arrow leads an "occasional" end_rod.

**Files.**

- [`common/.../classification/FacetIndex.java`](../../common/src/main/java/dev/imagio/slot/classification/FacetIndex.java)
  — load `palette` (multi-value); add `palette(itemId)` accessor.
  `flavor` was already parsed in Phase 1 but never read by anyone
  downstream.
- [`common/.../inventory/triage/IslandSignalDescriptor.java`](../../common/src/main/java/dev/imagio/slot/inventory/triage/IslandSignalDescriptor.java)
  — add `palette: List<String>` field; backfill compact constructors.
- [`neoforge/.../triage/IslandSignalExtractor.java`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/triage/IslandSignalExtractor.java)
  — populate `palette` from `FacetIndexHolder`.
- [`common/.../debug/RealisticAtlasGenerator.java`](../../common/src/main/java/dev/imagio/slot/debug/RealisticAtlasGenerator.java)
  — `WITHIN_ISLAND_COMPARATOR` re-keyed on a layered cluster key:
  `1dye:<stem>` → `2pal:<primary-palette>:<flavor-rank>:<origin-tier>` →
  `3pln:<flavor-rank>:<origin-tier>:<full-id>`. Within a dye cluster,
  items follow a canonical Minecraft dye order (white → light_gray →
  gray → black → brown → red → orange → yellow → lime → green → cyan →
  light_blue → blue → purple → magenta → pink). Within a palette tone,
  items split by flavor → origin → id-alpha. Plain-id items partition
  by flavor → origin → id-alpha.
- Tests: `FacetIndexTest`, `RealisticAtlasGeneratorTest` (added
  `dyedItemsClusterByStemThenCanonicalColorOrder`,
  `palettedItemsClusterByPrimaryToneNotIdAlpha`,
  `dyedItemsLeadPalettedItemsLeadPlainItems`,
  `flavorPartitionsPlainBlocksBeforeFancyAndAncient`,
  `originTierOrdersEarlyOverworldAheadOfLateNetherAndCreative`).

**Decisions.**

- Carry-rank still dominates the cluster key — high-frequency items
  always lead the island. The cluster key is the secondary order
  within a carry-rank band.
- Ordering between zones (dye → palette → plain-id) is enforced via
  the numeric prefix on the cluster key. Dyed blocks come first
  because the dye-table color wheel is the visually loudest cluster
  and reads as "the painted section" of the island. Palette tones come
  next as a softer visual grouping. Plain-id items round out the
  bottom.
- Dye clusters are kept intact — flavor doesn't refine them, because
  the 16-color wheel is more visually meaningful than splitting
  "plain wool" from "fancy wool" within the wheel.
- Flavor coverage: 9 distinct values across 823 vanilla items. The
  rank ordering is curated, not alphabetical: plain → natural →
  variant → colored → fancy → mechanical → mystical → ominous →
  ancient → null. The earlier facet-driven plan tried flavor as a
  top-level secondary key and rejected it as alphabetical noise; as
  a deep sub-cluster (after carry-rank, dye, palette) the
  alphabetical-noise risk vanishes because flavor only refines
  same-cluster groups.
- Vanilla coverage: 232 items have `dye_color`, 362 have `palette`,
  823 have `flavor`, 1300 have `origin`. Cross-population overlap is
  minimal so the cluster bands don't fight each other.
- Origin's 27 distinct values collapse into 5 tier bands (early /
  mid / late / unknown / creative). `crafted_only` (671 items)
  joins the early band — crafted items are accessible alongside
  their early-game inputs and shouldn't be artificially separated.
  Carry-rank already pushes truly rare items down regardless of
  origin, so origin's job is to refine same-flavor same-rank groups
  (an "occasional" arrow ahead of an "occasional" end_rod), not to
  override the carry-rank dominance.

## Phase 6.1 — Live placement uses the same cluster ordering (shipped 2026-04-30)

**Goal.** When a player accepts a chip, the new home should land in
the right cluster slot — not blindly appended to the end of the
island. The populate-time generator and the live placement path now
share the comparator, so a chip-accepted black_wool tucks in next to
existing wools instead of dangling at the bottom.

**Files.**

- New [`common/.../inventory/triage/WithinIslandOrdering.java`](../../common/src/main/java/dev/imagio/slot/inventory/triage/WithinIslandOrdering.java)
  holds the comparator + cluster key + helper functions, extracted
  from `RealisticAtlasGenerator`. This breaks the awkward
  `inventory.workspace → debug` import path and gives both call
  sites a single source of truth.
- [`common/.../debug/RealisticAtlasGenerator.java`](../../common/src/main/java/dev/imagio/slot/debug/RealisticAtlasGenerator.java)
  delegates `WITHIN_ISLAND_COMPARATOR` to the extracted class. The
  populate-time `DescribedStack` record stays defined here as a
  type alias so existing tests keep compiling unchanged.
- [`common/.../inventory/workspace/SlotWorkspaceCommandService.java`](../../common/src/main/java/dev/imagio/slot/inventory/workspace/SlotWorkspaceCommandService.java)
  `resolveOrdinal` now does cluster-aware insertion when the
  caller passes `ordinal=null` (chip-accept path; explicit
  drag-drop ordinals still pass through unchanged). For each
  existing assignment in the target island, it builds a
  `DescribedStack` from the view-model's display stack +
  `signalExtractor`, walks them in current-ordinal order, and
  returns the first existing ordinal whose comparator key sorts
  *after* the new identity. Falls back to plain append when
  descriptors aren't available.
- New [`common/.../inventory/triage/WithinIslandOrderingTest.java`](../../common/src/test/java/dev/imagio/slot/inventory/triage/WithinIslandOrderingTest.java)
  exercises the comparator directly: zone ordering (dye → palette →
  plain), canonical dye order, palette × flavor × origin sub-keys,
  carry-rank dominance.

**Decisions.**

- Manual drag-drop still wins. The cluster-aware insertion only
  fires on auto-append (`ordinal=null`); explicit ordinals pass
  through to the projection unchanged. Players who manually
  re-arranged an island won't see chip-accept fight their layout.
- Insertion uses linear scan over current-ordinal-sorted assignments
  in the target island. O(N) per chip-accept, where N is the
  island's item count (typically tens, rarely > 200) — well within
  the chip-accept latency budget.
- When the new identity is already homed in the same island, the
  insertion loop skips it so the projection's
  `compactOrdinalsAfterRemove` does the right thing.

**Out of scope.**

- **Re-sorting an existing island.** This is intentionally not done
  — the user might have manually arranged things and we don't want
  to fight that. The cluster ordering is *initial placement* only.

---

## Phase 6.2 — Learned-rule adjacency expansion (shipped 2026-04-30)

**Goal.** The learned-rule store records "items I home together
suggest the same destination" via four adjacency channels: TAG,
MATERIAL_FAMILY, NAMESPACE, CREATIVE_TAB. Two of the FacetIndex
facets carry strong "player intent" signal that previously had no
learning path: `mod_subsystem` (a player who homes two Create
mechanical-power items is signaling "Create machinery cluster") and
`dye_color` (homing two white items signals "white-themed island").
Phase 6.2 adds those two as new adjacency kinds so manual placement
overrides the template default and sticks.

**Files.**

- [`common/.../inventory/triage/LearnedAdjacencyKey.java`](../../common/src/main/java/dev/imagio/slot/inventory/triage/LearnedAdjacencyKey.java)
  adds `Kind.SUBSYSTEM` and `Kind.DYE_COLOR` (both at priority rank
  0, matching TAG / MATERIAL_FAMILY) plus `subsystem(id)` and
  `dyeColor(value)` factory methods.
- [`common/.../inventory/triage/LearnedIslandRuleStore.java`](../../common/src/main/java/dev/imagio/slot/inventory/triage/LearnedIslandRuleStore.java)
  `adjacencyKeys` emits one SUBSYSTEM key per non-blank entry in
  `descriptor.subsystems()` (multi-value) and one DYE_COLOR key
  when `descriptor.dyeColor()` is set.
- New tests in
  [`LearnedIslandRuleStoreTest.java`](../../common/src/test/java/dev/imagio/slot/inventory/triage/LearnedIslandRuleStoreTest.java):
  `subsystemAdjacencyFiresOnceTwoCreateItemsLandOnSameIsland`,
  `dyeColorAdjacencyFiresAcrossDyedSiblings`,
  `differentSubsystemsAreIndependentLearningChannels` (gates that
  `create:mechanical_power` learnings don't bleed into the
  `create:logistics` SUBSYSTEM channel — though NAMESPACE adjacency
  still fires for shared-namespace items, which is expected).

**Decisions.**

- Both new kinds get priority rank 0 alongside TAG /
  MATERIAL_FAMILY. They're equally strong specific signals; the
  same `min_confirmations=2` threshold prevents one-off placements
  from spamming chip suggestions.
- `flavor` and `origin` are deliberately *not* added as adjacency
  channels. `flavor` doesn't reflect placement intent (most players
  don't think "I'll put my fancy items here"). `origin` is too
  diffuse — `crafted_only` covers 671 items and would over-trigger.
- The chip-suggestion service caps total chips at 2 and learned
  chips at 2, so adding two adjacency dimensions doesn't multiply
  chip noise — at most one chip per matching island is shown.

---

## Phase 6.3 — Deposit-planner facet-affinity fallback (shipped 2026-04-30)

**Goal.** `DepositPlanner` previously required exact-identity affinity
to route a stack to a chest. A brand-new `netherite_ingot` couldn't
deposit into the "Mining" chest even though it shared `c:ingots` /
`material_family=*` / `namespace=minecraft` with the iron and gold
ingots already there. Phase 6.3 adds a facet-affinity fallback: chests
with no direct identity bond but with bonds to facet-similar
identities still become candidates, ranked below direct-affinity
chests.

**Files.**

- [`common/.../inventory/triage/LearnedAdjacencyKey.java`](../../common/src/main/java/dev/imagio/slot/inventory/triage/LearnedAdjacencyKey.java)
  promoted `keysFor(IslandSignalDescriptor)` from a package-private
  helper inside `LearnedIslandRuleStore` to a public static method
  on `LearnedAdjacencyKey`. Single source of truth for the canonical
  adjacency-key set; the broad-namespace blacklist
  (`minecraft`/`c`/`forge`/`neoforge`) is now centralized too.
- [`common/.../inventory/triage/LearnedIslandRuleStore.java`](../../common/src/main/java/dev/imagio/slot/inventory/triage/LearnedIslandRuleStore.java)
  delegates `adjacencyKeys` to `LearnedAdjacencyKey.keysFor`.
- [`common/.../inventory/workspace/DepositPlanner.java`](../../common/src/main/java/dev/imagio/slot/inventory/workspace/DepositPlanner.java)
  new 5-arg overload accepts a
  `Function<ItemIdentity, IslandSignalDescriptor>` lookup. When the
  carried identity has zero direct affinity for a proximate chest,
  the planner sums affinity over chest residents that share at least
  one adjacency key with the carried identity. Direct-affinity
  chests always rank ahead of facet-affinity chests; within each
  tier the highest score wins. The legacy 4-arg overload preserves
  the old behavior (no fallback).
- [`neoforge/.../screen/ldlib/SlotWorkspaceUiSession.java`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceUiSession.java)
  `descriptorForIdentity` resolves an `ItemIdentity` to an
  `IslandSignalDescriptor` via `GhostAtlasStackFactory.resolve` +
  `IslandSignalExtractor.extract`. Wired into the deposit caller as
  a method reference.
- New tests in
  [`DepositPlannerTest`](../../common/src/test/java/dev/imagio/slot/inventory/workspace/DepositPlannerTest.java):
  `facetSimilarBondsRouteWhenNoDirectAffinity`,
  `directAffinityOutranksFacetAffinity`,
  `facetFallbackSkippedWhenLookupReturnsNull`.

**Decisions.**

- Direct affinity always outranks facet affinity, even when the
  facet aggregate score is much higher. A chest with affinity=1 for
  the exact identity beats a chest with facet-aggregate=10 for
  similar identities. Reasoning: the player's explicit history
  (they put this exact item there before) is a stronger signal than
  inferred similarity.
- Facet score sums `bond.score()` over chest residents where any
  adjacency key intersects the carried identity's keys. A bond is
  counted at most once even if it shares multiple keys, so two
  bonds with overlapping facets aren't double-weighted.
- Adjacency-key construction now lives on `LearnedAdjacencyKey`. Both
  the chip-suggestion learning store and the deposit-planner
  fallback go through the same code, so a player's "items I home
  together" learnings line up with their "items I deposit together"
  routing.
- Datapack-only / removed-mod identities silently drop out of the
  facet-aggregate via the `null`-tolerant lookup, so deposit routing
  doesn't crash on unknown ids.

**Out of scope.**

- **Persisting facet-affinity bonds.** The fallback is computed
  on-the-fly from existing direct bonds; we don't accumulate a
  separate "facet bond strength" record. The persisted state stays
  identity-keyed and forward-compatible.
- **Cross-namespace facet adjacency.** The broad-namespace blacklist
  prevents `minecraft` / `c` / `forge` / `neoforge` from acting as a
  facet match. A modded ingot still matches an iron_ingot via
  `c:ingots` (tag), `material_family=iron` (when classified), or
  the modded namespace if both share it — same rules as the chip
  learning side.

---

## Phase 6.4 — Debug-generator chest content clusters by facet (shipped 2026-04-30)

**Goal.** `RealisticAtlasGenerator.buildChestSpec` previously picked
each fill uniformly at random from the linked island's pool, so a
20-slot MATERIALS chest was a uniform scoop of the entire island
(iron, gold, copper, redstone, …). Real players theme each chest:
one for iron-family, one for gold, one for wood. Phase 6.4 seeds
each generated chest with a "facet flavor" so the populated atlas
reads as themed sub-clusters across multiple chests in the same
island.

**Files.**

- [`common/.../debug/RealisticAtlasGenerator.java`](../../common/src/main/java/dev/imagio/slot/debug/RealisticAtlasGenerator.java)
  `buildChestSpec` picks a seed `DescribedStack` from the linked
  island, then gets the seed's "specific" adjacency keys (TAG /
  MATERIAL_FAMILY / SUBSYSTEM / DYE_COLOR — priority-rank-0 kinds
  only; NAMESPACE / CREATIVE_TAB are excluded so namespace matches
  don't collapse the cluster). New `pickLinkedItem` helper biases
  fills toward seed-key matches with probability
  `CHEST_FACET_SIMILARITY_BIAS = 0.7`; the remaining 30% of linked
  fills are uniform random so siblings of one cluster occasionally
  spill into adjacent chests (just like real player chests).
- New test `linkedChestsClusterContentByFacetSimilarity` in
  [`RealisticAtlasGeneratorTest`](../../common/src/test/java/dev/imagio/slot/debug/RealisticAtlasGeneratorTest.java)
  populates 8 iron-family + 8 gold-family items into MATERIALS and
  asserts ≥ 60% of generated chests are facet-themed (≥ 60/40
  family split). Without the cluster bias, uniform-random pick
  would give ~25% themed by binomial chance.

**Decisions.**

- The "specific" key filter (priority-rank-0 only) is essential —
  the failing first test pass showed that unfiltered keys let
  NAMESPACE matches bleed every `modded:*` item into a single
  cluster. The chip-learning side uses the full key set because the
  goal there is "any signal that pulls items together"; the
  chest-clustering side uses the tighter set because the goal is
  "what makes one chest visibly different from its siblings".
- Bias is 70% (not 100%) so a chest still picks up a few "spill"
  items from sibling clusters. Real chests tend to have one or two
  items that don't fit the theme — a stray torch in the iron chest,
  a stray bucket in the wood chest. 70% reads as themed without
  being sterile.
- The 85% linked-pool bias and the 60%-template-match acceptance
  test (`linkedChestsMostlyContainTemplateMatchedItems`) still pass
  unchanged — the chest's facet flavor is layered *on top of* the
  existing template bias, not in place of it.

---

## Phase 6.5 — Cross-chest seed diversity (shipped 2026-04-30)

**Goal.** Phase 6.4 themed each chest individually but seeded each
chest independently — three MATERIALS chests in the same island
could randomly all seed on iron, ignoring gold and copper. Phase 6.5
threads a per-island "claimed seed keys" set through `planChests`
so subsequent chests in the same island prefer seeds whose specific
keys are *disjoint* from already-claimed keys. The result: a
populated MATERIALS section reads as "iron chest / gold chest /
copper chest" rather than "iron chest / iron chest / iron chest".

**Files.**

- [`common/.../debug/RealisticAtlasGenerator.java`](../../common/src/main/java/dev/imagio/slot/debug/RealisticAtlasGenerator.java)
  `planChests` now keeps a
  `Map<String, Set<LearnedAdjacencyKey>>` of claimed keys per
  island id. Each linked chest passes its island's set to
  `buildChestSpec`; the new `pickDiverseSeed` helper biases seed
  selection toward items whose specific keys are disjoint from the
  already-claimed set, then `buildChestSpec` adds the new seed's
  keys to the claim set so the next chest in the same island
  picks a different theme.
- New test
  `multipleChestsInSameIslandSpanDifferentFacetThemes` in
  [`RealisticAtlasGeneratorTest`](../../common/src/test/java/dev/imagio/slot/debug/RealisticAtlasGeneratorTest.java).
  Five seeds × 8 iron + 8 gold + 8 copper, all routed to MATERIALS;
  asserts that across the aggregate of generated chests, ≥ 2
  distinct dominant families surface (50% threshold per chest, with
  multi-seed aggregation to absorb individual seed noise).

**Decisions.**

- Multi-seed aggregation in the test reflects real-world reality:
  with only 24 items and a small linked-chest budget per island
  (typically 2 chests), individual-seed family-purity is sensitive
  to which 4–20 fills each chest happens to draw. Aggregating
  across seeds gives a stable signal that the diversity bias
  works without over-fitting to a specific RNG.
- The diverse-seed pool falls back to uniform random when every
  candidate's keys overlap the claim set (i.e. the island's facet
  themes are exhausted). With 3 chests and 3 families, the third
  chest's diverse pool is the remaining family; with 4+ chests,
  duplication starts. That's intentional — it mirrors the player's
  reality of accumulating duplicate-themed chests in mature bases.
- Unlinked chests (no `IslandBuild`) get a fresh empty claim set
  per chest so they don't pollute the linked-island claim
  bookkeeping.

---

## Phase 6.6 — Rarity-aware stack count rolls (shipped 2026-04-30)

**Goal.** `rollStackCount` was template-only, so a trophy-tier item
(`role=trophy` or `rarity=unique`) routed to CURIOSITY rolled
1–8 instead of always 1. A `nether_star` showing up as a stack of 5
in a populated chest reads as wrong. Phase 6.6 threads the
descriptor through every `rollStackCount` callsite and short-circuits
to count=1 for trophies and display-only / never items.

**Files.**

- [`common/.../debug/RealisticAtlasGenerator.java`](../../common/src/main/java/dev/imagio/slot/debug/RealisticAtlasGenerator.java)
  added the 4-arg
  `rollStackCount(stack, template, descriptor, random)` overload;
  trophies (via `IslandSuggestionTemplate.isTrophy(descriptor)`) and
  `carry_frequency in {display_only, never}` items short-circuit to
  count=1 before the template-keyed roll. The legacy 3-arg overload
  delegates with `descriptor=null`. `TemplatedStack` record gained
  a `descriptor` field so the descriptor flows through both the
  carried-pool and chest-content paths (the latter both for
  facet-similar linked picks and 15% non-linked overflow).
- New test
  `uniqueRarityItemsAlwaysRollAsSingleStackInChests` in
  [`RealisticAtlasGeneratorTest`](../../common/src/test/java/dev/imagio/slot/debug/RealisticAtlasGeneratorTest.java).
  Generates 12 trophy items and asserts every trophy that lands in
  a chest has count=1.

**Decisions.**

- Used `IslandSuggestionTemplate.isTrophy(descriptor)` so both
  `role=trophy` and `rarity=unique` paths get the same treatment —
  matches the trophy shunt logic that drives island routing, no
  divergence between "where it goes" and "how many of it appear".
- Did *not* clamp other rarities (e.g. `rare` → smaller stack)
  because that's playtest-invisible noise — the trophy count=1
  is the visible win. Defer rare/uncommon clamping until playtest
  signals demand it.
- The 15% non-linked overflow path now also passes the descriptor
  through (previously hardcoded to `null`), so a trophy that lands
  in a non-trophy chest via the cross-island spill still rolls as
  count=1.

---

## Out of scope (for this plan)

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
