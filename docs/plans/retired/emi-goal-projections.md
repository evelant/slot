# EMI Goal Projections Plan

> Retired 2026-05-16. Replaced as the near-term EMI surface by
> [ADR 0007](../../decisions/0007-emi-recipe-sidebar.md): when an EMI recipe
> screen is open, SLOT shows the normal sidebar filtered to visible recipe
> ingredients. Do not resume this plan unless playtesting proves a persistent
> recipe-goal surface is needed.

Last updated: 2026-05-12

Status: in progress; Slice 0 contract spike completed 2026-05-11, Slice 1 common
projection model landed 2026-05-11, Slice 2 fixture goal-tab UI landed
2026-05-11, and Slice 3 EMI context integration landed 2026-05-11. Playtest
stabilization passes are now in progress. This plan owns the runtime/UI side of
recipe goals: SLOT-side recipe goals captured from EMI recipe context become
server-persisted goal tabs, and each tab projects the normal SLOT wall to show
what the player has, what is known in storage, and what is missing for the
selected recipe goal. Future EMI favorite/pin ingestion can be added if EMI
exposes a stable public API for it; the first implementation must not depend on
internal favorite state.

This plan deliberately does not own vocabulary generation or generic ambient
task views. See [classification-facet-vocabulary.md](../classification-facet-vocabulary.md)
and [ambient-task-views.md](../ambient-task-views.md).

## Product Boundary

SLOT should not become a recipe browser. EMI remains the place to inspect
recipes, usages, alternatives, machines, and recipe explanations.

SLOT should not recreate EMI recipe data structures unless the contract spike
proves there is no stable way to reuse the relevant EMI concepts. The intended
split is:

- EMI owns recipe discovery, recipe/category screens, recipe tree semantics,
  tag/list ingredient display, recipe choice/resolution UI, and recipe metadata
  such as catalysts, outputs, and remainders
- SLOT owns authority projection across carried inventory and claimed storage,
  ghost/missing cards, wayfinding, goal-scoped wanted-count overlays, and
  SLOT-specific diagnostics

SLOT adds value by connecting an EMI-selected goal to real inventory authority:

- carried items render as normal cards
- storage-known requirements render as ghosts with pips and wayfinding signals
- absent requirements render as missing ghosts without pips
- wanted counts use main's carried-target display for the active goal
- tooltips explain goal status and recipe-chain position
- `R` / `U` delegates recipe and usage explanation back to EMI where supported

## First Target Flow

1. The player uses a SLOT "Add recipe goal" affordance from an EMI recipe
   context for `Coke Oven`.
2. SLOT shows a `Coke Oven` goal tab beside `All` and any ambient views.
3. The player selects the `Coke Oven` tab.
4. The normal SLOT wall is projected to coke-oven-related cards: carried
   inputs, storage-backed ghosts with pips, missing ghosts with no pips, and
   choice cards only where a tag/list ingredient still needs a decision.
5. The goal output count defaults from the captured recipe output.
6. The player can ctrl-scroll the goal tab or output card to request more.
7. Wanted counts update only for the selected tab and only for the additional
   amount still needed after current authority is subtracted.
8. If the player already has items that satisfy a tag/list ingredient, SLOT
   applies those concrete items automatically and marks the cards with a small
   choice indicator instead of forcing a decision up front.
9. Hovering a ghost explains its status and recipe-chain position, for example
   `clay ball -> unfired coke brick -> coke brick -> coke brick block -> coke oven`.
10. Pressing `R` or `U` on a goal card opens EMI recipe/use detail.

## Goal Projection Model

A goal projection is not just a filter. It can add ghosts for item identities
the player has never carried or stored.

Inputs:

- SLOT goal descriptor captured from an EMI recipe context
- target output count for that goal
- adapted EMI recipe data and SLOT-recorded alternatives/resolutions where
  available
- optional goal-scoped manual ingredient choices
- current authority snapshot: carried inventory, hotbar, offhand, proximate
  claimed chests, elsewhere claimed chests

Outputs:

