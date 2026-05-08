package dev.imagio.slot.forge.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.forge.workflow.ForgePlayerWorkflowRuntimeService;
import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;
import dev.imagio.slot.ui.action.WorkspaceActionSessionContext;
import dev.imagio.slot.ui.action.WorkspaceActionValidation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ForgeWorkspaceSessionRegistry {
    private static final Map<UUID, ForgeWorkspaceSession> SESSIONS = new ConcurrentHashMap<>();

    private ForgeWorkspaceSessionRegistry() {
    }

    public static WorkspaceActionValidation open(ServerPlayer player, WorkspaceActionEnvelope envelope) {
        if (player == null) {
            return WorkspaceActionValidation.rejected("missing_player");
        }
        if (envelope == null || envelope.sessionId().isBlank()) {
            return WorkspaceActionValidation.rejected("missing_session_id");
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return WorkspaceActionValidation.rejected("missing_menu");
        }
        if (envelope.menuContainerId() != menu.containerId) {
            return WorkspaceActionValidation.rejected(
                    "wrong_menu:expected=" + menu.containerId + ":actual=" + envelope.menuContainerId());
        }
        ForgeWorkspaceSession session = new ForgeWorkspaceSession(
                envelope,
                menu.containerId,
                ForgePlayerWorkflowRuntimeService.runtime(player));
        SESSIONS.put(player.getUUID(), session);
        SlotCommon.LOGGER.info(
                "Opened Forge workspace session: player={} session={} menu={} revision={}",
                player.getGameProfile().getName(),
                session.context().sessionId(),
                session.context().menuContainerId(),
                session.context().latestViewRevision());
        return WorkspaceActionValidation.ok();
    }

    public static WorkspaceActionSessionContext current(ServerPlayer player) {
        ForgeWorkspaceSession session = session(player);
        return session == null ? null : session.context();
    }

    public static ForgeWorkspaceSession session(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        ForgeWorkspaceSession session = SESSIONS.get(player.getUUID());
        if (session == null) {
            return null;
        }
        WorkspaceActionSessionContext context = session.context();
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null || menu.containerId != context.menuContainerId()) {
            SESSIONS.remove(player.getUUID(), session);
            SlotCommon.LOGGER.info(
                    "Closed stale Forge workspace session after menu drift: player={} session={} expectedMenu={} actualMenu={}",
                    player.getGameProfile().getName(),
                    context.sessionId(),
                    context.menuContainerId(),
                    menu == null ? "null" : menu.containerId);
            return null;
        }
        return session;
    }

    public static void close(ServerPlayer player) {
        if (player != null) {
            SESSIONS.remove(player.getUUID());
        }
    }

    public static void clear() {
        SESSIONS.clear();
    }
}
