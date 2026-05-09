package dev.imagio.slot.forge.mixin;

import dev.imagio.slot.inventory.storage.BackpackReroute;
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

@Mixin(AbstractContainerMenu.class)
public abstract class MenuShiftClickMixin {
    @Shadow
    @Final
    public NonNullList<Slot> slots;

    @Unique
    private static final ThreadLocal<ItemStack[]> slot$preClickSnapshot = new ThreadLocal<>();

    @Inject(method = "clicked", at = @At("HEAD"))
    private void slot$snapshotBeforeShiftClick(
            int slotId,
            int dragType,
            ClickType clickType,
            Player player,
            CallbackInfo callback
    ) {
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
            return;
        }
        slot$preClickSnapshot.set(slot$captureVanillaLanes(serverPlayer));
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void slot$routeNewlyArrivedItems(
            int slotId,
            int dragType,
            ClickType clickType,
            Player player,
            CallbackInfo callback
    ) {
        ItemStack[] before = slot$preClickSnapshot.get();
        if (before == null) {
            return;
        }
        slot$preClickSnapshot.set(null);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        Inventory inventory = serverPlayer.getInventory();
        for (int index = 0; index < 36; index++) {
            slot$routeIfGrew(serverPlayer, before[index], inventory.items.get(index));
        }
        slot$routeIfGrew(serverPlayer, before[36], inventory.offhand.get(0));
    }

    @Unique
    private static ItemStack[] slot$captureVanillaLanes(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        ItemStack[] snapshot = new ItemStack[37];
        for (int index = 0; index < 36; index++) {
            snapshot[index] = inventory.items.get(index).copy();
        }
        snapshot[36] = inventory.offhand.get(0).copy();
        return snapshot;
    }

    @Unique
    private static void slot$routeIfGrew(ServerPlayer player, ItemStack before, ItemStack after) {
        if (after == null || after.isEmpty()) {
            return;
        }
        int delta;
        if (before == null || before.isEmpty() || !ItemStack.isSameItemSameTags(before, after)) {
            delta = after.getCount();
        } else {
            delta = after.getCount() - before.getCount();
        }
        if (delta > 0) {
            BackpackReroute.routeToBackpack(player, after, delta);
        }
    }
}
