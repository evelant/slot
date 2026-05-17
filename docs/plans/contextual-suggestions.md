# Contextual Suggestions

Last updated: 2026-05-17

Status: first playable prototype landed; playtest diagnostics and tuning remain.
This narrows the broader ambient task view idea into two prototype lanes:
**Useful Now** and **Put Away**. The prototype persists behavioral aggregates
across sessions so real play can prove whether the suggestions become useful.

Related docs:

- [ambient-task-views.md](ambient-task-views.md) for the broader deferred
  task-lens idea.
- [classification-facet-vocabulary.md](classification-facet-vocabulary.md) for
  the facet data that can power contextual scoring.
- [retired/emi-goal-projections.md](retired/emi-goal-projections.md) for the
  retired explicit recipe-goal direction. Current EMI recipe context uses the
  transient sidebar filter from ADR 0007 instead.

## Implementation State

Landed 2026-05-15:

- common contextual signal records, item/context aggregates, learned event
  association indexes, station-open context capture, and workflow persistence
  under `WorkflowDomainFileStore` schema 9
- deterministic `ContextualSuggestionScorer` using recent strong item/station
  events, learned before/after item associations, player aggregate history, and
  narrow advisory `FacetIndex` accessors for `workflow`, `workflow_role`,
  `used_at`, and `processing_in`; carried state is no longer relevance
  evidence
- `SlotWorkspaceViewModel.ContextualSuggestionLane` projection with NeoForge
  and Forge NBT codecs
- compact Useful Now / Put Away sections pinned above the wall scroller on both
  loaders, reusing normal wall cards and gestures; empty Useful Now shows a
  waiting placeholder, Useful Now can include carried cards plus suggestion-only
  nearby storage ghosts, and Put Away forces the existing wayfinding strip when
  a non-proximate tracked chest containing that identity is known
- Useful Now excludes carried storage containers and no longer treats desired
  carry reservations as current-use evidence by themselves
- Put Away treats carried count above an explicit desired count as a strongest
  cleanup signal: the reserved desired amount remains protected, but excess is
  suggested for deposit
- carried state is candidate/action state only: it can make a card eligible for
  Useful Now or Put Away and provide source/action data, but it does not train
  associations or add relevance by itself
- station-open signals now contribute semantic context even when they do not
  name an item identity; `primary_uses`, `is_fuel`, and a small token bridge
  cover early helper-tool cases such as campfire/pot fuel activity suggesting
  flint and steel
- free-text token overlap ignores low-information words like `and`, so broad
  tooltip or primary-use prose cannot promote unrelated carried tools by
  sharing grammar instead of meaning
- tool-region station input/output changes now emit contextual item events from
  both loaders, so crafting grids and integrated tool panels make the changed
  item active context without relying on passive possession;
  station diffs use item id + count rather than component fingerprints, so
  heat/progress/damage NBT churn does not train Useful Now or force refreshes
- world item use now emits contextual signals from both loaders: right-click
  item/entity/tool attempts, entity attacks, block-break tool use, block
  placement, use-finish consumption, and item-destroyed durability events;
  right-click block placement by `BlockItem` is not also counted as a tool-use
  signal. Right-clicking storage/openable blocks while a tool happens to be in
  hand is ignored as low-information use, while real tool interactions such as
  wrenching Create machines still count. Targetless/air right-clicks keep exact
  held-tool relevance but no longer add target advisory context.
- learned before/after associations are intentionally narrow: station item
  moves can associate with other station item moves in the same context, and an
  explicit storage take can associate with a following station item move.
  Broad `STATION_OPENED`, `ITEM_USED`, acquisition, production, placement,
  consumption, deposit, and damage signatures do not train or replay association
  hints. Tool use still contributes exact recent relevance for that tool.
  Passive carried tools do not become hints merely because the player had them
  in inventory.
- the vanilla player inventory menu and carried-only portable/container menus
  are not treated as station context even when they expose tool-like regions;
  stale `InventoryMenu` and portable-menu station-content association buckets
  are pruned from the replayable association index
- nearby storage ghosts need exact/history relevance or a strong structured
  advisory match; weak text-only overlap such as generic fuel/fire terms cannot
  surface stored ghosts by itself
- saved goal plans are not currently emitted as Useful Now context; goal state
  remains a Put Away protection signal until goal capture is reliable enough to
  train suggestions
