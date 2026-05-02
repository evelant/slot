package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.AtlasItemDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.HotbarSlotDrag;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;

/**
 * Compact chest chip stack — replaces the link-era storage strip.
 *
 * <p>Renders as an overlay docked above the Triage panel on the left
 * edge, mirroring Triage's column. Shows one chip per *proximate* claimed
 * chest; non-proximate chests are hidden (the player isn't near them, so
 * they aren't actionable here). See docs/plans/learned-storage.md.
 */
final class StoragePanelBuilder {
    static final int CHIP_HEIGHT = 18;
    static final int PANEL_GAP = 3;
    static final int PANEL_PADDING = 6;
    static final int HEADER_HEIGHT = 12;
    static final int MAX_CHIPS = 7;

    private final SlotWorkspaceUiController host;

    StoragePanelBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    private int countVisible() {
        int count = 0;
        for (SlotWorkspaceViewModel.ChestChip chip : host.viewModel.chestChips()) {
            if (isChipVisible(chip)) {
                count++;
            }
        }
        return count;
    }

    /**
     * A chest chip is visible when it is in proximity OR when an active
     * search query has at least one match that lives in this chest. The
     * search-driven case lets the player see remote chests holding the
     * search target without walking to them.
     */
    private boolean isChipVisible(SlotWorkspaceViewModel.ChestChip chip) {
        if (chip.proximate()) {
            return true;
        }
        String query = host.searchController.normalizedQuery();
        if (query.isBlank()) {
            return false;
        }
        return chestHasSearchMatch(chip.storageId());
    }

