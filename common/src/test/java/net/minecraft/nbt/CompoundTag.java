package net.minecraft.nbt;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class CompoundTag extends Tag {
    private final Map<String, Object> values = new LinkedHashMap<>();

    public void putLong(String key, long value) {
        values.put(key, value);
    }

    public long getLong(String key) {
        Object value = values.get(key);
        return value instanceof Number number ? number.longValue() : 0L;
    }

    public void putString(String key, String value) {
        values.put(key, value == null ? "" : value);
    }

    public String getString(String key) {
        Object value = values.get(key);
        return value instanceof String string ? string : "";
    }

    public void putInt(String key, int value) {
        values.put(key, value);
    }

    public int getInt(String key) {
        Object value = values.get(key);
        return value instanceof Number number ? number.intValue() : 0;
    }

    public void putBoolean(String key, boolean value) {
        values.put(key, value);
    }

    public boolean getBoolean(String key) {
        Object value = values.get(key);
        return value instanceof Boolean bool && bool;
    }

    public Tag put(String key, Tag tag) {
        Object previous = values.put(key, tag == null ? new Tag() : tag);
        return previous instanceof Tag previousTag ? previousTag : null;
    }

    public ListTag getList(String key, int type) {
        Object value = values.get(key);
        return value instanceof ListTag listTag ? listTag : new ListTag();
    }

    public CompoundTag getCompound(String key) {
        Object value = values.get(key);
        return value instanceof CompoundTag compoundTag ? compoundTag : new CompoundTag();
    }

    @Override
    public CompoundTag copy() {
        CompoundTag copy = new CompoundTag();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            copy.values.put(entry.getKey(), value instanceof Tag tag ? tag.copy() : value);
        }
        return copy;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CompoundTag compoundTag && Objects.equals(values, compoundTag.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }
}
