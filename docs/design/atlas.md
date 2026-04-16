# Atlas Design

Last updated: 2026-04-16

Status: current design direction for SLOT's primary player-inventory surface.

SLOT's primary inventory UI is a pan/zoom **atlas**: item identities get
stable visual "homes" on a large canvas. New identities arrive in a
first-class `Triage` island; the player places them in existing islands or
creates new ones. Placed homes become authoritative visual placement and are
not silently moved.

This is the living concept document. For the engineering slices, see
[../plans/atlas-prototype.md](../plans/atlas-prototype.md). For how atlas
homes interact with task-oriented groupings (Kits), see [kits.md](kits.md).

## Why An Atlas

Three useful ideas conflict in any Minecraft inventory UI:

- **Sorting/filtering** reduces information burden but destroys spatial memory.
- **Stable placement** leverages visual perception but scales poorly in one
  dense rectangle.
- **Each container shape** forces the player to relearn a map unless SLOT
  provides a consistent global organization.

Grids preserve spatial memory but scale poorly. Sorted lists scale better but
erase placement memory. Terminals search well but feel like databases.

SLOT's bet: separate **visual home** from **physical source**. Give each item
identity a stable visual coordinate in a pan/zoom atlas. Visual placement is
presentation state; real inventory authority stays separate. Search becomes
navigation, categories become places, and the atlas can grow far beyond a
single screen without losing the memory advantages of a grid.

## Item Homes

An **item home** is a stable visual coordinate for an item identity in a
named visual map.

The home says:

- "When this identity is relevant to this view, show it here."

It does **not** say:

- "This item physically lives in this slot."

A home can render one of several **states**:

- **Present here** — item is in the active authority scope
- **Present elsewhere** — item is in an external source that's currently
  visible
- **Missing but wanted** — loadout/Kit/collection expects it, not currently
  accessible
- **New or changed** — recently acquired or count changed
- **Conflict or overflow** — multiple identities compete for the home, or
  the preferred home has no local space
- **Unclassified** — no explicit home yet (lives in `Triage`)

The key invariant: "known elsewhere" must not look like "currently in my
inventory." Ghost/outline/badge treatments make presence explicit.

## Atlas Structure

### Vocabulary

- **Atlas** — the large pan/zoom canvas containing everything below
- **Home** — a stable visual coordinate for an item identity
- **Neighborhood** — a semantic region (Blocks, Tools, Food, Ores, Magic,
  Machines, Junk, Recent, …)
- **Island** — a distinct source or curated grouping (Player Carried,
  Backpack A, Ore Chest, a player-authored island, etc.)
- **Board** — a workflow surface (Boss Kit, Building Kit, Collection:
  Components) — Kits are boards
- **Lens** — a temporary emphasis/dim/connect overlay (search, recent,
  cleanup, task, compare, craft)
- **Tray** — a temporary side surface for items needing attention (search
  results, cleanup preview, triage inbox when not on-atlas)

### Landmarks

A large atlas needs anchors so it does not become empty space dotted with
icons:

- category neighborhoods
- Kit boards
- hotbar/equipment rack
- intake shore / `Triage` island for arrivals
- active external source islands
- muted last-seen external islands
- player-defined labels, paths, and dividers

### Spaces And Islands

Carried inventory is the home space. Each backpack, chest, or other source
is a separate island. Opened sources become active and bright; unopened
known sources appear muted last-seen.

Hard rule: **a visual connection is not an action route.** Moving items
between islands still requires active authority and must fail closed when
unavailable.

### Scale And Density

Density is not uniform across the atlas:

- hotbar/equipment rail is fixed and always large
- common favorites get larger homes
- long-tail components are dense within category shelves
- ambiguous-icon families get larger cards
- junk/low-value items can be compact until triage
- task/Kit boards use larger ghost slots for readability

## Semantic Zoom

The same atlas reveals different information at different zoom levels, but
the canonical home geometry must not change. This is **anchor-first
semantic zoom**: anchors stay fixed; disclosure changes.

Invariants:

- each item identity owns a stable anchor in world space
- zoom changes what's drawn inside/around the anchor
- atlas layout never globally reflows just so items can expand later

Avoid the anti-pattern of "every card physically expands as zoom
increases." That forces a bad tradeoff: either big gaps at overview, or
neighboring cards overlap at detail zoom.

Prefer **screen-budget-aware** rendering: compute the anchor's current
on-screen pixel budget from camera scale, and pick a layout template to
match. Icon and text sizing grow sublinearly (or clamp) so extra pixels
become layout room rather than "the same thing, bigger."

