# Storage Areas — Implementation Plan (SUPERSEDED 2026-04-30)

Last updated: 2026-04-30

> **Superseded by the learned-storage swap.** See
> [learned-storage.md](learned-storage.md) and the recap in
> [current.md](current.md). The design below — explicit player-named
> storage areas with chip-collapse-by-default and expand-on-proximity
> — is **not the plan**. What actually shipped:
>
> - **Implicit chest claiming.** First deposit auto-claims; no
>   explicit area assignment, no claim button.
> - **Storage areas as derived clusters.** `ChestClusterMap` groups
>   chests by 16-block spatial proximity (union-find); cluster
>   ordinals are stable, default labels read "Storage Area N", and
>   right-click → context menu on a chip cluster header surfaces
>   rename.
> - **Chest contents on the atlas as ghost cards.** Items in proximate
>   chests render as faded ghost atlas cards on their homed island.
>   Non-proximate stocks surface via the chest-locator panel (search)
>   and the `+N stored` corner badge that paints on carried + ghost
>   cards under search.
> - **Time-based + explicit forgetting.** Affinity decays
>   ~1 point per in-game day; right-click chip → `Forget chest`
>   (undoable through the existing undo stack). No separate
>   "demote area" gesture needed.
> - **No proximity-based expand/collapse of areas.** Cluster headers
>   render in the chip stack only when a cluster has > 1 chip; chest
>   tile rendering retired entirely (chips replaced tiles).
>
> The text below is preserved as historical context for the design
> exploration. **Don't implement it.** If you want chest-content
> detail or remote-chest visibility, see the loot-chest panel + the
> search-time `+N` badge wiring in
> [learned-storage.md](learned-storage.md).

## Why (historical context only — do not implement)

The current storage prototype renders every claimed chest as a tile in
one shared storage zone. Bases form *visually* (chests dragged near
each other look like a base) but there's no data type backing the
grouping — no name, no chip-collapse-when-far, no "Where is iron?"
answer in base-units. At scale (multiple physical bases — Main, Mountain
Mine, Oil Derrick, Warehouse) the storage zone becomes a flat sprawl of
40+ chest tiles competing with the carried atlas for camera real
estate.

