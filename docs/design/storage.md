# Storage And Atlas Scale Design

Last updated: 2026-04-30 (largely SUPERSEDED — see banner below)

> **The chest-side design in this document is retired.** Almost
> everything below — the **Claim button** in the vanilla chest UI,
> **chest tiles in a storage zone**, **island-to-chest links**,
> **chest-glow / link threads on proximity**, **per-item presence
> strip**, **deposit / withdraw verbs against linked chests**,
> **kit-driven shopping-trip wayfinding** — was replaced by the
> learned-storage swap that shipped 2026-04-30. The current model:
>
> - **Auto-claim on first deposit.** No Claim button.
> - **Chest chips in the left column**, not chest tiles in a storage
>   zone. Cluster grouping derived from spatial proximity
>   (`ChestClusterMap`), no explicit named areas.
> - **Affinity-based deposit routing**, no chest links. Affinity
>   decays over time + an explicit Forget gesture.
> - **Ghost atlas cards** for items in proximate chests render on
>   their visual home; **chest locator panel + `+N stored` corner
>   badge under search** surface non-proximate stocks. The old
>   "chest tile body is the chest's contents" grid retired.
> - **Take All / Take by identity** verbs survive on the chip /
>   loot-chest panel, but the deposit / withdraw model is replaced
>   wholesale by affinity routing.
>
> See [../plans/learned-storage.md](../plans/learned-storage.md) for
> the canonical design and [../plans/current.md](../plans/current.md)
> for the full recap. **Do not implement the link / area / chest-tile
> design below.** It's preserved as historical context — the
> motivation, edge-case enumerations, and the "What This Model
> Deliberately Does Not Do" list are still relevant context for what
> the project chose NOT to build (no remote storage, no logistics
> network, no per-identity chest claims, etc.).
>
> **The atlas-scale half** (`Asymmetric LOD`, `Fit-Carried Default
> Camera`, `Per-Region Carried Badges`, `Pocket Lens`) was
> generalized into [relevance-lod.md](relevance-lod.md) and remains
> accurate as the single-contributor v1 model; that part is the
> still-relevant content of this doc.

SLOT's atlas proves that stable visual homes work for carried inventory. This
document extends that model in two connected directions:

- making the atlas *readable* once the total identity count grows into the
  thousands, without sacrificing stability or leveraging compacted alternate
  views
- integrating external storage (base chests) as a lightweight companion to the
  island/home system, not as a second organization axis

The atlas-scale half of this doc was generalized into
[relevance-lod.md](relevance-lod.md), which replaces "carried high, ghost
low" with a per-item relevance score that combines carried, kit-relevant,
shopping-list, search, and proximity contributions. The "Asymmetric LOD"
section below is the seed of that model and remains accurate as the
single-contributor v1 implementation. **The storage-side text below is
retired** — see banner above and [../plans/learned-storage.md](../plans/learned-storage.md).

For the atlas concept, item homes, and the triage/home loop, see
[atlas.md](atlas.md). For the unified relevance-driven LOD model that
generalizes the scale story, see [relevance-lod.md](relevance-lod.md). For
the task-oriented bring-list semantics the storage model leans on, see
[kits.md](kits.md). For the near-term engineering sequence, see
[../plans/current.md](../plans/current.md).

## Core Bets

- **One organization axis.** Islands organize item identities. Chests inherit
  organization from islands via player-authored links. There is no second
  classification system (no tags, no roles, no per-identity chest claims).
- **Visual memory over database features.** Everything in this document biases
  toward stable spatial geometry and progressive disclosure. If a feature's
  win is "filter/sort faster", that feature belongs elsewhere.
- **Proximity gates all storage interaction.** SLOT never teleports items
  between distant bases, never routes across unreachable storage, and never
  mutates a container without current authoritative access.
- **Organize once.** A player shouldn't have to plan storage separately from
  carried organization. Linking an island to a chest is the only organizational
  action storage requires.

## Scale Response: Carried Readability In A Huge Atlas

The atlas can grow to thousands of identities on a heavily modded pack. At
any given moment the player is typically carrying 25–30 items (up to ~100
after a long gathering run). Most of the map is ghosts of identities the
player owns but isn't currently carrying.

Hiding ghosts or reshuffling by "what's carried now" would break the spatial
memory the atlas is built on. The response is visual prominence, camera, and
transient emphasis — not layout change.

### Asymmetric LOD For Carried vs Ghost

