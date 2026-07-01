# Incremental Workspace Projection Plan

Last updated: 2026-07-01

Status: proposed follow-up to [workspace-performance.md](workspace-performance.md).
The first performance pass added timing, per-refresh identity reuse, storage
index caching, remote-detail gating, and encoded-slice reuse. This plan goes
one layer deeper: make projection itself incremental so ordinary inventory
changes update only the few facts they actually touch.

## Context

The current workspace refresh path is still shaped as one large derivation:

1. The loader reads live authority and builds a `WorkspaceProjectionRequest`.
2. `WorkspaceProjectionSessionCache` hashes the whole request.
3. On a structural miss, `SlotWorkspaceViewModel.project(...)` rebuilds the
   whole wall, ghosts, chips, wayfinding, hotbar, workflow panel, and
   contextual lanes.
4. The loader encodes the resulting view model, reusing unchanged encoded
   slices where possible.

That helped encoding, but it does not prevent most projection work. Taking a
stack of wood, losing a hammer, or changing one tracked chest still makes the
server re-evaluate many unrelated identities and storages. The better model is
to maintain a read-side projection store whose atoms are keyed by identity,
storage id, section id, and slice. Full projection remains the oracle and
fallback, but normal refreshes become local invalidations plus small deltas.

## Goals

- Make ordinary workspace refresh cost proportional to the changed identities
  and storages, not total carried + tracked storage size.
- Preserve the exact authority/mutation contract: cached projection facts never
  authorize mutation.
- Preserve the identity correctness contract from
  [workspace-performance.md](workspace-performance.md): exact stack data
  fingerprints for invalidation, movable identity for grouping, and special
  handling for charge/fluid/container semantics.
- Keep Forge and NeoForge behavior equivalent by putting projection facts and
  invalidation rules in `common/`.
- Keep a full-snapshot path for session open, gap recovery, tests, and any
  ambiguous invalidation.

## Non-Goals

- Do not weaken identity to `itemId`.
- Do not mutate storage from remembered or remote facts.
- Do not make loader UI code own inventory semantics.
- Do not require every external mod to emit perfect storage change events
  before we can improve common cases.
- Do not delete the full projection path until incremental/full parity is
  proven by tests and live diagnostics.

## Current Constraints

- `WorkspaceProjectionSessionCache` has one structural key. It can reuse the
  previous full structural view or rebuild all of it; it cannot update one
  card, one storage chip, or one section.
- `SlotWorkspaceViewModel.project(...)` mixes fact gathering, card derivation,
  section ordering, ghost synthesis, workflow pressure, storage chips,
  wayfinding, and contextual lanes in one method.
- `WorkspaceStorageIndexCache` is already layered by remembered storage, live
  display snapshots, proximate snapshots, and deposit overlays. The final view
  above it is still whole-model.
- Forge currently has broad dirty signals (`markDirty`, carried revision,
  workflow sequence, proximity change) instead of typed invalidations with
  affected identities/storages.
- Some changes are known exactly from SLOT commands. Others, especially pipes
  or machines inserting into tracked storage, need storage fingerprint polling
  or adapter-specific event hooks.

## Target Model

Introduce a session-owned `WorkspaceProjectionStore` in common. It keeps
immutable read-model facts and derived slices. The store is updated by
`WorkspaceInvalidation` records and can emit either a full `SlotWorkspaceViewModel`
or a delta.

Core keys:

- `IdentityKey` — canonical `ItemIdentity` key for movable grouping and card
  identity.
- `ExactStackKey` — item id + exact `SlotStackAccess.dataFingerprint(...)` for
  content invalidation and display-stack freshness.
- `StorageKey` — storage id plus target kind / display kind when needed.
- `SectionKey` — visual home section id.
- `SourceSlotKey` — authority source id + stable entry key / slot index.

Primary facts:

- `SourceEntryFact`: exact identity, movable identity, count, display stack,
  exact stack key, source id, slot index, carried/source metadata.
- `CarriedIdentityFact`: total carried count, representative display stack,
  largest carried source/slot, carried-container capacity, free slot impact.
