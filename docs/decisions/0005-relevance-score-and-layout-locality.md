# 0005: Relevance Score Is A Derivation; Layout Is Client-Owned

Status: accepted

Created: 2026-04-25

This record captures where relevance scoring and atlas layout run in the
SLOT process boundary, and why position/size data stops crossing the
RPC wire as authority-shaped state.

For the relevance model, see
[../design/relevance-lod.md](../design/relevance-lod.md).
For the engineering plan, see
[../plans/relevance-lod-prototype.md](../plans/relevance-lod-prototype.md).
For the LDLib2 transport boundary this still runs over, see
[0002-ldlib2-workspace.md](0002-ldlib2-workspace.md).

## Decision

- **Relevance score is a derivation, not state.** Computed at the use
  site from `RelevanceContext` + a contributor list, both in `common/`.
  It is never persisted, never synced over the wire.
- **Server and client may both compute scores** — for different
  consumers, with different contexts, possibly with different
  contributor sets. The shared machinery (`RelevanceScore`,
  `RelevanceContributor`, `BandPicker`, `WeightedGridPacker`) lives in
  `common/` so either side can call it.
- **Layout is client-owned.** Cell sizes, cell positions, and the
  `x, y, width, height` of every `AtlasItem` are computed on the
  client. They are no longer authoritative wire fields shipped from
  the server.
- **`AtlasItem` wire format drops `x, y, width, height`.** The server
  ships identity + canonical-order signal + everything else needed to
  build a `RelevanceContext` (carried/recents/kit are already there).
  Client computes positions before render.
- **Drag-drop becomes ordinal.** Player drops at coordinate `(x, y)`,
  client resolves to `(islandId, ordinal)`, sends an ordinal command
  to the server. `VisualHomeAssignment` gains an `ordinal` field;
  `localX/localY` either retire or persist transitionally as a sort
  key.
- **Search query stays client-only.** Reflow on submit/clear matches
  the design's coarse-trigger discipline. Client recomputes layout;
  no server roundtrip.

## Context

The pre-decision flow:

- `SlotWorkspaceViewModel.build` runs server-side
  ([common/.../SlotWorkspaceViewModel.java](../../common/src/main/java/dev/imagio/slot/inventory/workspace/SlotWorkspaceViewModel.java)).
- Item positions come from `VisualHomeAssignment.localX/localY`, set
  by drag-drop commands.
- Cell sizes are uniform 32×32 (`SlotWorkspaceAtlasLayout.CARD_*`).
- Per-card render-time scaling (`ghostScaleFor`) shrinks ghosts via a
  `Transform2D` shim on top of the uniform layout.
- The view model — including positions — is encoded
  ([SlotWorkspaceViewModelCodec](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceViewModelCodec.java))
  and shipped to the client.

The relevance model expects per-band cell sizing and packer-driven
positions. Two architectural questions surface:

1. Where does the score get computed?
2. Where does the layout get computed?

The first question forced the second.

## Rationale

### Why "score is a derivation" beats syncing scores

Score is derived from inputs both sides have access to (carried
identities, recents, active kit, classification facets) plus one
client-only input (search query). Treating score as state would force:

- a wire field on every `AtlasItem` carrying redundant derived data,
- a server roundtrip whenever a client-only input (search) changes,
- a decision about which contributor set the wire-side score
  represents — Phase 2's, or the future auto-home phase's, or a
  union — and what to do when consumers disagree.

Treating score as a derivation removes all three problems. Server's
auto-home logic builds a context from authority data and calls the
same `RelevanceScore.compute(...)` the client does for layout. They
choose contributor sets that fit their consumer needs. They never
have to agree, never have to sync.

### Why layout belongs on the client

Reviewed every server-side consumer of position/size data. None
actually need it:

- **Drag-drop**: client resolves drop coordinate → island + insertion
  ordinal *before* sending. Server takes the resolved result.
- **Persistence**: `VisualHomeAssignment.localX/localY` were only ever
  consumed as a sort key. The relevance model moves to ordinal
  ordering anyway.
- **Hit-testing, click routing, camera fit**: client concerns.
- **Auto-homing on pickup** (future): needs identity + destination
  island, not pixel positions. The server runs scoring + classification
  to pick a destination; layout never enters the decision.

Position and size flowing over the wire as authority-shaped data is
the smell. They worked when layout was simple (uniform cells,
persisted localX/localY); the relevance model exposes the wrongness.
The fix is to stop shipping presentation as authority.

### Why search stays client-only

The design's reflow discipline (coarse triggers only — submit/clear,
not per-keystroke) means we don't actually need real-time server-side
search awareness. Pre-submit search is just visual highlight (current
behavior). On submit, the client rebuilds its layout with the search
contributor active. No server change.

If a future feature needs server-side search awareness (e.g., "auto-home
based on what the player is currently searching for"), revisit then —
syncing a string is a small change. We do not pay that cost
speculatively now.

## Consequences

- **Wire format diet.** `AtlasItem` loses four fields. Codec, encode,
  decode, and every consumer of `item.x()/item.y()/item.width()/item.height()`
  in `neoforge/` updates to read from the client-side layout result
  instead.
- **New client-side layout pass.** Some flavor of `AtlasLayoutSession`
  consumes the view model + camera scale + search query, runs scoring
  + `BandPicker` + `WeightedGridPacker`, returns per-island layouts.
- **Drag-drop semantics change.** Player can no longer place items at
  arbitrary pixel coordinates within an island. They reorder; the
  packer assigns positions. The design doc accepts this — "exact
  pixel coordinates not preserved, and that's fine."
- **`VisualHomeAssignment` evolves.** New `ordinal` field; transitional
  `localX/localY` may persist as a sort key during migration.
- **Tests churn.** The wire-format tests
  (`SlotWorkspaceLdlibModelTest`) and any client-side layout tests
  update.
- **`SlotWorkspaceViewModel.build` shrinks.** The position-assignment
  pass moves out; what remains is identity grouping, kit projection,
  recents, presence, etc.
- **Camera-fit code** (`FitCarriedCamera`) stops reading positions
  from `AtlasItem` and starts reading from the layout-session output.
  Already client-side; trivial change.

## Non-reversal guidance

Reverse this decision only if:

- A real server-side feature needs layout (no candidate today; the
  exhaustive review above found none), **and**
- Computing it client-side would produce the wrong answer (e.g., a
  multi-player workspace where two clients must agree on positions —
  not in scope for SLOT).

Reverse the score-as-derivation half only if:

- The score becomes load-bearing for cross-client agreement (again,
  not in scope), **or**
- The cost of recomputing scores at every consumer site dominates a
  measured hot path. Profile first; the contributor set is small and
  the inputs are cheap to look up.

Drag-drop's ordinal semantics is the most user-facing consequence. If
playtest reveals players actively miss spatial placement within an
island, revisit by reintroducing a "freeform" island kind that retains
authored coordinates while the rest of the atlas runs the relevance
model. Do not revert the architecture decision wholesale.