- goal tab metadata
- goal membership for real cards already present in authority
- storage-backed ghost entries for requirements present only in known storage
- missing ghost entries for requirements absent from known storage
- choice-card entries for unresolved ingredient groups
- concrete auto-resolutions for ingredient groups satisfied by visible authority
- goal-scoped wanted counts
- tooltip chain breadcrumbs
- choice indicators and context-menu metadata for cards that came from a
  tag/list ingredient decision

Wanted counts are scoped to the active tab. They use main's carried-target
semantics, but the target is derived from the goal's additional missing amount.
If the goal needs 27 coke bricks and the player has 12 visible across carried
and known storage, the projection asks only for the missing 15. If some of the
visible count is already carried, that carried amount contributes to the
displayed `M/N` target so main's wanted-count auto-clear behavior stays correct.

Goal-scoped wanted counts use the same atlas badge/tooltip surface as main's
wanted-count feature, but they are projection output for the active tab rather
than persisted player targets. They should not feed deposit protection or kit
logic unless a later slice explicitly promotes that behavior after playtest.

Each goal is independent. SLOT should not reserve items across goals and should
not calculate cross-goal conflicts. Selecting another goal tab recalculates and
displays that goal's wanted counts against the current authority snapshot.

## Recipe Expansion Rules

The resolver/adapter should reuse EMI's recipe tree and resolution semantics as
far as the EMI contract allows. If SLOT has to own a common projection model, it
should represent EMI concepts faithfully rather than inventing a parallel recipe
browser.

Recursive expansion must stay bounded and explicit:

- expand from the SLOT goal descriptor captured from EMI recipe context
- subtract visible authority before assigning upstream wanted counts
- detect loops and reversible transforms
- preserve substitute groups instead of flattening them too early
- auto-resolve substitute groups from visible concrete authority before showing
  a choice card
- avoid expanding every possible combination for broad tags
- preserve catalysts, remainders, chance outputs, fluids, and multi-output facts
  when EMI exposes them; otherwise fail closed instead of pretending they do not
  exist
- fail closed with diagnostics when recipe data is incomplete

The first implementation should be deliberately narrow:

- one active SLOT-side recipe goal captured from EMI context
- item ingredients
- simple tag alternatives
- bounded recursion
- carried and claimed-storage counts
- no inventory mutation
- no quest/Patchouli/advancement goals
- no workstation instruction UI
- no compact goal header until playtesting proves the wall cards alone are not
  enough
- no generated "goal kit"; hotbar loadouts are too personal to synthesize from a
  recipe goal

## Choice Cards And Tag Resolution

Ingredient alternatives can explode. SLOT should expose ambiguity without
building a parallel tag browser.

Rules:

- choice cards are not storage ghosts; they are explicit "decision required"
  cards for unresolved recipe ingredient groups
- if visible matching items satisfy the choice, show the concrete item cards
  automatically and do not force the player through a tag card
- if visible matching items cover only part of the deficit, show those concrete
  cards automatically, then show one choice card for the unresolved remainder
- if more than one visible item can satisfy the same choice, use a deterministic
  visible-authority order for the automatic projection and mark the involved
  concrete cards; the context menu lets the player choose differently
- if no concrete matching item is visible, show one choice card with the
  tag/ingredient label, unresolved count, and a clear action-needed indicator
- a choice card should explain that it is a recipe alternative group, not a
  known stored item
- opening the choice card should delegate selection/details to EMI where
  possible
- concrete cards that came from automatic or manual choice resolution get a
  small choice pip/indicator
- right-clicking a choice-involved concrete card should offer "choose a
  different ingredient" and, for manual choices, "clear manual choice" so SLOT
  falls back to automatic visible-authority resolution

Example label:

```text
Any #forge:plates/iron x4
```

## UI Behavior

Entry points:

- goal tabs derive from SLOT-side recipe goals captured from EMI recipe context;
  passive EMI favorite/pin ingestion is deferred until EMI exposes a stable
  public listing/observation API
- ctrl-scroll on a goal tab or goal output card adjusts target count
- `All` remains the default wall
- ambient task views, if enabled later, are separate from EMI goal tabs

When a goal tab is active:

