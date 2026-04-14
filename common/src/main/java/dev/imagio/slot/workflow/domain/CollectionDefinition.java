package dev.imagio.slot.workflow.domain;

public record CollectionDefinition(
        String id,
        String name,
        boolean builtIn
) {
    public CollectionDefinition {
        id = id == null ? "" : id;
        name = name == null ? "" : name;
    }
}
