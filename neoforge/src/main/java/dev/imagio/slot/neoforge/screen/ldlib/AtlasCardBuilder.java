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
        band.setAllowHitTest(true);
        String targetStorageId = item.presence().get(0).storageId();
        band.addEventListener(UIEvents.CLICK, event -> {
            SlotWorkspaceViewModel.ClaimedChestTile tile = host.viewModel.claimedChestTile(targetStorageId);
            if (tile != null) {
                event.stopPropagation();
                host.camera.panToChestTile(atlas, tile);
                host.localStatus.set("panned to " + tile.label());
                host.rebuild();
            }
        });
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

        int shellColor = carried ? 0xB0141B23 : dimAlpha(0xB0141B23, GHOST_CARD_ALPHA);
        int innerColor = carried ? 0xD90A1218 : dimAlpha(0xD90A1218, GHOST_CARD_ALPHA);

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
     * Visual size multiplier for non-carried ("ghost") atlas cards. Carried
     * items always render at full size. Ghosts shrink progressively as we
     * zoom out, becoming a small dot at REGION/PIP and full-size at
     * DETAIL/INSPECT — at high zoom you can already see every card.
     */
    static float ghostScaleFor(boolean carried, Band band) {
        if (carried || band == null) {
            return 1f;
        }
        return switch (band) {
            case DETAIL -> 1f;
            case INSPECT -> 0.95f;
            case READ -> 0.7f;
            case BROWSE -> 0.5f;
            case REGION, PIP -> 0.4f;
        };
    }

    UIElement slotPreview(SlotWorkspaceViewModel.AtlasItem item, int size, boolean showMarker) {
        float shell = size;
        float inset = 1f;
        float icon = Math.max(10f, size - 4f);
        boolean carried = item.carried();
        int shellColor = carried ? 0xB0141B23 : dimAlpha(0xB0141B23, GHOST_CARD_ALPHA);
        int innerColor = carried ? 0xD90A1218 : dimAlpha(0xD90A1218, GHOST_CARD_ALPHA);
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
            AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
            float sideInset = Math.min(place.width() * 0.04f, atlas.worldUnitsForPixels(2f));
            float bottomInset = Math.min(place.height() * 0.04f, atlas.worldUnitsForPixels(1f));
            float barHeight = Math.min(place.height() * 0.08f, atlas.worldUnitsForPixels(2f));
            float barWidth = Math.max(place.width() * 0.4f, place.width() - sideInset * 2f);
            body.addChild(panel(ACCENT).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(sideInset)
                    .bottom(bottomInset)
                    .width(barWidth)
                    .height(barHeight)));
        }
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
        if (RelevanceDebugOverlay.enabled()) {
            addRelevanceDebugBadge(body, atlas, item);
        }
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
        int total = 0;
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
            SlotWorkspaceViewModel.ClaimedChestTile tile =
                    host.viewModel.claimedChestTile(entry.storageId());
            if (tile != null && tile.proximate()) {
                total += entry.count();
            }
        }
        return total;
    }

    record ChestSlotRef(String storageId, int chestSlotIndex) {
    }

    ChestSlotRef firstProximateChestSlotFor(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null) {
            return null;
        }
        SlotWorkspaceViewModel.IdentityRef identity = item.identity();
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : host.viewModel.claimedChestTiles()) {
            if (!tile.proximate()) {
                continue;
            }
            java.util.List<ItemStack> contents = tile.contents();
            java.util.List<Integer> indices = tile.contentSlotIndices();
            for (int i = 0; i < contents.size(); i++) {
                ItemStack stack = contents.get(i);
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                SlotWorkspaceViewModel.IdentityRef cellIdentity = SlotWorkspaceViewModel.IdentityRef.from(
                        dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(stack));
                if (identity.equals(cellIdentity)) {
                    int slotIdx = i < indices.size() ? indices.get(i) : i;
                    return new ChestSlotRef(tile.storageId(), slotIdx);
                }
            }
        }
        return null;
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
        button.setOnClick(event -> {
            event.stopPropagation();
            if (Screen.hasShiftDown()) {
                host.rpc.sendAssignHomeToFreeHotbar(item);
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
        button.addEventListener(UIEvents.MOUSE_WHEEL, event -> {
            if (!Screen.hasShiftDown()) {
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
                AtlasCardBuilder.ChestSlotRef source = firstProximateChestSlotFor(fresh);
                if (source == null) {
                    host.localStatus.set("no nearby chest has " + fresh.name());
                    return;
                }
                for (int i = 0; i < magnitude; i++) {
                    host.rpc.sendTakeOneFromChest(source.storageId(), source.chestSlotIndex());
                }
            } else {
                boolean canPush = host.atlasItemHasDepositTarget(fresh)
                        || firstProximateChestSlotFor(fresh) != null;
                if (!canPush) {
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
        // Items sit on top of their island panel (z=2 vs z=1) and receive
        // their own mouse enter/leave, so hovering an item inside an
        // island must also flip host.hoveredIslandId — otherwise the
        // cross-surface highlight on linked chest cards only fires when
        // the cursor happens to hit empty island background between cards.
        SlotWorkspaceViewModel.AtlasIsland hoverIsland = host.viewModel.island(item.islandId());
        host.islandChest.attachIslandHoverListeners(button, hoverIsland);
        return button;
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
