package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import dev.imagio.slot.atlas.lod.AtlasLayoutResult;
import dev.imagio.slot.atlas.lod.Band;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.world.item.ItemStack;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

final class AtlasCardBuilder {
    private final SlotWorkspaceUiController host;

    AtlasCardBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    void rebuildAtlasBody(
            UIElement container,
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item,
            AtlasRenderBudget budget,
            boolean searchMatch
    ) {
        float pinned = animationTargetScale(atlas);
        atlas.setPinnedContentScale(pinned);
        try {
            container.clearAllChildren();
            container.addChild(buildAtlasBody(atlas, item, budget, searchMatch));
        } finally {
            atlas.setPinnedContentScale(null);
        }
    }

    /**
     * Per-frame paint flip: when {@code host.hoveredStorageId} matches a
     * chest with presence for this item's identity, paint an accent overlay.
     * Symmetric with the chip-side flip in {@link StoragePanelBuilder}; the
     * pair forms the cross-surface highlight pulse described in
     * {@code docs/plans/learned-storage.md}.
     */
    private void installChestHoverPaint(UIElement button, SlotWorkspaceViewModel.AtlasItem item) {
        boolean[] lastLit = {false};
        button.addEventListener(UIEvents.TICK, event -> {
            boolean lit = isItemPresentInHoveredChest(item);
            if (lit == lastLit[0]) {
                return;
            }
            lastLit[0] = lit;
            button.style(style -> style.overlayTexture(lit ? rect(HOVER_ACCENT_OVERLAY) : com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture.EMPTY));
        });
    }

