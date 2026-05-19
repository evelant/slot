package dev.imagio.slot.forge.compat.toolbelt;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class ToolBeltSupport {
    static final String PREFIX = "toolbelt:carried";

    private static final ReflectionState REFLECTION = ReflectionState.load();

    private ToolBeltSupport() {
    }

    static boolean isAvailable() {
        return REFLECTION.available();
    }

    static boolean isToolBeltItem(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && REFLECTION.isToolBelt(stack)
                && REFLECTION.itemHandler(stack) != null;
    }

    static List<BeltSnapshot> readPlayerBelts(Player player) {
        if (player == null || !isAvailable()) {
            return List.of();
        }

        ArrayList<Carrier> carriers = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            addCarrier(new Carrier(CarrierKind.INVENTORY, slot, ""), inventory.items.get(slot), carriers);
        }
        for (int slot = 0; slot < inventory.armor.size(); slot++) {
            addCarrier(new Carrier(CarrierKind.ARMOR, slot, ""), inventory.armor.get(slot), carriers);
        }
        if (!inventory.offhand.isEmpty()) {
            addCarrier(new Carrier(CarrierKind.OFFHAND, 0, ""), inventory.offhand.get(0), carriers);
        }
        REFLECTION.addBeltSlotCarrier(player, carriers);
        REFLECTION.addCuriosCarriers(player, carriers);

        ArrayList<BeltSnapshot> snapshots = new ArrayList<>(carriers.size());
        int ordinal = 0;
        for (Carrier carrier : carriers) {
            ItemStack beltStack = carrier.stack(player);
            IItemHandler handler = REFLECTION.itemHandler(beltStack);
            if (!REFLECTION.isToolBelt(beltStack) || handler == null || handler.getSlots() <= 0) {
                continue;
            }
            snapshots.add(new BeltSnapshot(
                    carrier,
                    carrier.sourceId(),
                    player,
                    beltStack,
                    handler,
                    handler.getSlots(),
                    beltStack.getHoverName().copy(),
                    55 + ordinal
            ));
            ordinal++;
        }
        return List.copyOf(snapshots);
    }

    static BeltSnapshot find(Player player, String sourceId) {
        if (player == null || sourceId == null || sourceId.isBlank() || !isAvailable()) {
            return null;
        }
        Carrier carrier = Carrier.parse(sourceId);
        if (carrier == null) {
            return null;
        }
        ItemStack beltStack = carrier.stack(player);
        IItemHandler handler = REFLECTION.itemHandler(beltStack);
        if (!REFLECTION.isToolBelt(beltStack) || handler == null || handler.getSlots() <= 0) {
            return null;
        }
        int stableOrder = 55;
        for (BeltSnapshot snapshot : readPlayerBelts(player)) {
            if (snapshot.sourceId().equals(sourceId)) {
                stableOrder = snapshot.stableOrder();
                break;
            }
        }
        return new BeltSnapshot(
                carrier,
                carrier.sourceId(),
                player,
                beltStack,
                handler,
                handler.getSlots(),
                beltStack.getHoverName().copy(),
                stableOrder
        );
    }

    static ItemStack insertBestFit(Player player, ItemStack stack, boolean simulate) {
        if (player == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        List<BeltSnapshot> belts = readPlayerBelts(player);
        if (belts.isEmpty()) {
            return stack;
        }
        ItemStack remaining = stack.copy();
        remaining = insertIntoMatchingSlots(belts, remaining, simulate);
        if (remaining.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ArrayList<BeltSnapshot> emptySlotOrder = new ArrayList<>(belts);
        emptySlotOrder.sort(Comparator.comparingInt(BeltSnapshot::stableOrder));
        return insertIntoEmptySlots(emptySlotOrder, remaining, simulate);
    }

    static ItemStack insertInto(BeltSnapshot snapshot, ItemStack stack, boolean simulate) {
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

    static ItemStack extract(BeltSnapshot snapshot, int slotIndex, int amount, boolean simulate) {
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

    static ItemStack peek(BeltSnapshot snapshot, int slotIndex) {
        if (snapshot == null || slotIndex < 0 || slotIndex >= snapshot.slotCount()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = snapshot.handler().getStackInSlot(slotIndex);
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    private static ItemStack insertIntoMatchingSlots(List<BeltSnapshot> snapshots, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack;
        for (BeltSnapshot snapshot : snapshots) {
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

    private static ItemStack insertIntoEmptySlots(List<BeltSnapshot> snapshots, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack;
        for (BeltSnapshot snapshot : snapshots) {
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

    private static void addCarrier(Carrier carrier, ItemStack stack, List<Carrier> out) {
        if (REFLECTION.isToolBelt(stack) && REFLECTION.itemHandler(stack) != null) {
            out.add(carrier);
        }
    }

    private static void markChanged(BeltSnapshot snapshot, int beforeCount, int afterCount, boolean simulate) {
        if (simulate || snapshot == null || beforeCount <= afterCount) {
            return;
        }
        Player player = snapshot.owner();
        if (player != null) {
            player.getInventory().setChanged();
            snapshot.carrier().markChanged(player);
            if (player.containerMenu != null) {
                player.containerMenu.broadcastChanges();
            }
            REFLECTION.sendBeltSync(player);
        }
    }

    enum CarrierKind {
        INVENTORY("inventory"),
        ARMOR("armor"),
        OFFHAND("offhand"),
        BELT_SLOT("belt_slot"),
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
                case BELT_SLOT -> REFLECTION.beltSlotStack(player);
                case CURIOS -> REFLECTION.curiosStack(player, identifier, slotIndex);
            };
        }

        void markChanged(Player player) {
            if (kind == CarrierKind.BELT_SLOT) {
                REFLECTION.notifyBeltSlotChanged(player);
            }
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

    record BeltSnapshot(
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
            Class<?> toolBeltItemClass,
            Capability<IItemHandler> itemHandlerCapability,
            Method beltExtensionGetMethod,
            Method beltExtensionGetBeltMethod,
            Method extensionSlotGetContentsMethod,
            Method extensionSlotOnContentsChangedMethod,
            Method beltFinderSendSyncMethod,
            Method getCuriosInventoryMethod,
            Method getCuriosMethod,
            Method getStacksMethod,
            Method getStackInSlotMethod,
            Method getSlotsMethod
    ) {
        @SuppressWarnings("unchecked")
        static ReflectionState load() {
            try {
                ClassLoader loader = ToolBeltSupport.class.getClassLoader();
                Class<?> toolBeltItemClass =
                        Class.forName("dev.gigaherz.toolbelt.belt.ToolBeltItem", false, loader);
                Field itemHandlerField = toolBeltItemClass.getField("ITEM_HANDLER");
                Capability<IItemHandler> itemHandlerCapability =
                        (Capability<IItemHandler>) itemHandlerField.get(null);

                Class<?> beltExtensionSlotClass =
                        Class.forName("dev.gigaherz.toolbelt.slot.BeltExtensionSlot", false, loader);
                Method beltExtensionGet =
                        beltExtensionSlotClass.getMethod("get", LivingEntity.class);
                Method beltExtensionGetBelt = beltExtensionSlotClass.getMethod("getBelt");
                Class<?> extensionSlotClass =
                        Class.forName("dev.gigaherz.toolbelt.customslots.IExtensionSlot", false, loader);
                Method extensionSlotGetContents = extensionSlotClass.getMethod("getContents");
                Method extensionSlotOnContentsChanged = extensionSlotClass.getMethod("onContentsChanged");
                Class<?> beltFinderClass = Class.forName("dev.gigaherz.toolbelt.BeltFinder", false, loader);
                Method beltFinderSendSync = beltFinderClass.getMethod("sendSync", Player.class);

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
                    // ToolBelt's built-in belt slot and inventory-held belts work without Curios.
                }

                return new ReflectionState(
                        toolBeltItemClass,
                        itemHandlerCapability,
                        beltExtensionGet,
                        beltExtensionGetBelt,
                        extensionSlotGetContents,
                        extensionSlotOnContentsChanged,
                        beltFinderSendSync,
                        getCuriosInventory,
                        getCurios,
                        getStacks,
                        getStackInSlot,
                        getSlots
                );
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return new ReflectionState(null, null, null, null, null, null, null, null, null, null, null, null);
            }
        }

        boolean available() {
            return toolBeltItemClass != null && itemHandlerCapability != null;
        }

        boolean isToolBelt(ItemStack stack) {
            return available()
                    && stack != null
                    && !stack.isEmpty()
                    && toolBeltItemClass.isInstance(stack.getItem());
        }

        IItemHandler itemHandler(ItemStack stack) {
            if (!isToolBelt(stack)) {
                return null;
            }
            try {
                LazyOptional<IItemHandler> optional = stack.getCapability(itemHandlerCapability, null);
                return optional.resolve().orElse(null);
            } catch (RuntimeException | LinkageError ignored) {
                return null;
            }
        }

        void addBeltSlotCarrier(Player player, List<Carrier> out) {
            ItemStack stack = beltSlotStack(player);
            addCarrier(new Carrier(CarrierKind.BELT_SLOT, 0, ""), stack, out);
        }

        ItemStack beltSlotStack(Player player) {
            Object slot = beltSlot(player);
            if (slot == null || extensionSlotGetContentsMethod == null) {
                return ItemStack.EMPTY;
            }
            try {
                Object stack = extensionSlotGetContentsMethod.invoke(slot);
                return stack instanceof ItemStack itemStack ? itemStack : ItemStack.EMPTY;
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return ItemStack.EMPTY;
            }
        }

        void notifyBeltSlotChanged(Player player) {
            Object slot = beltSlot(player);
            if (slot == null || extensionSlotOnContentsChangedMethod == null) {
                return;
            }
            try {
                extensionSlotOnContentsChangedMethod.invoke(slot);
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            }
        }

        void sendBeltSync(Player player) {
            if (player == null || beltFinderSendSyncMethod == null) {
                return;
            }
            try {
                beltFinderSendSyncMethod.invoke(null, player);
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            }
        }

        void addCuriosCarriers(Player player, List<Carrier> out) {
            Map<String, Object> curios = curiosHandlers(player);
            if (curios.isEmpty()) {
                return;
            }
            for (Map.Entry<String, Object> entry : curios.entrySet()) {
                Object stacks = stacksHandler(entry.getValue());
                int slots = slotCount(stacks);
                for (int slot = 0; slot < slots; slot++) {
                    ItemStack stack = stackInSlot(stacks, slot);
                    addCarrier(new Carrier(CarrierKind.CURIOS, slot, entry.getKey()), stack, out);
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

        private Object beltSlot(Player player) {
            if (player == null || beltExtensionGetMethod == null || beltExtensionGetBeltMethod == null) {
                return null;
            }
            try {
                Object lazy = beltExtensionGetMethod.invoke(null, player);
                if (!(lazy instanceof LazyOptional<?> optional)) {
                    return null;
                }
                Optional<?> resolved = optional.resolve();
                if (resolved.isEmpty()) {
                    return null;
                }
                return beltExtensionGetBeltMethod.invoke(resolved.get());
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return null;
            }
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
