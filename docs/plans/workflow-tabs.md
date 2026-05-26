# Workflow Tabs Plan

Last updated: 2026-05-26

Status: core implementation, the first playtest polish pass, and the EMI-first
craft-run/staging slice have landed.
Workflow tabs now support active-tab filtering, one-level variants, tab-local
desired/wanted targets, accepted exact/tag inputs, accepted proximate substitute
ghosts, compact nearby headers, three-row floating Recents, hidden Useful Now scoring,
visible activation-scoped Put Away guidance, search/keybind polish, shared
tool/container/display-storage target resolution, put-away destination
highlighting/wayfinding, and the adjacent junk/trash pressure-relief slice.
Workflow/variant reorder UI plus duplicate/rename polish has also landed on
the current Kit-backed substrate. EMI recipe screens now keep the transient
recipe sidebar and can add the visible recipe to a persisted server-owned
recipe list, adjust or remove entries, decrement remaining output through
acquisition activity, and stage selected recipe inputs into player main
inventory through the normal action pipeline. Recipe entries render as wall-list
sections with per-recipe stage/count/done controls, and recipe inputs project as
transient wanted-count pressure rather than tab-local desired counts. The
legacy `SLOT goal` / recipe-goal model, UI, RPC, codec, and
persistence surface has been removed. Remaining follow-ups are explicit
hovered-item `Use this` concretization for unresolved recipe alternatives, if
playtesting shows it matters, and the later Kit-name cleanup in Slice 6. This
supersedes Kit Rack / Kit prototype work as the
task-workflow direction while reusing the current Kit, desired-count,
wanted-count, gather, loadout, and storage code wherever it already fits.

## Core Decision

Kits become **workflow tabs**: player-authored filtered views over the normal
inventory projection. `All` remains the unfiltered default tab. Counts set on
`All` are the global baseline and automatically apply to every workflow tab.

Do not add a separate global policy layer for favorites, always-carry items,
never-auto-deposit items, consumable targets, or protection. Those are already
expressed by desired/wanted counts on `All`.

A workflow tab adds task-local intent on top of `All`:

- tab-local desired counts
- tab-local wanted counts
- persistent tab membership, which behaves as an implicit active wanted count
  of `1` when no explicit tab count exists
- accepted inputs, which match exact items or item tags and suppress Put Away
  while adding no wanted/desired count and no missing-item pressure
- optional Belt/offhand pages, reusing the current Kit hotbar code
- optional one-level variants that add more task-local intent on top of their
  parent tab

When a workflow tab is active, missing tab targets use the same gather,
wayfinding, storage-ghost, and gap chrome that desired/wanted counts already
use. Carried items that were present when the workflow was activated and are
not relevant to `All` or the active tab get put-away guidance; later pickups do
not become cleanup guidance unless the workflow is activated again.

## Current Landed Behavior

- User-facing workflow tabs are live on both loaders, but code still uses
  transitional `Kit*` domain names where the old substrate is still useful.
- `All`, active parent tab, and active variant targets compose as floors through
  the shared workflow target resolver. Bulk gather, bulk deposit/protection,
  active-tab projection, and loadout page apply all consume that resolver instead
  of duplicating target math.
- Right-click item menus can add/remove accepted exact-item rules or accepted
  tag rules for the active tab. Tag options prefer material-specific paths such
  as `forge:ores/hematite` and skip broad parent process tags such as
  `forge:dusts`.
- Accepted inputs are relevance-only: they suppress put-away and can reveal a
  nearby substitute, but they do not create desired/wanted counts or missing
  craft pressure.
- Ordinary nearby/proximate storage ghosts are still present in the view model
  but stay collapsed unless search, storage x-ray, or the section header reveal
  asks for them. Accepted-tag proximate substitutes are intent ghosts and show
  inside the active tab by default.
- Search uses the same global item/section matcher everywhere. An active
  workflow tab may hide unrelated cards by default, but a query can reveal any
  matching carried, proximate, or tracked-storage card; active tabs do not get a
  narrower search dialect.
- Empty sections render as compact headers with `+x` nearby counts on the
  header. Clicking anywhere on a header with hidden nearby cards toggles the
  section reveal.
- Useful Now scoring remains hidden in the rendered wall while its projection
  code stays available for later playtests. The workflow Put Away strip is
  visible and uses the same activation-snapshot eligibility as put-away card
  state and bulk deposit.
- Active-workflow put-away clutter now projects distinct wayfinding targets for
  known chest/display destinations, with green put-away HUD/glow styling and
  preserved acquisition highlighting when a destination is also a fetch target.
- Workflow and variant context menus can move siblings left/right, duplicate
  beside the source family with readable copy names, and reject sibling rename
  collisions instead of allowing ambiguous visible names.
- Two rows of Recents render above the wall. Search auto-commits after about two
  seconds idle, clears after the interface has been closed for about ten seconds,
  and can be cleared by right-clicking the search label. Text inputs suppress
  workspace hotkeys while focused.
- The configurable move-to-main-inventory key defaults to grave accent. It moves
  a hovered identity from backpack/hotbar into main inventory. The separate
  move-to-backpack key defaults to Shift+grave and moves a hovered identity from
  main inventory or hotbar into backpack storage when provider space exists.
- EMI recipe screens keep the normal filtered sidebar and expose `Add Recipe`
  actions for the visible recipes in the wall list. The configurable
  `Add visible EMI recipe to crafting` keybind adds the hovered recipe. Added
  recipes render as wall-list sections, one section per recipe entry, with
  compact output-icon headers and stage/count/done controls. Recipe inputs
  project as transient
  wanted-count pressure, so gather, wayfinding, storage ghosts, and protection
  use the same resolver as the card chrome rather than a separate recipe-goal
  wall.

