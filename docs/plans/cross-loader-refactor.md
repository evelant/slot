# Cross-Loader UI SPI

## Why this exists

LDLib2 only targets modern Minecraft (1.20.5+ networking, NeoForge ≥21). Going to 1.20.1 Forge means either backporting LDLib2 ourselves (multi-month, library we don't own) or rewriting our UI on something else. This plan picks the third path: introduce a small SLOT-internal UI SPI in `common`, with two backends — LDLib2 on modern, vanilla `Screen` + Taffy on legacy. The SPI captures only what we use.

Background analysis: LDLib2's actual layout engine is [Taffy](https://central.sonatype.com/artifact/dev.vfyjxf/taffy) — confirmed by the fact that LDLib2 v2.2.5 ships `taffy-1.1.4.jar` jarjar'd inside its mod jar, alongside a `TaffyLayoutStyle` DSL. Pure Java, Java 17, MIT — same engine on both sides. ScrollerView is not virtualized, so there is no parity gap to replicate. Yoga interop in LDLib2 is legacy / dead weight for our usage. The LDLib2 surfaces that genuinely don't port are its NeoForge networking (replaced with two custom Forge `SimpleChannel` packets) and its capture-phase drag system (which we eliminate entirely on legacy — see drag-scope section).

## SPI shape (`common/src/main/java/dev/imagio/slot/ui/`)

Concrete commitments. Layout types stay raw Taffy (`dev.vfyjxf.taffy.style.*`) — we already import them directly, and a wrapper would just rename the same DSL. `backgroundTexture` / `overlayTexture` / `zIndex` hang off `Element` directly rather than being a separate `Style` type (three fields don't justify a value class).

```
slot.ui.Element                // Taffy NodeId + child list + listeners + render hook
                               //   + backgroundTexture, overlayTexture, zIndex
slot.ui.elements/
    Button       Label        TextField        ScrollerView
slot.ui.event/
    Event        EventKind     Listener     // MOUSE_*, CLICK, KEY_DOWN, CHAR_TYPED, BLUR,
                                            // TICK, LAYOUT_CHANGED, MUI_CHANGED, HOVER_TOOLTIPS
                                            // (no DRAG_* — see drag-scope section below)
                                            // single-pass bubble walk, stopPropagation; no capture phase
slot.ui.tex/
    Texture      ColorRect     ItemStackTex     Icons       // 7 constants we use
slot.ui.rpc/
    ActionChannel            // one channel; backends provide impl
    ActionId<...>            // typed handle, replaces RPCEmitter
slot.ui.screen/
    UiBackend                // service-loaded factory; also exposes viewTagPush(CompoundTag)
                             //   to replace the single DataBindingBuilder.tagS2C use site
    ScreenFactory<MENU>      // wraps PlayerUIMenuType / vanilla MenuType per backend
    MenuHolder               // analogue of IModularUIHolderMenu
```

Drop entirely from our surface: `BindableValue` (used once, replace with imperative update), LDLib2's LSS / stylesheets, animation engine, visual editor hooks, yoga interop overloads. No `Layout`, `Style`, or `ViewModelChannel<T>` SPI types — each was an over-abstraction for our usage (raw Taffy already works; three style fields belong on Element; one tag-push site is one method).

## Architecture

**Modern backend** (`neoforge/.../ui/backend/ldlib2/`): each `slot.ui.Element` wraps an LDLib2 `UIElement`; methods forward 1:1. `ActionChannel` wraps `RPCEmitter`. `viewTagPush` wraps `DataBindingBuilder.tagS2C`. `ScreenFactory` uses `PlayerUIMenuType` / `IModularUIHolderMenu`. LDLib2's drag system stays in use on modern via the existing `DragDropWiring` code — drag is *not* part of the SPI surface; modern code that needs drag continues to talk to LDLib2 directly.

**Legacy backend** (`forge-1.20/.../ui/backend/vanilla/`): each `slot.ui.Element` owns a Taffy `NodeId` directly in a screen-scoped `TaffyTree`. Renders through vanilla `GuiGraphics`. Owns its own single-pass event dispatch (hit-test topmost → walk up parents until `stopPropagation`), scissor stack, scroll/clip math, z-order sort, and a Forge `SimpleChannel` pair (`C2SAction`, `S2CViewModel`). **No drag manager** — every gesture is cursor or keyboard (see drag-scope section). No LDLib1 dependency at any point.

**Both** depend on `dev.vfyjxf:taffy:1.1.4`. On modern this is already transitively present via LDLib2's jarjar; on legacy we declare it as a direct dep.

### Drag-and-drop scope split

The current code has 5 live drag types ([WorkspaceDrags.java](../../neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/WorkspaceDrags.java) — plus `ChestTileDrag` which is dead and gets deleted in Phase 2). On legacy we eliminate drag entirely — replicating screen-level drag interception in vanilla would force a capture-phase event bus and a custom drag manager, both significant new code we'd own forever. Cursor and keyboard cover every gesture:

| Drag type | Modern | Legacy | Reason |
|---|---|---|---|
| `AtlasItemDrag` (re-home) | drag (LDLib2) | cursor pickup → drop on section | Re-home becomes a deliberate two-step gesture; undo covers accidental drops. |
| `IslandDrag` (section reorder) | drag (LDLib2) | ↑/↓ keyboard with section/TOC entry focused | Keyboard reorder is also an accessibility win on modern, but kept optional there. |
| `HotbarSlotDrag` | drag (LDLib2) | cursor only | Stack swap/assign — cursor covers it cleanly. |
| `ChestStackDrag` | drag (LDLib2) | cursor only | Already in cursor's wheelhouse. |
| `KitSlotDrag` | drag (LDLib2) | cursor only | Stack swap. |
| `KitBringDrag` | drag (LDLib2) | cursor only | Pickup-style gesture. |

Modern users keep every gesture they have today, unchanged. Legacy users lose drag entirely; they use cursor pickup/drop and keyboard reorder. Documented UX divergence between loaders.

This collapses the legacy event system to a vanilla-like hit-test + bubble walk. No screen-level capture, no synthetic DRAG_* dispatch, no multi-target ENTER/LEAVE bookkeeping anywhere.

## Phases

Phases are sequenced so each one delivers value or de-risks something independently. After Phase 4 (the decision point) the modern build is shippable on the new SPI with extended cursor coverage; we can stop there if 1.20.1 stops looking worth it.

### Phase 0 — Spike

- Spike only: vanilla 1.20.1 `Screen` rendering a Taffy tree of coloured rectangles, with hit-test + bubble dispatch and a working scroll viewport. Goal: validate Taffy → `GuiGraphics` translation, scissor stacking, the bubble walk, and `compute_layout` perf before committing to the SPI shape.
- **No SPI signatures yet.** Designing the SPI before Phase 1 implements anything tends to bake in imagined needs. Defer the SPI commit to fall out of Phase 1's adapter work, when actual usage has shape.
- **Gate:** spike scrolls a 200-row list at ≥60 fps; bubble dispatch + `stopPropagation` work; layout output for a representative tree matches what LDLib2 produces for the equivalent inputs.

### Phase 1 — Modern backend (LDLib2 adapter) + first-cut SPI

- Write `slot.ui.*` signatures *as the LDLib2 adapter is built*, in `neoforge/.../ui/backend/ldlib2/`. Each method exists because the adapter needs it.
- Wire `UiBackend` via `ServiceLoader`.
- Two- or three-screen smoke test exercising every element type, RPC, view-tag push. (No drag in the SPI; modern drag stays on direct LDLib2 calls.)
- **Gate:** smoke screens behave identically to direct-LDLib2 equivalents; SPI surface is reviewed and frozen before Phase 2.

### Phase 2 — Migrate existing UI to SPI

- Mechanical translation of `neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/` (~14.7k LOC, 30 files) to the SPI.
- Builders moved to `common` where they don't reference loader-specific APIs; remaining loader-specific glue (e.g., menu factory, EMI integration, drag wiring) stays in `neoforge`.
- **No behaviour changes.** Pure refactor.
- **Gate:** parity with main-branch UI on every gameplay path used by Phase 1+2 list view; in-game test of triage panel, drag/drop, search, RPC actions, undo/redo, kit ops, chest ops.

### Phase 3 — Cursor coverage extension + keyboard reorder

This is the actual prerequisite for legacy: every gesture that legacy needs must have a working cursor or keyboard path on modern first, so the cursor code is the well-tested one before the legacy backend depends on it. Modern's drag handlers stay untouched.

- Audit cursor reachability across all gestures. Anywhere cursor *can't* reach today (drop-overlap between kit pages, dropping onto an empty section header for re-home, etc.), extend `WorkspaceCursorState` handling. This is the feature work that ships value to modern users.
- Add keyboard section reorder: with a section card or its TOC entry focused, ↑/↓ moves the section by one ordinal. Wire it on modern first; legacy gets it for free in Phase 6. (Bonus: accessibility win on modern.)
- Verify cursor pickup → drop on a section card or header executes the same re-home as drag does today.

**Gate:** every legacy-bound gesture has a fully working cursor/keyboard path on modern; ChestTileDrag and any other dead drag code deleted.

### Phase 4 — Decision point

After Phase 3, modern is on the SPI and every gesture has a cursor/keyboard path that legacy can reuse. Reassess:
- Is the SPI clean, or did Phase 2 surface compromises that mean Phase 6+ will be painful?
- Is 1.20.1 Forge still a target worth the remaining work?
- Alternative: park the legacy backend, treat the SPI + cursor/keyboard coverage as future-proofing only (which still ships value to modern users).

### Phase 5 — Legacy module scaffolding

- New gradle module `forge-1.20/` (Forge 1.20.1, Java 17). Pin a conservative Forge version in the 47.x range — *not* the latest patch, since 1.20.1 modpacks tend to lag and we want to maximise the set of packs we work in. Pick whatever current TFG Modern is targeting.
- Mirrors `neoforge/` layout: registration, network, mixins, EMI compat stub. (EMI on 1.20.1 has a different API surface than 1.21.x — budget time for the integration glue here, not later.)
- Vanilla deltas: `GuiGraphics` ↔ `PoseStack`, `DataComponents` ↔ NBT, `RegistryFriendlyByteBuf` ↔ `FriendlyByteBuf`. Most non-UI code in `common` either compiles unchanged or needs targeted adapters; build a small `slot.platform` SPI for the deltas.
- **Gate:** mod loads, registers blocks/items/menus, opens an empty SLOT screen (no widgets yet).

### Phase 6 — Legacy backend implementation

Order is risk-first; ship per-component as it stabilises rather than as one big merge. With drag gone from legacy, every step here is well-trodden vanilla territory.

1. **Element + Taffy renderer**. Walk tree, run `compute_layout`, draw via `GuiGraphics` at computed coords. Hit-test by reverse z-order traversal.
2. **Z-index sort**. Sort siblings before render *and* hit-test.
3. **Event dispatch (single-pass bubble)**. Hit-test the topmost element under the cursor, dispatch the event there, walk up parent chain until a listener calls `stopPropagation`. Dispatch kinds: MOUSE_*, CLICK, KEY_DOWN, CHAR_TYPED, BLUR, TICK, LAYOUT_CHANGED, MUI_CHANGED, HOVER_TOOLTIPS. No capture phase. Keyboard goes to focused element first, then bubbles. This is straightforward — no DRAG_* events, no synthetic dispatch, no screen-level interception. Match LDLib2's order-of-listener-invocation only on the bubble path so cross-backend listener registration order produces the same result.
4. **Concrete elements**.
   - `Label`: `Font.draw` with align/wrap (truncate-with-ellipsis logic from our usage).
   - `Button`: `Label` + click event + hover/press visual states + `addPreIcon`.
   - `TextField`: wrap vanilla `EditBox`. Focus, BLUR commit, KEY_DOWN forwarding.
   - `ScrollerView`: viewPort + viewContainer Taffy nodes, scroll wheel, scissor on render, scrollbar element. **Match LDLib2's `setValue` normalization** (memory note).
5. **Action channel + view-tag push**. Two `SimpleChannel` packets: `C2SAction(short id, byte[] payload)` with a 60-row codec table covering Integer/String/Double/Boolean args, and `S2CViewModel(CompoundTag)`. Dispatch through the same `host.session::*` methods.
6. **Texture impls + Icons**. Port `ColorRectTexture`, `ItemStackTexture` (`GhostItemTexture` subclass), and the seven `Icons.*` we use.
7. **Screen factory glue**. Vanilla `MenuType` + custom container. Sidebar's player-inventory overlay needs careful slot wiring on both server and client menus.
8. **Keyboard section reorder hook-up**. The Phase 3 ↑/↓ handlers are in `common`; on legacy, just route `KEY_DOWN` from focused section/TOC entries into them.

**Gate:** the legacy build opens the workspace screen, scrolls a 200-section list, re-home works via cursor pickup → drop on a section, section reorder works via ↑/↓ keyboard on a focused section/TOC entry, cursor pickup/drop covers hotbar/chest/kit interactions, every RPC action fires, view-tag pushes arrive, undo/redo works.

### Phase 7 — Cross-loader product validation

- Side-by-side play-testing on both loaders. Track parity bugs in a single list.
- Performance: 60 fps target on both at the workspace screen with 500-section worst case.
- Document the intentional drag-vs-cursor-vs-keyboard UX delta and any other loader-specific deltas in `docs/loader-deltas.md`.
- **Gate:** zero P0 parity bugs; legacy and modern pass the same scripted UI walkthrough (with the documented drag delta).

## Risks and contingencies

| Risk | Mitigation | Contingency |
|---|---|---|
| Phase 2 refactor regresses modern UI | Phase 1 smoke tests must run before each Phase 2 PR. Land Phase 2 in chunks (per panel builder) rather than one mega-PR | Worst case: revert per-panel PRs without losing whole-effort progress |
| Phase 3 surfaces cursor gaps that are bigger than they look | Audit cursor coverage at the start of Phase 3 before any other work | Expand cursor scope (this is feature work, but it's needed for legacy anyway and ships value to modern) |
| Two-step cursor re-home on legacy is felt as worse UX than drag | Make sure undo is one keystroke and visually obvious; tutorial/tooltip on first re-home | Documented UX delta; no engineering response unless players actually complain |
| Keyboard section reorder feels clunky on legacy | Spec it for both loaders in Phase 3 so modern users exercise it too — flushes UX issues out before legacy ships | Adjust key bindings; consider held-modifier + click as a secondary path |
| Taffy layout perf differs subtly between versions / between LDLib2's wrapper and our direct use | Phase 0 spike spot-checks against equivalent LDLib2 screen for layout output equivalence | Pin the same Taffy version on both sides; diff `compute_layout` outputs in a test |
| LDLib2 quirks leak through the SPI | Treat each known quirk (`ScrollerView.setValue` normalization, `Button.text` absorbing hit-tests, `project()` needs `currentTick` for affinity decay) as a documented SPI invariant; test both backends against it | Add quirk-specific tests in `common` that both backends run |
| 1.20.1 vanilla deltas surface gameplay-not-just-UI work | Phase 5 forces this discovery before any UI work begins | If non-UI deltas are too large, abort cross-loader and keep SPI + Phase 3 cursor/keyboard coverage as future-proofing |

## Out of scope

- LDLib2 visual editor, LSS stylesheets, animation engine, configurator system.
- `BindableValue` as a reactive primitive (the one site is rewritten imperatively).
- Yoga interop overloads (`YogaDisplay`, `StyleSizeLength`, `YogaCodecs`).
- Backporting LDLib2 itself.
- **Drag of any kind on the legacy backend** (intentional — cursor + keyboard cover everything; eliminates the entire drag manager and capture-phase event bus).
- Drag-as-cursor-alias on modern (originally Phase 3; dropped — modern keeps drag using LDLib2 directly, with cursor as an independent parallel path).
- Other loaders (Fabric, Quilt). The SPI shape doesn't preclude them but no work is planned.
