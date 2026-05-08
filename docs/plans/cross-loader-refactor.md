# Cross-Loader UI SPI

Last updated: 2026-05-07

ADR: [0006-cross-loader-legacy-forge.md](../decisions/0006-cross-loader-legacy-forge.md).

## Why this exists

LDLib2 only targets modern Minecraft / NeoForge. Running SLOT on
Minecraft 1.20.1 Forge means either backporting LDLib2 ourselves,
rewriting the UI directly on vanilla screens, or introducing a small
SLOT-owned UI SPI with two backends. This plan chooses the SPI path.

The important Phase 0 finding is validated: Forge 1.20.1 can render a
scrollable Taffy tree through vanilla `Screen` / `GuiGraphics`, with
hit-test bubbling and `stopPropagation` behaving as expected. The
throwaway spike source that proved this has been deleted now that the
production `ForgeSlotUiTree` backend exists. Taffy
(`dev.vfyjxf:taffy:1.1.4`) is pure Java and is already the layout
engine LDLib2 ships jarjar'd.

The next risk is **not** another UI mock. It is making shared SLOT domain
code compile and behave under Forge 1.20.1 before a large UI migration
locks in the wrong boundaries.

## Target Shape

Modern build:

- Minecraft `1.21.1`, Java `21`, NeoForge, LDLib2 backend.
- Keeps LDLib2 drag support and LDLib2 menu/sync plumbing where the
  backend owns it.

Legacy build:

- Minecraft `1.20.1`, Java `17`, Forge `47.x`, vanilla screen backend.
- Uses direct Taffy layout, vanilla `GuiGraphics`, Forge networking, and
  Forge capabilities.
- Does not implement drag. Cursor/drop and keyboard paths cover legacy
  interactions.

Shared code:

- Workspace builders move behind `slot.ui.*` only after platform compile
  risk is retired.
- Inventory semantics remain in `common`.
- Loader-specific APIs stay behind narrow platform adapters.

## Semantic Authority

The existing NeoForge implementation is the behavior oracle for the
port. Forge is another adapter, not a second product surface. When a
gesture or command is ported:

- First trace the NeoForge path and write or update the common decision
  table / command service from that behavior.
- Platform UI should reduce to `raw event -> common Context -> common
  Decision -> backend send`.
- A recognized gesture must return a common action or a common status.
  It must not return `NONE` and let backend code invent a fallback.
- Backend-specific branches for gesture semantics are forbidden unless
  this plan explicitly lists the loader delta. The current intentional
  delta is only that legacy Forge omits drag; cursor, keyboard, and
  catalog actions must preserve the same meaning.
- Missing view-model data must fail closed with a useful diagnostic
  rather than falling back to older fields or inferred behavior.
- Click-to-select is not an inventory command mode. Hover/focus,
  cursor pickup, shift-click, digit hotkeys, and drag/drop may initiate
  commands; hidden "selected item" state must not route later hotbar or
  section clicks.

## SPI Shape

Do not freeze this before migrating at least one hard panel. The first
cut should be grown from actual adapter work, but the expected surface is:

```text
slot.ui.Element
  Taffy layout handle, child list, listener registration, render hook,
  backgroundTexture, overlayTexture, zIndex, allowHitTest, focus/display state

slot.ui.elements
  Button, Label, TextField, ScrollerView

slot.ui.event
  Event, EventKind, Listener
  MOUSE_*, CLICK, KEY_DOWN, CHAR_TYPED, BLUR, TICK, LAYOUT_CHANGED,
  MUI_CHANGED, HOVER_TOOLTIPS
  Minimal capture/pre-dispatch is allowed for key/char/global shortcuts.
  DRAG_* is not part of the cross-loader SPI.

slot.ui.tex
  Texture, ColorRect, ItemStackTex, Icons

slot.ui.action
  WorkspaceActionCatalog, WorkspaceActionId, WorkspaceActionChannel
  One shared action registry consumed by both LDLib2 RPC and Forge packets.

slot.ui.screen
  UiBackend, ScreenFactory<MENU>, MenuHolder/viewTagPush equivalent
```

