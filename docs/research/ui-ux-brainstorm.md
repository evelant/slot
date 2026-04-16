# SLOT UI/UX Problem Space And Design Brainstorm

Last updated: 2026-04-16

Status: exploratory design notes, not a normative spec. Historical UX
problem-space analysis that informed later direction. Concrete layout
proposals and near-term-experiment suggestions from earlier drafts have
been removed; the current chosen direction is the triage-first visual
atlas documented in [../design/atlas.md](../design/atlas.md), with buildable
slices in [../plans/atlas-prototype.md](../plans/atlas-prototype.md).

This document expands the UI/UX problem space for heavily modded Minecraft
inventory management and collects possible design directions for SLOT. It is
intended to frame the problem, not to freeze implementation.

For normative behavior, see [../product/spec.md](../product/spec.md).
For current architecture, see [../architecture/overview.md](../architecture/overview.md).
For current implementation status, see [../status.md](../status.md).

## Design Frame

Default Minecraft inventory is a strong small-inventory UI that becomes a weak
large-modpack UI. Its core problem is not only "too few slots." The deeper
problem is that the UI asks the player to use visual recognition, spatial
memory, and manual stack movement in a state space that grows beyond what those
tools can comfortably handle.

SLOT should not simply make the grid bigger. A bigger grid preserves the same
failure mode: more icons, more movement, more scanning, more manual routing.

The stronger opportunity is to make inventory behave more like a player-facing
workflow surface:

- browse by meaning, not only by position
- act on items without losing source authority
- make recent change visible
- make common task transitions cheap
- make cleanup safe and reversible where promised
- preserve enough physical affordance that the inventory still feels like the
  player's carried stuff, not an abstract database

## Default Inventory Strengths

These strengths matter. SLOT should avoid throwing them away blindly.

### Strong Spatial Affordance

The grid feels like a bag, pouch, or set of pockets. Players understand that an
item occupies a slot and that moving it changes where it lives.

Design implication:

- even if SLOT uses lists, it should preserve visible source/slot affordances
  where they affect trust or action correctness
- quick access and equipment should remain more physical than ordinary storage

### High Information Density

A 3x9 grid plus hotbar shows many item icons in little space. Expert players can
scan a known inventory quickly.

Design implication:

- list-first views need compact rows, dense modes, or hybrid icon strips so the
  UI does not feel bloated
- dense secondary views may still be valuable for known-small item sets

### Direct Manipulation

Clicking, dragging, splitting, swapping, and dropping stacks are tactile and
learnable. The player feels in control.

Design implication:

- SLOT should keep direct manipulation for quick access, cursor interactions,
  and exact-slot operations
- higher-level actions should show what they will do before applying when risk
  is nontrivial

### Stable Vanilla Vocabulary

Players already know hotbar, offhand, armor, cursor, shift-click, right-click,
and drop.

Design implication:

- SLOT can innovate on presentation, but it should reuse familiar verbs where
  the semantic behavior is truly the same
- when behavior differs, it should be labeled directly rather than disguised as
  vanilla

### Honest Scarcity

The grid makes limited space visible. Running out of room is obvious.

Design implication:

- aggregated list views still need to make capacity, overflow, protected
  slots, and full destinations visible
- SLOT should not hide inventory pressure until an action fails

## Default Inventory Pain Points In Modded Play

### Icon Entropy

Heavily modded games introduce hundreds or thousands of unique icons. Many are
visually similar, low contrast, or unfamiliar.

Symptoms:

- scanning becomes slow and error-prone
- visual search requires tooltips
- item variants with identical icons are nearly invisible as distinct objects
- texture-pack differences can break learned recognition

Potential responses:

- list rows with item names and subtitles
- compact item-id or mod-name secondary text
- variant-aware labels for potions, enchanted books, filled maps, tools,
  storage disks, bees, upgrades, and NBT-heavy items
- category and mod-origin badges
- "ambiguous icon" treatment where the label is visually dominant

### Spatial Memory Breaks Down

Spatial memory works when items stay put. It fails when pickups, sorting,
restacking, crafting, backpacks, and transfers constantly move stacks around.

Symptoms:

- "I know I had it" becomes a manual scan
- recently acquired items disappear into the grid
- sorting improves order but destroys memory
- moving between screens changes where items appear

