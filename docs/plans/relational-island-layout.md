# Relational Island Layout — Design Plan

Last updated: 2026-04-27

## Why

Islands today carry authored `(atlasX, atlasY)` world coords. The
client packer (`AtlasLayout.packAtlas`) treats those as *preferences*
and slides islands right (or drops to a new row) when their packed
footprint would collide with a placed neighbour. Two problems fall
out of that:

- **Teleport on grow.** When sliding right can't make progress,
  `packAtlas` drops below the obstructing rect and resets to the
  authored x. A small grow can therefore send an island several
  hundred pixels in one frame, jumping past unrelated neighbours.
  Spatial memory shatters.
- **Wasted space + fiddling.** Authored coords let the player leave
  arbitrary gaps between islands — pleasant when you're authoring,
  but the default view shows everything at once and density drives
  readability. The player ends up doing layout maintenance that the
  system could do.

The fix is to drop authored coords entirely and store islands as a
**relational DAG**: each island records "which island am I directly
adjacent to, on the north side and the west side" — nothing more. The
renderer derives every pixel position each frame from the DAG plus the
current packed sizes.

Why this works:

- **No teleport.** There are no authored coords for a packer to
  abandon. An island grows ⟹ exactly the islands east-of and
  south-of it (transitively) shift by the overlap amount. Minimum
  movement, no surprises.
- **No fiddling.** The player drags to express *adjacency*, not
  pixels. Drop captures "is now west of X / north of Y"; the
  renderer's job is to pack tightly under those relations.
- **No accidental waste.** Pixels are derived. Empty-space-as-design
  isn't expressible; the system always packs.

## What this changes (and what it doesn't)

Changes:

- `VisualAtlasIsland.x` / `.y` → `northAnchorId` / `westAnchorId`
  (both nullable; null means "topmost" / "leftmost").
- The atlas-level layout pass in
  [common/.../atlas/lod/AtlasLayout.java](../../common/src/main/java/dev/imagio/slot/atlas/lod/AtlasLayout.java)
  walks the DAG instead of consulting authored coords.
- `sendMoveIsland(islandId, worldX, worldY)` → `sendReanchorIsland(islandId, screenX, screenY)`
  (or similar — the wire still needs a 2-D drop point so the server can
  resolve it to anchors deterministically).
- Drag-drop UX: a live preview during drag shows where the island
  would land; commit-on-release captures the resolved anchors.

Doesn't change:

- Per-island packing. Items inside an island still pack via
  `WeightedGridPacker` against the auto-square wrap target.
- Relevance-LOD. Per-item cell sizes still come from the relevance
  contributors. The DAG only governs island-level placement.
- Triage island, atlas item placements, or any non-PLAYER island
  semantics. Triage stays a docked panel.

## Data model

```java
record VisualAtlasIsland(
    String id,
    String label,
    VisualAtlasIslandKind kind,
    String northAnchorId,   // id of the island this one sits south-of, or null
    String westAnchorId,    // id of the island this one sits east-of, or null
    int color,
    ItemIdentity iconIdentity
) { … }
```

Properties of the DAG:

- **Acyclic by construction.** Anchors point toward the top-left.
  Walk in topological order (depth in the DAG = max anchor depth + 1)
  to place each island after its anchors are placed. The leaf at
  rank 0 is the unique island with both anchors null.
- **Forest is fine.** More than one island may have both anchors
  null — they're independent roots, laid out side-by-side. Useful
  when an island is freshly created with nothing above-or-left of
  it. The first island ever created is the canonical root.
- **An anchor is just an id.** No offsets, no per-edge metadata.
  The renderer always packs flush against the anchor edges.

## Render rule (masonry-style with outer-boundary constraint)

This is the load-bearing detail; everything else is plumbing.

Each island has two binding constraints:

- **west-anchor X:** `my.left ≥ X.right + atlasIslandGap`
- **north-anchor Y:** `my.bottom ≥ Y.bottom + atlasIslandGap`
  (note: `bottom`, not `top` — see below)

