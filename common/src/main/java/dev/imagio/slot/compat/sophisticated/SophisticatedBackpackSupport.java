package dev.imagio.slot.compat.sophisticated;

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

    public static List<BackpackSlotStack> readPlayerBackpackStacks(Player player) {
        return readPlayerBackpackStacks(player, null);
    }

    public static int countPlayerBackpackSlots(Player player) {
        return countPlayerBackpackSlots(player, null);
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

    public static void refreshClientBackpackContents(Player player, UUID backpackUuid) {
        if (player == null || backpackUuid == null || !REFLECTION.available()) {
            return;
        }
        REFLECTION.refreshClientBackpackContents(player, backpackUuid);
    }

    /**
     * Locate the carrier {@link ItemStack} for a given stable container id.
     * Iterates the same SB-provided handler list that
     * {@link #readPlayerBackpacks} walks, so it finds backpacks regardless
     * of which slot category (main, hotbar, chestslot, curios) they live in
     * — main-inventory-only lookups via {@code inv.items[carrierSlotIndex]}
     * silently miss worn or curios-equipped backpacks, leaving extract /
     * insert paths broken for them.
     */
    public static ItemStack findCarrierByStableId(Player player, String stableContainerId) {
        if (player == null || stableContainerId == null || stableContainerId.isBlank()
                || !REFLECTION.available()) {
            return ItemStack.EMPTY;
        }
        ItemStack[] match = {ItemStack.EMPTY};
        REFLECTION.runOnBackpacks(player, (carrierStack, handlerName, identifier, backpackSlotIndex) -> {
            if (carrierStack == null || carrierStack.isEmpty()) {
                return false;
            }
            BackpackCarrierRef ref = new BackpackCarrierRef(handlerName, identifier, backpackSlotIndex);
            String stable = REFLECTION.contentsUuid(REFLECTION.fromStack(carrierStack))
                    .map(UUID::toString)
                    .orElseGet(ref::stableFallbackId);
            if (stableContainerId.equals(stable)) {
                match[0] = carrierStack;
                return true;
            }
            return false;
        });
        return match[0];
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
            Object backpackWrapperCapability,
            Method itemStackGetCapabilityMethod,
            Method lazyOptionalResolveMethod,
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
                Class<?> itemStackHandlerClass = firstClass(
                        loader,
                        "net.neoforged.neoforge.items.ItemStackHandler",
                        "net.minecraftforge.items.ItemStackHandler"
                );
                Class<?> playerInventoryProviderClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider", false, loader);
                Class<?> backpackInventorySlotConsumerClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider$BackpackInventorySlotConsumer", false, loader);
                Object playerInventoryProvider = playerInventoryProviderClass.getMethod("get").invoke(null);
                Method runOnBackpacksMethod = playerInventoryProviderClass.getMethod("runOnBackpacks", net.minecraft.world.entity.player.Player.class, backpackInventorySlotConsumerClass);
                Method fromStackMethod = methodOrNull(backpackWrapperClass, "fromStack", ItemStack.class);
                Object backpackWrapperCapability = null;
                Method itemStackGetCapabilityMethod = null;
                Method lazyOptionalResolveMethod = null;
                if (fromStackMethod == null) {
                    Class<?> capabilityWrapperClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper", false, loader);
                    Class<?> capabilityClass = Class.forName("net.minecraftforge.common.capabilities.Capability", false, loader);
                    Class<?> lazyOptionalClass = Class.forName("net.minecraftforge.common.util.LazyOptional", false, loader);
                    backpackWrapperCapability = capabilityWrapperClass.getMethod("getCapabilityInstance").invoke(null);
                    itemStackGetCapabilityMethod = ItemStack.class.getMethod("getCapability", capabilityClass, net.minecraft.core.Direction.class);
                    lazyOptionalResolveMethod = lazyOptionalClass.getMethod("resolve");
                }
                Method getInventoryHandlerMethod = storageWrapperClass.getMethod("getInventoryHandler");
                Method getSlotsMethod = itemStackHandlerClass.getMethod("getSlots");
                Method getStackInSlotMethod = itemStackHandlerClass.getMethod("getStackInSlot", int.class);
                Method getContentsUuidMethod = backpackWrapperClass.getMethod("getContentsUuid");
                Method onContentsNbtUpdatedMethod = backpackWrapperClass.getMethod("onContentsNbtUpdated");

                Class<?> backpackContainerClass = null;
                Method getBackpackContextMethod = null;
                Method getBackpackSlotIndexMethod = null;
                Field handlerNameField = null;
                Field identifierField = null;
                try {
                    backpackContainerClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer", false, loader);
                    Class<?> backpackContextItemClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext$Item", false, loader);
                    getBackpackContextMethod = backpackContainerClass.getMethod("getBackpackContext");
                    getBackpackSlotIndexMethod = backpackContextItemClass.getMethod("getBackpackSlotIndex");
                    handlerNameField = backpackContextItemClass.getDeclaredField("handlerName");
                    handlerNameField.setAccessible(true);
                    identifierField = backpackContextItemClass.getDeclaredField("identifier");
                    identifierField.setAccessible(true);
                } catch (ReflectiveOperationException | LinkageError ignored) {
                    // Opened-backpack menu detection is optional. Carried
                    // inventory discovery/routing must keep working across
                    // SB loader/version UI package drift.
                }
                return new ReflectionState(
                        true,
                        backpackItemClass,
                        backpackContainerClass,
                        playerInventoryProvider,
                        backpackInventorySlotConsumerClass,
                        runOnBackpacksMethod,
                        fromStackMethod,
                        backpackWrapperCapability,
                        itemStackGetCapabilityMethod,
                        lazyOptionalResolveMethod,
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
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return new ReflectionState(false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
            }
        }

        private static Class<?> firstClass(ClassLoader loader, String... classNames) throws ClassNotFoundException {
            ClassNotFoundException last = null;
            for (String className : classNames) {
                try {
                    return Class.forName(className, false, loader);
                } catch (ClassNotFoundException exception) {
                    last = exception;
                }
            }
            throw last == null ? new ClassNotFoundException("missing class candidates") : last;
        }

        private static Method methodOrNull(Class<?> owner, String name, Class<?>... parameterTypes) {
            try {
                return owner.getMethod(name, parameterTypes);
            } catch (NoSuchMethodException ignored) {
                return null;
            }
        }

        private boolean isBackpackItem(ItemStack stack) {
            return available && backpackItemClass != null && backpackItemClass.isInstance(stack.getItem());
        }

        private Object fromStack(ItemStack stack) {
            if (!available || stack == null || stack.isEmpty()) {
                return null;
            }
            try {
                if (fromStackMethod != null) {
                    return fromStackMethod.invoke(null, stack);
                }
                if (itemStackGetCapabilityMethod == null
                        || backpackWrapperCapability == null
                        || lazyOptionalResolveMethod == null) {
                    return null;
                }
                Object lazyOptional = itemStackGetCapabilityMethod.invoke(stack, backpackWrapperCapability, null);
                Object resolved = lazyOptionalResolveMethod.invoke(lazyOptional);
                if (resolved instanceof Optional<?> optional) {
                    return optional.orElse(null);
                }
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return null;
            }
            return null;
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

        private void refreshClientBackpackContents(Player player, UUID backpackUuid) {
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
            if (!available
                    || menu == null
                    || backpackContainerClass == null
                    || getBackpackContextMethod == null
                    || getBackpackSlotIndexMethod == null
                    || handlerNameField == null
                    || identifierField == null
                    || !backpackContainerClass.isInstance(menu)) {
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
