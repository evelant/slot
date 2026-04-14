package dev.imagio.slot.compat.sophisticated;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackSupport.BackpackCarrierRef;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntUnaryOperator;

public final class SophisticatedBackpackTransferSupport {
    private static final ReflectionState REFLECTION = ReflectionState.load();

    private SophisticatedBackpackTransferSupport() {
    }

    public static boolean isAvailable() {
        return REFLECTION.available();
    }

    public static ItemStack insertIntoPlayerBackpacks(Player player, ItemStack stack, Map<UUID, CompoundTag> syncedContents) {
        if (player == null || stack == null || stack.isEmpty() || !REFLECTION.available()) {
            return stack;
        }

        ItemStack[] remainder = new ItemStack[]{stack};
        REFLECTION.runOnBackpacks(player, (carrierStack, handlerName, identifier, carrierSlotIndex) -> {
            Object wrapper = REFLECTION.fromStack(carrierStack);
            Object inventoryHandler = REFLECTION.inventoryHandler(wrapper);
            if (inventoryHandler == null) {
                return false;
            }

            remainder[0] = REFLECTION.insertIntoHandler(inventoryHandler, remainder[0]);
            REFLECTION.captureContents(wrapper, syncedContents);
            return remainder[0].isEmpty();
        });
        return remainder[0];
    }

    public static boolean canFullyInsertIntoPlayerBackpacks(Player player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return true;
        }
        if (player == null || !REFLECTION.available()) {
            return false;
        }

