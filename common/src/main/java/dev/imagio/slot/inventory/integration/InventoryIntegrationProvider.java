package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.registry.ProviderResult;

import java.util.List;

public interface InventoryIntegrationProvider {
    String providerId();

    default int priority() {
        return 0;
    }

    ProviderResult<InventoryHostSession> openHost(InventoryHostContext context);

    default List<PlayerInventoryExtension> playerExtensions(PlayerInventoryContext context) {
        return List.of();
    }
}
