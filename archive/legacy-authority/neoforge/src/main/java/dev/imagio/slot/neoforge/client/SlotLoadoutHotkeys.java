package dev.imagio.slot.neoforge.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;
import dev.imagio.slot.client.screen.InventoryActionOrchestrator;
import dev.imagio.slot.client.screen.QuickAccessMutationResult;
import dev.imagio.slot.client.screen.QuickAccessFollowUpState;
import dev.imagio.slot.client.screen.QuickAccessPendingState;
import dev.imagio.slot.client.screen.QuickAccessService;
import dev.imagio.slot.client.screen.RecentLootTracker;
import dev.imagio.slot.client.screen.SlotActionOutcomeState;
import dev.imagio.slot.client.screen.SlotActionResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SlotLoadoutHotkeys {
    private static final String HOTKEY_ROUTING_KEY = "workflow:loadout-hotkey";
    private static final long PENDING_REQUEST_TIMEOUT_NANOS = 60_000_000_000L;
    private static int lastTriggeredHotkey = -1;
    private static final Map<String, Long> pendingRequestIds = new LinkedHashMap<>();

    private SlotLoadoutHotkeys() {
    }

    static void reset() {
        lastTriggeredHotkey = -1;
        pendingRequestIds.clear();
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        applyConfirmedHotkeyOutcomes(minecraft);
        if (minecraft.player == null || !SlotNeoForgeClient.settingsController().slotEnabled()) {
            reset();
            return;
        }
        if (minecraft.screen instanceof ChatScreen) {
            lastTriggeredHotkey = -1;
            return;
        }
        if (minecraft.screen != null) {
            lastTriggeredHotkey = -1;
            return;
        }

        int hotkey = activeLoadoutHotkey(minecraft);
        if (hotkey < 0) {
            lastTriggeredHotkey = -1;
            return;
        }
        if (hotkey == lastTriggeredHotkey) {
            return;
        }

        HotbarLoadoutDefinition loadout = SlotNeoForgeClient.collectionStore().loadoutForHotkey(hotkey);
        lastTriggeredHotkey = hotkey;
        if (loadout == null || QuickAccessPendingState.hasPendingTargets() || QuickAccessFollowUpState.hasPendingActions()) {
            return;
        }

        QuickAccessMutationResult result =
                new QuickAccessService(null, HOTKEY_ROUTING_KEY, false).applyLoadoutMutation(loadout);
        if (result.changed()) {
            RecentLootTracker.suppressPositiveDeltas();
            recordPendingRequests(result);
            showFeedback(minecraft, result.transferSyncExpected()
                    ? SlotActionResult.requested(Component.translatable("slot.screen.action.outcome.generic.requested"))
                    : SlotActionResult.applied(Component.translatable("slot.screen.action.outcome.generic.applied")));
        }
        SlotDebugLog.log(
                "Loadout hotkey triggered: hotkey={} loadout={} applied={}",
                hotkey + 1,
                loadout.id(),
                result.changed()
        );
    }

    private static void applyConfirmedHotkeyOutcomes(Minecraft minecraft) {
        pruneExpiredPendingRequests();
        if (minecraft == null || pendingRequestIds.isEmpty()) {
            return;
        }

        List<SlotActionOutcomeState.PublishedOutcome> outcomes =
                SlotActionOutcomeState.pollMatching(HOTKEY_ROUTING_KEY, pendingRequestIds.keySet());
        if (outcomes.isEmpty()) {
            return;
        }

        for (SlotActionOutcomeState.PublishedOutcome outcome : outcomes) {
            if (outcome != null && outcome.requestId() != null && !outcome.requestId().isBlank()) {
                pendingRequestIds.remove(outcome.requestId());
            }
        }

        InventoryActionOrchestrator.OutcomeSummary summary = InventoryActionOrchestrator.summarizeOutcomes(outcomes);
        showFeedback(minecraft, summary.feedback());
    }

    private static void recordPendingRequests(QuickAccessMutationResult result) {
        if (result == null || !result.transferSyncExpected()) {
            return;
        }
        long now = System.nanoTime();
        for (QuickAccessMutationResult.RequestedChange change : result.pendingChanges()) {
            if (change == null || change.requestId() == null || !change.requestId().present()) {
                continue;
            }
            pendingRequestIds.put(change.requestId().value(), now);
        }
    }

    private static void pruneExpiredPendingRequests() {
        if (pendingRequestIds.isEmpty()) {
            return;
        }

        long now = System.nanoTime();
        pendingRequestIds.entrySet().removeIf(entry -> entry == null
                || entry.getValue() == null
                || now - entry.getValue() > PENDING_REQUEST_TIMEOUT_NANOS);
    }

    private static void showFeedback(Minecraft minecraft, SlotActionResult result) {
        if (minecraft == null || minecraft.gui == null || result == null || !result.visible()) {
            return;
        }
        minecraft.gui.setOverlayMessage(result.message(), false);
    }

    private static int activeLoadoutHotkey(Minecraft minecraft) {
        long window = minecraft.getWindow().getWindow();
        if (!primaryModifierDown(window)) {
            return -1;
        }

        int activeHotkey = -1;
        for (int hotkey = 0; hotkey < 9; hotkey++) {
            int keyCode = GLFW.GLFW_KEY_1 + hotkey;
            if (!InputConstants.isKeyDown(window, keyCode)) {
                continue;
            }
            if (activeHotkey >= 0) {
                return -1;
            }
            activeHotkey = hotkey;
        }
        return activeHotkey;
    }

    private static boolean primaryModifierDown(long window) {
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SUPER)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SUPER);
    }
}
