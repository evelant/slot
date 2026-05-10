package dev.imagio.slot.forge.compat.sacks;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class SacksNSuchSupport {
    static final String PREFIX = "sns:container";

    private static final int STABLE_ORDER_BASE = 51;
    private static final ReflectionState REFLECTION = ReflectionState.load();

    private SacksNSuchSupport() {
    }

    static boolean isAvailable() {
        return REFLECTION.available();
    }

    static boolean isContainerItem(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && REFLECTION.isContainerItem(stack)
                && itemHandler(stack) != null;
    }

    static List<ContainerSnapshot> readPlayerContainers(Player player) {
        if (player == null || !isAvailable()) {
            return List.of();
        }

        ItemStack openContainer = REFLECTION.openedContainerStack(player.containerMenu);
        ArrayList<Carrier> carriers = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            addCarrier(player, new Carrier(CarrierKind.INVENTORY, slot, ""), inventory.items.get(slot), openContainer, carriers);
        }
        for (int slot = 0; slot < inventory.armor.size(); slot++) {
            addCarrier(player, new Carrier(CarrierKind.ARMOR, slot, ""), inventory.armor.get(slot), openContainer, carriers);
        }
        if (!inventory.offhand.isEmpty()) {
            addCarrier(player, new Carrier(CarrierKind.OFFHAND, 0, ""), inventory.offhand.get(0), openContainer, carriers);
        }
        REFLECTION.addCuriosCarriers(player, openContainer, carriers);

        ArrayList<ContainerSnapshot> snapshots = new ArrayList<>(carriers.size());
        int ordinal = 0;
        for (Carrier carrier : carriers) {
            ItemStack carrierStack = carrier.stack(player);
            IItemHandler handler = itemHandler(carrierStack);
            if (handler == null || handler.getSlots() <= 0) {
                continue;
            }
            snapshots.add(new ContainerSnapshot(
                    carrier,
                    carrier.sourceId(),
                    player,
                    carrierStack,
                    handler,
                    handler.getSlots(),
                    carrierStack.getHoverName().copy(),
                    STABLE_ORDER_BASE + ordinal
            ));
            ordinal++;
        }
        return List.copyOf(snapshots);
    }

    static ContainerSnapshot find(Player player, String sourceId) {
        if (player == null || sourceId == null || sourceId.isBlank()) {
            return null;
        }
        Carrier carrier = Carrier.parse(sourceId);
        if (carrier == null) {
            return null;
        }
        ItemStack carrierStack = carrier.stack(player);
        IItemHandler handler = itemHandler(carrierStack);
        if (!isContainerItem(carrierStack) || handler == null) {
            return null;
        }
        int stableOrder = STABLE_ORDER_BASE;
        for (ContainerSnapshot snapshot : readPlayerContainers(player)) {
            if (snapshot.sourceId().equals(sourceId)) {
                stableOrder = snapshot.stableOrder();
                break;
            }
        }
        return new ContainerSnapshot(
                carrier,
                carrier.sourceId(),
                player,
                carrierStack,
                handler,
                handler.getSlots(),
                carrierStack.getHoverName().copy(),
                stableOrder
        );
    }

    static ItemStack insertBestFit(Player player, ItemStack stack, boolean simulate) {
        if (player == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        List<ContainerSnapshot> containers = readPlayerContainers(player);
        if (containers.isEmpty()) {
            return stack;
        }

        ItemStack remaining = stack.copy();
        remaining = insertIntoMatchingSlots(containers, remaining, simulate);
        if (remaining.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ArrayList<ContainerSnapshot> emptySlotOrder = new ArrayList<>(containers);
        emptySlotOrder.sort(Comparator
                .comparingInt((ContainerSnapshot snapshot) -> emptySlotPriority(snapshot.carrierStack()))
                .thenComparingInt(ContainerSnapshot::stableOrder));
        return insertIntoEmptySlots(emptySlotOrder, remaining, simulate);
    }

    static ItemStack insertInto(ContainerSnapshot snapshot, ItemStack stack, boolean simulate) {
        if (snapshot == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        ItemStack remaining = ItemHandlerHelper.insertItemStacked(snapshot.handler(), stack.copy(), simulate);
        if (remaining == null) {
            remaining = ItemStack.EMPTY;
        }
        markChanged(snapshot, stack.getCount(), remaining.getCount(), simulate);
        return remaining;
    }

    static ItemStack extract(ContainerSnapshot snapshot, int slotIndex, int amount, boolean simulate) {
        if (snapshot == null || slotIndex < 0 || slotIndex >= snapshot.slotCount() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = snapshot.handler().extractItem(slotIndex, amount, simulate);
        if (extracted == null) {
            extracted = ItemStack.EMPTY;
        }
        markChanged(snapshot, extracted.getCount(), 0, simulate);
        return extracted;
    }

    static ItemStack peek(ContainerSnapshot snapshot, int slotIndex) {
        if (snapshot == null || slotIndex < 0 || slotIndex >= snapshot.slotCount()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = snapshot.handler().getStackInSlot(slotIndex);
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    private static ItemStack insertIntoMatchingSlots(List<ContainerSnapshot> snapshots, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack;
        for (ContainerSnapshot snapshot : snapshots) {
            for (int slot = 0; slot < snapshot.slotCount() && !remaining.isEmpty(); slot++) {
                ItemStack existing = snapshot.handler().getStackInSlot(slot);
                if (!ItemHandlerHelper.canItemStacksStack(existing, remaining)) {
                    continue;
                }
                int before = remaining.getCount();
                remaining = snapshot.handler().insertItem(slot, remaining, simulate);
                if (remaining == null) {
                    remaining = ItemStack.EMPTY;
                }
                markChanged(snapshot, before, remaining.getCount(), simulate);
            }
        }
        return remaining;
    }

    private static ItemStack insertIntoEmptySlots(List<ContainerSnapshot> snapshots, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack;
        for (ContainerSnapshot snapshot : snapshots) {
            for (int slot = 0; slot < snapshot.slotCount() && !remaining.isEmpty(); slot++) {
                if (!snapshot.handler().getStackInSlot(slot).isEmpty()) {
                    continue;
                }
                int before = remaining.getCount();
                remaining = snapshot.handler().insertItem(slot, remaining, simulate);
                if (remaining == null) {
                    remaining = ItemStack.EMPTY;
                }
                markChanged(snapshot, before, remaining.getCount(), simulate);
            }
        }
        return remaining;
    }

    private static int emptySlotPriority(ItemStack carrierStack) {
        String itemId = itemId(carrierStack);
        return switch (itemId) {
            case "sns:seed_pouch", "sns:ore_sack", "sns:lunchbox", "sns:quiver" -> 0;
            case "sns:straw_basket", "sns:leather_sack", "sns:burlap_sack" -> 1;
            case "sns:frame_pack" -> 2;
            default -> 3;
        };
    }

    private static String itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        return stack.getItem().builtInRegistryHolder().key().location().toString();
    }

    private static void addCarrier(
            Player player,
            Carrier carrier,
            ItemStack stack,
            ItemStack openContainer,
            List<Carrier> out
    ) {
        if (!isContainerItem(stack)) {
            return;
        }
        if (openContainer != null && !openContainer.isEmpty() && stack == openContainer) {
            return;
        }
        out.add(carrier);
    }

    private static IItemHandler itemHandler(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return stack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
    }

    private static void markChanged(ContainerSnapshot snapshot, int beforeCount, int afterCount, boolean simulate) {
        if (simulate || snapshot == null || beforeCount <= afterCount) {
            return;
        }
        Player player = snapshot.owner();
        if (player != null) {
            player.getInventory().setChanged();
            if (player.containerMenu != null) {
                player.containerMenu.broadcastChanges();
            }
        }
    }

    enum CarrierKind {
        INVENTORY("inventory"),
        ARMOR("armor"),
        OFFHAND("offhand"),
        CURIOS("curios");

        final String token;

        CarrierKind(String token) {
            this.token = token;
        }

        static CarrierKind fromToken(String token) {
            for (CarrierKind kind : values()) {
                if (kind.token.equals(token)) {
                    return kind;
                }
            }
            return null;
        }
    }

    record Carrier(CarrierKind kind, int slotIndex, String identifier) {
        Carrier {
            identifier = identifier == null ? "" : identifier;
        }

        String sourceId() {
            if (kind == CarrierKind.CURIOS) {
                return PREFIX + "/" + kind.token + "/" + encode(identifier) + "/" + slotIndex;
            }
            return PREFIX + "/" + kind.token + "/" + slotIndex;
        }

        ItemStack stack(Player player) {
            if (player == null || kind == null || slotIndex < 0) {
                return ItemStack.EMPTY;
            }
            Inventory inventory = player.getInventory();
            return switch (kind) {
                case INVENTORY -> slotIndex < inventory.items.size()
                        ? inventory.items.get(slotIndex)
                        : ItemStack.EMPTY;
                case ARMOR -> slotIndex < inventory.armor.size()
                        ? inventory.armor.get(slotIndex)
                        : ItemStack.EMPTY;
                case OFFHAND -> slotIndex == 0 && !inventory.offhand.isEmpty()
                        ? inventory.offhand.get(0)
                        : ItemStack.EMPTY;
                case CURIOS -> REFLECTION.curiosStack(player, identifier, slotIndex);
            };
        }

        static Carrier parse(String sourceId) {
            if (sourceId == null || !sourceId.startsWith(PREFIX + "/")) {
                return null;
            }
            String suffix = sourceId.substring(PREFIX.length() + 1);
            String[] parts = suffix.split("/");
            if (parts.length < 2) {
                return null;
            }
            CarrierKind kind = CarrierKind.fromToken(parts[0]);
            if (kind == null) {
                return null;
            }
            if (kind == CarrierKind.CURIOS) {
                if (parts.length != 3) {
                    return null;
                }
                Integer slot = parseInt(parts[2]);
                return slot == null ? null : new Carrier(kind, slot, decode(parts[1]));
            }
            if (parts.length != 2) {
                return null;
            }
            Integer slot = parseInt(parts[1]);
            return slot == null ? null : new Carrier(kind, slot, "");
        }
    }

    record ContainerSnapshot(
            Carrier carrier,
            String sourceId,
            Player owner,
            ItemStack carrierStack,
            IItemHandler handler,
            int slotCount,
            Component label,
            int stableOrder
    ) {
    }

    private static Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String encode(String raw) {
        return raw.replace("%", "%25").replace("/", "%2F");
    }

    private static String decode(String encoded) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < encoded.length(); i++) {
            char c = encoded.charAt(i);
            if (c == '%' && i + 2 < encoded.length()) {
                String code = encoded.substring(i + 1, i + 3);
                if ("2F".equalsIgnoreCase(code)) {
                    out.append('/');
                    i += 2;
                    continue;
                }
                if ("25".equalsIgnoreCase(code)) {
                    out.append('%');
                    i += 2;
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    private record ReflectionState(
            Class<?> containerItemClass,
            Class<?> containerItemMenuClass,
            Method getContainerStackMethod,
            Method getCuriosInventoryMethod,
            Method getCuriosMethod,
            Method getStacksMethod,
            Method getStackInSlotMethod,
            Method getSlotsMethod
    ) {
        static ReflectionState load() {
            try {
                ClassLoader loader = SacksNSuchSupport.class.getClassLoader();
                Class<?> containerItemClass =
                        Class.forName("mod.traister101.sns.common.items.ContainerItem", false, loader);
                Class<?> menuClass = null;
                Method getContainerStack = null;
                try {
                    menuClass = Class.forName("mod.traister101.sns.common.menu.ContainerItemMenu", false, loader);
                    getContainerStack = menuClass.getMethod("getContainerStack");
                } catch (ReflectiveOperationException | LinkageError ignored) {
                    // The carried-provider path does not require the Sacks menu class.
                }

                Method getCuriosInventory = null;
                Method getCurios = null;
                Method getStacks = null;
                Method getStackInSlot = null;
                Method getSlots = null;
                try {
                    Class<?> curiosApi = Class.forName("top.theillusivec4.curios.api.CuriosApi", false, loader);
                    Class<?> curiosItemHandler =
                            Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler", false, loader);
                    Class<?> curioStacksHandler =
                            Class.forName("top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler", false, loader);
                    Class<?> dynamicStackHandler =
                            Class.forName("top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler", false, loader);
                    getCuriosInventory = curiosApi.getMethod("getCuriosInventory", LivingEntity.class);
                    getCurios = curiosItemHandler.getMethod("getCurios");
                    getStacks = curioStacksHandler.getMethod("getStacks");
                    getStackInSlot = dynamicStackHandler.getMethod("getStackInSlot", int.class);
                    getSlots = dynamicStackHandler.getMethod("getSlots");
                } catch (ReflectiveOperationException | LinkageError ignored) {
                    // Curios is optional; inventory/offhand sacks still work without it.
                }

                return new ReflectionState(
                        containerItemClass,
                        menuClass,
                        getContainerStack,
                        getCuriosInventory,
                        getCurios,
                        getStacks,
                        getStackInSlot,
                        getSlots
                );
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return new ReflectionState(null, null, null, null, null, null, null, null);
            }
        }

        boolean available() {
            return containerItemClass != null;
        }

        boolean isContainerItem(ItemStack stack) {
            return available() && stack != null && !stack.isEmpty() && containerItemClass.isInstance(stack.getItem());
        }

        ItemStack openedContainerStack(AbstractContainerMenu menu) {
            if (menu == null
                    || containerItemMenuClass == null
                    || getContainerStackMethod == null
                    || !containerItemMenuClass.isInstance(menu)) {
                return ItemStack.EMPTY;
            }
            try {
                Object stack = getContainerStackMethod.invoke(menu);
                return stack instanceof ItemStack itemStack ? itemStack : ItemStack.EMPTY;
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return ItemStack.EMPTY;
            }
        }

        void addCuriosCarriers(Player player, ItemStack openContainer, List<Carrier> out) {
            Map<String, Object> curios = curiosHandlers(player);
            if (curios.isEmpty()) {
                return;
            }
            for (Map.Entry<String, Object> entry : curios.entrySet()) {
                Object stacks = stacksHandler(entry.getValue());
                int slots = slotCount(stacks);
                for (int slot = 0; slot < slots; slot++) {
                    ItemStack stack = stackInSlot(stacks, slot);
                    addCarrier(player, new Carrier(CarrierKind.CURIOS, slot, entry.getKey()), stack, openContainer, out);
                }
            }
        }

        ItemStack curiosStack(Player player, String identifier, int slotIndex) {
            if (player == null || identifier == null || slotIndex < 0) {
                return ItemStack.EMPTY;
            }
            Object handler = curiosHandlers(player).get(identifier);
            if (handler == null) {
                return ItemStack.EMPTY;
            }
            return stackInSlot(stacksHandler(handler), slotIndex);
        }

        @SuppressWarnings("unchecked")
        private Map<String, Object> curiosHandlers(Player player) {
            if (player == null || getCuriosInventoryMethod == null || getCuriosMethod == null) {
                return Map.of();
            }
            try {
                Object lazy = getCuriosInventoryMethod.invoke(null, player);
                if (!(lazy instanceof LazyOptional<?> optional)) {
                    return Map.of();
                }
                Optional<?> resolved = optional.resolve();
                if (resolved.isEmpty()) {
                    return Map.of();
                }
                Object curios = getCuriosMethod.invoke(resolved.get());
                if (!(curios instanceof Map<?, ?> map)) {
                    return Map.of();
                }
                LinkedHashMap<String, Object> out = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() instanceof String key && entry.getValue() != null) {
                        out.put(key, entry.getValue());
                    }
                }
                return out;
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return Map.of();
            }
        }

        private Object stacksHandler(Object curioStacksHandler) {
            if (curioStacksHandler == null || getStacksMethod == null) {
                return null;
            }
            try {
                return getStacksMethod.invoke(curioStacksHandler);
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return null;
            }
        }

        private int slotCount(Object stacksHandler) {
            if (stacksHandler == null || getSlotsMethod == null) {
                return 0;
            }
            try {
                Object slots = getSlotsMethod.invoke(stacksHandler);
                return slots instanceof Integer count ? Math.max(0, count) : 0;
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return 0;
            }
        }

        private ItemStack stackInSlot(Object stacksHandler, int slotIndex) {
            if (stacksHandler == null || getStackInSlotMethod == null || slotIndex < 0) {
                return ItemStack.EMPTY;
            }
            try {
                Object stack = getStackInSlotMethod.invoke(stacksHandler, slotIndex);
                return stack instanceof ItemStack itemStack ? itemStack : ItemStack.EMPTY;
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return ItemStack.EMPTY;
            }
        }
    }
}