## Why

The current wall has a useful unified carried/storage projection, but player
workflows still force scanning and re-scanning the full wall:

- EMI crafting needs recipe ingredients located, gathered, sometimes staged
  into vanilla main inventory, and sometimes tracked as temporary wanted items.
- Making room needs the inverse of gather guidance: which carried items can
  leave now, and where should the player stand to put them away?
- Multi-step processes such as smithing, ore refining, cooking, or farming
  need a player-curated working set, not a generic mode like `Craft` or
  `Clean Up`.
- Kit prep is already close to this model: a task name, hotbar pages, and
  non-hotbar carry targets.

Workflow tabs reduce friction by hiding irrelevant cards for the current task
without changing inventory authority or inventing another planning system.

## Player Model

### `All`

`All` is the current default wall:

- shows the unfiltered carried/storage projection
- owns player-global desired counts
- owns player-global wanted counts
- provides the baseline targets inherited by every workflow tab

If the player sets `torch x64`, food, a preferred tool, or any other
always-carry target on `All`, every workflow tab treats that target as relevant
and protected.

### Workflow Tab

A workflow tab is a named player-authored task view such as `Mining`,
`Smithing`, `Cooking`, `Ore Refining`, `Farming`, or `Exploration`.

The active tab shows:

- items from the `All` baseline
- tab members
- tab-local desired/wanted targets
- tab Belt/offhand page items
- visible missing cards for needed items
- relevant nearby/tracked storage ghosts for needed items
- nearby/proximate substitute ghosts that satisfy accepted exact/tag inputs
- ordinary nearby/tracked storage ghosts only when search, storage x-ray, or a
  section-header reveal asks for them
- carried items that were present at activation and are not relevant, with
  put-away card state

The active tab does not change inventory authority. It changes what the wall
prioritizes and what guidance is active.

### Variants

Workflow tabs may have one level of variants. Variants are not arbitrary nested
folders; they are a lightweight way to layer a specific subtask on top of a
broader workflow.

Examples:

```text
Smithing
  Steel Smelting
  Bronze Smelting
  Steel Smithing

Cooking
  Soup
  Preserves
  Sandwiches
```

Activating `Steel Smelting` means:

```text
effective targets = All + Smithing + Steel Smelting
```

The parent carries stable shared needs such as hammer, tongs, ignitor, common
fuel, flux, and molds. The variant adds the transient or technology-specific
needs such as anthracite plus iron dust, copper plus tin, or steel ingots plus
rods. When the pack's technology changes, the player can update the parent once
instead of editing every related workflow.

Rules:

- one parent level only; variants cannot have variants
- parent targets and variant targets are additive
- variants cannot silently lower `All` or parent targets
- activating a parent without a variant is valid and means `All + parent`
- Belt/offhand pages inherit from the parent unless the variant explicitly
  defines its own page set

### Tab Membership

Adding an item to a workflow tab should be cheap. A member with no explicit
count behaves like:

```text
while tab is active:
  want at least 1 carried
```

Already-carried members are immediately satisfied. Missing members get the
normal guidance to gather or navigate to storage. If the player wants a
specific quantity, they set a tab-local desired or wanted count.

Implementation note: historical Kit "bring" behavior has been folded into
kit-scoped desired counts, and the old visible `targets` row / drag payload is
retired. Desired counts are edited through the normal item context menu or
scroll gesture and resolve through the shared workflow target resolver. Tab
activation applies Belt/offhand slots only; explicit gather uses the shared
server-authoritative gather path instead of a platform-local auto-fetch pass.

Movable identity matching is shared in `common/`, not per workflow action, and
workflow-facing identity sets/maps should use `ItemIdentityCollections` rather
than strict `Set.contains`, `Map.get`, `putIfAbsent`, or `remove`. `ITEM_ID`
targets are broad matches for the same item id. Stack-created identities
collapse to item-id when shared signals say their component data is condition
rather than identity: Minecraft damageability, registered portable container
classifiers, common tool item tags, tool-state component/NBT keys such as
`minecraft:tool` / `GT.Tool`, or condition-only damage/container fingerprints.
Do not add item-name token exceptions for one modpack item family; add a shared
signal or classifier instead.

### Tab Wanted Counts

Wanted counts need active-tab semantics:

- on `All`, wanted counts keep today's player-global behavior
- with a workflow tab active, "wanted 1" should be scoped to that active tab
- once satisfied, the item remains visible in the active tab until the tab is
  deactivated
- deactivating the tab removes the temporary active-tab wanted pressure unless
  the player promoted the item to permanent tab membership or a tab desired
  count

This is still the wanted-count concept. It is not a separate scratchpad UI.

### Accepted Inputs

Accepted inputs are for "any of these is okay" workflow materials: ore variants,
fuel tags, recipe alternatives, and similar process inputs where the workflow
should recognize relevance without demanding a particular stack.

Rules:

- accepted inputs can be exact item identities or item tags
- the same right-click menu path adds and removes accepted inputs
- accepted inputs do not display a wanted or desired count
- accepted inputs do not create a missing target when none are carried nearby
- matching carried items are not considered active-tab put-away clutter
- matching proximate storage ghosts are revealed as substitutes in the active tab
- matching tracked-only ghosts stay behind storage x-ray until we add better
  wayfinding for "any of these accepted inputs"

Tag choices are intentionally conservative. The menu filters out broad parent
process tags such as `forge:dusts` / `forge:ores` and prefers material-specific
paths such as `forge:dusts/hematite` or `forge:ores/hematite`. Facet
classification data such as `material_family` is useful evidence, but it is not
currently an active acceptance matcher; item tags are the shipped matcher.

