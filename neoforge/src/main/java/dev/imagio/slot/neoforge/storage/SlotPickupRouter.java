package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackTransferSupport;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

// Server-side pickup interceptor. Runs after vanilla pickup finishes — items
// have landed in the main inventory, which is where we want them briefly:
// vanilla pickup animation/sounds fire, quests and advancements trigger,
// pickup-tooltip mods see the event. We then transfer as much as fits into
// the player's carried Sophisticated Backpacks so the main inventory doesn't
// fill up. The Sophisticated Backpacks magnet upgrade intercepts items
// pre-pickup (bypassing those sibling side-effects), which is a usability
// loss we explicitly avoid here.
public final class SlotPickupRouter {
    private static boolean registered;

    private SlotPickupRouter() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(SlotPickupRouter::onItemPickupPost);
        registered = true;
    }

    private static void onItemPickupPost(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (!SophisticatedBackpackTransferSupport.isAvailable()) {
            return;
        }

        ItemStack original = event.getOriginalStack();
        ItemStack leftOver = event.getCurrentStack();
        int pickedCount = original.getCount() - (leftOver == null ? 0 : leftOver.getCount());
        if (pickedCount <= 0 || original.isEmpty()) {
            return;
        }

        ItemStack toRoute = original.copy();
        toRoute.setCount(pickedCount);

        Map<UUID, CompoundTag> syncedContents = new LinkedHashMap<>();
        ItemStack remainder;
        try {
            remainder = SophisticatedBackpackTransferSupport.insertIntoPlayerBackpacks(player, toRoute, syncedContents);
        } catch (RuntimeException failure) {
            SlotDebugLog.log(
                    "SlotPickupRouter insertIntoPlayerBackpacks threw {}; leaving pickup in main inventory",
                    failure.toString()
            );
            return;
        }
        int absorbed = pickedCount - remainder.getCount();
        if (absorbed <= 0) {
            return;
        }

        shrinkMatchingStacks(player.getInventory(), original, absorbed);
    }

    // Remove `count` items matching `pattern` from the player's inventory.
    // We don't track which slot vanilla pickup landed into — pickup may merge
    // across multiple existing stacks — so iterate and shrink matching stacks
    // until the absorbed count is accounted for. Hotbar/main/offhand are
    // covered by the default Inventory.getContainerSize() iteration.
    private static void shrinkMatchingStacks(Inventory inventory, ItemStack pattern, int count) {
        int remaining = count;
        int size = inventory.getContainerSize();
        for (int slot = 0; slot < size && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack, pattern)) {
                continue;
            }
            int take = Math.min(stack.getCount(), remaining);
            stack.shrink(take);
            remaining -= take;
        }
        inventory.setChanged();
    }
}
