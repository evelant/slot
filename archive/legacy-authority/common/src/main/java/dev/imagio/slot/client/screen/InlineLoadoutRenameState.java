package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class InlineLoadoutRenameState {
    private String collectionId;
    private String loadoutId;
    private boolean laidOut;

    public void begin(String collectionId, HotbarLoadoutDefinition loadout) {
        if (collectionId == null || loadout == null) {
            return;
        }
        this.collectionId = collectionId;
        this.loadoutId = loadout.id();
        this.laidOut = false;
    }

    public void beginFrame() {
        laidOut = false;
    }

    public void markLaidOut() {
        laidOut = true;
    }

    public boolean isActive() {
        return collectionId != null && loadoutId != null;
    }

    public boolean shouldCancelAfterLayout() {
        return isActive() && !laidOut;
    }

    public boolean isTarget(String collectionId, HotbarLoadoutDefinition loadout) {
        return loadout != null
                && Objects.equals(this.collectionId, collectionId)
                && Objects.equals(this.loadoutId, loadout.id());
    }

    public boolean targetsCollection(String collectionId) {
        return Objects.equals(this.collectionId, collectionId);
    }

    public boolean matchesSelectedLoadout(Function<String, HotbarLoadoutDefinition> selectedLoadoutResolver) {
        if (!isActive()) {
            return false;
        }
        HotbarLoadoutDefinition selected = selectedLoadoutResolver.apply(collectionId);
        return selected != null && Objects.equals(selected.id(), loadoutId);
    }

    public Optional<Commit> commit(String rawValue) {
        if (!isActive()) {
            return Optional.empty();
        }

        Commit commit = new Commit(collectionId, loadoutId, rawValue == null ? "" : rawValue.trim());
        cancel();
        return Optional.of(commit);
    }

    public void cancel() {
        collectionId = null;
        loadoutId = null;
        laidOut = false;
    }

    public record Commit(String collectionId, String loadoutId, String newName) {
    }
}