Drop from the shared surface: LDLib2 LSS/stylesheets, visual editor,
animation engine, configurator system, Yoga overloads, and
`BindableValue` as a general reactive primitive. The one view-model tag
push can be an explicit backend method.

### Drag Scope

Modern retains existing LDLib2 drag. Legacy omits drag entirely.

Common builders must not call LDLib2 drag APIs directly. Use an optional
interaction/drag hook owned by the backend:

- modern hook unwraps the native LDLib2 element and installs existing
  `DragDropWiring`
- legacy hook is a no-op because cursor and keyboard paths are the
  supported interaction model

| Current drag payload | Modern | Legacy |
|---|---|---|
| `AtlasItemDrag` | LDLib2 drag | cursor pickup -> drop on section |
| `IslandDrag` | LDLib2 drag | focused section / TOC keyboard reorder |
| `HotbarSlotDrag` | LDLib2 drag | cursor/drop |
| `ChestStackDrag` | LDLib2 drag | cursor/drop |
| `KitSlotDrag` | LDLib2 drag | cursor/drop |
| `KitBringDrag` | LDLib2 drag | cursor/drop |

## Phases

### Phase 0 — Spike (validated)

Validated in the current `forge-1.20` module:

- Forge 1.20.1 module compiles with Java 17.
- Taffy direct dependency and jarJar packaging compile.
- Vanilla `Screen` renders a Taffy tree through `GuiGraphics`.
- 200-row scroll viewport, hit testing, bubble dispatch, and
  `stopPropagation` work in-game.

The Phase 0 spike package has been deleted. Do not restore or extend it;
future renderer checks should be small production-backend fixtures or
tests against `ForgeSlotUiTree`.

### Phase 0.5 — Shared Platform Compile Gate

Goal: discover and retire 1.20.1 blockers before the UI migration.

Current probe:

- `./gradlew :forge-1.20:compileSharedProbeJava` compiles the whole
  shared `dev.imagio.slot` common source tree against Forge 1.20.1 /
  Java 17, plus Forge-side probe adapters.
- `forge-1.20` `main` now also compiles the shared common source tree,
  with production Forge 1.20 implementations of `SlotStackAccess` and
  `SlotResourceAccess` installed during mod bootstrap. The separate
  shared probe remains as an explicit cross-loader compile gate.
- The first real adapter seams are `SlotStackAccess` and
  `SlotResourceAccess`: modern installs NeoForge implementations using
  1.21 component/resource APIs; the Forge probe validates the matching
  1.20 tag/resource APIs.
- Common Java 21 syntax dependencies that blocked Java 17 compilation
  are retired from the shared tree. Remaining known loader work is no
  longer common compilation; it is platform implementation: Forge
  capabilities/events/networking/storage-id persistence and the legacy
  UI backend.

- Make `forge-1.20` compile a shared-source slice from `common`.
- Start with non-client domain packages, then widen only when green.
- Introduce a small `slot.platform` adapter only for real API deltas:
  `ResourceLocation` construction, stack equivalence, item identity
  fingerprinting, stack/NBT serialization, and registry access.
- Split or adapt platform storage code:
  - NeoForge attachments -> Forge block-entity persistence or Forge
    capability-backed storage id
  - NeoForge item/block capabilities -> Forge `LazyOptional` capability
    resolution
  - NeoForge events/networking -> Forge event bus + `SimpleChannel`
- Keep all inventory semantics in `common`; platform code only exposes
  primitives and adapters.

Gate:

- A Forge 1.20.1 compile task covers the selected shared source.
- The first platform adapter tests/fixtures cover item id, stack
  equality, and identity fingerprint behavior.
- No UI SPI files are introduced just to satisfy imagined future needs.

### Phase 1 — Shared Workspace Action Catalog

Goal: replace ad hoc LDLib2 `RPCEmitter` shape with a backend-neutral
catalog before Forge packets are written.

Current state:

- `dev.imagio.slot.ui.action` defines stable workspace action ids,
  argument schemas, validation, typed packet values, a session/menu
  envelope, and a backend-neutral `WorkspaceActionChannel`.
