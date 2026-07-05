package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player registry of active {@link SlotSidebarUiHandle}s.
 *
 * <p>Ticking is <em>not</em> driven from here: each handle attaches its
 * {@code ModularUI} to the player's host {@code AbstractContainerMenu},
 * and vanilla's per-tick {@code broadcastChanges()} on the active menu
 * routes through LDLib2's existing {@code AbstractContainerMenuMixin}
 * to call {@code modularUI.tickServer()}. The registry only exists for
 * lifecycle bookkeeping: dispose-on-replacement (player switching from
 * one host menu to another mid-session) and dispose-on-logout safety
 * net.
 */
public final class SlotSidebarUiHandles {
    private static final Map<UUID, SlotSidebarUiHandle> HANDLES = new ConcurrentHashMap<>();
    private static final Map<UUID, SlotWorkspaceViewModel> LAST_VIEW_MODELS = new ConcurrentHashMap<>();
    private static boolean registered;

    private SlotSidebarUiHandles() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(SlotSidebarUiHandles::onPlayerLoggedOut);
        registered = true;
    }

    public static void open(ServerPlayer player) {
        if (player == null) {
            return;
        }
        UUID id = player.getUUID();
        SlotSidebarUiHandle existing = HANDLES.remove(id);
        if (existing != null) {
            rememberLastViewModel(id, existing);
            existing.dispose();
        }
        SlotSidebarUiHandle handle = new SlotSidebarUiHandle(player);
        HANDLES.put(id, handle);
        SlotDebugLog.log("[SLOT][sidebar] opened handle for {}", player.getGameProfile().getName());
    }

    public static void close(ServerPlayer player) {
        if (player == null) {
            return;
        }
        UUID id = player.getUUID();
        SlotSidebarUiHandle handle = HANDLES.remove(id);
        if (handle != null) {
            rememberLastViewModel(id, handle);
            handle.dispose();
            SlotDebugLog.log("[SLOT][sidebar] closed handle for {}", player.getGameProfile().getName());
        }
    }

    public static SlotWorkspaceViewModel currentViewModel(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        SlotSidebarUiHandle handle = HANDLES.get(player.getUUID());
        if (handle != null && !handle.isDisposed()) {
            return handle.currentViewModel();
        }
        return LAST_VIEW_MODELS.get(player.getUUID());
    }

    public static boolean applyExternalOutcome(ServerPlayer player, WorkspaceCommandOutcome outcome) {
        if (player == null) {
            return false;
        }
        SlotSidebarUiHandle handle = HANDLES.get(player.getUUID());
        if (handle == null || handle.isDisposed()) {
            return false;
        }
        handle.applyExternalOutcome(outcome);
        return true;
    }

    private static void rememberLastViewModel(UUID playerId, SlotSidebarUiHandle handle) {
        if (playerId == null || handle == null || handle.currentViewModel() == null) {
            return;
        }
        LAST_VIEW_MODELS.put(playerId, handle.currentViewModel());
    }

    private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            close(player);
            LAST_VIEW_MODELS.remove(player.getUUID());
        }
    }
}
