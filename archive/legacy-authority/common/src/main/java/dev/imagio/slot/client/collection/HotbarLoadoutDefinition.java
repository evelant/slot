package dev.imagio.slot.client.collection;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.model.ItemIdentitySupport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record HotbarLoadoutDefinition(
        String id,
        String name,
        Integer hotkeySlot,
        List<HotbarLoadoutSlot> slots,
        ItemIdentity offhandIdentity
) {
    public static final int HOTBAR_SLOT_COUNT = 9;
    public static final int OFFHAND_SLOT_INDEX = 9;
    public static final int QUICK_ACCESS_SLOT_COUNT = 10;

    public HotbarLoadoutDefinition(String id, String name, Integer hotkeySlot, List<HotbarLoadoutSlot> slots) {
        this(id, name, hotkeySlot, slots, null);
    }

    public HotbarLoadoutDefinition(String id, String name, List<HotbarLoadoutSlot> slots) {
        this(id, name, null, slots, null);
    }

    public HotbarLoadoutDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Loadout id must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Loadout name must not be blank");
        }

        Map<Integer, HotbarLoadoutSlot> normalizedSlots = new LinkedHashMap<>();
        if (slots != null) {
            for (HotbarLoadoutSlot slot : slots) {
                if (slot == null) {
                    continue;
                }
                normalizedSlots.put(
                        slot.slotIndex(),
                        new HotbarLoadoutSlot(slot.slotIndex(), ItemIdentitySupport.normalizeQuickAccessIdentity(slot.identity()))
                );
            }
        }

        id = id.trim();
        name = name.trim();
        hotkeySlot = hotkeySlot == null || hotkeySlot < 0 || hotkeySlot > 8 ? null : hotkeySlot;
        slots = List.copyOf(new ArrayList<>(normalizedSlots.values()));
        offhandIdentity = ItemIdentitySupport.normalizeQuickAccessIdentity(offhandIdentity);
    }

    public int configuredSlotCount() {
        return slots.size() + (offhandIdentity == null ? 0 : 1);
    }

    public ItemIdentity identityForSlot(int slotIndex) {
        for (HotbarLoadoutSlot slot : slots) {
            if (slot.slotIndex() == slotIndex) {
                return slot.identity();
            }
        }
        return null;
    }

    public ItemIdentity identityForQuickAccessSlot(int slotIndex) {
        return slotIndex == OFFHAND_SLOT_INDEX ? offhandIdentity : identityForSlot(slotIndex);
    }

    public String hotkeyIndicator() {
        return hotkeySlot == null ? "H-" : "H" + (hotkeySlot + 1);
    }
}