Zoom bands (roughly — exact thresholds are tuning):

- **Region** (~0.30–0.55) — orientation. Neighborhoods, landmarks, dense
  anchors, icon silhouettes, tiny state pips, no text.
- **Browse** (~0.55–1.05) — everyday "where is my thing?" scanning. Icon
  plus compact count plus one state marker. Names hidden.
- **Read** (~1.05–1.90) — identify specific items without tooltip fishing.
  Icon anchored top-left, one readable item-name line. Ambiguous-icon
  families become distinguishable here.
- **Inspect** (~1.90–3.00) — precise browsing and organization within a
  region. Icon, short name, one secondary line for variant/source, state
  markers.
- **Close Inspect** (~3.00–4.50) — deliberate organization/placement.

Default open camera should land near the top of `Browse` or into `Read`
so the player doesn't need to zoom to read item-level detail.

Hover and selection may add extras but must not be the only path to
basic readability — zoom alone should reveal enough. The inspector is
the stable precision-reading surface for the currently focused item.

## Atlas Item Signal Grammar

Not every piece of information is text. At lower budgets, use a small
consistent visual grammar:

- **icon silhouette** — primary recognition
- **stack count on icon** — familiar Minecraft convention, never duplicated
  in detail text
- **border treatment** — selected, search-matched, special emphasis
- **corner pip** — recent, changed, collection membership, protection
- **bottom underline / tint band** — search match
- **footer/side strip** — short text token

Field priority, highest first:

1. icon
2. count on stack
3. item name
4. differentiator for ambiguous families (enchant, effect, tier, color)
5. recent delta, collection membership, desired count (context-dependent)
6. source summary, exact source slot, full ids, namespace

Unused space at higher zoom goes first toward name and differentiator,
not low-value metadata.

### Ambiguous-Icon Families

Enchanted books, potions, spell books, upgrade cards, machines with tier
encoded outside the icon: by `Read` zoom, these should show one useful
differentiator without requiring hover. Fallback if no safe differentiator
exists: clear family marker on atlas, full tooltip preview in inspector.

## Canonical Home Versus Derived Projection

The atlas needs a hard representational boundary:

- an item identity gets **one canonical home** in a given atlas
- alternate workflows (search, Recent, cleanup, collections, Kits) may
  show the identity again, but only as a **derived projection**
- a projection must visually read as a reference back to the canonical
  home, not as a second home

Projection types:

- **Anchor** — the canonical home on the atlas
- **Mirror** — a temporary copy in a tray/inbox/board, linked to its
  anchor
- **Ghost** — missing or desired placeholder (loadouts, Kits, last-seen
  external memory)
- **Action surface** — hotbar rail, trash tray, void tray, crafting
  result — a target/command, not a home

Rules:

- only anchors define where an item "lives"
- mirrors can summarize, group, or preview, but they don't become homes
  unless the player explicitly assigns one
- ghosts are planning state, not stack state
- action surfaces are destinations/commands, not inventory homes
- **if the same item appears in two places at once, one must visually read
  as mirror/ghost/action-target, not as a second home**

### Projection UX Rules

The user must be able to tell immediately that a panel item is a
projection, not a second location:

- projections use distinct chrome (list rows, compact cards, drawer items,
  stacked inbox cards, edge trays) rather than full atlas-style placement
  cards
- every projection item shows a home hint (island label, breadcrumb,
  "Find on map")
- hovering a projection highlights its home on the atlas and draws a
  subtle tether
- selecting keeps the tether visible until cleared or focus changes
- "focus home" action pans/zooms the camera to the home
- unhomed items resolve explicitly to `Triage`, never pretend to have a
  location

The inverse rule: unhomed items need a **real place on the atlas**
(`Triage` island / intake shore), not a floating projection panel.

### Projection Surfaces And Triggers

Prefer a small set of clear surfaces:

- **Search results tray** — attached to the search capsule, opens when
  in-place spotlight isn't precise enough
- **Recent activity ribbon** (optional) — for assigned items that only
  need review/dismissal; the real "where new things are" is the atlas
  intake region
- **Collection/Kit sideboard** — opened explicitly; mirrors for present
  items, ghosts for missing
- **Cleanup tray** — opened via cleanup mode; groups junk candidates and
  proposed actions; tints anchors in place
- **Trash/void surfaces** — explicit edge trays or confirmation drawers;
  never look like storage homes

The atlas should usually show at most one large projection panel at a
time. Tabs that fully rebuild the same inventory into different
arrangements are risky — they teach competing geographies. Prefer lenses,
trays, and sideboards.

## Lenses

