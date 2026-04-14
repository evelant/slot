package dev.imagio.slot.registry;

public record ProviderDiagnostics(
        String providerId,
        String reasonCode,
        String summary
) {
    public static final ProviderDiagnostics NONE = new ProviderDiagnostics("", "", "");

    public ProviderDiagnostics {
        providerId = providerId == null ? "" : providerId;
        reasonCode = reasonCode == null ? "" : reasonCode;
        summary = summary == null ? "" : summary;
    }

    public boolean present() {
        return !providerId.isBlank() || !reasonCode.isBlank() || !summary.isBlank();
    }
}