Inside those constraints, the placer chooses the smallest `(my.top,
my.left)` that doesn't overlap any already-placed rect.

The "bottom ≥ anchor.bottom" rule is what makes the layout *masonry*
instead of *strict-grid*:

- An island B with `north = A` may slide its top edge **above**
  `A.bottom` — i.e., overlap A's vertical range — as long as B
  still sticks out further down than A. Reads as "B is below A"
  without forcing B into a row that wastes space whenever it's
  shorter than its row-mates.
- Symmetric for east-of: `my.right ≥ X.right + atlasIslandGap` lets
  B overlap A horizontally while still reading as "east of A."

Result: columns, rows, and Pinterest-style staggers all emerge
naturally from the DAG. There is no "row" concept in the data, only
"this thing is below that thing."

### Algorithm sketch

```
placed: List<Rect> = []
for island in topologicalOrder(islands):
    let n = placed[island.northAnchorId] or null
    let w = placed[island.westAnchorId] or null

    // Hard lower bounds from anchors.
    minLeft = (w != null) ? w.right + gap : 0
    minBottom = (n != null) ? n.bottom + gap : island.height

    // Slide the candidate up + left as far as possible without
    // colliding with any already-placed rect, while keeping
    // my.left ≥ minLeft and my.bottom ≥ minBottom.
    candidate = solveTightest(minLeft, minBottom, island.size, placed)
    placed[island.id] = candidate
```

`solveTightest` is a small per-island sweep — for the placement set
sizes we ship (low hundreds at most), an O(n²) scan finds the
tightest legal `(top, left)` quickly enough.

## Drag / drop

### Anchor pick

When the player drops island X at screen point `(dropX, dropY)`:

1. Convert to world coords against the current layout.
2. Among all other islands whose **right edge ≤ dropX**, pick the one
   with the largest `right` (closest to the drop from the left).
   That's X's new `westAnchor` — or null if none exist.
3. Among all other islands whose **bottom edge ≤ dropY**, pick the
   one with the largest `bottom`. That's X's new `northAnchor` — or
   null if none exist.
4. Commit: write X's new anchors.

Loose, not strict — a candidate anchor doesn't have to be
horizontally/vertically aligned with the drop. "Closest from the
top-left direction" is the only test.

### Reanchor affected siblings

After step 4, walk every other island Y. If X is now a *closer*
top-left neighbour for Y than Y's current `northAnchor` /
`westAnchor`, repoint Y. Concretely:

- For each Y where Y.westAnchor's `right` < X.right and X.right ≤ Y.left:
  Y.westAnchor = X.id.
- Same for north.

This is what the user spotted: dropping X *between* Tools and Food
re-anchors Food to X (sandwiching X in the chain) instead of leaving
Food anchored to Tools and X "alongside" them.

O(n²) per drag. n is dozens; drags are rare. Fine.

### Live preview during drag

Mid-drag (before release), run the anchor-pick and a layout pass
against the in-flight position so the player sees where the island
will land. Commit-on-release captures the final anchors.

If preview-as-you-drag costs too much, fall back to drop-shadow at
the cursor + a "would-anchor-here" indicator and only re-layout on
release. Defer the call to playtest.

## Create / delete

- **Create.** New island has anchors derived from its initial drop
  point via the same anchor-pick algorithm. If created with no drop
  context (e.g., from a chip suggestion), spawn it with both
  anchors null at the bottom-right of the current pack — least
  intrusive default — then run the reanchor-affected-siblings step.
- **Delete.** For every island Y whose anchor references the
  deleted X, run the anchor-pick algorithm against Y's last-rendered
  position to pick replacement anchors. Then re-render. Y's
  rendered position only shifts by the gap freed up by the missing X.

## Migration from `(x, y)`

Existing saves carry authored `x` / `y` per island. Convert at
load:

1. Sort islands ascending by `(y, x)`. This is the canonical reading
   order — the same order the current `packAtlas` walks.
2. Walk in that order, for each island X:
   - westAnchor = islands placed before X with the largest `x.right`
     such that `x.right ≤ X.x`.
   - northAnchor = islands placed before X with the largest
     `x.bottom` such that `x.bottom ≤ X.y`.
