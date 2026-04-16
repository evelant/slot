# Agent Guide

## Bootstrap (Start Here Every Session)

1. Read [README.md](README.md) for orientation and the doc map.
2. Read [docs/status.md](docs/status.md) for current baseline, focus, the
   concept → code map, and the key-terms glossary.
3. For the active slice sequence and exit criteria, read
   [docs/plans/current.md](docs/plans/current.md).
4. Only then dive into architecture/design/decision docs that match the task.

## Verify Before Trusting Doc Claims

This is a fast-moving experimental repo. Docs describe intent as of their
"Last updated" date — not necessarily current code.

- Before relying on a "landed" claim in status.md, confirm with `git log`
  or by reading the file.
- If a doc and the code disagree, trust the code and update the doc.
- If a claim is load-bearing for the task you are about to do (e.g. "the
  atlas transfer RPC exists"), grep for the symbol before building on top
  of it.

## Working Rules

- This mod is unreleased and experimental. Any code may be deleted or
  restructured without regard for backwards compatibility or API stability.
  Prefer clean rewrites over compatibility facades.
- Do not keep old interfaces or facades in place to avoid refactoring.
  Implement the current plan/direction cleanly.
- Absolutely no quick fixes. Investigate root causes and wider impacts
  before changing code. Focused patches without understanding the
  surrounding system create more bugs.
- UI and LDLib code may own rendering, local focus, and transport; SLOT
  common owns inventory semantics.
- Screens and client RPC commands must not provide authoritative stack,
  count, identity, host id, or menu ref for real mutations — build
  authoritative requests on the server from live authority.
- Unsupported host state must fail closed and log a useful diagnostic.
- Keep LDLib2 imports out of `common/`. Keep inventory semantics out of
  `neoforge/` UI code.

## When Direction Changes

SLOT is experimental and pivots are expected. A pivot is a change in
chosen surface, approach, or concept — not a routine slice
refinement. When the user picks a new direction, or when exploration
concludes the current one is wrong, do all of this in the same change:

1. **Write an ADR.** Next number under [docs/decisions/](docs/decisions/).
   Capture: the decision, the context that forced it, why the old
   direction was abandoned, consequences, and non-reversal guidance
   (what evidence should be required to revert). See 0003 and 0004 for
   style.
2. **Update [docs/status.md](docs/status.md) "Current Focus".** Two or
   three sentences pointing at the new direction and the
   plans/current.md slice that carries it.
3. **Update [docs/plans/current.md](docs/plans/current.md).** Rewrite
   the slice sequence. This is the single source of truth for
   near-term engineering order — nowhere else should duplicate it.
4. **Delete or trim superseded docs.** Docs describing only the
   abandoned direction should be deleted (git history preserves them).
   Docs that are partially valid should have their superseded sections
   removed with a pointer to the new direction. Do not leave "this is
   superseded by X" stubs in place of real content.
5. **Link the new ADR from [README.md](README.md)** in the Decisions
   section so future agents find the "why" without grepping.

Partial pivots (narrowing scope, swapping a planner, changing a single
slice) do not need an ADR — just update status.md and current.md.

## Traps

Specific mistakes this codebase has learned to avoid. Consult before
writing new UI or action code.

- **Do not use `ScrollerView` as the atlas primitive.** Use the generic
  `com.lowdragmc.lowdraglib2.gui.ui.elements.GraphView` (not the
  node-toolkit wrapper at `nodegraphtookit.gui.GraphView`).
- **Do not fork LDLib2's pan/zoom.** If gestures block the UX, write a
  thin wrapper (`SlotAtlasGraphView`) that normalizes events or exposes a
  safe `setCamera(offsetX, offsetY, scale)` method.
- **Do not mutate `GraphView.offsetX` / `offsetY` directly.** Those
  setters do not refresh the content transform. Use `fit` /
  `fitToChildren` or a wrapper-level camera method.
- **Mouse-wheel zoom checks `event.target == this`,** so zoom may not
  fire while the pointer is over hit-testable cards. Plan for this in
  gesture handling.
- **Left-drag panning only starts on empty graph background;** middle-drag
  pans more broadly. Account for both in the atlas UX.
- **Visual home assignment commands must stay separate from real
  inventory mutation commands.** Moving a home is presentation state;
  moving a stack is authority mutation. Conflating them breaks trust.
- **Do not auto-home ambiguous items.** Only very high-confidence
  placeable blocks auto-home. Everything else lands in `Triage` until the
  player places it.
- **Do not treat broad heuristic categories as authoritative.** Show them
  as inspector hints at most.
- **Coordinates are not action targets.** A visual slot-shaped object
  must either be a real action target (hotbar slot) or clearly a visual
  mirror/ghost — never both.
- **The prototype list screen is abandoned.** Do not extend it, fix it,
  or use it as a fallback mode.
- **Exposing unimplemented action verbs in UI is forbidden.** `SWAP`,
  `TRASH`, `VOID`, `SORT_SOURCE`, `DISTRIBUTE`, `COLLECT_MATCHING`, and
  `SET_FILTER` are declared vocabulary but must fail closed until
  planners/executors land.

## Reference Material

- Local mod source: [reference/](reference/) — includes LDLib2,
  Applied-Energistics-2, Sophisticated Backpacks/Core, Tom's Storage,
  InventoryEssentials, TrashSlot, EMI. Look here before guessing at API
  shapes.
- LDLib2 docs:
  <https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/>
- LDLib2 UI agent guide:
  <https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/ui/agent_guide/>
- LDLib2 data bindings:
  <https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/ui/preliminary/data_bindings/>
- LDLib2 RPC packet:
  <https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/sync/rpc_packet/>
- Use context7 and deepwiki MCP for NeoForge, Minecraft, LDLib2, and
  related mod APIs instead of guessing. Especially important for LDLib2
  and NeoForge.
