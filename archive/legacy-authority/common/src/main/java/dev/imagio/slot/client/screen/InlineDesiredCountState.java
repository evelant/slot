package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;

import java.util.Objects;
import java.util.Optional;

public final class InlineDesiredCountState {
    private String collectionId;
    private ItemIdentity identity;
    private boolean laidOut;

    public void begin(String collectionId, ItemIdentity identity) {
        if (collectionId == null || identity == null) {
            return;
        }
        this.collectionId = collectionId;
        this.identity = identity;
        this.laidOut = false;
    }

    public void beginFrame() {
        laidOut = false;
    }

    public void markLaidOut() {
        laidOut = true;
    }

    public boolean isActive() {
        return collectionId != null && identity != null;
    }

    public boolean shouldCancelAfterLayout() {
        return isActive() && !laidOut;
    }

    public boolean isTarget(String collectionId, ItemIdentity identity) {
        return identity != null
                && Objects.equals(this.collectionId, collectionId)
                && Objects.equals(this.identity, identity);
    }

    public boolean targetsCollection(String collectionId) {
        return Objects.equals(this.collectionId, collectionId);
    }

    public String collectionId() {
        return collectionId;
    }

    public ItemIdentity identity() {
        return identity;
    }

    public Optional<Commit> commit(String rawValue) {
        if (!isActive()) {
            return Optional.empty();
        }

        String trimmed = rawValue == null ? "" : rawValue.trim();
        int desiredCount = trimmed.isEmpty() ? 1 : Math.max(1, Integer.parseInt(trimmed));
        Commit commit = new Commit(collectionId, identity, desiredCount);
        cancel();
        return Optional.of(commit);
    }

    public void cancel() {
        collectionId = null;
        identity = null;
        laidOut = false;
    }

    public record Commit(String collectionId, ItemIdentity identity, int desiredCount) {
    }
}
