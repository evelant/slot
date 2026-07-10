# Learned Storage — Design Sketch

Last updated: 2026-07-08

> **Affinity-role follow-up LANDED 2026-05-21.** ADR
> [0008](../decisions/0008-chest-roles-and-affinity-correction.md)
> replaces the old claim/forget control with one active-chest role
> button: `Storage -> Input -> Output -> Ignore -> Storage`. Freshly opened
> unclaimed chests display as `Ignore`; first player deposit still
> auto-claims as `Storage`. Only `Storage` learns affinity or accepts
> ambient quick/bulk deposit. `Input` and `Output` remain
> visible/searchable/pullable but are not ambient deposit targets;
> `Output` is preferred first for take shortcuts and `Input` is tried last.
> Both accept put shortcuts only while their interface is open. `Ignore` is hidden from SLOT storage
> projection. Machine/vessel deny tags cover station-like hosts, item
> context menus can clear one active-chest affinity bond, and moving the
> last known stack from one storage chest to another clears the old
> origin affinity.

> **Bug track LANDED 2026-04-30.** All 14 original UX bugs that
> playtesting surfaced shipped, plus a follow-on batch of 9 bugs from
> real-instance testing. The recap lives in
> [current.md](current.md); the active-bug-track section in
> [../status.md](../status.md) is closed. Highlights from the resolution:
>
> - **Claim flow.** Right-click intercept now matches anything
>   `ChestStorageAnchors.isClaimable` accepts; V hotkey is
>   context-sensitive (opens the chest's vanilla GUI when a loot panel
>   is showing); drag carried items onto the loot panel auto-claims and
>   deposits in one gesture. The active role button is now the normal
>   way to hide or re-enable a claimed chest.
> - **Chest correction.** The current correction path is the active role
>   button plus per-item `Don't auto-deposit here` from the item context
>   menu while that chest is open.
> - **Layout.** Triage carries a 120 px soft floor; the chest locator
>   (renamed from "Search matches") docks top-left under the search
>   input; all four left-column panels now compose into a single
>   `LeftColumnBuilder` flex column.
> - **Carried-also-stored indicator.** Bottom-left `+N` badge on
>   carried + ghost cards under search, summing proximate + elsewhere
>   stock. The illegible `presence` strip retired.
> - **Cross-surface hover.** Wired both directions through
>   `hoveredStorageId` / `hoveredAtlasIdentity`; lookups now consult
>   both `presence` and `elsewhere` so non-proximate chests visible
>   under search participate in the highlight pulse.
>
> **Status:** All phases landed; all bugs landed. Diagnostic logging
> in `AtlasNudgeLayout` / `AtlasLayout` is still on for the
> initial-open overlap fix; remove once a playtest confirms the layout
> converges cleanly across resolutions / GUI scales.
>
> - **Phases 1–4 (structural swap):** chest tiles → chips, ChestLink /
>   StorageArea deleted, deposit routing reads ChestAffinityMap, and
>   proximate-chest contents project as faded ghost cards on homed
>   islands.
> - **Auto-claim on first deposit:** `ChestDepositObserver` wires
>   `PlayerInteractEvent.RightClickBlock` + `PlayerContainerEvent.Open/
>   Close` for vanilla `ChestMenu`. On close with net-positive deltas,
>   the chest auto-claims (single + double chests, with the storage-id
>   attachment stamped on both halves) and `recordDeposit` is called per
>   identity. Net-zero or negative sessions skip — that collapses the
>   30 s "take-back guard" to "same-session take-back" for the most
>   common case.
> - **Loot-chest panel (atlas-side):** when an unclaimed chest is
>   within proximity (8 blocks), the workspace projects its contents
>   into `viewModel.lootChestPanel` — one Triage-style row per unique
>   item identity, with chip suggestions from the same signal-extractor
>   pipeline as Triage (so unhomed identities get FOOD / TOOLS / etc.
>   chips). `LootChestPanelBuilder` docks the panel immediately right
>   of Triage on the atlas. Interactions:
>   - click chip → home the identity (existing chip-accept flow)
>   - click row → take that identity into carry (`sendLootChestTakeIdentity`)
>   - shift+click row → accept the top chip then take, so the item
>     lands homed instead of in Triage
>   - "Take all" → for each unhomed identity in the chest, accept
>     the top chip suggestion server-side first (so the item lands
>     at its suggested home rather than Triage), then take everything
>     into carry. Already-homed identities skip the accept step and
>     route to their existing home through normal deposit flow.
>
>   The panel is purely derived from proximity — no payload, no
>   client-side state holder. `LootChestProximityResolver` walks the
>   player's loaded chunks, filters for `ChestBlockEntity` lacking a
>   SLOT storage-id (and no anchor-matched workflow claim), and
>   returns the closest. The chat-line summary on close stays as a
>   complementary post-interaction confirmation. The chest still
>   doesn't auto-claim until the player actually deposits.
> - **Affinity decay (lazy, currently disabled):**
>   `ChestAffinity#effectiveScore(tick)` and
>   `ChestAffinityMap#decayed(tick)` keep the 1-point-per-in-game-day
>   decay implementation, but the default path has `DECAY_ENABLED=false`
>   for playtesting until the rate is tunable.
> - **Cluster derivation:** `ChestClusterMap.derive(chests)` runs
>   union-find with a 16-block threshold (same dimension only). Chest
>   chips carry a `clusterId`; the panel renders a cluster header above
>   each multi-chip cluster. Default labels are stable ordinals
>   ("Storage Area 1"). Sticky-on-split rename is deferred — cluster ids
>   are derived from the smallest-uuid chest, so single-chest churn
>   keeps existing chips stable but cluster-spanning topology changes
>   may renumber.
> - **Cross-surface highlight pulses:** chip title bars light when the
>   player hovers an atlas card / island whose contents overlap the
>   chest, and atlas item cards light when a chest chip is hovered.
>   Reuses the existing `hoveredAtlasIdentity` / `hoveredIslandId` /
>   `hoveredStorageId` plumbing.
> - **Search-as-find:** `ElsewhereGhostProjection` walks non-proximate
>   chests and threads `elsewhere: List<ChestPresenceEntry>` onto each
>   `AtlasItem`, with the chest label suffixed by dimension
>   ("Storage Area 2 — nether"). Atlas cards render an "elsewhere: …"
>   strip when a search query is active. Non-proximate-only items
>   aren't synthesized as new cards yet; they're only surfaced if the
>   player has carried or homed the identity already (gap noted).
> - **Kit ghost markers:** when a kit is active, every needed-but-not-
>   carried identity (page slots + bring list) flags `kitNeeded` on
>   matching `AtlasItem`s; identities with no existing accumulator get
>   synthesized ghosts so they show up on their visual home. The atlas
>   card paints a "★" badge top-left.
>
> Two intertwined moves:
>
> 1. **Replace the explicit-link / explicit-claim / explicit-area-
>    assignment trio** with a transparent, learned-affinity routing
>    model. (Affinity, auto-claim, auto-cluster.)
> 2. **Surface external storage as ghost cards on atlas islands**
>    (faded, count-badged) for items present in *proximate* chests.
>    No separate chest grid in the workspace; the chest panel is a
>    minimal chip stack. Search and kit-activation extend the same
>    ghost machinery to non-proximate items.
>
> The two compose: affinity drives routing; proximate-ghosts make
> reachable storage visible without inventing a new surface.
>
> Per AGENTS.md, the original landing deleted old link/area artifacts
> in the same change — no migration, no dual-write, no soft-deprecation.