- the same SLOT wall renders, scoped to the goal projection
- carried items render as carried items
- storage-known requirements render as ghosts with pips
- absent requirements render as missing ghosts with wanted counts and no pips
- choice cards represent unresolved ingredient groups
- concrete cards involved in an ingredient-group decision show a compact choice
  indicator and context-menu entries for changing the choice
- search filters within the active goal projection
- pips and wayfinding keep their normal meaning

The goal tab should answer "what do I need, what do I already have, and where is
it?" It should not explain the whole recipe chain in visible text. Do not add a
separate compact goal header in the first pass; keep the surface close to the
normal wall unless playtesting shows the goal state needs dedicated chrome.

## Implementation Slices

### Slice 0: EMI Contract And Reuse Spike

- verify what EMI exposes for recipe favorites/pins, recipe context, pin/favorite
  order, change observation, and persistence
- inspect EMI's BoM/material-tree APIs and decide whether SLOT can reuse them,
  mirror their outputs, or must adapt raw `EmiRecipe` inputs into SLOT records
- verify opening EMI recipe, usage, tag, and list-ingredient views from SLOT
  cards
- verify how EMI records tag/list resolutions and recipe choices, and whether
  SLOT can receive or observe those choices without unstable internals
- verify loader/version availability for the target NeoForge 1.21.1 and Forge
  1.20.1 tracks
- document which EMI types stay behind the optional compat adapter and what
  common receives as SLOT-owned descriptors

Exit criteria:

- the plan knows whether "pin" means EMI favorite, recipe tree goal, another EMI
  surface, or a SLOT-side goal derived from EMI context
- the plan knows which EMI tree/resolution behavior is reused directly and which
  behavior must be represented by SLOT descriptors
- unsupported or internal-only EMI paths are identified before any UI work starts

#### Slice 0 Findings

Evidence checked:

- current SLOT loader setup: `gradle.properties`, `neoforge/build.gradle`,
  `forge-1.20/build.gradle`, both EMI plugin classes, and both mod metadata files
- resolved API jars:
  `emi-neoforge-1.1.22+1.21.1-api.jar` and
  `emi-forge-1.1.22+1.20.1-api.jar`
- upstream EMI source tags `1.1.22+1.21.1` and `1.1.22+1.20.1`; the relevant
  runtime files matched between the two tags
- Context7 EMI docs for public runtime navigation and recipe manager behavior

Public, stable-enough API:

- `EmiApi.getRecipeManager()` plus `EmiRecipeManager` give read-only recipe
  lookup by id, input, output, category, and full recipe list.
- `EmiRecipe` exposes ids, categories, inputs, catalysts, outputs, display
  widgets, `supportsRecipeTree()`, and backing vanilla recipe where available.
- `EmiIngredient` and `EmiStack` expose amount, chance, alternatives,
  item/fluid stack ids, remainders, item stacks, and target-version-specific
  component/NBT data.
- `EmiIngredientSerializer.getSerialized(...)` is the best public bridge for
  preserving tag/list ingredient shape at the adapter boundary: item stacks,
  tags, and lists serialize through EMI's registered serializers without SLOT
  importing internal ingredient classes.
- `EmiApi.displayRecipe(...)`, `displayRecipes(...)`, `displayUses(...)`,
  `viewRecipeTree()`, and `focusRecipe(...)` are supported delegation points for
  recipe/usage/tree UI.

Internal-only or unsupported for SLOT:

- EMI favorites are runtime classes (`EmiFavorite`, `EmiFavorites`) and are not
  present in the published API jar. EMI persists them in `emi.json`, but no
  public API lists favorites, exposes favorite order, or observes favorite
  changes.
- EMI's Bill-of-Materials state is runtime-only (`BoM.tree`, `MaterialTree`,
  `MaterialNode`, `TreeCost`). Public API can open the BoM screen, but cannot
  read the current goal, set a goal, read costs/progress, observe batch-count
  changes, or observe resolution changes.
- EMI recipe/tree choices are stored through internal `BoM` default/added/
  disabled recipe maps and `MaterialTree.resolutions`; SLOT should not import or
  reflect those internals for production behavior. EMI's public recipe display
  path uses `BoM.getRecipe(...)` internally to focus the default recipe, but the
  default map itself is not exposed through the published API jar.
