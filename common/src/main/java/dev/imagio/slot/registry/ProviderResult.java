package dev.imagio.slot.registry;

import java.util.Objects;
import java.util.Optional;

public record ProviderResult<T>(
        Status status,
        T value,
        ProviderDiagnostics diagnostics
) {
    public ProviderResult {
        Objects.requireNonNull(status, "status");
        diagnostics = diagnostics == null ? ProviderDiagnostics.NONE : diagnostics;
        if (status == Status.SUPPORTED && value == null) {
            throw new IllegalArgumentException("supported result requires a value");
        }
        if (status != Status.SUPPORTED && value != null) {
            throw new IllegalArgumentException("unsupported/error result must not include a value");
        }
    }

    public static <T> ProviderResult<T> supported(T value) {
        return new ProviderResult<>(Status.SUPPORTED, value, ProviderDiagnostics.NONE);
    }

    public static <T> ProviderResult<T> supported(T value, ProviderDiagnostics diagnostics) {
        return new ProviderResult<>(Status.SUPPORTED, value, diagnostics);
    }

    public static <T> ProviderResult<T> unsupported(String providerId, String reasonCode, String summary) {
        return new ProviderResult<>(Status.UNSUPPORTED, null, new ProviderDiagnostics(providerId, reasonCode, summary));
    }

    public static <T> ProviderResult<T> error(String providerId, String reasonCode, String summary) {
        return new ProviderResult<>(Status.ERROR, null, new ProviderDiagnostics(providerId, reasonCode, summary));
    }

    public boolean supported() {
        return status == Status.SUPPORTED;
    }

    public boolean unsupported() {
        return status == Status.UNSUPPORTED;
    }

    public boolean error() {
        return status == Status.ERROR;
    }

    public Optional<T> resolved() {
        return Optional.ofNullable(value);
    }

    public enum Status {
        SUPPORTED,
        UNSUPPORTED,
        ERROR
    }
}
