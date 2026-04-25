# Atlas Prototype Plan

Last updated: 2026-04-16

Status: implementation-facing plan for the triage-first carried atlas.

This plan turns the atlas concept in [../design/atlas.md](../design/atlas.md)
into buildable slices. It states what to build now, what to defer, and which
decisions still block a robust design. For the near-term engineering sequence
across the whole project, see [current.md](current.md).

## Decision: Triage-First Carried Atlas With Player-Authored Homes

The current prototype direction is a carried-inventory atlas where new item
identities arrive in a first-class triage area and the player gives them stable
visual homes. Automatic categorization stays conservative: SLOT only auto-homes
items when confidence is very high; everything else lands in `Triage` until the
player decides.

The existing prototype has already proven the core surface:

- LDLib2 `GraphView` pan/zoom works
- atlas cards can drive the existing hotbar transfer RPC
- progressive disclosure by zoom level is viable
- translucent workspace / search overlay is viable

The next design target is not richer automatic taxonomy. It is the item-home
workflow:

- player inventory only
- `Triage` island for unhomed, new, and ambiguous identities
- a small number of high-confidence starter islands
- player-authored item homes, initially in memory only
- existing main-to-hotbar and hotbar-to-main behavior remains unchanged
- search highlights / navigates stable homes rather than replacing the map

This tests the central UX question: can players quickly give new item types a
memorable place, then rely on that place forever unless they move it?

Durable persistence and external-storage authority are still deferred until the
home/triage loop proves useful.

## Implementable MVP

### User Experience

The inventory opens into a map-style view with:

- a large canvas holding a `Triage` island and a few starter islands
- item cards/icons at stable deterministic coordinates
- labels for islands/regions
- current item counts
- hotbar rail fixed on the side or bottom
- status/diagnostic bar

Interactions:

- scroll or drag navigates the canvas
- clicking an item card selects its first backing main-inventory stack
- clicking a hotbar slot assigns that selected stack to the exact slot
- clicking an occupied hotbar slot selects it
- "move selected to inventory" transfers hotbar to main inventory
- search highlights matching homes in place
- unhomed items can be assigned to an island/home through the next prototype
  interaction

### Non-Goals For MVP

Do not implement yet:

- durable persisted homes
- external source memory
- trails to in-world chests
- task boards
- optional recent recap ribbon
- multi-select
- physical source sorting
- world position tracking

Manual home assignment is no longer a non-goal. It is the next prototype slice.
In-memory home state first; persistence intentionally deferred.

## Data Model

Add a server-synced atlas DTO for the workspace view plus prototype home
assignment state. The existing list DTO may be reused as a migration scaffold,
but the intended public workspace model is atlas/hotbar, not list/hotbar.

### `SlotWorkspaceAtlasViewModel`

Fields: `revision`, `status`, `diagnostics`, `pendingCount`,
`selectedQuickAccessSlot`, `canvasWidth`, `canvasHeight`, `regions`, `islands`,
`homes`, `triageHomes`, `hotbarSlots`.

### `AtlasRegion`

Fields: `regionId`, `label`, `x`, `y`, `width`, `height`, `color`, `itemCount`.

Regions are broad map zones. Islands are the player-facing organizational
objects inside them. A region may contain one or more islands.

### `AtlasIsland`

Fields: `islandId`, `label`, `x`, `y`, `width`, `height`, `color`, `icon`,
`kind`, `itemCount`.

Initial island kinds: `TRIAGE`, `STARTER`, `PLAYER`.

- `TRIAGE` is special: unhomed and ambiguous items land there.
- `STARTER` islands are conservative defaults (e.g. `Blocks`).
- `PLAYER` islands are manually created or renamed by the player.

### `AtlasItemHome`

Fields: `homeId`, `regionId`, `itemId`, `name`, `displayStack`, `totalCount`,
`firstSourceId`, `firstSlotIndex`, `x`, `y`, `width`, `height`, `presence`,
`badges`.

