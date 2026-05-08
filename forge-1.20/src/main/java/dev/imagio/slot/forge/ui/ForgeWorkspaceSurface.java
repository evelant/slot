package dev.imagio.slot.forge.ui;

import dev.imagio.slot.forge.network.ForgeWorkspaceActionChannel;
import dev.imagio.slot.forge.network.ForgeWorkspaceOpenMessage;
import dev.imagio.slot.forge.network.ForgeWorkspaceRefreshMessage;
import dev.imagio.slot.forge.network.ForgeWorkspaceViewModelClientCache;
import dev.imagio.slot.forge.network.SlotForgeNetworking;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceSearchQuery;
import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;
import dev.imagio.slot.ui.action.WorkspaceActionId;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import dev.imagio.slot.ui.workspace.ActiveChestStripUiBuilder;
import dev.imagio.slot.ui.workspace.HotbarBeltUiBuilder;
import dev.imagio.slot.ui.workspace.KitRackUiBuilder;
import dev.imagio.slot.ui.workspace.RecentsStripUiBuilder;
import dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy;
import dev.imagio.slot.ui.workspace.WallCardUiBuilder;
import dev.imagio.slot.ui.workspace.WallSectionHeaderUiBuilder;
import dev.imagio.slot.ui.workspace.WallSectionUiBuilder;
import dev.imagio.slot.ui.workspace.WayfindingDisplay;
import dev.imagio.slot.ui.workspace.WorkspaceSearchInputPolicy;
import dev.imagio.slot.ui.workspace.WorkspaceUiAttachments;
import dev.imagio.slot.ui.workspace.WorkspaceUiPalette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Forge-local workspace controller shared by the full-screen debug surface and
 * vanilla-container sidebar. Common owns the widget builders and semantics;
 * this class owns Forge session, event, and packet plumbing.
 */
public final class ForgeWorkspaceSurface {
    public static final int WIDTH = 260;

    private static final int STANDALONE_BACKGROUND = 0x96060A0E;
    private static final int SIDEBAR_BACKGROUND = 0xD0060A0E;
    private static final int PANEL = 0xC8162029;
    private static final long REFRESH_INTERVAL_TICKS = 10L;

    private final Mode mode;
    private final WorkspaceActionEnvelope envelope;
    private final ForgeWorkspaceActionChannel actionChannel;
    private final List<SlotWorkspaceViewModel.AtlasIsland> islands = new ArrayList<>();
    private final List<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
    private final List<SlotWorkspaceViewModel.IdentityRef> recents = new ArrayList<>();
    private final List<SlotWorkspaceViewModel.HotbarSlot> hotbarSlots = new ArrayList<>();
    private final Map<SlotWorkspaceViewModel.IdentityRef, SlotWorkspaceViewModel.AtlasItem> byIdentity =
            new LinkedHashMap<>();

    private ForgeSlotUiTree tree;
    private SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.empty();
    private SlotWorkspaceViewModel.OffhandSlot offhand = SlotWorkspaceViewModel.OffhandSlot.empty();
    private SlotWorkspaceViewModel.IdentityRef hoveredIdentity;
    private String searchQuery = "";
    private String lastSentSearchQuery = "";
    private boolean searchActive;
    private long appliedRevision = -1L;
    private long lastRefreshGameTime = Long.MIN_VALUE;
    private String status;
    private boolean openSessionRequested;
    private boolean rebuildRequested = true;
    private boolean kitRackOpen;

    public ForgeWorkspaceSurface(Mode mode) {
        this.mode = mode == null ? Mode.STANDALONE : mode;
        this.envelope = new WorkspaceActionEnvelope(
                UUID.randomUUID().toString(),
                currentMenuContainerId(),
                0L);
        this.actionChannel = new ForgeWorkspaceActionChannel(envelope);
        this.status = this.mode == Mode.SIDEBAR
                ? "opening SLOT sidebar"
                : "waiting for Forge workspace session";
    }

    public void openSessionIfNeeded() {
        if (openSessionRequested) {
            return;
        }
        openSessionRequested = true;
        boolean sent = SlotForgeNetworking.openWorkspaceSession(new ForgeWorkspaceOpenMessage(envelope));
        status = sent ? openedStatus() : "failed to open Forge workspace session";
        if (sent) {
            lastRefreshGameTime = clientGameTime();
        }
        rebuildRequested = true;
    }

    public void tick(int width, int height) {
        openSessionIfNeeded();
        requestViewRefreshIfDue();
        applySyncedViewIfAvailable();
        if (tree == null || rebuildRequested) {
            rebuild(width, height);
            return;
        }
        tree.tick();
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, int width, int height) {
        if (tree == null) {
            rebuild(width, height);
        }
        tree.compute(width, height);
        tree.render(graphics, mouseX, mouseY);
        renderCursorStack(graphics, mouseX, mouseY);
        if (!isCursorCarrying()) {
            tree.renderTooltip(graphics, mouseX, mouseY);
        }
    }

