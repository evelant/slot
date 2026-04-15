package dev.imagio.slot.inventory.query;

import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.integration.PlayerInventoryExtension;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryDomainQueryServiceTest {
    @Test
    void queryServiceReadsHostAndExtensionSourcesFromDomainModels() {
        InventorySourceDescriptor hostSource = InventorySourceDescriptor.builder("host.storage.a")
                .label(Component.literal("Storage"))
                .domain(InventorySourceDomain.HOST_STORAGE)
                .role(InventorySourceRole.PRIMARY_STORAGE)
                .logicalSlotCount(6)
                .bindingRoute(InventoryBindingRoute.MENU)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.EXTERNAL)
                .stableOrder(10)
                .build();
        InventorySourceDescriptor extensionSource = InventorySourceDescriptor.builder("player.extension.backpack")
                .label(Component.literal("Backpack"))
                .domain(InventorySourceDomain.PLAYER_EXTENSION)
                .role(InventorySourceRole.PROVIDER_DEFINED)
                .logicalSlotCount(4)
                .bindingRoute(InventoryBindingRoute.PROVIDER)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(20)
                .build();

        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "test.screen",
                Component.literal("Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                new TestHostSession(hostSource.id()),
                List.of(new TestExtension(extensionSource)),
                PlayerRuntimeStateDescriptor.vanilla(0),
                List.of(hostSource, extensionSource),
                List.of(),
                List.of(),
                List.of(),
                dev.imagio.slot.inventory.integration.InventoryHostObservationHints.defaults(),
                ""
        );
        InventoryAuthoritySnapshot authority = InventoryAuthorityFixtures.authority(
                host,
                Map.of(
                        extensionSource.id(),
                        List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:bread", 3, 64), 3))
                ),
                Map.of(extensionSource.id(), 4)
        );

        InventorySourceSnapshot hostSnapshot = InventoryDomainQueryService.readSource(authority, hostSource.id());
        InventorySourceSnapshot extensionSnapshot = InventoryDomainQueryService.readSource(authority, extensionSource.id());
        InventoryDomainQueryService.PaneCapacity carriedCapacity =
                InventoryDomainQueryService.summarizePane(authority, InventoryPaneMembership.CARRIED);
        InventoryDomainQueryService.PaneCapacity externalCapacity =
                InventoryDomainQueryService.summarizePane(authority, InventoryPaneMembership.EXTERNAL);

        assertEquals(1, hostSnapshot.entries().size());
        assertEquals(1, extensionSnapshot.entries().size());
        assertEquals(4, carriedCapacity.totalSlots());
        assertEquals(1, carriedCapacity.occupiedSlots());
        assertEquals(6, externalCapacity.totalSlots());
        assertEquals(1, externalCapacity.occupiedSlots());
        assertTrue(InventoryDomainQueryService.sourcesInPane(host, InventoryPaneMembership.CARRIED)
                .stream()
                .anyMatch(source -> extensionSource.id().equals(source.id())));
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

    private record TestHostSession(String sourceId) implements InventoryHostSession {
        @Override
        public String providerId() {
            return "test";
        }

        @Override
        public List<InventoryStackSnapshot> readSnapshots(InventoryHostDescriptor host, String requestedSourceId) {
            if (!sourceId.equals(requestedSourceId)) {
                return List.of();
            }
            return List.of(new InventoryStackSnapshot(1, new ItemStack("minecraft:oak_log", 7, 64), 7));
        }
    }

    private record TestExtension(InventorySourceDescriptor source) implements PlayerInventoryExtension {
        @Override
        public String providerId() {
            return "test:extension";
        }

        @Override
        public List<InventorySourceDescriptor> additionalSources() {
            return List.of(source);
        }
    }
}
