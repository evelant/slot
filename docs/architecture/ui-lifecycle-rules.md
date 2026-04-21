# SLOT UI Lifecycle Rules

Last updated: 2026-04-21

Operational rules derived from flicker / flash / desync bugs we hit while
building the LDLib2 workspace. Each rule includes the concrete failure it
prevents so you can recognise the same class of bug if it resurfaces.

For the overall UI architecture see [host-ui.md](host-ui.md).

---

## Rendering & Rebuild

**Never mutate the element tree inside a render-path callback.**
`drawBackgroundTexture`, `drawForegroundTexture`, per-frame ticks that fire
during the draw walk — by the time you hit them, ancestors up the tree have
already committed their layout for the frame. Mutating children here means
either (a) outer elements drew the old tree while inner elements drew the
new one, or (b) Taffy doesn't settle the layout until next frame. Both
surface as a 1-frame blank-frame flash.

*Fix pattern:* hook structural mutations to the game tick (`root.TICK`
listener, dispatched by `ModularUI.tick()` ahead of any render), or to an
explicit pre-render callback if one exists. In
`SlotWorkspaceUiFactory.create()`, `flushRebuildIfPending` is the first
thing registered on `root.TICK`.

**Coalesce rebuilds to one per tick, not per request.** Multiple
`rebuild()` calls within a single tick (a server sync + a local state
change + an observable callback) should collapse to one tree rebuild. Use
a `rebuildPending` flag flipped by `rebuild()` and cleared by the flush
listener.

**Build persistent scaffolding once; refresh inner content on subsequent
rebuilds.** For a UI with a stable top-level shape (header / body /
statusBar here), create those children on the first rebuild and keep them
forever. Subsequent rebuilds only repopulate the part that actually
changed (atlas content). Wholesale `clearAllChildren` of the top-level
container is both wasteful and visible, especially when it also invalidates
event listener subscriptions in persistent children.

**Initialize scale/state-dependent visuals at build time, not only via
TICK.** Between element creation and the first screen tick (~50 ms worst
case) the element renders with whatever values the builder set. Priming
the initial state — for example invoking `applyHeaderScale.run()` on the
island title bar immediately after registering its TICK listener, and
seeding `initialWorldFont` in `anchorTextBand` — eliminates the flash of
"untouched default fontSize" between creation and first TICK.

---

## Scale & Camera Animation

**Size anything the pose stack will multiply against using
`atlas.getScale()`, not `animationTargetScale(atlas)`.**
`animationTargetScale` returns the animation's target scale, i.e. the
future. The pose stack renders at the interpolated *current* scale.
Baking a label with `fontSize = screenPx / target` and rendering it at
`fontSize × currentScale` during an animation produces sizes that are off
by `currentScale / target`. For zoom-out peeks (target &lt; current) this
manifests as a giant-text flash; for zoom-in peeks, tiny text.

Concretely: `anchorTextBand` uses `atlas.getScale()` for `initialScale`
and in its band TICK. The island header's `applyHeaderScale` does the
same.

**Gate LOD rebuilds on `!cameraController.isAnimating()`.**
`atlasBudget` uses `animationTargetScale` so the LOD *signature*
previews what the destination LOD will be, but rendering during the
animation still happens at interpolated scales. If a rebuild fires at
animation start, the new body is baked for the target LOD but drawn at
the current scale — a guaranteed mid-animation mismatch. Let cards keep
their pre-animation LOD through the animation; re-evaluate signature
only when the camera settles. See `atlasCardButton`'s TICK.

**Peek tap threshold should accommodate real human tap latency.**
Keep `PEEK_TAP_THRESHOLD_MS` above ~200 ms. A 100 ms cap treats almost
every intentional tap as a hold-then-snapback.

---

## Inventory Source Iteration

**Iterate semantic categories, not hardcoded lane lists.** `PLAYER_MAIN`,
`PLAYER_QUICK_ACCESS_LANE_0`, and `PLAYER_OFFHAND` are not the complete
set of carried sources — providers (Sophisticated Backpacks and future
integrations) contribute additional sources at runtime via
`PlayerInventoryExtension.additionalSources()`. Always walk
`authority.carriedSources()` (i.e. every source with
`InventoryPaneMembership.CARRIED`) when the intent is "every carried
item". A hardcoded list was why backpack contents silently never appeared
in the atlas or triage.

**Atlas-item → hotbar actions must be identity-based, not slot-index-
based.** The client side only has `AtlasItem.firstSlotIndex()` — a plain
integer with no source ID. Sending
`TARGET_MAIN_SLOT + firstSlotIndex` assumes the item lives in
`PLAYER_MAIN`; for items in a backpack the slot is empty there and the
transfer rejects with `empty_source`. Use an RPC that takes
`(identity, hotbarIndex)` and resolves the source on the server by
scanning every CARRIED source.

---

## Client–Server Container State

**`Minecraft.setScreen(new X)` alone does not close the server-side
menu.** When swapping the client's active screen while a custom
container menu is open server-side, slot clicks in the new screen either
target the wrong menu or silently no-op. Symptom: actions look like they
worked client-side (visual state updates) but don't persist through a
reconnect. Explicitly call `player.closeContainer()` on the client
before `setScreen()` when replacing a `ModularUI` screen with a vanilla
one. `SlotWorkspaceMountController.openVanillaInventory()` does this for
the "Vanilla" escape hatch.

---

## Debugging Technique

**When blamed code is the suspect, neuter it in observe-only mode before
investigating.** Introduce a constant flag that routes the code into
"log only, don't mutate" behaviour. If the bug still reproduces with
writes disabled, the suspect is innocent — move on. Applied to
`SlotPickupRouter` this was the fastest way to prove the pickup
interceptor wasn't causing the backpack desync (which turned out to be
the Vanilla button's missing close packet).

**Don't over-interpret a single log line.** `player_slot_not_insertable`
with `mainSlots=[] hotbarSlots=[]` looks alarming but is expected when
the inventory is genuinely full: the destination slot that the user
asked for just can't accept the stack. Check whether the observable
precondition (inventory full, not-proximate, etc.) makes the failure
expected before hypothesising a deeper bug.
