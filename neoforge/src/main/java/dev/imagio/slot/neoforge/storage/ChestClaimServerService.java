package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.atlas.StorageZoneAutoPlacement;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestClaimWorkflowDomainService;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.StorageAreaMap;
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
        return claim(player, pos, null, "");
    }

    public static ClaimedChest claim(ServerPlayer player, BlockPos pos, UUID requestedAreaId) {
        return claim(player, pos, requestedAreaId, "");
    }

    public static ClaimedChest claim(
            ServerPlayer player,
            BlockPos pos,
            UUID requestedAreaId,
            String newAreaLabel
    ) {
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

        // Drop dangling projection records at the new positions before a
        // fresh claim — otherwise claimWithId rejects on the duplicate
        // anchor check and we silently no-op.
        purgeStaleAnchors(level, service, anchors);
        stripOrphanAttachments(level, anchors);

        ChestAnchor placementAnchor = anchorByPos(level, pos, anchors);

        // Compute placement first so we know where the new chest will land;
        // a freshly created area's chip is seeded near that placement.
        StorageZoneAutoPlacement.Result preview = StorageZoneAutoPlacement.compute(
                service.claimedChestMap().chests(),
                placementAnchor,
                requestedAreaId,
                StorageZoneAutoPlacement.Config.defaults()
        );

        UUID resolvedAreaId;
        String trimmedNewLabel = newAreaLabel == null ? "" : newAreaLabel.trim();
        if (!trimmedNewLabel.isEmpty()) {
            dev.imagio.slot.workflow.domain.StorageArea created = runtime.storageAreaWorkflow().createArea(
                    trimmedNewLabel,
                    preview.atlasX(),
                    preview.atlasY()
            );
            resolvedAreaId = created == null ? StorageAreaMap.DEFAULT_AREA_ID : created.areaId();
        } else {
            resolvedAreaId = resolveAreaId(runtime, requestedAreaId, placementAnchor);
        }

        StorageZoneAutoPlacement.Result placement = StorageZoneAutoPlacement.compute(
                service.claimedChestMap().chests(),
                placementAnchor,
                resolvedAreaId,
                StorageZoneAutoPlacement.Config.defaults()
        );

        ClaimedChest chest = service.claim(
                anchors,
                placement.atlasX(),
                placement.atlasY(),
                "",
                resolvedAreaId
        );
        if (chest == null) {
            SlotCommon.LOGGER.warn("[SLOT] claim failed to create chest at {}/{}", level.dimension().location(), pos);
            return null;
        }
        writeAttachments(level, chest);
        SlotCommon.LOGGER.info(
                "[SLOT] claimed chest storageId={} anchors={} atlas=({},{}) area={} usedNeighbor={}",
                chest.storageId(), chest.anchors().size(), chest.atlasX(), chest.atlasY(),
                resolvedAreaId, placement.usedNeighbor()
        );
        return chest;
    }

    /**
     * Release a claim for the chest at {@code pos}, deleting the
     * domain record and clearing the {@code slot:storage_id} attachment
     * so the spot is fully fresh for any future claim.
     *
     * @return true if a claim existed and was released; false if the
     *         spot wasn't claimed by this player or is otherwise unknown
     */
    public static boolean unclaim(ServerPlayer player, BlockPos pos) {
        if (player == null || pos == null) {
            return false;
        }
        ServerLevel level = player.serverLevel();
        Set<ChestAnchor> anchors = ChestStorageAnchors.resolveAnchors(level, pos);
        if (anchors.isEmpty()) {
            // No live block entity at this position any more; fall back
            // to a single-anchor probe so we can still release a stranded
            // projection record.
            anchors = Set.of(ChestStorageAnchors.toAnchor(level, pos));
        }
        WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
        ChestClaimWorkflowDomainService service = runtime.chestClaimWorkflow();

        UUID storageId = findLiveExistingId(level, anchors, service);
        if (storageId == null) {
            for (ChestAnchor anchor : anchors) {
                ClaimedChest byAnchor = service.chestByAnchor(anchor);
                if (byAnchor != null) {
                    storageId = byAnchor.storageId();
                    break;
                }
            }
        }
        if (storageId == null) {
            return false;
        }

        ClaimedChest target = service.chest(storageId);
        if (target != null) {
            for (ChestAnchor anchor : target.anchors()) {
                if (!anchor.dimensionId().equals(level.dimension().location().toString())) {
                    continue;
                }
                ChestStorageIds.clear(level, new BlockPos(anchor.x(), anchor.y(), anchor.z()));
            }
        }
        boolean deleted = service.deleteChest(storageId);
        SlotCommon.LOGGER.info(
                "[SLOT] unclaim storageId={} pos={}/{} ok={}",
                storageId, level.dimension().location(), pos, deleted
        );
        return deleted;
    }

    /**
     * Pick the area a new claim should land in.
     *
     * <p>Resolution order: an explicit caller-supplied area (verified to
     * exist), then proximity inference against existing chests, finally the
     * default Main Base. Phase 2 of {@code docs/plans/storage-areas.md}.
     */
    private static UUID resolveAreaId(
            WorkflowDomainRuntime runtime,
            UUID requestedAreaId,
            ChestAnchor placementAnchor
    ) {
        if (requestedAreaId != null
                && runtime.workflowProjection().storageAreaMap().contains(requestedAreaId)) {
            return requestedAreaId;
        }
        UUID inferred = StorageZoneAutoPlacement.inferProximityArea(
                runtime.chestClaimWorkflow().claimedChestMap().chests(),
                placementAnchor,
                StorageZoneAutoPlacement.Config.defaults().worldRadius()
        );
        if (inferred != null
                && runtime.workflowProjection().storageAreaMap().contains(inferred)) {
            return inferred;
        }
        return StorageAreaMap.DEFAULT_AREA_ID;
    }

    /**
     * Identifies an in-world live claim covering the new anchors.
     *
     * <p>Source of truth is the BlockEntity's {@code slot:storage_id}
     * attachment: a claim is "live" only when both the NBT attachment
     * <em>and</em> the projection agree. The previous fallback that
     * accepted a projection-only anchor match silently re-attached new
     * placements to whichever stale claim the projection still
     * remembered (which is what made "+ New Area" land in the old
     * area's bucket when a chest had been broken without firing
     * BlockEvent.BreakEvent). Stranded projection records get cleaned
     * up on next login by ChestPersistenceReconciliation.
     */
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
        return null;
    }

    /**
     * Sweep stale projection anchors at the new claim's positions before
     * we land a fresh claim. A new chest sitting on a previously-claimed
     * spot whose break-event never fired would otherwise leave a dangling
     * record that {@link ChestClaimWorkflowDomainService#claimWithId}
     * rejects as a duplicate anchor.
     */
    private static void purgeStaleAnchors(
            ServerLevel level,
            ChestClaimWorkflowDomainService service,
            Set<ChestAnchor> anchors
    ) {
        for (ChestAnchor anchor : anchors) {
            ClaimedChest stale = service.chestByAnchor(anchor);
            if (stale == null) {
                continue;
            }
            BlockPos anchorPos = new BlockPos(anchor.x(), anchor.y(), anchor.z());
            Optional<UUID> nbtId = ChestStorageIds.read(level, anchorPos);
            if (nbtId.isPresent() && stale.storageId().equals(nbtId.get())) {
                continue;
            }
            SlotCommon.LOGGER.info(
                    "[SLOT] purging stale claim anchor storageId={} pos={}/{} (NBT={})",
                    stale.storageId(),
                    level.dimension().location(),
                    anchorPos,
                    nbtId.map(UUID::toString).orElse("absent")
            );
            service.removeAnchor(stale.storageId(), anchor);
        }
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
