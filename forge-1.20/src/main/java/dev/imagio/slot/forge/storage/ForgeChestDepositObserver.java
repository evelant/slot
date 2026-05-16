package dev.imagio.slot.forge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.forge.SlotForge;
import dev.imagio.slot.forge.workflow.ForgePlayerWorkflowRuntimeService;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.ChestContentAffinitySeeder;
import dev.imagio.slot.inventory.workspace.ChestDepositObservationSupport;
import dev.imagio.slot.inventory.workspace.StorageTargetRef;
import dev.imagio.slot.inventory.workspace.WorkspaceStorageMemoryStore;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
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
        long now = player.serverLevel().getGameTime();
        BlockPos pos = pending != null && now - pending.tick <= PENDING_TICK_BUDGET
                ? pending.pos
                : claimableMenuPos(player, menu);
        if (pos == null) {
            return;
        }
        if (!ForgeChestStorageAnchors.isClaimable(player.serverLevel(), pos)) {
            return;
        }
        List<Integer> storageSlots = ChestDepositObservationSupport.storageMenuSlots(menu, player.getInventory());
        if (storageSlots.isEmpty()) {
            return;
        }
        ItemStack[] snapshot = ChestDepositObservationSupport.snapshot(menu, storageSlots);
        SESSIONS.put(menu, new OpenSession(pos, snapshot, storageSlots));
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
        UUID stampedStorageId = ForgeChestStorageIds.read(level, session.pos).orElse(null);
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
            observeRememberedContents(
                    level, chestService, storageId, anchor, session.pos, menu, session.storageSlots, "container_close_deposit");
            SlotCommon.LOGGER.info(
                    "[SLOT] forge auto-claim observed deposits player={} pos={} storage={} identities={} seeded={}",
                    player.getScoreboardName(), session.pos, storageId, deposits.size(), seeded);
            return;
        }

        ClaimedChest trackedChest = chestService.chestByAnchor(anchor);
        UUID memoryStorageId = stampedStorageId != null
                ? stampedStorageId
                : trackedChest == null ? null : trackedChest.storageId();
        observeRememberedContents(
                level, chestService, memoryStorageId, anchor, session.pos, menu, session.storageSlots, "container_close");

        if (!takes.isEmpty() && trackedChest != null) {
            for (ItemIdentity identity : takes.keySet()) {
                runtime.dismissRecent(identity);
            }
        }
    }

    private static void observeRememberedContents(
            ServerLevel level,
            ChestClaimWorkflowDomainService chestService,
            UUID storageId,
            ChestAnchor anchor,
            BlockPos pos,
            AbstractContainerMenu menu,
            List<Integer> storageSlots,
            String source
    ) {
        if (level == null || storageId == null || menu == null || storageSlots == null || storageSlots.isEmpty()) {
            return;
        }
        WorkspaceStorageMemoryStore store = WorkspaceStorageMemoryStore.forServer(level.getServer());
        if (store == null) {
            return;
        }
        ClaimedChest claimed = chestService == null ? null : chestService.chest(storageId);
        StorageTargetRef ref = claimed == null
                ? StorageTargetRef.claimed(
                        storageId,
                        anchor == null ? level.dimension().location().toString() : anchor.dimensionId(),
                        anchor == null ? pos.getX() : anchor.x(),
                        anchor == null ? pos.getY() : anchor.y(),
                        anchor == null ? pos.getZ() : anchor.z(),
                        "",
                        true,
                        false,
                        true)
                : StorageTargetRef.claimed(claimed, true, false, true);
        store.observe(
                ref,
                storageSlots.size(),
                ChestDepositObservationSupport.currentContents(menu, storageSlots),
                level.getGameTime(),
                source);
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
        Optional<UUID> stampedStorageId = ForgeChestStorageIds.read(level, pos);
        if (stampedStorageId.isPresent()) {
            UUID storageId = stampedStorageId.get();
            ClaimedChest byId = chestService.chest(storageId);
            if (byId != null) {
                return byId.storageId();
            }
            ClaimedChest claimed = chestService.claimWithId(storageId, anchors, pos.getX() * 100, pos.getZ() * 100, "");
            return claimed == null ? storageId : claimed.storageId();
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

    private static BlockPos claimableMenuPos(ServerPlayer player, AbstractContainerMenu menu) {
        if (player == null || menu == null) {
            return null;
        }
        BlockEntity blockEntity = menuBlockEntity(menu);
        if (blockEntity == null) {
            return null;
        }
        BlockPos pos = blockEntity.getBlockPos();
        if (!ForgeChestStorageAnchors.isClaimable(player.serverLevel(), pos)) {
            return null;
        }
        return pos.immutable();
    }

    private static BlockEntity menuBlockEntity(AbstractContainerMenu menu) {
        Method method = findNoArgMethod(menu.getClass(), "getBlockEntity");
        if (method == null) {
            return null;
        }
        try {
            method.setAccessible(true);
            Object result = method.invoke(menu);
            return result instanceof BlockEntity blockEntity ? blockEntity : null;
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static Method findNoArgMethod(Class<?> type, String name) {
        Class<?> cursor = type;
        while (cursor != null && cursor != Object.class) {
            try {
                Method method = cursor.getDeclaredMethod(name);
                if (method.getParameterCount() == 0) {
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
            }
            cursor = cursor.getSuperclass();
        }
        return null;
    }

    private record PendingInteraction(BlockPos pos, long tick) {
    }

    private record OpenSession(BlockPos pos, ItemStack[] snapshot, List<Integer> storageSlots) {
    }
}
