# Workspace Performance Plan

Last updated: 2026-07-01

Status: implementation landed on 2026-07-01; TerraFirmaGreg manual/profile
validation remains pending. This roadmap was written after Forge 1.20.1 spark
captures of mining pickup and bulk carried-to-storage deposit flows in
TerraFirmaGreg.

Follow-up: [workspace-incremental-projection.md](workspace-incremental-projection.md)
tracks the larger architecture step of replacing whole-model refreshes with
typed invalidations, projection slices, identity/storage-local facts, and delta
view sends.

For the current operational baseline, see [../status.md](../status.md). This
plan expands the workspace projection follow-up in [current.md](current.md).

## Context

The latest profiles show SLOT doing noticeable work after each storage-heavy
interaction. The hot path is:

1. Forge receives a workspace action.
2. `SlotForgeNetworking.sendViewToPlayer(...)` projects the view.
3. `ForgeWorkspaceSession.project(...)` builds a full projection request.
4. `WorkspaceProjectionSessionCache.project(...)` hashes inputs and may project.
5. `SlotWorkspaceViewModel.project(...)` builds wall items, storage ghosts,
   wayfinding, chips, hotbar state, workflow state, and diagnostics.
6. Forge hashes / encodes / sends the full view model.

The profile also points at repeated `ItemIdentityMatcher.create(...)` calls,
selector parsing, wayfinding matching, storage index construction, content
fingerprinting, NBT encoding, hotbar recency, and carried activity tracking.

## Goals

- Reduce the 100-400 ms interaction stalls seen in large carried / tracked
  storage states.
- Keep the projection path closer to a pure read of current state.
- Reuse exact identity and stack fingerprints instead of recomputing them in
  every projection sub-pass.
- Split expensive work into independently cached or explicitly requested
  slices.
- Preserve correctness for stateful items: batteries, fluid containers,
  backpacks, damageable/custom tools, and other NBT/component-heavy stacks.

## Non-Goals

- Do not simplify identity to `itemId` as a performance shortcut.
- Do not remove exact stack `dataFingerprint` from content invalidation.
- Do not authorize mutation from remembered or remote storage data.
- Do not add loader-specific fast paths that bypass common inventory semantics.
- Do not change the player-visible storage model while optimizing it.

## Key Files

| File | Role |
| --- | --- |
| [forge-1.20/.../SlotForgeNetworking.java](../../forge-1.20/src/main/java/dev/imagio/slot/forge/network/SlotForgeNetworking.java) | Forge action handling, projection send, view message encode boundary |
| [forge-1.20/.../ForgeWorkspaceSession.java](../../forge-1.20/src/main/java/dev/imagio/slot/forge/network/ForgeWorkspaceSession.java) | Forge projection request assembly, auto-home reproject, storage routing context |
| [neoforge/.../SlotWorkspaceUiSession.java](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceUiSession.java) | NeoForge equivalent projection/session path |
| [common/.../WorkspaceProjectionSessionCache.java](../../common/src/main/java/dev/imagio/slot/inventory/workspace/WorkspaceProjectionSessionCache.java) | Structural projection cache, memo lifetime, content fingerprint comparison |
| [common/.../WorkspaceProjectionFingerprint.java](../../common/src/main/java/dev/imagio/slot/inventory/workspace/WorkspaceProjectionFingerprint.java) | Input/content hashing; keeps exact stack data fingerprints |
| [common/.../SlotWorkspaceViewModel.java](../../common/src/main/java/dev/imagio/slot/inventory/workspace/SlotWorkspaceViewModel.java) | Main projection builder: wall items, ghosts, chips, wayfinding, hotbar/workflow slices |
| [common/.../WorkspaceStorageIndex.java](../../common/src/main/java/dev/imagio/slot/inventory/workspace/WorkspaceStorageIndex.java) | Claimed/display storage index, remembered contents, live storage probing |
| [common/.../ItemIdentityMatcher.java](../../common/src/main/java/dev/imagio/slot/inventory/core/ItemIdentityMatcher.java) | Shared item identity and movable normalization contract |
| [common/.../CarriedIdentityCounts.java](../../common/src/main/java/dev/imagio/slot/inventory/query/CarriedIdentityCounts.java) | Current one-shot carried identity count helper |
| [common/.../InventoryAuthorityDiffClassifier.java](../../common/src/main/java/dev/imagio/slot/inventory/session/InventoryAuthorityDiffClassifier.java) | Activity diffing currently rebuilds identity counts |
| [common/.../HotbarSlotRecencyTracker.java](../../common/src/main/java/dev/imagio/slot/inventory/workspace/HotbarSlotRecencyTracker.java) | Hotbar observation currently fingerprints display stacks separately |

