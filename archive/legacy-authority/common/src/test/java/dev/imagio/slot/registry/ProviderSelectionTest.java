package dev.imagio.slot.registry;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProviderSelectionTest {
    @Test
    void returnsFirstSupportedProvider() {
        ProviderResult<String> result = ProviderSelection.firstSupported(
                List.of(
                        new FakeProvider("unsupported", ProviderResult.unsupported("unsupported", "no_match", "Did not match")),
                        new FakeProvider("supported", ProviderResult.supported("value")),
                        new FakeProvider("later", ProviderResult.supported("later"))
                ),
                FakeProvider::result,
                FakeProvider::id,
                "registry",
                "empty",
                "No provider"
        );

        assertEquals(ProviderResult.Status.SUPPORTED, result.status());
        assertEquals("value", result.value());
    }

    @Test
    void nullResultFailsClosed() {
        ProviderResult<String> result = ProviderSelection.firstSupported(
                List.of(new FakeProvider("broken", null)),
                FakeProvider::result,
                FakeProvider::id,
                "registry",
                "empty",
                "No provider"
        );

        assertEquals(ProviderResult.Status.ERROR, result.status());
        assertEquals("broken", result.diagnostics().providerId());
        assertEquals("null_result", result.diagnostics().reasonCode());
    }

    @Test
    void returnsLastUnsupportedDiagnosticWhenNothingSupports() {
        ProviderResult<String> result = ProviderSelection.firstSupported(
                List.of(
                        new FakeProvider("first", ProviderResult.unsupported("first", "unsupported_a", "A")),
                        new FakeProvider("second", ProviderResult.unsupported("second", "unsupported_b", "B"))
                ),
                FakeProvider::result,
                FakeProvider::id,
                "registry",
                "empty",
                "No provider"
        );

        assertEquals(ProviderResult.Status.UNSUPPORTED, result.status());
        assertEquals("second", result.diagnostics().providerId());
        assertEquals("unsupported_b", result.diagnostics().reasonCode());
    }

    private record FakeProvider(String id, ProviderResult<String> result) {
    }
}
