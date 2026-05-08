package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

final class HotkeyRouter {
    private final SlotWorkspaceUiController host;

    HotkeyRouter(SlotWorkspaceUiController host) {
        this.host = host;
    }

    void installBeltHotkeys() {
        host.root.setEnforceFocus(event -> {
        });
        host.root.addEventListener(UIEvents.MUI_CHANGED, event -> host.root.focus());
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleCursorCancelKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleBeltHotkey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, host.searchController::handleKeyDown, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleCycleKitPageKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleGatherActiveKitKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleUndoRedoKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleOpenVanillaKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleRelevanceDebugOverlayKey, true);
        host.root.addEventListener(UIEvents.CHAR_TYPED, event -> {
            if (event.codePoint >= '1' && event.codePoint <= '9') {
                event.stopPropagation();
            }
        }, true);
        host.root.addEventListener(UIEvents.CHAR_TYPED, host.searchController::handleCharTyped, true);
        host.root.addEventListener(UIEvents.TICK, event -> host.flushRebuildIfPending());
        host.root.addEventListener(UIEvents.TICK, event -> host.searchController.tickIdleTimer());
    }

    void handleCursorCancelKey(UIEvent event) {
        if (event.keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return;
        }
        if (!WorkspaceCursorState.isCarrying()) {
            return;
        }
        event.stopPropagation();
        host.rpc.sendCursorCancel();
    }

    void handleBeltHotkey(UIEvent event) {
        int digit = digitFromKeyCode(event.keyCode);
        if (digit < 1 || digit > 9) {
            return;
        }
        event.stopPropagation();
        SlotWorkspaceViewModel.AtlasItem target = host.hoveredAtlasItem();
        if (target == null) {
            host.localStatus.set("hover an atlas item to assign with 1-9");
            host.rebuild();
            return;
        }
        host.searchController.confirmForHotbar();
        host.rpc.sendAssignToHotbarSlot(target, digit - 1);
    }

    boolean isTextInputFocused() {
        var mui = host.root.getModularUI();
        if (mui == null) {
            return false;
        }
        UIElement focused = mui.getFocusedElement();
        return focused != null && focused != host.root && focused instanceof TextField;
    }

    void handleCycleKitPageKey(UIEvent event) {
        if (isTextInputFocused() || host.searchController.modalActive()) {
            return;
        }
        if (!dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesCycleKitPage(event.keyCode, event.scanCode)) {
            return;
        }
        SlotWorkspaceViewModel.KitCard active = host.viewModel.activeKit();
        if (active == null || active.pageCount() <= 1) {
            return;
        }
        event.stopPropagation();
        int direction = Screen.hasShiftDown() ? -1 : 1;
        host.rpc.sendSwitchKitPage(direction);
    }

    /**
     * In-screen handler for the gather-active-kit hotkey. Sends the
     * same packet as the in-world client-tick path so server-side
     * behaviour is identical regardless of where the player triggered
     * it. Stopping propagation prevents the keymap's
     * {@code consumeClick} loop in {@code SlotNeoForgeClient.onClientTick}
     * from also firing — the in-tick loop skips when a screen is open
     * but the keymap still records the click, so swallowing the event
     * here keeps things tidy.
     */
    void handleGatherActiveKitKey(UIEvent event) {
        if (isTextInputFocused() || host.searchController.modalActive()) {
            return;
        }
        if (!dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesGatherActiveKit(event.keyCode, event.scanCode)) {
            return;
        }
        SlotWorkspaceViewModel.KitCard active = host.viewModel.activeKit();
        if (active == null) {
            host.localStatus.set("activate a kit first");
            host.rebuild();
            return;
        }
        event.stopPropagation();
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new dev.imagio.slot.neoforge.network.SlotGatherActiveKitPayload());
        host.localStatus.set("gathering active kit from nearby chests");
        host.rebuild();
    }

    void handleUndoRedoKey(UIEvent event) {
        if (isTextInputFocused() || host.searchController.modalActive()) {
            return;
        }
        if (dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesUndo(event.keyCode, event.scanCode)) {
            event.stopPropagation();
            host.rpc.sendUndo();
        } else if (dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesRedo(event.keyCode, event.scanCode)) {
            event.stopPropagation();
            host.rpc.sendRedo();
        }
    }

    /**
     * In-screen handler for the "open vanilla inventory" binding. The
     * {@code consumeClick} loop in {@code SlotNeoForgeClient.onClientTick}
     * only fires when no screen is open; while the SLOT atlas is up,
     * keyboard events are routed to the screen first, so we re-check the
     * binding here and bail out to vanilla on match.
     *
     * <p>Context-sensitive: when a loot chest panel is showing (i.e.
     * the player walked up to / right-clicked an unclaimed chest and
     * landed in the SLOT workspace), the binding opens the vanilla
     * chest GUI for that chest instead of the player inventory. That's
     * the only path that lets the player deposit into and claim a
     * fresh chest. Falls back to the player inventory otherwise.
     */
    void handleOpenVanillaKey(UIEvent event) {
        if (isTextInputFocused() || host.searchController.modalActive()) {
            return;
        }
        if (!dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesOpenVanilla(event.keyCode, event.scanCode)) {
            return;
        }
        event.stopPropagation();
        dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel.LootChestPanel lootPanel
                = host.viewModel.lootChestPanel();
        if (lootPanel.isPresent()) {
            host.rpc.sendLootChestOpenVanilla(lootPanel);
            return;
        }
        dev.imagio.slot.neoforge.client.screen.SlotWorkspaceMountController.openVanillaInventory();
    }

    void handleRelevanceDebugOverlayKey(UIEvent event) {
        if (isTextInputFocused() || host.searchController.modalActive()) {
            return;
        }
        if (!dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesRelevanceDebugOverlay(event.keyCode, event.scanCode)) {
            return;
        }
        event.stopPropagation();
        RelevanceDebugOverlay.toggle();
        host.rebuild();
    }

    int digitFromKeyCode(int keyCode) {
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
            return keyCode - GLFW.GLFW_KEY_1 + 1;
        }
        if (keyCode >= GLFW.GLFW_KEY_KP_1 && keyCode <= GLFW.GLFW_KEY_KP_9) {
            return keyCode - GLFW.GLFW_KEY_KP_1 + 1;
        }
        return 0;
    }

}