For MVP, `presence` can be only `PRESENT_CARRIED`. `homeId` derives from item
identity, not slot index, so the item keeps a stable projected coordinate
across refreshes while present.

### `VisualHomeAssignment`

Fields: `homeId`, `itemIdentity`, `islandId`, `x`, `y`, `origin`, `locked`.

Initial `origin` values: `TRIAGE`, `HIGH_CONFIDENCE_AUTO`, `PLAYER_PLACED`.

`PLAYER_PLACED` always wins over automatic placement. SLOT must not silently
move a player-placed home.

### `AtlasHotbarSlot`

Mirror the existing `SlotWorkspaceViewModel.HotbarSlot`.

## Triage-First Layout Algorithm

Deterministic and headless-testable, but deterministic placement is only the
fallback before a player-authored home exists.

Inputs: current authority snapshot, item identity, display name, category
resolver, optional mod id, in-memory prototype home assignments.

Output: stable region id, stable island id, stable coordinate inside the
island.

### Home / Island Selection

Conservative classification. The goal is trust, not coverage:

- if the item already has a player-authored home, use it
- else if the item has a very high-confidence automatic home, use it
- else place it in `Triage`

High-confidence automatic homes must stay minimal at first:

- obvious placeable building blocks → `Blocks`
- everything else → `Triage` until proven safe

Avoid broad heuristic claims for tools, magic, machines, storage, or mod
namespace. Heuristic labels may appear as inspector hints, but must not
silently create a permanent home.

### Island Placement

Hard-code a first atlas geography:

- `Triage`: central or upper-left, near the default camera
- `Blocks`: one high-confidence starter island
- Hotbar/action rail: fixed screen chrome, not an atlas island yet
- Player-created islands: empty map space near triage until persisted layout
  exists

This is a design experiment, not final behavior.

### Home Placement

Within an island:

- player-authored coordinates win
- otherwise sort by display name, then item id
- island-local grid with stable cell size and enough spacing for labels/counts

Unhomed items re-sort when new items insert before them; that is acceptable.
Homed items must not move unless the player moves them. Isolate placement
behind an `AtlasLayoutService` so persistent homes can replace in-memory
prototype homes later.

### Why Not Hash Coordinates?

Hashing item id into coordinates gives stable placement without persistence
but creates collisions and meaningless geography. Use deterministic
category/name layout only for unhomed triage fallback. The target design is
player-authored coordinates.

## LDLib2 UI Strategy

Treat `SlotWorkspaceUiFactory` as the LDLib atlas surface owner, not a
list-screen owner. The old list-first prototype has served its purpose and
should not remain as a supported mode.

Preserve the validated mutation semantics:

- atlas item cards select authoritative carried source targets
- hotbar slots receive exact `ASSIGN` requests with displace policy
- hotbar-to-main uses transfer into player main inventory
- rendering and selection stay client-side; request construction remains
  server-authoritative

### Source Finding

The relevant LDLib2 primitive is the generic pan/zoom element:

- `com.lowdragmc.lowdraglib2.gui.ui.elements.GraphView`

Do not confuse it with the node-toolkit wrapper:

- `com.lowdragmc.lowdraglib2.nodegraphtookit.gui.GraphView`

Relevant capabilities from local source:

- owns a `contentRoot` and applies a `Transform2D` translate/scale camera
  transform
- mouse-wheel zoom, drag panning, configurable `allowZoom`, `allowPan`,
  `minScale`, `maxScale`, grid background, `fit` / `fitToChildren`
- `GraphView.addContentChild` adds arbitrary `UIElement` children to the
  transformed content root, so atlas regions and cards can be ordinary LDLib
  elements with absolute positions
- `UIElement` transform support affects rendering and hit testing; card clicks
  resolve in transformed coordinates
- `UIElement.setOverflowVisible(false)` and the rendering path provide scissor
  clipping for the visible viewport
- `ScrollerView` does not solve semantic zoom or camera movement and should
  not be the atlas primitive

### SLOT Wrapper