### Effective Targets

For each identity, the active carry target is the maximum of:

- `All` desired count
- `All` wanted count
- active parent tab desired count, if a variant is active
- active parent tab wanted count, if a variant is active
- active parent tab implicit membership target (`1`), if a variant is active
- active tab desired count
- active tab wanted count
- active tab implicit membership target (`1`)
- active tab Belt/offhand page requirement

`All` and any active parent tab are floors, not something a variant can
silently lower. A child variant can ask for more than the parent by setting a
higher variant-local target.

## Guidance

### Gather Guidance

Gather guidance remains the current desired/wanted/Kit guidance generalized to
workflow tabs:

- missing carried target
- reachable in proximate storage
- known elsewhere and needs wayfinding
- missing everywhere known

The existing gather action should become "gather active tab targets", backed by
the same server-authoritative storage path as current Kit gather.

### Put-Away Guidance

Put-away guidance is the inverse of active-tab relevance, scoped to the
activation moment.

A carried identity is eligible when:

- it was present in carry when the workflow tab was activated
- it is not needed by `All`
- it is not needed by the active parent workflow tab, if a variant is active
- it is not needed by the active workflow tab or variant
- it is not part of the active parent or variant Belt/offhand page state
- it is not protected by existing safety rules such as portable-container
  protection

When eligible, SLOT should surface where it can go:

- nearby learned-affinity or matching-content destination
- known tracked storage destination that requires walking there
- no known home / needs manual storage choice

The workflow Put Away strip is visible because activation-scoped clutter is
specific enough to be actionable. Active-tab-irrelevant carried cards also
remain visible with put-away border/chrome, and bulk deposit uses the same
activation-snapshot eligibility and protection logic. Put-away guidance should
continue to use a distinct visual language from gather guidance, including a
distinct chest world-highlight color. Acquisition and cleanup are opposite
intents; they should not look identical.

## Bulk Hotkeys

The common bulk actions need first-class keybindings because they interrupt
almost every workflow and should not require finding a small UI button:

- **Bulk gather active tab:** gather missing `All + parent + active tab`
  targets from proximate storage through the existing server-authoritative
  gather path.
- **Bulk deposit active tab clutter:** deposit carried identities from the
  workflow activation snapshot that are not relevant to `All`, the active
  parent, or the active tab/variant. This is the hotkey version of put-away
  guidance; later pickups are not included until the workflow is activated
  again.
- **Trash hovered identity:** delete carried stacks matching the hovered item
  type, with undo, while marking that identity as junk for later pickup pressure
  relief.
- **Move hovered identity to main/backpack:** grave accent moves a hovered
  identity to main inventory; Shift+grave moves a hovered identity to backpack
  storage when provider space exists.

Bindings should be configurable and should share the same action pipeline as
their visible buttons. Bulk put-away is unbound by default so players opt into
one-key cleanup after trusting their storage homes. If no active workflow tab is
selected, bulk gather uses `All` targets and bulk deposit uses the normal `All`
cleanup eligibility. Direct trash still respects desired/wanted/tab protection
and only deletes currently carried matching stacks.

## Authoring Flows

Workflow tabs must be cheap to create and modify:

- create empty tab
- create an empty tab from the visible workflow-tab controls, not only by saving
  a hidden Kit page
- create variant under a tab
- duplicate tab
- rename/delete/reorder tab
- rename/delete/reorder variants
- save current Belt/offhand as a tab page
- add/remove/reorder Belt pages
- add hovered item to active tab
- add selected wall card to active tab from the right-click menu
- add current search result to active tab
- add the visible EMI recipe to the tracked recipe list
- stage a selected craft-run recipe into player main inventory
- set tab-local desired/wanted counts from card chrome or menu

The old Kit Rack should not remain a hidden separate mode. Workflow tabs are
visible as tabs, and the active tab's Belt pages should stay available near the
bottom Belt area rather than hidden behind a rack toggle.

### Input And Search Behavior

The workspace should treat text fields as text fields, even in sidebar mode.
While the player is typing in a tab name, search, anvil field, or other focused
input, workspace hotkeys such as `e`, `x`, tab-to-hotbar, wanted-count controls,
and move-to-main should not steal the keypress.

Search is a temporary filter, not a sticky modal trap. Current behavior:

- typing `/` opens search
- idle for about two seconds commits/unfocuses the search field
- right-click on the search label clears the query
- after the workspace has been closed for about ten seconds, the remembered
  query clears automatically
- `x` storage reveal remains available while a workflow tab is active so players
  can reveal nearby candidates while building the tab

## EMI Crafting Workflow

The EMI recipe sidebar should feed persisted craft runs instead of directly
mutating workflow tabs or becoming a separate recipe-goal surface.

The current EMI recipe sidebar remains a transient visible-ingredient filter.
EMI/JEI own recipe discovery, recipe alternatives, categories, and explanation;
SLOT owns inventory/storage context, persisted craft intent, acquisition
guidance, staging into player main inventory, and cleanup guidance. Do not
revive recursive SLOT-side recipe goals or recipe explanation unless
playtesting shows this craft-run model is insufficient.

### Implementation Clarifications

These constraints are part of the first craft-run implementation, not polish:

- initial implementation is EMI-first; common craft-run records should stay
  recipe-viewer-neutral enough for a future JEI adapter, but no JEI behavior is
  required until a focused JEI compat slice exists
