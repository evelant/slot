package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.registry.ProviderResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class InventoryIntegrationRegistry {
    private static final List<InventoryIntegrationProvider> PROVIDERS = new ArrayList<>();
    private static boolean bootstrapped;

    private InventoryIntegrationRegistry() {
    }

    public static synchronized void clear() {
        PROVIDERS.clear();
        bootstrapped = false;
    }

    public static synchronized void register(InventoryIntegrationProvider provider) {
        if (provider == null) {
            return;
        }
        PROVIDERS.removeIf(existing -> existing.providerId().equals(provider.providerId()));
        PROVIDERS.add(provider);
        PROVIDERS.sort(Comparator
                .comparingInt(InventoryIntegrationProvider::priority)
                .reversed()
                .thenComparing(InventoryIntegrationProvider::providerId));
    }

    public static synchronized List<InventoryIntegrationProvider> providers() {
        return List.copyOf(PROVIDERS);
    }

    public static synchronized void markBootstrapped() {
        bootstrapped = true;
    }

    public static synchronized boolean bootstrapped() {
        return bootstrapped;
    }

    public static ProviderResult<InventoryHostSession> openHost(InventoryHostContext context) {
        if (!bootstrapped()) {
            return ProviderResult.error(
                    "inventory_integration_registry",
                    "not_bootstrapped",
                    "Inventory integration providers were not bootstrapped"
            );
        }
        if (context == null || context.menu() == null || context.playerInventory() == null) {
            return ProviderResult.unsupported(
                    "inventory_integration_registry",
                    "missing_context",
                    "Inventory host context was missing"
            );
        }

        ProviderResult<InventoryHostSession> unsupported = ProviderResult.unsupported(
                "inventory_integration_registry",
                "unsupported_host",
                "No integration provider matched the active host"
        );
        for (InventoryIntegrationProvider provider : providers()) {
            ProviderResult<InventoryHostSession> result = provider.openHost(context);
            if (result == null) {
                continue;
            }
            if (result.status() == ProviderResult.Status.SUPPORTED) {
                return result;
            }
            if (result.status() == ProviderResult.Status.ERROR) {
                return result;
            }
            unsupported = result;
        }
        return unsupported;
    }

    public static List<PlayerInventoryExtension> playerExtensions(PlayerInventoryContext context) {
        if (!bootstrapped() || context == null || context.playerInventory() == null) {
            return List.of();
        }
        List<PlayerInventoryExtension> extensions = new ArrayList<>();
        for (InventoryIntegrationProvider provider : providers()) {
            List<PlayerInventoryExtension> provided = provider.playerExtensions(context);
            if (provided == null || provided.isEmpty()) {
                continue;
            }
            extensions.addAll(provided.stream().filter(extension -> extension != null).toList());
        }
        return List.copyOf(extensions);
    }
}
