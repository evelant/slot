# Storage Prototype Plan

Last updated: 2026-04-17

Status: near-term engineering plan for the first end-to-end prototype of the
atlas scale features and island-to-chest storage integration. Slice 0 and
Slice 1 landed; Slice 2 is in progress — the common-side domain and the
NeoForge-side server identity machinery are in, UI/RPC/rendering still
open. See per-slice status headers below.

For the storage concept and interaction model, see
[../design/storage.md](../design/storage.md). For the atlas concept that this
builds on, see [../design/atlas.md](../design/atlas.md). For the broader
near-term engineering sequence, see [current.md](current.md). For the
operational baseline, see [../status.md](../status.md).

## Goals And Non-Goals

Goals:

- prove the **carried readability** story (asymmetric LOD, fit-carried camera,
  per-region badges) on an atlas populated with many identities
- prove the **one-axis storage model** end to end: claim a chest, link it to
  an island, deposit eligible stacks, withdraw to Kit bring targets
- keep every flow proximity-gated and fail-closed — no remote authority, no
  cross-base routing
- land a **test helper command** that makes a populated atlas reproducible
  without hours of gameplay
- keep every slice independently evaluable: carried readability ships first
  and is valuable even if storage slips

Non-goals:

- visual polish; greybox chrome is expected until the interaction model lands
- world-side wayfinding (particle trails, holograms, compass gesture)
- pocket lens view modifier
- per-chest internal layout memory
- slot-level pins
- per-island desired-carry defaults
- multi-select / bulk link or deposit flows
- multiplayer sync of per-player link state (single-player first)

## Prerequisites

This prototype is downstream of the triage/home loop in
[current.md](current.md).

As of 2026-04-17, the triage/home loop is landed enough to support most of
this plan: template + learned suggestion chips, home assignment, island
management, and persisted homes are all in place. Slice 3b (reversible
assignment records) is partial, and slice 5 (search spotlight) has not
started, but neither blocks the storage prototype.

What each slice needs:

- slices 0–3 (test helper, carried readability, claim, link) have no open
  dependencies and can start immediately
- slice 4 (deposit) can ship a **without-Kit-holdouts first pass** whenever;
  Kit holdout integration is layered on once Kit prototype slice 5 (bring +
  Kit-active protection) lands
- slice 5 (withdraw) genuinely requires Kit prototype slice 5, because the
  withdraw target counts come from the Kit's bring list
- slices 6–7 (presence strip, persistence) have no Kit dependency

## Risk Register

### 1. Carried Readability Fails At Scale

A few hundred homes render fine today. The scale story has not been tested at
the couple-thousand-identity count a heavily modded pack produces.

Mitigation:

- ship the test helper command early (see below) so every subsequent slice is
  evaluated against a realistic atlas
- measure LDLib2 `GraphView` frame cost at the target count before committing
  to per-frame screen-budget computations; cache per-home screen-budget bands
  and recompute only on camera change

### 2. Fit-Carried Camera Feels Wrong At The Extremes

If the carried set spans distant regions, the fit can zoom out below
readability. If it is a tight cluster, the fit can zoom in past the comfort
band.

Mitigation:

- clamp fit to the atlas's `Browse`/`Read` band; if the strict fit would
  require a scale outside that band, fall back to centering on the largest
  carried cluster at a readable scale
- keep this tuning behind a small service so the fallback strategy is easy to
  swap after manual testing

### 3. Storage Identity Must Be Stable Across Modded Container Types

Modded packs routinely mix vanilla chests, vanilla barrels, sophisticated
storage chests/barrels, storage drawers, and a long tail of other
item-handler blocks. A claim must refer to the same logical storage even if
the block is pistoned, upgraded (e.g., sophisticated upgrade swap), or
rebuilt. And proximity/auto-placement still need a current world anchor.

Mitigation:

- `ClaimedChest` owns a server-generated `storageId` (UUID) plus one or
  more **world anchors** (`dimension + BlockPos`) for proximity checks and
  auto-placement; the atlas never uses world position as layout geometry
- claim wraps whatever item handler the player currently has open; type is
  discovered through capability lookup (`IItemHandler` or NeoForge's
  block-entity item-handler capability), not by block-class matching
- double chests: vanilla adjacent-chest pairs claim as one storage with two
  anchors; opening either half hits proximity for the same tile
- the block entity stores the `storageId` in its NBT so piston pushes /
  sophisticated upgrades / chunk reloads preserve identity
