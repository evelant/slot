# LDLib2 Reference

Last updated: 2026-05-05.

Layout, sizing, and event-routing notes derived from real bugs hit while
building the SLOT workspace UI. This is the "lessons learned" companion
to [ui-lifecycle-rules.md](ui-lifecycle-rules.md). Read this before
adding new LDLib2 widgets — most pitfalls below have already cost us a
debugging session.

The full upstream docs are at
<https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/>; the source we work
against lives in [reference/LDLib2/](../../reference/LDLib2/).

---

## Where to look in the LDLib2 source

When the in-tree `reference/LDLib2/` source contradicts what you assumed
from external docs, **trust the source**. Common entry points:

- `gui/ui/UIElement.java` — base widget. Layout state, event listeners,
  hit testing, child management.
- `gui/ui/ModularUI.java` — owns the focus + event-dispatch machinery.
  See `requestFocus`, `charTyped`, `keyPressed` for the focus-aware
  paths. The mui's `screen` field is what glues it to a host
  `Screen` for `setFocused` callbacks.
- `gui/ui/elements/Label.java` + `gui/ui/elements/TextElement.java` —
  text widgets. `TextElement.recompute()` is where measured text width
  becomes the layout width (only when `adaptiveWidth(true)`).
- `gui/ui/elements/Button.java` — note `setOnClick` fires on
  `MOUSE_DOWN`, not on release (see Traps in AGENTS.md).
- `gui/ui/elements/ScrollerView.java` — vertical scrollers. `setValue`
  is normalized `[0, 1]`.
- `gui/ui/event/UIEvents.java` — event name constants. `CHAR_TYPED`,
  `KEY_DOWN`, `MOUSE_DOWN`, `CLICK`, `MUI_CHANGED`, `TICK`, etc.
- `gui/ui/event/UIEventDispatcher.java` — manual event dispatch entry
  point. Useful when you need to inject an event without going through
  the focused-element path.
- `core/mixins/ui/ScreenMixin.java` +
  `core/mixins/ui/ContainerEventHandlerMixin.java` — the only events
  LDLib2 forwards from a *host* `Screen` into child `IModularUIHolder`s
  are `tick`, `removed`, `keyPressed` (inventory key only),
  `mouseDragged`, `mouseMoved`, and `shouldCloseOnEsc`. Everything else
  needs explicit routing in sidebar mode (see § Sidebar mode).
- `gui/holder/IModularUIHolder.java` +
  `gui/holder/IModularUIHolderMenu.java` — the marker interfaces that
  let LDLib2 walk a screen's children list to find mounted UIs.

---

## Labels and adaptive width

**A bare `Label` doesn't size to its text — it grows to fill its
parent.** Without an explicit width or `adaptiveWidth(true)`, Taffy
treats the Label like any other element and gives it the parent's
content area along the main axis.

This bites in two ways:

1. **Backgrounds painted via `style.backgroundTexture` paint at the
   layout-box size, not the rendered-text size.** A Label with
   `paddingHorizontal(8)` and a GLASS background extends across the full
   parent width even though only the leading text is visible — when the
   Label is one of three children in a flex-row, the backdrop bleeds
   into the next sibling's column.
2. **Hit-testable Label widgets absorb clicks across their full layout
   box.** A "soft" Label-as-a-button can intercept clicks meant for
   widgets that sit visually next to it.

**Fix.** Turn on adaptive width:

```java
Label hint = label("Press / to search", MUTED);
hint.layout(layout -> layout.paddingHorizontal(8).paddingVertical(4));
hint.textStyle(style -> style.adaptiveWidth(true));   // ← critical
hint.style(style -> style.backgroundTexture(rect(GLASS)));
```

`TextElement.recompute()` then writes an `IMPORTANT`-priority width
override equal to the measured first-line text width plus the layout's
horizontal padding. Same for `adaptiveHeight(true)` if vertical sizing
matters.