This is the v1 of relevance-driven LOD with a single contributor
("carried") and two effective bands ("full" vs "ghost"). The general
model and direction live in [relevance-lod.md](relevance-lod.md);
everything below describes the version landed in the current prototype.

Ghosts and carried items share the same stable anchor geometry but not the
same screen-budget rules:

- ghosts shrink aggressively as the camera zooms out; at overview they fade
  into a faint lattice
- carried items clamp at a minimum readable screen size regardless of zoom
- at overview the atlas reads as ghost terrain with ~30 carried beacons
  scattered across it; the *constellation* of carried items is perceivable at
  any zoom

Tuning goal: carried items must be findable by eye at the farthest zoom a
player would naturally reach when looking for "what am I carrying."

### Fit-Carried As Default Camera

The default camera on inventory open is the tightest fit that frames all
currently-carried items:

- scattered carried set → open zoomed out enough to see them all
- tightly clustered carried set → open zoomed in on that cluster
- empty carried set → fall back to Triage-centered or the previous camera

This replaces the earlier "overview" or "centered on Triage" default. The
player opens the inventory and immediately sees their pockets in context,
without needing to pan.

### Per-Region Carried Badges

Each neighborhood/region label carries a compact count of how many carried
items live in that region: "Food ·3 • Tools ·5 • Magic ·1". Readable from
anywhere on the atlas without hovering. Clicking a badge flies the camera
to that region.

Region bounds stay fixed; badges are label decoration, not geometry changes.

### Optional Pocket Lens (folded into relevance-lod.md)

A transient modifier-held lens de-emphasizes ghosts to near-invisible and
leaves carried items full-bright at their homes. Releasing restores the full
atlas. This is a view modifier, not a view mode — nothing moves.

In the relevance-driven model, the Pocket Lens is "set every contributor
except `carried` to zero for the duration of the modifier hold." Same
mechanism, no new primitives. See
[relevance-lod.md § Relevance contributors](relevance-lod.md).

## Atlas Topology For Storage

> **Direction (SUPERSEDED 2026-04-30):** both the original
> single-storage-zone design *and* the player-named storage areas
> direction described below are now retired. What shipped is the
> learned-storage swap:
>
> - Chests show as **chips** in a left-column panel above Triage, not
>   tiles in a storage zone.
> - **Auto-claim** on first deposit; no explicit claim or area
>   assignment.
> - **Derived clusters** via `ChestClusterMap` (16-block union-find)
>   provide the "area" grouping, with player rename via the chip's
>   context menu.
> - **Ghost atlas cards** for items in proximate chests render on
>   their homed island; **chest locator** + **`+N stored` badge**
>   surface non-proximate contents under search.
> - **Affinity decay + explicit forget** replace any per-area
>   demote / collapse gesture.
>
> See [plans/learned-storage.md](../plans/learned-storage.md) and
> [plans/current.md](../plans/current.md). The remaining text in this
> section is preserved for historical context only — do not implement.

Chests participate in the same pan-navigable atlas canvas, not a separate
continent. Two spatial zones share the atlas:

- **Carried zone** — the existing atlas geography of islands and homes. The
  default camera lives here.
- **Storage zone** — where claimed chest tiles live. Distinct chrome
  (archive / shelf / warehouse motif rather than the constellation motif of
  the carried zone), further from the default camera, pan-reachable.

Reason for one canvas: player-authored links between islands and chests must
be drawable as actual lines. Splitting into two disconnected continents would
force cross-continent link UX that gains nothing over a pan divider.

Bases are emergent. Chests the player drops near each other in the storage
zone *form* a base visually. No base concept in data — just arrangement plus
optional text labels ("Home Base", "Nether Outpost"). Multiple bases are
multiple visual clusters of chest tiles in the storage zone.

## Claimed Chests

A chest on the atlas is called a **claimed chest**. Only claimed chests
participate in the linking, deposit, and withdraw flows.

"Chest" here is the user-facing term for any claimable storage container.
In practice this covers vanilla chests and trapped chests, vanilla barrels,
modded sophisticated storage chests/barrels, storage drawer controllers,
and any other block that exposes an item handler the player can currently
access. The internal identity is a stable `storageId` that survives piston
moves and block-type agnostic — the atlas doesn't care whether a claim
wraps a 27-slot vanilla chest or a 4096-slot sophisticated barrel.

### Claiming

- when a player opens a chest in the world, a **Claim** button appears in
  SLOT's chest UI