    private boolean isItemPresentInHoveredChest(SlotWorkspaceViewModel.AtlasItem item) {
        String storageId = host.hoveredStorageId;
        if (storageId == null || item == null) {
            return false;
        }
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
            if (storageId.equals(entry.storageId())) {
                return true;
            }
        }
        // Also flip on hover of a non-proximate chest (e.g. a row in the
        // search-results panel): the chest's storageId only appears in the
        // {@code elsewhere} list for that case, not {@code presence}.
        // Without this branch, hovering the search-results panel would
        // never light up an atlas card.
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.elsewhere()) {
            if (storageId.equals(entry.storageId())) {
                return true;
            }
        }
        return false;
    }

    UIElement buildAtlasBody(
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item,
            AtlasRenderBudget budget,
            boolean searchMatch
    ) {
        return switch (budget.level()) {
            case PIP, REGION -> regionAtlasBody(atlas, item, budget, searchMatch);
            case BROWSE -> browseAtlasBody(atlas, item, budget, searchMatch);
            case READ -> readAtlasBody(atlas, item, budget, searchMatch);
            case INSPECT -> inspectAtlasBody(atlas, item, budget, searchMatch);
            case DETAIL -> detailAtlasBody(atlas, item, budget, searchMatch);
        };
    }

    AtlasRenderBudget atlasBudget(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasItem item) {
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        float scale = animationTargetScale(atlas);
        int screenBudget = Math.max(1, Math.round(Math.min(place.width(), place.height()) * scale));
        return AtlasRenderBudget.forScreenBudget(screenBudget);
    }

    // Rebuild key for atlas card bodies. Only true LOD transitions trigger
    // a host.rebuild: disclosure level, READ's 1↔2 line flip at
    // cellBudgetPx>=40, and INSPECT's secondary-reveal at >=58. Fine-
    // grained scale tracking (keeping on-screen font px roughly constant
    // inside a level) is handled per-label inside anchorTextBand via a
    // TICK listener, which avoids thrashing the whole body subtree.
    long atlasLayoutSignature(AtlasRenderBudget budget) {
        long signature = budget.level().ordinal() & 0x7L;
        signature = (signature << 1) | (budget.cellBudgetPx() >= 40 ? 1L : 0L);
        signature = (signature << 1) | (budget.cellBudgetPx() >= 58 ? 1L : 0L);
        return signature;
    }

    float animationTargetScale(SlotAtlasGraphView atlas) {
        if (host.cameraController.isAnimating()) {
            AtlasCamera target = host.cameraController.animTarget();
            if (target != null) {
                return target.scale();
            }
        }
        if (host.atlasCamera != null) {
            return host.atlasCamera.scale();
        }
        return atlas.getScale();
    }

    UIElement regionAtlasBody(
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item,
            AtlasRenderBudget budget,
            boolean searchMatch
    ) {
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        UIElement body = atlasBodyContainer();
        float cardBound = Math.min(place.width(), place.height());
        float shell = Math.min(cardBound, atlas.worldUnitsForPixels(budget.shellPx()));
        float shellLeft = centeredWorld(place.width(), shell);
        float shellTop = centeredWorld(place.height(), shell);
        addCommonAtlasSignals(body, atlas, item, budget, searchMatch);
        body.addChild(slotPreview(atlas, item, budget).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(shellLeft)
                .top(shellTop)));
        addOverlaySignals(body, atlas, item, budget);
        return body;
    }

    UIElement browseAtlasBody(
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item,
            AtlasRenderBudget budget,
            boolean searchMatch
    ) {
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        UIElement body = atlasBodyContainer();
        float cardBound = Math.min(place.width(), place.height());
        float shell = Math.min(cardBound, atlas.worldUnitsForPixels(budget.shellPx()));
        float shellLeft = centeredWorld(place.width(), shell);
        float shellTop = centeredWorld(place.height(), shell);
        addCommonAtlasSignals(body, atlas, item, budget, searchMatch);
        body.addChild(slotPreview(atlas, item, budget).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(shellLeft)
                .top(shellTop)));
        addOverlaySignals(body, atlas, item, budget);
        return body;
    }

    UIElement readAtlasBody(
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item,
            AtlasRenderBudget budget,
            boolean searchMatch
    ) {
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        UIElement body = atlasBodyContainer();
        float sidePad = atlas.worldUnitsForPixels(1f);
        float gap = atlas.worldUnitsForPixels(1f);
        float shellPx = budget.shellPx();
        float iconPx = budget.iconPx();
        float shell = atlas.worldUnitsForPixels(shellPx);
        float shellTop = atlas.worldUnitsForPixels(1f);
        int labelLines = budget.cellBudgetPx() >= 40 ? 2 : 1;
        float labelScreenHeight = budget.primaryLineHeightPx() * labelLines + (labelLines > 1 ? 1f : 0f);
        float labelHeight = atlas.worldUnitsForPixels(labelScreenHeight);
        addCommonAtlasSignals(body, atlas, item, budget, searchMatch);
        float shellLeft = (place.width() - shell) / 2f;
        body.addChild(slotPreview(atlas, item, shellPx, iconPx, budget.level()).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(shellLeft)
                .top(shellTop)));
        body.addChild(anchorTextBand(
                atlas,
                preferredPrimaryLabel(item, budget),
                searchMatch ? ACCENT : TEXT,
                budget.primaryFontPx(),
                budget.primaryMaxChars(),
                labelLines,
                0xB4111921,
                Horizontal.CENTER
        ).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(sidePad)
                .top(shellTop + shell + gap)
                .width(place.width() - sidePad * 2f)
                .height(labelHeight)));
        addOverlaySignals(body, atlas, item, budget);
        return body;
    }

    UIElement inspectAtlasBody(
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item,
            AtlasRenderBudget budget,
            boolean searchMatch
    ) {
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        UIElement body = atlasBodyContainer();
        float sidePad = atlas.worldUnitsForPixels(1f);
        float gap = atlas.worldUnitsForPixels(1f);
        String secondary = preferredSecondaryLabel(item, budget);
        boolean showSecondary = !secondary.isBlank() && budget.cellBudgetPx() >= 58;
        float shellPx = showSecondary
                ? Math.min(budget.cellBudgetPx() * 0.48f, budget.shellPx() + 4f)
                : Math.min(budget.cellBudgetPx() * 0.60f, budget.shellPx() + 10f);
        float iconPx = Math.max(10f, shellPx - 4f);
        float shell = atlas.worldUnitsForPixels(shellPx);
        float topPad = atlas.worldUnitsForPixels(1f);
        float primaryHeight = atlas.worldUnitsForPixels(budget.primaryLineHeightPx() * 2f + 1f);
        float secondaryHeight = atlas.worldUnitsForPixels(budget.secondaryLineHeightPx());
        addCommonAtlasSignals(body, atlas, item, budget, searchMatch);
        float shellLeft = (place.width() - shell) / 2f;
        body.addChild(slotPreview(atlas, item, shellPx, iconPx, budget.level()).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(shellLeft)
                .top(topPad)));
        float cursorTop = topPad + shell + gap;
        body.addChild(anchorTextBand(
                atlas,
                preferredPrimaryLabel(item, budget),
                TEXT,
                budget.primaryFontPx(),
                budget.primaryMaxChars(),
                2,
                0xB4111921,
                Horizontal.LEFT
        ).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(sidePad)
                .top(cursorTop)
                .width(place.width() - sidePad * 2f)
                .height(primaryHeight)));
        if (showSecondary) {
            body.addChild(anchorTextBand(
                    atlas,
                    secondary,
                    searchMatch ? ACCENT : MUTED,
                    budget.secondaryFontPx(),
                    budget.secondaryMaxChars(),
                    1,
                    0x9A111921,
                    Horizontal.LEFT
            ).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(sidePad)
                    .top(cursorTop + primaryHeight + gap)
                    .width(place.width() - sidePad * 2f)
                    .height(secondaryHeight)));
        }
        addOverlaySignals(body, atlas, item, budget);
        return body;
    }

    UIElement detailAtlasBody(
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item,
            AtlasRenderBudget budget,
            boolean searchMatch
    ) {
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        UIElement body = atlasBodyContainer();
        float topPad = atlas.worldUnitsForPixels(2f);
        float sidePad = atlas.worldUnitsForPixels(2f);
        float gap = atlas.worldUnitsForPixels(2f);
        String secondary = preferredSecondaryLabel(item, budget);
        String auxiliary = preferredAuxiliaryLabel(item, budget);
        boolean hasSecondary = !secondary.isBlank();
        boolean hasAuxiliary = !auxiliary.isBlank();
        float shellPx = !hasSecondary && !hasAuxiliary
                ? Math.min(budget.cellBudgetPx() * 0.64f, budget.shellPx() + 14f)
                : Math.min(budget.cellBudgetPx() * 0.54f, budget.shellPx() + 6f);
        float iconPx = Math.max(14f, shellPx - 6f);
        int primaryLines = hasSecondary || hasAuxiliary ? 2 : 3;
        float nameHeight = atlas.worldUnitsForPixels(budget.primaryLineHeightPx() * primaryLines + (primaryLines - 1));
        float shell = atlas.worldUnitsForPixels(shellPx);
        float shellTop = topPad;
        float auxLineHeight = atlas.worldUnitsForPixels(budget.secondaryLineHeightPx());
        addCommonAtlasSignals(body, atlas, item, budget, searchMatch);
        body.addChild(slotPreview(atlas, item, shellPx, iconPx, budget.level()).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(sidePad)
                .top(shellTop)));
        float cursorTop = shellTop + shell + gap;
        float primaryTop = cursorTop;
        body.addChild(anchorTextBand(
                atlas,
                preferredPrimaryLabel(item, budget),
                TEXT,
                budget.primaryFontPx(),
                budget.primaryMaxChars(),
                primaryLines,
                0xB8111921,
                Horizontal.LEFT
        ).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(sidePad)
                .top(primaryTop)
                .width(place.width() - sidePad * 2f)
                .height(nameHeight)));
        cursorTop += nameHeight;
        if (hasSecondary) {
            cursorTop += gap;
            float secondaryTop = cursorTop;
            body.addChild(anchorTextBand(
                    atlas,
                    secondary,
                    searchMatch ? ACCENT : MUTED,
                    budget.secondaryFontPx(),
                    budget.secondaryMaxChars(),
                    1,
                    0x90111921,
                    Horizontal.LEFT
            ).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(sidePad)
                    .top(secondaryTop)
                    .width(place.width() - sidePad * 2f)
                    .height(auxLineHeight)));
            cursorTop += auxLineHeight;
        }
        if (hasAuxiliary) {
            cursorTop += gap;
            float auxiliaryTop = cursorTop;
            body.addChild(anchorTextBand(
                    atlas,
                    auxiliary,
                    MUTED,
                    budget.secondaryFontPx() - 0.5f,
                    budget.secondaryMaxChars(),
                    1,
                    0x80111921,
                    Horizontal.LEFT
            ).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(sidePad)
                    .top(auxiliaryTop)
                    .width(place.width() - sidePad * 2f)
                    .height(auxLineHeight)));
            cursorTop += auxLineHeight;
        }
        if (item.isCarriedContainer()) {
            cursorTop += gap;
            float containerTop = cursorTop;
            body.addChild(anchorTextBand(
                    atlas,
                    formatFreeSlots(item.containerFreeSlotCount()),
                    item.containerFreeSlotCount() == 0 ? WARNING : ACCENT,
                    budget.secondaryFontPx(),
                    budget.secondaryMaxChars(),
                    1,
                    0x90111921,
                    Horizontal.LEFT
            ).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(sidePad)
                    .top(containerTop)
                    .width(place.width() - sidePad * 2f)
                    .height(auxLineHeight)));
            cursorTop += auxLineHeight;
        }
        if (!item.presence().isEmpty()) {
            cursorTop += gap;
            UIElement strip = presenceStrip(atlas, item, budget);
            if (strip != null) {
                float presenceTop = cursorTop;
                body.addChild(strip.layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(sidePad)
                        .top(presenceTop)
                        .width(place.width() - sidePad * 2f)
                        .height(auxLineHeight)));
                cursorTop += auxLineHeight;
            }
        }
        // Search-as-find: show "elsewhere" presence (non-proximate chests)
        // only when a search query is active. Otherwise it's information
        // overload — players don't need to see "this lives at Storage Area 2"
        // until they're actively looking for it.
        if (!item.elsewhere().isEmpty() && !host.searchController.normalizedQuery().isBlank()) {
            cursorTop += gap;
            UIElement strip = elsewhereStrip(atlas, item, budget);
            if (strip != null) {
                float elsewhereTop = cursorTop;
                body.addChild(strip.layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(sidePad)
                        .top(elsewhereTop)
                        .width(place.width() - sidePad * 2f)
                        .height(auxLineHeight)));
            }
        }
        addOverlaySignals(body, atlas, item, budget);
        return body;
    }

    UIElement presenceStrip(
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item,
            AtlasRenderBudget budget
    ) {
        if (item.presence().isEmpty()) {
            return null;
        }
        StringBuilder text = new StringBuilder("in: ");
        int maxEntries = Math.min(item.presence().size(), 3);
        for (int index = 0; index < maxEntries; index++) {
            SlotWorkspaceViewModel.ChestPresenceEntry entry = item.presence().get(index);
            if (index > 0) {
                text.append(" · ");
            }
            text.append(entry.label()).append(" · ").append(entry.count());
        }
        if (item.presence().size() > maxEntries) {
            text.append(" · +").append(item.presence().size() - maxEntries);
        }
        int maxChars = Math.max(8, budget.secondaryMaxChars() + 12);
        UIElement band = anchorTextBand(
                atlas,
                text.toString(),
                ACCENT,
                budget.secondaryFontPx() - 0.5f,
                maxChars,
                1,
                0x80121B1F,
                Horizontal.LEFT
        );
        // Chest tiles no longer render, so click-to-pan is gone.
        band.setAllowHitTest(false);
        return band;
    }

    /**
     * Render an "elsewhere: …" strip when the player has searched and the
     * matched item lives in non-proximate chests. Spacebar zoom on the card
     * surfaces the per-chest detail via the existing LOD path.
     */
    UIElement elsewhereStrip(
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item,
            AtlasRenderBudget budget
    ) {
        if (item.elsewhere().isEmpty()) {
            return null;
        }
        StringBuilder text = new StringBuilder("elsewhere: ");
        int maxEntries = Math.min(item.elsewhere().size(), 2);
        for (int index = 0; index < maxEntries; index++) {
            SlotWorkspaceViewModel.ChestPresenceEntry entry = item.elsewhere().get(index);
            if (index > 0) {
                text.append(" · ");
            }
            text.append(entry.label()).append(" · ").append(entry.count());
        }
        if (item.elsewhere().size() > maxEntries) {
            text.append(" · +").append(item.elsewhere().size() - maxEntries);
        }
        int maxChars = Math.max(8, budget.secondaryMaxChars() + 12);
        UIElement band = anchorTextBand(
                atlas,
                text.toString(),
                MUTED,
                budget.secondaryFontPx() - 0.5f,
                maxChars,
                1,
                0x80121B1F,
                Horizontal.LEFT
        );
        band.setAllowHitTest(false);
        return band;
    }

    UIElement atlasBodyContainer() {
        UIElement body = new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));
        body.setAllowHitTest(false);
        return body;
    }

    UIElement slotPreview(
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item,
            AtlasRenderBudget budget
    ) {
        return slotPreview(atlas, item, budget.shellPx(), budget.iconPx(), budget.level());
    }

    UIElement slotPreview(
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item,
            float shellPx,
            float iconPx,
            Band band
    ) {
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        float cardBound = Math.min(place.width(), place.height());
        float fullShell = Math.min(cardBound, atlas.worldUnitsForPixels(shellPx));
        float fullIcon = atlas.worldUnitsForPixels(iconPx);

        boolean carried = item.carried();
        // Ghost cards (visible but not in inventory) shrink as we zoom out
        // so the carried items dominate the silhouette — that's the "what
        // am I actually carrying?" overview the player needs at low zoom.
        // Stays full-size at DETAIL/INSPECT where you can read every cell.
        float ghostScale = ghostScaleFor(carried, band);
        float shell = fullShell * ghostScale;
        float icon = fullIcon * ghostScale;
        float inset = Math.min(shell * 0.5f, atlas.worldUnitsForPixels(1f) * ghostScale);
        icon = Math.max(0f, Math.min(shell - inset * 2f, icon));

        int shellColor = carried ? CARD_SHELL : CARD_SHELL_GHOST;
        int innerColor = carried ? CARD_INNER : CARD_INNER_GHOST;

        // Wrapper occupies the full-size cell so the call site's centering
        // math (which still uses the un-scaled shell) keeps the ghost
        // visually centered within the card slot.
        UIElement wrapper = new UIElement().layout(layout -> layout.width(fullShell).height(fullShell));
        wrapper.setAllowHitTest(false);

        float shellOffset = (fullShell - shell) / 2f;
        UIElement shellElement = panel(shellColor).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(shellOffset)
                .top(shellOffset)
                .width(shell)
                .height(shell));
        shellElement.setAllowHitTest(false);
        final float finalShell = shell;
        final float finalIcon = icon;
        final float finalInset = inset;
        shellElement.addChild(panel(innerColor).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(finalInset)
                .top(finalInset)
                .width(finalShell - finalInset * 2f)
                .height(finalShell - finalInset * 2f)));
        shellElement.addChild(itemIcon(item.displayStack(), finalIcon, carried).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(centeredWorld(finalShell, finalIcon))
                .top(centeredWorld(finalShell, finalIcon))));
        wrapper.addChild(shellElement);
        return wrapper;
    }

    /**
     * Render-layer size multiplier for non-carried ("ghost") atlas cards.
     * Now always 1f — ghosts represent reachable proximate-chest stock the
     * player can grab (shift+click → take), so they need to be the same
     * size as carried cards at every zoom band. The legacy ramp shrunk
     * memory-palace ghosts so they wouldn't dominate; that role is gone.
     * See docs/plans/learned-storage.md.
     */
    static float ghostScaleFor(boolean carried, Band band) {
        return 1f;
    }

    UIElement slotPreview(SlotWorkspaceViewModel.AtlasItem item, int size, boolean showMarker) {
        float shell = size;
        float inset = 1f;
        float icon = Math.max(10f, size - 4f);
        boolean carried = item.carried();
        int shellColor = carried ? CARD_SHELL : CARD_SHELL_GHOST;
        int innerColor = carried ? CARD_INNER : CARD_INNER_GHOST;
        UIElement shellElement = panel(shellColor).layout(layout -> layout.width(shell).height(shell));
        shellElement.setAllowHitTest(false);
        shellElement.addChild(panel(innerColor).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(inset)
                .top(inset)
                .width(shell - inset * 2f)
                .height(shell - inset * 2f)));
        shellElement.addChild(itemIcon(item.displayStack(), icon, carried).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(centeredWorld(shell, icon))
                .top(centeredWorld(shell, icon))));
        if (showMarker) {
            shellElement.addChild(panel(itemMarkerColor(item, host.viewModel.island(item.islandId()))).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(1f)
                    .top(1f)
                    .width(3f)
                    .height(3f)));
        }
        return shellElement;
    }

