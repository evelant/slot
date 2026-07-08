# Storage Integration

Last updated: 2026-07-08

How SLOT abstracts storage, and how to add new storage mods without touching
executors, UI code, or the core kernel.

For the broader authority / projection / action model, see
[overview.md](overview.md).

## Why this exists

Before this layer, executors hardcoded
`{PLAYER_MAIN, PLAYER_QUICK_ACCESS_LANE_0, PLAYER_OFFHAND}` when scanning
for items. Result: deposit, take-all, kit activation, and pickup routing
were all silently blind to Sophisticated Backpacks. The same shape of bug
kept getting re-introduced every time a new carried mod was added, and
every time we added a new operation that needed to iterate carried
sources.

The fix: one SPI per storage axis (carried vs world), dispatched at the
platform edge. Adding a new storage mod is now an O(1) change — register
one provider, you're done — instead of an O(n) change across every
executor + UI path.

## The two axes

Storage lives on one of two axes:

| Axis | What it is | Examples |
| --- | --- | --- |
| **Carried** | Player-adjacent, stateful per-player | Vanilla main / hotbar / offhand; Sophisticated Backpacks; Curios slots; a hypothetical travelers-backpack mod |
| **World** | Block-bound, addressed in the world | Chests, barrels, shulkers, hoppers; TFC/TFG tool racks; TFC placed-item blocks; Storage Drawers; Tom's Storage terminals; AE2 networks (via delegate) |

Any code that needs to peek, extract, or insert goes through one of two
platform-neutral interfaces in `dev.imagio.slot.inventory.storage`:

| Axis | Interface | Install via | Platform impl |
| --- | --- | --- | --- |
| Carried | `CarriedSourceAccess` | `StorageAccessRegistry.installCarriedSourceAccess` | `NeoForgeCarriedSourceAccess` |
| World | `WorldStorageAccess` | `StorageAccessRegistry.installWorldStorageAccess` | `NeoForgeWorldStorageAccess`, `ForgeWorldStorageAccess` |

Both installed at platform init (`SlotNeoForge`). Callers retrieve via:

```java
CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
```

Item mutation remains item-only. Read-only fluid discovery is layered beside
the same axes: carried and world accessors enumerate `FluidContent` records,
and loader code adapts Forge/NeoForge `FluidStack` state through
`FluidStackAccess`. Common code sees only `SlotResourceIdentity.FLUID` plus a
long millibucket amount.

## Carried storage integration

### Extension points

- **`CarriedProvider`** — SPI for a carried storage family. Owns a stable
  prefix (e.g. `sophisticatedbackpacks:carried`, `curios:slot`) and
  answers peek / extract / insert / sourceIds / slotCount. Registered in
  `CarriedProviderRegistry` at mod init.
- **`DefaultCarriedProviderIntegration`** — auto-synthesises a
  `PlayerInventoryExtension` for every `CarriedProvider` with
  `autoSynthesizeExtension() == true`. Emits carried-pane source
  descriptors and routes `PlayerInventoryExtension.mutate()` →
  `CarriedProvider.insert` / `CarriedProvider.extract`. No caller ever
  needs to know which mod owns a source; dispatch is by prefix.

### Path 1: Minimal carried mod (Curios, generic backpack)

Implement `CarriedProvider`, register. Done.

```java
public final class CuriosCarriedProvider implements CarriedProvider {
    public String prefix() { return "curios:slot"; }
    public List<String> sourceIds(Player p) { /* enumerate curios slot ids */ }
    public ItemStack peek(Player p, String sourceId, int slotIndex) { … }
    public ItemStack extract(ServerPlayer p, String sourceId, int slotIndex,
                             int amount, boolean simulate) { … }
    public ItemStack insert(ServerPlayer p, String sourceId, ItemStack stack,
                            boolean simulate) { … }
    public int slotCount(Player p, String sourceId) { … }
}

// SlotNeoForge.java, once at mod init
CarriedProviderRegistry.register(new CuriosCarriedProvider());
```

That's it. The auto-synthesiser picks it up, which means:

- `authority.carriedSources()` sees the new sources
- `PlayerInventoryExtension.mutate()` routes INSERT / EXTRACT requests to
  `CarriedProvider.insert` / `CarriedProvider.extract`
- Every existing executor (`DepositExecutor`, `TakeAllExecutor`,
  `SlotPickupRouter`, `SlotWorkspaceUiSession`) sees the new sources
  automatically — no edits
- `CarriedSourceAccess.findIdentity` / `findAllMatching` /
  `insertBestFit` / `insertIntoProviders` all walk the new sources
  without knowing the mod exists

### Path 2: Rich carried mod (SB-class with its own openable UI)

Sophisticated Backpacks is the canonical example. It has things a Curios
slot doesn't:

- An openable backpack menu that exposes internal slots as `HOST_STORAGE`
- Tool upgrade slots (pickup / magnet / crafting panel) visible in that
  menu
- Mod-specific translated labels, custom `stableOrder`, diagnostics
- Pickup magnet / upgrade-driven behaviour that needs custom integration

For these, the minimal path isn't enough. You need:

1. **Implement `CarriedProvider`** (same as Path 1, for ops dispatch).
2. **Override `CarriedProvider.autoSynthesizeExtension()` to return
   `false`.** This stops `DefaultCarriedProviderIntegration` from
   emitting sources for your provider, avoiding duplicate registration.
3. **Implement your own `InventoryIntegrationProvider`** with:
   - `openHost()` returning a host session when your menu is open
   - `playerExtensions()` returning a `PlayerInventoryExtension` that
     emits your rich source descriptors (translated labels,
     `stableOrder`, tool artifacts, diagnostics) and routes `mutate()`
     to your mod-specific support layer
4. **Register the integration** in `SlotCommon.init()`.

The `CarriedProvider` still serves ops dispatch from
`NeoForgeCarriedSourceAccess`. The custom `InventoryIntegrationProvider`
serves snapshot metadata and host-session opening. The two live side by
side without duplicating source emission because of the opt-out.

### Source-id convention

All carried sources use the form `<prefix>/<stableId>`:

- `sophisticatedbackpacks:carried/<contentUuid>`
- `curios:slot/<slotName>-<index>`
- `my-future-mod:pocket-dim/<stableId>`

`CarriedProviderRegistry.forSource(sourceId)` routes via the first
provider whose `handles(sourceId)` returns true. The default `handles()`
implementation matches on `prefix() + "/"` — follow that convention
unless you have a reason not to.

### `Player` vs `ServerPlayer` in the SPI

Read methods (`sourceIds`, `peek`, `slotCount`, `findIdentity`,
`findAllMatching`) take `Player` (the parent type) so the same provider
serves both client (for UI host-descriptor construction) and server (for
authority reads). Mutation methods (`extract`, `insert`, `insertBestFit`)
require `ServerPlayer` because they only run authoritatively.

### Read-only fluids in carried storage

`CarriedSourceAccess.enumerateFluids` walks carried item stacks and inspects
their item fluid capabilities. This covers buckets, drums, super tanks, cells,
flasks, and provider-backed carried containers whose visible item slots contain
fluid holders. Higher-level code must not call item fluid capabilities
directly; add or fix the platform accessor/provider instead.

Fluid enumeration does not authorize fluid mutation. Commands that operate on
item identities reject fluid resource cards until a fluid planner/executor
exists.

## World-bound storage integration

### Extension points

- **`WorldStorageAccess`** — interface for `insert` / `extract` /
  `enumerate` / `slotCount` / `isAccessible` on `Target`. Insert/extract
  have actor-aware overloads; use the `ServerPlayer` forms for providers
  whose power, security, stats, or action-source semantics depend on a
  player.
- **`Target`** — sealed interface. `Chest(ClaimedChest)` covers claimed
  storage. `Display(WorldDisplayStorageKind, dimension, x, y, z)` covers
  small world item displays such as TFC/TFG tool racks and TFC placed-item
  blocks. `Virtual(providerId, storageId, routeKind, dimension, x, y, z)`
  covers routed aggregate storage such as AE2 networks.
- **`WorldStorageAccess.Delegate`** — SPI for virtual / aggregated
  storage that can't be reached via the default block-capability path.

### Path 1: Block-backed storage with `ItemHandler.BLOCK` capability

Works by default. Any block that exposes
`Capabilities.ItemHandler.BLOCK` is reachable through
`NeoForgeWorldStorageAccess`'s default capability path. This includes:

- Vanilla chests, barrels, shulker boxes, dispensers, droppers, hoppers
- Storage Drawers, Sophisticated Storage barrels, Create item vaults,
  Tom's Storage terminals, most modded containers

No integration needed.

The capability being present means "readable/reachable," not always
"currently mutable." Some blocks expose a handler while temporarily
refusing insert/extract. TFC large vessels are the reference case: sealed
vessels stay visible as tracked storage, but their simulated insert/extract
returns no movement, so bulk deposit, take, and affinity previews must treat
them as read-only until unsealed.

Small processing inventories still are not storage homes. TFC barrels have
item slots for recipes, but they are liquid/recipe stations rather than
bulk-storage targets; their slot count keeps them out of claim/affinity
eligibility unless an explicit allow tag is added deliberately.

### Read-only fluids in world storage and machines

`WorldStorageAccess.enumerateFluids` reads two surfaces:

- Fluid capabilities on item stacks stored inside the target inventory.
- Direct block/machine tank capabilities exposed by the target block.