- clicking Claim creates a chest tile in the storage zone
- auto-placement: when claiming, SLOT checks for already-claimed chests
  within some world-space radius and places the new tile near their atlas
  positions. Over time, chest arrangement in the storage zone approximates
  the player's world base layout without SLOT ever tracking world
  positions authoritatively
- the player can drag the tile freely after claim if auto-placement is wrong

### Unclaimed Chests Are Vanilla

Chests the player has not claimed never consume atlas territory. Loot chests,
dungeon chests, one-off containers all stay vanilla. Taking items from them
routes into the normal carried flow (Triage for new identities, or the
identity's existing island).

### Chest Destroyed Or Broken

When a claimed chest's in-world block is removed, its atlas tile disappears.
The player who broke it is already carrying the former contents and is in
the middle of dealing with them; a dead tile would be an extra dismiss step
for no information the player doesn't already have. Organization is rooted
in islands, not chests, so deleting the tile does not reassign any item's
home — the items land back in normal triage/home flow as they're used or
re-stored.

## Island-To-Chest Linking

A **link** is a player-authored relationship between an island and a chest
that declares: "items in this island belong in this chest when I'm nearby."

### Cardinality

- an island may link to multiple chests (same base with multiple overflow
  chests, or distinct bases)
- a chest may link to multiple islands (small bases where one chest holds
  multiple kinds of things)
- links are per-player (atlas state is per-player; chest identity is shared)

### Linking UX

Linking is an uncommon action; it should not require fancy tooling:

- primary: a small dropdown menu on a chest tile exposes "Link to island…"
  with the player's islands listed
- optional nicer affordance: a dedicated drag-handle on chests and islands;
  dragging from the handle to the other endpoint creates a link
- unlink: same menu, or drag the endpoint off

No bulk-link flows; no multi-select link UX. If it turns out players want
these, they earn their way in via observed friction, not speculation.

### Link Rendering (On-Demand Only)

- links are **not** drawn by default
- when the player is in world near a claimed chest, that chest's links
  render as faint threads on the atlas to each linked island, in addition
  to the chest tile glowing
- the linked islands also highlight so the player sees "these islands will
  deposit here if I trigger the verb"
- walking away fades both the chest glow and the link threads
- this is the entire on-atlas link visibility story. No hover-based link
  rendering, no always-on link threads

### Chest Tile Body Is The Chest's Contents

The chest tile on the atlas is not an opaque label — its body renders the
chest's **live contents as a grid** so the player sees at a glance what
each claimed chest currently holds. The grid uses the same item-card
vocabulary as the rest of the atlas (identity icons, counts).

Proximity gates activation, not visibility:

- when the player is outside world-proximity of the chest, the tile is
  fully drawn but **dimmed** (ghost-style alpha on chrome and cells),
  consistent with carried-vs-ghost treatment elsewhere on the atlas
- when the player steps into proximity, the tile **activates / lights
  up**: full brightness, a subtle glow on the border, and link threads
  + linked-island highlights (see above) render

This is the player's primary "what's in that chest" surface. The
detail-zoom per-item "also in:" strip on homed items (see below) is a
complementary inverse view ("which chests contain X") and remains a
separate surface.

### Per-Item Chest Presence

Players searching for "where is iron stored" don't need link lines for
that — they need per-item truth. When an item's home is zoomed in enough to
render its detail widget:

- the widget shows a compact "also in: Chest A · 128 · Chest B · 64" strip
- clicking a chest entry pans the camera to that chest tile

Per-item presence is observation of current contents, not a stored link.
It's computed from whatever chests are claimed and what they currently hold.

## Deposit Flow

### Trigger

A deposit verb (hotkey or button) fires while the player is within world
proximity of one or more claimed chests.

### Selection

For each stack in carried inventory:

- if the stack's exact identity has positive learned affinity with a
  nearby claimed chest, the stack is **eligible** for deposit
- otherwise it stays carried
- eligibility is further reduced by kit holdouts (next section)

### Destination

When a stack is eligible and multiple learned-affinity chests are nearby,
the destination walk follows affinity score, then stable storage-id order,
and inserts into the first chest with space. No classifier/facet
similarity, live-presence inference, empty-chest fallback, distribution
rules, concentration heuristics, or fallback routing to chests at other
bases.

### Full Chest Behavior

If every learned-affinity chest for the identity is full or inaccessible,
the deposit for that stack fails with a simple notification:

> Couldn't deposit Iron Ingot · 64 — Ore Chest full

The player's response is deterministic and low-friction: place the stack in
the chest they want to teach, then future deposits can follow that learned
affinity. No overflow tray, no review queue, no scatter.

### Feedback

Each successful deposit animates a brief particle trail from the carried
stack's on-screen position to the destination chest tile on the atlas (or,
when atlas is not open, to the chest in world). Trails are per-stack and
short.

