package net.minecraft.nbt;

import java.util.ArrayList;

public class ListTag extends Tag {
    private final ArrayList<Tag> values = new ArrayList<>();

    public boolean add(Tag tag) {
        return values.add(tag == null ? new Tag() : tag);
    }

    public boolean add(Object tag) {
        return add(tag instanceof Tag resolved ? resolved : new Tag());
    }

    public int size() {
        return values.size();
    }

    public CompoundTag getCompound(int index) {
        Tag tag = values.get(index);
        return tag instanceof CompoundTag compoundTag ? compoundTag : new CompoundTag();
    }

    @Override
    public ListTag copy() {
        ListTag copy = new ListTag();
        for (Tag value : values) {
            copy.add(value.copy());
        }
        return copy;
    }
}
