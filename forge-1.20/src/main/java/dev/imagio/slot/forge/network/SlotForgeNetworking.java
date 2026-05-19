package dev.imagio.slot.forge.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.forge.storage.ForgeCarriedActivityTracker;
import dev.imagio.slot.forge.workflow.ForgePlayerWorkflowRuntimeService;
import dev.imagio.slot.inventory.goal.GoalPlanState;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.workspace.KitGatherService;
import dev.imagio.slot.inventory.workspace.KitPageCycleService;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceChestCommandService;
import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;
import dev.imagio.slot.ui.action.WorkspaceActionPacket;
import dev.imagio.slot.ui.action.WorkspaceActionSessionContext;
import dev.imagio.slot.ui.action.WorkspaceActionSessionValidator;
import dev.imagio.slot.ui.action.WorkspaceActionValidation;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Map;
import java.util.function.Supplier;

public final class SlotForgeNetworking {
    private static final String PROTOCOL_VERSION = "3";
    private static SimpleChannel channel;

    private SlotForgeNetworking() {
    }

    public static void register() {
        if (channel != null) {
            return;
        }
        channel = NetworkRegistry.newSimpleChannel(
                SlotCommon.id("workspace_actions"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals);
        channel.messageBuilder(ForgeWorkspaceActionMessage.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ForgeWorkspaceActionMessage::encode)
                .decoder(ForgeWorkspaceActionMessage::decode)
                .consumerMainThread(SlotForgeNetworking::handleWorkspaceAction)
                .add();
        channel.messageBuilder(ForgeWorkspaceOpenMessage.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ForgeWorkspaceOpenMessage::encode)
                .decoder(ForgeWorkspaceOpenMessage::decode)
                .consumerMainThread(SlotForgeNetworking::handleWorkspaceOpen)
                .add();
        channel.messageBuilder(ForgeWorkspaceViewModelMessage.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ForgeWorkspaceViewModelMessage::encode)
                .decoder(ForgeWorkspaceViewModelMessage::decode)
                .consumerMainThread(SlotForgeNetworking::handleWorkspaceView)
                .add();
        channel.messageBuilder(ForgeWorkspaceCloseMessage.class, 3, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ForgeWorkspaceCloseMessage::encode)
                .decoder(ForgeWorkspaceCloseMessage::decode)
                .consumerMainThread(SlotForgeNetworking::handleWorkspaceClose)
                .add();
        channel.messageBuilder(ForgeGatherActiveKitMessage.class, 4, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ForgeGatherActiveKitMessage::encode)
                .decoder(ForgeGatherActiveKitMessage::decode)
                .consumerMainThread(SlotForgeNetworking::handleGatherActiveKit)
                .add();
        channel.messageBuilder(ForgeKitPageCycleMessage.class, 5, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ForgeKitPageCycleMessage::encode)
                .decoder(ForgeKitPageCycleMessage::decode)
                .consumerMainThread(SlotForgeNetworking::handleKitPageCycle)
                .add();
        channel.messageBuilder(ForgeGoalRecipeDefaultMessage.class, 6, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ForgeGoalRecipeDefaultMessage::encode)
                .decoder(ForgeGoalRecipeDefaultMessage::decode)
                .consumerMainThread(SlotForgeNetworking::handleGoalRecipeDefault)
                .add();
        channel.messageBuilder(ForgeGoalPlanMessage.class, 7, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ForgeGoalPlanMessage::encode)
                .decoder(ForgeGoalPlanMessage::decode)
                .consumerMainThread(SlotForgeNetworking::handleGoalPlan)
                .add();
        channel.messageBuilder(ForgeSetWantedCountMessage.class, 8, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ForgeSetWantedCountMessage::encode)
                .decoder(ForgeSetWantedCountMessage::decode)
                .consumerMainThread(SlotForgeNetworking::handleSetWantedCount)
                .add();
        channel.messageBuilder(ForgeDepositPutAwayMessage.class, 9, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ForgeDepositPutAwayMessage::encode)
                .decoder(ForgeDepositPutAwayMessage::decode)
                .consumerMainThread(SlotForgeNetworking::handleDepositPutAway)
                .add();
    }

    public static boolean sendToServer(ForgeWorkspaceActionMessage message) {
        if (channel == null) {
            SlotCommon.LOGGER.warn("Cannot send Forge workspace action before network channel registration");
            return false;
        }
        try {
            channel.sendToServer(message);
            return true;
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn("Failed to send Forge workspace action packet", exception);
            return false;
        }
    }

    public static boolean openWorkspaceSession(ForgeWorkspaceOpenMessage message) {
        if (channel == null) {
            SlotCommon.LOGGER.warn("Cannot open Forge workspace session before network channel registration");
            return false;
        }
        try {
            channel.sendToServer(message);
            return true;
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn("Failed to send Forge workspace open packet", exception);
            return false;
        }
    }

    public static boolean closeWorkspaceSession(ForgeWorkspaceCloseMessage message) {
        if (channel == null) {
            SlotCommon.LOGGER.warn("Cannot close Forge workspace session before network channel registration");
            return false;
        }
        try {
            channel.sendToServer(message);
            return true;
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn("Failed to send Forge workspace close packet", exception);
            return false;
        }
    }

    public static boolean gatherActiveKit() {
        if (channel == null) {
            SlotCommon.LOGGER.warn("Cannot gather desired items before network channel registration");
            return false;
        }
        try {
            channel.sendToServer(new ForgeGatherActiveKitMessage());
            return true;
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn("Failed to send Forge gather desired items packet", exception);
            return false;
        }
    }

    public static boolean depositPutAway() {
        if (channel == null) {
            SlotCommon.LOGGER.warn("Cannot put away carried clutter before network channel registration");
            return false;
        }
        try {
            channel.sendToServer(new ForgeDepositPutAwayMessage());
            return true;
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn("Failed to send Forge put away packet", exception);
            return false;
        }
    }

    public static boolean cycleKitPage(int direction) {
        if (channel == null) {
            SlotCommon.LOGGER.warn("Cannot cycle kit page before network channel registration");
            return false;
        }
        try {
            channel.sendToServer(new ForgeKitPageCycleMessage(direction));
            return true;
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn("Failed to send Forge kit page cycle packet", exception);
            return false;
        }
    }

    public static boolean rememberGoalRecipeDefault(String outputItemId, String recipeId) {
        if (channel == null) {
            SlotCommon.LOGGER.warn("Cannot remember Forge goal recipe default before network channel registration");
            return false;
        }
        try {
            channel.sendToServer(new ForgeGoalRecipeDefaultMessage(outputItemId, recipeId));
            return true;
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn("Failed to send Forge goal recipe default packet", exception);
            return false;
        }
    }

    public static boolean saveGoalPlan(GoalPlanState goal) {
        if (channel == null) {
            SlotCommon.LOGGER.warn("Cannot save Forge goal plan before network channel registration");
            return false;
        }
        try {
            channel.sendToServer(ForgeGoalPlanMessage.save(goal));
            return true;
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn("Failed to send Forge goal plan save packet", exception);
            return false;
        }
    }

    public static boolean removeGoalPlan(String goalId) {
        if (channel == null) {
            SlotCommon.LOGGER.warn("Cannot remove Forge goal plan before network channel registration");
            return false;
        }
        try {
            channel.sendToServer(ForgeGoalPlanMessage.remove(goalId));
            return true;
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn("Failed to send Forge goal plan remove packet", exception);
            return false;
        }
    }

    public static boolean setWantedCount(
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            int targetCount
    ) {
        if (channel == null) {
            SlotCommon.LOGGER.warn("Cannot set Forge wanted count before network channel registration");
            return false;
        }
        try {
            channel.sendToServer(new ForgeSetWantedCountMessage(
                    itemId,
                    comparisonMode,
                    componentFingerprint,
                    targetCount));
            return true;
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn("Failed to send Forge wanted count packet", exception);
            return false;
        }
    }

    private static void handleWorkspaceOpen(
            ForgeWorkspaceOpenMessage message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPlayer player = contextSupplier.get().getSender();
        WorkspaceActionValidation opened = ForgeWorkspaceSessionRegistry.open(player, message.envelope());
        if (!opened.valid()) {
            SlotCommon.LOGGER.info(
                    "Rejected Forge workspace session open: player={} session={} menu={} revision={} diagnostics={}",
                    playerName(player),
                    message.envelope().sessionId(),
                    message.envelope().menuContainerId(),
                    message.envelope().viewRevision(),
                    opened.diagnostics());
            return;
        }
        ForgeWorkspaceSession session = ForgeWorkspaceSessionRegistry.session(player);
        sendViewToPlayer(player, session, true);
    }

    private static void handleWorkspaceView(
            ForgeWorkspaceViewModelMessage message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ForgeWorkspaceViewModelClientCache.update(message.envelope(), message.viewModel());
    }

    private static void handleWorkspaceClose(
            ForgeWorkspaceCloseMessage message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPlayer player = contextSupplier.get().getSender();
        WorkspaceActionSessionContext current = ForgeWorkspaceSessionRegistry.current(player);
        WorkspaceActionValidation session = WorkspaceActionSessionValidator.validate(message.envelope(), current);
        if (session.valid()) {
            ForgeWorkspaceSessionRegistry.close(player);
        }
    }

    private static void handleWorkspaceAction(
            ForgeWorkspaceActionMessage message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        WorkspaceActionPacket packet = message.packet();
        WorkspaceActionValidation shape = packet.validateShape();
        if (!shape.valid()) {
            SlotCommon.LOGGER.warn(
                    "Rejected malformed Forge workspace action: player={} action={} diagnostics={}",
                    playerName(player),
                    packet.action(),
                    shape.diagnostics());
            return;
        }

        WorkspaceActionSessionContext current = ForgeWorkspaceSessionRegistry.current(player);
        WorkspaceActionValidation session = WorkspaceActionSessionValidator.validate(packet.envelope(), current);
        if (!session.valid()) {
            SlotCommon.LOGGER.info(
                    "Rejected Forge workspace action: player={} action={} session={} menu={} revision={} diagnostics={}",
                    playerName(player),
                    packet.action(),
                    packet.envelope().sessionId(),
                    packet.envelope().menuContainerId(),
                    packet.envelope().viewRevision(),
                    session.diagnostics());
            return;
        }

        ForgeWorkspaceSession workspaceSession = ForgeWorkspaceSessionRegistry.session(player);
        if (workspaceSession == null) {
            SlotCommon.LOGGER.info(
                    "Rejected Forge workspace action after validation without session object: player={} action={} session={} menu={} diagnostics=session_unavailable",
                    playerName(player),
                    packet.action(),
                    packet.envelope().sessionId(),
                    packet.envelope().menuContainerId());
            return;
        }

        WorkspaceCommandOutcome outcome = workspaceSession.handleAction(player, packet);
        sendViewToPlayer(player, workspaceSession, false);
        if (logWorkspaceActionAtInfo(outcome)) {
            SlotCommon.LOGGER.info(
                    "{} Forge workspace action: player={} action={} session={} menu={} status={} diagnostics={}",
                    outcome.success() ? "Accepted" : "Rejected",
                    playerName(player),
                    packet.action(),
                    packet.envelope().sessionId(),
                    packet.envelope().menuContainerId(),
                    outcome.status(),
                    outcome.diagnostics());
        } else {
            SlotCommon.LOGGER.debug(
                    "{} Forge workspace action: player={} action={} session={} menu={} status={} diagnostics={}",
                    outcome.success() ? "Accepted" : "Rejected",
                    playerName(player),
                    packet.action(),
                    packet.envelope().sessionId(),
                    packet.envelope().menuContainerId(),
                    outcome.status(),
                    outcome.diagnostics());
        }
    }

    private static boolean logWorkspaceActionAtInfo(WorkspaceCommandOutcome outcome) {
        if (outcome == null || !outcome.success()) {
            return true;
        }
        return !("nothing_to_take".equals(outcome.status()) || "nothing_to_deposit".equals(outcome.status()));
    }

    private static void handleGatherActiveKit(
            ForgeGatherActiveKitMessage message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPlayer player = contextSupplier.get().getSender();
        if (player == null) {
            return;
        }
        KitGatherService.Outcome outcome = ForgeWorkspaceSession.gatherActiveKitAndReapply(
                player,
                ForgePlayerWorkflowRuntimeService.runtime(player));
        ForgeWorkspaceSession session = ForgeWorkspaceSessionRegistry.session(player);
        if (session != null) {
            sendViewToPlayer(player, session, true);
        }
        SlotCommon.LOGGER.info(
                "[SLOT] gather hotkey (Forge): player={} reason={} pulled={} unreachable={}",
                playerName(player),
                outcome.reason(),
                outcome.totalItemsPulled(),
                outcome.identitiesUnreachable());
    }

    private static void handleDepositPutAway(
            ForgeDepositPutAwayMessage message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPlayer player = contextSupplier.get().getSender();
        if (player == null) {
            return;
        }
        InventoryHostDescriptor host = resolveCarriedHost(player);
        if (host == null) {
            SlotCommon.LOGGER.info(
                    "[SLOT] rejected Forge put-away hotkey: player={} diagnostics=host_resolution_failed",
                    playerName(player));
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        WorkspaceCommandOutcome outcome = WorkspaceChestCommandService.deposit(
                player,
                ForgePlayerWorkflowRuntimeService.runtime(player),
                authority);
        ForgeWorkspaceSession session = ForgeWorkspaceSessionRegistry.session(player);
        if (session != null) {
            sendViewToPlayer(player, session, true);
        }
        SlotCommon.LOGGER.info(
                "[SLOT] Forge put-away hotkey: player={} status={} diagnostics={}",
                playerName(player),
                outcome.status(),
                outcome.diagnostics());
    }

    private static void handleKitPageCycle(
            ForgeKitPageCycleMessage message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPlayer player = contextSupplier.get().getSender();
        if (player == null) {
            return;
        }
        int direction = Integer.signum(message.direction());
        if (direction == 0) {
            direction = 1;
        }
        WorkspaceCommandOutcome outcome = KitPageCycleService.switchActivePage(
                player,
                ForgePlayerWorkflowRuntimeService.runtime(player),
                direction,
                "forge_in_world",
                actionOutcome -> {
                    if (actionOutcome != null && actionOutcome.successful()) {
                        ForgeCarriedActivityTracker.suppressNext(player);
                    }
                });
        ForgeWorkspaceSession session = ForgeWorkspaceSessionRegistry.session(player);
        if (session != null) {
            sendViewToPlayer(player, session, true);
        }
        SlotCommon.LOGGER.info(
                "[SLOT] kit page cycle hotkey (Forge): player={} direction={} status={} diagnostics={}",
                playerName(player),
                direction,
                outcome.status(),
                outcome.diagnostics());
    }

    private static void handleGoalRecipeDefault(
            ForgeGoalRecipeDefaultMessage message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPlayer player = contextSupplier.get().getSender();
        if (player == null || message == null) {
            return;
        }
        boolean changed = ForgePlayerWorkflowRuntimeService.runtime(player)
                .goalRecipeDefaultWorkflow()
                .set(message.outputItemId(), message.recipeId());
        if (changed) {
            ForgeWorkspaceSession session = ForgeWorkspaceSessionRegistry.session(player);
            if (session != null) {
                sendViewToPlayer(player, session, true);
            }
        }
        SlotCommon.LOGGER.info(
                "[SLOT] remembered Forge goal recipe default: player={} output={} recipe={} changed={}",
                playerName(player),
                message.outputItemId(),
                message.recipeId(),
                changed);
    }

    private static void handleGoalPlan(
            ForgeGoalPlanMessage message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPlayer player = contextSupplier.get().getSender();
        if (player == null || message == null) {
            return;
        }
        boolean changed;
        if (ForgeGoalPlanMessage.ACTION_REMOVE.equals(message.action())) {
            changed = ForgePlayerWorkflowRuntimeService.runtime(player)
                    .goalPlanWorkflow()
                    .remove(message.goalId());
        } else {
            changed = ForgePlayerWorkflowRuntimeService.runtime(player)
                    .goalPlanWorkflow()
                    .save(message.goal());
        }
        if (changed) {
            ForgeWorkspaceSession session = ForgeWorkspaceSessionRegistry.session(player);
            if (session != null) {
                sendViewToPlayer(player, session, true);
            }
        }
        SlotCommon.LOGGER.info(
                "[SLOT] persisted Forge goal plan: player={} action={} goal={} changed={}",
                playerName(player),
                message.action(),
                message.goalId(),
                changed);
    }

    private static void handleSetWantedCount(
            ForgeSetWantedCountMessage message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPlayer player = contextSupplier.get().getSender();
        if (player == null || message == null) {
            return;
        }
        InventoryHostDescriptor host = resolveCarriedHost(player);
        if (host == null) {
            SlotCommon.LOGGER.info(
                    "[SLOT] rejected Forge wanted hover hotkey: player={} item={} diagnostics=host_resolution_failed",
                    playerName(player),
                    message.itemId());
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.setWantedCount(
                ForgePlayerWorkflowRuntimeService.runtime(player),
                authority,
                message.itemId(),
                message.comparisonMode(),
                message.componentFingerprint(),
                message.targetCount());
        ForgeWorkspaceSession session = ForgeWorkspaceSessionRegistry.session(player);
        if (session != null) {
            sendViewToPlayer(player, session, true);
        }
        SlotCommon.LOGGER.info(
                "[SLOT] Forge wanted hover hotkey: player={} item={} target={} status={} diagnostics={}",
                playerName(player),
                message.itemId(),
                message.targetCount(),
                outcome.status(),
                outcome.diagnostics());
    }

    static void sendViewToPlayer(ServerPlayer player, ForgeWorkspaceSession session, boolean logViewSend) {
        if (player == null || channel == null || session == null) {
            return;
        }
        try {
            long previousRevision = session.context().latestViewRevision();
            SlotWorkspaceViewModel viewModel = session.project(player, logViewSend);
            session.clearDirty();
            if (!logViewSend && viewModel.revision() == previousRevision) {
                return;
            }
            WorkspaceActionEnvelope viewEnvelope = new WorkspaceActionEnvelope(
                    session.context().sessionId(),
                    session.context().menuContainerId(),
                    viewModel.revision());
            ForgeWorkspaceViewModelMessage message = new ForgeWorkspaceViewModelMessage(
                    viewEnvelope,
                    viewModel);
            channel.send(PacketDistributor.PLAYER.with(() -> player), message);
            if (logViewSend) {
                SlotCommon.LOGGER.info(
                        "Sent Forge workspace view: player={} session={} menu={} revision={} items={} sections={}",
                        playerName(player),
                        viewEnvelope.sessionId(),
                        viewEnvelope.menuContainerId(),
                        viewEnvelope.viewRevision(),
                        viewModel.atlasItems().size(),
                        viewModel.islands().size());
            }
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn("Failed to send Forge workspace view model", exception);
        }
    }

    private static String playerName(ServerPlayer player) {
        return player == null ? "<unknown>" : player.getGameProfile().getName();
    }

    private static InventoryHostDescriptor resolveCarriedHost(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return null;
        }
        return InventoryHostResolver.resolve(new InventoryHostContext(
                menu,
                player.getInventory(),
                Component.literal("SLOT Carried Hotkey"),
                SlotForgeNetworking.class.getName(),
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.CARRIED_ONLY,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        true,
                        true,
                        Map.of("slotCarriedHotkey", "forge")
                )
        ));
    }
}
