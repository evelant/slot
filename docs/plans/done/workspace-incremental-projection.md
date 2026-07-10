# Incremental Workspace Projection Plan

> Closed 2026-07-02. Shipped incremental projection infrastructure through the
> Slice 9 common event matrix: typed invalidations, localized fact-store
> updates, rendered local card/chip/edge/workflow/panel branches, full-oracle
> parity coverage, and Forge/NeoForge dirty-check integration. Dropped from
> this plan: complex put-away routing beyond the guarded simple
> claimed/deposit-eligible/live-observed/display-storage cases, complex craft-run
> pressure outside the guarded simple cases, richer wayfinding, and manual
> TerraFirmaGreg profiling; spin fresh plans if playtest signal justifies them,
> and do not reopen this one.

Last updated: 2026-07-02

Status: implemented 2026-07-02. Slices 0-8 are
landed: common typed invalidations, invalidation/fallback diagnostics, a
full-oracle parity harness, a common `WorkspaceProjectionStore` that
materializes source-entry, carried-identity, storage-meta, storage-contents,
storage-presence, and simple player target facts, and a projection-slice cache
that composes the existing compatibility view model from reusable frame / wall /
storage / hotbar / workflow / panel / contextual slices before encoding. Slice 4 adds
an identity-keyed card projection cache with full-card fingerprints, reused /
rebuilt / removed card diagnostics, and parity tests for one-identity card
changes. Slice 5 adds storage-keyed chip reuse with full-chip fingerprints,
storage chip reuse diagnostics, and tests for one-storage changes/removals.
Slice 6 adds storage-id keyed wayfinding-target reuse plus depositability-set
fingerprint reuse, edge diagnostics, and focused edge projector tests. Slice 7
adds a revisioned full/delta view-transfer envelope on both loaders, client
merge/gap handling, and focused transfer tests. Slice 8 adds budgeted common
polling for non-proximate tracked storage, updates remembered contents before
index composition, and reports poll candidates/checked/changed/failure counts
in refresh diagnostics. Slice 9 has started with two hot-path reductions:
normal remembered/typed search is client-local unless remote detail is
explicitly in `SEARCH` mode, and authority-only workflow/hotbar commands no
longer force a pre-command projection before their post-command broadcast.
Cursor-only structural churn now applies a common hotbar/frame invalidation and
matches a fresh full oracle without running the monolithic projector.
Localized identity/storage store mutation now updates only affected source-entry,
carried-identity, storage-meta, storage-contents, and storage-presence facts.
The first rendered-slice branches handle carried identity count, acquire, and
removal invalidations plus common visual-home drop invalidations in empty
or visual-home-only workflows, simple storage-chip change/removal invalidations,
simple player desired/wanted target changes for carried cards and target ghosts
plus the Fetch contextual lane, and simple mixed
identity+storage invalidations from those facts without running the monolithic
projector. The storage-only branch can run with unchanged simple carried cards
already on the wall; mixed identity+storage can update the affected card and
chip together, and identity-only carried changes can update the affected card
while preserving unchanged simple storage chips. Non-proximate tracked storage now updates desired/wanted target
ghosts, elsewhere presence, and acquisition wayfinding from localized facts.
Homed `TRACKED_XRAY` remote-only ghosts, identity-only xray card repaints, and
unhomed xray no-card storage updates also localize against unchanged remote storage facts.
Homed remote-search tracked-display ghosts now localize against the request's
tracked detail list.
Simple carried junk tag flips localize as card chrome when no junk ghost or
active workflow pressure is involved.
Simple craft-run wanted/no-pressure updates now localize for visual-home-only workflows,
including common craft-run command invalidations for recipe add, adjust,
ingredient selection, and remove; carried alternatives choose pressure, unresolved
alternatives without carried evidence stay pressure-free, and when a simple proximate claimed storage
already holds the ingredient, including a selected alternative, the same
invalidations localize the storage ghost, Fetch lane, and KIT-scoped wayfinding
from cached facts.
Simple active-workflow activation/deactivation localizes metadata-only
workflow/kit management, kit-needed target cards,
scoped desired/wanted/member target pressure, exact accepted-input chrome
including target pressure, accepted-tag substitute chrome from explicit carried
or proximate storage stack-tag evidence, Fetch lane, kit-card active state,
simple carried acquire/removal while the workflow stays active including
proximate claimed-storage KIT wayfinding updates, and
un-routed plus simple claimed-storage, affinity-backed deposit-eligible
claimed-storage, live-observed claimed-storage, and remote tracked-display
routed activation put-away cards/lanes/`PUT_AWAY` wayfinding
when complex put-away routes beyond the simple claimed/deposit-eligible/live-observed/display-storage
guards are not involved. If the active
workflow target is already present in simple proximate claimed storage, the same
path localizes the storage-backed ghost card, Fetch lane, and KIT-scoped
acquisition wayfinding from cached facts.
Proximate display and claimed-chest storage now update desired/wanted target
ghosts, nearby presence, acquisition wayfinding, and simple claimed-chest
depositability from localized facts. Simple proximate storage take has explicit
oracle coverage for affected card, chip, and wayfinding updates. Simple carried-container count/free/capacity
chrome localizes when the carried identity is invalidated. Explicit
active-chest panel slice invalidations localize as panel-only updates. Forge and
NeoForge carried-revision dirty checks now compare the previous and current
authority snapshots before projection; bounded carried acquire/remove/count/swap
diffs become common identity-local or frame-only invalidations, and Forge's menu
slot listener now emits a localized companion hint instead of forcing full
projection. Missing previous authority, missing source snapshots, source-shape
drift, or identity resolution failure preserves the full invalidation. These paths fail closed if
carried authority is underspecified or if complex put-away routing beyond the simple claimed/deposit-eligible/live-observed/display-storage guards, complex craft-run cases outside the simple wanted/no-pressure/proximate-claimed-storage guard, or wayfinding outside the simple acquisition/KIT cases are involved. The
Forge and NeoForge storage-proximity dirty checks now emit non-full
storage-local invalidations for the changed proximate/contextual storage ids,
letting the existing simple storage-presence branch handle claimed-chest
enter/leave when its guards pass while unsupported contextual or complex request
shapes still fall back through the oracle. Workflow-sequence dirty checks also
downgrade to frame-only when the previous/current workflow projection inputs are
unchanged apart from sequence bookkeeping. Remote-detail changes now localize
simple tracked-display search enter/clear/leave by removing old remote-only
ghosts and adding only current `SEARCH` matches. Complex put-away routing beyond
the simple claimed/deposit-eligible/live-observed/display-storage guards,
broader complex craft-run pressure, richer wayfinding, and richer remote-detail
request shapes intentionally remain fail-closed follow-up scope.
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
  [workspace-performance.md](../workspace-performance.md): exact stack data
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
- Forge and NeoForge still have broad dirty signals (`markDirty`, workflow
  sequence; carried revision and storage proximity now fall back full only when
  diffing or localized projection is unbounded) instead of typed invalidations
  for every ordinary refresh.
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