- ender chests (deferred): if included, a single claim owns all ender
  chest blocks in the player's world as a shared storage; design doc covers
  the intent but prototype scope defaults to deferring them
- proximity is a server-side check; never trust client-provided positions
  for verb authorization

### 4. Chest Full During Deposit Loses Stacks If Handled Wrong

"Couldn't deposit" must never drop the stack on the floor, duplicate it, or
route it to an unreachable chest.

Mitigation:

- deposit is stack-by-stack: each stack either completes fully into one
  nearby linked chest with space, or stays in carried inventory untouched
- no partial multi-chest split for a single stack; no fallback to distant
  chests
- chest-full notification is a status-strip message keyed by identity, not a
  modal

### 5. Link And Presence Data Can Drift From Reality

Per-item presence ("also in: Ore Chest · 128") is recomputed from live
contents; the link graph is player-authored. These must not be conflated.

Mitigation:

- `ChestLink(islandId, chestId)` is the only stored storage relation; no
  per-item→chest records
- presence strips derive from scanning currently-claimed chests' snapshots on
  demand, never from link records; scans are server-side and rate-limited

## Slice Sequence

### Slice 0: Test Helper Command — LANDED

Goal:

- make a populated atlas reproducible so every subsequent slice is evaluable
  without hours of play

Status:

- `/slot test populate-atlas triage <count>` — samples distinct registry
  items into main inventory, overflow drops to world
- `/slot test populate-atlas homed <count> [islands] [seed]` — seeds
  `VisualHomeMap` directly with synthetic `PLAYER_PLACED` homes across a
  generated island grid; identities resolve to ghost items in the projection
- `/slot test clear` — removes islands whose ids start with
  `SyntheticHomedAtlasGenerator.SYNTHETIC_ISLAND_ID_PREFIX` and their
  assignments (inventory not touched; use vanilla `/clear`)
- `populate-chests` subcommand deferred to Slice 2 (claim RPC dependency)
- command gated on `source.hasPermission(2)` (op)
- common helpers (`IdentitySampler`, `SyntheticHomedAtlasGenerator`,
  `SyntheticHomedAtlasPlan`) live in `dev.imagio.slot.debug` with unit
  tests covering determinism, distribution, and empty-pool safety

Deliverables:

- NeoForge server command `/slot test populate-atlas <mode> <count>` gated on
  cheats / operator permission and no-op on release builds:
  - `mode=triage` inserts a spread of distinct `ItemStack`s directly into
    the player's main inventory. SLOT's triage is driven by the workspace
    projection scan over the authority snapshot on each refresh, not by
    pickup events, so direct insertion is sufficient: the next view refresh
    groups the new identities, attaches chips, and shows them in the Triage
    island. Stacks that don't fit fall back to vanilla drop so the set size
    isn't silently capped at 36.
  - `mode=homed` seeds the `VisualHomeMap` directly with synthetic
    `PLAYER_PLACED` homes across a procedurally generated set of islands
    (useful for LOD / readability testing at scale without clicking through
    thousands of chips)
- a separate subcommand `/slot test populate-chests <count>` places a row of
  claimable chests near the player and claims them with a spread of
  identity-to-chest content (useful for slice 2+ testing)
- item pool draws from the live registry; filter to at least
  `ItemStack::isEmpty == false` and a reasonable stack-size default
- command output reports what it did (counts, islands created, chests
  placed) so CI/manual runs have a reproducible transcript

Exit criteria:

- on a fresh world, `/slot test populate-atlas homed 2000` opens into an
  atlas with 2000 realistically distributed homes
- `/slot test populate-atlas triage 100` puts 100 identities into Triage as
  if the player had just gone on a long gathering run
- command paths fail closed with a useful diagnostic when preconditions
  (cheats off, not a server context) are not met

Tests:

- headless unit test for the identity-sampling logic (deterministic with a
  seed; no duplicates within a run)
- unit test for the `homed` path: seeded map has expected island count and
  home distribution given a seed

### Slice 1: Carried Readability At Scale — LANDED

Goal:

- prove the carried vs ghost visual contrast and the fit-carried camera work
  at the target identity count, independent of any storage code

Status:

- `AtlasItem.carried` + `AtlasIsland.carriedCount` on the view-model
  records; carried lanes = `PLAYER_MAIN` + `PLAYER_QUICK_ACCESS_LANE_0` +
  `PLAYER_OFFHAND`; identities in `VisualHomeMap.assignments()` that are
  not carried project as ghost atlas items