    private boolean chestHasSearchMatch(String storageId) {
        for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
            if (!host.searchController.matchesItem(item)) {
                continue;
            }
            for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
                if (storageId.equals(entry.storageId())) {
                    return true;
                }
            }
            for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.elsewhere()) {
                if (storageId.equals(entry.storageId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private SlotWorkspaceViewModel.ChestClusterDescriptor cluster(String clusterId) {
        if (clusterId == null || clusterId.isEmpty()) {
            return null;
        }
        for (SlotWorkspaceViewModel.ChestClusterDescriptor cluster : host.viewModel.chestClusters()) {
            if (clusterId.equals(cluster.clusterId())) {
                return cluster;
            }
        }
        return null;
    }

    /**
     * Returns null when there are no proximate chests so the panel
     * disappears entirely. Otherwise returns a flex item to be added
     * to {@link LeftColumnBuilder}'s flex column — content-fit height
     * (the column's flex(1) children are loot + Triage, not this).
     */
    UIElement overlay() {
        int visibleCount = countVisible();
        if (visibleCount == 0) {
            host.storagePanelElement = null;
            return null;
        }

        UIElement overlay = panel(GLASS).layout(layout -> layout
                .widthPercent(100)
                .paddingAll(PANEL_PADDING)
                .gapAll(PANEL_GAP)
                .flexDirection(FlexDirection.COLUMN));
        overlay.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        Label header = label("Nearby chests", ACCENT);
        header.layout(layout -> layout.widthPercent(100).height(HEADER_HEIGHT));
        header.textStyle(style -> style
                .textColor(ACCENT)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        header.setAllowHitTest(false);
        overlay.addChild(header);

        // Tally visible chips per cluster up to MAX_CHIPS so we can skip
        // a header for single-chip clusters (visual noise otherwise).
        java.util.Map<String, Integer> chipsPerCluster = new java.util.LinkedHashMap<>();
        int previewed = 0;
        for (SlotWorkspaceViewModel.ChestChip chip : host.viewModel.chestChips()) {
            if (!isChipVisible(chip)) {
                continue;
            }
            chipsPerCluster.merge(chip.clusterId(), 1, Integer::sum);
            if (++previewed >= MAX_CHIPS) {
                break;
            }
        }

        int rendered = 0;
        String currentClusterId = null;
        for (SlotWorkspaceViewModel.ChestChip chip : host.viewModel.chestChips()) {
            if (!isChipVisible(chip)) {
                continue;
            }
            if (!chip.clusterId().equals(currentClusterId)) {
                currentClusterId = chip.clusterId();
                if (chipsPerCluster.getOrDefault(currentClusterId, 0) > 1) {
                    SlotWorkspaceViewModel.ChestClusterDescriptor descriptor = cluster(currentClusterId);
                    overlay.addChild(clusterHeader(descriptor));
                }
            }
            overlay.addChild(chestChip(chip));
            if (++rendered >= MAX_CHIPS) {
                break;
            }
        }

        host.storagePanelElement = overlay;
        return overlay;
    }

    /**
     * Legacy entry point — body() returns an empty placeholder so
     * existing callers in AtlasPanelBuilder still compile. The real
     * panel is the overlay; mount {@link #overlay()} on the atlas panel.
     */
    UIElement body() {
        return new UIElement().layout(layout -> layout.width(0).height(0));
    }

    void repopulate() {
        // Overlay is rebuilt each frame from {@link #overlay()}; nothing
        // to do here. Kept for API parity with the old strip.
    }

    private UIElement clusterHeader(SlotWorkspaceViewModel.ChestClusterDescriptor descriptor) {
        String clusterId = descriptor == null ? "" : descriptor.clusterId();
        boolean editing = !clusterId.isBlank() && clusterId.equals(host.editingClusterId);
        if (editing) {
            return clusterRenameField(descriptor);
        }
        String text = descriptor == null || descriptor.label().isBlank()
                ? "Storage Area"
                : descriptor.label();
        com.lowdragmc.lowdraglib2.gui.ui.elements.Button header =
                button("", true, 0).noText();
        header.layout(layout -> layout.widthPercent(100).height(HEADER_HEIGHT));
        Label headerLabel = label(text, MUTED);
        headerLabel.layout(layout -> layout.widthPercent(100).heightPercent(100));
        headerLabel.textStyle(style -> style
                .textColor(MUTED)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        headerLabel.setAllowHitTest(false);
        header.addChild(headerLabel);
        // Right-click cluster header → enter rename mode. Mirrors the
        // chip's right-click-forget pattern and the kit context menu's
        // rename flow.
        header.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button != 1) {
                return;
            }
            event.stopPropagation();
            host.editingClusterId = clusterId;
            host.clusterLabelDraft = descriptor.label();
            host.rebuild();
        }, true);
        host.installTextTooltip(header, net.minecraft.network.chat.Component.literal(
                "Right-click to rename"));
        return header;
    }

    private UIElement clusterRenameField(SlotWorkspaceViewModel.ChestClusterDescriptor descriptor) {
        String clusterId = descriptor.clusterId();
        com.lowdragmc.lowdraglib2.gui.ui.elements.TextField input =
                new com.lowdragmc.lowdraglib2.gui.ui.elements.TextField();
        input.setAnyString();
        input.setText(host.clusterLabelDraft == null ? "" : host.clusterLabelDraft, false);
        input.layout(layout -> layout.widthPercent(100).height(HEADER_HEIGHT));
        input.style(style -> style.backgroundTexture(rect(0xC60D1318)));
        input.textFieldStyle(style -> style
                .font(FONT_UI)
                .placeholder(net.minecraft.network.chat.Component.literal(descriptor.label()))
                .textColor(TEXT)
                .cursorColor(ACCENT)
                .textShadow(false)
                .fontSize(7));
        input.setTextResponder(value -> host.clusterLabelDraft = value == null ? "" : value);
        Runnable commit = () -> {
            String trimmed = host.clusterLabelDraft == null ? "" : host.clusterLabelDraft.trim();
            host.editingClusterId = null;
            host.clusterLabelDraft = "";
            // Empty trimmed input → reset to default (server interprets blank as remove).
            host.rpc.sendRenameCluster(clusterId, trimmed);
            host.rebuild();
        };
        Runnable cancel = () -> {
            host.editingClusterId = null;
            host.clusterLabelDraft = "";
            host.rebuild();
        };
        input.addEventListener(UIEvents.KEY_DOWN, event -> {
            if (event.keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
                    || event.keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
                event.stopPropagation();
                commit.run();
            } else if (event.keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                event.stopPropagation();
                cancel.run();
            }
        });
        return input;
    }

    private UIElement chestChip(SlotWorkspaceViewModel.ChestChip chip) {
        int fill = (PANEL_ALT & 0x00FFFFFF) | 0xC0000000;

        UIElement element = panel(fill).layout(layout -> layout
                .widthPercent(100)
                .height(CHIP_HEIGHT)
                .paddingHorizontal(6)
                .gapAll(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));

        Label labelEl = label(chip.label(), TEXT);
        labelEl.layout(layout -> layout.flex(1).height(CHIP_HEIGHT));
        labelEl.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        labelEl.setAllowHitTest(false);
        element.addChild(labelEl);

        if (chip.slotCapacity() > 0) {
            Label countEl = label(chip.filledSlots() + "/" + chip.slotCapacity(), MUTED);
            countEl.layout(layout -> layout.width(36).height(CHIP_HEIGHT));
            countEl.textStyle(style -> style
                    .textColor(MUTED)
                    .textShadow(false)
                    .fontSize(7)
                    .textAlignHorizontal(Horizontal.RIGHT)
                    .textAlignVertical(Vertical.CENTER));
            countEl.setAllowHitTest(false);
            element.addChild(countEl);
        }

        Label dot = label("●", ACCENT);
        dot.layout(layout -> layout.width(8).height(CHIP_HEIGHT));
        dot.textStyle(style -> style
                .textColor(ACCENT)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        dot.setAllowHitTest(false);
        element.addChild(dot);

        installChipDropTarget(element, chip);
        installChipHover(element, chip);
        installChipCursorDropTarget(element, chip);
        installChipContextMenu(element, chip);
        host.installTextTooltip(element, net.minecraft.network.chat.Component.literal("Right-click for options"));
        return element;
    }

    /**
     * While the split cursor is non-empty, intercept clicks on the chip
     * and route them to a count-aware deposit. Capture-phase + early
     * stopPropagation ensures this fires before the chip's context-menu
     * handler (right-click) and before the bubble-phase root cancel
     * handler. Pickup intentionally not wired on chips: chips are
     * identity / chest handles, not slot handles, so there's no concrete
     * source slot to halve.
     */
    private void installChipCursorDropTarget(UIElement chip, SlotWorkspaceViewModel.ChestChip target) {
        String storageId = target.storageId();
        chip.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (!host.cursor.isCarrying()) {
                return;
            }
            WorkspaceCursorGestures.Result mode = WorkspaceCursorGestures.classify(event, true);
            int count = switch (mode) {
                case DROP_ALL -> host.cursor.dropCount(WorkspaceCursorCarry.DropMode.ALL);
                case DROP_ONE -> host.cursor.dropCount(WorkspaceCursorCarry.DropMode.ONE);
                case DROP_HALF -> host.cursor.dropCount(WorkspaceCursorCarry.DropMode.HALF);
                default -> 0;
            };
            if (count <= 0) {
                return;
            }
            event.stopPropagation();
            WorkspaceCursorCarry.State state = host.cursor.current();
            host.rpc.sendCursorDropToChest(state, storageId, count);
            host.cursor.consume(count);
        }, true);
    }

    /**
     * Right-click on a chest chip → open the chest context menu (Rename
     * + Forget). Replaces the prior "right-click instantly forgets"
     * gesture, which was a one-click destructive action with no
     * confirmation, no rename surface, and no tooltip. The context menu
     * pattern matches the kit chip's right-click flow.
     */
    private void installChipContextMenu(UIElement chip, SlotWorkspaceViewModel.ChestChip target) {
        String storageId = target.storageId();
        chip.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button != 1) {
                return;
            }
            event.stopPropagation();
            host.menu.openContextMenuForChest(storageId, event.x, event.y);
        }, true);
    }

    /**
     * Wire the chip's title bar into the cross-surface highlight pulse.
     * Hovering this chip sets {@code hoveredStorageId} (so atlas islands
     * and cards can light themselves); a per-frame TICK paint flip lights
     * the chip back when the player is hovering an atlas item or island
     * whose contents overlap this chest.
     */
    private void installChipHover(UIElement element, SlotWorkspaceViewModel.ChestChip chip) {
        String storageId = chip.storageId();
        element.addEventListener(UIEvents.MOUSE_ENTER, event -> host.hoveredStorageId = storageId, true);
        element.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (storageId.equals(host.hoveredStorageId)) {
                host.hoveredStorageId = null;
            }
        }, true);

        boolean[] lastLit = {false};
        element.addEventListener(UIEvents.TICK, event -> {
            // Only the specific hovered card lights chests — the chest
            // highlight is for "which chest holds this item", not "which
            // chests hold anything from this island". Island-level
            // highlighting drowned the per-item signal.
            boolean lit = isHoveredItemPresentInChest(storageId);
            if (lit == lastLit[0]) {
                return;
            }
            lastLit[0] = lit;
            element.style(style -> style.overlayTexture(lit ? rect(HOVER_ACCENT_OVERLAY) : IGuiTexture.EMPTY));
        });
    }

    /**
     * True iff the currently-hovered atlas identity has presence (in a
     * proximate chest) OR elsewhere (in a non-proximate chest visible
     * via search) for {@code storageId}. Without the elsewhere branch,
     * hovering a card whose only stock is in a non-proximate chest
     * wouldn't light the matching chip in the search-driven chip view.
     */
    private boolean isHoveredItemPresentInChest(String storageId) {
        SlotWorkspaceViewModel.IdentityRef hovered = host.hoveredAtlasIdentity;
        if (hovered == null) {
            return false;
        }
        SlotWorkspaceViewModel.AtlasItem item = host.viewModel.atlasItem(hovered);
        if (item == null) {
            return false;
        }
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
            if (storageId.equals(entry.storageId())) {
                return true;
            }
        }
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.elsewhere()) {
            if (storageId.equals(entry.storageId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Drop an atlas-item or hotbar-slot drag onto a chip → deposit the
     * stack into that specific chest. Bypasses affinity routing so the
     * player can override where an item goes.
     *
     * <p>{@code DRAG_PERFORM} is registered in the capture phase so the
     * chip wins ordering against the atlas's catch-all drop handler — the
     * leftColumn flex refactor moved chips off direct hit-test targets
     * for atlas drops, and bubbling-phase handlers were running after the
     * atlas handler had already early-returned.
     */
    private void installChipDropTarget(UIElement chip, SlotWorkspaceViewModel.ChestChip target) {
        String storageId = target.storageId();
        chip.addEventListener(UIEvents.DRAG_ENTER, event -> {
            boolean acceptable = host.drag.atlasItemDrag(event) != null
                    || host.drag.hotbarSlotDrag(event) != null;
            host.drag.updateGenericDropOverlay(chip, acceptable, ACCENT);
        }, true);
        chip.addEventListener(UIEvents.DRAG_UPDATE, event -> {
            boolean acceptable = host.drag.atlasItemDrag(event) != null
                    || host.drag.hotbarSlotDrag(event) != null;
            host.drag.updateGenericDropOverlay(chip, acceptable, ACCENT);
        }, true);
        chip.addEventListener(UIEvents.DRAG_LEAVE, event -> host.drag.clearDropOverlay(chip), true);
        chip.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            host.drag.clearDropOverlay(chip);
            AtlasItemDrag atlasDrag = host.drag.atlasItemDrag(event);
            HotbarSlotDrag hotbarDrag = host.drag.hotbarSlotDrag(event);
            if (atlasDrag == null && hotbarDrag == null) {
                return;
            }
            if (atlasDrag != null) {
                host.rpc.sendDepositCarriedToChest(atlasDrag.identity(), storageId);
            } else {
                host.rpc.sendDepositHotbarToChest(hotbarDrag.hotbarIndex(), storageId);
            }
            event.stopPropagation();
        }, true);
    }
}