Use the generic `GraphView` through a small SLOT-owned wrapper
(`SlotAtlasGraphView` or `SlotAtlasSurface`), not a from-scratch pan/zoom.

Wrapper owns SLOT-specific policy:

- atlas region/card layer creation
- default camera fit
- search spotlight camera jumps
- gesture rules
- click handling for item-card selection
- selection clearing on inactive background clicks

Default LDLib2 behavior to account for:

- left-drag panning starts on empty graph background; middle-drag panning
  works more broadly
- mouse-wheel zoom checks `event.target == this`, so zoom may not fire while
  hovering hit-testable item cards
- `offsetX` / `offsetY` have public setters, but those setters do not refresh
  the content transform; use `fit` / `fitToChildren` or add a wrapper-level
  camera method rather than directly mutating offsets

If zoom-over-card or programmatic camera movement blocks the UX, the first
custom code should be a thin subclass/wrapper that normalizes events or
exposes a safe `setCamera(offsetX, offsetY, scale)` method. Do not fork the
full pan/zoom implementation unless LDLib2's private camera fields make the
wrapper insufficient.

Keep atlas model coordinates independent of LDLib rendering details so the
surface can be replaced later if necessary.

## Panel Layout, Navigation, And Search UX

### Visual Treatment

Semi-transparent full-screen workspace, not an opaque modal:

- world faintly visible behind a dark translucent scrim
- atlas canvas uses a stronger translucent tint than the outer screen so
  icons remain readable
- floating controls use "glass card" backgrounds with enough opacity for
  text contrast
- item cards may be more opaque than the canvas, especially at detail zoom
- background grid/region texture subtle and reinforcing the map metaphor

Secondary lore/affordance goal: the atlas may use stars, nebulas,
constellations, dimensional lenses, ancient technology, or similar motifs to
make the workspace feel like a Minecraft-plausible artifact. Treat this as
swappable emotional texture; usability wins over theme.

Avoid: fully transparent text panels over noisy world backgrounds; large
opaque rectangles that make the atlas feel like a normal application; hiding
the hotbar so deeply that transfers lose affordance; animated particles that
flicker or compete with item icons.

### Screen Regions

- atlas canvas occupies most of the screen
- docked Triage panel on a fixed edge (left edge in current prototype),
  camera-anchored and not part of the pannable atlas
- top-left floating search/navigation capsule
- top-right compact camera controls and optional minimap toggle
- right-side or bottom fixed hotbar/action rail
- bottom status/diagnostics strip, compact and low-contrast unless rejected
- selected-item inspector appears only when needed (right drawer or popover)
- projection panels attach to edges or the search capsule rather than
  replacing the atlas canvas

Hotbar rail placement is still open. For the current prototype, keep the
right rail until search/navigation proves out. Revisit bottom rail when we
add keyboard shortcut affordances and selected-item actions.

Projection panel placement guidance:

- search results tray → attached to the search capsule
- collection/task board → explicit right or left sideboard
- cleanup tray → bottom or lower-side edge surface
- trash/void confirmation → edge drawer or modal tray separate from the atlas
- optional recent-activity ribbon → compact top or side surface only if home
  pulses plus the docked Triage panel are insufficient

### Navigation Model

Core controls:

- drag empty atlas space to pan
- mouse wheel zooms around cursor
- Home button resets to carried-inventory overview
- Back/Forward buttons move through camera history
- click region label jumps/fits to that region
- optional minimap shows current viewport and region landmarks

Camera history records intentional jumps (search selection, region jump,
Home reset, task/loadout jump) — not every drag/wheel frame.

### Search As Navigation

Search must not destroy the map by replacing it with a sorted list. It
guides the player through the stable atlas:

- typing filters semantically but leaves all homes in place
- matches get bright outlines/pulses
- non-matches dim but remain visible enough to preserve context
- region headers show match counts
- best result spotlighted
- Enter cycles through results
- selecting a result pans/zooms to its home
- Escape clears the query and returns to the previous camera target if
  search moved the camera

