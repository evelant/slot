package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.CarriedIdentityCounts;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Per-refresh identity facts derived from one authority snapshot.
 *
 * <p>Build this inside the active {@link ItemIdentityMatcher.Memo} scope so
 * projection sub-passes share exact stack identity and movable-normalized
 * identity without weakening the matcher contract.
 */
public final class ProjectionIdentityContext {
    private static final ProjectionIdentityContext EMPTY = new ProjectionIdentityContext(
            InventoryAuthoritySnapshot.empty(),
            Map.of(),
            CarriedIdentityCounts.empty(),
            Map.of(),
            0,
            0,
            0,
            0);

    private final InventoryAuthoritySnapshot authority;
    private final Map<String, EntryIdentity> identitiesByEntryKey;
    private final CarriedIdentityCounts carriedCounts;
    private final Map<ItemIdentity, ItemStack> displayStacksByIdentity;
    private final int carriedFreeSlotCount;
    private final int carriedSlotCapacity;
    private final int presentEntryCount;
    private final int carriedEntryCount;

    private ProjectionIdentityContext(
            InventoryAuthoritySnapshot authority,
            Map<String, EntryIdentity> identitiesByEntryKey,
            CarriedIdentityCounts carriedCounts,
            Map<ItemIdentity, ItemStack> displayStacksByIdentity,
            int carriedFreeSlotCount,
            int carriedSlotCapacity,
            int presentEntryCount,
            int carriedEntryCount
    ) {
        this.authority = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        this.identitiesByEntryKey = identitiesByEntryKey == null ? Map.of() : Map.copyOf(identitiesByEntryKey);
        this.carriedCounts = carriedCounts == null ? CarriedIdentityCounts.empty() : carriedCounts;
        this.displayStacksByIdentity = copyDisplayStacks(displayStacksByIdentity);
        this.carriedFreeSlotCount = Math.max(0, carriedFreeSlotCount);
        this.carriedSlotCapacity = Math.max(this.carriedFreeSlotCount, carriedSlotCapacity);
        this.presentEntryCount = Math.max(0, presentEntryCount);
        this.carriedEntryCount = Math.max(0, carriedEntryCount);
    }

    public static ProjectionIdentityContext empty() {
        return EMPTY;
    }

    public static ProjectionIdentityContext from(InventoryAuthoritySnapshot authority) {
        InventoryAuthoritySnapshot resolved = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        if (resolved.host() == null || resolved.sourcesById().isEmpty()) {
            return empty();
        }

        Set<String> carriedSourceIds = carriedSourceIds(resolved);
        LinkedHashMap<String, EntryIdentity> identities = new LinkedHashMap<>();
        LinkedHashMap<ItemIdentity, Integer> carriedCounts = new LinkedHashMap<>();
        LinkedHashMap<ItemIdentity, ItemStack> displayStacks = new LinkedHashMap<>();
        int freeSlots = 0;
        int capacityTotal = 0;
        int presentEntries = 0;
        int carriedEntries = 0;

        for (InventorySourceDescriptor source : resolved.sourceDescriptors()) {
            if (source == null) {
                continue;
            }
            boolean carried = carriedSourceIds.contains(source.id());
            int presentInSource = 0;
            for (InventoryEntrySnapshot entry : resolved.entries(source.id())) {
                if (entry == null || !entry.present() || entry.stack() == null || entry.stack().isEmpty()) {
                    continue;
                }
                presentEntries++;
                presentInSource++;
                ItemIdentity exact = ItemIdentityMatcher.create(entry.stack());
                ItemIdentity movable = ItemIdentityMatcher.normalizeMovable(exact);
                identities.put(entry.entryKey().stableKey(), new EntryIdentity(
                        entry.sourceId(),
                        entry.entryKey().stableKey(),
                        entry.slotIndex(),
                        exact,
                        movable));
                displayStacks.putIfAbsent(ItemIdentityCollections.key(exact), entry.stack().copy());
                displayStacks.putIfAbsent(ItemIdentityCollections.key(movable), entry.stack().copy());
                if (carried) {
                    carriedEntries++;
                    ItemIdentityCollections.mergeCount(carriedCounts, exact, entry.count());
                }
            }
            if (carried) {
                int capacity = resolved.slotCapacity(source.id());
                if (capacity > 0) {
                    freeSlots += Math.max(0, capacity - presentInSource);
                    capacityTotal += capacity;
                }
            }
        }

        return new ProjectionIdentityContext(
                resolved,
                identities,
                carriedCounts.isEmpty() ? CarriedIdentityCounts.empty() : new CarriedIdentityCounts(carriedCounts),
                displayStacks,
                freeSlots,
                capacityTotal,
                presentEntries,
                carriedEntries);
    }

    public InventoryAuthoritySnapshot authority() {
        return authority;
    }

    public ItemIdentity exactIdentity(InventoryEntrySnapshot entry) {
        EntryIdentity identity = entryIdentity(entry);
        return identity == null ? null : identity.exactIdentity();
    }

    public ItemIdentity movableIdentity(InventoryEntrySnapshot entry) {
        EntryIdentity identity = entryIdentity(entry);
        return identity == null ? null : identity.movableIdentity();
    }

    public EntryIdentity entryIdentity(InventoryEntrySnapshot entry) {
        if (entry == null || entry.entryKey() == null) {
            return null;
        }
        return identitiesByEntryKey.get(entry.entryKey().stableKey());
    }

    public CarriedIdentityCounts carriedCounts() {
        return carriedCounts;
    }

    public Map<ItemIdentity, ItemStack> displayStacksByIdentity() {
        return copyDisplayStacks(displayStacksByIdentity);
    }

    public ItemStack displayStack(ItemIdentity identity) {
        ItemStack stack = ItemIdentityCollections.findCanonical(displayStacksByIdentity, identity);
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public int carriedFreeSlotCount() {
        return carriedFreeSlotCount;
    }

    public int carriedSlotCapacity() {
        return carriedSlotCapacity;
    }

    public int presentEntryCount() {
        return presentEntryCount;
    }

    public int carriedEntryCount() {
        return carriedEntryCount;
    }

    private static Set<String> carriedSourceIds(InventoryAuthoritySnapshot authority) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        for (InventorySourceDescriptor source : authority.carriedSources()) {
            if (source != null && !source.id().isBlank()) {
                ids.add(source.id());
            }
        }
        return ids.isEmpty() ? Set.of() : Set.copyOf(ids);
    }

    private static Map<ItemIdentity, ItemStack> copyDisplayStacks(Map<ItemIdentity, ItemStack> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<ItemIdentity, ItemStack> copy = new LinkedHashMap<>();
        for (Map.Entry<ItemIdentity, ItemStack> entry : source.entrySet()) {
            ItemIdentity identity = entry.getKey();
            ItemStack stack = entry.getValue();
            if (identity != null && stack != null && !stack.isEmpty()) {
                copy.put(identity, stack.copy());
            }
        }
        return copy.isEmpty() ? Map.of() : Map.copyOf(copy);
    }

    public record EntryIdentity(
            String sourceId,
            String stableEntryKey,
            int slotIndex,
            ItemIdentity exactIdentity,
            ItemIdentity movableIdentity
    ) {
        public EntryIdentity {
            sourceId = sourceId == null ? "" : sourceId;
            stableEntryKey = stableEntryKey == null ? "" : stableEntryKey;
            slotIndex = Math.max(-1, slotIndex);
        }
    }
}
