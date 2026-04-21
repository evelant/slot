package dev.imagio.slot.atlas;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public final class CameraHistory<T> {
    public static final int DEFAULT_MAX_ENTRIES = 20;

    private final Deque<T> back = new ArrayDeque<>();
    private final Deque<T> forward = new ArrayDeque<>();
    private final int maxEntries;

    public CameraHistory() {
        this(DEFAULT_MAX_ENTRIES);
    }

    public CameraHistory(int maxEntries) {
        if (maxEntries < 1) {
            throw new IllegalArgumentException("maxEntries must be >= 1");
        }
        this.maxEntries = maxEntries;
    }

    public void recordCommit(T previousCamera) {
        if (previousCamera == null) {
            return;
        }
        forward.clear();
        back.push(previousCamera);
        while (back.size() > maxEntries) {
            back.removeLast();
        }
    }

    public Optional<T> back(T currentCamera) {
        if (back.isEmpty()) {
            return Optional.empty();
        }
        if (currentCamera != null) {
            forward.push(currentCamera);
            while (forward.size() > maxEntries) {
                forward.removeLast();
            }
        }
        return Optional.of(back.pop());
    }

    public Optional<T> forward(T currentCamera) {
        if (forward.isEmpty()) {
            return Optional.empty();
        }
        if (currentCamera != null) {
            back.push(currentCamera);
            while (back.size() > maxEntries) {
                back.removeLast();
            }
        }
        return Optional.of(forward.pop());
    }

    public void clear() {
        back.clear();
        forward.clear();
    }

    public int backSize() {
        return back.size();
    }

    public int forwardSize() {
        return forward.size();
    }

    public boolean canGoBack() {
        return !back.isEmpty();
    }

    public boolean canGoForward() {
        return !forward.isEmpty();
    }
}
