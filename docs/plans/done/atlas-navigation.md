# Atlas Navigation Plan

Last updated: 2026-04-20

Status: **all four slices landed**, plus an extended QoL pass on top
(observable state, persistent UI chrome, single-item push/pull, link
navigation arrows, grid snap, presence pip, LOD tuning, integer font
snap, etc.). Landing points and current tuning live in
[../status.md](../status.md) under "Atlas navigation + QoL landing
points." This plan stays here as the historical spec for the original
four slices. Next focus: integrate embers-text-api for crisp MSDF
glyphs at any atlas scale.

This plan extends the atlas UI that landed under
[atlas-prototype.md](atlas-prototype.md) and the hover-trail vocabulary
from core-workflow-ux Slice 3 (rotated `Transform2D` panels).
The storage prototype and core-workflow UX pass are complete
(see [storage-prototype.md](../retired/storage-prototype.md) and
[core-workflow-ux.md](core-workflow-ux.md)); nothing in this plan
touches RPCs, the intent router, or domain state.

For the atlas concept, including the explicit design call-outs for
"camera fly to search result," "Back/Forward buttons move through
camera history," and "Escape clears the query and returns to the
previous camera target if search moved the camera," see
[../design/atlas.md](../design/atlas.md) and
[atlas-prototype.md](atlas-prototype.md) §Core controls and §Search
As Navigation.

## Goals And Non-Goals

Goals:

- make navigating a populous atlas cheap: hover-peek at any item-bearing
  element, tap to commit, bracket keys or mouse 4/5 to retrace
- extend the existing `SlotAtlasGraphView` camera with animated
  transitions and a bounded back/forward history — one primitive that
  peek, goto-commit, search, and the already-landed programmatic jumps
  (`panToIsland`, `panToChestTile`, `homeButton`, chip-accept focus,
  island-creation focus) all route through
- collapse quick search into the same animation path so the player
  learns one mental model: "ease to target, optionally snap back"
- keep history honest: commit-intent gestures push, passive gestures
  (drag-pan, wheel-zoom) do not; peek never pushes

Non-goals:

- a general action/domain undo stack (out of scope — see
  [current.md](../current.md) "Later Feature Tracks"; camera history is
  strictly screen posture per the `MapViewportState` rule in atlas.md)
- moving any fixed overlay (Belt, Triage panel, Kit Rack, context menu,
  link popover) — the camera moves, the chrome does not
- any server-side change: no new RPCs, no view-model fields, no
  workflow events, no persistence delta
- search result tray / match list UI (deferred; the floating chip +
  live camera preview is the only visual in this pass)
- indexing beyond item display name + island label + item id (no
  tooltip-derived variants, mod namespace, tags, or chest labels yet —
  upgrade the index later if the primary pool feels thin in playtest)
- fuzzy / infix matching (prefix-only this pass; keep the top-match
  ranking deterministic and cheap)
- per-player persistence of camera history (always session-scoped;
  fresh empty stack on every open)

## Prerequisites

What must already be landed before each slice can start:

- **slice 1 (camera controller)** depends on the existing
  `SlotAtlasGraphView` / `AtlasCamera` pair at
  `neoforge/src/main/java/dev/imagio/slot/neoforge/screen/ldlib/SlotWorkspaceUiFactory.java`
  (~line 4685 `SlotAtlasGraphView`, ~line 4775 `AtlasCamera`), plus
  `FitCarriedCamera` at
  `common/src/main/java/dev/imagio/slot/atlas/FitCarriedCamera.java`.
  Both landed.
- **slice 2 (peek + goto)** depends on slice 1's animated transition
  and snap-back primitive. The existing hover-state fields
  (`hoveredHotbarIndex`, `hoveredAtlasIdentity`) already cover hotbar
  slots, atlas home cards, and triage panel rows (see
  `SlotWorkspaceUiFactory` ~line 113-115, 625-630, 1535-1540,
  2722-2729); chest-content cells need new hover tracking.