This means a chest containing a filled drum contributes both an item count for
the drum and a fluid resource count for the contained fluid. A machine or hatch
with exposed tanks contributes fluid resources even when it has no item slots.
GregTech multiblocks are treated through the exposed fluid handlers on hatches
or tank-bearing blocks; SLOT does not infer hidden controller inventory.

Empty tanks are ignored. Inaccessible or unsupported handlers fail closed with
diagnostics at the platform edge instead of producing guessed resources.

### Path 2: Virtual / aggregated storage (AE2, Create networks)

Implement `WorldStorageAccess.Delegate` and register it.

```java
WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
world.registerDelegate(new Ae2NetworkDelegate());
```

The delegate is tried **before** the default capability path. It may expose
live `WorldDisplayStorageSource` records discovered near the player, intercept
`Target.Display` routes for small display blocks, or intercept
`Target.Virtual` routes for aggregated storage. Matching targets return
`Optional.of(result)`; non-matching targets return `Optional.empty()` to fall
through to the default capability lookup.

AE2 Forge 1.20.1 v1 is the reference virtual storage integration; ADR
[0009](../decisions/0009-ae2-persistent-network-storage.md) records the
identity decision. Nearby physical item/crafting terminal parts observe the
active ME network as `WorldDisplayStorageKind.AE2_NETWORK` when mounted
storage-cell media can be discovered. The storage id is
`ae2:network:<hash(sorted(mediaIds))>`, where each mounted cell receives a
SLOT-owned media UUID if AE2 does not expose a stable public serial. Only
mounted drive/ME-chest cells with positive stored `AEItemKey` contents enter
that media set; empty cells and non-item-only cells are observed but do not
churn the network identity. Networks with no discoverable/stampable item media fall back to live-only
`AE2_TERMINAL` display storage.

AE2 enumeration emits one logical entry per stored `AEItemKey`: the render
stack count is capped to the item's normal stack size, while
`SlotContent.count()` carries the full ME total. Craftables, autocrafting,
fluids, and other key types are not stored counts. AE2 fluid keys require a
future delegate and must not be folded into item counts or v1 fluid counts.
Insert/extract route through AE2 powered storage operations with
`IActionSource.ofPlayer(player, actionHost)`; do not mutate AE2 menu slots or
bypass AE2 power/security checks.

SLOT also keeps an AE2 media ledger keyed by the stamped media UUID. Drive and
ME-chest cells update the ledger while defining active storage identity; IO
ports update the ledger only as observers. If a cell is observed empty or
non-item-only, any remembered AE2 record that still claims that media is
retired so cell-emptying machines and cell-to-cell transfers do not leave stale
item counts behind.

Physical terminals provide a route for mutation and wayfinding. Open
item/crafting terminal screens, including wireless item/crafting terminal
menus, are claimed by a dedicated host provider exposing primary storage as
provider-backed `ae2:terminal`; the crafting grid is not primary storage.
Those open screens also refresh the same media-set storage record. If the open
route is wireless/open-only, SLOT may mutate through the current menu while it
is open, but remembered wayfinding keeps the last known physical terminal route
and does not invent a player-position route.

When a live AE2 observation has media overlap with an older remembered AE2
record under a different storage id, the old record is retired. This handles
cell moves, network splits, and network merges by preferring a temporary
undercount over double-counted storage. If the same physical route is later
proven to point at a different media set with no overlap, the older remembered
record is demoted to unreachable: its counts remain searchable, but it no
longer offers mutation or wayfinding until a live route refreshes it.

AE2 storage-bus aliasing is handled in the world-storage index. A storage bus
mounts the adjacent external inventory into AE2's grid storage service, so the
same chest/crate may be visible once as direct world storage and once through
ME contents. AE2 sources report storage-bus target blocks as aliases. If an
alias matches a loaded/readable claimed chest, SLOT reads that chest even when
it is outside normal proximity and subtracts exact counts from the ME source.
If the alias target is unreadable but remembered, SLOT subtracts remembered
counts. If the alias target is a known child AE2 network, SLOT subtracts that
child network once. Ambiguous display aliases fail diagnostic-first instead of
guessing. If the alias is unknown, SLOT leaves those counts in ME because
there is no separate storage record to subtract.

### Known gap: claimed non-chest storage

`Display` targets are tracked for SLOT display and live nearby mutation, but
they are not claimed-storage homes with player labels, chest clusters, or
affinity learning. If you want drawers, barrels, AE2 network interfaces, or
display blocks to become **claimable SLOT homes** rather than tracked display
targets, generalise the claim model (`ClaimedChestMap`,
`ChestStorageBreakListener`, `ChestProximityResolver`, and related naming) as a
single slice rather than retrofitting piecemeal.

## Architectural invariants (do not violate)

These are the patterns that create the "executor-is-blind-to-storage-X"
bug class. If you catch yourself writing any of these, stop and use the
abstraction instead.

