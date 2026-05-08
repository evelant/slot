package dev.imagio.slot.ui.action;

import java.util.ArrayList;
import java.util.List;

final class InMemoryWorkspaceActionPacketBuffer implements WorkspaceActionPacketBuffer {
    private final List<Object> values = new ArrayList<>();
    private int readIndex;

    @Override
    public void writeString(String value) {
        values.add(value == null ? "" : value);
    }

    @Override
    public String readString() {
        return (String) values.get(readIndex++);
    }

    @Override
    public void writeInt(int value) {
        values.add(value);
    }

    @Override
    public int readInt() {
        return (Integer) values.get(readIndex++);
    }

    @Override
    public void writeLong(long value) {
        values.add(value);
    }

    @Override
    public long readLong() {
        return (Long) values.get(readIndex++);
    }

    @Override
    public void writeBoolean(boolean value) {
        values.add(value);
    }

    @Override
    public boolean readBoolean() {
        return (Boolean) values.get(readIndex++);
    }

    @Override
    public void writeDouble(double value) {
        values.add(value);
    }

    @Override
    public double readDouble() {
        return (Double) values.get(readIndex++);
    }
}