- craft runs replace the legacy EMI `SLOT goal` surface; remove or retire the
  EMI recipe goal buttons, drag/drop target, goal-tab capture, and
  `SlotEmiGoalAdapter` / loader-equivalent routes as part of this slice rather
  than showing old goal creation beside craft-run actions
- craft runs also replace the old recipe-goal data model; remove the dormant
  goal projection, goal-tab UI, goal persistence, goal RPC, and workspace
  view-model goal fields before considering Slice 5 complete
- the one current craft run is server-owned, player-scoped, and persisted in
  workflow state; the client may display and edit it, but recipe-screen data is
  only player intent
- the current run survives closing the SLOT sidebar, switching normal workflow
  tabs, closing/reopening EMI, navigating among EMI recipes, player logout/rejoin,
  and server restart; its temporary pressure clears when acquisition progress
  completes recipe entries or when the player removes entries
- common craft-run state must not contain `dev.emi.*`, JEI, screen, widget, or
  loader objects
- loader compat captures a recipe-viewer DTO and submits it as intent: selected
  recipe key/index, recipe label/category when available, output display stack,
  output identity or alternatives, output count per batch, input groups,
  per-batch required counts, display labels/stacks, complete matching data when
  available, and opaque/non-item diagnostics
- separate matching data from display data: a UI may cap rendered alternatives,
  but staging and `Use this` matching must use a complete matcher or fail
  closed with an actionable diagnostic instead of treating a truncated display
  list as complete
- if EMI shows multiple visible recipes, actions target one explicitly selected
  or hovered recipe; never serialize a whole visible page of alternatives into
  one run entry
- remaining count is output units, not batches; input requirements are computed
  as `ceil(remainingOutput / outputCountPerBatch)` batches, and unknown output
  count falls back to one output per batch with a diagnostic
- count controls add/remove one recipe-output batch and clamp at one batch
- adding or count-adjusting recipes raises the first same-list producer recipe
  to at least the output units required by other active recipe inputs
- when one acquisition delta matches multiple recipe entries, consume it in
  stable visible run order; do not infer parent-child priority
- acquisition progress listens to the same post-suppression carried-acquisition
  activity stream that drives `Recent`; do not listen directly to platform
  crafting-result events

### Craft Runs

A craft run is a player-authored session: "I am trying to obtain these recipe
outputs." It is persisted because large recipes can span play sessions, but it
is not a workflow tab, not an EMI favorite, not a saved recipe shortcut, and not
a recursive planner.

Rules:

- the initial implementation has one current tracked recipe list, not multiple
  parked runs
- the current craft run contains a flat list of recipe entries
- each recipe entry has a remaining output count, not a separate "done" state
- recipe entries contribute ingredient pressure only while their remaining
  output count is greater than zero
- recipe entries whose output is required by other active recipe entries are
  raised to that required output count when recipes are added or counts are
  adjusted; this does not create parent-child recipe edges
- matching item-acquisition deltas decrement the remaining output count and
  remove entries when they reach zero
- acquiring more after the entry has been removed does nothing
- the player may adjust a recipe entry's remaining count at any time
- the player may click `Done` / remove on any recipe entry at any time,
  regardless of whether the remaining count is zero
- completing or removing the last recipe entry clears its temporary pressure; no workflow
  membership, accepted-input rule, desired count, or persistent wanted count is
  written unless the player explicitly performs a separate workflow edit

The UI home is the normal wall list, not a separate top-row lifecycle panel.
When EMI/JEI has visible recipes, the wall shows one `Add Recipe` action per
captured recipe context; the keybind adds the hovered recipe. Added recipes
become individual wall-list sections with remaining count, stage, count adjust,
and remove controls in compact output-icon headers. Clicking normal workflow tabs
can still browse the workflow wall; the
tracked recipes remain until acquisition progress or `Done` removes them.
While tracked recipes are present, the fixed `Fetch` suggestion lane is hidden;
the per-recipe sections own that guidance and avoid duplicating the same cards.
Multiple simultaneous craft runs are deferred until playtesting shows players
need to juggle unrelated craft sessions, because acquisitions, staging, and
put-away pressure need unambiguous attribution.

Use acquired-item deltas as the primary progress signal, not crafting-result
events. Players often obtain craft-run outputs from machines, chests,
chute-fed buffers, storage terminals, world pickups, or other automation rather
than by hand-crafting in a result slot. The progress rule is "the player
obtained this output," not "the player clicked this recipe result."

Only meaningful acquisitions should count:

- count: world pickup, taking from chest/storage, taking machine output,
  crafted output entering carried inventory, or other source-to-player
  acquisition events
- do not count: moving the same item between carried sources, staging into main
  inventory, hotbar/backpack rearrangement, cursor reshuffle, SLOT internal
  transfer, deposit, or put-away

The run is flat on purpose. When the player opens an upstream recipe from EMI
or from a missing card, SLOT should offer `Add to current run`, but it should
not infer a parent-child edge. EMI exploration is nonlinear, and automatic
linking would be wrong too often. SLOT can still aggregate current recipe inputs
to raise same-list producer counts so upstream recipes start at the amount
needed by the rest of the list.

Planned outputs do not suppress downstream recipe guidance. If a casing recipe
requires plates and a plate recipe is also tracked, the casing still asks for
plates and the plate recipe asks for its own inputs; the only automatic coupling
is the plate recipe's minimum remaining output count.

Primary actions:

- `Add Recipe`
- `Set remaining count` / `Add N batches`
- `Stage selected recipe`
- `Done` / remove recipe entry

`Add Recipe` uses the selected visible EMI recipe as the source of recipe entry
outputs and input requirements. Multiple visible recipes render separate add
actions, and the keybind resolves to the hovered recipe; do not silently add a
whole page of alternatives unless the player asks for that.