## Why this exists

Today the player has to manage three explicit concepts to make
deposits work the way they want:

1. **Claim** a chest in world (right-click) before SLOT touches it.
2. **Assign** that chest to a Storage Area (Main Base, Mountain Mine, …).
3. **Link** the chest to one or more atlas islands so deposit-from-
   island routes there.

All three exist to encode the same underlying intent: *"this chest
holds these kinds of items."* Three concepts is twice too many. The
link UI in particular is the worst part — a popup picker that
overflows the screen, can only be opened from the chest side, and
hides which islands are already linked.

## Player goals (what we're actually serving)

1. **Don't run between chests** to keep things organised. Standing
   near a chest cluster + a deposit gesture should put each carried
   item where it belongs.
2. **Find stuff** across thousands of items without remembering which
   chest is which.
3. **Quickly grab kits** of items the player knows they need.

Existing pieces already cover some of this: the kits system handles
(3) once routing exists, and `ChestPresenceEntry` on item cards
already tells you "iron lives in chest A and B". What's missing is
the routing layer for (1) and the discoverability layer for "this
chest *wants* iron, currently empty of it" for (2).

## Concept

**Learned per-(chest, item) affinity** is the single relation. It
replaces ChestLinkMap and the explicit-claim and explicit-area-assign
gestures.