1. **Never hardcode a source-id scan.** If you find yourself writing
   `if (sourceId.equals(PLAYER_MAIN) || sourceId.equals(PLAYER_QUICK_ACCESS_LANE_0) …)`,
   you're re-introducing the backpack-blind bug. Use
   `CarriedSourceAccess.findIdentity` / `findAllMatching`, or iterate
   `authority.carriedSources()`.
2. **Never call capabilities directly from higher-level code.**
   `level.getCapability(Capabilities.ItemHandler.BLOCK, …)` is banned
   outside `NeoForgeWorldStorageAccess`.
   `stack.getCapability(Capabilities.ItemHandler.ITEM)` is banned
   outside `CarriedProvider` implementations.
   Fluid handler capabilities follow the same rule: platform accessors own
   enumeration, common code consumes fluid-content records.
3. **Never name a specific provider in routing code.**
   `SophisticatedBackpackTransferSupport` and friends must not be
   imported by executors, UI sessions, or the router. The provider set
   is open-ended by design.
4. **Never mutate `player.getInventory()` directly from UI or executor
   code.** Route through `CarriedSourceAccess.extract` /
   `insertBestFit` / `insertIntoProviders`. Direct vanilla `Inventory`
   mutation is reserved for `NeoForgeCarriedSourceAccess` itself.
5. **Never re-implement transfer logic per-caller.** If you're writing
   a loop that does "find → peek → extract → insert → put-back-on-failure,"
   that's a symptom. Use the `InventoryActionExecutor` action pipeline
   for policy-gated transfers, or `CarriedSourceAccess` +
   `WorldStorageAccess` for workflow-level ops. See the layer guide
   below.

## When to use each API (layered, not redundant)

Three parallel mutation entry points exist; they are layered for
different callers:

| API | When to use | Example callers |
| --- | --- | --- |
| `InventoryActionExecutor` | Full action pipeline: policy, conflict detection, projection planning, canonicalization | UI-triggered `ASSIGN` / `TRANSFER` verbs |
| `InventoryMutationRouter` | Dispatch an already-resolved `InventoryMutationRequest` to the owning extension / host session | Inside `InventoryActionExecutor` |
| `CarriedSourceAccess` / `WorldStorageAccess` | Direct workflow-level ops with no policy or canonicalization | `DepositExecutor`, `TakeAllExecutor`, `SlotPickupRouter`, `SlotWorkspaceUiSession` |

Rule of thumb:

- If you have an `InventoryMutationRequest` in hand already, use the
  router.
- If you just want to peek / extract / insert a specific slot on a
  specific source, use the direct APIs.
- If you have a user gesture that needs full policy, canonicalization,
  and multi-source planning, emit an `InventoryActionRequest` through
  the executor.

For the read side, two paths also coexist by design:

| Read API | Granularity | Used by |
| --- | --- | --- |
| `InventoryAuthoritySnapshot` (via `InventoryAuthorityReadService`) | Bulk snapshot of every source for UI projection | Workspace / browse layer |
| `CarriedSourceAccess.peek` / `findIdentity` | Point queries against a single slot or identity | Executors, pickup routing |

Both are correct. Use the snapshot when you're building UI state; use
`peek` / `findIdentity` when you want a single answer.

## Checklist: adding a new carried mod

- [ ] Implement `CarriedProvider` with `prefix()`, `sourceIds(Player)`,
      `peek`, `extract`, `insert`, `slotCount`
- [ ] Source ids follow `<prefix>/<stableId>` convention
- [ ] Register once in `SlotNeoForge` (or similar platform init):
      `CarriedProviderRegistry.register(new YourProvider())`
- [ ] If the mod has an openable UI, tool upgrades, or custom labels:
      override `autoSynthesizeExtension()` → `false` and ship a hand-
      written `InventoryIntegrationProvider`
- [ ] Do **not** edit `NeoForgeCarriedSourceAccess`, `DepositExecutor`,
      `TakeAllExecutor`, `SlotPickupRouter`, or any UI session file
- [ ] Smoke test: verify deposit, take-all, pickup routing, and kit
      activation all see the new sources (no code changes should be
      needed in those flows)

## Checklist: adding a new world-block storage

- [ ] Exposes `Capabilities.ItemHandler.BLOCK`? → nothing to do, works
      by default.
- [ ] Virtual / aggregated (AE2 network, Create routing)? → implement
      `WorldStorageAccess.Delegate`, register via
      `world.registerDelegate(...)`
- [ ] Needs a first-class non-chest target (claimed drawer, claimed
      barrel)? → see "Known gap" above; requires `Target` variant +
      claim-model generalisation

## See also

- [overview.md](overview.md) — authority / projection / action model
- [action-taxonomy.md](action-taxonomy.md) — verb + quantity + scope
  grammar
- [../../AGENTS.md](../../AGENTS.md) — terse traps for common mistakes
