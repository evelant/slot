# Storage Integration

Last updated: 2026-04-23

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
| **World** | Block-bound, addressed in the world | Chests, barrels, shulkers, hoppers; Storage Drawers; Tom's Storage terminals; AE2 networks (via delegate) |

Any code that needs to peek, extract, or insert goes through one of two
platform-neutral interfaces in `dev.imagio.slot.inventory.storage`:

| Axis | Interface | Install via | Platform impl |
| --- | --- | --- | --- |
| Carried | `CarriedSourceAccess` | `StorageAccessRegistry.installCarriedSourceAccess` | `NeoForgeCarriedSourceAccess` |
| World | `WorldStorageAccess` | `StorageAccessRegistry.installWorldStorageAccess` | `NeoForgeWorldStorageAccess` |

Both installed at platform init (`SlotNeoForge`). Callers retrieve via:

```java
CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
```

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

## World-bound storage integration

### Extension points

- **`WorldStorageAccess`** — interface for `insert` / `extract` /
  `enumerate` / `slotCount` / `isAccessible` on `Target`.
- **`Target`** — sealed interface. Currently only `Chest(ClaimedChest)`.
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

### Path 2: Virtual / aggregated storage (AE2, Create networks)

Implement `WorldStorageAccess.Delegate` and register it.

```java
WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
world.registerDelegate(new Ae2NetworkDelegate());
```

The delegate is tried **before** the default capability path, so it can
intercept `Target.Chest` targets that are attached to an ME network,
route the operation through the network, and return `Optional.of(result)`.
Non-matching targets return `Optional.empty()` to fall through to the
default capability lookup.

### Known gap: non-chest `Target` variants

`Target` is currently sealed with only one variant: `Chest`. If you want
to claim drawers, barrels, or AE2 network interfaces as **first-class
SLOT storage** (beyond just "a block with `ItemHandler.BLOCK`"), you need
to add a new `Target` variant — that's a larger refactor involving
`ClaimedChestMap`, `ChestStorageBreakListener`, `ChestProximityResolver`
(all chest-specific today). This is intentional YAGNI territory until we
ship a non-chest claimed-storage feature. When the time comes, generalise
the claim model (rename `ClaimedChest` → `ClaimedStorage`, expand the
`Target` sum type, generalise the break / proximity listeners) as a
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
