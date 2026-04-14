package dev.imagio.slot.session;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.source.BasicInventorySource;
import dev.imagio.slot.client.source.SourceGroup;
import dev.imagio.slot.session.InventorySourceBackingKind;
import dev.imagio.slot.session.InventorySourceCapability;
import dev.imagio.slot.session.InventorySourceDescriptor;
import dev.imagio.slot.session.InventorySourceDomain;
import dev.imagio.slot.session.InventorySourceRole;
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryHostDescriptorTest {
    @Test
    void carriedOnlyHostUsesOpenPaneSourcesAsItsCarriedWorkingSet() {
        ChestLikeMenuLayout layout = new ChestLikeMenuLayout(
                4,
                List.of(
                        new BasicInventorySource(ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE, "Backpack", SourceGroup.CARRIED, 0, false, true, true),
                        new BasicInventorySource(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, "Main", SourceGroup.PLAYER_MAIN, 10, false, true, true),
                        new BasicInventorySource(ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR, "Hotbar", SourceGroup.PLAYER_HOTBAR, 20, false, true, true),
                        new BasicInventorySource(ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK, "Backpack", SourceGroup.CARRIED, 30, false, true, true)
                ),
                "Backpack",
                Map.of(
                        ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE, List.of(0, 1, 2, 3),
                        ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, List.of(),
                        ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR, List.of()
                ),
                Map.of(0, ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE),
                true,
                new TestStorageViewSession(true)
        );
        InventoryHostDescriptor host = InventoryHostDescriptor.create(
                "screen:test",
                Component.literal("Backpack"),
                new TestMenu(),
                layout,
                false,
                true,
                true
        );

        assertTrue(host.carriedOnly());
        assertTrue(host.carriedSourceIds().contains(ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE));
        assertTrue(host.actionableSourceIds().contains(ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE));
        assertEquals(Set.of(), host.externalSourceIds());
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

    private record TestStorageViewSession(boolean primaryStorageIsCarried) implements StorageViewProviderSession {
        @Override
        public String providerId() {
            return "test";
        }

        @Override
        public InventorySourceDescriptor primaryStorageSource() {
            return InventorySourceDescriptor.builder(primaryStorageIsCarried
                            ? ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE
                            : ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER)
                    .label(Component.literal("Test"))
                    .domain(InventorySourceDomain.HOST_STORAGE)
                    .role(InventorySourceRole.PRIMARY_STORAGE)
                    .slotCount(4)
                    .backingKind(InventorySourceBackingKind.MENU_BACKED)
                    .capabilities(Set.of(InventorySourceCapability.INSERT, InventorySourceCapability.EXTRACT))
                    .actionable(true)
                    .menuBacked(true)
                    .build();
        }

        @Override
        public List<Integer> primaryMenuSlots() {
            return List.of(0, 1, 2, 3);
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