Lenses change emphasis, not the map.

- **Search lens** — keep items in homes; dim non-matches; count matches by
  region; optionally lift matches into a tray leaving ghost anchors
- **Category lens** — category pockets are permanent regions; the lens
  zooms to a pocket rather than rebuilding
- **Recent lens** — recently acquired items with homes pulse at their
  homes; new identities without homes arrive in the atlas-native intake
  region; dismissing removes the pulse, not the home
- **Task/Kit lens** — overlays desired-count and loadout targets onto
  existing homes; missing items show as ghosts on the Kit board
- **Cleanup lens** — tints homes by cleanup recommendation; a cleanup
  tray groups proposed actions; executing animates from homes to
  storage/trash/void
- **External compare lens** — carried atlas beside external source map,
  matching identities connected with faint lines or count-compare badges

## Triage And Placement

### First Contact

New unhomed items appear in the `Triage` island. This is not a failure
state — it's the normal place where SLOT asks the player what an item
means in their organization.

From Triage, the player can:

- assign to an existing island
- create a new island from the selected item
- accept a high-confidence suggested home
- mark junk / favorite / protect
- add to Kit or collection
- ignore for now

### Auto-Homing

Auto-homing is deliberately conservative. Player trust matters more than
classifier coverage.

Initial scope:

- obvious placeable building blocks can auto-home to a `Blocks` starter
  island
- everything else starts in `Triage`

Rules:

- **manual home beats automatic home** (`PLAYER_PLACED` wins)
- pinned/favorite/loadout items get stronger homes
- ambiguous or risky items stay in triage
- broad mod namespace or tag guesses are suggestions, not silent homes

A bad silent home is worse than visible triage.

### Learning From Repetition

If the player repeatedly moves an item to a visual area, SLOT can offer
("Always place Copper Ore here?") but should not auto-apply — surprises
break trust.

## Handling Scale

A stable atlas can become too large. Layers, not endless zoom alone:

- **Locality by category** — global map shows categories as pockets;
  items have stable pocket-local homes; zoom/search within a pocket
- **Importance layers** — top-level visible: hotbar/loadout, favorites,
  recent, selected task items, pinned, currently-present carried.
  Collapsed: old seen, rare components, external-only, category long tail
- **Semantic zoom** — disclosure changes with zoom; anchors don't
- **Overflow shelves** — when a pocket is full, unpinned items go to a
  stable overflow shelf in the same category

## Authority And Visual Memory

The UI must never imply a visual home is a real slot unless it is.

### Visual Coordinate ≠ Action Target

Home coordinate is presentation/workflow state. Action targets are
`InventoryActionTarget` values (exact source, slot, entry, quick-access
target, equipment target, tool target).

When a player acts on a home, SLOT resolves current authority. If
resolution fails, the action fails closed with a diagnostic.

### Count And Presence Must Be Honest

A home can display carried count, external count, known-elsewhere count,
desired count, missing state, or stale/unknown state. These must be
visually distinct.

### Ghosts Are Powerful But Dangerous

Ghosts (missing desired items) are useful but confusing if they look too
real:

- ghosts must be visibly translucent or framed differently
- actions on ghosts are planning actions, not direct stack actions
- ghosts should explain why they exist: loadout, collection, Kit, last
  seen, desired count

### Move Visual Home ≠ Move Real Item

Dragging in the atlas can mean several things. Mitigations:

- dragging from map background/home moves visual placement
- dragging from a source breakdown or with a transfer modifier moves real
  inventory
- real inventory movement uses action trails and status text
- start safe: select-then-assign flows first; arbitrary drag reposition
  waits until the assignment flow is safely distinct from mutation

## Types Of Maps

SLOT supports several map kinds, all built from the same home concept:

- **Global Carried Atlas** — the default. Stable homes for placed/pinned/
  important identities; unhomed → `Triage`; category pockets as
  destinations or suggestions
- **Category Pocket Maps** — each category owns a local sub-grid; blocks
  live in block geography, magic in magic geography, etc.
- **Kit/Task Boards** — task-shaped. Desired-count and loadout targets
  overlay existing homes; missing items ghost on the board. See
  [kits.md](kits.md).
- **Collection Boards** — persistent curated groupings (project kits,
  progression parts, ritual ingredients); collections are not just
  filters, they become physical planning surfaces
- **Source Maps** — known source containers with remembered layouts;
  requires stable source identity
- **Physical Storage Memory** — remembered organization *inside* known
  chests, surfaced when player searches/restocks or opens the source;
  navigation aid, not remote authority

## External Storage Memory