**`wrappedLabel` is the opposite trade.** It sets `widthPercent(100) +
textWrap(WRAP)`. Wrapping requires a definite parent width — if the
parent is content-fit, the wrap target is undefined and Taffy resolves
to a degenerate narrow column where each character wraps onto its own
line (literally a vertical strip of letters). Only use `wrappedLabel`
inside a parent with an explicit `width(...)` or `widthPercent(...)`.

---

## Taffy flex layout

LDLib2 uses [vfyjxf/taffy](https://github.com/vfyjxf/taffy) under the
hood — CSS-flexbox semantics, but with one combined `AlignContent` enum
serving both `justifyContent` and `alignItems` / `alignContent`.

### The combined-enum gotcha

```java
.justifyContent(dev.vfyjxf.taffy.style.AlignContent.SPACE_BETWEEN)
```

`AlignContent.SPACE_BETWEEN` is correct for `justifyContent` even though
the enum name is `AlignContent` — Taffy reuses the enum across both
axes. Don't go looking for a separate `JustifyContent` type.

### Flex-shrink shrinks children when content overflows

Default `flex-shrink = 1` on every child. In a flex-row with `NO_WRAP`,
when total child content + gaps exceeds the parent width, every child
shrinks proportionally — even ones with explicit `width(...)`. This is
how a "Press / to search" Label and a 60px chip and a 200px action
cluster could end up all squeezed into a 30px column under pressure.

`SPACE_BETWEEN` distributes leftover space *after* shrinking. It does
not prevent shrinking.

**Fixes**, depending on intent:

- Add `flex-shrink: 0` semantics to elements that must hold their
  content size. (Taffy usually exposes this as `.flexShrink(0)` —
  check the DSL.)
- Give the flex container `wrap(FlexWrap.WRAP)` if a second line is
  acceptable when content overflows. Beware: with three siblings and
  WRAP, the 3rd one drops to its own row even if it fits at the
  workspace's natural width but not at the player's GUI scale. We were
  bitten by this with the search hint + free-slots chip + actions
  cluster — see the topRow comment in `ListWallPanelBuilder`.
- Restructure: when one slot of a flex row needs to grow (a search
  modal taking over the whole header), conditionally render *that
  element as the row's only child* instead of fighting siblings.

### `widthPercent(100)` on a child of a content-fit parent is circular

Parent's width depends on max child width; child wants 100% of parent's
width. Taffy resolves this to *something*, but the result is rarely
what you want and depends on layout-pass ordering. If you find
yourself reaching for `widthPercent(100)` on a child, also give the
parent an explicit width or convert the child to `flex(1)` so the
parent's flex layout drives sizing.

### Taffy `flex(1)` on a Label misbehaves under flex-row

Direct `flex(1)` on a `Label` (without a wrapper element) ends up
either over-stretching or coalescing with neighbors. Wrap the Label in
a plain `UIElement` with `flex(1).heightPercent(100)` and put the
Label inside.

### Cheat sheet

| Want | Pattern |
|---|---|
| Content-fit Label, background hugs text | `adaptiveWidth(true)` + bg on the Label |
| Flexible row child that grows to fill space | wrapper UIElement with `flex(1)`, content inside |
| Three siblings with even gutters at left/middle/right | row with `justifyContent(SPACE_BETWEEN)` + `wrap(NO_WRAP)` + content-fit children + `adaptiveWidth(true)` on text |
| Single-line text, never wraps | default `textWrap(NONE)` (don't use `wrappedLabel`) |
| Multi-line text, wraps to parent width | `wrappedLabel` **only inside a fixed-width parent** |
| Element doesn't shrink under pressure | `flexShrink(0)` |

---

## Backgrounds, overlays, hit testing

- Background textures (`style.backgroundTexture`) paint at the layout
  box size — see § Labels above.
- Overlay textures (`style.overlayTexture`) paint *on top of* the
  element's content at the layout-box size. Use for hover highlights,
  selection chrome.
- `setAllowHitTest(false)` on decorative children stops them from
  absorbing clicks. Default is hit-testable.
- `event.stopPropagation()` in a `MOUSE_DOWN` listener prevents the
  click from reaching ancestors — needed when a panel sits over the
  wall and you don't want background clicks (deselect / cancel
  cursor) to fire through.

---

## Events: focus, charTyped, keyPressed

`ModularUI.charTyped` and `ModularUI.keyPressed` *only dispatch events
to the focused element*. If `focusedElement` is null, both return
`false` immediately and your `addEventListener(UIEvents.CHAR_TYPED, …)`
on `host.root` never fires.

Focus is set via `requestFocus(element)`. The standalone workspace
relies on this MUI_CHANGED hook:

```java
host.root.addEventListener(UIEvents.MUI_CHANGED, event -> host.root.focus());
```

`requestFocus` *also* calls `screen.setFocused(getWidget())` when
`mui.screen` is non-null, which tells the host screen its focused
child is our LDLib2 widget. That second step is what lets the host
screen's `charTyped` / `keyPressed` reach the LDLib2 widget tree.

### Sidebar mode: char/key events do not reach the widget tree

When the sidebar is mounted on a non-LDLib2 host
(`SlotSidebarClientUi.mount`), LDLib2's `ScreenMixin` forwards only the
**inventory key** to children's `ModularUI.keyPressed`, and forwards
**no** `charTyped` events at all. So `/` (search), digits (belt
assign), `Z` (undo), and every other in-screen hotkey silently die.

The sidebar fixes this with explicit NeoForge `ScreenEvent` listeners
in `SlotContainerSidebar`:

- `ScreenEvent.CharacterTyped.Pre` →
  `SlotSidebarClientUi.dispatchCharTyped` builds a
  `UIEvent(CHAR_TYPED)` targeted at our root and dispatches it through
  `UIEventDispatcher.dispatchEvent`. This bypasses the focused-element
  check — the event reaches every listener bound to our root or its
  descendants.
- `ScreenEvent.KeyPressed.Pre` → mirror plumbing for key codes.

Both bail out when the host screen has a vanilla `EditBox` focused
(anvil rename, etc.) so the player can still type into vanilla text
inputs without SLOT stealing the keystrokes.

If you add a new global hotkey / char shortcut and it works in
standalone mode but silently fails when the sidebar is mounted on a
chest, this is almost certainly why.

### `Button.setOnClick` fires on MOUSE_DOWN

Already documented in AGENTS.md Traps; reiterating because it surfaces
when a button is also a drag source. Use
`addEventListener(UIEvents.CLICK, …)` for click-and-drag elements —
LDLib2 dispatches `CLICK` from `ModularUI` on `MOUSE_UP` after
`DRAG_PERFORM`, only when release target equals press target.

---

## Manual event dispatch

For events the focused-element path can't deliver (sidebar char input,
synthetic test events, cross-cutting input forwarding), build a
`UIEvent` and pass it to `UIEventDispatcher.dispatchEvent`:

```java
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEventDispatcher;

UIEvent event = UIEvent.create(UIEvents.CHAR_TYPED);
event.codePoint = '/';
event.modifiers = 0;
event.target = root;
UIEventDispatcher.dispatchEvent(event);
boolean consumed = event.hasHandler;
```

Events go through capture (root → target) then bubble (target → root)
unless either phase is disabled on the event. `event.target` is the
deepest element involved.

---

## Common patterns to verify in source before guessing

1. **Default value of a layout style.** `LayoutProperties.java`
   declares each property with its default — `ALIGN_CONTENT` defaults
   to `FLEX_START`, `JUSTIFY_CONTENT` defaults to `FLEX_START`, etc.
2. **Whether a widget supports a given event.** `UIEvents.java` lists
   the dispatchable event names; `ModularUI` and `UIElement` show
   which method paths fire which event.
3. **Scroller behavior.** `ScrollerView.setValue` is normalized to
   [0, 1]; multiply by `(container.height - viewport.height)` to get
   pixel offset.
4. **Whether a Button is also a focusable / draggable surface.**
   Re-read `Button.java` — `setOnClick` vs `addEventListener(CLICK)`
   has bitten us at least three times.

When in doubt, `grep -rn "<thing>" reference/LDLib2/` is faster than
guessing or asking. The tree is small.
