package net.minecraft.resources;

public final class ResourceLocation {
    private final String namespace;
    private final String path;

    private ResourceLocation(String namespace, String path) {
        this.namespace = namespace == null || namespace.isBlank() ? "minecraft" : namespace;
        this.path = path == null ? "" : path;
    }

    public static ResourceLocation tryParse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        int colon = trimmed.indexOf(':');
        if (colon < 0) {
            return new ResourceLocation("minecraft", trimmed);
        }
        if (colon == 0 || colon + 1 >= trimmed.length()) {
            return null;
        }
        return new ResourceLocation(trimmed.substring(0, colon), trimmed.substring(colon + 1));
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