- Every player-initiated deposit of item X into a `Storage` chest C
  raises `affinity[C, X]` by one increment. Eligibility still defaults
  to at least 6 storage slots; `slot:no_storage_affinity` denies a
  block, and `slot:storage_affinity_allowed` admits a small but
  deliberate storage block.
- Chest role gates participation:
  `Storage` is visible, learns affinity, and accepts quick/bulk deposit;
  `Input` is visible/searchable/pullable, takes last, and never learns or
  accepts ambient quick deposit; `Output` is visible/searchable/pullable,
  takes first, and never learns or accepts ambient quick deposit; `Ignore` is
  hidden from SLOT storage projection.
- Affinity decay exists in code but is disabled for current playtests;
  re-enable only after the decay rate is tunable and validated.
- Reorganization is treated as intent: if the player empties an identity
  from one `Storage` chest and next deposits that identity into a
  different `Storage` chest, the old origin affinity is cleared.
- Current contents of a `Storage` chest also count as routing evidence:
  if a chest has 12 iron in it right now, deposit may route iron there
  regardless of history. `Input` and `Output` contents remain
  visible/pullable but are never ambient deposit-routing evidence.

Routing reads from `affinity[C, X]`. Discoverability reads from it
too (ghost-slot "wants").

## Auto-claim

A storage-affinity-eligible chest becomes part of the workspace as
`Storage` the first time the player **deposits** into it (not on open,
not on take). A newly opened chest that has not been deposited into is
shown as `Ignore` in the active strip so the player can explicitly opt
it into storage if needed. Machine input buffers should usually be set to
`Input`, machine output buffers should usually be set to `Output`, and
stations and vessels should be
denied by storage-affinity tags and can still be forced to `Ignore` if
they slip through.

## Auto-clustered areas

Chests cluster into named regions purely from spatial proximity
(connected components within ~16 blocks of each other in the same
dimension). Cluster names default to a stable ordinal
("Storage Area 1", "Storage Area 2", …) assigned in cluster-creation
order; the player can rename. World coordinates aren't meaningful UI
for the player so we don't surface them in the default name. Ordinals
are sticky — splitting an existing cluster keeps the original
ordinal on the larger half and the next free ordinal goes to the
smaller half, so existing names don't shuffle when the topology
changes. There is no behavioural consequence to which cluster a chest
belongs to — clusters are visual grouping only.

Risk: clusters can split or merge as the player adds chests. Mitigated
by a generous threshold and by the fact that area boundaries don't
gate behaviour (routing is per-chest, not per-area). The strip just
re-groups.

## Routing policy

For each carried item the player wants to deposit:

1. Restrict to **proximate** chests (already gated by
   `ChestProximityResolver`).
2. Restrict to chests whose role is `Storage`. Normal Minecraft
   shift-click/machine insertion can still move items into stations or
   buffers; SLOT just does not remember those moves as homes or choose
   them as quick-deposit targets.
3. Among those, find chests with `affinity[C, X] > 0` or matching live
   contents.
4. Rank by affinity score first, then content-only evidence, then
   stable storage id. Deposit until full, then spill to the next
   candidate.
5. If no proximate `Storage` chest has affinity or matching live
   contents for X, the item stays in carry. Surface it as a "needs a
   home" hint (Triage already handles unhomed items; reuse that
   surface).

## Hover and cross-highlighting

Hovering anything that carries an item identity should make every
other surface representing that identity respond. The principle:
*one item identity, one cross-surface highlight pass, in both
directions.* This subsumes several UX bugs from the link era (chest
card glowing the whole tile when its linked island is hovered, no
glow when an atlas item is hovered, etc.).