Potential responses:

- stable row ordering by semantic identity rather than volatile slot position
- recently changed highlights
- "pinned row" or "keep near top" behavior for important identities
- source badges so movement is explainable
- a recent-acquisition rail independent of current sort order

### Carried Storage Is Fragmented

Backpacks, pouches, curios, offhand, main inventory, and hotbar are all carried
by the player but often require separate UI visits and manual shuffling.

Symptoms:

- the player uses main inventory as a temporary transfer buffer
- moving backpack A to backpack B through the player grid is tedious
- deposit and withdraw flows require remembering where items live
- expanded inventory increases power but also increases housekeeping

Potential responses:

- unified carried browsing with source-aware actions
- source badges and expandable "where is it?" details per row
- direct carried-source to carried-source transfers where supported
- source filters for "main only", "backpacks", "hotbar", "offhand"
- staging-free transfer planning that never requires the 3-row grid as a human
  scratchpad unless authority requires it

### Hotbar Is Both Toolbelt And Transfer Target

The hotbar is quick-access muscle memory, but it is also a physical inventory
region. Those roles conflict.

Symptoms:

- one-off usable items require disturbing a stable hotbar
- task changes require rebuilding the hotbar
- accidental transfers displace combat/building tools
- loadout state is mental, not represented by the UI

Potential responses:

- treat hotbar as a visible action rail, not just inventory rows
- exact assignment semantics with clear displacement behavior
- one-off use action that temporarily stages an item into hand, uses it, then
  restores the previous hand item when safe
- named task loadouts for exploration, combat, building, mining, farming, and
  tech work
- hotbar slot protection or "lock this slot for this task"
- ghost placeholders for missing loadout items

### Junk Accumulates Faster Than The Player Can Triage

Modded play creates many low-value item pickups: blocks, seeds, mob drops,
fragments, partial tools, loot-table leftovers, quest artifacts, and byproducts.

Symptoms:

- one useful item pickup often comes with many slot-filling side pickups
- trashing is slower than picking up
- dropping items on the ground often re-picks them up
- players delay cleanup until inventory pressure interrupts gameplay

Potential responses:

- recent pickup triage surface
- junk tags and low-value rules
- explicit trash action with undo/recovery token
- explicit void action for irreversible deletion
- "ignore pickup for this identity for N seconds" or "do not re-pick up until
  moved away" where technically possible
- cleanup preview grouped by reason: junk, duplicate low-value blocks, overflow,
  unprotected mob drops
- conservative auto-trash only for player-approved identities and contexts

### Recent Change Is Hidden

The default grid shows current state, not what changed.

Symptoms:

- after combat/mining/looting, the player cannot tell what was new
- quest or mod reward items are easy to miss
- cleanup is reactive rather than continuous
- Recent is confused with "currently in inventory" unless modeled explicitly

Potential responses:

- Recent rail or inbox driven by activity events
- row badges for newly acquired, increased count, decreased count, moved,
  crafted, or withdrawn
- "triage complete" dismissal by identity and activity sequence
- recently acquired rows remain visible across sort/filter changes until
  dismissed
- contextual producer labels: pickup, crafted, withdrawn, reward, trade, loot

### Tooltip-Only Identity

Some item families cannot be distinguished by icon alone.

Examples:

- enchanted books
- potions
- bees and spawn eggs
- filled maps
- modded upgrades
- storage cells or disks
- tools with enchantments or damage states
- NBT-configured machines or cards

Symptoms:

- players must hover many items in sequence
- identical icons hide meaningful variants
- search often misses tooltip-only data if not indexed

Potential responses:

- subtitle extraction for high-value item families
- row secondary text such as "Efficiency IV", "Night Vision 8:00", "Map #12",
  "63k ME Storage Cell"
- variant chips and badges
- expanded row details on focus rather than hover-only discovery
- item-family-specific comparators for grouping and search

### Task Context Is Not Represented

Inventory usefulness depends on what the player is doing. Default inventory has
no task model.

Symptoms:

- players rebuild the same hotbars repeatedly
- exploration, boss combat, building, farming, mining, and factory work compete
  for quick-access slots
- "items I need for this task" are mixed with everything else
- missing task items are not visible until the player needs them

Potential responses:

- named task contexts
- task loadouts for hotbar, offhand, armor, and optional carried items
- collection-linked desired counts
- missing-item placeholders with "find/withdraw/craft" actions
- "prepare task" flow that pulls accessible carried/external items into the
  right surfaces
- protection rules scoped by active task

### External Storage Comparison Is Awkward

The default dual-pane container model is simple but weak when carried storage is
larger than vanilla main inventory or external storage is provider-backed.

Symptoms:

- carried contents and external contents are hard to compare
- duplicate management is manual
- deposit actions do not explain what moved
- terminal lists and grids do not share a stable interaction vocabulary

Potential responses:

- dual-pane comparison by category, mod, collection, recent, or search result
- row-level "carried count / external count" comparison
- deposit preview by item family
- source-aware transfer details in an expandable drawer
- provider uncertainty surfaced as a diagnostic rather than hidden

### Action Risk Is Poorly Communicated

Default inventory actions are direct but not always clear in complex contexts.

Symptoms:

- shift-click destination may be surprising
- broad actions can move protected or important items if not guarded
- full destinations fail silently or scatter results
- users cannot tell whether an action is reversible

Potential responses:

- action labels that include destination and count
- protected item indicators
- reversible versus irreversible action styling
- dry-run previews for broad cleanup, sort, loadout, and void flows
- partial-success diagnostics tied to concrete source entries

## Design Principles For SLOT UI Experiments

### Preserve Authority, Innovate Presentation

The UI can group, label, sort, and annotate aggressively. It cannot invent
storage truth. Every visible affordance that mutates inventory must resolve back
to exact authority.

### Prefer Recognition By Meaning Over Recognition By Icon

Icons are useful but insufficient in modded play. Names, categories, subtitles,
source badges, producer labels, and task context should carry more of the
recognition burden.

### Make Change First-Class

Inventory is not just a set of items. It is a stream of acquisitions, movements,
crafts, withdrawals, losses, and cleanup decisions. UI should expose recent
change directly.

### Make Task Switching Cheap

The player should be able to move from mining to building to combat without
rebuilding the same hotbar and carried kit by hand each time.

### Separate Quick Access From Storage

Hotbar, offhand, armor, and tools are active-use surfaces. Treating them as just
more storage slots causes friction and accidental disturbance.

### Keep Cleanup Explicit And Trustworthy

Trash, void, and auto-triage are powerful. They must be opt-in, explainable,
guarded by protection policy, and reversible where promised.

### Offer Progressive Density

Different moments need different density. Searching for a specific enchanted
book benefits from labels. Quickly checking blocks may benefit from dense icon
clusters. SLOT should support multiple densities over the same state.

### Avoid File-Manager Sterility

A list can solve scanning but can also make inventory feel like a spreadsheet.
Use physical zones, source badges, item cards, rails, and tactile actions to
retain the feeling that these are carried objects.

## UI Building Blocks To Explore

### Item Rows

List-first row optimized for scanning.

Possible contents:

- icon
- item name
- variant subtitle
- total count
- source badges
- category/mod badge
- recent/protected/favorite/junk/loadout chips
- primary action affordance
- hover shortcuts

Useful for:

- high-entropy item sets
- NBT-heavy variants
- search results
- recent triage

Risk:

- lower density than grid
- can feel too abstract if source and physicality are hidden

### Dense Icon Strip

Compact icon-only or icon-plus-count strip for known item groups.

Useful for:

- blocks within one category
- quick scanning of low-risk groups
- preserving the bag-of-stuff feel
- secondary surfaces where names are less important

Risk:

- repeats the default grid's icon entropy if overused

### Hotbar Action Rail

A physical quick-access rail that keeps the hotbar visually distinct from
ordinary storage.

Possible features:

- slot numbers
- selected slot state
- locked/protected indicators
- task loadout ghosts
- drag/drop or click-to-assign behavior
- displaced item preview
- one-off use target

Useful for:

- preserving muscle memory
- making assignment semantics obvious
- supporting task loadouts

### Recent Inbox

A dedicated surface for newly acquired or changed items.

Possible features:

- grouped by acquisition producer
- dismiss/keep/junk/favorite actions
- "move to storage", "assign to hotbar", "trash", "void" actions
- count delta display
- triage completion state