- `FitCarriedCamera` in `common/src/main/java/dev/imagio/slot/atlas/`
  with `fit()` + `fitOrFallback()` (largest-cluster fallback when the
  natural fit would fall below a readability floor). Unit-tested.
- `SlotWorkspaceUiFactory.applyInitialCamera` wires fit-carried into the
  atlas `LAYOUT_CHANGED` handler on first open; empty carried falls back
  to Triage-centered, then to `fitToChildren`
- ghost vs carried visual distinction via `cardChromeColor`
  alpha-dim on the card chrome **and** an `overlayTexture` on the icon
  itself (shader-color tint via `ItemStackTexture.setColor` was tried
  first and abandoned — it only tints flat GUI-shader items; block
  models, emissive torches, and special shaders ignore it; the overlay
  approach works uniformly)
- per-island carried-count pill rendered in the island chrome when
  `carriedCount > 0`; click pans the camera to that island via
  `FitCarriedCamera.fit`
- ghost stack resolution isolated in `GhostAtlasStackFactory` (neoforge-
  only) behind a `Function<String, ItemStack>` resolver on
  `SlotWorkspaceViewModel` so the view model stays test-loadable without
  Minecraft `Item` / `ItemLike` stubs

Departures from the original plan:

- **Atlas widgets never grow.** Design principle: LOD reveals or hides
  information; widget world-space footprint is frozen at
  `item.width() × item.height()`. The plan's "carried items clamp at a
  minimum readable screen size" would grow the card on zoom-out and
  overlap neighbors; this was removed. Every rendering path inside the
  card (shell, inner panel, icon) now clamps to the card's bounds —
  including caller-site centering math in `regionAtlasBody` /
  `browseAtlasBody`, because `AtlasRenderBudget.forScreenBudget` has
  px floors (e.g. `shellPx` min 16) that can translate back to world
  units larger than the card at low zoom if unclamped.
- **Corner state pip removed entirely** — the old `anchorStatePip`
  pipped every homed item indistinguishably (every card got an ACCENT
  dot). The proper "+N gained since last open" treatment is deferred;
  see `docs/plans/current.md` "Later Feature Tracks".

Camera band used: `CARRIED_FIT_MIN_SCALE = 0.20`,
`CARRIED_FIT_MAX_SCALE = 2.50`,
`CARRIED_FIT_READABILITY_MIN_SCALE = 1.00`. Cluster fallback window
shrinks by `0.65` to force a tight cluster selection rather than
covering most of the canvas.

Tests: `FitCarriedCameraTest` covers fit + fallback; new
`SlotWorkspaceLdlibModelTest` cases cover `carriedFlagDerivesFromAnyCarriedLane`,
`ghostItemAppearsForHomedIdentityNotInCarried`,
`islandCarriedCountEqualsCarriedHomesInThatIsland`.

### Slice 2: Claim And Storage Zone Tile — IN PROGRESS

Goal:

- land the data model and UI for claimed chests without any linking yet

Status (2026-04-17):

- common-side domain landed: `ChestAnchor`, `ClaimedChest`,
  `ClaimedChestMap`, `ChestClaimWorkflowDomainService`
  (claim / move / updateAnchors / removeAnchor / relabel / delete),
  5 `WorkflowEvent` sub-records, projection reducer, `WorkflowDomainRuntime`
  accessor, `WorkflowDomainSnapshot` accessor. Anchor collision,
  duplicate-claim rejection, and final-anchor delete cascade are
  server-authoritative.
- `common/atlas/StorageZoneAutoPlacement` pure-function placement
  landed: neighbor detection within world radius, scaled world→atlas
  delta, grid snap, spiral-bump collision avoidance, dimension
  isolation. Unit-tested.
- NeoForge-side identity machinery landed in `neoforge/storage/`:
  `SlotAttachmentTypes` registers `AttachmentType<UUID>` keyed
  `slot:storage_id` (UUID codec, persisted through BE NBT);
  `ChestStorageAnchors` gates claimability on
  `Capabilities.ItemHandler.BLOCK` with `BlockTags.SHULKER_BOXES`
  exclusion, resolves vanilla double chests into two anchors via
  `ChestBlock.TYPE` + `getConnectedDirection` with block / facing
  verification; `ChestStorageIds` wraps `hasData`/`getData`/`setData`/
  `removeData` with `setChanged()`; `ChestClaimServerService` orchestrates
  the full claim path (capability check → anchor resolve → live/orphan
  attachment handling → auto-placement → common service claim → write
  attachments to all anchor BEs); `ChestStorageBreakListener` subscribes
  `BlockEvent.BreakEvent` server-side and removes the anchor from the
  breaking player's runtime (cascades to claim delete when the last
  anchor is gone).