## Identity Correctness Contract

Every optimization in this plan must preserve the existing identity semantics:

- GregTech battery charge / mode churn is identity noise for movable matching
  and Recents.
- GregTech fluid containers preserve fluid type (`FluidName`, `stored`) while
  ignoring amount/fill churn (`Amount`, `storedAmount`).
- Portable-container normalization must run after stable semantic selectors
  are extracted, not before.
- `SlotStackAccess.current().dataFingerprint(stack)` remains the exact
  cross-loader stack-state fingerprint for content invalidation.
- New caches may memoize exact current matcher results; they must not invent
  weaker keys.

Regression tests that must stay in the gate:

```bash
./gradlew :common:test --tests dev.imagio.slot.inventory.core.ItemIdentityTest
./gradlew :common:test --tests dev.imagio.slot.inventory.session.CarriedAcquisitionActivityTrackerTest
```

## Slice 0 - Phase Timings (landed 2026-07-01)

Add lightweight timing around the server refresh path before changing behavior.

Measure:

- authority read
- projection request setup
- storage routing / index build
- projection input key
- actual projection miss work
- content key
- Forge / NeoForge encode
- network send scheduling

Log only when explicit view logging is enabled or a refresh exceeds a threshold.
Include counts that explain scale: carried entries, atlas items, triage items,
storage entries, tracked display entries, wayfinding targets, chest chips,
content summaries, memo hits/misses, and payload size where available.

Exit criteria:

- A storage-heavy deposit logs one compact line per slow refresh.
- The log distinguishes storage index time from view-model projection time.
- No behavior or serialization changes.

## Slice 1 - Shared Per-Refresh Identity Snapshot (landed 2026-07-01)

Introduce a common `AuthorityIdentityIndex` or `ProjectionIdentityContext`
built once per authority snapshot under the existing `ItemIdentityMatcher.Memo`.

It should expose:

- exact identity per source/slot entry
- normalized movable identity per source/slot entry
- carried identity counts
- carried free slot / capacity counts
- display stack by exact/canonical identity
- optional hotbar identity/fingerprint facts

Use it first in `SlotWorkspaceViewModel.project(...)` to replace repeated
scans in carried counts, grouped atlas entries, recent/junk display lookup,
content summaries where the source is authority-backed, and hotbar recency.
Wanted clearing and activity diffing remain candidates for a later pass when
their callers already have the same authority snapshot.

Exit criteria:

- Projection builds carried identities once for the common view path.
- Existing battery/fluid identity tests pass unchanged.
- The timing log shows fewer identity misses and less selector parsing during
  repeated deposits.

## Slice 2 - Wayfinding Matching Index (landed 2026-07-01)

`wayfindingTargetForStorage(...)` currently checks every stored identity against
every missing kit / desired / wanted identity. Late-game tracked storage makes
this expensive.

Plan:

- Normalize the missing need set once.
- Prefer `ChestContentsSnapshot.countsByIdentity()` over slot-by-slot stack
  walks whenever present.
- Match storage identities through canonical lookup first.
- Fall back to `ItemIdentityMatcher.matchesMovable(...)` only for ambiguous
  shapes that canonical lookup cannot prove.
- Return the same source-specific target sets (`kit`, `desired`, `wanted`) as
  today.

Exit criteria:

- `WayfindingTargetTest` passes unchanged or gains explicit coverage for
  fluid-container and battery-equivalent identities.
- Storage-heavy profiles no longer show wayfinding as a top projection cost.

## Slice 3 - Storage Routing / Index Cache (landed 2026-07-01)

Build a session-level cache for read-only storage index pieces.

Separate the cache into layers:

- remembered / non-proximate storage keyed by claimed-storage topology,
  storage roles, and `WorkspaceStorageMemoryStore.revision()`
- live display-source snapshots keyed by display storage id plus a structural
  contents/fingerprint key
- proximate live chest snapshots keyed by proximate storage set and a short
  invalidation bucket
- deposit eligibility overlay keyed by carried identity summary, because
  `canInsertAnyCarried(...)` depends on current carried contents

Rules:

- Live mutation executors still read and mutate through `WorldStorageAccess` /
  `CarriedSourceAccess`; cached storage summaries are only projection input.
- Remembered contents may support display/search/wayfinding, never authority.
- Live reads may still update `WorkspaceStorageMemoryStore`, with disk flush
  kept off the hot interaction path.