- `WorkspaceRpcDispatcher` implements the channel for modern LDLib2 RPC
  and validates every RPC registration against the shared catalog at UI
  creation. The dispatcher send helpers now route through
  `WorkspaceActionChannel.send(...)` instead of calling individual
  emitters directly.
- `WorkspaceActionPacketCodec` owns argument encoding behind a tiny
  buffer interface. NeoForge and the Forge 1.20 shared probe both have
  `FriendlyByteBuf` wrappers, so packet encoding rules stay common
  while loader networking stays local.
- Forge 1.20 now registers a production `SimpleChannel` payload for
  common workspace action packets. The handler validates packet shape
  through the shared catalog, validates the session/menu envelope against
  a server-side Forge workspace session registry, and routes safe
  metadata actions through `SlotWorkspaceCommandService`. Forge now
  installs carried/world storage accessors and binds the first guarded
  `TRANSFER` path for built-in main/hotbar targets through
  `InventoryActionExecutor`, plus identity-to-hotbar, hotbar-return,
  hotbar-to-section, kit, desired-count, chest metadata, deposit/take,
  cursor, and cross-surface adapters for the first belt/workflow
  interactions. Verbs outside that basic workspace set still fail closed
  until their Forge session adapters are explicitly ported. The Forge `G` screen opens a server-side workspace session and
  syncs typed search through `SET_SEARCH_QUERY` on the same catalog path.
  Forge also registers `/slot test populate <profile>` and `/slot test
  clear` for carried-inventory/workflow/chest testing; chest placement
  now uses Forge persistent block-entity data for storage ids.
- The same Forge channel now has a server-to-client session view-model
  payload. The server owns a Forge workflow runtime, projects carried
  player inventory through the common `SlotWorkspaceViewModel` pipeline
  with bounded auto-home and Forge 1.20 `ItemStack` NBT encoding, then
  sends that view, including hotbar/offhand and claimed-chest ghost
  projection, to the direct Taffy/GuiGraphics screen. This replaces the
  temporary narrow debug projection. Forge now observes manual vanilla
  chest deposits through the same common close-delta helper as NeoForge,
  records learned affinity for those deposits, and reconciles persisted
  chest claims through the shared reconciliation helper. The Forge
  workspace screen now also mounts the first common kit-rack panel for
  save/update, activate/deactivate, page management, and non-drag gather
  actions. Rich chest panels remain pending UI adapters.

- Define action ids, argument schemas, handler binding, and validation in
  one catalog.
- Modern adapter sends catalog actions through LDLib2 RPC.
- Legacy adapter sends the same action ids through Forge `SimpleChannel`.
- Packets include enough session/menu identity to reject stale or wrong
  menu requests server-side.

Gate:

- Existing modern UI still sends every current workspace action.
- Tests assert catalog uniqueness, action argument validation,
  packet-codec round trip, and stale-session / wrong-menu rejection.
- Phase 1 action transport now covers the basic Forge mutation set:
  deposit/take, cursor pickup/cancel/smart-deposit/drop, and
  cross-surface host-slot flows route through common services instead of
  Forge-only business logic. Session validation, safe metadata command dispatch, Forge storage
  accessors, guarded built-in transfer, identity-to-hotbar /
  hotbar-return, kit/desired-count dispatch, chest metadata dispatch,
  and carried/chest common projection are live. Forge populate/clear
  commands cover carried inventory and claimed chest test loops.

### Phase 2 — First-Cut UI SPI + LDLib2 Backend

Goal: grow the SPI from real migration pressure, not a blank-screen
abstraction.

Current state:

- `dev.imagio.slot.ui.spi` defines the first narrow element/layout/text
  event tree, and the LDLib2 backend renders it through
  `LdlibSlotUiRenderer`.
