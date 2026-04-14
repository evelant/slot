package dev.imagio.slot.client.screen.container;

import dev.imagio.slot.storage.adapter.ExternalToolKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DockedToolPanelReusePolicyTest {
    @Test
    void reusesCraftingPanelsWhenTheMenuInstanceIsStable() {
        assertTrue(DockedToolPanelReusePolicy.canReuse(true, ExternalToolKind.CRAFTING_GRID, ExternalToolKind.CRAFTING_GRID));
    }

    @Test
    void doesNotReusePanelsAcrossDifferentMenus() {
        assertFalse(DockedToolPanelReusePolicy.canReuse(false, ExternalToolKind.CRAFTING_GRID, ExternalToolKind.CRAFTING_GRID));
    }

    @Test
    void doesNotReuseWhenTheToolKindChanges() {
        assertFalse(DockedToolPanelReusePolicy.canReuse(true, ExternalToolKind.CRAFTING_GRID, null));
    }
}