        ItemStack[] remainder = new ItemStack[]{stack.copy()};
        REFLECTION.runOnBackpacks(player, (carrierStack, handlerName, identifier, carrierSlotIndex) -> {
            Object wrapper = REFLECTION.fromStack(carrierStack);
            Object inventoryHandler = REFLECTION.inventoryHandler(wrapper);
            if (inventoryHandler == null) {
                return false;
            }

            remainder[0] = REFLECTION.simulateInsertIntoHandler(inventoryHandler, remainder[0]);
            return remainder[0].isEmpty();
        });
        return remainder[0].isEmpty();
    }

    public static ItemStack insertIntoBackpack(
            Player player,
            BackpackCarrierRef carrier,
            ItemStack stack,
            Map<UUID, CompoundTag> syncedContents
    ) {
        if (player == null || carrier == null || stack == null || stack.isEmpty() || !REFLECTION.available()) {
            return stack;
        }
        return withBackpack(player, carrier, stack, (wrapper, inventoryHandler) -> {
            ItemStack remainder = REFLECTION.insertIntoHandler(inventoryHandler, stack);
            REFLECTION.captureContents(wrapper, syncedContents);
            return remainder;
        });
    }

    public static boolean canFullyInsertIntoBackpack(Player player, BackpackCarrierRef carrier, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return true;
        }
        if (player == null || carrier == null || !REFLECTION.available()) {
            return false;
        }
        ItemStack remainder = withBackpack(player, carrier, stack.copy(), (wrapper, inventoryHandler) ->
                REFLECTION.simulateInsertIntoHandler(inventoryHandler, stack.copy()));
        return remainder.isEmpty();
    }

    public static ItemStack insertIntoBackpackSlot(
            Player player,
            BackpackCarrierRef carrier,
            int slotIndex,
            ItemStack stack,
            boolean simulate,
            Map<UUID, CompoundTag> syncedContents
    ) {
        if (player == null || carrier == null || stack == null || stack.isEmpty() || slotIndex < 0 || !REFLECTION.available()) {
            return stack;
        }
        return withBackpack(player, carrier, stack, (wrapper, inventoryHandler) -> {
            ItemStack remainder = REFLECTION.insertIntoSlot(inventoryHandler, slotIndex, stack, simulate);
            if (!simulate) {
                REFLECTION.captureContents(wrapper, syncedContents);
            }
            return remainder;
        });
    }

    public static boolean moveMatchingBackpackStackToMenu(
            Player player,
            ItemIdentity identity,
            int maxCount,
            AbstractContainerMenu menu,
            Iterable<Integer> targetMenuSlots,
            Map<UUID, CompoundTag> syncedContents
    ) {
        return moveMatchingBackpackStack(
                player,
                identity,
                maxCount,
                stack -> insertIntoMenuSlots(stack, menu, targetMenuSlots),
                syncedContents
        );
    }

    public static boolean containsMatchingBackpackStack(Player player, ItemIdentity identity) {
        return !copyFirstMatchingBackpackStack(player, identity).isEmpty();
    }

    public static ItemStack copyFirstMatchingBackpackStack(Player player, ItemIdentity identity) {
        if (player == null || identity == null || !REFLECTION.available()) {
            return ItemStack.EMPTY;
        }

        ItemStack[] match = new ItemStack[]{ItemStack.EMPTY};
        REFLECTION.runOnBackpacks(player, (carrierStack, handlerName, identifier, carrierSlotIndex) -> {
            Object wrapper = REFLECTION.fromStack(carrierStack);
            Object inventoryHandler = REFLECTION.inventoryHandler(wrapper);
            if (inventoryHandler == null) {
                return false;
            }

            int slotCount = REFLECTION.slotCount(inventoryHandler);
            for (int slot = 0; slot < slotCount; slot++) {
                ItemStack stack = REFLECTION.stackInSlot(inventoryHandler, slot);
                if (!ItemIdentityMatcher.matchesMovable(stack, identity)) {
                    continue;
                }

                match[0] = stack.copy();
                return true;
            }

            return false;
        });
        return match[0];
    }

    public static ItemStack copyFirstMatchingBackpackStack(Player player, BackpackCarrierRef carrier, ItemIdentity identity) {
        if (player == null || carrier == null || identity == null || !REFLECTION.available()) {
            return ItemStack.EMPTY;
        }
        return withBackpack(player, carrier, ItemStack.EMPTY, (wrapper, inventoryHandler) -> {
            int slotCount = REFLECTION.slotCount(inventoryHandler);
            for (int slot = 0; slot < slotCount; slot++) {
                ItemStack stack = REFLECTION.stackInSlot(inventoryHandler, slot);
                if (ItemIdentityMatcher.matchesMovable(stack, identity)) {
                    return stack.copy();
                }
            }
            return ItemStack.EMPTY;
        });
    }

    public static ItemStack previewBackpackSlot(Player player, BackpackCarrierRef carrier, int slotIndex) {
        if (player == null || carrier == null || slotIndex < 0 || !REFLECTION.available()) {
            return ItemStack.EMPTY;
        }
        return withBackpack(player, carrier, ItemStack.EMPTY, (wrapper, inventoryHandler) ->
                REFLECTION.stackInSlot(inventoryHandler, slotIndex).copy());
    }

    public static ItemStack extractBackpackSlot(
            Player player,
            BackpackCarrierRef carrier,
            int slotIndex,
            int amount,
            boolean simulate,
            Map<UUID, CompoundTag> syncedContents
    ) {
        if (player == null || carrier == null || slotIndex < 0 || amount <= 0 || !REFLECTION.available()) {
            return ItemStack.EMPTY;
        }
        return withBackpack(player, carrier, ItemStack.EMPTY, (wrapper, inventoryHandler) -> {
            ItemStack extracted = REFLECTION.extractFromHandler(inventoryHandler, slotIndex, amount, simulate);
            if (!simulate && !extracted.isEmpty()) {
                REFLECTION.captureContents(wrapper, syncedContents);
            }
            return extracted;
        });
    }

    public static boolean moveMatchingBackpackStack(
            Player player,
            ItemIdentity identity,
            int maxCount,
            BackpackInsertTarget insertTarget,
            Map<UUID, CompoundTag> syncedContents
    ) {
        if (player == null || identity == null || maxCount <= 0 || insertTarget == null || !REFLECTION.available()) {
            return false;
        }

        boolean[] movedAny = new boolean[]{false};
        int[] remainingBudget = new int[]{maxCount};
        REFLECTION.runOnBackpacks(player, (carrierStack, handlerName, identifier, carrierSlotIndex) -> {
            Object wrapper = REFLECTION.fromStack(carrierStack);
            Object inventoryHandler = REFLECTION.inventoryHandler(wrapper);
            if (inventoryHandler == null) {
                return false;
            }

            int slotCount = REFLECTION.slotCount(inventoryHandler);
            for (int slot = 0; slot < slotCount; slot++) {
                if (remainingBudget[0] <= 0) {
                    return true;
                }
                ItemStack stack = REFLECTION.stackInSlot(inventoryHandler, slot);
                if (!ItemIdentityMatcher.matchesMovable(stack, identity)) {
                    continue;
                }

                ItemStack extracted = REFLECTION.extractFromHandler(inventoryHandler, slot, Math.min(remainingBudget[0], stack.getCount()));
                if (extracted.isEmpty()) {
                    continue;
                }

                int extractedCount = extracted.getCount();
                ItemStack remainder = insertTarget.insert(extracted);
                int insertedCount = extractedCount - remainder.getCount();
                if (insertedCount > 0) {
                    movedAny[0] = true;
                    remainingBudget[0] -= insertedCount;
                }
                if (!remainder.isEmpty()) {
                    remainder = REFLECTION.insertIntoHandler(inventoryHandler, remainder);
                    if (!remainder.isEmpty()) {
                        SlotDebugLog.log("Backpack transfer could not fully restore remainder for identity={} remaining={}", identity.itemId(), remainder.getCount());
                    }
                }

                REFLECTION.captureContents(wrapper, syncedContents);
                if (insertedCount <= 0) {
                    return true;
                }
                if (remainingBudget[0] <= 0) {
                    return true;
                }
            }

            return false;
        });
        return movedAny[0];
    }

    public static boolean moveMatchingBackpackStack(
            Player player,
            BackpackCarrierRef carrier,
            ItemIdentity identity,
            int maxCount,
            BackpackInsertTarget insertTarget,
            Map<UUID, CompoundTag> syncedContents
    ) {
        if (player == null || carrier == null || identity == null || maxCount <= 0 || insertTarget == null || !REFLECTION.available()) {
            return false;
        }

        return withBackpack(player, carrier, false, (wrapper, inventoryHandler) -> {
            int remainingBudget = maxCount;
            int slotCount = REFLECTION.slotCount(inventoryHandler);
            boolean movedAny = false;
            for (int slot = 0; slot < slotCount; slot++) {
                if (remainingBudget <= 0) {
                    break;
                }
                ItemStack stack = REFLECTION.stackInSlot(inventoryHandler, slot);
                if (!ItemIdentityMatcher.matchesMovable(stack, identity)) {
                    continue;
                }

                ItemStack extracted = REFLECTION.extractFromHandler(inventoryHandler, slot, Math.min(remainingBudget, stack.getCount()), false);
                if (extracted.isEmpty()) {
                    continue;
                }

                int extractedCount = extracted.getCount();
                ItemStack remainder = insertTarget.insert(extracted);
                int insertedCount = extractedCount - remainder.getCount();
                if (insertedCount > 0) {
                    movedAny = true;
                    remainingBudget -= insertedCount;
                }
                if (!remainder.isEmpty()) {
                    remainder = REFLECTION.insertIntoHandler(inventoryHandler, remainder);
                    if (!remainder.isEmpty()) {
                        SlotDebugLog.log("Backpack transfer could not fully restore remainder for identity={} remaining={}", identity.itemId(), remainder.getCount());
                    }
                }
                REFLECTION.captureContents(wrapper, syncedContents);
                if (insertedCount <= 0) {
                    break;
                }
            }
            return movedAny;
        });
    }

    public static int moveFirstMatchingBackpackStack(
            Player player,
            ItemIdentity identity,
            IntUnaryOperator requestedCountForSourceStack,
            BackpackInsertTarget insertTarget,
            Map<UUID, CompoundTag> syncedContents
    ) {
        if (player == null || identity == null || requestedCountForSourceStack == null || insertTarget == null || !REFLECTION.available()) {
            return 0;
        }

        int[] movedCount = new int[]{0};
        REFLECTION.runOnBackpacks(player, (carrierStack, handlerName, identifier, carrierSlotIndex) -> {
            Object wrapper = REFLECTION.fromStack(carrierStack);
            Object inventoryHandler = REFLECTION.inventoryHandler(wrapper);
            if (inventoryHandler == null) {
                return false;
            }

            int slotCount = REFLECTION.slotCount(inventoryHandler);
            for (int slot = 0; slot < slotCount; slot++) {
                ItemStack stack = REFLECTION.stackInSlot(inventoryHandler, slot);
                if (!ItemIdentityMatcher.matchesMovable(stack, identity)) {
                    continue;
                }

                int requestedCount = Math.max(1, Math.min(stack.getCount(), requestedCountForSourceStack.applyAsInt(stack.getCount())));
                ItemStack extracted = REFLECTION.extractFromHandler(inventoryHandler, slot, requestedCount);
                if (extracted.isEmpty()) {
                    continue;
                }

                int extractedCount = extracted.getCount();
                ItemStack remainder = insertTarget.insert(extracted);
                movedCount[0] = Math.max(0, extractedCount - remainder.getCount());
                if (!remainder.isEmpty()) {
                    remainder = REFLECTION.insertIntoHandler(inventoryHandler, remainder);
                    if (!remainder.isEmpty()) {
                        SlotDebugLog.log("Backpack transfer could not fully restore remainder for identity={} remaining={}", identity.itemId(), remainder.getCount());
                    }
                }

                REFLECTION.captureContents(wrapper, syncedContents);
                return true;
            }

            return false;
        });
        return movedCount[0];
    }

    public static int moveBackpackSlot(
            Player player,
            BackpackCarrierRef carrier,
            int slotIndex,
            IntUnaryOperator requestedCountForSourceStack,
            BackpackInsertTarget insertTarget,
            Map<UUID, CompoundTag> syncedContents
    ) {
        if (player == null || carrier == null || slotIndex < 0 || requestedCountForSourceStack == null || insertTarget == null || !REFLECTION.available()) {
            return 0;
        }
        return withBackpack(player, carrier, 0, (wrapper, inventoryHandler) -> {
            ItemStack stack = REFLECTION.stackInSlot(inventoryHandler, slotIndex);
            if (stack.isEmpty()) {
                return 0;
            }
            int requestedCount = Math.max(1, Math.min(stack.getCount(), requestedCountForSourceStack.applyAsInt(stack.getCount())));
            ItemStack extracted = REFLECTION.extractFromHandler(inventoryHandler, slotIndex, requestedCount, false);
            if (extracted.isEmpty()) {
                return 0;
            }

            int extractedCount = extracted.getCount();
            ItemStack remainder = insertTarget.insert(extracted);
            int movedCount = Math.max(0, extractedCount - remainder.getCount());
            if (!remainder.isEmpty()) {
                remainder = REFLECTION.insertIntoHandler(inventoryHandler, remainder);
                if (!remainder.isEmpty()) {
                    SlotDebugLog.log("Backpack transfer could not fully restore remainder from slot={} remaining={}", slotIndex, remainder.getCount());
                }
            }
            REFLECTION.captureContents(wrapper, syncedContents);
            return movedCount;
        });
    }

    public static void applyClientContents(UUID backpackUuid, CompoundTag backpackContents) {
        if (backpackUuid == null || backpackContents == null || !REFLECTION.available()) {
            return;
        }
        REFLECTION.applyClientContents(backpackUuid, backpackContents);
    }

    public static Map<UUID, CompoundTag> capturePlayerBackpackContents(Player player) {
        if (player == null || !REFLECTION.available()) {
            return Map.of();
        }

        Map<UUID, CompoundTag> syncedContents = new LinkedHashMap<>();
        REFLECTION.runOnBackpacks(player, (carrierStack, handlerName, identifier, carrierSlotIndex) -> {
            Object wrapper = REFLECTION.fromStack(carrierStack);
            REFLECTION.captureContents(wrapper, syncedContents);
            return false;
        });
        return Map.copyOf(syncedContents);
    }

    private static ItemStack insertIntoMenuSlots(ItemStack stack, AbstractContainerMenu menu, Iterable<Integer> targetMenuSlots) {
        ItemStack remainder = stack;
        for (int menuSlot : targetMenuSlots) {
            if (remainder.isEmpty()) {
                continue;
            }
            Slot slot = safeMenuSlot(menu, menuSlot);
            if (slot == null) {
                continue;
            }
            remainder = slot.safeInsert(remainder);
        }
        return remainder;
    }

    private static Slot safeMenuSlot(AbstractContainerMenu menu, int slotId) {
        if (menu == null || slotId < 0) {
            return null;
        }
        try {
            return menu.getSlot(slotId);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T withBackpack(
            Player player,
            BackpackCarrierRef carrier,
            T fallback,
            BackpackOperation<T> operation
    ) {
        if (player == null || carrier == null || operation == null || !REFLECTION.available()) {
            return fallback;
        }
        Object[] result = new Object[]{fallback};
        boolean[] matched = new boolean[]{false};
        REFLECTION.runOnBackpacks(player, (carrierStack, handlerName, identifier, carrierSlotIndex) -> {
            BackpackCarrierRef candidate = new BackpackCarrierRef(handlerName, identifier, carrierSlotIndex);
            if (!carrier.matches(candidate)) {
                return false;
            }
            Object wrapper = REFLECTION.fromStack(carrierStack);
            Object inventoryHandler = REFLECTION.inventoryHandler(wrapper);
            if (inventoryHandler == null) {
                return false;
            }
            matched[0] = true;
            result[0] = operation.apply(wrapper, inventoryHandler);
            return true;
        });
        return matched[0] ? (T) result[0] : fallback;
    }

    private interface BackpackConsumer {
        boolean accept(ItemStack carrierStack, String handlerName, String identifier, int carrierSlotIndex);
    }

    @FunctionalInterface
    private interface BackpackOperation<T> {
        T apply(Object wrapper, Object inventoryHandler);
    }

    @FunctionalInterface
    public interface BackpackInsertTarget {
        ItemStack insert(ItemStack stack);
    }

    private record ReflectionState(
            boolean available,
            Class<?> backpackInventorySlotConsumerClass,
            Object playerInventoryProvider,
            Object backpackStorage,
            Method runOnBackpacksMethod,
            Method fromStackMethod,
            Method getInventoryHandlerMethod,
            Method getSlotsMethod,
            Method getStackInSlotMethod,
            Method extractItemMethod,
            Method insertItemMethod,
            Method getContentsUuidMethod,
            Method getOrCreateBackpackContentsMethod,
            Method setBackpackContentsMethod
    ) {
        private static ReflectionState load() {
            try {
                ClassLoader loader = SophisticatedBackpackTransferSupport.class.getClassLoader();
                Class<?> playerInventoryProviderClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider", false, loader);
                Class<?> backpackInventorySlotConsumerClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider$BackpackInventorySlotConsumer", false, loader);
                Class<?> backpackWrapperClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper", false, loader);
                Class<?> inventoryHandlerClass = Class.forName("net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler", false, loader);
                Class<?> backpackStorageClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage", false, loader);

                Object playerInventoryProvider = playerInventoryProviderClass.getMethod("get").invoke(null);
                Object backpackStorage = backpackStorageClass.getMethod("get").invoke(null);

                return new ReflectionState(
                        true,
                        backpackInventorySlotConsumerClass,
                        playerInventoryProvider,
                        backpackStorage,
                        playerInventoryProviderClass.getMethod("runOnBackpacks", Player.class, backpackInventorySlotConsumerClass),
                        backpackWrapperClass.getMethod("fromStack", ItemStack.class),
                        backpackWrapperClass.getMethod("getInventoryHandler"),
                        inventoryHandlerClass.getMethod("getSlots"),
                        inventoryHandlerClass.getMethod("getStackInSlot", int.class),
                        inventoryHandlerClass.getMethod("extractItem", int.class, int.class, boolean.class),
                        inventoryHandlerClass.getMethod("insertItem", int.class, ItemStack.class, boolean.class),
                        backpackWrapperClass.getMethod("getContentsUuid"),
                        backpackStorageClass.getMethod("getOrCreateBackpackContents", UUID.class),
                        backpackStorageClass.getMethod("setBackpackContents", UUID.class, CompoundTag.class)
                );
            } catch (ReflectiveOperationException ignored) {
                return new ReflectionState(false, null, null, null, null, null, null, null, null, null, null, null, null, null);
            }
        }

        private void runOnBackpacks(Player player, BackpackConsumer consumer) {
            if (!available || player == null || consumer == null) {
                return;
            }
            try {
                Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                        backpackInventorySlotConsumerClass.getClassLoader(),
                        new Class<?>[]{backpackInventorySlotConsumerClass},
                        (ignoredProxy, method, args) -> {
                            if (!"accept".equals(method.getName()) || args == null || args.length != 4) {
                                return false;
                            }
                            ItemStack carrierStack = args[0] instanceof ItemStack itemStack ? itemStack : ItemStack.EMPTY;
                            String handlerName = args[1] instanceof String value ? value : "";
                            String identifier = args[2] instanceof String value ? value : "";
                            int carrierSlotIndex = args[3] instanceof Integer value ? value : -1;
                            return consumer.accept(carrierStack, handlerName, identifier, carrierSlotIndex);
                        }
                );
                runOnBackpacksMethod.invoke(playerInventoryProvider, player, proxy);
            } catch (ReflectiveOperationException ignored) {
            }
        }

        private Object fromStack(ItemStack stack) {
            if (!available || stack == null || stack.isEmpty()) {
                return null;
            }
            try {
                return fromStackMethod.invoke(null, stack);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }

        private Object inventoryHandler(Object wrapper) {
            if (!available || wrapper == null) {
                return null;
            }
            try {
                return getInventoryHandlerMethod.invoke(wrapper);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }

        private int slotCount(Object inventoryHandler) {
            if (!available || inventoryHandler == null) {
                return 0;
            }
            try {
                Object slots = getSlotsMethod.invoke(inventoryHandler);
                return slots instanceof Integer value ? value : 0;
            } catch (ReflectiveOperationException ignored) {
                return 0;
            }
        }

        private ItemStack stackInSlot(Object inventoryHandler, int slot) {
            if (!available || inventoryHandler == null) {
                return ItemStack.EMPTY;
            }
            try {
                Object stack = getStackInSlotMethod.invoke(inventoryHandler, slot);
                return stack instanceof ItemStack itemStack ? itemStack : ItemStack.EMPTY;
            } catch (ReflectiveOperationException ignored) {
                return ItemStack.EMPTY;
            }
        }

        private ItemStack extractFromHandler(Object inventoryHandler, int slot, int amount) {
            return extractFromHandler(inventoryHandler, slot, amount, false);
        }

        private ItemStack extractFromHandler(Object inventoryHandler, int slot, int amount, boolean simulate) {
            if (!available || inventoryHandler == null || amount <= 0) {
                return ItemStack.EMPTY;
            }
            try {
                Object stack = extractItemMethod.invoke(inventoryHandler, slot, amount, simulate);
                return stack instanceof ItemStack itemStack ? itemStack : ItemStack.EMPTY;
            } catch (ReflectiveOperationException ignored) {
                return ItemStack.EMPTY;
            }
        }

        private ItemStack insertIntoHandler(Object inventoryHandler, ItemStack stack) {
            return insertIntoHandler(inventoryHandler, stack, false);
        }

        private ItemStack insertIntoSlot(Object inventoryHandler, int slotIndex, ItemStack stack, boolean simulate) {
            if (!available || inventoryHandler == null || stack == null || stack.isEmpty() || slotIndex < 0) {
                return stack;
            }
            try {
                Object result = insertItemMethod.invoke(inventoryHandler, slotIndex, stack, simulate);
                return result instanceof ItemStack itemStack ? itemStack : stack;
            } catch (ReflectiveOperationException ignored) {
                return stack;
            }
        }

        private ItemStack simulateInsertIntoHandler(Object inventoryHandler, ItemStack stack) {
            return insertIntoHandler(inventoryHandler, stack, true);
        }

        private ItemStack insertIntoHandler(Object inventoryHandler, ItemStack stack, boolean simulate) {
            if (!available || inventoryHandler == null || stack == null || stack.isEmpty()) {
                return stack;
            }

            ItemStack remainder = stack;
            int slots = slotCount(inventoryHandler);
            for (int slot = 0; slot < slots; slot++) {
                if (remainder.isEmpty()) {
                    break;
                }
                try {
                    Object result = insertItemMethod.invoke(inventoryHandler, slot, remainder, simulate);
                    remainder = result instanceof ItemStack itemStack ? itemStack : remainder;
                } catch (ReflectiveOperationException ignored) {
                    return remainder;
                }
            }
            return remainder;
        }

        @SuppressWarnings("unchecked")
        private void captureContents(Object wrapper, Map<UUID, CompoundTag> syncedContents) {
            if (!available || wrapper == null || syncedContents == null) {
                return;
            }
            try {
                Object result = getContentsUuidMethod.invoke(wrapper);
                if (!(result instanceof Optional<?> optional)) {
                    return;
                }
                Object uuid = optional.orElse(null);
                if (!(uuid instanceof UUID backpackUuid)) {
                    return;
                }
                Object contents = getOrCreateBackpackContentsMethod.invoke(backpackStorage, backpackUuid);
                if (contents instanceof CompoundTag compoundTag) {
                    syncedContents.put(backpackUuid, compoundTag.copy());
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }

        private void applyClientContents(UUID backpackUuid, CompoundTag backpackContents) {
            if (!available || backpackUuid == null || backpackContents == null) {
                return;
            }
            try {
                setBackpackContentsMethod.invoke(backpackStorage, backpackUuid, backpackContents.copy());
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }
}
