package dev.imagio.slot.workflow.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ClaimedChestMap(
        List<ClaimedChest> chests
) {
    public ClaimedChestMap {
        chests = copyChests(chests);
    }

    public static ClaimedChestMap empty() {
        return new ClaimedChestMap(List.of());
    }

    public ClaimedChest chest(UUID storageId) {
        if (storageId == null) {
            return null;
        }
        for (ClaimedChest chest : chests) {
            if (chest != null && storageId.equals(chest.storageId())) {
                return chest;
            }
        }
        return null;
    }

    public ClaimedChest chestByAnchor(ChestAnchor anchor) {
        if (anchor == null) {
            return null;
        }
        for (ClaimedChest chest : chests) {
            if (chest != null && chest.hasAnchor(anchor)) {
                return chest;
            }
        }
        return null;
    }

    public static List<ClaimedChest> copyChests(List<ClaimedChest> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        Map<UUID, ClaimedChest> uniqueById = new LinkedHashMap<>();
        for (ClaimedChest chest : source) {
            if (chest != null) {
                uniqueById.put(chest.storageId(), chest);
            }
        }
        return List.copyOf(new ArrayList<>(uniqueById.values()));
    }
}
