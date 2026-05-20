package dev.imagio.slot.forge.client;

import dev.imagio.slot.forge.network.SlotForgeNetworking;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ScreenEvent;

/**
 * Forge parity for the global GUI "wanted=1 under cursor" shortcut.
 * EMI hover state covers recipe widgets; vanilla slot hit-testing covers
 * ordinary container screens.
 */
public final class ForgeHoveredWantedHotkey {
    private static boolean pressed;

    private ForgeHoveredWantedHotkey() {
    }

    public static void onClientTick() {
        if (!ForgeWorkspaceClient.setWantedHoverDown()) {
            pressed = false;
        }
    }

    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (event == null) {
            return;
        }
        if (!ForgeWorkspaceClient.matchesSetWantedHover(event.getKeyCode(), event.getScanCode())) {
            return;
        }
        Screen screen = event.getScreen();
        if (screen == null || ForgeHoveredItemHotkeySupport.isSlotOwnedScreen(screen)) {
            return;
        }
        if (pressed) {
            event.setCanceled(true);
            return;
        }
        ItemStack stack = hoveredStack(screen);
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ItemIdentity identity = ItemIdentityMatcher.create(stack);
        SlotWorkspaceViewModel.IdentityRef ref = SlotWorkspaceViewModel.IdentityRef.from(identity);
        SlotForgeNetworking.setWantedCount(
                ref.itemId(),
                ref.comparisonMode(),
                ref.componentFingerprint(),
                1);
        pressed = true;
        event.setCanceled(true);
    }

    private static ItemStack hoveredStack(Screen screen) {
        return ForgeHoveredItemHotkeySupport.hoveredStack(screen);
    }
}
