# List View — Phase 3a: Embed Wall as Sidebar in Vanilla Container Screens

> **Closed (2026-05-05) alongside the parent
> [list-view.md](list-view.md).** Shipped: **3a.1** (sidebar embed
> on every `AbstractContainerScreen`, 2026-05-04), **3a.2 direction
> A** (wall → vanilla slot via drag / shift+click / shift+wheel,
> 2026-05-04). The remaining sub-phases — **3a.2 direction B**
> (vanilla cursor → wall card), **3a.3** (broaden host coverage past
> plain chests / `AbstractContainerScreen` allow-list), **3a.4**
> (hard-custom screens like AE2 / RS terminals), and **EMI exclusion
> area registration** — are not actively tracked. Spin a fresh plan
> in `docs/plans/` if playtest signal demands them; don't reopen
> this one. Phase 3b (hide vanilla 36-slot band) is deferred as a
> separate experiment and tracked from `docs/plans/current.md`.

Sibling to [list-view.md](list-view.md). This doc captures the
structural mount strategy used to land 3a.1 + 3a.2.A; preserved as
a design reference for the embed pattern (LDLib2 `ScreenMixin` /
`ContainerEventHandlerMixin` reuse, `IModularUIHolderMenu`
attachment, packet routing through `player.containerMenu`).

## Goal

When the player opens any non-SLOT `AbstractContainerScreen` (chest,
crafting table, furnace, anvil, machine GUI), render the SLOT wall as a
left-side sidebar so the player can act on carried inventory + homed
sections + kits without having to detour through the vanilla 36-slot
band. Vanilla menu logic stays intact (so chests, crafting, machines
keep working); the sidebar is purely additive.

## Strategy: embed as renderable widget

