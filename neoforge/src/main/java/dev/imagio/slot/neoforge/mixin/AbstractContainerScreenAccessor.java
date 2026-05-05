package dev.imagio.slot.neoforge.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes {@code AbstractContainerScreen.leftPos} so SLOT can shift
 * the vanilla container GUI right when the sidebar is mounted on its
 * left edge, and {@code findSlot} so the cross-surface drag bridge
 * can detect "release over a vanilla slot" without re-implementing
 * vanilla's slot hit-test math. See
 * {@code dev.imagio.slot.neoforge.client.screen.SlotContainerSidebar}
 * and the cross-surface drag listener.
 */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    void slot$setLeftPos(int leftPos);

    @Invoker("findSlot")
    @Nullable
    Slot slot$findSlot(double mouseX, double mouseY);
}
