package dev.imagio.slot.inventory.integration;

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
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.registry.ProviderResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MenuBackedInventoryIntegrationProvider implements InventoryIntegrationProvider {
    private static final String PROVIDER_ID = "menu_backed";
    private static final String PRIMARY_SOURCE_ID = "host.menu.primary";

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public ProviderResult<InventoryHostSession> openHost(InventoryHostContext context) {
        if (context == null || context.menu() == null || context.playerInventory() == null) {
            return ProviderResult.unsupported(providerId(), "missing_context", "Menu-backed host context was missing");
        }

        int containerSlotCount = MenuBackedHostSupport.inferSupportedStorageSlots(
                context.menu(),
                context.playerInventory(),
                context.screenClassName()
        );
        if (containerSlotCount <= 0) {
            return ProviderResult.unsupported(
                    providerId(),
                    "unsupported_menu",
                    "Menu does not expose supported menu-backed storage slots"
            );
        }

        String label = context.title() == null || context.title().getString().isBlank()
                ? Component.translatable("slot.source.open_container").getString()
                : context.title().getString();
        InventorySourceDescriptor primarySource = InventorySourceDescriptor.builder(PRIMARY_SOURCE_ID)
                .label(Component.literal(label))
                .domain(InventorySourceDomain.HOST_STORAGE)
                .role(InventorySourceRole.PRIMARY_STORAGE)
                .groupId("primary")
                .logicalSlotCount(containerSlotCount)
                .bindingRoute(InventoryBindingRoute.MENU)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.MENU_MUTATION)
                .paneMembership(InventoryPaneMembership.EXTERNAL)
                .stableOrder(0)
                .build();
        List<Integer> primaryMenuSlots = MenuBackedHostSupport.slotRange(
                0,
                Math.min(containerSlotCount, context.menu().slots.size()) - 1
        );

        return ProviderResult.supported(new InventoryHostSession() {
            @Override
            public String providerId() {
                return PROVIDER_ID;
            }

            @Override
            public List<InventorySourceDescriptor> hostSources() {
                return List.of(primarySource);
            }

            @Override
            public InventoryTopologyDescriptor topology() {
                return new InventoryTopologyDescriptor(
                        Map.of(primarySource.id(), primaryMenuSlots),
                        MenuBackedHostSupport.sourceIdsByMenuSlot(primarySource.id(), primaryMenuSlots),
                        Map.of()
                );
            }

            @Override
            public InventorySourceSnapshot readSourceSnapshot(InventoryHostDescriptor host, String sourceId) {
                if (!primarySource.id().equals(sourceId) || host == null) {
                    return InventorySourceSnapshot.empty(sourceId == null || sourceId.isBlank() ? "__missing__" : sourceId);
                }
                return MenuBackedHostSupport.readSourceSnapshot(host.menu(), sourceId, primaryMenuSlots);
            }

            @Override
            public MutationResult mutate(
                    InventoryHostDescriptor host,
                    InventoryMutationRequest request,
                    InventoryMutationMode mode
            ) {
                if (request == null || !primarySource.id().equals(request.sourceId())) {
                    return MutationResult.blocked("unsupported_source", request == null ? null : request.stack());
                }
                return MenuBackedHostSupport.mutateMenuSlots(host, request, mode, primaryMenuSlots);
            }
        });
    }
}
