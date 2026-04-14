package dev.imagio.slot.registry;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class ProviderSelection {
    private ProviderSelection() {
    }

    public static <P, T> ProviderResult<T> firstSupported(
            List<P> providers,
            Function<P, ProviderResult<T>> resolver,
            Function<P, String> providerIdResolver,
            String emptyProviderId,
            String emptyReasonCode,
            String emptySummary
    ) {
        Objects.requireNonNull(resolver, "resolver");
        Objects.requireNonNull(providerIdResolver, "providerIdResolver");

        ProviderResult<T> lastFailure = ProviderResult.unsupported(
                emptyProviderId,
                emptyReasonCode,
                emptySummary
        );
        for (P provider : providers == null ? List.<P>of() : providers) {
            ProviderResult<T> result = resolver.apply(provider);
            if (result == null) {
                lastFailure = ProviderResult.error(
                        safeProviderId(providerIdResolver.apply(provider)),
                        "null_result",
                        "Provider returned null instead of ProviderResult"
                );
                continue;
            }
            if (result.supported()) {
                return result;
            }
            if (result.diagnostics().present()) {
                lastFailure = result;
            }
        }
        return lastFailure;
    }

    private static String safeProviderId(String providerId) {
        return providerId == null ? "" : providerId;
    }
}
