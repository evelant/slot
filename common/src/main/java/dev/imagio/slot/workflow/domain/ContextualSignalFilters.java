package dev.imagio.slot.workflow.domain;

import java.util.Locale;

public final class ContextualSignalFilters {
    private ContextualSignalFilters() {
    }

    public static boolean lowInformationUse(ContextualSignalEvent event) {
        if (event == null || event.kind() != ContextualSignalKind.ITEM_USED) {
            return false;
        }
        return lowInformationWorldUse(event.metadataValue("action"), event.metadataValue("target"));
    }

    public static boolean lowInformationWorldUse(String action, String targetKey) {
        String normalizedAction = clean(action);
        String normalizedTarget = clean(targetKey);
        if (!"right_click_block".equals(normalizedAction) || !normalizedTarget.startsWith("block:")) {
            return false;
        }
        String path = normalizedTarget.substring("block:".length());
        int namespaceSeparator = path.indexOf(':');
        if (namespaceSeparator >= 0) {
            path = path.substring(namespaceSeparator + 1);
        }
        return path.contains("chest")
                || path.contains("barrel")
                || path.contains("drawer")
                || path.contains("crate")
                || path.contains("basket")
                || path.contains("backpack")
                || path.contains("pouch")
                || path.contains("sack")
                || path.contains("vessel")
                || path.contains("shelf")
                || path.contains("cabinet")
                || path.contains("storage")
                || path.contains("tool_rack")
                || path.contains("log_pile")
                || path.contains("shulker_box");
    }

    public static boolean ignoredStationContext(String contextKey) {
        return "menu:net.minecraft.world.inventory.inventorymenu".equals(clean(contextKey));
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
