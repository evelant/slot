package dev.imagio.slot.workflow;

import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class InspectionService {
    private final CollectionStore collectionStore;

    public InspectionService(CollectionStore collectionStore) {
        this.collectionStore = Objects.requireNonNull(collectionStore, "collectionStore");
    }

    public InspectionView inspect(ItemIdentity identity, ItemStack displayStack, String displayName) {
        ItemStack resolvedDisplayStack = displayStack == null ? ItemStack.EMPTY : displayStack.copy();
        if (resolvedDisplayStack.isEmpty() && identity != null) {
            resolvedDisplayStack = ItemBehaviorPolicy.approximateDisplayStack(identity);
        }

        ItemBehaviorPolicy.ItemInspection inspection = ItemBehaviorPolicy.inspectItem(collectionStore, identity, resolvedDisplayStack);
        return new InspectionView(
                identity,
                resolvedDisplayStack,
                resolveDisplayName(identity, resolvedDisplayStack, displayName),
                inspection
        );
    }

    private static Component resolveDisplayName(ItemIdentity identity, ItemStack displayStack, String displayName) {
        if (displayName != null && !displayName.isBlank()) {
            return Component.literal(displayName);
        }
        if (displayStack != null && !displayStack.isEmpty()) {
            return displayStack.getHoverName();
        }
        if (identity != null) {
            return Component.literal(identity.itemId());
        }
        return Component.translatable("slot.screen.debug.none");
    }

    public record InspectionView(
            ItemIdentity identity,
            ItemStack displayStack,
            Component displayName,
            ItemBehaviorPolicy.ItemInspection inspection
    ) {
        public InspectionView {
            displayStack = displayStack == null ? ItemStack.EMPTY : displayStack.copy();
            displayName = displayName == null ? Component.empty() : displayName;
            inspection = inspection == null
                    ? ItemBehaviorPolicy.inspectItem(null, null, ItemStack.EMPTY)
                    : inspection;
        }
    }
}
