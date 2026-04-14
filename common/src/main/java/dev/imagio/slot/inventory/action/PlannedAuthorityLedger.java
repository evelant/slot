package dev.imagio.slot.inventory.action;

import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.inventory.query.ProjectedEntryRef;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

final class PlannedAuthorityLedger {
    private final Map<String, SlotBackedSourceState> slotStates = new LinkedHashMap<>();
    private final Map<String, LinkedHashSet<ItemIdentity>> identitiesBySource = new LinkedHashMap<>();

    PlannedAuthorityLedger(InventoryAuthoritySnapshot authority) {
        if (authority == null || authority.host() == null) {
            return;
        }
        InventoryHostDescriptor host = authority.host();
        for (InventorySourceDescriptor source : host.sourceDescriptors()) {
            if (source == null) {
                continue;
            }
            InventorySourceSnapshot snapshot = authority.sourceSnapshot(source.id());
            if (snapshot == null) {
                continue;
            }
            identitiesBySource.put(source.id(), identitySet(snapshot));
            if (!source.providerBacked()) {
                slotStates.put(source.id(), new SlotBackedSourceState(snapshot));
            }
        }
    }

    boolean sourceContainsIdentity(String sourceId, ItemIdentity identity) {
        if (sourceId == null || sourceId.isBlank() || identity == null) {
            return false;
        }
        SlotBackedSourceState slotState = slotStates.get(sourceId);
        if (slotState != null && slotState.contains(identity)) {
            return true;
        }
        LinkedHashSet<ItemIdentity> identities = identitiesBySource.get(sourceId);
        if (identities == null) {
            return false;
        }
        for (ItemIdentity candidate : identities) {
            if (ItemIdentityMatcher.matchesMovable(candidate, identity)) {
                return true;
            }
        }
        return false;
    }

    int acceptIntoSource(String sourceId, ItemIdentity identity, ItemStack template, int requestedCount) {
        if (sourceId == null || sourceId.isBlank() || identity == null || template == null || template.isEmpty()) {
            return 0;
        }
        SlotBackedSourceState slotState = slotStates.get(sourceId);
        if (slotState == null) {
            return 0;
        }
        int accepted = slotState.accept(identity, template, requestedCount);
        if (accepted > 0) {
            identitiesBySource.computeIfAbsent(sourceId, ignored -> new LinkedHashSet<>()).add(identity);
        }
        return accepted;
    }

    void noteProviderInsert(String sourceId, ItemIdentity identity, int acceptedCount) {
        if (sourceId == null || sourceId.isBlank() || identity == null || acceptedCount <= 0) {
            return;
        }
        identitiesBySource.computeIfAbsent(sourceId, ignored -> new LinkedHashSet<>()).add(identity);
    }

    void noteExtraction(ProjectedEntryRef entry, int extractedCount) {
        if (entry == null || extractedCount <= 0 || !entry.slotBacked()) {
            return;
        }
        SlotBackedSourceState slotState = slotStates.get(entry.sourceId());
        if (slotState != null) {
            slotState.extract(entry.entryKey().slotIndex(), extractedCount);
        }
    }

    private static LinkedHashSet<ItemIdentity> identitySet(InventorySourceSnapshot snapshot) {
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        for (InventoryEntrySnapshot entry : snapshot.entries()) {
            if (entry != null && entry.present()) {
                ItemIdentity identity = identityFromStack(entry.stack());
                if (identity != null) {
                    identities.add(identity);
                }
            }
        }
        return identities;
    }

