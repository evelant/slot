# Storage Prototype Plan (RETIRED 2026-04-30)

Last updated: 2026-04-30

> **Retired** — the chest-link / chest-tile / storage-zone /
> island-to-chest-link model that this prototype was built on was
> wholesale replaced by the **learned-storage** swap on 2026-04-30:
>
> - **Auto-claim on first deposit** replaced the `Claim button`.
> - **Chest chips in a left-column flex panel** replaced the
>   "storage zone" + chest-tile-with-content-grid surface.
> - **`ChestClusterMap`** (16-block spatial union-find) replaced
>   `StorageArea` and the link-based grouping.
> - **Affinity-based deposit routing** (`ChestAffinityMap`) replaced
>   `ChestLink` / island-to-chest-link entirely.
> - **Ghost atlas cards on homed islands** for proximate chest
>   contents, plus a **chest locator panel** + **`+N stored` corner
>   badge under search** for remote stocks, replaced the per-chest
>   live-content grid + per-item presence strip.
> - **Affinity decay** (~1 point per in-game day) + an **explicit
>   `Forget chest`** gesture replaced the "release link" gesture.
>
> The remaining open work this prototype's Slices 4b / 5 covered
> conceptually — Kit-holdout deposit (don't deposit Kit-bring items
> while a Kit is active) and an explicit withdraw verb — needs
> re-planning against the new chip / affinity model. Filed as
> residual polish in [../current.md](../current.md).
>
> Canonical design: [../learned-storage.md](../learned-storage.md).
> Text below preserved as the historical engineering record — **do
> not implement.**

For the broader near-term engineering sequence, see [../current.md](../current.md). For the
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

- Superseded by the unified `/slot test populate <profile>` command
  (profiles: `starter | organized | late-modpack`). Each profile
  generates a single cohesive world-state in one shot: semantic-bucket
  islands, homes, triage leftovers, and claimed+linked chests. See the
  `status.md` "Realistic populate seeder landed" note for the shape
  of the generator and the classifier used.
- `/slot test clear` — full-reset: deletes all player-created islands
  and assignments, deletes all claimed chests (breaking their blocks
  in loaded chunks), and empties player main/hotbar/offhand/armor.
  Backpack items stay in place but their contents are emptied via
  the `Capabilities.ItemHandler.ITEM` capability. The docked Triage
  panel re-populates with the now-unhomed carried items.
- command gated on `source.hasPermission(2)` (op)
- generator + classifier live in `dev.imagio.slot.debug`
  (`RealisticAtlasGenerator`, `FacetIndexTemplateClassifier`,
  `PopulateProfile`) and group items by `IslandSuggestionTemplate`
  via `FacetIndex`; unit tests cover island layout, chest
  template-match ratio, triage fraction, and empty-pool safety. The
  pre-FacetIndex `SemanticBucket(Resolver)` + `SubBucket*` path was
  retired 2026-04-26 once template-driven classification covered its
  use cases.

Deliverables (historical — see Status above for the landed shape):

- NeoForge server command `/slot test populate <profile>` gated on
  operator permission, where `<profile>` is `starter | organized |
  late-modpack`. Each profile produces islands + homes + triage stacks
  + claimed+linked chests in one call.
- item pool draws from the live registry; filter to at least
  `ItemStack::isEmpty == false` and a reasonable stack-size default
- command output reports what it did (islands, assignments, triage
  added, chests placed/claimed/linked/skipped) so CI/manual runs have
  a reproducible transcript

Exit criteria:

- on a fresh world, `/slot test populate late-modpack` opens into an
  atlas with up to ~800 identities distributed across semantic-bucket
  islands (Materials ends up largest, consistent with late-modpack
  shape), a handful of triage leftovers in inventory, and ~16 claimed
  chests auto-linked to matching bucket islands
- `/slot test populate starter` puts ~30 identities into a small atlas
  with 1 chest, simulating a fresh playthrough
- command paths fail closed with a useful diagnostic when preconditions
  (not a server context, unknown profile) are not met

Tests:

- unit tests for the bucket-driven generator: one island per
  non-empty bucket, assignments fit inside island bounds, triage
  fraction matches profile, linked chests mostly contain bucket-
  matched items (≥60% under RNG variance), island rects do not
  overlap
