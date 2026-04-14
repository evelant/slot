package dev.imagio.slot.client.collection;

public record CollectionDefinition(
        String id,
        String name,
        boolean builtIn,
        CollectionDisplayMode displayMode
) {
}
