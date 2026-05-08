package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.ui.action.WorkspaceActionPacketBuffer;
import net.minecraft.network.FriendlyByteBuf;

public final class NeoForgeWorkspaceActionPacketBuffer implements WorkspaceActionPacketBuffer {
    private final FriendlyByteBuf buffer;

    public NeoForgeWorkspaceActionPacketBuffer(FriendlyByteBuf buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("buffer must not be null");
        }
        this.buffer = buffer;
    }

    @Override
    public void writeString(String value) {
        buffer.writeUtf(value == null ? "" : value);
    }

    @Override
    public String readString() {
        return buffer.readUtf();
    }

    @Override
    public void writeInt(int value) {
        buffer.writeInt(value);
    }

    @Override
    public int readInt() {
        return buffer.readInt();
    }

    @Override
    public void writeLong(long value) {
        buffer.writeLong(value);
    }

    @Override
    public long readLong() {
        return buffer.readLong();
    }

    @Override
    public void writeBoolean(boolean value) {
        buffer.writeBoolean(value);
    }

    @Override
    public boolean readBoolean() {
        return buffer.readBoolean();
    }

    @Override
    public void writeDouble(double value) {
        buffer.writeDouble(value);
    }

    @Override
    public double readDouble() {
        return buffer.readDouble();
    }
}