- persistence refactor bundled in this slice:
  `WorkflowDomainFileStore` moved from `neoforge/persistence` →
  `common/workflow/domain/persistence/` (zero platform imports).
  Claimed-chest events are not yet encoded — the encoder returns null
  for each and the encode pipeline filters nulls. Full codec +
  `ClaimedChestMap` checkpoint + load-time anchor reconciliation
  lands in Slice 7.
- single-player first: the break listener only updates the breaking
  player's runtime. Multiplayer anchor sync across players is a Slice 2
  non-goal.
- portable-chest detection limited to `BlockTags.SHULKER_BOXES`;
  modded portable-chest capability probes are deferred until we hit a
  concrete case.

Still open in Slice 2:

- claim RPC + Claim button in the SLOT chest UI (wraps any
  `IItemHandler`-exposing menu; hidden for shulker / portable chests;
  reuses `ChestClaimServerService`)
- storage-zone atlas region in the view model; chest tile renderer with
  auto-label header and drag-to-reposition (visual only; never mutates
  world state)
- `/slot test populate-chests <count>` debug helper using the same
  `ChestClaimServerService` path (no direct-to-repository injection,
  per the helper-command rules)

Deliverables:

- domain type `ClaimedChest(storageId, anchors, atlasX, atlasY, label?)`
  where `storageId` is a server-generated UUID and `anchors` is a set of
  `(dimension, blockPos)` covering all blocks that resolve to the same
  logical storage (one for a single chest, two for a vanilla double chest)
- the block entity for each anchored block stores the `storageId` in its
  NBT so piston moves and upgrades preserve identity; reads on any anchor
  block route to the same claim record
- `ClaimedChestStore` in workflow domain (in-memory; persistence in slice 7)
- a **Claim** button in the SLOT chest UI when the player opens any block
  that exposes an `IItemHandler` capability; the button is hidden for
  shulker boxes and portable chests (detected via `#c:shulker_boxes` and
  the portable-chest capability check); ender chests are deferred
- claim RPC: server creates the tile, assigns a fresh `storageId`, writes
  it into the block entity(ies) at the anchor position(s), computes
  auto-placement (`atlasX/atlasY` seeded from neighboring claimed storages
  within a fixed world radius), returns the updated view-model
- **storage zone** region in the atlas view model: a separate `AtlasRegion`
  with distinct greybox chrome (flat tint plus different grid) to the
  carried zone
- tile renders with a small header (auto-label derived from storage kind
  and position; player rename can land later in slice 7 or earlier if easy)
- drag-to-reposition on chest tiles (visual only; never mutates world state
  or contents)
- when all of a claimed storage's anchor blocks are removed, its tile is
  deleted — no "missing" state, no dismiss affordance; if the player
  accidentally destroys storage they needed, they re-claim the replacement

Exit criteria:

- opening any item-handler block (vanilla chest, vanilla barrel, modded
  sophisticated chest/barrel, storage drawers) in world shows the Claim
  button; clicking it creates a tile in the storage zone at an auto-placed
  coordinate
- claiming a second storage near the first places its tile near the first
  tile's atlas coordinate
- a vanilla double chest claims once and both halves trigger proximity for
  the same tile
- breaking all anchor blocks of a claimed storage removes its tile
- a pistoned chest keeps its claim (block entity NBT carries the
  `storageId`); re-opening it after the move still targets the same tile
- shulker boxes and portable chests do not show the Claim button

Tests:

- auto-placement: given N existing tiles and their claim positions, a new
  claim near one of them lands within a tuned offset
- double-chest claim: both halves resolve to the same `storageId`
- piston move preserves `storageId` via block-entity NBT round-trip
- tile deletion when the last anchor block is removed
- claim RPC is server-authoritative (client-provided positions and storage
  ids are ignored)

### Slice 3: Island-To-Chest Link

Goal:

- land the link data model and its proximity-gated render behavior

Deliverables:

- domain type `ChestLink(islandId, storageId)`; per-player, not persisted
  yet