## Kit Holdouts And Desired Carry

Storage flows cooperate with Kits (see [kits.md](kits.md)) to preserve the
items a player wants on their person for the current task.

### Kit-Only For Desired Carry

Desired carry is owned by Kits, not islands. Without an active Kit, the
player has no declared "I want to carry N of this" state, so every eligible
stack is subject to deposit at full count.

This keeps the model simple:

- Kit active with `bring: iron_pickaxe x1, torch x32` → those identities
  stay carried through deposit
- No Kit active → everything eligible goes

If playtesting shows that per-island defaults are needed (e.g., "I always
want some food on me regardless of Kit"), island-level defaults enter as
overrides. Not in scope for first prototype.

### Kit Protection

A Kit's protection flags (see [kits.md](kits.md)) already cover "don't let
cleanup destroy task-critical gear." Deposit respects the same protection —
a protected identity under an active Kit is not deposited even if its
island has a linked chest.

## Withdraw Flow

### Explicit Withdraw

A withdraw verb near linked chests pulls items up to desired-carry targets
declared by the active Kit:

- for each `bring` entry in the active Kit, if carried count is below the
  target and the identity's island has a nearby linked chest with the item,
  pull to target
- particle trails animate chest → player, one per identity

Without an active Kit, explicit withdraw does nothing. This matches the
symmetry of deposit (no Kit → no desired carry → no withdraw intent).

### Implicit Withdraw On Kit Activation

Activating a Kit while at base auto-runs the withdraw flow. If the player
activates a Kit far from base, the activation completes normally and no
withdraw happens. This saves a keystroke in the common case without
introducing long-distance semantics.

### Take All

Each claimed chest exposes a "Take all" verb (via its tile on the atlas or
via the chest's UI when open) that empties its contents to carried inventory,
respecting available space. Useful when abandoning a base, consolidating
stock, or doing a manual reorganization.

### Ad-Hoc Manual Use

Opening a chest in world and interacting with its contents directly still
works exactly as vanilla. Nothing in this design replaces manual interaction;
the verbs augment it.

## Readiness Indicators

While the player is near linked chests, islands whose carried contents
exceed desired-carry pulse faintly on the atlas: "something here is ready
to deposit." Readable without being a notification. Below desired-carry
pulls the opposite visual cue: "this island has headroom to withdraw."

These indicators are ambient. The player can ignore them and the deposit
verb still works; they exist to let the player glance at the atlas and know
whether a deposit session is worth triggering.

## World-Side Wayfinding

The atlas shows *what* is where. World-side cues show *how to physically
get there*. These layer on top of the atlas model without replacing it.

### Particle Trails

A request for a specific item (from search, a Kit bring-list restock, or a
manual query on an item's home) can emit a particle trail in the world
leading from the player toward the nearest claimed chest containing that
identity.

Trail rules:

- only for items the player explicitly asks about — not ambient; not for
  every carried item
- draws while the player is outside the storage's active proximity; fades
  as the player arrives
- multiple trails are acceptable for search results with multiple chest
  sources; thinner trails for farther / smaller-count sources
- trails do not imply remote authority; arriving at the chest still
  requires opening it to act on its contents

### In-World Item Holograms

When a requested item is in a chest within close range, a small floating
item icon renders above the chest block, visible through walls up to some
short radius. Fades with distance. Cheaper and more legible than long
trails in dense bases.

### Compass Gesture

Holding a modifier key while an atlas item is selected renders an on-screen
compass arrow pointing toward the nearest chest that contains it. Useful in
caves, the Nether, or open world where trails can't path cleanly.

### Kit Procurement

Activating a Kit with missing bring items and walking toward base activates
the wayfinding stack: trails to chests that contain the missing items,
holograms in close range, compass available on demand. Opening each chest
fills carried to target. Feels like a shopping trip, not a database query.

## What This Model Deliberately Does Not Do

- **No long-distance deposit or withdraw.** Proximity always gates.
- **No routing across bases.** A full nearby chest is a full nearby chest;
  SLOT does not forward to a different base.
- **No chest roles, tags, or classification.** Links carry the full
  organization signal.
- **No per-identity ownership tracking.** A chest doesn't remember "I own
  iron." Whatever's in the chest is what's in the chest.
- **No automatic distribution or balancing.** Nearest with space, that's it.
- **No overflow tray / review queue.** A simple notification is enough.
- **No island-level desired carry.** Kit-only until evidence says otherwise.
- **No world-position tracking on the atlas.** Storage zone is freeform;
  world position is handled by wayfinding, not atlas geography.
- **No cross-continent link UX.** One canvas; pan-reachable zones.
- **No terminal-like remote access.** SLOT is not AE2, RS, or a logistics
  network. Everything physical happens where the player physically is.

## Deferred And Later

Explicitly parked for now, to be reconsidered only if real friction
demands them:

- **Pins for slot-level bindings.** A declared "this specific slot in this
  specific chest holds this identity" — useful for Kit-critical gear that
  the player wants in a known exact place. Deferred because island-level
  linking covers the common case; slot-level binding can be added as an
  island→chest-slot refinement later.
- **Bulk actions for non-stackables.** Multi-select and mass-deposit of
  unique items like spellbooks; leverages triage learned rules to offer
  "deposit all these to Spellbook Chest" in one tap.
- **Per-island desired-carry defaults.** Only if kit-only turns out to be
  insufficient.
- **In-world holograms and compass.** Particle trails come first; the other
  wayfinding surfaces enter after trails prove useful.
- **Per-chest internal layout memory.** Remembering where items sit *inside*
  a chest grid. Deferred; the link model doesn't need it.

## Edge Cases Named Now

- **Shulker boxes and portable chests.** Not claimable; they travel with
  the player. They interact with the UI as inventory containers, not storage
  participants. Stated explicitly so the claim button is hidden for them.
- **Ender chests.** Shared virtual contents at many world positions. Claim
  once on the atlas; trigger proximity by any ender chest block.
- **Multiplayer.** Atlas state is per-player. Chest identity is shared, but
  each player's links to that chest are independent. A deposit by player A
  doesn't affect player B's atlas organization.
- **Reassigning an island's link.** Nothing retroactive. Existing chest
  contents stay put; the link only governs future deposits and withdraws.
- **Exception items** (e.g., rare ore goes to a Trophy chest, common ore to
  Main). The player models them as two islands. No per-item override at
  the link level.
- **Item in carried but not in any island.** It's in Triage. Triage doesn't
  link to chests. The item stays carried until placed in an island, which
  the existing triage/home loop already handles.

## Open Questions Worth Watching

- How does link visibility behave when the player pans to the storage zone
  while near a claimed chest? Do the threads stay drawn while they're
  on-screen even if the player moves out of world-proximity during the pan?
  Probably: "proximity at verb-trigger time" is what matters; on-atlas
  visibility is a UX affordance, not authority.
- Does the "fit carried" default camera feel right when the carried set
  spans many distant regions, or does it open too zoomed-out to read? May
  need a fallback: if fit would require zooming below a readability
  threshold, fall back to centering on the largest cluster instead.
- Do players develop a mental model of "storage zone" quickly, or does it
  feel arbitrary? The auto-placement on claim should help by making the
  zone's layout reflect the player's world; worth manual-testing early.
- How loud should the "couldn't deposit — full" notification be? Probably
  status-strip level, not a modal. But if players consistently miss it,
  escalate.

## Prototype Slice Sequence

This design is downstream of the current triage/home work in
[../plans/current.md](../plans/current.md). The slices below assume that
loop is landed.

1. **Atlas scale helpers.** Asymmetric LOD for carried vs ghost, fit-carried
   default camera, per-region carried badges. No storage code; these prove
   the visual model holds up at scale before storage grows it further.
2. **Claim and tile.** Claim button in the chest UI, a chest tile in the
   storage zone, auto-placement by world proximity, drag to reposition.
3. **Island-to-chest link.** Dropdown-based link UX, link data model,
   proximity-gated rendering of links and chest-glow.
4. **Deposit verb.** Proximity-triggered deposit respecting kit holdouts,
   nearest-linked-with-space destination, simple "chest full" notification
   on failure. Particle trail feedback.
5. **Withdraw verb.** Explicit withdraw to kit bring targets, implicit on
   kit activation at base. Take-all on chest tile.
6. **Per-item chest presence.** On an item home's detail widget, "also in:"
   strip with count per chest and click-to-pan.
7. **Particle trails.** In-world wayfinding trails for explicit item
   requests. Holograms and compass come later.
8. **Persistence.** Claimed chests, island-chest links, and auto-placement
   history persist across client restart and world reload.

After these, revisit deferred items against observed friction.
