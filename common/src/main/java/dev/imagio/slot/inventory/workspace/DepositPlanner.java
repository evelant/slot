package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.workflow.domain.ChestLinkMap;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeMap;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class DepositPlanner {
    private static final String[] CARRIED_LANE_IDS = new String[]{
            BuiltinInventoryIds.PLAYER_MAIN,
            BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
            BuiltinInventoryIds.PLAYER_OFFHAND
    };

    private DepositPlanner() {
    }

    public static DepositPlan plan(
            InventoryAuthoritySnapshot authority,
            VisualHomeMap visualHomeMap,
            ChestLinkMap chestLinkMap,
            Set<String> proximateStorageIds
    ) {
        if (authority == null || visualHomeMap == null || chestLinkMap == null) {
            return DepositPlan.empty();
        }
        Set<String> resolvedProximate = proximateStorageIds == null ? Set.of() : proximateStorageIds;
        if (resolvedProximate.isEmpty()) {
            return DepositPlan.empty();
        }

        ArrayList<DepositPlan.Assignment> assignments = new ArrayList<>();
        for (String laneId : CARRIED_LANE_IDS) {
            for (InventoryEntrySnapshot entry : authority.entries(laneId)) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.create(entry.stack());
                VisualHomeAssignment home = visualHomeMap.assignment(identity);
                if (home == null) {
                    continue;
                }
                String islandId = home.islandId();
                if (islandId == null || islandId.isBlank()
                        || SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId)) {
                    continue;
                }

                Set<UUID> linkedChests = chestLinkMap.chestsLinkedFrom(islandId);
                if (linkedChests.isEmpty()) {
                    continue;
                }

                LinkedHashSet<String> candidates = new LinkedHashSet<>();
                for (UUID storageId : linkedChests) {
                    String idString = storageId.toString();
                    if (resolvedProximate.contains(idString)) {
                        candidates.add(idString);
                    }
                }
                if (candidates.isEmpty()) {
                    continue;
                }

                assignments.add(new DepositPlan.Assignment(
                        laneId,
                        entry.slotIndex(),
                        identity.itemId(),
                        new ArrayList<>(candidates)
                ));
            }
        }
        return new DepositPlan(assignments);
    }
}