[design/relevance-lod.md § Storage areas](../design/relevance-lod.md#storage-areas)
sketches the fix: a **storage area** is a player-named container of
chest tiles. Areas default to **chip size** on the atlas; they
**expand** when any of their chests is in proximity, when search /
shopping-list resolution touches them, or when the player pins them.
Inside an expanded area, individual chest tiles still follow the
relevance/LOD model — chests in proximity render full live contents,
others stay compact. Same machinery as island chips, recursively
applied.

This plan converts that design into landable code. It assumes the
existing storage prototype (slices 0–7) stays mostly intact — claims,
links, deposit, take-all, presence, and persistence keep their shapes.
**Areas are a containment layer above chest tiles**, not a replacement
for any existing concept.

For background:

- [design/relevance-lod.md § Storage areas](../design/relevance-lod.md#storage-areas)
  — the model and the area-as-relevance-recipient framing.
- [design/storage.md § Atlas Topology For Storage](../design/storage.md#atlas-topology-for-storage)
  — the direction note that flagged this rework.
- [storage-prototype.md](storage-prototype.md) — the existing claim /
  link / deposit / withdraw / persistence slices that areas extend.
- [relevance-lod-prototype.md § Deferred](relevance-lod-prototype.md#deferred-write-down-so-we-dont-forget)
  — the explicit "re-evaluate StorageArea after Phase 2 playtest" hold
  this plan releases.

## Sequence

1. **Phase 1** — Domain type + migration. Every claim gets an
   `areaId`; existing claims migrate into a single default "Main Base"
   area on world load.
2. **Phase 2** — Claim-flow change. New claims auto-pick the
   current-proximity area; UI offers an area picker plus inline
   "create new area" affordance.
3. **Phase 3** — Atlas chip rendering. Areas render as compact chips
   by default; expand on proximity / pin / search.
4. **Phase 4** — Relevance contributors. `area_proximity` and
   `chest_holds_relevant` join the score; carried-relevance badges on
   area chips light up when something inside is wanted.
5. **Phase 5** — Per-area presence + verb-routing fallout. Presence
   strips read "in: Main Base · 128 · Mountain Mine · 32" instead of
   per-chest; deposit/take-all/withdraw paths all respect area
   membership.

Phase 1 is the load-bearing slice — once areas exist as a domain type
with a default migration, every other phase has somewhere to attach.
3, 4, and 5 are independently evaluable on top of that.

---

## Phase 1 — Domain type + migration

**Goal.** Land `StorageArea` as a workflow-domain record with a
projection map, an "every claim has an area" invariant, and a one-shot
migration that drops existing claims into a default "Main Base" area.

**Files.**

- New: `common/.../workflow/domain/StorageArea.java` — record.
- New: `common/.../workflow/domain/StorageAreaMap.java` — projection
  map (mirrors `ClaimedChestMap` shape).
- New: `common/.../workflow/domain/StorageAreaWorkflowDomainService.java`
  — create / rename / recolor / move / delete.
- Extend: `common/.../workflow/domain/ClaimedChest.java` — gain
  `areaId: UUID` field.
- Extend: `WorkflowEvent.java` — five new sub-records: `StorageAreaCreated /
  Renamed / Recolored / Moved / Deleted`, plus `ClaimedChestAreaChanged`
  (the chest-moves-between-areas event).
- Extend: `WorkflowProjection.java` — reducer for the new events;
  `Snapshot` exposes a `storageAreaMap()` accessor.
- Extend: `WorkflowDomainFileStore.java` — codec for the events +
  snapshot.
- Extend: `ChestClaimWorkflowDomainService.claim*` to require an
  `areaId` argument.

**Concrete changes.**

1. `StorageArea` shape:
   ```java
   public record StorageArea(
       UUID areaId,
       String label,           // player-set; default "Main Base"
       int color,              // ARGB; default neutral grey, recolor later
       int atlasX,              // chip top-left in atlas world coords
       int atlasY,
       int displayOrder        // sort hint; new areas append
   )
   ```
2. `ClaimedChest.areaId` is non-null. Default-area UUID
   (`StorageAreaMap.DEFAULT_AREA_ID = ...`) is a stable known constant
   so migration + load-time reconciliation can refer to it without
   round-tripping through projection state.
3. **Migration.** A claim record loaded from disk without an `areaId`
   field gets the default `Main Base` UUID. The first projection pass
   on a save that lacks `StorageAreaMap` synthesises a default area
   record with that UUID. Schema is additive — old saves load with
   every chest in `Main Base`.
4. **Service operations.**
   - `createArea(label, atlasX, atlasY) → StorageArea`
   - `renameArea(areaId, label)`
   - `recolorArea(areaId, color)`
   - `moveArea(areaId, atlasX, atlasY)`
   - `deleteArea(areaId)` — refuses if any chests still reference the
     area; UI must reassign or delete chests first.
   - `moveChestToArea(storageId, areaId)` (lives on
     `ChestClaimWorkflowDomainService` since it mutates the chest).
5. The default `Main Base` area is **not deletable** — the projection
   re-creates it if the event log says delete. Cheaper than threading a
   "no zero-area state" invariant through every consumer; a single
   deletable default + a cascade-delete-the-chests gate is the
   alternative we explicitly *don't* take (deletion bombs are bad UX).

**Acceptance.**

- A fresh world has exactly one storage area: `Main Base`,
  auto-created on first claim (no claims = no area visible yet, fine).
- An old save with N claimed chests loads with N chests all in
  `Main Base`; the area's `atlasX/atlasY` defaults to a deterministic
  position derived from the claims' bounding box.
- `ChestClaimWorkflowDomainService.claim(...)` without an `areaId`
  is a compile error; callers pass `StorageAreaMap.DEFAULT_AREA_ID`
  during migration and a real area id afterwards.
- Persistence round-trips an area + a chest reassigned away from
  `Main Base` + an area rename.

**Estimated size.** ~400 LOC across domain types + projection +
codec + service + migration. The biggest chunk is wiring the new
events through `WorkflowDomainFileStore` (encode + decode + dispatcher)
mirroring how `ChestLink*` events are handled today.

---

## Phase 2 — Claim-flow change

**Goal.** When a player claims a chest, the new tile lands in a
sensible area without forcing a picker dialog. Make manual area choice
available but optional.

**Files.**

- `neoforge/.../storage/ChestClaimServerService.java` — claim
  routing.
- `neoforge/.../screen/ldlib/ClaimChestServerHandler` (or wherever
  `SlotChestClaimPayload` resolves) — accepts an optional `areaId`
  field on the payload.
- `neoforge/.../screen/ldlib/...` claim-button popover (TBD location)
  — area picker UI.
- `common/.../atlas/StorageZoneAutoPlacement.java` — extend to
  return an `areaId` along with the placement; see below.

**Concrete changes.**

1. **Default area inference.** When a claim arrives without an
   explicit `areaId`:
   - If the player is currently within world-proximity of any
     chest already in some area, use that area.
   - Else fall back to `Main Base`.
   - This is a server-side decision; the client never picks the area.
2. **Manual override.** The Claim button popover (currently a single
   button) grows to:
   - "Claim into [Main Base ▼]" — dropdown lists existing areas, with
     a "+ New area…" entry that opens a one-line input.
   - The dropdown defaults to the proximity-inferred area so a
     single-click claim still works in the common case.
3. **Auto-placement** stays inside the chosen area. The atlas-placement
   heuristic in `StorageZoneAutoPlacement` becomes
   *area-relative* — neighbors are looked up among chests in the same
   area, not across the whole storage zone. New areas placed somewhere
   outside the existing storage-zone bounding box (offset right by
   "area chip width + gap") so the chip doesn't overlap.
4. **Move chest between areas.** The chest tile context menu (right-
   click on tile) gains a "Move to area…" entry that fires
   `moveChestToArea` with a chosen target. Drag-to-area is *not* part
   of this phase — Phase 3 needs the area chrome to land first.

**Acceptance.**

- Claiming a chest within 8 blocks of an existing claim assigns the
  new claim to that claim's area; the new tile lands near the
  existing tile.
- Claiming the first chest of a fresh save creates `Main Base` (if
  not already created via migration) and lands the tile inside it.
- The "+ New area" picker entry takes a label, creates the area,
  and routes the claim into it. Cancelling the input falls through
  to the default area without claiming.
- The chest tile right-click menu shows a "Move to area…" submenu
  that lists all areas and a "+ New area" entry; selecting one
  emits `moveChestToArea` and the tile re-renders in the new area's
  layout.

**Estimated size.** ~200 LOC client + server. The area-aware
auto-placement is the trickiest piece; the popover changes are
straightforward menu wiring.

---

## Phase 3 — Atlas chip rendering

**Goal.** Areas render as compact chips by default; expand on
proximity, search, or pin. Inside an expanded area, the existing
chest-tile geometry is unchanged.

**Files.**

- `neoforge/.../screen/ldlib/StorageAreaPanelBuilder.java` (new) —
  area chip + expanded-area chrome.
- `neoforge/.../screen/ldlib/AtlasPanelBuilder.java` — replace the
  current per-tile loop in the storage zone with an
  area-grouped loop.
- `common/.../atlas/lod/...` — areas join the relevance/LOD model;
  see Phase 4 for the contributors. This phase ships the rendering
  side and uses a simple "area is expanded" boolean for now.
- `common/.../inventory/workspace/SlotWorkspaceViewModel.java` — gain
  `List<StorageAreaSnapshot>` (one per area) carrying its own
  `chestTiles`, replacing the flat `List<ClaimedChestTile>`.

**Concrete changes.**

1. `SlotWorkspaceViewModel.StorageAreaSnapshot`:
   ```java
   public record StorageAreaSnapshot(
       UUID areaId,
       String label,
       int color,
       int atlasX, int atlasY,
       int chestCount,
       int totalSlotCount,
       boolean proximate,                  // any chest in proximity
       boolean expanded,                   // explicit pin or search-pin
       List<ClaimedChestTile> chestTiles   // existing tile shape
   )
   ```
2. **Chip rendering.** A compact pill (~120×24 atlas world units)
   showing label + chest count + a small carried-relevance badge
   (Phase 4 wires the badge meaningfully). A click pins the area to
   `expanded = true`; a click on a pinned area un-pins it.
3. **Expansion.** When `proximate || expanded`, the chip is replaced
   with the existing chest-tile cluster — chests sized and laid out
   exactly as today. The chip stays as a header banner above the
   cluster so the player sees the area boundary.
4. **Layout.** Areas pack inside the storage zone the same way
   islands pack inside the carried zone: the existing
   `WeightedGridPacker` runs on the area-snapshot list, with each
   area's effective width/height derived from its current state
   (chip size when collapsed, full-cluster size when expanded).
   Non-trivial — the chest-tile cluster's footprint already has its
   own packing pass; we wrap that result.
5. **Drag-to-area.** Drag a chest tile out of its current cluster
   and drop on another area's chip → `moveChestToArea`. Drop on the
   storage zone outside any area → no-op (areas own all chests).

**Acceptance.**

- A storage zone with 4 areas (Main Base 12 chests, Mountain Mine 6,
  Oil Derrick 3, Warehouse 1) at default zoom shows 4 chips taking
  ~40% of the atlas's previous storage-zone footprint.
- Walking up to one of Main Base's chests in world expands Main Base
  to the full cluster; the other three areas stay collapsed.
- Clicking a collapsed chip pins it expanded; clicking again un-pins.
- Dragging a chest tile from Main Base onto Mountain Mine's chip
  moves the chest to Mountain Mine and re-lays out both areas.

**Estimated size.** ~400 LOC client (chip + expanded-area chrome,
panel-builder rewrite, drag handling) + ~80 LOC view-model snapshot
changes. The area-of-the-storage-zone packing pass is the load-bearing
piece; rest is straightforward UI.

---

## Phase 4 — Relevance contributors

**Goal.** Areas join the relevance system with two new contributors
so the chip → expanded transition is driven by intent (proximity,
shopping-list, search), not just explicit pin.

**Files.**

- New: `common/.../atlas/lod/contributors/AreaProximityContributor.java`
- New: `common/.../atlas/lod/contributors/ChestHoldsRelevantContributor.java`
- `common/.../atlas/lod/RelevanceContext.java` — add
  `proximateAreaIds: Set<UUID>` and `relevantStorageIds: Set<UUID>`
  fields.
- `common/.../atlas/lod/AtlasRelevance.java` — wire the new
  contributors into the default contributor list.
- `neoforge/.../screen/ldlib/RelevanceContextProvider` (or wherever
  the context is built) — populate the two new fields from the live
  proximity service + active kit / search state.

**Concrete changes.**

1. **`area_proximity`** — score=high when an item's identity has a
   home island linked to a chest in a currently-proximate area. Same
   shape as the existing `proximate-storage` boost but at area
   granularity, so an item homed to "Tools" boosts whenever ANY
   chest in any area linked to "Tools" is in proximity. This makes
   items pop in the carried zone as the player walks toward base.
2. **`chest_holds_relevant`** — score=medium-high when a chest
   contains a kit-missing or shopping-list item. Boosts the chest
   *within its area*, even when not in proximity, so search/gather
   flows light up off-base storage. This is how the area chip's
   "carried-relevance badge" gets its number — a chip glows when its
   area contains any chest-holds-relevant chest.
3. **Area-chip badge.** With Phase 4's contributors live, the badge
   shows the count of relevant items across the area's chests
   (e.g., "Main Base · 12 ⚡3" — 3 kit-missing items found inside).
   Clicking the badge pins the area and pans to the first matching
   chest.

**Acceptance.**

- Walking up to a chest in Mountain Mine boosts every island linked
  to a Mountain Mine chest; the carried zone visibly emphasises
  those islands' contents at any zoom.
- A kit with `bring: iron_pickaxe` near full-but-storable chests
  lights up the relevant area's chip badge with a "1" count.
- Pinning an area mid-search jumps the camera to that area without
  reflowing the rest.

**Estimated size.** ~150 LOC across contributors + context wiring.

---

## Phase 5 — Per-area presence + verb routing

**Goal.** Every storage UX surface that today reads "in: Chest A · 128
· Chest B · 64" reads in **area** units instead. Verbs (deposit,
take-all, withdraw) all gate on area membership.

**Files.**

- `common/.../inventory/workspace/SlotWorkspaceViewModel.java` —
  `ChestPresenceEntry` becomes `AreaPresenceEntry(areaId, label,
  count, ...)` (subsume per-chest counts inside).
- `neoforge/.../screen/ldlib/AtlasCardBuilder.java` — render the
  area-keyed presence strip.
- `common/.../inventory/workspace/SlotWorkspaceCommandService.java` —
  deposit/withdraw planners filter candidates by area.
- `neoforge/.../storage/DepositExecutor.java` — gate on
  area-of-link.
- `neoforge/.../storage/TakeAllExecutor.java` — keep working at
  chest-level; no behavioural change, just rename diagnostics to
  carry the area name.

**Concrete changes.**

1. **Presence strip.** Today's atlas card detail view reads "in:
   Chest A · 128 · Chest B · 64" sourced from `ChestPresenceEntry`.
   Change to "in: Main Base · 192" (sum of both chests within Main
   Base), with hover/expand showing the per-chest breakdown.
   Multi-area cases stay multi-row: "in: Main Base · 128 · Mountain
   Mine · 64" — area-keyed first, chest detail under expansion.
2. **Deposit.** `DepositPlanner` already iterates carried stacks and
   resolves home → linked chest; extend to track *area* of each
   candidate. With Phase 4's `proximateAreaIds`, the planner accepts
   only candidates whose area is currently proximate. The
   per-stack "nearest chest with space within proximate areas" rule
   replaces today's "nearest chest with space" — same semantics,
   just gated by area.
3. **Withdraw.** Same change: bring-list resolution finds chests via
   area first, then chest within area.
4. **Take-all.** No semantic change — Take All on a chest tile still
   empties that chest. Diagnostics get the area name for clarity:
   "took_all 27 stacks from Main Base / Ore Chest" instead of just
   "took_all 27 stacks from Ore Chest".

**Acceptance.**

- An item homed to "Tools" with linked chests in Main Base (24
  total) and Mountain Mine (8 total) renders "in: Main Base · 24 ·
  Mountain Mine · 8" in the detail card.
- Pressing deposit while only Main Base is proximate moves only the
  Main-Base-eligible stacks; Mountain-Mine-eligible stacks stay
  carried.
- Activating a kit at Mountain Mine pulls bring items from Mountain
  Mine chests only, not Main Base.

**Estimated size.** ~200 LOC across view-model presence rework +
planner gates + diagnostics.

---

## Out of scope (for this plan)

- **Auto-merge nearby areas.** If the player puts two areas' chests
  spatially close in the atlas, we don't auto-merge them. Areas are
  explicit by design.
- **Sub-areas.** No "Mountain Mine / Top Floor" hierarchy. Single
  flat list of areas. Add later if real bases prove deep enough.
- **Cross-area routing.** Deposit/withdraw still never routes across
  bases; area membership tightens the proximity rule, doesn't
  loosen it.
- **Area icons / colors / themes.** Color is in the data model from
  Phase 1; recolor and icon UI come later if visual differentiation
  is genuinely needed beyond labels.
- **Multiplayer area sharing.** Areas are per-player; same model as
  islands and links. Multiplayer area sync is a separate problem.

## Test discipline

Per phase:

- **Phase 1** —
  `StorageAreaWorkflowDomainServiceTest`: create / rename / move /
  delete + persistence round-trip; `ChestClaim*Test` extends with
  area assignment + reassign.
- **Phase 2** — `ChestClaimServerServiceTest`: proximity-area
  inference picks the correct area, falls back to Main Base when no
  neighbor proximate.
- **Phase 3** — view-model snapshot tests: areas pack into the
  storage zone deterministically; expanded area swaps to chest
  cluster geometry; drag-to-area routes through `moveChestToArea`.
- **Phase 4** — `AreaProximityContributorTest` and
  `ChestHoldsRelevantContributorTest`: score derivation against
  hand-crafted contexts; integration-level test that area-chip
  badges count kit-missing items correctly.
- **Phase 5** — `DepositPlannerTest` extended with area-membership
  fixtures; presence strip renders area-keyed entries when the same
  identity exists across multiple areas.

Every phase ships its own tests; existing tests in
[storage-prototype.md § Testing Priorities](storage-prototype.md#testing-priorities)
keep passing — nothing in the existing claim / link / deposit /
withdraw / persistence behavior should break.

## Notes for the next session

- Start with Phase 1. The migration step ("every existing claim
  belongs to Main Base") is what makes every later phase non-
  breaking. Get that right and the rest is layered, evaluable work.
- The default-area-undeletable invariant is load-bearing — code that
  assumes "every chest has a non-null area" is much simpler than
  threading a "what if there are no areas?" branch through every
  consumer.
- Phase 3's chip ↔ expanded transition reuses the
  `WeightedGridPacker` infrastructure that the carried zone already
  uses for islands. It's worth re-reading
  [common/.../atlas/lod/AtlasLayout.java](../../common/src/main/java/dev/imagio/slot/atlas/lod/AtlasLayout.java)
  before laying it out — there's a chance the same pass can run on
  storage areas with a different contributor list.
- Phase 4's contributors are named in
  [relevance-lod-prototype.md § Out of scope this phase (still)](relevance-lod-prototype.md#out-of-scope-this-phase-still)
  as deferred-pending-StorageArea. This plan releases that hold.
- Phase 5's area-keyed presence is the most visible UX change —
  worth a quick playtest pass after it lands to confirm the "in:
  Main Base · 128" framing reads as the player expects. If the
  per-chest detail under expansion feels redundant, drop it.
