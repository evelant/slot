# Relevance-Driven LOD

Last updated: 2026-04-25

> **Retired (2026-05-03).** Superseded by
> [../../plans/done/list-view.md](../../plans/done/list-view.md). The list-view direction
> uses a single LOD — no zoom, no band selection — so the LOD-band part
> of this design isn't shipping. The **relevance scoring** machinery
> survives in reduced form: scoring still drives ordering hints inside
> sections, search filtering, and TOC status dots in the new model. The
> band-picker, multi-band card chrome, and density-via-LOD framing are
> stale. Do not author new work against this doc; see the plan.

Status: current direction for managing atlas density at modded scale.
Generalizes the existing carried-vs-ghost asymmetric LOD into a per-item
**relevance score** that picks each item's render band, so the atlas
continuously deforms toward what's relevant *right now* without giving up
canonical homes or rebuilding geometry.

For the atlas concept this builds on, see [atlas.md](atlas.md). For the
storage model that joins the relevance system, see [storage.md](storage.md).
For the facet dataset that several of the contributors lean on, see
[classification/README.md](classification/README.md).

## Why

The atlas was built on two principles:

- **One canonical home per identity.** Preserves spatial memory.
- **Always show everything at the same fidelity.** Preserves the constellation.

Both held up well for carried inventory. At modded scale (1500+ identities
in vanilla, several thousand in a heavy pack) the atlas becomes mostly
ghosts. The relevant constellation drowns in noise; the player's eye
spends more time discarding than recognizing. Spatial memory still works
in principle but the signal-to-noise of "what matters right now" collapses.

Considered alternatives:

- **Multiple homes per identity.** Fragments memory across instances; an
  identity that lives in two places lives in *neither* place reliably.
- **Separate views with different geometries.** Teaches competing
  geographies — already flagged as a risk in
  [atlas.md § Canonical Home Versus Derived Projection](atlas.md). Two
  maps to learn is worse than one map with one density problem.

The reframe behind this design: **spatial memory and one-home are means,
not ends.** The end is "alleviate inventory-management tedium." The
relevance model serves that end while preserving as much of the structural
spatial memory as practically useful.

## The model in one paragraph

Every item has a per-frame **relevance score**, computed as a combination
of named contributions (carried, kit-relevant, search-matched, …). The
score and the camera scale together pick which LOD band the item renders
at — full readable card at one end, single-pixel pip at the other. High
scores get visual budget; low scores fade toward the underlying lattice.
Layout packs items in their canonical order with cell sizes derived from
their bands, so islands compact around relevant items without rearranging.
(Earlier drafts of this doc described recursive application to chest
tiles inside named storage areas; that surface retired with the
learned-storage swap — chests are now chips in a left-column flex
panel and chest contents render as ghost atlas cards on their homed
island.)

## Relevance contributors

The score is the **max** of independent contributions. Each contributor is
named, debuggable, and addable without changing the model. (Weighted-sum
combination is a possible future refinement, but max is simpler to reason
about and avoids parameter-explosion; reach for sums only if a concrete
case demands them.)

- **Carried** — item is in carried inventory (mainhand, offhand, hotbar,
  main). High.
- **Kit member** — item is on the active Kit's belt, offhand, or bring
  list. High.
- **Kit missing** — Kit wants the item but it's not present anywhere.
  High, so ghosts of wanted items don't shrink and the gather flow has
  visible targets.
- **Shopping list** — item is an ingredient in a pinned recipe (EMI/JEI
  integration or in-app craft planner). High.
- **Search match** — item matches the active search query. High; outranks
  carried so search results pop out of the local constellation.
- **Recently touched** — item entered or left carried inventory inside a
  recent window. Medium.
