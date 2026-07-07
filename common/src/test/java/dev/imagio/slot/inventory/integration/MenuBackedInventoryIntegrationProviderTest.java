package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.registry.ProviderResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuBackedInventoryIntegrationProviderTest {
    @Test
    void ae2LikeTerminalMenuIsNotClaimedByGenericMenuFallback() {
        MenuBackedInventoryIntegrationProvider provider = new MenuBackedInventoryIntegrationProvider();

        ProviderResult<InventoryHostSession> result = provider.openHost(new InventoryHostContext(
                new CraftingTerminalMenu(),
                new Inventory(new TestPlayer()),
                Component.literal("ME Crafting Terminal"),
                "appeng.menu.me.items.CraftingTermMenu",
                InventoryHostObservationHints.defaults()
        ));

        assertTrue(result.unsupported());
        assertEquals("terminal_requires_dedicated_provider", result.diagnostics().reasonCode());
    }

    @Test
    void providerBackedTerminalHintsRequireDedicatedProviderEvenWithoutAe2Classes() {
        MenuBackedInventoryIntegrationProvider provider = new MenuBackedInventoryIntegrationProvider();

        ProviderResult<InventoryHostSession> result = provider.openHost(new InventoryHostContext(
                new PlainMenu(),
                new Inventory(new TestPlayer()),
                Component.literal("Terminal"),
                "example.storage.Screen",
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.TERMINAL_HYBRID,
                        InventorySlotOwnershipPosture.PROVIDER_BACKED,
                        false,
                        true,
                        Map.of())
        ));

        assertTrue(result.unsupported());
        assertEquals("terminal_requires_dedicated_provider", result.diagnostics().reasonCode());
    }

    private static final class CraftingTerminalMenu extends AbstractContainerMenu {
        private CraftingTerminalMenu() {
            super(null, 0);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

    private static final class PlainMenu extends AbstractContainerMenu {
        private PlainMenu() {
            super(null, 0);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

    private static final class TestPlayer extends Player {
    }
}
