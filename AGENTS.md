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
- There are no users, no published releases, no save files in the wild,
  and no remote API consumers. When replacing a concept, **delete the
  old events, projection paths, persistence fields, RPCs, and UI code in
  the same change**. Do not propose migration plans, "leave it for one
  release," soft-deprecation, dual-write windows, or save-format
  upgraders. Save format may change shape freely; existing local saves
  may break and that is acceptable.
- Absolutely no quick fixes. Investigate root causes and wider impacts
  before changing code. Focused patches without understanding the
  surrounding system create more bugs.
- UI and LDLib code may own rendering, local focus, and transport; SLOT
  common owns inventory semantics.
- Before editing LDLib2 workspace UI code, skim
  [docs/architecture/ui-lifecycle-rules.md](docs/architecture/ui-lifecycle-rules.md)
  for the rebuild/scale/animation rules we derived from real flicker
  bugs.
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
- **Atlas card render must stay inside its allocated cell.** The
  widget's world-space footprint is strictly `item.width() × item.height()`
  — set at layout time by the band picker (see
  [docs/design/relevance-lod.md](docs/design/relevance-lod.md)). Cell
  size *can* vary per item (band-driven); what must not happen is
  rendering escaping the cell. Everything drawn inside — shell, inner
  panel, icon — must clamp to card bounds, including caller-site
  `centeredWorld(...)` math. `AtlasRenderBudget.forScreenBudget` has
  px floors (e.g. `shellPx` min 16) that translate to world units
  larger than the card at low zoom if unclamped.
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
- **Never call `BuiltinInventoryActionExecutor.*` directly from
  session / integration code.** Use `InventoryActionExecutor.execute`.
  The builtin layer's PROVIDER/TOOL diagnostics
  (`non_builtin_target_route`, `unresolved_target`,
  `unsupported_builtin_extract_route`, `unsupported_builtin_insert_route`)
  are **boundary-skip markers**, not real failures — they tell the outer
  executor to try the provider layer. Calling the builtin directly means
  any backpack / terminal / tool source silently fails. The *only*
  legitimate direct caller is `InventoryActionExecutor` itself. See
  [architecture/overview.md → Transfer routing layers](docs/architecture/overview.md).
- **`ASSIGN` requires both source and destination to be `PLAYER`-bound.**
  The in-place swap path in `BuiltinInventoryActionExecutor.assign`
  rejects `MENU` / `PROVIDER` / `TOOL` bindings with
  `assign_requires_player_bound_targets`. There is **no provider
  fallback for `executeAssign`**. If you need to move an identity from a
  backpack (or any provider source) onto a quick-access slot, emit
  `TRANSFER + INSERT_ONLY + STACK` and guarantee the destination is
  empty first (stage the current occupant out via TRANSFER). This is
  what `LoadoutApplyService` does automatically via `isPlayerBoundSource`
  + `applyKind`.
- **Restore must use the same layer that extracted.** When an
  `InventoryActionExecutor.executeTransfer` partially succeeds (insert
  returns a remainder) or fails after extract, the un-inserted portion
  must be re-inserted through the layer that owned the original extract.
  Writing a provider-extracted stack back via
  `BuiltinInventoryActionExecutor.insert` against a `PROVIDER` source
  returns `non_builtin_target_route` and the stack silently vanishes.
  `ExtractionResult.viaProvider()` tracks the original layer;
  `restoreExtracted` consults it.
- **When an outcome is blocked, both layers' diagnostics matter.** Do
  not surface *only* `builtin.diagnostics()` or *only*
  `extraction.diagnostics()` — that masks the real failure behind a
  boundary-skip marker. Use `preferProviderDiagnostic(builtin, provider)`:
  it prefers whichever layer emitted a *meaningful* diagnostic, falls
  back to joining both boundary markers when both layers only said
  "not my concern." Provider-layer `source_is_not_provider_backed`
  diagnostics carry a `:sourceId=bindingRoute` suffix so host-topology
  drift is debuggable.
- **`stableOrder` is the routing preference, and it's backpack-first.**
  Lower rank = tried first, for both extraction candidate search and
  insertion destination allocation. Canonical ranks: backpack 15–50 <
  `PLAYER_MAIN` 100 < `PLAYER_QUICK_ACCESS_LANE_0` 110 < armor 120 <
  offhand 130. Main only fills when every backpack slot is taken. Do
  not silently re-rank without updating the docs — the invariant is
  "backpacks are overflow storage; main is workspace; hotbar is
  actively-used space," and users notice when overflow rules flip. If a
  new integration adds a carried source, give it a rank that sits in
  the correct bucket (overflow: small numbers; workspace: 100-range;
  active use: 110+).
