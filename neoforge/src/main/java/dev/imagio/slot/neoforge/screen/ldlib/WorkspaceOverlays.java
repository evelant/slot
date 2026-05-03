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
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

final class WorkspaceOverlays {
    private static final int CARRIED_CHIP_WIDTH = 96;
    private static final int CARRIED_CHIP_HEIGHT = 20;
    private static final int CARRIED_CHIP_BAR_HEIGHT = 3;

    private final SlotWorkspaceUiController host;

    WorkspaceOverlays(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement topRightActionsOverlay() {
        // Floating action cluster pinned to the atlas panel's top-right
        // corner. Replaces the former persistent header strip. Vanilla is
        // always visible (primary escape hatch, also bound to a keymap).
        // Deposit only reveals itself when a claimed chest is proximate —
        // the same TICK-poll pattern the old header used, kept here so
        // the chip stays in sync as the host.player moves.
        UIElement overlay = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .top(10)
                .right(10)
                .gapAll(6)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        overlay.style(style -> style.zIndex(11));

        int initialDepositCount = countDepositable();
        boolean initialDepositEnabled = host.anyChestProximate() && initialDepositCount > 0;
        Button depositButton = button(depositLabel(initialDepositCount), true, ACCENT);
        depositButton.layout(layout -> layout.width(72).height(16));
        depositButton.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        depositButton.setVisible(initialDepositEnabled);
        boolean[] lastDepositVisible = {initialDepositEnabled};
        int[] lastDepositCount = {initialDepositCount};
        depositButton.addEventListener(UIEvents.TICK, event -> {
            int count = countDepositable();
            boolean enabled = host.anyChestProximate() && count > 0;
            if (enabled != lastDepositVisible[0]) {
                lastDepositVisible[0] = enabled;
                depositButton.setVisible(enabled);
            }
            if (count != lastDepositCount[0]) {
                lastDepositCount[0] = count;
                depositButton.setText(Component.literal(depositLabel(count)));
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
        // Deposit is *affinity-driven* — only stacks the player has
        // already deposited into a proximate chest at least once go
        // automatically. Items with no learned bond stay in carry. The
        // tooltip surfaces this so the player understands "nothing to
        // deposit" instead of assuming the button is broken.
        host.installTextTooltip(
                depositButton,
                Component.literal(
                        "Deposit carried items into proximate chests by learned affinity. "
                                + "Items without an existing bond stay in carry — drop one in manually first to teach the chest."));

        // Top-level Gather button. Mirrors the per-kit "gather N" inside
        // the kit rack but pulls for whichever kit is currently active.
        // Only shows when at least one kit is active AND a chest is
        // proximate — without those preconditions the action is a no-op.
        boolean initialGatherEnabled = host.anyChestProximate() && host.viewModel.activeKit() != null;
        Button gatherButton = button("Gather", true, ACTIVE_HOTBAR);
        gatherButton.layout(layout -> layout.width(54).height(16));
        gatherButton.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        gatherButton.setVisible(initialGatherEnabled);
        boolean[] lastGatherVisible = {initialGatherEnabled};
        gatherButton.addEventListener(UIEvents.TICK, event -> {
            boolean enabled = host.anyChestProximate() && host.viewModel.activeKit() != null;
            if (enabled == lastGatherVisible[0]) {
                return;
            }
            lastGatherVisible[0] = enabled;
            gatherButton.setVisible(enabled);
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
            if (host.viewModel.activeKit() == null) {
                host.localStatus.set("activate a kit first");
                host.rebuild();
                return;
            }
            dev.imagio.slot.SlotCommon.LOGGER.info(
                    "[SLOT] gather button clicked: activeKit={}",
                    host.viewModel.activeKit().kitId());
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new dev.imagio.slot.neoforge.network.SlotGatherActiveKitPayload());
            host.localStatus.set("gathering active kit from nearby chests");
            host.rebuild();
        });
        host.installKeybindTooltip(
                gatherButton,
                "Pull every item the active kit needs from nearby chests",
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

        Button vanillaButton = button("Vanilla", true, GLASS);
        vanillaButton.layout(layout -> layout.width(48).height(16));
        vanillaButton.textStyle(style -> style
                .textColor(MUTED)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        vanillaButton.setOnClick(event -> {
            event.stopPropagation();
            dev.imagio.slot.neoforge.client.screen.SlotWorkspaceMountController.openVanillaInventory();
        });

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
     * server projects from the affinity map. Drives the deposit
     * button's "Deposit (N)" label and visibility.
     */
    private int countDepositable() {
        return host.viewModel.depositableIdentities().size();
    }

    private String depositLabel(int count) {
        return count > 0 ? "Deposit (" + count + ")" : "Deposit";
    }

    UIElement searchChipOverlay() {
        // Anchor at the same top-left location as searchHintOverlay so opening
        // search doesn't feel like the UI jumped — the hint slides seamlessly
        // into the live modal.
        UIElement chip = panel(GLASS).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .top(10)
                .left(10)
                .width(280)
                .paddingAll(6)
                .gapAll(3)
                .flexDirection(FlexDirection.COLUMN));
        chip.style(style -> style.zIndex(12));
        chip.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        String bufferDisplay = "/" + host.searchController.buffer() + "_";
        chip.addChild(label(bufferDisplay, ACCENT).layout(layout -> layout.height(12)));

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
        // Content-fit the summary line: the previous flex(1) caused the
        // chip to claim extra vertical space below the buffer line, so
        // the search modal grew a strip of empty space when active.
        // Wrapping is still allowed via wrappedLabel's textWrap, so a
        // long summary line still spills onto a second row when needed.
        Label summaryLabel = wrappedLabel(summary, MUTED);
        summaryLabel.layout(layout -> layout.widthPercent(100));
        chip.addChild(summaryLabel);
        return chip;
    }

    UIElement searchHintOverlay() {
        UIElement hint = panel(GLASS).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .top(10)
                .left(10)
                .paddingHorizontal(8)
                .paddingVertical(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        hint.style(style -> style.zIndex(11));
        hint.setAllowHitTest(false);
        hint.addChild(label("Press / to search", MUTED).layout(layout -> layout.height(10)));
        return hint;
    }

    UIElement carriedFreeSlotsChip() {
        int initialFree = host.viewModel == null ? 0 : host.viewModel.carriedFreeSlotCount();
        int initialCapacity = host.viewModel == null ? 0 : host.viewModel.carriedSlotCapacity();
        UIElement chip = panel(GLASS).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .top(10)
                .width(CARRIED_CHIP_WIDTH)
                .height(CARRIED_CHIP_HEIGHT)
                .paddingHorizontal(8)
                .paddingTop(3)
                .paddingBottom(CARRIED_CHIP_BAR_HEIGHT + 3)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        chip.style(style -> style.zIndex(11));
        chip.setAllowHitTest(false);
        Label valueLabel = label(formatFreeSlots(initialFree), TEXT);
        valueLabel.layout(layout -> layout.widthPercent(100).flex(1));
        valueLabel.textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER));
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
        float[] lastLeft = {-1f};
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
            float panelWidth = host.wallPanelElement == null ? 0f : host.wallPanelElement.getContentWidth();
            if (panelWidth <= 0f) {
                return;
            }
            float desiredLeft = Math.max(0f, (panelWidth - CARRIED_CHIP_WIDTH) / 2f);
            if (Math.abs(desiredLeft - lastLeft[0]) > 0.5f) {
                lastLeft[0] = desiredLeft;
                chip.layout(layout -> layout.left(desiredLeft));
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

    UIElement statusBar() {
        if (host.statusBarElement == null) {
            host.statusBarLabel = label("", MUTED);
            host.statusBarLabel.layout(layout -> layout.widthPercent(100).height(12));
            host.statusBarElement = panel(PANEL_ALT).layout(layout -> layout
                    .widthPercent(100)
                    .height(25)
                    .paddingAll(7));
            host.statusBarElement.addChild(host.statusBarLabel);
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
                "selected: " + host.selectionLabel()
                        + "  pending: " + host.viewModel.pendingCount()
                        + "  rev: " + host.viewModel.revision()
                        + "  " + (host.localStatus.get().isBlank() ? host.viewModel.status() : host.localStatus.get())
                        + (host.viewModel.diagnostics().isBlank() ? "" : "  " + host.viewModel.diagnostics())));
    }

    /**
     * Floating cursor-carry ghost — a virtual item icon + count that
     * follows the mouse while {@link WorkspaceCursorCarry} is non-empty.
     * The cursor itself is purely client-side state (no server
     * representation), so this overlay is the only on-screen evidence
     * the player is mid-pickup. Hit-testing is disabled so clicks pass
     * through to whatever drop target sits beneath the cursor.
     *
     * <p>Lives at root so it draws above every panel; positioning is a
     * MOUSE_MOVE-driven absolute offset on the root tracked-mouse
     * coordinate. Re-renders the inner card from the cursor state on
     * every TICK so count updates immediately after a drop or a
     * cumulative half-pickup.
     */
    UIElement cursorOverlay() {
        UIElement overlay = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .top(0)
                .left(0)
                .width(CURSOR_OVERLAY_SIZE)
                .height(CURSOR_OVERLAY_SIZE));
        overlay.style(style -> style.zIndex(99));
        overlay.setAllowHitTest(false);
        // Last-rendered identity + count so we only rebuild the inner card
        // when something actually changed — TICK fires every frame and
        // teardown/rebuild of the icon child every frame is wasted work.
        final String[] lastIdentityKey = {""};
        final int[] lastCount = {-1};
        host.root.addEventListener(UIEvents.MOUSE_MOVE, event -> {
            if (!host.cursor.isCarrying()) {
                return;
            }
            // Offset the ghost so the cursor tip lands roughly at the
            // ghost's top-left corner (vanilla cursor-carry visual).
            float left = event.x - CURSOR_OVERLAY_SIZE / 2f;
            float top = event.y - CURSOR_OVERLAY_SIZE / 2f;
            overlay.layout(layout -> layout.left(left).top(top));
        }, true);
        overlay.addEventListener(UIEvents.TICK, event -> {
            WorkspaceCursorCarry.State state = host.cursor.current();
            if (state == null) {
                if (!lastIdentityKey[0].isEmpty()) {
                    overlay.clearAllChildren();
                    lastIdentityKey[0] = "";
                    lastCount[0] = -1;
                }
                return;
            }
            String key = state.identity().itemId() + "|" + state.identity().componentFingerprint();
            if (key.equals(lastIdentityKey[0]) && state.count() == lastCount[0]) {
                return;
            }
            overlay.clearAllChildren();
            ItemStack stack = state.displayStack();
            if (stack == null) {
                stack = ItemStack.EMPTY;
            }
            UIElement card = itemSlotCard(stack, CURSOR_OVERLAY_SIZE, 0x00000000, true, state.count());
            overlay.addChild(card);
            lastIdentityKey[0] = key;
            lastCount[0] = state.count();
        });
        return overlay;
    }

    private static final float CURSOR_OVERLAY_SIZE = 16f;
}