- Concrete `TagEmiIngredient`, `ListEmiIngredient`, `ItemEmiStack`, and
  `FluidEmiStack` classes are annotated internal upstream and absent from the
  API jar. Adapter code may handle their serialized form, but common must not
  depend on their classes.

Contract decisions:

- For the first integration, "pin" cannot mean passively reading EMI's favorite
  sidebar. It must mean a SLOT-side goal created from an explicit EMI recipe
  context action, a hovered/focused EMI recipe context, or another future
  public EMI API if one appears.
- SLOT should reuse EMI for recipe discovery and display delegation, then adapt
  public `EmiRecipe` / `EmiIngredient` / serialized ingredient data into
  SLOT-owned descriptors. Common receives only those descriptors, never EMI
  objects.
- SLOT should implement its own bounded projection/tree resolver in common for
  inventory-authority projection. EMI's BoM code is useful behavioral reference
  for loops, catalysts, remainders, chance outputs, default recipes, and
  visible-inventory subtraction, but the runtime classes stay outside the
  product path.
- Manual ingredient choices must be SLOT-owned for now. The adapter can reopen
  EMI recipe/usage/list/tag views for explanation, but it should not pretend it
  can subscribe to EMI choice state.
- Loader availability is acceptable for both active tracks: both modules already
  compile against EMI `1.1.22` API jars and already register optional
  `EmiPlugin` exclusion areas. The compat adapter should live under each
  loader's optional EMI package and feed common descriptors through a narrow
  loader-neutral interface.

Descriptor boundary for Slice 1:

- `common/` should receive a goal descriptor containing goal id, display label,
  target output stack(s), target count, focused recipe id/category, and a list
  of adapted recipe descriptors.
- Each recipe descriptor should contain recipe id, category id, supports-tree
  flag, outputs, inputs, catalysts, and diagnostics for unsupported fluids,
  chance outputs, multi-output facts, remainders, or unserializable ingredients.
- Each ingredient descriptor should carry quantity, chance, serialized EMI
  ingredient text/JSON for adapter round-tripping, concrete stack alternatives,
  optional tag/list label when serialization exposes it, and an explicit
  `choice_required` bit when more than one alternative remains.
- The optional EMI adapter keeps `EmiRecipe`, `EmiIngredient`, `EmiStack`,
  `EmiIngredientSerializer`, and all display-delegation calls. Common gets no
  `dev.emi.*`, `net.minecraft.*`, or loader imports.

### Slice 1: Projection Adapter Fixture

- define `GoalProjection`, requirement, and choice/tag requirement records in
  common
- add fixture or captured EMI-adapter data that simulates one EMI recipe goal
  and target count
- expand/project through a bounded recipe tree with tag alternatives preserved
- compare requirements against carried and claimed-storage authority
- subtract visible authority to compute goal-scoped wanted counts
- dump a normal-wall projection with real cards, storage ghosts, missing ghosts,
  choice cards, choice indicators, and tooltip breadcrumbs

Exit criteria:

- one fixture recipe goal produces useful wanted counts without changing homes
  or moving items
- visible matching items auto-resolve ingredient choices, while unresolved
  remainders stay visible instead of being silently flattened
- loops/depth limits fail closed with diagnostics

Implementation notes (landed 2026-05-11):

- `common/src/main/java/dev/imagio/slot/inventory/goal/` now owns EMI-neutral
  goal descriptors, recipe/ingredient descriptors, visible-authority counts,
  projection entries, choice requirements, and a bounded
  `GoalProjectionService`.
- The service consumes a SLOT-owned descriptor graph and `GoalVisibleAuthority`
  counts, subtracts visible carried/storage authority, expands missing
  craftable requirements, preserves unresolved alternatives as choice cards, and
  emits goal-scoped wanted counts only for unresolved concrete leaves.
- Common descriptors carry `ItemIdentity`, labels, counts, serialized ingredient
  text, alternatives, and diagnostics. They do not carry EMI objects or loader
  classes.