- **slice 3 (history UI)** depends on slice 1's history primitive and
  slice 2's commit path. It binds keys (`[`, `]`) and mouse buttons
  (4, 5) through the existing root `KEY_DOWN` listener at
  `SlotWorkspaceUiFactory.installBeltHotkeys()` (~line 189) and the
  screen's `mouseClicked` dispatch. Rebinding routes through
  NeoForge's `RegisterKeyMappingsEvent`; see the
  archived `SlotDebugKeyMappings` at
  `archive/legacy-authority/neoforge/src/main/java/dev/imagio/slot/neoforge/client/SlotDebugKeyMappings.java`
  as a shape reference (not a dependency).
- **slice 4 (quick search)** depends on slice 1's animated ease and
  slice 2's origin-snapback. The existing in-place search (capsule
  `TextField` + `matchesSearch` + `searchSummary` at
  `SlotWorkspaceUiFactory` ~line 1678-1705 and 4370-4398) stays; the
  new modal search overlay is layered on top and shares the
  `searchQuery` string so match-dimming continues to work.

## Risk Register

### 1. Key And Mouse-Button Conflicts

Space, `/`, `[`, `]`, mouse 4, and mouse 5 must not collide with
vanilla or existing atlas bindings.

Found (grep pass over `SlotWorkspaceUiFactory`):

- **Space**: not bound in the SLOT workspace screen. In vanilla,
  space jumps; but this screen is an `AbstractContainerScreen`-family
  modal, so game input is suppressed while the screen is open — no
  conflict.
- **`/`** (`GLFW_KEY_SLASH`): not bound. In vanilla, `/` opens chat
  as a command prefix, but chat is suppressed in container screens —
  no conflict.
- **`[` / `]`**: not bound.
- **Mouse 4 / 5**: not bound. LDLib2 passes `event.button` as a raw
  int (see `reference/LDLib2/.../UIEvent.java:95`), so buttons 3 and 4
  (the GLFW codes for back/forward) are available once we route
  `mouseClicked` past the workspace menu.
- **Digits 1-9**: already reserved for belt hotkeys at
  `installBeltHotkeys` (line 189); search must suppress digits in its
  buffer and let them reach the belt handler instead.
- **Enter / Escape**: Enter is consumed by island-rename `TextField`
  (line 2310); Escape already dismisses the context menu catcher
  (line 2020-2029). Search modal's Escape handler must sit on the
  search overlay's own input handling and stop-propagation before the
  context-menu catcher can see it.
- **Shift**: used by shift-click throughout; space is unrelated.

Mitigation:

- all four gesture surfaces live on the root `KEY_DOWN` listener
  (alongside the existing belt hotkey handler) and stop-propagation
  only when they actually consume the key; the belt hotkey path
  still wins on digits while search is inactive