Useful for:

- making loot/combat/mining results legible
- continuous cleanup
- preventing new useful items from being buried

### Category Rail

Coarse first-pass filter.

Possible categories:

- Blocks
- Tools
- Combat
- Food
- Ingredients
- Machines
- Magic
- Storage
- Consumables
- Recent
- Favorites
- Junk
- Task

Useful for:

- reducing search space before text search
- controller/keyboard navigation
- giving the UI a game-like structure

Risk:

- bad categorization creates frustration
- categories must be overrideable and searchable

### Source Drawer

Expandable details for where a row's items actually live.

Possible contents:

- player main count
- hotbar count
- backpack counts by source
- external count in dual-pane mode
- exact backing entries for advanced users
- source-specific actions

Useful for:

- preserving trust in aggregation
- debugging partial actions
- avoiding fake authority

### Action Palette

Contextual menu or command palette for selected row/surface.

Possible actions:

- move one
- move stack
- move all visible exact
- move all visible
- assign to hotbar slot
- use once
- favorite
- add to collection
- mark junk
- trash
- void
- protect
- inspect sources

Useful for:

- keeping the main UI clean
- exposing advanced actions without permanent clutter
- keyboard-driven workflows

Risk:

- hidden actions reduce discoverability
- common actions still need visible affordances or stable shortcuts

### Task Lens

A mode focused on one activity: building, combat, exploration, mining,
farming, factory work, magic, or a user-defined task.

Possible features:

- loadout rail
- desired carried counts
- missing item placeholders
- protected task items
- task-specific category ordering
- "prepare" action
- "cleanup after task" action

Useful for:

- reducing repeated setup
- making inventory support gameplay rather than interrupting it

### Triage Lane

A high-speed processing lane for low-value or recently acquired items.

Possible actions:

- keep
- favorite/protect
- add to collection
- move to storage
- trash
- void
- always treat like junk

Useful for:

- post-mining or post-combat cleanup
- avoiding the "throw it away, pick it back up" loop

Risk:

- too much automation can feel unsafe
- needs excellent undo/recovery semantics for trash

### Compare Pane

Dual-pane view that compares carried and external contents by identity instead
of merely showing two unrelated grids.

Possible row display:

- item
- carried count
- external count
- delta/action affordance
- desired count or collection membership

Useful for:

- deposit/withdraw decisions
- storage cleanup
- restocking task kits

### Inspector Panel

Focused detail panel for the selected item or row.

Possible contents:

- full tooltip
- source breakdown
- recent activity
- collection memberships
- loadout usage
- protection reason
- available actions
- recipe or use hints where available

Useful for:

- avoiding hover-only tooltip hunting
- making complex item variants understandable

## Interaction Ideas

### Hover Shortcuts

Keyboard shortcuts act on the hovered or focused row.

Candidate mappings:

- `1-9`: assign selected/hovered item to hotbar slot
- `E` or `Enter`: primary action
- `Q`: trash or drop, depending context and safety
- `Shift+Q`: void only if enabled and confirmed
- `F`: favorite
- `J`: mark junk
- `P`: protect
- `R`: reveal sources or recent activity
- `Ctrl+Click`: move one
- `Shift+Click`: move stack
- `Shift+Ctrl+Click`: move all exact visible type

Design rule:

- shortcuts should mirror visible affordances and report exactly what action
  they triggered

### One-Off Use

A command that uses an item without permanently disturbing hotbar layout.

Possible implementation semantics:

- if the item can be used from inventory, invoke directly
- otherwise stage the item into selected hand, use it, then restore the prior
  hand item when safe
- if restore is unsafe, surface a clear diagnostic and leave the resulting
  authoritative state visible

Good candidates:

- food
- potions
- rockets
- ender pearls
- backpacks or portable tools
- modded activation items

Risk:

- vanilla use semantics are hand/state-sensitive; this needs conservative
  per-item behavior and fail-closed diagnostics

### Stable Row Order With Change Highlights

Instead of constantly resorting visible rows, preserve stable order within a
session and animate or badge count changes.

Possible policies:

- category-first, then pinned/favorite, then stable name order
- recent acquisitions temporarily float to Recent but not necessarily the main
  list
- item rows keep their place when counts change

Benefit:

