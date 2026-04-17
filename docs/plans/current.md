# SLOT Current Implementation Plan

Last updated: 2026-04-16

This is the near-term engineering sequence from the current baseline. For the
short operational handoff, read [../status.md](../status.md) first.

For product goals, see [../product/direction.md](../product/direction.md).
For current architecture, see [../architecture/overview.md](../architecture/overview.md).
For action semantics, see
[../architecture/action-taxonomy.md](../architecture/action-taxonomy.md).
For the LDLib2 workspace decision, see
[../decisions/0002-ldlib2-workspace.md](../decisions/0002-ldlib2-workspace.md).
For the triage/home design, see [../design/atlas.md](../design/atlas.md).

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
- opening the atlas on a fresh profile shows only the Triage island with
  every item inside it

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
- a fresh profile opens to Triage only, with no pre-seeded non-Triage islands
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