- resolver accuracy is validated empirically in-game rather than
  unit-tested; if systematic misclassifications bother us, add a
  hardcoded override map rather than JSON infrastructure

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

### Slice 2: Claim And Storage Zone Tile — LANDED

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
- claim RPC landed: `SlotChestClaimPayload(dimension, pos)` registered
  `playToServer`; `SlotChestClaimPayloadHandler` re-resolves the
  dimension, enforces a server-side reach check, and routes through
  `ChestClaimServerService.claim`.
- Claim button landed: `ChestClaimButtonController` captures the last
  `PlayerInteractEvent.RightClickBlock` on the client within a 1.5s
  window, then on `ScreenEvent.Init.Post` for any non-shulker
  `AbstractContainerScreen` (excluding `InventoryScreen`) injects a
  "Claim" button that emits `SlotChestClaimPayload`.
- view-model + rendering landed: `SlotWorkspaceViewModel.ClaimedChestTile`
  (storage id, dimension, atlas coords, label with auto-fallback
  `Chest #xxxx`, anchor count) + codec roundtrip; projection derives
  tiles directly from `ClaimedChestMap` on every refresh.
  `SlotWorkspaceUiFactory.storageZoneBackdrop` paints a translucent
  region behind the tile cluster; `chestTilePanel` draws each tile
  (label header + kind subtitle); drag-to-reposition routes through
  `moveChestEmitter` → `SlotWorkspaceUiSession.moveChest` →
  `SlotWorkspaceCommandService.moveChest` →
  `ChestClaimWorkflowDomainService.moveChest`.
- chest placement landed inside this slice as a standalone
  `populate-chests` subcommand at the time. The chest-placement
  behaviour now lives inside the unified `/slot test populate
  <profile>` command (same `ChestClaimServerService.claim` routing;
  contents are bucket-biased and auto-linked to the matching island)
  — see "Test Helper Command" at the bottom of this doc for the
  current shape.
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

### Slice 3: Island-To-Chest Link + Chest Contents Grid

Goal:

- land the link data model and its proximity-gated render behavior
- render the chest tile body as a live grid of the chest's current
  contents, with the same proximity gating that drives link visibility

Deliverables:

- domain type `ChestLink(islandId, storageId)`; per-player, not persisted
  yet
- dropdown menu on chest tiles: "Link to island…" lists the player's
  islands; "Unlink…" lists currently linked islands
- server-side contents snapshot reader: walks a claim's anchors, reads
  the live `IItemHandler` snapshot from the first resolvable anchor,
  **filters empty slots out at the source** so only filled stacks are
  transported; falls back to an empty list when no anchor resolves
  (chunk unloaded, BE missing); cached per view-model refresh, not per
  frame
- server-side proximity check: same-dimension squared-distance from
  `ServerPlayer.blockPosition()` to any of a claim's anchors, within a
  configured radius (default 8 blocks); produces a `Set<storageId>`
  consumed by projection
- view-model extension: `ClaimedChestTile` carries `slotCount` (the
  authoritative container capacity, for future features), `contents`
  (the filled stacks only, not padded to capacity), and a `proximate`
  flag; codec round-trips them
- chest tile rendering: the tile body is a 9-column grid of item cells
  (reuse `ItemStackTexture`); tile height auto-fits `ceil(filled / 9)`
  rows; no per-chest cap, so a Sophisticated barrel with 500 filled
  stacks renders as a ~56-row-tall tile the player can zoom to
- proximity gating: when `proximate == false`, apply the same
  carried-vs-ghost visual vocabulary (alpha-dim card chrome + icon
  overlay) to the tile + cells; when proximate, full brightness and a
  subtle glow on the tile border
- on-atlas rendering: by default no link lines are drawn
- proximity-driven link render: while the player is near a claimed
  chest, that chest's linked islands highlight and faint threads render
  from the chest tile to each linked island
- threads and glow fade when proximity ends; no hover-based rendering

Exit criteria:

- claiming a chest near the player shows its live contents in the tile
  grid immediately; walking away dims the tile, walking back reactivates
  it (no workspace reopen required within the refresh cadence)
