# SLOT Wayfinding — Kit + Desired-Count Chest Locator

Last updated: 2026-05-02

Plan for [current.md § Queue](current.md#queue) item 1.3 ("Navigation
to chests with kit-needed items"). Covers desired-count gaps in the
same machinery — same projection shape, same surfaces.

## Goal

When a kit is active OR a player-global desired count is unmet, help
the player find chests holding the missing items. No magic teleport,
no particle trails through walls, no on-atlas teleport. Each cue must
correspond to something the player could in principle observe in the
world (a glow on a real block, a compass-style direction).

## Approach

Two layers. Layer 1 lives in the world. Layer 2 is a single chip
component — chest name + cluster + missing-item icons + compass +
distance — rendered at two mount points (HUD when no screen open,
atlas chest panels when the workspace is open). Same content shape
in both places so the player sees the same information whether they
glance at the HUD mid-walk or open the atlas to scan.

The two layers share one server-side projection; client renderers
consume it independently so either can be tuned without touching the
other.

### Layer 1 — In-world chest glow

Chests holding any kit-needed or unmet-desired-count identity render
a soft outline / pulse on the block while their chunk is loaded.
- Color tracks scope: amber for kit-scoped, blue for player-global,
  matching the existing desired-count pip palette.
- Intensity scales with distance: stronger when in line of sight,
  weaker through walls (cap, not zero — peripheral awareness still
  works for nearby unseen chests).
- Pulses gently (slow sine on alpha) so it's noticeable in motion
  without being agitating when stationary.

### Layer 2 — Wayfinding chip (HUD + atlas mounts)

One chip shape, five pieces of content:
- **Chest name** (the existing label / auto-name).
- **Cluster name** when the chest belongs to one (drawn smaller /
  muted underneath the name).
- **Missing-item icons** — a horizontal strip of the identities this
  chest holds that intersect the player's kit-needed / desired-count
  gaps. Capped to ~4 icons; "+N" tail when more.
- **Compass arrow** rotated each frame from the player's facing
  toward the chest's world position.
- **Distance** in meters (e.g. "24m"). Always rendered.

Cross-dimension target → compass + distance swap to a
dimension-shorthand label plus the chest's coords. Same content
otherwise.

**HUD mount.** Renders only while no screen is open. Walks the
target list (current dimension first), sorts by distance, takes the
top N (~5) and stacks them down the right edge below vanilla
potion-icon territory; a "+N more" tail collapses the rest. Each
chip fades to 0 within ~16 blocks so Layer 1's glow takes over
without doubling the cue.

**Atlas mount.** The chip *replaces* the content of the existing
chest chips in `StoragePanelBuilder` (proximity panel) and
`SearchResultsPanelBuilder` (chest locator). Same render function,
plus existing atlas-only behaviors layered on top: cross-surface
hover highlighting (already wired through `hoveredStorageId`),
right-click context menu (rename / forget), drag-drop targets.
Chips that aren't `WayfindingTarget`s render the same shape minus
the missing-item strip — name + cluster + (if loaded) compass +
distance still apply.

Atlas-card hover doesn't feed wayfinding directly: cross-surface
hover already pulses the matching chest chips, and amplifying
in-world glow while the workspace is open would land on a screen
the player can't see.

## Phases

### Phase 1 — Foundation (`WayfindingTarget` projection)

- New record in `inventory/workspace`:
  ```
  WayfindingTarget(
    storageId,
    dimensionId,
    BlockPos pos,            // ChestAnchor → BlockPos
    Set<ItemIdentity> missingIdentities,
    int totalMissingCount,
    Scope scope              // KIT or PLAYER (mixed → KIT wins)
  )
  ```
- Built alongside `kitNeededIdentities` in `SlotWorkspaceViewModel.project`,
  cross-referenced against `claimedChestMap` + chest contents.
- An identity is "missing" if its kit/player desired count > carried
  count for that identity. A chest "has" the missing identity if its
  contents projection contains the identity (movable-aware).
- Chests can target multiple missing identities; the per-target
  `missingIdentities` is the intersection of "chest contents" ∩
  "kit/player gaps." Empty intersection → not a target.
- Codec: round-trip the list over the existing workspace projection
  RPC. New `wayfindingTargets` field on `SlotWorkspaceViewModel`.
- Coverage: tests exercise empty, single-target, multi-target, and
  cross-dimension cases. Verify that fixing bug #5 keeps the
  projection stable across non-stackable identities (water_bucket
  shouldn't split).

### Phase 2 — Layer 1 chest glow

- New `neoforge/client/wayfinding/` package.
- `WayfindingTargetCache` — client-side mirror of
  `wayfindingTargets`, refreshed when the workspace projection
  arrives. Keyed by `storageId`.
- `WayfindingChestGlowRenderer` listens to
  `RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS`:
  - Iterates `WayfindingTargetCache`, filters to the current
    dimension + loaded chunks.
  - For each target: draws a wireframe AABB on the chest block (or
    on the lower half of a double chest's pair). Color from `scope`,
    alpha from `f(distance, lineOfSight, sineTickPhase)`.
  - Skips when player is more than `MAX_GLOW_RADIUS` blocks away
    (default 64 — covers a base, not a render dimension).
- Visual ref: vanilla `Entity#isCurrentlyGlowing` outline pass;
  alternative is a custom `RenderType.lines` draw, simpler to get
  right.
- No server changes in this phase — it's pure consumption.

### Phase 3 — Layer 2 chip component + atlas mount

Build the shared chip component and land it in the atlas first
(plumbing is mostly there) so the chip can be tuned against real
chests before the HUD mount adds the screen-edge layout concerns.

- New `WayfindingChip` builder under
  `neoforge/screen/ldlib/wayfinding/` that takes a
  `WayfindingTarget` (or null for non-wayfinding chests), the
  chest's display label + cluster label, and a render mode
  (`HUD` | `ATLAS`). Returns a `UIElement` with the five-piece
  content described in the Approach section.
- The compass + distance ticks on whatever per-frame update the
  hosting surface already has (workspace render loop for the atlas
  mount, RenderGuiEvent.Pre for the HUD). Source: player camera yaw
  + `target.pos()`. Distance is squared-distance bucketed to whole
  meters.
- `StoragePanelBuilder` (proximity panel) and
  `SearchResultsPanelBuilder` (chest locator): replace their chip
  content with `WayfindingChip` in `ATLAS` mode. Existing
  atlas-only behaviors stay: cross-surface hover highlighting,
  right-click rename/forget context menu, drag-drop wiring (none
  of which `WayfindingChip` knows about — they're attached to the
  chip's outer container by the panel builder).
- Cross-dimension chips render the dim-shorthand variant via the
  same builder.
- Non-`WayfindingTarget` chests still render the chip (name +
  cluster + compass + distance) just without the missing-item
  strip; this gives the proximity panel a uniform look.

### Phase 4 — Layer 2 HUD mount

- `WayfindingHudRenderer` on `RenderGuiEvent.Pre` (or `Post` if Pre
  fights vanilla overlays):
  - Renders only while `Minecraft.screen` is null.
  - Walks `WayfindingTargetCache` (current dimension first), sorts
    by squared-distance, takes top N.
  - Mounts each as a `WayfindingChip` in `HUD` mode, anchored down
    the right edge below vanilla potion-icon territory. A "+N more"
    tail collapses the rest.
  - Each chip fades to 0 alpha at <16 blocks so Layer 1's glow
    takes over.
  - Cross-dimension chips render in a fixed corner via the chip's
    dim-shorthand variant — kept off the distance-sorted edge list
    so they don't shift around as the player walks.
- Hotkey toggle reuses `SlotAtlasKeyMappings`: bind "wayfinding HUD"
  to a configurable key. Default-on; players can disable if it
  reads as noisy.

No client-side hover state is added at any phase. Atlas-card hover
already pulses matching chest chips through the existing
cross-surface hover plumbing; amplifying Phase 2's in-world glow
while the workspace is open would draw onto a screen the player
can't see.

## Data flow

```
SlotWorkspaceViewModel.project
  └─ kitNeededIdentities + desiredCount gaps
     └─ wayfindingTargets (Phase 1)        ──► codec ──► client
                                                          │
client cache (WayfindingTargetCache) ◄───────────────────┘
  ├── Phase 2: RenderLevelStageEvent → block glow (Layer 1)
  ├── Phase 3: WayfindingChip builder → atlas chest panels mount it
  │            in ATLAS mode (replaces existing chip content)
  └── Phase 4: WayfindingChip in HUD mode → screen-edge stack on
              RenderGuiEvent when no screen is open
```

## Tradeoffs

- **Visual noise risk.** A loaded base with 10 chests, 9 kit-needed
  identities, can light up half the room. Mitigations: distance fade
  on the in-world glow, N-nearest cap on the HUD mount, and a
  per-scope color so the player can tell "kit" from "standing order."
- **Line-of-sight in Phase 1.** Drawing through walls makes the cue
  feel magical; hiding behind walls fails the "the next room over"
  case. Compromise: full alpha on LOS, half alpha behind one wall,
  zero past two — implemented via a coarse `clipContext.traceBlocks`
  test, capped to 32 blocks so a long ray doesn't stall the render
  thread.
- **HUD pip and screen real estate.** Edge pips compete with vanilla
  hotbar / status overlays. Anchor them to the right edge below
  vanilla potion icons; cap to 5; collapse the rest to a single
  "+N more" chip.
- **Cross-dimension in Layer 1.** Glow can't fire across dimensions
  (different render world). Layer 2's chip handles this in both
  mounts via its dim-shorthand variant — coords replace the
  compass + distance, content otherwise unchanged.
- **Chip density in HUD vs atlas.** The atlas has space for the
  full content; the HUD is tighter. Same builder, but `HUD` mode
  uses smaller fonts and tighter padding. If the HUD chip ends up
  too dense in playtest, the cluster label is the first thing to
  drop on that mount.

## Open questions

- **Default-on vs default-off.** Lean: all default-on. Layer 1 is
  subtle and world-native; Layer 2's atlas mount only shows when
  the player has the workspace open; Layer 2's HUD mount gets a
  hotkey toggle in case it reads as noisy in playtest.
- **Performance ceiling.** With 30 wayfinding targets, the per-frame
  cost is dominated by the LOS trace in Phase 1. Cache LOS results
  per target keyed by `(playerChunkPos, targetPos)` and refresh on
  chunk change — N traces per chunk crossing, not per frame.

## Success criteria

- Player with active kit closes inventory; the HUD shows
  wayfinding chips (chest name + cluster + missing-item icons +
  compass + distance) for the nearest targets. Walking 16+ blocks
  closer, the visible chest in their base glows softly and its HUD
  chip fades. Reopening the workspace, the same chip content
  appears in the locator / proximity panel for every chest chip,
  with hover + right-click affordances unchanged.
- Cross-dimension target: chip in both mounts shows
  dim-shorthand + coords in place of compass + distance; no
  in-world glow fires.
- No measurable frame-time regression at 30 wayfinding targets in a
  single dimension (bench: `:neoforge:test` adds a render-budget
  smoke check).
- Identity divergence regression (bug #5 era): wayfinding targets
  for a non-stackable kit-needed identity (water_bucket) collapse to
  a single target chest, not two.

## Out of scope

- Pathfinding / breadcrumb routes.
- World-pinned waypoints persisted across sessions.
- Compass-as-item (the "wild idea" — needs a separate design before
  occupying a real inventory slot).
- Atlas → world camera-fly (violates the no-teleport guardrail).
- Sophisticated Backpacks as wayfinding targets. Backpacks aren't
  commonly used as fixed external storage in the world, so the
  effort to track a carrier's moving position for the glow + HUD is
  not justified. Wayfinding only fires on chests with a stable
  BlockPos; backpacks are skipped at the projection layer.

## File map (proposed)

```
common/src/main/java/dev/imagio/slot/inventory/workspace/
  WayfindingTarget.java                 (new, Phase 1)
  SlotWorkspaceViewModel.java           (extended, Phase 1)
common/src/test/java/dev/imagio/slot/inventory/workspace/
  WayfindingTargetTest.java             (new, Phase 1)
neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/
  SlotWorkspaceViewModelCodec.java      (extended, Phase 1)
  StoragePanelBuilder.java              (extended, Phase 3 — uses chip)
  SearchResultsPanelBuilder.java        (extended, Phase 3 — uses chip)
neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/wayfinding/
  WayfindingChip.java                   (new, Phase 3 — shared builder)
neoforge/src/main/java/dev/imagio/slot/neoforge/client/wayfinding/
  WayfindingTargetCache.java            (new, Phase 2)
  WayfindingChestGlowRenderer.java      (new, Phase 2)
  WayfindingHudRenderer.java            (new, Phase 4)
neoforge/src/main/java/dev/imagio/slot/neoforge/client/
  SlotNeoForgeClient.java               (event registrations)
```