- deposit observations update cleanup history and apply a short exact-identity
  Useful Now penalty until a newer active signal re-promotes that item, so
  putting something away does not immediately imply it is useful again
- placement and consumption apply a short exact-identity spent penalty, so
  planting saplings or eating food does not pin that exact item as Useful Now
  unless newer stronger evidence re-promotes it
- non-tool right-click use remains context-only and does not replay learned
  before/after associations, which prevents older block-placement double-count
  history from keeping placed items alive
- recent contextual signals also decay by observed world tick when a current
  game tick is available, so an otherwise idle exact-use signal does not remain
  Useful Now forever just because no newer signal arrived
- suggestion lanes carry debug score/reason metadata through both loader codecs;
  a client config toggle can append detailed Useful Now / Put Away score inputs
  to item tooltips, including candidate flags, history/exact/advisory
  relevance, top matches, score terms, and Put Away history inputs
- `/slot debug contextual` dumps recent item/station events, learned
  associations, aggregates, and workspace/sidebar score breakdowns to the
  server log, including carried/proximate/elsewhere candidate source counts.
  The loader registries retain the last closed view model long enough for the
  command to be run after closing SLOT, since chat is not usable while the
  sidebar owns search input.
- saved goal tabs hydrate without auto-selecting a goal, keeping the All wall
  visible by default so contextual lanes are not hidden for an entire session
- tests for scoring, persistence round-trip, and both loader view-model paths

Still pending: Slice 3 tuning. The scorer is still deliberately plain and
conservative; use the richer tooltip diagnostics to tune thresholds from
playtest screenshots/logs before adding suppression state or player-facing
reason chrome. Remaining signal gaps are broader machine-specific output
collection beyond the tool/source/world-use observations already available
through the authority model. Fine-grained durability deltas are not tracked yet;
the first damage signal is item-destroyed only. Item-id text now uses only the
path portion, not the namespace, so broad mod ids such as `tfc:*` cannot create
context relevance by themselves.

## Product Frame

The current sectioned wall is a good fallback browser, but playtesting suggests
the highest-value surfaces are the ones that put relevant items in front of the
player automatically: Recent, search, desired/wanted gaps, and goal tabs.

This plan explores whether SLOT can infer short-lived player context from
behavior + facets and surface likely useful or likely put-away items before the
player has to browse.

The target is not "detect the player's one true activity." Real play mixes
contexts. A player can be alloying metal, then pause to cook food, then return
to the forge. The model should score item relevance against recent context,
not force behavior into named activity buckets.

## Core Concepts

### Useful Now

**Question:** What items is the player likely to want soon?

Useful Now is a compact suggestion lane that can surface carried items,
proximate storage ghosts, or known tracked-storage ghosts when current evidence
suggests they are relevant soon.

Examples:

- Picking up charcoal near a charcoal forge may suggest ore dust, molds, flux,
  crucibles, or relevant metalworking tools.
- Interacting with a cooking pot after picking up vegetables may suggest bowls,
  water containers, fuel, spices, or preserved ingredients.
- Opening a crafting station while a Kit is active may suggest missing
  Kit-related supplies or recent ingredients used in the same station context.

Useful Now should be conservative. It is allowed to be occasionally incomplete;
it should not flood the wall with speculative cards.

### Put Away

**Question:** What carried items are probably safe and useful to deposit now?

Put Away is a dedicated cleanup suggestion lane. It is not just another ambient
activity because cleanup pressure is universal: inventory space is limited in
every workflow, and players frequently carry items that should return to nearby
storage.

Put Away is the inverse of Useful Now. It favors carried items that appear
unrelated to the current context and are likely to be out-of-place clutter.
Having a confident nearby deposit route improves the card's immediate action
value, but is not required for suggestion. Items without a known route still
help the player notice that they need to find or create a home.

Strong candidates:

- carried items only, not storage ghosts
- not hotbar, offhand, armor, or other quick-access/equipment state
- not protected by the active Kit
- not wanted, desired, or goal-needed
- exception: carried count above an explicit desired count is a high-confidence
  Put Away candidate because only the desired amount is reserved
- not strongly related to current context signals
- low `carry_frequency` or other short/rare-carry facet priors when player
  history is sparse
- historically low carry frequency or short carry duration when player history
  exists