- a Sophisticated Storage chest with a partial fill renders only the
  filled stacks, not the empty capacity — empty slots are stripped at
  the reader before they ever reach the view model
- linking an island to a chest via the dropdown produces a `ChestLink`
  record on the server
- walking toward a linked chest in world visibly draws thread(s) to
  linked islands and highlights them without the player opening any menu
- walking away fades both

Tests:

- link create/remove RPC is player-scoped and idempotent
- proximity service returns the expected `storageId` set for a given
  player position (across multiple anchor blocks per storage)
- chest contents reader returns the expected slot snapshot for a loaded
  BE, empty list for an unresolvable anchor
- view-model tile `proximate` flag and contents list are driven by the
  server resolvers, not by client state
- grid height computed from `contents.size()` (filled-stack count),
  not `slotCount` (capacity); empty-slot filtering happens at the
  reader, not the renderer

### Slice 4: Deposit Verb

Goal:

- make the core "drop everything off that belongs here" flow work end to end

Two-phase implementation:

- **4a: no-Kit-holdouts first pass — LANDED**
- **4b: Kit holdout integration** — layers on once Kit prototype slice 5
  (bring + Kit-active protection) lands

Deliverables (4a, landed):

- deposit verb: button in the SLOT workspace header (proximity-gated;
  disabled when no claimed chest is in range); hotkey TBD
- `DepositPlanner` in `common/inventory/workspace/`:
  - iterates carried stacks (main + hotbar + offhand)
  - resolves each stack's home island via `VisualHomeMap`
  - filters to stacks whose island has a link to a proximate claimed chest
    (via `ChestLinkMap` + the server-provided `proximateStorageIds`)
  - returns a `DepositPlan` of `Assignment(laneId, slotIndex, itemId,
    candidateStorageIds)` entries
- `DepositExecutor` in `neoforge/storage/`:
  - for each assignment, iterates candidate chests in order, resolves the
    `IItemHandler` capability on the first loaded anchor, simulates a full
    stack insertion via `ItemHandlerHelper.insertItemStacked(handler,
    stack, true)`, and only commits if the stack fits *whole*
  - on fit: inserts to chest, clears the player's slot
  - on no-fit for any candidate: records failure, leaves the stack
    untouched (no partial splits, no fallback to distant chests)
- session status-bar diagnostics: `deposited / deposited_partial /
  rejected / nothing_to_deposit` with counts
- greybox feedback: destination-tile flash is deferred to a later polish
  pass (status-bar counts are enough to validate the flow)

Prototype departures from the original plan:

- mutations bypass the intent router and write directly to the player
  inventory + chest handler on the server thread (atomic, no client
  authority). The intent router's action taxonomy (`InventoryActionTarget`)
  doesn't model external-block `IItemHandler` targets; rather than invent
  a new target kind for slice 4a, the executor writes the split directly.
  When Kit slice 5 lands we'll decide whether to route through the intent
  router or keep the direct path.

Deliverables (4b, once Kits are ready):

- deposit planner subtracts Kit holdouts: if a Kit is active, `bring`
  targets remain carried at the target count, and Kit-protected identities
  are excluded from eligibility

Exit criteria:

- near a base with two linked chests, pressing the deposit button moves
  eligible stacks to their destinations and leaves unlinked-island stacks
  untouched (4a); Kit-active stacks also untouched (4b)
- a full nearby linked chest leaves the affected stack untouched and
  surfaces `deposit_failed=N` in the status bar rather than routing to a
  distant chest
- authority stays server-side: client sends only a zero-arg `deposit`
  RPC; the server resolves proximity, the plan, and the mutation

Tests:

- `DepositPlannerTest` (common) covers: empty proximate set → empty plan;
  no-home / triage-home / no-link cases ignored; proximate linked chest
  assigned; multi-candidate ordering; main + hotbar + offhand coverage
- 4b: active Kit with bring targets → targets held, above-targets
  eligible (once Kit slice 5 lands)
- future: full-chest behavior (currently validated via the executor's
  `insertItemStacked` simulate step but not yet covered by an automated
  test — needs a server-integration or IItemHandler stub fixture)

