package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackSupport;
import dev.imagio.slot.inventory.storage.CarriedProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link CarriedProvider} for Sophisticated Backpacks. Source ids have the
 * form {@code sophisticatedbackpacks:carried/<stableContainerId>}; the stable
 * id is the backpack's content UUID (or a deterministic fallback for
 * backpacks without one).
 *
 * <p>Operations go through the carrier stack's
 * {@code Capabilities.ItemHandler.ITEM} — the same path the core refactor
 * used, just isolated here so adding a second provider doesn't touch the
 * platform dispatcher.
 */
public final class SophisticatedBackpacksCarriedProvider implements CarriedProvider {

    public static final String PREFIX = "sophisticatedbackpacks:carried";

    @Override
    public String prefix() {
        return PREFIX;
    }

    /**
     * SB ships a bespoke {@link dev.imagio.slot.inventory.integration.SophisticatedBackpackInventoryIntegrationProvider}
     * that already emits carried source descriptors + handles {@code mutate()} — skip the
     * auto-synthesised extension or we'd double-register sources.
     */
    @Override
    public boolean autoSynthesizeExtension() {
        return false;
    }

    @Override
    public List<String> sourceIds(Player player) {
        if (player == null || !SophisticatedBackpackSupport.isAvailable()) {
            return List.of();
        }
        ArrayList<String> ids = new ArrayList<>();
        try {
            for (var snapshot : SophisticatedBackpackSupport.readPlayerBackpacks(player, null)) {
                if (snapshot == null) {
                    continue;
                }
                String stable = snapshot.stableContainerId();
                if (stable == null || stable.isBlank()) {
                    continue;
                }
                ids.add(PREFIX + "/" + stable);
            }
        } catch (RuntimeException | LinkageError ignored) {
        }
        return List.copyOf(ids);
    }

    @Override
    public ItemStack peek(Player player, String sourceId, int slotIndex) {
        IItemHandler handler = handlerFor(player, sourceId);
        if (handler == null || slotIndex < 0 || slotIndex >= handler.getSlots()) {
            return ItemStack.EMPTY;
        }
        try {
            ItemStack s = handler.getStackInSlot(slotIndex);
            return s == null ? ItemStack.EMPTY : s;
        } catch (RuntimeException | LinkageError ignored) {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack extract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate) {
        IItemHandler handler = handlerFor(player, sourceId);
        if (handler == null || slotIndex < 0 || slotIndex >= handler.getSlots() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        try {
            ItemStack taken = handler.extractItem(slotIndex, amount, simulate);
            return taken == null ? ItemStack.EMPTY : taken;
        } catch (RuntimeException | LinkageError ignored) {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack insert(ServerPlayer player, String sourceId, ItemStack stack, boolean simulate) {
        if (stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        IItemHandler handler = handlerFor(player, sourceId);
        if (handler == null) {
            return stack;
        }
        ItemStack remainder = stack.copy();
        int slots = handler.getSlots();
        for (int slot = 0; slot < slots && !remainder.isEmpty(); slot++) {
            try {
                remainder = handler.insertItem(slot, remainder, simulate);
                if (remainder == null) {
                    return ItemStack.EMPTY;
                }
            } catch (RuntimeException | LinkageError ignored) {
                return remainder;
            }
        }
        return remainder;
    }

    @Override
    public int slotCount(Player player, String sourceId) {
        IItemHandler handler = handlerFor(player, sourceId);
        if (handler == null) {
            return 0;
        }
        try {
            return handler.getSlots();
        } catch (RuntimeException | LinkageError ignored) {
            return 0;
        }
    }

    private static IItemHandler handlerFor(Player player, String sourceId) {
        if (player == null || sourceId == null || sourceId.isBlank()) {
            return null;
        }
        String suffix = stableIdSuffix(sourceId);
        if (suffix.isEmpty()) {
            return null;
        }
        // Resolve the carrier through SB's own provider, which walks every
        // slot category (main, hotbar, chest-slot, curios). The previous
        // code looked up {@code inv.items[carrierSlotIndex]} which only
        // works for main-inventory backpacks: a worn (chest-slot) backpack
        // returned the wrong stack and {@code getCapability} was null,
        // leaving the items visible-but-immovable.
        try {
            ItemStack carrier = SophisticatedBackpackSupport.findCarrierByStableId(player, suffix);
            if (carrier == null || carrier.isEmpty()) {
                return null;
            }
            IItemHandler handler = carrier.getCapability(Capabilities.ItemHandler.ITEM);
            return handler;
        } catch (RuntimeException | LinkageError ignored) {
        }
        return null;
    }

    private static String stableIdSuffix(String sourceId) {
        int slash = sourceId.indexOf('/', PREFIX.length());
        if (slash < 0 || slash + 1 >= sourceId.length()) {
            return "";
        }
        return sourceId.substring(slash + 1);
    }
}