3. Drop `x` and `y` from the projection in a follow-up after the
   relational model has soaked.

Bonus: this migration is also the algorithm a "snapshot to relational
layout" tool would run, so it's worth keeping the helper around even
after the migration period ends.

## Slices

1. **Domain + projection.** Add `northAnchorId` / `westAnchorId` to
   `VisualAtlasIsland`. Migrate at load via the algorithm above.
   `x` / `y` stay as vestigial fields for one release, populated from
   the rendered placement so saves continue to load on rollback.
2. **Render: relational packer.** Replace `AtlasLayout.packAtlas`
   with a topological walk + masonry-rule placer. Existing per-island
   `packIsland` (item layout) is unchanged.
3. **Drag-drop.** Replace `sendMoveIsland(id, x, y)` with
   `sendReanchorIsland(id, screenX, screenY)` (or repurpose the
   existing payload — coords still arrive, but the server resolves
   them to anchors via the algorithm above instead of writing them).
   Implement reanchor-affected-siblings on the server.
4. **Create + delete reanchor.** Wire create-island and delete-island
   commands to run the same reanchor pass.
5. **Live preview.** Add the during-drag layout preview. Cheap
   version first; tighten if it stutters.
6. **Cleanup.** Drop `x` / `y` from `VisualAtlasIsland` and the
   wire format. Drop `sendMoveIsland`'s coord parameters from the
   RPC.

(1) + (2) + (3) deliver the core feature; (4) makes flows other than
drag honor the model; (5) is polish; (6) is post-soak housekeeping.

## Testing

- **Migration.** Snapshot test: `(x, y)` save → relational anchors →
  re-render. Verify rendered positions match the original within a
  small tolerance for arbitrary fixture saves.
- **Render rule.** Hand-crafted DAG fixtures exercise the masonry
  constraint: tall A + short B+C+D stack to its right packs without
  wasting space below B; Pinterest-style staggers come out.
- **Drop reanchor.** Drop X between Tools and Food: Food's anchor
  flips from Tools to X.
- **Grow propagation.** A grows ⟹ rendered positions for east-of-A
  and south-of-A shift by exactly the overlap delta; everything else
  unchanged.
- **No teleport.** Adversarial test: grow A by 10000 px. No island
  moves more than its overlap-with-A's-new-rect amount.
- **Cycle proof.** Property test: any drop sequence produces an
  acyclic DAG. (Should be impossible to construct a cycle since
  anchors point top-left, but worth a guarantee.)

## Open questions

- **Anchor-pick tiebreak when distances are equal.** Two islands at
  the same `right` / `bottom` — pick by id? by recency? Defer until
  we hit it; an arbitrary deterministic rule is fine.
- **Floating root islands.** Multiple islands with both anchors null
  → render them in a row at the top-left. Order-by-id for now;
  revisit if the visual feels arbitrary.
- **Anchor preservation across delete-then-recreate.** If an island
  is deleted and recreated (e.g., undo / redo), should its old
  anchors be restored, or re-derived from current positions?
  Restored is more "undo-respecting" but harder to keep consistent
  if the surrounding atlas changed during the gap. Lean toward
  re-derive; revisit if undo feels wrong.
- **Mid-drag layout cost.** If the live-preview repack stutters at
  high atlas counts, fall back to commit-on-release with a
  drop-shadow only. Measure first.

## What's intentionally out of scope

- **Continents / explicit grouping.** The user discussed grouping
  islands into named "continents" as a separate concept. Out of
  scope for this plan; the relational DAG handles tight packing
  without it. Revisit after this lands if visual grouping cues
  (background tint, label) feel needed.
- **Per-anchor offsets.** Recording "Food is 200 wu east of Tools"
  was considered and rejected — it re-introduces wasted space and
  the player drags-as-pixels mental model we're getting rid of.
  Pure DAG only.
- **Authored layout escape hatch.** A "pin to absolute position"
  toggle for power users. Cheap to add later; doesn't ship in v1.