- reduces the "everything moved" problem while keeping search useful

### Cleanup Preview

Before trash/void/bulk-deposit, show a grouped preview.

Possible groups:

- known junk
- low-value blocks
- duplicate tools
- unprotected mob drops
- recently acquired
- protected and skipped
- uncertain and skipped

Benefit:

- broad actions become powerful but inspectable

### Missing Item Placeholders

Rows for desired-but-missing items.

Use cases:

- collection desired counts
- loadout missing item
- recipe preparation
- task kit setup

Possible actions:

- search carried
- search external
- craft if recipe available
- pin to task
- dismiss for now

### "Where Did It Go?" Feedback

After an action, briefly show movement feedback.

Examples:

- "Moved 32 Stone to Hotbar 3"
- "Displaced 23 Inventory Cable to Main Inventory"
- "Moved 12 Torches from Backpack A to Chest"
- "Skipped 1 Shulker Box: protected portable container"

Benefit:

- reduces distrust in aggregated and broad actions

## Visual And Affordance Directions

### Maintain Physical Zones

Even list-first UI should have distinct physical-feeling zones:

- hotbar rail
- equipment rack
- backpack/source stack
- recent inbox
- action tray
- external container pane

This helps avoid pure file-manager feel.

### Use Labels Without Losing Icons

Icons remain useful anchors. Labels solve the modded recognition problem.

Good default row balance:

- icon for recognition
- name for certainty
- subtitle for variant
- compact badges for metadata

### Make Risk Visually Obvious

Irreversible or broad actions need stronger styling than reversible local
movement.

Potential visual language:

- neutral: transfer, assign, inspect
- blue/green: keep, favorite, prepare, restore
- amber: broad move, sort, cleanup preview
- red: void, irreversible delete
- locked/shield icon: protected

### Animate Meaningful Change Only

Useful motion:

- new item row reveal
- count increment pulse
- row-to-hotbar assignment trace
- displaced item trace
- trash/void confirmation transition

Avoid:

- constant decorative animation
- moving rows just because counts refresh

## Feature Ideas By Problem

### Finding Items

- search by item name, mod, category, tag, tooltip-derived variant, collection,
  and source
- category rail with stable custom ordering
- ambiguous-icon subtitle extraction
- fuzzy matching for long modded names
- "show only items usable now" context filter
- "show items related to selected task"

### Reducing Slot Pressure

- junk marking and cleanup preview
- recent triage lane
- deposit all unprotected junk
- void action for explicit irreversible cleanup
- protected portable container rules
- "keep N" desired-count cleanup

### Improving Hotbar/Use Flow

- hotbar loadouts
- task-specific quick-access profiles
- one-off use without permanent hotbar disturbance
- exact assign by pressing number over a row
- slot locks
- displaced-item preview

### Improving Transfers

- direct carried-source to external transfer without main-inventory staging
- compare pane with carried/external counts
- move one/stack/all exact/all visible as visible actions
- source-aware partial-success diagnostics
- transfer preview for broad actions

### Improving Recent/Triage

- activity-backed Recent inbox
- producer labels
- dismiss-by-identity sequence watermarks
- keep/trash/favorite/protect actions
- "new since last opened" grouping
- "recently changed count" badges in main list

### Improving Task Switching

- task lenses
- collection-linked loadouts
- desired carried counts
- missing placeholders
- prepare task action
- cleanup after task action
- task-scoped protection

### Improving Trust

- source drawer
- action result toasts/status rail
- diagnostics in plain language
- undo/recovery for trash where promised
- fail-closed unsupported host messaging
- visible protection reasons

## Visual And Tactile Design Concepts

The strongest direction may not be "list replaces grid." It may be a family of
views that preserve the grid's object feel and spatial memory while giving the
player semantic lenses for category, task, recency, and cleanup.

### Persistent Item Homes

Give each item identity a stable visual home once the player places or pins it.
This line of thinking became the atlas direction — for the deeper design, see
[../design/atlas.md](../design/atlas.md).

Core idea:

- when a player manually places an item row/icon into a visual location, SLOT
  remembers that preferred visual position for that identity
- future copies of that item appear in or near that home when possible,
  regardless of exact backing source
- missing items can show as ghost placeholders in their homes

Why it works:

- leverages strong spatial memory
- supports "I always know where my rockets/books/building blocks are"
- combines grid familiarity with semantic aggregation
- task kits and collections can become physical boards of item homes

Possible scopes:

- global item home
- collection-specific home
- task-specific home
- source-specific home for players who want exact storage identity
- external-storage home when browsing a known chest or terminal

Open question:

- should item homes be automatically learned from repeated placement, explicitly
  pinned, or both?

Risk:

- a persistent home can lie if the item is not actually present; ghost
  treatment must be very clear
- placement persistence can become another thing to manage unless the UI makes
  it effortless

### Stable Layout With Lenses

Avoid reflow when possible. Let views act like lenses over stable positions.

Examples:

- search dims and outlines matches instead of rebuilding all rows
- Recent lens highlights recently changed items in place and opens a side inbox
- collection lens draws colored rings around collection members
- junk lens tints junk candidates and reveals cleanup controls
- task lens draws paths from required items to hotbar/loadout targets

Why it works:

- spatial memory survives filtering
- different workflows become overlays, not separate worlds
- the player sees how workflow meaning relates to the same carried objects

Risk:

- if too many overlays stack, the UI becomes noisy
- some searches still need a compact result list for precision

### Source Constellations

For a selected row, show backing sources as small orbiting icons or pips.

Example:

- Stone row shows total `74`
- small pips show `32 main`, `18 backpack`, `24 chest`
- clicking a pip expands exact slots or source actions

Why it works:

- aggregation remains trustworthy
- source identity is available without dominating every row
- partial transfers can explain exactly which pip changed

Risk:

- source pips need consistent visual grammar

### Animated Provenance Trails

Use short path animations to show where items moved.

Examples:

- selected list row sends an icon trail to hotbar slot 3
- displaced hotbar item trails back to main inventory or source drawer
- cleanup items trail into trash tray
- external deposit trails across the pane boundary
- recent pickup enters through an inbox edge

Why it works:

- broad or abstract actions become physical
- helps players trust assignment/displacement
- reinforces that SLOT is moving real objects, not changing a database row

Rule:

- animate action meaning, not every refresh

### Task Mats

A task mat is a physical preparation surface for a gameplay mode.

Examples:

- Building mat: blocks, scaffolding, tools, food, flight items
- Combat mat: weapons, shields, potions, food, charms
- Farming mat: hoe, seeds, bonemeal, buckets, shears
- Mining mat: pickaxes, torches, food, repair items, backpack filters

Visual model:

- task has named slots or groups
- present items are solid
- missing items are ghosted
- protected loadout targets are visibly locked
- "prepare" moves accessible items into place

Why it works:

- hotbar loadouts become visible and tactile
- task switching stops being a memory chore
- missing items are visible before the player leaves base

Risk:

- task setup UI must be fast or players will ignore it

### Recent Pile / Loot Inbox

Recent items can be shown as a physical pile that needs triage.

Possible interaction:

- newly acquired items land in the inbox
- player can flick items to Keep, Junk, Storage, Favorite, Task, Trash, Void
- dismissed items leave the inbox but remain in main inventory
- dangerous actions require preview/confirmation

Why it works:

- mirrors the feeling of emptying pockets after a trip
- makes triage a satisfying ritual
- stops new items from being buried

Risk:

- if inbox becomes mandatory busywork, it adds friction
- needs "dismiss all safe" and strong defaults

### Junk Sieve

A cleanup surface that visually separates low-value material from important
items.

Possible model:

- items flow through a sieve with lanes:
  Keep, Store, Junk, Trash, Void, Protected
- rules are visible and editable
- uncertain items stay out of destructive lanes

Why it works:

- makes automation legible
- turns cleanup into reviewable triage instead of hidden deletion

Risk:

- automated cleanup must be conservative; trust is easy to lose

### Loadout Mannequin / Equipment Rack

Represent hotbar, offhand, armor, and important carried items as a rack or
mannequin rather than a list.

Possible features:

- active equipment slots are fixed physical landmarks
- hotbar slots are numbered hooks
- missing desired items appear as silhouettes
- slot locks are visible clasps
- one-off use items can be temporarily staged on a "hand" hook

Why it works:

- equipment has stronger physical affordance than storage
- task loadouts become easy to understand

Risk:

- must coexist with the normal hotbar rail without duplicating state

### Collection Boards

Collections can be shown as boards with pinned item homes.

Examples:

- "Building Blocks" board
- "Boss Kit" board
- "Automation Parts" board
- "Magic Ritual" board

Features:

- desired count labels
- missing placeholders
- source availability badges
- prepare/withdraw/deposit actions
- manual spatial arrangement

Why it works:

- collections become tangible project surfaces
- desired counts and missing items are visual, not buried in metadata

Risk:

- manual boards are powerful but need easy defaults

### External Chest Memory

For known external containers, remember visual positions or category pockets.

Core idea:

- a chest or terminal can have a stable "map" remembered by container identity
- item finder behavior can highlight where an item belongs or was last seen
- external storage can participate in the same category/geography system as
  carried inventory when identity is stable

Why it works:

- extends spatial memory beyond the player inventory
- helps bases where chests have semantic roles
- supports "where did I store this?" without remote storage semantics

Risk:

- container identity must be reliable
- provider-backed terminals may not support stable slot positions, so this may
  be a lens rather than a physical map

### Search As Spotlight

Search can be visual rather than purely list-filtering.

Options:

- non-matches dim in place
- matches glow in their stable homes
- paths draw from category labels to matching items
- exact matches float into a temporary result tray
- repeated searches teach item homes by keeping stable context visible

Why it works:

- search stops destroying spatial context
- players learn where items live even while searching

### Gesture-Based Sorting Magic

Use tactile gestures for organization.

Examples:

- drag an item into a category pocket to teach classification
- drag an item to a collection board to add it
- drag an item to a junk sieve lane to mark junk
- drag a row to a hotbar hook to assign
- drag a category header to reorder the bag geography
- pinch/collapse category pocket to compact it

Why it works:

- organization becomes direct manipulation, not settings menus
- the UI learns from player actions

Risk:

- gestures need visible alternatives for discoverability and accessibility

### Modal Lenses Instead Of Modal Screens

Keep the physical workspace stable while changing what overlays mean.

Lenses:

- Browse lens: normal item finding
- Recent lens: changes and triage
- Task lens: loadout and desired items
- Cleanup lens: junk/trash/protection
- Compare lens: carried versus external
- Craft lens: ingredients and outputs

Why it works:

- one workspace, multiple meanings
- preserves orientation better than completely separate screens

Risk:

- lens state must be obvious so players know what actions will do

## Open Design Questions

- What is the default first surface when opening inventory: all carried items,
  recent triage, active task, or last used view?
- How much row density is acceptable before list scanning becomes too slow?
- Should categories be automatic-first, user-custom-first, or mixed?
- How should we expose powerful cleanup without making players afraid of the
  UI?
- Should task lenses be lightweight tags, full loadout objects, or collection
  views with desired counts?
- How much direct manipulation should list rows support before the UI becomes
  gesture-heavy and hard to learn?
- What belongs in the always-visible hotbar rail versus an inspector panel?
- How do we show carried source identity enough for trust without overwhelming
  ordinary browsing?
- Which item families deserve custom subtitle extraction first?
- How should SLOT coexist visually with EMI/JEI overlays and recipe panels?

## Evaluation Criteria For Experiments

Each UI experiment should be judged by whether it improves the underlying job,
not by whether it looks novel.

Questions:

- Can the player find a known item faster than in the grid?
- Can the player distinguish similar item variants without tooltip hunting?
- Can the player triage new pickups without losing useful items?
- Can the player switch tasks without rebuilding a hotbar by hand?
- Can the player move items between carried storage and external storage
  without using main inventory as a manual buffer?
- Are broad actions understandable before and after execution?
- Does the UI preserve trust in where items really live?
- Does it still feel like managing carried equipment, not editing a database?
- Does it keep direct manipulation where direct manipulation is better?
- Does it fail closed when authority or compat support is incomplete?

## Where Experiments Live Now

Near-term engineering experiments have moved to
[../plans/atlas-prototype.md](../plans/atlas-prototype.md) and
[../plans/current.md](../plans/current.md). The list-first experiment set
that appeared here previously (item-row hardening, recent-inbox surface,
source drawer, hover shortcuts, task/loadout panel) is superseded by the
atlas slice sequence.
