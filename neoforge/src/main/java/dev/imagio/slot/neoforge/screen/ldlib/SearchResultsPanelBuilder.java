package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;

/**
 * Phase 5 of the single-column workspace plan retired the dedicated
 * Chest-locator panel. Search results live on the cards now: the
 * existing "+N elsewhere" badge in
 * {@link AtlasCardBuilder#addAlsoStoredBadge} surfaces non-proximate
 * presence, and tooltips carry the per-chest detail.
 *
 * <p>This builder remains as a stub call site so {@link LeftColumnBuilder}
 * keeps a stable wiring point — its {@link #overlay()} always returns
 * {@code null}.
 */
final class SearchResultsPanelBuilder {
    @SuppressWarnings("unused")
    private final SlotWorkspaceUiController host;

    SearchResultsPanelBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement overlay() {
        return null;
    }
}