- `StorageMetaFact`: role, label, position, dimension, cluster, proximate state.
- `StorageContentsFact`: storage id, contents fingerprint, slot count, filled
  count, per-slot summaries.
- `StoragePresenceFact`: identity + storage id + count + representative display
  stack.
- `TargetFact`: desired, wanted, active-workflow needed, accepted input,
  craft-run pressure, junk, protection, recent rank.
- `HomeFact`: identity to section, ordinal, origin, player-placed flag.
- `ContextFact`: selected hotbar slot, active chest panel, loot panel, search
  query, remote-detail intent, current proximity set.

Derived slices:

- `CardSlice`: `identity -> AtlasItem` plus triage status.
- `SectionSlice`: section descriptors, ordered identity refs, carried counts.
- `StorageSlice`: chest chips, cluster descriptors, content summaries.
- `WayfindingSlice`: storage-to-target edges and merged target rows.
- `DepositabilitySlice`: identities currently eligible for quick/bulk deposit.
- `HotbarSlice`: hotbar, offhand, recent identity refs.
- `WorkflowSlice`: workflow cards, craft run, task-panel target pressure.
- `PanelSlice`: active chest and loot chest panels.
- `ContextualSlice`: contextual suggestion lanes.
- `FrameSlice`: status, diagnostics, pending count, selected slot, revision.

Each slice gets its own input key and revision. Encoders then reuse changed
projection slices rather than only reusing already-built NBT tags.

## Invalidation Model

Add a common invalidation envelope:

```java
record WorkspaceInvalidation(
        Reason reason,
        Set<ItemIdentity> identities,
        Set<String> storageIds,
        Set<String> sectionIds,
        EnumSet<WorkspaceProjectionSlice> slices,
        boolean requiresFullProjection,
        String diagnostics
) {}
```

Rules:

- Prefer specific identity/storage/section sets.
- Use `requiresFullProjection` when the event cannot be localized safely.
- Multiple invalidations coalesce before projection.
- Every delta carries a monotonically increasing view revision. The client asks
  for a full snapshot if it sees a gap or cannot apply a delta.
- Full projection remains a test oracle: after applying invalidations, the
  incremental view must equal a fresh full projection for the same request.

### Event Locality

**Player acquired item `I`**

Invalidate:

- `CarriedIdentityFact(I)`
- `CardSlice(I)`
- the section containing `I`
- `HotbarSlice` if the item entered hotbar/offhand or recents changed
- `WorkflowSlice` / `WayfindingSlice` if `I` satisfies desired, wanted,
  workflow, craft-run, or accepted-input pressure
- `FrameSlice` status/diagnostics only when the command outcome changed them

Do not touch unrelated storage chips or unrelated cards.

**Player lost item `I`**

Invalidate:

- `CarriedIdentityFact(I)`
- `CardSlice(I)`; if carried count reaches zero, decide whether a proximate,
  remote, desired, wanted, recent, or junk ghost should replace it
- the old and new sections if the card moves between wall and triage/Misc
- `WorkflowSlice`, `WayfindingSlice`, and `DepositabilitySlice` for `I`
- hotbar/offhand if the loss came from those lanes

**Player moved item `I` into or out of storage `S`**

Invalidate:

- `CarriedIdentityFact(I)`
- `StorageContentsFact(S)`
- `StoragePresenceFact(I, S)`
- `CardSlice(I)` because proximate/elsewhere presence pips can change
- `StorageSlice(S)` for chip counts/summaries
- `WayfindingSlice` and `DepositabilitySlice` edges involving `I` or `S`

No other storage's cards or chips should rebuild.

**External insert/extract in tracked storage `S`**

Detect by event hook or fingerprint polling. Once detected, diff the storage
snapshot and invalidate:

- `StorageContentsFact(S)`
- `StoragePresenceFact(I, S)` for each changed identity `I`
- `CardSlice(I)` only for changed identities that are visible or have active
  remote/search/wanted/desired/recent/junk intent
- `StorageSlice(S)`
- `WayfindingSlice` edges for changed identities and `S`

If the storage diff cannot identify changed identities, invalidate `S` and
fall back to rebuilding storage-derived slices while keeping unrelated carried
facts.

**Home assignment / reorder for identity `I`**

