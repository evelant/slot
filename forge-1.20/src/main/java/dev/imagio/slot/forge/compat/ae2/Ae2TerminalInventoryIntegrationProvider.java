package dev.imagio.slot.forge.compat.ae2;

import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.integration.InventoryIntegrationProvider;
import dev.imagio.slot.inventory.integration.InventoryMutationMode;
import dev.imagio.slot.inventory.integration.InventoryMutationRequest;
import dev.imagio.slot.inventory.integration.InventoryMutationKind;
import dev.imagio.slot.inventory.integration.MutationResult;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.registry.ProviderResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Ae2TerminalInventoryIntegrationProvider implements InventoryIntegrationProvider {
    private static final String ITEM_TERMINAL_MENU = "appeng.menu.me.common.MEStorageMenu";
    private static final String CRAFTING_TERMINAL_MENU = "appeng.menu.me.items.CraftingTermMenu";
    private static final String WIRELESS_CRAFTING_TERMINAL_MENU =
            "appeng.menu.me.items.WirelessCraftingTermMenu";

    @Override
    public String providerId() {
        return Ae2StorageBridge.PROVIDER_ID;
    }

    @Override
    public int priority() {
        return 200;
    }

    @Override
    public ProviderResult<InventoryHostSession> openHost(InventoryHostContext context) {
        if (context == null || context.menu() == null || !isSupportedMenu(context.menu())) {
            return ProviderResult.unsupported(providerId(), "unsupported_menu", "Menu is not a supported AE2 terminal");
        }
        Object terminalHost = terminalHost(context.menu());
        if (!Ae2StorageBridge.isSupportedOpenTerminalHost(terminalHost)) {
            return ProviderResult.unsupported(
                    providerId(),
                    "unsupported_terminal_host",
                    "AE2 terminal host is not a supported item/crafting/wireless terminal");
        }
        InventorySourceDescriptor primarySource = InventorySourceDescriptor.builder(Ae2StorageBridge.PRIMARY_SOURCE_ID)
                .label(label(context))
                .domain(InventorySourceDomain.HOST_STORAGE)
                .role(InventorySourceRole.PRIMARY_STORAGE)
                .groupId("ae2")
                .logicalSlotCount(0)
                .bindingRoute(InventoryBindingRoute.PROVIDER)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .simulationSupported(true)
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.EXTERNAL)
                .stableOrder(0)
                .build();

        return ProviderResult.supported(new InventoryHostSession() {
            @Override
            public String providerId() {
                return Ae2StorageBridge.PROVIDER_ID;
            }

            @Override
            public String providerScopeId() {
                String menuName = context.menu().getClass().getName();
                if (WIRELESS_CRAFTING_TERMINAL_MENU.equals(menuName)) {
                    return "wireless_crafting_terminal";
                }
                if (CRAFTING_TERMINAL_MENU.equals(menuName)) {
                    return "crafting_terminal";
                }
                return "item_terminal";
            }

            @Override
            public List<InventorySourceDescriptor> hostSources() {
                return List.of(primarySource);
            }

            @Override
            public InventoryTopologyDescriptor topology() {
                return new InventoryTopologyDescriptor(Map.of(), Map.of(), Map.of());
            }

            @Override
            public InventorySourceSnapshot readSourceSnapshot(InventoryHostDescriptor host, String sourceId) {
                if (!Ae2StorageBridge.PRIMARY_SOURCE_ID.equals(sourceId)) {
                    return InventorySourceSnapshot.empty(sourceId == null || sourceId.isBlank() ? "__missing__" : sourceId);
                }
                return Ae2StorageBridge.sourceSnapshot(
                        sourceId,
                        Ae2StorageBridge.endpoint(terminalHost(host == null ? null : host.menu())).orElse(null));
            }

            @Override
            public MutationResult mutate(
                    InventoryHostDescriptor host,
                    InventoryMutationRequest request,
                    InventoryMutationMode mode
            ) {
                if (request == null || !Ae2StorageBridge.PRIMARY_SOURCE_ID.equals(request.sourceId())) {
                    return MutationResult.blocked("unsupported_source", request == null ? ItemStack.EMPTY : request.stack());
                }
                Ae2StorageBridge.Endpoint endpoint =
                        Ae2StorageBridge.endpoint(terminalHost(host == null ? null : host.menu())).orElse(null);
                if (endpoint == null) {
                    return MutationResult.blocked("ae2_terminal_unavailable", request.stack());
                }
                InventoryMutationMode resolvedMode = mode == null ? InventoryMutationMode.EXECUTE : mode;
                if (request.kind() == InventoryMutationKind.INSERT) {
                    return MutationResult.success(Ae2StorageBridge.insert(
                            endpoint,
                            request.player(),
                            request.stack(),
                            resolvedMode));
                }
                if (request.kind() == InventoryMutationKind.EXTRACT) {
                    return MutationResult.success(Ae2StorageBridge.extract(
                            endpoint,
                            request.player(),
                            request.entryId(),
                            request.identity(),
                            request.requestedCount(),
                            request.transferMode(),
                            resolvedMode));
                }
                return MutationResult.blocked("unsupported_mutation", request.stack());
            }
        });
    }

    private static boolean isSupportedMenu(AbstractContainerMenu menu) {
        if (menu == null) {
            return false;
        }
        String name = menu.getClass().getName();
        return ITEM_TERMINAL_MENU.equals(name)
                || CRAFTING_TERMINAL_MENU.equals(name)
                || WIRELESS_CRAFTING_TERMINAL_MENU.equals(name);
    }

    private static Object terminalHost(AbstractContainerMenu menu) {
        if (menu == null) {
            return null;
        }
        try {
            Method method = menu.getClass().getMethod("getHost");
            return method.invoke(menu);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static Component label(InventoryHostContext context) {
        if (context != null && context.title() != null && !context.title().getString().isBlank()) {
            return context.title();
        }
        return Component.literal("ME Network");
    }
}