- **Area proximity** — item lives in a chest in a currently-proximate
  storage area. Medium-high. (See [Storage areas](#storage-areas).)
- **Chest holds relevant** — chest contains a kit-missing or
  shopping-list item. Boosts the chest within its area, even when not in
  proximity, so search/gather flows can light up off-base storage.
- **Baseline** — everything else. Low.

Future contributors slot in the same way — activity-facet kit emphasis,
cleanup-mode dimming, biome-aware combat highlights — without inventing
new modes. Every "view" idea collapses into "add a contributor that boosts
items matching a predicate."

## LOD bands

The band set in [atlas.md § Semantic Zoom](atlas.md) is unchanged:
`region`, `browse`, `read`, `inspect`, `close-inspect`, plus a `pip` band
below `region` for ghost-at-overview rendering. What changes is the
picker:

```
band = pickBand(cameraScale, relevance)
```

Both inputs raise the band: high zoom *or* high relevance gets the
player to readable cards; low zoom *and* low relevance produces pips.
Discrete bands avoid continuous-size jitter — a contributor change
snaps the item cleanly to a new band rather than easing through fractional
sizes.

## Layout under variable LOD

The mechanic that has to feel right is **how an island reflows when its
items render at different sizes**. The model is a heterogeneous weighted
grid:

- Each item has a layout weight derived from its rendered band.
- The island walks items in **canonical order** (the order they were
  homed in), packing rows where each row's height is the max weight in
  that row and each cell's width is its own weight.
- The island's bounding box is whatever that layout produces.

This preserves what spatial memory actually uses:

- **Identity-to-region binding** ("iron is in Tools") — preserved exactly.
- **Within-region ordering** ("iron is upper-left of gold within Tools")
  — preserved exactly.
- **Region adjacency** ("Tools is left of Food") — preserved approximately;
  neighbors may shift slightly as their effective sizes change.
- **Exact pixel coordinates** — not preserved, and that's fine. People
  remember structure, not pixels.

The atlas applies the same packing one level up. Each island has an
effective bounding box from its interior; islands repack within the atlas
based on those boxes. An island with zero high-relevance items shrinks to
a chip showing label and a `carried/total` count badge — but never
disappears. The label is the landmark; the landmark must remain even when
the territory is empty.

## Reflow discipline

Continuous reflow on every pickup tick would make the atlas restless.
Reflow happens on coarse triggers only:

- inventory open
- kit activate / deactivate / page change
- search submit / clear
- shopping-list pin / unpin
- area proximity change
- explicit "reflow now" hotkey (escape hatch)

Mid-session pickups inflate locally — the new card animates in at its
target band — without cascading neighbor reflow. Cascade only happens at
coarse-trigger boundaries. The deformation must read as continuous
animation, not as a teleport; the existing animation discipline in
[atlas.md § Animation Discipline](atlas.md) covers the case.

## Where the score and layout run

Captured as
[../decisions/0005-relevance-score-and-layout-locality.md](../decisions/0005-relevance-score-and-layout-locality.md).
The relevant invariants for this design:

- **Score is a derivation, not state.** Computed at the use site from
  a `RelevanceContext` plus a contributor list. Server and client may
  both compute it, with different contexts and possibly different
  contributor sets, depending on what they're consuming it for.
- **Layout is client-owned.** The packer + band picker run client-side
  from the view model + camera scale + active search query. The
  server stops shipping per-item position/size on the wire.
- **Drag-drop is ordinal.** Player drops resolve to
  `(islandId, insertionOrdinal)` client-side; the server stores the
  ordinal in `VisualHomeAssignment`. Exact pixel coordinates are no
  longer authority-shaped state.
- **Search query stays client-only.** Reflow on submit/clear matches
  the coarse-trigger discipline below.

This locality choice keeps presentation out of the wire format, lets
the server use the same scoring code for its own decisions
(auto-homing, kit-aware pickup), and avoids the server-side-vs-
client-side split-brain that "ship layout from server but adjust on
client" would create.

## Storage areas (SUPERSEDED 2026-04-30)

> **Replaced by the learned-storage swap.** The "explicit named area
> with chip-collapse + expand-on-proximity" design below is no longer
> the plan. What shipped:
>
> - Implicit auto-claim on first deposit.
> - Derived clusters via `ChestClusterMap` (16-block spatial union-
>   find), default labels "Storage Area N", player rename via
>   right-click chip context menu.
> - Chest contents render as ghost atlas cards on their homed island
>   when the chest is proximate; non-proximate stocks surface via the
>   chest locator panel and the search-time `+N stored` badge.
> - Affinity decay + explicit forget (undoable). No demote / collapse
>   gesture needed; chips are already chip-sized by default.
>
> See [plans/learned-storage.md](../plans/learned-storage.md) and
> [plans/current.md](../plans/current.md) for the implementation
> recap. Text below is preserved as historical context for the
> relevance / LOD design exploration only.

External storage joins the relevance model with one small structural
addition: storage **areas**.

An **area** is a player-named container of chest tiles ("Main Base",
"Mountain Mine", "Oil Derrick", "Warehouse"). Every claimed chest belongs
to exactly one area. There is no auto-cluster-by-world-proximity emergence
— area assignment is explicit at claim time, with a current-proximity-area
default and "Main Base" as initial fallback.

Areas participate in the same relevance/LOD model as islands, recursively:

- **Areas** default to chip size. Area chips show name, chest count, and
  a "carried-relevance" badge when something inside is relevant.
- An area expands when any of its chests is in proximity, when search /
  shopping-list resolution touches it, or when the player pins it.
- **Inside an expanded area**, individual chest tiles follow the same
  rules. Chests in proximity render their full live contents grid;
  chests not in proximity stay at chip-or-empty-island size; chests
  holding high-scoring items (kit-missing, shopping-list, search-match)
  glow within an otherwise compact tile so the relevant cells pop.

The result: storage takes near-zero atlas real estate by default;
expands on intent (proximity, search, kits); and within an expanded area,
only the relevant parts render fully. Same machinery, no special cases.

### Knock-on effects

- "Where is iron?" answers in area-units ("128 in Main Base, 32 in
  Mountain Mine") rather than chest-units. Easier to read at scale.
- Wayfinding particle trails get a meaningful named destination ("trail
  to Main Base") instead of "trail to nearest chest of N."
- Shulker boxes and ender chests fit naturally as portable / virtual
  areas — no new concept.
- Existing claimed chests migrate into a default "Main Base" area on
  first run; players regroup at leisure.

## Classification-driven placement

Adjacent to the LOD model, the same facet dataset feeds **Triage chip
suggestions** — a separate surface from the atlas, but worth covering in
this doc because both lean on classification data and both are part of
the same prototype phase.

Today's chip suggestion logic in `IslandSuggestionService` (see
[../plans/current.md § 2b](../plans/current.md)) was built before the
facet dataset was complete. It uses item-class checks (`DiggerItem`,
`ArmorItem`, …), a few `#c:*` tags, and `DataComponents.FOOD`. Coverage
is poor in modded packs and the suggestions are vague when they hit:
"Tools" rather than "Iron Tools," "Materials" rather than "Mekanism
Factory Frames." That weakness is a meaningful source of triage
friction.

With a confident per-item classification record (`role`,
`material_family`, `tier`, `activity`, `mod_subsystem`, `flavor`, …),
almost every item can offer a precise suggestion the moment it lands in
Triage. A pickaxe becomes "→ Iron Tools" rather than "→ Tools template";
an AE2 cable becomes "→ AE2 Cables" rather than nothing.

### Confidence-graded behavior

- **High confidence + matching island exists** — single-tap chip
  placing into the matched island. Confidence comes from the facet
  record's own per-facet `confidence` field plus how many facets agree
  on a target island.
- **High confidence + no matching island** — single-tap chip
  materializing a new island seeded from the facet (label / color /
  icon defaults from `role` and `material_family`).
- **Medium confidence with two plausible matches** — two chips
  (the existing 2-chip cap stays).
- **Low confidence** — no chip; item stays in Triage waiting for the
  player. The current rule that "no chip is better than a wrong chip"
  is preserved.

### Stretch goal: rethink Triage's existence

If facet-driven suggestions land at very high precision (close to
99% acceptance on the high-confidence band across modded packs), the
project may revisit the "no silent auto-homing" rule for that band. The
hypothesis: a tap the player would have done anyway adds friction
without value. If that hypothesis holds, Triage shrinks to a small
panel of genuine ambiguities, or disappears entirely in favor of
auto-homing for high-confidence items plus a "review recent
auto-homes" surface.

This is **not a commitment** — it's a stretch goal whose decision
depends on numbers we don't have yet. The prototype should track
suggestion-acceptance rates so we can decide from data, not theory.

The seam is small: `IslandSuggestionService` already takes signals +
learned rules + islands and returns chips. Swap the signal source from
class / tag / component checks to facet lookups via `FacetIndex`.
Depends on classification milestone 6
([item-classification.md](../plans/item-classification.md)) being
landed.

## What this replaces or generalizes

- [storage.md § Asymmetric LOD For Carried vs Ghost](storage.md) is the
  seed of this model. "Carried high, ghost low" becomes one contributor
  pair in a richer score.
- The [Pocket Lens](storage.md) brainstorm collapses to "set every
  contributor except `carried` to zero" — a hold-modifier toggle on
  the score, not a separate view.
- The "view" / "lens" ideas from earlier brainstorming collapse into
  "add a contributor that boosts items matching a predicate." Activity
  lenses, kit focus, shopping-list focus, search highlights — all the
  same machinery.
- The "Triage island" landmark in earlier docs is *not* in this model.
  Triage is a docked list panel (see
  [../plans/done/core-workflow-ux.md § Slice 1](../plans/done/core-workflow-ux.md))
  and is not subject to atlas LOD. The relevance model applies to atlas
  content; the docked panel is its own surface, in one fixed place,
  optimized for sequential processing.

## What stays the same

- One canonical home per identity. The mechanism for managing density
  is now LOD, not a second home.
- Stable spatial memory at the structural level (region binding,
  ordering, adjacency).
- Camera zoom semantics. The band picker just has a second input.
- Existing animation discipline; no new animation primitives.
- All authority rules from [atlas.md § Authority And Visual Memory](atlas.md).
  Visual size is presentation, not authority. Action targets stay
  source-aware and proximity-gated.

## Prototype plan

**Goal:** prove the relevance-driven LOD model is the right answer to
atlas density at modded scale, by wiring up the full set of contributors
that have source data today and observing whether the atlas surfaces the
right items in the right contexts.

The prototype is **one cut, not a series of single-contributor
experiments.** Adding contributors one at a time wouldn't actually test
the idea — relevance only feels right when the score reflects player
intent across multiple signals simultaneously (carried + kit + search +
proximity). Ship the machinery and the contributors together, then
evaluate.

### Dependencies

- The carried-vs-ghost LOD already shipped (see
  [storage.md § Asymmetric LOD](storage.md)). It refactors into the
  generalized model.
- Kit prototype's bring + protection slice should be at least partially
  landed for `kit_member` and `kit_missing` contributors to have data.
- Storage areas land as part of this prototype (see Phase 3) — they are
  not a prerequisite from elsewhere.
- Classification `FacetIndex` (milestone 6 of
  [item-classification.md](../plans/item-classification.md)) is required
  for the classification-driven Triage suggestions in Phase 4. The LOD
  core does not depend on it.

### Phase 1 — Core machinery

Internal refactor; visible behavior should match today.

- Introduce a `RelevanceScore` abstraction with named, pluggable
  contributors. Refactor the existing carried-vs-ghost path so `carried`
  is the first contributor; same band output, no visible change.
- Replace the uniform-cell island layout with a weighted-grid packer
  that walks canonical order and emits heterogeneous-cell rows. With
  only `carried` wired, weights are still effectively binary, so the
  visible result matches today's layout.
- Apply the same packer one level up so islands repack by effective size
  at the atlas level.
- Add a relevance debug overlay: per-item score badge plus contributor
  breakdown, toggleable via hotkey or dev flag. Invaluable for the next
  phase.
- Plumb the reflow triggers (inventory open, kit activate / deactivate /
  page change, search submit / clear, area proximity change, explicit
  hotkey).

Phase 1 should land with no visible change to the player; if the atlas
looks different after Phase 1, something regressed.

### Phase 2 — Wire all available contributors in one cut

This is the phase that actually tests the idea. Land together:

- `carried` — already wired; promotes to score-driven
- `search_match` — extend the existing atlas search highlight to feed
  the score; band lift outranks `carried` so search results pop
- `recently_touched` — small inventory-event listener; medium weight,
  decays over a few seconds / inventory opens
- `kit_member` — items on the active Kit's belt / offhand / bring,
  read from existing Kit projection state
- `kit_missing` — items the active Kit wants but has nowhere
  (so wanted ghosts stay readable)
- `area_proximity` and `chest_holds_relevant` — depend on storage
  areas (Phase 3); land at the same time

Phase 2 + Phase 3 together are the prototype's evaluation point.
Splitting them leaves a half-tested model.

### Phase 3 — Storage areas (RETIRED — superseded by learned-storage)

> The `StorageArea` domain type, claim-flow area picker, and
> recursive chest-tile-inside-expanded-area LOD described below
> never shipped. The learned-storage swap (2026-04-30) replaced the
> direction with implicit auto-claim, derived clusters via
> `ChestClusterMap`, and chest chips rendered in a flex left-column
> panel. Chest contents surface as ghost atlas cards on their homed
> island when proximate; non-proximate stocks render via the
> chest-locator panel + the search-time `+N stored` corner badge.
> See [../plans/learned-storage.md](../plans/learned-storage.md).
>
> The text below is preserved for context only.

Player-facing change.

- Introduce a `StorageArea` domain type (id, name, ordered chest
  membership). One-shot migration: existing claimed chests move into a
  default "Main Base" area on first run. No atlas-coordinate changes.
- Update the claim flow to ask for / default an area at claim time
  (default = current-proximity area, fallback = Main Base, plus an
  inline "create new area" option).
- Areas render as chips by default. Chips show name + chest count + a
  carried-relevance badge when something inside is relevant.
- Areas expand on chest-proximity, search / shopping-list resolution
  hits, or explicit pin. Inside an expanded area, chest tiles follow
  the same LOD rules as atlas islands — proximity-near tiles render
  full content grid, others stay compact unless they hold high-scoring
  items.
- Carry the existing `Per-item chest presence` strip behavior forward,
  but answer in area-units when summarizing ("Main Base · 128 ·
  Mountain Mine · 32") instead of one chest at a time.

### Phase 4 — Classification-driven Triage suggestions

Adjacent to LOD; lands once `FacetIndex` is wired.

- Replace `IslandSuggestionService` signal source: from class / tag /
  component checks to facet lookups via `FacetIndex`.
- Add the confidence bands described in
  [Classification-driven placement](#classification-driven-placement).
- Track suggestion-acceptance rates per confidence band so the
  "rethink Triage's existence" stretch goal can be evaluated from
  real numbers.

### Evaluation criteria

The prototype is judged on whether these hold:

- Default-zoom inventory open shows the carried set legibly even when
  total identity count is in the thousands.
- Activating a Kit visibly shrinks non-Kit ghosts and grows
  Kit-relevant ghosts. The atlas reads as "Kit-shaped" while the Kit
  is active.
- Submitting a search visibly raises matches out of the local
  constellation; clearing returns to baseline cleanly.
- Walking up to a chest cluster lights its chip in the left panel and
  surfaces its contents as ghost cards on the homed islands; walking
  away dims back without a perceived framerate hit. (Earlier drafts
  expected a "storage area expand into chest tiles" sequence — that
  surface retired with the learned-storage swap.)
- Pickups during a session animate in locally without the whole atlas
  reflowing or "convulsing."
- The relevance debug overlay, when enabled, makes "why did this item
  get this size" answerable at a glance.
- Triage suggestion acceptance on the high-confidence band is high
  enough to inform the auto-home stretch goal decision.

If any of these fail consistently in playtest, that's signal to revisit
the model — re-tune weights, refine reflow triggers, or reconsider
score combination — before declaring the direction wrong.

### Out of scope for this prototype

Deferred until evidence warrants them:

- **`shopping_list` contributor** — depends on a shopping-list /
  craft-planner feature that doesn't exist yet. Wires in cleanly when
  that feature lands.
- **Per-player relevance weights.** Not worth considering until
  there's evidence specific players need them.
- **Anti-relevance / suppression contributors.** Easier to add later
  than to remove a misbehaving one.
- **"Max relevance everywhere" hold-modifier toggle** — implement only
  if playtesting shows pip-state visibility is genuinely insufficient.

## Open questions

- **Search performance and ergonomics at scale.** Search becomes the
  primary discovery path once ghost weight is near zero — that's
  desirable, since visual scanning at thousands of items isn't a
  tractable UX anyway. But it puts a hard requirement on search being
  *fast* and *easy*. Existing storage mods get search clunky because
  it's tied to individual containers; SLOT's atlas-wide
  facet-aware search should sidestep that, but worth measuring early
  in Phase 2 with a populated atlas.
- **Layout reflow cost at modded scale.** Weighted-grid packing must
  stay cheap when an island holds hundreds of items and the score
  distribution shifts on a coarse trigger. Worth measuring during
  Phase 1 before the contributor surface fans out.
- **Area-proximity granularity.** "Any chest in the area" expands the
  whole area; only chests near the player render contents. Probably
  fine, but could feel weird if a player sets up an area that spans
  hundreds of blocks. Refine on observed friction during Phase 3.
- **Confidence threshold for the auto-home stretch goal.** If
  classification-driven chip suggestions become extremely reliable,
  what acceptance rate / confidence band justifies revisiting "no
  silent auto-homing"? Decide from numbers during Phase 4, not in
  advance.

## Relationship to facet data

Several contributors lean on the
[classification dataset](classification/README.md):

- **Search match** can use facet predicates ("`role:tool material:iron`")
  in addition to plain text.
- **Kit member / kit missing** are direct identity matches today, but
  Kits could later declare facet-shaped intents (e.g., a Kit covers
  `activity:mining` regardless of which specific pickaxe is in the
  bring list).
- **Shopping list** decomposes recipes into ingredients; facets help
  surface where unowned ingredients come from (`origin`,
  `y_level_range`, `required_tool`).

These are forward-looking integrations. The model doesn't depend on any
of them; they're examples of how rich classification data composes with
the relevance score once `FacetIndex` is wired into the runtime
(see [classification milestone 6+](../plans/item-classification.md)).
