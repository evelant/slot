package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.action.WorkspaceActionId;
import dev.imagio.slot.ui.workspace.StorageGhostRevealMode;
import dev.imagio.slot.ui.workspace.WorkspaceGatherUiSupport;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

final class HotkeyRouter {
    private final SlotWorkspaceUiController host;
    private boolean markWantedKeyConsumed;
    private boolean setWantedHoverKeyConsumed;
    private boolean storageXrayKeyConsumed;

    HotkeyRouter(SlotWorkspaceUiController host) {
        this.host = host;
    }

    void installBeltHotkeys() {
        host.root.setEnforceFocus(event -> {
        });
        host.root.addEventListener(UIEvents.MUI_CHANGED, event -> host.root.focus());
        host.root.addEventListener(UIEvents.KEY_DOWN, host.searchController::handleKeyDown, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleCursorCancelKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleFocusHoveredItemKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleAutoHotbarKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleBeltHotkey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleSetWantedHoverKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleMarkWantedKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleGoalRecipeKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleCycleKitPageKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleGatherActiveKitKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleStorageXrayKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleUndoRedoKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleOpenVanillaKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleRelevanceDebugOverlayKey, true);
        host.root.addEventListener(UIEvents.CHAR_TYPED, event -> {
            if (event.codePoint >= '1' && event.codePoint <= '9') {
                event.stopPropagation();
            }
        }, true);
        host.root.addEventListener(UIEvents.CHAR_TYPED, host.searchController::handleCharTyped, true);
        host.root.addEventListener(UIEvents.TICK, event -> {
            host.flushWheelTransferBatch();
            host.flushRebuildIfPending();
            host.applyPendingWallScrollRestore();
        });
        host.root.addEventListener(UIEvents.TICK, event -> host.searchController.tickIdleTimer());
        host.root.addEventListener(UIEvents.TICK, event -> host.shiftClickTransferState.observeShiftDown(Screen.hasShiftDown()));
        host.root.addEventListener(UIEvents.TICK, event -> {
            if (!dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings.markWantedDown()) {
                markWantedKeyConsumed = false;
            }
            if (!dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings.setWantedHoverDown()) {
                setWantedHoverKeyConsumed = false;
            }
            if (!dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings.storageXrayDown()) {
                storageXrayKeyConsumed = false;
            }
        });
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
        if (host.goalTabActive()) {
            event.stopPropagation();
            host.localStatus.set("goal tab is browse only");
            host.rebuild();
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

    void handleAutoHotbarKey(UIEvent event) {
        if (event.keyCode != GLFW.GLFW_KEY_TAB || Screen.hasShiftDown()
                || isTextInputFocused() || Screen.hasControlDown()) {
            return;
        }
        SlotWorkspaceViewModel.AtlasItem target = host.hoveredAtlasItem();
        if (target == null) {
            return;
        }
        event.stopPropagation();
        if (host.goalTabActive()) {
            host.localStatus.set("goal tab is browse only");
            host.rebuild();
            return;
        }
        host.searchController.confirmForHotbar();
        host.rpc.sendAssignToAutoHotbar(target);
    }

    void handleFocusHoveredItemKey(UIEvent event) {
        if (event.keyCode != GLFW.GLFW_KEY_TAB || !Screen.hasShiftDown()
                || isTextInputFocused() || Screen.hasControlDown()) {
            return;
        }
        SlotWorkspaceViewModel.AtlasItem target = host.hoveredAtlasItem();
        if (target == null) {
            return;
        }
        event.stopPropagation();
        host.focusWallItem(target);
    }

    boolean isTextInputFocused() {
        var mui = host.root.getModularUI();
        if (mui == null) {
            return false;
        }
        UIElement focused = mui.getFocusedElement();
        return focused != null && focused != host.root && focused instanceof TextField;
    }

    void handleGoalRecipeKey(UIEvent event) {
        if (isTextInputFocused() || host.searchController.modalActive() || Screen.hasControlDown()) {
            return;
        }
        if (event.keyCode != GLFW.GLFW_KEY_R && event.keyCode != GLFW.GLFW_KEY_U) {
            return;
        }
        event.stopPropagation();
        SlotWorkspaceViewModel.AtlasItem target = host.hoveredAtlasItem();
        if (target == null) {
            host.localStatus.set("hover an item for recipe or usage details");
            host.rebuild();
            return;
        }
        if (event.keyCode == GLFW.GLFW_KEY_R) {
            host.openRecipe(target);
        } else {
            host.openUses(target);
        }
    }

    void handleMarkWantedKey(UIEvent event) {
        if (isTextInputFocused() || host.searchController.modalActive() || Screen.hasControlDown()) {
            return;
        }
        if (!dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesMarkWanted(event.keyCode, event.scanCode)) {
            return;
        }
        event.stopPropagation();
        if (isAltKey(event.keyCode)) {
            return;
        }
        if (markWantedKeyConsumed) {
            return;
        }
        markWantedKeyConsumed = true;
        if (host.goalTabActive()) {
            host.localStatus.set("goal tab is browse only");
            host.rebuild();
            return;
        }
        SlotWorkspaceViewModel.AtlasItem target = host.hoveredAtlasItem();
        if (target == null) {
            host.localStatus.set("hover an item to mark wanted");
            host.rebuild();
            return;
        }
        host.rpc.sendToggleWantedItem(target.identity());
    }

    void handleSetWantedHoverKey(UIEvent event) {
        if (isTextInputFocused() || host.searchController.modalActive() || Screen.hasControlDown()) {
            return;
        }
        if (!dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesSetWantedHover(event.keyCode, event.scanCode)) {
            return;
        }
        event.stopPropagation();
        if (setWantedHoverKeyConsumed) {
            return;
        }
        setWantedHoverKeyConsumed = true;
        if (host.goalTabActive()) {
            host.localStatus.set("goal tab is browse only");
            host.rebuild();
            return;
        }
        SlotWorkspaceViewModel.AtlasItem target = host.hoveredAtlasItem();
        if (target == null) {
            host.localStatus.set("hover an item to mark wanted");
            host.rebuild();
            return;
        }
        host.rpc.sendSetWantedCount(target.identity(), wantedHoverTargetCount(target));
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
     * In-screen handler for the gather hotkey. Uses the shared workspace
     * action; the in-world client-tick path sends a packet that delegates
     * to the same common service.
     */
    void handleGatherActiveKitKey(UIEvent event) {
        if (isTextInputFocused() || host.searchController.modalActive()) {
            return;
        }
        if (!dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesGatherActiveKit(event.keyCode, event.scanCode)) {
            return;
        }
        if (host.goalTabActive()) {
            event.stopPropagation();
            host.localStatus.set("goal tab is browse only");
            host.rebuild();
            return;
        }
        if (!anyGatherableIdentity()) {
            host.localStatus.set("nothing to gather");
            host.rebuild();
            return;
        }
        event.stopPropagation();
        host.rpc.send(WorkspaceActionId.GATHER_ACTIVE_KIT);
        host.localStatus.set("gathering desired items from nearby chests");
        host.rebuild();
    }

    void handleStorageXrayKey(UIEvent event) {
        if (isTextInputFocused() || host.searchController.modalActive()) {
            return;
        }
        if (!dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesStorageXray(event.keyCode, event.scanCode)) {
            return;
        }
        event.stopPropagation();
        if (storageXrayKeyConsumed) {
            return;
        }
        storageXrayKeyConsumed = true;
        host.toggleStorageGhostRevealMode(Screen.hasShiftDown()
                ? StorageGhostRevealMode.TRACKED
                : StorageGhostRevealMode.PROXIMATE);
    }

    private boolean anyGatherableIdentity() {
        for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
            if (WorkspaceGatherUiSupport.isGatherableItem(item)) {
                return true;
            }
        }
        return false;
    }

    private int wantedHoverTargetCount(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null) {
            return 1;
        }
        if (host.recipeSidebarActive() && item.desiredCount() > 0) {
            return item.desiredCount();
        }
        return 1;
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
     * In-screen handler for the "open vanilla inventory" binding. The key is
     * intentionally scoped to SLOT surfaces instead of being consumed from the
     * global client tick.
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

    private static boolean isAltKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT;
    }

}
