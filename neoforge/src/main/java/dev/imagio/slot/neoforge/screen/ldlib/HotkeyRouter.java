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
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleBeltHotkey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, host.searchController::handleKeyDown, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handlePeekKeyDown, true);
        host.root.addEventListener(UIEvents.KEY_UP, this::handlePeekKeyUp, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleCameraHistoryKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleCycleKitPageKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleUndoRedoKey, true);
        host.root.addEventListener(UIEvents.KEY_DOWN, this::handleRelevanceDebugOverlayKey, true);
        host.root.addEventListener(UIEvents.MOUSE_DOWN, this::handleCameraHistoryMouse, true);
        host.root.addEventListener(UIEvents.CHAR_TYPED, event -> {
            if (event.codePoint >= '1' && event.codePoint <= '9') {
                event.stopPropagation();
            }
        }, true);
        host.root.addEventListener(UIEvents.CHAR_TYPED, host.searchController::handleCharTyped, true);
        // Flush pending rebuilds FIRST thing each game tick, before any
        // render can start. Running this from atlas.setPerFrameTick (inside
        // drawBackgroundTexture) caused a visible 1-frame flash on
        // server-driven rebuilds: by the time the flush ran, ancestors
        // (content, body, atlasPanelElement) had already drawn this frame
        // with the old tree, so newly rebuilt children rendered inside a
        // momentarily stale parent layout. ModularUI.tick fires once per
        // game tick before the next render, so the host.rebuild is complete
        // before anything tries to draw.
        host.root.addEventListener(UIEvents.TICK, event -> host.flushRebuildIfPending());
        host.root.addEventListener(UIEvents.TICK, event -> host.cameraController.tick());
        host.root.addEventListener(UIEvents.TICK, event -> host.searchController.tickIdleTimer());
    }

    void handleBeltHotkey(UIEvent event) {
        int digit = digitFromKeyCode(event.keyCode);
        if (digit < 1 || digit > 9) {
            return;
        }
        event.stopPropagation();
        SlotWorkspaceViewModel.AtlasItem target = host.hoveredAtlasItem();
        if (target == null) {
            target = host.selectedAtlasItem();
        }
        if (target == null) {
            host.localStatus.set("hover or select an atlas item to assign with 1-9");
            host.rebuild();
            return;
        }
        host.rpc.sendAssignToHotbarSlot(target, digit - 1);
    }

    void handlePeekKeyDown(UIEvent event) {
        if (event.keyCode != GLFW.GLFW_KEY_SPACE) {
            return;
        }
        if (isTextInputFocused() || host.searchController.modalActive()) {
            return;
        }
        if (host.peekActive) {
            return;
        }
        if (host.cameraController.isDragging()) {
            return;
        }
        AtlasCamera target = host.camera.resolvePeekTarget();
        if (target == null) {
            return;
        }
        event.stopPropagation();
        host.cameraController.recordOrigin();
        host.peekTarget = target;
        host.peekPressTimeMs = System.currentTimeMillis();
        host.peekActive = true;
        host.cameraController.ease(target, AtlasCameraController.CUBIC_IN_OUT, AtlasCameraController.PEEK_DURATION_MS);
    }

    void handlePeekKeyUp(UIEvent event) {
        if (event.keyCode != GLFW.GLFW_KEY_SPACE) {
            return;
        }
        if (!host.peekActive) {
            return;
        }
        event.stopPropagation();
        long heldMs = System.currentTimeMillis() - host.peekPressTimeMs;
        AtlasCamera target = host.peekTarget;
        AtlasCamera origin = host.cameraController.origin();
        host.peekActive = false;
        host.peekTarget = null;
        host.cameraController.clearOrigin();
        if (heldMs <= AtlasCameraController.PEEK_TAP_THRESHOLD_MS && target != null) {
            host.cameraController.commitFrom(
                    origin,
                    target,
                    AtlasCameraController.CommitSource.HOVER_GOTO,
                    AtlasCameraController.CUBIC_IN_OUT,
                    AtlasCameraController.COMMIT_DURATION_MS);
        } else if (origin != null) {
            host.cameraController.ease(
                    origin,
                    AtlasCameraController.CUBIC_IN_OUT,
                    AtlasCameraController.PEEK_SNAPBACK_DURATION_MS);
        }
    }

    boolean isTextInputFocused() {
        var mui = host.root.getModularUI();
        if (mui == null) {
            return false;
        }
        UIElement focused = mui.getFocusedElement();
        return focused != null && focused != host.root && focused instanceof TextField;
    }

    void handleCameraHistoryKey(UIEvent event) {
        if (isTextInputFocused() || host.searchController.modalActive()) {
            return;
        }
        if (dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesBackKey(event.keyCode, event.scanCode)) {
            event.stopPropagation();
            performCameraBack();
        } else if (dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesForwardKey(event.keyCode, event.scanCode)) {
            event.stopPropagation();
            performCameraForward();
        }
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

    void handleCameraHistoryMouse(UIEvent event) {
        if (dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesBackMouse(event.button)) {
            event.stopPropagation();
            performCameraBack();
        } else if (dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesForwardMouse(event.button)) {
            event.stopPropagation();
            performCameraForward();
        }
    }

    void performCameraBack() {
        if (!host.cameraController.back()) {
            host.localStatus.set("no further camera history");
            host.rebuild();
        }
    }

    void performCameraForward() {
        if (!host.cameraController.forward()) {
            host.localStatus.set("at latest camera");
            host.rebuild();
        }
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