We mount a standalone `ModularUI` as a child of the vanilla screen via
`Screen.addRenderableWidget(...)`. LDLib2's existing mixins do most of
the lifecycle work for us: `ScreenMixin` walks `screen.children()`
looking for `IModularUIHolder` for tick / removed / keyPressed
([ScreenMixin.java:30](../../../reference/LDLib2/src/main/java/com/lowdragmc/lowdraglib2/core/mixins/ui/ScreenMixin.java#L30)),
and `ContainerEventHandlerMixin` does the same for mouseDragged /
mouseMoved ([ContainerEventHandlerMixin.java:22](../../../reference/LDLib2/src/main/java/com/lowdragmc/lowdraglib2/core/mixins/ui/ContainerEventHandlerMixin.java#L22)).
We do not replace the vanilla menu, do not write new mixins, and do not
overdraw via `ScreenEvent.Render`.

Why not the alternatives:

- **Server-side menu wrap** (the pattern the existing SLOT workspace
  uses on `InventoryScreen`) is unviable here. Replacing the chest /
  crafting / machine menu would break the gameplay logic those menus
  encode.
- **Custom mixin into AbstractContainerScreen.render / mouseClicked**
  reimplements what LDLib2's children-walking mixins already do
  cooperatively, and stacks mixins on mixins for no gain.
- **ScreenEvent.Render overdraw + manual hit-test** would force us to
  drive ModularUI's render and event pumps by hand, fighting LDLib2's
  expectations that the widget tree is a registered child.

Key facts that make the embed approach work:

- `ModularUI` constructs without a menu — its second constructor takes
  just a `UI` and an optional `Player`
  ([ModularUI.java:164](../../../reference/LDLib2/src/main/java/com/lowdragmc/lowdraglib2/gui/ui/ModularUI.java#L164)).
- `UISyncManager` only needs the player reference; it dispatches via
  `PacketDistributor` independently of menu lifecycle
  ([UISyncManager.java:57](../../../reference/LDLib2/src/main/java/com/lowdragmc/lowdraglib2/gui/sync/UISyncManager.java#L57)).
- `IModularUIHolder` is a tiny interface
  ([IModularUIHolder.java](../../../reference/LDLib2/src/main/java/com/lowdragmc/lowdraglib2/gui/holder/IModularUIHolder.java));
  a small wrapper that implements it plus `GuiEventListener` and
  `Renderable` is all we need to register as a child.

## Server-side data pump: parallel hidden menu (Option A)

Resolved: open a hidden second LDLib2 `ModularUIContainerMenu` in
parallel with the vanilla host menu. Vanilla menu drives the host's
gameplay (chest contents, crafting matrix, machine slots); the parallel
LDLib2 menu drives the sidebar's view-model sync, reusing every line of
`SlotWorkspaceUiSession`, `WorkspaceRpcDispatcher`, and the existing
view-model push pipeline. The hidden menu has no slots, so it doesn't
fight the host menu for slot indices, and the player never sees it
because we don't show it as a screen — we drive the LDLib2 sync layer
only and render the widget tree as a child of the vanilla screen.

Fallback to **Option B** (menu-free per-player tick service that drives
`UISyncManager` directly) only if the parallel-menu approach causes real
friction — e.g. ordering issues with vanilla menu close, sync packets
racing each other, or `MenuType` registration overhead per host screen.
Option A reuses more existing code, so start there.

## Layout: lean on Taffy, not absolute positioning

The current wall widget tree mixes flex containers with abs-positioned
overlays (kit rack, context menu, deposit/gather action cluster) keyed
to the workspace screen's outer bounds. Those positions will be wrong
when the wall is constrained to a left strip inside a chest screen.

The fix is to stop reaching for absolute positioning and let LDLib2 +
Taffy do the heavy lifting:

- Build a sidebar-mode root that flex-columns the wall, TOC, belt, and
  kit chrome inside a fixed-width column, with overlays attached as
  flex-positioned descendants instead of viewport-anchored absolutes.
- Where overlays today use `position: absolute` against the screen,
  re-anchor against the sidebar root (or use Taffy's relative flow with
  z-index where they really need to float over siblings).
- Treat the standalone-mode `WorkspaceUi` root and the sidebar-mode root
  as two callers of one shared "wall column" builder; keep panel
  builders (`ListWallPanelBuilder`, `TocPanelBuilder`, `LeftColumnBuilder`,
  the belt builder) ignorant of which mode they're in.

We accept some refactor cost to remove abs-positioning from the
existing surface. The cost is paid once and the code is cleaner in both
modes.

## Width policy + the vanilla-shift question

Default: ratio of viewport width with min/max clamps.

- **Width** = `clamp(viewportWidth / 3, 280, 360)` px. Pin the exact
  numbers during implementation and update this doc if they move.
- The sidebar always sits at the screen's left edge, full height.

The harder question is what happens to the vanilla GUI underneath.
`AbstractContainerScreen.init()` centers itself by setting
`leftPos = (width - imageWidth) / 2`. Without intervention the vanilla
GUI stays centered and our sidebar overlaps its leftmost columns —
unacceptable, the player loses access to the leftmost slots.

Two options for resolving the overlap:

1. **Shift the vanilla GUI right.** After `ScreenEvent.Init.Post`,
   bump `leftPos` rightward so `leftPos >= sidebarWidth + gap`. Plain
   `AbstractContainerScreen` subclasses (vanilla chest / crafting /
   furnace / anvil / brewing stand) honor `leftPos` consistently, so
   they recenter in the remaining space cleanly. Mods that bypass
   `leftPos` and hardcode pixel coordinates render incorrectly under
   this approach.
2. **Don't shift; sidebar overlays the leftmost host pixels.** Safer
   for hostile screens, but the player loses the leftmost slot column.

Decision: ship Option 1 (shift `leftPos` right) for vanilla
`AbstractContainerScreen` subclasses, since heavy modpacks like
TerraFirmaGreg lean heavily on machines with custom GUIs that often
*do* override layout. Add a per-screen-class allow-list config so we
can opt-out specific hostile screens without code changes. Hard-custom
screens (AE2 / Refined Storage terminals; see § Hard-custom screens)
are an entirely separate path.

## Hard-custom screens — start with an EMI-coexistence study

Some terminals (AE2's `AETerminalScreen`, Refined Storage's grid)
aren't `AbstractContainerScreen` at all — they extend `Screen` directly
and reimplement slot rendering / inventory controls from scratch. These
matter for our target modpack profile (TerraFirmaGreg, GregTech-style
content), so they need real coverage, but as **3a follow-up**, not as
part of the first 3a cut.

Starting point for the follow-up investigation: how does EMI manage to
get its panels working inside AE2 / RS terminal screens? EMI is
known to integrate cleanly there, and its registration + plugin model
(`EmiExclusionArea`, `EmiPlugin`, the per-screen integrations under
`reference/classification/emi_and_plugins/emi/plugins/`) is the cleanest
existing pattern we could adapt. Concretely:

- Read EMI's AE2 and RS plugins to see whether they hook the screen
  directly, register exclusion zones, or use a shared screen-handler
  registry.
- Decide whether SLOT can ride on EMI's plugin layer (would require an
  optional EMI dependency) or needs a parallel registry of its own.

If we end up with our own per-mod plugin system, the surface area is
small: one method per terminal that returns `LayoutBounds` (where the
sidebar fits) and overrides the vanilla `leftPos`-shift behavior.

## Phased breakdown

### Phase 3a.0 — investigation (done in this doc)

Strategy chosen, open questions answered, plan written. Skeleton hook
already exists at
[`SlotContainerSidebar.java`](../../../neoforge/src/main/java/dev/imagio/slot/neoforge/client/screen/SlotContainerSidebar.java)
and currently diagnostic-logs only.

### Phase 3a.1 — minimal embed (SHIPPED 2026-05-04)

Code that landed:

- **`SlotContainerSidebar`** (client) listens to
  `ScreenEvent.Init.Post` for non-SLOT `AbstractContainerScreen`,
  fires `SidebarOpenPayload`, and mounts the workspace via
  `SlotSidebarClientUi.mount`. Closes via `ScreenEvent.Closing`.
- **`SlotSidebarUiHandle` + `SlotSidebarUiHandles`** (server)
  per-player registry. The handle constructs
  `SlotWorkspaceUiSession` + `ModularUI` and **attaches the
  modular UI to the player's host menu** via LDLib2's
  `IModularUIHolderMenu.setModularUI`. **This attachment is
  load-bearing**: `PacketModularUISync.executeServer/Client`
  routes through `player.containerMenu instanceof
  IUISyncManagerHolder`, so without attaching the host menu
  carries no sync manager and packets are silently dropped.
  Once attached, vanilla's `broadcastChanges` (called every
  server tick) routes through LDLib2's
  `AbstractContainerMenuMixin` and ticks our `ModularUI` for
  free — no per-player `ServerTickEvent.Post` driver needed.
- **Two new mixins** in `neoforge/.../mixin/`:
  `ScreenInvoker.slot$addRenderableWidget` (exposes the
  protected `Screen.addRenderableWidget`) and
  `AbstractContainerScreenAccessor.slot$setLeftPos` /
  `slot$findSlot`.
- **Two new payloads**: `SlotSidebarOpenPayload` /
  `SlotSidebarClosePayload` + handlers, registered in
  `SlotNetworking`. Protocol bumped to "23".
- **Width policy** (revised twice): final landed at
  `WORKSPACE_WIDTH_PX = 414` (wall 242 + gap 4 + leftCol 140 +
  padding 28). Initial cut targeted `clamp(width/3, 280, 360)`
  per the original plan; got revised to half-screen, then
  pinned to a fixed natural content width once the layout
  unification (below) made standalone and sidebar share the
  same widget tree.
- **`leftPos` shift** anchors the host GUI flush against
  `sidebarWidth + 8` so leftover space falls naturally to the
  right where EMI sits (centering the host in the remaining
  space leaves an awkward corridor).
- **Layout-mode unification** (mid-3a.1, after the user
  pointed out the duplication): initial cut introduced
  `WorkspaceLayoutMode { STANDALONE, SIDEBAR }` enum threaded
  through 12 files with 21 SIDEBAR/STANDALONE branches. All
  collapsed to a single widget tree on a unified composition
  path; enum deleted, `wallLeftReservation` deleted, dead
  constants removed (`WALL_TOP_PAD_PX`, `WALL_BOTTOM_PAD_PX`,
  `RESERVED_WIDTH`, `LEFT`, `WIDTH`). The runtime branching
  the cross-surface gestures still need (sidebar vs not) reads
  `SlotSidebarClientUi.isActive()` instead.
- **Centering on standalone** — since `ModularUI.init` reads
  the root's WIDTH style for centering math (not the rendered
  size), a fixed-width root would have been ideal but ran into
  the upstream LDLib2 typo (see below). Workaround: root stays
  `widthPercent(100).heightPercent(100)`; new inner `content`
  wrapper carries `maxWidth(WORKSPACE_WIDTH_PX) +
  marginHorizontalAuto` to center.
- **Belt + kit rack as root-level slots** — moved out of the
  centered content wrapper into root-level full-width sibling
  slots (`beltSlot` / `kitRackSlot` on the controller) so the
  hotbar covers the vanilla one in sidebar mode.

Discoveries / things the original plan missed:

- **LDLib2 `ModularUI.init` typo** (line 563):
  `Float.isNaN(layoutWidth)` is used in the second NaN check
  instead of `layoutHeight`. When the root has a fixed WIDTH
  style, both axes' available space become `MAX_CONTENT`,
  scroller never engages, belt overflows. Worked around by
  keeping root at `widthPercent(100)`; **user is filing the
  upstream issue**.
- **`PacketModularUISync` routes through `containerMenu`**.
  See above — drove the design decision to attach via
  `IModularUIHolderMenu` rather than running a parallel hidden
  menu (which the original plan called for).
- **`UIElement.isMouseDown(button)` is global**, not
  per-element — reads `modularUI.lastMouseDownButton`. So any
  held mouse button matched every drag source's `MOUSE_LEAVE`
  guard. Fixed via new `DragDropWiring.mouseIsHeldOnSource`
  helper that AND-checks the global flag with "did
  `lastMouseDownElement` walk up to me?". Applied to all five
  drag sources.
- **EMI exclusion area registration** deferred — needs an EMI
  plugin entrypoint (compile dep + `@EmiEntrypoint`) which is
  larger than expected for a one-commit add. Logged as
  outstanding.

### Phase 3a.2 — cross-surface drag

**Direction A (wall → vanilla slot) SHIPPED 2026-05-04.** Three
gestures route to the host menu when sidebar is active:

1. **Drag wall card → release on vanilla slot.**
   `ScreenEvent.MouseButtonReleased.Pre` listener in
   `SlotContainerSidebar` finds the slot under cursor via the
   new `findSlot` invoker and fires
   `WorkspaceRpcDispatcher.sendCrossSurfaceDropOnHostSlot`.
   Skips player-side slots (no-op shuffle).
2. **Shift+click on wall card.** `AtlasCardBuilder` checks
   `SlotSidebarClientUi.isActive() && target.carried()` —
   when true, fires `sendCrossSurfaceQuickMove(identity, 1)`
   instead of the chest-take path.
3. **Shift+wheel-up on wall card.** Same branch with
   `magnitude` from the wheel ticks.

Server side: both new methods on `SlotWorkspaceUiSession`
(`crossSurfaceDropOnHostSlot`, `crossSurfaceQuickMoveAtlas`)
**use `CarriedSourceAccess.findIdentity` + `extract`, not
`menu.clicked`**. The first cut tried `menu.clicked(slot, 0,
ClickType.PICKUP)` and hit a wall: `findPlayerSlotInMenuWithIdentity`
only scanned `slot.container == player.getInventory()` slots,
which misses Sophisticated Backpacks contents entirely (and any
future provider-based carried source). Items inside backpacks
are ItemStack data inside a backpack ItemStack inside a vanilla
slot — they're not menu slots. `CarriedSourceAccess` walks every
registered carried source in stableOrder. Drop side uses
`Slot.safeInsert` so vanilla `mayPlace` rules apply (crafting
input limits, machine input filters, chest accept rules all
work natively). Leftover bounces back via `insertBestFit`.

**Cross-surface release cleanup**: cancelling
`ScreenEvent.MouseButtonReleased.Pre` (to consume the drop)
prevented LDLib2's `mouseReleased` from clearing
`lastMouseDownButton`, causing a phantom drag the next time the
cursor moved over a wall card. Bridge now forwards
`widget.mouseReleased(mouseX, mouseY, 0)` after `stopDrag` so
state clears.

**Direction B (vanilla cursor → wall card; shift-click chest
contents into wall) — DEFERRED.** Symmetric shape: when the
player picks up a vanilla cursor item and drops on a wall card,
the cursor item lands in carry. Wall card's MOUSE_DOWN handler
would check `containerScreen.getMenu().getCarried()` and fire a
new `sendCrossSurfaceVanillaCursorDrop` RPC; server takes
`menu.getCarried()` and routes through
`CarriedSourceAccess.insertBestFit`.

**Acceptance for 3a.2:**

- Drag-from-wall to chest slot works. ✓ (direction A)
- Drag-from-chest to wall works. ✗ (direction B deferred)
- No event loops or double-fires when the host menu also
  handles the click. ✓ (Pre cancel + forward release fix)

### Phase 3a.3 — broaden host coverage

Add coverage for crafting table, furnace, anvil, brewing stand. These
are all stock `AbstractContainerScreen` subclasses, so the same shift +
embed should work.

**Acceptance for 3a.3:**

- Each of: chest, crafting table, furnace, anvil, brewing stand opens
  with the sidebar.
- One TerraFirmaGreg / GregTech machine playtested manually as a
  smoke test for "does the leftPos shift survive heavy machine GUIs"
  representative of the target modpack profile.
- If a representative machine breaks, document it and gate it via the
  per-screen-class allow-list rather than back out the feature.

### Phase 3a.4 — hard-custom screens (next-up follow-up)

Out of scope for the initial 3a ship. Start with the EMI-coexistence
study described in § Hard-custom screens, then plan AE2 / RS terminal
support as 3a.4 with its own acceptance criteria.

## Cross-referenced work that stays out of scope here

- **Phase 3b** (hide vanilla 36-slot band, give the sidebar that
  vertical real estate) ships separately. EMI's `+` button is safe under
  3b because `FillRecipeC2SPacket` uses `Slot.id` indices, not pixels
  ([FillRecipeC2SPacket.java:31](../../../reference/classification/emi_and_plugins/emi/network/FillRecipeC2SPacket.java#L31)).
- **Phase 3c** (mod-observer transparency for shift-click / hotkey
  transfer / sorting hooks) ships separately. Most observer mods bind
  to `Slot` indices in the menu, so as long as the sidebar uses the
  same intent router as the rest of SLOT, transparency falls out for
  free in many cases.
- **EMI integration proper** (recipe pin = relevance contributor, "+"
  that pulls from carry + proximate chests transparently) is its own
  follow-up plan as called out in [list-view.md § Out of scope](list-view.md).

## Open considerations and escape hatches

- **Parallel hidden menu cleanup.** When the player closes the host
  screen, the parallel LDLib2 menu must close too. NeoForge's
  `ScreenEvent.Closing` (or the `removed()` walk) is the hook; verify
  that no orphan menus accumulate during normal play.
- **Sync packet ordering.** With two menus open, packets from both
  flow through the same `PacketDistributor`. Confirm that LDLib2's
  `UISyncManager` already namespaces packets by ModularUI instance so
  there's no cross-talk; if not, that's the first thing that pushes us
  to Option B.
- **Sidebar focus / keyboard input.** When the host screen is focused
  and the player hits a search hotkey, the sidebar's search input
  needs to grab focus. LDLib2's `ScreenMixin.keyPressed()` walks
  children — verify the input element claims focus correctly.
- **The vanilla GUI shift breaks something.** Capture the failing
  screen class in the per-screen allow-list. Document the breakage
  pattern so future hosts in the same family can be opted out without
  re-investigation.

## Pointers

- [list-view.md § Phase 3](list-view.md) — the parent design.
- [`SlotContainerSidebar.java`](../../../neoforge/src/main/java/dev/imagio/slot/neoforge/client/screen/SlotContainerSidebar.java)
  — current skeleton hook (diagnostic-only).
- [`SlotWorkspaceMountController.java`](../../../neoforge/src/main/java/dev/imagio/slot/neoforge/client/screen/SlotWorkspaceMountController.java)
  — the existing screen-replacement mount (the strategy A pattern that
  this doc explicitly does **not** apply to chest/machine screens).
- [LDLib2 `ModularUI`](../../../reference/LDLib2/src/main/java/com/lowdragmc/lowdraglib2/gui/ui/ModularUI.java),
  [`ModularUIScreen`](../../../reference/LDLib2/src/main/java/com/lowdragmc/lowdraglib2/gui/holder/ModularUIScreen.java),
  [`IModularUIHolder`](../../../reference/LDLib2/src/main/java/com/lowdragmc/lowdraglib2/gui/holder/IModularUIHolder.java),
  [`UISyncManager`](../../../reference/LDLib2/src/main/java/com/lowdragmc/lowdraglib2/gui/sync/UISyncManager.java).
- [LDLib2 `ScreenMixin`](../../../reference/LDLib2/src/main/java/com/lowdragmc/lowdraglib2/core/mixins/ui/ScreenMixin.java),
  [`ContainerEventHandlerMixin`](../../../reference/LDLib2/src/main/java/com/lowdragmc/lowdraglib2/core/mixins/ui/ContainerEventHandlerMixin.java),
  [`AbstractContainerScreenMixin`](../../../reference/LDLib2/src/main/java/com/lowdragmc/lowdraglib2/core/mixins/ui/AbstractContainerScreenMixin.java).
- [EMI `EmiExclusionArea`](../../../reference/classification/emi_and_plugins/emi/api/EmiExclusionArea.java),
  [EMI `MouseMixin`](../../../reference/classification/emi_and_plugins/emi/mixin/MouseMixin.java),
  [LDLib2 `RecipeScreenMixin`](../../../reference/LDLib2/src/main/java/com/lowdragmc/lowdraglib2/core/mixins/emi/RecipeScreenMixin.java)
  for the EMI ↔ LDLib2 integration model.
