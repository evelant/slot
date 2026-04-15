package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolToggleId;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryMutationRouterTest {
    @Test
    void routerDelegatesToHostSessionAndExtensionsAndRoutesBuiltinPlayerSourcesThroughCoreExecutor() {
        InventorySourceDescriptor hostSource = source(
                "host.storage.a",
                InventorySourceDomain.HOST_STORAGE,
                InventorySourceRole.PRIMARY_STORAGE,
                InventoryActionRoute.PROVIDER_MUTATION,
                InventoryBindingRoute.MENU
        );
        InventorySourceDescriptor extensionSource = source(
                "player.extension.backpack",
                InventorySourceDomain.PLAYER_EXTENSION,
                InventorySourceRole.PROVIDER_DEFINED,
                InventoryActionRoute.PROVIDER_MUTATION,
                InventoryBindingRoute.PROVIDER
        );
        InventorySourceDescriptor builtinPlayerSource = source(
                "player.main",
                InventorySourceDomain.PLAYER,
                InventorySourceRole.MAIN,
                InventoryActionRoute.PLAYER_MUTATION,
                InventoryBindingRoute.PLAYER
        );

        TestHostSession hostSession = new TestHostSession(hostSource);
        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "test.screen",
                Component.literal("Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                hostSession,
                List.of(new TestExtension(extensionSource.id())),
                PlayerRuntimeStateDescriptor.vanilla(0),
                List.of(hostSource, extensionSource, builtinPlayerSource),
                List.of(),
                List.of(),
                List.of(),
                dev.imagio.slot.inventory.integration.InventoryHostObservationHints.defaults(),
                ""
        );

        MutationResult hostMutation = InventoryMutationRouter.mutate(
                host,
                InventoryMutationRequest.insert(host, null, hostSource.id(), ItemStack.EMPTY),
                InventoryMutationMode.EXECUTE
        );
        MutationResult extensionMutation = InventoryMutationRouter.mutate(
                host,
                InventoryMutationRequest.insert(host, null, extensionSource.id(), ItemStack.EMPTY),
                InventoryMutationMode.EXECUTE
        );
        MutationResult builtinMutation = InventoryMutationRouter.mutate(
                host,
                InventoryMutationRequest.insert(host, null, builtinPlayerSource.id(), ItemStack.EMPTY),
                InventoryMutationMode.EXECUTE
        );
        ToolActionResult activation = InventoryMutationRouter.activateTool(host, "tool:test", InventoryActionMode.EXECUTE);
        ToolActionResult action = InventoryMutationRouter.executeToolAction(host, "tool:test", InventoryToolActionId.CLEAR_GRID, InventoryActionMode.EXECUTE);
        ToolActionResult toggle = InventoryMutationRouter.setToolToggle(host, "tool:test", InventoryToolToggleId.AUTO_REFILL, true, InventoryActionMode.EXECUTE);

        assertTrue(hostMutation.successful());
        assertTrue(extensionMutation.successful());
        assertTrue(builtinMutation.successful());
        assertTrue(activation.successful());
        assertTrue(action.successful());
        assertTrue(toggle.successful());
    }

    private static InventorySourceDescriptor source(
            String id,
            InventorySourceDomain domain,
            InventorySourceRole role,
            InventoryActionRoute actionRoute,
            InventoryBindingRoute bindingRoute
    ) {
        return InventorySourceDescriptor.builder(id)
                .label(Component.literal(id))
                .domain(domain)
                .role(role)
                .logicalSlotCount(4)
                .bindingRoute(bindingRoute)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(actionRoute)
                .paneMembership(domain == InventorySourceDomain.HOST_STORAGE
                        ? InventoryPaneMembership.EXTERNAL
                        : InventoryPaneMembership.CARRIED)
                .build();
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

    private record TestHostSession(InventorySourceDescriptor source) implements InventoryHostSession {
        @Override
        public String providerId() {
            return "test";
        }

        @Override
        public List<InventorySourceDescriptor> hostSources() {
            return List.of(source);
        }

        @Override
        public MutationResult mutate(
                InventoryHostDescriptor host,
                InventoryMutationRequest request,
                InventoryMutationMode mode
        ) {
            return source.id().equals(request.sourceId())
                    ? MutationResult.success(ItemStack.EMPTY)
                    : MutationResult.blocked("wrong_source", ItemStack.EMPTY);
        }

        @Override
        public ToolActionResult activateTool(InventoryHostDescriptor host, String toolId, InventoryActionMode mode) {
            return ToolActionResult.success();
        }

        @Override
        public ToolActionResult executeToolAction(
                InventoryHostDescriptor host,
                String toolId,
                InventoryToolActionId actionId,
                InventoryActionMode mode
        ) {
            return ToolActionResult.success();
        }

        @Override
        public ToolActionResult setToolToggle(
                InventoryHostDescriptor host,
                String toolId,
                InventoryToolToggleId toggleId,
                boolean enabled,
                InventoryActionMode mode
        ) {
            return ToolActionResult.success();
        }
    }

    private record TestExtension(String sourceId) implements PlayerInventoryExtension {
        @Override
        public String providerId() {
            return "test:extension";
        }

        @Override
        public List<InventorySourceDescriptor> additionalSources() {
            return List.of(InventorySourceDescriptor.builder(sourceId)
                    .label(Component.literal(sourceId))
                    .domain(InventorySourceDomain.PLAYER_EXTENSION)
                    .role(InventorySourceRole.PROVIDER_DEFINED)
                    .logicalSlotCount(4)
                    .bindingRoute(InventoryBindingRoute.PROVIDER)
                    .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                    .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                    .paneMembership(InventoryPaneMembership.CARRIED)
                    .build());
        }

        @Override
        public MutationResult mutate(
                InventoryHostDescriptor host,
                InventoryMutationRequest request,
                InventoryMutationMode mode
        ) {
            return sourceId.equals(request.sourceId())
                    ? MutationResult.success(ItemStack.EMPTY)
                    : MutationResult.blocked("wrong_extension_source", ItemStack.EMPTY);
        }
    }
}
