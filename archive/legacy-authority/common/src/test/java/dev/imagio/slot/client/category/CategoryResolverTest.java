package dev.imagio.slot.client.category;

import dev.imagio.slot.client.model.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoryResolverTest {
    @Test
    void exactOverrideWinsOverSignals() {
        CategoryResolver resolver = new CategoryResolver()
                .addExactOverride("test:widget", SlotCategory.COMPONENTS);

        CategoryResolver.Resolution resolution = resolver.resolveDetailed(new CategorySubject(
                ItemIdentity.of("test:widget"),
                EnumSet.of(CategorySignal.STORAGE, CategorySignal.TOOL)
        ));

        assertEquals(SlotCategory.COMPONENTS, resolution.category());
        assertEquals(CategoryResolver.ResolutionSource.EXACT_OVERRIDE, resolution.source());
    }

    @Test
    void namespaceDefaultWinsOverSignals() {
        CategoryResolver resolver = new CategoryResolver()
                .addNamespaceDefault("test", SlotCategory.DECORATION);

        CategoryResolver.Resolution resolution = resolver.resolveDetailed(new CategorySubject(
                ItemIdentity.of("test:widget"),
                EnumSet.of(CategorySignal.STORAGE)
        ));

        assertEquals(SlotCategory.DECORATION, resolution.category());
        assertEquals(CategoryResolver.ResolutionSource.NAMESPACE_DEFAULT, resolution.source());
    }

    @Test
    void storageSignalBeatsLaterSignals() {
        CategoryResolver resolver = new CategoryResolver();

        CategoryResolver.Resolution resolution = resolver.resolveDetailed(new CategorySubject(
                ItemIdentity.of("test:widget"),
                EnumSet.of(CategorySignal.STORAGE, CategorySignal.MATERIAL)
        ));

        assertEquals(SlotCategory.STORAGE_AND_TRANSPORT, resolution.category());
        assertEquals(CategoryResolver.ResolutionSource.STORAGE_SIGNAL, resolution.source());
    }

    @Test
    void noSignalsFallsBackToMisc() {
        CategoryResolver resolver = new CategoryResolver();

        CategoryResolver.Resolution resolution = resolver.resolveDetailed(new CategorySubject(
                ItemIdentity.of("test:widget"),
                EnumSet.noneOf(CategorySignal.class)
        ));

        assertEquals(SlotCategory.MISC, resolution.category());
        assertEquals(CategoryResolver.ResolutionSource.FALLBACK_MISC, resolution.source());
    }
}
