package dev.imagio.slot.inventory.storage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry of {@link CarriedProvider}s. Providers register at mod init; the
 * platform {@link CarriedSourceAccess} implementation dispatches non-builtin
 * source ids through this registry. Registration order defines walk order
 * (first-registered wins for ambiguous prefixes, first-registered is first
 * seen in find / insert iterations).
 */
public final class CarriedProviderRegistry {

    private static final List<CarriedProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private CarriedProviderRegistry() {
    }

    public static void register(CarriedProvider provider) {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(provider.prefix(), "provider.prefix()");
        PROVIDERS.add(provider);
    }

    public static List<CarriedProvider> all() {
        return List.copyOf(PROVIDERS);
    }

    /** First registered provider whose {@link CarriedProvider#handles(String)} returns true. */
    public static Optional<CarriedProvider> forSource(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return Optional.empty();
        }
        for (CarriedProvider p : PROVIDERS) {
            if (p.handles(sourceId)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    public static void resetForTests() {
        PROVIDERS.clear();
    }
}
