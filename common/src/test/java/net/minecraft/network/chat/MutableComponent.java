package net.minecraft.network.chat;

public final class MutableComponent implements Component {
    private final String value;

    public MutableComponent(String value) {
        this.value = value == null ? "" : value;
    }

    @Override
    public String getString() {
        return value;
    }
}