- usually deposited soon after pickup
- has a confident nearby deposit route through learned affinity or matching
  existing chest contents, when available
- low tool/equipment signal from facets

Items not selected for Put Away do not move to a separate cleanup bucket. They
remain in normal browsing.

## Evidence Signals

The first scoring model should combine behavior signals with facet priors.

Behavior signals:

- item taken from tracked or proximate storage
- item deposited into tracked or proximate storage
- item recently acquired from explicit pickup/take/reward signals; authority
  diff and passive internal inventory movement do not train Useful Now
- item crafted, damaged, or transformed
- item consumed or placed as spent context, not as exact self-promotion
- right-click world use attempts with a held non-block tool/item, even when
  final success is ambiguous
- machine, block entity, crafting grid, or workstation opened
- station inventory changed because the player moved related items/tools
- EMI recipe screen or SLOT goal tab recently active (deferred until those
  surfaces are reliable enough to train suggestions)
- repeated item/station/use transitions across nearby time windows

Facet signals:

- carry frequency / long-term carry likelihood
- workflow / workflow role
- used-at station or process context
- processing-in recipe evidence
- material or process stage
- food / cooking / preservation facets
- tool, equipment, protection, or container facets
- organization group as a broad fallback, not as the primary context signal

The model should treat `processing_in` as raw evidence and `used_at` /
workflow facets as stronger relevance context. `carry_frequency` should act as
the base prior for Put Away when SLOT has little or no player-specific carry
duration history.

## Signal Rules Matrix

Contextual scoring has three separate decisions:

- candidate/action state: can this identity appear in Useful Now or Put Away,
  and which source/action affordances can the card expose?
- relevance evidence: does this observation make an identity more or less
  useful right now?
- history learning: is this observation safe to persist as a before/after
  association that can influence future sessions?

Most bad suggestions have come from treating candidate state or low-information
UI noise as relevance evidence. Use this matrix as the implementation checklist
for new signals and tuning.

| Case | Examples | Suggestion rule | Learning rule | Status |
| --- | --- | --- | --- | --- |
| Passive carried state | food, axe, pickaxe, knife, supports, grappling hook always carried | Eligibility/action state only. Useful Now may show carried cards and Put Away is carried-only, but possession adds no Useful Now relevance. | Never train associations or context hints. | Implemented. |
| Internal carried moves | main inventory to backpack, backpack to main, sacks/baskets, hotbar reordering | Ignore for Useful Now relevance. Preserve source/action data for normal cards. | Never train. Audit new carried providers so their menus do not look like external stations. | Implemented for core carried state and carried-only host hints; keep auditing new provider menus. |
| External storage take or world pickup | taking an ingredient from a chest, picking up drops | Exact recent item relevance and clears newer deposit/spent penalties for that identity. | Storage take may train only when followed shortly by station item movement; plain pickup does not create broad associations. | Implemented. |
| Deposit into real storage | putting ores, gears, or supplies into chests/barrels | Negative Useful Now signal for that exact identity and positive Put Away/cleanup history. A newer take/pickup/use can re-promote it. | Do not teach future usefulness; deposits are cleanup evidence. | Implemented. |
| Station item movement | moving ingredients/tools in a workbench, machine, forge-like UI, or tool panel | Strong context for the moved identity and station. | May learn same-station moved-item relationships and explicit storage-take -> station-move relationships. | Implemented for observed station diffs; broaden per-station coverage only after testing. |
| Generic or carried-only menus | vanilla player inventory, SLOT/backpack workspace, sacks/baskets opened as carried storage | Not station context. Do not let their contents become Useful Now context. | Never train station-content associations. | Implemented through carried-only host hints plus explicit portable-menu context filters. |
| Real station opened | workbench, anvil, crucible, forge, machine menu | Context-only signal. Useful for station facets and exact later station movement, but opening alone should be weak. | Do not train before/after item associations from open events. | Implemented. |
| Held tool used on real target | wrenching a Create machine, axe/chisel/hammer/tongs use, entity attack | Exact recent relevance for the held item. Target context may add advisory tokens. | Do not train broad associations from tool use. | Implemented. |
| Held tool while opening storage/openable block | right-click chest/barrel/drawer/tool rack while wrench/knife/etc. is in hand | Low-information use; ignore as tool relevance so storage access does not pin the held item. | Never train. | Implemented for known storage/openable target names; extend marker list from logs. |
| Right-click air or failed world use | repeated `target=air`, ambiguous right-click item attempts | Exact held-item context only when the item itself matters; no target advisory boost from `air`. Rate/decay should prevent stuck suggestions. | Do not train associations. | Implemented for targetless/air advisory suppression; keep tuning exact-use decay from logs. |
| Placement and consumption | planting saplings, placing blocks, eating/drinking | Treat as spent context, not self-promotion. The exact identity should cool down unless newer evidence appears. | Do not replay placement/consumption associations. | Implemented. |
| Damage or item destroyed | durability loss, tool breaks | Useful as exact active-tool evidence and possible replacement/repair hint, not as proof nearby carried items mattered. | Do not train broad associations. | Partial: destroyed events exist; fine-grained durability deltas are still pending. |
| Advisory facet/text overlap | `fire`, `fuel`, `wood`, `used_at:campfire`, workflow text | Advisory only. It can explain/order a candidate, but broad text overlap should not be enough to surface unrelated storage ghosts by itself. | Never train from advisory overlap. | Implemented for storage ghosts through strong structured-advisory gating; carried-card advisory tuning remains playtest-driven. |
| Nearby storage ghosts | stored ingredients/tools within the proximate radius | Eligible for Useful Now only when there is meaningful recent relevance. Do not fill the lane with generic nearby materials. | Candidate presence never trains history. | Implemented for exact/history/strong structured relevance; keep tuning what qualifies as strong. |
| Tracked non-proximate ghosts | known storage elsewhere | May appear in Useful Now only from strong relevance or explicit target/goal state. Do not make Useful Now a remote storage browser. | Candidate presence never trains history. | Partial; keep conservative until proximate ghosts behave well. |
| Goals and recipe context | EMI recipe screen, SLOT saved goals | Ignore for training until those surfaces are reliable. Goal state can protect Put Away reservations. | Deferred; do not persist learned associations from rough goals. | Deferred by design. |
| Persisted historical noise | old broad `station_opened`, `item_used`, `InventoryMenu`, placement, consumption, or portable-menu buckets | Do not score polluted broad buckets. Prefer clear pruning over compatibility with bad history. | Reject non-replayable signatures during load/replay. | Implemented for known broad and portable-menu signatures; add versioned pruning if more polluted buckets appear. |

