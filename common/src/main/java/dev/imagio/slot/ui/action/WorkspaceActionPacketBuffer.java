package dev.imagio.slot.ui.action;

public interface WorkspaceActionPacketBuffer {
    void writeString(String value);

    String readString();

    void writeInt(int value);

    int readInt();

    void writeLong(long value);

    long readLong();

    void writeBoolean(boolean value);

    boolean readBoolean();

    void writeDouble(double value);

    double readDouble();
}