Compact result tray only as precision aid: appears below the search capsule,
lists top matches by display name, mod id, count; clicking navigates to the
stable home rather than creating a new layout; tray rows show tiny region
breadcrumbs.

Search eventually indexes: display name, item id / mod namespace,
category/region, tags, tooltip-derived variant data for ambiguous-icon
families, collection/loadout membership, remembered external source labels.

## Progressive Disclosure Rules

Zoom changes how much information is visible, not where items live and not
canonical atlas geometry.

Invariant:

- each item identity owns a stable anchor/home
- zoom changes disclosure around that anchor
- atlas does not globally reflow or reserve large empty footprints

Avoid "all cards scale up in place." Instead use anchor-first semantic zoom:

- overview dense and slot-like
- summary adds lightweight metadata around the same home
- detail belongs to focus surfaces (callouts or inspector)

## Atlas LOD Rendering Rules

LDLib2 `GraphView` uniformly scales the content tree, so if SLOT does nothing
special, zoom only makes "the same thing, bigger." To turn zoom into useful
information density, atlas item internals must derive icon size, text size,
padding, and line count from the current on-screen pixel budget.

Atlas items behave like screen-budget-aware cells:

- stable world-space home
- growing screen-space budget as the camera zooms in
- sublinear or clamped icon/text sizing
- more rows and markers revealed as more screen pixels become available

Rules:

- atlas layout stores anchor position and a compact base footprint
- overview renders like a dense inventory slot plus count
- summary and detail driven by the screen-pixel budget currently available to
  the anchor, not only a raw zoom threshold
- detail reveals inside the same stable home before promoting to the inspector
- when there is not enough room for a label, suppress it rather than shifting
  neighbors
- if local expansion is ever allowed, it must be temporary and not rewrite
  canonical coordinates

## Item LOD Design Pass

The prototype needs explicit answers to:

- what is the player supposed to read at each zoom level?
- which details belong on the atlas versus in the inspector?
- how do we avoid making hover-only tooltip fishing the only way to
  distinguish ambiguous items?

Information hierarchy:

1. Recognition — icon silhouette, neighborhood, rough count,
   recent/protected/search-match state
2. Basic identification — short name or abbreviation, source or home marker,
   collection/task membership hints
3. Variant disambiguation — first useful differentiator for ambiguous-icon
   families (enchant short token, potion effect, spell school, upgrade tier,
   backpack color)
4. Full inspection — full tooltip-derived lines, item id / mod id, exact
   source summary, actions, diagnostics, future workflow details

The atlas owns 1–3. The inspector owns 4.

### Interaction-State Rules

LOD primarily depends on screen budget; interaction state is a modifier.

- `unfocused`: default browsing state
- `hovered`: temporary preview
- `selected`: sticky inspect state until cleared/replaced
- `search-matched`: may promote a small amount of label/badge information

Hover may preview detail but must not be the only way to disambiguate common
ambiguous items. Selection pins the inspector. Search may promote short
labels or variant markers. Focused state adds affordances, not basic
readability — zoom alone must already give the player a readable layout.

### Budget Bands

Derive a rough screen-space budget from camera and anchor world size:

- `cellScreenWidth = cellWorldWidth * atlasScale`
- `cellScreenHeight = cellWorldHeight * atlasScale`
- the more-constrained dimension is the budget input

Bands:

- `Region`, ~0.30–0.55: orientation/navigation; icon silhouette, optional
  tiny pip; counts optional and very small; cluster/shelf summaries
  acceptable for dense areas
- `Browse`, ~0.55–1.05: everyday "where is my thing?" scanning; icon +
  compact count + one state marker; names generally hidden; hover/search
  may temporarily reveal a nearby label; default open camera lands near the
  top of this band or just into the next
- `Read`, ~1.05–1.90: identify specific items without tooltip-per-item;
  homes still fixed-size in world space but visually large enough for
  readable text; icon + count on stack glyph + one readable short
  item-name-first line + one source/state marker; ambiguous-icon families
  become distinguishable here