The goal is not "every remembered chest is always drawn on the atlas."
That implies terminal-like remote access and misleads players.

Direction:

- SLOT remembers where identities were last seen in known physical
  sources
- remembered sources appear **contextually** when the player searches,
  restocks, or opens/targets that source
- currently-opened sources appear as active islands or compare panes
- unopened remembered sources are **guidance**, not authority
- temporary trails (UI-local or particle) guide the player toward likely
  storage without implying remote mutation

### Find And Restock Trails

Instead of mounting every remembered inventory:

- "Find redstone" draws a trail from the redstone home to a remembered
  Ore Chest marker
- opening the Ore Chest makes the trail solid and shows current redstone
  count
- "Restock torches" draws trails to coal, sticks, torches, and crafting
  sources if memories exist
- stale memories draw dotted/smoky trails; verified active sources draw
  bright trails

Blocking rule: **no external-memory feature mutates an inventory unless
SLOT has current authoritative access to that source.**

### Terminal-Mod Replacement Hypothesis

A persistent visual memory layer could reduce terminal-mod dependence
for many playstyles without converting the base into an abstract
terminal:

- each known chest gets a stable visual identity
- items remember homes inside physical source maps
- search shows a source marker, route, or temporary trail
- chest roles help deposits pick the right reachable destination
- compare lenses show carried versus active external availability

SLOT should not become remote storage, infinite inventory, a network, or
autocrafting.

## Data Model (Conceptual)

Not an implementation commitment — see
[../plans/atlas-prototype.md](../plans/atlas-prototype.md) for concrete
types.

- **VisualHomeMap** — named map: id, kind (global/category/task/
  collection/source/external/temporary), layout/bounds/regions, camera
  defaults/bookmarks, home entries, version/revision
- **VisualHome** — one persistent placement: identity matcher, visual
  coordinate or region id, display priority, source/task scope, origin
  (manual/automatic), pinned/locked, last-seen metadata
- **VisualHomeProjection** — session-derived: home + counts (carried/
  external/desired), source breakdown, presence state, badges, available
  actions, diagnostics
- **TriageQueue** — items needing placement: new identities, ambiguous
  classification, conflicts, overflowed homes, variant-split families
- **MapViewportState** — ephemeral camera: map id, center, zoom,
  selected home, focused region, active lens, history. Screen/session
  posture, not authority or workflow history

## Animation Discipline

Good animations (animate spatial meaning):

- camera fly to search result
- icon trail from source to hotbar
- item entering the intake region
- item moving from `Triage` to permanent home
- category pocket zoom-in
- source island becoming active when opened

Bad animations (decorative motion without meaning):

- every refresh moving icons
- long resorting animations
- camera motion that interrupts urgent gameplay
- flickering background particles

## Costs And Risks

- **State complexity** — home maps introduce persistent UI state;
  migration/versioning, conflicts, item/mod renames, source identity
  changes, stale external memory
- **Cognitive overhead** — if every item asks for placement, the system
  becomes work. Mitigations: auto-home low-risk items; only ask for
  important/repeated items; keep unhomed items usable; Triage is fine
- **Misleading authority** — homes can look like slots. Mitigations:
  strong visual distinction, source badges, ghost states, source drawer
  on selection, fail-closed action diagnostics
- **Map overgrowth** — a global atlas can become too large. Mitigations:
  category pockets, semantic zoom, importance layers, overflow shelves,
  result trays
- **Animation cost** — view morphing and movement trails can be
  distracting or expensive. Mitigations: animate meaningful changes only;
  keep animations short; reduced-motion support

## Visual Treatment

Target feel: a Minecraft-plausible artifact, not a desktop application.
Possible lore framing: a "universal lens of matter storage," an ancient
dimensional instrument, a star/nebula/constellation map. This is
emotional texture — usability wins over theme. The motif is a swappable
layer; UX decisions should not depend on one lore explanation.

Guidelines:

- world remains faintly visible through a dark translucent scrim
- atlas canvas uses a stronger translucent tint so icons stay readable
- floating controls use glass-card backgrounds with enough opacity for
  contrast
- item cards can be more opaque than the canvas at detail zoom
- background texture is subtle and reinforces the map metaphor

Avoid: fully transparent text panels over noisy world backgrounds, large
opaque rectangles that make the atlas feel like a desktop app, hidden
hotbar/action rail, animated particles/nebula noise that flickers or
competes with item icons.

## Screen Regions (Preferred Layout)

