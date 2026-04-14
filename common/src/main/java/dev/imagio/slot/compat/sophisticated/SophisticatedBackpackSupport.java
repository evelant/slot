package dev.imagio.slot.compat.sophisticated;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SophisticatedBackpackSupport {
    private static final ReflectionState REFLECTION = ReflectionState.load();

    private SophisticatedBackpackSupport() {
    }

    public static boolean isAvailable() {
        return REFLECTION.available();
    }

    public static boolean isBackpackItem(ItemStack stack) {
        return stack != null && !stack.isEmpty() && REFLECTION.isBackpackItem(stack);
    }

    public static List<BackpackSlotStack> readPlayerBackpackStacks(LocalPlayer player) {
        return readPlayerBackpackStacks(player, null);
    }

    public static List<BackpackSlotStack> readPlayerBackpackStacks(Player player) {
        return readPlayerBackpackStacks(player, null);
    }

    public static int countPlayerBackpackSlots(LocalPlayer player) {
        return countPlayerBackpackSlots(player, null);
    }

    public static int countPlayerBackpackSlots(Player player) {
        return countPlayerBackpackSlots(player, null);
    }

    public static int countPlayerBackpackSlots(LocalPlayer player, BackpackCarrierRef excludedCarrier) {
        return countPlayerBackpackSlots((Player) player, excludedCarrier);
    }

    public static int countPlayerBackpackSlots(Player player, BackpackCarrierRef excludedCarrier) {
        if (player == null || !REFLECTION.available()) {
            return 0;
        }

        int[] totalSlots = {0};
        REFLECTION.runOnBackpacks(player, (carrierStack, handlerName, identifier, backpackSlotIndex) -> {
            BackpackCarrierRef carrier = new BackpackCarrierRef(handlerName, identifier, backpackSlotIndex);
            if (excludedCarrier != null && excludedCarrier.matches(carrier)) {
                return false;
            }

            totalSlots[0] += backpackSlotCount(carrierStack);
            return false;
        });
        return totalSlots[0];
    }

    public static List<BackpackSlotStack> readPlayerBackpackStacks(LocalPlayer player, BackpackCarrierRef excludedCarrier) {
        return readPlayerBackpackStacks((Player) player, excludedCarrier);
    }

    public static List<BackpackSlotStack> readPlayerBackpackStacks(Player player, BackpackCarrierRef excludedCarrier) {
        if (player == null || !REFLECTION.available()) {
            return List.of();
        }

        List<BackpackSlotStack> stacks = new ArrayList<>();
        REFLECTION.runOnBackpacks(player, (carrierStack, handlerName, identifier, backpackSlotIndex) -> {
            BackpackCarrierRef carrier = new BackpackCarrierRef(handlerName, identifier, backpackSlotIndex);
            if (excludedCarrier != null && excludedCarrier.matches(carrier)) {
                return false;
            }

            addBackpackStacks(carrierStack, carrier, stacks);
            return false;
        });
        return List.copyOf(stacks);
    }

    public static List<BackpackInventorySnapshot> readPlayerBackpacks(LocalPlayer player, BackpackCarrierRef excludedCarrier) {
        return readPlayerBackpacks((Player) player, excludedCarrier);
    }

    public static List<BackpackInventorySnapshot> readPlayerBackpacks(Player player, BackpackCarrierRef excludedCarrier) {
        if (player == null || !REFLECTION.available()) {
            return List.of();
        }

        List<BackpackInventorySnapshot> backpacks = new ArrayList<>();
        REFLECTION.runOnBackpacks(player, (carrierStack, handlerName, identifier, backpackSlotIndex) -> {
            BackpackCarrierRef carrier = new BackpackCarrierRef(handlerName, identifier, backpackSlotIndex);
            if (excludedCarrier != null && excludedCarrier.matches(carrier)) {
                return false;
            }

            BackpackInventorySnapshot snapshot = backpackSnapshot(carrierStack, carrier);
            if (snapshot != null) {
                backpacks.add(snapshot);
            }
            return false;
        });
        return List.copyOf(backpacks);
    }

    public static BackpackCarrierRef openedBackpackCarrier(AbstractContainerMenu menu) {
        if (menu == null || !REFLECTION.available()) {
            return null;
        }
        return REFLECTION.openedBackpackCarrier(menu);
    }

    public static void refreshClientBackpackContents(LocalPlayer player, UUID backpackUuid) {
        if (player == null || backpackUuid == null || !REFLECTION.available()) {
            return;
        }
        REFLECTION.refreshClientBackpackContents(player, backpackUuid);
    }

    private static int backpackSlotCount(ItemStack carrierStack) {
        if (carrierStack == null || carrierStack.isEmpty() || !REFLECTION.isBackpackItem(carrierStack)) {
            return 0;
        }

        Object wrapper = REFLECTION.fromStack(carrierStack);
        if (wrapper == null) {
            return 0;
        }

        Object inventoryHandler = REFLECTION.inventoryHandler(wrapper);
        if (inventoryHandler == null) {
            return 0;
        }

        return REFLECTION.slotCount(inventoryHandler);
    }

    private static BackpackInventorySnapshot backpackSnapshot(ItemStack carrierStack, BackpackCarrierRef carrier) {
        if (carrierStack == null || carrier == null || carrierStack.isEmpty() || !REFLECTION.isBackpackItem(carrierStack)) {
            return null;
        }
        Object wrapper = REFLECTION.fromStack(carrierStack);
        Object inventoryHandler = wrapper == null ? null : REFLECTION.inventoryHandler(wrapper);
        if (inventoryHandler == null) {
            return null;
        }

        int slotCount = REFLECTION.slotCount(inventoryHandler);
        ArrayList<BackpackEntry> entries = new ArrayList<>();
        for (int slot = 0; slot < slotCount; slot++) {
            ItemStack stack = REFLECTION.stackInSlot(inventoryHandler, slot);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            entries.add(new BackpackEntry(slot, stack.copy()));
        }

        String stableContainerId = REFLECTION.contentsUuid(wrapper)
                .map(UUID::toString)
                .orElseGet(carrier::stableFallbackId);
        return new BackpackInventorySnapshot(
                carrier,
                stableContainerId,
                slotCount,
                List.copyOf(entries)
        );
    }

    private static void addBackpackStacks(ItemStack carrierStack, BackpackCarrierRef carrier, List<BackpackSlotStack> stacks) {
        int slots = backpackSlotCount(carrierStack);
        if (slots <= 0) {
            return;
        }
        Object wrapper = REFLECTION.fromStack(carrierStack);
        Object inventoryHandler = wrapper == null ? null : REFLECTION.inventoryHandler(wrapper);
        if (inventoryHandler == null) {
            return;
        }
        int baseSyntheticSlot = carrier.syntheticBaseSlot();
        for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = REFLECTION.stackInSlot(inventoryHandler, slot);
            if (stack == null || stack.isEmpty()) {
                continue;
            }

            stacks.add(new BackpackSlotStack(carrier, baseSyntheticSlot + slot, stack.copy()));
        }
    }

    public record BackpackCarrierRef(String handlerName, String identifier, int carrierSlotIndex) {
        public BackpackCarrierRef {
            handlerName = handlerName == null ? "" : handlerName;
            identifier = identifier == null ? "" : identifier;
        }

        public boolean matches(BackpackCarrierRef other) {
            return other != null
                    && carrierSlotIndex == other.carrierSlotIndex
                    && handlerName.equals(other.handlerName)
                    && identifier.equals(other.identifier);
        }

        private int syntheticBaseSlot() {
            return Math.floorMod(Objects.hash(handlerName, identifier, carrierSlotIndex), 1_000_000) * 1000;
        }

        public String stableFallbackId() {
            return handlerName + "/" + identifier + "/" + carrierSlotIndex;
        }
    }

    public record BackpackSlotStack(BackpackCarrierRef carrier, int slotIndex, ItemStack stack) {
    }

    public record BackpackEntry(int slotIndex, ItemStack stack) {
    }

    public record BackpackInventorySnapshot(
            BackpackCarrierRef carrier,
            String stableContainerId,
            int slotCount,
            List<BackpackEntry> entries
    ) {
        public BackpackInventorySnapshot {
            stableContainerId = stableContainerId == null ? "" : stableContainerId;
            slotCount = Math.max(0, slotCount);
            entries = entries == null ? List.of() : List.copyOf(entries);
        }
    }

    private record ReflectionState(
            boolean available,
            Class<?> backpackItemClass,
            Class<?> backpackContainerClass,
            Object playerInventoryProvider,
            Class<?> backpackInventorySlotConsumerClass,
            Method runOnBackpacksMethod,
            Method fromStackMethod,
            Method getInventoryHandlerMethod,
            Method getSlotsMethod,
            Method getStackInSlotMethod,
            Method getBackpackContextMethod,
            Method getBackpackSlotIndexMethod,
            Method getContentsUuidMethod,
            Method onContentsNbtUpdatedMethod,
            Field handlerNameField,
            Field identifierField
    ) {
        private static ReflectionState load() {
            try {
                ClassLoader loader = SophisticatedBackpackSupport.class.getClassLoader();
                Class<?> backpackItemClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem", false, loader);
                Class<?> backpackWrapperClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper", false, loader);
                Class<?> storageWrapperClass = Class.forName("net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper", false, loader);
                Class<?> itemStackHandlerClass = Class.forName("net.neoforged.neoforge.items.ItemStackHandler", false, loader);
                Class<?> playerInventoryProviderClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider", false, loader);
                Class<?> backpackInventorySlotConsumerClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider$BackpackInventorySlotConsumer", false, loader);
                Class<?> backpackContainerClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer", false, loader);
                Class<?> backpackContextItemClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext$Item", false, loader);
                Object playerInventoryProvider = playerInventoryProviderClass.getMethod("get").invoke(null);
                Method runOnBackpacksMethod = playerInventoryProviderClass.getMethod("runOnBackpacks", net.minecraft.world.entity.player.Player.class, backpackInventorySlotConsumerClass);
                Method fromStackMethod = backpackWrapperClass.getMethod("fromStack", ItemStack.class);
                Method getInventoryHandlerMethod = storageWrapperClass.getMethod("getInventoryHandler");
                Method getSlotsMethod = itemStackHandlerClass.getMethod("getSlots");
                Method getStackInSlotMethod = itemStackHandlerClass.getMethod("getStackInSlot", int.class);
                Method getBackpackContextMethod = backpackContainerClass.getMethod("getBackpackContext");
                Method getBackpackSlotIndexMethod = backpackContextItemClass.getMethod("getBackpackSlotIndex");
                Method getContentsUuidMethod = backpackWrapperClass.getMethod("getContentsUuid");
                Method onContentsNbtUpdatedMethod = backpackWrapperClass.getMethod("onContentsNbtUpdated");
                Field handlerNameField = backpackContextItemClass.getDeclaredField("handlerName");
                handlerNameField.setAccessible(true);
                Field identifierField = backpackContextItemClass.getDeclaredField("identifier");
                identifierField.setAccessible(true);
                return new ReflectionState(
                        true,
                        backpackItemClass,
                        backpackContainerClass,
                        playerInventoryProvider,
                        backpackInventorySlotConsumerClass,
                        runOnBackpacksMethod,
                        fromStackMethod,
                        getInventoryHandlerMethod,
                        getSlotsMethod,
                        getStackInSlotMethod,
                        getBackpackContextMethod,
                        getBackpackSlotIndexMethod,
                        getContentsUuidMethod,
                        onContentsNbtUpdatedMethod,
                        handlerNameField,
                        identifierField
                );
            } catch (ReflectiveOperationException ignored) {
                return new ReflectionState(false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
            }
        }

        private boolean isBackpackItem(ItemStack stack) {
            return available && backpackItemClass != null && backpackItemClass.isInstance(stack.getItem());
        }

        private Object fromStack(ItemStack stack) {
            if (!available) {
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
                return slots instanceof Integer count ? count : 0;
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

        @SuppressWarnings("unchecked")
        private Optional<UUID> contentsUuid(Object wrapper) {
            if (!available || wrapper == null) {
                return Optional.empty();
            }
            try {
                Object result = getContentsUuidMethod.invoke(wrapper);
                if (result instanceof Optional<?> optional && optional.orElse(null) instanceof UUID uuid) {
                    return Optional.of(uuid);
                }
            } catch (ReflectiveOperationException ignored) {
            }
            return Optional.empty();
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

        private void refreshClientBackpackContents(LocalPlayer player, UUID backpackUuid) {
            runOnBackpacks(player, (carrierStack, handlerName, identifier, carrierSlotIndex) -> {
                Object wrapper = fromStack(carrierStack);
                if (wrapper == null) {
                    return false;
                }
                try {
                    Object result = getContentsUuidMethod.invoke(wrapper);
                    if (!(result instanceof Optional<?> optional)) {
                        return false;
                    }
                    Object uuid = optional.orElse(null);
                    if (!(uuid instanceof UUID contentsUuid) || !backpackUuid.equals(contentsUuid)) {
                        return false;
                    }
                    onContentsNbtUpdatedMethod.invoke(wrapper);
                    return true;
                } catch (ReflectiveOperationException ignored) {
                    return false;
                }
            });
        }

        private BackpackCarrierRef openedBackpackCarrier(AbstractContainerMenu menu) {
            if (!available || menu == null || backpackContainerClass == null || !backpackContainerClass.isInstance(menu)) {
                return null;
            }
            try {
                Object backpackContext = ReflectiveCompatInvoker.invokeInstanceIfCompatible(getBackpackContextMethod, backpackContainerClass, menu);
                if (backpackContext == null) {
                    return null;
                }
                String handlerName = handlerNameField.get(backpackContext) instanceof String value ? value : "";
                String identifier = identifierField.get(backpackContext) instanceof String value ? value : "";
                Object backpackSlotIndex = getBackpackSlotIndexMethod.invoke(backpackContext);
                if (!(backpackSlotIndex instanceof Integer slotIndex)) {
                    return null;
                }
                return new BackpackCarrierRef(handlerName, identifier, slotIndex);
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
                return null;
            }
        }
    }

    @FunctionalInterface
    private interface BackpackConsumer {
        boolean accept(ItemStack carrierStack, String handlerName, String identifier, int carrierSlotIndex);
    }
}
