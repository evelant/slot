package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record KitPage(
        List<ItemIdentity> hotbarIdentities
) {
    public static final int HOTBAR_SLOT_COUNT = 9;

    public KitPage {
        hotbarIdentities = normalize(hotbarIdentities);
    }

    public static KitPage empty() {
        ArrayList<ItemIdentity> empty = new ArrayList<>(HOTBAR_SLOT_COUNT);
        for (int index = 0; index < HOTBAR_SLOT_COUNT; index++) {
            empty.add(null);
        }
        return new KitPage(empty);
    }

    public KitPage withSlot(int slotIndex, ItemIdentity identity) {
        if (slotIndex < 0 || slotIndex >= HOTBAR_SLOT_COUNT) {
            throw new IllegalArgumentException("slotIndex out of range: " + slotIndex);
        }
        ArrayList<ItemIdentity> next = new ArrayList<>(hotbarIdentities);
        next.set(slotIndex, identity);
        return new KitPage(next);
    }

    public int filledSlotCount() {
        int count = 0;
        for (ItemIdentity identity : hotbarIdentities) {
            if (identity != null) {
                count++;
            }
        }
        return count;
    }

    public ItemIdentity slot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= hotbarIdentities.size()) {
            return null;
        }
        return hotbarIdentities.get(slotIndex);
    }

    private static List<ItemIdentity> normalize(List<ItemIdentity> source) {
        ArrayList<ItemIdentity> normalized = new ArrayList<>(HOTBAR_SLOT_COUNT);
        if (source != null) {
            for (int index = 0; index < HOTBAR_SLOT_COUNT && index < source.size(); index++) {
                normalized.add(source.get(index));
            }
        }
        while (normalized.size() < HOTBAR_SLOT_COUNT) {
            normalized.add(null);
        }
        return Collections.unmodifiableList(normalized);
    }
}
