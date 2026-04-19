package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestClaimWorkflowDomainService;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Walks a freshly-loaded {@link WorkflowDomainRuntime}'s claimed chests and prunes
 * anchors whose in-world block entities no longer carry the expected {@code slot:storage_id}
 * attachment. Anchors in unloaded chunks are left alone (unknown ≠ broken) so players don't
 * lose claims they haven't visited since logging in. When all of a claim's anchors are
 * known-broken, the claim itself is deleted (cascading to its links via the projection
 * reducer).
 */
public final class ChestPersistenceReconciliation {
    private ChestPersistenceReconciliation() {
    }

    public static void reconcile(MinecraftServer server, WorkflowDomainRuntime runtime) {
        if (server == null || runtime == null) {
            return;
        }
        ChestClaimWorkflowDomainService chestService = runtime.chestClaimWorkflow();
        List<ClaimedChest> claims = new ArrayList<>(chestService.claimedChestMap().chests());
        int removedAnchors = 0;
        int deletedChests = 0;
        for (ClaimedChest chest : claims) {
            if (chest == null) {
                continue;
            }
            ReconcileResult result = reconcileChest(server, chest);
            if (result.deleted) {
                chestService.deleteChest(
                        chest.storageId(),
                        DomainEventMetadata.origin("workflow.storage.chest.reconcile.delete")
                );
                deletedChests++;
                removedAnchors += result.removedAnchors;
                continue;
            }
            if (result.removedAnchors > 0) {
                chestService.updateAnchors(
                        chest.storageId(),
                        result.remainingAnchors,
                        DomainEventMetadata.origin("workflow.storage.chest.reconcile.anchors")
                );
                removedAnchors += result.removedAnchors;
            }
        }
        if (removedAnchors > 0 || deletedChests > 0) {
            SlotCommon.LOGGER.info(
                    "[SLOT] persistence reconcile removed_anchors={} deleted_chests={}",
                    removedAnchors, deletedChests
            );
        }
    }

    private static ReconcileResult reconcileChest(MinecraftServer server, ClaimedChest chest) {
        LinkedHashSet<ChestAnchor> surviving = new LinkedHashSet<>();
        int broken = 0;
        boolean anyUnknown = false;
        for (ChestAnchor anchor : chest.anchors()) {
            AnchorState state = evaluateAnchor(server, chest.storageId(), anchor);
            switch (state) {
                case ALIVE -> surviving.add(anchor);
                case UNKNOWN -> {
                    surviving.add(anchor);
                    anyUnknown = true;
                }
                case BROKEN -> broken++;
            }
        }
        if (surviving.isEmpty() && !anyUnknown) {
            return new ReconcileResult(true, broken, LinkedHashSet.newLinkedHashSet(0));
        }
        if (broken == 0) {
            return new ReconcileResult(false, 0, LinkedHashSet.newLinkedHashSet(0));
        }
        return new ReconcileResult(false, broken, surviving);
    }

    private static AnchorState evaluateAnchor(MinecraftServer server, UUID expectedStorageId, ChestAnchor anchor) {
        if (anchor == null) {
            return AnchorState.BROKEN;
        }
        ServerLevel level = resolveLevel(server, anchor);
        if (level == null) {
            return AnchorState.UNKNOWN;
        }
        BlockPos pos = new BlockPos(anchor.x(), anchor.y(), anchor.z());
        if (!level.isLoaded(pos)) {
            return AnchorState.UNKNOWN;
        }
        Optional<UUID> actual = ChestStorageIds.read(level, pos);
        if (actual.isEmpty()) {
            return AnchorState.BROKEN;
        }
        return expectedStorageId.equals(actual.get()) ? AnchorState.ALIVE : AnchorState.BROKEN;
    }

    private static ServerLevel resolveLevel(MinecraftServer server, ChestAnchor anchor) {
        if (anchor.dimensionId() == null || anchor.dimensionId().isBlank()) {
            return null;
        }
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().location().toString().equals(anchor.dimensionId())) {
                return level;
            }
        }
        return null;
    }

    private enum AnchorState {
        ALIVE,
        UNKNOWN,
        BROKEN
    }

    private record ReconcileResult(boolean deleted, int removedAnchors, LinkedHashSet<ChestAnchor> remainingAnchors) {
    }
}
