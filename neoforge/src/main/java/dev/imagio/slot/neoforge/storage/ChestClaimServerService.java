package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.atlas.StorageZoneAutoPlacement;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestClaimWorkflowDomainService;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ChestClaimServerService {
    private ChestClaimServerService() {
    }

    public static ClaimedChest claim(ServerPlayer player, BlockPos pos) {
        if (player == null || pos == null) {
            return null;
        }
        ServerLevel level = player.serverLevel();
        if (!ChestStorageAnchors.isClaimable(level, pos)) {
            SlotCommon.LOGGER.debug("[SLOT] claim rejected: not claimable at {}/{}", level.dimension().location(), pos);
            return null;
        }

        Set<ChestAnchor> anchors = ChestStorageAnchors.resolveAnchors(level, pos);
        if (anchors.isEmpty()) {
            return null;
        }

        WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
        ChestClaimWorkflowDomainService service = runtime.chestClaimWorkflow();

        UUID existingId = findLiveExistingId(level, anchors, service);
        if (existingId != null) {
            return syncExistingClaim(level, existingId, anchors, service);
        }

        stripOrphanAttachments(level, anchors);

        StorageZoneAutoPlacement.Result placement = StorageZoneAutoPlacement.compute(
                service.claimedChestMap().chests(),
                anchorByPos(level, pos, anchors),
                StorageZoneAutoPlacement.Config.defaults()
        );

        ClaimedChest chest = service.claim(anchors, placement.atlasX(), placement.atlasY(), "");
        if (chest == null) {
            SlotCommon.LOGGER.warn("[SLOT] claim failed to create chest at {}/{}", level.dimension().location(), pos);
            return null;
        }
        writeAttachments(level, chest);
        SlotCommon.LOGGER.info(
                "[SLOT] claimed chest storageId={} anchors={} atlas=({},{}) usedNeighbor={}",
                chest.storageId(), chest.anchors().size(), chest.atlasX(), chest.atlasY(), placement.usedNeighbor()
        );
        return chest;
    }

    private static UUID findLiveExistingId(
            ServerLevel level,
            Set<ChestAnchor> anchors,
            ChestClaimWorkflowDomainService service
    ) {
        for (ChestAnchor anchor : anchors) {
            BlockPos anchorPos = new BlockPos(anchor.x(), anchor.y(), anchor.z());
            Optional<UUID> existing = ChestStorageIds.read(level, anchorPos);
            if (existing.isPresent() && service.chest(existing.get()) != null) {
                return existing.get();
            }
        }
        for (ChestAnchor anchor : anchors) {
            ClaimedChest byAnchor = service.chestByAnchor(anchor);
            if (byAnchor != null) {
                return byAnchor.storageId();
            }
        }
        return null;
    }

    private static ClaimedChest syncExistingClaim(
            ServerLevel level,
            UUID storageId,
            Set<ChestAnchor> anchors,
            ChestClaimWorkflowDomainService service
    ) {
        ClaimedChest existing = service.chest(storageId);
        if (existing == null) {
            return null;
        }
        if (!existing.anchors().equals(anchors)) {
            LinkedHashSet<ChestAnchor> merged = new LinkedHashSet<>(existing.anchors());
            merged.addAll(anchors);
            ClaimedChest updated = service.updateAnchors(storageId, merged);
            if (updated != null) {
                existing = updated;
            }
        }
        writeAttachments(level, existing);
        return existing;
    }

    private static void stripOrphanAttachments(ServerLevel level, Set<ChestAnchor> anchors) {
        for (ChestAnchor anchor : anchors) {
            if (!anchor.dimensionId().equals(level.dimension().location().toString())) {
                continue;
            }
            ChestStorageIds.clear(level, new BlockPos(anchor.x(), anchor.y(), anchor.z()));
        }
    }

    private static void writeAttachments(ServerLevel level, ClaimedChest chest) {
        for (ChestAnchor anchor : chest.anchors()) {
            if (!anchor.dimensionId().equals(level.dimension().location().toString())) {
                continue;
            }
            ChestStorageIds.write(level, new BlockPos(anchor.x(), anchor.y(), anchor.z()), chest.storageId());
        }
    }

    private static ChestAnchor anchorByPos(ServerLevel level, BlockPos pos, Set<ChestAnchor> anchors) {
        ChestAnchor target = ChestStorageAnchors.toAnchor(level, pos);
        for (ChestAnchor anchor : anchors) {
            if (anchor.equals(target)) {
                return anchor;
            }
        }
        return target;
    }
}