## Context Scoring

Do not require an enumerated set of concrete activities for the prototype.
There will never be a complete hand-authored list that describes every real
mixed player activity accurately enough.

Instead, score items against a latent, generic current-context model built from
recent events and facets:

- recent item identities and their facet vectors
- recent real station contexts, not generic inventory/backpack/SLOT hosts
- recent goal / recipe / Kit / wanted / desired context once those surfaces are
  reliable enough to train suggestions
- learned station-item and explicit storage-take associations
- decayed behavior signals
- player-specific active-use / deposit aggregates, once available

The output is item-level signal, not a named player activity:

- Useful Now: high positive relevance to current context.
- Put Away: carried item with low current-context relevance and high cleanup
  prior.

Human-readable activity labels may be derived from dominant facets later for
debugging or display, but they are not the driver. If shown at all, labels
should be explanatory metadata, not durable product objects or filtering
requirements.

Mixed context rule: new evidence can raise relevance for some item clusters
without clearing older signals. Switching from metalworking-like behavior to
food-prep-like behavior should make food items rise quickly while recent
metalworking items decay slowly if they are still plausibly in use.

Do not emit or score carried-set changes as contextual evidence. Carrying an
item is candidate/action state: Useful Now may show a carried card, and Put Away
is carried-only, but possession is not proof that the item is useful right now.
Player desired counts are also carry reservations, not Useful Now evidence
unless other recent behavior or context independently makes the item relevant.

Station/menu signals may not carry an item identity. They still need to add
context through the menu key, title, and metadata, and opening the same station
again should refresh context hints when the player picked up new carried items
since the previous observation.

## UI Shape

Prototype the UI as two compact sections above the normal wall scroller, pinned
with Recent rather than scrolling away with the main sections:

1. **Useful Now**
2. **Put Away**

Put Away should collapse when empty. Useful Now may show a compact waiting
placeholder while it has no behavioral context yet. Both sections should be
bounded so they cannot consume the whole workspace.