    public void rebuild(int width, int height) {
        rebuildRequested = false;
        float scrollY = tree == null ? 0f : tree.scrollY();
        tree = ForgeSlotUiTree.build(Minecraft.getInstance(), buildRoot());
        tree.compute(width, height);
        tree.setScrollY(scrollY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tree == null) {
            return false;
        }
        return tree.mouseClicked(mouseX, mouseY, button, Screen.hasShiftDown());
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (tree == null) {
            return false;
        }
        return tree.mouseReleased(mouseX, mouseY, button, Screen.hasShiftDown());
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (tree == null) {
            return false;
        }
        return tree.mouseScrolled(mouseX, mouseY, delta, 22f, Screen.hasShiftDown());
    }

    public boolean keyPressed(int keyCode) {
        if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_Z) {
            sendAction(WorkspaceActionId.UNDO, "undo requested");
            return true;
        }
        if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_Y) {
            sendAction(WorkspaceActionId.REDO, "redo requested");
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && isCursorCarrying()) {
            sendAction(WorkspaceActionId.CURSOR_CANCEL, "returning cursor");
            return true;
        }
        int hotbarIndex = hotbarIndexFromKeyCode(keyCode);
        if (hotbarIndex >= 0) {
            if (mode == Mode.SIDEBAR && hoveredIdentity == null && !searchActive) {
                return false;
            }
            handleHotbarKey(hotbarIndex);
            return true;
        }
        WorkspaceSearchInputPolicy.ControlKey controlKey = searchControlKey(keyCode);
        return controlKey != null && applySearchDecision(WorkspaceSearchInputPolicy.keyPressed(
                searchActive,
                searchQuery,
                controlKey));
    }

    public boolean charTyped(char codePoint) {
        return applySearchDecision(WorkspaceSearchInputPolicy.charTyped(
                searchActive,
                searchQuery,
                codePoint,
                false));
    }

    private String openedStatus() {
        if (mode == Mode.SIDEBAR) {
            return "opened chest sidebar";
        }
        return "opened Forge workspace session for menu " + envelope.menuContainerId();
    }

    private void requestViewRefreshIfDue() {
        if (!openSessionRequested) {
            return;
        }
        long gameTime = clientGameTime();
        if (lastRefreshGameTime != Long.MIN_VALUE
                && gameTime >= lastRefreshGameTime
                && gameTime - lastRefreshGameTime < REFRESH_INTERVAL_TICKS) {
            return;
        }
        lastRefreshGameTime = gameTime;
        SlotForgeNetworking.refreshWorkspaceSession(new ForgeWorkspaceRefreshMessage(envelope));
    }

    private void applySyncedViewIfAvailable() {
        SlotWorkspaceViewModel synced = ForgeWorkspaceViewModelClientCache.latestFor(envelope.sessionId());
        if (synced == null || synced.revision() <= appliedRevision) {
            return;
        }
        appliedRevision = synced.revision();
        applyViewModel(synced);
        String diagnostics = synced.diagnostics() == null || synced.diagnostics().isBlank()
                ? ""
                : " · " + synced.diagnostics();
        status = synced.status() + " · rev " + synced.revision()
                + " · items " + synced.atlasItems().size()
                + diagnostics;
        rebuildRequested = true;
    }

    private void applyViewModel(SlotWorkspaceViewModel synced) {
        viewModel = synced == null ? SlotWorkspaceViewModel.empty() : synced;
        islands.clear();
        islands.addAll(viewModel.islands());
        items.clear();
        items.addAll(viewModel.atlasItems());
        byIdentity.clear();
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            byIdentity.put(item.identity(), item);
        }
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.triageItems()) {
            byIdentity.putIfAbsent(item.identity(), item);
        }
        recents.clear();
        recents.addAll(viewModel.recentIdentities());
        hotbarSlots.clear();
        hotbarSlots.addAll(viewModel.hotbarSlots());
        offhand = viewModel.offhand();
    }

    private SlotUiElement buildRoot() {
        if (mode == Mode.SIDEBAR) {
            SlotUiElement root = SlotUiElement.element()
                    .allowHitTest(false)
                    .layout(layout -> layout
                            .widthPercent(100)
                            .heightPercent(100)
                            .alignItems(SlotUiLayout.AlignItems.FLEX_START)
                            .flexDirection(SlotUiLayout.FlexDirection.ROW));
            root.addChild(workspaceColumn(true));
            return root;
        }
        return workspaceColumn(false);
    }

    private SlotUiElement workspaceColumn(boolean sidebarMode) {
        SlotUiElement column = SlotUiElement.panel(sidebarMode ? SIDEBAR_BACKGROUND : STANDALONE_BACKGROUND)
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.width(WIDTH);
                    } else {
                        layout.widthPercent(100).alignItems(SlotUiLayout.AlignItems.CENTER);
                    }
                    layout.heightPercent(100)
                            .paddingAll(8)
                            .gapAll(4)
                            .flexDirection(SlotUiLayout.FlexDirection.COLUMN);
                });
        column.on(SlotUiEventKind.MOUSE_DOWN, event -> {
            if (!isCursorCarrying()) {
                return;
            }
            if (event.button() == 1) {
                event.stopPropagation();
                sendAction(WorkspaceActionId.CURSOR_CANCEL, "returning cursor");
                return;
            }
            if (event.button() == 0) {
                event.stopPropagation();
                sendAction(WorkspaceActionId.CURSOR_SMART_DEPOSIT, "depositing cursor");
            }
        });

        column.addChild(titleRow(sidebarMode));
        if (sidebarMode) {
            column.addChild(activeChestOrEmpty());
            column.addChild(summaryDepositRow());
        }
        column.addChild(searchDepositRow(sidebarMode));
        if (!sidebarMode) {
            SlotUiElement activeChestStrip = new ActiveChestStripUiBuilder(new ActiveChestContext())
                    .strip(viewModel.activeChestPanel());
            if (activeChestStrip != null) {
                column.addChild(activeChestStrip.layout(layout -> layout.width(WIDTH)));
            }
        }
        column.addChild(kitCluster(sidebarMode));
        if (kitRackOpen && sidebarMode) {
            column.addChild(SlotUiElement.element()
                    .layout(layout -> layout.widthPercent(100))
                    .addChild(new KitRackUiBuilder(new KitContext()).rack(viewModel)));
        }
        column.addChild(recents(sidebarMode));
        column.addChild(wallViewport(sidebarMode));
        if (kitRackOpen && !sidebarMode) {
            column.addChild(SlotUiElement.element()
                    .layout(layout -> layout.width(WIDTH))
                    .addChild(new KitRackUiBuilder(new KitContext()).rack(viewModel)));
        }
        column.addChild(hotbar(sidebarMode));
        column.addChild(statusRow(sidebarMode));
        return column;
    }

    private SlotUiElement titleRow(boolean sidebarMode) {
        String title = sidebarMode ? "SLOT" : "SLOT Forge 1.20.1 SPI renderer";
        return SlotUiElement.label(title, WorkspaceUiPalette.ACCENT)
                .layout(layout -> layout.height(12))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.ACCENT)
                        .fontSize(9)
                        .horizontal(sidebarMode
                                ? SlotUiTextStyle.Horizontal.LEFT
                                : SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER));
    }

    private SlotUiElement activeChestOrEmpty() {
        SlotUiElement activeChestStrip = new ActiveChestStripUiBuilder(new ActiveChestContext())
                .strip(viewModel.activeChestPanel());
        if (activeChestStrip != null) {
            return activeChestStrip;
        }
        return SlotUiElement.panel(PANEL)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(ActiveChestStripUiBuilder.STRIP_HEIGHT_PX)
                        .paddingHorizontal(4)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW))
                .addChild(SlotUiElement.label("No active chest", WorkspaceUiPalette.MUTED)
                        .layout(layout -> layout.flex(1).heightPercent(100))
                        .textStyle(style -> style
                                .color(WorkspaceUiPalette.MUTED)
                                .fontSize(7)
                                .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                                .vertical(SlotUiTextStyle.Vertical.CENTER)));
    }

    private SlotUiElement summaryDepositRow() {
        return SlotUiElement.panel(PANEL)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(20)
                        .paddingHorizontal(4)
                        .gapAll(4)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW))
                .addChild(SlotUiElement.label("Items " + viewModel.atlasItems().size(), WorkspaceUiPalette.TEXT)
                        .layout(layout -> layout.flex(1).heightPercent(100))
                        .textStyle(style -> style
                                .color(WorkspaceUiPalette.TEXT)
                                .fontSize(7)
                                .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                                .vertical(SlotUiTextStyle.Vertical.CENTER)))
                .addChild(depositButton(48, 12));
    }

    private SlotUiElement searchDepositRow(boolean sidebarMode) {
        SlotUiElement row = SlotUiElement.panel(PANEL)
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.widthPercent(100);
                    } else {
                        layout.width(WIDTH);
                    }
                    layout.height(16)
                            .paddingHorizontal(6)
                            .gapAll(4)
                            .alignItems(SlotUiLayout.AlignItems.CENTER)
                            .flexDirection(SlotUiLayout.FlexDirection.ROW);
                });
        row.addChild(SlotUiElement.label("Search", WorkspaceUiPalette.MUTED)
                .layout(layout -> layout.width(34))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.MUTED)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)));
        row.addChild(SlotUiElement.label(searchDisplayText(), WorkspaceUiPalette.TEXT)
                .layout(layout -> layout.flex(1))
                .textStyle(style -> style
                        .color(searchActive ? WorkspaceUiPalette.ACCENT : WorkspaceUiPalette.TEXT)
                        .fontSize(8)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)));
        if (!sidebarMode) {
            row.addChild(depositButton(42, 11));
        }
        return row;
    }

    private SlotUiElement depositButton(int width, int height) {
        return SlotUiElement.button("Deposit", true, WorkspaceUiPalette.ROW_HOVER)
                .tooltip(Component.literal(
                        "Deposit carried items into proximate chests by learned affinity. "
                                + "Items without an existing bond stay in carry - drop one in manually first."))
                .layout(layout -> layout.width(width).height(height))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.TEXT)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER))
                .on(SlotUiEventKind.CLICK, event -> {
                    if (event.button() != 0) {
                        return;
                    }
                    event.stopPropagation();
                    sendAction(WorkspaceActionId.DEPOSIT, "depositing carried items");
                });
    }

    private SlotUiElement kitCluster(boolean sidebarMode) {
        return SlotUiElement.panel(PANEL)
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.widthPercent(100);
                    } else {
                        layout.width(WIDTH);
                    }
                    layout.height(26)
                            .paddingHorizontal(6)
                            .gapAll(4)
                            .alignItems(SlotUiLayout.AlignItems.CENTER)
                            .flexDirection(SlotUiLayout.FlexDirection.ROW);
                })
                .addChild(new KitRackUiBuilder(new KitContext())
                        .cluster(viewModel, kitRackOpen)
                        .layout(layout -> layout.flex(1)));
    }

    private SlotUiElement recents(boolean sidebarMode) {
        SlotUiElement strip = new RecentsStripUiBuilder(new RecentsContext()).overlay(recents);
        if (!sidebarMode) {
            strip.layout(layout -> layout.width(WIDTH));
        }
        return strip;
    }

    private SlotUiElement wallViewport(boolean sidebarMode) {
        SlotUiElement viewport = SlotUiElement.panel(0xB810171D)
                .attach(ForgeSlotUiTree.SCROLL_VIEWPORT, Boolean.TRUE)
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.widthPercent(100);
                    } else {
                        layout.width(WIDTH);
                    }
                    layout.flex(1)
                            .paddingAll(4)
                            .flexDirection(SlotUiLayout.FlexDirection.COLUMN);
                });
        SlotUiElement content = SlotUiElement.element()
                .layout(layout -> layout
                        .widthPercent(100)
                        .gapAll(4)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        WallSectionHeaderUiBuilder headerBuilder = new WallSectionHeaderUiBuilder(new HeaderContext());
        WallSectionUiBuilder sectionBuilder = new WallSectionUiBuilder(headerBuilder);
        boolean filtering = !normalizedSearchQuery().isBlank();
        for (SlotWorkspaceViewModel.AtlasIsland island : islands) {
            List<SlotWorkspaceViewModel.AtlasItem> islandItems = items.stream()
                    .filter(item -> island.islandId().equals(item.islandId()))
                    .toList();
            List<SlotWorkspaceViewModel.AtlasItem> visibleItems = islandItems.stream()
                    .filter(item -> !filtering || matchesSearch(item))
                    .toList();
            content.addChild(enrichSection(
                    sectionBuilder.section(island, visibleItems, islandItems.size(), filtering),
                    visibleItems));
        }
        viewport.addChild(content);
        return viewport;
    }

    private SlotUiElement hotbar(boolean sidebarMode) {
        SlotUiElement belt = new HotbarBeltUiBuilder(new HotbarContext()).belt(hotbarSlots, offhand);
        if (!sidebarMode) {
            return SlotUiElement.element()
                    .layout(layout -> layout.width(WIDTH))
                    .addChild(belt);
        }
        return belt;
    }

    private SlotUiElement statusRow(boolean sidebarMode) {
        return SlotUiElement.panel(PANEL)
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.widthPercent(100).height(30).paddingAll(4);
                    } else {
                        layout.width(WIDTH).height(18).paddingHorizontal(6);
                    }
                    layout.alignItems(SlotUiLayout.AlignItems.CENTER)
                            .flexDirection(SlotUiLayout.FlexDirection.ROW);
                })
                .addChild(SlotUiElement.label(status, WorkspaceUiPalette.MUTED)
                        .layout(layout -> layout.flex(1).heightPercent(100))
                        .textStyle(style -> style
                                .color(WorkspaceUiPalette.MUTED)
                                .fontSize(7)
                                .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                                .vertical(SlotUiTextStyle.Vertical.CENTER)));
    }

    private SlotUiElement enrichSection(
            SlotUiElement section,
            List<SlotWorkspaceViewModel.AtlasItem> islandItems
    ) {
        SlotUiElement grid = null;
        for (SlotUiElement child : section.children()) {
            if (child.hasAttachment(WorkspaceUiAttachments.WALL_SECTION_GRID)) {
                grid = child;
                break;
            }
        }
        if (grid == null) {
            return section;
        }
        WallCardUiBuilder cardBuilder = new WallCardUiBuilder(new CardContext());
        for (SlotWorkspaceViewModel.AtlasItem item : islandItems) {
            SlotUiElement card = cardBuilder.card(item);
            float[] wheelAccumulator = {0f};
            card.on(SlotUiEventKind.MOUSE_DOWN, event -> {
                if (event.button() == 0) {
                    event.stopPropagation();
                    return;
                }
                WallCardTransferGesturePolicy.Decision decision = WallCardTransferGesturePolicy.pointerDown(
                        cardGestureContext(item, event.button(), event.shiftDown()));
                if (dispatchCardGestureDecision(item, decision)) {
                    event.stopPropagation();
                }
            });
            card.on(SlotUiEventKind.CLICK, event -> {
                if (event.button() != 0) {
                    return;
                }
                event.stopPropagation();
                WallCardTransferGesturePolicy.Decision decision = WallCardTransferGesturePolicy.click(
                        cardGestureContext(item, event.button(), event.shiftDown()));
                dispatchCardGestureDecision(item, decision);
            });
            card.on(SlotUiEventKind.MOUSE_WHEEL, event -> {
                if (!event.shiftDown() && !Screen.hasControlDown()) {
                    return;
                }
                if (isCursorCarrying()) {
                    return;
                }
                float delta = event.wheelDelta();
                if (delta == 0f) {
                    return;
                }
                event.stopPropagation();
                wheelAccumulator[0] += delta;
                int steps = (int) wheelAccumulator[0];
                if (steps == 0) {
                    return;
                }
                wheelAccumulator[0] -= steps;
                WallCardTransferGesturePolicy.Decision decision = WallCardTransferGesturePolicy.wheel(
                        cardGestureContext(item, 0, event.shiftDown()),
                        steps);
                dispatchCardGestureDecision(item, decision);
            });
            grid.addChild(card);
        }
        return section;
    }

    private String normalizedSearchQuery() {
        return WorkspaceSearchQuery.normalized(searchQuery);
    }

    private boolean matchesSearch(SlotWorkspaceViewModel.AtlasItem item) {
        return WorkspaceSearchQuery.matchesItem(
                searchQuery,
                item,
                item == null ? null : island(item.islandId()));
    }

    private SlotWorkspaceViewModel.AtlasIsland island(String islandId) {
        for (SlotWorkspaceViewModel.AtlasIsland island : islands) {
            if (island.islandId().equals(islandId)) {
                return island;
            }
        }
        return null;
    }

    private String searchDisplayText() {
        if (searchActive) {
            return "/" + searchQuery + "_";
        }
        return searchQuery.isBlank() ? "press /" : searchQuery;
    }

    private void syncSearchQuery() {
        if (searchQuery.equals(lastSentSearchQuery)) {
            return;
        }
        boolean sent = actionChannel.send(WorkspaceActionId.SET_SEARCH_QUERY, searchQuery);
        if (sent) {
            lastSentSearchQuery = searchQuery;
        } else {
            status = "failed to sync search query";
        }
        rebuildRequested = true;
    }

    private void setSearchQuery(String value) {
        String next = WorkspaceSearchQuery.cleanInput(value);
        if (next.equals(searchQuery)) {
            return;
        }
        searchQuery = next;
        syncSearchQuery();
        rebuildRequested = true;
    }

    private boolean applySearchDecision(WorkspaceSearchInputPolicy.Decision decision) {
        if (decision == null || !decision.handled()) {
            return false;
        }
        searchActive = decision.active();
        setSearchQuery(decision.query());
        status = switch (decision.action()) {
            case OPEN -> "search";
            case CONFIRM -> searchQuery.isBlank() ? "search confirmed" : "search: " + searchQuery;
            case DISMISS -> "search dismissed";
            case BACKSPACE, APPEND, IGNORE_DIGIT -> "search";
            case NONE -> status;
        };
        rebuildRequested = true;
        return true;
    }

    private static WorkspaceSearchInputPolicy.ControlKey searchControlKey(int keyCode) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> WorkspaceSearchInputPolicy.ControlKey.ENTER;
            case GLFW.GLFW_KEY_ESCAPE -> WorkspaceSearchInputPolicy.ControlKey.ESCAPE;
            case GLFW.GLFW_KEY_BACKSPACE -> WorkspaceSearchInputPolicy.ControlKey.BACKSPACE;
            default -> null;
        };
    }

    private static int hotbarIndexFromKeyCode(int keyCode) {
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
            return keyCode - GLFW.GLFW_KEY_1;
        }
        if (keyCode >= GLFW.GLFW_KEY_KP_1 && keyCode <= GLFW.GLFW_KEY_KP_9) {
            return keyCode - GLFW.GLFW_KEY_KP_1;
        }
        return -1;
    }

    private void handleHotbarKey(int hotbarIndex) {
        SlotWorkspaceViewModel.IdentityRef identity = hoveredIdentity;
        if (identity == null) {
            applySearchDecision(WorkspaceSearchInputPolicy.confirmForHotbar(searchActive, searchQuery));
            setStatus("hover an atlas item to assign with 1-9");
            return;
        }
        applySearchDecision(WorkspaceSearchInputPolicy.confirmForHotbar(searchActive, searchQuery));
        sendIdentityToHotbar(identity, hotbarIndex);
    }

    private void sendAction(WorkspaceActionId action, String sentStatus, Object... args) {
        boolean sent = actionChannel.send(action, args);
        status = sent ? sentStatus : "failed to send " + action.name().toLowerCase();
        rebuildRequested = true;
    }

    private void sendIdentityToHotbar(SlotWorkspaceViewModel.IdentityRef identity, int hotbarIndex) {
        if (identity == null || hotbarIndex < 0 || hotbarIndex > 8) {
            setStatus("hotbar assign unavailable");
            return;
        }
        sendAction(
                WorkspaceActionId.ASSIGN_IDENTITY_TO_HOTBAR_SLOT,
                "assigning to belt " + (hotbarIndex + 1),
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                hotbarIndex);
    }

    private void setStatus(String nextStatus) {
        status = nextStatus == null || nextStatus.isBlank() ? "ready" : nextStatus;
        rebuildRequested = true;
    }

    private void repeat(int count, Runnable action) {
        int safeCount = Math.max(0, count);
        for (int index = 0; index < safeCount; index++) {
            action.run();
        }
    }

    private WallCardTransferGesturePolicy.Context cardGestureContext(
            SlotWorkspaceViewModel.AtlasItem item,
            int button,
            boolean shiftDown
    ) {
        return new WallCardTransferGesturePolicy.Context(
                item,
                button,
                shiftDown,
                Screen.hasControlDown(),
                cursorIdentity(),
                isCursorCarrying(),
                mode == Mode.SIDEBAR,
                carriedFreeSlotCount(),
                anyChestProximate());
    }

    private boolean dispatchCardGestureDecision(
            SlotWorkspaceViewModel.AtlasItem item,
            WallCardTransferGesturePolicy.Decision decision
    ) {
        if (decision == null || !decision.handled()) {
            return false;
        }
        int count = decision.count();
        switch (decision.action()) {
            case NONE -> {
                return false;
            }
            case STATUS -> setStatus(decision.status());
            case PICKUP_TO_CURSOR -> sendIdentityAction(
                    WorkspaceActionId.PICKUP_TO_CURSOR,
                    item,
                    "picking up " + item.name(),
                    count <= 0 ? WallCardTransferGesturePolicy.PICKUP_MAX : count);
            case CURSOR_CANCEL -> sendAction(WorkspaceActionId.CURSOR_CANCEL, "returning cursor");
            case CURSOR_SMART_DEPOSIT -> sendAction(WorkspaceActionId.CURSOR_SMART_DEPOSIT, "depositing cursor");
            case CURSOR_CANCEL_THEN_PICKUP_TO_CURSOR -> {
                sendAction(WorkspaceActionId.CURSOR_CANCEL, "returning cursor");
                sendIdentityAction(
                        WorkspaceActionId.PICKUP_TO_CURSOR,
                        item,
                        "picking up " + item.name(),
                        count <= 0 ? WallCardTransferGesturePolicy.PICKUP_MAX : count);
            }
            case TAKE_STACK_BY_IDENTITY -> sendIdentityAction(
                    WorkspaceActionId.TAKE_STACK_BY_IDENTITY,
                    item,
                    "taking " + item.name());
            case TAKE_ONE_BY_IDENTITY -> repeat(count, () -> sendIdentityAction(
                    WorkspaceActionId.TAKE_ONE_BY_IDENTITY,
                    item,
                    "taking one " + item.name()));
            case DEPOSIT_HOME_TO_LINKED_CHEST -> sendIdentityAction(
                    WorkspaceActionId.DEPOSIT_HOME_TO_LINKED_CHEST,
                    item,
                    "depositing " + item.name());
            case DEPOSIT_ONE_HOME_TO_LINKED_CHEST -> repeat(count, () -> sendIdentityAction(
                    WorkspaceActionId.DEPOSIT_ONE_HOME_TO_LINKED_CHEST,
                    item,
                    "depositing one " + item.name()));
            case CROSS_SURFACE_QUICK_MOVE -> sendIdentityAction(
                    WorkspaceActionId.CROSS_SURFACE_QUICK_MOVE_ATLAS,
                    item,
                    "shift-clicking to host",
                    count);
            case ADJUST_PLAYER_DESIRED_COUNT -> sendIdentityAction(
                    WorkspaceActionId.ADJUST_PLAYER_DESIRED_COUNT,
                    item,
                    "desired count updated",
                    count);
        }
        return true;
    }

    private void sendIdentityAction(
            WorkspaceActionId action,
            SlotWorkspaceViewModel.AtlasItem item,
            String sentStatus,
            Object... tail
    ) {
        if (item == null || item.identity() == null) {
            setStatus("missing item identity");
            return;
        }
        Object[] args = new Object[3 + (tail == null ? 0 : tail.length)];
        args[0] = item.identity().itemId();
        args[1] = item.identity().comparisonMode();
        args[2] = item.identity().componentFingerprint();
        if (tail != null) {
            System.arraycopy(tail, 0, args, 3, tail.length);
        }
        sendAction(action, sentStatus, args);
    }

    private boolean sendKitAction(WorkspaceActionId action, String sentStatus, Object... args) {
        boolean sent = actionChannel.send(action, args);
        status = sent ? sentStatus : "failed to send " + action.name().toLowerCase();
        rebuildRequested = true;
        return sent;
    }

    private void sendTakeByIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
        if (identity == null) {
            setStatus("invalid kit identity");
            return;
        }
        sendKitAction(
                WorkspaceActionId.TAKE_STACK_BY_IDENTITY,
                "gathering " + identity.itemId(),
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint());
    }

    private int carriedFreeSlotCount() {
        if (appliedRevision < 0 && viewModel.carriedSlotCapacity() == 0) {
            return 9;
        }
        return viewModel.carriedFreeSlotCount();
    }

    private boolean anyChestProximate() {
        for (SlotWorkspaceViewModel.ChestChip chip : viewModel.chestChips()) {
            if (chip != null && chip.proximate()) {
                return true;
            }
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item != null && !item.presence().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isCursorCarrying() {
        ItemStack stack = cursorStack();
        return stack != null && !stack.isEmpty();
    }

    private ItemStack cursorStack() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.containerMenu == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = minecraft.player.containerMenu.getCarried();
        return stack == null ? ItemStack.EMPTY : stack;
    }

    private SlotWorkspaceViewModel.IdentityRef cursorIdentity() {
        ItemStack stack = cursorStack();
        return stack.isEmpty()
                ? null
                : SlotWorkspaceViewModel.IdentityRef.from(ItemIdentityMatcher.create(stack));
    }

    private void renderCursorStack(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack stack = cursorStack();
        if (stack.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int x = mouseX - 8;
        int y = mouseY - 8;
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(minecraft.font, stack, x, y);
    }

    private static int currentMenuContainerId() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.containerMenu == null) {
            return WorkspaceActionEnvelope.NO_MENU_CONTAINER;
        }
        return minecraft.player.containerMenu.containerId;
    }

    private static long clientGameTime() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            return minecraft.level.getGameTime();
        }
        return System.currentTimeMillis() / 50L;
    }

    public enum Mode {
        STANDALONE,
        SIDEBAR
    }

    private final class ActiveChestContext implements ActiveChestStripUiBuilder.Context {
        @Override
        public void claimChestAt(SlotWorkspaceViewModel.ActiveChestPanel panel) {
            if (panel == null || !panel.isPresent()) {
                setStatus("no active chest to claim");
                return;
            }
            sendAction(
                    WorkspaceActionId.CLAIM_CHEST_AT_POS,
                    "claiming chest",
                    panel.dimensionId(),
                    panel.posX(),
                    panel.posY(),
                    panel.posZ());
        }

        @Override
        public void forgetChest(String storageId) {
            if (storageId == null || storageId.isBlank()) {
                setStatus("no claimed chest to forget");
                return;
            }
            sendAction(WorkspaceActionId.FORGET_CHEST, "forgetting chest", storageId);
        }
    }

    private final class HeaderContext implements WallSectionHeaderUiBuilder.Context {
        @Override
        public void beginIslandEdit(SlotWorkspaceViewModel.AtlasIsland island, float screenX, float screenY) {
            setStatus("would edit section " + island.label());
        }
    }

    private final class KitContext implements KitRackUiBuilder.Context {
        @Override
        public void toggleKitRack() {
            kitRackOpen = !kitRackOpen;
            setStatus(kitRackOpen
                    ? "kit rack open (" + viewModel.kits().size() + ")"
                    : "kit rack closed");
        }

        @Override
        public void closeKitRack() {
            kitRackOpen = false;
            setStatus("kit rack closed");
        }

        @Override
        public void saveCurrentBeltAsKit() {
            sendKitAction(WorkspaceActionId.SAVE_KIT, "saving belt as kit", "");
        }

        @Override
        public void activateKit(String kitId) {
            sendKitAction(WorkspaceActionId.ACTIVATE_KIT, "activating kit", kitId);
        }

        @Override
        public void deactivateKit() {
            sendKitAction(WorkspaceActionId.DEACTIVATE_KIT, "deactivating kit");
        }

        @Override
        public void switchActiveKitPage(int direction) {
            sendKitAction(WorkspaceActionId.SWITCH_KIT_PAGE, "switching kit page", direction);
        }

        @Override
        public void addKitPage(String kitId) {
            sendKitAction(WorkspaceActionId.ADD_KIT_PAGE, "adding kit page", kitId);
        }

        @Override
        public void removeKitPage(String kitId, int pageIndex) {
            sendKitAction(WorkspaceActionId.REMOVE_KIT_PAGE, "removing kit page", kitId, pageIndex);
        }

        @Override
        public void clearKitSlot(String kitId, int pageIndex, int slotIndex) {
            sendKitAction(
                    WorkspaceActionId.SET_KIT_SLOT_IDENTITY,
                    "clearing kit slot",
                    kitId,
                    pageIndex,
                    slotIndex,
                    "",
                    "",
                    "");
        }

        @Override
        public void clearKitBring(String kitId, SlotWorkspaceViewModel.IdentityRef identity) {
            if (identity == null) {
                setStatus("invalid kit bring item");
                return;
            }
            sendKitAction(
                    WorkspaceActionId.SET_KIT_SCOPED_DESIRED_COUNT,
                    "clearing kit desired item",
                    kitId,
                    identity.itemId(),
                    identity.comparisonMode(),
                    identity.componentFingerprint(),
                    0);
        }

        @Override
        public void takeStackByIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
            sendTakeByIdentity(identity);
        }

        @Override
        public int proximateCount(SlotWorkspaceViewModel.IdentityRef identity) {
            SlotWorkspaceViewModel.AtlasItem item = byIdentity.get(identity);
            return item == null ? 0 : item.proximateCount();
        }

        @Override
        public void setStatus(String nextStatus) {
            ForgeWorkspaceSurface.this.setStatus(nextStatus);
        }
    }

    private final class RecentsContext implements RecentsStripUiBuilder.Context {
        @Override
        public SlotWorkspaceViewModel.AtlasItem atlasItem(SlotWorkspaceViewModel.IdentityRef identity) {
            return byIdentity.get(identity);
        }

        @Override
        public void focusRecent(SlotWorkspaceViewModel.AtlasItem item) {
            hoveredIdentity = item == null ? null : item.identity();
            setStatus(item == null ? "ready" : item.name());
        }
    }

    private final class CardContext implements WallCardUiBuilder.Context {
        @Override
        public SlotWorkspaceViewModel.IdentityRef activeIdentity() {
            return cursorIdentity();
        }

        @Override
        public String normalizedSearchQuery() {
            return ForgeWorkspaceSurface.this.normalizedSearchQuery();
        }

        @Override
        public boolean matchesItem(SlotWorkspaceViewModel.AtlasItem item) {
            return matchesSearch(item);
        }

        @Override
        public boolean isMapFocusItem(SlotWorkspaceViewModel.AtlasItem item) {
            return item != null && item.identity().equals(hoveredIdentity);
        }

        @Override
        public void hoverAtlasIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
            hoveredIdentity = identity;
        }

        @Override
        public void clearHoveredAtlasIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
            if (identity != null && identity.equals(hoveredIdentity)) {
                hoveredIdentity = null;
            }
        }

        @Override
        public WayfindingDisplay.CardText wayfindingText(SlotWorkspaceViewModel.ChestPresenceEntry entry) {
            if (entry == null) {
                return WayfindingDisplay.CardText.unavailable();
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.level == null) {
                return WayfindingDisplay.CardText.unavailable();
            }
            return WayfindingDisplay.forStorage(
                    entry.storageId(),
                    viewModel.wayfindingTargets(),
                    viewModel.chestChips(),
                    minecraft.level.dimension().location().toString(),
                    minecraft.player.getX(),
                    minecraft.player.getY(),
                    minecraft.player.getZ(),
                    minecraft.player.getYRot());
        }
    }

    private final class HotbarContext implements HotbarBeltUiBuilder.Context {
        @Override
        public void returnHotbarToHome(int hotbarIndex) {
            sendAction(WorkspaceActionId.RETURN_HOTBAR_TO_HOME, "returning belt " + (hotbarIndex + 1), hotbarIndex);
        }

        @Override
        public boolean isCursorCarrying() {
            return ForgeWorkspaceSurface.this.isCursorCarrying();
        }

        @Override
        public void dropCursorAtHotbar(int hotbarIndex, int button) {
            sendAction(
                    WorkspaceActionId.DROP_CURSOR_AT_HOTBAR,
                    "dropping cursor on belt " + (hotbarIndex + 1),
                    hotbarIndex,
                    button);
        }

        @Override
        public void setStatus(String status) {
            ForgeWorkspaceSurface.this.setStatus(status);
        }
    }
}
