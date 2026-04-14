package dev.imagio.slot.client.screen.container;

import dev.imagio.slot.storage.adapter.ExternalToolKind;

final class DockedToolPanelReusePolicy {
    private DockedToolPanelReusePolicy() {
    }

    static boolean canReuse(boolean sameMenu, ExternalToolKind existingKind, ExternalToolKind nextKind) {
        return sameMenu
                && existingKind == ExternalToolKind.CRAFTING_GRID
                && nextKind == ExternalToolKind.CRAFTING_GRID;
    }
}