Invalidate:

- `HomeFact(I)`
- `CardSlice(I)`
- the old and new `SectionSlice`

If the operation reorders many items in one section, rebuild that section only.

**Chest role / label / anchor change for storage `S`**

Invalidate:

- `StorageMetaFact(S)`
- `StorageSlice(S)`
- every `StoragePresenceFact(*, S)` if visibility changes
- all cards whose only or visible presence came from `S`
- search/remote indexes for `S`

`Ignore` removes the storage from projection/routing; `Buffer` remains
visible/searchable but does not learn homes or quick-store; `Storage` stays
visible/searchable and participates in affinity/quick-store.

**Search query changed**

Default wall filtering should be client-local over already-known cards. Server
projection should only run when the query requests remote detail that is not
already materialized. Debounce remote search and invalidate only
`RemoteSearchSlice` / relevant `CardSlice` entries.

**Proximity set changed**

For each storage entering or leaving proximity:

- update `StorageMetaFact(S).proximate`
- update cards for identities present in `S`
- update `WayfindingSlice`, `DepositabilitySlice`, and `StorageSlice(S)`

Do not rebuild storages whose proximity state did not change.

## Event Sources

Use exact event sources where we already have them:

- Workspace action outcomes know which identity moved, source target,
  destination target, moved count, and whether activity events were recorded.
- `CarriedSourceAccess` mutations can report provider-backed carried changes
  with source ids that match live mutation authority.
- Menu slot listeners can capture changed slot keys instead of only setting a
  broad dirty bit.
- Chest deposit/take observers already diff open/close snapshots and can emit
  changed storage ids and identity deltas.
- Workflow command services know which identity, section, storage, workflow,
  or craft-run entry they changed; expose that as domain invalidation metadata
  instead of relying only on `nextGlobalSequence()`.

Use bounded detection where exact events are unavailable:

- Keep a tracked-storage fingerprint table for proximate/tracked storages.
- Poll a small budget of storages per tick/session refresh.
- Diff only storages whose fingerprint changed.
- Mark ambiguous diffs as storage-local or full fallback with diagnostics.

## Slice Dependencies

`CardSlice(identity)` depends on:

- `CarriedIdentityFact(identity)`
- `HomeFact(identity)`
- `TargetFact(identity)`
- recent/junk/protection flags for `identity`
- proximate/elsewhere presence for `identity`
- container-capacity resolver for `identity`
- chip suggestions only when the identity is unhomed/triage

`SectionSlice(section)` depends on:

- section metadata
- ordered card refs in the section
- carried counts for cards in the section
- cards entering/leaving the section

`StorageSlice(storage)` depends on:

- storage metadata
- storage contents summary
- cluster labels and local role state
- proximate flag

`WayfindingSlice` depends on:

- target identities with missing counts
- per-storage presence for those identities
- storage proximity / visibility / roles
- put-away routes for active workflow clutter

`WorkflowSlice` depends on:

- workflow definitions and activation
- target counts for identities referenced by active workflows/craft run
- carried counts for those identities

`ContextualSlice` is the least local and can remain whole-slice at first. It
must not block the core card/storage/hotbar slices from becoming incremental.

## Implementation Phases

### Slice 0 - Parity Harness And Diagnostics

Build confidence before changing behavior.

- Add fixtures that run a full projection, apply one synthetic invalidation,
  run incremental projection, and compare against a fresh full projection.
- Log invalidation reason, changed identity count, changed storage count,
  changed slice count, full-fallback reason, and delta payload size.
- Keep the existing slow-refresh logs, but add counts for projection facts
  updated versus reused.

Exit criteria:

- Full/incremental parity harness exists.
- Every fallback logs why it could not be localized.
- No player-visible behavior changes.

### Slice 1 - Typed Invalidation Envelope

Replace broad dirty-only refresh decisions with typed invalidations while still
running the old full projection.

- Add `WorkspaceInvalidation`, `WorkspaceProjectionSlice`, and dirty-reason
  coalescing in common.
- Teach Forge and NeoForge sessions to collect invalidations.
- Convert carried revision, workflow sequence, storage proximity change, search
  detail changes, and command outcomes into invalidation records.