### Variant Ingredients

Recipes with tag/list ingredients should not force concrete choices before the
recipe enters the run. Store unresolved ingredient groups in the recipe entry
and resolve them lazily from live inventory/storage when rendering guidance or
staging.

Do not build a SLOT-owned alternative browser. For unresolved groups with many
possible matches, clicking the group should delegate to EMI/JEI's normal
ingredient/tag page, the same way interacting with the ingredient in the recipe
viewer does. SLOT's added affordance is run-scoped concretization:

- when the player is hovering or viewing a concrete item in EMI/JEI, show a
  `Use this` action if it satisfies any unresolved group in the current run
- also provide a configurable hover hotkey that applies the hovered concrete
  item to any unresolved group in the current run that it can satisfy
- if the item can satisfy multiple unresolved groups, apply it to all compatible
  groups by default unless playtesting shows this needs a chooser
- chosen alternatives are run-scoped only; they do not become workflow accepted
  inputs or saved preferences
- if no choice has been made, staging may use any matching carried item by the
  normal source priority rather than blocking on concretization

### Staging

`Stage selected recipe` moves already-gathered ingredients from carried sources
into player main inventory slots that EMI/JEI and vanilla can actually consume
from. Staging is source-aware and server-authoritative:

- destination is player main inventory, not the hotbar as a fake staging lane
- source may be any carried provider SLOT can authoritatively extract from
- the server recomputes live sources and safe destinations before mutating
- the client recipe entry is intent, not authority
- staging only acts on the selected/current recipe entry, not the whole run
- unresolved variant ingredients stage any matching carried item by the same
  source priority used for normal carried extraction
- staging keeps the UI simple: no preflight questionnaire, no modal choice
  picker, and only a small status if nothing could be staged

If player main inventory is too full to stage recipe inputs, surface `Make
room` / put-away guidance for active-workflow-irrelevant carried items before
staging. Do not pretend EMI can pull from carried providers that EMI cannot
actually see.

### Workflow Interaction

Craft runs compose with the active workflow for guidance, but they do not
pollute the workflow:

- active workflow targets still protect their normal items
- craft-run inputs and outputs suppress put-away while the run is active
- craft-run missing items use the same gather/storage/wayfinding chrome as
  other temporary acquisition pressure
- removing recipe entries removes that temporary pressure
- adding recipe ingredients, alternatives, or outputs to the workflow remains a
  separate explicit workflow edit

This distinction matters for one-off crafts such as a steel grindstone: the
player can gather, stage, acquire, and clean up without leaving permanent
recipe ingredients inside `Smithing`. Repeated processes such as steel smelting
can still become real workflow tabs/variants through the normal workflow
authoring tools.

### Deferred Recipe Shortcuts

Workflow-scoped recipe shortcuts / run presets are a plausible future feature,
but they are deliberately out of the initial implementation. They are distinct
from EMI/JEI favorites only if they behave as contextual workflow launchers:
"inside this workflow, track this recipe again." If implemented later, store
only enough to add a recipe through the recipe viewer boundary, such as
recipe id/source key, display name/output, default count, and run-scoped
alternative choices. Do not turn workflows into recipe dashboards or duplicate
EMI/JEI's global bookmark surface.

## Overflow / Junk Pressure Relief

Junk is independent from workflow tabs but fits the same "reduce scanning"
theme.

Landed slice:

- right-click item identity -> mark/unmark as junk
- junk marks auto-expire after 30 minutes
- junk identity cards show a small low-priority indicator
- direct trash is available from the item context menu and from a configurable,
  unbound hovered-item hotkey
- direct trash deletes carried stacks matching the hovered identity, records
  undo/redo, and marks the identity as junk
- when effective carried storage is over half full before or after a pickup,
  marked junk stacks are deleted before backpack reroute, preferring the
  just-picked stack when it is marked junk and voiding the incoming junk entity
  if vanilla would otherwise have no room to pick it up; known specialist Sacks
  n' Such containers do not count as general-purpose pressure capacity
- carried storage pressure is cached against common per-player carried-inventory
  revisions bumped by both loader mutation hooks and common mutation routes

This is not an item-value heuristic and not a progression system. The player
explicitly marks junk identities; SLOT uses them as pressure relief.

## Implementation Slices

### Slice 1: Domain Vocabulary And Target Resolution

Goal: introduce workflow-tab terminology and target math without changing
storage authority.

- add or rename domain vocabulary from Kit to Workflow Tab in the user-facing
  model
- preserve or migrate current `KitDefinition` data shape only as an
  implementation stepping stone
- define `All` as the player-global target scope
- add optional `parentId` / variant relationship, capped at one level
- make effective targets combine `All` baseline, active parent tab, and active
  variant/tab targets using the target-floor rule above
- add active-tab wanted semantics; current `WantedCountWorkflowDomainService`
  is player-scoped only
- keep desired/wanted count stores as the only target-count model

Acceptance:

- tests prove `All` targets apply inside a tab
- tests prove parent-tab targets apply inside a variant
- tests prove active-tab and variant targets cannot lower inherited targets
- tests prove tab membership creates a target of `1`
- tests prove tab wanted targets stop affecting `All` after tab deactivation
- tests reject or normalize attempts to create variants of variants

### Slice 2: Projection And Filtering

Status: landed. Follow-up polish added accepted-input substitute ghosts, kept
ordinary storage ghosts collapsed until search/x-ray/header reveal, and compacted
empty section headers without changing the wall section model.

Goal: make the wall actually behave like the active tab.

- active tab projection includes `All` baseline, tab members, tab counts, and
  tab Belt/offhand identities