Initial player-facing cards do not need always-visible reason/status text.
Debugging reasons are exposed through a client config toggle that appends score
inputs, context-relevance breakdown, and top matching tokens to suggestion-card
tooltips.

Cards in both sections should behave like normal wall cards with the normal
gestures and context menu available. The suggestion lane changes visibility and
ordering, not card semantics.

Put Away has one display-only exception to the normal-card rule: a Put Away
card should show the chest finder chip for a tracked chest containing that
identity when that chest is not already proximate. Nearby matching chests are
already represented by the blue proximate-storage pip; that pip should also
appear as a route-only marker when a proximate chest can accept the item even
though it currently contains zero of that identity. This does not create a new
action path; it just makes "where does this probably belong?" visible while the
player is cleaning up. Lack of either marker is also useful information: the
player may need to find or create a home before quick deposit can work.

No automatic item movement should happen from either section. Suggestions may
boost visibility and expose actions; mutations remain explicit.

## Safety Rules

- Suggestions are projection only. They never create authority.
- Useful Now may reveal ghosts, but it does not grant remote mutation authority.
- Put Away does not require a confident deposit destination. Missing route
  confidence only means bulk deposit or quick deposit cannot work for that card
  yet; it does not mean the item is useful to keep carrying.
- Put Away must respect active Kit protection, desired-count reservations,
  wanted counts, goal-needed items, hotbar/offhand/equipment boundaries, and
  carried-source safety rules. Desired-count excess is the exception: if the
  player explicitly wants X carried and is carrying more than X, the excess is
  valid cleanup guidance.
- False positives should be easy to ignore and should not change deposit
  routing, home assignment, or Kit definitions unless the player explicitly
  chooses an action.
- Contextual suggestions should not duplicate EMI's recipe explanation surface.
  EMI remains the place to inspect recipes; SLOT suggests inventory/storage
  items relevant to current context.

## Existing Seams

Use the current workflow and wall boundaries instead of creating a parallel UI
or persistence system.

- `WorkflowDomainRuntime.recordActivityEvent(...)` already persists
  identity-bearing inventory events through the activity store.
- `InventoryActivityEvent` requires an item identity and count, so station-open
  and screen-context events should not be forced through it. Add a sibling
  contextual signal/aggregate model in `common/`.
- `WorkflowDomainFileStore` is the persistence boundary for workflow-domain
  state. New codecs for contextual signals or aggregates belong there, beside
  the domain data they encode.
- `SlotWorkspaceViewModel.project(...)` is the server projection point where
  authority, workflow state, storage ghosts, goals, wanted/desired counts, and
  card records already come together.
- `WallCardUiBuilder` / `WallSectionUiBuilder` provide the shared normal card
  and section rendering used by both loaders. Suggestion lanes should reuse
  those builders.
- Loader code should only emit platform facts that common cannot know, such as
  "this crafting/machine screen opened" or "this host menu changed." Scoring,
  aggregation, protection, and candidate selection stay in common.

## Data Model

### Contextual Signal Events

Add a small common event type for non-authoritative context facts that do not
fit `InventoryActivityEvent`.

Candidate fields:

- `kind`: `STATION_OPENED`, `STATION_CONTENTS_CHANGED`,
  `GOAL_CONTEXT_OBSERVED`, `RECIPE_CONTEXT_OBSERVED`, `ITEM_ACQUIRED`,
  `ITEM_TAKEN_FROM_STORAGE`, `ITEM_DEPOSITED_TO_STORAGE`,
  `ITEM_CRAFTED_OR_PRODUCED`, `ITEM_USED`, `ITEM_PLACED`, `ITEM_CONSUMED`,
  `ITEM_DAMAGED`
- `globalSequence`: use the existing workflow sequence machinery so ordering is
  comparable with activity records
- `identity`: optional, present for item-scoped signals
- `count`: optional count/sample value
- `contextKey`: stable string for station/menu/recipe/goal context
- `contextLabel`: debug-only display label, not scoring authority
- `sourceKey`: optional stable source/menu/storage key
- `metadata`: origin/correlation fields matching existing domain metadata style

Do not model concrete player activities here. These events are raw observations.

### Persistent Aggregates

Persist compact aggregates from the first playable slice. Keep raw contextual
event history bounded and useful for diagnostics, but do not rely on a long
event log for scoring.

Per-item aggregate:

- `identity`
- `timesAcquired`
- `timesTakenFromStorage`
- `timesDepositedToStorage`
- `timesCraftedOrProduced`
- `timesUsed`
- `timesPlaced`
- `timesConsumed`
- `timesDamaged`
- `lastActiveSequence`
- `lastAcquiredSequence`
- `lastDepositedSequence`

Per-context aggregate:

- `contextKey`
- `timesSeen`
- `lastSeenSequence`
- label and bounded diagnostic hints, not scoring authority

Association index:

- normalized replayable event signature, currently `station_contents|...` or
  `item_taken|...`
- bounded top item hints likely to follow that signature
- count, score, last sequence, and average sequence delta per hint

Persistence rule: persist aggregates and enough recent raw signals to debug bad
suggestions. Do not persist candidate scores, lane membership, or human-readable
activity labels; those are derived projection state.

Compaction rule: bound every map/list. Prefer decayed counters or top-N retained
signals over unbounded per-item history.

### Projection Records

Add suggestion output to the workspace view model as projection data, not
authority.

Candidate shape:

```text
ContextualSuggestionLane(id, label, itemRefs)
```

or, if hidden ghost cards cannot be resolved from ordinary wall items:

```text
ContextualSuggestionLane(id, label, atlasItems)
```

The UI should still render normal `AtlasItem` cards. If a suggestion identity
also appears in the normal wall, the same card data can be shared. If a hidden
storage ghost is suggested, projection may include it in the suggestion lane
without forcing its normal section to expand.

Debug-only candidate data can include score and reason tokens, but those should
not be required by the player-facing renderer.

## Event Capture

### Identity Events

Start from existing authoritative outcomes and storage observers:

- `InventoryActionOutcome.activityEvents()` for SLOT-initiated transfers
- deposit/take paths that already pass through `DepositExecutor`,
  `WorkspaceChestCommandService`, or provider-aware action execution
- existing recent/acquired activity records

Audit for missing take/deposit activity events rather than adding a second
observer that can disagree with the action pipeline.

### Carried State

Do not emit carried-set changes as contextual signals. Carried state is already
available in the workspace projection and should remain eligibility/action data:
Useful Now may show carried cards, and Put Away is carried-only, but passive
possession must not train relevance.
- Save on boundary changes, not every tick.

This gives Put Away real carry-duration history across sessions while avoiding
constant persistence churn.

### Station And Screen Context

Add platform-specific emitters for screen/menu context that common cannot
derive.

First pass:

- crafting table / player crafting grid opened
- furnace-like or machine-like menu opened where the platform can identify a
  stable menu/block key
- EMI recipe context or SLOT goal context already exposed by existing goal
  integration

Later pass:

- station inventory changed while open
- item inserted/extracted from station slot
- machine output collected

All platform emitters should call a common service with raw context keys. Common
decides how much the signal affects scoring.

## Scoring Contract

The scorer should be deterministic, testable, and boring. No ML runtime is
needed for the prototype.

### Candidate Universe

Useful Now candidates:

- carried cards
- proximate storage ghosts
- tracked-storage ghosts when already known to the view model
- goal/wanted/desired/Kit-relevant placeholders when they already have normal
  card semantics

Put Away candidates:

- carried cards only
- exclude hotbar, offhand, armor, and other quick-access/equipment state
- exclude active Kit protected identities
- exclude wanted, desired, and goal-needed identities

Do not require a known deposit route for Put Away. Destination confidence is a
boost and a card affordance, not an eligibility gate.

### Facet Vector

Build a small weighted token vector per item from classification data:

- `used_at`
- `workflow`
- `workflow_role`
- `processing_in`
- material / process stage facets
- food / cooking / preservation facets
- tool, equipment, protection, and container facets
- `carry_frequency`
- organization group as a weak fallback only

If the current `FacetIndex` does not expose a needed facet yet, add a narrow
accessor there rather than duplicating classification parsing in the scorer.

### Current Context Vector

Build a decayed context vector from:

- recently acquired/taken/deposited identities
- recently opened real station context keys, excluding generic carried-only
  SLOT/backpack/inventory hosts and the vanilla player inventory menu
- recent goal / recipe / Kit / wanted / desired context once those surfaces are
  reliable enough to train suggestions
- learned before/after associations from station item movement and explicit
  storage take before station use

Signals should decay, but not disappear instantly. A temporary food-prep detour
should not erase the metalworking-like context if the player is still taking
metalworking-like actions.

