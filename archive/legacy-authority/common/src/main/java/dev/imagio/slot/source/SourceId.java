package dev.imagio.slot.source;

import java.util.Objects;

public record SourceId(String value) {
    public SourceId {
        Objects.requireNonNull(value, "value");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    public static SourceId of(String value) {
        return new SourceId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
