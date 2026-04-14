package dev.imagio.slot.storage.provider;

import dev.imagio.slot.session.InventoryHostDescriptor;
import net.minecraft.client.player.LocalPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SupplementalCarriedSourceProviderRegistry {
    private static final List<SupplementalCarriedSourceProvider> PROVIDERS = new ArrayList<>();
    private static boolean bootstrapped;

    private SupplementalCarriedSourceProviderRegistry() {
    }

    public static List<SupplementalCarriedSourceDescriptor> describe(InventoryHostDescriptor host) {
        ensureBootstrapped();
        if (host == null) {
            return List.of();
        }

        return uniqueDescriptors(providers(), provider -> provider.describe(host));
    }

    public static List<SupplementalCarriedSourceDescriptor> describeDefault(Set<String> sourceIds) {
        ensureBootstrapped();
        if (sourceIds == null || sourceIds.isEmpty()) {
            return List.of();
        }

        return uniqueDescriptors(providers(), provider -> provider.describeDefault(sourceIds));
    }

    private static List<SupplementalCarriedSourceDescriptor> uniqueDescriptors(
            List<SupplementalCarriedSourceProvider> providers,
            java.util.function.Function<SupplementalCarriedSourceProvider, List<SupplementalCarriedSourceDescriptor>> loader
    ) {
        Map<String, SupplementalCarriedSourceDescriptor> descriptorsByKey = new LinkedHashMap<>();
        for (SupplementalCarriedSourceProvider provider : providers) {
            for (SupplementalCarriedSourceDescriptor descriptor : loader.apply(provider)) {
                if (descriptor == null) {
                    continue;
                }
                descriptorsByKey.putIfAbsent(descriptor.providerId() + "|" + descriptor.sourceId(), descriptor);
            }
        }
        return List.copyOf(descriptorsByKey.values());
    }

    public static List<SupplementalCarriedStackSnapshot> readSnapshots(
            LocalPlayer player,
            InventoryHostDescriptor host,
            String sourceId
    ) {
        if (player == null || host == null || sourceId == null || sourceId.isBlank()) {
            return List.of();
        }

        List<SupplementalCarriedStackSnapshot> snapshots = new ArrayList<>();
        for (SupplementalCarriedSourceDescriptor descriptor : host.supplementalCarriedSources()) {
            if (!descriptor.matchesSource(sourceId)) {
                continue;
            }
            SupplementalCarriedSourceProvider provider = provider(descriptor.providerId());
            if (provider == null) {
                continue;
            }
            snapshots.addAll(provider.readSnapshots(player, host, descriptor));
        }
        return List.copyOf(snapshots);
    }

    public static List<SupplementalCarriedStackSnapshot> readSnapshots(
            LocalPlayer player,
            String sourceId,
            String referenceKey
    ) {
        if (player == null || sourceId == null || sourceId.isBlank()) {
            return List.of();
        }

        List<SupplementalCarriedStackSnapshot> snapshots = new ArrayList<>();
        for (SupplementalCarriedSourceDescriptor descriptor : describeDefault(Set.of(sourceId))) {
            if (!descriptor.matchesSource(sourceId)) {
                continue;
            }
            SupplementalCarriedSourceProvider provider = provider(descriptor.providerId());
            if (provider == null) {
                continue;
            }
            SupplementalCarriedSourceDescriptor resolvedDescriptor = new SupplementalCarriedSourceDescriptor(
                    descriptor.providerId(),
                    descriptor.sourceId(),
                    referenceKey == null ? "" : referenceKey,
                    descriptor.sourceDescriptor()
            );
            snapshots.addAll(provider.readSnapshots(player, null, resolvedDescriptor));
        }
        return List.copyOf(snapshots);
    }

    public static int slotCapacity(
            LocalPlayer player,
            InventoryHostDescriptor host,
            String sourceId
    ) {
        if (player == null || host == null || sourceId == null || sourceId.isBlank()) {
            return 0;
        }

        int capacity = 0;
        for (SupplementalCarriedSourceDescriptor descriptor : host.supplementalCarriedSources()) {
            if (!descriptor.matchesSource(sourceId)) {
                continue;
            }
            SupplementalCarriedSourceProvider provider = provider(descriptor.providerId());
            if (provider == null) {
                continue;
            }
            capacity += Math.max(0, provider.slotCapacity(player, host, descriptor));
        }
        return capacity;
    }

    private static SupplementalCarriedSourceProvider provider(String providerId) {
        for (SupplementalCarriedSourceProvider provider : providers()) {
            if (provider.providerId().equals(providerId)) {
                return provider;
            }
        }
        return null;
    }

    public static synchronized void clear() {
        PROVIDERS.clear();
        bootstrapped = false;
    }

    public static synchronized void register(SupplementalCarriedSourceProvider provider) {
        if (provider == null) {
            return;
        }
        PROVIDERS.removeIf(existing -> existing.providerId().equals(provider.providerId()));
        PROVIDERS.add(provider);
        PROVIDERS.sort(Comparator
                .comparingInt(SupplementalCarriedSourceProvider::priority)
                .reversed()
                .thenComparing(SupplementalCarriedSourceProvider::providerId));
    }

    public static synchronized List<SupplementalCarriedSourceProvider> providers() {
        ensureBootstrapped();
        return List.copyOf(PROVIDERS);
    }

    public static synchronized void markBootstrapped() {
        bootstrapped = true;
    }

    public static synchronized boolean bootstrapped() {
        return bootstrapped;
    }

    private static synchronized void ensureBootstrapped() {
        if (!bootstrapped) {
            bootstrapped = true;
        }
    }
}