- `GoalProjectionServiceTest` covers the fixture recipe goal, storage ghosts,
  missing ghosts, choice indicators/cards, useful wanted-count overlays, and
  fail-closed loop/depth-limit diagnostics.

### Slice 2: Goal Tab UI Spike

- add a fixture-backed goal tab beside `All`
- render goal projection entries through the existing wall/card path
- add tooltip status for storage-backed ghosts, missing ghosts, choice cards,
  and choice-involved concrete cards
- support ctrl-scroll target-count adjustment
- delegate `R` / `U` from goal cards to the existing EMI path if one exists;
  otherwise log a clear unsupported diagnostic
- add right-click menu entries for changing or clearing a choice resolution where
  the adapter can support it
- avoid adding a compact goal header in this slice

Exit criteria:

- selecting the fixture goal tab narrows/augments the wall without changing
  homes
- wanted counts appear only while that goal tab is active
- storage-backed and missing ghosts are visually distinguishable
- choice cards and choice indicators are visually distinct from normal storage
  pips

Implementation notes (landed 2026-05-11):

- `GoalWorkspaceProjection` adapts the Slice 1 fixture projection into normal
  `SlotWorkspaceViewModel.AtlasIsland` / `AtlasItem` records so the existing
  wall/card renderers can show carried requirements, storage-backed ghosts,
  missing ghosts, unresolved choice cards, goal-scoped `M/N` badges, and choice
  indicators without changing stored homes.
- NeoForge LDLib2 and Forge 1.20 both render an `All` tab plus a fixture
  `Coke Oven` tab. Selecting the goal tab swaps the wall/search/TOC projection
  locally; selecting `All` returns to the normal view-model projection.
- Ctrl-scroll on the goal tab or a goal card adjusts the fixture target count.
  In goal mode, card pickup, hotbar assignment, re-home, gather, and section
  edit gestures fail closed as browse-only instead of mutating inventory.
- Goal card tooltips add status, visible authority counts, missing counts,
  breadcrumbs, diagnostics, and recipe-alternative notes. Slice 2 initially
  surfaced pending EMI-adapter diagnostics for right-click and `R` / `U`; Slice
  3 replaced those with EMI delegation where supported.

### Slice 3: EMI Context Integration

- use the Slice 0 contract decision for explicit EMI recipe-context goal
  creation
- map EMI recipe ingredients into the common requirement records
- handle recipe ids, outputs, ingredient alternatives, and focused output
- handle SLOT-recorded ingredient resolutions and delegate explanations back to
  EMI where supported
- update or remove fixture-only code

Exit criteria:

- an explicit EMI recipe-context action creates a SLOT goal tab
- removing a SLOT-side recipe goal removes the corresponding goal tab
- unsupported EMI data fails closed with diagnostics

Implementation notes (landed 2026-05-11):

- NeoForge and Forge EMI plugins now register an always-visible `SLOT+`
  overlay button on EMI recipe screens plus a drag/drop goal target for EMI
  stacks. The recipe-screen button reads the currently displayed EMI recipe
  group and adapts that recipe through public EMI API (`EmiRecipe`,
  `EmiRecipeManager`, `EmiIngredientSerializer`) into the common
  `GoalDescriptor`. Dragging an EMI stack onto the SLOT sidebar, or onto the
  small `SLOT goal` drop target shown while dragging on non-SLOT screens, uses
  the same adapter path from EMI recipe context or the first public output
  recipe. Both paths save/activate a SLOT goal tab and open the SLOT workspace
  when needed.
- `GoalWorkspaceClientState` owns active-tab state and local editing state while
  the UI is open, but goal tabs themselves are persisted through workflow-domain
  `GoalPlanState` events so reconnects restore the player's recipe goals. The
  former fixture tab path is no longer shown in normal UI; `All` plus persisted
  EMI-created tabs drive both LDLib2 and Forge surfaces.
- The adapter records item outputs, concrete alternatives, tag/list
  ingredients, bounded recursive child recipes, non-item output/alternative
  diagnostics, and recipe-tree support flags. It intentionally bounds recipe
  depth and alternative expansion instead of trying to solve the whole pack.
