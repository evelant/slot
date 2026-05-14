package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.ChestContentAffinitySeeder;
import dev.imagio.slot.inventory.workspace.ChestDepositObservationSupport;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestClaimWorkflowDomainService;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Observes claimable storage GUI sessions: when the player closes storage with
 * a net positive item delta, auto-claims it (if not already claimed) and bumps
 * {@code ChestAffinityMap} for each deposited identity.
 *
 * <p>Pairs PlayerInteractEvent.RightClickBlock (to capture the chest BlockPos
 * before the GUI opens) with PlayerContainerEvent.Open / Close (to snapshot
 * and diff the chest's contents).
 */
public final class ChestDepositObserver {
    /** Stale threshold (in ticks) for a pending right-click → menu-open hand-off. */
    private static final long PENDING_TICK_BUDGET = 5L;

    private static boolean registered;

    /** Per-player most recent right-clicked chest block. Cleared on consume / staleness. */
    private static final Map<UUID, PendingInteraction> PENDING = new HashMap<>();

    /** Live chest sessions, keyed by container instance (which is unique per open). */
    private static final WeakHashMap<AbstractContainerMenu, OpenSession> SESSIONS = new WeakHashMap<>();

    private ChestDepositObserver() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(ChestDepositObserver::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(ChestDepositObserver::onContainerOpen);
        NeoForge.EVENT_BUS.addListener(ChestDepositObserver::onContainerClose);
        registered = true;
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player playerEntity = event.getEntity();
        if (!(playerEntity instanceof ServerPlayer player)) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!ChestStorageAnchors.isClaimable(level, pos)) {
            return;
        }
        long tick = level instanceof ServerLevel serverLevel ? serverLevel.getGameTime() : 0L;
        PENDING.put(player.getUUID(), new PendingInteraction(pos, tick));
    }

    /**
     * Authorize the next chest GUI open for {@code player} as targeting
     * {@code pos}, even though no {@link PlayerInteractEvent.RightClickBlock}
     * fires (e.g. the SLOT workspace's "open vanilla here" loot-panel
     * button drives the open via {@code player.openMenu} on the server).
     * Without this hook the observer's open-session table would skip the
     * synthetic open and the chest's auto-claim on deposit-close would
     * never fire — the bug that left forgotten / right-click-intercepted
     * chests unclaimable in normal play.
     */
    public static void expectOpen(ServerPlayer player, BlockPos pos) {
        if (player == null || pos == null) {
            return;
        }
        long tick = player.serverLevel().getGameTime();
        PENDING.put(player.getUUID(), new PendingInteraction(pos.immutable(), tick));
    }

    /**
     * The block position of the storage backing the player's currently-open
     * container menu, or {@code null} when the player isn't viewing a
     * tracked storage session. Surfaced for the workspace's active-chest
     * panel — it needs the BlockPos to resolve the storage claim state
     * (or offer a "Claim" affordance when unclaimed) without re-walking
     * the world.
     */
    public static BlockPos activeChestPos(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        AbstractContainerMenu menu = player.containerMenu;
        OpenSession session = SESSIONS.get(menu);
        return session == null ? null : session.pos;
    }

    private static void onContainerOpen(PlayerContainerEvent.Open event) {
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

    private static void onContainerClose(PlayerContainerEvent.Close event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        AbstractContainerMenu menu = event.getContainer();
        OpenSession session = SESSIONS.remove(menu);
        if (session == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        ChestAnchor anchor = ChestStorageAnchors.toAnchor(level, session.pos);
        WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
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
                    "[SLOT] auto-claim observed deposits player={} pos={} storage={} identities={} seeded={}",
                    player.getScoreboardName(), session.pos, storageId, deposits.size(), seeded
            );
            return;
        }

        // Recents filter: takes from a CLAIMED (tracked) chest don't
        // belong in the player's "where did the thing I just grabbed
        // end up?" strip — the chest is part of their organised
        // storage, not a discovery. Dismiss those identities so the
        // ACQUIRED events from the authority diff don't surface them.
        // Loot/world pickups + crafting outputs aren't routed through
        // here, so they keep populating recents normally.
        if (!takes.isEmpty() && chestService.chestByAnchor(anchor) != null) {
            for (ItemIdentity identity : takes.keySet()) {
                runtime.dismissRecent(identity);
            }
        }

        // Loot chest summary: player took items from a chest with no
        // existing claim. Emit a one-line chat hint that names which taken
        // identities have visual homes — useful as the player walks back
        // to base. No overlay rendering yet; the chat line is the v0
        // surface for "Triage-style loot panel" per docs/plans/learned-storage.md.
        if (!takes.isEmpty() && chestService.chestByAnchor(anchor) == null) {
            sendLootChestSummary(player, runtime, takes);
        }
    }

    private static void sendLootChestSummary(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            Map<ItemIdentity, Integer> takes
    ) {
        VisualHomeMap homeMap = runtime.snapshot().visualHomeMap();
        StringBuilder homed = new StringBuilder();
        int homedCount = 0;
        int unhomedCount = 0;
        for (Map.Entry<ItemIdentity, Integer> entry : takes.entrySet()) {
            VisualHomeAssignment assignment = homeMap.assignment(entry.getKey());
            if (assignment == null) {
                unhomedCount++;
                continue;
            }
            if (homedCount < 3) {
                if (homed.length() > 0) {
                    homed.append(", ");
                }
                String shortId = entry.getKey().itemId();
                int colon = shortId.indexOf(':');
                if (colon >= 0) {
                    shortId = shortId.substring(colon + 1);
                }
                homed.append(shortId)
                        .append("×").append(entry.getValue())
                        .append(" → ")
                        .append(islandLabel(homeMap, assignment.islandId()));
            }
            homedCount++;
        }
        if (homedCount == 0 && unhomedCount == 0) {
            return;
        }
        StringBuilder summary = new StringBuilder("[SLOT] looted ");
        if (homedCount > 0) {
            summary.append(homed);
            if (homedCount > 3) {
                summary.append(" (+").append(homedCount - 3).append(" more homed)");
            }
        }
        if (unhomedCount > 0) {
            if (homedCount > 0) {
                summary.append("; ");
            }
            summary.append(unhomedCount).append(" unhomed (Triage)");
        }
        player.sendSystemMessage(Component.literal(summary.toString()).withStyle(ChatFormatting.GRAY));
    }

    private static String islandLabel(VisualHomeMap homeMap, String islandId) {
        if (islandId == null || islandId.isBlank()) {
            return "Triage";
        }
        for (VisualAtlasIsland island : homeMap.playerIslands()) {
            if (islandId.equals(island.id())) {
                return island.label() == null || island.label().isBlank() ? islandId : island.label();
            }
        }
        return islandId;
    }

    /**
     * Resolve the existing claim for {@code pos} or create a fresh one
     * (folding in any partial paired-chest claim along the way) and
     * stamp the storage-id attachment on both halves. Public so the
     * loot-panel "claim and deposit" path can reuse the auto-claim
     * logic without going through the vanilla GUI open observer.
     */
    public static UUID resolveOrCreateClaim(
            ChestClaimWorkflowDomainService chestService,
            ServerLevel level,
            BlockPos pos,
            ChestAnchor anchor
    ) {
        ClaimedChest existing = chestService.chestByAnchor(anchor);
        if (existing != null) {
            return existing.storageId();
        }
        Set<ChestAnchor> anchors = ChestStorageAnchors.resolveAnchors(level, pos);
        if (anchors.isEmpty()) {
            return null;
        }
        // Detect a partial claim covering the paired half — fold this anchor into it.
        for (ChestAnchor candidate : anchors) {
            ClaimedChest covering = chestService.chestByAnchor(candidate);
            if (covering != null) {
                LinkedHashMap<ChestAnchor, Boolean> merged = new LinkedHashMap<>();
                covering.anchors().forEach(a -> merged.put(a, Boolean.TRUE));
                anchors.forEach(a -> merged.put(a, Boolean.TRUE));
                chestService.updateAnchors(covering.storageId(), merged.keySet());
                stampStorageId(level, pos, covering.storageId());
                return covering.storageId();
            }
        }
        int atlasX = pos.getX() * 100;
        int atlasY = pos.getZ() * 100;
        ClaimedChest created = chestService.claim(anchors, atlasX, atlasY, "");
        if (created == null) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] auto-claim: claim returned null for pos={}", pos);
            return null;
        }
        stampStorageId(level, pos, created.storageId());
        return created.storageId();
    }

    private static void stampStorageId(ServerLevel level, BlockPos pos, UUID storageId) {
        ChestStorageIds.write(level, pos, storageId);
        BlockPos paired = ChestStorageAnchors.pairedChestNeighbor(level, pos);
        if (paired != null) {
            ChestStorageIds.write(level, paired, storageId);
        }
    }

    private record PendingInteraction(BlockPos pos, long tick) {
    }

    private record OpenSession(BlockPos pos, ItemStack[] snapshot, List<Integer> storageSlots) {
    }
}
