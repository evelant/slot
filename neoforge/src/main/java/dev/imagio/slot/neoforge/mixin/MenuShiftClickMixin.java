package dev.imagio.slot.neoforge.mixin;

import dev.imagio.slot.neoforge.storage.BackpackReroute;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Backpack-first routing for shift-click. Whenever the player shift-
 * clicks a slot whose container is *not* the player's own inventory
 * (crafting result, crafting matrix, furnace output, chest slot,
 * machine slot, …), vanilla {@code quickMoveStack} dumps the items
 * into main / hotbar. We snapshot vanilla lanes around the click and
 * migrate any newly-arrived items into a registered backpack provider
 * via {@link BackpackReroute}, mirroring what {@link
 * dev.imagio.slot.neoforge.storage.SlotPickupRouter} does for world
 * pickups.
 *
 * <p>Why "source is not player inventory" rather than "any shift-click":
 * shift-clicks within the player's own inventory (main → hotbar, hotbar
 * → main, main → crafting matrix slots) are intentional placement by
 * the player. Auto-routing those would prevent them from ever using
 * shift-click to move items into hotbar. We only re-route items that
 * vanilla just pulled in from somewhere else.
 *
 * <p>Why per-slot diff rather than reading the source's "moved count":
 * non-result slots have no equivalent of {@code ResultSlot.removeCount}.
 * Diffing the player's own lanes is uniform across every menu and
 * source-slot type without per-class accessor mixins.
 *
 * <p>The thread-local snapshot is bounded: it's always cleared at HEAD
 * before any decision to set, so an exception in vanilla code that
 * skips the RETURN hook can't leak a stale snapshot into the next
 * click. The server thread runs menu logic single-threaded, and
 * {@code doClick} re-entry (quick-craft → PICKUP recursion) uses a
 * different ClickType, so our QUICK_MOVE gate skips it.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class MenuShiftClickMixin {

    @Shadow
    @Final
    public NonNullList<Slot> slots;

    @Unique
    private static final ThreadLocal<ItemStack[]> slot$preClickSnapshot = new ThreadLocal<>();

    @Inject(method = "clicked", at = @At("HEAD"))
    private void slot$snapshotBeforeShiftClick(int slotId, int dragType, ClickType clickType, Player player,
                                                CallbackInfo ci) {
        // Always clear first so an exception-skipped previous RETURN
        // can't bleed a stale snapshot into the current click.
        slot$preClickSnapshot.set(null);
        if (clickType != ClickType.QUICK_MOVE) {
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (slotId < 0 || slotId >= this.slots.size()) {
            return;
        }
        Slot sourceSlot = this.slots.get(slotId);
        if (sourceSlot.container == player.getInventory()) {
            // Player → player internal move; respect placement intent.
            return;
        }
        slot$preClickSnapshot.set(slot$captureVanillaLanes(serverPlayer));
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void slot$routeNewlyArrivedItems(int slotId, int dragType, ClickType clickType, Player player,
                                              CallbackInfo ci) {
        ItemStack[] before = slot$preClickSnapshot.get();
        if (before == null) {
            return;
        }
        slot$preClickSnapshot.set(null);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        Inventory inv = serverPlayer.getInventory();
        for (int i = 0; i < 36; i++) {
            slot$routeIfGrew(serverPlayer, before[i], inv.items.get(i));
        }
        slot$routeIfGrew(serverPlayer, before[36], inv.offhand.get(0));
    }

    @Unique
    private static ItemStack[] slot$captureVanillaLanes(ServerPlayer player) {
        Inventory inv = player.getInventory();
        ItemStack[] arr = new ItemStack[37];
        for (int i = 0; i < 36; i++) {
            arr[i] = inv.items.get(i).copy();
        }
        arr[36] = inv.offhand.get(0).copy();
        return arr;
    }

    @Unique
    private static void slot$routeIfGrew(ServerPlayer player, ItemStack before, ItemStack after) {
        if (after.isEmpty()) {
            return;
        }
        int delta;
        if (before.isEmpty() || !ItemStack.isSameItemSameComponents(before, after)) {
            // Slot was empty or held a different item — vanilla just
            // placed `after` here. Treat the entire stack as new.
            delta = after.getCount();
        } else {
            delta = after.getCount() - before.getCount();
        }
        if (delta > 0) {
            BackpackReroute.routeToBackpack(player, after, delta);
        }
    }
}