- Keep `requiresFullProjection=true` for cases not yet localized.

Exit criteria:

- Refresh logs explain why each projection ran.
- Existing behavior and wire format remain unchanged.

### Slice 2 - Fact Store Skeleton

Extract fact construction without changing output shape.

- Create `WorkspaceProjectionStore` with immutable fact maps.
- Move `ProjectionIdentityContext` data into carried/source-entry facts.
- Expose storage index entries as `StorageMetaFact`,
  `StorageContentsFact`, and `StoragePresenceFact`.
- Add keys that include exact stack data fingerprints where display or content
  state matters.
- Continue composing the existing `SlotWorkspaceViewModel` from facts.

Exit criteria:

- Full projection from facts matches current `SlotWorkspaceViewModel.project`.
- Identity tests for batteries, fluid containers, backpacks, damageable tools,
  and water flasks pass unchanged.

### Slice 3 - Per-Slice Projection Cache

Promote encoded slices into projection slices.

- Split the current projection output into named slice builders.
- Give each slice an input key derived from only its dependencies.
- Reuse unchanged projection slices before encoding.
- Keep the existing full view model as a composed compatibility object.

Exit criteria:

- A hotbar-only change does not rebuild wall/storage/workflow slices.
- A status/frame-only change does not rebuild cards or chips.
- Slice hit/miss counts appear in refresh diagnostics.

### Slice 4 - Identity-Local Card Updates

Make card derivation per identity.

- Extract a pure `WorkspaceCardProjector` that builds one `AtlasItem` from
  the relevant facts.
- Cache cards by `IdentityKey`.
- Update only cards whose identities are invalidated or whose local
  dependencies changed.
- Recompute only old/new sections for cards that move section, triage, Misc, or
  hidden state.
- Keep full card rebuild fallback for global home-map or workflow changes until
  those changes are localized.

Exit criteria:

- Acquiring or losing one identity updates one card plus affected sections.
- Moving one stack into storage updates that identity's card and the target
  storage chip, not unrelated cards.

### Slice 5 - Storage-Local Updates

Make storage summaries and presence edges local.

- Cache `StorageChip` / content summaries by storage id.
- Maintain `identity -> storage presence` and `storage -> identity presence`
  indexes.
- Update presence for changed identities in changed storage only.
- Make remote/search/x-ray detail query those indexes instead of rebuilding all
  elsewhere ghosts.

Exit criteria:

- External change in one tracked chest updates that chest and changed
  identities only.
- Search-as-find still finds remote identities without materializing every
  remote card during normal refresh.
- `Ignore`/`Buffer`/`Storage` role behavior remains unchanged.

### Slice 6 - Wayfinding And Depositability Edges

Make target/storage edges incremental.

- Represent wayfinding as edges from `(storageId, identity)` to reason buckets
  (`workflow`, `desired`, `wanted`, `put_away`).
- Rebuild edges only when a target identity changes, a storage presence for
  that identity changes, or a storage enters/leaves proximity.
- Rebuild depositability only for changed carried identities and changed
  eligible storages.

Exit criteria:

- One acquired target item clears or adjusts only that identity's wayfinding
  rows.
- One storage content change does not scan all target identities against all
  storages.

### Slice 7 - Delta Wire Protocol

Send changed facts/slices instead of full snapshots after session open.

- Add a view message that can carry either `FULL_SNAPSHOT` or `DELTA`.
- Delta operations: upsert/remove cards, update sections, upsert/remove storage
  chips, update wayfinding rows, update hotbar, update workflow/craft run,
  update panels, update frame.
- Keep monotonic revisions and client gap detection.
- Client requests or receives a full snapshot on mismatch, missing base, or
  unsupported delta.

Exit criteria:

- Full snapshot still works for open/recovery.
- Normal single-identity changes send small deltas.
- Forge and NeoForge apply the same common delta semantics.

### Slice 8 - External Storage Change Detection

Improve non-player storage updates without making correctness depend on every
mod exposing hooks.

- Add tracked/proximate storage fingerprint polling with a per-session budget.
- Prefer exact hooks/listeners where available.
- Diff changed storage snapshots to changed identities.
- Fall back to storage-local or full projection when diffing is ambiguous.

