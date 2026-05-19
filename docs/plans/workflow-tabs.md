# Workflow Tabs Plan

Last updated: 2026-05-19

Status: core implementation and the first playtest polish pass have landed.
Workflow tabs now support active-tab filtering, one-level variants, tab-local
desired/wanted targets, accepted exact/tag inputs, accepted proximate substitute
ghosts, compact nearby headers, two-row Recents, hidden noisy suggestion rows,
search/keybind polish, shared tool/container/display-storage target resolution,
and the adjacent junk/trash pressure-relief slice. Remaining follow-ups are
recipe import/staging, destination highlighting/wayfinding polish for put-away,
reorder UI, and tab duplication/rename polish. This supersedes Kit Rack / Kit
prototype work as the task-workflow direction while reusing the current Kit,
desired-count, wanted-count, gather, loadout, and storage code wherever it
already fits.

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
use. Carried items not relevant to `All` or the active tab get put-away
guidance.

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
- Useful Now and Put Away suggestion lanes are currently hidden in the rendered
  wall while their projection/scoring code remains in place for later playtests.
  Put-away card state and bulk deposit still use the same eligibility logic.
- Two rows of Recents render above the wall. Search auto-commits after about two
  seconds idle, clears after the interface has been closed for about ten seconds,
  and can be cleared by right-clicking the search label. Text inputs suppress
  workspace hotkeys while focused.
- The configurable move-to-main-inventory key defaults to grave accent. It moves
  a hovered identity from backpack/hotbar into main inventory, or from main
  inventory back into backpack storage when provider space exists.

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
- carried items that are not relevant only when they have put-away card state

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

Movable identity matching is shared in `common/`, not per workflow action.
`ITEM_ID` targets are broad matches for the same item id. Stack-created
identities collapse to item-id when shared signals say their component data is
condition rather than identity: Minecraft damageability, registered portable
container classifiers, or single-field damage/container fingerprints. Do not
add item-name token exceptions for one modpack item family; add a shared signal
or classifier instead.

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

Put-away guidance is the inverse of active-tab relevance.

A carried identity is eligible when:

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

Current UI hides the experimental Put Away suggestion row, but the projection
and card state are still live: active-tab-irrelevant carried cards can remain
visible with put-away border/chrome, and bulk deposit uses the same eligibility
and protection logic. Put-away guidance should continue to use a distinct visual
language from gather guidance, including a distinct chest world-highlight color.
Acquisition and cleanup are opposite intents; they should not look identical.

## Bulk Hotkeys

The common bulk actions need first-class keybindings because they interrupt
almost every workflow and should not require finding a small UI button:

- **Bulk gather active tab:** gather missing `All + parent + active tab`
  targets from proximate storage through the existing server-authoritative
  gather path.
- **Bulk deposit active tab clutter:** deposit carried identities that are not
  relevant to `All`, the active parent, or the active tab/variant. This is the
  hotkey version of put-away guidance.
- **Trash hovered identity:** delete carried stacks matching the hovered item
  type, with undo, while marking that identity as junk for later pickup pressure
  relief.
- **Move hovered identity to main/backpack:** grave accent by default. If the
  hovered identity is in a backpack or hotbar, move it to main inventory; if it
  is already in main inventory, move it back into backpack storage when provider
  space exists.

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
- add visible EMI recipe ingredients to current tab
- create new tab from visible EMI recipe ingredients
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

The EMI recipe sidebar should feed workflow tabs instead of becoming a separate
goal surface.

High-value actions:

- `Add recipe to current tab`
- `New tab from recipe`
- `Want ingredients for 1 craft`
- `Want ingredients for N crafts`
- `Want one of each visible ingredient` for locate/guidance-only use

EMI-specific friction to address after tab import works:

- **Stage for EMI:** move gathered ingredients from carried providers into
  player main inventory slots EMI can consume from, without abusing the hotbar
  as the staging area.
- **Make room first:** if player main inventory is too full to stage recipe
  inputs, suggest or perform put-away actions for active-tab-irrelevant carried
  items before staging.

This remains source-aware and server-authoritative. SLOT should not pretend EMI
can pull from carried providers that EMI cannot actually see.

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
- when carried storage is over 75% full, newly picked-up junk identities are
  deleted before backpack reroute

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

Status: put-away card state, protected bulk deposit, no-home card chrome,
content-backed display-storage deposit, display-storage undo ids, active-chest
range gating, and cross-loader put-away hotkey landed. The explicit Put Away
suggestion row is currently hidden for playtesting. Destination
highlighting/wayfinding polish remains follow-up work.

Goal: make cleanup as first-class as acquisition.

- compute active-tab-irrelevant carried identities
- render put-away guidance on normal cards or in a compact guidance lane
- route put-away actions through existing deposit planning
- add a configurable bulk-deposit hotkey that deposits active-tab-irrelevant
  carried identities through the same planning path
- add destination highlighting distinct from gather/acquisition highlighting
- add wayfinding for "go here to put this away", not only "go here to get
  this"

Acceptance:

- carried items outside `All` + active tab show put-away guidance when a route
  is known
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

Status: core tab rendering, activation, variant display, visible create flow,
membership/accepted-input menus, wider right-click menus, active-tab filtering,
two-row Recents, search idle commit/clear, header reveal clicks, and
main-inventory move keybind landed. Remaining editing polish is tracked as
follow-up work in `current.md`.

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

### Slice 5: EMI Recipe Import And Staging

Goal: make recipe workflows feed tabs with minimal ceremony.

- add current visible recipe ingredients to current tab
- create a new tab from current visible recipe ingredients
- set tab wanted/desired counts from recipe ratios
- preserve current EMI recipe sidebar behavior as the source of visible
  ingredients
- add "stage for EMI" from carried providers into player main inventory

Acceptance:

- importing a recipe activates guidance for missing ingredients
- "want one of each" is available for locate-only workflows
- staging does not move items to hotbar just because EMI needs main inventory
- staging fails closed with a clear status when main inventory has no safe room

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
