package net.minecraft.network.chat;

public interface Component {
    static MutableComponent empty() {
        return new MutableComponent("");
    }

    static MutableComponent literal(String value) {
        return new MutableComponent(value);
    }

    static MutableComponent translatable(String key) {
        return new MutableComponent(key);
    }

    static MutableComponent translatable(String key, Object... args) {
        if (args == null || args.length == 0) {
            return new MutableComponent(key);
        }
        StringBuilder builder = new StringBuilder(key);
        builder.append('[');
        for (int index = 0; index < args.length; index++) {
            if (index > 0) {
                builder.append(", ");
            }
            builder.append(args[index]);
        }
        builder.append(']');
        return new MutableComponent(builder.toString());
    }

    String getString();
}