- accepted-input rules make matching carried items relevant without showing a
  desired/wanted count or missing target
- active variant projection includes `All`, parent tab, and variant targets
- active tab projection hides irrelevant ordinary cards by default
- active tab projection still reveals missing/relevant storage ghosts via the
  existing active-intent rule
- active tab projection reveals accepted-input proximate substitutes as
  intentful ghosts, while ordinary nearby/tracked ghosts stay collapsed unless
  the player explicitly reveals them
- recents remain unchanged and are not absorbed into tabs
- `All` remains the current unfiltered wall

Acceptance:

- activating a tab hides unrelated carried clutter
- activating a variant keeps parent workflow items visible
- already-carried tab members show as satisfied
- accepted inputs remain visible and are not placed in Put Away guidance
- accepted-input proximate substitutes appear without showing as missing targets
- missing tab members show gaps and storage guidance
- deactivating the tab returns to the `All` wall behavior

### Slice 3: Put-Away Guidance And Deposit Wayfinding

Status: landed. Put-away card state, visible activation-scoped Put Away strip,
protected bulk deposit, no-home card chrome, content-backed display-storage
deposit, display-storage undo ids, active-chest range gating, cross-loader
put-away hotkey, and destination highlighting / wayfinding are live. Routed
put-away clutter can produce distinct "go here to put this away" wayfinding
targets without changing deposit mechanics, while no-home activation clutter is
still visible as "no learned destination" cleanup guidance.

Goal: make cleanup as first-class as acquisition.

- compute active-tab-irrelevant carried identities from the activation snapshot
- render put-away guidance on normal cards or in a compact guidance lane
- route put-away actions through existing deposit planning
- add a configurable bulk-deposit hotkey that deposits active-tab-irrelevant
  carried identities through the same planning path
- add destination highlighting distinct from gather/acquisition highlighting
- add wayfinding for "go here to put this away", not only "go here to get
  this"

Acceptance:

- carried items present at activation outside `All` + active tab show put-away
  guidance when a route is known
- carried items picked up after activation do not enter put-away guidance until
  the workflow is activated again
- no-home items are clearly marked instead of silently disappearing from
  guidance
- put-away guidance respects existing protection and active target counts
- bulk deposit follows the same eligibility and protection rules as visible
  put-away guidance
- bulk deposit uses the shared workflow target resolver for tab members,
  desired counts, wanted counts, accepted inputs, and movable tool/container
  identities; automatic display-storage deposit is content-backed, so empty
  tool racks remain explicit-deposit targets rather than generic cleanup
  targets
- deposit undo keeps raw storage target ids, so deposits into display storage
  can be reversed through the same world-storage abstraction as chests
- active-chest deposit fallback still uses the shared proximate-storage radius;
  open-container state must not grant longer-range deposits than pickup

### Slice 4: Tab UI And Editing

Status: landed. Core tab rendering, activation, variant display, visible create
flow, membership/accepted-input menus, wider right-click menus, active-tab
filtering, three-row floating Recents, search idle commit/clear, header reveal clicks,
main-inventory move keybind, workflow/variant reorder controls, adjacent
duplicate insertion, readable copy names, and sibling rename collision checks
are live.

Goal: replace Kit Rack interaction with visible workflow tabs.

- render `All` plus workflow tabs in the top workflow row
- render variants as a second-level selector only when their parent is active
- support create, rename, duplicate, delete, and reorder for tabs and variants
- show active tab Belt/offhand page controls near the bottom Belt
- support add/remove item from active tab by card menu, hover-hotkey, and search
- keep page switching backed by the current loadout apply/page-cycle services
- add a configurable bulk-gather hotkey for active-tab missing targets

Acceptance:

- a player can create `Mining`, add items, add a hotbar page, activate it, and
  see the wall filter
- a player can create `Smithing` with a `Steel Smelting` variant, activate the
  variant, and see both parent and variant targets
- active tab page switching remains deterministic and server-authoritative
- bulk gather uses `All + parent + active tab` targets and never invents remote
  mutation authority
- editing an active tab updates guidance immediately

### Slice 5: EMI Craft Runs And Staging

Status: landed for the EMI-first path. Common now owns persisted craft-run
state and acquisition progress; NeoForge and Forge capture visible
EMI recipe, render an `Add Recipe` action plus per-recipe wall sections, submit
add intent to the server, and stage selected entries through the normal action
executor. The
legacy recipe-goal model and live `SLOT goal` surface were deleted in the same
slice. Explicit hovered-item `Use this` concretization from Slice 5c remains
deferred; unresolved ingredient groups are retained, and staging may use any
matching carried alternative by normal source priority.

Goal: implement persisted craft runs from the EMI recipe sidebar without
polluting workflow tabs or reviving recursive SLOT-side recipe goals. This
slice replaces the legacy EMI `SLOT goal` button / drag-drop / goal-tab capture
surface and removes the old recipe-goal system instead of layering craft runs
beside it.

#### Slice 5a: Common Craft-Run Model

Status: landed.

- add common craft-run records/state for a flat list of recipe entries
- support one current craft run for the initial implementation; defer multiple
  parked runs until acquisition and staging attribution have clear product
  signal
- keep the current run server-owned, player-scoped, persisted in workflow
  state, and cleared when acquisition progress completes the remaining entries
  or when the player removes them
- keep the run alive across sidebar close, EMI close/reopen, EMI recipe
  navigation, normal workflow tab switching, logout/rejoin, and server restart
- represent each entry by recipe/source key, selected recipe key/index, display
  label/output, output identity or alternatives, output count per batch,
  remaining output count, input groups, per-batch counts, and opaque/non-item
  diagnostics
