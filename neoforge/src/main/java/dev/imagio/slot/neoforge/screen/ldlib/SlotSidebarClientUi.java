package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.holder.IModularUIHolderMenu;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.mixin.ScreenInvoker;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.AtlasItemDrag;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

/**
 * Client-side bridge that hides the LDLib2 surface from the rest of
 * the {@code neoforge} module — required by
 * {@code ArchitectureDependencyTest.ldlibImportsStayIsolatedToLdlibScreenPackage()},
 * which forbids {@code import com.lowdragmc.lowdraglib2.*} outside the
 * {@code screen.ldlib} package.
 *
 * <p>Two surfaces:
 *
 * <ol>
 *   <li>{@link #mount} — called from {@code SlotContainerSidebar} on
 *       host screen open; builds the workspace UI, attaches it to the
 *       player's host menu so {@code PacketModularUISync} can route
 *       sync packets, and stashes the active session for the
 *       cross-surface bridge.</li>
 *   <li>{@link #tryConsumeAtlasDragForHostSlot},
 *       {@link #isActive}, {@link #release} — read/end the active
 *       session's drag state so the {@code MouseButtonReleased} hook
 *       in {@code client.screen} can route a wall-card drag release
 *       to the appropriate cross-surface RPC without ever touching an
 *       LDLib2 type itself.</li>
 * </ol>
 */
public final class SlotSidebarClientUi {
    @Nullable
    private static ActiveMount activeMount;

    /**
     * Preferred sidebar width (in screen px) for the SLOT sidebar.
     * Same fixed width that the standalone surface uses — the
     * workspace renders the same widget tree at the same dimensions
     * regardless of whether it's mounted as a sidebar inside a vanilla
     * container screen or opened standalone via the player-inventory
     * key. The canonical constant lives on
     * {@link SlotWorkspaceUiController#WORKSPACE_WIDTH_PX}.
     */
    public static int preferredSidebarWidth() {
        return SlotWorkspaceUiController.WORKSPACE_WIDTH_PX;
    }

    private SlotSidebarClientUi() {
    }

    /**
     * Mount a sidebar workspace as a child widget of the host screen.
     *
     * <p>The widget renders at screen origin {@code (0, 0)} with the
     * given sidebar bounds; LDLib2's {@code ScreenMixin} +
     * {@code ContainerEventHandlerMixin} pump tick / removed /
     * keyPressed / mouseDragged / mouseMoved by walking
     * {@code screen.children()} for {@code IModularUIHolder}.
     *
     * <p>The sidebar's {@code ModularUI} is also attached to the host
     * {@code AbstractContainerMenu} via LDLib2's
     * {@code IModularUIHolderMenu} interface, so that
     * {@code PacketModularUISync} (which routes through
     * {@code player.containerMenu instanceof IUISyncManagerHolder})
     * can deliver server-pushed view-model state and forward
     * client-side RPCs. Without this attachment the sync layer
     * silently drops all sidebar packets.
     *
     * @return true if mounted, false if the host screen is not an
     *         {@link AbstractContainerScreen} (sidebar mount only
     *         supports container screens)
     */
    public static boolean mount(Player player, Screen hostScreen, int sidebarWidth, int sidebarHeight) {
        if (!(hostScreen instanceof AbstractContainerScreen<?> containerScreen)) {
            return false;
        }
        SlotWorkspaceUiSession session = new SlotWorkspaceUiSession(player);
        SlotWorkspaceUiController controller = new SlotWorkspaceUiController(session, player);
        ModularUI sidebar = controller.create();
        sidebar.init(sidebarWidth, sidebarHeight);
        ((ScreenInvoker) containerScreen).slot$addRenderableWidget(sidebar.getWidget());

        if (containerScreen.getMenu() instanceof IModularUIHolderMenu holder) {
            ModularUI existing = holder.getModularUI();
            if (existing != null && existing != sidebar) {
                SlotDebugLog.log(
                        "[SLOT][sidebar] host menu {} already carries a ModularUI on the client; not attaching",
                        containerScreen.getMenu().getClass().getName()
                );
            } else {
                holder.setModularUI(sidebar);
            }
        }
        activeMount = new ActiveMount(controller, sidebar);
        return true;
    }

    /** Drop the static handle so a fresh sidebar can mount on the next host screen. */
    public static void release() {
        activeMount = null;
    }

    public static boolean isActive() {
        return activeMount != null;
    }