- `Inspect`, ~1.90–3.00: precise browsing/organization inside a region;
  stack glyph + short name + one secondary line (variant or source) +
  state/source markers; inspector remains primary full-detail surface
- `Close Inspect`, ~3.00–4.50: deliberate organization and precise
  placement; not normal browsing; richer in-cell structure where budget
  allows

Initial zoom limits: min ~0.30, default open ~0.80–1.10, max ~4.00–4.50.
Exact numbers are less important than "useful reading band is easy to reach
without microscope mode."

### Screen-Budget-Aware Rendering Strategy

Per atlas item:

1. Keep a stable world-space home and hitbox.
2. Measure approximate screen-space budget from current graph scale.
3. Choose a template from that budget.
4. Derive icon size, text size, padding, and line count from desired
   on-screen pixels, then convert back to world units:
   `worldUnitsForPixels(px) = px / atlasScale`.

- icon size grows sublinearly or clamps around useful screen-pixel sizes
- text size stays within a readable screen-pixel range
- extra budget produces more rows and columns, not just larger glyphs

Candidate budget table:

- under ~24 px: stack glyph only
- ~24–40 px: stack glyph + one small state marker
- ~40–72 px: stack glyph + one readable item-name line
- ~72–110 px: stack glyph + item-name + secondary line
- above ~110 px: richer two-line layout + compact badges if still valuable

### Atlas Item Signal Grammar

Use a small, consistent visual grammar instead of text-only fields:

- icon silhouette: primary recognition anchor
- stack count on the icon itself (never duplicated in detail text)
- border treatment: broad state (selected, search-matched, emphasis)
- corner pip or badge: compact boolean or tiny-count states
- footer or side strip: short text token or label
- secondary line: only when budget supports truly readable text

Recommended compact signals:

- top-right pip: recent / changed
- top-left or left border accent: collection/task membership
- bottom underline or tint band: search match
- border intensity or chrome treatment: selected, hovered, pinned

Do not overload one marker with many meanings.

### Field Priority

- Highest: icon, count on stack, item name, first useful differentiator for
  ambiguous families
- Medium: recent delta (`+4`), collection membership marker, desired count
  under an active collection/task context, source or home-status marker
  (inbox / auto / player-set)
- Lower: non-`minecraft` mod tag, exact source summary, multiple collection
  labels, full ids / namespace strings

Higher-LOD budget goes first to item-name and differentiator display, not
low-value metadata.

### Conditional Field Rules

- plain `minecraft` dirt with no membership / recent change: mostly
  name-first at higher LOD
- enchanted book: enchant token before mod tag
- item in active collection: desired count or shortage before mod tag
- recently changed: compact `+delta` badge or footer token before generic
  source metadata

Recent delta behaves like short-lived session context: `+4` means gained
four since last open, persists across close/reopen until a newer recent
batch supersedes it. It is a workflow aid, not permanent metadata.

### Geometry Guidance

Square homes remain the default: strongest continuity with vanilla slot
inventory, best dense packing at low LOD, simplest spatial-memory story.
Keep homes square for now; improve internal layout (top-left anchored icon,
better line usage) before changing geometry.

Reserve a slightly wider landscape cell as a later experiment if square
homes still prove too cramped. First experiment should be conservative —
around `1:1.15` or `1:1.25`, not `1:1.5`. Larger rectangular jumps only if
square homes continue to block useful `Read` and `Inspect` layouts even
after better field prioritization and icon anchoring.

### Ambiguous-Icon Families

Strongest pressure test for the design. Examples: enchanted books, potions,
spell books / scrolls, upgrade cards, machine items with mode/tier encoded
outside the icon.

Design rule: by `Read` zoom, these families should show one useful
differentiator without raw tooltip hover. Candidates: first/strongest enchant
short token, potion effect short token, spell school or tier token, machine
tier token, color chip or family symbol.

If SLOT cannot derive a safe differentiator, fallback is a clear family
marker on the atlas + full tooltip preview in the inspector. Still better
than "hover every identical icon in sequence."

### Inspector Role

