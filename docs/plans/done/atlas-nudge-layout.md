# Atlas Nudge Layout — Design

Last updated: 2026-04-28

> **Status:** Implemented. Replaces the gravity-toward-centroid model
> (renamed from `positional-gravity-layout.md`) which was scrapped after
> playtesting showed it could not stay stable under size changes.
> The previous design history is preserved in git history.

## Why this exists

The atlas needs *some* automatic layout because islands change size as
items are added / removed / deposited / kit-grabbed. Without any layout
help, growing islands would overlap their neighbours.

But the gravity model — where every island falls toward a single
attractor (origin or cluster centroid) — proved fundamentally hostile
to size changes. Centroid drift moves *every* island whenever *any*
island's size changes, so bulk operations re-shuffle the entire cluster.
The "never jumps" property the player wanted is incompatible with any
global force model.

## What we want

1. Islands stay **exactly** where the player put them, until forced to
   move by a specific local cause.
2. When an island grows, only the islands it actually intersects (and
   their cascade) move out of the way.
3. When an island shrinks or is deleted, islands that had been
   displaced pull back toward their authored home.
4. Bulk operations (deposit-all, kit grab, triage chip-through) compose
   from these local primitives — each grow/shrink resolves locally; the
   sum is a stable cluster, not a re-shuffled one.
5. Compaction (closing manually-authored gaps) is *not* this layer's
   job. Layer it on top later if playtesting wants it.

## Data model

Per island we keep:

- **Home** `(homeX, homeY)`: the player-authored top-left, server-
  authoritative. The player drags an island to set its home.
- **Render** `(renderX, renderY)`: the actual on-screen top-left,
  derived locally and cached across frames. Defaults to home.
- **Size** `(w, h)`: derived from packed contents at render time.

Render only deviates from home when forced. The deviation is the
"displacement"; pull-home tries to recover it whenever there's room.

## Algorithm

Two primitives, run in order on every render. The renderer compares the
current frame's input to the previous frame's per-island state to decide
what is "active" — i.e., what just changed in a way that should cause a
push.

### Push (phase 1)

For every island that grew, was just dragged to a new home, or is new
this frame: BFS-walk from it, pushing any overlapping islands away
along the smaller-displacement axis, in the direction away from the
pusher's center. Each pushed island enters the queue and may push
*its* overlappers; cascade continues until no overlaps remain
reachable.

A single island is pushed at most once per frame. Multi-overlapper
fan-out (e.g., six islands authored at the same coordinates resolving
into a row) is handled by re-resolving each newly-pushed island
against the already-pushed set after the first push: if it lands on
top of a sibling, push it past that sibling too. This is bounded
because each subsequent push moves the island further from the
original anchor.

The `pushAway` step uses no EPS slop — `mover.edge = pusher.edge`
exactly. The overlap test uses strict `>`, so flush-touching reads as
non-overlapping. Adding EPS would shift the pusher's center slightly
and flip the next cascaded push's "which side?" decision.

After the active-island BFS, a leftover-overlap loop catches any
residual overlaps from startup state (two homes that happen to
coincide, or rare edge cases) by promoting the lex-smaller id of any
overlapping pair to anchor.

### Pull-home (phase 2)

Every island whose render ≠ home tries to sweep along the straight
line back to home, stopping at first contact with any other island.
Pull-home **never displaces other islands** — only growers push.

Iterated in ascending displacement-magnitude order, so islands closer
to home recover first and free space for further-displaced ones
behind them. Loops until no further progress.

Because pull-home doesn't push, a shrunk island's previously-pushed
neighbour can only return as far as it can travel without crashing
into something else. If the path is blocked it stops, and the player
will need to manually drag if they want it back at home.

## Why this is stable under size changes

- Bulk grows: each grower's BFS only touches islands it actually
  intersects. Unrelated islands stay put.
- Bulk shrinks: pull-home is monotonic — every island tries to close
  the displacement; the order doesn't matter and it converges.
- Cascades are local: A's grow ripples east only as far as the eastern
  neighbours' positions force it; islands north / south of A are never
  touched unless A actually overlaps them.
- No global state: the centroid no longer matters because there is no
  centroid. Every position is a function of its own home plus its own
  push history.

