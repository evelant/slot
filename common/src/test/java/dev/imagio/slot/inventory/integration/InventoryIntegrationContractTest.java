package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.EquipmentGroupDescriptor;
import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryToolAction;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolDescriptor;
import dev.imagio.slot.inventory.core.InventoryToolKind;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.QuickAccessLaneDescriptor;
import dev.imagio.slot.inventory.core.ToolActivationToken;
import dev.imagio.slot.inventory.core.ToolPresentationHints;
import dev.imagio.slot.inventory.core.ToolRegionDescriptor;
import dev.imagio.slot.inventory.core.ToolRegionRole;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryIntegrationContractTest {
    @AfterEach
    void clearRegistry() {
        InventoryIntegrationRegistry.clear();
    }

    @Test
    void syntheticTerminalAndEquipmentProvidersFitFinalContractShape() {
        InventoryIntegrationRegistry.register(new Ae2StyleTerminalProvider());
        InventoryIntegrationRegistry.register(new RefinedStorageStyleTerminalProvider());
        InventoryIntegrationRegistry.register(new CuriosStyleExtensionProvider());
        InventoryIntegrationRegistry.markBootstrapped();

        InventoryHostDescriptor host = InventoryHostResolver.resolve(new InventoryHostContext(
                new TestMenu(),
                new Inventory(new TestPlayer()),
                Component.literal("Terminal"),
                "ae2.terminal",
                false,
                true,
                false
        ));

        assertNotNull(host.source("ae2:terminal.primary"));
        assertNotNull(host.source("curios:charm_slots"));
        assertEquals("curios:utility_lane", host.quickAccessLane("curios:utility_lane").sourceId());
        assertEquals("curios:charm_slots", host.equipmentGroup("curios:charms").sourceId());
        assertEquals(List.of(0, 1, 2), host.topology().menuSlotsForSource("ae2:terminal.primary"));
        assertEquals(List.of(20, 21, 22, 23), host.topology().menuSlotsForToolRegion("ae2:craft/input"));
        assertTrue(host.tool("ae2:crafting_terminal").actions().stream().map(InventoryToolAction::id).anyMatch(id -> id == InventoryToolActionId.CLEAR_GRID));
        assertTrue(host.source("ae2:terminal.primary").simulationSupported());
        assertTrue(host.tool("ae2:crafting_terminal").actions().stream().allMatch(InventoryToolAction::simulationSupported));

        ToolActionResult simulatedActivation = InventoryMutationRouter.activateTool(host, "ae2:crafting_terminal", InventoryActionMode.SIMULATE);
        assertTrue(simulatedActivation.successful());
    }

    @Test
    void syntheticRefinedStorageProviderSupportsHostMutationSimulation() {
        InventoryIntegrationRegistry.register(new Ae2StyleTerminalProvider());
        InventoryIntegrationRegistry.register(new RefinedStorageStyleTerminalProvider());
        InventoryIntegrationRegistry.markBootstrapped();

        InventoryHostDescriptor host = InventoryHostResolver.resolve(new InventoryHostContext(
                new TestMenu(),
                new Inventory(new TestPlayer()),
                Component.literal("Grid"),
                "rs.terminal",
                false,
                true,
                false
        ));

        assertNotNull(host.source("rs:grid.primary"));
        assertEquals(List.of(5, 6, 7, 8), host.topology().menuSlotsForSource("rs:grid.primary"));
        assertEquals(List.of(30, 31, 32), host.topology().menuSlotsForToolRegion("rs:grid/pattern"));
        assertTrue(host.source("rs:grid.primary").simulationSupported());

        MutationResult simulatedExtract = InventoryMutationRouter.mutate(
                host,
                InventoryMutationRequest.extract(host, null, "rs:grid.primary", ItemIdentity.of("minecraft:stone"), InventoryTransferMode.ONE),
                InventoryMutationMode.SIMULATE
        );
        ToolActionResult simulatedAction = InventoryMutationRouter.executeToolAction(
                host,
                "rs:grid",
                InventoryToolActionId.CLEAR_GRID,
                InventoryActionMode.SIMULATE
        );

        assertTrue(simulatedExtract.successful());
        assertTrue(simulatedAction.successful());
    }

    private static final class Ae2StyleTerminalProvider implements InventoryIntegrationProvider {
        @Override
        public String providerId() {
            return "ae2:test";
        }

        @Override
        public int priority() {
            return 500;
        }

        @Override
        public ProviderResult<InventoryHostSession> openHost(InventoryHostContext context) {
            if (context == null || !"ae2.terminal".equals(context.screenClassName())) {
                return ProviderResult.unsupported(providerId(), "unsupported_host", "Not an AE2 terminal host");
            }

            InventorySourceDescriptor primarySource = InventorySourceDescriptor.builder("ae2:terminal.primary")
                    .label(Component.literal("ME Terminal"))
                    .domain(InventorySourceDomain.HOST_STORAGE)
                    .role(InventorySourceRole.PRIMARY_STORAGE)
                    .logicalSlotCount(3)
                    .bindingRoute(InventoryBindingRoute.MENU)
                    .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                    .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                    .paneMembership(InventoryPaneMembership.EXTERNAL)
                    .build();

            InventoryToolDescriptor craftingTool = new InventoryToolDescriptor(
                    "ae2:crafting_terminal",
                    providerId(),
                    InventoryToolKind.CRAFTING_GRID,
                    Component.literal("Crafting Terminal"),
                    new ToolPresentationHints("Crafting Terminal", 80, "docked", 70),
                    80,
                    true,
                    true,
                    new ToolActivationToken(providerId(), "ae2:crafting_terminal", Map.of("terminal", "crafting")),
                    List.of(new ToolRegionDescriptor(
                            "ae2:craft/input",
                            ToolRegionRole.INPUT,
                            4,
                            InventoryBindingRoute.MENU,
                            Set.of(InventoryCapability.TOOL_REGION_MUTATION),
                            false,
                            "",
                            ""
                    )),
                    List.of(new InventoryToolAction(
                            "clear_grid",
                            InventoryToolActionId.CLEAR_GRID,
                            Component.literal("Clear"),
                            Component.empty()
                    )),
                    List.of(),
                    Map.of(),
                    Map.of("supportsSimulate", "true"),
                    ""
            );

            return ProviderResult.supported(new InventoryHostSession() {
                @Override
                public String providerId() {
                    return "ae2:test";
                }

                @Override
                public List<InventorySourceDescriptor> hostSources() {
                    return List.of(primarySource);
                }

                @Override
                public InventoryTopologyDescriptor topology() {
                    return new InventoryTopologyDescriptor(
                            Map.of("ae2:terminal.primary", List.of(0, 1, 2)),
                            Map.of(0, "ae2:terminal.primary", 1, "ae2:terminal.primary", 2, "ae2:terminal.primary"),
                            Map.of("ae2:craft/input", List.of(20, 21, 22, 23))
                    );
                }

                @Override
                public List<InventoryToolDescriptor> tools() {
                    return List.of(craftingTool);
                }

                @Override
                public ToolActionResult activateTool(InventoryHostDescriptor host, String toolId, InventoryActionMode mode) {
                    return "ae2:crafting_terminal".equals(toolId) ? ToolActionResult.success() : ToolActionResult.blocked("wrong_tool");
                }
            });
        }
    }

    private static final class RefinedStorageStyleTerminalProvider implements InventoryIntegrationProvider {
        @Override
        public String providerId() {
            return "rs:test";
        }

        @Override
        public int priority() {
            return 400;
        }

        @Override
        public ProviderResult<InventoryHostSession> openHost(InventoryHostContext context) {
            if (context == null || !"rs.terminal".equals(context.screenClassName())) {
                return ProviderResult.unsupported(providerId(), "unsupported_host", "Not a Refined Storage terminal host");
            }

            InventorySourceDescriptor primarySource = InventorySourceDescriptor.builder("rs:grid.primary")
                    .label(Component.literal("Grid"))
                    .domain(InventorySourceDomain.HOST_STORAGE)
                    .role(InventorySourceRole.PRIMARY_STORAGE)
                    .logicalSlotCount(4)
                    .bindingRoute(InventoryBindingRoute.MENU)
                    .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                    .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                    .paneMembership(InventoryPaneMembership.EXTERNAL)
                    .build();

            InventoryToolDescriptor gridTool = new InventoryToolDescriptor(
                    "rs:grid",
                    providerId(),
                    InventoryToolKind.PROVIDER_DEFINED,
                    Component.literal("Grid"),
                    new ToolPresentationHints("Grid", 60, "docked", 60),
                    60,
                    true,
                    true,
                    new ToolActivationToken(providerId(), "rs:grid", Map.of("mode", "grid")),
                    List.of(new ToolRegionDescriptor(
                            "rs:grid/pattern",
                            ToolRegionRole.INPUT,
                            3,
                            InventoryBindingRoute.MENU,
                            Set.of(InventoryCapability.TOOL_REGION_MUTATION),
                            false,
                            "",
                            ""
                    )),
                    List.of(new InventoryToolAction(
                            "clear_grid",
                            InventoryToolActionId.CLEAR_GRID,
                            Component.literal("Clear"),
                            Component.empty()
                    )),
                    List.of(),
                    Map.of(),
                    Map.of("supportsSimulate", "true"),
                    ""
            );

            return ProviderResult.supported(new InventoryHostSession() {
                @Override
                public String providerId() {
                    return "rs:test";
                }

                @Override
                public List<InventorySourceDescriptor> hostSources() {
                    return List.of(primarySource);
                }

                @Override
                public InventoryTopologyDescriptor topology() {
                    return new InventoryTopologyDescriptor(
                            Map.of("rs:grid.primary", List.of(5, 6, 7, 8)),
                            Map.of(5, "rs:grid.primary", 6, "rs:grid.primary", 7, "rs:grid.primary", 8, "rs:grid.primary"),
                            Map.of("rs:grid/pattern", List.of(30, 31, 32))
                    );
                }

                @Override
                public List<InventoryToolDescriptor> tools() {
                    return List.of(gridTool);
                }

                @Override
                public MutationResult mutate(
                        InventoryHostDescriptor host,
                        InventoryMutationRequest request,
                        InventoryMutationMode mode
                ) {
                    if (!"rs:grid.primary".equals(request.sourceId())) {
                        return MutationResult.blocked("wrong_source", request == null ? null : request.stack());
                    }
                    return switch (request.kind()) {
                        case EXTRACT -> MutationResult.success(new net.minecraft.world.item.ItemStack("minecraft:stone", 1, 64));
                        case INSERT -> MutationResult.success(net.minecraft.world.item.ItemStack.EMPTY);
                        default -> MutationResult.blocked("unsupported_mutation", request.stack());
                    };
                }

                @Override
                public ToolActionResult executeToolAction(
                        InventoryHostDescriptor host,
                        String toolId,
                        InventoryToolActionId actionId,
                        InventoryActionMode mode
                ) {
                    return "rs:grid".equals(toolId) && actionId == InventoryToolActionId.CLEAR_GRID
                            ? ToolActionResult.success()
                            : ToolActionResult.blocked("wrong_tool_or_action");
                }
            });
        }
    }

    private static final class CuriosStyleExtensionProvider implements InventoryIntegrationProvider {
        @Override
        public String providerId() {
            return "curios:test";
        }

        @Override
        public ProviderResult<InventoryHostSession> openHost(InventoryHostContext context) {
            return ProviderResult.unsupported(providerId(), "unsupported_host", "Curios-style provider contributes player extensions only");
        }

        @Override
        public List<PlayerInventoryExtension> playerExtensions(PlayerInventoryContext context) {
            return List.of(new PlayerInventoryExtension() {
                @Override
                public String providerId() {
                    return "curios:test";
                }

                @Override
                public List<InventorySourceDescriptor> additionalSources() {
                    return List.of(
                            InventorySourceDescriptor.builder("curios:utility_lane")
                                    .label(Component.literal("Utility"))
                                    .domain(InventorySourceDomain.PLAYER_EXTENSION)
                                    .role(InventorySourceRole.QUICK_ACCESS)
                                    .laneId("curios:utility_lane")
                                    .logicalSlotCount(2)
                                    .bindingRoute(InventoryBindingRoute.PROVIDER)
                                    .capabilities(Set.of(
                                            InventoryCapability.INSERT,
                                            InventoryCapability.EXTRACT,
                                            InventoryCapability.QUICK_ACCESS_ASSIGN
                                    ))
                                    .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                                    .paneMembership(InventoryPaneMembership.CARRIED)
                                    .build(),
                            InventorySourceDescriptor.builder("curios:charm_slots")
                                    .label(Component.literal("Charms"))
                                    .domain(InventorySourceDomain.PLAYER_EXTENSION)
                                    .role(InventorySourceRole.EQUIPMENT)
                                    .groupId("curios:charms")
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
                            "curios:utility_lane",
                            Component.literal("Utility"),
                            "curios:utility_lane",
                            2,
                            Set.of(
                                    InventoryCapability.INSERT,
                                    InventoryCapability.EXTRACT,
                                    InventoryCapability.QUICK_ACCESS_ASSIGN
                            ),
                            "",
                            200
                    ));
                }

                @Override
                public List<EquipmentGroupDescriptor> additionalEquipmentGroups() {
                    return List.of(new EquipmentGroupDescriptor(
                            "curios:charms",
                            Component.literal("Charms"),
                            "curios:charm_slots",
                            2,
                            Set.of(
                                    InventoryCapability.INSERT,
                                    InventoryCapability.EXTRACT,
                                    InventoryCapability.EQUIP,
                                    InventoryCapability.UNEQUIP
                            ),
                            "",
                            200
                    ));
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