- dropdown menu on chest tiles: "Link to island…" lists the player's
  non-Triage islands; "Unlink…" lists currently linked islands
- on-atlas rendering: by default no link lines are drawn
- proximity-driven render: while the player is in world within the configured
  radius of a claimed chest, that chest's tile glows and its linked islands
  highlight, and faint threads are drawn from the chest tile to each linked
  island
- threads and glow fade when proximity ends; no hover-based rendering

Exit criteria:

- linking an island to a chest via the dropdown produces a `ChestLink` record
  on the server
- walking toward a linked chest in world visibly glows its tile and draws
  thread(s) to linked islands without the player opening any menu
- walking away fades both

Tests:

- link create/remove RPC is player-scoped and idempotent
- proximity service returns the expected `storageId` set for a given
  player position (across multiple anchor blocks per storage)
- view-model link overlay is driven by the proximity set, not by hover state

### Slice 4: Deposit Verb

Goal:

- make the core "drop everything off that belongs here" flow work end to end

Two-phase implementation:

- **4a: no-Kit-holdouts first pass** — can ship whenever slice 3 is done
- **4b: Kit holdout integration** — layers on once Kit prototype slice 5
  (bring + Kit-active protection) lands

Deliverables (4a):

- deposit verb: hotkey + button in the SLOT workspace; fires only when the
  player is within proximity of one or more claimed chests
- deposit planner in common kernel:
  - iterates carried stacks
  - resolves each stack's home island
  - filters to stacks whose island has a link to a nearby claimed chest
  - destination per eligible stack is the nearest linked chest with space
  - a stack that cannot fit nearby fails for that stack with a status-strip
    diagnostic; deposit continues for the remaining stacks
- real mutations go through the existing intent router; deposit does not
  invent parallel action semantics
- greybox feedback: a short flash on the destination tile per deposited
  stack; skip the full particle trail until a later slice

Deliverables (4b, once Kits are ready):

- deposit planner subtracts Kit holdouts: if a Kit is active, `bring`
  targets remain carried at the target count, and Kit-protected identities
  are excluded from eligibility

Exit criteria:

- near a base with two linked chests, pressing the deposit hotkey moves
  eligible stacks to their destinations and leaves unlinked-island stacks
  untouched (4a); Kit-active stacks also untouched (4b)
- a full nearby linked chest produces a clear per-stack failure message
  without routing to a distant chest
- no authority state is invented client-side; every mutation is a standard
  intent router request

Tests:

- deposit planner correctness against a fixture:
  - every stack whose home has a nearby linked chest is selected (4a)
  - active Kit with bring targets → targets held, above-targets eligible (4b)
  - nearest-with-space selection when two linked chests are in range
  - full-chest produces a failure row, not a routing attempt to a distant
    chest
- architecture test: deposit planner lives in common, proximity adapter
  lives on the NeoForge side

### Slice 5: Withdraw Verb + Take All

Goal:

- round-trip the storage loop with explicit and Kit-implicit withdrawal, and
  add the chest-level Take All for manual reorganization

Deliverables:

- explicit withdraw verb (hotkey + button): for each `bring` entry in the
  active Kit, pull from nearby linked chests until carried count reaches the
  target; no-op without an active Kit
- implicit withdraw: Kit activation while within the proximity of linked
  chests auto-fires the withdraw flow; Kit activation far from base runs
  normally without withdraw
- "Take all" verb on each claimed-chest tile and in the SLOT chest UI:
  empties chest contents into carried inventory, respecting available space
- greybox feedback: short flash on each carried target home per withdrawn
  stack

Exit criteria:

- activating a Kit at base pulls missing bring items into carried; activating
  it far from base just activates
- explicit withdraw produces only the items declared in the active Kit's
  bring list
- Take All empties a chest into carried up to available space; any
  unsuccessful stack stays in the chest

Tests:

- withdraw planner against a fixture produces the expected per-identity
  target deltas
- Kit-activation integration: implicit withdraw fires only when in proximity
- Take All overflow behavior: with a near-full carried, leftover items
  remain in the chest

### Slice 6: Per-Item Chest Presence

Goal:

- surface "where else is this item stored" without adding a second
  organization axis

Deliverables:

- detail-zoom widget on an item home shows a compact "also in:" strip listing
  claimed chests currently holding the identity, with counts
- entries are clickable; clicking pans the camera to that chest tile using
  the existing camera-history path
- presence data is derived server-side from live chest snapshots of
  currently-claimed chests, rate-limited (e.g., refresh on view-model
  refresh cycle, not per frame)
