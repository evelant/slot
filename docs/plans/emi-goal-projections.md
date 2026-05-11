# EMI Goal Projections Plan

Last updated: 2026-05-11

Status: proposed; Slice 0 contract spike completed 2026-05-11. This plan owns
the runtime/UI side of recipe goals: SLOT-side recipe goals captured from EMI
recipe context become goal tabs, and each tab projects the normal SLOT wall to
show what the player has, what is known in storage, and what is missing for the
selected recipe goal. Future EMI favorite/pin ingestion can be added if EMI
exposes a stable public API for it; the first implementation must not depend on
internal favorite state.

This plan deliberately does not own vocabulary generation or generic ambient
task views. See [classification-facet-vocabulary.md](classification-facet-vocabulary.md)
and [ambient-task-views.md](ambient-task-views.md).

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
  ghost/missing cards, wayfinding, goal-scoped desired-count overlays, and
  SLOT-specific diagnostics

SLOT adds value by connecting an EMI-selected goal to real inventory authority:

- carried items render as normal cards
- storage-known requirements render as ghosts with pips and wayfinding signals
- absent requirements render as missing ghosts without pips
- desired counts show only the additional amount needed for the active goal
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
7. Desired counts update only for the selected tab and only for the additional
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
- goal-scoped desired counts
- tooltip chain breadcrumbs
- choice indicators and context-menu metadata for cards that came from a
  tag/list ingredient decision

Desired counts are scoped to the active tab. If the goal needs 27 coke bricks
and the player has 12 visible across carried and known storage, the active goal
asks for 15 more coke bricks. Upstream ingredients receive desired counts only
for the missing 15, not for the full 27.

Goal-scoped desired counts are an overlay, not player-global desired counts.
They should not feed gather, deposit protection, or kit logic unless a later
slice explicitly promotes that behavior after playtest.

Each goal is independent. SLOT should not reserve items across goals and should
not calculate cross-goal conflicts. Selecting another goal tab recalculates and
displays that goal's desired counts against the current authority snapshot.

## Recipe Expansion Rules

The resolver/adapter should reuse EMI's recipe tree and resolution semantics as
far as the EMI contract allows. If SLOT has to own a common projection model, it
should represent EMI concepts faithfully rather than inventing a parallel recipe
browser.

Recursive expansion must stay bounded and explicit:

- expand from the SLOT goal descriptor captured from EMI recipe context
- subtract visible authority before assigning upstream desired counts
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
  tag/ingredient label, desired count, and a clear action-needed indicator
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
- absent requirements render as missing ghosts with desired counts and no pips
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
  reflect those internals for production behavior.
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
- subtract visible authority to compute goal-scoped desired counts
- dump a normal-wall projection with real cards, storage ghosts, missing ghosts,
  choice cards, choice indicators, and tooltip breadcrumbs

Exit criteria:

- one fixture recipe goal produces useful desired counts without changing homes
  or moving items
- visible matching items auto-resolve ingredient choices, while unresolved
  remainders stay visible instead of being silently flattened
- loops/depth limits fail closed with diagnostics

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
- desired counts appear only while that goal tab is active
- storage-backed and missing ghosts are visually distinguishable
- choice cards and choice indicators are visually distinct from normal storage
  pips

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

## Open Questions

- What is the exact UI affordance for creating a SLOT goal from EMI recipe
  context: a keybind on hovered EMI output, a SLOT context-menu action, or a
  small adapter button in EMI recipe screens?
- Should ctrl-scroll target counts step by recipe output count, stack size, or
  the existing desired-count increment policy?
- Should SLOT-side recipe goals persist across sessions, or are they workspace
  session state until playtesting proves otherwise?
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
- **Desired-count confusion.** Goal desired counts are tab overlays, not
  player-global desired counts, until explicitly promoted by a later slice.
- **Stale storage ghosts.** Treat pips as known-location hints, not proof until
  authority refreshes.
- **Ambiguous tags.** Use choice cards and delegate details back to EMI.