`Ignore` removes the storage from projection/routing; `Input` and `Output`
remain visible/searchable but do not learn homes or ambient quick-store;
`Storage` stays visible/searchable and participates in affinity/quick-store.

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
- `Ignore`/`Input`/`Output`/`Storage` role behavior remains unchanged.

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

Current blocker before landing: `WorkspaceProjectionStore` can now localize
identity and storage fact updates, and the session cache retains that store
across refreshes. Search typing no longer changes the structural key or sends a
server RPC in normal collapsed/intent-only modes, and commands that read live
authority without consuming the visual view no longer force a pre-command
projection. NeoForge cursor-only refreshes now take the hotbar/frame localized
branch, Forge and NeoForge carried-revision dirty checks now localize bounded
authority diffs to affected identities, Forge menu-slot listener dirtiness no
longer forces full projection by itself, storage-proximity dirty checks now
emit storage-local changed-id invalidations, sequence-only workflow dirty checks
become frame-only, simple remote-detail search enter/clear/leave localizes,
carried identity count/acquire/removal invalidations and common visual-home
drop invalidations in empty or visual-home-only workflows can
project matching rendered cards from store facts, simple player desired/wanted
target changes can update affected carried cards, target ghosts, and the Fetch
contextual lane, simple storage-chip change/removal invalidations can project
matching rendered chips from store facts even with unchanged simple carried
cards present, and simple mixed identity+storage invalidations can update the
affected card and chip together. Identity-only carried changes can also update
the affected card while preserving unchanged simple storage chips. Non-proximate tracked storage can now update
desired/wanted target ghosts, elsewhere presence, and acquisition wayfinding
from localized facts; proximate display and claimed-chest storage can now update
desired/wanted target ghosts, nearby presence, acquisition wayfinding, and simple
claimed-chest depositability from localized facts;
storage-only claimed-chest proximity enter/leave and simple proximate storage
deposit/take/cursor command-record invalidations can now update affected presence cards,
storage chips, and wayfinding from cached storage facts;
homed `TRACKED_XRAY` remote-only ghosts, identity-only xray card repaints, and
unhomed xray no-card storage updates also localize against unchanged remote storage facts;
homed remote-search tracked-display ghosts localize against the request-scoped
tracked detail list;
common junk-tag/direct-trash/belt/cursor commands now emit identity-local invalidations,
and simple carried junk/direct-trash/belt/cursor moves localize as card chrome;
simple craft-run wanted/no-pressure updates in visual-home-only workflows localize from
common craft-run command invalidations, including carried alternatives,
pressure-free unresolved alternatives, and proximate claimed-storage
craft-run ghosts/wayfinding when the ingredient, including a selected
alternative, is already cached there;
simple active-workflow activation/deactivation localizes metadata-only workflow/kit management and identity target cards,
scoped desired/wanted/member target pressure, exact accepted-input chrome
including target pressure, accepted-tag substitute chrome from explicit stack-tag
evidence, the Fetch lane, kit-card active state, simple carried acquire/removal
while the workflow stays active including proximate claimed-storage KIT wayfinding
updates, un-routed plus simple
claimed-storage, affinity-backed deposit-eligible claimed-storage,
live-observed claimed-storage, and remote tracked-display routed activation put-away
cards/lanes/`PUT_AWAY` wayfinding,
active-workflow storage-only route changes, and proximate claimed-storage KIT wayfinding from common workflow invalidations;
simple claimed-chest claim and `Ignore`/`Input`/`Output` role changes localize storage
addition/removal or quick-store eligibility cleanup from storage-keyed invalidations;
chest relabel commands emit storage-local chip invalidations, cluster relabel
commands refresh storage cluster descriptors from localized projection,
affinity-forget clears storage chip affinity/depositability chrome, and island
create/delete/label/color/icon/position/reorder commands refresh section descriptors
from localized projection; chest move commands are frame-only because current wall
chips do not render persisted atlas coordinates;
simple carried-container count/free/capacity chrome localizes when the carried
identity is invalidated;
explicit active-chest panel slice invalidations localize as panel-only updates. Those
paths are authority-guarded and do not localize complex put-away routing beyond the simple claimed/deposit-eligible/live-observed/display-storage guards, complex craft-run cases outside the simple wanted/no-pressure/proximate-claimed-storage guard, or wayfinding outside the simple acquisition/KIT cases. Do not bypass
`SlotWorkspaceViewModel.project(...)` for workflow structural misses or richer
identity/storage/home changes until rendered card, storage, home, and workflow
slices are projected from localized facts and parity-check against the oracle;
several commands still need a fresh server-side view as input and then a
post-mutation broadcast.

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
| Take stack from proximate storage | identity card, storage chip, presence edge, wayfinding (simple claimed-chest path localized) |
| Pipe inserts into tracked storage | storage chip, changed identities, remote/search index |
| Assign/reorder home | changed identity and old/new sections from common home-drop invalidation; island create/delete/label/color/position/reorder updates section descriptors |
| Claim chest / change role to `Ignore` | storage added/removed, affected identity presence/cards updated (simple claimed-chest path localized) |
| Change chest role to `Input`/`Output` | storage visible/searchable, no ambient quick-store eligibility (simple claimed-chest path localized) |
| Rename/move chest metadata | chest relabel updates the storage chip; cluster relabel updates cluster descriptors; affinity-forget clears chip/depositability chrome; move is frame-only until rendered coordinates return |
| Activate workflow | target facts for active workflow identities, metadata-only workflow/kit management, scoped desired/wanted/member/accepted input pressure with explicit stack-tag evidence, workflow slice, simple Put Away cards/lanes, and proximate claimed-storage KIT/`PUT_AWAY` wayfinding including affinity-backed deposit-eligible claimed storage |
| Mark/unmark junk or direct-trash carried junk | identity card, section, and workflow chrome from common command invalidation |
| Add/remove craft-run recipe | craft-run slice, target facts for recipe ingredients (simple visual-home wanted pressure localized; richer cases full) |
| Search local carried item | client filter only; no server full projection |
| Search remote tracked item | request-scoped remote detail slice plus homed matching cards |
| Proximity enter/leave storage | that storage plus identities present there (simple claimed-chest storage-only path localized) |
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
./gradlew :common:test --tests dev.imagio.slot.inventory.workspace.WorkspaceAuthorityInvalidationsTest
./gradlew :common:test --tests dev.imagio.slot.inventory.workspace.WorkspaceProximityInvalidationsTest
./gradlew :common:test --tests dev.imagio.slot.inventory.workspace.WorkspaceWorkflowInvalidationsTest
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
