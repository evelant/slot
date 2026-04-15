package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.core.EquipmentGroupDescriptor;
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
import dev.imagio.slot.inventory.core.QuickAccessLaneDescriptor;
import dev.imagio.slot.registry.ProviderResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryHostResolverTest {
    @AfterEach
    void clearRegistry() {
        InventoryIntegrationRegistry.clear();
    }

    @Test
    void resolverMergesProviderHostSourcesAndPlayerExtensions() {
        InventoryIntegrationRegistry.register(new TestProvider());
        InventoryIntegrationRegistry.markBootstrapped();

        TestMenu menu = new TestMenu();
        Inventory playerInventory = new Inventory(new TestPlayer());
        InventoryHostDescriptor host = InventoryHostResolver.resolve(new InventoryHostContext(
                menu,
                playerInventory,
                Component.literal("Host"),
                "test.screen",
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.DUAL_PANE,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        false,
                        true,
                        Map.of("resolverTest", "true")
                )
        ));

        assertNotNull(host);
        assertNotNull(host.source("provider.storage.a"));
        assertNotNull(host.source("provider.storage.b"));
        assertNotNull(host.source("extension.backpack"));
        assertEquals("extension.quick_access", host.quickAccessLane("extension.quick_access").sourceId());
        assertEquals("extension.curios", host.equipmentGroup("extension.curios").sourceId());
        assertEquals(List.of(40, 41), host.topology().menuSlotsForSource("provider.storage.a"));
        assertEquals(List.of(42, 43), host.topology().menuSlotsForToolRegion("tool.region.filter"));
        assertTrue(host.tool("tool.filter").regions().stream().anyMatch(region -> region.id().equals("tool.region.filter")));
        assertEquals(InventoryHostFamilyHint.DUAL_PANE, host.observationHints().hostFamilyHint());
        assertEquals(InventorySlotOwnershipPosture.SLOT_OWNED, host.observationHints().slotOwnershipPosture());
    }

    @Test
    void resolverUsesStableHostInstanceKeyForSameMenuAndProviderScope() {
        InventoryIntegrationRegistry.register(new TestProvider());
        InventoryIntegrationRegistry.markBootstrapped();

        TestMenu menu = new TestMenu();
        Inventory playerInventory = new Inventory(new TestPlayer());
        InventoryHostContext context = new InventoryHostContext(
                menu,
                playerInventory,
                Component.literal("Host"),
                "test.screen",
                InventoryHostObservationHints.defaults()
        );

        InventoryHostDescriptor first = InventoryHostResolver.resolve(context);
        InventoryHostDescriptor second = InventoryHostResolver.resolve(context);

        assertEquals(first.hostId(), second.hostId());
        assertEquals(new HostInstanceKey(TestMenu.class.getName(), menu.containerId, "test.provider", "scope:test"), first.hostId());
    }

    @Test
    void resolverRejectsDuplicateSourceIdsFromSupportedProviders() {
        InventoryIntegrationRegistry.register(new DuplicateSourceProvider());
        InventoryIntegrationRegistry.markBootstrapped();

        assertThrows(IllegalStateException.class, () -> InventoryHostResolver.resolve(new InventoryHostContext(
                new TestMenu(),
                new Inventory(new TestPlayer()),
                Component.literal("Host"),
                "duplicate.screen",
                InventoryHostObservationHints.defaults()
        )));
    }

    private static final class TestProvider implements InventoryIntegrationProvider {
        @Override
        public String providerId() {
            return "test.provider";
        }

        @Override
        public ProviderResult<InventoryHostSession> openHost(InventoryHostContext context) {
            InventorySourceDescriptor storageA = InventorySourceDescriptor.builder("provider.storage.a")
                    .label(Component.literal("A"))
                    .domain(InventorySourceDomain.HOST_STORAGE)
                    .role(InventorySourceRole.PRIMARY_STORAGE)
                    .logicalSlotCount(2)
                    .bindingRoute(InventoryBindingRoute.MENU)
                    .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                    .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                    .paneMembership(InventoryPaneMembership.EXTERNAL)
                    .build();
            InventorySourceDescriptor storageB = InventorySourceDescriptor.builder("provider.storage.b")
                    .label(Component.literal("B"))
                    .domain(InventorySourceDomain.HOST_STORAGE)
                    .role(InventorySourceRole.PROVIDER_DEFINED)
                    .logicalSlotCount(3)
                    .bindingRoute(InventoryBindingRoute.PROVIDER)
                    .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                    .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                    .paneMembership(InventoryPaneMembership.EXTERNAL)
                    .build();

            return ProviderResult.supported(new InventoryHostSession() {
                @Override
                public String providerId() {
                    return "test.provider";
                }

                @Override
                public String providerScopeId() {
                    return "scope:test";
                }

                @Override
                public List<InventorySourceDescriptor> hostSources() {
                    return List.of(storageA, storageB);
                }

                @Override
                public InventoryTopologyDescriptor topology() {
                    return new InventoryTopologyDescriptor(
                            Map.of("provider.storage.a", List.of(40, 41)),
                            Map.of(40, "provider.storage.a", 41, "provider.storage.a"),
                            Map.of("tool.region.filter", List.of(42, 43))
                    );
                }

                @Override
                public List<dev.imagio.slot.inventory.core.InventoryToolDescriptor> tools() {
                    return List.of(new dev.imagio.slot.inventory.core.InventoryToolDescriptor(
                            "tool.filter",
                            "test.provider",
                            dev.imagio.slot.inventory.core.InventoryToolKind.FILTER,
                            Component.literal("Filter"),
                            new dev.imagio.slot.inventory.core.ToolPresentationHints("Filter", 10, "docked", 0),
                            10,
                            true,
                            false,
                            null,
                            List.of(new dev.imagio.slot.inventory.core.ToolRegionDescriptor(
                                    "tool.region.filter",
                                    dev.imagio.slot.inventory.core.ToolRegionRole.FILTER,
                                    2,
                                    InventoryBindingRoute.MENU,
                                    Set.of(InventoryCapability.TOOL_REGION_MUTATION),
                                    false,
                                    "",
                                    ""
                            )),
                            List.of(),
                            List.of(),
                            Map.of(),
                            Map.of(),
                            ""
                    ));
                }
            });
        }

        @Override
        public List<PlayerInventoryExtension> playerExtensions(PlayerInventoryContext context) {
            return List.of(new PlayerInventoryExtension() {
                @Override
                public String providerId() {
                    return "test.provider.extension";
                }

                @Override
                public List<InventorySourceDescriptor> additionalSources() {
                    return List.of(
                            InventorySourceDescriptor.builder("extension.backpack")
                                    .label(Component.literal("Backpack"))
                                    .domain(InventorySourceDomain.PLAYER_EXTENSION)
                                    .role(InventorySourceRole.PROVIDER_DEFINED)
                                    .laneId("extension.quick_access")
                                    .groupId("extension.curios")
                                    .logicalSlotCount(6)
                                    .bindingRoute(InventoryBindingRoute.PROVIDER)
                                    .capabilities(Set.of(
                                            InventoryCapability.INSERT,
                                            InventoryCapability.EXTRACT,
                                            InventoryCapability.QUICK_ACCESS_ASSIGN,
                                            InventoryCapability.EQUIP
                                    ))
                                    .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                                    .paneMembership(InventoryPaneMembership.CARRIED)
                                    .build(),
                            InventorySourceDescriptor.builder("extension.quick_access")
                                    .label(Component.literal("Quick"))
                                    .domain(InventorySourceDomain.PLAYER_EXTENSION)
                                    .role(InventorySourceRole.QUICK_ACCESS)
                                    .laneId("extension.quick_access")
                                    .logicalSlotCount(3)
                                    .bindingRoute(InventoryBindingRoute.PROVIDER)
                                    .capabilities(Set.of(
                                            InventoryCapability.INSERT,
                                            InventoryCapability.EXTRACT,
                                            InventoryCapability.QUICK_ACCESS_ASSIGN
                                    ))
                                    .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                                    .paneMembership(InventoryPaneMembership.CARRIED)
                                    .build(),
                            InventorySourceDescriptor.builder("extension.curios")
                                    .label(Component.literal("Curios"))
                                    .domain(InventorySourceDomain.PLAYER_EXTENSION)
                                    .role(InventorySourceRole.EQUIPMENT)
                                    .groupId("extension.curios")
                                    .logicalSlotCount(2)
                                    .bindingRoute(InventoryBindingRoute.PROVIDER)
                                    .capabilities(Set.of(
                                            InventoryCapability.INSERT,
                                            InventoryCapability.EXTRACT,
                                            InventoryCapability.EQUIP,
                                            InventoryCapability.UNEQUIP
                                    ))
                                    .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                                    .paneMembership(InventoryPaneMembership.CARRIED)
                                    .build()
                    );
                }

                @Override
                public List<QuickAccessLaneDescriptor> additionalQuickAccessLanes() {
                    return List.of(new QuickAccessLaneDescriptor(
                            "extension.quick_access",
                            Component.literal("Utility"),
                            "extension.quick_access",
                            3,
                            Set.of(
                                    InventoryCapability.INSERT,
                                    InventoryCapability.EXTRACT,
                                    InventoryCapability.QUICK_ACCESS_ASSIGN
                            ),
                            "",
                            100
                    ));
                }

                @Override
                public List<EquipmentGroupDescriptor> additionalEquipmentGroups() {
                    return List.of(new EquipmentGroupDescriptor(
                            "extension.curios",
                            Component.literal("Curios"),
                            "extension.curios",
                            2,
                            Set.of(
                                    InventoryCapability.INSERT,
                                    InventoryCapability.EXTRACT,
                                    InventoryCapability.EQUIP,
                                    InventoryCapability.UNEQUIP
                            ),
                            "",
                            100
                    ));
                }
            });
        }
    }

    private static final class DuplicateSourceProvider implements InventoryIntegrationProvider {
        @Override
        public String providerId() {
            return "duplicate.provider";
        }

        @Override
        public ProviderResult<InventoryHostSession> openHost(InventoryHostContext context) {
            if (context == null || !"duplicate.screen".equals(context.screenClassName())) {
                return ProviderResult.unsupported(providerId(), "unsupported_host", "Not a duplicate host");
            }
            return ProviderResult.supported(new InventoryHostSession() {
                @Override
                public String providerId() {
                    return "duplicate.provider";
                }

                @Override
                public List<InventorySourceDescriptor> hostSources() {
                    InventorySourceDescriptor duplicate = InventorySourceDescriptor.builder("duplicate.source")
                            .label(Component.literal("Duplicate"))
                            .domain(InventorySourceDomain.HOST_STORAGE)
                            .role(InventorySourceRole.PRIMARY_STORAGE)
                            .logicalSlotCount(1)
                            .bindingRoute(InventoryBindingRoute.MENU)
                            .capabilities(Set.of(InventoryCapability.INSERT))
                            .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                            .paneMembership(InventoryPaneMembership.EXTERNAL)
                            .build();
                    return List.of(duplicate, duplicate);
                }
            });
        }
    }

    private static final class TestPlayer extends Player {
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