### Slice 5: Withdraw Verb + Take All

Goal:

- round-trip the storage loop with explicit and Kit-implicit withdrawal, and
  add the chest-level Take All for manual reorganization

Take All landed (standalone, no Kit dep):

- `TakeAllExecutor` in `neoforge/storage/`: resolves the first loaded
  anchor's `IItemHandler`, extracts each non-empty slot, pushes through
  `player.getInventory().add(stack)`, re-inserts any remainder back into
  the chest slot; anything that still doesn't fit is dropped at the
  player's feet via `player.drop(...)` so stacks are never silently lost
- "Take" button added to each chest tile header, enabled only when the
  tile is both proximate and non-empty; zero-arg RPC routes through
  `SlotWorkspaceUiSession.takeAllFromChest(storageId)`
- session status bar surfaces `took_all / took_all_partial /
  nothing_to_take / rejected (chest_not_proximate)`
- chest-UI "Take All" button (alongside the Claim button in the vanilla
  chest screen) deferred to a later polish pass

Withdraw verbs (explicit + Kit-implicit) still waiting on Kit
prototype slice 5 for bring-list targets; listed below for completeness:

- explicit withdraw verb (hotkey + button): for each `bring` entry in the
  active Kit, pull from nearby linked chests until carried count reaches the
  target; no-op without an active Kit
- implicit withdraw: Kit activation while within the proximity of linked
  chests auto-fires the withdraw flow; Kit activation far from base runs
  normally without withdraw
- greybox feedback: short flash on each carried target home per withdrawn
  stack

Exit criteria:

- Take All empties a nearby claimed chest into carried up to available
  space; any unsuccessful stack stays in the chest (or drops as a safety
  fallback if the chest re-insert also fails) ✅
- activating a Kit at base pulls missing bring items into carried;
  activating it far from base just activates (deferred)
- explicit withdraw produces only the items declared in the active Kit's
  bring list (deferred)

Tests:

- withdraw planner against a fixture produces the expected per-identity
  target deltas (deferred)
- Kit-activation integration: implicit withdraw fires only when in
  proximity (deferred)
- Take All overflow behavior needs a server-integration or
  `IItemHandler` stub fixture; currently validated by manual playtest

### Slice 6: Per-Item Chest Presence — LANDED

Goal:

- surface "where else is this item stored" without adding a second
  organization axis

Status (landed):

- `SlotWorkspaceViewModel.ChestPresenceEntry(storageId, label, count)`
  record added alongside the view model; `AtlasItem` gained a
  `List<ChestPresenceEntry> presence` field (codec-roundtripped, defaults
  to empty)
- presence is computed in `SlotWorkspaceViewModel.project(...)` from the
  tile contents the session already resolves: iterate tiles, bucket each
  stack by `ItemIdentity.itemId()` + `storageId`, sum counts, sort
  per-identity by descending count; attach the list to every AtlasItem
  that matches an identity key. Observation-only — no stored per-item
  links
- UI: `detailAtlasBody` renders a single-line "in: &lt;label&gt; ·
  &lt;count&gt; · …" strip (up to 3 entries + "+N" overflow) using the
  same `anchorTextBand` vocabulary as the secondary/auxiliary rows;
  strip inherits `ACCENT` color so it reads as a navigation hint
- click-to-pan on the strip routes to `panToChestTile`, which reuses
  `FitCarriedCamera.fit` with the same band / padding as the existing
  `panToIsland`; `event.stopPropagation()` keeps the atlas-card select
  path intact
- names feed the strip: chest tiles auto-label on first link to an
  island via `ChestLinkWorkflowDomainService`, and the Link popover now
  exposes a rename input at the top (`relabelChestEmitter` →
  `SlotWorkspaceCommandService.relabelChest` →
  `ChestClaimWorkflowDomainService.relabelChest`)

Exit criteria:

- zooming in on an item home whose identity exists in one or more
  claimed chests shows the "in: …" strip with accurate counts ✅
- clicking the strip pans the camera to the first-ranked chest tile ✅
- updating chest contents produces an updated strip within one
  workspace refresh ✅ (presence derives from the live
  `ChestContentsReader` snapshot, which already refreshes per view
  cycle)

