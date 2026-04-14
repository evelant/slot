package dev.imagio.slot.inventory.kernel;

import dev.imagio.slot.session.ChestLikeMenuLayout;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ActionableSourcePolicy {
    private final MenuSlotResolver resolver;

    public ActionableSourcePolicy(MenuSlotResolver resolver) {
        this.resolver = resolver;
    }

    public boolean sourceActionable(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return false;
        }
        if (ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK.equals(sourceId)) {
            return true;
        }
        return !menuSlotsForSource(sourceId).isEmpty();
    }

    public List<Integer> menuSlotsForSource(String sourceId) {
        if (resolver != null && resolver.layout() != null) {
            return resolver.menuSlotsForSource(sourceId);
        }
        return vanillaSlotsForSource(sourceId, resolver == null ? null : resolver.menu());
    }

    public List<Integer> playerTransferTargets(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return List.of();
        }

        if (ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK.equals(sourceId)
                || ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE.equals(sourceId)) {
            ArrayList<Integer> targets = new ArrayList<>();
            targets.addAll(mainInventoryMenuSlots());
            targets.addAll(hotbarMenuSlots());
            return List.copyOf(targets);
        }

        if (resolver != null && resolver.layout() != null) {
            if (!resolver.sourceMenuBacked(sourceId)) {
                return List.of();
            }
            return resolver.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE);
        }

        return vanillaSlotsForSource(sourceId, resolver == null ? null : resolver.menu());
    }

    public List<Integer> mainInventoryMenuSlots() {
        if (resolver != null && resolver.layout() != null) {
            return resolver.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN);
        }
        return slotRange(InventoryMenu.INV_SLOT_START, InventoryMenu.INV_SLOT_END, resolver == null ? null : resolver.menu());
    }

    public List<Integer> hotbarMenuSlots() {
        if (resolver != null && resolver.layout() != null) {
            return resolver.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR);
        }
        return slotRange(InventoryMenu.USE_ROW_SLOT_START, InventoryMenu.USE_ROW_SLOT_END, resolver == null ? null : resolver.menu());
    }

    public List<Integer> armorMenuSlots() {
        if (resolver != null && resolver.layout() != null) {
            return resolver.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR);
        }
        return slotRange(InventoryMenu.ARMOR_SLOT_START, InventoryMenu.ARMOR_SLOT_END, resolver == null ? null : resolver.menu());
    }

    public Integer offhandMenuSlot() {
        if (resolver != null && resolver.layout() != null) {
            Integer resolved = resolver.resolveMenuSlot(ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND, 0);
            if (resolved != null) {
                return resolved;
            }
        }
        return MenuSlotResolver.safeSlot(resolver == null ? null : resolver.menu(), InventoryMenu.SHIELD_SLOT) == null
                ? null
                : InventoryMenu.SHIELD_SLOT;
    }

    public List<Integer> quickAccessCandidateSourceSlots(Set<String> preferredSourceIds) {
        LinkedHashSet<Integer> slots = new LinkedHashSet<>();
        if (preferredSourceIds != null && !preferredSourceIds.isEmpty()) {
            for (String sourceId : preferredSourceIds) {
                slots.addAll(menuSlotsForSource(sourceId));
            }
        }
        slots.addAll(mainInventoryMenuSlots());
        slots.addAll(hotbarMenuSlots());
        slots.addAll(armorMenuSlots());
        Integer offhandSlot = offhandMenuSlot();
        if (offhandSlot != null) {
            slots.add(offhandSlot);
        }
        if (resolver != null && resolver.layout() != null) {
            slots.addAll(resolver.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE));
        }
        return List.copyOf(slots);
    }

    public List<Integer> playerPickupSourceSlots() {
        LinkedHashSet<Integer> slots = new LinkedHashSet<>();
        Integer offhandSlot = offhandMenuSlot();
        if (offhandSlot != null) {
            slots.add(offhandSlot);
        }
        slots.addAll(armorMenuSlots());
        slots.addAll(mainInventoryMenuSlots());
        slots.addAll(hotbarMenuSlots());
        return List.copyOf(slots);
    }

    public List<Integer> playerInsertTargetSlots() {
        LinkedHashSet<Integer> slots = new LinkedHashSet<>();
        slots.addAll(mainInventoryMenuSlots());
        slots.addAll(hotbarMenuSlots());
        return List.copyOf(slots);
    }

    private static List<Integer> vanillaSlotsForSource(String sourceId, AbstractContainerMenu menu) {
        return switch (sourceId) {
            case ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR -> slotRange(InventoryMenu.USE_ROW_SLOT_START, InventoryMenu.USE_ROW_SLOT_END, menu);
            case ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR -> slotRange(InventoryMenu.ARMOR_SLOT_START, InventoryMenu.ARMOR_SLOT_END, menu);
            case ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND -> {
                if (menu == null || MenuSlotResolver.safeSlot(menu, InventoryMenu.SHIELD_SLOT) != null) {
                    yield List.of(InventoryMenu.SHIELD_SLOT);
                }
                yield List.of();
            }
            case ChestLikeMenuLayout.SOURCE_PLAYER_MAIN -> slotRange(InventoryMenu.INV_SLOT_START, InventoryMenu.INV_SLOT_END, menu);
            default -> List.of();
        };
    }

    private static List<Integer> slotRange(int startInclusive, int endExclusive, AbstractContainerMenu menu) {
        ArrayList<Integer> slots = new ArrayList<>(Math.max(0, endExclusive - startInclusive));
        for (int slotId = startInclusive; slotId < endExclusive; slotId++) {
            if (menu == null || MenuSlotResolver.safeSlot(menu, slotId) != null) {
                slots.add(slotId);
            }
        }
        return List.copyOf(slots);
    }
}
