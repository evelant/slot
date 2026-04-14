package dev.imagio.slot.storage.provider;

import dev.imagio.slot.registry.ProviderResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class StorageViewProviderRegistry {
    private static final List<StorageViewProvider> PROVIDERS = new ArrayList<>();
    private static boolean bootstrapped;

    private StorageViewProviderRegistry() {
    }

    public static ProviderResult<StorageViewProviderSession> resolve(StorageViewProviderContext context) {
        if (!bootstrapped()) {
            return ProviderResult.error(
                    "storage_view_registry",
                    "not_bootstrapped",
                    "Storage view providers were not bootstrapped"
            );
        }
        if (context == null) {
            return ProviderResult.unsupported(
                    "storage_view_registry",
                    "missing_context",
                    "Storage view provider context was missing"
            );
        }

        ProviderResult<StorageViewProviderSession> unsupported = ProviderResult.unsupported(
                "storage_view_registry",
                "unsupported_menu",
                "No storage view provider matched the active menu"
        );
        for (StorageViewProvider provider : providers()) {
            ProviderResult<StorageViewProviderSession> resolution = provider.createSession(context);
            if (resolution == null) {
                continue;
            }
            if (resolution.status() == ProviderResult.Status.SUPPORTED) {
                return resolution;
            }
            if (resolution.status() == ProviderResult.Status.ERROR) {
                return resolution;
            }
            unsupported = resolution;
        }
        return unsupported;
    }

    public static synchronized void clear() {
        PROVIDERS.clear();
        bootstrapped = false;
    }

    public static synchronized void register(StorageViewProvider provider) {
        if (provider == null) {
            return;
        }
        PROVIDERS.removeIf(existing -> existing.providerId().equals(provider.providerId()));
        PROVIDERS.add(provider);
        PROVIDERS.sort(Comparator
                .comparingInt(StorageViewProvider::priority)
                .reversed()
                .thenComparing(StorageViewProvider::providerId));
    }

    public static synchronized List<StorageViewProvider> providers() {
        return List.copyOf(PROVIDERS);
    }

    public static synchronized void markBootstrapped() {
        bootstrapped = true;
    }

    public static synchronized boolean bootstrapped() {
        return bootstrapped;
    }

}
