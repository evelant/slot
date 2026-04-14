package dev.imagio.slot.client.policy;

import dev.imagio.slot.client.category.InventoryGroupingOverrides;
import dev.imagio.slot.client.category.CategoryResolver;
import dev.imagio.slot.client.category.CategorySignal;
import dev.imagio.slot.client.category.CategorySubject;
import dev.imagio.slot.client.category.SlotCategory;
import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.model.ItemIdentitySupport;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ItemBehaviorPolicy {
    public enum DirectInventoryAction {
        USE("slot.screen.inventory.use_from_inventory"),
        PLACE("slot.screen.inventory.place_from_inventory");

        private final String translationKey;

        DirectInventoryAction(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }
    }

    public record ItemCompatibility(
            boolean stableMovableIdentity,
            boolean protectFromBulkStore,
            boolean implicitJunkCandidate,
            DirectInventoryAction directInventoryAction
    ) {
        private static final ItemCompatibility EMPTY = new ItemCompatibility(false, false, false, null);
    }

    public record ItemInspection(
            ItemIdentity identity,
            SlotCategory category,
            CategoryResolver.Resolution categoryResolution,
            InventoryGroupingOverrides.GroupingBucket fallbackGrouping,
            Set<String> collections,
            Map<String, Integer> desiredCountsByCollection,
            ItemCompatibility compatibility
    ) {
        public ItemInspection {
            collections = collections == null ? Set.of() : Set.copyOf(collections);
            desiredCountsByCollection = desiredCountsByCollection == null ? Map.of() : Map.copyOf(desiredCountsByCollection);
        }
    }

    private ItemBehaviorPolicy() {
    }

    public static ItemIdentity createIdentity(ItemStack stack) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (!stack.isStackable() || !stack.getComponentsPatch().isEmpty()) {
            return ItemIdentity.exact(itemId, stack.getComponentsPatch().toString());
        }
        return ItemIdentity.of(itemId);
    }

    public static ItemIdentity normalizeTrackedIdentity(ItemIdentity identity) {
        return ItemIdentitySupport.normalizeTrackedIdentity(identity);
    }

    public static ItemIdentity normalizeQuickAccessIdentity(ItemIdentity identity) {
        return ItemIdentitySupport.normalizeQuickAccessIdentity(identity);
    }

    public static ItemIdentity normalizeMovableIdentity(ItemIdentity identity) {
        return ItemIdentitySupport.normalizeMovableIdentity(identity);
    }

    public static boolean matchesTrackedIdentity(ItemIdentity left, ItemIdentity right) {
        return ItemIdentitySupport.matchesTrackedIdentity(left, right);
    }

    public static boolean matchesMovableIdentity(ItemIdentity left, ItemIdentity right) {
        return ItemIdentitySupport.matchesMovableIdentity(left, right);
    }

    public static boolean matchesMovableIdentity(ItemStack stack, ItemIdentity identity) {
        if (stack.isEmpty() || identity == null) {
            return false;
        }
        if (matchesIdentity(stack, identity)) {
            return true;
        }
        return ItemIdentitySupport.matchesMovableIdentity(createIdentity(stack), identity);
    }

    public static ItemStack approximateDisplayStack(ItemIdentity identity) {
        if (identity == null || identity.itemId() == null || identity.itemId().isBlank()) {
            return ItemStack.EMPTY;
        }

        ResourceLocation itemId = ResourceLocation.tryParse(identity.itemId());
        if (itemId == null) {
            return ItemStack.EMPTY;
        }

        return BuiltInRegistries.ITEM.getOptional(itemId)
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
    }

    public static SlotCategory resolveCategory(ItemIdentity identity, ItemStack stack) {
        return ItemCategoryHeuristics.categoryResolver().resolve(new CategorySubject(identity, ItemCategoryHeuristics.collectSignals(stack)));
    }

    public static InventoryGroupingOverrides.GroupingBucket resolveFallbackGrouping(ItemIdentity identity, SlotCategory category) {
        return InventoryGroupingOverrides.resolveFallbackBucket(identity, category);
    }

    public static boolean usesStableMovableIdentity(String itemId) {
        return ItemIdentitySupport.usesStableMovableIdentity(itemId);
    }

    public static ItemCompatibility compatibility(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemCompatibility.EMPTY;
        }

        boolean portableContainer = isPortableContainerStack(stack);
        DirectInventoryAction directAction = ItemCategoryHeuristics.resolveDirectInventoryAction(stack, portableContainer);
        return new ItemCompatibility(
                ItemCategoryHeuristics.supportsStableMovableIdentity(stack),
                portableContainer,
                ItemCategoryHeuristics.isImplicitJunkCandidateStack(stack, portableContainer),
                directAction
        );
    }

    public static DirectInventoryAction directInventoryAction(ItemStack stack) {
        return compatibility(stack).directInventoryAction();
    }

    public static boolean canOfferDirectUse(ItemStack stack) {
        return directInventoryAction(stack) != null;
    }

    public static boolean shouldProtectFromBulkStore(ItemStack stack) {
        return compatibility(stack).protectFromBulkStore();
    }

    public static CategoryResolver.Resolution inspectCategoryResolution(ItemIdentity identity, ItemStack stack) {
        if (identity == null) {
            return new CategoryResolver.Resolution(
                    SlotCategory.MISC,
                    CategoryResolver.ResolutionSource.FALLBACK_MISC,
                    EnumSet.noneOf(CategorySignal.class)
            );
        }
        return ItemCategoryHeuristics.categoryResolver().resolveDetailed(new CategorySubject(identity, ItemCategoryHeuristics.collectSignals(stack)));
    }

    public static ItemInspection inspectItem(CollectionStore collectionStore, ItemIdentity identity, ItemStack stack) {
        if (identity == null) {
            return new ItemInspection(
                    null,
                    SlotCategory.MISC,
                    new CategoryResolver.Resolution(
                            SlotCategory.MISC,
                            CategoryResolver.ResolutionSource.FALLBACK_MISC,
                            EnumSet.noneOf(CategorySignal.class)
                    ),
                    null,
                    Set.of(),
                    Map.of(),
                    ItemCompatibility.EMPTY
            );
        }

        CategoryResolver.Resolution categoryResolution = inspectCategoryResolution(identity, stack);
        Set<String> collectionIds = collectionStore == null ? Set.of() : collectionStore.collectionsFor(identity);
        Map<String, Integer> desiredCountsByCollection = new LinkedHashMap<>();
        if (collectionStore != null) {
            for (String collectionId : collectionIds) {
                if (!CollectionStore.FAVORITES_ID.equals(collectionId) && !CollectionStore.JUNK_ID.equals(collectionId)) {
                    desiredCountsByCollection.put(collectionId, collectionStore.desiredCount(collectionId, identity));
                }
            }
        }

        return new ItemInspection(
                identity,
                categoryResolution.category(),
                categoryResolution,
                resolveFallbackGrouping(identity, categoryResolution.category()),
                collectionIds,
                desiredCountsByCollection,
                compatibility(stack)
        );
    }

    public static boolean isPortableContainerStack(ItemStack stack) {
        return ItemCategoryHeuristics.isPortableContainerStack(stack);
    }

    private static boolean matchesIdentity(ItemStack stack, ItemIdentity identity) {
        if (stack.isEmpty() || identity == null) {
            return false;
        }

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (!itemId.equals(identity.itemId())) {
            return false;
        }

        return switch (identity.comparisonMode()) {
            case ITEM_ID -> true;
            case ITEM_ID_AND_COMPONENTS -> stack.getComponentsPatch().toString().equals(identity.componentFingerprint());
        };
    }
}