- Goal card context actions and `R` / `U` now delegate through clicked-card or
  choice-group metadata where supported. The remaining work is real-pack
  validation of recipe capture, invalid-choice feedback, fluid placeholders, and
  recursive producer coverage.

## Playtest Handoff: 2026-05-12 Goal Projection Stabilization

Current code state:

- Slices 0-3 are present in the worktree: common goal descriptors/projection,
  persisted goal tabs, explicit EMI `SLOT+` recipe-screen goal creation, and EMI
  drag/drop goal creation exist on both loaders.
- Goals and producer recipe defaults are now workflow-domain state. The client
  still owns transient UI state such as the pending EMI recipe-capture flow, but
  reconnect should restore saved goals and per-output recipe defaults.
- The feature is still a playtest spike, not a ready base for extra chrome. The
  next slice should be a root-cause validation pass against real recipes, not a
  new surface.
- Manual ingredient choices are required by this plan. SLOT cannot observe EMI's
  internal BoM choice state, so goal-scoped recipe-choice state must be
  SLOT-owned. Do not treat choice controls as dead scope to remove; make them
  real and honest.

Plan invariants to keep while fixing:

- A goal tab is the normal SLOT wall scoped to the active goal projection. Do
  not invent recipe-depth sections or a special goal-only layout.
- Goal projection is more than a filter: it may add storage-backed ghosts,
  missing ghosts, unresolved choice cards, and goal-scoped wanted-count
  overlays for items absent from the normal wall.
- Wanted counts use carried-target semantics but are derived from additional
  amount needed after visible authority is subtracted, and only in the active
  goal tab.
- Choice cards represent unresolved tag/list ingredient groups. Concrete
  authority that satisfies a tag/list should auto-resolve first and get a choice
  indicator; the player must still be able to concretize or override the choice.
- EMI remains the recipe explanation and discovery surface. SLOT should delegate
  recipe/usage/details back to EMI, but SLOT owns projection, authority
  subtraction, and manual choice state.

Observed bugs and gaps:

Fixed in the first bug pass:

- Goal-only cards no longer create a synthetic `Goal` section. Existing cards
  stay in their normal sections; unseen ghosts and choice cards fall back to the
  normal `Misc` section.
- Repeated requirement entries aggregate by identity for the card surface while
  raw projection entries remain available for diagnostics.
- Missing concrete requirements render as needed cards even when the item is
  absent from authority. If a concrete requirement or unresolved choice card
  cannot resolve an icon from its descriptor, the card stays visible with a
  stable placeholder and a goal diagnostic is logged.
- Goal card wanted counts use the projected additional missing amount translated
  into main's carried-target count, not `max(requiredCount, wantedCount)`.
- Choice indicators use a high-contrast dark pip with bright question mark.
- `R` / recipe actions route from the clicked card's producer recipe or the
  relevant choice-group recipe before falling back to the active goal recipe.
  `U` / usage actions resolve choice-card delegation through a concrete
  alternative instead of the synthetic choice identity.
- Manual choices are now goal-scoped session state. Context menus show concrete
  alternatives, record the selected identity, recompute the projection, and show
  `Clear manual choice` only when a manual choice exists.

Fixed in the runtime-logging pass:

- Placeholder goal cards still use a stable fallback icon, but their hover name
  and primary tooltip title are now SLOT-owned (`Choice: ...` / `Missing: ...`)
  instead of exposing the raw knowledge-book item name as the first tooltip
  line.
- `Open alternatives in EMI` now round-trips the stored serialized
  `EmiIngredient` and calls EMI's ingredient/recipe display path. If SLOT cannot
  deserialize the ingredient, it fails closed with a diagnostic instead of
  opening the parent recipe and presenting that as an alternatives view.
- Tag/list ingredients with exactly one concrete item alternative are no longer
  treated as player choices just because they came from a tag. They project as
  concrete requirements and can recurse into their producer recipes; only
  multi-alternative or no-item-alternative groups stay choice cards.
