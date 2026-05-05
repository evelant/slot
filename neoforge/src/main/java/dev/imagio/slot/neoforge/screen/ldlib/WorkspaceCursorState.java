package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Client-side accessors for the real menu cursor — the vanilla
 * {@code AbstractContainerMenu#getCarried()} stack on
 * {@code Minecraft.getInstance().player.containerMenu}. Used by wall
 * card click handlers to gate "is the cursor empty" decisions and by
 * the active-chrome tick subscription to reflect cursor identity in
 * the wall card chrome.
 */
final class WorkspaceCursorState {
    private WorkspaceCursorState() {
    }

    static ItemStack carriedStack() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return ItemStack.EMPTY;
        }
        AbstractContainerMenu menu = mc.player.containerMenu;
        if (menu == null) {
            return ItemStack.EMPTY;
        }
        return menu.getCarried();
    }

    static boolean isCarrying() {
        return !carriedStack().isEmpty();
    }

    static SlotWorkspaceViewModel.IdentityRef carriedIdentity() {
        ItemStack stack = carriedStack();
        if (stack.isEmpty()) {
            return null;
        }
        return SlotWorkspaceViewModel.IdentityRef.from(ItemIdentityMatcher.create(stack));
    }
}
