package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEmitter;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.BindableValue;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.GraphView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import dev.imagio.slot.atlas.FitCarriedCamera;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

final class SlotWorkspaceUiFactory {
    private static final int BACKGROUND = 0x96060A0E;
    private static final int PANEL = 0xC8162029;
    private static final int PANEL_ALT = 0xD01E2933;
    private static final int GLASS = 0xCC0C141A;
    private static final int ROW = 0xEC24313D;
    private static final int ROW_DIM = 0x7C24313D;
    private static final int ROW_HOVER = 0xEC334354;
    private static final int ROW_MATCH = 0xED345749;
    private static final int SELECTED = 0xF0507E6B;
    private static final int ACTIVE_HOTBAR = 0xF0665B33;
    private static final int TEXT = 0xFFE8EEF2;
    private static final int MUTED = 0xFFA0AAB3;
    private static final int ACCENT = 0xFF7AC7A7;
    private static final int WARNING = 0xFFFFC66D;
    private static final int COLLECTION = 0xFFBE8CFF;
    private static final int ISLAND_BORDER = 0xA04F6578;
    private static final int STORAGE_ZONE_FILL = 0x501A2430;
    private static final int STORAGE_ZONE_HEADER_FILL = 0xC02E3A48;
    private static final int STORAGE_ZONE_HEADER_HEIGHT = 16;
    private static final int STORAGE_TILE_FILL = 0xD02E3A48;
    private static final int STORAGE_TILE_FILL_DIM = 0x602E3A48;
    private static final int STORAGE_TILE_CELL_FILL = 0x801A2430;
    private static final int STORAGE_TILE_CELL_FILL_DIM = 0x401A2430;
    private static final int LINK_THREAD_COLOR = 0xC07AC7A7;
    private static final int LINK_HIGHLIGHT_COLOR = 0xA07AC7A7;
    private static final int LINK_HIGHLIGHT_THICKNESS = 2;
    private static final int HOVER_TRAIL_COLOR = 0xD0FFC66D;
    private static final int HOVER_TRAIL_THICKNESS = 2;
    private static final int HOVER_ACCENT_OVERLAY = 0x60FFC66D;
    private static final int BROWSE_CELL_PX = 32;
    private static final int READ_CELL_PX = 48;
    private static final int INSPECT_CELL_PX = 72;
    private static final int DETAIL_CELL_PX = 124;
    private static final float CARRIED_FIT_MIN_SCALE = 0.20f;
    private static final float CARRIED_FIT_MAX_SCALE = 2.50f;
    private static final float CARRIED_FIT_READABILITY_MIN_SCALE = 1.00f;
    private static final float CARRIED_FIT_PADDING_PX = 72f;
    private static final int BELT_HEIGHT = 24;
    private static final int BELT_SLOT_SIZE = 20;
    private static final int BELT_DIVIDER_HEIGHT = 16;
    private static final int TRIAGE_PANEL_WIDTH = 152;
    private static final float NAV_CAPSULE_INSET_PX = 96f;
    private static final float BELT_CAMERA_INSET_PX = 44f;
    private static final float SIDE_CAMERA_INSET_PX = 48f;
    private static final float GHOST_CARD_ALPHA = 0.18f;
    private static final int GHOST_ICON_OVERLAY_COLOR = 0xC8060A0E;
    private static final int DRAG_START_THRESHOLD_PX = 4;

    private SlotWorkspaceUiFactory() {
    }

    static ModularUI create(SlotWorkspaceUiSession session, Player player) {
        return new Controller(session, player).create();
    }

    private static final class Controller {
        private final SlotWorkspaceUiSession session;
        private final Player player;
        private final UIElement root;
        private final UIElement content;

        private SlotWorkspaceViewModel viewModel;
        private String localStatus = "";
        private String searchQuery = "";
        private SlotWorkspaceViewModel.IdentityRef selectedAtlasIdentity;
        private SlotWorkspaceViewModel.IdentityRef hoveredAtlasIdentity;
        private int selectedHotbarIndex = -1;
        private int hoveredHotbarIndex = -1;
        private final java.util.Map<Integer, UIElement> hotbarSlotElements = new java.util.HashMap<>();
        private SlotWorkspaceViewModel.IdentityRef contextMenuAtlasIdentity;
        private int contextMenuHotbarIndex = -1;
        private float contextMenuScreenX;
        private float contextMenuScreenY;
        private SlotWorkspaceViewModel.IdentityRef rehomePickerIdentity;
        private String editingIslandId = null;
        private String editingChestStorageId = null;
        private String islandLabelDraft = "";
        private SlotWorkspaceViewModel.IdentityRef pendingCreateIdentity;
        private int pendingCreateWorldX;
        private int pendingCreateWorldY;
        private String pendingCreateLabel = "";
        private int pendingCreateColor = ISLAND_PALETTE[0];
        private boolean pendingCreateFocusPending;

        private RPCEmitter transferEmitter;
        private RPCEmitter homeEmitter;
        private RPCEmitter createNamedIslandEmitter;
        private RPCEmitter hotbarToAtlasEmitter;
        private RPCEmitter moveIslandEmitter;
        private RPCEmitter moveChestEmitter;
        private RPCEmitter moveStorageZoneEmitter;
        private RPCEmitter relabelChestEmitter;
        private RPCEmitter linkChestEmitter;
        private RPCEmitter unlinkChestEmitter;
        private RPCEmitter depositEmitter;
        private RPCEmitter takeAllEmitter;
        private RPCEmitter renameIslandEmitter;
        private RPCEmitter recolorIslandEmitter;
        private RPCEmitter setIslandIconEmitter;
        private RPCEmitter deleteIslandEmitter;
        private RPCEmitter acceptChipEmitter;
        private RPCEmitter saveKitEmitter;
        private RPCEmitter activateKitEmitter;
        private RPCEmitter deactivateKitEmitter;
        private RPCEmitter deleteKitEmitter;
        private RPCEmitter returnHotbarToHomeEmitter;
        private RPCEmitter assignHomeToFreeHotbarEmitter;
        private RPCEmitter depositCarriedToChestEmitter;
        private RPCEmitter depositHotbarToChestEmitter;
        private RPCEmitter takeFromChestEmitter;
        private RPCEmitter assignHomeToHotbarOnlyEmitter;
        private RPCEmitter depositHomeToLinkedChestEmitter;
        private boolean kitRackOpen;
        private AtlasCamera atlasCamera;

        private Controller(SlotWorkspaceUiSession session, Player player) {
            this.session = session;
            this.player = player;
            this.viewModel = session.viewModel();
            this.root = panel(BACKGROUND).layout(layout -> layout
                    .widthPercent(100)
                    .heightPercent(100)
                    .paddingAll(14)
                    .gapAll(8)
                    .flexDirection(FlexDirection.COLUMN));
            this.content = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .heightPercent(100)
                    .gapAll(8)
                    .flexDirection(FlexDirection.COLUMN));
            clearSelectionOnDirectClick(root);
        }

        private ModularUI create() {
            registerRpcs();
            installBeltHotkeys();
            root.addChildren(syncBinding(), content);
            rebuild();
            return ModularUI.of(UI.of(root), player);
        }

        private void installBeltHotkeys() {
            root.setEnforceFocus(event -> {
            });
            root.addEventListener(UIEvents.MUI_CHANGED, event -> root.focus());
            root.addEventListener(UIEvents.KEY_DOWN, this::handleBeltHotkey, true);
            root.addEventListener(UIEvents.CHAR_TYPED, event -> {
                if (event.codePoint >= '1' && event.codePoint <= '9') {
                    event.stopPropagation();
                }
            }, true);
        }

        private void handleBeltHotkey(UIEvent event) {
            int digit = digitFromKeyCode(event.keyCode);
            if (digit < 1 || digit > 9) {
                return;
            }
            event.stopPropagation();
            SlotWorkspaceViewModel.AtlasItem target = hoveredAtlasItem();
            if (target == null) {
                target = selectedAtlasItem();
            }
            if (target == null) {
                localStatus = "hover or select an atlas item to assign with 1-9";
                rebuild();
                return;
            }
            sendTransfer(
                    SlotWorkspaceUiSession.TARGET_MAIN_SLOT,
                    target.firstSlotIndex(),
                    SlotWorkspaceUiSession.TARGET_HOTBAR_SLOT,
                    digit - 1
            );
        }

