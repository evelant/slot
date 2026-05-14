package dev.imagio.slot.forge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.forge.SlotForge;
import dev.imagio.slot.forge.workflow.ForgePlayerWorkflowRuntimeService;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.ChestContentAffinitySeeder;
import dev.imagio.slot.inventory.workspace.ChestDepositObservationSupport;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestClaimWorkflowDomainService;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Forge twin of the NeoForge storage deposit observer. It watches claimable
 * storage menu close deltas and records {@code ChestDepositObserved} events so
 * affinity routing learns from manual organization.
 */
@Mod.EventBusSubscriber(modid = SlotForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeChestDepositObserver {
    private static final long PENDING_TICK_BUDGET = 5L;

    private static final Map<UUID, PendingInteraction> PENDING = new HashMap<>();
    private static final WeakHashMap<AbstractContainerMenu, OpenSession> SESSIONS = new WeakHashMap<>();

    private ForgeChestDepositObserver() {
    }

    public static void expectOpen(ServerPlayer player, BlockPos pos) {
        if (player == null || pos == null) {
            return;
        }
        long tick = player.serverLevel().getGameTime();
        PENDING.put(player.getUUID(), new PendingInteraction(pos.immutable(), tick));
    }

    public static BlockPos activeChestPos(ServerPlayer player) {
        if (player == null || player.containerMenu == null) {
            return null;
        }
        OpenSession session = SESSIONS.get(player.containerMenu);
        return session == null ? null : session.pos;
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player playerEntity = event.getEntity();
        if (!(playerEntity instanceof ServerPlayer player)) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = event.getPos();
        if (!ForgeChestStorageAnchors.isClaimable(level, pos)) {
            return;
        }
        PENDING.put(player.getUUID(), new PendingInteraction(pos.immutable(), level.getGameTime()));
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        AbstractContainerMenu menu = event.getContainer();
        PendingInteraction pending = PENDING.remove(player.getUUID());
        if (pending == null) {
            return;
        }
        long now = player.serverLevel().getGameTime();
        if (now - pending.tick > PENDING_TICK_BUDGET) {
            return;
        }
        List<Integer> storageSlots = ChestDepositObservationSupport.storageMenuSlots(menu, player.getInventory());
        if (storageSlots.isEmpty()) {
            return;
        }
        ItemStack[] snapshot = ChestDepositObservationSupport.snapshot(menu, storageSlots);
        SESSIONS.put(menu, new OpenSession(pending.pos, snapshot, storageSlots));
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        AbstractContainerMenu menu = event.getContainer();
        OpenSession session = SESSIONS.remove(menu);
        if (session == null) {
            return;
        }

        ServerLevel level = player.serverLevel();
        ChestAnchor anchor = ForgeChestStorageAnchors.toAnchor(level, session.pos);
        WorkflowDomainRuntime runtime = ForgePlayerWorkflowRuntimeService.runtime(player);
        ChestClaimWorkflowDomainService chestService = runtime.chestClaimWorkflow();

        ChestDepositObservationSupport.Observation observation =
                ChestDepositObservationSupport.observe(
                        session.snapshot,
                        menu,
                        session.storageSlots);
        Map<ItemIdentity, Integer> deposits = observation.deposits();
        Map<ItemIdentity, Integer> takes = observation.takes();

        if (!deposits.isEmpty()) {
            UUID storageId = resolveOrCreateClaim(chestService, level, session.pos, anchor);
            if (storageId == null) {
                return;
            }
            long tick = level.getGameTime();
            int seeded = ChestContentAffinitySeeder.seedInitialContents(
                    chestService,
                    storageId,
                    session.snapshot,
                    tick);
            for (Map.Entry<ItemIdentity, Integer> entry : deposits.entrySet()) {
                chestService.recordDeposit(storageId, entry.getKey(), entry.getValue(), tick);
            }
            SlotCommon.LOGGER.info(
                    "[SLOT] forge auto-claim observed deposits player={} pos={} storage={} identities={} seeded={}",
                    player.getScoreboardName(), session.pos, storageId, deposits.size(), seeded);
            return;
        }

        if (!takes.isEmpty() && chestService.chestByAnchor(anchor) != null) {
            for (ItemIdentity identity : takes.keySet()) {
                runtime.dismissRecent(identity);
            }
        }
    }

    public static UUID resolveOrCreateClaim(
            ChestClaimWorkflowDomainService chestService,
            ServerLevel level,
            BlockPos pos,
            ChestAnchor anchor
    ) {
        if (chestService == null || level == null || pos == null || anchor == null) {
            return null;
        }
        ClaimedChest existing = chestService.chestByAnchor(anchor);
        if (existing != null) {
            return existing.storageId();
        }
        Set<ChestAnchor> anchors = ForgeChestStorageAnchors.resolveAnchors(level, pos);
        if (anchors.isEmpty()) {
            return null;
        }
        for (ChestAnchor candidate : anchors) {
            ClaimedChest covering = chestService.chestByAnchor(candidate);
            if (covering == null) {
                continue;
            }
            LinkedHashMap<ChestAnchor, Boolean> merged = new LinkedHashMap<>();
            covering.anchors().forEach(a -> merged.put(a, Boolean.TRUE));
            anchors.forEach(a -> merged.put(a, Boolean.TRUE));
            chestService.updateAnchors(covering.storageId(), merged.keySet());
            stampStorageId(level, pos, covering.storageId());
            return covering.storageId();
        }
        int atlasX = pos.getX() * 100;
        int atlasY = pos.getZ() * 100;
        ClaimedChest created = chestService.claim(anchors, atlasX, atlasY, "");
        if (created == null) {
            SlotCommon.LOGGER.warn("[SLOT] forge auto-claim: claim returned null for pos={}", pos);
            return null;
        }
        stampStorageId(level, pos, created.storageId());
        return created.storageId();
    }

    private static void stampStorageId(ServerLevel level, BlockPos pos, UUID storageId) {
        ForgeChestStorageIds.write(level, pos, storageId);
        BlockPos paired = ForgeChestStorageAnchors.pairedChestNeighbor(level, pos);
        if (paired != null) {
            ForgeChestStorageIds.write(level, paired, storageId);
        }
    }

    private record PendingInteraction(BlockPos pos, long tick) {
    }

    private record OpenSession(BlockPos pos, ItemStack[] snapshot, List<Integer> storageSlots) {
    }
}