Exit criteria:

- Repeated deposits into the same nearby storage do not rebuild the full storage
  index when topology and carried identity summary are unchanged.
- Storage memory revision changes invalidate remembered entries correctly.
- Quick-deposit eligibility changes when carried contents change.

## Slice 4 - Remote / Tracked Ghost Gating (landed 2026-07-01)

`ElsewhereGhostProjection.build(...)` currently runs unconditionally because
tracked-storage x-ray and search need remote-only identities already present in
the view model. That is useful, but expensive in large storage systems.

Preferred approach:

1. Add explicit projection intent for remote storage detail:
   `NONE`, `INTENT_ONLY`, `SEARCH`, `TRACKED_XRAY`.
2. In normal collapsed wall mode, include remote ghosts only for active kit,
   desired, wanted, recent, junk, or put-away needs.
3. When search or tracked x-ray is active, request the broader remote index and
   synthesize visible remote cards.
4. Keep a compact remote count/index if the UI needs distant-count badges
   without full `AtlasItem` payloads.

Exit criteria:

- Default deposits do not synthesize all remote-only cards.
- Search-as-find and tracked x-ray still reveal remote identities after an
  explicit reproject.
- Tests cover collapsed hidden remote ghosts, search reveal, tracked x-ray
  reveal, wanted/desired remote ghosts, and fluid-container distinctness.

## Slice 5 - Projection Fingerprint And Send Slices (landed 2026-07-01)

The current content key hashes the full view model and Forge sends the full NBT
model whenever the revision advances. Keep exact stack fingerprints, but split
the model into independently fingerprinted slices:

- frame/status/diagnostics
- wall structure and cards
- storage chips and wayfinding
- hotbar/offhand/Belt
- workflow/craft-run/task panel
- active chest / loot panel
- contextual lanes

Short-term implementation can reuse the last encoded tag per unchanged slice.
Longer-term implementation can send slice deltas with per-slice revisions.

Exit criteria:

- A hotbar selection or small status change does not force re-encoding the wall
  and storage slices.
- Any stack data change still invalidates the slice containing that stack.
- Forge and NeoForge codecs stay symmetric.

## Slice 6 - Memo Boundaries And Eviction (landed 2026-07-01)

`ItemIdentityMatcher.Memo` now evicts the eldest cached identity when a cache
reaches 4096 entries instead of clearing the whole cache. Large modpacks can
still churn through the bound, so the slow-refresh logs include cache sizes and
eviction counts.

Rules for future memo work:

- avoid global cross-tick identity caches unless the key contains exact item id,
  exact data fingerprint, stackability, damageability, and tag inputs

Exit criteria:

- Memo churn is visible in diagnostics.
- Any eviction change is covered by existing identity tests.
- No long-lived cache can return a stale identity for changed stack data.

## Verification

Focused tests:

```bash
./gradlew :common:test --tests dev.imagio.slot.inventory.core.ItemIdentityTest
./gradlew :common:test --tests dev.imagio.slot.inventory.session.CarriedAcquisitionActivityTrackerTest
./gradlew :common:test --tests dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModelDepositTest
./gradlew :common:test --tests dev.imagio.slot.inventory.workspace.WorkspaceProjectionSessionCacheTest
./gradlew :common:test --tests dev.imagio.slot.inventory.workspace.WorkspaceStorageIndexTest
./gradlew :common:test --tests dev.imagio.slot.inventory.workspace.WayfindingTargetTest
./gradlew :common:test --tests dev.imagio.slot.inventory.workspace.WallSectionVisibilityTest
./gradlew :common:test --tests dev.imagio.slot.inventory.workspace.WallSectionItemSorterTest
```

Broad gates:

```bash
./gradlew :common:test
./gradlew :forge-1.20:compileJava :forge-1.20:compileSharedProbeJava :neoforge:compileJava
```

Manual/profile checks:

- Repeat the bulk carried-to-external deposit profile in TerraFirmaGreg.
- Compare slow-refresh phase logs before/after each slice.
- Test charged and discharged GregTech batteries.
- Test empty, water-filled, lava-filled, and partially-filled GregTech drums /
  super tanks.
- Test damageable/custom tools with changing state.
- Test Sophisticated Backpacks and any other provider-backed carried storage.
- Test search, tracked x-ray, wanted/desired, Recents, junk, wayfinding, and
  quick-deposit in a large tracked-storage area.

Manual/profile status: pending after the 2026-07-01 implementation pass.
