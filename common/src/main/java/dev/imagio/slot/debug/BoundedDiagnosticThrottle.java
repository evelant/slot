package dev.imagio.slot.debug;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bounded per-key log throttle for diagnostics whose keys include world state.
 */
public final class BoundedDiagnosticThrottle {
    private final long intervalNanos;
    private final int maxKeys;
    private final ConcurrentHashMap<String, Long> lastEmitNanosByKey = new ConcurrentHashMap<>();

    public BoundedDiagnosticThrottle(long intervalNanos, int maxKeys) {
        if (intervalNanos <= 0L) {
            throw new IllegalArgumentException("intervalNanos must be positive");
        }
        if (maxKeys <= 0) {
            throw new IllegalArgumentException("maxKeys must be positive");
        }
        this.intervalNanos = intervalNanos;
        this.maxKeys = maxKeys;
    }

    public boolean shouldEmit(String key, long nowNanos) {
        String resolvedKey = key == null ? "" : key;
        Long previous = lastEmitNanosByKey.get(resolvedKey);
        if (previous != null && nowNanos - previous < intervalNanos) {
            return false;
        }
        lastEmitNanosByKey.put(resolvedKey, nowNanos);
        trim(nowNanos);
        return true;
    }

    public void clear() {
        lastEmitNanosByKey.clear();
    }

    int size() {
        return lastEmitNanosByKey.size();
    }

    private void trim(long nowNanos) {
        if (lastEmitNanosByKey.size() <= maxKeys) {
            return;
        }
        lastEmitNanosByKey.entrySet().removeIf(entry -> isExpired(nowNanos, entry));
        int overflow = lastEmitNanosByKey.size() - maxKeys;
        if (overflow <= 0) {
            return;
        }
        Iterator<String> iterator = lastEmitNanosByKey.keySet().iterator();
        while (overflow > 0 && iterator.hasNext()) {
            iterator.next();
            iterator.remove();
            overflow--;
        }
    }

    private boolean isExpired(long nowNanos, Map.Entry<String, Long> entry) {
        Long emittedAt = entry.getValue();
        return emittedAt == null || nowNanos - emittedAt >= intervalNanos;
    }
}
