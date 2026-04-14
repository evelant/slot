package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;

import java.util.Optional;

public final class NewCollectionPromptState {
    private ItemIdentity identity;

    public void begin(ItemIdentity identity) {
        if (identity == null) {
            return;
        }
        this.identity = identity;
    }

    public boolean isActive() {
        return identity != null;
    }

    public ItemIdentity identity() {
        return identity;
    }

    public Optional<Commit> commit(String rawValue) {
        if (!isActive()) {
            return Optional.empty();
        }

        Commit commit = new Commit(identity, rawValue == null ? "" : rawValue.trim());
        cancel();
        return Optional.of(commit);
    }

    public void cancel() {
        identity = null;
    }

    public record Commit(ItemIdentity identity, String name) {
    }
}