- atlas canvas takes most of the screen
- `Triage` / intake region lives on the atlas near the default camera
- top-left floating search/navigation capsule
- top-right compact camera controls and optional minimap toggle
- right-side or bottom fixed hotbar/action rail
- bottom status/diagnostic strip, compact and low-contrast unless
  rejected
- selected-item inspector appears only when needed, as a right-side
  drawer or popover anchored near the selection
- projection panels attach to edges or the search capsule, never replace
  the atlas canvas

Hotbar rail placement stays open. Bottom rail preserves vanilla spatial
familiarity and number-key mapping. Right rail preserves vertical atlas
space and pairs well with a detail inspector. For the current prototype,
keep the right rail until search/navigation proves out.

## Design North Star

The ideal experience:

- the player opens inventory and sees a familiar personal geography
- they pan/zoom through that geography when the item set is large
- new items arrive in a visible triage place
- important items stay where the player put them
- search finds and teaches locations by navigating the map, not
  resetting it
- task/Kit views overlay meaning onto the same visual world
- external storage is remembered without pretending to be always
  reachable
- real mutations remain source-aware and server-authoritative
- visual treatment can feel mysterious, dimensional, or artifact-like,
  but only insofar as it makes the workspace more legible, memorable,
  and pleasant

## Appendix: Validated Workflows

Pressure-tested against the following workflows. This is a summary of the
prior workflow brainstorm — the atlas direction was judged net-positive
across all of them, with caveats noted.

- opening inventory — strong default camera + fixed hotbar keeps quick
  open cheap
- first-time setup / onboarding — Triage makes "no homes yet" usable
- finding a known item — spatial memory beats scanning sorted lists at
  scale
- finding a type/category — category pockets are neighborhoods, not sort
  modes
- ambiguous icon families — differentiator tokens at `Read` zoom plus
  inspector
- new pickup triage — intake region makes "what did I just get" visible
- junk cleanup — cleanup lens tints in place; cleanup tray groups
  actions
- avoiding ground drop re-pickup — recent pulse + Triage membership
- moving items to hotbar — existing ASSIGN path stays authoritative
- one-off use — cursor/hotbar gestures unchanged
- task switching — Kit boards (see [kits.md](kits.md))
- collections / project boards — persistent curated boards with ghosts
  for missing
- direct carried storage unification — one working set across main,
  hotbar, offhand, backpack
- external chest organization — opened source island with remembered
  layout
- external storage search without terminals — last-seen guidance +
  active-source compare
- deposit to organized storage — chest roles + active-source priority
- withdraw / restock — restock trails + active-source open requirement
- dual-pane comparison — compare lens with matching-identity badges
- crafting and recipe fill — crafting surface as first-class workflow
  (see [../architecture/overview.md](../architecture/overview.md))
- sort / restack — anchors don't move; stack consolidation is a lens
- protection, favorites, locks — protected anchors stay visibly locked
- multi-select and bulk actions — selection state overlays homes; no
  layout change
- cursor and direct manipulation — vanilla cursor semantics preserved
- equipment and accessories — equipment rail as a fixed landmark
- map customization — rename/recolor/icon/move/delete for player-created
  islands
- failure and stale memory — explicit "unknown if still present" states
- multiplayer/shared storage — per-player visual homes; shared
  containers don't leak per-player placement
- terminal-mod alternative — persistent visual memory + active-source
  compare as a non-authority replacement

Main unresolved problems carried into the prototype:

- potential heaviness for quick actions — mitigated by strong default
  camera, keyboard search, fixed hotbar, fast camera reset
- visual-home vs real-item movement — separated gestures, clear cursor
  modes, action previews, trails only for real movement
- map scale at thousands of identities — category pockets, semantic
  zoom, importance layers, overflow shelves, result trays, triage
- zoom-driven geometry breaking memory — stable anchor per home,
  screen-budget-aware templates, not linear scaling
- external memory overpromising — distinct reachable vs last-seen,
  active highlighting, fail-closed on unreachable
- implementation complexity — prototype in memory first, persist only
  after interaction feels good, start with player inventory only

## Open Questions

- Minimum home system that proves value without becoming a settings-heavy
  UI?
- Is pan/zoom fast enough for short Minecraft inventory interaction
  loops?
- Per item identity, item family, tag, category, or a mix?
- One family home or separate variant homes for same-icon families by
  default?
- What counts as stable external source identity in vanilla and modded
  contexts?
- Search default: in-place spotlight, result tray, or both?
- What persists across worlds, modpacks, saves?
- Removed-mod / item-id-change handling?
- Export/import as user preference profiles?
- Inferring homes from vanilla slot positions during migration?
- Can source islands make terminal mods optional rather than required?