- Verbose goal logging now traces EMI recipe collection, serialized ingredient
  shapes, non-item alternatives, producer candidates, producer collisions,
  authority subtraction, auto/manual choice resolution, child-recipe selection,
  wanted-count assignment, and expansion/stop decisions. NeoForge uses the
  existing `verboseLogging` client config; Forge 1.20 reads
  `-Dslot.verboseLogging=true` because that target does not yet have a client
  config file.
- Active goal projection is cached by view-model revision, goal-state revision,
  and active goal id. Render, tooltip, and context-menu paths no longer rebuild
  the same captured recipe graph every frame while the visible authority and
  selected goal are unchanged.
- Per-projection diagnostics moved to verbose logging. Default debug logging
  still records high-signal goal creation and explicit user actions, but
  projection diagnostics should no longer spam every render tick.
- EMI producer recipe collection is breadth-first instead of deep-first.
  Direct producers for root inputs are queued before descending into source
  acquisition branches, and unsupported/loot producers are skipped before they
  can consume the bounded recipe budget.
- Non-item EMI alternatives now preserve a public EMI name/id/key in the
  descriptor label and diagnostics when available, so fluid-like requirements
  have a meaningful placeholder label instead of only `input N`.
- Host-resolution diagnostics are deduped per origin. The workspace session and
  carried-activity tracker can both resolve the same menu every tick without
  alternating signatures and drowning goal logs.
- Producer recipe expansion no longer auto-selects the first EMI recipe when
  several recipes can produce the same missing item. If exactly one producer
  recipe has all item inputs visible in carried/storage authority, SLOT expands
  that route; otherwise the missing item stays a leaf and SLOT adds a
  producer-choice card.
- EMI inputs whose alternatives return the same movable item as a remainder are
  treated as reusable tools. A saw required by a plank recipe is a one-tool
  requirement, not one consumed saw per output batch.
- Recipe-choice cards now use an explicit EMI capture bridge instead of
  observing EMI's internal BoM/tree state. Choosing `Browse in EMI` /
  `Choose recipe in EMI` records a pending goal choice, opens the relevant EMI
  recipe list, and retargets SLOT's recipe-screen button from "add goal" to
  "use this recipe". Clicking it validates that the recipe satisfies the
  pending choice, merges the selected recipe descriptors into the goal graph,
  records the recipe id as goal-scoped choice state, and returns to the
  workspace.
- Producer recipe choices no longer render a second synthetic card for the same
  missing item. The unresolved state attaches to the existing requirement card;
  after the player chooses a producer recipe, the card keeps enough choice state
  for clear/change actions but stops showing the unresolved `?` marker.
- Chosen producer recipes are remembered as SLOT-owned output defaults keyed by
  output item id. If the player chooses the barrel recipe for Glue once, later
  producer choices for Glue can expand that route automatically without reading
  EMI's internal BoM defaults; the right-click menu still exposes the EMI
  capture flow so the player can change the remembered route.

Fixed in the persistence and real-pack choice pass:

- Goal tabs are saved through workflow-domain `GoalPlanSaved` /
  `GoalPlanRemoved` events and restored into the workspace view-model, so goals
  should survive logout/login instead of being only client session state.
- Producer recipe defaults are saved through workflow-domain
  `GoalRecipeDefaultSet` events and projected back to both loaders, so remembered
  choices survive reconnect and can be reused by future goals for the same
  output item.
- Empty crafting-grid slots from shaped EMI recipes are omitted before planning.
  They no longer create unknown placeholder requirements for blank recipe
  positions.
- The choice indicator was enlarged and redrawn as a high-contrast badge so the
  `?` marker is readable over item art.
- Visible carried, wall, triage, and proximate-storage stacks are now used to
  enrich serialized EMI ingredient alternatives before projection. If a visible
  stored item satisfies a tag/list ingredient, the projection can auto-resolve it
  instead of forcing a choice card.
- Movable identity matching now tokenizes path separators as delimiters, so
  path-shaped tool ids such as `tfc:metal/saw/steel` can satisfy stable
  `saw`-style requirements.