- compute input requirements from output units:
  `ceil(remainingOutput / outputCountPerBatch)` batches
- clamp count controls to recipe-output batches so multi-output recipes do not
  drift onto unreachable odd/even counts through `+` / `-`
- recompute simple same-list producer floors when recipes are added or remaining
  counts change; floor by output units required by other active recipe entries
- keep craft-run state player-scoped and persisted as craft-run state only; do
  not persist it as a workflow tab, desired count, wanted count, accepted input,
  or recipe shortcut
- aggregate remaining run inputs to compute producer-count floors without a
  parent-child tree
- expose craft-run pressure through the normal workspace projection/chrome
  rather than a separate recipe-goal wall

Acceptance:

- adding one recipe creates one recipe entry with the requested remaining
  output count
- adding another recipe appends a flat sibling entry, not a child
- there is only one current run; adding the visible recipe always appends a
  flat entry to that current list
- closing/reopening EMI, switching workflow tabs, logout/rejoin, or server
  restart does not lose the current run
- output counts from multi-output recipes scale input requirements by batch
  count rather than by raw desired output count
- adding a producer recipe for an item required by other tracked recipes raises
  its remaining output count to at least the current requirement
- increasing a consumer recipe's remaining count recomputes and raises matching
  producer recipe floors
- completed recipe entries are removed and stop contributing ingredient pressure
- removing recipe entries clears their temporary pressure and leaves workflow
  membership/accepted inputs/desired counts/persistent wanted counts unchanged

#### Slice 5b: EMI Sidebar Actions

Status: landed for the single-visible-recipe EMI path.

- keep `RecipeIngredientSidebarSpec` as the visible-recipe source boundary
- add a sibling recipe-capture boundary for craft runs; do not overload the
  existing sidebar projection DTO with output/batch/run state
- add explicit actions for `Add Recipe`, `Set remaining count` / `Add N
  batches`, and `Done` / remove
- render per-visible-recipe `Add Recipe` actions and per-recipe sections in the
  wall list, not in a separate row under the workflow tabs
- require an explicit selected or hovered recipe when EMI shows multiple visible
  recipes
- keep loader-specific EMI/JEI objects out of common craft-run state
- preserve current transient recipe sidebar behavior when no run is active
- remove or retire legacy EMI goal buttons, generic drag/drop goal target, and
  goal-tab capture from the live EMI surface

Acceptance:

- opening an EMI recipe still shows the filtered sidebar before any recipe is
  added
- `Add Recipe` creates temporary missing/gather guidance for the selected or
  hovered visible recipe
- pressing `Add Recipe` again adds another flat entry and recomputes producer
  count floors
- `Done` removes only the selected recipe entry
- no `SLOT goal` recipe button, goal drag/drop target, or goal-tab capture UI is
  shown in EMI recipe screens after craft-run actions land

#### Slice 5c: Variant Choice Concretization

Status: deferred follow-up. The landed implementation preserves unresolved
ingredient groups and stages matching carried alternatives without forcing a
manual choice; explicit `Use this` UI/hotkey can be added after playtesting
shows that concrete run-scoped choice is needed.

- keep unresolved tag/list ingredient groups inside craft-run recipe entries
- do not expand large alternative lists into SLOT-owned chooser cards
- delegate unresolved ingredient exploration to EMI/JEI's normal ingredient/tag
  page
- expose `Use this` when the hovered/viewed concrete EMI/JEI item satisfies an
  unresolved group in the current run
- add a configurable hover hotkey for the same `Use this` action
- store concrete choices only in the current run
- validate `Use this` through complete captured matching data or a
  loader-compat matcher; if the group is opaque or matching data was truncated,
  fail closed with a small status instead of guessing
- display alternatives may be capped, but matching data used for staging and
  hotkey concretization must not silently use that cap as the truth

Acceptance:

- adding a variant-heavy recipe to a run does not require choosing concrete
  alternatives first
- clicking an unresolved group opens the corresponding EMI/JEI ingredient/tag
  view rather than a SLOT alternative browser
- hovering a concrete satisfying item in EMI/JEI can apply it through both the
  visible `Use this` action and the hotkey
- chosen alternatives affect the current run only and do not mutate workflows

#### Slice 5d: Acquisition Progress

Status: landed for workflow activity records that represent meaningful
acquisition kinds.

- wire meaningful acquired-item deltas into the craft-run service from the
  post-suppression carried-acquisition activity stream
- decrement matching recipe-entry remaining counts and remove entries that
  reach zero
- when one acquired stack matches multiple entries, consume the delta in stable
  visible run order until it is exhausted
- ignore internal moves, staging, hotbar/backpack rearrangement, cursor moves,
  SLOT transfers, deposits, and put-away
- do not use crafting-result events as the primary progress signal; they miss
  machine, storage, automation, and pickup flows

Acceptance:

- taking a matching machine/chest/storage/world/crafted output decrements the
  matching remaining count or removes the entry when complete
- duplicate matching output entries are decremented deterministically in visible
  run order
- moving a matching stack between carried sources does not decrement the count
- staging a matching stack into main inventory does not decrement the count
- acquiring extra output after the count reaches zero leaves it at zero
- the player can raise the count again after it reaches zero

#### Slice 5e: Stage Selected Recipe

Status: landed for carried-provider-to-player-main staging through the shared
transfer executor.

- implement server-authoritative staging from carried providers into player
  main inventory
- stage only the selected/current recipe entry
- never use the hotbar as the staging area
- recompute live carried sources and safe main-inventory destinations on the
  server
- compute deficits in recipe input order, first consuming matching stacks
  already in player main inventory, then staging only the remaining needed
  amounts from carried providers