- The first production migration is the main wall section shell, card
  shell, Recents strip, hotbar belt, non-drag kit rack, and active chest
  strip, via `WallSectionUiBuilder`,
  `WallSectionHeaderUiBuilder`, `WallCardUiBuilder`,
  `RecentsStripUiBuilder`, `HotbarBeltUiBuilder`, and
  `KitRackUiBuilder` / `ActiveChestStripUiBuilder`. It covers section layout,
  card width/search
  chrome, Recents icon layout, hotbar/offhand slot chrome, kit summary /
  page / gather controls, chest claim/forget, text/count layout, click vs
  mouse-down event separation, edit actions, section grid drop targets, a
  shared fallback atlas-card body (item icon, count badge, chest-presence
  pips, desired marker, wayfinding strip), and backend-owned modern
  section/card/tooltip
  hooks. NeoForge still overrides the body with its richer LDLib2 card
  renderer and keeps its richer LDLib2 kit rack for drag/context-menu
  affordances; Forge uses the shared non-drag kit rack and fallback card
  body until those richer panels migrate. Forge now also renders the shared
  tooltip metadata and a Forge-native in-world wayfinding chest glow driven
  by the same common `WayfindingTarget` projection.
- A compact `StoragePanelUiBuilder` exists from the Forge parity pass
  and has tests for proximate/search-driven chip visibility and cursor
  deposit, but it is intentionally not mounted right now. NeoForge hides
  the old nearby-chest chip stack too; item-finder affordances live on
  cards/HUD, and active chest management lives in the per-chest bar until
  product direction says otherwise.
- The removed docked Triage panel is intentionally not a migration
  target. Auto-home plus Recents are the live flow; Triage survives only
  as older naming around the auto-home candidate/suggestion pipeline and
  legacy routing sentinels.
- Gesture semantics are now actively moving to common policy/command
  classes such as `WallCardTransferGesturePolicy`,
  `WorkspaceBeltCommandService`, and `WorkspaceSearchQuery`. NeoForge
  remains the behavior source of truth; Forge and NeoForge should
  dispatch common decisions rather than keeping local shift-click /
  wheel/search/hotbar fallbacks.

- Build the LDLib2 backend and SPI while porting one hard panel first.
  Preferred candidates: atlas card/list section, context menu, or kit
  rack, because they exercise events, text, icons, state, and actions.
- Add optional backend interaction hooks for modern-only drag.
- Keep direct LDLib2 code only in the backend and modern glue.

Gate:

- The migrated hard panel behaves identically on modern.
- The SPI surface is reviewed after real panel pressure, then expanded
  panel by panel.

### Phase 3 — Migrate Modern UI To SPI

Goal: move the existing LDLib2 UI builders without behavior changes.

- Translate `neoforge/.../screen/ldlib/` builders in chunks.
- Move UI-neutral builders to `common` only when they no longer import
  loader APIs.
- Keep menu factory, EMI integration, screen mounting, networking, and
  modern drag hook in `neoforge`.

Gate:

- Modern in-game parity for search, wall cards, context menus, hotbar,
  kits, chest panels, undo/redo, wayfinding, and current drag paths.
- Modern compile/test tasks stay green throughout.

### Phase 4 — Cursor Coverage + Keyboard Reorder

Goal: make every legacy interaction path work on modern before the
legacy backend depends on it.

- Audit all gestures that currently require drag.
- Extend cursor/drop handling where needed: section header re-home, empty
  section drops, kit page/slot edge cases, chest/kit/hotbar cursor paths.
- Add keyboard section reorder for focused section cards and TOC entries.
- Delete dead drag payloads such as `ChestTileDrag` once verified unused.

Gate:

- Every legacy-bound gesture has a modern cursor or keyboard path.
- Modern drag remains available and unchanged.

### Phase 5 — Decision Point

Reassess after modern runs on the SPI and legacy-bound gestures are
usable:

- Is the SPI clean enough to own?
- Did Phase 0.5 expose manageable Forge 1.20.1 platform deltas?
- Is Forge 1.20.1 still worth the backend work?

If no, park the legacy backend and keep the modern SPI/action/catalog
work only if it is still paying for itself.

### Phase 6 — Legacy Backend

Order risk-first:

Current early backend test point:

- `ForgeSlotUiTree` renders the common SPI directly through Taffy +
  vanilla `GuiGraphics`, including color rects, labels, buttons, item
  icons, z-index ordering, hit-test bubbling, click separation,
  hover/tick dispatch, scissor clipping, and a scroll viewport.