- Non-item EMI outputs and alternatives are no longer skipped during producer
  lookup. Fluid-like leaves can recurse into producer recipes where EMI exposes
  one, instead of ending the branch just because the intermediate is not an item
  stack.
- Synthetic goal display fallback now maps known fluid-like requirements to
  safer visible items: `limewater` displays as a named barrel, `water` and
  `lava` use their buckets, and generic fluid/liquid placeholders use a bucket
  fallback rather than a knowledge book.

Still needs real-recipe playtest:

- Re-test the auto-choice invariant with real tracked storage. A stored steel
  saw near the player should satisfy a plank recipe's saw requirement without a
  choice card, and the same path should cover other tag/list tools.
- Wood/source-acquisition remains unresolved product behavior: EMI may expose an
  automation recipe such as hydroponics for a log even when the practical player
  action is to chop a tree. Enable verbose producer-candidate logs before
  deciding whether SLOT should stop at the missing log, show an acquisition
  placeholder, or delegate to EMI without expanding that branch.
- Fluid ingredients now get useful synthetic display items, but they are not
  first-class "barrel containing fluid" requirements. The next playtest should
  confirm whether limewater-as-barrel is sufficient or whether SLOT needs a
  richer filled-container placeholder.
- Verify that fluid and non-item recursion is complete in representative pack
  recipes: limewater should lead to water + flux where EMI exposes that route,
  and liquid copper should lead to copper input plus molds when the selected
  recipe path requires them.
- Producer-choice cards now delegate recipe selection through EMI, but the
  capture flow still needs real-pack playtest for wording, invalid-recipe
  feedback, and whether a visible cancel affordance is needed on the EMI screen.
- Reconnect persistence needs one more real-client check: create a goal, choose a
  few producer recipes, disconnect/reconnect, then confirm both the goal tab and
  remembered output defaults return.
- Choice-card UX is actionable now; refine wording/ordering after seeing real
  tag/list alternatives from a pack.

Suggested next slice:

1. Re-test the Blackwood Barrel Press goal and verify default logging stays
   quiet while the goal tab is open.
2. Confirm tracked-storage alternatives auto-fill: the nearby steel saw should
   satisfy plank/saw choices without asking the player, and stored concrete
   materials should win over broad unresolved tags.
3. Confirm downstream branches such as metal rods/plates/gears, glue,
   limewater, water/flux, liquid copper, and molds appear or fail closed with a
   useful diagnostic.
4. Confirm ambiguous producers such as long-rod uncrafting or slime-to-glue
   remain unresolved unless the required input items are already visible, then
   use EMI capture to pick the intended route and confirm the projection expands
   and persists that selected route.
5. Refine fluid/source-acquisition placeholders and choice-menu wording from
   observed pack data, without adding a persistent goal header or forcing
   choices already satisfied by visible authority.

## Open Questions

- Should ctrl-scroll target counts step by recipe output count, stack size, or
  the existing desired-count increment policy?
- What is the right explicit removal/editing UX for persisted recipe goals once
  players have several active goals?
- What is the right recursion depth for the first public version?
- What visible-authority ordering should automatic choice resolution use:
  carried first, proximate first, or "least disruptive" count matching?
- If EMI later exposes public favorite/pin listing and observation, should SLOT
  subscribe to it directly or keep explicit SLOT-side goals as the source of
  truth?

## Risks

- **Parallel EMI implementation.** Reuse EMI public APIs for recipe discovery,
  ingredient serialization, and display delegation; mirror BoM behavior only
  where EMI exposes no stable data API.
- **Planner creep.** Keep the projection browse-first; do not choose an optimal
  route or mutate inventory.
- **Recipe explosion.** Bound recursion and preserve alternatives.
- **Automatic choice surprise.** Auto-resolve from visible authority, but mark
  choice-involved cards and make changing/clearing the choice discoverable.
- **Wanted-count confusion.** Goal wanted counts are active-tab projection
  outputs, not persisted player wanted counts, until explicitly promoted by a
  later slice.
- **Stale storage ghosts.** Treat pips as known-location hints, not proof until
  authority refreshes.
- **Ambiguous tags.** Use choice cards and delegate details back to EMI.
