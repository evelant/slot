package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestClusterMap;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiFunction;

/**
 * Shared active-container panel projection. Loader adapters supply only the
 * currently open chest position and their anchor resolver; claim/cluster label
 * semantics stay common so Forge and NeoForge sidebars cannot drift.
 */
public final class ActiveChestPanelProjectionSupport {
    private ActiveChestPanelProjectionSupport() {
    }

    public static SlotWorkspaceViewModel.ActiveChestPanel resolve(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ClaimedChestMap claimedChestMap,
            BlockPos activeChestPos,
            BiFunction<ServerLevel, BlockPos, ChestAnchor> anchorResolver
    ) {
        if (player == null || runtime == null || activeChestPos == null || anchorResolver == null) {
            return SlotWorkspaceViewModel.ActiveChestPanel.empty();
        }
        ServerLevel level = player.serverLevel();
        String dimensionId = level.dimension().location().toString();
        ChestAnchor anchor = anchorResolver.apply(level, activeChestPos);
        ClaimedChest claim = anchor == null ? null : runtime.chestClaimWorkflow().chestByAnchor(anchor);
        if (claim == null) {
            return new SlotWorkspaceViewModel.ActiveChestPanel(
                    "", "", "", "", 0,
                    activeChestPos.getX(), activeChestPos.getY(), activeChestPos.getZ(),
                    dimensionId
            );
        }

        ClaimedChestMap resolvedMap = claimedChestMap == null ? ClaimedChestMap.empty() : claimedChestMap;
        ChestClusterMap clusterMap = ChestClusterMap.derive(resolvedMap);
        ChestClusterMap.Cluster cluster = null;
        for (ChestClusterMap.Cluster candidate : clusterMap.clusters()) {
            if (candidate.storageIds().contains(claim.storageId())) {
                cluster = candidate;
                break;
            }
        }
        String clusterId = cluster == null ? "" : cluster.clusterId();
        String customClusterLabel = clusterId.isEmpty()
                ? ""
                : runtime.snapshot().clusterLabels().getOrDefault(clusterId, "");
        String clusterLabel = customClusterLabel.isBlank() && cluster != null
                ? cluster.defaultLabel()
                : customClusterLabel;
        String chestLabel = claim.label() == null || claim.label().isBlank()
                ? "Chest"
                : claim.label();
        return new SlotWorkspaceViewModel.ActiveChestPanel(
                claim.storageId().toString(),
                chestLabel,
                clusterId,
                clusterLabel,
                0,
                activeChestPos.getX(), activeChestPos.getY(), activeChestPos.getZ(),
                dimensionId
        );
    }
}