- digits 0-9 are explicitly excluded from the search buffer (both
  from `CHAR_TYPED` and from the search overlay's own `KEY_DOWN`)
  so hotbar-assign keeps working during search. The player can
  always close search with Escape or idle-commit to get digits back
- mouse button binding lives on the root `MOUSE_DOWN` listener; `[`,
  `]`, and mouse 4/5 are user-rebindable via NeoForge `KeyMapping`
  registered on `RegisterKeyMappingsEvent` (category: "SLOT Atlas
  Navigation")
- key-repeat is ignored for `[` / `]` / Space (GLFW repeat events
  produce `KEY_DOWN` with `action=REPEAT`; the handler gates on
  `action=PRESS` only) so a held bracket doesn't chain through
  history

### 2. Peek vs. Pan-Drag Gesture Ambiguity

Holding space while the mouse is already panning would race the two
camera sources. Similarly, during an active peek, the player
incidentally moving the cursor off the hover target would snap the
camera back mid-gesture.

Mitigation:

- peek is refused while `SlotAtlasGraphView.isDragging()` is true
  (LDLib2 drag state); the viewport stays put until the drag ends
- once peek starts, the target is **latched** on press — moving the
  cursor off the element does not cancel peek; only releasing space
  (or pressing space to tap-commit) ends it. This keeps "space is
  pressed, I'm looking" coherent even if the pan movement shifted
  the hover
- the peek ease runs on LDLib2 `TICK` events against the live
  `GraphView` offset/scale fields; passive wheel-zoom during peek is
  still allowed (it edits the camera directly via the existing
  `onMouseWheel`) but the origin snapshot stays fixed — release
  restores the pre-peek camera regardless of wheel adjustments

### 3. Animation Budget And Cost

Every hover-peek and every search keystroke kicks an animation. Past
work (core-workflow-ux Slice 3) called out per-frame recomputation as
a hotspot; the same rule applies here.

Mitigation:

- one in-flight camera transition at a time. Starting a new one
  replaces the current transition's target; the ease continues from
  the current interpolated camera (no hard cut)
- ease parameters are constants (cubic-in-out, ~180 ms on peek, ~240
  ms on goto-commit, ~140 ms on search preview) tuned empirically
  during the slice — not per-card recompute
- the animation driver is a single `TICK` listener installed once at
  `installBeltHotkeys`-adjacent registration time; no per-card, per-
  island, or per-cell tick overhead
- passive drag-pan / wheel-zoom are unaffected — they write directly
  to `GraphView` offset/scale as they do today; the animation driver
  checks an `animating` flag and stays out of the way when false

### 4. Camera History Pollution

An eager history pushes on every programmatic jump would blow the
bound quickly; an under-eager one would miss the "I just jumped to a
chip suggestion and want to return" case.

Mitigation:

- explicit allow-list of commit callers: hover-goto (Slice 2 tap),
  search auto-commit / Enter / Tab-commit (Slice 4), `panToIsland`,
  `panToChestTile`, `homeButton`, chip-accept focus, island-creation
  focus, Re-home... picker selection
- explicit deny-list: peek (held), search live preview, passive
  drag-pan, passive wheel-zoom, `applyInitialCamera` on open,
  `resetToOverview`-style initial fits
- implemented as a single `commit(camera, source)` entry point on
  the camera controller (slice 1), so history pushes are
  concentrated in one file rather than scattered. Passive
  `captureCamera` calls in `SlotAtlasGraphView` continue to fire
  (they must; the controller reads them back), but they do not
  touch history
- bound: fixed 20 entries, ring-buffer semantics (oldest evicted on
  push). Forward stack discards on any new commit after a back
  (standard undo/redo shape)
- a "current camera" slot distinct from the back/forward stacks so
  Back from the active camera returns to the **most recently
  committed** camera, not the camera-before-current

### 5. Search Buffer Shape And Discoverability

Quick search is a modal keystroke capture layered on a screen that
already uses digits 1-9 and accepts typing in other contexts (island
rename). The player must never type into search thinking they were
hitting a hotkey, and must never hit a hotkey thinking search was
active.

Mitigation:

- explicit enter gesture: `/` starts search **only if** no other
  focused text input is active. `TextField`-focused state (e.g., the
  existing capsule search, the island rename input, the pending
  island-create label) disables the `/` handler — LDLib2 keeps the
  focused element, and normal text input wins
- visible search chip near cursor (or anchored to nav capsule —
  chose nav capsule for the prototype; simpler to position, doesn't
  occlude hover targets) with a blinking cursor. When search is
  inactive, no chip, no buffer
- first-open hint: the nav capsule summary (currently `"Drag to
  pan. Drag anchors between atlas and belt. ..."` at
  `SlotWorkspaceUiFactory:4389`) gains `"Press / to search"` as its
  first line until the player has used search at least once per save;
  gate the flag on a simple `boolean firstSearchSeen` stored with the
  workflow snapshot (additive; no migration). If that flag pipe
  feels heavy for a UX hint, start with a 10-second first-open
  toast instead and revisit
- Escape aborts: snaps back to the pre-search origin with no history
  push; search chip dismisses; `searchQuery` clears (so the
  existing in-place dim also clears)
- Tab cycles the top-match ordering deterministically (by the
  ranking below) so a repeated Tab is stable across keystrokes

### 6. Search Ranking Ambiguity

Two items starting with the same prefix need a deterministic winner
or Tab will feel jittery.

Mitigation:

- primary pool is carried + homed item display names; secondary
  pool is island labels. Primary always outranks secondary at the
  same prefix depth
- within a pool, rank by: (a) carried before ghost / unhomed,
  (b) shorter name first (prefix match is "more of the word"),
  (c) lexicographic `name.toLowerCase(Locale.ROOT)` tiebreak, then
  (d) identity hash for final stability
- Tab cycle order is the full match list, not just the top three
- ranking is a pure function in `common/atlas/` so it's unit-
  testable and identical on both sides of the `common/neoforge`
  boundary

## Slice 1 — Camera Controller Primitive

Goal: one place owns the animated camera transitions and the bounded
history stack. No UI yet.

Deliverables:

- new `AtlasCameraController` living next to `SlotAtlasGraphView`
  inside `SlotWorkspaceUiFactory.java` (private nested class to
  preserve the "LDLib2 imports stay in neoforge" rule; no `common`
  changes needed):
  - wraps the existing `SlotAtlasGraphView`
  - `public void ease(AtlasCamera target, Easing easing, long durationMs)`
    — begins an animated transition from the current camera state to
    `target`; replaces any in-flight ease, starting from the current
    interpolated camera so there are no hard cuts
  - `public void snap(AtlasCamera target)` — instant, used by the
    origin snap-back path on peek-release and search-Escape
  - `public void commit(AtlasCamera target, CommitSource source,
    Easing easing, long durationMs)` — like `ease`, but additionally
    pushes the pre-commit camera onto the back stack and clears
    the forward stack
  - `public boolean back()` / `public boolean forward()` — pop/push
    and ease; no-op + returns false when the corresponding stack is
    empty
  - `public void recordOrigin()` / `public void clearOrigin()` /
    `public void restoreOrigin()` — for peek and search snap-back;
    origin is a single `AtlasCamera?` slot, not part of the back
    stack
- `enum CommitSource` with named constants
  (`HOVER_GOTO`, `SEARCH_COMMIT`, `SEARCH_ENTER`, `HISTORY_BACK`,
  `HISTORY_FORWARD` — note: history nav *does not* push, but the
  enum lets debug logs trace the source, `HOME_RESET`, `PAN_TO_ISLAND`,
  `PAN_TO_CHEST`, `CHIP_ACCEPT`, `ISLAND_CREATE_FOCUS`,
  `REHOME_PICK`) — one commit call per existing programmatic jump so
  the push allow-list is explicit in code, not a comment
- `Easing` is an inline functional interface (`float apply(float
  t01)`) with two constants (`LINEAR`, `CUBIC_IN_OUT`); no external
  dependency
- the animation loop is a single `TICK` listener registered on the
  `content` root element (mirrors the hover-trail overlay pattern at
  `SlotWorkspaceUiFactory:1280`). On each tick:
  - if not animating, no-op
  - else interpolate offset+scale from start to target, write
    directly to `GraphView.offsetX / offsetY / scale` and call
    `refreshContentTransform()` (or reuse `SlotAtlasGraphView.fit(...)`
    — the private method invoked by `restoreCamera` — prefer direct
    field writes for smoothness so we're not re-running `fit`'s
    centering math each frame)
  - on completion, write the final target exactly; clear the
    `animating` flag
- refactor existing programmatic jumps to route through
  `commit(...)` instead of directly calling `atlas.restoreCamera`:
  - `panToIsland` (~line 1799), `panToChestTile` (~line 1777),
    `homeButton` on click (~line 1708), pending-island-create focus
    (grep `pendingCreateFocusPending` callers),
    `chestPresenceEntry` click-to-pan (already calls `panToChestTile`
    — already covered once `panToChestTile` routes through
    `commit(...)`)
- the `applyInitialCamera` path on layout-changed / first open calls
  `snap(...)`, not `commit(...)` — opening the screen should leave
  history empty

Exit criteria:

- all existing "click to pan" behaviors (Home button, island label
  click-to-pan, chest presence strip click) animate smoothly rather
  than hard-cutting; no regressions vs. current visual behavior
  once the ease finishes
- `back()` after opening and clicking an island jumps back to the
  carried-fit camera; `forward()` returns to the island
- `back()` with an empty stack no-ops (returns false)
- unit tests in `common/atlas/` cover: history push bounds
  (overflow at 20 evicts oldest), forward-stack-clears-on-commit,
  origin record/restore is independent of back/forward. Since the
  controller itself is in `neoforge/`, the testable logic is
  extracted as a `CameraHistory` record/class in `common/atlas/`
  that the controller delegates to — keeps `common` LDLib2-free
- `CameraHistoryTest` covers: push/pop/forward/clear-on-commit/
  bounds, plus the ring-buffer eviction
- manual QA: drag-pan and wheel-zoom do not push to history
  (verified by doing 100 pan+wheel then pressing `back` and
  expecting the previous *committed* camera, not the last dragged
  frame)

## Slice 2 — Hover Peek And Hover Goto

Goal: the two-verb space gesture — **hold** to peek, **tap** to commit.

Deliverables:

- one "peek target" resolver keyed on the current hover state.
  Producers, in priority order (first non-null wins):
  - hovered hotbar slot (`hoveredHotbarIndex`) → its home via
    `atlasItemInIslandLayer` (already present at
    `SlotWorkspaceUiFactory:1354`); target rect is the home card
    bounds
  - hovered atlas home card (`hoveredAtlasIdentity`) → that card's
    world rect
  - hovered triage panel row (`hoveredAtlasIdentity` covers this —
    triage rows share the same identity hover; existing)
  - hovered chest-content cell → the **home** for that cell's
    identity (not the chest tile; the "home" of the item is its
    atlas card, same as hotbar-hover). Requires a new
    `hoveredChestCellIdentity: IdentityRef?` field + MOUSE_ENTER /
    MOUSE_LEAVE listeners on chest cells (mirror the existing
    pattern at `SlotWorkspaceUiFactory:1061`); targets fall back to
    the chest tile if the cell's identity has no home
  - hovered atlas home card (`hoveredAtlasIdentity`) when hovering
    the home of an item that only exists in chests → target is the
    first-ranked chest presence entry (mirrors the existing
    `panToChestTile` behavior on presence-strip click)
  - otherwise: null (no peek target)
- space key press (GLFW_KEY_SPACE, action=PRESS, via the existing
  root `KEY_DOWN` listener):
  - refuse if `SlotAtlasGraphView.isDragging()`, or if any
    `TextField` is focused (island rename / pending-create / search
    modal), or if peek target is null
  - else: `recordOrigin()`, compute the target's
    `FitCarriedCamera.Camera` via `FitCarriedCamera.fit(...)` (reusing
    the existing `CARRIED_FIT_*` constants), call `ease(target,
    CUBIC_IN_OUT, 180ms)`, set `peekActive = true`, record the
    target rect so subsequent cursor movement doesn't retarget
  - action=REPEAT is ignored
- space key release (GLFW_KEY_SPACE, action=RELEASE):
  - if `peekActive`: `snap(origin)` — origin is the camera state
    **before** the press, not the interpolated camera at release —
    so snapback is exact; clear origin; clear `peekActive`
  - else: no-op
- space key press while `peekActive` (second press within the same
  peek — "tap-while-held"):
  - treats the second press as the commit gesture: take the current
    in-flight target, call `commit(target, HOVER_GOTO, CUBIC_IN_OUT,
    240ms)`, clear origin and `peekActive`. This handles
    "hold → tap-commit → release" and "tap → (no hold)" with one
    code path
- hover **goto** (the tap-only variant): distinguished from peek by
  duration. Chose **press-release within 180ms** as the tap
  threshold (shorter than the peek ease); any release after 180ms
  is a peek-release, any release before is a tap-commit.
  Alternative considered and rejected: separate keys (hold=space,
  tap=shift+space). The spec is clear that tap and hold are the
  same key with different dwell — we keep that.
  - on tap-commit: `commit(target, HOVER_GOTO, CUBIC_IN_OUT,
    240ms)`, clear origin and `peekActive`
- visual affordance during peek: dim the non-target chrome slightly
  (lean on the existing search-dim vocabulary — pass the peek
  target through the same `ROW_HOVER` / dimming code path used by
  `matchesSearch`). If polish feels wrong in playtest, fall back to
  no dim; the camera movement itself is the primary signal

Exit criteria:

- hovering a hotbar slot, holding space → viewport eases to that
  slot's home card and stays centered; releasing space snaps back
  exactly to the pre-press camera (no history push)
- hovering a home card, tapping space (<180ms) → viewport commits;
  back returns to the pre-tap camera
- hovering a triage panel row behaves the same as hovering any
  other atlas home card
- hovering a chest-content cell with a homed identity peeks to that
  home; with an unhomed / triage identity peeks to the cell's chest
  tile
- peek refused during active drag (drag-pan, chest tile drag, chest
  cell drag-extract, atlas card drag); releasing the drag and
  re-pressing space works
- peek refused while any `TextField` is focused (island rename,
  pending-create, search modal)
- holding space with no hover target is a no-op; releasing is a
  no-op
- peek never pushes history; hover-goto always does (one entry per
  tap)

## Slice 3 — Camera History Navigation UI

Goal: back/forward keys and mouse buttons, all rebindable.

Deliverables:

- register four new NeoForge `KeyMapping`s (category "SLOT Atlas
  Navigation") via `RegisterKeyMappingsEvent`:
  - `key.slot.camera_back` — default `GLFW_KEY_LEFT_BRACKET`
  - `key.slot.camera_forward` — default `GLFW_KEY_RIGHT_BRACKET`
  - `key.slot.camera_back_mouse` — default mouse 4 (`InputConstants.Type.MOUSE`, code 3)
  - `key.slot.camera_forward_mouse` — default mouse 5 (`InputConstants.Type.MOUSE`, code 4)
  - (four separate mappings rather than one "back" / one "forward"
    because `KeyMapping` only binds one input; this matches how
    vanilla handles left-click/right-click as separate mappings)
- the bindings register in a new
  `neoforge/src/main/java/dev/imagio/slot/neoforge/client/input/SlotAtlasKeyMappings.java`
  similar in shape to the archived `SlotDebugKeyMappings`
- bind the actions **inside the workspace screen** — not at the
  game-input level — so they only fire while the atlas is open:
  - extend `installBeltHotkeys` (~line 189) to also listen for
    `KEY_DOWN` / `MOUSE_DOWN` events whose key / button matches
    either mapping's currently-bound input (poll the mapping each
    press; the player may have rebound). `KeyMapping.matches(keyCode,
    scanCode)` is the intended API
  - `back` key / mouse button → `controller.back()`; `forward` →
    `controller.forward()`
  - on no-op (empty stack), set `localStatus` to
    `"no further camera history"` / `"at latest camera"` so the
    player gets a diagnostic
  - stop-propagation on consumed presses so the belt hotkey path
    does not see them (not an issue for brackets; is an issue for
    mouse 4/5 if anything else listened, which today it doesn't)
- action=REPEAT is ignored (no held-chain through history)

Exit criteria:

- fresh atlas open: both stacks empty; `back` / `forward` no-op and
  surface the diagnostic
- click an island → `back` returns; `forward` goes back to the
  island; mouse 4 / 5 equivalent
- after `back`, doing any new commit (another island click, or a
  hover-goto tap-commit) clears the forward stack
- Minecraft "Controls" menu shows the four mappings under a
  "SLOT Atlas Navigation" category and rebinding takes effect on
  the next press (no restart)
- after 25 commits, `back` can walk back exactly 20 times and stops
  (oldest 5 evicted per the ring-buffer)
- peek gestures (held-space, release) do not alter history
- passive drag-pan and wheel-zoom do not alter history (verified by
  doing 50 pan+wheel between two commits and expecting `back` to
  skip them)

## Slice 4 — Quick Search Modal

Goal: `/` activates a keystroke-level search overlay; live preview
eases to the top match; idle auto-commits; Escape aborts.

Deliverables:

- state machine: `searchModalActive: boolean`, `searchBuffer:
  String`, `searchOrigin: AtlasCamera?`, `lastKeystrokeTickMs: long`,
  `matches: List<SearchMatch>` (cached, recomputed on each buffer
  change), `matchIndex: int` (Tab cycle cursor)
- `/` key handler (on the root `KEY_DOWN`):
  - refuse if any `TextField` is focused
  - else: `searchModalActive = true`, `searchBuffer = ""`,
    `searchOrigin = controller.recordOrigin()`,
    show the chip, show the match highlight
- during search, `CHAR_TYPED` events:
  - if codePoint is a digit 0-9: stop-propagation? **no** — let it
    reach the belt hotkey handler. The belt handler at
    `installBeltHotkeys:195` already filters and handles digits;
    our search excludes digits from the buffer by simply not
    appending them
  - if codePoint is a printable ASCII non-digit: append to buffer,
    bump `lastKeystrokeTickMs`, recompute `matches`, reset
    `matchIndex = 0`, `ease(topMatchCamera, CUBIC_IN_OUT, 140ms)`
    if matches non-empty, else no ease but keep buffer (user is
    still typing)
- during search, `KEY_DOWN`:
  - Backspace: pop last char from buffer; bump keystroke tick;
    recompute matches; re-ease
  - Tab: `matchIndex = (matchIndex + 1) % matches.size()` if
    matches non-empty; ease to the new top; do NOT reset keystroke
    tick (tab-cycle should not prolong idle timer)
  - Enter: manual commit — `commit(matches[matchIndex].camera,
    SEARCH_ENTER, CUBIC_IN_OUT, 240ms)`, close modal, clear
    origin (do not snap back)
  - Escape: abort — `snap(searchOrigin)`, close modal, clear origin
    and buffer, no history push
  - `/`: no-op (don't re-enter search on top of itself)
  - digit keys, digit-KP keys: let them propagate so the belt
    hotkey handler fires normally (the player can 1-9 during
    search; keeps the "vanilla hotbar select still works" rule
    from the spec)
  - any other key: ignored by the search handler (propagates to
    the normal path)
- idle timer: a single `TICK` listener on `root` (alongside the
  animation tick) checks
  `searchModalActive && matches non-empty && System.currentTimeMillis()
   - lastKeystrokeTickMs >= 800`. On true: auto-commit identical to
  Enter (uses `SEARCH_COMMIT` source), close modal
- match computation (pure, in `common/atlas/AtlasSearchIndex`):
  - input: `List<AtlasItem>`, `List<AtlasIsland>`, `String query`
  - output: `List<SearchMatch>`, where `SearchMatch` has
    `target` (enough info for the caller to compute the camera —
    pass `Rect` or `(centerX, centerY, width, height)`; caller
    calls `FitCarriedCamera.fit(...)`) and `pool` (PRIMARY /
    SECONDARY) for highlight display
  - matching: `name.toLowerCase(Locale.ROOT).startsWith(query.toLowerCase(Locale.ROOT))`;
    primary pool = `AtlasItem.name()`, secondary pool =
    `AtlasIsland.label()`
  - ordering: primary before secondary; within a pool, carried
    before ghost (carried = `AtlasItem.carried()`), shorter name
    first, lexicographic, identity-hash tiebreak
  - unit-tested in `common/src/test/java/dev/imagio/slot/atlas/AtlasSearchIndexTest.java`
- visual:
  - floating chip anchored below the nav capsule (simpler than
    cursor-follow; the nav capsule already owns top-left screen
    real estate and has the search capsule for long queries).
    Absolute-positioned glass panel, z-index 10, showing
    `"/log_"` with the buffer text and a blinking cursor
  - highlight on the current top match: add a brief border accent
    on the matched card (reuse existing
    `cardChromeColor(..., searchMatch=true, ...)` treatment but
    force it on for `matches[matchIndex]` even when
    `normalizedSearchQuery()` is empty — in this modal it is
    non-empty by definition)
  - summary subline: `"3 of 12 matches · Tab to cycle · Enter to
    lock · Esc to abort"`
- first-open hint: a 10-second toast shown on the first atlas open
  of the client session — `"Press / to search"`. Gated on a
  client-only `static boolean firstSearchHintShown` flag (no
  persistence, no view-model field). The toast dismisses on any
  keypress, on `/` activation, or after 10 seconds. If playtest
  says the hint needs to stick beyond one session, upgrade to the
  workflow-snapshot persistence described in the rejected
  alternative

Exit criteria:

- pressing `/` with the atlas open opens the chip and starts an
  empty buffer; the camera does not move yet
- typing `log` eases to the top-matching "log" item (in a
  typical carried atlas — verify with
  `/slot test populate organized`)
- 800ms of idle → auto-commits; pressing `back` returns to the
  camera before `/` was pressed
- typing `log` then Tab cycles through matches deterministically;
  repeated Tabs are stable across keystrokes of the same buffer
- typing `xxzzz` with zero matches keeps the buffer and does not
  move the camera; idle timer does not fire on zero matches
- pressing Escape snaps back to origin and does not push history;
  `back` after Escape returns to the camera *before the previous
  commit*, not to the search origin (because no commit happened)
- digits 1-9 during search still trigger belt hotkeys (the
  existing handler at `installBeltHotkeys` still wins because
  `CHAR_TYPED` excludes them and `KEY_DOWN` propagates); search
  buffer remains whatever it was
- digits 1-9 are never appended to the search buffer
- the first atlas open of a client session shows the
  `"Press / to search"` toast; subsequent opens within the same
  session do not
- opening any TextField (island rename) then pressing `/` inserts
  a `/` into the text field, not into search

## Testing Priorities

Highest-value coverage to add, in slice order:

- `common/atlas/CameraHistory`:
  - push/pop/forward/clear-on-commit
  - ring-buffer bound: push 25, back walks 20 and stops
  - origin record/restore independent of back/forward
- `common/atlas/AtlasSearchIndex`:
  - prefix match, case-insensitive, primary-before-secondary
  - carried-before-ghost, shorter-name-first, lexicographic,
    identity-hash tiebreak
  - Tab cycle stability across the same buffer and across one-char
    edits
  - empty query returns empty list (no accidental full dump)
- `SlotWorkspaceUiFactory` (manual QA checklist; these paths live
  in `neoforge/` and remain harder to unit test):
  - space-hold peek eases to target and snaps back exactly
  - space-tap commits and pushes history
  - peek refused during drag and while any TextField focused
  - `[` / `]` / mouse 4 / mouse 5 walk history; no-op on empty
    stacks with status diagnostic
  - rebinding in the vanilla Controls menu takes effect immediately
  - `/` opens search; typing eases; idle auto-commits; Escape snaps
    back; Enter commits; Tab cycles
  - digits 1-9 continue to trigger belt hotkeys at every stage of
    search
  - existing behavior unchanged: drag-pan, wheel-zoom, island
    label click-to-pan, Home button, chest presence strip click,
    chip accept focus all still work (now via animated ease)
  - performance: no measurable FPS regression on the populated
    atlas (`/slot test populate late-modpack`) during an active
    peek or search
- architecture check: `common` still does not import LDLib2 (the
  existing architecture test covers this)

## Definition Of Done For This Phase

This phase is complete when:

- holding space with any item-bearing hover target peeks (eases to,
  snaps back from) that target's home
- tapping space with a hover target commits and pushes camera
  history
- `[` / `]` and mouse 4 / 5 walk camera history back/forward; all
  four are user-rebindable via the Minecraft Controls menu
- every existing programmatic camera jump (Home, island label
  click-to-pan, chest presence strip click-to-pan, chip accept
  focus, island-creation focus, Re-home… picker selection) pushes
  one history entry and animates via the ease primitive
- passive drag-pan, passive wheel-zoom, screen-open
  `applyInitialCamera`, and peek (held) do **not** push history
- `/` opens a modal search chip; live preview eases to the top
  match as the buffer grows; 800ms idle auto-commits; Tab cycles;
  Enter commits; Escape snaps back without push
- digit keys are excluded from the search buffer and continue to
  drive belt hotkeys throughout search
- the first-open `"Press / to search"` toast appears on the first
  atlas open per client session
- no new RPCs, no view-model fields, no workflow domain events
- `common` continues not to import LDLib2
- unit tests for `CameraHistory` and `AtlasSearchIndex` pass
- manual QA checklist (above) passes on
  `/slot test populate organized` and `/slot test populate late-modpack`
