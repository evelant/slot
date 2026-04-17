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
- NEVER GUESS - especially when it comes to APIs and library behavior. 
  Always verify that apis and behavior are what you might assume they are.
- ALWAYS check for upstream / downstream consequences of changes.
- Core domain logic belongs in `common/`, even when invoked from a platform
  RPC handler or UI widget. A good heuristic: if a file has zero
  `net.minecraft.*` / `net.neoforged.*` / `com.lowdragmc.*` / `com.mojang.*`
  imports, it should be in `common/`. Exceptions are rare and deliberate.
  When a platform call is needed from inside domain logic (e.g., signal
  extraction from an `ItemStack`), inject it as a functional parameter
  (`Function<ItemStack, IslandSignalDescriptor>`) rather than pulling the
  domain into the platform module.
- When adding new domain types (e.g., a new `WorkflowEvent` subtype),
  do not route them through platform-side exhaustive switches or
  per-record NBT methods — those create a ratchet where every core
  addition forces a platform edit. Keep codecs in a sibling class in the
  same module as the data.

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
- **`Button.setOnClick` fires on `MOUSE_DOWN`, not on release** — see
  LDLib2 `Button.onMouseDown`. If the button also needs to be a drag
  source, the `setOnClick` handler will run the moment the user presses
  and pre-empts the drag-start flow (and if the handler calls
  `rebuild()`, it destroys the drag source element outright). For
  elements that must support both click and drag, register the click via
  `addEventListener(UIEvents.CLICK, ...)` instead — LDLib dispatches
  `CLICK` from `ModularUI` on `MOUSE_UP` after `DRAG_PERFORM`, and only
  when release target equals press target.
- **Atlas card widgets must not grow.** The widget's world-space
  footprint is strictly `item.width() × item.height()`. LOD adjusts
  detail (disclosure level) but never widget size. Everything rendered
  inside a card — shell, inner panel, icon — must clamp to card bounds,
  including caller-site `centeredWorld(...)` math.
  `AtlasRenderBudget.forScreenBudget` has px floors (e.g. `shellPx` min
  16) that translate to world units larger than the card at low zoom if
  unclamped.
- **LDLib2 `GraphView` has no pan bounds.** `offsetX` / `offsetY` are
  unbounded `float`; content children render at arbitrary positive or
  negative world coords. Do not impose artificial canvas-size clamps on
  island coordinates — `VisualAtlasIsland.x`/`y` are unbounded `int` in
  the domain for the same reason.
- **`ItemStackTexture.setColor` only dims flat GUI-shader items.**
  Internally it calls `RenderSystem.setShaderColor` before
  `graphics.renderItem`. Block models, emissive items (torches), and
  items with custom shaders ignore the shader color. For uniform
  dimming across every item type, draw an `overlayTexture` rect on top
  of the icon `UIElement` instead.
- **View-model island projection must preserve stored `width`/`height`.**
  `SlotWorkspaceAtlasLayout.baseIslands` previously hardcoded
  `PLAYER_ISLAND_MIN_WIDTH` / `HEIGHT` onto every projected island,
  discarding the stored dimensions. Downstream `clampPlacement` then
  used the hardcoded size as the card-placement clamp ceiling, so any
  item with a stored `localX`/`localY` past the min-size edge was
  pulled onto the edge — producing piles of overlapping cards at the
  island boundary.
- **`worldX >= 0 && worldY >= 0` is not the right "coords provided"
  check.** Use `worldX != null && worldY != null`. Negative world
  coordinates are valid (islands can live at any `x`/`y`); sign carries
  no "coords absent" meaning.
- **Do not bury inventory semantics inside an RPC endpoint.** The LDLib
  RPC surface (`SlotWorkspaceUiSession`) is platform-coupled only because
  LDLib dispatches by method name; the method *bodies* belong in
  `SlotWorkspaceCommandService` (common) and return a
  `WorkspaceCommandOutcome` the adapter applies to session state. Same
  pattern for any future RPC surface. If you find yourself writing
  business rules ("chip-accept materializes a template island",
  "dropping on Triage clears the home") inside an RPC handler, stop and
  move them. Concrete markers that you are leaking:
  - A method on the RPC handler reads from `WorkflowDomainRuntime` and
    writes an `InventoryActionRequest`, `VisualAtlasIsland`,
    `VisualHomeAssignment`, or other common type.
  - A static helper in a neoforge file takes only common types as inputs
    and outputs (e.g., `matchingTemplate(VisualAtlasIsland) →
    IslandSuggestionTemplate`).
  - A data record in `neoforge/` has `toTag(...)` / `fromTag(...)`
    methods on it — split into a pure record (common) + a codec class
    (neoforge). See `SlotWorkspaceViewModel` (common) +
    `SlotWorkspaceViewModelCodec` (neoforge) for the canonical pattern.
- **Do not put exhaustive `switch` over sealed domain types in a
  platform file.** The compiler enforces coverage, which means every new
  event type forces a platform edit. Keep the switch in the same module
  as the sealed type — see `WorkflowDomainFileStore` (common) for the
  reference placement of event encoders.

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
