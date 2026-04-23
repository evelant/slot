package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
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
        // Walk every carried source (main, hotbar, offhand, backpacks, curios,
        // any future provider) in stableOrder. Previously this iterated a
        // hardcoded vanilla-lane list and silently skipped backpacks, which
        // was the "bulk Deposit ignores backpacks" bug class.
        //
        // When a host descriptor is present (production path) we use
        // carriedSources() to respect pane membership. When host is absent
        // (test fixtures that construct an authority from raw source
        // snapshots) we fall back to iterating every source id — the caller
        // is responsible for only including carried sources in the snapshot.
        List<InventorySourceDescriptor> declaredCarried = authority.carriedSources();
        Iterable<String> sourceIds = declaredCarried.isEmpty()
                ? authority.sourcesById().keySet()
                : declaredCarried.stream().map(InventorySourceDescriptor::id).toList();
        for (String sourceId : sourceIds) {
            for (InventoryEntrySnapshot entry : authority.entries(sourceId)) {
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
                        sourceId,
                        entry.slotIndex(),
                        identity.itemId(),
                        new ArrayList<>(candidates)
                ));
            }
        }
        return new DepositPlan(assignments);
    }
}