The inspector is the stable precision-reading surface, not trivia. For a
focused atlas item it should show: full name, full tooltip preview, item id
and mod namespace, source summary, home/island/collection context,
available actions.

The player scans visually, hovers/selects to verify, and reads rich detail
in one predictable place.

### Prototype Guidance

- keep compact fixed anchors
- do not use world-space callout cards as the primary detail mechanism
- make `Browse` and `Read` the most useful bands
- ensure `Read` zoom already reveals meaningful item-level differentiation
- make the inspector the authoritative rich-detail surface for focused items
- reserve `Close Inspect` for deliberate organization, not ordinary browsing

## Triage And Island Management Workflow

Core flow:

- new or ambiguous item identities appear in `Triage`
- high-confidence starter placement only for obvious placeable building
  blocks
- selecting a triage item should make valid destinations visually obvious
- assigning to an existing island creates a stable home for that identity
- assigning to empty atlas space creates a new player island seeded by that
  item
- once placed, future copies appear at that home
- SLOT must not silently relocate player-placed homes

Candidate interactions, in order of prototype complexity:

1. Select triage card, then click an island header to assign the home there.
2. Select triage card, then click empty atlas space to create a new island.
3. Drag triage card onto an island to assign.
4. Drag triage card into empty atlas space to create a new island.
5. Multi-select triage cards, then assign/create a home group in bulk.

First implementation can use click-target assignment if LDLib drag/drop adds
too much uncertainty. UX target remains drag-to-create and drag-to-place.

Island management should feel physical:

- island title acts as a handle
- moving an island moves its child homes visually, not physical inventory
- context menu or inspector can rename, recolor, set icon, split, merge, or
  delete an empty island
- dropping near an island edge previews expansion
- suggested island labels can be generated from the seed item; the player
  remains authoritative

## Immediate Prototype Next Step

Next UI slice:

1. Refactor atlas items around stable compact anchors rather than zoom-scaled
   cards.
2. Make overview render as a dense slot-like icon plus count.
3. Make summary render as the same anchor with lightweight badges or
   optional short labels.
4. Move full ids/source details and richer action affordances into a focused
   inspector or tethered callout.
5. Reduce or replace the aliased grid texture and tighten atlas chrome.
6. Preserve homes, camera, search query, and selection through view
   refreshes.

### Mapping To The Current Prototype

- `SlotWorkspaceViewModel.AtlasItem.x/y` is the stable home anchor
- current `width/height` should converge toward a compact canonical footprint
- disclosure level swaps rendering treatments, not atlas layout
- overview should resemble vanilla slot density more than a broad card row
- detail primarily in the inspector rail, with optional local callout for the
  focused item

Shrink the importance of wide per-item card layout; grow the importance of
slot-like anchors, badges, labels on demand, and focused detail surfaces.

## Integration With Current Workspace

Current files:

- `SlotWorkspaceViewModel`: current workspace DTO, still carrying
  list-derived fields during atlas migration
- `SlotWorkspaceUiSession`: server-owned view state
- `SlotWorkspaceUiFactory`: LDLib UI construction
- `SlotWorkspaceTransferRequestFactory`: server-side request construction

Approach:

1. Keep the LDLib menu/session/RPC boundary as the player-inventory workspace
   transport.
2. Evolve the view model toward atlas/island/home fields; remove list-only
   concepts when they stop being useful scaffolding.
3. Keep hotbar rail and transfer RPC unchanged.
4. Item card click selects `SourceSlotTarget(PLAYER_MAIN, firstSlotIndex)`.
5. Hotbar click sends the same RPC command as today.
6. Add visual home assignment commands separately from real mutation commands
   so moving a home cannot be confused with moving an item stack.

Risk is concentrated in presentation and visual-memory state. The atlas does
not change core action execution.

## External Storage Direction

External storage does not start as "every remembered chest is always mounted
on the atlas" — that implies terminal-like remote access and misleads players
about what SLOT can safely mutate.

Long-term direction is physical storage memory:

- SLOT remembers where item identities were last seen in known physical
  sources
- remembered sources appear contextually when the player searches, restocks,
  or opens/targets that source
- current opened sources show as active islands or compare panes
- unopened remembered sources are guidance, not authority
- particle or UI trails point toward likely storage without implying remote
  mutation

Sequence after the carried atlas proves useful:

1. Active-source compare: currently opened external inventory beside or near
   the carried atlas, with carried/external badges and only authority-backed
   actions.
2. Source memory records: persist last-seen identities, counts, source
   label, source position when available, and staleness metadata for stable
   physical sources only.
3. Find guidance: search shows "last seen in X" markers or UI-local trails
   while keeping actions disabled until a source is active.
4. Restock guidance: task/loadout restock produces a route/checklist through
   remembered sources before any remote-access semantics.

Blocking rule: no external-memory feature may mutate an inventory unless
SLOT has current authoritative access through an open menu, player
inventory, or another explicit supported authority path.

## Required Tests

### Projection Tests

Atlas projection:

- groups main inventory entries by identity
- routes unhomed ambiguous identities to `Triage`
- auto-homes only high-confidence obvious building blocks to `Blocks`
- assigns each home to a deterministic island
- computes deterministic coordinates inside that island
- preserves identity-derived home ids across refresh
- respects player-authored visual home assignments over automatic placement
- includes first backing source/slot target
- includes counts and display stacks
- keeps hotbar slots intact

### Serialization Tests

If synced through NBT tags like the current view model:

- atlas DTO round-trips through tag serialization
- empty atlas round-trips safely
- unknown or missing fields default safely

### Architecture Tests

- atlas projection in `common` must not import LDLib2
- LDLib2 rendering remains in NeoForge UI packages

### UI Behavior Tests

In NeoForge tests:

- atlas item click builds the same authoritative source-target selection as
  the legacy list row click did
- hotbar assignment request remains
  `ASSIGN + STACK + SINGLE_TARGET + ASSIGN_WITH_DISPLACE`
- hotbar-to-main remains `TRANSFER + STACK + SINGLE_TARGET + INSERT_ONLY`

## Major Blocking Concepts

### 1. Can The Atlas Replace The Prototype List Without Losing Speed?

Decision: the atlas is the target player inventory view; the prototype list
does not remain as a supported mode.

Risk: atlas navigation may be too heavy for quick inventory opens.

Mitigation: keep the hotbar rail fixed; make the initial viewport useful
immediately; rely on search/spotlight and camera reset instead of a
permanent list mode.

### 2. LDLib2 GraphView Gesture Fit

Decision: LDLib2 supports the atlas through the generic `GraphView`; do not
use `ScrollerView`.

Risk: default gesture handling may not zoom or pan exactly as wanted when
the pointer is over hit-testable cards.

Recommendation: use `GraphView` directly for the first atlas surface;
wrap/subclass only for gesture normalization and safe camera helper methods;
custom-from-scratch widget only as fallback if a thin wrapper is
insufficient.

### 3. Visual Home Identity Granularity

Question: is a home keyed by item id, item identity, NBT-sensitive identity,
item family, or category?

Recommendation for MVP: use current `ItemIdentity` from
`ItemIdentityMatcher`. Add item-family homes later for ambiguous icon
families.

Reason: current authority/projection already uses this identity.

### 4. Category Taxonomy

Question: are current categories good enough to become atlas neighborhoods?

Recommendation: do not treat broad heuristic categories as authoritative
homes; use only very high-confidence auto placement at first; route
everything else to `Triage`; allow heuristic labels as suggestions or
inspector hints.

Reason: player trust > classifier coverage. A bad silent home is worse than
visible triage.

### 5. Persistent Home Storage

Question: where do homes live when persistence is added?

Candidate: workflow domain state, because homes are durable user
organization preferences, not inventory authority.

Recommendation: do not persist in MVP; define a future `VisualHomeMap`
domain state after the UI proves value.

### 6. Move Visual Home Versus Move Real Item

