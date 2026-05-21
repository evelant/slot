package dev.imagio.slot.forge.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.forge.workflow.ForgePlayerWorkflowRuntimeService;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;
import dev.imagio.slot.ui.action.WorkspaceActionSessionContext;
import dev.imagio.slot.ui.action.WorkspaceActionValidation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Map.Entry;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ForgeWorkspaceSessionRegistry {
    private static final Map<UUID, ForgeWorkspaceSession> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<UUID, SlotWorkspaceViewModel> LAST_VIEW_MODELS = new ConcurrentHashMap<>();

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
        ForgeWorkspaceSession previous = SESSIONS.remove(player.getUUID());
        if (previous != null) {
            rememberLastViewModel(player.getUUID(), previous);
            previous.detachMenuListener();
        }
        ForgeWorkspaceSession session = new ForgeWorkspaceSession(
                envelope,
                menu.containerId,
                ForgePlayerWorkflowRuntimeService.runtime(player));
        session.attachMenuListener(player);
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
            rememberLastViewModel(player.getUUID(), session);
            session.detachMenuListener();
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

    public static SlotWorkspaceViewModel currentViewModel(ServerPlayer player) {
        ForgeWorkspaceSession session = session(player);
        if (session != null) {
            return session.currentViewModel();
        }
        return player == null ? null : LAST_VIEW_MODELS.get(player.getUUID());
    }

    public static void close(ServerPlayer player) {
        if (player != null) {
            ForgeWorkspaceSession session = SESSIONS.remove(player.getUUID());
            if (session != null) {
                rememberLastViewModel(player.getUUID(), session);
                session.detachMenuListener();
            }
        }
    }

    public static void closeIfMenu(ServerPlayer player, AbstractContainerMenu menu) {
        if (player == null || menu == null) {
            return;
        }
        ForgeWorkspaceSession session = SESSIONS.get(player.getUUID());
        if (session != null && (session.observes(menu) || session.context().menuContainerId() == menu.containerId)) {
            close(player);
        }
    }

    public static void flushDirty(MinecraftServer server) {
        if (server == null || SESSIONS.isEmpty()) {
            return;
        }
        for (Entry<UUID, ForgeWorkspaceSession> entry : SESSIONS.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                ForgeWorkspaceSession removed = SESSIONS.remove(entry.getKey());
                if (removed != null) {
                    removed.detachMenuListener();
                }
                LAST_VIEW_MODELS.remove(entry.getKey());
                continue;
            }
            ForgeWorkspaceSession session = session(player);
            if (session == null || !session.shouldRefresh(player)) {
                continue;
            }
            SlotForgeNetworking.sendViewToPlayer(player, session, false);
            session.clearDirty();
        }
    }

    public static void clear() {
        for (Entry<UUID, ForgeWorkspaceSession> entry : SESSIONS.entrySet()) {
            rememberLastViewModel(entry.getKey(), entry.getValue());
            entry.getValue().detachMenuListener();
        }
        SESSIONS.clear();
        LAST_VIEW_MODELS.clear();
    }

    public static void forgetLastViewModel(ServerPlayer player) {
        if (player != null) {
            LAST_VIEW_MODELS.remove(player.getUUID());
        }
    }

    private static void rememberLastViewModel(UUID playerId, ForgeWorkspaceSession session) {
        if (playerId == null || session == null || session.currentViewModel() == null) {
            return;
        }
        LAST_VIEW_MODELS.put(playerId, session.currentViewModel());
    }
}