- The Forge debug key (`G`) opens `ForgeWorkspaceSpiDebugScreen`, a
  session-backed wall/Recents/belt view built from the same common
  `WallSectionUiBuilder`, `WallCardUiBuilder`, `RecentsStripUiBuilder`,
  and `HotbarBeltUiBuilder` used by the migration. It waits for the
  session-backed server view instead of rendering a local fixture, so
  projection/session failures are visible. Card pickup uses the real
  menu cursor; background/hotbar drops route through the shared cursor
  command service. Forge installs the same item-id ghost stack resolver
  hook as NeoForge so common view-model ghost synthesis can render item
  icons on both loaders.
- Forge now also mounts an early vanilla-container sidebar on
  `AbstractContainerScreen` hosts. The sidebar opens its own workspace
  session against the live host menu, renders the common active chest
  controls, non-drag kit rack, Recents, wall cards, search, card gestures,
  and hotbar belt, and lets vanilla host slots continue
  receiving input outside the sidebar. Host-menu changes are refreshed
  through a Forge transport sync message, not a shared inventory action.
  The full-screen debug surface and container sidebar now share
  `ForgeWorkspaceSurface`, so Forge has one local controller for
  view-model application, widget composition, card gestures, search,
  kit/hotbar/storage/active-chest contexts, and action sends. Forge also
  renders HUD wayfinding chips and in-world chest outlines from the latest
  synced workspace view. It is still a first-cut adapter: NeoForge drag
  parity is out of scope for Forge.

1. Element tree, direct Taffy renderer, scissor stack, z-index render and
   hit-test ordering.
2. Event dispatch with bubble plus minimal pre-dispatch/capture for
   global key/char shortcuts. No drag events.
3. `Label`, `Button`, `TextField`, `ScrollerView`.
4. Textures and icons, including item stack and ghost item rendering.
5. View-model tag push.
6. Forge `SimpleChannel` action transport backed by the shared catalog.
7. Vanilla `MenuType` / `AbstractContainerMenu` / screen factory glue.
8. Sidebar overlay and host-menu coexistence.

Gate:

- Legacy workspace opens, scrolls a representative worst-case wall, and
  runs the same scripted UI walkthrough as modern except for documented
  drag-vs-cursor deltas.

### Phase 7 — Cross-Loader Product Validation

- Side-by-side playtesting on both loaders.
- Performance target: 60 fps on the workspace screen with the worst
  practical wall data set.
- Document intentional loader deltas in `docs/loader-deltas.md`.
- Track parity bugs in one list, not per-loader folklore.

Gate:

- Zero P0 parity bugs.
- Storage, carried-source, and item identity behavior match across
  loaders for vanilla, Sophisticated Backpacks/Core, Tom's Storage, and
  the supported test pack surface.

## Risks And Contingencies

| Risk | Mitigation | Contingency |
|---|---|---|
| Forge 1.20.1 platform deltas are larger than UI work | Phase 0.5 compile/storage gate before SPI migration | Park legacy before sunk UI cost |
| SPI turns into a private LDLib2 clone | Grow it from one hard panel at a time | Keep backend-specific hooks instead of abstracting unused features |
| Modern drag leaks into common builders | Backend interaction hook owns drag installation | Move affected builder code back to modern glue until cursor path exists |
| Legacy cursor UX is weaker than modern drag | Prove cursor/keyboard paths on modern first | Document loader delta; add non-drag shortcut only if playtest demands it |
| Loader gesture semantics drift | NeoForge is the behavior oracle; gesture decision tables live in common with parity tests | Remove backend fallbacks and fail closed until the common action exists |
| Action packet surface becomes unsafe | Shared catalog + server-side session/menu validation | Fail closed with diagnostics on stale or unsupported requests |
| Taffy behavior differs between direct and LDLib2 wrapper | Equivalence fixture and shared version pin | Treat LDLib2 quirks as explicit SPI invariants |

## Out Of Scope

- Backporting LDLib2.
- LDLib2 visual editor, LSS stylesheets, animation engine, configurator
  system, or Yoga interop.
- Drag support on the legacy backend.
- Forcing modern users off LDLib2 drag.
- Fabric or Quilt.
