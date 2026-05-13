package dev.imagio.slot.inventory.triage;

import java.util.Collection;
import java.util.Locale;

/**
 * Normalizes loader/version common tag conventions into semantic tag
 * families. Forge 1.20 used {@code forge:<family>/<material>}; modern
 * NeoForge/Fabric and Forge 1.21 common tags use {@code c:<family>/<material>}.
 * The family shape is the stable signal, not the namespace.
 */
public final class CommonItemTagFamilies {

    private CommonItemTagFamilies() {
    }

    public enum Family {
        INGOTS("ingots"),
        GEMS("gems"),
        NUGGETS("nuggets"),
        PLATES("plates"),
        RODS("rods"),
        WIRES("wires"),
        GEARS("gears"),
        DUSTS("dusts"),
        POWDERS("powders"),
        RAW_MATERIALS("raw_materials"),
        ORES("ores");

        private final String rootPath;

        Family(String rootPath) {
            this.rootPath = rootPath;
        }

        public String rootPath() {
            return rootPath;
        }
    }

    public static boolean hasFamily(Collection<String> tags, Family family) {
        if (tags == null || tags.isEmpty() || family == null) {
            return false;
        }
        for (String tag : tags) {
            if (familyOf(tag) == family) {
                return true;
            }
        }
        return false;
    }

    public static String canonicalRootTag(Family family) {
        return family == null ? "" : "c:" + family.rootPath();
    }

    private static Family familyOf(String tag) {
        if (tag == null || tag.isBlank()) {
            return null;
        }
        String normalized = tag.toLowerCase(Locale.ROOT);
        int colon = normalized.indexOf(':');
        if (colon <= 0 || colon == normalized.length() - 1) {
            return null;
        }
        String namespace = normalized.substring(0, colon);
        if (!"c".equals(namespace) && !"forge".equals(namespace)) {
            return null;
        }
        String path = normalized.substring(colon + 1);
        int slash = path.indexOf('/');
        String root = slash >= 0 ? path.substring(0, slash) : path;
        for (Family family : Family.values()) {
            if (family.rootPath.equals(root)) {
                return family;
            }
        }
        return null;
    }
}