Tests:

- current coverage relies on the view-model's existing snapshot round
  trip + the tile contents path; a dedicated
  `presenceByItemId` unit test is a good next addition but not yet
  written. Deferred for a targeted fixture that builds a
  `ClaimedChestMap` + contents + carries and asserts entries

### Slice 7: Persistence — LANDED

Goal:

- promote the new storage state from in-memory to workflow-domain persistence

Status (landed):

- `WorkflowCheckpointData` gained `claimedChests` + `chestLinks` arrays
  with `ClaimedChestData` / `ChestAnchorData` / `ChestLinkData` records;
  checkpoint encode/decode round-trips every chest claim (storage id,
  anchors, atlas coords, label) and every `ChestLink(islandId,
  storageId)`
- the seven previously null-stubbed workflow events —
  `ClaimedChestCreated / Moved / AnchorsChanged / Relabeled / Deleted`
  and `ChestLinkCreated / Removed` — now encode and decode via the
  existing event-log format, sharing `WorkflowEventData` fields
  (`storageId`, `anchors`, `claimedChest`, existing `x/y/label`)
- schema is additive; old save files (which lack these arrays) load
  with empty chests + links, so there's no migration step required
- neoforge-side `ChestPersistenceReconciliation.reconcile(server,
  runtime)` runs right after `persistence.loadInto(...)` in
  `SlotPlayerWorkflowRuntimeService.createRuntime`. Policy: "unknown ≠
  broken" — anchors in unloaded chunks are kept as-is; only anchors
  whose BE is loaded and either missing or carrying a mismatched
  `slot:storage_id` attachment get pruned. When every surviving anchor
  of a claim is known-broken, the claim is deleted (cascades to links
  via the projection reducer)
- player-authored chest label already landed in Slice 2 via
  `ChestClaimWorkflowDomainService.relabelChest`; persistence here just
  round-trips it

Exit criteria:

- claimed chests, their links, and their atlas positions survive client
  restart and world reload ✅
- a claimed chest whose loaded-anchor block lost its `storage_id`
  attachment while the world was last closed loads with no tile ✅
- anchors in unloaded chunks are preserved across load (not pruned on
  "unknown" state) ✅

Tests:

- `WorkflowDomainFileStoreTest.fileStoreRoundTripsClaimedChestsAndChestLinks`
  covers a realistic mixed state: a double-anchor claim, a renamed +
  moved single-anchor claim, a deleted claim, and two cross-island
  links; asserts checkpoint round-trip + snapshot equality
- future: integration test for reconciliation requires a server-level
  fixture; not yet wired (current logic is covered by the design-check
  "unknown ≠ broken" pass through the in-game claim/break flows)

## Test Helper Command (Spec Detail)

This is a development/test helper, not a shipped feature. It lives behind
the same cheats/operator gate as vanilla `/give`, and the command tree is
only registered when a `slot.debug` config flag is on (default on in dev,
off in release builds).

- `/slot test populate <starter|organized|late-modpack>` — in one call,
  generates a cohesive atlas state for the chosen profile: islands
  sized by semantic bucket (TOOLS, COMBAT, ARMOR, FOOD, MATERIALS,
  BUILDING, NATURAL, DECORATION, REDSTONE, STORAGE, MISC), homes
  assigned to each bucket island, triage leftovers inserted into the
  player's inventory, and claimed chests placed in a ring around the
  player with contents biased toward a matching bucket and auto-linked
  to that bucket's island. Non-deterministic; each run is fresh.
- `/slot test clear` — full-reset the player's atlas for a fresh
  slate: deletes all player-created islands and home assignments
  (Triage survives as the built-in), deletes all claimed chests and
  their links, physically breaks the underlying chest blocks in the
  world (loaded anchors only — unloaded anchors are counted and get
  reconciled on next server start), and empties the player's main,
  hotbar, offhand, and armor slots. Stacks whose item id contains
  "backpack" (case-insensitive) are kept in place, but their
  contents are emptied via the `Capabilities.ItemHandler.ITEM`
  capability (works with Sophisticated Backpacks / Simply Backpacks
  / anything that exposes its inventory via the standard NeoForge
  item handler capability).

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
