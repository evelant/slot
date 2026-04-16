package dev.imagio.slot.neoforge.client.host;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class ObservedScreenContexts {
    private ObservedScreenContexts() {
    }

    public static ObservedScreenContext observe(Minecraft minecraft) {
        if (minecraft == null || minecraft.player == null || !(minecraft.screen instanceof AbstractContainerScreen<?> screen)) {
            return null;
        }
        return observe(screen, minecraft.player.getInventory());
    }

    public static ObservedScreenContext observe(AbstractContainerScreen<?> screen, Inventory playerInventory) {
        if (screen == null || playerInventory == null) {
            return null;
        }
        AbstractContainerMenu menu = screen.getMenu();
        if (menu == null || playerInventory == null) {
            return null;
        }

        String screenClassName = screen.getClass().getName();
        Component title = screen.getTitle();
        InventoryHostObservationHints hints = inferHints(screenClassName, title, menu, playerInventory);
        SlotDebugLog.log(
                "Observed screen {} menu {} family {} slotOwnership {} carriedOnly {}",
                screenClassName,
                menu.getClass().getName(),
                hints.hostFamilyHint(),
                hints.slotOwnershipPosture(),
                hints.carriedOnly()
        );
        return new ObservedScreenContext(screen, menu, screenClassName, title, playerInventory, hints);
    }

    private static InventoryHostObservationHints inferHints(
            String screenClassName,
            Component title,
            AbstractContainerMenu menu,
            Inventory playerInventory
    ) {
        String normalizedClass = normalize(screenClassName);
        String normalizedTitle = normalize(title == null ? "" : title.getString());
        boolean terminalLike = containsAny(normalizedClass, normalizedTitle, "terminal", "grid", "network", "me", "storage terminal");
        boolean portableLike = containsAny(normalizedClass, normalizedTitle, "backpack", "satchel", "pouch");
        boolean creativeLike = normalizedClass.contains("creativemodeinventoryscreen") || normalizedTitle.contains("creative");
        int supportedStorageSlots = inferSupportedStorageSlots(menu, playerInventory);

        InventoryHostFamilyHint hostFamilyHint = portableLike || menu instanceof InventoryMenu
                ? InventoryHostFamilyHint.CARRIED_ONLY
                : terminalLike
                ? InventoryHostFamilyHint.TERMINAL_HYBRID
                : supportedStorageSlots > 0
                ? InventoryHostFamilyHint.DUAL_PANE
                : InventoryHostFamilyHint.UNKNOWN;

        InventorySlotOwnershipPosture slotOwnershipPosture = terminalLike
                ? supportedStorageSlots > 0 ? InventorySlotOwnershipPosture.HYBRID : InventorySlotOwnershipPosture.PROVIDER_BACKED
                : (supportedStorageSlots > 0 || menu instanceof InventoryMenu || menu instanceof CraftingMenu)
                ? InventorySlotOwnershipPosture.SLOT_OWNED
                : InventorySlotOwnershipPosture.UNKNOWN;

        Map<String, String> shapeSignals = new LinkedHashMap<>();
        shapeSignals.put("menuClass", menu.getClass().getName());
        shapeSignals.put("supportedStorageSlots", Integer.toString(Math.max(0, supportedStorageSlots)));
        shapeSignals.put("portableLike", Boolean.toString(portableLike));
        shapeSignals.put("terminalLike", Boolean.toString(terminalLike));
        return new InventoryHostObservationHints(
                hostFamilyHint,
                slotOwnershipPosture,
                hostFamilyHint == InventoryHostFamilyHint.CARRIED_ONLY,
                !creativeLike,
                Map.copyOf(shapeSignals)
        );
    }

    private static boolean containsAny(String className, String title, String... values) {
        for (String value : values) {
            String normalized = normalize(value);
            if (className.contains(normalized) || title.contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static int inferSupportedStorageSlots(AbstractContainerMenu menu, Inventory playerInventory) {
        if (menu instanceof ChestMenu chestMenu) {
            return chestMenu.getRowCount() * 9;
        }
        if (menu instanceof ShulkerBoxMenu) {
            return 27;
        }
        if (menu == null || playerInventory == null || menu.slots.size() <= 36) {
            return -1;
        }

        int trailingPlayerSlots = 0;
        for (int slotIndex = menu.slots.size() - 1; slotIndex >= 0; slotIndex--) {
            if (menu.getSlot(slotIndex).container == playerInventory) {
                trailingPlayerSlots++;
            } else {
                break;
            }
        }
        if (trailingPlayerSlots != 36) {
            return -1;
        }
        int containerSlots = menu.slots.size() - trailingPlayerSlots;
        return containerSlots > 0 ? containerSlots : -1;
    }
}