Concretely:

- **Hover atlas item card →** highlight matching cells in proximate
  chests, pulse the ghost slot in chests with affinity for that
  item but currently empty of it, light the title bar of every
  chest tile that participates.
- **Hover chest cell (real item) →** highlight the matching atlas
  card.
- **Hover chest ghost slot (affinity, empty) →** highlight other
  chests with affinity for the same item, plus the matching atlas
  card. Communicates "this is where else iron lives / would live."
- **Hover chest tile title →** highlight every atlas card the chest
  has stock or affinity for. Communicates "this chest is for these
  items."
- **Hover atlas island title →** light the title bars of chests
  whose stock/affinity overlaps the island. (This is what the
  link-era island-hover-glows-chest behaviour was trying to do; it
  becomes a derived overlap query instead of an explicit link
  lookup.)

Highlight scope: only the title bar lights up, not the whole tile,
to avoid the "blinding" effect from the current implementation
where the entire chest panel glows. The hover state plumbing
(`hoveredAtlasIdentity`, `hoveredChestCellIdentity`,
`hoveredIslandId`, `hoveredStorageId`) is already in place; this is
a wiring exercise on top of the same fields.

**Drawing visible link lines** between hovered-item and its chests
is tempting but defer — common items (sticks, planks, cobble) would
fill the screen with lines. Revisit if highlight + pulse turns out
to be insufficient.

## Proximate-ghost UX model

The core UX primitive: every item present in a *proximate* chest renders
as a *faded ghost card on its homed island*. Ghost cards aggregate counts
across all proximate chests holding that item; per-chest breakdowns
surface on zoom (LOD).

This collapses several previously separate surfaces into one consistent
rendering. The atlas now shows:

| State | Render |
|---|---|
| Carried, not in any proximate chest | vibrant card |
| Carried, also in some proximate chest | vibrant card with "also stored" pip |
| Not carried, in some proximate chest | faded ghost card with aggregate count |
| Not carried, not in any proximate chest | not rendered (search reveals) |

The exact visual balance — how prominent the "also stored" pip is, how
faded the ghost is, how the count is laid out — needs playtest tuning.
We already have a pip glyph and a faded-ghost render path from the
old "homed but not carried" model; we re-use them with this new
semantic.

**Atlas-as-reachable-state** is the principle. Ghost = "you can grab
this from where you're standing." Items in non-proximate chests stay
invisible until search or kit-activation surfaces them; this preserves
the model.

### Proximate chest panel

A small left-side panel above Triage stacks *chest chips* — one chip
per proximate chest. Each chip:

- Chest name (default `Chest #abcd`, renamable)
- Slot fullness summary (e.g., `16/27`)
- Role-visible participation (`Storage`, `Input`, and `Output`; `Ignore` is hidden)
- Drag target only when the chip is `Storage`

Chips do **not** render the chest grid. Contents surface as ghosts on
islands; the chip is awareness plus a role-gated action target. The
chest's actual slot grid is a *detail surface* opened only on demand
(right-click chest in world, or a "show contents" action on the chip
if needed). The default workspace never has a chest grid visible.

If the chip count grows large (>5–7), the panel becomes scrollable.
Sort by recent affinity / use so the relevant chests sit on top.

### Interactions

Mirror vanilla / InventoryTweaks / MouseTweaks for muscle memory.
Existing keyboard primitives (shift+scroll for incremental take/push,
spacebar to zoom) carry over.

- **Click ghost** → take one stack from highest-affinity proximate
  chest into carry.
- **Shift+click ghost** → take all matching to carry (or until carry
  is full).
- **Shift+scroll on ghost** → incremental take.
- **Drag ghost** → take to drop target.
- **Drag carried card onto ghost / onto chest chip** → deposit
  (routed to highest-affinity proximate chest with capacity).
- **Spacebar zoom on ghost** → reveal per-chest breakdown (which
  chest holds how many) via existing LOD.

LOD already drives detail surfacing on zoom; explicit "click for
details" is unnecessary because spacebar zoom is the existing gesture.

### Correction gestures