    private static ItemIdentity identityFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        String itemId = invokeStringMethod(stack, "itemId");
        if (itemId != null && !itemId.isBlank()) {
            String componentFingerprint = invokeStringMethod(stack, "componentFingerprint");
            return componentFingerprint == null || componentFingerprint.isBlank()
                    ? ItemIdentity.of(itemId)
                    : ItemIdentity.exact(itemId, componentFingerprint);
        }
        return ItemIdentityMatcher.create(stack);
    }

    private static String invokeStringMethod(ItemStack stack, String methodName) {
        try {
            Method method = stack.getClass().getMethod(methodName);
            Object value = method.invoke(stack);
            return value instanceof String text ? text : null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    private static final class SlotBackedSourceState {
        private final List<SlotState> slots;

        private SlotBackedSourceState(InventorySourceSnapshot snapshot) {
            int slotCapacity = snapshot == null ? 0 : Math.max(snapshot.slotCapacity(), highestSlot(snapshot) + 1);
            ArrayList<SlotState> slotStates = new ArrayList<>(slotCapacity);
            for (int index = 0; index < slotCapacity; index++) {
                slotStates.add(new SlotState(ItemStack.EMPTY, 0));
            }
            if (snapshot != null) {
                for (InventoryEntrySnapshot entry : snapshot.entries()) {
                    if (entry == null || !entry.slotBacked() || !entry.present()) {
                        continue;
                    }
                    int slotIndex = entry.slotIndex();
                    while (slotIndex >= slotStates.size()) {
                        slotStates.add(new SlotState(ItemStack.EMPTY, 0));
                    }
                    ItemStack stack = entry.stack().copy();
                    stack.setCount(Math.min(Math.max(1, entry.count()), stack.getMaxStackSize()));
                    slotStates.set(slotIndex, new SlotState(stack, entry.count()));
                }
            }
            this.slots = slotStates;
        }

        private boolean contains(ItemIdentity identity) {
            for (SlotState slot : slots) {
                ItemIdentity slotIdentity = identityFromStack(slot.stack());
                if (slot.present() && slotIdentity != null && ItemIdentityMatcher.matchesMovable(slotIdentity, identity)) {
                    return true;
                }
            }
            return false;
        }

        private int accept(ItemIdentity identity, ItemStack template, int requestedCount) {
            int remaining = Math.max(0, requestedCount);
            if (remaining <= 0) {
                return 0;
            }

            for (SlotState slot : slots) {
                if (remaining <= 0) {
                    break;
                }
                ItemIdentity slotIdentity = identityFromStack(slot.stack());
                if (!slot.present() || slotIdentity == null || !ItemIdentityMatcher.matchesMovable(slotIdentity, identity)) {
                    continue;
                }
                int capacity = Math.max(0, slot.stack().getMaxStackSize() - slot.count());
                if (capacity <= 0) {
                    continue;
                }
                int inserted = Math.min(remaining, capacity);
                slot.count += inserted;
                remaining -= inserted;
            }

            for (SlotState slot : slots) {
                if (remaining <= 0) {
                    break;
                }
                if (slot.present()) {
                    continue;
                }
                int inserted = Math.min(remaining, Math.max(1, template.getMaxStackSize()));
                ItemStack placed = template.copy();
                placed.setCount(Math.min(inserted, placed.getMaxStackSize()));
                slot.stack = placed;
                slot.count = inserted;
                remaining -= inserted;
            }

            return Math.max(0, requestedCount - remaining);
        }

        private void extract(int slotIndex, int extractedCount) {
            if (slotIndex < 0 || slotIndex >= slots.size() || extractedCount <= 0) {
                return;
            }
            SlotState slot = slots.get(slotIndex);
            if (!slot.present()) {
                return;
            }
            slot.count = Math.max(0, slot.count - extractedCount);
            if (slot.count <= 0) {
                slot.stack = ItemStack.EMPTY;
                slot.count = 0;
            } else {
                slot.stack.setCount(Math.min(slot.count, slot.stack.getMaxStackSize()));
            }
        }

        private static int highestSlot(InventorySourceSnapshot snapshot) {
            if (snapshot == null) {
                return -1;
            }
            return snapshot.entries().stream()
                    .filter(entry -> entry != null && entry.slotBacked())
                    .mapToInt(InventoryEntrySnapshot::slotIndex)
                    .max()
                    .orElse(-1);
        }
    }

    private static final class SlotState {
        private ItemStack stack;
        private int count;

        private SlotState(ItemStack stack, int count) {
            this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
            this.count = Math.max(0, count);
        }

        private boolean present() {
            return !stack.isEmpty() && count > 0;
        }

        private ItemStack stack() {
            return stack;
        }

        private int count() {
            return count;
        }
    }
}