private void addCommonAtlasSignals(
            UIElement body,
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item,
            AtlasRenderBudget budget,
            boolean searchMatch
    ) {
        // Signals drawn *below* the item icon. LDLib2's drawContents paints
        // children in insertion order (zIndex only affects pose Z, not
        // paint order within a parent), so addOverlaySignals must be
        // called AFTER the slotPreview is added to keep overlays on top.
        if (searchMatch) {
            addSearchMatchOutline(body, atlas, item);
        }
    }

    /**
     * Bright outline framing the whole atlas card when it matches the
     * active search query. Replaces the prior tiny bottom-edge accent
     * which was invisible at low zoom and barely visible on faded ghost
     * cards. Each edge is its own rect so the outline is hollow (the
     * card icon stays readable inside). Thickness floors at 2 screen
     * pixels so it's always visible regardless of zoom; scales up with
     * the card so close-up cards still get a proportional border.
     */
    private void addSearchMatchOutline(UIElement body, SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasItem item) {
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        float thickness = Math.max(atlas.worldUnitsForPixels(2f), place.width() * 0.04f);
        // High z so the outline survives the +232 pose Z bump from
        // drawItemStack and lands on top of the icon. WARNING is the
        // brightest theme constant we have; works on both vibrant and
        // faded (ghost) cards.
        int color = WARNING;
        UIElement top = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).top(0).widthPercent(100).height(thickness));
        top.style(style -> style.zIndex(263));
        top.setAllowHitTest(false);
        body.addChild(top);
        UIElement bottom = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).bottom(0).widthPercent(100).height(thickness));
        bottom.style(style -> style.zIndex(263));
        bottom.setAllowHitTest(false);
        body.addChild(bottom);
        UIElement left = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).top(0).width(thickness).heightPercent(100));
        left.style(style -> style.zIndex(263));
        left.setAllowHitTest(false);
        body.addChild(left);
        UIElement right = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .right(0).top(0).width(thickness).heightPercent(100));
        right.style(style -> style.zIndex(263));
        right.setAllowHitTest(false);
        body.addChild(right);
    }

    void addOverlaySignals(
            UIElement body,
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item,
            AtlasRenderBudget budget
    ) {
        // Must be invoked AFTER slotPreview is added to body so these paint
        // over the item icon. DrawerHelper.drawItemStack translates pose Z
        // by +232, but that only matters for depth testing — within a
        // single parent, sibling paint order is the list order, not the
        // zIndex order.
        if (item.isCarriedContainer()) {
            addContainerFullnessBar(body, atlas, item);
        }
        // Top-right "stored" pip: nearby (proximate) chest contents
        // always count toward this number (existing behaviour).
        int proximateCount = proximateChestCount(item);
        if (proximateCount > 0) {
            AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
            float inset = Math.min(place.width() * 0.04f, atlas.worldUnitsForPixels(2f));
            float pipSizeRaw = Math.min(place.width() * 0.22f, atlas.worldUnitsForPixels(10f));
            float pipSize = Math.max(pipSizeRaw, place.width() * 0.08f);
            final float finalPipSize = pipSize;
            UIElement pip = panel(LINK_THREAD_COLOR).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(inset)
                    .top(inset)
                    .width(finalPipSize)
                    .height(finalPipSize));
            // zIndex pushes pose Z above the +232 item-icon depth so the
            // pip survives the icon's depth-write before the shader clears
            // the depth buffer.
            pip.style(style -> style.zIndex(260));
            pip.setAllowHitTest(false);
            if (budget.level() != Band.REGION) {
                Label count = label(String.valueOf(Math.min(proximateCount, 999)), TEXT);
                count.layout(layout -> layout.widthPercent(100).heightPercent(100));
                count.setAllowHitTest(false);
                float requestedPipFontPx = finalPipSize * 0.7f * atlas.getScale();
                float pipFontWorld = clampScreenFontPx(requestedPipFontPx) / Math.max(0.0001f, atlas.getScale());
                count.textStyle(style -> style
                        .textColor(TEXT)
                        .textShadow(false)
                        .fontSize(pipFontWorld)
                        .textAlignHorizontal(Horizontal.CENTER)
                        .textAlignVertical(Vertical.CENTER));
                pip.addChild(count);
            }
            body.addChild(pip);
        }
        if (item.kitNeeded()) {
            addKitNeedsBadge(body, atlas, item);
        }
        if (item.desiredCount() > 0) {
            addDesiredCountPip(body, atlas, item, budget);
        }
        // Search-time "also stored" badge for items that have copies
        // in any chest (proximate or remote). The proximate-only top-
        // right pip already counts nearby copies, but when search is
        // active the player is looking across the whole base — they
        // need a clear signal that a *carried* match also lives in
        // storage. Bottom-left, distinct color, only visible under
        // search so the atlas stays clean during normal play.
        if (!host.searchController.normalizedQuery().isBlank()) {
            int storedCount = proximateChestCount(item);
            for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.elsewhere()) {
                storedCount += entry.count();
            }
            if (storedCount > 0) {
                addAlsoStoredBadge(body, atlas, item, storedCount);
            }
        }
        if (RelevanceDebugOverlay.enabled()) {
            addRelevanceDebugBadge(body, atlas, item);
        }
    }

    /**
     * Bottom-left "+N" badge marking a carried/ghost atlas card whose
     * identity also has copies in chests. Painted only while a search
     * query is active — outside of search the proximate-only top-right
     * pip carries the load, and remote stock is read off the search-
     * results panel. Sized like the kit-needs star so the visual
     * vocabulary stays consistent.
     */
    private void addAlsoStoredBadge(
            UIElement body,
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item,
            int storedCount
    ) {
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        float inset = Math.min(place.width() * 0.04f, atlas.worldUnitsForPixels(2f));
        float pipSizeRaw = Math.min(place.width() * 0.28f, atlas.worldUnitsForPixels(12f));
        float pipSize = Math.max(pipSizeRaw, place.width() * 0.10f);
        UIElement pip = panel(LINK_THREAD_COLOR).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(inset)
                .bottom(inset)
                .width(pipSize)
                .height(pipSize));
        pip.style(style -> style.zIndex(260));
        pip.setAllowHitTest(false);
        Label count = label("+" + Math.min(storedCount, 999), TEXT);
        count.layout(layout -> layout.widthPercent(100).heightPercent(100));
        count.setAllowHitTest(false);
        float requestedFontPx = pipSize * 0.55f * atlas.getScale();
        float fontWorld = clampScreenFontPx(requestedFontPx) / Math.max(0.0001f, atlas.getScale());
        count.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(fontWorld)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        pip.addChild(count);
        body.addChild(pip);
    }

    /**
     * Top-left "★" badge marking an item the active kit needs but the
     * player isn't carrying. Sized like the proximate-count pip but
     * positioned on the opposite corner so the two compose without
     * stacking.
     */
    private void addKitNeedsBadge(
            UIElement body,
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item
    ) {
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        float inset = Math.min(place.width() * 0.04f, atlas.worldUnitsForPixels(2f));
        float pipSizeRaw = Math.min(place.width() * 0.22f, atlas.worldUnitsForPixels(10f));
        float pipSize = Math.max(pipSizeRaw, place.width() * 0.08f);
        UIElement pip = panel(0xCCFFC66D).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(inset)
                .top(inset)
                .width(pipSize)
                .height(pipSize));
        pip.style(style -> style.zIndex(260));
        pip.setAllowHitTest(false);
        Label star = label("★", 0xFF1A1A1A);
        star.layout(layout -> layout.widthPercent(100).heightPercent(100));
        star.setAllowHitTest(false);
        float requestedPipFontPx = pipSize * 0.7f * atlas.getScale();
        float pipFontWorld = clampScreenFontPx(requestedPipFontPx) / Math.max(0.0001f, atlas.getScale());
        star.textStyle(style -> style
                .textColor(0xFF1A1A1A)
                .textShadow(false)
                .fontSize(pipFontWorld)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        pip.addChild(star);
        body.addChild(pip);
    }

    /**
     * Bottom-right pip showing the resolved desired count for this
     * identity. The kit-needed star sits top-left and the proximate-stock
     * pip sits top-right; bottom-right is the only free corner that
     * doesn't collide with the search-mode "also stored" badge
     * (bottom-left). Background colour reflects scope:
     * {@link WorkspaceTheme#DESIRED_COUNT_PIP_KIT} when the active kit's
     * value is in effect, {@link WorkspaceTheme#DESIRED_COUNT_PIP_GLOBAL}
     * otherwise — so the player can tell at a glance whether the count
     * applies forever or just while this kit is up.
     */
    private void addDesiredCountPip(
            UIElement body,
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item,
            AtlasRenderBudget budget
    ) {
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        float inset = Math.min(place.width() * 0.04f, atlas.worldUnitsForPixels(2f));
        float pipSizeRaw = Math.min(place.width() * 0.22f, atlas.worldUnitsForPixels(10f));
        float pipSize = Math.max(pipSizeRaw, place.width() * 0.08f);
        int pipColor = item.desiredCountFromKit() ? DESIRED_COUNT_PIP_KIT : DESIRED_COUNT_PIP_GLOBAL;
        UIElement pip = panel(pipColor).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .right(inset)
                .bottom(inset)
                .width(pipSize)
                .height(pipSize));
        pip.style(style -> style.zIndex(260));
        pip.setAllowHitTest(false);
        if (budget.level() != Band.REGION) {
            Label count = label(String.valueOf(Math.min(item.desiredCount(), 999)), TEXT);
            count.layout(layout -> layout.widthPercent(100).heightPercent(100));
            count.setAllowHitTest(false);
            float requestedPipFontPx = pipSize * 0.7f * atlas.getScale();
            float pipFontWorld = clampScreenFontPx(requestedPipFontPx) / Math.max(0.0001f, atlas.getScale());
            count.textStyle(style -> style
                    .textColor(TEXT)
                    .textShadow(false)
                    .fontSize(pipFontWorld)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            pip.addChild(count);
        }
        body.addChild(pip);
    }

    void addRelevanceDebugBadge(
            UIElement body,
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item
    ) {
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        String text = RelevanceDebugOverlay.formatScore(
                RelevanceDebugOverlay.scoreFor(item, host.viewModel, host.searchController.normalizedQuery())
        );
        float inset = Math.min(place.width() * 0.04f, atlas.worldUnitsForPixels(2f));
        float badgeHeight = Math.min(place.height() * 0.22f, atlas.worldUnitsForPixels(10f));
        float badgeWidth = Math.min(place.width() * 0.6f, atlas.worldUnitsForPixels(28f));
        UIElement badge = panel(0xCC0C141A).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(inset)
                .bottom(inset)
                .width(badgeWidth)
                .height(badgeHeight));
        badge.style(style -> style.zIndex(280));
        badge.setAllowHitTest(false);
        Label scoreLabel = label(text, ACCENT);
        scoreLabel.layout(layout -> layout.widthPercent(100).heightPercent(100));
        scoreLabel.setAllowHitTest(false);
        float requestedFontPx = badgeHeight * 0.75f * atlas.getScale();
        float fontWorld = clampScreenFontPx(requestedFontPx) / Math.max(0.0001f, atlas.getScale());
        scoreLabel.textStyle(style -> style
                .textColor(ACCENT)
                .textShadow(false)
                .fontSize(fontWorld)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        badge.addChild(scoreLabel);
        body.addChild(badge);
    }

    void addContainerFullnessBar(
            UIElement body,
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasItem item
    ) {
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        float inset = Math.min(place.width() * 0.04f, atlas.worldUnitsForPixels(2f));
        float trackWidth = place.width() - inset * 2f;
        if (trackWidth <= 0f) {
            return;
        }
        float barHeight = Math.max(
                atlas.worldUnitsForPixels(2f),
                Math.min(place.height() * 0.06f, atlas.worldUnitsForPixels(4f)));
        int capacity = Math.max(0, item.containerSlotCapacity());
        int free = Math.max(0, item.containerFreeSlotCount());
        int filled = Math.max(0, capacity - free);
        // Dim track keeps the bar visible even on an empty container, so the
        // bar itself is the "this is a carried container" signal.
        UIElement track = panel(CARRIED_CONTAINER_PIP & 0x66FFFFFF).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(inset)
                .top(inset)
                .width(trackWidth)
                .height(barHeight));
        // zIndex > DrawerHelper.drawItemStack's +232 Z push so the bar stays
        // visible when it overlaps the item icon (which can happen at large
        // LODs where the icon spans most of the card).
        track.style(style -> style.zIndex(260));
        track.setAllowHitTest(false);
        body.addChild(track);
        if (capacity > 0 && filled > 0) {
            float ratio = Math.min(1f, (float) filled / capacity);
            float fillWidth = Math.max(0f, trackWidth * ratio);
            int fillColor = fullnessColor(filled, capacity);
            UIElement fill = panel(fillColor).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(inset)
                    .top(inset)
                    .width(fillWidth)
                    .height(barHeight));
            fill.style(style -> style.zIndex(261));
            fill.setAllowHitTest(false);
            body.addChild(fill);
        }
    }

    int proximateChestCount(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || item.presence().isEmpty()) {
            return 0;
        }
        // presence is already proximate-only (built from proximate-chest
        // ghost projection in the view model).
        int total = 0;
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
            total += entry.count();
        }
        return total;
    }

    /** Reference to a proximate chest holding the identity. The server
     *  resolves the actual slot index when servicing the take RPC. */
    record ChestSlotRef(String storageId, int chestSlotIndex) {
    }

    ChestSlotRef firstProximateChestSlotFor(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || item.presence().isEmpty()) {
            return null;
        }
        // Slot-level info is no longer projected client-side. Take-from-chest
        // RPCs accept slot index 0 here as a placeholder; the server-side
        // executor walks the chest for a matching identity. (Full slot-
        // precision requires a chest-content overlay surface, deferred.)
        return new ChestSlotRef(item.presence().get(0).storageId(), 0);
    }

    UIElement anchorTextBand(
            SlotAtlasGraphView atlas,
            String text,
            int color,
            float screenFontPx,
            int maxLength,
            int lines,
            int backgroundColor,
            Horizontal align
    ) {
        // Label rendering sits inside GraphView's scaled content transform,
        // so a world-unit fontSize renders at (fontSize * atlas.getScale())
        // screen pixels. Baking fontSize once at build-time makes the label
        // drift as zoom changes (shrinks on zoom-out, grows on zoom-in).
        // Instead, we install a TICK listener that recomputes
        // world-fontSize = screenFontPx / currentScale each frame, keeping
        // rendered screen pixels ~constant. This mirrors the island-header
        // pattern.
        // Use the actual render scale (getScale) instead of scaleForContent
        // for sizing the label. During a camera animation rebuildAtlasBody
        // pins scaleForContent to the animation *target* — so if we baked
        // the world fontSize from that, the first rendered frame would
        // draw fontSize×currentScale, and when target diverges strongly
        // from current (e.g. zoom-out peek) the label flashes oversized
        // for a frame before the TICK below corrects it. getScale() is
        // always the real scale the pose stack will apply, so the initial
        // value already matches what you see on screen.
        float initialScale = Math.max(0.0001f, atlas.getScale());
        float snappedScreenFont = clampScreenFontPx(screenFontPx);
        float initialWorldFont = snappedScreenFont / initialScale;
        float initialWorldPad = 1f / initialScale;
        float initialLineSpacing = lines > 1 ? initialWorldPad : 0f;

        UIElement band = panel(backgroundColor).layout(layout -> layout
                .paddingHorizontal(initialWorldPad)
                .paddingVertical(initialWorldPad)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        band.setAllowHitTest(false);
        if (lines <= 1) {
            band.setOverflowVisible(false);
        }
        String displayText = compactAnchorText(text, maxLength);
        Label token = anchorLabel(displayText, color, initialWorldFont);
        token.layout(layout -> layout.widthPercent(100).heightPercent(100));
        token.textStyle(style -> style
                .fontSize(initialWorldFont)
                .lineSpacing(initialLineSpacing)
                .textWrap(lines > 1 ? TextWrap.WRAP : TextWrap.NONE)
                .textAlignVertical(lines > 1 ? Vertical.TOP : Vertical.CENTER)
                .textAlignHorizontal(align));
        band.addChild(token);

        // Quantize the *world* fontSize to quarter-unit steps so tiny zoom
        // deltas don't trigger a TextElement.recompute every frame. That
        // keeps rendered pixels within ~0.25 screen-px of the target while
        // capping fontSize writes to a handful per zoom range.
        int[] lastFontQuarter = {Math.round(initialWorldFont * 4f)};
        float[] lastScale = {initialScale};
        band.addEventListener(UIEvents.TICK, event -> {
            // Track the actual render scale (not animation target) so the
            // label stays sized correctly every frame during animations
            // instead of flashing to the target-scale bake until the
            // animation settles.
            float scale = Math.max(0.0001f, atlas.getScale());
            float worldFont = snappedScreenFont / scale;
            int fontQuarter = Math.max(1, Math.round(worldFont * 4f));
            boolean scaleChanged = scale != lastScale[0];
            if (fontQuarter != lastFontQuarter[0]) {
                lastFontQuarter[0] = fontQuarter;
                float quantizedWorldFont = fontQuarter / 4f;
                float lineSpacing = lines > 1 ? 1f / scale : 0f;
                token.textStyle(style -> style
                        .fontSize(quantizedWorldFont)
                        .lineSpacing(lineSpacing));
            }
            if (scaleChanged) {
                lastScale[0] = scale;
                float pad = 1f / scale;
                band.layout(layout -> layout
                        .paddingHorizontal(pad)
                        .paddingVertical(pad)
                        .alignItems(AlignItems.CENTER)
                        .flexDirection(FlexDirection.ROW));
                band.markTaffyStyleDirty();
            }
        });

        return band;
    }

    void applyAtlasCardLayout(Button button, SlotWorkspaceViewModel.AtlasItem item) {
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        button.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(place.x())
                .top(place.y())
                .width(place.width())
                .height(place.height())
                .paddingAll(0));
    }

    Button atlasCardButton(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasItem item) {
        boolean selected = item.identity().equals(host.selectedAtlasIdentity.get());
        boolean searchMatch = host.searchController.matchesItem(item);
        boolean activeSearchMatch = !host.searchController.normalizedQuery().isBlank() && searchMatch;
        AtlasRenderBudget initialBudget = atlasBudget(atlas, item);
        Button button = button("", true, cardChromeColor(initialBudget.level(), selected, searchMatch, item.recent(), item.carried(), !host.searchController.normalizedQuery().isBlank()));
        applyAtlasCardLayout(button, item);
        button.noText();
        button.style(style -> style.zIndex(2));
        // Cursor handling on atlas cards. Pickup is wired via a hotbar
        // fallback: the AtlasItem doesn't project per-slot source info for
        // non-hotbar carried slots (e.g. main inventory, backpack), so
        // ctrl+right pickup looks up the identity on the hotbar and picks
        // from there if found, otherwise refuses with a helpful status.
        // Drops are not implemented for atlas cards (requires "send home"
        // semantics with a count override). Any other click while carrying
        // cancels — atlas cards stopPropagation, so the bubble-cancel
        // handler on root never fires for them.
        button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (handleCursorAtlasGesture(event, item)) {
                event.stopPropagation();
            }
        }, true);
        button.setOnClick(event -> {
            event.stopPropagation();
            if (Screen.hasShiftDown()) {
                // Pull from chests whenever proximate stock exists, even
                // for already-carried items. Without this, the moment the
                // first chest yields a stack the card flips to carried and
                // subsequent shift+clicks fall to the "send to hotbar"
                // branch — leaving the rest of the item unreachable in the
                // remaining proximate chests. Carried items with no chest
                // stock still get the original "send to hotbar" semantic.
                SlotWorkspaceViewModel.AtlasItem fresh = host.viewModel.atlasItem(item.identity());
                SlotWorkspaceViewModel.AtlasItem target = fresh != null ? fresh : item;
                if (proximateChestCount(target) > 0) {
                    if (host.viewModel.carriedFreeSlotCount() <= 0 && !target.carried()) {
                        host.localStatus.set("carry full — drop something first");
                        host.rebuild();
                        return;
                    }
                    host.rpc.sendTakeStackByIdentity(target.identity());
                } else {
                    host.rpc.sendAssignHomeToFreeHotbar(target);
                }
                return;
            }
            host.selectedAtlasIdentity.set(item.identity());
            host.selectedHotbarIndex.set(-1);
            host.localStatus.set(item.playerPlaced()
                    ? "selected homed item: drag to hotbar or another island"
                    : "selected inbox item: drag to an island or create one");
        });
        button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) {
                event.stopPropagation();
                host.menu.openContextMenuForAtlas(item, event.x, event.y);
            }
        });
        float[] scrollAccumulator = {0f};
        float[] desiredScrollAccumulator = {0f};
        button.addEventListener(UIEvents.MOUSE_WHEEL, event -> {
            // ctrl+scroll: adjust player-scoped desired count (±1 per tick).
            // Mirrors the shift+scroll cadence so the gesture vocabulary stays
            // parallel — shift = "move N items", ctrl = "want N items." The
            // dispatcher accumulator deduplicates the exact ±1 increments on
            // touchpads where one notch may produce multiple sub-1 deltas.
            if (Screen.hasControlDown()) {
                if (host.cursor.isCarrying()) {
                    return;
                }
                float dDelta = event.deltaY != 0f ? event.deltaY : event.deltaX;
                if (dDelta == 0f) {
                    return;
                }
                event.stopPropagation();
                desiredScrollAccumulator[0] += dDelta;
                int desiredDelta = (int) desiredScrollAccumulator[0];
                if (desiredDelta == 0) {
                    return;
                }
                desiredScrollAccumulator[0] -= desiredDelta;
                host.rpc.sendAdjustPlayerDesiredCount(item.identity(), desiredDelta);
                return;
            }
            if (!Screen.hasShiftDown()) {
                return;
            }
            // shift+scroll moves items between carry and chests, which would
            // mutate the cursor's recorded source mid-flight. Suppress while
            // the cursor is non-empty so the player can't accidentally
            // desync the cursor from its origin.
            if (host.cursor.isCarrying()) {
                return;
            }
            // Minecraft swaps scrollX ↔ scrollY when shift is held, so the
            // scroll magnitude lands in deltaX under our shift-scroll gesture.
            float delta = event.deltaY != 0f ? event.deltaY : event.deltaX;
            if (delta == 0f) {
                return;
            }
            event.stopPropagation();
            scrollAccumulator[0] += delta;
            int count = (int) scrollAccumulator[0];
            if (count == 0) {
                return;
            }
            scrollAccumulator[0] -= count;
            SlotWorkspaceViewModel.AtlasItem fresh = host.viewModel.atlasItem(item.identity());
            if (fresh == null) {
                return;
            }
            int magnitude = Math.abs(count);
            if (count > 0) {
                // Take: walk all proximate chests server-side and pull from
                // the first slot matching this identity (highest-affinity
                // chest first). Works for both ghost cards and carried
                // cards — for carried ones it consolidates more from chests.
                if (proximateChestCount(fresh) <= 0) {
                    host.localStatus.set("no nearby chest has " + fresh.name());
                    return;
                }
                if (host.viewModel.carriedFreeSlotCount() <= 0 && !fresh.carried()) {
                    // Carry has zero free slots and the item isn't already
                    // mergeable into a carried stack — server take will
                    // silently fail. Short-circuit with a clear message.
                    host.localStatus.set("carry full — drop something first");
                    host.rebuild();
                    return;
                }
                for (int i = 0; i < magnitude; i++) {
                    host.rpc.sendTakeOneByIdentity(fresh.identity());
                }
            } else {
                if (!host.anyChestProximate()) {
                    host.localStatus.set("no nearby chest to push " + fresh.name());
                    return;
                }
                if (!fresh.carried()) {
                    host.localStatus.set(fresh.name() + " not carried");
                    return;
                }
                for (int i = 0; i < magnitude; i++) {
                    host.rpc.sendDepositOneHomeToLinkedChest(fresh);
                }
            }
        });
        button.addEventListener(UIEvents.MOUSE_ENTER, event -> host.hoveredAtlasIdentity = item.identity(), true);
        button.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (item.identity().equals(host.hoveredAtlasIdentity)) {
                host.hoveredAtlasIdentity = null;
            }
        }, true);
        host.drag.installAtlasHoverTooltip(button, item);
        host.drag.installAtlasItemDragSource(button, item);
        installChestHoverPaint(button, item);

        UIElement body = new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));
        body.setAllowHitTest(false);
        rebuildAtlasBody(body, atlas, item, initialBudget, activeSearchMatch);
        button.addChild(body);

        long[] lastSignature = new long[]{atlasLayoutSignature(initialBudget)};
        button.addEventListener(UIEvents.TICK, event -> {
            AtlasRenderBudget budget = atlasBudget(atlas, item);
            boolean currentSelected = item.identity().equals(host.selectedAtlasIdentity.get());
            boolean focused = host.isMapFocusItem(item);
            long signature = atlasLayoutSignature(budget);
            // Skip LOD rebuilds while the camera is animating. atlasBudget
            // uses animationTargetScale while rendering uses the live
            // interpolated scale, so a host.rebuild mid-animation bakes labels
            // for the target and draws them at the current scale — visible
            // as a big-text flash at the start of a zoom-in peek. Letting
            // cards stay at the pre-animation LOD means labels either
            // scale with the zoom or stay absent until the camera settles;
            // either way it's continuous, not a jump.
            if (signature != lastSignature[0] && !host.cameraController.isAnimating()) {
                rebuildAtlasBody(body, atlas, item, budget, activeSearchMatch);
                body.markTaffyStyleDirty();
                button.markTaffyStyleDirty();
                lastSignature[0] = signature;
            }
            button.style(style -> style.zIndex(focused ? 10 : currentSelected ? 7 : 2));
            applyButtonColors(button, true, cardChromeColor(budget.level(), currentSelected, searchMatch, item.recent(), item.carried(), !host.searchController.normalizedQuery().isBlank()));
        });
        return button;
    }

    /**
     * Atlas-card MOUSE_DOWN classifier. Returns true when the click was
     * consumed by cursor logic (caller should stopPropagation). Pickup
     * uses the AtlasItem's largest-carried-slot info (set server-side
     * during view model build) so the cursor sources from a real slot
     * regardless of where the items live — main, hotbar, backpack, or
     * offhand. Falls back to the item's own displayStack if the
     * largest-slot info isn't populated (legacy / chest-only items).
     * Drops on atlas cards aren't wired (route via "send home" with
     * count override is a follow-up); a non-pickup click while carrying
     * cancels.
     */
    private boolean handleCursorAtlasGesture(
            com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event,
            SlotWorkspaceViewModel.AtlasItem item
    ) {
        boolean carrying = host.cursor.isCarrying();
        WorkspaceCursorGestures.Result mode = WorkspaceCursorGestures.classify(event, carrying);
        if (mode == WorkspaceCursorGestures.Result.PICKUP_HALF) {
            if (!item.hasLargestCarriedSlot()) {
                host.localStatus.set(item.name() + " has no carried slot — pick up only works on items you carry");
                host.rebuild();
                return true;
            }
            boolean picked = host.cursor.pickupHalf(
                    item.largestCarriedSourceId(),
                    item.largestCarriedSlotIndex(),
                    item.identity(),
                    item.displayStack(),
                    item.largestCarriedSlotCount());
            if (picked) {
                host.localStatus.set("cursor: " + host.cursor.current().count() + " " + item.name());
            } else if (carrying) {
                host.localStatus.set("cursor already holds another item — drop or ESC first");
            }
            host.rebuild();
            return true;
        }
        if (carrying) {
            // Any non-pickup click while carrying cancels (atlas cards
            // aren't drop targets in the initial cut).
            host.cursor.clear();
            host.localStatus.set("cursor cancelled");
            host.rebuild();
            return true;
        }
        return false;
    }

    void addAtlasItemChips(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasItem item) {
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        List<ChipSuggestion> chips = item.chipSuggestions();
        if (chips.isEmpty()) {
            return;
        }
        int chipHeight = 10;
        int chipGap = 1;
        for (int index = 0; index < chips.size(); index++) {
            ChipSuggestion chip = chips.get(index);
            int top = place.y() + place.height() + 2 + index * (chipHeight + chipGap);
            Button chipButton = button("", true, chip.color());
            chipButton.noText();
            chipButton.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(place.x())
                    .top(top)
                    .width(place.width())
                    .height(chipHeight)
                    .paddingAll(1)
                    .gapAll(2)
                    .flexDirection(FlexDirection.ROW)
                    .alignItems(AlignItems.CENTER));
            chipButton.style(style -> style.zIndex(3));
            chipButton.setOnClick(event -> {
                event.stopPropagation();
                host.rpc.sendChipAccept(item, chip);
            });
            Label chipLabel = label(SlotWorkspaceUiController.chipLabelText(chip), TEXT);
            chipLabel.layout(layout -> layout.flex(1).height(chipHeight - 2));
            chipLabel.textStyle(style -> style
                    .textColor(TEXT)
                    .fontSize(6)
                    .textShadow(false)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            chipLabel.setAllowHitTest(false);
            chipButton.addChild(chipLabel);
            atlas.addContentChild(chipButton);
        }
    }

}
