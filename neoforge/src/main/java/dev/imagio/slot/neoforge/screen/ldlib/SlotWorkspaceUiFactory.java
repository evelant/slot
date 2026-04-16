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
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
    private static final int BROWSE_CELL_PX = 32;
    private static final int READ_CELL_PX = 58;
    private static final int INSPECT_CELL_PX = 94;
    private static final int DETAIL_CELL_PX = 124;

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
        private String collectionDraft = "";
        private SlotWorkspaceViewModel.IdentityRef selectedAtlasIdentity;
        private SlotWorkspaceViewModel.IdentityRef hoveredAtlasIdentity;
        private int selectedHotbarIndex = -1;

        private RPCEmitter transferEmitter;
        private RPCEmitter homeEmitter;
        private RPCEmitter createIslandEmitter;
        private RPCEmitter toggleCollectionEmitter;
        private RPCEmitter createCollectionEmitter;
        private RPCEmitter hotbarToAtlasEmitter;
        private RPCEmitter moveIslandEmitter;
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
            createIslandEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    session::createIslandForItem
            ));
            toggleCollectionEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    session::toggleCollectionMembership
            ));
            createCollectionEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    session::createCollection
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
            content.clearAllChildren();
            content.addChildren(
                    header(),
                    body(),
                    beltPanel(),
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
            header.addChildren(
                    label("SLOT Atlas", ACCENT).layout(layout -> layout.flex(1).height(12)),
                    label("Persistent carried inventory", MUTED).layout(layout -> layout.height(12))
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
            body.addChildren(atlasPanel(), inspectorPanel());
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
                    .minScale(0.30f)
                    .maxScale(4.50f)
                    .gridTexture(IGuiTexture.EMPTY)
                    .gridSize(48));
            atlas.addEventListener(UIEvents.LAYOUT_CHANGED, event -> {
                if (atlasCamera == null) {
                    atlas.fitToChildren(72f, 0.45f);
                    atlas.captureCamera();
                } else {
                    atlas.restoreCamera(atlasCamera);
                }
            });
            installAtlasCanvasDropTarget(panel, atlas);
            installAtlasBackgroundDropTarget(atlas);
            buildAtlas(atlas);
            panel.addChildren(atlas, navigationCapsule(atlas));
            return panel;
        }

        private void buildAtlas(SlotAtlasGraphView atlas) {
            for (SlotWorkspaceViewModel.AtlasIsland island : viewModel.islands()) {
                atlas.addContentChild(islandPanel(atlas, island));
            }
            for (SlotWorkspaceViewModel.AtlasItem item : viewModel.atlasItems()) {
                atlas.addContentChild(atlasCardButton(atlas, item));
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
            header.setOnClick(event -> {
                event.stopPropagation();
                if (selectedAtlasItem() == null) {
                    localStatus = "select a triage or homed item first";
                    rebuild();
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

            IslandRenderBudget[] lastBudget = new IslandRenderBudget[1];
            panel.addEventListener(UIEvents.TICK, event -> {
                IslandRenderBudget budget = IslandRenderBudget.forScreenBudget(
                        Math.max(1, atlas.screenPixelsForWorldUnits(island.width()))
                );
                if (!budget.equals(lastBudget[0])) {
                    header.layout(layout -> layout.widthPercent(100).height(atlas.worldUnitsForPixels(budget.headerHeightPx())));
                    header.textStyle(style -> style
                            .textColor(TEXT)
                            .textShadow(false)
                            .fontSize(atlas.worldUnitsForPixels(budget.titleFontPx()))
                            .textAlignHorizontal(Horizontal.CENTER)
                            .textAlignVertical(Vertical.CENTER));
                    subtitle.layout(layout -> layout.widthPercent(100).height(atlas.worldUnitsForPixels(budget.subtitleHeightPx())));
                    subtitle.textStyle(style -> style
                            .textColor(MUTED)
                            .textShadow(false)
                            .fontSize(atlas.worldUnitsForPixels(budget.subtitleFontPx()))
                            .textAlignHorizontal(Horizontal.LEFT)
                            .textAlignVertical(Vertical.CENTER));
                    line.layout(layout -> layout.widthPercent(100).height(atlas.worldUnitsForPixels(budget.ruleHeightPx())));
                    lastBudget[0] = budget;
                }
                subtitle.setDisplay(budget.showSubtitle());
                line.setDisplay(budget.showSubtitle());
                subtitle.setText(Component.literal(islandSubtitle(island)));
                panel.markTaffyStyleDirty();
            });
            return panel;
        }

        private Button atlasCardButton(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasItem item) {
            boolean selected = item.identity().equals(selectedAtlasIdentity);
            boolean searchMatch = matchesSearch(item);
            boolean activeSearchMatch = !normalizedSearchQuery().isBlank() && searchMatch;
            AtlasRenderBudget initialBudget = atlasBudget(atlas, item);
            Button button = button("", true, cardChromeColor(initialBudget.level(), selected, searchMatch, item.recent()));
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
                applyButtonColors(button, true, cardChromeColor(budget.level(), currentSelected, searchMatch, item.recent()));
            });
            return button;
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

            Label summary = label(searchSummary(), MUTED);
            summary.layout(layout -> layout.widthPercent(100).height(14));
            summary.addEventListener(UIEvents.TICK, event -> summary.setText(Component.literal(searchSummary())));

            capsule.addChildren(topRow, summary);
            return capsule;
        }

        private Button homeButton(SlotAtlasGraphView atlas) {
            Button button = button("Home", true, PANEL_ALT);
            button.layout(layout -> layout.width(52).height(22));
            button.setOnClick(event -> {
                event.stopPropagation();
                atlas.resetToOverview();
                localStatus = "camera reset";
                rebuild();
            });
            return button;
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

        private UIElement inspectorPanel() {
            UIElement panel = panel(PANEL).layout(layout -> layout
                    .width(284)
                    .heightPercent(100)
                    .paddingAll(8)
                    .gapAll(6)
                    .flexDirection(FlexDirection.COLUMN));
            clearSelectionOnDirectClick(panel);
            panel.addChildren(
                    selectionPanel(),
                    atlasActionsPanel(),
                    collectionsPanel()
            );
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
                children.add(wrappedLabel("collections: " + collectionSummary(atlasItem), MUTED));
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

        private UIElement atlasActionsPanel() {
            UIElement panel = panel(PANEL_ALT).layout(layout -> layout
                    .widthPercent(100)
                    .paddingAll(6)
                    .gapAll(4)
                    .flexDirection(FlexDirection.COLUMN));
            SlotWorkspaceViewModel.AtlasItem atlasItem = selectedAtlasItem();
            panel.addChildren(label("Atlas Actions", ACCENT).layout(layout -> layout.height(12)));
            Button createButton = button(
                    atlasItem == null ? "Select an item to create island" : "Create island from selected item",
                    true
            );
            createButton.layout(layout -> layout.widthPercent(100).height(22));
            createButton.setOnClick(event -> {
                event.stopPropagation();
                sendCreateIsland();
            });
            installCreateIslandDropTarget(createButton);
            panel.addChildren(createButton);
            panel.addChildren(wrappedLabel("Drag an atlas card onto an island to place it. Drop onto this action to forge a new island.", MUTED));
            return panel;
        }

        private UIElement collectionsPanel() {
            UIElement panel = panel(PANEL_ALT).layout(layout -> layout
                    .widthPercent(100)
                    .paddingAll(6)
                    .gapAll(4)
                    .flexDirection(FlexDirection.COLUMN));
            panel.addChildren(label("Collections", ACCENT).layout(layout -> layout.height(12)));

            TextField input = new TextField();
            input.setAnyString();
            input.setText(collectionDraft, false);
            input.layout(layout -> layout.widthPercent(100).height(20));
            input.style(style -> style.backgroundTexture(rect(0xC60D1318)));
            input.textFieldStyle(style -> style
                    .placeholder(Component.literal("New collection"))
                    .textColor(TEXT)
                    .cursorColor(ACCENT)
                    .textShadow(false)
                    .fontSize(10));
            input.setTextResponder(value -> collectionDraft = value == null ? "" : value);
            panel.addChild(input);

            Button create = button("Create collection", !collectionDraft.isBlank());
            create.layout(layout -> layout.widthPercent(100).height(20));
            create.setOnClick(event -> {
                event.stopPropagation();
                if (collectionDraft.isBlank()) {
                    localStatus = "enter a collection name first";
                    rebuild();
                    return;
                }
                boolean sent = createCollectionEmitter != null && createCollectionEmitter.send(collectionDraft.trim());
                localStatus = sent ? "collection create requested" : "collection create unavailable";
                collectionDraft = "";
                rebuild();
            });
            panel.addChild(create);

            viewModel.collections().stream()
                    .sorted(Comparator.comparing(entry -> entry.label().toLowerCase(Locale.ROOT)))
                    .forEach(collection -> panel.addChild(collectionToggleButton(collection)));
            return panel;
        }

        private Button collectionToggleButton(SlotWorkspaceViewModel.CollectionEntry collection) {
            SlotWorkspaceViewModel.AtlasItem atlasItem = selectedAtlasItem();
            boolean active = atlasItem != null;
            boolean member = active && atlasItem.collectionIds().contains(collection.collectionId());
            Button button = button((member ? "[x] " : "[ ] ") + shorten(collection.label(), 18) + " (" + collection.memberCount() + ")", active, member ? SELECTED : ROW);
            button.layout(layout -> layout.widthPercent(100).height(20));
            button.setOnClick(event -> {
                event.stopPropagation();
                if (selectedAtlasItem() == null) {
                    localStatus = "select an atlas item first";
                    rebuild();
                    return;
                }
                sendToggleCollection(collection.collectionId());
            });
            return button;
        }

        private UIElement beltPanel() {
            UIElement panel = panel(PANEL).layout(layout -> layout
                    .widthPercent(100)
                    .height(44)
                    .paddingAll(4)
                    .gapAll(4)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            clearSelectionOnDirectClick(panel);
            panel.addChild(beltSpacer());
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

        private UIElement beltSpacer() {
            UIElement spacer = new UIElement().layout(layout -> layout.flex(1).height(1));
            spacer.setAllowHitTest(false);
            return spacer;
        }

        private UIElement beltDivider() {
            UIElement divider = panel(ISLAND_BORDER).layout(layout -> layout.width(1).height(28));
            divider.setAllowHitTest(false);
            return divider;
        }

        private Button beltSlotButton(SlotWorkspaceViewModel.HotbarSlot slot) {
            boolean selected = selectedHotbarIndex == slot.hotbarIndex();
            int color = selected ? SELECTED : slot.selected() ? ACTIVE_HOTBAR : ROW;
            Button button = button("", true, color);
            button.layout(layout -> layout
                    .width(34)
                    .height(34)
                    .paddingAll(2)
                    .gapAll(1)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.COLUMN));
            button.noText();
            button.setOnClick(event -> {
                event.stopPropagation();
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

            button.addChildren(
                    label(Integer.toString(slot.hotbarIndex() + 1), slot.selected() ? WARNING : MUTED)
                            .layout(layout -> layout.widthPercent(100).height(7)),
                    slot.occupied() ? itemIcon(slot.displayStack(), 16) : emptyIcon(),
                    label(slot.occupied() ? "x" + compactCount(slot.count()) : "", ACCENT)
                            .layout(layout -> layout.widthPercent(100).height(7))
            );
            return button;
        }

        private UIElement offhandSlotButton(SlotWorkspaceViewModel.OffhandSlot offhand) {
            Button button = button("", false, ROW_DIM);
            button.layout(layout -> layout
                    .width(34)
                    .height(34)
                    .paddingAll(2)
                    .gapAll(1)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.COLUMN));
            button.noText();
            button.setActive(false);
            installOffhandHoverTooltip(button, offhand);
            button.addChildren(
                    label("off", MUTED).layout(layout -> layout.widthPercent(100).height(7)),
                    offhand.occupied() ? itemIcon(offhand.displayStack(), 16) : emptyIcon(),
                    label(offhand.occupied() ? "x" + compactCount(offhand.count()) : "", MUTED)
                            .layout(layout -> layout.widthPercent(100).height(7))
            );
            return button;
        }

        private Button equipmentToggleButton() {
            Button button = button("+", false, PANEL_ALT);
            button.layout(layout -> layout
                    .width(34)
                    .height(34)
                    .paddingAll(2)
                    .alignItems(AlignItems.CENTER));
            button.setActive(false);
            button.textStyle(style -> style.textColor(MUTED).textShadow(false).fontSize(10)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            return button;
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

        private void sendCreateIsland() {
            SlotWorkspaceViewModel.AtlasItem item = selectedAtlasItem();
            if (item == null) {
                localStatus = "select an atlas item first";
                rebuild();
                return;
            }
            sendCreateIsland(item.identity());
        }

        private void sendCreateIsland(SlotWorkspaceViewModel.IdentityRef identity) {
            if (identity == null) {
                localStatus = "select an atlas item first";
                rebuild();
                return;
            }
            boolean sent = createIslandEmitter != null && createIslandEmitter.send(
                    identity.itemId(),
                    identity.comparisonMode(),
                    identity.componentFingerprint()
            );
            localStatus = sent ? "create island requested" : "create island unavailable";
            selectedAtlasIdentity = null;
            selectedHotbarIndex = -1;
            rebuild();
        }

        private void sendToggleCollection(String collectionId) {
            SlotWorkspaceViewModel.AtlasItem item = selectedAtlasItem();
            if (item == null) {
                localStatus = "select an atlas item first";
                rebuild();
                return;
            }
            boolean sent = toggleCollectionEmitter != null && toggleCollectionEmitter.send(
                    item.identity().itemId(),
                    item.identity().comparisonMode(),
                    item.identity().componentFingerprint(),
                    collectionId
            );
            localStatus = sent ? "collection update requested" : "collection update unavailable";
            rebuild();
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
            localStatus = sent ? "island move requested" : "island move unavailable";
            rebuild();
        }

        private void installAtlasItemDragSource(UIElement source, SlotWorkspaceViewModel.AtlasItem item) {
            source.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                if (!source.isMouseDown(0) || isDragging(source)) {
                    return;
                }
                source.startDrag(
                        new AtlasItemDrag(item.identity(), item.displayStack().copy()),
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
            source.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                if (!source.isMouseDown(0) || isDragging(source)) {
                    return;
                }
                int grabOffsetX = Math.max(0, Math.min(island.width(), atlas.worldX(event.x) - island.x()));
                int grabOffsetY = Math.max(0, Math.min(island.height(), atlas.worldY(event.y) - island.y()));
                int actualWidthPx = Math.max(48, atlas.screenPixelsForWorldUnits(island.width()));
                int actualHeightPx = Math.max(24, atlas.screenPixelsForWorldUnits(island.height()));
                float dragScale = Math.min(1f, Math.min(260f / actualWidthPx, 180f / actualHeightPx));
                int dragWidthPx = Math.max(72, Math.round(actualWidthPx * dragScale));
                int dragHeightPx = Math.max(22, Math.round(actualHeightPx * dragScale));
                int dragOffsetX = Math.round(grabOffsetX * atlas.getScale() * dragScale);
                int dragOffsetY = Math.round(grabOffsetY * atlas.getScale() * dragScale);
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
                    sendAssignHome(
                            atlasItem.identity(),
                            SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                            atlas.worldX(event.x),
                            atlas.worldY(event.y)
                    );
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

        private void installAtlasCanvasDropTarget(UIElement target, SlotAtlasGraphView atlas) {
            target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                IslandDrag islandDrag = islandDrag(event);
                if (islandDrag == null) {
                    return;
                }
                if (event.target == atlas) {
                    return;
                }
                sendMoveIsland(
                        islandDrag.islandId(),
                        atlas.worldX(event.x) - islandDrag.grabOffsetX(),
                        atlas.worldY(event.y) - islandDrag.grabOffsetY()
                );
                event.stopPropagation();
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

        private void installCreateIslandDropTarget(Button target) {
            target.addEventListener(UIEvents.DRAG_ENTER, event -> updateGenericDropOverlay(target, atlasItemDrag(event) != null), true);
            target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateGenericDropOverlay(target, atlasItemDrag(event) != null));
            target.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(target), true);
            target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                clearDropOverlay(target);
                AtlasItemDrag drag = atlasItemDrag(event);
                if (drag != null) {
                    sendCreateIsland(drag.identity());
                    event.stopPropagation();
                }
            });
        }

        private void updateHotbarDropOverlay(Button target, SlotWorkspaceViewModel.HotbarSlot slot, UIEvent event) {
            updateGenericDropOverlay(target, atlasItemDrag(event) != null, slot.occupied() ? ACTIVE_HOTBAR : ACCENT);
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
            boolean acceptable = atlasItemDrag(event) != null || hotbarSlotDrag(event) != null || islandDrag != null;
            updateGenericDropOverlay(atlas, acceptable, islandDrag != null ? SELECTED : WARNING);
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
            float shell = atlas.worldUnitsForPixels(budget.shellPx());
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
            float shell = atlas.worldUnitsForPixels(budget.shellPx());
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
            }
            return body;
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
            float shell = atlas.worldUnitsForPixels(shellPx);
            float inset = atlas.worldUnitsForPixels(1f);
            float icon = atlas.worldUnitsForPixels(iconPx);
            UIElement shellElement = panel(0xB0141B23).layout(layout -> layout.width(shell).height(shell));
            shellElement.setAllowHitTest(false);
            shellElement.addChild(panel(0xD90A1218).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(inset)
                    .top(inset)
                    .width(shell - inset * 2f)
                    .height(shell - inset * 2f)));
            shellElement.addChild(itemIcon(item.displayStack(), icon).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(centeredWorld(shell, icon))
                    .top(centeredWorld(shell, icon))));
            return shellElement;
        }

        private UIElement slotPreview(SlotWorkspaceViewModel.AtlasItem item, int size, boolean showMarker) {
            float shell = size;
            float inset = 1f;
            float icon = Math.max(10f, size - 4f);
            UIElement shellElement = panel(0xB0141B23).layout(layout -> layout.width(shell).height(shell));
            shellElement.setAllowHitTest(false);
            shellElement.addChild(panel(0xD90A1218).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(inset)
                    .top(inset)
                    .width(shell - inset * 2f)
                    .height(shell - inset * 2f)));
            shellElement.addChild(itemIcon(item.displayStack(), icon).layout(layout -> layout
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

        private UIElement anchorStatePip(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget
        ) {
            UIElement marker = panel(itemMarkerColor(item)).layout(layout -> layout
                    .width(atlas.worldUnitsForPixels(budget.pipPx()))
                    .height(atlas.worldUnitsForPixels(budget.pipPx())));
            marker.setAllowHitTest(false);
            return marker;
        }

        private void addCommonAtlasSignals(
                UIElement body,
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
            if (item.collectionIds() != null && !item.collectionIds().isEmpty()) {
                body.addChild(panel(COLLECTION).layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(0)
                        .top(atlas.worldUnitsForPixels(1f))
                        .width(atlas.worldUnitsForPixels(budget.level().atLeast(DisclosureLevel.READ) ? 2f : 1f))
                        .height(item.height() - atlas.worldUnitsForPixels(2f))));
            }
            body.addChild(anchorStatePip(atlas, item, budget).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(atlas.worldUnitsForPixels(2f))
                    .top(atlas.worldUnitsForPixels(2f))
                    .width(atlas.worldUnitsForPixels(budget.pipPx()))
                    .height(atlas.worldUnitsForPixels(budget.pipPx()))));
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
            String collection = collectionToken(item, budget.secondaryMaxChars());
            String mod = modToken(item, budget.secondaryMaxChars());
            String primary = preferredPrimaryLabel(item, budget);
            if (!variant.isBlank() && !normalizeTooltipText(variant).equals(normalizeTooltipText(primary))) {
                return variant;
            }
            if (!collection.isBlank()) {
                return collection;
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
            String collection = collectionToken(item, budget.secondaryMaxChars());
            String mod = modToken(item, budget.secondaryMaxChars());
            if (!collection.isBlank() && !normalizeTooltipText(collection).equals(normalizeTooltipText(secondary))) {
                return collection;
            }
            if (!mod.isBlank() && !normalizeTooltipText(mod).equals(normalizeTooltipText(secondary))) {
                return mod;
            }
            return "";
        }

        private String collectionToken(SlotWorkspaceViewModel.AtlasItem item, int maxLength) {
            if (item == null || item.collectionIds().isEmpty()) {
                return "";
            }
            if (item.collectionIds().size() == 1) {
                return compactAnchorText(viewModel.collectionLabel(item.collectionIds().getFirst()), maxLength);
            }
            String first = viewModel.collectionLabel(item.collectionIds().getFirst());
            String suffix = "+" + (item.collectionIds().size() - 1);
            int baseLength = Math.max(1, maxLength - suffix.length());
            return compactAnchorText(first, baseLength) + suffix;
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

        private String collectionSummary(SlotWorkspaceViewModel.AtlasItem item) {
            if (item == null || item.collectionIds().isEmpty()) {
                return "none";
            }
            StringBuilder summary = new StringBuilder();
            for (String collectionId : item.collectionIds()) {
                if (summary.length() > 0) {
                    summary.append(", ");
                }
                summary.append(viewModel.collectionLabel(collectionId));
            }
            return summary.toString();
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
            for (String collectionId : item.collectionIds()) {
                searchable.append(viewModel.collectionLabel(collectionId).toLowerCase(Locale.ROOT)).append(' ');
            }
            return searchable.toString().contains(query);
        }

        private String searchSummary() {
            String query = normalizedSearchQuery();
            if (query.isBlank()) {
                return "Drag empty atlas or island surfaces to pan. Drag anchors between atlas and hotbar. Drag island titles to reposition them.";
            }
            long matches = viewModel.atlasItems().stream().filter(this::matchesSearch).count();
            return matches + " match" + (matches == 1 ? "" : "es")
                    + " for \"" + searchQuery.trim() + "\". Non-matches stay dimmed in place.";
        }

        private String normalizedSearchQuery() {
            return searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
        }

        private String islandSubtitle(SlotWorkspaceViewModel.AtlasIsland island) {
            String count = island.itemCount() + " item" + (island.itemCount() == 1 ? "" : "s");
            return switch (island.kind()) {
                case TRIAGE -> count + " awaiting placement";
                case STARTER -> count + " high-confidence homes";
                case PLAYER -> count + " player-authored homes";
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
            ItemStack iconStack = stack == null ? ItemStack.EMPTY : stack.copy();
            UIElement icon = new UIElement().layout(layout -> layout.width(size).height(size))
                    .style(style -> style.backgroundTexture(new ItemStackTexture(iconStack)));
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
            ItemStack displayStack
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
