package dev.imagio.slot.inventory.query;

import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InventoryWorkingSetProjectionServiceTest {
    @Test
    void mergedCarriedRowsRetainExactBackingEntriesInStableSourceOrder() {
        InventoryHostDescriptor host = host(List.of(
                source("player.main", 10, 27),
                source("carried.backpack.one", 11, 9),
                source("carried.backpack.two", 12, 9)
        ));
        InventoryAuthoritySnapshot authority = new InventoryAuthoritySnapshot(
                host,
                Map.of(
                        "player.main", slotSource("player.main", 27, 0, new ItemStack("minecraft:apple", 3, 64)),
                        "carried.backpack.one", slotSource("carried.backpack.one", 9, 0, new ItemStack("minecraft:apple", 2, 64)),
                        "carried.backpack.two", slotSource("carried.backpack.two", 9, 0, new ItemStack("minecraft:apple", 5, 64))
                ),
                CursorStateSnapshot.empty()
        );

        InventoryWorkingSetProjection projection = InventoryWorkingSetProjectionService.project(
                authority,
                InventoryPaneMembership.CARRIED,
                entry -> identity(entry.stack())
        );

        assertEquals(1, projection.rows().size());
        ProjectedInventoryRow row = projection.rows().getFirst();
        assertNotNull(row.identity());
        assertEquals(10, row.visibleTotalCount());
        assertEquals(List.of("player.main", "carried.backpack.one", "carried.backpack.two"), row.backingSources());
        assertEquals(
                List.of(
                        InventoryEntryKey.slot("player.main", 0),
                        InventoryEntryKey.slot("carried.backpack.one", 0),
                        InventoryEntryKey.slot("carried.backpack.two", 0)
                ),
                row.backingEntries().stream().map(ProjectedEntryRef::entryKey).toList()
        );
    }

    private static InventorySourceDescriptor source(String sourceId, int stableOrder, int slotCount) {
        return InventorySourceDescriptor.builder(sourceId)
                .label(Component.literal(sourceId))
                .domain(InventorySourceDomain.PLAYER_EXTENSION)
                .role(InventorySourceRole.PROVIDER_DEFINED)
                .logicalSlotCount(slotCount)
                .bindingRoute(InventoryBindingRoute.PROVIDER)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(stableOrder)
                .build();
    }

    private static InventorySourceSnapshot slotSource(String sourceId, int slotCapacity, int slotIndex, ItemStack stack) {
        return new InventorySourceSnapshot(
                sourceId,
                slotCapacity,
                List.of(new InventoryEntrySnapshot(
                        InventoryEntryKey.slot(sourceId, slotIndex),
                        stack,
                        stack.getCount(),
                        ""
                )),
                ""
        );
    }

    private static ItemIdentity identity(ItemStack stack) {
        return new ItemIdentity(stack.itemId(), ItemComparisonMode.ITEM_ID, stack.componentFingerprint());
    }

    private static InventoryHostDescriptor host(List<InventorySourceDescriptor> sources) {
        TestMenu menu = new TestMenu();
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 1, "test.provider", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "test.screen",
                Component.literal("Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                sources,
                List.of(),
                List.of(),
                List.of(),
                false,
                true,
                false,
                ""
        );
    }

    private static final class TestMenu extends AbstractContainerMenu {
        private TestMenu() {
            super(null, 0);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