### Useful Now Score

High-level shape:

```text
useful_now =
  learned_history_votes
  + recent_exact_item_activity
  + facet_advisory_overlap
  + explicit_goal_or_target_boost
  - recent_deposit_penalty
  - recent_placed_or_consumed_penalty
  - noise_penalties
```

Include already-carried items and nearby stored ghost items. Useful Now is a
quick-access surface for the things needed now, not only a fetch list. Carried
status affects eligibility and actions, not relevance.

### Put Away Score

High-level shape:

```text
put_away =
  cleanup_prior_from_carry_frequency
  + player_history_short_carry_or_fast_deposit
  + inventory_pressure_boost
  + optional_destination_confidence_boost
  - current_context_relevance
  - protection_or_target_penalties
  - tool_equipment_long_carry_penalties
```

`carry_frequency` is the base value when player-specific carry history is
missing. Player history can override it gradually, but the first playtest build
should already produce meaningful Put Away cards from facet priors alone.

### Thresholds And Stability

- Cap each lane to a small number of cards so suggestions cannot consume the
  wall.
- Reserve room for qualifying nearby-storage ghosts in Useful Now so a cluster
  of recently used carried tools cannot crowd out every relevant stored item.
- Use separate enter/exit thresholds or short hysteresis so cards do not flicker
  on every projection refresh.
- Preserve lane order by score, then stable item identity order.
- Keep debug score/reason output available for `/slot` diagnostics or logs.

## Prototype Slice

### Slice 0 — Persistent Model And Scorer — Landed

- add common contextual signal / aggregate records
- extend workflow persistence to save/load aggregates and bounded recent
  context signals
- add deterministic scorer tests with hand-authored facet fixtures for
  metallurgy-like and cooking-like item sets
- prove Useful Now includes already-carried relevant items
- prove Put Away works with zero player history through `carry_frequency`
- prove Put Away does not require a destination route
- prove mixed context scoring with the metallurgy + food-prep interruption
  example

### Slice 1 — Event Capture — Landed

- connect existing activity outcomes to the aggregate updater
- add station/item/use/deposit event capture without passive carried observation
- add first platform screen/station-open emitters for both loaders
- persist aggregates across client/server restart in a dedicated test or manual
  verification path

### Slice 2 — Projection And UI — Landed

- extend `SlotWorkspaceViewModel` with suggestion lanes
- update NeoForge and Forge view-model codecs
- render Useful Now and Put Away above the normal wall using normal card
  builders
- do not add special gestures or context menu actions
- on Put Away cards, show the chest finder chip for a tracked chest containing
  that identity when one exists outside the proximate set
- show the proximate-storage pip as a route-only marker when a matching
  proximate chest can accept the item but currently holds zero of it

### Slice 3 — Diagnostics And Playtest Tuning — Partly Landed

- use detailed tooltip debug output to tune top candidates, scores, and reason
  tokens
- keep testing the signal rules matrix against playtest logs, especially exact
  tool-use decay and the threshold for strong storage-ghost advisory matches
- tune thresholds from playtest screenshots/logs
- only then consider suppression/correction state, search zero-state changes,
  or richer station-content events

## Open Questions

Resolved for the first prototype:

- Inventory event abstraction mostly belongs in `common/`; platform-specific
  work is likely needed for crafting and machine-open events.
- Put Away should work with zero carry-duration history by falling back to
  `carry_frequency` and related facet priors.
- Do not implement Put Away suppression in the initial version. Revisit only if
  false positives become noisy.
- Useful Now should include already-carried items. Quick access to what is
  useful right now is the point; fetch-only suggestions would force browsing
  for carried-but-relevant items.
- Leave search zero-state alone for the initial version.
- Do not seed individually defined activities. Use generic context scoring.
- The fresh facet pipeline should provide thorough enough `used_at` / workflow
  coverage to attempt metallurgy and cooking fixtures.

Still open:

- Does inventory fullness change Put Away thresholds?
- Which crafting and machine interaction events are reliable on both loaders?
- Is the current strong structured-advisory gate for storage ghosts too strict
  for real cooking/forge workflows, or still too permissive for generic fuel?
- Are the detailed tooltip diagnostics enough to explain scoring mistakes, or
  do playtest logs need a parallel `/slot` dump for candidates that did not
  make a lane?