- **Chest role button** in the active chest strip → cycles
  `Storage -> Input -> Output -> Ignore -> Storage`. Setting a chest to
  `Input`, `Output`, or `Ignore` clears its learned affinity.
- **Don't auto-deposit here** on an item while an active chest has
  affinity for it → clears only `affinity[C, X]`. Use case: one item
  was temporarily staged in the wrong storage chest.

### Cluster rename

Cluster rename happens on the cluster header in the chest panel
(if we surface clusters as visual groupings of chips) — replaces
the old area-rename UI.

## Loot chests and unhomed-reachable items

A chest the player has never deposited into is a *loot chest* — its
contents do **not** participate in island ghosts. Loot chests live in
a different mental model: random world chests, dungeon caches,
village storage, NPC chests. Treating their contents as ghosts on
islands would pollute the atlas with items the player is not trying
to organise.

Instead, opening a loot chest surfaces its contents in a
*Triage-like docked panel*:

- **Items the player has homed elsewhere** → suggestion chip points
  to the existing home (`→ Materials`). Accept = deposit-route to
  that island's affinity-matched chest, or take into carry.
- **Items the player has not homed** → standard Triage-style chip
  suggestions (FacetIndex roles, etc.). Accept = home and either
  carry or deposit-route.
- **Take-all** action on the loot panel → unhomed items go to
  Triage; homed items take into carry (or deposit-route, configurable).

The loot chest panel is *transient*: it exists only while the chest
is open. Closing the chest dismisses the panel. The chest itself is
**not** auto-claimed into the workspace until the player explicitly
deposits into it (consistent with the auto-claim trigger above).

Two distinct mental models:
- *Storage chests* — your organised storage, surfaced as ghosts on
  islands.
- *Input chests* — process ingredients/staging storage, searchable and
  pullable only after outputs and storage, and never ambient quick-deposit homes.
- *Output chests* — process result storage, searchable and pullable before
  storage, and never ambient quick-deposit homes.
- *Loot chests* — random world chests you encounter, surfaced as a
  Triage-style overlay only while open.

A chest moves from "loot chest" to "storage chest" the moment the
player deposits into it (auto-claim), unless the player changes its
role afterward.

## Search behaviour

Search currently zooms the atlas to results. With proximate-ghosts,
the better behaviour is:

- **Carried matches** → highlight matching cards on their islands
  (no zoom).