        private int digitFromKeyCode(int keyCode) {
            if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
                return keyCode - GLFW.GLFW_KEY_1 + 1;
            }
            if (keyCode >= GLFW.GLFW_KEY_KP_1 && keyCode <= GLFW.GLFW_KEY_KP_9) {
                return keyCode - GLFW.GLFW_KEY_KP_1 + 1;
            }
            return 0;
        }

        private void registerRpcs() {
            transferEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    Integer.class,
                    Integer.class,
                    Integer.class,
                    Integer.class,
                    String.class,
                    session::transfer
            ));
            homeEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    Integer.class,
                    Integer.class,
                    session::assignHome
            ));
            createNamedIslandEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    Integer.class,
                    Integer.class,
                    Integer.class,
                    session::createNamedIslandForItem
            ));
            hotbarToAtlasEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    Integer.class,
                    String.class,
                    Integer.class,
                    Integer.class,
                    session::moveHotbarToAtlas
            ));
            moveIslandEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    Integer.class,
                    Integer.class,
                    session::moveIsland
            ));
            moveChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    Integer.class,
                    Integer.class,
                    session::moveChest
            ));
            moveStorageZoneEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    Integer.class,
                    Integer.class,
                    session::moveStorageZone
            ));
            relabelChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    session::relabelChest
            ));
            linkChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    session::linkIslandToChest
            ));
            unlinkChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    session::unlinkIslandFromChest
            ));
            depositEmitter = root.addRPCEvent(RPCEventBuilder.simple((Runnable) session::deposit));
            takeAllEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    session::takeAllFromChest
            ));
            renameIslandEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    session::renameIsland
            ));
            recolorIslandEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    Integer.class,
                    session::recolorIsland
            ));
            setIslandIconEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    session::setIslandIcon
            ));
            deleteIslandEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    session::deleteIsland
            ));
            acceptChipEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    session::acceptChip
            ));
            saveKitEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    session::saveBeltAsKit
            ));
            activateKitEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    session::activateKit
            ));
            deactivateKitEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    (Runnable) session::deactivateKit
            ));
            deleteKitEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    session::deleteKit
            ));
            returnHotbarToHomeEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    Integer.class,
                    session::returnHotbarToHome
            ));
            assignHomeToFreeHotbarEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    session::assignHomeToFreeHotbar
            ));
            depositCarriedToChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    session::depositCarriedToChest
            ));
            depositHotbarToChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    Integer.class,
                    String.class,
                    session::depositHotbarToChest
            ));
            takeFromChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    Integer.class,
                    session::takeFromChest
            ));
            assignHomeToHotbarOnlyEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    session::assignHomeToHotbarOnly
            ));
            depositHomeToLinkedChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    session::depositHomeToLinkedChest
            ));
        }

        private UIElement syncBinding() {
            BindableValue<Tag> binding = new BindableValue<>();
            binding.bind(DataBindingBuilder.tagS2C(session::viewTag)
                    .remoteSetter(tag -> {
                        session.acceptRemoteView(tag);
                        viewModel = session.viewModel();
                        localStatus = "";
                        rebuild();
                    })
                    .build());
            binding.layout(layout -> layout.width(0).height(0));
            return binding;
        }

        private void rebuild() {
            if (selectedAtlasIdentity != null && viewModel.atlasItem(selectedAtlasIdentity) == null) {
                selectedAtlasIdentity = null;
            }
            if (hoveredAtlasIdentity != null && viewModel.atlasItem(hoveredAtlasIdentity) == null) {
                hoveredAtlasIdentity = null;
            }
            if (hoveredHotbarIndex >= 0
                    && (hoveredHotbarIndex >= viewModel.hotbarSlots().size()
                    || !viewModel.hotbarSlots().get(hoveredHotbarIndex).occupied())) {
                hoveredHotbarIndex = -1;
            }
            hotbarSlotElements.clear();
            content.clearAllChildren();
            content.addChildren(
                    header(),
                    body(),
                    statusBar()
            );
            content.markTaffyStyleDirty();
        }

        private UIElement header() {
            UIElement header = panel(PANEL_ALT).layout(layout -> layout
                    .widthPercent(100)
                    .height(34)
                    .paddingAll(8)
                    .gapAll(8)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            boolean depositEnabled = anyChestProximate();
            Button depositButton = button("Deposit", depositEnabled, depositEnabled ? ACCENT : PANEL_ALT);
            depositButton.layout(layout -> layout.width(72).height(18));
            depositButton.textStyle(style -> style
                    .textColor(depositEnabled ? TEXT : MUTED)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            depositButton.setOnClick(event -> {
                event.stopPropagation();
                if (!depositEnabled) {
                    localStatus = "no claimed chest nearby";
                    rebuild();
                    return;
                }
                sendDeposit();
            });
            header.addChildren(
                    label("SLOT Atlas", ACCENT).layout(layout -> layout.flex(1).height(12)),
                    depositButton
            );
            clearSelectionOnDirectClick(header);
            return header;
        }

        private UIElement body() {
            UIElement body = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .flex(1)
                    .gapAll(8)
                    .flexDirection(FlexDirection.ROW));
            body.addChildren(atlasPanel());
            return body;
        }

        private UIElement atlasPanel() {
            UIElement panel = panel(PANEL).layout(layout -> layout
                    .flex(1)
                    .heightPercent(100)
                    .paddingAll(0));
            clearSelectionOnDirectClick(panel);

            SlotAtlasGraphView atlas = new SlotAtlasGraphView();
            atlas.onCameraChanged(camera -> atlasCamera = camera);
            atlas.layout(layout -> layout.widthPercent(100).heightPercent(100));
            atlas.style(style -> style.backgroundTexture(rect(0xB810171D)).zIndex(0));
            atlas.graphViewStyle(style -> style
                    .minScale(0.05f)
                    .maxScale(4.50f)
                    .gridTexture(IGuiTexture.EMPTY)
                    .gridSize(48));
            atlas.addEventListener(UIEvents.LAYOUT_CHANGED, event -> {
                if (atlasCamera == null) {
                    applyInitialCamera(atlas);
                } else {
                    atlas.restoreCamera(atlasCamera);
                }
            });
            installAtlasCanvasDropTarget(panel, atlas);
            installAtlasBackgroundDropTarget(atlas);
            buildAtlas(atlas);
            panel.addChildren(atlas, navigationCapsule(atlas), triagePanelOverlay(), beltOverlay());
            panel.addChild(hoverTrailOverlay(atlas));
            if (kitRackOpen) {
                panel.addChild(kitRackOverlay());
            }
            UIElement contextMenu = contextMenuOverlay();
            if (contextMenu != null) {
                panel.addChild(contextMenu);
            }
            UIElement editPopover = islandEditPopover();
            if (editPopover != null) {
                panel.addChild(editPopover);
            }
            UIElement createPopover = createIslandPopover();
            if (createPopover != null) {
                panel.addChild(createPopover);
            }
            UIElement linkPopover = chestLinkPopover();
            if (linkPopover != null) {
                panel.addChild(linkPopover);
            }
            return panel;
        }

        private UIElement beltOverlay() {
            UIElement overlay = beltPanel();
            overlay.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(0)
                    .right(0)
                    .bottom(4)
                    .height(BELT_HEIGHT));
            overlay.style(style -> style.zIndex(6));
            return overlay;
        }

        private UIElement triagePanelOverlay() {
            UIElement overlay = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(8)
                    .top(94)
                    .bottom(BELT_HEIGHT + 12)
                    .width(TRIAGE_PANEL_WIDTH)
                    .paddingAll(6)
                    .gapAll(4)
                    .flexDirection(FlexDirection.COLUMN));
            overlay.style(style -> style.zIndex(7));
            overlay.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

            int triageCount = viewModel.triageItems().size();
            UIElement headerRow = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(14)
                    .gapAll(4)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            headerRow.addChildren(
                    label("Triage", ACCENT).layout(layout -> layout.flex(1).height(12)),
                    label(String.valueOf(triageCount), MUTED).layout(layout -> layout.width(24).height(12))
            );
            headerRow.setAllowHitTest(false);
            overlay.addChild(headerRow);

            UIElement divider = panel(ISLAND_BORDER).layout(layout -> layout.widthPercent(100).height(1));
            divider.setAllowHitTest(false);
            overlay.addChild(divider);

            if (triageCount == 0) {
                Label empty = label("Inbox is empty.", MUTED);
                empty.layout(layout -> layout.widthPercent(100).height(24));
                empty.textStyle(style -> style
                        .textColor(MUTED)
                        .fontSize(7)
                        .textShadow(false)
                        .textAlignHorizontal(Horizontal.LEFT)
                        .textAlignVertical(Vertical.CENTER));
                empty.setAllowHitTest(false);
                overlay.addChild(empty);
            } else {
                for (SlotWorkspaceViewModel.AtlasItem item : viewModel.triageItems()) {
                    overlay.addChild(triagePanelRow(item));
                    for (ChipSuggestion chip : item.chipSuggestions()) {
                        overlay.addChild(triagePanelChip(item, chip));
                    }
                }
            }

            installTriagePanelDropTarget(overlay);
            return overlay;
        }

        private UIElement triagePanelRow(SlotWorkspaceViewModel.AtlasItem item) {
            boolean selected = item.identity().equals(selectedAtlasIdentity);
            int chrome = selected ? SELECTED : (item.recent() ? ROW_MATCH : ROW);
            Button row = button("", true, chrome);
            row.noText();
            boolean[] lastAccent = {false};
            row.addEventListener(UIEvents.TICK, event -> {
                boolean accent = shouldAccentTriageRow(item);
                if (accent != lastAccent[0]) {
                    row.style(style -> style.overlayTexture(accent ? rect(HOVER_ACCENT_OVERLAY) : IGuiTexture.EMPTY));
                    lastAccent[0] = accent;
                }
            });
            row.layout(layout -> layout
                    .widthPercent(100)
                    .height(20)
                    .paddingHorizontal(4)
                    .gapAll(4)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            row.setOnClick(event -> {
                event.stopPropagation();
                if (event.button == 0 && Screen.hasShiftDown()) {
                    int hotbarIndex = hotbarSlotForIdentity(item.identity());
                    if (hotbarIndex >= 0) {
                        sendReturnHotbarToHome(hotbarIndex);
                    } else {
                        localStatus = item.name() + " is not in the hotbar";
                        rebuild();
                    }
                    return;
                }
                selectedAtlasIdentity = item.identity();
                selectedHotbarIndex = -1;
                localStatus = "selected inbox item: drag to an island, click an island, or accept a chip";
                rebuild();
            });
            row.addEventListener(UIEvents.MOUSE_ENTER, event -> hoveredAtlasIdentity = item.identity(), true);
            row.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                if (item.identity().equals(hoveredAtlasIdentity)) {
                    hoveredAtlasIdentity = null;
                }
            }, true);
            installAtlasItemDragSource(row, item);
            installAtlasHoverTooltip(row, item);

            UIElement icon = itemIcon(item.displayStack(), 16, item.carried());
            icon.layout(layout -> layout.width(16).height(16));
            row.addChild(icon);

            Label name = label(shorten(item.name(), 14), TEXT);
            name.layout(layout -> layout.flex(1).height(12));
            name.textStyle(style -> style
                    .textColor(item.carried() ? TEXT : MUTED)
                    .fontSize(7)
                    .textShadow(false)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            name.setAllowHitTest(false);
            row.addChild(name);

            if (item.totalCount() > 0) {
                Label count = label("x" + item.totalCount(), MUTED);
                count.layout(layout -> layout.width(28).height(12));
                count.textStyle(style -> style
                        .textColor(MUTED)
                        .fontSize(7)
                        .textShadow(false)
                        .textAlignHorizontal(Horizontal.RIGHT)
                        .textAlignVertical(Vertical.CENTER));
                count.setAllowHitTest(false);
                row.addChild(count);
            }
            return row;
        }

        private UIElement triagePanelChip(SlotWorkspaceViewModel.AtlasItem item, ChipSuggestion chip) {
            Button chipButton = button("", true, chip.color());
            chipButton.noText();
            chipButton.layout(layout -> layout
                    .widthPercent(100)
                    .height(11)
                    .paddingHorizontal(4)
                    .gapAll(2)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            chipButton.setOnClick(event -> {
                event.stopPropagation();
                sendChipAccept(item, chip);
            });
            Label chipLabel = label(chipLabelText(chip), TEXT);
            chipLabel.layout(layout -> layout.flex(1).height(9));
            chipLabel.textStyle(style -> style
                    .textColor(TEXT)
                    .fontSize(6)
                    .textShadow(false)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            chipLabel.setAllowHitTest(false);
            chipButton.addChild(chipLabel);
            return chipButton;
        }

        private void installTriagePanelDropTarget(UIElement target) {
            target.addEventListener(UIEvents.DRAG_ENTER, event -> updateGenericDropOverlay(target, isTriagePanelDropAcceptable(event), WARNING), true);
            target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateGenericDropOverlay(target, isTriagePanelDropAcceptable(event), WARNING));
            target.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(target), true);
            target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                clearDropOverlay(target);
                AtlasItemDrag atlasItem = atlasItemDrag(event);
                if (atlasItem != null) {
                    sendAssignHome(
                            atlasItem.identity(),
                            SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                            0,
                            0
                    );
                    event.stopPropagation();
                    return;
                }
                HotbarSlotDrag hotbarItem = hotbarSlotDrag(event);
                if (hotbarItem != null) {
                    sendMoveHotbarToAtlas(
                            hotbarItem.hotbarIndex(),
                            SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                            0,
                            0
                    );
                    event.stopPropagation();
                }
            });
        }

        private boolean isTriagePanelDropAcceptable(UIEvent event) {
            return atlasItemDrag(event) != null || hotbarSlotDrag(event) != null;
        }

        private void buildAtlas(SlotAtlasGraphView atlas) {
            StorageZoneBounds bounds = storageZoneBounds();
            if (bounds != null) {
                atlas.addContentChild(storageZoneBackdrop(bounds));
                atlas.addContentChild(storageZoneHeader(bounds, atlas));
            }
            Set<String> highlightedIslandIds = highlightedIslandIdsFromProximateTiles();
            for (SlotWorkspaceViewModel.AtlasIsland island : viewModel.islands()) {
                atlas.addContentChild(islandPanel(atlas, island));
            }
            for (String islandId : highlightedIslandIds) {
                SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(islandId);
                if (island != null) {
                    addIslandHighlightFrame(atlas, island);
                }
            }
            for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
                atlas.addContentChild(chestTilePanel(atlas, tile));
            }
            for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
                if (!tile.proximate()) {
                    continue;
                }
                for (String islandId : tile.linkedIslandIds()) {
                    SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(islandId);
                    if (island == null) {
                        continue;
                    }
                    UIElement thread = linkThread(tile, island);
                    if (thread != null) {
                        atlas.addContentChild(thread);
                    }
                }
            }
            for (SlotWorkspaceViewModel.AtlasItem item : viewModel.atlasItems()) {
                atlas.addContentChild(atlasCardButton(atlas, item));
                addAtlasItemChips(atlas, item);
            }
            if (viewModel.atlasItems().isEmpty()) {
                UIElement empty = label("No main inventory stacks visible", MUTED)
                        .layout(layout -> layout
                                .positionType(TaffyPosition.ABSOLUTE)
                                .left(viewModel.canvasWidth() / 2f - 104)
                                .top(viewModel.canvasHeight() / 2f - 8)
                                .width(208)
                                .height(16));
                empty.setAllowHitTest(false);
                atlas.addContentChild(empty);
            }
        }

        private UIElement islandPanel(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
            UIElement panel = panel(island.color()).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(island.x())
                    .top(island.y())
                    .width(island.width())
                    .height(island.height())
                    .paddingAll(8)
                    .gapAll(3)
                    .flexDirection(FlexDirection.COLUMN));

            Button header = button(island.label(), true, island.color());
            header.layout(layout -> layout.widthPercent(100).height(18));
            header.addEventListener(UIEvents.CLICK, event -> {
                if (event.button != 0) {
                    return;
                }
                event.stopPropagation();
                if (selectedAtlasItem() == null) {
                    localStatus = "select a triage or homed item first";
                    return;
                }
                sendAssignHome(island.islandId());
            });

            Label subtitle = label(islandSubtitle(island), MUTED);
            subtitle.layout(layout -> layout.widthPercent(100).height(12));
            subtitle.setAllowHitTest(false);

            UIElement line = panel(ISLAND_BORDER).layout(layout -> layout.widthPercent(100).height(1));
            line.setAllowHitTest(false);

            panel.addChildren(header, subtitle, line);
            installViewportPanSurface(panel, atlas);
            installIslandDragSource(header, atlas, island);
            installIslandDropTarget(panel, panel, atlas, island);
            installIslandDropTarget(header, panel, atlas, island);

            if (island.carriedCount() > 0) {
                Button carriedBadge = button(
                        island.carriedCount() + "●",
                        true,
                        ACTIVE_HOTBAR
                );
                carriedBadge.layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(4)
                        .top(4)
                        .width(26)
                        .height(12));
                carriedBadge.textStyle(style -> style
                        .textColor(TEXT)
                        .textShadow(false)
                        .fontSize(7)
                        .textAlignHorizontal(Horizontal.CENTER)
                        .textAlignVertical(Vertical.CENTER));
                carriedBadge.style(style -> style.zIndex(4));
                carriedBadge.setOnClick(event -> {
                    event.stopPropagation();
                    panToIsland(atlas, island);
                    localStatus = "panned to " + island.label();
                });
                panel.addChild(carriedBadge);
            }

            if (island.kind() == VisualAtlasIslandKind.PLAYER) {
                Button editButton = button("...", true, PANEL_ALT);
                editButton.layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .right(4)
                        .top(4)
                        .width(18)
                        .height(14));
                editButton.textStyle(style -> style
                        .textColor(TEXT)
                        .textShadow(false)
                        .fontSize(8)
                        .textAlignHorizontal(Horizontal.CENTER)
                        .textAlignVertical(Vertical.CENTER));
                editButton.style(style -> style.zIndex(4));
                editButton.setOnClick(event -> {
                    event.stopPropagation();
                    beginIslandEdit(island);
                });
                panel.addChild(editButton);
            }

            IslandRenderBudget[] lastBudget = new IslandRenderBudget[1];
            float[] lastScale = {Float.NaN};
            String[] lastSubtitleText = new String[]{""};
            panel.addEventListener(UIEvents.TICK, event -> {
                IslandRenderBudget budget = IslandRenderBudget.forScreenBudget(
                        Math.max(1, atlas.screenPixelsForWorldUnits(island.width()))
                );
                boolean budgetChanged = !budget.equals(lastBudget[0]);
                float currentScale = atlas.getScale();
                boolean scaleChanged = currentScale != lastScale[0];
                if (budgetChanged || scaleChanged) {
                    header.textStyle(style -> style
                            .textColor(TEXT)
                            .textShadow(false)
                            .fontSize(atlas.worldUnitsForPixels(budget.titleFontPx()))
                            .textAlignHorizontal(Horizontal.CENTER)
                            .textAlignVertical(Vertical.CENTER));
                    subtitle.textStyle(style -> style
                            .textColor(MUTED)
                            .textShadow(false)
                            .fontSize(atlas.worldUnitsForPixels(budget.subtitleFontPx()))
                            .textAlignHorizontal(Horizontal.LEFT)
                            .textAlignVertical(Vertical.CENTER));
                    lastScale[0] = currentScale;
                }
                if (budgetChanged || scaleChanged) {
                    header.layout(layout -> layout.widthPercent(100).height(atlas.worldUnitsForPixels(budget.headerHeightPx())));
                    subtitle.layout(layout -> layout.widthPercent(100).height(atlas.worldUnitsForPixels(budget.subtitleHeightPx())));
                    line.layout(layout -> layout.widthPercent(100).height(atlas.worldUnitsForPixels(budget.ruleHeightPx())));
                    subtitle.setDisplay(budget.showSubtitle());
                    line.setDisplay(budget.showSubtitle());
                    lastBudget[0] = budget;
                    panel.markTaffyStyleDirty();
                }
                String nextSubtitle = islandSubtitle(island);
                if (!nextSubtitle.equals(lastSubtitleText[0])) {
                    subtitle.setText(Component.literal(nextSubtitle));
                    lastSubtitleText[0] = nextSubtitle;
                }
            });
            return panel;
        }

        private StorageZoneBounds storageZoneBounds() {
            List<SlotWorkspaceViewModel.ClaimedChestTile> tiles = viewModel.claimedChestTiles();
            if (tiles.isEmpty()) {
                return null;
            }
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            for (SlotWorkspaceViewModel.ClaimedChestTile tile : tiles) {
                minX = Math.min(minX, tile.atlasX());
                minY = Math.min(minY, tile.atlasY());
                maxX = Math.max(maxX, tile.atlasX() + tile.width());
                maxY = Math.max(maxY, tile.atlasY() + tile.height());
            }
            int pad = SlotWorkspaceAtlasLayout.STORAGE_ZONE_PADDING;
            return new StorageZoneBounds(
                    minX - pad,
                    minY - pad,
                    (maxX - minX) + pad * 2,
                    (maxY - minY) + pad * 2
            );
        }

        private UIElement storageZoneBackdrop(StorageZoneBounds bounds) {
            UIElement backdrop = panel(STORAGE_ZONE_FILL).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(bounds.left())
                    .top(bounds.top())
                    .width(bounds.width())
                    .height(bounds.height()));
            backdrop.style(style -> style.zIndex(0));
            backdrop.setAllowHitTest(false);
            return backdrop;
        }

        private UIElement storageZoneHeader(StorageZoneBounds bounds, SlotAtlasGraphView atlas) {
            int headerHeight = STORAGE_ZONE_HEADER_HEIGHT;
            UIElement header = panel(STORAGE_ZONE_HEADER_FILL).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(bounds.left())
                    .top(bounds.top() - headerHeight)
                    .width(bounds.width())
                    .height(headerHeight)
                    .paddingHorizontal(8)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            header.style(style -> style.zIndex(1));
            Label title = label("Storage", ACCENT);
            title.layout(layout -> layout.flex(1).height(headerHeight));
            title.textStyle(style -> style
                    .textColor(ACCENT)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            title.setAllowHitTest(false);
            header.addChild(title);
            installStorageZoneDragSource(header, atlas, bounds);
            return header;
        }

        private void installStorageZoneDragSource(UIElement source, SlotAtlasGraphView atlas, StorageZoneBounds bounds) {
            int[] clickWorldX = {Integer.MIN_VALUE};
            int[] clickWorldY = {Integer.MIN_VALUE};
            source.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button != 0) {
                    return;
                }
                clickWorldX[0] = atlas.worldX(event.x);
                clickWorldY[0] = atlas.worldY(event.y);
            });
            source.addEventListener(UIEvents.MOUSE_UP, event -> {
                clickWorldX[0] = Integer.MIN_VALUE;
                clickWorldY[0] = Integer.MIN_VALUE;
            });
            source.addEventListener(UIEvents.MOUSE_MOVE, event -> {
                if (clickWorldX[0] == Integer.MIN_VALUE) {
                    return;
                }
                if (!source.isMouseDown(0) || isDragging(source)) {
                    return;
                }
                float scale = atlas.getScale();
                float screenDx = (atlas.worldX(event.x) - clickWorldX[0]) * scale;
                float screenDy = (atlas.worldY(event.y) - clickWorldY[0]) * scale;
                if (screenDx * screenDx + screenDy * screenDy < DRAG_START_THRESHOLD_PX * DRAG_START_THRESHOLD_PX) {
                    return;
                }
                int grabOffsetX = clickWorldX[0] - bounds.left();
                int grabOffsetY = clickWorldY[0] - bounds.top();
                int widthPx = Math.max(48, atlas.screenPixelsForWorldUnits(bounds.width()));
                int heightPx = Math.max(20, atlas.screenPixelsForWorldUnits(bounds.height() + STORAGE_ZONE_HEADER_HEIGHT));
                int dragOffsetX = Math.round(grabOffsetX * scale);
                int dragOffsetY = Math.round((grabOffsetY + STORAGE_ZONE_HEADER_HEIGHT) * scale);
                source.startDrag(
                        new StorageZoneDrag(grabOffsetX, grabOffsetY, bounds.left(), bounds.top()),
                        rect((STORAGE_ZONE_FILL & 0x00FFFFFF) | 0x60000000)
                ).setDragTexture(-dragOffsetX, -dragOffsetY, widthPx, heightPx);
                localStatus = "moving storage zone";
            });
            source.addEventListener(UIEvents.DRAG_END, event -> handleDragEnd(event));
        }

        private UIElement chestTilePanel(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.ClaimedChestTile tile) {
            int fill = tile.proximate() ? STORAGE_TILE_FILL : STORAGE_TILE_FILL_DIM;
            int textColor = tile.proximate() ? TEXT : MUTED;
            int cellSize = SlotWorkspaceAtlasLayout.CHEST_TILE_CELL;
            int cols = SlotWorkspaceAtlasLayout.CHEST_TILE_COLUMNS;
            int padding = SlotWorkspaceAtlasLayout.CHEST_TILE_PADDING;
            int headerHeight = SlotWorkspaceAtlasLayout.CHEST_TILE_HEADER_HEIGHT;

            UIElement panel = panel(fill).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(tile.atlasX())
                    .top(tile.atlasY())
                    .width(tile.width())
                    .height(tile.height())
                    .paddingAll(0));
            panel.style(style -> style.zIndex(1));

            Label header = label(tile.label(), textColor);
            header.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(padding)
                    .top(0)
                    .width(tile.width() - padding * 2)
                    .height(headerHeight));
            header.textStyle(style -> style
                    .textColor(textColor)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            panel.addChild(header);

            int cellsToRender = tile.contents().size();
            List<Integer> contentIndices = tile.contentSlotIndices();
            for (int index = 0; index < cellsToRender; index++) {
                ItemStack stack = tile.contents().get(index);
                int chestSlotIndex = index < contentIndices.size() ? contentIndices.get(index) : index;
                int col = index % cols;
                int row = index / cols;
                int cellX = padding + col * cellSize;
                int cellY = headerHeight + row * cellSize;
                UIElement cell = chestTileCell(stack, tile.proximate(), cellSize);
                cell.layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(cellX)
                        .top(cellY)
                        .width(cellSize)
                        .height(cellSize));
                if (tile.proximate() && stack != null && !stack.isEmpty()) {
                    String storageId = tile.storageId();
                    ItemStack cellStack = stack;
                    cell.addEventListener(UIEvents.CLICK, event -> {
                        if (event.button != 0) {
                            return;
                        }
                        event.stopPropagation();
                        if (Screen.hasShiftDown()) {
                            sendTakeFromChest(storageId, chestSlotIndex);
                            return;
                        }
                        SlotWorkspaceViewModel.IdentityRef identityRef = SlotWorkspaceViewModel.IdentityRef.from(
                                dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(cellStack));
                        selectedAtlasIdentity = identityRef;
                        selectedHotbarIndex = -1;
                        localStatus = "selected " + cellStack.getHoverName().getString();
                        rebuild();
                    });
                    installChestStackDragSource(cell, atlas, storageId, chestSlotIndex, cellStack, tile.label());
                }
                panel.addChild(cell);
            }

            Button linkButton = button("Link", true, tile.linkedIslandIds().isEmpty() ? PANEL_ALT : ACCENT);
            linkButton.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(padding)
                    .top(0)
                    .width(28)
                    .height(headerHeight));
            linkButton.textStyle(style -> style
                    .textColor(TEXT)
                    .textShadow(false)
                    .fontSize(7)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            linkButton.style(style -> style.zIndex(3));
            linkButton.setOnClick(event -> {
                event.stopPropagation();
                beginChestLinkEdit(tile);
            });
            panel.addChild(linkButton);

            boolean canTake = tile.proximate() && !tile.contents().isEmpty();
            Button takeAllButton = button("Take", canTake, canTake ? ROW : PANEL_ALT);
            takeAllButton.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(padding + 30)
                    .top(0)
                    .width(28)
                    .height(headerHeight));
            takeAllButton.textStyle(style -> style
                    .textColor(canTake ? TEXT : MUTED)
                    .textShadow(false)
                    .fontSize(7)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            takeAllButton.style(style -> style.zIndex(3));
            String takeStorageId = tile.storageId();
            takeAllButton.setOnClick(event -> {
                event.stopPropagation();
                if (!canTake) {
                    localStatus = tile.proximate() ? "chest is empty" : "chest is too far";
                    rebuild();
                    return;
                }
                sendTakeAll(takeStorageId);
            });
            panel.addChild(takeAllButton);

            installChestTileDragSource(panel, atlas, tile);
            installChestTileDropTarget(panel, tile);
            return panel;
        }

        private void installChestTileDropTarget(UIElement target, SlotWorkspaceViewModel.ClaimedChestTile tile) {
            String storageId = tile.storageId();
            boolean proximate = tile.proximate();
            target.addEventListener(UIEvents.DRAG_ENTER, event -> updateChestTileDropOverlay(target, proximate, event), true);
            target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateChestTileDropOverlay(target, proximate, event));
            target.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(target), true);
            target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                clearDropOverlay(target);
                AtlasItemDrag atlasDrag = atlasItemDrag(event);
                HotbarSlotDrag hotbarDrag = hotbarSlotDrag(event);
                if (atlasDrag == null && hotbarDrag == null) {
                    return;
                }
                if (!proximate) {
                    localStatus = "chest is too far";
                    rebuild();
                    event.stopPropagation();
                    return;
                }
                if (atlasDrag != null) {
                    sendDepositCarriedToChest(atlasDrag.identity(), storageId);
                } else {
                    sendDepositHotbarToChest(hotbarDrag.hotbarIndex(), storageId);
                }
                event.stopPropagation();
            });
        }

        private void updateChestTileDropOverlay(UIElement target, boolean proximate, UIEvent event) {
            boolean acceptable = atlasItemDrag(event) != null || hotbarSlotDrag(event) != null;
            updateGenericDropOverlay(target, acceptable, proximate ? ACCENT : WARNING);
        }

        private void sendDepositCarriedToChest(SlotWorkspaceViewModel.IdentityRef identity, String storageId) {
            if (depositCarriedToChestEmitter == null || identity == null || storageId == null || storageId.isBlank()) {
                return;
            }
            boolean sent = depositCarriedToChestEmitter.send(
                    identity.itemId(),
                    identity.comparisonMode(),
                    identity.componentFingerprint(),
                    storageId
            );
            if (!sent) {
                localStatus = "deposit unavailable";
                rebuild();
            }
        }

        private void sendDepositHotbarToChest(int hotbarIndex, String storageId) {
            if (depositHotbarToChestEmitter == null || storageId == null || storageId.isBlank()) {
                return;
            }
            boolean sent = depositHotbarToChestEmitter.send(hotbarIndex, storageId);
            if (!sent) {
                localStatus = "deposit unavailable";
                rebuild();
            }
        }

        private void sendTakeFromChest(String storageId, int chestSlotIndex) {
            if (takeFromChestEmitter == null || storageId == null || storageId.isBlank()) {
                return;
            }
            boolean sent = takeFromChestEmitter.send(storageId, chestSlotIndex);
            if (!sent) {
                localStatus = "take unavailable";
                rebuild();
            }
        }

        private Set<String> highlightedIslandIdsFromProximateTiles() {
            LinkedHashSet<String> highlighted = new LinkedHashSet<>();
            for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
                if (tile.proximate()) {
                    highlighted.addAll(tile.linkedIslandIds());
                }
            }
            return highlighted;
        }

        private void addIslandHighlightFrame(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
            int thickness = LINK_HIGHLIGHT_THICKNESS;
            int color = LINK_HIGHLIGHT_COLOR;
            int x = island.x();
            int y = island.y();
            int w = island.width();
            int h = island.height();
            atlas.addContentChild(highlightFrameSegment(color, x - thickness, y - thickness, w + thickness * 2, thickness));
            atlas.addContentChild(highlightFrameSegment(color, x - thickness, y + h, w + thickness * 2, thickness));
            atlas.addContentChild(highlightFrameSegment(color, x - thickness, y, thickness, h));
            atlas.addContentChild(highlightFrameSegment(color, x + w, y, thickness, h));
        }

        private UIElement highlightFrameSegment(int color, int x, int y, int w, int h) {
            UIElement segment = panel(color).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(x)
                    .top(y)
                    .width(w)
                    .height(h));
            segment.style(style -> style.zIndex(5));
            segment.setAllowHitTest(false);
            return segment;
        }

        private UIElement linkThread(
                SlotWorkspaceViewModel.ClaimedChestTile tile,
                SlotWorkspaceViewModel.AtlasIsland island
        ) {
            int tileCenterX = tile.atlasX() + tile.width() / 2;
            int tileCenterY = tile.atlasY() + tile.height() / 2;
            int islandCenterX = island.x() + island.width() / 2;
            int islandCenterY = island.y() + island.height() / 2;
            int dx = islandCenterX - tileCenterX;
            int dy = islandCenterY - tileCenterY;
            double distance = Math.sqrt((double) dx * dx + (double) dy * dy);
            if (distance < 1.0) {
                return null;
            }
            int length = Math.max(1, (int) Math.round(distance));
            int thickness = 2;
            float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
            UIElement thread = panel(LINK_THREAD_COLOR).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(tileCenterX)
                    .top(tileCenterY - thickness / 2)
                    .width(length)
                    .height(thickness));
            thread.style(style -> style.zIndex(0));
            thread.transform(transform -> transform.pivot(0f, 0.5f).rotation(angleDeg));
            thread.setAllowHitTest(false);
            return thread;
        }

        private UIElement hoverTrailOverlay(SlotAtlasGraphView atlas) {
            UIElement trail = panel(HOVER_TRAIL_COLOR);
            trail.style(style -> style.zIndex(9));
            trail.setAllowHitTest(false);
            trail.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(0)
                    .top(0)
                    .width(0)
                    .height(0));
            int[] lastLength = {0};
            trail.addEventListener(UIEvents.TICK, event -> {
                HoverTrailEndpoints endpoints = resolveHoverTrail();
                if (endpoints == null) {
                    if (lastLength[0] != 0) {
                        trail.layout(layout -> layout
                                .positionType(TaffyPosition.ABSOLUTE)
                                .left(0).top(0).width(0).height(0));
                        trail.markTaffyStyleDirty();
                        lastLength[0] = 0;
                    }
                    return;
                }
                UIElement slotElement = hotbarSlotElements.get(endpoints.hotbarIndex());
                if (slotElement == null) {
                    return;
                }
                float panelLeft = atlas.getPositionX();
                float panelTop = atlas.getPositionY();
                float slotW = slotElement.getSizeWidth();
                float slotH = slotElement.getSizeHeight();
                if (slotW <= 0f || slotH <= 0f) {
                    return;
                }
                float originScreenX = slotElement.getPositionX() + slotW / 2f;
                float originScreenY = slotElement.getPositionY() + slotH / 2f;
                int worldTargetX = endpoints.atlasItem().x() + endpoints.atlasItem().width() / 2;
                int worldTargetY = endpoints.atlasItem().y() + endpoints.atlasItem().height() / 2;
                float targetScreenX = atlas.screenX(worldTargetX);
                float targetScreenY = atlas.screenY(worldTargetY);
                float dx = targetScreenX - originScreenX;
                float dy = targetScreenY - originScreenY;
                double distance = Math.sqrt((double) dx * dx + (double) dy * dy);
                if (distance < 1.0) {
                    return;
                }
                int length = Math.max(1, (int) Math.round(distance));
                float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
                int leftRelative = Math.round(originScreenX - panelLeft);
                int topRelative = Math.round(originScreenY - panelTop) - HOVER_TRAIL_THICKNESS / 2;
                trail.layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(leftRelative)
                        .top(topRelative)
                        .width(length)
                        .height(HOVER_TRAIL_THICKNESS));
                trail.transform(transform -> transform.pivot(0f, 0.5f).rotation(angleDeg));
                trail.markTaffyStyleDirty();
                lastLength[0] = length;
            });
            return trail;
        }

        private HoverTrailEndpoints resolveHoverTrail() {
            if (hoveredHotbarIndex >= 0 && hoveredHotbarIndex < viewModel.hotbarSlots().size()) {
                SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(hoveredHotbarIndex);
                if (slot.occupied()) {
                    SlotWorkspaceViewModel.IdentityRef identity = SlotWorkspaceViewModel.IdentityRef.from(
                            dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(slot.displayStack()));
                    SlotWorkspaceViewModel.AtlasItem item = atlasItemInIslandLayer(identity);
                    if (item != null) {
                        return new HoverTrailEndpoints(slot.hotbarIndex(), item);
                    }
                }
            }
            if (hoveredAtlasIdentity != null) {
                int hotbarIndex = hotbarSlotForIdentity(hoveredAtlasIdentity);
                SlotWorkspaceViewModel.AtlasItem item = atlasItemInIslandLayer(hoveredAtlasIdentity);
                if (hotbarIndex >= 0 && item != null) {
                    return new HoverTrailEndpoints(hotbarIndex, item);
                }
            }
            return null;
        }

        private SlotWorkspaceViewModel.AtlasItem atlasItemInIslandLayer(SlotWorkspaceViewModel.IdentityRef identity) {
            if (identity == null) {
                return null;
            }
            for (SlotWorkspaceViewModel.AtlasItem candidate : viewModel.atlasItems()) {
                if (candidate.identity().equals(identity)) {
                    return candidate;
                }
            }
            return null;
        }

        private boolean shouldAccentHotbarSlot(SlotWorkspaceViewModel.HotbarSlot slot) {
            if (hoveredAtlasIdentity == null) {
                return false;
            }
            SlotWorkspaceViewModel.IdentityRef slotIdentity = SlotWorkspaceViewModel.IdentityRef.from(
                    dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(slot.displayStack()));
            return hoveredAtlasIdentity.equals(slotIdentity);
        }

        private boolean shouldAccentTriageRow(SlotWorkspaceViewModel.AtlasItem item) {
            if (hoveredHotbarIndex < 0 || hoveredHotbarIndex >= viewModel.hotbarSlots().size()) {
                return false;
            }
            SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(hoveredHotbarIndex);
            if (!slot.occupied()) {
                return false;
            }
            SlotWorkspaceViewModel.IdentityRef slotIdentity = SlotWorkspaceViewModel.IdentityRef.from(
                    dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(slot.displayStack()));
            return item.identity().equals(slotIdentity);
        }

        private record HoverTrailEndpoints(int hotbarIndex, SlotWorkspaceViewModel.AtlasItem atlasItem) {
        }

        private UIElement chestTileCell(ItemStack stack, boolean proximate, int cellSize) {
            int chromeColor = proximate ? STORAGE_TILE_CELL_FILL : STORAGE_TILE_CELL_FILL_DIM;
            UIElement cell = panel(chromeColor);
            if (stack != null && !stack.isEmpty()) {
                int iconSize = Math.max(8, cellSize - 2);
                UIElement icon = itemIcon(stack, iconSize, proximate);
                icon.layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(1)
                        .top(1));
                cell.addChild(icon);
            }
            return cell;
        }

        private void installChestTileDragSource(
                UIElement source,
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.ClaimedChestTile tile
        ) {
            int[] clickWorldX = {Integer.MIN_VALUE};
            int[] clickWorldY = {Integer.MIN_VALUE};
            source.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button != 0) {
                    return;
                }
                clickWorldX[0] = atlas.worldX(event.x);
                clickWorldY[0] = atlas.worldY(event.y);
            }, true);
            source.addEventListener(UIEvents.MOUSE_UP, event -> {
                clickWorldX[0] = Integer.MIN_VALUE;
                clickWorldY[0] = Integer.MIN_VALUE;
            }, true);
            source.addEventListener(UIEvents.MOUSE_MOVE, event -> {
                if (clickWorldX[0] == Integer.MIN_VALUE) {
                    return;
                }
                if (!source.isMouseDown(0) || isDragging(source)) {
                    return;
                }
                float scale = atlas.getScale();
                float screenDx = (atlas.worldX(event.x) - clickWorldX[0]) * scale;
                float screenDy = (atlas.worldY(event.y) - clickWorldY[0]) * scale;
                if (screenDx * screenDx + screenDy * screenDy < DRAG_START_THRESHOLD_PX * DRAG_START_THRESHOLD_PX) {
                    return;
                }
                int grabOffsetX = Math.max(0, Math.min(tile.width(), clickWorldX[0] - tile.atlasX()));
                int grabOffsetY = Math.max(0, Math.min(tile.height(), clickWorldY[0] - tile.atlasY()));
                int widthPx = Math.max(48, atlas.screenPixelsForWorldUnits(tile.width()));
                int heightPx = Math.max(20, atlas.screenPixelsForWorldUnits(tile.height()));
                int dragOffsetX = Math.round(grabOffsetX * scale);
                int dragOffsetY = Math.round(grabOffsetY * scale);
                source.startDrag(
                        new ChestTileDrag(tile.storageId(), grabOffsetX, grabOffsetY),
                        rect((STORAGE_TILE_FILL & 0x00FFFFFF) | 0x70000000)
                ).setDragTexture(-dragOffsetX, -dragOffsetY, widthPx, heightPx);
                localStatus = "dragging " + tile.label();
            });
            source.addEventListener(UIEvents.DRAG_END, event -> handleDragEnd(event));
        }

        private void installChestStackDragSource(
                UIElement cell,
                SlotAtlasGraphView atlas,
                String storageId,
                int chestSlotIndex,
                ItemStack stack,
                String chestLabel
        ) {
            int[] clickWorldX = {Integer.MIN_VALUE};
            int[] clickWorldY = {Integer.MIN_VALUE};
            cell.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button != 0) {
                    return;
                }
                clickWorldX[0] = atlas.worldX(event.x);
                clickWorldY[0] = atlas.worldY(event.y);
            });
            cell.addEventListener(UIEvents.MOUSE_UP, event -> {
                clickWorldX[0] = Integer.MIN_VALUE;
                clickWorldY[0] = Integer.MIN_VALUE;
            });
            cell.addEventListener(UIEvents.MOUSE_MOVE, event -> {
                if (clickWorldX[0] == Integer.MIN_VALUE) {
                    return;
                }
                if (!cell.isMouseDown(0) || isDragging(cell)) {
                    return;
                }
                float scale = atlas.getScale();
                float screenDx = (atlas.worldX(event.x) - clickWorldX[0]) * scale;
                float screenDy = (atlas.worldY(event.y) - clickWorldY[0]) * scale;
                if (screenDx * screenDx + screenDy * screenDy < DRAG_START_THRESHOLD_PX * DRAG_START_THRESHOLD_PX) {
                    return;
                }
                cell.startDrag(
                        new ChestStackDrag(storageId, chestSlotIndex, stack.copy()),
                        dragTexture(stack)
                ).setDragTexture(-10, -10, 20, 20);
                localStatus = "dragging " + stack.getHoverName().getString() + " from " + chestLabel;
            });
            cell.addEventListener(UIEvents.DRAG_END, event -> {
                Object payload = event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
                if (payload instanceof ChestStackDrag drag
                        && drag.storageId().equals(storageId)
                        && drag.chestSlotIndex() == chestSlotIndex) {
                    sendTakeFromChest(storageId, chestSlotIndex);
                }
                handleDragEnd(event);
            });
        }

        private Button atlasCardButton(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasItem item) {
            boolean selected = item.identity().equals(selectedAtlasIdentity);
            boolean searchMatch = matchesSearch(item);
            boolean activeSearchMatch = !normalizedSearchQuery().isBlank() && searchMatch;
            AtlasRenderBudget initialBudget = atlasBudget(atlas, item);
            Button button = button("", true, cardChromeColor(initialBudget.level(), selected, searchMatch, item.recent(), item.carried()));
            button.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(item.x())
                    .top(item.y())
                    .width(item.width())
                    .height(item.height())
                    .paddingAll(0));
            button.noText();
            button.style(style -> style.zIndex(2));
            button.setOnClick(event -> {
                event.stopPropagation();
                if (event.button == 1) {
                    openContextMenuForAtlas(item, event.x, event.y);
                    return;
                }
                if (event.button == 0 && Screen.hasShiftDown()) {
                    sendAssignHomeToFreeHotbar(item);
                    return;
                }
                selectedAtlasIdentity = item.identity();
                selectedHotbarIndex = -1;
                localStatus = item.playerPlaced()
                        ? "selected homed item: drag to hotbar or another island"
                        : "selected inbox item: drag to an island or create one";
                rebuild();
            });
            button.addEventListener(UIEvents.MOUSE_ENTER, event -> hoveredAtlasIdentity = item.identity(), true);
            button.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                if (item.identity().equals(hoveredAtlasIdentity)) {
                    hoveredAtlasIdentity = null;
                }
            }, true);
            installAtlasHoverTooltip(button, item);
            installAtlasItemDragSource(button, item);

            UIElement body = new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));
            body.setAllowHitTest(false);
            rebuildAtlasBody(body, atlas, item, initialBudget, activeSearchMatch);
            button.addChild(body);

            AtlasRenderBudget[] lastBudget = new AtlasRenderBudget[]{initialBudget};
            button.addEventListener(UIEvents.TICK, event -> {
                AtlasRenderBudget budget = atlasBudget(atlas, item);
                boolean currentSelected = item.identity().equals(selectedAtlasIdentity);
                boolean focused = isMapFocusItem(item);
                if (!lastBudget[0].equals(budget)) {
                    rebuildAtlasBody(body, atlas, item, budget, activeSearchMatch);
                    body.markTaffyStyleDirty();
                    button.markTaffyStyleDirty();
                    lastBudget[0] = budget;
                }
                button.style(style -> style.zIndex(focused ? 10 : currentSelected ? 7 : 2));
                applyButtonColors(button, true, cardChromeColor(budget.level(), currentSelected, searchMatch, item.recent(), item.carried()));
            });
            return button;
        }

        private void addAtlasItemChips(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasItem item) {
            List<ChipSuggestion> chips = item.chipSuggestions();
            if (chips.isEmpty()) {
                return;
            }
            int chipHeight = 10;
            int chipGap = 1;
            for (int index = 0; index < chips.size(); index++) {
                ChipSuggestion chip = chips.get(index);
                int top = item.y() + item.height() + 2 + index * (chipHeight + chipGap);
                Button chipButton = button("", true, chip.color());
                chipButton.noText();
                chipButton.layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(item.x())
                        .top(top)
                        .width(item.width())
                        .height(chipHeight)
                        .paddingAll(1)
                        .gapAll(2)
                        .flexDirection(FlexDirection.ROW)
                        .alignItems(AlignItems.CENTER));
                chipButton.style(style -> style.zIndex(3));
                chipButton.setOnClick(event -> {
                    event.stopPropagation();
                    sendChipAccept(item, chip);
                });
                Label chipLabel = label(chipLabelText(chip), TEXT);
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

        private void sendChipAccept(SlotWorkspaceViewModel.AtlasItem item, ChipSuggestion chip) {
            if (acceptChipEmitter == null) {
                return;
            }
            selectedAtlasIdentity = item.identity();
            String templateName = chip.template() == null ? "" : chip.template().name();
            int accepted = 0;
            for (SlotWorkspaceViewModel.AtlasItem candidate : viewModel.atlasItems()) {
                if (!chipMatches(candidate, chip)) {
                    continue;
                }
                acceptChipEmitter.send(
                        candidate.identity().itemId(),
                        candidate.identity().comparisonMode(),
                        candidate.identity().componentFingerprint(),
                        chip.islandId(),
                        templateName
                );
                accepted++;
            }
            localStatus = accepted <= 1
                    ? "accepting chip: " + chip.label()
                    : "accepting chip: " + chip.label() + " x" + accepted;
            rebuild();
        }

        private static boolean chipMatches(SlotWorkspaceViewModel.AtlasItem item, ChipSuggestion target) {
            if (item == null || target == null) {
                return false;
            }
            for (ChipSuggestion candidate : item.chipSuggestions()) {
                if (candidate == null || candidate.kind() != target.kind()) {
                    continue;
                }
                if (target.kind() == ChipSuggestion.ChipKind.TEMPLATE) {
                    if (candidate.template() == target.template()) {
                        return true;
                    }
                } else if (candidate.islandId().equals(target.islandId())) {
                    return true;
                }
            }
            return false;
        }

        private static String chipLabelText(ChipSuggestion chip) {
            String label = chip.kind() == ChipSuggestion.ChipKind.TEMPLATE && chip.template() != null
                    ? chip.template().defaultLabel()
                    : chip.label();
            return label == null ? "" : shorten(label, 10);
        }

        private UIElement navigationCapsule(SlotAtlasGraphView atlas) {
            UIElement capsule = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(10)
                    .top(10)
                    .width(470)
                    .height(72)
                    .paddingAll(8)
                    .gapAll(6)
                    .flexDirection(FlexDirection.COLUMN));
            capsule.style(style -> style.zIndex(10));
            capsule.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

            UIElement topRow = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(24)
                    .gapAll(6)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            TextField search = new TextField();
            search.setAnyString();
            search.setText(searchQuery, false);
            search.layout(layout -> layout.flex(1).height(22));
            search.style(style -> style.backgroundTexture(rect(0xD60B1117)));
            search.textFieldStyle(style -> style
                    .placeholder(Component.literal("Search homes, ids, islands, collections"))
                    .textColor(TEXT)
                    .cursorColor(ACCENT)
                    .textShadow(false)
                    .fontSize(10));
            search.setTextResponder(value -> {
                searchQuery = value == null ? "" : value;
                localStatus = searchQuery.isBlank() ? "search cleared" : "searching: " + searchQuery.trim();
            });
            topRow.addChildren(
                    label("Atlas", ACCENT).layout(layout -> layout.width(42).height(12)),
                    search,
                    homeButton(atlas),
                    clearSearchButton()
            );

            Label summary = wrappedLabel(searchSummary(), MUTED);
            summary.layout(layout -> layout.widthPercent(100).flex(1));
            summary.addEventListener(UIEvents.TICK, event -> summary.setText(Component.literal(searchSummary())));

            capsule.addChildren(topRow, summary);
            return capsule;
        }

        private Button homeButton(SlotAtlasGraphView atlas) {
            Button button = button("Home", true, PANEL_ALT);
            button.layout(layout -> layout.width(52).height(22));
            button.setOnClick(event -> {
                event.stopPropagation();
                AtlasCamera camera = computeOverviewCamera(atlas.getContentWidth(), atlas.getContentHeight());
                if (camera != null) {
                    atlas.restoreCamera(camera);
                } else {
                    atlas.resetToOverview();
                }
                localStatus = "camera reset";
                rebuild();
            });
            return button;
        }

        private void applyInitialCamera(SlotAtlasGraphView atlas) {
            float viewportWidth = atlas.getContentWidth();
            float viewportHeight = atlas.getContentHeight();
            if (viewportWidth <= 0f || viewportHeight <= 0f) {
                // Viewport not laid out yet; leave atlasCamera null so the next
                // LAYOUT_CHANGED pass can compute the real fit-carried camera.
                return;
            }

            AtlasCamera camera = computeOverviewCamera(viewportWidth, viewportHeight);
            if (camera == null) {
                atlas.fitToChildren(CARRIED_FIT_PADDING_PX, 0.45f);
                atlas.captureCamera();
                return;
            }
            atlas.restoreCamera(camera);
        }

        private AtlasCamera computeOverviewCamera(float viewportWidth, float viewportHeight) {
            ArrayList<FitCarriedCamera.Rect> fitRects = new ArrayList<>();
            for (SlotWorkspaceViewModel.AtlasIsland island : viewModel.islands()) {
                fitRects.add(FitCarriedCamera.Rect.of(island.x(), island.y(), island.width(), island.height()));
            }
            for (SlotWorkspaceViewModel.AtlasItem item : viewModel.atlasItems()) {
                if (item.carried()) {
                    fitRects.add(FitCarriedCamera.Rect.of(item.x(), item.y(), item.width(), item.height()));
                }
            }
            if (fitRects.isEmpty()) {
                return null;
            }
            FitCarriedCamera.Rect bbox = FitCarriedCamera.union(fitRects);
            if (bbox == null) {
                return null;
            }
            // Reserve screen space for the nav capsule (top-left chrome) and the
            // belt overlay (bottom chrome) so they do not occlude content at the
            // default overview zoom.
            float effectiveWidth = Math.max(1f, viewportWidth - 2f * SIDE_CAMERA_INSET_PX);
            float effectiveHeight = Math.max(1f, viewportHeight - NAV_CAPSULE_INSET_PX - BELT_CAMERA_INSET_PX);
            float scale = Math.min(
                    effectiveWidth / Math.max(1f, bbox.width()),
                    effectiveHeight / Math.max(1f, bbox.height())
            );
            scale = Math.max(CARRIED_FIT_MIN_SCALE, Math.min(CARRIED_FIT_MAX_SCALE, scale));
            float centerScreenX = viewportWidth / 2f;
            float centerScreenY = (NAV_CAPSULE_INSET_PX + viewportHeight - BELT_CAMERA_INSET_PX) / 2f;
            float offsetX = bbox.centerX() - centerScreenX / scale;
            float offsetY = bbox.centerY() - centerScreenY / scale;
            return new AtlasCamera(offsetX, offsetY, scale);
        }

        private void panToChestTile(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.ClaimedChestTile tile) {
            if (atlas == null || tile == null) {
                return;
            }
            float viewportWidth = atlas.getContentWidth();
            float viewportHeight = atlas.getContentHeight();
            if (viewportWidth <= 0f || viewportHeight <= 0f) {
                return;
            }
            FitCarriedCamera.Camera camera = FitCarriedCamera.fit(
                    FitCarriedCamera.Rect.of(tile.atlasX(), tile.atlasY(), tile.width(), tile.height()),
                    viewportWidth,
                    viewportHeight,
                    CARRIED_FIT_MIN_SCALE,
                    CARRIED_FIT_MAX_SCALE,
                    CARRIED_FIT_PADDING_PX
            );
            if (camera != null) {
                atlas.restoreCamera(new AtlasCamera(camera.offsetX(), camera.offsetY(), camera.scale()));
            }
        }

        private void panToIsland(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
            if (atlas == null || island == null) {
                return;
            }
            float viewportWidth = atlas.getContentWidth();
            float viewportHeight = atlas.getContentHeight();
            if (viewportWidth <= 0f || viewportHeight <= 0f) {
                return;
            }
            FitCarriedCamera.Camera camera = FitCarriedCamera.fit(
                    FitCarriedCamera.Rect.of(island.x(), island.y(), island.width(), island.height()),
                    viewportWidth,
                    viewportHeight,
                    CARRIED_FIT_MIN_SCALE,
                    CARRIED_FIT_MAX_SCALE,
                    CARRIED_FIT_PADDING_PX
            );
            if (camera != null) {
                atlas.restoreCamera(new AtlasCamera(camera.offsetX(), camera.offsetY(), camera.scale()));
            }
        }

        private Button clearSearchButton() {
            Button button = button("Clear", true, PANEL_ALT);
            button.layout(layout -> layout.width(52).height(22));
            button.setOnClick(event -> {
                event.stopPropagation();
                searchQuery = "";
                localStatus = "search cleared";
                rebuild();
            });
            return button;
        }

        private static final int[] ISLAND_PALETTE = {
                0xCC7D5A3A, 0xCC5A6E3D, 0xCC6E3D3D, 0xCC3D5A6E,
                0xCC3D6E5A, 0xCC5A3D6E, 0xCC5A4A6E, 0xCC4E5A4A
        };

        private void beginChestLinkEdit(SlotWorkspaceViewModel.ClaimedChestTile tile) {
            if (tile == null) {
                return;
            }
            editingChestStorageId = tile.storageId();
            localStatus = "linking " + tile.label();
            rebuild();
        }

        private void endChestLinkEdit() {
            editingChestStorageId = null;
            rebuild();
        }

        private UIElement chestLinkPopover() {
            if (editingChestStorageId == null) {
                return null;
            }
            SlotWorkspaceViewModel.ClaimedChestTile tile = viewModel.claimedChestTile(editingChestStorageId);
            if (tile == null) {
                editingChestStorageId = null;
                return null;
            }

            UIElement capsule = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(10)
                    .top(10)
                    .width(250)
                    .paddingAll(8)
                    .gapAll(6)
                    .flexDirection(FlexDirection.COLUMN));
            capsule.style(style -> style.zIndex(20));
            capsule.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

            UIElement titleRow = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(16)
                    .gapAll(6)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            Label title = label("Manage chest", ACCENT);
            title.layout(layout -> layout.flex(1).height(12));
            Button close = button("x", true, PANEL_ALT);
            close.layout(layout -> layout.width(18).height(14));
            close.setOnClick(event -> {
                event.stopPropagation();
                endChestLinkEdit();
            });
            titleRow.addChildren(title, close);
            capsule.addChild(titleRow);

            TextField nameInput = new TextField();
            nameInput.setAnyString();
            nameInput.setText(tile.label(), false);
            nameInput.layout(layout -> layout.widthPercent(100).height(20));
            nameInput.style(style -> style.backgroundTexture(rect(0xC60D1318)));
            nameInput.textFieldStyle(style -> style
                    .placeholder(Component.literal("Chest name"))
                    .textColor(TEXT)
                    .cursorColor(ACCENT)
                    .textShadow(false)
                    .fontSize(10));
            String currentStorageId = tile.storageId();
            String currentLabel = tile.label();
            nameInput.setTextResponder(value -> {
                String next = value == null ? "" : value;
                String trimmed = next.trim();
                if (trimmed.equals(currentLabel)) {
                    return;
                }
                if (relabelChestEmitter != null) {
                    relabelChestEmitter.send(currentStorageId, trimmed);
                }
            });
            capsule.addChild(nameInput);

            List<SlotWorkspaceViewModel.AtlasIsland> playerIslands = new ArrayList<>();
            for (SlotWorkspaceViewModel.AtlasIsland island : viewModel.islands()) {
                if (island.kind() == VisualAtlasIslandKind.PLAYER) {
                    playerIslands.add(island);
                }
            }
            if (playerIslands.isEmpty()) {
                Label hint = label("No player islands yet — create one first", MUTED);
                hint.layout(layout -> layout.widthPercent(100).height(14));
                capsule.addChild(hint);
                return capsule;
            }

            for (SlotWorkspaceViewModel.AtlasIsland island : playerIslands) {
                boolean linked = tile.linkedIslandIds().contains(island.islandId());
                UIElement row = new UIElement().layout(layout -> layout
                        .widthPercent(100)
                        .height(18)
                        .gapAll(6)
                        .alignItems(AlignItems.CENTER)
                        .flexDirection(FlexDirection.ROW));
                Label name = label(island.label(), linked ? ACCENT : TEXT);
                name.layout(layout -> layout.flex(1).height(12));
                Button action = button(linked ? "Unlink" : "Link", true, linked ? PANEL_ALT : ROW);
                action.layout(layout -> layout.width(54).height(14));
                String islandId = island.islandId();
                String storageId = tile.storageId();
                action.setOnClick(event -> {
                    event.stopPropagation();
                    if (linked) {
                        sendUnlinkChest(islandId, storageId);
                    } else {
                        sendLinkChest(islandId, storageId);
                    }
                });
                row.addChildren(name, action);
                capsule.addChild(row);
            }
            return capsule;
        }

        private void openContextMenuForAtlas(SlotWorkspaceViewModel.AtlasItem item, float screenX, float screenY) {
            if (item == null) {
                return;
            }
            contextMenuAtlasIdentity = item.identity();
            contextMenuHotbarIndex = -1;
            rehomePickerIdentity = null;
            contextMenuScreenX = screenX;
            contextMenuScreenY = screenY;
            rebuild();
        }

        private void openContextMenuForHotbar(SlotWorkspaceViewModel.HotbarSlot slot, float screenX, float screenY) {
            if (slot == null || !slot.occupied()) {
                return;
            }
            contextMenuHotbarIndex = slot.hotbarIndex();
            contextMenuAtlasIdentity = null;
            rehomePickerIdentity = null;
            contextMenuScreenX = screenX;
            contextMenuScreenY = screenY;
            rebuild();
        }

        private void closeContextMenu() {
            contextMenuAtlasIdentity = null;
            contextMenuHotbarIndex = -1;
            rehomePickerIdentity = null;
            rebuild();
        }

        private void openRehomePicker(SlotWorkspaceViewModel.IdentityRef identity) {
            if (identity == null) {
                return;
            }
            rehomePickerIdentity = identity;
            contextMenuAtlasIdentity = null;
            contextMenuHotbarIndex = -1;
            rebuild();
        }

        private UIElement contextMenuOverlay() {
            if (rehomePickerIdentity != null) {
                return buildRehomePicker();
            }
            if (contextMenuAtlasIdentity != null) {
                SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItem(contextMenuAtlasIdentity);
                if (item == null) {
                    contextMenuAtlasIdentity = null;
                    return null;
                }
                return buildAtlasContextMenu(item);
            }
            if (contextMenuHotbarIndex >= 0 && contextMenuHotbarIndex < viewModel.hotbarSlots().size()) {
                SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(contextMenuHotbarIndex);
                if (!slot.occupied()) {
                    contextMenuHotbarIndex = -1;
                    return null;
                }
                return buildHotbarContextMenu(slot);
            }
            return null;
        }

        private UIElement contextMenuCatcher(Runnable onDismiss) {
            UIElement catcher = new UIElement().layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(0).right(0).top(0).bottom(0));
            catcher.style(style -> style.zIndex(21));
            catcher.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                event.stopPropagation();
                onDismiss.run();
            });
            return catcher;
        }

        private UIElement buildAtlasContextMenu(SlotWorkspaceViewModel.AtlasItem item) {
            UIElement catcher = contextMenuCatcher(this::closeContextMenu);
            UIElement menu = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .width(160)
                    .paddingAll(4)
                    .gapAll(2)
                    .flexDirection(FlexDirection.COLUMN));
            anchorPopover(menu, contextMenuScreenX, contextMenuScreenY, 160, 96);
            menu.style(style -> style.zIndex(22));
            menu.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

            menu.addChild(label(shorten(item.name(), 22), ACCENT)
                    .layout(layout -> layout.widthPercent(100).height(12)));

            int freeHotbarIndex = firstFreeHotbarIndex();
            menu.addChild(menuButton(
                    "Send to hotbar",
                    freeHotbarIndex >= 0,
                    freeHotbarIndex >= 0 ? null : "no free hotbar slot",
                    () -> {
                        sendAssignHomeToHotbarOnly(item);
                        closeContextMenu();
                    }
            ));

            boolean depositAvailable = atlasItemHasDepositTarget(item);
            menu.addChild(menuButton(
                    "Deposit to linked chest",
                    depositAvailable,
                    depositAvailable ? null : "no proximate linked chest",
                    () -> {
                        sendDepositHomeToLinkedChest(item);
                        closeContextMenu();
                    }
            ));

            menu.addChild(menuButton(
                    "Re-home\u2026",
                    !viewModel.islands().isEmpty(),
                    viewModel.islands().isEmpty() ? "no islands yet" : null,
                    () -> openRehomePicker(item.identity())
            ));

            menu.addChild(menuButton("Cancel", true, null, this::closeContextMenu));

            UIElement wrapper = new UIElement().layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(0).right(0).top(0).bottom(0));
            wrapper.addChildren(catcher, menu);
            return wrapper;
        }

        private UIElement buildHotbarContextMenu(SlotWorkspaceViewModel.HotbarSlot slot) {
            UIElement catcher = contextMenuCatcher(this::closeContextMenu);
            UIElement menu = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .width(160)
                    .paddingAll(4)
                    .gapAll(2)
                    .flexDirection(FlexDirection.COLUMN));
            anchorPopover(menu, contextMenuScreenX, contextMenuScreenY, 160, 80);
            menu.style(style -> style.zIndex(22));
            menu.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

            String name = slot.displayStack() == null || slot.displayStack().isEmpty()
                    ? "hotbar " + (slot.hotbarIndex() + 1)
                    : slot.displayStack().getHoverName().getString();
            menu.addChild(label(shorten(name, 22), ACCENT)
                    .layout(layout -> layout.widthPercent(100).height(12)));

            int hotbarIdx = slot.hotbarIndex();
            menu.addChild(menuButton(
                    "Send to home",
                    true,
                    null,
                    () -> {
                        sendReturnHotbarToHome(hotbarIdx);
                        closeContextMenu();
                    }
            ));

            menu.addChild(menuButton("Cancel", true, null, this::closeContextMenu));

            UIElement wrapper = new UIElement().layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(0).right(0).top(0).bottom(0));
            wrapper.addChildren(catcher, menu);
            return wrapper;
        }

        private UIElement buildRehomePicker() {
            SlotWorkspaceViewModel.IdentityRef identity = rehomePickerIdentity;
            UIElement catcher = contextMenuCatcher(this::closeContextMenu);
            UIElement menu = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .width(180)
                    .paddingAll(6)
                    .gapAll(3)
                    .flexDirection(FlexDirection.COLUMN));
            int height = Math.min(220, 48 + viewModel.islands().size() * 18);
            anchorPopover(menu, contextMenuScreenX, contextMenuScreenY, 180, height);
            menu.style(style -> style.zIndex(22));
            menu.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

            menu.addChild(label("Re-home to\u2026", ACCENT)
                    .layout(layout -> layout.widthPercent(100).height(12)));

            if (viewModel.islands().isEmpty()) {
                menu.addChild(label("No islands yet", MUTED)
                        .layout(layout -> layout.widthPercent(100).height(14)));
            } else {
                for (SlotWorkspaceViewModel.AtlasIsland island : viewModel.islands()) {
                    String islandId = island.islandId();
                    menu.addChild(menuButton(
                            shorten(island.label(), 22),
                            true,
                            null,
                            () -> {
                                sendAssignHome(identity, islandId, 0, 0);
                                closeContextMenu();
                            }
                    ));
                }
            }
            menu.addChild(menuButton(
                    "Return to Triage",
                    true,
                    null,
                    () -> {
                        sendAssignHome(identity, SlotWorkspaceAtlasLayout.ISLAND_TRIAGE, 0, 0);
                        closeContextMenu();
                    }
            ));
            menu.addChild(menuButton("Cancel", true, null, this::closeContextMenu));

            UIElement wrapper = new UIElement().layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(0).right(0).top(0).bottom(0));
            wrapper.addChildren(catcher, menu);
            return wrapper;
        }

        private Button menuButton(String text, boolean enabled, String disabledHint, Runnable onClick) {
            String label = enabled || disabledHint == null ? text : text + " (" + disabledHint + ")";
            Button button = button(label, enabled, enabled ? ROW : PANEL_ALT);
            button.layout(layout -> layout.widthPercent(100).height(14));
            button.textStyle(style -> style
                    .textColor(enabled ? TEXT : MUTED)
                    .textShadow(false)
                    .fontSize(7)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            if (enabled) {
                button.setOnClick(event -> {
                    event.stopPropagation();
                    if (event.button != 0) {
                        return;
                    }
                    onClick.run();
                });
            }
            return button;
        }

        private void anchorPopover(UIElement menu, float screenX, float screenY, int width, int approxHeight) {
            int left = Math.max(8, Math.round(screenX) + 4);
            int top = Math.max(8, Math.round(screenY) + 4);
            menu.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(left)
                    .top(top)
                    .width(width));
        }

        private int firstFreeHotbarIndex() {
            for (SlotWorkspaceViewModel.HotbarSlot s : viewModel.hotbarSlots()) {
                if (!s.occupied()) {
                    return s.hotbarIndex();
                }
            }
            return -1;
        }

        private boolean atlasItemHasDepositTarget(SlotWorkspaceViewModel.AtlasItem item) {
            if (item == null || item.presence().isEmpty()) {
                return false;
            }
            for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
                SlotWorkspaceViewModel.ClaimedChestTile tile = viewModel.claimedChestTile(entry.storageId());
                if (tile != null && tile.proximate()) {
                    return true;
                }
            }
            return false;
        }

        private void beginIslandEdit(SlotWorkspaceViewModel.AtlasIsland island) {
            if (island == null) {
                return;
            }
            editingIslandId = island.islandId();
            islandLabelDraft = island.label();
            localStatus = "editing " + island.label();
            rebuild();
        }

        private void endIslandEdit() {
            editingIslandId = null;
            islandLabelDraft = "";
            rebuild();
        }

        private UIElement islandEditPopover() {
            if (editingIslandId == null) {
                return null;
            }
            SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(editingIslandId);
            if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER) {
                editingIslandId = null;
                islandLabelDraft = "";
                return null;
            }

            UIElement capsule = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(10)
                    .top(10)
                    .width(250)
                    .paddingAll(8)
                    .gapAll(6)
                    .flexDirection(FlexDirection.COLUMN));
            capsule.style(style -> style.zIndex(20));
            capsule.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

            UIElement titleRow = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(16)
                    .gapAll(6)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            Label title = label("Edit island", ACCENT);
            title.layout(layout -> layout.flex(1).height(12));
            Button close = button("x", true, PANEL_ALT);
            close.layout(layout -> layout.width(18).height(14));
            close.setOnClick(event -> {
                event.stopPropagation();
                endIslandEdit();
            });
            titleRow.addChildren(title, close);
            capsule.addChild(titleRow);

            TextField nameInput = new TextField();
            nameInput.setAnyString();
            nameInput.setText(islandLabelDraft, false);
            nameInput.layout(layout -> layout.widthPercent(100).height(20));
            nameInput.style(style -> style.backgroundTexture(rect(0xC60D1318)));
            nameInput.textFieldStyle(style -> style
                    .placeholder(Component.literal("Island name"))
                    .textColor(TEXT)
                    .cursorColor(ACCENT)
                    .textShadow(false)
                    .fontSize(10));
            nameInput.setTextResponder(value -> islandLabelDraft = value == null ? "" : value);
            Runnable commitRename = () -> {
                if (editingIslandId == null) {
                    return;
                }
                String trimmed = islandLabelDraft == null ? "" : islandLabelDraft.trim();
                if (trimmed.isBlank() || trimmed.equals(island.label())) {
                    return;
                }
                if (renameIslandEmitter != null) {
                    renameIslandEmitter.send(editingIslandId, trimmed);
                }
            };
            nameInput.addEventListener(UIEvents.BLUR, event -> commitRename.run());
            nameInput.addEventListener(UIEvents.KEY_DOWN, event -> {
                if (event.keyCode == GLFW.GLFW_KEY_ENTER || event.keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    commitRename.run();
                    event.stopPropagation();
                }
            });
            capsule.addChild(nameInput);

            capsule.addChild(label("Color", MUTED).layout(layout -> layout.height(10)));
            UIElement paletteRow = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(18)
                    .gapAll(4)
                    .flexDirection(FlexDirection.ROW));
            for (int color : ISLAND_PALETTE) {
                boolean selected = color == island.color();
                Button swatch = button("", true, color);
                swatch.layout(layout -> layout.flex(1).height(18));
                swatch.noText();
                if (selected) {
                    swatch.style(style -> style.zIndex(1));
                }
                int finalColor = color;
                swatch.setOnClick(event -> {
                    event.stopPropagation();
                    if (finalColor == island.color()) {
                        return;
                    }
                    boolean sent = recolorIslandEmitter != null && recolorIslandEmitter.send(editingIslandId, finalColor);
                    localStatus = sent ? "recolor requested" : "recolor unavailable";
                    rebuild();
                });
                paletteRow.addChild(swatch);
            }
            capsule.addChild(paletteRow);

            SlotWorkspaceViewModel.AtlasItem selected = selectedAtlasItem();
            boolean canSetIcon = selected != null;
            Button setIcon = button(
                    canSetIcon ? "Set icon: " + shorten(selected.name(), 16) : "Select an item to set icon",
                    canSetIcon
            );
            setIcon.layout(layout -> layout.widthPercent(100).height(18));
            setIcon.setOnClick(event -> {
                event.stopPropagation();
                if (selected == null) {
                    localStatus = "select an atlas item first";
                    rebuild();
                    return;
                }
                boolean sent = setIslandIconEmitter != null && setIslandIconEmitter.send(
                        editingIslandId,
                        selected.identity().itemId(),
                        selected.identity().comparisonMode(),
                        selected.identity().componentFingerprint()
                );
                localStatus = sent ? "set icon requested" : "set icon unavailable";
                rebuild();
            });
            capsule.addChild(setIcon);

            Button clearIcon = button("Clear icon", true);
            clearIcon.layout(layout -> layout.widthPercent(100).height(18));
            clearIcon.setOnClick(event -> {
                event.stopPropagation();
                boolean sent = setIslandIconEmitter != null && setIslandIconEmitter.send(editingIslandId, "", "", "");
                localStatus = sent ? "clear icon requested" : "clear icon unavailable";
                rebuild();
            });
            capsule.addChild(clearIcon);

            boolean empty = island.itemCount() == 0;
            Button deleteButton = button(empty ? "Delete island" : "Delete (move items first)", empty);
            deleteButton.layout(layout -> layout.widthPercent(100).height(18));
            deleteButton.setOnClick(event -> {
                event.stopPropagation();
                if (!empty) {
                    localStatus = "move all items off this island first";
                    rebuild();
                    return;
                }
                boolean sent = deleteIslandEmitter != null && deleteIslandEmitter.send(editingIslandId);
                localStatus = sent ? "delete requested" : "delete unavailable";
                if (sent) {
                    endIslandEdit();
                    return;
                }
                rebuild();
            });
            capsule.addChild(deleteButton);

            return capsule;
        }

        private void beginCreateIsland(SlotWorkspaceViewModel.AtlasItem item, int worldX, int worldY) {
            if (item == null) {
                return;
            }
            pendingCreateIdentity = item.identity();
            pendingCreateWorldX = worldX;
            pendingCreateWorldY = worldY;
            pendingCreateLabel = item.name();
            pendingCreateColor = ISLAND_PALETTE[0];
            pendingCreateFocusPending = true;
            localStatus = "name the new island";
            rebuild();
        }

        private void endCreateIsland() {
            pendingCreateIdentity = null;
            pendingCreateLabel = "";
            pendingCreateColor = ISLAND_PALETTE[0];
            pendingCreateFocusPending = false;
            rebuild();
        }

        private UIElement createIslandPopover() {
            if (pendingCreateIdentity == null) {
                return null;
            }
            SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItem(pendingCreateIdentity);
            if (item == null) {
                pendingCreateIdentity = null;
                return null;
            }

            UIElement capsule = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(10)
                    .top(10)
                    .width(260)
                    .paddingAll(8)
                    .gapAll(6)
                    .flexDirection(FlexDirection.COLUMN));
            capsule.style(style -> style.zIndex(20));
            capsule.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

            UIElement titleRow = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(16)
                    .gapAll(6)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            Label title = label("New island for " + shorten(item.name(), 18), ACCENT);
            title.layout(layout -> layout.flex(1).height(12));
            Button close = button("x", true, PANEL_ALT);
            close.layout(layout -> layout.width(18).height(14));
            close.setOnClick(event -> {
                event.stopPropagation();
                endCreateIsland();
            });
            titleRow.addChildren(title, close);
            capsule.addChild(titleRow);

            TextField nameInput = new TextField();
            nameInput.setAnyString();
            nameInput.setText(pendingCreateLabel, false);
            nameInput.layout(layout -> layout.widthPercent(100).height(20));
            nameInput.style(style -> style.backgroundTexture(rect(0xC60D1318)));
            nameInput.textFieldStyle(style -> style
                    .placeholder(Component.literal("Island name"))
                    .textColor(TEXT)
                    .cursorColor(ACCENT)
                    .textShadow(false)
                    .fontSize(10));
            nameInput.setTextResponder(value -> pendingCreateLabel = value == null ? "" : value);
            if (pendingCreateFocusPending) {
                pendingCreateFocusPending = false;
                nameInput.addEventListener(UIEvents.LAYOUT_CHANGED, event -> {
                    nameInput.focus();
                    String current = nameInput.getValue();
                    int length = current == null ? 0 : current.length();
                    nameInput.setCursor(length);
                    nameInput.setSelection(0, length);
                }, true);
            }
            capsule.addChild(nameInput);

            capsule.addChild(label("Color", MUTED).layout(layout -> layout.height(10)));
            UIElement paletteRow = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(18)
                    .gapAll(4)
                    .flexDirection(FlexDirection.ROW));
            for (int color : ISLAND_PALETTE) {
                boolean selected = color == pendingCreateColor;
                Button swatch = button("", true, color);
                swatch.layout(layout -> layout.flex(1).height(18));
                swatch.noText();
                if (selected) {
                    swatch.style(style -> style.zIndex(1));
                }
                int finalColor = color;
                swatch.setOnClick(event -> {
                    event.stopPropagation();
                    pendingCreateColor = finalColor;
                    rebuild();
                });
                paletteRow.addChild(swatch);
            }
            capsule.addChild(paletteRow);

            UIElement actionRow = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(20)
                    .gapAll(6)
                    .flexDirection(FlexDirection.ROW));
            Button cancel = button("Cancel", true, PANEL_ALT);
            cancel.layout(layout -> layout.flex(1).height(20));
            cancel.setOnClick(event -> {
                event.stopPropagation();
                endCreateIsland();
            });
            boolean nameReady = pendingCreateLabel != null && !pendingCreateLabel.trim().isBlank();
            Button create = button("Create", nameReady);
            create.layout(layout -> layout.flex(1).height(20));
            create.setOnClick(event -> {
                event.stopPropagation();
                String trimmed = pendingCreateLabel == null ? "" : pendingCreateLabel.trim();
                if (trimmed.isBlank()) {
                    localStatus = "enter an island name";
                    rebuild();
                    return;
                }
                boolean sent = createNamedIslandEmitter != null && createNamedIslandEmitter.send(
                        pendingCreateIdentity.itemId(),
                        pendingCreateIdentity.comparisonMode(),
                        pendingCreateIdentity.componentFingerprint(),
                        trimmed,
                        pendingCreateColor,
                        pendingCreateWorldX,
                        pendingCreateWorldY
                );
                localStatus = sent ? "create island requested" : "create island unavailable";
                if (sent) {
                    endCreateIsland();
                    return;
                }
                rebuild();
            });
            actionRow.addChildren(cancel, create);
            capsule.addChild(actionRow);

            return capsule;
        }

        private UIElement inspectorPanel() {
            UIElement panel = panel(PANEL).layout(layout -> layout
                    .width(284)
                    .heightPercent(100)
                    .paddingAll(8)
                    .gapAll(6)
                    .flexDirection(FlexDirection.COLUMN));
            clearSelectionOnDirectClick(panel);
            panel.addChildren(selectionPanel());
            return panel;
        }

        private UIElement selectionPanel() {
            UIElement panel = panel(PANEL_ALT).layout(layout -> layout
                    .widthPercent(100)
                    .paddingAll(6)
                    .gapAll(4)
                    .flexDirection(FlexDirection.COLUMN));
            SlotWorkspaceViewModel.AtlasItem atlasItem = focusedAtlasItem();
            SlotWorkspaceViewModel.HotbarSlot hotbar = selectedHotbarSlot();
            if (atlasItem != null) {
                SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(atlasItem.islandId());
                ArrayList<UIElement> children = new ArrayList<>();
                UIElement hero = new UIElement().layout(layout -> layout
                        .widthPercent(100)
                        .height(20)
                        .gapAll(6)
                        .alignItems(AlignItems.CENTER)
                        .flexDirection(FlexDirection.ROW));
                hero.addChildren(
                        slotPreview(atlasItem, 18, true),
                        label(shorten(atlasItem.name(), 24), TEXT).layout(layout -> layout.flex(1).height(12)),
                        label("x" + compactCount(atlasItem.totalCount()), ACCENT).layout(layout -> layout.width(28).height(12))
                );
                children.add(label(selectedAtlasItem() != null ? "Selected Item" : "Focused Item", ACCENT).layout(layout -> layout.height(12)));
                children.add(hero);
                children.add(wrappedLabel("id: " + atlasItem.identity().itemId(), MUTED));
                children.add(wrappedLabel("source: main:" + atlasItem.firstSlotIndex(), MUTED));
                children.add(wrappedLabel("home: " + (island == null ? atlasItem.islandId() : island.label()), MUTED));
                children.add(label(selectionHomeStatus(atlasItem), atlasItem.playerPlaced() ? ACCENT : island != null && island.kind() == VisualAtlasIslandKind.TRIAGE ? WARNING : ACCENT)
                        .layout(layout -> layout.height(12)));
                children.add(wrappedLabel("Drag to move this home. Drop on a hotbar slot to assign quick access.", MUTED));
                appendTooltipPreview(children, atlasItem);
                panel.addChildren(children.toArray(UIElement[]::new));
            } else if (hotbar != null) {
                panel.addChildren(
                        label("Selected Hotbar", ACCENT).layout(layout -> layout.height(12)),
                        label("slot " + (hotbar.hotbarIndex() + 1), TEXT).layout(layout -> layout.height(12)),
                        label(hotbar.occupied() ? itemName(hotbar.displayStack()) : "empty", MUTED).layout(layout -> layout.height(12))
                );
            } else {
                panel.addChildren(
                        label("Selection", ACCENT).layout(layout -> layout.height(12)),
                        wrappedLabel("Select an anchor or hotbar slot to inspect it. Rich detail lives here so atlas homes can stay compact.", MUTED)
                );
            }
            return panel;
        }

        private UIElement beltPanel() {
            UIElement panel = panel(PANEL).layout(layout -> layout
                    .widthPercent(100)
                    .height(BELT_HEIGHT)
                    .paddingAll(2)
                    .gapAll(2)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            clearSelectionOnDirectClick(panel);
            panel.addChild(beltSpacer());
            panel.addChild(kitsToggleButton());
            panel.addChild(beltDivider());
            for (SlotWorkspaceViewModel.HotbarSlot slot : viewModel.hotbarSlots()) {
                panel.addChild(beltSlotButton(slot));
            }
            panel.addChild(beltDivider());
            panel.addChild(offhandSlotButton(viewModel.offhand()));
            panel.addChild(beltDivider());
            panel.addChild(equipmentToggleButton());
            panel.addChild(beltSpacer());
            return panel;
        }

        private Button kitsToggleButton() {
            int kitCount = viewModel.kits().size();
            SlotWorkspaceViewModel.KitCard activeCard = viewModel.activeKit();
            String label = activeCard != null ? "Kit:" + shorten(activeCard.name(), 10) : "Kits";
            int color = kitRackOpen ? ACCENT : activeCard != null ? ACTIVE_HOTBAR : PANEL_ALT;
            Button button = button(label, true, color);
            button.layout(layout -> layout
                    .width(Math.max(40, label.length() * 5 + 8))
                    .height(BELT_SLOT_SIZE)
                    .paddingAll(2)
                    .alignItems(AlignItems.CENTER));
            button.textStyle(style -> style
                    .textColor(activeCard != null && !kitRackOpen ? TEXT : MUTED)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            button.setOnClick(event -> {
                event.stopPropagation();
                kitRackOpen = !kitRackOpen;
                localStatus = kitRackOpen
                        ? "kit rack open (" + kitCount + " kit" + (kitCount == 1 ? "" : "s") + ")"
                        : "kit rack closed";
                rebuild();
            });
            return button;
        }

        private UIElement beltSpacer() {
            UIElement spacer = new UIElement().layout(layout -> layout.flex(1).height(1));
            spacer.setAllowHitTest(false);
            return spacer;
        }

        private UIElement beltDivider() {
            UIElement divider = panel(ISLAND_BORDER).layout(layout -> layout.width(1).height(BELT_DIVIDER_HEIGHT));
            divider.setAllowHitTest(false);
            return divider;
        }

        private Button beltSlotButton(SlotWorkspaceViewModel.HotbarSlot slot) {
            boolean selected = selectedHotbarIndex == slot.hotbarIndex();
            int color = selected ? SELECTED : slot.selected() ? ACTIVE_HOTBAR : ROW;
            Button button = button("", true, color);
            button.layout(layout -> layout
                    .width(BELT_SLOT_SIZE)
                    .height(BELT_SLOT_SIZE)
                    .paddingAll(1)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.COLUMN));
            button.noText();
            button.setOnClick(event -> {
                event.stopPropagation();
                if (event.button == 1 && slot.occupied()) {
                    openContextMenuForHotbar(slot, event.x, event.y);
                    return;
                }
                if (event.button == 0 && Screen.hasShiftDown() && slot.occupied()) {
                    sendReturnHotbarToHome(slot.hotbarIndex());
                    return;
                }
                SlotWorkspaceViewModel.AtlasItem atlasItem = selectedAtlasItem();
                if (atlasItem != null) {
                    sendTransfer(
                            SlotWorkspaceUiSession.TARGET_MAIN_SLOT,
                            atlasItem.firstSlotIndex(),
                            SlotWorkspaceUiSession.TARGET_HOTBAR_SLOT,
                            slot.hotbarIndex()
                    );
                    return;
                }
                if (!slot.occupied()) {
                    selectedHotbarIndex = -1;
                    localStatus = "belt " + (slot.hotbarIndex() + 1) + " is empty";
                    rebuild();
                    return;
                }
                selectedHotbarIndex = slot.hotbarIndex();
                selectedAtlasIdentity = null;
                localStatus = "selected belt " + (slot.hotbarIndex() + 1) + " -> drag to atlas to return";
                rebuild();
            });
            installHotbarDragSource(button, slot);
            installHotbarDropTarget(button, slot);
            installHotbarHoverTooltip(button, slot);
            button.addEventListener(UIEvents.MOUSE_ENTER, event -> {
                if (slot.occupied()) {
                    hoveredHotbarIndex = slot.hotbarIndex();
                }
            }, true);
            button.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                if (hoveredHotbarIndex == slot.hotbarIndex()) {
                    hoveredHotbarIndex = -1;
                }
            }, true);
            hotbarSlotElements.put(slot.hotbarIndex(), button);
            if (slot.occupied()) {
                boolean[] lastAccent = {false};
                button.addEventListener(UIEvents.TICK, event -> {
                    boolean accent = shouldAccentHotbarSlot(slot);
                    if (accent != lastAccent[0]) {
                        button.style(style -> style.overlayTexture(accent ? rect(HOVER_ACCENT_OVERLAY) : IGuiTexture.EMPTY));
                        lastAccent[0] = accent;
                    }
                });
            }

            UIElement iconSlot = slot.occupied() ? itemIcon(slot.displayStack(), 16) : emptyIcon();
            iconSlot.layout(layout -> layout.width(16).height(16));
            button.addChild(iconSlot);
            if (slot.occupied() && slot.count() > 1) {
                Label countBadge = label(compactCount(slot.count()), ACCENT);
                countBadge.layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .right(1)
                        .bottom(0)
                        .height(6));
                countBadge.textStyle(style -> style
                        .textColor(ACCENT)
                        .fontSize(6)
                        .textShadow(true)
                        .textAlignHorizontal(Horizontal.RIGHT)
                        .textAlignVertical(Vertical.BOTTOM));
                countBadge.setAllowHitTest(false);
                button.addChild(countBadge);
            }
            Label indexBadge = label(Integer.toString(slot.hotbarIndex() + 1), slot.selected() ? WARNING : MUTED);
            indexBadge.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(1)
                    .top(0)
                    .height(6));
            indexBadge.textStyle(style -> style
                    .textColor(slot.selected() ? WARNING : MUTED)
                    .fontSize(6)
                    .textShadow(true)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.TOP));
            indexBadge.setAllowHitTest(false);
            button.addChild(indexBadge);
            return button;
        }

        private UIElement offhandSlotButton(SlotWorkspaceViewModel.OffhandSlot offhand) {
            Button button = button("", false, ROW_DIM);
            button.layout(layout -> layout
                    .width(BELT_SLOT_SIZE)
                    .height(BELT_SLOT_SIZE)
                    .paddingAll(1)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.COLUMN));
            button.noText();
            button.setActive(false);
            installOffhandHoverTooltip(button, offhand);
            UIElement iconSlot = offhand.occupied() ? itemIcon(offhand.displayStack(), 16) : emptyIcon();
            iconSlot.layout(layout -> layout.width(16).height(16));
            button.addChild(iconSlot);
            if (offhand.occupied() && offhand.count() > 1) {
                Label countBadge = label(compactCount(offhand.count()), MUTED);
                countBadge.layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .right(1)
                        .bottom(0)
                        .height(6));
                countBadge.textStyle(style -> style
                        .textColor(MUTED)
                        .fontSize(6)
                        .textShadow(true)
                        .textAlignHorizontal(Horizontal.RIGHT)
                        .textAlignVertical(Vertical.BOTTOM));
                countBadge.setAllowHitTest(false);
                button.addChild(countBadge);
            }
            Label offLabel = label("off", MUTED);
            offLabel.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(1)
                    .top(0)
                    .height(6));
            offLabel.textStyle(style -> style
                    .textColor(MUTED)
                    .fontSize(6)
                    .textShadow(true)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.TOP));
            offLabel.setAllowHitTest(false);
            button.addChild(offLabel);
            return button;
        }

        private Button equipmentToggleButton() {
            Button button = button("+", false, PANEL_ALT);
            button.layout(layout -> layout
                    .width(BELT_SLOT_SIZE)
                    .height(BELT_SLOT_SIZE)
                    .paddingAll(1)
                    .alignItems(AlignItems.CENTER));
            button.setActive(false);
            button.textStyle(style -> style.textColor(MUTED).textShadow(false).fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            return button;
        }

        private UIElement kitRackOverlay() {
            UIElement overlay = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(16)
                    .right(16)
                    .bottom(BELT_HEIGHT + 10)
                    .paddingAll(6)
                    .gapAll(6)
                    .flexDirection(FlexDirection.COLUMN));
            overlay.style(style -> style.zIndex(7));
            overlay.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());
            overlay.addChild(kitRackHeader());
            overlay.addChild(kitRackBody());
            return overlay;
        }

        private UIElement kitRackHeader() {
            UIElement row = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(16)
                    .gapAll(6)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            int kitCount = viewModel.kits().size();
            Label title = label("Kits (" + kitCount + ")", ACCENT);
            title.layout(layout -> layout.flex(1).height(12));
            title.textStyle(style -> style
                    .textColor(ACCENT)
                    .textShadow(false)
                    .fontSize(9)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            Button save = button("Save Current Belt", true, ACCENT);
            save.layout(layout -> layout.width(92).height(14));
            save.textStyle(style -> style
                    .textColor(TEXT)
                    .textShadow(false)
                    .fontSize(7)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            save.setOnClick(event -> {
                event.stopPropagation();
                sendSaveKit();
            });
            Button close = button("x", true, PANEL_ALT);
            close.layout(layout -> layout.width(14).height(14));
            close.textStyle(style -> style
                    .textColor(MUTED)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            close.setOnClick(event -> {
                event.stopPropagation();
                kitRackOpen = false;
                rebuild();
            });
            row.addChildren(title, save, close);
            return row;
        }

        private UIElement kitRackBody() {
            UIElement body = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(44)
                    .gapAll(6)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            if (viewModel.kits().isEmpty()) {
                Label empty = label("No kits yet. Load your belt, then Save Current Belt.", MUTED);
                empty.layout(layout -> layout.flex(1).height(12));
                empty.textStyle(style -> style
                        .textColor(MUTED)
                        .textShadow(false)
                        .fontSize(8)
                        .textAlignHorizontal(Horizontal.CENTER)
                        .textAlignVertical(Vertical.CENTER));
                body.addChild(empty);
                return body;
            }
            for (SlotWorkspaceViewModel.KitCard card : viewModel.kits()) {
                body.addChild(kitCardButton(card));
            }
            return body;
        }

        private UIElement kitCardButton(SlotWorkspaceViewModel.KitCard card) {
            int color = card.active() ? ACTIVE_HOTBAR : ROW;
            Button button = button("", true, color);
            button.layout(layout -> layout
                    .width(112)
                    .height(40)
                    .paddingAll(3)
                    .gapAll(2)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.COLUMN));
            button.noText();
            button.setOnClick(event -> {
                event.stopPropagation();
                if (card.active()) {
                    sendDeactivateKit();
                } else {
                    sendActivateKit(card.kitId());
                }
            });
            button.addChild(kitCardHeader(card));
            button.addChild(kitCardSlotStrip(card));
            return button;
        }

        private UIElement kitCardHeader(SlotWorkspaceViewModel.KitCard card) {
            UIElement row = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(10)
                    .gapAll(2)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            Label name = label(shorten(card.name(), 12), card.active() ? TEXT : TEXT);
            name.layout(layout -> layout.flex(1).height(9));
            name.textStyle(style -> style
                    .textColor(TEXT)
                    .textShadow(false)
                    .fontSize(7)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            name.setAllowHitTest(false);
            Label readiness = label(card.readyCount() + "/" + card.slotCount(),
                    card.readyCount() == card.slotCount() ? ACCENT : WARNING);
            readiness.layout(layout -> layout.width(22).height(9));
            readiness.textStyle(style -> style
                    .textColor(card.readyCount() == card.slotCount() ? ACCENT : WARNING)
                    .textShadow(false)
                    .fontSize(7)
                    .textAlignHorizontal(Horizontal.RIGHT)
                    .textAlignVertical(Vertical.CENTER));
            readiness.setAllowHitTest(false);
            Button delete = button("x", true, PANEL_ALT);
            delete.layout(layout -> layout.width(10).height(9));
            delete.textStyle(style -> style
                    .textColor(MUTED)
                    .textShadow(false)
                    .fontSize(7)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            delete.setOnClick(event -> {
                event.stopPropagation();
                sendDeleteKit(card.kitId());
            });
            row.addChildren(name, readiness, delete);
            return row;
        }

        private UIElement kitCardSlotStrip(SlotWorkspaceViewModel.KitCard card) {
            UIElement strip = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(12)
                    .gapAll(1)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            strip.setAllowHitTest(false);
            for (SlotWorkspaceViewModel.KitSlotState slot : card.slots()) {
                strip.addChild(kitCardSlotCell(slot));
            }
            return strip;
        }

        private UIElement kitCardSlotCell(SlotWorkspaceViewModel.KitSlotState slot) {
            int fill = !slot.filled() ? 0x60141B22 : slot.ready() ? ROW : ROW_DIM;
            UIElement cell = panel(fill).layout(layout -> layout
                    .width(11)
                    .height(11)
                    .paddingAll(1)
                    .alignItems(AlignItems.CENTER));
            cell.setAllowHitTest(false);
            if (slot.filled() && !slot.displayStack().isEmpty()) {
                cell.addChild(itemIcon(slot.displayStack(), 9, slot.ready()));
            }
            return cell;
        }

        private void sendSaveKit() {
            boolean sent = saveKitEmitter != null && saveKitEmitter.send("");
            localStatus = sent ? "saving kit..." : "save kit unavailable";
            rebuild();
        }

        private void sendActivateKit(String kitId) {
            boolean sent = activateKitEmitter != null && activateKitEmitter.send(kitId);
            localStatus = sent ? "activating kit..." : "activate kit unavailable";
            rebuild();
        }

        private void sendDeactivateKit() {
            boolean sent = deactivateKitEmitter != null && deactivateKitEmitter.send();
            localStatus = sent ? "deactivating kit..." : "deactivate kit unavailable";
            rebuild();
        }

        private void sendDeleteKit(String kitId) {
            boolean sent = deleteKitEmitter != null && deleteKitEmitter.send(kitId);
            localStatus = sent ? "deleting kit..." : "delete kit unavailable";
            rebuild();
        }

        private void installHotbarHoverTooltip(Button button, SlotWorkspaceViewModel.HotbarSlot slot) {
            button.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                if (!slot.occupied() || slot.displayStack().isEmpty()) {
                    return;
                }
                event.hoverTooltips = new HoverTooltips(
                        List.copyOf(DrawerHelper.getItemToolTip(slot.displayStack())),
                        slot.displayStack().getTooltipImage().orElse(null),
                        null,
                        slot.displayStack()
                );
            });
        }

        private void installOffhandHoverTooltip(Button button, SlotWorkspaceViewModel.OffhandSlot offhand) {
            button.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                if (!offhand.occupied() || offhand.displayStack().isEmpty()) {
                    return;
                }
                event.hoverTooltips = new HoverTooltips(
                        List.copyOf(DrawerHelper.getItemToolTip(offhand.displayStack())),
                        offhand.displayStack().getTooltipImage().orElse(null),
                        null,
                        offhand.displayStack()
                );
            });
        }

        private UIElement statusBar() {
            return panel(PANEL_ALT).layout(layout -> layout
                    .widthPercent(100)
                    .height(25)
                    .paddingAll(7))
                    .addChild(label(
                            "selected: " + selectionLabel()
                                    + "  pending: " + viewModel.pendingCount()
                                    + "  rev: " + viewModel.revision()
                                    + "  " + (localStatus.isBlank() ? viewModel.status() : localStatus)
                                    + (viewModel.diagnostics().isBlank() ? "" : "  " + viewModel.diagnostics()),
                            MUTED
                    ).layout(layout -> layout.widthPercent(100).height(12)));
        }

        private void sendTransfer(int sourceKind, int sourceIndex, int destinationKind, int destinationIndex) {
            boolean sent = transferEmitter != null && transferEmitter.send(
                    sourceKind,
                    sourceIndex,
                    destinationKind,
                    destinationIndex,
                    "slot_workspace.ldlib.hotbar_transfer"
            );
            localStatus = sent ? "transfer requested" : "transfer unavailable";
            selectedAtlasIdentity = null;
            selectedHotbarIndex = -1;
            rebuild();
        }

        private void sendAssignHome(String islandId) {
            SlotWorkspaceViewModel.AtlasItem item = selectedAtlasItem();
            if (item == null) {
                localStatus = "select an atlas item first";
                rebuild();
                return;
            }
            sendAssignHome(item.identity(), islandId, -1, -1);
        }

        private void sendAssignHome(
                SlotWorkspaceViewModel.IdentityRef identity,
                String islandId,
                int worldX,
                int worldY
        ) {
            if (identity == null || islandId == null || islandId.isBlank()) {
                localStatus = "invalid home target";
                rebuild();
                return;
            }
            boolean sent = homeEmitter != null && homeEmitter.send(
                    identity.itemId(),
                    identity.comparisonMode(),
                    identity.componentFingerprint(),
                    islandId,
                    worldX,
                    worldY
            );
            localStatus = sent ? "home assignment requested" : "home assignment unavailable";
            selectedAtlasIdentity = null;
            selectedHotbarIndex = -1;
            rebuild();
        }

        private void sendReturnHotbarToHome(int hotbarIndex) {
            if (returnHotbarToHomeEmitter == null) {
                return;
            }
            boolean sent = returnHotbarToHomeEmitter.send(hotbarIndex);
            if (!sent) {
                localStatus = "return-to-home unavailable";
                rebuild();
            }
        }

        private void sendAssignHomeToFreeHotbar(SlotWorkspaceViewModel.AtlasItem item) {
            if (assignHomeToFreeHotbarEmitter == null || item == null) {
                return;
            }
            boolean sent = assignHomeToFreeHotbarEmitter.send(
                    item.identity().itemId(),
                    item.identity().comparisonMode(),
                    item.identity().componentFingerprint()
            );
            if (!sent) {
                localStatus = "assign-to-hotbar unavailable";
                rebuild();
            }
        }

        private void sendAssignHomeToHotbarOnly(SlotWorkspaceViewModel.AtlasItem item) {
            if (assignHomeToHotbarOnlyEmitter == null || item == null) {
                return;
            }
            boolean sent = assignHomeToHotbarOnlyEmitter.send(
                    item.identity().itemId(),
                    item.identity().comparisonMode(),
                    item.identity().componentFingerprint()
            );
            if (!sent) {
                localStatus = "assign-to-hotbar unavailable";
                rebuild();
            }
        }

        private void sendDepositHomeToLinkedChest(SlotWorkspaceViewModel.AtlasItem item) {
            if (depositHomeToLinkedChestEmitter == null || item == null) {
                return;
            }
            boolean sent = depositHomeToLinkedChestEmitter.send(
                    item.identity().itemId(),
                    item.identity().comparisonMode(),
                    item.identity().componentFingerprint()
            );
            if (!sent) {
                localStatus = "deposit unavailable";
                rebuild();
            }
        }

        private int hotbarSlotForIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
            if (identity == null) {
                return -1;
            }
            for (SlotWorkspaceViewModel.HotbarSlot slot : viewModel.hotbarSlots()) {
                if (!slot.occupied()) {
                    continue;
                }
                if (identity.equals(SlotWorkspaceViewModel.IdentityRef.from(
                        dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(slot.displayStack())))) {
                    return slot.hotbarIndex();
                }
            }
            return -1;
        }

        private void sendMoveHotbarToAtlas(int hotbarIndex, String islandId, int worldX, int worldY) {
            boolean sent = hotbarToAtlasEmitter != null && hotbarToAtlasEmitter.send(
                    hotbarIndex,
                    islandId,
                    worldX,
                    worldY
            );
            localStatus = sent ? "return to atlas requested" : "return to atlas unavailable";
            selectedAtlasIdentity = null;
            selectedHotbarIndex = -1;
            rebuild();
        }

        private void sendMoveIsland(String islandId, int worldX, int worldY) {
            if (islandId == null || islandId.isBlank()) {
                localStatus = "invalid island move";
                rebuild();
                return;
            }
            boolean sent = moveIslandEmitter != null && moveIslandEmitter.send(
                    islandId,
                    worldX,
                    worldY
            );
            dev.imagio.slot.SlotCommon.LOGGER.info(
                    "[SLOT] sendMoveIsland id={} worldX={} worldY={} sent={}",
                    islandId, worldX, worldY, sent);
            localStatus = sent ? "island move requested" : "island move unavailable";
            rebuild();
        }

        private void sendTakeAll(String storageId) {
            boolean sent = takeAllEmitter != null && takeAllEmitter.send(storageId);
            localStatus = sent ? "take-all requested" : "take-all unavailable";
            rebuild();
        }

        private void sendDeposit() {
            boolean sent = depositEmitter != null && depositEmitter.send();
            localStatus = sent ? "deposit requested" : "deposit unavailable";
            rebuild();
        }

        private boolean anyChestProximate() {
            for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
                if (tile.proximate()) {
                    return true;
                }
            }
            return false;
        }

        private void sendLinkChest(String islandId, String storageId) {
            if (islandId == null || islandId.isBlank() || storageId == null || storageId.isBlank()) {
                localStatus = "invalid chest link";
                rebuild();
                return;
            }
            boolean sent = linkChestEmitter != null && linkChestEmitter.send(islandId, storageId);
            localStatus = sent ? "chest link requested" : "chest link unavailable";
            rebuild();
        }

        private void sendUnlinkChest(String islandId, String storageId) {
            if (islandId == null || islandId.isBlank() || storageId == null || storageId.isBlank()) {
                localStatus = "invalid chest unlink";
                rebuild();
                return;
            }
            boolean sent = unlinkChestEmitter != null && unlinkChestEmitter.send(islandId, storageId);
            localStatus = sent ? "chest unlink requested" : "chest unlink unavailable";
            rebuild();
        }

        private void sendMoveStorageZone(int deltaX, int deltaY) {
            if (deltaX == 0 && deltaY == 0) {
                return;
            }
            boolean sent = moveStorageZoneEmitter != null && moveStorageZoneEmitter.send(deltaX, deltaY);
            localStatus = sent ? "storage zone moved" : "storage zone move unavailable";
            rebuild();
        }

        private void sendMoveChest(String storageId, int atlasX, int atlasY) {
            if (storageId == null || storageId.isBlank()) {
                localStatus = "invalid chest move";
                rebuild();
                return;
            }
            boolean sent = moveChestEmitter != null && moveChestEmitter.send(
                    storageId,
                    atlasX,
                    atlasY
            );
            localStatus = sent ? "chest move requested" : "chest move unavailable";
            rebuild();
        }

        private void installAtlasItemDragSource(UIElement source, SlotWorkspaceViewModel.AtlasItem item) {
            source.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                if (!source.isMouseDown(0) || isDragging(source)) {
                    return;
                }
                source.startDrag(
                        new AtlasItemDrag(item.identity(), item.displayStack().copy(), item.islandId()),
                        dragTexture(item.displayStack())
                ).setDragTexture(-10, -10, 20, 20);
                localStatus = "dragging " + item.name();
            }, true);
            source.addEventListener(UIEvents.DRAG_END, event -> handleDragEnd(event));
        }

        private void installIslandDragSource(
                UIElement source,
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasIsland island
        ) {
            if (island.kind() != VisualAtlasIslandKind.PLAYER) {
                return;
            }
            int[] clickWorldX = {Integer.MIN_VALUE};
            int[] clickWorldY = {Integer.MIN_VALUE};
            source.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button != 0) {
                    return;
                }
                clickWorldX[0] = atlas.worldX(event.x);
                clickWorldY[0] = atlas.worldY(event.y);
            }, true);
            source.addEventListener(UIEvents.MOUSE_UP, event -> {
                clickWorldX[0] = Integer.MIN_VALUE;
                clickWorldY[0] = Integer.MIN_VALUE;
            }, true);
            source.addEventListener(UIEvents.MOUSE_MOVE, event -> {
                if (clickWorldX[0] == Integer.MIN_VALUE) {
                    return;
                }
                if (!source.isMouseDown(0) || isDragging(source)) {
                    return;
                }
                float scale = atlas.getScale();
                float screenDx = (atlas.worldX(event.x) - clickWorldX[0]) * scale;
                float screenDy = (atlas.worldY(event.y) - clickWorldY[0]) * scale;
                if (screenDx * screenDx + screenDy * screenDy < DRAG_START_THRESHOLD_PX * DRAG_START_THRESHOLD_PX) {
                    return;
                }
                int grabOffsetX = Math.max(0, Math.min(island.width(), clickWorldX[0] - island.x()));
                int grabOffsetY = Math.max(0, Math.min(island.height(), clickWorldY[0] - island.y()));
                int actualWidthPx = Math.max(48, atlas.screenPixelsForWorldUnits(island.width()));
                int actualHeightPx = Math.max(24, atlas.screenPixelsForWorldUnits(island.height()));
                float dragScale = Math.min(1f, Math.min(260f / actualWidthPx, 180f / actualHeightPx));
                int dragWidthPx = Math.max(72, Math.round(actualWidthPx * dragScale));
                int dragHeightPx = Math.max(22, Math.round(actualHeightPx * dragScale));
                int dragOffsetX = Math.round(grabOffsetX * scale * dragScale);
                int dragOffsetY = Math.round(grabOffsetY * scale * dragScale);
                source.startDrag(
                        new IslandDrag(island.islandId(), grabOffsetX, grabOffsetY),
                        rect((island.color() & 0x00FFFFFF) | 0x5A000000)
                ).setDragTexture(-dragOffsetX, -dragOffsetY, dragWidthPx, dragHeightPx);
                localStatus = "dragging island " + island.label();
            }, true);
            source.addEventListener(UIEvents.DRAG_END, event -> handleDragEnd(event));
        }

        private void installAtlasHoverTooltip(Button button, SlotWorkspaceViewModel.AtlasItem item) {
            button.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                if (item == null || item.displayStack().isEmpty()) {
                    return;
                }
                event.hoverTooltips = new HoverTooltips(
                        atlasTooltipLines(item),
                        item.displayStack().getTooltipImage().orElse(null),
                        null,
                        item.displayStack()
                );
            });
        }

        private void installHotbarDragSource(UIElement source, SlotWorkspaceViewModel.HotbarSlot slot) {
            if (!slot.occupied()) {
                return;
            }
            source.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                if (!source.isMouseDown(0) || isDragging(source)) {
                    return;
                }
                source.startDrag(
                        new HotbarSlotDrag(slot.hotbarIndex(), slot.displayStack().copy()),
                        dragTexture(slot.displayStack())
                ).setDragTexture(-10, -10, 20, 20);
                localStatus = "dragging hotbar " + (slot.hotbarIndex() + 1);
            }, true);
            source.addEventListener(UIEvents.DRAG_END, event -> handleDragEnd(event));
        }

        private void installHotbarDropTarget(Button target, SlotWorkspaceViewModel.HotbarSlot slot) {
            target.addEventListener(UIEvents.DRAG_ENTER, event -> updateHotbarDropOverlay(target, slot, event), true);
            target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateHotbarDropOverlay(target, slot, event));
            target.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(target), true);
            target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                clearDropOverlay(target);
                AtlasItemDrag drag = atlasItemDrag(event);
                if (drag == null) {
                    return;
                }
                SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItem(drag.identity());
                if (item == null) {
                    localStatus = "dragged item is no longer visible";
                    rebuild();
                    return;
                }
                if (!item.carried()) {
                    localStatus = "can't move " + item.name() + " to hotbar — none carried";
                    rebuild();
                    event.stopPropagation();
                    return;
                }
                sendTransfer(
                        SlotWorkspaceUiSession.TARGET_MAIN_SLOT,
                        item.firstSlotIndex(),
                        SlotWorkspaceUiSession.TARGET_HOTBAR_SLOT,
                        slot.hotbarIndex()
                );
                event.stopPropagation();
            });
        }

        private void installAtlasBackgroundDropTarget(SlotAtlasGraphView atlas) {
            atlas.addEventListener(UIEvents.DRAG_ENTER, event -> updateAtlasBackgroundDropOverlay(atlas, event), true);
            atlas.addEventListener(UIEvents.DRAG_UPDATE, event -> updateAtlasBackgroundDropOverlay(atlas, event));
            atlas.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(atlas), true);
            atlas.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                if (!isDirectDragTarget(event, atlas)) {
                    return;
                }
                clearDropOverlay(atlas);
                AtlasItemDrag atlasItem = atlasItemDrag(event);
                if (atlasItem != null) {
                    int worldX = atlas.worldX(event.x);
                    int worldY = atlas.worldY(event.y);
                    if (wasDraggedFromTriage(atlasItem)) {
                        SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItem(atlasItem.identity());
                        if (item == null) {
                            localStatus = "dragged item is no longer visible";
                            rebuild();
                            event.stopPropagation();
                            return;
                        }
                        beginCreateIsland(item, worldX, worldY);
                    } else {
                        sendAssignHome(
                                atlasItem.identity(),
                                SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                                worldX,
                                worldY
                        );
                    }
                    event.stopPropagation();
                    return;
                }
                IslandDrag islandDrag = islandDrag(event);
                if (islandDrag != null) {
                    sendMoveIsland(
                            islandDrag.islandId(),
                            atlas.worldX(event.x) - islandDrag.grabOffsetX(),
                            atlas.worldY(event.y) - islandDrag.grabOffsetY()
                    );
                    event.stopPropagation();
                    return;
                }
                ChestTileDrag chestDrag = chestTileDrag(event);
                if (chestDrag != null) {
                    sendMoveChest(
                            chestDrag.storageId(),
                            atlas.worldX(event.x) - chestDrag.grabOffsetX(),
                            atlas.worldY(event.y) - chestDrag.grabOffsetY()
                    );
                    event.stopPropagation();
                    return;
                }
                StorageZoneDrag zoneDrag = storageZoneDrag(event);
                if (zoneDrag != null) {
                    int newLeft = atlas.worldX(event.x) - zoneDrag.grabOffsetX();
                    int newTop = atlas.worldY(event.y) - zoneDrag.grabOffsetY();
                    sendMoveStorageZone(newLeft - zoneDrag.originX(), newTop - zoneDrag.originY());
                    event.stopPropagation();
                    return;
                }
                HotbarSlotDrag hotbarItem = hotbarSlotDrag(event);
                if (hotbarItem != null) {
                    sendMoveHotbarToAtlas(
                            hotbarItem.hotbarIndex(),
                            SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                            atlas.worldX(event.x),
                            atlas.worldY(event.y)
                    );
                    event.stopPropagation();
                }
            });
        }

        private boolean wasDraggedFromTriage(AtlasItemDrag drag) {
            if (drag == null) {
                return false;
            }
            String originIslandId = drag.originIslandId();
            if (SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(originIslandId)) {
                return true;
            }
            SlotWorkspaceViewModel.AtlasIsland origin = viewModel.island(originIslandId);
            return origin != null && origin.kind() == VisualAtlasIslandKind.TRIAGE;
        }

        private void installAtlasCanvasDropTarget(UIElement target, SlotAtlasGraphView atlas) {
            target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                IslandDrag islandDrag = islandDrag(event);
                if (islandDrag != null) {
                    if (event.target == atlas) {
                        return;
                    }
                    sendMoveIsland(
                            islandDrag.islandId(),
                            atlas.worldX(event.x) - islandDrag.grabOffsetX(),
                            atlas.worldY(event.y) - islandDrag.grabOffsetY()
                    );
                    event.stopPropagation();
                    return;
                }
                ChestTileDrag chestDrag = chestTileDrag(event);
                if (chestDrag != null) {
                    if (event.target == atlas) {
                        return;
                    }
                    sendMoveChest(
                            chestDrag.storageId(),
                            atlas.worldX(event.x) - chestDrag.grabOffsetX(),
                            atlas.worldY(event.y) - chestDrag.grabOffsetY()
                    );
                    event.stopPropagation();
                    return;
                }
                StorageZoneDrag zoneDrag = storageZoneDrag(event);
                if (zoneDrag != null) {
                    if (event.target == atlas) {
                        return;
                    }
                    int newLeft = atlas.worldX(event.x) - zoneDrag.grabOffsetX();
                    int newTop = atlas.worldY(event.y) - zoneDrag.grabOffsetY();
                    sendMoveStorageZone(newLeft - zoneDrag.originX(), newTop - zoneDrag.originY());
                    event.stopPropagation();
                }
            });
        }

        private void installIslandDropTarget(
                UIElement target,
                UIElement highlightTarget,
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasIsland island
        ) {
            target.addEventListener(UIEvents.DRAG_ENTER, event -> updateIslandDropOverlay(highlightTarget, island, event), true);
            target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateIslandDropOverlay(highlightTarget, island, event));
            target.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(highlightTarget), true);
            target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                clearDropOverlay(highlightTarget);
                IslandDrag islandDrag = islandDrag(event);
                if (islandDrag != null) {
                    sendMoveIsland(
                            islandDrag.islandId(),
                            atlas.worldX(event.x) - islandDrag.grabOffsetX(),
                            atlas.worldY(event.y) - islandDrag.grabOffsetY()
                    );
                    event.stopPropagation();
                    return;
                }
                AtlasItemDrag atlasItem = atlasItemDrag(event);
                if (atlasItem != null) {
                    sendAssignHome(
                            atlasItem.identity(),
                            island.islandId(),
                            atlas.worldX(event.x),
                            atlas.worldY(event.y)
                    );
                    event.stopPropagation();
                    return;
                }
                HotbarSlotDrag hotbarItem = hotbarSlotDrag(event);
                if (hotbarItem != null) {
                    sendMoveHotbarToAtlas(
                            hotbarItem.hotbarIndex(),
                            island.islandId(),
                            atlas.worldX(event.x),
                            atlas.worldY(event.y)
                    );
                    event.stopPropagation();
                }
            });
        }

        private void installViewportPanSurface(UIElement target, SlotAtlasGraphView atlas) {
            target.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.target != target) {
                    return;
                }
                if (atlas.beginViewportPan(event)) {
                    event.stopPropagation();
                }
            });
        }

        private void updateHotbarDropOverlay(Button target, SlotWorkspaceViewModel.HotbarSlot slot, UIEvent event) {
            AtlasItemDrag drag = atlasItemDrag(event);
            if (drag == null) {
                clearDropOverlay(target);
                return;
            }
            SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItem(drag.identity());
            boolean carried = item != null && item.carried();
            updateGenericDropOverlay(target, carried, carried ? (slot.occupied() ? ACTIVE_HOTBAR : ACCENT) : WARNING);
        }

        private void updateIslandDropOverlay(UIElement highlightTarget, SlotWorkspaceViewModel.AtlasIsland island, UIEvent event) {
            boolean acceptable = atlasItemDrag(event) != null || hotbarSlotDrag(event) != null || islandDrag(event) != null;
            updateGenericDropOverlay(
                    highlightTarget,
                    acceptable,
                    islandDrag(event) != null
                            ? SELECTED
                            : island.kind() == VisualAtlasIslandKind.TRIAGE ? WARNING : ACCENT
            );
        }

        private void updateAtlasBackgroundDropOverlay(SlotAtlasGraphView atlas, UIEvent event) {
            if (!isDirectDragTarget(event, atlas)) {
                clearDropOverlay(atlas);
                return;
            }
            IslandDrag islandDrag = islandDrag(event);
            ChestTileDrag chestDrag = chestTileDrag(event);
            boolean acceptable = atlasItemDrag(event) != null
                    || hotbarSlotDrag(event) != null
                    || islandDrag != null
                    || chestDrag != null;
            int color = islandDrag != null || chestDrag != null ? SELECTED : WARNING;
            updateGenericDropOverlay(atlas, acceptable, color);
        }

        private void updateGenericDropOverlay(UIElement target, boolean active) {
            updateGenericDropOverlay(target, active, ACCENT);
        }

        private void updateGenericDropOverlay(UIElement target, boolean active, int color) {
            target.style(style -> style.overlayTexture(active ? rect((color & 0x00FFFFFF) | 0x44000000) : IGuiTexture.EMPTY));
        }

        private void clearDropOverlay(UIElement target) {
            target.style(style -> style.overlayTexture(IGuiTexture.EMPTY));
        }

        private void handleDragEnd(UIEvent event) {
            if (event.relatedTarget == null) {
                localStatus = "drag cancelled";
            }
        }

        private boolean isDragging(UIElement element) {
            return element.getModularUI() != null && element.getModularUI().getDragHandler().isDragging();
        }

        private AtlasItemDrag atlasItemDrag(UIEvent event) {
            Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            return payload instanceof AtlasItemDrag atlasItemDrag ? atlasItemDrag : null;
        }

        private HotbarSlotDrag hotbarSlotDrag(UIEvent event) {
            Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            return payload instanceof HotbarSlotDrag hotbarSlotDrag ? hotbarSlotDrag : null;
        }

        private IslandDrag islandDrag(UIEvent event) {
            Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            return payload instanceof IslandDrag islandDrag ? islandDrag : null;
        }

        private ChestTileDrag chestTileDrag(UIEvent event) {
            Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            return payload instanceof ChestTileDrag chestTileDrag ? chestTileDrag : null;
        }

        private StorageZoneDrag storageZoneDrag(UIEvent event) {
            Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            return payload instanceof StorageZoneDrag storageZoneDrag ? storageZoneDrag : null;
        }

        private boolean isDirectDragTarget(UIEvent event, UIElement element) {
            return event != null && event.target == element;
        }

        private IGuiTexture dragTexture(ItemStack stack) {
            return new ItemStackTexture(stack == null ? ItemStack.EMPTY : stack.copy());
        }

        private void rebuildAtlasBody(
                UIElement container,
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
            container.clearAllChildren();
            container.addChild(buildAtlasBody(atlas, item, budget, searchMatch));
        }

        private UIElement buildAtlasBody(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
            return switch (budget.level()) {
                case REGION -> regionAtlasBody(atlas, item, budget, searchMatch);
                case BROWSE -> browseAtlasBody(atlas, item, budget, searchMatch);
                case READ -> readAtlasBody(atlas, item, budget, searchMatch);
                case INSPECT -> inspectAtlasBody(atlas, item, budget, searchMatch);
                case DETAIL -> detailAtlasBody(atlas, item, budget, searchMatch);
            };
        }

        private AtlasRenderBudget atlasBudget(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasItem item) {
            int screenBudget = Math.max(1, Math.round(Math.min(item.width(), item.height()) * atlas.getScale()));
            return AtlasRenderBudget.forScreenBudget(screenBudget);
        }

        private UIElement regionAtlasBody(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
            UIElement body = atlasBodyContainer();
            float cardBound = Math.min(item.width(), item.height());
            float shell = Math.min(cardBound, atlas.worldUnitsForPixels(budget.shellPx()));
            float shellLeft = centeredWorld(item.width(), shell);
            float shellTop = centeredWorld(item.height(), shell);
            addCommonAtlasSignals(body, atlas, item, budget, searchMatch);
            body.addChild(slotPreview(atlas, item, budget).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(shellLeft)
                    .top(shellTop)));
            return body;
        }

        private UIElement browseAtlasBody(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
            UIElement body = atlasBodyContainer();
            float cardBound = Math.min(item.width(), item.height());
            float shell = Math.min(cardBound, atlas.worldUnitsForPixels(budget.shellPx()));
            float shellLeft = centeredWorld(item.width(), shell);
            float shellTop = centeredWorld(item.height(), shell);
            addCommonAtlasSignals(body, atlas, item, budget, searchMatch);
            body.addChild(slotPreview(atlas, item, budget).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(shellLeft)
                    .top(shellTop)));
            return body;
        }

        private UIElement readAtlasBody(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
            UIElement body = atlasBodyContainer();
            float sidePad = atlas.worldUnitsForPixels(2f);
            float gap = atlas.worldUnitsForPixels(2f);
            float shellPx = Math.min(budget.cellBudgetPx() * 0.56f, budget.shellPx() + 4f);
            float iconPx = Math.max(12f, shellPx - 6f);
            float shell = atlas.worldUnitsForPixels(shellPx);
            float shellLeft = sidePad;
            float shellTop = atlas.worldUnitsForPixels(2f);
            float labelHeight = atlas.worldUnitsForPixels(budget.primaryLineHeightPx() * 2f + 1f);
            addCommonAtlasSignals(body, atlas, item, budget, searchMatch);
            body.addChild(slotPreview(atlas, item, shellPx, iconPx).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(shellLeft)
                    .top(shellTop)));
            body.addChild(anchorTextBand(
                    atlas,
                    preferredPrimaryLabel(item, budget),
                    searchMatch ? ACCENT : TEXT,
                    budget.primaryFontPx(),
                    budget.primaryMaxChars(),
                    2,
                    0xB4111921,
                    Horizontal.LEFT
            ).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(sidePad)
                    .top(shellTop + shell + gap)
                    .width(item.width() - sidePad * 2f)
                    .height(labelHeight)));
            return body;
        }

        private UIElement inspectAtlasBody(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
            UIElement body = atlasBodyContainer();
            float sidePad = atlas.worldUnitsForPixels(2f);
            float gap = atlas.worldUnitsForPixels(2f);
            String secondary = preferredSecondaryLabel(item, budget);
            boolean hasSecondary = !secondary.isBlank();
            float shellPx = hasSecondary
                    ? Math.min(budget.cellBudgetPx() * 0.50f, budget.shellPx() + 2f)
                    : Math.min(budget.cellBudgetPx() * 0.60f, budget.shellPx() + 10f);
            float iconPx = Math.max(12f, shellPx - 6f);
            float shell = atlas.worldUnitsForPixels(shellPx);
            float shellLeft = sidePad;
            float topPad = atlas.worldUnitsForPixels(2f);
            float nameHeight = atlas.worldUnitsForPixels(budget.primaryLineHeightPx() * 2f + 1f);
            float secondaryHeight = atlas.worldUnitsForPixels(budget.secondaryLineHeightPx());
            float shellTop = topPad;
            addCommonAtlasSignals(body, atlas, item, budget, searchMatch);
            body.addChild(slotPreview(atlas, item, shellPx, iconPx).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(shellLeft)
                    .top(shellTop)));
            float cursorTop = shellTop + shell + gap;
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
                    .width(item.width() - sidePad * 2f)
                    .height(nameHeight)));
            if (hasSecondary) {
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
                        .top(cursorTop + nameHeight + gap)
                        .width(item.width() - sidePad * 2f)
                        .height(secondaryHeight)));
            }
            return body;
        }

        private UIElement detailAtlasBody(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
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
            body.addChild(slotPreview(atlas, item, shellPx, iconPx).layout(layout -> layout
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
                    .width(item.width() - sidePad * 2f)
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
                        .width(item.width() - sidePad * 2f)
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
                        .width(item.width() - sidePad * 2f)
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
                            .width(item.width() - sidePad * 2f)
                            .height(auxLineHeight)));
                }
            }
            return body;
        }

        private UIElement presenceStrip(
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
                SlotWorkspaceViewModel.ClaimedChestTile tile = viewModel.claimedChestTile(targetStorageId);
                if (tile != null) {
                    event.stopPropagation();
                    panToChestTile(atlas, tile);
                    localStatus = "panned to " + tile.label();
                    rebuild();
                }
            });
            return band;
        }

        private UIElement atlasBodyContainer() {
            UIElement body = new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));
            body.setAllowHitTest(false);
            return body;
        }

        private UIElement slotPreview(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget
        ) {
            return slotPreview(atlas, item, budget.shellPx(), budget.iconPx());
        }

        private UIElement slotPreview(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                float shellPx,
                float iconPx
        ) {
            float cardBound = Math.min(item.width(), item.height());
            float shell = Math.min(cardBound, atlas.worldUnitsForPixels(shellPx));
            float inset = Math.min(shell * 0.5f, atlas.worldUnitsForPixels(1f));
            float icon = Math.max(0f, Math.min(shell - inset * 2f, atlas.worldUnitsForPixels(iconPx)));
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
            return shellElement;
        }

        private UIElement slotPreview(SlotWorkspaceViewModel.AtlasItem item, int size, boolean showMarker) {
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
                shellElement.addChild(panel(itemMarkerColor(item)).layout(layout -> layout
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
            // Corner pip intentionally omitted — see docs/plans/current.md
            // "newness indicators (`+N` delta since last open)" for the
            // tracking slice that will re-introduce it with correct semantics.
            if (searchMatch) {
                body.addChild(panel(ACCENT).layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(atlas.worldUnitsForPixels(2f))
                        .bottom(atlas.worldUnitsForPixels(1f))
                        .width(item.width() - atlas.worldUnitsForPixels(4f))
                        .height(atlas.worldUnitsForPixels(2f))));
            }
        }

        private UIElement anchorTextBand(
                SlotAtlasGraphView atlas,
                String text,
                int color,
                float fontPx,
                int maxLength,
                int lines,
                int backgroundColor,
                Horizontal align
        ) {
            UIElement band = panel(backgroundColor).layout(layout -> layout
                    .paddingHorizontal(atlas.worldUnitsForPixels(1f))
                    .paddingVertical(atlas.worldUnitsForPixels(1f))
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            band.setAllowHitTest(false);
            String displayText = compactAnchorText(text, maxLength);
            Label token = anchorLabel(displayText, color, atlas.worldUnitsForPixels(fontPx));
            token.layout(layout -> layout.widthPercent(100).heightPercent(100));
            token.textStyle(style -> style
                    .fontSize(atlas.worldUnitsForPixels(fontPx))
                    .lineSpacing(lines > 1 ? atlas.worldUnitsForPixels(1f) : 0f)
                    .textWrap(lines > 1 ? TextWrap.WRAP : TextWrap.HIDE)
                    .textAlignVertical(lines > 1 ? Vertical.TOP : Vertical.CENTER)
                    .textAlignHorizontal(align));
            band.addChild(token);
            return band;
        }

        private String preferredPrimaryLabel(SlotWorkspaceViewModel.AtlasItem item, AtlasRenderBudget budget) {
            return compactAnchorText(item == null ? "" : item.name(), budget.primaryMaxChars());
        }

        private String preferredSecondaryLabel(SlotWorkspaceViewModel.AtlasItem item, AtlasRenderBudget budget) {
            String variant = tooltipVariantToken(item, budget.secondaryMaxChars());
            String mod = modToken(item, budget.secondaryMaxChars());
            String primary = preferredPrimaryLabel(item, budget);
            if (!variant.isBlank() && !normalizeTooltipText(variant).equals(normalizeTooltipText(primary))) {
                return variant;
            }
            if (item.recent()) {
                return "new";
            }
            if (!mod.isBlank()) {
                return mod;
            }
            return "";
        }

        private String preferredAuxiliaryLabel(SlotWorkspaceViewModel.AtlasItem item, AtlasRenderBudget budget) {
            String secondary = preferredSecondaryLabel(item, budget);
            String mod = modToken(item, budget.secondaryMaxChars());
            if (!mod.isBlank() && !normalizeTooltipText(mod).equals(normalizeTooltipText(secondary))) {
                return mod;
            }
            return "";
        }

        private String modToken(SlotWorkspaceViewModel.AtlasItem item, int maxLength) {
            if (item == null) {
                return "";
            }
            String namespace = namespace(item.identity().itemId());
            if (namespace.isBlank() || "minecraft".equals(namespace)) {
                return "";
            }
            return compactItemLabel(namespace.replace('_', ' ').replace('-', ' '), maxLength);
        }

        private String tooltipVariantToken(SlotWorkspaceViewModel.AtlasItem item, int maxLength) {
            List<Component> tooltipLines = atlasTooltipLines(item);
            String name = item == null ? "" : item.name();
            String namespace = namespace(item == null ? "" : item.identity().itemId());
            for (Component line : tooltipLines) {
                if (line == null) {
                    continue;
                }
                String normalized = normalizeTooltipText(line.getString());
                if (normalized.isBlank()) {
                    continue;
                }
                if (normalizeTooltipText(name).equals(normalized)) {
                    continue;
                }
                if (isGenericTooltipToken(normalized, namespace)) {
                    continue;
                }
                return compactAnchorText(normalized, maxLength);
            }
            return "";
        }

        private String namespace(String itemId) {
            if (itemId == null) {
                return "";
            }
            int separator = itemId.indexOf(':');
            return separator < 0 ? "" : itemId.substring(0, separator);
        }

        private String normalizeTooltipText(String text) {
            if (text == null) {
                return "";
            }
            return text
                    .replace('\n', ' ')
                    .replaceAll("\\s+", " ")
                    .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit} ]", "")
                    .trim()
                    .toLowerCase(Locale.ROOT);
        }

        private boolean isGenericTooltipToken(String normalizedText, String namespace) {
            if (normalizedText.isBlank()) {
                return true;
            }
            String normalizedNamespace = normalizeTooltipText(namespace == null ? "" : namespace.replace('_', ' '));
            if (!normalizedNamespace.isBlank() && normalizedText.equals(normalizedNamespace)) {
                return true;
            }
            return normalizedText.startsWith("hold ")
                    || normalizedText.startsWith("press ")
                    || normalizedText.startsWith("when ")
                    || normalizedText.contains(" shift")
                    || normalizedText.contains(" ctrl")
                    || normalizedText.contains("details")
                    || normalizedText.startsWith("durability")
                    || normalizedText.startsWith("emc ")
                    || normalizedText.startsWith("energy ")
                    || normalizedText.startsWith("burn time");
        }

        private String compactAnchorText(String text, int maxLength) {
            if (text == null) {
                return "";
            }
            String normalized = text.replace('\n', ' ').replaceAll("\\s+", " ").trim();
            if (normalized.isBlank()) {
                return "";
            }
            return shorten(normalized, maxLength);
        }

        private String compactItemLabel(String text, int maxLength) {
            if (text == null) {
                return "";
            }
            String normalized = text.replace('\n', ' ').replaceAll("\\s+", " ").trim();
            if (normalized.isBlank()) {
                return "";
            }
            String[] words = normalized.split(" ");
            if (words.length >= 2) {
                StringBuilder builder = new StringBuilder();
                for (int index = 0; index < Math.min(2, words.length); index++) {
                    String word = words[index];
                    int remaining = maxLength - builder.length() - (builder.isEmpty() ? 0 : 1);
                    if (remaining <= 0) {
                        break;
                    }
                    String piece = truncateWord(word, remaining);
                    if (piece.isBlank()) {
                        continue;
                    }
                    if (!builder.isEmpty()) {
                        builder.append(' ');
                    }
                    builder.append(piece);
                }
                if (!builder.isEmpty()) {
                    return builder.toString();
                }
            }
            return shorten(normalized, maxLength);
        }

        private String truncateWord(String word, int maxLength) {
            if (word == null || word.isBlank() || maxLength <= 0) {
                return "";
            }
            return word.length() <= maxLength ? word : word.substring(0, maxLength);
        }

        private int itemMarkerColor(SlotWorkspaceViewModel.AtlasItem item) {
            if (item.recent()) {
                return WARNING;
            }
            if (item.playerPlaced()) {
                return ACCENT;
            }
            SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(item.islandId());
            return island != null && island.kind() != VisualAtlasIslandKind.TRIAGE
                    ? 0xFF94D8B8
                    : MUTED;
        }

        private String compactCount(int count) {
            if (count <= 0) {
                return "0";
            }
            if (count > 99) {
                return "99+";
            }
            return Integer.toString(count);
        }

        private SlotWorkspaceViewModel.AtlasItem hoveredAtlasItem() {
            return viewModel.atlasItem(hoveredAtlasIdentity);
        }

        private SlotWorkspaceViewModel.AtlasItem selectedAtlasItem() {
            return viewModel.atlasItem(selectedAtlasIdentity);
        }

        private SlotWorkspaceViewModel.AtlasItem focusedAtlasItem() {
            SlotWorkspaceViewModel.AtlasItem selected = selectedAtlasItem();
            return selected != null ? selected : hoveredAtlasItem();
        }

        private SlotWorkspaceViewModel.IdentityRef currentMapFocusIdentity() {
            if (hoveredAtlasIdentity != null && viewModel.atlasItem(hoveredAtlasIdentity) != null) {
                return hoveredAtlasIdentity;
            }
            return selectedAtlasIdentity;
        }

        private boolean isMapFocusItem(SlotWorkspaceViewModel.AtlasItem item) {
            SlotWorkspaceViewModel.IdentityRef focusIdentity = currentMapFocusIdentity();
            return item != null && focusIdentity != null && item.identity().equals(focusIdentity);
        }

        private SlotWorkspaceViewModel.HotbarSlot selectedHotbarSlot() {
            if (selectedHotbarIndex < 0 || selectedHotbarIndex >= viewModel.hotbarSlots().size()) {
                return null;
            }
            SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(selectedHotbarIndex);
            return slot.occupied() ? slot : null;
        }

        private String selectionLabel() {
            SlotWorkspaceViewModel.AtlasItem atlasItem = selectedAtlasItem();
            if (atlasItem != null) {
                return atlasItem.name();
            }
            SlotWorkspaceViewModel.HotbarSlot hotbar = selectedHotbarSlot();
            if (hotbar != null) {
                return "hotbar " + (hotbar.hotbarIndex() + 1);
            }
            return "none";
        }

        private boolean matchesSearch(SlotWorkspaceViewModel.AtlasItem item) {
            String query = normalizedSearchQuery();
            if (query.isBlank()) {
                return true;
            }
            StringBuilder searchable = new StringBuilder();
            searchable.append(item.name().toLowerCase(Locale.ROOT)).append(' ')
                    .append(item.identity().itemId().toLowerCase(Locale.ROOT)).append(' ');
            SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(item.islandId());
            if (island != null) {
                searchable.append(island.label().toLowerCase(Locale.ROOT)).append(' ');
                searchable.append(island.kind().name().toLowerCase(Locale.ROOT)).append(' ');
            }
            return searchable.toString().contains(query);
        }

        private String searchSummary() {
            String query = normalizedSearchQuery();
            if (query.isBlank()) {
                return "Drag to pan. Drag anchors between atlas and belt. Drag island titles to move.";
            }
            long matches = viewModel.atlasItems().stream().filter(this::matchesSearch).count();
            return matches + " match" + (matches == 1 ? "" : "es")
                    + " for \"" + searchQuery.trim() + "\".";
        }

        private String normalizedSearchQuery() {
            return searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
        }

        private String islandSubtitle(SlotWorkspaceViewModel.AtlasIsland island) {
            String count = island.itemCount() + " item" + (island.itemCount() == 1 ? "" : "s");
            String carriedBadge = island.carriedCount() > 0 ? "  ·  " + island.carriedCount() + " carried" : "";
            return switch (island.kind()) {
                case TRIAGE -> count + " awaiting placement" + carriedBadge;
                case PLAYER -> count + " player-authored homes" + carriedBadge;
            };
        }

        private String itemMarker(SlotWorkspaceViewModel.AtlasItem item) {
            if (item.recent()) {
                return "new";
            }
            SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(item.islandId());
            if (item.playerPlaced()) {
                return "set";
            }
            if (island != null && island.kind() != VisualAtlasIslandKind.TRIAGE) {
                return "auto";
            }
            return "inbox";
        }

        private String selectionHomeStatus(SlotWorkspaceViewModel.AtlasItem item) {
            SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(item.islandId());
            if (item.playerPlaced()) {
                return "player-placed home";
            }
            if (island != null && island.kind() != VisualAtlasIslandKind.TRIAGE) {
                return "starter home";
            }
            return "awaiting placement";
        }

        private int cardChromeColor(DisclosureLevel level, boolean selected, boolean searchMatch, boolean recent) {
            return cardChromeColor(level, selected, searchMatch, recent, true);
        }

        private int cardChromeColor(
                DisclosureLevel level,
                boolean selected,
                boolean searchMatch,
                boolean recent,
                boolean carried
        ) {
            int base = cardChromeBaseColor(level, selected, searchMatch, recent);
            if (!carried && !selected) {
                base = dimAlpha(base, GHOST_CARD_ALPHA);
            }
            return base;
        }

        private int cardChromeBaseColor(DisclosureLevel level, boolean selected, boolean searchMatch, boolean recent) {
            if (level == DisclosureLevel.REGION) {
                if (selected) {
                    return 0x68435F55;
                }
                if (normalizedSearchQuery().isBlank()) {
                    return recent ? 0x242B433A : 0x1410161B;
                }
                return searchMatch ? (recent ? 0x342B433A : 0x241A242C) : 0x05000000;
            }
            if (selected) {
                return SELECTED;
            }
            if (normalizedSearchQuery().isBlank()) {
                return recent ? ROW_MATCH : 0xC926313B;
            }
            return searchMatch ? (recent ? ROW_MATCH : ROW_HOVER) : 0x2824313D;
        }

        private static int dimAlpha(int color, float alphaFactor) {
            int alpha = (color >>> 24) & 0xFF;
            int dimmed = Math.round(alpha * alphaFactor);
            return (dimmed << 24) | (color & 0x00FFFFFF);
        }

        private String itemName(ItemStack stack) {
            if (stack == null || stack.isEmpty()) {
                return "empty";
            }
            String name = stack.getHoverName().getString();
            return name.isBlank() ? "unknown" : name;
        }

        private List<Component> atlasTooltipLines(SlotWorkspaceViewModel.AtlasItem item) {
            if (item == null || item.displayStack().isEmpty()) {
                return List.of();
            }
            return List.copyOf(DrawerHelper.getItemToolTip(item.displayStack()));
        }

        private void appendTooltipPreview(List<UIElement> children, SlotWorkspaceViewModel.AtlasItem item) {
            List<Component> tooltipLines = atlasTooltipLines(item);
            if (tooltipLines.isEmpty()) {
                return;
            }
            ArrayList<String> preview = new ArrayList<>();
            String itemName = item.name();
            for (Component line : tooltipLines) {
                if (line == null) {
                    continue;
                }
                String text = line.getString();
                if (text == null || text.isBlank()) {
                    continue;
                }
                if (preview.isEmpty() && text.equals(itemName)) {
                    continue;
                }
                preview.add(text);
            }
            if (preview.isEmpty()) {
                return;
            }
            children.add(label("Tooltip Preview", ACCENT).layout(layout -> layout.height(12)));
            int previewCount = Math.min(5, preview.size());
            for (int index = 0; index < previewCount; index++) {
                children.add(wrappedLabel(preview.get(index), MUTED));
            }
            if (preview.size() > previewCount) {
                children.add(wrappedLabel("(+" + (preview.size() - previewCount) + " more lines)", MUTED));
            }
        }

        private UIElement itemIcon(ItemStack stack, float size) {
            return itemIcon(stack, size, true);
        }

        private UIElement itemIcon(ItemStack stack, float size, boolean carried) {
            ItemStack iconStack = stack == null ? ItemStack.EMPTY : stack.copy();
            ItemStackTexture texture = new ItemStackTexture(iconStack);
            UIElement icon = new UIElement().layout(layout -> layout.width(size).height(size))
                    .style(style -> {
                        style.backgroundTexture(texture);
                        if (!carried) {
                            style.overlayTexture(rect(GHOST_ICON_OVERLAY_COLOR));
                        }
                    });
            icon.setAllowHitTest(false);
            return icon;
        }

        private UIElement emptyIcon() {
            UIElement icon = panel(0x80323B44).layout(layout -> layout.width(16).height(16));
            icon.setAllowHitTest(false);
            return icon;
        }

        private Label anchorLabel(String text, int color, float fontSize) {
            Label label = label(text, color);
            label.textStyle(style -> style
                    .fontSize(fontSize)
                    .textAlignVertical(Vertical.CENTER)
                    .textAlignHorizontal(Horizontal.LEFT));
            return label;
        }

        private float centeredWorld(float container, float child) {
            return Math.max(0f, (container - child) / 2f);
        }

        private void clearSelectionOnDirectClick(UIElement element) {
            element.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.target == element && (selectedAtlasIdentity != null || selectedHotbarIndex >= 0)) {
                    selectedAtlasIdentity = null;
                    selectedHotbarIndex = -1;
                    localStatus = "selection cleared";
                    rebuild();
                }
            });
        }
    }

    private record AtlasItemDrag(
            SlotWorkspaceViewModel.IdentityRef identity,
            ItemStack displayStack,
            String originIslandId
    ) {
    }

    private record HotbarSlotDrag(
            int hotbarIndex,
            ItemStack displayStack
    ) {
    }

    private record IslandDrag(
            String islandId,
            int grabOffsetX,
            int grabOffsetY
    ) {
    }

    private record ChestTileDrag(
            String storageId,
            int grabOffsetX,
            int grabOffsetY
    ) {
    }

    private record ChestStackDrag(
            String storageId,
            int chestSlotIndex,
            ItemStack displayStack
    ) {
    }

    private record StorageZoneBounds(int left, int top, int width, int height) {
    }

    private record StorageZoneDrag(
            int grabOffsetX,
            int grabOffsetY,
            int originX,
            int originY
    ) {
    }

    private static String shorten(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private static UIElement panel(int color) {
        return new UIElement().style(style -> style.backgroundTexture(rect(color)));
    }

    private static Button button(String text, boolean active) {
        return button(text, active, active ? ROW : PANEL_ALT);
    }

    private static Button button(String text, boolean active, int color) {
        Button button = new Button();
        button.setText(Component.literal(text));
        button.setActive(active);
        applyButtonColors(button, active, color);
        return button;
    }

    private static void applyButtonColors(Button button, boolean active, int color) {
        button.buttonStyle(style -> {
            style.baseTexture(rect(color));
            style.hoverTexture(rect(active ? hoverColor(color) : color));
            style.pressedTexture(rect(active ? SELECTED : color));
        });
        button.textStyle(style -> style.textColor(active ? TEXT : MUTED).textShadow(false).fontSize(8));
    }

    private static int hoverColor(int color) {
        if (color == ROW_DIM) {
            return ROW;
        }
        return ROW_HOVER;
    }

    private static Label label(String text, int color) {
        Label label = new Label();
        label.setText(Component.literal(text == null ? "" : text));
        label.textStyle(style -> style
                .textColor(color)
                .fontSize(8)
                .textShadow(false)
                .textAlignVertical(Vertical.CENTER)
                .textAlignHorizontal(Horizontal.LEFT));
        label.setAllowHitTest(false);
        return label;
    }

    private static Label wrappedLabel(String text, int color) {
        Label label = label(text, color);
        label.layout(layout -> layout.widthPercent(100));
        label.textStyle(style -> style.textWrap(TextWrap.WRAP).textAlignVertical(Vertical.TOP));
        return label;
    }

    private static ColorRectTexture rect(int color) {
        return new ColorRectTexture(color);
    }

    private static final class SlotAtlasGraphView extends GraphView {
        private Consumer<AtlasCamera> cameraListener = camera -> {
        };

        private void onCameraChanged(Consumer<AtlasCamera> listener) {
            cameraListener = listener == null ? camera -> {
            } : listener;
        }

        private void captureCamera() {
            cameraListener.accept(new AtlasCamera(getOffsetX(), getOffsetY(), getScale()));
        }

        private void restoreCamera(AtlasCamera camera) {
            if (camera == null || getContentWidth() <= 0 || getContentHeight() <= 0) {
                return;
            }
            float viewWidth = getContentWidth() / camera.scale();
            float viewHeight = getContentHeight() / camera.scale();
            fit(
                    camera.offsetX(),
                    camera.offsetY(),
                    camera.offsetX() + viewWidth,
                    camera.offsetY() + viewHeight,
                    camera.scale()
            );
            captureCamera();
        }

        private void resetToOverview() {
            fitToChildren(72f, 0.45f);
            captureCamera();
        }

        private boolean beginViewportPan(UIEvent event) {
            if (event == null || !getGraphViewStyle().allowPan()) {
                return false;
            }
            if (event.button != 0 && event.button != 2) {
                return false;
            }
            if (!isSelfOrChildHover() || !isMouseOverContent(event.x, event.y)) {
                return false;
            }
            startDrag(new DragOffset(getOffsetX(), getOffsetY()), null);
            return true;
        }

        private int worldX(float screenX) {
            return Math.round(getOffsetX() + (screenX - getContentX()) / Math.max(0.0001f, getScale()));
        }

        private int worldY(float screenY) {
            return Math.round(getOffsetY() + (screenY - getContentY()) / Math.max(0.0001f, getScale()));
        }

        private float screenX(float worldX) {
            return (worldX - getOffsetX()) * getScale() + getContentX();
        }

        private float screenY(float worldY) {
            return (worldY - getOffsetY()) * getScale() + getContentY();
        }

        private float worldUnitsForPixels(float pixels) {
            return pixels / Math.max(0.0001f, getScale());
        }

        private int screenPixelsForWorldUnits(float worldUnits) {
            return Math.round(worldUnits * getScale());
        }

        @Override
        protected void onMouseWheel(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event) {
            UIElement target = event.target;
            if (target != this && isSelfOrChildHover()) {
                event.target = this;
            }
            super.onMouseWheel(event);
            event.target = target;
            captureCamera();
        }

        @Override
        protected void onDragSourceUpdate(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event) {
            super.onDragSourceUpdate(event);
            captureCamera();
        }
    }

    private record AtlasCamera(
            float offsetX,
            float offsetY,
            float scale
    ) {
    }

    private record IslandRenderBudget(
            float titleFontPx,
            float subtitleFontPx,
            float headerHeightPx,
            float subtitleHeightPx,
            float ruleHeightPx,
            boolean showSubtitle
    ) {
        private static IslandRenderBudget forScreenBudget(int islandScreenWidthPx) {
            int clamped = Math.max(1, islandScreenWidthPx);
            float titleFont = Math.max(8.5f, Math.min(12.5f, clamped * 0.026f));
            float subtitleFont = Math.max(6.5f, Math.min(8.5f, clamped * 0.018f));
            boolean showSubtitle = clamped >= 220;
            return new IslandRenderBudget(
                    titleFont,
                    subtitleFont,
                    titleFont + 5.5f,
                    subtitleFont + 3.5f,
                    showSubtitle ? 1.5f : 1f,
                    showSubtitle
            );
        }
    }

    private record AtlasRenderBudget(
            DisclosureLevel level,
            int cellBudgetPx,
            float shellPx,
            float iconPx,
            float pipPx,
            float primaryFontPx,
            float secondaryFontPx,
            float primaryLineHeightPx,
            float secondaryLineHeightPx,
            int primaryMaxChars,
            int secondaryMaxChars
    ) {
        private static AtlasRenderBudget forScreenBudget(int cellBudgetPx) {
            int clamped = Math.max(1, cellBudgetPx);
            if (clamped >= DETAIL_CELL_PX) {
                return new AtlasRenderBudget(
                        DisclosureLevel.DETAIL,
                        clamped,
                        clamp(clamped * 0.42f, 30f, 54f),
                        clamp(clamped * 0.36f, 24f, 48f),
                        4f,
                        clamp(clamped * 0.064f, 6.25f, 7.25f),
                        clamp(clamped * 0.051f, 5.5f, 6.25f),
                        clamp(clamped * 0.083f, 9.0f, 11.0f),
                        clamp(clamped * 0.062f, 7.0f, 8.5f),
                        38,
                        26
                );
            }
            if (clamped >= INSPECT_CELL_PX) {
                return new AtlasRenderBudget(
                        DisclosureLevel.INSPECT,
                        clamped,
                        clamp(clamped * 0.40f, 26f, 44f),
                        clamp(clamped * 0.34f, 20f, 38f),
                        4f,
                        clamp(clamped * 0.060f, 6.0f, 7.0f),
                        clamp(clamped * 0.048f, 5.25f, 6.0f),
                        clamp(clamped * 0.077f, 8.25f, 10.0f),
                        clamp(clamped * 0.058f, 6.75f, 8.0f),
                        32,
                        18
                );
            }
            if (clamped >= READ_CELL_PX) {
                return new AtlasRenderBudget(
                        DisclosureLevel.READ,
                        clamped,
                        clamp(clamped * 0.46f, 24f, 36f),
                        clamp(clamped * 0.40f, 18f, 30f),
                        4f,
                        clamp(clamped * 0.056f, 5.5f, 6.25f),
                        0f,
                        clamp(clamped * 0.074f, 8.0f, 9.5f),
                        0f,
                        28,
                        0
                );
            }
            if (clamped >= BROWSE_CELL_PX) {
                return new AtlasRenderBudget(
                        DisclosureLevel.BROWSE,
                        clamped,
                        clamp(clamped - 4f, 22f, 46f),
                        clamp(clamped - 8f, 18f, 40f),
                        4f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0,
                        0
                );
            }
            return new AtlasRenderBudget(
                    DisclosureLevel.REGION,
                    clamped,
                    clamp(clamped - 2f, 16f, 34f),
                    clamp(clamped - 6f, 12f, 28f),
                    3f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0,
                    0
            );
        }

        private static float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }
    }

    private enum DisclosureLevel {
        REGION,
        BROWSE,
        READ,
        INSPECT,
        DETAIL;

        static DisclosureLevel fromScreenBudget(int cellBudgetPx) {
            return AtlasRenderBudget.forScreenBudget(cellBudgetPx).level();
        }

        boolean atLeast(DisclosureLevel minimum) {
            return ordinal() >= minimum.ordinal();
        }
    }
}
