package dev.imagio.slot.session;

import dev.imagio.slot.capability.ToolCapabilityDescriptor;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.session.InventorySourceBackingKind;
import dev.imagio.slot.session.InventorySourceCapability;
import dev.imagio.slot.session.InventorySourceDescriptor;
import dev.imagio.slot.session.InventorySourceDomain;
import dev.imagio.slot.session.InventorySourceRole;
import dev.imagio.slot.session.ToolOpenCommand;
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
import dev.imagio.slot.storage.adapter.ExternalToolKind;
import dev.imagio.slot.storage.adapter.ExternalToolSpec;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import dev.imagio.slot.storage.provider.StorageViewProviderSession;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class InventoryHostToolCoordinatorTest {
    @Test
    void activationCommandOnlyExistsForInactiveTabbedTools() {
        ExternalToolSpec toolSpec = ExternalToolSpec.craftingGrid(
                "tool:test",
                Component.literal("Craft"),
                40,
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9),
                10
        );
        ToolCapabilityDescriptor inactiveTool = new ToolCapabilityDescriptor(
                "tool:test",
                "sophisticatedbackpacks",
                ExternalToolKind.CRAFTING_GRID,
                toolSpec,
                false,
                new ToolOpenCommand("sophisticatedbackpacks", "tool:test", 7),
                Map.of(),
                Map.of()
        );
        InventoryHostDescriptor host = host(List.of(inactiveTool));

        assertEquals(inactiveTool, InventoryHostToolCoordinator.firstTool(host, ExternalToolKind.CRAFTING_GRID, "sophisticatedbackpacks"));
        assertNotNull(InventoryHostToolCoordinator.activationCommand(inactiveTool));
        assertNull(InventoryHostToolCoordinator.activationCommand(new ToolCapabilityDescriptor(
                "tool:live",
                "sophisticatedbackpacks",
                ExternalToolKind.CRAFTING_GRID,
                toolSpec,
                true,
                new ToolOpenCommand("sophisticatedbackpacks", "tool:live", 7),
                Map.of(),
                Map.of()
        )));
    }

    private static InventoryHostDescriptor host(List<ToolCapabilityDescriptor> tools) {
        TestMenu menu = new TestMenu();
        ChestLikeMenuLayout layout = new ChestLikeMenuLayout(
                0,
                List.of(),
                "Test",
                Map.of(ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER, List.of()),
                Map.of(),
                false,
                new TestStorageViewSession(tools)
        );
        return InventoryHostDescriptor.create("screen:test", Component.literal("Test"), menu, layout, false, false, false);
    }

    private static final class TestMenu extends AbstractContainerMenu {
        private TestMenu() {
            super(null, 0);
        }

        @Override
        public boolean stillValid(net.minecraft.world.entity.player.Player player) {
            return true;
        }
    }

    private record TestStorageViewSession(List<ToolCapabilityDescriptor> toolDescriptors) implements StorageViewProviderSession {
        @Override
        public String providerId() {
            return "test";
        }

        @Override
        public InventorySourceDescriptor primaryStorageSource() {
            return InventorySourceDescriptor.builder(ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER)
                    .label(Component.literal("Test"))
                    .domain(InventorySourceDomain.HOST_STORAGE)
                    .role(InventorySourceRole.PRIMARY_STORAGE)
                    .slotCount(0)
                    .backingKind(InventorySourceBackingKind.MENU_BACKED)
                    .capabilities(Set.of(InventorySourceCapability.INSERT, InventorySourceCapability.EXTRACT))
                    .actionable(true)
                    .menuBacked(true)
                    .build();
        }

        @Override
        public List<Integer> primaryMenuSlots() {
            return List.of();
        }

        @Override
        public List<ToolCapabilityDescriptor> toolDescriptors() {
            return toolDescriptors;
        }

        @Override
        public List<ExternalStorageStackSnapshot> readClientPrimarySnapshots(AbstractContainerMenu menu) {
            return List.of();
        }

        @Override
        public ItemStack extractFromPrimary(AbstractContainerMenu menu, ServerPlayer player, ItemIdentity identity, StorageTransferMode mode) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertIntoPrimary(AbstractContainerMenu menu, ServerPlayer player, ItemStack stack) {
            return stack;
        }
    }
}
