package dev.imagio.slot.neoforge.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes {@code findSlot} so the cross-surface drag bridge can detect
 * "release over a vanilla slot" without re-implementing vanilla's slot
 * hit-test math. See the cross-surface drag listener in
 * {@code dev.imagio.slot.neoforge.client.screen.SlotContainerSidebar}.
 */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Invoker("findSlot")
    @Nullable
    Slot slot$findSlot(double mouseX, double mouseY);
}