## Operations

| Operation | What changes | What the algorithm does |
|-----------|--------------|-------------------------|
| Drop island at (x,y) | New island, home = (x,y), no prev state | NEW → active. Push any overlappers; pull-home settles. |
| Drag island from P1 to P2 | island.home updated to P2 | HOME_MOVED → active. Push overlappers at P2; pull-home (others may pull toward homes since P1 is now empty). |
| Grow island | size(w,h) increased | GREW → active. Push overlappers. |
| Shrink island | size(w,h) decreased | Not active. Pull-home runs; previously-displaced neighbours can recover. |
| Delete island | island gone | Pull-home runs; neighbours may recover toward home. |

Item-level operations (homing items, depositing, kit-grabbing)
translate into one or more grow / shrink events when an island's
packed footprint changes; the rest is fallout from those.

## Implementation

`AtlasGravityLayout` and its tests are deleted. Replaced by:

- **`common/src/main/java/dev/imagio/slot/atlas/lod/AtlasNudgeLayout.java`** —
  the algorithm. Pure / static; one entry point
  `layout(specs, prevState)` that mutates `prevState` in place.
- **`common/src/test/java/dev/imagio/slot/atlas/lod/AtlasNudgeLayoutTest.java`** —
  unit tests covering each operation kind.
- **`AtlasLayout.packAtlas`** in `common` calls
  `AtlasNudgeLayout.layout`, threading the per-controller `nudgeState`
  map through.
- **`SlotWorkspaceUiController.nudgeState`** in `neoforge` is the
  per-session state map. The controller doesn't need any settled-back
  RPCs, layered caches, or centroid math — the algorithm is purely
  local and the cache is just "what did each island look like last
  frame?".

The data flow is now plain: server stores authored homes; client
renders homes plus locally-tracked displacements. No round-trips, no
settled-back writes. The bug class around stale authored positions is
gone by construction.

## Logging

`dev.imagio.slot.atlas.nudge` (SLF4J). At DEBUG: per-island push and
pull-home events showing the from/to and the pusher / blocker id. At
WARN: the rare case where a leftover-overlap pair can't be resolved.

## Manual compaction: tighten gesture (Shift+click)

Shipped. Shift+left-click on an island slides it toward its nearest
axis-aligned neighbour (or any blocker in the path) and stops at flush.
The home is set `TIGHTEN_FOLLOW_DELTA` (~36 px = one card row) past the
stop position along the snap axis only — the other axis is preserved.

Consequences:

- Two consecutive snaps (one per axis) compose: each click writes a
  per-axis sticky home, accumulating across gestures.
- When the snap target shrinks, pull-home pulls the snapped island
  toward home until contact, absorbing up to one card-row of shrink
  automatically. Larger shrinks leave a gap; the player can re-tighten.
- When the snap target is deleted, the snapped island teleports up to
  one card-row past the old snap position. Bounded by `TIGHTEN_FOLLOW_DELTA`.
- Idempotent: shift+clicking an island already flush against its
  nearest neighbour re-establishes the same sticky home.
- If no axis-aligned neighbour exists (target is fully diagonal from
  every other island), the gesture is a no-op with a "nothing to snap
  to" status hint.

Implemented as `AtlasNudgeLayout.tighten(state, islandId, followDelta)`
returning a `TightenResult` with the new home + snap axis + target id.
The function mutates the state map in place to record the post-snap
render and home, so the very next layout call sees no home-change and
leaves the cluster alone. The controller also optimistically updates
the local view model with the new home so any rebuild that lands
between the gesture and the server round-trip doesn't observe a stale
home and undo the snap.

## Future work (deliberately out of scope)

- **Auto-compaction.** A layer-on-top operation that nudges all islands
  toward their nearest neighbour to close authored gaps. Trivial to
  layer over the tighten gesture, but deliberately not automatic —
  player intent on gap placement is unknowable from outside.
- **Multi-cluster / continents.** Each island belongs to one logical
  group; layout runs per-group. Adds a group-id field to home +
  renders each group's layout independently.
- **Drag preview.** Render a ghost rect at cursor showing where the
  drop would land. Reuse the algorithm on a temp copy of the state.
