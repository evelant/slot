package dev.imagio.slot.inventory.core;

import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;

public final class PlacementPolicy {
    private PlacementPolicy() {
    }

    public static List<InventorySourceDescriptor> orderedInsertionSources(
            InventoryHostDescriptor host,
            InventoryPaneMembership paneMembership,
            InventoryCapability requiredCapability
    ) {
        if (host == null || paneMembership == null || requiredCapability == null) {
            return List.of();
        }
        return host.sourceDescriptors().stream()
                .filter(source -> source != null
                        && source.paneMembership() == paneMembership
                        && source.supports(requiredCapability)
                        && source.actionable())
                .sorted(Comparator.comparingInt(InventorySourceDescriptor::stableOrder))
                .toList();
    }

    public static List<Integer> orderedMenuSlotsForInsertion(
            InventoryHostDescriptor host,
            String sourceId,
            ItemStack stack
    ) {
        if (host == null || sourceId == null || sourceId.isBlank() || stack == null || stack.isEmpty()) {
            return List.of();
        }
        return InventoryBindingResolver.resolveMenuSlotsForSource(host, sourceId);
    }
}
