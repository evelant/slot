# Dedup chest-routing logic

Three cleanups + one real bug surfaced when consolidating
`smartDepositLeftover` and the deposit-button RPC under
[`DepositPlanner.rankChestsForIdentity`](../../common/src/main/java/dev/imagio/slot/inventory/workspace/DepositPlanner.java).
The same pattern shows up in several other places where someone wrote a
new entry point and rebuilt routing logic inline instead of calling
through. Fixing now while the shape is still fresh.

## 1. Bug — `claimChestAtPos` doesn't pair-fold double chests

[`SlotWorkspaceUiSession.claimChestAtPos`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceUiSession.java)
(active-chest-strip "Claim" button) calls
`runtime.chestClaimWorkflow().autoClaimByAnchor(anchor, …)` directly.
That method only handles a single anchor — it does **not** fold the
partner half of a double chest into the same claim, and doesn't stamp
the BE `slot:storage_id` attachment on the partner.

The correct path that does both is
[`ChestDepositObserver.resolveOrCreateClaim`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/storage/ChestDepositObserver.java)
— public exactly so other claim-on-demand callers can reuse it.

**Fix.** Replace the `autoClaimByAnchor` + `ChestStorageIds.write` pair
in `claimChestAtPos` with a single `ChestDepositObserver.resolveOrCreateClaim`
call. After the call, the active-chest panel's next refresh resolves
the (newly-paired) claim and the strip flips to its claimed state.

**Test.** Place a vanilla double chest, deposit-populate one half via
debug, walk to the other half (unclaimed), open it, hit Claim. Both
halves should resolve to one storage UUID.

## 2. Take-by-identity affinity ranking duplicated 4×

`{ rank proximate chests by affinity score; walk; call
TakeAllExecutor.takeByIdentity }` appears verbatim in:

- [`KitGatherService.execute`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/storage/KitGatherService.java) — top-level Gather button when an active kit needs items.
- [`SlotWorkspaceUiSession.pickupToCursor`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceUiSession.java) — cursor smart-pull from chests when carry/backpack don't have the identity.
- [`SlotWorkspaceUiSession.reapplyActiveKitFromCarry`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceUiSession.java) ~L1742 — kit auto-fetch loop.
- [`SlotWorkspaceUiSession`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceUiSession.java) ~L2554 — `take-one-by-identity` / `take-stack-by-identity` RPCs.

All four are pure affinity sort, no facet or presence tier.

**Fix.** Add a shared helper alongside the deposit ranker. Two
sensible options:

- **(a)** Sibling on `DepositPlanner` —
  `rankProximateChestsForTake(identity, claims, affinity, proximate) → List<ClaimedChest>`.
  Affinity-only, returns ClaimedChest (not UUID) since every caller
  needs the chest object to pass to `TakeAllExecutor`.
- **(b)** New common-side class `ChestRoutingRanker` that owns both
  `forDeposit` (direct affinity only) and `forTake` (affinity-only).
  Cleaner home for "chest routing decisions" but bigger move.

Lean toward **(a)** for the smaller diff. The deposit ranker already
returns UUIDs; the take ranker can return ClaimedChest directly, both
on the same class.

**Note for take semantics.** Affinity-only is the current behavior.
Presence-first take ranking may still become useful, but it is not part
of the deposit rule. Any future presence-aware take work should add its
own explicit contents lookup instead of reintroducing presence as a
deposit signal.

## 3. `resolveProximateLinkedChestForIdentity` is a third deposit-ranker

[`SlotWorkspaceUiSession.resolveProximateLinkedChestForIdentity`](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceUiSession.java)
~L2733 picks a single chest for the per-card "deposit home to linked
chest" / "deposit one home to linked chest" actions. Has its own
affinity sort + simulated-insert capacity filter. Falls back to "any
proximate chest with capacity" when affinity is empty — a homegrown
mini-version of what `DepositPlanner.rankChestsForIdentity` now owns.

**Fix.** Call
`DepositPlanner.rankChestsForIdentity(identity, claims, affinity,
proximate)` and walk the returned UUIDs in order, returning the first
whose capacity simulation passes. The capacity filter (simulated insert
returns empty leftover) stays local to this function since the planner
doesn't do simulation. Do not add a fallback to similar contents,
presence, or the emptiest chest.

**Test.** Per-card "deposit home to linked chest" should now also
honor direct learned affinity only — same routing as the deposit
button.

## Out of scope for this pass

- Migrating `KitGatherService` to common. It uses
  `TakeAllExecutor` (neoforge-side, needs `ServerPlayer` +
  `WorldStorageAccess`) so the executor stays neoforge. Only the
  ranking helper moves.
- Auditing `recordDeposit` call sites. Many exist — `ChestDepositObserver`,
  `smartDepositLeftover`, deposit RPC executor, populate command, etc. —
  but each fires after a specific successful deposit, not as a
  duplicated decision. Not the same kind of duplication.

## Verification

```bash
./gradlew :common:compileJava :neoforge:compileJava
./gradlew :common:test :neoforge:test
```

Manual checks:

- Claim a double chest from the active-chest strip → both halves
  share one storage UUID (read each half's `slot:storage_id` BE
  attachment with `/data get block ...`).
- Try to deposit something into a chest where another player put
  compatible items but the local player never has → it stays carried
  until the player teaches exact affinity. Deposit button,
  cursor smart-deposit, and per-card "deposit home to linked chest"
  all behave the same.
- Active kit gather pulls from the highest-affinity chest first
  (existing behavior, just verifying no regression).