- **Proximate-chest matches** → already rendered as ghosts; highlight.
- **Non-proximate chest matches** → temporarily render as ghosts on
  the appropriate island with an "elsewhere" badge. Spacebar zoom on
  the ghost reveals which chest holds the item ("Mountain Mine
  Chest #3, in nether").

Search becomes a *find-where* tool, not a *zoom-to* tool. The atlas
stays in place; ghosts reveal the locations.

## Kits integration

Kit activation surfaces non-carried needed items as *temporary
ghosts* on their homed islands, with a "kit-needs" indicator. Same
rendering machinery as proximate-chest ghosts; different driver.

- **Reachable items** (in proximate chests) appear as standard
  ghosts; the player grabs them with normal click/shift-click.
- **Non-reachable items** (in non-proximate chests) appear as
  ghosts with an "elsewhere" badge. Spacebar zoom shows the source
  chest. Player walks there; ghosts upgrade to reachable as
  proximity changes.
- Once the kit's needed amount is satisfied (carry meets target),
  that item's kit-needs marker fades; satisfied slots render normally.

Routing primitive symmetry stays:

- **Kit activate** = `route(item) → take` against
  `affinity[C, item]` for proximate chests. Items the player can
  reach now are pulled; the rest are surfaced as ghost markers
  guiding the walk.
- **Kit deactivate** = `route(item) → put` against the same map.
  Excess items deposit-route opportunistically as the player walks
  past proximate chests.

The kit's "bring along" list reuses the same fetch logic.

**Open question:** when a player ends a kit far from any storage
chest, should the carry items return to the player's main inventory
(today's behaviour) or stay in carry until next deposit-near-chests?
Recommendation: stay in carry, surface "kit returned X items to
carry — deposit when near storage" in the status line. Avoids the
"kit-end mysteriously dumps items into hotbar" surprise.

## Standing orders (tentative)

Beyond kits, a player commonly wants persistent baseline carry:
sleeping bag × 1, pickaxe × 1, full armor set, food × 16. This is
worth a separate concept from kits — a flat per-player "always
carry" list with desired counts. Implementation deferred; sketched
here to keep the model coherent:

- Standing orders + active kit compose: target = max-overlap union.
- Same fetch primitive: ghosts surface needed-but-not-carried items
  on their islands, walk to grab.
- Implicit defaults from item type (tool=1, food=16, blocks=64)
  cover 90% of cases; scroll-wheel on a slot overrides.

## Landed shape

The old link/area/chest-tile model is gone. Live storage state is:

- `ClaimedChest` plus `ChestRole`
- `ChestAffinityMap`
- derived `ChestClusterMap`
- proximate and elsewhere content projections
- loot-chest overlay while an unclaimed chest is open
- active-chest role button
- per-item active-chest affinity correction

Routing is implemented in `DepositPlanner`; observation and first-deposit
claiming live in the loader deposit observers; persistence lives in
`WorkflowDomainFileStore`. The UI no longer renders chest grids as the primary
workspace surface.

## Edge cases / open questions

- **Bootstrapping.** First-ever deposit into a never-touched chest:
  no affinity anywhere and no claimed proximate chest already holds
  the item, so the item stays in carry. Player manually drops one
  stack into the eligible chest of their choice; existing contents and
  then learned affinity make future deposits route. Deposits into
  station-sized inventories still perform the vanilla/station action
  but do not bootstrap persistent storage memory.
- **Decay rate.** The code still has a lazy decay implementation, but
  playtesting rejected decay as the main correction mechanism. Keep it
  disabled unless a future design gives the player clear control.
- **Cluster threshold distance.** 16 blocks as a starting point;
  generous enough that a base feels like one cluster.
- **Multi-stack split policy.** Prefer single chest, spill on
  full. Open question: if the player is carrying a 64-stack of
  building blocks and the highest-affinity chest can take only
  16, do we offer them a choice or just spill silently? Silent
  spill matches "transparent" but loses control. Default to
  silent + status line summary ("deposited 64 building blocks
  across 3 chests"); add an explicit "deposit one stack to
  active chest" gesture if needed.
- **Server multi-player.** Affinity is per-player today
  (`SlotPlayerWorkflowRuntimeService.runtime(player)` is
  per-UUID). That stays right here.
- **Cross-dimension chests.** Items in non-current-dimension chests
  do not appear as ghosts on the atlas (the atlas is "reachable
  now"). Search reveals them with an "elsewhere" badge.
  Cross-dimension chests still own their affinity — the moment the
  player crosses the portal, their items materialise as ghosts and
  routing kicks in. Open question: should the chest panel show a
  collapsed "+N chests in nether" entry while in overworld, or stay
  silent? Probably the former — surface the existence of the other
  base, just don't pollute the atlas with its contents.
- **Loot vs storage chest ambiguity.** A chest *could* be both —
  player loots a dungeon chest, then deposits something into it
  intending to use it. Auto-claim on first deposit handles this:
  one deposit moves the chest from loot to `Storage`. The player can
  then cycle it to `Input`, `Output`, or `Ignore` if it was actually a
  process chest.
- **Loot chest opened from a remote source** (e.g., Refined Storage
  remote-access mods). The loot panel should fire on the chest
  GUI open event regardless of how it was opened, since the player
  expects the same Triage-style help. Sources without GUI events
  (auto-loot machines, etc.) are out of scope; treat as the
  player's own internal storage.
- **Backpack contents and carried containers.** Don't deposit
  *contents of carried containers* — only loose carried items.
  Already the existing rule, no change.

## Risks

- **Role friction.** The role button is intentionally manual. If
  players still fight feeder/buffer setups, improve the tooltip or
  placement before reintroducing automatic feeder detection.
- **Cluster instability.** Adding a chest that bridges two
  clusters merges them, which renames the visual area. Mitigated
  by leaving rename intact; clusters are visual only.
- **"My deposit went to the wrong chest."** With links the player
  could verify intent; with affinity they can't. The role button,
  per-item affinity correction, rehome-on-move, post-deposit status,
  and undo are the recovery path.