    /**
     * Dispatch a CHAR_TYPED event into the active sidebar's widget tree,
     * targeted at the controller root. Bypasses {@code ModularUI.charTyped}'s
     * focused-element check so the sidebar's hotkeys ({@code /} for
     * search, etc.) work even when the host screen owns focus and our
     * sidebar widget never becomes the focused child of the host.
     *
     * <p>{@link dev.imagio.slot.neoforge.client.screen.SlotContainerSidebar}
     * subscribes to NeoForge's
     * {@code ScreenEvent.CharacterTyped.Pre} and feeds chars in here;
     * if any listener consumed the event the caller should cancel the
     * screen event so vanilla doesn't see the keystroke twice.
     *
     * <p>Returns true when at least one listener handled the event.
     */
    public static boolean dispatchCharTyped(char codePoint, int modifiers) {
        ActiveMount mount = activeMount;
        if (mount == null) {
            return false;
        }
        com.lowdragmc.lowdraglib2.gui.ui.UIElement root = mount.controller.root;
        if (root == null) {
            return false;
        }
        com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event =
                com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent.create(
                        com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents.CHAR_TYPED);
        event.codePoint = codePoint;
        event.modifiers = modifiers;
        event.target = root;
        com.lowdragmc.lowdraglib2.gui.ui.event.UIEventDispatcher.dispatchEvent(event);
        return event.hasHandler;
    }

    /**
     * Same shape as {@link #dispatchCharTyped} for KEY_DOWN — routes the
     * keystroke to the sidebar root so the hotbar / undo / open-vanilla
     * / search-modal hotkeys fire while the sidebar is mounted on a
     * non-LDLib2 host screen.
     */
    public static boolean dispatchKeyPressed(int keyCode, int scanCode, int modifiers) {
        ActiveMount mount = activeMount;
        if (mount == null) {
            return false;
        }
        com.lowdragmc.lowdraglib2.gui.ui.UIElement root = mount.controller.root;
        if (root == null) {
            return false;
        }
        com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event =
                com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent.create(
                        com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents.KEY_DOWN);
        event.keyCode = keyCode;
        event.scanCode = scanCode;
        event.modifiers = modifiers;
        event.target = root;
        com.lowdragmc.lowdraglib2.gui.ui.event.UIEventDispatcher.dispatchEvent(event);
        return event.hasHandler;
    }

    /**
     * If the active sidebar is mid-drag with an {@link AtlasItemDrag}
     * payload, fire {@code sendCrossSurfaceDropOnHostSlot(identity,
     * hostSlotIndex)} and end the drag. Returns true when consumed —
     * caller (the {@code MouseButtonReleased.Pre} listener) should
     * cancel the screen event so vanilla doesn't process the release
     * as a stray click.
     *
     * <p>Returns false (and leaves the drag alone) if no drag is
     * active, the payload isn't an atlas item, or no sidebar is
     * mounted. The non-atlas-payload case lets section reorder /
     * hotbar / kit drags fall through to their existing in-sidebar
     * handlers via {@code stopDrag} happening naturally.
     */
    public static boolean tryConsumeAtlasDragForHostSlot(int hostSlotIndex, double mouseX, double mouseY) {
        ActiveMount mount = activeMount;
        if (mount == null) {
            SlotDebugLog.log("[xsurface][bridge] consume hostSlot={} skipped: no active mount", hostSlotIndex);
            return false;
        }
        boolean dragging = mount.modularUI.getDragHandler().isDragging();
        Object payload = mount.modularUI.getDragHandler().getDraggingObject();
        SlotDebugLog.log(
                "[xsurface][bridge] consume hostSlot={} dragging={} payloadType={}",
                hostSlotIndex,
                dragging,
                payload == null ? "null" : payload.getClass().getSimpleName()
        );
        if (!dragging) {
            return false;
        }
        if (!(payload instanceof AtlasItemDrag atlasDrag)) {
            return false;
        }
        SlotWorkspaceViewModel.IdentityRef identity = atlasDrag.identity();
        SlotDebugLog.log(
                "[xsurface][bridge] dispatching DropOnHostSlot identity={} hostSlot={}",
                identity == null ? "null" : identity.itemId(),
                hostSlotIndex
        );
        mount.controller.rpc.sendCrossSurfaceDropOnHostSlot(identity, hostSlotIndex);
        mount.modularUI.getDragHandler().stopDrag();
        // Forward the release into LDLib2 so its mouse-down tracker clears.
        // We're about to cancel the screen event (so vanilla's
        // mouseReleased won't iterate children to clean up state), and
        // the widget's hit-test would skip itself anyway since the
        // cursor is over a vanilla slot — without this, lastMouseDownButton
        // stays at 0 and the next MOUSE_LEAVE on a wall card sees
        // isMouseDown(0)=true and starts a phantom drag with no button held.
        mount.modularUI.getWidget().mouseReleased(mouseX, mouseY, 0);
        return true;
    }

    private record ActiveMount(SlotWorkspaceUiController controller, ModularUI modularUI) {
    }
}
