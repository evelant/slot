package dev.imagio.slot.storage.provider;

import dev.imagio.slot.registry.ProviderResult;

public interface StorageViewProvider {
    String providerId();

    default int priority() {
        return 0;
    }

    ProviderResult<StorageViewProviderSession> createSession(StorageViewProviderContext context);
}