Exit criteria:

- Pipe/machine inserts into a tracked storage eventually update search,
  ghosts, chips, and wayfinding.
- Polling cost is bounded and visible in diagnostics.

### Slice 9 - Retire Monolithic Hot Path

Keep full projection as an oracle/debug path, but stop using it for ordinary
refreshes.

- Session open builds a full store and full snapshot.
- Normal events apply invalidations and emit deltas.
- Full projection runs only on explicit fallback, test comparison, or recovery.
- Remove redundant pre-command projections that only exist to refresh a view
  before a mutation.

Exit criteria:

- Normal gameplay profile no longer shows `SlotWorkspaceViewModel.project(...)`
  as the dominant refresh path.
- Incremental/full parity tests cover the common event matrix.

## Acceptance Matrix

Each row needs full/incremental parity coverage and, where relevant, loader
coverage.

| Scenario | Expected invalidation |
| --- | --- |
| Pick up new carried item | identity card, section, recents, targets |
| Consume/drop carried item | identity card, section, targets, possible ghost |
| Move stack from carried to proximate storage | identity card, storage chip, presence edge, section count |
| Take stack from proximate storage | identity card, storage chip, presence edge, wayfinding |
| Pipe inserts into tracked storage | storage chip, changed identities, remote/search index |
| Assign/reorder home | changed identity and old/new sections |
| Change chest role to `Ignore` | storage removed, affected identity presence/cards updated |
| Change chest role to `Buffer` | storage visible/searchable, no quick-store eligibility |
| Activate workflow | target facts for active workflow identities, workflow slice |
| Add/remove craft-run recipe | craft-run slice, target facts for recipe ingredients |
| Search local carried item | client filter only; no server full projection |
| Search remote tracked item | remote detail slice plus matching cards |
| Proximity enter/leave storage | that storage plus identities present there |
| Water flask / fluid container identity | filled contents remain distinct where semantic |
| GregTech battery charge churn | charge-only changes do not create new movable card identity |
| Provider-backed backpack source | source ids match live mutation authority |

## Risks And Guardrails

- **Hidden global dependencies.** Contextual suggestions, affinity decay, and
  active workflow filtering can accidentally pull the whole world back into
  one slice. Keep them separate and allow them to be whole-slice while core
  cards/storage become local.
- **Identity drift.** All fact keys must preserve exact data fingerprints where
  display/content state matters and movable identity where grouping matters.
- **False-local invalidation.** If an event cannot identify changed identities
  or storages, fall back loudly instead of guessing.
- **Client delta desync.** Use revisions, base revision checks, and full
  snapshot recovery.
- **Memory growth.** Projection stores must evict stale identities/storages
  removed from all facts and cap retained display stacks.
- **Threading.** Build and mutate projection facts on the server thread unless
  a future async path first copies every `ItemStack`/fact into immutable data.
- **Loader divergence.** Loader adapters may observe different raw events, but
  they must emit the same common invalidation vocabulary.

## Verification

Focused tests:

```bash
./gradlew :common:test --tests dev.imagio.slot.inventory.workspace.WorkspaceProjectionSessionCacheTest
./gradlew :common:test --tests dev.imagio.slot.inventory.workspace.WorkspaceStorageIndexTest
./gradlew :common:test --tests dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModelDepositTest
./gradlew :common:test --tests dev.imagio.slot.inventory.core.ItemIdentityTest
./gradlew :common:test --tests dev.imagio.slot.inventory.session.CarriedAcquisitionActivityTrackerTest
```

Broad gates:

```bash
./gradlew :common:test
./gradlew :forge-1.20:test :forge-1.20:compileJava :forge-1.20:compileSharedProbeJava
./gradlew :neoforge:test :neoforge:compileJava
```

Profile checks:

- Normal crafting/inventory gameplay should show small invalidation counts and
  mostly slice hits.
- Single-identity changes should not rebuild all cards, all chips, or the full
  content key.
- Storage-heavy profiles should show bounded polling/diff cost and no broad
  remote ghost rebuild during collapsed wall refresh.
