package dev.imagio.slot.forge.probe;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.ItemStackEquivalence;
import dev.imagio.slot.platform.SlotResourceAccess;
import dev.imagio.slot.platform.SlotStackAccess;
import dev.imagio.slot.ui.action.WorkspaceActionPacketBuffer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class Forge120SharedCompileProbe {
    private static final SlotResourceAccess.ResourceAccess RESOURCE_ACCESS = new Forge120ResourceAccess();
    private static final SlotStackAccess.StackAccess STACK_ACCESS = new Forge120StackAccess();

    private Forge120SharedCompileProbe() {
    }

    public static ResourceLocation id(String path) {
        SlotResourceAccess.install(RESOURCE_ACCESS);
        return SlotResourceAccess.current().id("slot", path);
    }

    public static ItemIdentity identity(ItemStack stack) {
        SlotStackAccess.install(STACK_ACCESS);
        return ItemIdentityMatcher.create(stack);
    }

    public static boolean sameItemAndData(ItemStack first, ItemStack second) {
        SlotStackAccess.install(STACK_ACCESS);
        return ItemStackEquivalence.sameItemAndData(first, second);
    }

    public static WorkspaceActionPacketBuffer actionPacketBuffer(FriendlyByteBuf buffer) {
        return new Forge120WorkspaceActionPacketBuffer(buffer);
    }
}