- for unresolved variant ingredients, stage any matching carried item by normal
  source priority
- route mutations through the normal transfer/executor boundary; do not mutate
  vanilla inventory or provider slots directly from a UI/session handler
- keep staging simple: no modal choice flow and no special preflight UI beyond
  a small status when nothing stageable was moved
- surface `Make room` guidance through the existing active-workflow put-away
  path before staging when main inventory is too full

Acceptance:

- ingredients already in player main inventory are treated as ready for EMI
- ingredients in backpacks/carried providers are marked stageable and can be
  moved into safe main-inventory slots
- unresolved variant ingredients can stage matching carried stacks without
  requiring prior concretization
- staging unavailable ingredients leaves them for normal gather/find guidance
- staging with no matching carried inputs shows only a small `nothing stageable`
  style status

#### Slice 5f: Deferred Presets

- leave workflow-scoped recipe shortcuts/run presets out of the initial
  implementation
- keep a future hook in the model only if it does not complicate the current
  run path
- revisit only after playtesting shows repeated craft runs need contextual
  launchers beyond EMI/JEI favorites

Acceptance:

- initial craft-run implementation has no persistent recipe shortcut UI
- workflows are not mutated by browsing or running EMI recipes
- any later preset design remains a workflow-scoped launcher, not an EMI/JEI
  favorite clone

#### Slice 5g: Remove Legacy Recipe Goals

Status: landed.

- delete the old common recipe-goal model once craft-run state covers the live
  recipe intent path: `GoalDescriptor`, `GoalPlanState`, goal projection
  entries/requirements/choices/defaults, and the recursive
  `GoalProjectionService`
- remove `GoalWorkspaceClientState`, `GoalWorkspaceProjection`,
  `GoalWorkspaceProjectionCache`, `GoalWorkspaceIntegration`, and
  `GoalTabsUiBuilder`
- remove goal plan and goal recipe-default persistence services, file-store
  fields, codecs, and tests; no save migration or compatibility shim is needed
  because the mod is unreleased and saves may change shape freely
- remove NeoForge and Forge goal RPC payloads/handlers/messages, plus any
  workspace session/controller calls that save, remove, hydrate, or select goal
  tabs
- remove EMI goal adapters, recipe decorators, recipe goal buttons, generic
  goal drag/drop targets, and "SLOT goal" labels from both loader integrations
- remove `goalPlans` and `goalRecipeDefaults` from workspace view-model records,
  platform codecs, UI hydration paths, contextual suggestion scoring, and
  debug/test fixtures
- keep recipe-viewer integration routes that are still useful for craft runs,
  such as opening recipes/uses or reading the hovered EMI item, but rename them
  away from `Goal*` ownership while doing the cleanup
- update `docs/status.md`, [ADR 0007](../decisions/0007-emi-recipe-sidebar.md),
  and README references so they say the legacy goal system was removed and
  craft runs are the active recipe-intent surface

Acceptance:

- grepping production code for old goal-system entrypoints shows no live
  `GoalWorkspace*`, `GoalProjection*`, `GoalPlan*`, `SlotGoal*`, or
  `SLOT goal` UI/RPC surface
- workspace view-model snapshots and loader codecs no longer carry goal plan or
  goal recipe-default fields
- EMI recipe screens expose craft-run actions and the transient recipe sidebar,
  not goal creation
- contextual suggestions and put-away protection use workflows, wanted/desired
  counts, recents, and craft-run pressure where applicable; they do not inspect
  old goal plans
- tests formerly covering recipe goals are deleted or replaced with craft-run
  model/projection/staging/acquisition tests
- docs no longer describe the old goal system as live or pending cleanup

### Slice 6: Retire Kit-Only UI And Docs

Goal: finish the pivot cleanup around names and docs.

- keep user-facing Kit Rack language out of the live UI
- rename or delete Kit-only code paths that are no longer useful
- keep product spec, product direction, status, and README references pointed at
  workflow tabs
- move or trim superseded Kit planning docs

Acceptance:

- future docs describe workflow tabs as the task surface
- no active plan tells agents to continue Kit Rack-only work
- old Kit terminology remains only where code has not yet been renamed and is
  clearly transitional

## Non-Goals

- generic built-in modes such as `Craft`, `Clean Up`, or `Mining`
- automatic activity detection that creates or switches tabs
- arbitrary deep nesting beyond one parent plus variants
- recursive autocrafting
- remote storage or logistics-network behavior
- new global favorite/protection/always-carry concepts beyond `All` desired
  and wanted counts
- separate session scratchpad UI

## Code Pointers

Likely starting points:

- `KitDefinition`, `KitMap`, `KitWorkflowDomainService`,
  `KitActivation`, `KitPage`
- `DesiredCountWorkflowDomainService`
- `WantedCountWorkflowDomainService`
- `SlotWorkspaceViewModel`
- `WorkflowTabTargets`
- `WorkflowAcceptedInputOptions`
- `WorkspaceItemTargets`
- `WallSectionVisibility`, `WallSectionHeaderUiBuilder`
- `WorkspaceUiSessionMemory`, `RecentsStripUiBuilder`
- `WayfindingTarget` and `WayfindingDisplay`
- `KitGatherService`
- `KitPageCycleService`
- `LoadoutApplyService`
- `WorkspaceBeltCommandService`
- `KitRackUiBuilder`, `KitRackBuilder`, `BeltPanelBuilder`
- EMI recipe sidebar projection and wanted-count actions

Keep the implementation centered in `common/` for tab semantics. Loader UI code
should render tabs and forward intents, not decide inventory meaning.