- no stored per-item→chest links; presence is observation-only

Exit criteria:

- zooming in on an item home whose identity exists in one or more claimed
  chests shows the presence strip with accurate counts
- clicking a presence entry pans to that chest tile
- updating chest contents produces an updated strip within one workspace
  refresh

Tests:

- presence derivation: given claimed-chest snapshots and an identity, the
  strip lists the right chests with the right counts
- click-to-pan dispatches the same camera-history event as region-label
  clicks

### Slice 7: Persistence

Goal:

- promote the new storage state from in-memory to workflow-domain persistence

Deliverables:

- `ClaimedChest`, `ChestLink`, and chest tile atlas coordinates persist in
  the existing `WorkflowDomainSnapshot`
- migration/versioning matches the `VisualHomeMap` approach
- on world load, claimed storages whose anchor blocks no longer exist (or
  whose remaining anchor blocks no longer carry the expected `storageId`
  NBT) have their tiles deleted as part of load-time reconciliation (same
  rule as runtime block-break); anchors that still resolve are retained
- claimed chest player rename/label, if not already in slice 2, lands here

Exit criteria:

- claimed chests, their links, and their atlas positions survive client
  restart and world reload
- a claimed chest whose block was broken while the world was last closed
  loads with no tile

Tests:

- persistence round-trip for a realistic state (5 chests, 10 links across 3
  islands)
- load-time reconciliation deletes tiles for chests whose blocks are gone

## Test Helper Command (Spec Detail)

This is a development/test helper, not a shipped feature. It lives behind
the same cheats/operator gate as vanilla `/give`, and the command tree is
only registered when a `slot.debug` config flag is on (default on in dev,
off in release builds).

- `/slot test populate-atlas triage <count>` — sample `count` distinct
  registry items, spawn them as pickups near the player; identities flow
  through the normal triage path so chip/learned-rule behavior can be
  manually observed under load
- `/slot test populate-atlas homed <count> [--islands N] [--seed S]` —
  generate `N` synthetic player islands with template-default chrome and
  distribute `count` identities across them as `PLAYER_PLACED` homes; the
  seed makes runs reproducible so screenshots and bug reports are stable
- `/slot test populate-chests <count> [--radius R]` — place `count`
  claimable chests in a ring around the player, claim each one through the
  normal claim RPC path, and fill each with a deterministic identity spread
- `/slot test clear` — remove all synthetic state emitted by the helper
  (homed islands marked with a debug flag; chests placed by this command
  tagged so we do not remove real ones)

The helper must use the same domain-service entry points as normal UI paths.
It is explicitly not allowed to inject records into the persistence layer
directly — if it did, we would lose the "same code path as the player" test
guarantee.

## Testing Priorities

Highest-value near-term coverage:

- carried / ghost projection flag derivation
- fit-carried camera math and band clamping
- per-region carried badge counts
- claimed chest auto-placement heuristic
- tile deletion on world-side block removal (runtime and load-time)
- link RPC idempotency and scoping
- proximity service correctness (both for verb auth and for link render)
- deposit planner against Kit-holdout and full-chest scenarios
- withdraw planner target-delta derivation
- implicit withdraw gated on proximity
- Take All overflow behavior
- per-item presence derivation
- persistence round-trip for claimed chests and links
- architecture tests keeping LDLib2 imports out of common and inventory
  semantics out of `neoforge/` UI code

## Definition Of Done For This Prototype

This prototype is complete when:

- the carried atlas remains readable at ~2000 identities with the carried
  constellation findable at any zoom and the fit-carried camera landing
  inside the readability band
- claiming a chest produces a persistent tile in the storage zone; breaking
  the chest deletes its tile
- dropdown linking works and proximity-driven link rendering fades cleanly
  with distance
- deposit and withdraw verbs round-trip through the existing intent router
  with Kit holdouts honored and full-chest behavior graceful
- Take All empties a chest without dropping items or inventing authority
- per-item presence strips reflect live chest contents and pan the camera
  cleanly on click
- claimed chests, links, and tile positions persist across restart and
  reload
- the test helper command lets a fresh world reach a realistic atlas-and-
  storage configuration in seconds, using the same domain-service paths a
  player would exercise
- no remote authority, cross-base routing, per-chest classification, or
  world-position-driven atlas geography is introduced
- world-side wayfinding (particle trails, holograms, compass) remains
  deferred
