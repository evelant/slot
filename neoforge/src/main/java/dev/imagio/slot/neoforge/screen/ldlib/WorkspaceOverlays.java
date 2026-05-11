package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.atlas.AtlasSearchIndex;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.action.WorkspaceActionId;
import dev.imagio.slot.ui.workspace.WorkspaceGatherUiSupport;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import net.minecraft.network.chat.Component;

final class WorkspaceOverlays {
    private static final int CARRIED_CHIP_WIDTH = 60;
    private static final int CARRIED_CHIP_HEIGHT = 16;
    private static final int CARRIED_CHIP_BAR_HEIGHT = 2;

    private final SlotWorkspaceUiController host;

    WorkspaceOverlays(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement topRightActionsOverlay() {
        // In-flow row inside the top status bar; sized to its content
        // (no widthPercent), so the parent flex-row gives the search
        // hint the rest of the slack on its left. Vanilla button is
        // always visible; deposit only reveals itself when a claimed
        // chest is proximate (TICK-poll pattern).
        UIElement overlay = new UIElement().layout(layout -> layout
                .gapAll(6)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        overlay.style(style -> style.zIndex(11));

        // Deposit + gather stay in the top-right cluster all the time so
        // the player can see they exist; the chrome flips between
        // ACCENT (actionable) and MUTED (not actionable) instead of
        // hiding the button entirely. With 16-px icons the on/off
        // toggle from the text-button era was too easy to miss.
        boolean initialDepositEnabled = host.anyChestProximate() && countDepositable() > 0;
        Button depositButton = button("", true, initialDepositEnabled ? ACCENT : MUTED).noText();
        depositButton.addPreIcon(Icons.EXPORT);
        // Without this the icon child absorbs the cursor's hit-test and
        // fires MOUSE_LEAVE on the button as soon as the player moves
        // onto the icon — killing the deposit-preview outline.
        noChildHitTest(depositButton);
        depositButton.layout(layout -> layout.width(16).height(16));
        boolean[] lastDepositEnabled = {initialDepositEnabled};
        depositButton.addEventListener(UIEvents.TICK, event -> {
            boolean enabled = host.anyChestProximate() && countDepositable() > 0;
            if (enabled != lastDepositEnabled[0]) {
                lastDepositEnabled[0] = enabled;
                applyButtonColors(depositButton, true, enabled ? ACCENT : MUTED);
            }
        });
        depositButton.addEventListener(UIEvents.MOUSE_ENTER, event -> {
            // Light up matching atlas cards while the player considers
            // the click. Cards read host.depositPreviewActive on TICK
            // and paint an outline when their identity is in
            // viewModel.depositableIdentities().
            host.depositPreviewActive = true;
        });
        depositButton.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            host.depositPreviewActive = false;
        });
        depositButton.setOnClick(event -> {
            event.stopPropagation();
            boolean proximate = host.anyChestProximate();
            int chipCount = host.viewModel.chestChips().size();
            int proximateCount = 0;
            for (SlotWorkspaceViewModel.ChestChip chip : host.viewModel.chestChips()) {
                if (chip.proximate()) {
                    proximateCount++;
                }
            }
            dev.imagio.slot.SlotCommon.LOGGER.info(
                    "[SLOT] deposit button clicked: proximate={} chestChips={} proximateChips={}",
                    proximate, chipCount, proximateCount);
            if (!proximate) {
                host.localStatus.set("no proximate chest — walk closer to a claimed chest");
                return;
            }
            host.rpc.sendDeposit();
        });
        // Deposit is driven by explicit organization signals: learned
        // affinity or existing matching contents in a proximate chest. Items
        // with neither signal stay in carry. The tooltip surfaces this so
        // the player understands "nothing to deposit" instead of assuming
        // the button is broken.
        host.installTextTooltip(
                depositButton,
                Component.literal(
                        "Deposit carried items into proximate chests by learned affinity or matching contents. "
                                + "Items without either signal stay in carry."));

        // Top-level Gather button. Common gather semantics cover both
        // player-global desired counts and active-kit needs; the wire name
        // remains GATHER_ACTIVE_KIT for compatibility with the existing
        // action vocabulary.
        boolean initialGatherEnabled = host.anyChestProximate() && anyGatherableIdentity();
        Button gatherButton = button("", true, initialGatherEnabled ? ACTIVE_HOTBAR : MUTED).noText();
        gatherButton.addPreIcon(Icons.IMPORT);
        noChildHitTest(gatherButton);
        gatherButton.layout(layout -> layout.width(16).height(16));
        boolean[] lastGatherEnabled = {initialGatherEnabled};
        gatherButton.addEventListener(UIEvents.TICK, event -> {
            boolean enabled = host.anyChestProximate() && anyGatherableIdentity();
            if (enabled == lastGatherEnabled[0]) {
                return;
            }
            lastGatherEnabled[0] = enabled;
            applyButtonColors(gatherButton, true, enabled ? ACTIVE_HOTBAR : MUTED);
        });
        gatherButton.addEventListener(UIEvents.MOUSE_ENTER, event -> {
            // Mirrors the deposit-preview hover: cards + TOC rows for
            // identities that would actually be pulled in a single click
            // light up while the cursor is over Gather.
            host.gatherPreviewActive = true;
        });
        gatherButton.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            host.gatherPreviewActive = false;
        });
        gatherButton.setOnClick(event -> {
            event.stopPropagation();
            if (!host.anyChestProximate() || !anyGatherableIdentity()) {
                host.localStatus.set("nothing to gather from nearby chests");
                host.rebuild();
                return;
            }
            dev.imagio.slot.SlotCommon.LOGGER.info(
                    "[SLOT] gather button clicked: activeKit={} gatherable={}",
                    host.viewModel.activeKit() == null ? "<none>" : host.viewModel.activeKit().kitId(),
                    anyGatherableIdentity());
            host.rpc.send(WorkspaceActionId.GATHER_ACTIVE_KIT);
            host.localStatus.set("gathering desired items from nearby chests");
            host.rebuild();
        });
        host.installKeybindTooltip(
                gatherButton,
                "Pull every item with a desired-count gap from nearby chests",
                () -> dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                        .gatherActiveKitMapping().getTranslatedKeyMessage().getString()
        );

        // HISTORY is a counter-clockwise curved arrow (classic undo);
        // ROTATION is a clockwise curved arrow (matches redo convention).
        // LEFT/RIGHT would read as paging, not undo/redo, so avoid those.
        Button undoButton = button("", true, GLASS).noText();
        undoButton.addPreIcon(Icons.HISTORY);
        undoButton.layout(layout -> layout.width(16).height(16));
        undoButton.setOnClick(event -> {
            event.stopPropagation();
            host.rpc.sendUndo();
        });
        host.installKeybindTooltip(
                undoButton,
                "Undo",
                dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings::undoKeyLabel
        );

        Button redoButton = button("", true, GLASS).noText();
        redoButton.addPreIcon(Icons.ROTATION);
        redoButton.layout(layout -> layout.width(16).height(16));
        redoButton.setOnClick(event -> {
            event.stopPropagation();
            host.rpc.sendRedo();
        });
        host.installKeybindTooltip(
                redoButton,
                "Redo",
                dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings::redoKeyLabel
        );

        Button vanillaButton = button("", true, GLASS).noText();
        vanillaButton.addPreIcon(Icons.GRID);
        vanillaButton.layout(layout -> layout.width(16).height(16));
        vanillaButton.setOnClick(event -> {
            event.stopPropagation();
            dev.imagio.slot.neoforge.client.screen.SlotWorkspaceMountController.openVanillaInventory();
        });
        host.installTextTooltip(
                vanillaButton,
                Component.literal("Open vanilla inventory (safety fallback)"));

        // Persistent disable: flips the config flag and drops to vanilla
        // for the rest of the session (and beyond — config persists). The
        // vanilla inventory screen carries a "Re-enable SLOT" pill so the
        // route back is one click.
        Button disableButton = button("", true, GLASS).noText();
        disableButton.addPreIcon(Icons.EYE_OFF);
        disableButton.layout(layout -> layout.width(16).height(16));
        disableButton.setOnClick(event -> {
            event.stopPropagation();
            dev.imagio.slot.neoforge.config.SlotClientConfig.CLIENT.slotEnabled.set(false);
            dev.imagio.slot.neoforge.config.SlotClientConfig.CLIENT.slotEnabled.save();
            dev.imagio.slot.neoforge.client.screen.SlotWorkspaceMountController.openVanillaInventory();
        });
        host.installTextTooltip(
                disableButton,
                Component.literal(
                        "Disable SLOT — vanilla inventory opens until re-enabled from the vanilla screen."));

        overlay.addChildren(gatherButton, depositButton, undoButton, redoButton, vanillaButton, disableButton);
        return overlay;
    }

    /**
     * Number of distinct carried identities the planner would route into
     * a proximate chest right now. Read off
     * {@link SlotWorkspaceViewModel#depositableIdentities()} which the
     * server projects from affinity and live chest contents. Drives the
     * deposit button's "Deposit (N)" label and visibility.
     */
    private int countDepositable() {
        return host.viewModel.depositableIdentities().size();
    }

    /**
     * True iff any atlas identity has a positive desired-count gap (or
     * is kit-needed) AND is reachable from a proximate chest. Drives
     * the post-Phase-6 Gather button visibility, replacing the
     * active-kit-only gate.
     */
    private boolean anyGatherableIdentity() {
        for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
            if (WorkspaceGatherUiSupport.isGatherableItem(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Full-width search modal row, mounted by
     * {@link ListWallPanelBuilder#repopulateWallPanel} when the search
     * modal is active — it replaces the whole top row (hint + carry chip
     * + actions) for the duration of the search session. Single line:
     * buffer label on the left ({@code adaptiveWidth} so it sizes to its
     * text), summary label on the right with {@code flex(1)} so it
     * grows to fill remaining space without wrapping.
     *
     * <p>Earlier versions tried to squeeze a column-stacked chip into
     * the 3-element top row's middle slot — when the player started
     * typing, flex-shrink collapsed the chip down to a sliver where
     * {@code wrappedLabel.widthPercent(100)} promptly wrapped the
     * summary into a vertical column of single characters. Replacing
     * the row outright sidesteps the whole conflict.
     */
    UIElement searchModalRowOverlay() {
        UIElement row = panel(GLASS).layout(layout -> layout
                .widthPercent(100)
                .gapAll(8)
                .paddingHorizontal(8)
                .paddingVertical(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        row.style(style -> style.zIndex(12));
        row.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        String bufferDisplay = "/" + host.searchController.buffer() + "_";
        Label bufferLabel = label(bufferDisplay, ACCENT);
        bufferLabel.textStyle(style -> style.adaptiveWidth(true));
        row.addChild(bufferLabel);

        String summary;
        if (host.searchController.buffer().length() < AtlasSearchIndex.DEFAULT_MIN_QUERY_CHARS) {
            summary = "Type " + AtlasSearchIndex.DEFAULT_MIN_QUERY_CHARS
                    + "+ chars  ·  Esc to close";
        } else if (host.searchController.matches().isEmpty()) {
            summary = "No matches  ·  Esc to close";
        } else {
            String commitHint = host.searchController.interactionDisablesAutoDismiss()
                    ? "Esc to close"
                    : "idle auto-commits  ·  Esc to abort";
            summary = (host.searchController.matchIndex() + 1) + " of " + host.searchController.matches().size()
                    + " matches  ·  Tab cycle  ·  Enter commit  ·  " + commitHint;
        }
        Label summaryLabel = label(summary, MUTED);
        summaryLabel.layout(layout -> layout.flex(1));
        row.addChild(summaryLabel);
        return row;
    }

    UIElement searchHintOverlay() {
        // adaptiveWidth(true) is required: without it Taffy gives the
        // Label its parent's available width (auto-grow), so the
        // GLASS background paints across the entire top row and the
        // SPACE_BETWEEN-aligned free-slots chip ends up sitting on
        // top of the Label's invisible right half. With adaptiveWidth
        // the recompute() in TextElement pins the label's layout
        // width to the measured text width — the background hugs the
        // text, and the chip's flex slot gets the slack it expects.
        Label hint = label("Press / to search", MUTED);
        hint.layout(layout -> layout
                .paddingHorizontal(8)
                .paddingVertical(4));
        hint.textStyle(style -> style.adaptiveWidth(true));
        hint.style(style -> style.zIndex(11).backgroundTexture(rect(GLASS)));
        hint.setAllowHitTest(false);
        return hint;
    }

    UIElement carriedFreeSlotsChip() {
        int initialFree = host.viewModel == null ? 0 : host.viewModel.carriedFreeSlotCount();
        int initialCapacity = host.viewModel == null ? 0 : host.viewModel.carriedSlotCapacity();
        UIElement chip = panel(GLASS).layout(layout -> layout
                .width(CARRIED_CHIP_WIDTH)
                .height(CARRIED_CHIP_HEIGHT)
                .paddingHorizontal(4)
                .paddingTop(2)
                .paddingBottom(CARRIED_CHIP_BAR_HEIGHT + 1)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        chip.style(style -> style.zIndex(11));
        chip.setAllowHitTest(false);
        Label valueLabel = label(formatFreeSlots(initialFree), TEXT);
        valueLabel.layout(layout -> layout.widthPercent(100).flex(1));
        valueLabel.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.CENTER));
        chip.addChild(valueLabel);
        // Fullness bar pinned to the bottom edge of the chip: the width
        // is the *filled* portion (capacity - free), so the bar grows
        // toward a warning state as the host.player's inventory fills up.
        int initialFilled = Math.max(0, initialCapacity - initialFree);
        int initialBarColor = fullnessColor(initialFilled, initialCapacity);
        float initialBarWidth = fullnessBarWidth(initialFilled, initialCapacity,
                CARRIED_CHIP_WIDTH);
        UIElement bar = panel(initialBarColor).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0)
                .bottom(0)
                .width(initialBarWidth)
                .height(CARRIED_CHIP_BAR_HEIGHT));
        bar.style(style -> style.zIndex(12));
        bar.setAllowHitTest(false);
        chip.addChild(bar);
        int[] lastFree = {initialFree};
        int[] lastBarColor = {initialBarColor};
        float[] lastBarWidth = {initialBarWidth};
        chip.addEventListener(UIEvents.TICK, event -> {
            int free = host.viewModel == null ? 0 : host.viewModel.carriedFreeSlotCount();
            int capacity = host.viewModel == null ? 0 : host.viewModel.carriedSlotCapacity();
            if (free != lastFree[0]) {
                lastFree[0] = free;
                valueLabel.setText(Component.literal(formatFreeSlots(free)));
            }
            int filled = Math.max(0, capacity - free);
            int barColor = fullnessColor(filled, capacity);
            if (barColor != lastBarColor[0]) {
                lastBarColor[0] = barColor;
                bar.style(style -> style.backgroundTexture(rect(barColor)));
            }
            float barWidth = fullnessBarWidth(filled, capacity, CARRIED_CHIP_WIDTH);
            if (Math.abs(barWidth - lastBarWidth[0]) > 0.5f) {
                lastBarWidth[0] = barWidth;
                bar.layout(layout -> layout.width(barWidth));
            }
        });
        return chip;
    }

    UIElement inspectorPanel() {
        UIElement panel = panel(PANEL).layout(layout -> layout
                .width(284)
                .heightPercent(100)
                .paddingAll(8)
                .gapAll(6)
                .flexDirection(FlexDirection.COLUMN));
        host.clearSelectionOnDirectClick(panel);
        panel.addChildren(host.belt.selectionPanel());
        return panel;
    }

    /**
     * Debug-only thin status row. Shows the latest local status text +
     * view-model revision / pending counts so we have an at-a-glance
     * trace during playtest. No background, no padding, just a small
     * muted line — the goal is "doesn't interfere with the layout."
     * Sits in-flow between the scroller mid-row and the kit / belt
     * footer (added by {@link ListWallPanelBuilder#repopulateWallPanel}).
     * Once the workspace UX is stable this can be deleted entirely;
     * for now it's load-bearing for diagnosing sync / RPC issues.
     */
    UIElement statusBar() {
        if (host.statusBarElement == null) {
            host.statusBarLabel = label("", MUTED);
            host.statusBarLabel.layout(layout -> layout.widthPercent(100).height(8));
            host.statusBarLabel.textStyle(style -> style
                    .textColor(MUTED)
                    .textShadow(false)
                    .fontSize(6));
            host.statusBarLabel.style(style -> style.zIndex(8));
            host.statusBarLabel.setAllowHitTest(false);
            host.statusBarElement = host.statusBarLabel;
            host.localStatus.subscribe(v -> refreshStatusBarLabel());
        }
        refreshStatusBarLabel();
        return host.statusBarElement;
    }

    void refreshStatusBarLabel() {
        if (host.statusBarLabel == null) {
            return;
        }
        host.statusBarLabel.setText(Component.literal(
                "focus: " + host.selectionLabel()
                        + "  pending: " + host.viewModel.pendingCount()
                        + "  rev: " + host.viewModel.revision()
                        + "  " + (host.localStatus.get().isBlank() ? host.viewModel.status() : host.localStatus.get())
                        + (host.viewModel.diagnostics().isBlank() ? "" : "  " + host.viewModel.diagnostics())));
    }

}