Question: how does the player distinguish reorganizing the map from moving
actual inventory?

Recommendation: MVP supports assigning an unhomed item to an island/home.
Arbitrary drag-to-reposition waits until the assignment flow is safe. Real
inventory movement must keep using hotbar/action targets and clear action
affordances. Later, require explicit organize mode or distinct drag handles
for precise visual home movement.

Reason: major UX safety issue; do not rush.

### 7. Search Behavior

Question: should search dim in place, open result tray, pan camera, or all
three?

Recommendation: keep in-place highlighting and camera controls. Search
navigates or spotlights stable homes rather than rebuilding the atlas. Add
a compact result tray only when in-place spotlight is not precise enough.

### 8. External Source Identity

Question: what counts as a stable physical chest/source identity?

Recommendation: defer external memory; start with active opened source
only; do not persist last-seen external memory until source identity
confidence is solved.

### 9. World Guidance Trails

Question: can SLOT draw meaningful trails to physical chests without
world-position tracking, dimensions, and server/client sync?

Recommendation: defer world/particle trails; first implement UI-local
source markers and "last seen in X" labels.

### 10. Performance And Scale

Question: how many homes/cards can the UI render before LDLib performance
suffers?

Recommendation: MVP renders current player main inventory only; measure
before expanding to thousands of homes; semantic zoom / virtualization is
needed before long-tail external memory.

## What Is Not Blocking The MVP

Defer:

- final visual styling
- persistence
- external storage memory
- task boards
- loadout board UI
- optional recent recap ribbon
- trash/void
- item-family subtitle extraction
- search result tray
- true animated trails
- minimap
- world-space particles

## Implementation Slices

### Slice 1: Atlas Readability Cleanup

Add:

- lower-noise or non-aliased background treatment
- smaller item-card padding
- smaller detail typography
- compact summary text at medium zoom
- inspector/popover for full item id and source-slot details

Exit: normal browsing and detail zoom are readable without ellipsizing
common names or overflowing screen edges.

### Slice 2: Triage-First Projection

Replace broad category placement with conservative home selection.

Add:

- `Triage` island as the default for unhomed identities
- `Blocks` starter island for obvious high-confidence placeable blocks
- item-home projection that distinguishes `TRIAGE`,
  `HIGH_CONFIDENCE_AUTO`, and `PLAYER_PLACED`
- tests proving ambiguous/modded/multi-use items remain in `Triage`

Exit: new item identities land somewhere obvious and trusted instead of
being silently classified into questionable regions.

### Slice 3: In-Memory Home Assignment

Add:

- selected triage card state
- click island header to assign selected item to that island
- click empty atlas space to create a new player island seeded by the
  selected item
- in-memory `VisualHomeAssignment` state
- view refresh preservation for camera, query, selection, and homes

Exit: the player can place an item once and see future copies use that
stable home for the rest of the session.

### Slice 4: Island Management Basics

Add:

- rename island
- recolor island
- set icon from seed item
- move island as a visual object without moving real inventory
- delete only empty player islands

Exit: player-created islands feel intentional enough to evaluate the
visual-memory loop.

### Slice 5: Search Spotlight And Result Tray

Add:

- search spotlight against stable homes
- non-match dimming without layout replacement
- Enter/click navigation to matching homes
- compact result tray only if needed for ambiguous searches

Exit: search helps find and teach item locations without destroying map
memory.

Only after Slice 5 should persisted homes be considered.

## Implementation Recommendation

The next coding milestone should not attempt the whole persistent-map
vision.

Near-term milestone:

- clean up the current atlas surface
- add `Triage`, conservative `Blocks` auto-home, and in-memory visual homes
- implement select-item-to-island and select-item-to-empty-space assignment

That is enough to answer the first important question: can a player quickly
give new item types memorable places and then trust those places during
real inventory use?

If yes, proceed to search spotlight, island management, and persisted homes.
If no, the same projection and region concepts can still inform category
pockets or hybrid grid/card views without retaining the debug list as a
shipped surface.