- **`LoadoutApplyService` stage rollback must use `TRANSFER +
  INSERT_ONLY`, not `ASSIGN`.** The staging slot is always empty at
  rollback time (we just moved something out of it), so `INSERT_ONLY`
  is sufficient — and `TRANSFER` goes through the mixed-layer executor
  so rollback from a backpack staging slot works. `ASSIGN` would
  reject the backpack-bound source with
  `assign_requires_player_bound_targets`.
### Storage abstraction — rules for adding or editing storage code

**Read this before writing any code that reads, mutates, or iterates
storage.** The full architecture + recipes live in
[docs/architecture/storage-integration.md](docs/architecture/storage-integration.md);
the rules below are the terse reminder list.

**Why it exists.** Every "operation X silently ignores backpacks" bug
we've ever shipped came from the same shape: a caller hardcoded a list
of known sources (or called a specific mod's transfer support directly)
instead of routing through the storage abstraction. The abstraction
makes the provider set open-ended; the rules below keep it that way.

**Adding a new storage mod.** See
[storage-integration.md](docs/architecture/storage-integration.md) for
full recipes. Short version:

- Minimal carried mod (Curios, generic backpack): implement
  `CarriedProvider`, register in `CarriedProviderRegistry`. Done.
  `DefaultCarriedProviderIntegration` synthesises the
  `PlayerInventoryExtension` for you.
- Rich carried mod (SB-class — openable UI, tool upgrades, custom
  labels): implement `CarriedProvider` **and** a bespoke
  `InventoryIntegrationProvider`, override
  `CarriedProvider.autoSynthesizeExtension()` → `false` to avoid
  double-registration.
- World-block storage that exposes `Capabilities.ItemHandler.BLOCK`:
  nothing to do.
- Virtual / aggregated storage (AE2, Create): implement
  `WorldStorageAccess.Delegate`, register it.
- Non-chest claimed storage (drawers, barrels as first-class targets):
  **not supported today**; requires `Target` variant + claim-model
  generalisation. Talk to the architect before starting.

**Hard don'ts when writing higher-level code.** Each of these
re-introduces the exact bug class the abstraction was built to prevent:

- Don't hardcode `{PLAYER_MAIN, PLAYER_QUICK_ACCESS_LANE_0,
  PLAYER_OFFHAND}` (or any subset) as "the carried sources." Use
  `CarriedSourceAccess.findIdentity` / `findAllMatching`, or iterate
  `authority.carriedSources()`.
- Don't call `player.getInventory().add(…)` / `setItem(…)` /
  `offhand.set(…)` directly. Route through `CarriedSourceAccess`.
  Direct vanilla `Inventory` mutation is reserved for
  `NeoForgeCarriedSourceAccess` itself.
- Don't call `level.getCapability(Capabilities.ItemHandler.BLOCK, …)`
  or `stack.getCapability(Capabilities.ItemHandler.ITEM)` from outside
  the platform impls. Use `WorldStorageAccess` / `CarriedProvider`.
- Don't import a specific provider's name in higher-level code
  (`SophisticatedBackpackTransferSupport`, `CuriosApi`, …). Executors,
  UI sessions, pickup routing, and the action router must not know
  which mod owns a source — the provider set is open-ended by design.
- Don't write per-caller "find → peek → extract → insert →
  restore-on-failure" loops. That's a symptom — use the action
  pipeline or the direct access APIs and let the abstraction handle
  restore-via-same-layer.
- For pickup / push-out-of-vanilla flows, use
  `CarriedSourceAccess.insertIntoProviders` (walks providers only,
  skipping vanilla lanes). Never name SB's `insertIntoPlayerBackpacks`
  or similar provider-specific helpers from routing code.

**Which mutation API to use.** Three layered entry points — they are
not redundant:

- `InventoryActionExecutor` for UI-triggered verbs (ASSIGN, TRANSFER)
  that need policy + canonicalization + multi-source planning.
- `InventoryMutationRouter` when you already have an
  `InventoryMutationRequest` and need to dispatch to the owning
  extension / host session. Usually only called from inside the
  executor.
- `CarriedSourceAccess` / `WorldStorageAccess` for workflow-level ops
  that don't need policy (DepositExecutor, TakeAllExecutor,
  SlotPickupRouter, UI session handlers).

If you find yourself reaching across layers (e.g. `executeTransfer`
calling `CarriedSourceAccess.extract` directly), stop — pick the right
entry point instead.

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
