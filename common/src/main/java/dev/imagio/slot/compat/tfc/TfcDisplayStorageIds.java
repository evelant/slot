package dev.imagio.slot.compat.tfc;

import dev.imagio.slot.inventory.storage.WorldDisplayStorageKind;

/**
 * Pure id-shape recognition for TFC-family item displays. The loader
 * delegates still own reflection and mutation; this helper keeps Forge and
 * NeoForge aligned on which block ids are display storage.
 */
public final class TfcDisplayStorageIds {
    private TfcDisplayStorageIds() {
    }

    public static WorldDisplayStorageKind kindForBlockId(String namespace, String path) {
        if (namespace == null || path == null) {
            return null;
        }
        String ns = namespace.trim();
        String value = path.trim();
        if ("tfc".equals(ns) && "placed_item".equals(value)) {
            return WorldDisplayStorageKind.PLACED_ITEM;
        }
        if (isToolRackPath(value)) {
            return WorldDisplayStorageKind.TOOL_RACK;
        }
        return null;
    }

    private static boolean isToolRackPath(String path) {
        return path.startsWith("wood/tool_rack/")
                || (path.startsWith("wood/planks/") && path.endsWith("_tool_rack"));
    }
}
